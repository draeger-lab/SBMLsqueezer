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

/**
 * 
 * @author <a href="mailto:andreas.draeger@uni-tuebingen.de">Andreas
 *         Dr&auml;ger</a>
 * 
 */
public class IrrevCompetNonCooperativeEnzymes extends GeneralizedMassAction {

	private int numInhib;
	private int numOfEnzymes;

	/**
	 * @param parentReaction
	 * @param model
	 * @throws RateLawNotApplicableException
	 * @throws IOException
	 * @throws IllegalFormatException
	 */
	public IrrevCompetNonCooperativeEnzymes(PluginReaction parentReaction,
			PluginModel model) throws RateLawNotApplicableException,
			IOException, IllegalFormatException {
		super(parentReaction, model);
	}

	/**
	 * @param parentReaction
	 * @param model
	 * @param listOfPossibleEnzymes
	 * @throws RateLawNotApplicableException
	 * @throws IOException
	 * @throws IllegalFormatException
	 */
	public IrrevCompetNonCooperativeEnzymes(PluginReaction parentReaction,
			PluginModel model, List<String> listOfPossibleEnzymes)
			throws RateLawNotApplicableException, IOException,
			IllegalFormatException {
		super(parentReaction, model, listOfPossibleEnzymes);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbmlsqueezer.kinetics.BasicKineticLaw#getName()
	 */
	@Override
	public String getName() {
		switch (numInhib) {
		case 0:
			if (numOfEnzymes == 0)
				return "normalised kinetics of unireactant enzymes";
			return "Henri-Michaelis Menten equation";
		case 1:
			return "competitive inhibition of irreversible unireactant enzymes by one inhibitor";
		default:
			return "competitive inhibition of irreversible unireactant enzymes by non-exclusive non-cooperative inhibitors";
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbmlsqueezer.kinetics.BasicKineticLaw#getSBO()
	 */
	// @Override
	public String getSBO() {
		String name = getName().toLowerCase();
		if (name
				.equals("competitive inhibition of irreversible unireactant enzymes by non-exclusive non-cooperative inhibitors"))
			return "0000273";
		if (name
				.equals("competitive inhibition of irreversible unireactant enzymes by one inhibitor"))
			return "0000267";
		if (name.equals("henri-michaelis menten equation"))
			return "0000029";
		return "none";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.sbmlsqueezer.kinetics.BasicKineticLaw#createKineticEquation(jp.sbi
	 * .celldesigner.plugin.PluginModel, int, java.util.List, java.util.List,
	 * java.util.List, java.util.List, java.util.List, java.util.List)
	 */
	// @Override
	protected StringBuffer createKineticEquation(PluginModel model,
			List<String> modE, List<String> modActi, List<String> modTActi,
			List<String> modInhib, List<String> modTInhib, List<String> modCat)
			throws RateLawNotApplicableException, IllegalFormatException {
		if (((modTActi.size() > 0) && (modActi.size() > 0))
				|| ((modInhib.size() > 0) && (modTInhib.size() > 0)))
			throw new RateLawNotApplicableException(
					"Mixture of translational/transcriptional and regular activation/inhibition is not allowed.");
		if ((modCat.size() > 0))
			throw new RateLawNotApplicableException(
					"This rate law can only be applied to enzyme-catalyzed reactions.");
		PluginReaction reaction = getParentReaction();
		if ((reaction.getNumReactants() > 1)
				|| (reaction.getReactant(0).getStoichiometry() != 1.0))
			throw new RateLawNotApplicableException(
					"This rate law can only be applied to reactions with exactly one substrate.");
		if (modTActi.size() > 0)
			modActi = modTActi;
		if (modTInhib.size() > 0)
			modInhib = modTInhib;
		if (reaction.getReversible())
			reaction.setReversible(false);
		numInhib = modInhib.size();

		numOfEnzymes = modE.size();
		StringBuffer formula = new StringBuffer();

		int enzymeNum = 0;
		do {
			StringBuffer kcat = new StringBuffer();
			if (numOfEnzymes == 0)
				kcat = concat("V_", reaction.getId());
			else {
				kcat = concat("kcat_", reaction.getId());
				if (modE.size() > 1)
					kcat = concat(kcat, underscore, modE.get(enzymeNum));
			}
			addLocalParameter(kcat);
			addLocalParameter(kcat);
			StringBuffer currEnzyme = new StringBuffer();
			StringBuffer numerator = new StringBuffer();


			numerator = times(numerator, kcat);
			numerator = times(numerator, new StringBuffer(reaction.getReactant(
					0).getSpecies()));

			StringBuffer denominator = new StringBuffer();
			StringBuffer kM = new StringBuffer(concat("kM_", reaction.getId()));

			if (numOfEnzymes > 1)

				kM = concat(kM, underscore, modE.get(enzymeNum));
			kM = concat(kM, underscore, reaction.getReactant(0).getSpecies());
			addLocalParameter(kM);

			if (modInhib.size() == 0)
				denominator = sum(denominator, kM);
			else {
				StringBuffer factor = new StringBuffer();
				factor = times(factor, kM);
				for (int i = 0; i < modInhib.size(); i++) {

					StringBuffer kIi = new StringBuffer(concat("Ki_", reaction
							.getId()));
					StringBuffer exponent = concat("m_", reaction.getId());
					if (numOfEnzymes > 1) {
						kIi = concat(underscore, modE.get(enzymeNum));
						exponent = concat(underscore, modE.get(enzymeNum));
					}
					kIi = concat(kIi, underscore, modInhib.get(i));
					exponent = concat(exponent, underscore, modInhib.get(i));
					addLocalParameter(kIi);
					addLocalParameter(exponent);

					factor = times(factor, pow(sum(Integer.toString(1), frac(
							new StringBuffer(modInhib.get(i)), kIi)), exponent));
					System.out.println("factor:\t"+factor);
				}
				denominator = sum(denominator, factor);

			}
			denominator = sum(denominator, new StringBuffer(reaction
					.getReactant(0).getSpecies()));
			currEnzyme = frac(numerator, denominator);
			System.out.println("currEnzyme "+currEnzyme);
			System.out.println("formula "+ formula);
			enzymeNum++;
			if (numOfEnzymes <= 1)
				formula = currEnzyme;
			else {

				formula = sum(formula, currEnzyme);
				numerator = times(numerator, new StringBuffer(modE
						.get(enzymeNum)));
			}

		} while (enzymeNum <= modE.size() - 1);
	
		return times(activationFactor(modActi), formula);
	}

}
