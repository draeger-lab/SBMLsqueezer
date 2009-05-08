/*
 * Copyright (c) ZBiT, University of T&uuml;bingen, Germany Compiler:
 *         JDK 1.6.0
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
 * @since 2.0
 * @version
 * @author Nadine Hassis <Nadine.hassis@gmail.com>
 * @author Andreas Dr&auml;ger (draeger) <andreas.draeger@uni-tuebingen.de>
 * @author wouamba <dwouamba@yahoo.fr>
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
		StringBuffer inhib = new StringBuffer();
		StringBuffer acti = new StringBuffer();
		StringBuffer catalysts[] = new StringBuffer[modE.size()];

		PluginReaction reaction = getParentReaction();
		PluginSpeciesReference specRefE1 = (PluginSpeciesReference) reaction
				.getListOfReactants().get(0);
		PluginSpeciesReference specRefP1 = (PluginSpeciesReference) reaction
				.getListOfProducts().get(0);
		PluginSpeciesReference specRefE2 = null, specRefP2 = null;

		if (reaction.getNumReactants() == 2)
			specRefE2 = (PluginSpeciesReference) reaction.getListOfReactants()
					.get(1);
		else if (specRefE1.getStoichiometry() == 2.0)
			specRefE2 = specRefE1;
		else
			throw new RateLawNotApplicableException(
					"Number of reactants must equal two to apply ping-pong "
							+ "Michaelis-Menten kinetics to reaction "
							+ reaction.getId());

		boolean exception = false;
		switch (reaction.getNumProducts()) {
		case 1:
			if (specRefP1.getStoichiometry() == 2.0)
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
					"Number of products must equal two to apply ping-pong"
							+ "Michaelis-Menten kinetics to reaction "
							+ reaction.getId());

		int enzymeNum = 0;
		do {
			StringBuffer kcatp = new StringBuffer();
			StringBuffer kMr1 = concat("kM_", reaction.getId());
			StringBuffer kMr2 = concat("kM_", reaction.getId());
			StringBuffer enzyme = new StringBuffer(modE.get(enzymeNum));

			if (modE.size() == 0) {
				kcatp = concat("Vp_", reaction.getId());

			} else {
				kcatp = concat("kcatp_", reaction.getId());
				if (modE.size() > 1) {
					kcatp = concat(kcatp, Character.valueOf('_'), enzyme);
					kMr1 = concat(kMr1, Character.valueOf('_'), enzyme);
					kMr2 = concat(kMr2, Character.valueOf('_'), enzyme);
				}
			}
			kMr2 = concat(kMr2, Character.valueOf('_'), specRefE2.getSpecies());
			kMr1 = concat(kMr1, Character.valueOf('_'), specRefE2.getSpecies());
			if (specRefE2.equals(specRefE1)) {
				kMr1 = concat("kMr1", kMr1.substring(2));
				kMr2 = concat("kMr2", kMr2.substring(2));
			}

			if (!listOfLocalParameters.contains(kcatp))
				listOfLocalParameters.add(kcatp);
			if (!listOfLocalParameters.contains(kMr2))
				listOfLocalParameters.add(kMr2);
			if (!listOfLocalParameters.contains(kMr1))
				listOfLocalParameters.add(kMr1);

			/*
			 * Irreversible Reaction
			 */
			if (!reaction.getReversible()) {
				numerator = kcatp;

				if (modE.size() > 0)
					numerator = times(numerator, new StringBuffer(modE
							.get(enzymeNum)));
				numerator = times(numerator, new StringBuffer(specRefE1
						.getSpecies()));

				denominator = times(kMr2, sum(new StringBuffer(specRefE1
						.getSpecies()), kMr1), sum(new StringBuffer(specRefE2
						.getSpecies()),
						new StringBuffer(specRefE1.getSpecies())));

				if (specRefE2.equals(specRefE1)) {
					numerator = pow(numerator, new StringBuffer('2'));
					denominator = pow(denominator, new StringBuffer('2'));

				} else {
					numerator = times(numerator, new StringBuffer(specRefE2
							.getSpecies()));
					denominator = times(denominator, new StringBuffer(specRefE2
							.getSpecies()));
				}

				/*
				 * Reversible Reaction
				 */
			} else {
				StringBuffer numeratorForward = new StringBuffer();
				StringBuffer numeratorReverse = new StringBuffer();

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
						kcatn = concat(kcatn, Character.valueOf('_'),
								modEnzymeNumber);
						kMp1 = concat(kMp1, Character.valueOf('_'),
								modEnzymeNumber);
						kMp2 = concat(kMp2, Character.valueOf('_'),
								modEnzymeNumber);
						kIp1 = concat(kIp1, Character.valueOf('_'),
								modEnzymeNumber);
						kIp2 = concat(kIp2, Character.valueOf('_'),
								modEnzymeNumber);
						kIr1 = concat(kIr1, Character.valueOf('_'),
								modEnzymeNumber);
					}
				}
				kMp1 = concat(kMp1, Character.valueOf('_'), specRefP1
						.getSpecies());
				kMp2 = concat(kMp2, Character.valueOf('_'), specRefP2
						.getSpecies());
				kIp1 = concat(kIp1, Character.valueOf('_'), specRefP1
						.getSpecies());
				kIp2 = concat(kIp2, Character.valueOf('_'), specRefP2
						.getSpecies());
				kIr1 = concat(kIr1, Character.valueOf('_'), specRefE1
						.getSpecies());
				if (specRefP2.equals(specRefP1)) {
					kMp1 = concat("kMp1", kMp1.substring(2));
					kMp2 = concat("kMp2", kMp2.substring(2));
					kIp1 = concat("kip1", kIp1.substring(2));
					kIp2 = concat("kip2", kIp2.substring(2));

				}

				if (!listOfLocalParameters.contains(kcatn))
					listOfLocalParameters.add(kcatn);
				if (!listOfLocalParameters.contains(kMp2))
					listOfLocalParameters.add(kMp2);
				if (!listOfLocalParameters.contains(kMp1))
					listOfLocalParameters.add(kMp1);
				if (!listOfLocalParameters.contains(kIp1))
					listOfLocalParameters.add(kIp1);
				if (!listOfLocalParameters.contains(kIp2))
					listOfLocalParameters.add(kIp2);
				if (!listOfLocalParameters.contains(kIr1))
					listOfLocalParameters.add(kIr1);

				numeratorForward = frac(kcatp, times(kIr1, kMr2));
				if (modE.size() > 0)
					numeratorForward = times(numeratorForward,
							new StringBuffer(modE.get(enzymeNum)));
				// StringBuffer denominator_s1s2 = new
				// StringBuffer(specRefE1.getSpecies());
				denominator = sum(frac(
						new StringBuffer(specRefE1.getSpecies()), kIr1), frac(
						times(kMr1, new StringBuffer(specRefE2.getSpecies())),
						times(kIr1, kMr2)), frac(new StringBuffer(specRefP1
						.getSpecies()), kIp1), frac(times(kMp1,
						new StringBuffer(specRefP2.getSpecies())), times(kIp1,
						kMp2)));
				if (specRefE2.equals(specRefE1)) {
					numeratorForward = times(numeratorForward, pow(
							new StringBuffer(specRefE1.getSpecies()),
							new StringBuffer('2')));
					denominator = sum(denominator, frac(pow(new StringBuffer(
							specRefE1.getSpecies()), new StringBuffer('2')),
							times(kIr1, kMr2)));
				} else {
					numeratorForward = times(numeratorForward,
							new StringBuffer(specRefE1.getSpecies()),
							new StringBuffer(specRefE2.getSpecies()));
					denominator = sum(denominator, frac(times(new StringBuffer(
							specRefE1.getSpecies()), new StringBuffer(specRefE2
							.getSpecies())), times(kIr1, kMr2)));
				}
				numeratorReverse = frac(kcatn, times(kIp1, kMp2));

				denominator = sum(denominator, frac(times(new StringBuffer(
						specRefE1.getSpecies()), new StringBuffer(specRefP1
						.getSpecies())), times(kIr1, kIp1)), frac(times(kMr1,
						new StringBuffer(specRefE2.getSpecies()),
						new StringBuffer(specRefP2.getSpecies())), times(kIr1,
						kMr2, kIp2)));

				if (modE.size() > 0)
					numeratorReverse = times(numeratorReverse,
							new StringBuffer(modE.get(enzymeNum)));

				StringBuffer denominator_p1p2 = new StringBuffer(specRefE1
						.getSpecies());
				if (specRefP2.equals(specRefP1)) {
					numeratorReverse = times(numeratorReverse, pow(
							new StringBuffer(specRefP1.getSpecies()),
							new StringBuffer('2')));
					denominator_p1p2 = pow(new StringBuffer(specRefP1
							.getSpecies()), new StringBuffer('2'));

				} else {
					numeratorReverse = times(numeratorReverse,
							new StringBuffer(specRefP1.getSpecies()),
							new StringBuffer(specRefP2.getSpecies()));

					denominator_p1p2 = times(new StringBuffer(specRefP1
							.getSpecies()), new StringBuffer(specRefP2
							.getSpecies()));

				}
				numerator = diff(numeratorForward, numeratorReverse);
				denominator_p1p2 = frac(denominator_p1p2, times(kIp1, kMp2));
				denominator = sum(denominator, denominator_p1p2);
			}
			catalysts[enzymeNum++] = frac(numerator, denominator);
		} while (enzymeNum <= modE.size() - 1);
		acti = createModificationFactor(reaction.getId(), modActi, ACTIVATION);
		inhib = createModificationFactor(reaction.getId(), modInhib, INHIBITION);
		return times(acti, inhib, sum(catalysts));
	}

}
