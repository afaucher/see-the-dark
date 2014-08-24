package com.mygdx.game;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.WindowedMean;
import com.badlogic.gdx.utils.TimeUtils;
import com.mygdx.game.field.Field;
import com.mygdx.game.ship.Ship;
import com.mygdx.game.ship.components.Component;

public class MyGdxGame extends ApplicationAdapter implements InputProcessor {
	private OrthographicCamera camera = null;
	private Field field = null;
	ShapeRenderer renderer = null;
	TwoAxisControl playerOneControl = new TwoAxisControl();
	Ship playerOneShip = null;
	WindowedMean physicsMean = new WindowedMean(10);
	WindowedMean renderMean = new WindowedMean(10);
	long startTime = TimeUtils.nanoTime();

	private static final int HUD_PADDING = 5;
	private static final int HUD_HEIGHT = 15;

	@Override
	public void create() {
		camera = new OrthographicCamera();
		int x = Gdx.app.getGraphics().getWidth();
		int y = Gdx.app.getGraphics().getHeight();
		camera.setToOrtho(false, x, y);

		field = new Field();
		renderer = new ShapeRenderer(500);

		Gdx.input.setInputProcessor(this);

		field.resetLevel(playerOneControl);

		playerOneShip = field.getShips().get(0);
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
		field.tick((long) (Gdx.graphics.getDeltaTime() * 3000), 4);
		physicsMean
				.addValue((TimeUtils.nanoTime() - startPhysics) / 1000000000.0f);

		Color background = ColorPalate.BACKGROUND;
		Gdx.gl.glClearColor(background.r, background.g, background.b,
				background.a);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

		long startRender = TimeUtils.nanoTime();

		Ship ship = playerOneShip;

		// Camera
		Vector2 cameraPosition = ship.getPosition();
		camera.position.set(cameraPosition, 0);
		camera.update();
		renderer.identity();
		renderer.setProjectionMatrix(camera.combined);

		for (RenderLayer layer : RenderLayer.values()) {

			field.render(renderer, layer);
		}

		// HUD
		// TODO: COLOR is ugly!!!
		Matrix4 matrix = new Matrix4();
		int width = Gdx.graphics.getWidth();
		int height = Gdx.graphics.getHeight();
		matrix.setToOrtho2D(0, 0, width, height);
		renderer.setProjectionMatrix(matrix);

		Rectangle hudSpaceAvailable = new Rectangle(HUD_PADDING, HUD_PADDING,
				200, HUD_HEIGHT);
		for (Component c : ship.getComponents()) {
			if (!c.requiresHud())
				continue;
			Rectangle hudSpaceTaken = c.drawHud(renderer, hudSpaceAvailable);
			hudSpaceAvailable.x += HUD_PADDING + hudSpaceTaken.width;
		}

		renderMean
				.addValue((TimeUtils.nanoTime() - startRender) / 1000000000.0f);

		if (TimeUtils.nanoTime() - startTime > 1000000000) {
			Gdx.app.log("Profile: ",
					"fps: " + Gdx.graphics.getFramesPerSecond()
							+ " FPS, physics: " + physicsMean.getMean() * 1000
							+ ", rendering: " + renderMean.getMean() * 1000
							+ "");
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
			pressNum(0);
			break;
		case Input.Keys.NUM_2:
			pressNum(1);
			break;
		}

		return false;
	}

	@Override
	public boolean keyUp(int keycode) {
		switch (keycode) {
		case Input.Keys.W:
		case Input.Keys.S:
			playerOneControl.setY(0);
			break;
		case Input.Keys.A:
		case Input.Keys.D:
			playerOneControl.setX(0);
			break;
		}

		return false;
	}

	@Override
	public boolean keyTyped(char character) {
		return false;
	}

	@Override
	public boolean mouseMoved(int screenX, int screenY) {

		Vector3 touchWorldCoordinate = new Vector3(screenX, screenY, 0);
		Vector3 target3 = camera.unproject(touchWorldCoordinate);
		Vector2 target2 = new Vector2(target3.x, target3.y);

		playerOneShip.aimWeapons(target2);

		return true;
	}

	@Override
	public boolean scrolled(int amount) {
		return false;
	}
}
