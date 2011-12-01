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
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.PreferencesFactory;

import org.sbml.squeezer.SBMLsqueezer;

import de.zbit.io.OpenFile;
import de.zbit.io.SBFileFilter;
import de.zbit.util.prefs.Option;
import de.zbit.util.prefs.SBPreferences;

/**
 * @author Julianus Pfeuffer
 * @version $Rev$
 * @since 1.4
 */

public class TestFolder {

	private static String foldername = "";
	private static final Logger logger = Logger.getLogger(SqueezerTests.class
			.getName());

	public static void main(String[] args) {
		
		// Creating output folder at /workspace/SBMLSqueezer/files/tests/tmp
		File tmpDir = new File(System.getProperty("user.dir")
				+ "/files/tests/tmp");
		if (!tmpDir.exists()) {
			tmpDir.mkdir();
		}
		
		//Writing log-file
		try {
		    
		    FileHandler handler = new FileHandler(tmpDir.getAbsolutePath()
					+ '/' + "tests.log");
		    logger.addHandler(handler);
		} catch (IOException e) {
		}

		// Test data to be put in org.sbml.squeezer.test.data
		//foldername = TestFolder.class.getResource("data/").getPath();
		foldername = args[0];

		// Looking up all Files in this folder and subfolders
		TraverseFolder traverser = new TraverseFolder();
		try {
			traverser.traverse(new File(foldername));
		} catch (FileNotFoundException e1) {
			logger.log(Level.WARNING, "Couldn't open a File.");
		}
		ArrayList<File> filesToCheck = traverser.getFiles();

		// Different Levels/Versions to test
		SBFileFilter[] filterArray = { SBFileFilter.createSBMLFileFilterL1V1(),
				SBFileFilter.createSBMLFileFilterL1V2(),
				SBFileFilter.createSBMLFileFilterL2V1(),
				SBFileFilter.createSBMLFileFilterL2V2(),
				SBFileFilter.createSBMLFileFilterL2V3(),
				SBFileFilter.createSBMLFileFilterL2V4(),
				SBFileFilter.createSBMLFileFilterL3V1() };

		// Initializing squeezer
		SBMLsqueezer squeezer = new SBMLsqueezer();
		
		//Shouldn't I edit the preferences?
		/*SBPreferences prefs = SBPreferences.getPreferencesFor(SqueezerOptions.class);
		prefs.put(myoption, true);*/
		logger.info("Starting Tests...");

		ArrayList<String> failures = new ArrayList<String>();
		// iterating over all Levels/Versions and files and squeezing them
		for (SBFileFilter filter : filterArray) {
			for (int i = 0; i < filesToCheck.size(); i++) {
				File currentFile = filesToCheck.get(i);
				String currentFilename = currentFile.getName();
				if (filter.accept(currentFile)) {
					String outputPath = tmpDir.getAbsolutePath()
							+ '/'
							+ currentFilename.substring(0,
									currentFilename.lastIndexOf('.'))
							+ "_result.xml";
					try {
						logger.info(String.format("Squeezing file: %s",
								currentFile.getAbsolutePath()));
						squeezer.squeeze(currentFile.getAbsolutePath(),
								outputPath);
					} catch (Throwable e) {
						logger.log(Level.WARNING,
								currentFile.getAbsolutePath(), e);
						failures.add(currentFile.getAbsolutePath());
					}
				}
			}
		}

		for (String path : failures) {
			System.out.println(path);
		}
	}

}
