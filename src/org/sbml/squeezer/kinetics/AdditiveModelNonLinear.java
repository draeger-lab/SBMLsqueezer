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

import org.sbml.jsbml.ASTNode;
import org.sbml.jsbml.Reaction;
import org.sbml.squeezer.RateLawNotApplicableException;

/**
 * This class creates an equation based on a non-linear additive model.
 * 
 * @author <a href="mailto:snitschm@gmx.de">Sandra Nitschmann</a>
 * @author <a href="mailto:andreas.draeger@uni-tuebingen.de">Andreas
 *         Dr&auml;ger</a>
 * @since 1.3
 */
public class AdditiveModelNonLinear extends AdditiveModelLinear implements
		InterfaceGeneRegulatoryKinetics, InterfaceModulatedKinetics,
		InterfaceIrreversibleKinetics, InterfaceReversibleKinetics,
		InterfaceZeroReactants {

	/**
	 * @param parentReaction
	 * @param typeParameters
	 * @throws RateLawNotApplicableException
	 */
	public AdditiveModelNonLinear(Reaction parentReaction,
			Object... typeParameters) throws RateLawNotApplicableException {
		super(parentReaction, typeParameters);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.sbml.squeezer.kinetics.AdditiveModelLinear#activation(org.sbml.jsbml
	 * .ASTNode)
	 */
	@Override
	ASTNode activation(ASTNode g) {
		if (g == null) // unknown exponent
			return ASTNode.frac(1, ASTNode.sum(new ASTNode(1, this), ASTNode
					.exp(ASTNode.uMinus(new ASTNode(1, this)))));
		return ASTNode.frac(1, ASTNode.sum(new ASTNode(1, this), ASTNode
				.exp(ASTNode.uMinus(g))));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.squeezer.kinetics.AdditiveModelLinear#getSimpleName()
	 */
	@Override
	public String getSimpleName() {
		return "Non-linear additive model, general form";
	}

}
