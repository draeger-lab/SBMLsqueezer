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
 * TODO: comment missing
 * 
 * @since 1.0
 * @version
 * @author <a href="mailto:Nadine.hassis@gmail.com">Nadine Hassis</a>
 * @author <a href="mailto:andreas.draeger@uni-tuebingen.de">Andreas
 *         Dr&auml;ger</a>
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
	public PingPongMechanism(Reaction parentReaction)
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
	 * org.sbml.squeezer.kinetics.GeneralizedMassAction#createKineticEquation
	 * (java.util.List, java.util.List, java.util.List, java.util.List,
	 * java.util.List, java.util.List)
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
			StringBuffer kcatp;
			StringBuffer kMr1 = concat("kM_", reaction.getId());
			StringBuffer kMr2 = concat("kM_", reaction.getId());
			StringBuffer enzyme = new StringBuffer(modE.size() == 0 ? "" : modE
					.get(enzymeNum));

			if (modE.size() == 0)
				kcatp = concat("Vp_", reaction.getId());
			else {
				kcatp = concat("kcatp_", reaction.getId());
				if (modE.size() > 1) {
					append(kcatp, underscore, enzyme);
					append(kMr1, underscore, enzyme);
					append(kMr2, underscore, enzyme);
				}
			}
			append(kMr2, underscore, specRefE2.getSpecies());
			append(kMr1, underscore, specRefE1.getSpecies());
			if (specRefE2.equals(specRefE1)) {
				kMr1 = concat("kMr1", kMr1.substring(2));
				kMr2 = concat("kMr2", kMr2.substring(2));
			}
			Parameter p_kcatp = createOrGetParameter(kcatp.toString());
			p_kcatp.setSBOTerm(modE.size() == 0 ? 324 : 320);
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
					numerator = ASTNode.times(numerator, new ASTNode(modE
							.get(enzymeNum), this));
				numerator = ASTNode.times(numerator, new ASTNode(specRefE1
						.getSpeciesInstance(), this));

				denominator = ASTNode.sum(ASTNode.times(this, p_kMr2, specRefE1
						.getSpeciesInstance()), ASTNode.times(this, p_kMr1,
						specRefE2.getSpeciesInstance()));

				if (specRefE2.equals(specRefE1)) {
					numerator = ASTNode.pow(numerator, 2);
					denominator = ASTNode.pow(ASTNode.sum(denominator,
							new ASTNode(specRefE1.getSpeciesInstance(), this)),
							2);
				} else {
					numerator = ASTNode.times(numerator, new ASTNode(specRefE2
							.getSpeciesInstance(), this));
					denominator = ASTNode.sum(denominator, ASTNode.times(this,
							specRefE1.getSpeciesInstance(), specRefE2
									.getSpeciesInstance()));
				}

				/*
				 * Reversible Reaction
				 */
			} else {
				StringBuffer kcatn;
				StringBuffer kMp1 = concat("kM_", reaction.getId());
				StringBuffer kMp2 = concat("kM_", reaction.getId());
				StringBuffer kIp1 = concat("ki_", reaction.getId());
				StringBuffer kIp2 = concat("ki_", reaction.getId());
				StringBuffer kIr1 = concat("ki_", reaction.getId());

				if (modE.size() == 0)
					kcatn = concat("Vn_", reaction.getId());
				else {
					kcatn = concat("kcatn_", reaction.getId());
					if (modE.size() > 1) {
						StringBuffer modEnzymeNumber = new StringBuffer(modE
								.get(enzymeNum));
						kcatn = concat(kcatn, underscore, modEnzymeNumber);
						kMp1 = concat(kMp1, underscore, modEnzymeNumber);
						kMp2 = concat(kMp2, underscore, modEnzymeNumber);
						kIp1 = concat(kIp1, underscore, modEnzymeNumber);
						kIp2 = concat(kIp2, underscore, modEnzymeNumber);
						kIr1 = concat(kIr1, underscore, modEnzymeNumber);
					}
				}
				kMp1 = concat(kMp1, underscore, specRefP1.getSpecies());
				kMp2 = concat(kMp2, underscore, specRefP2.getSpecies());
				kIp1 = concat(kIp1, underscore, specRefP1.getSpecies());
				kIp2 = concat(kIp2, underscore, specRefP2.getSpecies());
				kIr1 = concat(kIr1, underscore, specRefE1.getSpecies());
				if (specRefP2.equals(specRefP1)) {
					kMp1 = concat("kMp1", kMp1.substring(2));
					kMp2 = concat("kMp2", kMp2.substring(2));
					kIp1 = concat("kip1", kIp1.substring(2));
					kIp2 = concat("kip2", kIp2.substring(2));
				}
				Parameter p_kcatn = createOrGetParameter(kcatn.toString());
				p_kcatn.setSBOTerm(modE.size() == 0 ? 325 : 321);
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
					numeratorForward = ASTNode.times(numeratorForward,
							new ASTNode(modE.get(enzymeNum), this));
				denominator = ASTNode.sum(ASTNode.frac(this, specRefE1
						.getSpeciesInstance(), p_kIr1), ASTNode.frac(ASTNode
						.times(this, p_kMr1, specRefE2.getSpeciesInstance()),
						ASTNode.times(this, p_kIr1, p_kMr2)), ASTNode.frac(
						this, specRefP1.getSpeciesInstance(), p_kIp1), ASTNode
						.frac(ASTNode.times(this, p_kMp1, specRefP2
								.getSpeciesInstance()), ASTNode.times(this,
								p_kIp1, p_kMp2)));
				if (specRefE2.equals(specRefE1)) {
					numeratorForward = ASTNode.times(numeratorForward, ASTNode
							.pow(new ASTNode(specRefE1.getSpeciesInstance(),
									this), 2));
					denominator = ASTNode.sum(denominator, ASTNode.frac(ASTNode
							.pow(new ASTNode(specRefE1.getSpeciesInstance(),
									this), 2), ASTNode.times(this, p_kIr1,
							p_kMr2)));
				} else {
					numeratorForward = ASTNode.times(numeratorForward,
							new ASTNode(specRefE1.getSpeciesInstance(), this),
							new ASTNode(specRefE2.getSpeciesInstance(), this));
					denominator = ASTNode.sum(denominator, ASTNode.frac(ASTNode
							.times(this, specRefE1.getSpeciesInstance(),
									specRefE2.getSpeciesInstance()), ASTNode
							.times(this, p_kIr1, p_kMr2)));
				}

				denominator = ASTNode.sum(denominator, ASTNode.frac(ASTNode
						.times(this, specRefE1.getSpeciesInstance(), specRefP1
								.getSpeciesInstance()), ASTNode.times(this,
						p_kIr1, p_kIp1)), ASTNode.frac(ASTNode.times(this,
						p_kMr1, specRefE2.getSpeciesInstance(), specRefP2
								.getSpeciesInstance()), ASTNode.times(this,
						p_kIr1, p_kMr2, p_kIp2)));

				if (modE.size() > 0)
					numeratorReverse = ASTNode.times(numeratorReverse,
							new ASTNode(modE.get(enzymeNum), this));

				ASTNode denominator_p1p2 = new ASTNode(specRefE1
						.getSpeciesInstance(), this);
				if (specRefP2.equals(specRefP1)) {
					numeratorReverse = ASTNode.times(numeratorReverse, ASTNode
							.pow(new ASTNode(specRefP1.getSpeciesInstance(),
									this), 2));
					denominator_p1p2 = ASTNode.pow(new ASTNode(specRefP1
							.getSpeciesInstance(), this), 2);

				} else {
					numeratorReverse = ASTNode.times(numeratorReverse,
							new ASTNode(specRefP1.getSpeciesInstance(), this),
							new ASTNode(specRefP2.getSpeciesInstance(), this));

					denominator_p1p2 = ASTNode.times(this, specRefP1
							.getSpeciesInstance(), specRefP2
							.getSpeciesInstance());
				}
				numerator = ASTNode.diff(numeratorForward, numeratorReverse);
				denominator_p1p2 = ASTNode.frac(denominator_p1p2, ASTNode
						.times(this, p_kIp1, p_kMp2));
				denominator = ASTNode.sum(denominator, denominator_p1p2);
			}
			catalysts[enzymeNum++] = ASTNode.frac(numerator, denominator);
		} while (enzymeNum <= modE.size() - 1);
		return ASTNode.times(activationFactor(modActi),
				inhibitionFactor(modInhib), ASTNode.sum(catalysts));
	}
}
