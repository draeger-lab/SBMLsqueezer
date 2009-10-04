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
 * TODO: comment missing
 * 
 * @since 2.0
 * @version
 * @author Andreas Dr&auml;ger (draeger) <andreas.draeger@uni-tuebingen.de>
 * @date Feb 8, 2008
 **/
public class ZerothOrderReverseGMAK extends GeneralizedMassAction implements
		InterfaceNonEnzymeKinetics, InterfaceReversibleKinetics,
		InterfaceIrreversibleKinetics, InterfaceZeroReactants,
		InterfaceZeroProducts, InterfaceModulatedKinetics {
		
	/**
	 * @param parentReaction
	 * @param model
	 * @throws RateLawNotApplicableException
	 * @throws IllegalFormatException
	 */
	public ZerothOrderReverseGMAK(Reaction parentReaction, Object... typeParameters)
			throws RateLawNotApplicableException, IllegalFormatException {
		super(parentReaction, typeParameters);
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
		reactantOrder = Double.NaN;
		productOrder = 0;
		StringBuffer kdiss = concat("kdiss_", getParentSBMLObject().getId());
		if (catalysts.size() > 0)
			append(kdiss, underscore, catalysts.get(c));
		Parameter p_kdiss = createOrGetParameter(kdiss.toString());
		p_kdiss.setSBOTerm(352);
		return new ASTNode(p_kdiss, this);
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.sbml.squeezer.kinetics.GeneralizedMassAction#getSimpleName()
	 */
	public String getSimpleName() {
		return "Zeroth order reverse mass action kinetics";
	}
}
