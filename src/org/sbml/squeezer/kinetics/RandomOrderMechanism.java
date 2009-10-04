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
 * @author <a href="mailto:andreas.draeger@uni-tuebingen.de">Andreas Dr&auml;ger</a>
 * 
 * @date Aug 1, 2007
 */
public class RandomOrderMechanism extends GeneralizedMassAction implements
		InterfaceBiUniKinetics, InterfaceBiBiKinetics,
		InterfaceReversibleKinetics, InterfaceIrreversibleKinetics,
		InterfaceModulatedKinetics {

	/**
	 * @param parentReaction
	 * @param model
	 * @throws RateLawNotApplicableException
	 * @throws IllegalFormatException
	 */
	public RandomOrderMechanism(Reaction parentReaction,
			Object... typeParameters) throws RateLawNotApplicableException,
			IllegalFormatException {
		super(parentReaction, typeParameters);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.squeezer.kinetics.GeneralizedMassAction#createKineticEquation
	 *      (java.util.List, java.util.List, java.util.List, java.util.List,
	 *      java.util.List, java.util.List)
	 */
	// @Override
	ASTNode createKineticEquation(List<String> modE, List<String> modActi,
			List<String> modTActi, List<String> modInhib,
			List<String> modTInhib, List<String> modCat)
			throws RateLawNotApplicableException, IllegalFormatException {

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
			if (specRefP1.getStoichiometry() == 1.0)
				biuni = true;
			else if (specRefP1.getStoichiometry() == 2.0)
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
		if (exception)
			throw new RateLawNotApplicableException(
					"Number of products must equal either one or two to apply random order "
							+ "Michaelis-Menten kinetics to reaction "
							+ reaction.getId());
		/*
		 * If modE is empty there was no enzyme sined to the reaction. Thus we
		 * do not want anything in modE to occur in the kinetic equation.
		 */
		int enzymeNum = 0;
		ASTNode numerator;// I
		ASTNode denominator; // II
		ASTNode catalysts[] = new ASTNode[Math.max(1, modE.size())];
		do {
			String enzyme = modE.size() == 0 ? null : modE.get(enzymeNum);
			/*
			 * Irreversible reaction
			 */
			if (!reaction.getReversible()) {
				StringBuffer kMr1 = concat("kM_", reaction.getId());
				StringBuffer kMr2 = concat("kM_", reaction.getId());
				StringBuffer kIr1 = concat("ki_", reaction.getId());

				if (modE.size() > 1) {
					append(kMr2, underscore, enzyme);
					append(kMr1, underscore, enzyme);
					append(kIr1, underscore, enzyme);
				}
				Species speciesR1 = specRefR1.getSpeciesInstance();
				Species speciesR2 = specRefR2.getSpeciesInstance();
				append(kMr1, underscore, speciesR1);
				append(kMr2, underscore, speciesR2);
				if (specRefR1.equals(specRefR2)) {
					append(kMr1, "kMr1", kMr1.substring(2));
					append(kMr2, "kMr2", kMr2.substring(2));
				}
				append(kIr1, underscore, speciesR1);
				Parameter p_kcatp = parameterKcatOrVmax(reaction.getId(),
						enzyme, true);
				Parameter p_kMr1 = createOrGetParameter(kMr1.toString());
				p_kMr1.setSBOTerm(322);
				Parameter p_kMr2 = createOrGetParameter(kMr2.toString());
				p_kMr2.setSBOTerm(322);
				Parameter p_kIr1 = createOrGetParameter(kIr1.toString());
				p_kIr1.setSBOTerm(261);

				numerator = new ASTNode(p_kcatp, this);
				if (modE.size() > 0)
					numerator.multiplyWith(speciesTerm(enzyme));
				if (specRefR2.equals(specRefR1)) {
					ASTNode r1square = ASTNode.pow(speciesTerm(speciesR1), 2);
					numerator = ASTNode.times(numerator, r1square);
					denominator = ASTNode.sum(ASTNode.times(this, p_kIr1,
							p_kMr2), ASTNode.times(ASTNode.sum(this, p_kMr1,
							p_kMr2), speciesTerm(speciesR1)), r1square);
				} else {
					numerator = ASTNode.times(numerator,
							speciesTerm(speciesR1), speciesTerm(speciesR2));
					denominator = ASTNode.sum(ASTNode.times(this, p_kIr1,
							p_kMr2), ASTNode.times(new ASTNode(p_kMr2, this),
							speciesTerm(speciesR1)), ASTNode.times(new ASTNode(
							p_kMr1, this), speciesTerm(speciesR2)), ASTNode
							.times(speciesTerm(speciesR1),
									speciesTerm(speciesR2)));
				}
			} else {
				/*
				 * Reversible reaction: Bi-Bi
				 */
				if (!biuni) {

					StringBuffer kMr2 = concat("kM_", reaction.getId());
					StringBuffer kIr1 = concat("ki_", reaction.getId());
					StringBuffer kMp1 = concat("kM_", reaction.getId());
					StringBuffer kIp1 = concat("ki_", reaction.getId());
					StringBuffer kIp2 = concat("ki_", reaction.getId());
					StringBuffer kIr2 = concat("ki_", reaction.getId());

					if (modE.size() > 1) {
						kMr2 = concat(kMr2, underscore, enzyme);
						kMp1 = concat(kMp1, underscore, enzyme);
						kIp1 = concat(kIp1, underscore, enzyme);
						kIp2 = concat(kIp2, underscore, enzyme);
						kIr2 = concat(kIr2, underscore, enzyme);
						kIr1 = concat(kIr1, underscore, enzyme);
					}
					Species speciesR1 = specRefR1.getSpeciesInstance();
					Species speciesR2 = specRefR2.getSpeciesInstance();
					Species speciesP1 = specRefP1.getSpeciesInstance();
					Species speciesP2 = specRefP2.getSpeciesInstance();
					kMr2 = concat(kMr2, underscore, speciesR2);
					kIr1 = concat(kIr1, underscore, speciesR1);
					kIr2 = concat(kIr2, underscore, speciesR2);
					kIp1 = concat(kIp1, underscore, speciesP1);
					kIp2 = concat(kIp2, underscore, speciesP2);
					kMp1 = concat(kMp1, underscore, speciesP1);
					if (specRefR2.equals(specRefR1)) {
						kIr1 = concat("kir1", kIr1.substring(2));
						kIr2 = concat("kir2", kIr2.substring(2));
					}
					if (specRefP2.equals(specRefP1)) {
						kIp1 = concat("kip1", kIp1.substring(2));
						kIp2 = concat("kip2", kIp2.substring(2));
					}
					Parameter p_kcatp = parameterKcatOrVmax(reaction.getId(),
							enzyme, true);
					Parameter p_kcatn = parameterKcatOrVmax(reaction.getId(),
							enzyme, false);
					Parameter p_kMr2 = createOrGetParameter(kMr2.toString());
					p_kMr2.setSBOTerm(322);
					Parameter p_kMp1 = createOrGetParameter(kMp1.toString());
					p_kMp1.setSBOTerm(323);
					Parameter p_kIp1 = createOrGetParameter(kIp1.toString());
					p_kIp1.setSBOTerm(261);
					Parameter p_kIp2 = createOrGetParameter(kIp2.toString());
					p_kIp2.setSBOTerm(261);
					Parameter p_kIr1 = createOrGetParameter(kIr1.toString());
					p_kIr1.setSBOTerm(261);
					Parameter p_kIr2 = createOrGetParameter(kIr2.toString());
					p_kIr2.setSBOTerm(261);

					ASTNode numeratorForward = ASTNode
							.frac(new ASTNode(p_kcatp, this), ASTNode.times(
									this, p_kIr1, p_kMr2));
					ASTNode numeratorReverse = ASTNode
							.frac(new ASTNode(p_kcatn, this), ASTNode.times(
									this, p_kIp2, p_kMp1));
					if (modE.size() > 0) {
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
					numeratorForward = ASTNode.times(numeratorForward, r1r2);
					numeratorReverse = ASTNode.times(numeratorReverse, p1p2);
					numerator = ASTNode
							.diff(numeratorForward, numeratorReverse);
					denominator = ASTNode.sum(new ASTNode(1, this), ASTNode
							.frac(speciesTerm(speciesR1), new ASTNode(p_kIr1,
									this)), ASTNode.frac(
							speciesTerm(speciesR2), new ASTNode(p_kIr2, this)),
							ASTNode.frac(speciesTerm(speciesP1), new ASTNode(
									p_kIp1, this)), ASTNode.frac(
									speciesTerm(speciesP2), new ASTNode(p_kIp2,
											this)), ASTNode.frac(p1p2, ASTNode
									.times(this, p_kIp2, p_kMp1)), ASTNode
									.frac(r1r2, ASTNode.times(this, p_kIr1,
											p_kMr2)));
				} else {
					/*
					 * Reversible reaction: Bi-Uni reaction
					 */
					StringBuffer kMr2 = concat("kM_", reaction.getId());
					StringBuffer kMp1 = concat("kM_", reaction.getId());
					StringBuffer kIr2 = concat("ki_", reaction.getId());
					StringBuffer kIr1 = concat("ki_", reaction.getId());

					if (modE.size() > 1) {
						append(kMr2, underscore, enzyme);
						append(kMp1, underscore, enzyme);
						append(kIr2, underscore, enzyme);
						append(kIr1, underscore, enzyme);
					}

					Species speciesR1 = specRefR1.getSpeciesInstance();
					Species speciesR2 = specRefR2.getSpeciesInstance();
					Species speciesP1 = specRefP1.getSpeciesInstance();
					append(kMr2, underscore, speciesR2);
					append(kIr1, underscore, speciesR2);
					append(kIr2, underscore, speciesR2);
					append(kMp1, underscore, speciesR2);

					if (specRefR2.equals(specRefR1)) {
						append(kIr1, "kip1", kIr1.substring(2));
						append(kIr2, "kip2", kIr2.substring(2));
					}
					Parameter p_kcatp = parameterKcatOrVmax(reaction.getId(),
							enzyme, true);
					Parameter p_kcatn = parameterKcatOrVmax(reaction.getId(),
							enzyme, false);
					Parameter p_kMr2 = createOrGetParameter(kMr2.toString());
					p_kMr2.setSBOTerm(322);
					Parameter p_kMp1 = createOrGetParameter(kMp1.toString());
					p_kMp1.setSBOTerm(323);
					Parameter p_kIr1 = createOrGetParameter(kIr1.toString());
					p_kIr1.setSBOTerm(261);
					Parameter p_kIr2 = createOrGetParameter(kIr2.toString());
					p_kIr2.setSBOTerm(261);

					ASTNode r1r2;
					if (specRefR1.equals(specRefR2))
						r1r2 = ASTNode.pow(speciesTerm(speciesR1), 2);
					else
						r1r2 = ASTNode.times(speciesTerm(speciesR1),
								speciesTerm(speciesR2));
					ASTNode numeratorForward = ASTNode
							.frac(new ASTNode(p_kcatp, this), ASTNode.times(
									this, p_kIr1, p_kMr2));
					ASTNode numeratorReverse = ASTNode.frac(this, p_kcatn,
							p_kMp1);
					if (modE.size() != 0) {
						numeratorForward.multiplyWith(speciesTerm(enzyme));
						numeratorReverse.multiplyWith(speciesTerm(enzyme));
					}
					numeratorForward.multiplyWith(r1r2);
					numeratorReverse.multiplyWith(speciesTerm(speciesP1));
					numerator = numeratorForward.minus(numeratorReverse);
					denominator = ASTNode.sum(new ASTNode(1, this), ASTNode
							.frac(speciesTerm(speciesR1), new ASTNode(p_kIr1,
									this)), ASTNode.frac(
							speciesTerm(speciesR2), new ASTNode(p_kIr2, this)),
							ASTNode.frac(r1r2, ASTNode.times(this, p_kIr1,
									p_kMr2)), ASTNode.frac(
									speciesTerm(speciesP1), new ASTNode(p_kMp1,
											this)));
				}
			}
			// Construct formula
			catalysts[enzymeNum++] = ASTNode.frac(numerator, denominator);
		} while (enzymeNum < modE.size());
		return ASTNode.times(activationFactor(modActi),
				inhibitionFactor(modInhib), ASTNode.sum(catalysts));
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
