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
			StringBuffer kcatp = modE.size() == 0 ? concat("Vp_",
					getParentReactionID()) : concat("kcatp_",
					getParentReactionID());
			if (modE.size() > 1)
				kcatp = append(kcatp, underscore, modE.get(enzymeNum));
			addLocalParameter(kcatp);
			numerator = new StringBuffer(kcatp);

			// sums for each educt
			StringBuffer[] denominator1 = new StringBuffer[parentReaction
					.getNumReactants()];
			for (int eductNum = 0; eductNum < parentReaction.getNumReactants(); eductNum++) {

				PluginSpeciesReference specref = parentReaction
						.getReactant(eductNum);

				// build denominator

				StringBuffer kM = concat("kM_", getParentReactionID());

				if (modE.size() > 1)
					kM = concat(kM, underscore, modE.get(enzymeNum));
				kM = append(kM, underscore, specref.getSpecies());

				addLocalParameter(kM);

				denominator1[eductNum] = brackets(frac(specref.getSpecies(), kM));

				for (int m = 1; m < (int) specref.getStoichiometry(); m++) {

					denominator1[eductNum] = sum(denominator1[eductNum], pow(
							frac(specref.getSpecies(), kM), Integer
									.toString(m + 1)));
				}

				if (!parentReaction.getReversible()
						|| ((parentReaction.getNumReactants() != 1) || (parentReaction
								.getNumProducts() == 1)))

					denominator1[eductNum] = sum(Integer.toString(1),
							denominator1[eductNum]);

				// build numerator
				if (specref.getStoichiometry() == 1.0)
					numerator = times(numerator, frac(specref.getSpecies(), kM));
				else
					numerator = times(numerator, pow(frac(specref.getSpecies(),
							kM), Integer.toString((int) specref
							.getStoichiometry())));
			}
			if (denominator1.length == 1)
				denominator = denominator1[0];
			else
				denominator = times(denominator1);

			/*
			 * only if reaction is reversible or we want it to be.
			 */
			if (parentReaction.getReversible()) {

				StringBuffer numerator2 = new StringBuffer();
				StringBuffer[] denominator2 = new StringBuffer[parentReaction
						.getNumProducts()];

				StringBuffer kcat = modE.size() == 0 ? concat("Vn_",
						getParentReactionID()) : concat("kcatn_",
						getParentReactionID());
				if (modE.size() > 1)
					kcat = concat(kcat, underscore, modE.get(enzymeNum));
				numerator2 = new StringBuffer(kcat);
				addLocalParameter(kcat);

				// Sums for each product

				for (int productNum = 0; productNum < parentReaction
						.getNumProducts(); productNum++) {

					StringBuffer kM = concat("kM_", getParentReactionID());

					if (modE.size() > 1)
						kM = append(kM, underscore, modE.get(enzymeNum));
					kM = append(kM, underscore, parentReaction.getProduct(
							productNum).getSpeciesInstance().getId());

					addLocalParameter(kM);

					PluginSpeciesReference specRefP = parentReaction
							.getProduct(productNum);

					denominator2[productNum] = frac(getSpecies(specRefP), kM);

					// for each stoichiometry (see Liebermeister et al.)s
					for (int m = 1; m < (int) specRefP.getStoichiometry(); m++)

						denominator2[productNum] = sum(
								denominator2[productNum], pow(frac(
										getSpecies(specRefP), kM), Integer
										.toString(m + 1)));
					if (parentReaction.getNumProducts() > 1)
						denominator2[productNum] = sum(Integer.toString(1),
								denominator2[productNum]);

					// build numerator
					if (specRefP.getStoichiometry() != 1.0)
						numerator2 = times(numerator2, pow(frac(
								getSpecies(specRefP), kM),
								getStoichiometry(specRefP)));
					else
						numerator2 = times(numerator2, frac(
								getSpecies(specRefP), kM));

				}
				if (parentReaction.getNumProducts() == 1)
					denominator = sum(denominator, denominator2[0]);
				else
					denominator = sum(denominator, times(denominator2));
				numerator = diff(numerator, numerator2);

				if ((parentReaction.getNumProducts() > 1)
						&& (parentReaction.getNumReactants() > 1))
					denominator = diff(denominator, Integer.toString(1));
			}

			formelTxt[enzymeNum] = frac(numerator, denominator);

			if (modE.size() > 0)
				formelTxt[enzymeNum] = times(modE.get(enzymeNum),
						formelTxt[enzymeNum]);
			enzymeNum++;
		} while (enzymeNum < modE.size());

		return times(activationFactor(modActi), inhibitionFactor(modInhib),
				sum(formelTxt));

	}
}
