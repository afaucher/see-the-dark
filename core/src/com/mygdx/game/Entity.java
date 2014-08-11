package com.mygdx.game;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.RayCastCallback;
import com.badlogic.gdx.physics.box2d.World;

public class Entity implements RayCastCallback {

	private Body body;
	private float angle = 0;
	private List<Vector2> hits = new ArrayList<Vector2>();
	private TwoAxisControl playerOne;
	private static final float SCAN_LENGTH = 300.0f;
	private static final float BODY_RADIUS = 10.0f;
	private static final float HIT_RADIUS = 5.0f;
	private static final float TURN_SPEED_RADIANS_PER_FRAME = 0.05f;
	private static final float SPEED_PER_SECOND = 100.0f;
	private static final float MAX_TREE_RADIUS = 30.0f;
	private static final float MIN_TREE_RADIUS = 5.0f;
	private static final float SCAN_HALF_RADIUS = 1.0f;
	private static final float SCAN_RADIUS = SCAN_HALF_RADIUS * 2.0f;
	private static final int SCAN_SLICES = 40;

	Random RAND = new Random();

	/**
	 * Creates a circle object with the given position and radius. Resitution
	 * defaults to 0.6.
	 */
	public static Body createCircle(World world, float x, float y,
			float radius, boolean isStatic) {
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
		body.createFixture(fdef);
		if (isStatic) {
			body.setType(BodyDef.BodyType.StaticBody);
		} else {
			body.setType(BodyDef.BodyType.DynamicBody);
		}
		return body;
	}

	public Entity(World world, TwoAxisControl playerOne) {
		this.playerOne = playerOne;
		body = createCircle(world, 0, 0, BODY_RADIUS, false);
		body.setBullet(true);

		//Create World Objects
		for (int i = 0; i < 50; i++) {
			float x = RAND.nextFloat() * 1000;
			float y = RAND.nextFloat() * 1000;
			float radius = RAND.nextFloat()
					* (MAX_TREE_RADIUS - MIN_TREE_RADIUS) + MIN_TREE_RADIUS;
			createCircle(world, x, y, radius, false);
		}
	}

	private void updateMovements() {
		angle -= playerOne.getX() * TURN_SPEED_RADIANS_PER_FRAME;
		float speed = playerOne.getY() * SPEED_PER_SECOND;
		float vX = (float) Math.cos(angle) * speed;
		float vY = (float) Math.sin(angle) * speed;

		body.setLinearVelocity(vX, vY);
	}

	public float getRotation() {
		return angle;
	}

	public Vector2 getPosition() {
		return body.getPosition();
	}

	public void render(ShapeRenderer renderer) {
		updateMovements();

		CircleShape cs = (CircleShape) body.getFixtureList().get(0).getShape();
		if (cs == null)
			return;

		float x = body.getPosition().x;
		float y = body.getPosition().y;

		World world = body.getWorld();
		Vector2 rayStart = body.getPosition();

		hits.clear();

		// Main Scanner
		for (float offset = -SCAN_HALF_RADIUS; offset <= SCAN_HALF_RADIUS; offset += SCAN_RADIUS
				/ SCAN_SLICES) {

			float rayEndX = x + (float) Math.cos(angle + offset) * SCAN_LENGTH;
			float rayEndY = y + (float) Math.sin(angle + offset) * SCAN_LENGTH;
			Vector2 rayEnd = new Vector2(rayEndX, rayEndY);

			world.rayCast(this, rayStart, rayEnd);
		}

		// Global Scanner
		float globalScannerLength = SCAN_LENGTH / 2;
		float globalSliceRadians = (MathUtils.PI2 - SCAN_RADIUS) / SCAN_SLICES;
		for (float offset = SCAN_HALF_RADIUS; offset <= MathUtils.PI2
				- SCAN_HALF_RADIUS; offset += globalSliceRadians) {

			float rayEndX = x + (float) Math.cos(angle + offset)
					* globalScannerLength;
			float rayEndY = y + (float) Math.sin(angle + offset)
					* globalScannerLength;
			Vector2 rayEnd = new Vector2(rayEndX, rayEndY);

			world.rayCast(this, rayStart, rayEnd);
		}

		// Scanner
		renderer.begin(ShapeType.Line);
		renderer.setColor(ColorPalate.SCANNER);
		for (Vector2 hit : hits) {
			renderer.line(rayStart, hit);
		}
		float startArcDeg = (angle - SCAN_HALF_RADIUS)
				* MathUtils.radiansToDegrees;
		float scannerArcDeg = SCAN_HALF_RADIUS * 2 * MathUtils.radiansToDegrees;
		renderer.arc(x, y, SCAN_LENGTH, startArcDeg, scannerArcDeg);
		renderer.arc(x, y, globalScannerLength, startArcDeg + scannerArcDeg,
				360 - scannerArcDeg);
		renderer.end();

		// Hits
		renderer.begin(ShapeType.Filled);
		renderer.setColor(ColorPalate.HITS);
		for (Vector2 hit : hits) {
			renderer.circle(hit.x, hit.y, HIT_RADIUS);
		}
		renderer.end();

		// Ship
		renderer.begin(ShapeType.Filled);
		renderer.setColor(ColorPalate.SHIP);
		renderer.circle(x, y, BODY_RADIUS);
		renderer.end();

	}

	@Override
	public float reportRayFixture(Fixture fixture, Vector2 point,
			Vector2 normal, float fraction) {
		if (body.getFixtureList().get(0).equals(fixture)) {
			return -1;
		}

		//Copy is needed as box2d reuses it
		hits.add(point.cpy());

		return -1;
	}
}