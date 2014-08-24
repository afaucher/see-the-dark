package com.mygdx.game.ship.components;

import com.badlogic.gdx.physics.box2d.Fixture;
import com.mygdx.game.ship.Ship;
import com.mygdx.game.ship.ShipSection;

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
