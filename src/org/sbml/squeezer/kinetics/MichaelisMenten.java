/*
 * $Id$
 * $URL$
 * ---------------------------------------------------------------------
 * This file is part of SBMLsqueezer, a Java program that creates rate 
 * equations for reactions in SBML files (http://sbml.org).
 *
 * Copyright (C) 2006-2012 by the University of Tuebingen, Germany.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 * ---------------------------------------------------------------------
 */
package org.sbml.squeezer.kinetics;

import java.text.MessageFormat;
import java.util.List;
import java.util.ResourceBundle;

import org.sbml.jsbml.ASTNode;
import org.sbml.jsbml.LocalParameter;
import org.sbml.jsbml.Reaction;
import org.sbml.jsbml.SBO;
import org.sbml.jsbml.Species;
import org.sbml.jsbml.Unit;
import org.sbml.squeezer.RateLawNotApplicableException;
import org.sbml.squeezer.util.Bundles;

import de.zbit.sbml.util.SBMLtools;
import de.zbit.util.ResourceManager;

/**
 * This is the well-known (Henry-) Michaelis-Menten equation and can be found in
 * the {@link SBO} under one of the following terms (depending on the specific
 * structure of the underlying {@link Reaction}): <a
 * href="http://identifiers.org/biomodels.sbo/SBO:0000028">28</a>, <a
 * href="http://identifiers.org/biomodels.sbo/SBO:0000199">199</a>, <a
 * href="http://identifiers.org/biomodels.sbo/SBO:0000266">266</a>, <a
 * href="http://identifiers.org/biomodels.sbo/SBO:0000269">269</a>, <a
 * href="http://identifiers.org/biomodels.sbo/SBO:0000275">275</a>, <a
 * href="http://identifiers.org/biomodels.sbo/SBO:0000276">276</a>, or <a
 * href="http://identifiers.org/biomodels.sbo/SBO:0000326">326</a>.
 * 
 * @author Nadine Hassis
 * @author Andreas Dr&auml;ger
 * @author Sarah R. M&uuml;ller vom Hagen
 * @date Aug 1, 2007
 * @since 1.0
 * @version $Rev$
 */
public class MichaelisMenten extends GeneralizedMassAction implements
		InterfaceUniUniKinetics, InterfaceReversibleKinetics,
		InterfaceIrreversibleKinetics, InterfaceModulatedKinetics {
	
	public static final transient ResourceBundle MESSAGES = ResourceManager.getBundle(Bundles.MESSAGES);
	public static final transient ResourceBundle WARNINGS = ResourceManager.getBundle(Bundles.WARNINGS);

	/**
	 * Generated serial version identifier.
	 */
	private static final long serialVersionUID = 1773589374066138555L;

	/**
	 * 
	 * @param parentReaction
	 * @param typeParameters
	 * @throws RateLawNotApplicableException
	 */
	public MichaelisMenten(Reaction parentReaction, Object... typeParameters)
			throws RateLawNotApplicableException {
		super(parentReaction, typeParameters);
	}

	/* (non-Javadoc)
	 * @see org.sbml.squeezer.kinetics.BasicKineticLaw#createKineticEquation(java.util.List, java.util.List, java.util.List, java.util.List)
	 */
	@Override
	ASTNode createKineticEquation(List<String> modE, List<String> modActi,
			List<String> modInhib, List<String> modCat)
			throws RateLawNotApplicableException {
		Reaction reaction = getParentSBMLObject();
		ASTNode numerator;
		ASTNode denominator;

		if ((reaction.getReactantCount() > 1)
				|| (reaction.getReactant(0).getStoichiometry() != 1d)) { 
			throw new RateLawNotApplicableException(
				MessageFormat.format(WARNINGS
					.getString("INCORRECT_STOICHIOMETRY_OF_REACTANT_SPECIES"),
					getSimpleName(), reaction.getId())); 
		}
		if (((reaction.getProductCount() != 1) || (reaction.getProduct(0)
				.getStoichiometry() != 1d)) && reaction.getReversible()) {
			throw new RateLawNotApplicableException(
				MessageFormat.format(WARNINGS
					.getString("INCORRECT_STOICHIOMETRY_OF_PRODUCT_SPECIES"),
					getSimpleName(), reaction.getId()));
		}
		SBMLtools.setSBOTerm(this,269); // enzymatic rate law for unireactant enzymes
		switch (modE.size()) {
			case 0: // no enzyme, irreversible
				if (!getParentSBMLObject().getReversible() && (modActi.size() == 0)
						&& (modInhib.size() == 0)) {
					SBMLtools.setSBOTerm(this, 199); // normalised enzymatic rate law for
					// unireactant enzymes
				} else if ((modActi.size() == 0) && (modInhib.size() == 0)) {
					SBMLtools.setSBOTerm(this, 326); // enzymatic rate law for non-modulated
				}
				// unireactant enzymes
				break;
			case 1: // one enzmye
				if (getParentSBMLObject().getReversible()) {
					if ((modActi.size() == 0) && (modInhib.size() == 0)) {
						SBMLtools.setSBOTerm(this, 326); // enzymatic rate law for non-modulated
						// unireactant enzymes
					}
				} else if ((modActi.size() == 0) && (modInhib.size() == 0)) {
					// irreversible equivalents: Briggs-Haldane equation (31) or
					// Van Slyke-Cullen equation (30)
					// 29 = Henri-Michaelis-Menten
					SBMLtools.setSBOTerm(this, 28); // enzymatic rate law for irreversible
					// non-modulated non-interacting unireactant
					// enzymes
				}
				break;
		}
		if (!getParentSBMLObject().getReversible()) {
			switch (modInhib.size()) {
			case 1:
				SBMLtools.setSBOTerm(this, 265); // enzymatic rate law for simple mixed-type
				// inhibition of irreversible unireactant
				// enzymes
				break;
			case 2:
				SBMLtools.setSBOTerm(this, 276); // enzymatic rate law for mixed-type inhibition
				// of irreversible unireactant enzymes by
				// two inhibitors
				break;
			default:
				SBMLtools.setSBOTerm(this, 275); // enzymatic rate law for mixed-type inhibition
				// of irreversible enzymes by mutually
				// exclusive inhibitors
				break;
			}
		}
		Species speciesR = reaction.getReactant(0).getSpeciesInstance();

		ASTNode formula[] = new ASTNode[Math.max(1, modE.size())];
		int enzymeNum = 0;
		do {
			String enzyme = modE.size() == 0 ? null : modE.get(enzymeNum);
			LocalParameter p_kcatp = parameterFactory.parameterKcatOrVmax(enzyme, true);
			LocalParameter p_kMr = parameterFactory.parameterMichaelis(speciesR.getId(), enzyme, true);

			ASTNode currEnzymeKin;
			if (!reaction.getReversible() || (reaction.getProductCount() == 0)) {
				/*
				 * Irreversible Reaction
				 */
				numerator = ASTNode.times(new ASTNode(p_kcatp, this), speciesTerm(speciesR));
				denominator = speciesTerm(speciesR);
			} else {
				/*
				 * Reversible Reaction
				 */
				Species speciesP = reaction.getProduct(0).getSpeciesInstance();

				numerator = ASTNode.times(ASTNode.frac(this, p_kcatp, p_kMr),
						speciesTerm(speciesR));
				denominator = ASTNode.frac(speciesTerm(speciesR), new ASTNode(p_kMr, this));
				LocalParameter p_kcatn = parameterFactory.parameterKcatOrVmax(enzyme, false);
				LocalParameter p_kMp = parameterFactory.parameterMichaelis(speciesP.getId(), enzyme, false);

				numerator.minus(ASTNode.times(ASTNode
						.frac(this, p_kcatn, p_kMp), speciesTerm(speciesP)));
				denominator.plus(ASTNode.frac(speciesTerm(speciesP),
						new ASTNode(p_kMp, this)));
			}
			denominator = createInihibitionTerms(modInhib, reaction, denominator,
				p_kMr, modE.size() > 1 ? modE.get(enzymeNum) : null);

			if (reaction.getReversible()) {
				ASTNode one = new ASTNode(1, this);
				SBMLtools.setUnits(one, p_kMr.getUnits());
				denominator = ASTNode.sum(one, denominator);
			} else if (modInhib.size() < 1) {
				denominator = ASTNode.sum(new ASTNode(p_kMr, this), denominator);
			}

			// construct formula
			currEnzymeKin = ASTNode.frac(numerator, denominator);
			if (modE.size() > 0) {
				currEnzymeKin = ASTNode.times(speciesTerm(modE.get(enzymeNum)),
					currEnzymeKin);
			}
			formula[enzymeNum++] = currEnzymeKin;
		} while (enzymeNum < modE.size());
		ASTNode sum = ASTNode.sum(formula);

		// the formalism from the convenience kinetics as a default.
		if ((modInhib.size() > 1) && (reaction.getReversible())) {
			sum = ASTNode.times(inhibitionFactor(modInhib), sum);
		}
		// Activation
		if (modActi.size() > 0) {
			sum = ASTNode.times(activationFactor(modActi), sum);
		}
		return sum;
	}

	/**
	 * * Inhibition
	 * 
	 * @param modInhib
	 *            A list containing the ids of all inhibitors of this reaction
	 * @param reaction
	 *            The reaction for which a kinetic equation is to be created
	 * @param modE
	 *            A list containing the ids of all enzymes of this reaction
	 * @param denominator
	 *            The denominator of this kinetic equation created so far.
	 * @param mr
	 *            Michaelis constant of the substrate
	 * @param enzymeNum
	 *            Current index of the enzyme in the list of enzyme ids
	 * @return
	 */
	private ASTNode createInihibitionTerms(List<String> modInhib,
			Reaction reaction, ASTNode denominator, LocalParameter mr,
			String enzyme) {
		if (modInhib.size() == 1) {
			LocalParameter p_kIa = parameterFactory.parameterKi(modInhib.get(0), enzyme, 1);
			LocalParameter p_kIb = parameterFactory.parameterKi(modInhib.get(0), enzyme, 2);
			ASTNode one = new ASTNode(1, this);
			SBMLtools.setUnits(one, Unit.Kind.DIMENSIONLESS);
			if (reaction.getReversible()) {
				denominator = ASTNode.sum(
					ASTNode.frac(
						speciesTerm(modInhib.get(0)),
						new ASTNode(p_kIa, this)
					), 
					ASTNode.times(
						denominator,
						ASTNode.sum(
							one,
							ASTNode.frac(
								speciesTerm(modInhib.get(0)),
								new ASTNode(p_kIb, this)
							)
						)
					)
				);
			} else {
				denominator = ASTNode.sum(
					ASTNode.times(
						new ASTNode(mr, this),
						ASTNode.sum(
							one,
							ASTNode.frac(
								speciesTerm(modInhib.get(0)),
								new ASTNode(p_kIa, this)
							)
						)
					),
					ASTNode.times(
						denominator,
						ASTNode.sum(
							one.clone(),
							ASTNode.frac(
								speciesTerm(modInhib.get(0)),
								new ASTNode(p_kIb, this)
							)
						)
					)
				);
			}

		} else if ((modInhib.size() > 1)
				&& !getParentSBMLObject().getReversible()) {
			/*
			 * mixed-type inihibition of irreversible enzymes by mutually
			 * exclusive inhibitors.
			 */
			ASTNode sumIa = new ASTNode(1, this);
			ASTNode sumIb = new ASTNode(1, this);
			SBMLtools.setUnits(sumIa, Unit.Kind.DIMENSIONLESS);
			SBMLtools.setUnits(sumIb, Unit.Kind.DIMENSIONLESS);
			for (int i = 0; i < modInhib.size(); i++) {
				String inhibitor = modInhib.get(i);
				LocalParameter p_kIai = parameterFactory.parameterKi(inhibitor, enzyme, 1);
				LocalParameter p_kIbi = parameterFactory.parameterKi(inhibitor, enzyme, 2);
				LocalParameter p_a = parameterFactory.parameterCooperativeInhibitorSubstrateCoefficient(
					'a', inhibitor, enzyme);
				LocalParameter p_b = parameterFactory.parameterCooperativeInhibitorSubstrateCoefficient(
					'b', inhibitor, enzyme);
				ASTNode specRefI = speciesTerm(inhibitor);
				sumIa = ASTNode.sum(
					sumIa,
					ASTNode.frac(
						specRefI,
						ASTNode.times(this, p_a, p_kIai)
					)
				);
				sumIb = ASTNode.sum(
					sumIb,
					ASTNode.frac(
						specRefI,
						ASTNode.times(this, p_b, p_kIbi)
					)
				);
			}
			denominator = ASTNode.sum(
				ASTNode.times(denominator, sumIa),
				ASTNode.times(new ASTNode(mr, this),
				sumIb)
			);
		}
		return denominator;
	}

	/* (non-Javadoc)
	 * @see org.sbml.squeezer.kinetics.GeneralizedMassAction#getSimpleName()
	 */
	@Override
	public String getSimpleName() {
		return MESSAGES.getString("MICHAELIS_MENTEN_SIMPLE_NAME");
	}

}
