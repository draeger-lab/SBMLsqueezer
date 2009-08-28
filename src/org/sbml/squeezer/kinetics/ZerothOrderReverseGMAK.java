/*
 * Feb 8, 2008
 *
 * Copyright (c) ZBiT, University of T&uuml;bingen, Germany
 * Compiler: JDK 1.6.0
 */
package org.sbml.squeezer.kinetics;

import java.io.IOException;
import java.util.List;

import org.sbml.ASTNode;
import org.sbml.Parameter;
import org.sbml.Reaction;
import org.sbml.squeezer.io.StringTools;

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
	public ZerothOrderReverseGMAK(Reaction parentReaction)
			throws RateLawNotApplicableException, IOException,
			IllegalFormatException {
		super(parentReaction);
		reactantOrder = Double.NaN;
		productOrder = 0;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.sbml.squeezer.kinetics.GeneralizedMassAction#dissociation(java.util
	 * .List, int)
	 */
	// @Override
	ASTNode dissociation(List<String> catalysts, int c) {
		StringBuffer kdiss = StringTools.concat("kdiss_", getParentSBMLObject()
				.getId());
		if (catalysts.size() > 0)
			kdiss = StringTools.concat(kdiss, StringTools.underscore, catalysts
					.get(c));
		addLocalParameter(new Parameter(kdiss.toString()));
		return new ASTNode(kdiss, this);
	}
}
