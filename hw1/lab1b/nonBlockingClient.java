import java.net.*;
import java.io.*;

public class nonBlockingClient {
  public static void main( String args[] ) {
    if ( args.length != 3 ) {
      System.err.println( "usage: java TcpClient port size server_ip" );
      return;
    }
    try {
      // establish a connection
      Socket writeSocket = new Socket( args[2], Integer.parseInt( args[0] ) );
      Socket readSocket = new Socket( args[2], Integer.parseInt( args[0] ) );

      OutputStream os = writeSocket.getOutputStream();
      InputStream is = readSocket.getInputStream();

      byte[] data = new byte[1024];
      os.write(data); // send data
      byte[] dataReceived = is.read(); // receive data

      System.out.println( new String (dataReceived);

    } catch( Exception e ) {
      e.printStackTrace( );
    }
  }
}
