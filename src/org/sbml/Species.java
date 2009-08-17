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
public class Species extends NamedSBase {

	private boolean boundaryCondition;
	private int charge;
	private boolean constant;
	// private Compartment compartment;
	private boolean hasOnlySubstanceUnits;
	private double initialAmount;
	private double initialConcentration;

	public Species(Species species) {
		this(species.getId());
		// TODO Auto-generated constructor stub
	}

	public Species(String id) {
		super(id);
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
	 * @see org.sbml.SBase#equals(java.lang.Object)
	 */
	// @Override
	public boolean equals(Object o) {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean getBoundaryCondition() {
		return isBoundaryCondition();
	}

	public int getCharge() {
		return charge;
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
		// TODO
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

	public void setBoundaryCondition(boolean boundaryCondition) {
		this.boundaryCondition = boundaryCondition;
	}

	public void setCharge(int charge) {
		this.charge = charge;
	}

	public void setConstant(boolean constant) {
		this.constant = constant;
	}

	public void setHasOnlySubstanceUnits(boolean hasOnlySubstanceUnits) {
		this.hasOnlySubstanceUnits = hasOnlySubstanceUnits;
	}

	public void setInitialAmount(double initialAmount) {
		this.initialAmount = initialAmount;
	}

	public void setInitialConcentration(double initialConcentration) {
		this.initialConcentration = initialConcentration;
	}

	public boolean isSetInitialAmount() {
		return initialAmount != Double.NaN;
	}
	
	public boolean isSetInitialConcentration() {
		return initialConcentration != Double.NaN;
	}
	/**
	 * This method is convenient when holding an object nested inside other
	 * objects in an SBML model. It allows direct access to the &lt;model&gt;
	 * 
	 * element containing it.
	 * 
	 * @return Returns the parent SBML object.
	 */
	public Reaction getParentSBMLObject() {
		return (Reaction) parentSBMLObject;
	}
}
