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

	private long startTime;
	
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

	public long getStartTime() {
		return startTime;
	}

	public void setStartTime(long startTime) {
		this.startTime = startTime;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj == null || obj.getClass() != getClass())
            return false;
        if (obj == this)
            return true;
        return this.hash == ((Task)obj).getHash();
	}
	
	@Override
	public int hashCode() {
		return hash.hashCode();
	}
	

}
