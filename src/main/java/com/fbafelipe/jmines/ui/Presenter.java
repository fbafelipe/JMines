package com.fbafelipe.jmines.ui;

import com.fbafelipe.jmines.domain.*;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

public class Presenter implements Mvp.Presenter {
	private Mvp.View mView;

	private MinesGame mGame;

	private AutoSolveThread mAutoSolveThread;

	public Presenter() {
		mGame = new MinesGame(Difficulty.HARD);
	}

	@Override
	public void setView(Mvp.View view) {
		mView = view;
		mView.setSize(mGame.getWidth(), mGame.getHeight());
		mView.showRemainingMines(mGame.getRemainingMines());
	}

	@Override
	public void onClickNewGame(Difficulty difficulty) {
		if (mAutoSolveThread != null) {
			mAutoSolveThread.stopSolving(() -> newGame(difficulty));
			mAutoSolveThread = null;
		}
		else
			newGame(difficulty);
	}

	private void newGame(Difficulty difficulty) {
		try {
			mGame = new MinesGame(difficulty);
			mView.setSize(difficulty.width, difficulty.height);
			mView.showRemainingMines(mGame.getRemainingMines());
		}
		catch (IllegalArgumentException e) {
			mView.showError(e.getMessage());
		}
	}

	@Override
	public void onClickExit() {
		System.exit(0);
	}

	@Override
	public void onClickTile(int x, int y) {
		if (!canPlayerInteract())
			return;

		openTile(x, y);

		mView.clearHintTile();
	}

	private void openTile(int x, int y) {
		List<Tile> revealed = new ArrayList<>();
		mGame.openTile(x, y, revealed);

		for (Tile tile : revealed)
			mView.revealTile(tile.x, tile.y, mGame.getTile(tile.x, tile.y));

		checkGameOver();
	}

	private void checkGameOver() {
		if (mGame.getGameState() == GameState.OVER_VICTORY)
			mView.showVictoryMessage();
		else if (mGame.getGameState() == GameState.OVER_DEFEAT) {
			List<Tile> revealMines = new ArrayList<>();
			List<Tile> wrongMarkedMines = new ArrayList<>();
			mGame.getDefeatReveal(revealMines, wrongMarkedMines);

			for (Tile tile : revealMines)
				mView.concealTile(tile.x, tile.y, TileMark.MINE);

			for (Tile tile : wrongMarkedMines)
				mView.concealTile(tile.x, tile.y, TileMark.WRONG_FLAG);

			mView.setBlowedTile(mGame.getBlowedMine().x, mGame.getBlowedMine().y);
		}
	}

	@Override
	public void onClickToggleMarkTile(int x, int y) {
		if (!canPlayerInteract())
			return;

		if (!mGame.isRevealed(x, y)) {
			mGame.flagTile(x, y);
			mView.concealTile(x, y, mGame.getTileMark(x, y));

			mView.showRemainingMines(mGame.getRemainingMines());
		}

		mView.clearHintTile();
	}

	@Override
	public void onClickAutoOpenTile(int x, int y) {
		if (!canPlayerInteract())
			return;

		List<Tile> revealed = new ArrayList<>();
		mGame.openNeighbours(x, y, revealed);

		for (Tile tile : revealed)
			mView.revealTile(tile.x, tile.y, mGame.getTile(tile.x, tile.y));

		checkGameOver();

		mView.clearHintTile();
	}

	@Override
	public void onClickHint() {
		if (!canPlayerInteract())
			return;

		Solver.Step step = new Solver(mGame).hint();
		if (step == null) {
			mView.showNoHintWarning();
			return;
		}

		executeSolverStep(step);
		mView.setHintTile(step.x, step.y);
	}

	private void executeSolverStep(Solver.Step step) {
		switch (step.stepType) {
			case FLAG:
				if (!mGame.isRevealed(step.x, step.y)) {
					mGame.flagTile(step.x, step.y, TileMark.FLAG);
					mView.concealTile(step.x, step.y, TileMark.FLAG);

					mView.showRemainingMines(mGame.getRemainingMines());
				}
				break;
			case OPEN:
				openTile(step.x, step.y);
				break;
		}
	}

	@Override
	public void onClickAutoSolve() {
		mView.clearHintTile();

		mAutoSolveThread = new AutoSolveThread();
		mAutoSolveThread.start();
	}

	private boolean canPlayerInteract() {
		return !mGame.isGameOver() && mAutoSolveThread == null;
	}

	private class AutoSolveThread extends Thread implements Solver.AutoSolveListener {
		private volatile boolean mRunning = true;

		private Runnable mStopSolvingCallback;

		public void stopSolving(Runnable callback) {
			mStopSolvingCallback = callback;
			mRunning = false;
		}

		@Override
		public void run() {
			Solver solver = new Solver(mGame);
			solver.autoSolve(this);
			mAutoSolveThread = null;

			try {
				SwingUtilities.invokeAndWait(() -> {
					if (mStopSolvingCallback != null)
						mStopSolvingCallback.run();
				});
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}

		@Override
		public boolean onStep(final Solver.Step step) {
			try {
				SwingUtilities.invokeAndWait(() -> {
					if (mRunning)
						executeSolverStep(step);
				});
			}
			catch (Exception e) {
				e.printStackTrace();
			}

			return mRunning;
		}
	}
}
