package com.mygdx.game.ship;

import com.badlogic.gdx.physics.box2d.Fixture;
import com.mygdx.game.BodyData;

public interface ShipSection {

	// Physics
	public Fixture getFixture();
	
	// Hull
	//[0-1]
	public float getHullIntegrity();
	//Temp
	public float getHeat();
	//Temp limit before immediate destruction
	public float getHeatLimit();

	// Engine
	public float getTorqueContribution();
	public float getThrustContribution();
	
	public float getFuel();
	public float getFuelCapacity();

	/**
	 * 
	 * @param toBurn fuel to try and burn
	 * @return Fuel remaining to burn
	 */
	public float burnFuel(float toBurn);
	public void update(float seconds);
}
