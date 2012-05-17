/*
 * $Id: KineticLawSelectionEquationPanelDescriptor.java 830 2012-02-26 00:33:31Z snagel $
 * $URL: https://rarepos.cs.uni-tuebingen.de/svn-path/SBMLsqueezer/trunk/src/org/sbml/squeezer/gui/wizard/KineticLawSelectionEquationPanelDescriptor.java $
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

import org.sbml.squeezer.KineticLawGenerator;
import org.sbml.squeezer.io.SBMLio;

import de.zbit.gui.StatusBar;
import de.zbit.gui.wizard.WizardPanelDescriptor;

/**
 * 
 * @author Sebastian Nagel
 * @date Feb 25, 2012
 * @since 1.4
 * @version $Rev: 830 $
 */
public class KineticLawSelectionEquationPanelDescriptor extends WizardPanelDescriptor {
	public static final String IDENTIFIER = "KINETIC_LAW_EQUATION_PANEL";
	
	private KineticLawSelectionEquationPanel panel;
	
	public KineticLawSelectionEquationPanelDescriptor(KineticLawGenerator klg, SBMLio sbmlIO) {
		super(IDENTIFIER, new KineticLawSelectionEquationPanel(klg, sbmlIO));
		this.panel = ((KineticLawSelectionEquationPanel) this.getPanelComponent());
	}
	
	public void displayingPanel() {
		panel.generateKineticLawDone();
	}
	
	public void aboutToHidePanel() {
		panel.apply();
    }    

	public Object getNextPanelDescriptor() {
        return FINISH;
    }
    
    public Object getBackPanelDescriptor() {
        return KineticLawSelectionOptionPanelDescriptor.IDENTIFIER;
    }
	
	public void setStatusBar(StatusBar statusBar) {
		((KineticLawSelectionEquationPanel) this.getPanelComponent()).setStatusBar(statusBar);
	}

}
