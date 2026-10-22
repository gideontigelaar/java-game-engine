package nl.team3.games.tictactoe.scenes;

import nl.team3.engine.core.SceneManager;
import nl.team3.engine.input.ActionMap;
import nl.team3.engine.input.InputManager;
import nl.team3.engine.ui.Button;
import nl.team3.games.tictactoe.network.OnlineSession;
import nl.team3.games.tictactoe.network.ServerAddress;

import java.util.ArrayList;
import java.util.List;

public class OnlineMenuScene extends MenuScene {
    private final ServerAddress address;
    private final List<Button> roomButtons = new ArrayList<>();
    private OnlineSession login;

    public OnlineMenuScene(InputManager input, ActionMap actions, SceneManager sceneManager, ServerAddress address) {
        this(input, actions, sceneManager, address, "");
    }

    public OnlineMenuScene(InputManager input, ActionMap actions, SceneManager sceneManager, ServerAddress address, String status) {
        super(input, actions, sceneManager, status);
        this.address = address;
    }

    @Override
    protected void initMenu() {
        for (OnlineSession.Room room : OnlineSession.Room.values()) {
            roomButtons.add(addButton(room.getTitle(), () -> join(room)));
        }
        addSmallButton("BACK", this::onBack);
    }

    private void join(OnlineSession.Room room) {
        login = new OnlineSession(address, room, playerName);
        roomButtons.forEach(button -> button.setEnabled(false));
        showProgress("Connecting to " + address + "...");
    }

    private void stopLogin() {
        login = null;
        roomButtons.forEach(button -> button.setEnabled(true));
    }

    @Override
    protected void updateMenu(float dt) {
        if (login == null) return;

        switch (login.updateLogin(dt)) {
            case DONE -> open(new LobbyScene(input, actions, sceneManager, login, false));
            case FAILED -> {
                showStatus(login.getLoginError());
                stopLogin();
            }
            case PENDING -> { }
        }
    }

    @Override
    protected void onBack() {
        if (login != null) {
            login.disconnect();
            stopLogin();
            clearStatus();
        } else {
            open(new MainMenuScene(input, actions, sceneManager));
        }
    }

    @Override
    protected String getDetail() {
        return address.toString();
    }
}