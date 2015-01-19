package com.beanfarmergames.seethedark.components;

import com.badlogic.gdx.physics.box2d.Fixture;
import com.beanfarmergames.seethedark.ship.Ship;
import com.beanfarmergames.seethedark.ship.ShipSection;

public abstract class AbstractComponent implements Component {
    private Ship ship = null;
    private ShipSection section = null;

    public void mountToSection(Ship ship, ShipSection section) {
        this.ship = ship;
        this.section = section;
    }

    // Always set if mounted
    public Ship getShip() {
        return ship;
    }

    public ShipSection getMountedSection() {
        return section;
    }

    // Can be null if mounted
    public Fixture getMountedFixture() {
        if (section == null)
            return null;

        return section.getFixture();
    }
}
