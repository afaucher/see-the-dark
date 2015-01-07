package com.mygdx.game.ship.components;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.Rectangle;
import com.mygdx.game.RenderLayer;
import com.mygdx.game.ship.Ship;
import com.mygdx.game.style.ColorPalate;

public class FuelControlComponent extends AbstractComponent implements FuelControl {

    @Override
    public ComponentType getComponentType() {
        return ComponentType.FuelControl;
    }

    @Override
    public Rectangle drawHud(ShapeRenderer renderer, Rectangle destAvailable) {
        float height = destAvailable.getHeight();

        float fuelGuageDegrees = (getFuel() / getFuelCapacity()) * 180;

        renderer.begin(ShapeType.Filled);
        renderer.setColor(ColorPalate.ACTIVE_HUD);

        renderer.arc(destAvailable.x + height, destAvailable.y, height, 0, fuelGuageDegrees);

        renderer.end();

        Rectangle result = new Rectangle(destAvailable);
        result.setWidth(height * 2);

        return result;
    }

    @Override
    public boolean requiresHud() {
        return true;
    }

    @Override
    public boolean requiresInput() {
        return false;
    }

    @Override
    public void keyPressed() {
    }

    @Override
    public void update(float seconds) {
    }

    private List<FuelControl> getFuelControls() {
        List<FuelControl> fuelControls = new ArrayList<FuelControl>();

        Ship ship = getShip();
        List<Component> components = ship.getComponents();

        for (Component c : components) {
            if (c == this) {
                continue;
            }
            if (!ComponentType.Fuel.equals(c.getComponentType())) {
                continue;
            }
            FuelControl fuelControl = (FuelControl) c;

            fuelControls.add(fuelControl);
        }

        return fuelControls;
    }

    @Override
    public float getFuel() {
        float fuel = 0;

        List<FuelControl> fuelControls = getFuelControls();

        for (FuelControl c : fuelControls) {
            fuel += c.getFuel();
        }
        return fuel;
    }

    @Override
    public float getFuelCapacity() {
        float fuel = 0;

        List<FuelControl> fuelControls = getFuelControls();

        for (FuelControl c : fuelControls) {

            fuel += c.getFuelCapacity();
        }
        return fuel;
    }

    @Override
    public float burnFuel(float toBurn) {
        List<FuelControl> fuelControls = getFuelControls();

        for (FuelControl c : fuelControls) {
            if (toBurn <= 0)
                break;
            toBurn = c.burnFuel(toBurn);
        }
        return toBurn;
    }

    @Override
    public void render(ShapeRenderer renderer, RenderLayer layer) {

    }

}
