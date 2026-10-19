package nl.team3.games.tictactoe.network;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.concurrent.ConcurrentLinkedQueue;

public class ServerConnection {
    private static final int CONNECT_TIMEOUT_MS = 3000;

    private final String host;
    private final int port;
    private final ConcurrentLinkedQueue<String> messageQueue = new ConcurrentLinkedQueue<>();

    private volatile Socket socket;
    private volatile PrintWriter out;
    private volatile boolean isConnected;
    private volatile boolean isClosed;

    public ServerConnection(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public void connect() {
        Thread listenThread = new Thread(this::listen, "server-connection");
        listenThread.setDaemon(true);
        listenThread.start();
    }

    private void listen() {
        try (Socket s = new Socket()) {
            socket = s;
            s.connect(new InetSocketAddress(host, port), CONNECT_TIMEOUT_MS);
            if (isClosed) return;
            BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream()));
            out = new PrintWriter(s.getOutputStream(), true);
            isConnected = true;

            String line;
            while ((line = in.readLine()) != null) {
                messageQueue.add(line);
            }
        } catch (IOException e) {
            if (!isClosed) {
                System.out.println("Connection closed: " + e.getMessage());
            }
        } finally {
            isConnected = false;
            isClosed = true;
        }
    }

    public void sendCommand(String command) {
        PrintWriter writer = out;
        if (writer != null && isConnected) {
            writer.println(command);
            System.out.println("C: " + command); // For debugging
        }
    }

    // Next message from the server, null when there is none
    public ServerMessage poll() {
        String line = messageQueue.poll();
        if (line == null) {
            return null;
        }
        System.out.println("S: " + line); // For debugging
        return ServerMessage.parse(line);
    }

    public void disconnect() {
        if (isClosed) return;
        sendCommand("disconnect");
        isConnected = false;
        isClosed = true;
        try {
            Socket s = socket;
            if (s != null) s.close();
        } catch (IOException e) {
            // Ignore during cleanup
        }
    }

    public boolean isConnecting() {
        return !isConnected && !isClosed;
    }

    public boolean isConnected() {
        return isConnected;
    }
}