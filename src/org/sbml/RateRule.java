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
 * 
 */
public class RateRule extends Rule {

	/**
	 * 
	 */
	private Symbol variable;

	/**
	 * @param sb
	 */
	public RateRule(RateRule sb) {
		super(sb);
		this.variable = sb.getVariableInstance();
	}

	/**
	 * 
	 */
	public RateRule(Symbol variable) {
		super();
		this.variable = variable;
	}

	/**
	 * @param math
	 */
	public RateRule(Symbol variable, ASTNode math) {
		super(math);
		this.variable = variable;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.MathContainer#clone()
	 */
	// @Override
	public RateRule clone() {
		return new RateRule(this);
	}

	/**
	 * 
	 * @return
	 */
	public String getVariable() {
		return variable.getId();
	}

	/**
	 * 
	 * @return
	 */
	public Symbol getVariableInstance() {
		return variable;
	}

	/**
	 * 
	 * @param variable
	 */
	public void setVariable(Symbol variable) {
		this.variable = variable;
		stateChanged();
	}

	/**
	 * 
	 * @param variable
	 */
	public void setVariable(String variable) {
		Symbol nsb = getModel().findSymbol(variable);
		if (nsb == null)
			throw new IllegalArgumentException(
					"Only the id of an existing Species, Compartments, or Parameters allowed as variables");
		setVariable(nsb);
	}

}
