/*
 * @author Gautham Narayanasamy
 */
package system;

import java.rmi.RMISecurityManager;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.HashMap;
import java.util.Map;

import exceptions.ArchiveException;

import utils.Constants;

import api.Router;
import api.ServerToRouter;
import api.StorageServer;
import api.Task;

/**
 * The Class RouterImpl contains the implementations of the logic to route the archiving requests to the appropriate Storage servers based on the hash.
 */
public class RouterImpl implements Router, ServerToRouter{

	/** The server map. */
	private Map<Integer, StorageServer> serverMap = new HashMap<>();
	
	/** The number of Storage Servers. */
	private int numServers = 0;
	
	/* (non-Javadoc)
	 * @see api.Router#put(byte[], byte[])
	 */
	@Override
	public void put(byte[] hash, byte[] data) {
		Task task = new Task();
		task.setData(data);
		task.setHash(hash);
		task.setType(Constants.TASK_TYPE_PUT);
		
		routeRequest(task);
	}

	/* (non-Javadoc)
	 * @see api.Router#get(byte[])
	 */
	@Override
	public void get(byte[] hash) {
		Task task = new Task();
		task.setHash(hash);
		task.setType(Constants.TASK_TYPE_GET);
		
		routeRequest(task);
	}	

	/* (non-Javadoc)
	 * @see api.ServerToRouter#register(api.StorageServer)
	 */
	@Override
	public synchronized void register(StorageServer server) {
		serverMap.put(numServers++, server);
	}
	
	/**
	 * Based on the hash value, route the request to the appropriate StorageServer.
	 *
	 * @param task the task
	 */
	private void routeRequest(Task task) {
		byte[] hash = task.getHash();
		
		int value = 0;
		int numBytes = Constants.HASH_BIT_NUMBER;
		
		if (numBytes > 4) {
			throw new ArchiveException("The number of bytes exceeds 4!");
		}
		
		// Getting the integer corresponding to the first 'numBytes' of the hash.
		for(int i = 0; i < numBytes; i++) {
			value = (value << 8) | hash[i];
		}
		
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
