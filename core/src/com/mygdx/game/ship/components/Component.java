package com.mygdx.game.ship.components;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.mygdx.game.RenderLayer;

public interface Component {
    public enum ComponentType {
        Weapon, Engine, EngineControl, Sensor, Fuel, FuelControl, Beacon,
    }

    public ComponentType getComponentType();

    public Rectangle drawHud(ShapeRenderer renderer, Rectangle destAvailable);

    public boolean requiresHud();

    public boolean requiresInput();

    // TODO: Components should take space!
    // public float getRadius();

    // TODO: This assumes interaction model
    public void keyPressed();

    public void update(float seconds);

    // Render in world coordinates
    public void render(ShapeRenderer renderer, RenderLayer layer);
}
