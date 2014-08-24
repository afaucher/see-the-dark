package com.mygdx.game;

public enum RenderLayer {
	BACKGROUND(0), SENSOR_GUIDE(1), PASSIVE_SENSOR_HIT(2), SENSOR_HIT(3), WEAPONS_FORE(
			4), PLAYER_BODY(5);

	private int layer;

	RenderLayer(int layer) {
		this.layer = layer;
	}

	public int getLayer() {
		return layer;
	}
}
