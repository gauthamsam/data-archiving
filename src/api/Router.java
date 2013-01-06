package api;

import java.rmi.Remote;

public interface Router extends Remote{

	public static final String SERVICE_NAME = "Router";
	
	public void put(byte[] hash, byte[] data);
	
	public void get(byte[] hash);

}
