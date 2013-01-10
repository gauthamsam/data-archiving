/*
 * @author Gautham Narayanasamy
 */
package server.scheduler;

import server.Accumulator;
import utils.Constants;

/**
 * The Class PutScheduler.
 */
public class PutScheduler extends Scheduler{
		
	/* (non-Javadoc)
	 * @see java.lang.Thread#run()
	 */
	public void run() {		
		Accumulator accumulator = Accumulator.getInstance();
		
		while(true) {
			processQueue(accumulator.getPutQueue(), accumulator.getPutMap(), Constants.TASK_TYPE_PUT);
		}
	}
}
