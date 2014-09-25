/*
 * $Id: CardReactionsM.java 1082 2014-02-22 23:54:18Z draeger $
 * $URL: https://rarepos.cs.uni-tuebingen.de/svn-path/SBMLsqueezer/trunk/src/org/sbml/squeezer/sabiork/wizard/gui/CardReactionsM.java $
 * ---------------------------------------------------------------------
 * This file is part of SBMLsqueezer, a Java program that creates rate
 * equations for reactions in SBML files (http://sbml.org).
 *
 * Copyright (C) 2006-2014 by the University of Tuebingen, Germany.
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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import org.sbml.jsbml.Reaction;
import org.sbml.squeezer.sabiork.wizard.gui.ComboBoxModelReactionFilters.ReactionFilter;
import org.sbml.squeezer.sabiork.wizard.gui.JDialogWizard.ButtonState;
import org.sbml.squeezer.sabiork.wizard.gui.JDialogWizard.CardID;

import org.sbml.squeezer.sabiork.wizard.model.WizardModel;

/**
 * @author Matthias Rall
 * @version $Rev: 1082 $
 * @since 2.0
 */
public class CardReactionsM extends Card implements ListSelectionListener,
ActionListener, PropertyChangeListener {
  
  /**
   * Generated serial version identifier.
   */
  private static final long serialVersionUID = -2273479139811227167L;
  //	private ComboBoxModelReactionFilters comboBoxReactionFiltersModel;
  private JComboBox comboBoxReactionFilters;
  private JScrollPane tableReactionsScrollPane;
  private JTable tableReactions;
  private TableModelReactions tableReactionsModel;
  
  /**
   * 
   * @param dialog
   * @param model
   */
  public CardReactionsM(JDialogWizard dialog, WizardModel model) {
    super(dialog, model);
    model.addPropertyChangeListener(this);
    initialize();
  }
  
  /**
   * 
   */
  private void initialize() {
    //		comboBoxReactionFiltersModel = new ComboBoxModelReactionFilters();
    //		comboBoxReactionFilters = new JComboBox(comboBoxReactionFiltersModel);
    //		comboBoxReactionFilters.setRenderer(comboBoxReactionFiltersModel.getRenderer());
    //		comboBoxReactionFilters.addActionListener(this);
    
    tableReactionsModel = new TableModelReactions();
    tableReactions = new JTable(tableReactionsModel);
    tableReactions.setRowSelectionAllowed(true);
    tableReactions.setColumnSelectionAllowed(false);
    tableReactions.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    tableReactions.getSelectionModel().addListSelectionListener(this);
    tableReactions.getColumnModel().getColumn(1).setCellRenderer(new TableCellRendererReactions());
    tableReactions.getColumnModel().getColumn(2).setCellRenderer(new TableCellRendererBooleans());
    tableReactions.getColumnModel().getColumn(3).setCellRenderer(new TableCellRendererBooleans());
    tableReactions.getColumnModel().getColumn(4).setCellRenderer(new TableCellRendererBooleans());
    tableReactions.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
    tableReactionsScrollPane = new JScrollPane(tableReactions);
    
    setLayout(new BorderLayout());
    setBorder(BorderFactory.createTitledBorder(
      BorderFactory.createEtchedBorder(),
        "Selected reaction"));
    //add(comboBoxReactionFilters, BorderLayout.NORTH);
    add(tableReactionsScrollPane, BorderLayout.CENTER);
  }
  
  /* (non-Javadoc)
   * @see org.sbml.squeezer.sabiork.wizard.gui.Card#performBeforeShowing()
   */
  @Override
  public void performBeforeShowing() {
    dialog.setButtonState(ButtonState.START);
    tableReactionsModel
    .setReactions(model.getReactions());
    if (tableReactionsModel.getReactions().size() > 0) {
      tableReactions.getSelectionModel().setSelectionInterval(0, 0);
    }
    setSelectedReaction();
  }
  
  /* (non-Javadoc)
   * @see org.sbml.squeezer.sabiork.wizard.gui.Card#getPreviousCardID()
   */
  @Override
  public CardID getPreviousCardID() {
    return CardID.METHOD;
  }
  
  /* (non-Javadoc)
   * @see org.sbml.squeezer.sabiork.wizard.gui.Card#getNextCardID()
   */
  @Override
  public CardID getNextCardID() {
    return CardID.SEARCH_M;
  }
  
  /**
   * Adds the selected {@link Reaction} to the model.
   */
  private void setSelectedReaction() {
    Reaction selectedReaction = null;
    int selectedRow = tableReactions.getSelectedRow();
    if (selectedRow != -1) {
      selectedReaction = tableReactionsModel.getReactions().get(
        selectedRow);
    }
    model.setSelectedReaction(selectedReaction);
  }
  
  /**
   * Returns the reactions according to the {@code reactionFilter}.
   * 
   * @param reactionFilter
   * @return a list of {@link Reaction}
   */
  public List<Reaction> getFilteredReactions(ReactionFilter reactionFilter) {
    List<Reaction> reactions = new ArrayList<Reaction>();
    switch (reactionFilter) {
      case ALL_REACTIONS:
        reactions = model.getReactions();
        break;
      case REACTIONS_WITH_KINETICLAW:
        reactions = model.getReactionsWithKineticLaw();
        break;
      case REACTIONS_WITHOUT_KINETICLAW:
        reactions = model.getReactionsWithoutKineticLaw();
        break;
      case REVERSIBLE_REACTIONS:
        reactions = model.getReversibleReactions();
        break;
      case IRREVERSIBLE_REACTIONS:
        reactions = model.getIrreversibleReactions();
        break;
      case FAST_REACTIONS:
        reactions = model.getFastReactions();
        break;
      case SLOW_REACTIONS:
        reactions = model.getSlowReactions();
        break;
    }
    return reactions;
  }
  
  /* (non-Javadoc)
   * @see javax.swing.event.ListSelectionListener#valueChanged(javax.swing.event.ListSelectionEvent)
   */
  @Override
  public void valueChanged(ListSelectionEvent e) {
    if (e.getSource().equals(tableReactions.getSelectionModel())) {
      if (!e.getValueIsAdjusting()) {
        setSelectedReaction();
      }
    }
  }
  
  /* (non-Javadoc)
   * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
   */
  @Override
  public void actionPerformed(ActionEvent e) {
    if (e.getSource().equals(comboBoxReactionFilters)) {
      tableReactionsModel
      .setReactions(model.getReactions());
      if (tableReactionsModel.getReactions().size() > 0) {
        tableReactions.getSelectionModel().setSelectionInterval(0, 0);
      }
      setSelectedReaction();
    }
  }
  
  /* (non-Javadoc)
   * @see java.beans.PropertyChangeListener#propertyChange(java.beans.PropertyChangeEvent)
   */
  @Override
  public void propertyChange(PropertyChangeEvent e) {
    if (e.getSource().equals(model)
        && e.getPropertyName().equals("selectedReaction")) {
      if (model.hasSelectedReaction()) {
        dialog.setButtonState(ButtonState.NEXT_ENABLED);
      } else {
        dialog.setButtonState(ButtonState.START);
      }
    }
  }
  
}
