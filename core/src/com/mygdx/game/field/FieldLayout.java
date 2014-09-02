package com.mygdx.game.field;

import java.util.List;

import com.badlogic.gdx.physics.box2d.World;
import com.mygdx.game.FieldUpdateCallback;

public interface FieldLayout {
    public List<FieldUpdateCallback> populateField(World world);
}
