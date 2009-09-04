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
 * @date 2009-09-04
 */
public class ModelCreator {

	private String email;
	private String familyName;
	private String givenName;
	private String organisation;

	/**
	 * 
	 */
	public ModelCreator() {

	}

	/**
	 * 
	 * @param modelCreator
	 */
	public ModelCreator(ModelCreator modelCreator) {
		// TODO Auto-generated constructor stub
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#clone()
	 */
	public ModelCreator clone() {
		return new ModelCreator(this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object sb) {
		return false;

	}

	/**
	 * Returns the email from the ModelCreator.
	 * 
	 * @return
	 */
	public String getEmail() {
		return email;
	}

	/**
	 * Returns the familyName from the ModelCreator.
	 * 
	 * @return
	 */
	public String getFamilyName() {
		return familyName;
	}

	/**
	 * Returns the givenName from the ModelCreator.
	 * 
	 * @return
	 */
	public String getGivenName() {
		return givenName;
	}

	/**
	 * Returns the organization from the ModelCreator.
	 * 
	 * @return
	 */
	public String getOrganisation() {
		return organisation;
	}

	/**
	 * Returns the organization from the ModelCreator.
	 * 
	 * @return
	 */
	public String getOrganization() {
		return organisation;
	}

	/**
	 * Predicate returning true or false depending on whether this
	 * ModelCreator's email has been set.
	 * 
	 * @return
	 */
	public boolean isSetEmail() {
		return email != null;
	}

	/**
	 * Predicate returning true or false depending on whether this
	 * ModelCreator's familyName has been set.
	 * 
	 * @return
	 */
	public boolean isSetFamilyName() {
		return familyName != null;
	}

	/**
	 * Predicate returning true or false depending on whether this
	 * ModelCreator's givenName has been set.
	 * 
	 * @return
	 */
	public boolean isSetGivenName() {
		return givenName != null;
	}

	/**
	 * Predicate returning true or false depending on whether this
	 * ModelCreator's organization has been set.
	 * 
	 * @return
	 */
	public boolean isSetOrganisation() {
		return organisation != null;
	}

	/**
	 * Predicate returning true or false depending on whether this
	 * ModelCreator's organization has been set.
	 * 
	 * @return
	 */
	public boolean isSetOrganization() {
		return isSetOrganisation();
	}

	/**
	 * Sets the email
	 * 
	 * @param email
	 * @return
	 */
	public int setEmail(String email) {
		this.email = email;
		// TODO
		return 0;
	}

	/**
	 * Sets the family name
	 * 
	 * @param familyName
	 * @return
	 */
	public int setFamilyName(String familyName) {
		this.familyName = familyName;
		// TODO
		return 0;
	}

	/**
	 * Sets the family name
	 * 
	 * @param givenName
	 * @return
	 */
	public int setGivenName(String givenName) {
		this.givenName = givenName;
		// TODO
		return 0;
	}

	/**
	 * Sets the organization
	 * 
	 * @param organization
	 * @return
	 */
	public int setOrganisation(String organization) {
		this.organisation = organization;
		// TODO
		return 0;
	}

	/**
	 * Sets the organization
	 * 
	 * @param organization
	 * @return
	 */
	public int setOrganization(String organization) {
		return setOrganisation(organization);
	}

	/**
	 * Unsets the email of this ModelCreator.
	 * 
	 * @return
	 */
	public int unsetEmail() {
		email = null;
		// TODO
		return 0;
	}

	/**
	 * Unsets the familyName of this ModelCreator.
	 * 
	 * @return
	 */
	public int unsetFamilyName() {
		familyName = null;
		// TODO
		return 0;
	}

	/**
	 * Unsets the givenName of this ModelCreator.
	 * 
	 * @return
	 */
	public int unsetGivenName() {
		givenName = null;
		// TODO
		return 0;
	}

	/**
	 * Unsets the organization of this ModelCreator.
	 * 
	 * @return
	 */
	public int unsetOrganisation() {
		organisation = null;
		// TODO
		return 0;
	}

	/**
	 * Unsets the organization of this ModelCreator.
	 * 
	 * @return
	 */
	public int unsetOrganization() {
		return unsetOrganisation();
	}
}
