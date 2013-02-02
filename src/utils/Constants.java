/*
 * @author Gautham Narayanasamy
 */
package utils;

/**
 * The Class Constants.
 */
public class Constants {

	/** The Constant BUCKET_DIR. */
	public static final String BUCKET_DIR = "/home/gautham/data-archiving/storage";
	
	/** The Constant DATA_DIR. */
	public static final String DATA_DIR = "/home/gautham/data-archiving/storage";
	
	/** The Constant BUCKET_FILE_EXTENSION. */
	public static final String BUCKET_FILE_EXTENSION = ".bucket";
	
	/** The Constant DATASTORE_FILE_EXTENSION. */
	public static final String DATASTORE_FILE_EXTENSION = ".data";
	
	/** The Constant OPERATION_PUT. */
	public static final int OPERATION_PUT = 0;
	
	/** The Constant OPERATION_GET. */
	public static final int OPERATION_GET = 1;
	
	/** The number of bits used to represent a bucket. */
	public static final int BUCKET_NUM_BITS = 1;

	/** The size of the buffer. Once the number of tasks in the scheduler queue exceeds this, the bucket becomes ready to be scheduled. */
	public static final int BUFFER_SIZE = 100;
	
	/** The Constant THREADS_PER_PROCESSOR. */
	public static final int THREADS_PER_PROCESSOR = 1;
		
	/** The maximum amount of time that a bucket can be a "zombie" without being scheduled. */
	public static final int MAX_TIME_IN_QUEUE = 500;
	
	/** The time period for scheduling tasks that are run by the ExecutorService in the Accumulator. */
	public static final int SCHEDULED_TIMER_PERIOD = 100;
}
