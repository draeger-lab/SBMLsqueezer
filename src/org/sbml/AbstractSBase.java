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

import java.util.HashSet;
import java.util.Set;

import org.sbml.squeezer.io.SBaseChangedListener;

/**
 * 
 * @author Andreas Dr&auml;ger <a
 *         href="mailto:andreas.draeger@uni-tuebingen.de">
 *         andreas.draeger@uni-tuebingen.de</a>
 * @date 2009-08-31
 */
public abstract class AbstractSBase implements SBase {

	SBase parentSBMLObject;
	private String metaId;
	private int sboTerm;
	private String notes;
	Set<SBaseChangedListener> setOfListeners;

	/**
	 * 
	 */
	public AbstractSBase() {
		sboTerm = -1;
		metaId = null;
		notes = null;
		parentSBMLObject = null;
		setOfListeners = new HashSet<SBaseChangedListener>();
	}

	/**
	 * 
	 * @param sb
	 */
	public AbstractSBase(AbstractSBase sb) {
		this();
		if (sb.isSetSBOTerm())
			this.sboTerm = sb.getSBOTerm();
		if (sb.isSetMetaId())
			this.metaId = new String(sb.getMetaId());
		if (sb.isSetNotes())
			this.notes = new String(sb.getNotesString());
		this.parentSBMLObject = sb.getParentSBMLObject();
		this.setOfListeners.addAll(sb.setOfListeners);
	}

	/**
	 * adds a listener to the SBase object. from now on changes will be saved
	 * 
	 * @param l
	 */
	public void addChangeListener(SBaseChangedListener l) {
		setOfListeners.add(l);
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
	// @Override
	public boolean equals(Object o) {
		if (o instanceof AbstractSBase) {
			SBase sbase = (SBase) o;
			boolean equals = true;
			if ((!sbase.isSetMetaId() && isSetMetaId())
					|| (sbase.isSetMetaId() && !isSetMetaId()))
				return false;
			else if (sbase.isSetMetaId() && isSetMetaId())
				equals &= sbase.getMetaId().equals(getMetaId());
			if ((!sbase.isSetNotes() && isSetNotes())
					|| (sbase.isSetNotes() && !isSetNotes()))
				return false;
			else if (sbase.isSetNotes() && isSetNotes())
				equals &= sbase.getNotesString().equals(getNotesString());
			if ((!sbase.isSetSBOTerm() && isSetSBOTerm())
					|| (sbase.isSetSBOTerm() && !isSetSBOTerm()))
				return false;
			else if (sbase.isSetSBOTerm() && isSetSBOTerm())
				equals &= sbase.getSBOTerm() == getSBOTerm();
			return equals;
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 * @see org.sbml.SBase#getMetaId()
	 */
	public String getMetaId() {
		return metaId;
	}

	/*
	 * (non-Javadoc)
	 * @see org.sbml.SBase#getModel()
	 */
	public Model getModel() {
		if (this instanceof Model)
			return (Model) this;
		if (getParentSBMLObject() != null)
			return getParentSBMLObject().getModel();
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see org.sbml.SBase#getNotesString()
	 */
	public String getNotesString() {
		return notes != null ? notes : "";
	}

	/*
	 * (non-Javadoc)
	 * @see org.sbml.SBase#getParentSBMLObject()
	 */
	public SBase getParentSBMLObject() {
		return parentSBMLObject;
	}

	/*
	 * (non-Javadoc)
	 * @see org.sbml.SBase#getSBOTerm()
	 */
	public int getSBOTerm() {
		return sboTerm;
	}

	/*
	 * (non-Javadoc)
	 * @see org.sbml.SBase#getSBOTermID()
	 */
	public String getSBOTermID() {
		return SBO.intToString(sboTerm);
	}

	/*
	 * (non-Javadoc)
	 * @see org.sbml.SBase#isSetMetaId()
	 */
	public boolean isSetMetaId() {
		return metaId != null;
	}

	/*
	 * (non-Javadoc)
	 * @see org.sbml.SBase#isSetNotes()
	 */
	public boolean isSetNotes() {
		return notes != null;
	}

	/*
	 * (non-Javadoc)
	 * @see org.sbml.SBase#isSetSBOTerm()
	 */
	public boolean isSetSBOTerm() {
		return sboTerm != -1;
	}

	/**
	 * 
	 * @param l
	 */
	public void removeChangeListener(SBaseChangedListener l) {
		setOfListeners.remove(l);
	}

    /*
     * (non-Javadoc)
     * @see org.sbml.SBase#sbaseAdded()
     */
	public void sbaseAdded() {
		for (SBaseChangedListener listener : setOfListeners)
			listener.sbaseAdded(this);
	}

	/*
	 * (non-Javadoc)
	 * @see org.sbml.SBase#sbaseRemoved()
	 */
	public void sbaseRemoved() {
		for (SBaseChangedListener listener : setOfListeners)
			listener.stateChanged(this);
	}

	/*
	 * (non-Javadoc)
	 * @see org.sbml.SBase#setMetaId(java.lang.String)
	 */
	public void setMetaId(String metaid) {
		this.metaId = metaid;
		stateChanged();
	}

	/*
	 * (non-Javadoc)
	 * @see org.sbml.SBase#setNotes(java.lang.String)
	 */
	public void setNotes(String notes) {
		this.notes = notes;
		stateChanged();
	}

	/*
	 * (non-Javadoc)
	 * @see org.sbml.SBase#setSBOTerm(int)
	 */
	public void setSBOTerm(int term) {
		if (!SBO.checkTerm(term))
			throw new IllegalArgumentException(
					"SBO terms must not be smaller than zero or larger than 9999999.");
		sboTerm = term;
		stateChanged();
	}

	/*
	 * (non-Javadoc)
	 * @see org.sbml.SBase#stateChanged()
	 */
	public void stateChanged() {
		for (SBaseChangedListener listener : setOfListeners)
			listener.stateChanged(this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	// @Override
	public abstract String toString();

	/**
	 * Sets the parent SBML object of the given list and all of its elements to
	 * this object.
	 * 
	 * @param list
	 */
	void setThisAsParentSBMLObject(ListOf<? extends AbstractSBase> list) {
		list.parentSBMLObject = this;
		for (AbstractSBase base : list)
			base.parentSBMLObject = this;
	}
}
