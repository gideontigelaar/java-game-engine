package nl.team3.games.tictactoe.scenes;

import nl.team3.engine.assets.AssetManager;
import nl.team3.engine.core.SceneManager;
import nl.team3.engine.graphics.NineSlice;
import nl.team3.engine.graphics.Sprite;
import nl.team3.engine.input.ActionMap;
import nl.team3.engine.input.InputManager;
import nl.team3.engine.ui.Button;
import nl.team3.games.tictactoe.network.OnlineSession;
import nl.team3.games.tictactoe.network.ServerMessage;
import org.joml.Vector4f;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class LobbyScene extends BaseScene {
    private static final float REFRESH_TIME = 2f;
    private static final float NOTICE_TIME = 3f;
    private static final float ACCEPT_TIMEOUT = 5f;

    private static final float PANEL_WIDTH = 560f;
    private static final float PANEL_PADDING = 16f;
    private static final float ROW_HEIGHT = 52f;
    private static final float ROW_GAP = 8f;
    private static final int MIN_ROWS = 2;
    private static final int MAX_ROWS = 6;
    private static final NineSlice WINDOW_SLICE = new NineSlice(8, 2, 2, 2);
    private static final Vector4f ACCEPT_HOVER_COLOR = new Vector4f(1f, 0.8f, 0f, 1f);
    private static final Vector4f PRESSED_COLOR = new Vector4f(1f, 0.5f, 0f, 1f);

    private final OnlineSession session;
    private final boolean searchOnStart;

    private Sprite statusDot;
    private Sprite panel;
    private Button btnFindMatch;
    private Button btnDisconnect;
    private final Button[] rowButtons = new Button[MAX_ROWS];
    private final List<String> entries = new ArrayList<>();

    private boolean searching;
    private float searchTime;
    private boolean accepting;
    private float acceptTime;
    private float refreshTimer;
    private int scroll;

    private String notice = "";
    private Vector4f noticeColor = WHITE;
    private float noticeTimer;

    private int visibleRows = MIN_ROWS;
    private float titleY;
    private float subtitleY;
    private float panelX;
    private float panelY;
    private float panelHeight;
    private float rowsY;
    private float matchmakingY;

    public LobbyScene(InputManager input, ActionMap actions, SceneManager sceneManager, OnlineSession session, boolean searchOnStart) {
        super(input, actions, sceneManager);
        this.session = session;
        this.searchOnStart = searchOnStart;
    }

    @Override
    protected void onInit(AssetManager assets) {
        statusDot = new Sprite(assets.loadTexture("connectedStatus", "/textures/connectedStatus.png"));
        statusDot.setScale(UI_SCALE);
        panel = new Sprite(assets.loadTexture("window", "/textures/window.png"));

        btnFindMatch = createButton("FIND MATCH", BUTTON_WIDTH, this::findMatch);
        btnDisconnect = createButton("DISCONNECT", SMALL_BUTTON_WIDTH, this::leave);
        for (int i = 0; i < MAX_ROWS; i++) {
            int slot = i;
            rowButtons[i] = createButton("", SMALL_BUTTON_WIDTH, ROW_HEIGHT, () -> onRowClicked(slot));
        }

        session.requestPlayers();
        refreshTimer = REFRESH_TIME;
        if (searchOnStart) {
            findMatch();
        }
    }

    private void findMatch() {
        searching = true;
        searchTime = 0f;
        btnFindMatch.setText("SEARCHING...");
        btnFindMatch.setEnabled(false);
        session.subscribe();
    }

    private void leave() {
        session.disconnect();
        transitionTo(new OnlineMenuScene(input, actions, sceneManager, session.getAddress()), visibleButtons());
    }

    private void onRowClicked(int slot) {
        int index = scroll + slot;
        if (index >= entries.size()) return;

        String player = entries.get(index);
        if (session.getChallenges().containsKey(player)) {
            accept(player);
        } else {
            session.challenge(player);
            showNotice("Challenge sent to " + OnlineSession.displayName(player), WHITE);
        }
    }

    private void accept(String player) {
        session.accept(player);
        accepting = true;
        acceptTime = 0f;
    }

    private void showNotice(String message, Vector4f color) {
        notice = message;
        noticeColor = color;
        noticeTimer = NOTICE_TIME;
    }

    @Override
    protected void onUpdate(float dt) {
        noticeTimer = Math.max(0f, noticeTimer - dt);
        if (isTransitioning()) return;

        ServerMessage message;
        while ((message = session.poll()) != null) {
            handleMessage(message);
            if (isTransitioning()) return;
        }
        if (!session.isConnected()) {
            transitionTo(new OnlineMenuScene(input, actions, sceneManager, session.getAddress(), "Connection lost!"), visibleButtons());
            return;
        }

        acceptTime += dt;
        if (accepting && acceptTime > ACCEPT_TIMEOUT) {
            accepting = false;
        }
        refreshTimer -= dt;
        if (refreshTimer <= 0f) {
            session.requestPlayers();
            refreshTimer = REFRESH_TIME;
        }
        if (searching) {
            searchTime += dt;
        }

        updateEntries();
        updateButtons();
    }

    private void handleMessage(ServerMessage message) {
        switch (message.type()) {
            case "SVR GAME MATCH" -> startMatch(message);
            case "SVR GAME CHALLENGE" -> {
                String challenger = message.get("CHALLENGER");
                if (!searching && session.getChallenges().containsKey(challenger)) {
                    showNotice(OnlineSession.displayName(challenger) + " wants to play!", HIGHLIGHT_COLOR);
                }
            }
            case "ERR" -> handleError(message);
            default -> { }
        }
    }

    private void handleError(ServerMessage error) {
        if (error.isReplyTo("challenge accept")) {
            accepting = false;
            showNotice("That challenge has expired", ERROR_COLOR);
        } else if (error.isReplyTo("challenge") && !searching) {
            showNotice("Couldn't send the challenge: " + error.data(), ERROR_COLOR);
        } else if (error.isReplyTo("subscribe")) {
            stopSearching();
            showNotice("Couldn't join matchmaking: " + error.data(), ERROR_COLOR);
        }
    }

    private void startMatch(ServerMessage match) {
        stopSearching();
        accepting = false;
        if (!session.isOurGame(match)) {
            session.forfeit();
            showNotice("Can't play " + match.get("GAMETYPE"), ERROR_COLOR);
            return;
        }
        transitionTo(OnlineMatchScene.create(this, session, match), visibleButtons());
    }

    private void stopSearching() {
        searching = false;
        btnFindMatch.setText("FIND MATCH");
        btnFindMatch.setEnabled(true);
    }

    private void updateEntries() {
        Map<String, String> challenges = session.getChallenges();
        entries.clear();
        entries.addAll(challenges.keySet());
        for (String player : session.getPlayers()) {
            if (!challenges.containsKey(player)) {
                entries.add(player);
            }
        }

        int maxScroll = Math.max(0, entries.size() - visibleRows);
        if (isMouseOverPanel()) {
            scroll -= (int) Math.signum(input.getScrollY());
        }
        scroll = Math.max(0, Math.min(maxScroll, scroll));
    }

    private boolean isMouseOverPanel() {
        return mousePosition.x >= panelX && mousePosition.x <= panelX + PANEL_WIDTH
                && mousePosition.y >= panelY && mousePosition.y <= panelY + panelHeight;
    }

    private void updateButtons() {
        for (int slot = 0; slot < visibleRows; slot++) {
            int index = scroll + slot;
            if (index >= entries.size()) break;

            String player = entries.get(index);
            Button button = rowButtons[slot];
            if (session.getChallenges().containsKey(player)) {
                button.setText("ACCEPT");
                button.setColors(HIGHLIGHT_COLOR, ACCEPT_HOVER_COLOR, PRESSED_COLOR);
                button.setEnabled(!accepting && !searching);
            } else {
                boolean sent = player.equals(session.getChallenged());
                button.setText(sent ? "SENT" : "CHALLENGE");
                button.setColors(WHITE, HOVER_COLOR, PRESSED_COLOR);
                button.setEnabled(!sent && !accepting && !searching);
            }
            button.update(input);
            if (isTransitioning()) return;
        }

        btnFindMatch.update(input);
        btnDisconnect.update(input);
        if (!isTransitioning() && isBackPressed()) {
            leave();
        }
    }

    private Button[] visibleButtons() {
        List<Button> buttons = new ArrayList<>();
        for (int slot = 0; slot < visibleRows && scroll + slot < entries.size(); slot++) {
            buttons.add(rowButtons[slot]);
        }
        buttons.add(btnFindMatch);
        buttons.add(btnDisconnect);
        return buttons.toArray(new Button[0]);
    }

    private String matchmakingText() {
        if (accepting) {
            return "Starting match...";
        }
        if (noticeTimer > 0f) {
            return notice;
        }
        if (!searching) {
            return "Press FIND MATCH or challenge a player";
        }

        int seconds = (int) searchTime;
        String time = String.format(" %d:%02d", seconds / 60, seconds % 60);
        boolean alone = session.getPlayers().isEmpty();
        return (alone ? "Waiting for another player..." : "Looking for an opponent...") + time;
    }

    private Vector4f matchmakingColor() {
        if (accepting || (searching && noticeTimer <= 0f)) {
            return HIGHLIGHT_COLOR;
        }
        return noticeTimer > 0f ? noticeColor : DIM_WHITE;
    }

    @Override
    protected void onRender() {
        renderTitle();
        drawCenteredText(smallFont, "Logged in as " + session.getDisplayName() + " on " + session.getAddress(), subtitleY, WHITE);
        renderPanel();
        drawCenteredText(smallFont, matchmakingText(), matchmakingY, matchmakingColor());

        btnFindMatch.render(spriteRenderer, textRenderer);
        btnDisconnect.render(spriteRenderer, textRenderer);
    }

    private void renderTitle() {
        String title = session.getMode().getTitle();
        float dotSize = statusDot.getTexture().getWidth() * UI_SCALE;
        float width = dotSize + UI_SPACING + gameFont.getTextWidth(title);
        float x = Math.round((currentWidth - width) / 2f);

        statusDot.setPosition(x + dotSize / 2f, titleY + gameFont.getCapHeight() / 2f);
        spriteRenderer.draw(statusDot);
        drawText(gameFont, title, x + dotSize + UI_SPACING, titleY, WHITE);
    }

    private void renderPanel() {
        panel.setOrigin(0f, 0f);
        spriteRenderer.drawNineSlice(panel, panelX, panelY, PANEL_WIDTH, panelHeight, WINDOW_SLICE, UI_SCALE);

        float titleX = panelX + PANEL_PADDING;
        float titleTop = panelY + UI_SCALE;
        drawText(smallFont, "PLAYERS ONLINE (" + entries.size() + ")", titleX, titleTop, DARK_TEXT_COLOR);
        if (entries.size() > visibleRows) {
            String range = (scroll + 1) + "-" + (scroll + visibleRows);
            drawText(smallFont, range, panelX + PANEL_WIDTH - PANEL_PADDING - smallFont.getTextWidth(range), titleTop, DARK_TEXT_COLOR);
        }

        if (entries.isEmpty()) {
            drawCenteredText(smallFont, "No other players online", rowsY + (ROW_HEIGHT - smallFont.getCapHeight()) / 2f, DIM_WHITE);
            return;
        }

        float lineOffset = (ROW_HEIGHT - smallFont.getCapHeight()) / 2f;
        for (int slot = 0; slot < visibleRows && scroll + slot < entries.size(); slot++) {
            String player = entries.get(scroll + slot);
            float rowY = rowsY + slot * (ROW_HEIGHT + ROW_GAP);
            Vector4f color = session.getChallenges().containsKey(player) ? HIGHLIGHT_COLOR : WHITE;
            drawText(smallFont, OnlineSession.displayName(player), titleX, rowY + lineOffset, color);
            rowButtons[slot].render(spriteRenderer, textRenderer);
        }
    }

    @Override
    protected void onResize(int width, int height) {
        float titleHeight = gameFont.getCapHeight();
        float lineHeight = smallFont.getCapHeight();
        float panelChrome = (WINDOW_SLICE.top() + WINDOW_SLICE.bottom()) * UI_SCALE + 2 * PANEL_PADDING;
        float fixedHeight = titleHeight + UI_SPACING + lineHeight + 2 * UI_SPACING
                + panelChrome + 2 * UI_SPACING + lineHeight + 2 * UI_SPACING + BUTTON_HEIGHT;

        float rowSpace = height - fixedHeight - 4 * UI_SPACING + ROW_GAP;
        visibleRows = Math.max(MIN_ROWS, Math.min(MAX_ROWS, (int) (rowSpace / (ROW_HEIGHT + ROW_GAP))));
        panelHeight = panelChrome + visibleRows * (ROW_HEIGHT + ROW_GAP) - ROW_GAP;

        float contentHeight = fixedHeight + panelHeight - panelChrome;
        float y = Math.max(0, Math.round((height - contentHeight) / 2f));

        titleY = y;
        y += titleHeight + UI_SPACING;
        subtitleY = y;
        y += lineHeight + 2 * UI_SPACING;

        panelX = Math.round((width - PANEL_WIDTH) / 2f);
        panelY = y;
        rowsY = panelY + WINDOW_SLICE.top() * UI_SCALE + PANEL_PADDING;
        float buttonX = panelX + PANEL_WIDTH - WINDOW_SLICE.right() * UI_SCALE - PANEL_PADDING / 2f - SMALL_BUTTON_WIDTH;
        for (int slot = 0; slot < MAX_ROWS; slot++) {
            rowButtons[slot].setPosition(buttonX, rowsY + slot * (ROW_HEIGHT + ROW_GAP));
        }
        y += panelHeight + 2 * UI_SPACING;

        matchmakingY = y;
        y += lineHeight + 2 * UI_SPACING;

        float buttonsWidth = BUTTON_WIDTH + UI_SPACING + SMALL_BUTTON_WIDTH;
        float buttonsX = Math.round((width - buttonsWidth) / 2f);
        btnFindMatch.setPosition(buttonsX, y);
        btnDisconnect.setPosition(buttonsX + BUTTON_WIDTH + UI_SPACING, y);
    }
}