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
 * TODO: comment missing
 * 
 * @since 1.0
 * @version
 * @author <a href="mailto:Nadine.hassis@gmail.com">Nadine Hassis</a>
 * @author <a href="mailto:andreas.draeger@uni-tuebingen.de">Andreas
 *         Dr&auml;ger</a>
 * @date Aug 1, 2007
 */
public class OrderedMechanism extends GeneralizedMassAction {

	/**
	 * @param parentReaction
	 * @param model
	 * @param reversibility
	 * @throws RateLawNotApplicableException
	 * @throws IOException
	 * @throws IllegalFormatException
	 */
	public OrderedMechanism(PluginReaction parentReaction, PluginModel model,
			boolean reversibility) throws RateLawNotApplicableException,
			IOException, IllegalFormatException {
		super(parentReaction, model);
	}

	/**
	 * @param parentReaction
	 * @param model
	 * @param reversibility
	 * @param listOfPossibleEnzymes
	 * @throws RateLawNotApplicableException
	 * @throws IOException
	 * @throws IllegalFormatException
	 */
	public OrderedMechanism(PluginReaction parentReaction, PluginModel model,
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
		// according to Cornish-Bowden: Fundamentals of Enzyme kinetics
		double stoichiometryRight = 0;
		for (int i = 0; i < getParentReaction().getNumProducts(); i++)
			stoichiometryRight += getParentReaction().getProduct(i)
					.getStoichiometry();
		String name = "compulsory-order ternary-complex mechanism";
		if ((getParentReaction().getNumProducts() == 2)
				|| (stoichiometryRight == 2))
			name += ", two products";
		else if ((getParentReaction().getNumProducts() == 1)
				|| (stoichiometryRight == 1))
			name += ", one product";
		if (getParentReaction().getReversible())
			return "reversible " + name;
		return "irreversible " + name;
	}

	// @Override
	public String getSBO() {
		return "none";
	}

	// @Override
	protected StringBuffer createKineticEquation(PluginModel model,
			List<String> modE, List<String> modActi, List<String> modTActi,
			List<String> modInhib, List<String> modTInhib, List<String> modCat)
			throws RateLawNotApplicableException, IllegalFormatException {
		StringBuffer numerator;// I
		StringBuffer denominator; // II
		StringBuffer catalysts[] = new StringBuffer[Math.max(1, modE.size())];

		PluginReaction reaction = getParentReaction();
		PluginSpeciesReference specRefE1 = reaction.getReactant(0), specRefE2 = null;
		PluginSpeciesReference specRefP1 = reaction.getProduct(0), specRefP2 = null;

		if (reaction.getNumReactants() == 2)
			specRefE2 = reaction.getReactant(1);
		else if (specRefE1.getStoichiometry() == 2f)
			specRefE2 = specRefE1;
		else
			throw new RateLawNotApplicableException(
					"Number of reactants must equal two to apply ordered "
							+ "Michaelis-Menten kinetics to reaction "
							+ reaction.getId());

		boolean exception = false, biuni = false;
		switch (reaction.getNumProducts()) {
		case 1:
			if (specRefP1.getStoichiometry() == 1f)
				biuni = true;
			else if (specRefP1.getStoichiometry() == 2f)
				specRefP2 = specRefP1;
			else
				exception = true;
			break;
		case 2:
			specRefP2 = reaction.getProduct(1);
			break;
		default:
			exception = true;
			break;
		}
		if (exception)
			throw new RateLawNotApplicableException(
					"Number of products must equal either one or two to apply ordered "
							+ "kinetics to reaction " + reaction.getId());

		int enzymeNum = 0;
		do {
			/*
			 * Variables that are needed for the different combinations of
			 * educts and prodcuts.
			 */

			StringBuffer kcatp;
			StringBuffer kMr1 = concat("kM_", reaction.getId());
			StringBuffer kMr2 = concat("kM_", reaction.getId());
			StringBuffer kIr1 = concat("ki_", reaction.getId());

			// reverse reactions
			StringBuffer kcatn;
			StringBuffer kMp1 = concat("kM_", reaction.getId());
			StringBuffer kMp2 = concat("kM_", reaction.getId());
			StringBuffer kIp1 = concat("ki_", reaction.getId());
			StringBuffer kIp2 = concat("ki_", reaction.getId());
			StringBuffer kIr2 = concat("ki_", reaction.getId());

			if (modE.size() == 0) {
				kcatp = concat("Vp_", reaction.getId());
				kcatn = concat("Vn_", reaction.getId());
			} else {
				kcatp = concat("kcatp_", reaction.getId());
				kcatn = concat("kcatn_", reaction.getId());
				if (modE.size() > 1) {
					String e = modE.get(enzymeNum);
					append(kcatp, underscore, e);
					append(kMr1, underscore, e);
					append(kMr2, underscore, e);
					append(kIr1, underscore, e);
					// reverse reactions
					append(kcatn, underscore, e);
					append(kMp1, underscore, e);
					append(kMp2, underscore, e);
					append(kIp1, underscore, e);
					append(kIp2, underscore, e);
					append(kIr2, underscore, e);
				}
			}
			append(kMr2, underscore, specRefE2.getSpecies());
			append(kMr1, underscore, specRefE1.getSpecies());
			// reverse reactions
			append(kMp1, underscore, specRefP1.getSpecies());
			if (specRefP2 != null)
				append(kMp2, underscore, specRefP2.getSpecies());

			if (specRefE2.equals(specRefE1)) {
				kMr1 = concat("kMr1", kMr1.substring(2));
				kMr2 = concat("kMr2", kMr2.substring(2));
				kIr1 = concat("kIr1", kIr1.substring(2));
				kIr2 = concat("kIr2", kIr2.substring(2));
			}
			append(kIr1, underscore, specRefE1.getSpecies());
			append(kIr2, underscore, specRefE2.getSpecies());

			// reversible reactions
			kIp1 = concat(kIp1, underscore, specRefP1.getSpecies());
			if (specRefP2 != null) {
				if (specRefP2.equals(specRefP1)) {
					kMp1 = concat("kMp1", kMp1.substring(2));
					kMp2 = concat("kMp2", kMp2.substring(2));
					kIp1 = concat("kIp1", kIp1.substring(2));
					kIp2 = concat("kIp2", kIp2.substring(2));
				}
				append(kIp2, underscore, specRefP2.getSpecies());
			}

			addLocalParameter(kcatp);

			/*
			 * Irreversible reaction (bi-bi or bi-uni does not matter)
			 */
			if (!reaction.getReversible()) {
				addLocalParameter(kMr2);
				addLocalParameter(kMr1);
				addLocalParameter(kIr1);

				numerator = new StringBuffer(kcatp);
				if (modE.size() > 0)
					numerator = times(numerator, modE.get(enzymeNum));
				numerator = times(numerator, pow(specRefE1.getSpecies(),
						getStoichiometry(specRefE1)));
				denominator = times(kIr1, kMr2);

				if (specRefE2.equals(specRefE1)) {
					denominator = sum(denominator, times(sum(kMr1, kMr2),
							specRefE1.getSpecies()), pow(
							specRefE1.getSpecies(), Integer.toString(2)));
				} else {
					numerator = times(numerator, pow(specRefE2.getSpecies(),
							getStoichiometry(specRefE2)));
					denominator = sum(denominator, times(kMr2, specRefE1
							.getSpecies()),
							times(kMr1, specRefE2.getSpecies()), times(
									specRefE1.getSpecies(), specRefE2
											.getSpecies()));
				}
			} else if (!biuni) {
				/*
				 * Reversible Bi-Bi reaction.
				 */
				addLocalParameter(kIr2);
				addLocalParameter(kcatn);
				addLocalParameter(kMr1);
				addLocalParameter(kMr2);
				addLocalParameter(kMp1);
				addLocalParameter(kMp2);
				addLocalParameter(kIr1);
				addLocalParameter(kIp1);
				addLocalParameter(kIp2);

				StringBuffer numeratorForward = new StringBuffer(kcatp);
				StringBuffer numeratorReverse = new StringBuffer(kcatn);

				if (modE.size() > 0)
					numeratorForward = times(numeratorForward, modE
							.get(enzymeNum));

				denominator = sum(Integer.toString(1), frac(specRefE1
						.getSpecies(), kIr1), frac(times(kMr1, specRefE2
						.getSpecies()), times(kIr1, kMr2)), frac(times(kMp2,
						specRefP1.getSpecies()), times(kIp2, kMp1)), frac(
						specRefP2.getSpecies(), kIp2));

				if (specRefE2.equals(specRefE1)) {
					numeratorForward = times(numeratorForward, pow(specRefE1
							.getSpecies(), Integer.toString(2)));
					denominator = sum(denominator, frac(pow(specRefE1
							.getSpecies(), Integer.toString(2)), times(kIr1,
							kMr2)));
				} else {
					numeratorForward = times(numeratorForward, times(specRefE1
							.getSpecies()), specRefE2.getSpecies());
					denominator = sum(denominator, frac(times(specRefE1
							.getSpecies(), specRefE2.getSpecies()), times(kIr1,
							kMr2)));
				}
				numeratorForward = frac(numeratorForward, times(kIr1, kMr2));

				if (modE.size() > 0)
					numeratorReverse = times(numeratorReverse, modE
							.get(enzymeNum));

				if (specRefP2.equals(specRefP1))
					numeratorReverse = times(numeratorReverse, pow(specRefP1
							.getSpecies(), Integer.toString(2)));
				else
					numeratorReverse = times(numeratorReverse, times(specRefP1
							.getSpecies(), specRefP2.getSpecies()));
				numeratorReverse = frac(numeratorReverse, times(kIp2, kMp1));
				numerator = diff(numeratorForward, numeratorReverse);

				denominator = sum(denominator, frac(times(kMp2, specRefE1
						.getSpecies(), specRefP1.getSpecies()), times(kIr1,
						kMp1, kIp2)), frac(times(kMr1, specRefE2.getSpecies(),
						specRefP2.getSpecies()), times(kIr1, kMr2, kIp2)));

				if (specRefP2.equals(specRefP1))
					denominator = sum(denominator, frac(pow(specRefP1
							.getSpecies(), Integer.toString(2)), times(kMp1,
							kIp2)));
				else
					denominator = sum(denominator, frac(times(specRefP1
							.getSpecies(), specRefP2.getSpecies()), times(kMp1,
							kIp2)));

				if (specRefE2.equals(specRefE1))
					denominator = sum(denominator, frac(times(pow(specRefE1
							.getSpecies(), Integer.toString(2)), specRefP1
							.getSpecies()), times(kIr1, kMr2, kIp1)));
				else
					denominator = sum(denominator, frac(times(specRefE1
							.getSpecies(), specRefE2.getSpecies(), specRefP1
							.getSpecies()), times(kIr1, kMr2, kIp1)));

				if (specRefP2.equals(specRefP1))
					denominator = sum(denominator, frac(times(specRefE2
							.getSpecies(), pow(specRefP1.getSpecies(), Integer
							.valueOf(2))), times(kIr2, kMp1, kIp2)));
				else
					denominator = sum(denominator, frac(times(specRefE2
							.getSpecies(), times(specRefP1.getSpecies(),
							specRefP2.getSpecies())), times(kIr2, kMp1, kIp2)));
			} else {
				/*
				 * Reversible bi-uni reaction
				 */
				addLocalParameter(kcatn);
				addLocalParameter(kMr1);
				addLocalParameter(kMr2);
				addLocalParameter(kMp1);
				addLocalParameter(kIr1);
				addLocalParameter(kIp1);

				StringBuffer numeratorForward = new StringBuffer(kcatp);
				StringBuffer numeratorReverse = new StringBuffer(kcatn);

				if (modE.size() > 0)
					numeratorForward = times(numeratorForward, modE
							.get(enzymeNum));

//				numeratorForward = times(numeratorForward, specRefE1
//						.getSpecies());

				denominator = sum(Integer.toString(1), frac(specRefE1
						.getSpecies(), kIr1), frac(times(kMr1, specRefE2
						.getSpecies()), times(kIr1, kMr2)));

				if (specRefE2.equals(specRefE1)) {
					numeratorForward = times(numeratorForward, pow(specRefE1
							.getSpecies(), Integer.toString(2)));
					denominator = sum(denominator, frac(pow(specRefE1
							.getSpecies(), Integer.toString(2)), times(kIr1,
							kMr2)));
				} else {
					numeratorForward = times(numeratorForward, times(specRefE1
							.getSpecies(), specRefE2.getSpecies()));
					denominator = sum(denominator, frac(times(specRefE1
							.getSpecies(), specRefE2.getSpecies()), times(kIr1,
							kMr2)));
				}
				numeratorForward = frac(numeratorForward, times(kIr1, kMr2));
				if (modE.size() > 0)
					numeratorReverse = times(numeratorReverse, modE
							.get(enzymeNum));
				numeratorReverse = times(numeratorReverse, frac(specRefP1
						.getSpecies(), kMp1));
				numerator = diff(numeratorForward, numeratorReverse);
				denominator = sum(denominator, frac(times(kMr1, specRefE2
						.getSpecies(), specRefP1.getSpecies()), times(kIr1,
						kMr2, kIp1)), frac(specRefP1.getSpecies(), kMp1));
			}

			/*
			 * Construct formula
			 */
			catalysts[enzymeNum++] = frac(numerator, denominator);
		} while (enzymeNum <= modE.size() - 1);
		return times(activationFactor(modActi), inhibitionFactor(modInhib),
				sum(catalysts));
	}
}
