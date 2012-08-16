package sabiork.wizard.gui;

import sabiork.wizard.model.WizardProperties;

/**
 * A class that provides the model of the combo box for selecting a reaction
 * filter.
 * 
 * @author Matthias Rall
 * 
 */
@SuppressWarnings("serial")
public class ComboBoxModelReactionFilters extends ComboBoxModelCaptions {

	public enum ReactionFilter {

		ALL_REACTIONS(WizardProperties
				.getText("COMBO_BOX_MODEL_REACTION_FILTERS_TEXT_ALL_REACTIONS")), REACTIONS_WITH_KINETICLAW(
				WizardProperties
						.getText("COMBO_BOX_MODEL_REACTION_FILTERS_TEXT_REACTIONS_WITH_KINETICLAW")), REACTIONS_WITHOUT_KINETICLAW(
				WizardProperties
						.getText("COMBO_BOX_MODEL_REACTION_FILTERS_TEXT_REACTIONS_WITHOUT_KINETICLAW")), REVERSIBLE_REACTIONS(
				WizardProperties
						.getText("COMBO_BOX_MODEL_REACTION_FILTERS_TEXT_REVERSIBLE_REACTIONS")), IRREVERSIBLE_REACTIONS(
				WizardProperties
						.getText("COMBO_BOX_MODEL_REACTION_FILTERS_TEXT_IRREVERSIBLE_REACTIONS")), FAST_REACTIONS(
				WizardProperties
						.getText("COMBO_BOX_MODEL_REACTION_FILTERS_TEXT_FAST_REACTIONS")), SLOW_REACTIONS(
				WizardProperties
						.getText("COMBO_BOX_MODEL_REACTION_FILTERS_TEXT_SLOW_REACTIONS"));

		private final String name;

		private ReactionFilter(String name) {
			this.name = name;
		}

		public String toString() {
			return name;
		}

	}

	public ComboBoxModelReactionFilters() {
		addCaption(WizardProperties
				.getText("COMBO_BOX_MODEL_REACTION_FILTERS_FILTER_OPTIONS"));
		addElement(ReactionFilter.ALL_REACTIONS);
		addElement(ReactionFilter.REACTIONS_WITH_KINETICLAW);
		addElement(ReactionFilter.REACTIONS_WITHOUT_KINETICLAW);
		addElement(ReactionFilter.REVERSIBLE_REACTIONS);
		addElement(ReactionFilter.IRREVERSIBLE_REACTIONS);
		addElement(ReactionFilter.FAST_REACTIONS);
		addElement(ReactionFilter.SLOW_REACTIONS);

		setSelectedItem(ReactionFilter.ALL_REACTIONS);
	}

	public ReactionFilter getSelectedReactionFilter() {
		return ((ReactionFilter) getSelectedItem());
	}

}
