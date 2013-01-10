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

import server.scheduler.GetScheduler;
import server.scheduler.PutScheduler;
import api.Task;

/**
 * The Class Accumulator.
 */
public class Accumulator {

	/** The put map. */
	private Map<Integer, Queue<Task>> putMap;
	
	/** The get map. */
	private Map<Integer, Queue<Task>> getMap;	
	
	/** The get queue. */
	private Queue<Integer> getQueue;
	
	/** The put queue. */
	private Queue<Integer> putQueue;
	
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
	private Accumulator(){
		this.putMap = Collections.synchronizedMap(new LinkedHashMap<Integer, Queue<Task>>());
		this.getMap = Collections.synchronizedMap(new LinkedHashMap<Integer, Queue<Task>>());
		this.getQueue = new PriorityBlockingQueue<Integer>(10, new PutComparator());
		this.putQueue = new PriorityBlockingQueue<Integer>(10, new PutComparator());
		
		new GetScheduler().start();
		new PutScheduler().start();
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
	 * Gets the gets the queue.
	 *
	 * @return the gets the queue
	 */
	public Queue<Integer> getGetQueue() {
		return getQueue;
	}

	/**
	 * Sets the gets the queue.
	 *
	 * @param getQueue the new gets the queue
	 */
	public void setGetQueue(Queue<Integer> getQueue) {
		this.getQueue = getQueue;
	}

	/**
	 * Gets the put queue.
	 *
	 * @return the put queue
	 */
	public Queue<Integer> getPutQueue() {
		return putQueue;
	}

	/**
	 * Sets the put queue.
	 *
	 * @param putQueue the new put queue
	 */
	public void setPutQueue(Queue<Integer> putQueue) {
		this.putQueue = putQueue;
	}

	/**
	 * Adds the task to put queue.
	 *
	 * @param bucket_hash the bucket_hash
	 * @param task the task
	 */
	public void addToPutQueue(int bucket_hash, Task task) {
		Queue<Task> tasks = putMap.get(bucket_hash);
		if (tasks == null) { 
			tasks = new LinkedList<>();
		}
		tasks.add(task);
		putMap.put(bucket_hash, tasks);
		
		// O(n)
		putQueue.remove(bucket_hash);
		// O(log(n))
		putQueue.add(bucket_hash);
		
	}
	
	/**
	 * Adds the task to get queue.
	 *
	 * @param bucket_hash the bucket_hash
	 * @param task the task
	 */
	public void addToGetQueue(int bucket_hash, Task task) {
		Queue<Task> tasks = getMap.get(bucket_hash);
		
		if (tasks == null) { 
			tasks = new LinkedList<>();
		}
		tasks.add(task);
		getMap.put(bucket_hash, tasks);

		// O(n)
		getQueue.remove(bucket_hash);
		// O(log(n))
		getQueue.add(bucket_hash);
		
	}
	
	/**
	 * The Class PutComparator.
	 */
	class PutComparator implements Comparator<Integer>{		
		
		/* (non-Javadoc)
		 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
		 */
		@Override
		public int compare(Integer i1, Integer i2) {			
			return putMap.get(i1).size() - putMap.get(i2).size();			
		}
	}
	
	/**
	 * The Class GetComparator.
	 */
	class GetComparator implements Comparator<Integer>{		
		
		/* (non-Javadoc)
		 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
		 */
		@Override
		public int compare(Integer i1, Integer i2) {			
			return getMap.get(i1).size() - getMap.get(i2).size();			
		}
	}
}
