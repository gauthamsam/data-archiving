/*
 * @author Gautham Narayanasamy
 */
package server;

import java.util.Comparator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import utils.Constants;
import entities.Task;

/**
 * The Class Accumulator.
 */
public class Accumulator {

	/** The map that would contain all the 'get' tasks associated with a bucket. */
	private Map<Integer, Queue<Task>> putMap;
	
	/** The map that would contain all the 'put' tasks associated with a bucket. */
	private Map<Integer, Queue<Task>> getMap;	
	
	/** The scheduler queue contains all the buckets that are ready to be scheduled. */
	private BlockingQueue<Integer> scheduleQueue;	
	
	private Map<Integer, Long> timerMap;
	
	/** The accumulator. */
	private static Accumulator accumulator;
	
	/**
	 * Gets the single instance of Accumulator.
	 *
	 * @return single instance of Accumulator
	 */
	public static synchronized Accumulator getInstance() {
		if (accumulator == null) {
			accumulator = new Accumulator();
			
			// Start the scheduler threads.
			int numThreads = 4;//Runtime.getRuntime().availableProcessors() * Constants.THREADS_PER_PROCESSOR;
			for (int i = 1; i <= numThreads; i++) {
				new Scheduler(i, accumulator).start();
			}
		}
		return accumulator;
	}
	
	/**
	 * Instantiates a new accumulator.
	 */
	private Accumulator() {		
		this.putMap = new ConcurrentHashMap<Integer, Queue<Task>>();
		this.getMap = new ConcurrentHashMap<Integer, Queue<Task>>();
		this.scheduleQueue = new PriorityBlockingQueue<Integer>(10, new ScheduleComparator());
		this.timerMap = new ConcurrentHashMap<>();
		new ScheduledTimer().execute();
	}
	
	/**
	 * Gets the put map.
	 *
	 * @return the put map
	 */
	public Map<Integer, Queue<Task>> getPutMap() {
		return putMap;
	}

	/**
	 * Sets the put map.
	 *
	 * @param putMap the put map
	 */
	public void setPutMap(Map<Integer, Queue<Task>> putMap) {
		this.putMap = putMap;
	}

	/**
	 * Gets the gets the map.
	 *
	 * @return the gets the map
	 */
	public Map<Integer, Queue<Task>> getGetMap() {
		return getMap;
	}

	/**
	 * Sets the get map.
	 *
	 * @param getMap the get map
	 */
	public void setGetMap(Map<Integer, Queue<Task>> getMap) {
		this.getMap = getMap;
	}

	/**
	 * Gets the schedule queue.
	 *
	 * @return the queue
	 */
	public BlockingQueue<Integer> getScheduleQueue() {
		return scheduleQueue;
	}

	/**
	 * Sets the schedule queue.
	 *
	 * @param queue the new queue
	 */
	public void setScheduleQueue(BlockingQueue<Integer> queue) {
		this.scheduleQueue = queue;
	}

	/**
	 * Adds the task to put queue.
	 *
	 * @param bucketId the bucketId
	 * @param task the task
	 */
	public void addToPutQueue(int bucketId, Task task) {
		// Check if the putMap already has a task queue for this bucket.
		Queue<Task> tasks = putMap.get(bucketId);
		if (tasks == null) {
			tasks = new LinkedList<>();
		}
		
		tasks.add(task);
		
		// Check if the total number of tasks has exceeded the threshold.
		Queue<Task> getQueue = getMap.get(bucketId);
		int queueSize = ((getQueue != null) ? getQueue.size() : 0 ) + tasks.size();
		if (queueSize > Constants.BUFFER_SIZE) {
			addToScheduleQueue(bucketId);
			timerMap.remove(bucketId);
		}
		else {
			/** When the bucket doesn't have enough requests buffered, mark the time.
			 * When it stays without being scheduled for a specified amount of time, schedule it forcefully.
			 */
			// System.out.println("Added " + bucketId + " to timer map.");
			timerMap.put(bucketId, System.currentTimeMillis());
		}
		
		putMap.put(bucketId, tasks);		
	}
		
	/**
	 * Adds the task to get queue.
	 *
	 * @param bucketId the bucketId
	 * @param task the task
	 */
	public void addToGetQueue(int bucketId, Task task) {
		// Check if the getMap already has a task queue for this bucket.		
		Queue<Task> tasks = getMap.get(bucketId);		
		if (tasks == null) {
			tasks = new LinkedList<>();
		}
		
		tasks.add(task);
		
		// Check if the total number of tasks has exceeded the threshold.
		Queue<Task> putQueue = putMap.get(bucketId);
		int queueSize = ((putQueue != null) ? putQueue.size() : 0 ) + tasks.size();
		if (queueSize > Constants.BUFFER_SIZE) {			
			addToScheduleQueue(bucketId);
		}
		getMap.put(bucketId, tasks);
	}
	
	
	/**
	 * Adds the bucket to schedule queue.
	 *
	 * @param bucketId the bucketId
	 */
	public synchronized void addToScheduleQueue(int bucketId) {
		// Remove the bucket from the priority queue before adding it.
		// O(n)
		scheduleQueue.remove(bucketId); 
		// O(log(n))
		scheduleQueue.add(bucketId);
		
	}
	/**
	 * The Comparator that defines the ordering (priority) of the elements in the priority queue.
	 */
	class ScheduleComparator implements Comparator<Integer> {		
		
		/* (non-Javadoc)
		 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
		 */
		@Override
		public int compare(Integer i1, Integer i2) {
			//System.out.println("Integer 1 " + i1);
			//System.out.println("Integer 2 " + i2);
			
			Queue<Task> putQueue1 = putMap.get(i1);
			Queue<Task> getQueue1 = getMap.get(i1);
			
			Queue<Task> putQueue2 = putMap.get(i2);
			Queue<Task> getQueue2 = getMap.get(i2);
			
			
			return ((putQueue1 != null ? putQueue1.size() : 0) + (getQueue1 != null ? getQueue1.size() : 0)) - ((putQueue2 != null ? putQueue2.size() : 0) + (getQueue2 != null ? getQueue2.size() : 0));			
		}
	}
	
	/**
	 * Class to execute tasks at scheduled time periods.
	 * If a bucket does not contains enough get/put requests to perform reading/writing, they are not scheduled. 
	 * This acts as a timer to check if there are any such tasks which are waiting in the queue for a specified amount of time.
	 * If so, it schedules those tasks.
	 */
	class ScheduledTimer {
		private final ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(5);

		private void execute() {
			final Runnable taskChecker = new Runnable() {
				
                public void run() {                	
                	for (Map.Entry<Integer, Long> entry : timerMap.entrySet()) {
                		int key = entry.getKey();
                		long currentTime = System.currentTimeMillis();
                		// System.out.println("Time difference " + (entry.getValue() - currentTime));
                		if ((currentTime - entry.getValue()) > 200) {
                			System.out.println("Adding " + key + " to scheduler queue.");
                			addToScheduleQueue(key);
                			timerMap.remove(key);
                		}
                	}
            	}
            };
            
			final ScheduledFuture<?> scheduledFuture = scheduledExecutorService.scheduleAtFixedRate(taskChecker, 0, 100, TimeUnit.MILLISECONDS);
			//scheduledExecutorService.shutdown();
		}
	}
}
