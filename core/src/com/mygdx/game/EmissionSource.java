package com.mygdx.game;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.RayCastCallback;
import com.badlogic.gdx.physics.box2d.World;

public class EmissionSource implements RayCastCallback {

    public enum EmissionPowerDropoff {
        EXPONENTIAL, LINEAR, NONE;
    }

    private Vector2 source = null;
    private float emissionPower = 0;
    private EmissionPowerDropoff dropoff;

    public EmissionSource(EmissionPowerDropoff dropoff) {
        this.dropoff = dropoff;
    }

    public void emit(World world, Vector2 source, Vector2 dest, float emissionPower) {
        this.source = source;
        this.emissionPower = emissionPower;

        world.rayCast(this, source, dest);
    }

    @Override
    public float reportRayFixture(Fixture fixture, Vector2 point, Vector2 normal, float fraction) {
        BodyData hit = null;

        float effectivePower = 0;
        switch (dropoff) {
        case EXPONENTIAL: {
            float distanceSquared = source.dst2(point);
            effectivePower = emissionPower / distanceSquared;
            break;
        }
        case LINEAR: {
            effectivePower = emissionPower * fraction;
            break;
        }
        case NONE: {
            effectivePower = emissionPower;
            break;
        }
        }

        if (fixture.getUserData() != null) {
            hit = (BodyData) fixture.getUserData();
        } else {
            hit = (BodyData) fixture.getBody().getUserData();
        }
        Emission emission = new Emission(source, point, effectivePower);

        hit.receiveEmission(emission);

        return 1;
    }

}