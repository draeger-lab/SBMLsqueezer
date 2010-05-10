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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import org.sbml.jsbml.SBMLException;
import org.sbml.jsbml.SBMLInputConverter;
import org.sbml.jsbml.SBMLOutputConverter;
import org.sbml.jsbml.SBase;
import org.sbml.jsbml.util.IOProgressListener;
import org.sbml.jsbml.xml.libsbml.LibSBMLReader;
import org.sbml.jsbml.xml.libsbml.LibSBMLWriter;
import org.sbml.squeezer.gui.SBMLsqueezerUI;
import org.sbml.squeezer.gui.UpdateMessage;
import org.sbml.squeezer.io.LaTeXExport;
import org.sbml.squeezer.io.SBFileFilter;
import org.sbml.squeezer.io.SBMLio;
import org.sbml.squeezer.kinetics.BasicKineticLaw;
import org.sbml.squeezer.kinetics.InterfaceArbitraryEnzymeKinetics;
import org.sbml.squeezer.kinetics.InterfaceBiBiKinetics;
import org.sbml.squeezer.kinetics.InterfaceBiUniKinetics;
import org.sbml.squeezer.kinetics.InterfaceGeneRegulatoryKinetics;
import org.sbml.squeezer.kinetics.InterfaceIntegerStoichiometry;
import org.sbml.squeezer.kinetics.InterfaceIrreversibleKinetics;
import org.sbml.squeezer.kinetics.InterfaceModulatedKinetics;
import org.sbml.squeezer.kinetics.InterfaceNonEnzymeKinetics;
import org.sbml.squeezer.kinetics.InterfaceReversibleKinetics;
import org.sbml.squeezer.kinetics.InterfaceUniUniKinetics;
import org.sbml.squeezer.kinetics.InterfaceZeroProducts;
import org.sbml.squeezer.kinetics.InterfaceZeroReactants;
import org.sbml.squeezer.math.Distance;
import org.sbml.squeezer.resources.Resource;
import org.sbml.squeezer.util.MessageListener;
import org.sbml.squeezer.util.MessageProcessor;

import eva2.tools.math.des.AbstractDESSolver;

/**
 * The main program of SBMLsqueezer. This class initializes all required
 * objects, starts the GUI if desired and loads all settings from the user.
 * 
 * @author Andreas Dr&auml;ger
 * @author Nadine Hassis
 * @author Hannes Borch
 * @since 1.0
 * @version $Revision: 293$
 */
public class SBMLsqueezer implements LawListener, IOProgressListener {
	/**
	 * 
	 */
	private final static String configFile = "org/sbml/squeezer/resources/cfg/SBMLsqueezer.cfg";
	/**
	 * The possible location of this class in a jar file if used in plug-in
	 * mode.
	 */
	public static final String JAR_LOCATION = "plugin" + File.separatorChar;
	/**
	 * The package where all kinetic equations are located.
	 */
	public static final String KINETICS_PACKAGE = "org.sbml.squeezer.kinetics";
	/**
	 * enzyme mechanism with an arbitrary number of reactants or products
	 */
	private static Set<String> kineticsArbitraryEnzymeMechanism;
	/**
	 * 
	 */
	private static Set<String> kineticsBiBi;
	/**
	 * 
	 */
	private static Set<String> kineticsBiUni;
	/**
	 * 
	 */
	private static Set<String> kineticsGeneRegulatoryNetworks;
	/**
	 * 
	 */
	private static Set<String> kineticsIntStoichiometry;
	/**
	 * 
	 */
	private static Set<String> kineticsIrreversible;
	/**
	 * Kinetics that do allow for inhibitors or activators
	 */
	private static Set<String> kineticsModulated;
	/**
	 * 
	 */
	private static Set<String> kineticsNonEnzyme;
	/**
	 * 
	 */
	private static Set<String> kineticsReversible;
	/**
	 * 
	 */
	private static Set<String> kineticsUniUni;
	/**
	 * 
	 */
	private static Set<String> kineticsZeroProducts;
	/**
	 * 
	 */
	private static Set<String> kineticsZeroReactants;
	/**
	 * The package where all mathematical functions, in particular distance
	 * functions, are located.
	 */
	public static final String MATH_PACKAGE = "org.sbml.squeezer.math";
	/**
	 * 
	 */
	private static MessageListener msg;
	/**
	 * 
	 */
	private static Properties settings;

	/**
	 * The package where all ODE solvers are assumed to be located.
	 */
	public static final String SOLVER_PACKAGE = "eva2.tools.math.des";
	/**
	 * The number of the current SBMLsqueezer version.
	 */
	private static final String versionNumber = "1.4";
	/**
	 * An array of all available implementations of distance functions to judge
	 * the quality of a simulation based on parameter and initial value
	 * settings.
	 */
	private static final Class<Distance> AVAILABLE_DISTANCES[] = Reflect
			.getAllClassesInPackage(MATH_PACKAGE, true, true, Distance.class,
					JAR_LOCATION, true);
	/**
	 * An array of all available ordinary differential equation solvers.
	 */
	private static final Class<AbstractDESSolver> AVAILABLE_SOLVERS[] = Reflect
			.getAllClassesInPackage(SOLVER_PACKAGE, true, true,
					AbstractDESSolver.class, JAR_LOCATION, true);
	/**
	 * Comment to be written into SBMLsqueezer's configuration file.
	 */
	private static final String configurationComment = "SBMLsqueezer "
			+ versionNumber + " configuration. Do not change manually.";
	/**
	 * The location of the user's configuration file.
	 */
	private final static String userConfigFile = System
			.getProperty("user.home")
			+ File.separatorChar
			+ ".SBMLsqueezer"
			+ File.separatorChar
			+ "SBMLsqueezer" + versionNumber + ".cfg";
	/**
	 * Load all available kinetic equations and the user's settings from the
	 * configuration file.
	 */
	static {
		msg = new MessageProcessor();
		msg.setWriteTime(false);
		long time = System.currentTimeMillis();
		msg.log("Loading kinetic equations... ");
		kineticsBiBi = new HashSet<String>();
		kineticsBiUni = new HashSet<String>();
		kineticsGeneRegulatoryNetworks = new HashSet<String>();
		kineticsNonEnzyme = new HashSet<String>();
		kineticsArbitraryEnzymeMechanism = new HashSet<String>();
		kineticsUniUni = new HashSet<String>();
		kineticsReversible = new HashSet<String>();
		kineticsIrreversible = new HashSet<String>();
		kineticsZeroReactants = new HashSet<String>();
		kineticsZeroProducts = new HashSet<String>();
		kineticsModulated = new HashSet<String>();
		kineticsIntStoichiometry = new HashSet<String>();
		Class<BasicKineticLaw> classes[] = Reflect.getAllClassesInPackage(
				KINETICS_PACKAGE, false, true, BasicKineticLaw.class,
				JAR_LOCATION, true);
		for (Class<BasicKineticLaw> c : classes) {
			Set<Class<?>> s = new HashSet<Class<?>>();
			for (Class<?> interf : c.getInterfaces())
				s.add(interf);
			if (s.contains(InterfaceIrreversibleKinetics.class))
				kineticsIrreversible.add(c.getCanonicalName());
			if (s.contains(InterfaceReversibleKinetics.class))
				kineticsReversible.add(c.getCanonicalName());
			if (s.contains(InterfaceUniUniKinetics.class))
				kineticsUniUni.add(c.getCanonicalName());
			if (s.contains(InterfaceBiUniKinetics.class))
				kineticsBiUni.add(c.getCanonicalName());
			if (s.contains(InterfaceBiBiKinetics.class))
				kineticsBiBi.add(c.getCanonicalName());
			if (s.contains(InterfaceArbitraryEnzymeKinetics.class))
				kineticsArbitraryEnzymeMechanism.add(c.getCanonicalName());
			if (s.contains(InterfaceGeneRegulatoryKinetics.class))
				kineticsGeneRegulatoryNetworks.add(c.getCanonicalName());
			if (s.contains(InterfaceNonEnzymeKinetics.class))
				kineticsNonEnzyme.add(c.getCanonicalName());
			if (s.contains(InterfaceZeroReactants.class))
				kineticsZeroReactants.add(c.getCanonicalName());
			if (s.contains(InterfaceZeroProducts.class))
				kineticsZeroProducts.add(c.getCanonicalName());
			if (s.contains(InterfaceModulatedKinetics.class))
				kineticsModulated.add(c.getCanonicalName());
			if (s.contains(InterfaceIntegerStoichiometry.class))
				kineticsIntStoichiometry.add(c.getCanonicalName());
		}
		msg.logln("done in " + (System.currentTimeMillis() - time) + " ms");
		msg.log("loading user settings...");
		settings = initProperties();
		msg.logln(" done.");
	}

	/**
	 * 
	 * @param args
	 * @return
	 */
	private static Properties analyzeCommandLineArguments(String[] args) {
		Properties p = new Properties();
		for (String string : args) {
			while (string.startsWith("-"))
				string = string.substring(1);
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
	 * Creates an instance of the given properties, in which all keys are
	 * literals from the configuration enum and all values are objects such as
	 * Boolean, Integer, Double, Kinetics and so on.
	 * 
	 * @param properties
	 * @return
	 */
	private static Properties correctProperties(Properties properties) {
		Object keys[] = properties.keySet().toArray();
		Properties props = new Properties();
		for (int i = keys.length - 1; i >= 0; i--) {
			CfgKeys k = CfgKeys.valueOf(keys[i].toString());
			String val = properties.get(keys[i]).toString();
			if (val.startsWith("user."))
				props.put(k, System.getProperty(val));
			else if (val.equalsIgnoreCase("true")
					|| val.equalsIgnoreCase("false"))
				props.put(k, Boolean.parseBoolean(val));
			else {
				try {
					props.put(k, Integer.valueOf(val));
				} catch (NumberFormatException e1) {
					try {
						props.put(k, Float.valueOf(val));
					} catch (NumberFormatException e2) {
						try {
							props.put(k, Double.valueOf(val));
						} catch (NumberFormatException e3) {
							if (val.length() == 1)
								props.put(k, Character.valueOf(val.charAt(0)));
							else
								props.put(k, val);
						}
					}
				}
			}
			if (k.toString().startsWith("KINETICS_")) {
				if (!val.startsWith(KINETICS_PACKAGE)) {
					val = KINETICS_PACKAGE + '.' + val;
					props.put(k, val);
				}
				boolean invalid = false;
				switch (k) {
				// check if valid default kinetics are given.
				case KINETICS_BI_BI_TYPE:
					invalid = !kineticsBiBi.contains(val);
					break;
				case KINETICS_BI_UNI_TYPE:
					invalid = !kineticsBiUni.contains(val);
					break;
				case KINETICS_GENE_REGULATION:
					invalid = !kineticsGeneRegulatoryNetworks.contains(val);
					break;
				case KINETICS_NONE_ENZYME_REACTIONS:
					invalid = !kineticsNonEnzyme.contains(val);
					break;
				case KINETICS_OTHER_ENZYME_REACTIONS:
					invalid = !kineticsArbitraryEnzymeMechanism.contains(val);
					break;
				case KINETICS_UNI_UNI_TYPE:
					invalid = !kineticsUniUni.contains(val);
					break;
				}
				boolean allReversible = properties
						.containsKey(CfgKeys.OPT_TREAT_ALL_REACTIONS_REVERSIBLE);
				if (allReversible)
					allReversible &= Boolean.parseBoolean(properties.get(
							CfgKeys.OPT_TREAT_ALL_REACTIONS_REVERSIBLE)
							.toString());
				else if (settings != null) {
					allReversible = settings
							.containsKey(CfgKeys.OPT_TREAT_ALL_REACTIONS_REVERSIBLE);
					if (allReversible)
						allReversible &= Boolean.parseBoolean(settings.get(
								CfgKeys.OPT_TREAT_ALL_REACTIONS_REVERSIBLE)
								.toString());
				}
				if (!kineticsIrreversible.contains(val) && !allReversible)
					invalid = false;
				if (invalid) {
					String defaultKin = getDefaultSettings().get(k).toString();
					if (!defaultKin.startsWith(KINETICS_PACKAGE))
						defaultKin = KINETICS_PACKAGE + '.' + defaultKin;
					props.put(k, defaultKin);
					msg.errf("Invalid %s %s; using default %s.", k.toString(),
							val, defaultKin);
				}
			}
		}
		return props;
	}

	public static final Class<Distance>[] getAvailableDistances() {
		return AVAILABLE_DISTANCES;
	}

	/**
	 * 
	 * @return
	 */
	public static final Class<AbstractDESSolver>[] getAvailableSolvers() {
		return AVAILABLE_SOLVERS;
	}

	/**
	 * Returns the location of the default configuration file.
	 * 
	 * @return the configFile
	 */
	public static String getConfigFile() {
		return configFile;
	}

	/**
	 * Reads the default configuration file and returns a properties hash map
	 * that contains pairs of configuration keys and the entries from the file.
	 * 
	 * @return
	 */
	public static Properties getDefaultSettings() {
		Properties defaults;
		try {
			defaults = Resource.readProperties(configFile);
		} catch (IOException e) {
			defaults = new Properties();
			e.printStackTrace();
		}
		return correctProperties(defaults);
	}

	/**
	 * @return the kineticsArbitraryEnzymeMechanism
	 */
	public static Set<String> getKineticsArbitraryEnzymeMechanism() {
		return kineticsArbitraryEnzymeMechanism;
	}

	/**
	 * @return the kineticsBiBi
	 */
	public static Set<String> getKineticsBiBi() {
		return kineticsBiBi;
	}

	/**
	 * @return the kineticsBiUni
	 */
	public static Set<String> getKineticsBiUni() {
		return kineticsBiUni;
	}

	/**
	 * @return the kineticsGeneRegulatoryNetworks
	 */
	public static Set<String> getKineticsGeneRegulatoryNetworks() {
		return kineticsGeneRegulatoryNetworks;
	}

	/**
	 * @return the kineticsIntStoichiometry
	 */
	public static Set<String> getKineticsIntStoichiometry() {
		return kineticsIntStoichiometry;
	}

	/**
	 * @return the kineticsIrreversible
	 */
	public static Set<String> getKineticsIrreversible() {
		return kineticsIrreversible;
	}

	/**
	 * @return the kineticsModulated
	 */
	public static Set<String> getKineticsModulated() {
		return kineticsModulated;
	}

	/**
	 * @return the kineticsNonEnzyme
	 */
	public static Set<String> getKineticsNonEnzyme() {
		return kineticsNonEnzyme;
	}

	/**
	 * @return the kineticsReversible
	 */
	public static Set<String> getKineticsReversible() {
		return kineticsReversible;
	}

	/**
	 * @return the kineticsUniUni
	 */
	public static Set<String> getKineticsUniUni() {
		return kineticsUniUni;
	}

	/**
	 * @return the kineticsZeroProducts
	 */
	public static Set<String> getKineticsZeroProducts() {
		return kineticsZeroProducts;
	}

	/**
	 * @return the kineticsZeroReactants
	 */
	public static Set<String> getKineticsZeroReactants() {
		return kineticsZeroReactants;
	}

	/**
	 * Returns an array of Strings that can be interpreted as enzymes. In
	 * particular this array will contain those configuration keys as strings
	 * for which in the current configuration the corresponding value is set to
	 * true.
	 * 
	 * @return
	 */
	public static String[] getPossibleEnzymeTypes() {
		Set<String> enzymeTypes = new HashSet<String>();
		String prefix = "POSSIBLE_ENZYME_";
		for (Object key : settings.keySet())
			if (key.toString().startsWith(prefix)) {
				if (((Boolean) settings.get(key)).booleanValue())
					enzymeTypes.add(key.toString().substring(prefix.length()));
			}
		return enzymeTypes.toArray(new String[] {});
	}

	/**
	 * 
	 * @return
	 */
	public static Properties getProperties() {
		return settings;
	}

	/**
	 * @return the userConfigFile
	 */
	public static String getUserConfigFile() {
		return userConfigFile;
	}

	/**
	 * 
	 * @return versionNumber
	 */
	public static final String getVersionNumber() {
		return versionNumber;
	}

	/**
	 * 
	 * @return
	 */
	private static Properties initProperties() {
		Properties properties;
		try {
			StringBuilder path = new StringBuilder();
			path.append(System.getProperty("user.home"));
			path.append(File.separatorChar);
			path.append(".SBMLsqueezer");
			File f = new File(path.toString());
			if (!f.exists())
				f.mkdir();
			f = new File(userConfigFile);
			if (!f.exists())
				f.createNewFile();
			if (f.exists() && f.length() == 0) {
				FileOutputStream os = new FileOutputStream(f);
				Resource.readProperties(configFile).store(os,
						configurationComment);
				os.close();
			}
			properties = correctProperties(Resource
					.readProperties(userConfigFile));
			// avoid senseless exceptions if keys are missing.
			for (CfgKeys key : CfgKeys.values())
				if (!properties.containsKey(key))
					properties.put(key, getDefaultSettings().get(key));
		} catch (Exception e) {
			e.printStackTrace();
			properties = getDefaultSettings();
		}
		return properties;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			System.loadLibrary("sbmlj");
			// Extra check to be sure we have access to libSBML:
			Class.forName("org.sbml.libsbml.libsbml");
		} catch (Exception e) {
			msg.err("Error: could not load the libSBML library");
			e.printStackTrace();
			System.exit(1);
		}
		final SBMLsqueezer squeezer = new SBMLsqueezer(new LibSBMLReader(),
				new LibSBMLWriter());
		msg.log("scanning command line arguments...");
		final Properties p = analyzeCommandLineArguments(args);
		for (Object key : p.keySet())
			SBMLsqueezer.getProperties().put(key, p.get(key));
		msg.logln(" done.\nreading SBO... done.");
		if (p.containsKey(CfgKeys.GUI)
				&& ((Boolean) p.get(CfgKeys.GUI)).booleanValue()) {
			if (p.containsKey(CfgKeys.SBML_FILE))
				squeezer.readSBMLSource(p.get(CfgKeys.SBML_FILE).toString());
			if (p.containsKey(CfgKeys.CHECK_FOR_UPDATES)
					&& ((Boolean) p.get(CfgKeys.CHECK_FOR_UPDATES))
							.booleanValue())
				squeezer.checkForUpdate(true);
			msg.log("loading GUI...");
			new Thread(new Runnable() {
				public void run() {
					msg.logln(" have fun!");
					if (p.containsKey(CfgKeys.SIMULATION_MODE)
							&& ((Boolean) p.get(CfgKeys.SIMULATION_MODE))
									.booleanValue())
						squeezer.showGUISimulation();
					else
						squeezer.showGUI();
				}
			}).start();
		} else {
			if (squeezer.getSBMLIO().getNumErrors() > 0
					&& ((Boolean) p.get(CfgKeys.SHOW_SBML_WARNINGS))
							.booleanValue())
				for (SBMLException exc : squeezer.getSBMLIO().getWarnings())
					msg.err(exc.getMessage());
			// Do a lot of other stuff...
			squeezer.checkForUpdate(false);
			if (p.containsKey(CfgKeys.SBML_OUT_FILE))
				try {
					squeezer.squeeze(p.get(CfgKeys.SBML_FILE).toString(), p
							.get(CfgKeys.SBML_OUT_FILE).toString());
				} catch (Throwable e) {
					e.printStackTrace();
				}
		}
	}

	/**
	 * @throws IOException
	 * @throws FileNotFoundException
	 * 
	 */
	public static void saveProperties(Properties settings)
			throws FileNotFoundException, IOException {
		if (!initProperties().equals(settings)) {
			Properties toSave = new Properties();
			for (Object key : settings.keySet())
				toSave.put(key.toString(), settings.get(key).toString());
			toSave.store(new FileOutputStream(userConfigFile),
					configurationComment);
		}
	}

	/**
	 * Displays a short copyright message on the screen.
	 */
	private static void showAboutMsg() {
		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(
					Resource.getInstance().getStreamFromResourceLocation(
							"org/sbml/squeezer/resources/html/about.htm")));
			String line;
			while ((line = br.readLine()) != null) {
				line = line.trim();
				if (line.startsWith("<"))
					continue;
				line = line.replace("</a>", "");
				line = line.replace("&#246;", "\u00f6");
				line = line.replace("&#169;", "\u00A9");
				line = line.replace("&#228;", "\u00e4");
				line = line.replace("&#252;", "\u00fc");
				if (line.endsWith("<br>"))
					line = line.substring(0, line.length() - 4);
				if (line.contains("<a href"))
					msg.logln(line.substring(0, line.indexOf('<') - 1) + ' ');
				else
					msg.logln(line);
			}
		} catch (Exception e) {
		}
	}

	private SBMLio sbmlIo;

	/**
	 * This constructor allows the integration of SBMLsqueezer into third-party
	 * programs, i.e., as a CellDesigner plug-in.
	 * 
	 * @param sbmlReader
	 * @param sbmlWriter
	 */
	public SBMLsqueezer(SBMLInputConverter sbmlReader,
			SBMLOutputConverter sbmlWriter) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < 60; i++)
			sb.append('-');
		msg.logln(sb.toString());
		showAboutMsg();
		msg.logln(sb.toString());
		sbmlIo = new SBMLio(sbmlReader, sbmlWriter);
		// sbmlIo.addIOProgressListener(this);
	}

	/**
	 * Let's see if there is a later version of SBMLsqueezer available.
	 * 
	 * @param gui
	 *            Decides whether or not the update message should appear in a
	 *            graphical mode (gui = true) or as a text on the console
	 *            otherwise.
	 */
	public void checkForUpdate(final boolean gui) {
		if (((Boolean) settings.get(CfgKeys.CHECK_FOR_UPDATES)).booleanValue()) {
			final SBMLsqueezer squeezer = this;
			new Thread(new Runnable() {
				public void run() {
					try {
						UpdateMessage.checkForUpdate(gui, squeezer);
					} catch (IOException exc) {
						// Don't annoy people
						// JOptionPane.showMessageDialog(squeezer,
						// exc.getMessage());
						// exc.printStackTrace();
					}
				}
			}).start();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.squeezer.LawListener#currentState(org.sbml.jsbml.SBase,
	 * int)
	 */
	public void currentState(SBase item, int num) {
		// nothing to do.
	}

	/**
	 * 
	 * @return
	 */
	public SBMLio getSBMLIO() {
		return sbmlIo;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.squeezer.LawListener#initLawListener(java.lang.String, int)
	 */
	public void initLawListener(String className, int numberOfElements) {
		// nothing to do.
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.jsbml.io.IOProgressListener#progress(java.lang.Object)
	 */
	public void ioProgressOn(Object currObject) {
		if (currObject != null)
			msg.log(currObject.toString());
	}

	/**
	 * 
	 * @param sbmlSource
	 */
	public void readSBMLSource(Object sbmlSource) {
		long time = System.currentTimeMillis();
		msg.log("reading SBML file...");
		try {
			sbmlIo.convert2Model(sbmlSource);
			msg.logf(" done in %d ms.\n", (System.currentTimeMillis() - time));
		} catch (Exception exc) {
			msg.errf(" A problem occured while trying to read the model: %s\n",
					exc.getMessage());
		}
	}

	/**
	 * Change the configuration of SBMLsqueezer.
	 * 
	 * @param key
	 * @param value
	 * @return
	 */
	public Object set(CfgKeys key, boolean value) {
		return settings.put(key, Boolean.valueOf(value));
	}

	/**
	 * Change the configuration of SBMLsqueezer.
	 * 
	 * @param key
	 * @param value
	 * @return
	 */
	public Object set(CfgKeys key, double value) {
		return settings.put(key, Double.valueOf(value));
	}

	/**
	 * Change the configuration of SBMLsqueezer.
	 * 
	 * @param key
	 * @param value
	 * @return
	 */
	public Object set(CfgKeys key, int value) {
		return settings.put(key, Integer.valueOf(value));
	}

	/**
	 * Change the configuration of SBMLsqueezer.
	 * 
	 * @param key
	 * @param value
	 * @return
	 */
	public Object set(CfgKeys key, String value) {
		return settings.put(key, value);
	}

	/**
	 * Shows the GUI of SBMLsqueezer stand-alone.
	 */
	public void showGUI() {
		SBMLsqueezerUI gui = new SBMLsqueezerUI(sbmlIo, settings);
		gui.setLocationRelativeTo(null);
		gui.setVisible(true);
	}

	/**
	 * Shows the simulation gui.
	 */
	public void showGUISimulation() {
		SBMLsqueezerUI gui = new SBMLsqueezerUI(sbmlIo, settings);
		if (settings.get(CfgKeys.CSV_FILE).toString().length() > 0) {
			gui.showSimulationControl(true, settings.get(CfgKeys.CSV_FILE)
					.toString());
		} else {
			gui.showSimulationControl(true);
		}
		gui.dispose();
		try {
			saveProperties(getProperties());
		} catch (FileNotFoundException exc) {
			exc.printStackTrace();
		} catch (IOException exc) {
			exc.printStackTrace();
		}
		System.exit(0);
	}

	/**
	 * Reads in the given SBML file, squeezes kinetic equations in and writes
	 * the reuslt back to the given SBML file. This method only works if
	 * SBMLsqueezer is used as a stand-alone program.
	 * 
	 * @param sbmlSource
	 *            the path to a file that contains SBML code or another object
	 *            that can be read by the current reader used by SBMLsqueezer.
	 * @param outfile
	 *            The absolute path to a file where the result should be stored.
	 *            This must be a file that ends with .xml or .sbml (case
	 *            insensitive).
	 * @throws Throwable
	 */
	public void squeeze(Object sbmlSource, String outfile) throws Throwable {
		File outFile = outfile != null ? new File(outfile) : null;
		readSBMLSource(sbmlSource);
		boolean errorFatal = false;
		SBMLException exception = null;
		for (SBMLException exc : sbmlIo.getWarnings())
			if (exc.isFatal() || exc.isXML() || exc.isError()) {
				errorFatal = true;
				exception = exc;
			}
		if (errorFatal)
			throw new SBMLException(exception);
		else if (!sbmlIo.getListOfModels().isEmpty()) {
			long time = System.currentTimeMillis();
			msg.log("Generating kinetic equations...");
			KineticLawGenerator klg = new KineticLawGenerator(sbmlIo
					.getSelectedModel(), settings);
			if (klg.getFastReactions().size() > 0) {
				msg.err("Model " + sbmlIo.getSelectedModel().getId()
						+ " contains " + klg.getFastReactions().size()
						+ " fast reaction. This feature is currently"
						+ "ignored by SBMLsqueezer.");
			}
			klg.storeKineticLaws(this);
			msg
					.logln(" done in " + (System.currentTimeMillis() - time)
							+ " ms");
			time = System.currentTimeMillis();
			msg.log("Saving changes and writing SBML file... ");
			sbmlIo.saveChanges(this);
			if (outFile != null
					&& SBFileFilter.SBML_FILE_FILTER.accept(outFile)) {
				sbmlIo.writeSelectedModelToSBML(outFile.getAbsolutePath());
				msg.logln(" done in " + (System.currentTimeMillis() - time)
						+ " ms");
				if (((Boolean) settings.get(CfgKeys.SHOW_SBML_WARNINGS))
						.booleanValue())
					for (SBMLException exc : sbmlIo.getWriteWarnings())
						msg.errln(exc.getMessage());
			} else
				msg.errln("Could not write output to SBML.");
		} else
			msg.errln("File contains no model. Nothing to do.");
	}

	/**
	 * Convenient method that writes a LaTeX file from the given SBML source.
	 * 
	 * @param sbmlInfile
	 * @param latexFile
	 * @throws IOException
	 */
	public void toLaTeX(Object sbmlSource, String latexFile) throws IOException {
		readSBMLSource(sbmlSource);
		String dir = settings.get(CfgKeys.LATEX_DIR).toString();
		if (latexFile != null) {
			File out = new File(latexFile);
			if (SBFileFilter.TeX_FILE_FILTER.accept(out)) {
				String path = out.getParent();
				if (!path.equals(dir))
					settings.put(CfgKeys.LATEX_DIR, path);
				if (!out.exists()) {
					long time = System.currentTimeMillis();
					msg.log("writing LaTeX output...");
					LaTeXExport.writeLaTeX(sbmlIo.getSelectedModel(), out,
							settings);
					msg.logf(" done in %d ms\n",
							(System.currentTimeMillis() - time));
				}
			} else
				msg.errf("no valid TeX file: %s\n", latexFile);
		} else
			msg.errln("no TeX file was provided");
	}
}
