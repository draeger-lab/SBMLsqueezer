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
import org.sbml.jsbml.Reaction;
import org.sbml.jsbml.Species;
import org.sbml.squeezer.RateLawNotApplicableException;
import org.sbml.squeezer.ReactionType;
import org.sbml.squeezer.util.Bundles;

import de.zbit.util.ResourceManager;

/**
 * This class creates an equation based on an non-linear additive model as
 * defined in the paper &ldquo;The NetGenerator Algorithm: Reconstruction of
 * Gene Regulatory Networks&rdquo; of T&ouml;pfer, S.; Guthke, R.; Driesch, D.;
 * W&ouml;tzel, D., and Pfaff 2007
 * 
 * @author Sandra Nitschmann
 * @author Andreas Dr&auml;ger
 * @since 1.3
 * @version $Rev$
 */
public class NetGeneratorNonLinear extends AdditiveModelNonLinear implements
		InterfaceGeneRegulatoryKinetics, InterfaceModulatedKinetics,
		InterfaceIrreversibleKinetics, InterfaceReversibleKinetics {
	
	public static final transient ResourceBundle MESSAGES = ResourceManager.getBundle(Bundles.MESSAGES);

	/**
	 * Generated serial version identifier.
	 */
	private static final long serialVersionUID = 641441833130051384L;

	/**
	 * @param parentReaction
	 * @param typeParameters
	 * @throws RateLawNotApplicableException
	 */
	public NetGeneratorNonLinear(Reaction parentReaction,
			Object... typeParameters) throws RateLawNotApplicableException {
		super(parentReaction, typeParameters);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.sbml.squeezer.kinetics.AdditiveModelLinear#createKineticEquation(
	 * java.util.List, java.util.List, java.util.List, java.util.List)
	 */
	@Override
	ASTNode createKineticEquation(List<String> modE, List<String> modActi,
			List<String> modInhib, List<String> modCat)
			throws RateLawNotApplicableException {
		ASTNode m = super
				.createKineticEquation(modE, modActi, modInhib, modCat);
		Reaction r = getParentSBMLObject();
		if (!ReactionType.representsEmptySet(r.getListOfProducts())) {
			Species s = r.getProduct(0).getSpeciesInstance();
			return ASTNode.sum(m, ASTNode.times(new ASTNode(parameterFactory
					.parameterW(s.getId(), r.getId()), this), speciesTerm(s)));
		}
		return m;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.squeezer.kinetics.AdditiveModelNonLinear#getSimpleName()
	 */
	@Override
	public String getSimpleName() {
		return MESSAGES.getString("NET_GENERATOR_NON_LINEAR_SIMPLE_NAME");
	}
}
