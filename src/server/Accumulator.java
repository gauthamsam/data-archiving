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
import java.util.concurrent.PriorityBlockingQueue;

import utils.Constants;
import api.Task;

/**
 * The Class Accumulator.
 */
public class Accumulator {

	/** The map that would contain all the 'get' tasks associated with a bucket. */
	private Map<Integer, Queue<Task>> putMap;
	
	/** The map that would contain all the 'put' tasks associated with a bucket. */
	private Map<Integer, Queue<Task>> getMap;	
	
	/** The priority queue. */
	private BlockingQueue<Integer> queue;	
	
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
			int numThreads = 1; //Runtime.getRuntime().availableProcessors() * Constants.THREADS_PER_PROCESSOR;
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
		System.out.println("Accumulator's constructor");
		this.putMap = new ConcurrentHashMap<Integer, Queue<Task>>();
		this.getMap = new ConcurrentHashMap<Integer, Queue<Task>>();
		this.queue = new PriorityBlockingQueue<Integer>(10, new ScheduleComparator());	
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
	 * Gets the queue.
	 *
	 * @return the queue
	 */
	public BlockingQueue<Integer> getQueue() {
		return queue;
	}

	/**
	 * Sets the queue.
	 *
	 * @param queue the new queue
	 */
	public void setQueue(BlockingQueue<Integer> queue) {
		this.queue = queue;
	}

	/**
	 * Adds the task to put queue.
	 *
	 * @param bucket_hash the bucket_hash
	 * @param task the task
	 */
	public void addToPutQueue(int bucket_hash, Task task) {
		// Check if the putMap already has a task queue for this bucket.
		Queue<Task> tasks = putMap.get(bucket_hash);
		if (tasks == null) { 
			tasks = new LinkedList<>();
		}
		tasks.add(task);
		putMap.put(bucket_hash, tasks);
		System.out.println("Bucket " + bucket_hash + " added to Putmap.");
		// Remove the bucket from the priority queue before adding it.
		// O(n)
		queue.remove(bucket_hash);
		// O(log(n))
		queue.add(bucket_hash);		
	}
	
	/**
	 * Adds the task to get queue.
	 *
	 * @param bucket_hash the bucket_hash
	 * @param task the task
	 */
	public void addToGetQueue(int bucket_hash, Task task) {
		// Check if the getMap already has a task queue for this bucket.		
		Queue<Task> tasks = getMap.get(bucket_hash);		
		if (tasks == null) { 
			tasks = new LinkedList<>();
		}
		
		tasks.add(task);
		getMap.put(bucket_hash, tasks);

		// Remove the bucket from the priority queue before adding it.
		// O(n)
		queue.remove(bucket_hash);
		// O(log(n))
		queue.add(bucket_hash);
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
			System.out.println("Integer 1 " + i1);
			System.out.println("Integer 2 " + i2);
			
			Queue<Task> putQueue1 = putMap.get(i1);
			Queue<Task> getQueue1 = getMap.get(i1);
			
			Queue<Task> putQueue2 = putMap.get(i2);
			Queue<Task> getQueue2 = getMap.get(i2);
			
			
			return ((putQueue1 != null ? putQueue1.size() : 0) + (getQueue1 != null ? getQueue1.size() : 0)) - ((putQueue2 != null ? putQueue2.size() : 0) + (getQueue2 != null ? getQueue2.size() : 0));			
		}
	}
}
