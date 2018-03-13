/*
 * $Id: SSystem.java 1077 2014-01-09 22:30:50Z draeger $
 * $URL: https://rarepos.cs.uni-tuebingen.de/svn-path/SBMLsqueezer/trunk/src/org/sbml/squeezer/kinetics/SSystem.java $
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
import org.sbml.jsbml.ListOf;
import org.sbml.jsbml.LocalParameter;
import org.sbml.jsbml.ModifierSpeciesReference;
import org.sbml.jsbml.Reaction;
import org.sbml.jsbml.SBO;
import org.sbml.jsbml.Species;
import org.sbml.jsbml.SpeciesReference;
import org.sbml.squeezer.RateLawNotApplicableException;
import org.sbml.squeezer.UnitFactory;
import org.sbml.squeezer.util.Bundles;

import de.zbit.sbml.util.SBMLtools;
import de.zbit.util.ResourceManager;

/**
 * 
 * This class creates a S-System equation as defined in the papers:
 * <ul>
 * <li>Tournier, L.: &ldquo;Approximation of dynamical systems using S-systems
 * theory: application to biological systems&rdquo;</li>
 * <li>Spieth, C.; Streichert, F.; Speer, N., and Zell, A.: &ldquo;Optimizing
 * Topology and Parameters of Gene Regulatory Network Models from Time-Series
 * Experiments&rdquo;</li>
 * <li>Spieth, C.; Hassis, N.; Streichert, F.; Supper, J.; Beyreuther, K., and
 * Zell, A.: &ldquo;Comparing Mathematical Models on the Problem of Network
 * Inference&rdquo; and</li>
 * <li>Hecker, M.; Lambeck, S.; T&ouml;pfer, S.; Someren, E. van, and Guthke,
 * R.: &ldquo;Gene regulatory network inference: data integration in dynamic
 * models-a review&rdquo;</li>
 * </ul>
 * 
 * @author Sandra Nitschmann
 * @author Andreas Dr&auml;ger
 * @author Sarah R. M&uuml;ller vom Hagen
 * @since 1.3
 * @version $Rev: 1077 $
 */
public class SSystem extends BasicKineticLaw implements
InterfaceGeneRegulatoryKinetics, InterfaceModulatedKinetics,
InterfaceIrreversibleKinetics, InterfaceReversibleKinetics,
InterfaceZeroReactants, InterfaceZeroProducts {
  
  public static final transient ResourceBundle MESSAGES = ResourceManager.getBundle(Bundles.MESSAGES);
  
  /**
   * Generated serial version identifier.
   */
  private static final long serialVersionUID = -1324851161924978070L;
  
  /**
   * @param parentReaction
   * @param typeParameters
   * @throws RateLawNotApplicableException
   * @throws XMLStreamException
   */
  public SSystem(Reaction parentReaction, Object... typeParameters)
      throws RateLawNotApplicableException, XMLStreamException {
    super(parentReaction, typeParameters);
  }
  
  /* (non-Javadoc)
   * @see org.sbml.squeezer.kinetics.BasicKineticLaw#createKineticEquation(java.util.List, java.util.List, java.util.List, java.util.List)
   */
  @Override
  ASTNode createKineticEquation(List<String> modE, List<String> modActi,
    List<String> modInhib, List<String> modCat)
        throws RateLawNotApplicableException {
    Reaction r = getParentSBMLObject();
    ASTNode node = prod(r.getListOfReactants(), true);
    UnitFactory unitFactory = new UnitFactory(getModel(), isBringToConcentration());
    if (r.getReversible()) {
      if (node.isUnknown()) {
        node = ASTNode.uMinus(prod(r.getListOfProducts(), false));
      } else {
        node.minus(prod(r.getListOfProducts(), false));
      }
    }
    for (ModifierSpeciesReference modifier : r.getListOfModifiers()) {
      Species modifierspec = modifier.getSpeciesInstance();
      if (SBO.isProtein(modifierspec.getSBOTerm())
          || SBO.isGeneric(modifierspec.getSBOTerm())
          || SBO.isRNAOrMessengerRNA(modifierspec.getSBOTerm())
          || SBO.isGeneOrGeneCodingRegion(modifierspec.getSBOTerm())) {
        if (!modifier.isSetSBOTerm()) {
          SBMLtools.setSBOTerm(modifier,19);
        }
        if (SBO.isModifier(modifier.getSBOTerm())) {
          LocalParameter exp = parameterFactory
              .parameterSSystemExponent(modifierspec.getId());
          String name = exp.getName();
          if (SBO.isStimulator(modifier.getSBOTerm())) {
            name.concat("_sti");
            exp.setName(name);
          }
          if (SBO.isInhibitor(modifier.getSBOTerm())) {
            name.concat("_inh");
            exp.setName(name);
          }
          ASTNode expnode = new ASTNode(exp, this);
          if (node.isUnknown()) {
            node = ASTNode.pow(speciesTerm(modifier), expnode);
          }
          else {
            ASTNode one = new ASTNode(1, this);
            if (unitFactory.getBringToConcentration()) {
              SBMLtools.setUnits(one,unitFactory.unitSubstancePerSize(
                getModel().getSubstanceUnitsInstance(),
                getModel().getVolumeUnitsInstance(), -1d));
            } else {
              SBMLtools.setUnits(one,unitFactory.unitPerSubstance(getModel().getSubstanceUnitsInstance()));
            }
            node.multiplyWith(
              one,
              ASTNode.pow(speciesTerm(modifier),
                expnode));
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
    return MESSAGES.getString("SSYSTEM_SIMPLE_NAME");
  }
  
  /**
   * The product term in S-Systems.
   * 
   * @param listOf
   * @param forward
   * @return
   */
  private ASTNode prod(ListOf<SpeciesReference> listOf, boolean forward) {
    String rID = getParentSBMLObject().getId();
    ASTNode prod = new ASTNode(forward ? parameterFactory.parameterSSystemAlpha(rID)
        : parameterFactory.parameterSSystemBeta(rID), this);
    UnitFactory unitFactory = new UnitFactory(getModel(), isBringToConcentration());
    for (SpeciesReference specRef : listOf) {
      if (!SBO.isEmptySet(specRef.getSpeciesInstance().getSBOTerm())) {
        LocalParameter exponent = parameterFactory.parameterSSystemExponent(specRef.getSpecies());
        ASTNode pow = ASTNode.pow(speciesTerm(specRef), new ASTNode(exponent, this));
        if (prod.isUnknown()) {
          prod = pow;
        } else {
          ASTNode one = new ASTNode(1, this);
          if (unitFactory.getBringToConcentration()) {
            SBMLtools.setUnits(one,unitFactory.unitSubstancePerSize(
              getModel().getSubstanceUnitsInstance(),
              getModel().getVolumeUnitsInstance(), -1d));
          } else {
            SBMLtools.setUnits(one,unitFactory.unitPerSubstance(getModel().getSubstanceUnitsInstance()));
          }
          prod.multiplyWith(pow,one);
        }
      }
    }
    return prod;
  }
  
}
