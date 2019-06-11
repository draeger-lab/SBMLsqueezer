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

import java.util.List;
import java.util.ResourceBundle;

import javax.xml.stream.XMLStreamException;

import org.sbml.jsbml.ASTNode;
import org.sbml.jsbml.LocalParameter;
import org.sbml.jsbml.ModifierSpeciesReference;
import org.sbml.jsbml.Reaction;
import org.sbml.jsbml.SBO;
import org.sbml.jsbml.Species;
import org.sbml.jsbml.SpeciesReference;
import org.sbml.jsbml.Unit;
import org.sbml.squeezer.RateLawNotApplicableException;
import org.sbml.squeezer.ReactionType;
import org.sbml.squeezer.util.Bundles;

import de.zbit.sbml.util.SBMLtools;
import de.zbit.util.ResourceManager;

/**
 * This class creates an equation based on a linear additive model.
 * 
 * @author Sandra Nitschmann
 * @author Andreas Dr&auml;ger
 * @author Sarah R. M&uuml;ller vom Hagen
 * @since 1.3
 * @version $Rev$
 */
public class AdditiveModelLinear extends BasicKineticLaw implements
InterfaceGeneRegulatoryKinetics, InterfaceModulatedKinetics,
InterfaceIrreversibleKinetics, InterfaceReversibleKinetics,
InterfaceZeroReactants, InterfaceZeroProducts {
  
  /**
   * 
   */
  public static final transient ResourceBundle MESSAGES = ResourceManager.getBundle(Bundles.MESSAGES);
  
  /**
   * Generated serial version identifier.
   */
  private static final long serialVersionUID = -2917401499718628334L;
  
  /**
   * @param parentReaction
   * @param typeParameters
   * @throws RateLawNotApplicableException
   * @throws XMLStreamException
   */
  public AdditiveModelLinear(Reaction parentReaction,
    Object... typeParameters) throws RateLawNotApplicableException, XMLStreamException {
    super(parentReaction, typeParameters);
  }
  
  /**
   * @param g
   * @return ASTNode
   */
  ASTNode activation(ASTNode g) {
    ASTNode node = new ASTNode(1, this);
    SBMLtools.setUnits(node, Unit.Kind.DIMENSIONLESS);
    return g == null ? node : g;
  }
  
  /**
   * @return ASTNode
   */
  ASTNode b_i() {
    return new ASTNode(parameterFactory.parameterB(getParentSBMLObject().getId()), this);
  }
  
  /* (non-Javadoc)
   * @see org.sbml.squeezer.kinetics.BasicKineticLaw#createKineticEquation(java.util.List, java.util.List, java.util.List, java.util.List)
   */
  @Override
  ASTNode createKineticEquation(List<String> modE, List<String> modActi,
    List<String> modInhib, List<String> modCat)
        throws RateLawNotApplicableException {
    ASTNode m = m();
    ASTNode a = activation(g(w(), v(), b_i()));
    if (m.isOne() && a.isOne()) {
      return m;
    }
    ASTNode swap = new ASTNode(ASTNode.Type.TIMES, this);
    swap.addChild(m());
    swap.addChild(a);
    return swap;
  }
  
  /**
   * @param w
   * @param v
   * @param b
   * @return ASTNode
   */
  ASTNode g(ASTNode w, ASTNode v, ASTNode b) {
    return ASTNode.sum(w, v, b);
  }
  
  /* (non-Javadoc)
   * @see org.sbml.squeezer.kinetics.BasicKineticLaw#getSimpleName()
   */
  @Override
  public String getSimpleName() {
    return MESSAGES.getString("ADDITIVE_MODEL_LINEAR_SIMPLE_NAME");
  }
  
  /**
   * @return ASTNode
   */
  ASTNode m() {
    return new ASTNode(parameterFactory.parameterM(getParentSBMLObject().getId()), this);
  }
  
  /**
   * weighted sum over all external factors
   * 
   * @return ASTNode
   */
  ASTNode v() {
    Reaction r = getParentSBMLObject();
    ASTNode node = new ASTNode(this);
    for (ModifierSpeciesReference modifier : r.getListOfModifiers()) {
      Species modifierSpec = modifier.getSpeciesInstance();
      if (!(SBO.isProtein(modifierSpec.getSBOTerm())
          || SBO.isGeneric(modifierSpec.getSBOTerm())
          || SBO.isRNAOrMessengerRNA(modifierSpec.getSBOTerm()) || SBO
          .isGeneOrGeneCodingRegion(modifierSpec.getSBOTerm()))) {
        if (!modifier.isSetSBOTerm()) {
          SBMLtools.setSBOTerm(modifier, SBO.getModifier());
        }
        if (SBO.isModifier(modifier.getSBOTerm())) {
          ASTNode modnode = speciesTerm(modifier);
          LocalParameter p = parameterFactory.parameterV(modifier.getSpecies(), r.getId());
          ASTNode pnode = new ASTNode(p, this);
          if (node.isUnknown()) {
            node = ASTNode.times(pnode, modnode);
          } else {
            node = ASTNode.sum(node, ASTNode.times(pnode, modnode));
          }
        }
      }
    }
    return node.isUnknown() ? null : node;
  }
  
  /**
   * weighted sum over all interacting RNAs
   * 
   * @return ASTNode
   */
  ASTNode w() {
    Reaction r = getParentSBMLObject();
    ASTNode node = new ASTNode(this);
    if (r.isSetListOfProducts() && !ReactionType.representsEmptySet(r.getListOfProducts())) {
      for (SpeciesReference product : r.getListOfProducts()) {
        LocalParameter w = parameterFactory.parameterW(product.getSpecies(), r.getId());
        if (node.isUnknown()) {
          node = ASTNode.times(new ASTNode(w, this), speciesTerm(product));
        } else {
          node = ASTNode.sum(node, ASTNode.times(new ASTNode(w, this), speciesTerm(product)));
        }
      }
    }
    for (ModifierSpeciesReference modifier : r.getListOfModifiers()) {
      Species modifierSpec = modifier.getSpeciesInstance();
      if (SBO.isProtein(modifierSpec.getSBOTerm())
          || SBO.isGeneric(modifierSpec.getSBOTerm())
          || SBO.isRNAOrMessengerRNA(modifierSpec.getSBOTerm())
          || SBO.isGeneOrGeneCodingRegion(modifierSpec.getSBOTerm())) {
        if (!modifier.isSetSBOTerm()) {
          SBMLtools.setSBOTerm(modifier, SBO.getModifier());
        }
        if (SBO.isModifier(modifier.getSBOTerm())) {
          LocalParameter p = parameterFactory.parameterW(modifier.getSpecies(), r.getId());
          if (node.isUnknown()) {
            node = ASTNode.times(new ASTNode(p, this), speciesTerm(modifier));
          } else {
            node = ASTNode.sum(node, ASTNode.times(new ASTNode(p, this), speciesTerm(modifier)));
          }
        }
      }
    }
    return node.isUnknown() ? null : node;
  }
  
}
