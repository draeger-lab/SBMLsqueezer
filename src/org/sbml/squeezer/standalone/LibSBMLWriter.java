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
 *  along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.sbml.squeezer.standalone;

import org.sbml.jlibsbml.AssignmentRule;
import org.sbml.jlibsbml.CVTerm;
import org.sbml.jlibsbml.Compartment;
import org.sbml.jlibsbml.CompartmentType;
import org.sbml.jlibsbml.Constraint;
import org.sbml.jlibsbml.Delay;
import org.sbml.jlibsbml.Event;
import org.sbml.jlibsbml.EventAssignment;
import org.sbml.jlibsbml.FunctionDefinition;
import org.sbml.jlibsbml.InitialAssignment;
import org.sbml.jlibsbml.KineticLaw;
import org.sbml.jlibsbml.MathContainer;
import org.sbml.jlibsbml.Model;
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
import org.sbml.jlibsbml.Trigger;
import org.sbml.jlibsbml.Unit;
import org.sbml.jlibsbml.UnitDefinition;
import org.sbml.libsbml.SBMLWriter;
import org.sbml.libsbml.libsbmlConstants;
import org.sbml.squeezer.io.AbstractSBMLWriter;

/**
 * @author Andreas Dr&auml;ger <a
 *         href="mailto:andreas.draeger@uni-tuebingen.de">
 *         andreas.draeger@uni-tuebingen.de</a>
 * 
 */
public class LibSBMLWriter extends AbstractSBMLWriter {

	private static LibSBMLReader reader = new LibSBMLReader(SBO
			.getDefaultPossibleEnzymes());

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.squeezer.io.AbstractSBMLWriter#saveChanges(org.sbml.Model,
	 * java.lang.Object)
	 */
	// @Override
	public void saveChanges(Model model, Object orig) {
		if (!(orig instanceof org.sbml.libsbml.Model))
			throw new IllegalArgumentException(
					"only instances of org.sbml.libsbml.Model can be considered.");
		org.sbml.libsbml.Model mo = (org.sbml.libsbml.Model) orig;
		long i;
		// Unit definitions
		for (UnitDefinition ud : model.getListOfUnitDefinitions())
			if (!ud.equals(UnitDefinition.SUBSTANCE)
					&& !ud.equals(UnitDefinition.VOLUME)
					&& !ud.equals(UnitDefinition.AREA)
					&& !ud.equals(UnitDefinition.LENGTH)
					&& !ud.equals(UnitDefinition.TIME)) {
				org.sbml.libsbml.UnitDefinition libU = mo.getUnitDefinition(ud
						.getId());
				if (libU != null) {
					saveNamedSBaseProperties(ud, libU);
					for (Unit u : ud.getListOfUnits()) {
						boolean contains = false;
						for (int j = 0; j < libU.getNumUnits() && !contains; j++) {
							Unit unit = reader.readUnit(libU.getUnit(j));
							if (unit.equals(u))
								contains = true;
						}
						if (!contains)
							libU.addUnit(writeUnit(u));
					}
				} else
					mo.addUnitDefinition(writeUnitDefinition(ud));
			}
		// remove unnecessary units
		for (i = mo.getNumUnitDefinitions() - 1; i >= 0; i--) {
			org.sbml.libsbml.UnitDefinition ud = mo.getUnitDefinition(i);
			if (model.getUnitDefinition(ud.getId()) == null)
				mo.getListOfUnitDefinitions().remove(i);
		}
		// Compartments
		for (Compartment c : model.getListOfCompartments()) {
			if (mo.getCompartment(c.getId()) == null)
				mo.addCompartment(writeCompartment(c));
			else
				saveCompartmentProperties(c, mo.getCompartment(c.getId()));
		}
		// remove unnecessary compartments
		for (i = mo.getNumCompartments() - 1; i >= 0; i--) {
			org.sbml.libsbml.Compartment c = mo.getCompartment(i);
			if (model.getCompartment(c.getId()) == null)
				mo.getListOfCompartments().remove(i);
		}
		// Species
		for (Species s : model.getListOfSpecies()) {
			if (mo.getSpecies(s.getId()) == null)
				mo.addSpecies(writeSpecies(s));
			else
				saveSpeciesProperties(s, mo.getSpecies(s.getId()));
		}
		// remove unnecessary species
		for (i = mo.getNumSpecies() - 1; i >= 0; i--) {
			org.sbml.libsbml.Species s = mo.getSpecies(i);
			if (model.getSpecies(s.getId()) == null)
				mo.getListOfSpecies().remove(i);
		}
		// add or change parameters
		for (Parameter p : model.getListOfParameters()) {
			if (mo.getParameter(p.getId()) == null)
				mo.addParameter(writeParameter(p));
			else
				saveParameterProperties(p, mo.getParameter(p.getId()));
		}
		// remove parameters
		for (i = mo.getNumParameters() - 1; i >= 0; i--) {
			org.sbml.libsbml.Parameter p = mo.getParameter(i);
			if (model.getParameter(p.getId()) == null)
				mo.getListOfParameters().remove(i);
		}
		// add or change reactions
		for (Reaction r : model.getListOfReactions()) {
			if (mo.getReaction(r.getId()) == null)
				mo.addReaction(writeReaction(r));
			else
				saveReactionProperties(r, mo.getReaction(r.getId()));
		}
		// remove reactions
		for (i = mo.getNumReactions() - 1; i >= 0; i--) {
			org.sbml.libsbml.Reaction r = mo.getReaction(i);
			if (model.getReaction(r.getId()) == null)
				mo.getListOfReactions().remove(i);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.sbml.jlibsbml.SBMLWriter#saveCompartmentProperties(org.sbml.jlibsbml
	 * .Compartment, java.lang.Object)
	 */
	public void saveCompartmentProperties(Compartment c, Object compartment) {
		if (!(compartment instanceof org.sbml.libsbml.Compartment))
			throw new IllegalArgumentException(
					"compartment must be an instance of org.sbml.libsbml.Compartment.");
		org.sbml.libsbml.Compartment comp = (org.sbml.libsbml.Compartment) compartment;
		saveNamedSBaseProperties(c, comp);
		if (c.getSize() != comp.getSize())
			comp.setSize(c.getSize());
		if (!c.getCompartmentType().equals(comp.getCompartmentType()))
			comp.setCompartmentType(c.getCompartmentType());
		if (c.getSpatialDimensions() != comp.getSpatialDimensions())
			comp.setSpatialDimensions(c.getSpatialDimensions());
		if (!c.getUnits().equals(comp.getUnits()))
			comp.setUnits(c.getUnits());
		if (!c.getOutside().equals(comp.getOutside()))
			comp.setOutside(c.getOutside());
		if (c.getConstant() != comp.getConstant())
			comp.setConstant(c.getConstant());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.sbml.jlibsbml.SBMLWriter#saveCVTermProperties(org.sbml.jlibsbml.CVTerm
	 * , java.lang.Object)
	 */
	public void saveCVTermProperties(CVTerm cvt, Object term) {
		if (!(term instanceof org.sbml.libsbml.CVTerm))
			throw new IllegalArgumentException(
					"term must be an instance of org.sbml.libsbml.CVTerm.");
		org.sbml.libsbml.CVTerm t = (org.sbml.libsbml.CVTerm) term;
		org.sbml.libsbml.CVTerm myTerm = writeCVTerm(cvt);
		if (myTerm.getQualifierType() != t.getQualifierType())
			t.setQualifierType(myTerm.getQualifierType());
		if (myTerm.getBiologicalQualifierType() != t
				.getBiologicalQualifierType())
			t.setBiologicalQualifierType(myTerm.getBiologicalQualifierType());
		if (myTerm.getModelQualifierType() != t.getModelQualifierType())
			t.setModelQualifierType(myTerm.getModelQualifierType());
		// add missing resources
		for (int i = 0; i < myTerm.getNumResources(); i++) {
			boolean contains = false;
			for (int j = 0; j < t.getNumResources() && !contains; j++) {
				if (myTerm.getResourceURI(i).equals(t.getResourceURI(j)))
					contains = true;
			}
			if (!contains)
				t.addResource(myTerm.getResourceURI(i));
		}
		// remove old resources
		for (long i = t.getNumResources() - 1; i >= 0; i--) {
			boolean contains = false;
			for (int j = 0; j < myTerm.getNumResources() && !contains; j++)
				if (myTerm.getResourceURI(j).equals(t.getResourceURI(i)))
					contains = true;
			if (!contains)
				t.removeResource(t.getResourceURI(i));
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.sbml.jlibsbml.SBMLWriter#saveKineticLawProperties(org.sbml.jlibsbml
	 * .KineticLaw, java.lang.Object)
	 */
	public void saveKineticLawProperties(KineticLaw kl, Object kineticLaw) {
		if (!(kineticLaw instanceof org.sbml.libsbml.KineticLaw))
			throw new IllegalArgumentException(
					"kineticLaw must be an instance of org.sbml.libsbml.KineticLaw.");
		org.sbml.libsbml.KineticLaw libKinLaw = (org.sbml.libsbml.KineticLaw) kineticLaw;
		saveMathContainerProperties(kl, libKinLaw);
		// add or change parameters
		for (Parameter p : kl.getListOfParameters()) {
			if (libKinLaw.getParameter(p.getId()) == null)
				libKinLaw.addParameter(writeParameter(p));
			else
				saveParameterProperties(p, libKinLaw.getParameter(p.getId()));
		}
		// remove parameters
		for (long i = libKinLaw.getNumParameters() - 1; i >= 0; i--) {
			org.sbml.libsbml.Parameter p = libKinLaw.getParameter(i);
			if (kl.getParameter(p.getId()) == null)
				libKinLaw.getListOfParameters().remove(i);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.sbml.jlibsbml.SBMLWriter#saveMathContainerProperties(org.sbml.jlibsbml
	 * .MathContainer, java.lang.Object)
	 */
	public void saveMathContainerProperties(MathContainer mc, Object sbase) {
		if (mc instanceof NamedSBase)
			saveNamedSBaseProperties((NamedSBase) mc, sbase);
		else
			saveSBaseProperties(mc, sbase);
		if (sbase instanceof org.sbml.libsbml.KineticLaw) {
			org.sbml.libsbml.KineticLaw kl = (org.sbml.libsbml.KineticLaw) sbase;
			if (mc.isSetMath())
				kl.setMath(convert(mc.getMath()));
		} else if (sbase instanceof org.sbml.libsbml.Rule) {
			org.sbml.libsbml.Rule kl = (org.sbml.libsbml.Rule) sbase;
			if (mc.isSetMath())
				kl.setMath(convert(mc.getMath()));
		}
		// TODO other data types..
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seeorg.sbml.SBMLWriter#saveModifierSpeciesReferenceProperties(org.sbml.
	 * ModifierSpeciesReference, java.lang.Object)
	 */
	// @Override
	public void saveModifierSpeciesReferenceProperties(
			ModifierSpeciesReference modifierSpeciesReference, Object msr) {
		if (!(msr instanceof org.sbml.libsbml.ModifierSpeciesReference))
			throw new IllegalArgumentException(
					"msr must be an instance of org.sbml.libsbml.ModifierSpeciesReference.");
		saveNamedSBaseProperties(modifierSpeciesReference, msr);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.SBMLWriter#saveNamedSBaseProperties(org.sbml.NamedSBase,
	 * java.lang.Object)
	 */
	// @Override
	public void saveNamedSBaseProperties(NamedSBase nsb, Object sb) {
		if (!(sb instanceof org.sbml.libsbml.SBase))
			throw new IllegalArgumentException(
					"sb must be an instance of org.sbml.libsbml.SBase.");
		saveSBaseProperties(nsb, sb);
		org.sbml.libsbml.SBase libSBase = (org.sbml.libsbml.SBase) sb;
		if (libSBase instanceof org.sbml.libsbml.Compartment) {
			org.sbml.libsbml.Compartment c = (org.sbml.libsbml.Compartment) libSBase;
			if (!nsb.getId().equals(c.getId()))
				c.setId(nsb.getId());
			if (!nsb.getName().equals(c.getName()))
				c.setName(nsb.getName());
		} else if (libSBase instanceof org.sbml.libsbml.CompartmentType) {
			org.sbml.libsbml.CompartmentType c = (org.sbml.libsbml.CompartmentType) libSBase;
			if (!nsb.getId().equals(c.getId()))
				c.setId(nsb.getId());
			if (!nsb.getName().equals(c.getName()))
				c.setName(nsb.getName());
		} else if (libSBase instanceof org.sbml.libsbml.Event) {
			org.sbml.libsbml.Event c = (org.sbml.libsbml.Event) libSBase;
			if (!nsb.getId().equals(c.getId()))
				c.setId(nsb.getId());
			if (!nsb.getName().equals(c.getName()))
				c.setName(nsb.getName());
		} else if (libSBase instanceof org.sbml.libsbml.FunctionDefinition) {
			org.sbml.libsbml.FunctionDefinition c = (org.sbml.libsbml.FunctionDefinition) libSBase;
			if (!nsb.getId().equals(c.getId()))
				c.setId(nsb.getId());
			if (!nsb.getName().equals(c.getName()))
				c.setName(nsb.getName());
		} else if (libSBase instanceof org.sbml.libsbml.Model) {
			org.sbml.libsbml.Model c = (org.sbml.libsbml.Model) libSBase;
			if (!nsb.getId().equals(c.getId()))
				c.setId(nsb.getId());
			if (!nsb.getName().equals(c.getName()))
				c.setName(nsb.getName());
		} else if (libSBase instanceof org.sbml.libsbml.Parameter) {
			org.sbml.libsbml.Parameter c = (org.sbml.libsbml.Parameter) libSBase;
			if (!nsb.getId().equals(c.getId()))
				c.setId(nsb.getId());
			if (!nsb.getName().equals(c.getName()))
				c.setName(nsb.getName());
		} else if (libSBase instanceof org.sbml.libsbml.Reaction) {
			org.sbml.libsbml.Reaction c = (org.sbml.libsbml.Reaction) libSBase;
			if (!nsb.getId().equals(c.getId()))
				c.setId(nsb.getId());
			if (!nsb.getName().equals(c.getName()))
				c.setName(nsb.getName());
		} else if (libSBase instanceof org.sbml.libsbml.SimpleSpeciesReference) {
			org.sbml.libsbml.SimpleSpeciesReference c = (org.sbml.libsbml.SimpleSpeciesReference) libSBase;
			if (!nsb.getId().equals(c.getId()))
				c.setId(nsb.getId());
			if (!nsb.getName().equals(c.getName()))
				c.setName(nsb.getName());
		} else if (libSBase instanceof org.sbml.libsbml.Species) {
			org.sbml.libsbml.Species c = (org.sbml.libsbml.Species) libSBase;
			if (!nsb.getId().equals(c.getId()))
				c.setId(nsb.getId());
			if (!nsb.getName().equals(c.getName()))
				c.setName(nsb.getName());
		} else if (libSBase instanceof org.sbml.libsbml.SpeciesType) {
			org.sbml.libsbml.SpeciesType c = (org.sbml.libsbml.SpeciesType) libSBase;
			if (!nsb.getId().equals(c.getId()))
				c.setId(nsb.getId());
			if (!nsb.getName().equals(c.getName()))
				c.setName(nsb.getName());
		} else if (libSBase instanceof org.sbml.libsbml.UnitDefinition) {
			org.sbml.libsbml.UnitDefinition c = (org.sbml.libsbml.UnitDefinition) libSBase;
			if (!nsb.getId().equals(c.getId()))
				c.setId(nsb.getId());
			if (!nsb.getName().equals(c.getName()))
				c.setName(nsb.getName());
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.sbml.jlibsbml.SBMLWriter#saveParameterProperties(org.sbml.jlibsbml
	 * .Parameter, java.lang.Object)
	 */
	public void saveParameterProperties(Parameter p, Object parameter) {
		if (!(parameter instanceof org.sbml.libsbml.Parameter))
			throw new IllegalArgumentException(
					"parameter must be an instance of org.sbml.libsbml.Parameter.");
		org.sbml.libsbml.Parameter po = (org.sbml.libsbml.Parameter) parameter;
		saveNamedSBaseProperties(p, po);
		if (p.getValue() != po.getValue())
			po.setValue(p.getValue());
		if (p.getConstant() != po.getConstant())
			po.setConstant(p.getConstant());
		if (!p.getUnits().equals(po.getUnits()))
			po.setUnits(p.getUnits());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.sbml.jlibsbml.SBMLWriter#saveReactionProperties(org.sbml.jlibsbml
	 * .Reaction, java.lang.Object)
	 */
	public void saveReactionProperties(Reaction r, Object reaction) {
		if (!(reaction instanceof org.sbml.libsbml.Reaction))
			throw new IllegalArgumentException(
					"reaction must be an instance of org.sbml.libsbml.Reaction.");
		org.sbml.libsbml.Reaction ro = (org.sbml.libsbml.Reaction) reaction;
		long i;
		saveNamedSBaseProperties(r, ro);
		if (r.getFast() != ro.getFast())
			ro.setFast(r.getFast());
		if (r.getReversible() != ro.getReversible())
			ro.setReversible(r.getReversible());
		long contains;
		// reactants.
		for (SpeciesReference sr : r.getListOfReactants()) {
			contains = -1;
			for (i = 0; i < ro.getNumReactants() && contains < 0; i++)
				if (sr.getSpecies().equals(ro.getReactant(i).getSpecies()))
					contains = i;
			if (contains < 0)
				ro.addReactant(writeSpeciesReference(sr));
			else
				saveSpeciesReferenceProperties(sr, ro.getReactant(contains));
		}
		// remove unnecessary reactants.
		for (i = ro.getNumReactants() - 1; i >= 0; i--) {
			org.sbml.libsbml.SpeciesReference roreactant = ro.getReactant(i);
			boolean keep = false;
			for (int j = 0; j < r.getNumReactants() && !keep; j++)
				if (r.getReactant(j).getSpecies().equals(roreactant.getSpecies()))
					keep = true;
			if (!keep)
				ro.getListOfReactants().remove(i);
		}
		for (SpeciesReference sr : r.getListOfProducts()) {
			contains = -1;
			for (i = 0; i < ro.getNumProducts() && contains < 0; i++)
				if (sr.getSpecies().equals(ro.getProduct(i).getSpecies()))
					contains = i;
			if (contains < 0)
				ro.addProduct(writeSpeciesReference(sr));
			else
				saveSpeciesReferenceProperties(sr, ro.getProduct(contains));
		}
		// remove unnecessary products.
		for (i = ro.getNumProducts() - 1; i >= 0; i--) {
			org.sbml.libsbml.SpeciesReference msr = ro.getProduct(i);
			boolean keep = false;
			for (int j = 0; j < r.getNumProducts() && !keep; j++)
				if (r.getProduct(j).getSpecies().equals(msr.getSpecies()))
					keep = true;
			if (!keep)
				ro.getListOfProducts().remove(i);
		}
		// check modifiers
		for (ModifierSpeciesReference mr : r.getListOfModifiers()) {
			contains = -1;
			for (i = 0; i < ro.getNumModifiers() && contains < 0; i++)
				if (mr.getSpecies().equals(ro.getModifier(i)))
					contains = i;
			if (contains < 0)
				ro.addModifier(writeModifierSpeciesReference(mr));
			else
				saveModifierSpeciesReferenceProperties(mr, ro
						.getModifier(contains));
		}
		// remove unnecessary modifiers.
		for (i = ro.getNumModifiers() - 1; i >= 0; i--) {
			org.sbml.libsbml.ModifierSpeciesReference msr = ro.getModifier(i);
			boolean keep = false;
			for (int j = 0; j < r.getNumModifiers() && !keep; j++)
				if (r.getModifier(j).getSpecies().equals(msr.getSpecies()))
					keep = true;
			if (!keep)
				ro.getListOfModifiers().remove(i);
		}
		if (r.isSetKineticLaw()) {
			if (!ro.isSetKineticLaw())
				ro.setKineticLaw(writeKineticLaw(r.getKineticLaw()));
			else if (ro.isSetKineticLaw())
				saveKineticLawProperties(r.getKineticLaw(), ro.getKineticLaw());
		} else if (ro.isSetKineticLaw())
			ro.unsetKineticLaw();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.SBMLWriter#saveSBaseProperties(org.sbml.SBase,
	 * java.lang.Object)
	 */
	// @Override
	public void saveSBaseProperties(SBase s, Object sb) {
		if (!(sb instanceof org.sbml.libsbml.SBase))
			throw new IllegalArgumentException(
					"sb must be an instance of org.sbml.libsbml.SBase.");
		org.sbml.libsbml.SBase po = (org.sbml.libsbml.SBase) sb;
		if (s.isSetMetaId() && po.isSetMetaId()
				&& !s.getMetaId().equals(po.getMetaId()))
			po.setMetaId(s.getMetaId());
		if (!s.getNotesString().equals(po.getNotesString()))
			po.setNotes(s.getNotesString());
		if (s.getSBOTerm() != po.getSBOTerm())
			po.setSBOTerm(s.getSBOTerm());
		for (CVTerm cvt : s.getCVTerms()) {
			long contains = -1;
			for (int i = 0; i < po.getNumCVTerms() && contains < 0; i++) {
				CVTerm cv = reader.readCVTerm(po.getCVTerm(i));
				if (cvt.equals(cv))
					contains = i;
			}
			if (contains < 0)
				po.addCVTerm(writeCVTerm(cvt));
			else
				saveCVTermProperties(cvt, po.getCVTerm(contains));
		}
		// remove CVTerms that are not needed anymore.
		for (long i = po.getNumCVTerms() - 1; i >= 0; i--) {
			if (!s.getCVTerms().contains(reader.readCVTerm(po.getCVTerm(i))))
				po.getCVTerms().remove(i);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.sbml.jlibsbml.SBMLWriter#saveSpeciesProperties(org.sbml.jlibsbml.
	 * Species, java.lang.Object)
	 */
	public void saveSpeciesProperties(Species s, Object species) {
		if (!(species instanceof org.sbml.libsbml.Species))
			throw new IllegalArgumentException(
					"species must be an instance of org.sbml.libsbml.Species.");
		org.sbml.libsbml.Species spec = (org.sbml.libsbml.Species) species;
		saveNamedSBaseProperties(s, spec);
		if (!s.getSpeciesType().equals(spec.getSpeciesType()))
			spec.setSpeciesType(s.getSpeciesType());
		if (!s.getCompartment().equals(spec.getCompartment()))
			spec.setCompartment(s.getCompartment());
		if (s.isSetInitialAmount() && !spec.isSetInitialAmount()
				|| s.getInitialAmount() != spec.getInitialAmount())
			spec.setInitialAmount(s.getInitialAmount());
		else if (s.isSetInitialConcentration()
				&& !spec.isSetInitialConcentration()
				|| s.getInitialConcentration() != spec
						.getInitialConcentration())
			spec.setInitialConcentration(s.getInitialConcentration());
		if (!s.getSubstanceUnits().equals(spec.getSubstanceUnits()))
			spec.setSubstanceUnits(s.getSubstanceUnits());
		if (s.getHasOnlySubstanceUnits() != spec.getHasOnlySubstanceUnits())
			spec.setHasOnlySubstanceUnits(s.getHasOnlySubstanceUnits());
		if (s.getBoundaryCondition() != spec.getBoundaryCondition())
			spec.setBoundaryCondition(spec.getBoundaryCondition());
		if (s.getCharge() != spec.getCharge())
			spec.setCharge(s.getCharge());
		if (s.getConstant() != spec.getConstant())
			spec.setConstant(s.getConstant());
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
			Object specRef) {
		if (!(specRef instanceof org.sbml.libsbml.SpeciesReference))
			throw new IllegalArgumentException(
					"specRef must be an instance of org.sbml.libsbml.SpeciesReference.");
		org.sbml.libsbml.SpeciesReference sp = (org.sbml.libsbml.SpeciesReference) specRef;
		saveNamedSBaseProperties(sr, sp);
		if (!sr.getSpecies().equals(sp.getSpecies()))
			sp.setSpecies(sr.getSpecies());
		if (sr.isSetStoichiometryMath())
			sp.setStoichiometryMath(writeStoichoimetryMath(sr
					.getStoichiometryMath()));
		else
			sp.setStoichiometry(sr.getStoichiometry());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.SBMLWriter#writeCompartment(org.sbml.Compartment)
	 */
	// @Override
	public org.sbml.libsbml.Compartment writeCompartment(Compartment compartment) {
		org.sbml.libsbml.Compartment c = new org.sbml.libsbml.Compartment(
				compartment.getLevel(), compartment.getVersion());
		saveNamedSBaseProperties(compartment, c);
		if (compartment.isSetCompartmentType())
			c.setCompartmentType(compartment.getCompartmentType());
		if (compartment.isSetOutside())
			c.setOutside(compartment.getOutside());
		if (compartment.isSetSize())
			c.setSize(compartment.getSize());
		if (compartment.isSetUnits())
			c.setUnits(compartment.getUnits());
		c.setConstant(compartment.getConstant());
		c.setSpatialDimensions(compartment.getSpatialDimensions());
		return c;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.SBMLWriter#writeCompartmentType(org.sbml.CompartmentType)
	 */
	// @Override
	public org.sbml.libsbml.CompartmentType writeCompartmentType(
			CompartmentType compartmentType) {
		org.sbml.libsbml.CompartmentType ct = new org.sbml.libsbml.CompartmentType(
				compartmentType.getLevel(), compartmentType.getVersion());
		saveNamedSBaseProperties(compartmentType, ct);
		return ct;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.SBMLWriter#writeConstraint(org.sbml.Constraint)
	 */
	// @Override
	public org.sbml.libsbml.Constraint writeConstraint(Constraint constraint) {
		org.sbml.libsbml.Constraint c = new org.sbml.libsbml.Constraint(
				constraint.getLevel(), constraint.getVersion());
		saveSBaseProperties(constraint, c);
		if (constraint.isSetMath())
			c.setMath(convert(constraint.getMath()));
		if (constraint.isSetMessage())
			c.setMessage(new org.sbml.libsbml.XMLNode(constraint.getMessage()));
		return c;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.jlibsbml.SBMLWriter#writeCVTerm(org.sbml.jlibsbml.CVTerm)
	 */
	public org.sbml.libsbml.CVTerm writeCVTerm(CVTerm t) {
		org.sbml.libsbml.CVTerm libCVt = new org.sbml.libsbml.CVTerm();
		switch (t.getQualifierType()) {
		case MODEL_QUALIFIER:
			libCVt.setQualifierType(libsbmlConstants.MODEL_QUALIFIER);
			switch (t.getModelQualifierType()) {
			case BQM_IS:
				libCVt.setModelQualifierType(libsbmlConstants.BQM_IS);
				break;
			case BQM_IS_DESCRIBED_BY:
				libCVt
						.setModelQualifierType(libsbmlConstants.BQM_IS_DESCRIBED_BY);
				break;
			case BQM_UNKNOWN:
				libCVt.setModelQualifierType(libsbmlConstants.BQM_UNKNOWN);
				break;
			default:
				break;
			}
			break;
		case BIOLOGICAL_QUALIFIER:
			libCVt.setQualifierType(libsbmlConstants.BIOLOGICAL_QUALIFIER);
			switch (t.getBiologicalQualifierType()) {
			case BQB_ENCODES:
				libCVt.setBiologicalQualifierType(libsbmlConstants.BQB_ENCODES);
				break;
			case BQB_HAS_PART:
				libCVt
						.setBiologicalQualifierType(libsbmlConstants.BQB_HAS_PART);
				break;
			case BQB_HAS_VERSION:
				libCVt
						.setBiologicalQualifierType(libsbmlConstants.BQB_HAS_VERSION);
				break;
			case BQB_IS:
				libCVt.setBiologicalQualifierType(libsbmlConstants.BQB_IS);
				break;
			case BQB_IS_DESCRIBED_BY:
				libCVt
						.setBiologicalQualifierType(libsbmlConstants.BQB_IS_DESCRIBED_BY);
				break;
			case BQB_IS_ENCODED_BY:
				libCVt
						.setBiologicalQualifierType(libsbmlConstants.BQB_IS_ENCODED_BY);
				break;
			case BQB_IS_HOMOLOG_TO:
				libCVt
						.setBiologicalQualifierType(libsbmlConstants.BQB_IS_HOMOLOG_TO);
				break;
			case BQB_IS_PART_OF:
				libCVt
						.setBiologicalQualifierType(libsbmlConstants.BQB_IS_PART_OF);
				break;
			case BQB_IS_VERSION_OF:
				libCVt
						.setBiologicalQualifierType(libsbmlConstants.BQB_IS_VERSION_OF);
				break;
			case BQB_OCCURS_IN:
				libCVt
						.setBiologicalQualifierType(libsbmlConstants.BQB_OCCURS_IN);
				break;
			case BQB_UNKNOWN:
				libCVt.setBiologicalQualifierType(libsbmlConstants.BQB_UNKNOWN);
				break;
			default:
				break;
			}
			break;
		default:
			break;
		}
		for (int j = 0; j < t.getNumResources(); j++) {
			libCVt.addResource(t.getResourceURI(j));
		}
		return libCVt;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.SBMLWriter#writeDelay(org.sbml.Delay)
	 */
	// @Override
	public org.sbml.libsbml.Delay writeDelay(Delay delay) {
		org.sbml.libsbml.Delay d = new org.sbml.libsbml.Delay(delay.getLevel(),
				delay.getVersion());
		saveSBaseProperties(delay, d);
		if (delay.isSetMath())
			d.setMath(convert(delay.getMath()));
		return d;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.SBMLWriter#writeEvent(org.sbml.Event)
	 */
	// @Override
	public org.sbml.libsbml.Event writeEvent(Event event) {
		org.sbml.libsbml.Event e = new org.sbml.libsbml.Event(event.getLevel(),
				event.getVersion());
		saveNamedSBaseProperties(event, e);
		if (event.isSetDelay())
			e.setDelay(writeDelay(event.getDelay()));
		for (EventAssignment ea : event.getListOfEventAssignments())
			e.addEventAssignment(writeEventAssignment(ea));
		if (event.isSetTimeUnits())
			e.setTimeUnits(event.getTimeUnits());
		if (e.isSetTrigger())
			e.setTrigger(writeTrigger(event.getTrigger()));
		e.setUseValuesFromTriggerTime(event.getUseValuesFromTriggerTime());
		return e;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.SBMLWriter#writeEventAssignment(org.sbml.EventAssignment)
	 */
	// @Override
	public org.sbml.libsbml.EventAssignment writeEventAssignment(
			EventAssignment eventAssignment) {
		org.sbml.libsbml.EventAssignment ea = new org.sbml.libsbml.EventAssignment(
				eventAssignment.getLevel(), eventAssignment.getVersion());
		saveSBaseProperties(eventAssignment, ea);
		if (eventAssignment.isSetMath())
			ea.setMath(convert(eventAssignment.getMath()));
		if (eventAssignment.isSetVariable())
			ea.setVariable(eventAssignment.getVariable());
		return ea;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.sbml.SBMLWriter#writeFunctionDefinition(org.sbml.FunctionDefinition)
	 */
	// @Override
	public org.sbml.libsbml.FunctionDefinition writeFunctionDefinition(
			FunctionDefinition functionDefinition) {
		org.sbml.libsbml.FunctionDefinition fd = new org.sbml.libsbml.FunctionDefinition(
				functionDefinition.getLevel(), functionDefinition.getVersion());
		saveNamedSBaseProperties(functionDefinition, fd);
		if (functionDefinition.isSetMath())
			fd.setMath(convert(functionDefinition.getMath()));
		return fd;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.sbml.SBMLWriter#writeInitialAssignment(org.sbml.InitialAssignment)
	 */
	// @Override
	public org.sbml.libsbml.InitialAssignment writeInitialAssignment(
			InitialAssignment initialAssignment) {
		org.sbml.libsbml.InitialAssignment ia = new org.sbml.libsbml.InitialAssignment(
				initialAssignment.getLevel(), initialAssignment.getVersion());
		saveSBaseProperties(initialAssignment, ia);
		if (initialAssignment.isSetMath())
			ia.setMath(convert(initialAssignment.getMath()));
		if (initialAssignment.isSetSymbol())
			ia.setSymbol(initialAssignment.getSymbol());
		return ia;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.sbml.jlibsbml.SBMLWriter#writeKineticLaw(org.sbml.jlibsbml.KineticLaw
	 * )
	 */
	public org.sbml.libsbml.KineticLaw writeKineticLaw(KineticLaw kinteicLaw) {
		org.sbml.libsbml.KineticLaw k = new org.sbml.libsbml.KineticLaw(
				kinteicLaw.getLevel(), kinteicLaw.getVersion());
		saveSBaseProperties(kinteicLaw, k);
		if (kinteicLaw.isSetMath())
			k.setMath(convert(kinteicLaw.getMath()));
		for (Parameter p : kinteicLaw.getListOfParameters())
			k.addParameter(writeParameter(p));
		return k;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.SBMLWriter#writeModel(org.sbml.Model)
	 */
	public Object writeModel(Model model) {
		org.sbml.libsbml.Model m = new org.sbml.libsbml.Model(model.getLevel(),
				model.getVersion());
		saveNamedSBaseProperties(model, m);
		for (UnitDefinition ud : model.getListOfUnitDefinitions())
			m.addUnitDefinition(writeUnitDefinition(ud));
		for (FunctionDefinition fd : model.getListOfFunctionDefinitions())
			m.addFunctionDefinition(writeFunctionDefinition(fd));
		for (CompartmentType ct : model.getListOfCompartmentTypes())
			m.addCompartmentType(writeCompartmentType(ct));
		for (SpeciesType st : model.getListOfSpeciesTypes())
			m.addSpeciesType(writeSpeciesType(st));
		for (Compartment c : model.getListOfCompartments())
			m.addCompartment(writeCompartment(c));
		for (Species s : model.getListOfSpecies())
			m.addSpecies(writeSpecies(s));
		for (Parameter p : model.getListOfParameters())
			m.addParameter(writeParameter(p));
		for (Constraint c : model.getListOfConstraints())
			m.addConstraint(writeConstraint(c));
		for (InitialAssignment ia : model.getListOfInitialAssignments())
			m.addInitialAssignment(writeInitialAssignment(ia));
		for (Rule r : model.getListOfRules())
			m.addRule(writeRule(r));
		for (Reaction r : model.getListOfReactions())
			m.addReaction(writeReaction(r));
		for (Event e : model.getListOfEvents())
			m.addEvent(writeEvent(e));
		return m;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seeorg.sbml.SBMLWriter#writeModifierSpeciesReference(org.sbml.
	 * ModifierSpeciesReference)
	 */
	// @Override
	public org.sbml.libsbml.ModifierSpeciesReference writeModifierSpeciesReference(
			ModifierSpeciesReference modifierSpeciesReference) {
		org.sbml.libsbml.ModifierSpeciesReference m = new org.sbml.libsbml.ModifierSpeciesReference(
				modifierSpeciesReference.getLevel(), modifierSpeciesReference
						.getVersion());
		saveNamedSBaseProperties(modifierSpeciesReference, m);
		if (modifierSpeciesReference.isSetSpecies())
			m.setSpecies(modifierSpeciesReference.getSpecies());
		return m;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.SBMLWriter#writeParameter(org.sbml.Parameter)
	 */
	// @Override
	public org.sbml.libsbml.Parameter writeParameter(Parameter parameter) {
		org.sbml.libsbml.Parameter p = new org.sbml.libsbml.Parameter(parameter
				.getLevel(), parameter.getVersion());
		saveNamedSBaseProperties(parameter, p);
		p.setConstant(parameter.getConstant());
		p.setUnits(parameter.getUnits());
		p.setValue(parameter.getValue());
		return p;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.SBMLWriter#writeReaction(org.sbml.Reaction)
	 */
	public org.sbml.libsbml.Reaction writeReaction(Reaction reaction) {
		org.sbml.libsbml.Reaction r = new org.sbml.libsbml.Reaction(reaction
				.getLevel(), reaction.getVersion());
		saveNamedSBaseProperties(reaction, r);
		r.setFast(reaction.getFast());
		r.setReversible(reaction.getReversible());
		r.setKineticLaw(writeKineticLaw(reaction.getKineticLaw()));
		for (SpeciesReference sr : reaction.getListOfReactants())
			r.addReactant(writeSpeciesReference(sr));
		for (SpeciesReference sr : reaction.getListOfProducts())
			r.addProduct(writeSpeciesReference(sr));
		for (ModifierSpeciesReference mr : reaction.getListOfModifiers())
			r.addModifier(writeModifierSpeciesReference(mr));
		return r;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.SBMLWriter#writeRule(org.sbml.Rule)
	 */
	// @Override
	public org.sbml.libsbml.Rule writeRule(Rule rule) {
		org.sbml.libsbml.Rule r;
		if (rule.isAlgebraic())
			r = new org.sbml.libsbml.AlgebraicRule(rule.getLevel(), rule
					.getVersion());
		else {
			if (rule.isAssignment()) {
				r = new org.sbml.libsbml.AssignmentRule(rule.getLevel(), rule
						.getVersion());
				if (((AssignmentRule) rule).isSetVariable())
					r.setVariable(((AssignmentRule) rule).getVariable());
			} else {
				r = new org.sbml.libsbml.RateRule(rule.getLevel(), rule
						.getVersion());
				if (((RateRule) rule).isSetVariable())
					r.setVariable(((RateRule) rule).getVariable());
			}
		}
		if (rule.isSetMath())
			r.setMath(convert(rule.getMath()));
		saveSBaseProperties(rule, r);
		return r;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.SBMLWriter#writeSBML(java.lang.Object, java.lang.String)
	 */
	// @Override
	public boolean writeSBML(Object sbmlDocument, String filename) {
		if (!(sbmlDocument instanceof org.sbml.libsbml.SBMLDocument)
				&& !(sbmlDocument instanceof org.sbml.libsbml.Model))
			throw new IllegalArgumentException("");
		org.sbml.libsbml.SBMLDocument d;
		if (sbmlDocument instanceof org.sbml.libsbml.SBMLDocument)
			d = (org.sbml.libsbml.SBMLDocument) sbmlDocument;
		else
			d = ((org.sbml.libsbml.Model) sbmlDocument).getSBMLDocument();
		return (new SBMLWriter()).writeSBML(d, filename);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.SBMLWriter#writeSpecies(org.sbml.Species)
	 */
	// @Override
	public org.sbml.libsbml.Species writeSpecies(Species species) {
		org.sbml.libsbml.Species s = new org.sbml.libsbml.Species(species
				.getLevel(), species.getVersion());
		saveNamedSBaseProperties(species, s);
		s.setBoundaryCondition(species.getBoundaryCondition());
		s.setCharge(species.getCharge());
		s.setCompartment(species.getCompartment());
		s.setConstant(species.getConstant());
		s.setHasOnlySubstanceUnits(species.getHasOnlySubstanceUnits());
		if (species.isSetInitialAmount())
			s.setInitialAmount(species.getInitialAmount());
		else if (species.isSetInitialConcentration())
			s.setInitialConcentration(species.getInitialConcentration());
		if (species.isSetSpeciesType())
			s.setSpeciesType(species.getSpeciesType());
		if (species.isSetSubstanceUnits())
			species.getSubstanceUnits();
		return s;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.SBMLWriter#writeSpeciesReference(org.sbml.SpeciesReference)
	 */
	// @Override
	public org.sbml.libsbml.SpeciesReference writeSpeciesReference(
			SpeciesReference speciesReference) {
		org.sbml.libsbml.SpeciesReference sr = new org.sbml.libsbml.SpeciesReference(
				speciesReference.getLevel(), speciesReference.getVersion());
		saveNamedSBaseProperties(speciesReference, sr);
		if (speciesReference.isSetSpecies())
			sr.setSpecies(speciesReference.getSpecies());
		if (speciesReference.isSetStoichiometryMath())
			sr.setStoichiometryMath(writeStoichoimetryMath(speciesReference
					.getStoichiometryMath()));
		else
			sr.setStoichiometry(speciesReference.getStoichiometry());
		return sr;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.SBMLWriter#writeSpeciesType(org.sbml.SpeciesType)
	 */
	// @Override
	public org.sbml.libsbml.SpeciesType writeSpeciesType(SpeciesType speciesType) {
		org.sbml.libsbml.SpeciesType st = new org.sbml.libsbml.SpeciesType(
				speciesType.getLevel(), speciesType.getVersion());
		saveNamedSBaseProperties(speciesType, st);
		return st;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.sbml.SBMLWriter#writeStoichoimetryMath(org.sbml.StoichiometryMath)
	 */
	// @Override
	public org.sbml.libsbml.StoichiometryMath writeStoichoimetryMath(
			StoichiometryMath stoichiometryMath) {
		org.sbml.libsbml.StoichiometryMath sm = new org.sbml.libsbml.StoichiometryMath(
				stoichiometryMath.getLevel(), stoichiometryMath.getVersion());
		saveSBaseProperties(stoichiometryMath, sm);
		if (stoichiometryMath.isSetMath())
			sm.setMath(convert(stoichiometryMath.getMath()));
		return sm;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.SBMLWriter#writeTrigger(org.sbml.Trigger)
	 */
	// @Override
	public org.sbml.libsbml.Trigger writeTrigger(Trigger trigger) {
		org.sbml.libsbml.Trigger t = new org.sbml.libsbml.Trigger(trigger
				.getLevel(), trigger.getVersion());
		saveSBaseProperties(trigger, t);
		if (trigger.isSetMath())
			t.setMath(convert(trigger.getMath()));
		return t;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.SBMLWriter#writeUnit(org.sbml.Unit)
	 */
	// @Override
	public org.sbml.libsbml.Unit writeUnit(Unit unit) {
		org.sbml.libsbml.Unit u = new org.sbml.libsbml.Unit(unit.getLevel(),
				unit.getVersion());
		saveSBaseProperties(unit, u);
		switch (unit.getKind()) {
		case UNIT_KIND_AMPERE:
			u.setKind(libsbmlConstants.UNIT_KIND_AMPERE);
			break;
		case UNIT_KIND_BECQUEREL:
			u.setKind(libsbmlConstants.UNIT_KIND_BECQUEREL);
			break;
		case UNIT_KIND_CANDELA:
			u.setKind(libsbmlConstants.UNIT_KIND_CANDELA);
			break;
		case UNIT_KIND_CELSIUS:
			u.setKind(libsbmlConstants.UNIT_KIND_CELSIUS);
			break;
		case UNIT_KIND_COULOMB:
			u.setKind(libsbmlConstants.UNIT_KIND_COULOMB);
			break;
		case UNIT_KIND_DIMENSIONLESS:
			u.setKind(libsbmlConstants.UNIT_KIND_DIMENSIONLESS);
			break;
		case UNIT_KIND_FARAD:
			u.setKind(libsbmlConstants.UNIT_KIND_FARAD);
			break;
		case UNIT_KIND_GRAM:
			u.setKind(libsbmlConstants.UNIT_KIND_GRAM);
			break;
		case UNIT_KIND_GRAY:
			u.setKind(libsbmlConstants.UNIT_KIND_GRAY);
			break;
		case UNIT_KIND_HENRY:
			u.setKind(libsbmlConstants.UNIT_KIND_HENRY);
			break;
		case UNIT_KIND_HERTZ:
			u.setKind(libsbmlConstants.UNIT_KIND_HERTZ);
			break;
		case UNIT_KIND_INVALID:
			u.setKind(libsbmlConstants.UNIT_KIND_INVALID);
			break;
		case UNIT_KIND_ITEM:
			u.setKind(libsbmlConstants.UNIT_KIND_ITEM);
			break;
		case UNIT_KIND_JOULE:
			u.setKind(libsbmlConstants.UNIT_KIND_JOULE);
			break;
		case UNIT_KIND_KATAL:
			u.setKind(libsbmlConstants.UNIT_KIND_KATAL);
			break;
		case UNIT_KIND_KELVIN:
			u.setKind(libsbmlConstants.UNIT_KIND_KELVIN);
			break;
		case UNIT_KIND_KILOGRAM:
			u.setKind(libsbmlConstants.UNIT_KIND_KILOGRAM);
			break;
		case UNIT_KIND_LITER:
			u.setKind(libsbmlConstants.UNIT_KIND_LITER);
			break;
		case UNIT_KIND_LITRE:
			u.setKind(libsbmlConstants.UNIT_KIND_LITRE);
			break;
		case UNIT_KIND_LUMEN:
			u.setKind(libsbmlConstants.UNIT_KIND_LUMEN);
			break;
		case UNIT_KIND_LUX:
			u.setKind(libsbmlConstants.UNIT_KIND_LUX);
			break;
		case UNIT_KIND_METER:
			u.setKind(libsbmlConstants.UNIT_KIND_METER);
			break;
		case UNIT_KIND_METRE:
			u.setKind(libsbmlConstants.UNIT_KIND_METRE);
			break;
		case UNIT_KIND_MOLE:
			u.setKind(libsbmlConstants.UNIT_KIND_MOLE);
			break;
		case UNIT_KIND_NEWTON:
			u.setKind(libsbmlConstants.UNIT_KIND_NEWTON);
			break;
		case UNIT_KIND_OHM:
			u.setKind(libsbmlConstants.UNIT_KIND_OHM);
			break;
		case UNIT_KIND_PASCAL:
			u.setKind(libsbmlConstants.UNIT_KIND_PASCAL);
			break;
		case UNIT_KIND_RADIAN:
			u.setKind(libsbmlConstants.UNIT_KIND_RADIAN);
			break;
		case UNIT_KIND_SECOND:
			u.setKind(libsbmlConstants.UNIT_KIND_SECOND);
			break;
		case UNIT_KIND_SIEMENS:
			u.setKind(libsbmlConstants.UNIT_KIND_SIEMENS);
			break;
		case UNIT_KIND_SIEVERT:
			u.setKind(libsbmlConstants.UNIT_KIND_SIEVERT);
			break;
		case UNIT_KIND_STERADIAN:
			u.setKind(libsbmlConstants.UNIT_KIND_STERADIAN);
			break;
		case UNIT_KIND_TESLA:
			u.setKind(libsbmlConstants.UNIT_KIND_TESLA);
			break;
		case UNIT_KIND_VOLT:
			u.setKind(libsbmlConstants.UNIT_KIND_VOLT);
			break;
		case UNIT_KIND_WATT:
			u.setKind(libsbmlConstants.UNIT_KIND_WATT);
			break;
		case UNIT_KIND_WEBER:
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
	 * @see org.sbml.SBMLWriter#writeUnitDefinition(org.sbml.UnitDefinition)
	 */
	// @Override
	public org.sbml.libsbml.UnitDefinition writeUnitDefinition(
			UnitDefinition unitDefinition) {
		org.sbml.libsbml.UnitDefinition ud = new org.sbml.libsbml.UnitDefinition(
				unitDefinition.getLevel(), unitDefinition.getVersion());
		saveNamedSBaseProperties(unitDefinition, ud);
		for (Unit u : unitDefinition.getListOfUnits())
			ud.addUnit(writeUnit(u));
		return ud;
	}

}
