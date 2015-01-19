package com.beanfarmergames.seethedark.state;

public class PausableGameState implements GameState {
    
    private boolean paused = false;
    private float gameTimeSeconds = 0;

    @Override
    public boolean isSimulationRunning() {
        return !paused;
    }

    @Override
    public boolean getPause() {
        return paused;
    }

    @Override
    public void setPause(boolean pause) {
        this.paused = pause;
    }

    @Override
    public boolean togglePause() {
        this.paused = !this.paused;
        return this.paused;
    }

}
