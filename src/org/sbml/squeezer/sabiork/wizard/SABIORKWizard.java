/*
 * $Id: SABIORKWizard.java 971 2012-08-17 13:36:54Z keller$
 * $URL: https://rarepos.cs.uni-tuebingen.de/svn-path/SBMLsqueezer/trunk/src/org/sbml/squeezer/sabiork/wizard/SABIORKWizard.java$
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
package org.sbml.squeezer.sabiork.wizard;

import java.awt.Dialog.ModalityType;
import java.awt.Window;
import org.sbml.jsbml.SBMLDocument;
import org.sbml.squeezer.sabiork.wizard.console.ConsoleWizard;
import org.sbml.squeezer.sabiork.wizard.gui.JDialogWizard;


/**
 * The SABIORKWizard class allows easy access to the data provided by the
 * SABIO-RK database.
 * 
 * @author Matthias Rall
 * @version $Rev$
 */
public class SABIORKWizard {

	/**
	 * Starts the SABIO-RK wizard in GUI mode and returns the result of the
	 * wizard.
	 * 
	 * @param owner
	 *            the top-level window of the wizard
	 * @param modalityType
	 *            the modality type
	 * @param sbmlDocument
	 *            the {@link SBMLDocument}
	 * @return the resulting {@link SBMLDocument}
	 */
	public static SBMLDocument getResultGUI(Window owner,
			ModalityType modalityType, SBMLDocument sbmlDocument) {
		JDialogWizard dialogWizard = new JDialogWizard(owner, modalityType,
				sbmlDocument);
		dialogWizard.setVisible(true);
		return dialogWizard.getResult();
	}

	/**
	 * Starts the SABIO-RK wizard in console mode and returns the result of the
	 * wizard. If a filter option is set to {@code null} the default value
	 * of that filter option will be used.
	 * 
	 * @param sbmlDocument
	 *            the {@link SBMLDocument}
	 * @param reactionFilter
	 *            a filter that specifies which type of {@link Reaction} should
	 *            be considered. All reactions (1), reactions with a
	 *            {@link KineticLaw} (2), reactions without a {@link KineticLaw}
	 *            (3), reversible reactions (4), irreversible reactions (5),
	 *            fast reactions (6) and slow reactions (7)
	 * @param pathway
	 * @param tissue
	 * @param organism
	 * @param cellularLocation
	 * @param isWildtype
	 * @param isMutant
	 * @param isRecombinant
	 * @param hasKineticData
	 * @param lowerpHValue
	 * @param upperpHValue
	 * @param lowerTemperature
	 * @param upperTemperature
	 * @param isDirectSubmission
	 * @param isJournal
	 * @param isEntriesInsertedSince
	 * @param dateSubmitted
	 *            a date of the format dd/MM/yyyy
	 * @return the resulting {@link SBMLDocument}
	 */
	public static SBMLDocument getResultConsole(SBMLDocument sbmlDocument,
			Integer reactionFilter, String pathway, String tissue,
			String organism, String cellularLocation, Boolean isWildtype,
			Boolean isMutant, Boolean isRecombinant, Boolean hasKineticData,
			Double lowerpHValue, Double upperpHValue, Double lowerTemperature,
			Double upperTemperature, Boolean isDirectSubmission,
			Boolean isJournal, Boolean isEntriesInsertedSince,
			String dateSubmitted) {
		ConsoleWizard consoleWizard = new ConsoleWizard(sbmlDocument,
				reactionFilter, pathway, tissue, organism, cellularLocation,
				isWildtype, isMutant, isRecombinant, hasKineticData,
				lowerpHValue, upperpHValue, lowerTemperature, upperTemperature,
				isDirectSubmission, isJournal, isEntriesInsertedSince,
				dateSubmitted);
		return consoleWizard.getResult();
	}

}
