/*
 * $Id$
 * $URL$
 * ---------------------------------------------------------------------
 * This file is part of SBMLsqueezer, a Java program that creates rate
 * equations for reactions in SBML files (http://sbml.org).
 *
 * Copyright (C) 2006-2015 by the University of Tuebingen, Germany.
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
import de.zbit.util.progressbar.AbstractProgressBar;

/**
 * This class is responsible for miniModel creation and the synchronisation
 * with the main {@link Model}
 * 
 * @since 2.0
 * @version $Rev$
 * @author Andreas Dr&auml;ger
 * @author Sebastian Nagel
 */
public class SubmodelController {
  
  /**
   * Localization support.
   */
  public static final transient ResourceBundle WARNINGS = ResourceManager.getBundle(Bundles.WARNINGS);
  
  /**
   * A {@link Logger} for this class.
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
  /**
   * A copy of the model that only covers all compartments, all parameters,
   * and all reactions for which kinetic equations are to be created. Hence,
   * it also contains all other information needed for that purpose, i.e., all
   * species that participate as reactants, products, or modifiers in at least
   * one of these reactions.
   */
  private Model submodel;
  
  private ProgressAdapter progressAdapter;
  private AbstractProgressBar progressBar;
  
  private double defaultSpatialDimensions = 3;
  
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
    return submodel;
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
    modelOrig = model;
  }
  
  /**
   * Creates a minimal copy of the original model that only covers those
   * elements needed for the creation of rate equations.
   * 
   * @param reactionID
   * 
   * @return
   */
  public Model createSubmodel(String reactionID) {
    
    if (progressBar != null) {
      progressAdapter = new ProgressAdapter(progressBar, TypeOfProgress.createMiniModel);
      progressAdapter.setNumberOfTags(modelOrig, submodel, removeUnnecessaryParameters);
    }
    
    boolean create = generateLawsForAllReactions;
    int level = modelOrig.getLevel(), version = modelOrig.getVersion();
    SBMLDocument miniDoc = new SBMLDocument(level, version);
    submodel = miniDoc.createModel("submodel_" + modelOrig.getId());
    //miniModel.addChangeListener(new ModelChangeListener());
    
    /*
     * Set default unit definitions if it is not already set
     */
    if (modelOrig.isSetAreaUnitsInstance()) {
      UnitDefinition areaUD = modelOrig.getAreaUnitsInstance().clone();
      if ((level > 2) && (!submodel.isSetAreaUnitsInstance() || !UnitDefinition.areIdentical(areaUD, submodel.getAreaUnitsInstance()))) {
        submodel.setAreaUnits(areaUD.clone());
      }
    } else {
      if (modelOrig.isSetAreaUnits() && Unit.Kind.isValidUnitKindString(modelOrig.getAreaUnits(), level, version)) {
        submodel.setAreaUnits(modelOrig.getAreaUnits());
      } else {
        UnitDefinition areaUD = UnitDefinition.getPredefinedUnit(UnitDefinition.AREA, 2, 4);
        org.sbml.jsbml.util.SBMLtools.setLevelAndVersion(areaUD, level, version);
        submodel.addUnitDefinition(areaUD);
        if (level > 2) {
          submodel.setAreaUnits(areaUD.getId());
        }
      }
    }
    
    if (modelOrig.isSetLengthUnitsInstance()) {
      UnitDefinition lengthUD = modelOrig.getLengthUnitsInstance().clone();
      if ((level > 2) && (!submodel.isSetLengthUnitsInstance() || !UnitDefinition.areIdentical(lengthUD, submodel.getLengthUnitsInstance()))) {
        submodel.setLengthUnits(lengthUD.clone());
      }
    } else {
      if (modelOrig.isSetLengthUnits() && Unit.Kind.isValidUnitKindString(modelOrig.getLengthUnits(), level, version)) {
        submodel.setLengthUnits(modelOrig.getLengthUnits());
      } else {
        UnitDefinition lengthUD = UnitDefinition.getPredefinedUnit(UnitDefinition.LENGTH, 2, 4);
        org.sbml.jsbml.util.SBMLtools.setLevelAndVersion(lengthUD, level, version);
        submodel.addUnitDefinition(lengthUD);
        if (level > 2) {
          submodel.setLengthUnits(lengthUD.getId());
        }
      }
    }
    
    if (modelOrig.isSetSubstanceUnitsInstance()) {
      UnitDefinition substanceUD = modelOrig.getSubstanceUnitsInstance().clone();
      if ((level > 2) && (!submodel.isSetSubstanceUnitsInstance() || !UnitDefinition.areIdentical(substanceUD, submodel.getSubstanceUnitsInstance()))) {
        submodel.setSubstanceUnits(substanceUD.clone());
      }
    } else {
      if (modelOrig.isSetSubstanceUnits() && Unit.Kind.isValidUnitKindString(modelOrig.getSubstanceUnits(), level, version)) {
        submodel.setSubstanceUnits(modelOrig.getSubstanceUnits());
      } else {
        UnitDefinition substanceUD = UnitDefinition.getPredefinedUnit(UnitDefinition.SUBSTANCE, 2, 4);
        org.sbml.jsbml.util.SBMLtools.setLevelAndVersion(substanceUD, level, version);
        submodel.addUnitDefinition(substanceUD);
        if (level > 2) {
          submodel.setSubstanceUnits(substanceUD.getId());
        }
      }
    }
    
    if (modelOrig.isSetTimeUnitsInstance()) {
      UnitDefinition timeUD = modelOrig.getTimeUnitsInstance().clone();
      if ((level > 2) && (!submodel.isSetTimeUnitsInstance() || !UnitDefinition.areIdentical(timeUD, submodel.getTimeUnitsInstance()))) {
        submodel.setTimeUnits(timeUD.clone());
      }
    } else {
      if (modelOrig.isSetTimeUnits() && Unit.Kind.isValidUnitKindString(modelOrig.getTimeUnits(), level, version)) {
        submodel.setTimeUnits(modelOrig.getTimeUnits());
      } else {
        UnitDefinition timeUD = UnitDefinition.getPredefinedUnit(UnitDefinition.TIME, 2, 4);
        org.sbml.jsbml.util.SBMLtools.setLevelAndVersion(timeUD, level, version);
        submodel.addUnitDefinition(timeUD);
        if (level > 2) {
          submodel.setTimeUnits(timeUD.getId());
        }
      }
    }
    
    if (modelOrig.isSetVolumeUnitsInstance()) {
      UnitDefinition volumeUD = modelOrig.getVolumeUnitsInstance().clone();
      if ((level > 2) && (!submodel.isSetVolumeUnitsInstance() || !UnitDefinition.areIdentical(volumeUD, submodel.getVolumeUnitsInstance()))) {
        submodel.setVolumeUnits(volumeUD.clone());
      }
    } else {
      if (modelOrig.isSetVolumeUnits() && Unit.Kind.isValidUnitKindString(modelOrig.getVolumeUnits(), level, version)) {
        submodel.setVolumeUnits(modelOrig.getVolumeUnits());
      } else {
        UnitDefinition volumeUD = UnitDefinition.getPredefinedUnit(UnitDefinition.VOLUME, 2, 4);
        org.sbml.jsbml.util.SBMLtools.setLevelAndVersion(volumeUD, level, version);
        submodel.addUnitDefinition(volumeUD);
        if (level > 2) {
          submodel.setVolumeUnits(volumeUD.getId());
        }
      }
    }
    
    UnitDefinition extentUD = modelOrig.getExtentUnitsInstance();
    if (extentUD != null) {
      submodel.setExtentUnits(extentUD.clone());
    } else if (level > 2) {
      submodel.setExtentUnits(submodel.getSubstanceUnits());
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
          if (reacOrig.isSetAnnotation()) {
            reac.setAnnotation(reacOrig.getAnnotation().clone());
            reac.getAnnotation().setAbout(reacOrig.getAnnotation().getAbout());
          }
          submodel.addReaction(reac);
          reac.setFast(reacOrig.getFast());
          reac.setReversible(reacOrig.getReversible());
          if (reacOrig.isSetListOfReactants()) {
            for (SpeciesReference specRefOrig : reacOrig.getListOfReactants()) {
              Species speciesOrig = specRefOrig.getSpeciesInstance();
              SpeciesReference sr = specRefOrig.clone();
              sr.setSpecies(copySpecies(speciesOrig, submodel));
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
              sr.setSpecies(copySpecies(speciesOrig, submodel));
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
              modifierReference.setSpecies(copySpecies(speciesOrig, submodel));
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
                    && (submodel.getParameter(parameter.getId()) != null)) {
                  submodel.addParameter(parameter.clone());
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
    
    return submodel;
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
   */
  public void storeUnits() {
    for (UnitDefinition ud : submodel.getListOfUnitDefinitions()) {
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
        progressAdapter.setNumberOfTags(modelOrig, submodel, removeUnnecessaryParameters);
        progressAdapter.progressOn();
      }
      if (unitsCount != ud.getUnitCount()) {
        logger.log(Level.WARNING, ud.getId() + "\t" + unitsCount + "\t->\t"
            + ud.getUnitCount());
      }
    }
    
    // Set "default" units
    if (submodel.isSetAreaUnits()) {
      if (!modelOrig.isSetAreaUnits() || !submodel.getAreaUnits().equals(modelOrig.getAreaUnits())) {
        modelOrig.setAreaUnits(submodel.getAreaUnits());
      }
    }
    if (submodel.isSetLengthUnits() || !submodel.getLengthUnits().equals(modelOrig.getLengthUnits())) {
      if (!modelOrig.isSetLengthUnits()) {
        modelOrig.setLengthUnits(submodel.getLengthUnits());
      }
    }
    if (submodel.isSetExtentUnits() || !submodel.getExtentUnits().equals(modelOrig.getExtentUnits())) {
      if (!modelOrig.isSetExtentUnits()) {
        modelOrig.setExtentUnits(submodel.getExtentUnits());
      }
    }
    if (submodel.isSetTimeUnits() || !submodel.getTimeUnits().equals(modelOrig.getTimeUnits())) {
      if (!modelOrig.isSetTimeUnits()) {
        modelOrig.setTimeUnits(submodel.getTimeUnits());
      }
    }
    if (submodel.isSetSubstanceUnits() || !submodel.getSubstanceUnits().equals(modelOrig.getSubstanceUnits())) {
      if (!modelOrig.isSetSubstanceUnits()) {
        modelOrig.setSubstanceUnits(submodel.getSubstanceUnits());
      }
    }
    if (submodel.isSetVolumeUnits() || !submodel.getVolumeUnits().equals(modelOrig.getVolumeUnits())) {
      if (!modelOrig.isSetVolumeUnits()) {
        modelOrig.setVolumeUnits(modelOrig.getVolumeUnits());
      }
    }
    
    for (Compartment c : submodel.getListOfCompartments()) {
      Compartment compOrig = modelOrig.getCompartment(c.getId());
      // if level > 1, set spatialDimension
      // the property spatialDimension is available since l2v1
      if (submodel.getLevel() > 1) {
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
        progressAdapter.setNumberOfTags(modelOrig, submodel, removeUnnecessaryParameters);
        progressAdapter.progressOn();
      }
    }
    
    for (Species spec : submodel.getListOfSpecies()) {
      Species specOrig = modelOrig.getSpecies(spec.getId());
      // if level > 1, set hasOnlySubstanceUnits
      // the hasOnlySubstanceUnits property is available since l2v1
      if (submodel.getLevel() > 1) {
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
        progressAdapter.setNumberOfTags(modelOrig, submodel, removeUnnecessaryParameters);
        progressAdapter.progressOn();
      }
    }
  }
  
  /**
   * 
   * @param submodel
   */
  private void storeParameters(Model submodel) {
    if (submodel.isSetListOfParameters()) {
      for (Parameter parameter : submodel.getListOfParameters()) {
        if (modelOrig.getParameter(parameter.getId()) == null) {
          Parameter p = parameter.clone();
          modelOrig.addParameter(p);
          logger.finer("Cloned parameter " + p);
          updateUnitReferences(modelOrig.getParameter(parameter.getId()));
        }
      }
    }
  }
  
  /**
   * 
   * @param submodel
   */
  private void storeFunctionDefinitions(Model submodel) {
    if (submodel.isSetListOfFunctionDefinitions()) {
      for (FunctionDefinition fd: submodel.getListOfFunctionDefinitions()) {
        if (modelOrig.getFunctionDefinition(fd.getId()) == null) {
          modelOrig.addFunctionDefinition(fd.clone());
        }
      }
    }
  }
  
  /**
   * This method stores all newly created parameters in the {@link Model} and sets the
   * references to the units appropriately. {@link UnitDefinition}s that are not
   * referenced by any object are removed from the {@link Model}.
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
    if (!kineticLaw.isSetMath()) {
      // TODO: Localize!
      logger.log(Level.SEVERE, MessageFormat.format("No math element defined for reaction {0}", reaction.getId()));
    } else {
      kineticLaw.getMath().updateVariables();
    }
    if (progressAdapter != null) {
      progressAdapter.setNumberOfTags(modelOrig, submodel, removeUnnecessaryParameters);
      progressAdapter.progressOn();
    }
  }
  
  /**
   * Moves the pointers from the units in the submodel to the units in the
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
          ud = submodel.getUnitDefinition(p.getUnits());
          UnitDefinition udClone = ud.clone();
          modelOrig.addUnitDefinition(udClone);
        }
        p.setUnits(ud);
      }
    }
  }
  
  /**
   * 
   * @param kineticLaw
   * @param removeParametersAndStoreUnits
   * @return
   */
  public Reaction storeKineticLaw(KineticLaw kineticLaw,
    boolean removeParametersAndStoreUnits) {
    Reaction submodelReaction = kineticLaw.getParentSBMLObject();
    Reaction reaction = modelOrig.getReaction(submodelReaction.getId());
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
      storeParameters(submodel);
      storeFunctionDefinitions(submodel);
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
    if (model.isSetListOfCompartments()) {
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
    }
    // Check species
    if (model.isSetListOfSpecies()) {
      for (Species species : model.getListOfSpecies()) {
        if (species.isSetSubstanceUnits()) {
          neededUnits.add(species.getSubstanceUnits());
        } else if ((level > 2) && model.isSetSubstanceUnits()) {
          neededUnits.add(model.getSubstanceUnits());
        }
      }
    }
    // Check parameters
    if (model.isSetListOfParameters()) {
      for (Parameter parameter : model.getListOfParameters()) {
        if (parameter.isSetUnits()) {
          neededUnits.add(parameter.getUnits());
        }
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
    if (model.isSetListOfReactions()) {
      for (Reaction reaction : model.getListOfReactions()) {
        if (reaction.isSetKineticLaw() && reaction.getKineticLaw().isSetListOfLocalParameters()) {
          for (LocalParameter localParameter : reaction.getKineticLaw().getListOfLocalParameters()) {
            if (localParameter.isSetUnits()) {
              neededUnits.add(localParameter.getUnits());
            }
          }
        }
      }
    }
    if (model.isSetListOfUnitDefinitions()) {
      for (i = model.getUnitDefinitionCount() - 1; i >= 0; i--) {
        UnitDefinition udef = model.getUnitDefinition(i);
        if (!Unit.isPredefined(udef) && !neededUnits.contains(udef.getId())) {
          model.removeUnitDefinition(udef);
        }
        if (progressAdapter != null) {
          progressAdapter.progressOn();
        }
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
   * @param model
   */
  @SuppressWarnings("deprecation")
  private void removeUnnecessaryParameters(Model model) {
    boolean isNeeded;
    int i, j, k = 0;
    Parameter p;
    
    Set<String> pKeys = new HashSet<String>();
    
    if ((submodel != null) && submodel.isSetListOfParameters()) {
      /* Idea:
       * Parameters in the subModel, which are not contained in the main
       * model have just been created and are therefore needed. This should
       * significantly speed up the computation.
       */
      for (Parameter param : submodel.getListOfParameters()) {
        if (!modelOrig.containsParameter(param.getId())) {
          pKeys.add(param.getId());
        }
      }
    }
    
    // remove unnecessary global parameters
    for (i = model.getParameterCount() - 1; i >= 0; i--) {
      p = model.getParameter(i);
      isNeeded = pKeys.contains(p.getId());
      if (isNeeded) {
        continue;
      }
      
      /*
       * Is this parameter necessary for some kinetic law or is this
       * parameter a conversion factor in some stoichiometric math?
       */
      for (j = 0; !isNeeded && (j < model.getReactionCount()); j++) {
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
      for (j = 0; !isNeeded && (j < model.getRuleCount()); j++) {
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
      for (j = 0; !isNeeded && (j < model.getEventCount()); j++) {
        Event e = model.getEvent(j);
        if (e.isSetTrigger() && e.getTrigger().isSetMath()
            && e.getTrigger().getMath().refersTo(p.getId())) {
          isNeeded = true;
        }
        if (e.isSetDelay() && e.getDelay().isSetMath()
            && e.getDelay().getMath().refersTo(p.getId())) {
          isNeeded = true;
        }
        for (k = 0; !isNeeded && (k < model.getEvent(j).getEventAssignmentCount()); k++) {
          EventAssignment ea = e.getEventAssignment(k);
          if ((ea.isSetVariable() && ea.getVariable().equals(p.getId()))
              || ea.isSetMath() && ea.getMath().refersTo(p.getId())) {
            isNeeded = true;
          }
        }
      }
      
      // is this parameter necessary for some function?
      for (j = 0; !isNeeded && (j < model.getFunctionDefinitionCount()); j++) {
        FunctionDefinition fd = model.getFunctionDefinition(j);
        if (fd.isSetMath() && fd.getMath().refersTo(p.getId())) {
          isNeeded = true;
        }
      }
      
      // is this parameter necessary for some initial assignment?
      for (j = 0; !isNeeded && (j < model.getInitialAssignmentCount()); j++) {
        InitialAssignment ia = model.getInitialAssignment(j);
        if ((ia.isSetVariable() && ia.getVariable().equals(p.getId()))
            || ia.isSetMath() && ia.getMath().refersTo(p.getId())) {
          isNeeded = true;
        }
      }
      
      // is this parameter necessary for some constraint?
      for (j = 0; !isNeeded && (j < model.getConstraintCount()); j++) {
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
   * @param listOf
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
   * @param compartment
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
    
    
    //		volumeUD = miniModel.getVolumeUnitsInstance();
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
            org.sbml.jsbml.util.SBMLtools.setLevelAndVersion(ud, modelToWrite.getLevel(), modelToWrite.getVersion());
            modelToWrite.setLengthUnits(ud);
          }
          compartment.setUnits(ud);
          break;
        case 2:
          ud = model.getAreaUnitsInstance();
          if (ud == null) {
            ud = UnitDefinition.getPredefinedUnit(UnitDefinition.AREA, 2, 4);
            org.sbml.jsbml.util.SBMLtools.setLevelAndVersion(ud, modelToWrite.getLevel(), modelToWrite.getVersion());
            modelToWrite.setAreaUnits(ud);
          }
          compartment.setUnits(ud);
          break;
        case 3:
          ud = model.getVolumeUnitsInstance();
          if (ud == null) {
            ud = UnitDefinition.getPredefinedUnit(UnitDefinition.VOLUME, 2, 4);
            org.sbml.jsbml.util.SBMLtools.setLevelAndVersion(ud, modelToWrite.getLevel(), modelToWrite.getVersion());
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
      compartment.setUnits(model.getVolumeUnitsInstance());
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
        org.sbml.jsbml.util.SBMLtools.setLevelAndVersion(ud, miniModel.getLevel(), miniModel.getVersion());
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
    return removeUnnecessaryParameters;
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
  
  /**
   * @return The original {@link SBMLDocument}, i.e., the one that encapsulates
   *         the full model.
   */
  public SBMLDocument getSBMLDocument() {
    return modelOrig.getSBMLDocument();
  }
  
  /**
   * 
   * @param removeUnnecessaryParameters
   */
  public void storeKineticLaws(boolean removeUnnecessaryParameters) {
    
    if (progressBar != null) {
      progressAdapter = new ProgressAdapter(progressBar, TypeOfProgress.storeKineticLaws);
      progressAdapter.setNumberOfTags(modelOrig, submodel, removeUnnecessaryParameters);
    }
    storeUnits();
    storeParameters(submodel);
    storeFunctionDefinitions(submodel);
    
    for (int i = 0; i < submodel.getReactionCount(); i++) {
      Reaction r = submodel.getReaction(i);
      if (r.isSetKineticLaw()) {
        // This check is important in case that for some reason no law could be generated for a certain reaction.
        storeKineticLaw(r.getKineticLaw(), false);
      }
      if (progressAdapter != null) {
        progressAdapter.progressOn();
      }
    }
    
    if (removeUnnecessaryParameters) {
      removeUnnecessaryParameters(modelOrig);
      removeUnnecessaryUnits(modelOrig);
    }
    
    if (progressBar != null) {
      progressAdapter.finished();
    }
    
  }
  
  public void setGenerateLawsForAllReactions(
    boolean generateLawsForAllReactions) {
    this.generateLawsForAllReactions = generateLawsForAllReactions;
  }
  
  /**
   * 
   * @param removeUnnecessaryParameters
   */
  public void setRemoveUnnecessaryParameters(
    boolean removeUnnecessaryParameters) {
    this.removeUnnecessaryParameters = removeUnnecessaryParameters;
  }
  
  /**
   * 
   * @param defaultHasOnlySubstanceUnits
   */
  public void setDefaultHasOnlySubstanceUnits(
    boolean defaultHasOnlySubstanceUnits) {
    this.defaultHasOnlySubstanceUnits = defaultHasOnlySubstanceUnits;
  }
  
  /**
   * 
   * @param setBoundaryCondition
   */
  public void setBoundaryCondition(boolean setBoundaryCondition) {
    this.setBoundaryCondition = setBoundaryCondition;
  }
  
  /**
   * 
   * @param defaultSpeciesInitVal
   */
  public void setDefaultSpeciesInitVal(double defaultSpeciesInitVal) {
    this.defaultSpeciesInitVal = defaultSpeciesInitVal;
  }
  
  /**
   * 
   * @param defaultCompartmentInitSize
   */
  public void setDefaultCompartmentInitSize(double defaultCompartmentInitSize) {
    this.defaultCompartmentInitSize = defaultCompartmentInitSize;
  }
  
  /**
   * 
   * @return
   */
  public Model createSubModel() {
    return createSubmodel(null);
  }
  
  /* (non-Javadoc)
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
    builder.append("SubmodelController [generateLawsForAllReactions=");
    builder.append(generateLawsForAllReactions);
    builder.append(", removeUnnecessaryParameters=");
    builder.append(removeUnnecessaryParameters);
    builder.append(", defaultHasOnlySubstanceUnits=");
    builder.append(defaultHasOnlySubstanceUnits);
    builder.append(", addParametersGlobally=");
    builder.append(addParametersGlobally);
    builder.append(", reversibility=");
    builder.append(reversibility);
    builder.append(", setBoundaryCondition=");
    builder.append(setBoundaryCondition);
    builder.append(", defaultSpeciesInitVal=");
    builder.append(defaultSpeciesInitVal);
    builder.append(", defaultCompartmentInitSize=");
    builder.append(defaultCompartmentInitSize);
    builder.append(", ");
    if (modelOrig != null) {
      builder.append("modelOrig=");
      builder.append(modelOrig);
      builder.append(", ");
    }
    if (submodel != null) {
      builder.append("submodel=");
      builder.append(submodel);
      builder.append(", ");
    }
    if (progressAdapter != null) {
      builder.append("progressAdapter=");
      builder.append(progressAdapter);
      builder.append(", ");
    }
    if (progressBar != null) {
      builder.append("progressBar=");
      builder.append(progressBar);
      builder.append(", ");
    }
    builder.append("defaultSpatialDimensions=");
    builder.append(defaultSpatialDimensions);
    builder.append(']');
    return builder.toString();
  }
  
}
