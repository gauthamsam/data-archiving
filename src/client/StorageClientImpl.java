/*
 * @author Gautham Narayanasamy
 */
package client;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RMISecurityManager;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import utils.Constants;
import api.Router;
import api.RouterToClient;
import api.StorageClient;
import entities.GetStatus;
import entities.PutStatus;
import entities.Status;
import entities.Task;

/**
 * The Class StorageClientImpl.
 */
public class StorageClientImpl extends UnicastRemoteObject implements StorageClient, RouterToClient {
		
	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 9066640835730444941L;

	/** The router. */
	private Router router;
	
	/** The request map. */
	private Map<String, Task> requestMap;
	
	/** The status queue. */
	private BlockingQueue<Status> statusQueue;
	
	/** The client. */
	private static StorageClientImpl client;
	
	/**
	 * Gets the single instance of StorageClientImpl.
	 *
	 * @return single instance of StorageClientImpl
	 * @throws RemoteException the remote exception
	 */
	public static StorageClientImpl getInstance() throws RemoteException{
		if (client == null) {
			client = new StorageClientImpl();
		}
		return client;
	}
	
	/**
	 * Instantiates a new storage client impl.
	 *
	 * @throws RemoteException the remote exception
	 */
	private StorageClientImpl() throws RemoteException{
		super();
		requestMap = new HashMap<>();
		statusQueue = new LinkedBlockingQueue<>();
		new Receiver().start();
	}
	
	/* (non-Javadoc)
	 * @see api.RouterToClient#setStatus(entities.Status)
	 */
	@Override
	public void setStatus(List<Status> status) throws RemoteException{
		statusQueue.addAll(status);
	}	
	
	/**
	 * The main method.
	 *
	 * @param args the arguments
	 * @throws InterruptedException the interrupted exception
	 * @throws MalformedURLException the malformed url exception
	 * @throws RemoteException the remote exception
	 * @throws NotBoundException the not bound exception
	 */
	public static void main(String[] args) throws InterruptedException, MalformedURLException, RemoteException, NotBoundException {
		// Construct & set a security manager to allow downloading of classes from a remote codebase
		System.setSecurityManager(new RMISecurityManager());
		String serverDomainName = args[0];
		String routerURL = "//" + serverDomainName + "/" + Router.SERVICE_NAME;
		
		// The RMI client requests a reference to a named remote object. The reference (the remote object's stub instance) is what the client will use to make remote method calls to the remote object.
		StorageClientImpl client = StorageClientImpl.getInstance();
		client.router = (Router) Naming.lookup(routerURL);
		client.router.setClient(client);

		String data = "sample text";
		MessageDigest md = null;
		byte[] hash = null;
		List<byte[]> hashList = new ArrayList<>();
		
		for (int i = 0; i < 100; i++) {
			data = "sample text " + i;
		    try {
		        md = MessageDigest.getInstance("SHA-1");
		        hash = md.digest(data.getBytes());
		    }
		    catch(NoSuchAlgorithmException e) {
		        e.printStackTrace();
		    }
			client.put(hash, data.getBytes());
			hashList.add(hash);
		}		
		
		
//		Thread.sleep(3000);
//		for(byte[] hash1 : hashList) {
//			router.get(hash1);
//		}
	}
	
	/* (non-Javadoc)
	 * @see api.StorageClient#put(byte[], byte[])
	 */
	@Override
	public void put(byte[] hash, byte[] data) {
		String strHash = new String(hash);
		Task task = new Task();
		task.setData(data);
		task.setHash(strHash);
		task.setType(Constants.TASK_TYPE_PUT);
		task.setStartTime(System.currentTimeMillis());
		
		routeRequest(task);
	}
	
	/* (non-Javadoc)
	 * @see api.StorageClient#get(byte[])
	 */
	@Override
	public void get(byte[] hash) {
		String strHash = new String(hash);
		Task task = new Task();
		task.setHash(strHash);
		task.setType(Constants.TASK_TYPE_GET);
		task.setStartTime(System.currentTimeMillis());
		
		routeRequest(task);
	}
	
	/**
	 * Route request.
	 *
	 * @param task the task
	 */
	private void routeRequest(Task task) {
		requestMap.put(task.getHash(), task);
		try {
			this.router.routeRequest(task);
		} catch (RemoteException e) {			
			e.printStackTrace();
		}
	}
	
	
	/**
	 * The receiver thread will process the return status of the submitted tasks.
	 */
	class Receiver extends Thread {
		
		/** The put_ avg time taken. */
		private List<Long> put_AvgTimeTaken = new ArrayList<>();
		
		/* (non-Javadoc)
		 * @see java.lang.Thread#run()
		 */
		public void run() {
			while (true) {
				try {
					Status status = statusQueue.take();	
					if (status instanceof GetStatus) {
						processGetStatus((GetStatus) status);
					}
					else {
						processPutStatus((PutStatus) status);
					}
					
				} catch (InterruptedException e) {					 
					e.printStackTrace();
				}
				catch(Exception e){
					e.printStackTrace();
				}
			}
		}
		
		/**
		 * Process get status.
		 *
		 * @param status the status
		 */
		private void processGetStatus(GetStatus status) {
			long currentTime = System.currentTimeMillis();
			System.out.println("Get: time taken for " + status.getHash() + " => " + (currentTime - requestMap.get(status.getHash()).getStartTime()));
		}
		
		/**
		 * Process put status.
		 *
		 * @param status the status
		 */
		private void processPutStatus(PutStatus status) {
			long currentTime = System.currentTimeMillis();			
			put_AvgTimeTaken.add((currentTime - requestMap.get(status.getHash()).getStartTime()));
			Collections.sort(put_AvgTimeTaken);
			//System.out.println("Put: time taken for " + status.getHash() + " => " + (currentTime - requestMap.get(status.getHash()).getStartTime()));
			System.out.println("Avg time: " + put_AvgTimeTaken);
			System.out.println("Size: " + put_AvgTimeTaken.size());
		}
	}

}
