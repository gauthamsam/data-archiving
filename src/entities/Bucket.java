/*
 * @author Gautham Narayanasamy
 */
package entities;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * The Class Bucket.
 */
public class Bucket implements Serializable{
	
	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = -3271477285326935085L;

	/** The id. */
	private int id;
	
	/** The index. */
	private Map<String, DataEntry> index;
	
	/**
	 * Instantiates a new bucket.
	 *
	 * @param id the id
	 */
	public Bucket(int id){
		this.id = id;
		this.index  = new HashMap<String, DataEntry>();
	}

	/**
	 * Gets the id.
	 *
	 * @return the id
	 */
	public int getId() {
		return id;
	}

	/**
	 * Sets the id.
	 *
	 * @param id the new id
	 */
	public void setId(int id) {
		this.id = id;
	}

	/**
	 * Gets the index.
	 *
	 * @return the index
	 */
	public Map<String, DataEntry> getIndex() {
		return index;
	}

	/**
	 * Sets the index.
	 *
	 * @param index the index
	 */
	public void setIndex(Map<String, DataEntry> index) {
		this.index = index;
	}	
	
	@Override
	public int hashCode() {
		return id;
	}
	
	@Override
	public boolean equals(Object obj) { 
		if (obj == null || obj.getClass() != getClass())
            return false;
        if (obj == this)
            return true;
        return this.id == ((Bucket)obj).getId(); 
	}
	
	@Override
	public String toString() {
		return String.valueOf(id);
	}

}
