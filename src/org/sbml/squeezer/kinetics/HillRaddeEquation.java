/*
 * $Id$
 * $URL$
 * ---------------------------------------------------------------------
 * This file is part of SBMLsqueezer, a Java program that creates rate 
 * equations for reactions in SBML files (http://sbml.org).
 *
 * Copyright (C) 2006-2012 by the University of Tuebingen, Germany.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 * ---------------------------------------------------------------------
 */
package org.sbml.squeezer.kinetics;

import java.util.List;
import java.util.ResourceBundle;

import org.sbml.jsbml.ASTNode;
import org.sbml.jsbml.LocalParameter;
import org.sbml.jsbml.ModifierSpeciesReference;
import org.sbml.jsbml.Reaction;
import org.sbml.jsbml.SBO;
import org.sbml.squeezer.RateLawNotApplicableException;
import org.sbml.squeezer.util.Bundles;

import de.zbit.sbml.util.SBMLtools;
import de.zbit.util.ResourceManager;

/**
 * This class creates Hill equation as defined in the PhD thesis &ldquo;Modeling
 * Non-Linear Dynamic Phenomena in Biochemical Networks&rdquo; of Nicole Radde
 * and the paper &ldquo;Modeling Non-Linear Dynamic Phenomena in Biochemical
 * Networks&ldquo; of Nicole Radde and Lars Kaderali.
 * 
 * @author Sandra Nitschmann
 * @author Andreas Dr&auml;ger
 * @author Sarah R. M&uuml;ller vom Hagen
 * @since 1.3
 * @version $Rev$
 */
public class HillRaddeEquation extends BasicKineticLaw implements
		InterfaceGeneRegulatoryKinetics, InterfaceModulatedKinetics,
		InterfaceIrreversibleKinetics, InterfaceReversibleKinetics {

	public static final transient ResourceBundle MESSAGES = ResourceManager.getBundle(Bundles.MESSAGES);
	
	/**
	 * Generated serial version identifier.
	 */
	private static final long serialVersionUID = -2127004439071244185L;

	/**
	 * @param parentReaction
	 * @param typeParameters
	 * @throws RateLawNotApplicableException
	 */
	public HillRaddeEquation(Reaction parentReaction, Object... typeParameters)
			throws RateLawNotApplicableException {
		super(parentReaction, typeParameters);
	}

	/* (non-Javadoc)
	 * @see org.sbml.squeezer.kinetics.BasicKineticLaw#createKineticEquation(java.util.List, java.util.List, java.util.List, java.util.List)
	 */
	ASTNode createKineticEquation(List<String> enzymes,
			List<String> activators, List<String> inhibitors,
			List<String> nonEnzymeCatalysts)
			throws RateLawNotApplicableException {

		ASTNode kineticLaw = new ASTNode(this);
		kineticLaw = ASTNode.sum(synrate(), regulation());
		return kineticLaw;
	}

	/* (non-Javadoc)
	 * @see org.sbml.squeezer.kinetics.BasicKineticLaw#getSimpleName()
	 */
	public String getSimpleName() {
		return MESSAGES.getString("HILL_RADDE_EQUATION");
	}

	/**
	 * @return ASTNode
	 */
	ASTNode regulation() {

		Reaction r = getParentSBMLObject();
		String rId = getParentSBMLObject().getId();
		ASTNode node = new ASTNode(this);

		for (int modifierNum = 0; modifierNum < r.getModifierCount(); modifierNum++) {

			ModifierSpeciesReference modifier = r.getModifier(modifierNum);

			if (!modifier.isSetSBOTerm()) {
				SBMLtools.setSBOTerm(modifier,19);
			}
			if (SBO.isModifier(modifier.getSBOTerm())) {
				LocalParameter p = parameterFactory.parameterW(modifier
						.getSpecies(), rId);
				ASTNode pnode = new ASTNode(p, this);
				LocalParameter theta = parameterFactory.parameterTheta(rId, modifier.getSpecies());
				ASTNode thetanode = new ASTNode(theta, this);
				LocalParameter coeff = parameterFactory.parameterHillCoefficient(modifier.getSpecies());
				ASTNode coeffnode = new ASTNode(coeff, this);

				if (node.isUnknown()) {
					//TODO: ASTNode.pow(speciesTerm(modifier), coeffnode) and
					//		ASTNode.pow(thetanode, coeffnode) have different units
					node = ASTNode.frac(
						ASTNode.times(
							pnode,
							ASTNode.pow(speciesTerm(modifier), coeffnode)
						),
						ASTNode.sum(
							ASTNode.pow(speciesTerm(modifier), coeffnode),
							ASTNode.pow(thetanode, coeffnode)
						)
					);
				} else {
					node = ASTNode.sum(
						node,
						ASTNode.frac(
							ASTNode.times(
								pnode,
							  ASTNode.pow(speciesTerm(modifier), coeffnode)
							),
							ASTNode.sum(
								ASTNode.pow(speciesTerm(modifier), coeffnode),
								ASTNode.pow(thetanode, coeffnode)
							)
						)
					);
				}
						
			}
		}
		return node.isUnknown() ? null : node;
	}

	/**
	 * synthesis rate
	 * 
	 * @return ASTNode
	 */
	//TODO: produces wrong unit: s^(-1) instead of mol*s^(-1)
	ASTNode synrate() {
		String rId = getParentSBMLObject().getId();
		LocalParameter b_i = parameterFactory.parameterB(rId);
		ASTNode b_i_node = new ASTNode(b_i, this);
		return b_i_node;
	}

}
