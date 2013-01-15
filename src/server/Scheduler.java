/*
 * @author Gautham Narayanasamy
 */
package server;

import java.util.Map;
import java.util.Queue;
import java.util.concurrent.BlockingQueue;

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
		System.out.println("Thread " + threadId + " running.");
		
		Map<Integer, Queue<Task>> putMap = accumulator.getPutMap();
		Map<Integer, Queue<Task>> getMap = accumulator.getGetMap();
		BlockingQueue<Integer> priorityQueue = accumulator.getQueue();
		
		Queue<Task> putQueue = null;
		Queue<Task> getQueue = null;
		int bucket = 0;
		int maxSize = 0;
		
		while (true) {
			try{
				// The task queues of the bucket that has the highest priority can be removed and processed.
				System.out.println("Waiting on " + priorityQueue);
				
				// O(log(n))			
				bucket = priorityQueue.take();			
				
				System.out.println("Thread " + threadId + " taking on bucket " + bucket);
				
				getQueue = getMap.remove(bucket);
				putQueue = putMap.remove(bucket);
//				System.out.println("getQueue " + getQueue);
//				System.out.println("putQueue " + putQueue);
				
				maxSize = ((getQueue != null) ? getQueue.size() : 0 ) + ((putQueue != null) ? putQueue.size() : 0);
//				System.out.println("maxSize: " + maxSize);
				
				if (maxSize > Constants.BUFFER_SIZE) {
					StorageManager.getInstance().processData(bucket, getQueue, putQueue);
				}
			}
			catch(Exception e){
				System.out.println("Exception in Scheduler!");
				e.printStackTrace();
				continue;
			}
		}
		
	}

}
