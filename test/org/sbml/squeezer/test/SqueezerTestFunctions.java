package org.sbml.squeezer.test;

import java.io.File;
import java.util.List;

import de.zbit.io.SBFileFilter;


/**
 * Additional functions for the test cases
 * 
 * @author Sarah R. M&uuml;ller vom Hagen
 */
public class SqueezerTestFunctions {
	
	/**
	 * 
	 * @param dir			the directory or file
	 * @param listOfFiles	the list in which the files are to be safed
	 */
	public static void getAllXMLFiles(String dir, List<File> listOfFiles){
		File f = new File(dir);
		if(f.isFile()){
			if(SBFileFilter.isSBMLFile(f) && !(f.getName().contains("sedml"))) {
				listOfFiles.add(f);
			}
		}else if(f.isDirectory()){
			File[] arrayOfFiles = new File(dir).listFiles();
			for(File file: arrayOfFiles){
				getAllXMLFiles(file.getAbsolutePath(), listOfFiles);			
			}
		}
	}	
}
