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
package org.sbml.squeezer.kinetics;

import java.util.List;

import org.sbml.jsbml.ASTNode;
import org.sbml.jsbml.LocalParameter;
import org.sbml.jsbml.Reaction;
import org.sbml.jsbml.SBO;
import org.sbml.jsbml.Species;
import org.sbml.squeezer.RateLawNotApplicableException;

/**
 * This is the generalized version of Hill's equation as suggested by
 * Cornish-Bowden (2004, p. 314, &ldquo;Fundamentals of Enzyme Kinetics&rdquo;).
 * 
 * @author Andreas Dr&auml;ger
 * @date 2010-03-25
 * @since 1.3
 * @version $Rev$
 */
public class HillEquation extends BasicKineticLaw implements
		InterfaceUniUniKinetics, InterfaceReversibleKinetics,
		InterfaceIrreversibleKinetics, InterfaceModulatedKinetics,
		InterfaceIntegerStoichiometry {

	/**
	 * Generated serial version identifier.
	 */
	private static final long serialVersionUID = -3319749306692986819L;

	/**
	 * @param parentReaction
	 * @param typeParameters
	 * @throws RateLawNotApplicableException
	 */
	public HillEquation(Reaction parentReaction, Object... typeParameters)
			throws RateLawNotApplicableException {
		super(parentReaction, typeParameters);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.sbml.squeezer.kinetics.BasicKineticLaw#createKineticEquation(java
	 * .util.List, java.util.List, java.util.List, java.util.List)
	 */
	ASTNode createKineticEquation(List<String> enzymes,
			List<String> activators, List<String> inhibitors,
			List<String> nonEnzymeCatalysts)
			throws RateLawNotApplicableException {

		Reaction r = getParentSBMLObject();

		if (activators.isEmpty() && inhibitors.isEmpty() && !r.getReversible())
			setSBOTerm(195);

		ASTNode rates[] = new ASTNode[Math.max(1, enzymes.size())];
		for (int enzymeNum = 0; enzymeNum < rates.length; enzymeNum++) {
			String enzyme = enzymes.size() == 0 ? null : enzymes.get(enzymeNum);
			Species reactant = r.getReactant(0).getSpeciesInstance();
			LocalParameter kSreactant = parameterFactory.parameterKS(reactant,
					enzyme);
			LocalParameter hillCoeff = parameterFactory
					.parameterHillCoefficient(enzyme);

			rates[enzymeNum] = new ASTNode(parameterFactory
					.parameterKcatOrVmax(enzyme, true), this);
			if (enzyme != null) {
				rates[enzymeNum].multiplyWith(speciesTerm(enzyme));
			}

			ASTNode specTerm = speciesTerm(reactant);
			ASTNode denominator = null;

			if (r.getReversible()) {
				Species product = r.getProduct(0).getSpeciesInstance();

				ASTNode prodTerm = new ASTNode(1, this);
				prodTerm.minus(ASTNode.frac(speciesTerm(product), ASTNode
						.times(new ASTNode(parameterFactory
								.parameterEquilibriumConstant(), this),
								speciesTerm(reactant))));

				rates[enzymeNum].multiplyWith(speciesTerm(reactant));
				rates[enzymeNum].divideBy(new ASTNode(kSreactant, this));
				rates[enzymeNum].multiplyWith(prodTerm);

				LocalParameter kSproduct = parameterFactory.parameterKS(
						product, enzyme);
				// S/kS + P/kP
				specTerm = ASTNode
						.frac(specTerm, new ASTNode(kSreactant, this));
				specTerm.plus(ASTNode.frac(speciesTerm(product), new ASTNode(
						kSproduct, this)));
				rates[enzymeNum].multiplyWith(ASTNode.pow(specTerm.clone(),
						(new ASTNode(hillCoeff, this)).minus(new ASTNode(1,
								this))));
			} else {
				denominator = ASTNode.pow(new ASTNode(kSreactant, this),
						new ASTNode(hillCoeff, this));
				rates[enzymeNum].multiplyWith(ASTNode.pow(
						speciesTerm(reactant), new ASTNode(hillCoeff, this)));
			}

			if (activators.size() + inhibitors.size() == 1) {
				Species modifier = getModel().getSpecies(
						activators.isEmpty() ? inhibitors.get(0) : activators
								.get(0));
				LocalParameter kMmodifier = parameterFactory
						.parameterMichaelis(modifier.getId(), enzyme);
				ASTNode kMmPow = ASTNode.pow(new ASTNode(kMmodifier, this),
						new ASTNode(hillCoeff, this));
				ASTNode modPow = ASTNode.pow(speciesTerm(modifier),
						new ASTNode(hillCoeff, this));
				LocalParameter beta = parameterFactory.parameterBeta(r.getId());
				if (SBO.isInhibitor(modifier.getSBOTerm()))
					beta.setValue(beta.getValue() * (-1));
				denominator = ASTNode.frac(kMmPow.clone().plus(modPow.clone()),
						kMmPow.plus(ASTNode.times(new ASTNode(beta, this),
								modPow)));
				if (!r.getReversible())
					denominator.multiplyWith(ASTNode.pow(new ASTNode(
							kSreactant, this), new ASTNode(hillCoeff, this)));
			}

			if (denominator != null)
				denominator.plus(ASTNode.pow(specTerm, new ASTNode(hillCoeff,
						this)));
			else
				denominator = ASTNode.pow(specTerm,
						new ASTNode(hillCoeff, this));

			rates[enzymeNum].divideBy(denominator);
		}
		return ASTNode.sum(rates);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.squeezer.kinetics.BasicKineticLaw#getSimpleName()
	 */
	public String getSimpleName() {
		if (!isSetSBOTerm())
			return "Generalized Hill equation";
		return SBO.getTerm(getSBOTerm()).getDefinition().replace("\\,", ",");
	}

}
