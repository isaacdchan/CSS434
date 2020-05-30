import java.nio.file.Path;
import java.nio.file.Files;
import java.io.File;
import java.nio.file.Paths;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.*;
import java.rmi.server.UnicastRemoteObject;

public class ServerImplementation extends UnicastRemoteObject implements ServerInterface {
    int port;
    static Vector<FileCache> serverCache;
    static HashMap<String, ClientInterface> ClientList;
    
    public ServerImplementation() throws RemoteException {
        this.port = 28580;
        ServerImplementation.serverCache = new Vector<FileCache>();
        ServerImplementation.ClientList = new HashMap<String, ClientInterface>();
    }
    
    public FileContents download( String client, String filename, String mode )
	throws RemoteException;

    public boolean upload( String client, String filename, 
			   FileContents contents ) throws RemoteException;
}