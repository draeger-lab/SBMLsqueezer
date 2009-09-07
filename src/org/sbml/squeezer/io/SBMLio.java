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
package org.sbml.squeezer.io;

import java.util.LinkedList;
import java.util.List;

import javax.swing.JTabbedPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.sbml.jlibsbml.AbstractSBase;
import org.sbml.jlibsbml.Compartment;
import org.sbml.jlibsbml.CompartmentType;
import org.sbml.jlibsbml.Constraint;
import org.sbml.jlibsbml.Delay;
import org.sbml.jlibsbml.Event;
import org.sbml.jlibsbml.EventAssignment;
import org.sbml.jlibsbml.FunctionDefinition;
import org.sbml.jlibsbml.InitialAssignment;
import org.sbml.jlibsbml.KineticLaw;
import org.sbml.jlibsbml.Model;
import org.sbml.jlibsbml.ModifierSpeciesReference;
import org.sbml.jlibsbml.NamedSBase;
import org.sbml.jlibsbml.Parameter;
import org.sbml.jlibsbml.Reaction;
import org.sbml.jlibsbml.Rule;
import org.sbml.jlibsbml.SBMLReader;
import org.sbml.jlibsbml.SBMLWriter;
import org.sbml.jlibsbml.SBase;
import org.sbml.jlibsbml.Species;
import org.sbml.jlibsbml.SpeciesReference;
import org.sbml.jlibsbml.SpeciesType;
import org.sbml.jlibsbml.StoichiometryMath;
import org.sbml.jlibsbml.Trigger;
import org.sbml.jlibsbml.Unit;
import org.sbml.jlibsbml.UnitDefinition;

/**
 * @author Andreas Dr&auml;ger <a
 *         href="mailto:andreas.draeger@uni-tuebingen.de">
 *         andreas.draeger@uni-tuebingen.de</a>
 * 
 */
public class SBMLio implements SBMLReader, SBMLWriter, SBaseChangedListener,
		ChangeListener {

	private List<SBase> added;
	private List<SBase> removed;
	private List<SBase> changed;
	private AbstractSBMLReader reader;
	private AbstractSBMLWriter writer;
	protected LinkedList<Model> listOfModels;
	private LinkedList<Object> listOfOrigModels;
	private int selectedModel;

	/**
	 * 
	 */
	public SBMLio(AbstractSBMLReader reader, AbstractSBMLWriter writer) {
		this.reader = reader;
		// this.reader.addSBaseChangeListener(this);
		this.writer = writer;
		listOfModels = new LinkedList<Model>();
		listOfOrigModels = new LinkedList<Object>();
		selectedModel = -1;
		added = new LinkedList<SBase>();
		removed = new LinkedList<SBase>();
		changed = new LinkedList<SBase>();
	}

	/**
	 * 
	 * @param model
	 */
	public SBMLio(AbstractSBMLReader reader, AbstractSBMLWriter writer,
			Object model) {
		this(reader, writer);
		this.listOfModels.addLast(reader.readModel(model));
		this.listOfOrigModels.addLast(model);
	}

	/**
	 * 
	 * @return
	 */
	public List<Model> getListOfModels() {
		return listOfModels;
	}

	/**
	 * 
	 * @return
	 */
	public Model getSelectedModel() {
		return listOfModels.get(selectedModel);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.SBMLReader#readCompartment(java.lang.Object)
	 */
	public Compartment readCompartment(Object compartment) {
		return reader.readCompartment(compartment);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.SBMLReader#readFunctionDefinition(java.lang.Object)
	 */
	public FunctionDefinition readFunctionDefinition(Object functionDefinition) {
		return reader.readFunctionDefinition(functionDefinition);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.SBMLReader#readInitialAssignment(java.lang.Object)
	 */
	public InitialAssignment readInitialAssignment(Object initialAssignment) {
		return reader.readInitialAssignment(initialAssignment);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.SBMLReader#readKineticLaw(java.lang.Object)
	 */
	public KineticLaw readKineticLaw(Object kineticLaw) {
		return reader.readKineticLaw(kineticLaw);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.SBMLReader#readModel(java.lang.Object)
	 */
	// @Override
	public Model readModel(Object model) {
		listOfModels.addLast(reader.readModel(model));
		if (model instanceof String)
			listOfOrigModels.addLast(reader.getOriginalModel());
		else
			listOfOrigModels.addLast(model);
		selectedModel = listOfModels.size() - 1;
		return listOfModels.getLast();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.SBMLReader#readModifierSpeciesReference(java.lang.Object)
	 */
	public ModifierSpeciesReference readModifierSpeciesReference(
			Object modifierSpeciesReference) {
		return reader.readModifierSpeciesReference(modifierSpeciesReference);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.SBMLReader#readParameter(java.lang.Object)
	 */
	public Parameter readParameter(Object parameter) {
		return reader.readParameter(parameter);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.SBMLReader#readReaction(java.lang.Object)
	 */
	// @Override
	public Reaction readReaction(Object reaction) {
		return reader.readReaction(reaction);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.SBMLReader#readRule(java.lang.Object)
	 */
	public Rule readRule(Object rule) {
		return reader.readRule(rule);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.SBMLReader#readSpecies(java.lang.Object)
	 */
	public Species readSpecies(Object species) {
		return reader.readSpecies(species);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.SBMLReader#readSpeciesReference(java.lang.Object)
	 */
	public SpeciesReference readSpeciesReference(Object speciesReference) {
		return reader.readSpeciesReference(speciesReference);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.SBMLReader#readSpeciesType(java.lang.Object)
	 */
	public SpeciesType readSpeciesType(Object speciesType) {
		return reader.readSpeciesType(speciesType);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.SBMLReader#readStoichiometricMath(java.lang.Object)
	 */
	public StoichiometryMath readStoichiometricMath(Object stoichiometryMath) {
		return reader.readStoichiometricMath(stoichiometryMath);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.SBMLReader#readUnit(java.lang.Object)
	 */
	public Unit readUnit(Object unit) {
		return reader.readUnit(unit);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.SBMLReader#readUnitDefinition(java.lang.Object)
	 */
	public UnitDefinition readUnitDefinition(Object unitDefinition) {
		return reader.readUnitDefinition(unitDefinition);
	}

	/**
	 * Write all changes back into the original model.
	 */
	public void saveChanges() {
		System.out.println("removed: " + removed);
		System.out.println("added:   " + added);
		System.out.println("changed: " + changed);
		writer.saveChanges(listOfModels.get(selectedModel), listOfOrigModels
				.get(selectedModel));
		System.out.println("fertig");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seeorg.sbml.SBMLWriter#saveModifierSpeciesReferenceProperties(org.sbml.
	 * ModifierSpeciesReference, java.lang.Object)
	 */
	public void saveModifierSpeciesReferenceProperties(
			ModifierSpeciesReference modifierSpeciesReference, Object msr) {
		writer.saveModifierSpeciesReferenceProperties(modifierSpeciesReference,
				msr);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.SBMLWriter#saveNamedSBaseProperties(org.sbml.NamedSBase,
	 * java.lang.Object)
	 */
	public void saveNamedSBaseProperties(NamedSBase nsb, Object sb) {
		writer.saveNamedSBaseProperties(nsb, sb);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.SBMLWriter#saveSBaseProperties(org.sbml.SBase,
	 * java.lang.Object)
	 */
	public void saveSBaseProperties(SBase s, Object sb) {
		writer.saveSBaseProperties(s, sb);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.SBaseChangedListener#sbaseAdded(org.sbml.SBase)
	 */
	public void sbaseAdded(AbstractSBase sb) {
		if (!added.contains(sb))
			added.add(sb);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.SBaseChangedListener#sbaseRemoved(org.sbml.SBase)
	 */
	public void sbaseRemoved(AbstractSBase sb) {
		if (!removed.contains(sb))
			removed.add(sb);
	}

	public void setSelectedModel(int selectedModel) {
		this.selectedModel = selectedModel;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.SBaseChangedListener#stateChanged(org.sbml.SBase)
	 */
	public void stateChanged(AbstractSBase sb) {
		if (!changed.contains(sb))
			changed.add(sb);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * javax.swing.event.ChangeListener#stateChanged(javax.swing.event.ChangeEvent
	 * )
	 */
	public void stateChanged(ChangeEvent e) {
		if (e.getSource() instanceof JTabbedPane) {
			JTabbedPane tabbedPane = (JTabbedPane) e.getSource();
			if (tabbedPane.getComponentCount() == 0)
				listOfModels.clear();
			else {
				for (Model m : listOfModels) {
					boolean contains = false;
					for (int i = 0; i < tabbedPane.getTabCount() && !contains; i++) {
						String title = tabbedPane.getTitleAt(i);
						if (title.equals(m.getName())
								|| title.equals(m.getId()))
							contains = true;
					}
					if (!contains)
						listOfModels.remove(m);
				}
				selectedModel = tabbedPane.getSelectedIndex();
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.SBMLWriter#writeCompartment(org.sbml.Compartment)
	 */
	// @Override
	public Object writeCompartment(Compartment compartment) {
		return writer.writeCompartment(compartment);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.SBMLWriter#writeCompartmentType(org.sbml.CompartmentType)
	 */
	// @Override
	public Object writeCompartmentType(CompartmentType compartmentType) {
		return writer.writeCompartmentType(compartmentType);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.SBMLWriter#writeConstraint(org.sbml.Constraint)
	 */
	// @Override
	public Object writeConstraint(Constraint constraint) {
		return writer.writeConstraint(constraint);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.SBMLWriter#writeDelay(org.sbml.Delay)
	 */
	// @Override
	public Object writeDelay(Delay delay) {
		return writer.writeDelay(delay);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.SBMLWriter#writeEvent(org.sbml.Event)
	 */
	// @Override
	public Object writeEvent(Event event) {
		return writer.writeEvent(event);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.SBMLWriter#writeEventAssignment(org.sbml.EventAssignment)
	 */
	// @Override
	public Object writeEventAssignment(EventAssignment eventAssignment) {
		return writer.writeEventAssignment(eventAssignment);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.sbml.SBMLWriter#writeFunctionDefinition(org.sbml.FunctionDefinition)
	 */
	// @Override
	public Object writeFunctionDefinition(FunctionDefinition functionDefinition) {
		return writer.writeFunctionDefinition(functionDefinition);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.sbml.SBMLWriter#writeInitialAssignment(org.sbml.InitialAssignment)
	 */
	// @Override
	public Object writeInitialAssignment(InitialAssignment initialAssignment) {
		return writer.writeInitialAssignment(initialAssignment);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.SBMLWriter#writeKineticLaw(org.sbml.KineticLaw)
	 */
	public Object writeKineticLaw(KineticLaw kineticLaw) {
		return writer.writeKineticLaw(kineticLaw);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.SBMLWriter#writeModel(org.sbml.Model)
	 */
	// @Override
	public Object writeModel(Model model) {
		return writer.writeModel(model);
	}

	/**
	 * 
	 * @param model
	 * @param filename
	 * @return
	 */
	public boolean writeModelToSBML(int model, String filename) {
		return writer.writeSBML(listOfOrigModels.get(model), filename);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seeorg.sbml.SBMLWriter#writeModifierSpeciesReference(org.sbml.
	 * ModifierSpeciesReference)
	 */
	public Object writeModifierSpeciesReference(
			ModifierSpeciesReference modifierSpeciesReference) {
		return writer.writeModifierSpeciesReference(modifierSpeciesReference);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.SBMLWriter#writeParameter(org.sbml.Parameter)
	 */
	public Object writeParameter(Parameter parameter) {
		return writer.writeParameter(parameter);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.SBMLWriter#writeReaction(org.sbml.Reaction)
	 */
	// @Override
	public Object writeReaction(Reaction reaction) {
		return writer.writeReaction(reaction);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.SBMLWriter#writeRule(org.sbml.Rule)
	 */
	// @Override
	public Object writeRule(Rule rule) {
		return writer.writeRule(rule);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.SBMLWriter#writeSBML(java.lang.Object, java.lang.String)
	 */
	// @Override
	public boolean writeSBML(Object sbmlDocument, String filename) {
		return writer.writeSBML(sbmlDocument, filename);
	}

	/**
	 * 
	 * @param filename
	 * @return
	 */
	public boolean writeSelectedModelToSBML(String filename) {
		return writer.writeSBML(listOfOrigModels.get(selectedModel), filename);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.SBMLWriter#writeSpecies(org.sbml.Species)
	 */
	// @Override
	public Object writeSpecies(Species species) {
		return writer.writeSpecies(species);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.SBMLWriter#writeSpeciesReference(org.sbml.SpeciesReference)
	 */
	public Object writeSpeciesReference(SpeciesReference speciesReference) {
		return writer.writeSpeciesReference(speciesReference);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.SBMLWriter#writeSpeciesType(org.sbml.SpeciesType)
	 */
	// @Override
	public Object writeSpeciesType(SpeciesType speciesType) {
		return writer.writeSpeciesType(speciesType);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.sbml.SBMLWriter#writeStoichoimetryMath(org.sbml.StoichiometryMath)
	 */
	public Object writeStoichoimetryMath(StoichiometryMath stoichiometryMath) {
		return writer.writeStoichoimetryMath(stoichiometryMath);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.SBMLWriter#writeTrigger(org.sbml.Trigger)
	 */
	// @Override
	public Object writeTrigger(Trigger trigger) {
		return writer.writeTrigger(trigger);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.SBMLWriter#writeUnit(org.sbml.Unit)
	 */
	// @Override
	public Object writeUnit(Unit unit) {
		return writer.writeUnit(unit);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.SBMLWriter#writeUnitDefinition(org.sbml.UnitDefinition)
	 */
	// @Override
	public Object writeUnitDefinition(UnitDefinition unitDefinition) {
		return writer.writeUnitDefinition(unitDefinition);
	}
}
