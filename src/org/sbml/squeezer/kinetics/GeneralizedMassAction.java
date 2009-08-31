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
import java.util.LinkedList;
import java.util.List;

import org.sbml.ASTNode;
import org.sbml.Parameter;
import org.sbml.Reaction;
import org.sbml.SpeciesReference;
import org.sbml.squeezer.RateLawNotApplicableException;

/**
 * This class creates rate equations according to the generalized mass action
 * rate law. For details see Heinrich and Schuster,
 * "The regulation of Cellular Systems", pp. 14-17, 1996.
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

	public static boolean isApplicable(Reaction reaction) {
		// TODO
		return true;
	}

	double reactantOrder;

	double productOrder;

	/**
	 * 
	 * @param parentReaction
	 * @param listOfPossibleEnzymes
	 * @throws RateLawNotApplicableException
	 * @throws IOException
	 * @throws IllegalFormatException
	 */
	public GeneralizedMassAction(Reaction parentReaction)
			throws RateLawNotApplicableException, IOException,
			IllegalFormatException {
		super(parentReaction);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbmlsqueezer.kinetics.BasicKineticLaw#getName()
	 */
	public String getName() {
		String name = "", orderF = "", orderR = "", participants = "";
		if (getParentSBMLObject().getReversible()) {
			if (reactantOrder == 0)
				orderF = "zeroth order forward";
			else
				switch (getParentSBMLObject().getNumReactants()) {
				case 1:
					if (getParentSBMLObject().getReactant(0).getStoichiometry() == 1.0)
						orderF = "first order forward";
					else if (getParentSBMLObject().getReactant(0)
							.getStoichiometry() == 2.0) {
						orderF = "second order forward";
						participants = "one reactant, ";
					} else if (getParentSBMLObject().getReactant(0)
							.getStoichiometry() == 3.0) {
						orderF = "third order forward";
						participants = "one reactant, ";
					}
					break;
				case 2:
					if ((getParentSBMLObject().getReactant(0)
							.getStoichiometry() == 1.0)
							&& (getParentSBMLObject().getReactant(1)
									.getStoichiometry() == 1.0)) {
						orderF = "second order forward";
						participants = "two reactants, ";
					} else if (((getParentSBMLObject().getReactant(0)
							.getStoichiometry() == 1.0) && (getParentSBMLObject()
							.getReactant(1).getStoichiometry() == 2.0))
							|| ((getParentSBMLObject().getReactant(0)
									.getStoichiometry() == 2.0) && (getParentSBMLObject()
									.getReactant(1).getStoichiometry() == 1.0))) {
						orderF = "third order forward";
						participants = "two reactants, ";
					}
					break;
				case 3:
					if ((getParentSBMLObject().getReactant(0)
							.getStoichiometry()
							+ getParentSBMLObject().getReactant(1)
									.getStoichiometry()
							+ getParentSBMLObject().getReactant(2)
									.getStoichiometry() == 3.0)
							&& (getParentSBMLObject().getReactant(0)
									.getStoichiometry() == 1.0)
							&& (getParentSBMLObject().getReactant(1)
									.getStoichiometry() == 1.0)
							&& (getParentSBMLObject().getReactant(2)
									.getStoichiometry() == 1.0)) {
						orderF = "third order forward";
						participants = "three reactants, ";
					}
					break;
				}

			if (productOrder == 0)
				orderR = "zeroth order reverse";
			else
				switch (getParentSBMLObject().getNumProducts()) {
				case 1:
					if (getParentSBMLObject().getProduct(0).getStoichiometry() == 1.0)
						orderR = "first order reverse";
					else if (getParentSBMLObject().getProduct(0)
							.getStoichiometry() == 2.0) {
						orderR = "second order reverse";
						participants += "one product, ";
					} else if (getParentSBMLObject().getProduct(0)
							.getStoichiometry() == 3.0) {
						orderR = "third order reverse";
						participants += "one product, ";
					}
					break;
				case 2:
					if ((getParentSBMLObject().getProduct(0).getStoichiometry() == 1.0)
							&& (getParentSBMLObject().getProduct(1)
									.getStoichiometry() == 1.0)) {
						orderR = "second order reverse";
						participants += "two products, ";
					} else if (((getParentSBMLObject().getProduct(0)
							.getStoichiometry() == 1.0) && (getParentSBMLObject()
							.getProduct(1).getStoichiometry() == 2.0))
							|| ((getParentSBMLObject().getProduct(0)
									.getStoichiometry() == 2.0) && (getParentSBMLObject()
									.getProduct(1).getStoichiometry() == 1.0))) {
						orderR = "third order reverse";
						participants += "two products, ";
					}
					break;
				case 3:
					if ((getParentSBMLObject().getProduct(0).getStoichiometry()
							+ getParentSBMLObject().getProduct(1)
									.getStoichiometry()
							+ getParentSBMLObject().getProduct(2)
									.getStoichiometry() == 3.0)
							&& ((getParentSBMLObject().getProduct(0)
									.getStoichiometry() == 1.0)
									&& (getParentSBMLObject().getProduct(1)
											.getStoichiometry() == 1.0) && (getParentSBMLObject()
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
			else if (getParentSBMLObject().getNumReactants() == 1) {
				if (getParentSBMLObject().getReactant(0).getStoichiometry() == 1.0)
					orderF = "first order ";
				else {
					if (getParentSBMLObject().getReactant(0).getStoichiometry() == 2.0) {
						orderF = "second order ";
						participants += ", one reactant";
					} else if (getParentSBMLObject().getReactant(0)
							.getStoichiometry() == 3.0) {
						orderF = "third order ";
						participants += ", one reactant";
					}
				}
			} else if (getParentSBMLObject().getNumReactants() == 2) {
				if ((getParentSBMLObject().getReactant(0).getStoichiometry() == 1.0)
						&& (getParentSBMLObject().getReactant(1)
								.getStoichiometry() == 1.0)) {
					orderF = "second order ";
					participants += ", two reactants";
				} else if (((getParentSBMLObject().getReactant(0)
						.getStoichiometry() == 2.0) && (getParentSBMLObject()
						.getReactant(1).getStoichiometry() == 1.0))
						|| ((getParentSBMLObject().getReactant(1)
								.getStoichiometry() == 2.0) && (getParentSBMLObject()
								.getReactant(0).getStoichiometry() == 1.0))) {
					orderF = "third order ";
					participants += ", two reactants";
				}
			} else if (getParentSBMLObject().getNumReactants() == 3) {
				// third order irreversible mass action kinetics, three
				// reactants
				if (((getParentSBMLObject().getReactant(0).getStoichiometry()
						+ getParentSBMLObject().getReactant(1)
								.getStoichiometry()
						+ getParentSBMLObject().getReactant(2)
								.getStoichiometry() == 3.0))
						&& (getParentSBMLObject().getReactant(0)
								.getStoichiometry() == 1.0)
						&& (getParentSBMLObject().getReactant(1)
								.getStoichiometry() == 1.0)
						&& (getParentSBMLObject().getReactant(2)
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

	// public String getSBO() {
	// if (sbo == null)
	// try {
	// sbo = getSBOnumber(SBOParser.getSBOidForName(getName()));
	// } catch (IOException e) {
	// e.printStackTrace();
	// sbo = "none";
	// }
	// return sbo;
	// }

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
	private ASTNode createModificationFactor(List<String> modifiers,
			boolean type) throws IllegalFormatException {
		if (!modifiers.isEmpty()) {
			ASTNode[] mods = new ASTNode[modifiers.size()];
			for (int i = 0; i < mods.length; i++) {
				if (type) {
					// Activator Mod
					Parameter p_kAn = new Parameter(concat("kA_",
							getParentSBMLObject().getId(), underscore,
							modifiers.get(i)).toString());
					addLocalParameter(p_kAn);
					ASTNode kA = new ASTNode(p_kAn, this);
					mods[i] = ASTNode
							.frac(new ASTNode(modifiers.get(i), this),
									ASTNode.sum(kA, new ASTNode(modifiers
											.get(i), this)));
				} else {
					// Inhibitor Mod
					StringBuffer kIn = concat("kI_", getParentSBMLObject()
							.getId(), underscore, modifiers.get(i));
					Parameter p_kIn = new Parameter(kIn.toString());
					addLocalParameter(p_kIn);
					ASTNode kI = new ASTNode(p_kIn, this);
					mods[i] = ASTNode.frac(kI, ASTNode.sum(kI, new ASTNode(
							modifiers.get(i), this)));
				}
			}
			return ASTNode.times(mods);
		}
		return null;
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
	ASTNode activationFactor(List<String> activators)
			throws IllegalFormatException {
		return createModificationFactor(activators, true);
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
	ASTNode association(List<String> catalysts, int catNum) {
		StringBuffer kass = concat("kass_", getParentSBMLObject().getId());
		if (catalysts.size() > 0)
			kass = concat(kass, underscore, catalysts.get(catNum));
		Parameter p_kass = new Parameter(kass.toString());
		addLocalParameter(p_kass);
		ASTNode ass = new ASTNode(p_kass, this);
		for (SpeciesReference reactant : getParentSBMLObject()
				.getListOfReactants()) {
			ASTNode basis = new ASTNode(reactant.getSpeciesInstance(), this);
			if (reactant.isSetStoichiometryMath())
				basis.raiseByThePowerOf(reactant.getStoichiometryMath()
						.getMath().clone());
			else
				basis.raiseByThePowerOf(reactant.getStoichiometry());
			ass.multiplyWith(basis);
		}
		return ass;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.sbml.squeezer.kinetics.BasicKineticLaw#createKineticEquation(java
	 * .util.List, java.util.List, java.util.List, java.util.List,
	 * java.util.List, java.util.List)
	 */
	// @Override
	ASTNode createKineticEquation(List<String> modE, List<String> modActi,
			List<String> modTActi, List<String> modInhib,
			List<String> modTInhib, List<String> modCat)
			throws RateLawNotApplicableException, IllegalFormatException {
		reactantOrder = productOrder = Double.NaN;
		List<String> catalysts = new LinkedList<String>(modE);
		catalysts.addAll(modCat);
		ASTNode rates[] = new ASTNode[Math.max(1, catalysts.size())];
		Reaction reaction = getParentSBMLObject();
		for (int c = 0; c < rates.length; c++) {
			rates[c] = association(catalysts, c);
			if (reaction.getReversible())
				rates[c].minus(dissociation(catalysts, c));
			if (catalysts.size() > 0)
				rates[c].multiplyWith(new ASTNode(catalysts.get(c), this));
		}
		return ASTNode.times(activationFactor(modActi),
				inhibitionFactor(modInhib), ASTNode.sum(rates));
	}

	/**
	 * Creates the part of the formula that describes the reverse reaction or
	 * dissociation.
	 * 
	 * @param catalysts
	 * @param c
	 * @return
	 */
	ASTNode dissociation(List<String> catalysts, int c) {
		StringBuffer kdiss = concat("kdiss_", getParentSBMLObject().getId());
		if (catalysts.size() > 0)
			kdiss = concat(kdiss, underscore, catalysts.get(c));
		Parameter p_kdiss = new Parameter(kdiss.toString());
		addLocalParameter(p_kdiss);
		ASTNode diss = new ASTNode(p_kdiss, this);
		for (int products = 0; products < getParentSBMLObject()
				.getNumProducts(); products++) {
			SpeciesReference p = getParentSBMLObject().getProduct(products);
			diss.multiplyWith(ASTNode.pow(new ASTNode(p.getSpeciesInstance(),
					this), new ASTNode(p.getStoichiometry(), this)));
		}
		return diss;
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
	ASTNode inhibitionFactor(List<String> modifiers)
			throws IllegalFormatException {
		return createModificationFactor(modifiers, false);
	}
}
