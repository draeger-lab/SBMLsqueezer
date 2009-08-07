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
 * This class creates a kinetic equation according to the random order mechanism
 * (see Cornish-Bowden: Fundamentals of Enzyme Kinetics, p. 169).
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
		for (int i = 0; i < getParentReaction().getNumProducts(); i++)
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

		PluginReaction reaction = getParentReaction();
		PluginSpeciesReference specRefR1 = reaction.getReactant(0), specRefR2;
		PluginSpeciesReference specRefP1 = reaction.getProduct(0), specRefP2 = null;

		if (reaction.getNumReactants() == 2)
			specRefR2 = (PluginSpeciesReference) reaction.getListOfReactants()
					.get(1);
		else if (specRefR1.getStoichiometry() == 2f)
			specRefR2 = specRefR1;
		else
			throw new RateLawNotApplicableException(
					"Number of reactants must equal two to apply random order "
							+ "Michaelis-Menten kinetics to reaction "
							+ reaction.getId());

		boolean exception = false;
		boolean biuni = false;
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
		StringBuffer numerator = new StringBuffer();// I
		StringBuffer denominator = new StringBuffer(); // II
		StringBuffer catalysts[] = new StringBuffer[Math.max(1, modE.size())];
		do {
			/*
			 * Irreversible reaction
			 */
			if (!reaction.getReversible()) {
				StringBuffer kcatp;
				StringBuffer kMr1 = concat("kM_", reaction.getId());
				StringBuffer kMr2 = concat("kM_", reaction.getId());
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
				String speciesR1 = specRefR1.getSpecies();
				String speciesR2 = specRefR2.getSpecies();
				append(kMr1, underscore, speciesR1);
				append(kMr2, underscore, speciesR2);
				if (specRefR1.equals(specRefR2)) {
					append(kMr1, "kMr1", kMr1.substring(2));
					append(kMr2, "kMr2", kMr2.substring(2));
				}
				addLocalParameter(kcatp);
				addLocalParameter(kMr1);
				addLocalParameter(kMr2);
				addLocalParameter(append(kIr1, underscore, speciesR1));
				
				numerator = new StringBuffer(kcatp);
				if (modE.size() > 0)
					numerator = times(numerator, modE.get(enzymeNum));
				if (specRefR2.equals(specRefR1)) {
					String r1square = pow(speciesR1, Integer.valueOf(2))
							.toString();
					numerator = times(numerator, r1square);
					denominator = sum(times(kIr1, kMr2), times(sum(kMr1, kMr2),
							speciesR1), r1square);
				} else {
					numerator = times(numerator, speciesR1, speciesR2);
					denominator = sum(times(kIr1, kMr2),
							times(kMr2, speciesR1), times(kMr1, speciesR2),
							times(speciesR1, speciesR2));
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
							String currEnzyme = modE.get(enzymeNum);
							kcatp = concat(kcatp, underscore, currEnzyme);
							kcatn = concat(kcatn, underscore, currEnzyme);
							kMr2 = concat(kMr2, underscore, currEnzyme);
							kMp1 = concat(kMp1, underscore, currEnzyme);
							kIp1 = concat(kIp1, underscore, currEnzyme);
							kIp2 = concat(kIp2, underscore, currEnzyme);
							kIr2 = concat(kIr2, underscore, currEnzyme);
							kIr1 = concat(kIr1, underscore, currEnzyme);
						}
					}
					String speciesR1 = specRefR1.getSpecies();
					String speciesR2 = specRefR2.getSpecies();
					String speciesP1 = specRefP1.getSpecies();
					String speciesP2 = specRefP2.getSpecies();
					kMr2 = concat(kMr2, underscore, speciesR2);
					kIr1 = concat(kIr1, underscore, speciesR1);
					kIr2 = concat(kIr2, underscore, speciesR2);
					kIp1 = concat(kIp1, underscore, speciesP1);
					kIp2 = concat(kIp2, underscore, speciesP2);
					kMp1 = concat(kMp1, underscore, speciesP1);
					if (specRefR2.equals(specRefR1)) {
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

					StringBuffer numeratorForward = frac(kcatp, times(kIr1,
							kMr2));
					StringBuffer numeratorReverse = frac(kcatn, times(kIp2,
							kMp1));
					if (modE.size() > 0) {
						numeratorForward = times(numeratorForward, modE
								.get(enzymeNum));
						numeratorReverse = times(numeratorReverse, modE
								.get(enzymeNum));
					}
					// happens if the reactant has a stoichiometry of two.
					StringBuffer r1r2 = specRefR1.equals(specRefR2) ? pow(
							speciesR1, Integer.toString(2)) : times(speciesR1,
							speciesR2);
					// happens if the product has a stoichiometry of two.
					StringBuffer p1p2 = specRefP1.equals(specRefP2) ? pow(
							speciesP1, Integer.toString(2)) : times(speciesP1,
							speciesP2);
					numeratorForward = times(numeratorForward, r1r2);
					numeratorReverse = times(numeratorReverse, p1p2);
					numerator = diff(numeratorForward, numeratorReverse);
					denominator = sum(Integer.toString(1),
							frac(speciesR1, kIr1), frac(speciesR2, kIr2), frac(
									speciesP1, kIp1), frac(speciesP2, kIp2),
							frac(p1p2, times(kIp2, kMp1)), frac(r1r2, times(
									kIr1, kMr2)));
				} else {
					/*
					 * Reversible reaction: Bi-Uni reaction
					 */
					StringBuffer kcatp;
					StringBuffer kcatn;
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

					String speciesR1 = specRefR1.getSpecies();
					String speciesR2 = specRefR2.getSpecies();
					String speciesP1 = specRefP1.getSpecies();
					append(kMr2, underscore, speciesR2);
					append(kIr1, underscore, speciesR2);
					append(kIr2, underscore, speciesR2);
					append(kMp1, underscore, speciesR2);

					if (specRefR2.equals(specRefR1)) {
						append(kIr1, "kip1", kIr1.substring(2));
						append(kIr2, "kip2", kIr2.substring(2));
					}
					addLocalParameter(kcatp);
					addLocalParameter(kcatn);
					addLocalParameter(kMr2);
					addLocalParameter(kMp1);
					addLocalParameter(kIr2);
					addLocalParameter(kIr1);

					StringBuffer r1r2;
					if (specRefR1.equals(specRefR2))
						r1r2 = pow(speciesR1, Integer.toString(2));
					else
						r1r2 = times(speciesR1, speciesR2);
					StringBuffer numeratorForward = frac(kcatp, times(kIr1,
							kMr2));
					StringBuffer numeratorReverse = frac(kcatn, kMp1);
					if (modE.size() != 0) {
						numeratorForward = times(numeratorForward, modE
								.get(enzymeNum));
						numeratorReverse = times(numeratorReverse, modE
								.get(enzymeNum));
					}
					numeratorForward = times(numeratorForward, r1r2);
					numeratorReverse = times(numeratorReverse, speciesP1);
					numerator = diff(numeratorForward, numeratorReverse);
					denominator = sum(Integer.toString(1),
							frac(speciesR1, kIr1), frac(speciesR2, kIr2), frac(
									r1r2, times(kIr1, kMr2)), frac(speciesP1,
									kMp1));
				}
			}
			// Construct formula
			catalysts[enzymeNum++] = frac(numerator, denominator);
		} while (enzymeNum < modE.size());
		return times(activationFactor(modActi), inhibitionFactor(modInhib),
				sum(catalysts));
	}
}
