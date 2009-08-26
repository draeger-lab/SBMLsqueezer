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
import org.sbml.Compartment;
import org.sbml.KineticLaw;
import org.sbml.Model;
import org.sbml.ModifierSpeciesReference;
import org.sbml.Parameter;
import org.sbml.Reaction;
import org.sbml.Species;
import org.sbml.SpeciesReference;
import org.sbml.libsbml.SBMLDocument;
import org.sbml.squeezer.io.AbstractSBMLReader;
import org.sbml.squeezer.io.SBaseChangedListener;

/**
 * @author Andreas Dr&auml;ger <a
 *         href="mailto:andreas.draeger@uni-tuebingen.de">
 *         andreas.draeger@uni-tuebingen.de</a>
 * 
 */
public class LibSBMLReader extends AbstractSBMLReader {

	private Set<SBMLDocument> setOfDocuments;

	public LibSBMLReader() {
		super();
		setOfDocuments = new HashSet<SBMLDocument>();
	}

	/**
	 * get a libsbml model converts it to sbmlsquezzer format and save the new
	 * model
	 * 
	 * @param model
	 */
	public LibSBMLReader(org.sbml.libsbml.Model model) {
		super(model);
		setOfDocuments = new HashSet<SBMLDocument>();
	}

	/**
	 * get a xml file, converts it and save the sbmlsquezzer model
	 * 
	 * @param fileName
	 */
	public LibSBMLReader(String fileName) {
		super();
		setOfDocuments = new HashSet<SBMLDocument>();
		readModel(fileName);
	}

	public Compartment convert(org.sbml.libsbml.Compartment compartment) {
		Compartment c = new Compartment(compartment.getId(), compartment
				.getName());
		c.setConstant(compartment.getConstant());
		if (compartment.isSetOutside()) {
			Compartment outside = getModel().getCompartment(
					compartment.getOutside());
			if (outside == null)
				getModel().addCompartment(convert(compartment));
			c.setOutside(outside);
		}
		c.setSize(compartment.getSize());
		c.setSpatialDimensions((int) compartment.getSpatialDimensions());
		return c;
	}

	public KineticLaw convert(org.sbml.libsbml.KineticLaw plukinlaw) {
		KineticLaw kinlaw = new KineticLaw();
		kinlaw.setNotes(plukinlaw.getNotesString());
		kinlaw.setSBOTerm(plukinlaw.getSBOTerm());
		if (plukinlaw.isSetMath()) {
			ASTNode ast = convert(plukinlaw.getMath(), kinlaw);
			ast.reduceToBinary();
			kinlaw.setMath(ast);
		}
		for (int i = 0; i < plukinlaw.getNumParameters(); i++)
			kinlaw.addParameter(convert(plukinlaw.getParameter(i)));
		addAllSBaseChangeListenersTo(kinlaw);
		return kinlaw;
	}

	public Model readModel(Object model) {
		if (model instanceof String) {
			SBMLDocument doc = (new org.sbml.libsbml.SBMLReader())
					.readSBML(model.toString());
			setOfDocuments.add(doc);
			model = doc.getModel();
		}
		if (model instanceof org.sbml.libsbml.Model) {
			org.sbml.libsbml.Model m = (org.sbml.libsbml.Model) model;
			Model myModel = new Model(m.getId());
			this.model = myModel;
			int i;
			for (i = 0; i < m.getNumCompartments(); i++)
				myModel.addCompartment(convert(m.getCompartment(i)));
			for (i = 0; i < m.getNumReactions(); i++)
				myModel.addReaction(readReaction(m.getReaction(i)));
			for (i = 0; i < m.getNumSpecies(); i++)
				myModel.addSpecies(convert(m.getSpecies(i)));
			for (i = 0; i < m.getNumParameters(); i++)
				myModel.addParameter(convert(m.getParameter(i)));
			addAllSBaseChangeListenersTo(myModel);
			return myModel;
		}
		return null;
	}

	public ModifierSpeciesReference convert(
			org.sbml.libsbml.ModifierSpeciesReference plumod) {
		ModifierSpeciesReference mod = new ModifierSpeciesReference(
				new Species(plumod.getSpecies()));
		addAllSBaseChangeListenersTo(mod);
		return mod;
	}

	public Parameter convert(org.sbml.libsbml.Parameter plupara) {
		Parameter para = new Parameter(plupara.getId());
		addAllSBaseChangeListenersTo(para);
		return para;
	}

	public Reaction readReaction(Object reac) {
		if (!(reac instanceof org.sbml.libsbml.Reaction))
			throw new IllegalArgumentException("reaction must be an instance of org.sbml.libsbml.Reaction.");
		org.sbml.libsbml.Reaction r = (org.sbml.libsbml.Reaction) reac;
		Reaction reaction = new Reaction(r.getId());
		for (int i = 0; i < r.getNumReactants(); i++)
			reaction.addReactant(convert(r.getReactant(i)));
		for (int i = 0; i < r.getNumProducts(); i++)
			reaction.addProduct(convert(r.getProduct(i)));
		for (int i = 0; i < r.getNumModifiers(); i++)
			reaction.addModifier(convert(r.getModifier(i)));
		reaction.setSBOTerm(r.getSBOTerm());
		if (r.isSetKineticLaw())
			reaction.setKineticLaw(convert(r.getKineticLaw()));
		if (r.isSetName())
			reaction.setName(r.getName());
		reaction.setSBOTerm(r.getSBOTerm());
		reaction.setNotes(r.getNotesString());
		reaction.setFast(r.getFast());
		reaction.setReversible(r.getReversible());
		addAllSBaseChangeListenersTo(reaction);
		return reaction;
	}

	public Species convert(org.sbml.libsbml.Species spec) {
		Species species = new Species(spec.getId());
		species.setSBOTerm(spec.getSBOTerm());
		if (spec.isSetCharge())
			species.setCharge(spec.getCharge());
		species.setBoundaryCondition(spec.getBoundaryCondition());
		species.setConstant(spec.getConstant());
		species.setCompartment(getModel().getCompartment(spec.getCompartment()));
		addAllSBaseChangeListenersTo(species);
		return species;
	}

	public SpeciesReference convert(org.sbml.libsbml.SpeciesReference specref) {
		SpeciesReference spec = new SpeciesReference(new Species(specref
				.getSpecies()));
		spec.setStoichiometry(specref.getStoichiometry());
		addAllSBaseChangeListenersTo(spec);
		return spec;
	}
}
