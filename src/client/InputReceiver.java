/*
 * @author Gautham Narayanasamy
 */
package client;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

import api.StorageClient;

/**
 * Listens continuously for data from the data generator.
 */
public class InputReceiver extends Thread{
	
	/** The service. */
	private ServerSocket service;
	
	/** The PORT. */
	private final int PORT = 12346;
	
	/**
	 * Instantiates a new input receiver.
	 */
	public InputReceiver() {
		try {
			service = new ServerSocket(PORT);			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Thread#run()
	 */
	public void run() {
		Socket connection = null;
		
		try {
			StorageClient client = StorageClientImpl.getInstance();
            
			while(true) {
				connection = service.accept();
	            BufferedInputStream isr = new BufferedInputStream(connection.getInputStream());
	            
	            byte[] b = new byte[1024 * 1024];
	            
	            int operation = 0;
	            byte[] hash = new byte[20];
	            
	            int offset = 0;
	            int totalBytes = 0;
	            
	            while((totalBytes = isr.read(b)) != -1) {
	            	offset = 0;
	            	System.out.println("bytes read " + new String(b) + " length: " + totalBytes);	            	
	            	
	            	operation |= b[offset++];
	            	System.out.println("Operation " + operation);
	            	
	            	for(int i = 0; i < 20; i++) {
	            		hash[i] = b[offset++];
	            	}
	            	
	            	System.out.println("Hash " + new String(hash));
	            	if (operation == 1) { // get operation
	            		client.get(hash);
	            	}
	            	
	            	else if (operation == 2) { // put operation            		
	            		byte[] data = new byte[totalBytes - offset];
	            		for(int i = 0; i < data.length; i++) {
	            			data[i] = b[offset++];
	            		}
	            		// client.put(hash, data);	            		            		
	            	}
	            	//Thread.sleep(1000);
	            }
			}
            
        }
        catch(Exception e) {
        	e.printStackTrace();
        }
        
        finally {
        	try {
				connection.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
        }

	}
	
}
