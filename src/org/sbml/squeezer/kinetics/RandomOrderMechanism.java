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
import org.sbml.jsbml.Parameter;
import org.sbml.jsbml.Reaction;
import org.sbml.jsbml.Species;
import org.sbml.jsbml.SpeciesReference;
import org.sbml.squeezer.RateLawNotApplicableException;

/**
 * This class creates a kinetic equation according to the random order mechanism
 * (see Cornish-Bowden: Fundamentals of Enzyme Kinetics, p. 169).
 * 
 * @since 1.0
 * @version
 * @author <a href="mailto:Nadine.hassis@gmail.com">Nadine Hassis</a>
 * @author <a href="mailto:andreas.draeger@uni-tuebingen.de">Andreas
 *         Dr&auml;ger</a>
 * 
 * @date Aug 1, 2007
 */
public class RandomOrderMechanism extends GeneralizedMassAction implements
		InterfaceBiUniKinetics, InterfaceBiBiKinetics,
		InterfaceReversibleKinetics, InterfaceIrreversibleKinetics,
		InterfaceModulatedKinetics {

	/**
	 * 
	 * @param parentReaction
	 * @param typeParameters
	 * @throws RateLawNotApplicableException
	 */
	public RandomOrderMechanism(Reaction parentReaction,
			Object... typeParameters) throws RateLawNotApplicableException {
		super(parentReaction, typeParameters);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.sbml.squeezer.kinetics.GeneralizedMassAction#createKineticEquation
	 * (java.util.List, java.util.List, java.util.List, java.util.List,
	 * java.util.List, java.util.List)
	 */
	// @Override
	ASTNode createKineticEquation(List<String> modE, List<String> modActi,
			List<String> modTActi, List<String> modInhib,
			List<String> modTInhib, List<String> modCat)
			throws RateLawNotApplicableException {

		Reaction reaction = getParentSBMLObject();
		SpeciesReference specRefR1 = reaction.getReactant(0), specRefR2;
		SpeciesReference specRefP1 = reaction.getProduct(0), specRefP2 = null;

		if (reaction.getNumReactants() == 2)
			specRefR2 = (SpeciesReference) reaction.getReactant(1);
		else if (specRefR1.getStoichiometry() == 2f)
			specRefR2 = specRefR1;
		else
			throw new RateLawNotApplicableException(
					"Number of reactants must equal two to apply random order "
							+ "Michaelis-Menten kinetics to reaction "
							+ reaction.getId());

		setSBOTerm(429);
		double stoichiometryRight = 0;
		for (int i = 0; i < reaction.getNumProducts(); i++)
			stoichiometryRight += reaction.getProduct(i).getStoichiometry();
		// according to Cornish-Bowden: Fundamentals of Enzyme kinetics
		// rapid-equilibrium random order ternary-complex mechanism
		if ((reaction.getNumProducts() == 1) && (stoichiometryRight == 1d)
				&& !reaction.getReversible())
			setSBOTerm(432);

		StringBuilder notes = new StringBuilder("rapid-equilibrium random ");
		notes.append("order ternary-complex mechanism");
		if ((reaction.getNumProducts() == 2) && (stoichiometryRight == 2))
			notes.append(" with two products");
		else if ((reaction.getNumProducts() == 1) && (stoichiometryRight == 1))
			notes.append(" with one product");
		notes.insert(0, "reversible ");
		if (!reaction.getReversible())
			notes.insert(0, "ir");
		setNotes(notes.toString());

		boolean exception = false;
		boolean biuni = false;
		switch (reaction.getNumProducts()) {
		case 1:
			if (specRefP1.getStoichiometry() == 1d)
				biuni = true;
			else if (specRefP1.getStoichiometry() == 2d)
				specRefP2 = specRefP1;
			else
				exception = true;
			break;
		case 2:
			specRefP2 = (SpeciesReference) reaction.getProduct(1);
			break;
		default:
			exception = true;
			break;
		}
		if (exception && reaction.getReversible())
			throw new RateLawNotApplicableException(
					String
							.format(
									"For reversible reactions the number of products must equal either one or two to apply random order Michaelis-Menten kinetics to reaction %s.",
									reaction.getId()));
		/*
		 * If modE is empty there was no enzyme sined to the reaction. Thus we
		 * do not want anything in modE to occur in the kinetic equation.
		 */
		int enzymeNum = 0;
		ASTNode catalysts[] = new ASTNode[Math.max(1, modE.size())];
		do {
			String enzyme = modE.size() == 0 ? null : modE.get(enzymeNum);
			if (!reaction.getReversible())
				catalysts[enzymeNum++] = irreversible(specRefP2, specRefP2,
						enzyme);
			else
				catalysts[enzymeNum++] = biuni ? reversibleBiUni(specRefP2,
						specRefP2, specRefP2, enzyme) : reversibleBiBi(
						specRefR1, specRefR2, specRefP1, specRefP2, enzyme);
		} while (enzymeNum < modE.size());
		return ASTNode.times(activationFactor(modActi),
				inhibitionFactor(modInhib), ASTNode.sum(catalysts));
	}

	/**
	 * Irreversible reaction
	 * 
	 * @param specRefR1
	 * @param specRefR2
	 * @param enzyme
	 */
	private ASTNode irreversible(SpeciesReference specRefR1,
			SpeciesReference specRefR2, String enzyme) {
		Species speciesR1 = specRefR1.getSpeciesInstance();
		Species speciesR2 = specRefR2.getSpeciesInstance();
		Parameter p_kcatp = parameterKcatOrVmax(enzyme, true);
		Parameter p_kMr1 = parameterMichaelis(speciesR1.getId(), enzyme, true);
		Parameter p_kMr2 = parameterMichaelis(speciesR2.getId(), enzyme, true);
		Parameter p_kIr1 = parameterKi(speciesR1.getId(), enzyme);
		ASTNode numerator = new ASTNode(p_kcatp, this);
		ASTNode denominator; // II
		if (enzyme != null)
			numerator.multiplyWith(speciesTerm(enzyme));
		if (specRefR2.equals(specRefR1)) {
			ASTNode r1square = ASTNode.pow(speciesTerm(speciesR1), 2);
			numerator = ASTNode.times(numerator, r1square);
			denominator = ASTNode.sum(ASTNode.times(this, p_kIr1, p_kMr2),
					ASTNode.times(ASTNode.sum(this, p_kMr1, p_kMr2),
							speciesTerm(speciesR1)), r1square);
		} else {
			numerator = ASTNode.times(numerator, speciesTerm(speciesR1),
					speciesTerm(speciesR2));
			denominator = ASTNode.sum(ASTNode.times(this, p_kIr1, p_kMr2),
					ASTNode.times(new ASTNode(p_kMr2, this),
							speciesTerm(speciesR1)), ASTNode.times(new ASTNode(
							p_kMr1, this), speciesTerm(speciesR2)), ASTNode
							.times(speciesTerm(speciesR1),
									speciesTerm(speciesR2)));
		}
		return ASTNode.frac(numerator, denominator);
	}

	/**
	 * Reversible reaction: Bi-Uni reactions
	 * 
	 * @param specRefR1
	 * @param specRefR2
	 * @param specRefP1
	 * @param enzyme
	 */
	private ASTNode reversibleBiUni(SpeciesReference specRefR1,
			SpeciesReference specRefR2, SpeciesReference specRefP1,
			String enzyme) {
		Species speciesR1 = specRefR1.getSpeciesInstance();
		Species speciesR2 = specRefR2.getSpeciesInstance();
		Species speciesP1 = specRefP1.getSpeciesInstance();
		Parameter p_kcatp = parameterKcatOrVmax(enzyme, true);
		Parameter p_kcatn = parameterKcatOrVmax(enzyme, false);
		Parameter p_kMr2 = parameterMichaelis(speciesR2.getId(), enzyme, true);
		Parameter p_kMp1 = parameterMichaelis(speciesP1.getId(), enzyme, false);
		Parameter p_kIr1 = parameterKi(speciesR1.getId(), enzyme);
		Parameter p_kIr2 = parameterKi(speciesR2.getId(), enzyme);

		ASTNode r1r2;
		if (specRefR1.equals(specRefR2))
			r1r2 = ASTNode.pow(speciesTerm(speciesR1), 2);
		else
			r1r2 = ASTNode
					.times(speciesTerm(speciesR1), speciesTerm(speciesR2));
		ASTNode numeratorForward = ASTNode.frac(new ASTNode(p_kcatp, this),
				ASTNode.times(this, p_kIr1, p_kMr2));
		ASTNode numeratorReverse = ASTNode.frac(this, p_kcatn, p_kMp1);
		if (enzyme != null) {
			numeratorForward.multiplyWith(speciesTerm(enzyme));
			numeratorReverse.multiplyWith(speciesTerm(enzyme));
		}
		numeratorForward.multiplyWith(r1r2);
		numeratorReverse.multiplyWith(speciesTerm(speciesP1));
		ASTNode numerator = numeratorForward.minus(numeratorReverse);
		ASTNode denominator = ASTNode
				.sum(
						new ASTNode(1, this),
						ASTNode.frac(speciesTerm(speciesR1), new ASTNode(
								p_kIr1, this)),
						ASTNode.frac(speciesTerm(speciesR2), new ASTNode(
								p_kIr2, this)),
						ASTNode.frac(r1r2, ASTNode.times(this, p_kIr1, p_kMr2)),
						ASTNode.frac(speciesTerm(speciesP1), new ASTNode(
								p_kMp1, this)));
		return ASTNode.frac(numerator, denominator);
	}

	/**
	 * Reversible bi-bi case.
	 * 
	 * @param specRefR1
	 * @param specRefR2
	 * @param specRefP1
	 * @param specRefP2
	 * @param enzyme
	 */
	private ASTNode reversibleBiBi(SpeciesReference specRefR1,
			SpeciesReference specRefR2, SpeciesReference specRefP1,
			SpeciesReference specRefP2, String enzyme) {
		Species speciesR1 = specRefR1.getSpeciesInstance();
		Species speciesR2 = specRefR2.getSpeciesInstance();
		Species speciesP1 = specRefP1.getSpeciesInstance();
		Species speciesP2 = specRefP2.getSpeciesInstance();
		Parameter p_kcatp = parameterKcatOrVmax(enzyme, true);
		Parameter p_kcatn = parameterKcatOrVmax(enzyme, false);
		Parameter p_kMr2 = parameterMichaelis(speciesR2.getId(), enzyme);
		Parameter p_kMp1 = parameterMichaelis(speciesP1.getId(), enzyme);
		Parameter p_kIp1 = parameterKi(speciesP1.getId(), enzyme);
		Parameter p_kIp2 = parameterKi(speciesP2.getId(), enzyme);
		Parameter p_kIr1 = parameterKi(speciesR1.getId(), enzyme);
		Parameter p_kIr2 = parameterKi(speciesR2.getId(), enzyme);

		ASTNode numeratorForward = ASTNode.frac(new ASTNode(p_kcatp, this),
				ASTNode.times(this, p_kIr1, p_kMr2));
		ASTNode numeratorReverse = ASTNode.frac(new ASTNode(p_kcatn, this),
				ASTNode.times(this, p_kIp2, p_kMp1));
		if (enzyme != null) {
			numeratorForward.multiplyWith(speciesTerm(enzyme));
			numeratorReverse.multiplyWith(speciesTerm(enzyme));
		}
		// happens if the reactant has a stoichiometry of two.
		ASTNode r1r2 = specRefR1.equals(specRefR2) ? ASTNode.pow(
				speciesTerm(speciesR1), 2) : ASTNode.times(
				speciesTerm(speciesR1), speciesTerm(speciesR2));
		// happens if the product has a stoichiometry of two.
		ASTNode p1p2 = specRefP1.equals(specRefP2) ? ASTNode.pow(
				speciesTerm(speciesP1), 2) : ASTNode.times(
				speciesTerm(speciesP1), speciesTerm(speciesP2));
		numeratorForward.multiplyWith(r1r2.clone());
		numeratorReverse.multiplyWith(p1p2.clone());
		ASTNode numerator = ASTNode.diff(numeratorForward, numeratorReverse);
		ASTNode denominator = ASTNode
				.sum(new ASTNode(1, this), ASTNode.frac(speciesTerm(speciesR1),
						new ASTNode(p_kIr1, this)), ASTNode.frac(
						speciesTerm(speciesR2), new ASTNode(p_kIr2, this)),
						ASTNode.frac(speciesTerm(speciesP1), new ASTNode(
								p_kIp1, this)), ASTNode.frac(
								speciesTerm(speciesP2), new ASTNode(p_kIp2,
										this)), ASTNode.frac(p1p2, ASTNode
								.times(this, p_kIp2, p_kMp1)), ASTNode.frac(
								r1r2, ASTNode.times(this, p_kIr1, p_kMr2)));
		return ASTNode.frac(numerator, denominator);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.squeezer.kinetics.GeneralizedMassAction#getSimpleName()
	 */
	public String getSimpleName() {
		return "Random order mechanism";
	}
}
