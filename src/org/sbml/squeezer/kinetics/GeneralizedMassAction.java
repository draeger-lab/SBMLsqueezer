/*
 * $Id$
 * $URL$
 * ---------------------------------------------------------------------
 * This file is part of SBMLsqueezer, a Java program that creates rate
 * equations for reactions in SBML files (http://sbml.org).
 *
 * Copyright (C) 2006-2016 by the University of Tuebingen, Germany.
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
import java.util.ResourceBundle;

import javax.xml.stream.XMLStreamException;

import org.sbml.jsbml.ASTNode;
import org.sbml.jsbml.LocalParameter;
import org.sbml.jsbml.Reaction;
import org.sbml.jsbml.SBO;
import org.sbml.jsbml.SpeciesReference;
import org.sbml.jsbml.Unit.Kind;
import org.sbml.squeezer.RateLawNotApplicableException;
import org.sbml.squeezer.util.Bundles;

import de.zbit.sbml.util.SBMLtools;
import de.zbit.util.ResourceManager;

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
  
  public static final transient ResourceBundle MESSAGES = ResourceManager.getBundle(Bundles.MESSAGES);
  
  /**
   * Generated serial version identifier.
   */
  private static final long serialVersionUID = 3636228520951145401L;
  
  /**
   * 
   * @param parentReaction
   * @param types
   * @throws RateLawNotApplicableException
   * @throws XMLStreamException
   */
  public GeneralizedMassAction(Reaction parentReaction, Object... types)
      throws RateLawNotApplicableException, XMLStreamException {
    super(parentReaction, types);
  }
  
  /**
   * 
   */
  @SuppressWarnings("deprecation")
  private void setSBOTerm() {
    Reaction r = getParentSBMLObject();
    SBMLtools.setSBOTerm(this, 12);
    double stoichiometryLeft = 0d;
    double stoichiometryRight = 0d;
    for (SpeciesReference specRef : r.getListOfReactants()) {
      if (!specRef.isSetStoichiometryMath()) {
        stoichiometryLeft += specRef.getStoichiometry();
      }
    }
    for (SpeciesReference specRef : r.getListOfProducts()) {
      if (!specRef.isSetStoichiometryMath()) {
        stoichiometryRight += specRef.getStoichiometry();
      }
    }
    if (r.getReversible()) {
      SBMLtools.setSBOTerm(this, 42);
      if (orderReactants == 0) {
        // mass action rate law for zeroth order reversible reactions
        SBMLtools.setSBOTerm(this, 69);
        if ((stoichiometryRight == 1d) && (r.getProductCount() == 1)) {
          SBMLtools.setSBOTerm(this, 70);
        } else if (stoichiometryRight == 2d) {
          switch (r.getProductCount()) {
            case 1:
              SBMLtools.setSBOTerm(this, 72);
              break;
            case 2:
              SBMLtools.setSBOTerm(this, 73);
            default:
              SBMLtools.setSBOTerm(this, 71);
              break;
          }
        } else if (stoichiometryRight == 3d) {
          switch (r.getProductCount()) {
            case 1:
              SBMLtools.setSBOTerm(this, 75);
              break;
            case 2:
              SBMLtools.setSBOTerm(this, 76);
              break;
            case 3:
              SBMLtools.setSBOTerm(this, 77);
              break;
            default:
              SBMLtools.setSBOTerm(this, 74);
              break;
          }
        }
      } else if (stoichiometryLeft == 1d) {
        // mass action rate law for first order reversible reactions
        SBMLtools.setSBOTerm(this, 78);
        if (orderProducts == 0) {
          SBMLtools.setSBOTerm(this, 79);
        } else if (stoichiometryRight == 1d && r.getProductCount() == 1) {
          SBMLtools.setSBOTerm(this, 80);
        } else if (stoichiometryRight == 2d) {
          switch (r.getProductCount()) {
            case 1:
              SBMLtools.setSBOTerm(this, 82);
              break;
            case 2:
              SBMLtools.setSBOTerm(this, 83);
              break;
            default:
              SBMLtools.setSBOTerm(this, 81);
              break;
          }
        } else if (stoichiometryRight == 3d) {
          switch (r.getProductCount()) {
            case 1:
              SBMLtools.setSBOTerm(this, 85);
              break;
            case 2:
              SBMLtools.setSBOTerm(this, 86);
              break;
            case 3:
              SBMLtools.setSBOTerm(this, 87);
              break;
            default:
              SBMLtools.setSBOTerm(this, 84);
              break;
          }
        }
      } else if (stoichiometryLeft == 2d) {
        // mass action rate law for second order reversible reactions
        SBMLtools.setSBOTerm(this, 88);
        if (r.getReactantCount() == 1) {
          if (orderProducts == 0) {
            SBMLtools.setSBOTerm(this, 90);
          } else if (stoichiometryRight == 1d
              && r.getProductCount() == 1) {
            SBMLtools.setSBOTerm(this, 91);
          } else if (stoichiometryRight == 2d) {
            switch (r.getProductCount()) {
              case 1:
                SBMLtools.setSBOTerm(this, 93);
                break;
              case 2:
                SBMLtools.setSBOTerm(this, 94);
                break;
              default:
                SBMLtools.setSBOTerm(this, 92);
                break;
            }
          } else if (stoichiometryRight == 3d) {
            switch (r.getProductCount()) {
              case 1:
                SBMLtools.setSBOTerm(this, 96);
                break;
              case 2:
                SBMLtools.setSBOTerm(this, 97);
                break;
              case 3:
                SBMLtools.setSBOTerm(this, 98);
                break;
              default:
                SBMLtools.setSBOTerm(this, 95);
                break;
            }
          } else {
            SBMLtools.setSBOTerm(this, 89);
          }
        } else if (r.getReactantCount() == 2) {
          SBMLtools.setSBOTerm(this, 99);
          if (orderProducts == 0) {
            SBMLtools.setSBOTerm(this, 100);
          } else if (stoichiometryRight == 1d
              && r.getProductCount() == 1) {
            SBMLtools.setSBOTerm(this, 101);
          } else if (stoichiometryRight == 2d) {
            switch (r.getProductCount()) {
              case 1:
                SBMLtools.setSBOTerm(this, 103);
                break;
              case 2:
                SBMLtools.setSBOTerm(this, 104);
                break;
              default:
                SBMLtools.setSBOTerm(this, 102);
                break;
            }
          } else if (stoichiometryRight == 3d) {
            switch (r.getProductCount()) {
              case 1:
                SBMLtools.setSBOTerm(this, 106);
                break;
              case 2:
                SBMLtools.setSBOTerm(this, 107);
                break;
              case 3:
                SBMLtools.setSBOTerm(this, 108);
                break;
              default:
                SBMLtools.setSBOTerm(this, 105);
                break;
            }
          }
        }
      } else if (stoichiometryLeft == 3d) {
        // mass action rate law for third order reversible reactions
        switch (r.getReactantCount()) {
          case 1:
            SBMLtools.setSBOTerm(this, 130);
            if (orderProducts == 0) {
              SBMLtools.setSBOTerm(this, 131);
            } else if (stoichiometryRight == 1d
                && r.getProductCount() == 1) {
              SBMLtools.setSBOTerm(this, 132);
            } else if (stoichiometryRight == 2d) {
              switch (r.getProductCount()) {
                case 1:
                  SBMLtools.setSBOTerm(this, 134);
                  break;
                case 2:
                  SBMLtools.setSBOTerm(this, 135);
                  break;
                default:
                  SBMLtools.setSBOTerm(this, 133);
                  break;
              }
            } else if (stoichiometryRight == 3d) {
              switch (r.getProductCount()) {
                case 1:
                  SBMLtools.setSBOTerm(this, 137);
                  break;
                case 2:
                  SBMLtools.setSBOTerm(this, 138);
                  break;
                case 3:
                  SBMLtools.setSBOTerm(this, 139);
                  break;
                default:
                  SBMLtools.setSBOTerm(this, 136);
                  break;
              }
            }
            break;
          case 2:
            SBMLtools.setSBOTerm(this, 110);
            if (orderProducts == 0) {
              SBMLtools.setSBOTerm(this, 111);
            } else if ((stoichiometryRight == 1d)
                && (r.getProductCount() == 1)) {
              SBMLtools.setSBOTerm(this, 112);
            } else if (stoichiometryRight == 2d) {
              switch (r.getProductCount()) {
                case 1:
                  SBMLtools.setSBOTerm(this, 114);
                  break;
                case 2:
                  SBMLtools.setSBOTerm(this, 115);
                  break;
                default:
                  SBMLtools.setSBOTerm(this, 116);
                  break;
              }
            } else if (stoichiometryRight == 3d) {
              switch (r.getProductCount()) {
                case 1:
                  SBMLtools.setSBOTerm(this, 117);
                  break;
                case 2:
                  SBMLtools.setSBOTerm(this, 118);
                  break;
                case 3:
                  SBMLtools.setSBOTerm(this, 119);
                  break;
                default:
                  SBMLtools.setSBOTerm(this, 113);
                  break;
              }
            }
            break;
          case 3:
            SBMLtools.setSBOTerm(this, 120);
            if (orderProducts == 0) {
              SBMLtools.setSBOTerm(this, 121);
            } else if ((stoichiometryRight == 1d)
                && (r.getProductCount() == 1)) {
              SBMLtools.setSBOTerm(this, 122);
            } else if (stoichiometryRight == 2d) {
              switch (r.getProductCount()) {
                case 1:
                  SBMLtools.setSBOTerm(this, 124);
                  break;
                case 2:
                  SBMLtools.setSBOTerm(this, 125);
                  break;
                default:
                  SBMLtools.setSBOTerm(this, 123);
                  break;
              }
            } else if (stoichiometryRight == 3d) {
              switch (r.getProductCount()) {
                case 1:
                  SBMLtools.setSBOTerm(this, 127);
                  break;
                case 2:
                  SBMLtools.setSBOTerm(this, 128);
                  break;
                case 3:
                  SBMLtools.setSBOTerm(this, 129);
                  break;
                default:
                  SBMLtools.setSBOTerm(this, 126);
                  break;
              }
            }
            break;
          default:
            // mass action rate law for third order reversible reactions
            SBMLtools.setSBOTerm(this, 109);
            break;
        }
      }
    } else {
      // irreversible
      SBMLtools.setSBOTerm(this, 41);
      if (orderReactants == 0) {
        // SBMLtools.setSBOTerm(this, 43);
        // if continuous
        SBMLtools.setSBOTerm(this, 47);
        // else 140
      } else if (stoichiometryLeft == 1d && r.getReactantCount() == 1) {
        // SBMLtools.setSBOTerm(this, 44);
        SBMLtools.setSBOTerm(this, 49);
        // monoexponential decay rate law
        // mass action rate law for first order irreversible reactions,
        // discrete scheme
      } else if (stoichiometryLeft == 2d) {
        switch (r.getReactantCount()) {
          case 1:
            // SBMLtools.setSBOTerm(this, 50);
            SBMLtools.setSBOTerm(this, 52); // continuous
            // 142 discrete
            break;
          case 2:
            // SBMLtools.setSBOTerm(this, 53);
            SBMLtools.setSBOTerm(this, 54);
            // 143 discrete
            break;
          default:
            SBMLtools.setSBOTerm(this, 45);
            break;
        }
      } else if (stoichiometryLeft == 3d) {
        switch (r.getReactantCount()) {
          case 1:
            // 56
            SBMLtools.setSBOTerm(this, 57);
            // 144 discrete
            break;
          case 2:
            // 58
            SBMLtools.setSBOTerm(this, 59);
            // 145 discrete
          case 3:
            // 60
            SBMLtools.setSBOTerm(this, 61);
            // 146 discrete
            break;
          default:
            SBMLtools.setSBOTerm(this, 55);
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
          LocalParameter p_kAn = parameterFactory.parameterKa(modifiers.get(i));
          mods[i] = ASTNode.frac(speciesTerm(modifiers.get(i)),
            ASTNode.sum(new ASTNode(p_kAn, this),
              speciesTerm(modifiers.get(i))));
        } else {
          // Inhibitor Mod
          LocalParameter p_kIn = parameterFactory.parameterKi(modifiers.get(i));
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
   * modeled with the formula {@code hA = A / (k + A)}, where A is the
   * activating species and k is some constant. If multiple activators take part
   * in this reaction, on such equation is created for each activator and
   * multiplied with all others. This method returns this formula for the given
   * list of activators.
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
  ASTNode association(List<String> catalysts, int catNum) {
    Reaction r = getParentSBMLObject();
    LocalParameter p_kass = parameterFactory.parameterAssociationConst(
      catalysts.size() > 0 ? catalysts.get(catNum) : null);
    ASTNode astNode = new ASTNode(p_kass, this);
    for (SpeciesReference specRef : r.getListOfReactants()) {
      createTerm(astNode, specRef);
    }
    return astNode;
  }
  
  /**
   * 
   * @param astNode
   * @param specRef
   */
  @SuppressWarnings("deprecation")
  private void createTerm(ASTNode astNode, SpeciesReference specRef) {
    if (!SBO.isEmptySet(specRef.getSpeciesInstance().getSBOTerm())) {
      ASTNode basis = speciesTerm(specRef);
      if (specRef.isSetStoichiometryMath()) {
        basis.raiseByThePowerOf(specRef.getStoichiometryMath().getMath().clone());
      } else if (specRef.isSetId() && (getLevel() > 2)) {
        // It might happen that there is an assignment rule that changes the stoichiometry.
        basis.raiseByThePowerOf(specRef);
      } else {
        double stoichiometry = specRef.getStoichiometry();
        if (stoichiometry != 1d) {
          basis.raiseByThePowerOf(stoichiometry);
          ASTNode exp = basis.getRightChild();
          if ((stoichiometry != 0d) && (getLevel() > 2) && !exp.isSetUnits()) {
            // The right child must be the stoichiometric coefficient because we just set it as exponent.
            exp.setUnits(Kind.DIMENSIONLESS);
          }
        }
      }
      astNode.multiplyWith(basis);
    }
  }
  
  /* (non-Javadoc)
   * @see org.sbml.squeezer.kinetics.BasicKineticLaw#createKineticEquation(java.util.List, java.util.List, java.util.List, java.util.List)
   */
  @Override
  ASTNode createKineticEquation(List<String> modE, List<String> modActi,
    List<String> modInhib, List<String> modCat)
        throws RateLawNotApplicableException, XMLStreamException {
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
    LocalParameter p_kdiss = parameterFactory.parameterDissociationConst(
      catalysts.size() > 0 ? catalysts.get(c) : null);
    ASTNode astNode = new ASTNode(p_kdiss, this);
    for (SpeciesReference specRef : r.getListOfProducts()) {
      createTerm(astNode, specRef);
    }
    return astNode;
  }
  
  /**
   * According to Liebermeister and Klipp, Dec. 2006, inhibition can be
   * modeled with the formula {@code hI = k/(k + I)}, where I is the inhibiting
   * species and k is some constant. In reactions infulenced by multiple
   * inhibitors one hI equation is created for each inhibitor and multiplied
   * with the others.
   * 
   * @param modifiers
   *            A list of modifiers
   * @return Inhibition formula.
   */
  ASTNode inhibitionFactor(List<String> modifiers) {
    return createModificationFactor(modifiers, false);
  }
  
  /* (non-Javadoc)
   * @see org.sbml.squeezer.kinetics.BasicKineticLaw#getSimpleName()
   */
  @Override
  public String getSimpleName() {
    return MESSAGES.getString("GENERALIZED_MASS_ACTION_SIMPLE_NAME");
  }
  
}
