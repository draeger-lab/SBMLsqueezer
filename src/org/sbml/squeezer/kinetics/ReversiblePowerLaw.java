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
import java.util.List;

import org.sbml.jsbml.ASTNode;
import org.sbml.jsbml.ModifierSpeciesReference;
import org.sbml.jsbml.Parameter;
import org.sbml.jsbml.Reaction;
import org.sbml.jsbml.SBO;
import org.sbml.jsbml.SpeciesReference;
import org.sbml.jsbml.Unit;
import org.sbml.jsbml.UnitDefinition;
import org.sbml.squeezer.RateLawNotApplicableException;

/**
 * Represents the reversible power law (RP) from Liebermeister et al.
 * 
 * @author Andreas Dr&auml;ger <a
 *         href="mailto:andreas.draeger@uni-tuebingen.de">
 *         andreas.draeger@uni-tuebingen.de</a>
 * @date 2009-09-17
 */
public class ReversiblePowerLaw extends BasicKineticLaw implements
		InterfaceUniUniKinetics, InterfaceBiUniKinetics, InterfaceBiBiKinetics,
		InterfaceArbitraryEnzymeKinetics, InterfaceReversibleKinetics,
		InterfaceIrreversibleKinetics, InterfaceModulatedKinetics {

	/**
	 * <ol>
	 * <li>cat</li>
	 * <li>hal</li>
	 * <li>weg</li>
	 * </ol>
	 */
	private short type;

	/**
	 * 
	 * @param parentReaction
	 * @param type
	 * @throws RateLawNotApplicableException
	 * @throws IllegalFormatException
	 */
	public ReversiblePowerLaw(Reaction parentReaction, Object type)
			throws RateLawNotApplicableException, IllegalFormatException {
		super(parentReaction, type);
		setSBOTerm(42);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.sbml.squeezer.kinetics.BasicKineticLaw#createKineticEquation(java
	 * .util.List, java.util.List, java.util.List, java.util.List,
	 * java.util.List, java.util.List)
	 */
	// @Override
	ASTNode createKineticEquation(List<String> modE, List<String> modActi,
			List<String> modTActi, List<String> modInhib,
			List<String> modTInhib, List<String> modCat)
			throws RateLawNotApplicableException, IllegalFormatException {
		int i;
		Reaction r = getParentSBMLObject();
		unsetSBOTerm();
		ASTNode activation = null, inhibition = null;
		for (String acti : modActi) {
			ModifierSpeciesReference msr = null;
			for (i = 0; i < r.getNumModifiers() && msr == null; i++)
				if (r.getModifier(i).getSpecies().equals(acti))
					msr = r.getModifier(i);
			Parameter kA = createOrGetParameter("ka_", r.getId(), underscore,
					msr.getSpecies());
			kA.setSBOTerm(363);
			kA.setUnits(mM());
			ASTNode curr = ASTNode.frac(this, msr.getSpeciesInstance(), kA);
			curr.divideBy(ASTNode.sum(new ASTNode(1, this), curr.clone()));
			if (SBO.isEssentialActivator(msr.getSBOTerm())) {
				if (activation == null)
					activation = curr;
				else
					activation.multiplyWith(curr);
			} else /* if (SBO.isNonEssentialActivator(msr.getSBOTerm())) */{
				Parameter rhoA = createOrGetParameter("rho_act_", r.getId(),
						underscore, msr.getSpecies());
				rhoA.setUnits(Unit.Kind.DIMENSIONLESS);
				curr = ASTNode.sum(new ASTNode(rhoA, this), ASTNode.times(
						ASTNode.diff(new ASTNode(1, this), new ASTNode(rhoA,
								this)), curr));
				if (activation == null)
					activation = curr;
				else
					activation.multiplyWith(curr);
			}
		}
		for (String inhib : modInhib) {
			ModifierSpeciesReference msr = null;
			for (i = 0; i < r.getNumModifiers() && msr == null; i++)
				if (r.getModifier(i).getSpecies().equals(inhib))
					msr = r.getModifier(i);
			if (SBO.isNonCompetetiveInhibitor(msr.getSBOTerm())) {
				Parameter kI = createOrGetParameter("ki_", r.getId(),
						underscore, msr.getSpecies());
				kI.setSBOTerm(261);
				kI.setUnits(mM());
				ASTNode curr = ASTNode.frac(new ASTNode(1, this), ASTNode.sum(
						new ASTNode(1, this), ASTNode.frac(this, msr
								.getSpeciesInstance(), kI)));
				if (inhibition == null)
					inhibition = curr;
				else
					inhibition.multiplyWith(curr);
			}
		}
		type = Short.parseShort(getTypeParameters()[0].toString());
		ASTNode numerator[] = new ASTNode[Math.max(1, modE.size())];
		for (i = 0; i < numerator.length; i++) {
			switch (type) {
			case 0: // CAT
				numerator[i] = cat(r, modE.size());
				break;
			case 1: // HAL
				numerator[i] = hal(r, modE.size());
				break;
			default: // WEG
				numerator[i] = weg(r, modE.size());
				break;
			}
			numerator[i].divideBy(denominator(r));
			if (i < modE.size()) {
				ModifierSpeciesReference enzyme = null;
				for (int j = 0; j < r.getNumModifiers() && enzyme == null; j++)
					if (r.getModifier(j).getSpecies().equals(modE.get(j)))
						enzyme = r.getModifier(j);
				numerator[i] = ASTNode.times(new ASTNode(enzyme
						.getSpeciesInstance(), this), numerator[i]);
			}
		}
		return ASTNode.times(activation, inhibition, ASTNode.sum(numerator));
	}

	/**
	 * The denominator of this kinetic law
	 * 
	 * @param r
	 * @return
	 */
	ASTNode denominator(Reaction r) {
		ASTNode denominator = new ASTNode(1, this);
		ASTNode competInhib = competetiveInhibitionSummand(r);
		return competInhib.isUnknown() ? denominator : denominator
				.plus(competInhib);
	}

	/**
	 * Creates the summand for competetive inhibition in the denominator.
	 * 
	 * @param r
	 * @return
	 */
	ASTNode competetiveInhibitionSummand(Reaction r) {
		ASTNode inhib = new ASTNode(this);
		for (ModifierSpeciesReference msr : r.getListOfModifiers())
			if (SBO.isCompetetiveInhibitor(msr.getSBOTerm())) {
				Parameter kI = createOrGetParameter("ki_", r.getId(),
						underscore, msr.getSpecies());
				kI.setSBOTerm(261);
				kI.setUnits(mM());
				ASTNode curr = ASTNode.frac(this, msr.getSpeciesInstance(), kI);
				if (inhib.isUnknown())
					inhib = curr;
				else
					inhib.plus(curr);
			}
		return inhib;
	}

	/**
	 * Weg version of the numerator
	 * 
	 * @param numEnzymes
	 * 
	 * @param listOfReactants
	 * @param listOfProducts
	 * @return
	 */
	private ASTNode weg(Reaction r, int numEnzymes) {
		ASTNode numerator = new ASTNode(
				velocityConstant(r.getId(), numEnzymes), this);
		Parameter hr = createOrGetParameter("h_", r.getId());
		hr.setSBOTerm(193);
		hr.setUnits(Unit.Kind.DIMENSIONLESS);
		Parameter R = createOrGetGlobalParameter("R");
		R.setValue(8.31447215);
		R.setName("ideal gas constant");
		UnitDefinition ud = new UnitDefinition("J_per_K_and_mole",
				r.getLevel(), r.getVersion());
		ud.addUnit(new Unit(Unit.Kind.JOULE, r.getLevel(), r.getVersion()));
		ud.addUnit(new Unit(1, 0, Unit.Kind.KELVIN, -1, r.getLevel(), r
				.getVersion()));
		ud.addUnit(new Unit(1, 0, Unit.Kind.MOLE, -1, r.getLevel(), r
				.getVersion()));
		r.getModel().addUnitDefinition(ud);
		R.setUnits(ud);
		Parameter T = createOrGetGlobalParameter("T");
		T.setSBOTerm(147);
		T.setValue(298.15);
		T.setUnits(Unit.Kind.KELVIN);
		ASTNode exponent = null;
		ASTNode forward = null, backward = null;
		UnitDefinition kJperMole = new UnitDefinition("kJ_per_mole",
				getLevel(), getVersion());
		kJperMole
				.addUnit(new Unit(3, Unit.Kind.JOULE, getLevel(), getVersion()));
		kJperMole.addUnit(new Unit(Unit.Kind.MOLE, getLevel(), getVersion()));
		r.getModel().addUnitDefinition(kJperMole);
		Parameter mu;
		for (SpeciesReference specRef : r.getListOfReactants()) {
			ASTNode curr = new ASTNode(specRef.getSpeciesInstance(), this);
			curr.raiseByThePowerOf(ASTNode.times(new ASTNode(hr, this),
					new ASTNode(specRef.getStoichiometry(), this)));
			if (forward == null)
				forward = curr;
			else
				forward.multiplyWith(curr);
			mu = createOrGetGlobalParameter("mu0_", specRef.getSpecies());
			mu.setName("standard chemical potential");
			mu.setSBOTerm(463);
			mu.setUnits(kJperMole);
			ASTNode product = ASTNode.times(new ASTNode(specRef
					.getStoichiometry(), this), new ASTNode(mu, this));
			if (exponent == null)
				exponent = product;
			else
				exponent.multiplyWith(product);
		}
		for (SpeciesReference specRef : r.getListOfProducts()) {
			if (r.getReversible()) {
				ASTNode curr = new ASTNode(specRef.getSpeciesInstance(), this);
				curr.raiseByThePowerOf(ASTNode.times(new ASTNode(hr, this),
						new ASTNode(specRef.getStoichiometry(), this)));
				if (backward == null)
					backward = curr;
				else
					backward.multiplyWith(curr);
			}
			mu = createOrGetGlobalParameter("mu0_", specRef.getSpecies());
			mu.setName("standard chemical potential");
			mu.setSBOTerm(463);
			mu.setUnits(kJperMole);
			ASTNode product = ASTNode.times(new ASTNode(specRef
					.getStoichiometry(), this), new ASTNode(mu, this));
			if (exponent == null)
				exponent = product;
			else
				exponent.multiplyWith(product);
		}
		if (exponent == null)
			exponent = new ASTNode(hr, this);
		else
			exponent.multiplyWith(new ASTNode(hr, this));
		exponent.divideBy(ASTNode.times(new ASTNode(2, this), new ASTNode(R,
				this), new ASTNode(T, this)));
		exponent = ASTNode.exp(exponent);
		if (forward == null)
			forward = new ASTNode(1, this);
		forward.divideBy(exponent);
		if (r.getReversible()) {
			backward.multiplyWith(exponent.clone());
			forward.minus(backward);
		}
		return numerator.multiplyWith(forward).divideBy(createRoot(r));
	}

	/**
	 * Hal version of the numerator
	 * 
	 * @param numEnzymes
	 * 
	 * @param listOfReactants
	 * @param listOfProducts
	 * @return
	 */
	private ASTNode hal(Reaction r, int numEnzymes) {
		ASTNode numerator = new ASTNode(
				velocityConstant(r.getId(), numEnzymes), this);
		Parameter hr = createOrGetParameter("h_", r.getId());
		hr.setSBOTerm(193);
		hr.setName("Hill coefficient");
		hr.setUnits(Unit.Kind.DIMENSIONLESS);
		Parameter keq = createOrGetParameter("keq_", r.getId());
		keq.setSBOTerm(281);
		keq.setName(concat("equilibrium constant of reaction ", r.getId())
				.toString());
		ASTNode forward = ASTNode.sqrt(ASTNode.pow(new ASTNode(keq, this),
				new ASTNode(hr, this)));
		double x = 0;
		for (SpeciesReference specRef : r.getListOfReactants()) {
			forward.multiplyWith(ASTNode.pow(new ASTNode(specRef
					.getSpeciesInstance(), this), ASTNode.times(new ASTNode(
					specRef.getStoichiometry(), this), new ASTNode(hr, this))));
			x += specRef.getStoichiometry();
		}
		numerator.multiplyWith(forward);
		if (r.getReversible()) {
			ASTNode backward = ASTNode.frac(1, ASTNode.sqrt(ASTNode.pow(this,
					keq, hr)));
			for (SpeciesReference specRef : r.getListOfProducts()) {
				backward.multiplyWith(ASTNode.pow(new ASTNode(specRef
						.getSpeciesInstance(), this), ASTNode.times(
						new ASTNode(specRef.getStoichiometry(), this),
						new ASTNode(hr, this))));
				x -= specRef.getStoichiometry();
			}
			numerator.minus(backward);
		}
		keq.setUnits(mM((int) x));
		return numerator.divideBy(createRoot(r));
	}

	/**
	 * Creates and annotates the velocity constant for the reaction with the
	 * given id and the number of enzymes.
	 * 
	 * @param id
	 * @param numEnzymes
	 * @return
	 */
	private Parameter velocityConstant(String id, int numEnzymes) {
		Parameter kVr;
		if (numEnzymes > 0) {
			kVr = createOrGetParameter("kv_", id);
			kVr.setName(concat("KV value of reaction ", id).toString());
			kVr.setUnits(new Unit(Unit.Kind.SECOND, -1, getLevel(),
					getVersion()));
		} else {
			kVr = createOrGetParameter("vmax_geom_", id);
			kVr.setName(concat(
					"limiting maximal velocity, geometric mean of reaction ",
					id).toString());
			kVr.setUnits(mMperSecond());
		}
		return kVr;
	}

	/**
	 * Creates the root term for hal and weg.
	 * 
	 * @param r
	 * @return
	 */
	private ASTNode createRoot(Reaction r) {
		ASTNode root = null, exponent = null, curr = null;
		Parameter kM = null;
		Parameter hr = createOrGetParameter("h_", r.getId());
		hr.setSBOTerm(193);
		hr.setUnits(Unit.Kind.DIMENSIONLESS);
		for (SpeciesReference specRef : r.getListOfReactants()) {
			kM = createOrGetParameter("kM_", r.getId(), underscore, specRef
					.getSpecies());
			kM.setSBOTerm(27);
			kM.setName(concat("Michaelis constant of species ",
					specRef.getSpecies(), " in reaction ", r.getId())
					.toString());
			kM.setUnits(mM());
			exponent = new ASTNode(specRef.getStoichiometry(), this);
			curr = ASTNode
					.pow(new ASTNode(kM, this), exponent.multiplyWith(hr));
			if (root == null)
				root = curr;
			else
				root.multiplyWith(curr);
		}
		if (r.getReversible()) {
			for (SpeciesReference specRef : r.getListOfProducts()) {
				kM = createOrGetParameter("kM_", r.getId(), underscore, specRef
						.getSpecies());
				kM.setSBOTerm(27);
				kM.setUnits(mM());
				kM.setName(concat("Michaelis constant of species ",
						specRef.getSpecies(), " in reaction ", r.getId())
						.toString());
				exponent = new ASTNode(specRef.getStoichiometry(), this);
				curr = ASTNode.pow(new ASTNode(kM, this), exponent
						.multiplyWith(hr));
				if (root == null)
					root = curr;
				else
					root.multiplyWith(curr);
			}
		}
		return root.sqrt();
	}

	/**
	 * Cat version of the numerator
	 * 
	 * @param numEnzymes
	 * 
	 * @param listOfReactants
	 * @param listOfProducts
	 * @return
	 */
	private ASTNode cat(Reaction r, int numEnzymes) {
		ASTNode forward = cat(r, numEnzymes, true);
		if (r.getReversible())
			forward.minus(cat(r, numEnzymes, false));
		return forward;
	}

	/**
	 * This actually creates the rate for forward or backward cat version
	 * equation.
	 * 
	 * @param r
	 * @param numEnzymes
	 * @param forward
	 * @return
	 */
	private ASTNode cat(Reaction r, int numEnzymes, boolean forward) {
		Parameter kr;
		if (numEnzymes > 0) {
			kr = createOrGetParameter("kcat_", forward ? "fwd" : "bwd",
					underscore, r.getId());
			kr.setSBOTerm(forward ? 320 : 321);
			kr
					.setUnits(new Unit(Unit.Kind.SECOND, -1, getLevel(),
							getVersion()));
		} else {
			kr = createOrGetParameter("vmax_", forward ? "fwd" : "bwd",
					underscore, r.getId());
			kr.setSBOTerm(forward ? 324 : 325);
			kr.setUnits(mMperSecond());
		}
		ASTNode rate = new ASTNode(kr, this);
		Parameter hr = createOrGetParameter("h_", r.getId());
		hr.setSBOTerm(193);
		hr.setUnits(Unit.Kind.DIMENSIONLESS);
		for (SpeciesReference specRef : r.getListOfReactants()) {
			Parameter kMrSi = createOrGetParameter("km_", r.getId(),
					underscore, specRef.getSpecies());
			kMrSi.setSBOTerm(27);
			kMrSi.setUnits(mM());
			rate.multiplyWith(ASTNode.pow(ASTNode.frac(this, specRef
					.getSpeciesInstance(), kMrSi), ASTNode.times(new ASTNode(
					specRef.getStoichiometry(), this), new ASTNode(hr, this))));
		}
		return rate;
	}
}
