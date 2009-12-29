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
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import org.sbml.jsbml.SBMLException;
import org.sbml.jsbml.SBMLReader;
import org.sbml.jsbml.SBMLWriter;
import org.sbml.jsbml.SBase;
import org.sbml.jsbml.io.IOProgressListener;
import org.sbml.jsbml.xml.libsbml.LibSBMLReader;
import org.sbml.jsbml.xml.libsbml.LibSBMLWriter;
import org.sbml.squeezer.gui.SBMLsqueezerUI;
import org.sbml.squeezer.gui.UpdateMessage;
import org.sbml.squeezer.io.LaTeXExport;
import org.sbml.squeezer.io.MessageListener;
import org.sbml.squeezer.io.MessageProcessor;
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
import org.sbml.squeezer.resources.Resource;

/**
 * The main program of SBMLsqueezer. This class initializes all requrired
 * objects, starts the GUI if desired and loads all settings from the user.
 * 
 * @author Andreas Dr&auml;ger <a
 *         href="mailto:andreas.draeger@uni-tuebingen.de">
 *         andreas.draeger@uni-tuebingen.de</a>
 * @author <a href="mailto:Nadine.hassis@gmail.com">Nadine Hassis</a>
 * @author <a href="mailto:hannes.borch@googlemail.com">Hannes Borch</a>
 * @since 1.3
 * @version $Revision: 293$
 */
public class SBMLsqueezer implements LawListener, IOProgressListener {

	/**
	 * 
	 */
	private final static String configFile = "org/sbml/squeezer/resources/cfg/SBMLsqueezer.cfg";

	/**
	 * The package where all kinetic equations are located.
	 */
	public static final String KINETICS_PACKAGE = "org.sbml.squeezer.kinetics";

	/**
	 * enzyme mechanism with an arbitrary number of reactants or products
	 */
	private static Set<String> kineticsArbitraryEnzymeMechanism;

	private static Set<String> kineticsBiBi;

	private static Set<String> kineticsBiUni;

	private static Set<String> kineticsGeneRegulatoryNetworks;

	private static Set<String> kineticsIntStoichiometry;

	private static Set<String> kineticsIrreversible;

	/**
	 * Kinetics that do allow for inhibitors or activators
	 */
	private static Set<String> kineticsModulated;

	private static Set<String> kineticsNonEnzyme;

	private static Set<String> kineticsReversible;

	private static Set<String> kineticsUniUni;

	private static Set<String> kineticsZeroProducts;

	private static Set<String> kineticsZeroReactants;

	private static MessageListener msg;

	private static Properties settings;

	/**
	 * The location of the user's configuration file.
	 */
	private final static String userConfigFile = System
			.getProperty("user.home")
			+ File.separatorChar
			+ ".SBMLsqueezer"
			+ File.separatorChar
			+ "SBMLsqueezer.cfg";

	/**
	 * The number of the current SBMLsqueezer version.
	 */
	private static final String versionNumber = "1.3";

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
		Class<?> classes[] = Reflect.getAllClassesInPackage(KINETICS_PACKAGE,
				false, true, BasicKineticLaw.class);
		if (classes == null || classes.length == 0) {
			HashSet<Class<?>> set = new HashSet<Class<?>>();
			String jarPath = "plugin" + File.separatorChar;
			boolean tryDir = true;
			if (tryDir) {
				File f = new File(jarPath);
				if (f.isDirectory()) {
					String[] pathElements = f.list();
					for (String entry : pathElements)
						Reflect.getClassesFromJarFltr(set, jarPath + entry,
								KINETICS_PACKAGE, true, BasicKineticLaw.class);
				}
			}
			classes = Reflect.hashSetToClassArray(set, true);
		}
		for (Class<?> c : classes) {
			if (!Modifier.isAbstract(c.getModifiers())) {
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
			if (string.contains("=")) {
				String keyVal[] = string.split("=");
				p.put(CfgKeys
						.valueOf(keyVal[0].toUpperCase().replace('-', '_')),
						keyVal[1]);
			} else
				p.put(CfgKeys.valueOf(string.toUpperCase().replace('-', '_')),
						Boolean.TRUE.toString());
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
				boolean allDigit = true;
				short dotCount = 0;
				for (char c : val.toCharArray()) {
					if (c == '.')
						dotCount++;
					else
						allDigit &= Character.isDigit(c);
				}
				if (allDigit && dotCount == 0)
					props.put(k, Integer.parseInt(val));
				else if (allDigit && dotCount == 1)
					props.put(k, Double.parseDouble(val));
				else
					props.put(k, val);
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
			StringBuilder path = new StringBuilder(System
					.getProperty("user.home"));
			path.append(File.separatorChar);
			path.append(".SBMLsqueezer");
			File f = new File(path.toString());
			if (!f.exists()) {
				f.mkdir();
				path.append(File.separatorChar);
				path.append("SBMLsqueezer.cfg");
				f = new File(path.toString());
				if (!f.exists())
					f.createNewFile();
			}
			f = new File(userConfigFile);
			if (f.exists() && f.length() == 0) {
				FileOutputStream os = new FileOutputStream(f);
				Resource.readProperties(configFile).store(os,
						"SBMLsqueezer configuration. Do not change manually.");
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
		Properties p = analyzeCommandLineArguments(args);
		for (Object key : p.keySet())
			SBMLsqueezer.getProperties().put(key, p.get(key));
		msg.logln(" done.\nreading SBO... done.");
		if (p.containsKey(CfgKeys.GUI)
				&& Boolean.parseBoolean(p.get(CfgKeys.GUI).toString())) {
			if (p.containsKey(CfgKeys.SBML_FILE))
				squeezer.readSBMLSource(p.get(CfgKeys.SBML_FILE).toString());
			squeezer.checkForUpdate(true);
			msg.log("loading GUI...");
			new Thread(new Runnable() {
				public void run() {
					squeezer.showGUI();
				}
			}).start();
		} else {
			if (squeezer.getSBMLIO().getNumErrors() > 0
					&& ((Boolean) settings.get(CfgKeys.SHOW_SBML_WARNINGS))
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
	 * 
	 */
	public static void saveProperties(Properties settings) {
		if (!initProperties().equals(settings))
			try {
				RandomAccessFile file = new RandomAccessFile(userConfigFile,
						"rw");
				file.setLength(0);
				file.close();
				BufferedWriter bw = new BufferedWriter(new FileWriter(
						userConfigFile));
				for (Object key : settings.keySet()) {
					bw.append(key.toString());
					bw.append('=');
					String value = settings.get(key).toString();
					char c;
					for (int i = 0; i < value.length(); i++) {
						c = value.charAt(i);
						if (c == '\\')
							bw.append('\\');
						bw.append(c);
					}
					bw.newLine();
				}
				bw.close();
			} catch (IOException e) {
				e.printStackTrace();
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
	public SBMLsqueezer(SBMLReader sbmlReader, SBMLWriter sbmlWriter) {
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
		// TODO Auto-generated method stub
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
		// TODO Auto-generated method stub
	}

	/**
	 * 
	 * @param sbmlSource
	 */
	public void readSBMLSource(Object sbmlSource) {
		long time = System.currentTimeMillis();
		msg.log("reading SBML file...");
		try {
			sbmlIo.readModel(sbmlSource);
			msg.logln(" done in " + (System.currentTimeMillis() - time)
					+ " ms.");
		} catch (Exception exc) {
			msg.errln(" a problem occured while trying to read the model:"
					+ exc.getMessage());
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
		msg.logln(" have fun!");
		SBMLsqueezerUI gui = new SBMLsqueezerUI(sbmlIo, settings);
		gui.setLocationRelativeTo(null);
		gui.setVisible(true);
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.jsbml.io.IOProgressListener#progress(java.lang.Object)
	 */
	public void ioProgressOn(Object currObject) {
		msg.log(currObject.toString());
	}
}
