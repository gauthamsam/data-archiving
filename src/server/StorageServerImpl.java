/*
 * @author Gautham Narayanasamy
 */
package server;

import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

import utils.Constants;

import api.Router;
import api.ServerToRouter;
import api.StorageServer;
import api.Task;

/**
 * The Class StorageServerImpl.
 */
public class StorageServerImpl extends UnicastRemoteObject implements StorageServer {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 3592782126605486179L;
	
	protected StorageServerImpl() throws RemoteException {
		super();		
	}

	/* (non-Javadoc)
	 * @see api.StorageServer#assignTask(int, api.Task)
	 */
	@Override
	public void assignTask(int bucket_hash, Task task) {
		
		Accumulator accumulator = Accumulator.getInstance();
		// if put task
		if (task.getType() == Constants.TASK_TYPE_PUT) {
			accumulator.addToPutQueue(bucket_hash, task);
		}		
		// if get task
		else if (task.getType() == Constants.TASK_TYPE_GET) {
			accumulator.addToGetQueue(bucket_hash, task);
		}
	}
	
	/**
	 * The main method.
	 *
	 * @param args the arguments
	 * @throws Exception the exception
	 */
	public static void main(String[] args) throws Exception {
		String routerDomainName = args[0];
		
		String routerURL = "//" + routerDomainName + "/" + Router.SERVICE_NAME;
		ServerToRouter router = (ServerToRouter) Naming.lookup(routerURL);
		
		StorageServer server = new StorageServerImpl(); // can throw RemoteException
		router.register(server);
		System.out.println("Server is ready.");
	}

}
