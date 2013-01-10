/*
 * @author Gautham Narayanasamy
 */
package server.scheduler;

import java.util.Map;
import java.util.Queue;

import server.StorageManager;
import utils.Constants;

import api.Task;

/**
 * The Class Scheduler.
 */
public abstract class Scheduler extends Thread {

	/**
	 * Process queue.
	 *
	 * @param queue the queue
	 * @param map the map
	 */
	public void processQueue(Queue<Integer> queue, Map<Integer, Queue<Task>> map, int taskType) {
		// Check to see which bucket's task queue can be removed and executed.

		// O(log(n))
		int bucket = queue.poll();
		Queue<Task> taskQ = map.get(bucket);
		int maxSize = taskQ.size();		

		if (maxSize > Constants.BUFFER_SIZE) {
			map.remove(bucket);

			// Blocking call
			if (taskType == Constants.TASK_TYPE_GET) {
				StorageManager.readData(bucket, taskQ);
			}
			else {
				StorageManager.writeData(bucket, taskQ);
			}
		} else {
			// O(log(n))
			queue.add(bucket);
		}
	}
}
