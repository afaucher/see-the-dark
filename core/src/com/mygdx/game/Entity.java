package com.mygdx.game;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.World;

public class Entity implements BodyData {

	private Body body;
	private float angle = 0;
	
	private TwoAxisControl playerOne;
	private SensorAccumlator sensorAccumulator = new SensorAccumlator();
	private static final float SCAN_LENGTH = 300.0f;
	private static final float BODY_RADIUS = 10.0f;
	private static final float HIT_RADIUS = 5.0f;
	private static final float TURN_SPEED_RADIANS_PER_FRAME = 0.05f;
	private static final float SPEED_PER_SECOND = 100.0f;
	private static final float SCAN_HALF_RADIUS = 1.0f;
	private static final float SCAN_RADIUS = SCAN_HALF_RADIUS * 2.0f;
	private static final int SCAN_SLICES = 40;

	public Entity(World world, TwoAxisControl playerOne) {
		this.playerOne = playerOne;
		body = BodyHelper.createCircle(world, 0, 0, BODY_RADIUS, false, this);
		body.setBullet(true);
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

		sensorAccumulator.reset();

		// Main Scanner
		for (float offset = -SCAN_HALF_RADIUS; offset <= SCAN_HALF_RADIUS; offset += SCAN_RADIUS
				/ SCAN_SLICES) {

			float rayEndX = x + (float) Math.cos(angle + offset) * SCAN_LENGTH;
			float rayEndY = y + (float) Math.sin(angle + offset) * SCAN_LENGTH;
			Vector2 rayEnd = new Vector2(rayEndX, rayEndY);

			world.rayCast(sensorAccumulator, rayStart, rayEnd);
		}

		// Global Scanner
		//TODO: Jiggles on rotation which the Main scanner does not :/
		float globalScannerLength = SCAN_LENGTH / 2;
		float globalSliceRadians = (MathUtils.PI2 - SCAN_RADIUS) / SCAN_SLICES;
		for (float offset = SCAN_HALF_RADIUS; offset <= MathUtils.PI2
				- SCAN_HALF_RADIUS; offset += globalSliceRadians) {

			float rayEndX = x + (float) Math.cos(angle + offset)
					* globalScannerLength;
			float rayEndY = y + (float) Math.sin(angle + offset)
					* globalScannerLength;
			Vector2 rayEnd = new Vector2(rayEndX, rayEndY);

			world.rayCast(sensorAccumulator, rayStart, rayEnd);
		}

		// Scanner
		renderer.begin(ShapeType.Line);
		renderer.setColor(ColorPalate.SCANNER);
		for (SensorHit hit : sensorAccumulator.getHits()) {
			renderer.line(rayStart, hit.hitLocation);
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
		for (SensorHit hit : sensorAccumulator.getHits()) {
			Vector2 hitLocation = hit.hitLocation;
			if (hit.data != null) {
				renderer.setColor(hit.data.getMaterialColor());
			} else {
				renderer.setColor(ColorPalate.HITS);
			}
			renderer.circle(hitLocation.x, hitLocation.y, HIT_RADIUS);
		}
		renderer.end();

		// Ship
		renderer.begin(ShapeType.Filled);
		renderer.setColor(ColorPalate.SHIP);
		renderer.circle(x, y, BODY_RADIUS);
		renderer.end();

	}

	@Override
	public BodyType getType() {
		return BodyType.PLAYER;
	}

	@Override
	public Color getMaterialColor() {
		return ColorPalate.SHIP;
	}
}
