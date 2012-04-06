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
import org.sbml.jsbml.util.StringTools;
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
		boolean contains = false;
		for (UnitDefinition ud : model.getListOfUnitDefinitions()) {
			if (UnitDefinition.areIdentical(ud, unitdef)) {
				unitdef = ud;
				contains = true;
				break;
			}
		}
		if (!contains) {
			updateAnnotation(unitdef, model.getSBMLDocument());
			model.addUnitDefinition(unitdef);
		}
		return unitdef;
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
	 * @param id
	 * @return
	 */
	private String checkId(String id) {
		StringBuilder sb = new StringBuilder();
		char c;
		if (Character.isDigit(id.charAt(0))) {
			sb.append('_');
		}
		for (int i = 0; i < id.length(); i++) {
			c = id.charAt(i);
			if (c == '^') {
				sb.append("_pow");
			} else if (c == '\u03BC') {
				sb.append("micro");
			} else if (Character.isDigit(c) || Character.isLetter(c)) {
				sb.append(c);
			} else if ((c == '.') && (id.charAt(i + 1) == '0')) {
				i++;
			} else {
				sb.append('_');
			}
		}
		return sb.toString();
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
	 * @param listOf
	 * @param zerothOrder
	 * @param x
	 * @return
	 */
	@SuppressWarnings("deprecation")
	private UnitDefinition unitConcentrationOrSubstance(
		List<? extends SimpleSpeciesReference> listOf, boolean zerothOrder,
		double... x) {
		int level = model.getLevel(), version = model.getVersion();
		UnitDefinition amount = new UnitDefinition("amount", level, version);
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
						if (ref.isSetStoichiometry() && (x.length < listOf.size())) {
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
		String id = "joule_per_kelvin_per_mole";
		UnitDefinition ud = model.getUnitDefinition(id);
		if (ud == null) {
			ud = new UnitDefinition(id, model.getLevel(), model.getVersion());
			ud.addUnit(Unit.Kind.JOULE);
			ud.addUnit(new Unit(1, 0, Unit.Kind.KELVIN, -1, model.getLevel(),
					model.getVersion()));
			ud.addUnit(new Unit(1, 0, Unit.Kind.MOLE, -1, model.getLevel(),
					model.getVersion()));
			ud = checkUnitDefinitions(ud, model);
		}
		return ud;
	}

	/**
	 * 
	 * @param substance
	 * @return
	 */
	public UnitDefinition unitkJperSubstance(UnitDefinition substance) {
		String id = "kjoule_per_" + substance.getId();
		UnitDefinition ud = model.getUnitDefinition(id);
		if (ud == null) {
			ud = new UnitDefinition(id, model.getLevel(), model.getVersion());
			ud.addUnit(new Unit(3, Unit.Kind.JOULE, model.getLevel(), model
					.getVersion()));
			ud.divideBy(substance);
			ud = checkUnitDefinitions(ud, model);
		}
		return ud;
	}

	/**
	 * 1/s, equivalent to Hz.
	 * 
	 * @return
	 */
	public UnitDefinition unitPerTime() {
		UnitDefinition ud = model.getTimeUnitsInstance().clone();
		if (ud.getUnitCount() == 1) {
			Unit u = ud.getUnit(0);
			u.setExponent(-1d);
			ud.setId("per_" + u.getKind().toString().toLowerCase());
			ud.setName("per time");
		} else {
			ud = new UnitDefinition("per_second", model.getLevel(), model
					.getVersion());
			ud.addUnit(new Unit(Unit.Kind.SECOND, -1, model.getLevel(), model
					.getVersion()));
			ud.setName("per second (Hz)");
		}
		UnitDefinition def = model.getUnitDefinition(ud.getId());
		if (def == null) {
			ud = checkUnitDefinitions(ud, model);
		}
		return model.getUnitDefinition(ud.getId());
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
		UnitDefinition ud = new UnitDefinition("ud", level, version);
		ud.divideBy(model.getTimeUnitsInstance());
		if (h != 0d) {
			for (Unit u : ud.getListOfUnits()) {
				u.setExponent(u.getExponent() - h);
			}
		}
		UnitDefinition amount = unitConcentrationOrSubstance(listOf, zerothOrder);
		ud = ud.divideBy(amount);
		ud = ud.multiplyWith(model.getSubstanceUnitsInstance());
		ud = ud.simplify();
		checkUnitDefinitions(ud, model);
		return ud;
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
		UnitDefinition ud = unitPerTimeAndConcentrationOrSubstance(listOf, zerothOrder, 0d);
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < ud.getUnitCount(); i++) {
			Unit u = ud.getUnit(i);
			if (i > 0) {
				sb.append('_');
			}
			if (u.getExponent() < 0) {
				sb.append("per_");
			}
			sb.append(u.getPrefix());
			sb.append(u.getKind().getName());
			if (u.getExponent() != 1d) {
				sb.append('^');
				sb.append(u.getExponent());
			}
		}
		String id = checkId(sb.toString());
		UnitDefinition def = model.getUnitDefinition(id);
		if (def == null) {
			ud.setId(id);
			ud = checkUnitDefinitions(ud, model);
		}
		return model.getUnitDefinition(id);
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
		UnitDefinition ud = unitPerTimeAndConcentrationOrSubstance(listOf,
				false, h, x, y);
		String id = bringToConcentration ? "concentration" : "substance";
		ud.setId(checkId(id + "_pow_" + (x * y) + "_per_time_pow_" + h));
		return checkUnitDefinitions(ud, model);
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
		ListOf<? extends SimpleSpeciesReference> l = modifiers
				.filterList(new SBOFilter(SBO.getCatalyst()));
		if (l.size() > 0) {
			for (SimpleSpeciesReference ssr : l) {
				Species s = ssr.getSpeciesInstance();
				UnitDefinition compUD = s.getCompartmentInstance().getUnitsInstance();
				if (bringToConcentration && s.hasOnlySubstanceUnits()) {
					ud.multiplyWith(compUD);
					ud.setId(ud.getId() + "_times_" + compUD.getId());
				} else if (!bringToConcentration && !s.hasOnlySubstanceUnits()) {
					ud.divideBy(compUD);
					ud.setId(ud.getId() + "_per_" + compUD.getId());
				}
				ud.divideBy(s.getUnitsInstance());
			}
			ud = checkUnitDefinitions(ud, model);
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
			StringBuilder name = new StringBuilder();
			if (!c.isSetUnits()) {
				c.setUnits(model.getVolumeUnitsInstance());
			}
			UnitDefinition sizeUnit = c.getUnitsInstance();
			if (sizeUnit.isVariantOfVolume()) {
				name.append("volume");
			} else if (sizeUnit.isVariantOfArea()) {
				name.append("area");
			} else if (sizeUnit.isVariantOfLength()) {
				name.append("length");
			}
			name.append(" per time");
			String id = name.toString().replace(' ', '_');
			UnitDefinition ud = model.getUnitDefinition(id);
			if (ud == null) {
				ud = new UnitDefinition(sizeUnit);
				ud.setId(id);
				ud.divideBy(model.getTimeUnitsInstance());
				ud.setName(name.toString());
				ud = checkUnitDefinitions(ud, model);
			}
			return ud;
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
		if (compartment == null) { throw new NullPointerException(
			MessageFormat.format(
				WARNINGS.getString("UNDEFINED_COMPARTMENT_OF_SPECIES"),
				species.toString())); }
		UnitDefinition substanceUnit = species.getSubstanceUnitsInstance();
		UnitDefinition sizeUnit = compartment.getUnitsInstance();
		if ((substanceUnit == null) || (sizeUnit == null)) { throw new NullPointerException(
			MessageFormat.format(
				WARNINGS.getString("UNDEFINED_UNIT_OF_SPECIES"),
				species.toString())); }
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
		StringBuffer id = StringTools.concat(substance.getId(), "_per_", size.getId());
		if (exponent != 1d) {
			id.append("_raised_by_");
			id.append(exponent);
		}
		UnitDefinition substancePerSize = model.getUnitDefinition(id.toString());
		if (substancePerSize == null) {
			substancePerSize = new UnitDefinition(id.toString(), model
					.getLevel(), model.getVersion());
			substancePerSize.multiplyWith(substance);
			substancePerSize.divideBy(size);
			substancePerSize.raiseByThePowerOf(exponent);
			substancePerSize = checkUnitDefinitions(substancePerSize, model);
		}
		return substancePerSize;
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
		String id = "substance_per_" + size.getId() + "_per_time";
		UnitDefinition mMperSecond = model.getUnitDefinition(id);
		if (mMperSecond == null) {
			mMperSecond = new UnitDefinition(id, model.getLevel(), model
					.getVersion());
			mMperSecond.multiplyWith(model.getSubstanceUnitsInstance());
			mMperSecond.divideBy(model.getTimeUnitsInstance());
			mMperSecond = checkUnitDefinitions(mMperSecond, model);
		}
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
		String id = substance.getId() + "_per_" + time.getId();
		UnitDefinition substancePerTime = model.getUnitDefinition(id);
		if (substancePerTime == null) {
			substancePerTime = new UnitDefinition(id, model.getLevel(), model.getVersion());
			substancePerTime.multiplyWith(substance);
			substancePerTime.divideBy(time);
			substancePerTime = checkUnitDefinitions(substancePerTime, model);
		}
		return substancePerTime;
	}

}
