package com.mygdx.game.ship.components;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.mygdx.game.RenderLayer;
import com.mygdx.game.ship.Ship;

public class EngineControlComponent extends AbstractComponent {

	@Override
	public ComponentType getComponentType() {
		return ComponentType.EngineControl;
	}

	@Override
	public Rectangle drawHud(ShapeRenderer renderer, Rectangle destAvailable) {
		return null;
	}

	@Override
	public boolean requiresHud() {
		return false;
	}

	@Override
	public boolean requiresInput() {
		return false;
	}

	@Override
	public void keyPressed() {
		// TODO Auto-generated method stub

	}

	@Override
	public void update(float seconds) {
		// TODO Auto-generated method stub

	}

	public List<EngineComponent> getEngines() {
		List<EngineComponent> engineControls = new ArrayList<EngineComponent>();

		Ship ship = getShip();
		List<Component> components = ship.getComponents();

		for (Component c : components) {
			if (c == this)
				continue;
			if (!ComponentType.Engine.equals(c.getComponentType()))
				continue;
			EngineComponent engineControl = (EngineComponent) c;

			engineControls.add(engineControl);
		}

		return engineControls;
	}

	public EngineContribution getEngineContribution() {
		EngineContribution contribution = new EngineContribution(0, 0);
		List<EngineComponent> engines = getEngines();
		for (EngineComponent e : engines) {
			contribution.addEngineContribution(e.getEngineContribution());
		}
		return contribution;
	}

	public void fireEngine(float torqueRatio, float thrustRatio) {
		List<EngineComponent> engines = getEngines();
		for (EngineComponent e : engines) {
			EngineContribution engineContribution = e.getEngineContribution();
			float torqueNewtons = engineContribution.torqueJoule * torqueRatio;
			float thrustNewtons = engineContribution.thrustJoule * thrustRatio;
			e.fireEngineFor(torqueNewtons,
					thrustNewtons);
		}

		//return enginesContribution;
	}

	@Override
	public void render(ShapeRenderer renderer, RenderLayer layer) {
	}

}
