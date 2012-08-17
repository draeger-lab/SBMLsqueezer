/*
 * $Id: SABIORK.java 973 2012-08-17 13:40:55Z keller$
 * $URL: https://rarepos.cs.uni-tuebingen.de/svn-path/SBMLsqueezer/trunk/src/org/sbml/squeezer/sabiork/SABIORK.java$
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
package org.sbml.squeezer.sabiork;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.NoRouteToHostException;
import java.net.URL;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.xml.stream.XMLStreamException;
import org.sbml.jsbml.KineticLaw;
import org.sbml.jsbml.Reaction;
import org.sbml.jsbml.SBMLDocument;
import org.sbml.jsbml.SBMLReader;
import org.sbml.squeezer.sabiork.util.Pair;
import org.sbml.squeezer.sabiork.util.WebServiceConnectException;
import org.sbml.squeezer.sabiork.util.WebServiceResponseException;
import org.sbml.squeezer.sabiork.util.XMLParser;

/**
 * The SABIORK class provides access to the SABIO-RK database via their RESTful
 * Web Services.
 * 
 * @author Matthias Rall
 * @version $Rev$
 * ${tags}
 */
public class SABIORK {

	public enum QueryField {

		ENTRY_ID("EntryID"), PATHWAY("Pathway"), KEGG_REACTION_ID(
				"KeggReactionID"), SABIO_REACTION_ID("SabioReactionID"), ANY_ROLE(
				"AnyRole"), SUBSTRATE("Substrate"), PRODUCT("Product"), INHIBITOR(
				"Inhibitor"), CATALYST("Catalyst"), COFACTOR("Cofactor"), ACTIVATOR(
				"Activator"), OTHER_MODIFIER("OtherModifier"), PUBCHEM_ID(
				"PubChemID"), KEGG_ID("KeggID"), CHEBI_ID("ChebiID"), SABIO_COMPOUND_ID(
				"SabioCompoundID"), ENZYMENAME("Enzymename"), EC_NUMBER(
				"ECNumber"), UNIPROT_ID("UniprotID"), TISSUE("Tissue"), ORGANISM(
				"Organism"), CELLULAR_LOCATION("CellularLocation"), PARAMETERTYPE(
				"Parametertype"), KINETIC_MECHANISM_TYPE("KineticMechanismType"), ASSOCIATED_SPECIES(
				"AssociatedSpecies"), TITLE("Title"), AUTHOR("Author"), YEAR(
				"Year"), PUBMED_ID("PubmedID"), EXPERIMENT_ID("ExperimentID"), ENZYME_TYPE(
				"EnzymeType"), INFOSOURCE_TYPE("InfosourceType"), HAS_KINETIC_DATA(
				"HasKineticData"), IS_RECOMBINANT("IsRecombinant"), PH_VALUE_RANGE(
				"pHValueRange"), TEMPERATURE_RANGE("TemperatureRange"), DATE_SUBMITTED(
				"DateSubmitted");

		private final String name;

		private QueryField(String name) {
			this.name = name;
		}

		public String toString() {
			return name;
		}

	}

	public enum Resource {

		ENTRY_IDS_BY_QUERY(
				"http://sabio.h-its.org/sabioRestWebServices/searchKineticLaws/kinlaws?q="), SBML_MODEL_BY_ENTRY_ID(
				"http://sabio.h-its.org/sabioRestWebServices/kineticLaws/"), SBML_MODEL_BY_ENTRY_IDS(
				"http://sabio.h-its.org/sabioRestWebServices/kineticLaws?kinlawids="), SBML_MODEL_BY_QUERY(
				"http://sabio.h-its.org/sabioRestWebServices/searchKineticLaws/sbml?q="), SUGGESTIONS_COMPOUNDS(
				"http://sabio.h-its.org/sabioRestWebServices/suggestions/Compounds?searchCompounds="), SUGGESTIONS_ENZYMES(
				"http://sabio.h-its.org/sabioRestWebServices/suggestions/Enzymes?searchEnzymes="), SUGGESTIONS_ORGANISMS(
				"http://sabio.h-its.org/sabioRestWebServices/suggestions/Organisms?searchOrganisms="), SUGGESTIONS_PATHWAYS(
				"http://sabio.h-its.org/sabioRestWebServices/suggestions/Pathways?searchPathways="), SUGGESTIONS_TISSUES(
				"http://sabio.h-its.org/sabioRestWebServices/suggestions/Tissues?searchTissues="), SUGGESTIONS_UNIPROT_IDS(
				"http://sabio.h-its.org/sabioRestWebServices/suggestions/UniprotIDs?searchUniprotIDs="), SUGGESTIONS_KEGG_COMPOUND_IDS(
				"http://sabio.h-its.org/sabioRestWebServices/suggestions/KEGGCompoundIDs?searchKEGGCompoundIDs="), SUGGESTIONS_KEGG_REACTION_IDS(
				"http://sabio.h-its.org/sabioRestWebServices/suggestions/KEGGReactionIDs?searchKEGGReactionIDs="), SUGGESTIONS_SABIO_COMPOUND_IDS(
				"http://sabio.h-its.org/sabioRestWebServices/suggestions/SABIOCompoundIDs?searchSABIOCompoundIDs="), SUGGESTIONS_SABIO_REACTION_IDS(
				"http://sabio.h-its.org/sabioRestWebServices/suggestions/SABIOReactionIDs?searchSABIOReactionIDs="), SUGGESTIONS_CHEBI_COMPOUND_IDS(
				"http://sabio.h-its.org/sabioRestWebServices/suggestions/CHEBICompoundIDs?searchCHEBICompoundIDs="), SUGGESTIONS_PUBCHEM_COMPOUND_IDS(
				"http://sabio.h-its.org/sabioRestWebServices/suggestions/PUBCHEMCompoundIDs?searchPUBCHEMCompoundIDs="), SUGGESTIONS_PUBMED_IDS(
				"http://sabio.h-its.org/sabioRestWebServices/suggestions/PubmedIDs?searchPubmedIDs=");

		private final String url;

		private Resource(String url) {
			this.url = url;
		}

		public String getURL(String query) throws UnsupportedEncodingException {
			return url + URLEncoder.encode(query, "UTF-8");
		}

	}

	/**
	 * Returns the direct response from the SABIO-RK resource.
	 * 
	 * @param url
	 *            the URL of the resource
	 * @return the response from the SABIO-RK resource
	 * @throws WebServiceConnectException
	 * @throws WebServiceResponseException
	 * @throws IOException
	 */
	private static String getResourceResponse(String url)
			throws WebServiceConnectException, WebServiceResponseException,
			IOException {
		try {
			URL resourceURL = new URL(url);
			HttpURLConnection resourceConnection = (HttpURLConnection) resourceURL
					.openConnection();
			if (resourceConnection.getResponseCode() == 200) {
				StringBuilder resourceResponse = new StringBuilder();
				BufferedReader resourceReader = new BufferedReader(
						new InputStreamReader(
								resourceConnection.getInputStream()));
				for (String line = resourceReader.readLine(); line != null; line = resourceReader
						.readLine()) {
					resourceResponse.append(line);
				}
				return resourceResponse.toString();
			} else {
				throw new WebServiceResponseException(
						"SABIO-RK returned HTTP response code "
								+ resourceConnection.getResponseCode()
								+ " for URL: " + resourceConnection.getURL(),
						resourceConnection.getResponseCode());
			}
		} catch (UnknownHostException e) {
			throw new WebServiceConnectException(
					"Unable to connect to SABIO-RK.", e);
		} catch (NoRouteToHostException e) {
			throw new WebServiceConnectException(
					"Unable to connect to SABIO-RK.", e);
		}
	}

	/**
	 * Returns all {@link KineticLaw} from the SBML document.
	 * 
	 * @param sbmlDocument
	 *            the SBML document
	 * @return a list of all {@link KineticLaw} from the SBML document
	 */
	private static List<KineticLaw> getKineticLaws(SBMLDocument sbmlDocument) {
		List<KineticLaw> kineticLaws = new ArrayList<KineticLaw>();
		if (sbmlDocument.getModel().isSetListOfReactions()) {
			for (Reaction reaction : sbmlDocument.getModel()
					.getListOfReactions()) {
				if (reaction.isSetKineticLaw()) {
					kineticLaws.add(reaction.getKineticLaw());
				}
			}
		}
		return kineticLaws;
	}

	/**
	 * Returns a SABIO-RK query for multiple SABIO-RK entry ids.
	 * 
	 * @param ids
	 *            the SABIO-RK entry ids
	 * @return a query consisting of the entry ids
	 */
	private static String getIDsQuery(List<Integer> ids) {
		StringBuilder query = new StringBuilder();
		for (Integer id : ids) {
			if (query.length() > 0) {
				query.append(",");
			}
			query.append(id);
		}
		return query.toString();
	}

	/**
	 * Returns the SABIO-RK specific annotation of the {@link KineticLaw}
	 * described by the given XML element.
	 * 
	 * @param kineticLaw
	 *            the {@link KineticLaw}
	 * @param annotationElementQNameLocalPart
	 *            the qualified name of the XML element in the annotation
	 * @return the annotation described by the XML element
	 * @throws UnsupportedEncodingException
	 * @throws XMLStreamException
	 */
	private static String getAnnotationElementTextContent(
			KineticLaw kineticLaw, String annotationElementQNameLocalPart)
			throws UnsupportedEncodingException, XMLStreamException {
		String annotationElementTextContent = "";
		String nonRDFannotation = kineticLaw.getAnnotation()
				.getNonRDFannotation();
		if (nonRDFannotation != null && !nonRDFannotation.trim().isEmpty()) {
			annotationElementTextContent = XMLParser.getXMLElementTextContent(
					nonRDFannotation, annotationElementQNameLocalPart,
					"http://sabiork.h-its.org");
		}
		return annotationElementTextContent;
	}

	/**
	 * Returns the SABIO-RK entry ids matching the given query.
	 * 
	 * @param query
	 *            the query
	 * @return a list of all SABIO-RK entry ids matching the given query
	 * @throws WebServiceConnectException
	 * @throws WebServiceResponseException
	 * @throws IOException
	 * @throws XMLStreamException
	 */
	public static List<Integer> getIDs(String query)
			throws WebServiceConnectException, WebServiceResponseException,
			IOException, XMLStreamException {
		List<Integer> ids = new ArrayList<Integer>();
		String url = Resource.ENTRY_IDS_BY_QUERY.getURL(query);
		String resourceResponse = getResourceResponse(url);
		if (!resourceResponse.equals("No results found for query")) {
			for (String id : XMLParser.getMultipleXMLElementTextContent(
					resourceResponse, "SabioEntryID", "")) {
				ids.add(Integer.valueOf(id));
			}
		}
		return ids;
	}

	/**
	 * Returns the {@link KineticLaw} corresponding to the given SABIO-RK entry
	 * id.
	 * 
	 * @param id
	 *            the SABIO-RK entry id
	 * @return the {@link KineticLaw} corresponding to the given SABIO-RK entry
	 *         id or <code>null</code> if it doesn't exist
	 * @throws WebServiceConnectException
	 * @throws WebServiceResponseException
	 * @throws IOException
	 * @throws XMLStreamException
	 */
	public static KineticLaw getKineticLaw(int id)
			throws WebServiceConnectException, WebServiceResponseException,
			IOException, XMLStreamException {
		KineticLaw kineticLaw = null;
		String url = Resource.SBML_MODEL_BY_ENTRY_ID.getURL(String.valueOf(id));
		String resourceResponse = getResourceResponse(url);
		if (!resourceResponse
				.equals("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?><xmlErrorMessage><messageText>You do not have permission to view Kinetic Law</messageText></xmlErrorMessage>")) {
			SBMLDocument sbmlDocument = SBMLReader.read(resourceResponse);
			List<KineticLaw> kineticLaws = getKineticLaws(sbmlDocument);
			if (!kineticLaws.isEmpty()) {
				kineticLaw = kineticLaws.get(0);
			}
		}
		return kineticLaw;
	}

	/**
	 * Returns all {@link KineticLaw} corresponding to the given SABIO-RK entry
	 * ids.
	 * 
	 * @param ids
	 *            the SABIO-RK entry ids
	 * @return a list of all {@link KineticLaw} corresponding to the given
	 *         SABIO-RK entry ids
	 * @throws WebServiceConnectException
	 * @throws WebServiceResponseException
	 * @throws IOException
	 * @throws XMLStreamException
	 */
	public static List<KineticLaw> getKineticLaws(List<Integer> ids)
			throws WebServiceConnectException, WebServiceResponseException,
			IOException, XMLStreamException {
		List<KineticLaw> kineticLaws = new ArrayList<KineticLaw>();
		String url = Resource.SBML_MODEL_BY_ENTRY_IDS.getURL(getIDsQuery(ids));
		String resourceResponse = getResourceResponse(url);
		if (!resourceResponse
				.equals("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?><xmlErrorMessage><messageText>You do not have permission to view Kinetic Law</messageText></xmlErrorMessage>")) {
			SBMLDocument sbmlDocument = SBMLReader.read(resourceResponse);
			kineticLaws.addAll(getKineticLaws(sbmlDocument));
		}
		return kineticLaws;
	}

	/**
	 * Returns all {@link KineticLaw} matching the given query.
	 * 
	 * @param query
	 *            the query
	 * @return a list of all {@link KineticLaw} matching the given query
	 * @throws WebServiceConnectException
	 * @throws WebServiceResponseException
	 * @throws IOException
	 * @throws XMLStreamException
	 */
	public static List<KineticLaw> getKineticLaws(String query)
			throws WebServiceConnectException, WebServiceResponseException,
			IOException, XMLStreamException {
		List<KineticLaw> kineticLaws = new ArrayList<KineticLaw>();
		String url = Resource.SBML_MODEL_BY_QUERY.getURL(query);
		String resourceResponse = getResourceResponse(url);
		if (!resourceResponse.equals("No results found for query")) {
			SBMLDocument sbmlDocument = SBMLReader.read(resourceResponse);
			kineticLaws.addAll(getKineticLaws(sbmlDocument));
		}
		return kineticLaws;
	}

	/**
	 * Returns suggestions for a partial string with regard to a given SABIO-RK
	 * query field.
	 * 
	 * @param queryField
	 *            the SABIO-RK query field
	 * @param partialString
	 *            the partial string
	 * @return a list of suggestions for the partial string with regard to the
	 *         given SABIO-RK query field
	 * @throws WebServiceConnectException
	 * @throws WebServiceResponseException
	 * @throws IOException
	 * @throws XMLStreamException
	 */
	public static List<String> getSuggestions(QueryField queryField,
			String partialString) throws WebServiceConnectException,
			WebServiceResponseException, IOException, XMLStreamException {
		List<String> suggestions = new ArrayList<String>();
		switch (queryField) {
		case PATHWAY:
			suggestions.addAll(XMLParser.getMultipleXMLElementTextContent(
					getResourceResponse(Resource.SUGGESTIONS_PATHWAYS
							.getURL(partialString)), "Pathway", ""));
			break;
		case KEGG_REACTION_ID:
			suggestions.addAll(XMLParser.getMultipleXMLElementTextContent(
					getResourceResponse(Resource.SUGGESTIONS_KEGG_REACTION_IDS
							.getURL(partialString)), "KEGGReactionID", ""));
			break;
		case SABIO_REACTION_ID:
			suggestions.addAll(XMLParser.getMultipleXMLElementTextContent(
					getResourceResponse(Resource.SUGGESTIONS_SABIO_REACTION_IDS
							.getURL(partialString)), "SABIOReactionID", ""));
			break;
		case ANY_ROLE:
			suggestions.addAll(XMLParser.getMultipleXMLElementTextContent(
					getResourceResponse(Resource.SUGGESTIONS_COMPOUNDS
							.getURL(partialString)), "Compound", ""));
			break;
		case PUBCHEM_ID:
			suggestions
					.addAll(XMLParser
							.getMultipleXMLElementTextContent(
									getResourceResponse(Resource.SUGGESTIONS_PUBCHEM_COMPOUND_IDS
											.getURL(partialString)),
									"PUBCHEMCompoundID", ""));
			break;
		case KEGG_ID:
			suggestions.addAll(XMLParser.getMultipleXMLElementTextContent(
					getResourceResponse(Resource.SUGGESTIONS_KEGG_COMPOUND_IDS
							.getURL(partialString)), "KEGGCompoundID", ""));
			break;
		case CHEBI_ID:
			suggestions.addAll(XMLParser.getMultipleXMLElementTextContent(
					getResourceResponse(Resource.SUGGESTIONS_CHEBI_COMPOUND_IDS
							.getURL(partialString)), "CHEBICompoundID", ""));
			break;
		case SABIO_COMPOUND_ID:
			suggestions.addAll(XMLParser.getMultipleXMLElementTextContent(
					getResourceResponse(Resource.SUGGESTIONS_SABIO_COMPOUND_IDS
							.getURL(partialString)), "SABIOCompoundID", ""));
			break;
		case ENZYMENAME:
			suggestions.addAll(XMLParser.getMultipleXMLElementTextContent(
					getResourceResponse(Resource.SUGGESTIONS_ENZYMES
							.getURL(partialString)), "Enzyme", ""));
			break;
		case UNIPROT_ID:
			suggestions.addAll(XMLParser.getMultipleXMLElementTextContent(
					getResourceResponse(Resource.SUGGESTIONS_UNIPROT_IDS
							.getURL(partialString)), "UniprotID", ""));
			break;
		case TISSUE:
			suggestions.addAll(XMLParser.getMultipleXMLElementTextContent(
					getResourceResponse(Resource.SUGGESTIONS_TISSUES
							.getURL(partialString)), "Tissue", ""));
			break;
		case ORGANISM:
			suggestions.addAll(XMLParser.getMultipleXMLElementTextContent(
					getResourceResponse(Resource.SUGGESTIONS_ORGANISMS
							.getURL(partialString)), "Organism", ""));
			break;
		case PUBMED_ID:
			suggestions.addAll(XMLParser.getMultipleXMLElementTextContent(
					getResourceResponse(Resource.SUGGESTIONS_PUBMED_IDS
							.getURL(partialString)), "PubmedID", ""));
			break;
		}
		return suggestions;
	}

	/**
	 * Returns the SABIO-RK entry id of the given {@link KineticLaw}.
	 * 
	 * @param kineticLaw
	 *            the {@link KineticLaw}
	 * @return the SABIO-RK entry id of the given {@link KineticLaw} or an empty
	 *         {@link String} if it doesn't exist
	 * @throws UnsupportedEncodingException
	 * @throws XMLStreamException
	 */
	public static String getKineticLawID(KineticLaw kineticLaw)
			throws UnsupportedEncodingException, XMLStreamException {
		return getAnnotationElementTextContent(kineticLaw, "kineticLawID");
	}

	/**
	 * Returns the pH value of the given {@link KineticLaw}.
	 * 
	 * @param kineticLaw
	 *            the {@link KineticLaw}
	 * @return the pH value of the given {@link KineticLaw} or an empty
	 *         {@link String} if it doesn't exist
	 * @throws UnsupportedEncodingException
	 * @throws XMLStreamException
	 */
	public static String getStartValuepH(KineticLaw kineticLaw)
			throws UnsupportedEncodingException, XMLStreamException {
		return getAnnotationElementTextContent(kineticLaw, "startValuepH");
	}

	/**
	 * Returns the temperature of the given {@link KineticLaw}.
	 * 
	 * @param kineticLaw
	 *            the {@link KineticLaw}
	 * @return the temperature of the given {@link KineticLaw} or an empty
	 *         {@link String} if it doesn't exist
	 * @throws UnsupportedEncodingException
	 * @throws XMLStreamException
	 */
	public static String getStartValueTemperature(KineticLaw kineticLaw)
			throws UnsupportedEncodingException, XMLStreamException {
		return getAnnotationElementTextContent(kineticLaw,
				"startValueTemperature");
	}

	/**
	 * Returns the temperature unit of the given {@link KineticLaw}.
	 * 
	 * @param kineticLaw
	 *            the {@link KineticLaw}
	 * @return the temperature unit of the given {@link KineticLaw} or an empty
	 *         {@link String} if it doesn't exist
	 * @throws UnsupportedEncodingException
	 * @throws XMLStreamException
	 */
	public static String getTemperatureUnit(KineticLaw kineticLaw)
			throws UnsupportedEncodingException, XMLStreamException {
		return getAnnotationElementTextContent(kineticLaw, "temperatureUnit");
	}

	/**
	 * Returns the buffer of the given {@link KineticLaw}.
	 * 
	 * @param kineticLaw
	 *            the {@link KineticLaw}
	 * @return the buffer of the given {@link KineticLaw} or an empty
	 *         {@link String} if it doesn't exist
	 * @throws UnsupportedEncodingException
	 * @throws XMLStreamException
	 */
	public static String getBuffer(KineticLaw kineticLaw)
			throws UnsupportedEncodingException, XMLStreamException {
		return getAnnotationElementTextContent(kineticLaw, "buffer");
	}

	/**
	 * Returns a SABIO-RK query for multiple search terms. The search terms are
	 * joined by 'AND'. If the value of a search term is missing, the search
	 * term will not be a part of the final query.
	 * 
	 * @param searchTerms
	 *            the search terms
	 * @return a query consisting of the search terms
	 */
	public static String getSearchTermsQuery(
			List<Pair<QueryField, String>> searchTerms) {
		StringBuilder searchTermsQuery = new StringBuilder();
		for (Pair<QueryField, String> searchTerm : searchTerms) {
			String value = searchTerm.getValue().trim();
			if (!value.isEmpty()) {
				if (searchTermsQuery.length() > 0) {
					searchTermsQuery.append(" AND ");
				}
				if (value.contains(" ")) {
					searchTermsQuery.append(searchTerm.getKey() + ":\"" + value
							+ "\"");
				} else {
					searchTermsQuery.append(searchTerm.getKey() + ":" + value);
				}
			}
		}
		return searchTermsQuery.toString();
	}

	/**
	 * Returns a SABIO-RK query for multiple filter options. If a filter option
	 * is set to <code>null</code> the default value of that filter option will
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
	public static String getFilterOptionsQuery(Boolean isWildtype,
			Boolean isMutant, Boolean isRecombinant, Boolean hasKineticData,
			Double lowerpHValue, Double upperpHValue, Double lowerTemperature,
			Double upperTemperature, Boolean isDirectSubmission,
			Boolean isJournal, Boolean isEntriesInsertedSince,
			Date dateSubmitted) {
		StringBuilder filterOptionsQuery = new StringBuilder();
		SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
		if (isWildtype == null) {
			isWildtype = true;
		}
		if (isMutant == null) {
			isMutant = true;
		}
		if (isRecombinant == null) {
			isRecombinant = false;
		}
		if (hasKineticData == null) {
			hasKineticData = true;
		}
		if (lowerpHValue == null) {
			lowerpHValue = 0.0;
		}
		if (upperpHValue == null) {
			upperpHValue = 14.0;
		}
		if (lowerTemperature == null) {
			lowerTemperature = -10.0;
		}
		if (upperTemperature == null) {
			upperTemperature = 115.0;
		}
		if (isDirectSubmission == null) {
			isDirectSubmission = true;
		}
		if (isJournal == null) {
			isJournal = true;
		}
		if (isEntriesInsertedSince == null) {
			isEntriesInsertedSince = false;
		}
		if (dateSubmitted == null) {
			try {
				dateSubmitted = dateFormat.parse("15/10/2008");
			} catch (ParseException e) {
				e.printStackTrace();
			}
		}
		if (!(isWildtype && isMutant)) {
			if (isWildtype) {
				filterOptionsQuery.append(" AND " + QueryField.ENZYME_TYPE
						+ ":wildtype");
			} else {
				filterOptionsQuery.append(" NOT " + QueryField.ENZYME_TYPE
						+ ":wildtype");
			}
			if (isMutant) {
				filterOptionsQuery.append(" AND " + QueryField.ENZYME_TYPE
						+ ":mutant");
			} else {
				filterOptionsQuery.append(" NOT " + QueryField.ENZYME_TYPE
						+ ":mutant");
			}
		}
		if (isRecombinant) {
			filterOptionsQuery.append(" AND " + QueryField.IS_RECOMBINANT
					+ ":true");
		}
		if (hasKineticData) {
			filterOptionsQuery.append(" AND " + QueryField.HAS_KINETIC_DATA
					+ ":true");
		}
		filterOptionsQuery.append(" AND " + QueryField.PH_VALUE_RANGE + ":["
				+ lowerpHValue + " TO " + upperpHValue + "]");
		filterOptionsQuery.append(" AND " + QueryField.TEMPERATURE_RANGE + ":["
				+ lowerTemperature + " TO " + upperTemperature + "]");
		if (!(isDirectSubmission && isJournal)) {
			if (isDirectSubmission) {
				filterOptionsQuery.append(" AND " + QueryField.INFOSOURCE_TYPE
						+ ":\"Direct Submission\"");
			} else {
				filterOptionsQuery.append(" NOT " + QueryField.INFOSOURCE_TYPE
						+ ":\"Direct Submission\"");
			}
			if (isJournal) {
				filterOptionsQuery.append(" AND " + QueryField.INFOSOURCE_TYPE
						+ ":Journal");
			} else {
				filterOptionsQuery.append(" NOT " + QueryField.INFOSOURCE_TYPE
						+ ":Journal");
			}
		}
		if (isEntriesInsertedSince) {
			filterOptionsQuery.append(" AND " + QueryField.DATE_SUBMITTED + ":"
					+ dateFormat.format(dateSubmitted));
		}
		return filterOptionsQuery.toString();
	}

}