/*
 * $Id: KineticLawSelectionOptionPanelDescriptor.java 830 2012-02-26 00:33:31Z snagel $
 * $URL: https://rarepos.cs.uni-tuebingen.de/svn-path/SBMLsqueezer/trunk/src/org/sbml/squeezer/gui/wizard/KineticLawSelectionOptionPanelDescriptor.java $
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

package org.sbml.squeezer.gui.wizard;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JDialog;

import de.zbit.gui.wizard.WizardPanelDescriptor;

/**
 * 
 * @author Sebastian Nagel
 * @date Feb 25, 2012
 * @since 1.4
 * @version $Rev: 830 $
 */
public class KineticLawSelectionOptionPanelDescriptor extends WizardPanelDescriptor implements ActionListener {
	
	/**
	 * 
	 */
	public static final String IDENTIFIER = "KINETIC_LAW_OPTION_PANEL";
	
	/**
	 * 
	 * @param dialog
	 */
	public KineticLawSelectionOptionPanelDescriptor(JDialog dialog) {
		super(IDENTIFIER, new KineticLawSelectionOptionPanel(dialog));
	}
	
	/* (non-Javadoc)
	 * @see de.zbit.gui.wizard.WizardPanelDescriptor#aboutToDisplayPanel()
	 */
	@Override
	public void aboutToDisplayPanel() {
	}
	
	/* (non-Javadoc)
	 * @see de.zbit.gui.wizard.WizardPanelDescriptor#getNextPanelDescriptor()
	 */
	@Override
	public Object getNextPanelDescriptor() {
		return KineticLawSelectionEquationPanelDescriptor.IDENTIFIER;
	}
	
	/* (non-Javadoc)
	 * @see de.zbit.gui.wizard.WizardPanelDescriptor#getBackPanelDescriptor()
	 */
	@Override
	public Object getBackPanelDescriptor() {
		return null;
	}
	
	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent arg0) {
		// TODO Auto-generated method stub
	}

}
