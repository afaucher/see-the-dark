package com.mygdx.game.ship;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.mygdx.game.RenderLayer;

public interface Component {
	public enum ComponentType {
		Weapon,
		Engine,
		Sensor,
	}
	
	public ComponentType getComponentType();
	
	public Rectangle drawHud(ShapeRenderer renderer, Rectangle destAvailable);
	public boolean requiresHud();
	public boolean requiresInput();
	//public float getRadius();
	
	public void keyPressed();
	
	public void update(float seconds);
	//Render in world coordinates
	public void render(ShapeRenderer renderer, RenderLayer layer);
}
