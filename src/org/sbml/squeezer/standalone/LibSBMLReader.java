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
import java.util.List;
import java.util.Set;

import org.sbml.ASTNode;
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
	private List<Integer> listOfPossibleEnzymes;
	private static final String error = " must be an instance of ";

	public LibSBMLReader(List<Integer> listOfPossibleEnzymes) {
		super();
		this.listOfPossibleEnzymes = listOfPossibleEnzymes;
		setOfDocuments = new HashSet<SBMLDocument>();
	}

	/**
	 * get a libsbml model converts it to sbmlsquezzer format and save the new
	 * model
	 * 
	 * @param model
	 */
	public LibSBMLReader(org.sbml.libsbml.Model model, List<Integer> listOfPossibleEnzymes) {
		super(model);
		this.listOfPossibleEnzymes = listOfPossibleEnzymes;
		setOfDocuments = new HashSet<SBMLDocument>();
	}

	/**
	 * get a xml file, converts it and save the sbmlsquezzer model
	 * 
	 * @param fileName
	 */
	public LibSBMLReader(String fileName, List<Integer> listOfPossibleEnzymes) {
		super();
		this.listOfPossibleEnzymes = listOfPossibleEnzymes;
		setOfDocuments = new HashSet<SBMLDocument>();
		readModel(fileName);
	}

	/*
	 * (non-Javadoc)
	 * @see org.sbml.SBMLReader#readCompartment(java.lang.Object)
	 */
	public Compartment readCompartment(Object compartment) {
		if (!(compartment instanceof org.sbml.libsbml.Compartment))
			throw new IllegalArgumentException("compartment" + error
					+ "org.sbml.libsbml.Compartment");
		org.sbml.libsbml.Compartment comp = (org.sbml.libsbml.Compartment) compartment;
		Compartment c = new Compartment(comp.getId());
		if (comp.isSetMetaId())
			c.setMetaId(comp.getMetaId());
		if (comp.isSetSBOTerm())
			c.setSBOTerm(comp.getSBOTerm());
		if (comp.isSetNotes())
			c.setNotes(comp.getNotesString());
		if (comp.isSetName())
			c.setName(comp.getName());
		if (comp.isSetOutside()) {
			Compartment outside = getModel().getCompartment(comp.getOutside());
			if (outside == null)
				getModel().addCompartment(readCompartment(compartment));
			c.setOutside(outside);
		}
		c.setConstant(comp.getConstant());
		c.setSize(comp.getSize());
		c.setSpatialDimensions((int) comp.getSpatialDimensions());
		return c;
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
		if (kl.isSetMetaId())
			kinlaw.setMetaId(kl.getMetaId());
		if (kl.isSetSBOTerm())
			kinlaw.setSBOTerm(kl.getSBOTerm());
		if (kl.isSetNotes())
			kinlaw.setNotes(kl.getNotesString());
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
			if (m.isSetMetaId())
				this.model.setMetaId(m.getMetaId());
			if (m.isSetName())
				this.model.setName(m.getName());
			if (m.isSetNotes())
				this.model.setNotes(m.getNotesString());
			if (m.isSetSBOTerm())
				this.model.setSBOTerm(m.getSBOTerm());
			for (i = 0; i < m.getNumCompartments(); i++)
				this.model.addCompartment(readCompartment(m.getCompartment(i)));
			for (i = 0; i < m.getNumSpecies(); i++)
				this.model.addSpecies(readSpecies(m.getSpecies(i)));
			for (i = 0; i < m.getNumParameters(); i++)
				this.model.addParameter(readParameter(m.getParameter(i)));
			for (i = 0; i < m.getNumReactions(); i++)
				this.model.addReaction(readReaction(m.getReaction(i)));
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
		if (msr.isSetId())
			mod.setId(msr.getId());
		if (msr.isSetName())
			mod.setName(msr.getName());
		if (msr.isSetMetaId())
			mod.setMetaId(msr.getMetaId());
		if (msr.isSetSBOTerm()) {
			mod.setSBOTerm(msr.getSBOTerm());
			if (!SBO.isEnzymaticCatalysis(mod.getSBOTerm())
					&& listOfPossibleEnzymes
							.contains(Integer.valueOf(mod.getSBOTerm())))
				mod.setSBOTerm(SBO.getEnzymaticCatalysis());
		}
		if (msr.isSetNotes())
			mod.setNotes(msr.getNotesString());
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
		for (int i = 0; i < r.getNumReactants(); i++)
			reaction.addReactant(readSpeciesReference(r.getReactant(i)));
		for (int i = 0; i < r.getNumProducts(); i++)
			reaction.addProduct(readSpeciesReference(r.getProduct(i)));
		for (int i = 0; i < r.getNumModifiers(); i++)
			reaction
					.addModifier(readModifierSpeciesReference(r.getModifier(i)));
		if (r.isSetSBOTerm())
			reaction.setSBOTerm(r.getSBOTerm());
		if (r.isSetKineticLaw())
			reaction.setKineticLaw(readKineticLaw(r.getKineticLaw()));
		if (r.isSetName())
			reaction.setName(r.getName());
		if (r.isSetNotes())
			reaction.setNotes(r.getNotesString());
		if (r.isSetMetaId())
			reaction.setMetaId(r.getMetaId());
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
		if (!(species instanceof org.sbml.libsbml.Species))
			throw new IllegalArgumentException("species" + error
					+ "org.sbml.libsbml.Species.");
		org.sbml.libsbml.Species spec = (org.sbml.libsbml.Species) species;
		Species s = new Species(spec.getId());
		if (spec.isSetName())
			s.setName(spec.getName());
		if (spec.isSetMetaId())
			s.setMetaId(spec.getMetaId());
		if (spec.isSetNotes())
			s.setNotes(spec.getNotesString());
		if (spec.isSetSBOTerm())
			s.setSBOTerm(spec.getSBOTerm());
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
		if (specref.isSetMetaId())
			spec.setMetaId(specref.getMetaId());
		if (specref.isSetSBOTerm())
			spec.setSBOTerm(specref.getSBOTerm());
		if (specref.isSetNotes())
			spec.setNotes(specref.getNotesString());
		if (specref.isSetId())
			spec.setId(specref.getId());
		if (specref.isSetName())
			spec.setName(specref.getName());
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
	 * @see org.sbml.SBMLReader#readStoichiometricMath(java.lang.Object)
	 */
	public StoichiometryMath readStoichiometricMath(Object stoichiometryMath) {
		org.sbml.libsbml.StoichiometryMath s = (org.sbml.libsbml.StoichiometryMath) stoichiometryMath;
		StoichiometryMath sm = new StoichiometryMath();
		if (s.isSetMetaId())
			sm.setMetaId(s.getMetaId());
		if (s.isSetSBOTerm())
			sm.setSBOTerm(s.getSBOTerm());
		if (s.isSetNotes())
			sm.setNotes(s.getNotesString());
		if (s.isSetMath())
			sm.setMath(convert(s.getMath(), sm));
		return sm;
	}
}
