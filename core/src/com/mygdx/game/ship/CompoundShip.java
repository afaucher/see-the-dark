package com.mygdx.game.ship;

import java.util.List;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.physics.box2d.Fixture;

public class CompoundShip implements ShipSection {

	private List<ShipSection> shipSections = null;

	public CompoundShip(List<ShipSection> shipSections) {
		this.shipSections = shipSections;
	}

	@Override
	public float getTorqueContribution() {
		float torque = 0;
		for (ShipSection s : shipSections) {
			torque += s.getTorqueContribution();
		}
		return torque;
	}

	@Override
	public float getThrustContribution() {
		float thrust = 0;
		for (ShipSection s : shipSections) {
			thrust += s.getThrustContribution();
		}
		return thrust;
	}

	@Override
	public float getFuel() {
		float fuel = 0;
		for (ShipSection s : shipSections) {
			fuel += s.getFuel();
		}
		return fuel;
	}

	@Override
	public float getFuelCapacity() {
		float fuel = 0;
		for (ShipSection s : shipSections) {
			fuel += s.getFuelCapacity();
		}
		return fuel;
	}

	@Override
	public Fixture getFixture() {
		return null;
	}

	@Override
	public float burnFuel(float toBurn) {
		for (ShipSection s : shipSections) {
			if (toBurn <= 0) break;
			toBurn = s.burnFuel(toBurn);
		}
		return toBurn;
	}

	@Override
	public float getHullIntegrity() {
		float minIntegrity = 0;
		for (ShipSection s : shipSections) {
			minIntegrity = Math.min(s.getHullIntegrity(), minIntegrity);
		}
		return minIntegrity;
	}

	@Override
	public float getHeat() {
		float heat = 0;
		for (ShipSection s : shipSections) {
			heat += s.getHeat();
		}
		return heat;
	}

	@Override
	public float getHeatLimit() {
		float heatLimit = 0;
		for (ShipSection s : shipSections) {
			heatLimit += s.getHeatLimit();
		}
		return heatLimit;
	}

	@Override
	public void update(float seconds) {
		for (ShipSection s : shipSections) {
			s.update(seconds);
		}
	}
}