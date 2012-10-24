/*
 * $Id: ConsoleWizard.java 971 2012-08-17 13:36:54Z keller$
 * $URL: https://rarepos.cs.uni-tuebingen.de/svn-path/SBMLsqueezer/trunk/src/org/sbml/squeezer/sabiork/wizard/console/ConsoleWizard.java$
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
package org.sbml.squeezer.sabiork.wizard.console;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Scanner;
import javax.xml.stream.XMLStreamException;
import org.sbml.jsbml.KineticLaw;
import org.sbml.jsbml.Reaction;
import org.sbml.jsbml.SBMLDocument;
import org.sbml.jsbml.util.ValuePair;
import org.sbml.squeezer.SubmodelController;
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
 * @version $Rev$
 */
public class ConsoleWizard {

	private WizardModel model;
	private Integer reactionFilter;
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
	 * @param reactionFilter
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
	public ConsoleWizard(SBMLDocument sbmlDocument, Integer reactionFilter,
			String pathway, String tissue, String organism,
			String cellularLocation, Boolean isWildtype, Boolean isMutant,
			Boolean isRecombinant, Boolean hasKineticData, Double lowerpHValue,
			Double upperpHValue, Double lowerTemperature,
			Double upperTemperature, Boolean isDirectSubmission,
			Boolean isJournal, Boolean isEntriesInsertedSince,
			String dateSubmitted) {
		//TODO check settings for overwriting laws
		this.model = new WizardModel(sbmlDocument, true);
		this.reactionFilter = reactionFilter;
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
	 * Returns the reactions according to the <code>reactionFilter</code>.
	 * 
	 * @param reactionFilter
	 *            a filter that specifies which type of {@link Reaction} should
	 *            be considered. All reactions (1), reactions with a
	 *            {@link KineticLaw} (2), reactions without a {@link KineticLaw}
	 *            (3), reversible reactions (4), irreversible reactions (5),
	 *            fast reactions (6) and slow reactions (7)
	 * @return a list of {@link Reaction}
	 */
	private List<Reaction> getSelectedReactions(Integer reactionFilter) {
		List<Reaction> reactions = new ArrayList<Reaction>();
		if (reactionFilter instanceof Integer) {
			switch (reactionFilter) {
			case 1:
				reactions = model.getReactions();
				break;
			case 2:
				reactions = model.getReactionsWithKineticLaw();
				break;
			case 3:
				reactions = model.getReactionsWithoutKineticLaw();
				break;
			case 4:
				reactions = model.getReversibleReactions();
				break;
			case 5:
				reactions = model.getIrreversibleReactions();
				break;
			case 6:
				reactions = model.getFastReactions();
				break;
			case 7:
				reactions = model.getSlowReactions();
				break;
			default:
				reactions = model.getReactions();
			}
		} else {
			reactions = model.getReactions();
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
	 * Starts the SABIO-RK wizard and returns the result of the wizard.
	 * 
	 * @return the resulting {@link SBMLDocument}
	 */
	public SubmodelController getResult() {
		List<Reaction> selectedReactions = getSelectedReactions(reactionFilter);
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
					return model.getResult();
				} catch (WebServiceResponseException e) {
					System.err.println(e.getMessage());
					return model.getResult();
				} catch (IOException e) {
					System.err.println(e.getMessage());
					return model.getResult();
				} catch (XMLStreamException e) {
					System.err.println(e.getMessage());
					return model.getResult();
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
			return model.getResult();
		} else {
			for (KineticLawImporter selectedKineticLawImporter : selectedKineticLawImporters) {
				if (selectedKineticLawImporter.isImportableKineticLaw()) {
					selectedKineticLawImporter.importKineticLaw();
					System.out.println("\n\n"
							+ selectedKineticLawImporter.getReport());
				}
			}
			Scanner scanner = new Scanner(System.in);
			String confirmMessage = "\n\nThe specified changes will be applied to your model.\nPlease confirm to proceed.\n\nY = Yes\nN = No\n\nApply changes?";
			System.out.println(confirmMessage);
			String input = scanner.nextLine().toLowerCase();
			while (!(input.equals("y") || input.equals("n"))) {
				System.out.println(confirmMessage);
				input = scanner.nextLine().toLowerCase();
			}
			if (input.equals("y")) {
				model.applyChanges();
			}
			return model.getResult();
		}
	}

}
