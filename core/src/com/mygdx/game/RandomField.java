package com.mygdx.game;

import java.util.Random;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.physics.box2d.World;

public class RandomField implements FieldLayout {
	Random RAND = new Random();

	private static final float MAX_TREE_RADIUS = 50.0f;
	private static final float MIN_TREE_RADIUS = 10.0f;

	private static final float MAX_BALL_RADIUS = 25.0f;
	private static final float MIN_BALL_RADIUS = 5.0f;

	private static final BodyData bouncyBallBodyData = new BodyData() {

		@Override
		public Color getMaterialColor() {
			return ColorPalate.BALL;
		}
	};

	private static final BodyData staticTreeBodyData = new BodyData() {

		@Override
		public Color getMaterialColor() {
			return ColorPalate.TREE;
		}
	};

	@Override
	public void populateField(World world) {
		// Create World Objects

		// Specifically bouncy balls
		for (int i = 0; i < 25; i++) {
			float x = RAND.nextFloat() * 1000;
			float y = RAND.nextFloat() * 1000;
			float radius = RAND.nextFloat()
					* (MAX_BALL_RADIUS - MIN_BALL_RADIUS) + MIN_BALL_RADIUS;
			BodyHelper.createCircle(world, x, y, radius, false,
					bouncyBallBodyData);
		}
		// Trees
		for (int i = 0; i < 25; i++) {
			float x = RAND.nextFloat() * 1000;
			float y = RAND.nextFloat() * 1000;
			float radius = RAND.nextFloat()
					* (MAX_TREE_RADIUS - MIN_TREE_RADIUS) + MIN_TREE_RADIUS;
			BodyHelper.createCircle(world, x, y, radius, true,
					staticTreeBodyData);
		}
	}
}
