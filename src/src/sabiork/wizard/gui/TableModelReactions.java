package sabiork.wizard.gui;

import java.util.ArrayList;
import java.util.List;
import javax.swing.table.AbstractTableModel;
import org.sbml.jsbml.Reaction;
import sabiork.wizard.model.WizardProperties;

/**
 * A class that allows the storage of {@link Reaction}.
 * 
 * @author Matthias Rall
 * 
 */
@SuppressWarnings("serial")
public class TableModelReactions extends AbstractTableModel {

	private String[] columnNames;
	private List<Reaction> reactions;

	public TableModelReactions() {
		this.columnNames = new String[] {
				WizardProperties.getText("TABLE_MODEL_REACTIONS_TEXT_NAME/ID"),
				WizardProperties.getText("TABLE_MODEL_REACTIONS_TEXT_REACTION"),
				WizardProperties
						.getText("TABLE_MODEL_REACTIONS_TEXT_KINETIC_LAW"),
				WizardProperties
						.getText("TABLE_MODEL_REACTIONS_TEXT_REVERSIBLE"),
				WizardProperties.getText("TABLE_MODEL_REACTIONS_TEXT_FAST") };
		this.reactions = new ArrayList<Reaction>();
	}

	public int getColumnCount() {
		return columnNames.length;
	}

	public String getColumnName(int column) {
		return columnNames[column];
	}

	public int getRowCount() {
		return reactions.size();
	}

	public Object getValueAt(int rowIndex, int columnIndex) {
		Reaction reaction = (Reaction) reactions.get(rowIndex);
		switch (columnIndex) {
		case 0:
			return reaction.toString();
		case 1:
			return reaction;
		case 2:
			return reaction.isSetKineticLaw();
		case 3:
			return reaction.isReversible();
		case 4:
			return reaction.isFast();
		default:
			return new Object();
		}
	}

	public Class<?> getColumnClass(int columnIndex) {
		switch (columnIndex) {
		case 0:
			return String.class;
		case 1:
			return Reaction.class;
		case 2:
			return Boolean.class;
		case 3:
			return Boolean.class;
		case 4:
			return Boolean.class;
		default:
			return Object.class;
		}
	}

	public boolean isCellEditable(int rowIndex, int columnIndex) {
		return false;
	}

	public List<Reaction> getReactions() {
		return reactions;
	}

	public void setReactions(List<Reaction> reactions) {
		this.reactions = reactions;
		fireTableDataChanged();
	}

}
