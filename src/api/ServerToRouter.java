/*
 * @author Gautham Narayanasamy
 */
package api;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

import entities.Task;

/**
 * The Interface ServerToRouter.
 */
public interface ServerToRouter extends Remote{

	/**
	 * Register.
	 *
	 * @param server the server
	 * @throws RemoteException the remote exception
	 */
	public void register(StorageServer server) throws RemoteException;
	
	/**
	 * Process response from the storage servers.
	 *
	 * @param status the status
	 * @throws RemoteException the remote exception
	 */
	public void processResponse(List<? extends Task> status) throws RemoteException;
}
