package com.mygdx.game.ship;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.mygdx.game.BodyData;
import com.mygdx.game.ColorPalate;

public class StaticShipFactory implements ShipFactory {

	private static final float BODY_RADIUS = 10.0f;
	private static final float TORQUE_PER_FRAME = 2000.0f;
	private static final float FORCE_PER_FRAME = 100000.0f;
	private static final float FUEL = 100000000;
	private static final float HEAT_LIMIT = 1000;
	private static final float DAMAGE_HEAT_THRESHOLD = HEAT_LIMIT * 9 / 10;
	// TODO: Make part of the specific component configuration
	private static final float HEAT_PER_FUEL = 0.0004f;
	private static final float HEAT_DAMAGE_PER_SECOND = 0.15f;
	private static final float HEAT_DISAPATED_PER_SECOND = HEAT_LIMIT / 2;

	private class ShipSectionInstance implements ShipSection, BodyData {

		private float torque = 0;
		private float thrust = 0;
		private float fuel = 0;
		private float fuelCapacity = 0;
		private float integrity = 1;
		private float heat = 0;
		private float heatLimit = 0;
		private Fixture fixture = null;

		public ShipSectionInstance(Vector2 position, float radius,
				float torque, float thrust, float fuel, float heatLimit,
				Body body) {
			this.torque = torque;
			this.thrust = thrust;
			this.fuel = fuel;
			this.fuelCapacity = fuel;
			this.heatLimit = heatLimit;

			CircleShape sd = new CircleShape();
			sd.setRadius(radius);
			sd.setPosition(position);

			FixtureDef fdef = new FixtureDef();
			fdef.shape = sd;
			fdef.density = 1.0f;
			fdef.friction = 0.5f;
			fdef.restitution = 0.6f;

			fixture = body.createFixture(fdef);
			fixture.setUserData(this);
		}

		@Override
		public Color getMaterialColor() {
			return ColorPalate.SHIP;
		}

		@Override
		public Fixture getFixture() {
			return fixture;
		}

		@Override
		public float getHullIntegrity() {
			return integrity;
		}

		@Override
		public float getTorqueContribution() {
			if (integrity > 0) {
				return torque;
			}
			return 0;
		}

		@Override
		public float getThrustContribution() {
			if (integrity > 0) {
				return thrust;
			}
			return 0;
		}

		@Override
		public float getFuel() {
			return fuel;
		}

		// TODO: Move to a model where ship components are 'on' and update
		// applies the right effect or are one shot
		@Override
		public float burnFuel(float toBurn) {
			float canBurn = Math.min(fuel, toBurn);
			fuel -= canBurn;
			heat = Math.min(HEAT_PER_FUEL * canBurn + heat, heatLimit);
			return Math.max(0, toBurn - canBurn);
		}

		@Override
		public float getFuelCapacity() {
			return fuelCapacity;
		}

		@Override
		public float getHeat() {
			return heat;
		}

		@Override
		public float getHeatLimit() {
			return heatLimit;
		}

		@Override
		public void update(float seconds) {
			if (heat > DAMAGE_HEAT_THRESHOLD) {
				integrity = Math.max(0, integrity
						- (HEAT_DAMAGE_PER_SECOND * seconds));
			}
			if (heat > 0) {
				heat = Math.max(0, heat - HEAT_DISAPATED_PER_SECOND * seconds);
			}
		}

	}

	@Override
	public List<ShipSection> buildShip(Body body) {

		Vector2 headPosition = new Vector2(0, 0);
		Vector2 tailPosition = new Vector2(-2 * BODY_RADIUS, 0);

		ShipSectionInstance head = new ShipSectionInstance(headPosition,
				BODY_RADIUS, TORQUE_PER_FRAME, 0, 0, HEAT_LIMIT, body);
		ShipSectionInstance tail = new ShipSectionInstance(tailPosition,
				BODY_RADIUS, 0, FORCE_PER_FRAME, FUEL, HEAT_LIMIT, body);

		List<ShipSection> sections = new ArrayList<ShipSection>();

		sections.add(head);
		sections.add(tail);

		return sections;
	}

}
