/*
 * $Id$
 * $URL$
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

import java.util.Date;
import java.util.ResourceBundle;

import org.sbml.squeezer.util.Bundles;

import de.zbit.util.ResourceManager;
import de.zbit.util.prefs.KeyProvider;
import de.zbit.util.prefs.Option;
import de.zbit.util.prefs.OptionGroup;
import de.zbit.util.prefs.Range;

/**
 * Options for SABIO-RK search.
 * @author Roland Keller
 * @version $Rev$
 * @since 2.0
 */
public interface SABIORKPreferences extends KeyProvider {
  
  /**
   * Localization support.
   */
  public static final ResourceBundle OPTIONS_BUNDLE = ResourceManager.getBundle(Bundles.OPTIONS);
  
  /**
   * Helper variable that contains the
   * {@link Range} of possible pH values.
   * @return
   */
  public static final Range<Double> PH_RANGE = new Range<Double>(Double.class, "{[0,14]}");
  
  /**
   * Helper variable that contains the
   * {@link Range} of possible temperature values.
   */
  public static final Range<Double> TEMPERATURE_RANGE = new Range<Double>(Double.class, "{[-271.15,1000]}");
  
  
  /**
   * If {@code true} the application searches for wildtype kinetics.
   */
  public static final Option<Boolean> IS_WILDTYPE = new Option<Boolean>(
      "IS_WILDTYPE",
      Boolean.class,
      OPTIONS_BUNDLE,
      Boolean.valueOf(true));
  
  /**
   * If {@code true} the application searches for kinetics of mutants.
   */
  public static final Option<Boolean> IS_MUTANT = new Option<Boolean>(
      "IS_MUTANT",
      Boolean.class,
      OPTIONS_BUNDLE,
      Boolean.valueOf(true));
  
  /**
   * If {@code true} the application searches for kinetics of recombinant organisms.
   */
  public static final Option<Boolean> IS_RECOMBINANT = new Option<Boolean>(
      "IS_RECOMBINANT",
      Boolean.class,
      OPTIONS_BUNDLE,
      Boolean.valueOf(false));
  
  /**
   * If {@code true} the application only searches for reactions with given kinetic data.
   */
  public static final Option<Boolean> HAS_KINETIC_DATA = new Option<Boolean>(
      "HAS_KINETIC_DATA",
      Boolean.class,
      OPTIONS_BUNDLE,
      Boolean.valueOf(true));
  
  /**
   * If {@code true} the application only searches for direct submissions.
   */
  public static final Option<Boolean> IS_DIRECT_SUBMISSION = new Option<Boolean>(
      "IS_DIRECT_SUBMISSION",
      Boolean.class,
      OPTIONS_BUNDLE,
      Boolean.valueOf(true));
  
  /**
   * If {@code true} the application only searches for entries from journal publications.
   */
  public static final Option<Boolean> IS_JOURNAL = new Option<Boolean>(
      "IS_JOURNAL",
      Boolean.class,
      OPTIONS_BUNDLE,
      Boolean.valueOf(true));
  
  /**
   * The lowest possible pH value for found entries.
   */
  public static final Option<Double> LOWEST_PH_VALUE = new Option<Double>(
      "LOWEST_PH_VALUE",
      Double.class,
      OPTIONS_BUNDLE,
      PH_RANGE,
      Double.valueOf(0d));
  
  /**
   * The highest possible pH value for found entries.
   */
  public static final Option<Double> HIGHEST_PH_VALUE = new Option<Double>(
      "HIGHEST_PH_VALUE",
      Double.class,
      OPTIONS_BUNDLE,
      PH_RANGE,
      Double.valueOf(14d));
  
  /**
   * The lowest possible temperature value for found entries.
   */
  public static final Option<Double> LOWEST_TEMPERATURE_VALUE = new Option<Double>(
      "LOWEST_TEMPERATURE_VALUE",
      Double.class,
      OPTIONS_BUNDLE,
      TEMPERATURE_RANGE,
      Double.valueOf(-10d));
  
  /**
   * The highest possible temperature value for found entries.
   */
  public static final Option<Double> HIGHEST_TEMPERATURE_VALUE = new Option<Double>(
      "HIGHEST_TEMPERATURE_VALUE",
      Double.class,
      OPTIONS_BUNDLE,
      TEMPERATURE_RANGE,
      Double.valueOf(115d));
  
  /**
   * If {@code true} the application only searches for entries inserted after the given date.
   */
  public static final Option<Boolean> IS_ENTRIES_INSERTED_SINCE = new Option<Boolean>(
      "IS_ENTRIES_INSERTED_SINCE",
      Boolean.class,
      OPTIONS_BUNDLE,
      Boolean.valueOf(false));
  
  /**
   * The earliest date for the entries.
   */
  public static final Option<Date> LOWEST_DATE = new Option<Date>(
      "LOWEST_DATE",
      Date.class,
      OPTIONS_BUNDLE,
      SABIORK.getDefaultDate());
  
  /**
   * 
   */
  @SuppressWarnings({ "unchecked" })
  public static final OptionGroup<Boolean> GROUP_GENERAL_PROPERTIES = new OptionGroup<Boolean>(
      "GROUP_GENERAL_PROPERTIES", OPTIONS_BUNDLE, IS_WILDTYPE, IS_MUTANT,
      IS_RECOMBINANT, HAS_KINETIC_DATA, IS_DIRECT_SUBMISSION, IS_JOURNAL,
      IS_ENTRIES_INSERTED_SINCE);
  
  /**
   * 
   */
  @SuppressWarnings({ "unchecked", "rawtypes" })
  public static final OptionGroup<Double> GROUP_TEMPERATURE = new OptionGroup(
    "GROUP_TEMPERATURE", OPTIONS_BUNDLE, LOWEST_TEMPERATURE_VALUE,
    HIGHEST_TEMPERATURE_VALUE);
  
  /**
   * 
   */
  @SuppressWarnings({ "unchecked", "rawtypes" })
  public static final OptionGroup<Double> GROUP_PH_VALUE = new OptionGroup(
    "GROUP_PH_VALUE", OPTIONS_BUNDLE, LOWEST_PH_VALUE, HIGHEST_PH_VALUE);
  
  /**
   * 
   */
  @SuppressWarnings("unchecked")
  public static final OptionGroup<Date> GROUP_DATE = new OptionGroup<Date>(
      "GROUP_DATE", OPTIONS_BUNDLE, LOWEST_DATE);
  
}
