/*
 * $Id:  TestFolder.java 3:05:33 PM jpfeuffer$
 * $URL$
 * ---------------------------------------------------------------------
 * This file is part of SBMLsqueezer, a Java program that creates rate 
 * equations for reactions in SBML files (http://sbml.org).
 *
 * Copyright (C) 2006-2012 by the University of Tuebingen, Germany.
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
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.prefs.BackingStoreException;

import org.sbml.squeezer.SBMLsqueezer;
import org.sbml.squeezer.OptionsGeneral;
import org.sbml.squeezer.kinetics.OptionsRateLaws;

import de.zbit.io.filefilter.SBFileFilter;
import de.zbit.util.logging.LogUtil;
import de.zbit.util.prefs.SBPreferences;

/**
 * @author Julianus Pfeuffer
 * @version $Rev$
 * @since 1.4
 */
public class TestFolder extends Handler {

	private static String foldername = "";
	private static final Logger logger = Logger.getLogger(SqueezerTests.class
			.getName());

	public TestFolder(String[] args) throws BackingStoreException, IOException {


		//To set the properties of the log4j logger to specifiy the levels it logs,
		//where to set the output.
		String propertiespath = TestFolder.class.getResource(
				"data/log4j.properties").getPath();
		System.setProperty("log4j.configuration", "file:"+propertiespath);


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
		foldername = (args.length > 0) ? args[0] : TestFolder.class.getResource(
				"data/").getPath();

		// Looking up all Files in this folder and subfolders
		TraverseFolder traverser = new TraverseFolder();
		try {
			traverser.traverse(new File(foldername));
		} catch (FileNotFoundException e1) {
			logger.log(Level.WARNING, "Couldn't open a File.");
		}
		ArrayList<File> filesToCheck = traverser.getFiles();

		// Different Levels/Versions to test
		SBFileFilter[] filterArray = {
				// SBFileFilter.createSBMLFileFilterL1V1(),
				// SBFileFilter.createSBMLFileFilterL1V2(),
				// SBFileFilter.createSBMLFileFilterL2V1(), viele!
				// SBFileFilter.createSBMLFileFilterL2V2(),
				// SBFileFilter.createSBMLFileFilterL2V3(),
				SBFileFilter.createSBMLFileFilterL2V4()
				// SBFileFilter.createSBMLFileFilterL3V1() 
		};

		// Initializing squeezer
		//String[] arg = {"-Dlog4j.configuration=/home/user/myLog4j.properties"};
		SBMLsqueezer squeezer = new SBMLsqueezer();
		LogUtil.addHandler(this, "org.sbml");
		LogUtil.initializeLogging(Level.WARNING, "org.sbml");

		//directs all logs to a temporary file, that is deleted in the end
		File temp = File.createTempFile("SBMLSqueezerLoggerOutput", ".log"); 
		System.setOut(new PrintStream(temp));

		//Try for reversible and irreversible reactions!
		SBPreferences prefs = SBPreferences.getPreferencesFor(OptionsGeneral.class);
		prefs.put(OptionsRateLaws.TREAT_ALL_REACTIONS_REVERSIBLE,
				Boolean.valueOf(true));
		prefs.flush();

		logger.info("Starting Tests...");

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
						System.err.println("Processing:" + currentFilename);
						squeezer.squeeze(currentFile.getAbsolutePath(),
								outputPath);
					} catch (Throwable e) {
						logger.log(Level.SEVERE, currentFile.getAbsolutePath(), e);
						System.exit(1);
					}
				}
			}
		}

		//Cleanup
		temp.delete();

	}

	public static void main(String[] args) throws BackingStoreException, IOException {
		new TestFolder(args);
	}

	/* (non-Javadoc)
	 * @see java.util.logging.Handler#close()
	 */
	@Override
	public void close() throws SecurityException {
	}

	/* (non-Javadoc)
	 * @see java.util.logging.Handler#flush()
	 */
	@Override
	public void flush() {
	}

	/* (non-Javadoc)
	 * @see java.util.logging.Handler#publish(java.util.logging.LogRecord)
	 */
	@Override
	public void publish(LogRecord record) {
		if (record.getLevel().intValue() < Level.FINE.intValue()) {
			System.err.print(record.getMessage());
		}
	}

}
