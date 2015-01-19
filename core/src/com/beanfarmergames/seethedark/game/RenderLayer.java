package com.beanfarmergames.seethedark.game;

public enum RenderLayer {
    BACKGROUND(0), NAVIGATION_GUIDE(1), SENSOR_GUIDE(2), PASSIVE_SENSOR_HIT(3), SENSOR_HIT(4), WEAPONS_FORE(5), PLAYER_BODY(
            6), BEACON(7), SCORES(8);

    private int layer;

    RenderLayer(int layer) {
        this.layer = layer;
    }

    public int getLayer() {
        return layer;
    }
}
