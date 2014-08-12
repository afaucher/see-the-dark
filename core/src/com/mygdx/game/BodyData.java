package com.mygdx.game;

import com.badlogic.gdx.graphics.Color;

public interface BodyData {
	public enum BodyType {
		PLAYER,
		PROP,
	}
	
	public BodyType getType();
	//Result is intended to be immutable
	public Color getMaterialColor();
}
