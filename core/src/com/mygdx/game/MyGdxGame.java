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
import com.badlogic.gdx.math.WindowedMean;
import com.badlogic.gdx.utils.TimeUtils;
import com.mygdx.game.ship.Component;

public class MyGdxGame extends ApplicationAdapter implements InputProcessor {
	private OrthographicCamera camera;
	private Field field;
	ShapeRenderer renderer;
	TwoAxisControl playerOneControl = new TwoAxisControl();
	WindowedMean physicsMean = new WindowedMean(10);
	WindowedMean renderMean = new WindowedMean(10);
	long startTime = TimeUtils.nanoTime();

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

		Ship ship = field.getShips().get(0);
		

		
		//Camera
		Vector2 cameraPosition = ship.getPosition();
		camera.position.set(cameraPosition, 0);
		camera.update();
		renderer.identity();
		renderer.setProjectionMatrix(camera.combined);

		field.render(renderer);
		

		//HUD
		//TODO: COLOR is ugly, drawing is in world coordinates and under game
		Matrix4 matrix = new Matrix4();
		int width = Gdx.graphics.getWidth();
		int height = Gdx.graphics.getHeight();
		matrix.setToOrtho2D(0, 0, width, height);
		renderer.setProjectionMatrix(matrix);
		//renderer.begin(ShapeType.Filled);
		//renderer.setColor(ColorPalate.HUD_BG);
		//renderer.circle(30, 30, 23);
		//renderer.setColor(ColorPalate.ACTIVE_HUD);
		
		Rectangle hudSpaceAvailable = new Rectangle(5,5,200,30);
		for (Component c : ship.getComponents()) {
			if (!c.requiresHud()) continue;
			Rectangle hudSpaceTaken = c.drawHud(renderer, hudSpaceAvailable);
			hudSpaceAvailable.x += 5 + hudSpaceTaken.width;
		}
		
		
		
		//float fuelGuageDegrees = (ship.getFuel() / ship.getFuelCapacity()) * 360;
		//float heatGuageDegrees = ship.g
		//renderer.arc(30, 30, 15, 0, fuelGuageDegrees);
		//renderer.arc(40, 0, 20, 0, heatGuageDegrees);
		//renderer.arc(80, 0, 20, 0, hullGuageDegrees);
		
		//renderer.end();
		
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
		Ship ship = field.getShips().get(0);
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
		return false;
	}

	@Override
	public boolean scrolled(int amount) {
		return false;
	}
}
