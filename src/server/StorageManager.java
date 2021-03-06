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
import entities.GetTask;
import entities.PutTask;
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
	private Map<Integer, Object> syncMap = new ConcurrentHashMap<Integer, Object>(); 

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
	public void processData(int bucketId, Queue<GetTask> getQueue, Queue<PutTask> putQueue) {
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
			//long startTime = System.currentTimeMillis();
			//System.out.println("size: " + putQueue.size());
			Bucket bucket = readIndex(bucketId);
			//System.out.println("Bucket: " + bucket);
			if(putQueue != null) {
				// Do the write operations.
				writeData(bucket, putQueue);
			}
			if(getQueue != null) {
				// Do the read operations.
				readData(bucket, getQueue);
			}			
			//long endTime = System.currentTimeMillis();
			//System.out.println("Time taken: " + (endTime - startTime) + " ms");
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
	private void readData(Bucket bucket, Queue<GetTask> tasks) {
		Map<String, DataEntry> index = bucket.getIndex();
		/**
		 * Get the offset of the data associated with each task in the queue.
		 * Sort the offsets and do a lookup in the disk.
		 */
		List<DataEntry> dataEntryList = new ArrayList<DataEntry>();
		DataEntry dataEntry = null;
		String hash = null;
		List<GetTask> statusList = new ArrayList<GetTask>();
		// System.out.println("Reading " + tasks.size() + " task(s) at a time!");
		Map<String, GetTask> taskMap = new HashMap<String, GetTask>();
		
		for (Iterator<GetTask> iter = tasks.iterator(); iter.hasNext();) {
			GetTask task = iter.next();
			hash = new String(task.getHash());
			
			dataEntry = index.get(hash);

			// If the index does not contain the hash
			if (dataEntry == null) {
				task.setStatus(false);
				statusList.add(task);
				logger.error("Error: There is no data associated with the hash " + task.getHash());
				System.out.println("There is no data associated with the hash " + task.getHash());	
				
			}
			else {
				taskMap.put(hash, task);
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
		
		List<GetTask> readList = readDataFromDisk(bucket, dataEntryList, taskMap);
		
		if (readList != null) {
			statusList.addAll(readList);
		}
		
		processResponse(statusList);
		statusList = null;
	}

	/**
	 * Write data.
	 * 
	 * @param bucket the bucket
	 * @param tasks the tasks
	 */
	private void writeData(Bucket bucket, Queue<PutTask> tasks) {
		Map<String, DataEntry> index = bucket.getIndex();

		/**
		 * Filter the data that is to be written to disk. i.e. Write only the
		 * blocks that are not already there.
		 */
		List<PutTask> dataToWrite = new ArrayList<PutTask>();
		List<PutTask> statusList = new ArrayList<PutTask>();		
		String hash = null;
		
		// System.out.println("Writing " + tasks.size() + " task(s) at a time!");
		
		for (Iterator<PutTask> iter = tasks.iterator(); iter.hasNext();) {
			PutTask task = iter.next();
			hash = new String(task.getHash());			
			
			// If the incoming data block is not a duplicate
			if (! index.containsKey(hash)) {
				dataToWrite.add(task);
				/** 
				 * Put a dummy entry in the index.
				 * So, when the task queue has two or more data blocks which are same, we will add only one copy to 'dataToWrite' map.
				 */
				index.put(hash, null);
			}
			else {				
				task.setStatus(true);
				statusList.add(task);
			}
			/**
			 * Remove the task after processing it in order to avoid situations
			 * where there will be duplicate task entries in the bucket queues because of multi-threading.
			 */
			iter.remove();			
		}		
		
		if (dataToWrite.size() > 0) {	
			// The bucket's index will be modified in place.
			statusList.addAll(writeDataToDisk(bucket, dataToWrite));
	
			// Write (serialize) the modified bucket back to disk.
			writeIndex(bucket);						
		}
		
		processResponse(statusList);
		statusList = null;
	}

	/**
	 * Read index.
	 * 
	 * @param bucketId the bucket id
	 * @return bucket
	 */
	private Bucket readIndex(int bucketId) {
		String bucketPath = Constants.BUCKET_DIR + File.separator + bucketId + Constants.INDEX_FILE_EXTENSION;
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
	 * Write index.
	 * 
	 * @param bucket the bucket
	 */
	private void writeIndex(Bucket bucket) {
		if (bucket == null) {
			throw new IllegalArgumentException("Invalid bucketId or task queue.");
		}

		String bucketPath = Constants.BUCKET_DIR + File.separator + bucket.getId() + Constants.INDEX_FILE_EXTENSION;
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
	private List<GetTask> readDataFromDisk(Bucket bucket, List<DataEntry> dataEntries, Map<String, GetTask> taskMap) {
		List<GetTask> statusList = new ArrayList<GetTask>();

		String filePath = Constants.DATA_DIR + File.separator + bucket.getId() + Constants.DATASTORE_FILE_EXTENSION;
		RandomAccessFile raf = null;
		// System.out.println("Reading data from disk for bucket " + bucket.getId());		
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
				
				GetTask task = taskMap.get(dataEntry.getHash());
				task.setStatus(true);
				task.setResponseData(data);
				
				statusList.add(task);
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
	private List<PutTask> writeDataToDisk(Bucket bucket, List<PutTask> dataToWrite) {
		OutputStream os = null;

		String filePath = Constants.DATA_DIR + File.separator + bucket.getId() + Constants.DATASTORE_FILE_EXTENSION;
		
		List<PutTask> statusList = new ArrayList<PutTask>();
		
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
			
			for (PutTask task : dataToWrite) {
				byte[] data = task.getData();
				os.write(data);
				
				DataEntry dataEntry = new DataEntry();
				dataEntry.setOffset(offset);
				dataEntry.setDataLength(data.length);
				if(index.get(task.getHash()) != null){
					System.out.println("Goner!!");
				}
				// Add the entry to the bucket's index.
				index.put(new String(task.getHash()), dataEntry);

				offset += data.length;				
				
				task.setStatus(true);
				statusList.add(task);
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
	private void processResponse(List<? extends Task> statusList) {		
		try {
			StorageServerImpl.getInstance().processResponse(statusList);			
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}	
	
}
