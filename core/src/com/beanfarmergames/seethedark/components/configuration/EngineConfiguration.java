package com.beanfarmergames.seethedark.components.configuration;

public class EngineConfiguration extends ComponentConfiguration {
	private float heatPerUnitFuelJoule;
	private float torqueJoule;
	private float thrustJoule;
	//TOOD: Fuel consumed per joule output?
	
	public float getHeatPerUnitFuelJoule() {
		return heatPerUnitFuelJoule;
	}
	public void setHeatPerUnitFuelJoule(float heatPerUnitFuelJoule) {
		this.heatPerUnitFuelJoule = heatPerUnitFuelJoule;
	}
	public float getTorqueJoule() {
		return torqueJoule;
	}
	public void setTorqueJoule(float torqueJoule) {
		this.torqueJoule = torqueJoule;
	}
	public float getThrustJoule() {
		return thrustJoule;
	}
	public void setThrustJoule(float thrustJoule) {
		this.thrustJoule = thrustJoule;
	}	
}
