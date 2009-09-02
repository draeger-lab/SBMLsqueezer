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

import org.sbml.ASTNode;
import org.sbml.AlgebraicRule;
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
import org.sbml.SBO;
import org.sbml.SBase;
import org.sbml.Species;
import org.sbml.SpeciesReference;
import org.sbml.SpeciesType;
import org.sbml.StoichiometryMath;
import org.sbml.Symbol;
import org.sbml.Trigger;
import org.sbml.UnitDefinition;
import org.sbml.libsbml.SBMLDocument;
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

	/**
	 * get a libsbml model converts it to sbmlsquezzer format and save the new
	 * model
	 * 
	 * @param model
	 */
	public LibSBMLReader(org.sbml.libsbml.Model model,
			Set<Integer> possibleEnzymes) {
		super(model);
		this.possibleEnzymes = possibleEnzymes;
		this.setOfDocuments = new HashSet<SBMLDocument>();
	}

	public LibSBMLReader(Set<Integer> possibleEnzymes) {
		super();
		this.possibleEnzymes = possibleEnzymes;
		setOfDocuments = new HashSet<SBMLDocument>();
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
		Compartment c = new Compartment(comp.getId());
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
		CompartmentType com = new CompartmentType(comp.getId());
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
		Constraint con = new Constraint();
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
		Delay de = new Delay();
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
		Event ev = new Event();
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
		FunctionDefinition f = new FunctionDefinition(fd.getId());
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
		KineticLaw kinlaw = new KineticLaw();
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
			org.sbml.libsbml.Model m = (org.sbml.libsbml.Model) model;
			this.model = new Model(m.getId());
			int i;
			copyNamedSBaseProperties(this.model, m);
			for (i = 0; i < m.getNumFunctionDefinitions(); i++)
				this.model.addFunctionDefinition(readFunctionDefinition(m
						.getFunctionDefinition(i)));
			for (i = 0; i < m.getNumUnitDefinitions(); i++)
				this.model.addUnitDefinition(readUnitDefinition(m
						.getUnitDefinition(i)));
			for (i = 0; i < m.getNumCompartmentTypes(); i++)
				this.model.addCompartmentType(readCompartmentType(m
						.getCompartmentType(i)));
			for (i = 0; i < m.getNumSpeciesTypes(); i++)
				this.model.addSpeciesType(readSpeciesType(m.getSpeciesType(i)));
			for (i = 0; i < m.getNumCompartments(); i++)
				this.model.addCompartment(readCompartment(m.getCompartment(i)));
			for (i = 0; i < m.getNumSpecies(); i++)
				this.model.addSpecies(readSpecies(m.getSpecies(i)));
			for (i = 0; i < m.getNumParameters(); i++)
				this.model.addParameter(readParameter(m.getParameter(i)));
			for (i = 0; i < m.getNumInitialAssignments(); i++)
				this.model.addInitialAssignment(readInitialAssignment(m
						.getInitialAssignment(i)));
			for (i = 0; i < m.getNumRules(); i++)
				this.model.addRule(readRule(m.getRule(i)));
			for (i = 0; i < m.getNumConstraints(); i++)
				this.model.addConstraint(readConstraint(m.getConstraint(i)));
			for (i = 0; i < m.getNumReactions(); i++)
				this.model.addReaction(readReaction(m.getReaction(i)));
			for (i = 0; i < m.getNumEvents(); i++)
				this.model.addEvent(readEvent(m.getEvent(i)));
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
		Parameter para = new Parameter(p.getId());
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
		Reaction reaction = new Reaction(r.getId());
		this.model.addReaction(reaction);
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
			r = new AlgebraicRule();
		else {
			Symbol v = model.findSymbol(libRule.getVariable());
			if (libRule.isAssignment()) {
				r = new AssignmentRule(v);
			} else {
				r = new RateRule(v);
			}
		}
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
		Species s = new Species(spec.getId());
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
			throw new IllegalArgumentException("org.sbml.libsbml.SpeciesType");
		// TODO Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.SBMLReader#readStoichiometricMath(java.lang.Object)
	 */
	public StoichiometryMath readStoichiometricMath(Object stoichiometryMath) {
		org.sbml.libsbml.StoichiometryMath s = (org.sbml.libsbml.StoichiometryMath) stoichiometryMath;
		StoichiometryMath sm = new StoichiometryMath();
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
		Trigger trig = new Trigger();
		copySBaseProperties(trig, trigg);
		if (trigg.isSetMath())
			trig.setMath(convert(trigg.getMath(), trig));
		return trig;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.SBMLReader#readUnitDefinition(java.lang.Object)
	 */
	public UnitDefinition readUnitDefinition(Object unitDefinition) {
		if (!(unitDefinition instanceof org.sbml.libsbml.UnitDefinition))
			throw new IllegalArgumentException(
					"org.sbml.libsbml.UnitDefinition");
		// TODO Auto-generated method stub
		return null;
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
		EventAssignment ev = new EventAssignment();
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
