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
 * 
 * @author Andreas Dr&auml;ger <a
 *         href="mailto:andreas.draeger@uni-tuebingen.de">
 *         andreas.draeger@uni-tuebingen.de</a>
 * 
 */
public class Model extends NamedSBase {

	private ListOf<Species> listOfSpecies;
	private ListOf<Reaction> listOfReactions;
	private ListOf<Parameter> listOfParameters;

	public Model(Model model) {
		super(model);
		listOfSpecies = model.getListOfSpecies().clone();
		listOfReactions = model.getListOfReactions().clone();
		listOfParameters = model.getListOfParameters().clone();
	}

	public Model(String id) {
		super(id);
		listOfSpecies = new ListOf<Species>();
		listOfReactions = new ListOf<Reaction>();
		listOfParameters = new ListOf<Parameter>();
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.sbml.SBase#addChangeListener(org.sbml.squeezer.io.SBaseChangedListener)
	 */
	public void addChangeListener(SBaseChangedListener l) {
		super.addChangeListener(l);
		listOfParameters.addChangeListener(l);
		listOfReactions.addChangeListener(l);
		listOfSpecies.addChangeListener(l);
	}

	public void addParameter(Parameter parameter) {
		Parameter p = new Parameter(parameter);
		listOfParameters.add(p);
		p.sbaseAdded();
	}

	/**
	 * adds a reaction to the model
	 * 
	 * @param reac
	 */
	public void addReaction(Reaction reac) {
		listOfReactions.add(reac);
		stateChanged();
	}

	/**
	 * adds a species to the model
	 * 
	 * @param spec
	 */
	public void addSpecies(Species spec) {
		listOfSpecies.add(spec);
		stateChanged();
	}

	// @Override
	public Model clone() {
		return new Model(this);
	}

	// @Override
	public boolean equals(Object o) {
		// TODO Auto-generated method stub
		return false;
	}

	public ListOf<Parameter> getListOfParameters() {
		return listOfParameters;
	}

	public ListOf<Reaction> getListOfReactions() {
		return listOfReactions;
	}

	public ListOf<Species> getListOfSpecies() {
		return listOfSpecies;
	}

	public int getNumParameters() {
		return listOfParameters.size();
	}

	public int getNumReactions() {
		return listOfReactions.size();
	}

	public int getNumSpecies() {
		return listOfSpecies.size();
	}

	public void removeParameter(Parameter parameter) {
		listOfParameters.remove(parameter);
		parameter.sbaseRemoved();
	}

	/**
	 * removes a reaction from the model
	 * 
	 * @param reac
	 */
	public void removeReaction(Reaction reac) {
		listOfReactions.remove(reac);
		reac.sbaseRemoved();
	}

	/**
	 * removes a species from the model
	 * 
	 * @param spec
	 */
	public void removeSpecies(Species spec) {
		listOfSpecies.remove(spec);
		stateChanged();
	}
	
	/**
	 * This method is convenient when holding an object nested inside other
	 * objects in an SBML model. It allows direct access to the &lt;model&gt;
	 * 
	 * element containing it.
	 * 
	 * @return Returns the parent SBML object.
	 */
	public SBase getParentSBMLObject() {
		return parentSBMLObject;
	}
}
