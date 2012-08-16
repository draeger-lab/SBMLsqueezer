package sabiork.wizard.gui;

import java.awt.BorderLayout;
import javax.swing.BorderFactory;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import sabiork.wizard.gui.JDialogWizard.ButtonState;
import sabiork.wizard.gui.JDialogWizard.CardID;
import sabiork.wizard.model.KineticLawImporter;
import sabiork.wizard.model.WizardModel;
import sabiork.wizard.model.WizardProperties;

/**
 * A class that provides a short summary of the changes made by the SABIO-RK
 * wizard.
 * 
 * @author Matthias Rall
 * 
 */
@SuppressWarnings("serial")
public class CardSummaryA extends Card {

	private JScrollPane textAreaSummaryScrollPane;
	private JTextArea textAreaSummary;

	public CardSummaryA(JDialogWizard dialog, WizardModel model) {
		super(dialog, model);
		initialize();
	}

	private void initialize() {
		textAreaSummary = new JTextArea();
		textAreaSummary.setEnabled(false);
		textAreaSummaryScrollPane = new JScrollPane(textAreaSummary);

		setLayout(new BorderLayout());
		setBorder(BorderFactory.createTitledBorder(
				BorderFactory.createEtchedBorder(),
				WizardProperties.getText("CARD_SUMMARY_A_TEXT_SUMMARY")));
		add(textAreaSummaryScrollPane, BorderLayout.CENTER);
	}

	public void performBeforeShowing() {
		StringBuilder report = new StringBuilder();
		if (model.hasSelectedKineticLawImporters()) {
			for (KineticLawImporter selectedKineticLawImporter : model
					.getSelectedKineticLawImporters()) {
				if (selectedKineticLawImporter.isImportableKineticLaw()) {
					selectedKineticLawImporter.importKineticLaw();
					if (report.length() > 0) {
						report.append("\n\n");
					}
					report.append(selectedKineticLawImporter.getReport());
				}
			}
		}
		textAreaSummary.setText(report.toString());
		dialog.setButtonState(ButtonState.FINISH);
	}

	public CardID getPreviousCardID() {
		return CardID.SEARCH_A;
	}

	public CardID getNextCardID() {
		return CardID.CONFIRM_DIALOG;
	}

}
