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

import org.sbml.jsbml.ASTNode;
import org.sbml.jsbml.Parameter;
import org.sbml.jsbml.Reaction;
import org.sbml.squeezer.RateLawNotApplicableException;

/**
 * This class creates an equation based on an additive model as defined in the paper
 * "Modeling regulatory networks with weight matrices" of Weaver, D.; Workman, C. & Stormo, G. 1999
 * 
 * @author <a href="mailto:snitschm@gmx.de">Sandra Nitschmann</a>
 *
 */
public class GRNAdditiveModel_2 extends GRNAdditiveModel implements GeneRegulatoryKinetics {

	/**
	 * @param parentReaction
	 * @param typeParameters
	 * @throws RateLawNotApplicableException
	 * @throws IllegalFormatException
	 */
	public GRNAdditiveModel_2(Reaction parentReaction, Object... typeParameters)
			throws RateLawNotApplicableException, IllegalFormatException {
		super(parentReaction, typeParameters);
	}
	
	ASTNode function_g(ASTNode w, ASTNode v, ASTNode b){
		String rId = getParentSBMLObject().getId();
		Parameter pa = createOrGetParameter("a_", rId);
		ASTNode pnode_a = new ASTNode(pa, this);
		Parameter pb = createOrGetParameter("b_", rId);
		ASTNode pnode_b = new ASTNode(pb, this);
		ASTNode node = ASTNode.frac(1, ASTNode.sum(new ASTNode(1,this),ASTNode.exp(
				ASTNode.sum(ASTNode.times(new ASTNode(-1,this),pnode_a,ASTNode.sum(w,v,b)),pnode_b))));
		return node;
	}
	
	ASTNode function_v(){
		return null;
	}
	
	ASTNode function_l(){
		return null;
	}
	
	ASTNode b_i(){
		return null;
	}

}
