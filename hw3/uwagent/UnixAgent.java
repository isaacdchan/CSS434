import java.io.*;
import java.util.*;
import UWAgent.*;

public class UnixAgent extends UWAgent {
    private static final long serialVersionUID = 6529685098267757690L;
    String outputOption;
    boolean print = false;
    int port = 0;
    int numServers;
    String[] servers = null;
    int numCommands;
    String[] commands = null;
    Vector<Vector<String>> outputs;
    long totalTimeElapsed = 0;

    public UnixAgent ( String[] args ) {
        try {
            outputOption = args[0];
            if (outputOption.equals("P")) {print = true;}

            // store servers in array
            numServers = Integer.parseInt(args[1]); 
            servers = new String[numServers];

            for (int i = 0; i < numServers; i++) {
                String server = args[2 + i];
                servers[i] = server;
            }

            // store commands in array
            numCommands = Integer.parseInt(args[2 + numServers]);
            commands = new String[numCommands];
            
            for (int i = 0; i < numCommands; i++) {
                String command = args[3 + numServers + i];
                commands[i] = command;
            }

            outputs = new Vector<Vector<String>>();
            for (int i = 0; i < numServers; i ++)
                outputs.addElement(new Vector<String>());

        } catch ( Exception e ) {
            System.out.println(e);
            System.exit( -1 );
        }
    }
    
    // hardcoded values for constructor with no arguments
    public UnixAgent( ) { 
        try {
            print = true;

            numServers = 2;
            servers = new String[numServers];
            servers[0] = "cssmpi2h";
            servers[1] = "cssmpi3h";

            numCommands = 2;
            commands = new String[numCommands];
            commands[0] = "who";
            commands[1] = "ls";
            
            // class variable to store output since hop returns void
            outputs = new Vector<Vector<String>>( );
            for (int i = 0; i < numServers; i ++) {
                outputs.addElement(new Vector<String>());
            }

        } catch ( Exception e ) {
            System.out.println(e);
            System.exit( -1 );
        }
    }

    public void init( ) {
        String[] args = new String[1];
        args[0] = "0";
        Date startTime = new Date();
        hop(servers[0], "execute", args);

        long timeElapsed = logTime(startTime);
        totalTimeElapsed += timeElapsed;
    }

    public void execute(String args[]) {
        int serverInt = Integer.parseInt(args[0]);

		try {
            String nextServer;
            String[] nextArgs = new String[1];
            
            Runtime runtime = Runtime.getRuntime( );
            
            for(String command: commands) {
                Process process = runtime.exec(command);
                InputStream input = process.getInputStream();
                BufferedReader bufferedInput
                = new BufferedReader( new InputStreamReader( input ) );
                
                String line;
                while ( ( line = bufferedInput.readLine( ) ) != null ) {
                    outputs.get(serverInt).addElement( line );
                }
            }

            // print entire output or just number of lines
            if (print == true) {
                for (String output: outputs.get(serverInt))
                    System.out.println(output);
            } else {
                System.out.println("Number of Lines in Message: " + outputs.get(serverInt).size());
            }

            // check if there are any more servers to hop to
            if (serverInt == numServers - 1) {
                hop(servers[0], "finish", nextArgs);
            } else {
                // increment serverInt to signify jumping
                nextServer = servers[serverInt+1];
                nextArgs[0] = Integer.toString(serverInt+1);
                hop(nextServer, "execute", nextArgs);
            }
        } catch ( IOException e ) {
			e.printStackTrace( );
        }
    }

    public void finish(String args[]) {
        System.out.println("Finished!");
    }
    private static long logTime(Date startTime) {
        Date endTime = new Date();
        long timeElapsed = endTime.getTime() - startTime.getTime();
        System.out.println("Time Elapsed: " + timeElapsed);

        return timeElapsed;
    }
}
