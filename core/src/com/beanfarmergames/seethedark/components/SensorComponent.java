package com.beanfarmergames.seethedark.components;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.Transform;
import com.badlogic.gdx.physics.box2d.World;
import com.beanfarmergames.seethedark.game.EmissionSource;
import com.beanfarmergames.seethedark.game.EmissionSource.EmissionPowerDropoff;
import com.beanfarmergames.seethedark.game.RenderLayer;
import com.beanfarmergames.seethedark.sensors.SensorAccumlator;
import com.beanfarmergames.seethedark.ship.Ship;
import com.beanfarmergames.seethedark.style.ColorPalate;
import com.beanfarmergames.seethedark.util.PhysicsUtil;

public class SensorComponent extends AbstractComponent {

    private boolean sensorEnabled = true;
    private float sensorRadius;
    private float sensorStartAngle;
    private float sensorStepAngle;
    private int sensorSteps;

    private static final float SENSOR_EMISSION_POWER = 100.0f;

    private EmissionSource emissionSource = new EmissionSource(EmissionPowerDropoff.LINEAR);

    public SensorComponent(float sensorRadius, float sensorStartAngle, float sensorStepAngle, int sensorSteps) {
        this.sensorRadius = sensorRadius;
        this.sensorStartAngle = sensorStartAngle;
        this.sensorStepAngle = sensorStepAngle;
        this.sensorSteps = sensorSteps;
    }

    @Override
    public ComponentType getComponentType() {
        return ComponentType.Sensor;
    }

    @Override
    public Rectangle drawHud(ShapeRenderer renderer, Rectangle destAvailable) {

        float height = destAvailable.getHeight();

        renderer.begin(ShapeType.Filled);
        Color color = sensorEnabled ? ColorPalate.ACTIVE_HUD : ColorPalate.INACTIVE_HUD;
        renderer.setColor(color);
        renderer.arc(destAvailable.x, destAvailable.y, height, 30, 30);
        renderer.end();

        Rectangle result = new Rectangle(destAvailable);
        result.setWidth(height);

        return result;
    }

    @Override
    public boolean requiresHud() {
        return true;
    }

    @Override
    public boolean requiresInput() {
        return true;
    }

    @Override
    public void keyPressed() {
        sensorEnabled = !sensorEnabled;
    }

    private Vector2[] getScanRayEnds(Vector2 rayStart, float angle) {
        Vector2[] result = new Vector2[sensorSteps];

        // Main Scanner
        for (int step = 0; step < sensorSteps; step++) {
            float offset = sensorStartAngle + step * sensorStepAngle;

            float rayEndX = rayStart.x + (float) Math.cos(angle + offset) * sensorRadius;
            float rayEndY = rayStart.y + (float) Math.sin(angle + offset) * sensorRadius;
            result[step] = new Vector2(rayEndX, rayEndY);
        }

        return result;
    }

    public void render(ShapeRenderer renderer, RenderLayer layer) {
        if (!RenderLayer.SENSOR_GUIDE.equals(layer) || !sensorEnabled) {
            return;
        }
        // Scanner
        renderer.begin(ShapeType.Line);
        renderer.setColor(ColorPalate.SCANNER);
        Transform transform = PhysicsUtil.getWorldFixturePositionTransform(getMountedFixture());
        Vector2 sensorBase = transform.getPosition();
        float startArcRad = transform.getRotation() + sensorStartAngle;
        float startArcDeg = startArcRad * MathUtils.radiansToDegrees;
        float scannerArcDeg = sensorStepAngle * sensorSteps * MathUtils.radiansToDegrees;
        renderer.arc(sensorBase.x, sensorBase.y, sensorRadius, startArcDeg, scannerArcDeg);
        renderer.end();

    }

    @Override
    public void update(float seconds) {
        if (!sensorEnabled) {
            return;
        }

        Fixture fixture = getMountedFixture();
        Ship ship = getShip();

        // Do transform between body & fixture offset
        Transform transform = PhysicsUtil.getWorldFixturePositionTransform(fixture);
        // Final position
        Vector2 rayStart = transform.getPosition().cpy();
        float angle = transform.getRotation();

        World world = fixture.getBody().getWorld();

        Vector2[] rayEnds = getScanRayEnds(rayStart, angle);
        SensorAccumlator sensorAccumulator = ship.getSensorAccumulator();
        for (Vector2 rayEnd : rayEnds) {
            world.rayCast(sensorAccumulator, rayStart, rayEnd);
            // Sensors emissions stop at the detection threshold. At least you
            // can both see eachother.
            // TODO: This is doing 2x ray casts, once to detect, once to emit
            emissionSource.emit(world, rayStart, rayEnd, SENSOR_EMISSION_POWER);
            // TODO: Generate heat
        }
    }

}
