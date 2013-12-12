/*
 * $Id$
 * $URL$
 * ---------------------------------------------------------------------
 * This file is part of SBMLsqueezer, a Java program that creates rate
 * equations for reactions in SBML files (http://sbml.org).
 *
 * Copyright (C) 2006-2013 by the University of Tuebingen, Germany.
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

import java.util.List;
import java.util.ResourceBundle;

import org.sbml.jsbml.ASTNode;
import org.sbml.jsbml.KineticLaw;
import org.sbml.jsbml.LocalParameter;
import org.sbml.jsbml.Reaction;
import org.sbml.jsbml.SBO;
import org.sbml.jsbml.Unit;
import org.sbml.squeezer.RateLawNotApplicableException;
import org.sbml.squeezer.util.Bundles;

import de.zbit.sbml.util.SBMLtools;
import de.zbit.util.ResourceManager;

/**
 * Irreversible non-exclusive non-cooperative competitive inihibition, a special
 * case of the {@link KineticLaw}s defined by {@link SBO} term identifier 199,
 * 29, 267, or 273 depending on the structure of the reaction.
 * 
 * @author Andreas Dr&auml;ger
 * @author Sarah R. M&uuml;ller vom Hagen
 * @since 1.0
 * @version $Rev$
 */
public class IrrevCompetNonCooperativeEnzymes extends GeneralizedMassAction
		implements InterfaceIrreversibleKinetics, InterfaceModulatedKinetics,
		InterfaceUniUniKinetics {
	
	public static final transient ResourceBundle MESSAGES = ResourceManager.getBundle(Bundles.MESSAGES);
	public static final transient ResourceBundle WARNINGS = ResourceManager.getBundle(Bundles.WARNINGS);

	/**
	 * Generated serial version identifier.
	 */
	private static final long serialVersionUID = -7128200927307678571L;

	/**
	 * 
	 * @param parentReaction
	 * @param typeParameters
	 * @throws RateLawNotApplicableException
	 */
	public IrrevCompetNonCooperativeEnzymes(Reaction parentReaction,
			Object... typeParameters) throws RateLawNotApplicableException {
		super(parentReaction, typeParameters);
	}

	/* (non-Javadoc)
	 * @see org.sbml.squeezer.kinetics.BasicKineticLaw#createKineticEquation(java.util.List, java.util.List, java.util.List, java.util.List)
	 */
	@Override
  ASTNode createKineticEquation(List<String> modE, List<String> modActi,
			List<String> modInhib, List<String> modCat)
			throws RateLawNotApplicableException {
		if (modCat.size() > 0) {
      throw new RateLawNotApplicableException(WARNINGS.getString("RATE_LAW_CAN_ONLY_APPLIED_TO_ENZYME_CATALYZED_REACTIONS"));
    }
		Reaction reaction = getParentSBMLObject();
		if ((reaction.getReactantCount() > 1)
				|| (reaction.getReactant(0).getStoichiometry() != 1d)) {
      throw new RateLawNotApplicableException(WARNINGS.getString("RATE_LAW_CAN_ONLY_APPLIED_TO_REACTIONS_WITH_EXACTLY_ONE_SUBSTRATE_SPECIES"));
    }
		if (reaction.getReversible()) {
			reaction.setReversible(false);
		}

		switch (modInhib.size()) {
		case 0:
			SBMLtools.setSBOTerm(this, modE.size() == 0 ? 199 : 29);
		case 1:
			SBMLtools.setSBOTerm(this, 267);
		default:
			SBMLtools.setSBOTerm(this, 273);
		}

		ASTNode[] formula = new ASTNode[Math.max(1, modE.size())];

		int enzymeNum = 0;
		do {
			ASTNode currEnzyme;
			ASTNode numerator;

			String enzyme = modE.isEmpty() ? null : modE.get(enzymeNum);
			numerator = new ASTNode(parameterFactory.parameterKcatOrVmax(
					enzyme, true), this);
			if (!modE.isEmpty()) {
				numerator.multiplyWith(speciesTerm(enzyme));
			}
			numerator = ASTNode.times(numerator, speciesTerm(reaction
					.getReactant(0)));

			ASTNode denominator;
			LocalParameter p_kM = parameterFactory.parameterMichaelis(reaction
					.getReactant(0).getSpecies(), enzyme, true);

			if (modInhib.size() == 0) {
				denominator = new ASTNode(p_kM, this);
			} else {
				ASTNode factor = new ASTNode(p_kM, this);
				for (int i = 0; i < modInhib.size(); i++) {
					LocalParameter p_kIi = parameterFactory.parameterKi(modInhib.get(i), enzyme);
					LocalParameter p_exp = parameterFactory.parameterNumBindingSites(enzyme, modInhib.get(i));

					// one must have the same unit as frac: speciesTerm (SubstancePerSizeOrSubstance)
					// divided by p_kIi (SubstancePerSizeOrSubstance) = dimensionless
					ASTNode one = new ASTNode(1, this);
					SBMLtools.setUnits(one, Unit.Kind.DIMENSIONLESS);
					
					ASTNode frac = ASTNode.frac(
							speciesTerm(modInhib.get(i)),
							new ASTNode(p_kIi, this));
					
					factor.multiplyWith(
						ASTNode.pow(
							ASTNode.sum(
								one,
								frac
							),
							new ASTNode(p_exp, this)
						)
					);
				}
				denominator = factor;
			}
			denominator.plus(speciesTerm(reaction.getReactant(0)));
			currEnzyme = ASTNode.frac(numerator, denominator);
			formula[enzymeNum++] = currEnzyme;
		} while (enzymeNum < modE.size());
		return ASTNode.times(activationFactor(modActi), ASTNode.sum(formula));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.squeezer.kinetics.GeneralizedMassAction#getSimpleName()
	 */
	@Override
  public String getSimpleName() {
		return MESSAGES.getString("IRREV_COMPET_NON_COOP_ENZYMES_SIMPLE_NAME");
	}
}
