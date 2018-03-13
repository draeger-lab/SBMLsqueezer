/*
 * $Id: UnitFactory.java 1092 2014-04-10 22:44:09Z draeger $
 * $URL: https://rarepos.cs.uni-tuebingen.de/svn-path/SBMLsqueezer/trunk/src/org/sbml/squeezer/UnitFactory.java $
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
package org.sbml.squeezer;

import java.text.MessageFormat;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

import org.sbml.jsbml.CVTerm;
import org.sbml.jsbml.CVTerm.Qualifier;
import org.sbml.jsbml.Compartment;
import org.sbml.jsbml.ListOf;
import org.sbml.jsbml.Model;
import org.sbml.jsbml.ModifierSpeciesReference;
import org.sbml.jsbml.SBMLDocument;
import org.sbml.jsbml.SBO;
import org.sbml.jsbml.SimpleSpeciesReference;
import org.sbml.jsbml.Species;
import org.sbml.jsbml.SpeciesReference;
import org.sbml.jsbml.Unit;
import org.sbml.jsbml.UnitDefinition;
import org.sbml.jsbml.util.Maths;
import org.sbml.jsbml.util.StringTools;
import org.sbml.jsbml.util.filters.SBOFilter;
import org.sbml.squeezer.util.Bundles;

import de.zbit.util.ResourceManager;

/**
 * A factory class that creates frequently used instances of {@link Unit} and
 * {@link UnitDefinition} if these are not yet present in a {@link Model}. To
 * this end, it always checks the {@link Model} and tries to obtain existing
 * {@link UnitDefinition}s from the {@link Model} whenever this is possible.
 * 
 * @author Andreas Dr&auml;ger
 * @date 2010-10-22
 * @version $Rev: 1092 $
 */
public class UnitFactory {
  
  /**
   * Localization support.
   */
  public static final transient ResourceBundle WARNINGS = ResourceManager.getBundle(Bundles.WARNINGS);
  
  /**
   * Avoids adding identical unit definitions multiple times to the model.
   * 
   * @param unitdef
   *            a unit definition that should be added to the given model.
   * @param model
   *            the model that is to be tested if it really lacks the given
   *            unit definition.
   * @return the unit definition found in the model or the given unit
   *         definition that was added to the model if lacking.
   */
  public static final UnitDefinition checkUnitDefinitions(
    UnitDefinition unitdef, Model model) {
    for (int i = unitdef.getUnitCount() - 1; i >= 0; i--) {
      Unit u = unitdef.getUnit(i);
      if (u.isDimensionless()) {
    	u.removeScale();
    	if (!u.isSetMultiplier() || (u.getMultiplier() == 1d)) {
          unitdef.removeUnit(i);
    	}
      }
    }
    UnitDefinition ud = model.findIdentical(unitdef);
    if (ud == null) {
      String identifier = createId(unitdef);
      //      if (Unit.Kind.isValidUnitKindString(identifier, model.getLevel(), model.getVersion())) {
      //
      //      }
      ud = model.getUnitDefinition(identifier);
      if (ud == null) {
        updateAnnotation(unitdef, model.getSBMLDocument());
        if (Unit.Kind.isValidUnitKindString(identifier, model.getLevel(), model.getVersion())) {
          unitdef = model.getPredefinedUnitDefinition(identifier.toLowerCase() + UnitDefinition.BASE_UNIT_SUFFIX);
        } else {
          unitdef.setId(identifier);
          unitdef.setName(createName(unitdef));
        }
        model.addUnitDefinition(unitdef);
        return unitdef;
      }
    }
    return ud;
  }
  
  /**
   * 
   * @param unitdef
   * @return
   */
  public static String createId(UnitDefinition unitdef) {
    return createLabel(unitdef, true);
  }
  
  /**
   * 
   * @param unitdef
   * @return
   */
  public static String createName(UnitDefinition unitdef) {
    return createLabel(unitdef, false);
  }
  
  /**
   * 
   * @param unitdef
   * @param forID
   * @return
   */
  @SuppressWarnings("deprecation")
  private static String createLabel(UnitDefinition unitdef, boolean forID) {
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < unitdef.getUnitCount(); i++) {
      
      Unit u = unitdef.getUnit(i);
      int scale = u.getScale();
      double exp = u.getExponent();
      double multiplier = u.getMultiplier();
      String kind = forID ? u.getKind().toString().toLowerCase() : u.getKind().getName();
      
      if (i > 0) {
        sb.append(forID ? '_' : ' ');
      }
      if (exp < 0d) {
        sb.append("per");
        sb.append(forID ? '_' : ' ');
        exp *= -1;
      } else if (i > 0) {
        sb.append("times");
        sb.append(forID ? '_' : ' ');
      }
      boolean brackets = (exp != 1d) && u.isSetOffset();
      if (brackets) {
        sb.append(forID ? "brackOpn_" : "(");
      }
      if (u.isSetOffset() && (u.getOffset() != 0d)) {
        if (forID) {
          sb.append(format(u.getOffset()));
          sb.append("_plus_");
        } else {
          sb.append(StringTools.toString(Locale.ENGLISH, u.getOffset()));
          //sb.append("\u00A0+\u00A0");
          sb.append(" + ");
        }
      }
      
      if (u.isSecond()) {
        if (multiplier >= 3600) {
          kind = "hour";
          multiplier = multiplier/3600;
        } else if (multiplier >= 60) {
          kind = "min";
          multiplier = multiplier/60;
        }
      }
      
      if (multiplier != 1d) {
        if (forID) {
          sb.append(format(multiplier));
          sb.append("_x_");
        } else {
          sb.append(StringTools.toString(Locale.ENGLISH, multiplier));
          //sb.append("\u00A0\u2219\u00A0");
          sb.append(" x ");
        }
      }
      if (scale != 0) {
        String prefix = (scale == -6) ? u.getPrefixAsWord() : u.getPrefix();
        if (!prefix.contains("^")) {
          if (forID || (scale != -6)) {
            sb.append(prefix);
          } else {
            sb.append(u.getPrefixAsWord());
          }
        } else {
          if (forID) {
            sb.append("1E");
            if (scale < 0) {
              sb.append("minus");
              sb.append((-1) * scale);
            } else {
              sb.append(scale);
            }
            sb.append("_x_");
          } else {
            sb.append("10");
            //sb.append(exponent(scale));
            sb.append('^');
            sb.append(Integer.toString(scale));
          }
        }
      }
      sb.append(kind);
      if (exp != 1d) {
        if (brackets) {
          sb.append(forID ? "_brackCls" : ")");
        }
        if (forID) {
          sb.append("_pow_");
          sb.append(format(exp));
        } else {
          if (((int) exp) - exp == 0) {
            //sb.append(exponent((int) exp));
            sb.append('^');
            sb.append(Integer.toString((int) exp));
          } else {
            sb.append('^');
            sb.append(StringTools.toString(Locale.ENGLISH, exp));
          }
        }
      }
    }
    String id = sb.toString();
    if (id.length() > 0) {
      if (Character.isDigit(id.charAt(0))) {
        sb.insert(0, '_');
      }
    }
    return sb.toString();
  }
  
  /**
   * 
   * @param scale
   * @return
   */
  private static String exponent(int scale) {
    StringBuffer sb = new StringBuffer();
    for (char num : Integer.toString(scale).toCharArray()) {
      switch (num) {
        case '0':
          sb.append('\u2070');
          break;
        case '1':
          sb.append('\u00B9');
          break;
        case '2':
          sb.append('\u00B2');
          break;
        case '3':
          sb.append('\u00B3');
          break;
        case '4':
          sb.append('\u2074');
          break;
        case '5':
          sb.append('\u2075');
          break;
        case '6':
          sb.append('\u2076');
          break;
        case '7':
          sb.append('\u2077');
          break;
        case '8':
          sb.append('\u2078');
          break;
        case '9':
          sb.append('\u2079');
          break;
      }
    }
    return sb.toString();
  }
  
  /**
   * 
   * @param num
   * @return
   */
  private static String format(double num) {
    StringBuilder sb = new StringBuilder();
    if (num < 0d) {
      sb.append("minus");
      num *= -1d;
    }
    if (Maths.isInt(num)) {
      sb.append(Integer.toString((int) num));
    } else {
      sb.append(Double.toString(num).replace(".", "dot"));
    }
    return sb.toString();
  }
  
  /**
   * 
   * @param lou
   * @param doc
   */
  private static void updateAnnotation(UnitDefinition ud, SBMLDocument doc) {
    ListOf<Unit> lou = ud.getListOfUnits();
    if (ud.isSetMetaId()) {
      ud.setMetaId(doc.nextMetaId());
    }
    if (lou.isSetMetaId()) {
      lou.setMetaId(doc.nextMetaId());
    }
    for (Unit u : lou) {
      if (u.getCVTermCount() == 0) {
        String resource = u.getKind().getUnitOntologyResource();
        if (resource != null) {
          // metaid will be created upon nessesity.
          // TODO: BQB_IS seems to be a better annotation, but we can't ensure that the unit won't be changed later on. Maybe it will get a multiplier or exponent that changes the actual unit.
          u.addCVTerm(new CVTerm(Qualifier.BQB_IS_VERSION_OF, resource));
        }
      }
      if (u.isSetMetaId()) {
        u.setMetaId(doc.nextMetaId());
      }
    }
  }
  
  /**
   * 
   */
  private boolean bringToConcentration;
  
  /**
   * Pointer to the model for which {@link UnitDefinition} objects are to be
   * created.
   */
  private Model model;
  
  /**
   * 
   * @param model
   * @param bringToConcentration
   */
  public UnitFactory(Model model, boolean bringToConcentration) {
    this.model = model;
    this.bringToConcentration = bringToConcentration;
  }
  
  /**
   * 
   * @return
   */
  public boolean getBringToConcentration() {
    return bringToConcentration;
  }
  
  /**
   * 
   * @param specRef
   * @param zerothOrder
   * @param x
   * @return
   */
  @SuppressWarnings("deprecation")
  public UnitDefinition unitPerConcentrationOrSubstance(Species species) {
    int level = model.getLevel(), version = model.getVersion();
    UnitDefinition speciesUnit = species.getDerivedUnitDefinition().clone();
    UnitDefinition compartmentUnit;
    
    if ((level == 2) && ((version == 1) || (version == 2))) {
      compartmentUnit = species.getSpatialSizeUnitsInstance();
    } else {
      Compartment compartment = species.getCompartmentInstance();
      compartmentUnit = compartment.getDerivedUnitDefinition().clone();
    }
    
    //    if (speciesUnit.getUnitCount() == 1) {
    //      Unit u = speciesUnit.getUnit(0);
    //      u.setExponent(-1d);
    //    } else {
    //      speciesUnit = new UnitDefinition(level, version);
    //      speciesUnit.addUnit(new Unit(1d, 0, Unit.Kind.MOLE, -1d, level, version));
    //    }
    
    if (bringToConcentration) {
      if (species.hasOnlySubstanceUnits()) {
        // species per compartment size
        speciesUnit.multiplyWith(compartmentUnit);
      } else {
        // species
      }
    } else {
      if (species.hasOnlySubstanceUnits()) {
        // species
      } else {
        // species times compartment size
        speciesUnit.divideBy(compartmentUnit);
      }
    }
    speciesUnit.raiseByThePowerOf(-1);
    return checkUnitDefinitions(speciesUnit, model);
  }
  
  /**
   * 
   * @param listOf
   * @param zerothOrder
   * @param x
   * @return
   */
  @SuppressWarnings("deprecation")
  public UnitDefinition unitConcentrationOrSubstance(
    List<? extends SimpleSpeciesReference> listOf, boolean zerothOrder,
    double... x) {
    int level = model.getLevel(), version = model.getVersion();
    UnitDefinition amount = new UnitDefinition(level, version);
    if (!zerothOrder) {
      UnitDefinition speciesUnit, compartmentUnit;
      SimpleSpeciesReference specRef;
      Species species;
      for (int i = 0; i < listOf.size(); i++) {
        specRef = listOf.get(i);
        species = specRef.getSpeciesInstance();
        speciesUnit = species.getDerivedUnitDefinition().clone();
        if ((level == 2) && ((version == 1) || (version == 2))) {
          compartmentUnit = species.getSpatialSizeUnitsInstance().clone();
        } else {
          Compartment compartment = species.getCompartmentInstance();
          compartmentUnit = compartment.getDerivedUnitDefinition().clone();
        }
        if ((compartmentUnit != null) && (specRef instanceof SpeciesReference)) {
          SpeciesReference ref = (SpeciesReference) specRef;
          if (ref.isSetStoichiometry() && (ref.getStoichiometry() != 1d) && (x.length < listOf.size())) {
            compartmentUnit.raiseByThePowerOf(ref.getStoichiometry());
          }
        }
        for (Unit u : speciesUnit.getListOfUnits()) {
          if (specRef instanceof SpeciesReference) {
            SpeciesReference ref = (SpeciesReference) specRef;
            if (ref.isSetStoichiometry() && (ref.getStoichiometry() != 1d) && (x.length < listOf.size())) {
              u.setExponent(u.getExponent() * ref.getStoichiometry());
            }
          } else if (x.length == listOf.size()) {
            u.setExponent(x[i]);
          }
        }
        if (bringToConcentration) {
          if (species.hasOnlySubstanceUnits()) {
            // species per compartment size
            speciesUnit.divideBy(compartmentUnit);
          } else {
            // species
          }
        } else {
          if (species.hasOnlySubstanceUnits()) {
            // species
          } else {
            // species times compartment size
            speciesUnit.multiplyWith(compartmentUnit);
          }
        }
        amount.multiplyWith(speciesUnit);
      }
    }
    return amount;
  }
  
  /**
   * Creates a new unit object or returns an existing one from the model.
   * 
   * @return Unit joule per kelvin and per mole.
   */
  public UnitDefinition unitJperKandM() {
    int level = model.getLevel(), version = model.getVersion();
    UnitDefinition ud = new UnitDefinition(level, version);
    ud.addUnit(Unit.Kind.JOULE);
    ud.addUnit(new Unit(1d, 0, Unit.Kind.KELVIN, -1d, level, version));
    ud.addUnit(new Unit(1d, 0, Unit.Kind.MOLE, -1d, level, version));
    return checkUnitDefinitions(ud, model);
  }
  
  /**
   * 
   * @param substance
   * @return
   */
  public UnitDefinition unitkJperSubstance(UnitDefinition substance) {
    int level = model.getLevel(), version = model.getVersion();
    UnitDefinition ud = new UnitDefinition(level, version);
    ud.addUnit(new Unit(1d, 3, Unit.Kind.JOULE, 1d, level, version));
    ud.divideBy(substance);
    return checkUnitDefinitions(ud, model);
  }
  
  /**
   * 1/s, equivalent to Hz.
   * 
   * @return
   */
  public UnitDefinition unitPerTime() {
    UnitDefinition ud = model.getTimeUnitsInstance().clone();
    int level = model.getLevel(), version = model.getVersion();
    if (ud.getUnitCount() == 1) {
      ud.getUnit(0).setExponent(-1d);
    } else {
      ud = new UnitDefinition(level, version);
      ud.addUnit(new Unit(1d, 0, Unit.Kind.SECOND, -1d, level, version));
    }
    return checkUnitDefinitions(ud, model);
  }
  
  /**
   * 
   * @param listOf
   * @param zerothOrder
   * @param h
   *            Exponent for the time
   * @param x
   *            Exponents for the species (parameter value, other than
   *            stoichiometry).
   * @return
   */
  private UnitDefinition unitPerTimeAndConcentrationOrSubstance(
    List<? extends SimpleSpeciesReference> listOf, boolean zerothOrder,
    double h, double... x) {
    int level = model.getLevel(), version = model.getVersion();
    UnitDefinition ud = new UnitDefinition(level, version);
    ud.divideBy(model.getTimeUnitsInstance());
    if (h != 0d) {
      for (Unit u : ud.getListOfUnits()) {
        u.setExponent(u.getExponent() - h);
      }
    }
    UnitDefinition amount = unitConcentrationOrSubstance(listOf, zerothOrder, x);
    ud = ud.divideBy(amount);
    //ud = ud.multiplyWith(model.getSubstanceUnitsInstance());
    return checkUnitDefinitions(ud.simplify(), model);
  }
  
  /**
   * 
   * @param listOf
   * @param zerothOrder
   *            if true this unit will be created for a zeroth order rate
   *            constant.
   * @return
   */
  public UnitDefinition unitPerTimeAndConcentrationOrSubstance(
    ListOf<? extends SimpleSpeciesReference> listOf, boolean zerothOrder) {
    return unitPerTimeAndConcentrationOrSubstance(listOf, zerothOrder, 0d);
  }
  
  /**
   * @param listOf
   * @param h
   * @param x
   * @param y
   * @return
   */
  public UnitDefinition unitPerTimeAndConcentrationOrSubstance(
    ListOf<? extends SimpleSpeciesReference> listOf, double h,
    double x, double y) {
    return unitPerTimeAndConcentrationOrSubstance(listOf, false, h, x, y);
  }
  
  /**
   * 
   * @param participants
   * @param modifiers filters for SBOterms 'catalyst' and 'catalysis'
   * @param zerothOrder
   * @return
   */
  public UnitDefinition unitPerTimeAndConcentrationOrSubstance(
    ListOf<SpeciesReference> participants,
    ListOf<ModifierSpeciesReference> modifiers, boolean zerothOrder) {
    UnitDefinition ud = unitPerTimeAndConcentrationOrSubstance(participants, zerothOrder);
    List<? extends SimpleSpeciesReference> l = modifiers.filterList(new SBOFilter(SBO.getCatalyst(), SBO.getCatalysis()));
    if (l.size() > 0) {
      UnitDefinition amount = unitConcentrationOrSubstance(l, zerothOrder);
      ud = ud.clone();
      ud = ud.divideBy(amount);
      return checkUnitDefinitions(ud.simplify(), model);
    }
    return ud;
  }
  
  /**
   * 
   * @param c
   * @return
   */
  public UnitDefinition unitPerTimeOrSizePerTime(Compartment c) {
    if (bringToConcentration) {
      if (!c.isSetUnits()) {
        c.setUnits(model.getVolumeUnitsInstance());
      }
      UnitDefinition ud = c.getUnitsInstance().clone();
      ud.divideBy(model.getTimeUnitsInstance());
      return checkUnitDefinitions(ud, model);
    } else {
      return unitPerTime();
    }
  }
  
  /**
   * 
   * @param species
   * @return
   */
  public UnitDefinition unitSubstancePerSize(Species species) {
    Compartment compartment = species.getCompartmentInstance();
    if (compartment == null) {
      throw new NullPointerException(MessageFormat.format(
        WARNINGS.getString("UNDEFINED_COMPARTMENT_OF_SPECIES"),
        species.toString()));
    }
    UnitDefinition substanceUnit = species.getSubstanceUnitsInstance();
    UnitDefinition sizeUnit = compartment.getUnitsInstance();
    if ((substanceUnit == null) || (sizeUnit == null)) {
      throw new NullPointerException(MessageFormat.format(
        WARNINGS.getString("UNDEFINED_UNIT_OF_SPECIES"),
        species.toString()));
    }
    return unitSubstancePerSize(substanceUnit, sizeUnit);
  }
  
  /**
   * 
   * @param substanceUnit
   * @param sizeUnit
   * @return
   */
  public UnitDefinition unitSubstancePerSize(UnitDefinition substanceUnit,
    UnitDefinition sizeUnit) {
    return unitSubstancePerSize(substanceUnit, sizeUnit, 1d);
  }
  
  /**
   * 
   * @param substance
   * @param size
   * @param exponent
   * @return
   */
  public UnitDefinition unitSubstancePerSize(UnitDefinition substance,
    UnitDefinition size, double exponent) {
    UnitDefinition substancePerSize = new UnitDefinition(model.getLevel(), model.getVersion());
    substancePerSize.multiplyWith(substance);
    substancePerSize.divideBy(size);
    substancePerSize.raiseByThePowerOf(exponent);
    return checkUnitDefinitions(substancePerSize, model);
  }
  
  /**
   * 
   * @param species
   * @return
   */
  public UnitDefinition unitSubstancePerSizeOrSubstance(Species species) {
    if (bringToConcentration) {
      return unitSubstancePerSize(species);
    }
    if (species.isSetSubstanceUnits()) {
      return species.getSubstanceUnitsInstance();
    }
    if (species.getLevel() > 2) {
      Model model = species.getModel();
      if (model.isSetSubstanceUnits()) {
        return model.getSubstanceUnitsInstance();
      }
    }
    return null;
  }
  
  /**
   * Returns the unit substance per size per second.
   * 
   * @param size
   *            unit of size
   * @return
   */
  public UnitDefinition unitSubstancePerSizePerTime(UnitDefinition size) {
    UnitDefinition mMperSecond = new UnitDefinition(model.getLevel(), model.getVersion());
    mMperSecond.multiplyWith(model.getSubstanceUnitsInstance());
    mMperSecond.divideBy(model.getTimeUnitsInstance());
    return checkUnitDefinitions(mMperSecond, model);
  }
  
  /**
   * 
   * @param substance
   * @param time
   * @return
   */
  public UnitDefinition unitSubstancePerTime(UnitDefinition substance,
    UnitDefinition time) {
    UnitDefinition substancePerTime = new UnitDefinition(model.getLevel(), model.getVersion());
    substancePerTime.multiplyWith(substance);
    substancePerTime.divideBy(time);
    return checkUnitDefinitions(substancePerTime, model);
  }
  
  /**
   * 
   * @param substance
   * @return
   */
  public UnitDefinition unitPerSubstance(UnitDefinition substance) {
    UnitDefinition substancePerSize = new UnitDefinition(model.getLevel(), model.getVersion());
    substancePerSize.divideBy(substance);
    return checkUnitDefinitions(substancePerSize, model);
  }
  
  /**
   * 
   * @param species
   * @param exponent
   * @return
   */
  public UnitDefinition unitSubstancePerSizeOrSubstance(Species species, double exponent) {
    if (exponent == 1d) {
      return unitSubstancePerSizeOrSubstance(species);
    }
    UnitDefinition ud = unitSubstancePerSizeOrSubstance(species).clone();
    ud.raiseByThePowerOf(exponent);
    return checkUnitDefinitions(ud, species.getModel());
  }
  
}
