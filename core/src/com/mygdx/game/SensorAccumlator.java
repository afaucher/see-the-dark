package com.mygdx.game;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.RayCastCallback;

public class SensorAccumlator implements RayCastCallback {
	private List<SensorHit> hits = new ArrayList<SensorHit>();
	private List<SensorHit> immutableHits = Collections.unmodifiableList(hits);

	public void reset() {
		hits.clear();
	}

	// Returns immutable list
	public List<SensorHit> getHits() {
		return immutableHits;
	}

	@Override
	public float reportRayFixture(Fixture fixture, Vector2 point,
			Vector2 normal, float fraction) {

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
}