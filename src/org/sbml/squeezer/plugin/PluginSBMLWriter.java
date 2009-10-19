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

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import jp.sbi.celldesigner.plugin.CellDesignerPlugin;
import jp.sbi.celldesigner.plugin.PluginAlgebraicRule;
import jp.sbi.celldesigner.plugin.PluginAssignmentRule;
import jp.sbi.celldesigner.plugin.PluginCompartment;
import jp.sbi.celldesigner.plugin.PluginCompartmentType;
import jp.sbi.celldesigner.plugin.PluginConstraint;
import jp.sbi.celldesigner.plugin.PluginEvent;
import jp.sbi.celldesigner.plugin.PluginEventAssignment;
import jp.sbi.celldesigner.plugin.PluginFunctionDefinition;
import jp.sbi.celldesigner.plugin.PluginInitialAssignment;
import jp.sbi.celldesigner.plugin.PluginKineticLaw;
import jp.sbi.celldesigner.plugin.PluginListOf;
import jp.sbi.celldesigner.plugin.PluginModel;
import jp.sbi.celldesigner.plugin.PluginModifierSpeciesReference;
import jp.sbi.celldesigner.plugin.PluginParameter;
import jp.sbi.celldesigner.plugin.PluginRateRule;
import jp.sbi.celldesigner.plugin.PluginReaction;
import jp.sbi.celldesigner.plugin.PluginRule;
import jp.sbi.celldesigner.plugin.PluginSBase;
import jp.sbi.celldesigner.plugin.PluginSimpleSpeciesReference;
import jp.sbi.celldesigner.plugin.PluginSpecies;
import jp.sbi.celldesigner.plugin.PluginSpeciesAlias;
import jp.sbi.celldesigner.plugin.PluginSpeciesReference;
import jp.sbi.celldesigner.plugin.PluginSpeciesType;
import jp.sbi.celldesigner.plugin.PluginUnit;
import jp.sbi.celldesigner.plugin.PluginUnitDefinition;
import jp.sbi.celldesigner.plugin.util.PluginCompartmentSymbolType;
import jp.sbi.celldesigner.plugin.util.PluginReactionSymbolType;
import jp.sbi.celldesigner.plugin.util.PluginSpeciesSymbolType;

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
import org.sbml.jsbml.ListOf;
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
import org.sbml.jsbml.SBO;
import org.sbml.jsbml.SBase;
import org.sbml.jsbml.SimpleSpeciesReference;
import org.sbml.jsbml.Species;
import org.sbml.jsbml.SpeciesReference;
import org.sbml.jsbml.SpeciesType;
import org.sbml.jsbml.StoichiometryMath;
import org.sbml.jsbml.Trigger;
import org.sbml.jsbml.Unit;
import org.sbml.jsbml.UnitDefinition;
import org.sbml.jsbml.io.AbstractSBMLWriter;
import org.sbml.jsbml.io.LibSBMLWriter;
import org.sbml.libsbml.ASTNode;
import org.sbml.libsbml.libsbml;
import org.sbml.libsbml.libsbmlConstants;

/**
 * @author Andreas Dr&auml;ger <a
 *         href="mailto:andreas.draeger@uni-tuebingen.de">
 *         andreas.draeger@uni-tuebingen.de</a>
 * 
 */
public class PluginSBMLWriter extends AbstractSBMLWriter {

	private static final String error = " must be an instance of ";
	/**
	 * 
	 */
	private PluginModel pluginModel;
	/**
	 * 
	 */
	private CellDesignerPlugin plugin;

	/**
	 * 
	 * @param plugin
	 */
	public PluginSBMLWriter(CellDesignerPlugin plugin) {
		this.plugin = plugin;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.jsbml.SBMLWriter#convertDate(java.util.Date)
	 */
	public Object convertDate(Date date) {
		// TODO Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.jsbml.SBMLWriter#getNumErrors(java.lang.Object)
	 */
	public int getNumErrors(Object sbase) {
		// TODO Auto-generated method stub
		return 0;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.jsbml.SBMLWriter#getWriteWarnings(java.lang.Object)
	 */
	public List<SBMLException> getWriteWarnings(Object sbase) {
		return new LinkedList<SBMLException>();
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
		pluginModel = (PluginModel) orig;
		int i;

		// Function definitions
		for (FunctionDefinition c : model.getListOfFunctionDefinitions()) {
			if (pluginModel.getFunctionDefinition(c.getId()) == null) {
				PluginFunctionDefinition fd = writeFunctionDefinition(c);
				pluginModel.addFunctionDefinition(fd);
				plugin.notifySBaseAdded(fd);
			} else {
				PluginFunctionDefinition fd = pluginModel
						.getFunctionDefinition(c.getId());
				saveMathContainerProperties(c, fd);
				plugin.notifySBaseChanged(fd);
			}
		}
		// remove unnecessary function definitions
		for (i = pluginModel.getNumFunctionDefinitions() - 1; i >= 0; i--) {
			PluginFunctionDefinition c = pluginModel.getFunctionDefinition(i);
			if (model.getFunctionDefinition(c.getId()) == null)
				plugin.notifySBaseDeleted(pluginModel
						.getListOfFunctionDefinitions().remove(i));
		}

		// Unit definitions
		for (UnitDefinition ud : model.getListOfUnitDefinitions())
			if (!ud.equals(UnitDefinition.substance(ud.getLevel(), ud
					.getVersion()))
					&& !ud.equals(UnitDefinition.volume(ud.getLevel(), ud
							.getVersion()))
					&& !ud.equals(UnitDefinition.area(ud.getLevel(), ud
							.getVersion()))
					&& !ud.equals(UnitDefinition.length(ud.getLevel(), ud
							.getVersion()))
					&& !ud.equals(UnitDefinition.time(ud.getLevel(), ud
							.getVersion()))) {
				PluginUnitDefinition libU = pluginModel.getUnitDefinition(ud
						.getId());
				if (libU != null) {
					saveSBaseProperties(ud, libU);
					for (Unit u : ud.getListOfUnits()) {
						boolean contains = false;
						for (int j = 0; j < libU.getNumUnits() && !contains; j++) {
							if (equal(u, libU.getUnit(j)))
								contains = true;
						}
						if (!contains) {
							PluginUnit unit = writeUnit(u, libU);
							libU.addUnit(unit);
							plugin.notifySBaseAdded(unit);
						}
					}
					plugin.notifySBaseChanged(libU);
				} else {
					PluginUnitDefinition udef = writeUnitDefinition(ud);
					pluginModel.addUnitDefinition(udef);
					plugin.notifySBaseAdded(udef);
				}
			}
		// remove unnecessary units
		for (i = pluginModel.getNumUnitDefinitions() - 1; i >= 0; i--) {
			PluginUnitDefinition ud = pluginModel.getUnitDefinition(i);
			if (model.getUnitDefinition(ud.getId()) == null)
				plugin.notifySBaseDeleted(pluginModel
						.getListOfUnitDefinitions().remove(i));
		}

		// Compartment types
		for (CompartmentType c : model.getListOfCompartmentTypes()) {
			if (pluginModel.getCompartmentType(c.getId()) == null) {
				PluginCompartmentType ct = writeCompartmentType(c);
				pluginModel.addCompartmentType(ct);
				plugin.notifySBaseAdded(ct);
			} else {
				PluginCompartmentType ct = pluginModel.getCompartmentType(c
						.getId());
				saveSBaseProperties(c, ct);
				plugin.notifySBaseChanged(ct);
			}
		}
		// remove unnecessary compartmentTypes
		for (i = pluginModel.getNumCompartmentTypes() - 1; i >= 0; i--) {
			PluginCompartmentType c = pluginModel.getCompartmentType(i);
			if (model.getCompartmentType(c.getId()) == null)
				plugin.notifySBaseDeleted(pluginModel
						.getListOfCompartmentTypes().remove(i));
		}

		// Species types
		for (SpeciesType c : model.getListOfSpeciesTypes()) {
			if (pluginModel.getSpeciesType(c.getId()) == null) {
				PluginSpeciesType st = writeSpeciesType(c);
				pluginModel.addSpeciesType(st);
				plugin.notifySBaseAdded(st);
			} else {
				PluginSpeciesType st = pluginModel.getSpeciesType(c.getId());
				saveSBaseProperties(c, st);
				plugin.notifySBaseChanged(st);
			}
		}
		// remove unnecessary speciesTypes
		for (i = pluginModel.getNumSpeciesTypes() - 1; i >= 0; i--) {
			PluginSpeciesType c = pluginModel.getSpeciesType(i);
			if (model.getSpeciesType(c.getId()) == null)
				plugin.notifySBaseDeleted(pluginModel.getListOfSpeciesTypes()
						.remove(i));
		}

		// Compartments
		for (Compartment c : model.getListOfCompartments()) {
			if (pluginModel.getCompartment(c.getId()) == null) {
				PluginCompartment pc = writeCompartment(c);
				pluginModel.addCompartment(pc);
				plugin.notifySBaseAdded(pc);
			} else {
				PluginCompartment pc = pluginModel.getCompartment(c.getId());
				saveCompartmentProperties(c, pc);
				plugin.notifySBaseChanged(pc);
			}
		}
		// remove unnecessary compartments
		for (i = pluginModel.getNumCompartments() - 1; i >= 0; i--) {
			PluginCompartment c = pluginModel.getCompartment(i);
			if (model.getCompartment(c.getId()) == null)
				plugin.notifySBaseDeleted(pluginModel.getListOfCompartments()
						.remove(i));
		}

		// Species
		for (Species s : model.getListOfSpecies()) {
			if (pluginModel.getSpecies(s.getId()) == null) {
				PluginSpecies ps = writeSpecies(s);
				pluginModel.addSpecies(ps);
				plugin.notifySBaseAdded(ps);
			} else {
				PluginSpecies ps = pluginModel.getSpecies(s.getId());
				saveSpeciesProperties(s, ps);
				plugin.notifySBaseChanged(ps);
			}
		}
		// remove unnecessary species
		for (i = pluginModel.getNumSpecies() - 1; i >= 0; i--) {
			PluginSpecies s = pluginModel.getSpecies(i);
			if (model.getSpecies(s.getId()) == null)
				plugin.notifySBaseDeleted(pluginModel.getListOfSpecies()
						.remove(i));
		}

		// add or change parameters
		for (Parameter p : model.getListOfParameters()) {
			if (pluginModel.getParameter(p.getId()) == null) {
				PluginParameter pp = writeParameter(p, pluginModel);
				pluginModel.addParameter(pp);
				plugin.notifySBaseAdded(pp);
			} else {
				PluginParameter pp = pluginModel.getParameter(p.getId());
				saveParameterProperties(p, pp);
				plugin.notifySBaseChanged(pp);
			}
		}
		// remove parameters
		for (i = pluginModel.getNumParameters() - 1; i >= 0; i--) {
			PluginParameter p = pluginModel.getParameter(i);
			if (model.getParameter(p.getId()) == null)
				plugin.notifySBaseDeleted(pluginModel.getListOfParameters()
						.remove(i));
		}

		// initial assignments
		for (i = 0; i < model.getNumInitialAssignments(); i++) {
			InitialAssignment ia = model.getInitialAssignment(i);
			int contains = -1;
			for (int j = 0; j < pluginModel.getNumInitialAssignments()
					&& contains < 0; j++) {
				PluginInitialAssignment libIA = pluginModel
						.getInitialAssignment(j);
				if (libIA.getSymbol().equals(ia.getSymbol())
						&& equal(ia.getMath(), libsbml.parseFormula(libIA
								.getMath())))
					contains = j;
			}
			if (contains < 0) {
				PluginInitialAssignment pia = writeInitialAssignment(ia);
				pluginModel.addInitialAssignment(pia);
				plugin.notifySBaseAdded(pia);
			} else {
				PluginInitialAssignment pia = pluginModel
						.getInitialAssignment(contains);
				saveMathContainerProperties(ia, pia);
				plugin.notifySBaseChanged(pia);
			}
		}
		// remove unnecessary initial assignments
		for (i = pluginModel.getNumInitialAssignments() - 1; i >= 0; i--) {
			PluginInitialAssignment c = pluginModel.getInitialAssignment(i);
			boolean contains = false;
			for (int j = 0; j < model.getNumInitialAssignments() && !contains; j++) {
				InitialAssignment ia = model.getInitialAssignment(j);
				if (ia.getSymbol().equals(c.getSymbol())
						&& equal(ia.getMath(), libsbml
								.parseFormula(c.getMath())))
					contains = true;
			}
			if (!contains)
				plugin.notifySBaseDeleted(pluginModel
						.getListOfInitialAssignments().remove(i));
		}

		// rules
		for (i = 0; i < model.getNumRules(); i++) {
			Rule rule = model.getRule(i);
			int contains = -1;
			for (int j = 0; j < pluginModel.getNumRules() && contains < 0; j++) {
				boolean equal = false;
				PluginRule ruleOrig = pluginModel.getRule(j);
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
			if (contains < 0) {
				PluginRule r = writeRule(rule);
				pluginModel.addRule(r);
				plugin.notifySBaseAdded(r);
			} else {
				// math is equal anyway...
				PluginRule r = pluginModel.getRule(contains);
				saveSBaseProperties(rule, r);
				plugin.notifySBaseChanged(r);
			}
		}

		// remove unnecessary rules
		for (i = pluginModel.getNumRules() - 1; i >= 0; i--) {
			PluginRule c = pluginModel.getRule(i);
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
				plugin.notifySBaseDeleted(pluginModel.getListOfRules()
						.remove(i));
		}

		// constraints
		for (i = 0; i < model.getNumConstraints(); i++) {
			Constraint ia = model.getConstraint(i);
			int contains = -1;
			for (int j = 0; j < pluginModel.getNumConstraints() && contains < 0; j++) {
				PluginConstraint c = pluginModel.getConstraint(j);
				if (equal(ia.getMath(), libsbml.parseFormula(c.getMath())))
					contains = j;
			}
			if (contains < 0) {
				PluginConstraint constr = writeConstraint(ia);
				pluginModel.addConstraint(constr);
				plugin.notifySBaseAdded(constr);
			} else {
				PluginConstraint constr = pluginModel.getConstraint(contains);
				saveMathContainerProperties(ia, constr);
				plugin.notifySBaseChanged(constr);
			}
		}
		// remove unnecessary constraints
		for (i = pluginModel.getNumConstraints() - 1; i >= 0; i--) {
			PluginConstraint c = pluginModel.getConstraint(i);
			boolean contains = false;
			for (int j = 0; j < model.getNumConstraints() && !contains; j++) {
				Constraint ia = model.getConstraint(j);
				if (equal(ia.getMath(), libsbml.parseFormula(c.getMath())))
					contains = true;
			}
			if (!contains)
				plugin.notifySBaseDeleted(pluginModel.getListOfConstraints()
						.remove(i));
		}

		// add or change reactions
		for (Reaction r : model.getListOfReactions()) {
			if (pluginModel.getReaction(r.getId()) == null) {
				PluginReaction reac = writeReaction(r);
				pluginModel.addReaction(reac);
				plugin.notifySBaseAdded(reac);
			} else {
				PluginReaction reac = pluginModel.getReaction(r.getId());
				saveReactionProperties(r, reac);
				plugin.notifySBaseChanged(reac);
			}
		}
		// remove reactions
		for (i = pluginModel.getNumReactions() - 1; i >= 0; i--) {
			PluginReaction r = pluginModel.getReaction(i);
			if (model.getReaction(r.getId()) == null)
				plugin.notifySBaseChanged(pluginModel.getListOfReactions()
						.remove(i));
		}

		// events
		for (i = 0; i < model.getNumEvents(); i++) {
			Event ia = model.getEvent(i);
			int contains = -1;
			for (int j = 0; j < pluginModel.getNumEvents() && contains < 0; j++) {
				PluginEvent libIA = pluginModel.getEvent(j);
				if (libIA.getId().equals(ia.getId())
						|| libIA.getName().equals(ia.getName())
						|| libIA.getNotesString().equals(ia.getNotesString()))
					contains = j;
			}
			if (contains < 0) {
				PluginEvent pia = writeEvent(ia);
				pluginModel.addEvent(pia);
				plugin.notifySBaseAdded(pia);
			} else {
				PluginEvent pia = pluginModel.getEvent(contains);
				saveEventProperties(ia, pia);
				plugin.notifySBaseChanged(pia);
			}
		}
		// remove events
		for (i = pluginModel.getNumEvents() - 1; i >= 0; i--) {
			PluginEvent eventOrig = pluginModel.getEvent(i);
			boolean contains = false;
			for (Event e : model.getListOfEvents())
				if (e.getId().equals(eventOrig)
						|| e.getName().equals(eventOrig.getName())
						|| e.getNotesString()
								.equals(eventOrig.getNotesString()))
					contains = true;
			if (!contains)
				plugin.notifySBaseDeleted(pluginModel.getListOfEvents().remove(
						i));
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seeorg.sbml.jsbml.SBMLWriter#saveCompartmentProperties(org.sbml.jsbml.
	 * Compartment, java.lang.Object)
	 */
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.sbml.jsbml.SBMLWriter#saveCVTermProperties(org.sbml.jsbml.CVTerm,
	 * java.lang.Object)
	 */
	public void saveCVTermProperties(CVTerm cvt, Object term) {
		// TODO Auto-generated method stub
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.jsbml.SBMLWriter#saveEventProperties(org.sbml.jsbml.Event,
	 * java.lang.Object)
	 */
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
			if (e.getDelay() != null) {
				e.setDelay(writeDelay(ev.getDelay()));
			} else
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
			int contains = -1;
			for (int i = 0; i < e.getNumEventAssignments() && contains < 0; i++) {
				PluginEventAssignment libEA = e.getEventAssignment(i);
				if (libEA.getVariable().equals(ea.getVariable())
						&& equal(ea.getMath(), libEA.getMath()))
					contains = i;
			}
			if (contains < 0) {
				PluginEventAssignment pev = writeEventAssignment(ea,
						(PluginEvent) event);
				e.addEventAssignment(pev);
				plugin.notifySBaseAdded(pev);
			} else {
				PluginEventAssignment pev = e.getEventAssignment(contains);
				saveMathContainerProperties(ea, pev);
				plugin.notifySBaseChanged(pev);
			}
		}
		// remove unnecessary event assignments
		for (int i = e.getNumEventAssignments() - 1; i >= 0; i--) {
			PluginEventAssignment ea = e.getEventAssignment(i);
			boolean contains = false;
			for (int j = 0; j < ev.getNumEventAssignments() && !contains; j++) {
				EventAssignment eventA = ev.getEventAssignment(j);
				if (eventA.isSetVariable()
						&& eventA.getVariable().equals(ea.getVariable())
						&& equal(eventA.getMath(), ea.getMath()))
					contains = true;
			}
			if (!contains) {
				PluginEventAssignment pev = e.getEventAssignment(i);
				e.removeEventAssignment(i);
				plugin.notifySBaseDeleted(pev);
			}
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.sbml.jsbml.SBMLWriter#saveKineticLawProperties(org.sbml.jsbml.KineticLaw
	 * , java.lang.Object)
	 */
	public void saveKineticLawProperties(KineticLaw kl, Object kineticLaw)
			throws SBMLException {
		if (!(kineticLaw instanceof PluginKineticLaw))
			throw new IllegalArgumentException(
					"kineticLaw must be an instance of PluginKineticLaw.");
		PluginKineticLaw libKinLaw = (PluginKineticLaw) kineticLaw;
		// add or change parameters
		saveSBaseProperties(kl, libKinLaw);
		int para = 0;
		for (Parameter p : kl.getListOfParameters()) {
			PluginParameter libParam = libKinLaw.getParameter(para);
			para++;
			if (libParam == null) {
				PluginParameter pp = writeParameter(p, libKinLaw);
				libKinLaw.addParameter(pp);
				plugin.notifySBaseAdded(pp);
			} else {
				saveParameterProperties(p, libParam);
				plugin.notifySBaseChanged(libParam);
			}

		}
		// remove parameters
		for (int i = libKinLaw.getNumParameters() - 1; i >= 0; i--) {
			PluginParameter p = libKinLaw.getParameter(i);
			if (kl.getParameter(p.getId()) == null)
				plugin.notifySBaseDeleted(libKinLaw.getListOfParameters()
						.remove(i));
		}
		saveMathContainerProperties(kl, libKinLaw);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.sbml.jsbml.SBMLWriter#saveMathContainerProperties(org.sbml.jsbml.
	 * MathContainer, java.lang.Object)
	 */
	public void saveMathContainerProperties(MathContainer mc, Object sbase)
			throws SBMLException {
		if (mc instanceof NamedSBase)
			saveSBaseProperties((SBase) mc, sbase);
		else
			saveSBaseProperties(mc, sbase);
		if (sbase instanceof PluginConstraint) {
			PluginConstraint kl = (PluginConstraint) sbase;
			boolean equal = (kl.getMath() != null) && mc.isSetMath()
					&& equal(mc.getMath(), libsbml.parseFormula(kl.getMath()));
			if (mc.isSetMath() && !equal)
				throw new SBMLException("Unable to set math of "
						+ mc.getClass().getSimpleName() + " in "
						+ kl.getClass().getName());

		} else if (sbase instanceof PluginEventAssignment) {
			PluginEventAssignment kl = (PluginEventAssignment) sbase;
			boolean equal = (kl.getMath() != null) && mc.isSetMath()
					&& equal(mc.getMath(), kl.getMath());
			if (mc.isSetMath() && !equal)
				kl.setMath(convert(mc.getMath()));

		} else if (sbase instanceof PluginFunctionDefinition) {
			PluginFunctionDefinition kl = (PluginFunctionDefinition) sbase;
			boolean equal = (kl.getMath() != null) && mc.isSetMath()
					&& equal(mc.getMath(), kl.getMath());
			if (mc.isSetMath() && !equal)
				kl.setMath(convert(mc.getMath()));

		} else if (sbase instanceof PluginInitialAssignment) {
			PluginInitialAssignment kl = (PluginInitialAssignment) sbase;
			boolean equal = (kl.getMath() != null) && mc.isSetMath()
					&& equal(mc.getMath(), libsbml.parseFormula(kl.getMath()));
			if (mc.isSetMath() && !equal)
				kl.setMath(libsbml.formulaToString(convert(mc.getMath())));

		} else if (sbase instanceof PluginKineticLaw) {
			PluginKineticLaw kl = (PluginKineticLaw) sbase;
			boolean equal = (kl.getMath() != null) && mc.isSetMath()
					&& equal(mc.getMath(), kl.getMath());
			if (mc.isSetMath() && !equal)
				kl.setMath(convert(mc.getMath()));

		} else if (sbase instanceof PluginRule) {
			PluginRule kl = (PluginRule) sbase;
			boolean equal = (kl.getMath() != null) && mc.isSetMath()
					&& equal(mc.getMath(), kl.getMath());
			if (mc.isSetMath() && !equal)
				kl.setMath(convert(mc.getMath()));
		}
		// else if (sbase instanceof PluginStoichiometryMath) {
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

	/*
	 * (non-Javadoc)
	 * 
	 * @seeorg.sbml.jsbml.SBMLWriter#saveModelHistoryProperties(org.sbml.jsbml.
	 * ModelHistory, java.lang.Object)
	 */
	public void saveModelHistoryProperties(ModelHistory mh, Object modelHistory) {
		// TODO Auto-generated method stub
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.sbml.jsbml.SBMLWriter#saveModifierSpeciesReferenceProperties(org.
	 * sbml.jsbml.ModifierSpeciesReference, java.lang.Object)
	 */
	public void saveModifierSpeciesReferenceProperties(
			ModifierSpeciesReference modifierSpeciesReference, Object msr) {
		if (!(msr instanceof PluginModifierSpeciesReference))
			throw new IllegalArgumentException("modifierSpeciesReference"
					+ error + "PluginModifierSpeciesReference.");
		saveNamedSBaseProperties(modifierSpeciesReference, msr);
		PluginModifierSpeciesReference pmsr = (PluginModifierSpeciesReference) msr;
		if (modifierSpeciesReference.isSetSBOTerm()) {
			String type = SBO.convertSBO2Alias(modifierSpeciesReference
					.getSBOTerm());
			if (type.length() > 0)
				pmsr.setModificationType(type);
			else
				pmsr.setModificationType(PluginReactionSymbolType.MODULATION);
		}
		// if (modifierSpeciesReference.isSetSpecies() &&
		// !modifierSpeciesReference.getSpecies().equals(pmsr.getSpecies()))
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.sbml.jsbml.SBMLWriter#saveNamedSBaseProperties(org.sbml.jsbml.NamedSBase
	 * , java.lang.Object)
	 */
	public void saveNamedSBaseProperties(NamedSBase nsb, Object sb) {
		saveSBaseProperties(nsb, sb);
		if (sb instanceof PluginCompartmentType) {
			PluginCompartmentType pt = (PluginCompartmentType) sb;
			// if (nsb.isSetId() && !pt.getId().equals(nsb.getId()))
			// pt.setId(nsb.getId());
			if (nsb.isSetName() && !pt.getName().equals(nsb.getName()))
				pt.setName(nsb.getName());
		} else if (sb instanceof PluginEvent) {
			PluginEvent pt = (PluginEvent) sb;
			// if (nsb.isSetId() && !pt.getId().equals(nsb.getId()))
			// pt.setId(nsb.getId());
			if (nsb.isSetName() && !pt.getName().equals(nsb.getName()))
				pt.setName(nsb.getName());
		} else if (sb instanceof PluginModel) {
			PluginModel pt = (PluginModel) sb;
			// if (nsb.isSetId() && !pt.getId().equals(nsb.getId()))
			// pt.setId(nsb.getId());
			if (nsb.isSetName() && !pt.getName().equals(nsb.getName()))
				pt.setName(nsb.getName());
		} else if (sb instanceof PluginReaction) {
			PluginReaction pt = (PluginReaction) sb;
			// if (nsb.isSetId() && !pt.getId().equals(nsb.getId()))
			// pt.setId(nsb.getId());
			if (nsb.isSetName() && !pt.getName().equals(nsb.getName()))
				pt.setName(nsb.getName());
		} else if (sb instanceof PluginSimpleSpeciesReference) {
			// PluginSimpleSpeciesReference pt = (PluginSimpleSpeciesReference)
			// sb;
			// if (nsb.isSetId() && !pt.getId().equals(nsb.getId()))
			// pt.setId(nsb.getId());
			// if (nsb.isSetName() && !pt.getName().equals(nsb.getName()))
			// pt.setName(nsb.getName());
			if (sb instanceof PluginModifierSpeciesReference) {
			} else if (sb instanceof PluginSpeciesReference) {
			}
		} else if (sb instanceof PluginSpeciesType) {
			PluginSpeciesType pt = (PluginSpeciesType) sb;
			// if (nsb.isSetId() && !pt.getId().equals(nsb.getId()))
			// pt.setId(nsb.getId());
			if (nsb.isSetName() && !pt.getName().equals(nsb.getName()))
				pt.setName(nsb.getName());
		} else if (sb instanceof PluginCompartment) {
			PluginCompartment pt = (PluginCompartment) sb;
			// if (nsb.isSetId() && !pt.getId().equals(nsb.getId()))
			// pt.setId(nsb.getId());
			if (nsb.isSetName() && !pt.getName().equals(nsb.getName()))
				pt.setName(nsb.getName());
		} else if (sb instanceof PluginParameter) {
			PluginParameter pt = (PluginParameter) sb;
			if (nsb.isSetId() && !pt.getId().equals(nsb.getId()))
				pt.setId(nsb.getId());
			if (nsb.isSetName() && !pt.getName().equals(nsb.getName()))
				pt.setName(nsb.getName());
		} else if (sb instanceof PluginSpecies) {
			// PluginSpecies pt = (PluginSpecies) sb;
			// if (nsb.isSetId() && !pt.getId().equals(nsb.getId()))
			// pt.setId(nsb.getId());
			// if (nsb.isSetName() && !pt.getName().equals(nsb.getName()))
			// pt.setName(nsb.getName());
		} else if (sb instanceof PluginUnitDefinition) {
			PluginUnitDefinition pt = (PluginUnitDefinition) sb;
			// if (nsb.isSetId() && !pt.getId().equals(nsb.getId()))
			// pt.setId(nsb.getId());
			if (nsb.isSetName() && !pt.getName().equals(nsb.getName()))
				pt.setName(nsb.getName());
		} else if (sb instanceof PluginFunctionDefinition) {

		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.sbml.jsbml.SBMLWriter#saveParameterProperties(org.sbml.jsbml.Parameter
	 * , java.lang.Object)
	 */
	public void saveParameterProperties(Parameter p, Object parameter) {
		if (!(parameter instanceof PluginParameter))
			throw new IllegalArgumentException("parameter" + error
					+ "PluginParameter.");
		PluginParameter po = (PluginParameter) parameter;
		saveNamedSBaseProperties(p, po);
		if (p.isSetValue() && p.getValue() != po.getValue())
			po.setValue(p.getValue());
		if (p.getConstant() != po.getConstant())
			po.setConstant(p.getConstant());
		if (p.isSetUnits() && !p.getUnits().equals(po.getUnits()))
			po.setUnits(p.getUnits());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.jsbml.SBMLWriter#saveSBaseProperties(org.sbml.jsbml.SBase,
	 * java.lang.Object)
	 */

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.sbml.jsbml.SBMLWriter#saveReactionProperties(org.sbml.jsbml.Reaction,
	 * java.lang.Object)
	 */
	public void saveReactionProperties(Reaction r, Object reaction)
			throws SBMLException {
		if (!(reaction instanceof PluginReaction))
			throw new IllegalArgumentException("reaction" + error
					+ "PluginReaction.");
		PluginReaction ro = (PluginReaction) reaction;
		saveNamedSBaseProperties(r, ro);
		if (r.getFast() != ro.getFast())
			ro.setFast(r.getFast());
		if (r.getReversible() != ro.getReversible())
			ro.setReversible(r.getReversible());
		saveListOfProperties(r.getListOfReactants(), ro, (short) 0);
		saveListOfProperties(r.getListOfProducts(), ro, (short) 1);
		saveListOfProperties(r.getListOfModifiers(), ro, (short) 2);
		if (r.isSetKineticLaw()) {
			if (ro.getKineticLaw() == null) {
				PluginKineticLaw plukin = writeKineticLaw(r.getKineticLaw(),
						(PluginReaction) reaction);
				ro.setKineticLaw(plukin);
				plugin.notifySBaseAdded(plukin);
			} else {
				PluginKineticLaw plukin = ro.getKineticLaw();
				saveKineticLawProperties(r.getKineticLaw(), plukin);
				plugin.notifySBaseChanged(plukin);
			}
		} else if (ro.getKineticLaw() != null) {
			PluginKineticLaw plukin = ro.getKineticLaw();
			ro.setKineticLaw(null);
			plugin.notifySBaseDeleted(plukin);
		}
	}

	/**
	 * 
	 * @param listOf
	 * @param ro
	 * @param reactant
	 * @throws SBMLException
	 */
	private void saveListOfProperties(
			ListOf<? extends SimpleSpeciesReference> listOf, PluginReaction ro,
			short reactant) throws SBMLException {
		int i, contains;
		for (SimpleSpeciesReference sr : listOf) {
			contains = -1;
			for (i = 0; i < ro.getNumReactants() && contains < 0; i++)
				if (sr.getSpecies().equals(ro.getReactant(i).getSpecies()))
					contains = i;
			if (contains < 0) {
				if (sr instanceof SpeciesReference) {
					String type;
					if (reactant == 0)
						type = PluginSpeciesReference.REACTANT;
					else if (reactant == 1)
						type = PluginSpeciesReference.PRODUCT;
					else
						type = PluginSpeciesReference.MODIFIER;
					PluginSpeciesReference psr = writeSpeciesReference(
							(SpeciesReference) sr, ro, type);
					if (reactant == 0)
						ro.addReactant(psr);
					else if (reactant == 1)
						ro.addProduct(psr);
					plugin.notifySBaseAdded(psr);
				} else {
					PluginModifierSpeciesReference pmsr = writeModifierSpeciesReference(
							(ModifierSpeciesReference) sr, ro, pluginModel);
					ro.addModifier(pmsr);
					plugin.notifySBaseAdded(pmsr);
				}
			} else {
				if (sr instanceof SpeciesReference) {
					PluginSpeciesReference psr = reactant == 0 ? ro
							.getReactant(contains) : ro.getProduct(contains);
					saveSpeciesReferenceProperties((SpeciesReference) sr, psr);
					plugin.notifySBaseChanged(psr);
				} else if (sr instanceof ModifierSpeciesReference) {
					PluginModifierSpeciesReference pmsr = ro
							.getModifier(contains);
					saveModifierSpeciesReferenceProperties(
							(ModifierSpeciesReference) sr, pmsr);
					plugin.notifySBaseChanged(pmsr);
				}
			}
		}
		// remove unnecessary reactants.
		PluginListOf plo;
		if (reactant == 0)
			plo = ro.getListOfReactants();
		else if (reactant == 1)
			plo = ro.getListOfProducts();
		else
			plo = ro.getListOfModifiers();
		for (i = plo.size() - 1; i >= 0; i--) {
			PluginSimpleSpeciesReference roreactant = (PluginSimpleSpeciesReference) plo
					.get(i);
			boolean keep = false;
			for (int j = 0; j < listOf.size() && !keep; j++)
				if (listOf.get(j).getSpecies().equals(roreactant.getSpecies()))
					keep = true;
			if (!keep)
				plugin.notifySBaseDeleted(plo.remove(i));
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.jsbml.SBMLWriter#saveSBaseProperties(org.sbml.jsbml.SBase,
	 * java.lang.Object)
	 */
	public void saveSBaseProperties(SBase s, Object sb) {
		if (!(sb instanceof PluginSBase))
			throw new IllegalArgumentException("sb" + error + "PluginSBase");
		PluginSBase plusbas = (PluginSBase) sb;
		plusbas.setNotes(s.getNotesString());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.sbml.jsbml.SBMLWriter#saveSpeciesProperties(org.sbml.jsbml.Species,
	 * java.lang.Object)
	 */
	public void saveSpeciesProperties(Species s, Object species) {
		if (!(species instanceof PluginSpecies))
			throw new IllegalArgumentException("species" + error
					+ "PluginSpecies");
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
		if (s.isSetSBOTerm()) {
			String type = SBO.convertSBO2Alias(s.getSBOTerm());
			if (type.length() > 0)
				spec.getSpeciesAlias(0).setType(type);
		}
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.sbml.jsbml.SBMLWriter#writeCompartment(org.sbml.jsbml.Compartment)
	 */
	public PluginCompartment writeCompartment(Compartment compartment) {
		PluginCompartment c = new PluginCompartment(
				PluginCompartmentSymbolType.SQUARE);
		saveCompartmentProperties(compartment, c);
		return c;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.sbml.jsbml.SBMLWriter#writeCompartmentType(org.sbml.jsbml.CompartmentType
	 * )
	 */
	public PluginCompartmentType writeCompartmentType(
			CompartmentType compartmentType) {
		PluginCompartmentType ct = new PluginCompartmentType(compartmentType
				.getId());
		saveNamedSBaseProperties(compartmentType, ct);
		return ct;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.jsbml.SBMLWriter#writeConstraint(org.sbml.jsbml.Constraint)
	 */
	public PluginConstraint writeConstraint(Constraint constraint) {
		PluginConstraint c = new PluginConstraint(libsbml
				.formulaToString(convert(constraint.getMath())));
		saveSBaseProperties(constraint, c);
		if (constraint.isSetMessage())
			c.setMessage(constraint.getMessage());
		return c;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.jsbml.SBMLWriter#writeCVTerm(org.sbml.jsbml.CVTerm)
	 */
	public Object writeCVTerm(CVTerm cvt) {
		// TODO Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.jsbml.SBMLWriter#writeDelay(org.sbml.jsbml.Delay)
	 */
	public ASTNode writeDelay(Delay delay) {
		return convert(delay.getMath());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.jsbml.SBMLWriter#writeEvent(org.sbml.jsbml.Event)
	 */
	public PluginEvent writeEvent(Event event) throws SBMLException {
		PluginEvent e = new PluginEvent(event.getId());
		saveEventProperties(event, e);
		return e;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.sbml.jsbml.SBMLWriter#writeEventAssignment(org.sbml.jsbml.EventAssignment
	 * , java.lang.Object[])
	 */
	public PluginEventAssignment writeEventAssignment(
			EventAssignment eventAssignment, Object... ev) throws SBMLException {
		if (ev.length != 1 || !(ev[0] instanceof PluginEvent))
			throw new IllegalArgumentException(
					"parent must be of type PluginEvent!");
		PluginEventAssignment ea = new PluginEventAssignment(
				(PluginEvent) ev[0]);
		saveMathContainerProperties(eventAssignment, ea);
		if (eventAssignment.isSetVariable())
			ea.setVariable(eventAssignment.getVariable());
		return ea;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seeorg.sbml.jsbml.SBMLWriter#writeFunctionDefinition(org.sbml.jsbml.
	 * FunctionDefinition)
	 */
	public PluginFunctionDefinition writeFunctionDefinition(
			FunctionDefinition functionDefinition) throws SBMLException {
		PluginFunctionDefinition fd = new PluginFunctionDefinition(
				functionDefinition.getId());
		saveNamedSBaseProperties(functionDefinition, fd);
		saveMathContainerProperties(functionDefinition, fd);
		return fd;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seeorg.sbml.jsbml.SBMLWriter#writeInitialAssignment(org.sbml.jsbml.
	 * InitialAssignment)
	 */
	public PluginInitialAssignment writeInitialAssignment(
			InitialAssignment initialAssignment) throws SBMLException {
		PluginInitialAssignment ia = new PluginInitialAssignment(
				initialAssignment.getSymbol());
		saveSBaseProperties(initialAssignment, ia);
		saveMathContainerProperties(initialAssignment, ia);
		return ia;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.SBMLWriter#writeKineticLaw(org.sbml.KineticLaw)
	 */
	public PluginKineticLaw writeKineticLaw(KineticLaw kineticLaw,
			Object... parent) throws SBMLException {
		if (parent.length != 1 || !(parent[0] instanceof PluginReaction))
			throw new IllegalArgumentException("parent" + error
					+ "PluginReaction");
		PluginKineticLaw k = new PluginKineticLaw((PluginReaction) parent[0]);
		saveKineticLawProperties(kineticLaw, k);
		return k;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.SBMLWriter#writeModel(org.sbml.Model)
	 */
	public PluginModel writeModel(Model model) throws SBMLException {
		LibSBMLWriter writer = new LibSBMLWriter();
		pluginModel = new PluginModel((org.sbml.libsbml.Model) writer
				.writeModel(model));
		saveChanges(model, pluginModel);
		return pluginModel;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.sbml.jsbml.SBMLWriter#writeModifierSpeciesReference(org.sbml.jsbml
	 * .ModifierSpeciesReference, java.lang.Object[])
	 */
	public PluginModifierSpeciesReference writeModifierSpeciesReference(
			ModifierSpeciesReference modifierSpeciesReference, Object... parent) {
		if (parent.length != 2 || !(parent[0] instanceof PluginReaction)
				|| !(parent[1] instanceof PluginModel))
			throw new IllegalArgumentException(
					"a PluginReaction and a PluginModel must be provided");
		PluginReaction pluReac = (PluginReaction) parent[0];
		PluginModel pluMod = (PluginModel) parent[1];
		String modificationType;
		if (modifierSpeciesReference.isSetSBOTerm())
			modificationType = SBO.convertSBO2Alias(modifierSpeciesReference
					.getSBOTerm());
		else
			modificationType = PluginReactionSymbolType.MODULATION;
		PluginModifierSpeciesReference m = new PluginModifierSpeciesReference(
				pluReac, new PluginSpeciesAlias(pluMod
						.getSpecies(modifierSpeciesReference.getSpecies()),
						modificationType));
		saveModifierSpeciesReferenceProperties(modifierSpeciesReference, m);
		return m;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.SBMLWriter#writeParameter(org.sbml.Parameter)
	 */
	public PluginParameter writeParameter(Parameter parameter, Object... parent) {
		if (parent.length != 1
				|| !((parent[0] instanceof PluginKineticLaw) || (parent[0] instanceof PluginModel)))
			throw new IllegalArgumentException("parent" + error
					+ "PluginKineticLaw or PluginModel");
		PluginParameter p;
		if (parent[0] instanceof PluginKineticLaw)
			p = new PluginParameter((PluginKineticLaw) parent[0]);
		else
			p = new PluginParameter((PluginModel) parent[0]);
		saveParameterProperties(parameter, p);
		return p;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.SBMLWriter#writeReaction(org.sbml.Reaction)
	 */
	public PluginReaction writeReaction(Reaction reaction) throws SBMLException {
		PluginReaction r = new PluginReaction();
		saveReactionProperties(reaction, r);
		return r;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.jsbml.SBMLWriter#writeRule(org.sbml.jsbml.Rule,
	 * java.lang.Object[])
	 */
	public PluginRule writeRule(Rule rule, Object... parent) {
		if (parent.length != 1 || !(parent[0] instanceof PluginModel))
			throw new IllegalArgumentException(
					"parent must be of type PluginModel!");
		PluginRule r;
		if (rule.isAlgebraic()) {
			r = new PluginAlgebraicRule((PluginModel) parent[0]);
		} else {
			if (rule.isAssignment()) {
				r = new PluginAssignmentRule((PluginModel) parent[0]);
				if (((AssignmentRule) rule).isSetVariable())
					((PluginAssignmentRule) r)
							.setVariable(((AssignmentRule) rule).getVariable());
			} else {
				r = new PluginRateRule((PluginModel) parent[0]);
				if (((RateRule) rule).isSetVariable())
					((PluginRateRule) r).setVariable(((RateRule) rule)
							.getVariable());
			}
		}
		if (rule.isSetMath())
			r.setMath(convert(rule.getMath()));
		saveSBaseProperties(rule, r);
		if (rule.getFormula() != null)
			r.setFormula(rule.getFormula());
		return r;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.jsbml.SBMLWriter#writeSBML(java.lang.Object,
	 * java.lang.String)
	 */
	public boolean writeSBML(Object sbmlModel, String filename)
			throws IOException, SBMLException {
		return writeSBML(sbmlModel, filename, null, null);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.jsbml.SBMLWriter#writeSBML(java.lang.Object,
	 * java.lang.String, java.lang.String, java.lang.String)
	 */
	public boolean writeSBML(Object sbmlModel, String filename,
			String programName, String versionNumber) throws SBMLException,
			IOException {
		if (!(sbmlModel instanceof PluginSBase))
			throw new IllegalArgumentException("sbmlModel" + error
					+ "PluginSBase");
		PluginSBase sbase = (PluginSBase) sbmlModel;
		BufferedWriter bw = new BufferedWriter(new FileWriter(filename));
		if (programName != null || versionNumber != null) {
			bw.append("<!-- ");
			if (programName != null) {
				bw.append("created by ");
				bw.append(programName);
				if (versionNumber != null)
					bw.append(' ');
			}
			if (versionNumber != null) {
				bw.append("version ");
				bw.append(versionNumber);
			}
			bw.append(" -->");
			bw.newLine();
		}
		bw.append(sbase.toSBML());
		bw.close();
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.jsbml.SBMLWriter#writeSpecies(org.sbml.jsbml.Species)
	 */
	public PluginSpecies writeSpecies(Species species) {
		String spectype = SBO.convertSBO2Alias(species.getSBOTerm());
		if (spectype == null || spectype.length() == 0)
			spectype = PluginSpeciesSymbolType.SIMPLE_MOLECULE;
		PluginSpecies s = new PluginSpecies(spectype, species.getName());
		saveSpeciesProperties(species, s);
		return s;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.SBMLWriter#writeSpeciesReference(org.sbml.SpeciesReference)
	 */
	public PluginSpeciesReference writeSpeciesReference(
			SpeciesReference speciesReference, Object... parent)
			throws SBMLException {
		if (parent.length != 2 || !(parent[0] instanceof PluginReaction)
				|| !(parent[1] instanceof String))
			throw new IllegalArgumentException("parent" + error
					+ "PluginReaction and type (String) must be given");
		PluginSpeciesReference sr = new PluginSpeciesReference(
				(PluginReaction) parent[0], new PluginSpeciesAlias(pluginModel
						.getSpecies(speciesReference.getSpecies()), parent[1]
						.toString()));
		saveSpeciesReferenceProperties(speciesReference, sr);
		return sr;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.sbml.jsbml.SBMLWriter#writeSpeciesType(org.sbml.jsbml.SpeciesType)
	 */
	public PluginSpeciesType writeSpeciesType(SpeciesType speciesType) {
		PluginSpeciesType st = new PluginSpeciesType(speciesType.getId());
		saveNamedSBaseProperties(speciesType, st);
		return st;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.sbml.SBMLWriter#writeStoichoimetryMath(org.sbml.StoichiometryMath)
	 */
	public org.sbml.libsbml.StoichiometryMath writeStoichoimetryMath(
			StoichiometryMath st) {
		org.sbml.libsbml.StoichiometryMath sm = new org.sbml.libsbml.StoichiometryMath(
				st.getLevel(), st.getVersion());
		if (st.isSetMetaId())
			sm.setMetaId(st.getMetaId());
		if (st.isSetAnnotation())
			sm.setAnnotation(st.getAnnotationString());
		if (st.isSetNotes())
			sm.setNotes(st.getNotesString());
		if (st.isSetMath())
			sm.setMath(convert(st.getMath()));
		return sm;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.jsbml.SBMLWriter#writeTrigger(org.sbml.jsbml.Trigger)
	 */
	public ASTNode writeTrigger(Trigger trigger) {
		return convert(trigger.getMath());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.jsbml.SBMLWriter#writeUnit(org.sbml.jsbml.Unit,
	 * java.lang.Object[])
	 */
	public PluginUnit writeUnit(Unit unit, Object... parent) {
		if (parent.length != 1 || !(parent[0] instanceof PluginUnitDefinition))
			throw new IllegalArgumentException("parent" + error
					+ "PluginUnitDefinition");
		PluginUnit u = new PluginUnit((PluginUnitDefinition) parent[0]);
		saveSBaseProperties(unit, u);
		switch (unit.getKind()) {
		case AMPERE:
			u.setKind(libsbmlConstants.UNIT_KIND_AMPERE);
			break;
		case BECQUEREL:
			u.setKind(libsbmlConstants.UNIT_KIND_BECQUEREL);
			break;
		case CANDELA:
			u.setKind(libsbmlConstants.UNIT_KIND_CANDELA);
			break;
		case CELSIUS:
			u.setKind(libsbmlConstants.UNIT_KIND_CELSIUS);
			break;
		case COULOMB:
			u.setKind(libsbmlConstants.UNIT_KIND_COULOMB);
			break;
		case DIMENSIONLESS:
			u.setKind(libsbmlConstants.UNIT_KIND_DIMENSIONLESS);
			break;
		case FARAD:
			u.setKind(libsbmlConstants.UNIT_KIND_FARAD);
			break;
		case GRAM:
			u.setKind(libsbmlConstants.UNIT_KIND_GRAM);
			break;
		case GRAY:
			u.setKind(libsbmlConstants.UNIT_KIND_GRAY);
			break;
		case HENRY:
			u.setKind(libsbmlConstants.UNIT_KIND_HENRY);
			break;
		case HERTZ:
			u.setKind(libsbmlConstants.UNIT_KIND_HERTZ);
			break;
		case INVALID:
			u.setKind(libsbmlConstants.UNIT_KIND_INVALID);
			break;
		case ITEM:
			u.setKind(libsbmlConstants.UNIT_KIND_ITEM);
			break;
		case JOULE:
			u.setKind(libsbmlConstants.UNIT_KIND_JOULE);
			break;
		case KATAL:
			u.setKind(libsbmlConstants.UNIT_KIND_KATAL);
			break;
		case KELVIN:
			u.setKind(libsbmlConstants.UNIT_KIND_KELVIN);
			break;
		case KILOGRAM:
			u.setKind(libsbmlConstants.UNIT_KIND_KILOGRAM);
			break;
		case LITER:
			u.setKind(libsbmlConstants.UNIT_KIND_LITER);
			break;
		case LITRE:
			u.setKind(libsbmlConstants.UNIT_KIND_LITRE);
			break;
		case LUMEN:
			u.setKind(libsbmlConstants.UNIT_KIND_LUMEN);
			break;
		case LUX:
			u.setKind(libsbmlConstants.UNIT_KIND_LUX);
			break;
		case METER:
			u.setKind(libsbmlConstants.UNIT_KIND_METER);
			break;
		case METRE:
			u.setKind(libsbmlConstants.UNIT_KIND_METRE);
			break;
		case MOLE:
			u.setKind(libsbmlConstants.UNIT_KIND_MOLE);
			break;
		case NEWTON:
			u.setKind(libsbmlConstants.UNIT_KIND_NEWTON);
			break;
		case OHM:
			u.setKind(libsbmlConstants.UNIT_KIND_OHM);
			break;
		case PASCAL:
			u.setKind(libsbmlConstants.UNIT_KIND_PASCAL);
			break;
		case RADIAN:
			u.setKind(libsbmlConstants.UNIT_KIND_RADIAN);
			break;
		case SECOND:
			u.setKind(libsbmlConstants.UNIT_KIND_SECOND);
			break;
		case SIEMENS:
			u.setKind(libsbmlConstants.UNIT_KIND_SIEMENS);
			break;
		case SIEVERT:
			u.setKind(libsbmlConstants.UNIT_KIND_SIEVERT);
			break;
		case STERADIAN:
			u.setKind(libsbmlConstants.UNIT_KIND_STERADIAN);
			break;
		case TESLA:
			u.setKind(libsbmlConstants.UNIT_KIND_TESLA);
			break;
		case VOLT:
			u.setKind(libsbmlConstants.UNIT_KIND_VOLT);
			break;
		case WATT:
			u.setKind(libsbmlConstants.UNIT_KIND_WATT);
			break;
		case WEBER:
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
	 * @see
	 * org.sbml.jsbml.SBMLWriter#writeUnitDefinition(org.sbml.jsbml.UnitDefinition
	 * )
	 */
	public PluginUnitDefinition writeUnitDefinition(
			UnitDefinition unitDefinition) {
		PluginUnitDefinition ud = new PluginUnitDefinition(unitDefinition
				.getId());
		saveNamedSBaseProperties(unitDefinition, ud);
		for (Unit u : unitDefinition.getListOfUnits()) {
			PluginUnit unit = writeUnit(u, ud);
			ud.addUnit(unit);
			plugin.notifySBaseAdded(unit);
		}
		return ud;
	}

	/**
	 * 
	 * 
	 * @param u
	 * @param unit
	 * @return
	 */
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

}
