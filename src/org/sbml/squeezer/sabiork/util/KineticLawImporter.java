/*
 * $$Id:  ${file_name} ${time} ${user} $$
 * $$URL: ${file_name} $$
 * ---------------------------------------------------------------------
 * This file is part of SBMLsqueezer, a Java program that creates rate 
 * equations for reactions in SBML files (http://sbml.org).
 *
 * Copyright (C) 2006-2012 by the University of Tuebingen, Germany.
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
package org.sbml.squeezer.sabiork.util;
/*
 * Verbesserte Version
 * 
 */
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
 * @author Matthias Rall
 * @version $$Rev$$
 * ${tags}
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

	public KineticLawImporter(KineticLaw kineticLaw, Reaction reaction) {
		this.kineticLaw = kineticLaw;
		this.reaction = reaction;
		this.reactionModel = reaction.getModel();

		this.referencedCompartments = new HashSet<Compartment>();
		this.referencedFunctionDefinitions = new HashSet<FunctionDefinition>();
		this.referencedLocalParameters = new HashSet<LocalParameter>();
		this.referencedParameters = new HashSet<Parameter>();
		this.referencedReactions = new HashSet<Reaction>();
		this.referencedSpecies = new HashSet<Species>();
		this.referencedSpeciesReferences = new HashSet<SpeciesReference>();
		this.referencedUnitDefinitions = new HashSet<UnitDefinition>();

		this.referenceableCompartments = new HashSet<Compartment>();
		this.referenceableReactions = new HashSet<Reaction>();
		this.referenceableSpecies = new HashSet<Species>();
		this.referenceableSpeciesReferences = new HashSet<SpeciesReference>();

		this.matches = new HashMap<CallableSBase, CallableSBase>();

		initialize();
	}

	private void initialize() {
		addReferencedComponents();
		addReferenceableComponents();
		addMatches();
	}

	private void addReferencedComponents() {
		if (kineticLaw.isSetMath()) {
			addReferencedComponents(kineticLaw.getMath());
			for (LocalParameter referencedLocalParameter : referencedLocalParameters) {
				if (referencedLocalParameter.isSetUnitsInstance()) {
					referencedUnitDefinitions.add(referencedLocalParameter.getUnitsInstance());
				}
			}
			for (Parameter referencedParameter : referencedParameters) {
				if (referencedParameter.isSetUnitsInstance()) {
					referencedUnitDefinitions.add(referencedParameter.getUnitsInstance());
				}
			}
		}
	}

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

	private void addReferenceableComponents() {
		if (reactionModel.isSetListOfReactions()) {
			referenceableReactions.addAll(reactionModel.getListOfReactions());
		}
		if (reactionModel.isSetListOfSpecies()) {
			for (Species species : reactionModel.getListOfSpecies()) {
				if (reaction.hasReactant(species) || reaction.hasModifier(species) || reaction.hasProduct(species)) {
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

	private List<String> getCVTermResources(Qualifier qualifier, SBase component) {
		List<String> cvTermResources = new ArrayList<String>();
		CVTermFilter filter = new CVTermFilter(qualifier);
		for (CVTerm cvTerm : component.getCVTerms()) {
			if (filter.accepts(cvTerm)) {
				cvTermResources.addAll(cvTerm.getResources());
			}
		}
		return cvTermResources;
	}

	private boolean hasCommonCVTermResource(Qualifier qualifier1, SBase component1, Qualifier qualifier2, SBase component2) {
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

	private boolean isMatch(Compartment compartment1, Compartment compartment2) {
		return (hasCommonCVTermResource(Qualifier.BQB_IS, compartment1, Qualifier.BQB_IS, compartment2) || referenceableCompartments.size() == 1);
	}

	private boolean isMatch(Reaction reaction1, Reaction reaction2) {
		return hasCommonCVTermResource(Qualifier.BQB_IS, reaction1, Qualifier.BQB_IS, reaction2);
	}

	private boolean isMatch(Species species1, Species species2) {
		return (hasCommonCVTermResource(Qualifier.BQB_IS, species1, Qualifier.BQB_IS, species2) || hasCommonCVTermResource(Qualifier.BQB_HAS_VERSION, species1, Qualifier.BQB_IS, species2) || hasCommonCVTermResource(Qualifier.BQB_IS, species1, Qualifier.BQB_HAS_VERSION, species2));
	}

	private boolean isMatch(SpeciesReference speciesReference1, SpeciesReference speciesReference2) {
		if (speciesReference1.isSetSpeciesInstance() && speciesReference2.isSetSpeciesInstance()) {
			return isMatch(speciesReference1.getSpeciesInstance(), speciesReference2.getSpeciesInstance());
		} else {
			return false;
		}
	}

	public void match(CallableSBase referencedComponent, CallableSBase referenceableComponent) {
		if (referencedCompartments.contains(referencedComponent) && referenceableCompartments.contains(referenceableComponent)) {
			matches.put(referencedComponent, referenceableComponent);
		}
		if (referencedReactions.contains(referencedComponent) && referenceableReactions.contains(referenceableComponent)) {
			matches.put(referencedComponent, referenceableComponent);
		}
		if (referencedSpecies.contains(referencedComponent) && referenceableSpecies.contains(referenceableComponent)) {
			matches.put(referencedComponent, referenceableComponent);
		}
		if (referencedSpeciesReferences.contains(referencedComponent) && referenceableSpeciesReferences.contains(referenceableComponent)) {
			matches.put(referencedComponent, referenceableComponent);
		}
	}

	public CallableSBase getMatchingReferenceableComponent(CallableSBase referencedComponent) {
		CallableSBase match = null;
		if (matches.containsKey(referencedComponent)) {
			match = matches.get(referencedComponent);
		}
		return match;
	}

	public boolean isImportableKineticLaw() {
		for (CallableSBase match : matches.keySet()) {
			if (matches.get(match) == null) {
				return false;
			}
		}
		return true;
	}

	public void importKineticLaw() {
		HashMap<UnitDefinition, UnitDefinition> unitDefinitions = new HashMap<UnitDefinition, UnitDefinition>();
		for (UnitDefinition referencedUnitDefinition : referencedUnitDefinitions) {
			UnitDefinition referencedUnitDefinitionCopy = referencedUnitDefinition.clone();
			referencedUnitDefinitionCopy.setId(getUniqueUnitDefinitionID(referencedUnitDefinitionCopy.getId()));
			referencedUnitDefinitionCopy.setLevel(reactionModel.getLevel());
			referencedUnitDefinitionCopy.setVersion(reactionModel.getVersion());
			reactionModel.addUnitDefinition(referencedUnitDefinitionCopy);
			unitDefinitions.put(referencedUnitDefinition, referencedUnitDefinitionCopy);
		}

		for (Entry<CallableSBase, CallableSBase> match : matches.entrySet()) {
			replaceKineticLawReferences(match.getKey(), match.getValue());
		}

		for (FunctionDefinition referencedFunctionDefinition : referencedFunctionDefinitions) {
			FunctionDefinition referencedFunctionDefinitionCopy = referencedFunctionDefinition.clone();
			referencedFunctionDefinitionCopy.setId(getUniqueID(referencedFunctionDefinitionCopy.getId()));
			referencedFunctionDefinitionCopy.setLevel(reactionModel.getLevel());
			referencedFunctionDefinitionCopy.setVersion(reactionModel.getVersion());
			reactionModel.addFunctionDefinition(referencedFunctionDefinitionCopy);
			replaceKineticLawReferences(referencedFunctionDefinition, referencedFunctionDefinitionCopy);
		}

		for (Parameter referencedParameter : referencedParameters) {
			Parameter referencedParameterCopy = referencedParameter.clone();
			referencedParameterCopy.setId(getUniqueID(referencedParameterCopy.getId()));
			referencedParameterCopy.setLevel(reactionModel.getLevel());
			referencedParameterCopy.setVersion(reactionModel.getVersion());
			if (unitDefinitions.containsKey(referencedParameter.getUnitsInstance())) {
				referencedParameterCopy.setUnits(unitDefinitions.get(referencedParameter.getUnitsInstance()));
			}
			reactionModel.addParameter(referencedParameterCopy);
			replaceKineticLawReferences(referencedParameter, referencedParameterCopy);
		}

		ListOf<LocalParameter> localParameters = new ListOf<LocalParameter>();
		localParameters.setLevel(reactionModel.getLevel());
		localParameters.setVersion(reactionModel.getVersion());
		for (LocalParameter referencedLocalParameter : referencedLocalParameters) {
			LocalParameter referencedLocalParameterCopy = referencedLocalParameter.clone();
			referencedLocalParameterCopy.setId(getUniqueID(referencedLocalParameterCopy.getId()));
			referencedLocalParameterCopy.setLevel(reactionModel.getLevel());
			referencedLocalParameterCopy.setVersion(reactionModel.getVersion());
			if (unitDefinitions.containsKey(referencedLocalParameter.getUnitsInstance())) {
				referencedLocalParameterCopy.setUnits(unitDefinitions.get(referencedLocalParameter.getUnitsInstance()));
			}
			localParameters.add(referencedLocalParameterCopy);
			replaceKineticLawReferences(referencedLocalParameter, referencedLocalParameterCopy);
		}
		kineticLaw.unsetListOfLocalParameters();

		KineticLaw kineticLawCopy = kineticLaw.clone();
		kineticLawCopy.setMetaId(getUniqueMetaID(reaction.getId() + "(KineticLaw)"));
		kineticLawCopy.setLevel(reactionModel.getLevel());
		kineticLawCopy.setVersion(reactionModel.getVersion());
		kineticLawCopy.setListOfLocalParameters(localParameters);
		reaction.setKineticLaw(kineticLawCopy);
	}

	private void replaceKineticLawReferences(CallableSBase oldReference, CallableSBase newReference) {
		if (kineticLaw.isSetMath()) {
			replaceKineticLawReferences(kineticLaw.getMath(), oldReference, newReference);
		}
	}

	private void replaceKineticLawReferences(ASTNode node, CallableSBase oldReference, CallableSBase newReference) {
		if (node.isString()) {
			CallableSBase variable = getVariable(node);
			if (variable.equals(oldReference)) {
				node.setName(newReference.getName());
				node.setVariable(newReference);
			}
		}
		for (ASTNode child : node.getChildren()) {
			replaceKineticLawReferences(child, oldReference, newReference);
		}
	}

	private boolean isUniqueID(String id) {
		return (reactionModel.findNamedSBase(id) == null);
	}

	private boolean isUniqueUnitDefinitionID(String id) {
		return (reactionModel.findUnitDefinition(id) == null);
	}

	private boolean isUniqueMetaID(String id) {
		return (!reactionModel.getSBMLDocument().containsMetaId(id));
	}

	private String getUniqueID(String id) {
		String oldID = id;
		String newID = id;
		for (int i = 1; !isUniqueID(newID); i++) {
			newID = oldID + i;
		}
		return newID;
	}

	private String getUniqueUnitDefinitionID(String id) {
		String oldID = id;
		String newID = id;
		for (int i = 1; !isUniqueUnitDefinitionID(newID); i++) {
			newID = oldID + i;
		}
		return newID;
	}

	private String getUniqueMetaID(String id) {
		String oldID = id;
		String newID = id;
		for (int i = 1; !isUniqueMetaID(newID); i++) {
			newID = oldID + i;
		}
		return newID;
	}

	public KineticLaw getKineticLaw() {
		return kineticLaw;
	}

	public Reaction getReaction() {
		return reaction;
	}

	public HashSet<Compartment> getReferencedCompartments() {
		return referencedCompartments;
	}

	public HashSet<FunctionDefinition> getReferencedFunctionDefinitions() {
		return referencedFunctionDefinitions;
	}

	public HashSet<LocalParameter> getReferencedLocalParameters() {
		return referencedLocalParameters;
	}

	public HashSet<Parameter> getReferencedParameters() {
		return referencedParameters;
	}

	public HashSet<Reaction> getReferencedReactions() {
		return referencedReactions;
	}

	public HashSet<Species> getReferencedSpecies() {
		return referencedSpecies;
	}

	public HashSet<SpeciesReference> getReferencedSpeciesReferences() {
		return referencedSpeciesReferences;
	}

	public HashSet<UnitDefinition> getReferencedUnitDefinitions() {
		return referencedUnitDefinitions;
	}

	public HashSet<Compartment> getReferenceableCompartments() {
		return referenceableCompartments;
	}

	public HashSet<Reaction> getReferenceableReactions() {
		return referenceableReactions;
	}

	public HashSet<Species> getReferenceableSpecies() {
		return referenceableSpecies;
	}

	public HashSet<SpeciesReference> getReferenceableSpeciesReferences() {
		return referenceableSpeciesReferences;
	}

	private CallableSBase getVariable(ASTNode node) {
		CallableSBase variable = node.getVariable();
		if ((variable == null)) {
			variable = node.getParentSBMLObject().getModel().findQuantity(node.getName());
			if ((variable == null)) {
				String id = node.getName();
				for (Reaction r : node.getParentSBMLObject().getModel().getListOfReactions()) {
					KineticLaw kl = r.getKineticLaw();
					for (LocalParameter lp : kl.getListOfLocalParameters()) {
						if (lp.getId().equals(id)) {
							variable = lp;
							break;
						}
					}
				}
			}
		}
		return variable;
	}

	public void printConsole() {
		System.out.println("KineticLaw: " + kineticLaw);
		System.out.println("Referenced Compartments: " + referencedCompartments);
		System.out.println("Referenced FunctionDefinitions: " + referencedFunctionDefinitions);
		System.out.println("Referenced LocalParameters: " + referencedLocalParameters);
		System.out.println("Referenced Parameters: " + referencedParameters);
		System.out.println("Referenced Reactions: " + referencedReactions);
		System.out.println("Referenced Species: " + referencedSpecies);
		System.out.println("Referenced SpeciesReferences: " + referencedSpeciesReferences);
		System.out.println("Referenced UnitDefinitions: " + referencedUnitDefinitions);
		System.out.println("---");
		System.out.println("Reaction: " + reaction);
		System.out.println("Reaction Model: " + reactionModel);
		System.out.println("Referenceable Compartments: " + referenceableCompartments);
		System.out.println("Referenceable Reactions: " + referenceableReactions);
		System.out.println("Referenceable Species: " + referenceableSpecies);
		System.out.println("Referenceable SpeciesReferences: " + referenceableSpeciesReferences);
		System.out.println("---");
		for (Entry<CallableSBase, CallableSBase> match : matches.entrySet()) {
			System.out.println(match.getKey() + " -> " + match.getValue());
		}
		System.out.println("---");
		System.out.println("Importable KineticLaw: " + isImportableKineticLaw());
	}

}
