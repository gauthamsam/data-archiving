/*
 * @author Gautham Narayanasamy
 */
package api;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * The Interface StorageServer.
 */
public interface StorageServer extends Remote{

	/**
	 * Assign task to the storage server. The call would return to the storage client after assigning the task.
	 *
	 * @param bucket_hash the bucket_hash
	 * @param task the task
	 */
	public void assignTask(int bucket_hash, Task task) throws RemoteException;
}
