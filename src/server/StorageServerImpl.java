/*
 * @author Gautham Narayanasamy
 */
package server;

import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import api.Router;
import api.ServerToRouter;
import api.StorageServer;
import entities.GetTask;
import entities.PutTask;
import entities.Task;
import exceptions.ArchiveException;

/**
 * The main class that represents the Storage Server.
 */
public class StorageServerImpl extends UnicastRemoteObject implements StorageServer {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 3592782126605486179L;
	
	/** The server. */
	private static StorageServerImpl server;
	
	/** The router. */
	private ServerToRouter router;
	
	/** The status queue. */
	private BlockingQueue<List<? extends Task>> statusQueue;
	
	/**
	 * Gets the single instance of StorageServerImpl.
	 *
	 * @return single instance of StorageServerImpl
	 * @throws RemoteException the remote exception
	 */
	public static synchronized StorageServerImpl getInstance() throws RemoteException {
		if (server == null) {
			server = new StorageServerImpl();
		}
		return server;
	}
	
	/**
	 * Instantiates a new storage server impl.
	 *
	 * @throws RemoteException the remote exception
	 */
	protected StorageServerImpl() throws RemoteException {
		super();
		statusQueue = new LinkedBlockingQueue<>();
		// Start the dispatcher thread.
		new ResponseDispatcher().start();
	}
	

	/**
	 * Gets the router.
	 *
	 * @return the router
	 */
	public ServerToRouter getRouter() {
		return router;
	}

	/**
	 * Sets the router.
	 *
	 * @param router the new router
	 */
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
	 * Process response.
	 *
	 * @param statusList the status list
	 */
	public void processResponse(List<? extends Task> statusList) {
		statusQueue.add(statusList);
	}
	
	
	/**
	 * The thread that is responsible for dispatching the response to the router.
	 */
	class ResponseDispatcher extends Thread {
		
		/* (non-Javadoc)
		 * @see java.lang.Thread#run()
		 */
		public void run() {
			while(true) {
				try {
					List<? extends Task> status = statusQueue.take();
					router.processResponse(status);
				} catch (InterruptedException e) {
					e.printStackTrace();
				} catch (RemoteException e) {
					e.printStackTrace();
				}
			}
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
