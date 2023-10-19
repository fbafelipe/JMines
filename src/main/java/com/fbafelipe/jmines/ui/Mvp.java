package com.fbafelipe.jmines.ui;

import com.fbafelipe.jmines.domain.Difficulty;
import com.fbafelipe.jmines.domain.TileMark;

public class Mvp {
	public interface Presenter {
		void setView(Mvp.View view);

		void onClickNewGame(Difficulty difficulty);
		void onClickExit();
		void onClickTile(int x, int y);
		void onClickToggleMarkTile(int x, int y);
		void onClickAutoOpenTile(int x, int y);

		void onClickHint();
		void onClickAutoSolve();
	}

	public interface View {
		void setPresenter(Mvp.Presenter presenter);

		void concealAll();
		void setSize(int width, int height);
		void revealTile(int x, int y, int value);
		void concealTile(int x, int y, TileMark tileMark);

		void setBlowedTile(int x, int y);

		void setHintTile(int x, int y);
		void clearHintTile();
		void showNoHintWarning();

		void showRemainingMines(int mines);
		void showVictoryMessage();

		void showError(String message);
	}
}
