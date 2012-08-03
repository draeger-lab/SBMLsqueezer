package org.sbml.squeezer.sabiork;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import javax.xml.stream.XMLStreamException;

import org.apache.commons.logging.impl.Log4JLogger;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.log4j.lf5.LogLevel;
import org.biojava.bio.seq.io.ParseException;
import org.biojavax.bio.taxa.NCBITaxon;
import org.biojavax.bio.taxa.io.NCBITaxonomyLoader;
import org.biojavax.bio.taxa.io.SimpleNCBITaxonomyLoader;
import org.sbgn.bindings.Map;
import org.sbml.jsbml.AbstractSBase;
import org.sbml.jsbml.CVTerm;
import org.sbml.jsbml.KineticLaw;
import org.sbml.jsbml.Reaction;
import org.sbml.jsbml.SBMLDocument;
import org.sbml.jsbml.SBMLReader;
import org.sbml.jsbml.SBMLWriter;
import org.sbml.jsbml.CVTerm.Qualifier;
import org.sbml.jsbml.util.filters.CVTermFilter;
import org.sbml.jsbml.xml.parsers.SBMLCoreParser;
import org.sbml.squeezer.sabiork.util.KineticLawImporter;
import org.sbml.squeezer.sabiork.util.SABIORK;
import org.sbml.squeezer.sabiork.util.WebServiceConnectException;
import org.sbml.squeezer.sabiork.util.WebServiceResponseException;

import de.zbit.io.filefilter.SBFileFilter;

public class AutomaticSearch {

	private static String getKeggReactionID(Reaction reaction) {
		String keggReactionID = "";
		CVTermFilter filter = new CVTermFilter(CVTerm.Qualifier.BQB_IS);
		List<CVTerm> cvTerms = new LinkedList<CVTerm>();
		for (CVTerm cvTerm : reaction.getCVTerms()) {
			if (filter.accepts(cvTerm)) {
				cvTerms.add(cvTerm);
			}
		}
		for (CVTerm cvTerm : cvTerms) {
			for (String resource : cvTerm.getResources()) {
				keggReactionID = resource.replaceAll("urn:miriam:kegg.reaction:", "");
			}
		}
		return keggReactionID;
	}

	public static void main(String[] args) throws XMLStreamException, IOException, WebServiceConnectException, WebServiceResponseException, ParseException {
		automaticSearch(args[0], args[1]);
	}
	
	public static void automaticSearch(String rootFolder, String taxonomyFile) throws XMLStreamException, IOException, WebServiceConnectException, WebServiceResponseException, ParseException {
		LogManager.getLogger(SBMLCoreParser.class).setLevel(Level.OFF);
		LogManager.getLogger(AbstractSBase.class).setLevel(Level.OFF);
		int matched=0;
		int noReactionID=0;
		int matchingNotPossible=0;
		int noKineticLawFound=0;
		
		File rootFile = new File(rootFolder);
		String parent = rootFile.getParent().replace("\\", "/");
		String sabioRootFolder = parent + "/sabio/";
		(new File(sabioRootFolder)).mkdir();
		
		
		LinkedList<File> fileList = new LinkedList<File>();
		
		File[] files = rootFile.listFiles();
		
		for(File file: files) {
			fileList.add(file);
		}

		
		BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(taxonomyFile)));
		
		HashMap<Integer, String> taxonToName = new HashMap<Integer,String>();
		
		String line = reader.readLine();
		line = reader.readLine();
		while(line != null) {
			String[] field = line.split("\t");
			int number = Integer.valueOf(field[0]);
			if((!taxonToName.containsKey(number)) || (field[6].endsWith("scientific name"))) {
				taxonToName.put(number, field[2]);
			}
			line = reader.readLine();
		}
		SBFileFilter sbmlFilter = SBFileFilter.createSBMLFileFilter();
		
		
		while(fileList.size() > 0) {
			File file = fileList.pop();
			if((!file.isDirectory()) && (sbmlFilter.accept(file))) {
				/*
				 * SBML-Input
				 */
				SBMLDocument sbmlDocument = SBMLReader.read(file);
				//String organism = "homo sapiens";
				String organism = null;
				
				CVTermFilter filter = new CVTermFilter(Qualifier.BQB_OCCURS_IN, "urn:miriam:taxonomy");
				
				for(CVTerm cv: sbmlDocument.getModel().getAnnotation().getListOfCVTerms()) {
					if(filter.accepts(cv)) {
						for(String resource: cv.getResources()) {
							if(resource.contains("urn:miriam:taxonomy")) {
								String name = resource.replace("urn:miriam:taxonomy:", "");
								int t = Integer.valueOf(name);
								organism = taxonToName.get(t);
							}
						}
					}
				}
				/*
				 * Constraints (Falls du bestimmte zus�tzliche Bedingungen an die Suche stellen willst)
				 * 
				 * Der Constraint: " AND " + SABIORK.QueryField.HAS_KINETIC_DATA +
				 * ":true"
				 * 
				 * sollte aber immer verwendet werden, da SABIO-RK dann nur KineticLaws,
				 * die eine kinetische Gleichung besitzen, ausw�hlt.
				 */
				StringBuilder constraints = new StringBuilder();
				if(organism!=null) {
					constraints.append(" AND " + SABIORK.QueryField.ORGANISM + ":\""+organism+"\"");
				}
				//constraints.append(" AND " + SABIORK.QueryField.TISSUE + ":liver");
				//constraints.append(" AND " + SABIORK.QueryField.PH_VALUE_RANGE + ":[0.0 TO 14.0]");
				constraints.append(" AND " + SABIORK.QueryField.HAS_KINETIC_DATA + ":true");
		
				for (Reaction reaction : sbmlDocument.getModel().getListOfReactions()) {
					String keggReactionID = getKeggReactionID(reaction);
					if (!keggReactionID.isEmpty()) {
						String query = SABIORK.QueryField.KEGG_REACTION_ID + ":" + keggReactionID + constraints;
						List<KineticLaw> kineticLaws = SABIORK.getKineticLaws(query);
						boolean imported = false;
						for (KineticLaw kineticLaw : kineticLaws) {
							KineticLawImporter importer = new KineticLawImporter(kineticLaw, reaction);
							if (importer.isImportableKineticLaw()) {
								importer.importKineticLaw();
								imported = true;
								break;
							}
						}
						if(imported==true) {
							matched++;
						}
						else {
							if(kineticLaws.size()>0) {
								matchingNotPossible++;
							}
							else {
								noKineticLawFound++;
							}
						}
						
					}
					else {
						noReactionID++;
					}
				}
		
				/*
				 * SBML-Output
				 */
				String outputFile = file.getAbsolutePath().replace("\\","/").replace(rootFolder, sabioRootFolder);
				SBMLWriter.write(sbmlDocument, new File(outputFile), ' ', (short) 4);
			}
			else {
				if(file.isDirectory()) {
					String outputFile = file.getAbsolutePath().replace("\\","/").replace(rootFolder, sabioRootFolder);
					(new File(outputFile)).mkdir();
				
					File[] filesInDirectory = file.listFiles();
					
					for(File f: filesInDirectory) {
						fileList.add(f);
					}
				}
			}
		}
		System.out.println("Matched: " + matched);
		System.out.println("Law not found " + noKineticLawFound);
		System.out.println("Matching not possible " + matchingNotPossible);
		System.out.println("No KEGG id given " + noReactionID);
		
	}

}
