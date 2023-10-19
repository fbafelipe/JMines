package com.fbafelipe.jmines.domain;

public class Tile {
	public static final int MINE = -1;

	public final int x;
	public final int y;

	public Tile(int _x, int _y) {
		x = _x;
		y = _y;
	}

	public boolean isNeighbour(int nx, int ny) {
		return Math.abs(x - nx) <= 1 && Math.abs(y - ny) <= 1;
	}

	public boolean equals(int _x, int _y) {
		return x == _x && y == _y;
	}
}
