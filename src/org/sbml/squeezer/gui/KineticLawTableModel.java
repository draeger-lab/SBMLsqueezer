/*
 *  SBMLsqueezer creates rate equations for reactions in SBML files
 *  (http://sbml.org).
 *  Copyright (C) 2009 ZBIT, University of Tübingen, Andreas Dräger
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.sbml.squeezer.gui;

import javax.swing.table.AbstractTableModel;

import org.sbml.jsbml.KineticLaw;
import org.sbml.jsbml.ListOf;
import org.sbml.jsbml.Parameter;
import org.sbml.jsbml.Reaction;
import org.sbml.squeezer.CfgKeys;
import org.sbml.squeezer.KineticLawGenerator;
import org.sbml.squeezer.kinetics.BasicKineticLaw;

/**
 * Data model for the {@link KineticLawTable} that determins, which information
 * is displayed there and it provides methods to access comlumn or row names and
 * single elements in the table.
 * 
 * @since 1.0
 * @version
 * @author Nadine Hassis <Nadine.hassis@gmail.com>
 * @author Andreas Dr&auml;ger (draeger) <andreas.draeger@uni-tuebingen.de>
 * @date Aug 1, 2007
 */
public class KineticLawTableModel extends AbstractTableModel {
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

		columnNames = new String[] { "Reaction", "Kinetic Law",
				"SBO", "#Reactants", "Reactants", "Products", "Parameters",
				"Formula" };
		data = new Object[klg.getNumCreatedKinetics()][this.columnNames.length];
		numOfWarnings = 0;

		int maxNumReactants = ((Integer) (klg.getSettings()
				.get(CfgKeys.OPT_MAX_NUMBER_OF_REACTANTS))).intValue();
		for (reactionNum = 0; reactionNum < klg.getNumCreatedKinetics(); reactionNum++) {
			Reaction reaction = klg.getModifiedReaction(reactionNum);
			String kinetic = reaction.getKineticLaw().getFormula();
			ListOf<Parameter> param = reaction.getKineticLaw()
					.getListOfParameters();
			numReac = 0;
			for (speciesNum = 0; speciesNum < reaction.getNumReactants(); speciesNum++)
				numReac += reaction.getReactant(speciesNum).getStoichiometry();
			if (numReac >= maxNumReactants)
				numOfWarnings++;
			String reac = "";
			String pro = "";
			String para = "";

			if (reaction.getNumReactants() > 0)
				reac += reaction.getReactant(0).getSpecies();
			for (speciesNum = 1; speciesNum < reaction.getNumReactants(); speciesNum++)
				reac += ", " + reaction.getReactant(speciesNum).getSpecies();

			if (reaction.getNumProducts() > 0)
				pro += reaction.getProduct(0).getSpecies();
			for (speciesNum = 1; speciesNum < reaction.getNumProducts(); speciesNum++)
				pro += ", " + reaction.getProduct(speciesNum).getSpecies();

			for (int j = 0; j < param.size() - 1; j++)
				para += param.get(j).getId() + ", ";
			para += param.get(param.size() - 1).getId();

			KineticLaw kl = reaction.getKineticLaw();

			// Reaction Identifier
			data[reactionNum][0] = reaction.getId();
			// Kinetic Law
			data[reactionNum][1] = kl instanceof BasicKineticLaw ? ((BasicKineticLaw) kl)
					.getSimpleName()
					: kl.toString();
			// SBO
			data[reactionNum][2] = kl.getSBOTermID();
			// #Reactants
			data[reactionNum][3] = Double.valueOf(numReac);
			// Reactants
			data[reactionNum][4] = reac;
			// Products
			data[reactionNum][5] = pro;
			// Parameters
			data[reactionNum][6] = para;
			// Formula
			data[reactionNum][7] = kinetic;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.table.TableModel#getColumnCount()
	 */
	public int getColumnCount() {
		return columnNames.length;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.table.AbstractTableModel#getColumnName(int)
	 */
	// @Override
	public String getColumnName(int column) {
		return columnNames[column];
	}

	/**
	 * Returns the number of rows in the table, which mark that the
	 * stoichiometry on the left hand side is to large.
	 * 
	 * @return
	 */
	public int getNumOfWarnings() {
		return numOfWarnings;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.table.TableModel#getRowCount()
	 */
	public int getRowCount() {
		return data.length;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.table.TableModel#getValueAt(int, int)
	 */
	public Object getValueAt(int row, int column) {
		return data[row][column];
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.table.AbstractTableModel#isCellEditable(int, int)
	 */
	// @Override
	public boolean isCellEditable(int row, int col) {
		if (col == 1)
			return true;
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.table.AbstractTableModel#setValueAt(java.lang.Object,
	 * int, int)
	 */
	// @Override
	public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
		data[rowIndex][columnIndex] = aValue;
		fireTableCellUpdated(rowIndex, columnIndex);
	}
}
