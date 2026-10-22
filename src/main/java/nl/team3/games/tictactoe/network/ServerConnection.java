package nl.team3.games.tictactoe.network;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class ServerConnection {
    private static final int CONNECT_TIMEOUT_MS = 3000;

    private final ServerAddress address;
    private final Queue<ServerMessage> messages = new ConcurrentLinkedQueue<>();
    private final Queue<String> awaitingReply = new ConcurrentLinkedQueue<>();

    private volatile Socket socket;
    private volatile PrintWriter out;
    private volatile boolean connected;
    private volatile boolean closed;

    public ServerConnection(ServerAddress address) {
        this.address = address;
    }

    public void connect() {
        Thread listenThread = new Thread(this::listen, "server-connection");
        listenThread.setDaemon(true);
        listenThread.start();
    }

    private void listen() {
        try (Socket s = new Socket()) {
            socket = s;
            s.connect(new InetSocketAddress(address.host(), address.port()), CONNECT_TIMEOUT_MS);
            s.setTcpNoDelay(true);
            if (closed) return;

            BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream(), StandardCharsets.UTF_8));
            out = new PrintWriter(s.getOutputStream(), true, StandardCharsets.UTF_8);
            connected = true;

            String line;
            while ((line = in.readLine()) != null) {
                if (line.isBlank()) continue;

                ServerMessage message = ServerMessage.parse(line);
                if (message.is("OK") || message.is("ERR")) {
                    message = message.answering(awaitingReply.poll());
                }
                messages.add(message);
            }
        } catch (IOException e) {
            // Refused, timed out or dropped
        } finally {
            connected = false;
            closed = true;
        }
    }

    public void sendCommand(String command) {
        PrintWriter writer = out;
        if (writer != null && connected) {
            awaitingReply.add(command);
            writer.print(command + "\n");
            writer.flush();
        }
    }

    // Next message from the server, null when there is none
    public ServerMessage poll() {
        return messages.poll();
    }

    public void disconnect() {
        if (closed) return;

        PrintWriter writer = out;
        if (writer != null && connected) {
            writer.print("disconnect\n");
            writer.flush();
        }
        connected = false;
        closed = true;
        try {
            Socket s = socket;
            if (s != null) s.close();
        } catch (IOException e) {
            // Ignore during cleanup
        }
    }

    public ServerAddress getAddress() {
        return address;
    }

    public boolean isConnecting() {
        return !connected && !closed;
    }

    public boolean isConnected() {
        return connected;
    }
}