/*
 * $Id: SearchAResult.java 1082 2014-02-22 23:54:18Z draeger $
 * $URL: https://rarepos.cs.uni-tuebingen.de/svn-path/SBMLsqueezer/trunk/src/org/sbml/squeezer/sabiork/wizard/console/SearchAResult.java$
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
package org.sbml.squeezer.sabiork.wizard.console;

import java.util.List;
import org.sbml.jsbml.Reaction;
import org.sbml.squeezer.sabiork.wizard.model.KineticLawImporter;

/**
 * A class for representing a result of the automatic search.
 * 
 * @author Matthias Rall
 * @version $Rev: 1082 $
 * @since 2.0
 */
public class SearchAResult {
  
  private Reaction reaction;
  private List<KineticLawImporter> possibleKineticLawImporters;
  private List<KineticLawImporter> impossibleKineticLawImporters;
  private List<KineticLawImporter> totalKineticLawImporters;
  
  /**
   * Creates a new result of the automatic search.
   * 
   * @param reaction
   * @param possibleKineticLawImporters
   * @param impossibleKineticLawImporters
   * @param totalKineticLawImporters
   */
  public SearchAResult(Reaction reaction,
    List<KineticLawImporter> possibleKineticLawImporters,
    List<KineticLawImporter> impossibleKineticLawImporters,
    List<KineticLawImporter> totalKineticLawImporters) {
    this.reaction = reaction;
    this.possibleKineticLawImporters = possibleKineticLawImporters;
    this.impossibleKineticLawImporters = impossibleKineticLawImporters;
    this.totalKineticLawImporters = totalKineticLawImporters;
  }
  
  /**
   * Returns the corresponding {@link Reaction}.
   * 
   * @return the corresponding {@link Reaction}
   */
  public Reaction getReaction() {
    return reaction;
  }
  
  /**
   * Returns a list of all importable {@link KineticLawImporter}.
   * 
   * @return a list of all importable {@link KineticLawImporter}
   */
  public List<KineticLawImporter> getPossibleKineticLawImporters() {
    return possibleKineticLawImporters;
  }
  
  /**
   * Returns a list of all {@link KineticLawImporter} which are not
   * importable.
   * 
   * @return a list of all {@link KineticLawImporter} which are not importable
   */
  public List<KineticLawImporter> getImpossibleKineticLawImporters() {
    return impossibleKineticLawImporters;
  }
  
  /**
   * Returns a list of all {@link KineticLawImporter}.
   * 
   * @return a list of all {@link KineticLawImporter}.
   */
  public List<KineticLawImporter> getTotalKineticLawImporters() {
    return totalKineticLawImporters;
  }
  
  /**
   * Returns the {@link KineticLawImporter} selected for import.
   * 
   * @return the {@link KineticLawImporter} selected for import
   */
  public KineticLawImporter getSelectedKineticLawImporter() {
    KineticLawImporter selectedKineticLawImporter = null;
    if (!possibleKineticLawImporters.isEmpty()) {
      selectedKineticLawImporter = possibleKineticLawImporters.get(0);
    }
    return selectedKineticLawImporter;
  }
  
}
