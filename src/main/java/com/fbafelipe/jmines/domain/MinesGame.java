package com.fbafelipe.jmines.domain;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MinesGame {
	private Difficulty mDifficulty;

	private int mTiles[][];
	private TileMark mMarks[][];
	private boolean mRevealed[][];
	private GameState mGameState = GameState.IN_PROGRESS;

	private Tile mBlowedMine = null;

	private boolean mMinesPlaced = false;

	private int mFlags = 0;
	private int mRevealedCount = 0;
	private int mTotalTilesToReveal;

	public MinesGame(Difficulty difficulty) {
		mDifficulty = difficulty;
		checkValidDifficulty();

		mTiles = new int[mDifficulty.width][mDifficulty.height];
		mMarks = new TileMark[mDifficulty.width][mDifficulty.height];
		mRevealed = new boolean[mDifficulty.width][mDifficulty.height];

		for (int y = 0; y < mDifficulty.height; ++y) {
			for (int x = 0; x < mDifficulty.width; ++x) {
				mMarks[x][y] = TileMark.NOTHING;
			}
		}

		mTotalTilesToReveal = mDifficulty.width * mDifficulty.height - mDifficulty.mines;
	}

	private void checkValidDifficulty() {
		if (mDifficulty.width <= 0 || mDifficulty.height <= 0)
			throw new IllegalArgumentException("Invalid game size");
		if (mDifficulty.mines < 0)
			throw new IllegalArgumentException("Invalid number of mines");
		if (mDifficulty.mines >= mDifficulty.width * mDifficulty.height)
			throw new IllegalArgumentException("Too many mines");
	}

	public boolean isGameOver() {
		return mGameState != GameState.IN_PROGRESS;
	}

	public GameState getGameState() {
		return mGameState;
	}

	public int getWidth() {
		return mDifficulty.width;
	}

	public int getHeight() {
		return mDifficulty.height;
	}

	public void getDefeatReveal(List<Tile> revealMines, List<Tile> wrongFlags) {
		for (int y = 0; y < mDifficulty.height; ++y) {
			for (int x = 0; x < mDifficulty.width; ++x) {
				if (!mRevealed[x][y] && mTiles[x][y] == Tile.MINE && mMarks[x][y] != TileMark.FLAG) {
					mRevealed[x][y] = true;
					revealMines.add(new Tile(x, y));
				}
				if (!mRevealed[x][y] && mTiles[x][y] != Tile.MINE && mMarks[x][y] == TileMark.FLAG)
					wrongFlags.add(new Tile(x, y));
			}
		}
	}

	public Tile getBlowedMine() {
		return mBlowedMine;
	}

	// return the revealed tiles
	public void openTile(int x, int y, List<Tile> revealed) {
		if (mRevealed[x][y] || mMarks[x][y] != TileMark.NOTHING)
			return;

		mRevealed[x][y] = true;
		revealed.add(new Tile(x, y));

		if (!mMinesPlaced)
			placeMines(x, y);

		if (mTiles[x][y] == Tile.MINE) {
			mGameState = GameState.OVER_DEFEAT;
			mBlowedMine = new Tile(x, y);
			return;
		}

		++mRevealedCount;

		if (mTiles[x][y] == 0) {
			visitNeighbours(x, y, (nx, ny) -> {
				if (!mRevealed[nx][ny] && mMarks[nx][ny] == TileMark.NOTHING)
					openTile(nx, ny, revealed);
			});
		}

		if (mRevealedCount >= mTotalTilesToReveal)
			mGameState = GameState.OVER_VICTORY;
	}

	private void placeMines(int firstX, int firstY) {
		List<Tile> possibleTiles = new ArrayList<>();

		for (int y = 0; y < mDifficulty.height; ++y) {
			for (int x = 0; x < mDifficulty.width; ++x) {
				if (firstX != x || firstY != y)
					possibleTiles.add(new Tile(x, y));
			}
		}

		Collections.shuffle(possibleTiles);
		for (int i = 0; i < mDifficulty.mines; ++i) {
			Tile tile = possibleTiles.get(i);
			mTiles[tile.x][tile.y] = Tile.MINE;

			visitNeighbours(tile.x, tile.y, (nx, ny) -> {
				if (mTiles[nx][ny] != Tile.MINE)
					++mTiles[nx][ny];
			});
		}

		mMinesPlaced = true;
	}

	public void openNeighbours(int x, int y, List<Tile> revealed) {
		if (!mRevealed[x][y])
			return;

		final int[] neighbourFlags = {0};
		final int[] neighbourQuestionMarks = {0};

		visitNeighbours(x, y, (nx, ny) -> {
			if (mMarks[nx][ny] == TileMark.FLAG)
				++neighbourFlags[0];
			else if (mMarks[nx][ny] == TileMark.QUESTION_MARK)
				++neighbourQuestionMarks[0];
		});

		if (neighbourFlags[0] == mTiles[x][y] && neighbourQuestionMarks[0] == 0) {
			visitNeighbours(x, y, (nx, ny) -> openTile(nx, ny, revealed));
		}
	}

	public void flagTile(int x, int y, TileMark setMark) {
		if (mMarks[x][y] == TileMark.FLAG)
			--mFlags;

		mMarks[x][y] = setMark;
		if (setMark == TileMark.FLAG)
			++mFlags;
	}

	public void flagTile(int x, int y) {
		if (mMarks[x][y] == TileMark.FLAG)
			--mFlags;

		switch (mMarks[x][y]) {
			case NOTHING:
				mMarks[x][y] = TileMark.FLAG;
				++mFlags;
				break;
			case FLAG:
				mMarks[x][y] = TileMark.QUESTION_MARK;
				break;
			case QUESTION_MARK:
				mMarks[x][y] = TileMark.NOTHING;
				break;
			default:
				break;
		}
	}

	public int getRemainingMines() {
		return mDifficulty.mines - mFlags;
	}

	// Tile.MINE is a mine
	public int getTile(int x, int y) {
		return mTiles[x][y];
	}

	public boolean isRevealed(int x, int y) {
		return mRevealed[x][y];
	}

	public TileMark getTileMark(int x, int y) {
		return mMarks[x][y];
	}

	public void visitNeighbours(int x, int y, TileVisitor visitor) {
		int xBegin = Math.max(x - 1, 0);
		int xEnd = Math.min(x + 2, mDifficulty.width);
		int yBegin = Math.max(y - 1, 0);
		int yEnd = Math.min(y + 2, mDifficulty.height);

		for (int nx = xBegin; nx < xEnd; ++nx) {
			for (int ny = yBegin; ny < yEnd; ++ny) {
				if (nx != x || ny != y)
					visitor.visit(nx, ny);
			}
		}
	}

	public interface TileVisitor {
		void visit(int x, int y);
	}
}
