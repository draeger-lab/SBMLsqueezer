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
import java.util.List;

/**
 * 
 * @author Andreas Dr&auml;ger <a
 *         href="mailto:andreas.draeger@uni-tuebingen.de">
 *         andreas.draeger@uni-tuebingen.de</a>
 * 
 */
public class Model extends NamedSBase {

	private LinkedList<Species> listOfSpecies = new LinkedList<Species>();
	private LinkedList<Reaction> listOfReactions = new LinkedList<Reaction>();
	private LinkedList<Parameter> listOfParameters = new LinkedList<Parameter>();;

	public Model(String id) {
		super(id);
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
	 * adds a reaction to the model
	 * 
	 * @param reac
	 */
	public void addReaction(Reaction reac) {
		listOfReactions.add(reac);
		stateChanged();
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

	public void addParameter(Parameter parameter) {
		Parameter p = new Parameter(parameter);
		listOfParameters.add(p);
		p.sbaseAdded();
	}

	public void removeParameter(Parameter parameter) {
		listOfParameters.remove(parameter);
		parameter.sbaseRemoved();
	}

	public List<Species> getListOfSpecies() {
		return listOfSpecies;
	}

	public List<Reaction> getListOfReactions() {
		return listOfReactions;
	}

	public List<Parameter> getListOfParameters() {
		return listOfParameters;
	}

	public int getNumReactions() {
		return listOfReactions.size();
	}

	public int getNumSpecies() {
		return listOfSpecies.size();
	}

	public int getNumParameters() {
		return listOfParameters.size();
	}

	// @Override
	public SBase clone() {
		// TODO Auto-generated method stub
		return null;
	}

	// @Override
	public boolean equals(Object o) {
		// TODO Auto-generated method stub
		return false;
	}
}
