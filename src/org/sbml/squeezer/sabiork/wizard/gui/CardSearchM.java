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
package org.sbml.squeezer.sabiork.wizard.gui;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingWorker;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.xml.stream.XMLStreamException;
import org.sbml.jsbml.KineticLaw;
import org.sbml.squeezer.sabiork.wizard.gui.JDialogWizard.ButtonState;
import org.sbml.squeezer.sabiork.wizard.gui.JDialogWizard.CardID;

import org.sbml.squeezer.sabiork.SABIORK;
import org.sbml.squeezer.sabiork.util.Pair;
import org.sbml.squeezer.sabiork.util.WebServiceConnectException;
import org.sbml.squeezer.sabiork.util.WebServiceResponseException;
import org.sbml.squeezer.sabiork.wizard.model.WizardModel;
import org.sbml.squeezer.sabiork.wizard.model.WizardProperties;

/**
 * A class that allows the manual search in the SABIO-RK wizard.
 * 
 * @author Matthias Rall
 * @version $Rev$
 * ${tags}
 */
@SuppressWarnings("serial")
public class CardSearchM extends Card implements ActionListener,
		PropertyChangeListener, ListSelectionListener, TableModelListener {

	public enum SearchState {
		START, DONE, CANCEL, RESET
	}

	private ComboBoxModelSearchItems comboBoxSearchItemsModel;
	private JButton buttonSearch;
	private JComboBox comboBoxSearchItems;
	private JComboBoxSearchField comboBoxSearchField;
	private JLabel labelResults;
	private JPanel panelSearch;
	private JPanel panelSearchInput;
	private JPanel panelResults;
	private JPanelFilterOptions panelFilterOptions;
	private JProgressBar progressBar;
	private JScrollPane tableSearchTermsScrollPane;
	private JScrollPane tableResultsScrollPane;
	private JTable tableSearchTerms;
	private JTable tableResults;
	private Search search;
	private TableModelSearchMResults tableResultsModel;
	private TableModelSearchTerms tableSearchTermsModel;

	public CardSearchM(JDialogWizard dialog, WizardModel model) {
		super(dialog, model);
		model.addPropertyChangeListener(this);
		initialize();
	}

	private void initialize() {
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
		panelFilterOptions.addPropertyChangeListener(this);

		tableSearchTermsModel = new TableModelSearchTerms();
		tableSearchTermsModel.addTableModelListener(this);
		tableSearchTerms = new JTable(tableSearchTermsModel);
		tableSearchTerms.setPreferredScrollableViewportSize(panelFilterOptions
				.getSize());
		tableSearchTermsScrollPane = new JScrollPane(tableSearchTerms);
		tableSearchTermsScrollPane.setBorder(BorderFactory.createTitledBorder(
				BorderFactory.createEtchedBorder(),
				WizardProperties.getText("CARD_SEARCH_M_TEXT_QUERY")));
		tableSearchTermsScrollPane.setBackground(getBackground());

		progressBar = new JProgressBar(0, 100);
		progressBar.setStringPainted(true);

		labelResults = new JLabel(MessageFormat.format(WizardProperties
				.getText("CARD_SEARCH_M_TEXT_THERE_ARE_X_KINETIC_LAW_ENTRIES"),
				0));
		labelResults.setBorder(new EmptyBorder(new Insets(5, 5, 15, 0)));

		tableResultsModel = new TableModelSearchMResults();
		tableResults = new JTable(tableResultsModel);
		tableResults.setRowSelectionAllowed(true);
		tableResults.setColumnSelectionAllowed(false);
		tableResults.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		tableResults.getSelectionModel().addListSelectionListener(this);
		tableResults.getColumnModel().getColumn(1)
				.setCellRenderer(new TableCellRendererReactions());
		tableResults.getColumnModel().getColumn(2)
				.setCellRenderer(new TableCellRendererTemperature());
		tableResults.getColumnModel().getColumn(3)
				.setCellRenderer(new TableCellRendererpHValues());
		tableResultsScrollPane = new JScrollPane(tableResults);

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
		panelSearch.add(progressBar, new GridBagConstraints(0, 2, 2, 1, 1.0,
				0.0, GridBagConstraints.PAGE_START,
				GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));

		panelResults = new JPanel(new BorderLayout());
		panelResults.setBorder(BorderFactory.createTitledBorder(
				BorderFactory.createEtchedBorder(),
				WizardProperties.getText("CARD_SEARCH_M_TEXT_RESULTS")));
		panelResults.add(labelResults, BorderLayout.NORTH);
		panelResults.add(tableResultsScrollPane, BorderLayout.CENTER);

		setLayout(new BorderLayout());
		add(panelSearch, BorderLayout.NORTH);
		add(panelResults, BorderLayout.CENTER);

		search = null;
	}

	public void performBeforeShowing() {
		dialog.setButtonState(ButtonState.NEXT_DISABLED);
		comboBoxSearchItems.setSelectedItem(SABIORK.QueryField.ENTRY_ID);
		comboBoxSearchField.setText("");
		setSearchState(SearchState.RESET);
		performSelectedReactionKeggIDSearch();
	};

	public CardID getPreviousCardID() {
		return CardID.REACTIONS_M;
	}

	public CardID getNextCardID() {
		return CardID.MATCHING;
	}

	/**
	 * Adds the selected {@link KineticLaw} to the model.
	 */
	private void setSelectedKineticLaw() {
		KineticLaw selectedKineticLaw = null;
		int selectedRow = tableResults.getSelectedRow();
		if (selectedRow != -1) {
			selectedKineticLaw = tableResultsModel.getKineticLaws().get(
					selectedRow);
		}
		model.setSelectedKineticLaw(selectedKineticLaw);
	}

	/**
	 * Sets different properties of the interface according to the current state
	 * of the search.
	 * 
	 * @param searchState
	 */
	private void setSearchState(SearchState searchState) {
		switch (searchState) {
		case START:
			buttonSearch.setText(WizardProperties
					.getText("CARD_SEARCH_M_TEXT_BUTTON_CANCEL"));
			progressBar.setValue(0);
			progressBar.setIndeterminate(true);
			progressBar.setStringPainted(false);
			tableResultsModel.clear();
			setResultCount(0);
			break;
		case DONE:
			buttonSearch.setText(WizardProperties
					.getText("CARD_SEARCH_M_TEXT_BUTTON_SEARCH"));
			progressBar.setIndeterminate(false);
			break;
		case CANCEL:
			buttonSearch.setText(WizardProperties
					.getText("CARD_SEARCH_M_TEXT_BUTTON_SEARCH"));
			if (progressBar.isIndeterminate()) {
				progressBar.setValue(0);
				progressBar.setIndeterminate(false);
			}
			break;
		case RESET:
			buttonSearch.setText(WizardProperties
					.getText("CARD_SEARCH_M_TEXT_BUTTON_SEARCH"));
			progressBar.setValue(0);
			progressBar.setIndeterminate(false);
			progressBar.setStringPainted(false);
			tableResultsModel.clear();
			setResultCount(0);
			tableSearchTermsModel.clear();
			break;
		}
	}

	/**
	 * Displays the number of results found.
	 * 
	 * @param resultCount
	 */
	private void setResultCount(int resultCount) {
		labelResults.setText(MessageFormat.format(WizardProperties
				.getText("CARD_SEARCH_M_TEXT_THERE_ARE_X_KINETIC_LAW_ENTRIES"),
				resultCount));
	}

	/**
	 * Returns the SABIO-RK query according to the selected search item.
	 * 
	 * @return
	 */
	private String getQuery() {
		StringBuilder query = new StringBuilder();
		String filterOptionsQuery = panelFilterOptions.getFilterOptionsQuery();
		if (comboBoxSearchItemsModel.isQueryFieldSelected()) {
			String searchTermsQuery = tableSearchTermsModel
					.getSearchTermsQuery();
			if (!searchTermsQuery.isEmpty()) {
				query.append(searchTermsQuery);
				query.append(filterOptionsQuery);
			}
		}
		if (comboBoxSearchItemsModel.isQueryExpressionSelected()) {
			String queryExpression = comboBoxSearchField.getText().trim();
			if (!queryExpression.isEmpty()) {
				query.append(queryExpression);
				query.append(filterOptionsQuery);
			}
		}
		return query.toString();
	}

	/**
	 * Adds the current search term to the table if it is not empty.
	 */
	private void addCurrentSearchTerm() {
		String currentTextTrimmed = comboBoxSearchField.getText().trim();
		if (!currentTextTrimmed.isEmpty()
				&& comboBoxSearchItemsModel.isQueryFieldSelected()) {
			tableSearchTermsModel.add(new Pair<SABIORK.QueryField, String>(
					comboBoxSearchItemsModel.getSelectedQueryField(),
					currentTextTrimmed));
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
				tableSearchTermsModel.add(new Pair<SABIORK.QueryField, String>(
						SABIORK.QueryField.KEGG_REACTION_ID, keggReactionID));
				startSearch();
			}
		}
	}

	/**
	 * Starts a new search.
	 */
	private void startSearch() {
		if (search != null && search.isStarted()) {
			search.cancel();
		}
		String query = getQuery();
		if (!query.isEmpty()) {
			setSearchState(SearchState.START);
			search = new Search(query);
			search.addPropertyChangeListener(this);
			search.start();
		}
	}

	/**
	 * Cancels the search and resets different properties of the interface.
	 */
	private void resetSearch() {
		if (search != null && search.isStarted()) {
			search.reset();
		}
		setSearchState(SearchState.RESET);
	}

	public void actionPerformed(ActionEvent e) {
		if (e.getSource().equals(buttonSearch)) {
			if (search != null && search.isStarted()) {
				search.cancel();
			} else {
				addCurrentSearchTerm();
				startSearch();
			}
		}
		if (e.getSource().equals(comboBoxSearchItems)) {
			if (comboBoxSearchItemsModel.isQueryFieldSelected()) {
				comboBoxSearchField
						.setSuggestionQueryField(comboBoxSearchItemsModel
								.getSelectedQueryField());
			} else {
				comboBoxSearchField.setSuggestionQueryField(null);
				resetSearch();
			}
		}
	}

	public void propertyChange(PropertyChangeEvent e) {
		if (e.getSource().equals(search)
				&& e.getPropertyName().equals("progress")) {
			progressBar.setValue((Integer) e.getNewValue());
		}
		if (e.getSource().equals(panelFilterOptions)) {
			startSearch();
		}
		if (e.getSource().equals(model)
				&& e.getPropertyName().equals("selectedKineticLaw")) {
			if (model.hasSelectedKineticLaw()) {
				dialog.setButtonState(ButtonState.NEXT_ENABLED);
			} else {
				dialog.setButtonState(ButtonState.NEXT_DISABLED);
			}
		}
	}

	public void valueChanged(ListSelectionEvent e) {
		if (e.getSource().equals(tableResults.getSelectionModel())) {
			if (!e.getValueIsAdjusting()) {
				setSelectedKineticLaw();
			}
		}
	}

	public void tableChanged(TableModelEvent e) {
		if (e.getSource().equals(tableSearchTermsModel)) {
			if (e.getType() == TableModelEvent.UPDATE) {
				int row = e.getFirstRow();
				int column = e.getColumn();
				if (column == 2) {
					tableSearchTermsModel.remove(row);
					if (tableSearchTermsModel.getRowCount() == 0) {
						resetSearch();
					} else {
						startSearch();
					}
				}
			}
		}
	}

	/**
	 * A class to perform the search.
	 * 
	 * @author Matthias Rall
	 * 
	 */
	private class Search extends SwingWorker<Void, KineticLaw> {

		private String query;
		private int resultCount;
		private SearchState finalSearchState;

		public Search(String query) {
			this.query = query;
			this.resultCount = 0;
			this.finalSearchState = SearchState.DONE;
		}

		/**
		 * Checks if this search is already in progress.
		 * 
		 * @return <code>true</code> if this search is already in progress,
		 *         <code>false</code> otherwise
		 */
		public boolean isStarted() {
			return (getState() == StateValue.STARTED);
		}

		/**
		 * Starts this search.
		 */
		public void start() {
			finalSearchState = SearchState.DONE;
			execute();
		}

		/**
		 * Cancels this search.
		 */
		public void cancel() {
			finalSearchState = SearchState.CANCEL;
			cancel(true);
		}

		/**
		 * Cancels this search and resets different properties of the interface.
		 */
		public void reset() {
			finalSearchState = SearchState.RESET;
			cancel(true);
			setProgress(0);
		}

		/**
		 * Performs the manual search process.
		 */
		protected Void doInBackground() {
			List<Integer> ids = new ArrayList<Integer>();
			try {
				ids = SABIORK.getIDs(query);
			} catch (WebServiceConnectException e) {
				JDialogWizard.showErrorDialog(e);
				e.printStackTrace();
			} catch (WebServiceResponseException e) {
				JDialogWizard.showErrorDialog(e);
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (XMLStreamException e) {
				e.printStackTrace();
			}
			int idCount = ids.size();
			for (int i = 0; i < idCount; i++) {
				Integer id = ids.get(i);
				KineticLaw kineticLaw = null;
				try {
					kineticLaw = SABIORK.getKineticLaw(id);
				} catch (WebServiceConnectException e) {
					JDialogWizard.showErrorDialog(e);
					e.printStackTrace();
				} catch (WebServiceResponseException e) {
					JDialogWizard.showErrorDialog(e);
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				} catch (XMLStreamException e) {
					e.printStackTrace();
				}
				if (kineticLaw != null) {
					if (!isCancelled()) {
						publish(kineticLaw);
						setProgress(Math.round(Float.valueOf(i + 1) / idCount
								* 100));
					} else {
						break;
					}
				}
			}
			return null;
		}

		/**
		 * Displays the results.
		 */
		protected void process(List<KineticLaw> chunks) {
			progressBar.setIndeterminate(false);
			progressBar.setStringPainted(true);
			if (!isCancelled()) {
				for (KineticLaw kineticLaw : chunks) {
					tableResultsModel.add(kineticLaw);
					resultCount++;
					setResultCount(resultCount);
				}
			} else {
				setSearchState(finalSearchState);
			}
		}

		protected void done() {
			try {
				setSearchState(finalSearchState);
				get();
			} catch (CancellationException e) {
			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (ExecutionException e) {
				e.printStackTrace();
			}
		}

	}

}
