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

	private ListOf<Compartment> listOfCompartments;
	private ListOf<Species> listOfSpecies;
	private ListOf<Parameter> listOfParameters;
	private ListOf<Reaction> listOfReactions;
	private ListOf<Event> listOfEvents;

	public Model(Model model) {
		super(model);
		listOfCompartments = model.getListOfCompartments().clone();
		listOfSpecies = model.getListOfSpecies().clone();
		listOfParameters = model.getListOfParameters().clone();
		listOfReactions = model.getListOfReactions().clone();
		listOfEvents = model.getListOfEvents().clone();
	}

	public Model(String id) {
		super(id);
		listOfCompartments = new ListOf<Compartment>();
		listOfSpecies = new ListOf<Species>();
		listOfParameters = new ListOf<Parameter>();
		listOfReactions = new ListOf<Reaction>();
		listOfEvents = new ListOf<Event>();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.sbml.SBase#addChangeListener(org.sbml.squeezer.io.SBaseChangedListener
	 * )
	 */
	public void addChangeListener(SBaseChangedListener l) {
		super.addChangeListener(l);
		listOfCompartments.addChangeListener(l);
		listOfSpecies.addChangeListener(l);
		listOfParameters.addChangeListener(l);
		listOfReactions.addChangeListener(l);
		listOfEvents.addChangeListener(l);
	}

	public void addCompartment(Compartment compartment) {
		if (!listOfCompartments.contains(compartment)) {
			Compartment c = compartment.clone();
			listOfCompartments.add(c);
			c.parentSBMLObject = this;
			c.sbaseAdded();
		}
	}

	public void addParameter(Parameter parameter) {
		if (!listOfParameters.contains(parameter)) {
			Parameter p = parameter.clone();
			listOfParameters.add(p);
			p.parentSBMLObject = this;
			p.sbaseAdded();
		}
	}

	/**
	 * adds a reaction to the model
	 * 
	 * @param reac
	 */
	public void addReaction(Reaction reac) {
		if (!listOfReactions.contains(reac)) {
			Reaction r = reac.clone();
			listOfReactions.add(r);
			r.parentSBMLObject = this;
			r.sbaseAdded();
		}
	}

	/**
	 * adds a species to the model
	 * 
	 * @param spec
	 */
	public void addSpecies(Species spec) {
		if (!listOfSpecies.contains(spec)) {
			Species s = spec.clone();
			listOfSpecies.add(s);
			s.parentSBMLObject = this;
			s.sbaseAdded();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.SBase#clone()
	 */
	// @Override
	public Model clone() {
		return new Model(this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.SBase#equals(java.lang.Object)
	 */
	// @Override
	public boolean equals(Object o) {
		if (o instanceof Model) {
			Model m = (Model) o;
			return m.getListOfCompartments().equals(listOfCompartments)
					&& m.getListOfSpecies().equals(listOfSpecies)
					&& m.getListOfParameters().equals(listOfParameters)
					&& m.getListOfReactions().equals(listOfReactions)
					&& m.getListOfEvents().equals(listOfEvents)
					&& m.getSBOTerm() == getSBOTerm();
		}
		return false;
	}

	public Compartment getCompartment(int n) {
		return listOfCompartments.get(n);
	}

	public Compartment getCompartment(String id) {
		for (Compartment c : listOfCompartments) {
			if (c.getId().equals(id))
				return c;
		}
		return null;
	}

	public Event getEvent(int i) {
		return listOfEvents.get(i);
	}

	public ListOf<Compartment> getListOfCompartments() {
		return listOfCompartments;
	}

	public ListOf<Event> getListOfEvents() {
		return listOfEvents;
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

	public int getNumCompartments() {
		return listOfCompartments.size();
	}

	public int getNumEvents() {
		return listOfEvents.size();
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

	public Parameter getParameter(int n) {
		return listOfParameters.get(n);
	}

	public Parameter getParameter(String id) {
		for (Parameter p : listOfParameters) {
			if (p.getId().equals(id))
				return p;
		}
		return null;
	}

	/**
	 * Get the n-th Reaction object in this Model.
	 * 
	 * @param reactionIndex
	 * @return the n-th Reaction of this Model.
	 */
	public Reaction getReaction(int n) {
		return listOfReactions.get(n);
	}

	public Reaction getReaction(String id) {
		for (Reaction r : listOfReactions) {
			if (r.getId().equals(id))
				return r;
		}
		return null;
	}

	/**
	 * Get the n-th Species object in this Model.
	 * 
	 * @param n
	 *            the nth Species of this Model.
	 * @return
	 */
	public Species getSpecies(int n) {
		return listOfSpecies.get(n);
	}

	public Species getSpecies(String id) {
		for (Species s : listOfSpecies) {
			if (s.getId().equals(id))
				return s;
		}
		return null;
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
		spec.sbaseRemoved();
	}

	public void setListOfCompartments(ListOf<Compartment> listOfCompartments) {
		this.listOfCompartments = listOfCompartments.clone();
	}

	public void setListOfEvents(ListOf<Event> listOfEvents) {
		this.listOfEvents = listOfEvents.clone();
	}

	public void setListOfParameters(ListOf<Parameter> listOfParameters) {
		this.listOfParameters = listOfParameters.clone();
	}

	public void setListOfReactions(ListOf<Reaction> listOfReactions) {
		this.listOfReactions = listOfReactions.clone();
	}

	public void setListOfSpecies(ListOf<Species> listOfSpecies) {
		this.listOfSpecies = listOfSpecies.clone();
	}
}
