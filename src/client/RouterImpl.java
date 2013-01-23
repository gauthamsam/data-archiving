/*
 * @author Gautham Narayanasamy
 */
package client;

import java.rmi.RMISecurityManager;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import utils.Constants;
import api.Router;
import api.RouterToClient;
import api.ServerToRouter;
import api.StorageServer;
import entities.Status;
import entities.Task;
import entities.TaskPair;
import exceptions.ArchiveException;

/**
 * The Class RouterImpl contains the implementations of the logic to route the archiving requests to the appropriate Storage servers based on the hash.
 */
public class RouterImpl extends UnicastRemoteObject implements Router, ServerToRouter{

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = -3548712810447954655L;

	/** The mapping between the server number and the storage server. */
	private Map<Integer, StorageServerProxy> serverMap;
	
	/** The router implementation instance. */
	private static RouterImpl router;
	
	/** The Client interface that will be used during asynchronous callback. */
	private RouterToClient client;
	
	/** The number of storage servers registered with the Router. */
	private int numServers;
	
	/**
	 * Gets the single instance of RouterImpl.
	 *
	 * @return single instance of RouterImpl
	 * @throws RemoteException the remote exception
	 */
	public static RouterImpl getInstance() throws RemoteException {
		if(router == null) {
			router = new RouterImpl();
		}
		return router;	
	}
	
	/**
	 * Instantiates a new router impl.
	 *
	 * @throws RemoteException the remote exception
	 */
	private RouterImpl() throws RemoteException {
		super();
		serverMap = new HashMap<>();
		numServers = 0;
	}

	/* (non-Javadoc)
	 * @see api.Router#setClient(api.RouterToClient)
	 */
	public void setClient(RouterToClient client) throws RemoteException {
		this.client = client;
	}

	/* (non-Javadoc)
	 * @see api.ServerToRouter#register(api.StorageServer)
	 */
	@Override
	public synchronized void register(StorageServer server) throws RemoteException {
		System.out.println("Server " + numServers + " registered!");		
		StorageServerProxy proxy = new StorageServerProxy(numServers, server);
		proxy.start();
		serverMap.put(numServers, proxy);		
		numServers++;
	}
	
	/**
	 * Based on the hash value, route the request to the appropriate StorageServer.
	 *
	 * @param task the task
	 * @throws RemoteException the remote exception
	 */
	public void routeRequest(Task task) throws RemoteException {
		byte[] hash = task.getHash().getBytes();
		
		int value = 0;
		int numBytes = Constants.BUCKET_HASH_NUM_BYTES;
		
		if (numBytes > 4) {
			throw new ArchiveException("The number of bytes exceeds 4!");
		}
		
		// Getting the integer corresponding to the first 'numBytes' of the hash.
		for(int i = 0; i < numBytes; i++) {
			value = (value << 8) | hash[i];
		}
		System.out.println("Bucket hash value: " + value);
		
		int modValue = (value < 0) ? (numServers - (Math.abs(value) % numServers) ) % numServers : (value % numServers);
		System.out.println("Routing to Server " + modValue);
		serverMap.get(modValue).assignTask(new TaskPair(value, task));		
	}
	
	/* (non-Javadoc)
	 * @see api.ServerToRouter#processResponse(entities.Status)
	 */
	public void processResponse(List<Status> status) throws RemoteException {
		client.setStatus(status);
	}
	
	/**
	 * Proxy class for the StorageServer.
	 * 
	 *
	 */
	class StorageServerProxy extends Thread {
		private BlockingQueue<TaskPair> taskQueue;		
		private StorageServer server;
		private int id;
		private int numRequests = 0;
		
		public StorageServerProxy(int id, StorageServer server) {
			this.id = id;
			this.server = server;
			this.taskQueue = new LinkedBlockingQueue<>();
		}
		
		public void run() {
			while(true) {
				try {
					TaskPair taskPair = taskQueue.take();
					server.assignTask(taskPair.getBucketId(), taskPair.getTask());					
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				catch (RemoteException e) {
					e.printStackTrace();					
				}
			}
		}
		
		public void assignTask(TaskPair taskPair) {
			taskQueue.add(taskPair);
			numRequests ++;
			System.out.println("Num requests routed: " + numRequests);
		}
		
	}
	
	/**
	 * The main method.
	 *
	 * @param args the arguments
	 * @throws Exception the exception
	 */
	public static void main(String[] args) throws Exception {
		// Construct & set a security manager to allow downloading of classes
		// from a remote codebase
		System.setSecurityManager(new RMISecurityManager());
		// instantiate a router object
		Router router = RouterImpl.getInstance();
		// construct an rmiregistry within this JVM using the default port
		Registry registry = LocateRegistry.createRegistry(1099);
		// bind router in rmiregistry.
		registry.rebind(Router.SERVICE_NAME, router);
		
		System.out.println("Router is ready.");
	}

}