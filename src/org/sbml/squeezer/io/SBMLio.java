/*
 * $Id$
 * $URL$
 * ---------------------------------------------------------------------
 * This file is part of SBMLsqueezer, a Java program that creates rate 
 * equations for reactions in SBML files (http://sbml.org).
 *
 * Copyright (C) 2006-2013 by the University of Tuebingen, Germany.
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

import java.beans.PropertyChangeEvent;
import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.LinkedList;
import java.util.List;
import java.util.ResourceBundle;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.tree.TreeNode;

import org.sbml.jsbml.Model;
import org.sbml.jsbml.Reaction;
import org.sbml.jsbml.SBMLDocument;
import org.sbml.jsbml.SBMLException;
import org.sbml.jsbml.SBMLException.Category;
import org.sbml.jsbml.SBMLInputConverter;
import org.sbml.jsbml.SBMLOutputConverter;
import org.sbml.jsbml.util.IOProgressListener;
import org.sbml.jsbml.util.TreeNodeChangeListener;
import org.sbml.jsbml.util.TreeNodeRemovedEvent;
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
 * @since 1.3
 * @version $Rev$
 */
public class SBMLio implements SBMLInputConverter, SBMLOutputConverter,
		TreeNodeChangeListener, ChangeListener {

	public static final transient ResourceBundle WARNINGS = ResourceManager.getBundle(Bundles.WARNINGS);

	public static final String ORIGINAL_MODEL_KEY = "org.sbml.squeezer.io.SBMLio.originalModelKey";

	private List<TreeNode> added;

	private List<TreeNode> changed;
	
	private LinkedList<OpenedFile<SBMLDocument>> listOfOpenedFiles;

	protected AbstractProgressBar progress;

	private SBMLInputConverter reader;

	private List<TreeNode> removed;

	private int selectedModel;
	
	private SBMLOutputConverter writer;
	
	OpenedFile<SBMLDocument> openedDocument;
	Model openedModel;
	
	/**
	 * 
	 */
	public SBMLio(SBMLInputConverter sbmlReader, SBMLOutputConverter sbmlWriter) {
		this.reader = sbmlReader;
		// this.reader.addSBaseChangeListener(this);
		this.writer = sbmlWriter;
		listOfOpenedFiles = new LinkedList<OpenedFile<SBMLDocument>>();
		selectedModel = -1;
		added = new LinkedList<TreeNode>();
		removed = new LinkedList<TreeNode>();
		changed = new LinkedList<TreeNode>();
	}

	/**
	 * 
	 * @param model
	 * @throws Exception
	 */
	public SBMLio(SBMLInputConverter reader, SBMLOutputConverter writer,
			Object model) throws Exception {
		this(reader, writer);
		Model convertedModel = reader.convertModel(model);
		convertedModel.putUserObject(ORIGINAL_MODEL_KEY, model);
		SBMLDocument sbmlDoc = convertedModel.getSBMLDocument();
		listOfOpenedFiles.addLast(new OpenedFile<SBMLDocument>(sbmlDoc));
	}

	/* (non-Javadoc)
	 * @see org.sbml.jsbml.SBMLInputConverter#addIOProgressListener(org.sbml.jsbml.io.IOProgressListener)
	 */
	public void addIOProgressListener(IOProgressListener listener) {
		reader.addIOProgressListener(listener);
		writer.addIOProgressListener(listener);
	}

	/* (non-Javadoc)
	 * @see org.sbml.jsbml.SBMLInputConverter#convertModel(java.lang.Object)
	 */
	public Model convertModel(Object model) throws SBMLException {
		try {
			Object origModel;
			Model convertedModel = null;
			File file = null;
			convertedModel = reader.convertModel(model);
			if (model instanceof String) {
				file = new File(model.toString());
				if (!file.exists() || !file.isFile() || !file.canRead()) {
					file = null;
				}
				origModel = reader.getOriginalModel();
			} else {
				origModel = model;
			}
			convertedModel.putUserObject(ORIGINAL_MODEL_KEY, origModel);
			openedModel = convertedModel;
			SBMLDocument doc = convertedModel.getSBMLDocument();
			if (doc == null) {
				doc = new SBMLDocument(convertedModel.getLevel(), convertedModel.getVersion());
				doc.setModel(convertedModel);
			}
			openedDocument = new OpenedFile<SBMLDocument>(file, convertedModel.getSBMLDocument());
			openedDocument.getDocument().addTreeNodeChangeListener(new SBMLfileChangeListener(openedDocument));
			listOfOpenedFiles.add(openedDocument);
			selectedModel = listOfOpenedFiles.size() - 1;
			return convertedModel;
		} catch (Exception exc) {
			// exc.fillInStackTrace();
			exc.printStackTrace();
			SBMLException sbmlExc = new SBMLException(
				MessageFormat.format(WARNINGS.getString("CANT_READ_MODEL"), ""),
				exc);
			sbmlExc.setCategory(Category.XML);
			throw sbmlExc;
		}
	}

	public int getErrorCount() {
		return getNumErrors();
	}

	public int getErrorCount(Object sbase) {
		return getNumErrors(sbase);
	}

	/**
	 * 
	 * @return
	 */
	public LinkedList<OpenedFile<SBMLDocument>> getListOfOpenedFiles() {
		return listOfOpenedFiles;
	}
	
	/**
	 * 
	 * @return
	 */
	public OpenedFile<SBMLDocument> getSelectedOpenedFile() {
		return getOpenedFile(selectedModel);
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
	 * @see org.sbml.jsbml.SBMLInputConverter#getNumErrors()
	 */
	public int getNumErrors() {
		return listOfOpenedFiles.size() > 0 ? reader.getErrorCount() : 0;
	}

	/* (non-Javadoc)
	 * @see org.sbml.jsbml.SBMLOutputConverter#getNumErrors(java.lang.Object)
	 */
	public int getNumErrors(Object sbase) {
		return writer.getErrorCount(sbase);
	}

	/* (non-Javadoc)
	 * @see org.sbml.jsbml.SBMLInputConverter#getOriginalModel()
	 */
	public Object getOriginalModel() {
		return reader.getOriginalModel();
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
	public File getSelectedFile() {
		return listOfOpenedFiles.size() > 0 ? listOfOpenedFiles.get(selectedModel).getFile() : null;
	}

	/* (non-Javadoc)
	 * @see org.sbml.jsbml.SBMLInputConverter#getWarnings()
	 */
	public List<SBMLException> getWarnings() {
		return reader.getWarnings();
	}

	/**
	 * 
	 * @return
	 */
	public List<SBMLException> getWriteWarnings() {
		List<SBMLException> warnings = null;
		try{warnings = writer.getWriteWarnings(listOfOpenedFiles.get(selectedModel).getDocument().getModel().getUserObject(ORIGINAL_MODEL_KEY));}
		catch(Exception e){warnings = writer.getWriteWarnings(openedModel.getUserObject(ORIGINAL_MODEL_KEY));}
		return warnings;
	}

	/* (non-Javadoc)
	 * @see org.sbml.jsbml.SBMLOutputConverter#getWriteWarnings(java.lang.Object)
	 */
	public List<SBMLException> getWriteWarnings(Object sbase) {
		return writer.getWriteWarnings(sbase);
	}

	/* (non-Javadoc)
	 * @see org.sbml.jsbml.SBaseChangedListener#sbaseAdded(org.sbml.jsbml.SBase)
	 */
	public void nodeAdded(TreeNode sb) {
		if (!added.contains(sb)) {
			added.add(sb);
		}
	}

	/* (non-Javadoc)
   * @see java.beans.PropertyChangeListener#propertyChange(java.beans.PropertyChangeEvent)
   */
  public void propertyChange(PropertyChangeEvent evt) {
    Object src = evt.getSource();
    if (!changed.contains(src) && (src instanceof TreeNode)) {
      changed.add((TreeNode) src);
    }
  }

	/* (non-Javadoc)
	 * @see org.sbml.jsbml.SBMLOutputConverter#removeUnneccessaryElements(org.sbml.jsbml.Model, java.lang.Object)
	 */
	public void removeUnneccessaryElements(Model model, Object orig) {
		writer.removeUnneccessaryElements(model, orig);
	}

	/**
	 * Write all changes back into the original model.
	 * 
	 * @param listener
	 * 
	 * @throws SBMLException
	 */
	public void saveChanges(IOProgressListener listener) throws SBMLException {
		writer.addIOProgressListener(listener);
		writer.saveChanges(getSelectedModel(), getSelectedModel().getUserObject(ORIGINAL_MODEL_KEY));
		listener.ioProgressOn(null);
	}

	/* (non-Javadoc)
	 * @see org.sbml.jsbml.SBMLOutputConverter#saveChanges(org.sbml.jsbml.Model, java.lang.Object)
	 */
	public boolean saveChanges(Model model, Object object) throws SBMLException {
		return writer.saveChanges(model, object);
	}

	/**
	 * 
	 * @param reaction
	 * @throws SBMLException
	 */
	public boolean saveChanges(Reaction reaction) throws SBMLException {
		return writer.saveChanges(reaction, getSelectedModel().getUserObject(ORIGINAL_MODEL_KEY));
	}

	/* (non-Javadoc)
	 * @see org.sbml.jsbml.SBMLOutputConverter#saveChanges(org.sbml.jsbml.Reaction, java.lang.Object)
	 */
	public boolean saveChanges(Reaction reaction, Object model)
			throws SBMLException {
		return writer.saveChanges(reaction, model);
	}

	/* (non-Javadoc)
	 * @see javax.swing.event.ChangeListener#stateChanged(javax.swing.event.ChangeEvent)
	 */
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
	 * @see org.sbml.jsbml.SBMLOutputConverter#writeModel(org.sbml.jsbml.Model)
	 */
	public Object writeModel(Model model) throws SBMLException {
		return writer.writeModel(model);
	}

	/**
	 * 
	 * @param model
	 * @param filename
	 * @return
	 * @throws SBMLException
	 * @throws IOException
	 */
	public boolean writeModelToSBML(int model, String filename)
			throws SBMLException, IOException {
		return writer.writeSBML(listOfOpenedFiles.get(model).getDocument().getModel().getUserObject(ORIGINAL_MODEL_KEY), filename);
	}

  /* (non-Javadoc)
	 * @see org.sbml.jsbml.SBMLOutputConverter#writeSBML(java.lang.Object, java.lang.String)
	 */
	public boolean writeSBML(Object sbmlDocument, String filename)
			throws SBMLException, IOException {
		return writer.writeSBML(sbmlDocument, filename);
	}

	/* (non-Javadoc)
	 * @see org.sbml.jsbml.SBMLOutputConverter#writeSBML(java.lang.Object, java.lang.String, java.lang.String, java.lang.String)
	 */
	public boolean writeSBML(Object object, String filename,
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
	public boolean writeSelectedModelToSBML(String filename)
			throws SBMLException, IOException {
		return writer.writeSBML(
			listOfOpenedFiles.get(selectedModel).getDocument().getModel().getUserObject(ORIGINAL_MODEL_KEY),
			filename,
			System.getProperty("app.name"),
			System.getProperty("app.version")
		);
	}

	/* (non-Javadoc)
	 * @see org.sbml.jsbml.util.TreeNodeChangeListener#nodeRemoved(org.sbml.jsbml.util.TreeNodeRemovedEvent)
	 */
	//@Override
	public void nodeRemoved(TreeNodeRemovedEvent evt) {
		TreeNode node = evt.getSource();
	    if (!removed.contains(node)) {
	        removed.add(node);
	    }
	}

	/**
	 * 
	 * @return
	 */
	public SBMLInputConverter getReader() {
		return reader;
	}

	public void setSelectedFile(int i) {
		selectedModel = i;
	}

}
