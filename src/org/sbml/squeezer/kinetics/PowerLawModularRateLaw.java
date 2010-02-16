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

import java.util.List;

import org.sbml.jsbml.ASTNode;
import org.sbml.jsbml.ModifierSpeciesReference;
import org.sbml.jsbml.Parameter;
import org.sbml.jsbml.Reaction;
import org.sbml.jsbml.SBO;
import org.sbml.jsbml.SpeciesReference;
import org.sbml.squeezer.RateLawNotApplicableException;

/**
 * Represents the reversible power law (RP) from Liebermeister et al.
 * 
 * @author Andreas Dr&auml;ger <a
 *         href="mailto:andreas.draeger@uni-tuebingen.de">
 *         andreas.draeger@uni-tuebingen.de</a>
 * @since 1.3
 * @date 2009-09-17
 */
public class PowerLawModularRateLaw extends BasicKineticLaw implements
		InterfaceUniUniKinetics, InterfaceBiUniKinetics, InterfaceBiBiKinetics,
		InterfaceArbitraryEnzymeKinetics, InterfaceReversibleKinetics,
		InterfaceModulatedKinetics {

	/**
	 * 
	 * @param parentReaction
	 * @param types
	 * @throws RateLawNotApplicableException
	 */
	public PowerLawModularRateLaw(Reaction parentReaction, Object... types)
			throws RateLawNotApplicableException {
		super(parentReaction, types);
		setSBOTerm(42);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.sbml.squeezer.kinetics.BasicKineticLaw#createKineticEquation(java
	 * .util.List, java.util.List, java.util.List, java.util.List)
	 */
	// @Override
	ASTNode createKineticEquation(List<String> modE, List<String> modActi,
			List<String> modInhib, List<String> modCat)
			throws RateLawNotApplicableException {
		int i;
		Reaction r = getParentSBMLObject();
		unsetSBOTerm();
		ASTNode activation = null, inhibition = null;
		for (String acti : modActi) {
			ModifierSpeciesReference msr = null;
			for (i = 0; i < r.getNumModifiers() && msr == null; i++)
				if (r.getModifier(i).getSpecies().equals(acti))
					msr = r.getModifier(i);
			Parameter kA = parameterKa(msr.getSpecies());
			ASTNode curr = ASTNode
					.frac(speciesTerm(msr), new ASTNode(kA, this));
			curr.divideBy(ASTNode.sum(new ASTNode(1, this), curr.clone()));
			if (SBO.isEssentialActivator(msr.getSBOTerm())) {
				if (activation == null)
					activation = curr;
				else
					activation.multiplyWith(curr);
			} else /* if (SBO.isNonEssentialActivator(msr.getSBOTerm())) */{
				Parameter rhoA = parameterRhoActivation(msr.getSpecies());
				curr = ASTNode.sum(new ASTNode(rhoA, this), ASTNode.times(
						ASTNode.diff(new ASTNode(1, this), new ASTNode(rhoA,
								this)), curr));
				if (activation == null)
					activation = curr;
				else
					activation.multiplyWith(curr);
			}
		}
		for (ModifierSpeciesReference msr : r.getListOfModifiers())
			if (SBO.isInhibitor(msr.getSBOTerm())) {
				ASTNode curr = new ASTNode(this);
				if (SBO.isNonCompetetiveInhibitor(msr.getSBOTerm())) {
					Parameter kI = parameterKi(r.getId(), msr.getSpecies());
					curr = ASTNode.frac(new ASTNode(1, this), ASTNode.sum(
							new ASTNode(1, this), ASTNode.frac(
									speciesTerm(msr), new ASTNode(kI, this))));
				} else if (!SBO.isCompetetiveInhibitor(msr.getSBOTerm())) {
					// partial non-competetive inhibition
					Parameter rhoI = parameterRhoInhibition(msr.getSpecies());
					Parameter kI = parameterKi(msr.getSpecies());
					curr = new ASTNode(rhoI, this);
					curr.plus(ASTNode.frac(ASTNode.diff(new ASTNode(1, this),
							new ASTNode(rhoI, this)), ASTNode.sum(new ASTNode(
							1, this), ASTNode.frac(this, msr
							.getSpeciesInstance(), kI))));
				}
				if (!curr.isUnknown()) {
					if (inhibition == null)
						inhibition = curr;
					else
						inhibition.multiplyWith(curr);
				}
			}
		ASTNode numerator[] = new ASTNode[Math.max(1, modE.size())];
		for (i = 0; i < numerator.length; i++) {
			String enzymeID = modE.size() <= i ? null : modE.get(i);
			switch (type) {
			case 0: // CAT
				numerator[i] = cat(enzymeID);
				break;
			case 1: // HAL
				numerator[i] = hal(enzymeID);
				break;
			default: // WEG
				numerator[i] = weg(enzymeID);
				break;
			}
			numerator[i].divideBy(denominator(enzymeID));
			if (i < modE.size()) {
				ModifierSpeciesReference enzyme = null;
				for (int j = 0; j < r.getNumModifiers() && enzyme == null; j++)
					if (r.getModifier(j).getSpecies().equals(modE.get(j)))
						enzyme = r.getModifier(j);
				numerator[i] = ASTNode.times(speciesTerm(enzyme), numerator[i]);
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
	ASTNode denominator(String enzyme) {
		ASTNode denominator = new ASTNode(1, this);
		ASTNode competInhib = competetiveInhibitionSummand(enzyme);
		return competInhib.isUnknown() ? denominator : denominator
				.plus(competInhib);
	}

	/**
	 * Creates the summand for competetive inhibition in the denominator.
	 * 
	 * @param enzyme
	 * 
	 * @param r
	 * @return
	 */
	ASTNode competetiveInhibitionSummand(String enzyme) {
		Reaction r = getParentSBMLObject();
		ASTNode inhib = new ASTNode(this);
		for (ModifierSpeciesReference msr : r.getListOfModifiers())
			if (SBO.isCompetetiveInhibitor(msr.getSBOTerm())) {
				Parameter kI = parameterKi(msr.getSpecies(), enzyme);
				ASTNode curr = ASTNode.frac(speciesTerm(msr), new ASTNode(kI,
						this));
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
	 * @param enzyme
	 * 
	 * @param listOfReactants
	 * @param listOfProducts
	 * @return
	 */
	private ASTNode weg(String enzyme) {
		ASTNode numerator = new ASTNode(parameterVelocityConstant(enzyme), this);
		Parameter R = parameterGasConstant();
		Parameter T = parameterTemperature();
		ASTNode exponent = null;
		ASTNode forward = null, backward = null;
		Parameter mu;
		Parameter hr = parameterReactionCooperativity(enzyme);
		Reaction r = getParentSBMLObject();
		for (SpeciesReference specRef : r.getListOfReactants()) {
			ASTNode curr = speciesTerm(specRef);
			curr.raiseByThePowerOf(ASTNode.times(new ASTNode(specRef
					.getStoichiometry(), this), new ASTNode(hr, this)));
			if (forward == null)
				forward = curr;
			else
				forward.multiplyWith(curr);
			mu = parameterStandardChemicalPotential(specRef.getSpecies());
			ASTNode product = ASTNode.times(new ASTNode(specRef
					.getStoichiometry(), this), new ASTNode(mu, this));
			if (exponent == null)
				exponent = product;
			else
				exponent.multiplyWith(product);
		}
		for (SpeciesReference specRef : r.getListOfProducts()) {
			if (r.getReversible()) {
				ASTNode curr = speciesTerm(specRef);
				curr.raiseByThePowerOf(ASTNode.times(new ASTNode(specRef
						.getStoichiometry(), this), new ASTNode(hr, this)));
				if (backward == null)
					backward = curr;
				else
					backward.multiplyWith(curr);
			}
			mu = parameterStandardChemicalPotential(specRef.getSpecies());
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
		return numerator.multiplyWith(forward).divideBy(createRoot(enzyme));
	}

	/**
	 * Hal version of the numerator
	 * 
	 * @param enzyme
	 * 
	 * @param listOfReactants
	 * @param listOfProducts
	 * @return
	 */
	private ASTNode hal(String enzyme) {
		ASTNode numerator = new ASTNode(parameterVelocityConstant(enzyme), this);
		Parameter hr = parameterReactionCooperativity(enzyme);
		Parameter keq = parameterEquilibriumConstant();
		ASTNode forward = ASTNode.sqrt(ASTNode.pow(new ASTNode(keq, this),
				new ASTNode(hr, this)));
		Reaction r = getParentSBMLObject();
		for (SpeciesReference specRef : r.getListOfReactants())
			forward.multiplyWith(ASTNode.pow(speciesTerm(specRef), ASTNode
					.times(new ASTNode(specRef.getStoichiometry(), this),
							new ASTNode(hr, this))));
		numerator.multiplyWith(forward);
		if (r.getReversible()) {
			ASTNode backward = ASTNode.frac(1, ASTNode.sqrt(ASTNode.pow(this,
					keq, hr)));
			for (SpeciesReference specRef : r.getListOfProducts())
				backward.multiplyWith(ASTNode.pow(speciesTerm(specRef), ASTNode
						.times(new ASTNode(specRef.getStoichiometry(), this),
								new ASTNode(hr, this))));
			numerator.minus(backward);
		}
		return numerator.divideBy(createRoot(enzyme));
	}

	/**
	 * Creates the root term for hal and weg.
	 * 
	 * @param enzyme
	 * @return
	 */
	private ASTNode createRoot(String enzyme) {
		ASTNode root = null, curr = null;
		Parameter kM = null;
		Parameter hr = parameterReactionCooperativity(enzyme);
		Reaction r = getParentSBMLObject();
		for (SpeciesReference specRef : r.getListOfReactants()) {
			kM = parameterMichaelis(specRef.getSpecies(), enzyme, true);
			curr = ASTNode.pow(new ASTNode(kM, this), ASTNode.times(
					new ASTNode(specRef.getStoichiometry(), this), new ASTNode(
							hr, this)));
			if (root == null)
				root = curr;
			else
				root.multiplyWith(curr);
		}
		if (r.getReversible()) {
			for (SpeciesReference specRef : r.getListOfProducts()) {
				kM = parameterMichaelis(specRef.getSpecies(), enzyme, false);
				curr = ASTNode.pow(new ASTNode(kM, this), ASTNode.times(
						new ASTNode(specRef.getStoichiometry(), this),
						new ASTNode(hr, this)));
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
	 * @param enzyme
	 * 
	 * @param listOfReactants
	 * @param listOfProducts
	 * @return
	 */
	private ASTNode cat(String enzyme) {
		ASTNode forward = cat(enzyme, true);
		if (getParentSBMLObject().getReversible())
			forward.minus(cat(enzyme, false));
		return forward;
	}

	/**
	 * This actually creates the rate for forward or backward cat version
	 * equation.
	 * 
	 * @param enzyme
	 * @param forward
	 * @return
	 */
	private ASTNode cat(String enzyme, boolean forward) {
		Parameter kr = parameterKcatOrVmax(enzyme, forward);
		ASTNode rate = new ASTNode(kr, this);
		Reaction r = getParentSBMLObject();
		Parameter hr = parameterReactionCooperativity(enzyme);
		for (SpeciesReference specRef : r.getListOfReactants()) {
			Parameter kM = parameterMichaelis(specRef.getSpecies(), enzyme,
					forward);
			rate.multiplyWith(ASTNode.pow(ASTNode.frac(speciesTerm(specRef),
					new ASTNode(kM, this)), ASTNode.times(new ASTNode(specRef
					.getStoichiometry(), this), new ASTNode(hr, this))));
		}
		return rate;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.squeezer.kinetics.BasicKineticLaw#getSimpleName()
	 */
	// @Override
	public String getSimpleName() {
		return "Power-law modular rate law";
	}
}
