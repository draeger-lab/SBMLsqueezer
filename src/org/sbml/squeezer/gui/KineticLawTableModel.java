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

import java.util.ResourceBundle;

import javax.swing.table.AbstractTableModel;

import org.sbml.jsbml.KineticLaw;
import org.sbml.jsbml.Reaction;
import org.sbml.jsbml.SBO;
import org.sbml.squeezer.KineticLawGenerator;
import org.sbml.squeezer.OptionsGeneral;
import org.sbml.squeezer.kinetics.BasicKineticLaw;
import org.sbml.squeezer.util.Bundles;

import de.zbit.util.ResourceManager;
import de.zbit.util.prefs.SBPreferences;
import de.zbit.util.progressbar.AbstractProgressBar;

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
	
	private boolean warnings[];
	private KineticLaw kineticLaws[];

	private int numOfWarnings;

	private int maxNumReactants;

	/**
	 * TODO: add comment
	 * @param progressBar 
	 * 
	 * @param maxEducts
	 * @param reactionNumAndKinetic
	 * @param reacNumOfNotExistKinetics
	 * @param reactionNumAndParameters
	 * @param idOfReaction
	 * @param reactionNumAndKineticBezeichnung
	 * @param model
	 */
	public KineticLawTableModel(KineticLawGenerator klg, AbstractProgressBar progressBar) {
		int reactionNum;
		
		columnNames = new String[] { 
				MESSAGES.getString("COL_REACTION"),
				MESSAGES.getString("COL_KINETIC_LAW"),
//				MESSAGES.getString("COL_SBO"),
				//MESSAGES.getString("COL_UNIT"),
				MESSAGES.getString("COL_NUM_REACTANTS"),
				MESSAGES.getString("COL_NUM_PRODUCTS"),
//				MESSAGES.getString("COL_REACTANTS"),
//				MESSAGES.getString("COL_PRODUCTS"),
//				MESSAGES.getString("COL_PARAMETERS"),
				MESSAGES.getString("COL_FORMULA")};
		data = new Object[klg.getCreatedKineticsCount()][this.columnNames.length];
		warnings = new boolean[data.length];
		kineticLaws = new KineticLaw[data.length];
		numOfWarnings = 0;

		maxNumReactants = SBPreferences.getPreferencesFor(OptionsGeneral.class).getInt(OptionsGeneral.MAX_NUMBER_OF_REACTANTS);
		double startTime = System.currentTimeMillis();
		for (reactionNum = 0; reactionNum < klg.getCreatedKineticsCount(); reactionNum++) {
			Reaction reaction = klg.getModifiedReaction(reactionNum);
			
			fillData(reaction, reactionNum);
			
			// Notify progress listener:
			double percent = reactionNum * 100d/klg.getCreatedKineticsCount();
			double remainingTime = 100 * ((System.currentTimeMillis() - startTime) / percent);
			// TODO: Localize
			progressBar.percentageChanged((int) Math.round(percent), remainingTime, "Creating overview");
		}
		progressBar.finished();
	}

	/**
	 * 
	 * @param reaction
	 * @param reactionNum
	 */
	public void fillData(Reaction reaction, int reactionNum) {
		int speciesNum;
		kineticLaws[reactionNum] = reaction.isSetKineticLaw() ? reaction.getKineticLaw() : null;
		String kinetic = (kineticLaws[reactionNum] != null) && kineticLaws[reactionNum].isSetMath() ? kineticLaws[reactionNum].getMath().toFormula() : " - ";
		
//		List<LocalParameter> param = reaction.isSetKineticLaw() ? reaction.getKineticLaw().getListOfLocalParameters() : new LinkedList<LocalParameter>();
		double numReac = 0d, numProduct = 0d;
		warnings[reactionNum] = false;
		for (speciesNum = 0; speciesNum < reaction.getReactantCount(); speciesNum++) {
			numReac += reaction.getReactant(speciesNum).getStoichiometry();
		}
		if (numReac >= maxNumReactants) {
			numOfWarnings++;
			warnings[reactionNum] = true;
		} else if (reaction.isReversible()) {
			for (speciesNum = 0; speciesNum < reaction.getProductCount(); speciesNum++) {
				numProduct += reaction.getProduct(speciesNum).getStoichiometry();
			}
			if (numProduct >= maxNumReactants) {
				numOfWarnings++;
				warnings[reactionNum] = true;
			}
		}
		
//		String reac = "";
//		String pro = "";
//		String para = "";
//
//		if (reaction.getReactantCount() > 0) {
//			reac += reaction.getReactant(0).getSpecies();
//		}
//		for (speciesNum = 1; speciesNum < reaction.getReactantCount(); speciesNum++) {
//			reac += ", " + reaction.getReactant(speciesNum).getSpecies();
//		}
//		if (reaction.getProductCount() > 0) {
//			pro += reaction.getProduct(0).getSpecies();
//		}
//		for (speciesNum = 1; speciesNum < reaction.getProductCount(); speciesNum++) {
//			pro += ", " + reaction.getProduct(speciesNum).getSpecies();
//		}
//		for (int j = 0; j < param.size() - 1; j++) {
//			para += param.get(j).getId() + ", ";
//		}
//		para += param.get(param.size() - 1).getId();

		KineticLaw kl = reaction.getKineticLaw();

		// Reaction Identifier
		int column = -1;
		data[reactionNum][++column] = reaction.isSetName() ? reaction.getName() : reaction.getId();
		// Kinetic Law
		if (kl != null) {
			if (kl.isSetSBOTerm()) {
				data[reactionNum][++column] = SBO.getTerm(kl.getSBOTerm()).getName();
			} else {
				data[reactionNum][++column] =  kl instanceof BasicKineticLaw ? ((BasicKineticLaw) kl).getSimpleName() : kl.toString();
			}
		} else {
			data[reactionNum][++column] = " - ";
		}
		// Derived Unit
		//data[reactionNum][++column] = reaction.getDerivedUnitDefinition();
		// #Reactants
		data[reactionNum][++column] = Double.valueOf(numReac);
//		// Reactants
//		data[reactionNum][++column] = reac;
//		// Products
//		data[reactionNum][++column] = pro;
//		// Parameters
//		data[reactionNum][++column] = para;
		data[reactionNum][++column] = Double.valueOf(numProduct);
		// Formula
		data[reactionNum][++column] = kinetic;
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
	
	/**
	 * 
	 * @param rowIndex
	 * @return
	 */
	public boolean hasTooManyReactionParticipants(int rowIndex) {
		return warnings[rowIndex];
	}
	
	/**
	 * 
	 * @param rowIndex
	 * @return
	 */
	public KineticLaw getKineticLaw(int rowIndex) {
		return kineticLaws[rowIndex];
	}

}
