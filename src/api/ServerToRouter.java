package api;

import java.rmi.Remote;

public interface ServerToRouter extends Remote{

	public void register(StorageServer server);
}
