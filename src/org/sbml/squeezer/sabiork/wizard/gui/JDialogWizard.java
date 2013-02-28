/*
 * $Id$
 * $URL$
 * ---------------------------------------------------------------------
 * This file is part of SBMLsqueezer, a Java program that creates rate 
 * equations for reactions in SBML files (http://sbml.org).
 *
 * Copyright (C) 2006-2013 by the University of Tuebingen, Germany.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 * ---------------------------------------------------------------------
 */
package org.sbml.squeezer.sabiork.wizard.gui;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;

import org.sbml.jsbml.SBMLDocument;
import org.sbml.jsbml.util.filters.NameFilter;
import org.sbml.squeezer.SubmodelController;
import org.sbml.squeezer.sabiork.util.WebServiceConnectException;
import org.sbml.squeezer.sabiork.util.WebServiceResponseException;
import org.sbml.squeezer.sabiork.wizard.model.WizardModel;
import org.sbml.squeezer.sabiork.wizard.model.WizardProperties;

/**
 * The GUI version of the SABIO-RK wizard.
 * 
 * @author Matthias Rall
 * @version $Rev$
 */
public class JDialogWizard extends JDialog implements ActionListener, WindowListener,
		MouseListener {

	/**
	 * Generated serial version identifier.
	 */
	private static final long serialVersionUID = 2106891365126572604L;

	/**
	 * 
	 * @author Matthias Rall
	 * @version $Rev$
	 */
	public enum ButtonState {
		START, NEXT_ENABLED, NEXT_DISABLED, FINISH;
	}

	/**
	 * 
	 * @author Matthias Rall
	 * @version $Rev$
	 */
	public enum CardID {
		NOT_AVAILABLE, CONFIRM_DIALOG, MATCHING, METHOD, REACTIONS_A, REACTIONS_M, SEARCH_A, SEARCH_M, SUMMARY_A, SUMMARY_M;
	}
	
	/**
	 * 
	 * @author Andreas Dr&auml;ger
	 * @version $Rev$
	 */
	public enum Actions {
		BACK, FINISH, CANCEL;
	}

	private Box boxButtons;
	private CardID currentCardID;
	private Map<CardID, Card> cards;
	private JButton buttonBack;
	private JButton buttonNextFinish;
	private JButton buttonCancel;
	private JLabel labelLogo;
	private JPanel panelLogo;
	private JPanel panelCards;
	private JPanel panelButtons;
	private WizardModel model;

	/**
	 * 
	 * @param owner
	 * @param modalityType
	 * @param sbmlDocument
	 * @param overwriteExistingLaws
	 */
	public JDialogWizard(Window owner, ModalityType modalityType,
			SBMLDocument sbmlDocument, boolean overwriteExistingLaws) {
		super(owner, modalityType);
		addWindowListener(this);
		this.model = new WizardModel(sbmlDocument, overwriteExistingLaws);
		this.model.setSelectedReactions(sbmlDocument.getModel().getListOfReactions());
		initialize(false);
	}

	/**
	 * @param owner
	 * @param modalityType
	 * @param sbmlDocument
	 * @param reactionId
	 */
	public JDialogWizard(Window owner, ModalityType modalityType,
		SBMLDocument sbmlDocument, String reactionId) {
		super(owner, modalityType);
		addWindowListener(this);
		this.model = new WizardModel(sbmlDocument, reactionId, true);
		this.model.setSelectedReactions(sbmlDocument.getModel().getListOfReactions().filterList(new NameFilter(reactionId)));
		initialize(true);
	}

	/**
	 * 
	 * @param manual
	 */
	private void initialize(boolean manual) {
		Icon icon = UIManager.getIcon("JDIALOG_WIZARD_IMAGE_LOGO");
		if (icon == null) {
			icon = new ImageIcon(this.getClass().getResource(
				WizardProperties.getText("JDIALOG_WIZARD_IMAGE_LOGO")));
			UIManager.put("JDIALOG_WIZARD_IMAGE_LOGO", icon);
		}
		labelLogo = new JLabel(icon);
		labelLogo.addMouseListener(this);

		panelLogo = new JPanel(new BorderLayout());
		panelLogo.setBackground(Color.WHITE);
		panelLogo.add(labelLogo, BorderLayout.WEST);
		panelLogo.add(new JComponentEtchedLine(), BorderLayout.SOUTH);

		panelCards = new JPanel(new CardLayout());
		panelCards.setBorder(new EmptyBorder(new Insets(10, 10, 10, 10)));

		buttonBack = new JButton(
				WizardProperties.getText("JDIALOG_WIZARD_TEXT_BUTTON_BACK"));
		buttonBack.addActionListener(this);
		buttonBack.setActionCommand(Actions.BACK.name());

		buttonNextFinish = new JButton(
				WizardProperties.getText("JDIALOG_WIZARD_TEXT_BUTTON_NEXT"));
		buttonNextFinish.setSelected(true);
		buttonNextFinish.addActionListener(this);
		buttonNextFinish.setActionCommand(Actions.FINISH.name());

		buttonCancel = new JButton(
				WizardProperties.getText("JDIALOG_WIZARD_TEXT_BUTTON_CANCEL"));
		buttonCancel.addActionListener(this);
		buttonCancel.setActionCommand(Actions.CANCEL.name());
		
		// pressing the ESCAPE button triggers "Cancel"
		getRootPane().registerKeyboardAction(this, Actions.CANCEL.name(),
			KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
			JComponent.WHEN_IN_FOCUSED_WINDOW);
		// pressing the ENTER button triggers "OK"
		getRootPane().registerKeyboardAction(this, Actions.FINISH.name(),
			KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0),
			JComponent.WHEN_IN_FOCUSED_WINDOW);

		boxButtons = new Box(BoxLayout.LINE_AXIS);
		boxButtons.setBorder(new EmptyBorder(new Insets(10, 10, 10, 10)));
		boxButtons.add(buttonBack);
		boxButtons.add(Box.createHorizontalStrut(10));
		boxButtons.add(buttonNextFinish);
		boxButtons.add(Box.createHorizontalStrut(30));
		boxButtons.add(buttonCancel);

		panelButtons = new JPanel(new BorderLayout());
		panelButtons.add(new JComponentEtchedLine(), BorderLayout.NORTH);
		panelButtons.add(boxButtons, BorderLayout.EAST);

		setLayout(new BorderLayout());
		setTitle(WizardProperties.getText("JDIALOG_WIZARD_TEXT_TITLE"));
		setMinimumSize(new Dimension(480, 600));
		setPreferredSize(new Dimension(640, 720));
		setSize(getPreferredSize());
		add(panelLogo, BorderLayout.NORTH);
		add(panelCards, BorderLayout.CENTER);
		add(panelButtons, BorderLayout.SOUTH);

		cards = new HashMap<CardID, Card>();
		cards.put(CardID.MATCHING, new CardMatching(this, model));
		cards.put(CardID.METHOD, new CardMethod(this, model));
		cards.put(CardID.REACTIONS_A, new CardReactionsA(this, model));
		cards.put(CardID.REACTIONS_M, new CardReactionsM(this, model));
		cards.put(CardID.SEARCH_A, new CardSearchA(this, model));
		cards.put(CardID.SEARCH_M, new CardSearchM(this, model));
		cards.put(CardID.SUMMARY_A, new CardSummaryA(this, model));
		cards.put(CardID.SUMMARY_M, new CardSummaryM(this, model));

		registerCards();

		if (manual) {
			currentCardID = CardID.REACTIONS_M;
		} else {
			currentCardID = CardID.REACTIONS_A;
		}

		pack();
		showCard(currentCardID);
	}

	/**
	 * Returns the result of the wizard.
	 * 
	 * @return
	 */
	public SubmodelController getResult() {
		return model.getResult();
	}

	/**
	 * Stores and registers all {@link Card}
	 */
	private void registerCards() {
		for (Entry<CardID, Card> card : cards.entrySet()) {
			panelCards.add(card.getValue(), card.getKey().toString());
		}
	}

	/**
	 * Shows a {@link Card}, a confirm dialog or does nothing according to the
	 * given cardID.
	 * 
	 * @param cardID
	 */
	private void showCard(CardID cardID) {
		switch (cardID) {
		case NOT_AVAILABLE:
			break;
		case CONFIRM_DIALOG:
			showConfirmDialog();
			break;
		default:
			cards.get(cardID).performBeforeShowing();
			((CardLayout) panelCards.getLayout()).show(panelCards, cardID.toString());
			currentCardID = cardID;
			break;
		}
	}

	/**
	 * Shows a confirm dialog.
	 */
	private void showConfirmDialog() {
		if (JOptionPane.showConfirmDialog(this, 
			WizardProperties.getText("JDIALOG_WIZARD_TEXT_CONFIRM_DIALOG_MESSAGE"),
			WizardProperties.getText("JDIALOG_WIZARD_TEXT_CONFIRM_DIALOG_TITLE"),
			JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
			model.applyChanges();
			dispose();
		}
	}

	/**
	 * Shows an error message concerning a web service connect exception.
	 * 
	 * @param e
	 */
	public static void showErrorDialog(WebServiceConnectException e) {
		JOptionPane
				.showMessageDialog(
						null,
						WizardProperties
								.getText("JDIALOG_WIZARD_TEXT_ERROR_MESSAGE_WEB_SERVICE_CONNECT_EXCEPTION"),
						WizardProperties
								.getText("JDIALOG_WIZARD_TEXT_ERROR_MESSAGE_DIALOG_TITLE"),
						JOptionPane.ERROR_MESSAGE);
	}

	/**
	 * Shows an error message concerning a web service response exception.
	 * 
	 * @param e
	 */
	public static void showErrorDialog(WebServiceResponseException e) {
		switch (e.getResponseCode()) {
		case 400:
			JOptionPane
					.showMessageDialog(
							null,
							WizardProperties
									.getText("JDIALOG_WIZARD_TEXT_ERROR_MESSAGE_WEB_SERVICE_RESPONSE_EXCEPTION_400"),
							WizardProperties
									.getText("JDIALOG_WIZARD_TEXT_ERROR_MESSAGE_DIALOG_TITLE"),
							JOptionPane.ERROR_MESSAGE);
			break;
		case 404:
			JOptionPane
					.showMessageDialog(
							null,
							WizardProperties
									.getText("JDIALOG_WIZARD_TEXT_ERROR_MESSAGE_WEB_SERVICE_RESPONSE_EXCEPTION_404"),
							WizardProperties
									.getText("JDIALOG_WIZARD_TEXT_ERROR_MESSAGE_DIALOG_TITLE"),
							JOptionPane.ERROR_MESSAGE);
			break;
		case 500:
			JOptionPane
					.showMessageDialog(
							null,
							WizardProperties
									.getText("JDIALOG_WIZARD_TEXT_ERROR_MESSAGE_WEB_SERVICE_RESPONSE_EXCEPTION_500"),
							WizardProperties
									.getText("JDIALOG_WIZARD_TEXT_ERROR_MESSAGE_DIALOG_TITLE"),
							JOptionPane.ERROR_MESSAGE);
			break;
		default:
			JOptionPane
					.showMessageDialog(
							null,
							e.getMessage(),
							WizardProperties
									.getText("JDIALOG_WIZARD_TEXT_ERROR_MESSAGE_DIALOG_TITLE"),
							JOptionPane.ERROR_MESSAGE);
			break;
		}
	}

	/**
	 * Sets different properties of the wizard buttons for navigation according
	 * to the given buttonState
	 * 
	 * @param buttonState
	 */
	public void setButtonState(ButtonState buttonState) {
		switch (buttonState) {
		case START:
			buttonBack.setVisible(false);
			buttonNextFinish.setText(WizardProperties
					.getText("JDIALOG_WIZARD_TEXT_BUTTON_NEXT"));
			buttonNextFinish.setEnabled(false);
			break;
		case NEXT_ENABLED:
			//buttonBack.setVisible(true);
			buttonNextFinish.setText(WizardProperties
					.getText("JDIALOG_WIZARD_TEXT_BUTTON_NEXT"));
			buttonNextFinish.setEnabled(true);
			break;
		case NEXT_DISABLED:
			buttonBack.setVisible(true);
			buttonNextFinish.setText(WizardProperties
					.getText("JDIALOG_WIZARD_TEXT_BUTTON_NEXT"));
			buttonNextFinish.setEnabled(false);
			break;
		case FINISH:
			buttonBack.setVisible(false);
			buttonNextFinish.setText(WizardProperties
					.getText("JDIALOG_WIZARD_TEXT_BUTTON_FINISH"));
			buttonNextFinish.setEnabled(true);
			break;
		}
	}

	/**
	 * Opens a URL in the browser window.
	 * 
	 * @param url
	 */
	private void openURL(String url) {
		if (Desktop.isDesktopSupported()) {
			Desktop desktop = Desktop.getDesktop();
			if (desktop.isSupported(Desktop.Action.BROWSE)) {
				try {
					URI uri = new URI(url);
					desktop.browse(uri);
				} catch (URISyntaxException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent e) {
		try {
			Actions action = Actions.valueOf(e.getActionCommand());
			Card currentCard;
			switch (action) {
				case BACK:
					currentCard = cards.get(currentCardID);
					showCard(currentCard.getPreviousCardID());
					break;
				case CANCEL:
					model.deleteResult();
					dispose();
					break;
				case FINISH:
					if (buttonNextFinish.isEnabled()) {
						currentCard = cards.get(currentCardID);
						showCard(currentCard.getNextCardID());
					}
					break;
				default:
					break;
			}
		} catch (Throwable t) {}
	}

	/* (non-Javadoc)
	 * @see java.awt.event.MouseListener#mouseClicked(java.awt.event.MouseEvent)
	 */
	public void mouseClicked(MouseEvent e) {
		if (e.getSource().equals(labelLogo)) {
			openURL(WizardProperties.getText("JDIALOG_WIZARD_TEXT_URL_SABIO_RK"));
		}
	}

	/* (non-Javadoc)
	 * @see java.awt.event.MouseListener#mouseEntered(java.awt.event.MouseEvent)
	 */
	public void mouseEntered(MouseEvent e) {
		if (e.getSource().equals(labelLogo)) {
			setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
			labelLogo.setToolTipText(WizardProperties
					.getText("JDIALOG_WIZARD_TEXT_URL_SABIO_RK"));
		}
	}

	/* (non-Javadoc)
	 * @see java.awt.event.MouseListener#mouseExited(java.awt.event.MouseEvent)
	 */
	public void mouseExited(MouseEvent e) {
		if (e.getSource().equals(labelLogo)) {
			setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
		}
	}

	/* (non-Javadoc)
	 * @see java.awt.event.MouseListener#mousePressed(java.awt.event.MouseEvent)
	 */
	public void mousePressed(MouseEvent e) {
	}

	/* (non-Javadoc)
	 * @see java.awt.event.MouseListener#mouseReleased(java.awt.event.MouseEvent)
	 */
	public void mouseReleased(MouseEvent e) {
	}

	/* (non-Javadoc)
	 * @see java.awt.event.WindowListener#windowOpened(java.awt.event.WindowEvent)
	 */
	//@Override
	public void windowOpened(WindowEvent e) {
	}

	/* (non-Javadoc)
	 * @see java.awt.event.WindowListener#windowClosing(java.awt.event.WindowEvent)
	 */
	//@Override
	public void windowClosing(WindowEvent e) {
		model.deleteResult();
		dispose();
	}

	/* (non-Javadoc)
	 * @see java.awt.event.WindowListener#windowClosed(java.awt.event.WindowEvent)
	 */
	//@Override
	public void windowClosed(WindowEvent e) {
	}

	/* (non-Javadoc)
	 * @see java.awt.event.WindowListener#windowIconified(java.awt.event.WindowEvent)
	 */
	//@Override
	public void windowIconified(WindowEvent e) {
	}

	/* (non-Javadoc)
	 * @see java.awt.event.WindowListener#windowDeiconified(java.awt.event.WindowEvent)
	 */
	//@Override
	public void windowDeiconified(WindowEvent e) {
	}

	/* (non-Javadoc)
	 * @see java.awt.event.WindowListener#windowActivated(java.awt.event.WindowEvent)
	 */
	//@Override
	public void windowActivated(WindowEvent e) {
	}

	/* (non-Javadoc)
	 * @see java.awt.event.WindowListener#windowDeactivated(java.awt.event.WindowEvent)
	 */
	//@Override
	public void windowDeactivated(WindowEvent e) {
	}

}
