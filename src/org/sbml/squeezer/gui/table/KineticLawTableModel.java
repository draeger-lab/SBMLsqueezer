package org.sbml.squeezer.gui.table;

import javax.swing.table.AbstractTableModel;

import org.sbml.jlibsbml.ListOf;
import org.sbml.jlibsbml.Parameter;
import org.sbml.jlibsbml.Reaction;
import org.sbml.squeezer.CfgKeys;
import org.sbml.squeezer.KineticLawGenerator;

/**
 * TODO: comment missing
 * 
 * @since 2.0
 * @version
 * @author Nadine Hassis <Nadine.hassis@gmail.com>
 * @author Andreas Dr&auml;ger (draeger) <andreas.draeger@uni-tuebingen.de>
 *         Copyright (c) ZBiT, University of T&uuml;bingen, Germany Compiler:
 *         JDK 1.5.0
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

		columnNames = new String[] { "Reaction Identifier", "Kinetic Law",
				"SBO", "#Reactants", "Reactants", "Products", "Parameters",
				"Formula" };
		data = new Object[klg.getNumCreatedKinetics()][this.columnNames.length];
		numOfWarnings = 0;

		int maxNumReactants = ((Integer) (klg.getSettings().get(CfgKeys.OPT_MAX_NUMBER_OF_REACTANTS))).intValue();
		for (reactionNum = 0; reactionNum < klg.getNumCreatedKinetics(); reactionNum++) {
			Reaction reaction = klg.getModifiedReaction(reactionNum);
			if (reaction.getNumReactants() >= maxNumReactants)
				numOfWarnings++;
			else
				for (speciesNum = 0; speciesNum < reaction.getNumReactants(); speciesNum++)
					if (reaction.getReactant(speciesNum).getStoichiometry() >= maxNumReactants) {
						numOfWarnings++;
						break;
					}
			String kinetic = reaction.getKineticLaw().getFormula();
			ListOf<Parameter> param = reaction.getKineticLaw().getListOfParameters();
			numReac = 0;
			for (speciesNum = 0; speciesNum < reaction.getNumReactants(); speciesNum++)
				numReac += reaction.getReactant(speciesNum).getStoichiometry();
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

			// Reaction Identifier
			data[reactionNum][0] = reaction.getId();
			// Kinetic Law
			data[reactionNum][1] = reaction.toString();
			// SBO
			data[reactionNum][2] = new String(reaction.getSBOTermID());
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
