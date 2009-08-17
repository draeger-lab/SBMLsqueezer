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
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.sbml.squeezer.gui;

import java.awt.GridBagLayout;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;

import org.sbml.KineticLaw;
import org.sbml.ListOf;
import org.sbml.Model;
import org.sbml.ModifierSpeciesReference;
import org.sbml.NamedSBase;
import org.sbml.Parameter;
import org.sbml.Reaction;
import org.sbml.SBase;
import org.sbml.SimpleSpeciesReference;
import org.sbml.Species;
import org.sbml.SpeciesReference;

/**
 * @author Andreas Dr&auml;ger <a
 *         href="mailto:andreas.draeger@uni-tuebingen.de">
 *         andreas.draeger@uni-tuebingen.de</a>
 * 
 */
public class SBasePanel extends JPanel {

	/**
	 * Generated serial version id.
	 */
	private static final long serialVersionUID = -4969096536922920641L;

	public SBasePanel(SBase sbase) {
		super();
		GridBagLayout gbl = new GridBagLayout();
		setLayout(gbl);
		int row = -1;
		String className = sbase.getClass().getName();
		className = className.substring(className.lastIndexOf('.') + 1);
        setBorder(BorderFactory.createTitledBorder(" " + className + " "));
		if (sbase instanceof NamedSBase) {
			NamedSBase nsb = (NamedSBase) sbase;
		    LayoutHelper.addComponent(this, gbl, new JLabel("Identifier"), 0, ++row, 1, 1, 1, 1);
		    LayoutHelper.addComponent(this, gbl, new JTextField(nsb.getId()), 1, row, 1, 1, 1, 1);
		    LayoutHelper.addComponent(this, gbl, new JLabel("Name"), 0, ++row, 1, 1, 1, 1);
		    LayoutHelper.addComponent(this, gbl, new JTextField(nsb.getName()), 1, row, 1, 1, 1, 1);
		}
		LayoutHelper.addComponent(this, gbl, new JLabel("Meta identifier"), 0, ++row, 1, 1, 1, 1);
		LayoutHelper.addComponent(this, gbl, new JTextField(sbase.getMetaid()), 1, row, 1, 1, 1, 1);
		LayoutHelper.addComponent(this, gbl, new JLabel("Notes"), 0, ++row, 1, 1, 1, 1);
		LayoutHelper.addComponent(this, gbl, new JTextField(sbase.getNotes()), 1, row, 1, 1, 1, 1);
		LayoutHelper.addComponent(this, gbl, new JLabel("SBO term"), 0, ++row, 1, 1, 1, 1);
		LayoutHelper.addComponent(this, gbl, new JTextField(sbase.getSBOTermID()), 1, row, 1, 1, 1, 1);
		if (sbase instanceof KineticLaw) {
			// TODO
		} else if (sbase instanceof ListOf) {
			// TODO			
		} else if (sbase instanceof Model) {
			// TODO
		} else if (sbase instanceof SimpleSpeciesReference) {
			// TODO
			if (sbase instanceof SpeciesReference) {
				// TODO
		    } else if (sbase instanceof ModifierSpeciesReference) {
				// TODO
			}			
		} else if (sbase instanceof Parameter) {
			// TODO
		} else if (sbase instanceof Reaction) {
			// TODO
	    } else if (sbase instanceof Species) {
			Species species = (Species) sbase;
			JSpinner spinInitial;
			if (species.isSetInitialAmount()) {
				spinInitial = new JSpinner(new SpinnerNumberModel(species.getInitialAmount(), 0d, Double.MAX_VALUE, .1d));
				LayoutHelper.addComponent(this, gbl, new JLabel("Initial amount"), 0, ++row, 1, 1, 1, 1);
				LayoutHelper.addComponent(this, gbl, spinInitial, 1, row, 1, 1, 1, 1);				
			} else {
				if (species.isSetInitialConcentration())
				spinInitial = new JSpinner(new SpinnerNumberModel(species.getInitialConcentration(), 0d, Double.MAX_VALUE, .1d));
				else spinInitial  = new JSpinner(new SpinnerNumberModel(0, 0d, Double.MAX_VALUE, .1d));
				LayoutHelper.addComponent(this, gbl, new JLabel("Initial amount"), 0, ++row, 1, 1, 1, 1);
				LayoutHelper.addComponent(this, gbl, spinInitial, 1, row, 1, 1, 1, 1);
			}
//			JSpinner spinCharge = new JSpinner(new SpinnerNumberModel())
//			LayoutHelper.addComponent(this, gbl, new JLabel("Charge"), 0, ++row, 1, 1, 1, 1);
//			LayoutHelper.addComponent(this, gbl, species.getCharge(), 1, row, 1, 1, 1, 1);				
			LayoutHelper.addComponent(this, gbl, new JCheckBox("Boundary condition", species.getBoundaryCondition()), 0, ++row, 2, 1, 1, 1);
			LayoutHelper.addComponent(this, gbl, new JCheckBox("Constant", species.getConstant()), 0, ++row, 2, 1, 1, 1);
			LayoutHelper.addComponent(this, gbl, new JCheckBox("Has only substance units", species.getHasOnlySubstanceUnits()), 0, ++row, 2, 1, 1, 1);
	    }
	}
}
