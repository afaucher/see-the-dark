package com.beanfarmergames.seethedark.sensors;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.RayCastCallback;
import com.badlogic.gdx.utils.Array;
import com.beanfarmergames.seethedark.game.BodyData;
import com.beanfarmergames.seethedark.game.Emission;
import com.beanfarmergames.seethedark.util.AgedElement;
import com.beanfarmergames.seethedark.util.AgingList;

public class SensorAccumlator implements RayCastCallback {
    private List<SensorHit> hits = new ArrayList<SensorHit>();
    private List<Emission> emissions = new ArrayList<Emission>();
    
    private AgingList<SensorHit> agedHits = new AgingList<SensorHit>(0.5f);
    private AgingList<Emission> agedEmissions = new AgingList<Emission>(5f);

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
    
    public Collection<Emission> getReceivedEmissions() {
        return agedEmissions.getElementCollection();
    }
    
    public Collection<AgedElement<Emission>> getReceivedEmissions(float t) {
        return agedEmissions.getScaledAgedCollection(t);
    }
    
    public Collection<SensorHit> getHits() {
        return agedHits.getElementCollection();
    }
    
    public List<AgedElement<SensorHit>> getHits(float t) {
        return agedHits.getScaledAgedCollection(t);
    }


    public void accumulateEmissions(Body body) {
        
        
        Array<Fixture> fixtures = body.getFixtureList();
        for (Fixture fixture : fixtures) {
            BodyData bodyData = (BodyData) fixture.getUserData();
            if (bodyData == null) {
                continue;
            }
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
    
    public void age(float clockSeconds) {
        agedHits.appendCollection(clockSeconds, hits);
        agedHits.purge(clockSeconds);
        hits.clear();
        
        agedEmissions.appendCollection(clockSeconds, emissions);
        agedEmissions.purge(clockSeconds);
        emissions.clear();
    }
    
}