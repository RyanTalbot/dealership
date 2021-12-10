import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

/*
This class create a client and connects to
the server, we use the same port number as the
server, but we also must specify the IP address,
in this case we use local host.

We then allow the client to pass input which is
then sent back to the server, the server can then
act on the command given.
 */

public class Client {
    private static final String SERVER_IP = "127.0.0.1";
    private static final int SERVER_PORT = 8080;

    public static void main(String[] args) throws IOException {
        Socket socket = new Socket(SERVER_IP, SERVER_PORT);
        ServerConnection serverConnection = new ServerConnection(socket);

        BufferedReader keyboard = new BufferedReader(new InputStreamReader(System.in));
        PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

        // Starting client thread
        new Thread(serverConnection).start();

        while (true) {
            System.out.print("> ");
            String command = keyboard.readLine();

            if (command.equalsIgnoreCase("stop")) break;
            out.println(command);

        }
        socket.close();
        System.exit(0);
    }
}

class ServerConnection implements Runnable {
    private Socket server;
    private BufferedReader in;

    public ServerConnection(Socket s) throws IOException {
        this.server = s;
        in = new BufferedReader(new InputStreamReader(server.getInputStream()));
    }

    @Override
    public void run() {
        try {
            while (true) {
                String serverResponse = in.readLine();

                if (serverResponse == null) break;

                System.out.println("[SERVER]: " + serverResponse);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                in.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
