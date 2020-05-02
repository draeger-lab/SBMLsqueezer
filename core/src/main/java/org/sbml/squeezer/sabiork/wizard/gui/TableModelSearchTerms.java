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
package org.sbml.squeezer.sabiork.wizard.gui;

import java.util.ArrayList;
import java.util.List;
import java.util.prefs.BackingStoreException;

import javax.swing.table.AbstractTableModel;

import org.sbml.jsbml.util.ValuePair;
import org.sbml.squeezer.sabiork.SABIORK;
import org.sbml.squeezer.sabiork.SABIORKOptions;
import org.sbml.squeezer.sabiork.wizard.model.WizardProperties;

import de.zbit.util.prefs.SBPreferences;

/**
 * A class that allows the storage of search terms.
 * 
 * @author Matthias Rall
 * @version $Rev$
 * @since 2.0
 */
public class TableModelSearchTerms extends AbstractTableModel {
  
  /**
   * Generated serial version identifier.
   */
  private static final long serialVersionUID = 1170618309623096342L;
  private String[] columnNames;
  private List<ValuePair<SABIORK.QueryField, String>> searchTerms;
  
  /**
   * 
   */
  public TableModelSearchTerms() {
    columnNames = new String[] {
        WizardProperties
        .getText("TABLE_MODEL_SEARCH_TERMS_TEXT_QUERY_FIELD"),
        WizardProperties.getText("TABLE_MODEL_SEARCH_TERMS_TEXT_VALUE"),
        WizardProperties
        .getText("TABLE_MODEL_SEARCH_TERMS_TEXT_CLICK_TO_REMOVE") };
    searchTerms = new ArrayList<ValuePair<SABIORK.QueryField, String>>();
  }
  
  /* (non-Javadoc)
   * @see javax.swing.table.TableModel#getColumnCount()
   */
  @Override
  public int getColumnCount() {
    return columnNames.length;
  }
  
  /* (non-Javadoc)
   * @see javax.swing.table.AbstractTableModel#getColumnName(int)
   */
  @Override
  public String getColumnName(int column) {
    return columnNames[column];
  }
  
  /* (non-Javadoc)
   * @see javax.swing.table.TableModel#getRowCount()
   */
  @Override
  public int getRowCount() {
    return searchTerms.size();
  }
  
  /* (non-Javadoc)
   * @see javax.swing.table.TableModel#getValueAt(int, int)
   */
  @Override
  public Object getValueAt(int rowIndex, int columnIndex) {
    ValuePair<SABIORK.QueryField, String> searchTerm = searchTerms
        .get(rowIndex);
    switch (columnIndex) {
      case 0:
        return searchTerm.getL().toString();
      case 1:
        return searchTerm.getV();
      case 2:
        return Boolean.TRUE;
      default:
        return new Object();
    }
  }
  
  /* (non-Javadoc)
   * @see javax.swing.table.AbstractTableModel#setValueAt(java.lang.Object, int, int)
   */
  @Override
  public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
    if (columnIndex == 2) {
      fireTableCellUpdated(rowIndex, columnIndex);
    }
  }
  
  /* (non-Javadoc)
   * @see javax.swing.table.AbstractTableModel#getColumnClass(int)
   */
  @Override
  public Class<?> getColumnClass(int columnIndex) {
    switch (columnIndex) {
      case 0:
        return String.class;
      case 1:
        return String.class;
      case 2:
        return Boolean.class;
      default:
        return Object.class;
    }
  }
  
  /* (non-Javadoc)
   * @see javax.swing.table.AbstractTableModel#isCellEditable(int, int)
   */
  @Override
  public boolean isCellEditable(int rowIndex, int columnIndex) {
    return (columnIndex == 2);
  }
  
  /**
   * 
   * @return
   */
  public List<ValuePair<SABIORK.QueryField, String>> getSearchTerms() {
    return searchTerms;
  }
  
  /**
   * 
   * @param searchTerms
   */
  public void setSearchTerms(
    List<ValuePair<SABIORK.QueryField, String>> searchTerms) {
    this.searchTerms = searchTerms;
    fireTableDataChanged();
  }
  
  /**
   * 
   * @param searchTerm
   */
  public void add(ValuePair<SABIORK.QueryField, String> searchTerm) {
    if (!searchTerms.contains(searchTerm)) {
      searchTerms.add(searchTerm);
      fireTableDataChanged();
    }
  }
  
  /**
   * Clears the search terms.
   */
  public void clear() {
    searchTerms.clear();
    fireTableDataChanged();
  }
  
  /**
   * 
   * @param index
   */
  public void remove(int index) {
    searchTerms.remove(index);
    saveSettings();
    fireTableDataChanged();
  }
  
  /**
   * 
   * @return
   */
  public String getSearchTermsQuery() {
    return SABIORK.getSearchTermsQuery(searchTerms);
  }
  
  /**
   * Loads the previous settings
   */
  public void loadSettings() {
    SBPreferences prefs = SBPreferences
        .getPreferencesFor(SABIORKOptions.class);
    
    if (prefs.containsKey(SABIORKOptions.CELLULAR_LOCATION)) {
      String value = prefs.get(SABIORKOptions.CELLULAR_LOCATION);
      if ((value != null) && !(value.equals(""))) {
        searchTerms.add(new ValuePair<SABIORK.QueryField, String>(SABIORK.QueryField.CELLULAR_LOCATION, value));
      }
    }
    if (prefs.containsKey(SABIORKOptions.ORGANISM)) {
      String value = prefs.get(SABIORKOptions.ORGANISM);
      if ((value != null) && !(value.equals(""))) {
        searchTerms.add(new ValuePair<SABIORK.QueryField, String>(SABIORK.QueryField.ORGANISM, value));
      }
    }
    if (prefs.containsKey(SABIORKOptions.PATHWAY)) {
      String value = prefs.get(SABIORKOptions.PATHWAY);
      if ((value != null) && !(value.equals(""))) {
        searchTerms.add(new ValuePair<SABIORK.QueryField, String>(SABIORK.QueryField.PATHWAY, value));
      }
    }
    if (prefs.containsKey(SABIORKOptions.TISSUE)) {
      String value = prefs.get(SABIORKOptions.TISSUE);
      if ((value != null) && !(value.equals(""))) {
        searchTerms.add(new ValuePair<SABIORK.QueryField, String>(SABIORK.QueryField.TISSUE, value));
      }
    }
  }
  
  /**
   * Saves the settings of the search
   */
  public void saveSettings() {
    SBPreferences prefs = SBPreferences
        .getPreferencesFor(SABIORKOptions.class);
    
    boolean cellularLocationSet = false;
    boolean organismSet = false;
    boolean pathwaySet = false;
    boolean tissueSet = false;
    
    for (ValuePair<SABIORK.QueryField, String> vp: searchTerms) {
      if (vp.getL().equals(SABIORK.QueryField.CELLULAR_LOCATION)) {
        prefs.put(SABIORKOptions.CELLULAR_LOCATION, vp.getV());
        cellularLocationSet = true;
      }
      if (vp.getL().equals(SABIORK.QueryField.ORGANISM)) {
        prefs.put(SABIORKOptions.ORGANISM, vp.getV());
        organismSet = true;
      }
      if (vp.getL().equals(SABIORK.QueryField.PATHWAY)) {
        prefs.put(SABIORKOptions.PATHWAY, vp.getV());
        pathwaySet = true;
      }
      if (vp.getL().equals(SABIORK.QueryField.TISSUE)) {
        prefs.put(SABIORKOptions.TISSUE, vp.getV());
        tissueSet = true;
      }
    }
    
    if (!cellularLocationSet) {
      prefs.put(SABIORKOptions.CELLULAR_LOCATION, "");
    }
    if (!organismSet) {
      prefs.put(SABIORKOptions.ORGANISM, "");
    }
    if (!pathwaySet) {
      prefs.put(SABIORKOptions.PATHWAY, "");
    }
    if (!tissueSet) {
      prefs.put(SABIORKOptions.TISSUE, "");
    }
    
    try {
      prefs.flush();
    } catch (BackingStoreException e) {
      e.printStackTrace();
    }
    
  }
  
}
