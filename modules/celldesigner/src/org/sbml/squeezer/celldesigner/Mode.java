/*
 * $Id$
 * $URL$
 * ---------------------------------------------------------------------
 * This file is part of SBMLsqueezer, a Java program that creates rate
 * equations for reactions in SBML files (http://sbml.org).
 *
 * Copyright (C) 2006-2015 by the University of Tuebingen, Germany.
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
package org.sbml.squeezer.celldesigner;

import java.util.ResourceBundle;

import de.zbit.util.ResourceManager;

/**
 * @author Andreas Dr&auml;ger
 * @date 2009-10-22
 * @version $Rev$
 * @since 2.0
 */
public enum Mode {
  /**
   * Returns the label of the menu item that points to this plug-in.
   */
  SQUEEZE_ALL,
  /**
   * This is the method that returns the label for the item in
   * CellDesigner's menu that allows to "squeeze" a kinetic equation from
   * a given reaction.
   */
  SQUEEZE_REACTION,
  /**
   * Command to change the configuration of SBMLsqueezer (start the option
   * menu).
   */
  CONFIGURE,
  /**
   * Export a single reaction int another format.
   */
  EXPORT_REACTION,
  /**
   * This method returns the text for the menu item that allows the user
   * to export the model into another format, such as LaTeX.
   */
  EXPORT_ALL,
  /**
   * Show the online help.
   */
  ONLINE_HELP,
  /**
   * Just for debugging purposes.
   */
  SHOW_JSBML_MODEL;
  
  /**
   * Localization support.
   */
  private static final transient ResourceBundle bundle = ResourceManager.getBundle(Mode.class.getPackage().getName() + ".Messages");
  
  /**
   * A human-readable label for each mode.
   * 
   * @return
   */
  public String getText() {
    String key = toString();
    if (bundle.containsKey(key)) {
      return bundle.getString(key);
    }
    return bundle.getString("DEFAULT_OPTION");
  }
  
  /**
   * A short description of the action.
   * 
   * @return
   */
  public String getToolTipText() {
    String key = toString() + "_TOOLTIP";
    if (bundle.containsKey(key)) {
      return bundle.getString(key);
    }
    return null;
  }
  
  /**
   * Returns the mode for the given label or {@code null}.
   * 
   * @param text
   * @return
   */
  public static Mode getMode(String text) {
    for (Mode m : values()) {
      if (text.equals(m.getText())) {
        return m;
      }
    }
    return null;
  }
  
}
