/*
 * $Id$
 * $URL$
 * ---------------------------------------------------------------------
 * This file is part of SBMLsqueezer, a Java program that creates rate 
 * equations for reactions in SBML files (http://sbml.org).
 *
 * Copyright (C) 2006-2012 by the University of Tuebingen, Germany.
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
 * values from <code>minimum</code> to <code>maximum</code> (step size of
 * <code>0.1</code>).
 * 
 * @author Matthias Rall
 * @version $Rev$
 * ${tags}
 */
@SuppressWarnings("serial")
public class JComboBoxInterval extends JComboBox {

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
	 * Adds all values from <code>lowerValue</code> to <code>upperValue</code>
	 * to the model (step size of <code>0.1</code>).
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
