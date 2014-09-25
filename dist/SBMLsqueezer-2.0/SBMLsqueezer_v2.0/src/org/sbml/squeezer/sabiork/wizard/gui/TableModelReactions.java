/*
 * $Id: TableModelReactions.java 1082 2014-02-22 23:54:18Z draeger $
 * $URL: https://rarepos.cs.uni-tuebingen.de/svn-path/SBMLsqueezer/trunk/src/org/sbml/squeezer/sabiork/wizard/gui/TableModelReactions.java $
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

import java.util.ArrayList;
import java.util.List;
import javax.swing.table.AbstractTableModel;
import org.sbml.jsbml.Reaction;
import org.sbml.squeezer.sabiork.wizard.model.WizardProperties;

/**
 * A class that allows the storage of {@link Reaction}.
 * 
 * @author Matthias Rall
 * @version $Rev: 1082 $
 * @since 2.0
 */
public class TableModelReactions extends AbstractTableModel {
  
  /**
   * Generated serial version identifier.
   */
  private static final long serialVersionUID = 5524642810924954865L;
  
  /**
   * 
   */
  private String[] columnNames;
  
  /**
   * 
   */
  private List<Reaction> reactions;
  
  /**
   * 
   */
  public TableModelReactions() {
    columnNames = new String[] {
        WizardProperties.getText("TABLE_MODEL_REACTIONS_TEXT_NAME/ID"),
        WizardProperties.getText("TABLE_MODEL_REACTIONS_TEXT_REACTION"),
        WizardProperties.getText("TABLE_MODEL_REACTIONS_TEXT_KINETIC_LAW"),
        WizardProperties.getText("TABLE_MODEL_REACTIONS_TEXT_REVERSIBLE"),
        WizardProperties.getText("TABLE_MODEL_REACTIONS_TEXT_FAST")
    };
    reactions = new ArrayList<Reaction>();
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
    return reactions.size();
  }
  
  /* (non-Javadoc)
   * @see javax.swing.table.TableModel#getValueAt(int, int)
   */
  @Override
  public Object getValueAt(int rowIndex, int columnIndex) {
    Reaction reaction = reactions.get(rowIndex);
    switch (columnIndex) {
      case 0:
        return reaction.toString();
      case 1:
        return reaction;
      case 2:
        return reaction.isSetKineticLaw();
      case 3:
        return reaction.isReversible();
      case 4:
        return reaction.isFast();
      default:
        return new Object();
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
        return Reaction.class;
      case 2:
        return Boolean.class;
      case 3:
        return Boolean.class;
      case 4:
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
    return false;
  }
  
  /**
   * 
   * @return
   */
  public List<Reaction> getReactions() {
    return reactions;
  }
  
  /**
   * 
   * @param reactions
   */
  public void setReactions(List<Reaction> reactions) {
    this.reactions = reactions;
    fireTableDataChanged();
  }
  
}
