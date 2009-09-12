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
import java.util.Set;

import jp.sbi.celldesigner.plugin.PluginCompartment;
import jp.sbi.celldesigner.plugin.PluginKineticLaw;
import jp.sbi.celldesigner.plugin.PluginModel;
import jp.sbi.celldesigner.plugin.PluginModifierSpeciesReference;
import jp.sbi.celldesigner.plugin.PluginParameter;
import jp.sbi.celldesigner.plugin.PluginReaction;
import jp.sbi.celldesigner.plugin.PluginSpecies;
import jp.sbi.celldesigner.plugin.PluginSpeciesReference;

import org.sbml.jsbml.CVTerm;
import org.sbml.jsbml.Compartment;
import org.sbml.jsbml.EventAssignment;
import org.sbml.jsbml.FunctionDefinition;
import org.sbml.jsbml.InitialAssignment;
import org.sbml.jsbml.KineticLaw;
import org.sbml.jsbml.Model;
import org.sbml.jsbml.ModifierSpeciesReference;
import org.sbml.jsbml.Parameter;
import org.sbml.jsbml.Reaction;
import org.sbml.jsbml.Rule;
import org.sbml.jsbml.SBO;
import org.sbml.jsbml.Species;
import org.sbml.jsbml.SpeciesReference;
import org.sbml.jsbml.SpeciesType;
import org.sbml.jsbml.StoichiometryMath;
import org.sbml.jsbml.Unit;
import org.sbml.jsbml.UnitDefinition;
import org.sbml.squeezer.io.AbstractSBMLReader;

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
		// TODO Auto-generated method stub
		return null;
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
		// TODO Auto-generated method stub
		return null;
	}

	public CVTerm readCVTerm(Object term) {
		// TODO Auto-generated method stub
		return null;
	}

	public EventAssignment readEventAssignment(Object eventAssignment) {
		// TODO Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.SBMLReader#readFunctionDefinition(java.lang.Object)
	 */
	public FunctionDefinition readFunctionDefinition(Object functionDefinition) {
		// TODO Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.SBMLReader#readInitialAssignment(java.lang.Object)
	 */
	public InitialAssignment readInitialAssignment(Object initialAssignment) {
		// TODO Auto-generated method stub
		return null;
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
		// TODO Auto-generated method stub
		return null;
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
		// TODO Auto-generated method stub
		return null;
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.SBMLReader#readUnit(java.lang.Object)
	 */
	public Unit readUnit(Object unit) {
		// TODO Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.SBMLReader#readUnitDefinition(java.lang.Object)
	 */
	public UnitDefinition readUnitDefinition(Object unitDefinition) {
		// TODO Auto-generated method stub
		return null;
	}

	public Date convertDate(Object d) {
		// TODO Auto-generated method stub
		return null;
	}

	public String getWarnings() {
		// TODO Auto-generated method stub
		return null;
	}

	public int getNumErrors() {
		// TODO Auto-generated method stub
		return 0;
	}
}
