package api;

import java.rmi.Remote;

public interface StorageServer extends Remote{

	public void assignTask(int bucket_hash, Task task);
}
