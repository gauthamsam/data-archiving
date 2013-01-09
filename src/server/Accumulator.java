/*
 * @author Gautham Narayanasamy
 */
package server;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

import api.Task;

/**
 * The Class Accumulator.
 */
public class Accumulator {

	/** The put queue. */
	private static Map<Integer, Queue<Task>> putQueue = new LinkedHashMap<>();
	
	/** The get queue. */
	private static Map<Integer, Queue<Task>> getQueue = new LinkedHashMap<>();	
	
	static{
		new GetScheduler().start();
		new PutScheduler().start();
	}
	
	/**
	 * Adds the task to put queue.
	 *
	 * @param task the task
	 */
	public static void addToPutQueue(int bucket_hash, Task task) {
		Queue<Task> tasks = putQueue.get(bucket_hash);
		if (tasks == null) { 
			tasks = new LinkedList<>();
		}
		tasks.add(task);
		putQueue.put(bucket_hash, tasks);
	}
	
	/**
	 * Adds the task to get queue.
	 *
	 * @param task the task
	 */
	public static void addToGetQueue(int bucket_hash, Task task) {
		Queue<Task> tasks = getQueue.get(bucket_hash);
		if (tasks == null) { 
			tasks = new LinkedList<>();
		}
		tasks.add(task);
		getQueue.put(bucket_hash, tasks);
	}
	
	/**
	 * The Class Scheduler.
	 */
	static class GetScheduler extends Thread {
		
		/* (non-Javadoc)
		 * @see java.lang.Thread#run()
		 */
		public void run() {
			while(true) {
				// Check to see which bucket's task queue can be removed and executed.
				// Iterating the maps?
				// Should the scheduler check both the maps?
				// Can get and put execute simultaneously?
			}
		}
	}
	
	/**
	 * The Class Scheduler.
	 */
	static class PutScheduler extends Thread {
		
		/* (non-Javadoc)
		 * @see java.lang.Thread#run()
		 */
		public void run() {
			while(true) {
				// Check to see which bucket's task queue can be removed and executed.
				// Iterating the maps?
				// Should the scheduler check both the maps?
				// Can get and put execute simultaneously?
			}
		}
	}
	
}
