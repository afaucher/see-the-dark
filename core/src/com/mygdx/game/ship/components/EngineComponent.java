package com.mygdx.game.ship.components;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.Transform;
import com.badlogic.gdx.physics.box2d.World;
import com.mygdx.game.ColorPalate;
import com.mygdx.game.EmissionSource;
import com.mygdx.game.EmissionSource.EmissionPowerDropoff;
import com.mygdx.game.RenderLayer;
import com.mygdx.game.ship.ShipSection;
import com.mygdx.game.util.PhysicsUtil;

public class EngineComponent extends AbstractComponent {

    private boolean engineEnabled = false;
    private EngineContribution contirbution = null;
    private static final float HEAT_PER_FUEL_JOULE = 0.0000075f;
    private static final float ENGINE_EMISSION_DIST = 1000.0f;

    public EngineComponent(float torqueJoule, float thrustJoule) {
        this.contirbution = new EngineContribution(torqueJoule, thrustJoule);
    }

    private EmissionSource emissionSource = new EmissionSource(EmissionPowerDropoff.EXPONENTIAL);

    @Override
    public ComponentType getComponentType() {
        return ComponentType.Engine;
    }

    @Override
    public Rectangle drawHud(ShapeRenderer renderer, Rectangle destAvailable) {
        float height = destAvailable.getHeight();
        float width = height / 2;

        renderer.begin(ShapeType.Filled);
        Color color = engineEnabled ? ColorPalate.ACTIVE_HUD : ColorPalate.INACTIVE_HUD;
        renderer.setColor(color);
        renderer.rect(destAvailable.x, destAvailable.y, width, height / 2);
        renderer.rect(destAvailable.x, destAvailable.y + height * 3 / 4, width, height / 4);

        renderer.end();

        Rectangle result = new Rectangle(destAvailable);
        result.setWidth(width);

        return result;
    }

    @Override
    public boolean requiresHud() {
        return true;
    }

    // Engine
    public EngineContribution getEngineContribution() {

        ShipSection section = getMountedSection();
        if (!engineEnabled || section.getHullIntegrity() <= 0) {
            return new EngineContribution(0, 0);
        }

        return contirbution.cpy();
    }

    public void fireEngineFor(float torqueJoules, float thrustJoules) {
        ShipSection section = getMountedSection();
        float energy = (Math.abs(torqueJoules) + Math.abs(thrustJoules)) * HEAT_PER_FUEL_JOULE;
        if (energy <= 0) {
            return;
        }
        section.accumlateHeat(energy);

        Fixture fixture = getMountedFixture();
        Transform transform = PhysicsUtil.getWorldFixturePositionTransform(fixture);

        World world = fixture.getBody().getWorld();
        Vector2 source = transform.getPosition().cpy();
        // Turn the opisite heading
        float engineOutputRad = transform.getRotation() + MathUtils.PI;
        float x = source.x + (float) Math.cos(engineOutputRad) * ENGINE_EMISSION_DIST;
        float y = source.y + (float) Math.sin(engineOutputRad) * ENGINE_EMISSION_DIST;
        Vector2 dest = new Vector2(x, y);

        // TODO: Spray
        emissionSource.emit(world, source, dest, energy);
    }

    @Override
    public boolean requiresInput() {
        return true;
    }

    @Override
    public void keyPressed() {
        engineEnabled = !engineEnabled;
    }

    @Override
    public void update(float seconds) {
        // TODO Auto-generated method stub

        // TODO: Always burn fuel while on, even inactive
    }

    @Override
    public void render(ShapeRenderer renderer, RenderLayer layer) {
        // TODO Auto-generated method stub

    }

}
