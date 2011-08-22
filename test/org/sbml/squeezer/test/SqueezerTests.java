package org.sbml.squeezer.test;


import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import junit.framework.TestCase;

import org.junit.Test;
import org.sbml.jsbml.Model;
import org.sbml.jsbml.Reaction;
import org.sbml.jsbml.SBMLException;
import org.sbml.jsbml.SBMLInputConverter;
import org.sbml.jsbml.SBMLOutputConverter;
import org.sbml.jsbml.xml.libsbml.LibSBMLReader;
import org.sbml.jsbml.xml.libsbml.LibSBMLWriter;
import org.sbml.squeezer.KineticLawGenerator;
import org.sbml.squeezer.SBMLsqueezer;
import org.sbml.squeezer.SqueezerOptions;
import org.sbml.squeezer.io.SBMLio;
import org.sbml.squeezer.io.SqSBMLReader;
import org.sbml.squeezer.io.SqSBMLWriter;

import de.zbit.io.SBFileFilter;
import de.zbit.util.prefs.SBPreferences;

public class SqueezerTests extends TestCase{

	private String testPath = System.getProperty("user.dir") + "/files/tests/SBML_test_cases/cases/semantic/001-100/00001";

	//String testPath = System.getProperty("user.dir") + "/files/tests/sbml-test-cases-2011-06-15";
	//private String testPath = System.getProperty("user.home") + "/workspace/SBMLsimulatorCore/files/SBML_test_cases/cases/semantic/00001";
	/**
	 * List of test files
	 */
	private List<File> listOfFiles;
	/**
	 * List of all models
	 */
	private Model currentModel;
	
	
	private KineticLawGenerator klg;

	private static final Logger logger = Logger.getLogger(SqueezerTests.class.getName());

	public SqueezerTests(){
		long time = System.currentTimeMillis();
		logger.info("search for files...");
		List<File> tempList = new LinkedList<File>();
		tempList.add(new File(testPath));
		listOfFiles = new LinkedList<File>();
		File f = null;
		while (!tempList.isEmpty()) {
			f = tempList.remove(0);
			if (f.isFile()) {
				if (SBFileFilter.isSBMLFile(f) && !(f.getName().contains("sedml"))) {
					listOfFiles.add(f);
				}
			} else if (f.isDirectory()) {
				tempList.addAll(Arrays.asList(f.listFiles()));
			}
		}
		logger.info("    done in " + (System.currentTimeMillis() - time) + " ms.");
	}

	/**
	 * test if the given directory contains XML Files, if not, then the 
	 * results of the import and export tests can be ignored
	 */
	@Test
	public void testDirectoryForXMLFiles(){
		long time = System.currentTimeMillis();
		logger.info("test if there are files in the given directory...");
		int numberOfFiles = listOfFiles.size();
		assertTrue(numberOfFiles > 0);	
		logger.info("    done in " + (System.currentTimeMillis() - time) + " ms.");
	}

	/**
	 * - tests the files can be imported as models
	 * - 
	 */
	@Test
	public void testModels() {
		long time = System.currentTimeMillis();
		logger.info("Generate SBMLreader and SBMLwriter.");
		boolean libSBMLAvailable = false;
		try {
			// In order to initialize libSBML, check the java.library.path.
			System.loadLibrary("sbmlj");
			// Extra check to be sure we have access to libSBML:
			Class.forName("org.sbml.libsbml.libsbml");
			libSBMLAvailable = true;
		} catch (Error e) {
		} catch (Throwable e) {
		} 
		SBMLInputConverter reader = null;
		SBMLOutputConverter writer = null;
		if (!libSBMLAvailable) {
			reader = new SqSBMLReader() ;
			writer = new SqSBMLWriter() ;
		} else {
			reader = new LibSBMLReader();
			writer = new LibSBMLWriter();
		}
		logger.info("    done in " + (System.currentTimeMillis() - time) + " ms.");



		time = System.currentTimeMillis();
		logger.info("Generate SBMLio and SBMLsqueezerUI.");
		SBMLio io = new SBMLio(reader,writer);
		logger.info("Test file import and model conversion.");
		boolean failed = false;
		Model miniModel;
		Model newModel; // model after saving ind importing the old model
		SBMLsqueezer squeezer;
		String safePath = testPath;
		
		for(int i=0; i<listOfFiles.size(); i++){
			// try to extract models from files
			File f = listOfFiles.get(i);
			try {
				logger.info("(file to model): " + f.getAbsolutePath());
				currentModel = io.convertModel(f.getAbsolutePath());
			} catch (Throwable e) {
				logger.log(Level.WARNING, "failed to convert Model: ");
				logger.log(Level.WARNING, f.getAbsolutePath(), e);
				failed = true; // other tests on model cannot be performed
				fail();
			}
			if(!failed){
				// try to generate kinetic laws for a model
				try {
					logger.info("(KineticLawGenerator for a model): " + f.getAbsolutePath());
					klg = new KineticLawGenerator(currentModel);
				} catch (Throwable e) {
					logger.log(Level.WARNING, "failed to generate kinetic equations: ");
					logger.log(Level.WARNING, f.getAbsolutePath(), e);
					failed = true; // the following tests would also fail
					fail();
				}
			}
			if(!failed){
				// try to generate kinetic laws for the reactions
				logger.info("(KineticLawGenerator for reaction)"+ f.getAbsolutePath());
				for(Reaction reac : currentModel.getListOfReactions()){
					try {
						logger.info("    (Reaction): "+reac.getId());
						new KineticLawGenerator(currentModel,reac.getId());
					} catch (Exception e) {
						logger.log(Level.WARNING, "failed to generate kinetic equation for reaction: ");
						logger.log(Level.WARNING, "file: "+f.getAbsolutePath());
						logger.log(Level.WARNING, "id: "+reac.getId(), e);
						fail();
					}
				}	
			}
			if(!failed){
				// try to get the miniModels
				try {
					logger.info("(get MiniModel for a model): " + f.getAbsolutePath());
					miniModel = klg.getMiniModel();
				} catch (Throwable e) {
					logger.log(Level.WARNING, "failed to generate the MiniModel: ");
					logger.log(Level.WARNING, f.getAbsolutePath(), e);
					fail();
				}
			}
			
			if(!failed){
				logger.info("(store kinetic laws): " + f.getAbsolutePath());
				try {
					squeezer = new SBMLsqueezer(reader, writer);
					klg.storeKineticLaws(squeezer);
				} catch (Exception e) {
					logger.log(Level.WARNING, "failed to store kinetic laws: ");
					logger.log(Level.WARNING, f.getAbsolutePath(), e);
					failed = true;
					fail();
				}
			}

			if(!failed){
				// try to safe the model in the folder given by testPath
				logger.info("(write model to file): " + f.getAbsolutePath());
				File testFile = new File(safePath);
				if(testFile.isDirectory()){
					safePath += "/test.xml";
				}else if(testFile.isFile()){
					safePath = testFile.getParent() + "/test.xml";
				}
				try {
					io.writeSelectedModelToSBML(safePath);
				} catch (Exception e) {
					logger.log(Level.WARNING, "failed to write model to file: ");
					logger.log(Level.WARNING, f.getAbsolutePath(), e);
					failed = true;
					fail();
				}
			}
			
			if(!failed){
				// try to compare the new model (after saving) and the old model (before saving)
				logger.info("(compare models): " + f.getAbsolutePath());
				File fnew = new File(safePath);
				try {
					newModel = io.convertModel(fnew.getAbsolutePath());
					assertTrue(currentModel.equals(newModel));
				} catch (Throwable e) {
					logger.log(Level.WARNING, "failed to compare models.");
					logger.log(Level.WARNING, f.getAbsolutePath(), e);
					failed = true; // other tests on model cannot be performed
					fail();
				}
			}

		}
		logger.info("    done in " + (System.currentTimeMillis() - time) + " ms.");


		/*
		//time = System.currentTimeMillis();
		logger.info("test squeezer.");
		for(File file : listOfFiles) {
			// test models
			try {
				logger.info("(squeeze file): " + file.getAbsolutePath());
				squeezer.squeeze(file.getAbsolutePath(), System.getProperty("user.home") + "/test.xml");

			} catch (Throwable e) {
				logger.log(Level.WARNING, file.getAbsolutePath(), e);
				fail();
			}
		}
		//logger.info("    done in " + (System.currentTimeMillis() - time) + " ms.");
		 */

	}

	/**
	 * test, if the libSBML is available.
	 * Note that a failure of this test does not lead to any problems
	 * as the functional components of this library are completely
	 * substituted. 
	 */
	@Test
	public void testLibSBML() {

		boolean libSBMLAvailable = false;
		try {
			// In order to initialize libSBML, check the java.library.path.
			System.loadLibrary("sbmlj");
			// Extra check to be sure we have access to libSBML:
			Class.forName("org.sbml.libsbml.libsbml");
			libSBMLAvailable = true;
		} catch (Error e) {
		} catch (Throwable e) {
		} 
		assertTrue(!libSBMLAvailable);
	}

	/**
	 * test if the program can be started with default settings
	 * @throws MalformedURLException 
	 */
	@Test
	public void testProgramStart() {

		try {
			SBMLsqueezer.main(new String[]{});
		} catch (MalformedURLException e) {
			e.printStackTrace();
			fail();
		}

	}

	public void testPreferences() {

		long time = System.currentTimeMillis();
		logger.info("test default settings of SBPreferences");

		SBPreferences preferences = new SBPreferences(SqueezerOptions.class);
		try {
			preferences.checkPrefs();
		} catch (Exception e) {
			logger.log(Level.WARNING, "Error in the preferences settings: ", e);
			fail();
		}
		logger.info("    done in " + (System.currentTimeMillis() - time) + " ms.");

	}


}
