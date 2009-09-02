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
package org.sbml;

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
	 * 
	 * @param id
	 */
	public UnitDefinition(String id) {
		super(id);
		initDefaults();
	}

	/**
	 * 
	 * @param id
	 * @param name
	 */
	public UnitDefinition(String id, String name) {
		super(id, name);
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
		listOfUnits = new ListOf<Unit>();
		setThisAsParentSBMLObject(listOfUnits);
	}

}
