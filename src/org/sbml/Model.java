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

	/**
	 * 
	 * @param model
	 */
	public Model(Model model) {
		super(model);
		listOfCompartments = model.getListOfCompartments().clone();
		setThisAsParentSBMLObject(listOfCompartments);
		listOfSpecies = model.getListOfSpecies().clone();
		setThisAsParentSBMLObject(listOfSpecies);
		listOfParameters = model.getListOfParameters().clone();
		setThisAsParentSBMLObject(listOfParameters);
		listOfReactions = model.getListOfReactions().clone();
		setThisAsParentSBMLObject(listOfReactions);
		listOfEvents = model.getListOfEvents().clone();
		setThisAsParentSBMLObject(listOfEvents);
	}

	/**
	 * 
	 * @param id
	 */
	public Model(String id) {
		super(id);
		listOfCompartments = new ListOf<Compartment>();
		setThisAsParentSBMLObject(listOfCompartments);
		listOfSpecies = new ListOf<Species>();
		setThisAsParentSBMLObject(listOfSpecies);
		listOfParameters = new ListOf<Parameter>();
		setThisAsParentSBMLObject(listOfParameters);
		listOfReactions = new ListOf<Reaction>();
		setThisAsParentSBMLObject(listOfReactions);
		listOfEvents = new ListOf<Event>();
		setThisAsParentSBMLObject(listOfEvents);
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

	/**
	 * 
	 * @param compartment
	 */
	public void addCompartment(Compartment compartment) {
		if (!listOfCompartments.contains(compartment)) {
			Compartment c = compartment.clone();
			listOfCompartments.add(c);
			c.parentSBMLObject = this;
			c.sbaseAdded();
		}
	}

	/**
	 * 
	 * @param parameter
	 */
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
	 * @see org.sbml.NamedSBase#equals(java.lang.Object)
	 */
	// @Override
	public boolean equals(Object o) {
		boolean equal = super.equals(o);
		if (o instanceof Model) {
			Model m = (Model) o;
			equal &= m.getListOfCompartments().equals(listOfCompartments);
			equal &= m.getListOfSpecies().equals(listOfSpecies);
			equal &= m.getListOfParameters().equals(listOfParameters);
			equal &= m.getListOfReactions().equals(listOfReactions);
			equal &= m.getListOfEvents().equals(listOfEvents);
			return equal;
		}
		return false;
	}

	/**
	 * 
	 * @param n
	 * @return
	 */
	public Compartment getCompartment(int n) {
		return listOfCompartments.get(n);
	}

	/**
	 * 
	 * @param id
	 * @return
	 */
	public Compartment getCompartment(String id) {
		for (Compartment c : listOfCompartments) {
			if (c.getId().equals(id))
				return c;
		}
		return null;
	}

	/**
	 * 
	 * @param i
	 * @return
	 */
	public Event getEvent(int i) {
		return listOfEvents.get(i);
	}

	/**
	 * 
	 * @return
	 */
	public ListOf<Compartment> getListOfCompartments() {
		return listOfCompartments;
	}

	/**
	 * 
	 * @return
	 */
	public ListOf<Event> getListOfEvents() {
		return listOfEvents;
	}

	/**
	 * 
	 * @return
	 */
	public ListOf<Parameter> getListOfParameters() {
		return listOfParameters;
	}

	/**
	 * 
	 * @return
	 */
	public ListOf<Reaction> getListOfReactions() {
		return listOfReactions;
	}

	/**
	 * 
	 * @return
	 */
	public ListOf<Species> getListOfSpecies() {
		return listOfSpecies;
	}

	/**
	 * 
	 * @return
	 */
	public int getNumCompartments() {
		return listOfCompartments.size();
	}

	/**
	 * 
	 * @return
	 */
	public int getNumEvents() {
		return listOfEvents.size();
	}

	/**
	 * 
	 * @return
	 */
	public int getNumParameters() {
		return listOfParameters.size();
	}

	/**
	 * 
	 * @return
	 */
	public int getNumReactions() {
		return listOfReactions.size();
	}

	/**
	 * 
	 * @return
	 */
	public int getNumSpecies() {
		return listOfSpecies.size();
	}

	/**
	 * 
	 * @param n
	 * @return
	 */
	public Parameter getParameter(int n) {
		return listOfParameters.get(n);
	}

	/**
	 * 
	 * @param id
	 * @return
	 */
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

	/**
	 * 
	 * @param id
	 * @return
	 */
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

	/**
	 * 
	 * @param id
	 * @return
	 */
	public Species getSpecies(String id) {
		for (Species s : listOfSpecies) {
			if (s.getId().equals(id))
				return s;
		}
		return null;
	}

	/**
	 * 
	 * @param parameter
	 */
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

	/**
	 * 
	 * @param listOfCompartments
	 */
	public void setListOfCompartments(ListOf<Compartment> listOfCompartments) {
		this.listOfCompartments = listOfCompartments.clone();
		setThisAsParentSBMLObject(listOfCompartments);
	}

	/**
	 * 
	 * @param listOfEvents
	 */
	public void setListOfEvents(ListOf<Event> listOfEvents) {
		this.listOfEvents = listOfEvents.clone();
		setThisAsParentSBMLObject(this.listOfEvents);
	}

	/**
	 * 
	 * @param listOfParameters
	 */
	public void setListOfParameters(ListOf<Parameter> listOfParameters) {
		this.listOfParameters = listOfParameters.clone();
		setThisAsParentSBMLObject(this.listOfParameters);
	}

	/**
	 * 
	 * @param listOfReactions
	 */
	public void setListOfReactions(ListOf<Reaction> listOfReactions) {
		this.listOfReactions = listOfReactions.clone();
		setThisAsParentSBMLObject(this.listOfReactions);
	}

	/**
	 * 
	 * @param listOfSpecies
	 */
	public void setListOfSpecies(ListOf<Species> listOfSpecies) {
		this.listOfSpecies = listOfSpecies.clone();
		setThisAsParentSBMLObject(this.listOfSpecies);
	}
}
