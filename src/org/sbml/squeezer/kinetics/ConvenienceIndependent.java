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

import java.util.IllegalFormatException;
import java.util.List;

import org.sbml.jsbml.ASTNode;
import org.sbml.jsbml.Parameter;
import org.sbml.jsbml.Reaction;
import org.sbml.jsbml.SpeciesReference;
import org.sbml.squeezer.RateLawNotApplicableException;

/**
 * <p>
 * This is the thermodynamically independent form of the convenience kinetics.
 * In cases that the stochiometric matrix has full column rank the less
 * complicated {@link Convenience} can bee invoked.
 * </p>
 * <p>
 * Creates the Convenience kinetic's thermodynamicely independent form. This
 * method in general works the same way as the according one for the convenience
 * kinetic. Each enzyme's fraction is multiplied with a reaction constant that
 * is global for the eynzme's reaction.
 * </p>
 * 
 * @since 1.0
 * @version
 * @author <a href="mailto:Nadine.hassis@gmail.com">Nadine Hassis </a>
 * @author <a href="mailto:andreas.draeger@uni-tuebingen.de">Andreas
 *         Dr&auml;ger</a>
 * @author <a href="mailto:hannes.borch@googlemail.com">Hannes Borch</a>
 * @date Aug 1, 2007
 */
public class ConvenienceIndependent extends Convenience {

	/**
	 * 
	 * @param parentReaction
	 * @throws RateLawNotApplicableException
	 * @throws IllegalFormatException
	 */
	public ConvenienceIndependent(Reaction parentReaction)
			throws RateLawNotApplicableException, IllegalFormatException {
		super(parentReaction);
	}

	public static boolean isApplicable(Reaction reaction) {
		// TODO
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.sbml.squeezer.kinetics.Convenience#createKineticEquation(java.util
	 * .List, java.util.List, java.util.List, java.util.List, java.util.List,
	 * java.util.List)
	 */
	// @Override
	ASTNode createKineticEquation(List<String> modE, List<String> modActi,
			List<String> modTActi, List<String> modInhib,
			List<String> modTInhib, List<String> modCat)
			throws RateLawNotApplicableException, IllegalFormatException {
		final boolean FORWARD = true;
		final boolean REVERSE = false;
		Reaction reaction = getParentSBMLObject();
		setSBOTerm(429);

		if (reaction.getReversible())
			setNotes("reversible thermodynamically independent convenience kinetics");
		else
			setNotes("irreversible thermodynamically independent convenience kinetics");

		ASTNode[] enzymes = new ASTNode[Math.max(modE.size(), 1)];
		for (int i = 0; i < enzymes.length; i++) {
			ASTNode numerator, denominator = null;
			String enzyme = modE.size() > 0 ? modE.get(i) : null;
			if (!reaction.getReversible()) {
				numerator = ASTNode.times(numeratorElements(enzyme, FORWARD));
				denominator = ASTNode
						.times(denominatorElements(enzyme, FORWARD));
			} else {
				numerator = ASTNode.diff(numeratorElements(enzyme, FORWARD),
						numeratorElements(enzyme, REVERSE));
				denominator = ASTNode.sum(ASTNode.times(denominatorElements(
						enzyme, FORWARD)), ASTNode.times(denominatorElements(
						enzyme, REVERSE)));
				if (reaction.getNumProducts() > 1
						&& reaction.getNumReactants() > 1)
					denominator.minus(1);
			}
			StringBuffer klV = concat("kV_", reaction.getId());
			if (enzyme != null)
				append(klV, underscore, modE.get(i));
			Parameter p_klV = createOrGetParameter(klV.toString());
			if (denominator != null)
				numerator.divideBy(denominator);
			enzymes[i] = ASTNode.times(new ASTNode(p_klV, this), numerator);
			if (enzyme != null)
				enzymes[i] = ASTNode.times(new ASTNode(enzyme, this),
						enzymes[i]);
		}
		return ASTNode.times(activationFactor(modActi),
				inhibitionFactor(modInhib), ASTNode.sum(enzymes));
	}

	/**
	 * Returns an array containing the factors of the reactants and products
	 * included in the convenience kinetic's numerator. Each factor is given by
	 * the quotient of the respective species' concentration and it's related
	 * equilibrium constant to the power of the species' stoichiometry,
	 * multiplied with the product of equilibrium constant and a dimensionless
	 * energy constant to the power of half the species' stoichiometry. This
	 * method is applicable for both forward and backward reactions.
	 * 
	 * @param enzyme
	 * @param type
	 * @return true means forward, false means reverse.
	 */
	private ASTNode numeratorElements(String enzyme, boolean type) {
		Reaction reaction = getParentSBMLObject();

		ASTNode[] reactants = new ASTNode[reaction.getNumReactants()];
		ASTNode[] products = new ASTNode[reaction.getNumProducts()];
		ASTNode[] reactantsroot = new ASTNode[reaction.getNumReactants()];
		ASTNode[] productroot = new ASTNode[reaction.getNumProducts()];
		ASTNode equation;
		StringBuffer kiG;

		for (int i = 0; i < reaction.getNumReactants(); i++) {
			SpeciesReference ref = reaction.getReactant(i);
			StringBuffer kM = concat("kM_", reaction.getId());
			if (enzyme != null)
				append(kM, underscore, enzyme);
			append(kM, underscore, ref.getSpecies());
			Parameter p_kM = createOrGetParameter(kM.toString());
			p_kM.setSBOTerm(322);
			kiG = concat("kG_", ref.getSpecies());
			Parameter p_kiG = createOrGetGlobalParameter(kiG.toString());
			reactants[i] = ASTNode.pow(ASTNode.frac(new ASTNode(ref
					.getSpeciesInstance(), this), new ASTNode(p_kM, this)),
					new ASTNode(ref.getStoichiometry(), this));
			reactantsroot[i] = ASTNode.pow(ASTNode.times(new ASTNode(p_kiG,
					this), new ASTNode(p_kM, this)), new ASTNode(ref
					.getStoichiometry(), this));
		}

		for (int i = 0; i < reaction.getNumProducts(); i++) {
			SpeciesReference ref = reaction.getProduct(i);
			StringBuffer kM = concat("kM_", reaction.getId());
			if (enzyme != null)
				append(kM, underscore, enzyme);
			append(kM, underscore, ref.getSpecies());
			Parameter p_kM = createOrGetParameter(kM.toString());
			p_kM.setSBOTerm(323);
			kiG = concat("kG_", ref.getSpecies());
			Parameter p_kiG = createOrGetGlobalParameter(kiG.toString());
			products[i] = ASTNode.pow(ASTNode.frac(new ASTNode(ref
					.getSpeciesInstance(), this), new ASTNode(p_kM, this)),
					new ASTNode(ref.getStoichiometry(), this));
			productroot[i] = ASTNode.pow(ASTNode.times(
					new ASTNode(p_kiG, this), new ASTNode(p_kM, this)),
					new ASTNode(ref.getStoichiometry(), this));
		}

		if (type)
			equation = ASTNode.times(ASTNode.times(reactants), ASTNode
					.sqrt(ASTNode.frac(reactantsroot.length > 0 ? ASTNode
							.times(reactantsroot) : new ASTNode(1, this),
							productroot.length > 0 ? ASTNode.times(productroot)
									: new ASTNode(1, this))));
		else
			equation = ASTNode.times(ASTNode.times(products), ASTNode
					.sqrt(ASTNode.frac(productroot.length > 0 ? ASTNode
							.times(productroot) : new ASTNode(1, this),
							reactantsroot.length > 0 ? ASTNode
									.times(reactantsroot)
									: new ASTNode(1, this))));
		return equation;
	}
}
