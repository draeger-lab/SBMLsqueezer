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
		StringBuffer[] enzymes = new StringBuffer[Math.max(modE.size(), 1)];
		try {
			int i = 0;
			do {
				StringBuffer klV = concat("kV_", reaction.getId());
				String enzyme = modE.size() > 0 ? modE.get(i) : null;
				if (enzyme != null) {
					append(klV, underscore, modE.get(i));
					enzymes[i] = new StringBuffer(enzyme);
				} else
					enzymes[i] = new StringBuffer();
				addLocalParameter(klV);

				StringBuffer numerator, denominator;
				if (!reaction.getReversible()) {
					numerator = times(numeratorElements(enzyme, FORWARD));
					denominator = times(denominatorElements(enzyme, FORWARD));
				} else {
					numerator = diff(numeratorElements(enzyme, FORWARD), numeratorElements(enzyme, REVERSE));
					denominator = sum(times(denominatorElements(enzyme, FORWARD)),
							times(denominatorElements(enzyme, REVERSE)));
					if (reaction.getNumProducts() > 1
							&& reaction.getNumReactants() > 1)
						denominator = diff(denominator, Integer.valueOf(1));
				}
				enzymes[i] = times(enzymes[i], klV,
						frac(numerator, denominator));
				i++;
			} while (i < enzymes.length);
			return times(activationFactor(modActi), inhibitionFactor(modInhib),
					sum(enzymes));
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
	 * @param enzyme
	 * @param type
	 *            true means forward, false means reverse.
	 * @return
	 */
	private StringBuffer numeratorElements(String enzyme, boolean type) {
		PluginReaction reaction = getParentReaction();

		StringBuffer educts = new StringBuffer();
		StringBuffer products = new StringBuffer();
		StringBuffer eductroot = new StringBuffer();
		StringBuffer productroot = new StringBuffer();
		StringBuffer equation = new StringBuffer();
		StringBuffer kiG;

		for (int i = 0; i < reaction.getNumReactants(); i++) {
			PluginSpeciesReference ref = reaction.getReactant(i);
			StringBuffer kM = concat("kM_", reaction.getId());
			if (enzyme != null)
				append(kM, underscore, enzyme);
			append(kM, underscore, ref.getSpecies());
			addLocalParameter(kM);
			kiG = concat("kG_", ref.getSpecies());
			addLocalParameter(kiG);
			educts = times(educts, pow(frac(getSpecies(ref), kM), ref
					.getStoichiometry()));
			eductroot = times(eductroot, pow(brackets(times(kiG, kM)), ref
					.getStoichiometry()));
		}
		for (int i = 0; i < reaction.getNumProducts(); i++) {
			PluginSpeciesReference ref = reaction.getProduct(i);
			StringBuffer kM = concat("kM_", reaction.getId());
			if (enzyme != null)
				append(kM, underscore, enzyme);
			append(kM, underscore, ref.getSpecies());
			addLocalParameter(kM);
			kiG = concat("kG_", ref.getSpecies());
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
	 * @param type
	 *            true means forward, false backward.
	 * @return
	 */
	private StringBuffer[] denominatorElements(String enzyme, boolean type) {
		PluginReaction reaction = getParentReaction();
		StringBuffer[] denoms = new StringBuffer[type ? reaction
				.getNumReactants() : reaction.getNumProducts()];
		boolean noOne = (denoms.length == 1)
				&& (!type || (type && reaction.getReversible() && reaction
						.getNumProducts() > 1));
		for (int i = 0; i < denoms.length; i++) {
			PluginSpeciesReference ref = type ? reaction.getReactant(i)
					: reaction.getProduct(i);
			StringBuffer kM = concat("kM_", getParentReactionID());
			if (enzyme != null)
				append(kM, underscore, enzyme);
			append(kM, underscore, ref.getSpecies());

			StringBuffer[] parts = new StringBuffer[(int) ref
					.getStoichiometry()
					+ (noOne ? 0 : 1)];
			StringBuffer part = frac(getSpecies(ref), kM);
			for (int j = 0; j < parts.length; j++)
				parts[j] = pow(part, Integer.toString(noOne ? j + 1 : j));
			denoms[i] = sum(parts);
		}
		return denoms;
	}
}
