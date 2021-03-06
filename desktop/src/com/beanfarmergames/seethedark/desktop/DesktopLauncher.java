package com.beanfarmergames.seethedark.desktop;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.beanfarmergames.seethedark.game.SeeTheDark;

public class DesktopLauncher {
	public static void main (String[] arg) {
		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
		config.resizable = true;
		config.width = 1024;
		config.height = 1000;
		config.title = "See The Dark";
		new LwjglApplication(new SeeTheDark(), config);
	}
}
