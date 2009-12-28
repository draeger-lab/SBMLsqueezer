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
import java.util.Arrays;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

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

import eva2.tools.des.DESystem;

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
 * @since 1.3
 * @version
 * @author Andreas Dr&auml;ger (draeger) <andreas.draeger@uni-tuebingen.de>
 * @author Dieudonne Motsou Wouamba <dwouamba@yahoo.fr>
 * @date Sep 6, 2007
 */
public class SBMLinterpreter implements ASTNodeCompiler, DESystem {

	/**
	 * Generated serial version UID
	 */
	private static final long serialVersionUID = 3453063382705340995L;

	private double currentTime;

	/**
	 * This field is necessary to also consider local parameters of the current
	 * reaction because it is not possible to access these parameters from the
	 * model. Hence we have to memorize an additional reference to the Reaction
	 * and thus to the list of these parameters.
	 */
	protected Reaction currentReaction;

	/**
	 * An array, which stores all computed initial values of the model. If this
	 * model does not contain initial assignments, the initial values will only
	 * be taken once from the information stored in the model. Otherwise they
	 * have to be computed again as soon as the parameter values of this model
	 * are changed, because the parameters may influence the return values of
	 * the initial assignments.
	 */
	// protected double[] initialValues;
	private HashMap<String, Value> valuesHash;

	/**
	 * An array, which stores for each constraint the list of times, in which
	 * the constraint was violated during the simulation.
	 */
	protected List<Double>[] listOfContraintsViolations;

	/**
	 * This array data structure stores at the position i a list of time (double
	 * values) at which the event with ID equals i must be executed. The value
	 * before last of the list ist the watcher. It can be
	 * <code>POSITIVE_INFINITY </code>if the trigger expession has the value
	 * <code>true</code>or <code>NaN<code> if value of trigger
	 * expression is <code>false</code>
	 */
	protected LinkedList<Double>[] listOfEvents_delay;

	/**
	 * The model to be simulated.
	 */
	protected Model model;

	/**
	 * This map data structure is necessary to save the index of every species
	 * within the model system since libSBML doesn't provide the index of a
	 * species for a given name. This, however, is necessary to work with an
	 * array containing the concentrations of the species.
	 */
	protected Map<String, Integer> speciesIdIndex;

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
		this.speciesIdIndex = new HashMap<String, Integer>();
		this.Y = new double[(int) this.model.getListOfSpecies().size()];
		this.v = new double[(int) this.model.getListOfReactions().size()];
		this.swap = new double[this.Y.length];
		this.init();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.jsbml.ASTNodeCompiler#abs(org.sbml.jsbml.ASTNode)
	 */
	public Double abs(ASTNode node) {
		return Double.valueOf(Math.abs(((Double) node.compile(this))
				.doubleValue()));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.jsbml.ASTNodeCompiler#arccos(org.sbml.jsbml.ASTNode)
	 */
	public Double arccos(ASTNode node) {
		return Double.valueOf(Math.acos(((Double) (node.compile(this)))
				.doubleValue()));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.jsbml.ASTNodeCompiler#arccosh(org.sbml.jsbml.ASTNode)
	 */
	public Double arccosh(ASTNode node) {
		double interim = ((Double) node.compile(this)).doubleValue();
		return Double.valueOf(Math.log((interim)
				+ (Math.sqrt(Math.pow(interim, 2) - 1))));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.jsbml.ASTNodeCompiler#arccot(org.sbml.jsbml.ASTNode)
	 */
	public Double arccot(ASTNode node) {
		double interim = ((Double) node.compile(this)).doubleValue();
		if (interim == 0)
			throw new java.lang.ArithmeticException("arccot(0) undefined");
		return Double.valueOf(Math.atan(1 / (interim)));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.jsbml.ASTNodeCompiler#arccoth(org.sbml.jsbml.ASTNode)
	 */
	public Double arccoth(ASTNode node) {
		double interim = ((Double) node.compile(this)).doubleValue();
		if (interim == 0)
			throw new java.lang.ArithmeticException("arccoth(0) undefined");
		return Double.valueOf((Math.log(1 + (1 / (interim))) - Math
				.log(1 - (1 / (interim)))) / 2);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.jsbml.ASTNodeCompiler#arccsc(org.sbml.jsbml.ASTNode)
	 */
	public Double arccsc(ASTNode node) {
		double interim = ((Double) node.compile(this)).doubleValue();
		if (Math.asin(interim) == 0)
			throw new java.lang.ArithmeticException("Arccsc undefined");
		return Double.valueOf(1 / (Math.asin(interim)));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.jsbml.ASTNodeCompiler#arccsch(org.sbml.jsbml.ASTNode)
	 */
	public Double arccsch(ASTNode node) {
		double interim = ((Double) node.compile(this)).doubleValue();
		if (Math.asin(interim) == 0)
			throw new java.lang.ArithmeticException("arccsch(0) undefined");
		return Double.valueOf(Math.log(1 / interim
				+ Math.sqrt(Math.pow(1 / interim, 2) + 1)));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.jsbml.ASTNodeCompiler#arcsec(org.sbml.jsbml.ASTNode)
	 */
	public Double arcsec(ASTNode node) {
		double interim = Double.valueOf(Math.acos(((Double) node.compile(this))
				.doubleValue()));
		if (interim == 0)
			throw new java.lang.ArithmeticException("arcsec undefined");
		return Double.valueOf(1 / interim);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.jsbml.ASTNodeCompiler#arcsech(org.sbml.jsbml.ASTNode)
	 */
	public Double arcsech(ASTNode node) {
		double interim = ((Double) node.compile(this)).doubleValue();
		if (interim == 0)
			throw new java.lang.ArithmeticException("arcsech(0) undefined");
		return Double.valueOf(Math.log((1 / interim)
				+ (Math.sqrt(Math.pow(1 / interim, 2) - 1))));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.jsbml.ASTNodeCompiler#arcsin(org.sbml.jsbml.ASTNode)
	 */
	public Double arcsin(ASTNode node) {
		return Double.valueOf(Math.asin(((Double) node.compile(this))
				.doubleValue()));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.jsbml.ASTNodeCompiler#arcsinh(org.sbml.jsbml.ASTNode)
	 */
	public Double arcsinh(ASTNode node) {
		double interim = ((Double) node.compile(this)).doubleValue();
		return Double.valueOf(Math.log(interim
				+ Math.sqrt(Math.pow(interim, 2) + 1)));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.jsbml.ASTNodeCompiler#arctan(org.sbml.jsbml.ASTNode)
	 */
	public Double arctan(ASTNode node) {
		return Double.valueOf(Math.atan(((Double) node.compile(this))
				.doubleValue()));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.jsbml.ASTNodeCompiler#arctanh(org.sbml.jsbml.ASTNode)
	 */
	public Double arctanh(ASTNode node) {
		double interim = ((Double) node.compile(this)).doubleValue();
		return Double
				.valueOf((Math.log(1 + interim) - Math.log(1 - interim)) / 2);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.jsbml.ASTNodeCompiler#ceiling(org.sbml.jsbml.ASTNode)
	 */
	public Double ceiling(ASTNode node) {
		return Double.valueOf(Math.ceil(((Double) node.compile(this))
				.doubleValue()));
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
		Value val;
		if (nsb instanceof Species || nsb instanceof Compartment
				|| nsb instanceof Parameter) {
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
			val = valuesHash.get(nsb.getId());
			if (val.getIndex() == -1)
				return val.getValue();
			else {
				return Y[val.getIndex()];
			}

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
		return Double.valueOf(Math.cos(((Double) node.compile(this))
				.doubleValue()));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.jsbml.ASTNodeCompiler#cosh(org.sbml.jsbml.ASTNode)
	 */
	public Double cosh(ASTNode node) {
		return Double.valueOf(Math.cosh(((Double) node.compile(this))
				.doubleValue()));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.jsbml.ASTNodeCompiler#cot(org.sbml.jsbml.ASTNode)
	 */
	public Double cot(ASTNode node) {
		double interim = ((Double) node.compile(this)).doubleValue();
		if (Math.sin(interim) == 0)
			throw new java.lang.ArithmeticException("cot undefined");
		return Double.valueOf(Math.cos(interim) / Math.sin(interim));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.jsbml.ASTNodeCompiler#coth(org.sbml.jsbml.ASTNode)
	 */
	public Double coth(ASTNode node) {
		double interim = ((Double) node.compile(this)).doubleValue();
		if (Math.sinh(interim) == 0)
			throw new java.lang.ArithmeticException("coth undefined");
		return Double.valueOf(Math.cosh(interim) / Math.sinh(interim));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.jsbml.ASTNodeCompiler#csc(org.sbml.jsbml.ASTNode)
	 */
	public Double csc(ASTNode node) {
		double interim = ((Double) node.compile(this)).doubleValue();
		if (Math.sin(interim) == 0)
			throw new java.lang.ArithmeticException("Csc undefined");
		return Double.valueOf(1 / Math.sin(interim));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.jsbml.ASTNodeCompiler#csch(org.sbml.jsbml.ASTNode)
	 */
	public Double csch(ASTNode node) {
		double interim = ((Double) node.compile(this)).doubleValue();
		if (Math.sinh(interim) == 0)
			throw new java.lang.ArithmeticException("Csch undefined");
		return Double.valueOf(1 / Math.sinh(interim));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.jsbml.ASTNodeCompiler#exp(org.sbml.jsbml.ASTNode)
	 */
	public Double exp(ASTNode node) {
		return Double.valueOf(Math.exp(((Double) node.compile(this))
				.doubleValue()));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.jsbml.ASTNodeCompiler#factorial(org.sbml.jsbml.ASTNode)
	 */
	public Double factorial(ASTNode node) {
		return Double.valueOf(Functions.factorial(((Double) node.compile(this))
				.doubleValue()));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.jsbml.ASTNodeCompiler#floor(org.sbml.jsbml.ASTNode)
	 */
	public Double floor(ASTNode node) {
		return Double.valueOf(Math.floor(((Double) node.compile(this))
				.doubleValue()));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.jsbml.ASTNodeCompiler#frac(org.sbml.jsbml.ASTNode,
	 * org.sbml.jsbml.ASTNode)
	 */
	public Double frac(ASTNode nodeleft, ASTNode noderight) {
		return Double.valueOf(((Double) nodeleft.compile(this)).doubleValue()
				/ ((Double) noderight.compile(this)).doubleValue());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.jsbml.ASTNodeCompiler#frac(int, int)
	 */
	public Integer frac(int arg0, int arg1) {
		return Integer.valueOf(arg0 / arg1);
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
					(Double) arguments[i].compile(this));
		return (Double) replace(lambda.getRightChild().clone(), args).compile(
				this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.jsbml.ASTNodeCompiler#functionDelay(java.lang.String)
	 */
	public Double functionDelay(String arg0) {
		return Double.valueOf(arg0);
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
	public Double getConstantFalse() {
		return Double.valueOf(0.0);
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
	public Double getConstantTrue() {
		return Double.valueOf(1.0);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * javaeva.server.oa.go.OptimizationProblems.InferenceRegulatoryNetworks
	 * .Des.DESystem#getDESystemDimension()
	 */
	public int getDESystemDimension() {
		return (int) model.getNumSpecies();
	}

	/**
	 * Returns the initial values of the model to be simulated.
	 * 
	 * @return Returns the initial values of the model to be simulated.
	 */
	// public double[] getInitialValues() {
	// return this.initialValues;
	// }
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

	/**
	 * Stores initial values of symbols changed due to initial assignments.
	 * 
	 * @author Andreas Dr&auml;ger <a
	 *         href="mailto:andreas.draeger@uni-tuebingen.de"
	 *         >andreas.draeger@uni-tuebingen.de</a>
	 * 
	 */
	private class Value {
		Integer index;
		double value;

		/**
		 * 
		 * @param index
		 * @param value
		 */
		public Value(Integer index) {
			setIndex(index);

		}

		public Value(Double value) {
			setValue(value);
		}

		public Integer getIndex() {
			return index;
		}

		public void setIndex(Integer index) {
			this.index = index;
			this.value = Double.NaN;
		}

		public double getValue() {
			return value;
		}

		public void setValue(double value) {
			this.value = value;
			this.index = -1;
		}

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
		// if ((Y == null) || (time == 0.0))
		// System.arraycopy(initialValues, 0, this.Y, 0, initialValues.length);
		// else
		// this.Y = Y;
		this.currentTime = time;
		double res[] = new double[Y.length];
		/*
		 * Events checking if the model has events and executing events that
		 * must be executed at this time point: t = time.
		 */
		if (model.getNumEvents() > 0) {
			Event ev;
			// number of events = listOfEvents_delay.length
			for (i = 0; i < listOfEvents_delay.length; i++) {
				ev = model.getEvent(i);
				// check if event must be fired (update)
				if (evaluateToBoolean(ev.getTrigger().getMath())) {
					if (listOfEvents_delay[i].get(
							listOfEvents_delay[i].size() - 2).isNaN()) {
						prepareEvents(i, time, ev.getDelay().getMath());
						listOfEvents_delay[i].set(
								listOfEvents_delay[i].size() - 2,
								Double.POSITIVE_INFINITY);
					}
				} else if (listOfEvents_delay[i].get(
						listOfEvents_delay[i].size() - 2).isInfinite()) {
					listOfEvents_delay[i].set(listOfEvents_delay[i].size() - 2,
							Double.NaN);
				}
			}
			// check events to be executed at this time point
			for (int j = 0; j < listOfEvents_delay.length; j++) {
				Double elt;
				int counter = 0;
				// execute events
				int k = 0;
				while (k < listOfEvents_delay[j].size() - 3) {
					elt = listOfEvents_delay[j].get(k).doubleValue();
					if (!elt.isNaN() && elt <= time) {
						counter++;
						System.out.println("Time\t" + time);
						performEvents(model.getEvent(j));
					}
					k++;
				}
				if (listOfEvents_delay[j].get(listOfEvents_delay[j].size() - 3)
						.isInfinite()) {
					listOfEvents_delay[j].set(listOfEvents_delay[j].size() - 3,
							Double.NaN);
					for (int p = 0; p < listOfEvents_delay[j].getLast()
							.intValue(); p++) {
						if (p < listOfEvents_delay[j].size() - 3)
							listOfEvents_delay[j].remove(p);
					}
				}
				if (listOfEvents_delay[j].get(listOfEvents_delay[j].size() - 3)
						.isNaN()) {
					listOfEvents_delay[j].set(listOfEvents_delay[j].size() - 3,
							Double.POSITIVE_INFINITY);

					listOfEvents_delay[j].set(listOfEvents_delay[j].size() - 1,
							counter * 1.0);
				}
			}
		}
		// Velocities of each reaction.
		for (i = 0; i < v.length; i++) {
			currentReaction = model.getReaction(i);
			KineticLaw kin = currentReaction.getKineticLaw();
			if (kin != null)
				v[i] = evaluateToDouble(kin.getMath());
			else
				v[i] = 0;
		}
		res = linearCombinationOfVelocities(v);

		/*
		 * TODO: Rules
		 */
		for (i = 0; i < model.getNumRules(); i++) {
			Rule rule = model.getRule(i);
			if (rule.isAlgebraic()) {
				// TODO
			} else if (rule.isAssignment()) {
				AssignmentRule ar = (AssignmentRule) rule;
				if (ar.isCompartmentVolume())
					model.getCompartment(ar.getVariable()).setVolume(
							evaluateToDouble(rule.getMath()));
				else if (ar.isParameter())
					model.getParameter(ar.getVariable()).setValue(
							evaluateToDouble(rule.getMath()));
				else if (ar.isSpeciesConcentration()) {
					// TODO
				}
			} else if (rule.isRate()) {
				// The entity identify must have its constant attribute set
				// to false
				RateRule rr = (RateRule) rule;
				String var = rr.getVariable();
				if (rr.isCompartmentVolume()) {
					// TODO
				} else if (rr.isSpeciesConcentration()) {
					// boundaryCondition must be true
					Y[speciesIdIndex.get(var).intValue()] = evaluateToDouble(rule
							.getMath());
				} else if (rr.isParameter()) {
					// TODOConstraints
				}
			} else if (rule.isScalar()) {
			}
		}
		/*
		 * Checking the Constraints
		 */
		for (i = 0; i < (int) model.getNumConstraints(); i++)
			if (evaluateToBoolean(model.getConstraint(i).getMath()))
				listOfContraintsViolations[i].add(time);
		return this.Y;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * javaeva.server.oa.go.OptimizationProblems.InferenceRegulatoryNetworks
	 * .Des.DESystem#getValue(double, double[], double[])
	 */
	public void getValue(double time, double[] Y, double[] res) {
		System.arraycopy(getValue(time, Y), 0, res, 0, res.length);
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
			d[i++] = (Double) node.compile(this);
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.jsbml.ASTNodeCompiler#ln(org.sbml.jsbml.ASTNode)
	 */
	public Double ln(ASTNode node) {
		return Double.valueOf(Math.log(((Double) node.compile(this))
				.doubleValue()));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.jsbml.ASTNodeCompiler#log(org.sbml.jsbml.ASTNode)
	 */
	public Double log(ASTNode node) {
		return Double.valueOf(Math.log10(((Double) node.compile(this))
				.doubleValue()));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.jsbml.ASTNodeCompiler#log(org.sbml.jsbml.ASTNode,
	 * org.sbml.jsbml.ASTNode)
	 */
	public Double log(ASTNode nodeleft, ASTNode noderight) {
		return Functions.logarithm(((Double) nodeleft.compile(this))
				.doubleValue(), ((Double) noderight.compile(this))
				.doubleValue());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.jsbml.ASTNodeCompiler#logicalNot(org.sbml.jsbml.ASTNode)
	 */
	public Double logicalNot(ASTNode node) {
		return Double
				.valueOf((((Double) node.compile(this)).doubleValue() == getConstantTrue()) ? 0.0
						: 1.0);
	}

	public Double logicalAND(ASTNode... nodes) {

		for (ASTNode node : nodes) {
			if (((Double) node.compile(this)).doubleValue() == getConstantFalse())
				return getConstantFalse();

		}
		return getConstantTrue();
	}

	public Double logicalOR(ASTNode... nodes) {
		for (ASTNode node : nodes) {
			if (((Double) node.compile(this)).doubleValue() == getConstantTrue())
				return getConstantTrue();

		}
		return getConstantFalse();
	}

	public Double logicalXOR(ASTNode... nodes) {
		Double value = getConstantFalse();

		if (nodes.length > 0) {
			value = ((Double) nodes[1].compile(this)).doubleValue();
		}

		for (int i = 1; i < nodes.length; i++) {
			if (((Double) nodes[i].compile(this)).doubleValue() == value)
				;

			value = getConstantFalse();
		}
		return value;
	}

	public Double and(ASTNode... nodes) {
		for (ASTNode node : nodes) {
			if (((Double) node.compile(this)).doubleValue() == getConstantFalse())
				return getConstantFalse();

		}
		return getConstantTrue();
	}

	public Double or(ASTNode... nodes) {
		for (ASTNode node : nodes) {
			if (((Double) node.compile(this)).doubleValue() == getConstantTrue())
				return getConstantTrue();

		}
		return getConstantFalse();
	}

	public Double xor(ASTNode... nodes) {
		Double value = getConstantFalse();

		if (nodes.length > 0) {
			value = ((Double) nodes[1].compile(this)).doubleValue();
		}

		for (int i = 1; i < nodes.length; i++) {
			if (((Double) nodes[i].compile(this)).doubleValue() == value)
				;

			value = getConstantFalse();
		}
		return value;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.sbml.jsbml.ASTNodeCompiler#logicalOperation(org.sbml.jsbml.ASTNode)
	 */
	public Object logicalOperation(ASTNode node) {
		// TODO Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.jsbml.ASTNodeCompiler#minus(org.sbml.jsbml.ASTNode[])
	 */
	public Double minus(ASTNode... nodes) {
		double value = 0.0;

		for (ASTNode node : nodes)
			value -= ((Double) node.compile(this)).doubleValue();

		return Double.valueOf(value);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.jsbml.ASTNodeCompiler#piecewise(org.sbml.jsbml.ASTNode[])
	 */
	public Double piecewise(ASTNode... nodes) {
		int i;
		for (i = 1; i < nodes.length - 1; i += 2) {
			if (((Double) nodes[i].compile(this)).doubleValue() == getConstantTrue())
				return Double.valueOf((Double) nodes[i - 1].compile(this));

		}

		return Double.valueOf((Double) nodes[i - 1].compile(this));

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.jsbml.ASTNodeCompiler#plus(org.sbml.jsbml.ASTNode[])
	 */
	public Double plus(ASTNode... nodes) {
		double value = 0.0;

		for (ASTNode node : nodes)
			value += ((Double) node.compile(this)).doubleValue();

		return Double.valueOf(value);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.jsbml.ASTNodeCompiler#pow(org.sbml.jsbml.ASTNode,
	 * org.sbml.jsbml.ASTNode)
	 */
	public Double pow(ASTNode nodeleft, ASTNode noderight) {
		return Double.valueOf(Math.pow(((Double) nodeleft.compile(this))
				.doubleValue(), ((Double) noderight.compile(this))
				.doubleValue()));

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.jsbml.ASTNodeCompiler#relationEqual(org.sbml.jsbml.ASTNode,
	 * org.sbml.jsbml.ASTNode)
	 */
	public Boolean relationEqual(ASTNode nodeleft, ASTNode noderight) {
		return Boolean
				.valueOf(((Double) nodeleft.compile(this)).doubleValue() == ((Double) noderight
						.compile(this)).doubleValue());
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
				.valueOf(((Double) nodeleft.compile(this)).doubleValue() >= ((Double) noderight
						.compile(this)).doubleValue());
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
				.valueOf(((Double) nodeleft.compile(this)).doubleValue() > ((Double) noderight
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
				.valueOf(((Double) nodeleft.compile(this)).doubleValue() <= ((Double) noderight
						.compile(this)).doubleValue());
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
				.valueOf(((Double) nodeleft.compile(this)).doubleValue() < ((Double) noderight
						.compile(this)).doubleValue());
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
				.valueOf(((Double) nodeleft.compile(this)).doubleValue() != ((Double) noderight
						.compile(this)).doubleValue());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.jsbml.ASTNodeCompiler#root(org.sbml.jsbml.ASTNode,
	 * org.sbml.jsbml.ASTNode)
	 */
	public Double root(ASTNode nodeleft, ASTNode noderight) {
		double interim = ((Double) noderight.compile(this)).doubleValue();
		if (interim != 0)
			return Double.valueOf(Math.pow(((Double) nodeleft.compile(this))
					.doubleValue(), 1 / interim));
		throw new java.lang.ArithmeticException("Division by zero");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.jsbml.ASTNodeCompiler#sec(org.sbml.jsbml.ASTNode)
	 */
	public Double sec(ASTNode node) {
		double interim = Math.cos(((Double) node.compile(this)).doubleValue());
		if (interim == 0)
			throw new java.lang.ArithmeticException("sec(0) undefined");
		return Double.valueOf(1 / interim);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.jsbml.ASTNodeCompiler#sech(org.sbml.jsbml.ASTNode)
	 */
	public Double sech(ASTNode node) {
		double interim = ((Double) node.compile(this)).doubleValue();
		if (Math.cosh(interim) == 0)
			throw new java.lang.ArithmeticException("Sech undefined");
		return Double.valueOf(1 / Math.cosh(interim));
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
		return Double.valueOf(Math.sin(((Double) node.compile(this))
				.doubleValue()));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.jsbml.ASTNodeCompiler#sinh(org.sbml.jsbml.ASTNode)
	 */
	public Double sinh(ASTNode node) {
		return Double.valueOf(Math.sinh(((Double) node.compile(this))
				.doubleValue()));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.jsbml.ASTNodeCompiler#sqrt(org.sbml.jsbml.ASTNode)
	 */
	public Double sqrt(ASTNode node) {
		return Double.valueOf(Math.sqrt(((Double) node.compile(this))
				.doubleValue()));
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
		return Double.valueOf(Math.tan(((Double) node.compile(this))
				.doubleValue()));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.jsbml.ASTNodeCompiler#tanh(org.sbml.jsbml.ASTNode)
	 */
	public Double tanh(ASTNode node) {
		return Double.valueOf(Math.tanh(((Double) node.compile(this))
				.doubleValue()));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.jsbml.ASTNodeCompiler#times(org.sbml.jsbml.ASTNode[])
	 */
	public Double times(ASTNode... nodes) {
		if (nodes.length == 0)
			return 0.0;

		double value = 1.0;

		for (ASTNode node : nodes)
			value *= ((Double) node.compile(this)).doubleValue();

		return Double.valueOf(value);
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

	/**
	 * TODO Comment
	 * 
	 */
	private void initEvents() {
		listOfEvents_delay = new LinkedList[(int) model.getNumEvents()];
		for (int i = 0; i < listOfEvents_delay.length; i++) {
			listOfEvents_delay[i] = new LinkedList<Double>();
			if (evaluateToBoolean(model.getEvent(i).getTrigger().getMath())) {
				listOfEvents_delay[i].add(Double.POSITIVE_INFINITY);
				listOfEvents_delay[i].addFirst(Double.NaN);
				listOfEvents_delay[i].addLast(0.0);
			} else {
				listOfEvents_delay[i].add(Double.NaN);
				listOfEvents_delay[i].addFirst(Double.NaN);
				listOfEvents_delay[i].addLast(0.0);
			}
		}
	}

	// ---- Setters ----

	/**
	 * Method inserting a double value in a sorted list which contains at least
	 * 2 element.
	 */
	private void insertSort(LinkedList<Double> list, double value) {
		// list size is at leat 2
		if (list.size() <= 3) {
			list.addFirst(value);
		} else {
			int counter = 0;
			while (list.get(counter) < value) {
				counter++;
			}
			list.add(counter, value);
		}
	}

	/*
	 * ---- Getters ----
	 */

	/**
	 * Method performing event.
	 */
	private void performEvents(Event ev) {
		for (int j = 0; j < ev.getNumEventAssignments(); j++) {
			ASTNode assignment_math = ev.getEventAssignment(j).getMath();
			/*
			 * check variable.
			 */
			String variable = ev.getEventAssignment(j).getVariable();
			// if the variable is a species
			if (model.getSpecies(variable) != null) {
				System.out.println(variable + "=\t"
						+ Y[speciesIdIndex.get(variable).intValue()] + "\t"
						+ evaluateToDouble(ev.getEventAssignment(j).getMath()));

				this.Y[speciesIdIndex.get(variable).intValue()] = evaluateToDouble(assignment_math);
			}
			// if the variable is a parameter
			else if (model.getParameter(variable) != null)
				model.getParameter(variable).setValue(
						evaluateToDouble(assignment_math));
			else if (model.getCompartment(variable) != null)
				model.getCompartment(variable).setVolume(
						evaluateToDouble(assignment_math));
			// if the variable is a function
			else if (model.getFunctionDefinition(assignment_math.getName()) != null)
				model.getFunctionDefinition(variable).setMath(assignment_math);
		}
	}

	/**
	 * This Method is filling the array listOfEvents_delay with lists of times
	 * at which events must be executed.
	 */
	private void prepareEvents(int index, double time, ASTNode delay) {
		double value = time;
		value += evaluateToDouble(delay);
		insertSort(listOfEvents_delay[index], value);
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
	 *         logical expression is TRUE and 0.0 otherwise.
	 */
	protected double evaluateToDouble(ASTNode astnode) {
		return ((Double) astnode.compile(this)).doubleValue();
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
		ArrayList<Double> variableValues = new ArrayList<Double>();
		int constantIndex = 0;

		for (i = 0; i < model.getNumSpecies(); i++) {
			Species s = model.getSpecies(i);
			// TODO boundary
			if (s.isConstant()) {
				if (s.isSetInitialAmount()) {
					// TODO Einheitenabgleich beim Umrechnen von Item in
					valuesHash.put(s.getId(), new Value(s.getInitialAmount()
							/ model.getCompartment(s.getCompartment())
									.getVolume()));
				} else
					valuesHash.put(s.getId(), new Value(s
							.getInitialConcentration()));
			}

			else {
				if (s.isSetInitialAmount())
					// TODO Einheitenabgleich beim Umrechnen von Item in
					variableValues.add(s.getInitialAmount()
							/ model.getCompartment(s.getCompartment())
									.getVolume());
				else
					variableValues.add(s.getInitialConcentration());

				valuesHash.put(s.getId(), new Value(constantIndex));
				constantIndex++;
			}
		}

		for (i = 0; i < model.getNumCompartments(); i++) {
			Compartment c = model.getCompartment(i);

			if (c.isConstant())
				valuesHash.put(c.getId(), new Value(c.getSize()));
			else {
				variableValues.add(c.getSize());
				valuesHash.put(c.getId(), new Value(constantIndex));
				constantIndex++;
			}
		}

		for (i = 0; i < model.getNumParameters(); i++) {
			Parameter p = model.getParameter(i);

			if (p.isConstant()) {
				valuesHash.put(p.getId(), new Value(p.getValue()));
			} else {
				variableValues.add(p.getValue());
				valuesHash.put(p.getId(), new Value(constantIndex));
				constantIndex++;
			}
		}

		this.Y = new double[variableValues.size()];

		for (i = 0; i < variableValues.size(); i++) {
			this.Y[i] = variableValues.get(i);
		}
		variableValues.clear();

		for (i = 0; i < model.getListOfInitialAssignments().size(); i++) {
			InitialAssignment assign = model.getInitialAssignment(i);
			Value val = null;
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
			if (val.getIndex() == -1)
				val.setValue(evaluateToDouble(assign.getMath()));
			else
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
		 * Initialize events. At the begin of the simulation each event has a
		 * list containing whether <code>POSITIVE_INFINITY </code>or
		 * <code>NaN</code>.
		 */
		if (model.getNumEvents() > 0)
			initEvents();

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
	 * @return An array containing the rates of change for each species in the
	 *         model system of this class.
	 */
	protected double[] linearCombinationOfVelocities(double[] velocities) {
		int reactionIndex, sReferenceIndex, speciesIndex;
		Species species;
		SpeciesReference speciesRef;
		Value val;
		Arrays.fill(swap, 0.0);
		for (reactionIndex = 0; reactionIndex < model.getNumReactions(); reactionIndex++) {
			Reaction r = model.getReaction(reactionIndex);
			for (sReferenceIndex = 0; sReferenceIndex < r.getNumReactants(); sReferenceIndex++) {
				speciesRef = r.getReactant(sReferenceIndex);
				// speciesIndex = (int) speciesIdIndex
				// .get(speciesRef.getSpecies()).intValue();
				// species = model.getSpecies(speciesIndex);
				species = model.getSpecies(speciesRef.getId());
				val = valuesHash.get(speciesRef.getId());

				if (!species.getBoundaryCondition() && !species.getConstant()) {
					speciesIndex = val.getIndex();
					if (speciesRef.isSetStoichiometryMath())
						swap[speciesIndex] -= evaluateToDouble(speciesRef
								.getStoichiometryMath().getMath())
								* velocities[reactionIndex];
					else
						swap[speciesIndex] -= speciesRef.getStoichiometry()
								* velocities[reactionIndex];
				}
			}
			for (sReferenceIndex = 0; sReferenceIndex < r.getNumProducts(); sReferenceIndex++) {
				speciesRef = r.getProduct(sReferenceIndex);
				// speciesIndex = (int) speciesIdIndex
				// .get(speciesRef.getSpecies()).intValue();
				// species = model.getSpecies(speciesIndex);
				species = model.getSpecies(speciesRef.getId());
				val = valuesHash.get(speciesRef.getId());
				if (!species.getBoundaryCondition() && !species.getConstant()) {
					speciesIndex = val.getIndex();
					if (speciesRef.isSetStoichiometryMath())
						swap[speciesIndex] += evaluateToDouble(speciesRef
								.getStoichiometryMath().getMath())
								* velocities[reactionIndex];
					else
						swap[speciesIndex] += speciesRef.getStoichiometry()
								* velocities[reactionIndex];
				}
			}
		}
		return swap;
	}

	/**
	 * logical AND
	 * 
	 * @param a
	 * @param b
	 * @return true if a and b are true, false otherwise
	 */
	protected boolean logicalAND(boolean a, boolean b) {
		return (a && b) ? true : false;
	}

	/**
	 * logocalOR
	 * 
	 * @param left
	 * @param right
	 * @return false false if left and right are false, true otherwise
	 */
	protected boolean logicalOR(boolean left, boolean right) {
		return (!left && !right) ? false : true;
	}

}
