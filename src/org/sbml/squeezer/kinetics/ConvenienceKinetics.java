/*
 * $Id$
 * $URL$
 * ---------------------------------------------------------------------
 * This file is part of SBMLsqueezer, a Java program that creates rate
 * equations for reactions in SBML files (http://sbml.org).
 *
 * Copyright (C) 2006-2014 by the University of Tuebingen, Germany.
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
import java.util.logging.Logger;

import javax.xml.stream.XMLStreamException;

import org.sbml.jsbml.ASTNode;
import org.sbml.jsbml.ListOf;
import org.sbml.jsbml.LocalParameter;
import org.sbml.jsbml.Reaction;
import org.sbml.jsbml.SpeciesReference;
import org.sbml.jsbml.Unit;
import org.sbml.jsbml.util.Maths;
import org.sbml.jsbml.util.StringTools;
import org.sbml.squeezer.RateLawNotApplicableException;
import org.sbml.squeezer.util.Bundles;

import de.zbit.sbml.util.SBMLtools;
import de.zbit.util.ResourceManager;

/**
 * <p>
 * This is the thermodynamically independent form of the convenience kinetics.
 * In cases that the stochiometric matrix has full column rank the less
 * complicated {@link Convenience} can bee invoked.
 * </p>
 * <p>
 * Creates the Convenience kinetic's thermodynamically independent form. This
 * method in general works the same way as the according one for the convenience
 * kinetic. Each enzyme's fraction is multiplied with a reaction constant that
 * is global for the eynzme's reaction.
 * </p>
 * 
 * @author Nadine Hassis
 * @author Andreas Dr&auml;ger
 * @author Hannes Borch
 * @author Sarah R. M&uuml;ller vom Hagen
 * @date Aug 1, 2007
 * @since 1.0
 * @version $Rev$
 */
public class ConvenienceKinetics extends GeneralizedMassAction implements
InterfaceUniUniKinetics, InterfaceBiUniKinetics, InterfaceBiBiKinetics,
InterfaceArbitraryEnzymeKinetics, InterfaceReversibleKinetics,
InterfaceIrreversibleKinetics, InterfaceModulatedKinetics {
  
  public static final transient ResourceBundle MESSAGES = ResourceManager.getBundle(Bundles.MESSAGES);
  
  /**
   * Generated serial version identifier.
   */
  private static final long serialVersionUID = 8622041794368325382L;
  
  /**
   * A {@link Logger} for this class.
   */
  private static final transient Logger logger = Logger.getLogger(ConvenienceKinetics.class.getName());
  
  /**
   * 
   * @param parentReaction
   * @param types
   * @throws RateLawNotApplicableException
   * @throws XMLStreamException
   */
  public ConvenienceKinetics(Reaction parentReaction, Object... types)
      throws RateLawNotApplicableException, XMLStreamException {
    super(parentReaction, types);
  }
  
  /* (non-Javadoc)
   * @see org.sbml.squeezer.kinetics.BasicKineticLaw#createKineticEquation(java.util.List, java.util.List, java.util.List, java.util.List)
   */
  @Override
  ASTNode createKineticEquation(List<String> modE, List<String> modActi,
    List<String> modInhib, List<String> modCat)
        throws RateLawNotApplicableException, XMLStreamException {
    Reaction reaction = getParentSBMLObject();
    SBMLtools.setSBOTerm(this, 429);
    setNotes(StringTools.firstLetterUpperCase((!fullRank) ?
        MESSAGES.getString("THERMODYNAMICALLY_INDEPENDENT_CONVENIENCE_KINETICS") :
          MESSAGES.getString("CONVENIENCE_KINETICS")));
    
    ASTNode[] enzymes = new ASTNode[Math.max(modE.size(), 1)];
    for (int i = 0; i < enzymes.length; i++) {
      ASTNode numerator, denominator = null;
      String enzyme = modE.size() > 0 ? modE.get(i) : null;
      numerator = numeratorElements(enzyme, true);
      denominator = denominatorElements(enzyme, true);
      if (reaction.isReversible() && (reaction.getProductCount() > 0)) {
        numerator.minus(numeratorElements(enzyme, false));
        if (denominator != null) {
          ASTNode tmp = denominatorElements(enzyme, false);
          if (denominator.isZero()) {
            denominator = tmp;
          } else if (!tmp.isZero()) {
            denominator.plus(tmp);
          }
          if ((reaction.getProductCount() > 1)
              && (reaction.getReactantCount() > 1)) {
            if (getLevel() > 2) {
              denominator.minus(1, Unit.Kind.DIMENSIONLESS.toString().toLowerCase());
            } else {
              denominator.minus(1);
            }
          }
        }
      }
      if (denominator != null) {
        numerator.divideBy(denominator);
      }
      enzymes[i] = !fullRank ? ASTNode.times(new ASTNode(parameterFactory
        .parameterVelocityConstant(enzyme), this), numerator)
        : numerator;
      if (enzyme != null) {
        enzymes[i] = ASTNode.times(speciesTerm(enzyme), enzymes[i]);
      }
    }
    return ASTNode.times(activationFactor(modActi),
      inhibitionFactor(modInhib), ASTNode.sum(enzymes));
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
   * @param forward
   * @return true means forward, false means reverse.
   */
  private ASTNode numeratorElements(String enzyme, boolean forward) {
    Reaction reaction = getParentSBMLObject();
    
    ASTNode[] reactants = new ASTNode[reaction.getReactantCount()];
    ASTNode[] products = new ASTNode[reaction.getProductCount()];
    ASTNode[] reactantsroot = new ASTNode[reaction.getReactantCount()];
    ASTNode[] productroot = new ASTNode[reaction.getProductCount()];
    ASTNode equation;
    LocalParameter p_kM;
    
    ListOf<SpeciesReference> listOf = forward ? reaction
        .getListOfReactants() : reaction.getListOfProducts();
        
        if (!fullRank) {
          int i;
          for (i = 0; i < listOf.size(); i++) {
            SpeciesReference ref = listOf.get(i);
            p_kM = parameterFactory.parameterMichaelis(ref.getSpecies(),
              enzyme, forward);
            if (forward) {
              reactants[i] = ASTNode.pow(speciesTerm(ref).divideBy(p_kM), stoichiometryTerm(ref));
            } else {
              products[i] = ASTNode.pow(speciesTerm(ref).divideBy(p_kM), stoichiometryTerm(ref));
            }
          }
          i = 0;
          for (SpeciesReference ref : reaction.getListOfReactants()) {
            reactantsroot[i++] = ASTNode.times(
              this,
              parameterFactory.parameterKG(ref.getSpecies()),
              parameterFactory.parameterMichaelis(ref.getSpecies(),
                enzyme, forward)).raiseByThePowerOf(
                  stoichiometryTerm(ref));
          }
          i = 0;
          for (SpeciesReference ref : reaction.getListOfProducts()) {
            productroot[i++] = ASTNode.times(
              this,
              parameterFactory.parameterKG(ref.getSpecies()),
              parameterFactory.parameterMichaelis(ref.getSpecies(),
                enzyme, forward)).raiseByThePowerOf(
                  stoichiometryTerm(ref));
          }
          if (forward) {
            // TODO: in Level 3 assign a unit to the numbers
            ASTNode proot = ASTNode.times(productroot);
            if (proot == null) {
              proot = new ASTNode(1, this);
              SBMLtools.setUnits(proot, Unit.Kind.DIMENSIONLESS.getName());
            }
            ASTNode rroot = ASTNode.times(reactantsroot);
            if (rroot == null) {
              rroot = new ASTNode(1, this);
              SBMLtools.setUnits(proot, Unit.Kind.DIMENSIONLESS.getName());
            }
            equation = ASTNode.times(ASTNode.times(reactants), ASTNode
              .sqrt(ASTNode.frac(rroot, proot)));
          } else {
            ASTNode proot = ASTNode.times(productroot);
            if (proot == null) {
              proot = new ASTNode(1, this);
              SBMLtools.setUnits(proot, Unit.Kind.DIMENSIONLESS.getName());
            }
            ASTNode rroot = ASTNode.times(reactantsroot);
            if (rroot == null) {
              rroot = new ASTNode(1, this);
              SBMLtools.setUnits(proot, Unit.Kind.DIMENSIONLESS.getName());
            }
            equation = ASTNode.times(ASTNode.times(products), ASTNode
              .sqrt(ASTNode.frac(proot, rroot)));
          }
        } else {
          LocalParameter kcat = parameterFactory.parameterKcatOrVmax(enzyme,
            forward);
          ASTNode curr;
          equation = new ASTNode(kcat, this);
          for (SpeciesReference specRef : listOf) {
            p_kM = parameterFactory.parameterMichaelis(
              specRef.getSpecies(), enzyme, forward);
            curr = speciesTerm(specRef).divideBy(p_kM);
            if ((specRef.getStoichiometry() != 1d) || ((specRef.isSetId()) && (getLevel() > 2))) {
              curr.raiseByThePowerOf(stoichiometryTerm(specRef));
            }
            equation.multiplyWith(curr);
          }
        }
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
   * @param forward
   *            true means forward, false backward.
   * @return
   */
  @SuppressWarnings("deprecation")
  private ASTNode denominatorElements(String enzyme, boolean forward) {
    int level = getLevel();
    Reaction reaction = getParentSBMLObject();
    ASTNode denoms[] = new ASTNode[forward ? reaction.getReactantCount() : reaction.getProductCount()];
    boolean noOne = (denoms.length == 1)
        && (!forward || (forward && reaction.getReversible() && reaction.getProductCount() > 1));
    for (int i = 0; i < denoms.length; i++) {
      SpeciesReference ref = forward ? reaction.getReactant(i) : reaction.getProduct(i);
      LocalParameter p_kM = parameterFactory.parameterMichaelis(ref.getSpecies(), enzyme, forward);
      if (!p_kM.isSetSBOTerm()) {
        SBMLtools.setSBOTerm(p_kM,forward ? 322 : 323);
      }
      ASTNode exponent;
      if (!ref.isSetStoichiometry() && Double.isNaN(ref.getStoichiometry())
          && !ref.isSetStoichiometryMath() && ((level < 3) || !ref.isSetId())) {
        // TODO: Localize
        logger.severe(MessageFormat.format("Unknown stoichiometry in reaction {0} for reaction participant {1}.", reaction, ref.getSpecies()));
      }
      if (ref.isSetStoichiometryMath()) {
        exponent = ref.getStoichiometryMath().getMath().clone();
      } else {
        if (!Maths.isInt(ref.getStoichiometry())) {
          // TODO: Localize
          logger.severe(MessageFormat.format("{0} can only deal with integer stoichiometries.", getSimpleName(), reaction));
        }
        if ((level > 2) && (ref.isSetId())) {
          exponent = new ASTNode(ref, this);
        } else {
          exponent = new ASTNode((int) ref.getStoichiometry(), this);
          SBMLtools.setUnits(exponent, Unit.Kind.DIMENSIONLESS);
        }
      }
      denoms[i] = ASTNode.pow(ASTNode.frac(speciesTerm(ref), new ASTNode(p_kM, this)), exponent);
      for (int j = (int) ref.getStoichiometry() - 1; j >= (noOne ? 1 : 0); j--) {
        denoms[i] = ASTNode.sum(ASTNode.pow(ASTNode.frac(speciesTerm(ref), new ASTNode(p_kM, this)), j), denoms[i]);
      }
    }
    ASTNode ast = ASTNode.times(denoms);
    return ast == null ? new ASTNode(0, this) : ast;
  }
  
  /* (non-Javadoc)
   * @see org.sbml.squeezer.kinetics.GeneralizedMassAction#getSimpleName()
   */
  @Override
  public String getSimpleName() {
    return MESSAGES.getString("CONVENIENCE_KINETICS_SIMPLE_NAME");
  }
  
}
