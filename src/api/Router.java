package api;

import java.rmi.Remote;

/**
 * The interface using which the data from the client is routed to the appropriate Server.
 */
public interface Router extends Remote{

	/** The Constant SERVICE_NAME. */
	public static final String SERVICE_NAME = "Router";
	
	/**
	 * Puts the hash and the data to the be stored.
	 *
	 * @param hash the hash
	 * @param data the data
	 */
	public void put(byte[] hash, byte[] data);
	
	/**
	 * Gets the data associated with the hash.
	 *
	 * @param hash the hash
	 */
	public void get(byte[] hash);

}
