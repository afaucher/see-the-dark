package com.beanfarmergames.seethedark.game.field;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.beanfarmergames.seethedark.game.Player;
import com.beanfarmergames.seethedark.game.RenderLayer;

public interface RenderCallback {
    public void render(ShapeRenderer renderer, RenderLayer layer, Player player);
}
