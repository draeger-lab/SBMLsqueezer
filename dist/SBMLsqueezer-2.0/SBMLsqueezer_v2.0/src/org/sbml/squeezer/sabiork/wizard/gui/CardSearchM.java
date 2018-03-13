/*
 * $Id: CardSearchM.java 1082 2014-02-22 23:54:18Z draeger $
 * $URL: https://rarepos.cs.uni-tuebingen.de/svn-path/SBMLsqueezer/trunk/src/org/sbml/squeezer/sabiork/wizard/gui/CardSearchM.java $
 * ---------------------------------------------------------------------
 * This file is part of SBMLsqueezer, a Java program that creates rate
 * equations for reactions in SBML files (http://sbml.org).
 *
 * Copyright (C) 2006-2018 by the University of Tuebingen, Germany.
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
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import org.sbml.jsbml.util.ValuePair;
import org.sbml.squeezer.sabiork.wizard.gui.JDialogWizard.ButtonState;
import org.sbml.squeezer.sabiork.wizard.gui.JDialogWizard.CardID;

import org.sbml.squeezer.sabiork.SABIORK;
import org.sbml.squeezer.sabiork.wizard.model.WizardModel;
import org.sbml.squeezer.sabiork.wizard.model.WizardProperties;

/**
 * A class that allows the manual search in the SABIO-RK wizard.
 * 
 * @author Matthias Rall
 * @version $Rev: 1082 $
 * @since 2.0
 */
public class CardSearchM extends Card implements ActionListener,
TableModelListener, ChangeListener {
  
  /**
   * Generated serial version identifier.
   */
  private static final long serialVersionUID = -6832464267968071096L;
  
  private JButton buttonSearch;
  private JComboBox comboBoxSearchItems;
  private JPanel panelSearch;
  private JPanel panelSearchInput;
  private JScrollPane tableSearchTermsScrollPane;
  private JTable tableSearchTerms;
  
  /**
   * 
   * @param dialog
   * @param model
   * @throws IOException
   */
  public CardSearchM(JDialogWizard dialog, WizardModel model) throws IOException {
    super(dialog, model);
    initialize();
  }
  
  /**
   * @throws IOException
   * 
   */
  private void initialize() throws IOException {
    comboBoxSearchItemsModel = new ComboBoxModelSearchItems();
    comboBoxSearchItems = new JComboBox(comboBoxSearchItemsModel);
    comboBoxSearchItems.setRenderer(comboBoxSearchItemsModel.getRenderer());
    comboBoxSearchItems.addActionListener(this);
    
    comboBoxSearchField = new JComboBoxSearchField();
    comboBoxSearchField.setSuggestionQueryField(comboBoxSearchItemsModel
      .getSelectedQueryField());
    
    buttonSearch = new JButton(
      WizardProperties.getText("CARD_SEARCH_M_TEXT_BUTTON_SEARCH"));
    buttonSearch.addActionListener(this);
    
    panelFilterOptions = new JPanelFilterOptions();
    panelFilterOptions.addChangeListener(this);
    
    tableSearchTermsModelM = new TableModelSearchTerms();
    tableSearchTermsModelM.addTableModelListener(this);
    tableSearchTerms = new JTable(tableSearchTermsModelM);
    tableSearchTerms.setPreferredScrollableViewportSize(panelFilterOptions
      .getSize());
    tableSearchTerms.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
    tableSearchTermsScrollPane = new JScrollPane(tableSearchTerms);
    tableSearchTermsScrollPane.setBorder(BorderFactory.createTitledBorder(
      BorderFactory.createEtchedBorder(),
      WizardProperties.getText("CARD_SEARCH_M_TEXT_QUERY")));
    tableSearchTermsScrollPane.setBackground(getBackground());
    
    panelSearchInput = new JPanel(new BorderLayout());
    panelSearchInput.add(comboBoxSearchItems, BorderLayout.WEST);
    panelSearchInput.add(comboBoxSearchField, BorderLayout.CENTER);
    panelSearchInput.add(buttonSearch, BorderLayout.EAST);
    
    panelSearch = new JPanel(new GridBagLayout());
    panelSearch.setBorder(BorderFactory.createTitledBorder(
      BorderFactory.createEtchedBorder(),
      WizardProperties.getText("CARD_SEARCH_M_TEXT_SEARCH")));
    panelSearch.add(panelSearchInput, new GridBagConstraints(0, 0, 2, 1,
      1.0, 0.0, GridBagConstraints.PAGE_START,
      GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
    panelSearch.add(tableSearchTermsScrollPane, new GridBagConstraints(0,
      1, 1, 1, 1.0, 0.0, GridBagConstraints.PAGE_START,
      GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
    panelSearch.add(panelFilterOptions, new GridBagConstraints(1, 1, 1, 1,
      0.0, 0.0, GridBagConstraints.PAGE_START,
      GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
    setLayout(new BorderLayout());
    add(panelSearch, BorderLayout.CENTER);
  }
  
  /*
   * (non-Javadoc)
   * @see org.sbml.squeezer.sabiork.wizard.gui.Card#performBeforeShowing()
   */
  @Override
  public void performBeforeShowing() {
    if ((tableSearchTermsModelM.getSearchTerms() != null) && (tableSearchTermsModelM.getSearchTerms().size() > 0)) {
      dialog.setButtonState(ButtonState.NEXT_BACK_ENABLED);
    }
    else {
      dialog.setButtonState(ButtonState.NEXT_DISABLED);
    }
    comboBoxSearchItems.setSelectedItem(SABIORK.QueryField.ENTRY_ID);
    comboBoxSearchField.setText("");
    tableSearchTermsModelM.loadSettings();
    performSelectedReactionKeggIDSearch();
  }
  
  /*
   * (non-Javadoc)
   * @see org.sbml.squeezer.sabiork.wizard.gui.Card#getPreviousCardID()
   */
  @Override
  public CardID getPreviousCardID() {
    return CardID.REACTIONS_M;
  }
  
  /*
   * (non-Javadoc)
   * @see org.sbml.squeezer.sabiork.wizard.gui.Card#getNextCardID()
   */
  @Override
  public CardID getNextCardID() {
    return CardID.SEARCHRESULTS_M;
  }
  
  /**
   * Adds the current search term to the table if it is not empty.
   */
  private void addCurrentSearchTerm() {
    String currentTextTrimmed = comboBoxSearchField.getText().trim();
    if (!currentTextTrimmed.isEmpty()
        && comboBoxSearchItemsModel.isQueryFieldSelected()) {
      tableSearchTermsModelM.add(new ValuePair<SABIORK.QueryField, String>(
          comboBoxSearchItemsModel.getSelectedQueryField(),
          currentTextTrimmed));
      dialog.setButtonState(ButtonState.NEXT_BACK_ENABLED);
      
    }
  }
  
  /**
   * Performs a search if the KeggID of the selected {@link Reaction} exists.
   */
  private void performSelectedReactionKeggIDSearch() {
    if (model.hasSelectedReaction()) {
      String keggReactionID = model.getKeggReactionID(model
        .getSelectedReaction());
      if (!keggReactionID.isEmpty()) {
        tableSearchTermsModelM.add(new ValuePair<SABIORK.QueryField, String>(
            SABIORK.QueryField.KEGG_REACTION_ID, keggReactionID));
        dialog.setButtonState(ButtonState.NEXT_BACK_ENABLED);
      }
    }
  }
  
  /*
   * (non-Javadoc)
   * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
   */
  @Override
  public void actionPerformed(ActionEvent e) {
    if (e.getSource().equals(buttonSearch)) {
      addCurrentSearchTerm();
    }
    if (e.getSource().equals(comboBoxSearchItems)) {
      if (comboBoxSearchItemsModel.isQueryFieldSelected()) {
        comboBoxSearchField
        .setSuggestionQueryField(comboBoxSearchItemsModel
          .getSelectedQueryField());
      } else {
        comboBoxSearchField.setSuggestionQueryField(null);
      }
    }
  }
  
  
  /*
   * (non-Javadoc)
   * @see javax.swing.event.TableModelListener#tableChanged(javax.swing.event.TableModelEvent)
   */
  @Override
  public void tableChanged(TableModelEvent e) {
    if (e.getSource().equals(tableSearchTermsModelM)) {
      if (e.getType() == TableModelEvent.UPDATE) {
        int row = e.getFirstRow();
        int column = e.getColumn();
        if (column == 2) {
          tableSearchTermsModelM.remove(row);
          if (tableSearchTermsModelM.getSearchTerms().size() == 0) {
            dialog.setButtonState(ButtonState.NEXT_DISABLED);
          }
        }
      }
    }
  }
  
  /*
   * (non-Javadoc)
   * @see org.sbml.squeezer.sabiork.wizard.gui.Card#performAfterPressingBack()
   */
  @Override
  public void performAfterPressingBack() {
    panelFilterOptions.saveSettings();
    tableSearchTermsModelM.saveSettings();
  }
  
  /*
   * (non-Javadoc)
   * @see org.sbml.squeezer.sabiork.wizard.gui.Card#performAfterCancel()
   */
  @Override
  public void performAfterCancel() {
    panelFilterOptions.saveSettings();
    tableSearchTermsModelM.saveSettings();
  }
  
  /*
   * (non-Javadoc)
   * @see org.sbml.squeezer.sabiork.wizard.gui.Card#performAfterNext()
   */
  @Override
  public void performAfterNext() {
    panelFilterOptions.saveSettings();
    tableSearchTermsModelM.saveSettings();
  }
  
  /* (non-Javadoc)
   * @see javax.swing.event.ChangeListener#stateChanged(javax.swing.event.ChangeEvent)
   */
  @Override
  public void stateChanged(ChangeEvent e) {
    if ((!panelFilterOptions.isUserConfiguration())) {
      panelFilterOptions.saveSettings();
      tableSearchTermsModelM.saveSettings();
    }
  }
  
}
