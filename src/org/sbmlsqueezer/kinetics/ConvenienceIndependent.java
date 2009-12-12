package org.sbmlsqueezer.kinetics;

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
 * @author Andreas Dr&auml;ger <andreas.draeger@uni-tuebingen.de> Copyright (c)
 *         ZBiT, University of T&uuml;bingen, Germany
 * @date Aug 1, 2007
 */
public class ConvenienceIndependent extends BasicKineticLaw {

	/**
	 * @param parentReaction
	 * @param model
	 * @param reversibility
	 * @throws RateLawNotApplicableException
	 */
	public ConvenienceIndependent(PluginReaction parentReaction,
			PluginModel model) throws RateLawNotApplicableException {
		super(parentReaction, model);
	}

	/**
	 * @param parentReaction
	 * @param model
	 * @param listOfPossibleEnzymes
	 * @param reversibility
	 * @throws RateLawNotApplicableException
	 */
	public ConvenienceIndependent(PluginReaction parentReaction,
			PluginModel model, List<String> listOfPossibleEnzymes)
			throws RateLawNotApplicableException {
		super(parentReaction, model, listOfPossibleEnzymes);
	}

	@Override
	protected String createKineticEquation(PluginModel model, int reactionNum,
			List<String> modE, List<String> modActi, List<String> modTActi,
			List<String> modInhib, List<String> modTInhib, List<String> modCat)
			throws RateLawNotApplicableException {
		String formelTxt = formelTeX = "";
		PluginReaction reaction = getParentReaction();

		int enzymeNum = 0;
		reactionNum++;
		do {
			String denominator = "", denominatorTeX = "";
			String numerator = "", numeratorTeX = "";
			String kiG = "", kiGTeX = "";
			String kM = "", kMTeX = "";
			String klV = "kV_" + reactionNum;
			String klVTeX = "k^\\text{V}_{" + reactionNum;
			String independenceP = "", independencePTeX = "", independenceN = "", independenceNTeX = "";

			if (modE.size() > 1) {
				klV += "_" + modE.get(enzymeNum);
				klVTeX += ",{" + Species.idToTeX(modE.get(enzymeNum)) + "}";
			}
			klVTeX += "}";
			if (!listOfLocalParameters.contains(klV))
				listOfLocalParameters.add(klV);

			/*
			 * ==================================================================
			 * ============ Construct thermodynamically independent parameters
			 */
			for (int productNum = 0; productNum < reaction.getNumProducts(); productNum++) {
				PluginSpeciesReference currentSpecRef = reaction
						.getProduct(productNum);
				kiG = "kG_" + reaction.getProduct(productNum).getSpecies();
				kiGTeX = "k^\\text{G}_\\text{"
						+ reaction.getProduct(productNum).getSpecies() + "}";

				/*
				 * extremely important: if the parameter list already contains
				 * this parameter we don't want to add it again since this
				 * parameter can only occur once for each reacting species!
				 */
				if (!listOfGlobalParameters.contains(kiG))
					listOfGlobalParameters.add(kiG);

				kM = "kM_" + reactionNum;
				kMTeX = "k^\\text{M}_{" + reactionNum;
				if (modE.size() > 1) {
					kM += "_" + modE.get(enzymeNum);
					kMTeX += ",{" + Species.idToTeX(modE.get(enzymeNum)) + "}";
				}
				kM += "_" + reaction.getProduct(productNum).getSpecies();
				kMTeX += ",{"
						+ Species.idToTeX(reaction.getProduct(productNum)
								.getSpecies()) + "}}";
				if (!listOfLocalParameters.contains(kM))
					listOfLocalParameters.add(kM);

				kiG += " * " + kM;
				kiGTeX += kMTeX;
				if ((0 < independenceN.length())
						&& (!independenceN.endsWith(" * "))) {
					independenceN += " * ";
					independenceNTeX += "\\cdot ";
				}
				if (currentSpecRef.getStoichiometry() == 1d) {
					independenceN += kiG;
					independenceNTeX += kiGTeX;
				} else {
					independenceN += "power(" + kiG + ", "
							+ currentSpecRef.getStoichiometry() + "/2)";
					independenceNTeX += "\\left(" + kiGTeX
							+ "\\right)^{\\frac{";
					if (currentSpecRef.getStoichiometry()
							- ((int) currentSpecRef.getStoichiometry()) == 0)
						independenceNTeX += ((int) currentSpecRef
								.getStoichiometry());
					else
						independenceNTeX += currentSpecRef.getStoichiometry();
					independenceNTeX += "}{2}}";
				}
				if (productNum < reaction.getNumProducts() - 1)
					independenceN += " * ";
			}
			/*
			 * ==================================================================
			 * =============
			 */

			/*
			 * Construct equation Iteration over all educts
			 */
			for (int eductNum = 0; eductNum < reaction.getNumReactants(); eductNum++) {
				String exp = "";
				PluginSpeciesReference currentSpecRef = reaction
						.getReactant(eductNum);

				// thermodynamically independent constants:
				kiG = "kG_" + currentSpecRef.getSpecies();
				kiGTeX = "k^\\text{G}_\\text{" + currentSpecRef.getSpecies()
						+ "}";
				/*
				 * kiG may only be included one time for each species in the
				 * whole model!
				 */
				if (!listOfGlobalParameters.contains(kiG))
					listOfGlobalParameters.add(kiG);

				kM = "kM_" + reactionNum;
				kMTeX = "k^\\text{M}_{" + reactionNum;
				if (modE.size() > 1) {
					kM += "_" + modE.get(enzymeNum);
					kMTeX += ",{" + Species.idToTeX(modE.get(enzymeNum)) + "}";
				}
				kM += "_" + currentSpecRef.getSpecies();
				kMTeX += ",{" + Species.idToTeX(currentSpecRef.getSpecies())
						+ "}}";
				if (!listOfLocalParameters.contains(kM))
					listOfLocalParameters.add(kM);

				kiG += " * " + kM;
				kiGTeX += kMTeX;
				if ((0 < independenceP.length())
						&& (!independenceP.endsWith(" * "))) {
					independenceP += " * ";
					independencePTeX += "\\cdot ";
				}
				if (currentSpecRef.getStoichiometry() == 1d) {
					independenceP += kiG;
					independencePTeX += kiGTeX;
				} else {
					independenceP += "power(" + kiG + ", "
							+ currentSpecRef.getStoichiometry() + "/2)";
					independencePTeX += "\\left(" + kiGTeX + "\\right)^{";
					if (currentSpecRef.getStoichiometry()
							- ((int) currentSpecRef.getStoichiometry()) == 0)
						independencePTeX += (int) currentSpecRef
								.getStoichiometry();
					else
						independencePTeX += currentSpecRef.getStoichiometry();
					independencePTeX += "}";
				}

				// we can save the brakets if there is just one educt.
				if (reaction.getNumReactants() > 1) {
					denominator += "(";
					denominatorTeX += "\\left(";
				}
				if (!reaction.getReversible()
						|| ((reaction.getNumReactants() != 1) || (reaction
								.getNumProducts() == 1))) {
					denominator += " 1 + ";
					denominatorTeX += "1 + ";
				}
				denominator += '(' + currentSpecRef.getSpecies() + '/' + kM + ')';
				denominatorTeX += "\\frac{"
						+ Species.toTeX(currentSpecRef.getSpecies()) + "}{"
						+ kMTeX + "}";

				/*
				 * for each stoichiometry (see Liebermeister et al.)
				 */
				for (int m = 1; m < (int) currentSpecRef.getStoichiometry(); m++) {
					exp = "^(" + (m + 1) + ')';
					denominator += " + (" + currentSpecRef.getSpecies() + '/'
							+ kM + ')' + exp;
					denominatorTeX += " + \\left(\\frac{"
							+ Species.toTeX(currentSpecRef.getSpecies()) + "}{"
							+ kMTeX + "}\\right)"
							+ exp.replace("(", "{").replace(")", "}");
				}

				// we can save the brakets if there is just one educt.
				if (reaction.getNumReactants() > 1) {
					denominatorTeX += "\\right)";
					denominator += ')';
				}
				if ((eductNum + 1) < reaction.getNumReactants()) {
					denominator += " * ";
					// denominatorTeX += "\\cdot ";
				}

				/*
				 * build numerator.
				 */
				if (exp.length() > 0) {
					numerator += '(' + currentSpecRef.getSpecies() + '/' + kM
							+ ')' + exp;
					numeratorTeX += "\\left(\\frac{"
							+ Species.toTeX(currentSpecRef.getSpecies()) + "}{"
							+ kMTeX + "}\\right)"
							+ exp.replace("(", "{").replace(")", "}");
				} else {
					numerator += '(' + currentSpecRef.getSpecies() + '/' + kM + ')';
					numeratorTeX += "\\frac{"
							+ Species.toTeX(currentSpecRef.getSpecies()) + "}{"
							+ kMTeX + "}";
				}

				if (eductNum < (reaction.getNumReactants() - 1)) {
					numerator += " * ";
					numeratorTeX += "\\cdot ";
				}
			}
			numerator += " * root(2, (" + independenceP + ")/(" + independenceN
					+ "))";
			numeratorTeX += "\\sqrt{\\frac{" + independencePTeX + "}{"
					+ independenceNTeX + "}}";

			/*
			 * Reverse Reaction. Only if reaction is reversible or we want it to
			 * be.
			 */
			if (reaction.getReversible()) {
				numerator += " - ";
				numeratorTeX += "-";
				denominator += " + ";
				denominatorTeX += " + ";

				// for each product
				for (int productNum = 0; productNum < reaction.getNumProducts(); productNum++) {
					String exp = "";
					PluginSpeciesReference specref = reaction
							.getProduct(productNum);

					kM = "kM_" + reactionNum;
					kMTeX = "k^\\text{M}_{" + reactionNum;
					if (modE.size() > 1) {
						kM += "_" + modE.get(enzymeNum);
						kMTeX += ",{" + Species.idToTeX(modE.get(enzymeNum))
								+ "}";
					}
					kM += "_" + reaction.getProduct(productNum).getSpecies();
					kMTeX += ",{"
							+ Species.idToTeX(reaction.getProduct(productNum)
									.getSpecies()) + "}}";
					if (!listOfLocalParameters.contains(kM))
						listOfLocalParameters.add(kM);

					if (reaction.getNumProducts() > 1) {
						denominator += "(1 + ";
						denominatorTeX += "\\left(1 + ";
					}
					denominator += '(' + specref.getSpecies() + '/' + kM + ')';
					denominatorTeX += "\\frac{"
							+ Species.toTeX(specref.getSpecies()) + "}{"
							+ kMTeX + "}";

					// for each stoichiometry (see Liebermeister et al.)
					for (int m = 1; m < (int) specref.getStoichiometry(); m++) {
						exp = "^(" + (m + 1) + ')';
						denominator += " + (" + specref.getSpecies() + '/' + kM
								+ ')' + exp;
						denominatorTeX += " + \\left(\\frac{"
								+ Species.toTeX(specref.getSpecies()) + "}{"
								+ kMTeX + "}\\right)"
								+ exp.replace("(", "{").replace(")", "}");
					}

					if (reaction.getNumProducts() > 1) {
						denominatorTeX += "\\right)";
						denominator += ')';
					}

					if ((productNum + 1) < reaction.getNumProducts()) {
						denominator += " * ";
						// denominatorTeX += "\\cdot ";
					}

					// build numerator
					if (exp.length() > 0) {
						numerator += '(' + specref.getSpecies() + '/' + kM
								+ ')' + exp;
						numeratorTeX += "\\left(\\frac{"
								+ Species.toTeX(specref.getSpecies()) + "}{"
								+ kMTeX + "}\\right)"
								+ exp.replace("(", "{").replace(")", "}");
					} else {
						numerator += '(' + specref.getSpecies() + '/' + kM + ')';
						numeratorTeX += "\\frac{"
								+ Species.toTeX(specref.getSpecies()) + "}{"
								+ kMTeX + "}";
					}

					if (productNum < (reaction.getNumProducts() - 1)) {
						numerator += " * ";
						numeratorTeX += "\\cdot ";
					}
				}

				numerator += " * root(2, (" + independenceN + ")/("
						+ independenceP + "))";
				numeratorTeX += "\\sqrt{\\frac{" + independenceNTeX + "}{"
						+ independencePTeX + "}}";

				if ((reaction.getNumProducts() > 1)
						&& (reaction.getNumReactants() > 1)) {
					denominator += " -1";
					denominatorTeX += " -1";
				}
			}
			if (modE.size() > 0) {
				formelTxt += modE.get(enzymeNum) + " * ";
				formelTeX += Species.toTeX(modE.get(enzymeNum)) + "\\cdot ";
			}
			formelTxt += klV + " * ((" + numerator + ")/("
					+ denominator + "))";
			formelTeX += klVTeX + "\\cdot\\frac{" + numeratorTeX + "}{"
					+ denominatorTeX + "}";
			if (enzymeNum < (modE.size()) - 1) {
				formelTxt += " + ";
				formelTeX += "\\\\+";
			}
			enzymeNum++;
		} while (enzymeNum <= modE.size() - 1);

		String inhib = "";
		String inhibTeX = "";
		String acti = "";
		String actiTeX = "";

		/*
		 * Activation
		 */
		if (!modActi.isEmpty()) {
			for (int activatorNum = 0; activatorNum < modActi.size(); activatorNum++) {
				String kA = "kA_" + reactionNum;
				String kATeX = "k^\\text{A}_{" + reactionNum;
				kA += "_" + modActi.get(activatorNum);
				kATeX += ",\\text{" + modActi.get(activatorNum) + "}}";
				if (!listOfLocalParameters.contains(kA))
					listOfLocalParameters.add(kA);
				acti += '(' + modActi.get(activatorNum) + "/(" + kA + " + "
						+ modActi.get(activatorNum) + ")) * ";
				actiTeX += "\\frac{" + Species.toTeX(modActi.get(activatorNum))
						+ "}{" + kATeX + "+"
						+ Species.toTeX(modActi.get(activatorNum)) + "}\\cdot ";
			}
		}
		/*
		 * Inhibition
		 */
		if (!modInhib.isEmpty()) {
			for (int inhibitorNum = 0; inhibitorNum < modInhib.size(); inhibitorNum++) {
				String kI = "kI_" + reactionNum, kITeX = "k^\\text{I}_{"
						+ reactionNum;
				kI += "_" + modInhib.get(inhibitorNum);
				kITeX += ",\\text{" + modInhib.get(inhibitorNum) + "}}";
				if (!listOfLocalParameters.contains(kI))
					listOfLocalParameters.add(kI);
				inhib += '(' + kI + "/(" + kI + " + " + modInhib.get(inhibitorNum)
						+ ")) * ";
				inhibTeX += "\\frac{" + kITeX + "}{" + kITeX + "+"
						+ Species.toTeX(modInhib.get(inhibitorNum))
						+ "}\\cdot ";
			}
		}
		if ((acti.length() + inhib.length() > 0) && (modE.size() > 1)) {
			inhib += '(';
			formelTxt += ')';
			inhibTeX += inhibTeX.substring(0, inhibTeX.length() - 6)
					+ "\\\\\\cdot\\left(";
			formelTeX = formelTeX.replaceAll("\\\\\\+",
					"\\right.\\\\\\\\+\\\\left.")
					+ "\\right)";
		}
		formelTxt = acti + inhib + formelTxt;
		formelTeX = actiTeX + inhibTeX + formelTeX;

		if (enzymeNum > 1)
			formelTeX += "\\end{multline}";
		return formelTxt;
	}

	@Override
	public String getName() {
		if (this.getParentReaction().getReversible())
			return "reversible thermodynamically independent convenience kinetics";
		return "irreversible thermodynamically independent convenience kinetics";
	}

	public static boolean isApplicable(PluginReaction reaction) {
		// TODO
		return true;
	}

	@Override
	public String getSBO() {
		return "none";
	}

}
