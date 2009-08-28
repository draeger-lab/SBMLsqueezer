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
import org.sbml.Parameter;
import org.sbml.Reaction;
import org.sbml.SpeciesReference;
import org.sbml.squeezer.io.StringTools;

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
	public RandomOrderMechanism(Reaction parentReaction) throws RateLawNotApplicableException,
			IOException, IllegalFormatException {
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
	 * @see org.sbml.squeezer.kinetics.GeneralizedMassAction#createKineticEquation(org.sbml.Model, java.util.List, java.util.List, java.util.List, java.util.List, java.util.List, java.util.List)
	 */
	// @Override
	ASTNode createKineticEquation(Model model, List<String> modE,
			List<String> modActi, List<String> modTActi, List<String> modInhib,
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
				StringBuffer kMr1 = StringTools.concat("kM_", reaction.getId());
				StringBuffer kMr2 = StringTools.concat("kM_", reaction.getId());
				StringBuffer kIr1 = StringTools.concat("ki_", reaction.getId());

				if (modE.size() == 0)
					kcatp = StringTools.concat("Vp_", reaction.getId());
				else {
					kcatp = StringTools.concat("kcatp_", reaction.getId());
					if (modE.size() > 1) {
						StringTools.append(kcatp, StringTools.underscore, modE
								.get(enzymeNum));
						StringTools.append(kMr2, StringTools.underscore, modE
								.get(enzymeNum));
						StringTools.append(kMr1, StringTools.underscore, modE
								.get(enzymeNum));
						StringTools.append(kIr1, StringTools.underscore, modE
								.get(enzymeNum));
					}
				}
				String speciesR1 = specRefR1.getSpecies();
				String speciesR2 = specRefR2.getSpecies();
				StringTools.append(kMr1, StringTools.underscore, speciesR1);
				StringTools.append(kMr2, StringTools.underscore, speciesR2);
				if (specRefR1.equals(specRefR2)) {
					StringTools.append(kMr1, "kMr1", kMr1.substring(2));
					StringTools.append(kMr2, "kMr2", kMr2.substring(2));
				}
				addLocalParameter(new Parameter(kcatp.toString()));
				addLocalParameter(new Parameter(kMr1.toString()));
				addLocalParameter(new Parameter(kMr2.toString()));
				addLocalParameter(new Parameter(StringTools.append(kIr1,
						StringTools.underscore, speciesR1).toString()));

				numerator = new ASTNode(kcatp, this);
				if (modE.size() > 0)
					numerator = ASTNode.times(numerator, new ASTNode(modE
							.get(enzymeNum), this));
				if (specRefR2.equals(specRefR1)) {
					ASTNode r1square = ASTNode.pow(
							new ASTNode(speciesR1, this), new ASTNode(2, this));
					numerator = ASTNode.times(numerator, r1square);
					denominator = ASTNode.sum(ASTNode.times(new ASTNode(kIr1,
							this), new ASTNode(kMr2, this)),
							ASTNode.times(ASTNode.sum(new ASTNode(kMr1, this),
									new ASTNode(kMr2, this)), new ASTNode(
									speciesR1, this)), r1square);
				} else {
					numerator = ASTNode.times(numerator, new ASTNode(speciesR1,
							this), new ASTNode(speciesR2, this));
					denominator = ASTNode.sum(ASTNode.times(new ASTNode(kIr1,
							this), new ASTNode(kMr2, this)), ASTNode.times(
							new ASTNode(kMr2, this), new ASTNode(speciesR1,
									this)), ASTNode.times(new ASTNode(kMr1,
							this), new ASTNode(speciesR2, this)), ASTNode
							.times(new ASTNode(speciesR1, this), new ASTNode(
									speciesR2, this)));
				}
			} else {
				/*
				 * Reversible reaction: Bi-Bi
				 */
				if (!biuni) {
					StringBuffer kcatp;
					StringBuffer kcatn;

					StringBuffer kMr2 = StringTools.concat("kM_", reaction
							.getId());
					StringBuffer kIr1 = StringTools.concat("ki_", reaction
							.getId());
					StringBuffer kMp1 = StringTools.concat("kM_", reaction
							.getId());
					StringBuffer kIp1 = StringTools.concat("ki_", reaction
							.getId());
					StringBuffer kIp2 = StringTools.concat("ki_", reaction
							.getId());
					StringBuffer kIr2 = StringTools.concat("ki_", reaction
							.getId());

					if (modE.size() == 0) {
						kcatp = StringTools.concat("Vp_", reaction.getId());
						kcatn = StringTools.concat("Vn_", reaction.getId());
					} else {
						kcatp = StringTools.concat("kcatp_", reaction.getId());
						kcatn = StringTools.concat("kcatn_", reaction.getId());
						if (modE.size() > 1) {
							String currEnzyme = modE.get(enzymeNum);
							kcatp = StringTools.concat(kcatp,
									StringTools.underscore, currEnzyme);
							kcatn = StringTools.concat(kcatn,
									StringTools.underscore, currEnzyme);
							kMr2 = StringTools.concat(kMr2,
									StringTools.underscore, currEnzyme);
							kMp1 = StringTools.concat(kMp1,
									StringTools.underscore, currEnzyme);
							kIp1 = StringTools.concat(kIp1,
									StringTools.underscore, currEnzyme);
							kIp2 = StringTools.concat(kIp2,
									StringTools.underscore, currEnzyme);
							kIr2 = StringTools.concat(kIr2,
									StringTools.underscore, currEnzyme);
							kIr1 = StringTools.concat(kIr1,
									StringTools.underscore, currEnzyme);
						}
					}
					String speciesR1 = specRefR1.getSpecies();
					String speciesR2 = specRefR2.getSpecies();
					String speciesP1 = specRefP1.getSpecies();
					String speciesP2 = specRefP2.getSpecies();
					kMr2 = StringTools.concat(kMr2, StringTools.underscore,
							speciesR2);
					kIr1 = StringTools.concat(kIr1, StringTools.underscore,
							speciesR1);
					kIr2 = StringTools.concat(kIr2, StringTools.underscore,
							speciesR2);
					kIp1 = StringTools.concat(kIp1, StringTools.underscore,
							speciesP1);
					kIp2 = StringTools.concat(kIp2, StringTools.underscore,
							speciesP2);
					kMp1 = StringTools.concat(kMp1, StringTools.underscore,
							speciesP1);
					if (specRefR2.equals(specRefR1)) {
						kIr1 = StringTools.concat("kir1", kIr1.substring(2));
						kIr2 = StringTools.concat("kir2", kIr2.substring(2));
					}
					if (specRefP2.equals(specRefP1)) {
						kIp1 = StringTools.concat("kip1", kIp1.substring(2));
						kIp2 = StringTools.concat("kip2", kIp2.substring(2));
					}
					addLocalParameter(new Parameter(kcatp.toString()));
					addLocalParameter(new Parameter(kMr2.toString()));
					addLocalParameter(new Parameter(kMr2.toString()));
					addLocalParameter(new Parameter(kMp1.toString()));
					addLocalParameter(new Parameter(kIp1.toString()));
					addLocalParameter(new Parameter(kIp2.toString()));
					addLocalParameter(new Parameter(kIr2.toString()));
					addLocalParameter(new Parameter(kIr1.toString()));

					ASTNode numeratorForward = ASTNode.frac(new ASTNode(kcatp,
							this), ASTNode.times(new ASTNode(kIr1, this),
							new ASTNode(kMr2, this)));
					ASTNode numeratorReverse = ASTNode.frac(new ASTNode(kcatn,
							this), ASTNode.times(new ASTNode(kIp2, this),
							new ASTNode(kMp1, this)));
					if (modE.size() > 0) {
						numeratorForward = ASTNode.times(numeratorForward,
								new ASTNode(modE.get(enzymeNum), this));
						numeratorReverse = ASTNode.times(numeratorReverse,
								new ASTNode(modE.get(enzymeNum), this));
					}
					// happens if the reactant has a stoichiometry of two.
					ASTNode r1r2 = specRefR1.equals(specRefR2) ? ASTNode.pow(
							new ASTNode(speciesR1, this), new ASTNode(2, this))
							: ASTNode.times(new ASTNode(speciesR1, this),
									new ASTNode(speciesR2, this));
					// happens if the product has a stoichiometry of two.
					ASTNode p1p2 = specRefP1.equals(specRefP2) ? ASTNode.pow(
							new ASTNode(speciesP1, this), new ASTNode(2, this))
							: ASTNode.times(new ASTNode(speciesP1, this),
									new ASTNode(speciesP2, this));
					numeratorForward = ASTNode.times(numeratorForward, r1r2);
					numeratorReverse = ASTNode.times(numeratorReverse, p1p2);
					numerator = ASTNode
							.diff(numeratorForward, numeratorReverse);
					denominator = ASTNode.sum(new ASTNode(1, this), ASTNode
							.frac(new ASTNode(speciesR1, this), new ASTNode(
									kIr1, this)), ASTNode.frac(new ASTNode(
							speciesR2, this), new ASTNode(kIr2, this)), ASTNode
							.frac(new ASTNode(speciesP1, this), new ASTNode(
									kIp1, this)), ASTNode.frac(new ASTNode(
							speciesP2, this), new ASTNode(kIp2, this)), ASTNode
							.frac(p1p2, ASTNode.times(new ASTNode(kIp2, this),
									new ASTNode(kMp1, this))), ASTNode.frac(
							r1r2, ASTNode.times(new ASTNode(kIr1, this),
									new ASTNode(kMr2, this))));
				} else {
					/*
					 * Reversible reaction: Bi-Uni reaction
					 */
					StringBuffer kcatp;
					StringBuffer kcatn;
					StringBuffer kMr2 = StringTools.concat("kM_", reaction
							.getId());
					StringBuffer kMp1 = StringTools.concat("kM_", reaction
							.getId());
					StringBuffer kIr2 = StringTools.concat("ki_", reaction
							.getId());
					StringBuffer kIr1 = StringTools.concat("ki_", reaction
							.getId());

					if (modE.size() == 0) {
						kcatp = StringTools.concat("Vp_", reaction.getId());
						kcatn = StringTools.concat("Vn_", reaction.getId());
					} else {
						kcatp = StringTools.concat("kcatp_", reaction.getId());
						kcatn = StringTools.concat("kcatn_", reaction.getId());
						if (modE.size() > 1) {
							StringTools.append(kcatp, StringTools.underscore,
									modE.get(enzymeNum));
							StringTools.append(kcatn, StringTools.underscore,
									modE.get(enzymeNum));
							StringTools.append(kMr2, StringTools.underscore,
									modE.get(enzymeNum));
							StringTools.append(kMp1, StringTools.underscore,
									modE.get(enzymeNum));
							StringTools.append(kIr2, StringTools.underscore,
									modE.get(enzymeNum));
							StringTools.append(kIr1, StringTools.underscore,
									modE.get(enzymeNum));
						}
					}

					String speciesR1 = specRefR1.getSpecies();
					String speciesR2 = specRefR2.getSpecies();
					String speciesP1 = specRefP1.getSpecies();
					StringTools.append(kMr2, StringTools.underscore, speciesR2);
					StringTools.append(kIr1, StringTools.underscore, speciesR2);
					StringTools.append(kIr2, StringTools.underscore, speciesR2);
					StringTools.append(kMp1, StringTools.underscore, speciesR2);

					if (specRefR2.equals(specRefR1)) {
						StringTools.append(kIr1, "kip1", kIr1.substring(2));
						StringTools.append(kIr2, "kip2", kIr2.substring(2));
					}
					addLocalParameter(new Parameter(kcatp.toString()));
					addLocalParameter(new Parameter(kcatn.toString()));
					addLocalParameter(new Parameter(kMr2.toString()));
					addLocalParameter(new Parameter(kMp1.toString()));
					addLocalParameter(new Parameter(kIr2.toString()));
					addLocalParameter(new Parameter(kIr1.toString()));

					ASTNode r1r2;
					if (specRefR1.equals(specRefR2))
						r1r2 = ASTNode.pow(new ASTNode(speciesR1, this),
								new ASTNode(2, this));
					else
						r1r2 = ASTNode.times(new ASTNode(speciesR1, this),
								new ASTNode(speciesR2, this));
					ASTNode numeratorForward = ASTNode.frac(new ASTNode(kcatp,
							this), ASTNode.times(new ASTNode(kIr1, this),
							new ASTNode(kMr2, this)));
					ASTNode numeratorReverse = ASTNode.frac(new ASTNode(kcatn,
							this), new ASTNode(kMp1, this));
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
							.frac(new ASTNode(speciesR1, this), new ASTNode(
									kIr1, this)), ASTNode.frac(new ASTNode(
							speciesR2, this), new ASTNode(kIr2, this)), ASTNode
							.frac(r1r2, ASTNode.times(new ASTNode(kIr1, this),
									new ASTNode(kMr2, this))), ASTNode.frac(
							new ASTNode(speciesP1, this), new ASTNode(kMp1,
									this)));
				}
			}
			// Construct formula
			catalysts[enzymeNum++] = ASTNode.frac(numerator, denominator);
		} while (enzymeNum < modE.size());
		return ASTNode.times(activationFactor(modActi),
				inhibitionFactor(modInhib), ASTNode.sum(catalysts));
	}
}
