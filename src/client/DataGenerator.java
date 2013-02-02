package client;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class DataGenerator {

	public static void main(String[] args) throws InterruptedException {
		
		Socket s = null;
		try {
			s = new Socket("localhost", 12346);
			
			OutputStream os = s.getOutputStream();
			
			for(int n = 0; n < 10; n++) {
				byte[] data = ("sample text" + n).getBytes();
				System.out.println("data length " + data.length);
				MessageDigest md = null;
				byte[] hash = null;
	
		        md = MessageDigest.getInstance("SHA-1");
		        hash = md.digest(data);	        
		        System.out.println("hash " + new String(hash));	        
		        
		        byte[] operation = ByteBuffer.allocate(4).putInt(0).array();
		        
		        int length = operation.length + hash.length + data.length;
		        byte[] dataLength = ByteBuffer.allocate(4).putInt(length).array();
		        
		        os.write(dataLength);
		        
		        System.out.println("length of operation " + operation.length);
		        os.write(operation);
		        
		        os.write(hash);
		        
		        System.out.println("length of hash " + hash.length);
		        
		        os.write(data);
		        
		        System.out.println("length of data " + data.length);
		        
		        System.out.println("length " + length);		        
			}
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		catch(NoSuchAlgorithmException e) {
	        e.printStackTrace();
	    }
		finally {
			try {
				s.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
	}	
	
}
