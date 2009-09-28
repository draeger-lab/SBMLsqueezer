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
import org.sbml.jsbml.ModifierSpeciesReference;
import org.sbml.jsbml.Parameter;
import org.sbml.jsbml.Reaction;
import org.sbml.jsbml.SBO;
import org.sbml.jsbml.Species;
import org.sbml.squeezer.RateLawNotApplicableException;

/**
 * This class creates an equation based on an non-linear additive model as defined in the paper
 * "The NetGenerator Algorithm: Reconstruction of Gene Regulatory Networks"
 * of Töpfer, S.; Guthke, R.; Driesch, D.; Wötzel, D. & Pfaff 2007
 * 
 * @author <a href="mailto:snitschm@gmx.de">Sandra Nitschmann</a>
 *
 */
public class GRNAdditiveModel_NGnonlinear extends GRNAdditiveModel implements InterfaceGeneRegulatoryKinetics {
		
	/**
	 * @param parentReaction
	 * @param typeParameters
	 * @throws RateLawNotApplicableException
	 * @throws IllegalFormatException
	 */
	public GRNAdditiveModel_NGnonlinear(Reaction parentReaction,
			Object... typeParameters) throws RateLawNotApplicableException,
			IllegalFormatException {
		super(parentReaction, typeParameters);
	}
	
	ASTNode actifunction(ASTNode g){
		return ASTNode.frac(1, ASTNode.sum(new ASTNode(1,this),ASTNode.exp(ASTNode.times(new ASTNode(-1,this),g))));
	}
	
	ASTNode function_w() {
		Reaction r = getParentSBMLObject();
		Parameter p = null;
		String rId = getParentSBMLObject().getId();
		ASTNode modnode = null;
		ASTNode pnode = null;
		ASTNode node = new ASTNode(this);
		Species product = r.getProduct(0).getSpeciesInstance();
		for (int modifierNum = 0; modifierNum < r.getNumModifiers(); modifierNum++) {
			Species modifierspec = r.getModifier(modifierNum)
					.getSpeciesInstance();
			ModifierSpeciesReference modifier = r.getModifier(modifierNum);
			if ((SBO.isProtein(modifierspec.getSBOTerm())
					|| SBO.isRNAOrMessengerRNA(modifierspec.getSBOTerm()))&&!product.equals(modifierspec)) {
				if (!modifier.isSetSBOTerm()) modifier.setSBOTerm(19);
				if (SBO.isModifier(modifier.getSBOTerm())) {
					modnode = new ASTNode(modifier.getSpeciesInstance(), this);
					p = createOrGetParameter("w_", modifierNum, underscore, rId);
					pnode = new ASTNode(p, this);
					if (node.isUnknown())
						node = ASTNode.times(pnode, modnode);
					else
						node = ASTNode.sum(node, ASTNode.times(pnode, modnode));
				}
			}
		}
		if (node.isUnknown()) return null;
		else return node;
	}
	
	ASTNode function_l(){
		Reaction r = getParentSBMLObject();
		String rId = getParentSBMLObject().getId();
		ASTNode node = new ASTNode(this);
		
		Species product = r.getProduct(0).getSpeciesInstance();
		ASTNode productnode = new ASTNode(product, this);	

		Parameter p = createOrGetParameter("w_", rId);
		ASTNode pnode = new ASTNode(p, this);
		//TODO: -(-1) zu +
		node = ASTNode.times(new ASTNode(-1,this),pnode, productnode);

		return node;
	}

	/*
	 * (non-Javadoc)
	 * @see org.sbml.squeezer.kinetics.GRNAdditiveModel#getSimpleName()
	 */
	public String getSimpleName() {
		return "A special additive model equation (NetGenerator non-linear model)";
	}
}
