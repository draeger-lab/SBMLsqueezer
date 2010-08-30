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
package org.sbml.optimizer;

import org.sbml.jsbml.Model;
import org.sbml.jsbml.Quantity;
import org.sbml.jsbml.SBMLException;
import org.sbml.jsbml.validator.ModelOverdeterminedException;
import org.sbml.squeezer.math.Distance;
import org.sbml.squeezer.math.SBMLinterpreter;

import eva2.server.go.problems.AbstractProblemDouble;
import eva2.tools.math.des.DESSolver;
import eva2.tools.math.des.Data;

/**
 * @author Andreas Dr&auml;ger
 * @date 2010-08-24
 */
public class EstimationProblem extends AbstractProblemDouble {

	/**
	 * Generated version identifier.
	 */
	private static final long serialVersionUID = 8918650806005528506L;

	/**
	 * 
	 * @return
	 */
	public static long getSerialversionuid() {
		return serialVersionUID;
	}

	/**
	 * 
	 */
	private DESSolver solver = null;

	/**
	 * 
	 */
	private Distance distance = null;

	/**
	 * 
	 */
	private Quantity[] quantities = null;

	/**
	 * Memorizes the original values of all given {@link Quantity}s to restore
	 * the original state.
	 */
	private double[] originalValues = null;

	/**
	 * 
	 */
	private SBMLinterpreter interpreter = null;

	/**
	 * Reference data used to judge the quality of a simulation result.
	 */
	private Data referenceData = null;

	/**
	 * An array to store the fitness of a parameter set to avoid multiple
	 * allocations
	 */
	private transient double[] fitness = new double[1];

	/**
	 * 
	 * @param solver
	 * @param distance
	 * @param model
	 * @param referenceData
	 * @param quantities
	 * @throws ModelOverdeterminedException
	 * @throws SBMLException
	 */
	public EstimationProblem(DESSolver solver, Distance distance, Model model,
			Data referenceData, Quantity... quantities)
			throws ModelOverdeterminedException, SBMLException {
		super();
		setSolver(solver);
		setDistance(distance);
		setModel(model);
		setReferenceData(referenceData);
		setQuantities(quantities);
	}

	/**
	 * @param problem
	 */
	public EstimationProblem(EstimationProblem problem) {
		super(problem);
		setSolver(problem.getSolver());
		setDistance(problem.getDistance());
		try {
			setModel(problem.getModel());
		} catch (Exception e) {
			// can never happen.
		}
		setQuantities(problem.getQuantities());
	}

	/**
	 * Checks whether or not the given model contains all of the given
	 * quantities.
	 * 
	 * @param quantities
	 * @return
	 */
	private boolean check(Quantity... quantities) {
		for (Quantity q : quantities) {
			if (!interpreter.getModel().containsQuantity(q)) {
				return false;
			}
		}
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see eva2.server.go.problems.AbstractOptimizationProblem#clone()
	 */
	@Override
	public EstimationProblem clone() {
		return new EstimationProblem(this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see eva2.server.go.problems.AbstractProblemDouble#eval(double[])
	 */
	@Override
	public double[] eval(double[] x) {
		for (int i = 0; i < x.length; i++) {
			quantities[i].setValue(x[i]);
		}
		try {
			interpreter.init();
			fitness[0] = distance.distance(solver.solve(interpreter,
					interpreter.getInitialValues(), referenceData
							.getTimePoints()), referenceData);
		} catch (Exception e) {
			e.printStackTrace();
			fitness[0] = Double.POSITIVE_INFINITY;
		}
		return fitness;
	}

	/**
	 * 
	 * @return
	 */
	public Distance getDistance() {
		return distance;
	}

	/**
	 * 
	 * @return
	 */
	public Model getModel() {
		return interpreter.getModel();
	}

	/**
	 * 
	 * @return
	 */
	public double[] getOriginalValues() {
		return originalValues;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see eva2.server.go.problems.AbstractProblemDouble#getProblemDimension()
	 */
	@Override
	public int getProblemDimension() {
		return isSetQuantities() ? quantities.length : 0;
	}

	/**
	 * 
	 * @return
	 */
	public Quantity[] getQuantities() {
		return quantities;
	}

	/**
	 * The matrix of reference data containing the time points in the first
	 * column.
	 * 
	 * @return
	 */
	public Data getReferenceData() {
		return referenceData;
	}

	/**
	 * 
	 * @return
	 */
	public DESSolver getSolver() {
		return solver;
	}

	/**
	 * 
	 * @return
	 */
	public boolean isSetQuantities() {
		return quantities != null;
	}

	/**
	 * 
	 * @return
	 */
	public boolean isSetReferenceData() {
		return referenceData != null;
	}

	/**
	 * Restores the original values of the model as before the model was
	 * modified during the optimization.
	 */
	public void restore() {
		for (int i = 0; i < quantities.length; i++) {
			quantities[i].setValue(originalValues[i]);
		}
	}

	/**
	 * 
	 * @param distance
	 */
	public void setDistance(Distance distance) {
		this.distance = distance;
	}

	/**
	 * 
	 * @param model
	 * @throws SBMLException
	 * @throws ModelOverdeterminedException
	 */
	private void setModel(Model model) throws ModelOverdeterminedException,
			SBMLException {

	}

	/**
	 * 
	 * @param quantities
	 */
	public void setQuantities(Quantity... quantities) {
		if (check(quantities)) {
			this.quantities = quantities;
			this.originalValues = new double[quantities.length];
			for (int i = 0; i < originalValues.length; i++) {
				originalValues[i] = quantities[i].getValue();
			}
		} else {
			throw new IllegalArgumentException(
					"cannot estimate the values of quantities that are not part of the given model.");
		}
	}

	/**
	 * 
	 * @param referenceData
	 */
	public void setReferenceData(Data referenceData) {
		if ((referenceData != null)
				&& (referenceData.getColumnCount() != 1 /* time column */+ getModel()
						.getNumSymbols())) {
			throw new IllegalArgumentException(
					"unequal number of reference data and symbols in the model");
		}
		this.referenceData = referenceData;
	}

	/**
	 * 
	 * @param solver
	 */
	public void setSolver(DESSolver solver) {
		this.solver = solver;
	}

	/**
	 * 
	 */
	public void unsetQuantities() {
		quantities = null;
		originalValues = null;
	}

	/**
	 * 
	 */
	public void unsetReferenceData() {
		referenceData = null;
	}

}
