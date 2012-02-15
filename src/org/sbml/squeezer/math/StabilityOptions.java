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
package org.sbml.squeezer.math;

import java.util.ResourceBundle;

import de.zbit.util.ResourceManager;
import de.zbit.util.prefs.KeyProvider;
import de.zbit.util.prefs.Option;

/**
 * @author Andreas Dr&auml;ger
 * @author Sarah R. M&uuml;ller vom Hagen
 * @author Sebastian Nagel
 * @date 2010-10-28
 * @version $Rev$
 */
public interface StabilityOptions extends KeyProvider {
	/**
	 * for localization support.
	 */
	public static final ResourceBundle bundle = ResourceManager.getBundle("org.sbml.squeezer.gui.locales.Options");
	
//	// TODO!!
//	
//	/**
//	 * The path to the associated configuration file, which contains one default
//	 * value for each option defined in this interface.
//	 */
//	public static final String CONFIG_FILE_LOCATION = "";
//
//	/**
//     * 
//     */
//	public static final Option<Double> STABILITY_VALUE_OF_DELTA = new Option<Double>(
//			"STABILITY_VALUE_OF_DELTA",
//			Double.class,
//			"Explanation...",
//			1E-4d);
//	/**
//	 * 
//	 */
//	public static final Option<File> STEUER_MI_OUTPUT = new Option<File>(
//			"STEUER_MI_OUTPUT",
//			File.class,
//			"Explanation...",
//			new File("user.home"));
//	/**
//	 * 
//	 */
//	public static final Option<Double> STEUER_MI_STEPSIZE = new Option<Double>(
//			"STEUER_MI_STEPSIZE",
//			Double.class,
//			"Explanation",
//			10d);
//	/**
//	 * 
//	 */
//	public static final Option<Double> STEUER_NUMBER_OF_RUNS = new Option<Double>(
//			"STEUER_NUMBER_OF_RUNS",
//			Double.class,
//			"Explanation...",
//			1E4d);
//	/**
//	 * 
//	 */
//	public static final Option<File> STEUER_PC_OUTPUT = new Option<File>(
//			"STEUER_PC_OUTPUT",
//			File.class,
//			"Explanation...",
//			new File("user.home"));
//	/**
//	 * 
//	 */
//	public static final Option<Double> STEUER_PC_STEPSIZE = new Option<Double>(
//			"STEUER_PC_STEPSIZE",
//			Double.class,
//			"Explanation...",
//			10d);
//	/**
//	 * 
//	 */
//	public static final Option<Double> STEUER_VALUE_OF_M = new Option<Double>(
//			"STEUER_VALUE_OF_M",
//			Double.class,
//			"Explanation...",
//			4.0);
	/**
	 * 
	 */
	public static final Option<Double> STEUER_VALUE_OF_N = new Option<Double>(
			"STEUER_VALUE_OF_N", 
			Double.class, 
			bundle, 
			//Double.valueOf(0d)
			4.0);

}
