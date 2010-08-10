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
package org.sbml.simulator.gui;

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
import javax.swing.JFormattedTextField;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;

import org.sbml.squeezer.CfgKeys;
import org.sbml.squeezer.SBMLsqueezer;
import org.sbml.squeezer.gui.GUITools;
import org.sbml.squeezer.gui.LayoutHelper;
import org.sbml.squeezer.gui.SettingsPanel;
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
	/*
	 * Plot
	 */
	private JCheckBox ckbxLogScale;
	private JCheckBox ckbxShowGrid;
	private JCheckBox ckbxShowLegend;
	/*
	 * Scan
	 */
	private SpinnerNumberModel spinModMaxCompartmentVal;
	private SpinnerNumberModel spinModMaxParameterVal;
	private SpinnerNumberModel spinModMaxSpeciesVal;
	private SpinnerNumberModel spinModParameterStepSize;
	/*
	 * Computing
	 */
	private JComboBox cmbBxDistance;
	private JComboBox cmbBxSolver;
	private SpinnerNumberModel spinModT1;
	private SpinnerNumberModel spinModT2;
	private double spinnerMaxVal = 10000;
	private SpinnerNumberModel spinModStepsPerUnitTime;
	private SpinnerNumberModel spinModNumSteps;
	/*
	 * Parsing
	 */
	private JTextField tfOpenDir;
	private JTextField tfSaveDir;
	private JFormattedTextField tfQuoteChar;
	private JFormattedTextField tfSeparatorChar;

	private char quoteChar = '\'';
	private char separatorChar = ',';

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
		super();
		setLayout(new GridBagLayout());
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
		for (Class<AbstractDESSolver> solver : availableSolvers) {
			solvers[i++] = solver.getConstructor().newInstance().getName();
		}
		cmbBxSolver = new JComboBox(solvers);
		cmbBxSolver.setEnabled(solvers.length > 1);
		cmbBxSolver.addItemListener(this);

		Class<Distance> availableDistances[] = SBMLsqueezer
				.getAvailableDistances();
		String[] distances = new String[availableDistances.length];
		i = 0;
		for (Class<Distance> distance : availableDistances)
			distances[i++] = distance.getConstructor().newInstance().getName();
		cmbBxDistance = new JComboBox(distances);
		cmbBxDistance.addItemListener(this);

		int maxTime = 100;
		spinModStepsPerUnitTime = new SpinnerNumberModel(5, 1,
				(int) spinnerMaxVal, 1);
		spinModT1 = new SpinnerNumberModel(0d, 0d, maxTime,
				((Integer) spinModStepsPerUnitTime.getValue()).intValue());
		JSpinner startTime = new JSpinner(spinModT1);
		startTime.addChangeListener(this);
		startTime.setEnabled(false);
		JSpinner maxStepsPerUnitTime = new JSpinner(new SpinnerNumberModel(
				((Integer) spinModStepsPerUnitTime.getValue()).intValue(), 1,
				Integer.MAX_VALUE, 1));
		maxStepsPerUnitTime.addChangeListener(this);
		double endT = 5;
		spinModNumSteps = new SpinnerNumberModel(
				(int) (((Integer) spinModStepsPerUnitTime.getValue())
						.intValue()
						* endT - 1) / 2, 1,
				(int) (((Integer) spinModStepsPerUnitTime.getValue())
						.intValue() * endT), 1);
		spinModT2 = new SpinnerNumberModel(
				endT,
				((Number) spinModT1.getValue()).doubleValue(),
				maxTime,
				((Integer) spinModNumSteps.getValue()).intValue()
						* (endT - ((Number) spinModT1.getValue()).doubleValue()));
		JSpinner numberOfSteps = new JSpinner(spinModNumSteps);
		numberOfSteps.addChangeListener(this);
		SpinnerNumberModel spinnerModel = new SpinnerNumberModel(maxTime, 0,
				1000, 1d); // Double.MAX_VALUE
		JSpinner maximalEndTime = new JSpinner(spinnerModel);
		maximalEndTime.addChangeListener(this);

		JSpinner endTime = new JSpinner(spinModT2);
		endTime.addChangeListener(this);
		LayoutHelper lh = createTitledPanel("Computing");
		lh.add("ODE Solver", cmbBxSolver, true);
		lh.add("Distance", cmbBxDistance, true);
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
		return ((Number) spinModMaxCompartmentVal.getValue()).doubleValue();
	}

	/**
	 * @return the maxParameterVal
	 */
	public double getMaxParameterValue() {
		return ((Number) spinModMaxParameterVal.getValue()).doubleValue();
	}

	/**
	 * @return the maxSpeciesVal
	 */
	public double getMaxSpeciesValue() {
		return ((Number) spinModMaxSpeciesVal.getValue()).doubleValue();
	}

	/**
	 * @return the maxTime
	 */
	public double getMaxTime() {
		return ((Number) spinModT2.getMaximum()).doubleValue();
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
		return ((Number) spinModParameterStepSize.getValue()).doubleValue();
	}

	public Properties getProperties() {
		Properties settings = new Properties();
		/*
		 * Plot
		 */
		settings.put(CfgKeys.PLOT_LOG_SCALE, Boolean.valueOf(getLogScale()));
		settings.put(CfgKeys.PLOT_SHOW_GRID, Boolean.valueOf(getShowGrid()));
		settings
				.put(CfgKeys.PLOT_SHOW_LEGEND, Boolean.valueOf(getShowLegend()));
		/*
		 * Scan
		 */
		settings
				.put(CfgKeys.SIM_MAX_COMPARTMENT_SIZE, getMaxCompartmentValue());
		settings.put(CfgKeys.SIM_MAX_SPECIES_VALUE, getMaxSpeciesValue());
		settings.put(CfgKeys.SIM_MAX_PARAMETER_VALUE, getMaxParameterValue());
		settings.put(CfgKeys.SPINNER_STEP_SIZE, getParameterStepSize());
		/*
		 * Computation
		 */
		settings.put(CfgKeys.SIM_ODE_SOLVER, getSolverFunction());
		settings.put(CfgKeys.SIM_DISTANCE_FUNCTION, getDistanceFunction());
		settings.put(CfgKeys.SIM_MAX_TIME, getMaxTime());
		double t1 = getSimulationStartTime();
		double t2 = getSimulationEndTime();
		settings.put(CfgKeys.SIM_START_TIME, t1);
		settings.put(CfgKeys.SIM_END_TIME, t2);
		settings.put(CfgKeys.SPINNER_MAX_VALUE, getSpinnerMaxValue());
		settings
				.put(CfgKeys.SIM_MAX_STEPS_PER_UNIT_TIME, getStepsPerUnitTime());
		settings.put(CfgKeys.SIM_STEP_SIZE, getNumIntegrationSteps()
				* (t2 - t1));
		/*
		 * Parsing
		 */
		settings.put(CfgKeys.CSV_FILES_OPEN_DIR, getOpenDir());
		settings.put(CfgKeys.CSV_FILES_SAVE_DIR, getSaveDir());
		settings.put(CfgKeys.CSV_FILES_QUOTE_CHAR, getQuoteChar());
		settings.put(CfgKeys.CSV_FILES_SEPARATOR_CHAR, getSeparatorChar());
		return settings;
	}

	/**
	 * 
	 * @return
	 */
	public String getSolverFunction() {
		return SBMLsqueezer.getAvailableSolvers()[cmbBxSolver
				.getSelectedIndex()].getName();
	}

	/**
	 * 
	 * @return
	 */
	public String getDistanceFunction() {
		return SBMLsqueezer.getAvailableDistances()[cmbBxDistance
				.getSelectedIndex()].getName();
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
	 * @return the separatorChar
	 */
	public char getSeparatorChar() {
		if (tfSeparatorChar.getText().length() > 0)
			return tfSeparatorChar.getText().charAt(0);
		return separatorChar;
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
		return ((Number) spinModT2.getValue()).doubleValue();
	}

	/**
	 * @return the t1
	 */
	public double getSimulationStartTime() {
		return ((Number) spinModT1.getValue()).doubleValue();
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

		tfQuoteChar = new JFormattedTextField();
		tfQuoteChar.setText(Character.toString(quoteChar));
		tfQuoteChar.addKeyListener(this);
		lh.add("Quote character", tfQuoteChar, true);
		tfSeparatorChar = new JFormattedTextField();
		tfSeparatorChar.setText(Character.toString(separatorChar));
		tfSeparatorChar.addKeyListener(this);
		lh.add("Separator character", tfSeparatorChar, true);
		JComboBox combo = new JComboBox(new String[] { "Linux", "Windows",
				"Mac OS" });
		combo.addItemListener(this);
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
		JSpinner paramStepSize = new JSpinner(spinModParameterStepSize);
		paramStepSize.addChangeListener(this);
		JSpinner maxCompVal = new JSpinner(spinModMaxCompartmentVal);
		maxCompVal.addChangeListener(this);
		lh.add("Step size", paramStepSize, true);
		lh.add("Max. Compartment value", maxCompVal, true);
		JSpinner maxSpecVal = new JSpinner(spinModMaxSpeciesVal);
		maxSpecVal.addChangeListener(this);
		JSpinner maxParamVal = new JSpinner(spinModMaxParameterVal);
		maxParamVal.addChangeListener(this);
		lh.add("Max. Species value", maxSpecVal, true);
		lh.add("Max. Param value", maxParamVal, true);
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
		ckbxLogScale.addItemListener(this);
		ckbxShowGrid.addItemListener(this);
		ckbxShowLegend.addItemListener(this);
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
	 * Set the properties of this panel.
	 * 
	 * @param settings
	 */
	public void setProperties(Properties settings) {
		this.settings = settings;
		/*
		 * Plot
		 */
		setLogScale(((Boolean) settings.get(CfgKeys.PLOT_LOG_SCALE))
				.booleanValue());
		setShowGrid(((Boolean) settings.get(CfgKeys.PLOT_SHOW_GRID))
				.booleanValue());
		setShowLegend(((Boolean) settings.get(CfgKeys.PLOT_SHOW_LEGEND))
				.booleanValue());
		/*
		 * Scan
		 */
		setMaxCompartmentValue(((Number) settings
				.get(CfgKeys.SIM_MAX_COMPARTMENT_SIZE)).doubleValue());
		setMaxSpeciesValue(((Number) settings
				.get(CfgKeys.SIM_MAX_SPECIES_VALUE)).doubleValue());
		setMaxParameterValue(((Number) settings
				.get(CfgKeys.SIM_MAX_PARAMETER_VALUE)).doubleValue());
		setParameterStepSize(((Number) settings.get(CfgKeys.SPINNER_STEP_SIZE))
				.doubleValue());
		/*
		 * Computing
		 */
		int solv = 0, dist = 0;
		Class<AbstractDESSolver> solvers[] = SBMLsqueezer.getAvailableSolvers();
		String name = settings.get(CfgKeys.SIM_ODE_SOLVER).toString();
		while (solv < cmbBxSolver.getItemCount()
				&& !solvers[solv].getName().equals(name))
			solv++;
		setSolver(solv);
		Class<Distance> dists[] = SBMLsqueezer.getAvailableDistances();
		name = settings.get(CfgKeys.SIM_DISTANCE_FUNCTION).toString();
		while (dist < cmbBxDistance.getItemCount()
				&& !dists[dist].getName().equals(name))
			dist++;
		setDistance(dist);
		setMaxTime(((Number) settings.get(CfgKeys.SIM_MAX_TIME)).doubleValue());
		double t1 = ((Number) settings.get(CfgKeys.SIM_START_TIME))
				.doubleValue();
		double t2 = ((Number) settings.get(CfgKeys.SIM_END_TIME)).doubleValue();
		setSimulationStartTime(t1);
		setSimulationEndTime(t2);
		setSpinnerMaxValue(((Number) settings.get(CfgKeys.SPINNER_MAX_VALUE))
				.doubleValue());
		setStepsPerUnitTime(((Integer) settings
				.get(CfgKeys.SIM_MAX_STEPS_PER_UNIT_TIME)).intValue());
		setNumIntegrationSteps((int) Math.round((t2 - t1)
				/ ((Number) settings.get(CfgKeys.SIM_STEP_SIZE)).doubleValue()));
		/*
		 * Parsing
		 */
		setOpenDir(settings.get(CfgKeys.CSV_FILES_OPEN_DIR).toString());
		setSaveDir(settings.get(CfgKeys.CSV_FILES_SAVE_DIR).toString());
		setQuoteChar(settings.get(CfgKeys.CSV_FILES_QUOTE_CHAR).toString()
				.charAt(0));
		setSeparatorChar(settings.get(CfgKeys.CSV_FILES_SEPARATOR_CHAR)
				.toString().charAt(0));
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
	 * @param separatorChar
	 *            the separatorChar to set
	 */
	public void setSeparatorChar(char separatorChar) {
		tfSeparatorChar.setText(Character.toString(separatorChar));
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