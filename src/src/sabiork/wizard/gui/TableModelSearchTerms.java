package sabiork.wizard.gui;

import java.util.ArrayList;
import java.util.List;
import javax.swing.table.AbstractTableModel;
import sabiork.SABIORK;
import sabiork.util.Pair;
import sabiork.wizard.model.WizardProperties;

/**
 * A class that allows the storage of search terms.
 * 
 * @author Matthias Rall
 * 
 */
@SuppressWarnings("serial")
public class TableModelSearchTerms extends AbstractTableModel {

	private String[] columnNames;
	private List<Pair<SABIORK.QueryField, String>> searchTerms;

	public TableModelSearchTerms() {
		this.columnNames = new String[] {
				WizardProperties
						.getText("TABLE_MODEL_SEARCH_TERMS_TEXT_QUERY_FIELD"),
				WizardProperties.getText("TABLE_MODEL_SEARCH_TERMS_TEXT_VALUE"),
				WizardProperties
						.getText("TABLE_MODEL_SEARCH_TERMS_TEXT_CLICK_TO_REMOVE") };
		this.searchTerms = new ArrayList<Pair<SABIORK.QueryField, String>>();
	}

	public int getColumnCount() {
		return columnNames.length;
	}

	public String getColumnName(int column) {
		return columnNames[column];
	}

	public int getRowCount() {
		return searchTerms.size();
	}

	public Object getValueAt(int rowIndex, int columnIndex) {
		Pair<SABIORK.QueryField, String> searchTerm = (Pair<SABIORK.QueryField, String>) searchTerms
				.get(rowIndex);
		switch (columnIndex) {
		case 0:
			return searchTerm.getKey().toString();
		case 1:
			return searchTerm.getValue();
		case 2:
			return Boolean.TRUE;
		default:
			return new Object();
		}
	}

	public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
		if (columnIndex == 2) {
			fireTableCellUpdated(rowIndex, columnIndex);
		}
	}

	public Class<?> getColumnClass(int columnIndex) {
		switch (columnIndex) {
		case 0:
			return String.class;
		case 1:
			return String.class;
		case 2:
			return Boolean.class;
		default:
			return Object.class;
		}
	}

	public boolean isCellEditable(int rowIndex, int columnIndex) {
		return (columnIndex == 2);
	}

	public List<Pair<SABIORK.QueryField, String>> getSearchTerms() {
		return searchTerms;
	}

	public void setSearchTerms(
			List<Pair<SABIORK.QueryField, String>> searchTerms) {
		this.searchTerms = searchTerms;
		fireTableDataChanged();
	}

	public void add(Pair<SABIORK.QueryField, String> searchTerm) {
		if (!this.searchTerms.contains(searchTerm)) {
			this.searchTerms.add(searchTerm);
			fireTableDataChanged();
		}
	}

	public void clear() {
		searchTerms.clear();
		fireTableDataChanged();
	}

	public void remove(int index) {
		searchTerms.remove(index);
		fireTableDataChanged();
	}

	public String getSearchTermsQuery() {
		return SABIORK.getSearchTermsQuery(searchTerms);
	}

}
