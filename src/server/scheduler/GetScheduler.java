/*
 * @author Gautham Narayanasamy
 */
package server.scheduler;

import server.Accumulator;

/**
 * The Class GetScheduler.
 */
public class GetScheduler extends Scheduler{
	
	/* (non-Javadoc)
	 * @see java.lang.Thread#run()
	 */
	public void run() {		
		while(true) {
			processQueue(Accumulator.getInstance().getGetQueue(), Accumulator.getInstance().getGetMap());
		}
	}
}
