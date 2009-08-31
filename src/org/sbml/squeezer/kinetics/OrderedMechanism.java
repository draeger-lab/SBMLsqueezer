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

import org.sbml.ASTNode;
import org.sbml.Parameter;
import org.sbml.Reaction;
import org.sbml.SpeciesReference;
import org.sbml.squeezer.RateLawNotApplicableException;

/**
 * TODO: comment missing
 * 
 * @since 1.0
 * @version
 * @author <a href="mailto:Nadine.hassis@gmail.com">Nadine Hassis</a>
 * @author <a href="mailto:andreas.draeger@uni-tuebingen.de">Andreas
 *         Dr&auml;ger</a>
 * @date Aug 1, 2007
 */
public class OrderedMechanism extends GeneralizedMassAction {

	/**
	 * @param parentReaction
	 * @throws RateLawNotApplicableException
	 * @throws IOException
	 * @throws IllegalFormatException
	 */
	public OrderedMechanism(Reaction parentReaction)
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
		// according to Cornish-Bowden: Fundamentals of Enzyme kinetics
		double stoichiometryRight = 0;
		for (int i = 0; i < getParentSBMLObject().getNumProducts(); i++)
			stoichiometryRight += getParentSBMLObject().getProduct(i)
					.getStoichiometry();
		String name = "compulsory-order ternary-complex mechanism";
		if ((getParentSBMLObject().getNumProducts() == 2)
				|| (stoichiometryRight == 2))
			name += ", two products";
		else if ((getParentSBMLObject().getNumProducts() == 1)
				|| (stoichiometryRight == 1))
			name += ", one product";
		if (getParentSBMLObject().getReversible())
			return "reversible " + name;
		return "irreversible " + name;
	}

	// @Override
	public String getSBO() {
		return "none";
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
	ASTNode createKineticEquation(List<String> modE,
			List<String> modActi, List<String> modTActi, List<String> modInhib,
			List<String> modTInhib, List<String> modCat)
			throws RateLawNotApplicableException, IllegalFormatException {
		ASTNode numerator;// I
		ASTNode denominator; // II
		ASTNode catalysts[] = new ASTNode[Math.max(1, modE.size())];

		Reaction reaction = getParentSBMLObject();
		SpeciesReference specRefE1 = reaction.getReactant(0), specRefE2 = null;
		SpeciesReference specRefP1 = reaction.getProduct(0), specRefP2 = null;

		if (reaction.getNumReactants() == 2)
			specRefE2 = reaction.getReactant(1);
		else if (specRefE1.getStoichiometry() == 2f)
			specRefE2 = specRefE1;
		else
			throw new RateLawNotApplicableException(
					"Number of reactants must equal two to apply ordered "
							+ "Michaelis-Menten kinetics to reaction "
							+ reaction.getId());

		boolean exception = false, biuni = false;
		switch (reaction.getNumProducts()) {
		case 1:
			if (specRefP1.getStoichiometry() == 1f)
				biuni = true;
			else if (specRefP1.getStoichiometry() == 2f)
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
					"Number of products must equal either one or two to apply ordered "
							+ "kinetics to reaction " + reaction.getId());

		int enzymeNum = 0;
		do {
			/*
			 * Variables that are needed for the different combinations of
			 * educts and prodcuts.
			 */

			StringBuffer kcatp;
			StringBuffer kMr1 = concat("kM_", reaction.getId());
			StringBuffer kMr2 = concat("kM_", reaction.getId());
			StringBuffer kIr1 = concat("ki_", reaction.getId());

			// reverse reactions
			StringBuffer kcatn;
			StringBuffer kMp1 = concat("kM_", reaction.getId());
			StringBuffer kMp2 = concat("kM_", reaction.getId());
			StringBuffer kIp1 = concat("ki_", reaction.getId());
			StringBuffer kIp2 = concat("ki_", reaction.getId());
			StringBuffer kIr2 = concat("ki_", reaction.getId());

			if (modE.size() == 0) {
				kcatp = concat("Vp_", reaction.getId());
				kcatn = concat("Vn_", reaction.getId());
			} else {
				kcatp = concat("kcatp_", reaction.getId());
				kcatn = concat("kcatn_", reaction.getId());
				if (modE.size() > 1) {
					String e = modE.get(enzymeNum);
					append(kcatp, underscore, e);
					append(kMr1, underscore, e);
					append(kMr2, underscore, e);
					append(kIr1, underscore, e);
					// reverse reactions
					append(kcatn, underscore, e);
					append(kMp1, underscore, e);
					append(kMp2, underscore, e);
					append(kIp1, underscore, e);
					append(kIp2, underscore, e);
					append(kIr2, underscore, e);
				}
			}
			append(kMr2, underscore, specRefE2.getSpecies());
			append(kMr1, underscore, specRefE1.getSpecies());
			// reverse reactions
			append(kMp1, underscore, specRefP1.getSpecies());
			if (specRefP2 != null)
				append(kMp2, underscore, specRefP2.getSpecies());

			if (specRefE2.equals(specRefE1)) {
				kMr1 = concat("kMr1", kMr1.substring(2));
				kMr2 = concat("kMr2", kMr2.substring(2));
				kIr1 = concat("kIr1", kIr1.substring(2));
				kIr2 = concat("kIr2", kIr2.substring(2));
			}
			append(kIr1, underscore, specRefE1.getSpecies());
			append(kIr2, underscore, specRefE2.getSpecies());

			// reversible reactions
			kIp1 = concat(kIp1, underscore, specRefP1.getSpecies());
			if (specRefP2 != null) {
				if (specRefP2.equals(specRefP1)) {
					kMp1 = concat("kMp1", kMp1.substring(2));
					kMp2 = concat("kMp2", kMp2.substring(2));
					kIp1 = concat("kIp1", kIp1.substring(2));
					kIp2 = concat("kIp2", kIp2.substring(2));
				}
				append(kIp2, underscore, specRefP2.getSpecies());
			}
			Parameter p_kcatp = new Parameter(kcatp.toString());
			addLocalParameter(p_kcatp);

			/*
			 * addLocalParameter(kcatp); Irreversible reaction (bi-bi or bi-uni
			 * does not matter)
			 */
			if (!reaction.getReversible()) {
				Parameter p_kMr1 = new Parameter(kMr1.toString());
				Parameter p_kMr2 = new Parameter(kMr2.toString());
				Parameter p_kIr1 = new Parameter(kIr1.toString());
				addLocalParameter(p_kMr1);
				addLocalParameter(p_kMr2);
				addLocalParameter(p_kIr1);

				numerator = new ASTNode(p_kcatp, this);
				if (modE.size() > 0)
					numerator = ASTNode.times(numerator, new ASTNode(modE
							.get(enzymeNum), this));
				numerator = ASTNode.times(numerator, ASTNode.pow(new ASTNode(
						specRefE1.getSpeciesInstance(), this), new ASTNode(
						specRefE1.getStoichiometry(), this)));
				denominator = ASTNode.times(new ASTNode(p_kIr1, this),
						new ASTNode(p_kMr2, this));

				if (specRefE2.equals(specRefE1)) {
					denominator = ASTNode.sum(denominator, ASTNode.times(
							ASTNode.sum(new ASTNode(p_kMr1, this), new ASTNode(
									p_kMr2, this)), new ASTNode(specRefE1
									.getSpeciesInstance(), this)), ASTNode.pow(
							new ASTNode(specRefE1.getSpeciesInstance(), this),
							new ASTNode(2, this)));
				} else {
					numerator = ASTNode.times(numerator, ASTNode.pow(
							new ASTNode(specRefE2.getSpeciesInstance(), this),
							new ASTNode(specRefE2.getStoichiometry(), this)));
					denominator = ASTNode.sum(denominator, ASTNode.times(
							new ASTNode(p_kMr2, this), new ASTNode(specRefE1
									.getSpeciesInstance(), this)), ASTNode
							.times(new ASTNode(p_kMr1, this), new ASTNode(
									specRefE2.getSpeciesInstance(), this)),
							ASTNode.times(new ASTNode(specRefE1
									.getSpeciesInstance(), this), new ASTNode(
									specRefE2.getSpeciesInstance(), this)));
				}
			} else if (!biuni) {
				/*
				 * Reversible Bi-Bi reaction.
				 */
				Parameter p_kIr2 = new Parameter(kIr2.toString());
				Parameter p_kcatn = new Parameter(kcatn.toString());
				Parameter p_kMr1 = new Parameter(kMr1.toString());
				Parameter p_kMr2 = new Parameter(kMr2.toString());
				Parameter p_kMp1 = new Parameter(kMp1.toString());
				Parameter p_kMp2 = new Parameter(kMp2.toString());
				Parameter p_kIr1 = new Parameter(kIr1.toString());
				Parameter p_kIp1 = new Parameter(kIp1.toString());
				Parameter p_kIp2 = new Parameter(kIp2.toString());
				addLocalParameters(p_kIr2, p_kcatn, p_kMr1, p_kMr2, p_kMp1,
						p_kMp2, p_kIr1, p_kIp1, p_kIp2);
				ASTNode numeratorForward = ASTNode.frac(new ASTNode(p_kcatp,
						this), ASTNode.times(new ASTNode(p_kIr1, this),
						new ASTNode(p_kMr2, this)));
				ASTNode numeratorReverse = ASTNode.frac(new ASTNode(p_kcatn,
						this), ASTNode.times(new ASTNode(p_kIp2, this),
						new ASTNode(p_kMp1, this)));

				if (modE.size() > 0)
					numeratorForward = ASTNode.times(numeratorForward,
							new ASTNode(modE.get(enzymeNum), this));

				denominator = ASTNode.sum(new ASTNode(1, this), ASTNode.frac(
						new ASTNode(specRefE1.getSpeciesInstance(), this),
						new ASTNode(p_kIr1, this)), ASTNode.frac(ASTNode.times(
						new ASTNode(p_kMr1, this), new ASTNode(specRefE2
								.getSpeciesInstance(), this)), ASTNode.times(
						new ASTNode(p_kIr1, this), new ASTNode(p_kMr2, this))),
						ASTNode.frac(ASTNode.times(new ASTNode(p_kMp2, this),
								new ASTNode(specRefP1.getSpeciesInstance(),
										this)), ASTNode.times(new ASTNode(
								p_kIp2, this), new ASTNode(p_kMp1, this))),
						ASTNode.frac(new ASTNode(
								specRefP2.getSpeciesInstance(), this),
								new ASTNode(p_kIp2, this)));

				if (specRefE2.equals(specRefE1)) {
					numeratorForward = ASTNode.times(numeratorForward, ASTNode
							.pow(new ASTNode(specRefE1.getSpeciesInstance(),
									this), new ASTNode(2, this)));
					denominator = ASTNode.sum(denominator, ASTNode.frac(ASTNode
							.pow(new ASTNode(specRefE1.getSpeciesInstance(),
									this), new ASTNode(2, this)), ASTNode
							.times(new ASTNode(p_kIr1, this), new ASTNode(
									p_kMr2, this))));
				} else {
					numeratorForward = ASTNode.times(numeratorForward, ASTNode
							.times(new ASTNode(specRefE1.getSpeciesInstance(),
									this)), new ASTNode(specRefE2
							.getSpeciesInstance(), this));
					denominator = ASTNode.sum(denominator, ASTNode.frac(ASTNode
							.times(new ASTNode(specRefE1.getSpeciesInstance(),
									this), new ASTNode(specRefE2
									.getSpeciesInstance(), this)), ASTNode
							.times(new ASTNode(p_kIr1, this), new ASTNode(
									p_kMr2, this))));
				}

				if (modE.size() > 0)
					numeratorReverse = ASTNode.times(numeratorReverse,
							new ASTNode(modE.get(enzymeNum), this));

				if (specRefP2.equals(specRefP1))
					numeratorReverse = ASTNode.times(numeratorReverse, ASTNode
							.pow(new ASTNode(specRefP1.getSpeciesInstance(),
									this), new ASTNode(2, this)));
				else
					numeratorReverse = ASTNode.times(numeratorReverse, ASTNode
							.times(new ASTNode(specRefP1.getSpeciesInstance(),
									this), new ASTNode(specRefP2
									.getSpeciesInstance(), this)));
				numerator = ASTNode.diff(numeratorForward, numeratorReverse);

				denominator = ASTNode.sum(denominator, ASTNode.frac(ASTNode
						.times(new ASTNode(p_kMp2, this), new ASTNode(specRefE1
								.getSpeciesInstance(), this), new ASTNode(
								specRefP1.getSpeciesInstance(), this)), ASTNode
						.times(new ASTNode(p_kIr1, this), new ASTNode(p_kMp1,
								this), new ASTNode(p_kIp2, this))), ASTNode
						.frac(ASTNode.times(new ASTNode(p_kMr1, this),
								new ASTNode(specRefE2.getSpeciesInstance(),
										this), new ASTNode(specRefP2
										.getSpeciesInstance(), this)), ASTNode
								.times(new ASTNode(p_kIr1, this), new ASTNode(
										p_kMr2, this),
										new ASTNode(p_kIp2, this))));

				if (specRefP2.equals(specRefP1))
					denominator = ASTNode.sum(denominator, ASTNode.frac(ASTNode
							.pow(new ASTNode(specRefP1.getSpeciesInstance(),
									this), new ASTNode(2, this)), ASTNode
							.times(new ASTNode(p_kMp1, this), new ASTNode(
									p_kIp2, this))));
				else
					denominator = ASTNode.sum(denominator, ASTNode.frac(ASTNode
							.times(new ASTNode(specRefP1.getSpeciesInstance(),
									this), new ASTNode(specRefP2
									.getSpeciesInstance(), this)), ASTNode
							.times(new ASTNode(p_kMp1, this), new ASTNode(
									p_kIp2, this))));

				if (specRefE2.equals(specRefE1))
					denominator = ASTNode.sum(denominator, ASTNode.frac(ASTNode
							.times(ASTNode.pow(new ASTNode(specRefE1
									.getSpeciesInstance(), this), new ASTNode(
									2, this)), new ASTNode(specRefP1
									.getSpeciesInstance(), this)), ASTNode
							.times(new ASTNode(p_kIr1, this), new ASTNode(
									p_kMr2, this), new ASTNode(p_kIp1, this))));
				else
					denominator = ASTNode.sum(denominator, ASTNode.frac(ASTNode
							.times(new ASTNode(specRefE1.getSpeciesInstance(),
									this), new ASTNode(specRefE2
									.getSpeciesInstance(), this), new ASTNode(
									specRefP1.getSpeciesInstance(), this)),
							ASTNode.times(new ASTNode(p_kIr1, this),
									new ASTNode(p_kMr2, this), new ASTNode(
											p_kIp1, this))));

				if (specRefP2.equals(specRefP1))
					denominator = ASTNode.sum(denominator, ASTNode.frac(ASTNode
							.times(new ASTNode(specRefE2.getSpeciesInstance(),
									this), ASTNode.pow(new ASTNode(specRefP1
									.getSpeciesInstance(), this), new ASTNode(
									2, this))), ASTNode.times(new ASTNode(
							p_kIr2, this), new ASTNode(p_kMp1, this),
							new ASTNode(p_kIp2, this))));
				else
					denominator = ASTNode.sum(denominator, ASTNode.frac(ASTNode
							.times(new ASTNode(specRefE2.getSpeciesInstance(),
									this), ASTNode.times(new ASTNode(specRefP1
									.getSpeciesInstance(), this), new ASTNode(
									specRefP2.getSpeciesInstance(), this))),
							ASTNode.times(new ASTNode(p_kIr2, this),
									new ASTNode(p_kMp1, this), new ASTNode(
											p_kIp2, this))));
			} else {
				/*
				 * Reversible bi-uni reaction
				 */
				Parameter p_kcatn = new Parameter(kcatn.toString());
				Parameter p_kMr1 = new Parameter(kMr1.toString());
				Parameter p_kMr2 = new Parameter(kMr2.toString());
				Parameter p_kMp1 = new Parameter(kMp1.toString());
				Parameter p_kIr1 = new Parameter(kIr1.toString());
				Parameter p_kIp1 = new Parameter(kIp1.toString());
				addLocalParameters(p_kcatn, p_kMr1, p_kMr2, p_kMp1, p_kIr1,
						p_kIp1);

				ASTNode numeratorForward = ASTNode.frac(new ASTNode(p_kcatp,
						this), ASTNode.times(new ASTNode(p_kIr1, this),
						new ASTNode(p_kMr2, this)));
				ASTNode numeratorReverse = ASTNode.frac(new ASTNode(p_kcatn,
						this), new ASTNode(p_kMp1, this));

				if (modE.size() > 0)
					numeratorForward = ASTNode.times(numeratorForward,
							new ASTNode(modE.get(enzymeNum), this));

				// numeratorForward = times(numeratorForward, specRefE1
				// .getSpeciesInstance());

				denominator = ASTNode.sum(new ASTNode(1, this), ASTNode.frac(
						new ASTNode(specRefE1.getSpeciesInstance(), this),
						new ASTNode(p_kIr1, this)), ASTNode.frac(ASTNode.times(
						new ASTNode(p_kMr1, this), new ASTNode(specRefE2
								.getSpeciesInstance(), this)), ASTNode.times(
						new ASTNode(p_kIr1, this), new ASTNode(p_kMr2, this))));

				if (specRefE2.equals(specRefE1)) {
					numeratorForward = ASTNode.times(numeratorForward, ASTNode
							.pow(new ASTNode(specRefE1.getSpeciesInstance(),
									this), new ASTNode(2, this)));
					denominator = ASTNode.sum(denominator, ASTNode.frac(ASTNode
							.pow(new ASTNode(specRefE1.getSpeciesInstance(),
									this), new ASTNode(2, this)), ASTNode
							.times(new ASTNode(p_kIr1, this), new ASTNode(
									p_kMr2, this))));
				} else {
					numeratorForward = ASTNode.times(numeratorForward, ASTNode
							.times(new ASTNode(specRefE1.getSpeciesInstance(),
									this), new ASTNode(specRefE2
									.getSpeciesInstance(), this)));
					denominator = ASTNode.sum(denominator, ASTNode.frac(ASTNode
							.times(new ASTNode(specRefE1.getSpeciesInstance(),
									this), new ASTNode(specRefE2
									.getSpeciesInstance(), this)), ASTNode
							.times(new ASTNode(p_kIr1, this), new ASTNode(
									p_kMr2, this))));
				}
				if (modE.size() > 0)
					numeratorReverse = ASTNode.times(numeratorReverse,
							new ASTNode(modE.get(enzymeNum), this));
				numeratorReverse = ASTNode.times(numeratorReverse, new ASTNode(
						specRefP1.getSpeciesInstance(), this));
				numerator = ASTNode.diff(numeratorForward, numeratorReverse);
				denominator = ASTNode.sum(denominator, ASTNode.frac(ASTNode
						.times(new ASTNode(p_kMr1, this), new ASTNode(specRefE2
								.getSpeciesInstance(), this), new ASTNode(
								specRefP1.getSpeciesInstance(), this)), ASTNode
						.times(new ASTNode(p_kIr1, this), new ASTNode(p_kMr2,
								this), new ASTNode(p_kIp1, this))), ASTNode
						.frac(
								new ASTNode(specRefP1.getSpeciesInstance(),
										this), new ASTNode(p_kMp1, this)));
			}

			/*
			 * Construct formula
			 */
			catalysts[enzymeNum++] = ASTNode.frac(numerator, denominator);
		} while (enzymeNum <= modE.size() - 1);
		return ASTNode.times(activationFactor(modActi),
				inhibitionFactor(modInhib), ASTNode.sum(catalysts));
	}
}
