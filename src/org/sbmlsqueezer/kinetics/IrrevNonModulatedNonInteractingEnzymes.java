/*
 * Feb 6, 2008 Copyright (c) ZBiT, University of T&uuml;bingen, Germany
 * Compiler: JDK 1.6.0
 */
package org.sbmlsqueezer.kinetics;

import java.util.List;

import org.sbml.libsbml.ASTNode;
import org.sbmlsqueezer.io.LaTeXExport;
import org.sbmlsqueezer.io.TextExport;

import jp.sbi.celldesigner.plugin.PluginModel;
import jp.sbi.celldesigner.plugin.PluginReaction;
import jp.sbi.celldesigner.plugin.PluginSpeciesReference;

/**
 * This class implements SBO:0000150 and all of its special cases. It is an
 * kinetic law of an irreversible enzyme reaction, which is not modulated, and
 * in which all reacting species do not interact: Kinetics of enzymes that react
 * with one or several substances, their substrates, that bind independently.
 * The enzymes do not catalyse the reactions in both directions.
 * 
 * @since 2.0
 * @version
 * @author Andreas Dr&auml;ger (draeger) <andreas.draeger@uni-tuebingen.de>
 * @date Feb 6, 2008
 */
public class IrrevNonModulatedNonInteractingEnzymes extends BasicKineticLaw {

	private int numOfEnzymes;

	/**
	 * @param parentReaction
	 * @param model
	 * @param listOfPossibleEnzymes
	 * @throws RateLawNotApplicableException
	 */
	public IrrevNonModulatedNonInteractingEnzymes(
			PluginReaction parentReaction, PluginModel model,
			List<String> listOfPossibleEnzymes)
			throws RateLawNotApplicableException {
		super(parentReaction, model, listOfPossibleEnzymes);
	}

	/**
	 * @param parentReaction
	 * @param model
	 * @throws RateLawNotApplicableException
	 */
	public IrrevNonModulatedNonInteractingEnzymes(
			PluginReaction parentReaction, PluginModel model)
			throws RateLawNotApplicableException {
		super(parentReaction, model);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbmlsqueezer.kinetics.BasicKineticLaw#createKineticEquation(jp.sbi.celldesigner.plugin.PluginModel,
	 *      int, java.util.List, java.util.List, java.util.List, java.util.List,
	 *      java.util.List, java.util.List)
	 */
	@Override
	protected String createKineticEquation(PluginModel model, int reactionNum,
			List<String> modE, List<String> modActi, List<String> modTActi,
			List<String> modInhib, List<String> modTInhib, List<String> modCat)
			throws RateLawNotApplicableException {
		if ((modActi.size() > 0) || (modInhib.size() > 0)
				|| (modTActi.size() > 0) || (modTInhib.size() > 0))
			throw new RateLawNotApplicableException(
					"This rate law can only be applied to non-modulated reactions.");
		if ((modCat.size() > 0))
			throw new RateLawNotApplicableException(
					"This rate law can only be applied to enzyme-catalyzed reactions.");
		if (getParentReaction().getReversible())
			getParentReaction().setReversible(false);

		numOfEnzymes = modE.size();
		reactionNum++;
		// String numerator = "", numeratorTeX = "", denominator = "",
		// denominatorTeX = "";
		String formelTxt = formelTeX = "";
		PluginReaction reaction = getParentReaction();
		ASTNode ast = null;

		int enzymeNum = 0;
		do {
			String kcat, kcatTeX;
			if (modE.size() == 0) {
				kcat = "V_" + reactionNum;
				kcatTeX = "V^\\text{m}_{+" + reactionNum + "}";
			} else {
				kcat = "kcat_" + reactionNum;
				kcatTeX = "k^\\text{cat}_{" + reactionNum;
				if (modE.size() > 1) {
					kcat += "_" + modE.get(enzymeNum);
					kcatTeX += ",{" + Species.idToTeX(modE.get(enzymeNum))
							+ "}";
				}
				kcatTeX += "}";
			}
			if (!paraList.contains(kcat))
				paraList.add(new String(kcat));

			ASTNode currEnzyme = new ASTNode(AST_DIVIDE);
			ASTNode numerator = new ASTNode(AST_TIMES);
			currEnzyme.addChild(numerator);

			ASTNode tmp;
			if (modE.size() <= 1) {
				ast = currEnzyme;
			} else {
				if (ast == null)
					ast = new ASTNode(AST_PLUS);
				ast.addChild(currEnzyme);
			}
			if (modE.size() >= 1) {
				tmp = new ASTNode(AST_NAME);
				tmp.setName(modE.get(enzymeNum));
				numerator.addChild(tmp);
			}
			tmp = new ASTNode(AST_NAME);
			tmp.setName(kcat);
			numerator.addChild(tmp);

			ASTNode denominator = null;
			for (int i = 0; i < reaction.getNumReactants(); i++) {
				PluginSpeciesReference si = reaction.getReactant(i);
				if (((int) si.getStoichiometry()) - si.getStoichiometry() != 0)
					throw new RateLawNotApplicableException(
							"This rate law can only be applied if all reactants have integer stoichiometries.");
				String kM = "kM_" + reactionNum;
				String kMeTeX = "k^\\text{M}_{" + reactionNum;
				if (modE.size() > 1) {
					kM += "_" + modE.get(enzymeNum);
					kMeTeX += ",{" + Species.idToTeX(modE.get(enzymeNum)) + "}";
				}
				kM += "_" + si.getSpecies();
				kMeTeX += ",{" + Species.idToTeX(si.getSpecies()) + "}}";
				if (!paraList.contains(kM))
					paraList.add(new String(kM));

				ASTNode frac = new ASTNode(AST_DIVIDE);
				tmp = new ASTNode(AST_NAME);
				tmp.setName(si.getSpecies());
				frac.addChild(tmp);
				tmp = new ASTNode(AST_NAME);
				tmp.setName(kM);
				frac.addChild(tmp);
				if (si.getStoichiometry() != 1) {
					tmp = new ASTNode(AST_POWER);
					tmp.addChild(frac);
					tmp.addChild(new ASTNode(AST_INTEGER));
					tmp.getChild(1).setValue(si.getStoichiometry());
					numerator.addChild(tmp);
				} else 
					numerator.addChild(frac);
				if (reaction.getNumReactants() > 1) {
					if (denominator == null)
						denominator = new ASTNode(AST_TIMES);
					tmp = new ASTNode(AST_PLUS);
					tmp.addChild(new ASTNode(AST_INTEGER));
					tmp.getChild(0).setValue(1);
					tmp.addChild(clone(frac));
					if (si.getStoichiometry() != 1) {
						ASTNode power = new ASTNode(AST_POWER);
						power.addChild(tmp);
						power.addChild(new ASTNode(AST_INTEGER));
						power.getChild(1).setValue(si.getStoichiometry());
						denominator.addChild(power);
					} else
						denominator.addChild(tmp);
				} else {
					if (si.getStoichiometry() != 1) {
						denominator = new ASTNode(AST_POWER);
						denominator.addChild(tmp);
						denominator.addChild(new ASTNode(AST_INTEGER));
						denominator.getChild(1).setValue(si.getStoichiometry());
					} else {
						denominator = new ASTNode(AST_PLUS);
						tmp = new ASTNode(AST_INTEGER);
						tmp.setValue(1);
						denominator.addChild(tmp);
						denominator.addChild(clone(frac));
					}
				}
			}
			currEnzyme.addChild(denominator);
			enzymeNum++;
		} while (enzymeNum <= modE.size() - 1);

		if (enzymeNum > 1)
			formelTeX += "\\end{multline}";		
		//setMath(ast);
		//formelTxt = getFormula();
		formelTeX = LaTeXExport.toLaTeX(model, ast);		
		formelTxt = TextExport.toText(model, ast);
		
		return formelTxt;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbmlsqueezer.kinetics.BasicKineticLaw#getName()
	 */
	@Override
	public String getName() {
		double stoichiometry = 0;
		for (int i = 0; i < getParentReaction().getNumReactants(); i++)
			stoichiometry += getParentReaction().getReactant(i)
					.getStoichiometry();
		switch ((int) Math.round(stoichiometry)) {
		case 1:
			if (numOfEnzymes == 0)
				return "normalised kinetics of unireactant enzymes";
			return "Henri-Michaelis Menten equation";
		case 2:
			return "kinetics of irreversible non-modulated non-interacting bireactant enzymes";
		case 3:
			return "kinetics of irreversible non-modulated non-interacting trireactant enzymes";
		default:
			return "kinetics of irreversible non-modulated non-interacting reactant enzymes";
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbmlsqueezer.kinetics.BasicKineticLaw#getSBO()
	 */
	@Override
	public String getSBO() {
		String name = getName().toLowerCase(), sbo = "none";
		if (name
				.equals("kinetics of irreversible non-modulated non-interacting reactant enzymes"))
			sbo = "0000150";
		else if (name
				.equals("kinetics of irreversible non-modulated non-interacting bireactant enzymes"))
			sbo = "0000151";
		else if (name
				.equals("kinetics of irreversible non-modulated non-interacting trireactant enzymes"))
			sbo = "0000152";
		else if (name.equals("normalised kinetics of unireactant enzymes"))
			sbo = "0000199";
		else if (name.equals("Henri-Michaelis Menten equation"))
			sbo = "0000029";
		return sbo;
	}

}
