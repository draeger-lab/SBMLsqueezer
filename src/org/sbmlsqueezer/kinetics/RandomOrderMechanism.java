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
 * 
 * @date Aug 1, 2007
 */
public class RandomOrderMechanism extends GeneralizedMassAction {

	/**
	 * @param parentReaction
	 * @param model
	 * @param reversibility
	 * @throws RateLawNotApplicableException
	 * @throws IOException
	 * @throws IllegalFormatException
	 */
	public RandomOrderMechanism(PluginReaction parentReaction,
			PluginModel model, boolean reversibility)
			throws RateLawNotApplicableException, IOException,
			IllegalFormatException {
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
	public RandomOrderMechanism(PluginReaction parentReaction,
			PluginModel model, List<String> listOfPossibleEnzymes)
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
		for (int i = 0; i < getParentReaction().getListOfProducts()
				.getNumItems(); i++)
			stoichiometryRight += getParentReaction().getProduct(i)
					.getStoichiometry();
		String name = "rapid-equilibrium random order ternary-complex mechanism";
		if ((getParentReaction().getNumProducts() == 2)
				|| (stoichiometryRight == 2))
			name += " with two products";
		else if ((getParentReaction().getNumProducts() == 1)
				|| (stoichiometryRight == 1))
			name += " with one product";
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
		boolean biuni = false;
		StringBuffer numerator = new StringBuffer();// I
		StringBuffer denominator = new StringBuffer(); // II
		StringBuffer catalysts[] = new StringBuffer[Math.max(1, modE.size())];

		PluginReaction reaction = getParentReaction();
		PluginSpeciesReference specRefE1 = reaction.getReactant(0), specRefE2;
		PluginSpeciesReference specRefP1 = reaction.getProduct(0), specRefP2 = null;

		if (reaction.getNumReactants() == 2)
			specRefE2 = (PluginSpeciesReference) reaction.getListOfReactants()
					.get(1);
		else if (specRefE1.getStoichiometry() == 2.0)
			specRefE2 = specRefE1;
		else
			throw new RateLawNotApplicableException(
					"Number of reactants must equal two to apply random order "
							+ "Michaelis-Menten kinetics to reaction "
							+ reaction.getId());

		boolean exception = false;
		switch (reaction.getNumProducts()) {
		case 1:
			if (specRefP1.getStoichiometry() == 1.0)
				biuni = true;
			else if (specRefP1.getStoichiometry() == 2.0)
				specRefP2 = specRefP1;
			else
				exception = true;
			break;
		case 2:
			specRefP2 = (PluginSpeciesReference) reaction.getListOfProducts()
					.get(1);
			break;
		default:
			exception = true;
			break;
		}
		if (exception)
			throw new RateLawNotApplicableException(
					"Number of products must equal either one or two to apply random order "
							+ "Michaelis-Menten kinetics to reaction "
							+ reaction.getId());
		/*
		 * If modE is empty there was no enzyme sined to the reaction. Thus we
		 * do not want anything in modE to occur in the kinetic equation.
		 */
		int enzymeNum = 0;

		do {
			/*
			 * Irreversible reaction
			 */
			if (!reaction.getReversible()) {
				StringBuffer kcatp;
				StringBuffer kMr2 = concat("kM_", reaction.getId());
				StringBuffer kMr1 = concat("kM_", reaction.getId());
				StringBuffer kIr1 = concat("ki_", reaction.getId());

				if (modE.size() == 0)
					kcatp = concat("Vp_", reaction.getId());
				else {
					kcatp = concat("kcatp_", reaction.getId());
					if (modE.size() > 1) {
						append(kcatp, underscore, modE.get(enzymeNum));
						append(kMr2, underscore, modE.get(enzymeNum));
						append(kMr1, underscore, modE.get(enzymeNum));
						append(kIr1, underscore, modE.get(enzymeNum));
					}
				}
				String speciesE2 = specRefE2.getSpecies();
				String speciesE1 = specRefE1.getSpecies();
				append(kMr1, underscore, speciesE1);
				append(kMr2, underscore, speciesE2);

				if (specRefE2.equals(specRefE1)) {
					append(kMr1, "kMr1", kMr1.substring(2));
					append(kMr2, "kMr2", kMr2.substring(2));
				}
				addLocalParameter(kcatp);
				addLocalParameter(kMr2);
				addLocalParameter(kMr1);
				addLocalParameter(append(kIr1, underscore, speciesE1));

				numerator = new StringBuffer(kcatp);
				if (modE.size() > 0)
					numerator = times(numerator, modE.get(enzymeNum));
				numerator = times(numerator, specRefE1.getSpecies());
				if (specRefE2.equals(specRefE1)) {
					numerator = pow(numerator, Integer.valueOf(2));
					denominator = sum(times(kIr1, kMr2), times(sum(kMr2, kMr1),
							specRefE1.getSpecies()), pow(
							specRefE1.getSpecies(), Integer.valueOf(2)));
				} else {
					numerator = times(numerator, specRefE2.getSpecies());
					denominator = sum(times(kIr1, kMr2), times(kMr2, specRefE1
							.getSpecies()),
							times(kMr1, specRefE2.getSpecies()), times(
									specRefE1.getSpecies(), specRefE2
											.getSpecies()));
				}
			} else {
				/*
				 * Reversible reaction: Bi-Bi
				 */
				if (!biuni) {
					StringBuffer kcatp;
					StringBuffer kcatn;

					StringBuffer kMr2 = concat("kM_", reaction.getId());
					StringBuffer kIr1 = concat("ki_", reaction.getId());
					StringBuffer kMp1 = concat("kM_", reaction.getId());
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
							String modEnzymeNumber = modE.get(enzymeNum);
							kcatp = concat(kcatp, underscore, modEnzymeNumber);
							kcatn = concat(kcatn, underscore, modEnzymeNumber);
							kMr2 = concat(kMr2, underscore, modEnzymeNumber);
							kMp1 = concat(kMp1, underscore, modEnzymeNumber);
							kIp1 = concat(kIp1, underscore, modEnzymeNumber);
							kIp2 = concat(kIp2, underscore, modEnzymeNumber);
							kIr2 = concat(kIr2, underscore, modEnzymeNumber);
							kIr1 = concat(kIr1, underscore, modEnzymeNumber);
						}
					}
					String speciesE2 = specRefE2.getSpecies();
					String speciesE1 = specRefE1.getSpecies();
					String speciesP1 = specRefP1.getSpecies();
					String speciesP2 = specRefP2.getSpecies();
					kMr2 = concat(kMr2, underscore, speciesE2);
					kIr1 = concat(kIr1, underscore, speciesE1);
					kIr2 = concat(kIr2, underscore, speciesE2);
					kIp1 = concat(kIp1, underscore, speciesP1);
					kIp2 = concat(kIp2, underscore, speciesP2);
					kMp1 = concat(kMp1, underscore, speciesP1);
					if (specRefE2.equals(specRefE1)) {
						kIr1 = concat("kir1", kIr1.substring(2));
						kIr2 = concat("kir2", kIr2.substring(2));
					}
					if (specRefP2.equals(specRefP1)) {
						kIp1 = concat("kip1", kIp1.substring(2));
						kIp2 = concat("kip2", kIp2.substring(2));
					}
					addLocalParameter(kcatp);
					addLocalParameter(kMr2);
					addLocalParameter(kMr2);
					addLocalParameter(kMp1);
					addLocalParameter(kIp1);
					addLocalParameter(kIp2);
					addLocalParameter(kIr2);
					addLocalParameter(kIr1);

					StringBuffer numeratorForward = new StringBuffer();
					StringBuffer numeratorReverse = new StringBuffer();

					numeratorForward = kcatp;
					if (modE.size() > 0)
						numeratorForward = times(numeratorForward,
								new StringBuffer(modE.get(enzymeNum)));
					denominator = sum(Integer.toString(1), frac(specRefE1
							.getSpecies(), kIr1), frac(specRefE2.getSpecies(),
							kIr2), frac(specRefP1.getSpecies(), kIp1), frac(
							specRefP2.getSpecies(), kIp2));

					// happens if the product has a stoichiometry of two.

					StringBuffer p1p2 = specRefP1.equals(specRefP2) ? pow(
							specRefP1.getSpecies(), Integer.toString(2))
							: times(specRefP1.getSpecies(), specRefP2
									.getSpecies());
					denominator = sum(denominator,
							frac(p1p2, times(kIp2, kMp1)));

					// happens if the educt has a stoichiometry of two.
					if (specRefE1.equals(specRefE2)) {

						numeratorForward = times(numeratorForward, pow(
								new StringBuffer(specRefE1.getSpecies()),
								Integer.toString(2)));
						p1p2 = pow(new StringBuffer(specRefE1.getSpecies()),
								Integer.toString(2));
					} else {
						numeratorForward = times(numeratorForward, times(
								new StringBuffer(specRefE1.getSpecies()),
								new StringBuffer(specRefE2.getSpecies())));
						p1p2 = times(new StringBuffer(specRefE1.getSpecies()),
								new StringBuffer(specRefE2.getSpecies()));
					}
					numeratorForward = frac(numeratorForward, times(kIr1, kMr2));
					denominator = sum(denominator,
							frac(p1p2, times(kIr1, kMr2)));

					numeratorReverse = kcatn;
					if (modE.size() != 0)
						numeratorReverse = times(numeratorReverse,
								new StringBuffer(modE.get(enzymeNum)));
					numeratorReverse = times(numeratorReverse,
							new StringBuffer(specRefP1.getSpecies()));
					if (specRefP1.equals(specRefP2)) {
						numeratorReverse = pow(numeratorReverse, Integer
								.toString(2));
					} else {
						numeratorReverse = times(numeratorReverse, specRefP2
								.getSpecies());
					}
					numeratorReverse = frac(numeratorReverse, times(kIp2, kMp1));
					numerator = diff(numeratorForward, numeratorReverse);
				} else {
					/*
					 * Reversible reaction: Bi-Uni reaction
					 */
					StringBuffer kcatp = new StringBuffer();
					StringBuffer kcatn = new StringBuffer();

					StringBuffer kMr2 = concat("kM_", reaction.getId());
					StringBuffer kMp1 = concat("kM_", reaction.getId());
					StringBuffer kIr2 = concat("ki_", reaction.getId());
					StringBuffer kIr1 = concat("ki_", reaction.getId());

					if (modE.size() == 0) {
						kcatp = concat("Vp_", reaction.getId());
						kcatn = concat("Vn_", reaction.getId());
					} else {
						kcatp = concat("kcatp_", reaction.getId());
						kcatn = concat("kcatn_", reaction.getId());
						if (modE.size() > 1) {
							append(kcatp, underscore, modE.get(enzymeNum));
							append(kcatn, underscore, modE.get(enzymeNum));
							append(kMr2, underscore, modE.get(enzymeNum));
							append(kMp1, underscore, modE.get(enzymeNum));
							append(kIr2, underscore, modE.get(enzymeNum));
							append(kIr1, underscore, modE.get(enzymeNum));
						}
					}

					StringBuffer speciesE2 = new StringBuffer(specRefE2
							.getSpecies());
					append(kMr2, underscore, speciesE2);
					append(kIr1, underscore, speciesE2);
					append(kIr2, underscore, speciesE2);
					append(kMp1, underscore, speciesE2);

					if (specRefE2.equals(specRefE1)) {
						append(kIr1, "kip1", kIr1.substring(2));
						append(kIr2, "kip2", kIr2.substring(2));
					}
					addLocalParameter(kcatp);
					addLocalParameter(kcatn);
					addLocalParameter(kMr2);
					addLocalParameter(kMp1);
					addLocalParameter(kIr2);
					addLocalParameter(kIr1);

					StringBuffer numeratorForward = new StringBuffer(kcatp);
					StringBuffer numeratorReverse = new StringBuffer();
					StringBuffer p1p2 = new StringBuffer();

					if (modE.size() != 0)
						numeratorForward = times(numeratorForward, modE
								.get(enzymeNum));

					denominator = sum(Integer.toString(1), frac(specRefE1
							.getSpecies(), kIr1), frac(specRefE2.getSpecies(),
							kIr2));
					if (specRefE1.equals(specRefE2)) {
						numeratorForward = times(numeratorForward, pow(
								specRefE1.getSpecies(), Integer.toString(2)));
						p1p2 = pow(specRefE1.getSpecies(), Integer.toString(2));
					} else {
						numeratorForward = times(numeratorForward, specRefE1
								.getSpecies(), specRefE2.getSpecies());
						p1p2 = times(specRefE1.getSpecies(), specRefE2
								.getSpecies());
					}
					numeratorForward = frac(numeratorForward, times(kIr1, kMr2));

					numeratorReverse = kcatn;
					if (modE.size() != 0)
						numeratorReverse = times(numeratorReverse, modE
								.get(enzymeNum));
					if ((specRefP2 != null) && specRefP2.equals(specRefP1))
						numeratorReverse = times(numeratorReverse, pow(
								specRefP1.getSpecies(), Integer.toString(2)));
					else if (specRefP2 != null)
						numeratorReverse = times(numeratorReverse, specRefP1
								.getSpecies(), specRefP2.getSpecies());

					numeratorReverse = times(numeratorReverse, frac(specRefP1
							.getSpecies(), kMp1));

					numerator = diff(numeratorForward, numeratorReverse);
					denominator = sum(denominator, frac(p1p2, sum(times(kIr1,
							kMr2))), frac(specRefP1.getSpecies(), kMp1));
				}
			}
			// Construct formula
			catalysts[enzymeNum++] = frac(numerator, denominator);
		} while (enzymeNum < modE.size());
		return times(activationFactor(modActi), inhibitionFactor(modInhib),
				sum(catalysts));
	}
}
