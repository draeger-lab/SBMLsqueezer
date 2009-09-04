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
package org.sbml.squeezer.plugin;

import org.sbml.Compartment;
import org.sbml.CompartmentType;
import org.sbml.Constraint;
import org.sbml.Delay;
import org.sbml.Event;
import org.sbml.EventAssignment;
import org.sbml.FunctionDefinition;
import org.sbml.InitialAssignment;
import org.sbml.KineticLaw;
import org.sbml.Model;
import org.sbml.ModifierSpeciesReference;
import org.sbml.NamedSBase;
import org.sbml.Parameter;
import org.sbml.Reaction;
import org.sbml.Rule;
import org.sbml.SBase;
import org.sbml.Species;
import org.sbml.SpeciesReference;
import org.sbml.SpeciesType;
import org.sbml.StoichiometryMath;
import org.sbml.Trigger;
import org.sbml.Unit;
import org.sbml.UnitDefinition;
import org.sbml.squeezer.io.AbstractSBMLWriter;

/**
 * @author Andreas Dr&auml;ger <a
 *         href="mailto:andreas.draeger@uni-tuebingen.de">
 *         andreas.draeger@uni-tuebingen.de</a>
 * 
 */
public class PluginSBMLWriter extends AbstractSBMLWriter {

	/**
	 * 
	 * @param plugin
	 */
	public PluginSBMLWriter(SBMLsqueezerPlugin plugin) {
		// TODO Auto-generated constructor stub
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.SBMLWriter#writeModel(org.sbml.Model)
	 */
	public Object writeModel(Model model) {
		// TODO Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.SBMLWriter#writeReaction(org.sbml.Reaction)
	 */
	public Object writeReaction(Reaction reaction) {
		// TODO Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.squeezer.io.AbstractSBMLWriter#saveChanges(org.sbml.Model,
	 * java.lang.Object)
	 */
	// @Override
	public void saveChanges(Model model, Object object) {
		// TODO Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.sbml.squeezer.io.AbstractSBMLWriter#saveSpeciesReferenceProperties
	 * (org.sbml.SpeciesReference, java.lang.Object)
	 */
	// @Override
	public void saveSpeciesReferenceProperties(SpeciesReference sr,
			Object specRef) {
		// TODO Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.SBMLWriter#saveNamedSBaseProperties(org.sbml.NamedSBase,
	 * java.lang.Object)
	 */
	public void saveNamedSBaseProperties(NamedSBase nsb, Object sb) {
		// TODO Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.SBMLWriter#saveSBaseProperties(org.sbml.SBase,
	 * java.lang.Object)
	 */
	public void saveSBaseProperties(SBase s, Object sb) {
		// TODO Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.SBMLWriter#writeKineticLaw(org.sbml.KineticLaw)
	 */
	public Object writeKineticLaw(KineticLaw kineticLaw) {
		// TODO Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.SBMLWriter#writeParameter(org.sbml.Parameter)
	 */
	public Object writeParameter(Parameter parameter) {
		// TODO Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.SBMLWriter#writeSpeciesReference(org.sbml.SpeciesReference)
	 */
	public Object writeSpeciesReference(SpeciesReference speciesReference) {
		// TODO Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.sbml.SBMLWriter#writeStoichoimetryMath(org.sbml.StoichiometryMath)
	 */
	public Object writeStoichoimetryMath(StoichiometryMath stoichiometryMath) {
		// TODO Auto-generated method stub
		return null;
	}

	public void saveModifierSpeciesReferenceProperties(
			ModifierSpeciesReference modifierSpeciesReference, Object msr) {
		// TODO Auto-generated method stub
		
	}

	public Object writeModifierSpeciesReference(
			ModifierSpeciesReference modifierSpeciesReference) {
		// TODO Auto-generated method stub
		return null;
	}

	public boolean writeSBML(Object sbmlDocument, String filename) {
		// TODO Auto-generated method stub
		return false;
	}

	public Object writeCompartment(Compartment compartment) {
		// TODO Auto-generated method stub
		return null;
	}

	public Object writeCompartmentType(CompartmentType compartmentType) {
		// TODO Auto-generated method stub
		return null;
	}

	public Object writeConstraint(Constraint constraint) {
		// TODO Auto-generated method stub
		return null;
	}

	public Object writeDelay(Delay delay) {
		// TODO Auto-generated method stub
		return null;
	}

	public Object writeEvent(Event event) {
		// TODO Auto-generated method stub
		return null;
	}

	public Object writeEventAssignment(EventAssignment eventAssignment) {
		// TODO Auto-generated method stub
		return null;
	}

	public Object writeFunctionDefinition(FunctionDefinition functionDefinition) {
		// TODO Auto-generated method stub
		return null;
	}

	public Object writeInitialAssignment(InitialAssignment initialAssignment) {
		// TODO Auto-generated method stub
		return null;
	}

	public Object writeRule(Rule rule) {
		// TODO Auto-generated method stub
		return null;
	}

	public Object writeSpecies(Species species) {
		// TODO Auto-generated method stub
		return null;
	}

	public Object writeSpeciesType(SpeciesType speciesType) {
		// TODO Auto-generated method stub
		return null;
	}

	public Object writeTrigger(Trigger trigger) {
		// TODO Auto-generated method stub
		return null;
	}

	public Object writeUnit(Unit unit) {
		// TODO Auto-generated method stub
		return null;
	}

	public Object writeUnitDefinition(UnitDefinition unitDefinition) {
		// TODO Auto-generated method stub
		return null;
	}

}
