package com.mygdx.game.util;

public final class AgedElement<T> {
    private final float t;
    private final T e;
    
    public float getT() {
        return t;
    }

    public T getE() {
        return e;
    }

    public AgedElement(float t, T e) {
        this.t = t;
        this.e = e;
    }
}