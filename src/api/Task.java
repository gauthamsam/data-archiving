/*
 * @author Gautham Narayanasamy
 */
package api;

/**
 * The Class Task.
 */
public class Task {
	
	/** The hash. */
	private byte[] hash;
	
	/** The data. */
	private byte[] data;

	/**
	 * Gets the hash.
	 *
	 * @return the hash
	 */
	public byte[] getHash() {
		return hash;
	}

	/**
	 * Sets the hash.
	 *
	 * @param hash the new hash
	 */
	public void setHash(byte[] hash) {
		this.hash = hash;
	}

	/**
	 * Gets the data.
	 *
	 * @return the data
	 */
	public byte[] getData() {
		return data;
	}

	/**
	 * Sets the data.
	 *
	 * @param data the new data
	 */
	public void setData(byte[] data) {
		this.data = data;
	}

}
