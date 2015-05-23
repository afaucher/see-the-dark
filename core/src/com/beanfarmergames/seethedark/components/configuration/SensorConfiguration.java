package com.beanfarmergames.seethedark.components.configuration;

public class SensorConfiguration extends ComponentConfiguration {
    private float sensorRadius;
    private float sensorSweepAngleRad;
    private int sensorSteps;
    private float sensorEmissionMaxPower;
    
	public float getSensorRadius() {
		return sensorRadius;
	}
	public void setSensorRadius(float sensorRadius) {
		this.sensorRadius = sensorRadius;
	}
	public float getSensorSweepAngleRad() {
		return sensorSweepAngleRad;
	}
	public void setSensorSweepAngleRad(float sensorSweepAngleRad) {
		this.sensorSweepAngleRad = sensorSweepAngleRad;
	}
	public int getSensorSteps() {
		return sensorSteps;
	}
	public void setSensorSteps(int sensorSteps) {
		this.sensorSteps = sensorSteps;
	}
	public float getSensorEmissionMaxPower() {
		return sensorEmissionMaxPower;
	}
	public void setSensorEmissionMaxPower(float sensorEmissionMaxPower) {
		this.sensorEmissionMaxPower = sensorEmissionMaxPower;
	}
}
