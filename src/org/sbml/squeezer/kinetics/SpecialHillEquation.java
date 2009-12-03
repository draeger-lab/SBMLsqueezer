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
import org.sbml.jsbml.ModifierSpeciesReference;
import org.sbml.jsbml.Parameter;
import org.sbml.jsbml.Reaction;
import org.sbml.jsbml.SBO;
import org.sbml.jsbml.SpeciesReference;
import org.sbml.squeezer.RateLawNotApplicableException;

/**
 * This class creates Hill equation as defined in the PhD thesis &ldquo;Modeling
 * Non-Linear Dynamic Phenomena in Biochemical Networks&rdquo; of Nicole Radde
 * and the paper &ldquo;Modeling Non-Linear Dynamic Phenomena in Biochemical
 * Networks&ldquo; of Nicole Radde and Lars Kaderali.
 * 
 * @author <a href="mailto:snitschm@gmx.de">Sandra Nitschmann</a>
 * @since 1.3
 * 
 */
public class SpecialHillEquation extends BasicKineticLaw implements
		InterfaceGeneRegulatoryKinetics {

	/**
	 * @param parentReaction
	 * @param typeParameters
	 * @throws RateLawNotApplicableException
	 */
	public SpecialHillEquation(Reaction parentReaction,
			Object... typeParameters) throws RateLawNotApplicableException {
		super(parentReaction, typeParameters);
	}

	/*
	 * (Kein Javadoc)
	 * 
	 * @see
	 * org.sbml.squeezer.kinetics.BasicKineticLaw#createKineticEquation(java
	 * .util.List, java.util.List, java.util.List, java.util.List,
	 * java.util.List, java.util.List)
	 */
	ASTNode createKineticEquation(List<String> enzymes,
			List<String> activators, List<String> transActivators,
			List<String> inhibitors, List<String> transInhibitors,
			List<String> nonEnzymeCatalysts)
			throws RateLawNotApplicableException {

		ASTNode kineticLaw = new ASTNode(this);
		kineticLaw = ASTNode.sum(synrate(), reg_function());
		return kineticLaw;
	}

	/*
	 * (Kein Javadoc)
	 * 
	 * @see org.sbml.squeezer.kinetics.BasicKineticLaw#getSimpleName()
	 */
	public String getSimpleName() {
		return "A special Hill-Equation";
	}

	/**
	 * @return ASTNode
	 */
	ASTNode reg_function() {

		Reaction r = getParentSBMLObject();
		String rId = getParentSBMLObject().getId();
		ASTNode node = new ASTNode(this);

		SpeciesReference reactant = r.getReactant(0);
		Parameter preactant = parameterW(reactant.getSpecies(), rId);
		ASTNode preactantnode = new ASTNode(preactant, this);
		Parameter thetareactant = parameterTheta(rId, reactant.getSpecies());
		ASTNode thetareactantnode = new ASTNode(thetareactant, this);
		Parameter coeffreactant = parameterHillCoefficient(reactant
				.getSpecies());
		ASTNode coeffreactantnode = new ASTNode(coeffreactant, this);

		node = ASTNode.frac(ASTNode.times(preactantnode, ASTNode.pow(
				speciesTerm(reactant), coeffreactantnode)), ASTNode.sum(ASTNode
				.pow(speciesTerm(reactant), coeffreactantnode), ASTNode.pow(
				thetareactantnode, coeffreactantnode)));

		for (int modifierNum = 0; modifierNum < r.getNumModifiers(); modifierNum++) {

			ModifierSpeciesReference modifier = r.getModifier(modifierNum);

			if (!modifier.isSetSBOTerm())
				modifier.setSBOTerm(19);
			if (SBO.isModifier(modifier.getSBOTerm())) {
				Parameter p = parameterW(modifier.getSpecies(), rId);
				ASTNode pnode = new ASTNode(p, this);
				Parameter theta = parameterTheta(rId, modifier.getSpecies());
				ASTNode thetanode = new ASTNode(theta, this);
				Parameter coeff = parameterHillCoefficient(modifier
						.getSpecies());
				ASTNode coeffnode = new ASTNode(coeff, this);

				if (node.isUnknown())
					node = ASTNode.frac(ASTNode.times(pnode, ASTNode.pow(
							speciesTerm(modifier), coeffnode)), ASTNode.sum(
							ASTNode.pow(speciesTerm(modifier), coeffnode),
							ASTNode.pow(thetanode, coeffnode)));
				else
					node = ASTNode.sum(node, ASTNode.frac(ASTNode.times(pnode,
							ASTNode.pow(speciesTerm(modifier), coeffnode)),
							ASTNode.sum(ASTNode.pow(speciesTerm(modifier),
									coeffnode), ASTNode.pow(thetanode,
									coeffnode))));
			}
		}
		if (node.isUnknown())
			return null;
		else
			return node;
	}

	/**
	 * synthesis rate
	 * 
	 * @return ASTNode
	 */
	ASTNode synrate() {
		String rId = getParentSBMLObject().getId();

		Parameter b_i = parameterB(rId);
		ASTNode b_i_node = new ASTNode(b_i, this);
		return b_i_node;
	}

}
