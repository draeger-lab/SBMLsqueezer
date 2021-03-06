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
import org.sbml.jsbml.Reaction;
import org.sbml.jsbml.SBO;
import org.sbml.jsbml.SpeciesReference;
import org.sbml.squeezer.RateLawNotApplicableException;
import org.sbml.squeezer.util.Bundles;

import de.zbit.util.ResourceManager;

/**
 * (16:44:54)
 *
 * @author Andreas Dr&auml;ger
 * 
 */
public class RestrictedSpaceKinetics extends GeneralizedMassAction implements
InterfaceIrreversibleKinetics, InterfaceBiUniKinetics {
  
  public static final transient ResourceBundle MESSAGES = ResourceManager.getBundle(Bundles.MESSAGES);
  
  /**
   * Generated serial version identifier.
   */
  private static final long serialVersionUID = -3578530408534473577L;
  
  /**
   * @param parentReaction
   * @param types
   * @throws RateLawNotApplicableException
   * @throws XMLStreamException
   */
  public RestrictedSpaceKinetics(Reaction parentReaction, Object... types)
      throws RateLawNotApplicableException, XMLStreamException {
    super(parentReaction, types);
  }
  
  /* (non-Javadoc)
   * @see org.sbml.squeezer.kinetics.GeneralizedMassAction#association(java.util.List, int)
   */
  @Override
  ASTNode association(List<String> catalysts, int catNum) {
    Reaction r = getParentSBMLObject();
    LocalParameter p_h = parameterFactory.parameterTimeOrder();
    // p_h.setValue(0d); // This leads to a crash of libSBML!
    LocalParameter p_kass = parameterFactory
        .parameterSpaceRestrictedAssociationConst(p_h.getValue());
    ASTNode ass = new ASTNode(p_kass, this);
    ass.multiplyWith(ASTNode.pow(new ASTNode(ASTNode.Type.NAME_TIME, this),
      ASTNode.uMinus(new ASTNode(p_h, this))));
    int i = 0;
    for (SpeciesReference specRef : r.getListOfReactants()) {
      if (!SBO.isEmptySet(specRef.getSpeciesInstance().getSBOTerm())) {
        ASTNode basis = speciesTerm(specRef);
        basis.raiseByThePowerOf(parameterFactory
          .parameterKineticOrder(i == 0 ? "x" : "y"));
        ass.multiplyWith(basis);
      }
      i++;
    }
    return ass;
  }
  
  /* (non-Javadoc)
   * @see org.sbml.squeezer.kinetics.GeneralizedMassAction#inhibitionFactor(java.util.List)
   */
  @Override
  ASTNode inhibitionFactor(List<String> modifiers) {
    return null;
  }
  
  /* (non-Javadoc)
   * @see org.sbml.squeezer.kinetics.GeneralizedMassAction#activationFactor(java.util.List)
   */
  @Override
  ASTNode activationFactor(List<String> activators) {
    return null;
  }
  
  /* (non-Javadoc)
   * @see org.sbml.squeezer.kinetics.GeneralizedMassAction#getSimpleName()
   */
  @Override
  public String getSimpleName() {
    return MESSAGES.getString("RESTRICTED_SPACE_KINETICS_SIMPLE_NAME");
  }
}
