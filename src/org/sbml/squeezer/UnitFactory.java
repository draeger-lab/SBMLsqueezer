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
import java.util.List;
import java.util.ResourceBundle;

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
import org.sbml.jsbml.util.filters.SBOFilter;
import org.sbml.squeezer.util.Bundles;

import de.zbit.util.ResourceManager;

/**
 * A factory class that creates frequently used instances of {@link Unit} and
 * {@link UnitDefinition} if these are not yet present in a {@link Model}. To
 * this end, it always checks the {@link Model} and tries to obtain existing
 * {@link UnitDefinition}s from the {@link Model} wheneer this is possible.
 * 
 * @author Andreas Dr&auml;ger
 * @date 2010-10-22
 * @version $Rev$
 */
public class UnitFactory {

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
		UnitDefinition ud = model.findIdentical(unitdef);
		if (ud == null) {
      String identifier = createId(unitdef);
      ud = model.getUnitDefinition(identifier);
      if (ud == null) {
      	updateAnnotation(unitdef, model.getSBMLDocument());
      	unitdef.setId(identifier);
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
	@SuppressWarnings("deprecation")
	private static String createId(UnitDefinition unitdef) {
	  StringBuilder sb = new StringBuilder();
    for (int i = 0; i < unitdef.getUnitCount(); i++) {
      Unit u = unitdef.getUnit(i);
      if (i > 0) {
        sb.append("_times_");
      }
      boolean brackets = (u.getExponent() != 1d) && u.isSetOffset();
      if (brackets) {
        sb.append("brackOpn_");
      }
      if (u.isSetOffset()) {
        sb.append(format(u.getOffset()));
        sb.append("_plus_");
      }
      if (u.getMultiplier() != 1d) {
        sb.append(format(u.getMultiplier()));
        sb.append("x");
      }
      if (u.getScale() != 0) {
				String prefix = (u.getScale() == -6) ? u.getPrefixAsWord() : u.getPrefix();
				if (prefix.contains("^")) {
				  sb.append("1E");
				  if (u.getScale() < 0) { 
				    sb.append("minus");
				  }
				  sb.append(u.getScale());
				  sb.append('x');
				} else {
					sb.append(prefix);
				}
      }
      sb.append(u.getKind());
      if (u.getExponent() != 1d) {
        if (brackets) {
          sb.append("_brackCls");
        }
        sb.append("_pow_");
        sb.append(format(u.getExponent()));
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
	 * @param ud
	 * @param sbmlDocument 
	 */
	private static void updateAnnotation(UnitDefinition ud, SBMLDocument doc) {
		for (int i = 0; i < ud.getUnitCount(); i++) {
			Unit unit = ud.getUnit(i);
			if (unit.isSetMetaId()) {
				unit.setMetaId(doc.nextMetaId());
			}
		}
	}

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
		
		if (speciesUnit.getUnitCount() == 1) {
			Unit u = speciesUnit.getUnit(0);
			u.setExponent(-1d);
			speciesUnit.setId("per_" + u.getKind().toString().toLowerCase());
			speciesUnit.setName("per substance or concentration");
		} else {
			speciesUnit = new UnitDefinition("per_substance_or_concentratiton", model.getLevel(), model
					.getVersion());
			speciesUnit.addUnit(new Unit(Unit.Kind.MOLE, -1, model.getLevel(), model
					.getVersion()));
			speciesUnit.setName("per mole");
		}
		
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
		
		UnitDefinition def = model.getUnitDefinition(speciesUnit.getId());
		if (def == null) {
			speciesUnit = checkUnitDefinitions(speciesUnit, model);
		}
		return model.getUnitDefinition(speciesUnit.getId());
		
		//return speciesUnit.raiseByThePowerOf(-1d);
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
					compartmentUnit = species.getSpatialSizeUnitsInstance();
				} else {
					Compartment compartment = species.getCompartmentInstance();
					compartmentUnit = compartment.getDerivedUnitDefinition().clone();
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
				for (Unit u : speciesUnit.getListOfUnits()) {
					if (specRef instanceof SpeciesReference) {
						SpeciesReference ref = (SpeciesReference) specRef;
						if (ref.isSetStoichiometry() && (ref.getStoichiometry() != 1d) && (x.length < listOf.size())) {
							u.setExponent(u.getExponent() * (ref.getStoichiometry()));
						}
					} else if (x.length == listOf.size()) {
						u.setExponent(x[i]);
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
		ud.addUnit(new Unit(1, 0, Unit.Kind.KELVIN, -1, level, version));
		ud.addUnit(new Unit(1, 0, Unit.Kind.MOLE, -1, level, version));
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
    ud.addUnit(new Unit(3, Unit.Kind.JOULE, level, version));
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
			ud.addUnit(new Unit(Unit.Kind.SECOND, -1, level, version));
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
		ud = ud.multiplyWith(model.getSubstanceUnitsInstance());
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
	 * @param modifiers
	 * @param zerothOrder
	 * @return
	 */
	public UnitDefinition unitPerTimeAndConcentrationOrSubstance(
			ListOf<SpeciesReference> participants,
			ListOf<ModifierSpeciesReference> modifiers, boolean zerothOrder) {
		UnitDefinition ud = unitPerTimeAndConcentrationOrSubstance(participants, zerothOrder);
		ListOf<? extends SimpleSpeciesReference> l = modifiers.filterList(new SBOFilter(SBO.getCatalyst()));
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
		return bringToConcentration ? unitSubstancePerSize(species) : species.getSubstanceUnitsInstance();
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
		mMperSecond = checkUnitDefinitions(mMperSecond, model);
		return mMperSecond;
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
		substancePerTime = checkUnitDefinitions(substancePerTime, model);
		return substancePerTime;
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

}
