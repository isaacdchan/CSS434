import java.io.*; // IOException
import java.net.*; // InetAddress
import java.rmi.*; // Naming
import java.rmi.server.*; // UnicastRemoteObject
import java.rmi.registry.*; // rmiregistry


public class FileServer {
	private BufferedReader input = null; // standard input

    public static void main(String[] args) {
        int port = 28580;
        try {
            startRegistry(port);
            ServerImplementation serverObject = new ServerImplementation( );
            Naming.rebind( "rmi://localhost:" + port + "/fileserver", serverObject );
            System.out.println("Server ready");
        }
        catch (Exception ex) {
            ex.printStackTrace();
            System.exit(-1);
        }
    }

    public void loop() {
        String filename = null;
        String mode = null;
        try {
            System.out.println("FileClient: Next file to open:");
            System.out.print("\tFile name: ");
            filename = input.readLine();
            if (filename.equals("quit") || filename.equals("exit")) {
                if (file.isStateWriteOwned())
                    file.upload();
                System.exit(0);
            } else if (filename.equals("")) {
                System.err.println("Do it again");
                writebackThread.kill();
                continue;
            }

            System.out.print("\tHow(r/w): ");
            mode = input.readLine();
            if (!mode.equals("r") && !mode.equals("w")) {
                System.err.println("Do it again");
                writebackThread.kill();
                continue;
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        // Now, the main thread manipulates the cached file. It terminates
        // the background thread that takes care of uploading the cached
        // file to the server.
        writebackThread.kill();

        // look through the cache
        if (file.hit(filename, mode) != true) {
            // cache miss
            if (file.isStateWriteOwned()) {
                // replacement
                file.upload();
            }
            // download a file from the server
            if (file.download(filename, mode) == false) {
                System.out.println("File downloaded failed");
                continue;
            }
        }
        // open an editor
        file.launchEditor(mode);
    }
    
    private static void startRegistry(int port) throws RemoteException {
        try {
            LocateRegistry.getRegistry(port).list();
        }
        catch (RemoteException ex) {
            LocateRegistry.createRegistry(port);
        }
    }
}