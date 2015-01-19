package com.mygdx.game.entities;

import com.badlogic.gdx.graphics.g2d.BitmapFont.TextBounds;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.Vector2;
import com.mygdx.game.Player;
import com.mygdx.game.RenderLayer;
import com.mygdx.game.field.RenderCallback;
import com.mygdx.game.style.ColorPalate;
import com.mygdx.game.style.FontPalate;

public class NavPoint implements RenderCallback {
    
    private Vector2 location;
    private float radius;
    private String name;
    //TODO: Will leak when destroyed
    private SpriteBatch spriteBatch = new SpriteBatch();
    
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
    public void render(ShapeRenderer renderer, RenderLayer layer, Player player) {
        
        if (!RenderLayer.NAVIGATION_GUIDE.equals(layer)) {
            return;
        }
        
        renderer.begin(ShapeType.Filled);
        
        renderer.setColor(ColorPalate.NAVIGATION_POINT);
        renderer.circle(location.x, location.y, radius);
        
        //TODO: This is a really hacky way to draw a ring
        renderer.setColor(ColorPalate.BACKGROUND);
        renderer.circle(location.x, location.y, radius * 3 / 4);
        
        renderer.end();
        
        //TODO: Ideally this would be drawn around the point nearest to the ship
        spriteBatch.setProjectionMatrix(renderer.getProjectionMatrix());
        
        spriteBatch.begin();
        
        spriteBatch.setColor(ColorPalate.NAVIGATION_POINT);
        //Draw centered
        TextBounds bounds = FontPalate.HUD_FONT.getBounds(name);
        FontPalate.HUD_FONT.draw(spriteBatch, name, location.x - bounds.width / 2, location.y + bounds.height / 2);
        spriteBatch.end();
    }

}
