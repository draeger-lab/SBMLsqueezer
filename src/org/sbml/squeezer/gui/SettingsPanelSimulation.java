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

import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;

/**
 * @author Andreas Dr&auml;ger
 * @since 1.4
 * @date 2010-04-13
 */
public class SettingsPanelSimulation extends JPanel implements ActionListener {

	/**
	 * Generated serial version identifier
	 */
	private static final long serialVersionUID = -618135419544428464L;
	private String[] availableSolvers = new String[] { "bla bla" };
	private String[] availableDistances = new String[] { "bli bli" };
	private double t1 = 0;
	private double t2 = 5;
	private int stepsPerUnitTime = 5;
	private int numSteps = 10;
	private double maxTime = 100;
	private String openDir;
	private String saveDir;
	private char quoteChar;
	private double parameterStepSize;
	private double maxCompartmentVal;
	private double spinnerMaxVal = 10000;
	private double maxSpeciesVal;
	private double maxParameterVal;
	private Icon showLegend;
	private Icon showGrid;
	private Icon logScale;

	/**
	 * 
	 * @author Andreas Dr&auml;ger
	 * @since 1.4
	 * @date 2010-04-13
	 * 
	 */
	public static enum Command {
		/**
		 * Command to open a directory as default open directory.
		 */
		OPEN_DIR,
		/**
		 * Comand to open a directory for saving files.
		 */
		SAVE_DIR
	}

	/**
	 * 
	 */
	public SettingsPanelSimulation() {
		super(new GridBagLayout());
		LayoutHelper lh = new LayoutHelper(this);
		lh.add(computingPanel(), 0, 0, 1, 1, 0, 0);
		lh.add(new JPanel(), 0, 1, 1, 1, 0, 0);
		lh.add(plotPanel(), 0, 2, 1, 1, 0, 0);
		lh.add(new JPanel(), 1, 0, 1, 1, 0, 0);
		lh.add(parsingPanel(), 2, 0, 1, 1, 0, 0);
		lh.add(scanPanel(), 2, 2, 1, 1, 0, 0);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent e) {
		switch (Command.valueOf(e.getActionCommand())) {
		case OPEN_DIR:
			openDir = selectOpenDir(openDir);
			break;

		default:
			break;
		}
	}

	/**
	 * 
	 * @param openDir
	 * @return
	 */
	private String selectOpenDir(String openDir) {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * 
	 * @return
	 */
	private JPanel computingPanel() {
		JComboBox solverBox = new JComboBox(availableSolvers);
		JComboBox distanceBox = new JComboBox(availableDistances);
		JSpinner startTime = new JSpinner(new SpinnerNumberModel(t1, 0,
				maxTime, stepsPerUnitTime));
		JSpinner maxStepsPerUnitTime = new JSpinner(new SpinnerNumberModel(
				stepsPerUnitTime, 1, Integer.MAX_VALUE, 1));
		JSpinner numberOfSteps = new JSpinner(new SpinnerNumberModel(numSteps,
				1, (int) stepsPerUnitTime * t2, 1));
		SpinnerNumberModel spinnerModel = new SpinnerNumberModel(maxTime, 0,
				1000, 1d); // Double.MAX_VALUE
		JSpinner maximalEndTime = new JSpinner(spinnerModel);
		JSpinner endTime = new JSpinner(new SpinnerNumberModel(t2, t1, maxTime,
				numSteps * (t2 - t1)));

		LayoutHelper lh = createTitledPanel("Computing");
		lh.add("ODE Solver", solverBox, true);
		lh.add("Distance", distanceBox, true);
		lh.add("Start time", startTime, true);
		lh.add("End time", endTime, true);
		lh.add("Maximal end time", maximalEndTime, true);
		lh.add(GUITools.toHTML("Maximal number of steps per unit time", 20),
				maxStepsPerUnitTime, true);
		lh.add("Number of steps", numberOfSteps, true);

		return (JPanel) lh.getContainer();
	}

	/**
	 * Helper method that creates a LayoutHelper with the given title.
	 * 
	 * @param title
	 * @return
	 */
	private LayoutHelper createTitledPanel(String title) {
		JPanel p = new JPanel();
		LayoutHelper lh = new LayoutHelper(p);
		p.setBorder(BorderFactory.createTitledBorder(' ' + title + ' '));
		return lh;
	}

	/**
	 * 
	 * @return
	 */
	private JPanel parsingPanel() {
		LayoutHelper lh = createTitledPanel("Parsing");

		JTextField tf = new JTextField(openDir);
		JButton button = GUITools
				.createButton(GUITools.ICON_OPEN, this, Command.OPEN_DIR,
						"Select the default open directory for experimental data files.");
		lh.add("Open directory", tf, button);
		lh.add(new JPanel(), 0, lh.getRow(), 1, 1);

		tf = new JTextField(saveDir);
		button = GUITools
				.createButton(GUITools.ICON_SAVE, this, Command.SAVE_DIR,
						"Select the default save directory for simulation data result files.");
		lh.add("Save directory", tf, button);
		lh.add(new JPanel(), 0, lh.getRow(), 1, 1);

		lh.add("Quote character",
				new JTextField(Character.toString(quoteChar)), true);
		JComboBox combo = new JComboBox(new String[] { "Linux", "Windows",
				"Mac OS" });
		lh.add("Line separator character", combo, true);

		return (JPanel) lh.getContainer();
	}

	/**
	 * 
	 * @return
	 */
	private JPanel plotPanel() {
		LayoutHelper lh = createTitledPanel("Plot settings");
		lh.add("Step size", new JSpinner(new SpinnerNumberModel(
				parameterStepSize, 0, spinnerMaxVal, 0.01)), true);
		lh.add("Max. Compartment value", new JSpinner(new SpinnerNumberModel(
				maxCompartmentVal, 0, spinnerMaxVal, 0.01)), true);
		lh.add("Max. Species value", new JSpinner(new SpinnerNumberModel(
				maxSpeciesVal, 0, spinnerMaxVal, 0.01)), true);
		lh.add("Max. Param value", new JSpinner(new SpinnerNumberModel(
				maxParameterVal, 0, spinnerMaxVal, 0.01)), true);
		return (JPanel) lh.getContainer();
	}

	/**
	 * 
	 * @return
	 */
	private JPanel scanPanel() {
		LayoutHelper lh = createTitledPanel("Interactive scan");
		lh.add(new JCheckBox("Show legend", showLegend));
		lh.add(new JPanel());
		lh.add(new JCheckBox("Show grid", showGrid));
		lh.add(new JPanel());
		lh.add(new JCheckBox("Logarithmic scale", logScale));
		return (JPanel) lh.getContainer();
	}

}
