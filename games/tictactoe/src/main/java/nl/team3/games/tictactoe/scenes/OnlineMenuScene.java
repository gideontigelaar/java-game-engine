package nl.team3.games.tictactoe.scenes;

import nl.team3.engine.assets.AssetManager;
import nl.team3.engine.core.EngineContext;
import nl.team3.engine.graphics.Sprite;
import nl.team3.engine.graphics.Texture;
import nl.team3.engine.ui.Button;
import nl.team3.games.tictactoe.network.OnlineSession;
import nl.team3.games.tictactoe.network.ServerAddress;

import java.util.ArrayList;
import java.util.List;

public class OnlineMenuScene extends MenuScene {
    private final ServerAddress address;
    private final List<Button> modeButtons = new ArrayList<>();
    private OnlineSession login;

    private Sprite statusDot;
    private Texture connectingTexture;
    private Texture disconnectedTexture;

    public OnlineMenuScene(EngineContext context, ServerAddress address) {
        this(context, address, "");
    }

    public OnlineMenuScene(EngineContext context, ServerAddress address, String status) {
        super(context, status);
        this.address = address;
    }

    @Override
    protected void initMenu(AssetManager assets) {
        connectingTexture = assets.loadTexture("connectingStatus", "/games/tictactoe/textures/connectingStatus.png");
        disconnectedTexture = assets.loadTexture("disconnectedStatus", "/games/tictactoe/textures/disconnectedStatus.png");
        statusDot = new Sprite(disconnectedTexture);
        statusDot.setScale(UI_SCALE);

        for (OnlineSession.Mode mode : OnlineSession.Mode.values()) {
            modeButtons.add(addButton(mode.getTitle(), () -> join(mode)));
        }
        addSmallButton("BACK", this::onBack);
    }

    private void join(OnlineSession.Mode mode) {
        login = new OnlineSession(address, mode, playerName);
        modeButtons.forEach(button -> button.setEnabled(false));
        showProgress("Connecting to " + address + "...");
    }

    private void stopLogin() {
        login = null;
        modeButtons.forEach(button -> button.setEnabled(true));
    }

    @Override
    protected void updateMenu(float dt) {
        if (login == null) return;

        switch (login.updateLogin(dt)) {
            case DONE -> open(new LobbyScene(context, login, false));
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
            open(new MainMenuScene(context));
        }
    }

    @Override
    protected String getDetail() {
        return address.toString();
    }

    @Override
    protected Sprite getDetailIcon() {
        statusDot.setTexture(login != null ? connectingTexture : disconnectedTexture);
        return statusDot;
    }
}