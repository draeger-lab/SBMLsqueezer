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
 * @date Aug 1, 2007
 */
public class OrderedMechanism extends GeneralizedMassAction {

	/**
	 * @param parentReaction
	 * @param model
	 * @param reversibility
	 * @throws RateLawNotApplicableException
	 * @throws IOException
	 * @throws IllegalFormatException
	 */
	public OrderedMechanism(Reaction parentReaction, Model model,
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
	public OrderedMechanism(Reaction parentReaction, Model model,
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

	// @Override
	protected ASTNode createKineticEquation(Model model, List<String> modE,
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

			addLocalParameter(kcatp);

			/*
			 * addLocalParameter(kcatp); Irreversible reaction (bi-bi or bi-uni
			 * does not matter)
			 */
			if (!reaction.getReversible()) {
				addLocalParameter(kMr2);
				addLocalParameter(kMr1);
				addLocalParameter(kIr1);

				numerator = new ASTNode(kcatp, this);
				if (modE.size() > 0)
					numerator = ASTNode.times(numerator, new ASTNode(modE
							.get(enzymeNum), this));
				numerator = ASTNode.times(numerator, ASTNode.pow(new ASTNode(
						specRefE1.getSpecies(), this), new ASTNode(
						getStoichiometry(specRefE1), this)));
				denominator = ASTNode.times(new ASTNode(kIr1, this),
						new ASTNode(kMr2, this));

				if (specRefE2.equals(specRefE1)) {
					denominator = ASTNode.sum(denominator, ASTNode.times(
							ASTNode.sum(new ASTNode(kMr1, this), new ASTNode(
									kMr2, this)), new ASTNode(specRefE1
									.getSpecies(), this)), ASTNode.pow(
							new ASTNode(specRefE1.getSpecies(), this),
							new ASTNode(2, this)));
				} else {
					numerator = ASTNode.times(numerator, ASTNode.pow(
							new ASTNode(specRefE2.getSpecies(), this),
							new ASTNode(getStoichiometry(specRefE2), this)));
					denominator = ASTNode.sum(denominator, ASTNode.times(
							new ASTNode(kMr2, this), new ASTNode(specRefE1
									.getSpecies(), this)), ASTNode.times(
							new ASTNode(kMr1, this), new ASTNode(specRefE2
									.getSpecies(), this)), ASTNode.times(
							new ASTNode(specRefE1.getSpecies(), this),
							new ASTNode(specRefE2.getSpecies(), this)));
				}
			} else if (!biuni) {
				/*
				 * Reversible Bi-Bi reaction.
				 */
				addLocalParameter(kIr2);
				addLocalParameter(kcatn);
				addLocalParameter(kMr1);
				addLocalParameter(kMr2);
				addLocalParameter(kMp1);
				addLocalParameter(kMp2);
				addLocalParameter(kIr1);
				addLocalParameter(kIp1);
				addLocalParameter(kIp2);

				ASTNode numeratorForward = ASTNode.frac(
						new ASTNode(kcatp, this), ASTNode.times(new ASTNode(
								kIr1, this), new ASTNode(kMr2, this)));
				ASTNode numeratorReverse = ASTNode.frac(
						new ASTNode(kcatn, this), ASTNode.times(new ASTNode(
								kIp2, this), new ASTNode(kMp1, this)));

				if (modE.size() > 0)
					numeratorForward = ASTNode.times(numeratorForward,
							new ASTNode(modE.get(enzymeNum), this));

				denominator = ASTNode
						.sum(new ASTNode(1, this), ASTNode.frac(new ASTNode(
								specRefE1.getSpecies(), this), new ASTNode(
								kIr1, this)), ASTNode.frac(ASTNode.times(
								new ASTNode(kMr1, this), new ASTNode(specRefE2
										.getSpecies(), this)), ASTNode.times(
								new ASTNode(kIr1, this),
								new ASTNode(kMr2, this))), ASTNode.frac(ASTNode
								.times(new ASTNode(kMp2, this), new ASTNode(
										specRefP1.getSpecies(), this)), ASTNode
								.times(new ASTNode(kIp2, this), new ASTNode(
										kMp1, this))), ASTNode.frac(
								new ASTNode(specRefP2.getSpecies(), this),
								new ASTNode(kIp2, this)));

				if (specRefE2.equals(specRefE1)) {
					numeratorForward = ASTNode.times(numeratorForward, ASTNode
							.pow(new ASTNode(specRefE1.getSpecies(), this),
									new ASTNode(2, this)));
					denominator = ASTNode.sum(denominator, ASTNode.frac(ASTNode
							.pow(new ASTNode(specRefE1.getSpecies(), this),
									new ASTNode(2, this)), ASTNode.times(
							new ASTNode(kIr1, this), new ASTNode(kMr2, this))));
				} else {
					numeratorForward = ASTNode.times(numeratorForward, ASTNode
							.times(new ASTNode(specRefE1.getSpecies(), this)),
							new ASTNode(specRefE2.getSpecies(), this));
					denominator = ASTNode.sum(denominator, ASTNode.frac(ASTNode
							.times(new ASTNode(specRefE1.getSpecies(), this),
									new ASTNode(specRefE2.getSpecies(), this)),
							ASTNode.times(new ASTNode(kIr1, this), new ASTNode(
									kMr2, this))));
				}

				if (modE.size() > 0)
					numeratorReverse = ASTNode.times(numeratorReverse,
							new ASTNode(modE.get(enzymeNum), this));

				if (specRefP2.equals(specRefP1))
					numeratorReverse = ASTNode.times(numeratorReverse, ASTNode
							.pow(new ASTNode(specRefP1.getSpecies(), this),
									new ASTNode(2, this)));
				else
					numeratorReverse = ASTNode.times(numeratorReverse, ASTNode
							.times(new ASTNode(specRefP1.getSpecies(), this),
									new ASTNode(specRefP2.getSpecies(), this)));
				numerator = ASTNode.diff(numeratorForward, numeratorReverse);

				denominator = ASTNode.sum(denominator, ASTNode.frac(ASTNode
						.times(new ASTNode(kMp2, this), new ASTNode(specRefE1
								.getSpecies(), this), new ASTNode(specRefP1
								.getSpecies(), this)), ASTNode.times(
						new ASTNode(kIr1, this), new ASTNode(kMp1, this),
						new ASTNode(kIp2, this))), ASTNode.frac(ASTNode.times(
						new ASTNode(kMr1, this), new ASTNode(specRefE2
								.getSpecies(), this), new ASTNode(specRefP2
								.getSpecies(), this)), ASTNode.times(
						new ASTNode(kIr1, this), new ASTNode(kMr2, this),
						new ASTNode(kIp2, this))));

				if (specRefP2.equals(specRefP1))
					denominator = ASTNode.sum(denominator, ASTNode.frac(ASTNode
							.pow(new ASTNode(specRefP1.getSpecies(), this),
									new ASTNode(2, this)), ASTNode.times(
							new ASTNode(kMp1, this), new ASTNode(kIp2, this))));
				else
					denominator = ASTNode.sum(denominator, ASTNode.frac(ASTNode
							.times(new ASTNode(specRefP1.getSpecies(), this),
									new ASTNode(specRefP2.getSpecies(), this)),
							ASTNode.times(new ASTNode(kMp1, this), new ASTNode(
									kIp2, this))));

				if (specRefE2.equals(specRefE1))
					denominator = ASTNode.sum(denominator, ASTNode.frac(ASTNode
							.times(
									ASTNode.pow(new ASTNode(specRefE1
											.getSpecies(), this), new ASTNode(
											2, this)), new ASTNode(specRefP1
											.getSpecies(), this)), ASTNode
							.times(new ASTNode(kIr1, this), new ASTNode(kMr2,
									this), new ASTNode(kIp1, this))));
				else
					denominator = ASTNode.sum(denominator, ASTNode.frac(ASTNode
							.times(new ASTNode(specRefE1.getSpecies(), this),
									new ASTNode(specRefE2.getSpecies(), this),
									new ASTNode(specRefP1.getSpecies(), this)),
							ASTNode.times(new ASTNode(kIr1, this), new ASTNode(
									kMr2, this), new ASTNode(kIp1, this))));

				if (specRefP2.equals(specRefP1))
					denominator = ASTNode.sum(denominator, ASTNode.frac(ASTNode
							.times(new ASTNode(specRefE2.getSpecies(), this),
									ASTNode.pow(new ASTNode(specRefP1
											.getSpecies(), this), new ASTNode(
											2, this))), ASTNode.times(
							new ASTNode(kIr2, this), new ASTNode(kMp1, this),
							new ASTNode(kIp2, this))));
				else
					denominator = ASTNode.sum(denominator, ASTNode.frac(ASTNode
							.times(new ASTNode(specRefE2.getSpecies(), this),
									ASTNode.times(new ASTNode(specRefP1
											.getSpecies(), this), new ASTNode(
											specRefP2.getSpecies(), this))),
							ASTNode.times(new ASTNode(kIr2, this), new ASTNode(
									kMp1, this), new ASTNode(kIp2, this))));
			} else {
				/*
				 * Reversible bi-uni reaction
				 */
				addLocalParameter(kcatn);
				addLocalParameter(kMr1);
				addLocalParameter(kMr2);
				addLocalParameter(kMp1);
				addLocalParameter(kIr1);
				addLocalParameter(kIp1);

				ASTNode numeratorForward = ASTNode.frac(
						new ASTNode(kcatp, this), ASTNode.times(new ASTNode(
								kIr1, this), new ASTNode(kMr2, this)));
				ASTNode numeratorReverse = ASTNode.frac(
						new ASTNode(kcatn, this), new ASTNode(kMp1, this));

				if (modE.size() > 0)
					numeratorForward = ASTNode.times(numeratorForward,
							new ASTNode(modE.get(enzymeNum), this));

				// numeratorForward = times(numeratorForward, specRefE1
				// .getSpecies());

				denominator = ASTNode.sum(new ASTNode(1, this), ASTNode.frac(
						new ASTNode(specRefE1.getSpecies(), this), new ASTNode(
								kIr1, this)), ASTNode.frac(ASTNode.times(
						new ASTNode(kMr1, this), new ASTNode(specRefE2
								.getSpecies(), this)), ASTNode.times(
						new ASTNode(kIr1, this), new ASTNode(kMr2, this))));

				if (specRefE2.equals(specRefE1)) {
					numeratorForward = ASTNode.times(numeratorForward, ASTNode
							.pow(new ASTNode(specRefE1.getSpecies(), this),
									new ASTNode(2, this)));
					denominator = ASTNode.sum(denominator, ASTNode.frac(ASTNode
							.pow(new ASTNode(specRefE1.getSpecies(), this),
									new ASTNode(2, this)), ASTNode.times(
							new ASTNode(kIr1, this), new ASTNode(kMr2, this))));
				} else {
					numeratorForward = ASTNode.times(numeratorForward, ASTNode
							.times(new ASTNode(specRefE1.getSpecies(), this),
									new ASTNode(specRefE2.getSpecies(), this)));
					denominator = ASTNode.sum(denominator, ASTNode.frac(ASTNode
							.times(new ASTNode(specRefE1.getSpecies(), this),
									new ASTNode(specRefE2.getSpecies(), this)),
							ASTNode.times(new ASTNode(kIr1, this), new ASTNode(
									kMr2, this))));
				}
				if (modE.size() > 0)
					numeratorReverse = ASTNode.times(numeratorReverse,
							new ASTNode(modE.get(enzymeNum), this));
				numeratorReverse = ASTNode.times(numeratorReverse, new ASTNode(
						specRefP1.getSpecies(), this));
				numerator = ASTNode.diff(numeratorForward, numeratorReverse);
				denominator = ASTNode
						.sum(denominator, ASTNode.frac(ASTNode.times(
								new ASTNode(kMr1, this), new ASTNode(specRefE2
										.getSpecies(), this), new ASTNode(
										specRefP1.getSpecies(), this)), ASTNode
								.times(new ASTNode(kIr1, this), new ASTNode(
										kMr2, this), new ASTNode(kIp1, this))),
								ASTNode.frac(new ASTNode(
										specRefP1.getSpecies(), this),
										new ASTNode(kMp1, this)));
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
