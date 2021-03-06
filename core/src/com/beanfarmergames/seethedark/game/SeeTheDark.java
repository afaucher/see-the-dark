package com.beanfarmergames.seethedark.game;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
//import com.badlogic.gdx.ai.steer.behaviors.Wander;
//import com.badlogic.gdx.ai.steer.limiters.LinearAccelerationLimiter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.WindowedMean;
import com.badlogic.gdx.utils.TimeUtils;
import com.beanfarmergames.seethedark.components.Component;
import com.beanfarmergames.seethedark.entities.NavPoint;
import com.beanfarmergames.seethedark.game.field.Field;
import com.beanfarmergames.seethedark.mode.GameMode;
import com.beanfarmergames.seethedark.mode.RaceGameMode;
import com.beanfarmergames.seethedark.mode.State;
import com.beanfarmergames.seethedark.ship.Ship;
import com.beanfarmergames.seethedark.state.GameState;
import com.beanfarmergames.seethedark.state.PausableGameState;
import com.beanfarmergames.seethedark.style.ColorPalate;
import com.beanfarmergames.seethedark.style.FontPalate;
import com.beanfarmergames.seethedark.util.DebbugingParameters;

public class SeeTheDark extends ApplicationAdapter implements InputProcessor {
    private OrthographicCamera camera = null;
    private Field field = null;
    private ShapeRenderer renderer = null;
    private TwoAxisControl playerOneControl = null;
    private Ship playerOneShip = null;
    private WindowedMean physicsMean = new WindowedMean(10);
    private WindowedMean renderMean = new WindowedMean(10);
    private long startTime = TimeUtils.nanoTime();
    private Vector3 touchScreenCoordinate = null;
    private SpriteBatch spriteBatch = null;

    private GameState gameState = null;
    private GameMode gameMode = null;

    public GameMode getGameMode() {
        return gameMode;
    }

    private static final int HUD_PADDING = 5;
    private static final int HUD_HEIGHT = 15;

    private List<Player> players = new ArrayList<Player>();

    private static final CharSequence[] hudText = { "1", "2", "3", "4", "5", "6", "7", "8", "9", };

    @Override
    public void create() {
        gameState = new PausableGameState();

        camera = new OrthographicCamera();
        int x = Gdx.app.getGraphics().getWidth();
        int y = Gdx.app.getGraphics().getHeight();
        camera.setToOrtho(false, x, y);

        field = new Field();
        renderer = new ShapeRenderer(500);

        Gdx.input.setInputProcessor(this);

        spriteBatch = new SpriteBatch();

        FontPalate.loadFonts();

        // Setup initial game

        field.resetLevel();

        // Setup Players

        Player playerOne = new Player("One");
        players.add(playerOne);

        Player playerTwo = new Player("Two");
        players.add(playerTwo);

        playerOneControl = playerOne.getSteetingControl();

        Vector2 spwanOne = new Vector2(0, 0);
        playerOneShip = new Ship(field, playerOneControl, spwanOne);

        playerOne.attachShip(playerOneShip);

        Vector2 spwanTwo = new Vector2(100, 100);
        Ship playerTwoShip = new Ship(field, playerTwo.getSteetingControl(), spwanTwo);

        playerTwo.attachShip(playerTwoShip);

        gameMode = new RaceGameMode(this, field, new ArrayList<NavPoint>(field.getNavPoints()));

        gameMode.setGameState(State.Playing);

    }

    public Collection<Player> getPlayers() {
        return players;
    }

    public Player getPlayerForShip(Ship s) {
        if (s == null) {
            return null;
        }
        for (Player player : players) {
            if (s == player.getShip()) {
                return player;
            }
        }
        return null;
    }

    @Override
    public boolean touchDown(int x, int y, int pointer, int button) {
        return false;
    }

    @Override
    public boolean touchUp(int x, int y, int pointer, int button) {
        return false;
    }

    @Override
    public boolean touchDragged(int x, int y, int pointer) {
        return false;
    }

    @Override
    public void render() {

        long startPhysics = TimeUtils.nanoTime();

        float tickTimeSeconds = Gdx.graphics.getDeltaTime();
        long tickTimeMiliseconds = (long) (tickTimeSeconds * 1000);

        if (gameState.isSimulationRunning()) {
            // TODO: Why o why is there a 3 here?
            field.tick(tickTimeMiliseconds * 3, 4);
            physicsMean.addValue((TimeUtils.nanoTime() - startPhysics) / 1000000000.0f);
        }

        Color background = ColorPalate.BACKGROUND;
        Gdx.gl.glClearColor(background.r, background.g, background.b, background.a);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        long startRender = TimeUtils.nanoTime();

        Ship ship = playerOneShip;

        // Camera
        Vector2 cameraPosition = ship.getPosition();

        camera.position.set(cameraPosition, 0);
        camera.update();

        renderer.identity();
        renderer.setProjectionMatrix(camera.combined);

        // TODO: Decouple aiming and rendering
        aimShip();

        for (RenderLayer layer : RenderLayer.values()) {
            field.render(renderer, layer, players.get(0));
        }

        // AXIS
        if (DebbugingParameters.DRAW_ORIGIN) {
            renderer.begin(ShapeType.Line);
            renderer.setColor(ColorPalate.AXIS);
            renderer.rect(0, 0, 10, 10);
            renderer.end();
        }

        // FPS Counter
        spriteBatch.begin();
        FontPalate.HUD_FONT.draw(spriteBatch, "FPS: " + Gdx.graphics.getFramesPerSecond(), 100, 100);
        spriteBatch.end();

        // HUD
        // TODO: COLOR is ugly!!!
        Matrix4 matrix = new Matrix4();
        int width = Gdx.graphics.getWidth();
        int height = Gdx.graphics.getHeight();
        matrix.setToOrtho2D(0, 0, width, height);
        renderer.setProjectionMatrix(matrix);

        int actionKey = 0;

        Rectangle hudSpaceAvailable = new Rectangle(HUD_PADDING, HUD_PADDING, 200, HUD_HEIGHT);
        for (Component c : ship.getComponents()) {
            if (!c.requiresHud())
                continue;
            Rectangle hudSpaceTaken = c.drawHud(renderer, hudSpaceAvailable);

            // Draw toggle key to interact with hud element
            if (c.requiresInput()) {
                spriteBatch.begin();
                FontPalate.HUD_FONT.draw(spriteBatch, hudText[actionKey], hudSpaceAvailable.x + HUD_PADDING,
                        hudSpaceAvailable.y + HUD_PADDING);
                spriteBatch.end();
                actionKey++;
            }

            hudSpaceAvailable.x += HUD_PADDING + hudSpaceTaken.width;
        }

        renderMean.addValue((TimeUtils.nanoTime() - startRender) / 1000000000.0f);

        if (TimeUtils.nanoTime() - startTime > 1000000000) {
            Gdx.app.log("Profile: ",
                    "fps: " + Gdx.graphics.getFramesPerSecond() + " FPS, physics: " + physicsMean.getMean() * 1000
                            + ", rendering: " + renderMean.getMean() * 1000 + "");
            startTime = TimeUtils.nanoTime();
        }
    }

    private void pressNum(int pressedNum) {
        Ship ship = playerOneShip;
        int componentNum = 0;
        for (Component c : ship.getComponents()) {
            if (c.requiresInput()) {
                if (pressedNum == componentNum) {
                    c.keyPressed();
                    return;
                }

                componentNum++;
            }
        }
    }

    @Override
    public boolean keyDown(int keycode) {

        switch (keycode) {
        case Input.Keys.P:
            gameState.togglePause();
            break;
        }

        if (!gameState.isSimulationRunning()) {
            // Remainder of keys interact with the simulation
            // TODO: Key presses started before or during pause will misbehave
            return false;
        }

        switch (keycode) {
        case Input.Keys.W:
            playerOneControl.setY(1);
            break;
        case Input.Keys.S:
            playerOneControl.setY(-1);
            break;
        case Input.Keys.A:
            playerOneControl.setX(-1);
            break;
        case Input.Keys.D:
            playerOneControl.setX(1);
            break;
        case Input.Keys.NUM_1:
        case Input.Keys.NUM_2:
        case Input.Keys.NUM_3:
        case Input.Keys.NUM_4:
        case Input.Keys.NUM_5:
        case Input.Keys.NUM_6:
        case Input.Keys.NUM_7:
        case Input.Keys.NUM_8:
        case Input.Keys.NUM_9:
            pressNum(keycode - Input.Keys.NUM_1);
            break;
        case Input.Keys.F1:
            DebbugingParameters.DRAW_ALL_SHIPS = !DebbugingParameters.DRAW_ALL_SHIPS;
            DebbugingParameters.DRAW_ORIGIN = !DebbugingParameters.DRAW_ORIGIN;
            break;
        case Input.Keys.F2:
            getGameMode().setGameState(State.Playing);
            break;
        }

        return false;
    }

    @Override
    public boolean keyUp(int keycode) {

        if (!gameState.isSimulationRunning()) {
            // Remainder of keys interact with the simulation
            return false;
        }

        switch (keycode) {
        case Input.Keys.W:
        case Input.Keys.S:
            playerOneControl.setY(0);
            break;
        case Input.Keys.A:
        case Input.Keys.D:
            playerOneControl.setX(0);
            break;
        case Input.Keys.SPACE:
            playerOneShip.fire();
            break;
        }

        return false;
    }

    @Override
    public boolean keyTyped(char character) {
        return false;
    }

    private void aimShip() {
        if (touchScreenCoordinate == null) {
            return;
        }

        Vector3 target3 = camera.unproject(touchScreenCoordinate.cpy());
        Vector2 target2 = new Vector2(target3.x, target3.y);

        // Gets reapplied even when the mouse doesn't move
        playerOneShip.aimWeapons(target2);
    }

    @Override
    public boolean mouseMoved(int screenX, int screenY) {

        this.touchScreenCoordinate = new Vector3(screenX, screenY, 0);

        // Vector2 cameraPosition = playerOneShip.getPosition();

        // camera.position.set(cameraPosition, 0);
        // camera.update();

        // aimShip();

        return true;
    }

    @Override
    public boolean scrolled(int amount) {

        float zoom_adjustment = (10 + amount) / 10.0f;
        camera.zoom = camera.zoom * zoom_adjustment;

        return true;
    }
}
