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

import java.util.ResourceBundle;

import javax.xml.stream.XMLStreamException;

import org.sbml.jsbml.ASTNode;
import org.sbml.jsbml.Reaction;
import org.sbml.jsbml.Unit;
import org.sbml.squeezer.RateLawNotApplicableException;
import org.sbml.squeezer.util.Bundles;

import de.zbit.sbml.util.SBMLtools;
import de.zbit.util.ResourceManager;

/**
 * This class creates an equation based on an additive model as defined in the
 * paper &ldquo;Modeling regulatory networks with weight matrices&rdquo; of
 * Weaver, D.; Workman, C., and Stormo, G. 1999
 * 
 * @author Sandra Nitschmann
 * @author Andreas Dr&auml;ger
 * @since 1.3
 * @version $Rev$
 */
public class Weaver extends AdditiveModelNonLinear implements
InterfaceGeneRegulatoryKinetics, InterfaceModulatedKinetics,
InterfaceIrreversibleKinetics, InterfaceReversibleKinetics,
InterfaceZeroReactants, InterfaceZeroProducts {
  
  public static final transient ResourceBundle MESSAGES = ResourceManager.getBundle(Bundles.MESSAGES);
  
  /**
   * Generated serial version identifier.
   */
  private static final long serialVersionUID = 8438865854245165600L;
  
  /**
   * @param parentReaction
   * @param typeParameters
   * @throws RateLawNotApplicableException
   * @throws XMLStreamException
   */
  public Weaver(Reaction parentReaction, Object... typeParameters)
      throws RateLawNotApplicableException, XMLStreamException {
    super(parentReaction, typeParameters);
  }
  
  /* (non-Javadoc)
   * @see org.sbml.squeezer.kinetics.AdditiveModelNonLinear#activation(org.sbml.jsbml.ASTNode)
   */
  @Override
  ASTNode activation(ASTNode g) {
    String rId = getParentSBMLObject().getId();
    
    ASTNode one1 = new ASTNode(1,this);
    SBMLtools.setUnits(one1, Unit.Kind.DIMENSIONLESS);
    
    ASTNode one2 = new ASTNode(1,this);
    SBMLtools.setUnits(one2, Unit.Kind.DIMENSIONLESS);
    
    if (g != null) {
      
      return ASTNode.frac(one1,
        ASTNode.sum(
          one2,
          ASTNode.exp(
            ASTNode.sum(
              ASTNode.times(
                ASTNode.uMinus(this, parameterFactory.parameterAlpha(rId)),
                g
                  ),
                  new ASTNode(parameterFactory.parameterBeta(rId), this)
                )
              )
            )
          );
    }
    
    return ASTNode.frac(one1,
      ASTNode.sum(
        one2,
        ASTNode.exp(
          ASTNode.sum(
            ASTNode.uMinus(this, parameterFactory.parameterAlpha(rId)),
            new ASTNode(parameterFactory.parameterBeta(rId), this)
              )
            )
          )
        );
  }
  
  /* (non-Javadoc)
   * @see org.sbml.squeezer.kinetics.AdditiveModelLinear#b_i()
   */
  @Override
  ASTNode b_i() {
    return null;
  }
  
  /* (non-Javadoc)
   * @see org.sbml.squeezer.kinetics.AdditiveModelNonLinear#getSimpleName()
   */
  @Override
  public String getSimpleName() {
    return MESSAGES.getString("WEAVER_SIMPLE_NAME");
  }
  
  /* (non-Javadoc)
   * @see org.sbml.squeezer.kinetics.AdditiveModelLinear#v()
   */
  @Override
  ASTNode v() {
    return null;
  }
  
}
