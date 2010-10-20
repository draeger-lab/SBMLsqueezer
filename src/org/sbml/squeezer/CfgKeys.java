/*
 *  SBMLsqueezer creates rate equations for reactions in SBML files
 *  (http://sbml.org).
 *  Copyright (C) 2009 ZBIT, University of Tübingen, Andreas Dräger
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.sbml.squeezer;

import java.io.IOException;
import java.util.Properties;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import com.sun.xml.internal.bind.v2.runtime.property.Property;

/**
 * This is a list of possible command line options and configuration of
 * SBMLsqueezer. Each element listed here determines a key for a configuration
 * value.
 * 
 * @author Andreas Dr&auml;ger
 * @since 1.3
 */
public enum CfgKeys {
	/**
	 * Decide whether or not SBMLsqueezer should search for updates at start-up.
	 */
	CHECK_FOR_UPDATES,
	/**
	 * Key to specify the default directory for Comma Separated Value (CSV)
	 * files.
	 */
	CSV_FILES_OPEN_DIR,
	/**
	 * A comma-separated file to be opened.
	 */
	CSV_FILE,
	/**
	 * The character that is used to quote strings inside of comma separated
	 * value files.
	 */
	CSV_FILES_QUOTE_CHAR,
	/**
	 * The default directory where comma separated value files are storted.
	 */
	CSV_FILES_SAVE_DIR,
	/**
	 * The separator character that is written between the entries of a comma
	 * separated value file. Not that actually any UTF8 character can be used as
	 * a separator, not only commas.
	 */
	CSV_FILES_SEPARATOR_CHAR,
	/**
	 * The minimal value of the initialization range in a parameter estimation
	 * procedure.
	 */
	EST_INIT_MIN_VALUE,
	/**
	 * The maximal value of the initialization range in a parameter estimation
	 * procedure.
	 */
	EST_INIT_MAX_VALUE,
	/**
	 * The minimal value in the absolute allowable range in a parameter
	 * estimation procedure.
	 */
	EST_MIN_VALUE,
	/**
	 * The maximal value in the absolute allowable range in a parameter
	 * estimation procedure.
	 */
	EST_MAX_VALUE,
	/**
	 * Boolean field to decide whether or not by default all local parameters in
	 * a model should be considered the target of an optimization, i.e., value
	 * estimation.
	 */
	EST_ALL_LOCAL_PARAMETERS,
	/**
	 * Boolean field to decide whether or not by default all global parameters
	 * in a model should be considered the target of an optimization, i.e.,
	 * value estimation.
	 */
	EST_ALL_GLOBAL_PARAMETERS,
	/**
	 * Boolean field to decide whether or not by default all compartments in a
	 * model should be considered the target of an optimization, i.e., value
	 * estimation.
	 */
	EST_ALL_COMPARTMENTS,
	/**
	 * Boolean field to decide whether or not by default all species in a model
	 * should be considered the target of an optimization, i.e., value
	 * estimation.
	 */
	EST_ALL_SPECIES,
	/**
	 * Boolean field to decide whether a model calibration should be done using
	 * multiple shoot technique. This should be the default. The other
	 * possibility is the so-called single shoot technique. This means that only
	 * one initial value is taken to integrate the ordinary differential
	 * equation system, whereas the multiple shoot technique restarts the
	 * integration in each time step given the values in this step. The aim is
	 * then to come as close as possible to the start value in the next time
	 * step. In many cases the fitness landscape becomes much more friendly when
	 * using a multiple shoot strategy.
	 */
	EST_MULTI_SHOOT,
	/**
	 * Can be used in combination with = true or = false or just --gui.
	 * Specifies whether or not SBMLsqueezer should display its graphical user
	 * interface.
	 */
	GUI,
	/**
	 * When storing any GUI elements or other pictures into a JPEG graphics file
	 * the value associated to this key is used for the degree of compression.
	 * Expected is a float value between 0 and 1.
	 */
	JPEG_COMPRESSION_FACTOR,
	/**
	 * The class name of the default kinetic law for bi-bi reactions. This can
	 * be any class that implements the
	 * {@link org.sbml.squeezer.kinetics.InterfaceBiBiKinetics}.
	 */
	KINETICS_BI_BI_TYPE,
	/**
	 * The class name of the default kinetic law for bi-uni reactions. This can
	 * be any class that implements the
	 * {@link org.sbml.squeezer.kinetics.InterfaceBiUniKinetics}.
	 */
	KINETICS_BI_UNI_TYPE,
	/**
	 * Determins the key for the standard kinetic law to be applied for
	 * reactions that are identified to belong to gene-regulatory processes,
	 * such as transcription or translation. The value is the class name of any
	 * class that implements the
	 * {@link org.sbml.squeezer.kinetics.InterfaceGeneRegulatoryNetworks}.
	 */
	KINETICS_GENE_REGULATION,
	/**
	 * Determines the key for the standard kinetic law to be applied for
	 * reactions that are catalyzed by non-enzymes or that are not catalyzed at
	 * all. The value may be any rate law that implements
	 * {@link org.sbml.squeezer.kinetics.InterfaceNonEnzymeKinetics}.
	 */
	KINETICS_NONE_ENZYME_REACTIONS,
	/**
	 * Determines the key for the standard kinetic law to be applied for
	 * reactions that are identified to be enzyme-catalyzed (with or without
	 * explicit catalyst) and that do not belong to one of the other standard
	 * enzyme-catalysis schemes. The value can be any rate law that implements
	 * {@link org.sbml.squeezer.kinetics.InterfaceArbitraryEnzymeKinetics}.
	 */
	KINETICS_OTHER_ENZYME_REACTIONS,
	/**
	 * This key defines the default kinetic law to be applied to
	 * enzyme-catalyzed reactions with one reactant and one product. Possible
	 * values are the names of classes that implement
	 * {@link org.sbml.squeezer.kinetics.InterfaceUniUniKinetics}.
	 */
	KINETICS_UNI_UNI_TYPE,
	/**
	 * Standard directory where LaTeX files can be stored.
	 */
	LATEX_DIR,
	/**
	 * The font size for LaTeX documents.
	 */
	LATEX_FONT_SIZE,
	/**
	 * Key that decides whether or not identifiers should be written in
	 * typewriter font when these occur in mathematical equations.
	 */
	LATEX_IDS_IN_TYPEWRITER_FONT,
	/**
	 * Decides whether to set the LaTeX document in landscape or portrait mode.
	 */
	LATEX_LANDSCAPE,
	/**
	 * Decides whether to write the names or the identifiers of NamedSBase
	 * object in equations.
	 */
	LATEX_NAMES_IN_EQUATIONS,
	/**
	 * The paper size for LaTeX documents.
	 */
	LATEX_PAPER_SIZE,
	/**
	 * Decides whether to create a separate title page instead of a simple
	 * heading.
	 */
	LATEX_TITLE_PAGE,
	/**
	 * Standard directory where SBML files can be found.
	 */
	OPEN_DIR,
	/**
	 * If true all parameters are stored globally for the whole model (default)
	 * else parameters are stored locally for the respective kinetic equation
	 * they belong to.
	 */
	OPT_ADD_NEW_PARAMETERS_ALWAYS_GLOBALLY,
	/**
	 * If true, all reactions within the network are considered enzyme
	 * reactions. If false, an explicit enzymatic catalyst must be assigned to a
	 * reaction to obtain this status.
	 */
	OPT_ALL_REACTIONS_ARE_ENZYME_CATALYZED,
	/**
	 * If not specified the value corresponding to this argument will be used to
	 * initialize the size of compartments.
	 */
	OPT_DEFAULT_COMPARTMENT_INITIAL_SIZE,
	/**
	 * If not specified the value corresponding to this argument will be used to
	 * initialize species depending on their hasOnlySubstanceUnits property as
	 * initial amount or initial concentration.
	 */
	OPT_DEFAULT_SPECIES_INITIAL_VALUE,
	/**
	 * The value that is set for newly created parameters.
	 */
	OPT_DEFAULT_VALUE_OF_NEW_PARAMETERS,
	/**
	 * If true a new rate law will be created for each reaction irrespective of
	 * whether there is already a rate law assigned to this reaction or not.
	 */
	OPT_GENERATE_KINETIC_LAW_FOR_EACH_REACTION,
	/**
	 * Allows the user to ignore species that are annotated with the given
	 * compound identifiers when creating rate laws for reactions that involve
	 * these species. For instance, water or single protons can often be ignored
	 * when creating rate equations, hence simplifying the resulting rate
	 * equations. Preselected are the KEGG compound identifiers for water and
	 * protons
	 */
	OPT_IGNORE_THESE_SPECIES_WHEN_CREATING_LAWS,
	/**
	 * The maximal number of reactants so that the reaction is still considered
	 * plausible.
	 */
	OPT_MAX_NUMBER_OF_REACTANTS,
	/**
	 * If true parameters and units that are never referenced by any element of
	 * the model are deleted when creating kinetic equations with SBMLsqueezer.
	 */
	OPT_REMOVE_UNNECESSARY_PARAMETERS_AND_UNITS,
	/**
	 * Decide whether or not to set the boundary condition for genes to true.
	 */
	OPT_SET_BOUNDARY_CONDITION_FOR_GENES,
	/**
	 * Property that decides whether to set all reactions to reversible before
	 * creating new kinetic equations.
	 */
	OPT_TREAT_ALL_REACTIONS_REVERSIBLE,
	/**
	 * If true, warnings will be displayed for too many reactants.
	 */
	OPT_WARNINGS_FOR_TOO_MANY_REACTANTS,
	/**
	 * This field decides whether or not by default a logarithmic scale should
	 * be applied when plotting values in a two-dimensional figure.
	 */
	PLOT_LOG_SCALE,
	/**
	 * With this key it can be specified whether a two-dimensional plot should
	 * display a grid to highlight the position of points and lines.
	 */
	PLOT_SHOW_GRID,
	/**
	 * Determines whether or not a legend should be shown by default when
	 * plotting data into a two-dimensional figure.
	 */
	PLOT_SHOW_LEGEND,
	/**
	 * Decides whether or not plots should display tooltips next to each curve.
	 */
	PLOT_SHOW_TOOLTIPS,
	/**
	 * The default save directory for graphics files as a result of a plot.
	 */
	PLOT_SAVE_DIR,
	/**
	 * Determins whether or not antisense RNA molecules are accepted as enzymes
	 * when catalyzing a reaction.
	 */
	POSSIBLE_ENZYME_ANTISENSE_RNA,
	/**
	 * Determins whether or not enzyme complexes are accepted as enzymes when
	 * catalyzing a reaction.
	 */
	POSSIBLE_ENZYME_COMPLEX,
	/**
	 * Determins whether or not generic proteins are accepted as enzymes when
	 * catalyzing a reaction.
	 */
	POSSIBLE_ENZYME_GENERIC,
	/**
	 * Determins whether or not receptors are accepted as enzymes when
	 * catalyzing a reaction.
	 */
	POSSIBLE_ENZYME_RECEPTOR,
	/**
	 * Determins whether or not RNA molecules are accepted as enzymes when
	 * catalyzing a reaction.
	 */
	POSSIBLE_ENZYME_RNA,
	/**
	 * Determins whether or not simple molecules are accepted as enzymes when
	 * catalyzing a reaction.
	 */
	POSSIBLE_ENZYME_SIMPLE_MOLECULE,
	/**
	 * Determins whether or not trunkated proteins are accepted as enzymes when
	 * catalyzing a reaction.
	 */
	POSSIBLE_ENZYME_TRUNCATED,
	/**
	 * Determins whether or not unknown molecules are accepted as enzymes when
	 * catalyzing a reaction.
	 */
	POSSIBLE_ENZYME_UNKNOWN,
	/**
	 * Standard directory where SBML or Text files can be saved.
	 */
	SAVE_DIR,
	/**
	 * SBML input file.
	 */
	SBML_FILE,
	/**
	 * Specifies the file where SBMLsqueezer writes its SBML output.
	 */
	SBML_OUT_FILE,
	/**
	 * Can be true or false, depending on if the user wants to see SBML
	 * warnings.
	 */
	SHOW_SBML_WARNINGS,
	/**
	 * Boolean argument to specify if SBMLsqueezer should be used as a pure
	 * simulator for a given SBML file. In this case, the simulation control
	 * panel will be displayed when launching SBMLsqeeezer. This is only
	 * possible if the {@link GUI} is set to true and {@link SBML_FILE} is set
	 * to a valid input file.
	 */
	SIMULATION_MODE,
	/**
	 * This specifies the class name of the default distance function that
	 * evaluates the quality of a simulation with respect to given
	 * (experimental) data.
	 */
	SIM_DISTANCE_FUNCTION,
	/**
	 * The default return value of a distance function that can be used if for
	 * some reason a distance cannot be computed.
	 */
	SIM_DISTANCE_DEFAULT_VALUE,
	/**
	 * The root parameter in the distance function: in case of the n-norm this
	 * is at the same time also the exponent. For instance, the Eulidean
	 * distance has a root value of two, whereas the Manhattan norm has a root
	 * of one. In the RSE, the default root is also two, but this value may be
	 * changed.
	 */
	SIM_DISTANCE_ROOT,
	/**
	 * With the associated non-negative double number that has to be greater
	 * than 0 when simulating SBML models, it is possible to perform a
	 * simulation.
	 */
	SIM_END_TIME,
	/**
	 * This is important for the graphical user interface as it defines the
	 * maximal possible value for compartiments within the input mask. Expected
	 * is a positive double value.
	 */
	SIM_MAX_COMPARTMENT_SIZE,
	/**
	 * This is important for the graphical user interface as it defines the
	 * maximal possible value for parameters within the input mask. Expected is
	 * a positive double value.
	 */
	SIM_MAX_PARAMETER_VALUE,
	/**
	 * This is important for the graphical user interface as it defines the
	 * maximal possible value for species within the input mask. Expected is a
	 * positive double value.
	 */
	SIM_MAX_SPECIES_VALUE,
	/**
	 * This key tells the graphical user interface the upper bound for the input
	 * mask of how many time steps per unit time can maximally be performed when
	 * simulating a model.
	 */
	SIM_MAX_STEPS_PER_UNIT_TIME,
	/**
	 * This is important for the graphical user interface as it defines the
	 * upper bound for the input mask for the simulation time.
	 */
	SIM_MAX_TIME,
	/**
	 * This gives the class name of the default solver for ordinary differential
	 * equation systems. The associated class must implement
	 * {@link eva2.tools.math.des.AbstractDESSolver} and must have a constructor
	 * without any parameters.
	 */
	SIM_ODE_SOLVER,
	/**
	 * The double value associated with this key must, in case of SBML equal to
	 * zero. Generally, any start time would be possible. This is why this key
	 * exists. But SBML is defined to start its simulation at the time zero.
	 */
	SIM_START_TIME,
	/**
	 * The greater this value the longer the computation time, but the more
	 * accurate will be the result.
	 */
	SIM_STEP_SIZE,
	/**
	 * The minimal value for JSpinners in the GUI.
	 */
	SPINNER_MIN_VALUE,
	/**
	 * The maximal value for JSpinners in the GUI.
	 */
	SPINNER_MAX_VALUE,
	/**
	 * This is important for the graphical user interface as it defines the step
	 * size between two values in input masks.
	 */
	SPINNER_STEP_SIZE,
	/**
     * 
     */
	STABILITY_VALUE_OF_DELTA,
	/**
	 * 
	 */
	STEUER_MI_OUTPUT,
	/**
	 * 
	 */
	STEUER_MI_STEPSIZE,
	/**
	 * 
	 */
	STEUER_NUMBER_OF_RUNS,
	/**
	 * 
	 */
	STEUER_PC_OUTPUT,
	/**
	 * 
	 */
	STEUER_PC_STEPSIZE,
	/**
	 * 
	 */
	STEUER_VALUE_OF_M,
	/**
	 * 
	 */
	STEUER_VALUE_OF_N,
	/**
	 * Path to a file with a time series of species/compartment/parameter
	 * values.
	 */
	TIME_SERIES_FILE,
	/**
	 * One of the following values: cat, hal or weg (important for
	 * Liebermeister's standard kinetics).
	 */
	TYPE_STANDARD_VERSION,
	/**
	 * How to ensure unit consistency in kinetic equations? One way is to set
	 * each participating species to an initial amount and to set the unit to
	 * mmole. The other way is to set the initial concentration of each species,
	 * set the unit to mmole per l and to multiply the species with the size of
	 * the surrounding compartment whenever it occurs in a kinetic equation.
	 * Hence, this type paramter belongs to two values.
	 */
	TYPE_UNIT_CONSISTENCY;

	/**
	 * Path to the configuration file with default properties.
	 */
	private static String defaultsCfgFile;
	/**
	 * Path to the user's configuration file.
	 */
	private static String userCfgFile;
	/**
	 * The root node for the user's preferences.
	 */
	private static String userRootNode;

	/**
	 * An optional comment for the configuration file.
	 */
	private static String commentCfgFile = null;

	/**
	 * A hastable of key value pairs representing the user's program
	 * configuration.
	 */
	private static Properties properties = null;

	/**
	 * 
	 * @param args
	 * @return
	 */
	public static Properties analyzeCommandLineArguments(String[] args) {
		Properties p = new Properties();
		for (String string : args) {
			while (string.startsWith("-")) {
				string = string.substring(1);
			}
			Object value = Boolean.TRUE;
			if (string.contains("=")) {
				String keyVal[] = string.split("=");
				string = keyVal[0];
				value = keyVal[1];
			}
			string = string.toUpperCase().replace('-', '_');
			p.put(CfgKeys.valueOf(string), value);
		}
		return correctProperties(p);
	}

	/**
	 * 
	 * @param prefs
	 * @return
	 * @throws BackingStoreException
	 */
	public static Properties convert(Preferences prefs)
			throws BackingStoreException {
		Properties p = new Properties();
		for (String key : prefs.keys()) {
			CfgKeys k = valueOf(key);
			p.put(k, prefs.get(key, "null"));
		}
		return correctProperties(p);
	}

	/**
	 * Creates an instance of the given properties, in which all keys are
	 * literals from the configuration enum and all values are objects such as
	 * Boolean, Integer, Double, Kinetics and so on.
	 * 
	 * @param properties
	 * @return
	 */
	public static Properties correctProperties(Properties properties) {
		Object k[] = properties.keySet().toArray();
		Properties props = new Properties();
		for (int i = k.length - 1; i >= 0; i--) {
			String val = properties.get(k[i]).toString();

			if (val.startsWith("user.")) {
				props.put(valueOf(k[i].toString()), System.getProperty(val));
			} else if (val.equalsIgnoreCase("true")
					|| val.equalsIgnoreCase("false")) {
				props.put(valueOf(k[i].toString()), Boolean.parseBoolean(val));
			} else {
				try {
					props.put(valueOf(k[i].toString()), Integer.valueOf(val));
				} catch (NumberFormatException e1) {
					try {
						props.put(valueOf(k[i].toString()), Float.valueOf(val));
					} catch (NumberFormatException e2) {
						try {
							props.put(valueOf(k[i].toString()), Double
									.valueOf(val));
						} catch (NumberFormatException e3) {
							if (val.length() == 1) {
								props.put(valueOf(k[i].toString()), Character
										.valueOf(val.charAt(0)));
							} else {
								props.put(valueOf(k[i].toString()), val);
							}
						}
					}
				}
			}
		}
		return props;
	}

	/**
	 * @return the commentCfgFile
	 */
	public static String getCommentCfgFile() {
		return commentCfgFile;
	}

	/**
	 * Reads the default configuration file and returns a properties hash map
	 * that contains pairs of configuration keys and the entries from the file.
	 * 
	 * @return
	 */
	public static Properties getDefaultProperties() {
		Properties defaults = new Properties();
		try {
			defaults.loadFromXML(defaults.getClass().getResourceAsStream(
					defaultsCfgFile));
		} catch (Exception exc) {
			try {
				defaults.load(defaults.getClass().getResourceAsStream(
						defaultsCfgFile));
				// defaults = Resource.readProperties(defaultsCfgFile);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return correctProperties(defaults);
	}

	/**
	 * 
	 * @return
	 */
	public static String getDefaultsCfgFile() {
		return defaultsCfgFile;
	}

	/**
	 * 
	 * @return
	 */
	public static Properties getProperties() {
		if (properties == null) {
			try {
				initProperties();
			} catch (BackingStoreException e) {
				properties = new Properties();
			}
		}
		return properties;
	}

	/**
	 * @return the userConfigFile
	 */
	public static String getUserConfigFile() {
		return userCfgFile;
	}

	/**
	 * 
	 * @return
	 */
	public static String getUserPrefNode() {
		return userRootNode;
	}

	/**
	 * 
	 * @return
	 * @throws BackingStoreException
	 */
	public static Properties initProperties() throws BackingStoreException {
		Preferences prefs = Preferences.userRoot().node(userRootNode);
		Properties defaults = getDefaultProperties();
		boolean change = false;
		for (Object k : defaults.keySet()) {
			Object v = defaults.get(k);
			if (prefs.get(k.toString(), "null").equals("null")) {
				put(prefs, k, v);
				change = true;
			}
		}
		if (change) {
			prefs.flush();
		}
		Properties props = convert(prefs);
		properties = props;
		return properties;
	}

	/**
	 * 
	 * @param prefs
	 * @param k
	 * @param v
	 */
	private static void put(Preferences prefs, Object k, Object v) {
		if (v instanceof Boolean) {
			prefs.putBoolean(k.toString(), ((Boolean) v).booleanValue());
		} else if (v instanceof Double) {
			prefs.putDouble(k.toString(), ((Double) v).doubleValue());
		} else if (v instanceof Float) {
			prefs.putFloat(k.toString(), ((Float) v).floatValue());
		} else if (v instanceof Integer) {
			prefs.putInt(k.toString(), ((Integer) v).intValue());
		} else if (v instanceof Long) {
			prefs.putLong(k.toString(), ((Long) v).longValue());
		} else {
			prefs.put(k.toString(), v.toString());
		}
	}

	/**
	 * 
	 * @param props
	 * @throws BackingStoreException
	 */
	public static void saveProperties(Properties props)
			throws BackingStoreException {
		if (!CfgKeys.initProperties().equals(props)) {
			Preferences pref = Preferences.userRoot().node(userRootNode);
			for (Object key : props.keySet()) {
				put(pref, key, props.get(key));
			}
			properties = props;
			pref.flush();
		}
	}

	/**
	 * Saves the currently valid {@link Properties}.
	 * 
	 * @see #saveProperties(Properties)
	 * @throws BackingStoreException
	 */
	public static void saveProperties() throws BackingStoreException {
		saveProperties(CfgKeys.properties);
	}

	/**
	 * @param commentCfgFile
	 *            the commentCfgFile to set
	 */
	public static void setCommentCfgFile(String commentCfgFile) {
		CfgKeys.commentCfgFile = commentCfgFile;
	}

	/**
	 * 
	 * @param defaultsCfgFile
	 */
	public static void setDefaultsCfgFile(String defaultsCfgFile) {
		CfgKeys.defaultsCfgFile = defaultsCfgFile;
	}

	/**
	 * 
	 * @param usrPrefNode
	 */
	public static void setUserPrefNode(String usrPrefNode) {
		userRootNode = usrPrefNode;
	}

	/**
	 * Returns the property associated to this enum element.
	 * 
	 * @return
	 */
	public Object getProperty() {
		return properties.get(this);
	}

	/**
	 * 
	 * @param value
	 *            The new {@link Property} value for this key.
	 * @return the previous value belonging to this key or null if there was no
	 *         other value.
	 */
	public Object putProperty(Object value) {
		return properties.put(this, value);
	}

}
