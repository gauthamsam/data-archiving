package api;

import java.rmi.Remote;

public interface StorageServer extends Remote{

	public void putTask();
}
