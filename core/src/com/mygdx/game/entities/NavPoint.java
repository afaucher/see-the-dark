package com.mygdx.game.entities;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.Vector2;
import com.mygdx.game.RenderLayer;
import com.mygdx.game.field.FieldRenderCallback;
import com.mygdx.game.style.ColorPalate;
import com.mygdx.game.style.FontPalate;

public class NavPoint implements FieldRenderCallback {
    
    private Vector2 location;
    private float radius;
    private String name;
    
    public Vector2 getLocation() {
        return location.cpy();
    }
    
    public float getRadius() {
        return radius;
    }
    
    public NavPoint(Vector2 location, float radius, String name) {
        this.location = location;
        this.radius = radius;
        this.name = name;
    }

    @Override
    public void render(ShapeRenderer renderer, RenderLayer layer) {
        
        if (!RenderLayer.NAVIGATION_GUIDE.equals(layer)) {
            return;
        }
        
        renderer.begin(ShapeType.Filled);
        
        renderer.setColor(ColorPalate.NAVIGATION_POINT);
        renderer.circle(location.x, location.y, radius);
        
        //TODO: This is a really hacky way to draw a ring
        renderer.setColor(ColorPalate.BACKGROUND);
        renderer.circle(location.x, location.y, radius * 3 / 4);
        
        SpriteBatch spriteBatch = null;
        spriteBatch = new SpriteBatch();
        spriteBatch.setProjectionMatrix(renderer.getProjectionMatrix());

        //TODO: Ideally this would be drawn around the point nearest to the ship
        spriteBatch.begin();
        FontPalate.HUD_FONT.draw(spriteBatch, name, location.x, location.y);
        spriteBatch.end();
        
        renderer.end();
        
    }

}
