package com.mygdx.game;

import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.World;

public class BodyHelper {

	/**
	 * Creates a circle object with the given position and radius. Resitution
	 * defaults to 0.6.
	 */
	public static Body createCircle(World world, float x, float y,
			float radius, boolean isStatic, BodyData data) {
		CircleShape sd = new CircleShape();
		sd.setRadius(radius);

		FixtureDef fdef = new FixtureDef();
		fdef.shape = sd;
		fdef.density = 1.0f;
		fdef.friction = 0.5f;
		fdef.restitution = 0.6f;

		BodyDef bd = new BodyDef();
		// bd.isBullet = true;
		bd.allowSleep = true;
		bd.position.set(x, y);
		Body body = world.createBody(bd);
		body.setUserData(data);
		body.setLinearDamping(0.05f);
		body.createFixture(fdef);
		if (isStatic) {
			body.setType(BodyDef.BodyType.StaticBody);
		} else {
			body.setType(BodyDef.BodyType.DynamicBody);
		}
		return body;
	}
}
