/*
 * $Id$
 * $URL$
 * ---------------------------------------------------------------------
 * This file is part of SBMLsqueezer, a Java program that creates rate 
 * equations for reactions in SBML files (http://sbml.org).
 *
 * Copyright (C) 2006-2011 by the University of Tuebingen, Germany.
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

import org.sbml.jsbml.ASTNode;
import org.sbml.jsbml.Reaction;
import org.sbml.squeezer.RateLawNotApplicableException;

/**
 * 
 * This class creates an equation based on an linear additive model as defined
 * in the paper &ldquo;The NetGenerator Algorithm: Reconstruction of Gene
 * Regulatory Networks&rdquo; of T&ouml;pfer, S.; Guthke, R.; Driesch, D.;
 * W&ouml;tzel, D., and Pfaff 2007
 * 
 * @author Sandra Nitschmann
 * @author Andreas Dr&auml;ger
 * @since 1.3
 * @version $Rev$
 */
public class NetGeneratorLinear extends AdditiveModelLinear implements
		InterfaceGeneRegulatoryKinetics, InterfaceModulatedKinetics,
		InterfaceIrreversibleKinetics, InterfaceReversibleKinetics {

	/**
	 * Generated serial version identifier.
	 */
	private static final long serialVersionUID = 7794507034989326338L;

	/**
	 * @param parentReaction
	 * @param typeParameters
	 * @throws RateLawNotApplicableException
	 */
	public NetGeneratorLinear(Reaction parentReaction, Object... typeParameters)
			throws RateLawNotApplicableException {
		super(parentReaction, typeParameters);
	}

	@Override
	ASTNode b_i() {
		return null;
	}

	@Override
	public String getSimpleName() {
		return "Linear additive model, NetGenerator form";
	}

	@Override
	ASTNode m() {
		return new ASTNode(1, this);
	}
}
