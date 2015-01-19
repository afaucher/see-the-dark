package com.beanfarmergames.seethedark.components;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.Rectangle;
import com.beanfarmergames.seethedark.entities.Beacon;
import com.beanfarmergames.seethedark.game.RenderLayer;
import com.beanfarmergames.seethedark.sensors.SensorAccumlator;
import com.beanfarmergames.seethedark.style.ColorPalate;

public class BeaconComponent extends AbstractComponent {
    
    private boolean enabled = true;
    
    private final SensorAccumlator accumulator;
    
    public BeaconComponent(SensorAccumlator accumulator) {
        this.accumulator = accumulator;
    }
    
    public Beacon getBeacon() {
        if (enabled) {
            return new Beacon(accumulator, "TODO", this.getShip().getPosition().cpy());
        }
        return null;
    }

    @Override
    public ComponentType getComponentType() {
        return ComponentType.Beacon;
    }

    @Override
    public Rectangle drawHud(ShapeRenderer renderer, Rectangle destAvailable) {
        float height = destAvailable.getHeight();

        renderer.begin(ShapeType.Filled);
        renderer.setColor(enabled ? ColorPalate.ACTIVE_HUD : ColorPalate.INACTIVE_HUD);
        
        float innerRadius = 4f;
        float outerRadius = height - innerRadius;
        renderer.circle(destAvailable.x + innerRadius, destAvailable.y + innerRadius, innerRadius);
        renderer.arc(destAvailable.x + innerRadius, destAvailable.y + innerRadius, outerRadius, 30, 60);

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
        enabled = !enabled;
    }

    @Override
    public void update(float seconds) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void render(ShapeRenderer renderer, RenderLayer layer) {
        
        
    }

}
