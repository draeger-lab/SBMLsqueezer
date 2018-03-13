/*
 * $Id: IrrevNonModulatedNonInteractingEnzymes.java 1077 2014-01-09 22:30:50Z draeger $
 * $URL: https://rarepos.cs.uni-tuebingen.de/svn-path/SBMLsqueezer/trunk/src/org/sbml/squeezer/kinetics/IrrevNonModulatedNonInteractingEnzymes.java $
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
import org.sbml.jsbml.Reaction;
import org.sbml.jsbml.SpeciesReference;
import org.sbml.jsbml.Unit;
import org.sbml.jsbml.util.Maths;
import org.sbml.squeezer.RateLawNotApplicableException;
import org.sbml.squeezer.util.Bundles;

import de.zbit.sbml.util.SBMLtools;
import de.zbit.util.ResourceManager;

/**
 * This class implements SBO:0000150 and all of its special cases. It is an
 * kinetic law of an irreversible enzyme reaction, which is not modulated, and
 * in which all reacting species do not interact: Kinetics of enzymes that react
 * with one or several substances, their substrates, that bind independently.
 * The enzymes do not catalyse the reactions in both directions.
 * 
 * @author Andreas Dr&auml;ger
 * @author Sarah R. M&uuml;ller vom Hagen
 * @date Feb 6, 2008
 * @since 1.0
 * @version $Rev: 1077 $
 */
public class IrrevNonModulatedNonInteractingEnzymes extends BasicKineticLaw
implements InterfaceIrreversibleKinetics, InterfaceUniUniKinetics,
InterfaceBiUniKinetics, InterfaceBiBiKinetics,
InterfaceArbitraryEnzymeKinetics, InterfaceIntegerStoichiometry {
  
  public static final transient ResourceBundle MESSAGES = ResourceManager.getBundle(Bundles.MESSAGES);
  public static final transient ResourceBundle WARNINGS = ResourceManager.getBundle(Bundles.WARNINGS);
  
  /**
   * Generated serial version identifier.
   */
  private static final long serialVersionUID = -7008903900757040218L;
  /**
   * 
   */
  private int numOfEnzymes;
  
  /**
   * 
   * @param parentReaction
   * @param typeParameters
   * @throws RateLawNotApplicableException
   * @throws XMLStreamException
   */
  public IrrevNonModulatedNonInteractingEnzymes(Reaction parentReaction,
    Object... typeParameters) throws RateLawNotApplicableException, XMLStreamException {
    super(parentReaction, typeParameters);
  }
  
  /* (non-Javadoc)
   * @see org.sbml.squeezer.kinetics.BasicKineticLaw#createKineticEquation(java.util.List, java.util.List, java.util.List, java.util.List)
   */
  @Override
  ASTNode createKineticEquation(List<String> modE, List<String> modActi,
    List<String> modInhib, List<String> modCat)
        throws RateLawNotApplicableException {
    if ((modActi.size() > 0) || (modInhib.size() > 0)) {
      throw new RateLawNotApplicableException(
        WARNINGS.getString("RATE_LAW_CAN_ONLY_APPLIED_TO_NON_MODULATED_REACTIONS"));
    }
    if ((modCat.size() > 0)) {
      throw new RateLawNotApplicableException(
        WARNINGS.getString("RATE_LAW_CAN_ONLY_APPLIED_TO_ENZYME_CATALYZED_REACTIONS"));
    }
    if (getParentSBMLObject().getReversible()) {
      throw new RateLawNotApplicableException(
        WARNINGS.getString("RATE_LAW_CAN_ONLY_APPLIED_TO_IRREVERSIBLE_REACTIONS"));
    }
    numOfEnzymes = modE.size();
    Reaction reaction = getParentSBMLObject();
    ASTNode enzymes[] = new ASTNode[Math.max(1, modE.size())];
    for (int enzymeNum = 0; enzymeNum < enzymes.length; enzymeNum++) {
      String enzyme = modE.size() == 0 ? null : modE.get(enzymeNum);
      LocalParameter p_kcat = parameterFactory.parameterKcatOrVmax(
        enzyme, true);
      ASTNode numerator = new ASTNode(p_kcat, this);
      
      ASTNode[] denominator = new ASTNode[reaction.getReactantCount()];
      for (int i = 0; i < reaction.getReactantCount(); i++) {
        SpeciesReference si = reaction.getReactant(i);
        if (!Maths.isInt(si.getStoichiometry())) {
          throw new RateLawNotApplicableException(
            WARNINGS.getString("RATE_LAW_CAN_ONLY_APPLIED_IF_REACTANTS_HAVE_INTEGER_STOICHIOMETRIES"));
        }
        LocalParameter p_kM = parameterFactory.parameterMichaelis(si
          .getSpecies(), enzyme, true);
        ASTNode frac = ASTNode.frac(speciesTerm(si), new ASTNode(p_kM,
          this));
        numerator = ASTNode.times(numerator, ASTNode.pow(ASTNode.frac(
          speciesTerm(si), new ASTNode(p_kM, this)), stoichiometryTerm(si)));
        
        // one must have the same unit as frac: speciesTerm (SubstancePerSizeOrSubstance)
        // divided by p_kM (SubstancePerSizeOrSubstance) = dimensionless
        ASTNode one = new ASTNode(1, this);
        SBMLtools.setUnits(one, Unit.Kind.DIMENSIONLESS);
        denominator[i] = ASTNode.pow(ASTNode.sum(one,frac), stoichiometryTerm(si));
      }
      
      if (modE.size() >= 1) {
        numerator = ASTNode.times(speciesTerm(enzyme), numerator);
      }
      enzymes[enzymeNum] = ASTNode.frac(numerator, ASTNode.times(denominator));
    }
    
    double stoichiometry = 0d;
    for (int i = 0; i < getParentSBMLObject().getReactantCount(); i++) {
      stoichiometry += getParentSBMLObject().getReactant(i).getStoichiometry();
    }
    switch ((int) Math.round(stoichiometry)) {
      case 1:
        SBMLtools.setSBOTerm(this, numOfEnzymes == 0 ? 199 : 29);
      case 2:
        SBMLtools.setSBOTerm(this, 151);
      case 3:
        SBMLtools.setSBOTerm(this, 152);
      default:
        SBMLtools.setSBOTerm(this, 150);
    }
    
    return ASTNode.sum(enzymes);
  }
  
  /* (non-Javadoc)
   * @see org.sbml.squeezer.kinetics.BasicKineticLaw#getSimpleName()
   */
  @Override
  public String getSimpleName() {
    return MESSAGES.getString("IRREV_NON_MODULATED_NON_INTERACTING_ENZYMES_SIMPLE_NAME");
  }
  
}
