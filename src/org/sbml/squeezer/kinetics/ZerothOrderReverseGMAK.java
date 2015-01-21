/*
 * $Id$
 * $URL$
 * ---------------------------------------------------------------------
 * This file is part of SBMLsqueezer, a Java program that creates rate
 * equations for reactions in SBML files (http://sbml.org).
 *
 * Copyright (C) 2006-2015 by the University of Tuebingen, Germany.
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
import org.sbml.jsbml.Reaction;
import org.sbml.squeezer.RateLawNotApplicableException;
import org.sbml.squeezer.util.Bundles;

import de.zbit.util.ResourceManager;

/**
 * Mass action rate law for zeroth order reverse reactions.
 * 
 * @author Andreas Dr&auml;ger
 * @date Feb 8, 2008
 * @since 1.0
 * @version $Rev$
 */
public class ZerothOrderReverseGMAK extends GeneralizedMassAction implements
InterfaceNonEnzymeKinetics, InterfaceReversibleKinetics,
InterfaceIrreversibleKinetics, InterfaceZeroReactants,
InterfaceZeroProducts, InterfaceModulatedKinetics {
  
  public static final transient ResourceBundle MESSAGES = ResourceManager.getBundle(Bundles.MESSAGES);
  
  /**
   * Generated serial version identifier.
   */
  private static final long serialVersionUID = 327453598422460479L;
  
  /**
   * 
   * @param parentReaction
   * @param typeParameters
   * @throws RateLawNotApplicableException
   * @throws XMLStreamException
   */
  public ZerothOrderReverseGMAK(Reaction parentReaction,
    Object... typeParameters) throws RateLawNotApplicableException, XMLStreamException {
    super(parentReaction, typeParameters);
  }
  
  /* (non-Javadoc)
   * @see org.sbml.squeezer.kinetics.GeneralizedMassAction#dissociation(java.util.List, int)
   */
  @Override
  ASTNode dissociation(List<String> catalysts, int c) {
    return new ASTNode(parameterFactory
      .parameterDissociationConst(catalysts.size() == 0 ? null
          : catalysts.get(c)), this);
  }
  
  /* (non-Javadoc)
   * @see org.sbml.squeezer.kinetics.BasicKineticLaw#getOrderProducts()
   */
  @Override
  double getOrderProducts() {
    return 0d;
  }
  
  /* (non-Javadoc)
   * @see org.sbml.squeezer.kinetics.GeneralizedMassAction#getSimpleName()
   */
  @Override
  public String getSimpleName() {
    return MESSAGES.getString("ZEROTH_ORDER_REVERSE_GMAK_SIMPLE_NAME");
  }
  
}
