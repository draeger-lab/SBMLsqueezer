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
package org.sbml.squeezer.standalone;

import org.sbml.AssignmentRule;
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
import org.sbml.RateRule;
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
import org.sbml.libsbml.SBMLWriter;
import org.sbml.libsbml.libsbmlConstants;
import org.sbml.squeezer.io.AbstractSBMLWriter;

/**
 * @author Andreas Dr&auml;ger <a
 *         href="mailto:andreas.draeger@uni-tuebingen.de">
 *         andreas.draeger@uni-tuebingen.de</a>
 * 
 */
public class LibSBMLWriter extends AbstractSBMLWriter {

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.squeezer.io.AbstractSBMLWriter#saveChanges(org.sbml.Model,
	 * java.lang.Object)
	 */
	// @Override
	public void saveChanges(Model model, Object orig) {
		if (!(orig instanceof org.sbml.libsbml.Model))
			throw new IllegalArgumentException(
					"only instances of org.sbml.libsbml.Model can be considered.");
		org.sbml.libsbml.Model mo = (org.sbml.libsbml.Model) orig;
		long i;
		// Unit definitions
		for (UnitDefinition ud : model.getListOfUnitDefinitions())
			if (!ud.equals(UnitDefinition.SUBSTANCE)
					&& !ud.equals(UnitDefinition.VOLUME)
					&& !ud.equals(UnitDefinition.AREA)
					&& !ud.equals(UnitDefinition.LENGTH)
					&& !ud.equals(UnitDefinition.TIME)) {
				org.sbml.libsbml.UnitDefinition libU = mo.getUnitDefinition(ud
						.getId());
				if (libU != null) {
					saveNamedSBaseProperties(ud, libU);
					for (Unit u : ud.getListOfUnits()) {
						// TODO
					}
				}
			}
		// remove unnecessary units
		for (i = mo.getNumUnitDefinitions() - 1; i >= 0; i--) {
			org.sbml.libsbml.UnitDefinition ud = mo.getUnitDefinition(i);
			if (model.getUnitDefinition(ud.getId()) == null)
				mo.getListOfUnitDefinitions().remove(i);
		}
		// Compartments
		for (Compartment c : model.getListOfCompartments()) {
			// TODO
		}
		// remove unnecessary compartments
		for (i = mo.getNumCompartments() - 1; i >= 0; i--) {
			org.sbml.libsbml.Compartment c = mo.getCompartment(i);
			if (model.getCompartment(c.getId()) == null)
				mo.getListOfCompartments().remove(i);
		}
		// Species
		for (Species s : model.getListOfSpecies()) {
			// TODO
		}
		// remove unnecessary species
		for (i = mo.getNumSpecies() - 1; i >= 0; i--) {
			org.sbml.libsbml.Species s = mo.getSpecies(i);
			if (model.getSpecies(s.getId()) == null)
				mo.getListOfSpecies().remove(i);
		}
		// add or change parameters
		for (Parameter p : model.getListOfParameters()) {
			if (mo.getParameter(p.getId()) == null)
				mo.addParameter((org.sbml.libsbml.Parameter) writeParameter(p));
			else {
				org.sbml.libsbml.Parameter po = (org.sbml.libsbml.Parameter) mo
						.getParameter(p.getId());
				if (p.getValue() != po.getValue())
					po.setValue(p.getValue());
				saveNamedSBaseProperties(p, po);
				if (p.getConstant() != po.getConstant())
					po.setConstant(p.getConstant());
				if (!p.getUnits().equals(po.getUnits()))
					po.setUnits(p.getUnits());
			}
		}
		// remove parameters
		for (i = mo.getNumParameters() - 1; i >= 0; i--) {
			org.sbml.libsbml.Parameter p = mo.getParameter(i);
			if (model.getParameter(p.getId()) == null)
				mo.getListOfParameters().remove(i);
		}
		// add or change reactions
		for (Reaction r : model.getListOfReactions()) {
			if (mo.getReaction(r.getId()) == null)
				mo.addReaction((org.sbml.libsbml.Reaction) writeReaction(r));
			else {
				org.sbml.libsbml.Reaction ro = (org.sbml.libsbml.Reaction) mo
						.getReaction(r.getId());
				saveNamedSBaseProperties(r, ro);
				if (r.getFast() != ro.getFast())
					ro.setFast(r.getFast());
				// TODO
				if ((ro.getKineticLaw() == null && r.isSetKineticLaw())
						|| (r.isSetKineticLaw() && ro.isSetKineticLaw() && r
								.getKineticLaw().getSBOTerm() != ro
								.getKineticLaw().getSBOTerm())) {
					ro
							.setKineticLaw((org.sbml.libsbml.KineticLaw) writeKineticLaw(r
									.getKineticLaw()));
				}
				long contains;
				for (SpeciesReference sr : r.getListOfReactants()) {
					contains = -1;
					for (i = 0; i < ro.getNumReactants() && contains < 0; i++)
						if (sr.getSpecies().equals(ro.getReactant(i)))
							contains = i;
					if (contains < 0)
						ro
								.addReactant((org.sbml.libsbml.SpeciesReference) writeSpeciesReference(sr));
					else
						saveSpeciesReferenceProperties(sr, ro
								.getReactant(contains));
				}
				// remove unnecessary reactants.
				for (i = ro.getNumReactants() - 1; i >= 0; i--) {
					org.sbml.libsbml.SpeciesReference msr = ro.getReactant(i);
					boolean keep = false;
					for (int j = 0; j < r.getNumReactants() && !keep; j++)
						if (r.getReactant(j).getSpecies().equals(
								msr.getSpecies()))
							keep = true;
					if (!keep)
						ro.getListOfReactants().remove(i);
				}
				for (SpeciesReference sr : r.getListOfProducts()) {
					contains = -1;
					for (i = 0; i < ro.getNumProducts() && contains < 0; i++)
						if (sr.getSpecies().equals(ro.getProduct(i)))
							contains = i;
					if (contains < 0)
						ro
								.addProduct((org.sbml.libsbml.SpeciesReference) writeSpeciesReference(sr));
					else
						saveSpeciesReferenceProperties(sr, ro
								.getProduct(contains));
				}
				// remove unnecessary products.
				for (i = ro.getNumProducts() - 1; i >= 0; i--) {
					org.sbml.libsbml.SpeciesReference msr = ro.getProduct(i);
					boolean keep = false;
					for (int j = 0; j < r.getNumProducts() && !keep; j++)
						if (r.getProduct(j).getSpecies().equals(
								msr.getSpecies()))
							keep = true;
					if (!keep)
						ro.getListOfProducts().remove(i);
				}
				// check modifiers
				for (ModifierSpeciesReference mr : r.getListOfModifiers()) {
					contains = -1;
					for (i = 0; i < ro.getNumModifiers() && contains < 0; i++)
						if (mr.getSpecies().equals(ro.getModifier(i)))
							contains = i;
					if (contains < 0)
						ro
								.addModifier((org.sbml.libsbml.ModifierSpeciesReference) writeModifierSpeciesReference(mr));
					else
						saveModifierSpeciesReferenceProperties(mr, ro
								.getModifier(contains));
				}
				// remove unnecessary modifiers.
				for (i = ro.getNumModifiers() - 1; i >= 0; i--) {
					org.sbml.libsbml.ModifierSpeciesReference msr = ro
							.getModifier(i);
					boolean keep = false;
					for (int j = 0; j < r.getNumModifiers() && !keep; j++)
						if (r.getModifier(j).getSpecies().equals(
								msr.getSpecies()))
							keep = true;
					if (!keep)
						ro.getListOfModifiers().remove(i);
				}
			}
		}
		// remove reactions
		for (i = mo.getNumReactions() - 1; i >= 0; i--) {
			org.sbml.libsbml.Reaction r = mo.getReaction(i);
			if (model.getReaction(r.getId()) == null)
				mo.getListOfReactions().remove(i);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seeorg.sbml.SBMLWriter#saveModifierSpeciesReferenceProperties(org.sbml.
	 * ModifierSpeciesReference, java.lang.Object)
	 */
	// @Override
	public void saveModifierSpeciesReferenceProperties(
			ModifierSpeciesReference modifierSpeciesReference, Object msr) {
		if (!(msr instanceof org.sbml.libsbml.ModifierSpeciesReference))
			throw new IllegalArgumentException(
					"msr must be an instance of org.sbml.libsbml.ModifierSpeciesReference.");
		saveNamedSBaseProperties(modifierSpeciesReference,
				(org.sbml.libsbml.ModifierSpeciesReference) msr);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.SBMLWriter#saveNamedSBaseProperties(org.sbml.NamedSBase,
	 * java.lang.Object)
	 */
	// @Override
	public void saveNamedSBaseProperties(NamedSBase nsb, Object sb) {
		if (!(sb instanceof org.sbml.libsbml.SBase))
			throw new IllegalArgumentException(
					"sb must be an instance of org.sbml.libsbml.SBase.");
		saveSBaseProperties(nsb, sb);
		org.sbml.libsbml.SBase po = (org.sbml.libsbml.SBase) sb;
		if (!nsb.getId().equals(po.getId()))
			po.setId(nsb.getId());
		if (!nsb.getName().equals(po.getName()))
			po.setName(nsb.getName());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.SBMLWriter#saveSBaseProperties(org.sbml.SBase,
	 * java.lang.Object)
	 */
	// @Override
	public void saveSBaseProperties(SBase s, Object sb) {
		if (!(sb instanceof org.sbml.libsbml.SBase))
			throw new IllegalArgumentException(
					"sb must be an instance of org.sbml.libsbml.SBase.");
		org.sbml.libsbml.SBase po = (org.sbml.libsbml.SBase) sb;
		if (s.getMetaId() != po.getMetaId())
			po.setMetaId(s.getMetaId());
		if (!s.getNotesString().equals(po.getNotesString()))
			po.setNotes(s.getNotesString());
		if (s.getSBOTerm() != po.getSBOTerm())
			po.setSBOTerm(s.getSBOTerm());
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
		if (!(specRef instanceof org.sbml.libsbml.SpeciesReference))
			throw new IllegalArgumentException(
					"specRef must be an instance of org.sbml.libsbml.SpeciesReference.");
		org.sbml.libsbml.SpeciesReference sp = (org.sbml.libsbml.SpeciesReference) specRef;
		saveNamedSBaseProperties(sr, sp);
		if (!sr.getSpecies().equals(sp.getSpecies()))
			sp.setSpecies(sr.getSpecies());
		if (sr.isSetStoichiometryMath())
			sp
					.setStoichiometryMath((org.sbml.libsbml.StoichiometryMath) writeStoichoimetryMath(sr
							.getStoichiometryMath()));
		else
			sp.setStoichiometry(sr.getStoichiometry());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.SBMLWriter#writeCompartment(org.sbml.Compartment)
	 */
	// @Override
	public Object writeCompartment(Compartment compartment) {
		org.sbml.libsbml.Compartment c = new org.sbml.libsbml.Compartment();
		saveNamedSBaseProperties(compartment, c);
		if (compartment.isSetCompartmentType())
			c.setCompartmentType(compartment.getCompartmentType());
		if (compartment.isSetOutside())
			c.setOutside(compartment.getOutside());
		if (compartment.isSetSize())
			c.setSize(compartment.getSize());
		if (compartment.isSetUnits())
			c.setUnits(compartment.getUnits());
		c.setConstant(compartment.getConstant());
		c.setSpatialDimensions(compartment.getSpatialDimensions());
		return c;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.SBMLWriter#writeCompartmentType(org.sbml.CompartmentType)
	 */
	// @Override
	public Object writeCompartmentType(CompartmentType compartmentType) {
		org.sbml.libsbml.CompartmentType ct = new org.sbml.libsbml.CompartmentType();
		saveNamedSBaseProperties(compartmentType, ct);
		return ct;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.SBMLWriter#writeConstraint(org.sbml.Constraint)
	 */
	// @Override
	public Object writeConstraint(Constraint constraint) {
		org.sbml.libsbml.Constraint c = new org.sbml.libsbml.Constraint();
		saveSBaseProperties(constraint, c);
		if (constraint.isSetMath())
			c.setMath(convert(constraint.getMath()));
		if (constraint.isSetMessage())
			c.setMessage(new org.sbml.libsbml.XMLNode(constraint.getMessage()));
		return c;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.SBMLWriter#writeDelay(org.sbml.Delay)
	 */
	// @Override
	public Object writeDelay(Delay delay) {
		org.sbml.libsbml.Delay d = new org.sbml.libsbml.Delay();
		saveSBaseProperties(delay, d);
		if (delay.isSetMath())
			d.setMath(convert(delay.getMath()));
		return d;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.SBMLWriter#writeEvent(org.sbml.Event)
	 */
	// @Override
	public Object writeEvent(Event event) {
		org.sbml.libsbml.Event e = new org.sbml.libsbml.Event();
		saveNamedSBaseProperties(event, e);
		if (event.isSetDelay())
			e.setDelay((org.sbml.libsbml.Delay) writeDelay(event.getDelay()));
		for (EventAssignment ea : event.getListOfEventAssignments())
			e
					.addEventAssignment((org.sbml.libsbml.EventAssignment) writeEventAssignment(ea));
		if (event.isSetTimeUnits())
			e.setTimeUnits(event.getTimeUnits());
		if (e.isSetTrigger())
			e.setTrigger((org.sbml.libsbml.Trigger) writeTrigger(event
					.getTrigger()));
		e.setUseValuesFromTriggerTime(event.getUseValuesFromTriggerTime());
		return e;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.SBMLWriter#writeEventAssignment(org.sbml.EventAssignment)
	 */
	// @Override
	public Object writeEventAssignment(EventAssignment eventAssignment) {
		org.sbml.libsbml.EventAssignment ea = new org.sbml.libsbml.EventAssignment();
		saveSBaseProperties(eventAssignment, ea);
		if (eventAssignment.isSetMath())
			ea.setMath(convert(eventAssignment.getMath()));
		if (eventAssignment.isSetVariable())
			ea.setVariable(eventAssignment.getVariable());
		return ea;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.sbml.SBMLWriter#writeFunctionDefinition(org.sbml.FunctionDefinition)
	 */
	// @Override
	public Object writeFunctionDefinition(FunctionDefinition functionDefinition) {
		org.sbml.libsbml.FunctionDefinition fd = new org.sbml.libsbml.FunctionDefinition();
		saveNamedSBaseProperties(functionDefinition, fd);
		if (functionDefinition.isSetMath())
			fd.setMath(convert(functionDefinition.getMath()));
		return fd;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.sbml.SBMLWriter#writeInitialAssignment(org.sbml.InitialAssignment)
	 */
	// @Override
	public Object writeInitialAssignment(InitialAssignment initialAssignment) {
		org.sbml.libsbml.InitialAssignment ia = new org.sbml.libsbml.InitialAssignment();
		saveSBaseProperties(initialAssignment, ia);
		if (initialAssignment.isSetMath())
			ia.setMath(convert(initialAssignment.getMath()));
		if (initialAssignment.isSetSymbol())
			ia.setSymbol(initialAssignment.getSymbol());
		return ia;
	}

	public Object writeKineticLaw(KineticLaw kl) {
		org.sbml.libsbml.KineticLaw k = new org.sbml.libsbml.KineticLaw();
		saveSBaseProperties(kl, k);
		if (kl.isSetMath())
			k.setMath(convert(kl.getMath()));
		for (Parameter p : kl.getListOfParameters())
			k.addParameter((org.sbml.libsbml.Parameter) writeParameter(p));
		return k;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.SBMLWriter#writeModel(org.sbml.Model)
	 */
	public Object writeModel(Model model) {
		org.sbml.libsbml.Model m = new org.sbml.libsbml.Model();
		saveNamedSBaseProperties(model, m);
		for (UnitDefinition ud : model.getListOfUnitDefinitions())
			m
					.addUnitDefinition((org.sbml.libsbml.UnitDefinition) writeUnitDefinition(ud));
		for (FunctionDefinition fd : model.getListOfFunctionDefinitions())
			m
					.addFunctionDefinition((org.sbml.libsbml.FunctionDefinition) writeFunctionDefinition(fd));
		for (CompartmentType ct : model.getListOfCompartmentTypes())
			m
					.addCompartmentType((org.sbml.libsbml.CompartmentType) writeCompartmentType(ct));
		for (SpeciesType st : model.getListOfSpeciesTypes())
			m
					.addSpeciesType((org.sbml.libsbml.SpeciesType) writeSpeciesType(st));
		for (Compartment c : model.getListOfCompartments())
			m
					.addCompartment((org.sbml.libsbml.Compartment) writeCompartment(c));
		for (Species s : model.getListOfSpecies())
			m.addSpecies((org.sbml.libsbml.Species) writeSpecies(s));
		for (Parameter p : model.getListOfParameters())
			m.addParameter((org.sbml.libsbml.Parameter) writeParameter(p));
		for (Constraint c : model.getListOfConstraints())
			m.addConstraint((org.sbml.libsbml.Constraint) writeConstraint(c));
		for (InitialAssignment ia : model.getListOfInitialAssignments())
			m
					.addInitialAssignment((org.sbml.libsbml.InitialAssignment) writeInitialAssignment(ia));
		for (Rule r : model.getListOfRules())
			m.addRule((org.sbml.libsbml.Rule) writeRule(r));
		for (Reaction r : model.getListOfReactions())
			m.addReaction((org.sbml.libsbml.Reaction) writeReaction(r));
		for (Event e : model.getListOfEvents())
			m.addEvent((org.sbml.libsbml.Event) writeEvent(e));
		return m;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seeorg.sbml.SBMLWriter#writeModifierSpeciesReference(org.sbml.
	 * ModifierSpeciesReference)
	 */
	// @Override
	public Object writeModifierSpeciesReference(
			ModifierSpeciesReference modifierSpeciesReference) {
		org.sbml.libsbml.ModifierSpeciesReference m = new org.sbml.libsbml.ModifierSpeciesReference();
		saveNamedSBaseProperties(modifierSpeciesReference, m);
		if (modifierSpeciesReference.isSetSpecies())
			m.setSpecies(modifierSpeciesReference.getSpecies());
		return m;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.SBMLWriter#writeParameter(org.sbml.Parameter)
	 */
	// @Override
	public Object writeParameter(Parameter parameter) {
		org.sbml.libsbml.Parameter p = new org.sbml.libsbml.Parameter(parameter
				.getId());
		saveNamedSBaseProperties(parameter, p);
		p.setConstant(parameter.getConstant());
		p.setUnits(parameter.getUnits());
		p.setValue(parameter.getValue());
		return p;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.SBMLWriter#writeReaction(org.sbml.Reaction)
	 */
	public Object writeReaction(Reaction reaction) {
		org.sbml.libsbml.Reaction r = new org.sbml.libsbml.Reaction();
		saveNamedSBaseProperties(reaction, r);
		r.setFast(reaction.getFast());
		r.setReversible(reaction.getReversible());
		r.setKineticLaw((org.sbml.libsbml.KineticLaw) writeKineticLaw(reaction
				.getKineticLaw()));
		for (SpeciesReference sr : reaction.getListOfReactants())
			r
					.addReactant((org.sbml.libsbml.SpeciesReference) writeSpeciesReference(sr));
		for (SpeciesReference sr : reaction.getListOfProducts())
			r
					.addProduct((org.sbml.libsbml.SpeciesReference) writeSpeciesReference(sr));
		for (ModifierSpeciesReference mr : reaction.getListOfModifiers())
			r
					.addModifier((org.sbml.libsbml.ModifierSpeciesReference) writeModifierSpeciesReference(mr));
		return r;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.SBMLWriter#writeRule(org.sbml.Rule)
	 */
	// @Override
	public Object writeRule(Rule rule) {
		org.sbml.libsbml.Rule r;
		if (rule.isAlgebraic())
			r = new org.sbml.libsbml.AlgebraicRule();
		else {
			if (rule.isAssignment()) {
				r = new org.sbml.libsbml.AssignmentRule();
				if (((AssignmentRule) rule).isSetVariable())
					r.setVariable(((AssignmentRule) rule).getVariable());
			} else {
				r = new org.sbml.libsbml.RateRule();
				if (((RateRule) rule).isSetVariable())
					r.setVariable(((RateRule) rule).getVariable());
			}
		}
		if (rule.isSetMath())
			r.setMath(convert(rule.getMath()));
		saveSBaseProperties(rule, r);
		return r;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.SBMLWriter#writeSBML(java.lang.Object, java.lang.String)
	 */
	// @Override
	public boolean writeSBML(Object sbmlDocument, String filename) {
		if (!(sbmlDocument instanceof org.sbml.libsbml.SBMLDocument)
				&& !(sbmlDocument instanceof org.sbml.libsbml.Model))
			throw new IllegalArgumentException("");
		org.sbml.libsbml.SBMLDocument d;
		if (sbmlDocument instanceof org.sbml.libsbml.SBMLDocument)
			d = (org.sbml.libsbml.SBMLDocument) sbmlDocument;
		else
			d = ((org.sbml.libsbml.Model) sbmlDocument).getSBMLDocument();
		return (new SBMLWriter()).writeSBML(d, filename);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.SBMLWriter#writeSpecies(org.sbml.Species)
	 */
	// @Override
	public Object writeSpecies(Species species) {
		org.sbml.libsbml.Species s = new org.sbml.libsbml.Species();
		saveNamedSBaseProperties(species, s);
		s.setBoundaryCondition(species.getBoundaryCondition());
		s.setCharge(species.getCharge());
		s.setCompartment(species.getCompartment());
		s.setConstant(species.getConstant());
		s.setHasOnlySubstanceUnits(species.getHasOnlySubstanceUnits());
		if (species.isSetInitialAmount())
			s.setInitialAmount(species.getInitialAmount());
		else if (species.isSetInitialConcentration())
			s.setInitialConcentration(species.getInitialConcentration());
		if (species.isSetSpeciesType())
			s.setSpeciesType(species.getSpeciesType());
		if (species.isSetSubstanceUnits())
			species.getSubstanceUnits();
		return s;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.SBMLWriter#writeSpeciesReference(org.sbml.SpeciesReference)
	 */
	// @Override
	public Object writeSpeciesReference(SpeciesReference speciesReference) {
		org.sbml.libsbml.SpeciesReference sr = new org.sbml.libsbml.SpeciesReference();
		saveNamedSBaseProperties(speciesReference, sr);
		if (speciesReference.isSetSpecies())
			sr.setSpecies(speciesReference.getSpecies());
		if (speciesReference.isSetStoichiometryMath())
			sr
					.setStoichiometryMath((org.sbml.libsbml.StoichiometryMath) writeStoichoimetryMath(speciesReference
							.getStoichiometryMath()));
		else
			sr.setStoichiometry(speciesReference.getStoichiometry());
		return sr;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.SBMLWriter#writeSpeciesType(org.sbml.SpeciesType)
	 */
	// @Override
	public Object writeSpeciesType(SpeciesType speciesType) {
		org.sbml.libsbml.SpeciesType st = new org.sbml.libsbml.SpeciesType();
		saveNamedSBaseProperties(speciesType, st);
		return st;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.sbml.SBMLWriter#writeStoichoimetryMath(org.sbml.StoichiometryMath)
	 */
	// @Override
	public Object writeStoichoimetryMath(StoichiometryMath stoichiometryMath) {
		org.sbml.libsbml.StoichiometryMath sm = new org.sbml.libsbml.StoichiometryMath();
		saveSBaseProperties(stoichiometryMath, sm);
		if (stoichiometryMath.isSetMath())
			sm.setMath(convert(stoichiometryMath.getMath()));
		return sm;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.SBMLWriter#writeTrigger(org.sbml.Trigger)
	 */
	// @Override
	public Object writeTrigger(Trigger trigger) {
		org.sbml.libsbml.Trigger t = new org.sbml.libsbml.Trigger();
		saveSBaseProperties(trigger, t);
		if (trigger.isSetMath())
			t.setMath(convert(trigger.getMath()));
		return t;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.SBMLWriter#writeUnit(org.sbml.Unit)
	 */
	// @Override
	public Object writeUnit(Unit unit) {
		org.sbml.libsbml.Unit u = new org.sbml.libsbml.Unit();
		saveSBaseProperties(unit, u);
		switch (unit.getKind()) {
		case UNIT_KIND_AMPERE:
			u.setKind(libsbmlConstants.UNIT_KIND_AMPERE);
			break;
		case UNIT_KIND_BECQUEREL:
			u.setKind(libsbmlConstants.UNIT_KIND_BECQUEREL);
			break;
		case UNIT_KIND_CANDELA:
			u.setKind(libsbmlConstants.UNIT_KIND_CANDELA);
			break;
		case UNIT_KIND_CELSIUS:
			u.setKind(libsbmlConstants.UNIT_KIND_CELSIUS);
			break;
		case UNIT_KIND_COULOMB:
			u.setKind(libsbmlConstants.UNIT_KIND_COULOMB);
			break;
		case UNIT_KIND_DIMENSIONLESS:
			u.setKind(libsbmlConstants.UNIT_KIND_DIMENSIONLESS);
			break;
		case UNIT_KIND_FARAD:
			u.setKind(libsbmlConstants.UNIT_KIND_FARAD);
			break;
		case UNIT_KIND_GRAM:
			u.setKind(libsbmlConstants.UNIT_KIND_GRAM);
			break;
		case UNIT_KIND_GRAY:
			u.setKind(libsbmlConstants.UNIT_KIND_GRAY);
			break;
		case UNIT_KIND_HENRY:
			u.setKind(libsbmlConstants.UNIT_KIND_HENRY);
			break;
		case UNIT_KIND_HERTZ:
			u.setKind(libsbmlConstants.UNIT_KIND_HERTZ);
			break;
		case UNIT_KIND_INVALID:
			u.setKind(libsbmlConstants.UNIT_KIND_INVALID);
			break;
		case UNIT_KIND_ITEM:
			u.setKind(libsbmlConstants.UNIT_KIND_ITEM);
			break;
		case UNIT_KIND_JOULE:
			u.setKind(libsbmlConstants.UNIT_KIND_JOULE);
			break;
		case UNIT_KIND_KATAL:
			u.setKind(libsbmlConstants.UNIT_KIND_KATAL);
			break;
		case UNIT_KIND_KELVIN:
			u.setKind(libsbmlConstants.UNIT_KIND_KELVIN);
			break;
		case UNIT_KIND_KILOGRAM:
			u.setKind(libsbmlConstants.UNIT_KIND_KILOGRAM);
			break;
		case UNIT_KIND_LITER:
			u.setKind(libsbmlConstants.UNIT_KIND_LITER);
			break;
		case UNIT_KIND_LITRE:
			u.setKind(libsbmlConstants.UNIT_KIND_LITRE);
			break;
		case UNIT_KIND_LUMEN:
			u.setKind(libsbmlConstants.UNIT_KIND_LUMEN);
			break;
		case UNIT_KIND_LUX:
			u.setKind(libsbmlConstants.UNIT_KIND_LUX);
			break;
		case UNIT_KIND_METER:
			u.setKind(libsbmlConstants.UNIT_KIND_METER);
			break;
		case UNIT_KIND_METRE:
			u.setKind(libsbmlConstants.UNIT_KIND_METRE);
			break;
		case UNIT_KIND_MOLE:
			u.setKind(libsbmlConstants.UNIT_KIND_MOLE);
			break;
		case UNIT_KIND_NEWTON:
			u.setKind(libsbmlConstants.UNIT_KIND_NEWTON);
			break;
		case UNIT_KIND_OHM:
			u.setKind(libsbmlConstants.UNIT_KIND_OHM);
			break;
		case UNIT_KIND_PASCAL:
			u.setKind(libsbmlConstants.UNIT_KIND_PASCAL);
			break;
		case UNIT_KIND_RADIAN:
			u.setKind(libsbmlConstants.UNIT_KIND_RADIAN);
			break;
		case UNIT_KIND_SECOND:
			u.setKind(libsbmlConstants.UNIT_KIND_SECOND);
			break;
		case UNIT_KIND_SIEMENS:
			u.setKind(libsbmlConstants.UNIT_KIND_SIEMENS);
			break;
		case UNIT_KIND_SIEVERT:
			u.setKind(libsbmlConstants.UNIT_KIND_SIEVERT);
			break;
		case UNIT_KIND_STERADIAN:
			u.setKind(libsbmlConstants.UNIT_KIND_STERADIAN);
			break;
		case UNIT_KIND_TESLA:
			u.setKind(libsbmlConstants.UNIT_KIND_TESLA);
			break;
		case UNIT_KIND_VOLT:
			u.setKind(libsbmlConstants.UNIT_KIND_VOLT);
			break;
		case UNIT_KIND_WATT:
			u.setKind(libsbmlConstants.UNIT_KIND_WATT);
			break;
		case UNIT_KIND_WEBER:
			u.setKind(libsbmlConstants.UNIT_KIND_WEBER);
			break;
		}
		u.setExponent(unit.getExponent());
		u.setMultiplier(unit.getMultiplier());
		u.setOffset(unit.getOffset());
		u.setScale(unit.getScale());
		return u;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.SBMLWriter#writeUnitDefinition(org.sbml.UnitDefinition)
	 */
	// @Override
	public Object writeUnitDefinition(UnitDefinition unitDefinition) {
		org.sbml.libsbml.UnitDefinition ud = new org.sbml.libsbml.UnitDefinition();
		saveNamedSBaseProperties(unitDefinition, ud);
		for (Unit u : unitDefinition.getListOfUnits())
			ud.addUnit((org.sbml.libsbml.Unit) writeUnit(u));
		return ud;
	}

}
