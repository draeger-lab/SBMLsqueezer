/*
 * $Id$
 * $URL$
 * ---------------------------------------------------------------------
 * This file is part of SBMLsqueezer, a Java program that creates rate 
 * equations for reactions in SBML files (http://sbml.org).
 *
 * Copyright (C) 2006-2011 by the University of Tuebingen, Germany.
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
import org.sbml.squeezer.kinetics.BasicKineticLaw;

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
			p.setConstant(true);
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
			BasicKineticLaw.setSBOTerm(p,2);
		}
		if (!p.isSetUnits()) {
			p.setUnits(Unit.Kind.DIMENSIONLESS);
		}
		if (!p.isSetName()) {
			p
					.setName("For the additive Model: weight parameter for the activation function");
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
			StringBuffer name = StringTools.concat(
					"ssociation constant of reaction ", r.getId());
			name.insert(0, zerothOrder ? "Zeroth order a" : "A");
			p_kass.setName(name.toString());
		}
		if (!p_kass.isSetSBOTerm()) {
			BasicKineticLaw.setSBOTerm(p_kass,zerothOrder ? 48 : 153);
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
			BasicKineticLaw.setSBOTerm(p,2);
		}
		if (!p.isSetUnits()) {
			p.setUnits(unitFactory.unitPerTime());
		}
		if (!p.isSetName()) {
			p.setName("For the additive Model: basis expression level");
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
			BasicKineticLaw.setSBOTerm(p,2);
		}
		if (!p.isSetUnits()) {
			p.setUnits(Unit.Kind.DIMENSIONLESS);
		}
		if (!p.isSetName()) {
			p.setName("Weight parameter for the activation function");
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
			BasicKineticLaw.setSBOTerm(coeff,385);
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
			StringBuffer name = StringTools.concat(
					"issociation constant of reaction ", r.getId());
			name.insert(0, zerothOrder ? "Zeroth order d" : "D");
			p_kdiss.setName(name.toString());
		}
		if (!p_kdiss.isSetSBOTerm()) {
			BasicKineticLaw.setSBOTerm(p_kdiss,156);
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
			BasicKineticLaw.setSBOTerm(keq,281);
		}
		if (!keq.isSetName()) {
			keq.setName(StringTools.concat("equilibrium constant of reaction ",
					reactionID).toString());
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
			R.setName("Ideal gas constant");
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
			BasicKineticLaw.setSBOTerm(hr,190);
		}
		if (!hr.isSetName()) {
			StringBuffer name = StringTools.concat("Hill coefficient");
			if (enzyme != null) {
				StringTools.append(name, " for enzyme ", enzyme);
			}
			StringTools.append(name, " in reaction ", reactionID);
			hr.setName(name.toString());
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
			BasicKineticLaw.setSBOTerm(kA,363);
		}
		if (!kA.isSetName()) {
			kA.setName(StringTools.concat("Activation constant of reaction ",
					reactionID).toString());
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
					BasicKineticLaw.setSBOTerm(kr,forward ? 320 : 321);
				} else {
					BasicKineticLaw.setSBOTerm(kr,25);
				}
			}
			if (!kr.isSetName()) {
				StringBuffer name = StringTools
						.concat("catalytic rate constant of ");
				if (enzyme != null) {
					StringTools.append(name, "enzyme ", enzyme, " in ");
				}
				StringTools.append(name, "reaction ", reactionID);
				name.insert(0, forward ? "Substrate " : "Product ");
				kr.setName(name.toString());
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
					BasicKineticLaw.setSBOTerm(kr,forward ? 324 : 325);
				} else {
					BasicKineticLaw.setSBOTerm(kr,186);
				}
			}
			if (!kr.isSetName()) {
				StringBuffer name = StringTools.concat("maximal velocity of ");
				if (enzyme != null) {
					StringTools.append(name, "enzyme ", enzyme, " in ");
				}
				StringTools.append(name, "reaction ", reactionID);
				name.insert(0, forward ? "Forward " : "Reverse ");
				kr.setName(name.toString());
			}
			if (!kr.isSetUnits()) {
				/*
				 * bringToConcentration ? unitmMperSecond() :
				 */
				kr.setUnits(unitFactory.unitSubstancePerTime(model
						.getSubstanceUnitsInstance(), model
						.getTimeUnitsInstance()));
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
			kG.setName(StringTools.concat("Energy constant of species ",
					species).toString());
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
			BasicKineticLaw.setSBOTerm(kI,261);
		}
		if (!kI.isSetName()) {
			StringBuffer name = StringTools.concat("Inhibitory constant");
			if (inhibitorSpecies != null) {
				StringTools.append(name, " for species ", inhibitorSpecies);
			}
			if (enzymeID != null) {
				StringTools.append(name, " of enzyme ", enzymeID);
			}
			if (bindingNum > 0) {
				StringTools.append(name, " with ",
						Integer.toString(bindingNum), " binding positions");
			}
			StringTools.append(name, " in reaction ", reactionID);
			kI.setName(name.toString());
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
			StringBuffer name = StringTools.concat("Kinetic order ");
			StringTools.append(name, " in reaction ", reactionID);
			hr.setName(name.toString());
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
			BasicKineticLaw.setSBOTerm(kS,194);
		}
		if (!kS.isSetName()) {
			StringBuffer name = StringTools.concat(
					"Half saturation constant of species ", species);
			if (enzyme != null) {
				StringTools.append(name, " and enzyme ", enzyme);
			}
			StringTools.append(name, " in reaction ", rid);
			kS.setName(name.toString());
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
			BasicKineticLaw.setSBOTerm(p,2);
		}
		if (!p.isSetUnits()) {
			p.setUnits(Unit.Kind.DIMENSIONLESS);
		}
		if (!p.isSetName()) {
			p.setName("For the additive Model: constant / max. expression");
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
			BasicKineticLaw.setSBOTerm(kM,27);
		}
		if (!kM.isSetName()) {
			StringBuffer name = StringTools.concat(
					"Michaelis constant of species ", species);
			if (enzyme != null) {
				StringTools.append(name, "and enzyme ", enzyme);
			}
			StringTools.append(name, " in reaction ", reactionID);
			kM.setName(name.toString());
		}
		if (!kM.isSetUnits()) {
			Species spec = model.getSpecies(species);
			kM.setUnits(unitFactory.getBringToConcentration() ? unitFactory
					.unitSubstancePerSize(spec.getSubstanceUnitsInstance(),
							spec.getCompartmentInstance().getUnitsInstance())
					: spec.getSubstanceUnitsInstance());
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
			BasicKineticLaw.setSBOTerm(kM,substrate ? 322 : 323);
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
			BasicKineticLaw.setSBOTerm(p_exp,189);
		}
		if (!p_exp.isSetName()) {
			StringBuffer name = StringTools
					.concat("Number of binding sites for the inhibitor");
			if (inhibitor != null) {
				StringTools.append(name, " ", inhibitor);
			}
			if (enzyme != null) {
				StringTools.append(name, " on the enzyme ", enzyme);
			}
			StringTools.append(name, " of reaction ", rid);
			p_exp.setName(name.toString());
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
			BasicKineticLaw.setSBOTerm(hr,382);
		}
		if (!hr.isSetName()) {
			hr.setName("Reaction cooperativity");
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
			rhoA.setName(StringTools.concat(
					"Activation baseline ration of species ", species,
					" in reaction ", rid).toString());
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
			rhoI.setName(StringTools.concat(
					"inhibition baseline ratio of species ", species,
					" in reaction ", rid).toString());
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
			StringBuffer name = StringTools.concat(
					"Association constant in restrictes spaces of reaction ", r
							.getId());
			p_kass.setName(name.toString());
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
			BasicKineticLaw.setSBOTerm(p,153);
		}
		if (!p.isSetUnits()) {
			p.setUnits(unitFactory.unitSubstancePerTime(model
					.getUnitDefinition("substance"), model
					.getUnitDefinition("time")));
		}
		if (!p.isSetName())
			p.setName("rate constant for synthesis");
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
			BasicKineticLaw.setSBOTerm(p,156);
		if (!p.isSetUnits()) {
			p.setUnits(unitFactory.unitSubstancePerTime(model
					.getUnitDefinition("substance"), model
					.getUnitDefinition("time")));
		}
		if (!p.isSetName())
			p.setName("rate constant for degradation");
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
			StringBuffer name = StringTools.concat("S-System exponent");
			if (modifier != null) {
				StringTools.append(name, " for modifier ", modifier);
			}
			StringTools.append(name, " in reaction ", reactionID);
			p.setName(name.toString());
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
			mu.setName(StringTools.concat(
					"Standard chemical potential of species ", species)
					.toString());
		}
		if (!mu.isSetSBOTerm()) {
			BasicKineticLaw.setSBOTerm(mu,463);
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
			BasicKineticLaw.setSBOTerm(T,147);
		}
		if (!T.isSetUnits()) {
			T.setUnits(Unit.Kind.KELVIN);
		}
		if (!T.isSetName()) {
			T.setName("The temperature of reaction system " + model.getId());
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
			BasicKineticLaw.setSBOTerm(p,2);
		}
		if (!p.isSetValue()) {
			p.setValue(0);
		}
		if (!p.isSetUnits()) {
			p.setUnits(Unit.Kind.DIMENSIONLESS);
		}
		if (!p.isSetName()) {
			p.setName("Treshold for the generalized hill function");
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
			StringBuffer name = StringTools.concat("Time order");
			StringTools.append(name, " in reaction ", reactionID);
			hr.setName(name.toString());
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
			BasicKineticLaw.setSBOTerm(p,2);
		}
		if (!p.isSetUnits()) {
			p.setUnits(unitFactory.unitPerTime());
		}
		if (!p.isSetName()) {
			p
					.setName("For the additive Model: weight parameter for the external inputs");
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
				kVr.setName(StringTools.concat(
						"Catalytic rate constant geometric mean of reaction ",
						reactionID).toString());
			}
			if (!kVr.isSetUnits()) {
				kVr.setUnits(unitFactory.unitPerTimeOrSizePerTime(model
						.getSpecies(enzyme).getCompartmentInstance()));
			}
		} else {
			kVr = createOrGetParameter("vmag_", reactionID);
			if (!kVr.isSetName()) {
				kVr.setName(StringTools.concat(
						"Maximal velocity geometric mean of reaction ",
						reactionID).toString());
			}
			if (!kVr.isSetSBOTerm()) {
				BasicKineticLaw.setSBOTerm(kVr,324);
			}
			if (!kVr.isSetUnits()) {
				if (unitFactory.getBringToConcentration()) {
					kVr.setUnits(unitFactory.unitSubstancePerSizePerTime(model
							.getVolumeUnitsInstance()));
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
			BasicKineticLaw.setSBOTerm(p,2);
		}
		if (!p.isSetUnits()) {
			p.setUnits(unitFactory.unitPerTime());
		}
		if (!p.isSetName()) {
			p
					.setName("For the additive Model: weight parameter for the gene products");
		}
		return p;
	}

}
