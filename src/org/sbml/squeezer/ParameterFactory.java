/*
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
import java.util.ResourceBundle;

import org.sbml.jsbml.ASTNode;
import org.sbml.jsbml.Compartment;
import org.sbml.jsbml.KineticLaw;
import org.sbml.jsbml.ListOf;
import org.sbml.jsbml.LocalParameter;
import org.sbml.jsbml.Model;
import org.sbml.jsbml.ModifierSpeciesReference;
import org.sbml.jsbml.Parameter;
import org.sbml.jsbml.Reaction;
import org.sbml.jsbml.SBO;
import org.sbml.jsbml.SimpleSpeciesReference;
import org.sbml.jsbml.Species;
import org.sbml.jsbml.SpeciesReference;
import org.sbml.jsbml.Unit;
import org.sbml.jsbml.UnitDefinition;
import org.sbml.jsbml.util.StringTools;
import org.sbml.jsbml.util.filters.SBOFilter;
import org.sbml.squeezer.util.Bundles;

import de.zbit.sbml.util.SBMLtools;
import de.zbit.util.ResourceManager;

/**
 * This class has many factory methods for the creation of parameters to be used
 * in varous kinetic equations for a specific model.
 * 
 * @author Andreas Dr&auml;ger
 * @author Sarah R. M&uuml;ller vom Hagen
 * @date 2010-10-22
 * @version $Rev$
 */
public class ParameterFactory {
  
  /**
   * 
   */
  public static final transient ResourceBundle MESSAGES = ResourceManager.getBundle(Bundles.MESSAGES);
  
  /**
   * 
   */
  private double defaultParamValue;
  /**
   * 
   */
  private KineticLaw kineticLaw;
  /**
   * 
   */
  private Model model;
  /**
   * 
   */
  private double orderProducts;
  /**
   * 
   */
  private double orderReactants;
  /**
   * 
   */
  private UnitFactory unitFactory;
  
  /**
   * 
   * @param kl
   * @param defaultParamValue
   * @param orderReactants
   * @param orderProducts
   * @param bringToConcentration
   */
  public ParameterFactory(KineticLaw kl, double defaultParamValue,
    double orderReactants, double orderProducts,
    boolean bringToConcentration) {
    kineticLaw = kl;
    model = kl.getModel();
    this.defaultParamValue = defaultParamValue;
    this.orderReactants = orderReactants;
    this.orderProducts = orderProducts;
    unitFactory = new UnitFactory(model, bringToConcentration);
  }
  
  /**
   * Concatenates the given name parts of the identifier and returns a global
   * parameter with this id.
   * 
   * @param idParts
   * @return
   */
  public Parameter createOrGetGlobalParameter(Object... idParts) {
    return createOrGetGlobalParameter(StringTools.concat(idParts)
      .toString());
  }
  
  /**
   * If the parent model does not yet contain a parameter with the given id, a
   * new parameter is created, its value is set to 1 and it is added to the
   * list of global parameters in the model. If there is already a parameter
   * with this id, a reference to it will be returned.
   * 
   * @param id
   *            the identifier of the global parameter.
   */
  public Parameter createOrGetGlobalParameter(String id) {
    Parameter p = model.getParameter(id);
    if (p == null) {
      p = new Parameter(id, model.getLevel(), model.getVersion());
      p.setValue(defaultParamValue);
      if (1 < model.getLevel()) {
        p.setConstant(true);
      }
      model.addParameter(p);
    }
    return p;
  }
  
  /**
   * Equvivalent to {@see createOrGetParameter} but the parts of the id can be
   * given separately to this method and are concatenated.
   * 
   * @param idParts
   * @return
   */
  public LocalParameter createOrGetParameter(Object... idParts) {
    return createOrGetParameter(StringTools.concat(idParts).toString());
  }
  
  /**
   * If a {@link Parameter} with the given identifier has already been created
   * and is contained in the list of {@link LocalParameter}s for this
   * {@link KineticLaw}, a pointer to it will be returned. If no such
   * {@link Parameter} exists, a new {@link Parameter} with the default value
   * will be created and a pointer to it will be returned.
   * 
   * @param id
   *        the identifier of the local parameter.
   * @return
   */
  public LocalParameter createOrGetParameter(String id) {
    LocalParameter p = kineticLaw.getLocalParameter(id);
    if (p == null) {
      p = kineticLaw.createLocalParameter(id);
      p.setValue(defaultParamValue);
    }
    return p;
  }
  
  /**
   * 
   * @return
   */
  public UnitFactory getUnitFactory() {
    return unitFactory;
  }
  
  /**
   * For the additive Model: slope of the curve activation function
   * 
   * @return Parameter
   */
  public LocalParameter parameterAlpha(String rId) {
    LocalParameter p = createOrGetParameter("alpha_", rId);
    if (!p.isSetSBOTerm()) {
      SBMLtools.setSBOTerm(p, 2);
    }
    if (!p.isSetUnits()) {
      p.setUnits(Unit.Kind.DIMENSIONLESS);
    }
    if (!p.isSetName()) {
      p.setName(MESSAGES.getString("FOR_ADDITIVE_MODEL")
        + ": " + MESSAGES.getString("WEIGHT_ACTIVATION_FUNCTION_PARAMETERS"));
    }
    return p;
  }
  
  /**
   * Association constant from mass action kinetics.
   * 
   * 
   * @param catalyst
   *            Identifier of the catalyst. Can be null.
   * @return
   */
  public LocalParameter parameterAssociationConst(String catalyst) {
    boolean zerothOrder = orderReactants == 0d;
    Reaction r = kineticLaw.getParentSBMLObject();
    StringBuilder kass = StringTools.concatStringBuilder("kass_", r.getId());
    if (zerothOrder) {
      kass.insert(0, 'z');
    }
    if (catalyst != null) {
      StringTools.append(kass, StringTools.underscore, catalyst);
    }
    LocalParameter p_kass = createOrGetParameter(kass.toString());
    if (!p_kass.isSetName()) {
      p_kass.setName((zerothOrder ? MESSAGES.getString("ZEROTH_ORDER") + " " : "")
        + MessageFormat.format(MESSAGES.getString("ASSOCIATION_CONSTANT_OF_REACTION"),
          SBMLtools.getName(r)));
    }
    if (!p_kass.isSetSBOTerm()) {
      SBMLtools.setSBOTerm(p_kass,zerothOrder ? 48 : 153);
    }
    if (!p_kass.isSetUnits()) {
      UnitDefinition ud = unitFactory.unitPerTimeAndConcentrationOrSubstance(
        r.getListOfReactants(), r.getListOfModifiers(), zerothOrder).clone();
      UnitDefinition substance = model.getSubstanceUnitsInstance();
      ud.multiplyWith(substance.clone());
      p_kass.setUnits(UnitFactory.checkUnitDefinitions(ud, model));
    }
    return p_kass;
  }
  
  /**
   * For the additive Model: weight parameter
   * 
   * @return weight for the weight matrix
   */
  public LocalParameter parameterB(String rId) {
    LocalParameter p = createOrGetParameter("b_", rId);
    if (!p.isSetSBOTerm()) {
      SBMLtools.setSBOTerm(p, 2);
    }
    if (!p.isSetUnits()) {
      p.setUnits(Unit.Kind.DIMENSIONLESS);
    }
    if (!p.isSetName()) {
      p.setName(MESSAGES.getString("FOR_ADDITIVE_MODEL")
        + ": " + MESSAGES.getString("BASIS_EXPRESSION_LEVEL"));
    }
    return p;
  }
  
  /**
   * For the HillRadde Model: weight parameter
   * 
   * @return weight for the weight matrix
   */
  public LocalParameter parameterBH(String rId) {
    LocalParameter p = createOrGetParameter("b_", rId);
    if (!p.isSetSBOTerm()) {
      SBMLtools.setSBOTerm(p, 2);
    }
    if (!p.isSetUnits()) {
      p.setUnits(unitFactory.unitSubstancePerTime(model.getUnitDefinition(UnitDefinition.SUBSTANCE),
        model.getUnitDefinition(UnitDefinition.TIME)));
    }
    if (!p.isSetName()) {
      p.setName(MESSAGES.getString("FOR_ADDITIVE_MODEL")
        + ": " + MESSAGES.getString("BASIS_EXPRESSION_LEVEL"));
    }
    return p;
  }
  
  /**
   * For the additive Model: activation curve's y-intercept
   * 
   * @return Parameter
   */
  public LocalParameter parameterBeta(String rId) {
    LocalParameter p = createOrGetParameter("beta_", rId);
    if (!p.isSetSBOTerm()) {
      SBMLtools.setSBOTerm(p, 2);
    }
    if (!p.isSetUnits()) {
      p.setUnits(Unit.Kind.DIMENSIONLESS);
    }
    if (!p.isSetName()) {
      p.setName(MESSAGES.getString("WEIGHT_ACTIVATION_FUNCTION_PARAMETERS"));
    }
    return p;
  }
  
  /**
   * biochemical cooperative inhibitor substrate coefficient.
   * 
   * @param aORb
   *            a character to specify this parameter. Allowed values are 'a'
   *            or 'b'.
   * @param inhibitor
   *            Identifier of the inhibitory species. Can be null.
   * @param enzyme
   *            Identifier of the catralyzing enzyme. Can be null.
   * @return
   */
  public LocalParameter parameterCooperativeInhibitorSubstrateCoefficient(
    char aORb, String inhibitor, String enzyme) {
    StringBuffer id = new StringBuffer();
    id.append(aORb);
    if (inhibitor != null) {
      StringTools.append(id, StringTools.underscore, inhibitor);
    }
    if (enzyme != null) {
      StringTools.append(id, StringTools.underscore, enzyme);
    }
    LocalParameter coeff = createOrGetParameter(id);
    if (!coeff.isSetSBOTerm()) {
      SBMLtools.setSBOTerm(coeff, 385);
    }
    if (!coeff.isSetUnits()) {
      coeff.setUnits(Unit.Kind.DIMENSIONLESS);
    }
    return coeff;
  }
  
  /**
   * Dissociation constant in mass action kinetics
   * 
   * @param catalyst
   *            Identifier of the catalyzing species. Can be null.
   * @return
   */
  public LocalParameter parameterDissociationConst(String catalyst) {
    boolean zerothOrder = orderProducts == 0d;
    Reaction r = kineticLaw.getParentSBMLObject();
    StringBuilder kdiss = StringTools.concatStringBuilder("kdiss_", r.getId());
    if (zerothOrder) {
      kdiss.insert(0, 'z');
    }
    if (catalyst != null) {
      StringTools.append(kdiss, StringTools.underscore, catalyst);
    }
    LocalParameter p_kdiss = createOrGetParameter(kdiss.toString());
    if (!p_kdiss.isSetName()) {
      p_kdiss.setName((zerothOrder ? MESSAGES.getString("ZEROTH_ORDER") + " " : "") +
        MessageFormat.format(MESSAGES.getString("DISASSOCIATION_CONSTANT_OF_REACTION"), SBMLtools.getName(r)));
    }
    if (!p_kdiss.isSetSBOTerm()) {
      SBMLtools.setSBOTerm(p_kdiss, 156);
    }
    if (!p_kdiss.isSetUnits()) {
      UnitDefinition ud = unitFactory.unitPerTimeAndConcentrationOrSubstance(
        r.getListOfProducts(), r.getListOfModifiers(), zerothOrder).clone();
      ud.multiplyWith(model.getSubstanceUnitsInstance().clone());
      p_kdiss.setUnits(UnitFactory.checkUnitDefinitions(ud, model));
    }
    return p_kdiss;
  }
  
  /**
   * Equilibrium constant of the parent reaction.
   * 
   * @param r
   * @return A new or an existing equilibrium constant for the reaction.
   */
  public LocalParameter parameterEquilibriumConstant(Reaction r) {
    String reactionID = r.getId();
    LocalParameter keq = createOrGetParameter("keq_", reactionID);
    if (!keq.isSetSBOTerm()) {
      SBMLtools.setSBOTerm(keq, 281);
    }
    if (!keq.isSetName()) {
      keq.setName(MessageFormat.format(MESSAGES.getString("EQUILIBRIUM_CONSTANT_OF_REACTION"), SBMLtools.getName(r)));
    }
    if (!keq.isSetUnits()) {
      UnitDefinition ud = null;
      double stoichDiff = 0d;
      if (r.isSetListOfReactants()) {
        for (SpeciesReference specRef : r.getListOfReactants()) {
          Species spec = specRef.getSpeciesInstance();
          if (spec != null) {
            UnitDefinition specUd = spec.getDerivedUnitDefinition();
            if (specUd != null) {
              specUd = specUd.clone();
              if (specRef.isSetStoichiometry() || ((specRef.getLevel() < 3) && !specRef.isSetStoichiometryMath())) {
                stoichDiff -= specRef.getStoichiometry();
              }
              Compartment compartment = spec.getCompartmentInstance();
              if (!spec.hasOnlySubstanceUnits() && !unitFactory.getBringToConcentration() && (compartment != null)) {
                specUd.multiplyWith(compartment.getDerivedUnitDefinition());
              }
              if (ud == null) {
                ud = specUd;
              } else {
                ud.multiplyWith(specUd);
              }
            }
          }
        }
      }
      if (r.getReversible() && r.isSetListOfProducts()) {
        for (SpeciesReference specRef : r.getListOfProducts()) {
          Species spec = specRef.getSpeciesInstance();
          if (spec != null) {
            UnitDefinition specUd = spec.getDerivedUnitDefinition();
            if (specUd != null) {
              specUd = specUd.clone();
              if (specRef.isSetStoichiometry() || ((specRef.getLevel() < 3) && !specRef.isSetStoichiometryMath())) {
                stoichDiff += specRef.getStoichiometry();
              }
              Compartment compartment = spec.getCompartmentInstance();
              if (!spec.hasOnlySubstanceUnits() && !unitFactory.getBringToConcentration() && (compartment != null)) {
                specUd.multiplyWith(compartment.getDerivedUnitDefinition());
              }
              if (ud == null) {
                ud = specUd;
              } else {
                ud.divideBy(specUd);
              }
            }
          }
        }
      }
      ud.raiseByThePowerOf(stoichDiff);
      ud = ud.simplify();
      if ((ud.getUnitCount() == 1) && (ud.getUnit(0).isDimensionless())) {
        keq.setUnits(Unit.Kind.DIMENSIONLESS);
      } else {
        ud = UnitFactory.checkUnitDefinitions(ud, model);
        keq.setUnits(ud);
      }
    }
    return keq;
  }
  
  /**
   * 
   * @param r
   * @return
   */
  public ASTNode parameterCStandard(KineticLaw kl) {
    ASTNode cStandard = new ASTNode(1, kl);
    if ((kl.getLevel() > 2) && !cStandard.isSetUnits()) {
      UnitDefinition ud = model.getSubstanceUnitsInstance().clone();
      ud.raiseByThePowerOf(.5d);
      ud = ud.simplify();
      if ((ud.getUnitCount() == 1) && (ud.getUnit(0).isDimensionless())) {
        cStandard.setUnits(Unit.Kind.DIMENSIONLESS);
      } else {
        ud = UnitFactory.checkUnitDefinitions(ud, model);
        cStandard.setUnits(ud);
      }
    }
    return cStandard;
  }
  
  /**
   * Bolzman's gas constant.
   * 
   * @return A new or the existing gas constant parameter from the model
   *         (global parameter).
   */
  public Parameter parameterGasConstant() {
    Parameter R = createOrGetGlobalParameter("R");
    R.setValue(8.31447215);
    if (!R.isSetName()) {
      R.setName(MESSAGES.getString("IDEAL_GAS_CONSTANT"));
    }
    if (!R.isSetUnits()) {
      R.setUnits(unitFactory.unitJperKandM());
    }
    return R;
  }
  
  /**
   * Hill coefficient.
   * 
   * @param enzyme
   *            Identifier of the catalyzing enzyme. Can be null.
   * @return
   */
  public LocalParameter parameterHillCoefficient(String enzyme) {
    String reactionID = kineticLaw.getParentSBMLObject().getId();
    StringBuffer id = StringTools.concat("hic_", reactionID);
    if (enzyme != null) {
      StringTools.append(id, StringTools.underscore, enzyme);
    }
    LocalParameter hr = createOrGetParameter(id.toString());
    if (!hr.isSetSBOTerm()) {
      SBMLtools.setSBOTerm(hr, 190);
    }
    if (!hr.isSetName()) {
      if (enzyme != null) {
        hr.setName(MessageFormat.format(
          MESSAGES.getString("HILL_COEFFICIENT_IN_REACTION"),
          MessageFormat.format(MESSAGES.getString("FOR_ENZYME"),
            SBMLtools.getName(model.getSpecies(enzyme)))));
      } else {
        hr.setName(MessageFormat.format(MESSAGES.getString("HILL_COEFFICIENT_IN_REACTION"), ""));
      }
    }
    if (!hr.isSetUnits()) {
      hr.setUnits(Unit.Kind.DIMENSIONLESS);
    }
    return hr;
  }
  
  /**
   * Activation constant.
   * 
   * @param activatorSpecies
   *            Identifier of the activatory species. Must not be null.
   * @return
   */
  public LocalParameter parameterKa(String activatorSpecies) {
    return parameterKa(activatorSpecies, null);
  }
  
  /**
   * Activation constant.
   * 
   * @param activatorSpecies
   *            Identifier of the activatory species. Must not be null.
   * @param enzyme
   * @return
   */
  public LocalParameter parameterKa(String activatorSpecies, String enzyme) {
    Reaction r = kineticLaw.getParent();
    String reactionID = r.getId();
    StringBuffer name = StringTools.concat("kac_", reactionID,
      StringTools.underscore, activatorSpecies);
    if (enzyme != null) {
      StringTools.append(name, StringTools.underscore, enzyme);
    }
    LocalParameter kA = createOrGetParameter(name);
    if (!kA.isSetSBOTerm()) {
      SBMLtools.setSBOTerm(kA, 363);
    }
    if (!kA.isSetName()) {
      kA.setName(MessageFormat.format(
        MESSAGES.getString("ACTIVATION_CONSTANT_OF_REACTION"),
        SBMLtools.getName(r)));
    }
    if (!kA.isSetUnits()) {
      Species species = model.getSpecies(activatorSpecies);
      kA.setUnits(unitFactory.unitSubstancePerSizeOrSubstance(species));
    }
    return kA;
  }
  
  /**
   * Turn over rate if enzyme is null and limiting rate otherwise.
   * 
   * @param enzyme
   * @param forward
   * @return The parameter vmax if and only if the number of enzymes is zero,
   *         otherwise kcat.
   */
  public LocalParameter parameterKcatOrVmax(String enzyme, boolean forward) {
    Reaction r = kineticLaw.getParent();
    String reactionID = r.getId();
    LocalParameter kr;
    if (enzyme != null) {
      /*
       * Catalytic constant
       */
      StringBuffer id = new StringBuffer();
      if (kineticLaw.getParentSBMLObject().getReversible()) {
        id.append("kcr");
        StringTools.append(id, forward ? 'f' : 'r',
            StringTools.underscore);
      } else {
        id.append("kcat_");
      }
      StringTools.append(id, reactionID, StringTools.underscore, enzyme);
      kr = createOrGetParameter(id);
      if (!kr.isSetSBOTerm()) {
        if (kineticLaw.getParentSBMLObject().getReversible()) {
          SBMLtools.setSBOTerm(kr,forward ? 320 : 321);
        } else {
          SBMLtools.setSBOTerm(kr, 25);
        }
      }
      if (!kr.isSetName()) {
        // Note that the enzyme must not be null here (see the check above)
        kr.setName(MessageFormat.format(
          MESSAGES.getString("CATALYTIC_RATE_CONSTANT_OF_ENZYME_IN_REACTION"),
          MESSAGES.getString(forward ? "SUBSTRATE" : "PRODUCT"),
          SBMLtools.getName(model.getSpecies(enzyme)), SBMLtools.getName(r)));
      }
      if (!kr.isSetUnits()) {
        kr.setUnits(unitFactory.unitPerTimeOrSizePerTime(model
          .getSpecies(enzyme).getCompartmentInstance()));
      }
    } else {
      /*
       * Maximal velocity, i.e., limiting rate.
       */
      StringBuffer id = StringTools.concat("vma");
      if (!kineticLaw.getParentSBMLObject().getReversible()) {
        id.append('x');
      } else {
        id.append(forward ? 'f' : 'r');
      }
      StringTools.append(id, StringTools.underscore, reactionID);
      kr = createOrGetParameter(id);
      if (!kr.isSetSBOTerm()) {
        if (kineticLaw.getParentSBMLObject().getReversible()) {
          SBMLtools.setSBOTerm(kr,forward ? 324 : 325);
        } else {
          SBMLtools.setSBOTerm(kr, 186);
        }
      }
      if (!kr.isSetName()) {
        kr.setName(MessageFormat.format(MESSAGES
          .getString("MAXIMAL_VELOCITY_OF_REACTION"), MESSAGES
          .getString(forward ? "FORWARD" : "REVERSE"),
          SBMLtools.getName(r)));
      }
      if (!kr.isSetUnits()) {
        /*
         * bringToConcentration ? unitmMperSecond() :
         */
        kr.setUnits(unitFactory.unitSubstancePerTime(
          model.getUnitDefinition(UnitDefinition.SUBSTANCE),
          model.getUnitDefinition(UnitDefinition.TIME)));
      }
    }
    return kr;
  }
  
  /**
   * energy constant of given species
   * 
   * @param species
   *            Identifier of the species for which this global parameter
   *            should be created. Must not be null.
   * @return A new or an existing global parameter representing the KG
   *         parameter for the given species.
   */
  public Parameter parameterKG(String species) {
    Parameter kG = createOrGetGlobalParameter("kG_", species);
    if (!kG.isSetUnits()) {
      kG.setUnits(Unit.Kind.DIMENSIONLESS);
    }
    if (!kG.isSetName()) {
      kG.setName(MessageFormat.format(
        MESSAGES.getString("ENERGY_CONSTANT_OF_SPECIES"),
        SBMLtools.getName(model.getSpecies(species))));
    }
    return kG;
  }
  
  /**
   * Inhibitory constant
   * 
   * @param inhibitorSpecies
   *            species that lowers this velocity. Must not be null.
   * @return
   */
  public LocalParameter parameterKi(String inhibitorSpecies) {
    return parameterKi(inhibitorSpecies, null);
  }
  
  /**
   * Inhibitory constant in an enzyme catalyzed reaction.
   * 
   * @param inhibitorSpecies
   *            Identifier of an inhibitory species in this reaction. Must not
   *            be null.
   * @param enzyme
   *            Identifier of a catalyzing enzyme in this reaction. Can be
   *            null.
   * @return
   */
  public LocalParameter parameterKi(String inhibitorSpecies, String enzyme) {
    return parameterKi(inhibitorSpecies, enzyme, 0);
  }
  
  /**
   * Inhibitory constant.
   * 
   * @param inhibitorSpecies
   *            Must not be null.
   * @param enzymeID
   *            can be null.
   * @param bindingNum
   *            if <= 0 not considered.
   * @return
   */
  public LocalParameter parameterKi(String inhibitorSpecies, String enzymeID,
    int bindingNum) {
    StringBuffer id = new StringBuffer();
    Species inhibitor = model.getSpecies(inhibitorSpecies);
    id.append("kic_");
    if (bindingNum > 0) {
      StringTools.append(id, bindingNum, StringTools.underscore);
    }
    Reaction r = kineticLaw.getParent();
    String reactionID = r.getId();
    id.append(reactionID);
    if (inhibitorSpecies != null) {
      StringTools.append(id, StringTools.underscore, inhibitorSpecies);
    }
    if (enzymeID != null) {
      StringTools.append(id, StringTools.underscore, enzymeID);
    }
    LocalParameter kI = createOrGetParameter(id.toString());
    if (!kI.isSetSBOTerm()) {
      SBMLtools.setSBOTerm(kI, 261);
    }
    if (!kI.isSetName()) {
      StringBuilder temp = new StringBuilder();
      if (inhibitor != null) {
        temp.append(MessageFormat.format(MESSAGES.getString("FOR_SPECIES"), SBMLtools.getName(inhibitor)));
      }
      if (enzymeID != null) {
        temp.append(MessageFormat.format(MESSAGES.getString("FOR_ENZYME"), enzymeID));
      }
      if (bindingNum > 0) {
        temp.append(MessageFormat.format(MESSAGES.getString("WITH_BINDING_POSITIONS"), Integer.toString(bindingNum)));
      }
      kI.setName(MessageFormat.format(MESSAGES.getString("INHIBITIORY_CONSTANT_OF_REACTION"), temp, SBMLtools.getName(r)));
    }
    if (!kI.isSetUnits()) {
      kI.setUnits(unitFactory.unitSubstancePerSizeOrSubstance(inhibitor));
    }
    return kI;
  }
  
  /**
   * @param prefix
   * @return
   */
  public LocalParameter parameterKineticOrder(String prefix) {
    Reaction r = kineticLaw.getParent();
    String reactionID = r.getId();
    StringBuffer id = StringTools.concat(prefix, reactionID);
    LocalParameter hr = createOrGetParameter(id.toString());
    if (!hr.isSetSBOTerm()) {
      // not yet available.
    }
    if (!hr.isSetName()) {
      hr.setName(MessageFormat.format(MESSAGES.getString("KINETIC_ORDER_OF_REACTION"), SBMLtools.getName(r)));
    }
    if (!hr.isSetUnits()) {
      hr.setUnits(Unit.Kind.DIMENSIONLESS);
    }
    return hr;
  }
  
  /**
   * Half saturation constant.
   * 
   * @param species
   *            Must not be null.
   * @param enzyme
   *            Identifier of the catalyzing enzyme. Can be null.
   * @return
   */
  public LocalParameter parameterKS(Species species, String enzyme) {
    // TODO: Should we have the substrate or product as an additional argument here to form the name?
    Reaction r = kineticLaw.getParent();
    String rid = r.getId();
    StringBuffer id = StringTools.concat("ksp_", rid);
    if (enzyme != null) {
      StringTools.append(id, StringTools.underscore, enzyme);
    }
    LocalParameter kS = createOrGetParameter(id.toString());
    if (!kS.isSetSBOTerm()) {
      SBMLtools.setSBOTerm(kS, 194);
    }
    if (!kS.isSetName()) {
      if (enzyme != null) {
        kS.setName(MessageFormat.format(MESSAGES.getString("HALF_SATURATION_CONSTANT_OF_SPECIES_AND_ENZYME_IN_REACTION"),
          SBMLtools.getName(species), SBMLtools.getName(model.getSpecies(enzyme)), SBMLtools.getName(r)));
      } else {
        kS.setName(MessageFormat.format(MESSAGES.getString("HALF_SATURATION_CONSTANT_OF_SPECIES_IN_REACTION"),
          SBMLtools.getName(species),  SBMLtools.getName(r)));
      }
    }
    if (!kS.isSetUnits()) {
      if (unitFactory.getBringToConcentration()) {
        kS.setUnits(unitFactory.unitSubstancePerSize(species));
      } else if (species != null) {
        kS.setUnits(species.getSubstanceUnits());
      }
    }
    return kS;
  }
  
  /**
   * For the additive Model: weight parameter
   * 
   * @return weight for the weight matrix
   */
  public LocalParameter parameterM(String rId) {
    LocalParameter p = createOrGetParameter("m_", rId);
    if (!p.isSetSBOTerm()) {
      SBMLtools.setSBOTerm(p, 2);
    }
    if (!p.isSetUnits()) {
      p.setUnits(unitFactory.unitSubstancePerTime(model
        .getUnitDefinition("substance"), model
        .getUnitDefinition("time")));
    }
    if (!p.isSetName()) {
      p.setName(MESSAGES.getString("FOR_ADDITIVE_MODEL")
        + ": " + MESSAGES.getString("CONST_MAX_EXPRESSION"));
    }
    return p;
  }
  
  /**
   * Michaelis constant.
   * 
   * @param species
   *            Must not be null.
   * @param enzyme
   *            Identifier of the catalyzing enzyme. Can be null.
   * @return
   */
  public LocalParameter parameterMichaelis(String species, String enzyme) {
    Reaction r = kineticLaw.getParent();
    String reactionID = r.getId();
    StringBuffer id = StringTools.concat("kmc_", reactionID,
      StringTools.underscore, species);
    if (enzyme != null) {
      StringTools.append(id, StringTools.underscore, enzyme);
    }
    LocalParameter kM = createOrGetParameter(id.toString());
    if (!kM.isSetSBOTerm()) {
      SBMLtools.setSBOTerm(kM, 27);
    }
    if (!kM.isSetName()) {
      if (enzyme != null) {
        kM.setName(MessageFormat.format(MESSAGES.getString("MICHAELIS_CONSTANT_OF_SPECIES_AND_ENZYME_IN_REACTION"),
          SBMLtools.getName(model.getSpecies(species)), SBMLtools.getName(model.getSpecies(enzyme)), SBMLtools.getName(r)));
      } else {
        kM.setName(MessageFormat.format(MESSAGES.getString("MICHAELIS_CONSTANT_OF_SPECIES_IN_REACTION"),
          SBMLtools.getName(model.getSpecies(species)), SBMLtools.getName(r)));
      }
    }
    if (!kM.isSetUnits()) {
      Species spec = model.getSpecies(species);
      kM.setUnits(unitFactory.unitSubstancePerSizeOrSubstance(spec));
    }
    return kM;
  }
  
  /**
   * Michaelis constant. See the SBO terms <a
   * href="http://identifiers.org/biomodels.sbo/SBO:0000322">322</a> and <a
   * href="http://identifiers.org/biomodels.sbo/SBO:0000322">322</a> for
   * details.
   * 
   * @param species
   *        Must not be {@code null}.
   * @param enzyme
   *        identifier of the catalyzing enzyme. Can be {@code null}.
   * @param substrate
   *        If {@code true} it returns the Michaels constant for substrate, else the
   *        Michaelis constant for the product.
   * @return
   */
  public LocalParameter parameterMichaelis(String species, String enzyme,
    boolean substrate) {
    LocalParameter kM = parameterMichaelis(species, enzyme);
    if (!kM.isSetSBOTerm()) {
      SBMLtools.setSBOTerm(kM, substrate ? 322 : 323);
    }
    return kM;
  }
  
  /**
   * Number of binding sites for the inhibitor on the enzyme that catalyses
   * this reaction.
   * 
   * @param enzyme
   *            Identifier of the catalyzing enzyme. Can be null.
   * @param inhibitor
   *            Identifier of the inhibitory species. Can be null.
   * @return
   */
  public LocalParameter parameterNumBindingSites(String enzyme,
    String inhibitor) {
    Reaction r = kineticLaw.getParent();
    String rid = r.getId();
    StringBuffer exponent = StringTools.concat("m_", rid);
    if (enzyme != null) {
      StringTools.append(exponent, StringTools.underscore, enzyme);
    }
    if (inhibitor != null) {
      StringTools.append(exponent, StringTools.underscore, inhibitor);
    }
    LocalParameter p_exp = createOrGetParameter(exponent.toString());
    if (!p_exp.isSetSBOTerm()) {
      SBMLtools.setSBOTerm(p_exp, 189);
    }
    if (!p_exp.isSetName()) {
      if (enzyme != null) {
        if (inhibitor != null) {
          p_exp.setName(MessageFormat.format(MESSAGES.getString("NUMBER_OF_BINDING_SITES_FOR_INHIBITOR_ON_ENZYME_OF_REACTION"),
            SBMLtools.getName(model.getSpecies(inhibitor)), SBMLtools.getName(model.getSpecies(enzyme)), SBMLtools.getName(r)));
        } else {
          p_exp.setName(MessageFormat.format(MESSAGES.getString("NUMBER_OF_BINDING_SITES_FOR_INHIBITOR_ON_ENZYME_OF_REACTION"),
            "", SBMLtools.getName(model.getSpecies(enzyme)), SBMLtools.getName(r)));
        }
      } else if (inhibitor != null) {
        p_exp.setName(MessageFormat.format(MESSAGES.getString("NUMBER_OF_BINDING_SITES_FOR_INHIBITOR_OF_REACTION"),
          SBMLtools.getName(model.getSpecies(inhibitor)), SBMLtools.getName(r)));
      } else {
        p_exp.setName(MessageFormat.format(MESSAGES.getString("NUMBER_OF_BINDING_SITES_FOR_INHIBITOR_OF_REACTION"),
          "", SBMLtools.getName(r)));
      }
    }
    if (!p_exp.isSetUnits()) {
      p_exp.setUnits(Unit.Kind.DIMENSIONLESS);
    }
    return p_exp;
  }
  
  /**
   * Biochemical exponential coefficient
   * 
   * @param enzyme
   *            Identifier of the catalyzing enzyme. Can be null.
   * @return
   */
  public LocalParameter parameterReactionCooperativity(String enzyme) {
    String reactionID = kineticLaw.getParentSBMLObject().getId();
    StringBuffer id = StringTools.concat("hco_", reactionID);
    if (enzyme != null) {
      StringTools.append(id, StringTools.underscore, enzyme);
    }
    LocalParameter hr = createOrGetParameter(id.toString());
    if (!hr.isSetSBOTerm()) {
      SBMLtools.setSBOTerm(hr, 382);
    }
    if (!hr.isSetName()) {
      hr.setName(MESSAGES.getString("REACTION_COOPERATIVITY"));
    }
    if (!hr.isSetUnits()) {
      hr.setUnits(Unit.Kind.DIMENSIONLESS);
    }
    return hr;
  }
  
  /**
   * Rho activator according to Liebermeister et al.
   * 
   * @param species
   *            Identifier of the species for which this parameter is to be
   *            created. Must not be null.
   * @return
   */
  public LocalParameter parameterRhoActivation(String species) {
    Reaction r = kineticLaw.getParent();
    String rid = r.getId();
    LocalParameter rhoA = createOrGetParameter("rac_", rid,
      StringTools.underscore, species);
    if (!rhoA.isSetName()) {
      rhoA.setName(MessageFormat.format(MESSAGES.getString("ACTIVATION_BASELINE_RELATION_OF_SPECIES_IN_REACTION"),
        SBMLtools.getName(model.getSpecies(species)), SBMLtools.getName(r)));
    }
    if (!rhoA.isSetUnits()) {
      rhoA.setUnits(Unit.Kind.DIMENSIONLESS);
    }
    return rhoA;
  }
  
  /**
   * 
   * @param species
   * @return
   */
  public LocalParameter parameterRhoInhibition(String species) {
    Reaction r = kineticLaw.getParent();
    String rid = r.getId();
    LocalParameter rhoI = createOrGetParameter("ric_", rid,
      StringTools.underscore, species);
    if (!rhoI.isSetUnits()) {
      rhoI.setUnits(Unit.Kind.DIMENSIONLESS);
    }
    if (!rhoI.isSetName()) {
      rhoI.setName(MessageFormat.format(MESSAGES.getString("INHIBITON_BASELINE_RELATION_OF_SPECIES_IN_REACTION"),
        SBMLtools.getName(model.getSpecies(species)), SBMLtools.getName(r)));
    }
    return rhoI;
  }
  
  /**
   * Parameter for reaction kinetics in restricted spaces.
   * 
   * @param h
   * @return
   */
  public LocalParameter parameterSpaceRestrictedAssociationConst(double h) {
    Reaction r = kineticLaw.getParentSBMLObject();
    StringBuffer kass = StringTools.concat("kar_", r.getId());
    LocalParameter p_kass = createOrGetParameter(kass.toString());
    if (!p_kass.isSetName()) {
      p_kass.setName(MessageFormat.format(MESSAGES.getString("ASSOCIATION_CONSTANT_IN_RESTRICTED_SPACES_OF_REACTION"),
        SBMLtools.getName(r)));
    }
    if (!p_kass.isSetSBOTerm()) {
      // not yet available.
    }
    if (!p_kass.isSetUnits()) {
      UnitDefinition ud = unitFactory
          .unitPerTimeAndConcentrationOrSubstance(r
            .getListOfReactants(), h, defaultParamValue,
            defaultParamValue);
      ListOf<? extends SimpleSpeciesReference> l = (ListOf<? extends SimpleSpeciesReference>)
          r.getListOfModifiers().filterList(new SBOFilter(SBO.getCatalyst(), SBO.getCatalysis()));
      for (SimpleSpeciesReference ssr : l) {
        Species s = ssr.getSpeciesInstance();
        if (unitFactory.getBringToConcentration()
            && s.hasOnlySubstanceUnits()) {
          ud.multiplyWith(s.getCompartmentInstance()
            .getUnitsInstance());
        } else if (!unitFactory.getBringToConcentration()
            && !s.hasOnlySubstanceUnits()) {
          ud.divideBy(s.getCompartmentInstance().getUnitsInstance());
        }
        ud.divideBy(s.getUnitsInstance());
      }
      p_kass.setUnits(UnitFactory.checkUnitDefinitions(ud, model));
    }
    return p_kass;
  }
  
  /**
   * 
   * @param rId
   * @return
   */
  @SuppressWarnings("deprecation")
  public LocalParameter parameterSSystemAlpha(String rId) {
    LocalParameter alpha = createOrGetParameter("alpha_", rId);
    if (!alpha.isSetSBOTerm()) {
      SBMLtools.setSBOTerm(alpha, 153);
    }
    if (!alpha.isSetUnits()) {
      Reaction reaction = model.getReaction(rId);
      UnitDefinition ud = null;
      if ((reaction.getReactantCount() > 0) && !ReactionType.representsEmptySet(reaction.getListOfReactants())) {
        ud = unitFactory.unitPerTimeAndConcentrationOrSubstance(model.getReaction(rId).getListOfReactants(), false);
      } else {
        ud = unitFactory.unitPerTime();
      }
      if (unitFactory.getBringToConcentration() && reaction.isSetListOfModifiers()) {
        for (ModifierSpeciesReference modSpecRef : reaction.getListOfModifiers()) {
          ud.multiplyWith(modSpecRef.getSpeciesInstance().getSpatialSizeUnitsInstance());
        }
      }
      alpha.setUnits(ud);
    }
    if (!alpha.isSetName()) {
      alpha.setName(MESSAGES.getString("RATE_CONSTANT_FOR_SYNTHESIS"));
    }
    return alpha;
  }
  
  /**
   * 
   * @param rId
   * @return
   */
  @SuppressWarnings("deprecation")
  public LocalParameter parameterSSystemBeta(String rId) {
    LocalParameter beta = createOrGetParameter("beta_", rId);
    if (!beta.isSetSBOTerm()) {
      SBMLtools.setSBOTerm(beta, 156);
    }
    if (!beta.isSetUnits()) {
      Reaction reaction = model.getReaction(rId);
      UnitDefinition ud = null;
      if ((reaction.getProductCount() > 0) && !ReactionType.representsEmptySet(reaction.getListOfProducts())) {
        ud = unitFactory.unitPerTimeAndConcentrationOrSubstance(model.getReaction(rId).getListOfProducts(), false);
      } else {
        ud = unitFactory.unitPerTime();
      }
      if (unitFactory.getBringToConcentration() && reaction.isSetListOfModifiers()) {
        for (ModifierSpeciesReference modSpecRef : reaction.getListOfModifiers()) {
          ud.multiplyWith(modSpecRef.getSpeciesInstance().getSpatialSizeUnitsInstance());
        }
      }
      beta.setUnits(ud);
    }
    if (!beta.isSetName()) {
      beta.setName(MESSAGES.getString("RATE_CONSTANT_FOR_DEGRADATION"));
    }
    return beta;
  }
  
  /**
   * S-System exponent
   * 
   * @return Parameter
   */
  public LocalParameter parameterSSystemExponent(String modifier) {
    Reaction r = kineticLaw.getParent();
    String reactionID = r.getId();
    StringBuffer id = StringTools.concat("ssexp_", reactionID);
    if (modifier != null) {
      StringTools.append(id, StringTools.underscore, modifier);
    }
    LocalParameter p = createOrGetParameter(id.toString());
    if (!p.isSetName()) {
      if (modifier != null) {
        p.setName(
          MessageFormat.format(MESSAGES.getString("S_SYSTEM_EXPONENT_FOR_MODIFIER_IN_REACTION"),
            SBMLtools.getName(model.getSpecies(modifier)), SBMLtools.getName(r)));
      } else {
        p.setName(MessageFormat.format(MESSAGES.getString("S_SYSTEM_EXPONENT_IN_REACTION"),
          SBMLtools.getName(r)));
      }
    }
    if (!p.isSetUnits()) {
      p.setUnits(Unit.Kind.DIMENSIONLESS);
    }
    return p;
  }
  
  /**
   * standard chemical potential
   * 
   * @param species
   *            Identifier of the reacting species whose potential parameter
   *            is to be created. Must not be null.
   * @return
   */
  public Parameter parameterStandardChemicalPotential(String species) {
    Parameter mu = createOrGetGlobalParameter("scp_", species);
    if (!mu.isSetName()) {
      mu.setName(MessageFormat.format(
        MESSAGES.getString("STANDARD_CHEMICAL_POTENTIAL_OF_SPECIES"),
        SBMLtools.getName(model.getSpecies(species))));
    }
    if (!mu.isSetSBOTerm()) {
      SBMLtools.setSBOTerm(mu, 463);
    }
    if (!mu.isSetUnits()) {
      mu.setUnits(unitFactory.unitkJperSubstance(model.getSpecies(species).getSubstanceUnitsInstance()));
    }
    return mu;
  }
  
  /**
   * Standard Temperature
   * 
   * @return Temperature parameter with standard value of 288.15 K.
   */
  public Parameter parameterTemperature() {
    Parameter T = createOrGetGlobalParameter("T");
    T.setValue(298.15);
    if (!T.isSetSBOTerm()) {
      SBMLtools.setSBOTerm(T, 147);
    }
    if (!T.isSetUnits()) {
      T.setUnits(Unit.Kind.KELVIN);
    }
    if (!T.isSetName()) {
      T.setName(MessageFormat.format(MESSAGES.getString("TEMP_OF_REACTION_SYSTEM"), SBMLtools.getName(model)));
    }
    return T;
  }
  
  /**
   * For the generalized hill function: threshold
   * 
   * @return Parameter
   */
  public LocalParameter parameterTheta(String rId, String name) {
    LocalParameter p = createOrGetParameter("theta_", rId,
      StringTools.underscore, name);
    if (!p.isSetSBOTerm()) {
      SBMLtools.setSBOTerm(p, 2);
    }
    if (!p.isSetValue()) {
      p.setValue(0);
    }
    if (!p.isSetUnits()) {
      if (unitFactory.getBringToConcentration()) {
        p.setUnits(unitFactory.unitSubstancePerSize(model.getSpecies(name)));
      } else {
        p.setUnits(model.getSpecies(name).getSubstanceUnits());
      }
    }
    if (!p.isSetName()) {
      p.setName(MESSAGES.getString("THRESHOLD_GENERALIZED_HILL_FUNCTION"));
    }
    return p;
  }
  
  /**
   * For space restricted reactions.
   * 
   * @param object
   * @return
   */
  public LocalParameter parameterTimeOrder() {
    Reaction r = kineticLaw.getParent();
    String reactionID = r.getId();
    StringBuffer id = StringTools.concat("hic_", reactionID);
    LocalParameter hr = createOrGetParameter(id.toString());
    if (!hr.isSetSBOTerm()) {
      // TODO: not yet available.
    }
    if (!hr.isSetName()) {
      hr.setName(MessageFormat.format(MESSAGES.getString("TIME_ORDER_IN_REACTION"), SBMLtools.getName(r)));
    }
    if (!hr.isSetUnits()) {
      hr.setUnits(Unit.Kind.DIMENSIONLESS);
    }
    return hr;
  }
  
  /**
   * For the additive Model: weight parameter
   * 
   * @param name
   * @param rid
   * @return weight for the weight matrix
   */
  public LocalParameter parameterV(String name, String rId) {
    LocalParameter p = createOrGetParameter("v_", rId, StringTools.underscore, name);
    if (!p.isSetSBOTerm()) {
      SBMLtools.setSBOTerm(p, 2);
    }
    if (!p.isSetUnits()) {
      //p.setUnits(unitFactory.unitPerTime());
      //p.setUnits(unitFactory.unitPerTimeOrSizePerTime(model.getSpecies(name).getCompartmentInstance()));
      p.setUnits(unitFactory.unitPerConcentrationOrSubstance(model.getSpecies(name)));
    }
    if (!p.isSetName()) {
      p.setName(MESSAGES.getString("FOR_ADDITIVE_MODEL")
        + ": " + MESSAGES.getString("WEIGHT_PARAMETER_FOR_EXTERNAL_INPUTS"));
    }
    return p;
  }
  
  /**
   * For the additive Model: weight parameter
   * 
   * @param name
   * @param rid
   * @return weight for the weight matrix
   */
  @SuppressWarnings("deprecation")
  public LocalParameter parameterVHSystem(String name, String rId) {
    LocalParameter p = createOrGetParameter("v_", rId, StringTools.underscore, name);
    if (!p.isSetSBOTerm()) {
      SBMLtools.setSBOTerm(p, 2);
    }
    if (!p.isSetUnits()) {
      Reaction reaction = model.getReaction(rId);
      ////p.setUnits(unitFactory.unitPerTime());
      //p.setUnits(unitFactory.unitPerTimeOrSizePerTime(model.getSpecies(name).getCompartmentInstance()));
      UnitDefinition ud = unitFactory.unitPerTimeAndConcentrationOrSubstance(reaction.getListOfModifiers(), false);
      if (unitFactory.getBringToConcentration()) {
        for (SpeciesReference specRef : reaction.getListOfProducts()) {
          ud.multiplyWith(specRef.getSpeciesInstance().getSpatialSizeUnitsInstance());
        }
      }
      p.setUnits(UnitFactory.checkUnitDefinitions(ud, model));
    }
    if (!p.isSetName()) {
      p.setName(MESSAGES.getString("FOR_ADDITIVE_MODEL")
        + ": " + MESSAGES.getString("WEIGHT_PARAMETER_FOR_EXTERNAL_INPUTS"));
    }
    return p;
  }
  
  /**
   * Creates and annotates the velocity constant for the reaction with the
   * given id and the number of enzymes.
   * 
   * @param enzyme
   *            Identifier of the catalyzing enzyme. Can be null.
   * @return
   */
  public LocalParameter parameterVelocityConstant(String enzyme) {
    LocalParameter kVr;
    Reaction r = kineticLaw.getParent();
    String reactionID = r.getId();
    if (enzyme != null) {
      kVr = createOrGetParameter("kcrg_", reactionID,
        StringTools.underscore, enzyme);
      if (!kVr.isSetName()) {
        kVr.setName(MessageFormat.format(
          MESSAGES.getString("CATALYTIC_RATE_CONSTANT_GEOMETIC_MEAN_OF_REACTION"),
          SBMLtools.getName(r)));
      }
      if (!kVr.isSetUnits()) {
        kVr.setUnits(unitFactory.unitPerTimeOrSizePerTime(model
          .getSpecies(enzyme).getCompartmentInstance()));
      }
    } else {
      kVr = createOrGetParameter("vmag_", reactionID);
      if (!kVr.isSetName()) {
        kVr.setName(MessageFormat.format(
          MESSAGES.getString("MAX_VELOCITY_GEOMETIC_MEAN_OF_REACTION"),
          SBMLtools.getName(r)));
      }
      if (!kVr.isSetSBOTerm()) {
        SBMLtools.setSBOTerm(kVr, 324);
      }
      if (!kVr.isSetUnits()) {
        if (unitFactory.getBringToConcentration()) {
          kVr.setUnits(unitFactory.unitSubstancePerSizePerTime(model.getVolumeUnitsInstance()));
        } else {
          kVr.setUnits(unitFactory.unitSubstancePerTime(model
            .getSubstanceUnitsInstance(), model
            .getTimeUnitsInstance()));
        }
      }
    }
    return kVr;
  }
  
  /**
   * Limiting rate
   * 
   * @param forward
   * @return
   */
  public LocalParameter parameterVmax(boolean forward) {
    return parameterKcatOrVmax(null, forward);
  }
  
  /**
   * For the additive Model: weight parameter
   * 
   * @param specId
   * @param rId
   * @return weight for the weight matrix
   */
  @SuppressWarnings("deprecation")
  public LocalParameter parameterW(String specId, String rId) {
    Species species = model.getSpecies(specId);
    LocalParameter p = createOrGetParameter("w_", rId, StringTools.underscore, specId);
    if (!p.isSetSBOTerm()) {
      SBMLtools.setSBOTerm(p, 2);
    }
    if (!p.isSetUnits()) {
      UnitDefinition ud = unitFactory.unitPerSubstance(species.getSubstanceUnitsInstance()).clone();
      
      if (unitFactory.getBringToConcentration()) {
        ud.multiplyWith(species.getSpatialSizeUnitsInstance());
      }
      ud = UnitFactory.checkUnitDefinitions(ud, model);
      
      //unitFactory.unitPerConcentrationOrSubstance(species));
      
      p.setUnits(ud);
    }
    if (!p.isSetName()) {
      p.setName(MESSAGES.getString("FOR_ADDITIVE_MODEL")
        + ": " + MESSAGES.getString("WEIGHT_PARAMETER_FOR_GENE_PRODUCTS"));
    }
    return p;
  }
  
  /**
   * For the additive Model: weight parameter
   * 
   * @return weight for the weight matrix
   */
  public LocalParameter parameterWH(String name, String rId) {
    LocalParameter p = createOrGetParameter("w_", rId,
      StringTools.underscore, name);
    if (!p.isSetSBOTerm()) {
      SBMLtools.setSBOTerm(p, 2);
    }
    if (!p.isSetUnits()) {
      p.setUnits(unitFactory.unitSubstancePerTime(model
        .getSubstanceUnitsInstance(), model
        .getTimeUnitsInstance()));
    }
    if (!p.isSetName()) {
      p.setName(MESSAGES.getString("FOR_ADDITIVE_MODEL")
        + ": " + MESSAGES.getString("WEIGHT_PARAMETER_FOR_GENE_PRODUCTS"));
    }
    return p;
  }
  
  /**
   * For the HSystem Model: weight parameter
   * 
   * @return weight for the weight matrix
   */
  public LocalParameter parameterWHS(String name, String rId) {
    Species species = model.getSpecies(name);
    LocalParameter p = createOrGetParameter("w_", rId,
      StringTools.underscore, name);
    if (!p.isSetSBOTerm()) {
      SBMLtools.setSBOTerm(p, 2);
    }
    if (!p.isSetUnits()) {
      if (unitFactory.getBringToConcentration() && species.hasOnlySubstanceUnits()) {
        p.setUnits(unitFactory.unitPerTimeOrSizePerTime(species.getCompartmentInstance()));
      } else {
        p.setUnits(unitFactory.unitPerTime());
      }
    }
    if (!p.isSetName()) {
      p.setName(MESSAGES.getString("FOR_ADDITIVE_MODEL")
        + ": " + MESSAGES.getString("WEIGHT_PARAMETER_FOR_GENE_PRODUCTS"));
    }
    return p;
  }
  
  public LocalParameter valueSubstancePerTime() {
    LocalParameter p = createOrGetParameter("sub_per_time");
    p.setValue(1d);
    
    if (!p.isSetUnits()) {
      p.setUnits(unitFactory.unitSubstancePerTime(model
        .getSubstanceUnitsInstance(), model
        .getTimeUnitsInstance()));
    }
    
    if (!p.isSetName()) {
      p.setName("substance per time");
    }
    
    return p;
  }
  
  public LocalParameter valuePerSubstanceAndConcentration() {
    LocalParameter p = createOrGetParameter("per_sub_con");
    p.setValue(1d);
    
    if (!p.isSetUnits()) {
      if (unitFactory.getBringToConcentration()) {
        p.setUnits(unitFactory.unitSubstancePerSize(model
          .getSubstanceUnitsInstance(), model
          .getVolumeUnitsInstance(), -1d));
      } else {
        p.setUnits(unitFactory.unitPerSubstance(model.getSubstanceUnitsInstance()));
      }
      
    }
    
    if (!p.isSetName()) {
      p.setName("");
    }
    
    return p;
  }
  
}
