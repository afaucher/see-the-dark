package com.mygdx.game;

import com.badlogic.gdx.graphics.Color;

public class ColorPalate {
    // http://paletton.com/#uid=72w161k00ij0Xz30qgO1Ugc4KfZk3HcOc89P6E9n5d58cb5fkHWdFqMeyzdegRUhE+ykWknzqgm2P4n3ylnghKlW9-

    public static final Color AXIS = new Color(0, 1, 0, 1);

    public static final Color BACKGROUND = new Color(0.16f, 0.17f, 0.02f, 1.0f); // Near
                                                                                 // black
    // Active sensors color only used as a default if object has no color
    // returned
    public static final Color ACTIVE_SENSOR_HITS = new Color(0.93f, 0.87f, 0.72f, 1.0f); // Yellow
    public static final Color PASSIVE_SENSOR_HITS = new Color(0.93f, 0.87f, 0.72f, 0.5f); // Yellow
    // orange
    public static final Color SHIP = new Color(0.71f, 0.32f, 0.12f, 1.0f); // Red
    public static final Color SCANNER = new Color(0.39f, 0.23f, 0.05f, 1.0f); // Brown

    public static final Color BALL = SCANNER;
    public static final Color TREE = ACTIVE_SENSOR_HITS;
    public static final Color SUN = new Color(0.95f, 0.95f, 1.00f, 1.0f); // A
                                                                          // LIGHT
                                                                          // BLUE
                                                                          // STAR!!
    public static final Color HUD_TEXT = new Color(1, 1, 1, 1); // White
    public static final Color ACTIVE_HUD = new Color(0.67f, 0.84f, 0.90f, 1.0f); // Light
                                                                                 // Blue
    public static final Color INACTIVE_HUD = new Color(0.37f, 0.54f, 0.60f, 1.0f); // Blue
    public static final Color HUD_BG = new Color(0.95f, 0.95f, 0.95f, 1); // Light
                                                                          // Grey

    public static final Color WEAPON_RANGE = new Color(0.79f, 0.23f, 0.05f, 1.0f); // Redish
                                                                                   // Bround
    public static final Color WEAPON_AIMER = new Color(1.0f, 0.0f, 0.00f, 0.5f); // Transparent
                                                                                 // red

    public static final Color SECTION_HULL_INDICATOR = new Color(0, 0, 0, 1);
    public static final Color SECTION_HEAT_INDICATOR = new Color(1, 0, 0, 1);
}
