/*
 * $$Id${file_name} ${time} ${user} $$
 * $$URL${file_name} $$
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

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.table.AbstractTableModel;
import javax.xml.stream.XMLStreamException;
import org.sbml.jsbml.KineticLaw;
import org.sbml.jsbml.Reaction;
import org.sbml.squeezer.sabiork.SABIORK;
import org.sbml.squeezer.sabiork.wizard.model.WizardProperties;

/**
 * A class that allows the storage of {@link KineticLaw}.
 * 
 * @author Matthias Rall
 * @version $$Rev$$
 * ${tags}
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
