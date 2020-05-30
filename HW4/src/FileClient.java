import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.Reader;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

public class FileClient
extends UnicastRemoteObject
implements ClientInterface {
    private BufferedReader input = null;
    private static final String ramDiskFile = "/tmp/eyesack.txt";
    private ServerInterface server = null;
    private File file = null;
    private boolean emacs_option = false;

    public FileClient(String string, String string2, boolean bl) throws RemoteException {
        try {
            this.server = (ServerInterface)Naming.lookup("rmi://" + string + ":" + string2 + "/fileserver");
        }
        catch (Exception exception) {
            exception.printStackTrace();
        }
        this.file = new File(this);
        this.input = new BufferedReader(new InputStreamReader(System.in));
        this.emacs_option = bl;
    }

    public void loop() {
        do {
            WritebackThread writebackThread = new WritebackThread(this);
            writebackThread.start();
            String string = null;
            String string2 = null;
            try {
                System.out.println("FileClient: Next file to open:");
                System.out.print("\tFile name: ");
                string = this.input.readLine();
                if (string.equals("quit") || string.equals("exit")) {
                    if (this.file.isStateWriteOwned()) {
                        this.file.upload();
                    }
                    System.exit(0);
                } else if (string.equals("")) {
                    System.err.println("Do it again");
                    writebackThread.kill();
                    continue;
                }
                System.out.print("\tHow(r/w): ");
                string2 = this.input.readLine();
                if (!string2.equals("r") && !string2.equals("w")) {
                    System.err.println("Do it again");
                    writebackThread.kill();
                    continue;
                }
            }
            catch (IOException iOException) {
                iOException.printStackTrace();
            }
            writebackThread.kill();
            if (!this.file.hit(string, string2)) {
                if (this.file.isStateWriteOwned()) {
                    this.file.upload();
                }
                if (!this.file.download(string, string2)) {
                    System.out.println("File downloaded failed");
                    continue;
                }
            }
            this.file.launchEditor(string2);
        } while (true);
    }

    @Override
    public boolean invalidate() {
        return this.file.invalidate();
    }

    @Override
    public boolean writeback() {
        return this.file.writeback();
    }

    public static void main(String[] args) {
        String string = "cssmpi1h";
        String string2 = "28580";
        boolean bl = false;
        try {
            FileClient fileClient = new FileClient(string, string2, bl);
            FileClient.startRegistry(Integer.parseInt(string2));
            Naming.rebind("rmi://localhost:" + string2 + "/fileclient", fileClient);
            System.out.println("rmi://localhost: " + string2 + "/fileclient invoked");
            fileClient.loop();
        }
        catch (Exception exception) {
            exception.printStackTrace();
            System.exit(1);
        }
    }

    private static void startRegistry(int n) throws RemoteException {
        try {
            Registry registry = LocateRegistry.getRegistry(n);
            registry.list();
        }
        catch (RemoteException remoteException) {
            Registry registry = LocateRegistry.createRegistry(n);
        }
    }
}