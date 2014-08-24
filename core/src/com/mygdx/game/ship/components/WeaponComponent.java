package com.mygdx.game.ship.components;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.Rectangle;
import com.mygdx.game.ColorPalate;
import com.mygdx.game.EmissionSource;
import com.mygdx.game.RenderLayer;
import com.mygdx.game.EmissionSource.EmissionPowerDropoff;

public class WeaponComponent extends AbstractComponent {
	


	@Override
	public ComponentType getComponentType() {
		return ComponentType.Weapon;
	}

	@Override
	public Rectangle drawHud(ShapeRenderer renderer, Rectangle destAvailable) {

		float height = destAvailable.getHeight();

		renderer.begin(ShapeType.Line);
		renderer.setColor(ColorPalate.ACTIVE_HUD);
		renderer.line(destAvailable.x, destAvailable.y, destAvailable.x
				+ height, destAvailable.y + height);
		renderer.line(destAvailable.x, destAvailable.y, destAvailable.x
				+ height / 2, destAvailable.y + height);
		renderer.line(destAvailable.x, destAvailable.y, destAvailable.x
				+ height, destAvailable.y + height / 2);

		renderer.end();

		return null;
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

	}

	@Override
	public void update(float seconds) {
		// TODO Auto-generated method stub

	}

	@Override
	public void render(ShapeRenderer renderer, RenderLayer layer) {
		// TODO Auto-generated method stub

	}

}
