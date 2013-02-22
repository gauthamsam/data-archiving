/*
 * @author Gautham Narayanasamy
 */
package entities;

/**
 * The Class TaskPair.
 */
public class TaskPair {
	
	/** The bucket id. */
	private int bucketId;
	
	/** The task. */
	private Task task;
	
	/**
	 * Instantiates a new task pair.
	 *
	 * @param bucketId the bucket id
	 * @param task the task
	 */
	public TaskPair(int bucketId, Task task) {
		this.bucketId = bucketId;
		this.task = task;
	}
	
	/**
	 * Gets the task.
	 *
	 * @return the task
	 */
	public Task getTask() {
		return task;
	}
	
	/**
	 * Sets the task.
	 *
	 * @param task the new task
	 */
	public void setTask(Task task) {
		this.task = task;
	}
	
	/**
	 * Gets the bucket id.
	 *
	 * @return the bucket id
	 */
	public int getBucketId() {
		return bucketId;
	}
	
	/**
	 * Sets the bucket id.
	 *
	 * @param bucketId the new bucket id
	 */
	public void setBucketId(int bucketId) {
		this.bucketId = bucketId;
	}
	
	
}
