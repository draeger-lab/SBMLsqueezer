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

import org.sbml.squeezer.io.SBaseChangedListener;

/**
 * @author Andreas Dr&auml;ger <a
 *         href="mailto:andreas.draeger@uni-tuebingen.de">
 *         andreas.draeger@uni-tuebingen.de</a>
 * 
 */
public class KineticLaw extends MathContainer {

	/**
	 * local parameters
	 */
	private ListOf<Parameter> listOfParameters;

	/**
	 * 
	 */
	public KineticLaw() {
		super();
		listOfParameters = new ListOf<Parameter>();
		listOfParameters.parentSBMLObject = this;
	}

	/**
	 * 
	 * @param kineticLaw
	 */
	public KineticLaw(KineticLaw kineticLaw) {
		super(kineticLaw);
		listOfParameters = kineticLaw.getListOfParameters().clone();
		listOfParameters.parentSBMLObject = this;
	}

	/**
	 * 
	 * @param parentReaction
	 */
	public KineticLaw(Reaction parentReaction) {
		this();
		parentReaction.setKineticLaw(this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.sbml.SBase#addChangeListener(org.sbml.squeezer.io.SBaseChangedListener
	 * )
	 */
	// @Override
	public void addChangeListener(SBaseChangedListener l) {
		super.addChangeListener(l);
		listOfParameters.addChangeListener(l);
	}

	/**
	 * Adds a copy of the given Parameter object to the list of local parameters
	 * in this KineticLaw.
	 * 
	 * @param p
	 */
	public void addParameter(Parameter parameter) {
		if (!getListOfParameters().contains(parameter)) {
			listOfParameters.add(parameter);
			parameter.parentSBMLObject = this;
			stateChanged();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#clone()
	 */
	public KineticLaw clone() {
		return new KineticLaw(this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.MathContainer#equals(java.lang.Object)
	 */
	// @Override
	public boolean equals(Object o) {
		boolean equal = super.equals(o);
		if (o instanceof KineticLaw) {
			KineticLaw kl = (KineticLaw) o;
			equal &= kl.getListOfParameters().equals(getListOfParameters());
			return equal;
		} else
			equal = false;
		return equal;
	}

	/**
	 * 
	 * @return
	 */
	public ListOf<Parameter> getListOfParameters() {
		return listOfParameters;
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
	 * This method is convenient when holding an object nested inside other
	 * objects in an SBML model. It allows direct access to the &lt;model&gt;
	 * 
	 * element containing it.
	 * 
	 * @return Returns the parent SBML object.
	 */
	public Reaction getParentSBMLObject() {
		return (Reaction) parentSBMLObject;
	}

	/**
	 * Removes the ith local parameter from this object.
	 * 
	 * @param i
	 */
	public void removeParameter(int i) {
		listOfParameters.remove(i).sbaseRemoved();
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
			listOfParameters.remove(i).sbaseRemoved();
	}

	/**
	 * 
	 * @param p
	 */
	public void removeParameter(Parameter p) {
		listOfParameters.remove(p);
	}
}
