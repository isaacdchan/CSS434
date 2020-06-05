
import java.util.*;
import java.io.File;
import java.nio.file.*;
import java.io.FileOutputStream; 
import java.io.OutputStream; 
import java.rmi.server.*;
import java.rmi.*;

public class ServerImplementation extends UnicastRemoteObject implements ServerInterface {
	int port;
	int waitingToWrite;
	static Vector<FileCache> serverCache;
	static HashMap<String, ClientInterface> clientList;

	public ServerImplementation() throws RemoteException {
		port = 28580;
		waitingToWrite = 0;
		ServerImplementation.serverCache = new Vector<FileCache>();
		ServerImplementation.clientList = new HashMap<String, ClientInterface>();
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
					notifyAll();
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
			if (mode.equals("r")) {
				serverCache.remove(fileCache);
				System.out.println("\tRemoving file cache");
				return null;
			}
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
			if (state.equals("not_shared") || state.equals("read_shared")) {
				System.out.println("\tdownload NS/RS");
				fileCache.owner = clientIP;
				fileCache.state = "write_shared";

			} else  {
				// System.out.println("\tdownload OC");
				// System.out.println("\tdownload WS");

				waitingToWrite++;
				while (fileCache.state.equals("ownership_change")) {
					wait();
				}

				if (fileCache.state.equals("write_shared")){
					fileCache.state = "ownership_change";
					lookupClient(fileCache.owner).writeback();
					synchronized (this) { wait(); }
				}

				waitingToWrite--;

				fileCache.owner = clientIP;
				if (waitingToWrite > 1)
					notify();

			}

			System.out.println("RETURNING FILE CONTENTS TO: " + clientIP);
			return new FileContents(fileCache.data);
		} catch (Exception ex) {
			return null;
		}
	}

	public void writeToDisk() {
		byte[] emptyBytes = new byte[0];

		for (FileCache cache: serverCache) {
			String fileName = cache.name;
			String fileString = "/home/NETID/eyesack/434/hw4/src/" + fileName;
			File file = new File(fileString);

			try {
				OutputStream os = new FileOutputStream(file);
				os.write(emptyBytes);
				os.write(cache.data);
				os.close();
			} catch (Exception e) {}
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
