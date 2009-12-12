/**
 * 
 */
package org.sbmlsqueezer.kinetics;

import java.util.List;

import jp.sbi.celldesigner.plugin.PluginModel;
import jp.sbi.celldesigner.plugin.PluginReaction;

import org.sbml.libsbml.ASTNode;
import org.sbmlsqueezer.io.LaTeXExport;
import org.sbmlsqueezer.io.TextExport;

/**
 * @author andreas
 * 
 */
public class IrrevCompetNonCooperativeEnzymes extends BasicKineticLaw {

	private int numInhib;
	private int numOfEnzymes;

	/**
	 * @param parentReaction
	 * @param model
	 * @param listOfPossibleEnzymes
	 * @throws RateLawNotApplicableException
	 */
	public IrrevCompetNonCooperativeEnzymes(PluginReaction parentReaction,
			PluginModel model, List<String> listOfPossibleEnzymes)
			throws RateLawNotApplicableException {
		super(parentReaction, model, listOfPossibleEnzymes);
	}

	/**
	 * @param parentReaction
	 * @param model
	 * @throws RateLawNotApplicableException
	 */
	public IrrevCompetNonCooperativeEnzymes(PluginReaction parentReaction,
			PluginModel model) throws RateLawNotApplicableException {
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
		reactionNum++;
		String formelTxt = formelTeX = "";
		ASTNode ast = null;

		int enzymeNum = 0;
		do {
			String kcat;
			if (numOfEnzymes == 0)
				kcat = "V_" + reactionNum;
			else {
				kcat = "kcat_" + reactionNum;
				if (modE.size() > 1)
					kcat += "_" + modE.get(enzymeNum);
			}
			if (!paraList.contains(kcat))
				paraList.add(new String(kcat));

			ASTNode currEnzyme = new ASTNode(AST_DIVIDE);
			ASTNode numerator = new ASTNode(AST_TIMES);
			currEnzyme.addChild(numerator);

			ASTNode tmp;
			if (numOfEnzymes <= 1)
				ast = currEnzyme;
			else {
				if (ast == null)
					ast = new ASTNode(AST_PLUS);
				ast.addChild(currEnzyme);
				tmp = new ASTNode(AST_NAME);
				tmp.setName(modE.get(enzymeNum));
				numerator.addChild(tmp);
			}
			tmp = new ASTNode(AST_NAME);
			tmp.setName(kcat);
			numerator.addChild(tmp);
			tmp = new ASTNode(AST_NAME);
			tmp.setName(reaction.getReactant(0).getSpecies());
			numerator.addChild(tmp);

			ASTNode denominator = new ASTNode(AST_PLUS);
			String kM = "kM_" + reactionNum;
			if (numOfEnzymes > 1)
				kM += "_" + modE.get(enzymeNum);
			kM += "_" + reaction.getReactant(0).getSpecies();
			if (!paraList.contains(kM))
				paraList.add(new String(kM));
			tmp = new ASTNode(AST_NAME);
			tmp.setName(kM);
			if (modInhib.size() == 0)
				denominator.addChild(tmp);
			else {
				ASTNode factor = new ASTNode(AST_TIMES);
				factor.addChild(tmp);
				for (int i = 0; i < modInhib.size(); i++) {
					ASTNode frac = new ASTNode(AST_DIVIDE);
					tmp = new ASTNode(AST_NAME);
					tmp.setName(modInhib.get(i));
					frac.addChild(tmp);
					tmp = new ASTNode(AST_NAME);
					String kIi = "Ki_" + reactionNum, exponent = "m_"
							+ reactionNum;
					if (numOfEnzymes > 1) {
						kIi += "_" + modE.get(enzymeNum);
						exponent += "_" + modE.get(enzymeNum);
					}
					kIi += "_" + modInhib.get(i);
					exponent += "_" + modInhib.get(i);
					if (!paraList.contains(kIi))
						paraList.add(new String(kIi));
					if (!paraList.contains(exponent))
						paraList.add(new String(exponent));
					tmp.setName(kIi);
					frac.addChild(tmp);

					ASTNode power = new ASTNode(AST_POWER);
					tmp = new ASTNode(AST_PLUS);
					tmp.addChild(new ASTNode(AST_INTEGER));
					tmp.getLeftChild().setValue(1);
					tmp.addChild(frac);
					power.addChild(tmp);
					power.addChild(new ASTNode(AST_NAME));
					power.getRightChild().setName(exponent);
					factor.addChild(power);
				}
				denominator.addChild(factor);
			}
			tmp = new ASTNode(AST_NAME);
			tmp.setName(reaction.getReactant(0).getSpecies());
			denominator.addChild(tmp);
			currEnzyme.addChild(denominator);
			enzymeNum++;
		} while (enzymeNum <= modE.size() - 1);

		/*
		 * Activation
		 */
		if (modActi.size() > 0) {
			int actiNum = 0;
			ASTNode activation = null;
			ASTNode tmp;
			do {
				tmp = new ASTNode(AST_DIVIDE);
				tmp.addChild(new ASTNode(AST_NAME));
				tmp.getLeftChild().setName(modActi.get(actiNum));
				tmp.addChild(new ASTNode(AST_PLUS));
				tmp.getRightChild().addChild(new ASTNode(AST_NAME));
				tmp.getRightChild().addChild(new ASTNode(AST_NAME));
				String kAi = "KA_" + reactionNum + "_" + modActi.get(actiNum);
				if (!paraList.contains(kAi))
					paraList.add(new String(kAi));
				tmp.getRightChild().getLeftChild().setName(kAi);
				tmp.getRightChild().getRightChild().setName(
						modActi.get(actiNum++));
				if (modActi.size() > 1) {
					if (activation == null)
						activation = new ASTNode(AST_TIMES);
					else
						activation.addChild(tmp);
				} else
					activation = tmp;
			} while (actiNum <= modActi.size() - 1);
			tmp = ast;
			ast = new ASTNode(AST_TIMES);
			ast.addChild(activation);
			ast.addChild(tmp);
		}

		// setMath(ast);
		// formelTxt = getFormula();
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
	@Override
	public String getSBO() {
		String name = getName().toLowerCase();
		if (name
				.equals("competitive inhibition of irreversible unireactant enzymes by non-exclusive non-cooperative inhibitors"))
			return "0000273";
		if (name
				.equals("competitive inhibition of irreversible unireactant enzymes by one inhibitor"))
			return "0000267";
		return "none";
	}

}
