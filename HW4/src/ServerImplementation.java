import java.nio.file.*;
import java.io.File;
import java.rmi.server.*;
import java.rmi.*;
import java.util.*;

public class ServerImplementation extends UnicastRemoteObject implements ServerInterface
{
    int port;
    static Vector<FileCache> serverCache;
    static HashMap<String, ClientInterface> ClientList;
    
    public ServerImplementation() throws RemoteException {
        this.port = 28580;
        ServerImplementation.serverCache = new Vector<FileCache>();
        ServerImplementation.ClientList = new HashMap<String, ClientInterface>();
    }
    
    public ClientInterface lookupClient(String clientIP) {
        try {
            ServerInterface serverObject =  ( ServerInterface ) 
                Naming.lookup( "rmi://" + clientIP + ":" + this.port + "/fileclient" );
        }
        catch (Exception ex) {
            return null;
        }
    }
    
    public ClientInterface registerClient(String name) {
        ClientInterface clientInterface = ServerImplementation.ClientList.get(name);
        if (clientInterface != null) {
            return clientInterface;
        }
        return this.lookupClient(name);
    }
    
    public synchronized boolean upload(String clientIP, String fileName, FileContents fileContents) {
        System.out.println("--------------------------");
        System.out.println(clientIP + " downloading: " + fileName);
        this.registerClient(clientIP);
        FileCache cachedFile = this.findCachedFile(fileName);
        System.out.println("456");
        String state = cachedFile.state;
        switch (state) {
            case "ownership_change": {
                cachedFile.write(s, fileContents.get());
                cachedFile.invalidateReaders();
                cachedFile.clearReaders();
                cachedFile.state = "write_shared";
                return true;
            }
            case "write_shared": {
                cachedFile.write(clientIP, fileContents.get());
                cachedFile.invalidateReaders();
                cachedFile.clearReaders();
                cachedFile.state = "not_shared";
                this.notify();
                return true;
            }
            default: {
                return false;
            }
        }
    }
    
    public synchronized FileContents download(String clientIP, String fileName, String mode) {
        System.out.println("--------------------------");
        System.out.println(clientIP + " downloading: " + fileName);
        this.registerClient(clientIP);
        FileCache cachedFile = this.findCachedFile(fileName);
        if (mode.equals("r")) {
            if (cachedFile == null) {
                return null;
            }
            cachedFile.registerReader(s);
            cachedFile.state = "read_shared";
            return new FileContents(cachedFile.data);
        }
        else {
            if (cachedFile == null) {
                return this.handleNewWrite(fileName, clientIP);
            }
            return this.handleCachedWrite(cachedFile, clientIP);
        }
    }
    
    public FileCache findCachedFile(String fileName) {
        System.out.println("Looking for file in cache: " + fileName);

        for (FileCache fileCache : ServerImplementation.serverCache) {
            if (fileCache.name.equals(fileName)) {
                System.out.println("\tFile found");
                return fileCache;
            }
        }
        System.out.println("\tFile not found");
        return null;
    }
    
    FileContents handleNewWrite(String clientIP, String fileName) {
        System.out.println("Handling new write");
        String fileString = "~/434/hw4/src/" + fileName;
        Path filePath = Paths.get(fileString, new String[0]);
        File file = new File(fileString);
        FileCache newFileCache = new FileCache(clientIP, fileName);
        byte[] fileBytes = new byte[0];

        if (file.exists()) {
            System.out.println("\tFile exists in directory");
            try {
                fileBytes = Files.readAllBytes(filePath);
                e.write(fileName, fileBytes);
            }
            catch (Exception ex) {}
        }
        else {
            System.out.println("\tFile does not exist in directory");
        }
        ServerImplementation.serverCache.add(newFileCache);
        return new FileContents(fileBytes);
    }
    
    FileContents handleCachedWrite(FileCache fileCache, String owner) {
        System.out.println("Handling cached write");
        String state = fileCache.state;
        try {
            String s = state;
            switch (s) {
                case "not_shared":
                case "read_shared": {
                    fileCache.owner = owner;
                    fileCache.state = "write_shared";
                }
                case "write_shared": {
                    this.lookupClient(fileCache.owner).writeback();
                    this.wait();
                    fileCache.state = "ownership_change";
                }
                case "ownership_change": {
                    this.wait();
                    fileCache.state = "ownership_change";
                    break;
                }
            }
            return new FileContents(fileCache.data);
        }
        catch (Exception ex) {
            return null;
        }
    }
}
