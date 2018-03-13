/*
 * $Id: ConnectToSABIORK.java 1082 2014-02-22 23:54:18Z draeger $
 * $URL: https://rarepos.cs.uni-tuebingen.de/svn-path/SBMLsqueezer/trunk/src/org/sbml/squeezer/util/ConnectToSABIORK.java $
 * ---------------------------------------------------------------------
 * This file is part of SBMLsqueezer, a Java program that creates rate
 * equations for reactions in SBML files (http://sbml.org).
 *
 * Copyright (C) 2006-2018  by the University of Tuebingen, Germany.
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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
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
 * @version $Rev: 1082 $
 * @since 2.0
 */
public class ConnectToSABIORK {
  
  /**
   * 
   */
  private static URL host;
  
  /**
   * 
   * @param keggId
   * @return
   */
  public List<Reaction> searchKineticLaw(String keggId, String organism) {
    
    try {
      if (host == null) {
        host = new URL("http://sabio.h-its.org/sabioRestWebServices/searchKineticLaws/");
      }
      String q = "sbml?searchTerms=RKEGG=" + keggId +";ORGANISM=" +
          organism;
      
      URL fullAddress = new URL(host, q);
      URLConnection conn = fullAddress.openConnection();
      
      InputStream stream = conn.getInputStream();
      if ((stream == null)) {
        return null;
      }
      SBMLDocument doc = SBMLReader.read(stream);
      List<Reaction> results = new LinkedList<Reaction>();
      
      for (Reaction r: doc.getModel().getListOfReactions()) {
        results.add(r);
      }
      
      return results;
    }
    catch (Exception e) {
      return null;
    }
    
  }
  
  /**
   * 
   * @param r
   * @param kl
   * @param m
   * @return
   * @throws RateLawNotApplicableException
   */
  public KineticLaw chooseLaw(Reaction r, KineticLaw kl, Model m) throws RateLawNotApplicableException {
    CVTermFilter filterIs = new CVTermFilter(Qualifier.BQB_IS);
    CVTermFilter filterHasVersion = new CVTermFilter(Qualifier.BQB_HAS_VERSION);
    CVTermFilter filterIsEncodedBy = new CVTermFilter(Qualifier.BQB_IS_ENCODED_BY);
    
    for (CallableSBase sBase: getAllSBasesToAdd(kl.getMath(), new HashSet<CallableSBase>())) {
      if (sBase instanceof FunctionDefinition) {
        FunctionDefinition copy = ((FunctionDefinition)sBase).clone();
        copy.setLevel(m.getLevel());
        copy.setVersion(m.getVersion());
        boolean add = true;
        if (copy.getMath().getRightChild().isNaN()) {
          return null;
        }
        for (FunctionDefinition f: m.getListOfFunctionDefinitions()) {
          if (f.equals(copy)) {
            add = false;
            break;
          }
        }
        if (add) {
          m.addFunctionDefinition(copy);
        }
      }
      else if (sBase instanceof Parameter) {
        Parameter copy = ((Parameter)sBase).clone();
        copy.setLevel(m.getLevel());
        copy.setVersion(m.getVersion());
        m.addParameter(copy);
      }
    }
    
    for (ASTNode speciesNode: getAllSpeciesNodes(kl.getMath(), new HashSet<ASTNode>())) {
      //substitute ids in kinetic law by corresponding ids in our model
      
      Species s = (Species) speciesNode.getVariable();
      List<CVTerm> terms = new LinkedList<CVTerm>();
      for (CVTerm ct: s.getCVTerms()) {
        if ((filterIs.accepts(ct)) || (filterHasVersion.accepts(ct)) || (filterIsEncodedBy.accepts(ct))) {
          terms.add(ct);
        }
      }
      if (terms.size() != 0) {
        List<CVTermFilter> filterTerms = new LinkedList<CVTermFilter>();
        
        for (CVTerm term: terms) {
          for (String resource: term.getResources()) {
            filterTerms.add(new CVTermFilter(term.getBiologicalQualifierType(), resource));
            if (term.getBiologicalQualifierType().equals(Qualifier.BQB_IS)) {
              filterTerms.add(new CVTermFilter(Qualifier.BQB_HAS_VERSION, resource));
            }
            else if (term.getBiologicalQualifierType().equals(Qualifier.BQB_HAS_VERSION)) {
              filterTerms.add(new CVTermFilter(Qualifier.BQB_IS, resource));
            }
          }
        }
        
        Species foundSpecies = null;
        for (Species sp: m.getListOfSpecies()) {
          for (CVTermFilter currentFilter: filterTerms) {
            if (currentFilter.accepts(sp)) {
              foundSpecies = sp;
              break;
            }
          }
        }
        if (foundSpecies != null) {
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
    if (node.isName()) {
      CallableSBase sb = node.getVariable();
      if (sb instanceof Species) {
        current.add(node);
      }
    }
    else {
      for (ASTNode child: node.getChildren()) {
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
    if (node.isFunction()) {
      CallableSBase sb = node.getVariable();
      if ((sb instanceof FunctionDefinition)) {
        current.add(sb);
      }
      for (ASTNode child: node.getChildren()) {
        getAllSBasesToAdd(child, current);
      }
    }
    else if (node.isName()) {
      CallableSBase sb = node.getVariable();
      if ((sb instanceof Parameter)) {
        current.add(sb);
      }
    }
    else {
      for (ASTNode child: node.getChildren()) {
        getAllSBasesToAdd(child, current);
      }
    }
    return current;
  }
  
  /**
   * 
   * @param args (input file, output file)
   * @throws XMLStreamException
   * @throws RateLawNotApplicableException
   * @throws IOException
   */
  public static void main(String[] args) throws XMLStreamException, RateLawNotApplicableException, IOException {
    ConnectToSABIORK sk = new ConnectToSABIORK();
    SBMLDocument doc = SBMLReader.read(new File(args[0]));
    Model model = doc.getModel();
    CVTermFilter filterKEGG = new CVTermFilter(Qualifier.BQB_IS, "urn:miriam:kegg.reaction");
    String query = null;
    List<Reaction> annotatedReactions = new LinkedList<Reaction>();
    for (Reaction r: model.getListOfReactions()) {
      if (filterKEGG.accepts(r)) {
        annotatedReactions.add(r);
      }
    }
    
    //CVTermFilter filterHuman = new CVTermFilter(Qualifier.BQB_OCCURS_IN, "urn:miriam:taxonomy:9606");
    CVTermFilter filterYeast = new CVTermFilter(Qualifier.BQB_OCCURS_IN, "urn:miriam:taxonomy:559292");
    String organism = "Homo+sapiens";
    if (filterYeast.accepts(model)) {
      organism = "Saccharomyces+cerevisiae";
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
      List<Reaction> result = sk.searchKineticLaw(query,organism);
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

