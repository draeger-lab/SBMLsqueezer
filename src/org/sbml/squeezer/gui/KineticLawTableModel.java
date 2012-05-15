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
package org.sbml.squeezer.gui;

import java.util.LinkedList;
import java.util.List;
import java.util.ResourceBundle;

import javax.swing.table.AbstractTableModel;

import org.sbml.jsbml.KineticLaw;
import org.sbml.jsbml.LocalParameter;
import org.sbml.jsbml.Reaction;
import org.sbml.squeezer.KineticLawGenerator;
import org.sbml.squeezer.SqueezerOptions;
import org.sbml.squeezer.kinetics.BasicKineticLaw;
import org.sbml.squeezer.util.Bundles;

import de.zbit.util.ResourceManager;
import de.zbit.util.prefs.SBPreferences;

/**
 * Data model for the {@link KineticLawTable} that determins, which information
 * is displayed there and it provides methods to access comlumn or row names and
 * single elements in the table.
 * 
 * @author Nadine Hassis
 * @author Andreas Dr&auml;ger
 * @date 2007-08-01
 * @since 1.0
 * @version $Rev$
 */
public class KineticLawTableModel extends AbstractTableModel {
	/**
	 * 
	 */
	public static final transient ResourceBundle MESSAGES = ResourceManager.getBundle(Bundles.MESSAGES);
	
	/**
	 * Generated serial ID.
	 */
	private static final long serialVersionUID = -5755507700427800869L;

	private Object[][] data;

	private String[] columnNames;

	private int numOfWarnings;

	/**
	 * TODO: add comment
	 * 
	 * @param maxEducts
	 * @param reactionNumAndKinetic
	 * @param reacNumOfNotExistKinetics
	 * @param reactionNumAndParameters
	 * @param idOfReaction
	 * @param reactionNumAndKineticBezeichnung
	 * @param model
	 */
	public KineticLawTableModel(KineticLawGenerator klg) {
		int reactionNum, speciesNum;
		double numReac;

		columnNames = new String[] { 
				MESSAGES.getString("COL_REACTION"),
				MESSAGES.getString("COL_KINETIC_LAW"),
				MESSAGES.getString("COL_SBO"),
				MESSAGES.getString("COL_UNIT"),
				MESSAGES.getString("COL_NUM_REACTANTS"),
				MESSAGES.getString("COL_REACTANTS"),
				MESSAGES.getString("COL_PRODUCTS"),
				MESSAGES.getString("COL_PARAMETERS"),
				MESSAGES.getString("COL_FORMULA")};
		data = new Object[klg.getCreatedKineticsCount()][this.columnNames.length];
		numOfWarnings = 0;

		int maxNumReactants = SBPreferences.getPreferencesFor(SqueezerOptions.class).getInt(SqueezerOptions.MAX_NUMBER_OF_REACTANTS);
		for (reactionNum = 0; reactionNum < klg.getCreatedKineticsCount(); reactionNum++) {
			Reaction reaction = klg.getModifiedReaction(reactionNum);
			String kinetic = reaction.isSetKineticLaw() && reaction.getKineticLaw().isSetMath() ? reaction.getKineticLaw().getMath().toFormula() : "";
			List<LocalParameter> param = reaction.isSetKineticLaw() ? reaction.getKineticLaw().getListOfLocalParameters() : new LinkedList<LocalParameter>();
			numReac = 0;
			for (speciesNum = 0; speciesNum < reaction.getReactantCount(); speciesNum++) {
				numReac += reaction.getReactant(speciesNum).getStoichiometry();
			}
			if (numReac >= maxNumReactants) {
				numOfWarnings++;
			}
			String reac = "";
			String pro = "";
			String para = "";

			if (reaction.getReactantCount() > 0) {
				reac += reaction.getReactant(0).getSpecies();
			}
			for (speciesNum = 1; speciesNum < reaction.getReactantCount(); speciesNum++) {
				reac += ", " + reaction.getReactant(speciesNum).getSpecies();
			}
			if (reaction.getProductCount() > 0) {
				pro += reaction.getProduct(0).getSpecies();
			}
			for (speciesNum = 1; speciesNum < reaction.getProductCount(); speciesNum++) {
				pro += ", " + reaction.getProduct(speciesNum).getSpecies();
			}
			for (int j = 0; j < param.size() - 1; j++) {
				para += param.get(j).getId() + ", ";
			}
			para += param.get(param.size() - 1).getId();

			KineticLaw kl = reaction.getKineticLaw();

			// Reaction Identifier
			data[reactionNum][0] = reaction.getId();
			// Kinetic Law
			if (kl != null) {
				data[reactionNum][1] = kl instanceof BasicKineticLaw ? ((BasicKineticLaw) kl)
						.getSimpleName()
						: kl.toString();
						// SBO
						data[reactionNum][2] = kl.getSBOTermID();
			}
			// Derived Unit
			data[reactionNum][3] = reaction.getDerivedUnitDefinition();
			// #Reactants
			data[reactionNum][4] = Double.valueOf(numReac);
			// Reactants
			data[reactionNum][5] = reac;
			// Products
			data[reactionNum][6] = pro;
			// Parameters
			data[reactionNum][7] = para;
			// Formula
			data[reactionNum][8] = kinetic;
		}
	}

	/* (non-Javadoc)
	 * @see javax.swing.table.TableModel#getColumnCount()
	 */
	public int getColumnCount() {
		return columnNames.length;
	}

	/* (non-Javadoc)
	 * @see javax.swing.table.AbstractTableModel#getColumnName(int)
	 */
	@Override
	public String getColumnName(int column) {
		return columnNames[column];
	}

	/**
	 * Returns the number of rows in the table, which mark that the
	 * stoichiometry on the left hand side is to large.
	 * 
	 * @return
	 */
	public int getWarningCount() {
		return numOfWarnings;
	}

	/* (non-Javadoc)
	 * @see javax.swing.table.TableModel#getRowCount()
	 */
	public int getRowCount() {
		return data.length;
	}

	/* (non-Javadoc)
	 * @see javax.swing.table.TableModel#getValueAt(int, int)
	 */
	public Object getValueAt(int row, int column) {
		return data[row][column];
	}

	/* (non-Javadoc)
	 * @see javax.swing.table.AbstractTableModel#isCellEditable(int, int)
	 */
	@Override
	public boolean isCellEditable(int row, int col) {
		return col == 1;
	}

	/* (non-Javadoc)
	 * @see javax.swing.table.AbstractTableModel#setValueAt(java.lang.Object, int, int)
	 */
	@Override
	public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
		data[rowIndex][columnIndex] = aValue;
		fireTableCellUpdated(rowIndex, columnIndex);
	}

}
