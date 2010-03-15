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
package org.sbml.squeezer.math;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;

import org.sbml.jsbml.ASTNode;
import org.sbml.jsbml.ASTNodeCompiler;
import org.sbml.jsbml.AssignmentRule;
import org.sbml.jsbml.Compartment;
import org.sbml.jsbml.Event;
import org.sbml.jsbml.FunctionDefinition;
import org.sbml.jsbml.InitialAssignment;
import org.sbml.jsbml.KineticLaw;
import org.sbml.jsbml.ListOf;
import org.sbml.jsbml.Model;
import org.sbml.jsbml.NamedSBase;
import org.sbml.jsbml.Parameter;
import org.sbml.jsbml.RateRule;
import org.sbml.jsbml.Reaction;
import org.sbml.jsbml.Rule;
import org.sbml.jsbml.Species;
import org.sbml.jsbml.SpeciesReference;
import org.sbml.jsbml.Symbol;

import eva2.tools.math.des.DESAssignment;
import eva2.tools.math.des.EventDESystem;

/**
 * <p>
 * This DifferentialEquationSystem takes a model in SBML format and maps it to a
 * data structure that is understood by the {@see RKSolver} of JavaEvA.
 * Therefore, this class implements all necessary functions expected by SBML.
 * </p>
 * <p>
 * Copyright (c) ZBiT, University of T&uuml;bingen, Germany Compiler: JDK 1.6.0
 * </p>
 * 
 * @since 1.4
 * @version
 * @author Andreas Dr&auml;ger (draeger) <andreas.draeger@uni-tuebingen.de>
 * @author Dieudonne Motsou Wouamba <dwouamba@yahoo.fr>
 * @date Sep 6, 2007
 */
public class SBMLinterpreter implements ASTNodeCompiler, EventDESystem {

	/**
	 * Stores initial values of symbols changed due to initial assignments.
	 * 
	 * @author Andreas Dr&auml;ger <a
	 *         href="mailto:andreas.draeger@uni-tuebingen.de"
	 *         >andreas.draeger@uni-tuebingen.de</a>
	 * 
	 */
	private class Value {
		int index;
		double value;

		public Value(Double value) {
			setValue(value);
		}

		/**
		 * 
		 * @param index
		 * @param value
		 */
		public Value(int index) {
			setIndex(index);

		}

		public int getIndex() {
			return index;
		}

		public double getValue() {
			return value;
		}

		public void setIndex(Integer index) {
			this.index = index;
			this.value = Double.NaN;
		}

		public void setValue(double value) {
			this.value = value;
			this.index = -1;
		}

	}

	/**
	 * Generated serial version UID
	 */
	private static final long serialVersionUID = 3453063382705340995L;

	/**
	 * This field is necessary to also consider local parameters of the current
	 * reaction because it is not possible to access these parameters from the
	 * model. Hence we have to memorize an additional reference to the Reaction
	 * and thus to the list of these parameters.
	 */
	protected Reaction currentReaction;

	/**
	 * Holds the current time of the simulation
	 */
	private double currentTime;

	/**
	 * This array data structure stores at the position i if the ith event has
	 * fired recently
	 */
	private boolean[] eventFired;

	/**
	 * Contains a list of all DESAssignment that emerged due to events and 
	 * have not been processed so far
	 */
	private ArrayList<DESAssignment> events;

	/**
	 * An array, which stores all computed initial values of the model. If this
	 * model does not contain initial assignments, the initial values will only
	 * be taken once from the information stored in the model. Otherwise they
	 * have to be computed again as soon as the parameter values of this model
	 * are changed, because the parameters may influence the return values of
	 * the initial assignments.
	 */
	protected double[] initialValues;

	/**
	 * An array, which stores for each constraint the list of times, in which
	 * the constraint was violated during the simulation.
	 */
	protected List<Double>[] listOfContraintsViolations;

	/**
	 * The model to be simulated.
	 */
	protected Model model;

	/**
	 * This array is to avoid to allocate memory repeatedly. It stores the
	 * values computed during the linear combination of velocities. These values
	 * are passed to the array Y afterwards.
	 */
	protected double[] swap;

	/**
	 * An array of the velocities of each reaction within the model system.
	 * Holding this globally saves many new memory allocations during simulation
	 * time.
	 */
	protected double[] v;

	/**
	 * Hashes the name of a species to an value object which contains the
	 * position in the Y vector
	 */
	private HashMap<String, Value> valuesHash;

	/**
	 * Hashes a DESAssignment to an ASTNode containing the
	 * mathematical expression of this assignment when the
	 * mathematical expression of this DESAssignment has to be processed
	 * at a later timepoint in the simulation
	 */
	private HashMap<DESAssignment, ASTNode> eventMath;

	/**
	 * An array of the current concentration of each species within the model
	 * system.
	 */
	protected double[] Y;

	/**
	 * <p>
	 * This constructs a new DifferentialEquationSystem for the given SBML
	 * model. Note that only a maximum of <code>Integer.MAX_VALUE</code> species
	 * can be simulated. If the model contains more species, this class is not
	 * applicable.
	 * </p>
	 * <p>
	 * Note that currently, units are not considered.
	 * </p>
	 * 
	 * @param model
	 */
	public SBMLinterpreter(Model model) {
		this.model = model;
		// this.speciesIdIndex = new HashMap<String, Integer>();
		this.v = new double[this.model.getListOfReactions().size()];
		this.init();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.jsbml.ASTNodeCompiler#abs(org.sbml.jsbml.ASTNode)
	 */
	public Double abs(ASTNode node) {
		return Double.valueOf(Math.abs(toDouble(node.compile(this))));
	}

	public Boolean and(ASTNode... nodes) {
		for (ASTNode node : nodes) {
			if (toBoolean(node.compile(this)) == getConstantFalse())
				return getConstantFalse();

		}
		return getConstantTrue();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.jsbml.ASTNodeCompiler#arccos(org.sbml.jsbml.ASTNode)
	 */
	public Double arccos(ASTNode node) {
		return Double.valueOf(Math.acos(toDouble(node.compile(this))));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.jsbml.ASTNodeCompiler#arccosh(org.sbml.jsbml.ASTNode)
	 */
	public Double arccosh(ASTNode node) {
		return Functions.arccosh(toDouble(node.compile(this)));

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.jsbml.ASTNodeCompiler#arccot(org.sbml.jsbml.ASTNode)
	 */
	public Double arccot(ASTNode node) {
		return Functions.arccot(toDouble(node.compile(this)));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.jsbml.ASTNodeCompiler#arccoth(org.sbml.jsbml.ASTNode)
	 */
	public Double arccoth(ASTNode node) {
		return Functions.arccoth(toDouble(node.compile(this)));

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.jsbml.ASTNodeCompiler#arccsc(org.sbml.jsbml.ASTNode)
	 */
	public Double arccsc(ASTNode node) {
		return Functions.arccsc(toDouble(node.compile(this)));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.jsbml.ASTNodeCompiler#arccsch(org.sbml.jsbml.ASTNode)
	 */
	public Double arccsch(ASTNode node) {
		return Functions.arccsch(toDouble(node.compile(this)));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.jsbml.ASTNodeCompiler#arcsec(org.sbml.jsbml.ASTNode)
	 */
	public Double arcsec(ASTNode node) {
		return Functions.arcsec(toDouble(node.compile(this)));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.jsbml.ASTNodeCompiler#arcsech(org.sbml.jsbml.ASTNode)
	 */
	public Double arcsech(ASTNode node) {
		return Functions.arcsech(toDouble(node.compile(this)));

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.jsbml.ASTNodeCompiler#arcsin(org.sbml.jsbml.ASTNode)
	 */
	public Double arcsin(ASTNode node) {
		return Double.valueOf(Math.asin(toDouble(node.compile(this))));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.jsbml.ASTNodeCompiler#arcsinh(org.sbml.jsbml.ASTNode)
	 */
	public Double arcsinh(ASTNode node) {
		return Functions.arcsinh(toDouble(node.compile(this)));

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.jsbml.ASTNodeCompiler#arctan(org.sbml.jsbml.ASTNode)
	 */
	public Double arctan(ASTNode node) {
		return Double.valueOf(Math.atan(toDouble(node.compile(this))));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.jsbml.ASTNodeCompiler#arctanh(org.sbml.jsbml.ASTNode)
	 */
	public Double arctanh(ASTNode node) {
		return Functions.arctanh(toDouble(node.compile(this)));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.jsbml.ASTNodeCompiler#ceiling(org.sbml.jsbml.ASTNode)
	 */
	public Double ceiling(ASTNode node) {
		return Double.valueOf(Math.ceil(toDouble(node.compile(this))));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.jsbml.ASTNodeCompiler#compile(org.sbml.jsbml.Compartment)
	 */
	public Double compile(Compartment c) {
		return Double.valueOf(c.getSize());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.jsbml.ASTNodeCompiler#compile(double)
	 */
	public Double compile(double arg0) {
		return Double.valueOf(arg0);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.jsbml.ASTNodeCompiler#compile(int)
	 */
	public Integer compile(int arg0) {
		return Integer.valueOf(arg0);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.jsbml.ASTNodeCompiler#compile(org.sbml.jsbml.NamedSBase)
	 */
	public Double compile(NamedSBase nsb) {
		Value speciesVal;
		if (nsb instanceof Species) {
			Species s = (Species) nsb;
			Value compVal = valuesHash.get(s.getCompartment());
			speciesVal = valuesHash.get(nsb.getId());
			int dim = s.getCompartmentInstance().getSpatialDimensions();
			if (Y[compVal.getIndex()] == 0d)
				return Y[speciesVal.getIndex()];
			if (s.isSetInitialAmount() && !s.getHasOnlySubstanceUnits())				
				//return Y[speciesVal.getIndex()] / Functions.root(Y[compVal.getIndex()],dim);
				return Y[speciesVal.getIndex()] / Y[compVal.getIndex()];
			if (s.isSetInitialConcentration() && s.getHasOnlySubstanceUnits())			
				return Y[speciesVal.getIndex()] * Y[compVal.getIndex()];
			return Y[speciesVal.getIndex()];
		}

		else if (nsb instanceof Compartment || nsb instanceof Parameter) {
			if (nsb instanceof Parameter) {
				Parameter p = (Parameter) nsb;
				if (p.getParentSBMLObject() instanceof KineticLaw) {
					ListOf<Parameter> params = ((KineticLaw) p
							.getParentSBMLObject()).getListOfParameters();
					for (int i = 0; i < params.size(); i++) {
						if (p.getId() == params.get(i).getId())
							return params.get(i).getValue();
					}
				}
			}
			speciesVal = valuesHash.get(nsb.getId());
			return Y[speciesVal.getIndex()];

		} else if (nsb instanceof FunctionDefinition)
			return function((FunctionDefinition) nsb);
		else if (nsb instanceof Reaction) {
			Reaction r = (Reaction) nsb;
			if (r.isSetKineticLaw())
				return (Double) r.getKineticLaw().getMath().compile(this);
		}
		return Double.NaN;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.jsbml.ASTNodeCompiler#compile(java.lang.String)
	 */
	public String compile(String arg0) {
		return String.valueOf(arg0);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.jsbml.ASTNodeCompiler#cos(org.sbml.jsbml.ASTNode)
	 */
	public Double cos(ASTNode node) {
		return Double.valueOf(Math.cos(toDouble(node.compile(this))));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.jsbml.ASTNodeCompiler#cosh(org.sbml.jsbml.ASTNode)
	 */
	public Double cosh(ASTNode node) {
		return Double.valueOf(Math.cosh(toDouble(node.compile(this))));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.jsbml.ASTNodeCompiler#cot(org.sbml.jsbml.ASTNode)
	 */
	public Double cot(ASTNode node) {
		return Functions.cot(toDouble(node.compile(this)));

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.jsbml.ASTNodeCompiler#coth(org.sbml.jsbml.ASTNode)
	 */
	public Double coth(ASTNode node) {
		return Functions.coth(toDouble(node.compile(this)));

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.jsbml.ASTNodeCompiler#csc(org.sbml.jsbml.ASTNode)
	 */
	public Double csc(ASTNode node) {
		return Functions.csc(toDouble(node.compile(this)));

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.jsbml.ASTNodeCompiler#csch(org.sbml.jsbml.ASTNode)
	 */
	public Double csch(ASTNode node) {
		return Functions.csch(toDouble(node.compile(this)));

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.jsbml.ASTNodeCompiler#delay(org.sbml.jsbml.ASTNode, double)
	 */
	public Double delay(ASTNode x, double d) {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * 
	 * @param as
	 * @param Y
	 */
	private void evaluateAssignmentRule(AssignmentRule as, double Y[]) {
		int speciesIndex;
		speciesIndex = valuesHash.get(as.getVariable()).getIndex();
		Y[speciesIndex] = evaluateToDouble(as.getMath());
	}

	/**
	 * 
	 * @param rr
	 * @param res
	 */
	private void evaluateRateRule(RateRule rr, double changeRate[]) {
		int speciesIndex;
		
		Value compVal = valuesHash.get(model.getSpecies(rr.getVariable()).getCompartment());
		
		speciesIndex = valuesHash.get(rr.getVariable()).getIndex();
		changeRate[speciesIndex] = evaluateToDouble(rr.getMath());
		changeRate[speciesIndex] = changeRate[speciesIndex] * Y[compVal.getIndex()];

	}

	/**
	 * TODO: comment missing
	 * 
	 * @param ast
	 * @return
	 */
	protected boolean evaluateToBoolean(ASTNode ast) {
		return Boolean.valueOf((Boolean) ast.compile(this));
	}

	/**
	 * Executes the mathematics of any ASTNode and returns the result.
	 * 
	 * @param astnode
	 *            A tree data structure representing the mathematics stored in
	 *            an SBML file.
	 * @return a real number that is the result of the mathematics described by
	 *         this ASTNode. This function return 1.0 if the evaluation of a
	 *         logical expchangeRatesion is TRUE and 0.0 otherwise.
	 */
	protected double evaluateToDouble(ASTNode astnode) {
		return toDouble(astnode.compile(this));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.jsbml.ASTNodeCompiler#exp(org.sbml.jsbml.ASTNode)
	 */
	public Double exp(ASTNode node) {
		return Double.valueOf(Math.exp(toDouble(node.compile(this))));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.jsbml.ASTNodeCompiler#factorial(org.sbml.jsbml.ASTNode)
	 */
	public Double factorial(ASTNode node) {
		return Double
				.valueOf(Functions.factorial(toDouble(node.compile(this))));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.jsbml.ASTNodeCompiler#floor(org.sbml.jsbml.ASTNode)
	 */
	public Double floor(ASTNode node) {
		return Double.valueOf(Math.floor(toDouble(node.compile(this))));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.jsbml.ASTNodeCompiler#frac(org.sbml.jsbml.ASTNode,
	 * org.sbml.jsbml.ASTNode)
	 */
	public Double frac(ASTNode nodeleft, ASTNode noderight) {
		return Double.valueOf(toDouble(nodeleft.compile(this))
				/ toDouble(noderight.compile(this)));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.jsbml.ASTNodeCompiler#frac(int, int)
	 */
	public Double frac(int arg0, int arg1) {
		return Double.valueOf((double) arg0 / (double) arg1);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.jsbml.ASTNodeCompiler#function(java.lang.String,
	 * org.sbml.jsbml.ASTNode[])
	 */
	public Double function(FunctionDefinition function, ASTNode... arguments) {
		ASTNode lambda = function.getMath();
		Hashtable<String, Double> args = new Hashtable<String, Double>();
		for (int i = 0; i < arguments.length; i++)
			args.put(lambda.getChild(i).compile(this).toString(),
					toDouble(arguments[i].compile(this)));

		return toDouble(replace(lambda.getRightChild().clone(), args).compile(
				this));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.jsbml.ASTNodeCompiler#getConstantE()
	 */
	public Double getConstantE() {
		return Double.valueOf(Math.E);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.jsbml.ASTNodeCompiler#getConstantFalse()
	 */
	public Boolean getConstantFalse() {
		return Boolean.FALSE;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.jsbml.ASTNodeCompiler#getConstantPi()
	 */
	public Double getConstantPi() {
		return Double.valueOf(Math.PI);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.jsbml.ASTNodeCompiler#getConstantTrue()
	 */
	public Boolean getConstantTrue() {
		return Boolean.TRUE;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * javaeva.server.oa.go.OptimizationProblems.InferenceRegulatoryNetworks
	 * .Des.DESystem#getDESystemDimension()
	 */
	public int getDESystemDimension() {
		return this.initialValues.length;
	}

	/**
	 * Returns the initial values of the model to be simulated.
	 * 
	 * @return Returns the initial values of the model to be simulated.
	 */
	public double[] getInitialValues() {
		return this.initialValues;
	}

	/**
	 * Returns the model that is used by this object.
	 * 
	 * @return Returns the model that is used by this object.
	 */
	public Model getModel() {
		return model;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.jsbml.ASTNodeCompiler#getNegativeInfinity()
	 */
	public Double getNegativeInfinity() {
		return Double.valueOf(Double.NEGATIVE_INFINITY);
	}

	/**
	 * This method tells you the complete number of parameters within the model.
	 * It counts the global model parameters and all local parameters
	 * (parameters within a kinetic law).
	 * 
	 * @return The total number of model parameters. Note that this number is
	 *         limited to an <code>int</code> value, whereas the SBML model may
	 *         contain <code>int</code> values.
	 */
	public int getNumParameters() {
		int p = (int) model.getNumParameters();
		for (int i = 0; i < model.getNumReactions(); i++) {
			KineticLaw k = model.getReaction(i).getKineticLaw();
			if (k != null)
				p += k.getNumParameters();
		}
		return p;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.jsbml.ASTNodeCompiler#getPositiveInfinity()
	 */
	public Double getPositiveInfinity() {
		return Double.valueOf(Double.POSITIVE_INFINITY);
	}

	/**
	 * 
	 * @return
	 */
	public double getTime() {
		return currentTime;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * javaeva.server.oa.go.OptimizationProblems.InferenceRegulatoryNetworks
	 * .Des.DESystem#getValue(double, double[])
	 */
	public double[] getValue(double time, double[] Y) {
		int i;
		this.currentTime = time;
		double changeRate[] = new double[Y.length];
		this.Y = Y;

		processVelocities(changeRate);

		processRules(changeRate);

		/*
		 * Checking the Constraints
		 */
		for (i = 0; i < (int) model.getNumConstraints(); i++)
			if (evaluateToBoolean(model.getConstraint(i).getMath()))
				listOfContraintsViolations[i].add(time);

		return changeRate;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * javaeva.server.oa.go.OptimizationProblems.InferenceRegulatoryNetworks
	 * .Des.DESystem#getValue(double, double[], double[])
	 */
	public void getValue(double time, double[] Y, double[] changeRate) {
		System
				.arraycopy(getValue(time, Y), 0, changeRate, 0,
						changeRate.length);
	}

	/**
	 * <p>
	 * This method initializes the differential equation system for simulation.
	 * In more detail: the initial amounts or concentration will be assigned to
	 * every species or initialAssignments if any are executed.
	 * </p>
	 * <p>
	 * To save computation time the results of this method should be stored in
	 * an array. Hence this method must only be called once. However, if the
	 * SBML model to be simulated contains initial assignments, this can lead to
	 * wrong simulation results because initial assignments may depend on
	 * current parameter values.
	 * </p>
	 * 
	 * @return An array containing the initial values of this model. Note that
	 *         this is not necessarily equal to the initial values stored in the
	 *         SBML file as this method also evaluates initial assignments if
	 *         there are any.
	 */
	protected void init() {
		int i;
		valuesHash = new HashMap<String, Value>();
		int yIndex = 0;
		Value val = null;
		currentTime = 0d;

		this.Y = new double[model.getNumCompartments() + model.getNumSpecies()
				+ model.getNumParameters()];

		for (i = 0; i < model.getNumCompartments(); i++) {
			Compartment c = model.getCompartment(i);

			if (Double.isNaN(c.getSize()))
				Y[yIndex] = 0;
			else
				Y[yIndex] = c.getSize();

			valuesHash.put(c.getId(), new Value(yIndex));
			yIndex++;

		}

		for (i = 0; i < model.getNumSpecies(); i++) {
			Species s = model.getSpecies(i);

			if (s.isSetInitialAmount())
				Y[yIndex] = s.getInitialAmount();
			else
				Y[yIndex] = s.getInitialConcentration();

			valuesHash.put(s.getId(), new Value(yIndex));
			yIndex++;

		}

		for (i = 0; i < model.getNumParameters(); i++) {
			Parameter p = model.getParameter(i);

			Y[yIndex] = p.getValue();
			valuesHash.put(p.getId(), new Value(yIndex));
			yIndex++;

		}

		for (i = 0; i < model.getListOfInitialAssignments().size(); i++) {
			InitialAssignment assign = model.getInitialAssignment(i);
			val = null;
			if (assign.isSetMath() && assign.isSetSymbol()) {
				if (model.getSpecies(assign.getSymbol()) != null) {
					Species s = model.getSpecies(assign.getSymbol());
					val = valuesHash.get(s.getId());
				} else if (model.getCompartment(assign.getSymbol()) != null) {
					Compartment c = model.getCompartment(assign.getSymbol());
					val = valuesHash.get(c.getId());
				} else if (model.getParameter(assign.getSymbol()) != null) {
					Parameter p = model.getParameter(assign.getSymbol());
					val = valuesHash.get(p.getId());
				} else
					System.err
							.println("The model contains an initial assignment for a "
									+ "component other than species, compartment or parameter.");
			}
			this.Y[val.getIndex()] = evaluateToDouble(assign.getMath());
		}

		/*
		 * Evaluate Constraints
		 */
		if (model.getNumConstraints() > 0) {
			this.listOfContraintsViolations = new LinkedList[(int) model
					.getNumConstraints()];
			for (i = 0; i < (int) model.getNumConstraints(); i++) {
				if (listOfContraintsViolations[i] == null)
					this.listOfContraintsViolations[i] = new LinkedList<Double>();
				if (evaluateToBoolean(model.getConstraint(i).getMath()))
					this.listOfContraintsViolations[i].add(Double.valueOf(0.0));
			}
		}

		/*
		 * Init Events
		 */
		if (model.getNumEvents() > 0) {
			this.eventFired = new boolean[model.getNumEvents()];
			this.eventMath = new HashMap<DESAssignment, ASTNode>();
			initEvents();
		}

		/*
		 * Rules
		 */
		processRules(Y);

		// save the initial values of this system
		initialValues = new double[Y.length];
		System.arraycopy(Y, 0, initialValues, 0, initialValues.length);
		this.swap = new double[this.Y.length];
		this.events = new ArrayList<DESAssignment>();
		
		

	}

	/**
	 * Initializes the events of the given model. An Event that triggers at t =
	 * 0 must not fire. Only when it triggers at t > 0
	 * 
	 */
	private void initEvents() {
		for (int i = 0; i < eventFired.length; i++) {
			if (evaluateToBoolean(model.getEvent(i).getTrigger().getMath()))
				eventFired[i] = this.getConstantTrue();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.jsbml.ASTNodeCompiler#lambda(org.sbml.jsbml.ASTNode[])
	 */
	public Double lambda(ASTNode... nodes) {
		Double d[] = new Double[Math.max(0, nodes.length - 1)];
		ASTNode function = nodes[nodes.length - 1];
		int i = 0;
		for (ASTNode node : nodes)
			d[i++] = toDouble(node.compile(this));
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.jsbml.ASTNodeCompiler#ln(org.sbml.jsbml.ASTNode)
	 */
	public Double ln(ASTNode node) {
		return Functions.ln(toDouble(node.compile(this)));

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.jsbml.ASTNodeCompiler#log(org.sbml.jsbml.ASTNode)
	 */
	public Double log(ASTNode node) {
		return Functions.log(toDouble(node.compile(this)));

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.jsbml.ASTNodeCompiler#log(org.sbml.jsbml.ASTNode,
	 * org.sbml.jsbml.ASTNode)
	 */
	public Double log(ASTNode nodeleft, ASTNode noderight) {
		return Functions.log(toDouble(nodeleft.compile(this)),
				toDouble(noderight.compile(this)));

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.jsbml.ASTNodeCompiler#minus(org.sbml.jsbml.ASTNode[])
	 */
	public Double minus(ASTNode... nodes) {
		double value = 0.0;
		if (nodes.length > 0) {
			value = toDouble(nodes[0].compile(this));
		}
		for (int i = 1; i < nodes.length; i++) {
			value -= toDouble(nodes[i].compile(this));
		}

		return Double.valueOf(value);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.jsbml.ASTNodeCompiler#not(org.sbml.jsbml.ASTNode)
	 */
	public Boolean not(ASTNode node) {
		if (toBoolean(node.compile(this)) == getConstantTrue())
			return getConstantFalse();
		else
			return getConstantTrue();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.jsbml.ASTNodeCompiler#or(org.sbml.jsbml.ASTNode[])
	 */
	public Boolean or(ASTNode... nodes) {
		for (ASTNode node : nodes) {
			if (toBoolean(node.compile(this)) == getConstantTrue())
				return getConstantTrue();

		}
		return getConstantFalse();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.jsbml.ASTNodeCompiler#piecewise(org.sbml.jsbml.ASTNode[])
	 */
	public Double piecewise(ASTNode... nodes) {
		int i;
		for (i = 1; i < nodes.length - 1; i += 2) {
			if (toBoolean(nodes[i].compile(this)) == getConstantTrue()) {
				return toDouble(nodes[i - 1].compile(this));
			}
		}
		
		return toDouble(nodes[i - 1].compile(this));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.jsbml.ASTNodeCompiler#plus(org.sbml.jsbml.ASTNode[])
	 */
	public Double plus(ASTNode... nodes) {
		double value = 0d;

		for (ASTNode node : nodes)
			value += toDouble(node.compile(this));

		return Double.valueOf(value);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.jsbml.ASTNodeCompiler#pow(org.sbml.jsbml.ASTNode,
	 * org.sbml.jsbml.ASTNode)
	 */
	public Double pow(ASTNode nodeleft, ASTNode noderight) {
		return Double.valueOf(Math.pow(toDouble(nodeleft.compile(this)),
				toDouble(noderight.compile(this))));

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see eva2.tools.math.des.EventDESystem#processAlgebraicRules(double,
	 * double[], double[])
	 */
	public ArrayList<DESAssignment> processAlgebraicRules(double t, double Y[]) {
		ArrayList<DESAssignment> algebraicRules = new ArrayList<DESAssignment>();
		// for (int i = 0; i < model.getNumRules(); i++) {
		// Rule rule = model.getRule(i);
		// if (rule.isAlgebraic()()) {
		//				
		// }
		// }
		return algebraicRules;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see eva2.tools.math.des.EventDESystem#processAssignmentRules(double,
	 * double[], double[])
	 */
	public ArrayList<DESAssignment> processAssignmentRules(double t, double Y[]) {
		ArrayList<DESAssignment> assignmentRules = new ArrayList<DESAssignment>();
		Value val;
		for (int i = 0; i < model.getNumRules(); i++) {
			Rule rule = model.getRule(i);
			if (rule.isAssignment()) {
				AssignmentRule as = (AssignmentRule) rule;
				val = valuesHash.get(as.getVariable());
				assignmentRules.add(new DESAssignment(t, val.getIndex(),
						evaluateToDouble(as.getMath())));

			}
		}

		return assignmentRules;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see eva2.tools.math.des.EventDESystem#processEvents(double, double[],
	 * double[])
	 */
	public ArrayList<DESAssignment> processEvents(double t, double[] Y) {
		ArrayList<DESAssignment> events = new ArrayList<DESAssignment>();
		// change point because of different timepoint due to events
		this.Y = Y;
		this.currentTime = t;

		ASTNode assignment_math;
		Symbol variable;
		double newVal;
		int index, i;
		DESAssignment desa;
		Value compVal;
		
		Event ev;
		// number of events = listOfEvents_delay.length
		for (i = 0; i < model.getNumEvents(); i++) {
			ev = model.getEvent(i);
			// check if event triggers
			if (evaluateToBoolean(ev.getTrigger().getMath())) {
				// check if trigger has just become true
				if (!eventFired[i]) {

					// fire event
					eventFired[i] = true;
					for (int l = 0; l < ev.getNumEventAssignments(); l++) {
						assignment_math = ev.getEventAssignment(l).getMath();
						variable = ev.getEventAssignment(l)
								.getVariableInstance();
						index = valuesHash.get(variable.getId()).getIndex();
						compVal = valuesHash.get(model.getSpecies(variable.getId()).getCompartment());
						// check conditions of the event
						if (ev.getDelay() != null) {
							if (ev.getUseValuesFromTriggerTime()) {
								newVal = evaluateToDouble(assignment_math) * Y[compVal.getIndex()];
								this.events.add(new DESAssignment(currentTime
										+ evaluateToDouble(ev.getDelay()
												.getMath()), index, newVal));
							} else {
								desa = new DESAssignment(currentTime
										+ evaluateToDouble(ev.getDelay()
												.getMath()), index);
								this.eventMath.put(desa, assignment_math);
								this.events.add(desa);
							}

						} else {
							newVal = evaluateToDouble(assignment_math) * Y[compVal.getIndex()];
							events.add(new DESAssignment(currentTime, index,
									newVal));
						}

					}
				}
			}
			// trigger is false -> event has not fired recently
			else {
				eventFired[i] = false;
			}

		}

		i = 0;

		while (this.events.size() > i) {
			desa = this.events.get(i);
			if (desa.getProcessTime() <= currentTime) {
				// uses value from trigger time
				if (desa.getValue() != null) {
					events.add(desa);
					this.events.remove(desa);
				}
				// don't uses value from trigger time
				else {
					//Consider Compartment
					//compVal = valuesHash.get(model.getSpecies(variable.getId()).getCompartment());
					desa.setValue(evaluateToDouble(this.eventMath.get(desa)));
					events.add(desa);
					this.events.remove(desa);
					this.eventMath.remove(desa);

				}
			} else
				i++;
		}

		return events;

	}

	private void processRules(double[] changeRate) {
		for (int i = 0; i < model.getNumRules(); i++) {
			Rule rule = model.getRule(i);
			if (rule.isRate() && currentTime > 0d) {
				RateRule rr = (RateRule) rule;
				evaluateRateRule(rr, changeRate);
			} else if (currentTime == 0d && rule.isAssignment()) {
				AssignmentRule as = (AssignmentRule) rule;
				evaluateAssignmentRule(as, changeRate);
			}

			else if (rule.isScalar()) {

			}
		}

	}

	/**
	 * This method computes the multiplication of the stoichiometric matrix of
	 * the given model system with the reaction velocities vector passed to this
	 * method. Note, the stoichiometric matrix is only constructed implicitely
	 * by running over all reactions and considering all participating reactants
	 * and products with their according stoichiometry or stoichiometric math.
	 * 
	 * @param velocities
	 *            An array of reaction velocities at the current time.
	 * @param Y
	 * @return An array containing the rates of change for each species in the
	 *         model system of this class.
	 */
	protected void processVelocities(double[] changeRate) {
		int reactionIndex, sReferenceIndex, speciesIndex;
		Species species;
		SpeciesReference speciesRef;
		Value val;
		// Velocities of each reaction.
		for (int i = 0; i < v.length; i++) {
			currentReaction = model.getReaction(i);
			KineticLaw kin = currentReaction.getKineticLaw();
			if (kin != null) {
				v[i] = evaluateToDouble(kin.getMath());
			} else
				v[i] = 0;
		}

		for (reactionIndex = 0; reactionIndex < model.getNumReactions(); reactionIndex++) {
			Reaction r = model.getReaction(reactionIndex);
			for (sReferenceIndex = 0; sReferenceIndex < r.getNumReactants(); sReferenceIndex++) {
				speciesRef = r.getReactant(sReferenceIndex);
				species = speciesRef.getSpeciesInstance();
				val = valuesHash.get(species.getId());
				if (!species.getBoundaryCondition() && !species.getConstant()) {
					speciesIndex = val.getIndex();

					if (speciesRef.isSetStoichiometryMath())
						changeRate[speciesIndex] -= evaluateToDouble(speciesRef
								.getStoichiometryMath().getMath())
								* v[reactionIndex];
					else
						changeRate[speciesIndex] -= speciesRef
								.getStoichiometry()
								* v[reactionIndex];

				}
			}

			for (sReferenceIndex = 0; sReferenceIndex < r.getNumProducts(); sReferenceIndex++) {
				speciesRef = r.getProduct(sReferenceIndex);
				species = speciesRef.getSpeciesInstance();
				val = valuesHash.get(species.getId());
				if (!species.getBoundaryCondition() && !species.getConstant()) {
					speciesIndex = val.getIndex();

					if (speciesRef.isSetStoichiometryMath())
						changeRate[speciesIndex] += evaluateToDouble(speciesRef
								.getStoichiometryMath().getMath())
								* v[reactionIndex];
					else
						changeRate[speciesIndex] += speciesRef
								.getStoichiometry()
								* v[reactionIndex];

				}
			}

		}
		
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.jsbml.ASTNodeCompiler#relationEqual(org.sbml.jsbml.ASTNode,
	 * org.sbml.jsbml.ASTNode)
	 */
	public Boolean relationEqual(ASTNode nodeleft, ASTNode noderight) {
		return Boolean
				.valueOf(toDouble(nodeleft.compile(this)) == toDouble(noderight
						.compile(this)));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.sbml.jsbml.ASTNodeCompiler#relationGreaterEqual(org.sbml.jsbml.ASTNode
	 * , org.sbml.jsbml.ASTNode)
	 */
	public Boolean relationGreaterEqual(ASTNode nodeleft, ASTNode noderight) {
		return Boolean
				.valueOf(toDouble(nodeleft.compile(this)) >= toDouble(noderight
						.compile(this)));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.sbml.jsbml.ASTNodeCompiler#relationGraterThan(org.sbml.jsbml.ASTNode,
	 * org.sbml.jsbml.ASTNode)
	 */
	public Boolean relationGreaterThan(ASTNode nodeleft, ASTNode noderight) {
		return Boolean
				.valueOf(toDouble(nodeleft.compile(this)) > toDouble(noderight
						.compile(this)));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.sbml.jsbml.ASTNodeCompiler#relationLessEqual(org.sbml.jsbml.ASTNode,
	 * org.sbml.jsbml.ASTNode)
	 */
	public Boolean relationLessEqual(ASTNode nodeleft, ASTNode noderight) {
		return Boolean
				.valueOf(toDouble(nodeleft.compile(this)) <= toDouble(noderight
						.compile(this)));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.sbml.jsbml.ASTNodeCompiler#relationLessThan(org.sbml.jsbml.ASTNode,
	 * org.sbml.jsbml.ASTNode)
	 */
	public Boolean relationLessThan(ASTNode nodeleft, ASTNode noderight) {
		return Boolean
				.valueOf(toDouble(nodeleft.compile(this)) < toDouble(noderight
						.compile(this)));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.sbml.jsbml.ASTNodeCompiler#relationNotEqual(org.sbml.jsbml.ASTNode,
	 * org.sbml.jsbml.ASTNode)
	 */
	public Boolean relationNotEqual(ASTNode nodeleft, ASTNode noderight) {
		return Boolean
				.valueOf(toDouble(nodeleft.compile(this)) != toDouble(noderight
						.compile(this)));
	}

	/**
	 * 
	 * @param lambda
	 * @param names
	 * @param d
	 * @return
	 */
	private ASTNode replace(ASTNode lambda, Hashtable<String, Double> args) {
		String name;
		for (ASTNode child : lambda.getListOfNodes())
			if (child.isName() && args.containsKey(child.getName())) {
				name = child.getName();
				child.setType(ASTNode.Type.REAL);
				child.setValue(args.get(name));
			} else if (child.getNumChildren() > 0)
				child = replace(child, args);
		return lambda;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.jsbml.ASTNodeCompiler#root(org.sbml.jsbml.ASTNode,
	 * org.sbml.jsbml.ASTNode)
	 */
	public Double root(ASTNode nodeleft, ASTNode noderight) {
		return Functions.root(toDouble(nodeleft.compile(this)),
				toDouble(noderight.compile(this)));

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.jsbml.ASTNodeCompiler#sec(org.sbml.jsbml.ASTNode)
	 */
	public Double sec(ASTNode node) {
		return Functions.sec(toDouble(node.compile(this)));

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.jsbml.ASTNodeCompiler#sech(org.sbml.jsbml.ASTNode)
	 */
	public Double sech(ASTNode node) {
		return Functions.sech(toDouble(node.compile(this)));

	}

	/**
	 * This method allows to set the parameters of the model to the specified
	 * values in the given array.
	 * 
	 * @param params
	 *            An array of parameter values to be set for this model. If the
	 *            number of given parameters does not match the number of model
	 *            parameters, an exception will be thrown.
	 */
	// TODO changing the model directly not allowed / does this method still
	// make sense?
	public void setParameters(double[] params) {
		// TODO consider local parameters as well.
		// if (params.length != model.getNumParameters())
		// throw new IllegalArgumentException(
		// "The number of parameters passed to this method must "
		// + "match the number of parameters in the model.");
		int paramNum, reactionNum, localPnum;
		for (paramNum = 0; paramNum < model.getNumParameters(); paramNum++)
			model.getParameter(paramNum).setValue(params[paramNum]);
		for (reactionNum = 0; reactionNum < model.getNumReactions(); reactionNum++) {
			KineticLaw law = model.getReaction(reactionNum).getKineticLaw();
			for (localPnum = 0; localPnum < law.getNumParameters(); localPnum++)
				law.getParameter(localPnum).setValue(params[paramNum++]);
		}
		if (model.getNumInitialAssignments() > 0 || model.getNumEvents() > 0)
			init();

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.jsbml.ASTNodeCompiler#sin(org.sbml.jsbml.ASTNode)
	 */
	public Double sin(ASTNode node) {
		return Double.valueOf(Math.sin(toDouble(node.compile(this))));
	}

	// ---- Setters ----

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.jsbml.ASTNodeCompiler#sinh(org.sbml.jsbml.ASTNode)
	 */
	public Double sinh(ASTNode node) {
		return Double.valueOf(Math.sinh(toDouble(node.compile(this))));
	}

	/*
	 * ---- Getters ----
	 */

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.jsbml.ASTNodeCompiler#sqrt(org.sbml.jsbml.ASTNode)
	 */
	public Double sqrt(ASTNode node) {
		return Double.valueOf(Math.sqrt(toDouble(node.compile(this))));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.jsbml.ASTNodeCompiler#symbolTime(java.lang.String)
	 */
	public Double symbolTime(String timeSymbol) {
		return Double.valueOf(getTime());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.jsbml.ASTNodeCompiler#tan(org.sbml.jsbml.ASTNode)
	 */
	public Double tan(ASTNode node) {
		return Double.valueOf(Math.tan(toDouble(node.compile(this))));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.jsbml.ASTNodeCompiler#tanh(org.sbml.jsbml.ASTNode)
	 */
	public Double tanh(ASTNode node) {
		return Double.valueOf(Math.tanh(toDouble(node.compile(this))));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.jsbml.ASTNodeCompiler#times(org.sbml.jsbml.ASTNode[])
	 */
	public Double times(ASTNode... nodes) {
		if (nodes.length == 0)
			return 0d;
		double value = 1d;
		for (ASTNode node : nodes)
			value *= toDouble(node.compile(this));
		return Double.valueOf(value);
	}

	/**
	 * Tries to convert the given Object into a boolean value.
	 * 
	 * @param o
	 *            An object that is either an instance of Boolean, Integer,
	 *            Double, or String.
	 * @return The boolean value represented by the givne Object. If the given
	 *         value is a number, true is returned if its value is not zero.
	 */
	private boolean toBoolean(Object o) {
		if (o instanceof Boolean)
			return ((Boolean) o).booleanValue();
		if (o instanceof String)
			return Boolean.parseBoolean(o.toString());
		return toInt(o) != 0;
	}

	/**
	 * Tries to convert the given Object into a double number.
	 * 
	 * @param o
	 *            An object that is either an instance of Integer, Double,
	 *            Boolean, or String.
	 * @return The double value represented by the given Object. In case the
	 *         given Object is an instance of Boolean, zero is returned for
	 *         false and one for true. If the object is null, NaN will be
	 *         returned.
	 */
	private double toDouble(Object o) {
		if (o == null)
			return Double.NaN;
		if (o instanceof Integer)
			return Double.valueOf(((Integer) o).intValue()).doubleValue();
		if (o instanceof Double)
			return ((Double) o).doubleValue();
		if (o instanceof Boolean)
			return ((Boolean) o).booleanValue() == false ? 0d : 1d;
		return Double.parseDouble(o.toString());
	}

	/**
	 * Tries to convert the given Object into a int number.
	 * 
	 * @param o
	 *            An object that is either an instance of Integer, Double,
	 *            Boolean, or String.
	 * @return The int value represented by the given Object. In case the given
	 *         Object is an instance of Boolean, zero is returned for false and
	 *         one for true.
	 */
	private int toInt(Object o) {
		if (o instanceof Integer)
			return ((Integer) o).intValue();
		if (o instanceof Double)
			return Integer.valueOf((int) ((Double) o).doubleValue());
		if (o instanceof Boolean)
			return ((Boolean) o).booleanValue() == false ? 0 : 1;
		return Integer.parseInt(o.toString());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.jsbml.ASTNodeCompiler#uiMinus(org.sbml.jsbml.ASTNode)
	 */
	public Double uiMinus(ASTNode node) {
		return Double.valueOf(-((Double) node.compile(this)).doubleValue());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.jsbml.ASTNodeCompiler#unknownASTNode()
	 */
	public Double unknownASTNode() {
		return Double.valueOf(Double.NaN);
	}

	public Boolean xor(ASTNode... nodes) {
		Boolean value = getConstantFalse();

		for (int i = 0; i < nodes.length; i++) {
			if (toBoolean(nodes[i].compile(this)) == getConstantTrue()) {
				if (value)
					return getConstantFalse();
				else
					value = getConstantTrue();
			}
		}
		return value;
	}

}
