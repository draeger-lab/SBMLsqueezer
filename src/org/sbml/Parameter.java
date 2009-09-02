/*
 *  SBMLsqueezer creates rate equations for reactions in SBML files
 *  (http://sbml.org).
 *  Copyright (C) 2009 ZBIT, University of Tübingen, Andreas Dräger
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.sbml;

/**
 * 
 * @author Andreas Dr&auml;ger <a
 *         href="mailto:andreas.draeger@uni-tuebingen.de">
 *         andreas.draeger@uni-tuebingen.de</a>
 * 
 */
public class Parameter extends Symbol {

	private double value;
	private boolean constant;

	/**
	 * 
	 * @param p
	 */
	public Parameter(Parameter p) {
		super(p);
		this.value = p.getValue();
		this.constant = p.isConstant();
	}

	/**
	 * 
	 * @param id
	 */
	public Parameter(String id) {
		super(id);
		initDefaults();
	}


	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.SBase#clone()
	 */
	// @Override
	public Parameter clone() {
		return new Parameter(this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.SBase#equals(java.lang.Object)
	 */
	// @Override
	public boolean equals(Object o) {
		boolean equals = super.equals(o);
		if (o instanceof Parameter) {
			Parameter p = (Parameter) o;
			equals &= p.getConstant() == constant;
			equals &= p.getValue() == value;
			return equals;
		} else
			equals = false;
		return equals;
	}

	/**
	 * 
	 * @return
	 */
	public boolean getConstant() {
		return isConstant();
	}

	/**
	 * 
	 * @return
	 */
	public double getValue() {
		return value;
	}

	/**
	 * 
	 */
	public void initDefaults() {
		value = Double.NaN;
		constant = true;
	}

	/**
	 * 
	 * @return
	 */
	public boolean isConstant() {
		return constant;
	}

	/**
	 * 
	 * @return
	 */
	public boolean isSetValue() {
		return !Double.isNaN(value);
	}

	/**
	 * 
	 * @param constant
	 */
	public void setConstant(boolean constant) {
		this.constant = constant;
	}

	/**
	 * 
	 * @param value
	 */
	public void setValue(double value) {
		this.value = value;
	}

	/**
	 * 
	 */
	public void unsetValue() {
		value = Double.NaN;
	}

}
