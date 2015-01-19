package com.beanfarmergames.seethedark.ship;

import com.badlogic.gdx.physics.box2d.Fixture;
import com.beanfarmergames.seethedark.game.BodyData;

public interface ShipSection extends BodyData {

    // Physics
    public Fixture getFixture();

    // Hull
    // [0-1]
    public float getHullIntegrity();

    // Temp limit before immediate destruction
    public float getHeatLimit();

    public void update(float seconds);
}
