/*
 * $Id$
 * $URL$
 * ---------------------------------------------------------------------
 * This file is part of SBMLsqueezer, a Java program that creates rate 
 * equations for reactions in SBML files (http://sbml.org).
 *
 * Copyright (C) 2006-2012 by the University of Tuebingen, Germany.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 * ---------------------------------------------------------------------
 */
package org.sbml.squeezer.sabiork.wizard.gui;

import java.util.ArrayList;
import java.util.List;
import javax.swing.table.AbstractTableModel;
import org.sbml.squeezer.sabiork.SABIORK;
import org.sbml.squeezer.sabiork.util.Pair;
import org.sbml.squeezer.sabiork.wizard.model.WizardProperties;

/**
 * A class that allows the storage of search terms.
 * 
* @author Matthias Rall
 * @version $Rev$
 */
public class TableModelSearchTerms extends AbstractTableModel {

	/**
	 * Generated serial version identifier.
	 */
	private static final long serialVersionUID = 1170618309623096342L;
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
