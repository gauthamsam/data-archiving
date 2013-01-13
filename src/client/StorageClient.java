package client;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RMISecurityManager;
import java.rmi.RemoteException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import api.Router;

public class StorageClient {
	
	public static void main(String[] args) throws MalformedURLException, RemoteException, NotBoundException {
		// Construct & set a security manager to allow downloading of classes from a remote codebase
		System.setSecurityManager(new RMISecurityManager());
		String serverDomainName = args[0];
		String routerURL = "//" + serverDomainName + "/" + Router.SERVICE_NAME;
		// The RMI client requests a reference to a named remote object. The reference (the remote object's stub instance) is what the client will use to make remote method calls to the remote object.
		Router router = (Router) Naming.lookup(routerURL);
		
		String data = "sample text";		
		MessageDigest md = null;
		byte[] hash = null;
		for (int i = 0; i < 2; i++){
			data = "sample text";
		    try {
		        md = MessageDigest.getInstance("SHA-1");
		        hash = md.digest(data.getBytes());
		    }
		    catch(NoSuchAlgorithmException e) {
		        e.printStackTrace();
		    }
			router.put(hash, data.getBytes());
		}
	    
				
	}

}
