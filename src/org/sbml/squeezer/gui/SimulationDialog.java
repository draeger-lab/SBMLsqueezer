/*
 *  SBMLsqueezer creates rate equations for reactions in SBML files
 *  (http://sbml.org).
 *  Copyright (C) 2009 ZBIT, University of Tübingen, Andreas Dräger
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.sbml.squeezer.gui;

import java.awt.Frame;
import java.io.IOException;
import java.util.Properties;

import javax.swing.JDialog;

import org.sbml.jsbml.Model;

/**
 * @author Andreas Dr&auml;ger
 * @since 1.4
 * @date 2010-04-15
 * 
 */
public class SimulationDialog extends JDialog {

	/**
	 * Generated serial version identifier
	 */
	private static final long serialVersionUID = -5289766427756813972L;
	/**
	 * GUI element that lets the user run the simulation.
	 */
	private SimulationPanel simPanel;

	/**
	 * 
	 * @param owner
	 * @param model
	 * @param settings
	 */
	public SimulationDialog(Frame owner, Model model, Properties settings) {
		super(owner, "Simulation of model " + model.toString());
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		simPanel = new SimulationPanel(model, settings);
		getContentPane().add(simPanel);
		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		pack();
		int maxSize = 700;
		if (getWidth() > 1.5 * maxSize)
			this.setSize((int) Math.round(1.5 * maxSize), getHeight());
		if (getHeight() > maxSize)
			this.setSize(getWidth(), maxSize);
		setLocationRelativeTo(owner);
	}
	
	/**
	 * 
	 * @return
	 */
	public Properties getProperties() {
		return simPanel.getProperties();
	}

	/**
	 * 
	 * @param path
	 * @throws IOException
	 */
	public void openExperimentalData(String path) throws IOException {
		this.simPanel.openExperimentalData(path);
	}
}
