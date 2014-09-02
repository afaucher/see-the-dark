package com.mygdx.game;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.RayCastCallback;
import com.badlogic.gdx.utils.Array;

public class SensorAccumlator implements RayCastCallback {
    private List<SensorHit> hits = new ArrayList<SensorHit>();
    private List<SensorHit> immutableHits = Collections.unmodifiableList(hits);
    private List<Emission> emissions = new ArrayList<Emission>();

    private List<Emission> immutableEmissions = Collections.unmodifiableList(emissions);

    public void resetActiveSensors() {
        hits.clear();
    }

    public void resetPassiveSensors() {
        emissions.clear();
    }

    // Returns immutable list
    public List<SensorHit> getHits() {
        return immutableHits;
    }

    // Returns immutable list
    public List<Emission> getReceivedEmissions() {
        return immutableEmissions;
    }

    @Override
    public float reportRayFixture(Fixture fixture, Vector2 point, Vector2 normal, float fraction) {

        // Copy is needed as box2d reuses it
        SensorHit hit = new SensorHit();
        if (fixture.getUserData() != null) {
            hit.data = (BodyData) fixture.getUserData();
        } else {
            hit.data = (BodyData) fixture.getBody().getUserData();
        }
        hit.hitLocation = point.cpy();
        hit.normal = normal.cpy();
        hits.add(hit);

        return 1;
    }

    public void accumulateEmissions(Body body) {
        Array<Fixture> fixtures = body.getFixtureList();
        for (Fixture fixture : fixtures) {
            BodyData bodyData = (BodyData) fixture.getUserData();
            if (bodyData == null)
                continue;
            List<Emission> bodyEmissions = bodyData.getEmissions();
            this.emissions.addAll(bodyEmissions);
            bodyData.resetEmissions();
        }
        BodyData bodyData = (BodyData) body.getUserData();
        if (bodyData == null)
            return;
        List<Emission> emissions = bodyData.getEmissions();
        emissions.addAll(emissions);

    }
}