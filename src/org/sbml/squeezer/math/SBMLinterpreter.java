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
import org.sbml.jsbml.ASTNodeValue;
import org.sbml.jsbml.AssignmentRule;
import org.sbml.jsbml.Compartment;
import org.sbml.jsbml.Event;
import org.sbml.jsbml.FunctionDefinition;
import org.sbml.jsbml.InitialAssignment;
import org.sbml.jsbml.KineticLaw;
import org.sbml.jsbml.ListOf;
import org.sbml.jsbml.LocalParameter;
import org.sbml.jsbml.Model;
import org.sbml.jsbml.NamedSBaseWithDerivedUnit;
import org.sbml.jsbml.Parameter;
import org.sbml.jsbml.RateRule;
import org.sbml.jsbml.Reaction;
import org.sbml.jsbml.Rule;
import org.sbml.jsbml.Species;
import org.sbml.jsbml.SpeciesReference;
import org.sbml.jsbml.Variable;
import org.sbml.jsbml.util.Maths;

import eva2.tools.math.des.DESAssignment;
import eva2.tools.math.des.EventDESystem;
import eva2.tools.math.des.RKSolver;

/**
 * <p>
 * This DifferentialEquationSystem takes a model in SBML format and maps it to a
 * data structure that is understood by the {@link RKSolver} of JavaEvA.
 * Therefore, this class implements all necessary functions expected by SBML.
 * </p>
 * 
 * @author Alexander D&ouml;rr
 * @author Andreas Dr&auml;ger
 * @author Dieudonn&eacute; Motsou Wouamba
 * @since 1.4
 * @date 2007-09-06
 */
public class SBMLinterpreter implements ASTNodeCompiler, EventDESystem {

	/**
	 * 
	 * @author Alexander D&ouml;rr
	 * 
	 */
	private class SpeciesValue extends Value {
		int compartment;

		/**
		 * 
		 * @param index
		 */
		public SpeciesValue(int index) {
			super(index);

		}

		/**
		 * 
		 * @param index
		 * @param compartment
		 */
		public SpeciesValue(int index, int compartment) {
			super(index);
			this.compartment = compartment;
		}

		/**
		 * 
		 * @return
		 */
		public int getCompartment() {
			return this.compartment;
		}

	}

	/**
	 * Stores initial values of symbols changed due to initial assignments.
	 * 
	 * @author Andreas Dr&auml;ger
	 * 
	 */
	private class Value {
		int index;

		/**
		 * 
		 * @param index
		 */
		public Value(int index) {
			setIndex(index);
		}

		/**
		 * 
		 * @return
		 */
		public int getIndex() {
			return index;
		}

		/**
		 * 
		 * @param index
		 */
		public void setIndex(Integer index) {
			this.index = index;
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
	 * Contains a list of all DESAssignment that emerged due to events and have
	 * not been processed so far
	 */
	private ArrayList<DESAssignment> events;

	/**
	 * Contains a list of all algebraic rules transformed to assignment rules
	 * for further processing
	 */
	private ArrayList<AssignmentRule> algebraicRules;

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
	 * Hashes the name of all compartments, species, and global parameters to an
	 * value object which contains the position in the Y vector
	 */
	private HashMap<String, Value> valuesHash;

	/**
	 * Hashes a DESAssignment to an ASTNode containing the mathematical
	 * expression of this assignment when the mathematical expression of this
	 * DESAssignment has to be processed at a later time point in the simulation
	 */
	private HashMap<DESAssignment, ASTNode> eventMath;

	/**
	 * Hashes a DESAssignment to an ASTNode containing the mathematical
	 * expression of this assignment when the mathematical expression of this
	 * DESAssignment has to be processed at a later time point in the simulation
	 */
	private HashMap<DESAssignment, String> eventSpecies;

	/**
	 * An array of the current concentration of each species within the model
	 * system.
	 */
	protected double[] Y;

	/**
	 * This table is necessary to store the values of arguments when a function
	 * definition is evaluated. For an identifier of the argument the
	 * corresponding value will be stored.
	 */
	private Hashtable<String, Double> funcArgs;

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
	public ASTNodeValue abs(ASTNodeValue node) {
		return new ASTNodeValue(Math.abs(node.toDouble()), this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.jsbml.ASTNodeCompiler#and(org.sbml.jsbml.ASTNodeValue[])
	 */
	public ASTNodeValue and(ASTNodeValue... nodes) {
		for (ASTNodeValue node : nodes) {
			if (!node.toBoolean()) {
				return getConstantFalse();
			}
		}
		return getConstantTrue();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.jsbml.ASTNodeCompiler#arccos(org.sbml.jsbml.ASTNode)
	 */
	public ASTNodeValue arccos(ASTNodeValue node) {
		return new ASTNodeValue(Math.acos(node.toDouble()), this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.jsbml.ASTNodeCompiler#arccosh(org.sbml.jsbml.ASTNode)
	 */
	public ASTNodeValue arccosh(ASTNodeValue node) {
		return new ASTNodeValue(Maths.arccosh(node.toDouble()), this);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.jsbml.ASTNodeCompiler#arccot(org.sbml.jsbml.ASTNode)
	 */
	public ASTNodeValue arccot(ASTNodeValue node) {
		return new ASTNodeValue(Maths.arccot(node.toDouble()), this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.jsbml.ASTNodeCompiler#arccoth(org.sbml.jsbml.ASTNode)
	 */
	public ASTNodeValue arccoth(ASTNodeValue node) {
		return new ASTNodeValue(Maths.arccoth(node.toDouble()), this);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.jsbml.ASTNodeCompiler#arccsc(org.sbml.jsbml.ASTNode)
	 */
	public ASTNodeValue arccsc(ASTNodeValue node) {
		return new ASTNodeValue(Maths.arccsc(node.toDouble()), this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.jsbml.ASTNodeCompiler#arccsch(org.sbml.jsbml.ASTNode)
	 */
	public ASTNodeValue arccsch(ASTNodeValue node) {
		return new ASTNodeValue(Maths.arccsch(node.toDouble()), this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.jsbml.ASTNodeCompiler#arcsec(org.sbml.jsbml.ASTNode)
	 */
	public ASTNodeValue arcsec(ASTNodeValue node) {
		return new ASTNodeValue(Maths.arcsec(node.toDouble()), this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.jsbml.ASTNodeCompiler#arcsech(org.sbml.jsbml.ASTNode)
	 */
	public ASTNodeValue arcsech(ASTNodeValue node) {
		return new ASTNodeValue(Maths.arcsech(node.toDouble()), this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.jsbml.ASTNodeCompiler#arcsin(org.sbml.jsbml.ASTNode)
	 */
	public ASTNodeValue arcsin(ASTNodeValue node) {
		return new ASTNodeValue(Math.asin(node.toDouble()), this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.jsbml.ASTNodeCompiler#arcsinh(org.sbml.jsbml.ASTNode)
	 */
	public ASTNodeValue arcsinh(ASTNodeValue node) {
		return new ASTNodeValue(Maths.arcsinh(node.toDouble()), this);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.jsbml.ASTNodeCompiler#arctan(org.sbml.jsbml.ASTNode)
	 */
	public ASTNodeValue arctan(ASTNodeValue node) {
		return new ASTNodeValue(Math.atan(node.toDouble()), this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.jsbml.ASTNodeCompiler#arctanh(org.sbml.jsbml.ASTNode)
	 */
	public ASTNodeValue arctanh(ASTNodeValue node) {
		return new ASTNodeValue(Maths.arctanh(node.toDouble()), this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.jsbml.ASTNodeCompiler#ceiling(org.sbml.jsbml.ASTNode)
	 */
	public ASTNodeValue ceiling(ASTNodeValue node) {
		return new ASTNodeValue(Math.ceil(node.toDouble()), this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.jsbml.ASTNodeCompiler#compile(org.sbml.jsbml.Compartment)
	 */
	public ASTNodeValue compile(Compartment c) {
		return new ASTNodeValue(c.getSize(), this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.jsbml.ASTNodeCompiler#compile(double)
	 */
	public ASTNodeValue compile(double value) {
		return new ASTNodeValue(value, this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.jsbml.ASTNodeCompiler#compile(int)
	 */
	public ASTNodeValue compile(int value) {
		return new ASTNodeValue(value, this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seeorg.sbml.jsbml.ASTNodeCompiler#compile(org.sbml.jsbml.
	 * NamedSBaseWithDerivedUnit)
	 */
	public ASTNodeValue compile(NamedSBaseWithDerivedUnit nsb) {
		Value speciesVal;
		if (nsb instanceof Species) {
			Species s = (Species) nsb;
			speciesVal = valuesHash.get(nsb.getId());
			// int dim = s.getCompartmentInstance().getSpatialDimensions();
			if (getCompartmentValueOf(speciesVal) == 0d) {
				return new ASTNodeValue(Y[speciesVal.getIndex()], this);
			}
			if (s.isSetInitialAmount() && !s.getHasOnlySubstanceUnits()) {
				return new ASTNodeValue(Y[speciesVal.getIndex()]
						/ getCompartmentValueOf(speciesVal), this);
			}
			// return Y[speciesVal.getIndex()] /
			// Maths.root(Y[compVal.getIndex()], 2);
			if (s.isSetInitialConcentration() && s.getHasOnlySubstanceUnits()) {
				return new ASTNodeValue(Y[speciesVal.getIndex()]
						* getCompartmentValueOf(speciesVal), this);
			}

			return new ASTNodeValue(Y[speciesVal.getIndex()], this);
			// return Y[speciesVal.getIndex()] /
			// Maths.root(Y[compVal.getIndex()],2);
		}

		else if (nsb instanceof Compartment || nsb instanceof Parameter
				|| nsb instanceof LocalParameter) {
			if (nsb instanceof LocalParameter) {
				LocalParameter p = (LocalParameter) nsb;
				if (p.getParentSBMLObject() instanceof KineticLaw) {
					ListOf<LocalParameter> params = ((KineticLaw) p
							.getParentSBMLObject()).getListOfParameters();
					for (int i = 0; i < params.size(); i++) {
						if (p.getId() == params.get(i).getId())
							return new ASTNodeValue(params.get(i).getValue(),
									this);
					}
				}
			}
			speciesVal = valuesHash.get(nsb.getId());
			// TODO: Was ist hier los? Warum wird das null?
			if (speciesVal == null) {
				System.out.println(nsb.getId());
			}
			return new ASTNodeValue(speciesVal != null ? Y[speciesVal
					.getIndex()] : Double.NaN, this);

		} else if (nsb instanceof FunctionDefinition) {
			return function((FunctionDefinition) nsb);
		} else if (nsb instanceof Reaction) {
			Reaction r = (Reaction) nsb;
			if (r.isSetKineticLaw()) {
				return r.getKineticLaw().getMath().compile(this);
			}
		}
		return new ASTNodeValue(Double.NaN, this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.jsbml.ASTNodeCompiler#compile(java.lang.String)
	 */
	public ASTNodeValue compile(String name) {
		if (funcArgs != null && funcArgs.containsKey(name)) {
			// replace the name by the associated value of the argument
			return new ASTNodeValue(funcArgs.get(name).doubleValue(), this);
		} else if (valuesHash.containsKey(name)) {
			return new ASTNodeValue(Y[valuesHash.get(name).getIndex()], this);
		}
		return new ASTNodeValue(String.valueOf(name), this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.jsbml.ASTNodeCompiler#cos(org.sbml.jsbml.ASTNode)
	 */
	public ASTNodeValue cos(ASTNodeValue node) {
		return new ASTNodeValue(Math.cos(node.toDouble()), this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.jsbml.ASTNodeCompiler#cosh(org.sbml.jsbml.ASTNode)
	 */
	public ASTNodeValue cosh(ASTNodeValue node) {
		return new ASTNodeValue(Math.cosh(node.toDouble()), this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.jsbml.ASTNodeCompiler#cot(org.sbml.jsbml.ASTNode)
	 */
	public ASTNodeValue cot(ASTNodeValue node) {
		return new ASTNodeValue(Maths.cot(node.toDouble()), this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.jsbml.ASTNodeCompiler#coth(org.sbml.jsbml.ASTNode)
	 */
	public ASTNodeValue coth(ASTNodeValue node) {
		return new ASTNodeValue(Maths.coth(node.toDouble()), this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.jsbml.ASTNodeCompiler#csc(org.sbml.jsbml.ASTNode)
	 */
	public ASTNodeValue csc(ASTNodeValue node) {
		return new ASTNodeValue(Maths.csc(node.toDouble()), this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.jsbml.ASTNodeCompiler#csch(org.sbml.jsbml.ASTNode)
	 */
	public ASTNodeValue csch(ASTNodeValue node) {
		return new ASTNodeValue(Maths.csch(node.toDouble()), this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.jsbml.ASTNodeCompiler#delay(org.sbml.jsbml.ASTNode, double)
	 */
	public ASTNodeValue delay(ASTNodeValue x, double d) {
		// TODO Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.jsbml.ASTNodeCompiler#equal(org.sbml.jsbml.ASTNodeValue,
	 * org.sbml.jsbml.ASTNodeValue)
	 */
	public ASTNodeValue eq(ASTNodeValue left, ASTNodeValue right) {
		return new ASTNodeValue(left.toDouble() == right.toDouble(), this);
	}

	/**
	 * Evaluates the algebraic rules of the given model to assignment rules
	 * 
	 * @param ar
	 * @param changeRate
	 */
	private void evaluateAlgebraicRule() {
		AlgebraicRuleConverter arc = new AlgebraicRuleConverter(model);
		algebraicRules = arc.getAssignmentRules();
	}

	/**
	 * Evaluates the assignment rules of the given model
	 * 
	 * @param as
	 * @param Y
	 */
	private void evaluateAssignmentRule(AssignmentRule as, double Y[]) {
		int speciesIndex;
		speciesIndex = valuesHash.get(as.getVariable()).getIndex();
		Y[speciesIndex] = as.getMath().compile(this).toDouble();
	}

	/**
	 * Evaluates the rate rules of the given model
	 * 
	 * @param rr
	 * @param res
	 */
	private void evaluateRateRule(RateRule rr, double changeRate[]) {
		int speciesIndex;
		double compartment;
		Value val = valuesHash.get(model.getSpecies(rr.getVariable()));

		speciesIndex = valuesHash.get(rr.getVariable()).getIndex();
		changeRate[speciesIndex] = rr.getMath().compile(this).toDouble();
		compartment = getCompartmentValueOf(val);
		if (compartment != 0d) {
			changeRate[speciesIndex] = changeRate[speciesIndex]
					* getCompartmentValueOf(val);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.jsbml.ASTNodeCompiler#exp(org.sbml.jsbml.ASTNode)
	 */
	public ASTNodeValue exp(ASTNodeValue node) {
		return new ASTNodeValue(Math.exp(node.toDouble()), this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.jsbml.ASTNodeCompiler#factorial(org.sbml.jsbml.ASTNode)
	 */
	public ASTNodeValue factorial(ASTNodeValue node) {
		return new ASTNodeValue(Maths.factorial(node.toDouble()), this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.jsbml.ASTNodeCompiler#floor(org.sbml.jsbml.ASTNode)
	 */
	public ASTNodeValue floor(ASTNodeValue node) {
		return new ASTNodeValue(Math.floor(node.toDouble()), this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.jsbml.ASTNodeCompiler#frac(org.sbml.jsbml.ASTNodeValue,
	 * org.sbml.jsbml.ASTNodeValue)
	 */
	public ASTNodeValue frac(ASTNodeValue left, ASTNodeValue right) {
		return new ASTNodeValue(left.toDouble() / right.toDouble(), this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.jsbml.ASTNodeCompiler#frac(int, int)
	 */
	public ASTNodeValue frac(int numerator, int denominator) {
		return new ASTNodeValue((double) numerator / (double) denominator, this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.jsbml.ASTNodeCompiler#function(java.lang.String,
	 * org.sbml.jsbml.ASTNode[])
	 */
	public ASTNodeValue function(FunctionDefinition function,
			ASTNodeValue... arguments) {
		ASTNode lambda = function.getMath();
		funcArgs = new Hashtable<String, Double>();
		for (int i = 0; i < arguments.length; i++) {
			funcArgs.put(lambda.getChild(i).compile(this).toString(),
					arguments[i].toDouble());
		}
		ASTNodeValue value = lambda.getRightChild().compile(this);
		funcArgs = null;
		return value;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.jsbml.ASTNodeCompiler#getConstantE()
	 */
	public ASTNodeValue getConstantE() {
		return new ASTNodeValue(Math.E, this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.jsbml.ASTNodeCompiler#getConstantFalse()
	 */
	public ASTNodeValue getConstantFalse() {
		return new ASTNodeValue(false, this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.jsbml.ASTNodeCompiler#getConstantPi()
	 */
	public ASTNodeValue getConstantPi() {
		return new ASTNodeValue(Math.PI, this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.jsbml.ASTNodeCompiler#getConstantTrue()
	 */
	public ASTNodeValue getConstantTrue() {
		return new ASTNodeValue(true, this);
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
	public ASTNodeValue getNegativeInfinity() {
		return new ASTNodeValue(Double.NEGATIVE_INFINITY, this);
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
			if (k != null) {
				p += k.getNumParameters();
			}
		}
		return p;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.jsbml.ASTNodeCompiler#getPositiveInfinity()
	 */
	public ASTNodeValue getPositiveInfinity() {
		return new ASTNodeValue(Double.POSITIVE_INFINITY, this);
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
		for (i = 0; i < (int) model.getNumConstraints(); i++) {
			if (model.getConstraint(i).getMath().compile(this).toBoolean()) {
				listOfContraintsViolations[i].add(Double.valueOf(time));
			}
		}
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.sbml.jsbml.ASTNodeCompiler#greaterEqual(org.sbml.jsbml.ASTNodeValue,
	 * org.sbml.jsbml.ASTNodeValue)
	 */
	public ASTNodeValue ge(ASTNodeValue nodeleft,
			ASTNodeValue noderight) {
		return new ASTNodeValue(nodeleft.toDouble() >= noderight.toDouble(),
				this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.sbml.jsbml.ASTNodeCompiler#greaterThan(org.sbml.jsbml.ASTNodeValue,
	 * org.sbml.jsbml.ASTNodeValue)
	 */
	public ASTNodeValue gt(ASTNodeValue left, ASTNodeValue right) {
		return new ASTNodeValue(left.toDouble() > right.toDouble(), this);
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
	@SuppressWarnings("unchecked")
	protected void init() {
		int i;
		valuesHash = new HashMap<String, Value>();
		int yIndex = 0;
		currentTime = 0d;

		this.Y = new double[model.getNumCompartments() + model.getNumSpecies()
				+ model.getNumParameters()];

		for (i = 0; i < model.getNumCompartments(); i++) {
			Compartment c = model.getCompartment(i);

			if (Double.isNaN(c.getSize())) {
				Y[yIndex] = 0;
			} else {
				Y[yIndex] = c.getSize();
			}

			valuesHash.put(c.getId(), new Value(yIndex));
			yIndex++;

		}

		for (i = 0; i < model.getNumSpecies(); i++) {
			Species s = model.getSpecies(i);
			Value compVal = valuesHash.get(s.getCompartment());

			if (s.isSetInitialAmount()) {
				Y[yIndex] = s.getInitialAmount();
			} else {
				Y[yIndex] = s.getInitialConcentration();
			}

			valuesHash.put(s.getId(), new SpeciesValue(yIndex, compVal
					.getIndex()));
			yIndex++;

		}

		for (i = 0; i < model.getNumParameters(); i++) {
			Parameter p = model.getParameter(i);

			Y[yIndex] = p.getValue();
			valuesHash.put(p.getId(), new Value(yIndex));
			yIndex++;

		}

		/*
		 * Evaluate Constraints
		 */
		if (model.getNumConstraints() > 0) {
			this.listOfContraintsViolations = (List<Double>[]) new LinkedList<?>[(int) model
					.getNumConstraints()];
			for (i = 0; i < (int) model.getNumConstraints(); i++) {
				if (listOfContraintsViolations[i] == null)
					this.listOfContraintsViolations[i] = new LinkedList<Double>();
				if (model.getConstraint(i).getMath().compile(this).toBoolean())
					this.listOfContraintsViolations[i].add(Double.valueOf(0d));
			}
		}

		/*
		 * Init Events
		 */
		if (model.getNumEvents() > 0) {
			this.eventFired = new boolean[model.getNumEvents()];
			this.eventMath = new HashMap<DESAssignment, ASTNode>();
			this.eventSpecies = new HashMap<DESAssignment, String>();
			initEvents();
		}

		/*
		 * Algebraic Rules
		 */
		for (i = 0; i < (int) model.getNumRules(); i++) {
			if (model.getRule(i).isAlgebraic()) {
				evaluateAlgebraicRule();
				break;
			}

		}

		/*
		 * All other rules
		 */
		processRules(Y);

		/*
		 * Initial assignments
		 */
		processInitialAssignments();

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
			if (model.getEvent(i).getTrigger().getMath().compile(this)
					.toBoolean()) {
				eventFired[i] = true;
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.jsbml.ASTNodeCompiler#lambda(org.sbml.jsbml.ASTNode[])
	 */
	public ASTNodeValue lambda(ASTNodeValue... nodes) {
		double d[] = new double[Math.max(0, nodes.length - 1)];
		ASTNodeValue function = nodes[nodes.length - 1];
		int i = 0;
		for (ASTNodeValue node : nodes) {
			d[i++] = node.toDouble();
		}
		return function;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.sbml.jsbml.ASTNodeCompiler#lessEqual(org.sbml.jsbml.ASTNodeValue,
	 * org.sbml.jsbml.ASTNodeValue)
	 */
	public ASTNodeValue le(ASTNodeValue left, ASTNodeValue right) {
		return new ASTNodeValue(left.toDouble() <= right.toDouble(), this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.jsbml.ASTNodeCompiler#lessThan(org.sbml.jsbml.ASTNodeValue,
	 * org.sbml.jsbml.ASTNodeValue)
	 */
	public ASTNodeValue lt(ASTNodeValue nodeleft, ASTNodeValue noderight) {
		return new ASTNodeValue(nodeleft.toDouble() < noderight.toDouble(),
				this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.jsbml.ASTNodeCompiler#ln(org.sbml.jsbml.ASTNode)
	 */
	public ASTNodeValue ln(ASTNodeValue node) {
		return new ASTNodeValue(Maths.ln(node.toDouble()), this);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.jsbml.ASTNodeCompiler#log(org.sbml.jsbml.ASTNode)
	 */
	public ASTNodeValue log(ASTNodeValue node) {
		return new ASTNodeValue(Maths.log(node.toDouble()), this);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.jsbml.ASTNodeCompiler#log(org.sbml.jsbml.ASTNode,
	 * org.sbml.jsbml.ASTNode)
	 */
	public ASTNodeValue log(ASTNodeValue left, ASTNodeValue right) {
		return new ASTNodeValue(Maths.log(left.toDouble(), right.toDouble()),
				this);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.jsbml.ASTNodeCompiler#minus(org.sbml.jsbml.ASTNode[])
	 */
	public ASTNodeValue minus(ASTNodeValue... nodes) {
		double value = 0.0;
		if (nodes.length > 0) {
			value = nodes[0].toDouble();
		}
		for (int i = 1; i < nodes.length; i++) {
			value -= nodes[i].toDouble();
		}
		return new ASTNodeValue(value, this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.jsbml.ASTNodeCompiler#not(org.sbml.jsbml.ASTNode)
	 */
	public ASTNodeValue not(ASTNodeValue node) {
		return node.toBoolean() ? getConstantFalse() : getConstantTrue();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.jsbml.ASTNodeCompiler#notEqual(org.sbml.jsbml.ASTNodeValue,
	 * org.sbml.jsbml.ASTNodeValue)
	 */
	public ASTNodeValue ne(ASTNodeValue left, ASTNodeValue right) {
		return new ASTNodeValue(left.toDouble() != right.toDouble(), this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.jsbml.ASTNodeCompiler#or(org.sbml.jsbml.ASTNode[])
	 */
	public ASTNodeValue or(ASTNodeValue... nodes) {
		for (ASTNodeValue node : nodes) {
			if (node.toBoolean()) {
				return getConstantTrue();
			}
		}
		return getConstantFalse();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.jsbml.ASTNodeCompiler#piecewise(org.sbml.jsbml.ASTNode[])
	 */
	public ASTNodeValue piecewise(ASTNodeValue... nodes) {
		int i;
		for (i = 1; i < nodes.length - 1; i += 2) {
			if (nodes[i].toBoolean()) {
				return new ASTNodeValue(nodes[i - 1].toDouble(), this);
			}
		}
		return new ASTNodeValue(nodes[i - 1].toDouble(), this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.jsbml.ASTNodeCompiler#plus(org.sbml.jsbml.ASTNode[])
	 */
	public ASTNodeValue plus(ASTNodeValue... nodes) {
		double value = 0d;
		for (ASTNodeValue node : nodes) {
			value += node.toDouble();
		}
		return new ASTNodeValue(value, this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.jsbml.ASTNodeCompiler#pow(org.sbml.jsbml.ASTNodeValue,
	 * org.sbml.jsbml.ASTNodeValue)
	 */
	public ASTNodeValue pow(ASTNodeValue left, ASTNodeValue right) {
		return new ASTNodeValue(Math.pow(left.toDouble(), right.toDouble()),
				this);

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
				assignmentRules.add(new DESAssignment(t, val.getIndex(), as
						.getMath().compile(this).toDouble()));
			}
		}

		for (AssignmentRule as : algebraicRules) {
			val = valuesHash.get(as.getVariable());
			assignmentRules.add(new DESAssignment(t, val.getIndex(), as
					.getMath().compile(this).toDouble()));

		}

		return assignmentRules;
	}

	/**
	 * Checks if Value is a SpeciesValue Object and returns the value of its
	 * compartment or 1d otherwise
	 * 
	 * @param val
	 * @return
	 */
	private double getCompartmentValueOf(Value val) {
		// Is Species
		if (val instanceof SpeciesValue) {
			return Y[((SpeciesValue) val).getCompartment()];
		}
		// Is compartment or parameter
		return 1d;
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

		Value val;
		ASTNode assignment_math;
		Variable variable;
		double newVal, compartmentValue;
		int index, i;
		DESAssignment desa;
		Event ev;

		// number of events = listOfEvents_delay.length
		for (i = 0; i < model.getNumEvents(); i++) {
			ev = model.getEvent(i);
			// check if event triggers
			if (ev.getTrigger().getMath().compile(this).toBoolean()) {
				// check if trigger has just become true
				if (!eventFired[i]) {

					// fire event
					eventFired[i] = true;
					for (int l = 0; l < ev.getNumEventAssignments(); l++) {
						assignment_math = ev.getEventAssignment(l).getMath();
						variable = ev.getEventAssignment(l)
								.getVariableInstance();
						val = valuesHash.get(variable.getId());
						index = val.getIndex();
						compartmentValue = getCompartmentValueOf(val);

						// check conditions of the event
						if (ev.getDelay() != null) {
							if (ev.getUseValuesFromTriggerTime()) {
								if (compartmentValue == 0d) {
									newVal = assignment_math.compile(this)
											.toDouble();
								} else {
									// newVal =
									// evaluateToDouble(assignment_math);
									newVal = assignment_math.compile(this)
											.toDouble()
											* compartmentValue;
								}
								this.events.add(new DESAssignment(currentTime
										+ ev.getDelay().getMath().compile(this)
												.toDouble(), index, newVal));

							} else {
								desa = new DESAssignment(currentTime
										+ ev.getDelay().getMath().compile(this)
												.toDouble(), index);
								this.eventMath.put(desa, assignment_math);
								this.eventSpecies.put(desa, variable.getId());
								this.events.add(desa);
							}

						} else {
							newVal = assignment_math.compile(this).toDouble();
							// newVal = evaluateToDouble(assignment_math) *
							// compartmentValue;
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
					if (valuesHash.get(eventSpecies.get(desa)) instanceof SpeciesValue) {
						// Species s =
						// model.getSpecies(this.eventSpecies.get(desa));
						desa.setValue(eventMath.get(desa).compile(this)
								.toDouble());
					} else {
						desa.setValue(eventMath.get(desa).compile(this)
								.toDouble());
					}
					events.add(desa);
					this.events.remove(desa);
					this.eventMath.remove(desa);
					this.eventSpecies.remove(desa);

				}
			} else {
				i++;
			}
		}
		return events;
	}

	/**
	 * Processes the initial assignments of the model
	 */
	private void processInitialAssignments() {
		for (int i = 0; i < model.getNumInitialAssignments(); i++) {
			InitialAssignment iA = model.getInitialAssignment(i);
			Value val = null;
			if (iA.isSetMath() && iA.isSetSymbol()) {
				if (model.getSpecies(iA.getSymbol()) != null) {
					Species s = model.getSpecies(iA.getSymbol());
					val = valuesHash.get(s.getId());
				} else if (model.getCompartment(iA.getSymbol()) != null) {
					Compartment c = model.getCompartment(iA.getSymbol());
					val = valuesHash.get(c.getId());
				} else if (model.getParameter(iA.getSymbol()) != null) {
					Parameter p = model.getParameter(iA.getSymbol());
					val = valuesHash.get(p.getId());
				} else {
					System.err
							.println("The model contains an initial assignment for a component other than species, compartment or parameter.");
				}
			}
			this.Y[val.getIndex()] = iA.getMath().compile(this).toDouble();
		}
	}

	/**
	 * 
	 * @param changeRate
	 */
	private void processRules(double[] changeRate) {
		// evaluation of assignment rules through the DESystem itself
		// only at timepoint 0d, at timepoints >=0d the solver carries on
		// with this task. Assignment rules are only processed during
		// initialization
		// in this class

		for (int i = 0; i < model.getNumRules(); i++) {
			Rule rule = model.getRule(i);
			if (rule.isRate() && currentTime > 0d) {
				RateRule rr = (RateRule) rule;
				evaluateRateRule(rr, changeRate);
			} else if (rule.isAssignment() && currentTime == 0d) {
				AssignmentRule as = (AssignmentRule) rule;
				evaluateAssignmentRule(as, changeRate);
			} else if (rule.isScalar()) {

			}
		}
		// process list of algebraic rules
		if (algebraicRules != null && currentTime == 0d) {
			for (AssignmentRule as : algebraicRules) {
				evaluateAssignmentRule(as, changeRate);
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
				v[i] = kin.getMath().compile(this).toDouble();
			} else {
				v[i] = 0;
			}
		}

		for (reactionIndex = 0; reactionIndex < model.getNumReactions(); reactionIndex++) {
			Reaction r = model.getReaction(reactionIndex);
			for (sReferenceIndex = 0; sReferenceIndex < r.getNumReactants(); sReferenceIndex++) {
				speciesRef = r.getReactant(sReferenceIndex);
				species = speciesRef.getSpeciesInstance();
				val = valuesHash.get(species.getId());
				if (!species.getBoundaryCondition() && !species.getConstant()) {
					speciesIndex = val.getIndex();

					if (speciesRef.isSetStoichiometryMath()) {
						changeRate[speciesIndex] -= speciesRef
								.getStoichiometryMath().getMath().compile(this)
								.toDouble()
								* v[reactionIndex];
					} else {
						changeRate[speciesIndex] -= speciesRef
								.getStoichiometry()
								* v[reactionIndex];
					}
				}
			}

			for (sReferenceIndex = 0; sReferenceIndex < r.getNumProducts(); sReferenceIndex++) {
				speciesRef = r.getProduct(sReferenceIndex);
				species = speciesRef.getSpeciesInstance();
				val = valuesHash.get(species.getId());
				if (!species.getBoundaryCondition() && !species.getConstant()) {
					speciesIndex = val.getIndex();

					if (speciesRef.isSetStoichiometryMath()) {
						changeRate[speciesIndex] += speciesRef
								.getStoichiometryMath().getMath().compile(this)
								.toDouble()
								* v[reactionIndex];
					} else {
						changeRate[speciesIndex] += speciesRef
								.getStoichiometry()
								* v[reactionIndex];
					}
				}
			}
		}
	}

	// /**
	// *
	// * @param lambda
	// * @param names
	// * @param d
	// * @return
	// */
	// private ASTNode replace(ASTNodeValue lambda, Hashtable<String, Double>
	// args) {
	// String name;
	// for (ASTNodeValue child : lambda.getListOfNodes())
	// if (child.isName() && args.containsKey(child.getName())) {
	// name = child.getName();
	// child.setType(ASTNode.Type.REAL);
	// child.setValue(args.get(name));
	// } else if (child.getNumChildren() > 0)
	// child = replace(child, args);
	// return lambda;
	// }

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.jsbml.ASTNodeCompiler#root(org.sbml.jsbml.ASTNode,
	 * org.sbml.jsbml.ASTNode)
	 */
	public ASTNodeValue root(ASTNodeValue rootExponent, ASTNodeValue radiant) {
		return root(rootExponent.toDouble(), radiant);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.jsbml.ASTNodeCompiler#root(double,
	 * org.sbml.jsbml.ASTNodeValue)
	 */
	public ASTNodeValue root(double rootExponent, ASTNodeValue radiant) {
		return new ASTNodeValue(Maths.root(radiant.toDouble(), rootExponent),
				this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.jsbml.ASTNodeCompiler#sec(org.sbml.jsbml.ASTNode)
	 */
	public ASTNodeValue sec(ASTNodeValue node) {
		return new ASTNodeValue(Maths.sec(node.toDouble()), this);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.jsbml.ASTNodeCompiler#sech(org.sbml.jsbml.ASTNode)
	 */
	public ASTNodeValue sech(ASTNodeValue node) {
		return new ASTNodeValue(Maths.sech(node.toDouble()), this);

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

	// ---- Setters ----

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.jsbml.ASTNodeCompiler#sin(org.sbml.jsbml.ASTNode)
	 */
	public ASTNodeValue sin(ASTNodeValue node) {
		return new ASTNodeValue(Math.sin(node.toDouble()), this);
	}

	/*
	 * ---- Getters ----
	 */

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.jsbml.ASTNodeCompiler#sinh(org.sbml.jsbml.ASTNode)
	 */
	public ASTNodeValue sinh(ASTNodeValue node) {
		return new ASTNodeValue(Math.sinh(node.toDouble()), this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.jsbml.ASTNodeCompiler#sqrt(org.sbml.jsbml.ASTNode)
	 */
	public ASTNodeValue sqrt(ASTNodeValue node) {
		return new ASTNodeValue(Math.sqrt(node.toDouble()), this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.jsbml.ASTNodeCompiler#symbolTime(java.lang.String)
	 */
	public ASTNodeValue symbolTime(String timeSymbol) {
		return new ASTNodeValue(getTime(), this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.jsbml.ASTNodeCompiler#tan(org.sbml.jsbml.ASTNode)
	 */
	public ASTNodeValue tan(ASTNodeValue node) {
		return new ASTNodeValue(Math.tan(node.toDouble()), this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.jsbml.ASTNodeCompiler#tanh(org.sbml.jsbml.ASTNode)
	 */
	public ASTNodeValue tanh(ASTNodeValue node) {
		return new ASTNodeValue(Math.tanh(node.toDouble()), this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.jsbml.ASTNodeCompiler#times(org.sbml.jsbml.ASTNode[])
	 */
	public ASTNodeValue times(ASTNodeValue... nodes) {
		if (nodes.length == 0) {
			return new ASTNodeValue(0d, this);
		}
		double value = 1d;
		for (ASTNodeValue node : nodes) {
			value *= node.toDouble();
		}
		return new ASTNodeValue(value, this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.jsbml.ASTNodeCompiler#uiMinus(org.sbml.jsbml.ASTNode)
	 */
	public ASTNodeValue uiMinus(ASTNodeValue node) {
		return new ASTNodeValue(-node.toDouble(), this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.jsbml.ASTNodeCompiler#unknownASTNode()
	 */
	public ASTNodeValue unknownValue() {
		return new ASTNodeValue(Double.NaN, this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.jsbml.ASTNodeCompiler#xor(org.sbml.jsbml.ASTNode[])
	 */
	public ASTNodeValue xor(ASTNodeValue... nodes) {
		boolean value = false;
		for (int i = 0; i < nodes.length; i++) {
			if (nodes[i].toBoolean()) {
				if (value) {
					return getConstantFalse();
				} else {
					value = true;
				}
			}
		}
		return new ASTNodeValue(value, this);
	}
}
