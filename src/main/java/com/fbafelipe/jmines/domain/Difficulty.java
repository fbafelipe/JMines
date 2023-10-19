package com.fbafelipe.jmines.domain;

public class Difficulty {
	public static final Difficulty EASY = new Difficulty(9, 9, 10);
	public static final Difficulty MEDIUM = new Difficulty(16, 16, 40);
	public static final Difficulty HARD = new Difficulty(30, 16, 99);

	public final int width;
	public final int height;
	public final int mines;

	public Difficulty(int w, int h, int m) {
		width = w;
		height = h;
		mines = m;
	}
}
