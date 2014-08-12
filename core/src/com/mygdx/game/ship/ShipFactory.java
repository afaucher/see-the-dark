package com.mygdx.game.ship;

import java.util.List;

import com.badlogic.gdx.physics.box2d.Body;

public interface ShipFactory {
	// Adds fixtures and sets damping, returns interaction components
	public List<ShipSection> buildShip(Body body);

}
