package api;


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
	
}
