/*
 * $Id: KineticLawSelectionWizard.java 830 2012-02-26 00:33:31Z snagel $
 * $URL: https://rarepos.cs.uni-tuebingen.de/svn-path/SBMLsqueezer/trunk/src/org/sbml/squeezer/gui/wizard/KineticLawSelectionWizard.java $
 * ---------------------------------------------------------------------
 * This file is part of SBMLsqueezer, a Java program that creates rate
 * equations for reactions in SBML files (http://sbml.org).
 *
 * Copyright (C) 2006-2016 by the University of Tuebingen, Germany.
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

import static de.zbit.util.Utils.getMessage;

import java.awt.Dimension;
import java.awt.Frame;
import java.util.ResourceBundle;
import java.util.logging.Logger;

import javax.swing.JDialog;

import org.sbml.squeezer.KineticLawGenerator;
import org.sbml.squeezer.io.SBMLio;
import org.sbml.squeezer.util.Bundles;

import de.zbit.gui.GUITools;
import de.zbit.gui.wizard.Wizard;
import de.zbit.gui.wizard.WizardPanelDescriptor;
import de.zbit.util.ResourceManager;

/**
 * This class implements a Wizard for the KineticLawGenerator. (Feb 25, 2012)
 * 
 * @see Wizard
 * @see KineticLawGenerator
 * 
 * @author Sebastian Nagel
 * @since 2.0
 *
 */
public class KineticLawSelectionWizard extends Wizard {
  
  /**
   * Localization support.
   */
  public static final transient ResourceBundle MESSAGES = ResourceManager.getBundle(Bundles.MESSAGES);
  
  /**
   * A {@link Logger} for this class.
   */
  private static final transient Logger logger = Logger.getLogger(KineticLawSelectionWizard.class.getName());
  
  private SBMLio<?> sbmlIO;
  
  private JDialog dialog;
  
  /**
   * 
   * @param owner
   * @param sbmlIO
   */
  public KineticLawSelectionWizard(Frame owner, SBMLio<?> sbmlIO) {
    super(owner);
    
    this.sbmlIO = sbmlIO;
    dialog = getDialog();
    
    // set dialog properties
    dialog.setTitle(System.getProperty("app.name"));
    dialog.setMinimumSize(new Dimension(650, 250));
    dialog.setLocationRelativeTo(owner);
    
    setModal(true);
    setWarningVisible(false);
    
    initDescriptors();
  }
  
  /**
   * init all descriptor (panels)
   */
  private void initDescriptors() {
    // try to init KineticLawGenerator with the selected model
    KineticLawGenerator klg = null;
    try {
      klg = new KineticLawGenerator(sbmlIO.getSelectedModel());
    } catch (ClassNotFoundException e) {
      GUITools.showErrorMessage(getDialog(), e);
    }
    
    // option panel
    WizardPanelDescriptor descriptor1 = new KineticLawSelectionOptionPanelDescriptor(klg);
    registerWizardPanel(KineticLawSelectionOptionPanelDescriptor.IDENTIFIER, descriptor1);
    
    
    // progress panel
    WizardPanelDescriptor descriptor2 = new KineticLawSelectionEquationProgressPanelDescriptor(klg);
    registerWizardPanel(KineticLawSelectionEquationProgressPanelDescriptor.IDENTIFIER, descriptor2);
    
    // equation panel
    WizardPanelDescriptor descriptor3 = new KineticLawSelectionEquationPanelDescriptor(klg, sbmlIO);
    registerWizardPanel(KineticLawSelectionEquationPanelDescriptor.IDENTIFIER, descriptor3);
    
    // set option panel as first panel
    setCurrentPanel(KineticLawSelectionOptionPanelDescriptor.IDENTIFIER);
  }
  
  /**
   * Method that indicates whether or not changes have been introduced into
   * the given model.
   * 
   * @return {@code true} if kinetic equations and parameters or anything else were
   *         changed by SBMLsqueezer.
   */
  public boolean isKineticsAndParametersStoredInSBML() {
    boolean result = true;
    KineticLawSelectionEquationPanelDescriptor desc = (KineticLawSelectionEquationPanelDescriptor) getPanel(KineticLawSelectionEquationPanelDescriptor.IDENTIFIER);
    try {
      result = ((KineticLawSelectionEquationPanel) desc.getPanelComponent()).isKineticsAndParametersStoredInSBML();
      logger.fine("stored kinetics: " + result);
    } catch (Exception exc) {
      logger.fine(getMessage(exc));
      exc.printStackTrace();
    }
    return result;
  }
  
}
