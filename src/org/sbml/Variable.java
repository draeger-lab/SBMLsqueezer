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
 *  along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.sbml;

/**
 * @author Andreas Dr&auml;ger <a
 *         href="mailto:andreas.draeger@uni-tuebingen.de">
 *         andreas.draeger@uni-tuebingen.de</a>
 * @date 2009-08-31
 */
public abstract class Variable extends AbstractNamedSBase {

	/**
	 * The unit attribute of this variable.
	 */
	private UnitDefinition units;
	/**
	 * The constant attribute of this variable.
	 */
	private boolean constant;
	/**
	 * The size, initial amount or concentration, or the actual value of this
	 * variable.
	 */
	private double value;

	/**
	 * @param id
	 */
	public Variable(String id) {
		super(id);
		this.units = null;
		this.value = Double.NaN;
	}

	/**
	 * @param id
	 * @param name
	 */
	public Variable(String id, String name) {
		super(id, name);
		this.units = null;
		this.value = Double.NaN;
	}

	/**
	 * @param nsb
	 */
	public Variable(Variable nsb) {
		super(nsb);
		this.units = nsb.isSetUnits() ? nsb.getUnits().clone() : null;
		this.value = nsb.getValue();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.AbstractNamedSBase#equals(java.lang.Object)
	 */
	// @Override
	public boolean equals(Object o) {
		boolean equal = super.equals(o);
		if (o instanceof Variable) {
			Variable v = (Variable) o;
			equal &= v.getConstant() == constant;
			equal &= v.isSetUnits() == isSetUnits();
			if (v.isSetUnits() && isSetUnits())
				equal &= v.getUnits().equals(units);
		} else
			equal = false;
		return equal;
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
	public UnitDefinition getUnits() {
		return units;
	}

	/**
	 * Returns the value of this variable. In Compartments the value is its
	 * size, in Species the value defines its initial amount or concentration,
	 * and in Parameters this returns the value attribute from SBML.
	 * 
	 * @return the value
	 */
	double getValue() {
		return value;
	}

	/**
	 * 
	 * @return
	 */
	boolean isSetValue() {
		return !Double.isNaN(value);
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
	public boolean isSetUnits() {
		return units != null;
	}

	/**
	 * 
	 * @param constant
	 */
	public void setConstant(boolean constant) {
		this.constant = constant;
		stateChanged();
	}

	/**
	 * Set the unit attribute of this variable to the given unit definition.
	 * 
	 * @param units
	 */
	public void setUnits(UnitDefinition units) {
		this.units = units;
		stateChanged();
	}

	/**
	 * Note that the meaning of the value can be different in all derived
	 * classes. In Compartments the value defines its size. In Species the value
	 * describes either the initial amount or the initial concentration. Only
	 * the class Parameter really defines a value attribute with this name.
	 * 
	 * @param value
	 *            the value to set
	 */
	void setValue(double value) {
		this.value = value;
		stateChanged();
	}
}
