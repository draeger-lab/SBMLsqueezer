/*
 * $Id: ZerothOrderForwardGMAK.java 1077 2014-01-09 22:30:50Z draeger $
 * $URL: https://rarepos.cs.uni-tuebingen.de/svn-path/SBMLsqueezer/trunk/src/org/sbml/squeezer/kinetics/ZerothOrderForwardGMAK.java $
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
import org.sbml.jsbml.Reaction;
import org.sbml.squeezer.RateLawNotApplicableException;
import org.sbml.squeezer.util.Bundles;

import de.zbit.util.ResourceManager;

/**
 * This class creates generalized mass action rate equations with zeroth order
 * in all reactants.
 * 
 * @author Andreas Dr&auml;ger
 * @date Feb 8, 2008
 * @since 1.0
 * 
 */
public class ZerothOrderForwardGMAK extends GeneralizedMassAction implements
InterfaceNonEnzymeKinetics, InterfaceReversibleKinetics,
InterfaceIrreversibleKinetics, InterfaceZeroReactants,
InterfaceZeroProducts, InterfaceModulatedKinetics {
  
  public static final transient ResourceBundle MESSAGES = ResourceManager.getBundle(Bundles.MESSAGES);
  
  /**
   * Generated serial version identifier.
   */
  private static final long serialVersionUID = -4541288426574964182L;
  
  /**
   * 
   * @param parentReaction
   * @param typeParameters
   * @throws RateLawNotApplicableException
   * @throws XMLStreamException
   */
  public ZerothOrderForwardGMAK(Reaction parentReaction,
    Object... typeParameters) throws RateLawNotApplicableException, XMLStreamException {
    super(parentReaction, typeParameters);
  }
  
  /* (non-Javadoc)
   * @see org.sbml.squeezer.kinetics.GeneralizedMassAction#association(java.util.List, int)
   */
  @Override
  final ASTNode association(List<String> catalysts, int catNum) {
    return new ASTNode(parameterFactory.parameterAssociationConst(catalysts
      .size() == 0 ? null : catalysts.get(catNum)), this);
  }
  
  /* (non-Javadoc)
   * @see org.sbml.squeezer.kinetics.BasicKineticLaw#getOrderReactants()
   */
  @Override
  double getOrderReactants() {
    return 0d;
  }
  
  /* (non-Javadoc)
   * @see org.sbml.squeezer.kinetics.GeneralizedMassAction#getSimpleName()
   */
  @Override
  public String getSimpleName() {
    return MESSAGES.getString("ZEROTH_ORDER_FORWARD_GMAK_SIMPLE_NAME");
  }
  
}
