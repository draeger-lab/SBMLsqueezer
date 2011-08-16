package org.sbml.squeezer.test;


import java.io.File;
import java.net.MalformedURLException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import junit.framework.TestCase;

import org.junit.Test;
import org.sbml.jsbml.Model;
import org.sbml.jsbml.SBMLInputConverter;
import org.sbml.jsbml.SBMLOutputConverter;
import org.sbml.jsbml.xml.libsbml.LibSBMLReader;
import org.sbml.jsbml.xml.libsbml.LibSBMLWriter;
import org.sbml.squeezer.SBMLsqueezer;
import org.sbml.squeezer.io.SBMLio;
import org.sbml.squeezer.io.SqSBMLReader;
import org.sbml.squeezer.io.SqSBMLWriter;

import de.zbit.io.SBFileFilter;

public class SqueezerTests extends TestCase{

  //String testPath = System.getProperty("user.dir") + "/files/tests/sbml-test-cases-2011-06-15/cases/semantic/00001";
	//String testPath = System.getProperty("user.dir") + "/files/tests/sbml-test-cases-2011-06-15";
  private String testPath = System.getProperty("user.home") + "/workspace/SBMLsimulatorCore/files/SBML_test_cases";
	/**
	 * List of test files
	 */
	private List<File> listOfFiles;
	/**
	 * List of all models
	 */
	private List<Model> listOfModels = new LinkedList<Model>();
	
	private static final Logger logger = Logger.getLogger(SqueezerTests.class.getName());
	
	public SqueezerTests(){
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
	}
	
	/**
	 * test if the given directory contains XML Files, if not, then the 
	 * results of the import and export tests can be ignored
	 */
	@Test
	public void testDirectoryForXMLFiles(){
		int numberOfFiles = listOfFiles.size();
		assertTrue(numberOfFiles > 0);	
	}
	
	/**
	 * - tests the files can be imported as models
	 * - 
	 */
	@Test
	public void testModels() {		
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
		
		logger.info("Generate SBMLio and SBMLsqueezerUI.");
		SBMLio io = new SBMLio(reader,writer);
		
		
		logger.info("test file import.");
		SBMLsqueezer squeezer = new SBMLsqueezer(reader, writer);
		for(File file : listOfFiles) {
			// test file import
			try {
			  logger.info("file: " + file.getAbsolutePath());
        squeezer.squeeze(file.getAbsolutePath(), System.getProperty("user.home") + "/test.xml");
      } catch (Throwable e) {
        logger.log(Level.WARNING, file.getAbsolutePath(), e);
        fail();
      }
		}		
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
		assertTrue(libSBMLAvailable);
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
	
	
}
