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
	
	/** The storage manager. */
	private static StorageManager storageManager;
	
	/**
	 * Gets the single instance of StorageManager.
	 *
	 * @return single instance of StorageManager
	 */
	public static synchronized StorageManager getInstance() {
		if (storageManager == null) {
			storageManager = new StorageManager();
		}
		return storageManager;
	}


	/**
	 * Process data.
	 *
	 * @param bucketId the bucket id
	 * @param getQueue the get queue
	 * @param putQueue the put queue
	 */
	public void processData(int bucketId, Queue<Task> getQueue, Queue<Task> putQueue) {
		if(bucketId < 0 || getQueue == null || getQueue.size() == 0 || putQueue == null || putQueue.size() == 0) {
			throw new IllegalArgumentException("Invalid bucketId or task queue.");
		}
		
		// Get the bucket from disk.
		Bucket bucket = readBucket(bucketId);
		
		synchronized(bucket) {
			// Do the read operations.
			readData(bucket, getQueue);
			
			// Do the write operations.
			writeData(bucket, putQueue);
		}
	}
	
	/**
	 * Read data.
	 *
	 * @param bucket the bucket
	 * @param tasks the tasks
	 */
	private void readData(Bucket bucket, Queue<Task> tasks) {
		Map<String, DataEntry> index = bucket.getIndex();
		/* Get the offset of the data associated with each task in the queue.
		 * Sort the offsets and do a lookup in the disk.
		 */
		List<DataEntry> offsets = new ArrayList<>();
		DataEntry offset = null;
		
		for(Task task : tasks) {
			offset = index.get(task.getHash());
			
			// If the index does not contain the hash
			if (offset == null) { 
				throw new ArchiveException("There is no data associated with the hash " + task.getHash());
			}
			
			offsets.add(offset);
		}
		
		Collections.sort(offsets);
		readDataFromDisk(bucket.getId(), offsets);
	}
	
	/**
	 * Write data.
	 *
	 * @param bucket the bucket
	 * @param tasks the tasks
	 */
	private void writeData(Bucket bucket, Queue<Task> tasks) {
		Map<String, DataEntry> index = bucket.getIndex();
		
		// Filter the data that is to be written to disk. i.e. Write only the blocks that are not already there.
		Map<String, byte[]> dataToWrite = new HashMap<>();
		for(Task task : tasks) {
			if(! index.containsKey(task.getHash())) {
				dataToWrite.put(task.getHash().toString(), task.getData());			
			}
		}
		
		// The bucket's index will be modified in place.
		writeDataToDisk(bucket.getId(), dataToWrite, index);
		
		// Write (serialize) the modified bucket back to disk.
		writeBucket(bucket);
	}
	
	/**
	 * Read bucket.
	 *
	 * @param bucketId the bucket id
	 * @return bucket
	 */
	private Bucket readBucket(int bucketId) {		
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
	 * @param bucket the bucket
	 */
	private void writeBucket(Bucket bucket) {
		if(bucket == null) {
			throw new IllegalArgumentException("Invalid bucketId or task queue.");
		}
		
		String bucketPath = Constants.FILE_PATH + File.separator + bucket.getId();
		ObjectOutputStream outputStream = null;
		
		try {
			outputStream = new ObjectOutputStream(new FileOutputStream(bucketPath));			
			outputStream.writeObject(bucket);
			outputStream.flush();
		}
		catch(IOException e) {
			logger.error(e);
			throw new ArchiveException(e);
		}
		finally {
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
	private void readDataFromDisk(int bucketId, List<DataEntry> dataEntries) {
		List<byte[]> dataList = new ArrayList<>();
		
		String filePath = Constants.FILE_PATH + File.separator + bucketId;
		RandomAccessFile raf = null;
		
		try {
			raf = new RandomAccessFile(filePath, "r");
			
			for(DataEntry dataEntry : dataEntries) {
				byte[] data = new byte[dataEntry.getDataLength()];
				// Seek to the data's offset and read the data.
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
	private void writeDataToDisk(int bucketId, Map<String, byte[]> dataToWrite, Map<String, DataEntry> index) {
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
