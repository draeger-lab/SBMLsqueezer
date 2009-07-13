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
 * @author <a href="mailto:dwouamba@yahoo.fr">Dieudonn&eacute; Wouamba</a>
 * 
 * @date Aug 1, 2007
 */
public class PingPongMechanism extends GeneralizedMassAction {

	/**
	 * @param parentReaction
	 * @param model
	 * @param reversibility
	 * @throws RateLawNotApplicableException
	 * @throws IOException
	 * @throws IllegalFormatException
	 */
	public PingPongMechanism(PluginReaction parentReaction, PluginModel model,
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
	public PingPongMechanism(PluginReaction parentReaction, PluginModel model,
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
		String name = "substituted-enzyme mechanism (Ping-Pong)";
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
		StringBuffer numerator = new StringBuffer();// I
		StringBuffer denominator = new StringBuffer(); // II
		StringBuffer catalysts[] = new StringBuffer[Math.max(1, modE.size())];

		PluginReaction reaction = getParentReaction();
		PluginSpeciesReference specRefE1 = reaction.getReactant(0);
		PluginSpeciesReference specRefP1 = reaction.getProduct(0);
		PluginSpeciesReference specRefE2 = null, specRefP2 = null;

		if (reaction.getNumReactants() == 2)
			specRefE2 = reaction.getReactant(1);
		else if (specRefE1.getStoichiometry() == 2d)
			specRefE2 = specRefE1;
		else
			throw new RateLawNotApplicableException(
					"Number of reactants must equal two to apply ping-pong "
							+ "Michaelis-Menten kinetics to reaction "
							+ reaction.getId());

		boolean exception = false;
		switch (reaction.getNumProducts()) {
		case 1:
			if (specRefP1.getStoichiometry() == 2d)
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
					"Number of products must equal two to apply ping-pong"
							+ "Michaelis-Menten kinetics to reaction "
							+ reaction.getId());

		int enzymeNum = 0;
		do {
			StringBuffer kcatp;
			StringBuffer kMr1 = concat("kM_", reaction.getId());
			StringBuffer kMr2 = concat("kM_", reaction.getId());
			StringBuffer enzyme = new StringBuffer(modE.size() == 0 ? "" : modE
					.get(enzymeNum));

			if (modE.size() == 0)
				kcatp = concat("Vp_", reaction.getId());
			else {
				kcatp = concat("kcatp_", reaction.getId());
				if (modE.size() > 1) {
					append(kcatp, underscore, enzyme);
					append(kMr1, underscore, enzyme);
					append(kMr2, underscore, enzyme);
				}
			}
			append(kMr2, underscore, specRefE2.getSpecies());
			append(kMr1, underscore, specRefE1.getSpecies());
			if (specRefE2.equals(specRefE1)) {
				kMr1 = concat("kMr1", kMr1.substring(2));
				kMr2 = concat("kMr2", kMr2.substring(2));
			}
			addLocalParameter(kcatp);
			addLocalParameter(kMr2);
			addLocalParameter(kMr1);

			/*
			 * Irreversible Reaction
			 */
			if (!reaction.getReversible()) {
				numerator = new StringBuffer(kcatp);

				if (modE.size() > 0)
					numerator = times(numerator, modE.get(enzymeNum));
				numerator = times(numerator, specRefE1.getSpecies());

				denominator = sum(times(kMr2, specRefE1.getSpecies()),times( kMr1,
						specRefE2.getSpecies()));

				if (specRefE2.equals(specRefE1)) {
					numerator = pow(numerator, Integer.toString(2));
					denominator = pow(sum(denominator,specRefE1.getSpecies()), Integer.toString(2));
				} else {
					numerator = times(numerator, specRefE2.getSpecies());
					denominator = sum(denominator,times(specRefE1.getSpecies(), specRefE2.getSpecies()));
				}

				/*
				 * Reversible Reaction
				 */
			} else {
				StringBuffer kcatn;
				StringBuffer kMp1 = concat("kM_", reaction.getId());
				StringBuffer kMp2 = concat("kM_", reaction.getId());
				StringBuffer kIp1 = concat("ki_", reaction.getId());
				StringBuffer kIp2 = concat("ki_", reaction.getId());
				StringBuffer kIr1 = concat("ki_", reaction.getId());

				if (modE.size() == 0)
					kcatn = concat("Vn_", reaction.getId());
				else {
					kcatn = concat("kcatn_", reaction.getId());
					if (modE.size() > 1) {
						StringBuffer modEnzymeNumber = new StringBuffer(modE
								.get(enzymeNum));
						kcatn = concat(kcatn, underscore, modEnzymeNumber);
						kMp1 = concat(kMp1, underscore, modEnzymeNumber);
						kMp2 = concat(kMp2, underscore, modEnzymeNumber);
						kIp1 = concat(kIp1, underscore, modEnzymeNumber);
						kIp2 = concat(kIp2, underscore, modEnzymeNumber);
						kIr1 = concat(kIr1, underscore, modEnzymeNumber);
					}
				}
				kMp1 = concat(kMp1, underscore, specRefP1.getSpecies());
				kMp2 = concat(kMp2, underscore, specRefP2.getSpecies());
				kIp1 = concat(kIp1, underscore, specRefP1.getSpecies());
				kIp2 = concat(kIp2, underscore, specRefP2.getSpecies());
				kIr1 = concat(kIr1, underscore, specRefE1.getSpecies());
				if (specRefP2.equals(specRefP1)) {
					kMp1 = concat("kMp1", kMp1.substring(2));
					kMp2 = concat("kMp2", kMp2.substring(2));
					kIp1 = concat("kip1", kIp1.substring(2));
					kIp2 = concat("kip2", kIp2.substring(2));
				}
				addLocalParameter(kcatn);
				addLocalParameter(kMp2);
				addLocalParameter(kMp1);
				addLocalParameter(kIp1);
				addLocalParameter(kIp2);
				addLocalParameter(kIr1);

				StringBuffer numeratorForward = frac(kcatp, times(kIr1, kMr2));
				StringBuffer numeratorReverse = frac(kcatn, times(kIp1, kMp2));

				if (modE.size() > 0)
					numeratorForward = times(numeratorForward,
							new StringBuffer(modE.get(enzymeNum)));
				denominator = sum(frac(specRefE1.getSpecies(), kIr1),
						frac(times(kMr1, specRefE2.getSpecies()), times(kIr1,
								kMr2)), frac(specRefP1.getSpecies(), kIp1),
						frac(times(kMp1, specRefP2.getSpecies()), times(kIp1,
								kMp2)));
				if (specRefE2.equals(specRefE1)) {
					numeratorForward = times(numeratorForward, pow(specRefE1
							.getSpecies(), Integer.toString(2)));
					denominator = sum(denominator, frac(pow(specRefE1
							.getSpecies(), Integer.toString(2)), times(kIr1,
							kMr2)));
				} else {
					numeratorForward = times(numeratorForward,
							new StringBuffer(specRefE1.getSpecies()),
							new StringBuffer(specRefE2.getSpecies()));
					denominator = sum(denominator, frac(times(specRefE1
							.getSpecies(), specRefE2.getSpecies()), times(kIr1,
							kMr2)));
				}

				denominator = sum(denominator, frac(times(specRefE1
						.getSpecies(), specRefP1.getSpecies()), times(kIr1,
						kIp1)), frac(times(kMr1, specRefE2.getSpecies(),
						specRefP2.getSpecies()), times(kIr1, kMr2, kIp2)));

				if (modE.size() > 0)
					numeratorReverse = times(numeratorReverse, modE
							.get(enzymeNum));

				StringBuffer denominator_p1p2 = new StringBuffer(specRefE1
						.getSpecies());
				if (specRefP2.equals(specRefP1)) {
					numeratorReverse = times(numeratorReverse, pow(specRefP1
							.getSpecies(), Integer.toString(2)));
					denominator_p1p2 = pow(specRefP1.getSpecies(), Integer
							.toString(2));

				} else {
					numeratorReverse = times(numeratorReverse, specRefP1
							.getSpecies(), specRefP2.getSpecies());

					denominator_p1p2 = times(specRefP1.getSpecies(), specRefP2
							.getSpecies());
				}
				numerator = diff(numeratorForward, numeratorReverse);
				denominator_p1p2 = frac(denominator_p1p2, times(kIp1, kMp2));
				denominator = sum(denominator, denominator_p1p2);
			}
			catalysts[enzymeNum++] = frac(numerator, denominator);
		} while (enzymeNum <= modE.size() - 1);
		return times(activationFactor(modActi), inhibitionFactor(modInhib),
				sum(catalysts));
	}

}
