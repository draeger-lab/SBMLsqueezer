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

package org.sbml.squeezer.gui.wizard;

import java.awt.BorderLayout;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JPanel;
import javax.swing.JProgressBar;

import org.sbml.squeezer.KineticLawGenerator;
import org.sbml.squeezer.SqueezerOptions;
import org.sbml.squeezer.util.Bundles;

import de.zbit.util.ResourceManager;
import de.zbit.util.prefs.SBPreferences;
import de.zbit.util.progressbar.gui.ProgressBarSwing;

/**
 * 
 * @author Sebastian Nagel
 * @version $Rev$
 * @since 1.4
 */
public class KineticLawSelectionEquationProgressPanel extends JPanel {
	public static final transient ResourceBundle MESSAGES = ResourceManager.getBundle(Bundles.MESSAGES);
	public static final transient ResourceBundle LABELS = ResourceManager.getBundle(Bundles.LABELS);

	/**
	 * 
	 */
	private static final long serialVersionUID = 2381499189035630841L;
	
	private final Logger logger = Logger.getLogger(KineticLawSelectionEquationPanelDescriptor.class.getName());
	
	private KineticLawGenerator klg;
	
	private SBPreferences prefs;

	private ProgressBarSwing progressBar;
	
	
	
	/**
	 * 
	 * @param sbmlIO
	 */
	public KineticLawSelectionEquationProgressPanel(KineticLawGenerator klg) {
		super(new BorderLayout());
		
		this.prefs = new SBPreferences(SqueezerOptions.class);
		this.klg = klg;
		
		init();
	}



	private void init() {
		JProgressBar jProgressBar = new JProgressBar();
		jProgressBar.setSize(jProgressBar.getSize().width,30);
		progressBar = new ProgressBarSwing(jProgressBar);
		add(jProgressBar, BorderLayout.CENTER);
	}

	public void generateKineticLaw() {
		try {
			prefs.flush();
			
			klg.setProgressBar(progressBar);
			
			klg.generateLaws();

			logger.log(Level.INFO, LABELS.getString("READY"));
			firePropertyChange("generateKineticLawDone", null, null);
			
		} catch (Throwable exc) {
			
		}
	}
}
