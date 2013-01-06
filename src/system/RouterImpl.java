package system;

import java.rmi.RMISecurityManager;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

import api.Router;
import api.ServerToRouter;
import api.StorageServer;

public class RouterImpl implements Router, ServerToRouter{

	@Override
	public void put(byte[] hash, byte[] data) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void get(byte[] hash) {
		// TODO Auto-generated method stub
		
	}
	

	@Override
	public void register(StorageServer server) {
		// TODO Auto-generated method stub
		
	}
	
	/**
	 * Based on the hash value, route the request to the appropriate StorageServer.
	 */
	private void routeRequest(){
		
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
