/*
 * $Id: TableCellRendererpHValues.java 1082 2014-02-22 23:54:18Z draeger $
 * $URL: https://rarepos.cs.uni-tuebingen.de/svn-path/SBMLsqueezer/trunk/src/org/sbml/squeezer/sabiork/wizard/gui/TableCellRendererpHValues.java $
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
package org.sbml.squeezer.sabiork.wizard.gui;

import java.awt.Color;
import java.awt.Component;
import java.io.UnsupportedEncodingException;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.xml.stream.XMLStreamException;
import org.sbml.jsbml.KineticLaw;
import org.sbml.squeezer.sabiork.SABIORK;
import org.sbml.squeezer.sabiork.wizard.model.WizardProperties;

/**
 * A class that renders pH values as a color within a gradient.
 * 
 * @author Matthias Rall
 * @version $Rev: 1082 $
 * @since 2.0
 */
public class TableCellRendererpHValues extends TableCellRendererColorGradient {
  
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
    if (value instanceof KineticLaw) {
      String pHValue = "";
      try {
        pHValue = SABIORK.getStartValuepH((KineticLaw) value).trim();
      } catch (UnsupportedEncodingException e) {
        e.printStackTrace();
      } catch (XMLStreamException e) {
        e.printStackTrace();
      }
      if (!pHValue.isEmpty()) {
        panel.add(new JLabel(String.valueOf(pHValue)));
        Double pHValueDouble = Double.valueOf(pHValue);
        pHValueDouble = round(pHValueDouble);
        Color colorpH0 = WizardProperties
            .getColor("TABLE_CELL_RENDERER_PH_VALUES_RGB_COLOR_PH0");
        Color colorpH5 = WizardProperties
            .getColor("TABLE_CELL_RENDERER_PH_VALUES_RGB_COLOR_PH5");
        Color colorpH7 = WizardProperties
            .getColor("TABLE_CELL_RENDERER_PH_VALUES_RGB_COLOR_PH7");
        Color colorpH9 = WizardProperties
            .getColor("TABLE_CELL_RENDERER_PH_VALUES_RGB_COLOR_PH9");
        Color colorpH14 = WizardProperties
            .getColor("TABLE_CELL_RENDERER_PH_VALUES_RGB_COLOR_PH14");
        if (0.0 <= pHValueDouble && pHValueDouble <= 5.0) {
          colorBackground = getGradientColor(pHValueDouble, 0.0, 5.0,
            colorpH0, colorpH5);
        } else if (5.1 <= pHValueDouble && pHValueDouble <= 7.0) {
          colorBackground = getGradientColor(pHValueDouble, 5.1, 7.0,
            colorpH5, colorpH7);
        } else if (7.1 <= pHValueDouble && pHValueDouble <= 9.0) {
          colorBackground = getGradientColor(pHValueDouble, 7.1, 9.0,
            colorpH7, colorpH9);
        } else if (9.1 <= pHValueDouble && pHValueDouble <= 14.0) {
          colorBackground = getGradientColor(pHValueDouble, 9.1,
            14.0, colorpH9, colorpH14);
        } else {
          colorBackground = getGradientColor(pHValueDouble, 0.0,
            14.0, colorpH0, colorpH14);
        }
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
