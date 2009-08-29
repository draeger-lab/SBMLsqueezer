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
import org.sbml.Parameter;
import org.sbml.Reaction;
import org.sbml.SpeciesReference;

/**
 * <p>
 * This is the standard convenience kinetics which is only appropriated for
 * systems whose stochiometric matrix has full column rank. Otherwise the more
 * complicated thermodynamically independend form {@link ConvenienceIndependent}
 * needs to be invoked.
 * </p>
 * <p>
 * Creates a convenience kinetic equation. For each enzyme contained in the
 * given reaction, the convenience kinetic term is formed by calling methods
 * which return the numerator and denominator of the fraction. These methods
 * distinguish between reversible and irreversible reactions. The fractions are
 * multiplied with the concentration of the respective enzyme and, afterwards,
 * summed up. The whole sum is multiplied with the activators and inhibitors of
 * the reaction.
 * </p>
 * 
 * @since 1.0
 * @version
 * @author <a href="mailto:Nadine.hassis@gmail.com">Nadine Hassis</a>
 * @author <a href="mailto:andreas.draeger@uni-tuebingen.de">Andreas
 *         Dr&auml;ger</a>
 * @author <a href="mailto:michael@diegrauezelle.de">Michael Ziller</a>
 * @author <a href="mailto:hannes.borch@googlemail.com">Hannes Borch</a>
 * @author <a href="mailto:dwouamba@yahoo.fr">Dieudonn&eacute; Motsou
 *         Wouamba</a>
 * @date Aug 1, 2007
 */
public class Convenience extends GeneralizedMassAction {

	public static boolean isApplicable(Reaction reaction) {
		// TODO
		return true;
	}

	/**
	 * @param parentReaction
	 * @throws RateLawNotApplicableException
	 * @throws IOException
	 * @throws IllegalFormatException
	 */
	public Convenience(Reaction parentReaction)
			throws RateLawNotApplicableException, IOException,
			IllegalFormatException {
		super(parentReaction);
	}

	// @Override
	public String getName() {
		if (getParentSBMLObject().getReversible())
			return "reversible simple convenience kinetics";
		return "irreversible simple convenience kinetics";
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
		ASTNode numerator;
		ASTNode formelTxt[] = new ASTNode[Math.max(1, modE.size())];

		Reaction parentReaction = getParentSBMLObject();
		int enzymeNum = 0;
		do {
			ASTNode denominator;
			StringBuffer kcatp = modE.size() == 0 ? concat("Vp_",
					getParentSBMLObject().getId()) : concat("kcatp_",
					getParentSBMLObject().getId());
			if (modE.size() > 1)
				kcatp = append(kcatp, underscore, modE.get(enzymeNum));
			Parameter p_kcatp = new Parameter(kcatp.toString());
			addLocalParameter(p_kcatp);
			numerator = new ASTNode(p_kcatp, this);

			// sums for each educt
			ASTNode[] denominator1 = new ASTNode[parentReaction
					.getNumReactants()];
			for (int eductNum = 0; eductNum < parentReaction.getNumReactants(); eductNum++) {

				SpeciesReference specref = parentReaction.getReactant(eductNum);

				// build denominator

				StringBuffer kM = concat("kM_", getParentSBMLObject().getId());

				if (modE.size() > 1)
					kM = concat(kM, underscore, modE.get(enzymeNum));
				kM = append(kM, underscore, specref.getSpecies());
				Parameter p_kM = new Parameter(kM.toString());
				addLocalParameter(p_kM);

				denominator1[eductNum] = ASTNode.frac(new ASTNode(specref
						.getSpeciesInstance(), this), new ASTNode(p_kM, this));

				for (int m = 1; m < (int) specref.getStoichiometry(); m++) {
					denominator1[eductNum] = ASTNode.sum(
							denominator1[eductNum], ASTNode.pow(ASTNode.frac(
									new ASTNode(specref.getSpeciesInstance(),
											this), new ASTNode(p_kM, this)),
									new ASTNode((m + 1), this)));
				}

				if (!parentReaction.getReversible()
						|| ((parentReaction.getNumReactants() != 1) || (parentReaction
								.getNumProducts() == 1)))

					denominator1[eductNum] = ASTNode.sum(new ASTNode(1, this),
							denominator1[eductNum]);

				// build numerator
				if (specref.getStoichiometry() == 1d)
					numerator = ASTNode.times(numerator, ASTNode.frac(
							new ASTNode(specref.getSpeciesInstance(), this),
							new ASTNode(p_kM, this)));
				else
					numerator = ASTNode.times(numerator, ASTNode.pow(ASTNode
							.frac(new ASTNode(specref.getSpeciesInstance(),
									this), new ASTNode(p_kM, this)),
							new ASTNode(specref.getStoichiometry(), this)));
			}
			if (denominator1.length == 1)
				denominator = denominator1[0];
			else
				denominator = ASTNode.times(denominator1);

			/*
			 * only if reaction is reversible or we want it to be.
			 */
			if (parentReaction.getReversible()) {

				ASTNode numerator2;
				ASTNode[] denominator2 = new ASTNode[parentReaction
						.getNumProducts()];

				StringBuffer kcat = modE.size() == 0 ? concat("Vn_",
						getParentSBMLObject().getId()) : concat("kcatn_",
						getParentSBMLObject().getId());
				if (modE.size() > 1)
					kcat = concat(kcat, underscore, modE.get(enzymeNum));
				Parameter p_kcat = new Parameter(kcat.toString());
				addLocalParameter(p_kcat);
				numerator2 = new ASTNode(p_kcat, this);

				// Sums for each product

				for (int productNum = 0; productNum < parentReaction
						.getNumProducts(); productNum++) {

					StringBuffer kM = concat("kM_", getParentSBMLObject()
							.getId());

					if (modE.size() > 1)
						kM = append(kM, underscore, modE.get(enzymeNum));
					kM = append(kM, underscore, parentReaction.getProduct(
							productNum).getSpeciesInstance().getId());
					Parameter p_kM = new Parameter(kM.toString());
					addLocalParameter(p_kM);

					SpeciesReference specRefP = parentReaction
							.getProduct(productNum);

					denominator2[productNum] = ASTNode.frac(new ASTNode(
							specRefP.getSpeciesInstance(), this), new ASTNode(
							p_kM, this));

					// for each stoichiometry (see Liebermeister et al.)s
					for (int m = 1; m < (int) specRefP.getStoichiometry(); m++)

						denominator2[productNum] = ASTNode.sum(
								denominator2[productNum], ASTNode.pow(ASTNode
										.frac(new ASTNode(specRefP
												.getSpeciesInstance(), this),
												new ASTNode(p_kM, this)),
										new ASTNode((m + 1), this)));
					if (parentReaction.getNumProducts() > 1)
						denominator2[productNum] = ASTNode.sum(new ASTNode(1,
								this), denominator2[productNum]);

					// build numerator
					if (specRefP.getStoichiometry() != 1.0)
						numerator2 = ASTNode.times(numerator2, ASTNode.pow(
								ASTNode.frac(new ASTNode(specRefP
										.getSpeciesInstance(), this),
										new ASTNode(p_kM, this)), new ASTNode(
										specRefP.getStoichiometry(), this)));
					else
						numerator2 = ASTNode.times(numerator2, ASTNode
								.frac(new ASTNode(
										specRefP.getSpeciesInstance(), this),
										new ASTNode(p_kM, this)));
				}
				if (parentReaction.getNumProducts() == 1)
					denominator = ASTNode.sum(denominator, denominator2[0]);
				else
					denominator = ASTNode.sum(denominator, ASTNode
							.times(denominator2));
				numerator = ASTNode.diff(numerator, numerator2);

				if ((parentReaction.getNumProducts() > 1)
						&& (parentReaction.getNumReactants() > 1))
					denominator = ASTNode.diff(denominator,
							new ASTNode(1, this));
			}

			formelTxt[enzymeNum] = ASTNode.frac(numerator, denominator);

			if (modE.size() > 0)
				formelTxt[enzymeNum] = ASTNode.times(new ASTNode(modE
						.get(enzymeNum), this), formelTxt[enzymeNum]);
			enzymeNum++;
		} while (enzymeNum < modE.size());

		return ASTNode.times(activationFactor(modActi),
				inhibitionFactor(modInhib), ASTNode.sum(formelTxt));
	}
}
