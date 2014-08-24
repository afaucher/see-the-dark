package com.mygdx.game.ship.components;

public interface FuelControl {
	public float getFuel();

	public float getFuelCapacity();

	/**
	 * 
	 * @param toBurn
	 *            fuel to try and burn
	 * @return Fuel remaining to burn
	 */
	public float burnFuel(float toBurn);
}
