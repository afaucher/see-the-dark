package com.mygdx.game.mode;

import com.mygdx.game.field.Field;

public abstract class AbstractGameMode implements GameMode {
    
    private State state;
    private Field field;

    public AbstractGameMode(Field field) {
        this.field = field;
        this.state = State.Setup;
        
        field.registerRenderCallback(this);
        field.registerUpdateCallback(this);
    }

    @Override
    public State getGameState() {
        return state;
    }
    
    @Override
    public void setGameState(State newState) {
        this.state = newState;
    }
    
    public Field getField() {
        return field;
    }

}
