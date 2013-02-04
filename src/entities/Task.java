/*
 * @author Gautham Narayanasamy
 */
package entities;

import java.io.Serializable;

/**
 * The Class Task.
 */
public abstract class Task implements Serializable{
	
	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1959265322617740977L;

	/** The hash. */
	private String hash;	

	/** The start time. */
	private long startTime;
	
	/** The end time. */
	private long endTime;
	
	/** The status. */
	private boolean status;
	
	/**
	 * Gets the end time.
	 *
	 * @return the end time
	 */
	public long getEndTime() {
		return endTime;
	}

	/**
	 * Sets the end time.
	 *
	 * @param endTime the new end time
	 */
	public void setEndTime(long endTime) {
		this.endTime = endTime;
	}

	/**
	 * Gets the status.
	 *
	 * @return the status
	 */
	public boolean getStatus() {
		return status;
	}

	/**
	 * Sets the status.
	 *
	 * @param status the new status
	 */
	public void setStatus(boolean status) {
		this.status = status;
	}

	/**
	 * Gets the serialversionuid.
	 *
	 * @return the serialversionuid
	 */
	public static long getSerialversionuid() {
		return serialVersionUID;
	}

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
	 * Gets the start time.
	 *
	 * @return the start time
	 */
	public long getStartTime() {
		return startTime;
	}

	/**
	 * Sets the start time.
	 *
	 * @param startTime the new start time
	 */
	public void setStartTime(long startTime) {
		this.startTime = startTime;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (obj == null || obj.getClass() != getClass())
            return false;
        if (obj == this)
            return true;
        return this.hash == ((Task)obj).getHash();
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return hash.hashCode();
	}
	

}
