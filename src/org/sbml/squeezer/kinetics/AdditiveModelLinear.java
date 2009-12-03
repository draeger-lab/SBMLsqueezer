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

/**
 * This class creates an equation based on a linear additive model.
 * 
 * @author <a href="mailto:snitschm@gmx.de">Sandra Nitschmann</a> 
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
	ASTNode actifunction(ASTNode g) {
		if (g == null)
			return new ASTNode(this);
		else
			return g;
	}

	/**
	 * @return ASTNode
	 */
	ASTNode b_i() {
		String rId = getParentSBMLObject().getId();

		Parameter b_i = parameterB(rId);
		ASTNode b_i_node = new ASTNode(b_i, this);
		return b_i_node;
	}

	/*
	 * (Kein Javadoc)
	 * 
	 * @see
	 * org.sbml.squeezer.kinetics.BasicKineticLaw#createKineticEquation(java
	 * .util.List, java.util.List, java.util.List, java.util.List,
	 * java.util.List, java.util.List)
	 */
	ASTNode createKineticEquation(List<String> modE, List<String> modActi,
			List<String> modTActi, List<String> modInhib,
			List<String> modTInhib, List<String> modCat)
			throws RateLawNotApplicableException {

		ASTNode kineticLaw = new ASTNode(this);

		kineticLaw = ASTNode.times(m_i(), actifunction(function_g(function_w(),
				function_v(), b_i())));

		return kineticLaw;
	}

	/**
	 * @param w
	 * @param v
	 * @param b
	 * @return ASTNode
	 */
	ASTNode function_g(ASTNode w, ASTNode v, ASTNode b) {
		return ASTNode.sum(w, v, b);
	}

	/**
	 * weighted sum over all external factors
	 * 
	 * @return ASTNode
	 */
	ASTNode function_v() {
		Reaction r = getParentSBMLObject();
		String rId = getParentSBMLObject().getId();
		ASTNode node = new ASTNode(this);
		for (int modifierNum = 0; modifierNum < r.getNumModifiers(); modifierNum++) {
			Species modifierspec = r.getModifier(modifierNum)
					.getSpeciesInstance();
			ModifierSpeciesReference modifier = r.getModifier(modifierNum);
			if (!(SBO.isProtein(modifierspec.getSBOTerm()) || SBO
					.isRNAOrMessengerRNA(modifierspec.getSBOTerm()))) {
				if (!modifier.isSetSBOTerm())
					modifier.setSBOTerm(19);
				if (SBO.isModifier(modifier.getSBOTerm())) {
					ASTNode modnode = speciesTerm(modifier);
					Parameter p = parameterV(modifier.getSpecies(), rId);
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

	/**
	 * weighted sum over all interacting RNAs
	 * 
	 * @return ASTNode
	 */
	ASTNode function_w() {
		Reaction r = getParentSBMLObject();
		String rId = getParentSBMLObject().getId();
		ASTNode node = new ASTNode(this);

		SpeciesReference reactant = r.getReactant(0);
		ASTNode reactantnode = speciesTerm(reactant);
		Parameter preactant = parameterW(reactant.getSpecies(), rId);
		ASTNode preactantnode = new ASTNode(preactant, this);
		node = ASTNode.times(preactantnode, reactantnode);

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
	 * @see org.sbml.squeezer.kinetics.BasicKineticLaw#getSimpleName()
	 */
	public String getSimpleName() {
		return "Additive model: linear";
	}

	/**
	 * @return ASTNode
	 */
	ASTNode m_i() {
		String rId = getParentSBMLObject().getId();
		Parameter m_i = parameterM(rId);
		ASTNode m_i_node = new ASTNode(m_i, this);
		return m_i_node;
	}

}
