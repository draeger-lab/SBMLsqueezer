/*
 * $Id$
 * $URL$
 * ---------------------------------------------------------------------
 * This file is part of SBMLsqueezer, a Java program that creates rate
 * equations for reactions in SBML files (http://sbml.org).
 *
 * Copyright (C) 2006-2014 by the University of Tuebingen, Germany.
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
import java.util.LinkedList;
import java.util.List;
import java.util.ResourceBundle;

import javax.xml.stream.XMLStreamException;

import org.sbml.jsbml.Model;
import org.sbml.jsbml.SBMLDocument;
import org.sbml.jsbml.SBMLException;
import org.sbml.jsbml.SBMLOutputConverter;
import org.sbml.jsbml.SBMLWriter;
import org.sbml.squeezer.util.Bundles;

import de.zbit.util.ResourceManager;

/**
 * This class is a libSBML independent converter for JSBML models.
 * 
 * @author Sarah R. M&uuml;ller vom Hagen
 * @date Aug 9, 2011
 * @since 1.4
 * @version $Rev$
 */
public class SqSBMLWriter implements SBMLOutputConverter<Model> {
  
  /**
   * 
   */
  public static final transient ResourceBundle WARNINGS = ResourceManager.getBundle(Bundles.WARNINGS);
  
  /* (non-Javadoc)
   * @see org.sbml.jsbml.SBMLOutputConverter#getErrorCount(java.lang.Object)
   */
  public int getErrorCount(Object sbase) {
    return 0;
  }
  
  /* (non-Javadoc)
   * @see org.sbml.jsbml.SBMLOutputConverter#getWriteWarnings(java.lang.Object)
   */
  @Override
  public List<SBMLException> getWriteWarnings(Model sbase) {
    List<SBMLException> excl = new LinkedList<SBMLException>();
    return excl;
  }
  
  /* (non-Javadoc)
   * @see org.sbml.jsbml.SBMLOutputConverter#writeSBML(java.lang.Object, java.lang.String)
   */
  @Override
  public boolean writeSBML(Model sbmlDocument, String filename)
      throws SBMLException, IOException {
    return writeSBML(sbmlDocument, filename, null, null);
  }
  
  /* (non-Javadoc)
   * @see org.sbml.jsbml.SBMLOutputConverter#writeSBML(java.lang.Object, java.lang.String, java.lang.String, java.lang.String)
   */
  @Override
  public boolean writeSBML(Model object, String filename,
    String programName, String versionNumber) throws SBMLException,
    IOException {
    
    // convert to SBML
    SBMLDocument sbmlDocument = object.getSBMLDocument();
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
