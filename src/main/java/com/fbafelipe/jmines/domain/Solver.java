package com.fbafelipe.jmines.domain;

import java.util.ArrayList;
import java.util.List;

public class Solver {
	private MinesGame mGame;

	public Solver(MinesGame game) {
		mGame = game;
	}

	public Step hint() {
		final SolverContext context = new SolverContext(mGame.getWidth(), mGame.getHeight());

		for (int y = 0; y < mGame.getHeight(); ++y) {
			for (int x = 0; x < mGame.getWidth(); ++x) {
				Step step = solveTile(x, y, context);
				if (step != null)
					return step;
			}
		}

		for (int y = 0; y < mGame.getHeight(); ++y) {
			for (int x = 0; x < mGame.getWidth(); ++x) {
				Step step = solveTileWithMineGroups(x, y, context);
				if (step != null)
					return step;
			}
		}

		return null;
	}

	private Step solveTile(int x, int y, final SolverContext context) {
		if (!mGame.isRevealed(x, y) || mGame.getTile(x, y) == 0)
			return null;

		countFlagsAndCandidates(x, y, context);

		if (context.firstCandidate == null)
			return null;

		if (context.flags == mGame.getTile(x, y))
			return new Step(context.firstCandidate, StepType.OPEN);
		else if (context.flags + context.candidateTiles == mGame.getTile(x, y))
			return new Step(context.firstCandidate, StepType.FLAG);

		createMissingMineGroups(x, y, context);

		return null;
	}

	private Step solveTileWithMineGroups(final int x, final int y, final SolverContext context) {
		if (!mGame.isRevealed(x, y) || mGame.getTile(x, y) == 0)
			return null;

		context.step = null;
		countFlagsAndCandidates(x, y, context);

		if (context.firstCandidate == null)
			return null;

		mGame.visitNeighbours(x, y, (nx, ny) -> {
			if (context.step != null)
				return;

			if (context.mineGroups[nx][ny] != null) {
				int tilesIsNeighbour = 0;
				int tilesIsNotNeighbour = 0;
				for (Tile t : context.mineGroups[nx][ny].tiles) {
					if (t.isNeighbour(x, y))
						++tilesIsNeighbour;
					else
						++tilesIsNotNeighbour;
				}
				int minMinesFromGroup = context.mineGroups[nx][ny].mines - tilesIsNotNeighbour;
				if (minMinesFromGroup > 0) {
					if (context.flags + minMinesFromGroup == mGame.getTile(x, y) && tilesIsNeighbour < context.candidateTiles) {
						Tile tile = findFirstNeighbourNotInMineGroup(x, y, context.mineGroups[nx][ny], context);
						context.step = new Step(tile, StepType.OPEN);
					}
					else {
						int maxMinesFromGroup = Math.min(context.mineGroups[nx][ny].mines, tilesIsNeighbour);
						int neighboursNotInGroup = countNeighboursNotInMineGroup(x, y, context.mineGroups[nx][ny], context);
						if (neighboursNotInGroup > 0 && minMinesFromGroup == maxMinesFromGroup && context.flags + maxMinesFromGroup + neighboursNotInGroup == mGame.getTile(x, y)) {
							Tile tile = findFirstNeighbourNotInMineGroup(x, y, context.mineGroups[nx][ny], context);
							context.step = new Step(tile, StepType.FLAG);
						}
					}
				}
			}
		});

		return context.step;
	}

	private Tile findFirstNeighbourNotInMineGroup(int x, int y, final MineGroup mineGroup, final SolverContext context) {
		final Tile[] tile = {null};

		mGame.visitNeighbours(x, y, (nx, ny) -> {
			if (tile[0] != null || mGame.isRevealed(nx, ny) || mGame.getTileMark(nx, ny) == TileMark.FLAG)
				return;

			boolean isInGroup = false;
			for (Tile t : mineGroup.tiles) {
				if (t.equals(nx, ny)) {
					isInGroup = true;
					break;
				}
			}

			if (!isInGroup)
				tile[0] = new Tile(nx, ny);
		});

		return tile[0];
	}

	private int countNeighboursNotInMineGroup(int x, int y, final MineGroup mineGroup, final SolverContext context) {
		final int[] count = {0};

		mGame.visitNeighbours(x, y, (nx, ny) -> {
			if (mGame.isRevealed(nx, ny) || mGame.getTileMark(nx, ny) == TileMark.FLAG)
				return;

			boolean isInGroup = false;
			for (Tile t : mineGroup.tiles) {
				if (t.equals(nx, ny)) {
					isInGroup = true;
					break;
				}
			}

			if (!isInGroup)
				++count[0];
		});

		return count[0];
	}

	private void countFlagsAndCandidates(int x, int y, final SolverContext context) {
		context.flags = 0;
		context.candidateTiles = 0;
		context.firstCandidate = null;

		mGame.visitNeighbours(x, y, (nx, ny) -> {
			if (!mGame.isRevealed(nx, ny)) {
				if (mGame.getTileMark(nx, ny) == TileMark.FLAG)
					++context.flags;
				else {
					++context.candidateTiles;
					context.firstCandidate = new Tile(nx, ny);
				}
			}
		});
	}

	private void createMissingMineGroups(int x, int y, final SolverContext context) {
		int missingMines = mGame.getTile(x, y) - context.flags;
		if (missingMines > 0) {
			final MineGroup group = new MineGroup(missingMines);
			context.mineGroups[x][y] = group;
			mGame.visitNeighbours(x, y, (nx, ny) -> {
				if (!mGame.isRevealed(nx, ny) && mGame.getTileMark(nx, ny) != TileMark.FLAG)
					group.tiles.add(new Tile(nx, ny));
			});
		}
	}

	public void autoSolve(AutoSolveListener listener) {
		while (!mGame.isGameOver()) {
			Step step = hint();
			if (step == null)
				step = randomGuess();

			if (step == null)
				break;

			if (!listener.onStep(step))
				break;

			try {
				Thread.sleep(25);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	private Step randomGuess() {
		for (int y = 0; y < mGame.getHeight(); ++y) {
			for (int x = 0; x < mGame.getWidth(); ++x) {
				if (!mGame.isRevealed(x, y) && mGame.getTileMark(x, y) != TileMark.FLAG)
					return new Step(x, y, StepType.OPEN);
			}
		}

		return null;
	}

	private static class SolverContext {
		public int flags = 0;
		public int candidateTiles = 0;
		public Tile firstCandidate = null;

		public Step step;

		public MineGroup[][] mineGroups;

		public SolverContext(int width, int height) {
			mineGroups = new MineGroup[width][height];
		}
	}

	private static class MineGroup {
		private int mines;
		public List<Tile> tiles;

		public MineGroup(int m) {
			mines = m;
			tiles = new ArrayList<>();
		}
	}

	public enum StepType {
		FLAG,
		OPEN
	}

	public static class Step {
		public final int x;
		public final int y;
		public final StepType stepType;

		public Step(int _x, int _y, StepType t) {
			x = _x;
			y = _y;
			stepType = t;
		}

		public Step(Tile tile, StepType t) {
			x = tile.x;
			y = tile.y;
			stepType = t;
		}

		@Override
		public String toString() {
			return stepType.name() + " " + x + "," + y;
		}
	}

	public interface AutoSolveListener {
		boolean onStep(Step step); // return true if should continue
	}
}
