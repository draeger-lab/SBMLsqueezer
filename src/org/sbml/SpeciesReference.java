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
public class SpeciesReference extends SimpleSpeciesReference {

	private double stoichiometry;

	public SpeciesReference() {
		super();
		initDefaults();
	}

	public SpeciesReference(Species spec) {
		super(spec);
		initDefaults();
	}

	public SpeciesReference(SpeciesReference speciesReference) {
		this();
		// TODO Auto-generated constructor stub
	}

	/*
	 * (non-Javadoc)
	 * @see org.sbml.SBase#clone()
	 */
	// @Override
	public SpeciesReference clone() {
		return new SpeciesReference(this);
	}

	/*
	 * (non-Javadoc)
	 * @see org.sbml.SBase#equals(java.lang.Object)
	 */
	// @Override
	public boolean equals(Object o) {
		// TODO Auto-generated method stub
		return false;
	}

	public double getStoichiometry() {
		return stoichiometry;
	}

	public void initDefaults() {
		stoichiometry = 1;
	}

	public void setStoichiometry(double stoichiometry) {
		this.stoichiometry = stoichiometry;
		stateChanged();
	}
	
	/**
	 * This method is convenient when holding an object nested inside other
	 * objects in an SBML model. It allows direct access to the &lt;model&gt;
	 * 
	 * element containing it.
	 * 
	 * @return Returns the parent SBML object.
	 */
	public Species getParentSBMLObject() {
		return (Species) parentSBMLObject;
	}

}
