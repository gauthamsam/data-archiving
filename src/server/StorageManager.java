/*
 * @author Gautham Narayanasamy
 */
package server;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import org.apache.log4j.Logger;

import utils.Constants;
import api.Task;
import entities.Bucket;
import entities.DataEntry;
import exceptions.ArchiveException;


/**
 * The StorageManager deals with all the disk operations. 
 */
public class StorageManager {
	
	/** The Constant logger. */
	private static final Logger logger = Logger.getLogger(StorageManager.class);


	/**
	 * Read data.
	 *
	 * @param bucketId the bucket id
	 * @param tasks the tasks
	 */
	public static synchronized void readData(int bucketId, Queue<Task> tasks) {
		if(bucketId < 0 || tasks == null || tasks.size() == 0){
			throw new IllegalArgumentException("Invalid bucketId or task queue.");
		}
		
		Bucket bucket = readBucket(bucketId);
		Map<String, DataEntry> index = bucket.getIndex();
		/* Get the offset of data associated with each task in the queue.
		 * Sort the offsets and do a lookup in the disk.
		 */
		List<DataEntry> offsets = new ArrayList<>();
		for(Task task : tasks){
			offsets.add(index.get(task.getHash()));
		}
		Collections.sort(offsets);
		readDataFromDisk(bucketId, offsets);
	}
	
	/**
	 * Write data.
	 *
	 * @param bucketId the bucket id
	 * @param tasks the tasks
	 */
	public static synchronized void writeData(int bucketId, Queue<Task> tasks){
		if(bucketId < 0 || tasks == null || tasks.size() == 0){
			throw new IllegalArgumentException("Invalid bucketId or task queue.");
		}
		
		Bucket bucket = readBucket(bucketId);
		Map<String, DataEntry> index = bucket.getIndex();
		
		// Filter the data that is to be written to disk. i.e. Write only the blocks that are not already there.
		Map<String, byte[]> dataToWrite = new HashMap<>();
		for(Task task : tasks){
			if(! index.containsKey(task.getHash())){
				dataToWrite.put(task.getHash().toString(), task.getData());			
			}
		}
		
		// The bucket's index will be modified in place.
		writeDataToDisk(bucketId, dataToWrite, index);
		
		// Write (serialize) the modified bucket back to disk.
		writeBucket(bucket);
		
	}
	
	/**
	 * Read bucket.
	 *
	 * @param bucketId the bucket id
	 * @return bucket
	 */
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

	/**
	 * Write bucket.
	 *
	 * @param bucketId the bucket id
	 * @param tasks the tasks
	 */
	private static void writeBucket(Bucket bucket) {
		if(bucket == null){
			throw new IllegalArgumentException("Invalid bucketId or task queue.");
		}
		
		String bucketPath = Constants.FILE_PATH + File.separator + bucket.getId();
		ObjectOutputStream outputStream = null;
		try{
			outputStream = new ObjectOutputStream(new FileOutputStream(bucketPath));			
			outputStream.writeObject(bucket);
			outputStream.flush();
		}
		catch(IOException e){
			logger.error(e);
			throw new ArchiveException(e);
		}
		finally{
			try {
				outputStream.close();
			} catch (IOException e) {
				logger.error(e);
				throw new ArchiveException(e);
			}
		}
	}

	/**
	 * Read data from disk.
	 *
	 * @param bucketId the bucket id
	 * @param dataEntries the data entries
	 */
	private static void readDataFromDisk(int bucketId, List<DataEntry> dataEntries){
		List<byte[]> dataList = new ArrayList<>();
		
		String filePath = Constants.FILE_PATH + File.separator + bucketId;
		RandomAccessFile raf = null;
		
		try {
			raf = new RandomAccessFile(filePath, "r");
			
			for(DataEntry dataEntry : dataEntries) {
				byte[] data = new byte[dataEntry.getDataLength()];
				raf.seek(dataEntry.getOffset());
				raf.read(data, 0, data.length);
				dataList.add(data);
			}
		} catch (IOException e) {
			logger.error(e);
			throw new ArchiveException(e);
		}
		finally{
			try {
				raf.close();
			} catch (IOException e) {
				logger.error(e);
				throw new ArchiveException(e);
			}
		}
		
	}
	
	/**
	 * Write data to disk.
	 *
	 * @param bucketId the bucket id
	 * @param dataToWrite the data
	 * @param index the bucket index
	 */
	private static void writeDataToDisk(int bucketId, Map<String, byte[]> dataToWrite, Map<String, DataEntry> index) {
		OutputStream os = null;
		
		String filePath = Constants.FILE_PATH + File.separator + bucketId;
		try {
			File f = new File(filePath);
			long offset = f.length();
			if (offset > 0){
				offset ++;
			}
			
			os = new FileOutputStream(f, true);
			
			for(Map.Entry<String, byte[]> entry : dataToWrite.entrySet()) {
				byte[] data = entry.getValue();
				os.write(data);
				
				DataEntry dataEntry = new DataEntry();
				dataEntry.setOffset(offset);
				dataEntry.setDataLength(data.length);
				
				// Add the entry to the bucket's index.
				index.put(entry.getKey(), dataEntry);
				
				offset += data.length;
				
			}	
			
		} catch (IOException e) {
			logger.error(e);
			throw new ArchiveException(e);
		}
		finally {
			try {
				os.close();
			} catch (IOException e) {
				logger.error(e);
				throw new ArchiveException(e);				
			}
			
		}
	}


}
