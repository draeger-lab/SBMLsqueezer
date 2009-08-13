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

import java.util.List;

import org.sbml.io.SBaseChangedListener;

/**
 * 
 * @author Andreas Dr&auml;ger <a
 *         href="mailto:andreas.draeger@uni-tuebingen.de">
 *         andreas.draeger@uni-tuebingen.de</a>
 * 
 */
public abstract class SBase {

	private String metaid;
	private int SBOTerm;
	private String notes;
	private List<SBaseChangedListener> listOfListeners;

	public SBase() {
	}

	/**
	 * adds a listener to the SBase object. from now on changes will be saved
	 * 
	 * @param l
	 */
	public void addChangeListener(SBaseChangedListener l) {
		listOfListeners.add(l);
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
		listOfListeners.remove(l);
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
		for (SBaseChangedListener listener : listOfListeners)
			listener.stateChanged(this);
	}

	/**
	 * all listeners are informed about the adding of this object to a list
	 * 
	 */

	public void sbaseAdded() {
		for (SBaseChangedListener listener : listOfListeners)
			listener.sbaseAdded(this);
	}

	/**
	 * 
	 * all listeners are informed about the deletion of this object from a list
	 */
	public void sbaseRemoved() {
		for (SBaseChangedListener listener : listOfListeners)
			listener.stateChanged(this);
	}
}
