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

import org.sbml.squeezer.io.SBaseChangedListener;

/**
 * 
 * @author Andreas Dr&auml;ger <a
 *         href="mailto:andreas.draeger@uni-tuebingen.de">
 *         andreas.draeger@uni-tuebingen.de</a>
 * 
 */
public class Model extends AbstractNamedSBase {

	private ListOf<FunctionDefinition> listOfFunctionDefinitions;
	private ListOf<UnitDefinition> listOfUnitDefinitions;
	private ListOf<CompartmentType> listOfCompartmentTypes;
	private ListOf<SpeciesType> listOfSpeciesTypes;
	private ListOf<Compartment> listOfCompartments;
	private ListOf<Species> listOfSpecies;
	private ListOf<Parameter> listOfParameters;
	private ListOf<InitialAssignment> listOfInitialAssignments;
	private ListOf<Rule> listOfRules;
	private ListOf<Constraint> listOfConstraints;
	private ListOf<Reaction> listOfReactions;
	private ListOf<Event> listOfEvents;

	/**
	 * 
	 * @param model
	 */
	public Model(Model model) {
		super(model);
		listOfFunctionDefinitions = model.getListOfFunctionDefinitions()
				.clone();
		setThisAsParentSBMLObject(listOfFunctionDefinitions);
		listOfUnitDefinitions = model.getListOfUnitDefinitions();
		setThisAsParentSBMLObject(listOfUnitDefinitions);
		listOfCompartmentTypes = model.getListOfCompartmentTypes().clone();
		setThisAsParentSBMLObject(listOfCompartmentTypes);
		listOfSpeciesTypes = model.getListOfSpeciesTypes().clone();
		setThisAsParentSBMLObject(listOfSpeciesTypes);
		listOfCompartments = model.getListOfCompartments().clone();
		setThisAsParentSBMLObject(listOfCompartments);
		listOfSpecies = model.getListOfSpecies().clone();
		setThisAsParentSBMLObject(listOfSpecies);
		listOfParameters = model.getListOfParameters().clone();
		setThisAsParentSBMLObject(listOfParameters);
		listOfInitialAssignments = model.getListOfInitialAssignments().clone();
		setThisAsParentSBMLObject(listOfInitialAssignments);
		listOfRules = model.getListOfRules().clone();
		setThisAsParentSBMLObject(listOfRules);
		listOfConstraints = model.getListOfConstraints().clone();
		setThisAsParentSBMLObject(listOfConstraints);
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
		listOfFunctionDefinitions = new ListOf<FunctionDefinition>();
		setThisAsParentSBMLObject(listOfFunctionDefinitions);
		listOfUnitDefinitions = new ListOf<UnitDefinition>();
		setThisAsParentSBMLObject(listOfUnitDefinitions);
		listOfCompartmentTypes = new ListOf<CompartmentType>();
		setThisAsParentSBMLObject(listOfCompartmentTypes);
		listOfSpeciesTypes = new ListOf<SpeciesType>();
		setThisAsParentSBMLObject(listOfSpeciesTypes);
		listOfCompartments = new ListOf<Compartment>();
		setThisAsParentSBMLObject(listOfCompartments);
		listOfSpecies = new ListOf<Species>();
		setThisAsParentSBMLObject(listOfSpecies);
		listOfParameters = new ListOf<Parameter>();
		setThisAsParentSBMLObject(listOfParameters);
		listOfInitialAssignments = new ListOf<InitialAssignment>();
		setThisAsParentSBMLObject(listOfInitialAssignments);
		listOfRules = new ListOf<Rule>();
		setThisAsParentSBMLObject(listOfRules);
		listOfConstraints = new ListOf<Constraint>();
		setThisAsParentSBMLObject(listOfRules);
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
		listOfFunctionDefinitions.addChangeListener(l);
		listOfUnitDefinitions.addChangeListener(l);
		listOfCompartmentTypes.addChangeListener(l);
		listOfSpeciesTypes.addChangeListener(l);
		listOfCompartments.addChangeListener(l);
		listOfSpecies.addChangeListener(l);
		listOfParameters.addChangeListener(l);
		listOfInitialAssignments.addChangeListener(l);
		listOfRules.addChangeListener(l);
		listOfConstraints.addChangeListener(l);
		listOfReactions.addChangeListener(l);
		listOfEvents.addChangeListener(l);
	}

	/**
	 * 
	 * @param compartment
	 */
	public void addCompartment(Compartment compartment) {
		if (!listOfCompartments.contains(compartment)) {
			listOfCompartments.add(compartment);
			compartment.parentSBMLObject = this;
			compartment.sbaseAdded();
		}
	}

	/**
	 * 
	 * @param event
	 */
	public void addEvent(Event event) {
		if (!listOfEvents.contains(event)) {
			listOfEvents.add(event);
			event.parentSBMLObject = this;
			event.sbaseAdded();
		}
	}

	/**
	 * 
	 * @param 
	 */
	public void addFunctionDefinition(FunctionDefinition functionDefinition) {
		if (!listOfFunctionDefinitions.contains(functionDefinition)) {
			listOfFunctionDefinitions.add(functionDefinition);
			functionDefinition.parentSBMLObject = this;
			functionDefinition.sbaseAdded();
		}
	}

	/**
	 * 
	 * @param parameter
	 */
	public void addParameter(Parameter parameter) {
		if (!listOfParameters.contains(parameter)) {
			listOfParameters.add(parameter);
			parameter.parentSBMLObject = this;
			parameter.sbaseAdded();
		}
	}

	/**
	 * adds a reaction to the model
	 * 
	 * @param reac
	 */
	public void addReaction(Reaction reac) {
		if (!listOfReactions.contains(reac)) {
			listOfReactions.add(reac);
			reac.parentSBMLObject = this;
			reac.sbaseAdded();
		}
	}

	/**
	 * adds a species to the model
	 * 
	 * @param spec
	 */
	public void addSpecies(Species spec) {
		if (!listOfSpecies.contains(spec)) {
			listOfSpecies.add(spec);
			spec.parentSBMLObject = this;
			spec.sbaseAdded();
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
			equal &= m.getListOfFunctionDefinitions().equals(
					listOfFunctionDefinitions);
			// equal &=
			// m.getListOfUnitDefinitions().equals(listOfUnitDefinitions);
			// equal &=
			// m.getListOfCompartmentTypes().equals(listOfCompartmentTypes);
			// equal &= m.getlistOfSpeciesTypes().equals(listOfSpeciesTypes);
			equal &= m.getListOfCompartments().equals(listOfCompartments);
			equal &= m.getListOfSpecies().equals(listOfSpecies);
			equal &= m.getListOfParameters().equals(listOfParameters);
			// equal &=
			// m.getListOfInitialAssignments().equals(listOfInitialAssignments);
			equal &= m.getListOfRules().equals(listOfRules);
			// equal &= m.getListOfConstraints().equals(listOfConstraints);
			equal &= m.getListOfReactions().equals(listOfReactions);
			equal &= m.getListOfEvents().equals(listOfEvents);
			return equal;
		}
		return false;
	}

	/**
	 * try to figure out the meaning of this name.
	 * 
	 * @param id
	 *            an id indicating a variable of the model.
	 * @return null if no model is available or the model does not contain a
	 *         compartment, species, or parameter wit the given id.
	 */
	public NamedSBase findNamedSBase(String id) {
		NamedSBase namedSBase = null;
		if (parentSBMLObject != null && parentSBMLObject.getModel() != null) {
			Model m = parentSBMLObject.getModel();
			namedSBase = m.getCompartment(id);
			if (namedSBase == null)
				namedSBase = m.getSpecies(id);
			if (namedSBase == null)
				namedSBase = m.getParameter(id);
			if (namedSBase == null)
				namedSBase = m.getReaction(id);
			if (namedSBase == null)
				namedSBase = m.getFunctionDefinition(id);

			// check all local parameters
			if (namedSBase == null)
				for (Reaction r : m.getListOfReactions()) {
					if (r.isSetKineticLaw())
						namedSBase = r.getKineticLaw().getParameter(id);
					if (namedSBase != null)
						break;
				}
		}
		return namedSBase;
	}

	/**
	 * 
	 * @param variable
	 * @return
	 */
	public Variable findVariable(String variable) {
		Variable nsb = getSpecies(variable);
		if (nsb == null)
			nsb = getParameter(variable);
		if (nsb == null)
			nsb = getCompartment(variable);
		return nsb;
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
	 * @param n
	 * @return
	 */
	public FunctionDefinition getFunctionDefinition(int n) {
		return listOfFunctionDefinitions.get(n);
	}

	/**
	 * 
	 * @param id
	 * @return
	 */
	public FunctionDefinition getFunctionDefinition(String id) {
		// TODO Auto-generated method stub
		return null;
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
	public ListOf<CompartmentType> getListOfCompartmentTypes() {
		return listOfCompartmentTypes;
	}

	/**
	 * @return the listOfConstraints
	 */
	public ListOf<Constraint> getListOfConstraints() {
		return listOfConstraints;
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
	public ListOf<FunctionDefinition> getListOfFunctionDefinitions() {
		return listOfFunctionDefinitions;
	}

	/**
	 * @return the listOfInitialAssignments
	 */
	public ListOf<InitialAssignment> getListOfInitialAssignments() {
		return listOfInitialAssignments;
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
	public ListOf<Rule> getListOfRules() {
		return listOfRules;
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
	public ListOf<SpeciesType> getListOfSpeciesTypes() {
		return listOfSpeciesTypes;
	}

	/**
	 * 
	 * @return
	 */
	public ListOf<UnitDefinition> getListOfUnitDefinitions() {
		return listOfUnitDefinitions;
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
	public int getNumCompartmentTypes() {
		return listOfCompartmentTypes.size();
	}

	/**
	 * 
	 * @return
	 */
	public int getNumConstraints() {
		return listOfConstraints.size();
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
	public int getNumFunctionDefinitions() {
		return listOfFunctionDefinitions.size();
	}

	/**
	 * 
	 * @return
	 */
	public int getNumInitialAssignments() {
		return listOfInitialAssignments.size();
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
	public int getNumRules() {
		return listOfRules.size();
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
	 * @return
	 */
	public int getNumSpeciesTypes() {
		return listOfSpeciesTypes.size();
	}

	/**
	 * 
	 * @return
	 */
	public int getNumUnitDefinitions() {
		return listOfUnitDefinitions.size();
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
	 * 
	 * @param n
	 * @return
	 */
	public Rule getRule(int n) {
		return listOfRules.get(n);
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
		stateChanged();
	}

	/**
	 * @param listOfCompartmentTypes
	 *            the listOfCompartmentTypes to set
	 */
	public void setListOfCompartmentTypes(
			ListOf<CompartmentType> listOfCompartmentTypes) {
		this.listOfCompartmentTypes = listOfCompartmentTypes;
		setThisAsParentSBMLObject(listOfCompartmentTypes);
		stateChanged();
	}

	/**
	 * @param listOfConstraints
	 *            the listOfConstraints to set
	 */
	public void setListOfConstraints(ListOf<Constraint> listOfConstraints) {
		this.listOfConstraints = listOfConstraints;
		setThisAsParentSBMLObject(listOfConstraints);
	}

	/**
	 * 
	 * @param listOfEvents
	 */
	public void setListOfEvents(ListOf<Event> listOfEvents) {
		this.listOfEvents = listOfEvents.clone();
		setThisAsParentSBMLObject(this.listOfEvents);
		stateChanged();
	}

	/**
	 * @param listOfFunctionDefinitions
	 *            the listOfFunctionDefinitions to set
	 */
	public void setListOfFunctionDefinitions(
			ListOf<FunctionDefinition> listOfFunctionDefinitions) {
		this.listOfFunctionDefinitions = listOfFunctionDefinitions;
		setThisAsParentSBMLObject(this.listOfFunctionDefinitions);
		stateChanged();
	}

	/**
	 * @param listOfInitialAssignments
	 *            the listOfInitialAssignments to set
	 */
	public void setListOfInitialAssignments(
			ListOf<InitialAssignment> listOfInitialAssignments) {
		this.listOfInitialAssignments = listOfInitialAssignments;
		setThisAsParentSBMLObject(listOfInitialAssignments);
	}

	/**
	 * 
	 * @param listOfParameters
	 */
	public void setListOfParameters(ListOf<Parameter> listOfParameters) {
		this.listOfParameters = listOfParameters.clone();
		setThisAsParentSBMLObject(this.listOfParameters);
		stateChanged();
	}

	/**
	 * 
	 * @param listOfReactions
	 */
	public void setListOfReactions(ListOf<Reaction> listOfReactions) {
		this.listOfReactions = listOfReactions.clone();
		setThisAsParentSBMLObject(this.listOfReactions);
		stateChanged();
	}

	/**
	 * 
	 * @return
	 */
	public void setListOfRules(ListOf<Rule> listOfRules) {
		this.listOfRules = listOfRules;
		setThisAsParentSBMLObject(this.listOfRules);
		stateChanged();
	}

	/**
	 * 
	 * @param listOfSpecies
	 */
	public void setListOfSpecies(ListOf<Species> listOfSpecies) {
		this.listOfSpecies = listOfSpecies.clone();
		setThisAsParentSBMLObject(this.listOfSpecies);
		stateChanged();
	}

	/**
	 * @param listOfSpeciesTypes
	 *            the listOfSpeciesTypes to set
	 */
	public void setListOfSpeciesTypes(ListOf<SpeciesType> listOfSpeciesTypes) {
		this.listOfSpeciesTypes = listOfSpeciesTypes;
		setThisAsParentSBMLObject(listOfSpeciesTypes);
		stateChanged();
	}

	/**
	 * 
	 * @param listOfUnitDefinitions
	 */
	public void setListOfUnitDefinitions(
			ListOf<UnitDefinition> listOfUnitDefinitions) {
		this.listOfUnitDefinitions = listOfUnitDefinitions;
		setThisAsParentSBMLObject(this.listOfUnitDefinitions);
		stateChanged();
	}
}
