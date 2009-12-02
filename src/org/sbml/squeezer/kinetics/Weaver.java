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
import org.sbml.jsbml.Parameter;
import org.sbml.jsbml.Reaction;
import org.sbml.squeezer.RateLawNotApplicableException;

/**
 * This class creates an equation based on an additive model as defined in the
 * paper "Modeling regulatory networks with weight matrices" of Weaver, D.;
 * Workman, C. & Stormo, G. 1999
 * 
 * @author <a href="mailto:snitschm@gmx.de">Sandra Nitschmann</a>
 * 
 */
public class Weaver extends AdditiveModelNonLinear implements
		InterfaceGeneRegulatoryKinetics {


	/**
	 * @param parentReaction
	 * @param typeParameters
	 * @throws RateLawNotApplicableException
	 */
	public Weaver(Reaction parentReaction, Object... typeParameters)
			throws RateLawNotApplicableException {
		super(parentReaction, typeParameters);
	}

	/* (Kein Javadoc)
	 * @see org.sbml.squeezer.kinetics.AdditiveModelLinear#actifunction(org.sbml.jsbml.ASTNode)
	 */
	ASTNode actifunction(ASTNode g) {
		String rId = getParentSBMLObject().getId();
		Parameter alpha = parameterAlpha(rId);
		Parameter beta = parameterBeta(rId);
		ASTNode betanode = new ASTNode(beta, this);

		if (!(g == null)) {
			return ASTNode.frac(1, ASTNode.sum(new ASTNode(1, this), ASTNode
					.exp(ASTNode.sum(ASTNode.times(new ASTNode(-1, this),
							new ASTNode(alpha, this), g), betanode))));
		} else {
			return ASTNode.frac(1, ASTNode.sum(new ASTNode(1, this), ASTNode
					.exp(ASTNode.sum(ASTNode.times(new ASTNode(-1, this),
							new ASTNode(alpha, this), new ASTNode(this)), betanode))));
		}
	}

	/* (Kein Javadoc)
	 * @see org.sbml.squeezer.kinetics.AdditiveModelLinear#b_i()
	 */
	ASTNode b_i() {
		return null;
	}

	/* (Kein Javadoc)
	 * @see org.sbml.squeezer.kinetics.AdditiveModelLinear#function_v()
	 */
	ASTNode function_v() {
		return null;
	}

	/* (Kein Javadoc)
	 * @see org.sbml.squeezer.kinetics.AdditiveModelLinear#getSimpleName()
	 */
	public String getSimpleName() {
		return "Additive model: Weaver, Workman & Stormo 1999";
	}
}
