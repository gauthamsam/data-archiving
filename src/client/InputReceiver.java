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
				long time1 = System.currentTimeMillis();
				long time2 = 0;
				long time3 = 0;
				long time4 = 0;
				long putTime = 0;
				
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
	            	
	            	numRequests ++;
	            	
	            	// Only 2 bytes are used for 'operation'.
	            	for(int i = 0; i < 2; i++) {
	            		operation = (operation << 8) | buffer[offset++];
	            	}
	            	operation <<= 16;
	            	
	            	operation = littleToBigEndian(operation);
	            	
	            	// System.out.println("Operation " + operation);
	            	
	            	for(int i = 0; i < 20; i++) {
	            		// little endian to big endian
	            		hash[20 - i - 1] = buffer[offset++];
	            	}
	            	
	            	// System.out.println("Hash " + new String(hash));
	            	
	            	if (operation == Constants.OPERATION_PUT) { 
		            	dataLength = bytesToRead - offset;
		            	// System.out.println("dataLength " + dataLength);
		            	totalDataReceived += dataLength;
	            		data = new byte[dataLength];
	            		for(int i = 0; i < data.length; i++) {
	            			data[i] = buffer[offset++];
	            		}
	            		/*byte[] bytes = ByteBuffer.allocate(8).putLong(numRequests).array();
	            		for(int i = 0; i < 2; i++) {
	            			hash[i] = bytes[i];
	            		}*/
	            		MessageDigest md = null;
	    				md = MessageDigest.getInstance("SHA-1");
	    		        hash = md.digest(("a" + numRequests).getBytes());
	    		        	        
	            		// time2 = System.currentTimeMillis();
	            		client.put(hash, data);	           
	            		// time3 = System.currentTimeMillis();
	            		putTime += (time3 - time2);
	            	}
	            	
	            	else if (operation == Constants.OPERATION_GET) { 
	            		client.get(hash);
	            	}
	            	
	            	if(totalDataReceived >= (1.0 * 1024 * 1024 * 1024)) {	            		
	            		System.out.println("1024 MB received!");
	            		break;
	            	}
	            	//Thread.sleep(1);
	            }
				
				time4 = System.currentTimeMillis(); 
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
