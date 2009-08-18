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
public class Parameter extends NamedSBase {

	private double value;
	private boolean constant;

	public Parameter(Parameter p) {
		super(p);
		this.value = p.getValue();
		this.constant = p.isConstant();
	}

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
		if (o instanceof Parameter) {
			Parameter p = (Parameter) o;
			return p.getConstant() == constant && p.getValue() == value
					&& p.getSBOTerm() == getSBOTerm();
		}
		return false;
	}

	public boolean getConstant() {
		return isConstant();
	}

	public double getValue() {
		return value;
	}

	public void initDefaults() {
		value = Double.NaN;
		constant = true;
	}

	public boolean isConstant() {
		return constant;
	}

	public boolean isSetValue() {
		return !Double.isNaN(value);
	}

	public void setConstant(boolean constant) {
		this.constant = constant;
	}

	public void setValue(double value) {
		this.value = value;
	}

	public void unsetValue() {
		value = Double.NaN;
	}

}
