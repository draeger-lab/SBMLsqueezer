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
package org.sbml.squeezer.io;

import java.util.LinkedList;
import java.util.List;

import javax.swing.JTabbedPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.sbml.Model;
import org.sbml.Reaction;
import org.sbml.SBMLReader;
import org.sbml.SBMLWriter;
import org.sbml.SBase;

/**
 * @author Andreas Dr&auml;ger <a
 *         href="mailto:andreas.draeger@uni-tuebingen.de">
 *         andreas.draeger@uni-tuebingen.de</a>
 * 
 */
public class SBMLio implements SBMLReader, SBMLWriter, SBaseChangedListener,
		ChangeListener {

	private List<SBase> added;
	private List<SBase> removed;
	private List<SBase> changed;
	private AbstractSBMLReader reader;
	private AbstractSBMLWriter writer;
	protected LinkedList<Model> listOfModels;
	private int selectedModel;

	public void setSelectedModel(int selectedModel) {
		this.selectedModel = selectedModel;
	}

	/**
	 * 
	 * @return
	 */
	public Model getSelectedModel() {
		return listOfModels.get(selectedModel);
	}

	/**
	 * 
	 */
	public SBMLio(AbstractSBMLReader reader, AbstractSBMLWriter writer) {
		this.reader = reader;
		this.reader.addSBaseChangeListener(this);
		this.writer = writer;
		selectedModel = -1;
		listOfModels = new LinkedList<Model>();
		added = new LinkedList<SBase>();
		removed = new LinkedList<SBase>();
		changed = new LinkedList<SBase>();
	}

	/**
	 * 
	 * @param model
	 */
	public SBMLio(AbstractSBMLReader reader, AbstractSBMLWriter writer,
			Object model) {
		this(reader, writer);
		this.listOfModels.addLast(reader.readModel(model));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.SBaseChangedListener#sbaseAdded(org.sbml.SBase)
	 */
	public void sbaseAdded(SBase sb) {
		added.add(sb);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.SBaseChangedListener#sbaseRemoved(org.sbml.SBase)
	 */
	public void sbaseRemoved(SBase sb) {
		removed.add(sb);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.SBaseChangedListener#stateChanged(org.sbml.SBase)
	 */
	public void stateChanged(SBase sb) {
		changed.add(sb);
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
			if (tabbedPane.getComponentCount() == 0)
				listOfModels.clear();
			else {
				for (Model m : listOfModels) {
					boolean contains = false;
					for (int i = 0; i < tabbedPane.getTabCount() && !contains; i++) {
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
	 * @see org.sbml.SBMLReader#readModel(java.lang.Object)
	 */
	// @Override
	public Model readModel(Object model) {
		listOfModels.addLast(reader.readModel(model));
		selectedModel = listOfModels.size() - 1;
		return listOfModels.getLast();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.SBMLReader#readReaction(java.lang.Object)
	 */
	// @Override
	public Reaction readReaction(Object reaction) {
		return reader.readReaction(reaction);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.SBMLWriter#writeModel(org.sbml.Model)
	 */
	// @Override
	public Object writeModel(Model model) {
		return writer.writeModel(model);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.SBMLWriter#writeReaction(org.sbml.Reaction)
	 */
	// @Override
	public Object writeReaction(Reaction reaction) {
		return writer.writeReaction(reaction);
	}

}
