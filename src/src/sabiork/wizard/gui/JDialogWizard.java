package sabiork.wizard.gui;

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
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map.Entry;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import org.sbml.jsbml.SBMLDocument;
import sabiork.util.WebServiceConnectException;
import sabiork.util.WebServiceResponseException;
import sabiork.wizard.model.WizardModel;
import sabiork.wizard.model.WizardProperties;

/**
 * The GUI version of the SABIO-RK wizard.
 * 
 * @author Matthias Rall
 */
@SuppressWarnings("serial")
public class JDialogWizard extends JDialog implements ActionListener,
		MouseListener {

	public enum ButtonState {
		START, NEXT_ENABLED, NEXT_DISABLED, FINISH
	}

	public enum CardID {
		NOT_AVAILABLE, CONFIRM_DIALOG, MATCHING, METHOD, REACTIONS_A, REACTIONS_M, SEARCH_A, SEARCH_M, SUMMARY_A, SUMMARY_M
	}

	private Box boxButtons;
	private CardID currentCardID;
	private HashMap<CardID, Card> cards;
	private JButton buttonBack;
	private JButton buttonNextFinish;
	private JButton buttonCancel;
	private JLabel labelLogo;
	private JPanel panelLogo;
	private JPanel panelCards;
	private JPanel panelButtons;
	private WizardModel model;

	public JDialogWizard(Window owner, ModalityType modalityType,
			SBMLDocument sbmlDocument) {
		super(owner, modalityType);
		this.model = new WizardModel(sbmlDocument);
		initialize();
	}

	private void initialize() {
		labelLogo = new JLabel(new ImageIcon(this.getClass().getResource(
				WizardProperties.getText("JDIALOG_WIZARD_IMAGE_LOGO"))));
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

		buttonNextFinish = new JButton(
				WizardProperties.getText("JDIALOG_WIZARD_TEXT_BUTTON_NEXT"));
		buttonNextFinish.addActionListener(this);

		buttonCancel = new JButton(
				WizardProperties.getText("JDIALOG_WIZARD_TEXT_BUTTON_CANCEL"));
		buttonCancel.addActionListener(this);

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
		setMinimumSize(new Dimension(800, 800));
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

		currentCardID = CardID.METHOD;

		showCard(currentCardID);
	}

	/**
	 * Returns the result of the wizard.
	 * 
	 * @return
	 */
	public SBMLDocument getResult() {
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
			((CardLayout) panelCards.getLayout()).show(panelCards,
					cardID.toString());
			currentCardID = cardID;
			break;
		}
	}

	/**
	 * Shows a confirm dialog.
	 */
	private void showConfirmDialog() {
		if (JOptionPane.showConfirmDialog(this, WizardProperties
				.getText("JDIALOG_WIZARD_TEXT_CONFIRM_DIALOG_MESSAGE"),
				WizardProperties
						.getText("JDIALOG_WIZARD_TEXT_CONFIRM_DIALOG_TITLE"),
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
			buttonNextFinish.setEnabled(true);
			break;
		case NEXT_ENABLED:
			buttonBack.setVisible(true);
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

	public void actionPerformed(ActionEvent e) {
		if (e.getSource().equals(buttonBack)) {
			Card currentCard = cards.get(currentCardID);
			showCard(currentCard.getPreviousCardID());
		}
		if (e.getSource().equals(buttonNextFinish)) {
			Card currentCard = cards.get(currentCardID);
			showCard(currentCard.getNextCardID());
		}
		if (e.getSource().equals(buttonCancel)) {
			dispose();
		}
	}

	public void mouseClicked(MouseEvent e) {
		if (e.getSource().equals(labelLogo)) {
			openURL(WizardProperties
					.getText("JDIALOG_WIZARD_TEXT_URL_SABIO_RK"));
		}
	}

	public void mouseEntered(MouseEvent e) {
		if (e.getSource().equals(labelLogo)) {
			setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
			labelLogo.setToolTipText(WizardProperties
					.getText("JDIALOG_WIZARD_TEXT_URL_SABIO_RK"));
		}
	}

	public void mouseExited(MouseEvent e) {
		if (e.getSource().equals(labelLogo)) {
			setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
		}
	}

	public void mousePressed(MouseEvent e) {
	}

	public void mouseReleased(MouseEvent e) {
	}

}
