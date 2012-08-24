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
import org.sbml.jsbml.Reaction;
import org.sbml.squeezer.sabiork.wizard.model.WizardProperties;

/**
 * A class that allows the storage of {@link Reaction}.
 * 
* @author Matthias Rall
 * @version $Rev$
 * ${tags}
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
