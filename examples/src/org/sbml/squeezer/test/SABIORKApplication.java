/*
 * $Id: SABIORKApplication.java 973 2012-08-17 13:40:55Z keller$
 * $URL: https://rarepos.cs.uni-tuebingen.de/svn-path/SBMLsqueezer/trunk/src/org/sbml/squeezer/sabiork/SABIORKApplication.java$
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

import java.awt.BorderLayout;
import java.awt.Dialog.ModalityType;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.UIManager;
import javax.xml.stream.XMLStreamException;

import org.sbml.jsbml.SBMLDocument;
import org.sbml.jsbml.SBMLException;
import org.sbml.jsbml.SBMLReader;
import org.sbml.jsbml.SBMLWriter;
import org.sbml.squeezer.sabiork.wizard.SABIORKWizard;

/**
 * @author Matthias Rall
 * @version $Rev$
 * @since 2.0
 */
public class SABIORKApplication {
  
  /**
   * Runs the wizard
   * 
   * @param input  the input file
   * @param output the output file
   */
  public static void runGUI(final File input, final File output) {
    try {
      UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
    } catch (Exception exc) {
      exc.printStackTrace();
    }
    final JFrame frame = new JFrame("SBMLsqueezer");
    frame.setLayout(new BorderLayout());
    frame.setMinimumSize(new Dimension(300, 300));
    
    JButton buttonWizard = new JButton("Open SABIO-RK Wizard");
    buttonWizard.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        SBMLDocument result = null;
        try {
          result = SABIORKWizard.getResultGUI(frame,
            ModalityType.APPLICATION_MODAL,
            SBMLReader.read(input), true).getSBMLDocument();
        } catch (XMLStreamException e1) {
          e1.printStackTrace();
        } catch (IOException e1) {
          e1.printStackTrace();
        }
        try {
          if (result != null) {
            SBMLWriter.write(result, output, ' ', (short) 2);
          }
        } catch (SBMLException e1) {
          e1.printStackTrace();
        } catch (XMLStreamException e1) {
          e1.printStackTrace();
        } catch (IOException e1) {
          e1.printStackTrace();
        }
      }
    });
    
    frame.add(buttonWizard, BorderLayout.NORTH);
    frame.setVisible(true);
  }
  
  /**
   * Reads a given SBMLDocument and runs the console mode. The reactions
   * are searched by their KEGG term and additional search terms.
   * 
   * @param args
   *        absolute or relative paths to two SBML files (input and output)
   */
  public static void main(String[] args) throws Throwable{
    // The different search terms can be set here (null means that the term
    // is not used).
    String pathway = null;
    String tissue = null;
    String organism = null;
    String cellularLocation = null;
    Boolean isWildtype = true;
    Boolean isMutant = true;
    Boolean isRecombinant = false;
    Boolean hasKineticData = true;
    Double lowerpHValue = 7.9d;
    Double upperpHValue = 14d;
    Double lowerTemperature = -10d;
    Double upperTemperature = 115d;
    Boolean isDirectSubmission = true;
    Boolean isJournal = true;
    Boolean isEntriesInsertedSince = false;
    String dateSubmitted = "15/10/2008";
    
    boolean overwriteExistingRateLaws = true;
    
    // Read the SBMLDocument
    SBMLDocument doc = SBMLReader.read(new File(args[0]));
    
    // Run the console mode
    SABIORKWizard.getResultConsole(doc, overwriteExistingRateLaws, pathway, tissue, organism,
      cellularLocation, isWildtype, isMutant, isRecombinant,
      hasKineticData, lowerpHValue, upperpHValue, lowerTemperature,
      upperTemperature, isDirectSubmission, isJournal,
      isEntriesInsertedSince, dateSubmitted);
    
    // Save the changed document
    SBMLWriter.write(doc, new File(args[1]), ' ', (short) 2);
  }
  
}
