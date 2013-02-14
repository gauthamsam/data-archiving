/*
 * @author Gautham Narayanasamy
 */
package client;

import java.io.DataInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.security.MessageDigest;

import utils.Constants;
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
				
				long startTime = System.currentTimeMillis();
				client.setStartTime(startTime);
				
				InputStream in = connection.getInputStream();
				DataInputStream dis = new DataInputStream(in);
				
				int operation = 0;
				byte[] hash = new byte[20];
				int dataLength = 0;
				int offset = 0;
				int bytesToRead = 0;
				byte[] buffer = null;
				byte[] data = null;
				long totalDataReceived = 0;
				long numRequests = 0;
				
				while(true) {
					
	            	// re-initialize all the values.
	            	offset = 0;
	            	operation = 0;
	            	dataLength = 0;
	            	
	            	try {
	            		bytesToRead = dis.readInt();
	            		bytesToRead = littleToBigEndian(bytesToRead);
	            		// System.out.println("Total bytes " + bytesToRead);
	            		buffer = new byte[bytesToRead];	            	
	            		dis.readFully(buffer);
	            	}
	            	catch(EOFException e) {
	            		// break from the inner while loop. But the outer while loop will continue waiting for a connection.
	            		System.out.println("End of stream!");	            		
	            		break;
	            	}
	            	
	            	
	            	// Only 2 bytes are used for 'operation'.
	            	for(int i = 0; i < 2; i++) {
	            		operation = (operation << 8) | buffer[offset++];
	            	}
	            	operation <<= 16;
	            	
	            	operation = littleToBigEndian(operation);
	            	
	            	// System.out.println("Operation " + operation);
	            	
	            	for(int i = 0; i < 20; i++) {
	            		// little endian to big endian
	            		hash[i] = buffer[offset++];
	            	}
	            	
	            	// System.out.println("Hash " + new String(hash));
	                dataLength = bytesToRead - offset;
		        // System.out.println("dataLength " + dataLength);
		        totalDataReceived += dataLength;
	            	data = new byte[dataLength];
	            	for(int i = 0; i < data.length; i++) {
	            		data[i] = buffer[offset++];
	            	} 	
	                if(isZero(data)){
 				//System.out.println("Zero block");
				//continue;
                        }	
                        if (operation == Constants.OPERATION_PUT) { 
		            	
	            		/*
	            		byte[] bytes = ByteBuffer.allocate(64).putLong(numRequests).array();
	            		for(int i = 0; i < 20; i++) {
	            			hash[i] = bytes[i];
	            		} */
	            	MessageDigest md = null;
	    				md = MessageDigest.getInstance("SHA-1");
	    		        hash = md.digest(("a" + numRequests).getBytes());	
	            	        client.put(hash, data);	           
	            		
	            		
	            	}
	            	
	            	else if (operation == Constants.OPERATION_GET) {
	            		client.get(hash);
	            	}
	                data = null;
	            	
	            	buffer = null;
	            	numRequests ++;
	            	/*if(totalDataReceived >= (1.0 * 1024 * 1024 * 1024 * 10)) {	            		
	            		System.out.println("10 GB received!");
	            		break;
	            	}*/
			if(numRequests >= 2000000) {
				System.out.println("2000000 requests.");
				break;
			}
	            	//Thread.sleep(1);
	            }
				
				System.out.println("Total data received " + totalDataReceived / (1.0 * 1024 * 1024) + " MB ");
				
				// Get the stats.
        		client.calculateStats(numRequests);
        		
			}
            
        }
        catch(Exception e) {
        	e.printStackTrace();
        }
        
        finally {
        	try {
        		if(connection != null) {
        			connection.close();
        		}
			} catch (IOException e) {
				e.printStackTrace();
			}
        }

	}

        
	/**
 * 	 * Checks if the given byte array contains only 0s.
 * 	 	 *
 * 	 	 	 * @param data the data
 * 	 	 	 	 * @return true, if is zero
 * 	 	 	 	 	 */
	private static boolean isZero(byte[] data) {
		int mask = (1 << 8) - 1;
		for(int i = 0; i < data.length; i++) {
			if((data[i] & mask) != 0) {
				return false;
			}
		}
		return true;
	}
	
	/**
	 * Converts the given integer from little endian to big endian format.
	 *
	 * @param i the integer
	 * @return the converted integer.
	 */
	private int littleToBigEndian(int i) {
		ByteBuffer bbuf = ByteBuffer.allocate(4);  
        bbuf.order(ByteOrder.LITTLE_ENDIAN);  
        bbuf.putInt(i);
        bbuf.order(ByteOrder.BIG_ENDIAN);  
        return bbuf.getInt(0);
	}
	
	
}
