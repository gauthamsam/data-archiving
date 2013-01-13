/*
 * @author Gautham Narayanasamy
 */
package system;

import java.rmi.RMISecurityManager;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.annotation.adapters.HexBinaryAdapter;

import exceptions.ArchiveException;

import utils.Constants;

import api.Router;
import api.ServerToRouter;
import api.StorageServer;
import api.Task;

/**
 * The Class RouterImpl contains the implementations of the logic to route the archiving requests to the appropriate Storage servers based on the hash.
 */
public class RouterImpl extends UnicastRemoteObject implements Router, ServerToRouter{


	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = -3548712810447954655L;

	/** The server map. */
	private Map<Integer, StorageServer> serverMap = new HashMap<>();
	
	/** The number of Storage Servers. */
	private int numServers = 0;
	
	
	protected RouterImpl() throws RemoteException {
		super();		
	}
	
	/* (non-Javadoc)
	 * @see api.Router#put(byte[], byte[])
	 */
	@Override
	public void put(byte[] hash, byte[] data) throws RemoteException {
		Task task = new Task();
		task.setData(data);
		task.setHash(new String(hash));
		task.setType(Constants.TASK_TYPE_PUT);
		
		routeRequest(task);
	}

	/* (non-Javadoc)
	 * @see api.Router#get(byte[])
	 */
	@Override
	public void get(byte[] hash) throws RemoteException {
		Task task = new Task();
		task.setHash(new String(hash));
		task.setType(Constants.TASK_TYPE_GET);
		
		routeRequest(task);
	}	

	/* (non-Javadoc)
	 * @see api.ServerToRouter#register(api.StorageServer)
	 */
	@Override
	public synchronized void register(StorageServer server) throws RemoteException {
		System.out.println("Server " + numServers + " registered!");
		serverMap.put(numServers++, server);
		
	}
	
	/**
	 * Based on the hash value, route the request to the appropriate StorageServer.
	 *
	 * @param task the task
	 */
	private void routeRequest(Task task) throws RemoteException {
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
		System.out.println("Routing to Server " + value % numServers);
		serverMap.get(value % numServers).assignTask(value, task);
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
		Router router = new RouterImpl();
		// construct an rmiregistry within this JVM using the default port
		Registry registry = LocateRegistry.createRegistry(1099);
		// bind router in rmiregistry.
		registry.rebind(Router.SERVICE_NAME, router);
		
		System.out.println("Router is ready.");
	}

}
