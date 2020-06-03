import java.nio.file.*;
import java.util.concurrent.TimeUnit;
import java.io.File;
import java.rmi.server.*;
import java.rmi.*;
import java.util.*;

public class ServerImplementation extends UnicastRemoteObject implements ServerInterface {
	int port;
	static Vector<FileCache> serverCache;
	static HashMap<String, ClientInterface> clientList;
	static HashMap<String, Boolean> uploadStatuses;
	
	public ServerImplementation() throws RemoteException {
		port = 28580;
		ServerImplementation.serverCache = new Vector<FileCache>();
		ServerImplementation.clientList = new HashMap<String, ClientInterface>();
		ServerImplementation.uploadStatuses = new HashMap<String, Boolean>();
	}
	
	public ClientInterface lookupClient(String clientIP) {
		try {
			ClientInterface clientObject =  ( ClientInterface ) 
				Naming.lookup( "rmi://" + clientIP + ":" + port + "/fileclient" );
			return clientObject;
		}
		catch (Exception ex) {
			return null;
		}
	}
	
	public ClientInterface registerClient(String clientIP) {
		ClientInterface clientInterface = ServerImplementation.clientList.get(clientIP);
		if (clientInterface != null) {

			return clientInterface;
		} else {
			ClientInterface newClientInterface = lookupClient(clientIP);
			clientList.put(clientIP, newClientInterface);
			uploadStatuses.put(clientIP, false);

			return newClientInterface;
		}
	}

	public FileCache findCachedFile(String fileName) {
		for (FileCache fileCache : ServerImplementation.serverCache) {
			if (fileCache.name.equals(fileName))
				return fileCache;
		}
		return null;
	}
	
	public synchronized boolean upload(String clientIP, String fileName, FileContents fileContents) {
		System.out.println("---------------------------");
		System.out.println(clientIP + " UPLOADING: " + fileName);
		logCache();
		registerClient(clientIP);
		FileCache cachedFile = findCachedFile(fileName);
		String state = cachedFile.state;
		switch (state) {
			case "ownership_change": {
				synchronized (this) {
					System.out.println("\tupload OC");
					cachedFile.write(clientIP, fileContents.get());
					cachedFile.invalidateReaders();
					cachedFile.clearReaders();
					cachedFile.state = "write_shared";
	
					uploadStatuses.put(clientIP, true);
					notify();
					return true;
				}
			}
			case "write_shared": {
				System.out.println("\tupload WS");
				cachedFile.write(clientIP, fileContents.get());
				cachedFile.invalidateReaders();
				cachedFile.clearReaders();
				cachedFile.state = "not_shared";
				return true;
			}
			default: {
				return false;
			}
		}
	}
	
	public synchronized FileContents download(String clientIP, String fileName, String mode) {
		System.out.println("---------------------------");
		System.out.println(clientIP + " DOWNLOADING: " + fileName);
		logCache();

		registerClient(clientIP);
		FileCache fileCache = findCachedFile(fileName);

		if (fileCache == null) {
			return newOperation(fileName, clientIP, mode);
		} else {
			return cachedOperation(fileCache, clientIP, mode);
		}
	}

	FileContents newOperation(String fileName, String clientIP, String mode) {
		System.out.println("Handling new operation");
		String fileString = "/home/NETID/eyesack/434/hw4/src/" + fileName;
		Path filePath = Paths.get(fileString, new String[0]);
		File file = new File(fileString);
		FileCache fileCache = new FileCache(fileName, clientIP);
		byte[] fileBytes = new byte[0];

		if (file.exists()) {
			System.out.println("\tFile exists in directory");
			try {
				fileBytes = Files.readAllBytes(filePath);
				if (mode.equals("w"))
					fileCache.write(clientIP, fileBytes);
			} catch (Exception ex) {}
		}
		else {
			System.out.println("\tFile does not exist in directory");
			if (mode.equals("r"))
				return null;
		}
		ServerImplementation.serverCache.add(fileCache);
		return new FileContents(fileBytes);
	}

	FileContents cachedOperation(FileCache fileCache, String clientIP, String mode) {
		if (mode.equals("r")) {
			System.out.println("Handling cached read");
			fileCache.registerReader(clientIP);
			fileCache.state = "read_shared";
			return new FileContents(fileCache.data);
		} else {
			return cachedWrite(fileCache, clientIP);
		}
	}

	
	FileContents cachedWrite(FileCache fileCache, String clientIP) {
		System.out.println("Handling cached write");
		String state = fileCache.state;
		try {
			if (state == "not_shared" || state == "read_shared") {
				System.out.println("\tdownload NS/RS");
				fileCache.owner = clientIP;
				fileCache.state = "write_shared";
			} else if (state == "ownership_change") {
				System.out.println("\tdownload OC");

			} else { // state == "write_shared"
				System.out.println("\tdownload WS");
				uploadStatuses.put(clientIP, false);

				fileCache.state = "ownership_change";
				lookupClient(fileCache.owner).writeback();

				synchronized (this) { wait(); }
				fileCache.owner = clientIP;
			}

			return new FileContents(fileCache.data);
		}
		catch (Exception ex) {
			return null;
		}
	}

	public void logCache() {
		System.out.println("-----------Cache----------");

		for (FileCache file : serverCache)
			System.out.println("Name: " + file.name  + ", Owner: " + file.owner + 
							", # Readers: " + file.Readers.size() + ", State: " + file.state);
		System.out.println("---------------------------");
	}
}
