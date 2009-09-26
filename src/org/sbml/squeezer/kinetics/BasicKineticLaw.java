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
package org.sbml.squeezer.kinetics;

import java.util.IllegalFormatException;
import java.util.LinkedList;
import java.util.List;

import org.sbml.jsbml.ASTNode;
import org.sbml.jsbml.KineticLaw;
import org.sbml.jsbml.Model;
import org.sbml.jsbml.ModifierSpeciesReference;
import org.sbml.jsbml.Parameter;
import org.sbml.jsbml.Reaction;
import org.sbml.jsbml.SBO;
import org.sbml.jsbml.Unit;
import org.sbml.jsbml.UnitDefinition;
import org.sbml.squeezer.RateLawNotApplicableException;
import org.sbml.squeezer.io.StringTools;

/**
 * An abstract super class of specialized kinetic laws.
 * 
 * @since 1.0
 * @version
 * @author <a href="mailto:andreas.draeger@uni-tuebingen.de">Andreas Dr&auml;ger</a>
 * @author <a href="mailto:Nadine.hassis@gmail.com">Nadine Hassis</a>
 * @author <a href="mailto:hannes.borch@googlemail.com">Hannes Borch</a>
 * @date Aug 1, 2007
 */
public abstract class BasicKineticLaw extends KineticLaw {

	/**
	 * identify which Modifer is used
	 * 
	 * @param reactionNum
	 */
	public static final void identifyModifers(Reaction reaction,
			List<String> inhibitors, List<String> transActivators,
			List<String> transInhibitors, List<String> activators,
			List<String> enzymes, List<String> nonEnzymeCatalyzers) {
		inhibitors.clear();
		transActivators.clear();
		transInhibitors.clear();
		activators.clear();
		enzymes.clear();
		nonEnzymeCatalyzers.clear();
		int type;
		for (ModifierSpeciesReference modifier : reaction.getListOfModifiers()) {
			type = modifier.getSBOTerm();
			if (SBO.isModifier(type)) {
				// Ok, this is confusing...
				// inhibitors.add(modifier.getSpecies());
				// activators.add(modifier.getSpecies());
			} else if (SBO.isInhibitor(type))
				inhibitors.add(modifier.getSpecies());
			else if (SBO.isTranscriptionalActivation(type)
					|| SBO.isTranslationalActivation(type))
				transActivators.add(modifier.getSpecies());
			else if (SBO.isTranscriptionalInhibitor(type)
					|| SBO.isTranslationalInhibitor(type))
				transInhibitors.add(modifier.getSpecies());
			else if (SBO.isTrigger(type) || SBO.isStimulator(type))
				// no extra support for unknown catalysis anymore...
				// physical stimulation is now also a stimulator.
				activators.add(modifier.getSpecies());
			else if (SBO.isCatalyst(type)) {
				if (SBO.isEnzymaticCatalysis(type))
					enzymes.add(modifier.getSpecies());
				else
					nonEnzymeCatalyzers.add(modifier.getSpecies());
			}
		}
	}

	private Object typeParameters[];

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
		List<String> modActi = new LinkedList<String>();
		List<String> modCat = new LinkedList<String>();
		List<String> modInhib = new LinkedList<String>();
		List<String> modE = new LinkedList<String>();
		List<String> modTActi = new LinkedList<String>();
		List<String> modTInhib = new LinkedList<String>();
		identifyModifers(parentReaction, modInhib, modTActi, modTInhib,
				modActi, modE, modCat);
		setMath(createKineticEquation(modE, modActi, modTActi, modInhib,
				modTInhib, modCat));
	}

	/**
	 * 
	 * @param k
	 * @param things
	 * @return
	 */
	StringBuffer append(StringBuffer k, Object... things) {
		return StringTools.append(k, things);
	}

	/**
	 * 
	 * @param things
	 * @return
	 */
	StringBuffer concat(Object... things) {
		return StringTools.concat(things);
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
		return createOrGetGlobalParameter(concat(idParts).toString());
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
			p.setValue(1d);
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
		return createOrGetParameter(concat(idParts).toString());
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
			p.setValue(1d);
			addParameter(p);
		}
		return p;
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

	/**
	 * 
	 * @param reactionID
	 * @return
	 */
	Parameter parameterEquilibriumConstant(String reactionID) {
		Parameter keq = createOrGetParameter("keq_", reactionID);
		keq.setSBOTerm(281);
		keq.setName(concat("equilibrium constant of reaction ", reactionID)
				.toString());
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
	 * @param reactionID
	 * @param enzyme
	 * @return
	 */
	Parameter parameterHillCoefficient(String reactionID, String enzyme) {
		StringBuffer id = concat("h_", reactionID);
		if (enzyme != null)
			append(id, underscore, enzyme);
		Parameter hr = createOrGetParameter(id.toString());
		hr.setSBOTerm(193);
		hr.setName("Hill coefficient");
		hr.setUnits(Unit.Kind.DIMENSIONLESS);
		return hr;
	}

	/**
	 * 
	 * @param reactionID
	 * @param species
	 * @return
	 */
	Parameter parameterKa(String reactionID, String species) {
		Parameter kA = createOrGetParameter("ka_", reactionID, underscore,
				species);
		kA.setSBOTerm(363);
		kA.setUnits(unitmM());
		return kA;
	}

	/**
	 * 
	 * @param reactionID
	 * @param enzyme
	 * @param forward
	 * @return The parameter vmax if and only if the number of enzymes is zero,
	 *         otherwise kcat.
	 */
	Parameter parameterKcatOrVmax(String reactionID, String enzyme,
			boolean forward) {
		Parameter kr;
		if (enzyme != null) {
			kr = createOrGetParameter("kcat_", forward ? "fwd" : "bwd",
					underscore, reactionID, underscore, enzyme);
			kr.setSBOTerm(forward ? 320 : 321);
			kr
					.setUnits(new Unit(Unit.Kind.SECOND, -1, getLevel(),
							getVersion()));
		} else {
			kr = createOrGetParameter("vmax_", forward ? "fwd" : "bwd",
					underscore, reactionID);
			kr.setSBOTerm(forward ? 324 : 325);
			kr.setUnits(unitmMperSecond());
		}
		return kr;
	}

	/**
	 * 
	 * @param reactionID
	 * @param species
	 * @return
	 */
	Parameter parameterKi(String reactionID, String species) {
		Parameter kI = createOrGetParameter("ki_", reactionID, underscore,
				species);
		kI.setSBOTerm(261);
		kI.setUnits(unitmM());
		return kI;
	}

	/**
	 * 
	 * @param reactionID
	 * @param species
	 * @param enzyme
	 * @return
	 */
	Parameter parameterMichaelis(String reactionID, String species,
			String enzyme) {
		StringBuffer id = concat("kM_", reactionID, underscore, species);
		if (enzyme != null)
			append(id, underscore, enzyme);
		Parameter kM = createOrGetParameter(id.toString());
		kM.setSBOTerm(27);
		kM.setName(concat("Michaelis constant of species ", species,
				" in reaction ", reactionID).toString());
		kM.setUnits(unitmM());
		return kM;
	}

	/**
	 * 
	 * @param reactionID
	 * @param species
	 * @param enzyme
	 * @return
	 */
	Parameter parameterMichaelisSubstrate(String reactionID, String species,
			String enzyme) {
		Parameter kM = parameterMichaelis(reactionID, species, enzyme);
		kM.setSBOTerm(322);
		return kM;
	}

	/**
	 * 
	 * @param reactionID
	 * @param species
	 * @param enzyme
	 * @return
	 */
	Parameter parameterMichaelisProduct(String reactionID, String species,
			String enzyme) {
		Parameter kM = parameterMichaelis(reactionID, species, enzyme);
		kM.setSBOTerm(323);
		return kM;
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
	 * @param reactionID
	 * @param numEnzymes
	 * @return
	 */
	Parameter parameterVelocityConstant(String reactionID, String enzyme) {
		Parameter kVr;
		if (enzyme != null) {
			kVr = createOrGetParameter("kv_", reactionID, underscore, enzyme);
			kVr.setName(concat("KV value of reaction ", reactionID).toString());
			kVr.setUnits(new Unit(Unit.Kind.SECOND, -1, getLevel(),
					getVersion()));
		} else {
			kVr = createOrGetParameter("vmax_geom_", reactionID);
			kVr.setName(concat(
					"limiting maximal velocity, geometric mean of reaction ",
					reactionID).toString());
			kVr.setUnits(unitmMperSecond());
		}
		return kVr;
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
			mM.addUnit(new Unit(-3, Unit.Kind.MOLE, exponent, getLevel(),
					getVersion()));
			mM.addUnit(new Unit(Unit.Kind.LITRE, -exponent, getLevel(),
					getVersion()));
			getModel().addUnitDefinition(mM);
		}
		return mM;
	}

	/**
	 * Returns the unit milli mole per litre per second.
	 * 
	 * @return
	 */
	UnitDefinition unitmMperSecond() {
		Model model = getModel();
		UnitDefinition mMperSecond = model.getUnitDefinition("mMperSecond");
		if (mMperSecond == null) {
			mMperSecond = unitmM().clone();
			mMperSecond.addUnit(new Unit(Unit.Kind.SECOND, -1, getLevel(),
					getVersion()));
			model.addUnitDefinition(mMperSecond);
		}
		return mMperSecond;
	}
}
