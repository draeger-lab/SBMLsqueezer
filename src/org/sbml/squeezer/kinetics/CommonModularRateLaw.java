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

import org.sbml.jsbml.ASTNode;
import org.sbml.jsbml.ListOf;
import org.sbml.jsbml.LocalParameter;
import org.sbml.jsbml.Reaction;
import org.sbml.jsbml.SpeciesReference;
import org.sbml.squeezer.RateLawNotApplicableException;

/**
 * This class creates the common modular rate law (CM) according to
 * Liebermeister et al. 2010.
 * 
 * @author Andreas Dr&auml;ger
 * @date 2009-09-21
 * @since 1.3
 * @version $Rev$
 */
public class CommonModularRateLaw extends PowerLawModularRateLaw implements
		InterfaceUniUniKinetics, InterfaceBiUniKinetics, InterfaceBiBiKinetics,
		InterfaceArbitraryEnzymeKinetics, InterfaceReversibleKinetics,
		InterfaceModulatedKinetics {

	/**
	 * Generated serial version identifier.
	 */
	private static final long serialVersionUID = -2058956774794592395L;

	/**
	 * 
	 * @param parentReaction
	 * @param types
	 * @throws RateLawNotApplicableException
	 */
	public CommonModularRateLaw(Reaction parentReaction, Object... types)
			throws RateLawNotApplicableException {
		super(parentReaction, types);
		setSBOTerm(528); // common modular rate law
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.sbml.squeezer.kinetics.ReversiblePowerLaw#denominator(org.sbml.jsbml
	 * .Reaction)
	 */
	ASTNode denominator(String enzyme) {
		ASTNode denominator = denominator(enzyme, true);
		if (denominator.isUnknown())
			denominator = denominator(enzyme, false);
		else
			denominator.plus(denominator(enzyme, false));
		denominator.minus(new ASTNode(1, this));
		ASTNode competInhib = specificModificationSummand(enzyme);
		return competInhib == null ? denominator : denominator
				.plus(competInhib);
	}

	/**
	 * This actually creates the denominator parts.
	 * 
	 * @param forward
	 *            true is forward, false reverse.
	 * @return
	 */
	private final ASTNode denominator(String enzyme, boolean forward) {
		ASTNode denominator = new ASTNode(this), curr;
		LocalParameter hr = parameterFactory
				.parameterReactionCooperativity(enzyme);
		Reaction r = getParentSBMLObject();
		ListOf<SpeciesReference> listOf = forward ? r.getListOfReactants() : r
				.getListOfProducts();
		for (SpeciesReference specRef : listOf) {
			LocalParameter kM = parameterFactory.parameterMichaelis(specRef
					.getSpecies(), enzyme, forward);
			curr = ASTNode.sum(new ASTNode(1, this), ASTNode.frac(
					speciesTerm(specRef), new ASTNode(kM, this)));
			curr.raiseByThePowerOf(ASTNode.times(new ASTNode(specRef
					.getStoichiometry(), this), new ASTNode(hr, this)));
			if (denominator.isUnknown()) {
				denominator = curr;
			} else {
				denominator.multiplyWith(curr);
			}
		}
		return denominator;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.squeezer.kinetics.ReversiblePowerLaw#getSimpleName()
	 */
	public String getSimpleName() {
		return "Common modular rate law (CM)";
	}
}
