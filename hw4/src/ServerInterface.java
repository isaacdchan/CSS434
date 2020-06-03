import java.rmi.*;
import java.util.*;

public interface ServerInterface extends Remote{
    int port = 28580;
    static Vector<FileCache> serverCache = new Vector<FileCache>();;
    static HashMap<String, ClientInterface> clientList = new HashMap<String, ClientInterface>();
    
    public FileContents download( String client, String filename, String mode )
	throws RemoteException;

    public boolean upload( String client, String filename, 
			   FileContents contents ) throws RemoteException;
}