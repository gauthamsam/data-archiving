/*
 * @author Gautham Narayanasamy
 */
package utils;

/**
 * The Class Constants.
 */
public class Constants {

	/** The Constant BUCKET_DIR. */
	public static final String BUCKET_DIR = "/home/sam/ucsb/data-archiving/storage";
	
	/** The Constant DATA_DIR. */
	public static final String DATA_DIR = "/home/sam/ucsb/data-archiving/storage";
	
	/** The Constant BUCKET_FILE_EXTENSION. */
	public static final String BUCKET_FILE_EXTENSION = ".bucket";
	
	/** The Constant DATASTORE_FILE_EXTENSION. */
	public static final String DATASTORE_FILE_EXTENSION = ".data";
	
	/** The Constant BUCKET_HASH_NUM_BYTES. */
	public static final int BUCKET_HASH_NUM_BITS = 8;

	/** The size of the buffer. Once the number of tasks in the scheduler queue exceeds this, the bucket becomes ready to be scheduled. */
	public static final int BUFFER_SIZE = 3;
	
	/** The Constant TASK_TYPE_PUT. */
	public static final int TASK_TYPE_PUT = 1;
	
	/** The Constant TASK_TYPE_GET. */
	public static final int TASK_TYPE_GET = 2;
	
	/** The Constant THREADS_PER_PROCESSOR. */
	public static final int THREADS_PER_PROCESSOR = 4;
		
	/** The maximum amount of time that a bucket can be a "zombie" without being scheduled. */
	public static final int MAX_TIME_IN_QUEUE = 500;
	
	/** The time period for scheduling tasks that are run by the ExecutorService in the Accumulator. */
	public static final int SCHEDULED_TIMER_PERIOD = 100;
}
