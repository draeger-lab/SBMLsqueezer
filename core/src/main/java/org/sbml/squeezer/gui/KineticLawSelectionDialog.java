/*
 * $Id$
 * $URL$
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
package org.sbml.squeezer.gui;

import java.awt.Dimension;
import java.awt.Frame;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JDialog;

import org.sbml.squeezer.OptionsGeneral;
import org.sbml.squeezer.io.SBMLio;
import org.sbml.squeezer.util.Bundles;

import de.zbit.gui.StatusBar;
import de.zbit.util.ResourceManager;
import de.zbit.util.prefs.SBPreferences;
import de.zbit.util.progressbar.AbstractProgressBar;

/**
 * This is the main GUI class. (Aug 3, 2007)
 * 
 * @author Nadine Hassis
 * @author Andreas Dr&auml;ger
 * @author Hannes Borch
 * @author Sebastian Nagel
 * @since 1.0
 *
 */
public class KineticLawSelectionDialog extends JDialog {
  
  /**
   * Localization support.
   */
  public static final transient ResourceBundle MESSAGES = ResourceManager.getBundle(Bundles.MESSAGES);
  /**
   * Localization support.
   */
  public static final transient ResourceBundle LABELS = ResourceManager.getBundle(Bundles.LABELS);
  
  /**
   * Generated serial version id.
   */
  private static final long serialVersionUID = -5980678130366530716L;
  
  // UI ELEMENTS DEFINITION: ReactionFrame
  private boolean kineticsAndParametersStoredInSBML = false;
  
  /**
   * 
   */
  private final Logger logger = Logger.getLogger(KineticLawSelectionDialog.class.getName());
  
  SBPreferences prefs;
  
  private StatusBar statusBar;
  
  
  /**
   * Creates an empty dialog with the given settings and sbml io object.
   * 
   * @param owner
   */
  private KineticLawSelectionDialog(Frame owner) {
    super(owner, System.getProperty("app.name"), true);
    // if (owner == null)
    // setIconImage(GUITools.ICON_LEMON);
    prefs = new SBPreferences(OptionsGeneral.class);
    // setAlwaysOnTop(true);
  }
  
  /**
   * This constructor is necessary for the GUI to generate just one single
   * rate equation for the given reaction.
   * 
   * @param owner
   * @param sbmlIO
   * @param reactionID
   * @throws Throwable
   */
  public KineticLawSelectionDialog(Frame owner, SBMLio<?> sbmlIO, String reactionID) throws Throwable {
    this(owner);
    
    // This thing is necessary for CellDesigner!
    KineticLawWindowAdapter adapter = new KineticLawWindowAdapter(this,
      sbmlIO, reactionID);
    
    pack();
    setMinimumSize(new Dimension(500, 450));
    setResizable(true);
    setLocationRelativeTo(owner);
    setVisible(true);
    
    if (statusBar != null) {
      AbstractProgressBar progressBar = statusBar.showProgress();
      adapter.showProgress(progressBar);
    }
    kineticsAndParametersStoredInSBML = adapter.isKineticsAndParametersStoredInSBML();
    dispose();
    if (statusBar != null) {
      statusBar.hideProgress();
    }
    logger.log(Level.INFO, LABELS.getString("READY"));
  }
  
  /**
   * Method that indicates whether or not changes have been introduced into
   * the given model.
   * 
   * @return {@code true} if kinetic equations and parameters or anything else were
   *         changed by SBMLsqueezer.
   */
  public boolean isKineticsAndParametersStoredInSBML() {
    return kineticsAndParametersStoredInSBML;
  }
  
}
