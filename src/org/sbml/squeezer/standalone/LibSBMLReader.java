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

import java.util.HashSet;
import java.util.Set;

import org.sbml.jlibsbml.ASTNode;
import org.sbml.jlibsbml.AlgebraicRule;
import org.sbml.jlibsbml.AssignmentRule;
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
import org.sbml.jlibsbml.ModelCreator;
import org.sbml.jlibsbml.ModelHistory;
import org.sbml.jlibsbml.ModifierSpeciesReference;
import org.sbml.jlibsbml.NamedSBase;
import org.sbml.jlibsbml.Parameter;
import org.sbml.jlibsbml.RateRule;
import org.sbml.jlibsbml.Reaction;
import org.sbml.jlibsbml.Rule;
import org.sbml.jlibsbml.SBO;
import org.sbml.jlibsbml.SBase;
import org.sbml.jlibsbml.Species;
import org.sbml.jlibsbml.SpeciesReference;
import org.sbml.jlibsbml.SpeciesType;
import org.sbml.jlibsbml.StoichiometryMath;
import org.sbml.jlibsbml.Symbol;
import org.sbml.jlibsbml.Trigger;
import org.sbml.jlibsbml.Unit;
import org.sbml.jlibsbml.UnitDefinition;
import org.sbml.jlibsbml.UnitKind;
import org.sbml.libsbml.SBMLDocument;
import org.sbml.libsbml.libsbmlConstants;
import org.sbml.squeezer.io.AbstractSBMLReader;

/**
 * @author Andreas Dr&auml;ger <a
 *         href="mailto:andreas.draeger@uni-tuebingen.de">
 *         andreas.draeger@uni-tuebingen.de</a>
 * 
 */
public class LibSBMLReader extends AbstractSBMLReader {

	private Set<SBMLDocument> setOfDocuments;
	private Set<Integer> possibleEnzymes;
	private static final String error = " must be an instance of ";
	private org.sbml.libsbml.Model originalModel;

	/**
	 * 
	 * @param possibleEnzymes
	 */
	public LibSBMLReader(Set<Integer> possibleEnzymes) {
		super();
		this.possibleEnzymes = possibleEnzymes;
		setOfDocuments = new HashSet<SBMLDocument>();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.squeezer.io.AbstractSBMLReader#getOriginalModel()
	 */
	// @Override
	public Object getOriginalModel() {
		return originalModel;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.SBMLReader#readCompartment(java.lang.Object)
	 */
	public Compartment readCompartment(Object compartment) {
		if (!(compartment instanceof org.sbml.libsbml.Compartment))
			throw new IllegalArgumentException("compartment" + error
					+ "org.sbml.libsbml.Compartment");
		org.sbml.libsbml.Compartment comp = (org.sbml.libsbml.Compartment) compartment;
		Compartment c = new Compartment(comp.getId(), (int) comp.getLevel(),
				(int) comp.getVersion());
		copyNamedSBaseProperties(c, comp);
		c.setName(comp.getName());
		if (comp.isSetOutside()) {
			Compartment outside = getModel().getCompartment(comp.getOutside());
			if (outside == null)
				getModel().addCompartment(readCompartment(compartment));
			c.setOutside(outside);
		}
		if (comp.isSetCompartmentType())
			c
					.setCompartmentType(readCompartmentType(comp
							.getCompartmentType()));
		c.setConstant(comp.getConstant());
		c.setSize(comp.getSize());
		c.setSpatialDimensions((short) comp.getSpatialDimensions());
		return c;
	}

	/**
	 * 
	 * @param compartmenttype
	 * @return
	 */
	public CompartmentType readCompartmentType(Object compartmenttype) {

		if (!(compartmenttype instanceof org.sbml.libsbml.CompartmentType))
			throw new IllegalArgumentException("compartmenttype" + error
					+ "org.sbml.libsbml.CompartmentType");
		org.sbml.libsbml.CompartmentType comp = (org.sbml.libsbml.CompartmentType) compartmenttype;
		CompartmentType com = new CompartmentType(comp.getId(), (int) comp
				.getLevel(), (int) comp.getVersion());
		copyNamedSBaseProperties(com, comp);
		return com;

	}

	/**
	 * 
	 * @param constraint
	 * @return
	 */
	public Constraint readConstraint(Object constraint) {
		if (!(constraint instanceof org.sbml.libsbml.Constraint))
			throw new IllegalArgumentException("constraint" + error
					+ "org.sbml.libsml.Constraint");
		org.sbml.libsbml.Constraint cons = (org.sbml.libsbml.Constraint) constraint;
		Constraint con = new Constraint((int) cons.getLevel(), (int) cons
				.getVersion());
		copySBaseProperties(con, cons);
		if (cons.isSetMath())
			con.setMath(convert(cons.getMath(), con));
		if (cons.isSetMessage())
			con.setMessage(cons.getMessageString());
		return con;

	}

	/**
	 * 
	 * @param delay
	 * @return
	 */
	public Delay readDelay(Object delay) {
		if (!(delay instanceof org.sbml.libsbml.Delay))
			throw new IllegalArgumentException("delay" + error
					+ "org.sbml.libsbml.Delay");
		org.sbml.libsbml.Delay del = (org.sbml.libsbml.Delay) delay;
		Delay de = new Delay((int) del.getLevel(), (int) del.getVersion());
		copySBaseProperties(de, del);
		if (del.isSetMath())
			de.setMath(convert(del.getMath(), de));
		return de;
	}

	/**
	 * 
	 * @param event
	 * @return
	 */
	public Event readEvent(Object event) {
		if (!(event instanceof org.sbml.libsbml.Event))
			throw new IllegalArgumentException("event" + error
					+ "org.sbml.libsbml.Event");
		org.sbml.libsbml.Event eve = (org.sbml.libsbml.Event) event;
		Event ev = new Event((int) eve.getLevel(), (int) eve.getVersion());
		copyNamedSBaseProperties(ev, eve);
		ev.setTrigger(readTrigger(eve.getTrigger()));
		if (eve.isSetDelay())
			ev.setDelay(readDelay(eve.getDelay()));
		for (int i = 0; i < eve.getNumEventAssignments(); i++) {
			ev.addEventAssignement(readEventAssignment(eve
					.getEventAssignment(i)));
		}
		return ev;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.SBMLReader#readFunctionDefinition(java.lang.Object)
	 */
	public FunctionDefinition readFunctionDefinition(Object functionDefinition) {
		if (!(functionDefinition instanceof org.sbml.libsbml.FunctionDefinition))
			throw new IllegalArgumentException("functionDefinition" + error
					+ "org.sbml.libsbml.FunctionDefinition.");
		org.sbml.libsbml.FunctionDefinition fd = (org.sbml.libsbml.FunctionDefinition) functionDefinition;
		FunctionDefinition f = new FunctionDefinition(fd.getId(), (int) fd
				.getLevel(), (int) fd.getVersion());
		copySBaseProperties(f, fd);
		copyNamedSBaseProperties(f, fd);
		if (fd.isSetMath())
			f.setMath(convert(fd.getMath(), f));
		return f;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.SBMLReader#readInitialAssignment(java.lang.Object)
	 */
	public InitialAssignment readInitialAssignment(Object initialAssignment) {
		if (!(initialAssignment instanceof org.sbml.libsbml.InitialAssignment))
			throw new IllegalArgumentException("initialAssignment" + error
					+ "org.sbml.libsbml.InitialAssignment.");
		org.sbml.libsbml.InitialAssignment sbIA = (org.sbml.libsbml.InitialAssignment) initialAssignment;
		if (!sbIA.isSetSymbol())
			throw new IllegalArgumentException(
					"Symbol attribute not set for InitialAssignment");
		InitialAssignment ia = new InitialAssignment(model.findSymbol(sbIA
				.getSymbol()));
		copySBaseProperties(ia, sbIA);
		if (sbIA.isSetMath())
			ia.setMath(convert(sbIA.getMath(), ia));
		return ia;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.SBMLReader#readKineticLaw(java.lang.Object)
	 */
	public KineticLaw readKineticLaw(Object kineticLaw) {
		if (!(kineticLaw instanceof org.sbml.libsbml.KineticLaw))
			throw new IllegalArgumentException("kineticLaw" + error
					+ "org.sbml.libsbml.KineticLaw.");
		org.sbml.libsbml.KineticLaw kl = (org.sbml.libsbml.KineticLaw) kineticLaw;
		KineticLaw kinlaw = new KineticLaw((int) kl.getLevel(), (int) kl
				.getVersion());
		copySBaseProperties(kinlaw, kl);
		if (kl.isSetMath()) {
			ASTNode ast = convert(kl.getMath(), kinlaw);
			ast.reduceToBinary();
			kinlaw.setMath(ast);
		}
		for (int i = 0; i < kl.getNumParameters(); i++)
			kinlaw.addParameter(readParameter(kl.getParameter(i)));
		addAllSBaseChangeListenersTo(kinlaw);
		return kinlaw;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.SBMLReader#readModel(java.lang.Object)
	 */
	public Model readModel(Object model) {
		if (model instanceof String) {
			SBMLDocument doc = (new org.sbml.libsbml.SBMLReader())
					.readSBML(model.toString());
			setOfDocuments.add(doc);
			model = doc.getModel();
		}
		if (model instanceof org.sbml.libsbml.Model) {
			this.originalModel = (org.sbml.libsbml.Model) model;
			this.model = new Model(originalModel.getId(), (int) originalModel
					.getLevel(), (int) originalModel.getVersion());
			int i;
			if (originalModel.isSetModelHistory()) {
				ModelHistory mh = new ModelHistory();
				for (i=0; i<originalModel.getModelHistory().getNumCreators(); i++) {
					ModelCreator mc = new ModelCreator();
					org.sbml.libsbml.ModelCreator creator = originalModel.getModelHistory().getCreator(i);
					System.out.println(creator.getFamilyName());
					mc.setGivenName(creator.getGivenName());
					mc.setFamilyName(creator.getFamilyName());
					mc.setEmail(creator.getEmail());
					mc.setOrganisation(creator.getOrganisation());
					mh.addCreator(mc);
				}
				this.model.setModelHistory(mh);
			}
			copyNamedSBaseProperties(this.model, originalModel);
			for (i = 0; i < originalModel.getNumFunctionDefinitions(); i++)
				this.model
						.addFunctionDefinition(readFunctionDefinition(originalModel
								.getFunctionDefinition(i)));
			for (i = 0; i < originalModel.getNumUnitDefinitions(); i++)
				this.model.addUnitDefinition(readUnitDefinition(originalModel
						.getUnitDefinition(i)));
			// This is something, libSBML wouldn't do...
			addPredefinedUnitDefinitions(this.model);
			for (i = 0; i < originalModel.getNumCompartmentTypes(); i++)
				this.model.addCompartmentType(readCompartmentType(originalModel
						.getCompartmentType(i)));
			for (i = 0; i < originalModel.getNumSpeciesTypes(); i++)
				this.model.addSpeciesType(readSpeciesType(originalModel
						.getSpeciesType(i)));
			for (i = 0; i < originalModel.getNumCompartments(); i++)
				this.model.addCompartment(readCompartment(originalModel
						.getCompartment(i)));
			for (i = 0; i < originalModel.getNumSpecies(); i++)
				this.model.addSpecies(readSpecies(originalModel.getSpecies(i)));
			for (i = 0; i < originalModel.getNumParameters(); i++)
				this.model.addParameter(readParameter(originalModel
						.getParameter(i)));
			for (i = 0; i < originalModel.getNumInitialAssignments(); i++)
				this.model
						.addInitialAssignment(readInitialAssignment(originalModel
								.getInitialAssignment(i)));
			for (i = 0; i < originalModel.getNumRules(); i++)
				this.model.addRule(readRule(originalModel.getRule(i)));
			for (i = 0; i < originalModel.getNumConstraints(); i++)
				this.model.addConstraint(readConstraint(originalModel
						.getConstraint(i)));
			for (i = 0; i < originalModel.getNumReactions(); i++)
				this.model.addReaction(readReaction(originalModel
						.getReaction(i)));
			for (i = 0; i < originalModel.getNumEvents(); i++)
				this.model.addEvent(readEvent(originalModel.getEvent(i)));
			addAllSBaseChangeListenersTo(this.model);
			return this.model;
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.SBMLReader#readModifierSpeciesReference(java.lang.Object)
	 */
	public ModifierSpeciesReference readModifierSpeciesReference(
			Object modifierSpeciesReference) {
		if (!(modifierSpeciesReference instanceof org.sbml.libsbml.ModifierSpeciesReference))
			throw new IllegalArgumentException("modifierSpeciesReference"
					+ error + "org.sbml.libsbml.ModifierSpeciesReference.");
		org.sbml.libsbml.ModifierSpeciesReference msr = (org.sbml.libsbml.ModifierSpeciesReference) modifierSpeciesReference;
		ModifierSpeciesReference mod = new ModifierSpeciesReference(model
				.getSpecies(msr.getSpecies()));
		copyNamedSBaseProperties(mod, msr);
		if (msr.isSetSBOTerm()) {
			mod.setSBOTerm(msr.getSBOTerm());
			if (!SBO.isEnzymaticCatalysis(mod.getSBOTerm())
					&& possibleEnzymes.contains(Integer.valueOf(mod
							.getSpeciesInstance().getSBOTerm())))
				mod.setSBOTerm(SBO.getEnzymaticCatalysis());
		}

		addAllSBaseChangeListenersTo(mod);
		return mod;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.SBMLReader#readParameter(java.lang.Object)
	 */
	public Parameter readParameter(Object parameter) {
		if (!(parameter instanceof org.sbml.libsbml.Parameter))
			throw new IllegalArgumentException("parameter" + error
					+ "org.sbml.libsbml.Parameter.");
		org.sbml.libsbml.Parameter p = (org.sbml.libsbml.Parameter) parameter;
		Parameter para = new Parameter(p.getId(), (int) p.getLevel(), (int) p
				.getVersion());
		if (p.isSetName())
			para.setName(p.getName());
		if (p.isSetMetaId())
			para.setMetaId(p.getMetaId());
		if (p.isSetNotes())
			para.setNotes(p.getNotesString());
		if (p.isSetSBOTerm())
			para.setSBOTerm(p.getSBOTerm());
		if (p.isSetValue())
			para.setValue(p.getValue());
		addAllSBaseChangeListenersTo(para);
		return para;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.SBMLReader#readReaction(java.lang.Object)
	 */
	public Reaction readReaction(Object reac) {
		if (!(reac instanceof org.sbml.libsbml.Reaction))
			throw new IllegalArgumentException("reaction" + error
					+ "org.sbml.libsbml.Reaction.");
		org.sbml.libsbml.Reaction r = (org.sbml.libsbml.Reaction) reac;
		Reaction reaction = new Reaction(r.getId(), (int) r.getLevel(), (int) r
				.getVersion());
		for (int i = 0; i < r.getNumReactants(); i++)
			reaction.addReactant(readSpeciesReference(r.getReactant(i)));
		for (int i = 0; i < r.getNumProducts(); i++)
			reaction.addProduct(readSpeciesReference(r.getProduct(i)));
		for (int i = 0; i < r.getNumModifiers(); i++)
			reaction
					.addModifier(readModifierSpeciesReference(r.getModifier(i)));
		if (r.isSetKineticLaw())
			reaction.setKineticLaw(readKineticLaw(r.getKineticLaw()));
		copyNamedSBaseProperties(reaction, r);
		reaction.setFast(r.getFast());
		reaction.setReversible(r.getReversible());
		addAllSBaseChangeListenersTo(reaction);
		return reaction;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.SBMLReader#readRule(java.lang.Object)
	 */
	public Rule readRule(Object rule) {
		if (!(rule instanceof org.sbml.libsbml.Rule))
			throw new IllegalArgumentException("rule" + error
					+ "org.sbml.libsbml.Rule.");
		org.sbml.libsbml.Rule libRule = (org.sbml.libsbml.Rule) rule;
		Rule r;
		if (libRule.isAlgebraic())
			r = new AlgebraicRule((int) libRule.getLevel(), (int) libRule
					.getVersion());
		else {
			Symbol s = model.findSymbol(libRule.getVariable());
			if (libRule.isAssignment())
				r = new AssignmentRule(s);
			else
				r = new RateRule(s);
		}
		copySBaseProperties(r, libRule);
		if (libRule.isSetMath())
			r.setMath(convert(libRule.getMath(), r));
		return r;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.SBMLReader#readSpecies(java.lang.Object)
	 */
	public Species readSpecies(Object species) {
		if (!(species instanceof org.sbml.libsbml.Species))
			throw new IllegalArgumentException("species" + error
					+ "org.sbml.libsbml.Species.");
		org.sbml.libsbml.Species spec = (org.sbml.libsbml.Species) species;
		Species s = new Species(spec.getId(), (int) spec.getLevel(), (int) spec
				.getVersion());
		copyNamedSBaseProperties(s, spec);
		if (spec.isSetCharge())
			s.setCharge(spec.getCharge());
		if (spec.isSetCompartment())
			s.setCompartment(getModel().getCompartment(spec.getCompartment()));
		s.setBoundaryCondition(spec.getBoundaryCondition());
		s.setConstant(spec.getConstant());
		s.setHasOnlySubstanceUnits(spec.getHasOnlySubstanceUnits());
		if (spec.isSetInitialAmount())
			s.setInitialAmount(spec.getInitialAmount());
		else if (spec.isSetInitialConcentration())
			s.setInitialConcentration(spec.getInitialConcentration());
		addAllSBaseChangeListenersTo(s);
		return s;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.SBMLReader#readSpeciesReference(java.lang.Object)
	 */
	public SpeciesReference readSpeciesReference(Object speciesReference) {
		if (!(speciesReference instanceof org.sbml.libsbml.SpeciesReference))
			throw new IllegalArgumentException("speciesReference" + error
					+ "org.sbml.libsbml.SpeciesReference.");
		org.sbml.libsbml.SpeciesReference specref = (org.sbml.libsbml.SpeciesReference) speciesReference;
		SpeciesReference spec = new SpeciesReference(model.getSpecies(specref
				.getSpecies()));

		copyNamedSBaseProperties(spec, specref);
		if (specref.isSetStoichiometryMath())
			spec.setStoichiometryMath(readStoichiometricMath(specref
					.getStoichiometryMath()));
		else
			spec.setStoichiometry(specref.getStoichiometry());
		addAllSBaseChangeListenersTo(spec);
		return spec;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.SBMLReader#readSpeciesType(java.lang.Object)
	 */
	public SpeciesType readSpeciesType(Object speciesType) {
		if (!(speciesType instanceof org.sbml.libsbml.SpeciesType))
			throw new IllegalArgumentException("speciesType" + error
					+ "org.sbml.libsbml.SpeciesType.");
		org.sbml.libsbml.SpeciesType libST = (org.sbml.libsbml.SpeciesType) speciesType;
		SpeciesType st = new SpeciesType(libST.getId(), (int) libST.getLevel(),
				(int) libST.getVersion());
		copyNamedSBaseProperties(st, libST);
		return st;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.SBMLReader#readStoichiometricMath(java.lang.Object)
	 */
	public StoichiometryMath readStoichiometricMath(Object stoichiometryMath) {
		org.sbml.libsbml.StoichiometryMath s = (org.sbml.libsbml.StoichiometryMath) stoichiometryMath;
		StoichiometryMath sm = new StoichiometryMath((int) s.getLevel(),
				(int) s.getVersion());
		copySBaseProperties(sm, s);
		if (s.isSetMath())
			sm.setMath(convert(s.getMath(), sm));
		return sm;
	}

	/**
	 * 
	 * @param trigger
	 * @return
	 */
	public Trigger readTrigger(Object trigger) {
		if (!(trigger instanceof org.sbml.libsbml.Trigger))
			throw new IllegalArgumentException("trigger" + error
					+ "org.sbml.libsbml.Trigger");
		org.sbml.libsbml.Trigger trigg = (org.sbml.libsbml.Trigger) trigger;
		Trigger trig = new Trigger((int) trigg.getLevel(), (int) trigg
				.getVersion());
		copySBaseProperties(trig, trigg);
		if (trigg.isSetMath())
			trig.setMath(convert(trigg.getMath(), trig));
		return trig;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.SBMLReader#readUnit(java.lang.Object)
	 */
	public Unit readUnit(Object unit) {
		if (!(unit instanceof org.sbml.libsbml.Unit))
			throw new IllegalArgumentException("unit" + error
					+ "org.sbml.libsbml.Unit");
		org.sbml.libsbml.Unit libUnit = (org.sbml.libsbml.Unit) unit;
		Unit u = new Unit((int) libUnit.getLevel(), (int) libUnit.getVersion());
		copySBaseProperties(u, libUnit);
		switch (libUnit.getKind()) {
		case libsbmlConstants.UNIT_KIND_AMPERE:
			u.setKind(UnitKind.UNIT_KIND_AMPERE);
			break;
		case libsbmlConstants.UNIT_KIND_BECQUEREL:
			u.setKind(UnitKind.UNIT_KIND_BECQUEREL);
			break;
		case libsbmlConstants.UNIT_KIND_CANDELA:
			u.setKind(UnitKind.UNIT_KIND_CANDELA);
			break;
		case libsbmlConstants.UNIT_KIND_CELSIUS:
			u.setKind(UnitKind.UNIT_KIND_CELSIUS);
			break;
		case libsbmlConstants.UNIT_KIND_COULOMB:
			u.setKind(UnitKind.UNIT_KIND_COULOMB);
			break;
		case libsbmlConstants.UNIT_KIND_DIMENSIONLESS:
			u.setKind(UnitKind.UNIT_KIND_DIMENSIONLESS);
			break;
		case libsbmlConstants.UNIT_KIND_FARAD:
			u.setKind(UnitKind.UNIT_KIND_FARAD);
			break;
		case libsbmlConstants.UNIT_KIND_GRAM:
			u.setKind(UnitKind.UNIT_KIND_GRAM);
			break;
		case libsbmlConstants.UNIT_KIND_GRAY:
			u.setKind(UnitKind.UNIT_KIND_GRAY);
			break;
		case libsbmlConstants.UNIT_KIND_HENRY:
			u.setKind(UnitKind.UNIT_KIND_HENRY);
			break;
		case libsbmlConstants.UNIT_KIND_HERTZ:
			u.setKind(UnitKind.UNIT_KIND_HERTZ);
			break;
		case libsbmlConstants.UNIT_KIND_INVALID:
			u.setKind(UnitKind.UNIT_KIND_INVALID);
			break;
		case libsbmlConstants.UNIT_KIND_ITEM:
			u.setKind(UnitKind.UNIT_KIND_ITEM);
			break;
		case libsbmlConstants.UNIT_KIND_JOULE:
			u.setKind(UnitKind.UNIT_KIND_JOULE);
			break;
		case libsbmlConstants.UNIT_KIND_KATAL:
			u.setKind(UnitKind.UNIT_KIND_KATAL);
			break;
		case libsbmlConstants.UNIT_KIND_KELVIN:
			u.setKind(UnitKind.UNIT_KIND_KELVIN);
			break;
		case libsbmlConstants.UNIT_KIND_KILOGRAM:
			u.setKind(UnitKind.UNIT_KIND_KILOGRAM);
			break;
		case libsbmlConstants.UNIT_KIND_LITER:
			u.setKind(UnitKind.UNIT_KIND_LITER);
			break;
		case libsbmlConstants.UNIT_KIND_LITRE:
			u.setKind(UnitKind.UNIT_KIND_LITRE);
			break;
		case libsbmlConstants.UNIT_KIND_LUMEN:
			u.setKind(UnitKind.UNIT_KIND_LUMEN);
			break;
		case libsbmlConstants.UNIT_KIND_LUX:
			u.setKind(UnitKind.UNIT_KIND_LUX);
			break;
		case libsbmlConstants.UNIT_KIND_METER:
			u.setKind(UnitKind.UNIT_KIND_METER);
			break;
		case libsbmlConstants.UNIT_KIND_METRE:
			u.setKind(UnitKind.UNIT_KIND_METRE);
			break;
		case libsbmlConstants.UNIT_KIND_MOLE:
			u.setKind(UnitKind.UNIT_KIND_MOLE);
			break;
		case libsbmlConstants.UNIT_KIND_NEWTON:
			u.setKind(UnitKind.UNIT_KIND_NEWTON);
			break;
		case libsbmlConstants.UNIT_KIND_OHM:
			u.setKind(UnitKind.UNIT_KIND_OHM);
			break;
		case libsbmlConstants.UNIT_KIND_PASCAL:
			u.setKind(UnitKind.UNIT_KIND_PASCAL);
			break;
		case libsbmlConstants.UNIT_KIND_RADIAN:
			u.setKind(UnitKind.UNIT_KIND_RADIAN);
			break;
		case libsbmlConstants.UNIT_KIND_SECOND:
			u.setKind(UnitKind.UNIT_KIND_SECOND);
			break;
		case libsbmlConstants.UNIT_KIND_SIEMENS:
			u.setKind(UnitKind.UNIT_KIND_SIEMENS);
			break;
		case libsbmlConstants.UNIT_KIND_SIEVERT:
			u.setKind(UnitKind.UNIT_KIND_SIEVERT);
			break;
		case libsbmlConstants.UNIT_KIND_STERADIAN:
			u.setKind(UnitKind.UNIT_KIND_STERADIAN);
			break;
		case libsbmlConstants.UNIT_KIND_TESLA:
			u.setKind(UnitKind.UNIT_KIND_TESLA);
			break;
		case libsbmlConstants.UNIT_KIND_VOLT:
			u.setKind(UnitKind.UNIT_KIND_VOLT);
			break;
		case libsbmlConstants.UNIT_KIND_WATT:
			u.setKind(UnitKind.UNIT_KIND_WATT);
			break;
		case libsbmlConstants.UNIT_KIND_WEBER:
			u.setKind(UnitKind.UNIT_KIND_WEBER);
			break;
		}
		u.setExponent(libUnit.getExponent());
		u.setMultiplier(libUnit.getMultiplier());
		u.setScale(libUnit.getScale());
		u.setOffset(libUnit.getOffset());
		return u;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.SBMLReader#readUnitDefinition(java.lang.Object)
	 */
	public UnitDefinition readUnitDefinition(Object unitDefinition) {
		if (!(unitDefinition instanceof org.sbml.libsbml.UnitDefinition))
			throw new IllegalArgumentException("unitDefinition" + error
					+ "org.sbml.libsbml.UnitDefinition");
		org.sbml.libsbml.UnitDefinition libUD = (org.sbml.libsbml.UnitDefinition) unitDefinition;
		UnitDefinition ud = new UnitDefinition(libUD.getId(), (int) libUD
				.getLevel(), (int) libUD.getVersion());
		copyNamedSBaseProperties(ud, libUD);
		for (int i = 0; i < libUD.getNumUnits(); i++)
			ud.addUnit(readUnit(libUD.getUnit(i)));
		return ud;
	}

	/**
	 * 
	 * @param sbase
	 * @param libSBase
	 */
	private void copyNamedSBaseProperties(NamedSBase sbase,
			org.sbml.libsbml.SBase libSBase) {
		copySBaseProperties(sbase, libSBase);
		if (libSBase.isSetId())
			sbase.setId(libSBase.getId());
		if (libSBase.isSetName())
			sbase.setName(libSBase.getName());

	}

	/**
	 * 
	 * @param sbase
	 * @param libSBase
	 */
	private void copySBaseProperties(SBase sbase,
			org.sbml.libsbml.SBase libSBase) {
		if (libSBase.isSetMetaId())
			sbase.setMetaId(libSBase.getMetaId());
		if (libSBase.isSetSBOTerm())
			sbase.setSBOTerm(libSBase.getSBOTerm());
		if (libSBase.isSetNotes())
			sbase.setNotes(libSBase.getNotesString());
	}

	/**
	 * 
	 * @param eventAssignment
	 * @return
	 */
	private EventAssignment readEventAssignment(Object eventass) {
		if (!(eventass instanceof org.sbml.libsbml.EventAssignment))
			throw new IllegalArgumentException("eventassignment" + error
					+ "org.sbml.libsbml.EventAssignment");
		org.sbml.libsbml.EventAssignment eve = (org.sbml.libsbml.EventAssignment) eventass;
		EventAssignment ev = new EventAssignment((int) eve.getLevel(),
				(int) eve.getVersion());
		copySBaseProperties(ev, eve);
		if (eve.isSetVariable()) {
			Symbol variable = model.findSymbol(eve.getVariable());
			if (variable == null)
				ev.setVariable(eve.getVariable());
			else
				ev.setVariable(variable);
		}
		if (eve.isSetMath())
			ev.setMath(convert(eve.getMath(), ev));
		return ev;
	}
}
