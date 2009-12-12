package org.sbmlsqueezer.kinetics;

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
 *         Copyright (c) ZBiT, University of T&uuml;bingen, Germany Compiler:
 *         JDK 1.6.0
 * @date Aug 1, 2007
 */
public class OrderedMechanism extends BasicKineticLaw {

	/**
	 * @param parentReaction
	 * @param model
	 * @param reversibility
	 * @param listOfPossibleEnzymes
	 * @throws RateLawNotApplicableException
	 */
	public OrderedMechanism(PluginReaction parentReaction, PluginModel model,
			List<String> listOfPossibleEnzymes)
			throws RateLawNotApplicableException {
		super(parentReaction, model, listOfPossibleEnzymes);
	}

	/**
	 * @param parentReaction
	 * @param model
	 * @param reversibility
	 * @throws RateLawNotApplicableException
	 */
	public OrderedMechanism(PluginReaction parentReaction, PluginModel model,
			boolean reversibility) throws RateLawNotApplicableException {
		super(parentReaction, model);
	}

	@Override
	protected String createKineticEquation(PluginModel model, int reactionNum,
			List<String> modE, List<String> modActi, List<String> modTActi,
			List<String> modInhib, List<String> modTInhib, List<String> modCat)
			throws RateLawNotApplicableException {
		String numerator = "", numeratorTeX = ""; // I
		String denominator = "", denominatorTeX = ""; // II
		String inhib = "", inhibTeX = "", acti = "", actiTeX = "";
		String formelTxt = formelTeX = "";

		PluginReaction reaction = getParentReaction();
		PluginSpeciesReference specRefE1 = (PluginSpeciesReference) reaction
				.getListOfReactants().get(0), specRefE2 = null;
		PluginSpeciesReference specRefP1 = (PluginSpeciesReference) reaction
				.getListOfProducts().get(0), specRefP2 = null;

		if (reaction.getNumReactants() == 2)
			specRefE2 = (PluginSpeciesReference) reaction.getListOfReactants()
					.get(1);
		else if (specRefE1.getStoichiometry() == 2.0)
			specRefE2 = specRefE1;
		else
			throw new RateLawNotApplicableException(
					"Number of reactants must equal two to apply ordered "
							+ "Michaelis-Menten kinetics to reaction "
							+ reaction.getId());

		boolean exception = false, biuni = false;
		switch (reaction.getNumProducts()) {
		case 1:
			if (specRefP1.getStoichiometry() == 1d)
				biuni = true;
			else if (specRefP1.getStoichiometry() == 2d)
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
					"Number of products must equal either one or two to apply ordered "
							+ "Michaelis-Menten kinetics to reaction "
							+ reaction.getId());

		int enzymeNum = 0;
		reactionNum++;
		do {
			/*
			 * Variables that are needed for the different combinations of
			 * educts and prodcuts.
			 */
			String kcatp, kcatpTeX;
			String kMr1 = "kM_" + reactionNum;
			String kMr2 = "kM_" + reactionNum;
			String kIr1 = "ki_" + reactionNum;
			String kMr1TeX = "k^\\text{M}_{" + reactionNum;
			String kMr2TeX = "k^\\text{M}_{" + reactionNum;
			String kIr1TeX = "k^\\text{i}_{" + reactionNum;
			// reverse reactions
			String kcatn, kcatnTeX;
			String kMp1 = "kM_" + reactionNum;
			String kMp2 = "kM_" + reactionNum;
			String kIp1 = "ki_" + reactionNum;
			String kIp2 = "ki_" + reactionNum;
			String kIr2 = "ki_" + reactionNum;
			String kMp1TeX = "k^\\text{M}_{" + reactionNum;
			String kMp2TeX = "k^\\text{M}_{" + reactionNum;
			String kIp1TeX = "k^\\text{i}_{" + reactionNum;
			String kIp2TeX = "k^\\text{i}_{" + reactionNum;
			String kIr2TeX = "k^\\text{i}_{" + reactionNum;

			if (modE.size() == 0) {
				kcatp = "Vp_" + reactionNum;
				kcatpTeX = "V^\\text{m}_{+" + reactionNum;
				// reverse reactions
				kcatn = "Vn_" + reactionNum;
				kcatnTeX = "V^\\text{m}_{-" + reactionNum;
			} else {
				kcatp = "kcatp_" + reactionNum;
				kcatpTeX = "k^\\text{cat}_{+" + reactionNum;
				//
				kcatn = "kcatn_" + reactionNum;
				kcatnTeX = "k^\\text{cat}_{-" + reactionNum;
				if (modE.size() > 1) {
					kcatp += "_" + modE.get(enzymeNum);
					kMr2 += "_" + modE.get(enzymeNum);
					kMr1 += "_" + modE.get(enzymeNum);
					kIr1 += "_" + modE.get(enzymeNum);
					kcatpTeX += ",{" + Species.idToTeX(modE.get(enzymeNum))
							+ '}';
					kMr2TeX += ",{" + Species.idToTeX(modE.get(enzymeNum))
							+ '}';
					kIr1TeX += ",{" + Species.idToTeX(modE.get(enzymeNum))
							+ '}';
					kMr1TeX += ",{" + Species.idToTeX(modE.get(enzymeNum))
							+ '}';
					// reverse reactions
					kcatn += "_" + modE.get(enzymeNum);
					kMp2 += "_" + modE.get(enzymeNum);
					kMp1 += "_" + modE.get(enzymeNum);
					kIp1 += "_" + modE.get(enzymeNum);
					kIp2 += "_" + modE.get(enzymeNum);
					kIr2 += "_" + modE.get(enzymeNum);
					kcatnTeX += ",{" + Species.idToTeX(modE.get(enzymeNum))
							+ '}';
					kMp2TeX += ",{" + Species.idToTeX(modE.get(enzymeNum))
							+ '}';
					kIp1TeX += ",{" + Species.idToTeX(modE.get(enzymeNum))
							+ '}';
					kIp2TeX += ",{" + Species.idToTeX(modE.get(enzymeNum))
							+ '}';
					kIr2TeX += ",{" + Species.idToTeX(modE.get(enzymeNum))
							+ '}';
					kMp1TeX += ",{" + Species.idToTeX(modE.get(enzymeNum))
							+ '}';
				}
			}
			kMr2 += "_" + specRefE2.getSpecies();
			kMr1 += "_" + specRefE1.getSpecies();
			// reverse reactions
			if (specRefP2 != null)
				kMp2 += "_" + specRefP2.getSpecies();
			kMp1 += "_" + specRefP1.getSpecies();

			if (specRefE2.equals(specRefE1)) {
				kMr1 = "kMr1" + kMr1.substring(2);
				kMr2 = "kMr2" + kMr2.substring(2);
				kMr1TeX = "k^\\text{Mr1" + kMr1TeX.substring(9);
				kMr2TeX = "k^\\text{Mr2" + kMr2TeX.substring(9);
			}
			// reverse reactions
			kIp1 += "_" + specRefP1.getSpecies();
			if (specRefP2 != null) {
				if (specRefP2.equals(specRefP1)) {
					kMp1 = "kMp1" + kMp1.substring(2);
					kMp2 = "kMp2" + kMp2.substring(2);
					kMp1TeX = "k^\\text{Mp1" + kMp1TeX.substring(9);
					kMp2TeX = "k^\\text{Mr2" + kMp2TeX.substring(9);
				}
				kIp2 += "_" + specRefP2.getSpecies();
				kMp2TeX += ",{" + Species.idToTeX(specRefP2.getSpecies())
						+ "}}";
				kIp2TeX += ",{" + Species.idToTeX(specRefP2.getSpecies())
						+ "}}";
			}
			kIr1 += "_" + specRefE1.getSpecies();
			kcatpTeX += '}';
			kMr2TeX += ",{" + Species.idToTeX(specRefE2.getSpecies()) + "}}";
			kIr1TeX += ",{" + Species.idToTeX(specRefE1.getSpecies()) + "}}";
			kMr1TeX += ",{" + Species.idToTeX(specRefE1.getSpecies()) + "}}";

			// reverse reactions
			kIr2 += "_" + specRefE2.getSpecies();
			kcatnTeX += '}';
			kIp1TeX += ",{" + Species.idToTeX(specRefP1.getSpecies()) + "}}";
			kIr2TeX += ",{" + Species.idToTeX(specRefE2.getSpecies()) + "}}";
			kMp1TeX += ",{" + Species.idToTeX(specRefP1.getSpecies()) + "}}";

			if (!listOfLocalParameters.contains(kcatp))
				listOfLocalParameters.add(kcatp);

			/*
			 * Irreversible reaction
			 */
			if (!reaction.getReversible()) {
				if (!listOfLocalParameters.contains(kMr2))
					listOfLocalParameters.add(kMr2);
				if (!listOfLocalParameters.contains(kMr1))
					listOfLocalParameters.add(kMr1);
				if (!listOfLocalParameters.contains(kIr1))
					listOfLocalParameters.add(kIr1);

				numerator = kcatp + " * ";
				numeratorTeX = kcatpTeX;
				if (modE.size() > 0) {
					numerator += kcatp + " * " + modE.get(enzymeNum) + " * ";
					numeratorTeX += Species.toTeX(modE.get(enzymeNum));
				}
				numerator += " * " + specRefE1.getSpecies();
				numeratorTeX += Species.toTeX(specRefE1.getSpecies());
				denominator = kIr1 + " * " + kMr2 + " + ";
				denominatorTeX = kIr1TeX + kMr2TeX + "+";
				if (specRefE2.equals(specRefE1)) {
					numerator += "^2";
					numeratorTeX += "^2";
					denominator += '(' + kMr2 + " + " + kMr1 + ") * "
							+ specRefE1.getSpecies() + " + "
							+ specRefE1.getSpecies() + "^2";
					denominatorTeX += "\\left(" + kMr2TeX + " + " + kMr1TeX
							+ "\\right)"
							+ Species.toTeX(specRefE1.getSpecies()) + " + "
							+ Species.toTeX(specRefE1.getSpecies()) + "^2";
				} else {
					numerator += " * " + specRefE2.getSpecies();
					numeratorTeX += Species.toTeX(specRefE2.getSpecies());
					denominator += kMr2 + " * " + specRefE1.getSpecies()
							+ " + " + kMr1 + " * " + specRefE2.getSpecies()
							+ " + " + specRefE1.getSpecies() + " * "
							+ specRefE2.getSpecies();
					denominatorTeX += kMr2TeX
							+ Species.toTeX(specRefE1.getSpecies()) + " + "
							+ kMr1TeX + Species.toTeX(specRefE2.getSpecies())
							+ " + " + Species.toTeX(specRefE1.getSpecies())
							+ Species.toTeX(specRefE2.getSpecies());
				}

			} else if (!biuni) {
				/*
				 * Reversible Bi-Bi reaction.
				 */
				if (!listOfLocalParameters.contains(kIr2))
					listOfLocalParameters.add(kIr2);
				if (!listOfLocalParameters.contains(kcatn))
					listOfLocalParameters.add(kcatn);
				if (!listOfLocalParameters.contains(kMr1))
					listOfLocalParameters.add(kMr1);
				if (!listOfLocalParameters.contains(kMr2))
					listOfLocalParameters.add(kMr2);
				if (!listOfLocalParameters.contains(kMp1))
					listOfLocalParameters.add(kMp1);
				if (!listOfLocalParameters.contains(kMp2))
					listOfLocalParameters.add(kMp2);
				if (!listOfLocalParameters.contains(kIr1))
					listOfLocalParameters.add(kIr1);
				if (!listOfLocalParameters.contains(kIp1))
					listOfLocalParameters.add(kIp1);
				if (!listOfLocalParameters.contains(kIp2))
					listOfLocalParameters.add(kIp2);

				numerator = "((" + kcatp + " * ";
				numeratorTeX = "\\frac{" + kcatpTeX + "}{" + kIr1TeX + kMr2TeX
						+ '}';
				if (modE.size() > 0) {
					numerator += modE.get(enzymeNum) + " * ";
					numeratorTeX += Species.toTeX(modE.get(enzymeNum));
				}

				numerator += specRefE1.getSpecies();
				numeratorTeX += Species.toTeX(specRefE1.getSpecies());
				denominator = "1 + (" + specRefE1.getSpecies() + '/' + kIr1
						+ ") + ((" + kMr1 + " * " + specRefE2.getSpecies()
						+ ")/(" + kIr1 + " * " + kMr2 + "))" + " + ((" + kMp2
						+ " * " + specRefP1.getSpecies() + ")/(" + kIp2 + " * "
						+ kMp1 + ")) + (" + specRefP2.getSpecies() + '/' + kIp2
						+ ") + (";
				denominatorTeX = "1+\\frac{"
						+ Species.toTeX(specRefE1.getSpecies()) + "}{"
						+ kIr1TeX + "}+\\frac{" + kMr1TeX
						+ Species.toTeX(specRefE2.getSpecies()) + "}{"
						+ kIr1TeX + kMr2TeX + "}+\\frac{" + kMp2TeX
						+ Species.toTeX(specRefP1.getSpecies()) + "}{"
						+ kIp2TeX + kMp1TeX + "}+\\frac{"
						+ Species.toTeX(specRefP2.getSpecies()) + "}{"
						+ kIp2TeX + "}+\\frac{"
						+ Species.toTeX(specRefE1.getSpecies());
				if (specRefE2.equals(specRefE1)) {
					numerator += "^2";
					numeratorTeX += "^2";
					denominator += specRefE1.getSpecies() + "^2";
					denominatorTeX += "^2";
				} else {
					numerator += " * " + specRefE2.getSpecies();
					numeratorTeX += Species.toTeX(specRefE2.getSpecies());
					denominator += '(' + specRefE1.getSpecies() + " * "
							+ specRefE2.getSpecies() + ')';
					denominatorTeX += Species.toTeX(specRefE2
							.getSpeciesInstance().getId());
				}
				numerator += ")/(" + kIr1 + " * " + kMr2 + "))" + " - ((" + kcatn
						+ " * ";
				numeratorTeX += " - \\frac{" + kcatnTeX + "}{" + kIp2TeX
						+ kMp1TeX + '}';
				if (modE.size() > 0) {
					numerator += modE.get(enzymeNum) + " * ";
					numeratorTeX += Species.toTeX(modE.get(enzymeNum));
				}
				numerator += specRefP1.getSpecies();
				numeratorTeX += Species.toTeX(specRefP1.getSpecies());
				if (specRefP2.equals(specRefP1)) {
					numerator += "^2";
					numeratorTeX += "^2";
				} else {
					numerator += " * " + specRefP2.getSpecies();
					numeratorTeX += Species.toTeX(specRefP2.getSpecies());
				}
				numerator += ")/(" + kIp2 + " * " + kMp1 + "))";
				denominator += "/(" + kIr1 + " * " + kMr2 + ")) + ((" + kMp2
						+ " * " + specRefE1.getSpecies() + " * "
						+ specRefP1.getSpecies() + ")/(" + kIr1 + " * " + kMp1
						+ " * " + kIp2 + ")) + ((" + kMr1 + " * "
						+ specRefE2.getSpecies() + " * "
						+ specRefP2.getSpecies() + ")/(" + kIr1 + " * " + kMr2
						+ " * " + kIp2 + ")) + (";
				denominatorTeX += "}{" + kIr1TeX + kMr2TeX + "}+\\frac{"
						+ kMp2TeX + Species.toTeX(specRefE1.getSpecies())
						+ Species.toTeX(specRefP1.getSpecies()) + "}{"
						+ kIr1TeX + kMp1TeX + kIp2TeX + "}+\\frac{" + kMr1TeX
						+ Species.toTeX(specRefE2.getSpecies())
						+ Species.toTeX(specRefP2.getSpecies()) + "}{"
						+ kIr1TeX + kMr2TeX + kIp2TeX + "}+\\frac{"
						+ Species.toTeX(specRefP1.getSpecies());
				if (specRefP2.equals(specRefP1)) {
					denominator += specRefP1.getSpecies() + "^2";
					denominatorTeX += "^2";
				} else {
					denominator += '(' + specRefP1.getSpecies() + " * "
							+ specRefP2.getSpecies() + ')';
					denominatorTeX += Species.toTeX(specRefP2
							.getSpeciesInstance().getId());
				}
				denominator += "/(" + kMp1 + " * " + kIp2 + ")) + (("
						+ specRefE1.getSpecies();
				denominatorTeX += "}{" + kMp1TeX + kIp2TeX + "}+\\frac{"
						+ Species.toTeX(specRefE1.getSpecies());
				if (specRefE2.equals(specRefE1)) {
					denominator += "^2";
					denominatorTeX += "^2";
				} else {
					denominator += " * " + specRefE2.getSpecies();
					denominatorTeX += Species.toTeX(specRefE2
							.getSpeciesInstance().getId());
				}
				denominator += " * " + specRefP1.getSpecies() + ")/(" + kIr1
						+ " * " + kMr2 + " * " + kIp1 + ")) + (("
						+ specRefE2.getSpecies() + " * "
						+ specRefP1.getSpecies();
				denominatorTeX += Species.toTeX(specRefP1.getSpecies()) + "}{"
						+ kIr1TeX + kMr2TeX + kIp1TeX + "}+\\frac{"
						+ Species.toTeX(specRefE2.getSpecies())
						+ Species.toTeX(specRefP1.getSpecies());
				if (specRefP2.equals(specRefP1)) {
					denominator += "^2";
					denominatorTeX += "^2";
				} else {
					denominator += " * " + specRefP2.getSpecies();
					denominatorTeX += Species.toTeX(specRefP2
							.getSpeciesInstance().getId());
				}
				denominator += ")/(" + kIr2 + " * " + kMp1 + " * " + kIp2 + "))";
				denominatorTeX += "}{" + kIr2TeX + kMp1TeX + kIp2TeX + '}';

			} else {
				/*
				 * Reversible bi-uni reaction
				 */
				if (!listOfLocalParameters.contains(kcatn))
					listOfLocalParameters.add(kcatn);
				if (!listOfLocalParameters.contains(kMr1))
					listOfLocalParameters.add(kMr1);
				if (!listOfLocalParameters.contains(kMr2))
					listOfLocalParameters.add(kMr2);
				if (!listOfLocalParameters.contains(kMp1))
					listOfLocalParameters.add(kMp1);
				if (!listOfLocalParameters.contains(kIr1))
					listOfLocalParameters.add(kIr1);
				if (!listOfLocalParameters.contains(kIp1))
					listOfLocalParameters.add(kIp1);

				numerator = "((" + kcatp + " * ";
				numeratorTeX = "\\frac{" + kcatpTeX + "}{" + kIr1TeX + kMr2TeX
						+ '}';
				if (modE.size() > 0) {
					numerator += modE.get(enzymeNum) + " * ";
					numeratorTeX += Species.toTeX(modE.get(enzymeNum));
				}
				numerator += specRefE1.getSpecies();
				numeratorTeX += Species.toTeX(specRefE1.getSpecies());
				denominator = "1 + (" + specRefE1.getSpecies() + '/' + kIr1
						+ ") + ((" + kMr1 + " * " + specRefE2.getSpecies()
						+ ")/(" + kIr1 + " * " + kMr2 + ")) + (";
				denominatorTeX = "1+\\frac{"
						+ Species.toTeX(specRefE1.getSpecies()) + "}{"
						+ kIr1TeX + "}+\\frac{" + kMr1TeX
						+ Species.toTeX(specRefE2.getSpecies()) + "}{"
						+ kIr1TeX + kMr2TeX + "}+\\frac{"
						+ Species.toTeX(specRefE1.getSpecies());
				if (specRefE2.equals(specRefE1)) {
					numerator += "^2";
					numeratorTeX += "^2";
					denominator += specRefE1.getSpecies() + "^2";
					denominatorTeX += "^2";
				} else {
					numerator += " * " + specRefE2.getSpecies();
					numeratorTeX += Species.toTeX(specRefE2.getSpecies());
					denominator += '(' + specRefE1.getSpecies() + " * "
							+ specRefE2.getSpecies() + ')';
					denominatorTeX += Species.toTeX(specRefE2
							.getSpeciesInstance().getId());
				}
				numerator += ")/(" + kIr1 + " * " + kMr2 + "))" + " - ((" + kcatn
						+ " * ";
				numeratorTeX += "-\\frac{" + kcatnTeX + "}{" + kMp1TeX + '}';
				if (modE.size() > 0) {
					numerator += modE.get(enzymeNum) + " * ";
					numeratorTeX += Species.toTeX(modE.get(enzymeNum));
				}
				numerator += specRefP1.getSpecies() + ")/" + kMp1 + ')';
				numeratorTeX += Species.toTeX(specRefP1.getSpecies());

				denominator += "/(" + kIr1 + " * " + kMr2 + ")) + ((" + kMr1
						+ " * " + specRefE2.getSpecies() + " * "
						+ specRefP1.getSpecies() + ")/(" + kIr1 + " * " + kMr2
						+ " * " + kIp1 + ")) + (" + specRefP1.getSpecies() + '/'
						+ kMp1 + ')';
				denominatorTeX += "}{" + kIr1TeX + kMr2TeX + "}+\\frac{"
						+ kMr1TeX + Species.toTeX(specRefE2.getSpecies())
						+ Species.toTeX(specRefP1.getSpecies()) + "}{"
						+ kIr1TeX + kMr2TeX + kIp1TeX + "}+\\frac{"
						+ Species.toTeX(specRefP1.getSpecies()) + "}{"
						+ kMp1TeX + '}';
			}

			/*
			 * Construct formula
			 */
			formelTxt += "((" + numerator + ")/(" + denominator + "))";
			formelTeX += "\\frac{" + numeratorTeX + "}{" + denominatorTeX + '}';
			if (enzymeNum < modE.size() - 1) {
				formelTxt += " + ";
				formelTeX += "\\\\+";
			}
			enzymeNum++;
		} while (enzymeNum <= modE.size() - 1);

		/*
		 * Activation
		 */
		if (!modActi.isEmpty()) {
			for (int activatorNum = 0; activatorNum < modActi.size(); activatorNum++) {
				String kA = "kA_" + reactionNum, kATeX = "k^\\text{A}_{"
						+ reactionNum;
				kA += "_" + modActi.get(activatorNum);
				kATeX += ",{" + Species.idToTeX(modActi.get(activatorNum))
						+ "}}";

				if (!listOfLocalParameters.contains(kA))
					listOfLocalParameters.add(kA);
				acti += '(' + modActi.get(activatorNum) + "/(" + kA + " + "
						+ modActi.get(activatorNum) + ")) * ";
				actiTeX += "\\frac{" + Species.toTeX(modActi.get(activatorNum))
						+ "}{" + kATeX + " + "
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
				kITeX += ",{" + Species.idToTeX(modInhib.get(inhibitorNum))
						+ "}}";
				if (!listOfLocalParameters.contains(kI))
					listOfLocalParameters.add(kI);
				inhib += '(' + kI + "/(" + kI + " + "
						+ modInhib.get(inhibitorNum) + ")) * ";
				inhibTeX += "\\frac{" + kITeX + "}{" + kITeX + " + "
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
		// according to Cornish-Bowden: Fundamentals of Enzyme kinetics
		double stoichiometryRight = 0;
		for (int i = 0; i < getParentReaction().getNumProducts(); i++)
			stoichiometryRight += getParentReaction().getProduct(i)
					.getStoichiometry();
		String name = "compulsory-order ternary-complex mechanism";
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

	public static boolean isApplicable(PluginReaction reaction) {
		// TODO
		return true;
	}

	@Override
	public String getSBO() {
		return "none";
	}

}
