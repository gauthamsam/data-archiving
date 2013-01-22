package client;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class DataGenerator {

	public static void main(String[] args) throws InterruptedException {
		try {
			Socket s = new Socket("localhost", 12346);
			OutputStream os = new BufferedOutputStream(s.getOutputStream());
			
			for(int n = 0; n < 10; n++) {
				byte[] data = ("sample text" + n).getBytes();
				System.out.println("data length " + data.length);
				MessageDigest md = null;
				byte[] hash = null;
	
		        byte[] dataToSend = new byte[21 + data.length];
		        
		        md = MessageDigest.getInstance("SHA-1");
		        hash = md.digest(data);	        
		        System.out.println("hash " + new String(hash));	        
		        dataToSend[0] = new Integer(2).byteValue();
		        
		        int index = 1;
		        
		        for(int i = 0; i < hash.length; i++) {
		        	dataToSend[index++] = hash[i];
		        }
		        
		        for(int i = 0; i < data.length; i++) {
		        	dataToSend[index++] = data[i];
		        }
		        
		        System.out.println("dataToSend " + new String(dataToSend));
		        System.out.println("length " + dataToSend.length);
		        os.write(dataToSend);
		        os.flush();
		        //Thread.sleep(1000);
			}
	        
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		catch(NoSuchAlgorithmException e) {
	        e.printStackTrace();
	    }
		
	}
}
