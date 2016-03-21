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
package org.sbml.squeezer.sabiork.wizard.gui;

import java.awt.Color;
import java.awt.Component;
import javax.swing.JCheckBox;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

/**
 * A class that renders {@link Boolean} values as disabled check boxes.
 * 
 * @author Matthias Rall
 * @version $Rev$
 * @since 2.0
 */
public class TableCellRendererBooleans implements TableCellRenderer {
  
  /* (non-Javadoc)
   * @see javax.swing.table.TableCellRenderer#getTableCellRendererComponent(javax.swing.JTable, java.lang.Object, boolean, boolean, int, int)
   */
  @Override
  public Component getTableCellRendererComponent(JTable table, Object value,
    boolean isSelected, boolean hasFocus, int row, int column) {
    JCheckBox checkBox = new JCheckBox();
    Color colorForeground = table.getForeground();
    Color colorBackground = table.getBackground();
    Color colorSelectionForeground = table.getSelectionForeground();
    Color colorSelectionBackground = table.getSelectionBackground();
    if (value instanceof Boolean) {
      checkBox.setSelected((Boolean) value);
    }
    checkBox.setEnabled(false);
    checkBox.setForeground(colorForeground);
    checkBox.setBackground(colorBackground);
    if (isSelected) {
      checkBox.setForeground(colorSelectionForeground);
      checkBox.setBackground(colorSelectionBackground);
    }
    return checkBox;
  }
  
}
