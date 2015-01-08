package com.mygdx.game.state;

public interface GameState {

    /**
     * True if simulation should run.
     * 
     * @return
     */
    public boolean isSimulationRunning();
    
    public boolean getPause();
    public void setPause(boolean pause);
    public boolean togglePause();
}
