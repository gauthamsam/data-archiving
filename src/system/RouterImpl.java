package system;

import java.rmi.RMISecurityManager;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.HashMap;
import java.util.Map;

import api.Router;
import api.ServerToRouter;
import api.StorageServer;
import api.Task;

public class RouterImpl implements Router, ServerToRouter{

	private static Map<Integer, StorageServer> serverMap = new HashMap<>();
	private static int numServers = 0;
	
	@Override
	public void put(byte[] hash, byte[] data) {
		Task task = new Task();
		task.setData(data);
		task.setHash(hash);
		
		routeRequest(task);
	}

	@Override
	public void get(byte[] hash) {
		Task task = new Task();
		task.setHash(hash);
		
		routeRequest(task);
	}
	

	@Override
	public synchronized void register(StorageServer server) {
		serverMap.put(numServers++, server);
	}
	
	/**
	 * Based on the hash value, route the request to the appropriate StorageServer.
	 */
	private void routeRequest(Task task){
		serverMap.get(1).assignTask(task);
	}
	
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
