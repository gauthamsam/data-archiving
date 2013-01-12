/*
 * @author Gautham Narayanasamy
 */
package api;

import java.io.Serializable;

/**
 * The Class Task.
 */
public class Task implements Serializable{
	
	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1959265322617740977L;

	/** The hash. */
	private byte[] hash;
	
	/** The data. */
	private byte[] data;

	private int type;
	
	
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

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

}
