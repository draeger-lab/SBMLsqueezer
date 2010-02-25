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
 * @author <a href="mailto:andreas.draeger@uni-tuebingen.de">Andreas
 *         Dr&auml;ger</a>
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
		return new ASTNode(parameterB(getParentSBMLObject().getId()), this);
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
			List<String> modInhib, List<String> modCat)
			throws RateLawNotApplicableException {
		return g(b_i(), v(), w());
	}

	/**
	 * 
	 * @param b
	 * @param v
	 * @param w
	 * @return
	 */
	ASTNode g(ASTNode b, ASTNode v, ASTNode w) {
		return ASTNode.sum(b, v, w);
	}

	/**
	 * @return ASTNode
	 */
	ASTNode v() {
		Reaction r = getParentSBMLObject();
		ASTNode node = new ASTNode(this);
		for (ModifierSpeciesReference modifier : r.getListOfModifiers()) {
			Species modifierspec = modifier.getSpeciesInstance();
			if (SBO.isProtein(modifierspec.getSBOTerm())
					|| SBO.isGeneric(modifierspec.getSBOTerm())
					|| SBO.isRNAOrMessengerRNA(modifierspec.getSBOTerm())
					|| SBO.isGeneOrGeneCodingRegion(modifierspec.getSBOTerm())) {
				if (!modifier.isSetSBOTerm())
					modifier.setSBOTerm(19);
				if (SBO.isModifier(modifier.getSBOTerm())) {
					Parameter p = parameterV(modifier.getSpecies(), r.getId());
					if (node.isUnknown())
						node = ASTNode.times(new ASTNode(p, this),
								speciesTerm(modifier));
					else
						node = ASTNode.sum(node, ASTNode.times(new ASTNode(p,
								this), speciesTerm(modifier)));
				}
			}
		}
		if (r.getNumProducts() > 0
				&& !SBO.isEmptySet(r.getProduct(0).getSpeciesInstance()
						.getSBOTerm()))
			return node.isUnknown() ? speciesTerm(r.getProduct(0)) : ASTNode
					.times(speciesTerm(r.getProduct(0)), node);
		return node.isUnknown() ? null : node;
	}

	/**
	 * @return ASTNode
	 */
	ASTNode w() {
		Reaction r = getParentSBMLObject();
		ASTNode node = new ASTNode(this);
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
					if (node.isUnknown())
						node = ASTNode.times(new ASTNode(p, this),
								speciesTerm(modifier));
					else
						node = ASTNode.sum(node, ASTNode.times(new ASTNode(p,
								this), speciesTerm(modifier)));
				}
			}
		}
		return node.isUnknown() ? null : node;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.squeezer.kinetics.BasicKineticLaw#getSimpleName()
	 */
	@Override
	public String getSimpleName() {
		return "H-system equation by Hadeler (2003)";
	}

}
