/*
 * $Id$ $URL: CardSearchResultsM.java
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
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingWorker;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.xml.stream.XMLStreamException;

import org.sbml.jsbml.KineticLaw;
import org.sbml.squeezer.sabiork.SABIORK;
import org.sbml.squeezer.sabiork.util.WebServiceConnectException;
import org.sbml.squeezer.sabiork.util.WebServiceResponseException;
import org.sbml.squeezer.sabiork.wizard.gui.JDialogWizard.ButtonState;
import org.sbml.squeezer.sabiork.wizard.gui.JDialogWizard.CardID;
import org.sbml.squeezer.sabiork.wizard.model.WizardModel;
import org.sbml.squeezer.sabiork.wizard.model.WizardProperties;

/**
 * @author Roland Keller
 * @version $Rev$
 * @since 2.0
 */
public class CardSearchResultsM extends Card implements ActionListener,
PropertyChangeListener, ListSelectionListener,
ChangeListener {
  
  /**
   * Generated serial version identifier.
   */
  private static final long serialVersionUID = -6832464267968071096L;
  
  public enum SearchState {
    START, DONE, CANCEL, RESET
  }
  
  private JLabel labelResults;
  private JPanel panelResults;
  private JProgressBar progressBar;
  private JScrollPane tableResultsScrollPane;
  private JTable tableResults;
  private Search search;
  private TableModelSearchMResults tableResultsModel;
  
  /**
   * 
   * @param dialog
   * @param model
   * @throws IOException
   */
  public CardSearchResultsM(JDialogWizard dialog, WizardModel model)
      throws IOException {
    super(dialog, model);
    model.addPropertyChangeListener(this);
    initialize();
  }
  
  /**
   * @throws IOException
   * 
   */
  private void initialize() throws IOException {
    progressBar = new JProgressBar(0, 100);
    progressBar.setStringPainted(true);
    
    labelResults = new JLabel(MessageFormat.format(WizardProperties
      .getText("CARD_SEARCH_M_TEXT_THERE_ARE_X_KINETIC_LAW_ENTRIES"), 0));
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
    tableResults.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
    tableResultsScrollPane = new JScrollPane(tableResults);
    
    
    panelResults = new JPanel(new BorderLayout());
    panelResults.setBorder(BorderFactory.createTitledBorder(
      BorderFactory.createEtchedBorder(),
      WizardProperties.getText("CARD_SEARCH_M_TEXT_RESULTS")));
    panelResults.add(labelResults, BorderLayout.NORTH);
    panelResults.add(tableResultsScrollPane, BorderLayout.CENTER);
    
    setLayout(new BorderLayout());
    add(panelResults, BorderLayout.CENTER);
    add(progressBar, BorderLayout.NORTH);
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
    setSearchState(SearchState.START);
    tableSearchTermsModelM.loadSettings();
    startSearch();
  }
  
  /*
   * (non-Javadoc)
   * 
   * @see org.sbml.squeezer.sabiork.wizard.gui.Card#getPreviousCardID()
   */
  @Override
  public CardID getPreviousCardID() {
    return CardID.SEARCH_M;
  }
  
  /*
   * (non-Javadoc)
   * 
   * @see org.sbml.squeezer.sabiork.wizard.gui.Card#getNextCardID()
   */
  @Override
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
      selectedKineticLaw = tableResultsModel.getKineticLaws().get(selectedRow);
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
        progressBar.setValue(0);
        progressBar.setIndeterminate(true);
        progressBar.setStringPainted(false);
        tableResultsModel.clear();
        setResultCount(0);
        break;
      case DONE:
        progressBar.setIndeterminate(false);
        break;
      case CANCEL:
        if (progressBar.isIndeterminate()) {
          progressBar.setValue(0);
          progressBar.setIndeterminate(false);
        }
        break;
      case RESET:
        progressBar.setValue(0);
        progressBar.setIndeterminate(false);
        progressBar.setStringPainted(false);
        tableResultsModel.clear();
        setResultCount(0);
        tableSearchTermsModelM.clear();
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
      String searchTermsQuery = tableSearchTermsModelM.getSearchTermsQuery();
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
    if (e.getSource().equals(search) && e.getPropertyName().equals("state") && e.getNewValue().equals(SwingWorker.StateValue.DONE)) {
      progressBar.setValue(100);
    }
    if (e.getSource().equals(model)
        && e.getPropertyName().equals("selectedKineticLaw")) {
      if (model.hasSelectedKineticLaw()) {
        dialog.setButtonState(ButtonState.NEXT_BACK_ENABLED);
      } else {
        dialog.setButtonState(ButtonState.NEXT_DISABLED);
      }
    }
  }
  
  /*
   * (non-Javadoc)
   * 
   * @see
   * javax.swing.event.ListSelectionListener#valueChanged(javax.swing.event.
   * ListSelectionEvent)
   */
  @Override
  public void valueChanged(ListSelectionEvent e) {
    if (e.getSource().equals(tableResults.getSelectionModel())) {
      if (!e.getValueIsAdjusting()) {
        setSelectedKineticLaw();
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
    tableSearchTermsModelM.saveSettings();
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
    tableSearchTermsModelM.saveSettings();
    search.cancel();
  }
  
  /*
   * (non-Javadoc)
   * 
   * @see org.sbml.squeezer.sabiork.wizard.gui.Card#performAfterNext()
   */
  @Override
  public void performAfterNext() {
    panelFilterOptions.saveSettings();
    tableSearchTermsModelM.saveSettings();
    search.cancel();
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
      resultCount = 0;
      finalSearchState = SearchState.DONE;
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
    
    /*
     * (non-Javadoc)
     * 
     * @see javax.swing.SwingWorker#doInBackground()
     */
    @Override
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
            setProgress(Math.round(Float.valueOf(i + 1) / idCount * 100));
          } else {
            break;
          }
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
    
    /*
     * (non-Javadoc)
     * 
     * @see javax.swing.SwingWorker#done()
     */
    @Override
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
  
  /*
   * (non-Javadoc)
   * 
   * @see
   * javax.swing.event.ChangeListener#stateChanged(javax.swing.event.ChangeEvent
   * )
   */
  @Override
  public void stateChanged(ChangeEvent e) {
    if ((!panelFilterOptions.isUserConfiguration())) {
      startSearch();
      panelFilterOptions.saveSettings();
      tableSearchTermsModelM.saveSettings();
    }
  }
  
}
