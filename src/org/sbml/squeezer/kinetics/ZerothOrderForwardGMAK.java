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

import java.util.List;

import org.sbml.jsbml.ASTNode;
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
 */
public class ZerothOrderForwardGMAK extends GeneralizedMassAction implements
		InterfaceNonEnzymeKinetics, InterfaceReversibleKinetics,
		InterfaceIrreversibleKinetics, InterfaceZeroReactants,
		InterfaceZeroProducts, InterfaceModulatedKinetics {

	/**
	 * 
	 * @param parentReaction
	 * @param typeParameters
	 * @throws RateLawNotApplicableException
	 */
	public ZerothOrderForwardGMAK(Reaction parentReaction,
			Object... typeParameters) throws RateLawNotApplicableException {
		super(parentReaction, typeParameters);
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
		orderReactants = 0;
		orderProducts = Double.NaN;
		return new ASTNode(
				parameterAssociationConst(catalysts.size() == 0 ? null
						: catalysts.get(catNum)), this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.squeezer.kinetics.GeneralizedMassAction#getSimpleName()
	 */
	public String getSimpleName() {
		return "Zeroth order forward mass action kinetics";
	}
}
