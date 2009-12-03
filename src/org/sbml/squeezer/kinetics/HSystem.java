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
 * This class creates a non-linear additive equation form
 * <ul>
 * <li>Hadeler, K.: &ldquo;Gedanken zur Parameteridentifikation.&rdquo; Personal
 * Communication, 2003, and</li>
 * <li>Spieth, C.; Hassis, N.; Streichert, F.; Supper, J.; Beyreuther, K., and
 * Zell, A. &ldquo;Comparing Mathematical Models on the Problem of Network
 * Inference&rdquo;, 2006.</li>
 * </ul>
 * 
 * @author <a href="mailto:snitschm@gmx.de">Sandra Nitschmann</a>
 * @since 1.3
 */
public class HSystem extends BasicKineticLaw implements
		InterfaceGeneRegulatoryKinetics {

	/**
	 * @param parentReaction
	 * @param typeParameters
	 * @throws RateLawNotApplicableException
	 */
	public HSystem(Reaction parentReaction, Object... typeParameters)
			throws RateLawNotApplicableException {
		super(parentReaction, typeParameters);
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

		kineticLaw = function_g(b_i(), function_w(), function_v());

		return kineticLaw;
	}

	/**
	 * @param w
	 * @param v
	 * @param b
	 * @return ASTNode
	 */
	ASTNode function_g(ASTNode b, ASTNode w, ASTNode v) {
		return ASTNode.sum(ASTNode.sum(b, w), v);
	}

	/**
	 * @return ASTNode
	 */
	ASTNode function_v() {
		Reaction r = getParentSBMLObject();
		String rId = getParentSBMLObject().getId();
		ASTNode node = new ASTNode(this);

		SpeciesReference reactant = r.getReactant(0);
		ASTNode reactantnode = speciesTerm(reactant);
		Parameter preactant = parameterV(reactant.getSpecies(), rId);
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
			return node = ASTNode.times(reactantnode, node);
	}

	/**
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
		return "H-System: Non-linear additive equation from K. Hadeler 2003";
	}

}
