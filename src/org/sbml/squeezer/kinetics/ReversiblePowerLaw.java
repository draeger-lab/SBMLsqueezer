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
public class ReversiblePowerLaw extends BasicKineticLaw implements
		InterfaceUniUniKinetics, InterfaceBiUniKinetics, InterfaceBiBiKinetics,
		InterfaceArbitraryEnzymeKinetics, InterfaceReversibleKinetics,
		InterfaceModulatedKinetics {

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
	public ReversiblePowerLaw(Reaction parentReaction, Object... types)
			throws RateLawNotApplicableException, IllegalFormatException {
		super(parentReaction, types);
		setSBOTerm(42);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.squeezer.kinetics.BasicKineticLaw#createKineticEquation(java
	 *      .util.List, java.util.List, java.util.List, java.util.List,
	 *      java.util.List, java.util.List)
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
			Parameter kA = parameterKa(r.getId(), msr.getSpecies());
			ASTNode curr = ASTNode
					.frac(speciesTerm(msr), new ASTNode(kA, this));
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
				Parameter kI = parameterKi(r.getId(), msr.getSpecies());
				ASTNode curr = ASTNode.frac(new ASTNode(1, this), ASTNode.sum(
						new ASTNode(1, this), ASTNode.frac(speciesTerm(msr),
								new ASTNode(kI, this))));
				if (inhibition == null)
					inhibition = curr;
				else
					inhibition.multiplyWith(curr);
			}
		}
		type = Short.parseShort(getTypeParameters()[0].toString());
		ASTNode numerator[] = new ASTNode[Math.max(1, modE.size())];
		for (i = 0; i < numerator.length; i++) {
			String enzymeID = modE.size() <= i ? null : modE.get(i);
			switch (type) {
			case 0: // CAT
				numerator[i] = cat(r, enzymeID);
				break;
			case 1: // HAL
				numerator[i] = hal(r, enzymeID);
				break;
			default: // WEG
				numerator[i] = weg(r, enzymeID);
				break;
			}
			numerator[i].divideBy(denominator(r, enzymeID));
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
	ASTNode denominator(Reaction r, String enzyme) {
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
				Parameter kI = parameterKi(r.getId(), msr.getSpecies());
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
	private ASTNode weg(Reaction r, String enzyme) {
		ASTNode numerator = new ASTNode(parameterVelocityConstant(r.getId(),
				enzyme), this);
		Parameter R = parameterGasConstant();
		Parameter T = parameterTemperature();
		ASTNode exponent = null;
		ASTNode forward = null, backward = null;
		Parameter mu;
		Parameter hr = parameterHillCoefficient(r.getId(), enzyme);
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
		return numerator.multiplyWith(forward).divideBy(createRoot(r, enzyme));
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
	private ASTNode hal(Reaction r, String enzyme) {
		ASTNode numerator = new ASTNode(parameterVelocityConstant(r.getId(),
				enzyme), this);
		Parameter hr = parameterHillCoefficient(r.getId(), enzyme);
		Parameter keq = parameterEquilibriumConstant(r.getId());
		ASTNode forward = ASTNode.sqrt(ASTNode.pow(new ASTNode(keq, this),
				new ASTNode(hr, this)));
		double x = 0;
		for (SpeciesReference specRef : r.getListOfReactants()) {
			forward.multiplyWith(ASTNode.pow(speciesTerm(specRef), ASTNode
					.times(new ASTNode(specRef.getStoichiometry(), this),
							new ASTNode(hr, this))));
			x += specRef.getStoichiometry();
		}
		numerator.multiplyWith(forward);
		if (r.getReversible()) {
			ASTNode backward = ASTNode.frac(1, ASTNode.sqrt(ASTNode.pow(this,
					keq, hr)));
			for (SpeciesReference specRef : r.getListOfProducts()) {
				backward.multiplyWith(ASTNode.pow(speciesTerm(specRef), ASTNode
						.times(new ASTNode(specRef.getStoichiometry(), this),
								new ASTNode(hr, this))));
				x -= specRef.getStoichiometry();
			}
			numerator.minus(backward);
		}
		keq.setUnits(unitmM((int) x));
		return numerator.divideBy(createRoot(r, enzyme));
	}

	/**
	 * Creates the root term for hal and weg.
	 * 
	 * @param r
	 * @return
	 */
	private ASTNode createRoot(Reaction r, String enzyme) {
		ASTNode root = null, curr = null;
		Parameter kM = null;
		Parameter hr = parameterHillCoefficient(r.getId(), enzyme);
		for (SpeciesReference specRef : r.getListOfReactants()) {
			kM = parameterMichaelisSubstrate(r.getId(), specRef.getSpecies(),
					enzyme);
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
				kM = parameterMichaelisProduct(r.getId(), specRef.getSpecies(),
						enzyme);
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
	private ASTNode cat(Reaction r, String enzyme) {
		ASTNode forward = cat(r, enzyme, true);
		if (r.getReversible())
			forward.minus(cat(r, enzyme, false));
		return forward;
	}

	/**
	 * This actually creates the rate for forward or backward cat version
	 * equation.
	 * 
	 * @param r
	 * @param enzyme
	 * @param forward
	 * @return
	 */
	private ASTNode cat(Reaction r, String enzyme, boolean forward) {
		Parameter kr = parameterKcatOrVmax(r.getId(), enzyme, forward);
		ASTNode rate = new ASTNode(kr, this);
		Parameter hr = parameterHillCoefficient(r.getId(), enzyme);
		for (SpeciesReference specRef : r.getListOfReactants()) {
			Parameter kM = forward ? parameterMichaelisSubstrate(r.getId(),
					specRef.getSpecies(), enzyme) : parameterMichaelisProduct(r
					.getId(), specRef.getSpecies(), enzyme);
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
		return "Reversible power law";
	}
}
