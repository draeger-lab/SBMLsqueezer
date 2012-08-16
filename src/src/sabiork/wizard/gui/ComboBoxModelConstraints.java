package sabiork.wizard.gui;

import sabiork.SABIORK;
import sabiork.wizard.model.WizardProperties;

/**
 * A class that provides the model of the combo box for selecting a constraint
 * in the automatic search.
 * 
 * @author Matthias Rall
 * 
 */
@SuppressWarnings("serial")
public class ComboBoxModelConstraints extends ComboBoxModelCaptions {

	public ComboBoxModelConstraints() {
		addCaption(WizardProperties
				.getText("COMBO_BOX_MODEL_CONSTRAINTS_TEXT_REACTION/PATHWAY"));
		addElement(SABIORK.QueryField.PATHWAY);

		addCaption(WizardProperties
				.getText("COMBO_BOX_MODEL_CONSTRAINTS_TEXT_BIOLOGICAL_SOURCE"));
		addElement(SABIORK.QueryField.TISSUE);
		addElement(SABIORK.QueryField.ORGANISM);
		addElement(SABIORK.QueryField.CELLULAR_LOCATION);

		setSelectedItem(SABIORK.QueryField.PATHWAY);
	}

	public SABIORK.QueryField getSelectedQueryField() {
		return ((SABIORK.QueryField) getSelectedItem());
	}

	public boolean isQueryFieldSelected() {
		return (getSelectedItem() instanceof SABIORK.QueryField);
	}

}
