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
package org.sbml.squeezer.sabiork.wizard.model;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.*;
import java.util.logging.Logger;

import org.sbml.jsbml.CVTerm;
import org.sbml.jsbml.KineticLaw;
import org.sbml.jsbml.Reaction;
import org.sbml.jsbml.SBMLDocument;
import org.sbml.jsbml.util.filters.CVTermFilter;
import org.sbml.squeezer.SubmodelController;
import org.sbml.squeezer.sabiork.SABIORK;

/**
 * The model of the SABIO-RK wizard.
 *
 * @author Matthias Rall
 * @since 2.0
 */
public class WizardModel implements PropertyChangeListener {

    /**
     * A {@link Logger} for this class.
     */
    private static final transient Logger logger = Logger.getLogger(WizardModel.class.getName());

    private final PropertyChangeSupport propertyChangeSupport;
    private Reaction selectedReaction;
    private KineticLaw selectedKineticLaw;
    private KineticLawImporter selectedKineticLawImporter;
    private List<Reaction> selectedReactions;
    private List<KineticLawImporter> selectedKineticLawImporters;
    private SubmodelController submodelController;
    private boolean deleted;

    /**
     * @param sbmlDocument
     * @param reactionId
     */
    public WizardModel(SBMLDocument sbmlDocument, String reactionId, boolean overwriteExistingLaws) {
        propertyChangeSupport = new PropertyChangeSupport(this);
        // (Debugging purposes)
        // this.propertyChangeSupport.addPropertyChangeListener(this);
        submodelController = new SubmodelController(sbmlDocument.getModel());
        submodelController.setGenerateLawsForAllReactions(overwriteExistingLaws);
        submodelController.createSubmodel(reactionId);
        selectedReaction = sbmlDocument.getModel().getReaction(reactionId);
        selectedKineticLaw = null;
        selectedKineticLawImporter = null;
        selectedReactions = new ArrayList<Reaction>();
        selectedKineticLawImporters = new ArrayList<KineticLawImporter>();
        deleted = false;
    }

    /**
     * @param sbmlDocument
     */
    public WizardModel(SBMLDocument sbmlDocument, boolean overwriteExistingLaws) {
        this(sbmlDocument, null, overwriteExistingLaws);
    }

    /**
     * @param listener
     */
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        propertyChangeSupport.addPropertyChangeListener(listener);
    }

    /**
     * @param listener
     */
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        propertyChangeSupport.removePropertyChangeListener(listener);
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
    public SubmodelController getResult() {
        if (deleted) {
            return null;
        }
        return submodelController;
    }

    /**
     *
     */
    public void deleteResult() {
        deleted = true;
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
                    keggReactionID = resource.replaceAll("urn:miriam:kegg.reaction:", "");
                } else if (resource.matches("http://identifiers.org/kegg.reaction/.*")) {
                    keggReactionID = resource.replaceAll(
                            "http://identifiers.org/kegg.reaction/", "");
                }
            }
        }
        return keggReactionID;
    }

    /**
     * Returns the any reaction ids of the given {@link Reaction} in a hashmap. If none exist,
     * an empty {@link java.util.HashMap} is returned.
     *
     * @return
     */
    public HashMap<SABIORK.QueryField, List<String>> getReactionIDs(Reaction reaction) {
        HashMap<SABIORK.QueryField, List<String>> reactionIDs = new HashMap<SABIORK.QueryField, List<String>>();
        CVTermFilter cvTermFilter = new CVTermFilter(CVTerm.Qualifier.BQB_IS);
        List<CVTerm> cvTerms = new LinkedList<CVTerm>();
        for (CVTerm cvTerm : reaction.getCVTerms()) {
            if (cvTermFilter.accepts(cvTerm)) {
                cvTerms.add(cvTerm);
            }
        }
        for (CVTerm cvTerm : cvTerms) {
            for (String resource : cvTerm.getResources()) {
                String source = "";
                String id = "";
                String[] resSplit;
                if (resource.matches("urn:miriam:.*")) {
                    resSplit = resource.split(":");
                    source = resSplit[2];
                    id = resSplit[3];
                } else if (resource.matches("http://identifiers.org/.*")) {
                    resSplit = resource.split("/");
                    source = resSplit[3];
                    id = resSplit[4];
                }
                SABIORK.QueryField key;
                switch(source) {
                    case "kegg.reaction":
                        key = SABIORK.QueryField.KEGG_REACTION_ID;
                        break;
                    case "rhea":
                        key = SABIORK.QueryField.RHEA_REACTION_ID;
                        break;
                    case "metacyc.reaction":
                        key = SABIORK.QueryField.METACYC_REACTION_ID;
                        break;
                    case "metanetx.reaction":
                        key = SABIORK.QueryField.METANETX_REACTION_ID;
                        break;
                    case "reactome":
                        key = SABIORK.QueryField.REACTOME_REACTION_ID;
                        break;
                    default:
                        key = null;
                        break;
                }
                if(key != null) {
                    List<String> idList = reactionIDs.get(key);;
                    if(idList == null) {
                        idList = new ArrayList<>();
                    }
                    idList.add(id);
                    reactionIDs.put(key, idList);
                }
            }
        }
        return reactionIDs;
    }

    /**
     * @return
     */
    public Reaction getSelectedReaction() {
        return selectedReaction;
    }

    /**
     * @return
     */
    public KineticLaw getSelectedKineticLaw() {
        return selectedKineticLaw;
    }

    /**
     * @return
     */
    public KineticLawImporter getSelectedKineticLawImporter() {
        return selectedKineticLawImporter;
    }

    /**
     * @return
     */
    public List<Reaction> getSelectedReactions() {
        return selectedReactions;
    }

    /**
     * @return
     */
    public List<KineticLawImporter> getSelectedKineticLawImporters() {
        return selectedKineticLawImporters;
    }

    /**
     * @param selectedReaction
     */
    public void setSelectedReaction(Reaction selectedReaction) {
        Reaction oldValue = this.selectedReaction;
        Reaction newValue = selectedReaction;
        this.selectedReaction = newValue;
        propertyChangeSupport.firePropertyChange("selectedReaction", oldValue,
                newValue);
    }

    /**
     * @param selectedKineticLaw
     */
    public void setSelectedKineticLaw(KineticLaw selectedKineticLaw) {
        KineticLaw oldValue = this.selectedKineticLaw;
        KineticLaw newValue = selectedKineticLaw;
        this.selectedKineticLaw = newValue;
        propertyChangeSupport.firePropertyChange("selectedKineticLaw",
                oldValue, newValue);
    }

    /**
     * @param selectedKineticLawImporter
     */
    public void setSelectedKineticLawImporter(
            KineticLawImporter selectedKineticLawImporter) {
        KineticLawImporter oldValue = this.selectedKineticLawImporter;
        KineticLawImporter newValue = selectedKineticLawImporter;
        this.selectedKineticLawImporter = newValue;
        propertyChangeSupport.firePropertyChange("selectedKineticLawImporter",
                oldValue, newValue);
    }

    /**
     * @param selectedReactions
     */
    public void setSelectedReactions(List<Reaction> selectedReactions) {
        List<Reaction> oldValue = this.selectedReactions;
        List<Reaction> newValue = selectedReactions;
        this.selectedReactions = newValue;
        propertyChangeSupport.firePropertyChange("selectedReactions", oldValue,
                newValue);
    }

    /**
     * @param selectedKineticLawImporters
     */
    public void setSelectedKineticLawImporters(
            List<KineticLawImporter> selectedKineticLawImporters) {
        List<KineticLawImporter> oldValue = this.selectedKineticLawImporters;
        List<KineticLawImporter> newValue = selectedKineticLawImporters;
        this.selectedKineticLawImporters = newValue;
        propertyChangeSupport.firePropertyChange("selectedKineticLawImporters",
                oldValue, newValue);
    }

    /**
     * @return
     */
    public boolean hasSelectedReaction() {
        return (selectedReaction != null);
    }

    /**
     * @return
     */
    public boolean hasSelectedKineticLaw() {
        return (selectedKineticLaw != null);
    }

    /**
     * @return
     */
    public boolean hasSelectedKineticLawImporter() {
        return (selectedKineticLawImporter != null);
    }

    /**
     * @return
     */
    public boolean hasSelectedReactions() {
        return !selectedReactions.isEmpty();
    }

    /**
     * @return
     */
    public boolean hasSelectedKineticLawImporters() {
        return !selectedKineticLawImporters.isEmpty();
    }

    /* (non-Javadoc)
     * @see java.beans.PropertyChangeListener#propertyChange(java.beans.PropertyChangeEvent)
     */
    @Override
    public void propertyChange(PropertyChangeEvent e) {
        if (e.getSource().equals(this)) {
            logger.info("Model");
            logger.info("PropertyName: " + e.getPropertyName());
            logger.info("OldValue: " + e.getOldValue());
            logger.info("NewValue: " + e.getNewValue());
        }
    }

}
