package com.mygdx.game.mode;

public enum State {
    Playing(0),
    GameOver(1);
    
    private int state;
    
    State(int state) {
        this.state = state;
    }
    
    public int getState() {
        return state;
    }
}
