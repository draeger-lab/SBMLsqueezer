/**
 * 
 */
package org.sbml.squeezer.math;

import java.io.File;

import de.zbit.util.prefs.KeyProvider;
import de.zbit.util.prefs.Option;

/**
 * @author draeger
 * @author Sarah R. M&uuml;ller vom Hagen
 * @date 2010-10-28
 */
public interface StabilityOptions extends KeyProvider {
	
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
			"Explanation...", 
			//Double.valueOf(0d)
			4.0);

}
