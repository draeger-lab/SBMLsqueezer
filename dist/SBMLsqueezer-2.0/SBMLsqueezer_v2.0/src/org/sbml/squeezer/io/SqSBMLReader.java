/*

 * $Id: SqSBMLReader.java 1082 2014-02-22 23:54:18Z draeger $
 * $URL: https://rarepos.cs.uni-tuebingen.de/svn-path/SBMLsqueezer/trunk/src/org/sbml/squeezer/io/SqSBMLReader.java $
 * ---------------------------------------------------------------------
 * This file is part of SBMLsqueezer, a Java program that creates rate
 * equations for reactions in SBML files (http://sbml.org).
 *
 * Copyright (C) 2006-2015 by the University of Tuebingen, Germany.
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
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.ResourceBundle;

import org.sbml.jsbml.Model;
import org.sbml.jsbml.SBMLDocument;
import org.sbml.jsbml.SBMLException;
import org.sbml.jsbml.SBMLInputConverter;
import org.sbml.jsbml.SBMLReader;
import org.sbml.jsbml.util.ProgressListener;
import org.sbml.jsbml.util.TreeNodeChangeListener;
import org.sbml.squeezer.util.Bundles;

import de.zbit.util.ResourceManager;


/**
 * This class provides methods to create JSBML models independently from libSBML.
 * 
 * @author Sarah R. M&uuml;ller vom Hagen
 * @date Aug 9, 2011
 * @since 2.0
 * @version $Rev: 1082 $
 */
public class SqSBMLReader implements SBMLInputConverter<Model> {
  
  /**
   * Localization support.
   */
  public static final transient ResourceBundle WARNINGS = ResourceManager.getBundle(Bundles.WARNINGS);
  
  /**
   * 
   */
  private LinkedList<TreeNodeChangeListener> listOfTreeNodeChangeListeners;
  
  /**
   * working copy of the original JSBL model
   */
  private Model model;
  
  /**
   * original JSBML model
   */
  private Model originalModel;
  
  /**
   * 
   */
  private HashSet<SBMLDocument> setOfDocuments;
  
  /**
   * 
   */
  public SqSBMLReader() {
    listOfTreeNodeChangeListeners = new LinkedList<TreeNodeChangeListener>();
    setOfDocuments = new HashSet<SBMLDocument>();
  }
  
  /**
   * 
   * @param model
   * @throws Exception
   */
  public SqSBMLReader(Model model) throws Exception {
    this();
    this.model = convertModel(model);
  }
  
  /* (non-Javadoc)
   * @see org.sbml.jsbml.SBMLInputConverter#convertModel(java.lang.Object)
   */
  @Override
  public Model convertModel(Model model) throws Exception {
    // set original model
    originalModel = model;
    // We can directly work with the original model, no copy needed here:
    this.model = originalModel;
    // add all SBaseChangeListeners to model
    if (model != null) {
      this.model.addAllChangeListeners(listOfTreeNodeChangeListeners);
    }
    setOfDocuments.add(model.getSBMLDocument());
    // return working copy
    return this.model;
  }
  
  /* (non-Javadoc)
   * @see org.sbml.jsbml.SBMLInputConverter#convertSBMLDocument(java.io.File)
   */
  @Override
  public SBMLDocument convertSBMLDocument(File sbmlFile) throws Exception {
    return SBMLReader.read(sbmlFile);
  }
  
  /* (non-Javadoc)
   * @see org.sbml.jsbml.SBMLInputConverter#convertSBMLDocument(java.lang.String)
   */
  @Override
  public SBMLDocument convertSBMLDocument(String fileName) throws Exception {
    return convertSBMLDocument(new File(fileName));
  }
  
  /* (non-Javadoc)
   * @see org.sbml.jsbml.SBMLInputConverter#getOriginalModel()
   */
  @Override
  public Model getOriginalModel() {
    return originalModel;
  }
  
  /* (non-Javadoc)
   * @see org.sbml.jsbml.SBMLInputConverter#getWarnings()
   */
  @Override
  public List<SBMLException> getWarnings() {
    return new ArrayList<SBMLException>(0);
  }
  
  /* (non-Javadoc)
   * @see org.sbml.jsbml.SBMLInputConverter#setListener(org.sbml.jsbml.util.ProgressListener)
   */
  @Override
  public void setListener(ProgressListener listener) {
  }
  
}
