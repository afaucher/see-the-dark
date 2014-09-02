package com.mygdx.game.field;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.physics.box2d.World;
import com.mygdx.game.AbstractBodyData;
import com.mygdx.game.BodyHelper;
import com.mygdx.game.ColorPalate;
import com.mygdx.game.FieldUpdateCallback;
import com.mygdx.game.entities.Star;

public class RandomField implements FieldLayout {
	private static Random RAND = new Random();

	private static final float MAX_TREE_RADIUS = 50.0f;
	private static final float MIN_TREE_RADIUS = 10.0f;

	private static final float MAX_BALL_RADIUS = 25.0f;
	private static final float MIN_BALL_RADIUS = 5.0f;

	private static final AbstractBodyData bouncyBallBodyData = new AbstractBodyData(
			false) {

		@Override
		public Color getMaterialColor() {
			return ColorPalate.BALL;
		}
	};

	private static final AbstractBodyData staticTreeBodyData = new AbstractBodyData(
			false) {

		@Override
		public Color getMaterialColor() {
			return ColorPalate.TREE;
		}
	};

	@Override
	public List<FieldUpdateCallback> populateField(World world) {
		// Create World Objects

		List<FieldUpdateCallback> callbacks = new ArrayList<FieldUpdateCallback>();

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

		Star star = new Star(this, world);
		callbacks.add(star);

		return callbacks;

	}
}
