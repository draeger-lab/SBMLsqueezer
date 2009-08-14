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

import java.util.HashSet;
import java.util.Set;

import org.sbml.squeezer.io.SBaseChangedListener;

/**
 * 
 * @author Andreas Dr&auml;ger <a
 *         href="mailto:andreas.draeger@uni-tuebingen.de">
 *         andreas.draeger@uni-tuebingen.de</a>
 * 
 */
public abstract class SBase {

	SBase parentSBMLObject;
	private String metaid;
	private int SBOTerm;
	private String notes;
	private Set<SBaseChangedListener> setOfListeners;

	public SBase() {
		parentSBMLObject = null;
		setOfListeners = new HashSet<SBaseChangedListener>();
	}

	public SBase(SBase sb) {
		this();
		if (sb.isSetMetaId())
			this.metaid = new String(sb.getMetaid());
		if (sb.isSetNotes())
			this.notes = new String(sb.getNotes());
	}

	/**
	 * Predicate returning true or false depending on whether this object's
	 * 'metaid' attribute has been set.
	 * 
	 * @return
	 */
	public boolean isSetMetaId() {
		return metaid != null;
	}

	/**
	 * Predicate returning true or false depending on whether this object's
	 * 'notes' subelement exists and has content.
	 * 
	 * @return
	 */
	public boolean isSetNotes() {
		return notes != null;
	}

	/**
	 * adds a listener to the SBase object. from now on changes will be saved
	 * 
	 * @param l
	 */
	public void addChangeListener(SBaseChangedListener l) {
		setOfListeners.add(l);
	}

	public String getMetaid() {
		return metaid;
	}

	public String getNotes() {
		return notes;
	}

	public int getSBOTerm() {
		return SBOTerm;
	}

	public String getSBOTermID() {
		StringBuffer sbo = new StringBuffer("SBO:");
		sbo.append(Integer.toString(SBOTerm));
		while (sbo.length() < 11)
			sbo.insert(4, '0');
		return sbo.toString();
	}

	public void removeChangeListener(SBaseChangedListener l) {
		setOfListeners.remove(l);
	}

	public void setMetaid(String metaid) {
		this.metaid = metaid;
		stateChanged();
	}

	public void setNotes(String notes) {
		this.notes = notes;
		stateChanged();
	}

	public void setSBOTerm(int term) {
		SBOTerm = term;
		stateChanged();
	}

	/**
	 * all listeners are informed about the change in this object
	 */
	public void stateChanged() {
		for (SBaseChangedListener listener : setOfListeners)
			listener.stateChanged(this);
	}

	/**
	 * all listeners are informed about the adding of this object to a list
	 * 
	 */

	public void sbaseAdded() {
		for (SBaseChangedListener listener : setOfListeners)
			listener.sbaseAdded(this);
	}

	/**
	 * 
	 * all listeners are informed about the deletion of this object from a list
	 */
	public void sbaseRemoved() {
		for (SBaseChangedListener listener : setOfListeners)
			listener.stateChanged(this);
	}

	/**
	 * This method is convenient when holding an object nested inside other
	 * objects in an SBML model. It allows direct access to the &lt;model&gt;
	 * 
	 * element containing it.
	 * 
	 * @return Returns the parent SBML object.
	 */
	public SBase getParentSBMLObject() {
		return parentSBMLObject;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#clone()
	 */
	public abstract SBase clone();

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public abstract boolean equals(Object o);

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	public abstract String toString();
}
