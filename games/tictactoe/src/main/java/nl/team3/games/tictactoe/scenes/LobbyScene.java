package nl.team3.games.tictactoe.scenes;

import nl.team3.engine.assets.AssetManager;
import nl.team3.engine.core.EngineContext;
import nl.team3.engine.graphics.NineSlice;
import nl.team3.engine.graphics.Sprite;
import nl.team3.engine.graphics.Texture;
import nl.team3.engine.input.MouseButtons;
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
    private static final float INVITE_PULSE_TIME = 1f;

    private static final float PANEL_WIDTH = 560f;
    private static final float PANEL_PADDING = 16f;
    private static final float ROW_HEIGHT = 52f;
    private static final float ROW_GAP = 8f;
    private static final float SCROLLBAR_WIDTH = 3 * UI_SCALE;
    private static final float MIN_THUMB_HEIGHT = 6 * UI_SCALE;
    private static final int MIN_ROWS = 2;
    private static final int MAX_ROWS = 6;
    private static final int NOTICE_LINES = 2;
    private static final NineSlice WINDOW_SLICE = new NineSlice(8, 2, 2, 2);
    private static final Vector4f ACCEPT_HOVER_COLOR = new Vector4f(1f, 0.8f, 0f, 1f);
    private static final Vector4f PRESSED_COLOR = new Vector4f(1f, 0.5f, 0f, 1f);
    private static final Vector4f SCROLL_COLOR = new Vector4f(0.408f, 0.682f, 0.831f, 1f);

    private final OnlineSession session;
    private final boolean searchOnStart;

    private Sprite statusDot;
    private Texture connectedTexture;
    private Texture connectingTexture;
    private Texture disconnectedTexture;
    private Sprite panel;
    private Sprite pixel;
    private Button btnFindMatch;
    private Button btnDisconnect;
    private final Button[] rowButtons = new Button[MAX_ROWS];
    private final List<String> entries = new ArrayList<>();

    private boolean searching;
    private float searchTime;
    private boolean cancelling;
    private boolean accepting;
    private float acceptTime;
    private float refreshTimer;
    private int scroll;
    private boolean draggingThumb;
    private float dragOffset;
    private float time;

    private String notice = "";
    private Vector4f noticeColor = WHITE;
    private float noticeTimer;

    private int visibleRows = MIN_ROWS;
    private List<String> subtitleLines = List.of();
    private float titleY;
    private float subtitleY;
    private float panelX;
    private float panelY;
    private float panelWidth;
    private float panelHeight;
    private float rowsY;
    private float trackX;
    private float rowButtonX;
    private float matchmakingY;

    public LobbyScene(EngineContext context, OnlineSession session, boolean searchOnStart) {
        super(context);
        this.session = session;
        this.searchOnStart = searchOnStart;
    }

    @Override
    protected void onInit(AssetManager assets) {
        connectedTexture = assets.loadTexture("connectedStatus", "/games/tictactoe/textures/connectedStatus.png");
        connectingTexture = assets.loadTexture("connectingStatus", "/games/tictactoe/textures/connectingStatus.png");
        disconnectedTexture = assets.loadTexture("disconnectedStatus", "/games/tictactoe/textures/disconnectedStatus.png");
        statusDot = new Sprite(connectedTexture);
        statusDot.setScale(UI_SCALE);
        panel = new Sprite(assets.loadTexture("window", "/games/tictactoe/textures/window.png"));
        pixel = new Sprite(assets.getTexture(AssetManager.WHITE_TEXTURE));
        pixel.setOrigin(0f, 0f);

        btnFindMatch = createButton("FIND MATCH", BUTTON_WIDTH, this::toggleSearch);
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

    private void toggleSearch() {
        if (searching) {
            cancelSearch();
        } else {
            findMatch();
        }
    }

    private void findMatch() {
        searching = true;
        searchTime = 0f;
        btnFindMatch.setText("CANCEL SEARCH");
        session.subscribe();
    }

    private void cancelSearch() {
        stopSearching();
        cancelling = true;
        accepting = false;
        session.reconnect();
    }

    private void leave() {
        session.disconnect();
        transitionTo(new OnlineMenuScene(context, session.getAddress()));
    }

    private void onRowClicked(int slot) {
        int index = scroll + slot;
        if (index >= entries.size()) return;

        String player = entries.get(index);
        if (session.getChallenges().containsKey(player)) {
            accept(player);
        } else {
            session.challenge(player);
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
        time += dt;
        if (isLeaving()) return;

        if (cancelling) {
            updateCancel(dt);
        } else {
            ServerMessage message;
            while ((message = session.poll()) != null) {
                handleMessage(message);
                if (isLeaving()) return;
            }
            if (!session.isConnected()) {
                transitionTo(new OnlineMenuScene(context, session.getAddress(), "Lost connection to the server!"));
                return;
            }

            refreshTimer -= dt;
            if (refreshTimer <= 0f) {
                session.requestPlayers();
                refreshTimer = REFRESH_TIME;
            }
        }
        if (isLeaving()) return;

        acceptTime += dt;
        if (accepting && acceptTime > ACCEPT_TIMEOUT) {
            accepting = false;
        }
        if (searching) {
            searchTime += dt;
        }

        if (!input.isButtonDown(MouseButtons.LEFT)) {
            updateEntries();
        }
        updateScroll();
        updateButtons();
    }

    private void updateCancel(float dt) {
        switch (session.updateLogin(dt)) {
            case DONE -> {
                cancelling = false;
                session.requestPlayers();
                refreshTimer = REFRESH_TIME;
                showNotice("Stopped searching for a match", WHITE);
            }
            case FAILED -> transitionTo(new OnlineMenuScene(context, session.getAddress(), session.getLoginError()));
            case PENDING -> { }
        }
    }

    private void handleMessage(ServerMessage message) {
        switch (message.type()) {
            case "SVR GAME MATCH" -> startMatch(message);
            case "SVR GAME CHALLENGE" -> {
                if (session.getChallenges().containsKey(message.get("CHALLENGER"))) {
                    scroll = 0;
                }
            }
            case "ERR" -> handleError(message);
            default -> { }
        }
    }

    private void handleError(ServerMessage error) {
        if (error.isReplyTo("challenge accept")) {
            accepting = false;
            showNotice("That invite has expired", ERROR_COLOR);
        } else if (error.isReplyTo("challenge")) {
            showNotice("Couldn't challenge that player!", ERROR_COLOR);
        } else if (error.isReplyTo("subscribe")) {
            stopSearching();
            showNotice("Couldn't start searching, try again!", ERROR_COLOR);
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
        transitionTo(OnlineMatchScene.create(context, session, match));
    }

    private void stopSearching() {
        searching = false;
        btnFindMatch.setText("FIND MATCH");
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
    }

    private void updateScroll() {
        int maxScroll = Math.max(0, entries.size() - visibleRows);
        if (isMouseOverPanel()) {
            scroll -= (int) Math.signum(input.getScrollY());
        }

        if (maxScroll > 0) {
            float thumbHeight = getThumbHeight();
            float thumbY = getThumbY(maxScroll, thumbHeight);
            boolean overTrack = mousePosition.x >= trackX - UI_SCALE && mousePosition.x <= trackX + SCROLLBAR_WIDTH + UI_SCALE
                    && mousePosition.y >= rowsY && mousePosition.y <= rowsY + getTrackHeight();
            if (input.isButtonPressed(MouseButtons.LEFT) && overTrack) {
                draggingThumb = true;
                boolean onThumb = mousePosition.y >= thumbY && mousePosition.y <= thumbY + thumbHeight;
                dragOffset = onThumb ? mousePosition.y - thumbY : thumbHeight / 2f;
            }
            float travel = getTrackHeight() - thumbHeight;
            if (draggingThumb && travel > 0f) {
                scroll = Math.round((mousePosition.y - dragOffset - rowsY) / travel * maxScroll);
            }
        }
        if (!input.isButtonDown(MouseButtons.LEFT)) {
            draggingThumb = false;
        }
        scroll = Math.max(0, Math.min(maxScroll, scroll));
    }

    private boolean isMouseOverPanel() {
        return mousePosition.x >= panelX && mousePosition.x <= panelX + panelWidth
                && mousePosition.y >= panelY && mousePosition.y <= panelY + panelHeight;
    }

    private float getTrackHeight() {
        return visibleRows * (ROW_HEIGHT + ROW_GAP) - ROW_GAP;
    }

    private float getThumbHeight() {
        float height = getTrackHeight() * visibleRows / entries.size();
        return Math.max(MIN_THUMB_HEIGHT, Math.round(height / UI_SCALE) * UI_SCALE);
    }

    private float getThumbY(int maxScroll, float thumbHeight) {
        float y = (getTrackHeight() - thumbHeight) * scroll / maxScroll;
        return rowsY + Math.round(y / UI_SCALE) * UI_SCALE;
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
                button.setEnabled(!accepting && !cancelling);
            } else {
                boolean sent = player.equals(session.getChallenged());
                button.setText(sent ? "SENT" : "CHALLENGE");
                button.setColors(WHITE, HOVER_COLOR, PRESSED_COLOR);
                button.setEnabled(!sent && !accepting && !cancelling);
            }
            if (!draggingThumb) {
                button.update(input);
            }
            if (isLeaving()) return;
        }

        btnFindMatch.setEnabled(!accepting && !cancelling);
        btnFindMatch.update(input);
        btnDisconnect.update(input);
        if (!isLeaving() && isBackPressed()) {
            leave();
        }
    }

    @Override
    protected Button[] getTransitionButtons() {
        return new Button[]{btnFindMatch, btnDisconnect};
    }

    private String matchmakingText() {
        if (accepting) {
            return "Starting the match...";
        }
        if (cancelling) {
            return "Stopping the search...";
        }
        if (noticeTimer > 0f) {
            return notice;
        }

        Map<String, String> challenges = session.getChallenges();
        if (challenges.size() == 1) {
            String challenger = challenges.keySet().iterator().next();
            return OnlineSession.displayName(challenger) + " wants to play! Press ACCEPT to start";
        }
        if (challenges.size() > 1) {
            return challenges.size() + " players want to play! Press ACCEPT to start";
        }
        if (searching) {
            int seconds = (int) searchTime;
            String time = String.format(" %d:%02d", seconds / 60, seconds % 60);
            boolean alone = session.getPlayers().isEmpty();
            return (alone ? "Waiting for another player..." : "Looking for an opponent...") + time;
        }
        if (session.getChallenged() != null) {
            return "Waiting for " + OnlineSession.displayName(session.getChallenged()) + " to accept...";
        }
        return "Press FIND MATCH or challenge a player";
    }

    private Vector4f matchmakingColor() {
        if (accepting || cancelling) {
            return WHITE;
        }
        if (noticeTimer > 0f) {
            return noticeColor;
        }
        if (searching || !session.getChallenges().isEmpty()) {
            return HIGHLIGHT_COLOR;
        }
        return session.getChallenged() != null ? WHITE : DIM_WHITE;
    }

    @Override
    protected void onRender() {
        renderTitle();
        float y = subtitleY;
        for (String line : subtitleLines) {
            drawCenteredText(smallFont, line, y, WHITE);
            y += smallFont.getCapHeight() + LINE_GAP;
        }
        renderPanel();
        drawWrappedText(smallFont, matchmakingText(), matchmakingY, getMaxTextWidth(), matchmakingColor());

        btnFindMatch.render(spriteRenderer, textRenderer);
        btnDisconnect.render(spriteRenderer, textRenderer);
    }

    private void renderTitle() {
        String title = session.getMode().getTitle();
        float dotSize = statusDot.getTexture().getWidth() * UI_SCALE;
        float width = dotSize + UI_SPACING + gameFont.getTextWidth(title);
        float x = Math.round((currentWidth - width) / 2f);

        statusDot.setTexture(cancelling ? connectingTexture : session.isConnected() ? connectedTexture : disconnectedTexture);
        statusDot.setPosition(x + dotSize / 2f + getContentOffset(), titleY + gameFont.getCapHeight() / 2f);
        spriteRenderer.draw(statusDot);
        drawText(gameFont, title, x + dotSize + UI_SPACING, titleY, WHITE);
    }

    private void renderPanel() {
        float offset = getContentOffset();
        panel.setOrigin(0f, 0f);
        spriteRenderer.drawNineSlice(panel, panelX + offset, panelY, panelWidth, panelHeight, WINDOW_SLICE, UI_SCALE);

        float titleX = panelX + PANEL_PADDING;
        float titleTop = panelY + UI_SCALE;
        drawText(smallFont, "PLAYERS ONLINE (" + entries.size() + ")", titleX, titleTop, WHITE);
        if (entries.size() > visibleRows) {
            String range = (scroll + 1) + "-" + (scroll + visibleRows);
            drawText(smallFont, range, panelX + panelWidth - PANEL_PADDING - smallFont.getTextWidth(range), titleTop, WHITE);
        }

        if (entries.isEmpty()) {
            String empty = cancelling ? "Reconnecting..." : "No other players online";
            drawCenteredText(smallFont, empty, rowsY + (ROW_HEIGHT - smallFont.getCapHeight()) / 2f, DIM_WHITE);
            return;
        }

        float lineOffset = (ROW_HEIGHT - smallFont.getCapHeight()) / 2f;
        float nameWidth = rowButtonX - titleX - UI_SPACING / 2f;
        for (int slot = 0; slot < visibleRows && scroll + slot < entries.size(); slot++) {
            String player = entries.get(scroll + slot);
            float rowY = rowsY + slot * (ROW_HEIGHT + ROW_GAP);
            boolean invite = session.getChallenges().containsKey(player);
            if (invite) {
                float pulse = 0.5f + 0.5f * (float) Math.sin(2 * Math.PI * time / INVITE_PULSE_TIME);
                float left = panelX + WINDOW_SLICE.left() * UI_SCALE;
                drawRect(left + offset, rowY - ROW_GAP / 2f, trackX - UI_SCALE - left, ROW_HEIGHT + ROW_GAP,
                        HIGHLIGHT_COLOR, 0.15f + 0.2f * pulse);
            }
            String name = fitText(smallFont, OnlineSession.displayName(player), nameWidth);
            drawText(smallFont, name, titleX, rowY + lineOffset, invite ? HIGHLIGHT_COLOR : WHITE);

            Button button = rowButtons[slot];
            button.setPosition(rowButtonX + offset, rowY);
            button.render(spriteRenderer, textRenderer);
        }
        renderScrollbar(offset);
    }

    private void renderScrollbar(float offset) {
        int maxScroll = entries.size() - visibleRows;
        if (maxScroll <= 0) return;

        float x = trackX + offset;
        float thumbHeight = getThumbHeight();
        float thumbY = getThumbY(maxScroll, thumbHeight);
        drawRect(x + UI_SCALE, rowsY, UI_SCALE, getTrackHeight(), DARK_TEXT_COLOR, 0.4f);
        drawRect(x, thumbY, SCROLLBAR_WIDTH, thumbHeight, DARK_TEXT_COLOR, 1f);
        drawRect(x + UI_SCALE, thumbY + UI_SCALE, UI_SCALE, thumbHeight - 2 * UI_SCALE, SCROLL_COLOR, 1f);
    }

    private void drawRect(float x, float y, float width, float height, Vector4f color, float alpha) {
        pixel.setPosition(x, y);
        pixel.setScale(width, height);
        pixel.setTint(color.x, color.y, color.z, alpha);
        spriteRenderer.draw(pixel);
    }

    @Override
    protected void onResize(int width, int height) {
        float titleHeight = gameFont.getCapHeight();
        float lineHeight = smallFont.getCapHeight();
        subtitleLines = wrapText(smallFont, "Logged in as " + session.getDisplayName() + " on " + session.getAddress(), getMaxTextWidth());
        float subtitleHeight = subtitleLines.size() * (lineHeight + LINE_GAP) - LINE_GAP;
        float noticeHeight = NOTICE_LINES * (lineHeight + LINE_GAP) - LINE_GAP;
        float panelChrome = (WINDOW_SLICE.top() + WINDOW_SLICE.bottom()) * UI_SCALE + 2 * PANEL_PADDING;
        float fixedHeight = titleHeight + UI_SPACING + subtitleHeight + 2 * UI_SPACING
                + panelChrome + 2 * UI_SPACING + noticeHeight + 2 * UI_SPACING + BUTTON_HEIGHT;

        float rowSpace = height - fixedHeight - 2 * UI_SPACING + ROW_GAP;
        visibleRows = Math.max(MIN_ROWS, Math.min(MAX_ROWS, (int) (rowSpace / (ROW_HEIGHT + ROW_GAP))));
        panelHeight = panelChrome + visibleRows * (ROW_HEIGHT + ROW_GAP) - ROW_GAP;

        float contentHeight = fixedHeight + panelHeight - panelChrome;
        float y = Math.max(0, Math.round((height - contentHeight) / 2f));

        titleY = y;
        y += titleHeight + UI_SPACING;
        subtitleY = y;
        y += subtitleHeight + 2 * UI_SPACING;

        panelWidth = Math.min(PANEL_WIDTH, width - 2 * UI_SPACING);
        panelX = Math.round((width - panelWidth) / 2f);
        panelY = y;
        rowsY = panelY + WINDOW_SLICE.top() * UI_SCALE + PANEL_PADDING;
        trackX = panelX + panelWidth - WINDOW_SLICE.right() * UI_SCALE - PANEL_PADDING / 2f - SCROLLBAR_WIDTH;
        rowButtonX = trackX - PANEL_PADDING / 2f - SMALL_BUTTON_WIDTH;
        for (int slot = 0; slot < MAX_ROWS; slot++) {
            rowButtons[slot].setPosition(rowButtonX, rowsY + slot * (ROW_HEIGHT + ROW_GAP));
        }
        y += panelHeight + 2 * UI_SPACING;

        matchmakingY = y;
        y += noticeHeight + 2 * UI_SPACING;

        float buttonsWidth = BUTTON_WIDTH + UI_SPACING + SMALL_BUTTON_WIDTH;
        float buttonsX = Math.round((width - buttonsWidth) / 2f);
        btnFindMatch.setPosition(buttonsX, y);
        btnDisconnect.setPosition(buttonsX + BUTTON_WIDTH + UI_SPACING, y);
    }
}