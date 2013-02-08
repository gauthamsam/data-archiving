/*
 * @author Gautham Narayanasamy
 */
package server;

import java.util.Comparator;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import utils.Constants;
import entities.GetTask;
import entities.PutTask;

/**
 * The Class Accumulator.
 */
public class Accumulator {

	/** The map that would contain all the 'get' tasks associated with a bucket. */
	private Map<Integer, Queue<PutTask>> putMap;
	
	/** The map that would contain all the 'put' tasks associated with a bucket. */
	private Map<Integer, Queue<GetTask>> getMap;	
	
	/** The scheduler queue contains all the buckets that are ready to be scheduled. */
	private BlockingQueue<Integer> scheduleQueue;	
	
	/** The map that would contain the time that each bucket has spent without being scheduled. */
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
			int numThreads = Constants.THREADS_PER_PROCESSOR; //Runtime.getRuntime().availableProcessors() * Constants.THREADS_PER_PROCESSOR;
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
		this.putMap = new ConcurrentHashMap<Integer, Queue<PutTask>>();
		this.getMap = new ConcurrentHashMap<Integer, Queue<GetTask>>();
		this.scheduleQueue = new PriorityBlockingQueue<Integer>(10, new ScheduleComparator());
		this.timerMap = new ConcurrentHashMap<Integer, Long>();
		new ScheduledTimer().execute();
	}
	
	/**
	 * Gets the put map.
	 *
	 * @return the put map
	 */
	public Map<Integer, Queue<PutTask>> getPutMap() {
		return putMap;
	}

	/**
	 * Sets the put map.
	 *
	 * @param putMap the put map
	 */
	public void setPutMap(Map<Integer, Queue<PutTask>> putMap) {
		this.putMap = putMap;
	}

	/**
	 * Gets the gets the map.
	 *
	 * @return the gets the map
	 */
	public Map<Integer, Queue<GetTask>> getGetMap() {
		return getMap;
	}

	/**
	 * Sets the get map.
	 *
	 * @param getMap the get map
	 */
	public void setGetMap(Map<Integer, Queue<GetTask>> getMap) {
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
	 * Gets the timer map.
	 *
	 * @return the timer map
	 */
	public Map<Integer, Long> getTimerMap() {
		return timerMap;
	}

	/**
	 * Sets the timer map.
	 *
	 * @param timerMap the timer map
	 */
	public void setTimerMap(Map<Integer, Long> timerMap) {
		this.timerMap = timerMap;
	}

	/**
	 * Adds the task to put queue.
	 *
	 * @param bucketId the bucketId
	 * @param task the task
	 */
	public void addToPutQueue(int bucketId, PutTask task) {
		// Check if the putMap already has a task queue for this bucket.
		Queue<PutTask> tasks = putMap.get(bucketId);
		if (tasks == null) {
			tasks = new LinkedBlockingQueue<PutTask>();
		}
		
		tasks.add(task);
		
		// Check if the total number of tasks has exceeded the threshold.
		Queue<GetTask> getQueue = getMap.get(bucketId);
		int queueSize = ((getQueue != null) ? getQueue.size() : 0 ) + tasks.size();
		if (queueSize >= Constants.BUFFER_SIZE) {
			addToScheduleQueue(bucketId);			
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
	public void addToGetQueue(int bucketId, GetTask task) {
		// Check if the getMap already has a task queue for this bucket.		
		Queue<GetTask> tasks = getMap.get(bucketId);		
		if (tasks == null) {
			tasks = new LinkedBlockingQueue<GetTask>();
		}
		
		tasks.add(task);
		
		// Check if the total number of tasks has exceeded the threshold.
		Queue<PutTask> putQueue = putMap.get(bucketId);
		int queueSize = ((putQueue != null) ? putQueue.size() : 0 ) + tasks.size();
		if (queueSize >= Constants.BUFFER_SIZE) {			
			addToScheduleQueue(bucketId);
		}
		else {
			/** 
			 * When the bucket doesn't have enough requests buffered, mark the time.
			 * When it stays without being scheduled for a specified amount of time, schedule it forcefully.
			 */
			// System.out.println("Added " + bucketId + " to timer map.");
			timerMap.put(bucketId, System.currentTimeMillis());
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
		timerMap.remove(bucketId);
	}
	
	
	/**
	 * The Comparator that defines the ordering (priority) of the elements in the priority queue.
	 * It is a function of both time (time that is spent waiting before being scheduled) and number of put and get requests.
	 */
	class ScheduleComparator implements Comparator<Integer> {		
		
		/* (non-Javadoc)
		 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
		 */
		@Override
		public synchronized int compare(Integer i1, Integer i2) {			
			Queue<PutTask> putQueue1 = putMap.get(i1);
			Queue<GetTask> getQueue1 = getMap.get(i1);
			
			Queue<PutTask> putQueue2 = putMap.get(i2);
			Queue<GetTask> getQueue2 = getMap.get(i2);
			Long time1 = timerMap.get(i1);
			Long time2 = timerMap.get(i2);
			
			long currentTime = System.currentTimeMillis();
						
			long term1 = (putQueue1 != null ? putQueue1.size() : 0) + (getQueue1 != null ? getQueue1.size() : 0) + (time1 != null ? (currentTime - time1) : 0);
			long term2 = (putQueue2 != null ? putQueue2.size() : 0) + (getQueue2 != null ? getQueue2.size() : 0) + (time2 != null ? (currentTime - time2) : 0);
						
			return (term1 - term2) > 0 ? 1 : (term1 - term2) < 0 ? -1 : 0;			
		}
	}
	
	/**
	 * Class to execute tasks at scheduled time periods.
	 * If a bucket does not contain enough get/put requests to perform reading/writing, they are not scheduled. 
	 * This acts as a timer to check if there are any such tasks which are waiting in the queue for a specified amount of time.
	 * If so, it schedules those tasks.
	 */
	class ScheduledTimer {
		
		/** The scheduled executor service. */
		private final ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(1);

		/**
		 * Execute.
		 */
		private void execute() {
			final Runnable taskChecker = new Runnable() {
				
                public void run() {                	
                	for (Map.Entry<Integer, Long> entry : timerMap.entrySet()) {
                		int key = entry.getKey();
                		long currentTime = System.currentTimeMillis();
                		
                		if ((currentTime - entry.getValue()) > Constants.MAX_TIME_IN_QUEUE) {                			
                			// System.out.println("Adding " + key + " to scheduler queue.");
                			addToScheduleQueue(key);
                		}
                	}
            	}
            };
            
			scheduledExecutorService.scheduleAtFixedRate(taskChecker, 0, Constants.SCHEDULED_TIMER_PERIOD, TimeUnit.MILLISECONDS);			
		}
	}
}
