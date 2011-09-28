/*
 * $Id:  SBMLtools.java 17:32:00 draeger$
 * $URL$
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

import javax.swing.tree.TreeNode;

import org.sbml.jsbml.ListOf;
import org.sbml.jsbml.NamedSBase;
import org.sbml.jsbml.SBO;
import org.sbml.jsbml.SBase;
import org.sbml.jsbml.util.filters.NameFilter;

/**
 * @author Andreas Dr&auml;ger
 * @author Sarah M. M&uuml;ller vom Hagen
 * @version $Rev$
 * @since 1.4
 */
public class SBMLtools {
  
  /**
   * Logger
   */
  private static final Logger logger = Logger.getLogger(SBMLtools.class.getName());
  
  /**
   * 
   * @param sbase
   * @param term
   */
  public static final void setSBOTerm(SBase sbase, int term) {
    if (-1 < sbase.getLevelAndVersion().compareTo(Integer.valueOf(2),
        Integer.valueOf(2))) {
      sbase.setSBOTerm(term);
    } else {
      logger.log(Level.FINE, String.format(
          "Could not set SBO term %s for %s with Level = %d and Version = %d.", 
          SBO.sboNumberString(term), sbase.getElementName(), sbase.getLevel(), sbase.getVersion()));
    }
  }
  
  /**
   * 
   * @param <T>
   * @param listOf
   * @param element
   */
  public static final <T extends NamedSBase> void addOrReplace(ListOf<T> listOf, T element) {
    T prev = listOf.firstHit(new NameFilter(element.getId()));
    if (prev != null) {
      listOf.remove(prev);
    }
    listOf.add(element);
  }
  
  /**
   * 
   * @param sbase
   * @param level
   * @param version
   */
  public static final void setLevelAndVersion(SBase sbase, int level, int version){
    sbase.setVersion(version);
    sbase.setLevel(level);
    for (int i=0; i<sbase.getChildCount(); i++) {
      TreeNode child = sbase.getChildAt(i);
      if (child instanceof SBase) {
        setLevelAndVersion((SBase) child, level, version);
      }
    }
  }
  
}
