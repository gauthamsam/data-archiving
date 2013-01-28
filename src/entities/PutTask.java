/*
 * @author Gautham Narayanasamy
 */
package entities;

/**
 * The Class PutTask.
 */
public class PutTask extends Task{
	
	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1585193027053594109L;
	
	/** The data. */
	private byte[] data;

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
