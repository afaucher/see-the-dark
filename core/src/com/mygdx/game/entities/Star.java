package com.mygdx.game.entities;

import java.util.Random;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.World;
import com.mygdx.game.AbstractBodyData;
import com.mygdx.game.BodyHelper;
import com.mygdx.game.ColorPalate;
import com.mygdx.game.EmissionSource;
import com.mygdx.game.EmissionSource.EmissionPowerDropoff;
import com.mygdx.game.FieldUpdateCallback;
import com.mygdx.game.field.RandomField;
import com.mygdx.game.util.PhysicsUtil;

public class Star extends AbstractBodyData implements FieldUpdateCallback {

    private static Random RAND = new Random();

    // TODO: Make configurable
    private float emissionRatePerSecond = 1000;
    private float emissionPower = 100000;
    private World world = null;
    private EmissionSource emissionSource = new EmissionSource(EmissionPowerDropoff.EXPONENTIAL);
    private Fixture fixture = null;
    private float radius;
    private float RAY_MAX_RADIUS = 3000.0f;
    private float SUN_TEMP = 1000000.0f;

    public Star(RandomField randomField, World world) {
        super(false);
        this.world = world;

        float x = RAND.nextFloat() * -1000;
        float y = RAND.nextFloat() * -1000;
        radius = RAND.nextFloat() * 100 + 100;

        Body body = BodyHelper.createCircle(world, x, y, radius, true, this);
        fixture = body.getFixtureList().first();

        fixture.setDensity(5);
        body.resetMassData();
    }

    @Override
    public Color getMaterialColor() {
        return ColorPalate.SUN;
    }

    @Override
    public void updateCallback(float seconds) {

        Vector2 root = PhysicsUtil.getWorldFixturePosition(fixture);
        for (int i = 0; i < emissionRatePerSecond * seconds; i++) {

            float exitPointRad = RAND.nextFloat() * MathUtils.PI2;
            Vector2 source = new Vector2(root.x + (float) Math.cos(exitPointRad) * radius, root.y
                    + (float) Math.sin(exitPointRad) * radius);

            float exitAngleRad = exitPointRad + (RAND.nextFloat() * MathUtils.PI) - (MathUtils.PI / 2.0f);

            float destX = source.x + (float) Math.cos(exitAngleRad) * RAY_MAX_RADIUS;
            float destY = source.y + (float) Math.sin(exitAngleRad) * RAY_MAX_RADIUS;
            Vector2 dest = new Vector2(destX, destY);

            emissionSource.emit(world, source, dest, emissionPower);
        }
    }

    @Override
    public float getTemperature() {
        return SUN_TEMP;
    }
}