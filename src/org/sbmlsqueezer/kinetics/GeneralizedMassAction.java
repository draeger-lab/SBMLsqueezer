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
import java.util.Vector;

import jp.sbi.celldesigner.plugin.PluginModel;
import jp.sbi.celldesigner.plugin.PluginReaction;
import jp.sbi.celldesigner.plugin.PluginSpeciesReference;

import org.sbmlsqueezer.io.SBOParser;

/**
 * This class creates rate equations according to the generalized mass action
 * rate law. For details see Heinrich and Schuster,
 * "The regulation of Cellluar Systems", pp. 14-17, 1996.
 * 
 * @since 1.0
 * @version
 * @author <a href="mailto:Nadine.hassis@gmail.com">Nadine Hassis</a>
 * @author <a href="mailto:andreas.draeger@uni-tuebingen.de">Andreas
 *         Dr&auml;ger</a>
 * @author <a href="mailto:hannes.borch@googlemail.com">Hannes Borch</a>
 * @date Aug 1, 2007
 */
public class GeneralizedMassAction extends BasicKineticLaw {

	protected double reactantOrder;

	protected double productOrder;

	private String sbo = null;

	/**
	 * 
	 * @param parentReaction
	 * @param model
	 * @throws RateLawNotApplicableException
	 * @throws IOException
	 * @throws IllegalFormatException
	 */
	public GeneralizedMassAction(PluginReaction parentReaction,
			PluginModel model) throws RateLawNotApplicableException,
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
	public GeneralizedMassAction(PluginReaction parentReaction,
			PluginModel model, List<String> listOfPossibleEnzymes)
			throws RateLawNotApplicableException, IOException,
			IllegalFormatException {
		super(parentReaction, model, listOfPossibleEnzymes);
	}

	public static boolean isApplicable(PluginReaction reaction) {
		// TODO
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbmlsqueezer.kinetics.BasicKineticLaw#getName()
	 */
	public String getName() {
		String name = "", orderF = "", orderR = "", participants = "";
		if (getParentReaction().getReversible()) {
			if (reactantOrder == 0)
				orderF = "zeroth order forward";
			else
				switch (getParentReaction().getNumReactants()) {
				case 1:
					if (getParentReaction().getReactant(0).getStoichiometry() == 1.0)
						orderF = "first order forward";
					else if (getParentReaction().getReactant(0)
							.getStoichiometry() == 2.0) {
						orderF = "second order forward";
						participants = "one reactant, ";
					} else if (getParentReaction().getReactant(0)
							.getStoichiometry() == 3.0) {
						orderF = "third order forward";
						participants = "one reactant, ";
					}
					break;
				case 2:
					if ((getParentReaction().getReactant(0).getStoichiometry() == 1.0)
							&& (getParentReaction().getReactant(1)
									.getStoichiometry() == 1.0)) {
						orderF = "second order forward";
						participants = "two reactants, ";
					} else if (((getParentReaction().getReactant(0)
							.getStoichiometry() == 1.0) && (getParentReaction()
							.getReactant(1).getStoichiometry() == 2.0))
							|| ((getParentReaction().getReactant(0)
									.getStoichiometry() == 2.0) && (getParentReaction()
									.getReactant(1).getStoichiometry() == 1.0))) {
						orderF = "third order forward";
						participants = "two reactants, ";
					}
					break;
				case 3:
					if ((getParentReaction().getReactant(0).getStoichiometry()
							+ getParentReaction().getReactant(1)
									.getStoichiometry()
							+ getParentReaction().getReactant(2)
									.getStoichiometry() == 3.0)
							&& (getParentReaction().getReactant(0)
									.getStoichiometry() == 1.0)
							&& (getParentReaction().getReactant(1)
									.getStoichiometry() == 1.0)
							&& (getParentReaction().getReactant(2)
									.getStoichiometry() == 1.0)) {
						orderF = "third order forward";
						participants = "three reactants, ";
					}
					break;
				}

			if (productOrder == 0)
				orderR = "zeroth order reverse";
			else
				switch (getParentReaction().getNumProducts()) {
				case 1:
					if (getParentReaction().getProduct(0).getStoichiometry() == 1.0)
						orderR = "first order reverse";
					else if (getParentReaction().getProduct(0)
							.getStoichiometry() == 2.0) {
						orderR = "second order reverse";
						participants += "one product, ";
					} else if (getParentReaction().getProduct(0)
							.getStoichiometry() == 3.0) {
						orderR = "third order reverse";
						participants += "one product, ";
					}
					break;
				case 2:
					if ((getParentReaction().getProduct(0).getStoichiometry() == 1.0)
							&& (getParentReaction().getProduct(1)
									.getStoichiometry() == 1.0)) {
						orderR = "second order reverse";
						participants += "two products, ";
					} else if (((getParentReaction().getProduct(0)
							.getStoichiometry() == 1.0) && (getParentReaction()
							.getProduct(1).getStoichiometry() == 2.0))
							|| ((getParentReaction().getProduct(0)
									.getStoichiometry() == 2.0) && (getParentReaction()
									.getProduct(1).getStoichiometry() == 1.0))) {
						orderR = "third order reverse";
						participants += "two products, ";
					}
					break;
				case 3:
					if ((getParentReaction().getProduct(0).getStoichiometry()
							+ getParentReaction().getProduct(1)
									.getStoichiometry()
							+ getParentReaction().getProduct(2)
									.getStoichiometry() == 3.0)
							&& ((getParentReaction().getProduct(0)
									.getStoichiometry() == 1.0)
									&& (getParentReaction().getProduct(1)
											.getStoichiometry() == 1.0) && (getParentReaction()
									.getProduct(2).getStoichiometry() == 1.0))) {
						orderR = "third order reverse";
						participants += "three products, ";
					}
					break;
				default:
					break;
				}
			if (orderF.length() == 0)
				orderR = "";
			if (orderR.length() == 0)
				orderF = "";
			else
				orderR = ", " + orderR;
			name = orderF + orderR + ", reversible reactions, " + participants
					+ "continuous scheme";
		} else { // irreversible
			if (reactantOrder == 0)
				orderF = "zeroth order ";
			else if (getParentReaction().getNumReactants() == 1) {
				if (getParentReaction().getReactant(0).getStoichiometry() == 1.0)
					orderF = "first order ";
				else {
					if (getParentReaction().getReactant(0).getStoichiometry() == 2.0) {
						orderF = "second order ";
						participants += ", one reactant";
					} else if (getParentReaction().getReactant(0)
							.getStoichiometry() == 3.0) {
						orderF = "third order ";
						participants += ", one reactant";
					}
				}
			} else if (getParentReaction().getNumReactants() == 2) {
				if ((getParentReaction().getReactant(0).getStoichiometry() == 1.0)
						&& (getParentReaction().getReactant(1)
								.getStoichiometry() == 1.0)) {
					orderF = "second order ";
					participants += ", two reactants";
				} else if (((getParentReaction().getReactant(0)
						.getStoichiometry() == 2.0) && (getParentReaction()
						.getReactant(1).getStoichiometry() == 1.0))
						|| ((getParentReaction().getReactant(1)
								.getStoichiometry() == 2.0) && (getParentReaction()
								.getReactant(0).getStoichiometry() == 1.0))) {
					orderF = "third order ";
					participants += ", two reactants";
				}
			} else if (getParentReaction().getNumReactants() == 3) {
				// third order irreversible mass action kinetics, three
				// reactants
				if (((getParentReaction().getReactant(0).getStoichiometry()
						+ getParentReaction().getReactant(1).getStoichiometry()
						+ getParentReaction().getReactant(2).getStoichiometry() == 3.0))
						&& (getParentReaction().getReactant(0)
								.getStoichiometry() == 1.0)
						&& (getParentReaction().getReactant(1)
								.getStoichiometry() == 1.0)
						&& (getParentReaction().getReactant(2)
								.getStoichiometry() == 1.0)) {
					orderF = "third order ";
					participants += ", three reactants";
				}
			}
			name = orderF + "irreversible reactions" + participants
					+ ", continuous scheme";
		}
		return "mass action rate law for " + name;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbmlsqueezer.kinetics.BasicKineticLaw#getSBO()
	 */
	public String getSBO() {
		if (sbo == null)
			try {
				sbo = getSBOnumber(SBOParser.getSBOidForName(getName()));
			} catch (IOException e) {
				e.printStackTrace();
				sbo = "none";
			}
		return sbo;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.sbmlsqueezer.kinetics.BasicKineticLaw#createKineticEquation(jp.sbi
	 * .celldesigner.plugin.PluginModel, java.util.List, java.util.List,
	 * java.util.List, java.util.List, java.util.List, java.util.List)
	 */
	protected StringBuffer createKineticEquation(PluginModel model,
			List<String> modE, List<String> modActi, List<String> modTActi,
			List<String> modInhib, List<String> modTInhib, List<String> modCat)
			throws RateLawNotApplicableException, IllegalFormatException {
		reactantOrder = productOrder = Double.NaN;
		List<String> catalysts = new Vector<String>(modE);
		catalysts.addAll(modCat);
		StringBuffer rates[] = new StringBuffer[Math.max(1, catalysts.size())];
		PluginReaction reaction = getParentReaction();
		for (int c = 0; c < rates.length; c++) {
			rates[c] = association(catalysts, c);
			if (reaction.getReversible())
				rates[c] = diff(rates[c], dissociation(catalysts, c));
			if (catalysts.size() > 0)
				rates[c] = times(catalysts.get(c), rates[c]);
		}
		return times(activationFactor(modActi), inhibitionFactor(modInhib),
				sum(rates));
	}

	/**
	 * This method creates the part of the formula that describes the formation
	 * of the product.
	 * 
	 * @param catalysts
	 *            The list of all catalysts.
	 * @param catNum
	 *            The current catalyst for which this formula is to be created.
	 * @return A formula consisting of a constant multiplied with the product
	 *         over all reactants.
	 */
	protected StringBuffer association(List<String> catalysts, int catNum) {
		StringBuffer kass = concat("kass_", getParentReaction().getId());
		if (catalysts.size() > 0)
			kass = concat(kass, underscore, catalysts.get(catNum));
		addLocalParameter(kass);
		StringBuffer ass = new StringBuffer(kass);
		for (int reactants = 0; reactants < getParentReaction()
				.getNumReactants(); reactants++) {
			PluginSpeciesReference r = getParentReaction().getReactant(
					reactants);
			ass = times(ass, pow(r.getSpecies(), getStoichiometry(r)));
		}
		return ass;
	}

	/**
	 * Creates the part of the formula that describes the reverse reaction or
	 * dissociation.
	 * 
	 * @param catalysts
	 * @param c
	 * @return
	 */
	protected Object dissociation(List<String> catalysts, int c) {
		StringBuffer kdiss = concat("kdiss_", getParentReaction().getId());
		if (catalysts.size() > 0)
			kdiss = concat(kdiss, underscore, catalysts.get(c));
		addLocalParameter(kdiss);
		StringBuffer diss = new StringBuffer(kdiss);
		for (int products = 0; products < getParentReaction().getNumProducts(); products++) {
			PluginSpeciesReference p = getParentReaction().getProduct(products);
			diss = times(diss, pow(p.getSpecies(), getStoichiometry(p)));
		}
		return diss;
	}

	/**
	 * Returns the product of either all the activation or inhibition terms of a
	 * reaction.
	 * 
	 * @param reactionID
	 * @param modifiers
	 * @param type
	 *            ACTIVATION or INHIBITION
	 * @return
	 * @throws IllegalFormatException
	 */
	private StringBuffer createModificationFactor(List<String> modifiers,
			boolean type) throws IllegalFormatException {
		if (!modifiers.isEmpty()) {
			StringBuffer[] mods = new StringBuffer[modifiers.size()];
			for (int i = 0; i < mods.length; i++) {
				if (type) {
					// Activator Mod
					StringBuffer kA = concat("kA_", getParentReactionID(),
							underscore, modifiers.get(i));
					mods[i] = frac(new StringBuffer(modifiers.get(i)), sum(kA,
							new StringBuffer(modifiers.get(i))));
					addLocalParameter(kA);
				} else {
					// Inhibitor Mod
					StringBuffer kI = concat("kI_", getParentReactionID(),
							underscore, modifiers.get(i));
					mods[i] = frac(kI, sum(kI, new StringBuffer(modifiers
							.get(i))));
					addLocalParameter(kI);
				}
			}
			return times(mods);
		}
		return new StringBuffer();
	}

	/**
	 * According to Liebermeister and Klipp, Dec. 2006, activation can be
	 * modeled with the formula
	 * 
	 * <pre>
	 * hA = A / (k + A),
	 * </pre>
	 * 
	 * where A is the activating species and k is some constant. If multiple
	 * activators take part in this reaction, on such equation is created for
	 * each activator and multiplied with all others. This method returns this
	 * formula for the given list of activators.
	 * 
	 * @param activators
	 *            A list of activators
	 * @return Activation formula.
	 * @throws IllegalFormatException
	 */
	protected StringBuffer activationFactor(List<String> activators)
			throws IllegalFormatException {
		return createModificationFactor(activators, true);
	}

	/**
	 * According to Liebermeister and Klipp, Dec. 2006, inhibition can be
	 * modeled with the formula
	 * 
	 * <pre>
	 * hI = k/(k + I),
	 * </pre>
	 * 
	 * where I is the inhibiting species and k is some constant. In reactions
	 * infulenced by multiple inhibitors one hI equation is created for each
	 * inhibitor and multiplied with the others.
	 * 
	 * @param modifiers
	 *            A list of modifiers
	 * @return Inhibition formula.
	 * @throws IllegalFormatException
	 */
	protected StringBuffer inhibitionFactor(List<String> modifiers)
			throws IllegalFormatException {
		return createModificationFactor(modifiers, false);
	}

}
