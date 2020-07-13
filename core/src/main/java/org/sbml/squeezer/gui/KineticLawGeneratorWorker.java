/* $Id: KineticLawGeneratorWorker.java 831 2012-02-26 12:33:51Z snagel $
 * $URL: https://rarepos.cs.uni-tuebingen.de/svn-path/SysBio/trunk/src/de/zbit/sbml/gui/KineticLawGeneratorWorker.java $
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

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.SwingWorker;

import org.sbml.squeezer.KineticLawGenerator;
import org.sbml.squeezer.util.Bundles;

import de.zbit.util.ResourceManager;

/**
 * Generates kinetic laws using SwingWorker
 * 
 * @author Sebastian Nagel
 * @since 2.0
 * 
 */
public class KineticLawGeneratorWorker extends SwingWorker<Void, Void> {
  /**
   * A {@link Logger} for this class.
   */
  private static final transient Logger logger = Logger.getLogger(KineticLawGeneratorWorker.class.getName());
  
  /**
   * 
   */
  private KineticLawGenerator klg;
  
  /**
   * 
   * @param klg
   */
  public KineticLawGeneratorWorker(KineticLawGenerator klg) {
    super();
    this.klg = klg;
  }
  
  /* (non-Javadoc)
   * @see javax.swing.SwingWorker#doInBackground()
   */
  @Override
  protected Void doInBackground() {
    try {
      klg.generateLaws();
    } catch (Throwable e) {
      logger.log(Level.WARNING, e.getLocalizedMessage());
      e.printStackTrace();
    }
    return null;
  }
  
  /* (non-Javadoc)
   * @see javax.swing.SwingWorker#done()
   */
  @Override
  protected void done() {
    logger.log(Level.INFO, ResourceManager.getBundle(Bundles.LABELS).getString("READY"));
    firePropertyChange("generateKineticLawDone", null, null);
  }
  
}
