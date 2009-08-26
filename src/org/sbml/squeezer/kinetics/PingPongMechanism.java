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
public class PingPongMechanism extends GeneralizedMassAction {

	/**
	 * @param parentReaction
	 * @param model
	 * @param reversibility
	 * @throws RateLawNotApplicableException
	 * @throws IOException
	 * @throws IllegalFormatException
	 */
	public PingPongMechanism(Reaction parentReaction, Model model,
			boolean reversibility) throws RateLawNotApplicableException,
			IOException, IllegalFormatException {
		super(parentReaction, model);
	}

	/**
	 * @param parentReaction
	 * @param model
	 * @param reversibility
	 * @param listOfPossibleEnzymes
	 * @throws RateLawNotApplicableException
	 * @throws IOException
	 * @throws IllegalFormatException
	 */
	public PingPongMechanism(Reaction parentReaction, Model model,
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
		// according to Cornish-Bowden: Fundamentals of Enzyme kinetics
		String name = "substituted-enzyme mechanism (Ping-Pong)";
		if (getParentSBMLObject().getReversible())
			return "reversible " + name;
		return "irreversible " + name;
	}

	// @Override
	public String getSBO() {
		return "none";
	}

	// @Override
	protected ASTNode createKineticEquation(Model model, List<String> modE,
			List<String> modActi, List<String> modTActi, List<String> modInhib,
			List<String> modTInhib, List<String> modCat)
			throws RateLawNotApplicableException, IllegalFormatException {
		ASTNode numerator;// I
		ASTNode denominator; // II
		ASTNode catalysts[] = new ASTNode[Math.max(1, modE.size())];

		Reaction reaction = getParentSBMLObject();
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
			addLocalParameter(kcatp);
			addLocalParameter(kMr2);
			addLocalParameter(kMr1);

			/*
			 * Irreversible Reaction
			 */
			if (!reaction.getReversible()) {
				numerator = new ASTNode(kcatp, this);

				if (modE.size() > 0)
					numerator = ASTNode.times(numerator, new ASTNode(modE
							.get(enzymeNum), this));
				numerator = ASTNode.times(numerator, new ASTNode(specRefE1
						.getSpecies(), this));

				denominator = ASTNode.sum(ASTNode.times(
						new ASTNode(kMr2, this), new ASTNode(specRefE1
								.getSpecies(), this)), ASTNode.times(
						new ASTNode(kMr1, this), new ASTNode(specRefE2
								.getSpecies(), this)));

				if (specRefE2.equals(specRefE1)) {
					numerator = ASTNode.pow(numerator, new ASTNode(2, this));
					denominator = ASTNode.pow(ASTNode.sum(denominator,
							new ASTNode(specRefE1.getSpecies(), this)),
							new ASTNode(2, this));
				} else {
					numerator = ASTNode.times(numerator, new ASTNode(specRefE2
							.getSpecies(), this));
					denominator = ASTNode.sum(denominator, ASTNode.times(
							new ASTNode(specRefE1.getSpecies(), this),
							new ASTNode(specRefE2.getSpecies(), this)));
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
				addLocalParameter(kcatn);
				addLocalParameter(kMp2);
				addLocalParameter(kMp1);
				addLocalParameter(kIp1);
				addLocalParameter(kIp2);
				addLocalParameter(kIr1);

				ASTNode numeratorForward = ASTNode.frac(
						new ASTNode(kcatp, this), ASTNode.times(new ASTNode(
								kIr1, this), new ASTNode(kMr2, this)));
				ASTNode numeratorReverse = ASTNode.frac(
						new ASTNode(kcatn, this), ASTNode.times(new ASTNode(
								kIp1, this), new ASTNode(kMp2, this)));

				if (modE.size() > 0)
					numeratorForward = ASTNode.times(numeratorForward,
							new ASTNode(modE.get(enzymeNum), this));
				denominator = ASTNode.sum(ASTNode.frac(new ASTNode(specRefE1
						.getSpecies(), this), new ASTNode(kIr1, this)), ASTNode
						.frac(ASTNode.times(new ASTNode(kMr1, this),
								new ASTNode(specRefE2.getSpecies(), this)),
								ASTNode.times(new ASTNode(kIr1, this),
										new ASTNode(kMr2, this))), ASTNode
						.frac(new ASTNode(specRefP1.getSpecies(), this),
								new ASTNode(kIp1, this)), ASTNode.frac(ASTNode
						.times(new ASTNode(kMp1, this), new ASTNode(specRefP2
								.getSpecies(), this)), ASTNode.times(
						new ASTNode(kIp1, this), new ASTNode(kMp2, this))));
				if (specRefE2.equals(specRefE1)) {
					numeratorForward = ASTNode.times(numeratorForward, ASTNode
							.pow(new ASTNode(specRefE1.getSpecies(), this),
									new ASTNode(2, this)));
					denominator = ASTNode.sum(denominator, ASTNode.frac(ASTNode
							.pow(new ASTNode(specRefE1.getSpecies(), this),
									new ASTNode(2, this)), ASTNode.times(
							new ASTNode(kIr1, this), new ASTNode(kMr2, this))));
				} else {
					numeratorForward = ASTNode.times(numeratorForward,
							new ASTNode(specRefE1.getSpecies(), this),
							new ASTNode(specRefE2.getSpecies(), this));
					denominator = ASTNode.sum(denominator, ASTNode.frac(ASTNode
							.times(new ASTNode(specRefE1.getSpecies(), this),
									new ASTNode(specRefE2.getSpecies(), this)),
							ASTNode.times(new ASTNode(kIr1, this), new ASTNode(
									kMr2, this))));
				}

				denominator = ASTNode.sum(denominator, ASTNode.frac(ASTNode
						.times(new ASTNode(specRefE1.getSpecies(), this),
								new ASTNode(specRefP1.getSpecies(), this)),
						ASTNode.times(new ASTNode(kIr1, this), new ASTNode(
								kIp1, this))), ASTNode.frac(ASTNode.times(
						new ASTNode(kMr1, this), new ASTNode(specRefE2
								.getSpecies(), this), new ASTNode(specRefP2
								.getSpecies(), this)), ASTNode.times(
						new ASTNode(kIr1, this), new ASTNode(kMr2, this),
						new ASTNode(kIp2, this))));

				if (modE.size() > 0)
					numeratorReverse = ASTNode.times(numeratorReverse,
							new ASTNode(modE.get(enzymeNum), this));

				ASTNode denominator_p1p2 = new ASTNode(specRefE1.getSpecies(),
						this);
				if (specRefP2.equals(specRefP1)) {
					numeratorReverse = ASTNode.times(numeratorReverse, ASTNode
							.pow(new ASTNode(specRefP1.getSpecies(), this),
									new ASTNode(2, this)));
					denominator_p1p2 = ASTNode.pow(new ASTNode(specRefP1
							.getSpecies(), this), new ASTNode(2, this));

				} else {
					numeratorReverse = ASTNode.times(numeratorReverse,
							new ASTNode(specRefP1.getSpecies(), this),
							new ASTNode(specRefP2.getSpecies(), this));

					denominator_p1p2 = ASTNode.times(new ASTNode(specRefP1
							.getSpecies(), this), new ASTNode(specRefP2
							.getSpecies(), this));
				}
				numerator = ASTNode.diff(numeratorForward, numeratorReverse);
				denominator_p1p2 = ASTNode.frac(denominator_p1p2,
						ASTNode.times(new ASTNode(kIp1, this), new ASTNode(
								kMp2, this)));
				denominator = ASTNode.sum(denominator, denominator_p1p2);
			}
			catalysts[enzymeNum++] = ASTNode.frac(numerator, denominator);
		} while (enzymeNum <= modE.size() - 1);
		return ASTNode.times(activationFactor(modActi),
				inhibitionFactor(modInhib), ASTNode.sum(catalysts));
	}
}
