package com.mygdx.game;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractBodyData implements BodyData {
	private List<Emission> receivedEmissions = new ArrayList<Emission>();
	private boolean accumlateEmissions = false;
	private float temperature = 0;
	private static float EMISSION_UNITS_PER_HEAT = 100.0f;

	public AbstractBodyData(boolean accumlateEmissions) {
		this.accumlateEmissions = accumlateEmissions;
	}

	@Override
	public void receiveEmission(Emission emission) {
		// TODO: Take duration into account, push into emission?
		if (!accumlateEmissions)
			return;
		this.accumlateHeat(emission.power/EMISSION_UNITS_PER_HEAT);
		receivedEmissions.add(emission);
		//TODO: Cause actual damage
	}

	@Override
	public void resetEmissions() {
		receivedEmissions.clear();
	}

	@Override
	public List<Emission> getEmissions() {
		return receivedEmissions;
	}

	@Override
	public float getTemperature() {
		return temperature;
	}

	public void disapateHeat(float energy) {
		// TODO: Scale for mass
		temperature = Math.max(temperature - energy, 0);
	}

	public void accumlateHeat(float energy) {
		// TODO: Scale for mass
		temperature += energy;
	}
}
