/*
 * @author Gautham Narayanasamy
 */
package server;

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
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
	private Queue<Integer> queue;	
	
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
		}
		return accumulator;
	}
	
	/**
	 * Instantiates a new accumulator.
	 */
	private Accumulator() {
		this.putMap = Collections.synchronizedMap(new LinkedHashMap<Integer, Queue<Task>>());
		this.getMap = Collections.synchronizedMap(new LinkedHashMap<Integer, Queue<Task>>());
		this.queue = new PriorityBlockingQueue<Integer>(10, new ScheduleComparator());
	
		// Start the scheduler threads.
		int numThreads = Runtime.getRuntime().availableProcessors() * Constants.THREADS_PER_PROCESSOR;
		for (int i = 0; i < numThreads; i++) {
			new Scheduler().start();
		}
		
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
	public Queue<Integer> getQueue() {
		return queue;
	}

	/**
	 * Sets the queue.
	 *
	 * @param queue the new queue
	 */
	public void setQueue(Queue<Integer> queue) {
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
			return (getMap.get(i1).size() + putMap.get(i1).size()) - (getMap.get(i2).size() + putMap.get(i2).size());			
		}
	}
}
