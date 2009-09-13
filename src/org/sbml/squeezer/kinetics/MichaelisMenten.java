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
 * @author <a href="mailto:andreas.draeger@uni-tuebingen.de">Andreas
 *         Dr&auml;ger</a>
 * @date Aug 1, 2007
 */
public class MichaelisMenten extends GeneralizedMassAction {

	private int numOfInhibitors;

	private int numOfActivators;

	private int numOfEnzymes;

	/**
	 * @param parentReaction
	 * @throws RateLawNotApplicableException
	 * @throws IllegalFormatException
	 * @throws IOException
	 */
	public MichaelisMenten(Reaction parentReaction)
			throws RateLawNotApplicableException, IOException,
			IllegalFormatException {
		super(parentReaction);
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

		Species specRefR = reaction.getReactant(0).getSpeciesInstance();
		Species specRefP = reaction.getProduct(0).getSpeciesInstance();

		ASTNode formula[] = new ASTNode[Math.max(1, modE.size())];
		int enzymeNum = 0;
		do {
			StringBuffer kcatp, kcatn;
			StringBuffer kMr = concat("kM_", reaction.getId());
			StringBuffer kMp = concat("kM_", reaction.getId());

			if (modE.size() == 0) {
				kcatp = concat("Vp_", reaction.getId());
				kcatn = concat("Vn_", reaction.getId());
			} else {
				kcatp = concat("kcatp_", reaction.getId());
				kcatn = concat("kcatn_", reaction.getId());
				if (modE.size() > 1) {
					append(kcatp, underscore, modE.get(enzymeNum));
					append(kcatn, underscore, modE.get(enzymeNum));
					append(kMr, underscore, modE.get(enzymeNum));
					append(kMp, underscore, modE.get(enzymeNum));
				}
			}
			append(kMr, underscore, specRefR);
			Parameter p_kcatp = createOrGetParameter(kcatp.toString());
			Parameter p_kMr = createOrGetParameter(kMr.toString());

			ASTNode currEnzymeKin;
			if (!reaction.getReversible()) {
				/*
				 * Irreversible Reaction
				 */
				numerator = ASTNode.times(this, p_kcatp, specRefR);
				denominator = new ASTNode(specRefR, this);
			} else {
				/*
				 * Reversible Reaction
				 */
				numerator = ASTNode.times(ASTNode.frac(this, p_kcatp, p_kMr),
						new ASTNode(specRefR, this));
				denominator = ASTNode.frac(this, specRefR, p_kMr);
				append(kMp, underscore, specRefP);
				Parameter p_kcatn = createOrGetParameter(kcatn.toString());
				Parameter p_kMp = createOrGetParameter(kMp.toString());

				numerator = ASTNode.diff(numerator, ASTNode.times(ASTNode.frac(
						this, p_kcatn, p_kMp), new ASTNode(specRefP, this)));
				denominator = ASTNode.sum(denominator, ASTNode.frac(this,
						specRefP, p_kMp));
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
			Parameter p_kIb = createOrGetParameter(kIb.toString());

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
				Parameter p_kIbi = createOrGetParameter(kIbi.toString());
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

	public String getName() {
		switch (numOfEnzymes) {
		case 0: // no enzyme, irreversible
			if (!getParentSBMLObject().getReversible()
					&& (numOfActivators == 0) && (numOfInhibitors == 0))
				return "normalised kinetics of unireactant enzymes"; // 0000199
			else if ((numOfActivators == 0) && (numOfInhibitors == 0))
				return "kinetics of non-modulated unireactant enzymes"; // 0000326
			break;
		case 1: // one enzmye
			if (getParentSBMLObject().getReversible()) {
				if ((numOfActivators == 0) && (numOfInhibitors == 0))
					return "kinetics of non-modulated unireactant enzymes";
			} else if ((numOfActivators == 0) && (numOfInhibitors == 0)) // irreversible
				// equivalents: Briggs-Haldane equation or Van
				// Slyke-Cullen
				// equation
				return "Henri-Michaelis Menten equation"; // 0000029
			break;
		}
		if (!getParentSBMLObject().getReversible())
			switch (numOfInhibitors) {
			case 1:
				return "simple mixed-type inhibition of irreversible unireactant enzymes"; // 0000265
			case 2:
				return "mixed-type inhibition of irreversible unireactant enzymes by two inhibitors"; // 0000276
			default:
				return "mixed-type inhibition of irreversible enzymes by mutually exclusive inhibitors"; // 0000275
			}
		return "kinetics of unireactant enzymes"; // 0000269
	}

	public static boolean isApplicable(Reaction reaction) {
		// TODO
		return true;
	}

	public String getSBO() {
		String name = getName().toLowerCase(), sbo = "none";
		if (name.equals("normalised kinetics of unireactant enzymes"))
			sbo = "0000199";
		else if (name.equals("kinetics of non-modulated unireactant enzymes"))
			sbo = "0000326";
		else if (name.equals("Briggs-Haldane equation"))
			sbo = "0000031";
		else if (name
				.equals("kinetics of irreversible non-modulated unireactant enzymes"))
			sbo = "0000028";
		else if (name
				.equals("simple mixed-type inhibition of irreversible unireactant enzymes"))
			sbo = "0000265";
		else if (name.equals("kinetics of unireactant enzymes"))
			sbo = "0000269";
		else if (name.equals("Henri-Michaelis Menten equation"))
			sbo = "0000029";
		else if (name.equals("Van Slyke-Cullen equation"))
			sbo = "0000030";
		else if (name
				.equals("simple uncompetitive inhibition of irreversible unireactant enzymes"))
			sbo = "0000262";
		else if (name
				.equals("mixed-type inhibition of irreversible enzymes by mutually exclusive inhibitors"))
			sbo = "0000275";
		else if (name
				.equals("simple non-competitive inhibition of unireactant enzymes"))
			sbo = "0000266";
		else if (name
				.equals("mixed-type inhibition of irreversible unireactant enzymes by two inhibitors"))
			sbo = "0000276";
		else if (name
				.equals("mixed-type inhibition of unireactactant enzymes by two inhibitors"))
			sbo = "0000277";
		else if (name
				.equals("simple competitive inhibition of irreversible unireactant enzymes by two non-exclusive inhibitors"))
			sbo = "0000274";
		else if (name
				.equals("competitive inhibition of irreversible unireactant enzymes by two exclusive inhibitors"))
			sbo = "0000271";
		else if (name
				.equals("competitive inhibition of irreversible unireactant enzymes by exclusive inhibitors"))
			sbo = "0000270";
		else if (name
				.equals("simple competitive inhibition of irreversible unireactant enzymes by one inhibitor"))
			sbo = "0000260";
		return sbo;
	}
}
