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
import org.sbml.squeezer.io.StringTools;

/**
 * TODO: comment missing
 * 
 * @since 1.0
 * @version
 * @author <a href="mailto:Nadine.hassis@gmail.com">Nadine Hassis</a>
 * @author <a href="mailto:andreas.draeger@uni-tuebingen.de">Andreas Dr&auml;ger</a>
 * @author <a href="mailto:dwouamba@yahoo.fr">Dieudonn&eacute; Wouamba</a>
 * 
 * @date Aug 1, 2007
 */
public class PingPongMechanism extends GeneralizedMassAction implements
		InterfaceBiBiKinetics, InterfaceReversibleKinetics,
		InterfaceIrreversibleKinetics, InterfaceModulatedKinetics {

	/**
	 * @param parentReaction
	 * @throws RateLawNotApplicableException
	 * @throws IllegalFormatException
	 */
	public PingPongMechanism(Reaction parentReaction, Object... typeParameters)
			throws RateLawNotApplicableException, IllegalFormatException {
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

		// according to Cornish-Bowden: Fundamentals of Enzyme kinetics
		StringBuilder notes = new StringBuilder(
				"substituted-enzyme mechanism (Ping-Pong)");
		notes.insert(0, "reversible ");
		if (!reaction.getReversible())
			notes.insert(0, "ir");
		setNotes(notes.toString());

		setSBOTerm(436);

		ASTNode numerator;// I
		ASTNode denominator; // II
		ASTNode catalysts[] = new ASTNode[Math.max(1, modE.size())];
		SpeciesReference specRefE1 = reaction.getReactant(0);
		SpeciesReference specRefP1 = reaction.getProduct(0);
		SpeciesReference specRefE2 = null, specRefP2 = null;

		if (reaction.getNumReactants() == 2)
			specRefE2 = reaction.getReactant(1);
		else if (specRefE1.getStoichiometry() == 2d)
			specRefE2 = specRefE1;
		else
			throw new RateLawNotApplicableException(
					"Number of reactants must equal two to apply ping-pong "
							+ "Michaelis-Menten kinetics to reaction "
							+ reaction.getId());

		boolean exception = false;
		switch (reaction.getNumProducts()) {
		case 1:
			if (specRefP1.getStoichiometry() == 2d)
				specRefP2 = specRefP1;
			else
				exception = true;
			break;
		case 2:
			specRefP2 = reaction.getProduct(1);
			break;
		default:
			exception = true;
			break;
		}
		if (exception)
			throw new RateLawNotApplicableException(
					"Number of products must equal two to apply ping-pong"
							+ "Michaelis-Menten kinetics to reaction "
							+ reaction.getId());

		int enzymeNum = 0;
		do {
			StringBuffer kMr1 = StringTools.concat("kM_", reaction.getId());
			StringBuffer kMr2 = StringTools.concat("kM_", reaction.getId());

			String enzyme = modE.size() == 0 ? null : modE.get(enzymeNum);
			if (modE.size() > 1) {
				StringTools.append(kMr1, underscore, enzyme);
				StringTools.append(kMr2, underscore, enzyme);
			}
			StringTools.append(kMr2, underscore, specRefE2.getSpecies());
			StringTools.append(kMr1, underscore, specRefE1.getSpecies());
			if (specRefE2.equals(specRefE1)) {
				kMr1 = StringTools.concat("kMr1", kMr1.substring(2));
				kMr2 = StringTools.concat("kMr2", kMr2.substring(2));
			}
			Parameter p_kcatp = parameterKcatOrVmax(reaction.getId(), enzyme,
					true);
			Parameter p_kMr1 = createOrGetParameter(kMr1.toString());
			p_kMr1.setSBOTerm(322);
			Parameter p_kMr2 = createOrGetParameter(kMr2.toString());
			p_kMr2.setSBOTerm(322);
			setSBOTerm(436);

			/*
			 * Irreversible Reaction
			 */
			if (!reaction.getReversible()) {
				numerator = new ASTNode(p_kcatp, this);

				if (modE.size() > 0)
					numerator.multiplyWith(speciesTerm(enzyme));
				numerator = ASTNode.times(numerator, speciesTerm(specRefE1));

				denominator = ASTNode.sum(ASTNode.times(new ASTNode(p_kMr2,
						this), speciesTerm(specRefE1)), ASTNode.times(
						new ASTNode(p_kMr1, this), speciesTerm(specRefE2)));

				if (specRefE2.equals(specRefE1)) {
					numerator = ASTNode.pow(numerator, 2);
					denominator = ASTNode.pow(ASTNode.sum(denominator,
							speciesTerm(specRefE1)), 2);
				} else {
					numerator.multiplyWith(speciesTerm(specRefE2));
					denominator.plus(ASTNode.times(speciesTerm(specRefE1),
							speciesTerm(specRefE2)));
				}

				/*
				 * Reversible Reaction
				 */
			} else {
				StringBuffer kMp1 = StringTools.concat("kM_", reaction.getId());
				StringBuffer kMp2 = StringTools.concat("kM_", reaction.getId());
				StringBuffer kIp1 = StringTools.concat("ki_", reaction.getId());
				StringBuffer kIp2 = StringTools.concat("ki_", reaction.getId());
				StringBuffer kIr1 = StringTools.concat("ki_", reaction.getId());

				if (modE.size() > 1) {
					kMp1 = StringTools.concat(kMp1, underscore, enzyme);
					kMp2 = StringTools.concat(kMp2, underscore, enzyme);
					kIp1 = StringTools.concat(kIp1, underscore, enzyme);
					kIp2 = StringTools.concat(kIp2, underscore, enzyme);
					kIr1 = StringTools.concat(kIr1, underscore, enzyme);
				}
				kMp1 = StringTools.concat(kMp1, underscore, specRefP1.getSpecies());
				kMp2 = StringTools.concat(kMp2, underscore, specRefP2.getSpecies());
				kIp1 = StringTools.concat(kIp1, underscore, specRefP1.getSpecies());
				kIp2 = StringTools.concat(kIp2, underscore, specRefP2.getSpecies());
				kIr1 = StringTools.concat(kIr1, underscore, specRefE1.getSpecies());
				if (specRefP2.equals(specRefP1)) {
					kMp1 = StringTools.concat("kMp1", kMp1.substring(2));
					kMp2 = StringTools.concat("kMp2", kMp2.substring(2));
					kIp1 = StringTools.concat("kip1", kIp1.substring(2));
					kIp2 = StringTools.concat("kip2", kIp2.substring(2));
				}
				Parameter p_kcatn = parameterKcatOrVmax(reaction.getId(),
						enzyme, false);
				Parameter p_kMp1 = createOrGetParameter(kMp1.toString());
				p_kMp1.setSBOTerm(322);
				Parameter p_kMp2 = createOrGetParameter(kMp2.toString());
				p_kMp2.setSBOTerm(322);
				Parameter p_kIp1 = createOrGetParameter(kIp1.toString());
				p_kIp1.setSBOTerm(261);
				Parameter p_kIp2 = createOrGetParameter(kIp2.toString());
				p_kIp2.setSBOTerm(261);
				Parameter p_kIr1 = createOrGetParameter(kIr1.toString());
				p_kIr1.setSBOTerm(261);

				ASTNode numeratorForward = ASTNode.frac(new ASTNode(p_kcatp,
						this), ASTNode.times(this, p_kIr1, p_kMr2));
				ASTNode numeratorReverse = ASTNode.frac(new ASTNode(p_kcatn,
						this), ASTNode.times(this, p_kIp1, p_kMp2));

				if (modE.size() > 0)
					numeratorForward.multiplyWith(speciesTerm(enzyme));
				denominator = ASTNode.sum(ASTNode.frac(speciesTerm(specRefE1),
						new ASTNode(p_kIr1, this)), ASTNode.frac(ASTNode.times(
						new ASTNode(p_kMr1, this), speciesTerm(specRefE2)),
						ASTNode.times(this, p_kIr1, p_kMr2)), ASTNode.frac(
						speciesTerm(specRefP1), new ASTNode(p_kIp1, this)),
						ASTNode.frac(ASTNode.times(new ASTNode(p_kMp1, this),
								speciesTerm(specRefP2)), ASTNode.times(this,
								p_kIp1, p_kMp2)));
				if (specRefE2.equals(specRefE1)) {
					numeratorForward = ASTNode.times(numeratorForward, ASTNode
							.pow(speciesTerm(specRefE1), 2));
					denominator = ASTNode.sum(denominator, ASTNode.frac(ASTNode
							.pow(speciesTerm(specRefE1), 2), ASTNode.times(
							this, p_kIr1, p_kMr2)));
				} else {
					numeratorForward = ASTNode.times(numeratorForward,
							speciesTerm(specRefE1), speciesTerm(specRefE2));
					denominator = ASTNode.sum(denominator, ASTNode.frac(ASTNode
							.times(speciesTerm(specRefE1),
									speciesTerm(specRefE2)), ASTNode.times(
							this, p_kIr1, p_kMr2)));
				}

				denominator = ASTNode.sum(denominator, ASTNode.frac(ASTNode
						.times(speciesTerm(specRefE1), speciesTerm(specRefP1)),
						ASTNode.times(this, p_kIr1, p_kIp1)), ASTNode
						.frac(
								ASTNode.times(new ASTNode(p_kMr1, this),
										speciesTerm(specRefE2),
										speciesTerm(specRefP2)), ASTNode.times(
										this, p_kIr1, p_kMr2, p_kIp2)));

				if (modE.size() > 0)
					numeratorReverse.multiplyWith(speciesTerm(enzyme));

				ASTNode denominator_p1p2 = speciesTerm(specRefE1);
				if (specRefP2.equals(specRefP1)) {
					numeratorReverse = ASTNode.times(numeratorReverse, ASTNode
							.pow(speciesTerm(specRefP1), 2));
					denominator_p1p2 = ASTNode.pow(speciesTerm(specRefP1), 2);

				} else {
					numeratorReverse.multiplyWith(speciesTerm(specRefP1),
							speciesTerm(specRefP2));

					denominator_p1p2 = ASTNode.times(speciesTerm(specRefP1),
							speciesTerm(specRefP2));
				}
				numerator = ASTNode.diff(numeratorForward, numeratorReverse);
				denominator_p1p2 = ASTNode.frac(denominator_p1p2, ASTNode
						.times(this, p_kIp1, p_kMp2));
				denominator = ASTNode.sum(denominator, denominator_p1p2);
			}
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
		return "Ping-Pong mechanism";
	}
}
