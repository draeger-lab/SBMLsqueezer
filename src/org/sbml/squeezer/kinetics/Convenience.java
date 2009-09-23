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
import java.util.List;

import org.sbml.jsbml.ASTNode;
import org.sbml.jsbml.Parameter;
import org.sbml.jsbml.Reaction;
import org.sbml.jsbml.SpeciesReference;
import org.sbml.squeezer.RateLawNotApplicableException;

/**
 * <p>
 * This is the standard convenience kinetics which is only appropriated for
 * systems whose stochiometric matrix has full column rank. Otherwise the more
 * complicated thermodynamically independend form {@link ConvenienceIndependent}
 * needs to be invoked.
 * </p>
 * <p>
 * Creates a convenience kinetic equation. For each enzyme contained in the
 * given reaction, the convenience kinetic term is formed by calling methods
 * which return the numerator and denominator of the fraction. These methods
 * distinguish between reversible and irreversible reactions. The fractions are
 * multiplied with the concentration of the respective enzyme and, afterwards,
 * summed up. The whole sum is multiplied with the activators and inhibitors of
 * the reaction.
 * </p>
 * 
 * @since 1.0
 * @version
 * @author <a href="mailto:Nadine.hassis@gmail.com">Nadine Hassis</a>
 * @author <a href="mailto:andreas.draeger@uni-tuebingen.de">Andreas
 *         Dr&auml;ger</a>
 * @author <a href="mailto:michael@diegrauezelle.de">Michael Ziller</a>
 * @author <a href="mailto:hannes.borch@googlemail.com">Hannes Borch</a>
 * @author <a href="mailto:dwouamba@yahoo.fr">Dieudonn&eacute; Motsou
 *         Wouamba</a>
 * @date Aug 1, 2007
 */
public class Convenience extends GeneralizedMassAction implements
		UniUniKinetics, BiUniKinetics, ArbitraryEnzymeKinetics,
		ReversibleKinetics, IrreversibleKinetics {

	public static boolean isApplicable(Reaction reaction) {
		// TODO
		return true;
	}

	/**
	 * 
	 * @param parentReaction
	 * @throws RateLawNotApplicableException
	 * @throws IllegalFormatException
	 */
	public Convenience(Reaction parentReaction)
			throws RateLawNotApplicableException, IllegalFormatException {
		super(parentReaction);
	}

	// @Override
	public String getName() {
		if (getParentSBMLObject().getReversible())
			return "reversible simple convenience kinetics";
		return "irreversible simple convenience kinetics";
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
		setSBOTerm(429);
		Reaction reaction = getParentSBMLObject();

		if (reaction.getReversible())
			setNotes("reversible simple convenience kinetics");
		else
			setNotes("irreversible simple convenience kinetics");

		ASTNode numerator;
		ASTNode formula[] = new ASTNode[Math.max(1, modE.size())];

		int enzymeNum = 0;
		do {
			StringBuffer kcatp = modE.size() == 0 ? concat("Vp_",
					getParentSBMLObject().getId()) : concat("kcatp_",
					getParentSBMLObject().getId());
			if (modE.size() > 1)
				kcatp = append(kcatp, underscore, modE.get(enzymeNum));
			Parameter p_kcatp = createOrGetParameter(kcatp.toString());
			p_kcatp.setSBOTerm(modE.size() == 0 ? 324 : 320);
			numerator = new ASTNode(p_kcatp, this);

			// sums for each reactant
			for (int eductNum = 0; eductNum < reaction.getNumReactants(); eductNum++) {

				SpeciesReference specref = reaction.getReactant(eductNum);

				StringBuffer kM = concat("kM_", getParentSBMLObject().getId());

				if (modE.size() > 1)
					kM = concat(kM, underscore, modE.get(enzymeNum));
				kM = append(kM, underscore, specref.getSpecies());
				Parameter p_kM = createOrGetParameter(kM.toString());
				p_kM.setSBOTerm(322);

				if (!reaction.getReversible()
						|| ((reaction.getNumReactants() != 1) || (reaction
								.getNumProducts() == 1)))

					// build numerator
					if (specref.getStoichiometry() == 1d)
						numerator.multiplyWith(ASTNode.frac(new ASTNode(specref
								.getSpeciesInstance(), this), new ASTNode(p_kM,
								this)));
					else
						numerator.multiplyWith(ASTNode.pow(ASTNode
								.frac(new ASTNode(specref.getSpeciesInstance(),
										this), new ASTNode(p_kM, this)),
								specref.getStoichiometry()));
			}

			/*
			 * only if reaction is reversible or we want it to be.
			 */
			if (reaction.getReversible()) {
				StringBuffer kcat = modE.size() == 0 ? concat("Vn_",
						getParentSBMLObject().getId()) : concat("kcatn_",
						getParentSBMLObject().getId());
				if (modE.size() > 1)
					kcat = concat(kcat, underscore, modE.get(enzymeNum));
				Parameter p_kcat = createOrGetParameter(kcat.toString());
				p_kcat.setSBOTerm(modE.size() == 0 ? 325 : 321);
				ASTNode numerator2 = new ASTNode(p_kcat, this);

				// Sums for each product

				for (int productNum = 0; productNum < reaction.getNumProducts(); productNum++) {

					StringBuffer kM = concat("kM_", getParentSBMLObject()
							.getId());

					if (modE.size() > 1)
						kM = append(kM, underscore, modE.get(enzymeNum));
					kM = append(kM, underscore, reaction.getProduct(productNum)
							.getSpecies());
					Parameter p_kM = createOrGetParameter(kM.toString());
					p_kM.setSBOTerm(323);

					SpeciesReference specRefP = reaction.getProduct(productNum);

					// build numerator
					if (specRefP.getStoichiometry() != 1.0)
						numerator2 = ASTNode
								.times(numerator2, ASTNode.pow(ASTNode.frac(
										this, specRefP.getSpeciesInstance(),
										p_kM), new ASTNode(specRefP
										.getStoichiometry(), this)));
					else
						numerator2.multiplyWith(ASTNode.frac(this, specRefP
								.getSpeciesInstance(), p_kM));
				}
				numerator.minus(numerator2);
			}
			ASTNode denominator = null;
			String enzyme = null;
			if (modE.size() > 0)
				enzyme = modE.get(enzymeNum);
			if (!reaction.getReversible())
				denominator = ASTNode.times(denominatorElements(enzyme, true)); // Forward
			else {
				denominator = ASTNode.sum(ASTNode.times(denominatorElements(
						enzyme, true)), ASTNode.times(denominatorElements(
						enzyme, false)));
				if (reaction.getNumProducts() > 1
						&& reaction.getNumReactants() > 1)
					denominator.minus(new ASTNode(1, this));
			}
			formula[enzymeNum] = denominator != null ? ASTNode.frac(numerator,
					denominator) : numerator;

			if (modE.size() > 0)
				formula[enzymeNum] = ASTNode.times(new ASTNode(modE
						.get(enzymeNum), this), formula[enzymeNum]);
			enzymeNum++;
		} while (enzymeNum < modE.size());

		return ASTNode.times(activationFactor(modActi),
				inhibitionFactor(modInhib), ASTNode.sum(formula));
	}

	/**
	 * Returns an array containing the factors of the reactants and products
	 * included in the convenience kinetic's denominator. For each factor, the
	 * respective species' concentration and it's equilibrium constant are
	 * divided and raised to the power of each integer value between zero and
	 * the species' stoichiometry. All of the species' powers are summed up to
	 * form the species' factor in the product. The method is applicable for
	 * both forward and backward reactions.
	 * 
	 * @param reaction
	 * @param type
	 *            true means forward, false backward.
	 * @return
	 */
	ASTNode[] denominatorElements(String enzyme, boolean type) {
		Reaction reaction = getParentSBMLObject();
		ASTNode[] denoms = new ASTNode[type ? reaction.getNumReactants()
				: reaction.getNumProducts()];
		boolean noOne = (denoms.length == 1)
				&& (!type || (type && reaction.getReversible() && reaction
						.getNumProducts() > 1));
		for (int i = 0; i < denoms.length; i++) {
			SpeciesReference ref = type ? reaction.getReactant(i) : reaction
					.getProduct(i);
			StringBuffer kM = concat("kM_", getParentSBMLObject().getId());
			if (enzyme != null)
				append(kM, underscore, enzyme);
			append(kM, underscore, ref.getSpecies());
			Parameter p_kM = createOrGetParameter(kM.toString());
			if (!p_kM.isSetSBOTerm())
				p_kM.setSBOTerm(type ? 322 : 323);
			denoms[i] = ASTNode.pow(ASTNode.frac(this,
					ref.getSpeciesInstance(), p_kM), (int) ref
					.getStoichiometry());
			for (int j = (int) ref.getStoichiometry() - 1; j >= (noOne ? 1 : 0); j--) {
				denoms[i] = ASTNode.sum(ASTNode.pow(ASTNode.frac(this, ref
						.getSpeciesInstance(), p_kM), j), denoms[i]);
			}
		}
		return denoms;
	}
}
