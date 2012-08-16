package sabiork.wizard.gui;

import javax.swing.JPanel;
import sabiork.wizard.gui.JDialogWizard.CardID;
import sabiork.wizard.model.WizardModel;

/**
 * The abstract base class for all panels to be displayed in the SABIO-RK
 * wizard.
 * 
 * @author Matthias Rall
 * 
 */
@SuppressWarnings("serial")
public abstract class Card extends JPanel {

	protected JDialogWizard dialog;
	protected WizardModel model;

	public Card(JDialogWizard dialog, WizardModel model) {
		this.dialog = dialog;
		this.model = model;
	}

	/**
	 * Actions to take place before the actual {@link Card} is displayed.
	 */
	public void performBeforeShowing() {
	};

	/**
	 * Returns the id of the preceding {@link Card}.
	 * 
	 * @return the id of the preceding {@link Card}
	 */
	public abstract CardID getPreviousCardID();

	/**
	 * Returns the id of the succeeding {@link Card}.
	 * 
	 * @return the id of the succeeding {@link Card}
	 */
	public abstract CardID getNextCardID();

}
