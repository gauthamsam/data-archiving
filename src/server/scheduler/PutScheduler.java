/*
 * @author Gautham Narayanasamy
 */
package server.scheduler;

import server.Accumulator;

/**
 * The Class PutScheduler.
 */
public class PutScheduler extends Scheduler{
		
	/* (non-Javadoc)
	 * @see java.lang.Thread#run()
	 */
	public void run() {		
		while(true) {
			processQueue(Accumulator.getInstance().getPutQueue(), Accumulator.getInstance().getPutMap());
		}
	}
}
