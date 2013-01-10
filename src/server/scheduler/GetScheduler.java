/*
 * @author Gautham Narayanasamy
 */
package server.scheduler;

import server.Accumulator;
import utils.Constants;

/**
 * The Class GetScheduler.
 */
public class GetScheduler extends Scheduler{
	
	/* (non-Javadoc)
	 * @see java.lang.Thread#run()
	 */
	public void run() {
		Accumulator accumulator = Accumulator.getInstance();
		
		while(true) {
			processQueue(accumulator.getGetQueue(), accumulator.getGetMap(), Constants.TASK_TYPE_GET);
		}
	}
}
