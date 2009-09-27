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

import org.sbml.jsbml.ASTNode;
import org.sbml.jsbml.Parameter;
import org.sbml.jsbml.Reaction;
import org.sbml.jsbml.Species;
import org.sbml.squeezer.RateLawNotApplicableException;

/**
 * TODO: comment missing
 * 
 * @since 1.0
 * @version
 * @author <a href="mailto:Nadine.hassis@gmail.com">Nadine Hassis</a>
 * @author <a href="mailto:andreas.draeger@uni-tuebingen.de">Andreas Dr&auml;ger</a>
 * @date Aug 1, 2007
 */
public class MichaelisMenten extends GeneralizedMassAction implements
		InterfaceUniUniKinetics, InterfaceReversibleKinetics,
		InterfaceIrreversibleKinetics, InterfaceModulatedKinetics {

	private int numOfInhibitors;

	private int numOfActivators;

	private int numOfEnzymes;

	/**
	 * @param parentReaction
	 * @throws RateLawNotApplicableException
	 * @throws IllegalFormatException
	 * @throws IOException
	 */
	public MichaelisMenten(Reaction parentReaction, Object... typeParameters)
			throws RateLawNotApplicableException, IllegalFormatException {
		super(parentReaction, typeParameters);
	}

	ASTNode createKineticEquation(List<String> modE, List<String> modActi,
			List<String> modTActi, List<String> modInhib,
			List<String> modTInhib, List<String> modCat)
			throws RateLawNotApplicableException, IllegalFormatException {
		Reaction reaction = getParentSBMLObject();
		ASTNode numerator;
		ASTNode denominator;

		numOfActivators = modActi.size();
		numOfEnzymes = modE.size();
		numOfInhibitors = modInhib.size();

		if ((reaction.getNumReactants() > 1)
				|| (reaction.getReactant(0).getStoichiometry() != 1d))
			throw new RateLawNotApplicableException(
					"This rate law can only be applied to reactions with exactly one reactant.");
		if (((reaction.getNumProducts() > 1) || (reaction.getProduct(0)
				.getStoichiometry() != 1.0))
				&& reaction.getReversible())
			throw new RateLawNotApplicableException(
					"This rate law can only be applied to reactions with exactly one product.");

		setSBOTerm(269);
		switch (numOfEnzymes) {
		case 0: // no enzyme, irreversible
			if (!getParentSBMLObject().getReversible()
					&& (numOfActivators == 0) && (numOfInhibitors == 0))
				setSBOTerm(199);
			else if ((numOfActivators == 0) && (numOfInhibitors == 0))
				setSBOTerm(326);
			break;
		case 1: // one enzmye
			if (getParentSBMLObject().getReversible()) {
				if ((numOfActivators == 0) && (numOfInhibitors == 0))
					setSBOTerm(326);
			} else if ((numOfActivators == 0) && (numOfInhibitors == 0))
				// irreversible equivalents: Briggs-Haldane equation (31) or
				// Van Slyke-Cullen equation (30)
				// 29 = Henri-Michaelis-Menten
				setSBOTerm(28);
			break;
		}
		if (!getParentSBMLObject().getReversible())
			switch (numOfInhibitors) {
			case 1:
				setSBOTerm(265);
			case 2:
				setSBOTerm(276);
			default:
				setSBOTerm(275);
			}

		Species speciesR = reaction.getReactant(0).getSpeciesInstance();
		Species speciesP = reaction.getProduct(0).getSpeciesInstance();

		ASTNode formula[] = new ASTNode[Math.max(1, modE.size())];
		int enzymeNum = 0;
		do {
			String enzyme = modE.size() == 0 ? null : modE.get(enzymeNum);
			Parameter p_kcatp = parameterKcatOrVmax(reaction.getId(), enzyme,
					true);
			Parameter p_kMr = parameterMichaelisSubstrate(reaction.getId(),
					speciesR.getId(), enzyme);

			ASTNode currEnzymeKin;
			if (!reaction.getReversible()) {
				/*
				 * Irreversible Reaction
				 */
				numerator = ASTNode.times(this, p_kcatp, speciesR);
				denominator = new ASTNode(speciesR, this);
			} else {
				/*
				 * Reversible Reaction
				 */
				numerator = ASTNode.times(ASTNode.frac(this, p_kcatp, p_kMr),
						new ASTNode(speciesR, this));
				denominator = ASTNode.frac(this, speciesR, p_kMr);
				Parameter p_kcatn = parameterKcatOrVmax(reaction.getId(),
						enzyme, false);
				Parameter p_kMp = parameterMichaelisProduct(reaction.getId(),
						speciesP.getId(), enzyme);

				numerator = ASTNode.diff(numerator, ASTNode.times(ASTNode.frac(
						this, p_kcatn, p_kMp), new ASTNode(speciesP, this)));
				denominator = ASTNode.sum(denominator, ASTNode.frac(this,
						speciesP, p_kMp));
			}
			denominator = createInihibitionTerms(modInhib, reaction, modE,
					denominator, p_kMr, enzymeNum);

			if (reaction.getReversible())
				denominator = ASTNode.sum(new ASTNode(1, this), denominator);
			else if (modInhib.size() <= 1)
				denominator = ASTNode
						.sum(new ASTNode(p_kMr, this), denominator);

			// construct formula
			currEnzymeKin = ASTNode.frac(numerator, denominator);
			if (modE.size() > 0)
				currEnzymeKin = ASTNode.times(new ASTNode(modE.get(enzymeNum),
						this), currEnzymeKin);
			formula[enzymeNum++] = currEnzymeKin;
		} while (enzymeNum < modE.size());
		ASTNode sum = ASTNode.sum(formula);

		// the formalism from the convenience kinetics as a default.
		if ((modInhib.size() > 1) && (reaction.getReversible()))
			sum = ASTNode.times(inhibitionFactor(modInhib), sum);
		// Activation
		if (modActi.size() > 0)
			sum = ASTNode.times(activationFactor(modActi), sum);
		return sum;
	}

	/**
	 * Inhibition
	 * 
	 * @param modInhib
	 * @param reaction
	 * @param modE
	 * @param denominator
	 * @param mr
	 * @param currEnzymeKin
	 * @param enzymeNum
	 */
	private ASTNode createInihibitionTerms(List<String> modInhib,
			Reaction reaction, List<String> modE, ASTNode denominator,
			Parameter mr, int enzymeNum) {
		if (modInhib.size() == 1) {
			StringBuffer kIa = concat("KIa_", reaction.getId()), kIb = concat(
					"KIb_", reaction.getId());
			if (modE.size() > 1) {
				append(kIa, underscore, modE.get(enzymeNum));
				append(kIb, underscore, modE.get(enzymeNum));
			}
			Parameter p_kIa = createOrGetParameter(kIa.toString());
			p_kIa.setSBOTerm(261);
			Parameter p_kIb = createOrGetParameter(kIb.toString());
			p_kIb.setSBOTerm(261);

			ASTNode specRefI = new ASTNode(modInhib.get(0), this);
			if (reaction.getReversible())
				denominator = ASTNode.sum(ASTNode.frac(specRefI, new ASTNode(
						p_kIa, this)), ASTNode.times(denominator, ASTNode.sum(
						new ASTNode(1, this), ASTNode.frac(specRefI,
								new ASTNode(p_kIb, this)))));
			else
				denominator = ASTNode.sum(ASTNode.times(ASTNode.frac(
						new ASTNode(mr, this), new ASTNode(p_kIa, this)),
						specRefI), denominator, ASTNode.times(ASTNode.frac(
						new ASTNode(mr, this), new ASTNode(p_kIb, this)),
						specRefI));

		} else if ((modInhib.size() > 1)
				&& !getParentSBMLObject().getReversible()) {
			/*
			 * mixed-type inihibition of irreversible enzymes by mutually
			 * exclusive inhibitors.
			 */
			setSBOTerm(275);
			ASTNode sumIa = new ASTNode(1, this);
			ASTNode sumIb = new ASTNode(1, this);
			for (int i = 0; i < modInhib.size(); i++) {
				StringBuffer kIai = concat(Integer.valueOf(i + 1), underscore,
						reaction.getId());
				if (modE.size() > 1)
					append(kIai, underscore, modE.get(enzymeNum));
				StringBuffer kIbi = concat("kIb_", kIai);
				kIai = concat("kIa_", kIai);
				Parameter p_kIai = createOrGetParameter(kIai.toString());
				p_kIai.setSBOTerm(261);
				Parameter p_kIbi = createOrGetParameter(kIbi.toString());
				p_kIbi.setSBOTerm(261);
				ASTNode specRefI = new ASTNode(modInhib.get(i), this);
				sumIa = ASTNode.sum(sumIa, ASTNode.frac(specRefI, new ASTNode(
						p_kIai, this)));
				sumIb = ASTNode.sum(sumIb, ASTNode.frac(specRefI, new ASTNode(
						p_kIbi, this)));
			}
			denominator = ASTNode.sum(ASTNode.times(denominator, sumIa),
					ASTNode.times(new ASTNode(mr, this), sumIb));
		}
		return denominator;
	}

	public static boolean isApplicable(Reaction reaction) {
		// TODO
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.squeezer.kinetics.GeneralizedMassAction#getSimpleName()
	 */
	public String getSimpleName() {
		return "Michaelis-Menten";
	}
}
