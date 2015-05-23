package com.beanfarmergames.seethedark.ship;

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
import com.beanfarmergames.seethedark.components.BeaconComponent;
import com.beanfarmergames.seethedark.components.Component;
import com.beanfarmergames.seethedark.components.Component.ComponentType;
import com.beanfarmergames.seethedark.components.EngineComponent;
import com.beanfarmergames.seethedark.components.EngineContribution;
import com.beanfarmergames.seethedark.components.EngineControlComponent;
import com.beanfarmergames.seethedark.components.FuelComponent;
import com.beanfarmergames.seethedark.components.FuelControlComponent;
import com.beanfarmergames.seethedark.components.SensorComponent;
import com.beanfarmergames.seethedark.components.WeaponComponent;
import com.beanfarmergames.seethedark.components.configuration.PartList;
import com.beanfarmergames.seethedark.entities.Beacon;
import com.beanfarmergames.seethedark.game.Emission;
import com.beanfarmergames.seethedark.game.Player;
import com.beanfarmergames.seethedark.game.RenderLayer;
import com.beanfarmergames.seethedark.game.TwoAxisControl;
import com.beanfarmergames.seethedark.game.field.Field;
import com.beanfarmergames.seethedark.game.field.FieldUpdateCallback;
import com.beanfarmergames.seethedark.game.field.RenderCallback;
import com.beanfarmergames.seethedark.sensors.SensorAccumlator;
import com.beanfarmergames.seethedark.sensors.SensorHit;
import com.beanfarmergames.seethedark.style.ColorPalate;
import com.beanfarmergames.seethedark.style.FontPalate;
import com.beanfarmergames.seethedark.util.AgedElement;
import com.beanfarmergames.seethedark.util.AgedElementComparator;

public class Ship implements FieldUpdateCallback, RenderCallback {

    private Body body;

    private TwoAxisControl controls = null;
    private SensorAccumlator sensorAccumulator = new SensorAccumlator();

    private List<Component> components = new ArrayList<Component>();
    private List<Component> immutableComponents = Collections.unmodifiableList(components);

    private FuelControlComponent fuelControl = null;
    private EngineControlComponent engineControl = null;

    private CompoundShip ship = null;
    private final Field field;

    //DRAWING SENSOR
    private static final float ACTIVE_SENSOR_HIT_RADIUS = 5.0f;
    private static final float PASSIVE_SENSOR_MAXIUM_POWER = 100.0f;
    private static final float PASSIVE_SENSOR_MIN_RADIUS = 3.0f;

    
    private static final float WEAPON_TARGET_MID_ARC_RAD = MathUtils.PI / 3;

    // This is used to sort sensor hits to show freshest on top
    private static AgedElementComparator<SensorHit> sensorComparator = new AgedElementComparator<SensorHit>();

    private static final ShipFactory factory = new StaticShipFactory();
    // TODO: Will leak when destroyed
    SpriteBatch spriteBatch = new SpriteBatch();

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
        
        SensorComponent frontSensor = new SensorComponent(PartList.SENSOR_DS_M2, 0);
        frontSensor.mountToSection(this, firstSection);
        components.add(frontSensor);

        
        SensorComponent rearSensor = new SensorComponent(PartList.SENSOR_CA_MI, MathUtils.PI);
        rearSensor.mountToSection(this, secondSection);

        components.add(rearSensor);

        fuelControl = new FuelControlComponent();
        fuelControl.mountToSection(this, null);

        components.add(fuelControl);

        FuelComponent fuel = new FuelComponent(PartList.FULE_MED, PartList.FULE_MED.getFuelCapacity() / 2.0f);
        fuel.mountToSection(this, secondSection);

        components.add(fuel);

        EngineComponent engine = new EngineComponent(PartList.ENGINE_MD_MII);
        engine.mountToSection(this, secondSection);

        components.add(engine);

        engineControl = new EngineControlComponent();
        engineControl.mountToSection(this, null);

        components.add(engineControl);

        WeaponComponent weaponOne = new WeaponComponent(PartList.WEAPON_PD_MII, WEAPON_TARGET_MID_ARC_RAD);
        weaponOne.mountToSection(this, firstSection);

        components.add(weaponOne);

        WeaponComponent weaponTwo = new WeaponComponent(PartList.WEAPON_PD_MII, -WEAPON_TARGET_MID_ARC_RAD);
        weaponTwo.mountToSection(this, firstSection);

        components.add(weaponTwo);

        BeaconComponent beacon = new BeaconComponent(this.sensorAccumulator);
        beacon.mountToSection(this, firstSection);

        components.add(beacon);

        // Self Register

        field.registerUpdateCallback(this);
        field.registerRenderCallback(this);
        field.spawnShip(this);
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

        sensorAccumulator.accumulateEmissions(body);
        float clockSeconds = field.getGameClockSeconds();
        sensorAccumulator.age(clockSeconds);

    }

    @Override
    public void render(ShapeRenderer renderer, RenderLayer layer, Player player) {

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

        float clockSeconds = field.getGameClockSeconds();

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

                    spriteBatch.setProjectionMatrix(renderer.getProjectionMatrix());

                    spriteBatch.begin();
                    FontPalate.HUD_FONT.draw(spriteBatch, b.getName(), beaconLocation.x + horizontalOffset * 2,
                            beaconLocation.y + verticalOffset * 3);
                    spriteBatch.end();
                }

            }

            renderer.end();
        }

    }

    private static void renderSensors(ShapeRenderer renderer, float clockSeconds, SensorAccumlator sensorAccumulator) {
        renderer.begin(ShapeType.Filled);
        List<AgedElement<SensorHit>> agedHits = sensorAccumulator.getHits(clockSeconds);
        Collections.sort(agedHits, sensorComparator);
        for (AgedElement<SensorHit> agedHit : agedHits) {
            SensorHit hit = agedHit.getE();
            Vector2 hitLocation = hit.hitLocation;
            Color hitColor = null;
            if (hit.data != null) {
                hitColor = hit.data.getMaterialColor();
            } else {
                hitColor = ColorPalate.ACTIVE_SENSOR_HITS;
            }
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
