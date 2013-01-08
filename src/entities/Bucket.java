package entities;

import java.util.HashMap;
import java.util.Map;

public class Bucket {
	private int id;
	
	private Map<String, Long> index;
	
	public Bucket(int id){
		this.id = id;
		this.index  = new HashMap<>();
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public Map<String, Long> getIndex() {
		return index;
	}

	public void setIndex(Map<String, Long> index) {
		this.index = index;
	}
	

}
