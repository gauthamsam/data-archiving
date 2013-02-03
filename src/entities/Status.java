/*
 * @author Gautham Narayanasamy
 */
package entities;

import java.io.Serializable;

/**
 * Represents the status of the put/get requests.
 */
public abstract class Status implements Serializable {
	
	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = -6957009940494260830L;
	
	/** The hash. */
	private String hash;
		
	/** The success. */
	private boolean success;
	
	private long startTime;
	
	private long endTime;

	
	/**
	 * Gets the hash.
	 *
	 * @return the hash
	 */
	public String getHash() {
		return hash;
	}

	/**
	 * Sets the hash.
	 *
	 * @param hash the new hash
	 */
	public void setHash(String hash) {
		this.hash = hash;
	}

	/**
	 * Checks if is success.
	 *
	 * @return true, if is success
	 */
	public boolean isSuccess() {
		return success;
	}

	/**
	 * Sets the success.
	 *
	 * @param success the new success
	 */
	public void setSuccess(boolean success) {
		this.success = success;
	}

	public long getStartTime() {
		return startTime;
	}

	public void setStartTime(long startTime) {
		this.startTime = startTime;
	}

	public long getEndTime() {
		return endTime;
	}

	public void setEndTime(long endTime) {
		this.endTime = endTime;
	}

}
