/*
 * @author Gautham Narayanasamy
 */
package api;

import java.rmi.Remote;
import java.rmi.RemoteException;

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
}
