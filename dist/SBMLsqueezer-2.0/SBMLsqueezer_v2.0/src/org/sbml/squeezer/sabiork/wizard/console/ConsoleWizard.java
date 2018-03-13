/*
 * $Id: ConsoleWizard.java 971 2012-08-17 13:36:54Z keller$
 * $URL: https://rarepos.cs.uni-tuebingen.de/svn-path/SBMLsqueezer/trunk/src/org/sbml/squeezer/sabiork/wizard/console/ConsoleWizard.java$
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
package org.sbml.squeezer.sabiork.wizard.console;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.stream.XMLStreamException;

import org.sbml.jsbml.KineticLaw;
import org.sbml.jsbml.Reaction;
import org.sbml.jsbml.SBMLDocument;
import org.sbml.jsbml.util.ValuePair;
import org.sbml.squeezer.sabiork.SABIORK;
import org.sbml.squeezer.sabiork.SABIORK.QueryField;
import org.sbml.squeezer.sabiork.util.WebServiceConnectException;
import org.sbml.squeezer.sabiork.util.WebServiceResponseException;
import org.sbml.squeezer.sabiork.wizard.model.KineticLawImporter;
import org.sbml.squeezer.sabiork.wizard.model.WizardModel;

/**
 * The console version of the SABIO-RK wizard.
 * 
 * @author Matthias Rall
 * @version $Rev: 1082 $
 * @since 2.0
 */
public class ConsoleWizard {
  
  private WizardModel model;
  private boolean overwriteExistingRateLaws;
  private String pathway;
  private String tissue;
  private String organism;
  private String cellularLocation;
  private Boolean isWildtype;
  private Boolean isMutant;
  private Boolean isRecombinant;
  private Boolean hasKineticData;
  private Double lowerpHValue;
  private Double upperpHValue;
  private Double lowerTemperature;
  private Double upperTemperature;
  private Boolean isDirectSubmission;
  private Boolean isJournal;
  private Boolean isEntriesInsertedSince;
  private String dateSubmitted;
  
  /**
   * 
   * @param sbmlDocument
   * @param overwriteExistingRateLaws
   * @param pathway
   * @param tissue
   * @param organism
   * @param cellularLocation
   * @param isWildtype
   * @param isMutant
   * @param isRecombinant
   * @param hasKineticData
   * @param lowerpHValue
   * @param upperpHValue
   * @param lowerTemperature
   * @param upperTemperature
   * @param isDirectSubmission
   * @param isJournal
   * @param isEntriesInsertedSince
   * @param dateSubmitted
   */
  public ConsoleWizard(SBMLDocument sbmlDocument, boolean overwriteExistingRateLaws,
    String pathway, String tissue, String organism,
    String cellularLocation, Boolean isWildtype, Boolean isMutant,
    Boolean isRecombinant, Boolean hasKineticData, Double lowerpHValue,
    Double upperpHValue, Double lowerTemperature,
    Double upperTemperature, Boolean isDirectSubmission,
    Boolean isJournal, Boolean isEntriesInsertedSince,
    String dateSubmitted) {
    model = new WizardModel(sbmlDocument, true);
    this.overwriteExistingRateLaws = overwriteExistingRateLaws;
    this.pathway = pathway;
    this.tissue = tissue;
    this.organism = organism;
    this.cellularLocation = cellularLocation;
    this.isWildtype = isWildtype;
    this.isMutant = isMutant;
    this.isRecombinant = isRecombinant;
    this.hasKineticData = hasKineticData;
    this.lowerpHValue = lowerpHValue;
    this.upperpHValue = upperpHValue;
    this.lowerTemperature = lowerTemperature;
    this.upperTemperature = upperTemperature;
    this.isDirectSubmission = isDirectSubmission;
    this.isJournal = isJournal;
    this.isEntriesInsertedSince = isEntriesInsertedSince;
    this.dateSubmitted = dateSubmitted;
  }
  
  /**
   * Returns the reactions according to the overwriteExistingLaws option.
   * 
   * @return a list of {@link Reaction} that can be equipped with laws from SABIO-RK
   */
  private List<Reaction> getSelectedReactions() {
    List<Reaction> reactions = new ArrayList<Reaction>();
    if (overwriteExistingRateLaws) {
      reactions = model.getReactions();
    }
    else {
      reactions = model.getReactionsWithoutKineticLaw();
    }
    return reactions;
  }
  
  /**
   * Returns a SABIO-RK query for the given query fields.
   * 
   * @param pathway
   * @param tissue
   * @param organism
   * @param cellularLocation
   * @return a SABIO-RK query for the given query fields
   */
  private String getSearchTermsQuery(String pathway, String tissue,
    String organism, String cellularLocation) {
    List<ValuePair<QueryField, String>> searchTerms = new ArrayList<ValuePair<QueryField, String>>();
    if (pathway instanceof String) {
      searchTerms.add(new ValuePair<SABIORK.QueryField, String>(
          SABIORK.QueryField.PATHWAY, pathway));
    }
    if (tissue instanceof String) {
      searchTerms.add(new ValuePair<SABIORK.QueryField, String>(
          SABIORK.QueryField.TISSUE, tissue));
    }
    if (organism instanceof String) {
      searchTerms.add(new ValuePair<SABIORK.QueryField, String>(
          SABIORK.QueryField.ORGANISM, organism));
    }
    if (cellularLocation instanceof String) {
      searchTerms.add(new ValuePair<SABIORK.QueryField, String>(
          SABIORK.QueryField.CELLULAR_LOCATION, cellularLocation));
    }
    return SABIORK.getSearchTermsQuery(searchTerms);
  }
  
  /**
   * Returns a SABIO-RK query for multiple filter options. If a filter option
   * is set to {@code null} the default value of that filter option will
   * be used. The query returned can be directly appended to another SABIO-RK
   * query since it already consists of a leading 'AND' or 'NOT'.
   * 
   * @param isWildtype
   * @param isMutant
   * @param isRecombinant
   * @param hasKineticData
   * @param lowerpHValue
   * @param upperpHValue
   * @param lowerTemperature
   * @param upperTemperature
   * @param isDirectSubmission
   * @param isJournal
   * @param isEntriesInsertedSince
   * @param dateSubmitted
   * @return a SABIO-RK query for the given filter options
   */
  private String getFilterOptionsQuery(Boolean isWildtype, Boolean isMutant,
    Boolean isRecombinant, Boolean hasKineticData, Double lowerpHValue,
    Double upperpHValue, Double lowerTemperature,
    Double upperTemperature, Boolean isDirectSubmission,
    Boolean isJournal, Boolean isEntriesInsertedSince,
    String dateSubmitted) {
    Date date = null;
    if (dateSubmitted instanceof String) {
      try {
        date = new SimpleDateFormat("dd/MM/yyyy").parse(dateSubmitted);
      } catch (ParseException e) {
        date = null;
        isEntriesInsertedSince = false;
      }
    }
    return SABIORK.getFilterOptionsQuery(isWildtype, isMutant,
      isRecombinant, hasKineticData, lowerpHValue, upperpHValue,
      lowerTemperature, upperTemperature, isDirectSubmission,
      isJournal, isEntriesInsertedSince, date);
  }
  
  /**
   * Starts the SABIO-RK wizard and returns the changed reactions.
   * 
   * @return the list of changed reactions
   */
  public Set<Reaction> getResult() {
    Set<Reaction> changedReactions = new HashSet<Reaction>();
    List<Reaction> selectedReactions = getSelectedReactions();
    String searchTermsQuery = getSearchTermsQuery(pathway, tissue,
      organism, cellularLocation);
    String filterOptionsQuery = getFilterOptionsQuery(isWildtype, isMutant,
      isRecombinant, hasKineticData, lowerpHValue, upperpHValue,
      lowerTemperature, upperTemperature, isDirectSubmission,
      isJournal, isEntriesInsertedSince, dateSubmitted);
    
    /**
     * Search for Kinetic Laws
     */
    List<SearchAResult> searchAResults = new ArrayList<SearchAResult>();
    for (Reaction selectedReaction : selectedReactions) {
      List<KineticLawImporter> possibleKineticLawImporters = new ArrayList<KineticLawImporter>();
      List<KineticLawImporter> impossibleKineticLawImporters = new ArrayList<KineticLawImporter>();
      List<KineticLawImporter> totalKineticLawImporters = new ArrayList<KineticLawImporter>();
      String keggReactionID = model.getKeggReactionID(selectedReaction);
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
          System.err.println(e.getMessage());
          return changedReactions;
        } catch (WebServiceResponseException e) {
          System.err.println(e.getMessage());
          return changedReactions;
        } catch (IOException e) {
          System.err.println(e.getMessage());
          return changedReactions;
        } catch (XMLStreamException e) {
          System.err.println(e.getMessage());
          return changedReactions;
        }
        for (KineticLaw kineticLaw : kineticLaws) {
          if (kineticLaw != null) {
            KineticLawImporter kineticLawImporter = new KineticLawImporter(
              kineticLaw, selectedReaction);
            totalKineticLawImporters.add(kineticLawImporter);
            if (kineticLawImporter.isImportableKineticLaw()) {
              possibleKineticLawImporters.add(kineticLawImporter);
            } else {
              impossibleKineticLawImporters
              .add(kineticLawImporter);
            }
          }
        }
      }
      searchAResults.add(new SearchAResult(selectedReaction,
        possibleKineticLawImporters, impossibleKineticLawImporters,
        totalKineticLawImporters));
      System.out.println(selectedReaction.getName() + " ["
          + possibleKineticLawImporters.size() + "|"
          + impossibleKineticLawImporters.size() + "|"
          + totalKineticLawImporters.size() + "]");
    }
    
    /**
     * Get all importable Kinetic Laws
     */
    List<KineticLawImporter> selectedKineticLawImporters = new ArrayList<KineticLawImporter>();
    for (SearchAResult searchAResult : searchAResults) {
      KineticLawImporter selectedKineticLawImporter = searchAResult
          .getSelectedKineticLawImporter();
      if (selectedKineticLawImporter != null) {
        selectedKineticLawImporters.add(selectedKineticLawImporter);
      }
    }
    
    /**
     * Print search result
     */
    if (selectedKineticLawImporters.isEmpty()) {
      System.out.println("No results were found for your query.");
    } else {
      for (KineticLawImporter selectedKineticLawImporter : selectedKineticLawImporters) {
        if (selectedKineticLawImporter.isImportableKineticLaw()) {
          selectedKineticLawImporter.importKineticLaw();
          changedReactions.add(selectedKineticLawImporter.getReaction());
          System.out.println("\n\n"
              + selectedKineticLawImporter.getReport());
        }
      }
      System.out.println("\n\nThe specified changes will be applied to your model.\n");
      model.applyChanges();
    }
    return changedReactions;
  }
  
}
