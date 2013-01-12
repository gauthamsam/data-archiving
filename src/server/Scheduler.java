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

	/** The accumulator. */
	private Accumulator accumulator = Accumulator.getInstance();

	
	/* (non-Javadoc)
	 * @see java.lang.Thread#run()
	 */
	public void run() {		
		Map<Integer, Queue<Task>> putMap = accumulator.getPutMap();
		Map<Integer, Queue<Task>> getMap = accumulator.getGetMap();
		Queue<Integer> priorityQueue = accumulator.getQueue();
		
		Queue<Task> putQueue = null;
		Queue<Task> getQueue = null;
		int bucket = 0;
		int maxSize = 0;
		
		while (true) {
			// The task queues of the bucket that has the highest priority can be removed and processed.
			
			// O(log(n))
			bucket = priorityQueue.poll();
			
			getQueue = getMap.get(bucket);
			putQueue = putMap.get(bucket);
			maxSize = getQueue.size() + putQueue.size();

			if (maxSize > Constants.BUFFER_SIZE) {				
				StorageManager.getInstance().processData(bucket, getQueue, putQueue);
				// After processing the tasks, clear them.
				getQueue.clear();
				putQueue.clear();				
				
			} else {
				// O(log(n))
				priorityQueue.add(bucket);
			}
		}
	}

}
