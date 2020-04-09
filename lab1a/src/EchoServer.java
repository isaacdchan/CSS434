import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;


public class EchoServer {
    public static void main( String args[] ) {
        // validate the argument

        if ( args.length != 1 ) {
            System.err.println("usage: java tcpServer port");
            return;
        }
        try {
            ServerSocket server = new ServerSocket(Integer.parseInt(args[0]));
            while ( true ) {
                Socket client = server.accept();
                client.close();
            }
        } catch (IOException e) {
            e.printStackTrace( );
        }
    }
}
