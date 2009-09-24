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

import org.sbml.jsbml.ASTNode;
import org.sbml.jsbml.ListOf;
import org.sbml.jsbml.Parameter;
import org.sbml.jsbml.Reaction;
import org.sbml.jsbml.SpeciesReference;
import org.sbml.jsbml.Unit;
import org.sbml.squeezer.RateLawNotApplicableException;

/**
 * This class creates the common saturable rate law according to Liebermeister
 * et al. 2009.
 * 
 * @author Andreas Dr&auml;ger <a
 *         href="mailto:andreas.draeger@uni-tuebingen.de">
 *         andreas.draeger@uni-tuebingen.de</a>
 * @date 2009-09-21
 */
public class CommonSaturable extends ReversiblePowerLaw implements
		InterfaceUniUniKinetics, InterfaceBiUniKinetics, InterfaceBiBiKinetics,
		InterfaceArbitraryEnzymeKinetics, InterfaceReversibleKinetics,
		InterfaceModulatedKinetics {

	
	/**
	 * @param parentReaction
	 * @param type
	 * @throws RateLawNotApplicableException
	 * @throws IllegalFormatException
	 */
	public CommonSaturable(Reaction parentReaction, Object... types)
			throws RateLawNotApplicableException, IllegalFormatException {
		super(parentReaction, types);
		unsetSBOTerm();
		setNotes("common saturable rate law");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.sbml.squeezer.kinetics.ReversiblePowerLaw#denominator(org.sbml.jsbml
	 * .Reaction)
	 */
	ASTNode denominator(Reaction r) {
		ASTNode denominator = denominator(r, true);
		if (denominator.isUnknown())
			denominator = denominator(r, false);
		else
			denominator.minus(denominator(r, false));
		denominator.minus(new ASTNode(1, this));
		ASTNode competInhib = competetiveInhibitionSummand(r);
		return competInhib.isUnknown() ? denominator : denominator
				.plus(competInhib);
	}

	/**
	 * This actually creates the denominator parts.
	 * 
	 * @param r
	 * @param forwardOrReverse
	 *            true is forward, false reverse.
	 * @return
	 */
	private ASTNode denominator(Reaction r, boolean forwardOrReverse) {
		ASTNode denominator = new ASTNode(this), curr;
		Parameter kM;
		Parameter hr = createOrGetParameter("h_", r.getId());
		hr.setSBOTerm(193);
		hr.setUnits(Unit.Kind.DIMENSIONLESS);
		ListOf<SpeciesReference> listOf = forwardOrReverse ? r
				.getListOfReactants() : r.getListOfProducts();
		for (SpeciesReference specRef : listOf) {
			kM = createOrGetParameter("km_", r.getId(), underscore, specRef
					.getSpecies());
			kM.setSBOTerm(27);
			kM.setUnits(mM());
			curr = ASTNode.sum(new ASTNode(1, this), ASTNode.frac(this, specRef
					.getSpeciesInstance(), kM));
			curr.raiseByThePowerOf(ASTNode.times(new ASTNode(specRef
					.getStoichiometry(), this), new ASTNode(hr, this)));
			if (denominator.isUnknown())
				denominator = curr;
			else
				denominator.multiplyWith(curr);
		}
		return denominator;
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.sbml.squeezer.kinetics.ReversiblePowerLaw#getSimpleName()
	 */
	public String getSimpleName() {
		return "Common saturable rate law";
	}
}
