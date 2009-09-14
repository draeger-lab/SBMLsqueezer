/*
 *  SBMLsqueezer creates rate equations for reactions in SBML files
 *  (http://sbml.org).
 *  Copyright (C) 2009 ZBIT, University of Tübingen, Andreas Dräger
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.sbml.squeezer.kinetics;

import java.util.IllegalFormatException;
import java.util.List;

import org.sbml.jsbml.ASTNode;
import org.sbml.jsbml.Parameter;
import org.sbml.jsbml.Reaction;
import org.sbml.squeezer.RateLawNotApplicableException;

/**
 * This class creates generalized mass action rate equations with zeroth order
 * in all reactants.
 * 
 * @since 1.0
 * @version
 * @author <a href="andreas.draeger@uni-tuebingen.de">Andreas Dr&auml;ger</a>
 * @date Feb 8, 2008
 **/
public class ZerothOrderForwardGMAK extends GeneralizedMassAction {

	/**
	 * @param parentReaction
	 * @param model
	 * @throws RateLawNotApplicableException
	 * @throws IllegalFormatException
	 */
	public ZerothOrderForwardGMAK(Reaction parentReaction)
			throws RateLawNotApplicableException, IllegalFormatException {
		super(parentReaction);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.sbml.squeezer.kinetics.GeneralizedMassAction#association(java.util
	 * .List, int)
	 */
	// @Override
	final ASTNode association(List<String> catalysts, int catNum) {
		reactantOrder = 0;
		productOrder = Double.NaN;
		StringBuffer kass = concat("kass_", getParentSBMLObject().getId());
		if (catalysts.size() > 0)
			kass = concat(kass, underscore, catalysts.get(catNum));
		Parameter p_kass = createOrGetParameter(kass.toString());
		p_kass.setSBOTerm(48);
		return new ASTNode(p_kass, this);
	}
}
