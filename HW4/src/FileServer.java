import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.Remote;
import java.rmi.Naming;

public class FileServer
{
    public static void main(String[] args) {
        int port = 28580;
        try {
            startRegistry(n);
            ServerImplementation serverObject = new ServerImplementation( );
            Naming.rebind( "rmi://localhost:" + port + "/server", serverObject );
            System.out.println("Server ready");
        }
        catch (Exception ex) {
            ex.printStackTrace();
            System.exit(-1);
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