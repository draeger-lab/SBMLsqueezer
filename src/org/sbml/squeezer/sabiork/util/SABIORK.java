package org.sbml.squeezer.sabiork.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import javax.xml.stream.XMLStreamException;
import org.sbml.jsbml.KineticLaw;
import org.sbml.jsbml.Reaction;
import org.sbml.jsbml.SBMLDocument;
import org.sbml.jsbml.SBMLReader;

public class SABIORK {

	public enum QueryField {

		ENTRY_ID("EntryID"), PATHWAY("Pathway"), KEGG_REACTION_ID("KeggReactionID"), SABIO_REACTION_ID("SabioReactionID"), ANY_ROLE("AnyRole"), SUBSTRATE("Substrate"), PRODUCT("Product"), INHIBITOR("Inhibitor"), CATALYST("Catalyst"), COFACTOR("Cofactor"), ACTIVATOR("Activator"), OTHER_MODIFIER("OtherModifier"), PUBCHEM_ID("PubChemID"), KEGG_ID("KeggID"), CHEBI_ID("ChebiID"), SABIO_COMPOUND_ID("SabioCompoundID"), ENZYMENAME("Enzymename"), ECNUMBER("ECNumber"), UNIPROT_ID("UniprotID"), TISSUE("Tissue"), ORGANISM("Organism"), CELLULAR_LOCATION("CellularLocation"), PARAMETERTYPE("Parametertype"), KINETIC_MECHANISM_TYPE("KineticMechanismType"), ASSOCIATED_SPECIES("AssociatedSpecies"), TITLE("Title"), AUTHOR("Author"), YEAR("Year"), PUBMED_ID("PubmedID"), EXPERIMENT_ID("ExperimentID"), ENZYME_TYPE("EnzymeType"), INFOSOURCE_TYP("InfosourceTyp"), HAS_KINETIC_DATA("HasKineticData"), IS_RECOMBINANT("IsRecombinant"), PH_VALUE_RANGE("pHValueRange"), TEMPERATURE_RANGE("TemperatureRange"), DATE_SUBMITTED("DateSubmitted");

		private final String name;

		private QueryField(String name) {
			this.name = name;
		}

		public String toString() {
			return name;
		}

	}

	public enum Resource {

		SABIOENTRYIDS_BY_QUERY("http://sabio.h-its.org/sabioRestWebServices/searchKineticLaws/kinlaws?q="), SBMLMODEL_BY_SABIOENTRYID("http://sabio.h-its.org/sabioRestWebServices/kineticLaws/"), SBMLMODEL_BY_SABIOENTRYIDS("http://sabio.h-its.org/sabioRestWebServices/kineticLaws?kinlawids="), SBMLMODEL_BY_QUERY("http://sabio.h-its.org/sabioRestWebServices/searchKineticLaws/sbml?q="), SUGGESTIONS_COMPOUNDS("http://sabio.h-its.org/sabioRestWebServices/suggestions/Compounds?searchCompounds="), SUGGESTIONS_ENZYMES("http://sabio.h-its.org/sabioRestWebServices/suggestions/Enzymes?searchEnzymes="), SUGGESTIONS_ORGANISMS("http://sabio.h-its.org/sabioRestWebServices/suggestions/Organisms?searchOrganisms="), SUGGESTIONS_PATHWAYS("http://sabio.h-its.org/sabioRestWebServices/suggestions/Pathways?searchPathways="), SUGGESTIONS_TISSUES("http://sabio.h-its.org/sabioRestWebServices/suggestions/Tissues?searchTissues="), SUGGESTIONS_UNIPROTIDS("http://sabio.h-its.org/sabioRestWebServices/suggestions/UniprotIDs?searchUniprotIDs="), SUGGESTIONS_KEGGCOMPOUNDIDS("http://sabio.h-its.org/sabioRestWebServices/suggestions/KEGGCompoundIDs?searchKEGGCompoundIDs="), SUGGESTIONS_KEGGREACTIONIDS("http://sabio.h-its.org/sabioRestWebServices/suggestions/KEGGReactionIDs?searchKEGGReactionIDs="), SUGGESTIONS_SABIOCOMPOUNDIDS("http://sabio.h-its.org/sabioRestWebServices/suggestions/SABIOCompoundIDs?searchSABIOCompoundIDs="), SUGGESTIONS_SABIOREACTIONIDS("http://sabio.h-its.org/sabioRestWebServices/suggestions/SABIOReactionIDs?searchSABIOReactionIDs="), SUGGESTIONS_CHEBICOMPOUNDIDS("http://sabio.h-its.org/sabioRestWebServices/suggestions/CHEBICompoundIDs?searchCHEBICompoundIDs="), SUGGESTIONS_PUBCHEMCOMPOUNDIDS("http://sabio.h-its.org/sabioRestWebServices/suggestions/PUBCHEMCompoundIDs?searchPUBCHEMCompoundIDs="), SUGGESTIONS_PUBMEDIDS("http://sabio.h-its.org/sabioRestWebServices/suggestions/PubmedIDs?searchPubmedIDs=");

		private final String url;

		private Resource(String url) {
			this.url = url;
		}

		public String getURL(String query) throws UnsupportedEncodingException {
			return url + URLEncoder.encode(query, "UTF-8");
		}

	}

	private static String getResourceResponse(String url) throws WebServiceConnectException, WebServiceResponseException, IOException {
		try {
			URL resourceURL = new URL(url);
			HttpURLConnection resourceConnection = (HttpURLConnection) resourceURL.openConnection();
			if (resourceConnection.getResponseCode() == 200) {
				StringBuilder resourceResponse = new StringBuilder();
				BufferedReader resourceReader = new BufferedReader(new InputStreamReader(resourceConnection.getInputStream()));
				for (String line = resourceReader.readLine(); line != null; line = resourceReader.readLine()) {
					resourceResponse.append(line);
				}
				return resourceResponse.toString();
			} else {
				throw new WebServiceResponseException("SABIO-RK returned HTTP response code " + resourceConnection.getResponseCode() + " for URL: " + resourceConnection.getURL(), resourceConnection.getResponseCode());
			}
		} catch (UnknownHostException e) {
			throw new WebServiceConnectException("Unable to connect to SABIO-RK.", e);
		}
	}

	private static List<KineticLaw> getKineticLaws(SBMLDocument sbmlDocument) {
		List<KineticLaw> kineticLaws = new ArrayList<KineticLaw>();
		for (Reaction reaction : sbmlDocument.getModel().getListOfReactions()) {
			if (reaction.isSetKineticLaw()) {
				kineticLaws.add(reaction.getKineticLaw());
			}
		}
		return kineticLaws;
	}

	private static String getQuery(List<Integer> ids) {
		StringBuilder query = new StringBuilder();
		for (Integer id : ids) {
			if (query.length() > 0) {
				query.append(",");
			}
			query.append(id);
		}
		return query.toString();
	}

	private static String getAnnotationElementTextContent(KineticLaw kineticLaw, String annotationElementQNameLocalPart) throws UnsupportedEncodingException, XMLStreamException {
		String annotationElementTextContent = "";
		String nonRDFannotation = kineticLaw.getAnnotation().getNonRDFannotation();
		if (nonRDFannotation != null && !nonRDFannotation.trim().isEmpty()) {
			annotationElementTextContent = XMLParser.getFirstElementTextContent(nonRDFannotation, annotationElementQNameLocalPart, "http://sabiork.h-its.org");
		}
		return annotationElementTextContent;
	}

	public static List<Integer> getIDs(String query) throws WebServiceConnectException, WebServiceResponseException, IOException, XMLStreamException {
		List<Integer> ids = new ArrayList<Integer>();
		String url = Resource.SABIOENTRYIDS_BY_QUERY.getURL(query);
		String resourceResponse = getResourceResponse(url);
		if (!resourceResponse.equals("No results found for query")) {
			for (String id : XMLParser.getMultipleElementTextContent(resourceResponse, "SabioEntryID", "")) {
				ids.add(Integer.valueOf(id));
			}
		}
		return ids;
	}

	public static KineticLaw getKineticLaw(int id) throws WebServiceConnectException, WebServiceResponseException, IOException, XMLStreamException {
		KineticLaw kineticLaw = null;
		String url = Resource.SBMLMODEL_BY_SABIOENTRYID.getURL(String.valueOf(id));
		String resourceResponse = getResourceResponse(url);
		if (!resourceResponse.equals("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?><xmlErrorMessage><messageText>You do not have permission to view Kinetic Law</messageText></xmlErrorMessage>")) {
			SBMLDocument sbmlDocument = SBMLReader.read(resourceResponse);
			List<KineticLaw> kineticLaws = getKineticLaws(sbmlDocument);
			if (!kineticLaws.isEmpty()) {
				kineticLaw = kineticLaws.get(0);
			}
		}
		return kineticLaw;
	}

	public static List<KineticLaw> getKineticLaws(List<Integer> ids) throws WebServiceConnectException, WebServiceResponseException, IOException, XMLStreamException {
		List<KineticLaw> kineticLaws = new ArrayList<KineticLaw>();
		String url = Resource.SBMLMODEL_BY_SABIOENTRYIDS.getURL(getQuery(ids));
		String resourceResponse = getResourceResponse(url);
		if (!resourceResponse.equals("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?><xmlErrorMessage><messageText>You do not have permission to view Kinetic Law</messageText></xmlErrorMessage>")) {
			SBMLDocument sbmlDocument = SBMLReader.read(resourceResponse);
			kineticLaws.addAll(getKineticLaws(sbmlDocument));
		}
		return kineticLaws;
	}

	public static List<KineticLaw> getKineticLaws(String query) throws WebServiceConnectException, WebServiceResponseException, IOException, XMLStreamException {
		List<KineticLaw> kineticLaws = new ArrayList<KineticLaw>();
		String url = Resource.SBMLMODEL_BY_QUERY.getURL(query);
		String resourceResponse = getResourceResponse(url);
		if (!resourceResponse.equals("No results found for query")) {
			SBMLDocument sbmlDocument = SBMLReader.read(resourceResponse);
			kineticLaws.addAll(getKineticLaws(sbmlDocument));
		}
		return kineticLaws;
	}

	public static List<String> getSuggestions(QueryField queryField, String partialString) throws WebServiceConnectException, WebServiceResponseException, IOException, XMLStreamException {
		List<String> suggestions = new ArrayList<String>();
		switch (queryField) {
		case PATHWAY:
			suggestions.addAll(XMLParser.getMultipleElementTextContent(getResourceResponse(Resource.SUGGESTIONS_PATHWAYS.getURL(partialString)), "Pathway", ""));
			break;
		case KEGG_REACTION_ID:
			suggestions.addAll(XMLParser.getMultipleElementTextContent(getResourceResponse(Resource.SUGGESTIONS_KEGGREACTIONIDS.getURL(partialString)), "KEGGReactionID", ""));
			break;
		case SABIO_REACTION_ID:
			suggestions.addAll(XMLParser.getMultipleElementTextContent(getResourceResponse(Resource.SUGGESTIONS_SABIOREACTIONIDS.getURL(partialString)), "SABIOReactionID", ""));
			break;
		case ANY_ROLE:
			suggestions.addAll(XMLParser.getMultipleElementTextContent(getResourceResponse(Resource.SUGGESTIONS_COMPOUNDS.getURL(partialString)), "Compound", ""));
			break;
		case PUBCHEM_ID:
			suggestions.addAll(XMLParser.getMultipleElementTextContent(getResourceResponse(Resource.SUGGESTIONS_PUBCHEMCOMPOUNDIDS.getURL(partialString)), "PUBCHEMCompoundID", ""));
			break;
		case KEGG_ID:
			suggestions.addAll(XMLParser.getMultipleElementTextContent(getResourceResponse(Resource.SUGGESTIONS_KEGGCOMPOUNDIDS.getURL(partialString)), "KEGGCompoundID", ""));
			break;
		case CHEBI_ID:
			suggestions.addAll(XMLParser.getMultipleElementTextContent(getResourceResponse(Resource.SUGGESTIONS_CHEBICOMPOUNDIDS.getURL(partialString)), "CHEBICompoundID", ""));
			break;
		case SABIO_COMPOUND_ID:
			suggestions.addAll(XMLParser.getMultipleElementTextContent(getResourceResponse(Resource.SUGGESTIONS_SABIOCOMPOUNDIDS.getURL(partialString)), "SABIOCompoundID", ""));
			break;
		case ENZYMENAME:
			suggestions.addAll(XMLParser.getMultipleElementTextContent(getResourceResponse(Resource.SUGGESTIONS_ENZYMES.getURL(partialString)), "Enzyme", ""));
			break;
		case UNIPROT_ID:
			suggestions.addAll(XMLParser.getMultipleElementTextContent(getResourceResponse(Resource.SUGGESTIONS_UNIPROTIDS.getURL(partialString)), "UniprotID", ""));
			break;
		case TISSUE:
			suggestions.addAll(XMLParser.getMultipleElementTextContent(getResourceResponse(Resource.SUGGESTIONS_TISSUES.getURL(partialString)), "Tissue", ""));
			break;
		case ORGANISM:
			suggestions.addAll(XMLParser.getMultipleElementTextContent(getResourceResponse(Resource.SUGGESTIONS_ORGANISMS.getURL(partialString)), "Organism", ""));
			break;
		case PUBMED_ID:
			suggestions.addAll(XMLParser.getMultipleElementTextContent(getResourceResponse(Resource.SUGGESTIONS_PUBMEDIDS.getURL(partialString)), "PubmedID", ""));
			break;
		}
		return suggestions;
	}

	public static String getKineticLawID(KineticLaw kineticLaw) throws UnsupportedEncodingException, XMLStreamException {
		return getAnnotationElementTextContent(kineticLaw, "kineticLawID");
	}

	public static String getStartValuepH(KineticLaw kineticLaw) throws UnsupportedEncodingException, XMLStreamException {
		return getAnnotationElementTextContent(kineticLaw, "startValuepH");
	}

	public static String getStartValueTemperature(KineticLaw kineticLaw) throws UnsupportedEncodingException, XMLStreamException {
		return getAnnotationElementTextContent(kineticLaw, "startValueTemperature");
	}

	public static String getTemperatureUnit(KineticLaw kineticLaw) throws UnsupportedEncodingException, XMLStreamException {
		return getAnnotationElementTextContent(kineticLaw, "temperatureUnit");
	}

	public static String getBuffer(KineticLaw kineticLaw) throws UnsupportedEncodingException, XMLStreamException {
		return getAnnotationElementTextContent(kineticLaw, "buffer");
	}

}