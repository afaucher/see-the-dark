package com.mygdx.game.ship.components;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.mygdx.game.RenderLayer;

public class FuelComponent extends AbstractComponent implements FuelControl {

	private float fuel = 0;
	private float fuelCapacity = 0;

	public FuelComponent(float fuel, float fuelCapacity) {
		this.fuel = fuel;
		this.fuelCapacity = fuelCapacity;
	}

	@Override
	public ComponentType getComponentType() {
		return ComponentType.Fuel;
	}

	@Override
	public Rectangle drawHud(ShapeRenderer renderer, Rectangle destAvailable) {
		return null;
	}

	@Override
	public boolean requiresHud() {
		return false;
	}

	@Override
	public boolean requiresInput() {
		return false;
	}

	@Override
	public void keyPressed() {

	}

	@Override
	public void update(float seconds) {

	}

	@Override
	public void render(ShapeRenderer renderer, RenderLayer layer) {

	}

	@Override
	public float getFuel() {
		return fuel;
	}

	// TODO: Move to a model where ship components are 'on' and update
	// applies the right effect or are one shot
	@Override
	public float burnFuel(float toBurn) {
		float canBurn = Math.min(fuel, toBurn);
		fuel -= canBurn;
		// TODO: Apply heat here
		// heat = Math.min(HEAT_PER_FUEL * canBurn + heat, heatLimit);
		return Math.max(0, toBurn - canBurn);
	}

	@Override
	public float getFuelCapacity() {
		return fuelCapacity;
	}

}
