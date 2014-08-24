package com.mygdx.game.ship.components;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.Rectangle;
import com.mygdx.game.ColorPalate;
import com.mygdx.game.EmissionSource;
import com.mygdx.game.RenderLayer;
import com.mygdx.game.EmissionSource.EmissionPowerDropoff;

public class EngineComponent extends AbstractComponent {

	boolean engineEnabled = false;
	
	private EmissionSource emissionSource = new EmissionSource(
			EmissionPowerDropoff.EXPONENTIAL);

	@Override
	public ComponentType getComponentType() {
		return ComponentType.Engine;
	}

	@Override
	public Rectangle drawHud(ShapeRenderer renderer, Rectangle destAvailable) {
		float height = destAvailable.getHeight();
		float width = height / 2;

		renderer.begin(ShapeType.Filled);
		Color color = engineEnabled ? ColorPalate.ACTIVE_HUD
				: ColorPalate.INACTIVE_HUD;
		renderer.setColor(color);
		renderer.rect(destAvailable.x, destAvailable.y, width, height / 2);
		renderer.rect(destAvailable.x, destAvailable.y + height * 3 / 4, width,
				height / 4);

		renderer.end();

		Rectangle result = new Rectangle(destAvailable);
		result.setWidth(width);

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
		engineEnabled = !engineEnabled;
	}

	@Override
	public void update(float seconds) {
		// TODO Auto-generated method stub

		//TODO: Always burn fuel while on, even inactive
	}

	@Override
	public void render(ShapeRenderer renderer, RenderLayer layer) {
		// TODO Auto-generated method stub

	}

}
