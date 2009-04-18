/**
 * Copyright (c) ZBiT, University of T&uuml;bingen, Germany
 */
package org.sbmlsqueezer.kinetics;

import java.io.IOException;
import java.util.List;

import jp.sbi.celldesigner.plugin.PluginModel;
import jp.sbi.celldesigner.plugin.PluginReaction;
import jp.sbi.celldesigner.plugin.PluginSpeciesReference;

/**
 * This is the thermodynamically independent form of the convenience kinetics.
 * In cases that the stochiometric matrix has full column rank the less
 * complicated {@see Convenience} can bee invoked.
 * 
 * @since 2.0
 * @version
 * @author Nadine Hassis <Nadine.hassis@gmail.com>
 * @author Andreas Dr&auml;ger <andreas.draeger@uni-tuebingen.de>
 * @author Hannes Borch <hannes.borch@googlemail.com>
 * @date Aug 1, 2007
 */
public class ConvenienceIndependent extends Convenience {

	/**
	 * @param parentReaction
	 * @param model
	 * @param reversibility
	 * @throws RateLawNotApplicableException
	 * @throws IOException
	 */
	public ConvenienceIndependent(PluginReaction parentReaction,
			PluginModel model) throws RateLawNotApplicableException,
			IOException {
		super(parentReaction, model);
	}

	/**
	 * @param parentReaction
	 * @param model
	 * @param listOfPossibleEnzymes
	 * @param reversibility
	 * @throws RateLawNotApplicableException
	 * @throws IOException
	 */
	public ConvenienceIndependent(PluginReaction parentReaction,
			PluginModel model, List<String> listOfPossibleEnzymes)
			throws RateLawNotApplicableException, IOException {
		super(parentReaction, model, listOfPossibleEnzymes);
	}

	public static boolean isApplicable(PluginReaction reaction) {
		// TODO
		return true;
	}

	// @Override
	public String getName() {
		if (this.getParentReaction().getReversible())
			return "reversible thermodynamically independent convenience kinetics";
		return "irreversible thermodynamically independent convenience kinetics";
	}

	// @Override
	public String getSBO() {
		return "none";
	}

	/**
	 * @Override Creates the Convenience kinetic's thermodynamicely independent
	 *           form. This method in general works the same way as the
	 *           according one for the convenience kinetic. Each enzyme's
	 *           fraction is multiplied with a reaction constant that is global
	 *           for the eynzme's reaction.
	 */
	protected StringBuffer createKineticEquation(PluginModel model,
			int reactionNum, List<String> modE, List<String> modActi,
			List<String> modTActi, List<String> modInhib,
			List<String> modTInhib, List<String> modCat)
			throws RateLawNotApplicableException {
		reactionNum++;

		PluginReaction parentReaction = getParentReaction();
		StringBuffer[] enzymes = new StringBuffer[modE.size()];
		try {
			StringBuffer acti = getReactionModifiers(reactionNum, modActi,
					ACTIVATION);
			StringBuffer inhib = getReactionModifiers(reactionNum, modInhib,
					INHIBITION);

			int i = 0;
			do {
				StringBuffer klV = new StringBuffer("kV_");
				klV.append(reactionNum);
				if (modE.size() > 1) {
					klV.append('_');
					klV.append(modE.get(i));
				}
				if (!listOfLocalParameters.contains(klV))
					listOfLocalParameters.add(klV);

				StringBuffer numerator, denominator;
				if (!parentReaction.getReversible()) {
					numerator = times(createNumerators(reactionNum,
							parentReaction, i, modE, FORWARD));
					denominator = diff(times(createDenominators(reactionNum,
							parentReaction, i, modE, FORWARD)),
							new StringBuffer("1"));
				} else {
					numerator = diff(times(createNumerators(reactionNum,
							parentReaction, i, modE, FORWARD)),
							times(createNumerators(reactionNum, parentReaction,
									i, modE, REVERSE)));
					denominator = diff(sum(times(createDenominators(
							reactionNum, parentReaction, i, modE, FORWARD)),
							times(createDenominators(reactionNum,
									parentReaction, i, modE, REVERSE))),
							new StringBuffer("1"));
				}

				if (modE.size() > 0)
					enzymes[i] = times(new StringBuffer(modE.get(i)), klV,
							frac(numerator, denominator));
				else
					return (times(acti, inhib, frac(numerator, denominator)));
				i++;
			} while (i < enzymes.length);
			return times(acti, inhib, sum(enzymes));
		} catch (IllegalFormatException exc) {
			exc.printStackTrace();
			return new StringBuffer();
		}
	}

	/**
	 * Returns an array containing the factors of the reactants and products
	 * included in the convenience kinetic's numerator. Each factor is given by
	 * the quotient of the respective species' concentration and it's related
	 * equilibrium constant to the power of the species' stoichiometry,
	 * multiplied with the product of equilibrium constant and a dimensionless
	 * energy constant to the power of half the species' stoichiometry. This
	 * method is applicable for both forward and backward reactions.
	 * 
	 * @param reactionNumber
	 * @param reaction
	 * @param enzymeNumber
	 * @param modE
	 * @param type
	 * @return
	 * @throws IllegalFormatException
	 */
	protected StringBuffer[] createNumerators(int reactionNumber,
			PluginReaction reaction, int enzymeNumber, List<String> modE,
			boolean type) throws IllegalFormatException {
		if (type == FORWARD || type == REVERSE) {
			StringBuffer[] nums = new StringBuffer[(type == FORWARD) ? (reaction
					.getNumReactants() + 1)
					: (reaction.getNumProducts() + 1)];
			nums[0] = new StringBuffer((type == FORWARD) ? "kcatp_" : "kcatn_");
			nums[0].append(reactionNumber);
			if (modE.size() > 1) {
				nums[0].append('_');
				nums[0].append(modE.get(enzymeNumber));
			}
			if (!listOfLocalParameters.contains(nums[0]))
				listOfLocalParameters.add(nums[0]);
			for (int i = 1; i < nums.length; i++) {
				PluginSpeciesReference ref = (type == FORWARD) ? reaction
						.getReactant(i - 1) : reaction.getProduct(i - 1);
				StringBuffer kM = new StringBuffer("kM_");
				kM.append(reactionNumber);
				if (modE.size() > 1) {
					kM.append('_');
					kM.append(modE.get(enzymeNumber));
				}
				kM.append('_');
				kM.append(ref.getSpecies());
				if (!listOfLocalParameters.contains(kM))
					listOfLocalParameters.add(kM);

				StringBuffer kiG = new StringBuffer("kG_");
				kiG.append(ref.getSpecies());
				if (!listOfGlobalParameters.contains(kiG))
					listOfGlobalParameters.add(kiG);

				nums[i] = times(pow(frac(getSpecies(ref), kM),
						getStoichiometry(ref)), root(new StringBuffer("2"),
						pow(times(kiG, kM), getStoichiometry(ref))));
			}

			return nums;
		} else {
			throw new IllegalFormatException(
					"Illegal type argument for Convenience kinetic numerators");
		}
	}
}
