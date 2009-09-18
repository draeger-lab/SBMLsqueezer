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

import java.util.Date;

import jp.sbi.celldesigner.plugin.PluginCompartment;
import jp.sbi.celldesigner.plugin.PluginCompartmentType;
import jp.sbi.celldesigner.plugin.PluginConstraint;
import jp.sbi.celldesigner.plugin.PluginEvent;
import jp.sbi.celldesigner.plugin.PluginEventAssignment;
import jp.sbi.celldesigner.plugin.PluginFunctionDefinition;
import jp.sbi.celldesigner.plugin.PluginInitialAssignment;
import jp.sbi.celldesigner.plugin.PluginKineticLaw;
import jp.sbi.celldesigner.plugin.PluginModel;
import jp.sbi.celldesigner.plugin.PluginModifierSpeciesReference;
import jp.sbi.celldesigner.plugin.PluginParameter;
import jp.sbi.celldesigner.plugin.PluginReaction;
import jp.sbi.celldesigner.plugin.PluginRule;
import jp.sbi.celldesigner.plugin.PluginSpecies;
import jp.sbi.celldesigner.plugin.PluginSpeciesAlias;
import jp.sbi.celldesigner.plugin.PluginSpeciesReference;
import jp.sbi.celldesigner.plugin.PluginSpeciesType;
import jp.sbi.celldesigner.plugin.PluginUnit;
import jp.sbi.celldesigner.plugin.PluginUnitDefinition;
import jp.sbi.celldesigner.plugin.PluginRateRule;
import jp.sbi.celldesigner.plugin.PluginAlgebraicRule;
import jp.sbi.celldesigner.plugin.PluginAssignmentRule;

import org.sbml.jsbml.AlgebraicRule;
import org.sbml.jsbml.AssignmentRule;
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
import org.sbml.jsbml.RateRule;
import org.sbml.jsbml.Reaction;
import org.sbml.jsbml.Rule;
import org.sbml.jsbml.SBMLException;
import org.sbml.jsbml.SBase;
import org.sbml.jsbml.Species;
import org.sbml.jsbml.SpeciesReference;
import org.sbml.jsbml.SpeciesType;
import org.sbml.jsbml.StoichiometryMath;
import org.sbml.jsbml.Trigger;
import org.sbml.jsbml.Unit;
import org.sbml.jsbml.UnitDefinition;
import org.sbml.libsbml.ASTNode;
import org.sbml.libsbml.libsbml;
import org.sbml.libsbml.libsbmlConstants;
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

	public Object convertDate(Date date) {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * 
	 * 
	 * @param u
	 * @param unit
	 * @return
	 */
	// TODO: libsbmlConstants?
	private boolean equal(Unit u, PluginUnit unit) {
		if (u == null || unit == null)
			return false;
		boolean equal = true;
		switch (unit.getKind()) {
		case libsbmlConstants.UNIT_KIND_AMPERE:
			equal &= u.getKind() == Unit.Kind.AMPERE;
			break;
		case libsbmlConstants.UNIT_KIND_BECQUEREL:
			equal &= u.getKind() == Unit.Kind.BECQUEREL;
			break;
		case libsbmlConstants.UNIT_KIND_CANDELA:
			equal &= u.getKind() == Unit.Kind.CANDELA;
			break;
		case libsbmlConstants.UNIT_KIND_CELSIUS:
			equal &= u.getKind() == Unit.Kind.CELSIUS;
			break;
		case libsbmlConstants.UNIT_KIND_COULOMB:
			equal &= u.getKind() == Unit.Kind.COULOMB;
			break;
		case libsbmlConstants.UNIT_KIND_DIMENSIONLESS:
			equal &= u.getKind() == Unit.Kind.DIMENSIONLESS;
			break;
		case libsbmlConstants.UNIT_KIND_FARAD:
			equal &= u.getKind() == Unit.Kind.FARAD;
			break;
		case libsbmlConstants.UNIT_KIND_GRAM:
			equal &= u.getKind() == Unit.Kind.GRAM;
			break;
		case libsbmlConstants.UNIT_KIND_GRAY:
			equal &= u.getKind() == Unit.Kind.GRAY;
			break;
		case libsbmlConstants.UNIT_KIND_HENRY:
			equal &= u.getKind() == Unit.Kind.HENRY;
			break;
		case libsbmlConstants.UNIT_KIND_HERTZ:
			equal &= u.getKind() == Unit.Kind.HERTZ;
			break;
		case libsbmlConstants.UNIT_KIND_INVALID:
			equal &= u.getKind() == Unit.Kind.INVALID;
			break;
		case libsbmlConstants.UNIT_KIND_ITEM:
			equal &= u.getKind() == Unit.Kind.ITEM;
			break;
		case libsbmlConstants.UNIT_KIND_JOULE:
			equal &= u.getKind() == Unit.Kind.JOULE;
			break;
		case libsbmlConstants.UNIT_KIND_KATAL:
			equal &= u.getKind() == Unit.Kind.KATAL;
			break;
		case libsbmlConstants.UNIT_KIND_KELVIN:
			equal &= u.getKind() == Unit.Kind.KELVIN;
			break;
		case libsbmlConstants.UNIT_KIND_KILOGRAM:
			equal &= u.getKind() == Unit.Kind.KILOGRAM;
			break;
		case libsbmlConstants.UNIT_KIND_LITER:
			equal &= u.getKind() == Unit.Kind.LITER;
			break;
		case libsbmlConstants.UNIT_KIND_LITRE:
			equal &= u.getKind() == Unit.Kind.LITRE;
			break;
		case libsbmlConstants.UNIT_KIND_LUMEN:
			equal &= u.getKind() == Unit.Kind.LUMEN;
			break;
		case libsbmlConstants.UNIT_KIND_LUX:
			equal &= u.getKind() == Unit.Kind.LUX;
			break;
		case libsbmlConstants.UNIT_KIND_METER:
			equal &= u.getKind() == Unit.Kind.METER;
			break;
		case libsbmlConstants.UNIT_KIND_METRE:
			equal &= u.getKind() == Unit.Kind.METRE;
			break;
		case libsbmlConstants.UNIT_KIND_MOLE:
			equal &= u.getKind() == Unit.Kind.MOLE;
			break;
		case libsbmlConstants.UNIT_KIND_NEWTON:
			equal &= u.getKind() == Unit.Kind.NEWTON;
			break;
		case libsbmlConstants.UNIT_KIND_OHM:
			equal &= u.getKind() == Unit.Kind.OHM;
			break;
		case libsbmlConstants.UNIT_KIND_PASCAL:
			equal &= u.getKind() == Unit.Kind.PASCAL;
			break;
		case libsbmlConstants.UNIT_KIND_RADIAN:
			equal &= u.getKind() == Unit.Kind.RADIAN;
			break;
		case libsbmlConstants.UNIT_KIND_SECOND:
			equal &= u.getKind() == Unit.Kind.SECOND;
			break;
		case libsbmlConstants.UNIT_KIND_SIEMENS:
			equal &= u.getKind() == Unit.Kind.SIEMENS;
			break;
		case libsbmlConstants.UNIT_KIND_SIEVERT:
			equal &= u.getKind() == Unit.Kind.SIEVERT;
			break;
		case libsbmlConstants.UNIT_KIND_STERADIAN:
			equal &= u.getKind() == Unit.Kind.STERADIAN;
			break;
		case libsbmlConstants.UNIT_KIND_TESLA:
			equal &= u.getKind() == Unit.Kind.TESLA;
			break;
		case libsbmlConstants.UNIT_KIND_VOLT:
			equal &= u.getKind() == Unit.Kind.VOLT;
			break;
		case libsbmlConstants.UNIT_KIND_WATT:
			equal &= u.getKind() == Unit.Kind.WATT;
			break;
		case libsbmlConstants.UNIT_KIND_WEBER:
			equal &= u.getKind() == Unit.Kind.WEBER;
			break;
		}
		equal &= u.getExponent() == unit.getExponent();
		equal &= u.getMultiplier() == unit.getMultiplier();
		equal &= u.getScale() == unit.getScale();
		equal &= u.getOffset() == unit.getOffset();
		return equal;
	}

	public int getNumErrors(Object sbase) {
		// TODO Auto-generated method stub
		return 0;
	}

	public String getWarnings(Object sbmlDocument) {
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
	public void saveChanges(Model model, Object orig) throws SBMLException {
		if (!(orig instanceof PluginModel))
			throw new IllegalArgumentException(
					"only instances of PluginModel can be considered.");
		PluginModel mo = (PluginModel) orig;
		int i;

		// Function definitions
		for (FunctionDefinition c : model.getListOfFunctionDefinitions()) {
			if (mo.getFunctionDefinition(c.getId()) == null)
				mo.addFunctionDefinition(writeFunctionDefinition(c));
			else
				saveMathContainerProperties(c, mo.getFunctionDefinition(c
						.getId()));
		}
		// remove unnecessary function definitions
		for (i = mo.getNumFunctionDefinitions() - 1; i >= 0; i--) {
			PluginFunctionDefinition c = mo.getFunctionDefinition(i);
			if (model.getFunctionDefinition(c.getId()) == null)
				mo.getListOfFunctionDefinitions().remove(i);
		}

		// Unit definitions
		for (UnitDefinition ud : model.getListOfUnitDefinitions())
			if (!ud.equals(UnitDefinition.SUBSTANCE)
					&& !ud.equals(UnitDefinition.VOLUME)
					&& !ud.equals(UnitDefinition.AREA)
					&& !ud.equals(UnitDefinition.LENGTH)
					&& !ud.equals(UnitDefinition.TIME)) {
				PluginUnitDefinition libU = mo.getUnitDefinition(ud.getId());
				if (libU != null) {
					saveNamedSBaseProperties(ud, libU);
					for (Unit u : ud.getListOfUnits()) {
						boolean contains = false;
						for (int j = 0; j < libU.getNumUnits() && !contains; j++) {
							if (equal(u, libU.getUnit(j)))
								contains = true;
						}
						if (!contains)
							libU.addUnit(writeUnit(u));
					}
				} else
					mo.addUnitDefinition(writeUnitDefinition(ud));
			}
		// remove unnecessary units
		for (i = mo.getNumUnitDefinitions() - 1; i >= 0; i--) {
			PluginUnitDefinition ud = mo.getUnitDefinition(i);
			if (model.getUnitDefinition(ud.getId()) == null)
				mo.getListOfUnitDefinitions().remove(i);
		}

		// Compartment types
		for (CompartmentType c : model.getListOfCompartmentTypes()) {
			if (mo.getCompartmentType(c.getId()) == null)
				mo.addCompartmentType(writeCompartmentType(c));
			else
				saveNamedSBaseProperties(c, mo.getCompartmentType(c.getId()));
		}
		// remove unnecessary compartmentTypes
		for (i = mo.getNumCompartmentTypes() - 1; i >= 0; i--) {
			PluginCompartmentType c = mo.getCompartmentType(i);
			if (model.getCompartmentType(c.getId()) == null)
				mo.getListOfCompartmentTypes().remove(i);
		}

		// Species types
		for (SpeciesType c : model.getListOfSpeciesTypes()) {
			if (mo.getSpeciesType(c.getId()) == null)
				mo.addSpeciesType(writeSpeciesType(c));
			else
				saveNamedSBaseProperties(c, mo.getSpeciesType(c.getId()));
		}
		// remove unnecessary speciesTypes
		for (i = mo.getNumSpeciesTypes() - 1; i >= 0; i--) {
			PluginSpeciesType c = mo.getSpeciesType(i);
			if (model.getSpeciesType(c.getId()) == null)
				mo.getListOfSpeciesTypes().remove(i);
		}

		// Compartments
		for (Compartment c : model.getListOfCompartments()) {
			if (mo.getCompartment(c.getId()) == null)
				mo.addCompartment(writeCompartment(c));
			else
				saveCompartmentProperties(c, mo.getCompartment(c.getId()));
		}
		// remove unnecessary compartments
		for (i = mo.getNumCompartments() - 1; i >= 0; i--) {
			PluginCompartment c = mo.getCompartment(i);
			if (model.getCompartment(c.getId()) == null)
				mo.getListOfCompartments().remove(i);
		}

		// Species
		for (Species s : model.getListOfSpecies()) {
			if (mo.getSpecies(s.getId()) == null)
				mo.addSpecies(writeSpecies(s));
			else
				saveSpeciesProperties(s, mo.getSpecies(s.getId()));
		}
		// remove unnecessary species
		for (i = mo.getNumSpecies() - 1; i >= 0; i--) {
			PluginSpecies s = mo.getSpecies(i);
			if (model.getSpecies(s.getId()) == null)
				mo.getListOfSpecies().remove(i);
		}

		// add or change parameters
		for (Parameter p : model.getListOfParameters()) {
			if (mo.getParameter(p.getId()) == null)
				mo.addParameter(writeParameter(p));
			else
				saveParameterProperties(p, mo.getParameter(p.getId()));
		}
		// remove parameters
		for (i = mo.getNumParameters() - 1; i >= 0; i--) {
			PluginParameter p = mo.getParameter(i);
			if (model.getParameter(p.getId()) == null)
				mo.getListOfParameters().remove(i);
		}

		// initial assignments
		for (i = 0; i < model.getNumInitialAssignments(); i++) {
			InitialAssignment ia = model.getInitialAssignment((int) i);
			int contains = -1;
			for (int j = 0; j < mo.getNumInitialAssignments() && contains < 0; j++) {
				PluginInitialAssignment libIA = mo.getInitialAssignment(j);
				if (libIA.getSymbol().equals(ia.getSymbol())
						&& equal(ia.getMath(), libsbml.parseFormula(libIA
								.getMath())))
					contains = j;
			}
			if (contains < 0)
				mo.addInitialAssignment(writeInitialAssignment(ia));
			else
				saveMathContainerProperties(ia, mo
						.getInitialAssignment(contains));
		}
		// remove unnecessary initial assignments
		for (i = mo.getNumInitialAssignments() - 1; i >= 0; i--) {
			PluginInitialAssignment c = mo.getInitialAssignment(i);
			boolean contains = false;
			for (int j = 0; j < model.getNumInitialAssignments() && !contains; j++) {
				InitialAssignment ia = model.getInitialAssignment(j);
				if (ia.getSymbol().equals(c.getSymbol())
						&& equal(ia.getMath(), libsbml
								.parseFormula(c.getMath())))
					contains = true;
			}
			if (!contains)
				mo.getListOfInitialAssignments().remove(i);
		}

		// rules
		for (i = 0; i < model.getNumRules(); i++) {
			Rule rule = model.getRule((int) i);
			int contains = -1;
			for (int j = 0; j < mo.getNumRules() && contains < 0; j++) {
				boolean equal = false;
				PluginRule ruleOrig = mo.getRule(j);
				if (rule instanceof RateRule
						&& ruleOrig instanceof PluginRateRule) {
					equal = ((RateRule) rule).getVariable().equals(
							((PluginRateRule) ruleOrig).getVariable());
				} else if (rule instanceof AssignmentRule
						&& ruleOrig instanceof PluginAssignmentRule) {
					equal = ((AssignmentRule) rule).getVariable().equals(
							((PluginAssignmentRule) ruleOrig).getVariable());
				} else if (rule instanceof AlgebraicRule
						&& ruleOrig instanceof PluginAlgebraicRule) {
					equal = true;
				}
				if (equal)
					equal &= equal(rule.getMath(), ruleOrig.getMath());
				if (equal)
					contains = j;
			}
			if (contains < 0)
				mo.addRule(writeRule(rule));
			else
				// math is equal anyway...
				saveSBaseProperties(rule, mo.getRule(contains));
		}
		// remove unnecessary rules
		for (i = mo.getNumRules() - 1; i >= 0; i--) {
			PluginRule c = mo.getRule(i);
			boolean contains = false;
			for (int j = 0; j < model.getNumRules() && !contains; j++) {
				Rule r = model.getRule(j);
				if ((c instanceof PluginRateRule && r instanceof RateRule && ((PluginRateRule) c)
						.getVariable().equals(((RateRule) r).getVariable()))
						|| (c instanceof PluginAssignmentRule
								&& r instanceof AssignmentRule && ((AssignmentRule) r)
								.getVariable().equals(
										((PluginAssignmentRule) c)
												.getVariable()))
						|| (c instanceof PluginAlgebraicRule && r instanceof AlgebraicRule))
					if (equal(r.getMath(), c.getMath()))
						contains = true;
			}
			if (!contains)
				mo.getListOfRules().remove(i);
		}

		// constraints
		for (i = 0; i < model.getNumConstraints(); i++) {
			Constraint ia = model.getConstraint((int) i);
			long contains = -1;
			for (long j = 0; j < mo.getNumConstraints() && contains < 0; j++) {
				PluginConstraint c = mo.getConstraint((int) j);
				if (equal(ia.getMath(), libsbml.parseFormula(c.getMath())))
					contains = j;
			}
			if (contains < 0)
				mo.addConstraint(writeConstraint(ia));
			else
				saveMathContainerProperties(ia, mo
						.getConstraint((int) contains));
		}
		// remove unnecessary constraints
		for (i = mo.getNumConstraints() - 1; i >= 0; i--) {
			PluginConstraint c = mo.getConstraint(i);
			boolean contains = false;
			for (int j = 0; j < model.getNumConstraints() && !contains; j++) {
				Constraint ia = model.getConstraint(j);
				if (equal(ia.getMath(), libsbml.parseFormula(c.getMath())))
					contains = true;
			}
			if (!contains)
				mo.getListOfConstraints().remove(i);
		}

		// add or change reactions
		for (Reaction r : model.getListOfReactions()) {
			if (mo.getReaction(r.getId()) == null)
				mo.addReaction(writeReaction(r));
			else
				saveReactionProperties(r, mo.getReaction(r.getId()));
		}
		// remove reactions
		for (i = mo.getNumReactions() - 1; i >= 0; i--) {
			PluginReaction r = mo.getReaction(i);
			if (model.getReaction(r.getId()) == null)
				mo.getListOfReactions().remove(i);
		}

		// events
		for (Event event : model.getListOfEvents()) {
			if (mo.getEvent(event.getId()) == null)
				mo.addEvent(writeEvent(event));
			else
				saveEventProperties(event, mo.getEvent(event.getId()));
		}
		// remove events
		for (i = mo.getNumEvents() - 1; i >= 0; i--) {
			PluginEvent eventOrig = mo.getEvent(i);
			if (model.getEvent(eventOrig.getId()) == null)
				mo.getListOfEvents().remove(i);
		}
	}

	public void saveCompartmentProperties(Compartment c, Object compartment) {
		if (!(compartment instanceof PluginCompartment))
			throw new IllegalArgumentException(
					"compartment must be an instance of PluginCompartment.");
		PluginCompartment comp = (PluginCompartment) compartment;
		saveNamedSBaseProperties(c, comp);
		if (c.isSetSize() && c.getSize() != comp.getSize())
			comp.setSize(c.getSize());
		if (c.isSetCompartmentType()
				&& !c.getCompartmentType().equals(comp.getCompartmentType()))
			comp.setCompartmentType(c.getCompartmentType());
		if (c.getSpatialDimensions() != comp.getSpatialDimensions())
			comp.setSpatialDimensions(c.getSpatialDimensions());
		if (c.isSetUnits() && !c.getUnits().equals(comp.getUnits()))
			comp.setUnits(c.getUnits());
		if (c.isSetOutside() && !c.getOutside().equals(comp.getOutside()))
			comp.setOutside(c.getOutside());
		if (c.getConstant() != comp.getConstant())
			comp.setConstant(c.getConstant());
	}

	public void saveCVTermProperties(CVTerm cvt, Object term) {
		// TODO Auto-generated method stub

	}

	public void saveEventProperties(Event ev, Object event)
			throws SBMLException {
		if (!(event instanceof PluginEvent))
			throw new IllegalArgumentException(
					"event must be an instance of PluginEvent.");
		PluginEvent e = (PluginEvent) event;
		saveNamedSBaseProperties(ev, e);
		if (ev.getUseValuesFromTriggerTime() != e.getUseValuesFromTriggerTime())
			e.setUseValuesFromTriggerTime(ev.getUseValuesFromTriggerTime());
		if (ev.isSetTimeUnits() && ev.getTimeUnits() != e.getTimeUnits())
			e.setTimeUnits(ev.getTimeUnits());
		if (ev.isSetDelay()) {
			if (e.getDelay() != null)
				e.setDelay(writeDelay(ev.getDelay()));
			else
				saveMathContainerProperties(ev.getDelay(), e.getDelay());
		} else if (e.getDelay() == null)
			e.setDelay(new ASTNode(0));
		if (ev.isSetTrigger()) {
			if (e.getTrigger() == null)
				e.setTrigger(writeTrigger(ev.getTrigger()));
			else
				saveMathContainerProperties(ev.getTrigger(), e.getTrigger());
		}
		// synchronize event assignments

		for (EventAssignment ea : ev.getListOfEventAssignments()) {
			long contains = -1;
			for (long i = 0; i < e.getNumEventAssignments() && contains < 0; i++) {
				PluginEventAssignment libEA = e.getEventAssignment((int) i);
				if (libEA.getVariable().equals(ea.getVariable())
						&& equal(ea.getMath(), libEA.getMath()))
					contains = i;
			}
			if (contains < 0)
				e.addEventAssignment(writeEventAssignment(ea,
						(PluginEvent) event));
			else
				saveMathContainerProperties(ea, e
						.getEventAssignment((int) contains));
		}
		// remove unnecessary event assignments
		for (long i = e.getNumEventAssignments() - 1; i >= 0; i--) {
			PluginEventAssignment ea = e.getEventAssignment((int) i);
			boolean contains = false;
			for (int j = 0; j < ev.getNumEventAssignments() && !contains; j++) {
				EventAssignment eventA = ev.getEventAssignment(j);
				if (eventA.isSetVariable()
						&& eventA.getVariable().equals(ea.getVariable())
						&& equal(eventA.getMath(), ea.getMath()))
					contains = true;
			}
			if (!contains)
				e.removeEventAssignment((int) i);
		}

	}

	public void saveKineticLawProperties(KineticLaw kl, Object kineticLaw)
			throws SBMLException {
		if (!(kineticLaw instanceof PluginKineticLaw))
			throw new IllegalArgumentException(
					"kineticLaw must be an instance of PluginKineticLaw.");
		PluginKineticLaw libKinLaw = (PluginKineticLaw) kineticLaw;
		// add or change parameters
		// TODO: parametercounter
		int para = 0;
		for (Parameter p : kl.getListOfParameters()) {
			PluginParameter libParam = libKinLaw.getParameter(para);
			para++;
			if (libParam == null)
				libKinLaw.addParameter(writeParameter(p));
			else
				saveParameterProperties(p, libParam);

		}
		// remove parameters
		for (long i = libKinLaw.getNumParameters() - 1; i >= 0; i--) {
			PluginParameter p = libKinLaw.getParameter((int) i);
			if (kl.getParameter(p.getId()) == null)
				libKinLaw.getListOfParameters().remove((int) i);
		}
		saveMathContainerProperties(kl, libKinLaw);
	}

	public void saveMathContainerProperties(MathContainer mc, Object sbase)
			throws SBMLException {
		if (mc instanceof NamedSBase)
			saveNamedSBaseProperties((NamedSBase) mc, sbase);
		else
			saveSBaseProperties(mc, sbase);
		if (sbase instanceof PluginConstraint) {
			PluginConstraint kl = (PluginConstraint) sbase;
			boolean equal = (kl.getMath() != null) && mc.isSetMath()
					&& equal(mc.getMath(), libsbml.parseFormula(kl.getMath()));
			if (mc.isSetMath() && !equal)
				// TODO constraint setmath not defined
				// && kl.setMath(convert(mc.getMath())) !=
				// libsbmlConstants.LIBSBML_OPERATION_SUCCESS)
				throw new SBMLException("Unable to set math of "
						+ mc.getClass().getSimpleName() + " in "
						+ kl.getClass().getName());
			// } else if (sbase instanceof PluginDelay) {
			// PluginDelay kl = (PluginDelay) sbase;
			// boolean equal = kl.isSetMath() && mc.isSetMath()
			// && equal(mc.getMath(), kl.getMath());
			// if (mc.isSetMath()
			// && !equal
			// && kl.setMath(convert(mc.getMath())) !=
			// libsbmlConstants.LIBSBML_OPERATION_SUCCESS)
			// throw new SBMLException("Unable to set math of "
			// + mc.getClass().getSimpleName() + " in "
			// + kl.getClass().getName());
		} else if (sbase instanceof PluginEventAssignment) {
			PluginEventAssignment kl = (PluginEventAssignment) sbase;
			boolean equal = (kl.getMath() != null) && mc.isSetMath()
					&& equal(mc.getMath(), kl.getMath());
			if (mc.isSetMath() && !equal)
				throw new SBMLException("Unable to set math of "
						+ mc.getClass().getSimpleName() + " in "
						+ kl.getClass().getName());
		} else if (sbase instanceof PluginFunctionDefinition) {
			PluginFunctionDefinition kl = (PluginFunctionDefinition) sbase;
			boolean equal = (kl.getMath() != null) && mc.isSetMath()
					&& equal(mc.getMath(), kl.getMath());
			if (mc.isSetMath() && !equal)
				throw new SBMLException("Unable to set math of "
						+ mc.getClass().getSimpleName() + " in "
						+ kl.getClass().getName());
		} else if (sbase instanceof PluginInitialAssignment) {
			PluginInitialAssignment kl = (PluginInitialAssignment) sbase;
			boolean equal = (kl.getMath() != null) && mc.isSetMath()
					&& equal(mc.getMath(), libsbml.parseFormula(kl.getMath()));
			if (mc.isSetMath() && !equal)
				throw new SBMLException("Unable to set math of "
						+ mc.getClass().getSimpleName() + " in "
						+ kl.getClass().getName());
		} else if (sbase instanceof PluginKineticLaw) {
			PluginKineticLaw kl = (PluginKineticLaw) sbase;
			boolean equal = (kl.getMath() != null) && mc.isSetMath()
					&& equal(mc.getMath(), kl.getMath());
			if (mc.isSetMath() && !equal)
				throw new SBMLException("Unable to set math of "
						+ mc.getClass().getSimpleName() + " in "
						+ kl.getClass().getName());
		} else if (sbase instanceof PluginRule) {
			PluginRule kl = (PluginRule) sbase;
			boolean equal = (kl.getMath() != null) && mc.isSetMath()
					&& equal(mc.getMath(), kl.getMath());
			if (mc.isSetMath() && !equal)
				throw new SBMLException("Unable to set math of "
						+ mc.getClass().getSimpleName() + " in "
						+ kl.getClass().getName());
		}
		// } else if (sbase instanceof PluginStoichiometryMath) {
		// PluginStoichiometryMath kl = (PluginStoichiometryMath) sbase;
		// boolean equal = kl.isSetMath() && mc.isSetMath()
		// && equal(mc.getMath(), kl.getMath());
		// if (mc.isSetMath()
		// && !equal
		// && kl.setMath(convert(mc.getMath())) !=
		// libsbmlConstants.LIBSBML_OPERATION_SUCCESS)
		// throw new SBMLException("Unable to set math of "
		// + mc.getClass().getSimpleName() + " in "
		// + kl.getClass().getName());
		// } else if (sbase instanceof PluginTrigger) {
		// PluginTrigger kl = (PluginTrigger) sbase;
		// boolean equal = kl.isSetMath() && mc.isSetMath()
		// && equal(mc.getMath(), kl.getMath());
		// if (mc.isSetMath()
		// && !equal
		// && kl.setMath(convert(mc.getMath())) !=
		// libsbmlConstants.LIBSBML_OPERATION_SUCCESS)
		// throw new SBMLException("Unable to set math of "
		// + mc.getClass().getSimpleName() + " in "
		// + kl.getClass().getName());
		// }
	}

	public void saveModelHistoryProperties(ModelHistory mh, Object modelHistory) {
		// TODO Auto-generated method stub

	}

	public void saveModifierSpeciesReferenceProperties(
			ModifierSpeciesReference modifierSpeciesReference, Object msr) {
		if (!(msr instanceof PluginModifierSpeciesReference))
			throw new IllegalArgumentException(
					"modifierSpeciesReference must be an instance of org.sbml.libsbml.ModifierSpeciesReference.");
		saveNamedSBaseProperties(modifierSpeciesReference, msr);
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

	public void saveParameterProperties(Parameter p, Object parameter) {
		if (!(parameter instanceof PluginParameter))
			throw new IllegalArgumentException(
					"parameter must be an instance of PluginParameter.");
		PluginParameter po = (PluginParameter) parameter;
		saveNamedSBaseProperties(p, po);
		if (p.isSetValue() && p.getValue() != po.getValue())
			po.setValue(p.getValue());
		if (p.getConstant() != po.getConstant())
			po.setConstant(p.getConstant());
		if (p.isSetUnits() && !p.getUnits().equals(po.getUnits()))
			po.setUnits(p.getUnits());
	}

	public void saveReactionProperties(Reaction r, Object reaction)
			throws SBMLException {
		if (!(reaction instanceof PluginReaction))
			throw new IllegalArgumentException(
					"reaction must be an instance of PluginReaction.");
		PluginReaction ro = (PluginReaction) reaction;
		long i;
		saveNamedSBaseProperties(r, ro);
		if (r.getFast() != ro.getFast())
			ro.setFast(r.getFast());
		if (r.getReversible() != ro.getReversible())
			ro.setReversible(r.getReversible());
		long contains;
		// reactants.
		for (SpeciesReference sr : r.getListOfReactants()) {
			contains = -1;
			for (i = 0; i < ro.getNumReactants() && contains < 0; i++)
				if (sr.getSpecies()
						.equals(ro.getReactant((int) i).getSpecies()))
					contains = i;
			if (contains < 0)
				ro.addReactant(writeSpeciesReference(sr));
			else
				saveSpeciesReferenceProperties(sr, ro
						.getReactant((int) contains));
		}
		// remove unnecessary reactants.
		for (i = ro.getNumReactants() - 1; i >= 0; i--) {
			PluginSpeciesReference roreactant = ro.getReactant((int) i);
			boolean keep = false;
			for (int j = 0; j < r.getNumReactants() && !keep; j++)
				if (r.getReactant(j).getSpecies().equals(
						roreactant.getSpecies()))
					keep = true;
			if (!keep)
				ro.getListOfReactants().remove((int) i);
		}
		for (SpeciesReference sr : r.getListOfProducts()) {
			contains = -1;
			for (i = 0; i < ro.getNumProducts() && contains < 0; i++)
				if (sr.getSpecies().equals(ro.getProduct((int) i).getSpecies()))
					contains = i;
			if (contains < 0)
				ro.addProduct(writeSpeciesReference(sr));
			else
				saveSpeciesReferenceProperties(sr, ro
						.getProduct((int) contains));
		}
		// remove unnecessary products.
		for (i = ro.getNumProducts() - 1; i >= 0; i--) {
			PluginSpeciesReference msr = ro.getProduct((int) i);
			boolean keep = false;
			for (int j = 0; j < r.getNumProducts() && !keep; j++)
				if (r.getProduct(j).getSpecies().equals(msr.getSpecies()))
					keep = true;
			if (!keep)
				ro.getListOfProducts().remove((int) i);
		}
		// check modifiers
		for (ModifierSpeciesReference mr : r.getListOfModifiers()) {
			contains = -1;
			for (i = 0; i < ro.getNumModifiers() && contains < 0; i++)
				if (mr.getSpecies()
						.equals(ro.getModifier((int) i).getSpecies()))
					contains = i;
			if (contains < 0)
				ro.addModifier(writeModifierSpeciesReference(mr,
						(PluginReaction) reaction));
			else
				saveModifierSpeciesReferenceProperties(mr, ro
						.getModifier((int) contains));
		}
		// remove unnecessary modifiers.
		for (i = ro.getNumModifiers() - 1; i >= 0; i--) {
			PluginModifierSpeciesReference msr = ro.getModifier((int) i);
			boolean keep = false;
			for (int j = 0; j < r.getNumModifiers() && !keep; j++)
				if (r.getModifier(j).getSpecies().equals(msr.getSpecies()))
					keep = true;
			if (!keep)
				ro.getListOfModifiers().remove((int) i);
		}
		if (r.isSetKineticLaw()) {
			if (ro.getKineticLaw() == null)
				ro.setKineticLaw(writeKineticLaw(r.getKineticLaw(),
						(PluginReaction) reaction));
			else if (ro.getKineticLaw() != null)
				saveKineticLawProperties(r.getKineticLaw(), ro.getKineticLaw());
		} else if (ro.getKineticLaw() != null)
			ro.setKineticLaw(null);
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

	public void saveSpeciesProperties(Species s, Object species) {
		if (!(species instanceof PluginSpecies))
			throw new IllegalArgumentException(
					"species must be an instance of PluginSpecies.");
		PluginSpecies spec = (PluginSpecies) species;
		saveNamedSBaseProperties(s, spec);
		if (s.isSetSpeciesType()
				&& !s.getSpeciesType().equals(spec.getSpeciesType()))
			spec.setSpeciesType(s.getSpeciesType());
		if (s.isSetCompartment()
				&& !s.getCompartment().equals(spec.getCompartment()))
			spec.setCompartment(s.getCompartment());
		if (s.isSetInitialAmount()) {
			if (!spec.isSetInitialAmount()
					|| s.getInitialAmount() != spec.getInitialAmount())
				spec.setInitialAmount(s.getInitialAmount());
		} else if (s.isSetInitialConcentration())
			if (!spec.isSetInitialConcentration()
					|| s.getInitialConcentration() != spec
							.getInitialConcentration())
				spec.setInitialConcentration(s.getInitialConcentration());
		if (s.isSetSubstanceUnits()
				&& !s.getSubstanceUnits().equals(spec.getSubstanceUnits()))
			spec.setSubstanceUnits(s.getSubstanceUnits());
		if (s.getHasOnlySubstanceUnits() != spec.getHasOnlySubstanceUnits())
			spec.setHasOnlySubstanceUnits(s.getHasOnlySubstanceUnits());
		if (s.getBoundaryCondition() != spec.getBoundaryCondition())
			spec.setBoundaryCondition(spec.getBoundaryCondition());
		if (s.isSetCharge() && s.getCharge() != spec.getCharge())
			spec.setCharge(s.getCharge());
		if (s.getConstant() != spec.getConstant())
			spec.setConstant(s.getConstant());
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
			Object specRef) throws SBMLException {
		if (!(specRef instanceof PluginSpeciesReference))
			throw new IllegalArgumentException(
					"specRef must be an instance of PluginSpeciesReference.");
		PluginSpeciesReference sp = (PluginSpeciesReference) specRef;
		saveNamedSBaseProperties(sr, sp);
		// TODO: species darf nicht geändert worden sein!
		// if (sr.isSetSpecies() && !sr.getSpecies().equals(sp.getSpecies()))
		// sp.setSpecies(sr.getSpecies());
		if (sr.isSetStoichiometryMath()) {
			if (sp.getStoichiometryMath() != null
					&& !equal(sr.getStoichiometryMath().getMath(), sp
							.getStoichiometryMath().getMath()))
				saveMathContainerProperties(sr.getStoichiometryMath(), sp
						.getStoichiometryMath());
			else
				sp.setStoichiometryMath(writeStoichoimetryMath(sr
						.getStoichiometryMath()));
		} else
			sp.setStoichiometry(sr.getStoichiometry());
	}

	public PluginCompartment writeCompartment(Compartment compartment) {
		PluginCompartment c = new PluginCompartment(compartment
				.getCompartmentType());
		saveNamedSBaseProperties(compartment, c);
		if (compartment.isSetCompartmentType()
				&& !c.getCompartmentType().equals(
						compartment.getCompartmentType()))
			c.setCompartmentType(compartment.getCompartmentType());
		if (compartment.isSetOutside() && !c.equals(compartment.getOutside()))
			c.setOutside(compartment.getOutside());
		if (compartment.isSetSize() && compartment.getSize() != c.getSize())
			c.setSize(compartment.getSize());
		if (compartment.isSetUnits()
				&& !compartment.getUnits().equals(c.getUnits()))
			c.setUnits(compartment.getUnits());
		c.setConstant(compartment.getConstant());
		c.setSpatialDimensions(compartment.getSpatialDimensions());
		return c;
	}

	public PluginCompartmentType writeCompartmentType(
			CompartmentType compartmentType) {
		// TODO: constructor mit id?
		PluginCompartmentType ct = new PluginCompartmentType(compartmentType
				.getId());
		saveNamedSBaseProperties(compartmentType, ct);
		return ct;
	}

	// TODO: constructor und setmath
	public PluginConstraint writeConstraint(Constraint constraint) {
		PluginConstraint c = new PluginConstraint(constraint.);
		saveSBaseProperties(constraint, c);
		if (constraint.isSetMath() && !equal(constraint.getMath(), libsbml.parseFormula(c.getMath())))
			c.setMath(convert(constraint.getMath()));
		if (constraint.isSetMessage()
				&& !constraint.getMessage().equals(c.getMessage()))
			c.setMessage(constraint.getMessage());
		return c;
	}

	public Object writeCVTerm(CVTerm cvt) {
		// TODO Auto-generated method stub
		return null;
	}

	public ASTNode writeDelay(Delay delay) {
		return convert(delay.getMath());
	}

	public PluginEvent writeEvent(Event event) throws SBMLException {
		PluginEvent e = new PluginEvent(event.getId());
		saveNamedSBaseProperties(event, e);
		if (event.isSetDelay())
			e.setDelay(writeDelay(event.getDelay()));
		for (EventAssignment ea : event.getListOfEventAssignments())
			e.addEventAssignment(writeEventAssignment(ea, e));
		if (event.isSetTimeUnits())
			e.setTimeUnits(event.getTimeUnits());
		if (e.getTrigger() != null)
			e.setTrigger(writeTrigger(event.getTrigger()));
		e.setUseValuesFromTriggerTime(event.getUseValuesFromTriggerTime());
		return e;
	}

	public PluginEventAssignment writeEventAssignment(
			EventAssignment eventAssignment, PluginEvent ev)
			throws SBMLException {
		PluginEventAssignment ea = new PluginEventAssignment(ev);
		saveMathContainerProperties(eventAssignment, ea);
		if (eventAssignment.isSetVariable())
			ea.setVariable(eventAssignment.getVariable());
		return ea;
	}

	public PluginFunctionDefinition writeFunctionDefinition(
			FunctionDefinition functionDefinition) throws SBMLException {
		PluginFunctionDefinition fd = new PluginFunctionDefinition(
				functionDefinition.getId());
		saveMathContainerProperties(functionDefinition, fd);
		return fd;
	}

	public PluginInitialAssignment writeInitialAssignment(
			InitialAssignment initialAssignment) throws SBMLException {
		PluginInitialAssignment ia = new PluginInitialAssignment(initialAssignment.);
		saveMathContainerProperties(initialAssignment, ia);
		if (initialAssignment.isSetSymbol())
			ia.setSymbol(initialAssignment.getSymbol());
		return ia;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.SBMLWriter#writeKineticLaw(org.sbml.KineticLaw)
	 */
	public PluginKineticLaw writeKineticLaw(KineticLaw kineticLaw,
			PluginReaction parent) throws SBMLException {
		PluginKineticLaw k = new PluginKineticLaw(parent);
		saveMathContainerProperties(kineticLaw, k);
		for (Parameter p : kineticLaw.getListOfParameters())
			k.addParameter(writeParameter(p));
		return k;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.SBMLWriter#writeModel(org.sbml.Model)
	 */
	public PluginModel writeModel(Model model) {
		return null;
	}

	public PluginModifierSpeciesReference writeModifierSpeciesReference(
			ModifierSpeciesReference modifierSpeciesReference,
			PluginReaction parent) {
		PluginModifierSpeciesReference m = new PluginModifierSpeciesReference(
				parent, writePluginSpeciesAlias(modifierSpeciesReference));
		saveNamedSBaseProperties(modifierSpeciesReference, m);
		return m;
	}

	private PluginSpeciesAlias writePluginSpeciesAlias(
			ModifierSpeciesReference msr) {	
		PluginSpeciesAlias psa = new PluginSpeciesAlias(msr.getSpeciesInstance(),msr.g)
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.SBMLWriter#writeParameter(org.sbml.Parameter)
	 */
	public PluginParameter writeParameter(Parameter parameter) {
		// TODO Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.SBMLWriter#writeReaction(org.sbml.Reaction)
	 */
	public PluginReaction writeReaction(Reaction reaction) throws SBMLException {
		PluginReaction r = new PluginReaction();
		saveNamedSBaseProperties(reaction, r);
		r.setFast(reaction.getFast());
		r.setReversible(reaction.getReversible());
		r.setKineticLaw(writeKineticLaw(reaction.getKineticLaw(), r));
		for (SpeciesReference sr : reaction.getListOfReactants())
			r.addReactant(writeSpeciesReference(sr));
		for (SpeciesReference sr : reaction.getListOfProducts())
			r.addProduct(writeSpeciesReference(sr));
		for (ModifierSpeciesReference mr : reaction.getListOfModifiers())
			r.addModifier(writeModifierSpeciesReference(mr, r));
		return r;
	}

	public PluginRule writeRule(Rule rule) {
		// TODO Auto-generated method stub
		return null;
	}

	public boolean writeSBML(Object sbmlDocument, String filename) {
		// TODO Auto-generated method stub
		return false;
	}

	public PluginSpecies writeSpecies(Species species) {
		// TODO Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.SBMLWriter#writeSpeciesReference(org.sbml.SpeciesReference)
	 */
	public PluginSpeciesReference writeSpeciesReference(
			SpeciesReference speciesReference) {
		// TODO Auto-generated method stub
		return null;
	}

	public PluginSpeciesType writeSpeciesType(SpeciesType speciesType) {
		// TODO Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.sbml.SBMLWriter#writeStoichoimetryMath(org.sbml.StoichiometryMath)
	 */
	public org.sbml.libsbml.StoichiometryMath writeStoichoimetryMath(
			StoichiometryMath stoichiometryMath) {
		// TODO Auto-generated method stub
		return null;
	}

	public ASTNode writeTrigger(Trigger trigger) {
		// TODO Auto-generated method stub
		return null;
	}

	public PluginUnit writeUnit(Unit unit) {
		// TODO Auto-generated method stub
		return null;
	}

	public PluginUnitDefinition writeUnitDefinition(
			UnitDefinition unitDefinition) {
		PluginUnitDefinition ud = new PluginUnitDefinition(unitDefinition
				.getId());
		saveNamedSBaseProperties(unitDefinition, ud);
		for (Unit u : unitDefinition.getListOfUnits())
			ud.addUnit(writeUnit(u));
		return ud;
	}

}
