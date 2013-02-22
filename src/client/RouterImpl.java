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
	
	/** The status queue. */
	private BlockingQueue<List<? extends Task>> statusQueue = new LinkedBlockingQueue<List<? extends Task>>();
	
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
		serverMap = new HashMap<Integer, StorageServerProxy>();
		numServers = 0;
		for(int i = 0; i < Constants.RESPONSE_ROUTER_THREADS; i++) {
			new ResponseRouter().start();
		}
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
		byte[] hash = task.getHash();
		int value = 0;
		int numBits = Constants.BUCKET_NUM_BITS;
		
		if (numBits > Integer.SIZE) {
			throw new ArchiveException("The number of bits exceeds the Integer maximum!");
		}
		
		int bucketValue = 0;
		
		// numBits will be set to 0 when only 1 bucket needs to be used.
		if(numBits > 0) {
			int mask = (1 << numBits) - 1; // creates an int value with 'numBits' ones in binary. Acts as the bit mask.
			// Getting the integer corresponding to the first 'numBytes' of the hash.
			for(int i = 0; i < Integer.SIZE; i += 8) {
				if(i >= numBits) {
					bucketValue = value & mask;
					break;
				}
				value = (value << 8) | (hash[20- i/8 - 1] & 0xff);
			}			
		}
		
		// System.out.println("Bucket hash value: " + bucketValue);
		
		//int modValue = (bucketValue < 0) ? (numServers - (Math.abs(bucketValue) % numServers) ) % numServers : (bucketValue % numServers);
		int modValue = bucketValue % numServers;
        //if(bucketValue == 111) System.out.println("byte[] " + Arrays.toString(hash)); 
        // System.out.println("Routing to Server " + modValue);
		serverMap.get(modValue).assignTask(new TaskPair(bucketValue, task));		
	}
	
	/* (non-Javadoc)
	 * @see api.ServerToRouter#processResponse(entities.Status)
	 */
	public void processResponse(List<? extends Task> status) throws RemoteException {
		statusQueue.add(status);
	}
	
	/**
	 * Proxy class for the StorageServer.
	 * 
	 *
	 */
	class StorageServerProxy extends Thread {
		
		/** The task queue. */
		private BlockingQueue<TaskPair> taskQueue;		
		
		/** The server. */
		private StorageServer server;
		
		/** The id. */
		private int id;
		
		/** The num requests. */
		private int numRequests = 0;
		
		/**
		 * Instantiates a new storage server proxy.
		 *
		 * @param id the id
		 * @param server the server
		 */
		public StorageServerProxy(int id, StorageServer server) {
			this.id = id;
			this.server = server;
			this.taskQueue = new LinkedBlockingQueue<TaskPair>();
		}
		
		/* (non-Javadoc)
		 * @see java.lang.Thread#run()
		 */
		public void run() {
			System.out.println("Starting proxy thread " + id);
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
		
		/**
		 * Assign task.
		 *
		 * @param taskPair the task pair
		 */
		public void assignTask(TaskPair taskPair) {
			taskQueue.add(taskPair);
			numRequests ++;
			// System.out.println("Num requests routed: " + numRequests);
		}
		
	}
	
	
	/**
	 * Thread that is responsible for routing the response to the client.
	 */
	class ResponseRouter extends Thread {		
		
		/* (non-Javadoc)
		 * @see java.lang.Thread#run()
		 */
		public void run() {
			while(true) {
				try {
					List<? extends Task> status = statusQueue.take();
					client.setStatus(status);
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
