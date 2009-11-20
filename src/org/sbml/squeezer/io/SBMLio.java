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

import java.io.IOException;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JTabbedPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.sbml.jsbml.CVTerm;
import org.sbml.jsbml.Compartment;
import org.sbml.jsbml.CompartmentType;
import org.sbml.jsbml.Constraint;
import org.sbml.jsbml.Delay;
import org.sbml.jsbml.Event;
import org.sbml.jsbml.EventAssignment;
import org.sbml.jsbml.FunctionDefinition;
import org.sbml.jsbml.InitialAssignment;
import org.sbml.jsbml.KineticLaw;
import org.sbml.jsbml.MathContainer;
import org.sbml.jsbml.Model;
import org.sbml.jsbml.ModelHistory;
import org.sbml.jsbml.ModifierSpeciesReference;
import org.sbml.jsbml.NamedSBase;
import org.sbml.jsbml.Parameter;
import org.sbml.jsbml.Reaction;
import org.sbml.jsbml.Rule;
import org.sbml.jsbml.SBMLException;
import org.sbml.jsbml.SBMLReader;
import org.sbml.jsbml.SBMLWriter;
import org.sbml.jsbml.SBase;
import org.sbml.jsbml.SBaseChangedListener;
import org.sbml.jsbml.Species;
import org.sbml.jsbml.SpeciesReference;
import org.sbml.jsbml.SpeciesType;
import org.sbml.jsbml.StoichiometryMath;
import org.sbml.jsbml.Trigger;
import org.sbml.jsbml.Unit;
import org.sbml.jsbml.UnitDefinition;
import org.sbml.jsbml.io.AbstractSBMLReader;
import org.sbml.jsbml.io.AbstractSBMLWriter;
import org.sbml.squeezer.SBMLsqueezer;

/**
 * @author Andreas Dr&auml;ger <a
 *         href="mailto:andreas.draeger@uni-tuebingen.de">
 *         andreas.draeger@uni-tuebingen.de</a>
 * 
 */
public class SBMLio implements SBMLReader, SBMLWriter, SBaseChangedListener,
		ChangeListener {

	private List<SBase> added;

	private List<SBase> changed;

	private LinkedList<Object> listOfOrigModels;

	private AbstractSBMLReader reader;

	private List<SBase> removed;

	private int selectedModel;

	private AbstractSBMLWriter writer;

	protected LinkedList<Model> listOfModels;

	/**
	 * 
	 */
	public SBMLio(AbstractSBMLReader sbmlReader, AbstractSBMLWriter sbmlWriter) {
		this.reader = sbmlReader;
		// this.reader.addSBaseChangeListener(this);
		this.writer = sbmlWriter;
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.jlibsbml.SBMLWriter#convertDate(java.util.Date)
	 */
	public Object convertDate(Date date) {
		return writer.convertDate(date);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.jlibsbml.SBMLReader#convertDate(java.lang.Object)
	 */
	public Date convertDate(Object d) {
		return reader.convertDate(d);
	}

	/**
	 * 
	 * @return
	 */
	public List<Model> getListOfModels() {
		return listOfModels;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.jlibsbml.SBMLReader#getNumErrors()
	 */
	public int getNumErrors() {
		return listOfModels.size() > 0 ? reader.getNumErrors() : 0;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.jlibsbml.SBMLWriter#getNumErrors(java.lang.Object)
	 */
	public int getNumErrors(Object sbase) {
		return writer.getNumErrors(sbase);
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
	 * @see org.sbml.jlibsbml.SBMLReader#getWarnings()
	 */
	public List<SBMLException> getWarnings() {
		return reader.getWarnings();
	}

	/**
	 * 
	 * @return
	 */
	public List<SBMLException> getWriteWarnings() {
		return writer.getWriteWarnings(listOfOrigModels.get(selectedModel));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.jlibsbml.SBMLWriter#getWarnings(java.lang.Object)
	 */
	public List<SBMLException> getWriteWarnings(Object sbase) {
		return writer.getWriteWarnings(sbase);
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
	 * @see org.sbml.jlibsbml.SBMLReader#readCVTerm(java.lang.Object)
	 */
	public CVTerm readCVTerm(Object term) {
		return reader.readCVTerm(term);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.jlibsbml.SBMLReader#readEventAssignment(java.lang.Object)
	 */
	public EventAssignment readEventAssignment(Object eventAssignment) {
		return reader.readEventAssignment(eventAssignment);
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
		try {
			listOfModels.addLast(reader.readModel(model));
			if (model instanceof String)
				listOfOrigModels.addLast(reader.getOriginalModel());
			else
				listOfOrigModels.addLast(model);
			selectedModel = listOfModels.size() - 1;
			return listOfModels.getLast();
		} catch (Exception exc) {
			exc.printStackTrace();
			throw new RuntimeException("Could not read model.");
		}
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
	 * 
	 * @throws SBMLException
	 */
	public void saveChanges() throws SBMLException {
		writer.saveChanges(listOfModels.get(selectedModel), listOfOrigModels
				.get(selectedModel));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.jlibsbml.SBMLWriter#saveCompartmentProperties(org.sbml.jlibsbml
	 *      .Compartment, java.lang.Object)
	 */
	public void saveCompartmentProperties(Compartment c, Object comp) {
		writer.saveCompartmentProperties(c, comp);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.jlibsbml.SBMLWriter#saveCVTermProperties(org.sbml.jlibsbml.CVTerm ,
	 *      java.lang.Object)
	 */
	public void saveCVTermProperties(CVTerm cvt, Object term) {
		writer.saveCVTermProperties(cvt, term);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.jlibsbml.SBMLWriter#saveEventProperties(org.sbml.jlibsbml.Event,
	 *      java.lang.Object)
	 */
	public void saveEventProperties(Event r, Object event) throws SBMLException {
		writer.saveEventProperties(r, event);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.jlibsbml.SBMLWriter#saveKineticLawProperties(org.sbml.jlibsbml
	 *      .KineticLaw, java.lang.Object)
	 */
	public void saveKineticLawProperties(KineticLaw kl, Object kineticLaw)
			throws SBMLException {
		writer.saveKineticLawProperties(kl, kineticLaw);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.jlibsbml.SBMLWriter#saveMathContainerProperties(org.sbml.jlibsbml
	 *      .MathContainer, java.lang.Object)
	 */
	public void saveMathContainerProperties(MathContainer mc, Object sbase)
			throws SBMLException {
		writer.saveMathContainerProperties(mc, sbase);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.jlibsbml.SBMLWriter#saveModelHistoryProperties(org.sbml.jlibsbml
	 *      .ModelHistory, java.lang.Object)
	 */
	public void saveModelHistoryProperties(ModelHistory mh, Object modelHistory) {
		writer.saveModelHistoryProperties(mh, modelHistory);
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
	 *      java.lang.Object)
	 */
	public void saveNamedSBaseProperties(NamedSBase nsb, Object sb) {
		writer.saveNamedSBaseProperties(nsb, sb);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.jlibsbml.SBMLWriter#saveParameterProperties(org.sbml.jlibsbml
	 *      .Parameter, java.lang.Object)
	 */
	public void saveParameterProperties(Parameter p, Object parameter) {
		writer.saveParameterProperties(p, parameter);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.jlibsbml.SBMLWriter#saveReactionProperties(org.sbml.jlibsbml
	 *      .Reaction, java.lang.Object)
	 */
	public void saveReactionProperties(Reaction r, Object reaction)
			throws SBMLException {
		writer.saveReactionProperties(r, reaction);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.SBMLWriter#saveSBaseProperties(org.sbml.SBase,
	 *      java.lang.Object)
	 */
	public void saveSBaseProperties(SBase s, Object sb) {
		writer.saveSBaseProperties(s, sb);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.jlibsbml.SBMLWriter#saveSpeciesProperties(org.sbml.jlibsbml.
	 *      Species, java.lang.Object)
	 */
	public void saveSpeciesProperties(Species s, Object species) {
		writer.saveSpeciesProperties(s, species);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.SBaseChangedListener#sbaseAdded(org.sbml.SBase)
	 */
	public void sbaseAdded(SBase sb) {
		if (!added.contains(sb))
			added.add(sb);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.SBaseChangedListener#sbaseRemoved(org.sbml.SBase)
	 */
	public void sbaseRemoved(SBase sb) {
		if (!removed.contains(sb))
			removed.add(sb);
	}

	public void setSelectedModel(int selectedModel) {
		this.selectedModel = selectedModel;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.event.ChangeListener#stateChanged(javax.swing.event.ChangeEvent )
	 */
	public void stateChanged(ChangeEvent e) {
		if (e.getSource() instanceof JTabbedPane) {
			JTabbedPane tabbedPane = (JTabbedPane) e.getSource();
			if (tabbedPane.getComponentCount() == 0) {
				listOfModels.clear();
				listOfOrigModels.clear();
			} else {
				// search for the currently selected model.
				for (Model m : listOfModels)
					if (m != null) {
						boolean contains = false;
						for (int i = 0; i < tabbedPane.getTabCount()
								&& !contains; i++) {
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
	 * @see org.sbml.SBaseChangedListener#stateChanged(org.sbml.SBase)
	 */
	public void stateChanged(SBase sb) {
		if (!changed.contains(sb))
			changed.add(sb);
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
	 * @see org.sbml.jlibsbml.SBMLWriter#writeCVTerm(org.sbml.jlibsbml.CVTerm)
	 */
	public Object writeCVTerm(CVTerm cvt) {
		return writer.writeCVTerm(cvt);
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
	public Object writeEvent(Event event) throws SBMLException {
		return writer.writeEvent(event);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.SBMLWriter#writeEventAssignment(org.sbml.EventAssignment)
	 */
	// @Override
	public Object writeEventAssignment(EventAssignment eventAssignment,
			Object... args) throws SBMLException {
		return writer.writeEventAssignment(eventAssignment, args);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.SBMLWriter#writeFunctionDefinition(org.sbml.FunctionDefinition)
	 */
	// @Override
	public Object writeFunctionDefinition(FunctionDefinition functionDefinition)
			throws SBMLException {
		return writer.writeFunctionDefinition(functionDefinition);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.SBMLWriter#writeInitialAssignment(org.sbml.InitialAssignment)
	 */
	// @Override
	public Object writeInitialAssignment(InitialAssignment initialAssignment)
			throws SBMLException {
		return writer.writeInitialAssignment(initialAssignment);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.SBMLWriter#writeKineticLaw(org.sbml.KineticLaw)
	 */
	public Object writeKineticLaw(KineticLaw kineticLaw, Object... args)
			throws SBMLException {
		return writer.writeKineticLaw(kineticLaw, args);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.SBMLWriter#writeModel(org.sbml.Model)
	 */
	// @Override
	public Object writeModel(Model model) throws SBMLException {
		return writer.writeModel(model);
	}

	/**
	 * 
	 * @param model
	 * @param filename
	 * @return
	 * @throws SBMLException
	 * @throws IOException
	 */
	public boolean writeModelToSBML(int model, String filename)
			throws SBMLException, IOException {
		return writer.writeSBML(listOfOrigModels.get(model), filename);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seeorg.sbml.SBMLWriter#writeModifierSpeciesReference(org.sbml.
	 * ModifierSpeciesReference)
	 */
	public Object writeModifierSpeciesReference(
			ModifierSpeciesReference modifierSpeciesReference, Object... args) {
		return writer.writeModifierSpeciesReference(modifierSpeciesReference,
				args);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.SBMLWriter#writeParameter(org.sbml.Parameter)
	 */
	public Object writeParameter(Parameter parameter, Object... args) {
		return writer.writeParameter(parameter, args);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.SBMLWriter#writeReaction(org.sbml.Reaction)
	 */
	// @Override
	public Object writeReaction(Reaction reaction) throws SBMLException {
		return writer.writeReaction(reaction);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.SBMLWriter#writeRule(org.sbml.Rule)
	 */
	// @Override
	public Object writeRule(Rule rule, Object... args) {
		return writer.writeRule(rule, args);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.SBMLWriter#writeSBML(java.lang.Object, java.lang.String)
	 */
	// @Override
	public boolean writeSBML(Object sbmlDocument, String filename)
			throws SBMLException, IOException {
		return writer.writeSBML(sbmlDocument, filename);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.jsbml.SBMLWriter#writeSBML(java.lang.Object,
	 *      java.lang.String, java.lang.String, java.lang.String)
	 */
	public boolean writeSBML(Object object, String filename,
			String programName, String versionNumber) throws SBMLException,
			IOException {
		return writer.writeSBML(object, filename, programName, versionNumber);
	}

	/**
	 * 
	 * @param filename
	 * @return
	 * @throws SBMLException
	 * @throws IOException
	 */
	public boolean writeSelectedModelToSBML(String filename)
			throws SBMLException, IOException {
		return writer.writeSBML(listOfOrigModels.get(selectedModel), filename,
				SBMLsqueezer.class.getSimpleName(), SBMLsqueezer
						.getVersionNumber());
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
	public Object writeSpeciesReference(SpeciesReference speciesReference,
			Object... args) throws SBMLException {
		return writer.writeSpeciesReference(speciesReference, args);
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
	 * @see org.sbml.SBMLWriter#writeStoichoimetryMath(org.sbml.StoichiometryMath)
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
	public Object writeUnit(Unit unit, Object... args) {
		return writer.writeUnit(unit, args);
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

	/**
	 * 
	 * @param reaction
	 * @throws SBMLException 
	 */
	public void saveChanges(Reaction reaction) throws SBMLException {
		writer.saveChanges(reaction, listOfOrigModels.get(selectedModel));	
	}
}
