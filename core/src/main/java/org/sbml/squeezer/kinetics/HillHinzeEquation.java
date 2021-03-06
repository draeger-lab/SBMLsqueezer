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
import org.sbml.jsbml.Species;
import org.sbml.jsbml.SpeciesReference;
import org.sbml.jsbml.Unit;
import org.sbml.squeezer.RateLawNotApplicableException;
import org.sbml.squeezer.ReactionType;
import org.sbml.squeezer.util.Bundles;

import de.zbit.sbml.util.SBMLtools;
import de.zbit.util.ResourceManager;

/**
 * This class creates a Hill equation as defined in the paper &ldquo;Hill
 * Kinetics meets P Systems: A Case Study on Gene Regulatory Networks as
 * Computing Agents in silico and in vivo&rdquo; of Hinze, T.; Hayat, S.;
 * Lenser, T.; Matsumaru, N., and Dittrich, P., 2007. (Aug 7, 2007)
 * 
 * @author Jochen Supper
 * @author Nadine Hassis
 * @author Andreas Dr&auml;ger
 * @author Sarah R. M&uuml;ller vom Hagen
 * @since 1.0
 * 
 */
public class HillHinzeEquation extends BasicKineticLaw implements
InterfaceGeneRegulatoryKinetics, InterfaceModulatedKinetics,
InterfaceIrreversibleKinetics, InterfaceReversibleKinetics,
InterfaceZeroReactants, InterfaceZeroProducts {
  
  public static final transient ResourceBundle MESSAGES = ResourceManager.getBundle(Bundles.MESSAGES);
  
  /**
   * Generated serial version identifier.
   */
  private static final long serialVersionUID = 5440459069184360541L;
  
  /**
   * 
   * @param parentReaction
   * @param typeParameters
   * @throws RateLawNotApplicableException
   * @throws XMLStreamException
   */
  public HillHinzeEquation(Reaction parentReaction, Object... typeParameters)
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
    // necessary due to the changes in CellDesigner from version 4.0 alpha
    // to beta and 4.0.1
    if (!modE.isEmpty()) {
      modActi.addAll(modE);
    }
    if (!modCat.isEmpty()) {
      modActi.addAll(modCat);
    }
    
    Reaction reaction = getParentSBMLObject();
    
    if ((reaction.getReactantCount() == 0) || (reaction.getModifierCount() == 0)) {
      SBMLtools.setSBOTerm(this,47);
    } else if (reaction.getReactantCount() == 1) {
      if (ReactionType.representsEmptySet(reaction.getListOfReactants())) {
        if ((reaction.getModifierCount() == 1)
            && !SBO.isInhibitor(reaction.getModifier(0).getSBOTerm())) {
          SBMLtools.setSBOTerm(this,195);
        }
      } else if (reaction.getModifierCount() == 0) {
        SBMLtools.setSBOTerm(this,195);
      }
    } else if ((reaction.getReactantCount() == 0)
        && (reaction.getModifierCount() == 1)
        && !SBO.isInhibitor(reaction.getModifier(0).getSBOTerm())) {
      SBMLtools.setSBOTerm(this,195);
    }
    
    if (SBO.isTranslation(reaction.getSBOTerm())
        || SBO.isTranscription(reaction.getSBOTerm())) {
      for (int i = 0; i < reaction.getReactantCount(); i++) {
        SpeciesReference reactant = reaction.getReactant(i);
        if (!SBO.isEmptySet(reactant.getSpeciesInstance().getSBOTerm())) {
          modActi.add(reactant.getSpecies());
        }
      }
    }
    ASTNode formula = createHillEquation(modActi, modInhib);
    // Influence of the concentrations of the reactants:
    for (int reactantNum = 0; reactantNum < reaction.getReactantCount(); reactantNum++) {
      Species reactant = reaction.getReactant(reactantNum).getSpeciesInstance();
      ASTNode gene;
      if (!SBO.isGeneOrGeneCodingRegion(reactant.getSBOTerm())) {
        gene = speciesTerm(reactant);
        SpeciesReference specRef = reaction.getReactant(reactantNum);
        if (specRef.isSetId() && (getLevel() > 2)) {
          // It might happen that there is an assignment rule that changes the stoichiometry.
          gene.raiseByThePowerOf(specRef);
          formula.multiplyWith(gene);
        } else if (specRef.getStoichiometry() != 1d) {
          // TODO: maybe changing stoichiometry by assignment rule?
          gene.raiseByThePowerOf(specRef.getStoichiometry());
          formula.multiplyWith(gene);
        }
      }
    }
    return formula;
  }
  
  /**
   * This method actually creates the Hill equation.
   * 
   * @param modTActi
   * @param modTInhib
   * @return
   */
  private ASTNode createHillEquation(List<String> modTActi,
    List<String> modTInhib) {
    ASTNode acti[] = new ASTNode[modTActi.size()];
    ASTNode inhib[] = new ASTNode[modTInhib.size()];
    
    // KS: half saturation constant.
    int i;
    for (i = 0; i < modTActi.size(); i++)
      /*
       * if (!model.getSpecies(modTActi.get(i)).getSpeciesAlias(0).getType()
       * .toUpperCase().equals("GENE"))
       */{
      LocalParameter p_hillcoeff = parameterFactory
          .parameterHillCoefficient(modTActi.get(i));
      LocalParameter p_kS = parameterFactory.parameterKS(getModel()
        .getSpecies(modTActi.get(i)), modTActi.get(i));
      acti[i] = ASTNode.times(ASTNode.frac(ASTNode.pow(
        speciesTerm(modTActi.get(i)),
        new ASTNode(p_hillcoeff, this)), ASTNode.sum(ASTNode.pow(
          speciesTerm(modTActi.get(i)),
          new ASTNode(p_hillcoeff, this)), ASTNode.pow(new ASTNode(
            p_kS, this), new ASTNode(p_hillcoeff, this)))));
    }
    for (i = 0; i < modTInhib.size(); i++)
      /*
       * if (!model.getSpecies(modTInhib.get(i)).getSpeciesAlias(0)
       * .getType().toUpperCase().equals("GENE"))
       */{
      LocalParameter p_hillcoeff = parameterFactory
          .parameterHillCoefficient(modTInhib.get(i));
      LocalParameter p_kS = parameterFactory.parameterKS(getModel()
        .getSpecies(modTInhib.get(i)), modTInhib.get(i));
      inhib[i] = ASTNode.frac(ASTNode.pow(speciesTerm(modTInhib.get(i)),
        new ASTNode(p_hillcoeff, this)), ASTNode.sum(ASTNode.pow(
          speciesTerm(modTInhib.get(i)), new ASTNode(p_hillcoeff,
            this)), ASTNode.pow(this, p_kS, p_hillcoeff)));
    }
    LocalParameter p_kg = parameterFactory.parameterVmax(true);
    
    ASTNode formula = new ASTNode(p_kg, this);
    if (modTActi.size() > 0) {
      formula.multiplyWith(acti);
    }
    if (modTInhib.size() > 0) {
      ASTNode one = new ASTNode(1,this);
      SBMLtools.setUnits(one, Unit.Kind.DIMENSIONLESS);
      formula.multiplyWith(ASTNode.diff(one, ASTNode.times(inhib)));
    }
    return formula;
  }
  
  /* (non-Javadoc)
   * @see org.sbml.squeezer.kinetics.BasicKineticLaw#getSimpleName()
   */
  @Override
  public String getSimpleName() {
    if (isSetSBOTerm() && SBO.isHillEquation(getSBOTerm())) {
      return MESSAGES.getString("HILL_EQUATION_SIMPLE_NAME");
    }
    return MESSAGES.getString("HILL_HINZE_EQUATION_SIMPLE_NAME");
  }
  
}
