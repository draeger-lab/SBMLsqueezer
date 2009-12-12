package org.sbmlsqueezer.kinetics;

import java.util.List;

import org.sbml.libsbml.ASTNode;
import org.sbmlsqueezer.io.LaTeXExport;
import org.sbmlsqueezer.io.TextExport;

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
public class MichaelisMenten extends BasicKineticLaw {

	private int numOfInhibitors;

	private int numOfActivators;

	private int numOfEnzymes;

	/**
	 * @param parentReaction
	 * @param model
	 * @param listOfPossibleEnzymes
	 * @throws RateLawNotApplicableException
	 */
	public MichaelisMenten(PluginReaction parentReaction, PluginModel model,
			List<String> listOfPossibleEnzymes)
			throws RateLawNotApplicableException {
		super(parentReaction, model, listOfPossibleEnzymes);
	}

	/**
	 * @param parentReaction
	 * @param model
	 * @throws RateLawNotApplicableException
	 */
	public MichaelisMenten(PluginReaction parentReaction, PluginModel model)
			throws RateLawNotApplicableException {
		super(parentReaction, model);
	}

	@Override
	protected String createKineticEquation(PluginModel model, int reactionNum,
			List<String> modE, List<String> modActi, List<String> modTActi,
			List<String> modInhib, List<String> modTInhib, List<String> modCat)
			throws RateLawNotApplicableException {
		String numerator = "", numeratorTeX = "", denominator = "", denominatorTeX = "";
		String formelTxt = formelTeX = "";
		numOfActivators = modActi.size();
		numOfEnzymes = modE.size();
		numOfInhibitors = modInhib.size();
		reactionNum++;

		PluginReaction reaction = getParentReaction();
		if ((reaction.getNumReactants() > 1)
				|| (reaction.getReactant(0).getStoichiometry() != 1.0))
			throw new RateLawNotApplicableException(
					"This rate law can only be applied to reactions with exactly one reactant.");
		if (((reaction.getNumProducts() > 1) || (reaction.getProduct(0)
				.getStoichiometry() != 1.0))
				&& reaction.getReversible())
			throw new RateLawNotApplicableException(
					"This rate law can only be applied to reactions with exactly one product.");

		PluginSpeciesReference specRefR = reaction.getReactant(0);
		PluginSpeciesReference specRefP = reaction.getProduct(0);
		ASTNode ast = null;
		int enzymeNum = 0;
		do {
			String kcatp, kcatn, kcatpTeX, kcatnTeX;
			String kMe = "kM_" + reactionNum, kMp = kMe;
			String kMeTeX = "k^\\text{M}_{" + reactionNum, kMpTeX = kMeTeX;

			if (modE.size() == 0) {
				kcatp = "Vp_" + reactionNum;
				kcatn = "Vn_" + reactionNum;
				kcatpTeX = "V^\\text{m}_{+" + reactionNum + "}";
				kcatnTeX = "V^\\text{m}_{-" + reactionNum + "}";
			} else {
				kcatp = "kcatp_" + reactionNum;
				kcatn = "kcatn_" + reactionNum;
				kcatpTeX = "k^\\text{cat}_{+" + reactionNum;
				kcatnTeX = "k^\\text{cat}_{-" + reactionNum;
				if (modE.size() > 1) {
					kcatp += "_" + modE.get(enzymeNum);
					kcatn += "_" + modE.get(enzymeNum);
					kcatpTeX += ",{" + Species.idToTeX(modE.get(enzymeNum))
							+ "}";
					kcatnTeX += ",{" + Species.idToTeX(modE.get(enzymeNum))
							+ "}";
					kMe += "_" + modE.get(enzymeNum);
					kMp += "_" + modE.get(enzymeNum);
					kMeTeX += ",{" + Species.idToTeX(modE.get(enzymeNum)) + "}";
					kMpTeX += ",{" + Species.idToTeX(modE.get(enzymeNum)) + "}";
				}
				kcatpTeX += "}";
				kcatnTeX += "}";
			}
			kMe += "_" + specRefR.getSpecies();
			kMeTeX += ",{" + Species.idToTeX(specRefR.getSpecies()) + "}}";

			if (!paraList.contains(kcatp))
				paraList.add(new String(kcatp));
			if (!paraList.contains(kMe))
				paraList.add(new String(kMe));
			ASTNode numerator_n;
			ASTNode denominator_n;
			ASTNode temp;
			ASTNode temp2;
			ASTNode currEnzyme = new ASTNode(AST_TIMES);
			ASTNode kMeN = new ASTNode(AST_NAME);
			kMeN.setName(kMe);
			/*
			 * Irreversible Reaction
			 */
			if (!reaction.getReversible()) {
				numerator = kcatp + " * " + specRefR.getSpecies();
				numerator_n = new ASTNode(AST_TIMES);
				temp = new ASTNode(AST_NAME);
				temp.setName(kcatp);
				numerator_n.addChild(temp);
				temp = new ASTNode(AST_NAME);
				temp.setName(specRefR.getSpecies());
				numerator_n.addChild(temp);
				numeratorTeX = kcatpTeX + Species.toTeX(specRefR.getSpecies());
				denominator = specRefR.getSpecies();
				denominator_n = new ASTNode(AST_NAME);
				denominator_n.setName(specRefR.getSpecies());
				denominatorTeX = Species.toTeX(specRefR.getSpecies());

				/*
				 * Reversible Reaction
				 */
			} else {
				temp2 = new ASTNode(AST_DIVIDE);
				numerator_n = new ASTNode(AST_TIMES);
				temp = new ASTNode(AST_NAME);
				temp.setName(kcatp);
				temp2.addChild(temp);
				temp = new ASTNode(AST_NAME);
				temp.setName(kMe);
				temp2.addChild(temp);
				numerator_n.addChild(temp2);
				temp = new ASTNode(AST_NAME);
				temp.setName(specRefR.getSpecies());
				numerator_n.addChild(temp);

				numerator = kcatp + "/" + kMe + " * " + specRefR.getSpecies();
				numeratorTeX = "\\frac{" + kcatpTeX + "}{" + kMeTeX + "}"
						+ Species.toTeX(specRefR.getSpecies());
				denominator_n = new ASTNode(AST_DIVIDE);
				temp = new ASTNode(AST_NAME);
				temp.setName(specRefR.getSpecies());
				denominator_n.addChild(temp);
				temp = new ASTNode(AST_NAME);
				temp.setName(kMe);
				denominator_n.addChild(temp);
				denominator = specRefR.getSpecies() + "/" + kMe;
				denominatorTeX = "\\frac{"
						+ Species.toTeX(specRefR.getSpecies()) + "}{" + kMeTeX
						+ "}";

				kMp += "_" + specRefP.getSpecies();
				kMpTeX += ",{" + Species.idToTeX(specRefP.getSpecies()) + "}}";

				if (!paraList.contains(kcatn))
					paraList.add(new String(kcatn));
				if (!paraList.contains(kMp))
					paraList.add(new String(kMp));

				temp2 = numerator_n;
				numerator_n = new ASTNode(AST_MINUS);
				numerator_n.addChild(temp2);
				temp = new ASTNode(AST_NAME);
				temp.setName(kcatn);
				temp2 = new ASTNode(AST_DIVIDE);
				temp2.addChild(temp);
				temp = new ASTNode(AST_NAME);
				temp.setName(kMp);
				temp2.addChild(temp);
				temp = new ASTNode(AST_TIMES);
				temp.addChild(temp2);
				temp2 = new ASTNode(AST_NAME);
				temp2.setName(specRefP.getSpecies());
				temp.addChild(temp2);
				numerator_n.addChild(temp);

				numerator += " - " + kcatn + "/" + kMp + " * "
						+ specRefP.getSpecies();
				numeratorTeX += "-\\frac{" + kcatnTeX + "}{" + kMpTeX + "}"
						+ Species.toTeX(specRefP.getSpecies());

				temp = denominator_n;
				denominator_n = new ASTNode(AST_PLUS);
				denominator_n.addChild(temp);
				temp2 = new ASTNode(AST_DIVIDE);
				temp = new ASTNode(AST_NAME);
				temp.setName(specRefP.getSpecies());
				temp2.addChild(temp);
				temp = new ASTNode(AST_NAME);
				temp.setName(kMp);
				temp2.addChild(temp);
				denominator_n.addChild(temp2);

				denominator += " + " + specRefP.getSpecies() + "/" + kMp;
				denominatorTeX += " + \\frac{"
						+ Species.toTeX(specRefP.getSpecies()) + "}{" + kMpTeX
						+ "}";
			}

			/*
			 * Inhibition
			 */
			if (modInhib.size() == 1) {
				String kIa, kIb, kIaTeX, kIbTeX;

				kIa = "KIa_" + reactionNum;
				kIb = "KIb_" + reactionNum;
				kIbTeX = "\\left(1+\\frac{" + Species.toTeX(modInhib.get(0))
						+ "}{K^\\text{Ib}_{" + reactionNum;
				if (reaction.getReversible())
					kIaTeX = "\\frac{" + Species.toTeX(modInhib.get(0))
							+ "}{K^\\text{Ia}_{" + reactionNum;
				else
					kIaTeX = "\\frac{" + kMeTeX + "}{K^\\text{Ia}_{"
							+ reactionNum;

				if (modE.size() > 1) {
					kIa += "_" + modE.get(enzymeNum);
					kIb += "_" + modE.get(enzymeNum);
					kIaTeX += ",{" + Species.idToTeX(modE.get(enzymeNum)) + "}";
					kIbTeX += ",{" + Species.idToTeX(modE.get(enzymeNum)) + "}";
				}

				if (!paraList.contains(kIa))
					paraList.add(new String(kIa));
				if (!paraList.contains(kIb))
					paraList.add(new String(kIb));
				ASTNode inh = new ASTNode(AST_PLUS);
				temp = new ASTNode(AST_INTEGER);
				temp.setValue(1);
				inh.addChild(temp);
				temp2 = new ASTNode(AST_DIVIDE);
				temp = new ASTNode(AST_NAME);
				temp.setName(modInhib.get(0));
				temp2.addChild(temp);
				temp = new ASTNode(AST_NAME);
				temp.setName(kIb);
				temp2.addChild(temp);
				inh.addChild(temp2);
				kIb = "(1 + " + modInhib.get(0) + "/" + kIb + ")";
				if (reaction.getReversible()) {
					kIbTeX += "}}\\right)";
					kIaTeX += "}}";

					temp2 = denominator_n;
					denominator_n = new ASTNode(AST_PLUS);
					ASTNode faktor = new ASTNode(AST_DIVIDE);
					temp = new ASTNode(AST_NAME);
					temp.setName(modInhib.get(0));
					faktor.addChild(temp);
					temp = new ASTNode(AST_NAME);
					temp.setName(kIa);
					faktor.addChild(temp);
					denominator_n.addChild(faktor);
					temp = new ASTNode(AST_TIMES);
					temp.addChild(temp2);
					temp.addChild(inh);
					denominator_n.addChild(temp);

					denominator = modInhib.get(0) + "/" + kIa + " + ("
							+ denominator + ") * " + kIb;
					denominatorTeX = kIaTeX + " + \\left(" + denominatorTeX
							+ "\\right)" + kIbTeX;
				} else {
					kIbTeX += "}}\\right)";
					kIaTeX += "}}" + Species.toTeX(modInhib.get(0));

					temp2 = new ASTNode(AST_TIMES);
					temp = new ASTNode(AST_NAME);
					temp.setName(kMe);
					temp2.addChild(temp);
					temp = new ASTNode(AST_NAME);
					temp.setName(modInhib.get(0));
					temp2.addChild(temp);
					temp = new ASTNode(AST_DIVIDE);
					temp.addChild(temp2);
					temp2 = new ASTNode(AST_NAME);
					temp2.setName(kIa);
					temp.addChild(temp2);

					temp2 = denominator_n;
					denominator_n = new ASTNode(AST_PLUS);
					denominator_n.addChild(temp);
					temp = new ASTNode(AST_TIMES);
					temp.addChild(temp2);
					temp.addChild(inh);
					denominator_n.addChild(temp);

					denominator = "(" + kMe + " * " + modInhib.get(0) + ")/"
							+ kIa + " + " + denominator + " * " + kIb;
					denominatorTeX = kIaTeX + " + " + denominatorTeX + kIbTeX;
				}

			} else if ((modInhib.size() > 1)
					&& !getParentReaction().getReversible()) {
				// mixed-type inihibition of irreversible enzymes by mutually
				// exclusive
				// inhibitors.

				temp2 = denominator_n;
				System.out.println("MEHRERE INIBITOREN");
				denominator_n = new ASTNode(AST_TIMES);
				denominator_n.addChild(temp2);
				temp = new ASTNode(AST_INTEGER);
				temp.setValue(1);
				temp2 = new ASTNode(AST_PLUS);
				temp2.addChild(temp);

				denominator += " * (1 + "; // substrate
				denominatorTeX += "\\cdot\\left(1+";

				kMeN = new ASTNode(AST_PLUS);
				temp = new ASTNode(AST_INTEGER);
				temp.setValue(1);
				kMeN.addChild(temp);
				ASTNode inh;
				// kMe += " * (1 + "; // Km
				kMeTeX += "\\cdot\\left(1+";
				for (int i = 0; i < modInhib.size(); i++) {
					String kIai = (i + 1) + "_" + reactionNum;
					String kIaiTeX = (i + 1) + "}_{" + reactionNum;
					String inhib = modInhib.get(i);
					if (modE.size() > 1) {
						kIai += "_" + modE.get(enzymeNum);
						kIaiTeX += ",{" + Species.idToTeX(modE.get(enzymeNum))
								+ "}";
					}
					kIaiTeX += "}";
					String kIbi = "kIb" + kIai;
					kIai = "kIa" + kIai;
					String kIbiTeX = "K^\\text{Ib" + kIaiTeX;
					kIaiTeX = "K^\\text{Ia" + kIaiTeX;
					if (!paraList.contains(kIai))
						paraList.add(new String(kIai));
					if (!paraList.contains(kIbi))
						paraList.add(new String(kIbi));

					inh = new ASTNode(AST_DIVIDE);
					temp = new ASTNode(AST_NAME);
					temp.setName(inhib);
					inh.addChild(temp);
					temp = new ASTNode(AST_NAME);
					temp.setName(kIai);
					inh.addChild(temp);
					temp2.addChild(inh);
					denominator += inhib + "/" + kIai;
					denominatorTeX += "\\frac{" + Species.idToTeX(inhib) + "}{"
							+ kIaiTeX + "}";

					inh = new ASTNode(AST_DIVIDE);
					temp = new ASTNode(AST_NAME);
					temp.setName(inhib);
					inh.addChild(temp);
					temp = new ASTNode(AST_NAME);
					temp.setName(kIbi);
					inh.addChild(temp);
					kMeN.addChild(inh);
					// kMe += inhib + "/" + kIbi; // Km
					kMeTeX += "\\frac{" + Species.idToTeX(inhib) + "}{"
							+ kIbiTeX + "}";
					if (i < modInhib.size() - 1) {
						denominator += " + ";
						denominatorTeX += "+";
						// kMe += " + "; // Km
						kMeTeX += "+";
					}
				}
				kMeTeX += "\\right)";
				// kMe += ")";
				temp = kMeN;
				kMeN = new ASTNode(AST_TIMES);
				inh = new ASTNode(AST_NAME);
				inh.setName(kMe);
				kMeN.addChild(inh);
				kMeN.addChild(temp);
				denominator_n.addChild(temp2);

				denominatorTeX += "\\right)";
				denominator += ")";

			} else if (modInhib.size() > 1) {
				// the formalism from the convenience kinetics as a default.
				String inhib = "", inhibTeX = "";
				ASTNode inh = new ASTNode(AST_TIMES);
				ASTNode faktor;
				for (int inhibitorNum = 0; inhibitorNum < modInhib.size(); inhibitorNum++) {
					String kI = "kI_" + reactionNum, kITeX = "k^\\text{I}_{"
							+ reactionNum;
					kI += "_" + modInhib.get(inhibitorNum);
					kITeX += ",\\text{" + modInhib.get(inhibitorNum) + "}}";
					if (!paraList.contains(kI))
						paraList.add(new String(kI));

					temp = new ASTNode(AST_PLUS);
					temp2 = new ASTNode(AST_NAME);
					temp2.setName(kI);
					temp.addChild(temp2);
					temp2 = new ASTNode(AST_NAME);
					temp2.setName(modInhib.get(inhibitorNum));
					temp.addChild(temp2);
					faktor = new ASTNode(AST_DIVIDE);
					temp2 = new ASTNode(AST_NAME);
					temp2.setName(kI);
					faktor.addChild(temp2);
					faktor.addChild(temp);
					inh.addChild(faktor);

					inhib += kI + "/(" + kI + " + "
							+ modInhib.get(inhibitorNum) + ") * ";
					inhibTeX += "\\frac{" + kITeX + "}{" + kITeX + "+"
							+ Species.toTeX(modInhib.get(inhibitorNum))
							+ "}\\cdot ";
				}
				currEnzyme.addChild(inh);
				formelTxt += inhib;
				formelTeX += inhibTeX;
			}

			if (reaction.getReversible()) {
				temp2 = denominator_n;
				denominator_n = new ASTNode(AST_PLUS);
				temp = new ASTNode(AST_INTEGER);
				temp.setValue(1);
				denominator_n.addChild(temp);
				denominator_n.addChild(temp2);

				denominator = "1 + " + denominator;
				denominatorTeX = "1 + " + denominatorTeX;
			} else {
				temp2 = denominator_n;
				denominator_n = new ASTNode(AST_PLUS);
				denominator_n.addChild(kMeN);
				denominator_n.addChild(temp2);

				denominator = kMe + " + " + denominator;
				denominatorTeX = kMeTeX + " + " + denominatorTeX;
			}

			// construct formula
			temp = new ASTNode(AST_DIVIDE);
			temp.addChild(numerator_n);
			temp.addChild(denominator_n);
			if (currEnzyme.getNumChildren() <= 1)
				currEnzyme = temp;

			formelTxt += "(" + numerator + ")/(" + denominator + ")";
			formelTeX += "\\frac{" + numeratorTeX + "}{" + denominatorTeX + "}";
			if (modE.size() > 0) {
				// TODO - ERROR
				temp = currEnzyme;
				currEnzyme = new ASTNode(AST_TIMES);
				temp2 = new ASTNode(AST_NAME);
				temp2.setName(modE.get(enzymeNum));
				currEnzyme.addChild(temp2);
				currEnzyme.addChild(temp);

				formelTxt = modE.get(enzymeNum) + " * " + formelTxt;
				formelTeX = Species.toTeX(modE.get(enzymeNum)) + "\\cdot"
						+ formelTeX;
				if (enzymeNum < (modE.size() - 1)) {
					formelTxt += " + ";
					formelTeX += "\\\\+";
				}
			}
			if (currEnzyme.getNumChildren() == 1) {
				currEnzyme = currEnzyme.getLeftChild();
			}
			if (numOfEnzymes <= 1)
				ast = currEnzyme;
			else {
				if (ast == null)
					ast = new ASTNode(AST_PLUS);
				ast.addChild(currEnzyme);
			}
			enzymeNum++;
		} while (enzymeNum <= modE.size() - 1);
		/*
		 * Activation
		 */
		ASTNode temp, temp2;
		ASTNode act = new ASTNode(AST_TIMES);
		for (int i = 0; i < modActi.size(); i++) {

			String kAa, kAaTeX, kAbTeX;

			kAa = "kA_" + reactionNum;
			// ????
			/* "\\cdot\\left(1+\\frac{" */
			kAaTeX = "k^\\text{A}_{" + reactionNum;
			kAbTeX = "\\cdot\\left(1+\\frac{k^\\text{Ab}_{" + reactionNum;

			if (!paraList.contains(kAa))
				paraList.add(new String(kAa));

			kAaTeX += "}";
			temp = new ASTNode(AST_NAME);
			temp.setName(kAa);
			temp2 = new ASTNode(AST_PLUS);
			temp2.addChild(temp);
			temp = new ASTNode(AST_NAME);
			temp.setName(modActi.get(i));
			temp2.addChild(temp);
			temp = new ASTNode(AST_DIVIDE);
			temp.addChild(new ASTNode(AST_NAME));
			temp.getLeftChild().setName(modActi.get(i));
			temp.addChild(temp2);
			act.addChild(temp);

			formelTxt += modActi.get(i) + "/(" + kAa + " + " + modActi.get(i)
					+ ") * ";
			formelTeX += "\\frac{" + Species.toTeX(modActi.get(i)) + "}{"
					+ kAaTeX + "+" + Species.toTeX(modActi.get(i)) + "}\\cdot ";

			// ????
			/*
			 * kAa = " * (1 + " + kAa + "/" + modActi.get(0) + ")"; kAb = " * (1 + " +
			 * kAb + "/" + modActi.get(0) + ")"; kAaTeX += "}}{" +
			 * Species.toTeX(modActi.get(0)) + "}\\right)"; kAbTeX += "}}{" +
			 * Species.toTeX(modActi.get(0)) + "}\\right)";
			 */
		}
		if (act.getNumChildren() > 0) {
			temp = ast;
			ast = new ASTNode(AST_TIMES);
			if (act.getNumChildren() == 1) {
				act = act.getLeftChild();
			}
			ast.addChild(act);
			ast.addChild(temp);
		}

		if (enzymeNum > 1)
			formelTeX += "\\end{multline}";

		// setMath(ast);
		// formelTxt = getFormula();
		formelTxt = TextExport.toText(model, ast);
		formelTeX = LaTeXExport.toLaTeX(model, ast);
		
		return formelTxt;
	}

	@Override
	public String getName() {
		switch (numOfEnzymes) {
		case 0: // no enzyme, irreversible
			if (!getParentReaction().getReversible() && (numOfActivators == 0)
					&& (numOfInhibitors == 0))
				return "normalised kinetics of unireactant enzymes"; // 0000199
			else if ((numOfActivators == 0) && (numOfInhibitors == 0))
				return "kinetics of non-modulated unireactant enzymes"; // 0000326
			break;
		case 1: // one enzmye
			if (getParentReaction().getReversible()) {
				if ((numOfActivators == 0) && (numOfInhibitors == 0))
					return "kinetics of non-modulated unireactant enzymes";
			} else if ((numOfActivators == 0) && (numOfInhibitors == 0)) // irreversible
				// equivalents: Briggs-Haldane equation or Van
				// Slyke-Cullen
				// equation
				return "Henri-Michaelis Menten equation"; // 0000029
			break;
		}
		if (!getParentReaction().getReversible())
			switch (numOfInhibitors) {
			case 1:
				return "simple mixed-type inhibition of irreversible unireactant enzymes"; // 0000265
			case 2:
				return "mixed-type inhibition of irreversible unireactant enzymes by two inhibitors"; // 0000276
			default:
				return "mixed-type inhibition of irreversible enzymes by mutually exclusive inhibitors"; // 0000275
			}
		return "kinetics of unireactant enzymes"; // 0000269
	}

	public static boolean isApplicable(PluginReaction reaction) {
		// TODO
		return true;
	}

	@Override
	public String getSBO() {
		String name = getName().toLowerCase(), sbo = "none";
		if (name.equals("normalised kinetics of unireactant enzymes"))
			sbo = "0000199";
		else if (name.equals("kinetics of non-modulated unireactant enzymes"))
			sbo = "0000326";
		else if (name.equals("Briggs-Haldane equation"))
			sbo = "0000031";
		else if (name
				.equals("kinetics of irreversible non-modulated unireactant enzymes"))
			sbo = "0000028";
		else if (name
				.equals("simple mixed-type inhibition of irreversible unireactant enzymes"))
			sbo = "0000265";
		else if (name.equals("kinetics of unireactant enzymes"))
			sbo = "0000269";
		else if (name.equals("Henri-Michaelis Menten equation"))
			sbo = "0000029";
		else if (name.equals("Van Slyke-Cullen equation"))
			sbo = "0000030";
		else if (name
				.equals("simple uncompetitive inhibition of irreversible unireactant enzymes"))
			sbo = "0000262";
		else if (name
				.equals("mixed-type inhibition of irreversible enzymes by mutually exclusive inhibitors"))
			sbo = "0000275";
		else if (name
				.equals("simple non-competitive inhibition of unireactant enzymes"))
			sbo = "0000266";
		else if (name
				.equals("mixed-type inhibition of irreversible unireactant enzymes by two inhibitors"))
			sbo = "0000276";
		else if (name
				.equals("mixed-type inhibition of unireactactant enzymes by two inhibitors"))
			sbo = "0000277";
		else if (name
				.equals("simple competitive inhibition of irreversible unireactant enzymes by two non-exclusive inhibitors"))
			sbo = "0000274";
		else if (name
				.equals("competitive inhibition of irreversible unireactant enzymes by two exclusive inhibitors"))
			sbo = "0000271";
		else if (name
				.equals("competitive inhibition of irreversible unireactant enzymes by exclusive inhibitors"))
			sbo = "0000270";
		else if (name
				.equals("simple competitive inhibition of irreversible unireactant enzymes by one inhibitor"))
			sbo = "0000260";

		return sbo;
	}
}
