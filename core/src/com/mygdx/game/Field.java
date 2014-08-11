package com.mygdx.game;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;

public class Field {
	World world;
	
	long gameTime;
	
	Entity entity;
	
	public void resetLevel(TwoAxisControl playerOne) {
		Vector2 gravity = new Vector2(0.0f, 0.0f);
		boolean doSleep = true;
		world = new World(gravity, doSleep);
		
		gameTime = 0;
		
		entity = new Entity(world, playerOne);
	}
	
	/** Called to advance the game's state by the specified number of milliseconds. iters is the number of times to call the Box2D
	 * World.step method; more iterations produce better accuracy. After updating physics, processes element collisions, calls
	 * tick() on every FieldElement, and performs scheduled actions. */
	void tick (long msecs, int iters) {
		float dt = (msecs / 1000.0f) / iters;

		for (int i = 0; i < iters; i++) {
			//clearBallContacts();
			world.step(dt, 10, 10);
			//processBallContacts();
		}

		gameTime += msecs;
		//processElementTicks();
	}
	
	public void render(ShapeRenderer renderer) {
		entity.render(renderer);
	}
}
