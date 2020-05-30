import java.io.*;
import UWAgent.*;

public class MyAgent extends UWAgent implements Serializable {
    private String destination = null;
    
    public MyAgent( String[] args ) {
        System.out.println( "Injected1" );
    }
    public MyAgent( ) { 
        System.out.println( "Injected2" );
    }

    public void init( ) {
        System.out.println("hop");
        hop("cssmpi2h", "func1");
    }

    public void func1() {
        System.out.println("step");
        hop("cssmpi3h", "func2");
    }
    public void func2() {
        System.out.println("jump");
    }
}
