package com.fbafelipe.jmines;

import com.fbafelipe.jmines.ui.Mvp;
import com.fbafelipe.jmines.ui.Presenter;
import com.fbafelipe.jmines.ui.View;

public class Main {
	public static void main(String args[]) {
		Mvp.View view = new View();
		Mvp.Presenter presenter = new Presenter();

		view.setPresenter(presenter);
		presenter.setView(view);
	}
}
