package com.fbafelipe.jmines.ui;

import com.fbafelipe.jmines.domain.Difficulty;

import javax.swing.*;
import javax.swing.text.NumberFormatter;
import java.awt.*;
import java.awt.event.ActionListener;
import java.text.NumberFormat;

public class NewGameDialog {
	private JDialog mDialog;

	private JRadioButton mEasyButton;
	private JRadioButton mMediumButton;
	private JRadioButton mHardButton;
	private JRadioButton mCustomButton;

	private JTextField mWidthField;
	private JTextField mHeightField;
	private JTextField mMinesField;

	private Difficulty mSelectedDifficulty = null;

	public NewGameDialog(JFrame parent) {
		mDialog = new JDialog(parent, "New Game");

		JPanel container = new JPanel();
		container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));
		mDialog.add(container, BorderLayout.CENTER);

		ButtonGroup group = new ButtonGroup();
		mEasyButton = createRadioButton(container, "Easy", group, (e) -> selectDifficulty(Difficulty.EASY));
		mMediumButton = createRadioButton(container, "Medium", group, (e) -> selectDifficulty(Difficulty.MEDIUM));
		mHardButton = createRadioButton(container, "Hard", group, (e) -> selectDifficulty(Difficulty.HARD));
		mCustomButton = createRadioButton(container, "Custom", group, (e) -> selectCustom());

		createCustomFields(container);
		createButtons(container);

		mHardButton.setSelected(true);
		selectDifficulty(Difficulty.HARD);

		mDialog.pack();
		mDialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		mDialog.setResizable(false);
		mDialog.setLocationRelativeTo(parent);
	}

	public Difficulty show() {
		mDialog.setModal(true);
		mDialog.setVisible(true);

		return mSelectedDifficulty;
	}

	private void selectDifficulty(Difficulty difficulty) {
		mWidthField.setText(String.valueOf(difficulty.width));
		mHeightField.setText(String.valueOf(difficulty.height));
		mMinesField.setText(String.valueOf(difficulty.mines));

		mWidthField.setEnabled(false);
		mHeightField.setEnabled(false);
		mMinesField.setEnabled(false);
	}

	private void selectCustom() {
		mWidthField.setEnabled(true);
		mHeightField.setEnabled(true);
		mMinesField.setEnabled(true);
	}

	private void createCustomFields(JPanel container) {
		NumberFormatter formatter = createFormatter();

		JPanel panel = new JPanel();

		mWidthField = new JFormattedTextField(formatter);
		mHeightField = new JFormattedTextField(formatter);
		mMinesField = new JFormattedTextField(formatter);

		mWidthField.setColumns(5);
		mHeightField.setColumns(5);
		mMinesField.setColumns(5);

		panel.add(new JLabel("Width:"));
		panel.add(mWidthField);
		panel.add(Box.createHorizontalStrut(10));
		panel.add(new JLabel("Height:"));
		panel.add(mHeightField);
		panel.add(Box.createHorizontalStrut(10));
		panel.add(new JLabel("Mines:"));
		panel.add(mMinesField);

		container.add(panel);
	}

	private Difficulty parseCustomDifficulty() {
		try {
			return new Difficulty(
					Integer.parseInt(mWidthField.getText()),
					Integer.parseInt(mHeightField.getText()),
					Integer.parseInt(mMinesField.getText())
			);
		}
		catch (NumberFormatException e){
			return null;
		}
	}

	private void createButtons(JPanel container) {
		JPanel panel = new JPanel();

		JButton okButton = new JButton("OK");
		okButton.addActionListener((e) -> {
			if (mEasyButton.isSelected())
				mSelectedDifficulty = Difficulty.EASY;
			else if (mMediumButton.isSelected())
				mSelectedDifficulty = Difficulty.MEDIUM;
			else if (mHardButton.isSelected())
				mSelectedDifficulty = Difficulty.HARD;
			else
				mSelectedDifficulty = parseCustomDifficulty();

			if (mSelectedDifficulty != null)
				mDialog.dispose();
		});

		JButton cancelButton = new JButton("Cancel");
		cancelButton.addActionListener((e) -> mDialog.dispose());

		panel.add(okButton);
		panel.add(Box.createHorizontalStrut(50));
		panel.add(cancelButton);
		container.add(panel);
	}

	private NumberFormatter createFormatter() {
		NumberFormat format = NumberFormat.getInstance();
		NumberFormatter formatter = new NumberFormatter(format);
		formatter.setValueClass(Integer.class);
		formatter.setMinimum(0);
		formatter.setMaximum(Integer.MAX_VALUE);
		formatter.setAllowsInvalid(false);
		formatter.setCommitsOnValidEdit(true);
		return formatter;
	}

	private JRadioButton createRadioButton(JPanel container, String text, ButtonGroup buttonGroup, ActionListener listener) {
		JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));

		JRadioButton button = new JRadioButton(text);
		panel.add(button);
		button.addActionListener(listener);

		buttonGroup.add(button);
		container.add(panel);

		return button;
	}
}
