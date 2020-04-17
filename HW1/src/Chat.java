import java.net.*;  // ServerSocket, Socket
import java.io.*;   // InputStream, ObjectInputStream, ObjectOutputStream
import java.util.*;   // Arraylist

public class Chat {
    // Each element i of the following arrays represent a chat member[i]
    private Socket[] sockets;             // connection to i
    private int[] stamps;
	private ArrayList<String> msgQueue;
	private ArrayList<int[]> stampQueue;
	private ArrayList<Integer> srcQueue;

	private InputStream[] indata;         // used to check data from i
    private ObjectInputStream[] inputs;   // a message from i
    private ObjectOutputStream[] outputs; // a message to i


    public Chat(int port, int rank, String[] hosts) throws IOException {
        // print out my port, rank and local hostname
        System.out.println("port = " + port + ", rank = " + rank +
                ", localhost = " + hosts[rank]);

        // create sockets, inputs, outputs, and vector arrays
        sockets = new Socket[hosts.length];
		stamps = new int[hosts.length];
		msgQueue = new ArrayList<String>();
		stampQueue = new ArrayList<int[]>();
		srcQueue = new ArrayList<Integer>();


		indata = new InputStream[hosts.length];
        inputs = new ObjectInputStream[hosts.length];
        outputs = new ObjectOutputStream[hosts.length];

        // establish a complete network
        ServerSocket server = new ServerSocket(port);
        for (int i = hosts.length - 1; i >= 0; i--) {
            if (i > rank) {
                // accept a connection from others with a higher rank
                Socket socket = server.accept();
                String src_host = socket.getInetAddress().getHostName();

                // find this source host's rank
                for (int j = 0; j < hosts.length; j++) {
					if (src_host.startsWith(hosts[j])) {
						// j is this source host's rank
						System.out.println("accepted from " + src_host);

						// store this source host j's connection, input stream
						// and object intput/output streams.
						sockets[j] = socket;
						indata[j] = socket.getInputStream();
						inputs[j] = new ObjectInputStream(indata[j]);
						outputs[j] = new ObjectOutputStream(socket.getOutputStream());
					}
				}
            }

            if (i < rank) {
                // establish a connection to others with a lower rank
                sockets[i] = new Socket(hosts[i], port);
                System.out.println("connected to " + hosts[i]);

                // store this destination host j's connection, input stream
                // and object intput/output streams.
                outputs[i] = new ObjectOutputStream(sockets[i].getOutputStream());
                indata[i] = sockets[i].getInputStream();
                inputs[i] = new ObjectInputStream(indata[i]);
            }
        }

        // create a keyboard stream
        BufferedReader keyboard
                = new BufferedReader(new InputStreamReader(System.in));

        // now goes into a chat
        while (true) {
            // read a message from keyboard and broadcast it to all the others.
            if (keyboard.ready()) {
                // since keyboard is ready, read one line.
                String message = keyboard.readLine();
                if (message == null) {
                    // keyboard was closed by "^d"
                    break; // terminate the program
                }
                // broadcast a message to each of the chat members.
				stamps[rank]++;
                for (int i = 0; i < hosts.length; i++) {
                    if (i != rank) {
                        // of course I should not send a message to myself
						outputs[i].writeObject(stamps);
						outputs[i].writeObject(message);
                        outputs[i].flush(); // make sure the message was sent
                    }
                }
            }

            // read a message from each of the chat members
            for (int i = 0; i < hosts.length; i++) {
                // to intentionally create a misordered message deliveray,
                // let's slow down the chat member #2.
                try {
                    if (rank == 2)
                        Thread.currentThread().sleep(5000); // sleep 5 sec.
                } catch (InterruptedException e) {}

                // check if chat member #i has something
                if (i != rank && indata[i].available() > 0) {
                    // read a message from chat member #i and print it out
                    // to the monitor
                    try {
						int[] srcStamps = (int[]) inputs[i].readObject();
                        String message = (String) inputs[i].readObject();

                        if (stampsTooDifferent(i, srcStamps)) {
							msgQueue.add(message);
							stampQueue.add(srcStamps);
							srcQueue.add(i);
						} else {
							System.out.println(hosts[i] + ": " + message);
							stamps[i]++;
						}

                    } catch (ClassNotFoundException e) {}
                }
            }


            for (int i = 0; i < stampQueue.size(); i ++) {
            	int[] pendingStamp = stampQueue.get(i);
            	int pendingSrc = srcQueue.get(i);
            	String pendingMessage = msgQueue.get(i);

            	if (!stampsTooDifferent(pendingSrc, pendingStamp)) {
					stampQueue.remove(i);
					srcQueue.remove(i);
					msgQueue.remove(i);

					stamps[pendingSrc]++;
					System.out.println(hosts[pendingSrc] + ": " + pendingMessage);
				}
			}
        }
    }

    public boolean stampsTooDifferent(int srcRank, int[] srcStamps) {
    	for (int i = 0; i < stamps.length; i++) {
    		if (i == srcRank && srcStamps[i] + 1 != stamps[i])
				return false;
			else {
				if (srcStamps[i] <= stamps[i])
					return false;
			}
		}
    	return true;
	}


    public static void main(String[] args) {

        // verify #args.
        if (args.length < 2) {
            System.err.println("Syntax: java Chat <port> <ip1> <ip2> ...");
            System.exit(-1);
        }

        // retrieve the port
        int port = 0;
        try {
            port = Integer.parseInt(args[0]);
        } catch (NumberFormatException e) {
            e.printStackTrace();
            System.exit(-1);
        }
        if (port <= 5000) {
            System.err.println("port should be 5001 or larger");
            System.exit(-1);
        }

        // retireve my local hostname
        String localhost = null;
        try {
            localhost = InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            e.printStackTrace();
            System.exit(-1);
        }

        // store a list of computing nodes in hosts[] and check my rank
        int rank = -1;
        String[] hosts = new String[args.length - 1];
        for (int i = 0; i < args.length - 1; i++) {
            hosts[i] = args[i + 1];
            if (localhost.startsWith(hosts[i]))
                // found myself in the i-th member of hosts
                rank = i;
        }

        // now start the Chat application
        try {
            new Chat(port, rank, hosts);
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(-1);
        }
    }
}
