/*
 * $Id: AutomaticSearch.java 958 2012-08-03 13:28:57Z keller$
 * $URL: https://rarepos.cs.uni-tuebingen.de/svn-path/SBMLsqueezer/trunk/src/org/sbml/squeezer/sabiork/AutomaticSearch.java$
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
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import javax.xml.stream.XMLStreamException;

import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.biojava.bio.seq.io.ParseException;
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

/**
 * @author Matthias Rall, Roland Keller
 * @version $Rev$
 * ${tags}
 */
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
	
	/**
	 * usage: model folder, file "names.dmp", output file
	 * @param args
	 * @throws XMLStreamException
	 * @throws IOException
	 * @throws WebServiceConnectException
	 * @throws WebServiceResponseException
	 * @throws ParseException
	 */
	public static void main(String[] args) throws XMLStreamException,
		IOException, WebServiceConnectException, WebServiceResponseException,
		ParseException {
		if(args.length>2) {
			automaticSearch(args[0], args[1], args[2]);
		}
		else {
			automaticSearch(args[0], args[1], null);
		}
	}
	
	/**
	 * 
	 * @param rootFolder
	 * @param taxonomyFile
	 * @param outputResultFile
	 * @throws XMLStreamException
	 * @throws IOException
	 * @throws WebServiceConnectException
	 * @throws WebServiceResponseException
	 * @throws ParseException
	 */
	public static void automaticSearch(String rootFolder, String taxonomyFile, String outputResultFile)
		throws XMLStreamException, IOException, WebServiceConnectException,
		WebServiceResponseException, ParseException {
		LogManager.getLogger(SBMLCoreParser.class).setLevel(Level.OFF);
		LogManager.getLogger(AbstractSBase.class).setLevel(Level.OFF);
		int matched = 0;
		int noReactionID = 0;
		int matchingNotPossible = 0;
		int noKineticLawFound = 0;

		BufferedWriter writer = new BufferedWriter(new FileWriter(outputResultFile));
		
		File rootFile = new File(rootFolder);
		String parent = rootFile.getParent().replace("\\", "/");
		String sabioRootFolder = parent + "/sabio/";
		(new File(sabioRootFolder)).mkdir();
		
		LinkedList<File> directoryList = new LinkedList<File>();
		
		File[] files = rootFile.listFiles();
		
		if ((files.length > 0) && (files[0].isDirectory())) {
			for (File file : files) {
				directoryList.add(file);
			}
		} else {
			directoryList.add(rootFile);
		}
		
		BufferedReader reader = new BufferedReader(new InputStreamReader(
			new FileInputStream(taxonomyFile)));
		
		HashMap<Integer, String> taxonToName = new HashMap<Integer, String>();
		
		String line = reader.readLine();
		line = reader.readLine();
		while (line != null) {
			String[] field = line.split("\t");
			int number = Integer.valueOf(field[0]);
			if ((!taxonToName.containsKey(number))
					|| (field[6].endsWith("scientific name"))) {
				taxonToName.put(number, field[2]);
			}
			line = reader.readLine();
		}
		SBFileFilter sbmlFilter = SBFileFilter.createSBMLFileFilter();
		
		for (File dir : directoryList) {
			String outputFolder = dir.getAbsolutePath().replace("\\", "/")
					.replace(rootFolder, sabioRootFolder);
			(new File(outputFolder)).mkdir();
			int matched_Organism = 0;
			int noReactionID_Organism = 0;
			int matchingNotPossible_Organism = 0;
			int noKineticLawFound_Organism = 0;
			String organism = null;
			for (File file : dir.listFiles()) {
				if (sbmlFilter.accept(file)) {
					/*
					 * SBML-Input
					 */
					SBMLDocument sbmlDocument = SBMLReader.read(file);
					
					CVTermFilter filter = new CVTermFilter(Qualifier.BQB_OCCURS_IN,
						"urn:miriam:taxonomy");
					
					for (CVTerm cv : sbmlDocument.getModel().getAnnotation()
							.getListOfCVTerms()) {
						if (filter.accepts(cv)) {
							for (String resource : cv.getResources()) {
								if (resource.contains("urn:miriam:taxonomy")) {
									String name = resource.replace("urn:miriam:taxonomy:", "");
									int t = Integer.valueOf(name);
									organism = taxonToName.get(t);
								}
							}
						}
					}
					/*
					 * Constraints (Falls du bestimmte zus�tzliche Bedingungen an die
					 * Suche stellen willst)
					 * 
					 * Der Constraint: " AND " + SABIORK.QueryField.HAS_KINETIC_DATA +
					 * ":true"
					 * 
					 * sollte aber immer verwendet werden, da SABIO-RK dann nur
					 * KineticLaws, die eine kinetische Gleichung besitzen, ausw�hlt.
					 */
					StringBuilder constraints = new StringBuilder();
					if (organism != null) {
						constraints.append(" AND " + SABIORK.QueryField.ORGANISM + ":\""
								+ organism + "\"");
					}
					//constraints.append(" AND " + SABIORK.QueryField.TISSUE + ":liver");
					//constraints.append(" AND " + SABIORK.QueryField.PH_VALUE_RANGE + ":[0.0 TO 14.0]");
					constraints.append(" AND " + SABIORK.QueryField.HAS_KINETIC_DATA
							+ ":true");
					
					for (Reaction reaction : sbmlDocument.getModel().getListOfReactions()) {
						String keggReactionID = getKeggReactionID(reaction);
						if (!keggReactionID.isEmpty()) {
							String query = SABIORK.QueryField.KEGG_REACTION_ID + ":"
									+ keggReactionID + constraints;
							List<KineticLaw> kineticLaws = SABIORK.getKineticLaws(query);
							boolean imported = false;
							for (KineticLaw kineticLaw : kineticLaws) {
								KineticLawImporter importer = new KineticLawImporter(
									kineticLaw, reaction);
								if (importer.isImportableKineticLaw()) {
									importer.importKineticLaw();
									imported = true;
									break;
								}
							}
							if (imported == true) {
								matched++;
								matched_Organism++;
							} else {
								if (kineticLaws.size() > 0) {
									matchingNotPossible++;
									matchingNotPossible_Organism++;
								} else {
									noKineticLawFound++;
									noKineticLawFound_Organism++;
								}
							}
							
						} else {
							noReactionID++;
							noReactionID_Organism++;
						}
					}
					/*
					 * SBML-Output
					 */
					String outputFile = file.getAbsolutePath().replace("\\", "/")
							.replace(rootFolder, sabioRootFolder);
					SBMLWriter.write(sbmlDocument, new File(outputFile), ' ', (short) 4);
				}
			}
			if(outputResultFile != null) {
				writer.append("Organism: " + organism);
				writer.newLine();
				writer.append("Matched: " + matched_Organism);
				writer.newLine();
				writer.append("Law not found " + noKineticLawFound_Organism);
				writer.newLine();
				writer.append("Matching not possible "
						+ matchingNotPossible_Organism);
				writer.newLine();
				writer.append("No KEGG id given " + noReactionID_Organism);
				writer.newLine();
			}
			System.out.println("Organism: " + organism);
			System.out.println("Matched: " + matched_Organism);
			System.out.println("Law not found " + noKineticLawFound_Organism);
			System.out.println("Matching not possible "
					+ matchingNotPossible_Organism);
			System.out.println("No KEGG id given " + noReactionID_Organism);
		}
		
		if(outputResultFile != null) {
			writer.append("Matched: " + matched);
			writer.newLine();
			writer.append("Law not found " + noKineticLawFound);
			writer.newLine();
			writer.append("Matching not possible "
					+ matchingNotPossible);
			writer.newLine();
			writer.append("No KEGG id given " + noReactionID);
			writer.newLine();
			writer.close();
		}
		System.out.println("Matched: " + matched);
		System.out.println("Law not found " + noKineticLawFound);
		System.out.println("Matching not possible " + matchingNotPossible);
		System.out.println("No KEGG id given " + noReactionID);
		
	}
	
}
