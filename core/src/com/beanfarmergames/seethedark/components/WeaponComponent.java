package com.beanfarmergames.seethedark.components;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.Transform;
import com.badlogic.gdx.physics.box2d.World;
import com.beanfarmergames.seethedark.components.configuration.WeaponConfiguration;
import com.beanfarmergames.seethedark.game.EmissionSource;
import com.beanfarmergames.seethedark.game.EmissionSource.EmissionPowerDropoff;
import com.beanfarmergames.seethedark.game.RenderLayer;
import com.beanfarmergames.seethedark.ship.ShipSection;
import com.beanfarmergames.seethedark.style.ColorPalate;
import com.beanfarmergames.seethedark.style.FontPalate;
import com.beanfarmergames.seethedark.util.CommonUtils;
import com.beanfarmergames.seethedark.util.PhysicsUtil;

public class WeaponComponent extends AbstractComponent {

    private boolean weaponEnabled = false;
    private float weaponStartAngleRad;
    private static float weaponTargetRadius = 5.0f;
    private static final Vector2 BASE_ANGLE = new Vector2(1, 0);
    
    private final WeaponConfiguration configuration;

    private Vector2 weaponTarget = null;
    // TODO: Will leak when destroyed
    private SpriteBatch spriteBatch = new SpriteBatch();

    private EmissionSource emissionSource = new EmissionSource(EmissionPowerDropoff.LINEAR);

    public WeaponComponent(WeaponConfiguration configuration, float weaponMidAngleRad) {
    	this.configuration = configuration;
    	this.weaponStartAngleRad = weaponMidAngleRad - configuration.getWeaponArcRad() / 2.0f;
    }

    @Override
    public ComponentType getComponentType() {
        return ComponentType.Weapon;
    }

    @Override
    public Rectangle drawHud(ShapeRenderer renderer, Rectangle destAvailable) {

        float height = destAvailable.getHeight();

        renderer.begin(ShapeType.Line);
        renderer.setColor(weaponEnabled ? ColorPalate.ACTIVE_HUD : ColorPalate.INACTIVE_HUD);
        renderer.line(destAvailable.x, destAvailable.y, destAvailable.x + height, destAvailable.y + height);
        renderer.line(destAvailable.x, destAvailable.y, destAvailable.x + height / 2, destAvailable.y + height);
        renderer.line(destAvailable.x, destAvailable.y, destAvailable.x + height, destAvailable.y + height / 2);

        renderer.end();

        Rectangle result = new Rectangle(destAvailable);
        result.setWidth(height);

        return result;
    }

    @Override
    public boolean requiresHud() {
        return true;
    }

    @Override
    public boolean requiresInput() {
        return true;
    }

    @Override
    public void keyPressed() {
        weaponEnabled = !weaponEnabled;
        if (!weaponEnabled) {
            weaponTarget = null;
        }
    }

    @Override
    public void update(float seconds) {
        if (weaponTarget != null && !canTarget(weaponTarget)) {
            // Clear the targeting computer if target goes out of range
            weaponTarget = null;
            // Gdx.app.log("Cleared target", null);
        }
    }

    public void fireWeapon() {
        if (weaponTarget == null) {
            return;
        }
        Fixture fixture = getMountedFixture();
        ShipSection section = getMountedSection();

        if (!weaponEnabled || section.getHullIntegrity() <= 0) {
            return;
        }

        // Do transform between body & fixture offset
        Transform transform = PhysicsUtil.getWorldFixturePositionTransform(fixture);
        // Final position
        Vector2 rayStart = transform.getPosition().cpy();

        World world = fixture.getBody().getWorld();

        // TODO: Project the target out, we should fire as far as we can in case
        // we miss.
        emissionSource.emit(world, rayStart, weaponTarget, configuration.getWeaponEmissionPower());
        section.accumlateHeat(configuration.getWeaponEmissionPower() *configuration.getWeaponHeatPerJoule());
    }

    public boolean canTarget(final Vector2 targetWorldCoordinate) {

        Transform transform = PhysicsUtil.getWorldFixturePositionTransform(getMountedFixture());
        Vector2 currentWorldPos = transform.getPosition().cpy();
        float dst2 = currentWorldPos.dst2(targetWorldCoordinate);
        if (dst2 > Math.pow(configuration.getWeaponRange(), 2)) {
            return false;
        }

        Vector2 relativeVector = targetWorldCoordinate.cpy().sub(currentWorldPos);
        float angleRad = BASE_ANGLE.angleRad(relativeVector);
		return CommonUtils.doesRadianRangeContain(weaponStartAngleRad
				+ transform.getRotation(), configuration.getWeaponArcRad(),
				angleRad);
    }

    public void aimWeapon(final Vector2 worldCoordinate) {
        if (worldCoordinate != null && canTarget(worldCoordinate)) {
            this.weaponTarget = worldCoordinate.cpy();
        } else {
            this.weaponTarget = null;
        }
    }

    @Override
    public void render(ShapeRenderer renderer, RenderLayer layer) {
        if (!RenderLayer.SENSOR_GUIDE.equals(layer) || !weaponEnabled) {
            return;
        }
        // Scanner
        renderer.begin(ShapeType.Line);
        renderer.setColor(ColorPalate.WEAPON_RANGE);
        Transform transform = PhysicsUtil.getWorldFixturePositionTransform(getMountedFixture());
        Vector2 sensorBase = transform.getPosition().cpy();
        float startArcRad = transform.getRotation() + weaponStartAngleRad;
        float startArcDeg = startArcRad * MathUtils.radiansToDegrees;
        float scannerArcDeg = configuration.getWeaponArcRad() * MathUtils.radiansToDegrees;
        renderer.arc(sensorBase.x, sensorBase.y, configuration.getWeaponRange(), startArcDeg, scannerArcDeg);

        if (weaponTarget != null) {
            renderer.setColor(ColorPalate.WEAPON_AIMER);
            renderer.circle(weaponTarget.x, weaponTarget.y, weaponTargetRadius);
        }

        renderer.end();

        if (weaponTarget != null) {
            spriteBatch.begin();
            FontPalate.HUD_FONT.draw(spriteBatch, "Target: " + weaponTarget.x + "," + weaponTarget.y, weaponTarget.x,
                    weaponTarget.y);
            spriteBatch.end();
        }

    }

}
