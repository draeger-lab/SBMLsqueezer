/*
 * Feb 8, 2008
 *
 * Copyright (c) ZBiT, University of T&uuml;bingen, Germany
 * Compiler: JDK 1.6.0
 */
package org.sbmlsqueezer.kinetics;

import java.io.IOException;
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
public class ZerothOrderGMAK extends GeneralizedMassAction {

	/**
	 * @param parentReaction
	 * @param model
	 * @throws RateLawNotApplicableException
	 * @throws IOException
	 */
	public ZerothOrderGMAK(PluginReaction parentReaction, PluginModel model,
			int type) throws RateLawNotApplicableException, IOException {
		super(parentReaction, model, type);
	}

	/**
	 * @param parentReaction
	 * @param model
	 * @param listOfPossibleEnzymes
	 * @throws RateLawNotApplicableException
	 * @throws IOException
	 */
	public ZerothOrderGMAK(PluginReaction parentReaction, PluginModel model,
			List<String> listOfPossibleEnzymes, int type)
			throws RateLawNotApplicableException, IOException {
		super(parentReaction, model, listOfPossibleEnzymes, type);
	}

	@Override
	protected StringBuffer createKineticEquation(PluginModel model,
			int reactionNum, List<String> modE, List<String> modActi,
			List<String> modTActi, List<String> modInhib,
			List<String> modTInhib, List<String> modCat)
			throws RateLawNotApplicableException {
		boolean zeroReact = true;
		boolean zeroProd = false;
		try {
			if (this.type == FORWARD) {
				reactantOrder = 0;
				productOrder = Double.NaN;
			} else if (this.type == REVERSE) {
				zeroReact = false;
				zeroProd = true;
				reactantOrder = Double.NaN;
				productOrder = 0;
			} else {
				throw new IllegalFormatException(
						"Invalid type argument for Zeroth order GMAK");
			}
		} catch (IllegalFormatException exc) {
			exc.printStackTrace();
		}

		if (modCat.isEmpty())
			return super.createKineticEquation(-1, reactionNum, modCat,
					modActi, modActi, zeroReact, zeroProd);
		StringBuffer[] parts = new StringBuffer[modCat.size()];
		for (int i = 0; i < parts.length; i++)
			parts[i] = super.createKineticEquation(i, reactionNum, modCat,
					modActi, modActi, zeroReact, zeroProd);
		return sum(parts);
	}
}
