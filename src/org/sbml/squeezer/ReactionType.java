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
package org.sbml.squeezer;

import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.sbml.jsbml.CVTerm.Qualifier;
import org.sbml.jsbml.ListOf;
import org.sbml.jsbml.ModifierSpeciesReference;
import org.sbml.jsbml.Reaction;
import org.sbml.jsbml.SBO;
import org.sbml.jsbml.Species;
import org.sbml.jsbml.SpeciesReference;
import org.sbml.squeezer.kinetics.BasicKineticLaw;
import org.sbml.squeezer.kinetics.ConvenienceKinetics;
import org.sbml.squeezer.kinetics.InterfaceArbitraryEnzymeKinetics;
import org.sbml.squeezer.kinetics.InterfaceBiBiKinetics;
import org.sbml.squeezer.kinetics.InterfaceBiUniKinetics;
import org.sbml.squeezer.kinetics.InterfaceGeneRegulatoryKinetics;
import org.sbml.squeezer.kinetics.InterfaceIntegerStoichiometry;
import org.sbml.squeezer.kinetics.InterfaceIrreversibleKinetics;
import org.sbml.squeezer.kinetics.InterfaceModulatedKinetics;
import org.sbml.squeezer.kinetics.InterfaceNonEnzymeKinetics;
import org.sbml.squeezer.kinetics.InterfaceReversibleKinetics;
import org.sbml.squeezer.kinetics.InterfaceUniUniKinetics;
import org.sbml.squeezer.kinetics.InterfaceZeroProducts;
import org.sbml.squeezer.kinetics.InterfaceZeroReactants;
import org.sbml.squeezer.util.Bundles;

import de.zbit.sbml.util.SBMLtools;
import de.zbit.util.ResourceManager;
import de.zbit.util.logging.LogUtil;

/**
 * The purpose of this class is to analyze a reaction object for several
 * properties, such as the number of reactants and products, the types of
 * modifiers and their number, if the stoichiometries are always integer numbers
 * or real valued and much more. With this information at hand the reaction type
 * is able to suggest applicable kinetic equations from the list of available
 * kinetic laws and also to select the default kinetic equation for this
 * specific type. This class can also answer several questions regarding a
 * reaction, for instance, whether a reaction is follows a bi-bi mechanism. This
 * can be used for GUIs to display reaction properties to a user.
 * 
 * @author Andreas Dr&auml;ger
 * @author Sarah R. M&uuml;ller vom Hagen
 * @date 2009-10-01
 * @since 1.3
 * @version $Rev$
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
public class ReactionType {
  
  /**
   * A {@link Logger} for this class.
   */
  private static final transient Logger logger = Logger.getLogger(ReactionType.class.getName());
  /**
   * Localization support
   */
  public static final transient ResourceBundle MESSAGES = ResourceManager.getBundle(Bundles.MESSAGES);
  
  /**
   * Set of those kinetic equations that can only be reversible.
   */
  private static Set<Class> notIrreversible;
  
  /**
   * Set of kinetic equations that can only be applied to irreversible
   * reactions.
   */
  private static Set<Class> notReversible;
  
  /**
   * {@link Set}s of kinetics with certain characteristics.
   */
  private static Set<Class> setOfKineticsForArbitraryEnzymeMechanism, setOfKineticsForBiUniReactions,
  setOfKineticsForGeneRegulatoryNetworks, setOfKineticsForIntStoichiometry,
  setOfKineticsForIrreversibleReactions, setOfKineticsForModulatedReactions, setOfKineticsForNonEnzymeReactions,
  setOfKineticsForReversibleReactions, setOfKineticsForUniUniReactions, setOfKineticsForZeroProducts,
  setOfKineticsForZeroReactants, setOfKineticsForBiBiReactions;
  
  /**
   * 
   */
  public static final transient ResourceBundle WARNINGS = ResourceManager.getBundle(Bundles.WARNINGS);
  
  /**
   * Initializes the sets of not reversible and not irreversible kinetic
   * equations.
   */
  static {
    ListOf.setDebugMode(true);
    
    /*
     * Load all available kinetic equations and the user's settings from the
     * configuration file.
     */
    long time = System.currentTimeMillis();
    logger.info("Loading kinetic equations...");
    setOfKineticsForBiBiReactions = new HashSet<Class>();
    setOfKineticsForBiUniReactions = new HashSet<Class>();
    setOfKineticsForGeneRegulatoryNetworks = new HashSet<Class>();
    setOfKineticsForNonEnzymeReactions = new HashSet<Class>();
    setOfKineticsForArbitraryEnzymeMechanism = new HashSet<Class>();
    setOfKineticsForUniUniReactions = new HashSet<Class>();
    setOfKineticsForReversibleReactions = new HashSet<Class>();
    setOfKineticsForIrreversibleReactions = new HashSet<Class>();
    setOfKineticsForZeroReactants = new HashSet<Class>();
    setOfKineticsForZeroProducts = new HashSet<Class>();
    setOfKineticsForModulatedReactions = new HashSet<Class>();
    setOfKineticsForIntStoichiometry = new HashSet<Class>();
    // removed Reflect for JavaWebStart
    /*Class<BasicKineticLaw> classes[] = Reflect.getAllClassesInPackage(
        KINETICS_PACKAGE.getName(), false, true, BasicKineticLaw.class,
        JAR_LOCATION, true);*/
    Class[] classes = {
        org.sbml.squeezer.kinetics.AdditiveModelLinear.class,
        org.sbml.squeezer.kinetics.AdditiveModelNonLinear.class,
        org.sbml.squeezer.kinetics.CommonModularRateLaw.class,
        org.sbml.squeezer.kinetics.ConvenienceKinetics.class,
        org.sbml.squeezer.kinetics.DirectBindingModularRateLaw.class,
        org.sbml.squeezer.kinetics.ForceDependentModularRateLaw.class,
        org.sbml.squeezer.kinetics.GeneralizedMassAction.class,
        org.sbml.squeezer.kinetics.HSystem.class,
        org.sbml.squeezer.kinetics.HillEquation.class,
        org.sbml.squeezer.kinetics.HillHinzeEquation.class,
        org.sbml.squeezer.kinetics.HillRaddeEquation.class,
        org.sbml.squeezer.kinetics.IrrevCompetNonCooperativeEnzymes.class,
        org.sbml.squeezer.kinetics.IrrevNonModulatedNonInteractingEnzymes.class,
        org.sbml.squeezer.kinetics.MichaelisMenten.class,
        org.sbml.squeezer.kinetics.NetGeneratorLinear.class,
        org.sbml.squeezer.kinetics.NetGeneratorNonLinear.class,
        org.sbml.squeezer.kinetics.OrderedMechanism.class,
        org.sbml.squeezer.kinetics.PingPongMechanism.class,
        org.sbml.squeezer.kinetics.PowerLawModularRateLaw.class,
        org.sbml.squeezer.kinetics.RandomOrderMechanism.class,
        // org.sbml.squeezer.kinetics.RestrictedSpaceKinetics.class,
        org.sbml.squeezer.kinetics.SSystem.class,
        org.sbml.squeezer.kinetics.SimultaneousBindingModularRateLaw.class,
        org.sbml.squeezer.kinetics.Vohradsky.class,
        org.sbml.squeezer.kinetics.Weaver.class,
        org.sbml.squeezer.kinetics.ZerothOrderForwardGMAK.class,
        org.sbml.squeezer.kinetics.ZerothOrderReverseGMAK.class
    };
    
    for (Class<BasicKineticLaw> c : classes) {
      Set<Class<?>> s = new HashSet<Class<?>>();
      for (Class<?> interf : c.getInterfaces()) {
        s.add(interf);
      }
      if (s.contains(InterfaceIrreversibleKinetics.class)) {
        setOfKineticsForIrreversibleReactions.add(c);
      }
      if (s.contains(InterfaceReversibleKinetics.class)) {
        setOfKineticsForReversibleReactions.add(c);
      }
      if (s.contains(InterfaceUniUniKinetics.class)) {
        setOfKineticsForUniUniReactions.add(c);
      }
      if (s.contains(InterfaceBiUniKinetics.class)) {
        setOfKineticsForBiUniReactions.add(c);
      }
      if (s.contains(InterfaceBiBiKinetics.class)) {
        setOfKineticsForBiBiReactions.add(c);
      }
      if (s.contains(InterfaceArbitraryEnzymeKinetics.class)) {
        setOfKineticsForArbitraryEnzymeMechanism.add(c);
      }
      if (s.contains(InterfaceGeneRegulatoryKinetics.class)) {
        setOfKineticsForGeneRegulatoryNetworks.add(c);
      }
      if (s.contains(InterfaceNonEnzymeKinetics.class)) {
        setOfKineticsForNonEnzymeReactions.add(c);
      }
      if (s.contains(InterfaceZeroReactants.class)) {
        setOfKineticsForZeroReactants.add(c);
      }
      if (s.contains(InterfaceZeroProducts.class)) {
        setOfKineticsForZeroProducts.add(c);
      }
      if (s.contains(InterfaceModulatedKinetics.class)) {
        setOfKineticsForModulatedReactions.add(c);
      }
      if (s.contains(InterfaceIntegerStoichiometry.class)) {
        setOfKineticsForIntStoichiometry.add(c);
      }
    }
    logger.info(MessageFormat.format(MESSAGES.getString("DONE_IN_MS"), (System.currentTimeMillis() - time)));
    
    notIrreversible = new HashSet<Class>();
    notIrreversible.addAll(getKineticsReversible());
    notIrreversible.removeAll(getKineticsIrreversible());
    notReversible = new HashSet<Class>();
    notReversible.addAll(getKineticsIrreversible());
    notReversible.removeAll(getKineticsReversible());
  }
  
  /**
   * Checks if the given set of kinetics can be used given the property if all
   * reactions should be treated reversibly.
   * 
   * @param treatReactionsReversible
   * @param set
   * @return A set of kinetics that can be used given the reversible property.
   */
  private static Set<Class> checkReactions(boolean treatReactionsReversible,
    Set<Class> set) {
    Set<Class> kinetics = new HashSet<Class>();
    kinetics.addAll(set);
    kinetics.removeAll(notReversible);
    if (!treatReactionsReversible) {
      kinetics.removeAll(notIrreversible);
    }
    /*
     * else kinetics.retainAll(notIrreversible);
     */
    return kinetics;
  }
  
  
  /**
   * 
   * @param treatReactionsReversible
   * @return
   */
  public static Set<Class> getKineticsArbitraryEnzyme(
    boolean treatReactionsReversible) {
    return checkReactions(treatReactionsReversible, getKineticsArbitraryEnzymeMechanism());
  }
  
  /**
   * @return the kineticsArbitraryEnzymeMechanism
   */
  public static Set<Class> getKineticsArbitraryEnzymeMechanism() {
    return setOfKineticsForArbitraryEnzymeMechanism;
  }
  
  /**
   * @return the kineticsBiBi
   */
  public static Set<Class> getKineticsBiBi() {
    return setOfKineticsForBiBiReactions;
  }
  
  /**
   * 
   * @param treatReactionsReversible
   * @return
   */
  public static Set<Class> getKineticsBiBi(boolean treatReactionsReversible) {
    return checkReactions(treatReactionsReversible, getKineticsBiBi());
  }
  
  /**
   * @return the kineticsBiUni
   */
  public static Set<Class> getKineticsBiUni() {
    return setOfKineticsForBiUniReactions;
  }
  
  /**
   * 
   * @param treatReactionsReversible
   * @return
   */
  public static Set<Class> getKineticsBiUni(boolean treatReactionsReversible) {
    return checkReactions(treatReactionsReversible, getKineticsBiUni());
  }
  
  /**
   * 
   * @param treatReactionsReversible
   * @return
   */
  public static Set<Class> getKineticsGeneRegulation(
    boolean treatReactionsReversible) {
    return checkReactions(treatReactionsReversible, getKineticsGeneRegulatoryNetworks());
  }
  
  /**
   * @return the kineticsGeneRegulatoryNetworks
   */
  public static Set<Class> getKineticsGeneRegulatoryNetworks() {
    return setOfKineticsForGeneRegulatoryNetworks;
  }
  
  /**
   * @return the kineticsIntStoichiometry
   */
  public static Set<Class> getKineticsIntStoichiometry() {
    return setOfKineticsForIntStoichiometry;
  }
  
  /**
   * @return the kineticsIrreversible
   */
  public static Set<Class> getKineticsIrreversible() {
    return setOfKineticsForIrreversibleReactions;
  }
  
  /**
   * 
   * @param type
   * @return
   */
  public static Set<Class> getKineticsIrreversible(Set<Class> type) {
    Set<Class> rev = new HashSet<Class>(type);
    rev.retainAll(setOfKineticsForIrreversibleReactions);
    return rev;
  }
  
  /**
   * 
   * @return
   */
  public static Set<Class> getKineticsIrreversibleArbitraryEnzymeMechanism() {
    return getKineticsIrreversible(setOfKineticsForArbitraryEnzymeMechanism);
  }
  
  /**
   * 
   * @return
   */
  public static Set<Class> getKineticsIrreversibleBiBi() {
    return getKineticsIrreversible(setOfKineticsForBiBiReactions);
  }
  
  /**
   * 
   * @return
   */
  public static Set<Class> getKineticsIrreversibleBiUni() {
    return getKineticsIrreversible(setOfKineticsForBiUniReactions);
  }
  
  /**
   * 
   * @return
   */
  public static Set<Class> getKineticsIrreversibleNonEnzyme() {
    return getKineticsIrreversible(setOfKineticsForNonEnzymeReactions);
  }
  
  /**
   * 
   * @return
   */
  public static Set<Class> getKineticsIrreversibleUniUni() {
    return getKineticsIrreversible(setOfKineticsForUniUniReactions);
  }
  
  /**
   * @return the kineticsModulated
   */
  public static Set<Class> getKineticsModulated() {
    return setOfKineticsForModulatedReactions;
  }
  
  /**
   * @return the kineticsNonEnzyme
   */
  public static Set<Class> getKineticsNonEnzyme() {
    return setOfKineticsForNonEnzymeReactions;
  }
  
  
  /**
   * 
   * @param treatReactionsReversible
   * @return
   */
  public static Set<Class> getKineticsNonEnzyme(
    boolean treatReactionsReversible) {
    return checkReactions(treatReactionsReversible, getKineticsNonEnzyme());
  }
  
  /**
   * @return the kineticsReversible
   */
  public static Set<Class> getKineticsReversible() {
    return setOfKineticsForReversibleReactions;
  }
  
  /**
   * 
   * @param type
   * @return
   */
  public static Set<Class> getKineticsReversible(Set<Class> type) {
    Set<Class> rev = new HashSet<Class>(type);
    rev.retainAll(setOfKineticsForReversibleReactions);
    return rev;
  }
  
  /**
   * 
   * @return
   */
  public static Set<Class> getKineticsReversibleArbitraryEnzymeMechanism() {
    return getKineticsReversible(setOfKineticsForArbitraryEnzymeMechanism);
  }
  
  /**
   * 
   * @return
   */
  public static Set<Class> getKineticsReversibleBiBi() {
    return getKineticsReversible(setOfKineticsForBiBiReactions);
  }
  
  /**
   * 
   * @return
   */
  public static Set<Class> getKineticsReversibleBiUni() {
    return getKineticsReversible(setOfKineticsForBiUniReactions);
  }
  
  /**
   * 
   * @return
   */
  public static Set<Class> getKineticsReversibleNonEnzyme() {
    return getKineticsReversible(setOfKineticsForNonEnzymeReactions);
  }
  
  /**
   * 
   * @return
   */
  public static Set<Class> getKineticsReversibleUniUni() {
    return getKineticsReversible(setOfKineticsForUniUniReactions);
  }
  
  /**
   * @return the kineticsUniUni
   */
  public static Set<Class> getKineticsUniUni() {
    return setOfKineticsForUniUniReactions;
  }
  
  /**
   * 
   * @param treatReactionsReversible
   * @return
   */
  public static Set<Class> getKineticsUniUni(boolean treatReactionsReversible) {
    return checkReactions(treatReactionsReversible, getKineticsUniUni());
  }
  
  /**
   * @return the kineticsZeroProducts
   */
  public static Set<Class> getKineticsZeroProducts() {
    return setOfKineticsForZeroProducts;
  }
  
  /**
   * @return the kineticsZeroReactants
   */
  public static Set<Class> getKineticsZeroReactants() {
    return setOfKineticsForZeroReactants;
  }
  
  /**
   * Identify which Modifier is used.
   * 
   * @param reaction
   * @param enzymes
   * @param activators
   * @param inhibitors
   * @param nonEnzymeCatalysts
   */
  public static final void identifyModifers(Reaction reaction,
    List<String> enzymes, List<String> activators,
    List<String> inhibitors, List<String> nonEnzymeCatalysts) {
    enzymes.clear();
    activators.clear();
    inhibitors.clear();
    nonEnzymeCatalysts.clear();
    int type;
    for (ModifierSpeciesReference modifier : reaction.getListOfModifiers()) {
      type = modifier.getSBOTerm();
      // if (SBO.isModifier(type)) {
      // Ok, this is confusing...
      // inhibitors.add(modifier.getSpecies());
      // activators.add(modifier.getSpecies());
      // }
      if (SBO.isCatalyst(type) || SBO.isChildOf(type, SBO.getCatalysis())) {
        if (SBO.isEnzymaticCatalysis(type)) {
          if (LogUtil.getCurrentLogLevel().intValue() <=  Level.FINE.intValue()) {
            logger.fine(MessageFormat.format(MESSAGES.getString("IS_ENZYMATIC_CATALYST"), modifier));
          }
          enzymes.add(modifier.getSpecies());
        } else {
          if (LogUtil.getCurrentLogLevel().intValue() <=  Level.FINE.intValue()) {
            logger.fine(MessageFormat.format(MESSAGES.getString("IS_NON_ENZYMATIC_CATALYST"), modifier));
          }
          nonEnzymeCatalysts.add(modifier.getSpecies());
        }
      } else if (SBO.isTranscriptionalInhibitor(type)
          || SBO.isTranslationalInhibitor(type)) {
        if (LogUtil.getCurrentLogLevel().intValue() <=  Level.FINE.intValue()) {
          logger.fine(MessageFormat.format(MESSAGES.getString("IS_TRANSCRIPTIONAL_OR_TRANSLATIONAL_INHIBITOR"), modifier));
        }
        inhibitors.add(modifier.getSpecies());
      } else if (SBO.isInhibitor(type) || SBO.isChildOf(type, SBO.getInhibition())) {
        if (LogUtil.getCurrentLogLevel().intValue() <=  Level.FINE.intValue()) {
          logger.fine(MessageFormat.format(MESSAGES.getString("IS_INHIBITOR"), modifier));
        }
        inhibitors.add(modifier.getSpecies());
      } else if (SBO.isTranscriptionalActivation(type)
          || SBO.isTranslationalActivation(type)) {
        if (LogUtil.getCurrentLogLevel().intValue() <=  Level.FINE.intValue()) {
          logger.fine(MessageFormat.format(MESSAGES.getString("IS_TRANSCRIPTIONAL_OR_TRANSLATIONAL_ACTIVATOR"), modifier));
        }
        activators.add(modifier.getSpecies());
      } else if (SBO.isTrigger(type) || SBO.isStimulator(type)
          || SBO.isChildOf(type, SBO.getStimulation())
          || SBO.isChildOf(type, SBO.getNecessaryStimulation())) {
        // no extra support for unknown catalysis anymore...
        // physical stimulation is now also a stimulator.
        if (LogUtil.getCurrentLogLevel().intValue() <=  Level.FINE.intValue()) {
          logger.fine(MessageFormat.format(MESSAGES.getString("IS_ACTIVATOR"), modifier));
        }
        activators.add(modifier.getSpecies());
      }
    }
  }
  
  /**
   * 
   * @param reaction
   * @return
   */
  public static double productOrder(Reaction reaction) {
    double stoichiometryRight = 0d;
    for (SpeciesReference specRef : reaction.getListOfProducts()) {
      stoichiometryRight += specRef.getStoichiometry();
    }
    return stoichiometryRight;
  }
  
  /**
   * 
   * Analyze properties of the reaction: compute stoichiometric properties.
   * 
   * @param reaction
   */
  public static double reactantOrder(Reaction reaction) {
    double stoichiometryLeft = 0d;
    for (SpeciesReference specRef : reaction.getListOfReactants()) {
      stoichiometryLeft += specRef.getStoichiometry();
    }
    return stoichiometryLeft;
  }
  
  /**
   * Convenient method to check whether the given ListOf is either empty or
   * contains only elements with an SBO annotation that corresponds to an
   * empty set.
   * 
   * @param listOf
   * @return
   */
  public static boolean representsEmptySet(ListOf<SpeciesReference> listOf) {
    if ((listOf == null) || (listOf.size() == 0)
        || (listOf.isSetSBOTerm() && SBO.isEmptySet(listOf.getSBOTerm()))) {
      return true;
    }
    boolean emptySet = true;
    for (SpeciesReference specRef : listOf) {
      emptySet &= SBO.isEmptySet(specRef.getSpeciesInstance().getSBOTerm());
    }
    return emptySet;
  }
  
  private boolean allReactionsAsEnzymeCatalyzed;
  
  /**
   * 
   */
  private boolean biBi, biUni, integerStoichiometry, nonEnzyme,
  reactionWithGenes = false, reactionWithRNAs = false, reversibility,
  stoichiometryIntLeft = true, uniUni, withoutModulation;
  
  /**
   * 
   */
  private List<String> enzymes, inhibitors, nonEnzymeCatalysts, activators;
  
  /**
   * 
   */
  private Reaction reaction;
  /**
   * 
   */
  private double stoichiometryLeft = 0d, stoichiometryRight = 0d;
  
  
  /**
   * Analyzes the given {@link Reaction} for several properties with respect to the given settings.
   * 
   * @param r
   *        The reaction to be analyzed.
   * @param reversibility
   * @param allReactionsAsEnzymeCatalyzed
   * @param boundaryConditionForGenes
   * @param ignoreList
   * @throws RateLawNotApplicableException
   */
  public ReactionType(Reaction r, boolean reversibility,
    boolean allReactionsAsEnzymeCatalyzed, boolean boundaryConditionForGenes, String... ignoreList)
        throws RateLawNotApplicableException {
    
    int i;
    reaction = r;
    this.allReactionsAsEnzymeCatalyzed = allReactionsAsEnzymeCatalyzed;
    removeSpeciesAccordingToIgnoreList(reaction, ignoreList);
    this.reversibility = reversibility;
    
    /*
     * Analyze properties of the reaction: compute stoichiometric properties
     */
    double stoichiometry;
    for (i = 0; i < reaction.getReactantCount(); i++) {
      stoichiometry = reaction.getReactant(i).getStoichiometry();
      stoichiometryLeft += stoichiometry;
      if (((int) stoichiometry) - stoichiometry != 0d) {
        stoichiometryIntLeft = false;
      }
      // Transcription or translation?
      Species reactantSpecies = reaction.getReactant(i).getSpeciesInstance();
      if (SBO.isGeneOrGeneCodingRegion(reactantSpecies.getSBOTerm())) {
        reactionWithGenes = true;
      } else if (SBO.isRNAOrMessengerRNA(reactantSpecies.getSBOTerm())) {
        reactionWithRNAs = true;
      }
    }
    
    // is at least one modifier a gene or RNA?
    for (ModifierSpeciesReference msr : reaction.getListOfModifiers()) {
      if (SBO.isGeneOrGeneCodingRegion(msr.getSpeciesInstance().getSBOTerm())) {
        reactionWithGenes = true;
        break;
      }
      if (SBO.isRNAOrMessengerRNA(msr.getSpeciesInstance().getSBOTerm())) {
        reactionWithRNAs = true;
        break;
      }
    }
    
    // boolean stoichiometryIntRight = true;
    for (i = 0; i < reaction.getProductCount(); i++) {
      stoichiometry = reaction.getProduct(i).getStoichiometry();
      stoichiometryRight += stoichiometry;
      // if (((int) stoichiometry) - stoichiometry != 0.0)
      // stoichiometryIntRight = false;
    }
    
    // identify types of modifiers
    nonEnzymeCatalysts = new LinkedList<String>();
    inhibitors = new LinkedList<String>();
    activators = new LinkedList<String>();
    enzymes = new LinkedList<String>();
    if (reaction.getModifierCount() > 0) {
      identifyModifers(reaction, enzymes, activators, inhibitors,
        nonEnzymeCatalysts);
    }
    nonEnzyme = (!allReactionsAsEnzymeCatalyzed && enzymes.size() == 0)
        || (nonEnzymeCatalysts.size() > 0)
        || (((reaction.getProductCount() == 0)
            || (SBO.isEmptySet(reaction.getProduct(0).getSpeciesInstance().getSBOTerm()))) && reaction.isReversible());
    
    uniUni = (stoichiometryLeft == 1d) && (stoichiometryRight == 1d);
    biUni = (stoichiometryLeft == 2d) && (stoichiometryRight == 1d);
    biBi = (stoichiometryLeft == 2d) && (stoichiometryRight == 2d);
    integerStoichiometry = stoichiometryIntLeft;
    withoutModulation = (inhibitors.size() == 0) && (activators.size() == 0);
    
    /*
     * Check if this reaction makes sense at all.
     */
    if (reactionWithGenes
        || (reaction.getReactantCount() == 0)
        || ((reaction.getReactantCount() == 1) && SBO.isEmptySet(reaction
          .getReactant(0).getSBOTerm()))) {
      boolean transcription = false;
      for (i = 0; i < reaction.getProductCount(); i++) {
        Species species = reaction.getProduct(i).getSpeciesInstance();
        if (SBO.isRNAOrMessengerRNA(species.getSBOTerm())) {
          transcription = reactionWithRNAs = true;
        }
      }
      if (transcription && SBO.isTranslation(reaction.getSBOTerm())) {
        throw new RateLawNotApplicableException(MessageFormat.format(
          WARNINGS.getString("REACTION_MUST_BE_TRANSRIPTION"), reaction.getId()));
      }
    }
    if (reactionWithRNAs
        || (reaction.getReactantCount() == 0)
        || ((reaction.getReactantCount() == 1) && SBO.isEmptySet(reaction
          .getReactant(0).getSBOTerm()))) {
      boolean translation = false;
      for (i = 0; i < reaction.getProductCount() && !translation; i++) {
        Species product = reaction.getProduct(i).getSpeciesInstance();
        if (SBO.isProtein(product.getSBOTerm())
            || SBO.isGeneric(product.getSBOTerm())) {
          translation = reactionWithRNAs = true;
        }
      }
      if (SBO.isTranscription(reaction.getSBOTerm()) && translation) {
        throw new RateLawNotApplicableException(MessageFormat.format(
          WARNINGS.getString("REACTION_MUST_BE_TRANSLATION"), reaction.getId()));
      }
    }
    if (uniUni) {
      Species species = reaction.getReactant(0).getSpeciesInstance();
      if (SBO.isGeneOrGeneCodingRegion(species.getSBOTerm())) {
        if (boundaryConditionForGenes) {
          setBoundaryCondition(species, true);
        }
        if (SBO.isTranslation(reaction.getSBOTerm())) {
          throw new RateLawNotApplicableException(MessageFormat.format(
            WARNINGS.getString("REACTION_MUST_BE_TRANSRIPTION"), reaction.getId()));
        }
      } else if (SBO.isRNAOrMessengerRNA(species.getSBOTerm())) {
        if (SBO.isTranscription(reaction.getSBOTerm())) {
          throw new RateLawNotApplicableException(MessageFormat.format(
            WARNINGS.getString("REACTION_MUST_BE_TRANSLATION"), reaction.getId()));
        }
      }
    }
    if ((SBO.isTranslation(reaction.getSBOTerm()) || SBO.isTranscription(reaction.getSBOTerm()))
        && !(reactionWithGenes || reactionWithRNAs)) {
      throw new RateLawNotApplicableException(MessageFormat.format(
        WARNINGS.getString("REACTION_MUST_BE_TRANSITION"), reaction.getId()));
    }
  }
  
  /**
   * Checks whether for this reaction the given kinetic law can be applied
   * just based on the reversibility property (nothing else is checked).
   * 
   * @param reaction
   * @param clazz
   * @return
   */
  private boolean checkReversibility(Reaction reaction, Class<?> clazz) {
    return (reaction.isReversible() && getKineticsReversible().contains(clazz))
        || (!reaction.isReversible() && getKineticsIrreversible().contains(clazz));
  }
  
  /**
   * @return the activators
   */
  public List<String> getActivators() {
    return activators;
  }
  
  /**
   * @return the enzymes
   */
  public List<String> getEnzymes() {
    return enzymes;
  }
  
  /**
   * @return the inhibitors
   */
  public List<String> getInhibitors() {
    return inhibitors;
  }
  
  /**
   * @return the nonEnzymeCatalysts
   */
  public List<String> getNonEnzymeCatalysts() {
    return nonEnzymeCatalysts;
  }
  
  /**
   * @return the reaction
   */
  public Reaction getReaction() {
    return reaction;
  }
  
  /**
   * @return the stoichiometryLeft
   */
  public double getStoichiometryLeft() {
    return stoichiometryLeft;
  }
  
  /**
   * @return the stoichiometryRight
   */
  public double getStoichiometryRight() {
    return stoichiometryRight;
  }
  
  /**
   * identify the reactionType for generating the kinetics
   * @param kineticsGeneRegulation
   * @param kineticsZeroReactants
   * @param kineticsZeroProducts
   * @param kineticsReversibleNonEnzymeReaction
   * @param kineticsIrreversibleNonEnzymeReaction
   * @param kineticsReversibleArbitraryEnzymeReaction
   * @param kineticsIrreversibleArbitraryEnzymeReaction
   * @param kineticsIrreversibleUniUniType
   * @param kineticsReversibleUniUniType
   * @param kineticsReversibleBiUniType
   * @param kineticsIrreversibleBiUniType
   * @param kineticsReversibleBiBiType
   * @param kineticsIrreversibleBiBiType
   * 
   */
  public Class<? extends BasicKineticLaw> identifyPossibleKineticLaw(Class<? extends BasicKineticLaw> kineticsGeneRegulation,
    Class<? extends BasicKineticLaw> kineticsZeroReactants,
    Class<? extends BasicKineticLaw> kineticsZeroProducts,
    Class<? extends BasicKineticLaw> kineticsReversibleNonEnzymeReaction,
    Class<? extends BasicKineticLaw> kineticsIrreversibleNonEnzymeReaction,
    Class<? extends BasicKineticLaw> kineticsReversibleArbitraryEnzymeReaction,
    Class<? extends BasicKineticLaw> kineticsIrreversibleArbitraryEnzymeReaction,
    Class<? extends BasicKineticLaw> kineticsReversibleUniUniType,
    Class<? extends BasicKineticLaw> kineticsIrreversibleUniUniType,
    Class<? extends BasicKineticLaw> kineticsReversibleBiUniType,
    Class<? extends BasicKineticLaw> kineticsIrreversibleBiUniType,
    Class<? extends BasicKineticLaw> kineticsReversibleBiBiType,
    Class<? extends BasicKineticLaw> kineticsIrreversibleBiBiType) {
    if (representsEmptySet(reaction.getListOfReactants())) {
      if (reactionWithGenes || reactionWithRNAs) {
        if (getKineticsZeroReactants().contains(kineticsGeneRegulation)) {
          return kineticsGeneRegulation;
        }
        for (Class<? extends BasicKineticLaw> kin : getKineticsGeneRegulatoryNetworks()) {
          if (getKineticsZeroReactants().contains(kin)) {
            if (!kin.equals(kineticsGeneRegulation) && !kin.equals(kineticsZeroReactants)) {
              logger.warning(MessageFormat.format(
                WARNINGS.getString("FALLBACK_KINETICS"),
                SBMLtools.getName(reaction),
                kineticsZeroReactants.getSimpleName(), kin.getSimpleName()));
            }
            return kin;
          }
        }
      }
      return kineticsZeroReactants;
    }
    if (representsEmptySet(reaction.getListOfProducts())
        && (reversibility || reaction.isReversible())) {
      if (reactionWithGenes || reactionWithRNAs) {
        for (Class<? extends BasicKineticLaw> kin : getKineticsGeneRegulatoryNetworks()) {
          if (getKineticsZeroReactants().contains(kin)) {
            if (!kin.equals(kineticsGeneRegulation) && !kin.equals(kineticsZeroProducts)) {
              logger.warning(MessageFormat.format(
                WARNINGS.getString("FALLBACK_KINETICS"),
                SBMLtools.getName(reaction),
                kineticsZeroProducts.getSimpleName(), kin.getSimpleName()));
            }
            return kin;
          }
        }
      }
      return kineticsZeroProducts;
    }
    
    boolean enzymeCatalyzed = allReactionsAsEnzymeCatalyzed || (enzymes.size() > 0);
    Class<? extends BasicKineticLaw> whichkin = reaction.isReversible() || reversibility ? kineticsReversibleNonEnzymeReaction : kineticsIrreversibleNonEnzymeReaction;
    
    if (enzymeCatalyzed) {
      if (reaction.isReversible() || reversibility) {
        whichkin = kineticsReversibleArbitraryEnzymeReaction;
      } else if (!getKineticsModulated().contains(kineticsIrreversibleArbitraryEnzymeReaction) && !isWithoutModulation()) {
        whichkin = kineticsIrreversibleArbitraryEnzymeReaction;
      } else {
        // TODO: Make this selectable!
        whichkin = ConvenienceKinetics.class;
        if (!whichkin.equals(kineticsIrreversibleArbitraryEnzymeReaction)) {
          logger.warning(MessageFormat.format(
            WARNINGS.getString("FALLBACK_KINETICS"), SBMLtools.getName(reaction),
            kineticsIrreversibleArbitraryEnzymeReaction.getSimpleName(),
            whichkin.getSimpleName()));
        }
      }
    }
    if (stoichiometryLeft == 1d) {
      Species reactant = reaction.getReactant(0).getSpeciesInstance();
      if (stoichiometryRight == 1d) {
        Species product = reaction.getProduct(0).getSpeciesInstance();
        if ((reactionWithGenes || reactionWithRNAs || (SBO.isGeneOrGeneCodingRegion(reactant.getSBOTerm()) ||
            (SBO.isEmptySet(reactant.getSBOTerm()) && (SBO.isProtein(product.getSBOTerm())
                || SBO.isGeneric(product.getSBOTerm()) || SBO.isRNAOrMessengerRNA(product.getSBOTerm()))))
                && !(SBO.isEmptySet(product.getSBOTerm())))) {
          whichkin = kineticsGeneRegulation;
        }
      }
      if (!whichkin.equals(kineticsGeneRegulation)
          && (((enzymes.size() > 0) || enzymeCatalyzed) && ((stoichiometryRight == 1d) || !(reaction
              .isReversible() || reversibility)))) {
        whichkin = reaction.isReversible() || reversibility ? kineticsReversibleUniUniType : kineticsIrreversibleUniUniType;
      }
    } else if (biUni && enzymeCatalyzed) {
      whichkin = reaction.isReversible() || reversibility ? kineticsReversibleBiUniType : kineticsIrreversibleBiUniType;
    } else if (biBi && enzymeCatalyzed) {
      whichkin = reaction.isReversible() || reversibility ? kineticsReversibleBiBiType : kineticsIrreversibleBiBiType;
    }
    return whichkin;
  }
  
  /**
   * 
   * @return Returns a sorted array of possible kinetic equations for the
   *         given reaction in the model (the names of the implementing
   *         classes).
   */
  public Class<?>[] identifyPossibleKineticLaws() {
    Set<Class> types = new HashSet<Class>();
    boolean emptyListOfReactants = representsEmptySet(reaction.getListOfReactants());
    boolean emptyListOfProducts = representsEmptySet(reaction.getListOfProducts());
    boolean enzymeKinetics = false;
    if (emptyListOfReactants
        || ((reaction.isReversible() || reversibility) && emptyListOfProducts)) {
      /*
       * Special case that occurs if we have at least one empty list of
       * species references.
       */
      if (emptyListOfReactants) {
        for (Class<?> className : getKineticsZeroReactants()) {
          if (!reactionWithGenes && !reactionWithRNAs) {
            types.add(className);
          }
        }
      } else {
        for (Class<?> className : getKineticsZeroProducts()) {
          if (getKineticsReversible().contains(className) && !reactionWithGenes && !reactionWithRNAs) {
            types.add(className);
          }
        }
      }
      // Gene-regulation
      if (reactionWithGenes || reactionWithRNAs) {
        for (Class<?> className : getKineticsGeneRegulatoryNetworks()) {
          if (((reaction.isReversible() && !notReversible.contains(className))
              || (!reaction.isReversible() && !notIrreversible.contains(className))
              && (!emptyListOfReactants || setOfKineticsForZeroReactants.contains(className))
              && (!emptyListOfProducts || setOfKineticsForZeroProducts.contains(className)))) {
            types.add(className);
          }
        }
      } else if (!nonEnzyme && !emptyListOfReactants) {
        enzymeKinetics = true;
      } else {
        nonEnzyme = true;
      }
      
    } else {
      if (nonEnzyme) {
        // non enzyme reactions
        types.addAll(getKineticsNonEnzyme());
      } else {
        enzymeKinetics = true;
      }
      
      /*
       * Gene regulation
       */
      if (uniUni) {
        Species reactant = reaction.getReactant(0).getSpeciesInstance();
        Species product = reaction.getProduct(0).getSpeciesInstance();
        if ((reactionWithGenes || reactionWithRNAs ||
            (SBO.isGeneOrGeneCodingRegion(reactant.getSBOTerm()) ||
                (SBO.isEmptySet(reactant.getSBOTerm()) &&
                    (SBO.isRNAOrMessengerRNA(product.getSBOTerm())
                        || SBO.isProtein(product.getSBOTerm()) ||
                        SBO.isGeneric(product.getSBOTerm())))))) {
          for (Class<?> className : getKineticsGeneRegulatoryNetworks()) {
            if ((reaction.isReversible() && !notReversible.contains(className))
                || (!reaction.isReversible() && !notIrreversible.contains(className))) {
              types.add(className);
            }
          }
        }
      }
    }
    if (enzymeKinetics) {
      /*
       * Enzym-Kinetics: Assign possible rate laws for arbitrary enzyme
       * reations.
       */
      boolean irreversibleWithEmptyListOfProducts = !reaction.isReversible() && emptyListOfProducts;
      for (Class<?> className : getKineticsArbitraryEnzymeMechanism()) {
        if (checkReversibility(reaction, className) && !irreversibleWithEmptyListOfProducts
            && (!getKineticsIntStoichiometry().contains(className) || integerStoichiometry)
            && (getKineticsModulated().contains(className) || withoutModulation)) {
          types.add(className);
        }
      }
      if (uniUni || ((stoichiometryLeft == 1d) && !(reaction.isReversible() || reversibility))) {
        Set<Class> onlyUniUni = new HashSet<Class>();
        onlyUniUni.addAll(getKineticsUniUni());
        onlyUniUni.removeAll(getKineticsArbitraryEnzymeMechanism());
        if (!integerStoichiometry) {
          onlyUniUni.removeAll(getKineticsIntStoichiometry());
        }
        if (!withoutModulation) {
          onlyUniUni.retainAll(getKineticsModulated());
        }
        for (Class<?> className : onlyUniUni) {
          if (checkReversibility(reaction, className)) {
            types.add(className);
          }
        }
      } else if (biUni || ((stoichiometryLeft == 2d) && !(reaction.isReversible() || reversibility))) {
        Set<Class> onlyBiUni = new HashSet<Class>();
        onlyBiUni.addAll(getKineticsBiUni());
        onlyBiUni.removeAll(getKineticsArbitraryEnzymeMechanism());
        if (!integerStoichiometry) {
          onlyBiUni.removeAll(getKineticsIntStoichiometry());
        }
        if (!withoutModulation) {
          onlyBiUni.retainAll(getKineticsModulated());
        }
        for (Class<?> className : onlyBiUni) {
          if (checkReversibility(reaction, className)) {
            types.add(className);
          }
        }
      } else if (biBi) {
        Set<Class> onlyBiBi = new HashSet<Class>();
        onlyBiBi.addAll(getKineticsBiBi());
        onlyBiBi.removeAll(getKineticsArbitraryEnzymeMechanism());
        if (!withoutModulation) {
          onlyBiBi.retainAll(getKineticsModulated());
        }
        for (Class<?> className : onlyBiBi) {
          if (checkReversibility(reaction, className)) {
            types.add(className);
          }
        }
      }
    }
    Class<?> t[] = types.toArray(new Class[] {});
    Arrays.sort(t, new Comparator<Class<?>>() {
      
      /* (non-Javadoc)
       * @see java.util.Comparator#compare(T, T)
       */
      @Override
      public int compare(Class<?> clazz1, Class<?> clazz2) {
        return clazz1.getName().compareTo(clazz2.getName());
      }
      
    });
    return t;
  }
  
  /**
   * @return the biBi
   */
  public boolean isBiBi() {
    return biBi;
  }
  
  /**
   * @return the biUni
   */
  public boolean isBiUni() {
    return biUni;
  }
  
  /**
   * Returns true only if this reaction should be considered enzyme-catalyzed
   * (independent of any settings, based on the SBO annotation of the current
   * model).
   * 
   * @return
   */
  public boolean isEnzymeReaction() {
    return ((enzymes.size() > 0) && (nonEnzymeCatalysts.size() == 0));
  }
  
  /**
   * @return the integerStoichiometry
   */
  public boolean isIntegerStoichiometry() {
    return integerStoichiometry;
  }
  
  /**
   * @return the nonEnzyme
   */
  public boolean isNonEnzyme() {
    return nonEnzyme;
  }
  
  /**
   * Returns true only if given the current settings this reaction cannot be
   * considered enzyme-catalyzed.
   * 
   * @return
   */
  public boolean isNonEnzymeReaction() {
    return (enzymes.size() == 0) && (nonEnzymeCatalysts.size() > 0);
  }
  
  /**
   * @return the reactionWithGenes
   */
  public boolean isReactionWithGenes() {
    return reactionWithGenes;
  }
  
  /**
   * @return the reactionWithRNAs
   */
  public boolean isReactionWithRNAs() {
    return reactionWithRNAs;
  }
  
  /**
   * @return the stoichiometryIntLeft
   */
  public boolean isStoichiometryIntLeft() {
    return stoichiometryIntLeft;
  }
  
  /**
   * @return the uniUni
   */
  public boolean isUniUni() {
    return uniUni;
  }
  
  /**
   * @return the withoutModulation
   */
  public boolean isWithoutModulation() {
    return withoutModulation;
  }
  
  /**
   * 
   * @param listOfParticipants
   * @param ignoreList
   */
  private static void removeSpeciesAccordingToIgnoreList(
    ListOf<SpeciesReference> listOfParticipants, String... ignoreList) {
    Species spec;
    for (int i = listOfParticipants.size() - 1; i >= 0; i--) {
      spec = listOfParticipants.get(i).getSpeciesInstance();
      for (String string : ignoreList) {
        if (spec.filterCVTerms(Qualifier.BQB_IS, ".*" + string + ".*").size() > 0) {
          SpeciesReference sr = listOfParticipants.remove(i);
          logger.fine("Removed " + sr + " from reaction " + listOfParticipants.getParent());
          break;
        }
      }
    }
  }
  
  /**
   * 
   * @param r
   * @param ignoreList
   */
  public static void removeSpeciesAccordingToIgnoreList(
    Reaction r, String... ignoreList) {
    if ((ignoreList != null) && (ignoreList.length >= 0)) {
      removeSpeciesAccordingToIgnoreList(r.getListOfReactants(), ignoreList);
      removeSpeciesAccordingToIgnoreList(r.getListOfProducts(), ignoreList);
    }
  }
  
  /**
   * set the boundaryCondition for a gene to the given value
   * 
   * @param species
   * @param condition
   */
  public void setBoundaryCondition(Species species, boolean condition) {
    if (condition != species.getBoundaryCondition()) {
      species.setBoundaryCondition(condition);
    }
  }
  
}
