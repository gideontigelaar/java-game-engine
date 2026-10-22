package nl.team3.games.tictactoe.network;

public record ServerAddress(String host, int port) {
    public static final int DEFAULT_PORT = 7789;

    public static ServerAddress parse(String text) {
        String value = text.trim();
        int colon = value.lastIndexOf(':');
        if (colon < 0) {
            return value.isEmpty() ? null : new ServerAddress(value, DEFAULT_PORT);
        }

        String host = value.substring(0, colon);
        try {
            int port = Integer.parseInt(value.substring(colon + 1));
            return host.isEmpty() || port < 1 || port > 65535 ? null : new ServerAddress(host, port);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    @Override
    public String toString() {
        return host + ":" + port;
    }
}
