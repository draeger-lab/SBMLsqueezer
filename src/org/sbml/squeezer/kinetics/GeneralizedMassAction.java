/*
 * $Id$
 * $URL$
 * ---------------------------------------------------------------------
 * This file is part of SBMLsqueezer, a Java program that creates rate 
 * equations for reactions in SBML files (http://sbml.org).
 *
 * Copyright (C) 2006-2011 by the University of Tuebingen, Germany.
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

import java.util.LinkedList;
import java.util.List;

import org.sbml.jsbml.ASTNode;
import org.sbml.jsbml.LocalParameter;
import org.sbml.jsbml.Reaction;
import org.sbml.jsbml.SBO;
import org.sbml.jsbml.SpeciesReference;
import org.sbml.squeezer.RateLawNotApplicableException;

/**
 * This class creates rate equations according to the generalized mass action
 * rate law. For details see Heinrich and Schuster, "The regulation of Cellular
 * Systems", pp. 14-17, 1996.
 * 
 * @author Nadine Hassis
 * @author Andreas Dr&auml;ger
 * @author Hannes Borch
 * @author Sarah R. M&uuml;ller vom Hagen
 * @date Aug 1, 2007
 * @since 1.0
 * @version $Rev$
 */
public class GeneralizedMassAction extends BasicKineticLaw implements
		InterfaceNonEnzymeKinetics, InterfaceReversibleKinetics,
		InterfaceIrreversibleKinetics, InterfaceModulatedKinetics {

	/**
	 * Generated serial version identifier.
	 */
	private static final long serialVersionUID = 3636228520951145401L;

	/**
	 * 
	 * @param parentReaction
	 * @param types
	 * @throws RateLawNotApplicableException
	 */
	public GeneralizedMassAction(Reaction parentReaction, Object... types)
			throws RateLawNotApplicableException {
		super(parentReaction, types);
	}

	/**
	 * 
	 */
	private void setSBOTerm() {
		Reaction r = getParentSBMLObject();
		BasicKineticLaw.setSBOTerm(this,12);
		double stoichiometryLeft = 0;
		double stoichiometryRight = 0;
		for (SpeciesReference specRef : r.getListOfReactants())
			if (!specRef.isSetStoichiometryMath())
				stoichiometryLeft += specRef.getStoichiometry();
		for (SpeciesReference specRef : r.getListOfProducts())
			if (!specRef.isSetStoichiometryMath())
				stoichiometryRight += specRef.getStoichiometry();

		if (r.getReversible()) {
			BasicKineticLaw.setSBOTerm(this,42);
			if (orderReactants == 0) {
				// mass action rate law for zeroth order reversible reactions
				BasicKineticLaw.setSBOTerm(this,69);
				if (stoichiometryRight == 1d && r.getNumProducts() == 1)
					BasicKineticLaw.setSBOTerm(this,70);
				else if (stoichiometryRight == 2d)
					switch (r.getNumProducts()) {
					case 1:
						BasicKineticLaw.setSBOTerm(this,72);
						break;
					case 2:
						BasicKineticLaw.setSBOTerm(this,73);
					default:
						BasicKineticLaw.setSBOTerm(this,71);
						break;
					}
				else if (stoichiometryRight == 3d)
					switch (r.getNumProducts()) {
					case 1:
						BasicKineticLaw.setSBOTerm(this,75);
						break;
					case 2:
						BasicKineticLaw.setSBOTerm(this,76);
						break;
					case 3:
						BasicKineticLaw.setSBOTerm(this,77);
						break;
					default:
						BasicKineticLaw.setSBOTerm(this,74);
						break;
					}
			} else if (stoichiometryLeft == 1d) {
				// mass action rate law for first order reversible reactions
				BasicKineticLaw.setSBOTerm(this,78);
				if (orderProducts == 0)
					BasicKineticLaw.setSBOTerm(this,79);
				else if (stoichiometryRight == 1d && r.getNumProducts() == 1)
					BasicKineticLaw.setSBOTerm(this,80);
				else if (stoichiometryRight == 2d)
					switch (r.getNumProducts()) {
					case 1:
						BasicKineticLaw.setSBOTerm(this,82);
						break;
					case 2:
						BasicKineticLaw.setSBOTerm(this,83);
						break;
					default:
						BasicKineticLaw.setSBOTerm(this,81);
						break;
					}
				else if (stoichiometryRight == 3d)
					switch (r.getNumProducts()) {
					case 1:
						BasicKineticLaw.setSBOTerm(this,85);
						break;
					case 2:
						BasicKineticLaw.setSBOTerm(this,86);
						break;
					case 3:
						BasicKineticLaw.setSBOTerm(this,87);
						break;
					default:
						BasicKineticLaw.setSBOTerm(this,84);
						break;
					}
			} else if (stoichiometryLeft == 2d) {
				// mass action rate law for second order reversible reactions
				BasicKineticLaw.setSBOTerm(this,88);
				if (r.getNumReactants() == 1) {
					if (orderProducts == 0)
						BasicKineticLaw.setSBOTerm(this,90);
					else if (stoichiometryRight == 1d
							&& r.getNumProducts() == 1)
						BasicKineticLaw.setSBOTerm(this,91);
					else if (stoichiometryRight == 2d) {
						switch (r.getNumProducts()) {
						case 1:
							BasicKineticLaw.setSBOTerm(this,93);
							break;
						case 2:
							BasicKineticLaw.setSBOTerm(this,94);
							break;
						default:
							BasicKineticLaw.setSBOTerm(this,92);
							break;
						}
					} else if (stoichiometryRight == 3d) {
						switch (r.getNumProducts()) {
						case 1:
							BasicKineticLaw.setSBOTerm(this,96);
							break;
						case 2:
							BasicKineticLaw.setSBOTerm(this,97);
							break;
						case 3:
							BasicKineticLaw.setSBOTerm(this,98);
							break;
						default:
							BasicKineticLaw.setSBOTerm(this,95);
							break;
						}
					} else
						BasicKineticLaw.setSBOTerm(this,89);
				} else if (r.getNumReactants() == 2) {
					BasicKineticLaw.setSBOTerm(this,99);
					if (orderProducts == 0)
						BasicKineticLaw.setSBOTerm(this,100);
					else if (stoichiometryRight == 1d
							&& r.getNumProducts() == 1)
						BasicKineticLaw.setSBOTerm(this,101);
					else if (stoichiometryRight == 2d) {
						switch (r.getNumProducts()) {
						case 1:
							BasicKineticLaw.setSBOTerm(this,103);
							break;
						case 2:
							BasicKineticLaw.setSBOTerm(this,104);
							break;
						default:
							BasicKineticLaw.setSBOTerm(this,102);
							break;
						}
					} else if (stoichiometryRight == 3d) {
						switch (r.getNumProducts()) {
						case 1:
							BasicKineticLaw.setSBOTerm(this,106);
							break;
						case 2:
							BasicKineticLaw.setSBOTerm(this,107);
							break;
						case 3:
							BasicKineticLaw.setSBOTerm(this,108);
							break;
						default:
							BasicKineticLaw.setSBOTerm(this,105);
							break;
						}
					}
				}
			} else if (stoichiometryLeft == 3d) {
				// mass action rate law for third order reversible reactions
				switch (r.getNumReactants()) {
				case 1:
					BasicKineticLaw.setSBOTerm(this,130);
					if (orderProducts == 0)
						BasicKineticLaw.setSBOTerm(this,131);
					else if (stoichiometryRight == 1d
							&& r.getNumProducts() == 1)
						BasicKineticLaw.setSBOTerm(this,132);
					else if (stoichiometryRight == 2d) {
						switch (r.getNumProducts()) {
						case 1:
							BasicKineticLaw.setSBOTerm(this,134);
							break;
						case 2:
							BasicKineticLaw.setSBOTerm(this,135);
							break;
						default:
							BasicKineticLaw.setSBOTerm(this,133);
							break;
						}
					} else if (stoichiometryRight == 3d) {
						switch (r.getNumProducts()) {
						case 1:
							BasicKineticLaw.setSBOTerm(this,137);
							break;
						case 2:
							BasicKineticLaw.setSBOTerm(this,138);
							break;
						case 3:
							BasicKineticLaw.setSBOTerm(this,139);
							break;
						default:
							BasicKineticLaw.setSBOTerm(this,136);
							break;
						}
					}
					break;
				case 2:
					BasicKineticLaw.setSBOTerm(this,110);
					if (orderProducts == 0)
						BasicKineticLaw.setSBOTerm(this,111);
					else if (stoichiometryRight == 1d
							&& r.getNumProducts() == 1)
						BasicKineticLaw.setSBOTerm(this,112);
					else if (stoichiometryRight == 2d)
						switch (r.getNumProducts()) {
						case 1:
							BasicKineticLaw.setSBOTerm(this,114);
							break;
						case 2:
							BasicKineticLaw.setSBOTerm(this,115);
							break;
						default:
							BasicKineticLaw.setSBOTerm(this,116);
							break;
						}
					else if (stoichiometryRight == 3d)
						switch (r.getNumProducts()) {
						case 1:
							BasicKineticLaw.setSBOTerm(this,117);
							break;
						case 2:
							BasicKineticLaw.setSBOTerm(this,118);
							break;
						case 3:
							BasicKineticLaw.setSBOTerm(this,119);
							break;
						default:
							BasicKineticLaw.setSBOTerm(this,113);
							break;
						}
					break;
				case 3:
					BasicKineticLaw.setSBOTerm(this,120);
					if (orderProducts == 0)
						BasicKineticLaw.setSBOTerm(this,121);
					else if (stoichiometryRight == 1d
							&& r.getNumProducts() == 1)
						BasicKineticLaw.setSBOTerm(this,122);
					else if (stoichiometryRight == 2d)
						switch (r.getNumProducts()) {
						case 1:
							BasicKineticLaw.setSBOTerm(this,124);
							break;
						case 2:
							BasicKineticLaw.setSBOTerm(this,125);
							break;
						default:
							BasicKineticLaw.setSBOTerm(this,123);
							break;
						}
					else if (stoichiometryRight == 3d)
						switch (r.getNumProducts()) {
						case 1:
							BasicKineticLaw.setSBOTerm(this,127);
							break;
						case 2:
							BasicKineticLaw.setSBOTerm(this,128);
							break;
						case 3:
							BasicKineticLaw.setSBOTerm(this,129);
							break;
						default:
							BasicKineticLaw.setSBOTerm(this,126);
							break;
						}
					break;
				default:
					// mass action rate law for third order reversible reactions
					BasicKineticLaw.setSBOTerm(this,109);
					break;
				}
			}
		} else {
			// irreversible
			BasicKineticLaw.setSBOTerm(this,41);
			if (orderReactants == 0) {
				// BasicKineticLaw.setSBOTerm(this,43);
				// if continuous
				BasicKineticLaw.setSBOTerm(this,47);
				// else 140
			} else if (stoichiometryLeft == 1d && r.getNumReactants() == 1) {
				// BasicKineticLaw.setSBOTerm(this,44);
				BasicKineticLaw.setSBOTerm(this,49);
				// monoexponential decay rate law
				// mass action rate law for first order irreversible reactions,
				// discrete scheme
			} else if (stoichiometryLeft == 2d)
				switch (r.getNumReactants()) {
				case 1:
					// BasicKineticLaw.setSBOTerm(this,50);
					BasicKineticLaw.setSBOTerm(this,52); // continuous
					// 142 discrete
					break;
				case 2:
					// BasicKineticLaw.setSBOTerm(this,53);
					BasicKineticLaw.setSBOTerm(this,54);
					// 143 discrete
					break;
				default:
					BasicKineticLaw.setSBOTerm(this,45);
					break;
				}
			else if (stoichiometryLeft == 3d) {
				switch (r.getNumReactants()) {
				case 1:
					// 56
					BasicKineticLaw.setSBOTerm(this,57);
					// 144 discrete
					break;
				case 2:
					// 58
					BasicKineticLaw.setSBOTerm(this,59);
					// 145 discrete
				case 3:
					// 60
					BasicKineticLaw.setSBOTerm(this,61);
					// 146 discrete
					break;
				default:
					BasicKineticLaw.setSBOTerm(this,55);
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
	 */
	private ASTNode createModificationFactor(List<String> modifiers,
			boolean type) {
		if (!modifiers.isEmpty()) {
			ASTNode[] mods = new ASTNode[modifiers.size()];
			for (int i = 0; i < mods.length; i++) {
				if (type) {
					// Activator Mod
					LocalParameter p_kAn = parameterFactory
							.parameterKa(modifiers.get(i));
					mods[i] = ASTNode.frac(speciesTerm(modifiers.get(i)),
							ASTNode.sum(new ASTNode(p_kAn, this),
									speciesTerm(modifiers.get(i))));
				} else {
					// Inhibitor Mod
					LocalParameter p_kIn = parameterFactory
							.parameterKi(modifiers.get(i));
					ASTNode kI = new ASTNode(p_kIn, this);
					mods[i] = ASTNode.frac(kI, ASTNode.sum(kI.clone(),
							speciesTerm(modifiers.get(i))));
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
	 */
	ASTNode activationFactor(List<String> activators) {
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
	@SuppressWarnings("deprecation")
	ASTNode association(List<String> catalysts, int catNum) {
		Reaction r = getParentSBMLObject();
		LocalParameter p_kass = parameterFactory
				.parameterAssociationConst(catalysts.size() > 0 ? catalysts
						.get(catNum) : null);
		ASTNode ass = new ASTNode(p_kass, this);
		for (SpeciesReference specRef : r.getListOfReactants()) {
			if (!SBO.isEmptySet(specRef.getSpeciesInstance().getSBOTerm())) {
				ASTNode basis = speciesTerm(specRef);
				if (specRef.isSetStoichiometryMath()) {
					basis.raiseByThePowerOf(specRef.getStoichiometryMath()
							.getMath().clone());
				} else {
					basis.raiseByThePowerOf(specRef.getStoichiometry());
				}
				ass.multiplyWith(basis);
			}
		}
		return ass;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.sbml.squeezer.kinetics.BasicKineticLaw#createKineticEquation(java
	 * .util.List, java.util.List, java.util.List, java.util.List)
	 */
	ASTNode createKineticEquation(List<String> modE, List<String> modActi,
			List<String> modInhib, List<String> modCat)
			throws RateLawNotApplicableException {
		orderReactants = orderProducts = Double.NaN;
		List<String> catalysts = new LinkedList<String>(modE);
		catalysts.addAll(modCat);
		ASTNode rates[] = new ASTNode[Math.max(1, catalysts.size())];
		Reaction reaction = getParentSBMLObject();
		for (int c = 0; c < rates.length; c++) {
			rates[c] = association(catalysts, c);
			if (reaction.getReversible()) {
				rates[c].minus(dissociation(catalysts, c));
			}
			if (catalysts.size() > 0) {
				rates[c].multiplyWith(speciesTerm(catalysts.get(c)));
			}
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
		LocalParameter p_kdiss = parameterFactory
				.parameterDissociationConst(catalysts.size() > 0 ? catalysts
						.get(c) : null);
		ASTNode diss = new ASTNode(p_kdiss, this);
		for (SpeciesReference specRef : r.getListOfProducts()) {
			if (!SBO.isEmptySet(specRef.getSpeciesInstance().getSBOTerm())) {
				diss.multiplyWith(ASTNode.pow(speciesTerm(specRef),
						new ASTNode(specRef.getStoichiometry(), this)));
			}
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
	 */
	ASTNode inhibitionFactor(List<String> modifiers) {
		return createModificationFactor(modifiers, false);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.squeezer.kinetics.BasicKineticLaw#getSimpleName()
	 */
	@Override
	public String getSimpleName() {
		return "Generalized mass-action";
	}
}
