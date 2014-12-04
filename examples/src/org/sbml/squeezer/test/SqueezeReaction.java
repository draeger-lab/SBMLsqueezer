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

import org.sbml.jsbml.Model;
import org.sbml.jsbml.Reaction;
import org.sbml.jsbml.SBMLDocument;
import org.sbml.jsbml.SBMLReader;
import org.sbml.jsbml.SBMLWriter;
import org.sbml.squeezer.KineticLawGenerator;
import org.sbml.squeezer.UnitConsistencyType;
import org.sbml.squeezer.kinetics.ConvenienceKinetics;
import org.sbml.squeezer.kinetics.TypeStandardVersion;

/**
 * @author Andreas Dr&auml;ger
 * @version $Rev$
 * @since 2.0
 */
public class SqueezeReaction {
  
  /**
   * Reads a given SBML file, creates a convenience rate law for the first
   * reaction within that file, and stores the result in the second file.
   * 
   * @param args absolute or relative paths to two SBML files (in and out)
   * @throws Throwable
   */
  public static void main(String args[]) throws Throwable {
    // Select an SBML reaction object from your SBML document
    SBMLDocument doc = SBMLReader.read(new File(args[0]));
    Model model = doc.getModel();
    Reaction reaction = model.getReaction(0);
    
    // Create and run the kinetic law generator
    KineticLawGenerator klg = new KineticLawGenerator(model);
    // This operates on a copy of the model that only contains one reaction
    klg.createKineticLaw(
      reaction,                   // the selected reaction
      ConvenienceKinetics.class,  // the type of rate law to be created
      false,                      // whether to set the reaction reversible
      TypeStandardVersion.cat,    // type parameter for modular rate laws
      UnitConsistencyType.amount, // how to ensure unit consistency
      1d);                        // the value for new parameters
    
    // Transfer the rate law from the model copy to the original model
    klg.storeKineticLaws();
    
    // Save the result in another file
    SBMLWriter.write(doc, new File(args[1]), ' ', (short) 2);
  }
  
}
