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

import org.sbml.jsbml.ASTNode;
import org.sbml.jsbml.ListOf;
import org.sbml.jsbml.Parameter;
import org.sbml.jsbml.Reaction;
import org.sbml.jsbml.SpeciesReference;
import org.sbml.squeezer.RateLawNotApplicableException;

/**
 * This class creates the mulitiplicative saturable kinetic equation according
 * to Liebermeister et al. 2009.
 * 
 * @author Andreas Dr&auml;ger <a
 *         href="mailto:andreas.draeger@uni-tuebingen.de">
 *         andreas.draeger@uni-tuebingen.de</a>
 * @date 2009-09-21
 * @since 1.3
 */
public class SimultaneousBindingModularRateLaw extends PowerLawModularRateLaw implements
		InterfaceUniUniKinetics, InterfaceBiUniKinetics, InterfaceBiBiKinetics,
		InterfaceArbitraryEnzymeKinetics, InterfaceReversibleKinetics,
		InterfaceModulatedKinetics {

	/**
	 * 
	 * @param parentReaction
	 * @param types
	 * @throws RateLawNotApplicableException
	 */
	public SimultaneousBindingModularRateLaw(Reaction parentReaction, Object... types)
			throws RateLawNotApplicableException {
		super(parentReaction, types);
		unsetSBOTerm();
		setNotes("multiplicative saturable rate law");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.sbml.squeezer.kinetics.ReversiblePowerLaw#denominator(java.lang.String
	 * )
	 */
	ASTNode denominator(String enzyme) {
		ASTNode denominator = new ASTNode(this);
		ASTNode forward = denominator(enzyme, true);
		ASTNode backward = denominator(enzyme, false);
		if (!forward.isUnknown())
			denominator = forward;
		if (!backward.isUnknown()) {
			if (!denominator.isUnknown())
				denominator.multiplyWith(backward);
			else
				denominator = backward;
		}
		ASTNode competInhib = specificModificationSummand(enzyme);
		return competInhib.isUnknown() ? denominator : denominator
				.plus(competInhib);
	}

	/**
	 * This actually creates the denominator parts.
	 * 
	 * @param enzyme
	 * @param forward
	 *            true means forward, false backward.
	 * @return
	 */
	private final ASTNode denominator(String enzyme, boolean forward) {
		ASTNode term = new ASTNode(this), curr;
		Parameter kM;
		Parameter hr = parameterReactionCooperativity(enzyme);
		Reaction r = getParentSBMLObject();
		ListOf<SpeciesReference> listOf = forward ? r.getListOfReactants() : r
				.getListOfProducts();
		for (SpeciesReference specRef : listOf) {
			kM = parameterMichaelis(specRef.getSpecies(), enzyme, forward);
			curr = new ASTNode(1, this);
			curr.plus(speciesTerm(specRef).divideBy(kM));
			curr.raiseByThePowerOf(ASTNode.times(new ASTNode(specRef
					.getStoichiometry(), this), new ASTNode(hr, this)));
			if (term.isUnknown())
				term = curr;
			else
				term.multiplyWith(curr);
		}
		return term;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.squeezer.kinetics.ReversiblePowerLaw#getSimpleName()
	 */
	public String getSimpleName() {
		return "Simultaneous binding modular rate law";
	}
}
