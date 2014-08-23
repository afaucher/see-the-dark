package com.mygdx.game.ship;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.Transform;
import com.badlogic.gdx.physics.box2d.World;
import com.mygdx.game.ColorPalate;
import com.mygdx.game.RenderLayer;
import com.mygdx.game.SensorAccumlator;
import com.mygdx.game.Ship;

public class SensorComponent implements Component {

	private boolean sensorEnabled = true;
	private float sensorRadius;
	private float sensorStartAngle;
	private float sensorStepAngle;
	private int sensorSteps;

	private Fixture origin = null;
	private Ship ship = null;

	public void mountToFixture(Ship ship, Fixture origin) {
		this.ship = ship;
		this.origin = origin;
	}

	public SensorComponent(float sensorRadius, float sensorStartAngle,
			float sensorStepAngle, int sensorSteps) {
		this.sensorRadius = sensorRadius;
		this.sensorStartAngle = sensorStartAngle;
		this.sensorStepAngle = sensorStepAngle;
		this.sensorSteps = sensorSteps;
	}

	@Override
	public ComponentType getComponentType() {
		return ComponentType.Sensor;
	}

	@Override
	public Rectangle drawHud(ShapeRenderer renderer, Rectangle destAvailable) {

		float height = destAvailable.getHeight();

		renderer.begin(ShapeType.Filled);
		Color color = sensorEnabled ? ColorPalate.ACTIVE_HUD
				: ColorPalate.INACTIVE_HUD;
		renderer.setColor(color);
		renderer.arc(destAvailable.x, 0, height, 30, 30);
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
		sensorEnabled = !sensorEnabled;
	}

	private Vector2[] getScanRayEnds(Vector2 rayStart, float angle) {
		Vector2[] result = new Vector2[sensorSteps];

		// Main Scanner
		for (int step = 0; step < sensorSteps; step++) {
			float offset = sensorStartAngle + step * sensorStepAngle;

			float rayEndX = rayStart.x + (float) Math.cos(angle + offset)
					* sensorRadius;
			float rayEndY = rayStart.y + (float) Math.sin(angle + offset)
					* sensorRadius;
			result[step] = new Vector2(rayEndX, rayEndY);
		}

		return result;
	}

	public void render(ShapeRenderer renderer, RenderLayer layer) {
		if (!RenderLayer.SENSOR_GUIDE.equals(layer) || !sensorEnabled) {
			return;
		}
		// Scanner
		renderer.begin(ShapeType.Line);
		renderer.setColor(ColorPalate.SCANNER);
		Transform transform = getTransform();
		Vector2 sensorBase = transform.getPosition();
		float startArcRad = transform.getRotation() + sensorStartAngle;
		float startArcDeg = startArcRad * MathUtils.radiansToDegrees;
		float scannerArcDeg = sensorStepAngle * sensorSteps
				* MathUtils.radiansToDegrees;
		renderer.arc(sensorBase.x, sensorBase.y, sensorRadius, startArcDeg,
				scannerArcDeg);
		renderer.end();

	}

	private Transform getTransform() {
		Vector2 vec = new Vector2();
		Body body = origin.getBody();
		Transform transform = body.getTransform();
		CircleShape shape = (CircleShape) origin.getShape();
		vec.set(shape.getPosition());
		transform.setPosition(transform.mul(vec));
		return transform;
	}

	@Override
	public void update(float seconds) {
		if (!sensorEnabled) {
			return;
		}

		// Do transform between body & fixture offset
		Transform transform = getTransform();
		// Final position
		Vector2 rayStart = transform.getPosition().cpy();
		float angle = transform.getRotation();

		World world = origin.getBody().getWorld();

		Vector2[] rayEnds = getScanRayEnds(rayStart, angle);
		SensorAccumlator sensorAccumulator = ship.getSensorAccumulator();
		for (Vector2 rayEnd : rayEnds) {
			world.rayCast(sensorAccumulator, rayStart, rayEnd);
		}
	}

}
