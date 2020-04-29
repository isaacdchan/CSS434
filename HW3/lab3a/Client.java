import java.io.*;
import java.rmi.*;
import java.util.*; // for scanner
import java.net.*;  // inetaddr

public class Client {

  public static void main( String args[] ) {
    // verify arguments
    int port = 0;
    try {
      if ( args.length == 2 ) {
	port = Integer.parseInt( args[1] );
	if ( port < 5001 || port > 65535 )
	  throw new Exception( );
      }
      else
	throw new Exception( );
    } catch ( Exception e ) {
      System.err.println( "usage: java Client serverIp port" );
      System.exit( -1 );
    }
    String serverIp = args[0];

    // looks for the registered Server Interface
    // and assigns it to serverObject
    try {
      // serverInterface doens't actually implement
      // only prototypes the functions. ends in semicolon
      ServerInterface serverObject =  ( ServerInterface ) Naming.lookup( "rmi://" + serverIp + ":" + port + "/server" );

      Scanner keyboard = new Scanner( System.in );
      
      // instantiates clientObject that will be passed to the serverObject
      ClientInterface clientObject = new ClientImplementation();
      // that way the serverObject know where to echo back
      serverObject.echo( clientObject, keyboard.nextLine( ) );
    }
    catch ( Exception e ) {
      e.printStackTrace( );
      System.exit( -1 );
    }
  }
}
