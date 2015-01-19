package com.beanfarmergames.seethedark.entities;

import com.badlogic.gdx.math.Vector2;
import com.beanfarmergames.seethedark.sensors.SensorAccumlator;

public class Beacon {
    private final SensorAccumlator accumulator;
    private final String name;
    private final Vector2 location;
    
    public Beacon(SensorAccumlator accumulator, String name, Vector2 location) {
        this.accumulator = accumulator;
        this.name = name;
        this.location = location;
    }

    public SensorAccumlator getAccumulator() {
        return accumulator;
    }

    public String getName() {
        return name;
    }

    public Vector2 getLocation() {
        return location;
    }
    
    
}
