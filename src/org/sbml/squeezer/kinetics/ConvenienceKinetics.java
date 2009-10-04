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
import org.sbml.jsbml.ListOf;
import org.sbml.jsbml.Parameter;
import org.sbml.jsbml.Reaction;
import org.sbml.jsbml.SpeciesReference;
import org.sbml.jsbml.Unit;
import org.sbml.squeezer.RateLawNotApplicableException;
import org.sbml.squeezer.io.StringTools;

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
 * @author <a href="mailto:andreas.draeger@uni-tuebingen.de">Andreas Dr&auml;ger</a>
 * @author <a href="mailto:hannes.borch@googlemail.com">Hannes Borch</a>
 * @date Aug 1, 2007
 */
public class ConvenienceKinetics extends GeneralizedMassAction implements
		InterfaceUniUniKinetics, InterfaceBiUniKinetics, InterfaceBiBiKinetics,
		InterfaceArbitraryEnzymeKinetics, InterfaceReversibleKinetics,
		InterfaceIrreversibleKinetics, InterfaceModulatedKinetics {

	/**
	 * 
	 * @param parentReaction
	 * @throws RateLawNotApplicableException
	 * @throws IllegalFormatException
	 */
	public ConvenienceKinetics(Reaction parentReaction, Object... types)
			throws RateLawNotApplicableException, IllegalFormatException {
		super(parentReaction, types);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.squeezer.kinetics.Convenience#createKineticEquation(java.util
	 *      .List, java.util.List, java.util.List, java.util.List,
	 *      java.util.List, java.util.List)
	 */
	// @Override
	ASTNode createKineticEquation(List<String> modE, List<String> modActi,
			List<String> modTActi, List<String> modInhib,
			List<String> modTInhib, List<String> modCat)
			throws RateLawNotApplicableException, IllegalFormatException {
		Reaction reaction = getParentSBMLObject();
		setSBOTerm(429);
		StringBuilder name = new StringBuilder();
		// if (!reaction.getReversible())
		// name.append("ir");
		// name.append("reversible ");
		boolean type = false;
		if (getTypeParameters().length > 1) {
			if (!Boolean.parseBoolean(getTypeParameters()[1].toString())) {
				name.append("thermodynamically independent ");
				type = true;
			}
		}
		name.append("convenience kinetics");
		setNotes(StringTools.firstLetterUpperCase(name.toString()));

		ASTNode[] enzymes = new ASTNode[Math.max(modE.size(), 1)];
		for (int i = 0; i < enzymes.length; i++) {
			ASTNode numerator, denominator = null;
			String enzyme = modE.size() > 0 ? modE.get(i) : null;
			if (!reaction.getReversible()) {
				numerator = numeratorElements(enzyme, true);
				denominator = ASTNode.times(denominatorElements(enzyme, true));
			} else {
				numerator = numeratorElements(enzyme, true).minus(
						numeratorElements(enzyme, false));
				denominator = ASTNode
						.times(denominatorElements(enzyme, true))
						.plus(ASTNode.times(denominatorElements(enzyme, false)));
				if (reaction.getNumProducts() > 1
						&& reaction.getNumReactants() > 1)
					denominator.minus(1);
			}
			if (denominator != null)
				numerator.divideBy(denominator);
			if (type) {
				Parameter p_klV = parameterVelocityConstant(reaction.getId(),
						enzyme);
				enzymes[i] = ASTNode.times(new ASTNode(p_klV, this), numerator);
			} else
				enzymes[i] = numerator;
			if (enzyme != null)
				enzymes[i] = ASTNode.times(speciesTerm(enzyme),
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
	 * @param forward
	 * @return true means forward, false means reverse.
	 */
	private ASTNode numeratorElements(String enzyme, boolean forward) {
		Reaction reaction = getParentSBMLObject();

		boolean fullRank = false;
		if (getTypeParameters().length > 1)
			fullRank = ((Boolean) getTypeParameters()[1]).booleanValue();

		ASTNode[] reactants = new ASTNode[reaction.getNumReactants()];
		ASTNode[] products = new ASTNode[reaction.getNumProducts()];
		ASTNode[] reactantsroot = new ASTNode[reaction.getNumReactants()];
		ASTNode[] productroot = new ASTNode[reaction.getNumProducts()];
		ASTNode equation;

		if (!fullRank) {
			for (int i = 0; i < reaction.getNumReactants(); i++) {
				SpeciesReference ref = reaction.getReactant(i);
				Parameter p_kM = parameterMichaelisSubstrate(reaction.getId(),
						ref.getSpecies(), enzyme);
				Parameter p_kiG = parameterKG(ref.getSpecies());
				reactants[i] = ASTNode.pow(ASTNode.frac(speciesTerm(ref),
						new ASTNode(p_kM, this)), new ASTNode(ref
						.getStoichiometry(), this));
				reactantsroot[i] = ASTNode.pow(ASTNode.times(new ASTNode(p_kiG,
						this), new ASTNode(p_kM, this)), new ASTNode(ref
						.getStoichiometry(), this));
			}

			for (int i = 0; i < reaction.getNumProducts(); i++) {
				SpeciesReference ref = reaction.getProduct(i);
				Parameter p_kM = parameterMichaelisProduct(reaction.getId(),
						ref.getSpecies(), enzyme);
				Parameter p_kiG = parameterKG(ref.getSpecies());
				products[i] = ASTNode.pow(ASTNode.frac(speciesTerm(ref),
						new ASTNode(p_kM, this)), new ASTNode(ref
						.getStoichiometry(), this));
				productroot[i] = ASTNode.pow(ASTNode.times(new ASTNode(p_kiG,
						this), new ASTNode(p_kM, this)), new ASTNode(ref
						.getStoichiometry(), this));
			}

			if (forward)
				equation = ASTNode.times(ASTNode.times(reactants), ASTNode
						.sqrt(ASTNode.frac(reactantsroot.length > 0 ? ASTNode
								.times(reactantsroot) : new ASTNode(1, this),
								productroot.length > 0 ? ASTNode
										.times(productroot) : new ASTNode(1,
										this))));
			else
				equation = ASTNode.times(ASTNode.times(products), ASTNode
						.sqrt(ASTNode.frac(productroot.length > 0 ? ASTNode
								.times(productroot) : new ASTNode(1, this),
								reactantsroot.length > 0 ? ASTNode
										.times(reactantsroot) : new ASTNode(1,
										this))));
		} else {
			Parameter kcat = parameterKcatOrVmax(reaction.getId(), enzyme,
					forward);
			ListOf<SpeciesReference> listOf = forward ? reaction
					.getListOfReactants() : reaction.getListOfProducts();
			equation = new ASTNode(kcat, this);
			for (SpeciesReference specRef : listOf) {
				ASTNode curr = speciesTerm(specRef);
				if (specRef.getStoichiometry() != 1d)
					curr.raiseByThePowerOf(specRef.getStoichiometry());
				equation.multiplyWith(curr);
			}
		}
		return equation;
	}

	/**
	 * 
	 * @param species
	 * @return
	 */
	private Parameter parameterKG(String species) {
		Parameter kG = createOrGetGlobalParameter("kG_", species);
		kG.setUnits(Unit.Kind.DIMENSIONLESS);
		kG.setName(concat("energy constant of species ", species).toString());
		return kG;
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
	 * @param forward
	 *            true means forward, false backward.
	 * @return
	 */
	private ASTNode[] denominatorElements(String enzyme, boolean forward) {
		Reaction reaction = getParentSBMLObject();
		ASTNode[] denoms = new ASTNode[forward ? reaction.getNumReactants()
				: reaction.getNumProducts()];
		boolean noOne = (denoms.length == 1)
				&& (!forward || (forward && reaction.getReversible() && reaction
						.getNumProducts() > 1));
		for (int i = 0; i < denoms.length; i++) {
			SpeciesReference ref = forward ? reaction.getReactant(i) : reaction
					.getProduct(i);
			Parameter p_kM = forward ? parameterMichaelisSubstrate(reaction
					.getId(), ref.getSpecies(), enzyme)
					: parameterMichaelisProduct(reaction.getId(), ref
							.getSpecies(), enzyme);
			if (!p_kM.isSetSBOTerm())
				p_kM.setSBOTerm(forward ? 322 : 323);
			denoms[i] = ASTNode.pow(ASTNode.frac(speciesTerm(ref), new ASTNode(
					p_kM, this)), (int) ref.getStoichiometry());
			for (int j = (int) ref.getStoichiometry() - 1; j >= (noOne ? 1 : 0); j--) {
				denoms[i] = ASTNode.sum(ASTNode.pow(ASTNode.frac(
						speciesTerm(ref), new ASTNode(p_kM, this)), j),
						denoms[i]);
			}
		}
		return denoms;
	}

	// @Override
	public String getSimpleName() {
		return getNotesString();
	}
}
