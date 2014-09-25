/*
 * $Id: JComboBoxInterval.java 1082 2014-02-22 23:54:18Z draeger $
 * $URL: https://rarepos.cs.uni-tuebingen.de/svn-path/SBMLsqueezer/trunk/src/org/sbml/squeezer/sabiork/wizard/gui/JComboBoxInterval.java $
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

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;

/**
 * A class that allows the creation of a combo box containing {@link Double}
 * values from {@code minimum} to {@code maximum} (step size of
 * {@code 0.1}).
 * 
 * @author Matthias Rall
 * @version $Rev: 1082 $
 * @since 2.0
 */
public class JComboBoxInterval extends JComboBox {
  
  /**
   * Generate serial version identifier.
   */
  private static final long serialVersionUID = 780253734453909235L;
  private double minimum;
  private double maximum;
  
  public JComboBoxInterval(double minimum, double maximum) {
    this.minimum = round(minimum);
    this.maximum = round(maximum);
    setInterval(minimum, maximum);
  }
  
  /**
   * Returns the rounded value.
   * 
   * @param value
   * @return
   */
  private double round(double value) {
    value = value * 10;
    value = Math.round(value);
    value = value / 10;
    return value;
  }
  
  /**
   * Adds all values from {@code lowerValue} to {@code upperValue}
   * to the model (step size of {@code 0.1}).
   * 
   * @param lowerValue
   * @param upperValue
   */
  public void setInterval(double lowerValue, double upperValue) {
    DefaultComboBoxModel model = new DefaultComboBoxModel();
    lowerValue = round(lowerValue);
    upperValue = round(upperValue);
    for (double i = lowerValue; i <= upperValue; i = i + 0.1) {
      model.addElement(round(i));
    }
    setModel(model);
  }
  
  /**
   * Returns the minimum value.
   * 
   * @return
   */
  public double getMinimum() {
    return minimum;
  }
  
  /**
   * Returns the maximum value.
   * 
   * @return
   */
  public double getMaximum() {
    return maximum;
  }
  
  /**
   * Returns the selected value.
   * 
   * @return
   */
  public double getSelectedValue() {
    return ((Double) getSelectedItem());
  }
  
}
