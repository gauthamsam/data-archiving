/*
 * @author Gautham Narayanasamy
 */
package entities;

import java.io.Serializable;

/**
 * The Class Entry.
 */
public class DataEntry implements Comparable<DataEntry>, Serializable {
	
	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = -4453648200469096075L;

	/** The offset of the data in disk. */
	private long offset;

	/** The length of the data in bytes. */
	private int dataLength;
		
	/** The hash. This need not be serialized. It is used only during reading data from disk and returning them to the client. */
	private transient String hash;

	/**
	 * Gets the offset.
	 *
	 * @return the offset
	 */
	public long getOffset() {
		return offset;
	}

	/**
	 * Sets the offset.
	 *
	 * @param offset the new offset
	 */
	public void setOffset(long offset) {
		this.offset = offset;
	}

	/**
	 * Gets the data length.
	 *
	 * @return the data length
	 */
	public int getDataLength() {
		return dataLength;
	}

	/**
	 * Sets the data length.
	 *
	 * @param dataLength the new data length
	 */
	public void setDataLength(int dataLength) {
		this.dataLength = dataLength;
	}

	/* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(DataEntry o) {
		return (offset > o.offset) ? 1 : (offset == o.offset) ? 0 : -1;
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

}
