/*
 *  SBMLsqueezer creates rate equations for reactions in SBML files
 *  (http://sbml.org).
 *  Copyright (C) 2009 ZBIT, University of Tübingen, Andreas Dräger
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.sbml.jlibsbml;

/**
 * @author Andreas Dr&auml;ger <a
 *         href="mailto:andreas.draeger@uni-tuebingen.de">
 *         andreas.draeger@uni-tuebingen.de</a>
 * @date 2009-08-31
 */
public enum UnitKind {
	/**
	 * The ampere unit
	 */
	UNIT_KIND_AMPERE,
	/**
	 * The becquerel unit.
	 */
	UNIT_KIND_BECQUEREL,
	/**
	 * The candela unit.
	 */
	UNIT_KIND_CANDELA,
	/**
	 * The Celsius unit.
	 */
	UNIT_KIND_CELSIUS,
	/**
	 * The coulomb unit.
	 */
	UNIT_KIND_COULOMB,
	/**
	 * A pseudo-unit indicating a dimensionless quantity. (This is in fact
	 * defined in the SBML specification.)
	 */
	UNIT_KIND_DIMENSIONLESS,
	/**
	 * The farad unit.
	 */
	UNIT_KIND_FARAD,
	/**
	 * The gram unit.
	 */
	UNIT_KIND_GRAM,
	/**
	 * The gray unit.
	 */
	UNIT_KIND_GRAY,
	/**
	 * The henry unit.
	 */
	UNIT_KIND_HENRY,
	/**
	 * The hertz unit.
	 */
	UNIT_KIND_HERTZ,
	/**
	 * A pseudo-unit representing a single 'thing'. (This is in fact defined in
	 * the SBML specification.)
	 */
	UNIT_KIND_ITEM,
	/**
	 * The joule unit.
	 */
	UNIT_KIND_JOULE,
	/**
	 * The katal unit.
	 */
	UNIT_KIND_KATAL,
	/**
	 * The kelvin unit.
	 */
	UNIT_KIND_KELVIN,
	/**
	 * The kilogram unit.
	 */
	UNIT_KIND_KILOGRAM,
	/**
	 * Alternate spelling of litre.
	 */
	UNIT_KIND_LITER,
	/**
	 * The litre unit.
	 */
	UNIT_KIND_LITRE,
	/**
	 * The lumen unit.
	 */
	UNIT_KIND_LUMEN,
	/**
	 * The lux unit.
	 */
	UNIT_KIND_LUX,
	/**
	 * Alternate spelling of metre.
	 */
	UNIT_KIND_METER,
	/**
	 * The metre unit.
	 */
	UNIT_KIND_METRE,
	/**
	 * The mole unit.
	 */
	UNIT_KIND_MOLE,
	/**
	 * The newton unit.
	 */
	UNIT_KIND_NEWTON,
	/**
	 * The ohm unit.
	 */
	UNIT_KIND_OHM,
	/**
	 * The pascal unit.
	 */
	UNIT_KIND_PASCAL,
	/**
	 * The radian unit.
	 */
	UNIT_KIND_RADIAN,
	/**
	 * The second unit.
	 */
	UNIT_KIND_SECOND,
	/**
	 * The siemens unit.
	 */
	UNIT_KIND_SIEMENS,
	/**
	 * The sievert unit.
	 */
	UNIT_KIND_SIEVERT,
	/**
	 * The steradian unit.
	 */
	UNIT_KIND_STERADIAN,
	/**
	 * The tesla unit.
	 */
	UNIT_KIND_TESLA,
	/**
	 * The volt unit.
	 */
	UNIT_KIND_VOLT,
	/**
	 * The watt unit.
	 */
	UNIT_KIND_WATT,
	/**
	 * The weber unit.
	 */
	UNIT_KIND_WEBER,
	/**
	 * Marker used by libSBML to indicate an invalid or unset unit.
	 */
	UNIT_KIND_INVALID
}
