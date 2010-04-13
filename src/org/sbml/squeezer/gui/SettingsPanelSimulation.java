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
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.lang.reflect.InvocationTargetException;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;

import org.sbml.squeezer.SBMLsqueezer;
import org.sbml.squeezer.math.Distance;

import eva2.tools.math.des.AbstractDESSolver;

/**
 * @author Andreas Dr&auml;ger
 * @since 1.4
 * @date 2010-04-13
 */
public class SettingsPanelSimulation extends SettingsPanel implements
		ActionListener, ItemListener {

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
	 * Generated serial version identifier
	 */
	private static final long serialVersionUID = -618135419544428464L;
	private boolean logScale;
	private double maxCompartmentVal;
	private double maxParameterVal;
	private double maxSpeciesVal;
	private double maxTime = 100;
	private int numSteps = 10;
	private JTextField openDirTextField;
	private double parameterStepSize;
	private char quoteChar;
	private JTextField saveDirTextField;
	private int selectedDistance;
	private int selectedSolver;
	private boolean showGrid;
	private boolean showLegend;
	private double spinnerMaxVal = 10000;
	private int stepsPerUnitTime = 5;
	private double t1 = 0;

	private double t2 = 5;

	/**
	 * @throws NoSuchMethodException
	 * @throws InvocationTargetException
	 * @throws IllegalAccessException
	 * @throws InstantiationException
	 * @throws SecurityException
	 * @throws IllegalArgumentException
	 * 
	 */
	public SettingsPanelSimulation() throws IllegalArgumentException,
			SecurityException, InstantiationException, IllegalAccessException,
			InvocationTargetException, NoSuchMethodException {
		super(new GridBagLayout());
		openDirTextField = new JTextField(System.getProperty("user.home"));
		saveDirTextField = new JTextField(System.getProperty("user.home"));
		openDirTextField.addKeyListener(this);
		saveDirTextField.addKeyListener(this);
		LayoutHelper lh = new LayoutHelper(this);
		lh.add(computingPanel(), 0, 0, 1, 1, 0, 0);
		lh.add(new JPanel(), 0, 1, 1, 1, 0, 0);
		lh.add(parsingPanel(), 0, 2, 1, 1, 0, 0);
		lh.add(new JPanel(), 1, 0, 1, 1, 0, 0);
		lh.add(scanPanel(), 2, 0, 1, 1, 0, 0);
		lh.add(plotPanel(), 2, 2, 1, 1, 0, 0);
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
			openDirTextField.setText(selectOpenDir(openDirTextField.getText()));
			break;

		default:
			break;
		}
	}

	/**
	 * 
	 * @return
	 * @throws NoSuchMethodException
	 * @throws InvocationTargetException
	 * @throws IllegalAccessException
	 * @throws InstantiationException
	 * @throws SecurityException
	 * @throws IllegalArgumentException
	 */
	private JPanel computingPanel() throws IllegalArgumentException,
			SecurityException, InstantiationException, IllegalAccessException,
			InvocationTargetException, NoSuchMethodException {
		Class<AbstractDESSolver> availableSolvers[] = SBMLsqueezer
				.getAvailableSolvers();
		String solvers[] = new String[availableSolvers.length];
		int i = 0;
		for (Class<AbstractDESSolver> solver : availableSolvers)
			solvers[i++] = solver.getConstructor().newInstance().getName();
		JComboBox solverBox = new JComboBox(solvers);
		solverBox.setName("solvers");
		solverBox.addItemListener(this);
		solverBox.setEnabled(solvers.length > 1);

		Class<Distance> availableDistances[] = SBMLsqueezer
				.getAvailableDistances();
		String[] distances = new String[availableDistances.length];
		i = 0;
		for (Class<Distance> distance : availableDistances)
			distances[i++] = distance.getConstructor().newInstance().getName();
		JComboBox distanceBox = new JComboBox(distances);
		distanceBox.setName("distances");
		distanceBox.addItemListener(this);

		JSpinner startTime = new JSpinner(new SpinnerNumberModel(t1, 0,
				maxTime, stepsPerUnitTime));
		startTime.setEnabled(false);
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
	 * @return the logScale
	 */
	public boolean getLogScale() {
		return logScale;
	}

	/**
	 * @return the maxCompartmentVal
	 */
	public double getMaxCompartmentValue() {
		return maxCompartmentVal;
	}

	/**
	 * @return the maxParameterVal
	 */
	public double getMaxParameterValue() {
		return maxParameterVal;
	}

	/**
	 * @return the maxSpeciesVal
	 */
	public double getMaxSpeciesValue() {
		return maxSpeciesVal;
	}

	/**
	 * @return the maxTime
	 */
	public double getMaxTime() {
		return maxTime;
	}

	/**
	 * @return the numSteps
	 */
	public int getNumIntegrationSteps() {
		return numSteps;
	}

	/**
	 * @return the openDir
	 */
	public String getOpenDir() {
		return openDirTextField.getText();
	}

	/**
	 * @return the parameterStepSize
	 */
	public double getParameterStepSize() {
		return parameterStepSize;
	}

	/**
	 * @return the quoteChar
	 */
	public char getQuoteChar() {
		return quoteChar;
	}

	/**
	 * @return the saveDir
	 */
	public String getSaveDir() {
		return saveDirTextField.getText();
	}

	/**
	 * @return the availableDistances
	 */
	public int getSelectedDistance() {
		return selectedDistance;
	}

	/**
	 * @return the availableSolvers
	 */
	public int getSelectedSolver() {
		return selectedSolver;
	}

	/**
	 * @return the showGrid
	 */
	public boolean getShowGrid() {
		return showGrid;
	}

	/**
	 * @return the showLegend
	 */
	public boolean getShowLegend() {
		return showLegend;
	}

	/**
	 * @return the t2
	 */
	public double getSimulationEndTime() {
		return t2;
	}

	/**
	 * @return the t1
	 */
	public double getSimulationStartTime() {
		return t1;
	}

	/**
	 * @return the spinnerMaxVal
	 */
	public double getSpinnerMaxValue() {
		return spinnerMaxVal;
	}

	/**
	 * @return the stepsPerUnitTime
	 */
	public int getStepsPerUnitTime() {
		return stepsPerUnitTime;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * java.awt.event.ItemListener#itemStateChanged(java.awt.event.ItemEvent)
	 */
	public void itemStateChanged(ItemEvent e) {
		if (e.getSource() instanceof JComboBox) {
			JComboBox box = (JComboBox) e.getSource();
			if (box.getName().equals("solvers"))
				selectedSolver = box.getSelectedIndex();
			else if (box.getName().equals("distances"))
				selectedDistance = box.getSelectedIndex();
		} else if (e.getSource() instanceof JCheckBox) {
			JCheckBox box = (JCheckBox) e.getSource();
			if (box.getName().equals("legend")) {
				showLegend = box.isSelected();
			} else if (box.getName().equals("grid")) {
				showGrid = box.isSelected();
			} else if (box.getName().equals("logarithm")) {
				logScale = box.isSelected();
			}
		}
	}

	/**
	 * 
	 * @return
	 */
	private JPanel parsingPanel() {
		LayoutHelper lh = createTitledPanel("Parsing");
		JButton button = GUITools
				.createButton(GUITools.ICON_OPEN, this, Command.OPEN_DIR,
						"Select the default open directory for experimental data files.");
		lh.add("Open directory", openDirTextField, button);
		lh.add(new JPanel(), 0, lh.getRow(), 1, 1);
		button = GUITools
				.createButton(GUITools.ICON_SAVE, this, Command.SAVE_DIR,
						"Select the default save directory for simulation data result files.");
		lh.add("Save directory", saveDirTextField, button);
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
		LayoutHelper lh = createTitledPanel("Interactive scan");
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
		LayoutHelper lh = createTitledPanel("Plot settings");
		JCheckBox legend = new JCheckBox("Show legend", showLegend);
		JCheckBox grid = new JCheckBox("Show grid", showGrid);
		JCheckBox logarithm = new JCheckBox("Logarithmic scale", logScale);
		legend.setName("legend");
		legend.addItemListener(this);
		grid.setName("grid");
		grid.addItemListener(this);
		logarithm.setName("logarithm");
		logarithm.addItemListener(this);
		lh.add(legend);
		lh.add(new JPanel());
		lh.add(grid);
		lh.add(new JPanel());
		lh.add(logarithm);
		return (JPanel) lh.getContainer();
	}

	/**
	 * 
	 * @param openDir
	 * @return
	 */
	private String selectOpenDir(String openDir) {
		JFileChooser fc = GUITools.createJFileChooser(openDir, true, false,
				JFileChooser.DIRECTORIES_ONLY);
		if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION)
			openDir = fc.getSelectedFile().getAbsolutePath();
		return openDir;
	}

	/**
	 * @param logScale
	 *            the logScale to set
	 */
	public void setLogScale(boolean logScale) {
		this.logScale = logScale;
	}

	/**
	 * @param maxCompartmentVal
	 *            the maxCompartmentVal to set
	 */
	public void setMaxCompartmentValue(double maxCompartmentVal) {
		this.maxCompartmentVal = maxCompartmentVal;
	}

	/**
	 * @param maxParameterVal
	 *            the maxParameterVal to set
	 */
	public void setMaxParameterValue(double maxParameterVal) {
		this.maxParameterVal = maxParameterVal;
	}

	/**
	 * @param maxSpeciesVal
	 *            the maxSpeciesVal to set
	 */
	public void setMaxSpeciesValue(double maxSpeciesVal) {
		this.maxSpeciesVal = maxSpeciesVal;
	}

	/**
	 * @param maxTime
	 *            the maxTime to set
	 */
	public void setMaxTime(double maxTime) {
		this.maxTime = maxTime;
	}

	/**
	 * @param numSteps
	 *            the numSteps to set
	 */
	public void setNumIntegrationSteps(int numSteps) {
		this.numSteps = numSteps;
	}

	/**
	 * @param openDir
	 *            the openDir to set
	 */
	public void setOpenDir(String openDir) {
		this.openDirTextField.setText(openDir);
	}

	/**
	 * @param parameterStepSize
	 *            the parameterStepSize to set
	 */
	public void setParameterStepSize(double parameterStepSize) {
		this.parameterStepSize = parameterStepSize;
	}

	/**
	 * @param quoteChar
	 *            the quoteChar to set
	 */
	public void setQuoteChar(char quoteChar) {
		this.quoteChar = quoteChar;
	}

	/**
	 * @param saveDir
	 *            the saveDir to set
	 */
	public void setSaveDir(String saveDir) {
		this.saveDirTextField.setText(saveDir);
	}

	/**
	 * @param showGrid
	 *            the showGrid to set
	 */
	public void setShowGrid(boolean showGrid) {
		this.showGrid = showGrid;
	}

	/**
	 * @param showLegend
	 *            the showLegend to set
	 */
	public void setShowLegend(boolean showLegend) {
		this.showLegend = showLegend;
	}

	/**
	 * @param t2
	 *            the t2 to set
	 */
	public void setSimulationEndTime(double t2) {
		this.t2 = t2;
	}

	/**
	 * @param t1
	 *            the t1 to set
	 */
	public void setSimulationStartTime(double t1) {
		this.t1 = t1;
	}

	/**
	 * @param spinnerMaxVal
	 *            the spinnerMaxVal to set
	 */
	public void setSpinnerMaxValue(double spinnerMaxVal) {
		this.spinnerMaxVal = spinnerMaxVal;
	}

	/**
	 * @param stepsPerUnitTime
	 *            the stepsPerUnitTime to set
	 */
	public void setStepsPerUnitTime(int stepsPerUnitTime) {
		this.stepsPerUnitTime = stepsPerUnitTime;
	}
}
