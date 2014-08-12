package com.mygdx.game;

import com.badlogic.gdx.graphics.Color;

/**
 * Defines interaction model with physical objects. 
 *
 * Right now this is a passive model but this is where effects would be pushed.
 */
public interface BodyData {

	// Result is intended to be immutable
	public Color getMaterialColor();
}
