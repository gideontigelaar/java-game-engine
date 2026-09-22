package nl.team3.engine.core.InputManager;

import java.util.*;

public class ActionMap {

    private final InputManager input;
    private final Map<String, List<Integer>> bindings = new HashMap<>();

    public ActionMap(InputManager input) {
        this.input = input;
    }

    //bind keys to action
    public void bind(String action, Integer... keys) {
        bindings.put(action, Arrays.asList(keys));
    }

    //add key to binding without overwriting
    public void addBinding(String action, int key) {
        bindings.computeIfAbsent(action, a -> new ArrayList<>()).add(key);
    }

    public boolean isActionDown(String action) {
        List<Integer> keys = bindings.get(action);
        if (keys == null) return false;
        for (int key : keys) {
            if (input.isKeyDown(key)) return true;
        }
        return false;
    }

    public boolean isActionPressed(String action) {
        List<Integer> keys = bindings.get(action);
        if (keys == null) return false;
        for (int key : keys) {
            if (input.isKeyPressed(key)) return true;
        }
        return false;
    }

    public boolean isActionReleased(String action) {
        List<Integer> keys = bindings.get(action);
        if (keys == null) return false;
        for (int key : keys) {
            if (input.isKeyReleased(key)) return true;
        }
        return false;
    }

}
