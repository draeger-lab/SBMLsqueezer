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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.LinkedList;
import java.util.List;

import javax.swing.AbstractCellEditor;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JColorChooser;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JToolBar;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

import org.sbml.jsbml.Compartment;
import org.sbml.jsbml.Model;
import org.sbml.jsbml.Parameter;
import org.sbml.jsbml.Species;
import org.sbml.jsbml.Symbol;
import org.sbml.squeezer.Reflect;
import org.sbml.squeezer.io.SBFileFilter;
import org.sbml.squeezer.math.SBMLinterpreter;

import au.com.bytecode.opencsv.CSVReader;
import eva2.gui.FunctionArea;
import eva2.tools.math.des.AbstractDESSolver;

/**
 * 
 * @since 1.4
 * 
 */
class ColorEditor extends AbstractCellEditor implements TableCellEditor,
		ActionListener {
	protected static final String EDIT = "edit";
	/**
	 * Generated serial version identifier.
	 */
	private static final long serialVersionUID = -3645125690115981580L;
	JButton button;
	JColorChooser colorChooser;
	Color currentColor;
	JDialog dialog;

	public ColorEditor() {
		button = new JButton();
		button.setActionCommand(EDIT);
		button.addActionListener(this);
		button.setBorderPainted(false);

		// Set up the dialog that the button brings up.
		colorChooser = new JColorChooser();
		dialog = JColorChooser.createDialog(button, "Pick a Color", true, // modal
				colorChooser, this, // OK button handler
				null); // no CANCEL button handler
	}

	public void actionPerformed(ActionEvent e) {
		if (EDIT.equals(e.getActionCommand())) {
			// The user has clicked the cell, so
			// bring up the dialog.
			button.setBackground(currentColor);
			colorChooser.setColor(currentColor);
			dialog.setVisible(true);

			fireEditingStopped(); // Make the renderer reappear.

		} else { // User pressed dialog's "OK" button.
			currentColor = colorChooser.getColor();
		}
	}

	// Implement the one CellEditor method that AbstractCellEditor doesn't.
	public Object getCellEditorValue() {
		return currentColor;
	}

	// Implement the one method defined by TableCellEditor.
	public Component getTableCellEditorComponent(JTable table, Object value,
			boolean isSelected, int row, int column) {
		currentColor = (Color) value;
		return button;
	}
}

/**
 * @author Andreas Dr&auml;ger
 * @since 1.4
 * @date 2010-04-06
 * 
 */
public class SimulationDialog extends JDialog implements ActionListener,
		ChangeListener, ItemListener {

	/**
	 * Commands that can be understood by this dialog.
	 * 
	 * @author Andreas Dr&auml;ger
	 * 
	 */
	public static enum Command {
		/**
		 * Open file with experimental data.
		 */
		OPEN_DATA,
		/**
		 * Start a new simulation with the current settings.
		 */
		SIMULATION_START
	}

	/**
	 * An array of all available ordinary differential equation solvers.
	 */
	private static Class<?> AVAILABLE_SOLVERS[];

	/**
	 * Generated serial version identifier
	 */
	private static final long serialVersionUID = -7278034514446047207L;

	/**
	 * The java package that contains differential equation solvers.
	 */
	private static String SOLVER_PACKAGE = "eva2.tools.math.des";
	static {
		Class<?> solvers[] = Reflect.getAllClassesInPackage(SOLVER_PACKAGE,
				true, true, AbstractDESSolver.class);
		List<Class<?>> impl = new LinkedList<Class<?>>();
		for (Class<?> c : solvers)
			if (!Modifier.isAbstract(c.getModifiers())) {
				impl.add(c);
			}
		AVAILABLE_SOLVERS = impl.toArray(new Class<?>[0]);
	}

	/**
	 * 
	 * @param index
	 * @return
	 */
	public static Color indexToColor(int index) {
		Color c = Color.black;
		int k = index % 10;
		switch (k) {
		case 0:
			c = Color.black;
			break;
		case 1:
			c = Color.red;
			break;
		case 2:
			c = Color.blue;
			break;
		case 3:
			c = Color.pink;
			break;
		case 4:
			c = Color.green;
			break;
		case 5:
			c = Color.gray;
			break;
		case 6:
			c = Color.magenta;
			break;
		case 7:
			c = Color.cyan;
			break;
		case 8:
			c = Color.orange;
			break;
		case 9:
			c = Color.darkGray;
			break;
		}
		return c;
	}

	/**
	 * The names in all tables for the columns (time, compartment, species,
	 * parameters).
	 */
	private String colNames[];
	/**
	 * Table for experimental data.
	 */
	private JTable expTable;

	/**
	 * The index of the class name of the solver to be used
	 */
	private int integrator;

	/**
	 * Table that contains the legend of this plot.
	 */
	private JTable legend;

	/**
	 * Maximal allowable number of integration steps per time unit
	 */
	private double maxStepsPerUnit;
	/**
	 * The maximal allowable simulation time
	 */
	private int maxTime;

	/**
	 * Model to be simulated
	 */
	private Model model;

	/**
	 * Standard directory to open data files.
	 */
	private String opendir;

	/**
	 * Plot area
	 */
	private FunctionArea plot;

	/**
	 * This is the quote character in CSV files
	 */
	private char quoteChar;

	/**
	 * This is the separator char in CSV files
	 */
	private char separatorChar;

	/**
	 * Decides whether or not a grid should be displayed in the plot.
	 */
	private boolean showGrid;

	/**
	 * Table for the simulation data.
	 */
	private JTable simTable;

	/**
	 * The integrator
	 */
	private AbstractDESSolver solver;

	/**
	 * Step size of the simulator
	 */
	private double stepSize;

	/**
	 * The spinner to change the number of integration steps.
	 */
	private SpinnerNumberModel stepsModel;

	/**
	 * Simulation start time
	 */
	private double t1;

	/**
	 * Simulation end time
	 */
	private double t2;

	/**
	 * The step size for the spinner in the interactive parameter scan.
	 */
	private double paramStepSize;

	/**
	 * The maximal allowable parameter value.
	 */
	private double maxParameterValue;

	/**
	 * 
	 * @param owner
	 * @param model
	 */
	public SimulationDialog(JFrame owner, Model model) {
		this(owner, model, 5, 0.1,
				"/home/draeger/workspace/SBMLsqueezer/files/tests/cases/semantic/00026");
		// System.getProperty("user.dir")
	}

	/**
	 * 
	 * @param owner
	 * @param model
	 * @param endTime
	 * @param stepSize
	 * @param openDir
	 */
	public SimulationDialog(JFrame owner, Model model, double endTime,
			double stepSize, String openDir) {
		super(owner);
		if (AVAILABLE_SOLVERS.length == 0)
			JOptionPane
					.showMessageDialog(
							owner,
							GUITools
									.toHTML("Could not find any solvers for differential equation systems. A simulation is therefore not possible."),
							"Warning", JOptionPane.WARNING_MESSAGE);
		else {
			// This should be included into our settings
			this.model = model;
			plot = new FunctionArea("time", "value");
			t1 = 0; // always for SBML!
			t2 = endTime;
			this.stepSize = stepSize;
			maxTime = 10000;
			integrator = 0;
			showGrid = true;
			maxStepsPerUnit = 500;
			this.opendir = openDir;
			separatorChar = ',';
			quoteChar = '\'';
			maxParameterValue = 10000;
			paramStepSize = 0.01;

			colNames = createColNames(model);

			JPanel simPanel = new JPanel(new BorderLayout());
			simTable = new JTable();
			simPanel.add(new JScrollPane(simTable,
					JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
					JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED),
					BorderLayout.CENTER);

			JPanel expPanel = new JPanel(new BorderLayout());
			expTable = new JTable();
			expPanel.add(new JScrollPane(expTable,
					JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
					JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED),
					BorderLayout.CENTER);

			JPanel legendPanel = new JPanel(new BorderLayout());
			legend = ledgendTable(model);
			legendPanel.add(new JScrollPane(legend,
					JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
					JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED),
					BorderLayout.CENTER);
			JPanel parameterPanel = parameterPanel(model);
			JSplitPane split = new JSplitPane(
					JSplitPane.HORIZONTAL_SPLIT,
					true,
					new JSplitPane(JSplitPane.VERTICAL_SPLIT, true,
							legendPanel, new JScrollPane(parameterPanel,
									JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
									JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED)),
					plot);

			JTabbedPane tabbedPane = new JTabbedPane();
			tabbedPane.add("Plot ", split);
			tabbedPane.add("Simulated data", simPanel);
			tabbedPane.add("Experimental data", expPanel);

			JToolBar toolbar = new JToolBar("Tools");
			toolbar.add(GUITools.createIconButton(GUITools.ICON_OPEN, this,
					Command.OPEN_DATA, "Load  experimental data from file."));

			setDefaultCloseOperation(DISPOSE_ON_CLOSE);
			setTitle("Simulation");
			setLayout(new BorderLayout());
			getContentPane().add(toolbar, BorderLayout.NORTH);
			getContentPane().add(tabbedPane, BorderLayout.CENTER);
			try {
				getContentPane().add(createFootPanel(), BorderLayout.SOUTH);
				pack();
				setLocationRelativeTo(owner);
				setVisible(true);
			} catch (Exception exc) {
				exc.printStackTrace();
				JOptionPane.showMessageDialog(getOwner(), exc.getMessage(), exc
						.getClass().getSimpleName(), JOptionPane.ERROR_MESSAGE);
				dispose();
			}
		}
	}

	/**
	 * 
	 * @param model
	 * @return
	 */
	private JPanel parameterPanel(Model model) {
		JPanel pPanel = new JPanel();
		LayoutHelper l = new LayoutHelper(pPanel);
		for (int i = 0; i < model.getNumParameters(); i++) {
			Parameter p = model.getParameter(i);
			l.add(new JLabel(p.isSetName() ? p.getName() : p.getId()), 0, i, 1,
					1, 0, 0);
			JSpinner spinner = new JSpinner(new SpinnerNumberModel(
					p.getValue(), 0, maxParameterValue, paramStepSize));
			spinner.setName(p.getId());
			spinner.addChangeListener(this);
			l.add(spinner, 3, i, 1, 1, 0, 0);
		}
		l.add(new JPanel(), 1, 0, 1, 1, 0, 0);
		pPanel.setBorder(BorderFactory.createTitledBorder(" Parameter scan "));
		return pPanel;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent e) {
		switch (Command.valueOf(e.getActionCommand())) {
		case SIMULATION_START:
			simulate(model);
			break;
		case OPEN_DATA:
			JFileChooser chooser = GUITools.createJFileChooser(opendir, false,
					false, JFileChooser.FILES_ONLY,
					SBFileFilter.CSV_FILE_FILTER);
			if (chooser.showOpenDialog(getOwner()) == JFileChooser.APPROVE_OPTION)
				try {
					plot(readCSVFile(chooser.getSelectedFile()), false);
				} catch (IOException exc) {
					exc.printStackTrace();
					JOptionPane.showMessageDialog(this, exc.getMessage(), exc
							.getClass().getSimpleName(),
							JOptionPane.ERROR_MESSAGE);
				}
			break;
		default:
			JOptionPane.showMessageDialog(this, "Invalid option "
					+ e.getActionCommand(), "Warning",
					JOptionPane.WARNING_MESSAGE);
			break;
		}
	}

	/**
	 * 
	 * @param model
	 * @return
	 */
	private String[] createColNames(Model model) {
		String colNames[] = new String[1 + model.getNumCompartments()
				+ model.getNumSpecies() + model.getNumParameters()];
		colNames[0] = "Time";
		int i;
		for (i = 1; i <= model.getNumCompartments(); i++) {
			Compartment c = model.getCompartment(i - 1);
			colNames[i] = c.isSetName() ? c.getName() : c.getId();
		}
		for (i = model.getNumCompartments() + 1; i <= model
				.getNumCompartments()
				+ model.getNumSpecies(); i++) {
			Species s = model.getSpecies(i - model.getNumCompartments() - 1);
			colNames[i] = s.isSetName() ? s.getName() : s.getId();
		}
		for (i = model.getNumCompartments() + model.getNumSpecies() + 1; i < colNames.length; i++) {
			Parameter p = model.getParameter(i - model.getNumCompartments()
					- model.getNumSpecies() - 1);
			colNames[i] = p.isSetName() ? p.getName() : p.getId();
		}
		return colNames;
	}

	/**
	 * 
	 * @return
	 * @throws NoSuchMethodException
	 * @throws SecurityException
	 * @throws InvocationTargetException
	 * @throws IllegalAccessException
	 * @throws InstantiationException
	 * @throws IllegalArgumentException
	 */
	private Component createFootPanel() throws SecurityException,
			NoSuchMethodException, IllegalArgumentException,
			InstantiationException, IllegalAccessException,
			InvocationTargetException {

		// Settings
		JSpinner startTime = new JSpinner(new SpinnerNumberModel(t1, 0,
				maxTime, stepSize));
		startTime.addChangeListener(this);
		startTime.setName("t1");
		startTime.setEnabled(false);
		JSpinner endTime = new JSpinner(new SpinnerNumberModel(t2, t1, maxTime,
				stepSize));
		endTime.addChangeListener(this);
		endTime.setName("t2");

		stepsModel = new SpinnerNumberModel((int) Math.round((t2 - t1)
				/ stepSize), 1, (int) Math.round((t2 - t1) * maxStepsPerUnit),
				1);

		JComboBox solvers = new JComboBox();
		for (Class<?> c : AVAILABLE_SOLVERS) {
			Constructor<?> constructor = c.getConstructor();
			solver = (AbstractDESSolver) constructor.newInstance();
			solvers.addItem(solver.getName());
		}
		solvers.setName("solvers");
		solvers.addItemListener(this);
		solvers.setSelectedIndex(integrator);
		if (solvers.getItemCount() == 1)
			solvers.setEnabled(false);

		JCheckBox gridCheckBox = new JCheckBox("Grid", showGrid);
		gridCheckBox.setName("grid");
		gridCheckBox.addChangeListener(this);

		JPanel sPanel = new JPanel();
		LayoutHelper settings = new LayoutHelper(sPanel);
		settings.add(new JLabel("Start time: "), 0, 0, 1, 1, 0, 0);
		settings.add(new JPanel(), 1, 0, 1, 1, 0, 0);
		settings.add(startTime, 3, 0, 1, 1, 0, 0);
		settings.add(new JPanel(), 4, 0, 1, 1, 0, 0);
		settings.add(new JLabel("End time: "), 5, 0, 1, 1, 0, 0);
		settings.add(new JPanel(), 6, 0, 1, 1, 0, 0);
		settings.add(endTime, 7, 0, 1, 1, 0, 0);
		settings.add(new JPanel(), 8, 0, 1, 1, 0, 0);
		settings.add(new JLabel("Steps: "), 9, 0, 1, 1, 0, 0);
		settings.add(new JPanel(), 10, 0, 1, 1, 0, 0);
		settings.add(new JSpinner(stepsModel), 11, 0, 5, 1, 0, 0);
		settings.add(new JPanel(), 0, 1, 1, 1, 0, 0);
		settings.add(new JLabel("ODE Solver: "), 0, 2, 1, 1, 0, 0);
		settings.add(solvers, 3, 2, 5, 1, 1, 0);
		settings.add(gridCheckBox, 9, 2, 2, 1, 1, 0);

		sPanel.setBorder(BorderFactory.createTitledBorder(" Settings "));

		// Actions
		JButton start = GUITools.createButton("Start", GUITools.ICON_GEAR_TINY,
				this, Command.SIMULATION_START,
				"Perform a simulation run with the current settings.");

		JPanel aPanel = new JPanel();
		aPanel.add(start);

		// Main
		JPanel mPanel = new JPanel(new BorderLayout());
		mPanel.add(sPanel, BorderLayout.CENTER);
		mPanel.add(aPanel, BorderLayout.SOUTH);
		return mPanel;
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
				try {
					Constructor<?> constructor = AVAILABLE_SOLVERS[box
							.getSelectedIndex()].getConstructor();
					solver = (AbstractDESSolver) constructor.newInstance();
				} catch (Exception exc) {
					// Can actually not happen, because everything has already
					// been initialized once before.
					exc.printStackTrace();
				}
		}
	}

	/**
	 * 
	 * @param model
	 * @return
	 */
	private JTable ledgendTable(final Model model) {
		JTable tab = new JTable();
		tab.setModel(new TableModelLedgend(model));
		tab.setDefaultEditor(Color.class, new ColorEditor());
		tab.setDefaultRenderer(Color.class, new TableCellRendererLedgend());
		tab.setDefaultRenderer(Symbol.class, new TableCellRendererLedgend());
		return tab;
	}

	/**
	 * Plots a matrix either by displaying unconnected or connected points
	 * depending on the connected parameter.
	 * 
	 * @param solution
	 *            The solution of a simulation, where the first column is
	 *            assumed to contain the simulation time.
	 * @param connected
	 *            If true, all points will be connected, singular points are
	 *            plotted for false.
	 */
	private void plot(double[][] solution, boolean connected) {
		if (solution.length > 0) {
			// int from = model.getNumCompartments();
			// int to = from + model.getNumSpecies();
			int from = 0, to = solution[0].length - 1;
			for (int i = 0; i < solution.length; i++)
				for (int j = from; j < to; j++) {
					if (connected)
						plot.setConnectedPoint(solution[i][0],
								solution[i][j + 1], j);
					else {
						plot.setUnconnectedPoint(solution[i][0],
								solution[i][j + 1], colNames.length - 1 + j);
					}
				}
		}
	}

	/**
	 * 
	 * @param file
	 * @throws IOException
	 */
	@SuppressWarnings("unchecked")
	private double[][] readCSVFile(File file) throws IOException {
		CSVReader csvreader = new CSVReader(new FileReader(file),
				separatorChar, quoteChar, 0);
		List<String[]> input = csvreader.readAll();
		csvreader.close();
		if (input.size() > 0) {
			int i, j;
			short numNumbers = 0;
			String names[] = input.get(0);
			for (i = 0; i < names.length; i++)
				try {
					numNumbers++;
					Double.parseDouble(names[i]);
				} catch (NumberFormatException exc) {
					numNumbers--;
				}
			TableModelDoubleMatrix tabModel = new TableModelDoubleMatrix();
			if (numNumbers == 0)
				tabModel.setColumnNames(input.remove(0));
			double data[][] = new double[input.size()][input.get(0).length];
			for (i = 0; i < data.length; i++)
				for (j = 0; j < data[i].length; j++)
					data[i][j] = Double.valueOf(input.get(i)[j]).doubleValue();
			tabModel.setData(data);
			this.expTable.setModel(tabModel);
			return data;
		}
		return new double[0][0];
	}

	/**
	 * 
	 */
	private void simulate(Model model) {
		stepSize = (t2 - t1) / stepsModel.getNumber().doubleValue();
		TableModelDoubleMatrix tabMod = new TableModelDoubleMatrix(
				solve(model), createColNames(model));
		simTable.setModel(tabMod);
		plot.clearAll();
		plot(tabMod.getData(), true);
		if (expTable.getColumnCount() > 0)
			plot(((TableModelDoubleMatrix) expTable.getModel()).getData(),
					false);
	}

	/**
	 * 
	 * @return
	 */
	private double[][] solve(Model model) {
		SBMLinterpreter interpreter = new SBMLinterpreter(model);
		solver.setStepSize(stepSize);
		double solution[][] = solver.solveByStepSizeIncludingTime(interpreter,
				interpreter.getInitialValues(), t1, t2);
		if (solver.isUnstable()) {
			JOptionPane.showMessageDialog(getOwner(), "Unstable!",
					"Simulation not possible", JOptionPane.WARNING_MESSAGE);
			dispose();
		}
		return solution;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * javax.swing.event.ChangeListener#stateChanged(javax.swing.event.ChangeEvent
	 * )
	 */
	public void stateChanged(ChangeEvent e) {
		if (e.getSource() instanceof JSpinner) {
			JSpinner spin = (JSpinner) e.getSource();
			if (spin.getName() != null && spin.getName().equals("t2")) {
				t2 = Double.valueOf(spin.getValue().toString()).doubleValue();
				stepsModel.setMinimum(1);
				stepsModel.setMaximum(Integer.valueOf((int) Math
						.round((t2 - t1) * maxStepsPerUnit)));
				stepsModel.setValue(Math.max(Integer.valueOf((int) Math
						.round((t2 - t1) * stepSize)), ((Integer) stepsModel
						.getMinimum()).intValue()));
			} else if (model.getParameter(spin.getName()) != null) {
				Parameter p = model.getParameter(spin.getName());
				p.setValue(((SpinnerNumberModel) spin.getModel()).getNumber()
						.doubleValue());
			}
		} else if (e.getSource() instanceof JCheckBox) {
			JCheckBox chck = (JCheckBox) e.getSource();
			if (chck.getName().equals("grid")) {
				plot.setGridVisible(chck.isSelected());
			}
		}
	}
}

/**
 * 
 * @author Andreas Dr&auml;ger
 * @since 1.4
 * @date 2010-04-07
 */
class TableCellRendererLedgend extends JLabel implements TableCellRenderer {

	/**
	 * Generated serial version identifier
	 */
	private static final long serialVersionUID = 5233542392522297524L;

	/**
	 * 
	 */
	public TableCellRendererLedgend() {
		super();
		setOpaque(true);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * javax.swing.table.TableCellRenderer#getTableCellRendererComponent(javax
	 * .swing.JTable, java.lang.Object, boolean, boolean, int, int)
	 */
	public Component getTableCellRendererComponent(JTable table, Object value,
			boolean isSelected, boolean hasFocus, int row, int column) {
		if (value instanceof Color) {
			Color newColor = (Color) value;
			setToolTipText("RGB value: " + newColor.getRed() + ", "
					+ newColor.getGreen() + ", " + newColor.getBlue());
			setBackground(newColor);
		} else if (value instanceof Symbol) {
			Symbol s = (Symbol) value;
			setText(s.isSetName() ? s.getName() : s.getId());
			setBackground(Color.WHITE);
		} else
			setText(value.toString());
		return this;
	}

}

/**
 * 
 * @author Andreas Dr&auml;ger
 * @since 1.4
 * @date 2010-04-07
 * 
 */
class TableModelLedgend extends AbstractTableModel {

	/**
	 * Generated serial version identifier.
	 */
	private static final long serialVersionUID = 7360401460080111135L;
	/**
	 * A colored button for each model component and
	 */
	private Object[][] data;

	/**
	 * 
	 * @param model
	 */
	public TableModelLedgend(Model model) {
		data = new Object[model.getNumCompartments() + model.getNumSpecies()
				+ model.getNumParameters()][3];
		int i, j;
		for (i = 0; i < model.getNumCompartments(); i++) {
			data[i][0] = Boolean.TRUE;
			data[i][1] = SimulationDialog.indexToColor(i);
			data[i][2] = model.getCompartment(i);
		}
		j = model.getNumCompartments();
		for (i = 0; i < model.getNumSpecies(); i++) {
			data[i + j][0] = Boolean.TRUE;
			data[i + j][1] = SimulationDialog.indexToColor(i + j);
			data[i + j][2] = model.getSpecies(i);
		}
		j = model.getNumCompartments() + model.getNumSpecies();
		for (i = 0; i < model.getNumParameters(); i++) {
			data[i + j][0] = Boolean.TRUE;
			data[i + j][1] = SimulationDialog.indexToColor(i + j);
			data[i + j][2] = model.getParameter(i);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.table.AbstractTableModel#getColumnClass(int)
	 */
	@Override
	public Class<?> getColumnClass(int c) {
		return getValueAt(0, c).getClass();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.table.TableModel#getColumnCount()
	 */
	public int getColumnCount() {
		return data.length > 0 ? data[0].length : 0;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.table.AbstractTableModel#getColumnName(int)
	 */
	@Override
	public String getColumnName(int column) {
		if (column == 0)
			return "Plot";
		if (column == 1)
			return "Color";
		if (column == 2)
			return "State";
		throw new IndexOutOfBoundsException("Only " + getColumnCount()
				+ " columns, no column " + column);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.table.TableModel#getRowCount()
	 */
	public int getRowCount() {
		return data.length;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.table.TableModel#getValueAt(int, int)
	 */
	public Object getValueAt(int rowIndex, int columnIndex) {
		return data[rowIndex][columnIndex];
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.table.AbstractTableModel#isCellEditable(int, int)
	 */
	@Override
	public boolean isCellEditable(int rowIndex, int columnIndex) {
		if (columnIndex < 2 && rowIndex < getRowCount())
			return true;
		return false;
	}

	/*
	 * 
	 */
	@Override
	public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
		data[rowIndex][columnIndex] = aValue;
		fireTableCellUpdated(rowIndex, columnIndex);
	}
}