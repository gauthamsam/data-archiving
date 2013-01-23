/*
 * @author Gautham Narayanasamy
 */
package server;

import java.util.Map;
import java.util.Queue;
import java.util.concurrent.BlockingQueue;

import entities.Task;


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
		Map<Integer, Queue<Task>> putMap = accumulator.getPutMap();
		Map<Integer, Queue<Task>> getMap = accumulator.getGetMap();
		BlockingQueue<Integer> priorityQueue = accumulator.getScheduleQueue();
		Map<Integer, Long> timerMap = accumulator.getTimerMap();
		
		Queue<Task> putQueue = null;
		Queue<Task> getQueue = null;
		int bucket = 0;
		
		while (true) {
			try {
				// The task queues of the bucket that has the highest priority can be removed and processed.				
				// O(log(n))			
				bucket = priorityQueue.take();
				
				getQueue = getMap.remove(bucket);
				putQueue = putMap.remove(bucket);
				timerMap.remove(bucket);

				StorageManager.getInstance().processData(bucket, getQueue, putQueue);				
			}
			catch(Exception e) {
				System.out.println("Exception in Scheduler!");
				e.printStackTrace();
				continue;
			}
		}
		
	}

}
