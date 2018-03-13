/*
 * $Id: NetGeneratorNonLinear.java 1077 2014-01-09 22:30:50Z draeger $
 * $URL: https://rarepos.cs.uni-tuebingen.de/svn-path/SBMLsqueezer/trunk/src/org/sbml/squeezer/kinetics/NetGeneratorNonLinear.java $
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
import org.sbml.jsbml.Species;
import org.sbml.squeezer.RateLawNotApplicableException;
import org.sbml.squeezer.ReactionType;
import org.sbml.squeezer.UnitFactory;
import org.sbml.squeezer.util.Bundles;

import de.zbit.sbml.util.SBMLtools;
import de.zbit.util.ResourceManager;

/**
 * This class creates an equation based on an non-linear additive model as
 * defined in the paper &ldquo;The NetGenerator Algorithm: Reconstruction of
 * Gene Regulatory Networks&rdquo; of T&ouml;pfer, S.; Guthke, R.; Driesch, D.;
 * W&ouml;tzel, D., and Pfaff 2007
 * 
 * @author Sandra Nitschmann
 * @author Andreas Dr&auml;ger
 * @since 1.3
 * @version $Rev: 1077 $
 */
public class NetGeneratorNonLinear extends AdditiveModelNonLinear implements
InterfaceGeneRegulatoryKinetics, InterfaceModulatedKinetics,
InterfaceIrreversibleKinetics, InterfaceReversibleKinetics,
InterfaceZeroReactants, InterfaceZeroProducts {
  
  public static final transient ResourceBundle MESSAGES = ResourceManager.getBundle(Bundles.MESSAGES);
  
  /**
   * Generated serial version identifier.
   */
  private static final long serialVersionUID = 641441833130051384L;
  
  /**
   * @param parentReaction
   * @param typeParameters
   * @throws RateLawNotApplicableException
   * @throws XMLStreamException
   */
  public NetGeneratorNonLinear(Reaction parentReaction,
    Object... typeParameters) throws RateLawNotApplicableException, XMLStreamException {
    super(parentReaction, typeParameters);
  }
  
  /* (non-Javadoc)
   * @see org.sbml.squeezer.kinetics.AdditiveModelLinear#createKineticEquation(java.util.List, java.util.List, java.util.List, java.util.List)
   */
  @Override
  ASTNode createKineticEquation(List<String> modE, List<String> modActi,
    List<String> modInhib, List<String> modCat)
        throws RateLawNotApplicableException {
    ASTNode m = super.createKineticEquation(modE, modActi, modInhib, modCat);
    Reaction r = getParentSBMLObject();
    UnitFactory unitFactory = new UnitFactory(getModel(), isBringToConcentration());
    ASTNode one = new ASTNode(1, this);
    SBMLtools.setUnits(one, unitFactory.unitSubstancePerTime(
      getModel().getSubstanceUnitsInstance(),
      getModel().getTimeUnitsInstance()));
    if (!ReactionType.representsEmptySet(r.getListOfProducts())) {
      Species s = r.getProduct(0).getSpeciesInstance();
      ASTNode swap = new ASTNode(ASTNode.Type.TIMES, this);
      swap.addChild(one);
      swap.addChild(new ASTNode(parameterFactory.parameterW(s.getId(), r.getId()), this));
      swap.addChild(speciesTerm(s));
      return ASTNode.sum(m, swap);
    }
    return m.multiplyWith(parameterFactory.valueSubstancePerTime());
  }
  
  /* (non-Javadoc)
   * @see org.sbml.squeezer.kinetics.AdditiveModelNonLinear#getSimpleName()
   */
  @Override
  public String getSimpleName() {
    return MESSAGES.getString("NET_GENERATOR_NON_LINEAR_SIMPLE_NAME");
  }
  
}
