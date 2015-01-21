/*
 * $Id$
 * $URL$
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
package org.sbml.squeezer.sabiork.wizard.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashSet;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.border.EmptyBorder;

import org.sbml.jsbml.CallableSBase;
import org.sbml.jsbml.Compartment;
import org.sbml.jsbml.FunctionDefinition;
import org.sbml.jsbml.LocalParameter;
import org.sbml.jsbml.Parameter;
import org.sbml.jsbml.Reaction;
import org.sbml.jsbml.SBase;
import org.sbml.jsbml.Species;
import org.sbml.jsbml.SpeciesReference;
import org.sbml.jsbml.UnitDefinition;
import org.sbml.squeezer.sabiork.wizard.gui.JDialogWizard.ButtonState;
import org.sbml.squeezer.sabiork.wizard.gui.JDialogWizard.CardID;

import de.zbit.sbml.gui.SBasePanel;

import org.sbml.squeezer.sabiork.wizard.model.KineticLawImporter;
import org.sbml.squeezer.sabiork.wizard.model.WizardModel;
import org.sbml.squeezer.sabiork.wizard.model.WizardProperties;

/**
 * A class that allows the manual matching of SBML components.
 * 
 * @author Matthias Rall
 * @version $Rev$
 * @since 2.0
 */
public class CardMatching extends Card implements PropertyChangeListener {
  
  /**
   * Generated serial version identifier.
   */
  private static final long serialVersionUID = -9179045292093429309L;
  
  private JDialog dialogComponentDetails;
  private JPanel panelMatchings;
  private JPanel panelComponentMatchEntries;
  private JPanel panelImports;
  private JPanel panelComponentImportEntries;
  private JScrollPane panelMatchingsScrollPane;
  private JScrollPane panelImportsScrollPane;
  private KineticLawImporter kineticLawImporter;
  
  /**
   * 
   * @param dialog
   * @param model
   */
  public CardMatching(JDialogWizard dialog, WizardModel model) {
    super(dialog, model);
    model.addPropertyChangeListener(this);
    initialize();
  }
  
  /**
   * Initialization of the card.
   */
  private void initialize() {
    panelComponentMatchEntries = new JPanel(new GridLayout(0, 1, 0, 5));
    
    panelComponentImportEntries = new JPanel(new GridLayout(0, 1, 0, 5));
    
    panelMatchings = new JPanel(new BorderLayout());
    panelMatchings.add(panelComponentMatchEntries, BorderLayout.NORTH);
    panelMatchingsScrollPane = new JScrollPane(panelMatchings);
    panelMatchingsScrollPane.setBorder(BorderFactory.createTitledBorder(
      BorderFactory.createEtchedBorder(),
      WizardProperties.getText("CARD_MATCHING_TEXT_MATCHINGS")));
    panelMatchingsScrollPane.setBackground(getBackground());
    panelMatchingsScrollPane
    .setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
    
    panelImports = new JPanel(new BorderLayout());
    panelImports.add(panelComponentImportEntries, BorderLayout.NORTH);
    panelImportsScrollPane = new JScrollPane(panelImports);
    panelImportsScrollPane.setBorder(BorderFactory.createTitledBorder(
      BorderFactory.createEtchedBorder(),
      WizardProperties.getText("CARD_MATCHING_TEXT_IMPORTS")));
    panelImportsScrollPane.setBackground(getBackground());
    panelImportsScrollPane
    .setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
    
    setLayout(new GridLayout(2, 1, 0, 20));
    add(panelMatchingsScrollPane);
    add(panelImportsScrollPane);
  }
  
  /* (non-Javadoc)
   * @see org.sbml.squeezer.sabiork.wizard.gui.Card#performBeforeShowing()
   */
  @Override
  public void performBeforeShowing() {
    dialog.setButtonState(ButtonState.NEXT_DISABLED);
    panelComponentMatchEntries.removeAll();
    panelComponentImportEntries.removeAll();
    kineticLawImporter = null;
    if (model.hasSelectedKineticLaw() && model.hasSelectedReaction()) {
      kineticLawImporter = new KineticLawImporter(
        model.getSelectedKineticLaw(), model.getSelectedReaction());
    }
    if (kineticLawImporter != null) {
      panelComponentMatchEntries.add(new JPanelComponentMatchEntry());
      for (Compartment referencedCompartment : kineticLawImporter
          .getReferencedCompartments()) {
        panelComponentMatchEntries.add(new JPanelComponentMatchEntry(
          referencedCompartment));
      }
      for (Reaction referencedReaction : kineticLawImporter
          .getReferencedReactions()) {
        panelComponentMatchEntries.add(new JPanelComponentMatchEntry(
          referencedReaction));
      }
      for (Species referencedSpecies : kineticLawImporter
          .getReferencedSpecies()) {
        panelComponentMatchEntries.add(new JPanelComponentMatchEntry(
          referencedSpecies));
      }
      for (SpeciesReference referencedSpeciesReference : kineticLawImporter
          .getReferencedSpeciesReferences()) {
        panelComponentMatchEntries.add(new JPanelComponentMatchEntry(
          referencedSpeciesReference));
      }
      for (FunctionDefinition referencedFunctionDefinition : kineticLawImporter
          .getReferencedFunctionDefinitions()) {
        panelComponentImportEntries.add(new JPanelComponentImportEntry(
          referencedFunctionDefinition));
      }
      for (LocalParameter referencedLocalParameter : kineticLawImporter
          .getReferencedLocalParameters()) {
        panelComponentImportEntries.add(new JPanelComponentImportEntry(
          referencedLocalParameter));
      }
      for (Parameter referencedParameter : kineticLawImporter
          .getReferencedParameters()) {
        panelComponentImportEntries.add(new JPanelComponentImportEntry(
          referencedParameter));
      }
      for (UnitDefinition referencedUnitDefinition : kineticLawImporter
          .getReferencedUnitDefinitions()) {
        panelComponentImportEntries.add(new JPanelComponentImportEntry(
          referencedUnitDefinition));
      }
      setSelectedKineticLawImporter();
    }
  }
  
  /* (non-Javadoc)
   * @see org.sbml.squeezer.sabiork.wizard.gui.Card#getPreviousCardID()
   */
  @Override
  public CardID getPreviousCardID() {
    return CardID.SEARCH_M;
  }
  
  /* (non-Javadoc)
   * @see org.sbml.squeezer.sabiork.wizard.gui.Card#getNextCardID()
   */
  @Override
  public CardID getNextCardID() {
    return CardID.SUMMARY_M;
  }
  
  /**
   * Adds the selected {@link KineticLawImporter} to the model or
   * {@code null} if it is not importable.
   */
  private void setSelectedKineticLawImporter() {
    KineticLawImporter selectedKineticLawImporter = null;
    if (kineticLawImporter.isImportableKineticLaw()) {
      selectedKineticLawImporter = kineticLawImporter;
    }
    model.setSelectedKineticLawImporter(selectedKineticLawImporter);
  }
  
  /**
   * Shows a dialog with additional information about the SBML component.
   * 
   * @param component
   *            the component
   * @see SBasePanel
   */
  private void showComponentDetailsDialog(SBase component) {
    dialogComponentDetails = new JDialog(dialog);
    dialogComponentDetails.add(new SBasePanel(component, true));
    dialogComponentDetails.setSize(dialogComponentDetails
      .getPreferredSize());
    dialogComponentDetails.setVisible(true);
  }
  
  /**
   * Sets the {@link ButtonState} of the wizard according to the selected
   * {@link KineticLawImporter}.
   */
  @Override
  public void propertyChange(PropertyChangeEvent e) {
    if (e.getSource().equals(model)
        && e.getPropertyName().equals("selectedKineticLawImporter")) {
      if (model.hasSelectedKineticLawImporter()) {
        dialog.setButtonState(ButtonState.NEXT_ENABLED);
      } else {
        dialog.setButtonState(ButtonState.NEXT_DISABLED);
      }
    }
  }
  
  /**
   * A class for manual matching of SBML components.
   * 
   * @author Matthias Rall
   * 
   */
  private class JPanelComponentMatchEntry extends JPanel implements
  ActionListener {
    
    /**
     * Generated serial version identifier.
     */
    private static final long serialVersionUID = -427890259513619815L;
    
    private CallableSBase referencedComponent;
    private CallableSBase matchingReferenceableComponent;
    private Color color;
    private HashSet<CallableSBase> referenceableComponents;
    private JButton buttonReferencedComponent;
    private JButton buttonReferenceableComponents;
    private JComboBox comboBoxReferencedComponent;
    private JComboBox comboBoxReferenceableComponents;
    private JLabel labelReferencedComponent;
    private JLabel labelReferenceableComponents;
    private String textReferencedComponent;
    private String textReferenceableComponents;
    
    /**
     * 
     */
    public JPanelComponentMatchEntry() {
      color = WizardProperties
          .getColor("JPANEL_COMPONENT_MATCH_ENTRY_RGB_COLOR_SELECTED_REACTION");
      textReferencedComponent = WizardProperties
          .getText("JPANEL_COMPONENT_MATCH_ENTRY_TEXT_SELECTED_REACTION_SABIO_RK");
      textReferenceableComponents = WizardProperties
          .getText("JPANEL_COMPONENT_MATCH_ENTRY_TEXT_SELECTED_REACTION");
      referencedComponent = kineticLawImporter.getKineticLaw()
          .getParent();
      matchingReferenceableComponent = kineticLawImporter
          .getReaction();
      referenceableComponents = new HashSet<CallableSBase>();
      referenceableComponents.add(kineticLawImporter.getReaction());
      initialize();
      comboBoxReferenceableComponents.setEnabled(false);
    }
    
    
    /**
     * 
     * @param referencedCompartment
     */
    public JPanelComponentMatchEntry(Compartment referencedCompartment) {
      color = WizardProperties
          .getColor("JPANEL_COMPONENT_MATCH_ENTRY_RGB_COLOR_COMPARTMENT");
      textReferencedComponent = WizardProperties
          .getText("JPANEL_COMPONENT_MATCH_ENTRY_TEXT_COMPARTMENT_SABIO_RK");
      textReferenceableComponents = WizardProperties
          .getText("JPANEL_COMPONENT_MATCH_ENTRY_TEXT_COMPARTMENT");
      referencedComponent = referencedCompartment;
      matchingReferenceableComponent = kineticLawImporter
          .getMatchingReferenceableComponent(referencedCompartment);
      referenceableComponents = new HashSet<CallableSBase>(
          kineticLawImporter.getReferenceableCompartments());
      initialize();
    }
    
    
    /**
     * 
     * @param referencedReaction
     */
    public JPanelComponentMatchEntry(Reaction referencedReaction) {
      color = WizardProperties
          .getColor("JPANEL_COMPONENT_MATCH_ENTRY_RGB_COLOR_REACTION");
      textReferencedComponent = WizardProperties
          .getText("JPANEL_COMPONENT_MATCH_ENTRY_TEXT_REACTION_SABIO_RK");
      textReferenceableComponents = WizardProperties
          .getText("JPANEL_COMPONENT_MATCH_ENTRY_TEXT_REACTION");
      referencedComponent = referencedReaction;
      matchingReferenceableComponent = kineticLawImporter
          .getMatchingReferenceableComponent(referencedReaction);
      referenceableComponents = new HashSet<CallableSBase>(
          kineticLawImporter.getReferenceableReactions());
      initialize();
    }
    
    /**
     * 
     * @param referencedSpecies
     */
    public JPanelComponentMatchEntry(Species referencedSpecies) {
      color = WizardProperties
          .getColor("JPANEL_COMPONENT_MATCH_ENTRY_RGB_COLOR_SPECIES");
      textReferencedComponent = WizardProperties
          .getText("JPANEL_COMPONENT_MATCH_ENTRY_TEXT_SPECIES_SABIO_RK");
      textReferenceableComponents = WizardProperties
          .getText("JPANEL_COMPONENT_MATCH_ENTRY_TEXT_SPECIES");
      referencedComponent = referencedSpecies;
      matchingReferenceableComponent = kineticLawImporter
          .getMatchingReferenceableComponent(referencedSpecies);
      referenceableComponents = new HashSet<CallableSBase>(
          kineticLawImporter.getReferenceableSpecies());
      initialize();
    }
    
    /**
     * 
     * @param referencedSpeciesReference
     */
    public JPanelComponentMatchEntry(
      SpeciesReference referencedSpeciesReference) {
      color = WizardProperties
          .getColor("JPANEL_COMPONENT_MATCH_ENTRY_RGB_COLOR_SPECIES_REFERENCE");
      textReferencedComponent = WizardProperties
          .getText("JPANEL_COMPONENT_MATCH_ENTRY_TEXT_SPECIES_REFERENCE_SABIO_RK");
      textReferenceableComponents = WizardProperties
          .getText("JPANEL_COMPONENT_MATCH_ENTRY_TEXT_SPECIES_REFERENCE");
      referencedComponent = referencedSpeciesReference;
      matchingReferenceableComponent = kineticLawImporter
          .getMatchingReferenceableComponent(referencedSpeciesReference);
      referenceableComponents = new HashSet<CallableSBase>(
          kineticLawImporter.getReferenceableSpeciesReferences());
      initialize();
    }
    
    /**
     * 
     */
    private void initialize() {
      labelReferencedComponent = new JLabel(textReferencedComponent);
      labelReferencedComponent.setFont(new Font(getFont().getName(),
        Font.BOLD, getFont().getSize()));
      
      labelReferenceableComponents = new JLabel(
        textReferenceableComponents);
      labelReferenceableComponents.setFont(new Font(getFont()
        .getName(), Font.BOLD, getFont().getSize()));
      
      comboBoxReferencedComponent = new JComboBox();
      comboBoxReferencedComponent.setEnabled(false);
      comboBoxReferencedComponent.addItem(referencedComponent);
      
      comboBoxReferenceableComponents = new JComboBox();
      for (CallableSBase referenceableComponent : referenceableComponents) {
        comboBoxReferenceableComponents.addItem(referenceableComponent);
      }
      comboBoxReferenceableComponents
      .setSelectedItem(matchingReferenceableComponent);
      comboBoxReferenceableComponents.addActionListener(this);
      
      buttonReferencedComponent = new JButton(
        WizardProperties
        .getText("JPANEL_COMPONENT_MATCH_ENTRY_TEXT_SHOW_DETAILS"));
      buttonReferencedComponent.addActionListener(this);
      
      buttonReferenceableComponents = new JButton(
        WizardProperties
        .getText("JPANEL_COMPONENT_MATCH_ENTRY_TEXT_SHOW_DETAILS"));
      buttonReferenceableComponents.addActionListener(this);
      
      setLayout(new GridLayout(0, 2, 30, 0));
      setBorder(new EmptyBorder(new Insets(0, 15, 0, 15)));
      setBackground(color);
      add(labelReferencedComponent);
      add(labelReferenceableComponents);
      add(comboBoxReferencedComponent);
      add(comboBoxReferenceableComponents);
      add(buttonReferencedComponent);
      add(buttonReferenceableComponents);
      
      setComponentDetailsDialogButtonState();
    }
    
    /**
     * Enables the button for showing additional information if the
     * corresponding component exists.
     */
    private void setComponentDetailsDialogButtonState() {
      buttonReferencedComponent.setEnabled(hasReferencedComponent());
      buttonReferenceableComponents
      .setEnabled(hasReferenceableComponent());
    }
    
    /**
     * Returns the referenced component.
     * 
     * @return the referenced component
     */
    private CallableSBase getReferencedComponent() {
      return ((CallableSBase) comboBoxReferencedComponent
          .getSelectedItem());
    }
    
    /**
     * Returns the selected referenceable component.
     * 
     * @return the selected referenceable component
     */
    private CallableSBase getReferenceableComponent() {
      return ((CallableSBase) comboBoxReferenceableComponents
          .getSelectedItem());
    }
    
    /**
     * Checks whether a referenced component exists or not.
     * 
     * @return {@code true} if a referenced component exists;
     *         {@code false} otherwise
     */
    private boolean hasReferencedComponent() {
      return (comboBoxReferencedComponent.getSelectedItem() != null);
    }
    
    /**
     * Checks whether a referenceable component exists or not.
     * 
     * @return {@code true} if a referenceable component exists;
     *         {@code false} otherwise
     */
    private boolean hasReferenceableComponent() {
      return (comboBoxReferenceableComponents.getSelectedItem() != null);
    }
    
    /*
     * (non-Javadoc)
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    @Override
    public void actionPerformed(ActionEvent e) {
      if (e.getSource().equals(comboBoxReferenceableComponents)) {
        if (hasReferencedComponent() && hasReferenceableComponent()) {
          kineticLawImporter.match(getReferencedComponent(),
            getReferenceableComponent());
          setComponentDetailsDialogButtonState();
          setSelectedKineticLawImporter();
        }
      }
      if (e.getSource().equals(buttonReferencedComponent)) {
        if (hasReferencedComponent()) {
          showComponentDetailsDialog(getReferencedComponent());
        }
      }
      if (e.getSource().equals(buttonReferenceableComponents)) {
        if (hasReferenceableComponent()) {
          showComponentDetailsDialog(getReferenceableComponent());
        }
      }
    }
    
  }
  
  /**
   * A class for simple showing of SBML components.
   * 
   * @author Matthias Rall
   * 
   */
  private class JPanelComponentImportEntry extends JPanel implements
  ActionListener {
    
    /**
     * Generated serial version identifier.
     */
    private static final long serialVersionUID = -4757499163593417872L;
    
    private Color color;
    private JButton buttonReferencedComponent;
    private JComboBox comboBoxReferencedComponent;
    private JLabel labelReferencedComponent;
    private SBase referencedComponent;
    private String textReferencedComponent;
    
    /**
     * 
     * @param referencedFunctionDefinition
     */
    public JPanelComponentImportEntry(
      FunctionDefinition referencedFunctionDefinition) {
      color = WizardProperties
          .getColor("JPANEL_COMPONENT_IMPORT_ENTRY_RGB_COLOR_FUNCTION_DEFINITION");
      textReferencedComponent = WizardProperties
          .getText("JPANEL_COMPONENT_IMPORT_ENTRY_TEXT_FUNCTION_DEFINITION");
      referencedComponent = referencedFunctionDefinition;
      initialize();
    }
    
    /**
     * 
     * @param referencedLocalParameter
     */
    public JPanelComponentImportEntry(
      LocalParameter referencedLocalParameter) {
      color = WizardProperties
          .getColor("JPANEL_COMPONENT_IMPORT_ENTRY_RGB_COLOR_LOCAL_PARAMETER");
      textReferencedComponent = WizardProperties
          .getText("JPANEL_COMPONENT_IMPORT_ENTRY_TEXT_LOCAL_PARAMETER");
      referencedComponent = referencedLocalParameter;
      initialize();
    }
    
    /**
     * 
     * @param referencedParameter
     */
    public JPanelComponentImportEntry(Parameter referencedParameter) {
      color = WizardProperties
          .getColor("JPANEL_COMPONENT_IMPORT_ENTRY_RGB_COLOR_PARAMETER");
      textReferencedComponent = WizardProperties
          .getText("JPANEL_COMPONENT_IMPORT_ENTRY_TEXT_PARAMETER");
      referencedComponent = referencedParameter;
      initialize();
    }
    
    /**
     * 
     * @param referencedUnitDefinition
     */
    public JPanelComponentImportEntry(
      UnitDefinition referencedUnitDefinition) {
      color = WizardProperties
          .getColor("JPANEL_COMPONENT_IMPORT_ENTRY_RGB_COLOR_UNIT_DEFINITION");
      textReferencedComponent = WizardProperties
          .getText("JPANEL_COMPONENT_IMPORT_ENTRY_TEXT_UNIT_DEFINITION");
      referencedComponent = referencedUnitDefinition;
      initialize();
    }
    
    /**
     * 
     */
    private void initialize() {
      labelReferencedComponent = new JLabel(textReferencedComponent);
      labelReferencedComponent.setFont(new Font(getFont().getName(),
        Font.BOLD, getFont().getSize()));
      
      comboBoxReferencedComponent = new JComboBox();
      comboBoxReferencedComponent.setEnabled(false);
      comboBoxReferencedComponent.addItem(referencedComponent);
      
      buttonReferencedComponent = new JButton(
        WizardProperties
        .getText("JPANEL_COMPONENT_IMPORT_ENTRY_TEXT_SHOW_DETAILS"));
      buttonReferencedComponent.addActionListener(this);
      
      setLayout(new GridLayout(0, 1));
      setBorder(new EmptyBorder(new Insets(0, 15, 0, 15)));
      setBackground(color);
      add(labelReferencedComponent);
      add(comboBoxReferencedComponent);
      add(buttonReferencedComponent);
      
      setComponentDetailsDialogButtonState();
    }
    
    /**
     * Enables the button for showing additional information if the
     * corresponding component exists.
     */
    private void setComponentDetailsDialogButtonState() {
      buttonReferencedComponent.setEnabled(hasReferencedComponent());
    }
    
    /**
     * Returns the referenced component.
     * 
     * @return the referenced component
     */
    private SBase getReferencedComponent() {
      return ((SBase) comboBoxReferencedComponent.getSelectedItem());
    }
    
    /**
     * Checks whether a referenced component exists or not.
     * 
     * @return {@code true} if a referenced component exists;
     *         {@code false} otherwise
     */
    private boolean hasReferencedComponent() {
      return (comboBoxReferencedComponent.getSelectedItem() != null);
    }
    
    /* (non-Javadoc)
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    @Override
    public void actionPerformed(ActionEvent e) {
      if (e.getSource().equals(buttonReferencedComponent)) {
        if (hasReferencedComponent()) {
          showComponentDetailsDialog(getReferencedComponent());
        }
      }
    }
    
  }
  
}
