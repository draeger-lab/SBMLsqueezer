package org.sbmlsqueezer.kinetics;

import java.util.List;

import org.sbml.libsbml.ASTNode;
import org.sbmlsqueezer.io.LaTeXExport;

import jp.sbi.celldesigner.plugin.PluginModel;
import jp.sbi.celldesigner.plugin.PluginReaction;
import jp.sbi.celldesigner.plugin.PluginSpeciesReference;

/**
 * This is the standard convenience kinetics which is only appropriated for
 * systems whose stochiometric matrix has full column rank. Otherwise the more
 * complicated thermodynamically independend form {@see ConvenienceIndependent}
 * needs to be invoked.
 *
 * @since 2.0
 * @version
 * @author Nadine Hassis <Nadine.hassis@gmail.com>
 * @author Andreas Dr&auml;ger <andreas.draeger@uni-tuebingen.de>
 * @author Michael Ziller <michael@diegrauezelle.de> Copyright (c) ZBiT,
 *         University of T&uuml;bingen, Germany
 * @date Aug 1, 2007
 */
public class Convenience extends BasicKineticLaw {

	/**
	 * @param parentReaction
	 * @param model
	 * @param reversibility
	 * @throws RateLawNotApplicableException
	 */
	public Convenience(PluginReaction parentReaction, PluginModel model)
	    throws RateLawNotApplicableException {
		super(parentReaction, model);
	}

	/**
	 * @param parentReaction
	 * @param model
	 * @param listOfPossibleEnzymes
	 * @param reversibility
	 * @throws RateLawNotApplicableException
	 */
	public Convenience(PluginReaction parentReaction, PluginModel model,
	    List<String> listOfPossibleEnzymes) throws RateLawNotApplicableException {
		super(parentReaction, model, listOfPossibleEnzymes);
	}

	@Override
	protected String createKineticEquation(PluginModel model, int reactionNum,
	    List<String> modE, List<String> modActi, List<String> modTActi,
	    List<String> modInhib, List<String> modTInhib, List<String> modCat)
	    throws RateLawNotApplicableException {
		String numerator = "";
		String numeratorTeX = "";
		String inhib = "";
		String inhibTeX = "";
		String acti = "";
		String actiTeX = "";
		String formelTxt = "";
		formelTeX = "";
		reactionNum++;
		int numOfEnzymes = modE.size();
		PluginReaction parentReaction = getParentReaction();
		ASTNode ast = null;
		ASTNode temp;
		ASTNode temp2;
		int enzymeNum = 0;
		do {

			String denominator = "", denominatorTeX = "", kM, kMTeX;

			numerator = "kcatp_" + reactionNum;
			numeratorTeX = "k^\\text{cat}_{+" + reactionNum;
			if (modE.size() > 1) {
				numerator += "_" + modE.get(enzymeNum);
				numeratorTeX += "," + Species.idToTeX(modE.get(enzymeNum)) + "}";
			}
			numeratorTeX += "}";

			if (!paraList.contains(numerator)) paraList.add(new String(numerator));
			/*
			 * ASTNode numerator_n = new ASTNode(AST_NAME);
			 * numerator_n.setName(numerator); ASTNode denominator_n = null; ASTNode
			 * currEnzyme = new ASTNode(AST_DIVIDE);
			 */
			// sums for each educt
			for (int eductNum = 0; eductNum < parentReaction.getNumReactants(); eductNum++) {
				String exp = "";
				PluginSpeciesReference specref = (PluginSpeciesReference) parentReaction
				    .getListOfReactants().get(eductNum);

				// build denominator
				kM = "kM_" + reactionNum;
				kMTeX = "k^\\text{M}_{" + reactionNum;

				if (modE.size() > 1) {
					kM += "_" + modE.get(enzymeNum);
					kMTeX += ",\\text{" + Species.idToTeX(modE.get(enzymeNum)) + "}";
				}
				kM += "_" + specref.getSpecies();
				kMTeX += ",{" + Species.idToTeX(specref.getSpecies()) + "}}";

				if (!paraList.contains(kM)) paraList.add(kM);

				// we can save the brakets if there is just one educt.
				if (parentReaction.getNumReactants() > 1) {
					denominator += "(";
					denominatorTeX += "\\left(";
				}
				if (!parentReaction.getReversible()
				    || ((parentReaction.getNumReactants() != 1) || (parentReaction
				        .getNumProducts() == 1))) {
					/*
					 * denominator_n = new ASTNode(AST_PLUS); temp = new
					 * ASTNode(AST_INTEGER); temp.setValue(1);
					 * denominator_n.addChild(temp);
					 */
					denominator += " 1 + ";
					denominatorTeX += "1 + ";
				}
				/*
				 * temp = new ASTNode(AST_DIVIDE); temp2 = new ASTNode(AST_NAME);
				 * temp2.setName(specref.getSpecies()); temp.addChild(temp2); temp2 =
				 * new ASTNode(AST_NAME); temp2.setName(kM); temp.addChild(temp2); if
				 * (denominator_n == null) { denominator_n = temp; } else {
				 * denominator_n.addChild(temp); }
				 */
				denominator += specref.getSpecies() + "/" + kM;
				denominatorTeX += "\\frac{" + Species.toTeX(specref.getSpecies())
				    + "}{" + kMTeX + "}";
				// ASTNode pow = new ASTNode(AST_POWER);
				// ASTNode basis = new ASTNode(AST_PLUS);
				// int exp_n = 1;
				for (int m = 1; m < (int) specref.getStoichiometry(); m++) {
					exp = "^" + (m + 1);
					/*
					 * exp_n = m+1; temp = new ASTNode(AST_NAME);
					 * temp.setName(specref.getSpecies()); basis.addChild(temp); temp =
					 * new ASTNode(AST_NAME); temp.setName(kM); basis.addChild(temp);
					 * pow.addChild(basis); temp = new ASTNode(AST_INTEGER);
					 * temp.setValue(m + 1); pow.addChild(temp); temp = denominator_n;
					 * denominator_n = new ASTNode(AST_PLUS);
					 * denominator_n.addChild(temp); denominator_n.addChild(pow);
					 */
					denominator += " + (" + specref.getSpecies() + "/" + kM + ")" + exp;
					denominatorTeX += " + \\left(\\frac{"
					    + Species.toTeX(specref.getSpecies()) + "}{" + kMTeX
					    + "}\\right)" + exp.replace("(", "{").replace(")", "}");
				}

				// we can save the brakets if there is just one educt
				if (parentReaction.getNumReactants() > 1) {
					denominatorTeX += "\\right)";
					denominator += ")";
				}
				if ((eductNum + 1) < parentReaction.getNumReactants())
				  denominator += " * ";

				// build numerator
				if (specref.getStoichiometry() == 1.0) {
					/*
					 * temp = new ASTNode(AST_DIVIDE); temp2 = new ASTNode(AST_NAME);
					 * temp2.setName(specref.getSpecies()); temp.addChild(temp2); temp2 =
					 * new ASTNode(AST_NAME); temp2.setName(kM); temp.addChild(temp2);
					 * temp2 = numerator_n; numerator_n = new ASTNode(AST_TIMES);
					 * numerator_n.addChild(temp2); numerator_n.addChild(temp);
					 */

					numerator += " * " + specref.getSpecies() + "/" + kM;
					numeratorTeX += "\\cdot \\frac{"
					    + Species.toTeX(specref.getSpecies()) + "}{" + kMTeX + "}";
				} else {
					/*
					 * temp = new ASTNode(AST_DIVIDE); temp2 = new ASTNode(AST_NAME);
					 * temp2.setName(specref.getSpecies()); temp.addChild(temp2); temp2 =
					 * new ASTNode(AST_NAME); temp2.setName(kM); temp.addChild(temp2); if
					 * (exp_n > 1){ temp2 = new ASTNode(AST_POWER); temp2.addChild(temp);
					 * temp = new ASTNode(AST_INTEGER); temp.setValue(exp_n);
					 * temp2.addChild(temp); } temp = numerator_n; numerator_n = new
					 * ASTNode(AST_TIMES); numerator_n.addChild(temp);
					 * numerator_n.addChild(temp2);
					 */
					numerator += " * (" + specref.getSpecies() + "/" + kM + ")" + exp;
					numeratorTeX += "\\cdot \\left(\\frac{"
					    + Species.toTeX(specref.getSpecies()) + "}{" + kMTeX
					    + "}\\right)" + exp.replace("(", "{").replace(")", "}");
				}
			}

			/*
			 * only if reaction is reversible or we want it to be.
			 */
			if (parentReaction.getReversible()) {
				denominator += " + ";
				denominatorTeX += " + ";
				numerator += " - ";
				numeratorTeX += " - ";

				String kcat = "kcatn_" + reactionNum, kcatTeX = "k^\\text{cat}_{-"
				    + reactionNum;

				if (modE.size() > 1) {
					kcat += "_" + modE.get(enzymeNum);
					kcatTeX += ",{" + Species.idToTeX(modE.get(enzymeNum)) + "}";
				}
				kcatTeX += "}";
				/*
				 * temp = numerator_n; numerator_n = new ASTNode(AST_MINUS);
				 * numerator_n.addChild(temp); temp = new ASTNode(AST_NAME);
				 * temp.setName(kcat); numerator_n.addChild(temp);
				 */
				numerator += kcat;
				numeratorTeX += kcatTeX;

				if (!paraList.contains(kcat)) paraList.add(kcat);

				// Sums for each product
				for (int productNum = 0; productNum < parentReaction.getNumProducts(); productNum++) {
					kM = "kM_" + reactionNum;
					kMTeX = "k^\\text{M}_{" + reactionNum;

					if (modE.size() > 1) {
						kM += "_" + modE.get(enzymeNum);
						kMTeX += ",{" + Species.idToTeX(modE.get(enzymeNum)) + "}";
					}
					kM += "_"
					    + parentReaction.getProduct(productNum).getSpeciesInstance()
					        .getId();
					kMTeX += ",{"
					    + Species.idToTeX(parentReaction.getProduct(productNum)
					        .getSpecies()) + "}}";

					if (!paraList.contains(kM)) paraList.add(kM);

					String exp = "";
					PluginSpeciesReference specRefP = parentReaction
					    .getProduct(productNum);
					/*
					 * temp = denominator_n; denominator_n = new ASTNode(AST_PLUS);
					 * denominator_n.addChild(temp); ASTNode part = null;
					 */
					if (parentReaction.getNumProducts() > 1) {
						/*
						 * part = new ASTNode(AST_PLUS); temp = new ASTNode(AST_INTEGER);
						 * temp.setValue(1); part.addChild(temp);
						 */
						denominator += "(1 + ";
						denominatorTeX += "\\left(1 + ";
					}
					/*
					 * if (part == null) { part = new ASTNode(AST_DIVIDE); temp = new
					 * ASTNode(AST_NAME); temp.setName(specRefP.getSpecies());
					 * part.addChild(temp); temp = new ASTNode(AST_NAME);
					 * temp.setName(kM); part.addChild(temp); } else { temp2 = new
					 * ASTNode(AST_DIVIDE); temp = new ASTNode(AST_NAME);
					 * temp.setName(specRefP.getSpecies()); temp2.addChild(temp); temp =
					 * new ASTNode(AST_NAME); temp.setName(kM); temp2.addChild(temp);
					 * part.addChild(temp2); }
					 */
					denominator += specRefP.getSpecies() + "/" + kM;
					denominatorTeX += "\\frac{" + Species.toTeX(specRefP.getSpecies())
					    + "}{" + kMTeX + "}";

					// for each stoichiometry (see Liebermeister et al.)
					// ASTNode basis;
					// int exp_n=1;
					for (int m = 1; m < (int) specRefP.getStoichiometry(); m++) {
						exp = "^" + (m + 1);
						/*
						 * exp_n = m+1; basis = new ASTNode(AST_POWER); temp = new
						 * ASTNode(AST_NAME); temp.setName(specRefP.getSpecies()); temp2 =
						 * new ASTNode(AST_DIVIDE); temp2.addChild(temp); temp = new
						 * ASTNode(AST_NAME); temp.setName(kM); temp2.addChild(temp);
						 * basis.addChild(temp2); temp = new ASTNode(AST_INTEGER);
						 * temp.setValue(m + 1); basis.addChild(temp); temp = part; part =
						 * new ASTNode(AST_PLUS); part.addChild(temp); part.addChild(basis);
						 */
						denominator += " + (" + specRefP.getSpecies() + "/" + kM + ")"
						    + exp;
						denominatorTeX += " + \\left(\\frac{"
						    + Species.toTeX(specRefP.getSpecies()) + "}{" + kMTeX
						    + "}\\right)" + exp;
					}
					if (parentReaction.getNumProducts() > 1) {
						// denominator_n.addChild(part);
						denominatorTeX += "\\right)";
						denominator += ")";
					}
					if ((productNum + 1) < parentReaction.getNumProducts())
					  denominator += " * ";

					// build numerator
					/*
					 * temp = numerator_n; numerator_n = new ASTNode(AST_TIMES);
					 * numerator_n.addChild(temp);
					 */
					if (specRefP.getStoichiometry() != 1.0) {
						/*
						 * temp2 = new ASTNode(AST_DIVIDE); temp = new ASTNode(AST_NAME);
						 * temp.setName(specRefP.getSpecies()); temp2.addChild(temp); temp =
						 * new ASTNode(AST_NAME); temp.setName(kM); temp2.addChild(temp); if
						 * (exp_n > 1){ temp = new ASTNode(AST_POWER); temp.addChild(temp2);
						 * temp2 = new ASTNode(AST_INTEGER); temp2.setValue(exp_n);
						 * temp.addChild(temp2); temp2 = temp; }
						 * numerator_n.addChild(temp2);
						 */

						numerator += " * (" + specRefP.getSpecies() + "/" + kM + ")" + exp;
						numeratorTeX += "\\cdot \\left(\\frac{"
						    + Species.toTeX(specRefP.getSpecies()) + "}{" + kMTeX
						    + "}\\right)" + exp;
					} else {
						/*
						 * temp2 = new ASTNode(AST_DIVIDE); temp = new ASTNode(AST_NAME);
						 * temp.setName(specRefP.getSpecies()); temp2.addChild(temp); temp =
						 * new ASTNode(AST_NAME); temp.setName(kM); temp2.addChild(temp);
						 * numerator_n.addChild(temp);
						 */

						numerator += " * " + specRefP.getSpecies() + "/" + kM;
						numeratorTeX += "\\cdot \\frac{"
						    + Species.toTeX(specRefP.getSpecies()) + "}{" + kMTeX + "}";
					}
				}
				if ((parentReaction.getNumProducts() > 1)
				    && (parentReaction.getNumReactants() > 1)) {
					/*
					 * temp = denominator_n; denominator_n = new ASTNode(AST_MINUS);
					 * denominator_n.addChild(temp); temp = new ASTNode(AST_INTEGER);
					 * temp.setValue(1); denominator_n.addChild(temp);
					 */

					denominator += " -1";
					denominatorTeX += " -1";
				}
			}
			if (modE.size() > 0) {
				formelTxt += modE.get(enzymeNum) + " * ";
				formelTeX += Species.toTeX(modE.get(enzymeNum)) + "\\cdot ";
			}
			/*
			 * if(denominator_n.getNumChildren() == 1) denominator_n =
			 * denominator_n.getLeftChild(); currEnzyme.addChild(numerator_n);
			 * currEnzyme.addChild(denominator_n); if (modE.size() > 0) { temp =
			 * currEnzyme; currEnzyme = new ASTNode(AST_TIMES); temp2 = new
			 * ASTNode(AST_NAME); temp2.setName(modE.get(enzymeNum));
			 * currEnzyme.addChild(temp2); currEnzyme.addChild(temp); }
			 */
			formelTxt += "(" + numerator + ")" + "/(" + denominator + ")";
			formelTeX += "\\frac{" + numeratorTeX + "}{" + denominatorTeX + "}";
			/*
			 * if (numOfEnzymes <= 1) ast = currEnzyme; else { if (ast == null) ast =
			 * new ASTNode(AST_PLUS); ast.addChild(currEnzyme); }
			 */
			if (enzymeNum < (modE.size()) - 1) {
				formelTxt += " + ";
				formelTeX += "\\\\+";
			}
			enzymeNum++;
		} while (enzymeNum <= modE.size() - 1);

		/*
		 * Activation
		 */
		// ASTNode act = null;
		if (!modActi.isEmpty()) {
			for (int activatorNum = 0; activatorNum < modActi.size(); activatorNum++) {
				String kA = "kA_" + reactionNum;
				String kATeX = "k^\\text{A}_{" + reactionNum;
				kA += "_" + modActi.get(activatorNum);
				kATeX += ",\\text{" + modActi.get(activatorNum) + "}}";
				if (!paraList.contains(kA)) paraList.add(kA);
				/*
				 * temp2 = new ASTNode(AST_DIVIDE); temp = new ASTNode(AST_NAME);
				 * temp.setName(modActi.get(activatorNum)); temp2.addChild(temp); temp =
				 * new ASTNode(AST_PLUS); temp.addChild(new ASTNode(AST_NAME));
				 * temp.getLeftChild().setName(kA); temp.addChild(new
				 * ASTNode(AST_NAME));
				 * temp.getRightChild().setName(modActi.get(activatorNum));
				 * temp2.addChild(temp); if (act == null) { act = temp2; } else { temp =
				 * act; act = new ASTNode(AST_TIMES); act.addChild(temp);
				 * act.addChild(temp2); }
				 */

				acti += modActi.get(activatorNum) + "/(" + kA + " + "
				    + modActi.get(activatorNum) + ") * ";
				actiTeX += "\\frac{" + Species.toTeX(modActi.get(activatorNum)) + "}{"
				    + kATeX + "+" + Species.toTeX(modActi.get(activatorNum))
				    + "}\\cdot ";
			}
		}
		/*
		 * Inhibition
		 */
		// temp2 = null;
		// ASTNode inh = null;
		if (!modInhib.isEmpty()) {
			// inh = new ASTNode(AST_TIMES);
			for (int inhibitorNum = 0; inhibitorNum < modInhib.size(); inhibitorNum++) {
				String kI = "kI_" + reactionNum, kITeX = "k^\\text{I}_{" + reactionNum;
				kI += "_" + modInhib.get(inhibitorNum);
				kITeX += ",\\text{" + modInhib.get(inhibitorNum) + "}}";
				if (!paraList.contains(kI)) paraList.add(kI);
				/*
				 * temp = new ASTNode(AST_PLUS); temp2 = new ASTNode(AST_NAME);
				 * temp2.setName(kI); temp.addChild(temp2); temp.addChild(new
				 * ASTNode(AST_NAME));
				 * temp.getRightChild().setName(modInhib.get(inhibitorNum)); temp2 = new
				 * ASTNode(AST_DIVIDE); temp2.addChild(new ASTNode(AST_NAME));
				 * temp2.getLeftChild().setName(kI); temp2.addChild(temp);
				 * inh.addChild(temp2);
				 */

				inhib += kI + "/(" + kI + " + " + modInhib.get(inhibitorNum) + ") * ";
				inhibTeX += "\\frac{" + kITeX + "}{" + kITeX + "+"
				    + Species.toTeX(modInhib.get(inhibitorNum)) + "}\\cdot ";
			}
			// if (inh.getNumChildren() == 1 ) inh = inh.getLeftChild();

		}
		// TODO here incomplete:
		if ((acti.length() + inhib.length() > 0) && (modE.size() > 1)) {
			inhib += "(";
			formelTxt += ")";
			inhibTeX += inhibTeX.substring(0, inhibTeX.length() - 6)
			    + "\\\\\\cdot\\left(";
			formelTeX = formelTeX.replaceAll("\\\\\\+", "\\right.\\\\\\\\+\\\\left.")
			    + "\\right)";
		}
		/*
		 * temp = ast; ast = null; if (act != null) { ast = new ASTNode(AST_TIMES);
		 * ast.addChild(act); } if (inh != null) { if (ast == null) ast = new
		 * ASTNode(AST_TIMES); ast.addChild(inh); } if (ast == null) ast = temp;
		 * else ast.addChild(temp); setMath(ast); formelTxt = getFormula();
		 * System.err.println(formelTxt); formelTeX =
		 * LaTeXExport.toLaTeX(model,ast);
		 */
		formelTxt = acti + inhib + formelTxt;
		formelTeX = actiTeX + inhibTeX + formelTeX;

		if (enzymeNum > 1) formelTeX += "\\end{multline}";
		System.err.println("Reversible 2");
		return formelTxt;
	}

	@Override
	public String getName() {
		if (getParentReaction().getReversible())
		  return "reversible simple convenience kinetics";
		return "irreversible simple convenience kinetics";
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
