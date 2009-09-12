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
package org.sbml.jsbml;

/**
 * @author Andreas Dr&auml;ger <a
 *         href="mailto:andreas.draeger@uni-tuebingen.de">
 *         andreas.draeger@uni-tuebingen.de</a>
 * @date 2009-08-31
 */
public class UnitDefinition extends AbstractNamedSBase {

	/**
	 * 
	 */
	private ListOf<Unit> listOfUnits;
	
	/**
	 * Predefined unit for substance.
	 */
	public static final UnitDefinition SUBSTANCE = getPredefinedUnitd("substance");
	
	/**
	 * Predefined unit for volume. 
	 */
	public static final UnitDefinition VOLUME = getPredefinedUnitd("volume");
	/**
	 * Predefined unit for area.
	 */
	public static final UnitDefinition AREA = getPredefinedUnitd("area");
	/**
	 * Predefined unit for length.
	 */
	public static final UnitDefinition LENGTH = getPredefinedUnitd("length");
	/**
	 * Predefined unit for time.
	 */
	public static final UnitDefinition TIME = getPredefinedUnitd("time");
	/**
	 * 
	 * @param id
	 * @return
	 */
	private static final UnitDefinition getPredefinedUnitd(String id) {
		id = id.toLowerCase();
		Unit u = new Unit(2, 4);
		String name = "Predefined unit ";
		if (id.equals("substance")) {
			u.setKind(Unit.Kind.MOLE);
			name += "mole";
		} else if (id.equals("volume")) {
			u.setKind(Unit.Kind.LITRE);
			name += "litre";
		} else if (id.equals("area")) {
			u.setKind(Unit.Kind.METRE);
			u.setExponent(2);
			name += "square metre";
		} else if (id.equals("length")) {
			u.setKind(Unit.Kind.METRE);
			name += "metre";
		} else if (id.equals("time")) {
			u.setKind(Unit.Kind.SECOND);
			name += "second";
		} else
			throw new IllegalArgumentException(
					"no predefined unit available for " + id);
		UnitDefinition ud = new UnitDefinition(id, 2, 4);
		ud.setName(name);
		ud.addUnit(u);
		return ud;
	}
	
	/**
	 * 
	 * @param id
	 */
	public UnitDefinition(String id, int level, int version) {
		super(id, level, version);
		initDefaults();
	}

	/**
	 * 
	 * @param id
	 * @param name
	 */
	public UnitDefinition(String id, String name, int level, int version) {
		super(id, name, level, version);
		initDefaults();
	}

	/**
	 * 
	 * @param nsb
	 */
	public UnitDefinition(UnitDefinition nsb) {
		super(nsb);
		listOfUnits = nsb.getListOfUnits().clone();
		setThisAsParentSBMLObject(listOfUnits);
	}

	/**
	 * 
	 * @param u
	 */
	public void addUnit(Unit u) {
		listOfUnits.add(u);
		u.parentSBMLObject = this;
		stateChanged();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.AbstractSBase#clone()
	 */
	// @Override
	public UnitDefinition clone() {
		return new UnitDefinition(this);
	}
	
	/**
	 * 
	 * @return
	 */
	public ListOf<Unit> getListOfUnits() {
		return listOfUnits;
	}
	
	/**
	 * 
	 * @return
	 */
	public int getNumUnits() {
		return listOfUnits.size();
	}

	/**
	 * 
	 * @param listOfUnits
	 */
	public void setListOfUnits(ListOf<Unit> listOfUnits) {
		this.listOfUnits = listOfUnits;
		setThisAsParentSBMLObject(listOfUnits);
		stateChanged();
	}

	/**
	 * 
	 */
	private void initDefaults() {
		listOfUnits = new ListOf<Unit>(getLevel(), getVersion());
		setThisAsParentSBMLObject(listOfUnits);
	}

}
