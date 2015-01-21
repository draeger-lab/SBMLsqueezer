/*
 * $Id: ComboBoxModelReactionFilters.java 1082 2014-02-22 23:54:18Z draeger $
 * $URL: https://rarepos.cs.uni-tuebingen.de/svn-path/SBMLsqueezer/trunk/src/org/sbml/squeezer/sabiork/wizard/gui/ComboBoxModelReactionFilters.java $
 * ---------------------------------------------------------------------
 * This file is part of SBMLsqueezer, a Java program that creates rate
 * equations for reactions in SBML files (http://sbml.org).
 *
 * Copyright (C) 2006-2015 by the University of Tuebingen, Germany.
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

import org.sbml.squeezer.sabiork.wizard.model.WizardProperties;

/**
 * A class that provides the model of the combo box for selecting a reaction
 * filter.
 * 
 * @author Matthias Rall
 * @version $Rev: 1082 $
 * @since 2.0
 */
public class ComboBoxModelReactionFilters extends ComboBoxModelCaptions {
  
  /**
   * Generated serial version identifier.
   */
  private static final long serialVersionUID = -8088462929075616570L;
  
  /**
   * 
   * @author Matthias Rall
   * @version $Rev: 1082 $
   */
  public enum ReactionFilter {
    
    ALL_REACTIONS(WizardProperties.getText("COMBO_BOX_MODEL_REACTION_FILTERS_TEXT_ALL_REACTIONS")),
    REACTIONS_WITH_KINETICLAW(WizardProperties.getText("COMBO_BOX_MODEL_REACTION_FILTERS_TEXT_REACTIONS_WITH_KINETICLAW")),
    REACTIONS_WITHOUT_KINETICLAW(WizardProperties.getText("COMBO_BOX_MODEL_REACTION_FILTERS_TEXT_REACTIONS_WITHOUT_KINETICLAW")),
    REVERSIBLE_REACTIONS(WizardProperties.getText("COMBO_BOX_MODEL_REACTION_FILTERS_TEXT_REVERSIBLE_REACTIONS")),
    IRREVERSIBLE_REACTIONS(WizardProperties.getText("COMBO_BOX_MODEL_REACTION_FILTERS_TEXT_IRREVERSIBLE_REACTIONS")),
    FAST_REACTIONS(WizardProperties.getText("COMBO_BOX_MODEL_REACTION_FILTERS_TEXT_FAST_REACTIONS")),
    SLOW_REACTIONS(WizardProperties.getText("COMBO_BOX_MODEL_REACTION_FILTERS_TEXT_SLOW_REACTIONS"));
    
    private final String name;
    
    /**
     * 
     * @param name
     */
    private ReactionFilter(String name) {
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
  public ComboBoxModelReactionFilters() {
    addCaption(WizardProperties
      .getText("COMBO_BOX_MODEL_REACTION_FILTERS_FILTER_OPTIONS"));
    addElement(ReactionFilter.ALL_REACTIONS);
    addElement(ReactionFilter.REACTIONS_WITH_KINETICLAW);
    addElement(ReactionFilter.REACTIONS_WITHOUT_KINETICLAW);
    addElement(ReactionFilter.REVERSIBLE_REACTIONS);
    addElement(ReactionFilter.IRREVERSIBLE_REACTIONS);
    addElement(ReactionFilter.FAST_REACTIONS);
    addElement(ReactionFilter.SLOW_REACTIONS);
    
    setSelectedItem(ReactionFilter.ALL_REACTIONS);
  }
  
  /**
   * 
   * @return
   */
  public ReactionFilter getSelectedReactionFilter() {
    return ((ReactionFilter) getSelectedItem());
  }
  
}
