import java.net.*;
import java.io.*;

public class TcpClientDouble {
    public static void main( String args[] ) {
		if ( args.length != 3 ) {
			System.err.println( "usage: java TcpClient port size server_ip" );
			return;
		}
		try {
			// establish a connection
			Socket socket = new Socket( args[2], Integer.parseInt( args[0] ) );

			ObjectOutputStream o_out = new ObjectOutputStream(socket.getOutputStream());
			ObjectInputStream o_in = new ObjectInputStream (socket.getInputStream( ));

			int size = Integer.parseInt( args[1] );
			DoubleArray dataToSend = new DoubleArray(size); // initialize data

			o_out.writeObject( dataToSend ); // send data
			DoubleArray dataReceived = (DoubleArray) o_in.readObject(); // receive data

			dataReceived.print();

			socket.close( ); // close the connection

		} catch( Exception e ) {
			e.printStackTrace( );
		}
    }
}
