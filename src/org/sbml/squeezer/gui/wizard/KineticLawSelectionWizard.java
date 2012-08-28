/*
 * $Id: KineticLawSelectionWizard.java 830 2012-02-26 00:33:31Z snagel $
 * $URL: https://rarepos.cs.uni-tuebingen.de/svn-path/SBMLsqueezer/trunk/src/org/sbml/squeezer/gui/wizard/KineticLawSelectionWizard.java $
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

import java.awt.Dimension;
import java.awt.Frame;
import java.beans.PropertyChangeListener;
import java.util.ResourceBundle;

import javax.swing.JDialog;

import org.sbml.squeezer.KineticLawGenerator;
import org.sbml.squeezer.gui.GUITools;
import org.sbml.squeezer.io.SBMLio;
import org.sbml.squeezer.util.Bundles;

import de.zbit.gui.StatusBar;
import de.zbit.gui.wizard.Wizard;
import de.zbit.gui.wizard.WizardPanelDescriptor;
import de.zbit.util.ResourceManager;
import de.zbit.util.prefs.SBPreferences;

/**
 * 
 * @author Sebastian Nagel
 * @date Feb 25, 2012
 * @since 1.4
 * @version $Rev: 830 $
 */
public class KineticLawSelectionWizard extends Wizard implements PropertyChangeListener {
	
	public static final transient ResourceBundle MESSAGES = ResourceManager.getBundle(Bundles.MESSAGES);

	private SBMLio sbmlIO;
	
	SBPreferences prefs;

	private StatusBar statusBar;

	private JDialog dialog;
	
	/**
	 * 
	 * @param owner
	 * @param sbmlIO
	 * @param reactionID
	 */
	public KineticLawSelectionWizard(Frame owner, SBMLio sbmlIO){
		super(owner);
		
		this.sbmlIO = sbmlIO;
		this.dialog = this.getDialog();
		
		dialog.setTitle(MESSAGES.getString("SBMLSQUEEZER"));
		dialog.setMinimumSize(new Dimension(650,250));
		dialog.setLocationRelativeTo(owner);
		
		setModal(true);
		setWarningVisible(false);
		
		initDescriptors();
	}

	/**
	 * 
	 */
	private void initDescriptors() {
		//try {
			WizardPanelDescriptor descriptor1 = new KineticLawSelectionOptionPanelDescriptor(this.getDialog());
		    registerWizardPanel(KineticLawSelectionOptionPanelDescriptor.IDENTIFIER, descriptor1);
		    
			KineticLawGenerator klg = null;
			try {
				klg = new KineticLawGenerator(this.sbmlIO.getSelectedModel());
			} catch (ClassNotFoundException e) {
				GUITools.showErrorMessage(this.getDialog(), e);
			}
			
			WizardPanelDescriptor descriptor2 = new KineticLawSelectionEquationProgressPanelDescriptor(klg);
		    registerWizardPanel(KineticLawSelectionEquationProgressPanelDescriptor.IDENTIFIER, descriptor2);
		    
		    WizardPanelDescriptor descriptor3 = new KineticLawSelectionEquationPanelDescriptor(klg, this.sbmlIO);
		    ((KineticLawSelectionEquationPanelDescriptor) descriptor3).setStatusBar(statusBar);
		    registerWizardPanel(KineticLawSelectionEquationPanelDescriptor.IDENTIFIER, descriptor3);
		    
		    setCurrentPanel(KineticLawSelectionOptionPanelDescriptor.IDENTIFIER);
		    
//		}  catch (Exception exc) {
//			GUITools.showErrorMessage(this.getDialog(), exc);
//		}
	}
	
	/**
	 * Method that indicates whether or not changes have been introduced into
	 * the given model.
	 * 
	 * @return True if kinetic equations and parameters or anything else were
	 *         changed by SBMLsqueezer.
	 */
	public boolean isKineticsAndParametersStoredInSBML() {
		boolean result = true;
		KineticLawSelectionEquationPanelDescriptor desc = (KineticLawSelectionEquationPanelDescriptor) this.getPanel(KineticLawSelectionEquationPanelDescriptor.IDENTIFIER);
		try{result = ((KineticLawSelectionEquationPanel) desc.getPanelComponent()).isKineticsAndParametersStoredInSBML();}
		catch(Exception e) {};
		return result;
	}

}
