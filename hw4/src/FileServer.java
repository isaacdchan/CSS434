import java.io.*; // IOException
import java.net.*; // InetAddress
import java.rmi.*; // Naming
import java.rmi.server.*; // UnicastRemoteObject
import java.rmi.registry.*; // rmiregistry


public class FileServer {
    private BufferedReader input = null; // standard input
    ServerImplementation serverObject;

    public FileServer() throws RemoteException {
        serverObject = new ServerImplementation( );
        input = new BufferedReader(new InputStreamReader(System.in));
    }
    
    public static void main(String[] args) {
        int port = 28580;
        try {
            startRegistry(port);
			FileServer fileServer = new FileServer();
            Naming.rebind( "rmi://localhost:" + port + "/fileserver", fileServer.serverObject);
            System.out.println("Server ready");
            fileServer.loop();
        }
        catch (Exception ex) {
            ex.printStackTrace();
            System.exit(-1);
        }
    }

    public void loop() {
        
        while (true) {
            try {
                System.out.println("Enter \"exit\" to close server and write updates");
                String exitCommand = input.readLine();
                if (!exitCommand.equals("exit")) {
                    System.err.println("Invalid input");
                    continue;
                } else {
                    serverObject.writeToDisk();
                    System.exit(-1);
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
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