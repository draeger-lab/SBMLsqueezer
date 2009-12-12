package org.sbmlsqueezer.gui.table;

import java.util.List;

import javax.swing.table.AbstractTableModel;

import jp.sbi.celldesigner.plugin.PluginReaction;

import org.sbmlsqueezer.kinetics.KineticLawGenerator;

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
	public KineticLawTableModel(KineticLawGenerator klg, int maxEducts) {
		int reactionNum, speciesNum;
		double numReac;

		columnNames = new String[] { "Reaction Identifier", "Kinetic Law",
				"SBO", "#Reactants", "Reactants", "Products", "Parameters",
				"Formula" };
		data = new Object[klg.getReactionNumOfNotExistKinetics().size()][this.columnNames.length];
		numOfWarnings = 0;

		for (reactionNum = 0; reactionNum < klg
				.getReactionNumOfNotExistKinetics().size(); reactionNum++) {
			PluginReaction reaction = klg.getModel().getReaction(
					klg.getReactionNumOfNotExistKinetics().get(reactionNum));
			if (reaction.getNumReactants() >= maxEducts)
				numOfWarnings++;
			else
				for (speciesNum = 0; speciesNum < reaction.getNumReactants(); speciesNum++)
					if (reaction.getReactant(speciesNum).getStoichiometry() >= maxEducts) {
						numOfWarnings++;
						break;
					}
			String kinetic = klg.getReactionNumAndKineticLaw().get(
					klg.getReactionNumOfNotExistKinetics().get(reactionNum))
					.getFormula();
			String id = klg.getModel().getReaction(
					klg.getReactionNumOfNotExistKinetics().get(reactionNum)
							.intValue()).getId();
			List<String> param = klg.getReactionNumAndKineticLaw().get(
					klg.getReactionNumOfNotExistKinetics().get(reactionNum))
					.getParameters();
			numReac = 0;
			for (speciesNum = 0; speciesNum < reaction.getNumReactants(); speciesNum++)
				numReac += reaction.getReactant(speciesNum).getStoichiometry();
			String reac = "";
			String pro = "";
			String para = "";

			for (speciesNum = 0; speciesNum < reaction.getNumReactants() - 1; speciesNum++)
				reac += reaction.getReactant(speciesNum).getSpecies() + ", ";
			reac += reaction.getReactant(speciesNum).getSpecies();

			for (speciesNum = 0; speciesNum < reaction.getNumProducts() - 1; speciesNum++)
				pro += reaction.getProduct(speciesNum).getSpecies() + ", ";
			pro += reaction.getProduct(speciesNum).getSpecies();

			for (int j = 0; j < param.size() - 1; j++)
				para += param.get(j) + ", ";
			para += param.get(param.size() - 1);

			// Reaction Identifier
			data[reactionNum][0] = id;
			// Kinetic Law
			data[reactionNum][1] = klg.getReactionNumAndKineticLaw().get(
			    klg.getReactionNumOfNotExistKinetics().get(reactionNum));
			// SBO
			data[reactionNum][2] = new String(klg.getReactionNumAndKineticLaw()
					.get(
							klg.getReactionNumOfNotExistKinetics().get(
									reactionNum)).getSBO());
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

	public int getColumnCount() {
		return columnNames.length;
	}

	@Override
	public String getColumnName(int column) {
		return columnNames[column];
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

	@Override
	public boolean isCellEditable(int row, int col) {
		if (col == 1)
			return true;
		return false;
	}

	@Override
	public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
		data[rowIndex][columnIndex] = aValue;
		fireTableCellUpdated(rowIndex, columnIndex);
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
}
