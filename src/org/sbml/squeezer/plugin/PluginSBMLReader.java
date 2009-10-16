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
import java.util.List;
import java.util.Set;

import jp.sbi.celldesigner.plugin.PluginAlgebraicRule;
import jp.sbi.celldesigner.plugin.PluginAssignmentRule;
import jp.sbi.celldesigner.plugin.PluginCompartment;
import jp.sbi.celldesigner.plugin.PluginCompartmentType;
import jp.sbi.celldesigner.plugin.PluginEvent;
import jp.sbi.celldesigner.plugin.PluginEventAssignment;
import jp.sbi.celldesigner.plugin.PluginFunctionDefinition;
import jp.sbi.celldesigner.plugin.PluginInitialAssignment;
import jp.sbi.celldesigner.plugin.PluginKineticLaw;
import jp.sbi.celldesigner.plugin.PluginModel;
import jp.sbi.celldesigner.plugin.PluginModifierSpeciesReference;
import jp.sbi.celldesigner.plugin.PluginParameter;
import jp.sbi.celldesigner.plugin.PluginRateRule;
import jp.sbi.celldesigner.plugin.PluginReaction;
import jp.sbi.celldesigner.plugin.PluginRule;
import jp.sbi.celldesigner.plugin.PluginSBase;
import jp.sbi.celldesigner.plugin.PluginSimpleSpeciesReference;
import jp.sbi.celldesigner.plugin.PluginSpecies;
import jp.sbi.celldesigner.plugin.PluginSpeciesReference;
import jp.sbi.celldesigner.plugin.PluginSpeciesType;
import jp.sbi.celldesigner.plugin.PluginUnit;
import jp.sbi.celldesigner.plugin.PluginUnitDefinition;

import org.sbml.jsbml.AlgebraicRule;
import org.sbml.jsbml.AssignmentRule;
import org.sbml.jsbml.CVTerm;
import org.sbml.jsbml.Compartment;
import org.sbml.jsbml.CompartmentType;
import org.sbml.jsbml.EventAssignment;
import org.sbml.jsbml.FunctionDefinition;
import org.sbml.jsbml.InitialAssignment;
import org.sbml.jsbml.KineticLaw;
import org.sbml.jsbml.Model;
import org.sbml.jsbml.ModifierSpeciesReference;
import org.sbml.jsbml.NamedSBase;
import org.sbml.jsbml.Parameter;
import org.sbml.jsbml.RateRule;
import org.sbml.jsbml.Reaction;
import org.sbml.jsbml.Rule;
import org.sbml.jsbml.SBMLException;
import org.sbml.jsbml.SBO;
import org.sbml.jsbml.SBase;
import org.sbml.jsbml.Species;
import org.sbml.jsbml.SpeciesReference;
import org.sbml.jsbml.SpeciesType;
import org.sbml.jsbml.StoichiometryMath;
import org.sbml.jsbml.Symbol;
import org.sbml.jsbml.Unit;
import org.sbml.jsbml.UnitDefinition;
import org.sbml.jsbml.io.AbstractSBMLReader;
import org.sbml.libsbml.libsbml;
import org.sbml.libsbml.libsbmlConstants;

/**
 * 
 * @author Andreas Dr&auml;ger <a
 *         href="mailto:andreas.draeger@uni-tuebingen.de">
 *         andreas.draeger@uni-tuebingen.de</a>
 * 
 */
public class PluginSBMLReader extends AbstractSBMLReader {

	private static final int level = 2;

	private static final int version = 4;

	private PluginModel originalmodel;

	/**
	 * 
	 */
	private static final String error = " must be an instance of ";

	/**
	 * 
	 */
	private Set<Integer> possibleEnzymes;

	/**
	 * get a model from the celldesigneroutput, converts it to sbmlsqueezer
	 * format and stores it
	 * 
	 * @param model
	 */
	public PluginSBMLReader(PluginModel model, Set<Integer> possibleEnzymes) {
		super(model);
		this.possibleEnzymes = possibleEnzymes;
	}

	/**
	 * 
	 */
	public PluginSBMLReader(Set<Integer> possibleEnzymes) {
		super();
		this.possibleEnzymes = possibleEnzymes;
	}

	@Override
	public Object getOriginalModel() {
		return originalmodel;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.SBMLReader#readCompartment(java.lang.Object)
	 */
	public Compartment readCompartment(Object compartment) {
		if (!(compartment instanceof PluginCompartment))
			throw new IllegalArgumentException("compartment" + error
					+ "PluginCompartment.");
		PluginCompartment pc = (PluginCompartment) compartment;
		Compartment c = new Compartment(pc.getId(), level, version);
		copyNamedSBaseProperties(c, pc);
		if (pc.getOutside() != null) {
			Compartment outside = getModel().getCompartment(pc.getOutside());
			if (outside == null)
				getModel().addCompartment(readCompartment(compartment));
			c.setOutside(outside);
		}
		if (pc.getCompartmentType() != null)
			c.setCompartmentType(readCompartmentType(pc.getCompartmentType()));
		c.setConstant(pc.getConstant());
		c.setSize(pc.getSize());
		c.setSpatialDimensions((short) pc.getSpatialDimensions());
		if (pc.getUnits() != null)
			c.setUnits(getModel().getUnitDefinition(pc.getUnits()));
		return c;
	}

	/**
	 * 
	 * @param compartmentType
	 * @return
	 */
	private CompartmentType readCompartmentType(Object compartmentType) {
		if (!(compartmentType instanceof PluginCompartmentType))
			throw new IllegalArgumentException("compartmenttype" + error
					+ "org.sbml.libsbml.CompartmentType");
		PluginCompartmentType comp = (PluginCompartmentType) compartmentType;
		CompartmentType com = new CompartmentType(comp.getId(), level, version);
		copyNamedSBaseProperties(com, comp);
		return com;
	}

	public EventAssignment readEventAssignment(Object eventass) {
		if (!(eventass instanceof PluginEventAssignment))
			throw new IllegalArgumentException("eventassignment" + error
					+ "org.sbml.libsbml.EventAssignment");
		PluginEventAssignment eve = (PluginEventAssignment) eventass;
		EventAssignment ev = new EventAssignment(level, version);
		copySBaseProperties(ev, eve);
		if (eve.getVariable() != null) {
			Symbol variable = model.findSymbol(eve.getVariable());
			if (variable == null)
				ev.setVariable(eve.getVariable());
			else
				ev.setVariable(variable);
		}
		if (eve.getMath() != null)
			ev.setMath(convert(eve.getMath(), ev));
		return ev;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.SBMLReader#readFunctionDefinition(java.lang.Object)
	 */
	public FunctionDefinition readFunctionDefinition(Object functionDefinition) {
		if (!(functionDefinition instanceof PluginFunctionDefinition))
			throw new IllegalArgumentException("functionDefinition" + error
					+ "org.sbml.libsbml.FunctionDefinition.");
		PluginFunctionDefinition fd = (PluginFunctionDefinition) functionDefinition;
		FunctionDefinition f = new FunctionDefinition(fd.getId(), level,
				version);
		copyNamedSBaseProperties(f, fd);
		if (fd.getMath() != null)
			f.setMath(convert(fd.getMath(), f));
		return f;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.SBMLReader#readInitialAssignment(java.lang.Object)
	 */
	public InitialAssignment readInitialAssignment(Object initialAssignment) {
		if (!(initialAssignment instanceof PluginInitialAssignment))
			throw new IllegalArgumentException("initialAssignment" + error
					+ "org.sbml.libsbml.InitialAssignment.");
		PluginInitialAssignment sbIA = (PluginInitialAssignment) initialAssignment;
		if (sbIA.getSymbol() == null)
			throw new IllegalArgumentException(
					"Symbol attribute not set for InitialAssignment");
		InitialAssignment ia = new InitialAssignment(model.findSymbol(sbIA
				.getSymbol()));
		copySBaseProperties(ia, sbIA);
		if (sbIA.getMath() != null)
			ia.setMath(convert(libsbml.parseFormula(sbIA.getMath()), ia));
		return ia;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.SBMLReader#readKineticLaw(java.lang.Object)
	 */
	public KineticLaw readKineticLaw(Object kineticLaw) {
		if (!(kineticLaw instanceof PluginKineticLaw))
			throw new IllegalArgumentException("kineticLaw" + error
					+ "PluginKineticLaw.");
		PluginKineticLaw plukinlaw = (PluginKineticLaw) kineticLaw;
		KineticLaw kinlaw = new KineticLaw(level, version);
		if (plukinlaw.getMath() != null)
			kinlaw.setMath(convert(plukinlaw.getMath(), kinlaw));
		for (int i = 0; i < plukinlaw.getNumParameters(); i++) {
			kinlaw.addParameter(readParameter(plukinlaw.getParameter(i)));
		}
		addAllSBaseChangeListenersTo(kinlaw);
		return kinlaw;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.SBMLReader#readModel(java.lang.Object)
	 */
	public Model readModel(Object model) {
		if (model instanceof PluginModel) {
			this.originalmodel = (PluginModel) model;
			PluginModel pm = (PluginModel) model;
			Model m = new Model(pm.getId(), level, version);
			for (int i = 0; i < pm.getNumReactions(); i++)
				m.addReaction(readReaction(pm.getReaction(i)));
			for (int i = 0; i < pm.getNumSpecies(); i++)
				m.addSpecies(readSpecies(pm.getSpecies(i)));
			addAllSBaseChangeListenersTo(m);
			return m;
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
		if (!(modifierSpeciesReference instanceof PluginModifierSpeciesReference))
			throw new IllegalArgumentException("modifierSpeciesReference"
					+ error + "PluginModifierSpeciesReference.");
		PluginModifierSpeciesReference plumod = (PluginModifierSpeciesReference) modifierSpeciesReference;
		ModifierSpeciesReference mod = new ModifierSpeciesReference(
				new Species(plumod.getSpeciesInstance().getId(), level, version));
		/*
		 * Set SBO term.
		 */
		mod.setSBOTerm(SBO.convertAlias2SBO(plumod.getModificationType()));
		if (SBO.isCatalyst(mod.getSBOTerm())) {
			PluginSpecies species = plumod.getSpeciesInstance();
			String speciesAliasType = species.getSpeciesAlias(0).getType()
					.equals("PROTEIN") ? species.getSpeciesAlias(0)
					.getProtein().getType() : species.getSpeciesAlias(0)
					.getType();
			if (possibleEnzymes.contains(Integer.valueOf(SBO
					.convertAlias2SBO(speciesAliasType))))
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
		if (!(parameter instanceof PluginParameter))
			throw new IllegalArgumentException("parameter" + error
					+ "PluginParameter.");
		PluginParameter pp = (PluginParameter) parameter;
		Parameter para = new Parameter(pp.getId(), level, version);
		addAllSBaseChangeListenersTo(para);
		return para;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.SBMLReader#readReaction(java.lang.Object)
	 */
	public Reaction readReaction(Object reac) {
		if (!(reac instanceof PluginReaction))
			throw new IllegalArgumentException(
					"reaction must be an instance of PluginReaction");
		PluginReaction r = (PluginReaction) reac;
		Reaction reaction = new Reaction(r.getId(), level, version);
		for (int i = 0; i < r.getNumReactants(); i++) {
			reaction.addReactant(readSpeciesReference(r.getReactant(i)));
		}
		for (int i = 0; i < r.getNumProducts(); i++) {
			reaction.addProduct(readSpeciesReference(r.getProduct(i)));
		}
		for (int i = 0; i < r.getNumModifiers(); i++) {
			reaction
					.addModifier(readModifierSpeciesReference(r.getModifier(i)));
		}
		reaction.setSBOTerm(SBO.convertAlias2SBO(r.getReactionType()));
		reaction.setKineticLaw(readKineticLaw(r.getKineticLaw()));
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
		if (!(rule instanceof PluginRule))
			throw new IllegalArgumentException("rule" + error
					+ "org.sbml.libsbml.Rule.");
		PluginRule libRule = (PluginRule) rule;
		Rule r;
		if (libRule instanceof PluginAlgebraicRule)
			r = new AlgebraicRule(level, version);
		else {

			if (libRule instanceof PluginAssignmentRule)
				r = new AssignmentRule(model
						.findSymbol(((PluginAssignmentRule) libRule)
								.getVariable()));
			else
				r = new RateRule(model.findSymbol(((PluginRateRule) libRule)
						.getVariable()));
		}
		copySBaseProperties(r, libRule);
		if (libRule.getMath() != null)
			r.setMath(convert(libRule.getMath(), r));
		return r;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.SBMLReader#readSpecies(java.lang.Object)
	 */
	public Species readSpecies(Object species) {
		if (!(species instanceof PluginSpecies))
			throw new IllegalArgumentException(
					"species must be an instance of PluginSpecies");
		PluginSpecies spec = (PluginSpecies) species;
		Species s = new Species(spec.getId(), level, version);
		s.setSBOTerm(SBO.convertAlias2SBO(spec.getSpeciesAlias(spec.getId())
				.getAliasID()));
		addAllSBaseChangeListenersTo(s);
		return s;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.SBMLReader#readSpeciesReference(java.lang.Object)
	 */
	public SpeciesReference readSpeciesReference(Object speciesReference) {
		if (!(speciesReference instanceof PluginSpeciesReference))
			throw new IllegalArgumentException("speciesReference" + error
					+ "PluginSpeciesReference.");
		PluginSpeciesReference specref = (PluginSpeciesReference) speciesReference;
		SpeciesReference spec = new SpeciesReference(new Species(specref
				.getSpeciesInstance().getId(), level, version));
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
		if (!(speciesType instanceof PluginSpeciesType))
			throw new IllegalArgumentException("speciesType" + error
					+ "org.sbml.libsbml.SpeciesType.");
		PluginSpeciesType libST = (PluginSpeciesType) speciesType;
		SpeciesType st = new SpeciesType(libST.getId(), level, version);
		copyNamedSBaseProperties(st, libST);
		return st;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.SBMLReader#readUnit(java.lang.Object)
	 */

	// TODO: PluginUnitbez.?
	public Unit readUnit(Object unit) {
		if (!(unit instanceof PluginUnit))
			throw new IllegalArgumentException("unit" + error
					+ "org.sbml.libsbml.Unit");
		PluginUnit libUnit = (PluginUnit) unit;
		Unit u = new Unit(level, version);
		copySBaseProperties(u, libUnit);
		switch (libUnit.getKind()) {
		case libsbmlConstants.UNIT_KIND_AMPERE:
			u.setKind(Unit.Kind.AMPERE);
			break;
		case libsbmlConstants.UNIT_KIND_BECQUEREL:
			u.setKind(Unit.Kind.BECQUEREL);
			break;
		case libsbmlConstants.UNIT_KIND_CANDELA:
			u.setKind(Unit.Kind.CANDELA);
			break;
		case libsbmlConstants.UNIT_KIND_CELSIUS:
			u.setKind(Unit.Kind.CELSIUS);
			break;
		case libsbmlConstants.UNIT_KIND_COULOMB:
			u.setKind(Unit.Kind.COULOMB);
			break;
		case libsbmlConstants.UNIT_KIND_DIMENSIONLESS:
			u.setKind(Unit.Kind.DIMENSIONLESS);
			break;
		case libsbmlConstants.UNIT_KIND_FARAD:
			u.setKind(Unit.Kind.FARAD);
			break;
		case libsbmlConstants.UNIT_KIND_GRAM:
			u.setKind(Unit.Kind.GRAM);
			break;
		case libsbmlConstants.UNIT_KIND_GRAY:
			u.setKind(Unit.Kind.GRAY);
			break;
		case libsbmlConstants.UNIT_KIND_HENRY:
			u.setKind(Unit.Kind.HENRY);
			break;
		case libsbmlConstants.UNIT_KIND_HERTZ:
			u.setKind(Unit.Kind.HERTZ);
			break;
		case libsbmlConstants.UNIT_KIND_INVALID:
			u.setKind(Unit.Kind.INVALID);
			break;
		case libsbmlConstants.UNIT_KIND_ITEM:
			u.setKind(Unit.Kind.ITEM);
			break;
		case libsbmlConstants.UNIT_KIND_JOULE:
			u.setKind(Unit.Kind.JOULE);
			break;
		case libsbmlConstants.UNIT_KIND_KATAL:
			u.setKind(Unit.Kind.KATAL);
			break;
		case libsbmlConstants.UNIT_KIND_KELVIN:
			u.setKind(Unit.Kind.KELVIN);
			break;
		case libsbmlConstants.UNIT_KIND_KILOGRAM:
			u.setKind(Unit.Kind.KILOGRAM);
			break;
		case libsbmlConstants.UNIT_KIND_LITER:
			u.setKind(Unit.Kind.LITER);
			break;
		case libsbmlConstants.UNIT_KIND_LITRE:
			u.setKind(Unit.Kind.LITRE);
			break;
		case libsbmlConstants.UNIT_KIND_LUMEN:
			u.setKind(Unit.Kind.LUMEN);
			break;
		case libsbmlConstants.UNIT_KIND_LUX:
			u.setKind(Unit.Kind.LUX);
			break;
		case libsbmlConstants.UNIT_KIND_METER:
			u.setKind(Unit.Kind.METER);
			break;
		case libsbmlConstants.UNIT_KIND_METRE:
			u.setKind(Unit.Kind.METRE);
			break;
		case libsbmlConstants.UNIT_KIND_MOLE:
			u.setKind(Unit.Kind.MOLE);
			break;
		case libsbmlConstants.UNIT_KIND_NEWTON:
			u.setKind(Unit.Kind.NEWTON);
			break;
		case libsbmlConstants.UNIT_KIND_OHM:
			u.setKind(Unit.Kind.OHM);
			break;
		case libsbmlConstants.UNIT_KIND_PASCAL:
			u.setKind(Unit.Kind.PASCAL);
			break;
		case libsbmlConstants.UNIT_KIND_RADIAN:
			u.setKind(Unit.Kind.RADIAN);
			break;
		case libsbmlConstants.UNIT_KIND_SECOND:
			u.setKind(Unit.Kind.SECOND);
			break;
		case libsbmlConstants.UNIT_KIND_SIEMENS:
			u.setKind(Unit.Kind.SIEMENS);
			break;
		case libsbmlConstants.UNIT_KIND_SIEVERT:
			u.setKind(Unit.Kind.SIEVERT);
			break;
		case libsbmlConstants.UNIT_KIND_STERADIAN:
			u.setKind(Unit.Kind.STERADIAN);
			break;
		case libsbmlConstants.UNIT_KIND_TESLA:
			u.setKind(Unit.Kind.TESLA);
			break;
		case libsbmlConstants.UNIT_KIND_VOLT:
			u.setKind(Unit.Kind.VOLT);
			break;
		case libsbmlConstants.UNIT_KIND_WATT:
			u.setKind(Unit.Kind.WATT);
			break;
		case libsbmlConstants.UNIT_KIND_WEBER:
			u.setKind(Unit.Kind.WEBER);
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
		if (!(unitDefinition instanceof PluginUnitDefinition))
			throw new IllegalArgumentException("unitDefinition" + error
					+ "org.sbml.libsbml.UnitDefinition");
		PluginUnitDefinition libUD = (PluginUnitDefinition) unitDefinition;
		UnitDefinition ud = new UnitDefinition(libUD.getId(), level, version);
		copyNamedSBaseProperties(ud, libUD);
		for (int i = 0; i < libUD.getNumUnits(); i++)
			ud.addUnit(readUnit(libUD.getUnit(i)));
		return ud;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.SBMLReader#readStoichiometricMath(java.lang.Object)
	 */
	public StoichiometryMath readStoichiometricMath(Object stoichiometryMath) {
		// TODO Auto-generated method stub
		return null;
	}

	public CVTerm readCVTerm(Object term) {
		// TODO Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see org.sbml.jsbml.SBMLReader#convertDate(java.lang.Object)
	 */
	public Date convertDate(Object d) {
		// TODO Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see org.sbml.jsbml.SBMLReader#getWarnings()
	 */
	public List<SBMLException> getWarnings() {
		// TODO Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see org.sbml.jsbml.SBMLReader#getNumErrors()
	 */
	public int getNumErrors() {
		// TODO Auto-generated method stub
		return 0;
	}

	/**
	 * 
	 * @param c
	 * @param pc
	 */
	private void copyNamedSBaseProperties(NamedSBase n, PluginSBase ps) {
		copySBaseProperties(n, ps);
		if (ps instanceof PluginCompartment) {
			PluginCompartment c = (PluginCompartment) ps;
			if (c.getId() != null)
				n.setId(c.getId());
			if (c.getName() != null)
				n.setName(c.getName());
		} else if (ps instanceof PluginCompartmentType) {
			PluginCompartmentType c = (PluginCompartmentType) ps;
			if (c.getId() != null)
				n.setId(c.getId());
			if (c.getName() != null)
				n.setName(c.getName());
		} else if (ps instanceof PluginEvent) {
			PluginEvent c = (PluginEvent) ps;
			if (c.getId() != null)
				n.setId(c.getId());
			if (c.getName() != null)
				n.setName(c.getName());
		} else if (ps instanceof PluginFunctionDefinition) {
			PluginFunctionDefinition c = (PluginFunctionDefinition) ps;
			if (c.getId() != null)
				n.setId(c.getId());
			if (c.getName() != null)
				n.setName(c.getName());
		} else if (ps instanceof PluginModel) {
			PluginModel c = (PluginModel) ps;
			if (c.getId() != null)
				n.setId(c.getId());
			if (c.getName() != null)
				n.setName(c.getName());
		} else if (ps instanceof PluginParameter) {
			PluginParameter c = (PluginParameter) ps;
			if (c.getId() != null)
				n.setId(c.getId());
			if (c.getName() != null)
				n.setName(c.getName());
		} else if (ps instanceof PluginReaction) {
			PluginReaction c = (PluginReaction) ps;
			if (c.getId() != null)
				n.setId(c.getId());
			if (c.getName() != null)
				n.setName(c.getName());
		} else if (ps instanceof PluginSimpleSpeciesReference) {
			// TODO
			// PluginSimpleSpeciesReference c = (PluginSimpleSpeciesReference)
			// ps;
			// if (c.get != null)
			// n.setId(c.getId());
			// if (c.getName() != null)
			// sbase.setName(c.getName());
		} else if (ps instanceof PluginSpecies) {
			PluginSpecies c = (PluginSpecies) ps;
			if (c.getId() != null)
				n.setId(c.getId());
			if (c.getName() != null)
				n.setName(c.getName());
		} else if (ps instanceof PluginSpeciesType) {
			PluginSpeciesType c = (PluginSpeciesType) ps;
			if (c.getId() != null)
				n.setId(c.getId());
			if (c.getName() != null)
				n.setName(c.getName());
		} else if (ps instanceof PluginUnitDefinition) {
			PluginUnitDefinition c = (PluginUnitDefinition) ps;
			if (c.getId() != null)
				n.setId(c.getId());
			if (c.getName() != null)
				n.setName(c.getName());
		}

	}

	/**
	 * 
	 * @param c
	 * @param pc
	 */

	private void copySBaseProperties(SBase c, PluginSBase pc) {
		if (pc.getNotesString() != null)
			c.setNotes(pc.getNotesString());
		// TODO

	}
}
