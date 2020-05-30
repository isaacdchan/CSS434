import java.util.*;     // for Vector
import java.io.*;       // for InputStream, BufferedReader Serializable
import java.net.*;      // for InetAddress
import UWAgent.*;

public class WhoAgent extends UWAgent implements Serializable {
    Vector<String> allusers;
    String[] servers;
    int index;
    String orgPlace;
    WhoAgent( ) { 
	System.out.println( "no arguments" );
    }

    public WhoAgent( String[] args ) {
	allusers = new Vector<String>( );
	servers = args;
	index = 0;
    }

    public void init( ) {
	if ( servers == null ) {
	    System.out.println( "nothing to do" );
	} else {
	    try {
		InetAddress inetaddr = InetAddress.getLocalHost( );
		orgPlace = inetaddr.getHostAddress( );
		hop( servers[index], "who", null );
	    } catch ( UnknownHostException e ) {
		System.err.println( e );
	    }
	}
    }

    public void who( ) {
	// find who are logging in
	Vector<String> output = get( );
	
	// Adding new user names to allusers
	allusers.add( new String( servers[index] + ":" ) );
	for ( int i = 0; i < output.size( ); i++ )
	    allusers.add( ( String )output.elementAt( i ) );

	if ( ++index < servers.length )
	    hop( servers[index], "who", null );
	else
	    hop( orgPlace, "results", null );
    }

    public void results( ) {
	for ( int i = 0; i < allusers.size( ); i++ )
	    System.out.println( ( String )allusers.elementAt( i ) );
    }

    public Vector<String> get( ) {
	Vector<String> output = new Vector<String>( );
	String line;
	try {
	    Runtime runtime = Runtime.getRuntime( );
	    Process process = runtime.exec( "who" );
	    InputStream input = process.getInputStream();
	    BufferedReader bufferedInput
		= new BufferedReader( new InputStreamReader( input ) );
	    while ( ( line = bufferedInput.readLine( ) ) != null ) {
		System.out.println( line );
		output.addElement( line );
	    }
	} catch ( IOException e ) {
	    e.printStackTrace( );
	    return output;
	}
	return output;
    }
}
