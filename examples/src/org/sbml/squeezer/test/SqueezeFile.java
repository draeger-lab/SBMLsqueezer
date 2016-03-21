/*
 * $Id$
 * $URL$
 * ---------------------------------------------------------------------
 * This file is part of SBMLsqueezer, a Java program that creates rate
 * equations for reactions in SBML files (http://sbml.org).
 *
 * Copyright (C) 2006-2016 by the University of Tuebingen, Germany.
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

import org.sbml.jsbml.Model;
import org.sbml.squeezer.OptionsGeneral;
import org.sbml.squeezer.SBMLsqueezer;
import org.sbml.squeezer.kinetics.ConvenienceKinetics;
import org.sbml.squeezer.kinetics.MichaelisMenten;
import org.sbml.squeezer.kinetics.OptionsRateLaws;

import de.zbit.util.prefs.SBPreferences;

/**
 * This is a simple example that displays how to use {@link SBMLsqueezer} as an
 * rate equation generating core in a more complex application.
 * 
 * @author Andreas Dr&auml;ger
 * @version $Rev$
 * @since 2.0
 */
public class SqueezeFile {
  
  /**
   * This program takes the absolute paths to an input file and an output
   * file as arguments, configures several preferences for
   * {@link SBMLsqueezer}, generates kinetic equations, units, parameters
   * etc. and saves the result in the given output file in SBML format.
   * 
   * @param args absolute or relative paths to two SBML files (in and out)
   * @throws Throwable if either user preferences cannot be made
   *        persistent or the creation of rate laws fails.
   */
  public static void main(String[] args) throws Throwable {
    /*
     * Configure user preferences of SBMLsqueezer
     */
    // General preferences
    SBPreferences prefs = SBPreferences.getPreferencesFor(OptionsGeneral.class);
    prefs.put(OptionsGeneral.ALL_REACTIONS_AS_ENZYME_CATALYZED, true);
    prefs.put(OptionsGeneral.DEFAULT_COMPARTMENT_SIZE, 1d);
    prefs.put(OptionsGeneral.POSSIBLE_ENZYME_RNA, true);
    prefs.flush();
    
    // Rate law selection
    prefs = SBPreferences.getPreferencesFor(OptionsRateLaws.class);
    prefs.put(OptionsRateLaws.KINETICS_REVERSIBLE_UNI_UNI_TYPE, MichaelisMenten.class);
    prefs.put(OptionsRateLaws.KINETICS_REVERSIBLE_ARBITRARY_ENZYME_REACTIONS, ConvenienceKinetics.class);
    prefs.flush();
    
    // Initialize SBMLsqueezer with JSBML as its internal SBML library
    SBMLsqueezer<Model> squeezer = new SBMLsqueezer<Model>();
    // Create kinetic equations, parameters, units etc. and save the result
    squeezer.squeeze(args[0], args[1]);
  }
  
}
