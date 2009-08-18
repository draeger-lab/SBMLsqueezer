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

import org.sbml.libsbml.libsbml;

/**
 * @author Andreas Dr&auml;ger <a
 *         href="mailto:andreas.draeger@uni-tuebingen.de">
 *         andreas.draeger@uni-tuebingen.de</a>
 * 
 */
public class EventAssignment extends MathContainer {

	private NamedSBase variable;

	public EventAssignment() {
		super();
		variable = null;
	}

	public EventAssignment(EventAssignment eventAssignment) {
		super(eventAssignment);
		setVariable(eventAssignment.getVariable());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.MathElement#clone()
	 */
	// @Override
	public EventAssignment clone() {
		return new EventAssignment(this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.MathElement#equals(java.lang.Object)
	 */
	// @Override
	public boolean equals(Object o) {
		if (o instanceof EventAssignment) {
			EventAssignment ea = (EventAssignment) o;
			return getMath().equals(ea.getMath())
					&& ea.getVariable().equals(getVariable());
		}
		return false;
	}

	public String getVariable() {
		return variable.getId();
	}

	public NamedSBase getVariableInstance() {
		return variable;
	}

	public void setVariable(NamedSBase variable) {
		if ((variable instanceof Species) || variable instanceof Compartment
				|| (variable instanceof Parameter))
			this.variable = variable;
		else
			throw new IllegalArgumentException(
					"Only Species, Compartments, or Parameters allowed as variables");
	}

	public void setVariable(String variable) {
		Model m = getModel();
		NamedSBase nsb = null;
		if (m != null) {
			nsb = m.getSpecies(variable);
			if (nsb == null)
				nsb = m.getParameter(variable);
			if (nsb == null)
				nsb = m.getCompartment(variable);
			if (nsb != null)
				setVariable(nsb);
		}
		if (nsb == null)
			throw new IllegalArgumentException(
					"Only the id of an existing Species, Compartments, or Parameters allowed as variables");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.SBase#toString()
	 */
	// @Override
	public String toString() {
		return getVariable() + " = " + libsbml.formulaToString(getMath());
	}

}
