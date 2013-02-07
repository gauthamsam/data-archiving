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
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import api.Router;
import api.RouterToClient;
import api.StorageClient;
import entities.GetTask;
import entities.PutTask;
import entities.Task;

/**
 * The Class StorageClientImpl.
 */
public class StorageClientImpl extends UnicastRemoteObject implements StorageClient, RouterToClient {
		
	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 9066640835730444941L;

	/** The router. */
	private Router router;
	
	/** The status queue. */
	private BlockingQueue<List<? extends Task>> statusQueue;
	
	/** The client. */
	private static StorageClientImpl client;
		
	/** The put time. */
	private List<Long> putTime = Collections.synchronizedList(new ArrayList<Long>());
	
	/** The get time. */
	private List<Long> getTime = Collections.synchronizedList(new ArrayList<Long>());
	
	private long startTime;
	
	/**
	 * Gets the single instance of StorageClientImpl.
	 *
	 * @return single instance of StorageClientImpl
	 * @throws RemoteException the remote exception
	 */
	public static synchronized StorageClientImpl getInstance() throws RemoteException {
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
	private StorageClientImpl() throws RemoteException {
		super();		
		statusQueue = new LinkedBlockingQueue<List<? extends Task>>();
		new InputReceiver().start();
		new OutputReceiver().start();
	}
	
	/* (non-Javadoc)
	 * @see api.RouterToClient#setStatus(entities.Status)
	 */
	@Override
	public void setStatus(List<? extends Task> status) throws RemoteException{
		statusQueue.add(status);
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
		// Set the client in the router for doing a callback.
		client.router.setClient(client);		
	}
	
	/* (non-Javadoc)
	 * @see api.StorageClient#put(byte[], byte[])
	 */
	@Override
	public void put(byte[] hash, byte[] data) {
		String strHash = new String(hash);
		PutTask task = new PutTask();
		task.setData(data);
		task.setHash(strHash);		
		task.setStartTime(System.currentTimeMillis());
		
		routeRequest(task);
	}
	
	/* (non-Javadoc)
	 * @see api.StorageClient#get(byte[])
	 */
	@Override
	public void get(byte[] hash) {
		String strHash = new String(hash);
		GetTask task = new GetTask();
		task.setHash(strHash);		
		task.setStartTime(System.currentTimeMillis());
		
		routeRequest(task);
	}
	
	/**
	 * Route request.
	 *
	 * @param task the task
	 */
	private void routeRequest(Task task) {		
		try {
			this.router.routeRequest(task);
		} catch (RemoteException e) {			
			e.printStackTrace();
		}
	}
	
	
	/**
	 * The receiver thread will process the return status of the submitted tasks.
	 */
	class OutputReceiver extends Thread {		
		
		/* (non-Javadoc)
		 * @see java.lang.Thread#run()
		 */
		public void run() {
			while (true) {
				try {
					List<? extends Task> statusList = statusQueue.take();
					long endTime = System.currentTimeMillis();
					for(Task status : statusList) {
					if (status instanceof GetTask) {
						processGetStatus((GetTask) status, endTime);
					}
					else {
						processPutStatus((PutTask) status, endTime);
					}
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
		private void processGetStatus(GetTask status, long endTime) {
						
			// getTime.add((currentTime - requestMap.get(status.getHash()).getStartTime()));
			getTime.add(endTime - status.getStartTime());
			//Collections.sort(get_AvgTimeTaken);
			//System.out.println("Put: time taken for " + status.getHash() + " => " + (currentTime - requestMap.get(status.getHash()).getStartTime()));
			//System.out.println("Max time: " + get_AvgTimeTaken.get(get_AvgTimeTaken.size() - 1));
			//System.out.println("Size: " + get_AvgTimeTaken.size());
		}
		
		/**
		 * Process put status.
		 *
		 * @param status the status
		 */
		private void processPutStatus(PutTask status, long endTime) {
						
			// putTime.add((currentTime - requestMap.get(status.getHash()).getStartTime()));
			// System.out.println("Data length " + status.getData().length);
			putTime.add(endTime - status.getStartTime());
			
			// Collections.sort(putTime);
			// System.out.println("Put: time taken for " + status.getHash() + " => " + (currentTime - requestMap.get(status.getHash()).getStartTime()));
			//System.out.println("Max time: " + putTime.get(putTime.size() - 1));
			// System.out.println("Size: " + putTime.size());
		}
		
	}


	/* (non-Javadoc)
	 * @see api.StorageClient#calculateStats()
	 */
	@Override
	public void calculateStats(long numRequests) {
		
		while(numRequests != (getTime.size() + putTime.size())) {			
		}
		
		long completionTime = (System.currentTimeMillis() - this.startTime);
		
		System.out.println("Time between first and last task: " + completionTime + " ms");
		
		if(getTime.size() > 0) {
			calculateGetStats();
		}
		
		if(putTime.size() > 0) {
			calculatePutStats();
		}		
	}
	
	/**
	 * Calculate get stats.
	 */
	private void calculateGetStats() {
		long sum = 0;
		for(long i : getTime) {
			sum += i;
		}
		System.out.println("Total get responses " + getTime.size());
		System.out.println("Average get time: " + sum/getTime.size());		
	}
	
	/**
	 * Calculate put stats.
	 */
	private void calculatePutStats() {
		double sum = 0;
		for(double i : putTime) {
			sum += i;
		}
		System.out.println("Total put responses " + putTime.size());		
		System.out.println("Average put time: " + sum/putTime.size());
		
	}

	@Override
	public void setStartTime(long startTime) {
		this.startTime = startTime;
	}

}
