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

import org.sbml.squeezer.sabiork.SABIORK;
import org.sbml.squeezer.sabiork.wizard.model.WizardProperties;

/**
 * A class that provides the model of the combo box for selecting a constraint
 * in the automatic search.
 * 
 * @author Matthias Rall
 *
 * @since 2.0
 */
public class ComboBoxModelConstraints extends ComboBoxModelCaptions {
  
  /**
   * Generated serial version identifier.
   */
  private static final long serialVersionUID = 6268004298121183140L;
  
  /**
   * 
   */
  public ComboBoxModelConstraints() {
    addCaption(WizardProperties
      .getText("COMBO_BOX_MODEL_CONSTRAINTS_TEXT_REACTION/PATHWAY"));
    addElement(SABIORK.QueryField.PATHWAY);
    
    addCaption(WizardProperties
      .getText("COMBO_BOX_MODEL_CONSTRAINTS_TEXT_BIOLOGICAL_SOURCE"));
    addElement(SABIORK.QueryField.TISSUE);
    addElement(SABIORK.QueryField.ORGANISM);
    addElement(SABIORK.QueryField.CELLULAR_LOCATION);
    
    setSelectedItem(SABIORK.QueryField.PATHWAY);
  }
  
  /**
   * 
   * @return queryField the selected query filed
   */
  public SABIORK.QueryField getSelectedQueryField() {
    return ((SABIORK.QueryField) getSelectedItem());
  }
  
  /**
   * 
   * @return
   */
  public boolean isQueryFieldSelected() {
    return (getSelectedItem() instanceof SABIORK.QueryField);
  }
  
}
