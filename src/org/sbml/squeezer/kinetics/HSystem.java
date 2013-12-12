/*
 * $Id$
 * $URL$
 * ---------------------------------------------------------------------
 * This file is part of SBMLsqueezer, a Java program that creates rate
 * equations for reactions in SBML files (http://sbml.org).
 *
 * Copyright (C) 2006-2013 by the University of Tuebingen, Germany.
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

import org.sbml.jsbml.ASTNode;
import org.sbml.jsbml.LocalParameter;
import org.sbml.jsbml.ModifierSpeciesReference;
import org.sbml.jsbml.Reaction;
import org.sbml.jsbml.SBO;
import org.sbml.jsbml.Species;
import org.sbml.squeezer.RateLawNotApplicableException;
import org.sbml.squeezer.UnitFactory;
import org.sbml.squeezer.util.Bundles;

import de.zbit.sbml.util.SBMLtools;
import de.zbit.util.ResourceManager;

/**
 * This class creates a non-linear additive equation form
 * <ul>
 * <li>Hadeler, K.: &ldquo;Gedanken zur Parameteridentifikation.&rdquo; Personal
 * Communication, 2003, and</li>
 * <li>Spieth, C.; Hassis, N.; Streichert, F.; Supper, J.; Beyreuther, K., and
 * Zell, A. &ldquo;Comparing Mathematical Models on the Problem of Network
 * Inference&rdquo;, 2006.</li>
 * </ul>
 * 
 * @author Sandra Nitschmann
 * @author Andreas Dr&auml;ger
 * @author Sarah R. M&uuml;ller vom Hagen
 * @since 1.3
 * @version $Rev$
 */
public class HSystem extends BasicKineticLaw implements
InterfaceGeneRegulatoryKinetics, InterfaceModulatedKinetics,
InterfaceIrreversibleKinetics, InterfaceReversibleKinetics,
InterfaceZeroReactants, InterfaceZeroProducts {
  
  public static final transient ResourceBundle MESSAGES = ResourceManager.getBundle(Bundles.MESSAGES);
  
  /**
   * Generated serial version identifier.
   */
  private static final long serialVersionUID = -5341622709394468145L;
  
  /**
   * @param parentReaction
   * @param typeParameters
   * @throws RateLawNotApplicableException
   */
  public HSystem(Reaction parentReaction, Object... typeParameters)
      throws RateLawNotApplicableException {
    super(parentReaction, typeParameters);
  }
  
  /**
   * @return ASTNode
   */
  ASTNode b_i() {
    return new ASTNode(parameterFactory.parameterBH(getParentSBMLObject().getId()), this);
  }
  
  /* (non-Javadoc)
   * @see org.sbml.squeezer.kinetics.BasicKineticLaw#createKineticEquation(java.util.List, java.util.List, java.util.List, java.util.List, java.util.List, java.util.List)
   */
  @Override
  ASTNode createKineticEquation(List<String> modE, List<String> modActi,
    List<String> modInhib, List<String> modCat)
        throws RateLawNotApplicableException {
    return g(b_i(), v(), w());
  }
  
  /**
   * 
   * @param b
   * @param v
   * @param w
   * @return
   */
  ASTNode g(ASTNode b, ASTNode v, ASTNode w) {
    return ASTNode.sum(b, v, w);
  }
  
  /**
   * @return ASTNode
   */
  ASTNode v() {
    Reaction r = getParentSBMLObject();
    ASTNode node = new ASTNode(this);
    
    UnitFactory unitFactory = new UnitFactory(getModel(), isBringToConcentration());
    ASTNode one = new ASTNode(1, this);
    if (unitFactory.getBringToConcentration()) {
      SBMLtools.setUnits(one,unitFactory.unitSubstancePerSize(
        getModel().getSubstanceUnitsInstance(),
        getModel().getVolumeUnitsInstance(), -1d));
    } else {
      SBMLtools.setUnits(one,unitFactory.unitPerSubstance(getModel().getSubstanceUnitsInstance()));
    }
    
    for (ModifierSpeciesReference modifier : r.getListOfModifiers()) {
      Species modifierspec = modifier.getSpeciesInstance();
      if (SBO.isProtein(modifierspec.getSBOTerm())
          || SBO.isGeneric(modifierspec.getSBOTerm())
          || SBO.isRNAOrMessengerRNA(modifierspec.getSBOTerm())
          || SBO.isGeneOrGeneCodingRegion(modifierspec.getSBOTerm())) {
        if (!modifier.isSetSBOTerm()) {
          SBMLtools.setSBOTerm(modifier, 19);
        }
        if (SBO.isModifier(modifier.getSBOTerm())) {
          LocalParameter p = parameterFactory.parameterVHSystem(modifier.getSpecies(), r.getId());
          if (node.isUnknown()) {
            node = ASTNode.times(new ASTNode(p, this),
              speciesTerm(modifier));
          } else {
            node = ASTNode.sum(node, ASTNode.times(new ASTNode(p,
              this), speciesTerm(modifier)));
          }
        }
      }
    }
    if ((r.getProductCount() > 0)
        && !SBO.isEmptySet(r.getProduct(0).getSpeciesInstance().getSBOTerm())) {
      if (node.isUnknown()) {
        return speciesTerm(r.getProduct(0));
      }
      return ASTNode.times(
        one,
        speciesTerm(r.getProduct(0)),
        node);
    }
    return node.isUnknown() ? null : node;
  }
  
  /**
   * @return ASTNode
   */
  ASTNode w() {
    Reaction r = getParentSBMLObject();
    ASTNode node = new ASTNode(this);
    for (ModifierSpeciesReference modifier : r.getListOfModifiers()) {
      Species modifierSpec = modifier.getSpeciesInstance();
      if (SBO.isProtein(modifierSpec.getSBOTerm())
          || SBO.isGeneric(modifierSpec.getSBOTerm())
          || SBO.isRNAOrMessengerRNA(modifierSpec.getSBOTerm())
          || SBO.isGeneOrGeneCodingRegion(modifierSpec.getSBOTerm())) {
        if (!modifier.isSetSBOTerm()) {
          SBMLtools.setSBOTerm(modifier,19);
        }
        if (SBO.isModifier(modifier.getSBOTerm())) {
          LocalParameter p = parameterFactory.parameterWHS(modifier
            .getSpecies(), r.getId());
          if (node.isUnknown()) {
            node = ASTNode.times(new ASTNode(p, this),
              speciesTerm(modifier));
          } else {
            node = ASTNode.sum(node, ASTNode.times(new ASTNode(p,
              this), speciesTerm(modifier)));
          }
        }
      }
    }
    return node.isUnknown() ? null : node;
  }
  
  /* (non-Javadoc)
   * @see org.sbml.squeezer.kinetics.BasicKineticLaw#getSimpleName()
   */
  @Override
  public String getSimpleName() {
    return MESSAGES.getString("HSYSTEM_SIMPLE_NAME");
  }
  
}
