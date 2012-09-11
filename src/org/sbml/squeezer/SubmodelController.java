/*
 * $Id$
 * $URL$
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
package org.sbml.squeezer;

import java.text.MessageFormat;
import java.util.HashSet;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.sbml.jsbml.AssignmentRule;
import org.sbml.jsbml.Compartment;
import org.sbml.jsbml.Constraint;
import org.sbml.jsbml.Event;
import org.sbml.jsbml.EventAssignment;
import org.sbml.jsbml.FunctionDefinition;
import org.sbml.jsbml.InitialAssignment;
import org.sbml.jsbml.KineticLaw;
import org.sbml.jsbml.ListOf;
import org.sbml.jsbml.LocalParameter;
import org.sbml.jsbml.Model;
import org.sbml.jsbml.ModifierSpeciesReference;
import org.sbml.jsbml.Parameter;
import org.sbml.jsbml.QuantityWithUnit;
import org.sbml.jsbml.RateRule;
import org.sbml.jsbml.Reaction;
import org.sbml.jsbml.Rule;
import org.sbml.jsbml.SBMLDocument;
import org.sbml.jsbml.SBO;
import org.sbml.jsbml.Species;
import org.sbml.jsbml.SpeciesReference;
import org.sbml.jsbml.Unit;
import org.sbml.jsbml.UnitDefinition;
import org.sbml.squeezer.util.Bundles;
import org.sbml.squeezer.util.ProgressAdapter;
import org.sbml.squeezer.util.ProgressAdapter.TypeOfProgress;

import de.zbit.sbml.util.SBMLtools;
import de.zbit.util.ResourceManager;
import de.zbit.util.prefs.SBPreferences;
import de.zbit.util.progressbar.AbstractProgressBar;

/**
 * This class is responsible for miniModel creation and the synchronisation
 * with the main {@link Model}
 * 
 * @since 1.4
 * @version $Rev$
 * @author Andreas Dr&auml;ger
 * @author Sebastian Nagel
 */
public class SubmodelController {
	
	/**
	 * 
	 */
	public static final transient ResourceBundle WARNINGS = ResourceManager.getBundle(Bundles.WARNINGS);
	
	/**
	 * 
	 */
	private final Logger logger = Logger.getLogger(SubmodelController.class.getName());
	
	/**
	 * This list contains all fast reactions of the original model. Since the
	 * fast attribute is still ignored by SBMLsqueezer, we use this list to
	 * decide whether a warning message should be displayed later on.
	 */
	private List<Reaction> listOfFastReactions;
	
	private boolean generateLawsForAllReactions;
	private boolean removeUnnecessaryParameters;
	private boolean defaultHasOnlySubstanceUnits;
	private boolean addParametersGlobally;
	private boolean reversibility;
	private boolean setBoundaryCondition;
	private double defaultSpeciesInitVal;
	private double defaultCompartmentInitSize;
	
	private Model modelOrig;
	private Model miniModel;
	
	private ProgressAdapter progressAdapter;
	private AbstractProgressBar progressBar;

	private double defaultSpatialDimensions;
	
	/**
	 * @return the defaultSpatialDimensions
	 */
	public double getDefaultSpatialDimensions() {
		return defaultSpatialDimensions;
	}

	/**
	 * @param defaultSpatialDimensions the defaultSpatialDimensions to set
	 */
	public void setDefaultSpatialDimensions(double defaultSpatialDimensions) {
		this.defaultSpatialDimensions = defaultSpatialDimensions;
	}

	/**
	 * 
	 * @param progressBar
	 */
	public void setProgressBar(AbstractProgressBar progressBar) {
		this.progressBar = progressBar;
	}
	
	/**
	 * 
	 * @return
	 */
	public Model getSubmodel() {
		return this.miniModel;
	}
	
	/**
	 * 
	 * @param addParametersGlobally
	 */
	public void setAddParametersGlobally(boolean addParametersGlobally) {
		this.addParametersGlobally = addParametersGlobally;
	}
	
	/**
	 * 
	 * @param reversibility
	 */
	public void setReversibility(boolean reversibility) {
		this.reversibility = reversibility;
	}
	
	/**
	 * 
	 * @param model
	 */
	public SubmodelController(Model model) {
		this(model, null);
	}
	
	/**
	 * 
	 * @param model
	 * @param reactionID
	 */
	public SubmodelController(Model model, String reactionID) {
		// Initialize user settings:
		SBPreferences prefs = SBPreferences.getPreferencesFor(SqueezerOptions.class);
		generateLawsForAllReactions = prefs.getBoolean(SqueezerOptions.GENERATE_KINETIC_LAWS_FOR_ALL_REACTIONS);
		removeUnnecessaryParameters = prefs.getBoolean(SqueezerOptions.REMOVE_UNNECESSARY_PARAMETERS_AND_UNITS);
		defaultHasOnlySubstanceUnits = prefs.getBoolean(SqueezerOptions.DEFAULT_SPECIES_HAS_ONLY_SUBSTANCE_UNITS);
		addParametersGlobally = prefs.getBoolean(SqueezerOptions.NEW_PARAMETERS_GLOBAL);
		reversibility = prefs.getBoolean(SqueezerOptions.TREAT_ALL_REACTIONS_REVERSIBLE);
		setBoundaryCondition = prefs.getBoolean(SqueezerOptions.SET_BOUNDARY_CONDITION_FOR_GENES);
		defaultSpeciesInitVal = prefs.getDouble(SqueezerOptions.DEFAULT_SPECIES_INIT_VAL);
		defaultCompartmentInitSize = prefs.getDouble(SqueezerOptions.DEFAULT_COMPARTMENT_SIZE);
		
		this.modelOrig = model;
		this.miniModel = createMinimalModel(reactionID);
	}
	
	/**
	 * Creates a minimal copy of the original model that only covers those
	 * elements needed for the creation of rate equations.
	 * 
	 * @param reactionID
	 * 
	 * @return
	 */
	private Model createMinimalModel(String reactionID) {
		
		boolean create = generateLawsForAllReactions;
		int level = modelOrig.getLevel(), version = modelOrig.getVersion();
		SBMLDocument miniDoc = new SBMLDocument(level, version);
		Model miniModel = miniDoc.createModel("submodel_" + modelOrig.getId());	  
		//miniModel.addChangeListener(new ModelChangeListener());
		
		/* 
		 * Set default unit definitions if it is not already set
		 */
		if (modelOrig.isSetAreaUnitsInstance()) {
			UnitDefinition areaUD = modelOrig.getAreaUnitsInstance().clone();
			if ((level > 2) && (!miniModel.isSetAreaUnitsInstance() || !UnitDefinition.areIdentical(areaUD, miniModel.getAreaUnitsInstance()))) {
				miniModel.setAreaUnits(areaUD.clone());
			}
		} else {
			if (modelOrig.isSetAreaUnits() && Unit.Kind.isValidUnitKindString(modelOrig.getAreaUnits(), level, version)) {
				miniModel.setAreaUnits(modelOrig.getAreaUnits());
			} else {
				UnitDefinition areaUD = UnitDefinition.getPredefinedUnit(UnitDefinition.AREA, 2, 4);
				SBMLtools.setLevelAndVersion(areaUD, level, version);
				miniModel.addUnitDefinition(areaUD);
				if (level > 2) {
					miniModel.setAreaUnits(areaUD.getId());
				}
			}
		}
		
		if (modelOrig.isSetLengthUnitsInstance()) {
			UnitDefinition lengthUD = modelOrig.getLengthUnitsInstance().clone();
			if ((level > 2) && (!miniModel.isSetLengthUnitsInstance() || !UnitDefinition.areIdentical(lengthUD, miniModel.getLengthUnitsInstance()))) {
				miniModel.setLengthUnits(lengthUD.clone());
			}
		} else {
			if (modelOrig.isSetLengthUnits() && Unit.Kind.isValidUnitKindString(modelOrig.getLengthUnits(), level, version)) {
				miniModel.setLengthUnits(modelOrig.getLengthUnits());
			} else {
				UnitDefinition lengthUD = UnitDefinition.getPredefinedUnit(UnitDefinition.LENGTH, 2, 4);
				SBMLtools.setLevelAndVersion(lengthUD, level, version);
				miniModel.addUnitDefinition(lengthUD);
				if (level > 2) {
					miniModel.setLengthUnits(lengthUD.getId());
				}
			}
		}
		
		if (modelOrig.isSetSubstanceUnitsInstance()) {
			UnitDefinition substanceUD = modelOrig.getSubstanceUnitsInstance().clone();
			if ((level > 2) && (!miniModel.isSetSubstanceUnitsInstance() || !UnitDefinition.areIdentical(substanceUD, miniModel.getSubstanceUnitsInstance()))) {
				miniModel.setSubstanceUnits(substanceUD.clone());
			}
		} else {
			if (modelOrig.isSetSubstanceUnits() && Unit.Kind.isValidUnitKindString(modelOrig.getSubstanceUnits(), level, version)) {
				miniModel.setSubstanceUnits(modelOrig.getSubstanceUnits());
			} else {
				UnitDefinition substanceUD = UnitDefinition.getPredefinedUnit(UnitDefinition.SUBSTANCE, 2, 4);
				SBMLtools.setLevelAndVersion(substanceUD, level, version);
				miniModel.addUnitDefinition(substanceUD);
				if (level > 2) {
					miniModel.setSubstanceUnits(substanceUD.getId());
				}
			}
		}
		
		if (modelOrig.isSetTimeUnitsInstance()) {
			UnitDefinition timeUD = modelOrig.getTimeUnitsInstance().clone();
			if ((level > 2) && (!miniModel.isSetTimeUnitsInstance() || !UnitDefinition.areIdentical(timeUD, miniModel.getTimeUnitsInstance()))) {
				miniModel.setTimeUnits(timeUD.clone());
			}
		} else {
			if (modelOrig.isSetTimeUnits() && Unit.Kind.isValidUnitKindString(modelOrig.getTimeUnits(), level, version)) {
				miniModel.setTimeUnits(modelOrig.getTimeUnits());
			} else {
				UnitDefinition timeUD = UnitDefinition.getPredefinedUnit(UnitDefinition.TIME, 2, 4);
				SBMLtools.setLevelAndVersion(timeUD, level, version);
				miniModel.addUnitDefinition(timeUD);
				if (level > 2) {
					miniModel.setTimeUnits(timeUD.getId());
				}
			}
		}
		
		if (modelOrig.isSetVolumeUnitsInstance()) {
			UnitDefinition volumeUD = modelOrig.getVolumeUnitsInstance().clone();
			if ((level > 2) && (!miniModel.isSetVolumeUnitsInstance() || !UnitDefinition.areIdentical(volumeUD, miniModel.getVolumeUnitsInstance()))) {
				miniModel.setVolumeUnits(volumeUD.clone());
			}
		} else {
			if (modelOrig.isSetVolumeUnits() && Unit.Kind.isValidUnitKindString(modelOrig.getVolumeUnits(), level, version)) {
				miniModel.setVolumeUnits(modelOrig.getVolumeUnits());
			} else {
				UnitDefinition volumeUD = UnitDefinition.getPredefinedUnit(UnitDefinition.VOLUME, 2, 4);
				SBMLtools.setLevelAndVersion(volumeUD, level, version);
				miniModel.addUnitDefinition(volumeUD);
				if (level > 2) {
					miniModel.setVolumeUnits(volumeUD.getId());
				}
			}
		}
		
		UnitDefinition extentUD = modelOrig.getExtentUnitsInstance();
		if (extentUD != null) {
			miniModel.setExtentUnits(extentUD.clone());
		} else if (level > 2) {
			miniModel.setExtentUnits(miniModel.getSubstanceUnits());
		}
		
		
		//		/* 
		//		 * Optionally, re-scale substance and volume:
		//		 */
		//		// TODO: Check user option whether re-scaling of units should be performed or not.
		//		Unit u = substanceUD.getListOfUnits().getFirst();
		//		if (u.isMole()) {
		//			u.setScale(-3);
		//			substanceUD.setName("mmol");
		//			if (miniModel.getListOfPredefinedUnitDefinitions().remove(substanceUD)) {
		//				miniModel.addUnitDefinition(substanceUD);
		//			}
		//		}
		//		u = volumeUD.getListOfUnits().getFirst();
		//		if (u.isLitre()) {
		//			u.setScale(-3);
		//			volumeUD.setName("ml");
		//			if (miniModel.getListOfPredefinedUnitDefinitions().remove(volumeUD)) {
		//				miniModel.addUnitDefinition(volumeUD);
		//			}
		//		}
		
		if (progressBar != null) {
			progressAdapter = new ProgressAdapter(progressBar, TypeOfProgress.createMiniModel);
			progressAdapter.setNumberOfTags(modelOrig, miniModel, removeUnnecessaryParameters);
		}
		
		/*
		 * Copy needed species and reactions.
		 */
		if (modelOrig.isSetListOfReactions()) {
			for (Reaction reacOrig : modelOrig.getListOfReactions()) {
				/*
				 * Let us find all fast reactions. This feature is currently
				 * ignored.
				 */
				if (reacOrig.getFast()) {
					listOfFastReactions.add(reacOrig);
				}
				if (reactionID != null) {
					if (!reacOrig.getId().equals(reactionID)) {
						continue;
					} else {
						create = true;
					}
				}
				if (reacOrig.isSetKineticLaw()) {
					KineticLaw kl = reacOrig.getKineticLaw();
					if (!kl.isSetMath()) {
						create = true;
					} else {
						String formula = kl.getMath().toFormula();
						if ((formula == null) || formula.isEmpty() || formula.equals(" ")) {
							logger.warning(MessageFormat.format(WARNINGS.getString("INVALID_REACTION_FORMAT"), reacOrig.getId()));
							create = true;
						}
					}
				}
				if (!reacOrig.isSetKineticLaw() || create) {
					Reaction reac = new Reaction(reacOrig.getId(), reacOrig.getLevel(), reacOrig.getVersion());
					if (reacOrig.isSetSBOTerm()) {
						SBMLtools.setSBOTerm(reac, reacOrig.getSBOTerm());
					}
					miniModel.addReaction(reac);
					reac.setFast(reacOrig.getFast());
					reac.setReversible(reacOrig.getReversible());
					if (reacOrig.isSetListOfReactants()) {
						for (SpeciesReference specRefOrig : reacOrig.getListOfReactants()) {
							Species speciesOrig = specRefOrig.getSpeciesInstance();
							SpeciesReference sr = specRefOrig.clone();
							sr.setSpecies(copySpecies(speciesOrig, miniModel));
							reac.addReactant(sr);
							if (progressAdapter != null) {
								progressAdapter.progressOn();
							}
						}
					}
					if (reacOrig.isSetListOfProducts()) {
						for (SpeciesReference s : reacOrig.getListOfProducts()) {
							Species speciesOrig = s.getSpeciesInstance();
							SpeciesReference sr = s.clone();
							sr.setSpecies(copySpecies(speciesOrig, miniModel));
							reac.addProduct(sr);
							if (progressAdapter != null) {
								progressAdapter.progressOn();
							}
						}
					}
					if (reacOrig.isSetListOfModifiers()) {
						for (ModifierSpeciesReference modifierReferenceOrig : reacOrig.getListOfModifiers()) {
							Species speciesOrig = modifierReferenceOrig.getSpeciesInstance();
							ModifierSpeciesReference modifierReference = modifierReferenceOrig.clone();
							modifierReference.setSpecies(copySpecies(speciesOrig, miniModel));
							reac.addModifier(modifierReference);
							if (progressAdapter != null) {
								progressAdapter.progressOn();
							}
						}
					}
					/*
					 * This will be over written later on anyway but ignoring it
					 * would be confusing for users...
					 */
					if (reacOrig.isSetKineticLaw()) {
						KineticLaw l = reacOrig.getKineticLaw();
						if (l.isSetMath() && modelOrig.isSetListOfParameters()) {
							for (Parameter parameter : modelOrig.getListOfParameters()) {
								if (l.getMath().refersTo(parameter.getId())
										&& (miniModel.getParameter(parameter.getId()) != null)) {
									miniModel.addParameter(parameter.clone());
								}
								
								if (progressAdapter != null) {
									progressAdapter.progressOn();
								}
							}
						}
						reac.setKineticLaw(l.clone());
					}
				}
			}
		}
		if (progressAdapter != null) {
			progressAdapter.finished();
		}
		
		return miniModel;
	}
	
	/**
	 * 
	 * @param speciesOrig
	 * @param miniModel
	 * @return
	 */
	private Species copySpecies(Species speciesOrig, Model miniModel) {
		if (miniModel.getSpecies(speciesOrig.getId()) == null) {
			miniModel.addSpecies(speciesOrig.clone());
		}
		Species spec = miniModel.getSpecies(speciesOrig.getId());
		if (!spec.isSetHasOnlySubstanceUnits() && (2 < spec.getLevel())) {
			spec.setHasOnlySubstanceUnits(defaultHasOnlySubstanceUnits);
		}
		Compartment compartment = miniModel.getCompartment(speciesOrig.getCompartment());
		if (compartment == null) {
			compartment = copyCompartment(speciesOrig.getCompartmentInstance(), miniModel);
		}
		spec.setCompartment(compartment);
		
		if (spec.isSetSubstanceUnits()
				&& !Unit.isUnitKind(spec.getUnits(), spec.getLevel(), spec.getVersion())) {
			if (miniModel.getUnitDefinition(spec.getUnits()) == null) {
				UnitDefinition ud = speciesOrig.getSubstanceUnitsInstance();
				if (ud != null) {
					miniModel.addUnitDefinition(ud.clone());
				} else {
					checkUnits(spec, miniModel);
				}
			}
			spec.setSubstanceUnits(miniModel.getUnitDefinition(spec.getUnits()));
		} else {
			checkUnits(spec, miniModel);
		}
		/*
		if (speciesOrig.isSetSubstanceUnits()
				&& !Unit.isUnitKind(speciesOrig.getUnits(), speciesOrig
						.getLevel(), speciesOrig.getVersion())) {
			if (miniModel.getUnitDefinition(speciesOrig.getSubstanceUnits()) == null) {
				miniModel.addUnitDefinition(((UnitDefinition) speciesOrig
						.getSubstanceUnitsInstance()).clone());
			}
			spec.setSubstanceUnits(miniModel.getUnitDefinition(speciesOrig
					.getSubstanceUnits()));
		} else {
			checkUnits(spec);
		}*/
		return spec;
	}
	
	/**
	 * 
	 * @param compartmenOrig
	 * @param miniModel
	 * @return
	 */
	private Compartment copyCompartment(Compartment compartmenOrig, Model miniModel) {
		
		if (miniModel.getCompartment(compartmenOrig.getId()) == null) {
			miniModel.addCompartment(compartmenOrig.clone());
		}
		Compartment compartment = miniModel.getCompartment(compartmenOrig.getId());
		// TODO: create a user-configuration for this!
		if (!compartment.isSetSpatialDimensions()) {
			compartment.setSpatialDimensions(defaultSpatialDimensions);
		}
		if (compartment.isSetUnits()) {
			if (!Unit.isUnitKind(compartment.getUnits(), compartment.getLevel(), compartment.getVersion())) {
				if (miniModel.getUnitDefinition(compartment.getUnits()) == null) {
					UnitDefinition ud = compartmenOrig.getUnitsInstance();
					// TODO: Set L3-specific fall-back units for compartments!
					if (ud != null) {
						miniModel.addUnitDefinition(ud.clone());
					} else {
						checkUnits(compartment, miniModel);
					}
				}
				compartment.setUnits(miniModel.getUnitDefinition(compartment.getUnits()));
			}
		} else {
			checkUnits(compartment, miniModel);
		}
		return compartment;
	}
	
	/**
	 * Stores all units created in the mini model in the original model.
	 * 
	 * @param l
	 */
	public void storeUnits() {
		for (UnitDefinition ud : miniModel.getListOfUnitDefinitions()) {
			int unitsCount = ud.getUnitCount();
			if (modelOrig.getUnitDefinition(ud.getId()) == null) {
				modelOrig.addUnitDefinition(ud.clone());
			} else {
				UnitDefinition orig = modelOrig.getUnitDefinition(ud.getId());
				if (!UnitDefinition.areIdentical(orig, ud)) {
					if (orig.isPredefined()
							&& modelOrig.getListOfPredefinedUnitDefinitions().remove(orig)) {
						modelOrig.addUnitDefinition(orig);
					}
					orig.setListOfUnits(ud.getListOfUnits().clone());
					orig.simplify();
					if (ud.isSetName()) {
						orig.setName(new String(ud.getName()));
					}
				}
			}
			if (progressAdapter != null) {
				progressAdapter.setNumberOfTags(modelOrig, miniModel, removeUnnecessaryParameters);
				progressAdapter.progressOn();
			}
			if (unitsCount != ud.getUnitCount()) {
				logger.log(Level.WARNING, ud.getId() + "\t" + unitsCount + "\t->\t"
						+ ud.getUnitCount());
			}
		}
		
		// Set "default" units
		if (miniModel.isSetAreaUnits()) {
			if (!modelOrig.isSetAreaUnits() || !miniModel.getAreaUnits().equals(modelOrig.getAreaUnits())) {
				modelOrig.setAreaUnits(miniModel.getAreaUnits());
			}
		}
		if (miniModel.isSetLengthUnits() || !miniModel.getLengthUnits().equals(modelOrig.getLengthUnits())) {
			if (!modelOrig.isSetLengthUnits()) {
				modelOrig.setLengthUnits(miniModel.getLengthUnits());
			}
		}
		if (miniModel.isSetExtentUnits() || !miniModel.getExtentUnits().equals(modelOrig.getExtentUnits())) {
			if (!modelOrig.isSetExtentUnits()) {
				modelOrig.setExtentUnits(miniModel.getExtentUnits());
			}
		}
		if (miniModel.isSetTimeUnits() || !miniModel.getTimeUnits().equals(modelOrig.getTimeUnits())) {
			if (!modelOrig.isSetTimeUnits()) {
				modelOrig.setTimeUnits(miniModel.getTimeUnits());
			}
		}
		if (miniModel.isSetSubstanceUnits() || !miniModel.getSubstanceUnits().equals(modelOrig.getSubstanceUnits())) {
			if (!modelOrig.isSetSubstanceUnits()) {
				modelOrig.setSubstanceUnits(miniModel.getSubstanceUnits());
			}
		}
		if (miniModel.isSetVolumeUnits() || !miniModel.getVolumeUnits().equals(modelOrig.getVolumeUnits())) {
			if (!modelOrig.isSetVolumeUnits()) {
				modelOrig.setVolumeUnits(modelOrig.getVolumeUnits());
			}
		}
		
		for (Compartment c : miniModel.getListOfCompartments()) {
			Compartment compOrig = modelOrig.getCompartment(c.getId());
			// if level > 1, set spatialDimension
			// the property spatialDimension is available since l2v1
			if (miniModel.getLevel() > 1) {
				compOrig.setSpatialDimensions(c.getSpatialDimensions());
			}
			if (c.isSetUnits()) {
				if (Unit.isUnitKind(c.getUnits(), c.getLevel(), c.getVersion())) {
					compOrig.setUnits(Unit.Kind.valueOf(c.getUnits().toUpperCase()));
				} else {
					compOrig.setUnits(modelOrig.getUnitDefinition(c.getUnits()));
				}
			}
			checkUnits(compOrig, modelOrig);
			if (progressAdapter != null) {
				progressAdapter.setNumberOfTags(modelOrig, miniModel, removeUnnecessaryParameters);
				progressAdapter.progressOn();
			}
		}
		
		for (Species spec : miniModel.getListOfSpecies()) {
			Species specOrig = modelOrig.getSpecies(spec.getId());
			// if level > 1, set hasOnlySubstanceUnits
			// the hasOnlySubstanceUnits property is available since l2v1
			if (miniModel.getLevel() > 1) {
				specOrig.setHasOnlySubstanceUnits(spec.getHasOnlySubstanceUnits());
			}
			if (spec.isSetSubstanceUnits()) {
				if (Unit.isUnitKind(spec.getSubstanceUnits(), spec.getLevel(), spec.getVersion())) {
					specOrig.setSubstanceUnits(Unit.Kind.valueOf(spec.getSubstanceUnits().toUpperCase()));
				} else {
					specOrig.setSubstanceUnits(modelOrig.getUnitDefinition(spec.getSubstanceUnits()));
				}
			}
			checkUnits(specOrig, modelOrig);
			
			if (progressAdapter != null) {
				progressAdapter.setNumberOfTags(modelOrig, miniModel, removeUnnecessaryParameters);
				progressAdapter.progressOn();
			}
		}
	}
	
	/**
	 * This method stores all newly created parameters in the model and sets the
	 * references to the units appropriately. UnitDefinitions that are not
	 * referenced by any object are removed from the model.
	 * 
	 * @param reaction
	 */
	private void storeParamters(Reaction reaction) {
		// setInitialConcentrationTo(reaction, 1d);
		KineticLaw kineticLaw = reaction.getKineticLaw();
		if (kineticLaw.isSetListOfLocalParameters()) {
			ListOf<LocalParameter> paramListLocal = kineticLaw.getListOfLocalParameters();
			for (int paramNum = paramListLocal.size() - 1; paramNum >= 0; paramNum--) {
				if (addParametersGlobally) {
					Parameter p = new Parameter(paramListLocal.remove(paramNum));
					if (modelOrig.getParameter(p.getId()) != null) {
						modelOrig.removeParameter(p.getId());
					}
					modelOrig.addParameter(p);
					updateUnitReferences(p);
				} else {
					updateUnitReferences(paramListLocal.get(paramNum));
				}
			}
		}
		for (Parameter parameter : miniModel.getListOfParameters()) {
			if (modelOrig.getParameter(parameter.getId()) == null) {
				modelOrig.addParameter(parameter.clone());
				updateUnitReferences(modelOrig.getParameter(parameter.getId()));
			}
			
		}
		if (!kineticLaw.isSetMath()) {
			// TODO: Localize!
			logger.log(Level.SEVERE, MessageFormat.format("No math element defined for reaction {0}", reaction.getId()));
		} else {
			kineticLaw.getMath().updateVariables();
		}
		if (progressAdapter != null) {
			progressAdapter.setNumberOfTags(modelOrig, miniModel, removeUnnecessaryParameters);
			progressAdapter.progressOn();
		}
	}
	
	/**
	 * Moves the pointers from the units in the mini Model to the units in the
	 * original model.
	 * 
	 * @param p
	 */
	private void updateUnitReferences(QuantityWithUnit p) {
		if (p.isSetUnits()) {
			String units = p.getUnits();
			if (Unit.isUnitKind(units, p.getLevel(), p.getVersion())) {
				p.setUnits(Unit.Kind.valueOf(units.toUpperCase()));
			} else {
				UnitDefinition ud = modelOrig.getUnitDefinition(p.getUnits());
				if (ud == null) {
					ud = miniModel.getUnitDefinition(p.getUnits());
					modelOrig.addUnitDefinition(ud.clone());
				}
				p.setUnits(ud);
			}
		}
	}
	
	/**
	 * 
	 * @param kineticLaw
	 * @param removeParametersAndStoreUnits
	 * @param l
	 * @return
	 */
	public Reaction storeKineticLaw(KineticLaw kineticLaw,
		boolean removeParametersAndStoreUnits) {
		Reaction reaction = modelOrig.getReaction(kineticLaw.getParentSBMLObject().getId());
		reaction.setReversible(reversibility || reaction.getReversible());
		reaction.setKineticLaw(kineticLaw.clone());
		// set the BoundaryCondition to true for genes if not set anyway:
		if (reaction.isSetListOfReactants()) {
			setBoundaryCondition(reaction.getListOfReactants(), setBoundaryCondition);
		}
		if (reaction.isSetListOfProducts()) {
			setBoundaryCondition(reaction.getListOfProducts(), setBoundaryCondition);
		}
		setInitialConcentrationTo(reaction, defaultSpeciesInitVal, defaultCompartmentInitSize);
		if (removeParametersAndStoreUnits) {
			storeUnits();
		}
		storeParamters(reaction);
		if (removeParametersAndStoreUnits && removeUnnecessaryParameters) {
			/*
			 * delete unnecessary units.
			 */
			removeUnnecessaryParameters(modelOrig);
			removeUnnecessaryUnits(modelOrig);
		}
		// TODO: Such a check would be great, but it takes much too long!
		//		UnitDefinition ud = reaction.getDerivedUnitDefinition();
		//		if (ud == null) {
		//			logger.warning("Could not derive units for reaction " + reaction.getId());
		//		} else if (!ud.isVariantOfSubstancePerTime()) {
		//			logger.warning("Units of kinetic law " + kineticLaw.getClass().getSimpleName() + " of reaction " + reaction.getId()
		//					+ " cannot be varified to be substance per time units (given: "
		//					+ UnitDefinition.printUnits(ud, true) + ").");
		//		}
		kineticLaw = reaction.getKineticLaw();
		if (kineticLaw.getLocalParameterCount() == 0) {
			kineticLaw.unsetListOfLocalParameters();
		}
		
		return reaction;
	}

	/**
	 * 
	 * @param model
	 */
	public void removeUnnecessaryUnits(Model model) {
		int i, level = model.getLevel();
		Set<String> neededUnits = new HashSet<String>();
		
		// Check compartments
		for (Compartment compartment : model.getListOfCompartments()) {
			if (compartment.isSetUnits()) {
				neededUnits.add(compartment.getUnits());
			} else if ((level > 2) && compartment.isSetSpatialDimensions()) {
				double dim = compartment.getSpatialDimensions();
				if ((dim == 3d) && model.isSetVolumeUnits()) {
					neededUnits.add(model.getVolumeUnits());
				} else if ((dim == 2d) && model.isSetAreaUnits()) {
					neededUnits.add(model.getAreaUnits());
				} else if ((dim == 1d) && model.isSetLengthUnits()) {
					neededUnits.add(model.getLengthUnits());
				}
			}
		}
		// Check species
		for (Species species : model.getListOfSpecies()) {
			if (species.isSetSubstanceUnits()) {
				neededUnits.add(species.getSubstanceUnits());
			} else if ((level > 2) && model.isSetSubstanceUnits()) {
				neededUnits.add(model.getSubstanceUnits());
			}
		}
		// Check parameters
		for (Parameter parameter : model.getListOfParameters()) {
			if (parameter.isSetUnits()) {
				neededUnits.add(parameter.getUnits());
			}
		}
		if (level > 2) {
			if (model.isSetTimeUnits()) {
				neededUnits.add(model.getTimeUnits());
			}
			if (model.isSetExtentUnits() && (model.getReactionCount() > 0)) {
				neededUnits.add(model.getExtentUnits());
			}
		}
		// Check local parameters
		for (Reaction reaction : model.getListOfReactions()) {
			if (reaction.isSetKineticLaw() && reaction.getKineticLaw().isSetListOfLocalParameters()) {
				for (LocalParameter localParameter : reaction.getKineticLaw().getListOfLocalParameters()) {
					if (localParameter.isSetUnits()) {
						neededUnits.add(localParameter.getUnits());
					}
				}
			}
		}
		for (i = model.getUnitDefinitionCount() - 1; i >= 0; i--) {
			UnitDefinition udef = model.getUnitDefinition(i);
			if (!Unit.isPredefined(udef) && !neededUnits.contains(udef.getId())) {
				model.removeUnitDefinition(udef);
			}
			if (progressAdapter != null) {
				progressAdapter.progressOn();
			}
		}
		
		if (level > 2) {
			int version = model.getVersion();
			if (model.isSetAreaUnits()
					&& !Unit.Kind.isValidUnitKindString(model.getAreaUnits(), level, version) 
					&& (model.getUnitDefinition(model.getAreaUnits()) == null)) {
				model.unsetAreaUnits();
			}
			if (model.isSetExtentUnits()
					&& !Unit.Kind.isValidUnitKindString(model.getExtentUnits(), level, version) 
					&& (model.getUnitDefinition(model.getExtentUnits()) == null)) {
				model.unsetExtentUnits();
			}
			if (model.isSetLengthUnits()
					&& !Unit.Kind.isValidUnitKindString(model.getLengthUnits(), level, version) 
					&& (model.getUnitDefinition(model.getLengthUnits()) == null)) {
				model.unsetLengthUnits();
			}
			if (model.isSetSubstanceUnits()
					&& !Unit.Kind.isValidUnitKindString(model.getSubstanceUnits(), level, version) 
					&& (model.getUnitDefinition(model.getSubstanceUnits()) == null)) {
				model.unsetSubstanceUnits();
			}
			if (model.isSetTimeUnits()
					&& !Unit.Kind.isValidUnitKindString(model.getTimeUnits(), level, version) 
					&& (model.getUnitDefinition(model.getTimeUnits()) == null)) {
				model.unsetTimeUnits();
			}
			if (model.isSetVolumeUnits()
					&& !Unit.Kind.isValidUnitKindString(model.getVolumeUnits(), level, version) 
					&& (model.getUnitDefinition(model.getVolumeUnits()) == null)) {
				model.unsetVolumeUnits();
			}
		}

	}
	
	/**
	 * Delete unnecessary parameters from the model. A parameter is defined to
	 * be unnecessary if and only if no kinetic law, no event assignment, no
	 * rule and no function makes use of this parameter.
	 * 
	 * @param selectedModel
	 */
	@SuppressWarnings("deprecation")
	public void removeUnnecessaryParameters(Model model) {
		boolean isNeeded;
		int i, j, k = 0;
		Parameter p;
		// remove unnecessary global parameters
		for (i = model.getParameterCount() - 1; i >= 0; i--) {
			isNeeded = false;
			p = model.getParameter(i);
			/*
			 * Is this parameter necessary for some kinetic law or is this
			 * parameter a conversion factor in some stoichiometric math?
			 */
			for (j = 0; (j < model.getReactionCount()) && !isNeeded; j++) {
				Reaction r = model.getReaction(j);
				if (r.isSetKineticLaw() && r.getKineticLaw().isSetMath()
						&& r.getKineticLaw().getMath().refersTo(p.getId())) {
					/*
					 * ok, parameter occurs here but there could also be a local
					 * parameter with the same id.
					 */
					if (r.getKineticLaw().getLocalParameter(p.getId()) == null) {
						isNeeded = true;
					}
				}
				if (isNeeded) {
					break;
				}
				SpeciesReference specRef;
				for (k = 0; k < r.getReactantCount(); k++) {
					specRef = r.getReactant(k);
					if (specRef.isSetStoichiometryMath()
							&& specRef.getStoichiometryMath().isSetMath()
							&& specRef.getStoichiometryMath().getMath().refersTo(p.getId())) {
						isNeeded = true;
					}
				}
				if (isNeeded) {
					break;
				}
				for (k = 0; k < r.getProductCount(); k++) {
					specRef = r.getProduct(k);
					if (specRef.isSetStoichiometryMath()
							&& specRef.getStoichiometryMath().isSetMath()
							&& specRef.getStoichiometryMath().getMath().refersTo(p.getId())) {
						isNeeded = true;
					}
				}
			}
			
			// is this parameter necessary for some rule?
			for (j = 0; (j < model.getRuleCount()) && !isNeeded; j++) {
				Rule r = model.getRule(j);
				if ((r instanceof AssignmentRule)
						&& ((AssignmentRule) r).getVariable().equals(p.getId())) {
					isNeeded = true;
				}
				if ((r instanceof RateRule)
						&& ((RateRule) r).getVariable().equals(p.getId())) {
					isNeeded = true;
				}
				if (r.isSetMath() && r.getMath().refersTo(p.getId())) {
					isNeeded = true;
				}
			}
			
			// is this parameter necessary for some event?
			for (j = 0; (j < model.getEventCount()) && !isNeeded; j++) {
				Event e = model.getEvent(j);
				if (e.isSetTrigger() && e.getTrigger().isSetMath()
						&& e.getTrigger().getMath().refersTo(p.getId())) {
					isNeeded = true;
				}
				if (e.isSetDelay() && e.getDelay().isSetMath()
						&& e.getDelay().getMath().refersTo(p.getId())) {
					isNeeded = true;
				}
				for (k = 0; k < model.getEvent(j).getEventAssignmentCount()
						&& !isNeeded; k++) {
					EventAssignment ea = e.getEventAssignment(k);
					if ((ea.isSetVariable() && ea.getVariable().equals(p.getId()))
							|| ea.isSetMath() && ea.getMath().refersTo(p.getId())) {
						isNeeded = true;
					}
				}
			}
			
			// is this parameter necessary for some function?
			for (j = 0; j < model.getFunctionDefinitionCount() && !isNeeded; j++) {
				FunctionDefinition fd = model.getFunctionDefinition(j);
				if (fd.isSetMath() && fd.getMath().refersTo(p.getId())) {
					isNeeded = true;
				}
			}
			
			// is this parameter necessary for some initial assignment?
			for (j = 0; j < model.getInitialAssignmentCount() && !isNeeded; j++) {
				InitialAssignment ia = model.getInitialAssignment(j);
				if ((ia.isSetVariable() && ia.getVariable().equals(p.getId()))
						|| ia.isSetMath() && ia.getMath().refersTo(p.getId())) {
					isNeeded = true;
				}
			}
			
			// is this parameter necessary for some constraint?
			for (j = 0; j < model.getConstraintCount() && !isNeeded; j++) {
				Constraint c = model.getConstraint(j);
				if (c.isSetMath() && c.getMath().refersTo(p.getId())) {
					isNeeded = true;
				}
			}
			
			if (!isNeeded) { // is this parameter necessary at all?
			  model.removeParameter(i);
			}
			
			if (progressAdapter != null) {
				progressAdapter.progressOn();
			}
			
		}
	  if (model.getParameterCount() == 0) {
	  	model.unsetListOfParameters();
	  }
	  
		// remove unnecessary local parameters
		for (i = 0; i < model.getReactionCount(); i++) {
			Reaction r = model.getReaction(i);
			if (r.isSetKineticLaw()) {
				KineticLaw law = r.getKineticLaw();
				for (j = law.getLocalParameterCount() - 1; j >= 0; j--) {
					if (law.isSetMath()
							&& !law.getMath().refersTo(
								law.getLocalParameter(j).getId())) {
						law.removeLocalParameter(j);
					}
				}
			}
			if (progressAdapter != null) {
				progressAdapter.progressOn();
			}
			
		}
	}
	
	/**
	 * Sets the boundary condition of all species referenced by the list of
	 * {@link SpeciesReference}s.
	 * 
	 * @param numReactants
	 * @param setBoundary
	 */
	private void setBoundaryCondition(ListOf<SpeciesReference> listOf,
		boolean setBoundary) {
		for (int i = 0; setBoundary && (i < listOf.size()); i++) {
			Species species = listOf.get(i).getSpeciesInstance();
			if (SBO.isGeneOrGeneCodingRegion(species.getSBOTerm()) || 
					SBO.isEmptySet(species.getSBOTerm())) {
				setBoundaryCondition(species, true);
			}
		}
	}
	
	/**
	 * set the boundaryCondition for a gene to the given value
	 * 
	 * @param species
	 * @param condition
	 */
	private void setBoundaryCondition(Species species, boolean condition) {
		if (condition != species.getBoundaryCondition()) {
			species.setBoundaryCondition(condition);
		}
	}
	
	/**
	 * Sets the initial amounts of all modifiers, reactants and products to the
	 * specified value.
	 * 
	 * @param reaction
	 * @param initialValue
	 * @param sizeValue
	 */
	private void setInitialConcentrationTo(Reaction reaction,
		double initialValue, double sizeValue) {
		Species species;
		for (int reactant = 0; reactant < reaction.getReactantCount(); reactant++) {
			species = reaction.getReactant(reactant).getSpeciesInstance();
			initializeSpeciesAndCompartmentIfNecessary(species, initialValue,
				species.getCompartmentInstance(), sizeValue);
		}
		for (int product = 0; product < reaction.getProductCount(); product++) {
			species = reaction.getProduct(product).getSpeciesInstance();
			initializeSpeciesAndCompartmentIfNecessary(species, initialValue,
				species.getCompartmentInstance(), sizeValue);
		}
		for (int modifier = 0; modifier < reaction.getModifierCount(); modifier++) {
			species = reaction.getModifier(modifier).getSpeciesInstance();
			initializeSpeciesAndCompartmentIfNecessary(species, initialValue,
				species.getCompartmentInstance(), sizeValue);
		}
	}
	
	/**
	 * 
	 * @param species
	 * @param initialValue
	 * @param compartmentInstance
	 * @param sizeValue
	 */
	private void initializeSpeciesAndCompartmentIfNecessary(Species species,
		double initialValue, Compartment compartment, double sizeValue) {
		if (!species.isSetInitialAmount()
				&& !species.isSetInitialConcentration()) {
			if (species.getHasOnlySubstanceUnits()) {
				species.setInitialAmount(initialValue);
			} else {
				species.setInitialConcentration(initialValue);
			}
		}
		if ((compartment != null) && !compartment.isSetSize()) {
			// set size, if the spatialDimension Property is set and > 0
			if (compartment.isSetSpatialDimensions() && (compartment.getSpatialDimensions() > 0)) {
				compartment.setSize(sizeValue);
			}
		}
	}
	
	
	
	/**
	 * 
	 * @param compartment
	 * @param modelToWrite
	 */
	private void checkUnits(Compartment compartment, Model modelToWrite) {
		Model model = compartment.getModel();
		/*
		 *  for level 2 and 3 the compartment size does not have to be set when 
		 *  the spatialDimensions field is 0.  
		 */
		double spatialD = compartment.getSpatialDimensions();
		if (!compartment.isSetSize() && 
				(spatialD != 0d) || (modelToWrite.getLevel() < 2)) {
			//TODO: Option for setting the initial compartment size
			compartment.setValue(1d);
		}
		
		
		//		volumeUD = miniModel.getUnitDefinition(UnitDefinition.VOLUME);
		//		if (volumeUD == null) {
		//			// This may happen in Level 3.
		//			// TODO: Depending on which compartments are in the model, we might need also Area and Length!
		//			volumeUD = UnitDefinition.getPredefinedUnit(UnitDefinition.VOLUME, 2, 4);
		//			SBMLtools.setLevelAndVersion(volumeUD, level, version);
		//			miniModel.setVolumeUnits(volumeUD);
		//		}
		///
		
		
		if (!compartment.isSetUnits() && (((short) spatialD) - spatialD == 0d)) {
			UnitDefinition ud;
			switch ((short) spatialD) {
				case 1:
					ud = model.getLengthUnitsInstance();
					if (ud == null) {
						ud = UnitDefinition.getPredefinedUnit(UnitDefinition.LENGTH, 2, 4);
						SBMLtools.setLevelAndVersion(ud, modelToWrite.getLevel(), modelToWrite.getVersion());
						modelToWrite.setLengthUnits(ud);
					}
					compartment.setUnits(ud);
					break;
				case 2:
					ud = model.getAreaUnitsInstance();
					if (ud == null) {
						ud = UnitDefinition.getPredefinedUnit(UnitDefinition.AREA, 2, 4);
						SBMLtools.setLevelAndVersion(ud, modelToWrite.getLevel(), modelToWrite.getVersion());
						modelToWrite.setAreaUnits(ud);
					}
					compartment.setUnits(ud);
					break;
				case 3:
					ud = model.getUnitDefinition(UnitDefinition.VOLUME);
					if (ud == null) {
						ud = UnitDefinition.getPredefinedUnit(UnitDefinition.VOLUME, 2, 4);
						SBMLtools.setLevelAndVersion(ud, modelToWrite.getLevel(), modelToWrite.getVersion());
						modelToWrite.setVolumeUnits(ud);
					}
					compartment.setUnits(ud);
					break;
				default:
					break;
			}
		}
		if (((spatialD <= 0d) || (spatialD > 3d)) && (1 < compartment.getLevel())) {
			compartment.setSpatialDimensions(3d);
			compartment.setUnits(model.getUnitDefinition(UnitDefinition.VOLUME));
			logger.warning(MessageFormat.format(
				WARNINGS.getString("INVALID_COMPARTMENT_DIMENSION"),
				compartment.getId()));
		}
	}
	
	/**
	 * check units of species and surrounding compartment.
	 * 
	 * @param species
	 */
	private void checkUnits(Species species, Model miniModel) {
		if (!species.isSetSubstanceUnits()
				|| !species.getSubstanceUnitsInstance().isVariantOfSubstance()) {
			UnitDefinition ud = species.getModel().getUnitDefinition(UnitDefinition.SUBSTANCE);
			if (ud == null) {
				ud = UnitDefinition.getPredefinedUnit(UnitDefinition.SUBSTANCE, 2, 4);
				SBMLtools.setLevelAndVersion(ud, miniModel.getLevel(), miniModel.getVersion());
				miniModel.setSubstanceUnits(ud);
			}
			species.setSubstanceUnits(ud);
		}
	}
	
	/**
	 * 
	 * @return
	 */
	public boolean isRemoveUnnecessaryParameters() {
		return this.removeUnnecessaryParameters;
	}
	
	/**
	 * 
	 * @return
	 */
	public boolean isAddParametersGlobally() {
		return addParametersGlobally;
	}
	
	/**
	 * 
	 * @return
	 */
	public boolean isReversibility() {
		return reversibility;
	}
	
	/**
	 * 
	 * @return
	 */
	public boolean isSetBoundaryCondition() {
		return setBoundaryCondition;
	}
	
}
