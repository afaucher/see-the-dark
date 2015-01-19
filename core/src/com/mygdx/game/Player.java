package com.mygdx.game;

import com.mygdx.game.ship.Ship;

public class Player {
    private String name;
    private TwoAxisControl steeringControl;
    private Ship s;

    public Player(String name) {
        this.name = name;
        this.steeringControl = new TwoAxisControl();
    }
    
    public TwoAxisControl getSteetingControl() {
        return steeringControl;
    }
    
    public void attachShip(Ship s) {
        this.s = s;
    }
    
    public Ship getShip() {
        return s;
    }
    
    public String getName() {
        return name;
    }
}
