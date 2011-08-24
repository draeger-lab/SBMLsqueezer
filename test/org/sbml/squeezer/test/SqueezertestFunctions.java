package org.sbml.squeezer.test;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.tree.TreeNode;

import org.sbml.jsbml.Model;
import org.sbml.jsbml.Reaction;
import org.sbml.squeezer.KineticLawGenerator;

public class SqueezertestFunctions {


	public static boolean compareModels(Model modelOrig, Model model){
		boolean areEqual = true;
		Logger logger = Logger.getLogger(KineticLawGenerator.class.getName());
		// test if and where the models differ
		if(model == null){
			logger.warning("-> newMiniModel is empty");
			areEqual = false;
		}else if(modelOrig == null){
			logger.warning("-> miniModel is empty");
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
				}else{
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
						for(int i = 0; i < modelOrig.getListOfReactions().size(); i++){
							if(modelOrig.getListOfReactions().get(i) != model.getListOfReactions().get(i)){
								logger.warning("-> reactions " + modelOrig.getListOfReactions().get(i).toString());
							}
						}
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
}
