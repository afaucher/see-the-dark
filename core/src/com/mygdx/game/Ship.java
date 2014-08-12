package com.mygdx.game;

import java.util.List;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.World;
import com.mygdx.game.ship.ShipFactory;
import com.mygdx.game.ship.ShipSection;
import com.mygdx.game.ship.StaticShipFactory;

public class Ship {

	private Body body;

	private TwoAxisControl controls = null;
	private SensorAccumlator sensorAccumulator = new SensorAccumlator();
	private List<ShipSection> shipSections = null;

	private static final float SCAN_LENGTH = 300.0f;
	private static final float GLOBAL_SCAN_LENGTH = SCAN_LENGTH / 2;
	private static final float HIT_RADIUS = 5.0f;
	private static final float SCAN_HALF_RADIUS = 1.0f;
	private static final float SCAN_RADIUS = SCAN_HALF_RADIUS * 2.0f;
	private static final int SCAN_SLICES = 40;

	private static final ShipFactory factory = new StaticShipFactory();

	public Ship(World world, TwoAxisControl controls, Vector2 spwan) {
		this.controls = controls;

		BodyDef bd = new BodyDef();
		bd.allowSleep = true;
		bd.position.set(spwan.x, spwan.y);
		body = world.createBody(bd);
		body.setBullet(true);
		body.setAngularDamping(0.2f);
		body.setLinearDamping(0.1f);
		shipSections = factory.buildShip(body);
		body.setType(BodyDef.BodyType.DynamicBody);

	}

	private float getTorque() {
		float torque = 0;
		for (ShipSection s : shipSections) {
			torque += s.getTorqueContribution();
		}
		return torque;
	}

	private float getThrust() {
		float thrust = 0;
		for (ShipSection s : shipSections) {
			thrust += s.getThrustContribution();
		}
		return thrust;
	}
	
	public float getFuel() {
		float fuel = 0;
		for (ShipSection s : shipSections) {
			fuel += s.getFuel();
		}
		return fuel;
	}
	
	public float getFuelCapacity() {
		float fuel = 0;
		for (ShipSection s : shipSections) {
			fuel += s.getFuelCapacity();
		}
		return fuel;
	}
	
	private void updateMovements() {
		// TODO: Always wakes player
		float torque = -controls.getX() * getTorque();
		float force = controls.getY() * getThrust();
		
		float fuelToBurn = Math.abs(torque) + Math.abs(force);
		for (ShipSection s : shipSections) {
			if (fuelToBurn <= 0) break;
			fuelToBurn = s.burnFuel(fuelToBurn);
		}
		if (fuelToBurn > 0) {
			//Out of fuel
			return;
		}
		
		float angle = getRotation();
		float vX = (float) Math.cos(angle) * force;
		float vY = (float) Math.sin(angle) * force;

		body.applyAngularImpulse(torque, true);
		body.applyForceToCenter(vX, vY, true);
	}

	public float getRotation() {
		return body.getAngle();
	}

	public Vector2 getPosition() {
		return body.getPosition();
	}
	
	public void update(float seconds) {
		updateMovements();
		
		float x = body.getPosition().x;
		float y = body.getPosition().y;
		float angle = getRotation();

		World world = body.getWorld();
		Vector2 rayStart = body.getPosition();

		scanArea(x, y, angle, world, rayStart);
	}

	public void render(ShapeRenderer renderer) {
		

		float x = body.getPosition().x;
		float y = body.getPosition().y;
		float angle = getRotation();

		Vector2 rayStart = body.getPosition();

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
			CircleShape cs = (CircleShape) fixture.getShape();
			if (cs == null)
				continue;
			Vector2 shapePosition = cs.getPosition();

			renderer.circle(shapePosition.x, shapePosition.y, cs.getRadius());
		}
		renderer.identity();

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
}
