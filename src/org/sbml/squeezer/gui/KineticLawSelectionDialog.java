/*
 * $Id$
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
package org.sbml.squeezer.gui;

import java.awt.Frame;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JDialog;
import javax.swing.JFrame;

import org.sbml.squeezer.SqueezerOptions;
import org.sbml.squeezer.io.SBMLio;
import org.sbml.squeezer.util.Bundles;

import de.zbit.gui.StatusBar;
import de.zbit.util.AbstractProgressBar;
import de.zbit.util.prefs.SBPreferences;

/**
 * This is the main GUI class.
 * 
 * @author Nadine Hassis
 * @author Andreas Dr&auml;ger
 * @author Hannes Borch
 * @author Sebastian Nagel
 * @date Aug 3, 2007
 * @since 1.0
 * @version $Rev$
 */
public class KineticLawSelectionDialog extends JDialog{

	/**
	 * Generated serial version id.
	 */
	private static final long serialVersionUID = -5980678130366530716L;

	// UI ELEMENTS DEFINITION: ReactionFrame
	private boolean KineticsAndParametersStoredInSBML = false;
	
	/**
	 * 
	 */
	private final Logger logger = Logger.getLogger(KineticLawSelectionDialog.class.getName());
	
	SBPreferences prefs;

	private StatusBar statusBar;


	/**
	 * Creates an empty dialog with the given settings and sbml io object.
	 * 
	 * @param owner
	 * @param progressListener 
	 */
	private KineticLawSelectionDialog(Frame owner) {
		super(owner, Bundles.MESSAGES.getString("SBMLSQUEEZER"), true);
		// if (owner == null)
		// setIconImage(GUITools.ICON_LEMON);
		this.prefs = new SBPreferences(SqueezerOptions.class);
		// setAlwaysOnTop(true);
	}

	/**
	 * This constructor is necessary for the GUI to generate just one single
	 * rate equation for the given reaction.
	 * 
	 * @param sbmlIO
	 * @param reaction
	 */
	public KineticLawSelectionDialog(Frame owner, SBMLio sbmlIO, String reactionID) {
		this(owner);
		
		try {
			// This thing is necessary for CellDesigner!
			final KineticLawWindowAdapter adapter = new KineticLawWindowAdapter(this,
					sbmlIO, reactionID);
			
			pack();
			setResizable(false);
			setLocationRelativeTo(owner);
			setVisible(true);
			
			// get new statusbar and limit the log message length
			statusBar = StatusBar.addStatusBar((JFrame) this.getOwner());
			statusBar.limitLogMessageLength(this.getWidth()-130);

			AbstractProgressBar progressBar = statusBar.showProgress();
			adapter.showProgress(progressBar);
			KineticsAndParametersStoredInSBML = adapter.isKineticsAndParametersStoredInSBML();
			dispose();
			statusBar.hideProgress();
			statusBar.unsetLogMessageLimit();
			logger.log(Level.INFO, Bundles.LABELS.getString("READY"));
			
		} catch (Throwable exc) {
			GUITools.showErrorMessage(this, exc);
		}
	}

	

	/**
	 * Method that indicates whether or not changes have been introduced into
	 * the given model.
	 * 
	 * @return True if kinetic equations and parameters or anything else were
	 *         changed by SBMLsqueezer.
	 */
	public boolean isKineticsAndParametersStoredInSBML() {
		return KineticsAndParametersStoredInSBML;
	}

}
