/*
 * $Id$
 * $URL$
 * ---------------------------------------------------------------------
 * This file is part of SBMLsqueezer, a Java program that creates rate 
 * equations for reactions in SBML files (http://sbml.org).
 *
 * Copyright (C) 2006-2013 by the University of Tuebingen, Germany.
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

import java.awt.Window;
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

import jp.sbi.garuda.platform.commons.exception.NetworkException;

import org.sbml.jsbml.Model;
import org.sbml.jsbml.SBMLException;
import org.sbml.jsbml.SBMLInputConverter;
import org.sbml.jsbml.SBMLOutputConverter;
import org.sbml.jsbml.util.IOProgressListener;
import org.sbml.jsbml.xml.libsbml.LibSBMLReader;
import org.sbml.jsbml.xml.libsbml.LibSBMLWriter;
import org.sbml.squeezer.gui.SBMLsqueezerUI;
import org.sbml.squeezer.io.IOOptions;
import org.sbml.squeezer.io.SBMLio;
import org.sbml.squeezer.io.SqSBMLReader;
import org.sbml.squeezer.io.SqSBMLWriter;
import org.sbml.squeezer.kinetics.OptionsRateLaws;
import org.sbml.squeezer.sabiork.SABIORKOptions;
import org.sbml.squeezer.util.Bundles;
import org.sbml.tolatex.LaTeXOptions;
import org.sbml.tolatex.SBML2LaTeX;
import org.sbml.tolatex.io.LaTeXOptionsIO;

import de.zbit.AppConf;
import de.zbit.Launcher;
import de.zbit.UserInterface;
import de.zbit.garuda.BackendNotInitializedException;
import de.zbit.garuda.GarudaOptions;
import de.zbit.garuda.GarudaSoftwareBackend;
import de.zbit.gui.GUIOptions;
import de.zbit.gui.GUITools;
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
 */
@SuppressWarnings("unchecked")
public class SBMLsqueezer extends Launcher implements IOProgressListener {
	
	/**
	 * 
	 */
  public static final transient ResourceBundle MESSAGES = ResourceManager.getBundle(Bundles.MESSAGES);
  /**
   * 
   */
  public static final transient ResourceBundle WARNINGS = ResourceManager.getBundle(Bundles.WARNINGS);
  
  /**
   * The {@link Logger} for this class.
   */
  private static final transient Logger logger = Logger.getLogger(SBMLsqueezer.class.getName());
	

  /**
   * 
   */
  private static SBMLInputConverter reader = null;

  /**
   * Generated serial version identifier.
   */
  private static final long serialVersionUID = 8751196023375780898L;
  
  /**
   * 
   */
  private static SBMLOutputConverter writer = null;
  
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
  	Class<? extends KeyProvider>[] list = new Class[4];
    list[0] = OptionsGeneral.class;
    list[1] = OptionsRateLaws.class;
    list[2] = SABIORKOptions.class;
    list[3] = LaTeXOptions.class;
    return list;
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
	   * Does initialization for creating a SBMLsqueezer Object.
	   * Checks if libSBML is available and initializes the Reader/Writer.
	   * @param tryLoadingLibSBML 
	   * @param reader
	   * @param writer
	   */
	  private static void initializeReaderAndWriter(boolean tryLoadingLibSBML){
	    boolean libSBMLAvailable = false;
	    if (tryLoadingLibSBML) {
	    	try {
	    		// In order to initialize libSBML, check the java.library.path.
	    		System.loadLibrary("sbmlj");
	    		// Extra check to be sure we have access to libSBML:
	    		Class.forName("org.sbml.libsbml.libsbml");
	    		logger.info(MESSAGES.getString("LOADING_LIBSBML"));
	    		libSBMLAvailable = true;
	    	} catch (Error e) {
	    	} catch (Throwable e) {
	    	}
	    }
	    if (libSBMLAvailable) {
	      reader = new LibSBMLReader();
	      writer = new LibSBMLWriter();
	    } else {
	      logger.info(MESSAGES.getString("LOADING_JSBML"));
	      reader = new SqSBMLReader();
	      writer = new SqSBMLWriter();
	    }
	  }

  /**
   * @param args
   */
  public static void main(String[] args) {
    new SBMLsqueezer(args);
  }
  
  /**
   * 
   */
  private SBMLio sbmlIo;

  /**
   * 
   */
  public SBMLsqueezer() {
    super();
  }

  /**
   * This constructor allows the integration of SBMLsqueezer into third-party
   * programs, i.e., as a CellDesigner plug-in.
   * 
   * @param sbmlReader
   * @param sbmlWriter
   */
  public SBMLsqueezer(SBMLInputConverter sbmlReader,
      SBMLOutputConverter sbmlWriter) {
  	this();
    sbmlIo = new SBMLio(sbmlReader, sbmlWriter);
    // sbmlIo.addIOProgressListener(this);
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
  public void commandLineMode(AppConf appConf) {
    SBProperties properties = appConf.getCmdArgs();
    if ((getSBMLIO().getNumErrors() > 0)
        && properties.getBoolean(OptionsGeneral.SHOW_SBML_WARNINGS)) {
      for (SBMLException exc : getSBMLIO().getWarnings()) {
        logger.log(Level.WARNING, exc.getMessage());
      }
    }
    if (properties.containsKey(IOOptions.SBML_OUT_FILE)) {
      try {
        squeeze(properties.get(IOOptions.SBML_IN_FILE).toString(),
          properties.get(IOOptions.SBML_OUT_FILE).toString());
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
  public List<Class<? extends KeyProvider>> getCmdLineOptions() {
    List<Class<? extends KeyProvider>> list = new ArrayList<Class<? extends KeyProvider>>(4);
    list.add(IOOptions.class);
    list.add(OptionsGeneral.class);
    list.add(OptionsRateLaws.class);
    list.add(GUIOptions.class);
    list.add(LaTeXOptions.class);
    list.add(GarudaOptions.class);
    return list;
  }


	/* (non-Javadoc)
   * @see de.zbit.Launcher#getInteractiveOptions()
   */
  public List<Class<? extends KeyProvider>> getInteractiveOptions() {
    return getInteractiveConfigOptions();
  }
  
  /* (non-Javadoc)
   * @see de.zbit.Launcher#getLogPackages()
   */
  public String[] getLogPackages() {
    return new String[] {"org.sbml", "de.zbit"};
  }
  
  /**
   * 
   * @return
   */
  public SBMLio getSBMLIO() {
    if (sbmlIo == null) {
      sbmlIo = new SBMLio(reader, writer);
    }
    return sbmlIo;
  }
  
  /* (non-Javadoc)
   * @see de.zbit.Launcher#getURLlicenseFile()
   */
  public URL getURLlicenseFile() {
    URL url = null;
    try {
      url = new URL("http://www.gnu.org/licenses/gpl-3.0-standalone.html");
    } catch (MalformedURLException exc) {
      logger.log(Level.FINER, exc.getLocalizedMessage(), exc);
    }
    return url;
  }
  
  /* (non-Javadoc)
   * @see de.zbit.Launcher#getURLOnlineUpdate()
   */
  public URL getURLOnlineUpdate() {
    URL url = null;
    try {
      url = new URL("http://www.cogsys.cs.uni-tuebingen.de/software/SBMLsqueezer/downloads/");
    } catch (MalformedURLException exc) {
      logger.log(Level.FINER, exc.getLocalizedMessage(), exc);
    }
    return url;
  }
  
  /* (non-Javadoc)
   * @see de.zbit.Launcher#getVersionNumber()
   */
  public String getVersionNumber() {
    return "2.0";
  }
  
  /* (non-Javadoc)
   * @see de.zbit.Launcher#getYearOfProgramRelease()
   */
  public short getYearOfProgramRelease() {
    return (short) 2013;
  }
  
  /* (non-Javadoc)
   * @see de.zbit.Launcher#getYearWhenProjectWasStarted()
   */
  public short getYearWhenProjectWasStarted() {
    return (short) 2006;
  }
  
  /* (non-Javadoc)
   * @see de.zbit.Launcher#initGUI(de.zbit.AppConf)
   */
  public Window initGUI(AppConf appConf) {
    SBProperties properties = appConf.getCmdArgs();
    if (properties.containsKey(IOOptions.SBML_IN_FILE)) {
      readSBMLSource(properties.get(IOOptions.SBML_IN_FILE));
    }
    final Window gui = new SBMLsqueezerUI(getSBMLIO(), appConf);
		if (getCmdLineOptions().contains(GarudaOptions.class)
				&& (!appConf.getCmdArgs().containsKey(GarudaOptions.CONNECT_TO_GARUDA) ||
						appConf.getCmdArgs().getBoolean(GarudaOptions.CONNECT_TO_GARUDA))) {
    	new Thread(new Runnable() {
    		/* (non-Javadoc)
    		 * @see java.lang.Runnable#run()
    		 */
    		public void run() {
    			try {
    				String localPath = SBMLsqueezer.class.getProtectionDomain().getCodeSource().getLocation().getPath();
            String folder = new File(localPath).getParent() + "/resources/org/sbml/squeezer/resources/img/";
    				String icon = folder + "SBMLsqueezerIcon_64.png";
    				                				
    				GarudaSoftwareBackend garudaBackend = new GarudaSoftwareBackend(
    					"dd624b40-7bc0-11e2-b92a-0800200c9a66",
    					(UserInterface) gui,
    					icon,
    					MESSAGES.getString("PROGRAM_DESCRIPTION"),
    					Arrays.asList(MESSAGES.getStringArray("KEYWORDS")),
							Arrays.asList(new String[] { 
									folder + "Screenshot_1.png",
									folder + "Screenshot_2.png",
									folder + "Screenshot_3.png"})
    				);
    				garudaBackend.addInputFileFormat("xml", "SBML");
    				garudaBackend.addInputFileFormat("sbml", "SBML");
    				garudaBackend.addOutputFileFormat("xml", "SBML");
    				garudaBackend.addOutputFileFormat("sbml", "SBML");
    				garudaBackend.init();
    				garudaBackend.registedSoftwareToGaruda();
    			} catch (NetworkException exc) {
    				GUITools.showErrorMessage(gui, exc);
    			} catch (BackendNotInitializedException exc) {
    				GUITools.showErrorMessage(gui, exc);
    			} catch (Throwable exc) {
    				logger.fine(exc.getLocalizedMessage());
    			}
    		}
    	}).start();
    }
    return gui;
  }
  
  /* (non-Javadoc)
   * @see org.sbml.jsbml.util.IOProgressListener#ioProgressOn(java.lang.Object)
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
  public Model readSBMLSource(Object sbmlSource) {
  	Model model = null;
    long time = System.currentTimeMillis();
    logger.info(MESSAGES.getString("READING_SBML_FILE"));
    try {
      model = getSBMLIO().convertModel(sbmlSource);
      logger.info(MessageFormat.format(MESSAGES.getString("DONE_IN_MS"), (System.currentTimeMillis() - time)));
    } catch (Exception exc) {
      logger.log(Level.WARNING, String.format(WARNINGS.getString("CANT_READ_MODEL"), exc.getLocalizedMessage()));
    }
    return model;
  }
  
  /* (non-Javadoc)
   * @see de.zbit.Launcher#setUp()
   */
  @Override
  protected void setUp() {
  	if ((reader == null) && (writer == null)) {
  		initializeReaderAndWriter(getAppConf().getCmdArgs().getBooleanProperty(
  			IOOptions.TRY_LOADING_LIBSBML));
  	}
  }

  /**
   * 
   * @param source
   * @param outFile
   * @param showProgress
   * @throws Throwable
   */
  public void squeeze(File source, File outFile, boolean showProgress) throws Throwable {
  	long workTime = System.currentTimeMillis();
  	readSBMLSource(source.getAbsolutePath());
  	boolean errorFatal = false;
  	SBMLException exception = null;
  	for (SBMLException exc : sbmlIo.getWarnings()) {
  		if (exc.isFatal() || exc.isXML() || exc.isError()) {
  			errorFatal = true;
  			exception = exc;
  		}
  	}
  	if (errorFatal) {
  		throw new SBMLException(exception);
  	} else if (!sbmlIo.getListOfOpenedFiles().isEmpty()) {
  		
  		KineticLawGenerator klg = new KineticLawGenerator(sbmlIo.getSelectedModel());
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
  		sbmlIo.saveChanges(this);
  		if ((outFile != null)
  				&& (SBFileFilter.createSBMLFileFilter().accept(outFile))) {
  			sbmlIo.writeSelectedModelToSBML(outFile.getAbsolutePath());
  			logger.info(MessageFormat.format(MESSAGES.getString("DONE_IN_MS"), (System.currentTimeMillis() - time)));
  			SBPreferences preferences = new SBPreferences(OptionsGeneral.class);
  			if (preferences.getBoolean(OptionsGeneral.SHOW_SBML_WARNINGS)) {
  				for (SBMLException exc : sbmlIo.getWriteWarnings()) {
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
   * @throws Throwable
   */
  public void squeeze(Object sbmlSource, String outfile) throws Throwable {
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
  				squeeze(inFile, outFile, false);
  			} catch (Throwable t) {
  				logger.log(Level.SEVERE, MessageFormat.format(
  					WARNINGS.getString("SQUEEZE_ERROR"),
  					entry.getKey().getAbsolutePath(), entry.getValue()), t);
  			}
  		}
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
