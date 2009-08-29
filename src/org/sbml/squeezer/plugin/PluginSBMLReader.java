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

import java.io.IOException;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

import jp.sbi.celldesigner.plugin.PluginCompartment;
import jp.sbi.celldesigner.plugin.PluginKineticLaw;
import jp.sbi.celldesigner.plugin.PluginModel;
import jp.sbi.celldesigner.plugin.PluginModifierSpeciesReference;
import jp.sbi.celldesigner.plugin.PluginParameter;
import jp.sbi.celldesigner.plugin.PluginReaction;
import jp.sbi.celldesigner.plugin.PluginSpecies;
import jp.sbi.celldesigner.plugin.PluginSpeciesReference;

import org.sbml.Compartment;
import org.sbml.KineticLaw;
import org.sbml.Model;
import org.sbml.ModifierSpeciesReference;
import org.sbml.Parameter;
import org.sbml.Reaction;
import org.sbml.SBO;
import org.sbml.Species;
import org.sbml.SpeciesReference;
import org.sbml.StoichiometryMath;
import org.sbml.squeezer.SBMLsqueezer;
import org.sbml.squeezer.io.AbstractSBMLReader;
import org.sbml.squeezer.resources.Resource;

public class PluginSBMLReader extends AbstractSBMLReader {

	private static final String error = " must be an instance of ";
	private List<Integer> listOfPossibleEnzymes;

	/**
	 * 
	 */
	public PluginSBMLReader(List<Integer> listOfPossibleEnzymes) {
		super();
		this.listOfPossibleEnzymes = listOfPossibleEnzymes;
	}

	/**
	 * get a model from the celldesigneroutput, converts it to sbmlsqueezer
	 * format and stores it
	 * 
	 * @param model
	 */
	public PluginSBMLReader(PluginModel model,
			List<Integer> listOfPossibleEnzymes) {
		super(model);
		this.listOfPossibleEnzymes = listOfPossibleEnzymes;
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
		KineticLaw kinlaw = new KineticLaw();
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
			Model m = new Model(pm.getId());
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
				new Species(plumod.getSpeciesInstance().getId()));
		/*
		 * Set SBO term.
		 */
		mod.setSBOTerm(SBMLsqueezer.convertAlias2SBO(plumod
				.getModificationType()));
		if (SBO.isCatalyst(mod.getSBOTerm())) {
			PluginSpecies species = plumod.getSpeciesInstance();
			String speciesAliasType = species.getSpeciesAlias(0).getType()
					.equals("PROTEIN") ? species.getSpeciesAlias(0)
					.getProtein().getType() : species.getSpeciesAlias(0)
					.getType();
			if (listOfPossibleEnzymes.contains(Integer.valueOf(SBMLsqueezer
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
		Parameter para = new Parameter(pp.getId());
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
		Reaction reaction = new Reaction(r.getId());
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
		reaction.setSBOTerm(SBMLsqueezer.convertAlias2SBO(r.getReactionType()));
		reaction.setKineticLaw(readKineticLaw(r.getKineticLaw()));
		reaction.setFast(r.getFast());
		reaction.setReversible(r.getReversible());
		addAllSBaseChangeListenersTo(reaction);
		return reaction;
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
		Species s = new Species(spec.getId());
		s.setSBOTerm(SBMLsqueezer.convertAlias2SBO(spec.getSpeciesAlias(
				spec.getId()).getAliasID()));
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
				.getSpeciesInstance().getId()));
		spec.setStoichiometry(specref.getStoichiometry());
		addAllSBaseChangeListenersTo(spec);
		return spec;
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
}
