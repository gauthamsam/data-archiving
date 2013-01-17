/*
 * @author Gautham Narayanasamy
 */
package entities;

import java.io.Serializable;

/**
 * The Class Status.
 */
public abstract class Status implements Serializable {
	
	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = -6957009940494260830L;
	
	/** The success. */
	private boolean success;

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

}
