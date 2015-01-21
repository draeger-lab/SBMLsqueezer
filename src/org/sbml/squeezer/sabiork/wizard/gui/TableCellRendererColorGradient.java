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
package org.sbml.squeezer.sabiork.wizard.gui;

import java.awt.Color;
import javax.swing.table.TableCellRenderer;

/**
 * A class that renders {@link Double} values as a color within a gradient.
 * 
 * @author Matthias Rall
 * @version $Rev$
 * @since 2.0
 */
public abstract class TableCellRendererColorGradient implements
TableCellRenderer {
  
  /**
   * Returns the rounded value.
   * 
   * @param value
   * @return
   */
  protected double round(double value) {
    value = value * 10;
    value = Math.round(value);
    value = value / 10;
    return value;
  }
  
  /**
   * Returns the {@link Color} of a {@link Double} value within a color
   * gradient between the color of the maximum and the minimum.
   * 
   * @param value
   * @param minimum
   * @param maximum
   * @param colorMinimum
   * @param colorMaximum
   * @return
   */
  protected Color getGradientColor(double value, double minimum,
    double maximum, Color colorMinimum, Color colorMaximum) {
    if (value < minimum) {
      return colorMinimum;
    } else if (maximum < value) {
      return colorMaximum;
    } else {
      double difference = maximum - minimum;
      double ratio = (value - minimum) / difference;
      int r = (int) Math.round(ratio * colorMaximum.getRed()
        + (1 - ratio) * colorMinimum.getRed());
      int g = (int) Math.round(ratio * colorMaximum.getGreen()
        + (1 - ratio) * colorMinimum.getGreen());
      int b = (int) Math.round(ratio * colorMaximum.getBlue()
        + (1 - ratio) * colorMinimum.getBlue());
      int a = (int) Math.round(ratio * colorMaximum.getAlpha()
        + (1 - ratio) * colorMinimum.getAlpha());
      return new Color(r, g, b, a);
    }
  }
  
}
