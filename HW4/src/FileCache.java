import java.io.PrintStream;
import java.rmi.Naming;
import java.util.Vector;

public class FileCache {
    int port = 28580;
    String name;
    Vector<String> Readers;
    String owner;
    String state;
    byte[] data;
    int dataSize;

    public FileCache(String string, String string2) {
        System.out.println("Creating new FileCache with name: " + string);
        this.name = string;
        this.Readers = new Vector();
        this.Readers.add(string2);
        this.owner = string2;
        this.state = "write_shared";
        this.data = new byte[0];
        this.dataSize = 0;
    }

    private ClientInterface lookupClient(String string) {
        try {
            return (ClientInterface)Naming.lookup("rmi://" + string + ":" + this.port + "/unixserver");
        }
        catch (Exception exception) {
            return null;
        }
    }

    public void registerReader(String string) {
        if (!this.Readers.contains(string)) {
            this.Readers.add(string);
        }
    }

    public void invalidateReaders() {
        try {
            for (String string : this.Readers) {
                this.lookupClient(string).invalidate();
            }
        }
        catch (Exception exception) {
        }
    }

    public void clearReaders() {
        this.Readers.clear();
    }

    public void write(String string, byte[] arrby) {
        this.owner = string;
        this.data = arrby;
        this.owner = "write_shared";
    }
}