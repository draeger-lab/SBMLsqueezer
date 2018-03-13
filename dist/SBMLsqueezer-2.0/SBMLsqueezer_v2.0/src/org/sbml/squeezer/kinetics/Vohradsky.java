/*
 * $Id: Vohradsky.java 1077 2014-01-09 22:30:50Z draeger $
 * $URL: https://rarepos.cs.uni-tuebingen.de/svn-path/SBMLsqueezer/trunk/src/org/sbml/squeezer/kinetics/Vohradsky.java $
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

import java.util.ResourceBundle;

import javax.xml.stream.XMLStreamException;

import org.sbml.jsbml.ASTNode;
import org.sbml.jsbml.Reaction;
import org.sbml.squeezer.RateLawNotApplicableException;
import org.sbml.squeezer.util.Bundles;

import de.zbit.util.ResourceManager;

/**
 * 
 * This class creates an equation based on an additive model as defined in the
 * papers &ldquo;Neural network model of gene expression.&rdquo; of
 * Vohradsk&yacute;, J. 2001 and &ldquo;Nonlinear differential equation model
 * for quantification of transcriptional regulation applied to microarray data
 * of <i>Saccharomyces cerevisiae</i>.&rdquo; of Vu, T. T. and Vohradsk&yacute;,
 * J. 2007
 * 
 * @author Sandra Nitschmann
 * @author Andreas Dr&auml;ger
 * @since 1.3
 * @version $Rev: 1077 $
 */
public class Vohradsky extends AdditiveModelNonLinear implements
InterfaceGeneRegulatoryKinetics, InterfaceModulatedKinetics,
InterfaceIrreversibleKinetics, InterfaceReversibleKinetics,
InterfaceZeroReactants, InterfaceZeroProducts {
  
  /**
   * Localization support
   */
  private static final transient ResourceBundle MESSAGES = ResourceManager.getBundle(Bundles.MESSAGES);
  
  /**
   * Generated serial version identifier.
   */
  private static final long serialVersionUID = -5955724687761012848L;
  
  /**
   * @param parentReaction
   * @param typeParameters
   * @throws RateLawNotApplicableException
   * @throws XMLStreamException
   */
  public Vohradsky(Reaction parentReaction, Object... typeParameters)
      throws RateLawNotApplicableException, XMLStreamException {
    super(parentReaction, typeParameters);
  }
  
  /* (non-Javadoc)
   * @see org.sbml.squeezer.kinetics.AdditiveModelNonLinear#getSimpleName()
   */
  @Override
  public String getSimpleName() {
    return MESSAGES.getString("VOHRADSKY_SIMPLE_NAME");
  }
  
  /* (non-Javadoc)
   * @see org.sbml.squeezer.kinetics.AdditiveModelLinear#v()
   */
  @Override
  ASTNode v() {
    return null;
  }
  
}
