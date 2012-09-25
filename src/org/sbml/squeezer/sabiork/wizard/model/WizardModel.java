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
package org.sbml.squeezer.sabiork.wizard.model;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import org.sbml.jsbml.CVTerm;
import org.sbml.jsbml.KineticLaw;
import org.sbml.jsbml.Reaction;
import org.sbml.jsbml.SBMLDocument;
import org.sbml.jsbml.util.filters.CVTermFilter;
import org.sbml.squeezer.SubmodelController;

/**
 * The model of the SABIO-RK wizard.
 * 
* @author Matthias Rall
 * @version $Rev$
 */
public class WizardModel implements PropertyChangeListener {

	private final PropertyChangeSupport propertyChangeSupport;
	private Reaction selectedReaction;
	private KineticLaw selectedKineticLaw;
	private KineticLawImporter selectedKineticLawImporter;
	private List<Reaction> selectedReactions;
	private List<KineticLawImporter> selectedKineticLawImporters;
	private SubmodelController submodelController;

	/**
	 * 
	 * @param sbmlDocument
	 * @param reactionId
	 */
	public WizardModel(SBMLDocument sbmlDocument, String reactionId) {
		this.propertyChangeSupport = new PropertyChangeSupport(this);
		// (Debugging purposes)
		// this.propertyChangeSupport.addPropertyChangeListener(this);
		submodelController = new SubmodelController(sbmlDocument.getModel());
		// TODO: Should all reactions be copied or only this single one?
		submodelController.createSubmodel(reactionId);
		this.selectedReaction = null;
		this.selectedKineticLaw = null;
		this.selectedKineticLawImporter = null;
		this.selectedReactions = new ArrayList<Reaction>();
		this.selectedKineticLawImporters = new ArrayList<KineticLawImporter>();
	}
	
	/**
	 * 
	 * @param sbmlDocument
	 */
	public WizardModel(SBMLDocument sbmlDocument) {
		this(sbmlDocument, null);
	}

	public void addPropertyChangeListener(PropertyChangeListener listener) {
		this.propertyChangeSupport.addPropertyChangeListener(listener);
	}

	public void removePropertyChangeListener(PropertyChangeListener listener) {
		this.propertyChangeSupport.removePropertyChangeListener(listener);
	}

	/**
	 * Swaps the copied model with the original model.
	 */
	public void applyChanges() {
		submodelController.storeKineticLaws(true);
	}

	/**
	 * Returns the result of the wizard.
	 * 
	 * @return
	 */
	public SBMLDocument getResult() {
		return submodelController.getSBMLDocument();
	}

	/**
	 * Returns all {@link Reaction} of the current SBML document.
	 * 
	 * @return
	 */
	public List<Reaction> getReactions() {
		return submodelController.getSubmodel().getListOfReactions();
	}

	/**
	 * Returns all {@link Reaction} with a {@link KineticLaw} of the current
	 * SBML document.
	 * 
	 * @return
	 */
	public List<Reaction> getReactionsWithKineticLaw() {
		List<Reaction> reactionsWithKineticLaw = new ArrayList<Reaction>();
		for (Reaction reaction : getReactions()) {
			if (reaction.isSetKineticLaw()) {
				reactionsWithKineticLaw.add(reaction);
			}
		}
		return reactionsWithKineticLaw;
	}

	/**
	 * Returns all {@link Reaction} without a {@link KineticLaw} of the current
	 * SBML document.
	 * 
	 * @return
	 */
	public List<Reaction> getReactionsWithoutKineticLaw() {
		List<Reaction> reactionsWithoutKineticLaw = new ArrayList<Reaction>();
		for (Reaction reaction : getReactions()) {
			if (!reaction.isSetKineticLaw()) {
				reactionsWithoutKineticLaw.add(reaction);
			}
		}
		return reactionsWithoutKineticLaw;
	}

	/**
	 * Returns all reversible {@link Reaction} of the current SBML document.
	 * 
	 * @return
	 */
	public List<Reaction> getReversibleReactions() {
		List<Reaction> reversibleReactions = new ArrayList<Reaction>();
		for (Reaction reaction : getReactions()) {
			if (reaction.isReversible()) {
				reversibleReactions.add(reaction);
			}
		}
		return reversibleReactions;
	}

	/**
	 * Returns all irreversible {@link Reaction} of the current SBML document.
	 * 
	 * @return
	 */
	public List<Reaction> getIrreversibleReactions() {
		List<Reaction> irreversibleReactions = new ArrayList<Reaction>();
		for (Reaction reaction : getReactions()) {
			if (!reaction.isReversible()) {
				irreversibleReactions.add(reaction);
			}
		}
		return irreversibleReactions;
	}

	/**
	 * Returns all fast {@link Reaction} of the current SBML document.
	 * 
	 * @return
	 */
	public List<Reaction> getFastReactions() {
		List<Reaction> fastReactions = new ArrayList<Reaction>();
		for (Reaction reaction : getReactions()) {
			if (reaction.isFast()) {
				fastReactions.add(reaction);
			}
		}
		return fastReactions;
	}

	/**
	 * Returns all slow {@link Reaction} of the current SBML document.
	 * 
	 * @return
	 */
	public List<Reaction> getSlowReactions() {
		List<Reaction> slowReactions = new ArrayList<Reaction>();
		for (Reaction reaction : getReactions()) {
			if (!reaction.isFast()) {
				slowReactions.add(reaction);
			}
		}
		return slowReactions;
	}

	/**
	 * Returns the kegg id of the given {@link Reaction}. If it doesn't exist,
	 * an empty {@link String} is returned.
	 * 
	 * @return
	 */
	public String getKeggReactionID(Reaction reaction) {
		String keggReactionID = "";
		CVTermFilter cvTermFilter = new CVTermFilter(CVTerm.Qualifier.BQB_IS);
		List<CVTerm> cvTerms = new LinkedList<CVTerm>();
		for (CVTerm cvTerm : reaction.getCVTerms()) {
			if (cvTermFilter.accepts(cvTerm)) {
				cvTerms.add(cvTerm);
			}
		}
		for (CVTerm cvTerm : cvTerms) {
			for (String resource : cvTerm.getResources()) {
				if (resource.matches("urn:miriam:kegg.reaction:.*")) {
					keggReactionID = resource.replaceAll(
							"urn:miriam:kegg.reaction:", "");
				} else if (resource
						.matches("http://identifiers.org/kegg.reaction/.*")) {
					keggReactionID = resource.replaceAll(
							"http://identifiers.org/kegg.reaction/", "");
				}
			}
		}
		return keggReactionID;
	}

	public Reaction getSelectedReaction() {
		return selectedReaction;
	}

	public KineticLaw getSelectedKineticLaw() {
		return selectedKineticLaw;
	}

	public KineticLawImporter getSelectedKineticLawImporter() {
		return selectedKineticLawImporter;
	}

	public List<Reaction> getSelectedReactions() {
		return selectedReactions;
	}

	public List<KineticLawImporter> getSelectedKineticLawImporters() {
		return selectedKineticLawImporters;
	}

	public void setSelectedReaction(Reaction selectedReaction) {
		Reaction oldValue = this.selectedReaction;
		Reaction newValue = selectedReaction;
		this.selectedReaction = newValue;
		propertyChangeSupport.firePropertyChange("selectedReaction", oldValue,
				newValue);
	}

	public void setSelectedKineticLaw(KineticLaw selectedKineticLaw) {
		KineticLaw oldValue = this.selectedKineticLaw;
		KineticLaw newValue = selectedKineticLaw;
		this.selectedKineticLaw = newValue;
		propertyChangeSupport.firePropertyChange("selectedKineticLaw",
				oldValue, newValue);
	}

	public void setSelectedKineticLawImporter(
			KineticLawImporter selectedKineticLawImporter) {
		KineticLawImporter oldValue = this.selectedKineticLawImporter;
		KineticLawImporter newValue = selectedKineticLawImporter;
		this.selectedKineticLawImporter = newValue;
		propertyChangeSupport.firePropertyChange("selectedKineticLawImporter",
				oldValue, newValue);
	}

	public void setSelectedReactions(List<Reaction> selectedReactions) {
		List<Reaction> oldValue = this.selectedReactions;
		List<Reaction> newValue = selectedReactions;
		this.selectedReactions = newValue;
		propertyChangeSupport.firePropertyChange("selectedReactions", oldValue,
				newValue);
	}

	public void setSelectedKineticLawImporters(
			List<KineticLawImporter> selectedKineticLawImporters) {
		List<KineticLawImporter> oldValue = this.selectedKineticLawImporters;
		List<KineticLawImporter> newValue = selectedKineticLawImporters;
		this.selectedKineticLawImporters = newValue;
		propertyChangeSupport.firePropertyChange("selectedKineticLawImporters",
				oldValue, newValue);
	}

	public boolean hasSelectedReaction() {
		return (selectedReaction != null);
	}

	public boolean hasSelectedKineticLaw() {
		return (selectedKineticLaw != null);
	}

	public boolean hasSelectedKineticLawImporter() {
		return (selectedKineticLawImporter != null);
	}

	public boolean hasSelectedReactions() {
		return !selectedReactions.isEmpty();
	}

	public boolean hasSelectedKineticLawImporters() {
		return !selectedKineticLawImporters.isEmpty();
	}

	public void propertyChange(PropertyChangeEvent e) {
		if (e.getSource().equals(this)) {
			System.out.println("Model");
			System.out.println("PropertyName: " + e.getPropertyName());
			System.out.println("OldValue: " + e.getOldValue());
			System.out.println("NewValue: " + e.getNewValue());
		}
	}

}
