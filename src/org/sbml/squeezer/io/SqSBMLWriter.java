/*
 * $Id$
 * $URL$
 * ---------------------------------------------------------------------
 * This file is part of SBMLsqueezer, a Java program that creates rate 
 * equations for reactions in SBML files (http://sbml.org).
 *
 * Copyright (C) 2006-2012 by the University of Tuebingen, Germany.
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

import java.io.IOException;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.xml.stream.XMLStreamException;

import org.sbml.jsbml.Model;
import org.sbml.jsbml.Reaction;
import org.sbml.jsbml.SBMLDocument;
import org.sbml.jsbml.SBMLException;
import org.sbml.jsbml.SBMLOutputConverter;
import org.sbml.jsbml.SBMLWriter;
import org.sbml.jsbml.util.IOProgressListener;
import org.sbml.squeezer.util.Bundles;

/**
 * This class is a libSBML independent converter for JSBML models.
 * 
 * @author Sarah R. M&uuml;ller vom Hagen
 * @date Aug 9, 2011
 * @since 1.4
 * @version $Rev$
 */
public class SqSBMLWriter implements SBMLOutputConverter{

	private Set<IOProgressListener> setOfIOListeners = new HashSet<IOProgressListener>();
	
	public void addIOProgressListener(IOProgressListener listener) {
		setOfIOListeners.add(listener);
	}

	public int getNumErrors(Object sbase) {
		return 0;
	}

	public List<SBMLException> getWriteWarnings(Object sbase) {
		List<SBMLException> excl = new LinkedList<SBMLException>();
		return excl;
	}

	public void removeUnneccessaryElements(Model model, Object orig) {
	}

	/* (non-Javadoc)
	 * @see org.sbml.jsbml.SBMLOutputConverter#saveChanges(org.sbml.jsbml.Model, java.lang.Object)
	 */
	public boolean saveChanges(Model model, Object object) throws SBMLException {
		if (!(object instanceof Model))
			throw new IllegalArgumentException(Bundles.WARNINGS.getString("NO_JSBML_MODEL"));
		return true;
	}

	/* (non-Javadoc)
	 * @see org.sbml.jsbml.SBMLOutputConverter#saveChanges(org.sbml.jsbml.Reaction, java.lang.Object)
	 */
	public boolean saveChanges(Reaction reaction, Object model)
			throws SBMLException {
		if (!(model instanceof Model)) {
			throw new IllegalArgumentException(Bundles.WARNINGS.getString("NO_JSBML_MODEL"));
		}
		return true;
	}

	public Object writeModel(Model model) throws SBMLException {
		// TODO Auto-generated method stub
		Model m = new Model(model);
		return m;
	}

	public boolean writeSBML(Object sbmlDocument, String filename)
			throws SBMLException, IOException {
		return writeSBML(sbmlDocument, filename, null, null);
	}

	public boolean writeSBML(Object object, String filename,
			String programName, String versionNumber) throws SBMLException,
			IOException {
		// check arguments
		if (!(object instanceof SBMLDocument) && !(object instanceof Model)) {
			throw new IllegalArgumentException(Bundles.WARNINGS.getString("NO_JSBML_MODEL_OR_SBMLDOCUMENT"));
		}
		// convert to SBML
		SBMLDocument sbmlDocument;
		if (object instanceof SBMLDocument) {
			sbmlDocument = (SBMLDocument) object;
		} else {
			sbmlDocument = ((Model) object).getSBMLDocument();
		}
		// write SBML to file
		boolean success = true; 
		try {
			SBMLWriter.write(sbmlDocument, filename, programName, versionNumber);
		} catch (XMLStreamException e) {
			success = false;
		}
		return success;
	}

}
