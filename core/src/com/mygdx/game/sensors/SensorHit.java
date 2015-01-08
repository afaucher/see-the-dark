package com.mygdx.game.sensors;

import com.badlogic.gdx.math.Vector2;
import com.mygdx.game.BodyData;

public class SensorHit {
    public Vector2 hitLocation = null;
    public Vector2 normal = null;
    // If available
    public BodyData data = null;
}
