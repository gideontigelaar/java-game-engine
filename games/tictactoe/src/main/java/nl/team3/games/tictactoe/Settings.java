package nl.team3.games.tictactoe;

import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

public final class Settings {
    public static final String DEFAULT_SERVER = "127.0.0.1:7789";

    private static final Preferences PREFS = Preferences.userNodeForPackage(Settings.class);
    private static final String PLAYER_NAME = "playerName";
    private static final String SERVER_ADDRESS = "serverAddress";

    private Settings() {
    }

    public static String getPlayerName() {
        return PREFS.get(PLAYER_NAME, "");
    }

    public static String getServerAddress() {
        return PREFS.get(SERVER_ADDRESS, DEFAULT_SERVER);
    }

    public static void save(String playerName, String serverAddress) {
        PREFS.put(PLAYER_NAME, playerName.trim());
        PREFS.put(SERVER_ADDRESS, serverAddress.trim());
        try {
            PREFS.flush();
        } catch (BackingStoreException e) {
            System.err.println("Could not save settings: " + e.getMessage());
        }
    }
}
