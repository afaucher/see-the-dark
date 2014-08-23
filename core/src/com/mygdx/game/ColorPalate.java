package com.mygdx.game;

import com.badlogic.gdx.graphics.Color;

public class ColorPalate {
	public static final Color BACKGROUND = new Color(0.16f, 0.17f, 0.02f, 1.0f); // Near
																					// black
	public static final Color HITS = new Color(0.93f, 0.87f, 0.72f, 1.0f); // Yellow
																			// orange
	public static final Color SHIP = new Color(0.71f, 0.32f, 0.12f, 1.0f); // Red
	public static final Color SCANNER = new Color(0.39f, 0.23f, 0.05f, 1.0f); // Brown

	public static final Color BALL = SCANNER;
	public static final Color TREE = HITS;
	
	public static final Color ACTIVE_HUD = new Color(0.67f, 0.84f, 0.90f, 1.0f); // Light Blue
	public static final Color INACTIVE_HUD = new Color(0.47f, 0.64f, 0.70f, 1.0f); // Blue
	public static final Color HUD_BG = new Color(0.95f,0.95f,0.95f,1); // Light Grey
	
	public static final Color SECTION_HULL_INDICATOR = new Color(0,0,0,1);
	public static final Color SECTION_HEAT_INDICATOR = new Color(1,0,0,1);
}
