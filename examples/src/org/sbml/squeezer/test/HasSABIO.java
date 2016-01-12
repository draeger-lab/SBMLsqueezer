/*
 * $Id$
 * $URL$
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
package org.sbml.squeezer.test;

import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.Set;

import javax.xml.stream.XMLStreamException;

import org.sbml.jsbml.Model;
import org.sbml.jsbml.Reaction;
import org.sbml.jsbml.SBMLDocument;
import org.sbml.jsbml.SBMLReader;
import org.sbml.squeezer.sabiork.wizard.SABIORKWizard;

/**
 * Class checks if SABIO-RK has content for a given file or directory with SBML
 * files.
 * 
 * @author Andreas Dr&auml;ger
 * @version $Rev$
 * @since 2.0
 */
public class HasSABIO {
  
  private static StringBuilder output;
  
  /**
   * @param args
   */
  public static void main(String[] args) {
    File f = new File(args[0]); // directory with SBML files or SBML file.
    output = new StringBuilder();
    process(f);
    System.out.println("=====================================================");
    System.out.println(output);
  }
  
  private static void process(File f) {
    if (f.isDirectory()) {
      for (File file : f.listFiles()) {
        process(file);
      }
    } else {
      try {
        hasRateLawsInSABIO(f);
      } catch (XMLStreamException exc) {
        output.append("Error while reading " + f.getAbsolutePath());
        exc.printStackTrace();
      } catch (IOException exc) {
        output.append("Error while reading " + f.getAbsolutePath());
        exc.printStackTrace();
      }
    }
  }
  
  /**
   * @param f
   * @throws XMLStreamException
   * @throws IOException
   */
  private static void hasRateLawsInSABIO(File f) throws XMLStreamException,
  IOException {
    // Read the SBMLDocument
    SBMLDocument doc = SBMLReader.read(f);
    // Query SABIO-RK and return set of reactions for which rate laws can be found
    Set<Reaction> setOfReactionsWithRateLawsFromSABIO = querySABIO(doc);
    // If something was found, display the result
    if (setOfReactionsWithRateLawsFromSABIO.size() > 0) {
      Model model = doc.getModel();
      output.append(f.getName());
      output.append(MessageFormat.format("Found rate laws for {0,number,integer} of {1,number,integer} reactions.\n", setOfReactionsWithRateLawsFromSABIO.size(), model.getReactionCount()));
    }
  }
  
  /**
   * 
   * @param doc
   * @return
   */
  private static Set<Reaction> querySABIO(SBMLDocument doc) {
    String pathway = null;
    String tissue = null;
    String organism = null;
    String cellularLocation = null;
    Boolean isWildtype = true;
    Boolean isMutant = true;
    Boolean isRecombinant = false;
    Boolean hasKineticData = true;
    Double lowerpHValue = 1d;
    Double upperpHValue = 14d;
    Double lowerTemperature = -10d;
    Double upperTemperature = 115d;
    Boolean isDirectSubmission = true;
    Boolean isJournal = true;
    Boolean isEntriesInsertedSince = false;
    String dateSubmitted = "01/01/2000";
    
    boolean overwriteExistingRateLaws = true;
    
    // Run the console mode
    return SABIORKWizard.getResultConsole(doc, overwriteExistingRateLaws, pathway, tissue, organism,
      cellularLocation, isWildtype, isMutant, isRecombinant,
      hasKineticData, lowerpHValue, upperpHValue, lowerTemperature,
      upperTemperature, isDirectSubmission, isJournal,
      isEntriesInsertedSince, dateSubmitted);
  }
  
}
