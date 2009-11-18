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
 * This class creates an equation based on an linear additive model as defined in the paper
 * "The NetGenerator Algorithm: Reconstruction of Gene Regulatory Networks"
 * of Töpfer, S.; Guthke, R.; Driesch, D.; Wötzel, D. & Pfaff 2007
 * 
 * @author <a href="mailto:snitschm@gmx.de">Sandra Nitschmann</a>
 *
 */
public class GRNAdditiveModel_NGlinear extends GRNAdditiveModel implements InterfaceGeneRegulatoryKinetics {
	
	/**
	 * 
	 * @param parentReaction
	 * @param typeParameters
	 * @throws RateLawNotApplicableException
	 */
	public GRNAdditiveModel_NGlinear(Reaction parentReaction,
			Object... typeParameters) throws RateLawNotApplicableException {
		super(parentReaction, typeParameters);
	}
	
	ASTNode b_i() {
		return null;
	}

	ASTNode m_i() {
		return new ASTNode(1, this);
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.sbml.squeezer.kinetics.GRNAdditiveModel#getSimpleName()
	 */
	public String getSimpleName() {
		return "A special additive model equation (NetGenerator linear model)";
	}
}
