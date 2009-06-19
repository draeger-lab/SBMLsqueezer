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
public class ZerothOrderReverseGMAK extends GeneralizedMassAction {

	/**
	 * @param parentReaction
	 * @param model
	 * @throws RateLawNotApplicableException
	 * @throws IOException
	 * @throws IllegalFormatException 
	 */
	public ZerothOrderReverseGMAK(PluginReaction parentReaction,
			PluginModel model) throws RateLawNotApplicableException,
			IOException, IllegalFormatException {
		super(parentReaction, model);
	}
	
	public ZerothOrderReverseGMAK(PluginReaction parentReaction,
			PluginModel model, List<String> listOfPossibleEnzymes)
			throws RateLawNotApplicableException, IOException, IllegalFormatException {
		super(parentReaction, model, listOfPossibleEnzymes);
	}

	@Override
	protected StringBuffer createKineticEquation(PluginModel model,
			 List<String> modE, List<String> modActi,
			List<String> modTActi, List<String> modInhib,
			List<String> modTInhib, List<String> modCat)
			throws RateLawNotApplicableException {
		boolean zeroReact = false;
		boolean zeroProd = true;
		reactantOrder = Double.NaN;
		productOrder = 0;
//		if (modCat.isEmpty())
//			return super.createKineticEquation(modE,-1, modCat,
//					modActi, modActi, zeroReact, zeroProd);
//		StringBuffer[] parts = new StringBuffer[modCat.size()];
//		for (int i = 0; i < parts.length; i++)
//			parts[i] = super.createKineticEquation(modE,i, modCat,
//					modActi, modActi, zeroReact, zeroProd);
//		return sum(parts);
		return new StringBuffer("1");
	}
}
