/*
 * $Id$
 * $URL$
 * ---------------------------------------------------------------------
 * This file is part of SBMLsqueezer, a Java program that creates rate
 * equations for reactions in SBML files (http://sbml.org).
 *
 * Copyright (C) 2006-2014 by the University of Tuebingen, Germany.
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
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;
import org.sbml.jsbml.Reaction;

import org.sbml.squeezer.sabiork.wizard.console.SearchAResult;
import org.sbml.squeezer.sabiork.wizard.model.WizardProperties;
import de.zbit.sbml.gui.ReactionPanel;

/**
 * A class that renders a complete {@link TableModelSearchAResults} according to
 * the {@link SearchAResult} within it.
 * 
 * @author Matthias Rall
 * @version $Rev$
 */
public class TableCellRendererSearchAResults implements TableCellRenderer {
  
  /* (non-Javadoc)
   * @see javax.swing.table.TableCellRenderer#getTableCellRendererComponent(javax.swing.JTable, java.lang.Object, boolean, boolean, int, int)
   */
  @Override
  public Component getTableCellRendererComponent(JTable table, Object value,
    boolean isSelected, boolean hasFocus, int row, int column) {
    JPanel panel = new JPanel();
    Color colorForeground = table.getForeground();
    Color colorBackground = table.getBackground();
    Color colorSelectionForeground = table.getSelectionForeground();
    Color colorSelectionBackground = table.getSelectionBackground();
    TableModel tableModel = table.getModel();
    if (tableModel instanceof TableModelSearchAResults) {
      if (value instanceof Reaction) {
        panel = new ReactionPanel((Reaction) value, true);
        table.setRowHeight(row, panel.getPreferredSize().height);
      }
      if (value instanceof String) {
        panel.add(new JLabel((String) value));
      }
      if (value instanceof Integer) {
        panel.add(new JLabel(String.valueOf(value)));
      }
      int matchingEntries = (Integer) tableModel.getValueAt(row, 3);
      int partialOrNonMatchingEntries = (Integer) tableModel.getValueAt(
        row, 4);
      int totalEntries = (Integer) tableModel.getValueAt(row, 5);
      if (matchingEntries > 0 && totalEntries > 0) {
        colorBackground = WizardProperties
            .getColor("TABLE_CELL_RENDERER_SEARCH_A_RESULTS_RGB_COLOR_HAS_MATCHING_ENTRIES");
      }
      if (matchingEntries == 0 && partialOrNonMatchingEntries > 0
          && totalEntries > 0) {
        colorBackground = WizardProperties
            .getColor("TABLE_CELL_RENDERER_SEARCH_A_RESULTS_RGB_COLOR_HAS_ONLY_PARTIAL/NON-MATCHING_ENTRIES");
      }
      if (matchingEntries == 0 && partialOrNonMatchingEntries == 0
          && totalEntries == 0) {
        colorBackground = WizardProperties
            .getColor("TABLE_CELL_RENDERER_SEARCH_A_RESULTS_RGB_COLOR_HAS_NO_ENTRIES");
      }
    }
    panel.setForeground(colorForeground);
    panel.setBackground(colorBackground);
    if (isSelected) {
      panel.setForeground(colorSelectionForeground);
      panel.setBackground(colorSelectionBackground);
    }
    return panel;
  }
  
}
