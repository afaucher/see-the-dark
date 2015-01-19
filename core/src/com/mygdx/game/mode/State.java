package com.mygdx.game.mode;

public enum State {
    Setup(0),
    Playing(1),
    GameOver(2);
    
    private int state;
    
    State(int state) {
        this.state = state;
    }
    
    public int getState() {
        return state;
    }
}
