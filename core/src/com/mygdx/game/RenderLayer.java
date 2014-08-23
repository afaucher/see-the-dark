package com.mygdx.game;

public enum RenderLayer {
	BACKGROUND(0),
	SENSOR_GUIDE(1),
	SENSOR_HIT(2),
	OTHER(3),
	SHIP(4);
		
	private int layer;
	
	RenderLayer(int layer) {
		this.layer = layer;
	}
	
	public int getLayer() {
		return layer;
	}
}
