/*
 * @author Gautham Narayanasamy
 */
package api;

import java.rmi.Remote;
import java.rmi.RemoteException;

import entities.Task;

/**
 * The interface using which the data from the Client is routed to the appropriate Server.
 */
public interface Router extends Remote {

	/** The Constant SERVICE_NAME. */
	public static final String SERVICE_NAME = "Router";
	
	public void setClient(RouterToClient client) throws RemoteException;
	
	/**
	 * Route request.
	 *
	 * @param task the task
	 * @throws RemoteException the remote exception
	 */
	public void routeRequest(Task task) throws RemoteException;

}
