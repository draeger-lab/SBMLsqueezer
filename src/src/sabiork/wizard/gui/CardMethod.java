package sabiork.wizard.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.border.EmptyBorder;
import sabiork.wizard.gui.JDialogWizard.ButtonState;
import sabiork.wizard.gui.JDialogWizard.CardID;
import sabiork.wizard.model.WizardModel;
import sabiork.wizard.model.WizardProperties;

/**
 * A class that allows to choose between the two different methods in the
 * SABIO-RK wizard.
 * 
 * @author Matthias Rall
 * 
 */
@SuppressWarnings("serial")
public class CardMethod extends Card {

	private ButtonGroup buttonGroup;
	private JLabel labelWelcome;
	private JLabel labelIntroduction;
	private JPanel panelTexts;
	private JPanel panelRadioButtons;
	private JPanel panelMethod;
	private JPanel panelWelcome;
	private JRadioButton radioButtonAutomatic;
	private JRadioButton radioButtonManual;

	public CardMethod(JDialogWizard dialog, WizardModel model) {
		super(dialog, model);
		initialize();
	}

	private void initialize() {
		labelWelcome = new JLabel(
				WizardProperties.getText("CARD_METHOD_TEXT_WELCOME"));
		labelWelcome.setFont(new Font(getFont().getName(), Font.BOLD, getFont()
				.getSize()));

		labelIntroduction = new JLabel(
				WizardProperties.getText("CARD_METHOD_TEXT_INTRODUCTION"));

		radioButtonAutomatic = new JRadioButton(
				WizardProperties.getText("CARD_METHOD_TEXT_AUTOMATIC"));
		radioButtonAutomatic.setSelected(true);

		radioButtonManual = new JRadioButton(
				WizardProperties.getText("CARD_METHOD_TEXT_MANUAL"));

		buttonGroup = new ButtonGroup();
		buttonGroup.add(radioButtonAutomatic);
		buttonGroup.add(radioButtonManual);

		panelTexts = new JPanel(new GridLayout(2, 1));
		panelTexts.add(labelWelcome);
		panelTexts.add(labelIntroduction);

		panelRadioButtons = new JPanel(new GridBagLayout());
		panelRadioButtons.add(radioButtonAutomatic, new GridBagConstraints(0,
				0, 1, 1, 0.0, 0.0, GridBagConstraints.LINE_START,
				GridBagConstraints.NONE, new Insets(10, 10, 5, 0), 0, 0));
		panelRadioButtons.add(radioButtonManual, new GridBagConstraints(0, 1,
				1, 1, 0.0, 0.0, GridBagConstraints.LINE_START,
				GridBagConstraints.NONE, new Insets(5, 10, 10, 0), 0, 0));

		panelMethod = new JPanel(new BorderLayout());
		panelMethod.setBorder(BorderFactory.createTitledBorder(
				BorderFactory.createEtchedBorder(),
				WizardProperties.getText("CARD_METHOD_TEXT_METHOD")));
		panelMethod.add(panelRadioButtons, BorderLayout.WEST);

		panelWelcome = new JPanel(new BorderLayout());
		panelWelcome.setBorder(new EmptyBorder(new Insets(15, 15, 15, 15)));
		panelWelcome.add(panelTexts, BorderLayout.NORTH);
		panelWelcome.add(Box.createRigidArea(new Dimension(0, 25)));
		panelWelcome.add(panelMethod, BorderLayout.SOUTH);

		setLayout(new BorderLayout());
		add(panelWelcome, BorderLayout.NORTH);
	}

	public void performBeforeShowing() {
		dialog.setButtonState(ButtonState.START);
	}

	public CardID getPreviousCardID() {
		return CardID.NOT_AVAILABLE;
	}

	public CardID getNextCardID() {
		if (radioButtonAutomatic.isSelected()) {
			return CardID.REACTIONS_A;
		} else {
			return CardID.REACTIONS_M;
		}
	}

}
