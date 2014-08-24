package com.mygdx.game.ship;

import java.util.List;

public class CompoundShip {

	private List<ShipSection> shipSections = null;

	public CompoundShip(List<ShipSection> shipSections) {
		this.shipSections = shipSections;
	}

	public float getTorqueContribution() {
		float torque = 0;
		for (ShipSection s : shipSections) {
			torque += s.getTorqueContribution();
		}
		return torque;
	}

	public float getThrustContribution() {
		float thrust = 0;
		for (ShipSection s : shipSections) {
			thrust += s.getThrustContribution();
		}
		return thrust;
	}

	public float getHullIntegrity() {
		float minIntegrity = 0;
		for (ShipSection s : shipSections) {
			minIntegrity = Math.min(s.getHullIntegrity(), minIntegrity);
		}
		return minIntegrity;
	}

	public float getTemperature() {
		float heat = 0;
		for (ShipSection s : shipSections) {
			heat += s.getTemperature();
		}
		return heat;
	}

	public float getHeatLimit() {
		float heatLimit = 0;
		for (ShipSection s : shipSections) {
			heatLimit += s.getHeatLimit();
		}
		return heatLimit;
	}

	public void update(float seconds) {
		for (ShipSection s : shipSections) {
			s.update(seconds);
		}
	}
}