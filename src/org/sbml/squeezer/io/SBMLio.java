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
import org.sbml.jsbml.SBMLInputConverter;
import org.sbml.jsbml.SBMLOutputConverter;
import org.sbml.jsbml.SBase;
import org.sbml.jsbml.SBaseChangedEvent;
import org.sbml.jsbml.SBaseChangedListener;
import org.sbml.jsbml.SBMLException.Category;
import org.sbml.jsbml.util.IOProgressListener;
import org.sbml.squeezer.SBMLsqueezer;

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
 */
public class SBMLio implements SBMLInputConverter, SBMLOutputConverter,
		SBaseChangedListener, ChangeListener {

	private List<SBase> added;

	private List<SBase> changed;

	private LinkedList<Object> listOfOrigModels;

	private SBMLInputConverter reader;

	private List<SBase> removed;

	private int selectedModel;

	private SBMLOutputConverter writer;

	protected LinkedList<Model> listOfModels;

	/**
	 * 
	 */
	public SBMLio(SBMLInputConverter sbmlReader, SBMLOutputConverter sbmlWriter) {
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
	 * @throws Exception
	 */
	public SBMLio(SBMLInputConverter reader, SBMLOutputConverter writer,
			Object model) throws Exception {
		this(reader, writer);
		this.listOfModels.addLast(reader.convertModel(model));
		this.listOfOrigModels.addLast(model);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.sbml.jsbml.SBMLInputConverter#addIOProgressListener(org.sbml.jsbml
	 * .io.IOProgressListener)
	 */
	public void addIOProgressListener(IOProgressListener listener) {
		reader.addIOProgressListener(listener);
		writer.addIOProgressListener(listener);
	}

	/*
	 * (non-Javadoc)
	 * @see org.sbml.jsbml.SBMLInputConverter#convertModel(java.lang.Object)
	 */
	public Model convertModel(Object model) throws SBMLException {
		try {
			listOfModels.addLast(reader.convertModel(model));
			if (model instanceof String) {
				listOfOrigModels.addLast(reader.getOriginalModel());
			} else {
				listOfOrigModels.addLast(model);
			}
			selectedModel = listOfModels.size() - 1;
			return listOfModels.getLast();
		} catch (Exception exc) {
			// exc.fillInStackTrace();
			exc.printStackTrace();
			SBMLException sbmlExc = new SBMLException("Could not read model.",
					exc);
			sbmlExc.setCategory(Category.XML);
			throw sbmlExc;
		}
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
	 * @see org.sbml.jsbml.SBMLInputConverter#getNumErrors()
	 */
	public int getNumErrors() {
		return listOfModels.size() > 0 ? reader.getNumErrors() : 0;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.jsbml.SBMLOutputConverter#getNumErrors(java.lang.Object)
	 */
	public int getNumErrors(Object sbase) {
		return writer.getNumErrors(sbase);
	}

	/*
	 * (non-Javadoc)
	 * 
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
		return listOfModels.size() > 0 ? listOfModels.get(selectedModel) : null;
	}

	/*
	 * (non-Javadoc)
	 * 
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
		return writer.getWriteWarnings(listOfOrigModels.get(selectedModel));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.sbml.jsbml.SBMLOutputConverter#getWriteWarnings(java.lang.Object)
	 */
	public List<SBMLException> getWriteWarnings(Object sbase) {
		return writer.getWriteWarnings(sbase);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.sbml.jsbml.SBMLOutputConverter#removeUnneccessaryElements(org.sbml
	 * .jsbml.Model, java.lang.Object)
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
	 * @see org.sbml.jsbml.SBMLOutputConverter#saveChanges(org.sbml.jsbml.Model,
	 * java.lang.Object)
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
		return writer
				.saveChanges(reaction, listOfOrigModels.get(selectedModel));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.sbml.jsbml.SBMLOutputConverter#saveChanges(org.sbml.jsbml.Reaction,
	 * java.lang.Object)
	 */
	public boolean saveChanges(Reaction reaction, Object model)
			throws SBMLException {
		return writer.saveChanges(reaction, model);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.jsbml.SBaseChangedListener#sbaseAdded(org.sbml.jsbml.SBase)
	 */
	public void sbaseAdded(SBase sb) {
		if (!added.contains(sb))
			added.add(sb);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.sbml.jsbml.SBaseChangedListener#sbaseRemoved(org.sbml.jsbml.SBase)
	 */
	public void sbaseRemoved(SBase sb) {
		if (!removed.contains(sb))
			removed.add(sb);
	}

	/**
	 * 
	 * @param selectedModel
	 */
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
				for (int i = listOfModels.size() - 1; i >= 0; i--) {
					Model m = listOfModels.get(i);
					if (m != null) {
						boolean contains = false;
						for (int j = 0; j < tabbedPane.getTabCount()
								&& !contains; j++) {
							String title = tabbedPane.getTitleAt(j);
							if (title.equals(m.getName())
									|| title.equals(m.getId()))
								contains = true;
						}
						if (!contains)
							listOfModels.remove(m);
					}
				}
				selectedModel = tabbedPane.getSelectedIndex();
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.sbml.jsbml.SBaseChangedListener#stateChanged(org.sbml.jsbml.SBaseChangedEvent)
	 */
	public void stateChanged(SBaseChangedEvent ev) {
		if (!changed.contains(ev.getSource())) {
			changed.add(ev.getSource());
		}
	}

	/*
	 * (non-Javadoc)
	 * 
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
		return writer.writeSBML(listOfOrigModels.get(model), filename);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.jsbml.SBMLOutputConverter#writeSBML(java.lang.Object,
	 * java.lang.String)
	 */
	public boolean writeSBML(Object sbmlDocument, String filename)
			throws SBMLException, IOException {
		return writer.writeSBML(sbmlDocument, filename);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.jsbml.SBMLOutputConverter#writeSBML(java.lang.Object,
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
