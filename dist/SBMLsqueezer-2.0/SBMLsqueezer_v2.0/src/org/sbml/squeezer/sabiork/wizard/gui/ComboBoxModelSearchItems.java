/*
 * $Id: ComboBoxModelSearchItems.java 1082 2014-02-22 23:54:18Z draeger $
 * $URL: https://rarepos.cs.uni-tuebingen.de/svn-path/SBMLsqueezer/trunk/src/org/sbml/squeezer/sabiork/wizard/gui/ComboBoxModelSearchItems.java $
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

import org.sbml.squeezer.sabiork.SABIORK;
import org.sbml.squeezer.sabiork.wizard.model.WizardProperties;

/**
 * A class that provides the model of the combo box for selecting a search item
 * in the manual search.
 * 
 * @author Matthias Rall
 * @version $Rev: 1082 $
 * @since 2.0
 */
public class ComboBoxModelSearchItems extends ComboBoxModelCaptions {
  
  /**
   * Generated serial version identifier.
   */
  private static final long serialVersionUID = 1580494676722299702L;
  
  public enum SearchItem {
    
    QUERY_EXPRESSION(WizardProperties
      .getText("COMBO_BOX_MODEL_SEARCH_ITEMS_TEXT_QUERY_EXPRESSION"));
    
    private final String name;
    
    private SearchItem(String name) {
      this.name = name;
    }
    
    /* (non-Javadoc)
     * @see java.lang.Enum#toString()
     */
    @Override
    public String toString() {
      return name;
    }
    
  }
  
  /**
   * 
   */
  public ComboBoxModelSearchItems() {
    addCaption(WizardProperties
      .getText("COMBO_BOX_MODEL_SEARCH_ITEMS_TEXT_ENTRY"));
    addElement(SABIORK.QueryField.ENTRY_ID);
    
    addCaption(WizardProperties
      .getText("COMBO_BOX_MODEL_SEARCH_ITEMS_TEXT_REACTION/PATHWAY"));
    addElement(SABIORK.QueryField.PATHWAY);
    addElement(SABIORK.QueryField.KEGG_REACTION_ID);
    addElement(SABIORK.QueryField.SABIO_REACTION_ID);
    
    addCaption(WizardProperties
      .getText("COMBO_BOX_MODEL_SEARCH_ITEMS_TEXT_COMPOUND"));
    addElement(SABIORK.QueryField.ANY_ROLE);
    addElement(SABIORK.QueryField.SUBSTRATE);
    addElement(SABIORK.QueryField.PRODUCT);
    addElement(SABIORK.QueryField.INHIBITOR);
    addElement(SABIORK.QueryField.CATALYST);
    addElement(SABIORK.QueryField.COFACTOR);
    addElement(SABIORK.QueryField.ACTIVATOR);
    addElement(SABIORK.QueryField.OTHER_MODIFIER);
    addElement(SABIORK.QueryField.PUBCHEM_ID);
    addElement(SABIORK.QueryField.KEGG_ID);
    addElement(SABIORK.QueryField.CHEBI_ID);
    addElement(SABIORK.QueryField.SABIO_COMPOUND_ID);
    
    addCaption(WizardProperties
      .getText("COMBO_BOX_MODEL_SEARCH_ITEMS_TEXT_ENZYME"));
    addElement(SABIORK.QueryField.ENZYMENAME);
    addElement(SABIORK.QueryField.EC_NUMBER);
    addElement(SABIORK.QueryField.UNIPROT_ID);
    
    addCaption(WizardProperties
      .getText("COMBO_BOX_MODEL_SEARCH_ITEMS_TEXT_BIOLOGICAL_SOURCE"));
    addElement(SABIORK.QueryField.TISSUE);
    addElement(SABIORK.QueryField.ORGANISM);
    addElement(SABIORK.QueryField.CELLULAR_LOCATION);
    
    addCaption(WizardProperties
      .getText("COMBO_BOX_MODEL_SEARCH_ITEMS_TEXT_KINETIC_DATA"));
    addElement(SABIORK.QueryField.PARAMETERTYPE);
    addElement(SABIORK.QueryField.KINETIC_MECHANISM_TYPE);
    addElement(SABIORK.QueryField.ASSOCIATED_SPECIES);
    
    addCaption(WizardProperties
      .getText("COMBO_BOX_MODEL_SEARCH_ITEMS_TEXT_PUBLICATION"));
    addElement(SABIORK.QueryField.TITLE);
    addElement(SABIORK.QueryField.AUTHOR);
    addElement(SABIORK.QueryField.YEAR);
    addElement(SABIORK.QueryField.PUBMED_ID);
    
    addCaption(WizardProperties
      .getText("COMBO_BOX_MODEL_SEARCH_ITEMS_TEXT_ADVANCED_SEARCH"));
    addElement(SearchItem.QUERY_EXPRESSION);
    
    setSelectedItem(SABIORK.QueryField.ENTRY_ID);
  }
  
  /**
   * Returns the currently selected SABIO-RK query field.
   * 
   * @return
   */
  public SABIORK.QueryField getSelectedQueryField() {
    return ((SABIORK.QueryField) getSelectedItem());
  }
  
  /**
   * Returns {@code true} if the currently selected search item is a
   * SABIO-RK query field; {@code false} otherwise.
   * 
   * @return
   */
  public boolean isQueryFieldSelected() {
    return (getSelectedItem() instanceof SABIORK.QueryField);
  }
  
  /**
   * Returns {@code true} if the currently selected search item is a
   * query expression; {@code false} otherwise.
   * 
   * @return
   */
  public boolean isQueryExpressionSelected() {
    return (getSelectedItem() instanceof SearchItem && getSelectedItem() == SearchItem.QUERY_EXPRESSION);
  }
  
}
