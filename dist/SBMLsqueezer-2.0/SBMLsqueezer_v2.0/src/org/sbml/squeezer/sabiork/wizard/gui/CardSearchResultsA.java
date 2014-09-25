/*
 * $Id: CardSearchResultsA.java 1082 2014-02-22 23:54:18Z draeger $ $URL: CardSearchResultsA.java
 * $ --------------------------------------------------------------------- This
 * file is part of SBMLsqueezer, a Java program that creates rate equations for
 * reactions in SBML files (http://sbml.org).
 * 
 * Copyright (C) 2006-2012 by the University of Tuebingen, Germany.
 * 
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 * ---------------------------------------------------------------------
 */
package org.sbml.squeezer.sabiork.wizard.gui;

import java.awt.BorderLayout;
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
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingWorker;
import javax.xml.stream.XMLStreamException;

import org.sbml.jsbml.KineticLaw;
import org.sbml.jsbml.Reaction;
import org.sbml.squeezer.sabiork.SABIORK;
import org.sbml.squeezer.sabiork.util.WebServiceConnectException;
import org.sbml.squeezer.sabiork.util.WebServiceResponseException;
import org.sbml.squeezer.sabiork.wizard.gui.JDialogWizard.ButtonState;
import org.sbml.squeezer.sabiork.wizard.gui.JDialogWizard.CardID;
import org.sbml.squeezer.sabiork.wizard.gui.TableModelSearchAResults.SearchAResult;
import org.sbml.squeezer.sabiork.wizard.model.KineticLawImporter;
import org.sbml.squeezer.sabiork.wizard.model.WizardModel;
import org.sbml.squeezer.sabiork.wizard.model.WizardProperties;

/**
 * @author Roland Keller
 * @version $Rev: 1082 $
 * @since 2.0
 */
public class CardSearchResultsA extends Card implements ActionListener,
PropertyChangeListener {
  
  /**
   * Generated serial version identifier.
   */
  private static final long serialVersionUID = 3025182595373189918L;
  
  public enum SearchState {
    START, RESET
  }
  
  private JPanel panelResults;
  private JProgressBar progressBar;
  private JScrollPane tableResultsScrollPane;
  private JTable tableResults;
  private TableModelSearchAResults tableResultsModel;
  private Search search;
  
  /**
   * 
   * @param dialog
   * @param model
   * @throws IOException
   */
  public CardSearchResultsA(JDialogWizard dialog, WizardModel model)
      throws IOException {
    super(dialog, model);
    model.addPropertyChangeListener(this);
    initialize();
  }
  
  /**
   * Initializes the card.
   * 
   * @throws IOException
   */
  private void initialize() throws IOException {
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
    tableResults.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
    tableResultsScrollPane = new JScrollPane(tableResults);
    
    panelResults = new JPanel(new BorderLayout());
    panelResults.add(progressBar, BorderLayout.NORTH);
    
    panelResults.setBorder(BorderFactory.createTitledBorder(
      BorderFactory.createEtchedBorder(),
      WizardProperties.getText("CARD_SEARCH_A_TEXT_RESULTS")));
    panelResults.add(tableResultsScrollPane, BorderLayout.CENTER);
    
    setLayout(new BorderLayout());
    add(panelResults, BorderLayout.CENTER);
    
    search = null;
    
  }
  
  /*
   * (non-Javadoc)
   * 
   * @see org.sbml.squeezer.sabiork.wizard.gui.Card#performBeforeShowing()
   */
  @Override
  public void performBeforeShowing() {
    dialog.setButtonState(ButtonState.NEXT_DISABLED);
    setSearchState(SearchState.RESET);
    startSearch();
  }
  
  /*
   * (non-Javadoc)
   * 
   * @see org.sbml.squeezer.sabiork.wizard.gui.Card#getPreviousCardID()
   */
  @Override
  public CardID getPreviousCardID() {
    return CardID.SEARCH_A;
  }
  
  /*
   * (non-Javadoc)
   * 
   * @see org.sbml.squeezer.sabiork.wizard.gui.Card#getNextCardID()
   */
  @Override
  public CardID getNextCardID() {
    return CardID.SUMMARY_A;
  }
  
  /**
   * Adds all selected {@link KineticLawImporter} to the model if they are
   * importable.
   */
  private void setSelectedKineticLawImporters() {
    List<KineticLawImporter> selectedKineticLawImporters = new ArrayList<KineticLawImporter>();
    for (SearchAResult searchAResult : tableResultsModel.getSearchAResults()) {
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
        break;
    }
  }
  
  /**
   * Starts a new search.
   */
  private void startSearch() {
    if (search != null && search.isStarted()) {
      search.cancel();
    }
    String searchTermsQuery = tableSearchTermsModelA.getSearchTermsQuery();
    String filterOptionsQuery = panelFilterOptions.getFilterOptionsQuery();
    setSearchState(SearchState.START);
    search = new Search(searchTermsQuery, filterOptionsQuery);
    search.addPropertyChangeListener(this);
    search.start();
  }
  
  /*
   * (non-Javadoc)
   * 
   * @see
   * java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
   */
  @Override
  public void actionPerformed(ActionEvent e) {
  }
  
  /*
   * (non-Javadoc)
   * 
   * @see
   * java.beans.PropertyChangeListener#propertyChange(java.beans.PropertyChangeEvent
   * )
   */
  @Override
  public void propertyChange(PropertyChangeEvent e) {
    if (e.getSource().equals(search) && e.getPropertyName().equals("progress")) {
      progressBar.setValue((Integer) e.getNewValue());
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
  
  /*
   * (non-Javadoc)
   * 
   * @see org.sbml.squeezer.sabiork.wizard.gui.Card#performAfterPressingBack()
   */
  @Override
  public void performAfterPressingBack() {
    panelFilterOptions.saveSettings();
    tableSearchTermsModelA.saveSettings();
    search.cancel();
  }
  
  /*
   * (non-Javadoc)
   * 
   * @see org.sbml.squeezer.sabiork.wizard.gui.Card#performAfterCancel()
   */
  @Override
  public void performAfterCancel() {
    panelFilterOptions.saveSettings();
    tableSearchTermsModelA.saveSettings();
    search.cancel();
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
     * @return {@code true} if this search is already in progress, {@code false}
     *         otherwise
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
    @Override
    protected Void doInBackground() {
      List<Reaction> selectedReactions = model.getSelectedReactions();
      int selectedReactionCount = selectedReactions.size();
      for (int i = 0; i < selectedReactionCount; i++) {
        Reaction selectedReaction = selectedReactions.get(i);
        List<KineticLawImporter> possibleKineticLawImporters = new ArrayList<KineticLawImporter>();
        List<KineticLawImporter> impossibleKineticLawImporters = new ArrayList<KineticLawImporter>();
        List<KineticLawImporter> totalKineticLawImporters = new ArrayList<KineticLawImporter>();
        String keggReactionID = model.getKeggReactionID(selectedReaction);
        if (!keggReactionID.isEmpty()) {
          StringBuilder query = new StringBuilder(
            SABIORK.QueryField.KEGG_REACTION_ID + ":" + keggReactionID);
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
                possibleKineticLawImporters.add(kineticLawImporter);
              } else {
                impossibleKineticLawImporters.add(kineticLawImporter);
              }
            }
          }
        }
        if (!isCancelled()) {
          publish(tableResultsModel.createSearchAResult(selectedReaction,
            possibleKineticLawImporters, impossibleKineticLawImporters,
            totalKineticLawImporters));
          setProgress(Math.round(Float.valueOf(i + 1) / selectedReactionCount
            * 100));
        } else {
          break;
        }
      }
      return null;
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see javax.swing.SwingWorker#process(java.util.List)
     */
    @Override
    protected void process(List<SearchAResult> chunks) {
      progressBar.setIndeterminate(false);
      progressBar.setStringPainted(true);
      if (!isCancelled()) {
        for (SearchAResult searchAResult : chunks) {
          tableResultsModel.add(searchAResult);
        }
      }
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see javax.swing.SwingWorker#done()
     */
    @Override
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
