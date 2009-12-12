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
package org.sbmlsqueezer.kinetics;

import java.io.IOException;
import java.util.List;

import jp.sbi.celldesigner.plugin.PluginModel;
import jp.sbi.celldesigner.plugin.PluginReaction;
import jp.sbi.celldesigner.plugin.PluginSpeciesReference;

/**
 * <p>
 * This is the thermodynamically independent form of the convenience kinetics.
 * In cases that the stochiometric matrix has full column rank the less
 * complicated {@link Convenience} can bee invoked.
 * </p>
 * <p>
 * Creates the Convenience kinetic's thermodynamicely independent form. This
 * method in general works the same way as the according one for the convenience
 * kinetic. Each enzyme's fraction is multiplied with a reaction constant that
 * is global for the eynzme's reaction.
 * </p>
 * 
 * @since 1.0
 * @version
 * @author <a href="mailto:Nadine.hassis@gmail.com">Nadine Hassis </a>
 * @author <a href="mailto:andreas.draeger@uni-tuebingen.de">Andreas
 *         Dr&auml;ger</a>
 * @author <a href="mailto:hannes.borch@googlemail.com">Hannes Borch</a>
 * @date Aug 1, 2007
 */
public class ConvenienceIndependent extends Convenience {

	/**
	 * @param parentReaction
	 * @param model
	 * @param reversibility
	 * @throws RateLawNotApplicableException
	 * @throws IOException
	 * @throws IllegalFormatException
	 */
	public ConvenienceIndependent(PluginReaction parentReaction,
			PluginModel model) throws RateLawNotApplicableException,
			IOException, IllegalFormatException {
		super(parentReaction, model);
	}

	/**
	 * @param parentReaction
	 * @param model
	 * @param listOfPossibleEnzymes
	 * @param reversibility
	 * @throws RateLawNotApplicableException
	 * @throws IOException
	 * @throws IllegalFormatException
	 */
	public ConvenienceIndependent(PluginReaction parentReaction,
			PluginModel model, List<String> listOfPossibleEnzymes)
			throws RateLawNotApplicableException, IOException,
			IllegalFormatException {
		super(parentReaction, model, listOfPossibleEnzymes);
	}

	public static boolean isApplicable(PluginReaction reaction) {
		// TODO
		return true;
	}

	// @Override
	public String getName() {
		if (this.getParentReaction().getReversible())
			return "reversible thermodynamically independent convenience kinetics";
		return "irreversible thermodynamically independent convenience kinetics";
	}

	// @Override
	public String getSBO() {
		return "none";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seeorg.sbmlsqueezer.kinetics.Convenience#createKineticEquation(jp.sbi.
	 * celldesigner.plugin.PluginModel, java.util.List, java.util.List,
	 * java.util.List, java.util.List, java.util.List, java.util.List)
	 */
	protected StringBuffer createKineticEquation(PluginModel model,
			List<String> modE, List<String> modActi, List<String> modTActi,
			List<String> modInhib, List<String> modTInhib, List<String> modCat)
			throws RateLawNotApplicableException, IllegalFormatException {
		final boolean FORWARD = true;
		final boolean REVERSE = false;
		PluginReaction reaction = getParentReaction();
		StringBuffer[] enzymes = new StringBuffer[modE.size()];
		try {
			StringBuffer acti = activationFactor(modActi);
			StringBuffer inhib = inhibitionFactor(modInhib);

			int i = 0;
			do {
				StringBuffer klV = concat("kV_", reaction.getId());
				if (modE.size() > 1)
					append(klV, underscore, modE.get(i));
				addLocalParameter(klV);

				StringBuffer numerator, denominator;
				if (!reaction.getReversible()) {
					numerator = times(num(i, modE, FORWARD));
					denominator = diff(times(denominators(i, modE, FORWARD)),
							Integer.toString(1));
				} else {
					numerator = diff(num(i, modE, FORWARD), (num(i, modE,
							REVERSE)));
					denominator = diff(sum(
							times(denominators(i, modE, FORWARD)),
							times(denominators(i, modE, REVERSE))), Integer
							.toString(1));
				}

				if (modE.size() > 0)
					enzymes[i] = times(new StringBuffer(modE.get(i)), klV,
							frac(numerator, denominator));
				else
					return (times(acti, inhib, frac(numerator, denominator)));
				i++;
			} while (i < enzymes.length);
			return times(acti, inhib, sum(enzymes));
		} catch (IllegalFormatException exc) {
			exc.printStackTrace();
			return new StringBuffer();
		}
	}

	/**
	 * Returns an array containing the factors of the reactants and products
	 * included in the convenience kinetic's numerator. Each factor is given by
	 * the quotient of the respective species' concentration and it's related
	 * equilibrium constant to the power of the species' stoichiometry,
	 * multiplied with the product of equilibrium constant and a dimensionless
	 * energy constant to the power of half the species' stoichiometry. This
	 * method is applicable for both forward and backward reactions.
	 * 
	 * @param reaction
	 * @param enzymeNumber
	 * @param modE
	 * @param type
	 *            true means forward, false means reverse.
	 * @return
	 */
	private StringBuffer num(int enzymeNumber, List<String> modE, boolean type) {
		PluginReaction reaction = getParentReaction();

		StringBuffer educts = new StringBuffer();
		StringBuffer products = new StringBuffer();
		StringBuffer eductroot = new StringBuffer();
		StringBuffer productroot = new StringBuffer();
		StringBuffer equation = new StringBuffer();

		for (int i = 0; i < reaction.getNumReactants(); i++) {
			PluginSpeciesReference ref = reaction.getReactant(i);
			StringBuffer kM = concat("kM_", reaction.getId());
			if (modE.size() > 1)
				append(kM, underscore, modE.get(enzymeNumber));
			append(kM, underscore, ref.getSpecies());
			addLocalParameter(kM);
			StringBuffer kiG = concat("kG_", ref.getSpecies());
			addLocalParameter(kiG);
			educts = times(educts, pow(frac(getSpecies(ref), kM), ref
					.getStoichiometry()));
			eductroot = times(eductroot, pow(brackets(times(kiG, kM)), ref
					.getStoichiometry()));
		}

		for (int i = 0; i < reaction.getNumProducts(); i++) {
			PluginSpeciesReference ref = reaction.getProduct(i);
			StringBuffer kM = concat("kM_", reaction.getId());
			if (modE.size() > 1)
				append(kM, underscore, modE.get(enzymeNumber));
			append(kM, underscore, ref.getSpecies());
			addLocalParameter(kM);
			StringBuffer kiG = concat("kG_", ref.getSpecies());
			addLocalParameter(kiG);
			products = times(products, pow(frac(getSpecies(ref), kM), ref
					.getStoichiometry()));
			productroot = times(productroot, pow(brackets(times(kiG, kM)), ref
					.getStoichiometry()));

		}
		equation = type ? times(educts, sqrt(frac(eductroot, productroot)))
				: times(products, sqrt(frac(productroot, eductroot)));
		return equation;
	}

	/**
	 * Returns an array containing the factors of the reactants and products
	 * included in the convenience kinetic's denominator. For each factor, the
	 * respective species' concentration and it's equilibrium constant are
	 * divided and raised to the power of each integer value between zero and
	 * the species' stoichiometry. All of the species' powers are summed up to
	 * form the species' factor in the product. The method is applicable for
	 * both forward and backward reactions.
	 * 
	 * @param reaction
	 * @param enzymeNumber
	 * @param modE
	 * @param type
	 *            true means forward, false backward.
	 * @return
	 */
	private StringBuffer[] denominators(int enzymeNumber, List<String> modE,
			boolean type) {
		// type FORWARD = true;
		PluginReaction reaction = getParentReaction();
		StringBuffer[] denoms = new StringBuffer[type ? reaction
				.getNumReactants() : reaction.getNumProducts()];
		for (int i = 0; i < denoms.length; i++) {
			PluginSpeciesReference ref = type ? reaction.getReactant(i)
					: reaction.getProduct(i);
			StringBuffer kM = concat("kM_", getParentReactionID());
			if (modE.size() > 1)
				append(kM, underscore, modE.get(enzymeNumber));
			append(kM, underscore, ref.getSpecies());

			StringBuffer[] parts = new StringBuffer[(int) ref
					.getStoichiometry() + 1];
			StringBuffer part = brackets(sum(Integer.toString(1), frac(
					getSpecies(ref), kM)));
			for (int j = 0; j < parts.length; j++)
				parts[j] = pow(part, Integer.toString(j));
			denoms[i] = sum(parts);
		}
		return denoms;
	}
}
