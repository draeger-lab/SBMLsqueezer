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

import java.util.List;

import org.sbml.jsbml.ASTNode;
import org.sbml.jsbml.Parameter;
import org.sbml.jsbml.Reaction;
import org.sbml.squeezer.RateLawNotApplicableException;

/**
 * Irreversible non-exclusive non-cooperative competitive inihibition, a special
 * case of the {@link KineticLaw}s defined by {@link SBO} term identifier 199,
 * 29, 267, or 273 depending on the structure of the reaction.
 * 
 * @author <a href="mailto:andreas.draeger@uni-tuebingen.de">Andreas
 *         Dr&auml;ger</a>
 * @since 1.0
 */
public class IrrevCompetNonCooperativeEnzymes extends GeneralizedMassAction
		implements InterfaceIrreversibleKinetics, InterfaceModulatedKinetics,
		InterfaceUniUniKinetics {

	/**
	 * 
	 */
	private int numInhib;

	/**
	 * 
	 */
	private int numOfEnzymes;

	/**
	 * 
	 * @param parentReaction
	 * @param typeParameters
	 * @throws RateLawNotApplicableException
	 */
	public IrrevCompetNonCooperativeEnzymes(Reaction parentReaction,
			Object... typeParameters) throws RateLawNotApplicableException {
		super(parentReaction, typeParameters);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.sbml.squeezer.kinetics.GeneralizedMassAction#createKineticEquation
	 * (java.util.List, java.util.List, java.util.List, java.util.List,
	 * java.util.List, java.util.List)
	 */
	// @Override
	ASTNode createKineticEquation(List<String> modE, List<String> modActi,
			List<String> modTActi, List<String> modInhib,
			List<String> modTInhib, List<String> modCat)
			throws RateLawNotApplicableException {
		if (((modTActi.size() > 0) && (modActi.size() > 0))
				|| ((modInhib.size() > 0) && (modTInhib.size() > 0)))
			throw new RateLawNotApplicableException(
					"Mixture of translational/transcriptional and regular activation/inhibition is not allowed.");
		if ((modCat.size() > 0))
			throw new RateLawNotApplicableException(
					"This rate law can only be applied to enzyme-catalyzed reactions.");
		Reaction reaction = getParentSBMLObject();
		if ((reaction.getNumReactants() > 1)
				|| (reaction.getReactant(0).getStoichiometry() != 1d))
			throw new RateLawNotApplicableException(
					"This rate law can only be applied to reactions with exactly one substrate species.");
		if (modTActi.size() > 0)
			modActi.addAll(modTActi);
		if (modTInhib.size() > 0)
			modInhib.addAll(modTInhib);
		if (reaction.getReversible())
			reaction.setReversible(false);
		numInhib = modInhib.size();
		numOfEnzymes = modE.size();

		switch (numInhib) {
		case 0:
			setSBOTerm(numOfEnzymes == 0 ? 199 : 29);
		case 1:
			setSBOTerm(267);
		default:
			setSBOTerm(273);
		}

		ASTNode[] formula = new ASTNode[Math.max(1, numOfEnzymes)];

		int enzymeNum = 0;
		do {
			String enzyme = null;
			if (numOfEnzymes > 0)
				enzyme = modE.get(enzymeNum);
			Parameter p_kcat = parameterKcatOrVmax(enzyme, true);
			ASTNode currEnzyme;
			ASTNode numerator;

			numerator = new ASTNode(p_kcat, this);
			numerator = ASTNode.times(numerator, speciesTerm(reaction
					.getReactant(0)));

			ASTNode denominator;
			Parameter p_kM = parameterMichaelis(reaction.getReactant(0)
					.getSpecies(), enzyme, true);

			if (modInhib.size() == 0)
				denominator = new ASTNode(p_kM, this);
			else {
				ASTNode factor = new ASTNode(p_kM, this);
				for (int i = 0; i < modInhib.size(); i++) {
					Parameter p_kIi = parameterKi(modInhib.get(i), enzyme);
					Parameter p_exp = parameterNumBindingSites(enzyme, modInhib
							.get(i));
					factor.multiplyWith(ASTNode.pow(ASTNode.sum(new ASTNode(1,
							this), ASTNode.frac(speciesTerm(modInhib.get(i)),
							new ASTNode(p_kIi, this))),
							new ASTNode(p_exp, this)));
				}
				denominator = factor;
			}
			denominator.plus(speciesTerm(reaction.getReactant(0)));
			currEnzyme = ASTNode.frac(numerator, denominator);
			if (numOfEnzymes > 1)
				numerator.multiplyWith(speciesTerm(enzyme));
			formula[enzymeNum++] = currEnzyme;
		} while (enzymeNum < modE.size());
		return ASTNode.times(activationFactor(modActi), ASTNode.sum(formula));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.squeezer.kinetics.GeneralizedMassAction#getSimpleName()
	 */
	public String getSimpleName() {
		return "Irreversible non-exclusive non-cooperative competitive inihibition";
	}
}
