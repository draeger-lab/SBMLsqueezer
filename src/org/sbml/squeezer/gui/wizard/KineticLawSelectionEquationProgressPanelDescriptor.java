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
package org.sbml.squeezer.gui.wizard;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import org.sbml.squeezer.KineticLawGenerator;

import de.zbit.gui.wizard.WizardPanelDescriptor;

/**
 * 
 * @author Sebastian Nagel
 * @version $Rev$
 * @since 1.4
 */
public class KineticLawSelectionEquationProgressPanelDescriptor  extends WizardPanelDescriptor implements PropertyChangeListener {
	public static final String IDENTIFIER = "KINETIC_LAW_EQUATION_PROGRESS_PANEL";
	
	private KineticLawSelectionEquationProgressPanel panel;
	
	/**
	 * 
	 * @param klg
	 */
	public KineticLawSelectionEquationProgressPanelDescriptor(KineticLawGenerator klg) {
		super(IDENTIFIER, new KineticLawSelectionEquationProgressPanel(klg));
		this.panel = ((KineticLawSelectionEquationProgressPanel) this.getPanelComponent());
		this.panel.addPropertyChangeListener(this);
	}
	
	/* (non-Javadoc)
	 * @see de.zbit.gui.wizard.WizardPanelDescriptor#displayingPanel()
	 */
	@Override
	public void displayingPanel() {
		this.getWizard().setNextFinishButtonEnabled(false);
		this.getWizard().setBackButtonEnabled(false);
		
		new Thread(new Runnable() {
			/* (non-Javadoc)
			 * @see java.lang.Runnable#run()
			 */
			public void run() {
				panel.generateKineticLaw();
			}
		}).start();
	}
	
	/* (non-Javadoc)
	 * @see de.zbit.gui.wizard.WizardPanelDescriptor#aboutToHidePanel()
	 */
	@Override
	public void aboutToHidePanel() {
	}    
	
	/* (non-Javadoc)
	 * @see de.zbit.gui.wizard.WizardPanelDescriptor#getNextPanelDescriptor()
	 */
	@Override
	public Object getNextPanelDescriptor() {
		return KineticLawSelectionEquationPanelDescriptor.IDENTIFIER;
	}
	
	/*
 (non-Javadoc)
	 * @see de.zbit.gui.wizard.WizardPanelDescriptor#getBackPanelDescriptor()
	 */
	@Override
	public Object getBackPanelDescriptor() {
		return KineticLawSelectionOptionPanelDescriptor.IDENTIFIER;
	}
	
	/* (non-Javadoc)
	 * @see java.beans.PropertyChangeListener#propertyChange(java.beans.PropertyChangeEvent)
	 */
	//@Override
	public void propertyChange(PropertyChangeEvent evt) {
		if (evt.getPropertyName().equals("generateKineticLawDone")){
			this.getWizard().goToNextPanel();
		}
	}
}
