/*
 * $Id:  ModelChangeListener.java 13:43:19 draeger$
 * $URL: ModelChangeListener.java $
 * ---------------------------------------------------------------------
 * This file is part of SBMLsqueezer, a Java program that creates rate 
 * equations for reactions in SBML files (http://sbml.org).
 *
 * Copyright (C) 2006-2011 by the University of Tuebingen, Germany.
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

package org.sbml.squeezer.util;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.sbml.jsbml.SBase;
import org.sbml.jsbml.util.SBaseChangeEvent;
import org.sbml.jsbml.util.SBaseChangeListener;

/**
 * @author Andreas Dr&auml;ger
 * @version $Rev$
 * @since 1.4
 */
public class ModelChangeListener implements SBaseChangeListener {
  
  private Logger logger = Logger.getLogger(ModelChangeListener.class.getName());
  
  /* (non-Javadoc)
   * @see org.sbml.jsbml.util.SBaseChangeListener#sbaseAdded(org.sbml.jsbml.SBase)
   */
  public void sbaseAdded(SBase sb) {
    logger.log(Level.INFO, "[ADD] " + sb.toString());
  }
  
  /* (non-Javadoc)
   * @see org.sbml.jsbml.util.SBaseChangeListener#sbaseRemoved(org.sbml.jsbml.SBase)
   */
  public void sbaseRemoved(SBase sb) {
    logger.log(Level.INFO, "[DEL] " + sb.toString());
  }
  
  /* (non-Javadoc)
   * @see org.sbml.jsbml.util.ChangeListener#stateChanged(org.sbml.jsbml.util.ChangeEvent)
   */
  public void stateChanged(SBaseChangeEvent event) {
    logger.log(Level.INFO, "[CHG] " + event.toString());    
  }
  
}
