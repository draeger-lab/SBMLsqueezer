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

import java.util.List;

import org.sbml.squeezer.io.SBaseChangedListener;

/**
 * @author Andreas Dr&auml;ger <a
 *         href="mailto:andreas.draeger@uni-tuebingen.de">
 *         andreas.draeger@uni-tuebingen.de</a>
 * @date 2009-08-31
 */
public interface SBase {

	/**
	 * adds a listener to the SBase object. from now on changes will be saved
	 * 
	 * @param l
	 */
	public void addChangeListener(SBaseChangedListener l);

	/**
	 * Adds a copy of the given CVTerm to this SBML object.
	 * 
	 * @param term
	 * @return
	 */
	public boolean addCVTerm(CVTerm term);

	/**
	 * 
	 * @param notes
	 * @return
	 */
	public void appendNotes(String notes);

	/**
	 * Returns the content of the 'annotation' subelement of this object as a
	 * character string.
	 * 
	 * @return
	 */
	public String getAnnotationString();

	/**
	 * Returns the ith CVTerm in the list of CVTerms of this SBML object.
	 * 
	 * @param i
	 * @return
	 */
	public CVTerm getCVTerm(int i);

	/**
	 * Returns a list of CVTerm objects in the annotations of this SBML object.
	 * 
	 * @return
	 */
	public List<CVTerm> getCVTerms();

	/**
	 * Returns the XML element name of this object.
	 * 
	 * @return
	 */
	public String getElementName();

	/**
	 * Returns the SBML Level of the overall SBML document.
	 * 
	 * @return the SBML level of this SBML object.
	 * @see getVersion()
	 */
	public int getLevel();

	/**
	 * 
	 * @return
	 */
	public String getMetaId();

	/**
	 * Returns the Model object in which the current object is located.
	 * 
	 * @return
	 */
	public Model getModel();

	/**
	 * 
	 * @return
	 */
	public String getNotesString();

	/**
	 * Returns the number of CVTerm objects in the annotations of this SBML
	 * object.
	 * 
	 * @return
	 */
	public int getNumCVTerms();

	/**
	 * This method is convenient when holding an object nested inside other
	 * objects in an SBML model. It allows direct access to the &lt;model&gt;
	 * 
	 * element containing it.
	 * 
	 * @return Returns the parent SBML object.
	 */
	public SBase getParentSBMLObject();

	/**
	 * Returns the parent SBMLDocument object.
	 * 
	 * LibSBML uses the class SBMLDocument as a top-level container for storing
	 * SBML content and data associated with it (such as warnings and error
	 * messages). An SBML model in libSBML is contained inside an SBMLDocument
	 * object. SBMLDocument corresponds roughly to the class Sbml defined in the
	 * SBML Level 2 specification, but it does not have a direct correspondence
	 * in SBML Level 1. (But, it is created by libSBML no matter whether the
	 * model is Level 1 or Level 2.)
	 * 
	 * This method allows the SBMLDocument for the current object to be
	 * retrieved.
	 * 
	 * @return the parent SBMLDocument object of this SBML object.
	 */
	public SBMLDocument getSBMLDocument();

	/**
	 * 
	 * @return
	 */
	public int getSBOTerm();

	/**
	 * 
	 * @return
	 */
	public String getSBOTermID();

	/**
	 * Returns the Version within the SBML Level of the overall SBML document.
	 * 
	 * @return the SBML version of this SBML object.
	 * @see getLevel()
	 */
	public int getVersion();

	/**
	 * Predicate returning true or false depending on whether this object's
	 * level/version and namespace values correspond to a valid SBML
	 * specification.
	 * 
	 * @return
	 */
	public boolean hasValidLevelVersionNamespaceCombination();

	/**
	 * Predicate returning true or false depending on whether this object's
	 * 'annotation' subelement exists and has content.
	 */
	public boolean isSetAnnotation();

	/**
	 * Predicate returning true or false depending on whether this object's
	 * 'metaid' attribute has been set.
	 * 
	 * @return
	 */
	public boolean isSetMetaId();

	/**
	 * Predicate returning true or false depending on whether this object's
	 * 'notes' subelement exists and has content.
	 * 
	 * @return
	 */
	public boolean isSetNotes();

	/**
	 * 
	 * @return
	 */
	public boolean isSetSBOTerm();

	/**
	 * all listeners are informed about the adding of this object to a list
	 * 
	 */
	public void sbaseAdded();

	/**
	 * 
	 * all listeners are informed about the deletion of this object from a list
	 */
	public void sbaseRemoved();

	/**
	 * Sets the value of the 'annotation' subelement of this SBML object to a
	 * copy of annotation given as a character string.
	 * 
	 * @param annotation
	 */
	public void setAnnotation(String annotation);

	/**
	 * 
	 * @param metaid
	 */
	public void setMetaId(String metaid);

	/**
	 * 
	 * @param notes
	 */
	public void setNotes(String notes);

	/**
	 * Sets the value of the 'sboTerm' attribute.
	 * 
	 * @param term
	 */
	public void setSBOTerm(int term);

	/**
	 * Sets the value of the 'sboTerm' attribute.
	 * 
	 * @param sboid
	 */
	public void setSBOTerm(String sboid);

	/**
	 * all listeners are informed about the change in this object
	 */
	public void stateChanged();

	/**
	 * Unsets the value of the 'annotation' subelement of this SBML object.
	 */
	public void unsetAnnotation();

	/**
	 * Clears the list of CVTerms of this SBML object.
	 */
	public void unsetCVTerms();

	/**
	 * Unsets the value of the 'metaid' attribute of this SBML object.
	 */
	public void unsetMetaId();

	/**
	 * Unsets the value of the 'notes' subelement of this SBML object.
	 */
	public void unsetNotes();

	/**
	 * Unsets the value of the 'sboTerm' attribute of this SBML object.
	 */
	public void unsetSBOTerm();

}
