package com.mygdx.game;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.World;

public class Entity implements BodyData {

	private Body body;

	private TwoAxisControl playerOne;
	private SensorAccumlator sensorAccumulator = new SensorAccumlator();
	private static final float SCAN_LENGTH = 300.0f;
	private static final float GLOBAL_SCAN_LENGTH = SCAN_LENGTH/2;
	private static final float BODY_RADIUS = 10.0f;
	private static final float HIT_RADIUS = 5.0f;
	//private static final float TURN_SPEED_RADIANS_PER_FRAME = 0.05f;
	private static final float TORQUE_PER_FRAME = 2000.0f;
	//private static final float SPEED_PER_SECOND = 100.0f;
	private static final float FORCE_PER_FRAME = 100000.0f;
	private static final float SCAN_HALF_RADIUS = 1.0f;
	private static final float SCAN_RADIUS = SCAN_HALF_RADIUS * 2.0f;
	private static final int SCAN_SLICES = 40;

	private static FixtureDef createCircleFixture(World world, Vector2 position,
			float radius) {
		CircleShape sd = new CircleShape();
		sd.setRadius(radius);
		sd.setPosition(position);

		FixtureDef fdef = new FixtureDef();
		fdef.shape = sd;
		fdef.density = 1.0f;
		fdef.friction = 0.5f;
		fdef.restitution = 0.6f;

		return fdef;
	}

	public Entity(World world, TwoAxisControl playerOne) {
		this.playerOne = playerOne;
		Vector2 headPosition = new Vector2(0, 0);
		Vector2 tailPosition = new Vector2(-2 * BODY_RADIUS, 0);
		FixtureDef head = createCircleFixture(world, headPosition,
				BODY_RADIUS);
		FixtureDef tail = createCircleFixture(world, tailPosition,
				BODY_RADIUS);

		BodyDef bd = new BodyDef();
		bd.allowSleep = true;
		bd.position.set(0, 0);
		body = world.createBody(bd);
		body.setBullet(true);
		body.setUserData(this);
		body.setAngularDamping(0.2f);
		body.setLinearDamping(0.1f);
		body.createFixture(head);
		body.createFixture(tail);
		body.setType(BodyDef.BodyType.DynamicBody);

	}

	private void updateMovements() {
		//TODO: Always wakes player
		float torque = -playerOne.getX() * TORQUE_PER_FRAME;
		//body.applyTorque(torque, true);
		body.applyAngularImpulse(torque, true);
		//angle -= playerOne.getX() * TURN_SPEED_RADIANS_PER_FRAME;
		//float speed = playerOne.getY() * SPEED_PER_SECOND;
		float force = playerOne.getY() * FORCE_PER_FRAME;
		float angle = getRotation();
		float vX = (float) Math.cos(angle) * force;
		float vY = (float) Math.sin(angle) * force;

		body.applyForceToCenter(vX, vY, true);
	}

	public float getRotation() {
		return body.getAngle();
	}

	public Vector2 getPosition() {
		return body.getPosition();
	}

	public void render(ShapeRenderer renderer) {
		updateMovements();

		float x = body.getPosition().x;
		float y = body.getPosition().y;
		float angle = getRotation();

		World world = body.getWorld();
		Vector2 rayStart = body.getPosition();

		scanArea(x, y, angle, world, rayStart);
		
		// Render

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
		renderer.arc(x, y, GLOBAL_SCAN_LENGTH, startArcDeg + scannerArcDeg,
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
		
		renderer.translate(x, y, 0);
		renderer.rotate(0, 0, 1, angle * MathUtils.radiansToDegrees);
		
		for (Fixture fixture : body.getFixtureList()) {
			CircleShape cs = (CircleShape)fixture.getShape();
			if (cs == null) continue;
			Vector2 shapePosition = cs.getPosition();
			
			renderer.circle(shapePosition.x, shapePosition.y, cs.getRadius());
		}
		
		renderer.end();

	}

	private void scanArea(float x, float y, float angle, World world,
			Vector2 rayStart) {
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
		// TODO: Jiggles on rotation which the Main scanner does not :/
		float globalSliceRadians = (MathUtils.PI2 - SCAN_RADIUS) / SCAN_SLICES;
		for (float offset = SCAN_HALF_RADIUS; offset <= MathUtils.PI2
				- SCAN_HALF_RADIUS; offset += globalSliceRadians) {

			float rayEndX = x + (float) Math.cos(angle + offset)
					* GLOBAL_SCAN_LENGTH;
			float rayEndY = y + (float) Math.sin(angle + offset)
					* GLOBAL_SCAN_LENGTH;
			Vector2 rayEnd = new Vector2(rayEndX, rayEndY);

			world.rayCast(sensorAccumulator, rayStart, rayEnd);
		}
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
