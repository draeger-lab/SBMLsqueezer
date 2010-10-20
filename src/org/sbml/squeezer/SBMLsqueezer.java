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
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import java.util.prefs.BackingStoreException;

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
import org.sbml.squeezer.util.MessageListener;
import org.sbml.squeezer.util.MessageProcessor;

import de.zbit.io.SBFileFilter;
import de.zbit.util.Reflect;

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
	 * The possible location of this class in a jar file if used in plug-in
	 * mode.
	 */
	public static final String JAR_LOCATION = "plugin" + File.separatorChar;
	/**
	 * The package where all kinetic equations are located.
	 */
	public static final String KINETICS_PACKAGE = "org.sbml.squeezer.kinetics";
	/**
	 * {@link Set}s of kinetics with certain characteristics.
	 */
	private static Set<String> kineticsArbitraryEnzymeMechanism, kineticsBiBi,
			kineticsBiUni, kineticsGeneRegulatoryNetworks,
			kineticsIntStoichiometry, kineticsIrreversible, kineticsModulated,
			kineticsNonEnzyme, kineticsReversible, kineticsUniUni,
			kineticsZeroProducts, kineticsZeroReactants;
	/**
	 * 
	 */
	private static MessageListener msg;
	/**
	 * The number of the current SBMLsqueezer version.
	 */
	private static final String versionNumber = "1.4";
	/**
	 * The directory within the user's preferences
	 */
	private static final String userPrefDir = "/org/sbml/squeezer";
	/**
	 * 
	 */
	private static Properties properties;

	/**
	 * Comment to be written into SBMLsqueezer's configuration file.
	 */
	private static final String configurationComment = "SBMLsqueezer "
			+ versionNumber + " configuration. Do not change manually.";

	/**
	 * 
	 */
	private final static String configFile = "/org/sbml/squeezer/resources/cfg/SBMLsqueezer.cfg";

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
			for (Class<?> interf : c.getInterfaces()) {
				s.add(interf);
			}
			if (s.contains(InterfaceIrreversibleKinetics.class)) {
				kineticsIrreversible.add(c.getCanonicalName());
			}
			if (s.contains(InterfaceReversibleKinetics.class)) {
				kineticsReversible.add(c.getCanonicalName());
			}
			if (s.contains(InterfaceUniUniKinetics.class)) {
				kineticsUniUni.add(c.getCanonicalName());
			}
			if (s.contains(InterfaceBiUniKinetics.class)) {
				kineticsBiUni.add(c.getCanonicalName());
			}
			if (s.contains(InterfaceBiBiKinetics.class)) {
				kineticsBiBi.add(c.getCanonicalName());
			}
			if (s.contains(InterfaceArbitraryEnzymeKinetics.class)) {
				kineticsArbitraryEnzymeMechanism.add(c.getCanonicalName());
			}
			if (s.contains(InterfaceGeneRegulatoryKinetics.class)) {
				kineticsGeneRegulatoryNetworks.add(c.getCanonicalName());
			}
			if (s.contains(InterfaceNonEnzymeKinetics.class)) {
				kineticsNonEnzyme.add(c.getCanonicalName());
			}
			if (s.contains(InterfaceZeroReactants.class)) {
				kineticsZeroReactants.add(c.getCanonicalName());
			}
			if (s.contains(InterfaceZeroProducts.class)) {
				kineticsZeroProducts.add(c.getCanonicalName());
			}
			if (s.contains(InterfaceModulatedKinetics.class)) {
				kineticsModulated.add(c.getCanonicalName());
			}
			if (s.contains(InterfaceIntegerStoichiometry.class)) {
				kineticsIntStoichiometry.add(c.getCanonicalName());
			}
		}
		msg.logln("done in " + (System.currentTimeMillis() - time) + " ms");
		msg.log("loading user settings...");
		CfgKeys.setDefaultsCfgFile(configFile);
		CfgKeys.setCommentCfgFile(configurationComment);
		CfgKeys.setUserPrefNode(userPrefDir);
		try {
			properties = CfgKeys.initProperties();
		} catch (BackingStoreException e) {
			e.printStackTrace();
			properties = CfgKeys.getDefaultProperties();
		}
		msg.logln(" done.");
	}

	/**
	 * 
	 * @param p
	 * @return
	 */
	private static Properties correctProperties(Properties p) {
		p = CfgKeys.correctProperties(p);
		String val;
		boolean allReversible = p
				.containsKey(CfgKeys.OPT_TREAT_ALL_REACTIONS_REVERSIBLE);
		if (allReversible) {
			allReversible &= Boolean.parseBoolean(p.get(
					CfgKeys.OPT_TREAT_ALL_REACTIONS_REVERSIBLE).toString());
		} else if (properties != null) {
			allReversible = properties
					.containsKey(CfgKeys.OPT_TREAT_ALL_REACTIONS_REVERSIBLE);
			if (allReversible) {
				allReversible &= Boolean.parseBoolean(properties.get(
						CfgKeys.OPT_TREAT_ALL_REACTIONS_REVERSIBLE).toString());
			}
		}
		for (Object k : p.keySet()) {
			if (k.toString().startsWith("KINETICS_")) {
				val = p.get(k).toString();
				if (!val.startsWith(KINETICS_PACKAGE)) {
					val = KINETICS_PACKAGE + '.' + val;
					p.put(k, val);
				}
				boolean invalid = false;
				switch (CfgKeys.valueOf(k.toString())) {
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
				if (!kineticsIrreversible.contains(val) && !allReversible) {
					invalid = false;
				}
				if (invalid) {
					String defaultKin = CfgKeys.getDefaultProperties().get(k)
							.toString();
					if (!defaultKin.startsWith(KINETICS_PACKAGE)) {
						defaultKin = KINETICS_PACKAGE + '.' + defaultKin;
					}
					p.put(k, defaultKin);
					msg.errf("Invalid %s %s; using default %s.", k.toString(),
							val, defaultKin);
				}
			}
		}
		return p;
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
		for (Object key : properties.keySet()) {
			if (key.toString().startsWith(prefix)) {
				if (((Boolean) properties.get(key)).booleanValue()) {
					enzymeTypes.add(key.toString().substring(prefix.length()));
				}
			}
		}
		return enzymeTypes.toArray(new String[] {});
	}

	/**
	 * 
	 * @return versionNumber
	 */
	public static final String getVersionNumber() {
		return versionNumber;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			System.out.println(System.getProperty("java.library.path"));
			System.loadLibrary("sbmlj");
			// Extra check to be sure we have access to libSBML:
			Class.forName("org.sbml.libsbml.libsbml");
		} catch (Throwable e) {
			msg.err("Error: could not load the libSBML library\n");
			e.printStackTrace();
			System.exit(1);
		}

		final SBMLsqueezer squeezer = new SBMLsqueezer(new LibSBMLReader(),
				new LibSBMLWriter());
		msg.log("scanning command line arguments...");
		final Properties p = correctProperties(CfgKeys
				.analyzeCommandLineArguments(args));
		for (Object key : p.keySet()) {
			CfgKeys.getProperties().put(key, p.get(key));
		}
		msg.logln(" done.\nreading SBO... done.");
		if (p.containsKey(CfgKeys.GUI)
				&& ((Boolean) p.get(CfgKeys.GUI)).booleanValue()) {
			if (p.containsKey(CfgKeys.SBML_FILE)) {
				squeezer.readSBMLSource(p.get(CfgKeys.SBML_FILE).toString());
			}
			if (p.containsKey(CfgKeys.CHECK_FOR_UPDATES)
					&& ((Boolean) p.get(CfgKeys.CHECK_FOR_UPDATES))
							.booleanValue()) {
				squeezer.checkForUpdate(true);
			}
			msg.log("loading GUI...");
			new Thread(new Runnable() {
				public void run() {
					msg.logln(" have fun!");
					if (p.containsKey(CfgKeys.SIMULATION_MODE)
							&& ((Boolean) p.get(CfgKeys.SIMULATION_MODE))
									.booleanValue()) {
						squeezer.showGUISimulation();
					} else {
						squeezer.showGUI();
					}
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
				line = line.replace("&#246;", "\u00f6"); // ö
				line = line.replace("&#169;", "\u00A9"); // ©
				line = line.replace("&#228;", "\u00e4"); // ä
				line = line.replace("&#252;", "\u00fc"); // ü
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

	/**
	 * 
	 */
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
		for (int i = 0; i < 60; i++) {
			sb.append('-');
		}
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
		if (((Boolean) properties.get(CfgKeys.CHECK_FOR_UPDATES))
				.booleanValue()) {
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
		return properties.put(key, Boolean.valueOf(value));
	}

	/**
	 * Change the configuration of SBMLsqueezer.
	 * 
	 * @param key
	 * @param value
	 * @return
	 */
	public Object set(CfgKeys key, double value) {
		return properties.put(key, Double.valueOf(value));
	}

	/**
	 * Change the configuration of SBMLsqueezer.
	 * 
	 * @param key
	 * @param value
	 * @return
	 */
	public Object set(CfgKeys key, int value) {
		return properties.put(key, Integer.valueOf(value));
	}

	/**
	 * Change the configuration of SBMLsqueezer.
	 * 
	 * @param key
	 * @param value
	 * @return
	 */
	public Object set(CfgKeys key, String value) {
		return properties.put(key, value);
	}

	/**
	 * Shows the GUI of SBMLsqueezer stand-alone.
	 */
	public void showGUI() {
		SBMLsqueezerUI gui = new SBMLsqueezerUI(sbmlIo, properties);
		gui.setLocationRelativeTo(null);
		gui.setVisible(true);
	}

	/**
	 * Shows the simulation GUI.
	 */
	public void showGUISimulation() {
		// TODO: Not in this version
		SBMLsqueezerUI gui = new SBMLsqueezerUI(sbmlIo, properties);
		if (properties.get(CfgKeys.CSV_FILE).toString().length() > 0) {
			// gui.showSimulationControl(true, settings.get(CfgKeys.CSV_FILE)
			// .toString());
		} else {
			// gui.showSimulationControl(true);
		}
		gui.dispose();
		try {
			CfgKeys.saveProperties(CfgKeys.getProperties());
		} catch (BackingStoreException exc) {
			exc.printStackTrace();
		}
		System.exit(0);
	}

	/**
	 * Reads in the given SBML file, squeezes kinetic equations in and writes
	 * the result back to the given SBML file. This method only works if
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
					.getSelectedModel(), properties);
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
			if ((outFile != null)
					&& (SBFileFilter.SBML_FILE_FILTER.accept(outFile))) {
				sbmlIo.writeSelectedModelToSBML(outFile.getAbsolutePath());
				msg.logln(" done in " + (System.currentTimeMillis() - time)
						+ " ms");
				if (((Boolean) properties.get(CfgKeys.SHOW_SBML_WARNINGS))
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
		String dir = properties.get(CfgKeys.LATEX_DIR).toString();
		if (latexFile != null) {
			File out = new File(latexFile);
			if (SBFileFilter.TeX_FILE_FILTER.accept(out)) {
				String path = out.getParent();
				if (!path.equals(dir)) {
					properties.put(CfgKeys.LATEX_DIR, path);
				}
				if (!out.exists()) {
					long time = System.currentTimeMillis();
					msg.log("writing LaTeX output...");
					LaTeXExport.writeLaTeX(sbmlIo.getSelectedModel(), out,
							properties);
					msg.logf(" done in %d ms\n",
							(System.currentTimeMillis() - time));
				}
			} else {
				msg.errf("no valid TeX file: %s\n", latexFile);
			}
		} else
			msg.errln("no TeX file was provided");
	}
}
