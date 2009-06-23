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
package org.sbmlsqueezer.kinetics;

import java.io.IOException;
import java.util.List;

import jp.sbi.celldesigner.plugin.PluginModel;
import jp.sbi.celldesigner.plugin.PluginReaction;
import jp.sbi.celldesigner.plugin.PluginSpeciesReference;

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
 * @author <a href="mailto:andreas.draeger@uni-tuebingen.de">Andreas Dr&auml;ger</a>
 * @author <a href="mailto:michael@diegrauezelle.de">Michael Ziller</a>
 * @author <a href="mailto:hannes.borch@googlemail.com">Hannes Borch</a>
 * @author <a href="mailto:dwouamba@yahoo.fr">Dieudonn&eacute; Motsou Wouamba</a>
 * @date Aug 1, 2007
 */
public class Convenience extends GeneralizedMassAction {

	/**
	 * @param parentReaction
	 * @param model
	 * @param reversibility
	 * @throws RateLawNotApplicableException
	 * @throws IOException
	 * @throws IllegalFormatException
	 */
	public Convenience(PluginReaction parentReaction, PluginModel model)
			throws RateLawNotApplicableException, IOException,
			IllegalFormatException {
		super(parentReaction, model);
	}

	/**
	 * @param parentReaction
	 * @param model
	 * @param listOfPossibleEnzymes
	 * @param reversibility
	 * @throws RateLawNotApplicableException
	 * @throws IOException
	 * @throws IllegalFormatException
	 */
	public Convenience(PluginReaction parentReaction, PluginModel model,
			List<String> listOfPossibleEnzymes)
			throws RateLawNotApplicableException, IOException,
			IllegalFormatException {
		super(parentReaction, model, listOfPossibleEnzymes);
	}

	public static boolean isApplicable(PluginReaction reaction) {
		// TODO
		return true;
	}

	// @Override
	public String getName() {
		if (getParentReaction().getReversible())
			return "reversible simple convenience kinetics";
		return "irreversible simple convenience kinetics";
	}

	// @Override
	public String getSBO() {
		return "none";
	}

	/*
	 * (non-Javadoc)
	 * @see org.sbmlsqueezer.kinetics.GeneralizedMassAction#createKineticEquation(jp.sbi.celldesigner.plugin.PluginModel, java.util.List, java.util.List, java.util.List, java.util.List, java.util.List, java.util.List)
	 */
	protected StringBuffer createKineticEquation(PluginModel model,
			List<String> modE, List<String> modActi, List<String> modTActi,
			List<String> modInhib, List<String> modTInhib, List<String> modCat)
			throws RateLawNotApplicableException, IllegalFormatException {
		PluginReaction reaction = getParentReaction();
		StringBuffer catalysts[] = new StringBuffer[Math.max(1, modE.size())];
		try {
			StringBuffer activation = activationFactor(modActi);
			StringBuffer inhibition = inhibitionFactor(modInhib);

			int i = 0;
			do {
				StringBuffer numerator, denominator;
				if (!reaction.getReversible()) {
					numerator = times(createNumerators(i, modE, FORWARD));
					denominator = diff(times(createDenominators(i, modE,
							FORWARD)), new StringBuffer("1"));
				} else {
					numerator = diff(times(createNumerators(i, modE, FORWARD)),
							times(createNumerators(i, modE, REVERSE)));
					denominator = diff(sum(times(createDenominators(i, modE,
							FORWARD)), times(createDenominators(i, modE,
							REVERSE))), new StringBuffer("1"));
				}
				if (modE.size() > 0)
					catalysts[i] = times(new StringBuffer(modE.get(i)), frac(
							numerator, denominator));
				else
					return (times(activation, inhibition, frac(numerator,
							denominator)));
				i++;
			} while (i < catalysts.length);
			System.out.println(times(activation, inhibition, sum(catalysts)));
			return times(activation, inhibition, sum(catalysts));
		} catch (IllegalFormatException exc) {
			exc.printStackTrace();
			return new StringBuffer();
		}
	}

	/**
	 * Returns an array containing the factors of the reactants and products
	 * included in the convenience kinetic's numerator. Each factor is given by
	 * the quotient of the respective species' concentration and it's related
	 * equilibrium constant to the power of the species' stoichiometry. The
	 * array also contains the catalytic constant of the reaction. This method
	 * is applicable for both forward and backward reactions.
	 * 
	 * @param reaction
	 * @param enzymeNumber
	 * @param modE
	 * @param type
	 * @return
	 * @throws IllegalFormatException
	 */
	protected StringBuffer[] createNumerators(int enzymeNumber,
			List<String> modE, boolean type) throws IllegalFormatException {
		PluginReaction reaction = getParentReaction();
		if (type == FORWARD || type == REVERSE) {
			StringBuffer[] nums = new StringBuffer[(type == FORWARD) ? (reaction
					.getNumReactants() + 1)
					: (reaction.getNumProducts() + 1)];
			nums[0] = new StringBuffer((type == FORWARD) ? "kcatp_" : "kcatn_");
			nums[0].append(reaction.getId());
			if (modE.size() > 1) {
				nums[0].append('_');
				nums[0].append(modE.get(enzymeNumber));
			}
			addLocalParameter(nums[0]);

			for (int i = 1; i < nums.length; i++) {
				PluginSpeciesReference ref = (type == FORWARD) ? reaction
						.getReactant(i - 1) : reaction.getProduct(i - 1);
				StringBuffer kM = new StringBuffer("kM_");
				kM.append(reaction.getId());
				if (modE.size() > 1) {
					kM.append('_');
					kM.append(modE.get(enzymeNumber));
				}
				kM.append('_');
				kM.append(ref.getSpecies());
				addLocalParameter(kM);
				nums[i] = pow(frac(getSpecies(ref), kM), getStoichiometry(ref));
			}

			return nums;
		} else {
			throw new IllegalFormatException(
					"Illegal type argument for Convenience kinetic numerators");
		}
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
	 * @param enzymeNumber
	 * @param modE
	 * @param type
	 * @return
	 * @throws IllegalFormatException
	 */
	protected StringBuffer[] createDenominators(int enzymeNumber, List<String> modE, boolean type)
			throws IllegalFormatException {
		PluginReaction reaction = getParentReaction();
		if (type == FORWARD || type == REVERSE) {
			StringBuffer[] denoms = new StringBuffer[(type == FORWARD) ? reaction
					.getNumReactants()
					: reaction.getNumProducts()];
			for (int i = 0; i < denoms.length; i++) {
				PluginSpeciesReference ref = (type == FORWARD) ? reaction
						.getReactant(i) : reaction.getProduct(i);
				StringBuffer kM = new StringBuffer("kM_");
				kM.append(getParentReactionID());
				if (modE.size() > 1) {
					kM.append('_');
					kM.append(modE.get(enzymeNumber));
				}
				kM.append('_');
				kM.append(ref.getSpecies());

				StringBuffer[] parts = new StringBuffer[(int) ref
						.getStoichiometry() + 1];
				StringBuffer part = frac(getSpecies(ref), kM);
				for (int j = 0; j < parts.length; j++) {
					parts[j] = pow(part, new StringBuffer(Integer.toString(j)));
				}
				denoms[i] = sum(parts);
			}
			return denoms;
		} else {
			throw new IllegalFormatException(
					"Illegal type argument for convenience kinetic numerators");
		}
	}

	/**
	 * TODO Method that sums up the single +1 and -1 terms in the denominator.
	 * 
	 * @param sb
	 * @return
	 */
	protected StringBuffer removeOnes(StringBuffer sb) {
		return sb;
	}
}
