/*
 * @author Gautham Narayanasamy
 */
package api;


/**
 * The Interface StorageClient.
 */
public interface StorageClient{
	/**
	 * Puts the hash and the data to be stored.
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
		
	public void setStartTime(long startTime);
	
	/**
	 * Calculate stats.
	 */
	public void calculateStats(long numRequests);
	
}
