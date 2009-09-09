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

import org.sbml.squeezer.io.TextFormula;

/**
 * @author Andreas Dr&auml;ger <a
 *         href="mailto:andreas.draeger@uni-tuebingen.de">
 *         andreas.draeger@uni-tuebingen.de</a>
 * @date 2009-08-31
 */
public class Unit extends AbstractSBase {

	/**
	 * @author Andreas Dr&auml;ger <a
	 *         href="mailto:andreas.draeger@uni-tuebingen.de">
	 *         andreas.draeger@uni-tuebingen.de</a>
	 * @date 2009-08-31
	 */
	public static enum Kind {
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
		 * A pseudo-unit representing a single 'thing'. (This is in fact defined
		 * in the SBML specification.)
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

	/**
	 * 
	 */
	private Kind kind;
	/**
	 * 
	 */
	private int exponent;

	/**
	 * 
	 */
	private int scale;

	/**
	 * 
	 */
	private double multiplier;
	/**
	 * 
	 */
	private double offset;

	/**
	 * 
	 */
	public Unit(int level, int version) {
		super(level, version);
		initDefaults();
	}

	/**
	 * 
	 * @param kind
	 */
	public Unit(Kind kind, int level, int version) {
		super(level, version);
		initDefaults();
		this.kind = kind;
	}

	/**
	 * @param sb
	 */
	public Unit(Unit sb) {
		super(sb);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.AbstractSBase#clone()
	 */
	// @Override
	public Unit clone() {
		return new Unit(this);
	}

	/**
	 * 
	 * @return
	 */
	public int getExponent() {
		return exponent;
	}

	/**
	 * 
	 * @return
	 */
	public Kind getKind() {
		return kind;
	}

	/**
	 * 
	 * @return
	 */
	public double getMultiplier() {
		return multiplier;
	}

	/**
	 * 
	 * @return
	 */
	public int getScale() {
		return scale;
	}

	/**
	 * 
	 */
	public void initDefaults() {
		exponent = 1;
		scale = 0;
		multiplier = 1d;
		offset = 0d;
		kind = Kind.UNIT_KIND_INVALID;
	}

	/**
	 * 
	 * @param exponent
	 */
	public void setExponent(int exponent) {
		this.exponent = exponent;
	}

	/**
	 * 
	 * @param kind
	 */
	public void setKind(Kind kind) {
		this.kind = kind;
		stateChanged();
	}

	/**
	 * 
	 * @param multiplier
	 */
	public void setMultiplier(double multiplier) {
		this.multiplier = multiplier;
		stateChanged();
	}

	/**
	 * 
	 * @param scale
	 */
	public void setScale(int scale) {
		this.scale = scale;
		stateChanged();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.AbstractSBase#toString()
	 */
	// @Override
	public String toString() {
		StringBuffer times = TextFormula.times(multiplier, TextFormula.pow(
				Integer.valueOf(10), Integer.valueOf(scale)), kind);
		if (isSetOffset())
			times = TextFormula.sum(Double.toString(offset), times);
		return TextFormula.pow(times, exponent).toString();
	}

	/**
	 * 
	 * @return
	 */
	private boolean isSetOffset() {
		return offset != 0d;
	}

	/**
	 * 
	 * @param offset
	 */
	public void setOffset(double offset) {
		this.offset = offset;
	}

	/**
	 * 
	 * @return
	 */
	public double getOffset() {
		return offset;
	}

	/**
	 * 
	 * @return
	 */
	public boolean isKilogram() {
		return kind == Kind.UNIT_KIND_KILOGRAM;
	}

	/**
	 * 
	 * @return
	 */
	public boolean isDimensionless() {
		return kind == Kind.UNIT_KIND_DIMENSIONLESS;
	}

}
