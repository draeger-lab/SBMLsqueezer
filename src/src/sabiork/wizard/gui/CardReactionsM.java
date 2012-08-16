package sabiork.wizard.gui;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import org.sbml.jsbml.Reaction;
import sabiork.wizard.gui.ComboBoxModelReactionFilters.ReactionFilter;
import sabiork.wizard.gui.JDialogWizard.ButtonState;
import sabiork.wizard.gui.JDialogWizard.CardID;
import sabiork.wizard.model.WizardModel;
import sabiork.wizard.model.WizardProperties;

@SuppressWarnings("serial")
public class CardReactionsM extends Card implements ListSelectionListener,
		ActionListener, PropertyChangeListener {

	private ComboBoxModelReactionFilters comboBoxReactionFiltersModel;
	private JComboBox comboBoxReactionFilters;
	private JScrollPane tableReactionsScrollPane;
	private JTable tableReactions;
	private TableModelReactions tableReactionsModel;

	public CardReactionsM(JDialogWizard dialog, WizardModel model) {
		super(dialog, model);
		model.addPropertyChangeListener(this);
		initialize();
	}

	private void initialize() {
		comboBoxReactionFiltersModel = new ComboBoxModelReactionFilters();
		comboBoxReactionFilters = new JComboBox(comboBoxReactionFiltersModel);
		comboBoxReactionFilters.setRenderer(comboBoxReactionFiltersModel
				.getRenderer());
		comboBoxReactionFilters.addActionListener(this);

		tableReactionsModel = new TableModelReactions();
		tableReactions = new JTable(tableReactionsModel);
		tableReactions.setRowSelectionAllowed(true);
		tableReactions.setColumnSelectionAllowed(false);
		tableReactions.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		tableReactions.getSelectionModel().addListSelectionListener(this);
		tableReactions.getColumnModel().getColumn(1)
				.setCellRenderer(new TableCellRendererReactions());
		tableReactions.getColumnModel().getColumn(2)
				.setCellRenderer(new TableCellRendererBooleans());
		tableReactions.getColumnModel().getColumn(3)
				.setCellRenderer(new TableCellRendererBooleans());
		tableReactions.getColumnModel().getColumn(4)
				.setCellRenderer(new TableCellRendererBooleans());
		tableReactionsScrollPane = new JScrollPane(tableReactions);

		setLayout(new BorderLayout());
		setBorder(BorderFactory.createTitledBorder(
				BorderFactory.createEtchedBorder(),
				WizardProperties.getText("CARD_REACTIONS_M_TEXT_REACTIONS")));
		add(comboBoxReactionFilters, BorderLayout.NORTH);
		add(tableReactionsScrollPane, BorderLayout.CENTER);
	}

	public void performBeforeShowing() {
		dialog.setButtonState(ButtonState.NEXT_DISABLED);
		tableReactionsModel
				.setReactions(getFilteredReactions(comboBoxReactionFiltersModel
						.getSelectedReactionFilter()));
	}

	public CardID getPreviousCardID() {
		return CardID.METHOD;
	}

	public CardID getNextCardID() {
		return CardID.SEARCH_M;
	}

	/**
	 * Adds the selected {@link Reaction} to the model.
	 */
	private void setSelectedReaction() {
		Reaction selectedReaction = null;
		int selectedRow = tableReactions.getSelectedRow();
		if (selectedRow != -1) {
			selectedReaction = tableReactionsModel.getReactions().get(
					selectedRow);
		}
		model.setSelectedReaction(selectedReaction);
	}

	/**
	 * Returns the reactions according to the <code>reactionFilter</code>.
	 * 
	 * @param reactionFilter
	 * @return a list of {@link Reaction}
	 */
	private List<Reaction> getFilteredReactions(ReactionFilter reactionFilter) {
		List<Reaction> reactions = new ArrayList<Reaction>();
		switch (reactionFilter) {
		case ALL_REACTIONS:
			reactions = model.getReactions();
			break;
		case REACTIONS_WITH_KINETICLAW:
			reactions = model.getReactionsWithKineticLaw();
			break;
		case REACTIONS_WITHOUT_KINETICLAW:
			reactions = model.getReactionsWithoutKineticLaw();
			break;
		case REVERSIBLE_REACTIONS:
			reactions = model.getReversibleReactions();
			break;
		case IRREVERSIBLE_REACTIONS:
			reactions = model.getIrreversibleReactions();
			break;
		case FAST_REACTIONS:
			reactions = model.getFastReactions();
			break;
		case SLOW_REACTIONS:
			reactions = model.getSlowReactions();
			break;
		}
		return reactions;
	}

	public void valueChanged(ListSelectionEvent e) {
		if (e.getSource().equals(tableReactions.getSelectionModel())) {
			if (!e.getValueIsAdjusting()) {
				setSelectedReaction();
			}
		}
	}

	public void actionPerformed(ActionEvent e) {
		if (e.getSource().equals(comboBoxReactionFilters)) {
			tableReactionsModel
					.setReactions(getFilteredReactions(comboBoxReactionFiltersModel
							.getSelectedReactionFilter()));
		}
	}

	public void propertyChange(PropertyChangeEvent e) {
		if (e.getSource().equals(model)
				&& e.getPropertyName().equals("selectedReaction")) {
			if (model.hasSelectedReaction()) {
				dialog.setButtonState(ButtonState.NEXT_ENABLED);
			} else {
				dialog.setButtonState(ButtonState.NEXT_DISABLED);
			}
		}
	}

}
