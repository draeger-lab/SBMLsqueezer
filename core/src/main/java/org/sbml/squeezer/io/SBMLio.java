/*
 * $Id$
 * $URL$
 * ---------------------------------------------------------------------
 * This file is part of SBMLsqueezer, a Java program that creates rate
 * equations for reactions in SBML files (http://sbml.org).
 *
 * Copyright (C) 2006-2016 by the University of Tuebingen, Germany.
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

import static de.zbit.util.Utils.getMessage;

import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.LinkedList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.sbml.jsbml.Model;
import org.sbml.jsbml.SBMLDocument;
import org.sbml.jsbml.SBMLException;
import org.sbml.jsbml.SBMLException.Category;
import org.sbml.jsbml.SBMLInputConverter;
import org.sbml.jsbml.SBMLOutputConverter;
import org.sbml.jsbml.util.ProgressListener;
import org.sbml.squeezer.util.Bundles;

import de.zbit.io.OpenedFile;
import de.zbit.sbml.io.SBMLfileChangeListener;
import de.zbit.util.ResourceManager;
import de.zbit.util.progressbar.AbstractProgressBar;

/**
 * A manager class for reading and writing models from some source into JSBML
 * data objects and to synchronize the model with the original data structure
 * afterwards. This class requires an instance of the so called
 * {@link SBMLInputConverter} and {@link SBMLOutputConverter} classes and
 * maintains all loaded original model objects and the JSBML representation of
 * it in designated lists. It can be used to, e.g., load a model from
 * CellDesigner's plug-in data structures and save changes back in CellDesigner
 * or to do the same with models that originate from libSBML.
 * 
 * @author Andreas Dr&auml;ger
 *
 * @since 1.3
 * @param <T> the type of SBML documents that can be treated by this controller.
 */
public class SBMLio<T> implements SBMLInputConverter<T>, SBMLOutputConverter<T>,
ChangeListener {
  
  /**
   * A {@link Logger} for this class.
   */
  private static final transient Logger logger = Logger.getLogger(SBMLio.class.getName());
  
  /**
   * Localization support.
   */
  public static final transient ResourceBundle WARNINGS = ResourceManager.getBundle(Bundles.WARNINGS);
  
  private List<OpenedFile<SBMLDocument>> listOfOpenedFiles;
  
  OpenedFile<SBMLDocument> openedDocument;
  
  Model openedModel;
  
  protected AbstractProgressBar progress;
  private SBMLInputConverter<T> reader;
  private int selectedModel;
  
  private SBMLOutputConverter<T> writer;
  
  /**
   * 
   * @param sbmlReader
   * @param sbmlWriter
   */
  public SBMLio(SBMLInputConverter<T> sbmlReader, SBMLOutputConverter<T> sbmlWriter) {
    this.reader = sbmlReader;
    // this.reader.addSBaseChangeListener(this);
    this.writer = sbmlWriter;
    listOfOpenedFiles = new LinkedList<OpenedFile<SBMLDocument>>();
    selectedModel = -1;
  }
  
  /* (non-Javadoc)
   * @see org.sbml.jsbml.SBMLInputConverter#convertModel(java.lang.Object)
   */
  @Override
  public Model convertModel(T model) throws SBMLException {
    try {
      Model convertedModel = reader.convertModel(model);
      setCurrentModel(null, convertedModel, null);
      return convertedModel;
    } catch (Exception exc) {
      // exc.fillInStackTrace();
      exc.printStackTrace();
      String message = MessageFormat.format(WARNINGS.getString("CANT_READ_MODEL"), model);
      SBMLException sbmlExc = new SBMLException(message, exc);
      logger.log(Level.SEVERE, message, exc);
      sbmlExc.setCategory(Category.XML);
      throw sbmlExc;
    }
  }
  
  /**
   * 
   * @param file
   * @param model
   * @param origModel
   */
  private void setCurrentModel(File file, Model model, T origModel) {
    openedModel = model;
    SBMLDocument doc = model.getSBMLDocument();
    if (doc == null) {
      doc = new SBMLDocument(model.getLevel(), model.getVersion());
      doc.setModel(model);
    }
    openedDocument = new OpenedFile<SBMLDocument>(file, model.getSBMLDocument());
    openedDocument.getDocument().addTreeNodeChangeListener(new SBMLfileChangeListener(openedDocument));
    listOfOpenedFiles.add(openedDocument);
    selectedModel = listOfOpenedFiles.size() - 1;
  }
  
  /* (non-Javadoc)
   * @see org.sbml.jsbml.SBMLInputConverter#convertSBMLDocument(java.io.File)
   */
  @Override
  public SBMLDocument convertSBMLDocument(File sbmlFile) throws Exception {
    SBMLDocument doc = reader.convertSBMLDocument(sbmlFile);
    setCurrentModel(sbmlFile, doc.getModel(), null);
    return doc;
  }
  
  /* (non-Javadoc)
   * @see org.sbml.jsbml.SBMLInputConverter#convertSBMLDocument(java.lang.String)
   */
  @Override
  public SBMLDocument convertSBMLDocument(String fileName) throws Exception {
    SBMLDocument doc = reader.convertSBMLDocument(fileName);
    setCurrentModel(new File(fileName), doc.getModel(), null);
    return doc;
  }
  
  /**
   * 
   * @return
   */
  public List<OpenedFile<SBMLDocument>> getListOfOpenedFiles() {
    return listOfOpenedFiles;
  }
  
  /**
   * 
   * @param index
   * @return
   */
  public OpenedFile<SBMLDocument> getOpenedFile(int index) {
    return listOfOpenedFiles.size() > 0 ? listOfOpenedFiles.get(index) : openedDocument;
  }
  
  /* (non-Javadoc)
   * @see org.sbml.jsbml.SBMLInputConverter#getOriginalModel()
   */
  @Override
  public T getOriginalModel() {
    return reader.getOriginalModel();
  }
  
  /**
   * 
   * @return
   */
  public SBMLInputConverter<T> getReader() {
    return reader;
  }
  
  /**
   * 
   * @return
   */
  public File getSelectedFile() {
    return listOfOpenedFiles.size() > 0 ? listOfOpenedFiles.get(selectedModel).getFile() : null;
  }
  
  /**
   * 
   * @return
   */
  public Model getSelectedModel() {
    return listOfOpenedFiles.size() > 0 ? listOfOpenedFiles.get(selectedModel).getDocument().getModel() : openedModel;
  }
  
  /**
   * 
   * @return
   */
  public OpenedFile<SBMLDocument> getSelectedOpenedFile() {
    return getOpenedFile(selectedModel);
  }
  
  /* (non-Javadoc)
   * @see org.sbml.jsbml.SBMLInputConverter#getWarnings()
   */
  @Override
  public List<SBMLException> getWarnings() {
    return reader.getWarnings();
  }
  
  /**
   * 
   * @return
   */
  @SuppressWarnings("unchecked")
  public List<SBMLException> getWriteWarnings() {
    try {
      return getWriteWarnings((T) listOfOpenedFiles.get(selectedModel).getDocument().getModel());
    } catch(Exception exc) {
      logger.fine(getMessage(exc));
    }
    return getWriteWarnings((T) openedModel);
  }
  
  /* (non-Javadoc)
   * @see org.sbml.jsbml.SBMLOutputConverter#getWriteWarnings(java.lang.Object)
   */
  @Override
  public List<SBMLException> getWriteWarnings(T sbase) {
    return writer.getWriteWarnings(sbase);
  }
  
  /* (non-Javadoc)
   * @see org.sbml.jsbml.SBMLInputConverter#setListener(org.sbml.jsbml.util.ProgressListener)
   */
  @Override
  public void setListener(ProgressListener listener) {
    getReader().setListener(listener);
  }
  
  public void setSelectedFile(int i) {
    selectedModel = i;
  }
  
  /* (non-Javadoc)
   * @see javax.swing.event.ChangeListener#stateChanged(javax.swing.event.ChangeEvent)
   */
  @Override
  public void stateChanged(ChangeEvent e) {
    if (e.getSource() instanceof javax.swing.JTabbedPane) {
      javax.swing.JTabbedPane tabbedPane = (javax.swing.JTabbedPane) e.getSource();
      listOfOpenedFiles.clear();
      if (tabbedPane.getTabCount() > 0) {
        // synchronize the list of opened files with the components in the tabs:
        java.awt.Component comp;
        for (int i = 0; i < tabbedPane.getTabCount(); i++) {
          comp = tabbedPane.getComponentAt(i);
          if (comp instanceof de.zbit.sbml.gui.SBMLModelSplitPane) {
            listOfOpenedFiles.add(((de.zbit.sbml.gui.SBMLModelSplitPane) comp).getOpenedFile());
          } else {
            throw new RuntimeException(MessageFormat.format(WARNINGS.getString("OPERATION_NOT_SUPPORTED"), comp.getClass().getName()));
          }
        }
        selectedModel = tabbedPane.getSelectedIndex();
      }
    }
  }
  
  /* (non-Javadoc)
   * @see org.sbml.jsbml.SBMLOutputConverter#writeSBML(java.lang.Object, java.lang.String)
   */
  @Override
  public boolean writeSBML(T sbmlDocument, String filename)
      throws SBMLException, IOException {
    return writer.writeSBML(sbmlDocument, filename);
  }
  
  /* (non-Javadoc)
   * @see org.sbml.jsbml.SBMLOutputConverter#writeSBML(java.lang.Object, java.lang.String, java.lang.String, java.lang.String)
   */
  @Override
  public boolean writeSBML(T object, String filename,
    String programName, String versionNumber) throws SBMLException,
    IOException {
    return writer.writeSBML(object, filename, programName, versionNumber);
  }
  
  /**
   * 
   * @param filename
   * @return
   * @throws SBMLException
   * @throws IOException
   */
  @SuppressWarnings("unchecked")
  public boolean writeSelectedModelToSBML(String filename)
      throws SBMLException, IOException {
    Model model = listOfOpenedFiles.get(selectedModel).getDocument().getModel();
    return writer.writeSBML(
      (T) model,
      filename,
      System.getProperty("app.name"),
      System.getProperty("app.version")
        );
  }
  
}
