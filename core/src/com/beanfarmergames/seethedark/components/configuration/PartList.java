package com.beanfarmergames.seethedark.components.configuration;

import com.badlogic.gdx.math.MathUtils;

public class PartList {

	// SENSORS
	private static final float FOWARD_SCAN_RADIUS = 300.0f;
	private static final float REAR_SCAN_RADIUS = FOWARD_SCAN_RADIUS / 2;
	private static final float SCAN_HALF_ARC_RAD = 1.0f;
	private static final float SCAN_ARC_RAD = SCAN_HALF_ARC_RAD * 2.0f;
	private static final float READ_SCAN_ARC_RAD = MathUtils.PI;
	private static final int SCAN_SLICES = 20;
	private static final float SENSOR_SCAN_POWER = 100.0f;
    
    //ENGINE
    private static final float FUEL = 1000000000;
    private static final float HEAT_PER_FUEL_JOULE = 0.0000075f;
    private static final float TORQUE_JOULE = 2000000.0f;
    private static final float THRUST_JOULE = 5000000.0f;
    
    //WEAPON

    private static final float WEAPON_EMISSION_POWER = 100000.0f;
    // private static final float WEAPON_RADIUS = 1000.0f;
    private static final float HEAT_PER_WEAPON_JOULE = 0.1f;
    private static final float WEAPON_TARGET_RANGE = 250.0f;
    private static final float WEAPON_TARGET_ARC = MathUtils.PI / 3;

	public static final SensorConfiguration SENSOR_DS_M2 = new SensorConfiguration();
	static {
		SENSOR_DS_M2.setConfigurationName("Dark Seer MII");
		SENSOR_DS_M2.setSensorEmissionMaxPower(SENSOR_SCAN_POWER);
		SENSOR_DS_M2.setSensorRadius(FOWARD_SCAN_RADIUS);
		SENSOR_DS_M2.setSensorSteps(SCAN_SLICES);
		SENSOR_DS_M2.setSensorSweepAngleRad(SCAN_ARC_RAD);

	}

	public static final SensorConfiguration SENSOR_CA_MI = new SensorConfiguration();
	static {
		SENSOR_CA_MI.setConfigurationName("Collision Avoidance MI");
		SENSOR_CA_MI.setSensorEmissionMaxPower(SENSOR_SCAN_POWER);
		SENSOR_CA_MI.setSensorRadius(REAR_SCAN_RADIUS);
		SENSOR_CA_MI.setSensorSteps(SCAN_SLICES);
		SENSOR_CA_MI.setSensorSweepAngleRad(READ_SCAN_ARC_RAD);
	}
	
	public static final EngineConfiguration ENGINE_MD_MII = new EngineConfiguration();
	static {
		ENGINE_MD_MII.setConfigurationName("Solid Mass Drive MII");
		ENGINE_MD_MII.setHeatPerUnitFuelJoule(HEAT_PER_FUEL_JOULE);
		ENGINE_MD_MII.setThrustJoule(THRUST_JOULE);
		ENGINE_MD_MII.setTorqueJoule(TORQUE_JOULE);
	}
	
	public static final FuelConfiguration FULE_MED = new FuelConfiguration();
	static {
		FULE_MED.setConfigurationName("Medium Fuel Tank");
		FULE_MED.setFuelCapacity(FUEL);
	}
	
	public static final WeaponConfiguration WEAPON_PD_MII = new WeaponConfiguration();
	static {
		WEAPON_PD_MII.setConfigurationName("Point Defence MII");
		WEAPON_PD_MII.setWeaponArcRad(WEAPON_TARGET_ARC);
		WEAPON_PD_MII.setWeaponEmissionPower(WEAPON_EMISSION_POWER);
		WEAPON_PD_MII.setWeaponHeatPerJoule(HEAT_PER_WEAPON_JOULE);
		WEAPON_PD_MII.setWeaponRange(WEAPON_TARGET_RANGE);
	}

}
