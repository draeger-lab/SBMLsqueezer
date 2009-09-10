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
package org.sbml.jlibsbml;

import java.util.Date;

import jp.sbi.sbml.util.SBMLException;

/**
 * @author Andreas Dr&auml;ger <a
 *         href="mailto:andreas.draeger@uni-tuebingen.de">
 *         andreas.draeger@uni-tuebingen.de</a>
 * 
 */
public interface SBMLWriter {
	
	/**
	 * 
	 * @param date
	 * @return
	 */
	public Object convertDate(Date date);
	
	/**
	 * 
	 * @return
	 */
	public int getNumErrors(Object sbase);

	/**
	 * 
	 * @param sbmlDocument
	 * @return
	 */
	public String getWarnings(Object sbmlDocument);
	
	/**
	 * 
	 * @param c
	 * @param comp
	 */
	public void saveCompartmentProperties(Compartment c, Object comp);

	/**
	 * 
	 * @param cvt
	 * @param term
	 */
	public void saveCVTermProperties(CVTerm cvt, Object term);
	
	/**
	 * 
	 * @param r
	 * @param event
	 */
	public void saveEventProperties(Event r, Object event);
	
	/**
	 * 
	 * @param kl
	 * @param kineticLaw
	 */
	public void saveKineticLawProperties(KineticLaw kl, Object kineticLaw);

	/**
	 * 
	 * @param mc
	 * @param sbase
	 */
	public void saveMathContainerProperties(MathContainer mc, Object sbase);

	/**
	 * 
	 * @param mh
	 * @param modelHistory
	 */
	public void saveModelHistoryProperties(ModelHistory mh, Object modelHistory);

	/**
	 * 
	 * @param modifierSpeciesReference
	 * @param msr
	 */
	public void saveModifierSpeciesReferenceProperties(
			ModifierSpeciesReference modifierSpeciesReference, Object msr);

	/**
	 * 
	 * @param nsb
	 * @param sb
	 */
	public void saveNamedSBaseProperties(NamedSBase nsb, Object sb);

	/**
	 * 
	 * @param p
	 * @param parameter
	 */
	public void saveParameterProperties(Parameter p, Object parameter);

	/**
	 * 
	 * @param r
	 * @param reaction
	 */
	public void saveReactionProperties(Reaction r, Object reaction);

	/**
	 * 
	 * @param s
	 * @param sb
	 */
	public void saveSBaseProperties(SBase s, Object sb);

	/**
	 * 
	 * @param s
	 * @param species
	 */
	public void saveSpeciesProperties(Species s, Object species);

	/**
	 * 
	 * @param compartment
	 * @return
	 */
	public Object writeCompartment(Compartment compartment);

	/**
	 * 
	 * @param compartmentType
	 * @return
	 */
	public Object writeCompartmentType(CompartmentType compartmentType);

	/**
	 * 
	 * @param constraint
	 * @return
	 */
	public Object writeConstraint(Constraint constraint);

	/**
	 * 
	 * @param cvt
	 * @return
	 */
	public Object writeCVTerm(CVTerm cvt);

	/**
	 * 
	 * @param delay
	 * @return
	 */
	public Object writeDelay(Delay delay);

	/**
	 * 
	 * @param event
	 * @return
	 */
	public Object writeEvent(Event event);

	/**
	 * 
	 * @param ea
	 * @return
	 */
	public Object writeEventAssignment(EventAssignment eventAssignment);

	/**
	 * 
	 * @param functionDefinition
	 * @return
	 */
	public Object writeFunctionDefinition(FunctionDefinition functionDefinition);

	/**
	 * 
	 * @param initialAssignment
	 * @return
	 */
	public Object writeInitialAssignment(InitialAssignment initialAssignment);

	/**
	 * 
	 * @param kineticLaw
	 * @return
	 */
	public Object writeKineticLaw(KineticLaw kineticLaw);

	/**
	 * 
	 * @param model
	 * @return
	 */
	public Object writeModel(Model model);

	/**
	 * 
	 * @param modifierSpeciesReference
	 * @return
	 */
	public Object writeModifierSpeciesReference(
			ModifierSpeciesReference modifierSpeciesReference);

	/**
	 * 
	 * @param parameter
	 * @return
	 */
	public Object writeParameter(Parameter parameter);

	/**
	 * 
	 * @param reaction
	 * @return
	 */
	public Object writeReaction(Reaction reaction);

	/**
	 * 
	 * @param rule
	 * @return
	 */
	public Object writeRule(Rule rule);

	/**
	 * 
	 * @param sbmlDocument
	 * @param filename
	 * @return
	 * @throws SBMLException 
	 */
	public boolean writeSBML(Object sbmlDocument, String filename) throws SBMLException;

	/**
	 * 
	 * @param species
	 * @return
	 */
	public Object writeSpecies(Species species);

	/**
	 * 
	 * @param speciesReference
	 * @return
	 */
	public Object writeSpeciesReference(SpeciesReference speciesReference);

	/**
	 * 
	 * @param speciesType
	 * @return
	 */
	public Object writeSpeciesType(SpeciesType speciesType);

	/**
	 * 
	 * @param stoichiometryMath
	 * @return
	 */
	public Object writeStoichoimetryMath(StoichiometryMath stoichiometryMath);

	/**
	 * 
	 * @param trigger
	 * @return
	 */
	public Object writeTrigger(Trigger trigger);

	/**
	 * 
	 * @param unit
	 * @return
	 */
	public Object writeUnit(Unit unit);

	/**
	 * 
	 * @param unitDefinition
	 * @return
	 */
	public Object writeUnitDefinition(UnitDefinition unitDefinition);
}
