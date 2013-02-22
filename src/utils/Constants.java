/*
 * @author Gautham Narayanasamy
 */
package utils;

/**
 * The Class Constants.
 */
public class Constants {

	/** The Constant BUCKET_DIR. */
	public static final String BUCKET_DIR = "/home/gautham/data-archiving/new_storage_3";
	
	/** The Constant DATA_DIR. */
	public static final String DATA_DIR = "/home/gautham/data-archiving/new_storage_3";
	
	/** The Constant BUCKET_FILE_EXTENSION. */
	public static final String INDEX_FILE_EXTENSION = ".index";
	
	/** The Constant DATASTORE_FILE_EXTENSION. */
	public static final String DATASTORE_FILE_EXTENSION = ".data";
	
	/** The Constant OPERATION_PUT. */
	public static final int OPERATION_PUT = 0;
	
	/** The Constant OPERATION_GET. */
	public static final int OPERATION_GET = 1;
	
	/** The number of bits used to represent a bucket. */
	public static final int BUCKET_NUM_BITS = 7;

	/** The size of the buffer. Once the number of tasks in the scheduler queue exceeds this, the bucket becomes ready to be scheduled. */
	public static final int BUFFER_SIZE = 500;
	
	/** The Constant THREADS_PER_PROCESSOR. */
	public static final int THREADS_PER_PROCESSOR = 4;
		
	/** The maximum amount of time that a bucket can be a "zombie" without being scheduled (in milliseconds). */
	public static final int MAX_TIME_IN_QUEUE = 1000;
	
	/** The time period for scheduling tasks that are run by the ExecutorService in the Accumulator (in milliseconds). */
	public static final int SCHEDULED_TIMER_PERIOD = 100;

	/** The number of receiver threads in the Router */
	public static final int RESPONSE_ROUTER_THREADS = 2;
}
