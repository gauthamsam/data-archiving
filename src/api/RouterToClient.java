/*
 * @author Gautham Narayanasamy
 */
package api;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

import entities.Status;

/**
 * The interface that the Router will use to communicate with the Client during callback. 
 */
public interface RouterToClient extends Remote{
	
	/**
	 * Sets the status of the submitted tasks.
	 *
	 * @param status the new status
	 * @throws RemoteException the remote exception
	 */
	public void setStatus (List<Status> status) throws RemoteException;

}
