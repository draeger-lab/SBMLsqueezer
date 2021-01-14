/*
 * $Id: KineticLawSelectionOptionPanelDescriptor.java 830 2012-02-26 00:33:31Z snagel $
 * $URL: https://rarepos.cs.uni-tuebingen.de/svn-path/SBMLsqueezer/trunk/src/org/sbml/squeezer/gui/wizard/KineticLawSelectionOptionPanelDescriptor.java $
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

import java.awt.Component;
import java.util.ResourceBundle;

import org.sbml.squeezer.KineticLawGenerator;
import org.sbml.squeezer.SBMLsqueezer;
import org.sbml.squeezer.util.Bundles;

import de.zbit.gui.JHelpBrowser;
import de.zbit.gui.wizard.WizardPanelDescriptor;
import de.zbit.util.ResourceManager;

/**
 * This class implements the descriptor for the option (main) panel. (Feb 25, 2012)
 * 
 * @author Sebastian Nagel
 * @since 2.0
 *
 */
public class KineticLawSelectionOptionPanelDescriptor extends WizardPanelDescriptor {
  
  /**
   * Localization support.
   */
  public static final transient ResourceBundle MESSAGES = ResourceManager.getBundle(Bundles.MESSAGES);
  /**
   * Localization support.
   */
  public static final transient ResourceBundle LABELS = ResourceManager.getBundle(Bundles.LABELS);
  
  /**
   * 
   */
  public static final String IDENTIFIER = "KINETIC_LAW_OPTION_PANEL";
  
  /**
   * 
   */
  public KineticLawSelectionOptionPanelDescriptor(KineticLawGenerator klg) {
    super(IDENTIFIER, new KineticLawSelectionOptionPanel(klg));
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
    return KineticLawSelectionEquationProgressPanelDescriptor.IDENTIFIER;
  }
  
  /* (non-Javadoc)
   * @see de.zbit.gui.wizard.WizardPanelDescriptor#getBackPanelDescriptor()
   */
  @Override
  public Object getBackPanelDescriptor() {
    return null;
  }
  
  /* (non-Javadoc)
   * @see de.zbit.gui.wizard.WizardPanelDescriptor#getHelpAction()
   */
  @Override
  public Component getHelpAction() {
    JHelpBrowser helpBrowser = new JHelpBrowser(getWizard().getDialog(),
      System.getProperty("app.name")
      + " "
      + String.format(LABELS.getString("ONLINE_HELP_FOR_THE_PROGRAM"),
        System.getProperty("app.version")),
        SBMLsqueezer.class.getResource("resources/html/help.html"));
    helpBrowser.setLocationRelativeTo(getWizard().getDialog());
    helpBrowser.setSize(640, 640);
    
    return helpBrowser;
  }
  
}
