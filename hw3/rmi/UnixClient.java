import java.io.*;
import java.rmi.*;
import java.util.*;
import java.net.*;  // inetaddr

public class UnixClient {
  	public static void main( String args[] ) {
		String outputOption;
		boolean print = false;
		int port = 0;
		int numServers;
		String[] servers = null;
		int numCommands;
		String[] commands = null;
		long totalTimeElapsed = 0;

		try {
			outputOption = args[0];
			if (outputOption.equals("P")) {print = true;}

			port = Integer.parseInt(args[1]);
			numServers = Integer.parseInt(args[2]); 
			servers = new String[numServers];

			// how to read all server arguments
			for (int i = 0; i < numServers; i++) {
				String server = args[3 + i];
				servers[i] = server;
			}

			numCommands = Integer.parseInt(args[3 + numServers]);
			commands = new String[numCommands];
			
			// how to read all command arguments
			for (int i = 0; i < numCommands; i++) {
				String command = args[4 + numServers + i];
				commands[i] = command;
			}

		} catch ( Exception e ) {
			System.exit( -1 );
		}

		try {
            for (String server: servers) {
				//modify the URL based on which server
				ServerInterface serverObject = (ServerInterface) 
					Naming.lookup( "rmi://" + server + ":" + port + "/unixserver" );

				for (String command: commands) {
					Date startTime = new Date();
					Vector<String> results = serverObject.execute(command);

					System.out.println(server + " command(" + command +	"):..............................");
					long timeElapsed = logTime(startTime);
					totalTimeElapsed += timeElapsed;

					if (print == true) {
						for (String result: results)
							System.out.println(result);
					} else {
						System.out.println("Number of Lines in Message: " + results.size());
					}
				}
			}

			System.out.println("Total time elapsed: " + totalTimeElapsed);
		} catch ( Exception e ) {
			e.printStackTrace( );
			System.exit( -1 );
		}
	  }
	  
    // show how much time has elapsed
	private static long logTime(Date startTime) {
		Date endTime = new Date();
		long timeElapsed = endTime.getTime() - startTime.getTime();
		System.out.println("Time Elapsed: " + timeElapsed);

    return timeElapsed;
  }
}
