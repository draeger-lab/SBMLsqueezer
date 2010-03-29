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
import org.sbml.jsbml.Reaction;
import org.sbml.squeezer.RateLawNotApplicableException;

/**
 * 
 * This class creates an equation based on an additive model as defined in the
 * papers &ldquo;Neural network model of gene expression.&rdquo; of
 * Vohradsk&yacute;, J. 2001 and &ldquo;Nonlinear differential equation model
 * for quantification of transcriptional regulation applied to microarray data
 * of <i>Saccharomyces cerevisiae</i>.&rdquo; of Vu, T. T. and Vohradsk&yacute;,
 * J. 2007
 * 
 * @author <a href="mailto:snitschm@gmx.de">Sandra Nitschmann</a>
 * @author <a href="mailto:andreas.draeger@uni-tuebingen.de">Andreas
 *         Dr&auml;ger</a>
 * @since 1.3
 */
public class Vohradsky extends AdditiveModelNonLinear implements
		InterfaceGeneRegulatoryKinetics, InterfaceModulatedKinetics,
		InterfaceIrreversibleKinetics, InterfaceReversibleKinetics,
		InterfaceZeroReactants {

	/**
	 * @param parentReaction
	 * @param typeParameters
	 * @throws RateLawNotApplicableException
	 */
	public Vohradsky(Reaction parentReaction, Object... typeParameters)
			throws RateLawNotApplicableException {
		super(parentReaction, typeParameters);
	}

	@Override
	public String getSimpleName() {
		return "Non-linear additive model by Vohradsk\u00fd (2001)";
	}

	@Override
	ASTNode v() {
		return null;
	}
}
