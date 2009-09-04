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

import java.util.Date;
import java.util.LinkedList;
import java.util.List;

/**
 * @author Andreas Dr&auml;ger <a
 *         href="mailto:andreas.draeger@uni-tuebingen.de">
 *         andreas.draeger@uni-tuebingen.de</a>
 * @date 2009-09-04
 */
public class ModelHistory {
	private LinkedList<ModelCreator> listOfModelCreators;
	private LinkedList<Date> listOfModification;
	private Date creation;
	private Date modifyed;

	/**
	 * 
	 */
	public ModelHistory() {
		listOfModelCreators = new LinkedList<ModelCreator>();
		listOfModification = new LinkedList<Date>();
		creation = null;
		modifyed = null;
	}

	/**
	 * 
	 * @param modelHistory
	 */
	public ModelHistory(ModelHistory modelHistory) {
		listOfModelCreators = new LinkedList<ModelCreator>();
		listOfModelCreators.addAll(modelHistory.getListCreators());
		listOfModification = new LinkedList<Date>();
		listOfModification.addAll(modelHistory.getListModifiedDates());
		creation = (Date) modelHistory.getCreatedDate().clone();
		modifyed = (Date) modelHistory.getModifiedDate().clone();
	}

	/**
	 * 
	 * @param mc
	 * @return
	 */
	public int addCreator(ModelCreator mc) {
		listOfModelCreators.add(mc);
		// TODO
		return 0;

	}

	/**
	 * 
	 * @param date
	 * @return
	 */
	public int addModifiedDate(Date date) {
		setModifiedDate(date);
		// TODO
		return 0;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#clone()
	 */
	public ModelHistory clone() {
		return new ModelHistory(this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object o) {
		if (o instanceof ModelHistory) {
			ModelHistory mh = (ModelHistory) o;
			return listOfModelCreators.equals(mh.getListCreators())
					&& listOfModification.equals(mh.getListModifiedDates());
		}
		return false;
	}

	/**
	 * Returns the createdDate from the ModelHistory.
	 * 
	 * @return Date object representing the createdDate from the ModelHistory.
	 */
	public Date getCreatedDate() {
		return creation;
	}

	/**
	 * Get the nth ModelCreator object in this ModelHistory.
	 * 
	 * @param i
	 * @return the nth ModelCreator of this ModelHistory.
	 */
	public ModelCreator getCreator(int i) {
		return listOfModelCreators.get(i);
	}

	/**
	 * Get the list of ModelCreator objects in this ModelHistory.
	 * 
	 * @return the list of ModelCreators for this ModelHistory.
	 */
	public List<ModelCreator> getListCreators() {
		return listOfModelCreators;
	}

	/**
	 * Get the list of ModifiedDate objects in this ModelHistory.
	 * 
	 * @return the list of ModifiedDates for this ModelHistory.
	 */
	public List<Date> getListModifiedDates() {
		return listOfModification;
	}

	/**
	 * Returns the modifiedDate from the ModelHistory.
	 * 
	 * @return Date object representing the modifiedDate from the ModelHistory.
	 */
	public Date getModifiedDate() {
		return listOfModification.getLast();
	}

	/**
	 * Get the nth Date object in the list of ModifiedDates in this
	 * ModelHistory.
	 * 
	 * @param n
	 *            the nth Date in the list of ModifiedDates of this
	 *            ModelHistory.
	 * @return
	 */
	public Date getModifiedDate(int n) {
		return listOfModification.get(n);
	}

	/**
	 * Get the number of ModelCreator objects in this ModelHistory.
	 * 
	 * @return the number of ModelCreators in this ModelHistory.
	 */
	public int getNumCreators() {
		return listOfModelCreators.size();
	}

	/**
	 * Get the number of ModifiedDate objects in this ModelHistory.
	 * 
	 * @return the number of ModifiedDates in this ModelHistory.
	 */
	public int getNumModifiedDates() {
		return listOfModification.size();
	}

	/**
	 * Predicate returning true or false depending on whether this
	 * ModelHistory's createdDate has been set.
	 * 
	 * @return true if the createdDate of this ModelHistory has been set, false
	 *         otherwise.
	 */
	public boolean isSetCreatedDate() {
		return creation != null;
	}

	/**
	 * Predicate returning true or false depending on whether this
	 * ModelHistory's modifiedDate has been set.
	 * 
	 * @return true if the modifiedDate of this ModelHistory has been set, false
	 *         otherwise.
	 */
	public boolean isSetModifiedDate() {
		return modifyed != null;
	}

	/**
	 * Sets the createdDate.
	 * 
	 * @param date
	 *            a Date object representing the date the ModelHistory was
	 *            created.
	 * @return integer value indicating success/failure of the function. The
	 *         possible values returned by this function are:
	 *         LIBSBML_OPERATION_SUCCESS or LIBSBML_INVALID_OBJECT
	 */
	public int setCreatedDate(Date date) {
		// TODO
		creation = date;
		return 0;
	}

	/**
	 * Sets the modifiedDate.
	 * 
	 * @param date
	 *            a Date object representing the date the ModelHistory was
	 *            modified.
	 * @return integer value indicating success/failure of the function. The
	 *         possible values returned by this function are:
	 *         LIBSBML_OPERATION_SUCCESS or LIBSBML_INVALID_OBJECT
	 */
	public int setModifiedDate(Date date) {
		if (isSetModifiedDate())
			listOfModification.add(getModifiedDate());
		modifyed = date;
		// TODO
		return 0;
	}

}
