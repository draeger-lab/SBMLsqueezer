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
import java.util.List;

import org.sbml.ASTNode;
import org.sbml.Model;
import org.sbml.Reaction;
import org.sbml.SpeciesReference;

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
	 * @param model
	 * @param reversibility
	 * @throws RateLawNotApplicableException
	 * @throws IOException
	 * @throws IllegalFormatException
	 */
	public ConvenienceIndependent(Reaction parentReaction, Model model)
			throws RateLawNotApplicableException, IOException,
			IllegalFormatException {
		super(parentReaction, model);
	}

	/**
	 * @param parentReaction
	 * @param model
	 * @param listOfPossibleEnzymes
	 * @param reversibility
	 * @throws RateLawNotApplicableException
	 * @throws IOException
	 * @throws IllegalFormatException
	 */
	public ConvenienceIndependent(Reaction parentReaction, Model model,
			List<String> listOfPossibleEnzymes)
			throws RateLawNotApplicableException, IOException,
			IllegalFormatException {
		super(parentReaction, model, listOfPossibleEnzymes);
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

	// @Override
	public String getSBO() {
		return "none";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seeorg.sbmlsqueezer.kinetics.Convenience#createKineticEquation(jp.sbi.
	 * celldesigner.plugin.PluginModel, java.util.List, java.util.List,
	 * java.util.List, java.util.List, java.util.List, java.util.List)
	 */
	protected ASTNode createKineticEquation(Model model, List<String> modE,
			List<String> modActi, List<String> modTActi, List<String> modInhib,
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
					enzymes[i] = new ASTNode("", this);
				addLocalParameter(klV);

				ASTNode numerator, denominator;
				if (!reaction.getReversible()) {
					numerator = ASTNode
							.times(numeratorElements(enzyme, FORWARD));
					denominator = ASTNode.times(denominatorElements(enzyme,
							FORWARD));
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
				enzymes[i] = ASTNode.times(enzymes[i], new ASTNode(klV, this),
						ASTNode.frac(numerator, denominator));
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

		ASTNode educts = new ASTNode("", this);
		ASTNode products = new ASTNode("", this);
		ASTNode eductroot = new ASTNode("", this);
		ASTNode productroot = new ASTNode("", this);
		ASTNode equation;
		StringBuffer kiG;

		for (int i = 0; i < reaction.getNumReactants(); i++) {
			SpeciesReference ref = reaction.getReactant(i);
			StringBuffer kM = concat("kM_", reaction.getId());
			if (enzyme != null)
				append(kM, underscore, enzyme);
			append(kM, underscore, ref.getSpecies());
			addLocalParameter(kM);
			kiG = concat("kG_", ref.getSpecies());
			addLocalParameter(kiG);
			educts = ASTNode.times(educts, ASTNode.pow(ASTNode.frac(
					new ASTNode(getSpecies(ref), this), new ASTNode(kM, this)),
					new ASTNode(ref.getStoichiometry(), this)));
			eductroot = ASTNode.times(eductroot, ASTNode.pow(ASTNode.times(
					new ASTNode(kiG, this), new ASTNode(kM, this)),
					new ASTNode(ref.getStoichiometry(), this)));
		}
		for (int i = 0; i < reaction.getNumProducts(); i++) {
			SpeciesReference ref = reaction.getProduct(i);
			StringBuffer kM = concat("kM_", reaction.getId());
			if (enzyme != null)
				append(kM, underscore, enzyme);
			append(kM, underscore, ref.getSpecies());
			addLocalParameter(kM);
			kiG = concat("kG_", ref.getSpecies());
			addLocalParameter(kiG);
			products = ASTNode.times(products, ASTNode.pow(ASTNode.frac(
					new ASTNode(getSpecies(ref), this), new ASTNode(kM, this)),
					new ASTNode(ref.getStoichiometry(), this)));
			productroot = ASTNode.times(productroot, ASTNode.pow(ASTNode.times(
					new ASTNode(kiG, this), new ASTNode(kM, this)),
					new ASTNode(ref.getStoichiometry(), this)));

		}
		equation = type ? ASTNode.times(educts, ASTNode.sqrt(ASTNode.frac(
				eductroot, productroot))) : ASTNode.times(products, ASTNode
				.sqrt(ASTNode.frac(productroot, eductroot)));
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

			ASTNode[] parts = new ASTNode[(int) ref.getStoichiometry()
					+ (noOne ? 0 : 1)];
			ASTNode part = ASTNode.frac(new ASTNode(getSpecies(ref), this),
					new ASTNode(kM, this));
			for (int j = 0; j < parts.length; j++)
				parts[j] = ASTNode.pow(part, new ASTNode((noOne ? j + 1 : j),
						this));
			denoms[i] = ASTNode.sum(parts);
		}
		return denoms;
	}
}
