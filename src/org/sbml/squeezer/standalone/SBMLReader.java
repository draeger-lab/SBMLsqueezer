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

import org.sbml.KineticLaw;
import org.sbml.Model;
import org.sbml.ModifierSpeciesReference;
import org.sbml.Parameter;
import org.sbml.Reaction;
import org.sbml.Species;
import org.sbml.SpeciesReference;
import org.sbml.libsbml.SBMLDocument;
import org.sbml.squeezer.io.AbstractSBMLconverter;

/**
 * @author Andreas Dr&auml;ger <a
 *         href="mailto:andreas.draeger@uni-tuebingen.de">
 *         andreas.draeger@uni-tuebingen.de</a>
 * 
 */
public class SBMLReader extends AbstractSBMLconverter {

	private Model model;
	private SBMLDocument doc;

	/**
	 * get a libsbml model converts it to sbmlsquezzer format and save the new
	 * model
	 * 
	 * @param model
	 */
	public SBMLReader(org.sbml.libsbml.Model model) {
		super();
		this.model = convert(model);
		this.model.addChangeListener(this);
	}

	/**
	 * get a xml file, converts it and save the sbmlsquezzer model
	 * 
	 * @param fileName
	 */
	public SBMLReader(String fileName) {
		super();
		doc = (new org.sbml.libsbml.SBMLReader()).readSBML(fileName);
		this.model = convert(doc.getModel());
		this.model.addChangeListener(this);
	}

	public Model getModel() {
		return model;
	}

	public Model convert(org.sbml.libsbml.Model model) {
		Model m = new Model(model.getId());
		int i;
		for (i = 0; i < model.getNumReactions(); i++) {
			m.addReaction(convert(model.getReaction(i)));
		}
		for (i = 0; i < model.getNumSpecies(); i++) {
			m.addSpecies(convert(model.getSpecies(i)));
		}
		for (i = 0; i< model.getNumParameters(); i++) {
			m.addParameter(convert(model.getParameter(i)));
		}
		return m;
	}

	public Reaction convert(org.sbml.libsbml.Reaction reac) {
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
		reaction.setKineticLaw(convert(reac.getKineticLaw()));
		reaction.setFast(reac.getFast());
		reaction.setReversible(reac.getReversible());
		reaction.addChangeListener(this);
		return reaction;
	}

	public Species convert(org.sbml.libsbml.Species spec) {
		Species species = new Species(spec.getId());
		species.addChangeListener(this);
		return species;
	}

	public SpeciesReference convert(org.sbml.libsbml.SpeciesReference specref) {
		SpeciesReference spec = new SpeciesReference(new Species(specref
				.getSpecies()));
		spec.setStoichiometry(specref.getStoichiometry());
		spec.addChangeListener(this);
		return spec;
	}

	public ModifierSpeciesReference convert(
			org.sbml.libsbml.ModifierSpeciesReference plumod) {
		ModifierSpeciesReference mod = new ModifierSpeciesReference(
				new Species(plumod.getSpecies()));
		mod.addChangeListener(this);
		return mod;
	}

	public KineticLaw convert(org.sbml.libsbml.KineticLaw plukinlaw) {
		KineticLaw kinlaw = new KineticLaw();
		kinlaw.setMath(plukinlaw.getMath());
		for (int i = 0; i < plukinlaw.getNumParameters(); i++) {
			kinlaw.addParameter(convert(plukinlaw.getParameter(i)));
		}
		kinlaw.addChangeListener(this);
		return kinlaw;
	}

	public Parameter convert(org.sbml.libsbml.Parameter plupara) {
		Parameter para = new Parameter(plupara.getId());
		para.addChangeListener(this);
		return para;
	}

}
