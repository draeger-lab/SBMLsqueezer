/*
 * $Id: CommonModularRateLaw.java 1077 2014-01-09 22:30:50Z draeger $
 * $URL: https://rarepos.cs.uni-tuebingen.de/svn-path/SBMLsqueezer/trunk/src/org/sbml/squeezer/kinetics/CommonModularRateLaw.java $
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
import org.sbml.jsbml.ListOf;
import org.sbml.jsbml.LocalParameter;
import org.sbml.jsbml.Reaction;
import org.sbml.jsbml.SpeciesReference;
import org.sbml.jsbml.Unit;
import org.sbml.squeezer.RateLawNotApplicableException;
import org.sbml.squeezer.util.Bundles;

import de.zbit.sbml.util.SBMLtools;
import de.zbit.util.ResourceManager;

/**
 * This class creates the common modular rate law (CM) according to
 * Liebermeister et al. 2010: &ldquo;Modular rate laws for enzymatic reactions:
 * thermodynamics, elasticities, and implementation&rdquo;.
 * 
 * @author Andreas Dr&auml;ger
 * @author Sarah R. M&uuml;ller vom Hagen
 * @date 2009-09-21
 * @since 1.3
 * @version $Rev: 1077 $
 */
public class CommonModularRateLaw extends PowerLawModularRateLaw implements
InterfaceUniUniKinetics, InterfaceBiUniKinetics, InterfaceBiBiKinetics,
InterfaceArbitraryEnzymeKinetics, InterfaceReversibleKinetics,
InterfaceModulatedKinetics {
  
  /**
   * 
   */
  public static final transient ResourceBundle MESSAGES = ResourceManager.getBundle(Bundles.MESSAGES);
  
  /**
   * Generated serial version identifier.
   */
  private static final long serialVersionUID = -2058956774794592395L;
  
  /**
   * 
   * @param parentReaction
   * @param types
   * @throws RateLawNotApplicableException
   * @throws XMLStreamException
   */
  public CommonModularRateLaw(Reaction parentReaction, Object... types)
      throws RateLawNotApplicableException, XMLStreamException {
    super(parentReaction, types);
    SBMLtools.setSBOTerm(this, 528); // common modular rate law
  }
  
  /* (non-Javadoc)
   * @see org.sbml.squeezer.kinetics.ReversiblePowerLaw#denominator(org.sbml.jsbml.Reaction)
   */
  @Override
  ASTNode denominator(String enzyme) {
    ASTNode denominator = denominator(enzyme, true);
    if (denominator.isUnknown()) {
      denominator = denominator(enzyme, false);
    } else {
      denominator.plus(denominator(enzyme, false));
    }
    ASTNode one = new ASTNode(1, this);
    SBMLtools.setUnits(one, Unit.Kind.DIMENSIONLESS);
    denominator.minus(one);
    ASTNode competInhib = specificModificationSummand(enzyme);
    return competInhib == null ? denominator : denominator.plus(competInhib);
  }
  
  /**
   * This actually creates the denominator parts.
   * 
   * @param forward
   *            true is forward, false reverse.
   * @return
   */
  private final ASTNode denominator(String enzyme, boolean forward) {
    ASTNode denominator = new ASTNode(this), curr;
    LocalParameter hr = parameterFactory.parameterReactionCooperativity(enzyme);
    Reaction r = getParentSBMLObject();
    ASTNode one = new ASTNode(1, this);
    if (getLevel() > 2) {
      one.setUnits(Unit.Kind.DIMENSIONLESS);
    }
    ListOf<SpeciesReference> listOf = null;
    if (forward && r.isSetListOfReactants()) {
      listOf = r.getListOfReactants();
    } else if (!forward && r.isSetListOfProducts()) {
      listOf = r.getListOfProducts();
    }
    if (listOf != null) {
      for (SpeciesReference specRef : listOf) {
        LocalParameter kM = parameterFactory.parameterMichaelis(specRef.getSpecies(), enzyme, forward);
        curr = ASTNode.sum(one.clone(), ASTNode.frac(speciesTerm(specRef), new ASTNode(kM, this)));
        curr.raiseByThePowerOf(ASTNode.times(stoichiometryTerm(specRef), new ASTNode(hr, this)));
        if (denominator.isUnknown()) {
          denominator = curr;
        } else {
          denominator.multiplyWith(curr);
        }
      }
    }
    return denominator;
  }
  
  /* (non-Javadoc)
   * @see org.sbml.squeezer.kinetics.ReversiblePowerLaw#getSimpleName()
   */
  @Override
  public String getSimpleName() {
    return MESSAGES.getString("COMMON_MODULAR_RATE_LAW_SIMPLE_NAME");
  }
  
}
