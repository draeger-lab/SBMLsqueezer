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
import java.util.Properties;

import jp.sbi.celldesigner.plugin.PluginKineticLaw;
import jp.sbi.celldesigner.plugin.PluginModel;
import jp.sbi.celldesigner.plugin.PluginModifierSpeciesReference;
import jp.sbi.celldesigner.plugin.PluginParameter;
import jp.sbi.celldesigner.plugin.PluginReaction;
import jp.sbi.celldesigner.plugin.PluginSpecies;
import jp.sbi.celldesigner.plugin.PluginSpeciesReference;

import org.sbml.KineticLaw;
import org.sbml.Model;
import org.sbml.Reaction;
import org.sbml.Species;
import org.sbml.SpeciesReference;
import org.sbml.ModifierSpeciesReference;
import org.sbml.Parameter;
import org.sbml.squeezer.io.AbstractSBMLconverter;
import org.sbml.squeezer.resources.Resource;

public class PluginSBMLReader extends AbstractSBMLconverter {

	private Model model;
	private static Properties alias2sbo;
	static {
		try {
			alias2sbo = Resource.readProperties(Resource.class.getResource(
					"cfg/Alias2SBO.cfg").getPath());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * get a model from the celldesigneroutput, converts it to sbmlsqueezer
	 * format and stores it
	 * 
	 * @param model
	 */

	public PluginSBMLReader(PluginModel model) {
		super();
		this.model = convert(model);
		this.model.addChangeListener(this);
	}

	public Model convert(PluginModel model) {
		Model m = new Model(model.getId());
		for (int i = 0; i < model.getNumReactions(); i++) {
			m.addReaction(convert(model.getReaction(i)));
		}
		for (int i = 0; i < model.getNumSpecies(); i++) {
			m.addSpecies(convert(model.getSpecies(i)));
		}
		return m;
	}

	public Reaction convert(PluginReaction reac) {
		Reaction reaction = new Reaction(reac.getId());
		for (int i = 0; i < reac.getNumReactants(); i++) {
			reaction.addReactant(convert(reac.getReactant(i)));
		}
		for (int i = 0; i < reac.getNumProducts(); i++) {
			reaction.addProduct(convert(reac.getProduct(i)));
		}
		for (int i = 0; i < reac.getNumModifiers(); i++) {
			reaction.addModifier(convert(reac.getModifier(i)));
		}
		reaction.setSBOTerm(Integer.parseInt(alias2sbo.get(
				reac.getReactionType()).toString()));
		reaction.setKineticLaw(convert(reac.getKineticLaw()));
		reaction.setFast(reac.getFast());
		reaction.setReversible(reac.getReversible());
		reaction.addChangeListener(this);
		return reaction;
	}

	public Species convert(PluginSpecies spec) {
		Species species = new Species(spec.getId());
		species.setSBOTerm(Integer.parseInt(alias2sbo.get(
				spec.getSpeciesAlias(spec.getId())).toString()));
		species.addChangeListener(this);
		return species;
	}

	public SpeciesReference convert(PluginSpeciesReference specref) {
		SpeciesReference spec = new SpeciesReference(new Species(specref
				.getSpeciesInstance().getId()));
		spec.setStoichiometry(specref.getStoichiometry());
		spec.addChangeListener(this);
		return spec;
	}

	public ModifierSpeciesReference convert(
			PluginModifierSpeciesReference plumod) {
		ModifierSpeciesReference mod = new ModifierSpeciesReference(
				new Species(plumod.getSpeciesInstance().getId()));
		mod.addChangeListener(this);
		return mod;
	}

	public KineticLaw convert(PluginKineticLaw plukinlaw) {
		KineticLaw kinlaw = new KineticLaw();
		kinlaw.setMath(plukinlaw.getMath());
		for (int i = 0; i < plukinlaw.getNumParameters(); i++) {
			kinlaw.addParameter(convert(plukinlaw.getParameter(i)));
		}
		kinlaw.addChangeListener(this);
		return kinlaw;
	}

	public Parameter convert(PluginParameter plupara) {
		Parameter para = new Parameter(plupara.getId());
		para.addChangeListener(this);
		return para;
	}
}
