/*
 * $Id: IOOptions.java 808 2012-02-03 13:53:59Z snagel $
 * $URL: https://rarepos.cs.uni-tuebingen.de/svn-path/SBMLsqueezer/trunk/src/org/sbml/squeezer/io/IOOptions.java $
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
package org.sbml.squeezer.io;

import java.io.File;
import java.util.ResourceBundle;

import org.sbml.squeezer.util.Bundles;

import de.zbit.util.ResourceManager;
import de.zbit.util.prefs.KeyProvider;
import de.zbit.util.prefs.Option;

/**
 * This is a list of possible io command line options. Each element
 * listed here determines a key for a configuration value.
 * 
 * @author Sebastian Nagel
 * @since 1.4
 * @version $Rev: 808 $
 */

public interface IOOptions extends KeyProvider {
  
  /**
   * for localization support.
   */
  public static final ResourceBundle OPTIONS_BUNDLE = ResourceManager.getBundle(Bundles.OPTIONS);
  
  /**
   * SBML input file.
   */
  public static final Option<File> SBML_IN_FILE = new Option<File>(
      "SBML_IN_FILE", File.class, OPTIONS_BUNDLE,
      new File(""));
  /**
   * Specifies the file where SBMLsqueezer writes its SBML output.
   */
  public static final Option<File> SBML_OUT_FILE = new Option<File>(
      "SBML_OUT_FILE", File.class, OPTIONS_BUNDLE,
      new File(""));
  
  /**
   * If {@code true}, the application will try to load the library libSBML for reading and
   * writing SBML files, otherwise everything will be done with JSBML only, i.e., pure Java.
   */
  public static final Option<Boolean> TRY_LOADING_LIBSBML = new Option<Boolean>(
      "TRY_LOADING_LIBSBML",
      Boolean.class,
      OPTIONS_BUNDLE,
      false);
  
}
