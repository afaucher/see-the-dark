package com.mygdx.game;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.WindowedMean;
import com.badlogic.gdx.utils.TimeUtils;

public class MyGdxGame extends ApplicationAdapter implements InputProcessor {
	//SpriteBatch batch;
	//Texture img;
	private OrthographicCamera camera;
	private Field field;
	ShapeRenderer renderer;
	TwoAxisControl playerOneControl = new TwoAxisControl();
	WindowedMean physicsMean = new WindowedMean(10);
	WindowedMean renderMean = new WindowedMean(10);
	long startTime = TimeUtils.nanoTime();

	@Override
	public void create() {
		//batch = new SpriteBatch();
		//img = new Texture("badlogic.jpg");
		camera = new OrthographicCamera();
		camera.setToOrtho(false, 800, 800);

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
		field.tick((long)(Gdx.graphics.getDeltaTime() * 3000), 4);
		physicsMean.addValue((TimeUtils.nanoTime() - startPhysics) / 1000000000.0f);

		Color background = ColorPalate.BACKGROUND;
		Gdx.gl.glClearColor(background.r,background.g,background.b,background.a);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		
		long startRender = TimeUtils.nanoTime();
		
		Vector2 cameraPosition = field.entity.getPosition();
		camera.position.set(cameraPosition,0);
		camera.update();
		renderer.setProjectionMatrix(camera.combined);

		field.render(renderer);
		renderMean.addValue((TimeUtils.nanoTime() - startRender) / 1000000000.0f);
		
		if (TimeUtils.nanoTime() - startTime > 1000000000) {
			Gdx.app.log("Profile: ", "fps: " + Gdx.graphics.getFramesPerSecond() + " FPS, physics: " + physicsMean.getMean() * 1000
				+ ", rendering: " + renderMean.getMean() * 1000 + "");
			startTime = TimeUtils.nanoTime();
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
