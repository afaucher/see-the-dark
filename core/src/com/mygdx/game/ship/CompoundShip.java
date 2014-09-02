package com.mygdx.game.ship;

import java.util.List;

//This is just a placeholder that loops over all sections for summaries
public class CompoundShip {

	private List<ShipSection> shipSections = null;

	public CompoundShip(List<ShipSection> shipSections) {
		this.shipSections = shipSections;
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