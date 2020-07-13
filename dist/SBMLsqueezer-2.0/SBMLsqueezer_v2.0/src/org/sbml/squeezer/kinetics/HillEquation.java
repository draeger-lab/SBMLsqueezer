/*
 * $Id: HillEquation.java 1077 2014-01-09 22:30:50Z draeger $
 * $URL: https://rarepos.cs.uni-tuebingen.de/svn-path/SBMLsqueezer/trunk/src/org/sbml/squeezer/kinetics/HillEquation.java $
 * ---------------------------------------------------------------------
 * This file is part of SBMLsqueezer, a Java program that creates rate
 * equations for reactions in SBML files (http://sbml.org).
 *
 * Copyright (C) 2006-2018 by the University of Tuebingen, Germany.
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

import javax.xml.stream.XMLStreamException;

import org.sbml.jsbml.ASTNode;
import org.sbml.jsbml.LocalParameter;
import org.sbml.jsbml.Reaction;
import org.sbml.jsbml.SBO;
import org.sbml.jsbml.Species;
import org.sbml.jsbml.SpeciesReference;
import org.sbml.jsbml.Unit;
import org.sbml.squeezer.RateLawNotApplicableException;
import org.sbml.squeezer.util.Bundles;

import de.zbit.sbml.util.SBMLtools;
import de.zbit.util.ResourceManager;

/**
 * This is the generalized version of Hill's equation as suggested by
 * Cornish-Bowden (2004, p. 314, &ldquo;Fundamentals of Enzyme Kinetics&rdquo;).
 * 
 * @author Andreas Dr&auml;ger
 * @author Sarah R. M&uuml;ller vom Hagen
 * @date 2010-03-25
 * @since 1.3
 *
 */
public class HillEquation extends BasicKineticLaw implements
InterfaceUniUniKinetics, InterfaceReversibleKinetics,
InterfaceIrreversibleKinetics, InterfaceModulatedKinetics,
InterfaceIntegerStoichiometry, InterfaceGeneRegulatoryKinetics {
  
  public static final transient ResourceBundle MESSAGES = ResourceManager.getBundle(Bundles.MESSAGES);
  
  /**
   * Generated serial version identifier.
   */
  private static final long serialVersionUID = -3319749306692986819L;
  
  /**
   * @param parentReaction
   * @param typeParameters
   * @throws RateLawNotApplicableException
   * @throws XMLStreamException
   */
  public HillEquation(Reaction parentReaction, Object... typeParameters)
      throws RateLawNotApplicableException, XMLStreamException {
    super(parentReaction, typeParameters);
  }
  
  /* (non-Javadoc)
   * @see org.sbml.squeezer.kinetics.BasicKineticLaw#createKineticEquation(java.util.List, java.util.List, java.util.List, java.util.List)
   */
  @Override
  ASTNode createKineticEquation(List<String> enzymes,
    List<String> activators, List<String> inhibitors,
    List<String> nonEnzymeCatalysts)
        throws RateLawNotApplicableException {
    
    Reaction r = getParentSBMLObject();
    
    if (activators.isEmpty() && inhibitors.isEmpty() && !r.getReversible()) {
      SBMLtools.setSBOTerm(this,195);
    }
    
    ASTNode rates[] = new ASTNode[Math.max(1, enzymes.size())];
    for (int enzymeNum = 0; enzymeNum < rates.length; enzymeNum++) {
      String enzyme = enzymes.size() == 0 ? null : enzymes.get(enzymeNum);
      // TODO: Consider that the reactant might be null or that its SBO term represents an empty set!
      SpeciesReference substrate = r.getReactant(0);
      Species reactant = substrate != null ? substrate.getSpeciesInstance() : null;
      LocalParameter kSreactant = parameterFactory.parameterKS(reactant, enzyme);
      LocalParameter hillCoeff = parameterFactory.parameterHillCoefficient(enzyme);
      
      rates[enzymeNum] = new ASTNode(parameterFactory.parameterKcatOrVmax(enzyme, true), this);
      if (enzyme != null) {
        rates[enzymeNum].multiplyWith(speciesTerm(enzyme));
      }
      
      ASTNode specTerm = reactant != null ? speciesTerm(reactant) : new ASTNode(getParent());
      ASTNode denominator = null;
      
      if (r.getReversible()) {
        // TODO: Product might be null or empty set
        Species product = r.getProduct(0).getSpeciesInstance();
        ASTNode prodTerm = new ASTNode(1, this);
        SBMLtools.setUnits(prodTerm, Unit.Kind.DIMENSIONLESS);
        prodTerm.minus(
          ASTNode.frac(
            speciesTerm(product),
            ASTNode.times(
              new ASTNode(parameterFactory.parameterEquilibriumConstant(r), this),
              specTerm.clone())
              )
            );
        rates[enzymeNum].multiplyWith(speciesTerm(reactant));
        rates[enzymeNum].divideBy(kSreactant);
        rates[enzymeNum].multiplyWith(prodTerm);
        
        LocalParameter kSproduct = parameterFactory.parameterKS(
          product, enzyme);
        // S/kS + P/kP
        specTerm = ASTNode.frac(specTerm, new ASTNode(kSreactant, this));
        specTerm.plus(ASTNode.frac(speciesTerm(product), new ASTNode(kSproduct, this)));
        ASTNode one = new ASTNode(1,this);
        SBMLtools.setUnits(one, Unit.Kind.DIMENSIONLESS);
        rates[enzymeNum].multiplyWith(
          ASTNode.pow(
            specTerm.clone(),
            ASTNode.diff(new ASTNode(hillCoeff, this), one)
              )
            );
      } else {
        denominator = ASTNode.pow(new ASTNode(kSreactant, this),
          new ASTNode(hillCoeff, this));
        if (reactant != null) {
          rates[enzymeNum].multiplyWith(ASTNode.pow(
            speciesTerm(reactant), new ASTNode(hillCoeff, this)));
        }
      }
      
      if (activators.size() + inhibitors.size() == 1) {
        Species modifier = getModel().getSpecies(activators.isEmpty() ? inhibitors.get(0) : activators.get(0));
        LocalParameter kMmodifier = parameterFactory.parameterMichaelis(modifier.getId(), enzyme);
        ASTNode kMmPow = ASTNode.pow(new ASTNode(kMmodifier, this), new ASTNode(hillCoeff, this));
        ASTNode modPow = ASTNode.pow(speciesTerm(modifier), new ASTNode(hillCoeff, this));
        LocalParameter beta = parameterFactory.parameterBeta(r.getId());
        if (SBO.isInhibitor(modifier.getSBOTerm())) {
          beta.setValue(beta.getValue() * (-1));
        }
        denominator = ASTNode.frac(
          kMmPow.clone().plus(modPow.clone()),
          kMmPow.plus(ASTNode.times(new ASTNode(beta, this), modPow))
            );
        if (!r.getReversible()) {
          denominator.multiplyWith(ASTNode.pow(this, kSreactant, hillCoeff));
        }
      }
      
      if (denominator != null) {
        denominator.plus(ASTNode.pow(specTerm, new ASTNode(hillCoeff, this)));
      } else {
        ASTNode one = new ASTNode(1, this);
        SBMLtools.setUnits(one, Unit.Kind.DIMENSIONLESS);
        denominator = ASTNode.sum(
          one,
          ASTNode.pow(specTerm, new ASTNode(hillCoeff, this))
            );
      }
      rates[enzymeNum].divideBy(denominator);
    }
    return ASTNode.sum(rates);
  }
  
  /* (non-Javadoc)
   * @see org.sbml.squeezer.kinetics.BasicKineticLaw#getSimpleName()
   */
  @Override
  public String getSimpleName() {
    if (!isSetSBOTerm()) {
      return MESSAGES.getString("GENERALIZED_HILL_EQUATION_SIMPLE_NAME");
    }
    return SBO.getTerm(getSBOTerm()).getName().replace("\\,", ",");
  }
  
}
