package com.mygdx.game.field;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.mygdx.game.Player;
import com.mygdx.game.RenderLayer;

public interface RenderCallback {
    public void render(ShapeRenderer renderer, RenderLayer layer, Player player);
}
