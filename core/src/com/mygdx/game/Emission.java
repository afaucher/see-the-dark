package com.mygdx.game;

import com.badlogic.gdx.math.Vector2;

//Immutable
public class Emission {
    public Vector2 source = null;
    public Vector2 dest = null;
    public float power = 0;

    // TODO: In the future we would want things like 'spectrum' here

    public Emission(Vector2 source, Vector2 dest, float power) {
        this.source = source;
        this.dest = dest;
        this.power = power;
    }
}
