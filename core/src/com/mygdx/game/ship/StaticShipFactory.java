package com.mygdx.game.ship;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.mygdx.game.ColorPalate;

public class StaticShipFactory implements ShipFactory {

	private static final float BODY_RADIUS = 10.0f;
	private static final float TORQUE_PER_FRAME = 2000.0f;
	private static final float FORCE_PER_FRAME = 100000.0f;
	private static final float FUEL = 100000000;

	private class ShipSectionInstance implements ShipSection {

		private float torque = 0;
		private float thrust = 0;
		private float fuel = 0;
		private float fuelCapacity = 0;
		private Fixture fixture = null;

		public ShipSectionInstance(Vector2 position, float radius,
				float torque, float thrust, float fuel, Body body) {
			this.torque = torque;
			this.thrust = thrust;
			this.fuel = fuel;
			this.fuelCapacity = fuel;

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
		public float getTorqueContribution() {
			return torque;
		}

		@Override
		public float getThrustContribution() {
			return thrust;
		}
		
		@Override
		public float getFuel() {
			return fuel;
		}

		@Override
		public float burnFuel(float toBurn) {
			float canBurn = Math.min(fuel, toBurn);
			fuel -= canBurn;
			return Math.max(0, toBurn - canBurn);
		}

		@Override
		public float getFuelCapacity() {
			return fuelCapacity;
		}

	}

	@Override
	public List<ShipSection> buildShip(Body body) {

		Vector2 headPosition = new Vector2(0, 0);
		Vector2 tailPosition = new Vector2(-2 * BODY_RADIUS, 0);

		ShipSectionInstance head = new ShipSectionInstance(headPosition,
				BODY_RADIUS, TORQUE_PER_FRAME, 0, 0, body);
		ShipSectionInstance tail = new ShipSectionInstance(tailPosition,
				BODY_RADIUS, 0, FORCE_PER_FRAME, FUEL, body);

		List<ShipSection> sections = new ArrayList<ShipSection>();

		sections.add(head);
		sections.add(tail);

		return sections;
	}

}
