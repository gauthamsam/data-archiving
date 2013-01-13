/*
 * @author Gautham Narayanasamy
 */
package server;

import java.util.Map;
import java.util.Queue;

import utils.Constants;
import api.Task;

/**
 * The Class Scheduler.
 */
public class Scheduler extends Thread {

	private int threadId;
	
	/** The accumulator. */
	private Accumulator accumulator;

	
	public Scheduler(int threadId, Accumulator accumulator) {
		this.threadId = threadId;
		this.accumulator = accumulator;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Thread#run()
	 */
	public void run() {
		try{
		System.out.println("Thread " + threadId + " running.");
		System.out.println("Accumulator: " + accumulator);
		Map<Integer, Queue<Task>> putMap = accumulator.getPutMap();
		Map<Integer, Queue<Task>> getMap = accumulator.getGetMap();
		Queue<Integer> priorityQueue = accumulator.getQueue();
		
		Queue<Task> putQueue = null;
		Queue<Task> getQueue = null;
		int bucket = 0;
		int maxSize = 0;
		
		while (true) {
			// The task queues of the bucket that has the highest priority can be removed and processed.
			System.out.println("Waiting");
			// O(log(n))
			bucket = priorityQueue.poll();
			System.out.println("Bucket to be processed: " + bucket);
			System.out.println("getMap " + getMap);
			getQueue = getMap.get(bucket);
			putQueue = putMap.get(bucket);
			System.out.println("getQueue " + getQueue);
			System.out.println("putQueue " + putQueue);
			
			maxSize = ((getQueue != null) ? getQueue.size() : 0 ) + ((putQueue != null) ? putQueue.size() : 0);
			System.out.println("maxSize: " + maxSize);
			
			if (maxSize > Constants.BUFFER_SIZE) {
				StorageManager.getInstance().processData(bucket, getQueue, putQueue);
				// After processing the tasks, clear them.
				if(getQueue != null) {
					getQueue.clear();
				}
				if(putQueue != null) {
					putQueue.clear();		
				}
			}
		}
		}
		catch(Exception e){
			System.out.println("here");
			e.printStackTrace();
		}
	}

}
