import java.net.*;
import java.io.*;

public class ChatClient {
    private Socket socket;
    private InputStream rawIn;
    private DataInputStream in;
    private DataOutputStream out;
    private BufferedReader stdin;

    public ChatClient(String name, String server, int port) {

		try {
			socket = new Socket(server, port);
			rawIn = socket.getInputStream();

			in = new DataInputStream(rawIn);
			out = new DataOutputStream(socket.getOutputStream());
			stdin = new BufferedReader(new InputStreamReader(System.in));

			out.writeUTF(name);
			while(true) {
				if (stdin.ready()) {
					String str = stdin.readLine();
					if (str == null)
						break;
					out.writeUTF(str);
				}

				if (rawIn.available() > 0) {
					String str = in.readUTF();
					System.out.println(str);
				}
			}
			socket.close();
		} catch (Exception e) {
//			e.printStackTrace();
		}
    }

	public static void main(String args[]) {
		if (args.length != 3) {
			System.err.println("Syntax: java ChatClient <your name> " +
					"<server ip name> <port>");
			System.exit(1);
		}

		int port = Integer.parseInt(args[2]);
		new ChatClient(args[0], args[1], port);
    }
}
