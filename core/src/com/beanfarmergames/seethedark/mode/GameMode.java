package com.beanfarmergames.seethedark.mode;

import com.beanfarmergames.seethedark.game.field.FieldUpdateCallback;
import com.beanfarmergames.seethedark.game.field.RenderCallback;

public interface GameMode extends RenderCallback, FieldUpdateCallback {

    public Mode getGameMode();

    /**
     * If game is over and ship won, return true else return false
     * 
     * @param s
     * @return
     */
    // public boolean isShipWinner(Ship s);

    public State getGameState();

    public void setGameState(State newState);
}
