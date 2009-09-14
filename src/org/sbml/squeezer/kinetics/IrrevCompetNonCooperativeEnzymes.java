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

import java.io.IOException;
import java.util.IllegalFormatException;
import java.util.List;

import org.sbml.jsbml.ASTNode;
import org.sbml.jsbml.Parameter;
import org.sbml.jsbml.Reaction;
import org.sbml.squeezer.RateLawNotApplicableException;

/**
 * 
 * @author <a href="mailto:andreas.draeger@uni-tuebingen.de">Andreas
 *         Dr&auml;ger</a>
 * 
 */
public class IrrevCompetNonCooperativeEnzymes extends GeneralizedMassAction {

	private int numInhib;
	private int numOfEnzymes;

	/**
	 * @param parentReaction
	 * @throws RateLawNotApplicableException
	 * @throws IOException
	 * @throws IllegalFormatException
	 */
	public IrrevCompetNonCooperativeEnzymes(Reaction parentReaction)
			throws RateLawNotApplicableException, 
			IllegalFormatException {
		super(parentReaction);
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
			throws RateLawNotApplicableException, IllegalFormatException {
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
					"This rate law can only be applied to reactions with exactly one substrate.");
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
			StringBuffer kcat = new StringBuffer();
			if (numOfEnzymes == 0)
				kcat = concat("V_", reaction.getId());
			else {
				kcat = concat("kcat_", reaction.getId());
				if (modE.size() > 1)
					kcat = concat(kcat, underscore, modE.get(enzymeNum));
			}
			Parameter p_kcat = createOrGetParameter(kcat.toString());
			p_kcat.setSBOTerm(25);
			ASTNode currEnzyme;
			ASTNode numerator;

			numerator = new ASTNode(p_kcat, this);
			numerator = ASTNode.times(numerator, new ASTNode(reaction
					.getReactant(0).getSpeciesInstance(), this));

			ASTNode denominator;
			StringBuffer kM = new StringBuffer(concat("kM_", reaction.getId()));

			if (numOfEnzymes > 1)
				kM = concat(kM, underscore, modE.get(enzymeNum));
			kM = concat(kM, underscore, reaction.getReactant(0).getSpecies());
			Parameter p_kM = createOrGetParameter(kM.toString());
			p_kM.setSBOTerm(27);

			if (modInhib.size() == 0)
				denominator = new ASTNode(p_kM, this);
			else {
				ASTNode factor = new ASTNode(p_kM, this);
				for (int i = 0; i < modInhib.size(); i++) {

					StringBuffer kIi = new StringBuffer(concat("Ki_", reaction
							.getId()));
					StringBuffer exponent = concat("m_", reaction.getId());
					if (numOfEnzymes > 1) {
						kIi = concat(underscore, modE.get(enzymeNum));
						exponent = concat(underscore, modE.get(enzymeNum));
					}
					kIi = concat(kIi, underscore, modInhib.get(i));
					exponent = concat(exponent, underscore, modInhib.get(i));
					Parameter p_kIi = createOrGetParameter(kIi.toString());
					p_kIi.setSBOTerm(261);
					Parameter p_exp = createOrGetParameter(exponent.toString());
					p_exp.setSBOTerm(189);

					factor.multiplyWith(ASTNode.pow(ASTNode.sum(new ASTNode(1,
							this), ASTNode.frac(new ASTNode(modInhib.get(i),
							this), new ASTNode(p_kIi, this))), new ASTNode(
							p_exp, this)));
				}
				denominator = factor;

			}
			denominator.plus(new ASTNode(reaction.getReactant(0)
					.getSpeciesInstance(), this));
			currEnzyme = ASTNode.frac(numerator, denominator);
			enzymeNum++;
			if (numOfEnzymes > 1)
				numerator.multiplyWith(new ASTNode(modE.get(enzymeNum), this));
			formula[enzymeNum] = currEnzyme;
		} while (enzymeNum <= modE.size() - 1);
		return ASTNode.times(activationFactor(modActi), ASTNode.sum(formula));
	}

}
