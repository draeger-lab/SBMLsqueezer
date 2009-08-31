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
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.sbml;

/**
 * 
 * @author Andreas Dr&auml;ger <a
 *         href="mailto:andreas.draeger@uni-tuebingen.de">
 *         andreas.draeger@uni-tuebingen.de</a>
 * 
 */
public class Species extends Variable {

	private boolean boundaryCondition;
	private int charge;
	private boolean constant;
	private Compartment compartment;
	private boolean hasOnlySubstanceUnits;
	private double initialAmount;
	private double initialConcentration;

	/**
	 * 
	 * @param species
	 */
	public Species(Species species) {
		super(species);
		this.boundaryCondition = species.getBoundaryCondition();
		this.charge = species.getCharge();
		this.compartment = species.getCompartmentInstance().clone();
		this.constant = species.getConstant();
		this.hasOnlySubstanceUnits = species.getHasOnlySubstanceUnits();
		if (species.isSetInitialAmount()) {
			this.initialAmount = species.getInitialAmount();
			this.initialConcentration = Double.NaN;
		} else if (species.isSetInitialConcentration()) {
			this.initialAmount = Double.NaN;
			this.initialConcentration = species.getInitialConcentration();
		} else {
			this.initialAmount = this.initialConcentration = Double.NaN;
		}
	}

	/**
	 * 
	 * @param id
	 */
	public Species(String id) {
		super(id);
		initDefaults();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.SBase#clone()
	 */
	// @Override
	public Species clone() {
		return new Species(this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.NamedSBase#equals(java.lang.Object)
	 */
	// @Override
	public boolean equals(Object o) {
		boolean equal = super.equals(o);
		if (o instanceof Species) {
			Species s = (Species) o;
			equal &= s.getBoundaryCondition() == boundaryCondition;
			equal &= s.getConstant() == constant;
			equal &= s.getHasOnlySubstanceUnits() == hasOnlySubstanceUnits;
			equal &= s.getCharge() == charge;
			if ((!s.isSetCompartment() && isSetCompartment())
					|| (s.isSetCompartment() && !isSetCompartment()))
				return false;
			if (s.isSetCompartment() && isSetCompartment())
				equal &= s.getCompartmentInstance().equals(compartment);
			equal &= s.getInitialAmount() == initialAmount;
			equal &= s.getInitialConcentration() == initialConcentration;
			return equal;
		} else
			equal = false;
		return equal;
	}

	/**
	 * 
	 * @return
	 */
	public boolean isSetCompartment() {
		return compartment != null;
	}

	public boolean getBoundaryCondition() {
		return isBoundaryCondition();
	}

	public int getCharge() {
		return charge != Integer.MIN_VALUE ? charge : 0;
	}

	public String getCompartment() {
		return compartment.getId();
	}

	public Compartment getCompartmentInstance() {
		return compartment;
	}

	public boolean getConstant() {
		return isConstant();
	}

	public boolean getHasOnlySubstanceUnits() {
		return isHasOnlySubstanceUnits();
	}

	public double getInitialAmount() {
		return initialAmount;
	}

	public double getInitialConcentration() {
		return initialConcentration;
	}

	public void initDefaults() {
		charge = Integer.MIN_VALUE;
		initialAmount = initialConcentration = Double.NaN;
		hasOnlySubstanceUnits = false;
		boundaryCondition = false;
		constant = false;
	}

	public boolean isBoundaryCondition() {
		return boundaryCondition;
	}

	public boolean isConstant() {
		return constant;
	}

	public boolean isHasOnlySubstanceUnits() {
		return hasOnlySubstanceUnits;
	}

	public boolean isSetCharge() {
		return charge != Integer.MIN_VALUE;
	}

	public boolean isSetInitialAmount() {
		return !Double.isNaN(initialAmount);
	}

	public boolean isSetInitialConcentration() {
		return !Double.isNaN(initialConcentration);
	}

	public void setBoundaryCondition(boolean boundaryCondition) {
		this.boundaryCondition = boundaryCondition;
		stateChanged();
	}

	public void setCharge(int charge) {
		this.charge = charge;
		stateChanged();
	}

	public void setCompartment(Compartment compartment) {
		this.compartment = compartment;
		stateChanged();
	}

	public void setConstant(boolean constant) {
		this.constant = constant;
		stateChanged();
	}

	public void setHasOnlySubstanceUnits(boolean hasOnlySubstanceUnits) {
		this.hasOnlySubstanceUnits = hasOnlySubstanceUnits;
		stateChanged();
	}

	public void setInitialAmount(double initialAmount) {
		this.initialAmount = initialAmount;
		stateChanged();
	}

	public void setInitialConcentration(double initialConcentration) {
		this.initialConcentration = initialConcentration;
		stateChanged();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.SBase#getParentSBMLObject()
	 */
	// @Override
	public Model getParentSBMLObject() {
		return (Model) parentSBMLObject;
	}
}
