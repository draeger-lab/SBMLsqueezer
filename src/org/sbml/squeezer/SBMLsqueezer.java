/*
 * $Id$
 * $URL$
 * ---------------------------------------------------------------------
 * This file is part of SBMLsqueezer, a Java program that creates rate
 * equations for reactions in SBML files (http://sbml.org).
 *
 * Copyright (C) 2006-2015 by the University of Tuebingen, Germany.
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

import static de.zbit.util.Utils.getMessage;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.stream.XMLStreamException;

import org.sbml.jsbml.Model;
import org.sbml.jsbml.Reaction;
import org.sbml.jsbml.SBMLDocument;
import org.sbml.jsbml.SBMLException;
import org.sbml.jsbml.SBMLInputConverter;
import org.sbml.jsbml.SBMLOutputConverter;
import org.sbml.jsbml.xml.libsbml.LibSBMLReader;
import org.sbml.jsbml.xml.libsbml.LibSBMLWriter;
import org.sbml.squeezer.gui.SBMLsqueezerUI;
import org.sbml.squeezer.io.IOOptions;
import org.sbml.squeezer.io.SBMLio;
import org.sbml.squeezer.io.SqSBMLReader;
import org.sbml.squeezer.io.SqSBMLWriter;
import org.sbml.squeezer.kinetics.OptionsRateLaws;
import org.sbml.squeezer.sabiork.SABIORKOptions;
import org.sbml.squeezer.sabiork.SABIORKPreferences;
import org.sbml.squeezer.sabiork.wizard.SABIORKWizard;
import org.sbml.squeezer.util.Bundles;
import org.sbml.tolatex.LaTeXOptions;
import org.sbml.tolatex.SBML2LaTeX;

import de.zbit.AppConf;
import de.zbit.Launcher;
import de.zbit.garuda.GarudaOptions;
import de.zbit.gui.GUIOptions;
import de.zbit.io.FileWalker;
import de.zbit.io.filefilter.SBFileFilter;
import de.zbit.util.ResourceManager;
import de.zbit.util.prefs.KeyProvider;
import de.zbit.util.prefs.SBPreferences;
import de.zbit.util.prefs.SBProperties;
import de.zbit.util.progressbar.ProgressBar;

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
 * @param <T> the type of SBML documents that can be treated by this controller.
 */
@SuppressWarnings("unchecked")
public class SBMLsqueezer<T> extends Launcher {
  
  private static Boolean libSBMLAvailable = null;
  /**
   * The {@link Logger} for this class.
   */
  private static final transient Logger logger = Logger.getLogger(SBMLsqueezer.class.getName());
  
  /**
   * Localization support.
   */
  public static final transient ResourceBundle MESSAGES = ResourceManager.getBundle(Bundles.MESSAGES);
  
  private static boolean sabiorkEnabled = true;
  
  /**
   * Generated serial version identifier.
   */
  private static final long serialVersionUID = 8751196023375780898L;
  
  /**
   * Localization support.
   */
  public static final transient ResourceBundle WARNINGS = ResourceManager.getBundle(Bundles.WARNINGS);
  
  /**
   * 
   * @return
   */
  public static List<Class<? extends KeyProvider>> getInteractiveConfigOptions() {
    return Arrays.asList(getInteractiveConfigOptionsArray());
  }
  
  /**
   * 
   * @return
   */
  public static Class<? extends KeyProvider>[] getInteractiveConfigOptionsArray() {
    Class<? extends KeyProvider>[] list = new Class[sabiorkEnabled ? 5 : 3];
    list[0] = OptionsGeneral.class;
    list[1] = OptionsRateLaws.class;
    if (sabiorkEnabled) {
      list[2] = SABIORKOptions.class;
      list[3] = SABIORKPreferences.class;
    }
    list[sabiorkEnabled ? 4 : 2] = LaTeXOptions.class;
    return list;
  }
  
  /**
   * Returns an array of Strings that can be interpreted as enzymes. In
   * particular this array will contain those configuration keys as strings
   * for which in the current configuration the corresponding value is set to
   * {@code true}.
   * 
   * @return
   */
  public static String[] getPossibleEnzymeTypes() {
    ResourceBundle MESSAGES = ResourceManager.getBundle(Bundles.MESSAGES);
    logger.log(Level.INFO, MESSAGES.getString("LOADING_USER_SETTINGS"));
    SBPreferences preferences = new SBPreferences(OptionsGeneral.class);
    logger.log(Level.INFO, "    " + MESSAGES.getString("DONE"));
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
   * @return the sabiorkEnabled
   */
  public static boolean isSABIORKEnabled() {
    return sabiorkEnabled;
  }
  
  /**
   * @param args
   */
  @SuppressWarnings("rawtypes")
  public static void main(String[] args) {
    new SBMLsqueezer(args);
  }
  
  /**
   * @param sabiorkEnabled the sabiorkEnabled to set
   */
  public static void setSABIORKEnabled(boolean sabiorkEnabled) {
    SBMLsqueezer.sabiorkEnabled = sabiorkEnabled;
  }
  
  /**
   * 
   */
  private AppConf appConf;
  
  /**
   * 
   */
  private SBMLio<T> sbmlIo;
  
  /**
   * 
   */
  public SBMLsqueezer() {
    this(null, null);
  }
  
  /**
   * This constructor allows the integration of SBMLsqueezer into third-party
   * programs, i.e., as a CellDesigner plug-in.
   * 
   * @param sbmlReader
   * @param sbmlWriter
   */
  public SBMLsqueezer(SBMLInputConverter<T> sbmlReader,
    SBMLOutputConverter<T> sbmlWriter) {
    super();
    if ((sbmlReader != null) && (sbmlWriter != null)) {
      sbmlIo = new SBMLio<T>(sbmlReader, sbmlWriter);
    }
  }
  
  /**
   * 
   * @param args
   */
  public SBMLsqueezer(String[] args) {
    super(args);
  }
  
  /* (non-Javadoc)
   * @see de.zbit.Launcher#commandLineMode(de.zbit.AppConf)
   */
  @Override
  public void commandLineMode(AppConf appConf) {
    this.appConf = appConf;
    SBProperties properties = appConf.getCmdArgs();
    List<SBMLException> listOfExceptions = getSBMLIO().getWarnings();
    if ((listOfExceptions.size() > 0)
        && properties.getBoolean(OptionsGeneral.SHOW_SBML_WARNINGS)) {
      for (SBMLException exc : listOfExceptions) {
        logger.log(Level.WARNING, exc.getMessage());
      }
    }
    if (properties.containsKey(IOOptions.SBML_OUT_FILE)) {
      try {
        boolean searchSABIO = false;
        if ((properties.containsKey(OptionsGeneral.READ_FROM_SABIO_RK)) && (properties.getBoolean(OptionsGeneral.READ_FROM_SABIO_RK))) {
          searchSABIO = true;
        }
        else {
          SBPreferences preferences = new SBPreferences(OptionsGeneral.class);
          if (preferences.getBoolean(OptionsGeneral.READ_FROM_SABIO_RK)) {
            searchSABIO = true;
          }
        }
        squeeze(properties.get(IOOptions.SBML_IN_FILE).toString(),
          properties.get(IOOptions.SBML_OUT_FILE).toString(), searchSABIO);
      } catch (Throwable e) {
        e.printStackTrace();
      }
    }
  }
  
  /* (non-Javadoc)
   * @see de.zbit.Launcher#getAppName()
   */
  @Override
  public String getAppName() {
    return getClass().getSimpleName();
  }
  
  /* (non-Javadoc)
   * @see de.zbit.Launcher#getCmdLineOptions()
   */
  @Override
  public List<Class<? extends KeyProvider>> getCmdLineOptions() {
    List<Class<? extends KeyProvider>> list = new ArrayList<Class<? extends KeyProvider>>(4);
    list.add(IOOptions.class);
    list.add(OptionsGeneral.class);
    list.add(OptionsRateLaws.class);
    list.add(SABIORKPreferences.class);
    list.add(SABIORKOptions.class);
    list.add(GUIOptions.class);
    list.add(LaTeXOptions.class);
    list.add(GarudaOptions.class);
    return list;
  }
  
  /* (non-Javadoc)
   * @see de.zbit.Launcher#getInteractiveOptions()
   */
  @Override
  public List<Class<? extends KeyProvider>> getInteractiveOptions() {
    return getInteractiveConfigOptions();
  }
  
  
  /* (non-Javadoc)
   * @see de.zbit.Launcher#getLogPackages()
   */
  @Override
  public String[] getLogPackages() {
    return new String[] {"org.sbml", "de.zbit"};
  }
  
  /**
   * 
   * @return
   */
  public SBMLio<T> getSBMLIO() {
    return sbmlIo;
  }
  
  /* (non-Javadoc)
   * @see de.zbit.Launcher#getURLlicenseFile()
   */
  @Override
  public URL getURLlicenseFile() {
    URL url = null;
    try {
      url = new URL("http://www.gnu.org/licenses/gpl-3.0-standalone.html");
    } catch (MalformedURLException exc) {
      logger.log(Level.FINER, getMessage(exc), exc);
    }
    return url;
  }
  
  /* (non-Javadoc)
   * @see de.zbit.Launcher#getURLOnlineUpdate()
   */
  @Override
  public URL getURLOnlineUpdate() {
    URL url = null;
    try {
      url = new URL("http://www.cogsys.cs.uni-tuebingen.de/software/SBMLsqueezer/downloads/");
    } catch (MalformedURLException exc) {
      logger.log(Level.FINER, getMessage(exc), exc);
    }
    return url;
  }
  
  /* (non-Javadoc)
   * @see de.zbit.Launcher#getVersionNumber()
   */
  @Override
  public String getVersionNumber() {
    return "2.0.2";
  }
  
  /* (non-Javadoc)
   * @see de.zbit.Launcher#getYearOfProgramRelease()
   */
  @Override
  public short getYearOfProgramRelease() {
    return (short) 2014;
  }
  
  /* (non-Javadoc)
   * @see de.zbit.Launcher#getYearWhenProjectWasStarted()
   */
  @Override
  public short getYearWhenProjectWasStarted() {
    return (short) 2006;
  }
  
  /* (non-Javadoc)
   * @see de.zbit.Launcher#initGUI(de.zbit.AppConf)
   */
  @Override
  public java.awt.Window initGUI(AppConf appConf) {
    SBProperties properties = appConf.getCmdArgs();
    if (properties.containsKey(IOOptions.SBML_IN_FILE)) {
      readSBMLSource(properties.get(IOOptions.SBML_IN_FILE));
    }
    return new SBMLsqueezerUI(getSBMLIO(), appConf);
  }
  
  /**
   * Does initialization for creating a SBMLsqueezer Object.
   * Checks if libSBML is available and initializes the Reader/Writer.
   * 
   * @param tryLoadingLibSBML
   */
  private void initializeReaderAndWriter(boolean tryLoadingLibSBML) {
    if (tryLoadingLibSBML) {
      if (libSBMLAvailable == null) {
        try {
          // In order to initialize libSBML, check the java.library.path.
          System.loadLibrary("sbmlj");
          // Extra check to be sure we have access to libSBML:
          Class.forName("org.sbml.libsbml.libsbml");
          logger.info(MESSAGES.getString("LOADING_LIBSBML"));
          libSBMLAvailable = Boolean.TRUE;
        } catch (Error e) {
          libSBMLAvailable = Boolean.FALSE;
        } catch (Throwable e) {
          libSBMLAvailable = Boolean.FALSE;
        }
      }
      if (libSBMLAvailable.booleanValue()) {
        logger.info(MESSAGES.getString("LAUNCHING_LIBSBML"));
        sbmlIo = (SBMLio<T>) new SBMLio<org.sbml.libsbml.Model>(
            new LibSBMLReader(), new LibSBMLWriter());
      }
    }
    if (sbmlIo == null) {
      logger.info(MESSAGES.getString("LOADING_JSBML"));
      sbmlIo = (SBMLio<T>) new SBMLio<Model>(new SqSBMLReader(),
          new SqSBMLWriter());
    }
  }
  
  /* (non-Javadoc)
   * @see de.zbit.Launcher#isGarudaEnabled()
   */
  @Override
  public boolean isGarudaEnabled() {
    return true;
  }
  
  /**
   * 
   * @param sbmlSource
   */
  public Model readSBMLSource(Object sbmlSource) {
    Model model = null;
    long time = System.currentTimeMillis();
    logger.info(MESSAGES.getString("READING_SBML_FILE"));
    try {
      SBMLio<T> sbmlio = getSBMLIO();
      if (sbmlSource instanceof File) {
        model = sbmlio.convertSBMLDocument((File) sbmlSource).getModel();
      } else if (sbmlSource instanceof String) {
        model = sbmlio.convertSBMLDocument(sbmlSource.toString()).getModel();
      } else {
        model = sbmlio.convertModel((T) sbmlSource);
      }
      logger.info(MessageFormat.format(MESSAGES.getString("DONE_IN_MS"), (System.currentTimeMillis() - time)));
    } catch (Exception exc) {
      logger.log(Level.WARNING, String.format(WARNINGS.getString("CANT_READ_MODEL"), getMessage(exc)));
    }
    return model;
  }
  
  /* (non-Javadoc)
   * @see de.zbit.Launcher#setUp()
   */
  @Override
  protected void setUp() {
    if (sbmlIo == null) {
      initializeReaderAndWriter(getAppConf().getCmdArgs().getBooleanProperty(
        IOOptions.TRY_LOADING_LIBSBML));
    }
  }
  
  /**
   * 
   * @param source
   * @param outFile
   * @param showProgress
   * @param searchSABIO
   * @throws Throwable
   */
  public void squeeze(File source, File outFile, boolean showProgress, boolean searchSABIO) throws Throwable {
    long workTime = System.currentTimeMillis();
    readSBMLSource(source.getAbsolutePath());
    boolean errorFatal = false;
    SBMLException exception = null;
    List<SBMLException> listOfWarnings = sbmlIo.getWarnings();
    for (SBMLException exc : listOfWarnings) {
      if (exc.isFatal() || exc.isXML() || exc.isError()) {
        errorFatal = true;
        exception = exc;
      }
    }
    if (errorFatal) {
      throw new SBMLException(exception);
    } else if (!sbmlIo.getListOfOpenedFiles().isEmpty()) {
      
      Set<Reaction> reactionsWithSABIOKinetics = null;
      if (searchSABIO) {
        boolean overwriteExistingRateLaws = false;
        
        if (appConf.getCmdArgs().containsKey(OptionsGeneral.OVERWRITE_EXISTING_RATE_LAWS)) {
          overwriteExistingRateLaws = appConf.getCmdArgs().getBoolean(OptionsGeneral.OVERWRITE_EXISTING_RATE_LAWS);
        }
        else {
          SBPreferences prefs = new SBPreferences(OptionsGeneral.class);
          overwriteExistingRateLaws = prefs.getBoolean(OptionsGeneral.OVERWRITE_EXISTING_RATE_LAWS);
        }
        reactionsWithSABIOKinetics = squeezeWithKineticsFromSABIORK(sbmlIo.getSelectedModel().getSBMLDocument(), overwriteExistingRateLaws);
      }
      KineticLawGenerator klg = new KineticLawGenerator(sbmlIo.getSelectedModel(), reactionsWithSABIOKinetics);
      ProgressBar progressBar = null;
      if (showProgress) {
        progressBar = new ProgressBar(0);
        klg.setProgressBar(progressBar);
      }
      
      
      long time = System.currentTimeMillis();
      logger.info(MESSAGES.getString("CREATING_KINETIC_LAWS"));
      klg.generateLaws();
      logger.info(MessageFormat.format(MESSAGES.getString("DONE_IN_MS"), (System.currentTimeMillis() - time)));
      
      if (showProgress) {
        progressBar = new ProgressBar(0);
        klg.setProgressBar(progressBar);
      }
      klg.storeKineticLaws();
      
      time = System.currentTimeMillis();
      logger.info(MESSAGES.getString("SAVING_TO_FILE"));
      if ((outFile != null)
          && (SBFileFilter.hasFileType(outFile, SBFileFilter.FileType.SBML_FILES)) || SBFileFilter.createSBMLFileFilter().accept(outFile)) {
        sbmlIo.writeSelectedModelToSBML(outFile.getAbsolutePath());
        logger.info(MessageFormat.format(MESSAGES.getString("DONE_IN_MS"), (System.currentTimeMillis() - time)));
        SBPreferences preferences = new SBPreferences(OptionsGeneral.class);
        if (preferences.getBoolean(OptionsGeneral.SHOW_SBML_WARNINGS)) {
          listOfWarnings = sbmlIo.getWriteWarnings();
          for (SBMLException exc : listOfWarnings) {
            logger.log(Level.WARNING, exc.getMessage());
          }
        }
      } else {
        logger.log(Level.WARNING, WARNINGS.getString("OUTPUT_ERROR"));
      }
    } else {
      logger.log(Level.WARNING, WARNINGS.getString("FILE_CONTAINS_NO_MODEL"));
    }
    logger.info(MessageFormat.format(
      MESSAGES.getString("TIME_NEEDED_FOR_SQUEEZING"),
      (System.currentTimeMillis() - workTime)/1000d,
      source.getName()));
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
   * @param searchSABIO
   * 						Also search for kinetics in SABIO-RK and add them if possible
   * @throws Throwable
   */
  public void squeeze(Object sbmlSource, String outfile, boolean searchSABIO) throws Throwable {
    File outFile = outfile != null ? new File(outfile) : null;
    File inFile = sbmlSource != null ? new File(sbmlSource.toString()) : null;
    logger.info(MESSAGES.getString("SCANNING_INPUT_FILES"));
    Map<File, String> ioPairs = FileWalker.filterAndCreate(inFile, outFile, SBFileFilter.createSBMLFileFilter(), true);
    if (ioPairs.size() == 0) {
      logger.info(MESSAGES.getString("EMPTY_INPUT_FILE_LIST"));
    } else {
      for (Map.Entry<File, String> entry : ioPairs.entrySet()) {
        try {
          inFile = entry.getKey();
          outFile = new File(entry.getValue());
          logger.info(MessageFormat.format(
            MESSAGES.getString("SQUEEZING_FILE"),
            inFile.getAbsolutePath(),
            outFile.getAbsolutePath()));
          squeeze(inFile, outFile, false, searchSABIO);
        } catch (Throwable t) {
          logger.log(Level.SEVERE, MessageFormat.format(
            WARNINGS.getString("SQUEEZE_ERROR"),
            entry.getKey().getAbsolutePath(), entry.getValue()), t);
        }
      }
    }
  }
  
  /**
   * @param absolutePath
   * @param outputPath
   * @throws Throwable
   */
  public void squeeze(String absolutePath, String outputPath) throws Throwable {
    squeeze(absolutePath, outputPath, false);
  }
  
  /**
   * Searches for SABIO-RK kinetics
   * @param sbmlDocument
   * @param overwriteExistingRateLaws
   * @return changedReactions
   * @throws XMLStreamException
   * @throws IOException
   */
  public Set<Reaction> squeezeWithKineticsFromSABIORK(SBMLDocument sbmlDocument, boolean overwriteExistingRateLaws) throws XMLStreamException, IOException {
    SBProperties properties = appConf.getCmdArgs();
    SBPreferences options = new SBPreferences(SABIORKOptions.class);
    SBPreferences prefs = new SBPreferences(SABIORKPreferences.class);
    
    
    
    String pathway = null;
    if (properties.containsKey(SABIORKOptions.PATHWAY)) {
      pathway = properties.get(SABIORKOptions.PATHWAY);
    }
    else {
      pathway = options.get(SABIORKOptions.PATHWAY);
    }
    
    String tissue = null;
    if (properties.containsKey(SABIORKOptions.TISSUE)) {
      tissue = properties.get(SABIORKOptions.TISSUE);
    }
    else {
      tissue = options.get(SABIORKOptions.TISSUE);
    }
    
    String organism = null;
    if (properties.containsKey(SABIORKOptions.ORGANISM)) {
      organism = properties.get(SABIORKOptions.ORGANISM);
    }
    else {
      organism = options.get(SABIORKOptions.ORGANISM);
    }
    
    String cellularLocation = null;
    if (properties.containsKey(SABIORKOptions.CELLULAR_LOCATION)) {
      cellularLocation = properties.get(SABIORKOptions.CELLULAR_LOCATION);
    }
    else {
      cellularLocation = options.get(SABIORKOptions.CELLULAR_LOCATION);
    }
    
    Boolean isWildtype = null;
    if (properties.containsKey(SABIORKPreferences.IS_WILDTYPE)) {
      isWildtype = properties.getBoolean(SABIORKPreferences.IS_WILDTYPE);
    }
    else {
      isWildtype = prefs.getBoolean(SABIORKPreferences.IS_WILDTYPE);
    }
    
    Boolean isMutant = null;
    if (properties.containsKey(SABIORKPreferences.IS_MUTANT)) {
      isMutant = properties.getBoolean(SABIORKPreferences.IS_MUTANT);
    }
    else {
      isMutant = prefs.getBoolean(SABIORKPreferences.IS_MUTANT);
    }
    
    Boolean isRecombinant = null;
    if (properties.containsKey(SABIORKPreferences.IS_RECOMBINANT)) {
      isRecombinant = properties.getBoolean(SABIORKPreferences.IS_RECOMBINANT);
    }
    else {
      isRecombinant = prefs.getBoolean(SABIORKPreferences.IS_RECOMBINANT);
    }
    
    Boolean hasKineticData = null;
    if (properties.containsKey(SABIORKPreferences.HAS_KINETIC_DATA)) {
      hasKineticData = properties.getBoolean(SABIORKPreferences.HAS_KINETIC_DATA);
    }
    else {
      hasKineticData = prefs.getBoolean(SABIORKPreferences.HAS_KINETIC_DATA);
    }
    
    Boolean isDirectSubmission = null;
    if (properties.containsKey(SABIORKPreferences.IS_DIRECT_SUBMISSION)) {
      isDirectSubmission = properties.getBoolean(SABIORKPreferences.IS_DIRECT_SUBMISSION);
    }
    else {
      isDirectSubmission = prefs.getBoolean(SABIORKPreferences.IS_DIRECT_SUBMISSION);
    }
    
    Boolean isJournal = null;
    if (properties.containsKey(SABIORKPreferences.IS_JOURNAL)) {
      isJournal = properties.getBoolean(SABIORKPreferences.IS_JOURNAL);
    }
    else {
      isJournal = prefs.getBoolean(SABIORKPreferences.IS_JOURNAL);
    }
    
    Boolean isEntriesInsertedSince = null;
    if (properties.containsKey(SABIORKPreferences.IS_ENTRIES_INSERTED_SINCE)) {
      isEntriesInsertedSince = properties.getBoolean(SABIORKPreferences.IS_ENTRIES_INSERTED_SINCE);
    }
    else {
      isEntriesInsertedSince = prefs.getBoolean(SABIORKPreferences.IS_ENTRIES_INSERTED_SINCE);
    }
    
    Double lowerpHValue = null;
    if (properties.containsKey(SABIORKPreferences.LOWEST_PH_VALUE)) {
      lowerpHValue = properties.getDouble(SABIORKPreferences.LOWEST_PH_VALUE);
    }
    else {
      lowerpHValue = prefs.getDouble(SABIORKPreferences.LOWEST_PH_VALUE);
    }
    
    Double upperpHValue = null;
    if (properties.containsKey(SABIORKPreferences.HIGHEST_PH_VALUE)) {
      upperpHValue = properties.getDouble(SABIORKPreferences.HIGHEST_PH_VALUE);
    }
    else {
      upperpHValue = prefs.getDouble(SABIORKPreferences.HIGHEST_PH_VALUE);
    }
    
    Double lowerTemperature = null;
    if (properties.containsKey(SABIORKPreferences.LOWEST_TEMPERATURE_VALUE)) {
      lowerTemperature = properties.getDouble(SABIORKPreferences.LOWEST_TEMPERATURE_VALUE);
    }
    else {
      lowerTemperature = prefs.getDouble(SABIORKPreferences.LOWEST_TEMPERATURE_VALUE);
    }
    
    Double upperTemperature = null;
    if (properties.containsKey(SABIORKPreferences.HIGHEST_TEMPERATURE_VALUE)) {
      upperTemperature = properties.getDouble(SABIORKPreferences.HIGHEST_TEMPERATURE_VALUE);
    }
    else {
      upperTemperature = prefs.getDouble(SABIORKPreferences.HIGHEST_TEMPERATURE_VALUE);
    }
    
    String dateSubmitted = null;
    if (properties.containsKey(SABIORKPreferences.LOWEST_DATE)) {
      dateSubmitted = properties.get(SABIORKPreferences.LOWEST_DATE);
    }
    else {
      dateSubmitted = prefs.get(SABIORKPreferences.LOWEST_DATE);
    }
    
    Set<Reaction> changedReactions = SABIORKWizard.getResultConsole(sbmlDocument, overwriteExistingRateLaws, pathway, tissue, organism, cellularLocation, isWildtype, isMutant, isRecombinant, hasKineticData, lowerpHValue, upperpHValue, lowerTemperature, upperTemperature, isDirectSubmission, isJournal, isEntriesInsertedSince, dateSubmitted);
    return changedReactions;
  }
  
  /**
   * Convenient method that writes a LaTeX file from the given SBML source.
   * 
   * @param sbmlSource
   * @param latexFile
   * @throws IOException
   * @throws SBMLException
   * @throws XMLStreamException
   */
  public void toLaTeX(Object sbmlSource, String latexFile) throws IOException, SBMLException, XMLStreamException {
    readSBMLSource(sbmlSource);
    SBPreferences prefsIO = SBPreferences.getPreferencesFor(GUIOptions.class);
    String dir = prefsIO.get(GUIOptions.OPEN_DIR).toString();
    if (latexFile != null) {
      File out = new File(latexFile);
      if (SBFileFilter.createTeXFileFilter().accept(out)) {
        String path = out.getParent();
        if (!path.equals(dir)) {
          prefsIO.put(GUIOptions.OPEN_DIR, path);
        }
        if (!out.exists()) {
          long time = System.currentTimeMillis();
          logger.info(MESSAGES.getString("WRITING_LATEX_OUTPUT"));
          SBML2LaTeX.convert(sbmlIo.getSelectedModel(), out);
          logger.info(MessageFormat.format(
            MESSAGES.getString("DONE_IN_MS"), (System.currentTimeMillis() - time)) + "\n");
        }
      } else {
        logger.log(Level.WARNING, MessageFormat.format(
          WARNINGS.getString("INVALID_TEX_FILE"), latexFile) +"\n");
      }
    } else {
      logger.log(Level.WARNING, WARNINGS.getString("NO_TEX_FILE_PROVIDED"));
    }
  }
  
}
