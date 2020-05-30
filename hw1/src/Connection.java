import java.net.*;
import java.io.*;

public class Connection {
    private Socket socket;
    private String userName;
    private String hostName;

    private InputStream rawIn;
    private DataInputStream in;
    private DataOutputStream out;

    public Connection(Socket _socket) {

        try {
            socket = _socket;
            rawIn = socket.getInputStream();
            in = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());
            userName = in.readUTF();
            hostName = socket.getInetAddress().getHostName();

        } catch(Exception e) {
            e.printStackTrace();
        }

    }
    public String getUserName() {
        return userName;
    }

    public boolean isReadAvailable() {
        try { return rawIn.available() > 0; }
        catch (IOException e) { return false; }
    }

    public Boolean writeMessage(String message) {
        try {
            System.out.println("MY NAME IS: " + userName);
            out.writeUTF(message);
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    public String readMessage() {
        try {
            if (rawIn.available() > 0)

                return userName +  ": " + in.readUTF();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Connection)) {
            return false;
        }
        Connection otherChatConnection = (Connection) obj;
        return userName.equals(otherChatConnection.userName)
                && hostName.equals(otherChatConnection.hostName);
    }
    public void deleteSocket() {
        try {
            socket.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {

    }
}
