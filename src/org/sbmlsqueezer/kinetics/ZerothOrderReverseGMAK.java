/*
 * Feb 8, 2008
 *
 * Copyright (c) ZBiT, University of T&uuml;bingen, Germany
 * Compiler: JDK 1.6.0
 */
package org.sbmlsqueezer.kinetics;

import java.util.List;

import jp.sbi.celldesigner.plugin.PluginModel;
import jp.sbi.celldesigner.plugin.PluginReaction;

/**
 * TODO: comment missing
 *
 * @since 2.0
 * @version
 * @author Andreas Dr&auml;ger (draeger) <andreas.draeger@uni-tuebingen.de>
 * @date Feb 8, 2008
 **/
public class ZerothOrderReverseGMAK extends GeneralizedMassAction {

	/**
	 * @param parentReaction
	 * @param model
	 * @throws RateLawNotApplicableException
	 */
	public ZerothOrderReverseGMAK(PluginReaction parentReaction, PluginModel model)
	    throws RateLawNotApplicableException {
		super(parentReaction, model);
	}

	/**
	 * @param parentReaction
	 * @param model
	 * @param listOfPossibleEnzymes
	 * @throws RateLawNotApplicableException
	 */
	public ZerothOrderReverseGMAK(PluginReaction parentReaction,
	    PluginModel model, List<String> listOfPossibleEnzymes)
	    throws RateLawNotApplicableException {
		super(parentReaction, model, listOfPossibleEnzymes);
	}

	@Override
	protected String createKineticEquation(PluginModel model, int reactionNum,
	    List<String> modE, List<String> modActi, List<String> modTActi,
	    List<String> modInhib, List<String> modTInhib, List<String> modCat)
	    throws RateLawNotApplicableException {
		boolean zeroReact = false, zeroProd = true;
		reactantOrder	= Double.NaN;
		productOrder	= 0;
		if (modCat.isEmpty())
		  return super.createKineticEquation(-1, reactionNum, modCat, modActi, modActi,
		      zeroReact, zeroProd);
		String formelTxt = "";
		for (int catalyzerNum = 0; catalyzerNum < modCat.size(); catalyzerNum++) {
			formelTxt += super.createKineticEquation(catalyzerNum, reactionNum, modCat,
			    modActi, modActi, zeroReact, zeroProd);
			if (catalyzerNum + 1 < modCat.size()) {
				formelTxt += "+";
				formelTeX += "\\\\+";
			}
		}
		if (modCat.size() > 1) formelTeX += "\\end{multline}";

		return formelTxt;
	}

}
