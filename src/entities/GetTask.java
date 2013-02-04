/*
 * @author Gautham Narayanasamy
 */
package entities;

/**
 * The Class GetTask.
 */
public class GetTask extends Task {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = -2608486235718679085L;
	
	/** The response data. */
	private byte[] responseData;

	/**
	 * Gets the response data.
	 *
	 * @return the response data
	 */
	public byte[] getResponseData() {
		return responseData;
	}

	/**
	 * Sets the response data.
	 *
	 * @param responseData the new response data
	 */
	public void setResponseData(byte[] responseData) {
		this.responseData = responseData;
	}
	
	

}
