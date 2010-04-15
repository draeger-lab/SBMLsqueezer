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
import java.lang.reflect.InvocationTargetException;
import java.util.Properties;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;

import org.sbml.squeezer.CfgKeys;
import org.sbml.squeezer.SBMLsqueezer;
import org.sbml.squeezer.math.Distance;

import eva2.tools.math.des.AbstractDESSolver;

/**
 * @author Andreas Dr&auml;ger
 * @since 1.4
 * @date 2010-04-13
 */
public class SettingsPanelSimulation extends SettingsPanel implements
		ActionListener {

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
	private JCheckBox ckbxLogScale;
	private JCheckBox ckbxShowGrid;
	private JCheckBox ckbxShowLegend;
	private JComboBox cmbBxDistance;
	private JComboBox cmbBxSolver;
	private char quoteChar;
	private SpinnerNumberModel spinModMaxCompartmentVal;
	private SpinnerNumberModel spinModMaxParameterVal;
	private SpinnerNumberModel spinModMaxSpeciesVal;
	private SpinnerNumberModel spinModNumSteps;
	private SpinnerNumberModel spinModParameterStepSize;
	private SpinnerNumberModel spinModStepsPerUnitTime;
	private SpinnerNumberModel spinModT1;
	private SpinnerNumberModel spinModT2;
	private double spinnerMaxVal = 10000;
	private JTextField tfOpenDir;
	private JTextField tfQuoteChar;
	private JTextField tfSaveDir;

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
		tfOpenDir = new JTextField(System.getProperty("user.home"));
		tfSaveDir = new JTextField(System.getProperty("user.home"));
		tfOpenDir.addKeyListener(this);
		tfSaveDir.addKeyListener(this);
		LayoutHelper lh = new LayoutHelper(this);
		lh.add(computingPanel(), 0, 0, 1, 1, 0, 0);
		lh.add(new JPanel(), 0, 1, 1, 1, 0, 0);
		lh.add(parsingPanel(), 0, 2, 1, 1, 0, 0);
		lh.add(new JPanel(), 1, 0, 1, 1, 0, 0);
		lh.add(scanPanel(), 2, 0, 1, 1, 0, 0);
		lh.add(plotPanel(), 2, 2, 1, 1, 0, 0);
	}

	/**
	 * 
	 * @param settings
	 * @throws IllegalArgumentException
	 * @throws SecurityException
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws InvocationTargetException
	 * @throws NoSuchMethodException
	 */
	public SettingsPanelSimulation(Properties settings)
			throws IllegalArgumentException, SecurityException,
			InstantiationException, IllegalAccessException,
			InvocationTargetException, NoSuchMethodException {
		this();
		setProperties(settings);
	}

	public void setProperties(Properties settings) {
		ckbxLogScale.setSelected(((Boolean) settings
				.get(CfgKeys.PLOT_LOG_SCALE)).booleanValue());
		ckbxShowGrid.setSelected(((Boolean) settings
				.get(CfgKeys.PLOT_SHOW_GRID)).booleanValue());
		ckbxShowLegend.setSelected(((Boolean) settings
				.get(CfgKeys.PLOT_SHOW_LEGEND)).booleanValue());
		// int dist = 0;
		// while (dist < cmbBxDistance.getItemCount() &&
		// !cmbBxDistance.getSelectedItem())
		// dist++;
		// cmbBxDistance.setSelectedIndex(dist);
		// int solv = 0;
		// while (solv < cmbBxSolver.getItemCount() &&
		// !cmbBxSolver.getSelectedItem().equals())
		// solv++;
		tfQuoteChar.setText(settings.get(CfgKeys.CSV_FILES_QUOTE_CHAR)
				.toString());
		spinModMaxCompartmentVal.setValue(((Double) settings
				.get(CfgKeys.SIM_MAX_COMPARTMENT_SIZE)).doubleValue());
		spinModMaxSpeciesVal.setValue(((Double) settings
				.get(CfgKeys.SIM_MAX_SPECIES_VALUE)).doubleValue());
		spinModMaxParameterVal.setValue(((Double) settings
				.get(CfgKeys.SIM_MAX_PARAMETER_VALUE)).doubleValue());
		spinModT1.setValue(((Double) settings.get(CfgKeys.SIM_START_TIME)).doubleValue());
		spinModT2.setValue(((Double) settings.get(CfgKeys.SIM_END_TIME)).doubleValue());
		spinnerMaxVal = ((Double) settings.get(CfgKeys.SPINNER_STEP_SIZE)).doubleValue();
//		spinModNumSteps.setValue();
		tfOpenDir.setText(settings.get(CfgKeys.CSV_FILES_OPEN_DIR).toString());
		tfSaveDir.setText(settings.get(CfgKeys.CSV_FILES_SAVE_DIR).toString());
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
			tfOpenDir.setText(selectOpenDir(tfOpenDir.getText()));
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
		cmbBxSolver = new JComboBox(solvers);
		cmbBxSolver.setEnabled(solvers.length > 1);

		Class<Distance> availableDistances[] = SBMLsqueezer
				.getAvailableDistances();
		String[] distances = new String[availableDistances.length];
		i = 0;
		for (Class<Distance> distance : availableDistances)
			distances[i++] = distance.getConstructor().newInstance().getName();
		cmbBxDistance = new JComboBox(distances);

		int maxTime = 100;
		spinModStepsPerUnitTime = new SpinnerNumberModel(5, 1,
				(int) spinnerMaxVal, 1);
		spinModT1 = new SpinnerNumberModel(0d, 0d, maxTime,
				((Integer) spinModStepsPerUnitTime.getValue()).intValue());
		JSpinner startTime = new JSpinner(spinModT1);
		startTime.setEnabled(false);
		JSpinner maxStepsPerUnitTime = new JSpinner(new SpinnerNumberModel(
				((Integer) spinModStepsPerUnitTime.getValue()).intValue(), 1,
				Integer.MAX_VALUE, 1));
		double endT = 5;
		spinModNumSteps = new SpinnerNumberModel(
				(int) (((Integer) spinModStepsPerUnitTime.getValue())
						.intValue()
						* endT - 1) / 2, 1,
				(int) (((Integer) spinModStepsPerUnitTime.getValue())
						.intValue() * endT), 1);
		spinModT2 = new SpinnerNumberModel(
				endT,
				((Double) spinModT1.getValue()).doubleValue(),
				maxTime,
				((Integer) spinModNumSteps.getValue()).intValue()
						* (endT - ((Double) spinModT1.getValue()).doubleValue()));
		JSpinner numberOfSteps = new JSpinner(spinModNumSteps);
		SpinnerNumberModel spinnerModel = new SpinnerNumberModel(maxTime, 0,
				1000, 1d); // Double.MAX_VALUE
		JSpinner maximalEndTime = new JSpinner(spinnerModel);

		LayoutHelper lh = createTitledPanel("Computing");
		lh.add("ODE Solver", cmbBxSolver, true);
		lh.add("Distance", cmbBxDistance, true);
		lh.add("Start time", startTime, true);
		lh.add("End time", new JSpinner(spinModT2), true);
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
	 * @return the availableDistances
	 */
	public int getDistance() {
		return cmbBxDistance.getSelectedIndex();
	}

	/**
	 * @return the logScale
	 */
	public boolean getLogScale() {
		return ckbxLogScale.isSelected();
	}

	/**
	 * @return the maxCompartmentVal
	 */
	public double getMaxCompartmentValue() {
		return ((Double) spinModMaxCompartmentVal.getValue()).doubleValue();
	}

	/**
	 * @return the maxParameterVal
	 */
	public double getMaxParameterValue() {
		return ((Double) spinModMaxParameterVal.getValue()).doubleValue();
	}

	/**
	 * @return the maxSpeciesVal
	 */
	public double getMaxSpeciesValue() {
		return ((Double) spinModMaxSpeciesVal.getValue()).doubleValue();
	}

	/**
	 * @return the maxTime
	 */
	public double getMaxTime() {
		return ((Double) spinModT2.getMaximum()).doubleValue();
	}

	/**
	 * @return the numSteps
	 */
	public int getNumIntegrationSteps() {
		return ((Integer) spinModNumSteps.getValue()).intValue();
	}

	/**
	 * @return the openDir
	 */
	public String getOpenDir() {
		return tfOpenDir.getText();
	}

	/**
	 * @return the parameterStepSize
	 */
	public double getParameterStepSize() {
		return ((Double) spinModParameterStepSize.getValue()).doubleValue();
	}

	/**
	 * @return the quoteChar
	 */
	public char getQuoteChar() {
		if (tfQuoteChar.getText().length() > 0)
			return tfQuoteChar.getText().charAt(0);
		return quoteChar;
	}

	/**
	 * @return the saveDir
	 */
	public String getSaveDir() {
		return tfSaveDir.getText();
	}

	/**
	 * @return the showGrid
	 */
	public boolean getShowGrid() {
		return ckbxShowGrid.isSelected();
	}

	/**
	 * @return the showLegend
	 */
	public boolean getShowLegend() {
		return ckbxShowLegend.isSelected();
	}

	/**
	 * @return the t2
	 */
	public double getSimulationEndTime() {
		return ((Double) spinModT2.getValue()).doubleValue();
	}

	/**
	 * @return the t1
	 */
	public double getSimulationStartTime() {
		return ((Double) spinModT1.getValue()).doubleValue();
	}

	/**
	 * @return the availableSolvers
	 */
	public int getSolver() {
		return cmbBxSolver.getSelectedIndex();
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
		return ((Integer) spinModStepsPerUnitTime.getValue()).intValue();
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
		lh.add("Open directory", tfOpenDir, button);
		lh.add(new JPanel(), 0, lh.getRow(), 1, 1);
		button = GUITools
				.createButton(GUITools.ICON_SAVE, this, Command.SAVE_DIR,
						"Select the default save directory for simulation data result files.");
		lh.add("Save directory", tfSaveDir, button);
		lh.add(new JPanel(), 0, lh.getRow(), 1, 1);

		tfQuoteChar = new JTextField(Character.toString(quoteChar));
		lh.add("Quote character", tfQuoteChar, true);
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
		double prev = 100d;
		LayoutHelper lh = createTitledPanel("Interactive scan");
		spinModParameterStepSize = new SpinnerNumberModel(0.1, 0,
				spinnerMaxVal, 0.01);
		spinModMaxCompartmentVal = new SpinnerNumberModel(prev, 0,
				spinnerMaxVal, 0.01);
		spinModMaxSpeciesVal = new SpinnerNumberModel(prev, 0, spinnerMaxVal,
				0.01);
		spinModMaxParameterVal = new SpinnerNumberModel(prev, 0, spinnerMaxVal,
				0.01);
		lh.add("Step size", new JSpinner(spinModParameterStepSize), true);
		lh.add("Max. Compartment value",
				new JSpinner(spinModMaxCompartmentVal), true);
		lh.add("Max. Species value", new JSpinner(spinModMaxSpeciesVal), true);
		lh.add("Max. Param value", new JSpinner(spinModMaxParameterVal), true);
		return (JPanel) lh.getContainer();
	}

	/**
	 * 
	 * @return
	 */
	private JPanel scanPanel() {
		LayoutHelper lh = createTitledPanel("Plot settings");
		ckbxShowLegend = new JCheckBox("Show legend");
		ckbxShowGrid = new JCheckBox("Show grid");
		ckbxLogScale = new JCheckBox("Logarithmic scale");
		lh.add(ckbxShowLegend);
		lh.add(new JPanel());
		lh.add(ckbxShowGrid);
		lh.add(new JPanel());
		lh.add(ckbxLogScale);
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
	 * 
	 * @param index
	 */
	public void setDistance(int index) {
		cmbBxDistance.setSelectedIndex(index);
	}

	/**
	 * @param logScale
	 *            the logScale to set
	 */
	public void setLogScale(boolean logScale) {
		this.ckbxLogScale.setSelected(logScale);
	}

	/**
	 * @param maxCompartmentVal
	 *            the maxCompartmentVal to set
	 */
	public void setMaxCompartmentValue(double maxCompartmentVal) {
		this.spinModMaxCompartmentVal.setValue(Double
				.valueOf(maxCompartmentVal));
	}

	/**
	 * @param maxParameterVal
	 *            the maxParameterVal to set
	 */
	public void setMaxParameterValue(double maxParameterVal) {
		this.spinModMaxParameterVal.setValue(Double.valueOf(maxParameterVal));
	}

	/**
	 * @param maxSpeciesVal
	 *            the maxSpeciesVal to set
	 */
	public void setMaxSpeciesValue(double maxSpeciesVal) {
		this.spinModMaxSpeciesVal.setValue(Double.valueOf(maxSpeciesVal));
	}

	/**
	 * @param maxTime
	 *            the maxTime to set
	 */
	public void setMaxTime(double maxTime) {
		this.spinModT2.setMaximum(Double.valueOf(maxTime));
	}

	/**
	 * @param numSteps
	 *            the numSteps to set
	 */
	public void setNumIntegrationSteps(int numSteps) {
		this.spinModNumSteps.setValue(Integer.valueOf(numSteps));
	}

	/**
	 * @param openDir
	 *            the openDir to set
	 */
	public void setOpenDir(String openDir) {
		this.tfOpenDir.setText(openDir);
	}

	/**
	 * @param parameterStepSize
	 *            the parameterStepSize to set
	 */
	public void setParameterStepSize(double parameterStepSize) {
		this.spinModParameterStepSize.setValue(Double
				.valueOf(parameterStepSize));
		this.spinModMaxCompartmentVal.setStepSize(Double
				.valueOf(parameterStepSize));
		this.spinModMaxParameterVal.setStepSize(Double
				.valueOf(parameterStepSize));
		this.spinModMaxSpeciesVal
				.setStepSize(Double.valueOf(parameterStepSize));
		this.spinModT1.setStepSize(Double.valueOf(parameterStepSize));
		this.spinModT2.setStepSize(Double.valueOf(parameterStepSize));
	}

	/**
	 * @param quoteChar
	 *            the quoteChar to set
	 */
	public void setQuoteChar(char quoteChar) {
		this.tfQuoteChar.setText(Character.toString(quoteChar));
	}

	/**
	 * @param saveDir
	 *            the saveDir to set
	 */
	public void setSaveDir(String saveDir) {
		this.tfSaveDir.setText(saveDir);
	}

	/**
	 * @param showGrid
	 *            the showGrid to set
	 */
	public void setShowGrid(boolean showGrid) {
		this.ckbxShowGrid.setSelected(showGrid);
	}

	/**
	 * @param showLegend
	 *            the showLegend to set
	 */
	public void setShowLegend(boolean showLegend) {
		this.ckbxShowLegend.setSelected(showLegend);
	}

	/**
	 * @param t2
	 *            the t2 to set
	 */
	public void setSimulationEndTime(double t2) {
		this.spinModT2.setValue(Double.valueOf(t2));
	}

	/**
	 * @param t1
	 *            the t1 to set
	 */
	public void setSimulationStartTime(double t1) {
		this.spinModT1.setValue(Double.valueOf(t1));
	}

	/**
	 * 
	 * @param index
	 */
	public void setSolver(int index) {
		cmbBxSolver.setSelectedIndex(index);
	}

	/**
	 * @param spinnerMaxVal
	 *            the spinnerMaxVal to set
	 */
	public void setSpinnerMaxValue(double spinnerMaxVal) {
		this.spinnerMaxVal = spinnerMaxVal;
		this.spinModMaxCompartmentVal.setMaximum(Double.valueOf(spinnerMaxVal));
		this.spinModMaxParameterVal.setMaximum(Double.valueOf(spinnerMaxVal));
		this.spinModMaxSpeciesVal.setMaximum(Double.valueOf(spinnerMaxVal));
		this.spinModNumSteps.setMaximum(Integer.valueOf((int) spinnerMaxVal));
		this.spinModParameterStepSize.setMaximum(Double.valueOf(spinnerMaxVal));
		this.spinModStepsPerUnitTime.setMaximum(Double.valueOf(spinnerMaxVal));
		this.spinModT1.setMaximum(Double.valueOf(spinnerMaxVal));
		this.spinModT2.setMaximum(Double.valueOf(spinnerMaxVal));
	}

	/**
	 * @param stepsPerUnitTime
	 *            the stepsPerUnitTime to set
	 */
	public void setStepsPerUnitTime(int stepsPerUnitTime) {
		this.spinModStepsPerUnitTime.setValue(stepsPerUnitTime);
	}
}
