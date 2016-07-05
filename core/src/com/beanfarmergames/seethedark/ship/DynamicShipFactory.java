package com.beanfarmergames.seethedark.ship;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.JsonWriter.OutputType;
import com.beanfarmergames.seethedark.components.Component;
import com.beanfarmergames.seethedark.components.EngineComponent;
import com.beanfarmergames.seethedark.components.FuelComponent;
import com.beanfarmergames.seethedark.components.SensorComponent;
import com.beanfarmergames.seethedark.components.WeaponComponent;
import com.beanfarmergames.seethedark.components.Component.ComponentType;
import com.beanfarmergames.seethedark.components.configuration.EngineConfiguration;
import com.beanfarmergames.seethedark.components.configuration.FuelConfiguration;
import com.beanfarmergames.seethedark.components.configuration.PartList;
import com.beanfarmergames.seethedark.components.configuration.PartList.RegisteredComponent;
import com.beanfarmergames.seethedark.components.configuration.SensorConfiguration;
import com.beanfarmergames.seethedark.components.configuration.WeaponConfiguration;
import com.beanfarmergames.seethedark.game.AbstractBodyData;
import com.beanfarmergames.seethedark.style.ColorPalate;

public class DynamicShipFactory {
	private static final String TAG = "FACTORY";

	private static final float BODY_RADIUS = 10.0f;

	private static final float HEAT_LIMIT = 1000;
	private static final float DAMAGE_HEAT_THRESHOLD = HEAT_LIMIT * 9 / 10;
	// TODO: Make part of the specific component configuration
	private static final float HEAT_DAMAGE_PER_SECOND = 0.15f;
	private static final float HEAT_DISAPATED_PER_SECOND = HEAT_LIMIT / 2;

	private class ShipSectionInstance extends AbstractBodyData implements
			ShipSection {

		private float integrity = 1;
		private float heatLimit = 0;
		private Fixture fixture = null;
		private float radius;
		private Vector2 relativePosition;

		public ShipSectionInstance(Vector2 position, float radius,
				float heatLimit, Body body) {
			super(true);

			this.heatLimit = heatLimit;
			this.radius = radius;
			this.relativePosition = position.cpy();

			CircleShape sd = new CircleShape();
			sd.setRadius(radius);
			sd.setPosition(position);

			FixtureDef fdef = new FixtureDef();
			fdef.shape = sd;
			fdef.density = 1.0f;
			fdef.friction = 0.5f;
			fdef.restitution = 0.6f;

			fixture = body.createFixture(fdef);
			fixture.setUserData(this);
		}

		@Override
		public Color getMaterialColor() {
			return ColorPalate.SHIP;
		}

		@Override
		public Fixture getFixture() {
			return fixture;
		}

		@Override
		public float getHullIntegrity() {
			return integrity;
		}

		@Override
		public float getHeatLimit() {
			return heatLimit;
		}

		@Override
		public void update(float seconds) {
			if (getTemperature() > DAMAGE_HEAT_THRESHOLD) {
				integrity = Math.max(0, integrity
						- (HEAT_DAMAGE_PER_SECOND * seconds));
			}
			disapateHeat(HEAT_DISAPATED_PER_SECOND * seconds);
		}

		@Override
		public float getRadius() {
			return radius;
		}

		@Override
		public Vector2 getRelativePosition() {
			return relativePosition.cpy();
		}

	}

	public class ShipModel {
		String name;
		Body body;
		List<Component> components = new ArrayList<Component>();
		List<ShipSection> sections = new ArrayList<ShipSection>();
	}

	enum ModelDefinition {
		//Root
		ModelProperties, ModelRootPart,
		//Part
		PartRadius, PartChildren, PartParentAngle, PartComponents,
		//Model
		ModelName,
		//Component
		ComponentName,
		ComponentMountingAngle,
	}

	private void buildPart(Ship ship, ShipModel model, ShipSection parent, JsonValue part) {
		Vector2 position = new Vector2(0, 0);
		float radius = part.getFloat(ModelDefinition.PartRadius.name(), BODY_RADIUS);
		if (parent != null) {
			float parentRadius = parent.getRadius();
			Vector2 parentPosition = parent.getRelativePosition();
			float parentAngle = part.getFloat(ModelDefinition.PartParentAngle.name(), 0);
			float totalDistance = radius + parentRadius;
			position.set(parentPosition.x + (float) Math.cos(parentAngle)
					* totalDistance,
					parentPosition.y + (float) Math.sin(parentAngle)
							* totalDistance);
		}

		ShipSectionInstance partSection = new ShipSectionInstance(position,
				radius, HEAT_LIMIT, model.body);
		
		model.sections.add(partSection);

		JsonValue childParts = part.get(ModelDefinition.PartChildren.name());
		if (childParts != null && childParts.isArray()) {
			JsonValue child = childParts.child;
			while (child != null) {
				buildPart(ship, model, partSection, child);
				child = child.next;
			}
		}
		
		JsonValue components = part.get(ModelDefinition.PartComponents.name());
		if (components != null && components.isArray()) {
			JsonValue component = components.child;
			while (component != null) {
				buildComponent(ship, partSection, model, component);
				component = component.next;
			}
		}
	}
	
	private void buildComponent(Ship ship, ShipSection shipSection, ShipModel model, JsonValue component) {
		//Gdx.app.error(TAG, "Building: " + component.prettyPrint(OutputType.json, 0));
		String componentName = component.getString(ModelDefinition.ComponentName.name());
		for (RegisteredComponent rc : RegisteredComponent.values()) {
			if (rc.getName().equals(componentName)) {
				if (rc.getConfig() instanceof SensorConfiguration) {
					float mountingAngle = component.getFloat(ModelDefinition.ComponentMountingAngle.name(), 0);
					SensorComponent sensor = new SensorComponent((SensorConfiguration)rc.getConfig(), mountingAngle);
					//TODO: Components need to know about the ship to interact but here it is irrelivant
					sensor.mountToSection(ship, shipSection);
					model.components.add(sensor);
				} else if (rc.getConfig() instanceof FuelConfiguration) {
					FuelComponent fuel = new FuelComponent(PartList.FULE_MED, PartList.FULE_MED.getFuelCapacity() / 2.0f);
			        fuel.mountToSection(ship, shipSection);
			        model.components.add(fuel);
				} else if (rc.getConfig() instanceof EngineConfiguration) {
					EngineComponent engine = new EngineComponent(PartList.ENGINE_MD_MII);
			        engine.mountToSection(ship, shipSection);
			        model.components.add(engine);
				} else if (rc.getConfig() instanceof WeaponConfiguration) {
					float mountingAngle = component.getFloat(ModelDefinition.ComponentMountingAngle.name(), 0);
					WeaponComponent weapon = new WeaponComponent(PartList.WEAPON_PD_MII, mountingAngle);
					weapon.mountToSection(ship, shipSection);
			        model.components.add(weapon);
				} else {
					throw new RuntimeException("Unknown component");
				}
				return;
			}
		}
	}

	private ShipModel buildModel(JsonValue model, Ship ship) {
		ShipModel shipModel = new ShipModel();

		World world = ship.getField().getWorld();

		BodyDef bd = new BodyDef();
        bd.allowSleep = true;
        Body body = world.createBody(bd);
        body.setBullet(true);
        body.setAngularDamping(0.2f);
        body.setLinearDamping(0.1f);
        body.setType(BodyDef.BodyType.DynamicBody);
        
		shipModel.body = body;

		JsonValue properties = model.get(ModelDefinition.ModelProperties.name());
		shipModel.name = properties.getString(ModelDefinition.ModelName.name());

		return shipModel;
	}

	public ShipModel buildModelForShip(Ship ship) {

		JsonReader r = new JsonReader();
		JsonValue model = r.parse(new FileHandle("ship_test.json"));
		//Gdx.app.error(TAG, model.prettyPrint(OutputType.json, 0));
		ShipModel shipModel = buildModel(model, ship);
		buildPart(ship, shipModel, null,
				model.get(ModelDefinition.ModelRootPart.name()));

		return shipModel;
	}

}
