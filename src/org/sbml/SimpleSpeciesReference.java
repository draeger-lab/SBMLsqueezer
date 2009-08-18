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
 * @author <a href="mailto:simon.schaefer@uni-tuebingen.de">Simon
 *         Sch&auml;fer</a>
 * 
 */
public abstract class SimpleSpeciesReference extends NamedSBase {

	private Species species;

	/**
	 * @param id
	 */
	public SimpleSpeciesReference() {
		super();
	}

	public SimpleSpeciesReference(SimpleSpeciesReference ssr) {
		super(ssr);
		this.species = ssr.getSpeciesInstance();
	}

	public SimpleSpeciesReference(Species spec) {
		this();
		this.species = spec;
	}

	public String getSpecies() {
		return species.getId();
	}

	public Species getSpeciesInstance() {
		return species;
	}

	public void setSpecies(Species spec) {
		this.species = spec;
		stateChanged();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.SBase#equals(java.lang.Object)
	 */
	// @Override
	public boolean equals(Object o) {
		if (o.getClass().getName().equals(getClass().getName())) {
			SimpleSpeciesReference ssr = (SimpleSpeciesReference) o;
			return ssr.getSBOTerm() == getSBOTerm()
					&& ssr.getSpeciesInstance().equals(species)
					&& ssr.getName().equals(getName());
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.SBase#getParentSBMLObject()
	 */
	// @Override
	public Reaction getParentSBMLObject() {
		return (Reaction) parentSBMLObject;
	}
}
