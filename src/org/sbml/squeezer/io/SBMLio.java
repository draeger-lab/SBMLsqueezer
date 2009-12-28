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
package org.sbml.squeezer.io;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JTabbedPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.sbml.jsbml.Model;
import org.sbml.jsbml.Reaction;
import org.sbml.jsbml.SBMLException;
import org.sbml.jsbml.SBMLReader;
import org.sbml.jsbml.SBMLWriter;
import org.sbml.jsbml.SBase;
import org.sbml.jsbml.SBaseChangedListener;
import org.sbml.jsbml.io.IOProgressListener;
import org.sbml.squeezer.SBMLsqueezer;

/**
 * A manager class for reading and writing models from some source into JSBML
 * data objects and to synchronize the model with the original data structure
 * afterwards. This class requires an instance of the so called
 * {@link SBMLReader} and {@link SBMLWriter} classes and maintains all loaded
 * original model objects and the JSBML representation of it in designated
 * lists. It can be used to, e.g., load a model from CellDesigner's plug-in data
 * structures and save changes back in CellDesigner or to do the same with
 * models that originate from libSBML.
 * 
 * @author Andreas Dr&auml;ger <a
 *         href="mailto:andreas.draeger@uni-tuebingen.de">
 *         andreas.draeger@uni-tuebingen.de</a>
 * @since 1.3
 */
public class SBMLio implements SBMLReader, SBMLWriter, SBaseChangedListener,
		ChangeListener {

	private List<SBase> added;

	private List<SBase> changed;

	private LinkedList<Object> listOfOrigModels;

	private SBMLReader reader;

	private List<SBase> removed;

	private int selectedModel;

	private SBMLWriter writer;

	protected LinkedList<Model> listOfModels;

	/**
	 * 
	 */
	public SBMLio(SBMLReader sbmlReader, SBMLWriter sbmlWriter) {
		this.reader = sbmlReader;
		// this.reader.addSBaseChangeListener(this);
		this.writer = sbmlWriter;
		listOfModels = new LinkedList<Model>();
		listOfOrigModels = new LinkedList<Object>();
		selectedModel = -1;
		added = new LinkedList<SBase>();
		removed = new LinkedList<SBase>();
		changed = new LinkedList<SBase>();
	}

	/**
	 * 
	 * @param model
	 */
	public SBMLio(SBMLReader reader, SBMLWriter writer, Object model) {
		this(reader, writer);
		this.listOfModels.addLast(reader.readModel(model));
		this.listOfOrigModels.addLast(model);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.jsbml.SBMLReader#addEventListener(java.util.EventListener)
	 */
	public void addIOProgressListener(IOProgressListener listener) {
		reader.addIOProgressListener(listener);
		writer.addIOProgressListener(listener);
	}

	/**
	 * 
	 * @return
	 */
	public List<Model> getListOfModels() {
		return listOfModels;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.jlibsbml.SBMLReader#getNumErrors()
	 */
	public int getNumErrors() {
		return listOfModels.size() > 0 ? reader.getNumErrors() : 0;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.jlibsbml.SBMLWriter#getNumErrors(java.lang.Object)
	 */
	public int getNumErrors(Object sbase) {
		return writer.getNumErrors(sbase);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.jsbml.SBMLReader#getOriginalModel()
	 */
	public Object getOriginalModel() {
		return reader.getOriginalModel();
	}

	/**
	 * 
	 * @return
	 */
	public Model getSelectedModel() {
		return listOfModels.get(selectedModel);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.jlibsbml.SBMLReader#getWarnings()
	 */
	public List<SBMLException> getWarnings() {
		return reader.getWarnings();
	}

	/**
	 * 
	 * @return
	 */
	public List<SBMLException> getWriteWarnings() {
		return writer.getWriteWarnings(listOfOrigModels.get(selectedModel));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.jlibsbml.SBMLWriter#getWarnings(java.lang.Object)
	 */
	public List<SBMLException> getWriteWarnings(Object sbase) {
		return writer.getWriteWarnings(sbase);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.SBMLReader#readModel(java.lang.Object)
	 */
	// @Override
	public Model readModel(Object model) {
		try {
			listOfModels.addLast(reader.readModel(model));
			if (model instanceof String)
				listOfOrigModels.addLast(reader.getOriginalModel());
			else
				listOfOrigModels.addLast(model);
			selectedModel = listOfModels.size() - 1;
			return listOfModels.getLast();
		} catch (Exception exc) {
			exc.printStackTrace();
			throw new RuntimeException("Could not read model.");
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.sbml.jsbml.SBMLWriter#removeUnneccessaryElements(org.sbml.jsbml.Model
	 * , java.lang.Object)
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
		writer.saveChanges(listOfModels.get(selectedModel), listOfOrigModels
				.get(selectedModel));
		listener.ioProgressOn(null);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.jsbml.SBMLWriter#saveChanges(org.sbml.jsbml.Model,
	 * java.lang.Object)
	 */
	public void saveChanges(Model model, Object object) throws SBMLException {
		writer.saveChanges(model, object);
	}

	/**
	 * 
	 * @param reaction
	 * @throws SBMLException
	 */
	public void saveChanges(Reaction reaction) throws SBMLException {
		writer.saveChanges(reaction, listOfOrigModels.get(selectedModel));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.jsbml.SBMLWriter#saveChanges(org.sbml.jsbml.Reaction,
	 * java.lang.Object)
	 */
	public void saveChanges(Reaction reaction, Object model)
			throws SBMLException {
		writer.saveChanges(reaction, model);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.SBaseChangedListener#sbaseAdded(org.sbml.SBase)
	 */
	public void sbaseAdded(SBase sb) {
		if (!added.contains(sb))
			added.add(sb);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.SBaseChangedListener#sbaseRemoved(org.sbml.SBase)
	 */
	public void sbaseRemoved(SBase sb) {
		if (!removed.contains(sb))
			removed.add(sb);
	}

	public void setSelectedModel(int selectedModel) {
		this.selectedModel = selectedModel;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * javax.swing.event.ChangeListener#stateChanged(javax.swing.event.ChangeEvent
	 * )
	 */
	public void stateChanged(ChangeEvent e) {
		if (e.getSource() instanceof JTabbedPane) {
			JTabbedPane tabbedPane = (JTabbedPane) e.getSource();
			if (tabbedPane.getComponentCount() == 0) {
				listOfModels.clear();
				listOfOrigModels.clear();
			} else {
				// search for the currently selected model.
				for (Model m : listOfModels)
					if (m != null) {
						boolean contains = false;
						for (int i = 0; i < tabbedPane.getTabCount()
								&& !contains; i++) {
							String title = tabbedPane.getTitleAt(i);
							if (title.equals(m.getName())
									|| title.equals(m.getId()))
								contains = true;
						}
						if (!contains)
							listOfModels.remove(m);
					}
				selectedModel = tabbedPane.getSelectedIndex();
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.SBaseChangedListener#stateChanged(org.sbml.SBase)
	 */
	public void stateChanged(SBase sb) {
		if (!changed.contains(sb))
			changed.add(sb);
	}

	/*
	 * (non-Javadoc)
	 * @see org.sbml.jsbml.SBMLWriter#writeModel(org.sbml.jsbml.Model)
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
		return writer.writeSBML(listOfOrigModels.get(model), filename);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.SBMLWriter#writeSBML(java.lang.Object, java.lang.String)
	 */
	// @Override
	public boolean writeSBML(Object sbmlDocument, String filename)
			throws SBMLException, IOException {
		return writer.writeSBML(sbmlDocument, filename);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.jsbml.SBMLWriter#writeSBML(java.lang.Object,
	 * java.lang.String, java.lang.String, java.lang.String)
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
		return writer.writeSBML(listOfOrigModels.get(selectedModel), filename,
				SBMLsqueezer.class.getSimpleName(), SBMLsqueezer
						.getVersionNumber());
	}
}
