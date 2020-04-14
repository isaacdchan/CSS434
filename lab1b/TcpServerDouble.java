import java.net.*;
import java.io.*;

public class TcpServerDouble {

    public static void main( String args[] ) {
		if ( args.length != 2 ) {
			System.err.println( "usage: java TcpServer port size" );
			return;
		}
		try {
			ServerSocket svr = new ServerSocket( Integer.parseInt( args[0] ) );
			int multiplier = 1; // change multiplier to int
			int size = Integer.parseInt( args[1] );
			while ( true ) {
				// establslih a connection
				Socket socket = svr.accept( );
				InputStream in = socket.getInputStream( );
				OutputStream out = socket.getOutputStream( );

				ObjectInputStream o_in = new ObjectInputStream(in);
				ObjectOutputStream o_out = new ObjectOutputStream(out);

				DoubleArray receivedObject = (DoubleArray) o_in.readObject();
				for ( int i = 0; i < size; i++ ) // modify data
					receivedObject.data[i] *= multiplier;
				o_out.writeObject(receivedObject); // send back data

				socket.close( );
				multiplier *= 2;
			}

		} catch( Exception e ) {
			e.printStackTrace( );
		}
    }
}
