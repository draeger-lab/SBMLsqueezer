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

import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.swing.JTabbedPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.sbml.AbstractSBase;
import org.sbml.Compartment;
import org.sbml.FunctionDefinition;
import org.sbml.InitialAssignment;
import org.sbml.KineticLaw;
import org.sbml.Model;
import org.sbml.ModifierSpeciesReference;
import org.sbml.Parameter;
import org.sbml.Reaction;
import org.sbml.Rule;
import org.sbml.SBMLReader;
import org.sbml.SBMLWriter;
import org.sbml.SBO;
import org.sbml.Species;
import org.sbml.SpeciesReference;
import org.sbml.SpeciesType;
import org.sbml.StoichiometryMath;
import org.sbml.Unit;
import org.sbml.UnitDefinition;

/**
 * @author Andreas Dr&auml;ger <a
 *         href="mailto:andreas.draeger@uni-tuebingen.de">
 *         andreas.draeger@uni-tuebingen.de</a>
 * 
 */
public class SBMLio implements SBMLReader, SBMLWriter, SBaseChangedListener,
		ChangeListener {

	private List<AbstractSBase> added;
	private List<AbstractSBase> removed;
	private List<AbstractSBase> changed;
	private AbstractSBMLReader reader;
	private AbstractSBMLWriter writer;
	protected LinkedList<Model> listOfModels;
	private int selectedModel;

	/**
	 * 
	 */
	public SBMLio(AbstractSBMLReader reader, AbstractSBMLWriter writer) {
		this.reader = reader;
		this.reader.addSBaseChangeListener(this);
		this.writer = writer;
		listOfModels = new LinkedList<Model>();
		selectedModel = -1;
		added = new LinkedList<AbstractSBase>();
		removed = new LinkedList<AbstractSBase>();
		changed = new LinkedList<AbstractSBase>();
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

	/**
	 * 
	 * @return
	 */
	public List<Model> getListOfModels() {
		return listOfModels;
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
	 * @see org.sbml.SBMLReader#readCompartment(java.lang.Object)
	 */
	public Compartment readCompartment(Object compartment) {
		return reader.readCompartment(compartment);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.SBMLReader#readFunctionDefinition(java.lang.Object)
	 */
	public FunctionDefinition readFunctionDefinition(Object functionDefinition) {
		return reader.readFunctionDefinition(functionDefinition);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.SBMLReader#readInitialAssignment(java.lang.Object)
	 */
	public InitialAssignment readInitialAssignment(Object initialAssignment) {
		return reader.readInitialAssignment(initialAssignment);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.SBMLReader#readKineticLaw(java.lang.Object)
	 */
	public KineticLaw readKineticLaw(Object kineticLaw) {
		return reader.readKineticLaw(kineticLaw);
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
	 * @see org.sbml.SBMLReader#readModifierSpeciesReference(java.lang.Object)
	 */
	public ModifierSpeciesReference readModifierSpeciesReference(
			Object modifierSpeciesReference) {
		return reader.readModifierSpeciesReference(modifierSpeciesReference);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.SBMLReader#readParameter(java.lang.Object)
	 */
	public Parameter readParameter(Object parameter) {
		return reader.readParameter(parameter);
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
	 * @see org.sbml.SBMLReader#readRule(java.lang.Object)
	 */
	public Rule readRule(Object rule) {
		return reader.readRule(rule);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.SBMLReader#readSpecies(java.lang.Object)
	 */
	public Species readSpecies(Object species) {
		return reader.readSpecies(species);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.SBMLReader#readSpeciesReference(java.lang.Object)
	 */
	public SpeciesReference readSpeciesReference(Object speciesReference) {
		return reader.readSpeciesReference(speciesReference);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.SBMLReader#readSpeciesType(java.lang.Object)
	 */
	public SpeciesType readSpeciesType(Object speciesType) {
		return reader.readSpeciesType(speciesType);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.SBMLReader#readStoichiometricMath(java.lang.Object)
	 */
	public StoichiometryMath readStoichiometricMath(Object stoichiometryMath) {
		return reader.readStoichiometricMath(stoichiometryMath);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.SBMLReader#readUnit(java.lang.Object)
	 */
	public Unit readUnit(Object unit) {
		return reader.readUnit(unit);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.SBMLReader#readUnitDefinition(java.lang.Object)
	 */
	public UnitDefinition readUnitDefinition(Object unitDefinition) {
		return reader.readUnitDefinition(unitDefinition);
	}

	/**
	 * 
	 */
	public void saveChanges() {
		// TODO Auto-generated method stub
		System.out.println("fertig");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.SBaseChangedListener#sbaseAdded(org.sbml.SBase)
	 */
	public void sbaseAdded(AbstractSBase sb) {
		added.add(sb);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.SBaseChangedListener#sbaseRemoved(org.sbml.SBase)
	 */
	public void sbaseRemoved(AbstractSBase sb) {
		removed.add(sb);
	}

	public void setSelectedModel(int selectedModel) {
		this.selectedModel = selectedModel;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.SBaseChangedListener#stateChanged(org.sbml.SBase)
	 */
	public void stateChanged(AbstractSBase sb) {
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
	 * @param model
	 * @param possibleEnzymes
	 */
	public void updateEnzymeKatalysis(Model model, Set<Integer> possibleEnzymes) {
		for (Reaction r : model.getListOfReactions()) {
			for (ModifierSpeciesReference modifier : r.getListOfModifiers()) {
				if (SBO.isEnzymaticCatalysis(modifier.getSBOTerm())
						&& !possibleEnzymes.contains(Integer.valueOf(modifier
								.getSpeciesInstance().getSBOTerm())))
					modifier.setSBOTerm(SBO.getCatalysis());
				else if (SBO.isCatalyst(modifier.getSBOTerm())
						&& possibleEnzymes.contains(Integer.valueOf(modifier
								.getSpeciesInstance().getSBOTerm())))
					modifier.setSBOTerm(SBO.getEnzymaticCatalysis());
			}
		}
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