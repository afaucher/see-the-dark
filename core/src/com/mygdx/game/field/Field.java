package com.mygdx.game.field;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

//import com.badlogic.gdx.ai.steer.behaviors.Wander;
//import com.badlogic.gdx.ai.steer.limiters.LinearAccelerationLimiter;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Array;
import com.mygdx.game.RenderLayer;
import com.mygdx.game.TwoAxisControl;
//import com.mygdx.game.ai.ShipSteeringEntity;
import com.mygdx.game.entities.NavPoint;
import com.mygdx.game.mode.GameMode;
import com.mygdx.game.mode.RaceGameMode;
import com.mygdx.game.mode.State;
import com.mygdx.game.ship.Ship;
import com.mygdx.game.ship.components.Component;
import com.mygdx.game.ship.components.Component.ComponentType;
import com.mygdx.game.util.DebbugingParameters;

public class Field {
    private World world;
    private List<Ship> ships = new ArrayList<Ship>();
    private List<NavPoint> navPoints = new ArrayList<NavPoint>();
    private List<Ship> immutableShips = Collections.unmodifiableList(ships);
    private List<FieldUpdateCallback> updateCallbacks = new ArrayList<FieldUpdateCallback>();
    private List<FieldRenderCallback> renderCallbacks = new ArrayList<FieldRenderCallback>();
    private static final boolean GRAVITY_ENABLED = true;
    
    private GameMode gameMode = null;

    // public static final double G = 6.67300E-11;
    // Extreme gravity!
    public static final double G = 1.0f;
    
    private long gameClockMiliseconds = 0;

    private Array<Body> gravityBodyArray = new Array<Body>(false, 100, Body.class);
    
    //ShipSteeringEntity sse;
    
    public GameMode getGameMode() {
        return gameMode;
    }
    
    private void addAIShip() {
        Vector2 spwanTwo = new Vector2(100, 100);
        
        TwoAxisControl aiControl = new TwoAxisControl();
        
        Ship aiShip = new Ship(this, aiControl, spwanTwo);
        for (Component c : aiShip.getComponents()) {
            if (ComponentType.Engine.equals(c.getComponentType())) {
                //Fixme: This is super hacky to turn on engines
                c.keyPressed();
            }
        }
        
        ships.add(aiShip);
        
        /*sse = new ShipSteeringEntity(aiShip, aiControl); 
        
        //FIXME: THis is completely bogus
        Wander<Vector2> wanderSB = new Wander<Vector2>(sse) //
                // Don't use Face internally because independent facing is off
                .setFaceEnabled(false) //
                // We don't need a limiter supporting angular components because Face is not used
                // No need to call setAlignTolerance, setDecelerationRadius and setTimeToTarget for the same reason
                .setLimiter(new LinearAccelerationLimiter(1)) //
                .setWanderOffset(60) //
                .setWanderOrientation(10) //
                .setWanderRadius(40) //
                .setWanderRate(MathUtils.PI / 5);
        
        sse.setSteeringBehavior(wanderSB);
        
        registerUpdateCallback(sse);*/
    }
    
    public void addNavPoint(NavPoint navPoint) {
        navPoints.add(navPoint);
    }

    public void resetLevel(TwoAxisControl playerOne) {
        Vector2 gravity = new Vector2(0.0f, 0.0f);
        boolean doSleep = true;
        world = new World(gravity, doSleep);
        updateCallbacks.clear();
        renderCallbacks.clear();
        navPoints.clear();

        Vector2 spwanOne = new Vector2(0, 0);
        

        ships.clear();
        

        Ship s = new Ship(this, playerOne, spwanOne);
        ships.add(s);
        
        addAIShip();


        FieldLayout fieldLayout = new RandomField();
        
        fieldLayout.populateField(this);
        
        gameMode = new RaceGameMode(this, new ArrayList<NavPoint>(navPoints));
        
        gameMode.setGameState(State.Playing);
    }
    
    public void registerUpdateCallback(FieldUpdateCallback updateCallback) {
        updateCallbacks.add(updateCallback);
    }
    
    public void registerRenderCallback(FieldRenderCallback renderCallback) {
        renderCallbacks.add(renderCallback);
    }

    public List<Ship> getShips() {
        return immutableShips;
    }
    
    public World getWorld() {
        return world;
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
    
    public float getGameClockSeconds() {
        return gameClockMiliseconds / 1000.0f;
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
        
        gameClockMiliseconds += msecs;
        
        float dt = seconds / iters;

        for (int i = 0; i < iters; i++) {
            world.step(dt, 10, 10);
        }

        applyBodyGravity(world);

        for (FieldUpdateCallback callback : updateCallbacks) {
            callback.updateCallback(seconds);
        }
    }
    
    public boolean shouldDrawShipInFull(Ship s) {
        boolean drawFull = (ships.get(0) == s) || DebbugingParameters.DRAW_ALL_SHIPS;
        return drawFull;
    }

    public void render(ShapeRenderer renderer, RenderLayer layer) {
        for (FieldRenderCallback callback : renderCallbacks) {
            callback.render(renderer, layer);
        }
    }
}
