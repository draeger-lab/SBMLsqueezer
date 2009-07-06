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
 * @author <a href="mailto:andreas.draeger@uni-tuebingen.de">Andreas
 *         Dr&auml;ger</a>
 * @author <a href="mailto:michael@diegrauezelle.de">Michael Ziller</a>
 * @author <a href="mailto:hannes.borch@googlemail.com">Hannes Borch</a>
 * @author <a href="mailto:dwouamba@yahoo.fr">Dieudonn&eacute; Motsou
 *         Wouamba</a>
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

	protected StringBuffer createKineticEquation(PluginModel model,
			List<String> modE, List<String> modActi, List<String> modTActi,
			List<String> modInhib, List<String> modTInhib, List<String> modCat)
			throws RateLawNotApplicableException, IllegalFormatException {
		StringBuffer numerator;
		StringBuffer formelTxt[] = new StringBuffer[Math.max(1, modE.size())];

		PluginReaction parentReaction = getParentReaction();
		int enzymeNum = 0;
		do {
			StringBuffer denominator = new StringBuffer();
			StringBuffer kcatp = concat("kcatp_", getParentReactionID());
			if (modE.size() > 1)
				kcatp = append(kcatp, underscore, modE.get(enzymeNum));
			addLocalParameter(kcatp);
			numerator = new StringBuffer(kcatp);

			// sums for each educt
			for (int eductNum = 0; eductNum < parentReaction.getNumReactants(); eductNum++) {
				StringBuffer exp = new StringBuffer();
				PluginSpeciesReference specref = parentReaction
						.getReactant(eductNum);

				// build denominator
				// TODO reactionnum
				StringBuffer kM = concat("kM_", getParentReactionID());

				if (modE.size() > 1)
					kM = concat(kM, underscore, modE.get(enzymeNum));
				kM = append(kM, underscore, specref.getSpecies());

				addLocalParameter(kM);

				// we can save the brakets if there is just one educt.
				if (parentReaction.getNumReactants() > 1)
					denominator.append(Character.valueOf('('));

				if (!parentReaction.getReversible()
						|| ((parentReaction.getNumReactants() != 1) || (parentReaction
								.getNumProducts() == 1)))
					denominator.append(" 1 + ");

				denominator = concat(denominator, Character.valueOf('('),
						specref.getSpecies(), Character.valueOf('/'), kM,
						Character.valueOf(')'));

				for (int m = 1; m < (int) specref.getStoichiometry(); m++) {
					exp = concat(Character.valueOf('^'), (m + 1));
					denominator = concat(denominator, " + (", specref
							.getSpecies(), Character.valueOf('/'), kM,
							Character.valueOf(')'), exp);
				}

				// we can save the brakets if there is just one educt
				if (parentReaction.getNumReactants() > 1)
					denominator.append(Character.valueOf(')'));

				if ((eductNum + 1) < parentReaction.getNumReactants())
					denominator.append(Character.valueOf('*'));

				// build numerator
				if (specref.getStoichiometry() == 1.0)
					numerator = concat(numerator, " * (", specref.getSpecies(),
							Character.valueOf('/'), kM, Character.valueOf(')'));
				else
					numerator = concat(numerator, " * (", specref.getSpecies(),
							Character.valueOf('/'), kM, Character.valueOf(')'),
							exp);
			}

			/*
			 * only if reaction is reversible or we want it to be.
			 */
			if (parentReaction.getReversible()) {
				denominator.append(Character.valueOf('+'));
				numerator.append(Character.valueOf('-'));

				// TODO reaction num
				StringBuffer kcat = concat("kcatn_", getParentReactionID());

				if (modE.size() > 1) {
					kcat = concat(kcat, underscore, modE.get(enzymeNum));

				}
				numerator.append(kcat);

				if (!listOfLocalParameters.contains(kcat))
					listOfLocalParameters.add(new StringBuffer(kcat));

				// Sums for each product
				for (int productNum = 0; productNum < parentReaction
						.getNumProducts(); productNum++) {
					// TODO reaction num
					StringBuffer kM = new StringBuffer("kM_");

					if (modE.size() > 1)
						kM = concat(kM, underscore, modE.get(enzymeNum));
					kM = concat(kM, underscore, parentReaction.getProduct(
							productNum).getSpeciesInstance().getId());

					if (!listOfLocalParameters.contains(kM))
						listOfLocalParameters.add(new StringBuffer(kM));

					PluginSpeciesReference specRefP = parentReaction
							.getProduct(productNum);
					if (parentReaction.getNumProducts() > 1)
						denominator.append("(1 + ");
					denominator = concat(denominator, Character.valueOf('('),
							specRefP.getSpecies(), Character.valueOf('/'), kM,
							Character.valueOf(')'));

					// for each stoichiometry (see Liebermeister et al.)
					for (int m = 1; m < (int) specRefP.getStoichiometry(); m++)
						// exp = "^" + (m + 1);
						denominator = sum(denominator, pow(frac(
								getSpecies(specRefP), kM), Integer
								.toString(m + 1)));

					// if (parentReaction.getNumProducts() > 1)
					// denominator += ')';

					if ((productNum + 1) < parentReaction.getNumProducts())
						denominator.append(" * ");

					// build numerator
					if (specRefP.getStoichiometry() != 1.0)
						numerator = times(numerator, pow(frac(
								getSpecies(specRefP), kM),
								getStoichiometry(specRefP)));
					else
						numerator = times(numerator, frac(getSpecies(specRefP),
								kM));
				}
				if ((parentReaction.getNumProducts() > 1)
						&& (parentReaction.getNumReactants() > 1))
					denominator = diff(denominator, Integer.toString(1));
			}
			formelTxt[enzymeNum] = frac(numerator, denominator);
			if (modE.size() > 0)
				formelTxt[enzymeNum] = times(modE.get(enzymeNum), formelTxt);
			enzymeNum++;
		} while (enzymeNum <= modE.size() - 1);
		return times(activationFactor(modActi), inhibitionFactor(modInhib),
				sum(formelTxt));
		// if (enzymeNum > 1)
		// System.err.println("Reversible 2");
		// return formelTxt;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.sbmlsqueezer.kinetics.GeneralizedMassAction#createKineticEquation
	 * (jp.sbi.celldesigner.plugin.PluginModel, java.util.List, java.util.List,
	 * java.util.List, java.util.List, java.util.List, java.util.List)
	 */
	// protected StringBuffer createKineticEquation(PluginModel model,
	// List<String> modE, List<String> modActi, List<String> modTActi,
	// List<String> modInhib, List<String> modTInhib, List<String> modCat)
	// throws RateLawNotApplicableException, IllegalFormatException {
	// PluginReaction reaction = getParentReaction();
	//		
	//
	// StringBuffer catalysts[] = new StringBuffer[Math.max(1, modE.size())];
	// for (int i = 0; i < catalysts.length; i++) {
	// StringBuffer numerator, denominator;
	// numerator = times(numerators(i, modE, true));
	// if (!reaction.getReversible())
	// denominator = times(denominators(i, modE, true));
	// else {
	// numerator = diff(numerator, times(numerators(i, modE, false)));
	// denominator = diff(sum(times(denominators(i, modE, true)),
	// times(denominators(i, modE, false))), Integer
	// .toString(1));
	// }
	// catalysts[i] = frac(numerator, denominator);
	// if (modE.size() > 0)
	// catalysts[i] = times(modE.get(i), catalysts[i]);
	// }
	// return times(activationFactor(modActi), inhibitionFactor(modInhib),
	// sum(catalysts));
	// }
	//
	// /**
	// * Returns an array containing the factors of the reactants and products
	// * included in the convenience kinetic's numerator. Each factor is given
	// by
	// * the quotient of the respective species' concentration and it's related
	// * equilibrium constant to the power of the species' stoichiometry. The
	// * array also contains the catalytic constant of the reaction. This method
	// * is applicable for both forward and backward reactions.
	// *
	// * @param reaction
	// * @param enzymeNumber
	// * @param modE
	// * @param type
	// * true means forward, false means reverse.
	// * @return
	// */
	protected StringBuffer[] numerators(int enzymeNumber, List<String> modE,
			boolean type) {
		// FORWARD = true;
		PluginReaction reaction = getParentReaction();
		StringBuffer[] nums = new StringBuffer[type ? (reaction
				.getNumReactants() + 1) : (reaction.getNumProducts() + 1)];
		nums[0] = new StringBuffer(type ? "kcatp_" : "kcatn_");
		nums[0].append(reaction.getId());
		if (modE.size() > 1)
			append(nums[0], underscore, modE.get(enzymeNumber));
		addLocalParameter(nums[0]);
		for (int i = 1; i < nums.length; i++) {
			PluginSpeciesReference ref = type ? reaction.getReactant(i - 1)
					: reaction.getProduct(i - 1);
			StringBuffer kM = concat("kM_", reaction.getId());
			if (modE.size() > 1)
				append(kM, underscore, modE.get(enzymeNumber));
			append(kM, underscore, ref.getSpecies());
			addLocalParameter(kM);
			nums[i] = pow(frac(getSpecies(ref), kM), getStoichiometry(ref));
		}
		return nums;
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
	 *            true means forward, false backward.
	 * @return
	 */
	protected StringBuffer[] denominators(int enzymeNumber, List<String> modE,
			boolean type) {
		// type FORWARD = true;
		PluginReaction reaction = getParentReaction();
		StringBuffer[] denoms = new StringBuffer[type ? reaction
				.getNumReactants() : reaction.getNumProducts()];
		for (int i = 0; i < denoms.length; i++) {
			PluginSpeciesReference ref = type ? reaction.getReactant(i)
					: reaction.getProduct(i);
			StringBuffer kM = concat("kM_", getParentReactionID());
			if (modE.size() > 1)
				append(kM, underscore, modE.get(enzymeNumber));
			append(kM, underscore, ref.getSpecies());

			StringBuffer[] parts = new StringBuffer[(int) ref
					.getStoichiometry() + 1];
			StringBuffer part = brackets(sum(Integer.toString(1), frac(
					getSpecies(ref), kM)));
			for (int j = 0; j < parts.length; j++)
				parts[j] = pow(part, Integer.toString(j));
			denoms[i] = sum(parts);
		}
		return denoms;
	}

	// /**
	// * TODO Method that sums up the single +1 and -1 terms in the denominator.
	// *
	// * @param sb
	// * @return
	// */
	// protected StringBuffer removeOnes(StringBuffer sb) {
	// return sb;
	// }
}
