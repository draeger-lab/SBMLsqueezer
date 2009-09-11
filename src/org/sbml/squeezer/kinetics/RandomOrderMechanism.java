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

import org.sbml.jlibsbml.ASTNode;
import org.sbml.jlibsbml.Parameter;
import org.sbml.jlibsbml.Reaction;
import org.sbml.jlibsbml.Species;
import org.sbml.jlibsbml.SpeciesReference;
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
public class RandomOrderMechanism extends GeneralizedMassAction {

	public static boolean isApplicable(Reaction reaction) {
		// TODO
		return true;
	}

	/**
	 * @param parentReaction
	 * @param model
	 * @throws RateLawNotApplicableException
	 * @throws IOException
	 * @throws IllegalFormatException
	 */
	public RandomOrderMechanism(Reaction parentReaction)
			throws RateLawNotApplicableException, IOException,
			IllegalFormatException {
		super(parentReaction);
	}

	// @Override
	public String getName() {
		// according to Cornish-Bowden: Fundamentals of Enzyme kinetics
		double stoichiometryRight = 0;
		for (int i = 0; i < getParentSBMLObject().getNumProducts(); i++)
			stoichiometryRight += getParentSBMLObject().getProduct(i)
					.getStoichiometry();
		String name = "rapid-equilibrium random order ternary-complex mechanism";
		if ((getParentSBMLObject().getNumProducts() == 2)
				|| (stoichiometryRight == 2))
			name += " with two products";
		else if ((getParentSBMLObject().getNumProducts() == 1)
				|| (stoichiometryRight == 1))
			name += " with one product";
		if (getParentSBMLObject().getReversible())
			return "reversible " + name;
		return "irreversible " + name;
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
			/*
			 * Irreversible reaction
			 */
			if (!reaction.getReversible()) {
				StringBuffer kcatp;
				StringBuffer kMr1 = concat("kM_", reaction.getId());
				StringBuffer kMr2 = concat("kM_", reaction.getId());
				StringBuffer kIr1 = concat("ki_", reaction.getId());

				if (modE.size() == 0)
					kcatp = concat("Vp_", reaction.getId());
				else {
					kcatp = concat("kcatp_", reaction.getId());
					if (modE.size() > 1) {
						append(kcatp, underscore, modE.get(enzymeNum));
						append(kMr2, underscore, modE.get(enzymeNum));
						append(kMr1, underscore, modE.get(enzymeNum));
						append(kIr1, underscore, modE.get(enzymeNum));
					}
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
				Parameter p_kcatp = new Parameter(kcatp.toString(), getLevel(),
						getVersion());
				Parameter p_kMr1 = new Parameter(kMr1.toString(), getLevel(),
						getVersion());
				Parameter p_kMr2 = new Parameter(kMr2.toString(), getLevel(),
						getVersion());
				Parameter p_kIr1 = new Parameter(kIr1.toString(), getLevel(),
						getVersion());
				addLocalParameters(p_kcatp, p_kMr1, p_kMr2, p_kIr1);

				numerator = new ASTNode(p_kcatp, this);
				if (modE.size() > 0)
					numerator = ASTNode.times(numerator, new ASTNode(modE
							.get(enzymeNum), this));
				if (specRefR2.equals(specRefR1)) {
					ASTNode r1square = ASTNode.pow(
							new ASTNode(speciesR1, this), 2);
					numerator = ASTNode.times(numerator, r1square);
					denominator = ASTNode.sum(ASTNode.times(this, p_kIr1,
							p_kMr2), ASTNode.times(ASTNode.sum(this, p_kMr1,
							p_kMr2), new ASTNode(speciesR1, this)), r1square);
				} else {
					numerator = ASTNode.times(numerator, new ASTNode(speciesR1,
							this), new ASTNode(speciesR2, this));
					denominator = ASTNode.sum(ASTNode.times(this, p_kIr1,
							p_kMr2), ASTNode.times(this, p_kMr2, speciesR1),
							ASTNode.times(this, p_kMr1, speciesR2), ASTNode
									.times(this, speciesR1, speciesR2));
				}
			} else {
				/*
				 * Reversible reaction: Bi-Bi
				 */
				if (!biuni) {
					StringBuffer kcatp;
					StringBuffer kcatn;

					StringBuffer kMr2 = concat("kM_", reaction.getId());
					StringBuffer kIr1 = concat("ki_", reaction.getId());
					StringBuffer kMp1 = concat("kM_", reaction.getId());
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
							String currEnzyme = modE.get(enzymeNum);
							kcatp = concat(kcatp, underscore, currEnzyme);
							kcatn = concat(kcatn, underscore, currEnzyme);
							kMr2 = concat(kMr2, underscore, currEnzyme);
							kMp1 = concat(kMp1, underscore, currEnzyme);
							kIp1 = concat(kIp1, underscore, currEnzyme);
							kIp2 = concat(kIp2, underscore, currEnzyme);
							kIr2 = concat(kIr2, underscore, currEnzyme);
							kIr1 = concat(kIr1, underscore, currEnzyme);
						}
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
					Parameter p_kcatp = new Parameter(kcatp.toString(),
							getLevel(), getVersion());
					Parameter p_kcatn = new Parameter(kcatn.toString(),
							getLevel(), getVersion());
					Parameter p_kMr2 = new Parameter(kMr2.toString(),
							getLevel(), getVersion());
					Parameter p_kMp1 = new Parameter(kMp1.toString(),
							getLevel(), getVersion());
					Parameter p_kIp1 = new Parameter(kIp1.toString(),
							getLevel(), getVersion());
					Parameter p_kIp2 = new Parameter(kIp2.toString(),
							getLevel(), getVersion());
					Parameter p_kIr1 = new Parameter(kIr1.toString(),
							getLevel(), getVersion());
					Parameter p_kIr2 = new Parameter(kIr2.toString(),
							getLevel(), getVersion());
					addLocalParameters(p_kcatp, p_kcatn, p_kMr2, p_kMp1,
							p_kIp1, p_kIp2, p_kIr1, p_kIr2);

					ASTNode numeratorForward = ASTNode
							.frac(new ASTNode(p_kcatp, this), ASTNode.times(
									this, p_kIr1, p_kMr2));
					ASTNode numeratorReverse = ASTNode
							.frac(new ASTNode(p_kcatn, this), ASTNode.times(
									this, p_kIp2, p_kMp1));
					if (modE.size() > 0) {
						numeratorForward = ASTNode.times(numeratorForward,
								new ASTNode(modE.get(enzymeNum), this));
						numeratorReverse = ASTNode.times(numeratorReverse,
								new ASTNode(modE.get(enzymeNum), this));
					}
					// happens if the reactant has a stoichiometry of two.
					ASTNode r1r2 = specRefR1.equals(specRefR2) ? ASTNode.pow(
							new ASTNode(speciesR1, this), 2) : ASTNode.times(
							this, speciesR1, speciesR2);
					// happens if the product has a stoichiometry of two.
					ASTNode p1p2 = specRefP1.equals(specRefP2) ? ASTNode.pow(
							new ASTNode(speciesP1, this), 2) : ASTNode.times(
							this, speciesP1, speciesP2);
					numeratorForward = ASTNode.times(numeratorForward, r1r2);
					numeratorReverse = ASTNode.times(numeratorReverse, p1p2);
					numerator = ASTNode
							.diff(numeratorForward, numeratorReverse);
					denominator = ASTNode.sum(new ASTNode(1, this), ASTNode
							.frac(this, speciesR1, p_kIr1), ASTNode.frac(this,
							speciesR2, p_kIr2), ASTNode.frac(this, speciesP1,
							p_kIp1), ASTNode.frac(this, speciesP2, p_kIp2),
							ASTNode.frac(p1p2, ASTNode.times(this, p_kIp2,
									p_kMp1)), ASTNode.frac(r1r2, ASTNode.times(
									this, p_kIr1, p_kMr2)));
				} else {
					/*
					 * Reversible reaction: Bi-Uni reaction
					 */
					StringBuffer kcatp;
					StringBuffer kcatn;
					StringBuffer kMr2 = concat("kM_", reaction.getId());
					StringBuffer kMp1 = concat("kM_", reaction.getId());
					StringBuffer kIr2 = concat("ki_", reaction.getId());
					StringBuffer kIr1 = concat("ki_", reaction.getId());

					if (modE.size() == 0) {
						kcatp = concat("Vp_", reaction.getId());
						kcatn = concat("Vn_", reaction.getId());
					} else {
						kcatp = concat("kcatp_", reaction.getId());
						kcatn = concat("kcatn_", reaction.getId());
						if (modE.size() > 1) {
							append(kcatp, underscore, modE.get(enzymeNum));
							append(kcatn, underscore, modE.get(enzymeNum));
							append(kMr2, underscore, modE.get(enzymeNum));
							append(kMp1, underscore, modE.get(enzymeNum));
							append(kIr2, underscore, modE.get(enzymeNum));
							append(kIr1, underscore, modE.get(enzymeNum));
						}
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
					Parameter p_kcatp = new Parameter(kcatp.toString(),
							getLevel(), getVersion());
					Parameter p_kcatn = new Parameter(kcatn.toString(),
							getLevel(), getVersion());
					Parameter p_kMr2 = new Parameter(kMr2.toString(),
							getLevel(), getVersion());
					Parameter p_kMp1 = new Parameter(kMp1.toString(),
							getLevel(), getVersion());
					Parameter p_kIr1 = new Parameter(kIr1.toString(),
							getLevel(), getVersion());
					Parameter p_kIr2 = new Parameter(kIr2.toString(),
							getLevel(), getVersion());
					addLocalParameters(p_kcatp, p_kcatn, p_kMr2, p_kMp1,
							p_kIr1, p_kIr2);

					ASTNode r1r2;
					if (specRefR1.equals(specRefR2))
						r1r2 = ASTNode.pow(new ASTNode(speciesR1, this), 2);
					else
						r1r2 = ASTNode.times(this, speciesR1, speciesR2);
					ASTNode numeratorForward = ASTNode
							.frac(new ASTNode(p_kcatp, this), ASTNode.times(
									this, p_kIr1, p_kMr2));
					ASTNode numeratorReverse = ASTNode.frac(this, p_kcatn,
							p_kMp1);
					if (modE.size() != 0) {
						numeratorForward = ASTNode.times(numeratorForward,
								new ASTNode(modE.get(enzymeNum), this));
						numeratorReverse = ASTNode.times(numeratorReverse,
								new ASTNode(modE.get(enzymeNum), this));
					}
					numeratorForward = ASTNode.times(numeratorForward, r1r2);
					numeratorReverse = ASTNode.times(numeratorReverse,
							new ASTNode(speciesP1, this));
					numerator = ASTNode
							.diff(numeratorForward, numeratorReverse);
					denominator = ASTNode.sum(new ASTNode(1, this), ASTNode
							.frac(this, speciesR1, p_kIr1), ASTNode.frac(this,
							speciesR2, p_kIr2), ASTNode.frac(r1r2, ASTNode
							.times(this, p_kIr1, p_kMr2)), ASTNode.frac(this,
							speciesP1, p_kMp1));
				}
			}
			// Construct formula
			catalysts[enzymeNum++] = ASTNode.frac(numerator, denominator);
		} while (enzymeNum < modE.size());
		return ASTNode.times(activationFactor(modActi),
				inhibitionFactor(modInhib), ASTNode.sum(catalysts));
	}
}
