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

import java.util.IllegalFormatException;
import java.util.LinkedList;
import java.util.List;

import org.sbml.jsbml.ASTNode;
import org.sbml.jsbml.Parameter;
import org.sbml.jsbml.Reaction;
import org.sbml.jsbml.SpeciesReference;
import org.sbml.squeezer.RateLawNotApplicableException;

/**
 * This class creates rate equations according to the generalized mass action
 * rate law. For details see Heinrich and Schuster, "The regulation of Cellular
 * Systems", pp. 14-17, 1996.
 * 
 * @since 1.0
 * @version
 * @author <a href="mailto:Nadine.hassis@gmail.com">Nadine Hassis</a>
 * @author <a href="mailto:andreas.draeger@uni-tuebingen.de">Andreas
 *         Dr&auml;ger</a>
 * @author <a href="mailto:hannes.borch@googlemail.com">Hannes Borch</a>
 * @date Aug 1, 2007
 */
public class GeneralizedMassAction extends BasicKineticLaw implements
		InterfaceNonEnzymeKinetics, InterfaceReversibleKinetics,
		InterfaceIrreversibleKinetics, InterfaceModulatedKinetics {

	public static boolean isApplicable(Reaction reaction) {
		// TODO
		return true;
	}

	double reactantOrder;

	double productOrder;

	/**
	 * 
	 * @param parentReaction
	 * @throws RateLawNotApplicableException
	 * @throws IllegalFormatException
	 */
	public GeneralizedMassAction(Reaction parentReaction, Object... types)
			throws RateLawNotApplicableException, IllegalFormatException {
		super(parentReaction, types);
	}

	/**
	 * 
	 */
	private void setSBOTerm() {
		Reaction r = getParentSBMLObject();
		setSBOTerm(12);
		double stoichiometryLeft = 0;
		double stoichiometryRight = 0;
		for (SpeciesReference specRef : r.getListOfReactants())
			if (!specRef.isSetStoichiometryMath())
				stoichiometryLeft += specRef.getStoichiometry();
		for (SpeciesReference specRef : r.getListOfProducts())
			if (!specRef.isSetStoichiometryMath())
				stoichiometryRight += specRef.getStoichiometry();

		if (r.getReversible()) {
			setSBOTerm(42);
			if (reactantOrder == 0) {
				// mass action rate law for zeroth order reversible reactions
				setSBOTerm(69);
				if (stoichiometryRight == 1d && r.getNumProducts() == 1)
					setSBOTerm(70);
				else if (stoichiometryRight == 2d)
					switch (r.getNumProducts()) {
					case 1:
						setSBOTerm(72);
						break;
					case 2:
						setSBOTerm(73);
					default:
						setSBOTerm(71);
						break;
					}
				else if (stoichiometryRight == 3d)
					switch (r.getNumProducts()) {
					case 1:
						setSBOTerm(75);
						break;
					case 2:
						setSBOTerm(76);
						break;
					case 3:
						setSBOTerm(77);
						break;
					default:
						setSBOTerm(74);
						break;
					}
			} else if (stoichiometryLeft == 1d) {
				// mass action rate law for first order reversible reactions
				setSBOTerm(78);
				if (productOrder == 0)
					setSBOTerm(79);
				else if (stoichiometryRight == 1d && r.getNumProducts() == 1)
					setSBOTerm(80);
				else if (stoichiometryRight == 2d)
					switch (r.getNumProducts()) {
					case 1:
						setSBOTerm(82);
						break;
					case 2:
						setSBOTerm(83);
						break;
					default:
						setSBOTerm(81);
						break;
					}
				else if (stoichiometryRight == 3d)
					switch (r.getNumProducts()) {
					case 1:
						setSBOTerm(85);
						break;
					case 2:
						setSBOTerm(86);
						break;
					case 3:
						setSBOTerm(87);
						break;
					default:
						setSBOTerm(84);
						break;
					}
			} else if (stoichiometryLeft == 2d) {
				// mass action rate law for second order reversible reactions
				setSBOTerm(88);
				if (r.getNumReactants() == 1) {
					if (productOrder == 0)
						setSBOTerm(90);
					else if (stoichiometryRight == 1d
							&& r.getNumProducts() == 1)
						setSBOTerm(91);
					else if (stoichiometryRight == 2d) {
						switch (r.getNumProducts()) {
						case 1:
							setSBOTerm(93);
							break;
						case 2:
							setSBOTerm(94);
							break;
						default:
							setSBOTerm(92);
							break;
						}
					} else if (stoichiometryRight == 3d) {
						switch (r.getNumProducts()) {
						case 1:
							setSBOTerm(96);
							break;
						case 2:
							setSBOTerm(97);
							break;
						case 3:
							setSBOTerm(98);
							break;
						default:
							setSBOTerm(95);
							break;
						}
					} else
						setSBOTerm(89);
				} else if (r.getNumReactants() == 2) {
					setSBOTerm(99);
					if (productOrder == 0)
						setSBOTerm(100);
					else if (stoichiometryRight == 1d
							&& r.getNumProducts() == 1)
						setSBOTerm(101);
					else if (stoichiometryRight == 2d) {
						switch (r.getNumProducts()) {
						case 1:
							setSBOTerm(103);
							break;
						case 2:
							setSBOTerm(104);
							break;
						default:
							setSBOTerm(102);
							break;
						}
					} else if (stoichiometryRight == 3d) {
						switch (r.getNumProducts()) {
						case 1:
							setSBOTerm(106);
							break;
						case 2:
							setSBOTerm(107);
							break;
						case 3:
							setSBOTerm(108);
							break;
						default:
							setSBOTerm(105);
							break;
						}
					}
				}
			} else if (stoichiometryLeft == 3d) {
				// mass action rate law for third order reversible reactions
				switch (r.getNumReactants()) {
				case 1:
					setSBOTerm(130);
					if (productOrder == 0)
						setSBOTerm(131);
					else if (stoichiometryRight == 1d
							&& r.getNumProducts() == 1)
						setSBOTerm(132);
					else if (stoichiometryRight == 2d) {
						switch (r.getNumProducts()) {
						case 1:
							setSBOTerm(134);
							break;
						case 2:
							setSBOTerm(135);
							break;
						default:
							setSBOTerm(133);
							break;
						}
					} else if (stoichiometryRight == 3d) {
						switch (r.getNumProducts()) {
						case 1:
							setSBOTerm(137);
							break;
						case 2:
							setSBOTerm(138);
							break;
						case 3:
							setSBOTerm(139);
							break;
						default:
							setSBOTerm(136);
							break;
						}
					}
					break;
				case 2:
					setSBOTerm(110);
					if (productOrder == 0)
						setSBOTerm(111);
					else if (stoichiometryRight == 1d
							&& r.getNumProducts() == 1)
						setSBOTerm(112);
					else if (stoichiometryRight == 2d)
						switch (r.getNumProducts()) {
						case 1:
							setSBOTerm(114);
							break;
						case 2:
							setSBOTerm(115);
							break;
						default:
							setSBOTerm(116);
							break;
						}
					else if (stoichiometryRight == 3d)
						switch (r.getNumProducts()) {
						case 1:
							setSBOTerm(117);
							break;
						case 2:
							setSBOTerm(118);
							break;
						case 3:
							setSBOTerm(119);
							break;
						default:
							setSBOTerm(113);
							break;
						}
					break;
				case 3:
					setSBOTerm(120);
					if (productOrder == 0)
						setSBOTerm(121);
					else if (stoichiometryRight == 1d
							&& r.getNumProducts() == 1)
						setSBOTerm(122);
					else if (stoichiometryRight == 2d)
						switch (r.getNumProducts()) {
						case 1:
							setSBOTerm(124);
							break;
						case 2:
							setSBOTerm(125);
							break;
						default:
							setSBOTerm(123);
							break;
						}
					else if (stoichiometryRight == 3d)
						switch (r.getNumProducts()) {
						case 1:
							setSBOTerm(127);
							break;
						case 2:
							setSBOTerm(128);
							break;
						case 3:
							setSBOTerm(129);
							break;
						default:
							setSBOTerm(126);
							break;
						}
					break;
				default:
					// mass action rate law for third order reversible reactions
					setSBOTerm(109);
					break;
				}
			}
		} else {
			// irreversible
			setSBOTerm(41);
			if (reactantOrder == 0) {
				// setSBOTerm(43);
				// if continuous
				setSBOTerm(47);
				// else 140
			} else if (stoichiometryLeft == 1d && r.getNumReactants() == 1) {
				// setSBOTerm(44);
				setSBOTerm(49);
				// monoexponential decay rate law
				// mass action rate law for first order irreversible reactions,
				// discrete scheme
			} else if (stoichiometryLeft == 2d)
				switch (r.getNumReactants()) {
				case 1:
					// setSBOTerm(50);
					setSBOTerm(52); // continuous
					// 142 discrete
					break;
				case 2:
					// setSBOTerm(53);
					setSBOTerm(54);
					// 143 discrete
					break;
				default:
					setSBOTerm(45);
					break;
				}
			else if (stoichiometryLeft == 3d) {
				switch (r.getNumReactants()) {
				case 1:
					// 56
					setSBOTerm(57);
					// 144 discrete
					break;
				case 2:
					// 58
					setSBOTerm(59);
					// 145 discrete
				case 3:
					// 60
					setSBOTerm(61);
					// 146 discrete
					break;
				default:
					setSBOTerm(55);
					break;
				}
			} else {
				// SBO:0000163 - mass action rate law for irreversible
				// reactions, continuous scheme
				// SBO:0000166 - mass action rate law for irreversible
				// reactions, discrete scheme
			}
		}
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
	private ASTNode createModificationFactor(List<String> modifiers,
			boolean type) throws IllegalFormatException {
		Reaction r = getParentSBMLObject();
		if (!modifiers.isEmpty()) {
			ASTNode[] mods = new ASTNode[modifiers.size()];
			for (int i = 0; i < mods.length; i++) {
				if (type) {
					// Activator Mod
					Parameter p_kAn = parameterKa(r.getId(), modifiers.get(i));
					mods[i] = ASTNode.frac(new ASTNode(modifiers.get(i), this),
							ASTNode.sum(new ASTNode(p_kAn, this), new ASTNode(
									modifiers.get(i), this)));
				} else {
					// Inhibitor Mod
					Parameter p_kIn = parameterKi(r.getId(), modifiers.get(i));
					ASTNode kI = new ASTNode(p_kIn, this);
					mods[i] = ASTNode.frac(kI, ASTNode.sum(kI.clone(),
							new ASTNode(modifiers.get(i), this)));
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
		Reaction r = getParentSBMLObject();
		StringBuffer kass = concat("kass_", r.getId());
		if (catalysts.size() > 0)
			append(kass, underscore, catalysts.get(catNum));
		Parameter p_kass = createOrGetParameter(kass.toString());
		p_kass.setSBOTerm(153);
		ASTNode ass = new ASTNode(p_kass, this);
		for (SpeciesReference reactant : r.getListOfReactants()) {
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
		setSBOTerm();
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
		Reaction r = getParentSBMLObject();
		StringBuffer kdiss = concat("kdiss_", r.getId());
		if (catalysts.size() > 0)
			kdiss = concat(kdiss, underscore, catalysts.get(c));
		Parameter p_kdiss = createOrGetParameter(kdiss.toString());
		p_kdiss.setSBOTerm(156);
		ASTNode diss = new ASTNode(p_kdiss, this);
		for (int products = 0; products < r.getNumProducts(); products++) {
			SpeciesReference p = r.getProduct(products);
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

	// @Override
	public String getSimpleName() {
		return "Generalized mass-action";
	}
}
