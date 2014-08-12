package com.mygdx.game.ship;

import com.badlogic.gdx.physics.box2d.Fixture;
import com.mygdx.game.BodyData;

public interface ShipSection extends BodyData {

	// Physics
	public Fixture getFixture();

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
}
