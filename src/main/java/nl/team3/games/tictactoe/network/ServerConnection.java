package nl.team3.games.tictactoe.network;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Consumer;

public class ServerConnection {
    private final String host;
    private final int port;
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private Thread listenThread;
    private boolean isConnected;
    private final ConcurrentLinkedQueue<String> messageQueue;

    public ServerConnection(String host, int port) {
        this.host = host;
        this.port = port;
        this.messageQueue = new ConcurrentLinkedQueue<>();
    }

    public void connect() throws IOException {
        socket = new Socket(host, port);
        out = new PrintWriter(socket.getOutputStream(), true);
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        isConnected = true;

        listenThread = new Thread(this::listen);
        listenThread.setDaemon(true);
        listenThread.start();
    }

    private void listen() {
        try {
            String line;
            while (isConnected && (line = in.readLine()) != null) {
                messageQueue.add(line);
            }
        } catch (IOException e) {
            if (isConnected) {
                messageQueue.add("ERR Connection lost: " + e.getMessage());
            }
        } finally {
            disconnect();
        }
    }

    public void sendCommand(String command) {
        if (out != null && isConnected) {
            out.println(command);
            System.out.println("C: " + command); // For debugging
        }
    }

    // Call during game update loop to process network messages on main thread
    public void update(Consumer<String> messageHandler) {
        while (!messageQueue.isEmpty()) {
            String msg = messageQueue.poll();
            System.out.println("S: " + msg); // For debugging
            messageHandler.accept(msg);
        }
    }

    public void disconnect() {
        if (!isConnected) return;
        isConnected = false;
        try {
            if (out != null) out.println("disconnect");
            if (socket != null) socket.close();
        } catch (IOException e) {
            // Ignore during cleanup
        }
    }

    public boolean isConnected() {
        return isConnected;
    }
}