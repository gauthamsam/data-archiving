/*
 * @author Gautham Narayanasamy
 */
package server;

import java.rmi.Naming;

import utils.Constants;

import api.Router;
import api.ServerToRouter;
import api.StorageServer;
import api.Task;

/**
 * The Class StorageServerImpl.
 */
public class StorageServerImpl implements StorageServer{

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
		if (task.getType() == Constants.TASK_TYPE_GET) {
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
