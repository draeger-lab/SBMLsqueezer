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
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingWorker;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.xml.stream.XMLStreamException;
import org.sbml.jsbml.KineticLaw;
import org.sbml.jsbml.Reaction;
import org.sbml.squeezer.sabiork.wizard.gui.JDialogWizard.ButtonState;
import org.sbml.squeezer.sabiork.wizard.gui.JDialogWizard.CardID;
import org.sbml.squeezer.sabiork.wizard.gui.TableModelSearchAResults.SearchAResult;

import org.sbml.squeezer.sabiork.SABIORK;
import org.sbml.squeezer.sabiork.util.Pair;
import org.sbml.squeezer.sabiork.util.WebServiceConnectException;
import org.sbml.squeezer.sabiork.util.WebServiceResponseException;
import org.sbml.squeezer.sabiork.wizard.model.KineticLawImporter;
import org.sbml.squeezer.sabiork.wizard.model.WizardModel;
import org.sbml.squeezer.sabiork.wizard.model.WizardProperties;

/**
 * A class that allows the automatic search in the SABIO-RK wizard.
 * 
 * @author Matthias Rall
 * @version $Rev$
 * ${tags}
 */
@SuppressWarnings("serial")
public class CardSearchA extends Card implements ActionListener,
		PropertyChangeListener, TableModelListener {

	public enum SearchState {
		START, RESET
	}

	private ComboBoxModelConstraints comboBoxConstraintsModel;
	private JButton buttonAdd;
	private JComboBox comboBoxConstraints;
	private JComboBoxSearchField comboBoxSearchField;
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
	private TableModelSearchAResults tableResultsModel;
	private TableModelSearchTerms tableSearchTermsModel;

	public CardSearchA(JDialogWizard dialog, WizardModel model) {
		super(dialog, model);
		model.addPropertyChangeListener(this);
		initialize();
	}

	private void initialize() {
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
		panelFilterOptions.addPropertyChangeListener(this);

		tableSearchTermsModel = new TableModelSearchTerms();
		tableSearchTermsModel.addTableModelListener(this);
		tableSearchTerms = new JTable(tableSearchTermsModel);
		tableSearchTerms.setPreferredScrollableViewportSize(panelFilterOptions
				.getSize());
		tableSearchTermsScrollPane = new JScrollPane(tableSearchTerms);
		tableSearchTermsScrollPane.setBorder(BorderFactory.createTitledBorder(
				BorderFactory.createEtchedBorder(),
				WizardProperties.getText("CARD_SEARCH_A_TEXT_CONSTRAINTS")));
		tableSearchTermsScrollPane.setBackground(getBackground());

		progressBar = new JProgressBar(0, 100);
		progressBar.setStringPainted(true);

		tableResultsModel = new TableModelSearchAResults();
		tableResults = new JTable(tableResultsModel);
		tableResults.setRowSelectionAllowed(false);
		tableResults.setColumnSelectionAllowed(false);
		tableResults.setFocusable(false);
		tableResults.getColumnModel().getColumn(0)
				.setCellRenderer(new TableCellRendererSearchAResults());
		tableResults.getColumnModel().getColumn(1)
				.setCellRenderer(new TableCellRendererSearchAResults());
		tableResults.getColumnModel().getColumn(2)
				.setCellRenderer(new TableCellRendererSearchAResults());
		tableResults.getColumnModel().getColumn(3)
				.setCellRenderer(new TableCellRendererSearchAResults());
		tableResults.getColumnModel().getColumn(4)
				.setCellRenderer(new TableCellRendererSearchAResults());
		tableResults.getColumnModel().getColumn(5)
				.setCellRenderer(new TableCellRendererSearchAResults());
		tableResultsScrollPane = new JScrollPane(tableResults);

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
		panelSearch.add(progressBar, new GridBagConstraints(0, 2, 2, 1, 1.0,
				0.0, GridBagConstraints.PAGE_START,
				GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));

		panelResults = new JPanel(new BorderLayout());
		panelResults.setBorder(BorderFactory.createTitledBorder(
				BorderFactory.createEtchedBorder(),
				WizardProperties.getText("CARD_SEARCH_A_TEXT_RESULTS")));
		panelResults.add(tableResultsScrollPane, BorderLayout.CENTER);

		setLayout(new BorderLayout());
		add(panelSearch, BorderLayout.NORTH);
		add(panelResults, BorderLayout.CENTER);

		search = null;
	}

	public void performBeforeShowing() {
		dialog.setButtonState(ButtonState.NEXT_DISABLED);
		comboBoxConstraints.setSelectedItem(SABIORK.QueryField.PATHWAY);
		comboBoxSearchField.setText("");
		setSearchState(SearchState.RESET);
		startSearch();
	};

	public CardID getPreviousCardID() {
		return CardID.REACTIONS_A;
	}

	public CardID getNextCardID() {
		return CardID.SUMMARY_A;
	}

	/**
	 * Adds all selected {@link KineticLawImporter} to the model if they are
	 * importable.
	 */
	private void setSelectedKineticLawImporters() {
		List<KineticLawImporter> selectedKineticLawImporters = new ArrayList<KineticLawImporter>();
		for (SearchAResult searchAResult : tableResultsModel
				.getSearchAResults()) {
			KineticLawImporter selectedKineticLawImporter = searchAResult
					.getSelectedKineticLawImporter();
			if (selectedKineticLawImporter != null) {
				selectedKineticLawImporters.add(selectedKineticLawImporter);
			}
		}
		model.setSelectedKineticLawImporters(selectedKineticLawImporters);
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
			dialog.setButtonState(ButtonState.NEXT_DISABLED);
			progressBar.setValue(0);
			progressBar.setIndeterminate(true);
			progressBar.setStringPainted(false);
			tableResultsModel.clear();
			break;
		case RESET:
			progressBar.setValue(0);
			progressBar.setIndeterminate(false);
			progressBar.setStringPainted(false);
			tableResultsModel.clear();
			tableSearchTermsModel.clear();
			break;
		}
	}

	/**
	 * Adds the current search term to the table if it is not empty.
	 */
	private void addCurrentSearchTerm() {
		String currentTextTrimmed = comboBoxSearchField.getText().trim();
		if (!currentTextTrimmed.isEmpty()
				&& comboBoxConstraintsModel.isQueryFieldSelected()) {
			tableSearchTermsModel.add(new Pair<SABIORK.QueryField, String>(
					comboBoxConstraintsModel.getSelectedQueryField(),
					currentTextTrimmed));
		}
	}

	/**
	 * Starts a new search.
	 */
	private void startSearch() {
		if (search != null && search.isStarted()) {
			search.cancel();
		}
		String searchTermsQuery = tableSearchTermsModel.getSearchTermsQuery();
		String filterOptionsQuery = panelFilterOptions.getFilterOptionsQuery();
		setSearchState(SearchState.START);
		search = new Search(searchTermsQuery, filterOptionsQuery);
		search.addPropertyChangeListener(this);
		search.start();
	}

	public void actionPerformed(ActionEvent e) {
		if (e.getSource().equals(buttonAdd)) {
			if (!comboBoxSearchField.getText().trim().isEmpty()) {
				addCurrentSearchTerm();
				startSearch();
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

	public void propertyChange(PropertyChangeEvent e) {
		if (e.getSource().equals(search)
				&& e.getPropertyName().equals("progress")) {
			progressBar.setValue((Integer) e.getNewValue());
		}
		if (e.getSource().equals(panelFilterOptions)) {
			startSearch();
		}
		if (e.getSource().equals(model)
				&& e.getPropertyName().equals("selectedKineticLawImporters")) {
			if (model.hasSelectedKineticLawImporters()) {
				dialog.setButtonState(ButtonState.NEXT_ENABLED);
			} else {
				dialog.setButtonState(ButtonState.NEXT_DISABLED);
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
					startSearch();
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
	private class Search extends SwingWorker<Void, SearchAResult> {

		private String searchTermsQuery;
		private String filterOptionsQuery;

		public Search(String searchTermsQuery, String filterOptionsQuery) {
			this.searchTermsQuery = searchTermsQuery;
			this.filterOptionsQuery = filterOptionsQuery;
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
			execute();
		}

		/**
		 * Cancels this search.
		 */
		public void cancel() {
			cancel(true);
		}

		/**
		 * Performs the automatic search process.
		 */
		protected Void doInBackground() {
			List<Reaction> selectedReactions = model.getSelectedReactions();
			int selectedReactionCount = selectedReactions.size();
			for (int i = 0; i < selectedReactionCount; i++) {
				Reaction selectedReaction = selectedReactions.get(i);
				List<KineticLawImporter> possibleKineticLawImporters = new ArrayList<KineticLawImporter>();
				List<KineticLawImporter> impossibleKineticLawImporters = new ArrayList<KineticLawImporter>();
				List<KineticLawImporter> totalKineticLawImporters = new ArrayList<KineticLawImporter>();
				String keggReactionID = model
						.getKeggReactionID(selectedReaction);
				if (!keggReactionID.isEmpty()) {
					StringBuilder query = new StringBuilder(
							SABIORK.QueryField.KEGG_REACTION_ID + ":"
									+ keggReactionID);
					if (!searchTermsQuery.isEmpty()) {
						query.append(" AND " + searchTermsQuery);
					}
					query.append(filterOptionsQuery);
					List<KineticLaw> kineticLaws = new ArrayList<KineticLaw>();
					try {
						kineticLaws = SABIORK.getKineticLaws(query.toString());
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
					for (KineticLaw kineticLaw : kineticLaws) {
						if (kineticLaw != null) {
							KineticLawImporter kineticLawImporter = new KineticLawImporter(
									kineticLaw, selectedReaction);
							totalKineticLawImporters.add(kineticLawImporter);
							if (kineticLawImporter.isImportableKineticLaw()) {
								possibleKineticLawImporters
										.add(kineticLawImporter);
							} else {
								impossibleKineticLawImporters
										.add(kineticLawImporter);
							}
						}
					}
				}
				if (!isCancelled()) {
					publish(tableResultsModel.createSearchAResult(
							selectedReaction, possibleKineticLawImporters,
							impossibleKineticLawImporters,
							totalKineticLawImporters));
					setProgress(Math.round(Float.valueOf(i + 1)
							/ selectedReactionCount * 100));
				} else {
					break;
				}
			}
			return null;
		}

		/**
		 * Displays the results.
		 */
		protected void process(List<SearchAResult> chunks) {
			progressBar.setIndeterminate(false);
			progressBar.setStringPainted(true);
			if (!isCancelled()) {
				for (SearchAResult searchAResult : chunks) {
					tableResultsModel.add(searchAResult);
				}
			}
		}

		protected void done() {
			try {
				progressBar.setIndeterminate(false);
				setSelectedKineticLawImporters();
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
