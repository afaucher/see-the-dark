package com.beanfarmergames.seethedark.style;

import com.badlogic.gdx.graphics.g2d.BitmapFont;

public class FontPalate {
    
    public static BitmapFont HUD_FONT;

    public static void loadFonts() {
        HUD_FONT = new BitmapFont();
        HUD_FONT.setColor(ColorPalate.HUD_TEXT);
    }
}
