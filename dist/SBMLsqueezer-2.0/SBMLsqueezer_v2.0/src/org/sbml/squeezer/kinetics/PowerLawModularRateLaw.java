/*
 * $Id: PowerLawModularRateLaw.java 1081 2014-02-22 15:15:58Z draeger $
 * $URL: https://rarepos.cs.uni-tuebingen.de/svn-path/SBMLsqueezer/trunk/src/org/sbml/squeezer/kinetics/PowerLawModularRateLaw.java $
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
import org.sbml.jsbml.LocalParameter;
import org.sbml.jsbml.ModifierSpeciesReference;
import org.sbml.jsbml.Parameter;
import org.sbml.jsbml.Reaction;
import org.sbml.jsbml.SBO;
import org.sbml.jsbml.SpeciesReference;
import org.sbml.jsbml.Unit;
import org.sbml.squeezer.RateLawNotApplicableException;
import org.sbml.squeezer.UnitFactory;
import org.sbml.squeezer.util.Bundles;

import de.zbit.sbml.util.SBMLtools;
import de.zbit.util.ResourceManager;

/**
 * Represents the power-law modular rate law (PM) from Liebermeister et al.
 * 2010, see <a href="http://bioinformatics.oxfordjournals.org/content/26/12/1528">Bioinformatics</a>
 * 
 * @author Andreas Dr&auml;ger
 * @author Sarah R. M&uuml;ller vom Hagen
 * @date 2009-09-17
 * @since 1.3
 *
 */
public class PowerLawModularRateLaw extends BasicKineticLaw implements
InterfaceUniUniKinetics, InterfaceBiUniKinetics, InterfaceBiBiKinetics,
InterfaceArbitraryEnzymeKinetics, InterfaceReversibleKinetics,
InterfaceModulatedKinetics {
  
  /**
   * Localization support.
   */
  public static final transient ResourceBundle MESSAGES = ResourceManager.getBundle(Bundles.MESSAGES);
  
  /**
   * Generated serial version identifier.
   */
  private static final long serialVersionUID = -4196064500187986636L;
  
  /**
   * 
   * @param parentReaction
   * @param types
   * @throws RateLawNotApplicableException
   * @throws XMLStreamException
   */
  public PowerLawModularRateLaw(Reaction parentReaction, Object... types)
      throws RateLawNotApplicableException, XMLStreamException {
    super(parentReaction, types);
    SBMLtools.setSBOTerm(this, 531); // power law modular rate law
  }
  
  /* (non-Javadoc)
   * @see org.sbml.squeezer.kinetics.BasicKineticLaw#createKineticEquation(java.util.List, java.util.List, java.util.List, java.util.List)
   */
  @Override
  ASTNode createKineticEquation(List<String> modE, List<String> modActi,
    List<String> modInhib, List<String> modCat)
        throws RateLawNotApplicableException {
    int i;
    Reaction r = getParentSBMLObject();
    ASTNode activation = null, inhibition = null;
    for (ModifierSpeciesReference msr : r.getListOfModifiers()) {
      if (SBO.isStimulator(msr.getSBOTerm())) {
        ASTNode curr = null;
        if (SBO.isCatalyticActivator(msr.getSBOTerm())) {
          // complete activation
          curr = ASTNode.frac(speciesTerm(msr), ASTNode.sum(
            new ASTNode(parameterFactory.parameterKa(msr
              .getSpecies()), this), speciesTerm(msr))); // A
          // /
          // (A
          // +
          // Ka)
        } else if (SBO.isNonEssentialActivator(msr.getSBOTerm())) {
          // partial activation
          curr = ASTNode.frac(speciesTerm(msr), ASTNode.sum(
            new ASTNode(parameterFactory.parameterKa(msr
              .getSpecies()), this), speciesTerm(msr))); // A
          // /
          // (A
          // +
          // Ka)
          LocalParameter rhoA = parameterFactory.parameterRhoActivation(msr.getSpecies());
          
          ASTNode one = new ASTNode(1,this);
          SBMLtools.setUnits(one, Unit.Kind.DIMENSIONLESS);
          
          curr = ASTNode.sum(
            new ASTNode(rhoA, this),
            ASTNode.times(
              ASTNode.diff(one, new ASTNode(rhoA, this)),
              curr
                )
              );
        }
        if (activation == null) {
          activation = curr;
        } else if (curr != null) {
          activation.multiplyWith(curr);
        }
      } else if (SBO.isInhibitor(msr.getSBOTerm())) {
        ASTNode curr = null;
        if (SBO.isCompleteInhibitor(msr.getSBOTerm())) {
          LocalParameter kI = parameterFactory.parameterKi(msr
            .getSpecies());
          curr = ASTNode.frac(new ASTNode(kI, this), ASTNode.sum(
            new ASTNode(kI, this), speciesTerm(msr)));
        } else if (SBO.isPartialInhibitor(msr.getSBOTerm())) {
          // partial non-competetive inhibition
          LocalParameter kI = parameterFactory.parameterKi(msr
            .getSpecies());
          curr = ASTNode.frac(new ASTNode(kI, this), ASTNode.sum(
            new ASTNode(kI, this), speciesTerm(msr)));
          LocalParameter rhoI = parameterFactory.parameterRhoInhibition(msr.getSpecies());
          
          ASTNode one = new ASTNode(1,this);
          SBMLtools.setUnits(one, Unit.Kind.DIMENSIONLESS);
          
          curr = ASTNode.times(
            ASTNode.sum(
              new ASTNode(rhoI, this),
              ASTNode.diff(one, new ASTNode(rhoI, this))
                ),
                curr
              );
        }
        if (inhibition == null) {
          inhibition = curr;
        } else if (curr != null) {
          inhibition.multiplyWith(curr);
        }
      }
    }
    ASTNode numerator[] = new ASTNode[Math.max(1, modE.size())];
    for (i = 0; i < numerator.length; i++) {
      String enzymeID = modE.size() <= i ? null : modE.get(i);
      switch (type) {
        case cat: // CAT
          numerator[i] = cat(enzymeID);
          break;
        case hal: // HAL
          numerator[i] = hal(enzymeID);
          break;
        default: // WEG
          numerator[i] = weg(enzymeID);
          break;
      }
      numerator[i].divideBy(denominator(enzymeID));
      if (i < modE.size()) {
        ModifierSpeciesReference enzyme = null;
        for (int j = 0; j < r.getModifierCount() && enzyme == null; j++) {
          if (r.getModifier(j).getSpecies().equals(modE.get(i))) {
            enzyme = r.getModifier(j);
          }
        }
        numerator[i] = ASTNode.times(speciesTerm(enzyme), numerator[i]);
      }
    }
    return ASTNode.times(activation, inhibition, ASTNode.sum(numerator));
  }
  
  /**
   * The denominator of this kinetic law
   * 
   * @param r
   * @return
   */
  ASTNode denominator(String enzyme) {
    ASTNode denominator = new ASTNode(1, this);
    // TODO: is dimensionless the correct unit?
    //SBMLtools.setUnits(denominator, Unit.Kind.DIMENSIONLESS);
    ASTNode competInhib = specificModificationSummand(enzyme);
    return competInhib == null ? denominator : denominator.plus(competInhib);
  }
  
  /**
   * Creates the summand for competitive inhibition in the denominator.
   * 
   * @param enzyme
   * 
   * @param r
   * @return
   */
  ASTNode specificModificationSummand(String enzyme) {
    Reaction r = getParentSBMLObject();
    ASTNode inhib = null;
    ASTNode activ = null;
    for (ModifierSpeciesReference msr : r.getListOfModifiers()) {
      if (SBO.isCompetetiveInhibitor(msr.getSBOTerm())) {
        // specific inhibition
        LocalParameter kI = parameterFactory.parameterKi(msr
          .getSpecies(), enzyme);
        ASTNode curr = ASTNode.frac(speciesTerm(msr), new ASTNode(kI, this));
        if (inhib == null) {
          inhib = curr;
        } else {
          inhib.plus(curr);
        }
      } else if (SBO.isSpecificActivator(msr.getSBOTerm())) {
        LocalParameter kA = parameterFactory.parameterKa(msr
          .getSpecies(), enzyme);
        ASTNode curr = ASTNode.frac(new ASTNode(kA, this),
          speciesTerm(msr));
        if (activ == null) {
          activ = curr;
        } else {
          activ.plus(curr);
        }
      }
    }
    return ASTNode.sum(activ, inhib);
  }
  
  /**
   * Weg version of the numerator
   * 
   * @param enzyme
   * 
   * @param listOfReactants
   * @param listOfProducts
   * @return
   */
  private ASTNode weg(String enzyme) {
    ASTNode numerator = new ASTNode(parameterFactory.parameterVelocityConstant(enzyme), this);
    ASTNode denominator = createRoot(enzyme);
    Parameter R = parameterFactory.parameterGasConstant();
    Parameter T = parameterFactory.parameterTemperature();
    ASTNode exponent = null;
    ASTNode forward = null, backward = null;
    Parameter mu;
    LocalParameter hr = parameterFactory.parameterReactionCooperativity(enzyme);
    Reaction r = getParentSBMLObject();
    for (SpeciesReference specRef : r.getListOfReactants()) {
      ASTNode curr = speciesTerm(specRef);
      curr.raiseByThePowerOf(ASTNode.times(stoichiometryTerm(specRef), new ASTNode(hr, this)));
      if (forward == null) {
        forward = curr;
      } else {
        forward.multiplyWith(curr);
      }
      mu = parameterFactory.parameterStandardChemicalPotential(specRef.getSpecies());
      ASTNode product = ASTNode.times(stoichiometryTerm(specRef), new ASTNode(mu, this));
      if (exponent == null) {
        exponent = product;
      } else {
        exponent.plus(product);
      }
    }
    for (SpeciesReference specRef : r.getListOfProducts()) {
      if (r.getReversible()) {
        ASTNode curr = speciesTerm(specRef);
        curr.raiseByThePowerOf(ASTNode.times(stoichiometryTerm(specRef), new ASTNode(hr, this)));
        if (backward == null) {
          backward = curr;
        } else {
          backward.multiplyWith(curr);
        }
      }
      mu = parameterFactory.parameterStandardChemicalPotential(specRef.getSpecies());
      ASTNode product = ASTNode.times(stoichiometryTerm(specRef), new ASTNode(mu, this));
      if (exponent == null) {
        exponent = product;
      } else {
        exponent.plus(product);
      }
    }
    if (exponent == null) {
      exponent = new ASTNode(hr, this);
    } else {
      exponent = (new ASTNode(hr, this)).multiplyWith(exponent);
    }
    ASTNode two = new ASTNode(2, this);
    SBMLtools.setUnits(two, Unit.Kind.DIMENSIONLESS);
    exponent.divideBy(ASTNode.times(two, new ASTNode(R, this), new ASTNode(T, this)));
    exponent = ASTNode.exp(exponent);
    if (forward == null) {
      UnitFactory unitFactory = new UnitFactory(getModel(), isBringToConcentration());
      
      // TODO: correct?
      // forward must have the same unit as backward:
      // SubstancePerSizeOrSubstance^r.getListOfProducts().size()
      forward = new ASTNode(1, this);
      SBMLtools.setUnits(
        forward,
        unitFactory.unitSubstancePerSizeOrSubstance(
          getModel().getSpecies(enzyme), r.getListOfProducts().size()));
    }
    ASTNode swap = new ASTNode(ASTNode.Type.TIMES, this);
    swap.addChild(parameterFactory.parameterCStandard(this));
    swap.addChild(exponent);
    forward.divideBy(swap);
    if (r.getReversible()) {
      backward.multiplyWith(swap.clone());
      forward.minus(backward);
    }
    forward.divideBy(denominator.clone());
    forward = numerator.multiplyWith(forward);
    return forward;
  }
  
  /**
   * Hal version of the numerator
   * 
   * @param enzyme
   * 
   * @param listOfReactants
   * @param listOfProducts
   * @return
   */
  private ASTNode hal(String enzyme) {
    Reaction r = getParentSBMLObject();
    LocalParameter hr = parameterFactory.parameterReactionCooperativity(enzyme);
    LocalParameter keq = parameterFactory.parameterEquilibriumConstant(r);
    LocalParameter kvr = parameterFactory.parameterVelocityConstant(enzyme);
    ASTNode denominator = createRoot(enzyme);
    ASTNode forward = ASTNode.times(
      new ASTNode(kvr, this),
      ASTNode.sqrt(ASTNode.pow(new ASTNode(keq, this), new ASTNode(hr, this))));
    for (SpeciesReference specRef : r.getListOfReactants()) {
      forward.multiplyWith(
        ASTNode.pow(
          speciesTerm(specRef),
          ASTNode.times(stoichiometryTerm(specRef), new ASTNode(hr, this)
              ))
          );
    }
    forward.divideBy(denominator);
    if (r.getReversible()) {
      ASTNode backward = ASTNode.frac(
        new ASTNode(kvr, this),
        ASTNode.sqrt(ASTNode.pow(this, keq, hr)));
      for (SpeciesReference specRef : r.getListOfProducts()) {
        backward.multiplyWith(
          ASTNode.pow(
            speciesTerm(specRef),
            ASTNode.times(stoichiometryTerm(specRef), new ASTNode(hr, this))
              )
            );
      }
      backward.divideBy(denominator);
      forward.minus(backward);
    }
    return forward;
  }
  
  /**
   * Creates the root term for {@link TypeStandardVersion#hal} and {@link TypeStandardVersion#weg}.
   * 
   * @param enzyme
   * @return
   */
  private ASTNode createRoot(String enzyme) {
    ASTNode root = null, curr = null;
    LocalParameter kM = null;
    LocalParameter hr = parameterFactory.parameterReactionCooperativity(enzyme);
    Reaction r = getParentSBMLObject();
    for (SpeciesReference specRef : r.getListOfReactants()) {
      kM = parameterFactory.parameterMichaelis(specRef.getSpecies(), enzyme, true);
      curr = ASTNode.pow(new ASTNode(kM, this), ASTNode.times(stoichiometryTerm(specRef), new ASTNode(hr, this)));
      if (root == null) {
        root = curr;
      } else {
        root.multiplyWith(curr);
      }
    }
    if (r.getReversible()) {
      for (SpeciesReference specRef : r.getListOfProducts()) {
        kM = parameterFactory.parameterMichaelis(specRef.getSpecies(), enzyme, false);
        curr = ASTNode.pow(new ASTNode(kM, this), ASTNode.times(stoichiometryTerm(specRef), new ASTNode(hr, this)));
        if (root == null) {
          root = curr;
        } else {
          root.multiplyWith(curr);
        }
      }
    }
    return root.sqrt();
  }
  
  /**
   * Cat version of the numerator
   * 
   * @param enzyme
   * 
   * @param listOfReactants
   * @param listOfProducts
   * @return
   */
  private ASTNode cat(String enzyme) {
    ASTNode forward = cat(enzyme, true);
    if (getParentSBMLObject().getReversible()) {
      forward.minus(cat(enzyme, false));
    }
    return forward;
  }
  
  /**
   * This actually creates the rate for forward or backward cat version
   * equation.
   * 
   * @param enzyme
   * @param forward
   * @return
   */
  private ASTNode cat(String enzyme, boolean forward) {
    LocalParameter kr = parameterFactory.parameterKcatOrVmax(enzyme, forward);
    ASTNode rate = new ASTNode(kr, this);
    Reaction r = getParentSBMLObject();
    LocalParameter hr = parameterFactory.parameterReactionCooperativity(enzyme);
    for (SpeciesReference specRef : forward ? r.getListOfReactants() : r.getListOfProducts()) {
      LocalParameter kM = parameterFactory.parameterMichaelis(specRef.getSpecies(), enzyme, forward);
      rate.multiplyWith(ASTNode.pow(ASTNode.frac(speciesTerm(specRef),
        new ASTNode(kM, this)), ASTNode.times(stoichiometryTerm(specRef), new ASTNode(hr, this))));
    }
    return rate;
  }
  
  /* (non-Javadoc)
   * @see org.sbml.squeezer.kinetics.BasicKineticLaw#getSimpleName()
   */
  @Override
  public String getSimpleName() {
    return MESSAGES.getString("POWER_MODULAR_RATE_LAW_SIMPLE_NAME");
  }
  
}
