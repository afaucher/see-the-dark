package com.mygdx.game.ship.components;

public class EngineContribution {
    public float torqueJoule = 0;
    public float thrustJoule = 0;

    public EngineContribution(float torqueJoule, float thrustJoule) {
        this.torqueJoule = torqueJoule;
        this.thrustJoule = thrustJoule;
    }

    public void addEngineContribution(EngineContribution contribution) {
        this.torqueJoule = contribution.torqueJoule;
        this.thrustJoule = contribution.thrustJoule;
    }

    public EngineContribution cpy() {
        return new EngineContribution(torqueJoule, thrustJoule);
    }
}