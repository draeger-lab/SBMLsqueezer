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
package org.sbml.squeezer.sabiork;

import java.util.ResourceBundle;

import org.sbml.squeezer.util.Bundles;

import de.zbit.util.ResourceManager;
import de.zbit.util.prefs.KeyProvider;
import de.zbit.util.prefs.Option;
import de.zbit.util.prefs.OptionGroup;

/**
 * @author Roland Keller
 * @version $Rev$
 * @since 2.0
 */
public interface SABIORKOptions extends KeyProvider {
  
  /**
   * Localization support.
   */
  public static final ResourceBundle OPTIONS_BUNDLE = ResourceManager.getBundle(Bundles.OPTIONS);
  
  /**
   * The pathway for which the kinetics have been determined.
   */
  public static final Option<String> PATHWAY = new Option<String>("PATHWAY", String.class, OPTIONS_BUNDLE, (String) null);
  
  /**
   * The tissue for which the kinetics have been determined.
   */
  public static final Option<String> TISSUE = new Option<String>("TISSUE", String.class, OPTIONS_BUNDLE, (String) null);
  
  /**
   * The cellular location for which the kinetics have been determined.
   */
  public static final Option<String> CELLULAR_LOCATION = new Option<String>("CELLULAR_LOCATION", String.class, OPTIONS_BUNDLE, (String) null);
  
  /**
   * The organism for which the kinetics have been determined.
   */
  public static final Option<String> ORGANISM = new Option<String>("ORGANISM", String.class, OPTIONS_BUNDLE, (String) null);
  
  /**
   * 
   */
  @SuppressWarnings({ "unchecked" })
  public static final OptionGroup<String> GROUP_GENERAL_OPTIONS = new OptionGroup<String>(
      "GROUP_GENERAL_OPTIONS", OPTIONS_BUNDLE, PATHWAY, TISSUE, CELLULAR_LOCATION, ORGANISM);
  
}
