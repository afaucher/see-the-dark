package com.mygdx.game;

import com.badlogic.gdx.graphics.Color;

public interface BodyData {
	public enum BodyType {
		PLAYER,
		//No idea, really
		PROP,
	}
	
	//Unknown use
	public BodyType getType();
	//Result is intended to be immutable
	public Color getMaterialColor();
}
