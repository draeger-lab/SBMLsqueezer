/*
 * $Id$
 * $URL$
 * ---------------------------------------------------------------------
 * This file is part of SBMLsqueezer, a Java program that creates rate 
 * equations for reactions in SBML files (http://sbml.org).
 *
 * Copyright (C) 2006-2012 by the University of Tuebingen, Germany.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 * ---------------------------------------------------------------------
 */
package org.sbml.squeezer;

import java.text.MessageFormat;

import org.sbml.jsbml.Compartment;
import org.sbml.jsbml.KineticLaw;
import org.sbml.jsbml.ListOf;
import org.sbml.jsbml.LocalParameter;
import org.sbml.jsbml.Model;
import org.sbml.jsbml.Parameter;
import org.sbml.jsbml.Reaction;
import org.sbml.jsbml.SBO;
import org.sbml.jsbml.SimpleSpeciesReference;
import org.sbml.jsbml.Species;
import org.sbml.jsbml.SpeciesReference;
import org.sbml.jsbml.Unit;
import org.sbml.jsbml.UnitDefinition;
import org.sbml.jsbml.util.StringTools;
import org.sbml.jsbml.util.filters.SBOFilter;
import org.sbml.squeezer.util.Bundles;
import org.sbml.squeezer.util.SBMLtools;

/**
 * 
 * @author Andreas Dr&auml;ger
 * @author Sarah R. M&uuml;ller vom Hagen
 * @date 2010-10-22
 * @version $Rev$
 */
public class ParameterFactory {

	/**
	 * 
	 */
	private double defaultParamValue;
	/**
	 * 
	 */
	private KineticLaw kineticLaw;
	/**
	 * 
	 */
	private Model model;
	/**
	 * 
	 */
	private double orderProducts;
	/**
	 * 
	 */
	private double orderReactants;
	/**
	 * 
	 */
	private UnitFactory unitFactory;

	/**
	 * 
	 * @param kl
	 * @param defaultParamValue
	 * @param orderReactants
	 * @param orderProducts
	 * @param bringToConcentration
	 */
	public ParameterFactory(KineticLaw kl, double defaultParamValue,
			double orderReactants, double orderProducts,
			boolean bringToConcentration) {
		this.kineticLaw = kl;
		this.model = kl.getModel();
		this.defaultParamValue = defaultParamValue;
		this.orderReactants = orderReactants;
		this.orderProducts = orderProducts;
		this.unitFactory = new UnitFactory(model, bringToConcentration);
	}

	/**
	 * Concatenates the given name parts of the identifier and returns a global
	 * parameter with this id.
	 * 
	 * @param idParts
	 * @return
	 */
	public Parameter createOrGetGlobalParameter(Object... idParts) {
		return createOrGetGlobalParameter(StringTools.concat(idParts)
				.toString());
	}

	/**
	 * If the parent model does not yet contain a parameter with the given id, a
	 * new parameter is created, its value is set to 1 and it is added to the
	 * list of global parameters in the model. If there is already a parameter
	 * with this id, a reference to it will be returned.
	 * 
	 * @param id
	 *            the identifier of the global parameter.
	 */
	public Parameter createOrGetGlobalParameter(String id) {
		Parameter p = model.getParameter(id);
		if (p == null) {
			p = new Parameter(id, model.getLevel(), model.getVersion());
			p.setValue(defaultParamValue);
			if (1 < model.getLevel()) {
				p.setConstant(true);
			}
			model.addParameter(p);
		}
		return p;
	}

	/**
	 * Equvivalent to {@see createOrGetParameter} but the parts of the id can be
	 * given separately to this method and are concatenated.
	 * 
	 * @param idParts
	 * @return
	 */
	public LocalParameter createOrGetParameter(Object... idParts) {
		return createOrGetParameter(StringTools.concat(idParts).toString());
	}

	/**
	 * If a parameter with the given identifier has already been created and is
	 * contained in the list of local parameters for this kinetic law, a pointer
	 * to it will be returned. If no such parameter exists, a new parameter with
	 * a value of 1 will be created and a pointer to it will be returned.
	 * 
	 * @param id
	 *            the identifier of the local parameter.
	 * @return
	 */
	public LocalParameter createOrGetParameter(String id) {
		LocalParameter p = kineticLaw.getLocalParameter(id);
		if (p == null) {
			p = new LocalParameter(id, model.getLevel(), model.getVersion());
			p.setValue(defaultParamValue);
			kineticLaw.addLocalParameter(p);
		}
		return p;
	}

	/**
	 * 
	 * @return
	 */
	public UnitFactory getUnitFactory() {
		return unitFactory;
	}

	/**
	 * For the additive Model: slope of the curve activation function
	 * 
	 * @return Parameter
	 */
	public LocalParameter parameterAlpha(String rId) {
		LocalParameter p = createOrGetParameter("alpha_", rId);
		if (!p.isSetSBOTerm()) {
			SBMLtools.setSBOTerm(p,2);
		}
		if (!p.isSetUnits()) {
			p.setUnits(Unit.Kind.DIMENSIONLESS);
		}
		if (!p.isSetName()) {
			p.setName(Bundles.MESSAGES.getString("FOR_ADDITIVE_MODEL") 
					+ ": " + Bundles.MESSAGES.getString("WEIGHT_ACTIVATION_FUNCTION_PARAMETERS"));
		}
		return p;
	}

	/**
	 * Association constant from mass action kinetics.
	 * 
	 * 
	 * @param catalyst
	 *            Identifier of the catalyst. Can be null.
	 * @return
	 */
	public LocalParameter parameterAssociationConst(String catalyst) {
		boolean zerothOrder = orderReactants == 0d;
		Reaction r = kineticLaw.getParentSBMLObject();
		StringBuffer kass = StringTools.concat("kass_", r.getId());
		if (zerothOrder) {
			kass.insert(0, 'z');
		}
		if (catalyst != null) {
			StringTools.append(kass, StringTools.underscore, catalyst);
		}
		LocalParameter p_kass = createOrGetParameter(kass.toString());
		if (!p_kass.isSetName()) {
			p_kass.setName((zerothOrder ? Bundles.MESSAGES.getString("ZEROTH_ORDER") + " " : "") +
					MessageFormat.format(Bundles.MESSAGES.getString("ASSOCIATION_CONSTANT_OF_REACTION"), r.getId()));
		}
		if (!p_kass.isSetSBOTerm()) {
			SBMLtools.setSBOTerm(p_kass,zerothOrder ? 48 : 153);
		}
		if (!p_kass.isSetUnits()) {
			p_kass.setUnits(unitFactory
					.unitPerTimeAndConcentrationOrSubstance(r
							.getListOfReactants(), r.getListOfModifiers(),
							zerothOrder));
		}
		return p_kass;
	}

	/**
	 * For the additive Model: weight parameter
	 * 
	 * @return weight for the weight matrix
	 */
	public LocalParameter parameterB(String rId) {
		LocalParameter p = createOrGetParameter("b_", rId);
		if (!p.isSetSBOTerm()) {
			SBMLtools.setSBOTerm(p,2);
		}
		if (!p.isSetUnits()) {
			p.setUnits(unitFactory.unitPerTime());
		}
		if (!p.isSetName()) {
			p.setName(Bundles.MESSAGES.getString("FOR_ADDITIVE_MODEL") 
					+ ": " + Bundles.MESSAGES.getString("BASIS_EXPRESSION_LEVEL"));
		}
		return p;
	}

	/**
	 * For the additive Model: activation curve's y-intercept
	 * 
	 * @return Parameter
	 */
	public LocalParameter parameterBeta(String rId) {
		LocalParameter p = createOrGetParameter("beta_", rId);
		if (!p.isSetSBOTerm()) {
			SBMLtools.setSBOTerm(p,2);
		}
		if (!p.isSetUnits()) {
			p.setUnits(Unit.Kind.DIMENSIONLESS);
		}
		if (!p.isSetName()) {
			p.setName(Bundles.MESSAGES.getString("WEIGHT_ACTIVATION_FUNCTION_PARAMETERS"));
		}
		return p;
	}

	/**
	 * biochemical cooperative inhibitor substrate coefficient.
	 * 
	 * @param aORb
	 *            a character to specify this parameter. Allowed values are 'a'
	 *            or 'b'.
	 * @param inhibitor
	 *            Identifier of the inhibitory species. Can be null.
	 * @param enzyme
	 *            Identifier of the catralyzing enzyme. Can be null.
	 * @return
	 */
	public LocalParameter parameterCooperativeInhibitorSubstrateCoefficient(
			char aORb, String inhibitor, String enzyme) {
		StringBuffer id = new StringBuffer();
		id.append(aORb);
		if (inhibitor != null) {
			StringTools.append(id, StringTools.underscore, inhibitor);
		}
		if (enzyme != null) {
			StringTools.append(id, StringTools.underscore, enzyme);
		}
		LocalParameter coeff = createOrGetParameter(id);
		if (!coeff.isSetSBOTerm()) {
			SBMLtools.setSBOTerm(coeff,385);
		}
		if (!coeff.isSetUnits()) {
			coeff.setUnits(Unit.Kind.DIMENSIONLESS);
		}
		return coeff;
	}

	/**
	 * Dissociation constant in mass action kinetics
	 * 
	 * @param catalyst
	 *            Identifier of the catalyzing species. Can be null.
	 * @return
	 */
	public LocalParameter parameterDissociationConst(String catalyst) {
		boolean zerothOrder = orderProducts == 0;
		Reaction r = kineticLaw.getParentSBMLObject();
		StringBuffer kdiss = StringTools.concat("kdiss_", r.getId());
		if (zerothOrder) {
			kdiss.insert(0, 'z');
		}
		if (catalyst != null) {
			StringTools.append(kdiss, StringTools.underscore, catalyst);
		}
		LocalParameter p_kdiss = createOrGetParameter(kdiss.toString());
		if (!p_kdiss.isSetName()) {
			p_kdiss.setName((zerothOrder ? Bundles.MESSAGES.getString("ZEROTH_ORDER") + " " : "") +
					MessageFormat.format(Bundles.MESSAGES.getString("DISASSOCIATION_CONSTANT_OF_REACTION"), r.getId()));
		}
		if (!p_kdiss.isSetSBOTerm()) {
			SBMLtools.setSBOTerm(p_kdiss,156);
		}
		if (!p_kdiss.isSetUnits()) {
			p_kdiss.setUnits(unitFactory
					.unitPerTimeAndConcentrationOrSubstance(r
							.getListOfReactants(), r.getListOfModifiers(),
							zerothOrder));
		}
		return p_kdiss;
	}

	/**
	 * Equilibrium constant of the parent reaction.
	 * 
	 * @return A new or an existing equilibrium constant for the reaction.
	 */
	public LocalParameter parameterEquilibriumConstant() {
		String reactionID = kineticLaw.getParentSBMLObject().getId();
		LocalParameter keq = createOrGetParameter("keq_", reactionID);
		if (!keq.isSetSBOTerm()) {
			SBMLtools.setSBOTerm(keq,281);
		}
		if (!keq.isSetName()) {
			keq.setName(MessageFormat.format(Bundles.MESSAGES.getString("EQUILIBRIUM_CONSTANT_OF_REACTION"),reactionID));
		}
		if (!keq.isSetUnits()) {
			int x = 0;
			Reaction r = kineticLaw.getParentSBMLObject();
			for (SpeciesReference specRef : r.getListOfReactants()) {
				x += specRef.getStoichiometry();
			}
			if (r.getReversible()) {
				for (SpeciesReference specRef : r.getListOfProducts()) {
					x -= specRef.getStoichiometry();
				}
			}
			if (x == 0) {
				keq.setUnits(Unit.Kind.DIMENSIONLESS);
			} else {
				keq.setUnits(unitFactory.unitSubstancePerSize(model
						.getSubstanceUnitsInstance(), model
						.getVolumeUnitsInstance(), x));
			}
		}
		return keq;
	}

	/**
	 * Bolzman's gas constant.
	 * 
	 * @return A new or the existing gas constant parameter from the model
	 *         (global parameter).
	 */
	public Parameter parameterGasConstant() {
		Parameter R = createOrGetGlobalParameter("R");
		R.setValue(8.31447215);
		if (!R.isSetName()) {
			R.setName(Bundles.MESSAGES.getString("IDEAL_GAS_CONSTANT"));
		}
		if (!R.isSetUnits()) {
			R.setUnits(unitFactory.unitJperKandM());
		}
		return R;
	}

	/**
	 * Hill coefficient.
	 * 
	 * @param enzyme
	 *            Identifier of the catalyzing enzyme. Can be null.
	 * @return
	 */
	public LocalParameter parameterHillCoefficient(String enzyme) {
		String reactionID = kineticLaw.getParentSBMLObject().getId();
		StringBuffer id = StringTools.concat("hic_", reactionID);
		if (enzyme != null) {
			StringTools.append(id, StringTools.underscore, enzyme);
		}
		LocalParameter hr = createOrGetParameter(id.toString());
		if (!hr.isSetSBOTerm()) {
			SBMLtools.setSBOTerm(hr,190);
		}
		if (!hr.isSetName()) {
			hr.setName(MessageFormat.format(Bundles.MESSAGES.getString("HILL_COEFFICIENT_IN_REACTION"),
					(enzyme != null) ? MessageFormat.format(Bundles.MESSAGES.getString("FOR_ENZYME"),
							enzyme) : ""));
		}
		if (!hr.isSetUnits()) {
			hr.setUnits(Unit.Kind.DIMENSIONLESS);
		}
		return hr;
	}

	/**
	 * Activation constant.
	 * 
	 * @param activatorSpecies
	 *            Identifier of the activatory species. Must not be null.
	 * @return
	 */
	public LocalParameter parameterKa(String activatorSpecies) {
		return parameterKa(activatorSpecies, null);
	}

	/**
	 * Activation constant.
	 * 
	 * @param activatorSpecies
	 *            Identifier of the activatory species. Must not be null.
	 * @param enzyme
	 * @return
	 */
	public LocalParameter parameterKa(String activatorSpecies, String enzyme) {
		String reactionID = kineticLaw.getParentSBMLObject().getId();
		StringBuffer name = StringTools.concat("kac_", reactionID,
				StringTools.underscore, activatorSpecies);
		if (enzyme != null) {
			StringTools.append(name, StringTools.underscore, enzyme);
		}
		LocalParameter kA = createOrGetParameter(name);
		if (!kA.isSetSBOTerm()) {
			SBMLtools.setSBOTerm(kA,363);
		}
		if (!kA.isSetName()) {
			kA.setName(MessageFormat.format(Bundles.MESSAGES.getString("ACTIVATION_CONSTANT_OF_REACTION"),reactionID));
		}
		if (!kA.isSetUnits()) {
			Species species = model.getSpecies(activatorSpecies);
			kA
					.setUnits(unitFactory.getBringToConcentration() ? unitFactory
							.unitSubstancePerSize(species
									.getSubstanceUnitsInstance(), species
									.getCompartmentInstance()
									.getUnitsInstance())
							: species.getSubstanceUnitsInstance());
		}
		return kA;
	}

	/**
	 * Turn over rate if enzyme is null and limiting rate otherwise.
	 * 
	 * @param enzyme
	 * @param forward
	 * @return The parameter vmax if and only if the number of enzymes is zero,
	 *         otherwise kcat.
	 */
	public LocalParameter parameterKcatOrVmax(String enzyme, boolean forward) {
		String reactionID = kineticLaw.getParentSBMLObject().getId();
		LocalParameter kr;
		if (enzyme != null) {
			/*
			 * Catalytic constant
			 */
			StringBuffer id = new StringBuffer();
			if (kineticLaw.getParentSBMLObject().getReversible()) {
				id.append("kcr");
				StringTools.append(id, forward ? 'f' : 'r',
						StringTools.underscore);
			} else {
				id.append("kcat_");
			}
			StringTools.append(id, reactionID, StringTools.underscore, enzyme);
			kr = createOrGetParameter(id);
			if (!kr.isSetSBOTerm()) {
				if (kineticLaw.getParentSBMLObject().getReversible()) {
					SBMLtools.setSBOTerm(kr,forward ? 320 : 321);
				} else {
					SBMLtools.setSBOTerm(kr,25);
				}
			}
			if (!kr.isSetName()) {
				kr.setName((enzyme != null) ? 
						MessageFormat.format(Bundles.MESSAGES.getString("CATALYTIC_RATE_CONSTANT_OF_ENZYME_IN_REACTION"),
							Bundles.MESSAGES.getString(forward ? "SUBSTRATE" : "PRODUCT"), enzyme,
							reactionID) :
						MessageFormat.format(Bundles.MESSAGES.getString("CATALYTIC_RATE_CONSTANT_OF_REACTION"),
							Bundles.MESSAGES.getString(forward ? "SUBSTRATE" : "PRODUCT"),
							reactionID)
						);
			}
			if (!kr.isSetUnits()) {
				kr.setUnits(unitFactory.unitPerTimeOrSizePerTime(model
						.getSpecies(enzyme).getCompartmentInstance()));
			}
		} else {
			/*
			 * Maximal velocity, i.e., limiting rate.
			 */
			StringBuffer id = StringTools.concat("vma");
			if (!kineticLaw.getParentSBMLObject().getReversible()) {
				id.append('x');
			} else {
				id.append(forward ? 'f' : 'r');
			}
			StringTools.append(id, StringTools.underscore, reactionID);
			kr = createOrGetParameter(id);
			if (!kr.isSetSBOTerm()) {
				if (kineticLaw.getParentSBMLObject().getReversible()) {
					SBMLtools.setSBOTerm(kr,forward ? 324 : 325);
				} else {
					SBMLtools.setSBOTerm(kr,186);
				}
			}
			if (!kr.isSetName()) {
				kr.setName((enzyme != null) ? 
						MessageFormat.format(Bundles.MESSAGES.getString("MAXIMAL_VELOCITY_OF_ENZYME_IN_REACTION"),
							Bundles.MESSAGES.getString(forward ? "FORWARD" : "REVERSE"),
							enzyme,
							reactionID) : 
						MessageFormat.format(Bundles.MESSAGES.getString("MAXIMAL_VELOCITY_OF_REACTION"),
							Bundles.MESSAGES.getString(forward ? "FORWARD" : "REVERSE"),
							reactionID));
			}
			if (!kr.isSetUnits()) {
				/*
				 * bringToConcentration ? unitmMperSecond() :
				 */
				kr.setUnits(unitFactory.unitSubstancePerTime(
						model.getUnitDefinition(UnitDefinition.SUBSTANCE), 
						model.getUnitDefinition(UnitDefinition.TIME)));
			}
		}
		return kr;
	}

	/**
	 * energy constant of given species
	 * 
	 * @param species
	 *            Identifier of the species for which this global parameter
	 *            should be created. Must not be null.
	 * @return A new or an existing global parameter representing the KG
	 *         parameter for the given species.
	 */
	public Parameter parameterKG(String species) {
		Parameter kG = createOrGetGlobalParameter("kG_", species);
		if (!kG.isSetUnits()) {
			kG.setUnits(Unit.Kind.DIMENSIONLESS);
		}
		if (!kG.isSetName()) {
			kG.setName(MessageFormat.format(Bundles.MESSAGES.getString("ENERGY_CONSTANT_OF_SPECIES"), species));
		}
		return kG;
	}

	/**
	 * Inhibitory constant
	 * 
	 * @param inhibitorSpecies
	 *            species that lowers this velocity. Must not be null.
	 * @return
	 */
	public LocalParameter parameterKi(String inhibitorSpecies) {
		return parameterKi(inhibitorSpecies, null);
	}

	/**
	 * Inhibitory constant in an enzyme catalyzed reaction.
	 * 
	 * @param inhibitorSpecies
	 *            Identifier of an inhibitory species in this reaction. Must not
	 *            be null.
	 * @param enzyme
	 *            Identifier of a catalyzing enzyme in this reaction. Can be
	 *            null.
	 * @return
	 */
	public LocalParameter parameterKi(String inhibitorSpecies, String enzyme) {
		return parameterKi(inhibitorSpecies, enzyme, 0);
	}

	/**
	 * Inhibitory constant.
	 * 
	 * @param inhibitorSpecies
	 *            Must not be null.
	 * @param enzymeID
	 *            can be null.
	 * @param bindingNum
	 *            if <= 0 not considered.
	 * @return
	 */
	public LocalParameter parameterKi(String inhibitorSpecies, String enzymeID,
			int bindingNum) {
		StringBuffer id = new StringBuffer();
		id.append("kic_");
		if (bindingNum > 0) {
			StringTools.append(id, bindingNum, StringTools.underscore);
		}
		String reactionID = kineticLaw.getParentSBMLObject().getId();
		id.append(reactionID);
		if (inhibitorSpecies != null) {
			StringTools.append(id, StringTools.underscore, inhibitorSpecies);
		}
		if (enzymeID != null) {
			StringTools.append(id, StringTools.underscore, enzymeID);
		}
		LocalParameter kI = createOrGetParameter(id.toString());
		if (!kI.isSetSBOTerm()) {
			SBMLtools.setSBOTerm(kI,261);
		}
		if (!kI.isSetName()) {
			String temp = "";
			if (inhibitorSpecies != null) {
				temp += MessageFormat.format(Bundles.MESSAGES.getString("FOR_SPECIES"), inhibitorSpecies);
			}
			if (enzymeID != null) {
				temp += MessageFormat.format(Bundles.MESSAGES.getString("FOR_ENZYME"), enzymeID);
			}
			if (bindingNum > 0) {
				temp += MessageFormat.format(Bundles.MESSAGES.getString("WITH_BINDING_POSITIONS"), Integer.toString(bindingNum));
			}
			kI.setName(MessageFormat.format(Bundles.MESSAGES.getString("INHIBITIORY_CONSTANT_OF_REACTION"), temp, reactionID));
		}
		if (!kI.isSetUnits()) {
			Species spec = model.getSpecies(inhibitorSpecies);
			if (unitFactory.getBringToConcentration()) {
				kI.setUnits(unitFactory.unitSubstancePerSize(spec
						.getSubstanceUnitsInstance(), spec
						.getCompartmentInstance().getUnitsInstance()));
			} else {
				kI.setUnits(spec.getSubstanceUnitsInstance());
			}
		}
		return kI;
	}

	/**
	 * @param prefix
	 * @return
	 */
	public LocalParameter parameterKineticOrder(String prefix) {
		String reactionID = kineticLaw.getParentSBMLObject().getId();
		StringBuffer id = StringTools.concat(prefix, reactionID);
		LocalParameter hr = createOrGetParameter(id.toString());
		if (!hr.isSetSBOTerm()) {
			// not yet available.
		}
		if (!hr.isSetName()) {
			hr.setName(MessageFormat.format(Bundles.MESSAGES.getString("KINETIC_ORDER_OF_REACTION"), reactionID));
		}
		if (!hr.isSetUnits()) {
			hr.setUnits(Unit.Kind.DIMENSIONLESS);
		}
		return hr;
	}

	/**
	 * Half saturation constant.
	 * 
	 * @param species
	 *            Must not be null.
	 * @param enzyme
	 *            Identifier of the catalyzing enzyme. Can be null.
	 * @return
	 */
	public LocalParameter parameterKS(Species species, String enzyme) {
		String rid = kineticLaw.getParentSBMLObject().getId();
		StringBuffer id = StringTools.concat("ksp_", rid);
		if (enzyme != null) {
			StringTools.append(id, StringTools.underscore, enzyme);
		}
		LocalParameter kS = createOrGetParameter(id.toString());
		if (!kS.isSetSBOTerm()) {
			SBMLtools.setSBOTerm(kS,194);
		}
		if (!kS.isSetName()) {
			kS.setName((enzyme != null) ?
					MessageFormat.format(Bundles.MESSAGES.getString("HALF_SATURATION_CONSTANT_OF_SPECIES_AND_ENZYME_IN_REACTION"),
							species, enzyme, rid) :
					MessageFormat.format(Bundles.MESSAGES.getString("HALF_SATURATION_CONSTANT_OF_SPECIES_IN_REACTION"),
							species, rid));
		}
		if (!kS.isSetUnits()) {
			if (unitFactory.getBringToConcentration()
					&& species.hasOnlySubstanceUnits()) {
				kS.setUnits(unitFactory.unitSubstancePerSize(species
						.getSubstanceUnitsInstance(), species
						.getCompartmentInstance().getUnitsInstance()));
			} else if (!unitFactory.getBringToConcentration()
					&& !species.hasOnlySubstanceUnits()) {
				UnitDefinition substance = species.getSubstanceUnitsInstance()
						.clone();
				UnitDefinition size = species.getCompartmentInstance()
						.getUnitsInstance();
				substance.multiplyWith(size);
				substance.setId(substance.getId() + "_times_" + size.getId());
				kS.setUnits(UnitFactory.checkUnitDefinitions(substance, model));
			} else {
				kS.setUnits(species.getUnitsInstance());
			}
		}
		return kS;
	}

	/**
	 * For the additive Model: weight parameter
	 * 
	 * @return weight for the weight matrix
	 */
	public LocalParameter parameterM(String rId) {
		LocalParameter p = createOrGetParameter("m_", rId);
		if (!p.isSetSBOTerm()) {
			SBMLtools.setSBOTerm(p,2);
		}
		if (!p.isSetUnits()) {
			p.setUnits(Unit.Kind.DIMENSIONLESS);
		}
		if (!p.isSetName()) {
			p.setName(Bundles.MESSAGES.getString("FOR_ADDITIVE_MODEL")
					+ ": " + Bundles.MESSAGES.getString("CONST_MAX_EXPRESSION"));
		}
		return p;
	}

	/**
	 * Michaelis constant.
	 * 
	 * @param species
	 *            Must not be null.
	 * @param enzyme
	 *            Identifier of the catalyzing enzyme. Can be null.
	 * @return
	 */
	public LocalParameter parameterMichaelis(String species, String enzyme) {
		String reactionID = kineticLaw.getParentSBMLObject().getId();
		StringBuffer id = StringTools.concat("kmc_", reactionID,
				StringTools.underscore, species);
		if (enzyme != null) {
			StringTools.append(id, StringTools.underscore, enzyme);
		}
		LocalParameter kM = createOrGetParameter(id.toString());
		if (kM.isSetSBOTerm()) {
			SBMLtools.setSBOTerm(kM,27);
		}
		if (!kM.isSetName()) {
			kM.setName((enzyme != null) ?
					MessageFormat.format(Bundles.MESSAGES.getString("MICHAELIS_CONSTANT_OF_SPECIES_AND_ENZYME_IN_REACTION"), species, enzyme, reactionID) :
					MessageFormat.format(Bundles.MESSAGES.getString("MICHAELIS_CONSTANT_OF_SPECIES_IN_REACTION"), species, reactionID));
		}
		if (!kM.isSetUnits()) {
			Species spec = model.getSpecies(species);
			if (unitFactory.getBringToConcentration()) {
			  Compartment compartment = spec.getCompartmentInstance();
			  if (compartment == null) {
          throw new NullPointerException(MessageFormat.format(
            Bundles.WARNINGS.getString("UNDEFINED_COMPARTMENT_OF_SPECIES"), spec.toString()));
			  }
        UnitDefinition specUnit = spec.getSubstanceUnitsInstance();
			  UnitDefinition compUnit = compartment.getUnitsInstance();
			  if ((specUnit == null) || (compUnit == null)) {
          throw new NullPointerException(MessageFormat.format(
                  Bundles.WARNINGS.getString("UNDEFINED_UNIT_OF_SPECIES"),
                  kM.toString(), spec.toString()));
			  }
        kM.setUnits(unitFactory.unitSubstancePerSize(specUnit, compUnit)); 
			} else {
			  kM.setUnits(spec.getSubstanceUnitsInstance()); 
			}
		}
		return kM;
	}

	/**
	 * Michaelis constant.
	 * 
	 * @param species
	 *            Must not be null.
	 * @param enzyme
	 *            identifier of the catalyzing enzyme. Can be null.
	 * @param substrate
	 *            If true it returns the Michaels constant for substrate, else
	 *            the Michaelis constant for the product.
	 * @return
	 */
	public LocalParameter parameterMichaelis(String species, String enzyme,
			boolean substrate) {
		LocalParameter kM = parameterMichaelis(species, enzyme);
		if (!kM.isSetSBOTerm()) {
			SBMLtools.setSBOTerm(kM,substrate ? 322 : 323);
		}
		return kM;
	}

	/**
	 * Number of binding sites for the inhibitor on the enzyme that catalyses
	 * this reaction.
	 * 
	 * @param enzyme
	 *            Identifier of the catalyzing enzyme. Can be null.
	 * @param inhibitor
	 *            Identifier of the inhibitory species. Can be null.
	 * @return
	 */
	public LocalParameter parameterNumBindingSites(String enzyme,
			String inhibitor) {
		String rid = kineticLaw.getParentSBMLObject().getId();
		StringBuffer exponent = StringTools.concat("m_", rid);
		if (enzyme != null) {
			StringTools.append(exponent, StringTools.underscore, enzyme);
		}
		if (inhibitor != null) {
			StringTools.append(exponent, StringTools.underscore, inhibitor);
		}
		LocalParameter p_exp = createOrGetParameter(exponent.toString());
		if (!p_exp.isSetSBOTerm()) {
			SBMLtools.setSBOTerm(p_exp,189);
		}
		if (!p_exp.isSetName()) {
			p_exp.setName((enzyme != null) ?
					MessageFormat.format(Bundles.MESSAGES.getString("NUMBER_OF_BINDING_SITES_FOR_INHIBITOR_ON_ENZYME_OF_REACTION"),
							(inhibitor != null) ? inhibitor : "", enzyme, rid):
					MessageFormat.format(Bundles.MESSAGES.getString("NUMBER_OF_BINDING_SITES_FOR_INHIBITOR_OF_REACTION"),
							(inhibitor != null) ? inhibitor : "", rid));
		}
		if (!p_exp.isSetUnits())
			p_exp.setUnits(Unit.Kind.DIMENSIONLESS);
		return p_exp;
	}

	/**
	 * Biochemical exponential coefficient
	 * 
	 * @param enzyme
	 *            Identifier of the catalyzing enzyme. Can be null.
	 * @return
	 */
	public LocalParameter parameterReactionCooperativity(String enzyme) {
		String reactionID = kineticLaw.getParentSBMLObject().getId();
		StringBuffer id = StringTools.concat("hco_", reactionID);
		if (enzyme != null) {
			StringTools.append(id, StringTools.underscore, enzyme);
		}
		LocalParameter hr = createOrGetParameter(id.toString());
		if (!hr.isSetSBOTerm()) {
			SBMLtools.setSBOTerm(hr,382);
		}
		if (!hr.isSetName()) {
			hr.setName(Bundles.MESSAGES.getString("REACTION_COOPERATIVITY"));
		}
		if (!hr.isSetUnits()) {
			hr.setUnits(Unit.Kind.DIMENSIONLESS);
		}
		return hr;
	}

	/**
	 * Rho activator according to Liebermeister et al.
	 * 
	 * @param species
	 *            Identifier of the species for which this parameter is to be
	 *            created. Must not be null.
	 * @return
	 */
	public LocalParameter parameterRhoActivation(String species) {
		String rid = kineticLaw.getParentSBMLObject().getId();
		LocalParameter rhoA = createOrGetParameter("rac_", rid,
				StringTools.underscore, species);
		if (!rhoA.isSetName()) {
			rhoA.setName(MessageFormat.format(Bundles.MESSAGES.getString("ACTIVATION_BASELINE_RELATION_OF_SPECIES_IN_REACTION"), 
					species, rid));
		}
		if (!rhoA.isSetUnits()) {
			rhoA.setUnits(Unit.Kind.DIMENSIONLESS);
		}
		return rhoA;
	}

	/**
	 * 
	 * @param species
	 * @return
	 */
	public LocalParameter parameterRhoInhibition(String species) {
		String rid = kineticLaw.getParentSBMLObject().getId();
		LocalParameter rhoI = createOrGetParameter("ric_", rid,
				StringTools.underscore, species);
		if (!rhoI.isSetUnits()) {
			rhoI.setUnits(Unit.Kind.DIMENSIONLESS);
		}
		if (!rhoI.isSetName()) {
			rhoI.setName(MessageFormat.format(Bundles.MESSAGES.getString("INHIBITON_BASELINE_RELATION_OF_SPECIES_IN_REACTION"),
					species, rid));
		}
		return rhoI;
	}

	/**
	 * Parameter for reaction kinetics in restricted spaces.
	 * 
	 * @param h
	 * @return
	 */
	public LocalParameter parameterSpaceRestrictedAssociationConst(double h) {
		Reaction r = kineticLaw.getParentSBMLObject();
		StringBuffer kass = StringTools.concat("kar_", r.getId());
		LocalParameter p_kass = createOrGetParameter(kass.toString());
		if (!p_kass.isSetName()) {
			p_kass.setName(MessageFormat.format(Bundles.MESSAGES.getString("ASSOCIATION_CONSTANT_IN_RESTRICTED_SPACES_OF_REACTION"), r.getId()));
		}
		if (!p_kass.isSetSBOTerm()) {
			// not yet available.
		}
		if (!p_kass.isSetUnits()) {
			UnitDefinition ud = unitFactory
					.unitPerTimeAndConcentrationOrSubstance(r
							.getListOfReactants(), h, defaultParamValue,
							defaultParamValue);
			ListOf<? extends SimpleSpeciesReference> l = r.getListOfModifiers()
					.filterList(new SBOFilter(SBO.getCatalyst()));
			for (SimpleSpeciesReference ssr : l) {
				Species s = ssr.getSpeciesInstance();
				if (unitFactory.getBringToConcentration()
						&& s.hasOnlySubstanceUnits()) {
					ud.multiplyWith(s.getCompartmentInstance()
							.getUnitsInstance());
				} else if (!unitFactory.getBringToConcentration()
						&& !s.hasOnlySubstanceUnits()) {
					ud.divideBy(s.getCompartmentInstance().getUnitsInstance());
				}
				ud.divideBy(s.getUnitsInstance());
			}
			p_kass.setUnits(UnitFactory.checkUnitDefinitions(ud, model));
		}
		return p_kass;
	}

	/**
	 * 
	 * @param rId
	 * @return
	 */
	public LocalParameter parameterSSystemAlpha(String rId) {
		LocalParameter p = createOrGetParameter("alpha_", rId);
		if (!p.isSetSBOTerm()) {
			SBMLtools.setSBOTerm(p,153);
		}
		if (!p.isSetUnits()) {
			p.setUnits(unitFactory.unitSubstancePerTime(model
					.getUnitDefinition("substance"), model
					.getUnitDefinition("time")));
		}
		if (!p.isSetName())
			p.setName(Bundles.MESSAGES.getString("RATE_CONSTANT_FOR_SYNTHESIS"));
		return p;
	}

	/**
	 * 
	 * @param rId
	 * @return
	 */
	public LocalParameter parameterSSystemBeta(String rId) {
		LocalParameter p = createOrGetParameter("beta_", rId);
		if (!p.isSetSBOTerm())
			SBMLtools.setSBOTerm(p,156);
		if (!p.isSetUnits()) {
			p.setUnits(unitFactory.unitSubstancePerTime(model
					.getUnitDefinition("substance"), model
					.getUnitDefinition("time")));
		}
		if (!p.isSetName())
			p.setName(Bundles.MESSAGES.getString("RATE_CONSTANT_FOR_DEGRADATION"));
		return p;
	}

	/**
	 * S-System exponent
	 * 
	 * @return Parameter
	 */
	public LocalParameter parameterSSystemExponent(String modifier) {
		String reactionID = kineticLaw.getParentSBMLObject().getId();
		StringBuffer id = StringTools.concat("ssexp_", reactionID);
		if (modifier != null)
			StringTools.append(id, StringTools.underscore, modifier);
		LocalParameter p = createOrGetParameter(id.toString());
		if (!p.isSetName()) {
			p.setName((modifier != null) ?
					MessageFormat.format(Bundles.MESSAGES.getString("S_SYSTEM_EXPONENT_FOR_MODIFIER_IN_REACTION"), 
							modifier, reactionID) :
					MessageFormat.format(Bundles.MESSAGES.getString("S_SYSTEM_EXPONENT_IN_REACTION"), 
							reactionID));
		}
		if (!p.isSetUnits()) {
			p.setUnits(Unit.Kind.DIMENSIONLESS);
		}
		return p;
	}

	/**
	 * standard chemical potential
	 * 
	 * @param species
	 *            Identifier of the reacting species whose potential parameter
	 *            is to be created. Must not be null.
	 * @return
	 */
	public Parameter parameterStandardChemicalPotential(String species) {
		Parameter mu = createOrGetGlobalParameter("scp_", species);
		if (!mu.isSetName()) {
			mu.setName(MessageFormat.format(Bundles.MESSAGES.getString("STANDARD_CHEMICAL_POTENTIAL_OF_SPECIES"),
					species));
		}
		if (!mu.isSetSBOTerm()) {
			SBMLtools.setSBOTerm(mu,463);
		}
		if (!mu.isSetUnits()) {
			mu.setUnits(unitFactory.unitkJperSubstance(model
					.getSpecies(species).getSubstanceUnitsInstance()));
		}
		return mu;
	}

	/**
	 * Standard Temperature
	 * 
	 * @return Temperature parameter with standard value of 288.15 K.
	 */
	public Parameter parameterTemperature() {
		Parameter T = createOrGetGlobalParameter("T");
		T.setValue(298.15);
		if (!T.isSetSBOTerm()) {
			SBMLtools.setSBOTerm(T,147);
		}
		if (!T.isSetUnits()) {
			T.setUnits(Unit.Kind.KELVIN);
		}
		if (!T.isSetName()) {
			T.setName(MessageFormat.format(Bundles.MESSAGES.getString("TEMP_OF_REACTION_SYSTEM"), model.getId()));
		}
		return T;
	}

	/**
	 * For the generalized hill function: threshold
	 * 
	 * @return Parameter
	 */
	public LocalParameter parameterTheta(String rId, String name) {
		LocalParameter p = createOrGetParameter("theta_", rId,
				StringTools.underscore, name);
		if (!p.isSetSBOTerm()) {
			SBMLtools.setSBOTerm(p,2);
		}
		if (!p.isSetValue()) {
			p.setValue(0);
		}
		if (!p.isSetUnits()) {
			p.setUnits(Unit.Kind.DIMENSIONLESS);
		}
		if (!p.isSetName()) {
			p.setName(Bundles.MESSAGES.getString("THRESHOLD_GENERALIZED_HILL_FUNCTION"));
		}
		return p;
	}

	/**
	 * For space restricted reactions.
	 * 
	 * @param object
	 * @return
	 */
	public LocalParameter parameterTimeOrder() {
		String reactionID = kineticLaw.getParentSBMLObject().getId();
		StringBuffer id = StringTools.concat("hic_", reactionID);
		LocalParameter hr = createOrGetParameter(id.toString());
		if (!hr.isSetSBOTerm()) {
			// not yet available.
		}
		if (!hr.isSetName()) {
			hr.setName(MessageFormat.format(Bundles.MESSAGES.getString("TIME_ORDER_IN_REACTION"), reactionID));
		}
		if (!hr.isSetUnits()) {
			hr.setUnits(Unit.Kind.DIMENSIONLESS);
		}
		return hr;
	}

	/**
	 * For the additive Model: weight parameter
	 * 
	 * @param name
	 * @param rid
	 * @return weight for the weight matrix
	 */
	public LocalParameter parameterV(String name, String rId) {
		LocalParameter p = createOrGetParameter("v_", rId,
				StringTools.underscore, name);
		if (!p.isSetSBOTerm()) {
			SBMLtools.setSBOTerm(p,2);
		}
		if (!p.isSetUnits()) {
			p.setUnits(unitFactory.unitPerTime());
		}
		if (!p.isSetName()) {
			p.setName(Bundles.MESSAGES.getString("FOR_ADDITIVE_MODEL")
					+ ": " + Bundles.MESSAGES.getString("WEIGHT_PARAMETER_FOR_EXTERNAL_INPUTS"));
		}
		return p;
	}

	/**
	 * Creates and annotates the velocity constant for the reaction with the
	 * given id and the number of enzymes.
	 * 
	 * @param enzyme
	 *            Identifier of the catalyzing enzyme. Can be null.
	 * @return
	 */
	public LocalParameter parameterVelocityConstant(String enzyme) {
		LocalParameter kVr;
		String reactionID = kineticLaw.getParentSBMLObject().getId();
		if (enzyme != null) {
			kVr = createOrGetParameter("kcrg_", reactionID,
					StringTools.underscore, enzyme);
			if (!kVr.isSetName()) {
				kVr.setName(MessageFormat.format(
						Bundles.MESSAGES.getString("CATALYTIC_RATE_CONSTANT_GEOMETIC_MEAN_OF_REACTION"),
						reactionID));
			}
			if (!kVr.isSetUnits()) {
				kVr.setUnits(unitFactory.unitPerTimeOrSizePerTime(model
						.getSpecies(enzyme).getCompartmentInstance()));
			}
		} else {
			kVr = createOrGetParameter("vmag_", reactionID);
			if (!kVr.isSetName()) {
				kVr.setName(MessageFormat.format(
						Bundles.MESSAGES.getString("MAX_VELOCITY_GEOMETIC_MEAN_OF_REACTION"),
						reactionID));
			}
			if (!kVr.isSetSBOTerm()) {
				SBMLtools.setSBOTerm(kVr,324);
			}
			if (!kVr.isSetUnits()) {
				if (unitFactory.getBringToConcentration()) {
					kVr.setUnits(unitFactory.unitSubstancePerSizePerTime(model.getVolumeUnitsInstance()));
				} else {
					kVr.setUnits(unitFactory.unitSubstancePerTime(model
							.getSubstanceUnitsInstance(), model
							.getTimeUnitsInstance()));
				}
			}
		}
		return kVr;
	}

	/**
	 * Limiting rate
	 * 
	 * @param forward
	 * @return
	 */
	public LocalParameter parameterVmax(boolean forward) {
		return parameterKcatOrVmax(null, forward);
	}

	/**
	 * For the additive Model: weight parameter
	 * 
	 * @return weight for the weight matrix
	 */
	public LocalParameter parameterW(String name, String rId) {
		LocalParameter p = createOrGetParameter("w_", rId,
				StringTools.underscore, name);
		if (!p.isSetSBOTerm()) {
			SBMLtools.setSBOTerm(p,2);
		}
		if (!p.isSetUnits()) {
			p.setUnits(unitFactory.unitPerTime());
		}
		if (!p.isSetName()) {
			p.setName(Bundles.MESSAGES.getString("FOR_ADDITIVE_MODEL")
					+ ": " + Bundles.MESSAGES.getString("WEIGHT_PARAMETER_FOR_GENE_PRODUCTS"));
		}
		return p;
	}

}
