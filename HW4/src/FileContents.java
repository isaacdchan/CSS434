import java.io.IOException;
import java.io.Serializable;

public class FileContents implements Serializable
{
    private static final long serialVersionUID = 1L;
    private byte[] contents;
    
    public FileContents(final byte[] contents) {
        this.contents = contents;
    }
    
    public void print() throws IOException {
        System.out.println( "FileContents = " + contents );
    }
    
    public byte[] get() {
        return this.contents;
    }
}