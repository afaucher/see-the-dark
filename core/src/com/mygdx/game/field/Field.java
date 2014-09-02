package com.mygdx.game.field;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Array;
import com.mygdx.game.FieldUpdateCallback;
import com.mygdx.game.RenderLayer;
import com.mygdx.game.TwoAxisControl;
import com.mygdx.game.ship.Ship;

public class Field {
    private World world;
    @SuppressWarnings("unused")
    private long gameTime;
    private List<Ship> ships = null;
    private List<Ship> immutableShips = null;
    private List<FieldUpdateCallback> updates = null;
    private static final boolean GRAVITY_ENABLED = false;

    // public static final double G = 6.67300E-11;
    // Extreme gravity!
    public static final double G = 1.0f;

    private Array<Body> gravityBodyArray = new Array<Body>(false, 100, Body.class);

    public void resetLevel(TwoAxisControl playerOne) {
        Vector2 gravity = new Vector2(0.0f, 0.0f);
        boolean doSleep = true;
        world = new World(gravity, doSleep);

        gameTime = 0;

        Vector2 spwanOne = new Vector2(0, 0);
        Vector2 spwanTwo = new Vector2(100, 100);

        ships = new ArrayList<Ship>();
        immutableShips = Collections.unmodifiableList(ships);

        ships.add(new Ship(world, playerOne, spwanOne));
        ships.add(new Ship(world, new TwoAxisControl(), spwanTwo));

        FieldLayout fieldLayout = new RandomField();
        updates = fieldLayout.populateField(world);
    }

    public List<Ship> getShips() {
        return immutableShips;
    }

    private void applyBodyGravity(World w) {
        if (!GRAVITY_ENABLED)
            return;
        world.getBodies(gravityBodyArray);

        if (gravityBodyArray.items == null)
            return;

        for (Body gravitySource : gravityBodyArray.items) {
            if (gravitySource == null)
                continue;

            float sourceMass = gravitySource.getMass();
            if (sourceMass <= 0)
                continue;
            Vector2 sourcePosition = gravitySource.getPosition();
            for (Body gravityDest : gravityBodyArray.items) {
                if (gravityDest == null)
                    continue;
                if (gravitySource == gravityDest)
                    continue;

                float destMass = gravityDest.getMass();
                if (destMass <= 0)
                    continue;
                Vector2 destPosition = gravityDest.getPosition();
                float distance2 = sourcePosition.dst2(destPosition);
                if (distance2 == 0)
                    continue;
                double force = G * sourceMass * destMass / distance2;
                if (force <= 0)
                    continue;

                Vector2 forceV = sourcePosition.sub(destPosition).nor().scl((float) force);

                // TODO: This is N^2 and we will compute every value 2x, we can
                // do better

                gravityDest.applyForceToCenter(forceV, true);
            }
        }
    }

    /**
     * Called to advance the game's state by the specified number of
     * milliseconds. iters is the number of times to call the Box2D World.step
     * method; more iterations produce better accuracy. After updating physics,
     * processes element collisions, calls tick() on every FieldElement, and
     * performs scheduled actions.
     */
    public void tick(long msecs, int iters) {
        float seconds = (msecs / 1000.0f);
        float dt = seconds / iters;

        for (int i = 0; i < iters; i++) {
            // clearBallContacts();
            world.step(dt, 10, 10);
            // processBallContacts();
        }

        gameTime += msecs;
        // processElementTicks();

        applyBodyGravity(world);

        for (Ship s : ships) {
            s.update(seconds);
        }

        for (FieldUpdateCallback callback : updates) {
            callback.updateCallback(seconds);
        }
    }

    public void render(ShapeRenderer renderer, RenderLayer layer) {
        // TODO: Until we know who we are rendering, just render the first as
        // local
        for (Ship s : ships) {
            s.render(renderer, ships.get(0) == s, layer);
        }
    }
}
