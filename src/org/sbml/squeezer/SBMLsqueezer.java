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
package org.sbml.squeezer;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.BackingStoreException;


import org.sbml.jsbml.SBMLException;
import org.sbml.jsbml.SBMLInputConverter;
import org.sbml.jsbml.SBMLOutputConverter;
import org.sbml.jsbml.util.IOProgressListener;
import org.sbml.jsbml.xml.libsbml.LibSBMLReader;
import org.sbml.jsbml.xml.libsbml.LibSBMLWriter;
import org.sbml.squeezer.gui.SBMLsqueezerUI;
import org.sbml.squeezer.io.SBMLio;
import org.sbml.squeezer.io.SqSBMLReader;
import org.sbml.squeezer.io.SqSBMLWriter;
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
import org.sbml.squeezer.math.StabilityOptions;
import org.sbml.squeezer.resources.Resource;
import org.sbml.tolatex.LaTeXOptions;
import org.sbml.tolatex.SBML2LaTeX;
import org.sbml.tolatex.io.LaTeXOptionsIO;

import de.zbit.gui.GUIOptions;
import de.zbit.gui.UpdateMessage;
import de.zbit.io.SBFileFilter;
import de.zbit.util.ProgressBar;
import de.zbit.util.Reflect;
import de.zbit.util.logging.LogUtil;
import de.zbit.util.prefs.KeyProvider;
import de.zbit.util.prefs.SBPreferences;
import de.zbit.util.prefs.SBProperties;

/**
 * The main program of SBMLsqueezer. This class initializes all required
 * objects, starts the GUI if desired and loads all settings from the user.
 * 
 * @author Andreas Dr&auml;ger
 * @author Nadine Hassis
 * @author Hannes Borch
 * @author Sarah R. M&uuml;ller vom Hagen
 * @since 1.0
 * @version $Rev$
 */
public class SBMLsqueezer implements IOProgressListener {
	/**
	 * 
	 */
	private static SBPreferences preferences;
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
	private static final Logger logger;
	/**
	 * The number of the current SBMLsqueezer version.
	 */
	private static final String versionNumber = "1.4";

	/**
	 * Load all available kinetic equations and the user's settings from the
	 * configuration file.
	 */
	static {
		logger = Logger.getLogger(SBMLsqueezer.class.getName());
		long time = System.currentTimeMillis();
		logger.info("Loading kinetic equations... ");
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
		logger.log(Level.INFO, "    done in " + (System.currentTimeMillis() - time)
				+ " ms.");
		logger.log(Level.INFO, "loading user settings...");
		preferences = new SBPreferences(SqueezerOptions.class);
		logger.log(Level.INFO, "    done.");
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
		for (Object key : preferences.keySet()) {
			if (key.toString().startsWith(prefix)) {
				if (preferences.getBoolean(key)) {
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
	 * @throws MalformedURLException 
	 */
    public static void main(String[] args) throws MalformedURLException {
	LogUtil.initializeLogging("org.sbml");
	boolean libSBMLAvailable = false;
	try {
	    // In order to initialize libSBML, check the java.library.path.
	    System.loadLibrary("sbmlj");
	    // Extra check to be sure we have access to libSBML:
	    Class.forName("org.sbml.libsbml.libsbml");
	    logger.log(Level.INFO, "Loading libSBML\n");
	    libSBMLAvailable = true;
	} catch (Error e) {
	} catch (Throwable e) {
	} 
	SBMLInputConverter reader = null;
	SBMLOutputConverter writer = null;
	if (!libSBMLAvailable) {
	    logger.log(Level.INFO, "Loading JSBML\n");
	    reader = new SqSBMLReader() ;
	    writer = new SqSBMLWriter() ;
	} else {
	    reader = new LibSBMLReader();
	    writer = new LibSBMLWriter();
	}

		final SBMLsqueezer squeezer = new SBMLsqueezer(reader,
				writer);
		logger.info("scanning command line arguments...");
		final SBProperties p = SBPreferences.analyzeCommandLineArguments(getListOfCommandLineOptions(), args);
		for (Class<? extends KeyProvider> keyProvider : getListOfCommandLineOptions()) {
			SBPreferences prefs = keyProvider != SqueezerOptions.class ? SBPreferences
					.getPreferencesFor(keyProvider) : preferences;
			for (Object key : p.keySet()) {
				if (KeyProvider.Tools.providesOption(keyProvider, key.toString())) {
					prefs.put(key, p.get(key));
				}
			}
			try {
				prefs.flush();
			} catch (BackingStoreException exc) {
				exc.printStackTrace();
			}
		}
		logger.info("   done.\nreading SBO... done.");
		if (p.getBooleanProperty(GUIOptions.GUI)) {
			if (p.containsKey(SqueezerOptions.SBML_IN_FILE)) {
				squeezer.readSBMLSource(p.get(SqueezerOptions.SBML_IN_FILE));
			}
			logger.info("loading GUI...");
			new Thread(new Runnable() {
				public void run() {
					logger.info("   have fun!");
					// TODO: Not in this version.
					// if (p.containsKey(CfgKeys.SIMULATION_MODE)
					// && ((Boolean) p.get(CfgKeys.SIMULATION_MODE))
					// .booleanValue()) {
					// squeezer.showGUISimulation();
					// } else {
					squeezer.showGUI();
					// }
				}
			}).start();
		} else {
			if ((squeezer.getSBMLIO().getNumErrors() > 0)
					&& p.getBoolean(SqueezerOptions.SHOW_SBML_WARNINGS)) {
				for (SBMLException exc : squeezer.getSBMLIO().getWarnings()) {
					logger.log(Level.WARNING, exc.getMessage());
				}
			}
			// Do a lot of other stuff...
			squeezer.checkForUpdate(false);
			if (p.containsKey(SqueezerOptions.SBML_OUT_FILE)) {
				try {
					squeezer.squeeze(p.get(SqueezerOptions.SBML_IN_FILE).toString(), p
							.get(SqueezerOptions.SBML_OUT_FILE).toString());
				} catch (Throwable e) {
					e.printStackTrace();
				}
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
				if (line.endsWith("<br>")) {
					line = line.substring(0, line.length() - 4);
				} else if (line.endsWith("<br/>")) {
				  line = line.substring(0, line.length() - 5);
				}
				if (line.contains("<a href")) {
					logger.info(line.substring(0, line.indexOf('<') - 1) + ' ');
				} else {
					logger.info(line);
				}
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
		logger.info(sb.toString());
		showAboutMsg();
		logger.info(sb.toString());
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
	 * @throws MalformedURLException 
	 */
	public void checkForUpdate(final boolean gui) throws MalformedURLException {
		UpdateMessage update = new UpdateMessage(false, getClass()
				.getSimpleName(), getURLOnlineUpdate(), getVersionNumber(),
				true);
		update.execute();
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
	 * @see org.sbml.jsbml.io.IOProgressListener#progress(java.lang.Object)
	 */
	public void ioProgressOn(Object currObject) {
		if (currObject != null) {
			logger.info(currObject.toString());
		}
	}

	/**
	 * 
	 * @param sbmlSource
	 */
	public void readSBMLSource(Object sbmlSource) {
		long time = System.currentTimeMillis();
		logger.info("reading SBML file...");
		try {
			sbmlIo.convertModel(sbmlSource);
			logger.info(String.format("    done in %d ms.\n", 
							(System.currentTimeMillis() - time)) );
		} catch (Exception exc) {
			logger.log(Level.WARNING, String.format("A problem occured while trying to read the model: %s\n",
							exc.getMessage() ));
		}
	}

	/**
	 * Shows the GUI of SBMLsqueezer stand-alone.
	 */
	public void showGUI() {
		SBMLsqueezerUI gui = new SBMLsqueezerUI(sbmlIo);
		gui.setLocationRelativeTo(null);
		gui.setVisible(true);
	}

//	/**
//	 * Shows the simulation GUI.
//	 */
//	public void showGUISimulation() {
//		 TODO: Not in this version
//		 SBProperties properties = configuration.getProperties();
//		SBMLsqueezerUI gui = new SBMLsqueezerUI(sbmlIo);
//		 if (properties.get(CfgKeys.CSV_FILE).toString().length() > 0) {
//		 // gui.showSimulationControl(true, settings.get(CfgKeys.CSV_FILE)
//		 // .toString());
//		 } else {
//		 // gui.showSimulationControl(true);
//		 }
//		showGUI();
//	}

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
			
			KineticLawGenerator klg = new KineticLawGenerator(sbmlIo
					.getSelectedModel());
			
			ProgressBar progressBar = new ProgressBar(0);
			klg.setProgressBar(progressBar);
			
			klg.generateLaws();
			
			progressBar = new ProgressBar(0);
			klg.setProgressBar(progressBar);
			
			klg.storeKineticLaws();
			long time = System.currentTimeMillis();
			logger.info("Saving changes and writing SBML file... ");
			sbmlIo.saveChanges(this);
			if ((outFile != null)
					&& (SBFileFilter.createSBMLFileFilter().accept(outFile))) {
				sbmlIo.writeSelectedModelToSBML(outFile.getAbsolutePath());
				logger.info("    done in " + (System.currentTimeMillis() - time) + " ms.");
				if (preferences.getBoolean(SqueezerOptions.SHOW_SBML_WARNINGS)) {
					for (SBMLException exc : sbmlIo.getWriteWarnings()) {
						logger.log(Level.WARNING, exc.getMessage());
					}
				}
			} else {
				logger.log(Level.WARNING, "Could not write output to SBML.");
			}
		} else {
			logger.log(Level.WARNING, "File contains no model. Nothing to do.");
		}
	}

	/**
	 * Convenient method that writes a LaTeX file from the given SBML source.
	 * 
	 * @param sbmlInfile
	 * @param latexFile
	 * @throws IOException
	 * @throws SBMLException 
	 */
	public void toLaTeX(Object sbmlSource, String latexFile) throws IOException, SBMLException {
		readSBMLSource(sbmlSource);
		SBPreferences prefsLaTeX = SBPreferences.getPreferencesFor(LaTeXOptionsIO.class);
		String dir = prefsLaTeX.get(LaTeXOptionsIO.LATEX_DIR).toString();
		if (latexFile != null) {
			File out = new File(latexFile);
			if (SBFileFilter.createTeXFileFilter().accept(out)) {
				String path = out.getParent();
				if (!path.equals(dir)) {
					prefsLaTeX.put(LaTeXOptionsIO.LATEX_DIR, path);
				}
				if (!out.exists()) {
					long time = System.currentTimeMillis();
					logger.info("writing LaTeX output...");
					SBML2LaTeX.convert(sbmlIo.getSelectedModel(), out);
					logger.info(String.format("    done in %d ms\n",
							(System.currentTimeMillis() - time)) );
				}
			} else {
				logger.log(Level.WARNING, String.format("no valid TeX file: %s\n", latexFile) );
			}
		} else {
			logger.log(Level.WARNING, "no TeX file was provided");
		}
	}

	/**
	 * @throws BackingStoreException
	 * 
	 */
	public static void saveProperties() throws BackingStoreException {
		preferences.flush();
	}

	/**
	 * 
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static Class<? extends KeyProvider>[] getCommandLineOptions() {
		return getListOfCommandLineOptions().toArray(new Class[0]);
	}

	/**
	 * 
	 * @return
	 */
	public static List<Class<? extends KeyProvider>> getListOfCommandLineOptions() {
		List<Class<? extends KeyProvider>> list = new LinkedList<Class<? extends KeyProvider>>();
		list.add(SqueezerOptions.class);
		list.add(StabilityOptions.class);
		list.add(GUIOptions.class);
		list.add(LaTeXOptions.class);
		return list;
	}

	/**
	 * @throws MalformedURLException 
	 * 
	 */
	public static URL getURLOnlineUpdate() throws MalformedURLException {
		return new URL("http://www.cogsys.cs.uni-tuebingen.de/software/SBMLsqueezer/downloads/");
	}
}
