/*
 * $Id: RandomOrderMechanism.java 1077 2014-01-09 22:30:50Z draeger $
 * $URL: https://rarepos.cs.uni-tuebingen.de/svn-path/SBMLsqueezer/trunk/src/org/sbml/squeezer/kinetics/RandomOrderMechanism.java $
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

import javax.xml.stream.XMLStreamException;

import org.sbml.jsbml.ASTNode;
import org.sbml.jsbml.LocalParameter;
import org.sbml.jsbml.Reaction;
import org.sbml.jsbml.Species;
import org.sbml.jsbml.SpeciesReference;
import org.sbml.jsbml.Unit;
import org.sbml.squeezer.RateLawNotApplicableException;
import org.sbml.squeezer.util.Bundles;

import de.zbit.sbml.util.SBMLtools;
import de.zbit.util.ResourceManager;

/**
 * This class creates a kinetic equation according to the random-order
 * ternary-complex mechanism (see Cornish-Bowden: Fundamentals of Enzyme
 * Kinetics, p. 169, 2004).
 * 
 * @author Nadine Hassis
 * @author Andreas Dr&auml;ger
 * @author Sarah R. M&uuml;ller vom Hagen
 * @date Aug 1, 2007
 * @since 1.0
 * @version $Rev: 1077 $
 */
public class RandomOrderMechanism extends GeneralizedMassAction implements
InterfaceBiUniKinetics, InterfaceBiBiKinetics,
InterfaceReversibleKinetics, InterfaceIrreversibleKinetics,
InterfaceModulatedKinetics {
  
  public static final transient ResourceBundle MESSAGES = ResourceManager.getBundle(Bundles.MESSAGES);
  public static final transient ResourceBundle WARNINGS = ResourceManager.getBundle(Bundles.WARNINGS);
  
  /**
   * Generated serial version identifier.
   */
  private static final long serialVersionUID = 4811790241953050181L;
  
  /**
   * 
   * @param parentReaction
   * @param typeParameters
   * @throws RateLawNotApplicableException
   * @throws XMLStreamException
   */
  public RandomOrderMechanism(Reaction parentReaction,
    Object... typeParameters) throws RateLawNotApplicableException, XMLStreamException {
    super(parentReaction, typeParameters);
  }
  
  /* (non-Javadoc)
   * @see org.sbml.squeezer.kinetics.BasicKineticLaw#createKineticEquation(java.util.List, java.util.List, java.util.List, java.util.List)
   */
  @Override
  ASTNode createKineticEquation(List<String> modE, List<String> modActi,
    List<String> modInhib, List<String> modCat)
        throws RateLawNotApplicableException, XMLStreamException {
    
    Reaction reaction = getParentSBMLObject();
    SpeciesReference specRefR1 = reaction.getReactant(0), specRefR2;
    SpeciesReference specRefP1 = reaction.getProduct(0), specRefP2 = null;
    
    if (reaction.getReactantCount() == 2) {
      specRefR2 = reaction.getReactant(1);
    } else if (specRefR1.getStoichiometry() == 2d) {
      specRefR2 = specRefR1;
    } else {
      throw new RateLawNotApplicableException(MessageFormat.format(
        WARNINGS.getString("RANDOM_ORDER_NUM_OF_REACTANTS_MUST_EQUAL"),
        reaction.getId()));
    }
    
    SBMLtools.setSBOTerm(this, 429);
    double stoichiometryRight = 0;
    for (int i = 0; i < reaction.getProductCount(); i++) {
      stoichiometryRight += reaction.getProduct(i).getStoichiometry();
    }
    // according to Cornish-Bowden: Fundamentals of Enzyme kinetics
    // rapid-equilibrium random order ternary-complex mechanism
    if ((reaction.getProductCount() == 1) && (stoichiometryRight == 1d)
        && !reaction.getReversible()) {
      SBMLtools.setSBOTerm(this,432);
    }
    
    String numProd = "";
    if ((reaction.getProductCount() == 2) && (stoichiometryRight == 2)) {
      numProd = ", " + MESSAGES.getString("TWO_PRODUCTS");
    } else if ((reaction.getProductCount() == 1) && (stoichiometryRight == 1)) {
      numProd = ", " + MESSAGES.getString("ONE_PRODUCT");
    }
    
    setNotes(MessageFormat.format(
      MESSAGES.getString("RAPID_EQUILIBRIUM_RANDOM_ORDER_TERNARY_COMPLEY_MEACHANISM"),
      MESSAGES.getString(!reaction.getReversible() ? "IRREVERSIBLE" : "REVERSIBLE"),
      numProd));
    
    boolean exception = false;
    boolean biuni = false;
    switch (reaction.getProductCount()) {
      case 1:
        if (specRefP1.getStoichiometry() == 1d) {
          biuni = true;
        } else if (specRefP1.getStoichiometry() == 2d) {
          specRefP2 = specRefP1;
        } else {
          exception = true;
        }
        break;
      case 2:
        specRefP2 = reaction.getProduct(1);
        break;
      default:
        exception = true;
        break;
    }
    if (exception && reaction.getReversible()) {
      throw new RateLawNotApplicableException(
        MessageFormat.format(
          WARNINGS.getString("RANDOM_ORDER_NUM_OF_PRODUCTS_MUST_EQUAL"),
          reaction.getId()));
    }
    
    /*
     * If modE is empty there was no enzyme sined to the reaction. Thus we
     * do not want anything in modE to occur in the kinetic equation.
     */
    int enzymeNum = 0;
    ASTNode catalysts[] = new ASTNode[Math.max(1, modE.size())];
    do {
      String enzyme = modE.size() == 0 ? null : modE.get(enzymeNum);
      if (!reaction.getReversible()) {
        catalysts[enzymeNum++] = irreversible(specRefR1, specRefR2,
          enzyme);
      } else {
        catalysts[enzymeNum++] = biuni ? reversibleBiUni(specRefR1,
          specRefR2, specRefP1, enzyme) : reversibleBiBi(
            specRefR1, specRefR2, specRefP1, specRefP2, enzyme);
      }
    } while (enzymeNum < modE.size());
    return ASTNode.times(activationFactor(modActi),
      inhibitionFactor(modInhib), ASTNode.sum(catalysts));
  }
  
  /**
   * Irreversible reaction
   * 
   * @param specRefR1
   * @param specRefR2
   * @param enzyme
   */
  private ASTNode irreversible(SpeciesReference specRefR1,
    SpeciesReference specRefR2, String enzyme) {
    Species speciesR1 = specRefR1.getSpeciesInstance();
    Species speciesR2 = specRefR2.getSpeciesInstance();
    LocalParameter p_kcatp = parameterFactory.parameterKcatOrVmax(enzyme, true);
    LocalParameter p_kMr1 = parameterFactory.parameterMichaelis(speciesR1.getId(), enzyme, true);
    LocalParameter p_kMr2 = parameterFactory.parameterMichaelis(speciesR2.getId(), enzyme, true);
    LocalParameter p_kIr1 = parameterFactory.parameterKi(speciesR1.getId(), enzyme);
    ASTNode numerator = new ASTNode(p_kcatp, this);
    ASTNode denominator; // II
    if (enzyme != null) {
      numerator.multiplyWith(speciesTerm(enzyme));
    }
    if (specRefR2.equals(specRefR1)) {
      ASTNode r1square = ASTNode.pow(speciesTerm(speciesR1), 2);
      numerator = ASTNode.times(numerator, r1square.clone());
      denominator = ASTNode.sum(ASTNode.times(this, p_kIr1, p_kMr2),
        ASTNode.times(ASTNode.sum(this, p_kMr1, p_kMr2),
          speciesTerm(speciesR1)), r1square);
    } else {
      numerator = ASTNode.times(numerator, speciesTerm(speciesR1),
        speciesTerm(speciesR2));
      denominator = ASTNode.sum(ASTNode.times(this, p_kIr1, p_kMr2),
        ASTNode.times(new ASTNode(p_kMr2, this),
          speciesTerm(speciesR1)), ASTNode.times(new ASTNode(
            p_kMr1, this), speciesTerm(speciesR2)), ASTNode
            .times(speciesTerm(speciesR1),
              speciesTerm(speciesR2)));
    }
    return ASTNode.frac(numerator, denominator);
  }
  
  /**
   * Reversible reaction: Bi-Uni reactions
   * 
   * @param specRefR1
   * @param specRefR2
   * @param specRefP1
   * @param enzyme
   */
  private ASTNode reversibleBiUni(SpeciesReference specRefR1,
    SpeciesReference specRefR2, SpeciesReference specRefP1,
    String enzyme) {
    Species speciesR1 = specRefR1.getSpeciesInstance();
    Species speciesR2 = specRefR2.getSpeciesInstance();
    Species speciesP1 = specRefP1.getSpeciesInstance();
    LocalParameter p_kcatp = parameterFactory.parameterKcatOrVmax(enzyme, true);
    LocalParameter p_kcatn = parameterFactory.parameterKcatOrVmax(enzyme, false);
    LocalParameter p_kMr2 = parameterFactory.parameterMichaelis(speciesR2.getId(), enzyme, true);
    LocalParameter p_kMp1 = parameterFactory.parameterMichaelis(speciesP1.getId(), enzyme, false);
    LocalParameter p_kIr1 = parameterFactory.parameterKi(speciesR1.getId(), enzyme);
    LocalParameter p_kIr2 = parameterFactory.parameterKi(speciesR2.getId(),	enzyme);
    
    ASTNode r1r2 = specRefR1.equals(specRefR2) ? ASTNode.pow(
      speciesTerm(speciesR1), 2) : ASTNode.times(
        speciesTerm(speciesR1), speciesTerm(speciesR2));
      ASTNode numeratorForward = ASTNode.frac(new ASTNode(p_kcatp, this),
        ASTNode.times(this, p_kIr1, p_kMr2));
      ASTNode numeratorReverse = ASTNode.frac(this, p_kcatn, p_kMp1);
      if (enzyme != null) {
        numeratorForward.multiplyWith(speciesTerm(enzyme));
        numeratorReverse.multiplyWith(speciesTerm(enzyme));
      }
      numeratorForward.multiplyWith(r1r2.clone());
      numeratorReverse.multiplyWith(speciesTerm(speciesP1));
      ASTNode numerator = numeratorForward.minus(numeratorReverse);
      ASTNode one = new ASTNode(1, this);
      SBMLtools.setUnits(one, Unit.Kind.DIMENSIONLESS);
      ASTNode denominator = ASTNode.sum(
        one,
        ASTNode.frac(
          speciesTerm(speciesR1),
          new ASTNode(p_kIr1, this)
            ),
            ASTNode.frac(
              speciesTerm(speciesR2),
              new ASTNode(p_kIr2, this)
                ),
                ASTNode.frac(r1r2, ASTNode.times(this, p_kIr1, p_kMr2)),
                ASTNode.frac(
                  speciesTerm(speciesP1),
                  new ASTNode(p_kMp1, this)
                    )
          );
      return ASTNode.frac(numerator, denominator);
  }
  
  /**
   * Reversible bi-bi case.
   * 
   * @param specRefR1
   * @param specRefR2
   * @param specRefP1
   * @param specRefP2
   * @param enzyme
   */
  private ASTNode reversibleBiBi(SpeciesReference specRefR1,
    SpeciesReference specRefR2, SpeciesReference specRefP1,
    SpeciesReference specRefP2, String enzyme) {
    Species speciesR1 = specRefR1.getSpeciesInstance();
    Species speciesR2 = specRefR2.getSpeciesInstance();
    Species speciesP1 = specRefP1.getSpeciesInstance();
    Species speciesP2 = specRefP2.getSpeciesInstance();
    LocalParameter p_kcatp = parameterFactory.parameterKcatOrVmax(enzyme, true);
    LocalParameter p_kcatn = parameterFactory.parameterKcatOrVmax(enzyme, false);
    LocalParameter p_kMr2 = parameterFactory.parameterMichaelis(speciesR2.getId(), enzyme);
    LocalParameter p_kMp1 = parameterFactory.parameterMichaelis(speciesP1.getId(), enzyme);
    LocalParameter p_kIp1 = parameterFactory.parameterKi(speciesP1.getId(), enzyme);
    LocalParameter p_kIp2 = parameterFactory.parameterKi(speciesP2.getId(), enzyme);
    LocalParameter p_kIr1 = parameterFactory.parameterKi(speciesR1.getId(), enzyme);
    LocalParameter p_kIr2 = parameterFactory.parameterKi(speciesR2.getId(), enzyme);
    
    ASTNode numeratorForward = ASTNode.frac(new ASTNode(p_kcatp, this),
      ASTNode.times(this, p_kIr1, p_kMr2));
    ASTNode numeratorReverse = ASTNode.frac(new ASTNode(p_kcatn, this),
      ASTNode.times(this, p_kIp2, p_kMp1));
    if (enzyme != null) {
      numeratorForward.multiplyWith(speciesTerm(enzyme));
      numeratorReverse.multiplyWith(speciesTerm(enzyme));
    }
    // happens if the reactant has a stoichiometry of two.
    ASTNode r1r2 = specRefR1.equals(specRefR2) ? ASTNode.pow(
      speciesTerm(speciesR1), 2) : ASTNode.times(
        speciesTerm(speciesR1), speciesTerm(speciesR2));
      // happens if the product has a stoichiometry of two.
      ASTNode p1p2 = specRefP1.equals(specRefP2) ? ASTNode.pow(
        speciesTerm(speciesP1), 2) : ASTNode.times(
          speciesTerm(speciesP1), speciesTerm(speciesP2));
        numeratorForward.multiplyWith(r1r2.clone());
        numeratorReverse.multiplyWith(p1p2.clone());
        ASTNode numerator = ASTNode.diff(numeratorForward, numeratorReverse);
        ASTNode one = new ASTNode(1, this);
        SBMLtools.setUnits(one, Unit.Kind.DIMENSIONLESS);
        ASTNode denominator = ASTNode.sum(
          one,
          ASTNode.frac(speciesTerm(speciesR1), new ASTNode(p_kIr1, this)),
          ASTNode.frac(speciesTerm(speciesR2), new ASTNode(p_kIr2, this)),
          ASTNode.frac(speciesTerm(speciesP1), new ASTNode(p_kIp1, this)),
          ASTNode.frac(speciesTerm(speciesP2), new ASTNode(p_kIp2, this)),
          ASTNode.frac(p1p2,
            ASTNode.times(this, p_kIp2, p_kMp1)),
            ASTNode.frac(
              r1r2,
              ASTNode.times(this, p_kIr1, p_kMr2)
                )
            );
        return ASTNode.frac(numerator, denominator);
  }
  
  /* (non-Javadoc)
   * @see org.sbml.squeezer.kinetics.GeneralizedMassAction#getSimpleName()
   */
  @Override
  public String getSimpleName() {
    return MESSAGES.getString("RANDOM_ORDER_MEACHANISM_SIMPLE_NAME");
  }
  
}
