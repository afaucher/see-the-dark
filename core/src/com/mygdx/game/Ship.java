package com.mygdx.game;

import java.util.ArrayList;
import java.util.Collections;
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
import com.mygdx.game.ship.Component;
import com.mygdx.game.ship.CompoundShip;
import com.mygdx.game.ship.SensorComponent;
import com.mygdx.game.ship.ShipFactory;
import com.mygdx.game.ship.ShipSection;
import com.mygdx.game.ship.StaticShipFactory;

public class Ship {

	private Body body;

	private TwoAxisControl controls = null;
	private SensorAccumlator sensorAccumulator = new SensorAccumlator();

	private List<Component> components = new ArrayList<Component>();
	private List<Component> immutableComponents = Collections.unmodifiableList(components);

	private CompoundShip ship = null;

	private static final float SCAN_RADIUS = 300.0f;
	private static final float GLOBAL_SCAN_RADIUS = SCAN_RADIUS / 2;
	private static final float SCAN_HALF_ARC_RAD = 1.0f;
	private static final float SCAN_ARC_RAD = SCAN_HALF_ARC_RAD * 2.0f;
	private static final int SCAN_SLICES = 40;

	private static final float HIT_RADIUS = 5.0f;

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
		List<ShipSection> sections = factory.buildShip(body);
		ship = new CompoundShip(sections);
		body.setType(BodyDef.BodyType.DynamicBody);

		Fixture firstFixture = sections.get(0).getFixture();
		Fixture secondFixture = sections.get(1).getFixture();

		SensorComponent frontSensor = new SensorComponent(SCAN_RADIUS,
				-SCAN_HALF_ARC_RAD, SCAN_ARC_RAD / SCAN_SLICES, SCAN_SLICES);
		frontSensor.mountToFixture(this, firstFixture);
		
		components.add(frontSensor);
		
		SensorComponent rearSensor = new SensorComponent(GLOBAL_SCAN_RADIUS,
				SCAN_HALF_ARC_RAD, (MathUtils.PI2 - SCAN_ARC_RAD) / SCAN_SLICES, SCAN_SLICES);
		rearSensor.mountToFixture(this, secondFixture);

		components.add(rearSensor);

	}

	public float getFuel() {
		return ship.getFuel();
	}

	public float getFuelCapacity() {
		return ship.getFuelCapacity();
	}

	private void updateMovements() {
		// TODO: Always wakes player
		float torque = -controls.getX() * ship.getTorqueContribution();
		float force = controls.getY() * ship.getThrustContribution();

		float fuelToBurn = Math.abs(torque) + Math.abs(force);
		fuelToBurn = ship.burnFuel(fuelToBurn);
		if (fuelToBurn > 0) {
			// Out of fuel
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

		ship.update(seconds);

		// scanArea(x, y, angle, world, rayStart);

		sensorAccumulator.reset();
		for (Component component : components) {
			component.update(seconds);
		}
	}

	public void render(ShapeRenderer renderer, boolean full) {

		if (!full) {
			// TODO: If same team, render the sensors
			// TODO: Better yet, do each layer in passes
			return;
		}

		float x = body.getPosition().x;
		float y = body.getPosition().y;
		float angle = getRotation();

		// Render

		// Scanner
		for (Component component : components) {
			component.render(renderer, RenderLayer.SENSOR_GUIDE);
		}

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

		renderer.translate(x, y, 0);
		renderer.rotate(0, 0, 1, angle * MathUtils.radiansToDegrees);

		for (Fixture fixture : body.getFixtureList()) {
			CircleShape cs = (CircleShape) fixture.getShape();
			if (cs == null)
				continue;
			Vector2 shapePosition = cs.getPosition();

			ShipSection section = (ShipSection) fixture.getUserData();
			// TODO: Fixme
			BodyData sectionBodyData = (BodyData) fixture.getUserData();

			renderer.setColor(sectionBodyData.getMaterialColor());
			renderer.circle(shapePosition.x, shapePosition.y, cs.getRadius());

			// Heat Indicator
			renderer.setColor(ColorPalate.SECTION_HEAT_INDICATOR);
			float heatIndicatorRadius = cs.getRadius() * 3 / 4;
			float heatPropotion = section.getHeat() / section.getHeatLimit();
			renderer.arc(shapePosition.x, shapePosition.y, heatIndicatorRadius,
					0, heatPropotion * 360);

			// Hull Indicator
			renderer.setColor(ColorPalate.SECTION_HULL_INDICATOR);
			float hullIndicatorRadius = cs.getRadius() / 2;
			renderer.arc(shapePosition.x, shapePosition.y, hullIndicatorRadius,
					0, section.getHullIntegrity() * 360);
		}
		renderer.identity();

		renderer.end();
	}

	public SensorAccumlator getSensorAccumulator() {
		return sensorAccumulator;
	}
	
	public List<Component> getComponents() {
		return immutableComponents;
	}
}
