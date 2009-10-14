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
package org.sbml.squeezer.kinetics;

import java.util.IllegalFormatException;
import java.util.LinkedList;
import java.util.List;

import org.sbml.jsbml.ASTNode;
import org.sbml.jsbml.Compartment;
import org.sbml.jsbml.KineticLaw;
import org.sbml.jsbml.ListOf;
import org.sbml.jsbml.Model;
import org.sbml.jsbml.Parameter;
import org.sbml.jsbml.Reaction;
import org.sbml.jsbml.SBO;
import org.sbml.jsbml.SimpleSpeciesReference;
import org.sbml.jsbml.Species;
import org.sbml.jsbml.SpeciesReference;
import org.sbml.jsbml.Unit;
import org.sbml.jsbml.UnitDefinition;
import org.sbml.squeezer.RateLawNotApplicableException;
import org.sbml.squeezer.ReactionType;
import org.sbml.squeezer.io.StringTools;

/**
 * An abstract super class of specialized kinetic laws.
 * 
 * @since 1.0
 * @version
 * @author <a href="mailto:andreas.draeger@uni-tuebingen.de">Andreas
 *         Dr&auml;ger</a>
 * @author <a href="mailto:Nadine.hassis@gmail.com">Nadine Hassis</a>
 * @author <a href="mailto:hannes.borch@googlemail.com">Hannes Borch</a>
 * @date Aug 1, 2007
 */
public abstract class BasicKineticLaw extends KineticLaw {

	/**
	 * The ids of activators that enhance the progress of this rate law.
	 */
	private List<String> activators;

	/**
	 * If true all species whose hasOnlySubstanceUnits attribute is true are
	 * divided by the size of their surrounding compartment. If false species
	 * whose hasOnlySubstanceUnits attribute is false are multiplied with the
	 * size of their surrounding compartment.
	 */
	private boolean bringToConcentration;

	/**
	 * The default value that is used to initialize new parameters.
	 */
	private double defaultParamValue;

	/**
	 * The ids of the enzymes catalyzing the reaction described by this rate
	 * law.
	 */
	private List<String> enzymes;

	/**
	 * Ids of inhibitors that lower the velocity of this rate law.
	 */
	private List<String> inhibitors;

	/**
	 * The ids of catalysts that are no enzymes.
	 */
	private List<String> nonEnzymeCatalysts;

	/**
	 * Ids of translational or transcriptional activators.
	 */
	private List<String> transActivators;

	/**
	 * Ids of transcriptional or translational inhibitors.
	 */
	private List<String> transInhibitors;

	/**
	 * 
	 */
	private Object typeParameters[];

	/**
	 * True if the reaction system to which the parent reaction belongs has a
	 * full collumn rank.
	 */
	boolean fullRank;

	/**
	 * <ol>
	 * <li>cat</li>
	 * <li>hal</li>
	 * <li>weg</li>
	 * </ol>
	 */
	short type;

	/**
	 * 
	 */
	final Character underscore = StringTools.underscore;

	/**
	 * 
	 * @param parentReaction
	 * @param typeParameters
	 * @throws RateLawNotApplicableException
	 * @throws IllegalFormatException
	 */
	public BasicKineticLaw(Reaction parentReaction, Object... typeParameters)
			throws RateLawNotApplicableException, IllegalFormatException {
		super(parentReaction);
		this.typeParameters = typeParameters;
		this.fullRank = true;
		this.bringToConcentration = false;
		this.defaultParamValue = 1d;
		if (typeParameters.length > 0)
			type = Short.parseShort(getTypeParameters()[0].toString());
		if (typeParameters.length > 1
				&& !Boolean.parseBoolean(getTypeParameters()[1].toString()))
			fullRank = false;
		if (typeParameters.length > 2)
			bringToConcentration = ((Integer) typeParameters[2]).intValue() != 0;
		if (typeParameters.length > 3)
			defaultParamValue = Double
					.parseDouble(typeParameters[3].toString());
		enzymes = new LinkedList<String>();
		activators = new LinkedList<String>();
		transActivators = new LinkedList<String>();
		inhibitors = new LinkedList<String>();
		transInhibitors = new LinkedList<String>();
		nonEnzymeCatalysts = new LinkedList<String>();
		ReactionType.identifyModifers(parentReaction, enzymes, activators,
				transActivators, inhibitors, transInhibitors,
				nonEnzymeCatalysts);
		setMath(createKineticEquation(enzymes, activators, transActivators,
				inhibitors, transInhibitors, nonEnzymeCatalysts));
	}

	/**
	 * Returns a string that gives a simple description of this rate equation.
	 * 
	 * @return
	 */
	public abstract String getSimpleName();

	/**
	 * 
	 * @return
	 */
	public Object[] getTypeParameters() {
		return typeParameters;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.jsbml.MathContainer#setMath(org.sbml.jsbml.ASTNode)
	 */
	public void setMath(ASTNode ast) {
		if (!isSetMath())
			super.setMath(ast);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.MathContainer#toString()
	 */
	// @Override
	public String toString() {
		return isSetSBOTerm() ? SBO.getTerm(getSBOTerm()).getDescription()
				.replace("\\,", ",") : getClass().getSimpleName();
	}

	/**
	 * 
	 * @param modE
	 * @param modActi
	 * @param modTActi
	 * @param modInhib
	 * @param modTInhib
	 * @param modCat
	 * @return
	 * @throws RateLawNotApplicableException
	 * @throws IllegalFormatException
	 */
	abstract ASTNode createKineticEquation(List<String> modE,
			List<String> modActi, List<String> modTActi, List<String> modInhib,
			List<String> modTInhib, List<String> modCat)
			throws RateLawNotApplicableException, IllegalFormatException;

	/**
	 * Concatenates the given name parts of the identifier and returns a global
	 * parameter with this id.
	 * 
	 * @param idParts
	 * @return
	 */
	Parameter createOrGetGlobalParameter(Object... idParts) {
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
	Parameter createOrGetGlobalParameter(String id) {
		Model m = getModel();
		Parameter p = m.getParameter(id);
		if (p == null) {
			p = new Parameter(id, getLevel(), getVersion());
			p.setValue(defaultParamValue);
			p.setConstant(true);
			m.addParameter(p);
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
	Parameter createOrGetParameter(Object... idParts) {
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
	Parameter createOrGetParameter(String id) {
		Parameter p = getParameter(id);
		if (p == null) {
			p = new Parameter(id, getLevel(), getVersion());
			p.setValue(defaultParamValue);
			p.setConstant(true);
			addParameter(p);
		}
		return p;
	}

	/**
	 * Association constant from mass action kinetics.
	 * 
	 * 
	 * @param catalyst
	 * @return
	 */
	Parameter parameterAssociationConst(String catalyst) {
		String reactionID = getParentSBMLObject().getId();
		StringBuffer kass = StringTools.concat("kass_", reactionID);
		if (catalyst != null)
			StringTools.append(kass, underscore, catalyst);
		Parameter p_kass = createOrGetParameter(kass.toString());
		if (!p_kass.isSetName())
			p_kass.setName("association constant of reaction " + reactionID);
		if (!p_kass.isSetSBOTerm())
			p_kass.setSBOTerm(153);
		if (!p_kass.isSetUnits())
			p_kass
					.setUnits(unitPerTimeAndConcentrationOrSubstance(getParentSBMLObject()
							.getListOfReactants()));
		return p_kass;
	}

	/**
	 * 
	 * @param listOf
	 * @return
	 */
	UnitDefinition unitPerTimeAndConcentrationOrSubstance(
			ListOf<SpeciesReference> listOf) {
		String id = "per_time";
		double order = 0;
		int i;
		for (SpeciesReference specRef : listOf)
			order += specRef.getStoichiometry();
		if (order != 1)
			id += "_and_sizeUnits";
		Model model = getModel();
		UnitDefinition ud = new UnitDefinition(id, getLevel(), getVersion());
		ud.divideBy(model.getUnitDefinition("time"));
		UnitDefinition amount = new UnitDefinition("amount", getLevel(),
				getVersion());
		UnitDefinition curr;
		SpeciesReference specRef;
		Species species;
		for (i = 0; i < listOf.size(); i++) {
			specRef = listOf.get(i);
			species = specRef.getSpeciesInstance();
			curr = species.getSubstanceUnitsInstance().clone();
			if (bringToConcentration)
				curr.divideBy(species.getCompartmentInstance()
						.getUnitsInstance());
			curr.raiseByThePowerOf((int) specRef.getStoichiometry() - 1);
			amount.multiplyWith(curr);
		}
		return ud.divideBy(amount).multiplyWith(
				model.getUnitDefinition("substance")).simplify();
	}

	/**
	 * biochemical cooperative inhibitor substrate coefficient.
	 * 
	 * @param name
	 * @param inhibitor
	 * @param enzyme
	 * @return
	 */
	Parameter parameterCooperativeInhibitorSubstrateCoefficient(String name,
			String inhibitor, String enzyme) {
		StringBuffer id = new StringBuffer();
		id.append(name.replace(' ', '_'));
		if (inhibitor != null)
			StringTools.append(id, underscore, inhibitor);
		if (enzyme != null)
			StringTools.append(id, underscore, enzyme);
		Parameter coeff = createOrGetParameter(id);
		if (!coeff.isSetSBOTerm())
			coeff.setSBOTerm(385);
		if (!coeff.isSetUnits())
			coeff.setUnits(Unit.Kind.DIMENSIONLESS);
		return coeff;
	}

	/**
	 * Dissociation constant in mass action kinetics
	 * 
	 * 
	 * @param catalyst
	 * @return
	 */
	Parameter parameterDissociationConst(String catalyst) {
		String reactionID = getParentSBMLObject().getId();
		StringBuffer kdiss = StringTools.concat("kdiss_", reactionID);
		if (catalyst != null)
			StringTools.append(kdiss, underscore, catalyst);
		Parameter p_kdiss = createOrGetParameter(kdiss.toString());
		if (!p_kdiss.isSetName())
			p_kdiss.setName("dissociation constant of reaction " + reactionID);
		if (!p_kdiss.isSetSBOTerm())
			p_kdiss.setSBOTerm(156);
		if (!p_kdiss.isSetUnits())
			p_kdiss
					.setUnits(unitPerTimeAndConcentrationOrSubstance(getParentSBMLObject()
							.getListOfProducts()));
		return p_kdiss;
	}

	/**
	 * 
	 * 
	 * @return
	 */
	Parameter parameterEquilibriumConstant() {
		String reactionID = getParentSBMLObject().getId();
		Parameter keq = createOrGetParameter("keq_", reactionID);
		keq.setSBOTerm(281);
		keq.setName(StringTools.concat("equilibrium constant of reaction ",
				reactionID).toString());
		return keq;
	}

	/**
	 * 
	 * @return
	 */
	Parameter parameterGasConstant() {
		Parameter R = createOrGetGlobalParameter("R");
		R.setValue(8.31447215);
		R.setName("ideal gas constant");
		R.setUnits(unitJperKandM());
		return R;
	}

	/**
	 * 
	 * 
	 * @param enzyme
	 * @return
	 */
	Parameter parameterHillCoefficient(String enzyme) {
		String reactionID = getParentSBMLObject().getId();
		StringBuffer id = StringTools.concat("h_", reactionID);
		if (enzyme != null)
			StringTools.append(id, underscore, enzyme);
		Parameter hr = createOrGetParameter(id.toString());
		hr.setSBOTerm(190);
		hr.setName("Hill coefficient");
		hr.setUnits(Unit.Kind.DIMENSIONLESS);
		return hr;
	}

	/**
	 * 
	 * 
	 * @param activatorSpecies
	 * @return
	 */
	Parameter parameterKa(String activatorSpecies) {
		String reactionID = getParentSBMLObject().getId();
		Parameter kA = createOrGetParameter("ka_", reactionID, underscore,
				activatorSpecies);
		kA.setSBOTerm(363);
		kA.setUnits(bringToConcentration ? unitmM() : getModel()
				.getUnitDefinition("substance"));
		return kA;
	}

	/**
	 * 
	 * @param enzyme
	 * @param forward
	 * @return
	 */
	Parameter parameterKcat(String enzyme, boolean forward) {
		return parameterKcatOrVmax(enzyme, forward);
	}

	/**
	 * Turn over rate if enzyme is null and limiting rate otherwise.
	 * 
	 * 
	 * @param enzyme
	 * @param forward
	 * @return The parameter vmax if and only if the number of enzymes is zero,
	 *         otherwise kcat.
	 */
	Parameter parameterKcatOrVmax(String enzyme, boolean forward) {
		String reactionID = getParentSBMLObject().getId();
		Parameter kr;
		if (enzyme != null) {
			StringBuffer id = StringTools.concat("kcat_");
			if (getParentSBMLObject().getReversible())
				StringTools.append(id, forward ? "fwd" : "bwd", underscore);
			StringTools.append(id, reactionID, underscore, enzyme);
			kr = createOrGetParameter(id);
			if (!kr.isSetSBOTerm()) {
				if (getParentSBMLObject().getReversible())
					kr.setSBOTerm(forward ? 320 : 321);
				else
					kr.setSBOTerm(25);
			}
			if (!kr.isSetUnits())
				kr.setUnits(unitPerTimeOrSizePerTime(getModel().getSpecies(
						enzyme).getCompartmentInstance()));
		} else {
			kr = createOrGetParameter("vmax_", forward ? "fwd" : "bwd",
					underscore, reactionID);
			if (!kr.isSetSBOTerm()) {
				if (getParentSBMLObject().getReversible())
					kr.setSBOTerm(forward ? 324 : 325);
				else
					kr.setSBOTerm(186);
			}
			if (!kr.isSetUnits())
				kr.setUnits(/*
							 * bringToConcentration ? unitmMperSecond() :
							 */unitmmolePerSecond());
		}
		return kr;
	}

	/**
	 * energy constant of given species
	 * 
	 * @param species
	 * @return
	 */
	Parameter parameterKG(String species) {
		Parameter kG = createOrGetGlobalParameter("kG_", species);
		kG.setUnits(Unit.Kind.DIMENSIONLESS);
		kG.setName(StringTools.concat("energy constant of species ", species)
				.toString());
		return kG;
	}

	/**
	 * 
	 * @param inhibitorSpecies
	 * @return
	 */
	Parameter parameterKi(String inhibitorSpecies) {
		return parameterKi(inhibitorSpecies, null);
	}

	/**
	 * 
	 * @param inhibitorSpecies
	 * @param enzyme
	 * @return
	 */
	Parameter parameterKi(String inhibitorSpecies, String enzyme) {
		return parameterKi(inhibitorSpecies, enzyme, 0);
	}

	/**
	 * 
	 * @param inhibitorSpecies
	 * @param enzymeID
	 * @param bindingNum
	 *            if <= 0 not considered.
	 * @return
	 */
	Parameter parameterKi(String inhibitorSpecies, String enzymeID,
			int bindingNum) {
		StringBuffer id = StringTools.concat("ki");
		if (bindingNum > 0)
			StringTools.append(id, Integer.toString(bindingNum));
		String reactionID = getParentSBMLObject().getId();
		StringTools.append(id, underscore, reactionID);
		if (inhibitorSpecies != null)
			StringTools.append(id, underscore, inhibitorSpecies);
		if (enzymeID != null)
			StringTools.append(id, underscore, enzymeID);
		Parameter kI = createOrGetParameter(id.toString());
		if (!kI.isSetSBOTerm())
			kI.setSBOTerm(261);
		if (!kI.isSetUnits())
			if (bringToConcentration)
				kI.setUnits(unitmM());
			else
				kI.setUnits(unitmmole());
		return kI;
	}

	/**
	 * Half saturation constant.
	 * 
	 * 
	 * @param enzyme
	 * @return
	 */
	Parameter parameterKS(String enzyme) {
		Parameter kS = createOrGetParameter("kSp_", getParentSBMLObject()
				.getId());
		kS.setSBOTerm(194);
		if (bringToConcentration)
			kS.setUnits(unitmM());
		else
			kS.setUnits(unitmmole());
		return kS;
	}

	/**
	 * 
	 * 
	 * @param species
	 * @param enzyme
	 * @return
	 */
	Parameter parameterMichaelis(String species, String enzyme) {
		String reactionID = getParentSBMLObject().getId();
		StringBuffer id = StringTools.concat("kM_", reactionID, underscore,
				species);
		if (enzyme != null)
			StringTools.append(id, underscore, enzyme);
		Parameter kM = createOrGetParameter(id.toString());
		if (kM.isSetSBOTerm())
			kM.setSBOTerm(27);
		if (!kM.isSetName())
			kM.setName(StringTools.concat("Michaelis constant of species ",
					species, " in reaction ", reactionID).toString());
		if (!kM.isSetUnits())
			kM.setUnits(bringToConcentration ? unitmM() : getModel()
					.getUnitDefinition("substance"));
		return kM;
	}

	/**
	 * Michaelis constant.
	 * 
	 * @param species
	 * @param enzyme
	 * @param substrate
	 *            If true it returns the Michaels constant for substrate, else
	 *            the Michaelis constant for the product.
	 * @return
	 */
	Parameter parameterMichaelis(String species, String enzyme,
			boolean substrate) {
		Parameter kM = parameterMichaelis(species, enzyme);
		kM.setSBOTerm(substrate ? 322 : 323);
		return kM;
	}

	/**
	 * Number of binding sites for the inhibitor on the enzyme that catalyses
	 * this reaction.
	 * 
	 * @param enzyme
	 * @param inhibitor
	 * @return
	 */
	Parameter parameterNumBindingSites(String enzyme, String inhibitor) {
		StringBuffer exponent = StringTools.concat("m_", getParentSBMLObject()
				.getId());
		if (enzyme != null)
			StringTools.append(exponent, underscore, enzyme);
		if (inhibitor != null)
			StringTools.append(exponent, underscore, inhibitor);
		Parameter p_exp = createOrGetParameter(exponent.toString());
		if (!p_exp.isSetSBOTerm())
			p_exp.setSBOTerm(189);
		if (!p_exp.isSetUnits())
			p_exp.setUnits(new Unit(Unit.Kind.DIMENSIONLESS, getLevel(),
					getVersion()));
		return p_exp;
	}

	/**
	 * 
	 * @param species
	 * @return
	 */
	Parameter parameterRhoActivation(String species) {
		Parameter rhoA = createOrGetParameter("rho_act_", getParentSBMLObject()
				.getId(), underscore, species);
		rhoA.setUnits(Unit.Kind.DIMENSIONLESS);
		return rhoA;
	}

	/**
	 * 
	 * @param species
	 * @return
	 */
	Parameter parameterStandardChemicalPotential(String species) {
		Parameter mu = createOrGetGlobalParameter("mu0_", species);
		mu.setName("standard chemical potential");
		mu.setSBOTerm(463);
		mu.setUnits(unitkJperMole());
		return mu;
	}

	/**
	 * 
	 * @return
	 */
	Parameter parameterTemperature() {
		Parameter T = createOrGetGlobalParameter("T");
		T.setSBOTerm(147);
		T.setValue(298.15);
		T.setUnits(Unit.Kind.KELVIN);
		return T;
	}

	/**
	 * Creates and annotates the velocity constant for the reaction with the
	 * given id and the number of enzymes.
	 * 
	 * @param numEnzymes
	 * @return
	 */
	Parameter parameterVelocityConstant(String enzyme) {
		Parameter kVr;
		String reactionID = getParentSBMLObject().getId();
		if (enzyme != null) {
			kVr = createOrGetParameter("kv_", reactionID, underscore, enzyme);
			kVr.setName(StringTools.concat("KV value of reaction ", reactionID)
					.toString());
			if (!kVr.isSetUnits())
				kVr.setUnits(unitPerTimeOrSizePerTime(getModel().getSpecies(
						enzyme).getCompartmentInstance()));
		} else {
			kVr = createOrGetParameter("vmax_geom_", reactionID);
			kVr.setName(StringTools.concat(
					"limiting maximal velocity, geometric mean of reaction ",
					reactionID).toString());
			kVr.setUnits(bringToConcentration ? unitmMperSecond()
					: unitmmolePerSecond());
		}
		return kVr;
	}

	/**
	 * Limiting rate
	 * 
	 * @param forward
	 * @return
	 */
	Parameter parameterVmax(boolean forward) {
		return parameterKcatOrVmax(null, forward);
	}

	/**
	 * 
	 * @param catalyst
	 * @return
	 */
	Parameter parameterZerothOrderForward(String catalyst) {
		StringBuffer kass = StringTools.concat("kass_", getParentSBMLObject()
				.getId());
		if (catalyst != null)
			StringTools.append(kass, underscore, catalyst);
		Parameter p_kass = createOrGetParameter(kass.toString());
		if (!p_kass.isSetSBOTerm())
			p_kass.setSBOTerm(48);
		return p_kass;
	}

	/**
	 * 
	 * @param catalyst
	 * @return
	 */
	Parameter parameterZerothOrderReverse(String catalyst) {
		StringBuffer kdiss = StringTools.concat("kdiss_", getParentSBMLObject()
				.getId());
		if (catalyst != null)
			StringTools.append(kdiss, underscore, catalyst);
		Parameter p_kdiss = createOrGetParameter(kdiss.toString());
		if (!p_kdiss.isSetSBOTerm())
			p_kdiss.setSBOTerm(352);
		return p_kdiss;
	}

	/**
	 * 
	 * @param simpleSpecRef
	 * @return
	 */
	ASTNode speciesTerm(SimpleSpeciesReference simpleSpecRef) {
		return speciesTerm(simpleSpecRef.getSpeciesInstance());
	}

	/**
	 * 
	 * @param species
	 * @return
	 */
	ASTNode speciesTerm(Species species) {
		ASTNode specTerm = new ASTNode(species, this);
		if (species.getHasOnlySubstanceUnits()) {
			// set initial amount and units appropriately
			if (bringToConcentration)
				specTerm.divideBy(species.getCompartmentInstance());
		} else {
			// multiply with compartment size.
			if (!bringToConcentration)
				specTerm.multiplyWith(species.getCompartmentInstance());
		}
		return specTerm;
	}

	/**
	 * 
	 * @param species
	 * @return
	 */
	ASTNode speciesTerm(String species) {
		return speciesTerm(getModel().getSpecies(species));
	}

	/**
	 * 
	 * @return
	 */
	UnitDefinition unitJperKandM() {
		UnitDefinition ud = new UnitDefinition("J_per_K_and_mole", getLevel(),
				getVersion());
		ud.addUnit(new Unit(Unit.Kind.JOULE, getLevel(), getVersion()));
		ud.addUnit(new Unit(1, 0, Unit.Kind.KELVIN, -1, getLevel(),
				getVersion()));
		ud
				.addUnit(new Unit(1, 0, Unit.Kind.MOLE, -1, getLevel(),
						getVersion()));
		getModel().addUnitDefinition(ud);
		return ud;
	}

	/**
	 * 
	 * @return
	 */
	UnitDefinition unitkJperMole() {
		UnitDefinition kJperMole = new UnitDefinition("kJ_per_mole",
				getLevel(), getVersion());
		kJperMole
				.addUnit(new Unit(3, Unit.Kind.JOULE, getLevel(), getVersion()));
		kJperMole.addUnit(new Unit(Unit.Kind.MOLE, getLevel(), getVersion()));
		getModel().addUnitDefinition(kJperMole);
		return kJperMole;
	}

	/**
	 * 
	 * @return
	 */
	UnitDefinition unitmM() {
		return unitmM(1);
	}

	/**
	 * 
	 * @param exponent
	 * @return
	 */
	UnitDefinition unitmM(int exponent) {
		String id = "mM";
		if (exponent != 1)
			id += exponent;
		UnitDefinition mM = getModel().getUnitDefinition(id);
		if (mM == null) {
			mM = new UnitDefinition(id, getLevel(), getVersion());
			mM.addUnit(unitmmole());
			mM.addUnit(new Unit(Unit.Kind.LITRE, -exponent, getLevel(),
					getVersion()));
			getModel().addUnitDefinition(mM);
		}
		return mM;
	}

	/**
	 * Creates and returns the unit milli mole.
	 * 
	 * @return A Unit object that represents the unit milli mole.
	 */
	Unit unitmmole() {
		return new Unit(-3, Unit.Kind.MOLE, getLevel(), getVersion());
	}

	/**
	 * 
	 * @return
	 */
	UnitDefinition unitmmolePerSecond() {
		Model model = getModel();
		String id = "mmolePerSecond";
		UnitDefinition mmolePerSecond = model.getUnitDefinition(id);
		if (mmolePerSecond == null) {
			mmolePerSecond = new UnitDefinition(id, getLevel(), getVersion());
			mmolePerSecond.addUnit(new Unit(-3, Unit.Kind.MOLE, getLevel(),
					getVersion()));
			mmolePerSecond.addUnit(new Unit(Unit.Kind.SECOND, -1, getLevel(),
					getVersion()));
			model.addUnitDefinition(mmolePerSecond);
		}
		return mmolePerSecond;
	}

	/**
	 * 
	 * @return Unit milli mole per metre.
	 */
	UnitDefinition unitmMperMetre() {
		UnitDefinition mMperMetre = unitmM().clone();
		mMperMetre.addUnit(new Unit(Unit.Kind.METRE, -1, getLevel(),
				getVersion()));
		return mMperMetre;
	}

	/**
	 * Returns the unit milli mole per litre per second.
	 * 
	 * @return
	 */
	UnitDefinition unitmMperSecond() {
		Model model = getModel();
		String id = "mMperSecond";
		UnitDefinition mMperSecond = model.getUnitDefinition(id);
		if (mMperSecond == null) {
			mMperSecond = unitmM().clone();
			mMperSecond.setId(id);
			mMperSecond.addUnit(new Unit(Unit.Kind.SECOND, -1, getLevel(),
					getVersion()));
			model.addUnitDefinition(mMperSecond);
		}
		return mMperSecond;
	}

	/**
	 * 
	 * @return Unit milli mole per square metre.
	 */
	UnitDefinition unitmMperSquareMetre() {
		UnitDefinition mMperSquareMetre = unitmM().clone();
		mMperSquareMetre.addUnit(new Unit(Unit.Kind.METRE, -2, getLevel(),
				getVersion()));
		return mMperSquareMetre;
	}

	/**
	 * 1/s, equivalent to Hz.
	 * 
	 * @return
	 */
	UnitDefinition unitPerTime() {
		UnitDefinition ud = getModel().getUnitDefinition("time").clone();
		if (ud.getNumUnits() == 1) {
			ud.getListOfUnits().getFirst().setExponent(-1);
			ud.setId("perTime");
		} else {
			ud = new UnitDefinition("perSecond", getLevel(), getVersion());
			Unit unit = new Unit(Unit.Kind.SECOND, -1, getLevel(), getVersion());
			ud.addUnit(unit);
		}
		return ud;
	}

	/**
	 * 
	 * @param c
	 * @return
	 */
	UnitDefinition unitPerTimeOrSizePerTime(Compartment c) {
		if (bringToConcentration) {
			Model model = getModel();
			StringBuilder name = new StringBuilder();
			if (!c.isSetUnits())
				c.setUnits(model.getUnitDefinition("volume"));
			UnitDefinition sizeUnit = c.getUnitsInstance();
			if (sizeUnit.isVariantOfVolume())
				name.append("volume");
			else if (sizeUnit.isVariantOfArea())
				name.append("area");
			else if (sizeUnit.isVariantOfLength())
				name.append("length");
			name.append(" per time");
			String id = name.toString().replace(' ', '_');
			UnitDefinition ud = model.getUnitDefinition(id);
			if (ud == null) {
				ud = new UnitDefinition(sizeUnit);
				ud.setId(id);
				ud.divideBy(model.getUnitDefinition("time"));
				ud.setName(name.toString());
				model.addUnitDefinition(ud);
			}
			return ud;
		} else {
			return unitPerTime();
		}
	}
}
