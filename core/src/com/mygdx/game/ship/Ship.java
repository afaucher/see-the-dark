package com.mygdx.game.ship;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.TimeUtils;
import com.mygdx.game.Emission;
import com.mygdx.game.RenderLayer;
import com.mygdx.game.SensorAccumlator;
import com.mygdx.game.SensorHit;
import com.mygdx.game.TwoAxisControl;
import com.mygdx.game.entities.Beacon;
import com.mygdx.game.field.Field;
import com.mygdx.game.field.FieldRenderCallback;
import com.mygdx.game.field.FieldUpdateCallback;
import com.mygdx.game.ship.components.BeaconComponent;
import com.mygdx.game.ship.components.Component;
import com.mygdx.game.ship.components.Component.ComponentType;
import com.mygdx.game.ship.components.EngineComponent;
import com.mygdx.game.ship.components.EngineContribution;
import com.mygdx.game.ship.components.EngineControlComponent;
import com.mygdx.game.ship.components.FuelComponent;
import com.mygdx.game.ship.components.FuelControlComponent;
import com.mygdx.game.ship.components.SensorComponent;
import com.mygdx.game.ship.components.WeaponComponent;
import com.mygdx.game.style.ColorPalate;
import com.mygdx.game.style.FontPalate;
import com.mygdx.game.util.AgedElement;

public class Ship implements FieldUpdateCallback, FieldRenderCallback {

    private Body body;

    private TwoAxisControl controls = null;
    private SensorAccumlator sensorAccumulator = new SensorAccumlator();

    private List<Component> components = new ArrayList<Component>();
    private List<Component> immutableComponents = Collections.unmodifiableList(components);

    private FuelControlComponent fuelControl = null;
    private EngineControlComponent engineControl = null;

    private CompoundShip ship = null;
    private final Field field;

    private static final float SCAN_RADIUS = 300.0f;
    private static final float GLOBAL_SCAN_RADIUS = SCAN_RADIUS / 2;
    private static final float SCAN_HALF_ARC_RAD = 1.0f;
    private static final float SCAN_ARC_RAD = SCAN_HALF_ARC_RAD * 2.0f;
    private static final int SCAN_SLICES = 20;
    private static final float FUEL = 1000000000;

    private static final float ACTIVE_SENSOR_HIT_RADIUS = 5.0f;
    private static final float PASSIVE_SENSOR_MAXIUM_POWER = 100.0f;
    private static final float PASSIVE_SENSOR_MIN_RADIUS = 3.0f;

    private static final float TORQUE_JOULE = 2000000.0f;
    private static final float THRUST_JOULE = 5000000.0f;

    private static final float WEAPON_TARGET_RANGE = 250.0f;
    private static final float WEAPON_TARGET_START_ARC = MathUtils.PI / 6;
    private static final float WEAPON_TARGET_ARC = MathUtils.PI / 3;

    private static final ShipFactory factory = new StaticShipFactory();

    // Only for the AI impl
    public Body getBody() {
        return body;
    }

    public Ship(Field field, TwoAxisControl controls, Vector2 spwan) {
        this.controls = controls;
        this.field = field;

        World world = field.getWorld();

        BodyDef bd = new BodyDef();
        bd.allowSleep = true;
        bd.position.set(spwan.x, spwan.y);
        body = world.createBody(bd);
        body.setBullet(true);
        body.setAngularDamping(0.2f);
        body.setLinearDamping(0.1f);
        List<ShipSection> sections = factory.buildShip(body);
        ship = new CompoundShip(sections);
        body.setType(BodyDef.BodyType.DynamicBody);

        ShipSection firstSection = sections.get(0);
        ShipSection secondSection = sections.get(1);

        SensorComponent frontSensor = new SensorComponent(SCAN_RADIUS, -SCAN_HALF_ARC_RAD, SCAN_ARC_RAD / SCAN_SLICES,
                SCAN_SLICES);
        frontSensor.mountToSection(this, firstSection);

        components.add(frontSensor);

        SensorComponent rearSensor = new SensorComponent(GLOBAL_SCAN_RADIUS, SCAN_HALF_ARC_RAD,
                (MathUtils.PI2 - SCAN_ARC_RAD) / SCAN_SLICES, SCAN_SLICES);
        rearSensor.mountToSection(this, secondSection);

        components.add(rearSensor);

        fuelControl = new FuelControlComponent();
        fuelControl.mountToSection(this, null);

        components.add(fuelControl);

        FuelComponent fuel = new FuelComponent(FUEL, FUEL);
        fuel.mountToSection(this, secondSection);

        components.add(fuel);

        EngineComponent engine = new EngineComponent(TORQUE_JOULE, THRUST_JOULE);
        engine.mountToSection(this, secondSection);

        components.add(engine);

        engineControl = new EngineControlComponent();
        engineControl.mountToSection(this, null);

        components.add(engineControl);

        WeaponComponent weaponOne = new WeaponComponent(WEAPON_TARGET_RANGE, WEAPON_TARGET_START_ARC, WEAPON_TARGET_ARC);
        weaponOne.mountToSection(this, firstSection);

        components.add(weaponOne);

        WeaponComponent weaponTwo = new WeaponComponent(WEAPON_TARGET_RANGE, MathUtils.PI2 - WEAPON_TARGET_START_ARC
                - WEAPON_TARGET_ARC, WEAPON_TARGET_ARC);
        weaponTwo.mountToSection(this, firstSection);

        components.add(weaponTwo);
        
        BeaconComponent beacon = new BeaconComponent(this.sensorAccumulator);
        beacon.mountToSection(this, firstSection);
        
        components.add(beacon);
        
        field.registerUpdateCallback(this);
        field.registerRenderCallback(this);
    }

    public void aimWeapons(final Vector2 target) {

        for (Component component : components) {
            if (!ComponentType.Weapon.equals(component.getComponentType())) {
                continue;
            }
            WeaponComponent w = (WeaponComponent) component;

            w.aimWeapon(target);
        }
    }

    public void fire() {
        for (Component component : components) {
            if (!ComponentType.Weapon.equals(component.getComponentType())) {
                continue;
            }
            WeaponComponent w = (WeaponComponent) component;

            w.fireWeapon();
        }
    }

    public Beacon getBeacon() {
        for (Component component : components) {
            if (!ComponentType.Beacon.equals(component.getComponentType())) {
                continue;
            }
            BeaconComponent bc = (BeaconComponent) component;
            return bc.getBeacon();
        }
        return null;
    }

    private void updateMovements(float seconds) {
        // TODO: Always wakes player
        EngineContribution potentialContribution = engineControl.getEngineContribution();

        float torqueNewtons = -controls.getX() * potentialContribution.torqueJoule * seconds;
        float thrustNewtons = controls.getY() * potentialContribution.thrustJoule * seconds;

        float fuelToBurn = Math.abs(torqueNewtons) + Math.abs(thrustNewtons);
        fuelToBurn = fuelControl.burnFuel(fuelToBurn);
        if (fuelToBurn > 0) {
            // Out of fuel
            return;
        }

        engineControl.fireEngine(Math.abs(controls.getX()), Math.abs(controls.getY()));

        float angle = getRotation();
        float vX = (float) Math.cos(angle) * thrustNewtons;
        float vY = (float) Math.sin(angle) * thrustNewtons;

        // body.applyAngularImpulse(torque, true);
        body.applyTorque(torqueNewtons, true);
        body.applyForceToCenter(vX, vY, true);
    }

    public float getRotation() {
        return body.getAngle();
    }

    public Vector2 getPosition() {
        return body.getPosition();
    }

    @Override
    public void updateCallback(float seconds) {
        updateMovements(seconds);

        ship.update(seconds);

        // Run logic for all components including sensors, engines, weapons, etc
        for (Component component : components) {
            component.update(seconds);
        }

        // TODO: Use game time, otherwise sensor readings expire while paused
        sensorAccumulator.accumulateEmissions(body);
        float clockSeconds = TimeUtils.nanoTime() / 1000000000.0f;
        sensorAccumulator.age(clockSeconds);

    }

    @Override
    public void render(ShapeRenderer renderer, RenderLayer layer) {

        if (!field.shouldDrawShipInFull(this)) {
            return;
        }

        float x = body.getPosition().x;
        float y = body.getPosition().y;
        float angle = getRotation();

        // Render

        // Scanner
        for (Component component : components) {
            component.render(renderer, layer);
        }

        float clockSeconds = TimeUtils.nanoTime() / 1000000000.0f;

        // Hits
        if (RenderLayer.PASSIVE_SENSOR_HIT.equals(layer)) {

            renderer.begin(ShapeType.Filled);
            for (AgedElement<Emission> agedEmission : sensorAccumulator.getReceivedEmissions(clockSeconds)) {
                Emission emission = agedEmission.getE();
                Vector2 hitLocation = emission.source;
                Color emissionColor = ColorPalate.PASSIVE_SENSOR_HITS.cpy();
                float colorScale = (float) Math.pow(agedEmission.getT(), 0.1);
                emissionColor = emissionColor.cpy();
                emissionColor.lerp(ColorPalate.BACKGROUND, colorScale);
                emissionColor.a = colorScale;
                renderer.setColor(emissionColor);

                float cappedPower = Math.min(emission.power, PASSIVE_SENSOR_MAXIUM_POWER);
                float powerRatio = cappedPower / PASSIVE_SENSOR_MAXIUM_POWER;
                float radius = (float) Math.sqrt(powerRatio);
                radius = Math.max(radius, PASSIVE_SENSOR_MIN_RADIUS);

                renderer.circle(hitLocation.x, hitLocation.y, radius);
            }
            renderer.end();

        }

        if (RenderLayer.SENSOR_HIT.equals(layer)) {
            renderSensors(renderer, clockSeconds, sensorAccumulator);
            
            for (Ship s : field.getShips()) {
                if (s == this) {
                    continue;
                }

                Beacon b = s.getBeacon();
                if (b == null) {
                    continue;
                }
                
                SensorAccumlator beaconAccum = b.getAccumulator();
                if (beaconAccum == null) {
                    continue;
                }
                renderSensors(renderer, clockSeconds, beaconAccum);
            }

        }

        // Ship
        if (RenderLayer.PLAYER_BODY.equals(layer)) {
            renderer.begin(ShapeType.Filled);

            renderer.translate(x, y, 0);
            renderer.rotate(0, 0, 1, angle * MathUtils.radiansToDegrees);

            for (Fixture fixture : body.getFixtureList()) {
                CircleShape cs = (CircleShape) fixture.getShape();
                if (cs == null)
                    continue;
                Vector2 shapePosition = cs.getPosition();

                ShipSection section = (ShipSection) fixture.getUserData();

                renderer.setColor(section.getMaterialColor());
                renderer.circle(shapePosition.x, shapePosition.y, cs.getRadius());

                // Heat Indicator
                renderer.setColor(ColorPalate.SECTION_HEAT_INDICATOR);
                float heatIndicatorRadius = cs.getRadius() * 3 / 4;
                float heatProportion = Math.min(1.0f, section.getTemperature() / section.getHeatLimit());
                renderer.arc(shapePosition.x, shapePosition.y, heatIndicatorRadius, 0, heatProportion * 360);

                // Hull Indicator
                renderer.setColor(ColorPalate.SECTION_HULL_INDICATOR);
                float hullIndicatorRadius = cs.getRadius() / 2;
                renderer.arc(shapePosition.x, shapePosition.y, hullIndicatorRadius, 0, section.getHullIntegrity() * 360);
            }
            renderer.identity();

            renderer.end();
        }

        if (RenderLayer.BEACON.equals(layer)) {

            renderer.begin(ShapeType.Filled);
            renderer.setColor(ColorPalate.NAVIGATION_BEACON);

            for (Ship s : field.getShips()) {
                if (s == this) {
                    continue;
                }

                Beacon b = s.getBeacon();
                if (b == null) {
                    continue;
                }

                Vector2 beaconLocation = b.getLocation();

                float verticalOffset = 5;
                float horizontalOffset = 3;
                renderer.triangle(beaconLocation.x, beaconLocation.y, beaconLocation.x + horizontalOffset,
                        beaconLocation.y + verticalOffset, beaconLocation.x - horizontalOffset, beaconLocation.y
                                + verticalOffset);
                
                if (b.getName() != null) {
                    
                    SpriteBatch spriteBatch = null;
                    spriteBatch = new SpriteBatch();
                    spriteBatch.setProjectionMatrix(renderer.getProjectionMatrix());
    
                    spriteBatch.begin();
                    FontPalate.HUD_FONT.draw(spriteBatch, b.getName(), beaconLocation.x + horizontalOffset * 2, beaconLocation.y + verticalOffset * 3);
                    spriteBatch.end();
                }

            }

            renderer.end();
        }

    }

    private static void renderSensors(ShapeRenderer renderer, float clockSeconds, SensorAccumlator sensorAccumulator) {
        renderer.begin(ShapeType.Filled);
        for (AgedElement<SensorHit> agedHit : sensorAccumulator.getHits(clockSeconds)) {
            SensorHit hit = agedHit.getE();
            Vector2 hitLocation = hit.hitLocation;
            Color hitColor = null;
            //if (hit.data != null) {
            //    hitColor = hit.data.getMaterialColor();
            //} else {
                hitColor = ColorPalate.ACTIVE_SENSOR_HITS;
            //}
            float colorScale = (float) Math.pow(agedHit.getT(), 0.1);
            hitColor = hitColor.cpy();
            // TODO: We should sort by age to make sure we draw in the right
            // order
            // TODO: We should highlight the 'fresh' data.
            hitColor.lerp(ColorPalate.BACKGROUND, colorScale);
            hitColor.a = colorScale;
            renderer.setColor(hitColor);
            renderer.circle(hitLocation.x, hitLocation.y, ACTIVE_SENSOR_HIT_RADIUS);
        }
        renderer.end();
    }

    public SensorAccumlator getSensorAccumulator() {
        return sensorAccumulator;
    }

    public List<Component> getComponents() {
        return immutableComponents;
    }

    public Field getField() {
        return field;
    }
}
