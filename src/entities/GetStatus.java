/*
 * @author Gautham Narayanasamy
 */
package entities;

/**
 * The Class GetStatus.
 */
public class GetStatus extends Status {

	
	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1456978708990373366L;
	
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
