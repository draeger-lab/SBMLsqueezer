package org.sbml.squeezer.test;

import java.util.logging.Logger;

import javax.swing.tree.TreeNode;

import org.sbml.jsbml.AlgebraicRule;
import org.sbml.jsbml.AssignmentRule;
import org.sbml.jsbml.Compartment;
import org.sbml.jsbml.CompartmentType;
import org.sbml.jsbml.Constraint;
import org.sbml.jsbml.Event;
import org.sbml.jsbml.ExplicitRule;
import org.sbml.jsbml.FunctionDefinition;
import org.sbml.jsbml.InitialAssignment;
import org.sbml.jsbml.Model;
import org.sbml.jsbml.Parameter;
import org.sbml.jsbml.RateRule;
import org.sbml.jsbml.Reaction;
import org.sbml.jsbml.Rule;
import org.sbml.jsbml.SBMLException;
import org.sbml.jsbml.Species;
import org.sbml.jsbml.SpeciesType;
import org.sbml.jsbml.UnitDefinition;
import org.sbml.squeezer.KineticLawGenerator;

public class SqueezertestFunctions {


	@SuppressWarnings("deprecation")
	public static boolean compareModels(Model modelOrig, Model model){
		boolean areEqual = true;
		Logger logger = Logger.getLogger(KineticLawGenerator.class.getName());
		// test if and where the models differ
		if(model == null){
			logger.warning("-> model is empty");
			areEqual = false;
		}else if(modelOrig == null){
			logger.warning("-> original model is empty");
			areEqual = false;
		}else{
			if(!modelOrig.getClass().equals(model.getClass())) {
				logger.warning("-> classes are unequal");
				areEqual = false;
			}
			if (model instanceof TreeNode) {
				int childCount = modelOrig.getChildCount();
				if(model.getChildCount() != childCount){
					logger.warning("-> unequal child count");
					areEqual = false;
				}
				if(!modelOrig.getListOfCompartments().equals(model.getListOfCompartments())){
					logger.warning("-> unequal List of compartments");
					areEqual = false; 
				}if(!modelOrig.getListOfCompartmentTypes().equals(model.getListOfCompartmentTypes())){
					logger.warning("-> unequal List of compartment types");
					areEqual = false;
				}if(!modelOrig.getListOfConstraints().equals(model.getListOfConstraints())){
					logger.warning("-> unequal List of constraints");
					areEqual = false; 
				}if(!modelOrig.getListOfEvents().equals(model.getListOfEvents())){
					logger.warning("-> unequal List of events");
					areEqual = false;
				}if(!modelOrig.getListOfFunctionDefinitions().equals(model.getListOfFunctionDefinitions())){
					logger.warning("-> unequal List of function definitions");
					areEqual = false;
				}if(!modelOrig.getListOfInitialAssignments().equals(model.getListOfInitialAssignments())){
					logger.warning("-> unequal List of initial assignments");
					areEqual = false;
				}if(!modelOrig.getListOfParameters().equals(model.getListOfParameters())){
					logger.warning("-> unequal List of parameters");
					areEqual = false;
				}if(!modelOrig.getListOfPredefinedUnitDefinitions().equals(model.getListOfPredefinedUnitDefinitions())){
					logger.warning("-> unequal List of predefined unit definitions");
					areEqual = false; 
				}if(!modelOrig.getListOfReactions().equals(model.getListOfReactions())){
					logger.warning("-> unequal List of reactions");
					areEqual = false; 
				}if(!modelOrig.getListOfRules().equals(model.getListOfRules())){
					logger.warning("-> unequal List of rules");
					areEqual = false;
				}if(!modelOrig.getListOfSpecies().equals(model.getListOfSpecies())){
					logger.warning("-> unequal List of species");
					areEqual = false;
				}if(!modelOrig.getListOfSpeciesTypes().equals(model.getListOfSpeciesTypes())){
					logger.warning("-> unequal List of species types");
					areEqual = false;
				}if(!modelOrig.getListOfUnitDefinitions().equals(model.getListOfUnitDefinitions())){
					logger.warning("-> unequal List of unit definitions");
					areEqual = false;
				}

			}
			if(model.isSetMetaId() != modelOrig.isSetMetaId()){
				logger.warning("-> one of the MetaIds is not set");
				areEqual = false;
			}else if (model.isSetMetaId()) {
				if(!model.getMetaId().equals(modelOrig.getMetaId())){
					logger.warning("-> models have different MetaId");
					areEqual = false;
				}
			}
			if(model.isSetSBOTerm() != modelOrig.isSetSBOTerm()){
				logger.warning("-> one of the SBOTerms is not set");
				areEqual = false;
			}else if(model.isSetSBOTerm()){
				if(model.getSBOTerm() != modelOrig.getSBOTerm()){
					logger.warning("-> models have different SBOTerm");
					areEqual = false;
				}
			}
			if(!model.getLevelAndVersion().equals(modelOrig.getLevelAndVersion())){
				logger.warning("models differ in version and / or level");
				areEqual = false;
			}
			if(model.isSetId() != modelOrig.isSetId()){
				logger.warning("-> one of the ids is not set");
				areEqual = false;
			}else if (model.isSetId()) {
				if(!model.getId().equals(modelOrig.getId())){
					logger.warning("models have different Ids");
					areEqual = false;
				}
			}
			if(model.isSetName() != modelOrig.isSetName()){
				logger.warning("-> one of the names is not set");
				areEqual = false;
			}else if(model.isSetName()){
				if(!model.getName().equals(modelOrig.getName())){
					logger.warning("models have different names");
					areEqual = false;
				}
			}
			if(model.isSetTimeUnits() != modelOrig.isSetTimeUnits()){
				logger.warning("-> one of the time units is not set");
				areEqual = false;
			}else if (modelOrig.isSetTimeUnits()) {
				if(!modelOrig.getTimeUnits().equals(model.getTimeUnits())){
					logger.warning("models have different time units");
					areEqual = false;
				}
			}
			if(model.isSetAreaUnits() != modelOrig.isSetAreaUnits()){
				logger.warning("-> one of the area units is not set");
				areEqual = false;
			}else if(modelOrig.isSetAreaUnits()){
				if(!modelOrig.getAreaUnits().equals(model.getAreaUnits())){
					logger.warning("models have different area units");
					areEqual = false;
				}
			}
			if(model.isSetConversionFactor() != modelOrig.isSetConversionFactor()){
				logger.warning("-> one of the conversion factors is not set");
				areEqual = false;
			}else if(modelOrig.isSetConversionFactor()){
				if(!modelOrig.getConversionFactor().equals(model.getConversionFactor())){
					logger.warning("models have different conversion factors");
					areEqual = false;
				}
			}
			if(model.isSetExtentUnits() != modelOrig.isSetExtentUnits()){
				logger.warning("-> one of the extent units is not set");
				areEqual = false;
			}else if(modelOrig.isSetExtentUnits()){
				if(!modelOrig.getExtentUnits().equals(model.getExtentUnits())){
					logger.warning("models have different extent units");
					areEqual = false;
				}
			}
			if(modelOrig.isSetLengthUnits() != modelOrig.isSetLengthUnits()){
				logger.warning("-> one of the length units is not set");
				areEqual = false;
			}else if(modelOrig.isSetLengthUnits()){
				if(!modelOrig.getLengthUnits().equals(model.getLengthUnits())){
					logger.warning("models have different length units");
					areEqual = false;
				}
			}
			if(model.isSetSubstanceUnits() != modelOrig.isSetSubstanceUnits()){
				logger.warning("-> one of the substance units is not set");
				areEqual = false;
			}else if(model.isSetSubstanceUnits()){
				if(!modelOrig.getSubstanceUnits().equals(model.getSubstanceUnits())){
					logger.warning("models have different substance units");
					areEqual = false;
				}
			}
			if(model.isSetVolumeUnits() != modelOrig.isSetVolumeUnits()){
				logger.warning("-> one of the volume units is not set");
				areEqual = false;
			}else if(modelOrig.isSetVolumeUnits()){
				if(!modelOrig.getVolumeUnits().equals(model.getVolumeUnits())){
					logger.warning("models have different volume units");
					areEqual = false;
				}
			}
		}
		return areEqual;
	}
	
	
	public static boolean saveChanges(Model model, Model originalModel) throws SBMLException {

		// Function definitions
		FunctionDefinition fd1;
		for (FunctionDefinition fd2 : model.getListOfFunctionDefinitions()) {
			fd1 = originalModel.getFunctionDefinition(fd2.getId());
			if (fd1 == null) {
				// new function definition
				originalModel.addFunctionDefinition(fd2);
			} else if(!fd1.equals(fd2)){
				// changed function definition
				originalModel.removeFunctionDefinition(fd2.getId());
				originalModel.addFunctionDefinition(fd2);
			}
		}

		// Unit definitions
		UnitDefinition ud1;
		for (UnitDefinition ud2 : model.getListOfUnitDefinitions()){
			ud1 = originalModel.getUnitDefinition(ud2.getId());
			if(ud1 == null){
				// new unit definition
				originalModel.addUnitDefinition(ud2);
			} else if(!UnitDefinition.areIdentical(ud1, ud2)){
				// changed unit definition
				originalModel.removeUnitDefinition(ud1);
				originalModel.addUnitDefinition(ud2);
			}
		}
		
		// Compartment types
		CompartmentType ct1;
		for (CompartmentType ct2 : model.getListOfCompartmentTypes()) {
			ct1 = originalModel.getCompartmentType(ct2.getId());
			if (ct1 == null){
				originalModel.addCompartmentType(ct2);
			}else if(!ct1.equals(ct2)){
				originalModel.removeCompartmentType(ct1.getId());
				originalModel.addCompartmentType(ct2);
			}
		}

		// Species types
		SpeciesType st1;
		for (SpeciesType st2 : model.getListOfSpeciesTypes()) {
			st1 = originalModel.getSpeciesType(st2.getId());
			if (st1 == null) {
				originalModel.addSpeciesType(st2);
			} else if(!st1.equals(st2)){
				originalModel.removeSpeciesType(st1.getId());
				originalModel.addSpeciesType(st2);
			}
		}

		// Compartments
		Compartment cm1;
		for (Compartment cm2 : model.getListOfCompartments()) {
			cm1 = originalModel.getCompartment(cm2.getId());
			if (cm1 == null) {
				originalModel.addCompartment(cm2);
			} else if(!cm1.equals(cm2)){
				originalModel.removeCompartment(cm1.getId());
				originalModel.addCompartment(cm2);
			}
		}

		// Species
		Species sp1;
		for (Species sp2 : model.getListOfSpecies()) {
			sp1 = originalModel.getSpecies(sp2.getId());
			if (sp1 == null) {
				originalModel.addSpecies(sp2);
			} else if(!sp1.equals(sp2)){
				originalModel.removeSpecies(sp1);
				originalModel.addSpecies(sp2);
			}
		}

		// add or change parameters
		Parameter pa1;
		for (Parameter pa2 : model.getListOfParameters()) {
			pa1 = originalModel.getParameter(pa2.getId());
			if (pa1 == null) {
				originalModel.addParameter(pa2);
			} else if(!pa1.equals(pa2)){
				originalModel.removeParameter(pa1);
				originalModel.addParameter(pa2);
			}
		}

		// initial assignments
		InitialAssignment ia1;
		int contains;
		for (InitialAssignment ia2 : model.getListOfInitialAssignments()) {			
			contains = -1;			
			for (int i = 0; i < originalModel.getNumInitialAssignments() && contains < 0; i++) {
				ia1 = originalModel.getInitialAssignment(i);
				if (ia1.equals(ia2)){
					contains = i;
				}
			}
			if (contains < 0) {
				originalModel.addInitialAssignment(ia2);
			} else if(!originalModel.getInitialAssignment(contains).equals(ia2)){
				originalModel.removeInitialAssignment(contains);
				originalModel.addInitialAssignment(ia2);
			}		
		}	

		// rules
		Rule ru1;
		ExplicitRule eru1;
		ExplicitRule eru2;
		boolean equal = false;
		for (Rule ru2 : model.getListOfRules()) {	
			eru2 = ((ExplicitRule) ru2);			
			contains = -1;			
			for (int i = 0; i < originalModel.getNumRules() && contains < 0; i++) {
				ru1 = originalModel.getRule(i);
				eru1 = ((ExplicitRule) ru1);
				if ((ru2 instanceof AlgebraicRule) && (ru1 instanceof AlgebraicRule)) {
					equal = true;
				}else {
					if ((ru2 instanceof RateRule) && (ru1 instanceof org.sbml.jsbml.RateRule)) {
						equal = ((RateRule) ru2).getVariable().equals(
									((RateRule) ru1).getVariable());
					} else if ((ru2 instanceof AssignmentRule) && (ru1 instanceof org.sbml.jsbml.AssignmentRule)) {
						equal = ((AssignmentRule) ru2).getVariable().equals(
								((AssignmentRule) ru1).getVariable());
					}
					equal &= eru2.isSetUnits() && eru1.isSetUnits();
					if (equal && eru1.isSetUnits()) {
						equal &= eru1.getUnits().equals(((ExplicitRule) ru2).getUnits());
					}
				}
				if (equal) {
					equal &= eru2.getMath().equals(ru1.getMath());
				}
				if (equal) {
					contains = i;
				}
			}
			if (contains < 0) {
				originalModel.addRule(ru2);
			} else if(!originalModel.getRule(contains).equals(ru2)){
				originalModel.removeRule(contains);
				originalModel.addRule(ru2);
			}
		}	

		// constraints
		Constraint cn1;
		for (Constraint cn2 : model.getListOfConstraints()) {
			contains = -1;
			for (int i = 0; i < originalModel.getNumConstraints() && contains < 0; i++) {
				cn1 = originalModel.getConstraint(i);
				if (cn2.getMath().equals(cn1.getMath()))
					contains = i;
			}
			if (contains < 0) {
				originalModel.addConstraint(cn2);
			} else {
				originalModel.removeConstraint(contains);
				originalModel.addConstraint(cn2);
			}
		}

		// add or change reactions
		Reaction re1;
		for (Reaction re2 : model.getListOfReactions()) {
			re1 = originalModel.getReaction(re2.getId());
			if (re1 == null) {
				originalModel.addReaction(re2);
			} else if(!re1.equals(re2)){
				originalModel.removeReaction(re1.getId());
				originalModel.addReaction(re2);
			}
		}

		// events
		Event ev1;
		for (Event ev2 : model.getListOfEvents()) {
			ev1 = originalModel.getEvent(ev2.getId());
			if (ev1 == null) {
				originalModel.addEvent(ev2);
			} else if(!ev1.equals(ev2)){
				originalModel.removeEvent(ev1.getId());
				originalModel.addEvent(ev2);
			}
		}
		
		return true;
	}
	
}
