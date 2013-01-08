package server;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import org.apache.log4j.Logger;

import utils.Constants;
import api.Task;
import entities.Bucket;
import exceptions.ArchiveException;


/**
 * The StorageManager deals with all the disk operations. 
 */
public class StorageManager {
	private static final Logger logger = Logger.getLogger(StorageManager.class);


	public static void readData(int bucketId, Queue<Task> tasks) {
		if(bucketId < 0 || tasks == null || tasks.size() == 0){
			throw new IllegalArgumentException("Invalid bucketId or task queue.");
		}
		
		Bucket bucket = readBucket(bucketId);
		Map<String, Long> index = bucket.getIndex();
		/* Get the offset of data associated with each task in the queue.
		 * Sort the offsets and do a lookup in the disk.
		 */
		List<Long> offsets = new ArrayList<>();
		for(Task task : tasks){
			offsets.add(index.get(task.getHash()));
		}
		Collections.sort(offsets);
		readDataFromDisk(bucketId, offsets);
	}
	
	public static void writeData(int bucketId, Queue<Task> tasks){
		if(bucketId < 0 || tasks == null || tasks.size() == 0){
			throw new IllegalArgumentException("Invalid bucketId or task queue.");
		}
		
		Bucket bucket = readBucket(bucketId);
		Map<String, Long> index = bucket.getIndex();
		
		List<byte[]> dataToWrite = new ArrayList<>();
		for(Task task : tasks){
			if(! index.containsKey(task.getHash())){
				dataToWrite.add(task.getData());				
			}
		}
		
		writeDataToDisk(bucketId, dataToWrite);
		
	}
	
	private static Bucket readBucket(int bucketId) {		
		String bucketPath = Constants.FILE_PATH + File.separator + bucketId;
		ObjectInputStream inputStream = null;
		Bucket bucket = null;
		try {
			inputStream = new ObjectInputStream(new FileInputStream(bucketPath));		
			bucket = (Bucket) inputStream.readObject();			
			
		} catch (ClassNotFoundException | IOException e) {
			logger.error("Bucket not found. Exception: " + e);
			throw new ArchiveException(e);
		}
		finally{
			try {
				inputStream.close();
			} catch (IOException e) {
				logger.error(e);
				throw new ArchiveException(e);
			}
		}
		return bucket;
	}

	public static void writeBucket(int bucketId, Queue<Task> tasks) {
		if(bucketId < 0 || tasks == null || tasks.size() == 0){
			throw new IllegalArgumentException("Invalid bucketId or task queue.");
		}
		
		String bucketPath = Constants.FILE_PATH + File.separator + bucketId;
		ObjectOutputStream outputStream = null;
		try{
			outputStream = new ObjectOutputStream(new FileOutputStream(bucketPath));
			
			outputStream.writeObject(null);
		}
		catch(IOException e){
			logger.debug("");
		}
		finally{
			try {
				outputStream.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

	}

	private static void readDataFromDisk(int bucketId, List<Long> offsets){
		
	}
	
	private static void writeDataToDisk(int bucketId, List<byte[]> data){
		
	}		
	

}
