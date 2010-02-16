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
import org.sbml.jsbml.ListOf;
import org.sbml.jsbml.Model;
import org.sbml.jsbml.ModifierSpeciesReference;
import org.sbml.jsbml.Parameter;
import org.sbml.jsbml.Reaction;
import org.sbml.jsbml.SBO;
import org.sbml.jsbml.Species;
import org.sbml.jsbml.SpeciesReference;
import org.sbml.squeezer.RateLawNotApplicableException;

/**
 * 
 * This class creates a S-System equation as defined in the papers:
 * <ul>
 * <li>Tournier, L.: &ldquo;Approximation of dynamical systems using S-systems
 * theory: application to biological systems&rdquo;</li>
 * <li>Spieth, C.; Streichert, F.; Speer, N., and Zell, A.: &ldquo;Optimizing
 * Topology and Parameters of Gene Regulatory Network Models from Time-Series
 * Experiments&rdquo;</li>
 * <li>Spieth, C.; Hassis, N.; Streichert, F.; Supper, J.; Beyreuther, K., and
 * Zell, A.: &ldquo;Comparing Mathematical Models on the Problem of Network
 * Inference&rdquo; and</li>
 * <li>Hecker, M.; Lambeck, S.; T&ouml;pfer, S.; Someren, E. van, and Guthke,
 * R.: &ldquo;Gene regulatory network inference: data integration in dynamic
 * models-a review&rdquo;</li>
 * </ul>
 * 
 * @author <a href="mailto:snitschm@gmx.de">Sandra Nitschmann</a>
 * @quthor <a href="mailto:andreas.draeger@uni-tuebingen.de">Andreas
 *         Dr&auml;ger</a>
 * @since 1.3
 */
public class SSystem extends BasicKineticLaw implements
		InterfaceGeneRegulatoryKinetics {

	/**
	 * @param parentReaction
	 * @param typeParameters
	 * @throws RateLawNotApplicableException
	 */
	public SSystem(Reaction parentReaction, Object... typeParameters)
			throws RateLawNotApplicableException {
		super(parentReaction, typeParameters);
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
		Reaction r = getParentSBMLObject();
		ASTNode node = prod(r.getListOfReactants(), true);
		if (r.getReversible()) {
			if (node.isUnknown())
				node = ASTNode.uMinus(prod(r.getListOfProducts(), false));
			else
				node.minus(prod(r.getListOfProducts(), false));
		}
		for (ModifierSpeciesReference modifier : r.getListOfModifiers()) {
			Species modifierspec = modifier.getSpeciesInstance();
			if (SBO.isProtein(modifierspec.getSBOTerm())
					|| SBO.isGeneric(modifierspec.getSBOTerm())
					|| SBO.isRNAOrMessengerRNA(modifierspec.getSBOTerm())
					|| SBO.isGeneOrGeneCodingRegion(modifierspec.getSBOTerm())) {
				if (!modifier.isSetSBOTerm())
					modifier.setSBOTerm(19);
				if (SBO.isModifier(modifier.getSBOTerm())) {
					Parameter exp = parameterSSystemExponent(modifierspec
							.getId());
					String name = exp.getName();
					if (SBO.isStimulator(modifier.getSBOTerm())) {
						name.concat("_sti");
						exp.setName(name);
					}
					if (SBO.isInhibitor(modifier.getSBOTerm())) {
						name.concat("_inh");
						exp.setName(name);
					}
					ASTNode expnode = new ASTNode(exp, this);
					if (node.isUnknown())
						node = ASTNode.pow(speciesTerm(modifier), expnode);
					else
						node.multiplyWith(ASTNode.pow(speciesTerm(modifier),
								expnode));
				}
			}
		}
		return node.isUnknown() ? null : node;
	}

	/*
	 * (Kein Javadoc)
	 * 
	 * @see org.sbml.squeezer.kinetics.BasicKineticLaw#getSimpleName()
	 */
	public String getSimpleName() {
		return "S-System based kinetic";
	}

	/**
	 * The product term in S-Systems.
	 * 
	 * @param listOf
	 * @param forward
	 * @return
	 */
	private ASTNode prod(ListOf<SpeciesReference> listOf, boolean forward) {
		String rID = getParentSBMLObject().getId();
		ASTNode prod = new ASTNode(forward ? parameterAlpha(rID)
				: parameterBeta(rID), this);
		for (SpeciesReference specRef : listOf) {
			if (!SBO.isEmptySet(specRef.getSpeciesInstance().getSBOTerm())) {
				Parameter exponent = parameterSSystemExponent(specRef
						.getSpecies());
				ASTNode pow = ASTNode.pow(speciesTerm(specRef), new ASTNode(
						exponent, this));
				if (prod.isUnknown())
					prod = pow;
				else
					prod.multiplyWith(pow);
			}
		}
		return prod;
	}

	@Override
	Parameter parameterAlpha(String rId) {
		Parameter p = createOrGetParameter("alpha_", rId);
		if (!p.isSetSBOTerm())
			p.setSBOTerm(153);
		if (!p.isSetUnits()) {
			Model m = getModel();
			p.setUnits(unitSubstancePerTime(m.getUnitDefinition("substance"), m
					.getUnitDefinition("time")));
		}
		if (!p.isSetName())
			p.setName("rate constant for synthesis");
		return p;
	}

	@Override
	Parameter parameterBeta(String rId) {
		Parameter p = createOrGetParameter("beta_", rId);
		if (!p.isSetSBOTerm())
			p.setSBOTerm(156);
		if (!p.isSetUnits()) {
			Model m = getModel();
			p.setUnits(unitSubstancePerTime(m.getUnitDefinition("substance"), m
					.getUnitDefinition("time")));
		}
		if (!p.isSetName())
			p.setName("rate constant for degradation");
		return p;
	}
}
