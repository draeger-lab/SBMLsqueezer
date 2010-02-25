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
import org.sbml.jsbml.Species;
import org.sbml.jsbml.SpeciesReference;
import org.sbml.squeezer.RateLawNotApplicableException;
import org.sbml.squeezer.ReactionType;

/**
 * This class creates an equation based on a linear additive model.
 * 
 * @author <a href="mailto:snitschm@gmx.de">Sandra Nitschmann</a>
 * @author <a href="mailto:andreas.draeger@uni-tuebingen.de">Andreas
 *         Dr&auml;ger</a>
 * @since 1.3
 * 
 */
public class AdditiveModelLinear extends BasicKineticLaw implements
		InterfaceGeneRegulatoryKinetics {

	/**
	 * @param parentReaction
	 * @param typeParameters
	 * @throws RateLawNotApplicableException
	 */
	public AdditiveModelLinear(Reaction parentReaction,
			Object... typeParameters) throws RateLawNotApplicableException {
		super(parentReaction, typeParameters);
	}

	/**
	 * @param g
	 * @return ASTNode
	 */
	ASTNode activation(ASTNode g) {
		return g == null ? new ASTNode(1, this) : g;
	}

	/**
	 * @return ASTNode
	 */
	ASTNode b_i() {
		return new ASTNode(parameterB(getParentSBMLObject().getId()), this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.sbml.squeezer.kinetics.BasicKineticLaw#createKineticEquation(java
	 * .util.List, java.util.List, java.util.List, java.util.List)
	 */
	ASTNode createKineticEquation(List<String> modE, List<String> modActi,
			List<String> modInhib, List<String> modCat)
			throws RateLawNotApplicableException {
		ASTNode m = m();
		ASTNode a = activation(g(w(), v(), b_i()));
		if (m.isOne() && a.isOne())
			return m;
		return ASTNode.times(m(), a);
	}

	/**
	 * @param w
	 * @param v
	 * @param b
	 * @return ASTNode
	 */
	ASTNode g(ASTNode w, ASTNode v, ASTNode b) {
		return ASTNode.sum(w, v, b);
	}

	/*
	 * (Kein Javadoc)
	 * 
	 * @see org.sbml.squeezer.kinetics.BasicKineticLaw#getSimpleName()
	 */
	public String getSimpleName() {
		return "Linear additive model, general form";
	}

	/**
	 * @return ASTNode
	 */
	ASTNode m() {
		return new ASTNode(parameterM(getParentSBMLObject().getId()), this);
	}

	/**
	 * weighted sum over all external factors
	 * 
	 * @return ASTNode
	 */
	ASTNode v() {
		Reaction r = getParentSBMLObject();
		ASTNode node = new ASTNode(this);
		for (ModifierSpeciesReference modifier : r.getListOfModifiers()) {
			Species modifierSpec = modifier.getSpeciesInstance();
			if (!(SBO.isProtein(modifierSpec.getSBOTerm())
					|| SBO.isGeneric(modifierSpec.getSBOTerm())
					|| SBO.isRNAOrMessengerRNA(modifierSpec.getSBOTerm()) || SBO
					.isGeneOrGeneCodingRegion(modifierSpec.getSBOTerm()))) {
				if (!modifier.isSetSBOTerm())
					modifier.setSBOTerm(19);
				if (SBO.isModifier(modifier.getSBOTerm())) {
					ASTNode modnode = speciesTerm(modifier);
					Parameter p = parameterV(modifier.getSpecies(), r.getId());
					ASTNode pnode = new ASTNode(p, this);
					node = node.isUnknown() ? ASTNode.times(pnode, modnode)
							: ASTNode.sum(node, ASTNode.times(pnode, modnode));
				}
			}
		}
		return node.isUnknown() ? null : node;
	}

	/**
	 * weighted sum over all interacting RNAs
	 * 
	 * @return ASTNode
	 */
	ASTNode w() {
		Reaction r = getParentSBMLObject();
		ASTNode node = new ASTNode(this);
		if (!ReactionType.representsEmptySet(r.getListOfProducts()))
			for (SpeciesReference product : r.getListOfProducts()) {
				Parameter p = parameterW(product.getSpecies(), r.getId());
				node = node.isUnknown() ? ASTNode.times(new ASTNode(p, this),
						speciesTerm(product)) : ASTNode.sum(node, ASTNode
						.times(new ASTNode(p, this), speciesTerm(product)));
			}
		for (ModifierSpeciesReference modifier : r.getListOfModifiers()) {
			Species modifierSpec = modifier.getSpeciesInstance();
			if (SBO.isProtein(modifierSpec.getSBOTerm())
					|| SBO.isGeneric(modifierSpec.getSBOTerm())
					|| SBO.isRNAOrMessengerRNA(modifierSpec.getSBOTerm())
					|| SBO.isGeneOrGeneCodingRegion(modifierSpec.getSBOTerm())) {
				if (!modifier.isSetSBOTerm())
					modifier.setSBOTerm(19);
				if (SBO.isModifier(modifier.getSBOTerm())) {
					Parameter p = parameterW(modifier.getSpecies(), r.getId());
					node = node.isUnknown() ? ASTNode.times(
							new ASTNode(p, this), speciesTerm(modifier))
							: ASTNode.sum(node, ASTNode.times(new ASTNode(p,
									this), speciesTerm(modifier)));
				}
			}
		}
		return node.isUnknown() ? null : node;
	}
}
