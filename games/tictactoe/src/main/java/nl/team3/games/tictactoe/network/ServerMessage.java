package nl.team3.games.tictactoe.network;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public record ServerMessage(String type, String data, String command, long time) {
    private static final Pattern FIELD = Pattern.compile("(\\w+): \"([^\"]*)\"");
    private static final Pattern ITEM = Pattern.compile("\"([^\"]*)\"");

    public static ServerMessage parse(String line) {
        String trimmed = line.trim();
        long time = System.nanoTime();
        if (trimmed.startsWith("ERR")) {
            return new ServerMessage("ERR", trimmed.substring(3).trim(), null, time);
        }

        int start = dataStart(trimmed);
        if (start < 0) {
            return new ServerMessage(trimmed, "", null, time);
        }
        return new ServerMessage(trimmed.substring(0, start).trim(), trimmed.substring(start), null, time);
    }

    private static int dataStart(String line) {
        int map = line.indexOf('{');
        int list = line.indexOf('[');
        if (map < 0 || list < 0) {
            return Math.max(map, list);
        }
        return Math.min(map, list);
    }

    ServerMessage answering(String command) {
        return new ServerMessage(type, data, command, time);
    }

    public float getAge() {
        return (System.nanoTime() - time) / 1_000_000_000f;
    }

    public boolean is(String type) {
        return this.type.equals(type);
    }

    public boolean isReplyTo(String commandPrefix) {
        return command != null && command.regionMatches(true, 0, commandPrefix, 0, commandPrefix.length());
    }

    public String get(String key) {
        Matcher matcher = FIELD.matcher(data);
        while (matcher.find()) {
            if (matcher.group(1).equals(key)) {
                return matcher.group(2);
            }
        }
        return "";
    }

    public List<String> items() {
        List<String> items = new ArrayList<>();
        Matcher matcher = ITEM.matcher(data);
        while (matcher.find()) {
            items.add(matcher.group(1));
        }
        return items;
    }
}