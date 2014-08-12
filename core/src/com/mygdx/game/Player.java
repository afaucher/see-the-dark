package com.mygdx.game;

public class Player {
	private String name;
	private TwoAxisControl steeringControl;

	public Player(String name, TwoAxisControl steeringControl) {
		this.name = name;
		this.steeringControl = steeringControl;
	}
}
