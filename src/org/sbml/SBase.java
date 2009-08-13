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

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

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
	private List<ChangeListener> listOfListeners;

	public SBase() {
	}

	public void addChangeListener(ChangeListener l) {
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

	public void removeChangeListener(ChangeListener l) {
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

	public void stateChanged() {
		for (ChangeListener listener : listOfListeners)
			listener.stateChanged(new ChangeEvent(this));
	}
}
