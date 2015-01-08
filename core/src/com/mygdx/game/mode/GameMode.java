package com.mygdx.game.mode;

import com.mygdx.game.field.FieldRenderCallback;
import com.mygdx.game.field.FieldUpdateCallback;
import com.mygdx.game.ship.Ship;

public interface GameMode extends FieldRenderCallback, FieldUpdateCallback {

    public Mode getGameMode();
    
    /**
     * If game is over and ship won, return true
     * else return false
     * 
     * @param s
     * @return
     */
    public boolean isShipWinner(Ship s);
    
    public State getGameState();
    public void setGameState(State newState);
}
