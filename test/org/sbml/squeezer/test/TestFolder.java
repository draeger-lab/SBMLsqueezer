/*
 * $Id:  TestFolder.java 3:05:33 PM jpfeuffer$
 * $URL: TestFolder.java $
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

package org.sbml.squeezer.test;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.sbml.jsbml.SBMLInputConverter;
import org.sbml.jsbml.SBMLOutputConverter;
import org.sbml.squeezer.SBMLsqueezer;

import de.zbit.io.GeneralFileFilter;
import de.zbit.io.SBFileFilter;

/**
 * @author Julianus Pfeuffer
 * @version $Rev$
 * @since 1.4
 */

public class TestFolder {
	
	private static String foldername = "";
	private static final Logger logger = Logger.getLogger(SqueezerTests.class.getName());
	
	public static void main (String[] args){
		foldername = args[0];
		TraverseFolder traverser = new TraverseFolder();
		try {
			traverser.traverse(new File(foldername));
		} catch (FileNotFoundException e1) {
			logger.log(Level.WARNING, "Couldn't open a File.");
		}
		ArrayList<File> filesToCheck = traverser.getFiles();
		SBFileFilter[] filterArray = 
				{
				SBFileFilter.createSBMLFileFilterL1V1(),
				SBFileFilter.createSBMLFileFilterL1V2(),
				SBFileFilter.createSBMLFileFilterL2V1(),
				SBFileFilter.createSBMLFileFilterL2V2(),
				SBFileFilter.createSBMLFileFilterL2V3(),
				SBFileFilter.createSBMLFileFilterL2V4(),
				SBFileFilter.createSBMLFileFilterL3V1()
				};

		SBMLsqueezer squeezer  = SBMLsqueezer.initializeSqueezer();
		logger.info("Starting Tests...");
		for (SBFileFilter filter:filterArray){
			for(int i = 0; i< filesToCheck.size(); i++){
				File currentFile = filesToCheck.get(i);
				String currentFilename = currentFile.getName();
				if(filter.accept(currentFile)){
					File outputFile = new File(System.getProperty("user.home") + "/tests/" + currentFilename.substring(0,currentFilename.length()-4) +"_result.xml");
					try {
						outputFile.createNewFile();
					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
					String outputPath = outputFile.getAbsolutePath();
						try {
							logger.info("(Squeezing file): " + currentFile.getAbsolutePath());
							squeezer.squeeze(currentFile.getAbsolutePath(),outputPath);

						} catch (Throwable e) {
							logger.log(Level.WARNING, currentFile.getAbsolutePath(), e);
						}
				}
			}
		}
	}

}
