package nl.team3.games.tictactoe.network;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class OnlineSession {
    public static final String GAME_TYPE = "Tic-tac-toe";
    public static final String PVP_TAG = "-pvp";

    private static final float LOGIN_TIMEOUT = 5f;

    public enum Room {
        PLAYER_VS_PLAYER("PLAYER VS PLAYER"),
        AI_VS_AI("AI VS AI");

        private final String title;

        Room(String title) {
            this.title = title;
        }

        public String getTitle() {
            return title;
        }

        boolean contains(String player) {
            return player.endsWith(PVP_TAG) == (this == PLAYER_VS_PLAYER);
        }
    }

    public enum LoginState { PENDING, DONE, FAILED }

    private final ServerConnection connection;
    private final Room room;
    private final String loginName;
    private final Map<String, String> challenges = new LinkedHashMap<>();

    private List<String> players = List.of();
    private String challenged;

    private boolean loginSent;
    private float loginTime;
    private String loginError = "";

    public OnlineSession(ServerAddress address, Room room, String name) {
        this.room = room;
        this.loginName = name.toLowerCase(Locale.ROOT) + (room == Room.PLAYER_VS_PLAYER ? PVP_TAG : "");
        this.connection = new ServerConnection(address);
        connection.connect();
    }

    public static String displayName(String player) {
        String name = player.endsWith(PVP_TAG) ? player.substring(0, player.length() - PVP_TAG.length()) : player;
        return name.toUpperCase(Locale.ROOT);
    }

    public LoginState updateLogin(float dt) {
        loginTime += dt;
        if (connection.isConnected() && !loginSent) {
            connection.sendCommand("login " + loginName);
            loginSent = true;
        }

        ServerMessage message;
        while ((message = connection.poll()) != null) {
            if (!message.isReplyTo("login")) continue;
            if (message.is("OK")) {
                return LoginState.DONE;
            }
            return fail("Login failed: " + message.data());
        }

        if (!connection.isConnecting() && !connection.isConnected()) {
            return fail(loginSent ? "Connection lost!" : "Can't reach " + connection.getAddress());
        }
        if (loginTime > LOGIN_TIMEOUT) {
            return fail("No answer from the server!");
        }
        return LoginState.PENDING;
    }

    private LoginState fail(String error) {
        loginError = error;
        disconnect();
        return LoginState.FAILED;
    }

    public ServerMessage poll() {
        ServerMessage message = connection.poll();
        if (message == null) {
            return null;
        }

        switch (message.type()) {
            case "SVR PLAYERLIST" -> players = message.items().stream()
                    .map(player -> player.toLowerCase(Locale.ROOT))
                    .filter(this::isRoomMate)
                    .sorted()
                    .toList();
            case "SVR GAME CHALLENGE" -> {
                String challenger = message.get("CHALLENGER").toLowerCase(Locale.ROOT);
                if (isOurGame(message) && isRoomMate(challenger)) {
                    challenges.put(challenger, message.get("CHALLENGENUMBER"));
                }
            }
            case "SVR GAME CHALLENGE CANCELLED" -> challenges.values().remove(message.get("CHALLENGENUMBER"));
            case "SVR GAME MATCH" -> challenged = null;
            case "ERR" -> {
                if (message.isReplyTo("challenge \"")) challenged = null;
            }
            default -> { }
        }
        return message;
    }

    private boolean isRoomMate(String player) {
        return !player.equals(loginName) && room.contains(player);
    }

    public boolean isOurGame(ServerMessage message) {
        return GAME_TYPE.equalsIgnoreCase(message.get("GAMETYPE"));
    }

    public boolean isMe(String player) {
        return player.equalsIgnoreCase(loginName);
    }

    public void requestPlayers() {
        connection.sendCommand("get playerlist");
    }

    public void subscribe() {
        connection.sendCommand("subscribe " + GAME_TYPE);
    }

    public void challenge(String player) {
        connection.sendCommand("challenge \"" + player + "\" \"" + GAME_TYPE + "\"");
        challenged = player;
    }

    public void accept(String challenger) {
        String number = challenges.remove(challenger);
        if (number != null) {
            connection.sendCommand("challenge accept " + number);
        }
    }

    public void move(int cell) {
        connection.sendCommand("move " + cell);
    }

    public void forfeit() {
        connection.sendCommand("forfeit");
    }

    public void disconnect() {
        connection.disconnect();
    }

    public boolean isConnected() {
        return connection.isConnected();
    }

    public Room getRoom() {
        return room;
    }

    public String getDisplayName() {
        return displayName(loginName);
    }

    public ServerAddress getAddress() {
        return connection.getAddress();
    }

    public String getLoginError() {
        return loginError;
    }

    public List<String> getPlayers() {
        return players;
    }

    public Map<String, String> getChallenges() {
        return Collections.unmodifiableMap(challenges);
    }

    public String getChallenged() {
        return challenged;
    }
}