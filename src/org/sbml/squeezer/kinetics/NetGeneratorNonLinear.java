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
import org.sbml.jsbml.ModifierSpeciesReference;
import org.sbml.jsbml.Parameter;
import org.sbml.jsbml.Reaction;
import org.sbml.jsbml.SBO;
import org.sbml.jsbml.Species;
import org.sbml.squeezer.RateLawNotApplicableException;

/**
 * This class creates an equation based on an non-linear additive model as
 * defined in the paper &ldquo;The NetGenerator Algorithm: Reconstruction of
 * Gene Regulatory Networks&rdquo; of T&ouml;pfer, S.; Guthke, R.; Driesch, D.;
 * W&ouml;tzel, D., and Pfaff 2007
 * 
 * @author <a href="mailto:snitschm@gmx.de">Sandra Nitschmann</a>
 * @since 1.3
 */
public class NetGeneratorNonLinear extends AdditiveModelNonLinear implements
		InterfaceGeneRegulatoryKinetics {

	/**
	 * @param parentReaction
	 * @param typeParameters
	 * @throws RateLawNotApplicableException
	 */
	public NetGeneratorNonLinear(Reaction parentReaction,
			Object... typeParameters) throws RateLawNotApplicableException {
		super(parentReaction, typeParameters);
	}

	/**
	 * weighted sum over all interacting RNAs
	 * 
	 * @return ASTNode
	 */
	ASTNode function_w() {
		Reaction r = getParentSBMLObject();
		String rId = getParentSBMLObject().getId();
		ASTNode node = new ASTNode(this);

		for (int modifierNum = 0; modifierNum < r.getNumModifiers(); modifierNum++) {
			Species modifierspec = r.getModifier(modifierNum)
					.getSpeciesInstance();
			ModifierSpeciesReference modifier = r.getModifier(modifierNum);

			if (SBO.isProtein(modifierspec.getSBOTerm())
					|| SBO.isRNAOrMessengerRNA(modifierspec.getSBOTerm())) {
				if (!modifier.isSetSBOTerm())
					modifier.setSBOTerm(19);
				if (SBO.isModifier(modifier.getSBOTerm())) {
					ASTNode modnode = speciesTerm(modifier);
					Parameter p = parameterW(modifier.getSpecies(), rId);
					ASTNode pnode = new ASTNode(p, this);
					if (node.isUnknown())
						node = ASTNode.times(pnode, modnode);
					else
						node = ASTNode.sum(node, ASTNode.times(pnode, modnode));
				}
			}
		}
		if (node.isUnknown())
			return null;
		else
			return node;
	}

	/*
	 * (Kein Javadoc)
	 * 
	 * @see org.sbml.squeezer.kinetics.AdditiveModelNonLinear#getSimpleName()
	 */
	public String getSimpleName() {
		return "Additive model: NetGenerator non-linear model";
	}
}
