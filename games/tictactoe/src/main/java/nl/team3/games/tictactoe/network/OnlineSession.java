package nl.team3.games.tictactoe.network;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class OnlineSession {
    public static final String GAME_TYPE = "Tic-tac-toe";

    private static final float LOGIN_TIMEOUT = 5f;
    private static final float LOGIN_RETRY_TIME = 0.5f;

    public enum Mode {
        PLAYER_VS_PLAYER("PLAYER VS PLAYER"),
        AI_VS_AI("AI VS AI");

        private final String title;

        Mode(String title) {
            this.title = title;
        }

        public String getTitle() {
            return title;
        }
    }

    public enum LoginState { PENDING, DONE, FAILED }

    private final ServerAddress address;
    private final Mode mode;
    private final String loginName;
    private final Map<String, String> challenges = new LinkedHashMap<>();
    private final Map<String, String> challengesView = Collections.unmodifiableMap(challenges);

    private ServerConnection connection;
    private List<String> players = List.of();
    private String challenged;
    private String gameType = GAME_TYPE;

    private boolean loginSent;
    private boolean relogin;
    private float loginTime;
    private float retryTimer;
    private String loginError = "";

    public OnlineSession(ServerAddress address, Mode mode, String name) {
        this.address = address;
        this.mode = mode;
        this.loginName = name.toLowerCase(Locale.ROOT);
        this.connection = new ServerConnection(address);
        connection.connect();
    }

    public static String displayName(String player) {
        return player.toUpperCase(Locale.ROOT);
    }

    public LoginState updateLogin(float dt) {
        loginTime += dt;
        retryTimer = Math.max(0f, retryTimer - dt);
        if (connection.isConnected() && !loginSent && retryTimer <= 0f) {
            connection.sendCommand("login " + loginName);
            loginSent = true;
        }

        ServerMessage message;
        while ((message = connection.poll()) != null) {
            if (!message.isReplyTo("login")) continue;
            if (message.is("OK")) {
                relogin = false;
                connection.sendCommand("get gamelist");
                return LoginState.DONE;
            }
            if (relogin && loginTime < LOGIN_TIMEOUT) {
                loginSent = false;
                retryTimer = LOGIN_RETRY_TIME;
                continue;
            }
            return fail(loginError(message.data()));
        }

        if (!connection.isConnecting() && !connection.isConnected()) {
            return fail(loginSent ? "Lost connection to the server!" : "Can't reach the server at " + address);
        }
        if (loginTime > LOGIN_TIMEOUT) {
            return fail("The server is not answering!");
        }
        return LoginState.PENDING;
    }

    private static String loginError(String reason) {
        String lower = reason.toLowerCase(Locale.ROOT);
        if (lower.contains("duplicate") || lower.contains("exist") || lower.contains("taken") || lower.contains("in use")) {
            return "Name already taken, try another!";
        }
        return "Name not accepted, try another!";
    }

    private LoginState fail(String error) {
        loginError = error;
        disconnect();
        return LoginState.FAILED;
    }

    public void reconnect() {
        connection.disconnect();
        connection = new ServerConnection(address);
        connection.connect();
        loginSent = false;
        relogin = true;
        loginTime = 0f;
        retryTimer = 0f;
        challenges.clear();
        challenged = null;
    }

    public ServerMessage poll() {
        ServerMessage message = connection.poll();
        if (message == null) {
            return null;
        }

        switch (message.type()) {
            case "SVR GAMELIST" -> message.items().stream()
                    .filter(game -> isGame(game, GAME_TYPE))
                    .findFirst()
                    .ifPresent(game -> gameType = game);
            case "SVR PLAYERLIST" -> {
                players = message.items().stream()
                        .filter(player -> !isMe(player))
                        .sorted(String.CASE_INSENSITIVE_ORDER)
                        .toList();
                if (challenged != null && players.stream().noneMatch(challenged::equalsIgnoreCase)) {
                    challenged = null;
                }
            }
            case "SVR GAME CHALLENGE" -> {
                String challenger = message.get("CHALLENGER");
                if (isOurGame(message) && !isMe(challenger)) {
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

    private static boolean isGame(String game, String other) {
        return normalize(game).equals(normalize(other));
    }

    private static String normalize(String game) {
        return game.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]", "");
    }

    public boolean isOurGame(ServerMessage message) {
        return isGame(message.get("GAMETYPE"), GAME_TYPE);
    }

    public boolean isMe(String player) {
        return player.equalsIgnoreCase(loginName);
    }

    public void requestPlayers() {
        connection.sendCommand("get playerlist");
    }

    public void subscribe() {
        connection.sendCommand("subscribe " + gameType);
    }

    public void challenge(String player) {
        connection.sendCommand("challenge \"" + player + "\" \"" + gameType + "\"");
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

    public Mode getMode() {
        return mode;
    }

    public String getDisplayName() {
        return displayName(loginName);
    }

    public ServerAddress getAddress() {
        return address;
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
        return challenged;
    }
}