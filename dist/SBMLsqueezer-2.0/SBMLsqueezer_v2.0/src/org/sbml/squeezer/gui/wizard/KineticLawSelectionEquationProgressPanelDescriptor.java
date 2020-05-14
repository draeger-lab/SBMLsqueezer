/*
 * $Id: KineticLawSelectionEquationProgressPanelDescriptor.java 1082 2014-02-22 23:54:18Z draeger $
 * $URL: https://rarepos.cs.uni-tuebingen.de/svn-path/SBMLsqueezer/trunk/src/org/sbml/squeezer/gui/wizard/KineticLawSelectionEquationProgressPanelDescriptor.java $
 * ---------------------------------------------------------------------
 * This file is part of SBMLsqueezer, a Java program that creates rate
 * equations for reactions in SBML files (http://sbml.org).
 *
 * Copyright (C) 2006-2018 by the University of Tuebingen, Germany.
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

import java.awt.BorderLayout;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JPanel;
import javax.swing.JProgressBar;

import org.sbml.squeezer.KineticLawGenerator;
import org.sbml.squeezer.gui.KineticLawGeneratorWorker;

import de.zbit.gui.wizard.Wizard;
import de.zbit.gui.wizard.WizardPanelDescriptor;
import de.zbit.util.progressbar.gui.ProgressBarSwing;

/**
 * This class implements the descriptor for the progress panel
 * 
 * @author Sebastian Nagel
 *
 * @since 2.0
 */
public class KineticLawSelectionEquationProgressPanelDescriptor extends WizardPanelDescriptor implements PropertyChangeListener {
  
  /**
   * 
   */
  public static final String IDENTIFIER = "KINETIC_LAW_EQUATION_PROGRESS_PANEL";
  
  /**
   * 
   */
  private KineticLawGenerator klg;
  
  /**
   * 
   * @param klg
   */
  public KineticLawSelectionEquationProgressPanelDescriptor(KineticLawGenerator klg) {
    super(IDENTIFIER, new JPanel(new BorderLayout()));
    this.klg = klg;
  }
  
  /* (non-Javadoc)
   * @see de.zbit.gui.wizard.WizardPanelDescriptor#displayingPanel()
   */
  @Override
  public void displayingPanel() {
    // disable the buttons while progress is running
    Wizard wizard = getWizard();
    wizard.setNextFinishButtonEnabled(false);
    wizard.setBackButtonEnabled(false);
    // set progress bar
    JProgressBar progressBar = new JProgressBar();
    JPanel p = (JPanel) getPanelComponent();
    p.add(progressBar, BorderLayout.CENTER);
    klg.setProgressBar(new ProgressBarSwing(progressBar));
    // generate kinetic laws
    KineticLawGeneratorWorker worker = new KineticLawGeneratorWorker(klg);
    worker.addPropertyChangeListener(this);
    worker.execute();
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
  
  /* (non-Javadoc)
   * @see de.zbit.gui.wizard.WizardPanelDescriptor#getBackPanelDescriptor()
   */
  @Override
  public Object getBackPanelDescriptor() {
    return KineticLawSelectionOptionPanelDescriptor.IDENTIFIER;
  }
  
  /* (non-Javadoc)
   * @see java.beans.PropertyChangeListener#propertyChange(java.beans.PropertyChangeEvent)
   */
  @Override
  public void propertyChange(PropertyChangeEvent evt) {
    if (evt.getPropertyName().equals("generateKineticLawDone")) {
      // when progress is done, go to next panel automatically
      getWizard().goToNextPanel();
    }
  }
  
}
