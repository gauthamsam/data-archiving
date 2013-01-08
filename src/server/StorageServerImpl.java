package server;

import java.rmi.Naming;

import api.Router;
import api.ServerToRouter;
import api.StorageServer;
import api.Task;

public class StorageServerImpl implements StorageServer{

	@Override
	public void assignTask(Task task) {
		// if put task
		Accumulator.addToPutQueue(task);
		// if get task
		Accumulator.addToGetQueue(task);
	}
	
	public static void main(String[] args) throws Exception {
		String routerDomainName = args[0];
		
		String routerURL = "//" + routerDomainName + "/" + Router.SERVICE_NAME;
		ServerToRouter router = (ServerToRouter) Naming.lookup(routerURL);
		
		StorageServer server = new StorageServerImpl(); // can throw RemoteException
		router.register(server);
		System.out.println("Server is ready.");
	}


}
