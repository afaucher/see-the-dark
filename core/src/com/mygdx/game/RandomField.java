package com.mygdx.game;

import java.util.Random;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.physics.box2d.World;

public class RandomField implements FieldLayout {
	Random RAND = new Random();
	

	private static final float MAX_TREE_RADIUS = 30.0f;
	private static final float MIN_TREE_RADIUS = 5.0f;
	
	private static final BodyData treeBodyData = new BodyData() {

		@Override
		public BodyType getType() {
			return BodyType.PROP;
		}

		@Override
		public Color getMaterialColor() {
			return ColorPalate.TREE;
		}
	};

	@Override
	public void populateField(World world) {
		//Create World Objects
		
		//Specifically bouncy trees
		for (int i = 0; i < 50; i++) {
			float x = RAND.nextFloat() * 1000;
			float y = RAND.nextFloat() * 1000;
			float radius = RAND.nextFloat()
					* (MAX_TREE_RADIUS - MIN_TREE_RADIUS) + MIN_TREE_RADIUS;
			BodyHelper.createCircle(world, x, y, radius, false, treeBodyData);
		}
	}
}
