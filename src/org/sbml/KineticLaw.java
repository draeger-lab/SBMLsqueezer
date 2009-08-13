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

import java.util.LinkedList;

import org.sbml.libsbml.ASTNode;
import org.sbml.libsbml.libsbml;

/**
 * @author Andreas Dr&auml;ger <a
 *         href="mailto:andreas.draeger@uni-tuebingen.de">
 *         andreas.draeger@uni-tuebingen.de</a>
 * 
 */
public class KineticLaw extends SBase {

	private LinkedList<Parameter> listOfParameters;
	private ASTNode math;
	private Reaction reaction;

	public KineticLaw() {
		listOfParameters = new LinkedList<Parameter>();
		reaction = null;
		math = null;
	}

	public KineticLaw(KineticLaw kineticLaw) {
		this();
		setMath(kineticLaw.getMath());
		setMetaid(kineticLaw.getMetaid());
		setSBOTerm(kineticLaw.getSBOTerm());
	}

	/**
	 * Adds a copy of the given Parameter object to the list of local parameters
	 * in this KineticLaw.
	 * 
	 * @param p
	 */
	public void addParameter(Parameter p) {
		listOfParameters.addLast(new Parameter(p));
		stateChanged();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#clone()
	 */
	public KineticLaw clone() {
		return new KineticLaw(this);
	}

	/**
	 * Returns the mathematical formula for this KineticLaw object and return it
	 * as as an AST.
	 * 
	 * @return
	 */
	public ASTNode getMath() {
		return isSetMath() ? math : new ASTNode();
	}

	/**
	 * Returns the number of local parameters in this KineticLaw instance.
	 * 
	 * @return
	 */
	public int getNumParameters() {
		return listOfParameters.size();
	}

	/**
	 * Returns the ith Parameter object in the list of local parameters in this
	 * KineticLaw instance.
	 * 
	 * @param i
	 * @return
	 */
	public Parameter getParameter(int i) {
		return listOfParameters.get(i);
	}

	/**
	 * Returns a local parameter based on its identifier.
	 * 
	 * @param id
	 * @return
	 */
	public Parameter getParameter(String id) {
		for (Parameter p : listOfParameters)
			if (p.getId().equals(id))
				return p;
		return null;
	}

	/**
	 * Returns the reaction belonging to this kinetic law.
	 * 
	 * @return
	 */
	public Reaction getReaction() {
		return reaction;
	}

	/**
	 * Removes the ith local parameter from this object.
	 * 
	 * @param i
	 */
	public void removeParameter(int i) {
		listOfParameters.remove(i);
		stateChanged();
	}

	/**
	 * Removes the ith local parameter from this object based on its id.
	 * 
	 * @param i
	 */
	public void removeParameter(String id) {
		int i = 0;
		while (i < listOfParameters.size()
				&& !listOfParameters.get(i).getId().equals(id))
			i++;
		if (i < listOfParameters.size())
			listOfParameters.remove(i);
		stateChanged();
	}

	/**
	 * Sets the mathematical expression of this KineticLaw instance to the given
	 * formula.
	 * 
	 * @param formula
	 */
	public void setFormula(String formula) {
		math = libsbml.parseFormula(formula);
		stateChanged();
	}

	/**
	 * Sets the mathematical expression of this KineticLaw instance to a copy of
	 * the given ASTNode.
	 * 
	 * @param math
	 */
	public void setMath(ASTNode math) {
		this.math = math.deepCopy();
		stateChanged();
	}

	public void setReaction(Reaction reaction) {
		this.reaction = reaction;
		stateChanged();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.SBase#toString()
	 */
	@Override
	public String toString() {
		if (isSetMath())
			return libsbml.formulaToString(math);
		return "";
	}

	public boolean isSetMath() {
		return math != null ? true : false;
	}
}
