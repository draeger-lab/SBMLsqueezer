/*
 * $Id$
 * $URL$
 * ---------------------------------------------------------------------
 * This file is part of SBMLsqueezer, a Java program that creates rate 
 * equations for reactions in SBML files (http://sbml.org).
 *
 * Copyright (C) 2006-2011 by the University of Tuebingen, Germany.
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
package org.sbml.squeezer.util;



import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.xml.stream.XMLStreamException;

import org.sbml.jsbml.ASTNode;
import org.sbml.jsbml.CVTerm;
import org.sbml.jsbml.CVTerm.Qualifier;
import org.sbml.jsbml.CallableSBase;
import org.sbml.jsbml.FunctionDefinition;
import org.sbml.jsbml.KineticLaw;
import org.sbml.jsbml.Model;
import org.sbml.jsbml.Parameter;
import org.sbml.jsbml.Reaction;
import org.sbml.jsbml.SBMLDocument;
import org.sbml.jsbml.SBMLReader;
import org.sbml.jsbml.SBMLWriter;
import org.sbml.jsbml.Species;
import org.sbml.jsbml.util.filters.CVTermFilter;
import org.sbml.squeezer.RateLawNotApplicableException;

/**
 * @author Roland Keller
 * @version $Rev$
 */

public class ConnectToSABIORK {
	private static URL host;
	
	public List<Reaction> searchKineticLaw(String keggId) {
	
		try {
			if(host == null) {
				host = new URL("http://sabio.h-its.org/sabioRestWebServices/");
			}
			URL fullAddress = new URL(host, "searchKineticLaws/sbml?searchTerms=RKEGG=" + keggId);
			URLConnection conn = fullAddress.openConnection();
			
			
			SBMLDocument doc = (new SBMLReader().readSBMLFromStream(conn.getInputStream()));
			List<Reaction> results = new LinkedList<Reaction>();
			
			for(Reaction r: doc.getModel().getListOfReactions()) {
				results.add(r);
			}
			
			return results;
		}
		catch (Exception e) { 
			e.printStackTrace();
			return null;
		} 
		
	}
	
	
	public KineticLaw chooseLaw(Reaction r, KineticLaw kl, Model m) throws RateLawNotApplicableException {
		CVTermFilter filter = new CVTermFilter(Qualifier.BQB_IS);
		
		for(CallableSBase sBase: getAllSBasesToAdd(kl.getMath(), new HashSet<CallableSBase>())) {
			if(sBase instanceof FunctionDefinition) {
				FunctionDefinition copy = ((FunctionDefinition)sBase).clone();
				copy.setLevel(m.getLevel());
				copy.setVersion(m.getVersion());
				boolean add = true;
				if(copy.getMath().getRightChild().isNaN()) {
					return null;
				}
				for(FunctionDefinition f: m.getListOfFunctionDefinitions()) {
					if(f.equals(copy)) {
						add = false;
						break;
					}
				}
				if(add) {
					m.addFunctionDefinition(copy);
				}
			}
			else if(sBase instanceof Parameter) {
				Parameter copy = ((Parameter)sBase).clone();
				copy.setLevel(m.getLevel());
				copy.setVersion(m.getVersion());
				m.addParameter(copy);
			}
		}
		
		for(ASTNode speciesNode: getAllSpeciesNodes(kl.getMath(), new HashSet<ASTNode>())) {
			//substitute ids in kinetic law by corresponding ids in our model
			
			Species s = (Species) speciesNode.getVariable();
			List<CVTerm> terms = new LinkedList<CVTerm>();
			for(CVTerm ct: s.getCVTerms()) {
				if(filter.accepts(ct)) {
					terms.add(ct);
				}
			}
			if(terms.size() != 0) {
				List<CVTermFilter> filterTerms = new LinkedList<CVTermFilter>();
				
				for(CVTerm term: terms) {
					for(String resource: term.getResources()) {
							filterTerms.add(new CVTermFilter(term.getBiologicalQualifierType(), resource));
					}
				}
			
				Species foundSpecies = null;
				for(Species sp: m.getListOfSpecies()) {
					for(CVTermFilter currentFilter: filterTerms) {
						if(currentFilter.accepts(sp)) {
							foundSpecies = sp;
							break;
						}
					}
				}
				if(foundSpecies != null) {
					speciesNode.setName(foundSpecies.getName());
					speciesNode.setVariable(foundSpecies);
				}
				else {
					return null;
				}
			}
			else {
				return null;
			}
		}
		KineticLaw lawCopy = kl.clone();
		lawCopy.setLevel(m.getLevel());
		lawCopy.setVersion(m.getVersion());
		lawCopy.setMetaId(r.getId() + "(KL)");
		r.setKineticLaw(lawCopy);
		return lawCopy;
	}
	
	/**
	 * 
	 * @param node
	 * @param current
	 * @return
	 */
	public Set<ASTNode> getAllSpeciesNodes(ASTNode node, Set<ASTNode> current) {
		if(node.isName()) {
			CallableSBase sb = node.getVariable();
			if(sb instanceof Species) {
				current.add(node);
			}
		}
		else {
			for(ASTNode child: node.getChildren()) {
				getAllSpeciesNodes(child, current);
			}
		}
		return current;
	}
	
	/**
	 * @param kl
	 * @return
	 */
	public Set<CallableSBase> getAllSBasesToAdd(ASTNode node, Set<CallableSBase> current) {
		if(node.isFunction()) {
			CallableSBase sb = node.getVariable();
			if((sb instanceof FunctionDefinition)) {
				current.add(sb);
			}
			for(ASTNode child: node.getChildren()) {
				getAllSBasesToAdd(child, current);
			}
		}
		else if(node.isName()) {
			CallableSBase sb = node.getVariable();
			if((sb instanceof Parameter)) {
				current.add(sb);
			}
		}
		else {
			for(ASTNode child: node.getChildren()) {
				getAllSBasesToAdd(child, current);
			}
		}
		return current;
	}

	public static void main(String[] args) throws XMLStreamException, RateLawNotApplicableException, IOException {
		ConnectToSABIORK sk = new ConnectToSABIORK();
		SBMLDocument doc = (new SBMLReader()).readSBMLFromFile((args[0]));
		Model model = doc.getModel();
		CVTermFilter filterKEGG = new CVTermFilter(Qualifier.BQB_IS, "urn:miriam:kegg.reaction");
		String query = null;
		List<Reaction> annotatedReactions = new LinkedList<Reaction>();
		for(Reaction r: model.getListOfReactions()) {
			if(filterKEGG.accepts(r)) {
				annotatedReactions.add(r);
			}
		}
		int reactionsWithLaw = 0;
		for (Reaction r : annotatedReactions) {
			for (CVTerm term : r.getCVTerms()) {
				if (filterKEGG.accepts(term)) {
					String urn = term.filterResources("urn:miriam:kegg.reaction").get(0);
					query = urn.replaceFirst("urn:miriam:kegg.reaction:R", "R");
					break;
				}
			}
			List<Reaction> result = sk.searchKineticLaw(query);
			if (result != null) {
				KineticLaw newLaw = null;
				int i = 0;
				while ((newLaw == null) && (i < result.size())) {
					newLaw = sk.chooseLaw(r, result.get(i).getKineticLaw(), model);
					i++;
				}
				if (newLaw != null) {
					reactionsWithLaw++;
				} else {
					System.out.println("A " + r.getId());
				}
			} else {
				System.out.println("B " + r.getId());
			}
		}
		System.out.println("Reactions: " + model.getReactionCount());
		System.out.println("Reactions with kinetic law " + reactionsWithLaw);
		(new SBMLWriter()).write(doc, args[1]);
	}
}

