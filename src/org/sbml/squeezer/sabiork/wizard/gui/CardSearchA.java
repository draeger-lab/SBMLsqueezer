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
 * A class that allows the automatic search in the SABIO-RK wizard.
 * 
 * @author Matthias Rall
 * @version $Rev$
 */
public class CardSearchA extends Card implements ActionListener,
		TableModelListener, ChangeListener {

	/**
	 * Generated serial version identifier.
	 */
	private static final long serialVersionUID = 3025182595373189918L;

	private ComboBoxModelConstraints comboBoxConstraintsModel;
	private JButton buttonAdd;
	private JComboBox comboBoxConstraints;
	private JComboBoxSearchField comboBoxSearchField;
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
	public CardSearchA(JDialogWizard dialog, WizardModel model) throws IOException {
		super(dialog, model);
		initialize();
	}

	/**
	 * Initializes the card.
	 * @throws IOException 
	 */
	private void initialize() throws IOException {
		comboBoxConstraintsModel = new ComboBoxModelConstraints();
		comboBoxConstraints = new JComboBox(comboBoxConstraintsModel);
		comboBoxConstraints.setRenderer(comboBoxConstraintsModel.getRenderer());
		comboBoxConstraints.addActionListener(this);

		comboBoxSearchField = new JComboBoxSearchField();
		comboBoxSearchField.setSuggestionQueryField(comboBoxConstraintsModel
				.getSelectedQueryField());

		buttonAdd = new JButton(
				WizardProperties.getText("CARD_SEARCH_A_TEXT_BUTTON_ADD"));
		buttonAdd.addActionListener(this);

		panelFilterOptions = new JPanelFilterOptions();
		panelFilterOptions.addChangeListener(this);

		tableSearchTermsModelA = new TableModelSearchTerms();
		tableSearchTermsModelA.addTableModelListener(this);
		tableSearchTerms = new JTable(tableSearchTermsModelA);
		tableSearchTerms.setPreferredScrollableViewportSize(panelFilterOptions
				.getSize());
		tableSearchTermsScrollPane = new JScrollPane(tableSearchTerms);
		tableSearchTermsScrollPane.setBorder(BorderFactory.createTitledBorder(
				BorderFactory.createEtchedBorder(),
				WizardProperties.getText("CARD_SEARCH_A_TEXT_CONSTRAINTS")));
		tableSearchTerms.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		tableSearchTermsScrollPane.setBackground(getBackground());

		
		panelSearchInput = new JPanel(new BorderLayout());
		panelSearchInput.add(comboBoxConstraints, BorderLayout.WEST);
		panelSearchInput.add(comboBoxSearchField, BorderLayout.CENTER);
		panelSearchInput.add(buttonAdd, BorderLayout.EAST);

		panelSearch = new JPanel(new GridBagLayout());
		panelSearch.setBorder(BorderFactory.createTitledBorder(
				BorderFactory.createEtchedBorder(),
				WizardProperties.getText("CARD_SEARCH_A_TEXT_SEARCH")));
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
	public void performBeforeShowing() {
		dialog.setButtonState(ButtonState.NEXT_BACK_ENABLED);
		comboBoxConstraints.setSelectedItem(SABIORK.QueryField.PATHWAY);
		comboBoxSearchField.setText("");
		tableSearchTermsModelA.loadSettings();
	}

	/*
	 * (non-Javadoc)
	 * @see org.sbml.squeezer.sabiork.wizard.gui.Card#getPreviousCardID()
	 */
	public CardID getPreviousCardID() {
		return CardID.REACTIONS_A;
	}

	/*
	 * (non-Javadoc)
	 * @see org.sbml.squeezer.sabiork.wizard.gui.Card#getNextCardID()
	 */
	public CardID getNextCardID() {
		return CardID.SEARCHRESULTS_A;
	}


	/**
	 * Adds the current search term to the table if it is not empty.
	 */
	private void addCurrentSearchTerm() {
		String currentTextTrimmed = comboBoxSearchField.getText().trim();
		if (!currentTextTrimmed.isEmpty()
				&& comboBoxConstraintsModel.isQueryFieldSelected()) {
			tableSearchTermsModelA.add(new ValuePair<SABIORK.QueryField, String>(
					comboBoxConstraintsModel.getSelectedQueryField(),
					currentTextTrimmed));
		}
	}

	/*
	 * (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent e) {
		if (e.getSource().equals(buttonAdd)) {
			if (!comboBoxSearchField.getText().trim().isEmpty()) {
				addCurrentSearchTerm();
				dialog.setButtonState(ButtonState.NEXT_BACK_ENABLED);
			}
		}
		if (e.getSource().equals(comboBoxConstraints)) {
			if (comboBoxConstraintsModel.isQueryFieldSelected()) {
				comboBoxSearchField
						.setSuggestionQueryField(comboBoxConstraintsModel
								.getSelectedQueryField());
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * @see javax.swing.event.TableModelListener#tableChanged(javax.swing.event.TableModelEvent)
	 */
	public void tableChanged(TableModelEvent e) {
		if (e.getSource().equals(tableSearchTermsModelA)) {
			if (e.getType() == TableModelEvent.UPDATE) {
				int row = e.getFirstRow();
				int column = e.getColumn();
				if (column == 2) {
					tableSearchTermsModelA.remove(row);
					dialog.setButtonState(ButtonState.NEXT_BACK_ENABLED);
				}
			}
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.sbml.squeezer.sabiork.wizard.gui.Card#performAfterPressingBack()
	 */
	public void performAfterPressingBack() {
		panelFilterOptions.saveSettings();
		tableSearchTermsModelA.saveSettings();
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.sbml.squeezer.sabiork.wizard.gui.Card#performAfterCancel()
	 */
	public void performAfterCancel() {
		panelFilterOptions.saveSettings();
		tableSearchTermsModelA.saveSettings();
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.sbml.squeezer.sabiork.wizard.gui.Card#performAfterNext()
	 */
	public void performAfterNext() {
		panelFilterOptions.saveSettings();
		tableSearchTermsModelA.saveSettings();
	}

	/* (non-Javadoc)
	 * @see javax.swing.event.ChangeListener#stateChanged(javax.swing.event.ChangeEvent)
	 */
	public void stateChanged(ChangeEvent e) {
		if ((!panelFilterOptions.isUserConfiguration())) {
			panelFilterOptions.saveSettings();
			tableSearchTermsModelA.saveSettings();
		}	
	}

}
