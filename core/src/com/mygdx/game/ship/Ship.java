package com.mygdx.game.ship;

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
import com.mygdx.game.BodyData;
import com.mygdx.game.ColorPalate;
import com.mygdx.game.Emission;
import com.mygdx.game.RenderLayer;
import com.mygdx.game.SensorAccumlator;
import com.mygdx.game.SensorHit;
import com.mygdx.game.TwoAxisControl;
import com.mygdx.game.ship.components.Component;
import com.mygdx.game.ship.components.EngineComponent;
import com.mygdx.game.ship.components.FuelComponent;
import com.mygdx.game.ship.components.FuelControlComponent;
import com.mygdx.game.ship.components.SensorComponent;

public class Ship {

	private Body body;

	private TwoAxisControl controls = null;
	private SensorAccumlator sensorAccumulator = new SensorAccumlator();

	private List<Component> components = new ArrayList<Component>();
	private List<Component> immutableComponents = Collections
			.unmodifiableList(components);

	private FuelControlComponent fuelControl = null;

	private CompoundShip ship = null;
	private Vector2 weaponTarget = null;

	private static final float SCAN_RADIUS = 300.0f;
	private static final float GLOBAL_SCAN_RADIUS = SCAN_RADIUS / 2;
	private static final float SCAN_HALF_ARC_RAD = 1.0f;
	private static final float SCAN_ARC_RAD = SCAN_HALF_ARC_RAD * 2.0f;
	private static final int SCAN_SLICES = 40;
	private static final float FUEL = 100000000;

	private static final float ACTIVE_SENSOR_HIT_RADIUS = 5.0f;
	private static final float PASSIVE_SENSOR_MAXIUM_POWER = 100.0f;
	private static final float PASSIVE_SENSOR_MIN_RADIUS = 3.0f;

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

		ShipSection firstSection = sections.get(0);
		ShipSection secondSection = sections.get(1);

		SensorComponent frontSensor = new SensorComponent(SCAN_RADIUS,
				-SCAN_HALF_ARC_RAD, SCAN_ARC_RAD / SCAN_SLICES, SCAN_SLICES);
		frontSensor.mountToSection(this, firstSection);

		components.add(frontSensor);

		SensorComponent rearSensor = new SensorComponent(GLOBAL_SCAN_RADIUS,
				SCAN_HALF_ARC_RAD,
				(MathUtils.PI2 - SCAN_ARC_RAD) / SCAN_SLICES, SCAN_SLICES);
		rearSensor.mountToSection(this, secondSection);

		components.add(rearSensor);

		fuelControl = new FuelControlComponent();
		fuelControl.mountToSection(this, null);

		components.add(fuelControl);

		FuelComponent fuel = new FuelComponent(FUEL, FUEL);
		fuel.mountToSection(this, secondSection);

		components.add(fuel);

		EngineComponent engine = new EngineComponent();
		engine.mountToSection(this, secondSection);

		components.add(engine);
	}

	public void aimWeapons(Vector2 target) {
		weaponTarget = target.cpy();
	}

	private void updateMovements() {
		// TODO: Always wakes player
		float torque = -controls.getX() * ship.getTorqueContribution();
		float force = controls.getY() * ship.getThrustContribution();

		float fuelToBurn = Math.abs(torque) + Math.abs(force);
		fuelToBurn = fuelControl.burnFuel(fuelToBurn);
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

		// Run logic for all components including sensors, engines, weapons, etc
		for (Component component : components) {
			component.update(seconds);
		}

		// At this point, senors have data points for all emissions from the
		// fixtures

		// TODO: Handle emissions, ex, generate heat

	}

	public void render(ShapeRenderer renderer, boolean full, RenderLayer layer) {

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
		if (RenderLayer.PASSIVE_SENSOR_HIT.equals(layer)) {

			// TODO: This needs to be applied in the update loop but there is an
			// issue with call ordering :/
			sensorAccumulator.accumulateEmissions(body);

			renderer.begin(ShapeType.Filled);
			for (Emission emission : sensorAccumulator.getReceivedEmissions()) {
				Vector2 hitLocation = emission.source;
				renderer.setColor(ColorPalate.PASSIVE_SENSOR_HITS);

				float cappedPower = Math.min(emission.power,
						PASSIVE_SENSOR_MAXIUM_POWER);
				float powerRatio = cappedPower / PASSIVE_SENSOR_MAXIUM_POWER;
				float radius = (float) Math.sqrt(powerRatio);
				radius = Math.max(radius, PASSIVE_SENSOR_MIN_RADIUS);

				renderer.circle(hitLocation.x, hitLocation.y, radius);
			}
			renderer.end();

			sensorAccumulator.resetPassiveSensors();
		}

		if (RenderLayer.SENSOR_HIT.equals(layer)) {
			renderer.begin(ShapeType.Filled);
			for (SensorHit hit : sensorAccumulator.getHits()) {
				Vector2 hitLocation = hit.hitLocation;
				if (hit.data != null) {
					renderer.setColor(hit.data.getMaterialColor());
				} else {
					renderer.setColor(ColorPalate.ACTIVE_SENSOR_HITS);
				}
				renderer.circle(hitLocation.x, hitLocation.y,
						ACTIVE_SENSOR_HIT_RADIUS);
			}
			renderer.end();

			sensorAccumulator.resetActiveSensors();
		}

		// Ship
		if (RenderLayer.PLAYER_BODY.equals(layer)) {
			renderer.begin(ShapeType.Filled);

			renderer.translate(x, y, 0);
			renderer.rotate(0, 0, 1, angle * MathUtils.radiansToDegrees);

			for (Fixture fixture : body.getFixtureList()) {
				CircleShape cs = (CircleShape) fixture.getShape();
				if (cs == null)
					continue;
				Vector2 shapePosition = cs.getPosition();

				ShipSection section = (ShipSection) fixture.getUserData();

				renderer.setColor(section.getMaterialColor());
				renderer.circle(shapePosition.x, shapePosition.y,
						cs.getRadius());

				// Heat Indicator
				renderer.setColor(ColorPalate.SECTION_HEAT_INDICATOR);
				float heatIndicatorRadius = cs.getRadius() * 3 / 4;
				float heatProportion = Math.min(1.0f, section.getTemperature()
						/ section.getHeatLimit());
				renderer.arc(shapePosition.x, shapePosition.y,
						heatIndicatorRadius, 0, heatProportion * 360);

				// Hull Indicator
				renderer.setColor(ColorPalate.SECTION_HULL_INDICATOR);
				float hullIndicatorRadius = cs.getRadius() / 2;
				renderer.arc(shapePosition.x, shapePosition.y,
						hullIndicatorRadius, 0,
						section.getHullIntegrity() * 360);
			}
			renderer.identity();

			renderer.end();
		}
	}

	public SensorAccumlator getSensorAccumulator() {
		return sensorAccumulator;
	}

	public List<Component> getComponents() {
		return immutableComponents;
	}
}
