/*
 * @author Gautham Narayanasamy
 */
package server;

import java.util.Map;
import java.util.Queue;
import java.util.concurrent.BlockingQueue;

import entities.GetTask;
import entities.PutTask;


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
		System.out.println("Starting scheduler thread " + threadId);
		Map<Integer, Queue<PutTask>> putMap = accumulator.getPutMap();
		Map<Integer, Queue<GetTask>> getMap = accumulator.getGetMap();
		BlockingQueue<Integer> priorityQueue = accumulator.getScheduleQueue();
		Map<Integer, Long> timerMap = accumulator.getTimerMap();
		
		Queue<PutTask> putQueue = null;
		Queue<GetTask> getQueue = null;
		int bucket = 0;
		
		while (true) {
			try {
				// The task queues of the bucket that has the highest priority can be removed and processed.				
				// O(log(n))			
				bucket = priorityQueue.take();
				
				getQueue = getMap.remove(bucket);
				putQueue = putMap.remove(bucket);
				timerMap.remove(bucket);
				long currentTime = System.currentTimeMillis();
				
				StorageManager.getInstance().processData(bucket, getQueue, putQueue, currentTime);				
			}
			catch(Exception e) {
				System.out.println("Exception in Scheduler!");
				e.printStackTrace();
				continue;
			}
		}
		
	}
}
