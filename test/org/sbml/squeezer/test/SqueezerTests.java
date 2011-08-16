package org.sbml.squeezer.test;


import java.io.File;
import java.net.MalformedURLException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

import org.sbml.jsbml.Model;
import org.sbml.jsbml.SBMLInputConverter;
import org.sbml.jsbml.SBMLOutputConverter;
import org.sbml.jsbml.xml.libsbml.LibSBMLReader;
import org.sbml.jsbml.xml.libsbml.LibSBMLWriter;
import org.sbml.squeezer.SBMLsqueezer;
import org.sbml.squeezer.gui.SBMLsqueezerUI;
import org.sbml.squeezer.io.SBMLio;
import org.sbml.squeezer.io.SqSBMLReader;
import org.sbml.squeezer.io.SqSBMLWriter;

import de.zbit.io.SBFileFilter;

import junit.framework.TestCase;

public class SqueezerTests extends TestCase{
	String testPath = System.getProperty("user.dir") + "/files/tests/sbml-test-cases-2011-06-15/cases/semantic/00001";
	//String testPath = System.getProperty("user.dir") + "/files/tests";
	File testDirectory;
	List<File> listOfFiles;
	
	private static final Logger logger = Logger.getLogger(SqueezerTests.class.getName());
	
	public SqueezerTests(){
		testDirectory = new File(testPath);
		File[] arrayOfFiles = testDirectory.listFiles();
		listOfFiles = new LinkedList<File>();
		
		for(int i=0; i < arrayOfFiles.length; i++){
			if(SBFileFilter.isSBMLFile(arrayOfFiles[i]) && !(arrayOfFiles[i].getName().contains("sedml"))) {
				listOfFiles.add(arrayOfFiles[i]);
			}
		}
	}
	
	/**
	 * test if the given directory contains XML Files, if not, then the 
	 * results of the import and export tests can be ignored
	 */
	public void testDirectoryForXMLFiles(){
		int numberOfFiles = listOfFiles.size();
		assertTrue(numberOfFiles > 0);	
	}
	
	/**
	 * tests if the test directory can be found and contains xml files
	 */
	public void testFileImport() {		
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
		for(int i=0; i < listOfFiles.size(); i++){
			if(listOfFiles.get(i).isFile()){
				try {
					logger.info("import file " + listOfFiles.get(i).getName());
					Model model = io.convertModel(listOfFiles.get(i).getAbsolutePath());
				} catch (Exception e) {
					fail();
					logger.warning("failed to import file " + listOfFiles.get(i).getName());
				}
			}
			//else if(listOfFiles[i].isDirectory()){}
		}
	}
	
	/**
	 * test, if the libSBML is available.
	 * Note that a failure of this test does not lead to any problems
	 * as the functional components of this library are completely
	 * substituted. 
	 */
	public void testLibSBML() {
		boolean libSBMLAvailable = false;
		try {
			// In order to initialize libSBML, check the java.library.path.
			System.loadLibrary("sbmlj");
			// Extra check to be sure we have access to libSBML:
			Class.forName("org.sbml.libsbml.libsbml");
			libSBMLAvailable = true;
		} catch (Error e) {
			
			fail();
		} catch (Throwable e) {
			fail();
		} 
		assertTrue(libSBMLAvailable);
	}
	
	/**
	 * test if the program can be started with default settings
	 * @throws MalformedURLException 
	 */
	public void testProgramStart() {
		try {
			SBMLsqueezer.main(new String[]{});
		} catch (MalformedURLException e) {
			e.printStackTrace();
			fail();
		}
	}
	
	
}
