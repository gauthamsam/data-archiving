package entities;

public class TaskPair {
	
	private int bucketId;
	
	private Task task;
	
	public TaskPair(int bucketId, Task task) {
		this.bucketId = bucketId;
		this.task = task;
	}
	
	public Task getTask() {
		return task;
	}
	public void setTask(Task task) {
		this.task = task;
	}
	public int getBucketId() {
		return bucketId;
	}
	public void setBucketId(int bucketId) {
		this.bucketId = bucketId;
	}
	
	
}
