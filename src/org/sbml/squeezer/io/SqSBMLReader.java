/*
 * $Id$
 * $URL$
 * ---------------------------------------------------------------------
 * This file is part of SBMLsqueezer, a Java program that creates rate 
 * equations for reactions in SBML files (http://sbml.org).
 *
 * Copyright (C) 2006-2011 by the University of Tuebingen, Germany.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 * ---------------------------------------------------------------------
 */
package org.sbml.squeezer.io;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

import javax.xml.stream.XMLStreamException;

import org.sbml.jsbml.Model;
import org.sbml.jsbml.SBMLDocument;
import org.sbml.jsbml.SBMLException;
import org.sbml.jsbml.SBMLInputConverter;
import org.sbml.jsbml.SBMLReader;
import org.sbml.jsbml.util.IOProgressListener;
import org.sbml.jsbml.util.TreeNodeChangeListener;

/**
 * This class provides methods to create JSBML models independently from libSBML.
 * 
 * @author Sarah R. M&uuml;ller vom Hagen
 * @date Aug 9, 2011
 * @since 1.4
 * @version $Rev$
 */
public class SqSBMLReader implements SBMLInputConverter {

	/**
	 * working copy of the original JSBL model
	 */
	Model model;
	/**
	 * original JSBML model
	 */
	Model originalModel;

	private HashSet<SBMLDocument> setOfDocuments;
	
	private HashSet<IOProgressListener> setOfIOListeners;
	
	private LinkedList<TreeNodeChangeListener> listOfTreeNodeChangeListeners;

	
	/**
	 * 
	 */
	public SqSBMLReader() {
		listOfTreeNodeChangeListeners = new LinkedList<TreeNodeChangeListener>();
		setOfDocuments = new HashSet<SBMLDocument>();
		setOfIOListeners = new HashSet<IOProgressListener>();
	}

	/**
	 * 
	 * @param model
	 * @throws Exception
	 */
	public SqSBMLReader(Object model) throws Exception {
		this();
		this.model = convertModel(model);
	}

	public void addIOProgressListener(IOProgressListener listener) {
		setOfIOListeners.add(listener);

	}

	public Model convertModel(Object model) throws IllegalArgumentException, IOException, XMLStreamException {
		if (model instanceof String){
			// file name or XML given; create SBML Document
			SBMLDocument doc = model2SBML(model.toString());
			// construct and return model
			return readModelFromSBML(doc);
		} else if (model instanceof Model){
			// JSBML model given; return model
			return (Model) model;
		} else if (model instanceof SBMLDocument){
			// SBMLDocument given; construct and return model
			return readModelFromSBML((SBMLDocument) model);
		} else {
			throw new IllegalArgumentException("model must be an instance of java.lang.String, org.sbml.jsbml.Model or org.sbml.jsbml.SBMLDocument");
		}
	}

	/**
	 * 
	 * @param model			a file name or XML String 
	 * @return				the corresponding sBML document
	 * @throws IOException
	 * @throws XMLStreamException 
	 */
	private org.sbml.jsbml.SBMLDocument model2SBML(String model)
	throws IOException, XMLStreamException {
		File file = new File(model.toString());
		SBMLDocument doc = null;
		if (!file.exists() || !file.isFile() || !file.canRead()) {
			// XML
			doc = SBMLReader.read((String) model);
		} else {
			// File name
			doc = SBMLReader.read(file);
		}
		setOfDocuments.add(doc);
		return doc;
	}

	/**
	 * 
	 * @param sbmldoc
	 * @param model
	 * @return
	 */
	private Model readModelFromSBML(SBMLDocument sbmldoc) {
		// set original model
		this.originalModel = sbmldoc.getModel();
		// We can directly work with the original model, no copy needed here:
		this.model = this.originalModel;
		// add all SBaseChangeListeners to model
    if (model != null) {
      this.model.addAllChangeListeners(listOfTreeNodeChangeListeners);
    }
		// return working copy
		return this.model;
	}
	
	public int getNumErrors() {
		return 0;
	}

	public Object getOriginalModel() {
		return originalModel;
	}

	public List<SBMLException> getWarnings() {
		List<SBMLException> excl = new LinkedList<SBMLException>();
		return excl;
	}

}
