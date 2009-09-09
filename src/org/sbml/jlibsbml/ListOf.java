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
package org.sbml.jlibsbml;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.sbml.squeezer.io.SBaseChangedListener;

/**
 * This list implementation is a java LinkedList that is however restricted to
 * generic types that implement the SBase interface and conatains all methods
 * from AbstractSBase. Unfortunately, there is no way for multiple inheritance
 * in Java.
 * 
 * @author Andreas Dr&auml;ger <a
 *         href="mailto:andreas.draeger@uni-tuebingen.de">
 *         andreas.draeger@uni-tuebingen.de</a>
 * 
 */
public class ListOf<E extends SBase> extends LinkedList<E> implements SBase {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5588467260915307797L;

	SBase parentSBMLObject;

	Set<SBaseChangedListener> setOfListeners;
	
	private int level;

	private int version;

	private int sboTerm;

	private String metaId;

	private String notes;

	private LinkedList<CVTerm> listOfCVTerms;

	private String annotation;
	
	/**
	 * 
	 */
	public ListOf(int level, int version) {
		super();
		sboTerm = -1;
		metaId = null;
		notes = null;
		parentSBMLObject = null;
		setOfListeners = new HashSet<SBaseChangedListener>();
		this.level = level;
		this.version = version;
		this.listOfCVTerms = new LinkedList<CVTerm>();
	}
	
	/**
	 * 
	 * @param listOf
	 */
	public ListOf(ListOf<? extends E> listOf) {
		super(listOf);
		if (listOf.isSetSBOTerm())
			this.sboTerm = listOf.getSBOTerm();
		if (listOf.isSetMetaId())
			this.metaId = new String(listOf.getMetaId());
		if (listOf.isSetNotes())
			this.notes = new String(listOf.getNotesString());
		this.parentSBMLObject = listOf.getParentSBMLObject();
		this.setOfListeners = new HashSet<SBaseChangedListener>();
		this.setOfListeners.addAll(listOf.setOfListeners);
		this.level = listOf.getLevel();
		this.version = listOf.getVersion();
		this.listOfCVTerms = new LinkedList<CVTerm>();
		for (CVTerm cvt : listOf.getCVTerms())
			this.listOfCVTerms.add(cvt.clone());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seeorg.sbml.jlibsbml.SBase#addChangeListener(org.sbml.squeezer.io.
	 * SBaseChangedListener)
	 */
	public void addChangeListener(SBaseChangedListener l) {
		setOfListeners.add(l);
		for (int i = 0; i < this.size(); i++)
			((SBase) get(i)).addChangeListener(l);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.jlibsbml.SBase#addCVTerm(org.sbml.jlibsbml.CVTerm)
	 */
	public boolean addCVTerm(CVTerm term) {
		return listOfCVTerms.add(term);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.jlibsbml.SBase#appendNotes(java.lang.String)
	 */
	public void appendNotes(String notes) {
		if (isSetNotes()) {
			this.notes = this.notes.trim();
			boolean body = false;
			if (this.notes.endsWith("\n"))
				this.notes = this.notes.substring(0, this.notes.length() - 2);
			if (this.notes.endsWith("</notes>"))
				this.notes = this.notes.substring(0, this.notes.length() - 9);
			if (this.notes.endsWith("</body>")) {
				body = true;
				this.notes = this.notes.substring(0, this.notes.length() - 8);
			}
			this.notes += notes;
			if (body)
				this.notes += "</body>";
			this.notes += "</notes>";
		} else
			this.notes = notes;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.LinkedList#clone()
	 */
	// @Override
	public ListOf<E> clone() {
		return new ListOf<E>(this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	// @Override
	public boolean equals(Object o) {
		if (o instanceof ListOf<?>) {
			boolean equals = super.equals(o);
			SBase sbase = (SBase) o;
			equals &= sbase.isSetMetaId() == isSetMetaId();
			if (sbase.isSetMetaId() && isSetMetaId())
				equals &= sbase.getMetaId().equals(getMetaId());
			equals &= sbase.isSetNotes() == isSetNotes();
			if (sbase.isSetNotes() && isSetNotes())
				equals &= sbase.getNotesString().equals(getNotesString());
			equals &= sbase.isSetSBOTerm() == isSetSBOTerm();
			if (sbase.isSetSBOTerm() && isSetSBOTerm())
				equals &= sbase.getSBOTerm() == getSBOTerm();
			equals &= sbase.getLevel() == getLevel();
			equals &= sbase.getVersion() == getVersion();
			equals &= sbase.getCVTerms().equals(getCVTerms());
			return equals;
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.jlibsbml.SBase#getAnnotationString()
	 */
	public String getAnnotationString() {
		return isSetAnnotation() ? annotation : "";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.jlibsbml.SBase#getCVTerm(int)
	 */
	public CVTerm getCVTerm(int i) {
		return listOfCVTerms.get(i);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.jlibsbml.SBase#getCVTerms()
	 */
	public List<CVTerm> getCVTerms() {
		return listOfCVTerms;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.jlibsbml.SBase#getElementName()
	 */
	public String getElementName() {
		String name = getClass().getCanonicalName();
		char c = Character.toLowerCase(name.charAt(0));
		return Character.toString(c) + name.substring(1);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.jlibsbml.SBase#getLevel()
	 */
	public int getLevel() {
		return level;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.SBase#getMetaId()
	 */
	public String getMetaId() {
		return metaId;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.SBase#getModel()
	 */
	public Model getModel() {
		if (getParentSBMLObject() != null)
			return getParentSBMLObject().getModel();
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.SBase#getNotesString()
	 */
	public String getNotesString() {
		return notes != null ? notes : "";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.jlibsbml.SBase#getNumCVTerms()
	 */
	public int getNumCVTerms() {
		return listOfCVTerms.size();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.SBase#getParentSBMLObject()
	 */
	public SBase getParentSBMLObject() {
		return parentSBMLObject;
	}

	/*
	 * (non-Javadoc)
	 * @see org.sbml.jlibsbml.SBase#getSBMLDocument()
	 */
	public SBMLDocument getSBMLDocument() {
		Model m = getModel();
		if (m != null)
			return m.getParentSBMLObject();
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.SBase#getSBOTerm()
	 */
	public int getSBOTerm() {
		return sboTerm;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.SBase#getSBOTermID()
	 */
	public String getSBOTermID() {
		return SBO.intToString(sboTerm);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.jlibsbml.SBase#getVersion()
	 */
	public int getVersion() {
		return version;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.jlibsbml.SBase#hasValidLevelVersionNamespaceCombination()
	 */
	public boolean hasValidLevelVersionNamespaceCombination() {
		boolean has = true;
		if (level == 1) {
			if (1 <= version && version <= 2)
				has = true;
			else
				has = false;
		} else if (level == 2) {
			if (1 <= version && version <= 4)
				has = true;
			else
				has = false;
		} else
			has = false;
		return has;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.jlibsbml.SBase#isSetAnnotation()
	 */
	public boolean isSetAnnotation() {
		return annotation != null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.SBase#isSetMetaId()
	 */
	public boolean isSetMetaId() {
		return metaId != null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.SBase#isSetNotes()
	 */
	public boolean isSetNotes() {
		return notes != null;
	}

	/*
	 * (non-Javadoc)
	 * 
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
	 * 
	 * @see org.sbml.SBase#sbaseAdded()
	 */
	public void sbaseAdded() {
		for (SBaseChangedListener listener : setOfListeners)
			listener.sbaseAdded(this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.SBase#sbaseRemoved()
	 */
	public void sbaseRemoved() {
		for (SBaseChangedListener listener : setOfListeners)
			listener.stateChanged(this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.jlibsbml.SBase#setAnnotation(java.lang.String)
	 */
	public void setAnnotation(String annotation) {
		this.annotation = annotation;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.SBase#setMetaId(java.lang.String)
	 */
	public void setMetaId(String metaid) {
		this.metaId = metaid;
		stateChanged();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.SBase#setNotes(java.lang.String)
	 */
	public void setNotes(String notes) {
		this.notes = notes;
		stateChanged();
	}

	/*
	 * (non-Javadoc)
	 * 
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
	 * 
	 * @see org.sbml.jlibsbml.SBase#setSBOTerm(java.lang.String)
	 */
	public void setSBOTerm(String sboid) {
		setSBOTerm(SBO.stringToInt(sboid));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.SBase#stateChanged()
	 */
	public void stateChanged() {
		for (SBaseChangedListener listener : setOfListeners)
			listener.stateChanged(this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.jlibsbml.SBase#unsetAnnotation()
	 */
	public void unsetAnnotation() {
		if (isSetAnnotation())
			annotation = null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.jlibsbml.SBase#unsetCVTerms()
	 */
	public void unsetCVTerms() {
		listOfCVTerms.clear();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.jlibsbml.SBase#unsetMetaId()
	 */
	public void unsetMetaId() {
		if (isSetMetaId())
			metaId = null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.jlibsbml.SBase#unsetNotes()
	 */
	public void unsetNotes() {
		if (isSetNotes())
			notes = null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.jlibsbml.SBase#unsetSBOTerm()
	 */
	public void unsetSBOTerm() {
		sboTerm = -1;
	}

}
