/*
 * @author Gautham Narayanasamy
 */
package api;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

import entities.Status;

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
	
	public void processResponse(List<Status> status) throws RemoteException;
}
