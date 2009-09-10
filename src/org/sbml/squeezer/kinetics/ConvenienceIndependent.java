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

import java.io.IOException;
import java.util.IllegalFormatException;
import java.util.List;

import org.sbml.jlibsbml.ASTNode;
import org.sbml.jlibsbml.Parameter;
import org.sbml.jlibsbml.Reaction;
import org.sbml.jlibsbml.SpeciesReference;
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
	 * @param parentReaction
	 * @throws RateLawNotApplicableException
	 * @throws IOException
	 * @throws IllegalFormatException
	 */
	public ConvenienceIndependent(Reaction parentReaction)
			throws RateLawNotApplicableException, IOException,
			IllegalFormatException {
		super(parentReaction);
	}

	public static boolean isApplicable(Reaction reaction) {
		// TODO
		return true;
	}

	// @Override
	public String getName() {
		if (this.getParentSBMLObject().getReversible())
			return "reversible thermodynamically independent convenience kinetics";
		return "irreversible thermodynamically independent convenience kinetics";
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
		ASTNode[] enzymes = new ASTNode[Math.max(modE.size(), 1)];
		try {
			int i = 0;
			do {
				StringBuffer klV = concat("kV_", reaction.getId());
				String enzyme = modE.size() > 0 ? modE.get(i) : null;
				if (enzyme != null) {
					append(klV, underscore, modE.get(i));
					enzymes[i] = new ASTNode(enzyme, this);
				} else
					enzymes[i] = null;
				Parameter p_klV = new Parameter(klV.toString(), getLevel(),
						getVersion());
				addParameter(p_klV);

				ASTNode numerator, denominator = null;
				if (!reaction.getReversible()) {
					numerator = ASTNode
							.times(numeratorElements(enzyme, FORWARD));
					ASTNode domElem[] = denominatorElements(enzyme, FORWARD);
					if (domElem.length > 0)
						denominator = ASTNode.times(domElem);
				} else {
					numerator = ASTNode.diff(
							numeratorElements(enzyme, FORWARD),
							numeratorElements(enzyme, REVERSE));
					denominator = ASTNode
							.sum(ASTNode.times(denominatorElements(enzyme,
									FORWARD)),
									ASTNode.times(denominatorElements(enzyme,
											REVERSE)));
					if (reaction.getNumProducts() > 1
							&& reaction.getNumReactants() > 1)
						denominator = ASTNode.diff(denominator, new ASTNode(1,
								this));
				}
				enzymes[i] = ASTNode.times(enzymes[i],
						new ASTNode(p_klV, this), denominator != null ? ASTNode
								.frac(numerator, denominator) : numerator);
				i++;
			} while (i < enzymes.length);
			return ASTNode.times(activationFactor(modActi),
					inhibitionFactor(modInhib), ASTNode.sum(enzymes));
		} catch (IllegalFormatException exc) {
			exc.printStackTrace();
			return new ASTNode("", this);
		}
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

		ASTNode[] educts = new ASTNode[reaction.getNumReactants()];
		ASTNode[] products = new ASTNode[reaction.getNumProducts()];
		ASTNode[] eductroot = new ASTNode[reaction.getNumReactants()];
		ASTNode[] productroot = new ASTNode[reaction.getNumProducts()];
		ASTNode equation;
		StringBuffer kiG;

		for (int i = 0; i < reaction.getNumReactants(); i++) {
			SpeciesReference ref = reaction.getReactant(i);
			StringBuffer kM = concat("kM_", reaction.getId());
			if (enzyme != null)
				append(kM, underscore, enzyme);
			append(kM, underscore, ref.getSpecies());
			Parameter p_kM = new Parameter(kM.toString(), getLevel(),
					getVersion());
			addParameter(p_kM);
			kiG = concat("kG_", ref.getSpecies());
			Parameter p_kiG = new Parameter(kiG.toString(), getLevel(),
					getVersion());
			addGlobalParameter(p_kiG);
			educts[i] = ASTNode.pow(ASTNode.frac(new ASTNode(ref
					.getSpeciesInstance(), this), new ASTNode(p_kM, this)),
					new ASTNode(ref.getStoichiometry(), this));
			eductroot[i] = ASTNode.pow(ASTNode.times(new ASTNode(p_kiG, this),
					new ASTNode(p_kM, this)), new ASTNode(ref
					.getStoichiometry(), this));
		}

		for (int i = 0; i < reaction.getNumProducts(); i++) {
			SpeciesReference ref = reaction.getProduct(i);
			StringBuffer kM = concat("kM_", reaction.getId());
			if (enzyme != null)
				append(kM, underscore, enzyme);
			append(kM, underscore, ref.getSpecies());
			Parameter p_kM = new Parameter(kM.toString(), getLevel(),
					getVersion());
			addParameter(p_kM);
			kiG = concat("kG_", ref.getSpecies());
			Parameter p_kiG = new Parameter(kiG.toString(), getLevel(),
					getVersion());
			addGlobalParameter(p_kiG);
			products[i] = ASTNode.pow(ASTNode.frac(new ASTNode(ref
					.getSpeciesInstance(), this), new ASTNode(p_kM, this)),
					new ASTNode(ref.getStoichiometry(), this));
			productroot[i] = ASTNode.pow(ASTNode.times(
					new ASTNode(p_kiG, this), new ASTNode(p_kM, this)),
					new ASTNode(ref.getStoichiometry(), this));

		}

		/*
		 * TODO: catch special cases for empty list of products or reactants.
		 */

		if (type) {
			/*
			 * if (educts.length == 0) equation = ASTNode.sqrt(ASTNode.frac(new
			 * ASTNode(1, this), ASTNode.times(productroot))); else
			 */
			equation = ASTNode.times(ASTNode.times(educts), ASTNode
					.sqrt(ASTNode.frac(ASTNode.times(eductroot), ASTNode
							.times(productroot))));
		} else {
			/*
			 * if (products.length == 0) equation = new ASTNode(1, this); else
			 * if (productroot.length > 0 && eductroot.length > 0) {
			 */
			equation = ASTNode.times(ASTNode.times(products), ASTNode
					.sqrt(ASTNode.frac(ASTNode.times(productroot), ASTNode
							.times(eductroot))));
			/*
			 * } else equation = ASTNode.times(ASTNode.times(products), ASTNode
			 * .sqrt(ASTNode.frac(new ASTNode(1, this), ASTNode
			 * .times(eductroot))));
			 */
		}
		return equation;
	}

	/**
	 * Returns an array containing the factors of the reactants and products
	 * included in the convenience kinetic's denominator. For each factor, the
	 * respective species' concentration and it's equilibrium constant are
	 * divided and raised to the power of each integer value between zero and
	 * the species' stoichiometry. All of the species' powers are summed up to
	 * form the species' factor in the product. The method is applicable for
	 * both forward and backward reactions.
	 * 
	 * @param reaction
	 * @param type
	 *            true means forward, false backward.
	 * @return
	 */
	private ASTNode[] denominatorElements(String enzyme, boolean type) {
		Reaction reaction = getParentSBMLObject();
		ASTNode[] denoms = new ASTNode[type ? reaction.getNumReactants()
				: reaction.getNumProducts()];
		boolean noOne = (denoms.length == 1)
				&& (!type || (type && reaction.getReversible() && reaction
						.getNumProducts() > 1));
		for (int i = 0; i < denoms.length; i++) {
			SpeciesReference ref = type ? reaction.getReactant(i) : reaction
					.getProduct(i);
			StringBuffer kM = concat("kM_", getParentSBMLObject().getId());
			if (enzyme != null)
				append(kM, underscore, enzyme);
			append(kM, underscore, ref.getSpecies());
			Parameter p_kM = new Parameter(kM.toString(), getLevel(),
					getVersion());
			addParameter(p_kM);

			ASTNode[] parts = new ASTNode[(int) ref.getStoichiometry()
					+ (noOne ? 0 : 1)];
			ASTNode part = ASTNode.frac(new ASTNode(ref.getSpeciesInstance(),
					this), new ASTNode(p_kM, this));
			for (int j = 0; j < parts.length; j++)
				parts[j] = ASTNode.pow(part, new ASTNode((noOne ? j + 1 : j),
						this));
			denoms[i] = ASTNode.sum(parts);
		}
		return denoms;
	}
}
