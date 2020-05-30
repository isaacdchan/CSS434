import java.net.*;
import java.io.*;
import java.util.*;

public class ChatServer {
    private ServerSocket svr;
    private Socket socket;
    private static Set<Connection> connections;


    public ChatServer(int port) {
        connections = new HashSet<>();

        try {
            // initialize server socket
            svr = new ServerSocket(port);
            // set non blocking timeout
            svr.setSoTimeout(500);

            while (true) {
                try {
                    socket = svr.accept();
                } catch (SocketTimeoutException e) {
                    socket = null;
                }

                // if a user connects
                if (socket != null) {
                    System.out.println("Adding connection");
                    Connection connection = new Connection(socket);
                    // add them to the connection list
                    connections.add(connection);
                }

                for (Connection connection : connections) {
                    // if a connection wants to broadcast a message
                    if (connection.isReadAvailable()) {
                        String message = connection.readMessage();
                        // if the message broadcast was null, an error occurred
                        if (message == null) {
                            System.out.println("Deleting connection1");
                            connection.deleteSocket();
                            connections.remove(connection);
                        } else
                            broadcast(message, connection);
                    }
                }

                System.out.println("----------");
            }

        } catch (Exception e) {
//            e.printStackTrace();
        }
    }

    // broadcasts message to every other connection in connection list
    private static void broadcast(String message, Connection sender) {
        System.out.println("Broadcasting Message From: " + sender.getUserName());

        boolean success = false;

        for (Connection connection : connections) {
            System.out.println("Attempting to send message to " + connection.getUserName());

            if (!connection.equals(sender)) {
                System.out.println("\tRecipient != Sender. Sending");

                success = connection.writeMessage(message);
                // if the message was unable to be broadcast to a user
                // an error occurred or they disconnected. either way, delete them
                if (!success) {
                    System.out.println("Deleting connection2");
                    connection.deleteSocket();
                    connections.remove(connection);
                }
            }
        }

    }

    public static void main(String args[]) {
        if (args.length != 1) {
            System.err.println("Syntax: java ServerClient <port>");
            System.exit(1);
        }

        int port = Integer.parseInt(args[0]);
        new ChatServer(port);
    }
}
