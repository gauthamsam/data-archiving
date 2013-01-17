package api;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

import entities.Status;

public interface RouterToClient extends Remote{
	
	public void setStatus (List<Status> status) throws RemoteException;

}
