package com.fbafelipe.jmines.ui;

import com.fbafelipe.jmines.domain.Difficulty;
import com.fbafelipe.jmines.domain.Tile;
import com.fbafelipe.jmines.domain.TileMark;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class View implements Mvp.View {
	private static final Color NUMBER_COLOR[] = {
			new Color(0.0f, 0.0f, 0.0f, 0.0f),
			new Color(0.012f, 0.012f, 0.510f),
			new Color(0.016f, 0.514f, 0.016f),
			new Color(0.584f, 0.502f, 0.169f),
			new Color(0.435f, 0.141f, 0.373f),
			new Color(0.557f, 0.016f, 0.016f),
			new Color(0.722f, 0.384f, 0.063f),
			new Color(0.235f, 0.235f, 0.235f),
			new Color(0.204f, 0.204f, 0.204f)
	};

	private static final Color HINT_BG_COLOR = Color.ORANGE;
	private static final Color BLOWED_MINE_BG_COLOR = Color.RED;

	private static final Dimension TILE_PREFERRED_SIZE = new Dimension(50, 50);

	private Mvp.Presenter mPresenter;

	private JFrame mFrame;
	private JPanel mGridPanel;
	private JPanel mTiles[][];

	private JTextField mMinesRemaining;

	private ImageIcon mFlagIcon;
	private ImageIcon mWrongFlagIcon;
	private ImageIcon mMineIcon;

	private Font mLabelFont;

	private Tile mHint = new Tile(-1, -1);

	public View() {
		mFlagIcon = new ImageIcon("assets/flag.png");
		mWrongFlagIcon = new ImageIcon("assets/wrong_flag.png");
		mMineIcon = new ImageIcon("assets/mine.png");

		mFrame = new JFrame("JMines");
		mFrame.getContentPane().setLayout(new BorderLayout());

		mGridPanel = new JPanel();
		mFrame.getContentPane().add(mGridPanel, BorderLayout.CENTER);
		mFrame.setIconImage(mMineIcon.getImage());

		createHeader();
		createMenu();
		mFrame.addComponentListener(new ComponentAdapter() {
			@Override
			public void componentResized(ComponentEvent componentEvent) {
				updateTileFontSize();
			}
		});
		mFrame.setVisible(true);
	}

	private void createHeader() {
		JPanel header = new JPanel(new FlowLayout(FlowLayout.LEFT));
		mMinesRemaining = new JTextField();
		mMinesRemaining.setColumns(3);
		mMinesRemaining.setEditable(false);
		mMinesRemaining.setFocusable(false);
		header.add(mMinesRemaining);
		mFrame.getContentPane().add(header, BorderLayout.NORTH);
	}

	private void createMenu() {
		JMenuBar menuBar = new JMenuBar();
		JMenu gameMenu = new JMenu("Game");

		JMenuItem newGame = new JMenuItem("New Game");
		newGame.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, KeyEvent.CTRL_DOWN_MASK));
		newGame.addActionListener((e) -> showNewGameDialog());
		gameMenu.add(newGame);

		JMenuItem exit = new JMenuItem("Exit");
		exit.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, KeyEvent.CTRL_DOWN_MASK));
		exit.addActionListener((e) -> mPresenter.onClickExit());
		gameMenu.add(exit);

		menuBar.add(gameMenu);

		JMenu helpMenu = new JMenu("Help");

		JMenuItem hint = new JMenuItem("Hint");
		hint.addActionListener((e) -> mPresenter.onClickHint());
		helpMenu.add(hint);

		JMenuItem autoSolve = new JMenuItem("Auto Solve");
		autoSolve.addActionListener((e) -> mPresenter.onClickAutoSolve());
		helpMenu.add(autoSolve);

		menuBar.add(helpMenu);

		mFrame.setJMenuBar(menuBar);
	}

	private void showNewGameDialog() {
		Difficulty difficulty = new NewGameDialog(mFrame).show();
		if (difficulty != null)
			mPresenter.onClickNewGame(difficulty);
	}

	@Override
	public void setPresenter(Mvp.Presenter presenter) {
		mPresenter = presenter;
	}

	@Override
	public void concealAll() {
		setSize(mTiles.length, mTiles[0].length);
	}

	@Override
	public void setSize(int width, int height) {
		mGridPanel.removeAll();
		mGridPanel.setLayout(new GridLayout(height, width));

		mTiles = new JPanel[width][height];

		for (int y = 0; y < height; ++y) {
			for (int x = 0; x < width; ++x) {
				mTiles[x][y] = new JPanel(new BorderLayout());
				concealTile(x, y, TileMark.NOTHING);

				mTiles[x][y].setMinimumSize(TILE_PREFERRED_SIZE);
				mTiles[x][y].setPreferredSize(TILE_PREFERRED_SIZE);
				mGridPanel.add(mTiles[x][y]);
			}
		}

		mFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		mFrame.pack();


		updateTileFontSize();
	}

	private void updateTileFontSize() {
		int width = mGridPanel.getWidth() / mTiles.length - 4;
		int height = mGridPanel.getHeight() / mTiles[0].length - 4;

		if (width <= 0 || height <= 0)
			return;

		JLabel label = new JLabel();
		Font labelFont = new JLabel().getFont();
		labelFont = labelFont.deriveFont(labelFont.getStyle() | Font.BOLD);

		FontMetrics fontMetrics = label.getFontMetrics(labelFont);
		int stringWidth = fontMetrics.stringWidth("8");
		int stringHeight = fontMetrics.getAscent();

		// Find out how much the font can grow in width
		float widthRatio = (float) width / (float) stringWidth;
		float heightRatio = (float) height / (float) stringHeight;

		float fontSize = Math.min(labelFont.getSize() * widthRatio, labelFont.getSize() * heightRatio);

		mLabelFont = labelFont.deriveFont(fontSize);

		for (Component component : mGridPanel.getComponents()) {
			if (component instanceof JLabel) {
				component.setFont(mLabelFont);
				component.repaint();
			}
		}
	}

	@Override
	public void revealTile(int x, int y, int value) {
		JPanel container = mTiles[x][y];
		container.removeAll();
		JLabel label;

		if (value > 0) {
			label = new JLabel(String.valueOf(value), SwingConstants.CENTER);
			label.setForeground(NUMBER_COLOR[value]);
			label.setFont(mLabelFont);
		}
		else if (value == Tile.MINE) {
			label = new JLabel(mMineIcon);
		}
		else
			label = new JLabel();

		container.add(label, BorderLayout.CENTER);

		label.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent mouseEvent) {
				if (isMiddleMouseButton(mouseEvent))
					mPresenter.onClickAutoOpenTile(x, y);
			}
		});

		container.validate();
	}

	@Override
	public void concealTile(final int x, final int y, TileMark tileMark) {
		JPanel container = mTiles[x][y];
		container.removeAll();
		JButton button;

		switch (tileMark) {
			case FLAG:
				button = new JButton(mFlagIcon);
				break;
			case QUESTION_MARK: {
				button = new JButton("?");
				Font f = button.getFont();
				button.setFont(f.deriveFont(f.getStyle() | Font.BOLD));
				break;
			}
			case WRONG_FLAG:
				button = new JButton(mWrongFlagIcon);
				break;
			case MINE:
				button = new JButton(mMineIcon);
				break;
			case NOTHING:
			default:
				button = new JButton();
				break;
		}
		container.add(button, BorderLayout.CENTER);

		button.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent mouseEvent) {
				if (SwingUtilities.isLeftMouseButton(mouseEvent))
					mPresenter.onClickTile(x, y);
				else if (SwingUtilities.isRightMouseButton(mouseEvent))
					mPresenter.onClickToggleMarkTile(x, y);
			}
		});

		container.validate();
	}

	@Override
	public void setBlowedTile(int x, int y) {
		for (Component c : mTiles[x][y].getComponents())
			c.setBackground(BLOWED_MINE_BG_COLOR);
		mTiles[x][y].setBackground(BLOWED_MINE_BG_COLOR);
	}

	@Override
	public void setHintTile(int x, int y) {
		clearHintTile();
		mHint = new Tile(x, y);

		for (Component c : mTiles[mHint.x][mHint.y].getComponents())
			c.setBackground(HINT_BG_COLOR);
		mTiles[mHint.x][mHint.y].setBackground(HINT_BG_COLOR);
	}

	@Override
	public void clearHintTile() {
		if (mHint.x >= 0 && mHint.y >= 0) {
			for (Component c : mTiles[mHint.x][mHint.y].getComponents())
				c.setBackground(null);
			mTiles[mHint.x][mHint.y].setBackground(null);
			mHint = new Tile(-1, -1);
		}
	}

	@Override
	public void showNoHintWarning() {
		JOptionPane.showMessageDialog(mFrame, "Solver cannot find a good move. You will have to guess...", "JMines", JOptionPane.WARNING_MESSAGE);
	}

	@Override
	public void showRemainingMines(int mines) {
		mMinesRemaining.setText(String.valueOf(mines));
	}

	@Override
	public void showVictoryMessage() {
		JOptionPane.showMessageDialog(mFrame, "Congratulations! You won!", "JMines", JOptionPane.INFORMATION_MESSAGE);
	}

	@Override
	public void showError(String message) {
		JOptionPane.showMessageDialog(mFrame, message, "JMines", JOptionPane.ERROR_MESSAGE);
	}

	private boolean isMiddleMouseButton(MouseEvent mouseEvent) {
		return SwingUtilities.isMiddleMouseButton(mouseEvent) ||
				(SwingUtilities.isLeftMouseButton(mouseEvent) && SwingUtilities.isRightMouseButton(mouseEvent));
	}
}
