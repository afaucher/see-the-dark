package com.beanfarmergames.seethedark.game;

import java.util.List;

import com.badlogic.gdx.graphics.Color;

/**
 * Defines interaction model with physical objects.
 *
 * Right now this is a passive model but this is where effects would be pushed.
 */
public interface BodyData {

    // Result is intended to be immutable
    public Color getMaterialColor();

    public void receiveEmission(Emission emission);

    public void resetEmissions();

    public List<Emission> getEmissions();

    public float getTemperature();

    // TODO: Scale for mass
    public void accumlateHeat(float energy);

    public void disapateHeat(float energy);
}
