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
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.BoundedRangeModel;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.sbml.jsbml.Model;
import org.sbml.squeezer.math.SBMLinterpreter;

import eva2.gui.FunctionArea;
import eva2.tools.math.des.AbstractDESSolver;
import eva2.tools.math.des.RKEventSolver;

/**
 * @author Andreas Dr&auml;ger
 * @since 1.4
 * @date 2010-04-06
 * 
 */
public class SimulationDialog extends JDialog implements ActionListener,
		ChangeListener {

	/**
	 * Generated serial version identifier
	 */
	private static final long serialVersionUID = -7278034514446047207L;
	/**
	 * Model to be simulated
	 */
	private Model model;
	/**
	 * Plot area
	 */
	private FunctionArea plot;
	/**
	 * Step size of the simulator
	 */
	private double stepSize;
	/**
	 * Simulation start time
	 */
	private double t1;
	/**
	 * Simulation end time
	 */
	private double t2;
	/**
	 * The integrator
	 */
	private AbstractDESSolver rk;
	/**
	 * The maximal allowable simulation time
	 */
	private int maxTime;
	/**
	 * The number of steps
	 */
	private JSlider steps;

	/**
	 * Commands that can be understood by this dialog.
	 * 
	 * @author Andreas Dr&auml;ger
	 * 
	 */
	public static enum Command {
		/**
		 * Start a new simulation with the current settings.
		 */
		SIMULATION_START
	}

	/**
	 * 
	 * @param owner
	 * @param model
	 * @param endTime
	 * @param stepSize
	 */
	public SimulationDialog(JFrame owner, Model model, double endTime,
			double stepSize) {
		super(owner);
		this.model = model;
		plot = new FunctionArea("time", "value");
		t1 = 0; // always for SBML!
		t2 = endTime;
		this.stepSize = stepSize;
		rk = new RKEventSolver();
		maxTime = 10000;

		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		setTitle("Simulation");
		setLayout(new BorderLayout());
		getContentPane().add(plot, BorderLayout.CENTER);
		getContentPane().add(createFootPanel(), BorderLayout.SOUTH);
		pack();
		setLocationRelativeTo(owner);
		setVisible(true);
	}

	/**
	 * 
	 * @param owner
	 * @param model
	 */
	public SimulationDialog(JFrame owner, Model model) {
		this(owner, model, 2, 0.1);
	}

	/**
	 * 
	 * @return
	 */
	private Component createFootPanel() {

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

		steps = new JSlider(JSlider.HORIZONTAL);
		steps.setMinimum(1);
		steps.setMaximum((int) Math.round((t2 - t1) / stepSize));
		steps.setValue((int) Math.round((t2 - t1) * stepSize));
		steps.setName("steps");

		JPanel sPanel = new JPanel();
		LayoutHelper settings = new LayoutHelper(sPanel);
		settings.add(new JLabel("Start time: "), 0, 0, 1, 1, 0, 0);
		settings.add(new JPanel(), 1, 0, 1, 1, 0, 0);
		settings.add(startTime, 3, 0, 1, 1, 0, 0);
		settings.add(new JPanel(), 4, 0, 1, 1, 0, 0);
		settings.add(new JLabel("End time: "), 5, 0, 1, 1, 0, 0);
		settings.add(new JPanel(), 6, 0, 1, 1, 0, 0);
		settings.add(endTime, 7, 0, 1, 1, 0, 0);
		settings.add(new JPanel(), 0, 1, 1, 1, 0, 0);
		settings.add(new JLabel("Steps: "), 0, 2, 1, 1, 0, 0);
		settings.add(steps, 3, 2, 5, 1, 0, 0);

		sPanel.setBorder(BorderFactory.createTitledBorder(" Settings "));

		// Actions
		JButton start = new JButton("Start");
		start.setActionCommand(Command.SIMULATION_START.toString());
		start.addActionListener(this);

		JPanel aPanel = new JPanel();
		aPanel.add(start);

		// Main
		JPanel mPanel = new JPanel(new BorderLayout());
		mPanel.add(sPanel, BorderLayout.CENTER);
		mPanel.add(aPanel, BorderLayout.SOUTH);
		return mPanel;
	}

	/**
	 * 
	 * @return
	 */
	private double[][] solve() {
		SBMLinterpreter interpreter = new SBMLinterpreter(model);
		rk.setStepSize(stepSize);
		double solution[][] = rk.solveByStepSize(interpreter, interpreter
				.getInitialValues(), t1, t2);
		if (rk.isUnstable()) {
			JOptionPane.showMessageDialog(getOwner(), "Unstable!",
					"Simulation not possible", JOptionPane.WARNING_MESSAGE);
			dispose();
		}
		return solution;
	}

	/**
	 * 
	 * @param solution
	 */
	private void plot(double[][] solution) {
		plot.clearAll();
		int from = model.getNumCompartments();
		int to = from + model.getNumSpecies();
		double time = t1;
		for (int i = 0; i < solution.length; i++) {
			double[] symbol = solution[i];
			for (int j = from; j < to; j++) {

				double sym = symbol[j];
				plot.setConnectedPoint(time, sym, j);

			}
			time += rk.getStepSize();
		}
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
				stepSize = (t2 - t1) / steps.getValue();
			plot(solve());
			break;
		default:
			break;
		}
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
				steps.setMinimum(1);
				steps.setMaximum((int) Math.round((t2 - t1) / stepSize));
				steps.setValue((int) Math.round((t2 - t1) * stepSize));
			}
		}
	}
}
