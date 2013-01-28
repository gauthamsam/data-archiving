/*
 * @author Gautham Narayanasamy
 */
package server;

import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

import api.Router;
import api.ServerToRouter;
import api.StorageServer;
import entities.GetTask;
import entities.PutTask;
import entities.Task;
import exceptions.ArchiveException;

/**
 * The Class StorageServerImpl.
 */
public class StorageServerImpl extends UnicastRemoteObject implements StorageServer {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 3592782126605486179L;
	
	private static StorageServerImpl server;
	
	private ServerToRouter router;
	
	public static synchronized StorageServerImpl getInstance() throws RemoteException {
		if (server == null) {
			server = new StorageServerImpl();
		}
		return server;
	}
	
	protected StorageServerImpl() throws RemoteException {
		super();		
	}
	

	public ServerToRouter getRouter() {
		return router;
	}

	public void setRouter(ServerToRouter router) {
		this.router = router;
	}


	/* (non-Javadoc)
	 * @see api.StorageServer#assignTask(int, api.Task)
	 */
	@Override
	public void assignTask(int bucketId, Task task) {
		
		Accumulator accumulator = Accumulator.getInstance();
		// if put task
		if (task instanceof PutTask) {
			accumulator.addToPutQueue(bucketId, (PutTask) task);
		}		
		// if get task
		else if (task instanceof GetTask) {
			accumulator.addToGetQueue(bucketId, (GetTask) task);
		}
		else {
			throw new ArchiveException("Invalid Task");
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
		
		StorageServerImpl server = StorageServerImpl.getInstance();
		router.register(server);
		server.setRouter(router);
		
		System.out.println("Server is ready.");
	}

}
