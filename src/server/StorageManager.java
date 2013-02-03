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
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;

import utils.Constants;
import entities.Bucket;
import entities.DataEntry;
import entities.GetStatus;
import entities.GetTask;
import entities.PutStatus;
import entities.PutTask;
import entities.Status;
import entities.Task;
import exceptions.ArchiveException;

/**
 * The StorageManager deals with all the disk operations.
 */
public class StorageManager {

	/** The Constant logger. */
	private static final Logger logger = Logger.getLogger(StorageManager.class);

	/** The storage manager. */
	private static StorageManager storageManager;
		
	/** The hashmap to synchronize access to a bucket. */
	private Map<Integer, Object> syncMap = new ConcurrentHashMap<>(); 

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
	public void processData(int bucketId, Queue<GetTask> getQueue, Queue<PutTask> putQueue, long currentTime) {
		//System.out.println("StorageManager - Processing Bucket: " + bucketId);
		
		/** The lockObject acts as the lock for the following critical section. 
		 *  No two threads operating on the same bucket will execute this section.
		 *  Any number of threads can execute this section as long as they are operating on different buckets. 
		 */
		Object lockObject = getLock(bucketId);
		
		synchronized (lockObject) {
			if ((getQueue == null || getQueue.size() == 0) && (putQueue == null || putQueue.size() == 0)) {
				// throw new IllegalArgumentException("Invalid task queues.");
				return;
			}
			Bucket bucket = readBucket(bucketId);
			//System.out.println("Bucket: " + bucket);
			
			if(putQueue != null) {
				
				// Do the write operations.
				writeData(bucket, putQueue, currentTime);				
			}
			
			if(getQueue != null) {
				// Do the read operations.
				readData(bucket, getQueue, currentTime);				
			}

			// Make the bucket eligible for garbage collection.
			bucket = null;
			
		}
		// System.out.println("Requests " + requests);
	}
	
	/**
	 * Gets the lock object for the given bucketId.
	 *
	 * @param bucketId the bucket id
	 * @return the lock
	 */
	private synchronized Object getLock(int bucketId) {		
		Object obj = syncMap.get(bucketId);
		if (obj == null) {
			obj = new Object();
			syncMap.put(bucketId, obj);
		}
		return obj;
	}

	/**
	 * Read data.
	 * 
	 * @param bucket the bucket
	 * @param tasks the tasks
	 */
	private void readData(Bucket bucket, Queue<GetTask> tasks, long currentTime) {
		Map<String, DataEntry> index = bucket.getIndex();
		/**
		 * Get the offset of the data associated with each task in the queue.
		 * Sort the offsets and do a lookup in the disk.
		 */
		List<DataEntry> dataEntryList = new ArrayList<>();
		DataEntry dataEntry = null;
		String hash = null;
		List<Status> statusList = new ArrayList<>();
		System.out.println("Reading " + tasks.size() + " task(s) at a time!");
		
		for (Iterator<GetTask> iter = tasks.iterator(); iter.hasNext();) {
			Task task = iter.next();
			hash = task.getHash();
			
			dataEntry = index.get(hash);

			// If the index does not contain the hash
			if (dataEntry == null) {				
				GetStatus status = new GetStatus();
				status.setSuccess(false);
				status.setHash(hash);
				statusList.add(status);
				logger.error("Error: There is no data associated with the hash " + task.getHash());
				System.out.println("There is no data associated with the hash " + task.getHash());				
			}
			else {
				dataEntry.setHash(hash);
				dataEntryList.add(dataEntry);
			}
			
			/**
			 * Remove the task after processing it in order to avoid situations
			 * where there will be duplicate task entries in the bucket queues because of multi-threading.
			 */
			iter.remove();			
		}		
		
		// Sort the list based on the disk offsets to do sequential reads. 
		Collections.sort(dataEntryList);
		
		List<Status> readList = readDataFromDisk(bucket, dataEntryList, currentTime);
		
		if (readList != null) {
			statusList.addAll(readList);
		}
		
		processResponse(statusList);
	}

	/**
	 * Write data.
	 * 
	 * @param bucket the bucket
	 * @param tasks the tasks
	 */
	private void writeData(Bucket bucket, Queue<PutTask> tasks, long currentTime) {
		Map<String, DataEntry> index = bucket.getIndex();

		/**
		 * Filter the data that is to be written to disk. i.e. Write only the
		 * blocks that are not already there.
		 */
		Map<String, byte[]> dataToWrite = new HashMap<>();
		List<Status> statusList = new ArrayList<>();		
		String hash = null;
		
		System.out.println("Writing " + tasks.size() + " task(s) at a time!");
		
		for (Iterator<PutTask> iter = tasks.iterator(); iter.hasNext();) {
			Task task = iter.next();
			hash = task.getHash();			
			
			if (! index.containsKey(hash)) {
				dataToWrite.put(hash, ((PutTask)task).getData());
				/** 
				 * Put a dummy entry in the index.
				 * So, when the task queue has two or more data blocks which are same, we will add only one copy to 'dataToWrite' map.
				 */
				index.put(hash, null);
			}
			else {				
				PutStatus status = new PutStatus();
				status.setHash(hash);
				status.setSuccess(true);
				status.setStartTime(currentTime);
				status.setEndTime(System.currentTimeMillis());
				statusList.add(status);
			}
			/**
			 * Remove the task after processing it in order to avoid situations
			 * where there will be duplicate task entries in the bucket queues because of multi-threading.
			 */
			iter.remove();
		}		
		
		if (dataToWrite.size() > 0) {	
			// The bucket's index will be modified in place.
			statusList.addAll(writeDataToDisk(bucket, dataToWrite, currentTime));
	
			// Write (serialize) the modified bucket back to disk.
			writeBucket(bucket);
						
		}
		processResponse(statusList);
	}

	/**
	 * Read bucket.
	 * 
	 * @param bucketId the bucket id
	 * @return bucket
	 */
	private Bucket readBucket(int bucketId) {
		String bucketPath = Constants.BUCKET_DIR + File.separator + bucketId + Constants.BUCKET_FILE_EXTENSION;
		ObjectInputStream inputStream = null;
		Bucket bucket = null;
		try {			
			File file = new File(bucketPath.substring(0, bucketPath.lastIndexOf("/")));
			
			// Create the directory if it is not already there.			
			if (! file.exists()) {				
				file.mkdirs();
				return new Bucket(bucketId);
			}
			
			// If the bucket file is not present, simply return a new bucket.
			if (! new File(bucketPath).exists()) {
				return new Bucket(bucketId);
			}
			
			inputStream = new ObjectInputStream(new FileInputStream(bucketPath));
			bucket = (Bucket) inputStream.readObject();						
			
		} catch (IOException e) {
			e.printStackTrace();
			throw new ArchiveException(e);
		} catch (ClassNotFoundException e) {
			logger.error("Bucket not found. Exception: " + e);
			throw new ArchiveException(e);
		}

		finally {
			try {
				if( inputStream != null) {
					inputStream.close();
				}
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
		if (bucket == null) {
			throw new IllegalArgumentException("Invalid bucketId or task queue.");
		}

		String bucketPath = Constants.BUCKET_DIR + File.separator + bucket.getId() + Constants.BUCKET_FILE_EXTENSION;
		ObjectOutputStream outputStream = null;

		try {
			outputStream = new ObjectOutputStream(new FileOutputStream(bucketPath));
			outputStream.writeObject(bucket);
			outputStream.flush();
		} catch (IOException e) {
			logger.error(e);
			throw new ArchiveException(e);
		} finally {
			try {
				if (outputStream != null) {
					outputStream.close();
				}
			} catch (IOException e) {
				logger.error(e);
				throw new ArchiveException(e);
			}
		}
	}

	/**
	 * Read data from disk.
	 *
	 * @param bucket the bucket
	 * @param dataEntries the data entries
	 * @return list
	 */
	private List<Status> readDataFromDisk(Bucket bucket, List<DataEntry> dataEntries, long currentTime) {
		List<Status> statusList = new ArrayList<>();

		String filePath = Constants.DATA_DIR + File.separator + bucket.getId() + Constants.DATASTORE_FILE_EXTENSION;
		RandomAccessFile raf = null;
		System.out.println("Reading data from disk for bucket " + bucket.getId());		
		try {
			File file = new File(filePath);
			if (! file.exists()) {
				logger.error("There is no file for bucket " + bucket.getId());
				System.out.println("There is no file for bucket " + bucket.getId());
				return null;
			}
			
			raf = new RandomAccessFile(filePath, "r");
			for (DataEntry dataEntry : dataEntries) {
				byte[] data = new byte[dataEntry.getDataLength()];
				// Seek to the data's offset and read the data.
				raf.seek(dataEntry.getOffset());
				raf.read(data, 0, data.length);				
				
				// Generate a Status for each read.
				GetStatus status = new GetStatus();
				status.setSuccess(true);
				status.setData(data);
				status.setHash(dataEntry.getHash());
				status.setStartTime(currentTime);
				status.setEndTime(System.currentTimeMillis());
				
				statusList.add(status);
			}
			
		} catch (IOException e) {
			logger.error(e);
			throw new ArchiveException(e);
		} finally {
			try {
				if (raf != null) {
					raf.close();
				}				
			} catch (IOException e) {
				logger.error(e);
				throw new ArchiveException(e);
			}
		}
		
		return statusList;

	}

	/**
	 * Write data to disk.
	 *
	 * @param bucket the bucket
	 * @param dataToWrite the data
	 * @return list
	 */
	private List<Status> writeDataToDisk(Bucket bucket, Map<String, byte[]> dataToWrite, long currentTime) {
		OutputStream os = null;

		String filePath = Constants.DATA_DIR + File.separator + bucket.getId() + Constants.DATASTORE_FILE_EXTENSION;
		
		List<Status> statusList = new ArrayList<>();
		
		try {
			File f = new File(filePath.substring(0, filePath.lastIndexOf("/")));
			// If this is the first write, create the dir first.
			if(! f.exists()) {
				f.mkdirs();
			}
			
			long offset = 0;
			f = new File(filePath);
			
			if(f.exists()) {
				offset = f.length();
			}
			
			os = new FileOutputStream(filePath, true);
			Map<String, DataEntry> index = bucket.getIndex();			

			for (Map.Entry<String, byte[]> entry : dataToWrite.entrySet()) {
				byte[] data = entry.getValue();
				os.write(data);

				DataEntry dataEntry = new DataEntry();
				dataEntry.setOffset(offset);
				dataEntry.setDataLength(data.length);

				// Add the entry to the bucket's index.
				index.put(entry.getKey(), dataEntry);

				offset += data.length;
				
				// Generate a PutStatus and send it to the client via the router.
				PutStatus status = new PutStatus();
				status.setSuccess(true);
				status.setHash(entry.getKey());
				status.setStartTime(currentTime);
				status.setEndTime(System.currentTimeMillis());
				statusList.add(status);
			}

		} catch (IOException e) {
			logger.error(e);
			throw new ArchiveException(e);
		} finally {
			try {
				if (os != null) {
					os.close();
				}
			} catch (IOException e) {
				logger.error(e);
				throw new ArchiveException(e);
			}

		}
		return statusList;
	}
	
	/**
	 * Send the response to be processed by the router.
	 *
	 * @param statusList the status list
	 */
	private void processResponse(List<Status> statusList) {		
		try {
			StorageServerImpl.getInstance().processResponse(statusList);			
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}

}
