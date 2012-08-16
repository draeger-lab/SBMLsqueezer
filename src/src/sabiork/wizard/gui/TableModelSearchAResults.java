package sabiork.wizard.gui;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.table.AbstractTableModel;
import javax.xml.stream.XMLStreamException;
import org.sbml.jsbml.Reaction;
import sabiork.SABIORK;
import sabiork.wizard.model.KineticLawImporter;
import sabiork.wizard.model.WizardProperties;

/**
 * A class that allows the storage of {@link SearchAResult}.
 * 
 * @author Matthias Rall
 * 
 */
@SuppressWarnings("serial")
public class TableModelSearchAResults extends AbstractTableModel {

	private String[] columnNames;
	private List<SearchAResult> searchAResults;

	public TableModelSearchAResults() {
		this.columnNames = new String[] {
				WizardProperties
						.getText("TABLE_MODEL_SEARCH_A_RESULTS_TEXT_REACTION"),
				WizardProperties
						.getText("TABLE_MODEL_SEARCH_A_RESULTS_TEXT_SELECTED_REACTION"),
				WizardProperties
						.getText("TABLE_MODEL_SEARCH_A_RESULTS_TEXT_ENTRY_ID"),
				WizardProperties
						.getText("TABLE_MODEL_SEARCH_A_RESULTS_TEXT_MATCHING_ENTRIES"),
				WizardProperties
						.getText("TABLE_MODEL_SEARCH_A_RESULTS_TEXT_PARTIAL/NON-MATCHING_ENTRIES"),
				WizardProperties
						.getText("TABLE_MODEL_SEARCH_A_RESULTS_TEXT_TOTAL_ENTRIES") };
		this.searchAResults = new ArrayList<SearchAResult>();
	}

	public int getColumnCount() {
		return columnNames.length;
	}

	public String getColumnName(int column) {
		return columnNames[column];
	}

	public int getRowCount() {
		return searchAResults.size();
	}

	public Object getValueAt(int rowIndex, int columnIndex) {
		SearchAResult searchAResult = (SearchAResult) searchAResults
				.get(rowIndex);
		KineticLawImporter selectedKineticLawImporter = searchAResult
				.getSelectedKineticLawImporter();
		switch (columnIndex) {
		case 0:
			return searchAResult.getReaction();
		case 1:
			if (selectedKineticLawImporter != null) {
				return selectedKineticLawImporter.getKineticLaw().getParent();
			} else {
				return null;
			}
		case 2:
			if (selectedKineticLawImporter != null) {
				try {
					return SABIORK.getKineticLawID(selectedKineticLawImporter
							.getKineticLaw());
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				} catch (XMLStreamException e) {
					e.printStackTrace();
				}
			} else {
				return "";
			}
		case 3:
			return searchAResult.getPossibleKineticLawImporters().size();
		case 4:
			return searchAResult.getImpossibleKineticLawImporters().size();
		case 5:
			return searchAResult.getTotalKineticLawImporters().size();
		default:
			return new Object();
		}
	}

	public Class<?> getColumnClass(int columnIndex) {
		switch (columnIndex) {
		case 0:
			return Reaction.class;
		case 1:
			return Reaction.class;
		case 2:
			return String.class;
		case 3:
			return Integer.class;
		case 4:
			return Integer.class;
		case 5:
			return Integer.class;
		default:
			return Object.class;
		}
	}

	public boolean isCellEditable(int rowIndex, int columnIndex) {
		return false;
	}

	public List<SearchAResult> getSearchAResults() {
		return searchAResults;
	}

	public void setSearchAResults(List<SearchAResult> searchAResults) {
		this.searchAResults = searchAResults;
		fireTableDataChanged();
	}

	public void add(SearchAResult searchAResult) {
		if (!this.searchAResults.contains(searchAResult)) {
			this.searchAResults.add(searchAResult);
			fireTableDataChanged();
		}
	}

	public void clear() {
		searchAResults.clear();
		fireTableDataChanged();
	}

	public SearchAResult createSearchAResult(Reaction reaction,
			List<KineticLawImporter> possibleKineticLawImporters,
			List<KineticLawImporter> impossibleKineticLawImporters,
			List<KineticLawImporter> totalKineticLawImporters) {
		return new SearchAResult(reaction, possibleKineticLawImporters,
				impossibleKineticLawImporters, totalKineticLawImporters);
	}

	public class SearchAResult {

		private Reaction reaction;
		private List<KineticLawImporter> possibleKineticLawImporters;
		private List<KineticLawImporter> impossibleKineticLawImporters;
		private List<KineticLawImporter> totalKineticLawImporters;

		public SearchAResult(Reaction reaction,
				List<KineticLawImporter> possibleKineticLawImporters,
				List<KineticLawImporter> impossibleKineticLawImporters,
				List<KineticLawImporter> totalKineticLawImporters) {
			this.reaction = reaction;
			this.possibleKineticLawImporters = possibleKineticLawImporters;
			this.impossibleKineticLawImporters = impossibleKineticLawImporters;
			this.totalKineticLawImporters = totalKineticLawImporters;
		}

		public Reaction getReaction() {
			return reaction;
		}

		public List<KineticLawImporter> getPossibleKineticLawImporters() {
			return possibleKineticLawImporters;
		}

		public List<KineticLawImporter> getImpossibleKineticLawImporters() {
			return impossibleKineticLawImporters;
		}

		public List<KineticLawImporter> getTotalKineticLawImporters() {
			return totalKineticLawImporters;
		}

		public KineticLawImporter getSelectedKineticLawImporter() {
			KineticLawImporter selectedKineticLawImporter = null;
			if (!possibleKineticLawImporters.isEmpty()) {
				selectedKineticLawImporter = possibleKineticLawImporters.get(0);
			}
			return selectedKineticLawImporter;
		}

	}

}
