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
package org.sbml.squeezer.sabiork.wizard.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import org.sbml.jsbml.ASTNode;
import org.sbml.jsbml.CVTerm;
import org.sbml.jsbml.CVTerm.Qualifier;
import org.sbml.jsbml.CallableSBase;
import org.sbml.jsbml.Compartment;
import org.sbml.jsbml.FunctionDefinition;
import org.sbml.jsbml.KineticLaw;
import org.sbml.jsbml.ListOf;
import org.sbml.jsbml.LocalParameter;
import org.sbml.jsbml.Model;
import org.sbml.jsbml.Parameter;
import org.sbml.jsbml.Reaction;
import org.sbml.jsbml.SBase;
import org.sbml.jsbml.Species;
import org.sbml.jsbml.SpeciesReference;
import org.sbml.jsbml.UnitDefinition;
import org.sbml.jsbml.util.filters.CVTermFilter;

/**
 * A class that provides the possibility to import a {@link KineticLaw} of
 * another {@link Reaction} into a given {@link Reaction}.
 * 
 * @author Matthias Rall
 * 
 * @since 2.0
 */
public class KineticLawImporter {
  
  private KineticLaw kineticLaw;
  private Reaction reaction;
  private Model reactionModel;
  private HashSet<Compartment> referencedCompartments;
  private HashSet<FunctionDefinition> referencedFunctionDefinitions;
  private HashSet<LocalParameter> referencedLocalParameters;
  private HashSet<Parameter> referencedParameters;
  private HashSet<Reaction> referencedReactions;
  private HashSet<Species> referencedSpecies;
  private HashSet<SpeciesReference> referencedSpeciesReferences;
  private HashSet<UnitDefinition> referencedUnitDefinitions;
  private HashSet<Compartment> referenceableCompartments;
  private HashSet<Reaction> referenceableReactions;
  private HashSet<Species> referenceableSpecies;
  private HashSet<SpeciesReference> referenceableSpeciesReferences;
  private HashMap<CallableSBase, CallableSBase> matches;
  private StringBuilder report;
  
  /**
   * 
   * @param kineticLaw
   * @param reaction
   */
  public KineticLawImporter(KineticLaw kineticLaw, Reaction reaction) {
    this.kineticLaw = kineticLaw;
    this.reaction = reaction;
    reactionModel = reaction.getModel();
    referencedCompartments = new HashSet<Compartment>();
    referencedFunctionDefinitions = new HashSet<FunctionDefinition>();
    referencedLocalParameters = new HashSet<LocalParameter>();
    referencedParameters = new HashSet<Parameter>();
    referencedReactions = new HashSet<Reaction>();
    referencedSpecies = new HashSet<Species>();
    referencedSpeciesReferences = new HashSet<SpeciesReference>();
    referencedUnitDefinitions = new HashSet<UnitDefinition>();
    referenceableCompartments = new HashSet<Compartment>();
    referenceableReactions = new HashSet<Reaction>();
    referenceableSpecies = new HashSet<Species>();
    referenceableSpeciesReferences = new HashSet<SpeciesReference>();
    matches = new HashMap<CallableSBase, CallableSBase>();
    report = new StringBuilder();
    initialize();
  }
  
  /**
   * 
   */
  private void initialize() {
    addReferencedComponents();
    addReferenceableComponents();
    addMatches();
  }
  
  /**
   * Adds all components referenced by the {@link KineticLaw}.
   */
  private void addReferencedComponents() {
    if (kineticLaw.isSetMath()) {
      addReferencedComponents(kineticLaw.getMath());
      for (LocalParameter referencedLocalParameter : referencedLocalParameters) {
        if (referencedLocalParameter.isSetUnitsInstance()) {
          referencedUnitDefinitions.add(referencedLocalParameter
            .getUnitsInstance());
        }
      }
      for (Parameter referencedParameter : referencedParameters) {
        if (referencedParameter.isSetUnitsInstance()) {
          referencedUnitDefinitions.add(referencedParameter.getUnitsInstance());
        }
      }
    }
  }
  
  /**
   * Adds all components referenced by the {@link KineticLaw}.
   */
  private void addReferencedComponents(ASTNode node) {
    if (node.isString()) {
      CallableSBase variable = getVariable(node);
      if (variable instanceof Compartment) {
        referencedCompartments.add((Compartment) variable);
      }
      if (variable instanceof FunctionDefinition) {
        referencedFunctionDefinitions.add((FunctionDefinition) variable);
      }
      if (variable instanceof LocalParameter) {
        referencedLocalParameters.add((LocalParameter) variable);
      }
      if (variable instanceof Parameter) {
        referencedParameters.add((Parameter) variable);
      }
      if (variable instanceof Reaction) {
        referencedReactions.add((Reaction) variable);
      }
      if (variable instanceof Species) {
        referencedSpecies.add((Species) variable);
      }
      if (variable instanceof SpeciesReference) {
        referencedSpeciesReferences.add((SpeciesReference) variable);
      }
    }
    for (ASTNode child : node.getChildren()) {
      addReferencedComponents(child);
    }
  }
  
  /**
   * Adds all referenceable components of the given {@link Reaction}
   */
  private void addReferenceableComponents() {
    if (reactionModel.isSetListOfReactions()) {
      referenceableReactions.addAll(reactionModel.getListOfReactions());
    }
    if (reactionModel.isSetListOfSpecies()) {
      for (Species species : reactionModel.getListOfSpecies()) {
        if (reaction.hasReactant(species) || reaction.hasModifier(species)
            || reaction.hasProduct(species)) {
          referenceableSpecies.add(species);
          if (species.isSetCompartment()) {
            referenceableCompartments.add(species.getCompartmentInstance());
          }
        }
      }
    }
    if (reaction.isSetListOfReactants()) {
      referenceableSpeciesReferences.addAll(reaction.getListOfReactants());
    }
    if (reaction.isSetListOfProducts()) {
      referenceableSpeciesReferences.addAll(reaction.getListOfProducts());
    }
  }
  
  /**
   * Adds all matches between the components referenced by the
   * {@link KineticLaw} and the referenceable components of the given
   * {@link Reaction}.
   */
  private void addMatches() {
    for (Compartment referencedCompartment : referencedCompartments) {
      matches.put(referencedCompartment, null);
    }
    for (Reaction referencedReaction : referencedReactions) {
      matches.put(referencedReaction, null);
    }
    for (Species referencedSpecie : referencedSpecies) {
      matches.put(referencedSpecie, null);
    }
    for (SpeciesReference referencedSpeciesReference : referencedSpeciesReferences) {
      matches.put(referencedSpeciesReference, null);
    }
    for (Compartment referencedCompartment : referencedCompartments) {
      for (Compartment referenceableCompartment : referenceableCompartments) {
        if (isMatch(referencedCompartment, referenceableCompartment)) {
          matches.put(referencedCompartment, referenceableCompartment);
          break;
        }
      }
    }
    for (Reaction referencedReaction : referencedReactions) {
      for (Reaction referenceableReaction : referenceableReactions) {
        if (isMatch(referencedReaction, referenceableReaction)) {
          matches.put(referencedReaction, referenceableReaction);
          break;
        }
      }
    }
    for (Species referencedSpecie : referencedSpecies) {
      for (Species referenceableSpecie : referenceableSpecies) {
        if (isMatch(referencedSpecie, referenceableSpecie)) {
          matches.put(referencedSpecie, referenceableSpecie);
          break;
        }
      }
    }
    for (SpeciesReference referencedSpeciesReference : referencedSpeciesReferences) {
      for (SpeciesReference referenceableSpeciesReference : referenceableSpeciesReferences) {
        if (isMatch(referencedSpeciesReference, referenceableSpeciesReference)) {
          matches.put(referencedSpeciesReference, referenceableSpeciesReference);
          break;
        }
      }
    }
  }
  
  /**
   * Returns all cvterm resources of a given component with regard to the
   * specified qualifier.
   * 
   * @param qualifier
   * @param component
   * @return
   */
  private List<String> getCVTermResources(Qualifier qualifier, SBase component) {
    List<String> cvTermResources = new ArrayList<String>();
    CVTermFilter cvTermFilter = new CVTermFilter(qualifier);
    for (CVTerm cvTerm : component.getCVTerms()) {
      if (cvTermFilter.accepts(cvTerm)) {
        cvTermResources.addAll(cvTerm.getResources());
      }
    }
    return cvTermResources;
  }
  
  /**
   * Returns {@code true} if at least one cvterm resource of component1 and
   * component2 is equal with regard to the specified qualifier .
   * 
   * @param qualifier1
   * @param component1
   * @param qualifier2
   * @param component2
   * @return
   */
  private boolean hasCommonCVTermResource(Qualifier qualifier1,
    SBase component1, Qualifier qualifier2, SBase component2) {
    boolean hasCommonCVTermResource = false;
    List<String> cvTermResources1 = getCVTermResources(qualifier1, component1);
    List<String> cvTermResources2 = getCVTermResources(qualifier2, component2);
    for (String cvTermResource1 : cvTermResources1) {
      if (cvTermResources2.contains(cvTermResource1)) {
        hasCommonCVTermResource = true;
        break;
      }
    }
    return hasCommonCVTermResource;
  }
  
  /**
   * @param compartment1
   * @param compartment2
   * @return {@code true} if both {@link Compartment} can be considered a
   * match.
   */
  private boolean isMatch(Compartment compartment1, Compartment compartment2) {
    return (hasCommonCVTermResource(Qualifier.BQB_IS, compartment1,
      Qualifier.BQB_IS, compartment2) || referenceableCompartments.size() == 1);
  }
  
  /**
   * @param reaction1
   * @param reaction2
   * @return {@code true} if both {@link Reaction} can be considered a match.
   */
  private boolean isMatch(Reaction reaction1, Reaction reaction2) {
    return hasCommonCVTermResource(Qualifier.BQB_IS, reaction1,
      Qualifier.BQB_IS, reaction2);
  }
  
  /**
   * @param species1
   * @param species2
   * @return {@code true} if both {@link Species} can be considered a match.
   */
  private boolean isMatch(Species species1, Species species2) {
    return (hasCommonCVTermResource(Qualifier.BQB_IS, species1,
      Qualifier.BQB_IS, species2)
      || hasCommonCVTermResource(Qualifier.BQB_HAS_VERSION, species1,
        Qualifier.BQB_IS, species2) || hasCommonCVTermResource(
          Qualifier.BQB_IS, species1, Qualifier.BQB_HAS_VERSION, species2));
  }
  
  /**
   * @param speciesReference1
   * @param speciesReference2
   * @return {@code true} if both {@link SpeciesReference} can be considered a
   * match.
   */
  private boolean isMatch(SpeciesReference speciesReference1,
    SpeciesReference speciesReference2) {
    if (speciesReference1.isSetSpeciesInstance()
        && speciesReference2.isSetSpeciesInstance()) {
      return isMatch(speciesReference1.getSpeciesInstance(),
        speciesReference2.getSpeciesInstance());
    } else {
      return false;
    }
  }
  
  /**
   * Sets a referenced component and a referenceable component as a match.
   * 
   * @param referencedComponent
   * @param referenceableComponent
   */
  public void match(CallableSBase referencedComponent,
    CallableSBase referenceableComponent) {
    if (referencedCompartments.contains(referencedComponent)
        && referenceableCompartments.contains(referenceableComponent)) {
      matches.put(referencedComponent, referenceableComponent);
    }
    if (referencedReactions.contains(referencedComponent)
        && referenceableReactions.contains(referenceableComponent)) {
      matches.put(referencedComponent, referenceableComponent);
    }
    if (referencedSpecies.contains(referencedComponent)
        && referenceableSpecies.contains(referenceableComponent)) {
      matches.put(referencedComponent, referenceableComponent);
    }
    if (referencedSpeciesReferences.contains(referencedComponent)
        && referenceableSpeciesReferences.contains(referenceableComponent)) {
      matches.put(referencedComponent, referenceableComponent);
    }
  }
  
  /**
   * Returns the matching component of the referenced component.
   * 
   * @param referencedComponent
   * @return
   */
  public CallableSBase getMatchingReferenceableComponent(
    CallableSBase referencedComponent) {
    CallableSBase match = null;
    if (matches.containsKey(referencedComponent)) {
      match = matches.get(referencedComponent);
    }
    return match;
  }
  
  /**
   * Returns {@code true} if it is possible to import the
   * {@link KineticLaw} into the {@link Reaction}.
   * 
   * @return
   */
  public boolean isImportableKineticLaw() {
    for (CallableSBase match : matches.keySet()) {
      if (matches.get(match) == null) { return false; }
    }
    return true;
  }
  
  /**
   * Performs the import of the {@link KineticLaw} into the {@link Reaction}.
   */
  public void importKineticLaw() {
    String miriamURN = null;
    CVTermFilter filter = new CVTermFilter(Qualifier.BQB_IS_DESCRIBED_BY,
        "urn:miriam:sabiork.kineticrecord:");
    for (CVTerm term : kineticLaw.getCVTerms()) {
      if (filter.accepts(term)) {
        miriamURN = term.getResourceURI(0);
      }
    }
    report.append("Reaction " + reaction.getId() + ":\n");
    
    HashMap<UnitDefinition, UnitDefinition> unitDefinitions = new HashMap<UnitDefinition, UnitDefinition>();
    for (UnitDefinition referencedUnitDefinition : referencedUnitDefinitions) {
      UnitDefinition referencedUnitDefinitionCopy = referencedUnitDefinition
          .clone();
      referencedUnitDefinitionCopy
      .setId(getUniqueUnitDefinitionID(referencedUnitDefinitionCopy.getId()));
      referencedUnitDefinitionCopy.setLevel(reactionModel.getLevel());
      referencedUnitDefinitionCopy.setVersion(reactionModel.getVersion());
      referencedUnitDefinitionCopy.unsetListOfUnits();
      report.append("    - UnitDefinition "
          + referencedUnitDefinitionCopy.getId() + " will be imported.\n");
      if (miriamURN != null) {
        referencedUnitDefinitionCopy.addCVTerm(new CVTerm(
          Qualifier.BQB_IS_PART_OF, miriamURN));
      }
      reactionModel.addUnitDefinition(referencedUnitDefinitionCopy);
      unitDefinitions.put(referencedUnitDefinition,
        referencedUnitDefinitionCopy);
    }
    
    boolean otherReactionDirection = false;
    for (Entry<CallableSBase, CallableSBase> match : matches.entrySet()) {
      if ((reaction.isReversible()) && (match.getKey() instanceof Species)) {
        Species s1 = (Species) match.getKey();
        Species s2 = (Species) match.getValue();
        if ((reaction.getReactantForSpecies(s1.getId()) != null)
            && (kineticLaw.getParent().getProductForSpecies(s2.getId()) != null)
            || (reaction.getReactantForSpecies(s2.getId()) != null)
            && (kineticLaw.getParent().getProductForSpecies(s1.getId()) != null)) {
          otherReactionDirection = true;
        }
      }
      replaceKineticLawReferences(match.getKey(), match.getValue());
      
    }
    
    for (FunctionDefinition referencedFunctionDefinition : referencedFunctionDefinitions) {
      FunctionDefinition referencedFunctionDefinitionCopy = referencedFunctionDefinition
          .clone();
      referencedFunctionDefinitionCopy
      .setId(getUniqueID(referencedFunctionDefinitionCopy.getId()));
      referencedFunctionDefinitionCopy.setLevel(reactionModel.getLevel());
      referencedFunctionDefinitionCopy.setVersion(reactionModel.getVersion());
      if (miriamURN != null) {
        referencedFunctionDefinitionCopy.addCVTerm(new CVTerm(
          Qualifier.BQB_IS_PART_OF, miriamURN));
      }
      report.append("    - FunctionDefinition "
          + referencedFunctionDefinitionCopy.getId() + " will be imported.\n");
      reactionModel.addFunctionDefinition(referencedFunctionDefinitionCopy);
      replaceKineticLawReferences(referencedFunctionDefinition,
        referencedFunctionDefinitionCopy);
    }
    
    for (Parameter referencedParameter : referencedParameters) {
      Parameter referencedParameterCopy = referencedParameter.clone();
      referencedParameterCopy
      .setId(getUniqueID(referencedParameterCopy.getId()));
      referencedParameterCopy.setLevel(reactionModel.getLevel());
      referencedParameterCopy.setVersion(reactionModel.getVersion());
      if (unitDefinitions.containsKey(referencedParameter.getUnitsInstance())) {
        referencedParameterCopy.setUnits(unitDefinitions
          .get(referencedParameter.getUnitsInstance()));
      }
      report.append("    - Parameter " + referencedParameterCopy.getId()
        + " will be imported.\n");
      reactionModel.addParameter(referencedParameterCopy);
      replaceKineticLawReferences(referencedParameter, referencedParameterCopy);
    }
    
    ListOf<LocalParameter> localParameters = new ListOf<LocalParameter>();
    localParameters.setLevel(reactionModel.getLevel());
    localParameters.setVersion(reactionModel.getVersion());
    for (LocalParameter referencedLocalParameter : referencedLocalParameters) {
      LocalParameter referencedLocalParameterCopy = referencedLocalParameter
          .clone();
      referencedLocalParameterCopy
      .setId(getUniqueID(referencedLocalParameterCopy.getId()));
      referencedLocalParameterCopy.setLevel(reactionModel.getLevel());
      referencedLocalParameterCopy.setVersion(reactionModel.getVersion());
      if (unitDefinitions.containsKey(referencedLocalParameter
        .getUnitsInstance())) {
        referencedLocalParameterCopy.setUnits(unitDefinitions
          .get(referencedLocalParameter.getUnitsInstance()));
      }
      report.append("    - LocalParameter "
          + referencedLocalParameterCopy.getId() + " will be imported.\n");
      localParameters.add(referencedLocalParameterCopy);
      replaceKineticLawReferences(referencedLocalParameter,
        referencedLocalParameterCopy);
    }
    kineticLaw.unsetListOfLocalParameters();
    
    KineticLaw kineticLawCopy = kineticLaw.clone();
    
    if (otherReactionDirection) {
      ASTNode math = new ASTNode(ASTNode.Type.TIMES);
      math.addChild(kineticLawCopy.getMath());
      math.addChild(new ASTNode(-1));
      kineticLawCopy.setMath(math);
    }
    
    kineticLawCopy
    .setMetaId(getUniqueMetaID(reaction.getId() + "_KineticLaw"));
    kineticLawCopy.setLevel(reactionModel.getLevel());
    kineticLawCopy.setVersion(reactionModel.getVersion());
    kineticLawCopy.setListOfLocalParameters(localParameters);
    report.append("    - KineticLaw " + kineticLawCopy.getMetaId()
      + " will be imported and set as KineticLaw.");
    reaction.setKineticLaw(kineticLawCopy);
  }
  
  /**
   * Replaces a reference in the {@link KineticLaw} with a new reference.
   * 
   * @param oldReference
   * @param newReference
   */
  private void replaceKineticLawReferences(CallableSBase oldReference,
    CallableSBase newReference) {
    if (kineticLaw.isSetMath()) {
      replaceKineticLawReferences(kineticLaw.getMath(), oldReference,
        newReference);
    }
  }
  
  private void replaceKineticLawReferences(ASTNode node,
    CallableSBase oldReference, CallableSBase newReference) {
    if (node.isString()) {
      CallableSBase variable = node.getVariable();
      if (variable != null) {
        if (variable.equals(oldReference)) {
          node.setName(newReference.getName());
          node.setVariable(newReference);
        }
      }
    }
    for (ASTNode child : node.getChildren()) {
      replaceKineticLawReferences(child, oldReference, newReference);
    }
  }
  
  /**
   * Returns {@code true} if the given id is a unique id.
   * 
   * @param id
   * @return
   */
  private boolean isUniqueID(String id) {
    return (reactionModel.findNamedSBase(id) == null);
  }
  
  /**
   * Returns {@code true} if the given id is a unique unit definition id.
   * 
   * @param id
   * @return
   */
  private boolean isUniqueUnitDefinitionID(String id) {
    return (reactionModel.findUnitDefinition(id) == null);
  }
  
  /**
   * Returns {@code true} if the given id is a unique meta id.
   * 
   * @param id
   * @return
   */
  private boolean isUniqueMetaID(String id) {
    return !reactionModel.getSBMLDocument().containsMetaId(id);
  }
  
  /**
   * Returns the id if it is a unique id. Otherwise a unique id is generated and
   * returned.
   * 
   * @param id
   * @return
   */
  private String getUniqueID(String id) {
    String oldID = id;
    String newID = id;
    for (int i = 1; !isUniqueID(newID); i++) {
      newID = oldID + i;
    }
    return newID;
  }
  
  /**
   * Returns the id if it is a unique unit definition id. Otherwise a unique
   * unit definition id is generated and returned.
   * 
   * @param id
   * @return
   */
  private String getUniqueUnitDefinitionID(String id) {
    String oldID = id;
    String newID = id;
    for (int i = 1; !isUniqueUnitDefinitionID(newID); i++) {
      newID = oldID + i;
    }
    return newID;
  }
  
  /**
   * Returns the id if it is a unique meta id. Otherwise a unique meta id is
   * generated and returned.
   * 
   * @param id
   * @return
   */
  private String getUniqueMetaID(String id) {
    String oldID = id;
    String newID = id;
    for (int i = 1; !isUniqueMetaID(newID); i++) {
      newID = oldID + i;
    }
    return newID;
  }
  
  /**
   * 
   * @return
   */
  public KineticLaw getKineticLaw() {
    return kineticLaw;
  }
  
  /**
   * 
   * @return
   */
  public Reaction getReaction() {
    return reaction;
  }
  
  /**
   * 
   * @return
   */
  public HashSet<Compartment> getReferencedCompartments() {
    return referencedCompartments;
  }
  
  /**
   * 
   * @return
   */
  public HashSet<FunctionDefinition> getReferencedFunctionDefinitions() {
    return referencedFunctionDefinitions;
  }
  
  /**
   * 
   * @return
   */
  public HashSet<LocalParameter> getReferencedLocalParameters() {
    return referencedLocalParameters;
  }
  
  /**
   * 
   * @return
   */
  public HashSet<Parameter> getReferencedParameters() {
    return referencedParameters;
  }
  
  /**
   * 
   * @return
   */
  public HashSet<Reaction> getReferencedReactions() {
    return referencedReactions;
  }
  
  /**
   * 
   * @return
   */
  public HashSet<Species> getReferencedSpecies() {
    return referencedSpecies;
  }
  
  /**
   * 
   * @return
   */
  public HashSet<SpeciesReference> getReferencedSpeciesReferences() {
    return referencedSpeciesReferences;
  }
  
  /**
   * 
   * @return
   */
  public HashSet<UnitDefinition> getReferencedUnitDefinitions() {
    return referencedUnitDefinitions;
  }
  
  /**
   * 
   * @return
   */
  public HashSet<Compartment> getReferenceableCompartments() {
    return referenceableCompartments;
  }
  
  /**
   * 
   * @return
   */
  public HashSet<Reaction> getReferenceableReactions() {
    return referenceableReactions;
  }
  
  /**
   * 
   * @return
   */
  public HashSet<Species> getReferenceableSpecies() {
    return referenceableSpecies;
  }
  
  /**
   * 
   * @return
   */
  public HashSet<SpeciesReference> getReferenceableSpeciesReferences() {
    return referenceableSpeciesReferences;
  }
  
  /**
   * 
   * @return
   */
  public String getReport() {
    return report.toString();
  }
  
  /**
   * Bug-Fix / Workaround
   * 
   * @deprecated
   * @param node
   * @return
   */
  @Deprecated
  private CallableSBase getVariable(ASTNode node) {
    CallableSBase variable = node.getVariable();
    if (variable == null) {
      variable = node.getParentSBMLObject().getModel()
          .findQuantity(node.getName());
      if (variable == null) {
        String id = node.getName();
        for (Reaction reaction : node.getParentSBMLObject().getModel()
            .getListOfReactions()) {
          KineticLaw kineticLaw = reaction.getKineticLaw();
          for (LocalParameter localParameter : kineticLaw
              .getListOfLocalParameters()) {
            if (localParameter.getId().equals(id)) {
              variable = localParameter;
              break;
            }
          }
        }
      }
    }
    return variable;
  }
  
  /**
   * Prints the current state of the {@link KineticLawImporter} to console
   * (Debugging purposes).
   */
  public void printConsole() {
    System.out.println("KineticLaw: " + kineticLaw);
    System.out.println("Referenced Compartments: " + referencedCompartments);
    System.out.println("Referenced FunctionDefinitions: "
        + referencedFunctionDefinitions);
    System.out.println("Referenced LocalParameters: "
        + referencedLocalParameters);
    System.out.println("Referenced Parameters: " + referencedParameters);
    System.out.println("Referenced Reactions: " + referencedReactions);
    System.out.println("Referenced Species: " + referencedSpecies);
    System.out.println("Referenced SpeciesReferences: "
        + referencedSpeciesReferences);
    System.out.println("Referenced UnitDefinitions: "
        + referencedUnitDefinitions);
    System.out.println("---");
    System.out.println("Reaction: " + reaction);
    System.out.println("Reaction Model: " + reactionModel);
    System.out.println("Referenceable Compartments: "
        + referenceableCompartments);
    System.out.println("Referenceable Reactions: " + referenceableReactions);
    System.out.println("Referenceable Species: " + referenceableSpecies);
    System.out.println("Referenceable SpeciesReferences: "
        + referenceableSpeciesReferences);
    System.out.println("---");
    for (Entry<CallableSBase, CallableSBase> match : matches.entrySet()) {
      System.out.println(match.getKey() + " -> " + match.getValue());
    }
    System.out.println("---");
    System.out.println("Importable KineticLaw: " + isImportableKineticLaw());
  }
  
}
