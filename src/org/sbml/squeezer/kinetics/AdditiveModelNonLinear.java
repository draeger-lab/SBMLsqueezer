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

import java.util.ResourceBundle;

import org.sbml.jsbml.ASTNode;
import org.sbml.jsbml.Reaction;
import org.sbml.jsbml.Unit;
import org.sbml.squeezer.RateLawNotApplicableException;
import org.sbml.squeezer.util.Bundles;

import de.zbit.sbml.util.SBMLtools;
import de.zbit.util.ResourceManager;

/**
 * This class creates an equation based on a non-linear additive model.
 * 
 * @author Sandra Nitschmann
 * @author Andreas Dr&auml;ger
 * @since 1.3
 * @version $Rev$
 */
public class AdditiveModelNonLinear extends AdditiveModelLinear implements
		InterfaceGeneRegulatoryKinetics, InterfaceModulatedKinetics,
		InterfaceIrreversibleKinetics, InterfaceReversibleKinetics {
	
	public static final transient ResourceBundle MESSAGES = ResourceManager.getBundle(Bundles.MESSAGES);

	/**
	 * Generated serial version identifier.
	 */
	private static final long serialVersionUID = 7012411486160642421L;

	/**
	 * @param parentReaction
	 * @param typeParameters
	 * @throws RateLawNotApplicableException
	 */
	public AdditiveModelNonLinear(Reaction parentReaction,
			Object... typeParameters) throws RateLawNotApplicableException {
		super(parentReaction, typeParameters);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.sbml.squeezer.kinetics.AdditiveModelLinear#activation(org.sbml.jsbml
	 * .ASTNode)
	 */
	@Override
	ASTNode activation(ASTNode g) {
		ASTNode node1 = new ASTNode(1, this);
		ASTNode node2 = new ASTNode(1, this);
		ASTNode node3;
		
		if (g == null) {
			node3 = new ASTNode(1, this);
		} else {
			node3 = g;
		}

		if (this.getLevel() > 2) {
			SBMLtools.setUnits(node1, Unit.Kind.DIMENSIONLESS);
			SBMLtools.setUnits(node2, Unit.Kind.DIMENSIONLESS);
			if (!node3.isSetUnits()) {
				SBMLtools.setUnits(node3, Unit.Kind.DIMENSIONLESS);
			}
		}
			
		return ASTNode.frac(node1, ASTNode.sum(node2, 
				ASTNode.exp(ASTNode.uMinus(node3))));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.squeezer.kinetics.AdditiveModelLinear#getSimpleName()
	 */
	@Override
	public String getSimpleName() {
		return MESSAGES.getString("ADDITIVE_MODEL_NON_LINEAR_SIMPLE_NAME");
	}

}
