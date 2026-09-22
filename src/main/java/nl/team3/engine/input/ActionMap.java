package nl.team3.engine.input;

import java.util.*;

public class ActionMap {
    private final InputManager input;
    private final Map<String, List<Integer>> bindings = new HashMap<>();

    public ActionMap(InputManager input) {
        this.input = input;
    }

    // Bind keys to action, overwrite existing binding
    public void bind(String action, Integer... keys) {
        bindings.put(action, Arrays.asList(keys));
    }

    // Add key to binding without overwriting
    public void addBinding(String action, int key) {
        bindings.computeIfAbsent(action, a -> new ArrayList<>()).add(key);
    }

    // Remove action and all bindings
    public void unbind(String action) {
        bindings.remove(action);
    }

    public void clear() {
        bindings.clear();
    }

    public boolean isActionDown(String action) {
        List<Integer> keys = bindings.get(action);
        if (keys == null) {
            return false;
        }

        for (int key : keys) {
            if (input.isKeyDown(key)) {
                return true;
            }
        }
        return false;
    }

    public boolean isActionPressed(String action) {
        List<Integer> keys = bindings.get(action);
        if (keys == null) {
            return false;
        }

        for (int key : keys) {
            if (input.isKeyPressed(key)) {
                return true;
            }
        }
        return false;
    }

    public boolean isActionReleased(String action) {
        List<Integer> keys = bindings.get(action);
        if (keys == null) {
            return false;
        }

        for (int key : keys) {
            if (input.isKeyReleased(key)) {
                return true;
            }
        }
        return false;
    }
}