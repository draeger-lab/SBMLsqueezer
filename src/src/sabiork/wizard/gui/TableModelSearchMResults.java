package sabiork.wizard.gui;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.table.AbstractTableModel;
import javax.xml.stream.XMLStreamException;
import org.sbml.jsbml.KineticLaw;
import org.sbml.jsbml.Reaction;
import sabiork.SABIORK;
import sabiork.wizard.model.WizardProperties;

/**
 * A class that allows the storage of {@link KineticLaw}.
 * 
 * @author Matthias Rall
 * 
 */
@SuppressWarnings("serial")
public class TableModelSearchMResults extends AbstractTableModel {

	private String[] columnNames;
	private List<KineticLaw> kineticLaws;

	public TableModelSearchMResults() {
		this.columnNames = new String[] {
				WizardProperties
						.getText("TABLE_MODEL_SEARCH_M_RESULTS_TEXT_ENTRY_ID"),
				WizardProperties
						.getText("TABLE_MODEL_SEARCH_M_RESULTS_TEXT_REACTION"),
				WizardProperties
						.getText("TABLE_MODEL_SEARCH_M_RESULTS_TEXT_TEMPERATURE"),
				WizardProperties
						.getText("TABLE_MODEL_SEARCH_M_RESULTS_TEXT_PH"),
				WizardProperties
						.getText("TABLE_MODEL_SEARCH_M_RESULTS_TEXT_BUFFER") };
		this.kineticLaws = new ArrayList<KineticLaw>();
	}

	public int getColumnCount() {
		return columnNames.length;
	}

	public String getColumnName(int column) {
		return columnNames[column];
	}

	public int getRowCount() {
		return kineticLaws.size();
	}

	public Object getValueAt(int rowIndex, int columnIndex) {
		KineticLaw kineticLaw = (KineticLaw) kineticLaws.get(rowIndex);
		switch (columnIndex) {
		case 0:
			try {
				return SABIORK.getKineticLawID(kineticLaw);
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			} catch (XMLStreamException e) {
				e.printStackTrace();
			}
		case 1:
			return kineticLaw.getParent();
		case 2:
			return kineticLaw;
		case 3:
			return kineticLaw;
		case 4:
			try {
				return SABIORK.getBuffer(kineticLaw);
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			} catch (XMLStreamException e) {
				e.printStackTrace();
			}
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
			return KineticLaw.class;
		case 3:
			return KineticLaw.class;
		case 4:
			return String.class;
		default:
			return Object.class;
		}
	}

	public boolean isCellEditable(int rowIndex, int columnIndex) {
		return false;
	}

	public List<KineticLaw> getKineticLaws() {
		return kineticLaws;
	}

	public void setKineticLaws(List<KineticLaw> kineticLaws) {
		this.kineticLaws = kineticLaws;
		fireTableDataChanged();
	}

	public void add(KineticLaw kineticLaw) {
		if (!this.kineticLaws.contains(kineticLaw)) {
			this.kineticLaws.add(kineticLaw);
			fireTableDataChanged();
		}
	}

	public void clear() {
		kineticLaws.clear();
		fireTableDataChanged();
	}

}
