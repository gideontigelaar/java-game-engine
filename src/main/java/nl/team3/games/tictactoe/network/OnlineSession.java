package nl.team3.games.tictactoe.network;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class OnlineSession {
    public static final String GAME_TYPE = "Tic-tac-toe";
    public static final String PVP_TAG = "pvp";

    private static final float LOGIN_TIMEOUT = 5f;
    private static final long CHALLENGE_TIMEOUT_NANOS = TimeUnit.SECONDS.toNanos(10);

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
            return hasPvpTag(player) == (this == PLAYER_VS_PLAYER);
        }
    }

    public enum LoginState { PENDING, DONE, FAILED }

    private final ServerConnection connection;
    private final Room room;
    private final String loginName;
    private final Map<String, String> challenges = new LinkedHashMap<>();
    private final Map<String, String> challengesView = Collections.unmodifiableMap(challenges);

    private List<String> players = List.of();
    private String challenged;
    private long challengedAt;

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
        String name = hasPvpTag(player) ? player.substring(0, player.length() - PVP_TAG.length()) : player;
        return name.toUpperCase(Locale.ROOT);
    }

    private static boolean hasPvpTag(String player) {
        return player.length() > PVP_TAG.length()
                && player.regionMatches(true, player.length() - PVP_TAG.length(), PVP_TAG, 0, PVP_TAG.length());
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
                    .filter(this::isRoomMate)
                    .sorted(String.CASE_INSENSITIVE_ORDER)
                    .toList();
            case "SVR GAME CHALLENGE" -> {
                String challenger = message.get("CHALLENGER");
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
        return !isMe(player) && room.contains(player);
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
        challengedAt = System.nanoTime();
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
        return challengesView;
    }

    public String getChallenged() {
        if (challenged != null && System.nanoTime() - challengedAt > CHALLENGE_TIMEOUT_NANOS) {
            challenged = null;
        }
        return challenged;
    }
}