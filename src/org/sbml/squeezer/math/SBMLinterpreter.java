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
import java.util.HashSet;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;

import org.sbml.jsbml.ASTNode;
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
import org.sbml.jsbml.SBMLException;
import org.sbml.jsbml.SBase;
import org.sbml.jsbml.Species;
import org.sbml.jsbml.SpeciesReference;
import org.sbml.jsbml.Variable;
import org.sbml.jsbml.util.Maths;
import org.sbml.jsbml.util.compilers.ASTNodeCompiler;
import org.sbml.jsbml.util.compilers.ASTNodeValue;
import org.sbml.jsbml.validator.ModelOverdeterminedException;
import org.sbml.jsbml.validator.OverdeterminationValidator;

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
	 * Generated serial version UID
	 */
	private static final long serialVersionUID = 3453063382705340995L;

	/**
	 * Contains a list of all algebraic rules transformed to assignment rules
	 * for further processing
	 */
	private List<AssignmentRule> algebraicRules;

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
	 * Hashes a DESAssignment to an ASTNode containing the mathematical
	 * expression of this assignment when the mathematical expression of this
	 * DESAssignment has to be processed at a later time point in the simulation
	 */
	private HashMap<DESAssignment, ASTNode> eventMath;

	/**
	 * Contains a list of all DESAssignment that emerged due to events and have
	 * not been processed so far
	 */
	private List<DESAssignment> events;

	/**
	 * Hashes a DESAssignment to the species id the DESAssignment is refering to
	 * when the mathematical expression of this DESAssignment has to be
	 * processed at a later time point in the simulation
	 */
	private HashMap<DESAssignment, String> eventSpecies;

	/**
	 * This table is necessary to store the values of arguments when a function
	 * definition is evaluated. For an identifier of the argument the
	 * corresponding value will be stored.
	 */
	private Hashtable<String, Double> funcArgs;

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
	 * A boolean that indicates, whether the intepreter is currently processing
	 * the reaction velocities or not.
	 */
	private boolean isProcessingVelocities;

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
	private HashMap<String, Integer> symbolHash;

	/**
	 * Hashes the name of all species located in a compartment to the position
	 * of their compartment in the Y vector. When a species has no compartment,
	 * it is hashed to null.
	 */
	private HashMap<String, Integer> compartmentHash;

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
	 * @throws ModelOverdeterminedException
	 * @throws SBMLException
	 */
	public SBMLinterpreter(Model model) throws ModelOverdeterminedException,
			SBMLException {
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
	public ASTNodeValue abs(ASTNode node) throws SBMLException {
		return new ASTNodeValue(Math.abs(node.compile(this).toDouble()), this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.jsbml.ASTNodeCompiler#and(org.sbml.jsbml.ASTNode[])
	 */
	public ASTNodeValue and(List<ASTNode> nodes) throws SBMLException {
		for (ASTNode node : nodes) {
			if (!node.compile(this).toBoolean()) {
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
	public ASTNodeValue arccos(ASTNode node) throws SBMLException {
		return new ASTNodeValue(Math.acos(node.compile(this).toDouble()), this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.jsbml.ASTNodeCompiler#arccosh(org.sbml.jsbml.ASTNode)
	 */
	public ASTNodeValue arccosh(ASTNode node) throws SBMLException {
		return new ASTNodeValue(Maths.arccosh(node.compile(this).toDouble()),
				this);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.jsbml.ASTNodeCompiler#arccot(org.sbml.jsbml.ASTNode)
	 */
	public ASTNodeValue arccot(ASTNode node) throws SBMLException {
		return new ASTNodeValue(Maths.arccot(node.compile(this).toDouble()),
				this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.jsbml.ASTNodeCompiler#arccoth(org.sbml.jsbml.ASTNode)
	 */
	public ASTNodeValue arccoth(ASTNode node) throws SBMLException {
		return new ASTNodeValue(Maths.arccoth(node.compile(this).toDouble()),
				this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.jsbml.ASTNodeCompiler#arccsc(org.sbml.jsbml.ASTNode)
	 */
	public ASTNodeValue arccsc(ASTNode node) throws SBMLException {
		return new ASTNodeValue(Maths.arccsc(node.compile(this).toDouble()),
				this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.jsbml.ASTNodeCompiler#arccsch(org.sbml.jsbml.ASTNode)
	 */
	public ASTNodeValue arccsch(ASTNode node) throws SBMLException {
		return new ASTNodeValue(Maths.arccsch(node.compile(this).toDouble()),
				this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.jsbml.ASTNodeCompiler#arcsec(org.sbml.jsbml.ASTNode)
	 */
	public ASTNodeValue arcsec(ASTNode node) throws SBMLException {
		return new ASTNodeValue(Maths.arcsec(node.compile(this).toDouble()),
				this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.jsbml.ASTNodeCompiler#arcsech(org.sbml.jsbml.ASTNode)
	 */
	public ASTNodeValue arcsech(ASTNode node) throws SBMLException {
		return new ASTNodeValue(Maths.arcsech(node.compile(this).toDouble()),
				this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.jsbml.ASTNodeCompiler#arcsin(org.sbml.jsbml.ASTNode)
	 */
	public ASTNodeValue arcsin(ASTNode node) throws SBMLException {
		return new ASTNodeValue(Math.asin(node.compile(this).toDouble()), this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.jsbml.ASTNodeCompiler#arcsinh(org.sbml.jsbml.ASTNode)
	 */
	public ASTNodeValue arcsinh(ASTNode node) throws SBMLException {
		return new ASTNodeValue(Maths.arcsinh(node.compile(this).toDouble()),
				this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.jsbml.ASTNodeCompiler#arctan(org.sbml.jsbml.ASTNode)
	 */
	public ASTNodeValue arctan(ASTNode node) throws SBMLException {
		return new ASTNodeValue(Math.atan(node.compile(this).toDouble()), this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.jsbml.ASTNodeCompiler#arctanh(org.sbml.jsbml.ASTNode)
	 */
	public ASTNodeValue arctanh(ASTNode node) throws SBMLException {
		return new ASTNodeValue(Maths.arctanh(node.compile(this).toDouble()),
				this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.jsbml.ASTNodeCompiler#ceiling(org.sbml.jsbml.ASTNode)
	 */
	public ASTNodeValue ceiling(ASTNode node) throws SBMLException {
		return new ASTNodeValue(Math.ceil(node.compile(this).toDouble()), this);
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
	 * @see org.sbml.jsbml.ASTNodeCompiler#compile(double, int,
	 * java.lang.String)
	 */
	public ASTNodeValue compile(double mantissa, int exponent, String units) {
		return new ASTNodeValue(mantissa * Math.pow(10, exponent), this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.jsbml.ASTNodeCompiler#compile(double, java.lang.String)
	 */
	public ASTNodeValue compile(double value, String units) {
		// TODO: units!
		return new ASTNodeValue(value, this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.jsbml.ASTNodeCompiler#compile(int, java.lang.String)
	 */
	public ASTNodeValue compile(int value, String units) {
		// TODO: units!
		return new ASTNodeValue(value, this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seeorg.sbml.jsbml.ASTNodeCompiler#compile(org.sbml.jsbml.
	 * NamedSBaseWithDerivedUnit)
	 */
	public ASTNodeValue compile(NamedSBaseWithDerivedUnit nsb)
			throws SBMLException {
		Integer symbolIndex;
		if (nsb instanceof Species) {
			Species s = (Species) nsb;
			symbolIndex = symbolHash.get(nsb.getId());

			if (isProcessingVelocities) {

				if (getCompartmentValueOf(nsb.getId()) == 0d) {
					return new ASTNodeValue(Y[symbolIndex], this);
				}

				if (s.isSetInitialAmount() && !s.getHasOnlySubstanceUnits()) {
					return new ASTNodeValue(Y[symbolIndex]
							/ getCompartmentValueOf(nsb.getId()), this);
				}

				// return new ASTNodeValue(Y[speciesVal.getIndex()] /
				// Maths.root(getCompartmentValueOf(speciesVal), 2), this);
				if (s.isSetInitialConcentration()
						&& s.getHasOnlySubstanceUnits()) {
					return new ASTNodeValue(Y[symbolIndex]
							* getCompartmentValueOf(nsb.getId()), this);
				}
			}
			return new ASTNodeValue(Y[symbolIndex], this);
			// return Y[speciesVal.getIndex()] /
			// Maths.root(Y[compVal.getIndex()],2);
		}

		else if (nsb instanceof Compartment || nsb instanceof Parameter
				|| nsb instanceof LocalParameter) {
			if (nsb instanceof LocalParameter) {
				LocalParameter p = (LocalParameter) nsb;
				// parent: list of parameter; parent of parent: kinetic law
				SBase parent = p.getParentSBMLObject().getParentSBMLObject();
				if (parent instanceof KineticLaw) {
					ListOf<LocalParameter> params = ((KineticLaw) parent)
							.getListOfParameters();
					for (int i = 0; i < params.size(); i++) {
						if (p.getId() == params.get(i).getId()) {
							return new ASTNodeValue(params.get(i).getValue(),
									this);
						}
					}
				}
			}
			symbolIndex = symbolHash.get(nsb.getId());
			return new ASTNodeValue(symbolIndex != null ? Y[symbolIndex]
					: Double.NaN, this);

		} else if (nsb instanceof FunctionDefinition) {
			return function((FunctionDefinition) nsb, new LinkedList<ASTNode>());
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
		} else if (symbolHash.containsKey(name)) {
			return new ASTNodeValue(Y[symbolHash.get(name)], this);
		}
		return new ASTNodeValue(String.valueOf(name), this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.jsbml.ASTNodeCompiler#cos(org.sbml.jsbml.ASTNode)
	 */
	public ASTNodeValue cos(ASTNode node) throws SBMLException {
		return new ASTNodeValue(Math.cos(node.compile(this).toDouble()), this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.jsbml.ASTNodeCompiler#cosh(org.sbml.jsbml.ASTNode)
	 */
	public ASTNodeValue cosh(ASTNode node) throws SBMLException {
		return new ASTNodeValue(Math.cosh(node.compile(this).toDouble()), this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.jsbml.ASTNodeCompiler#cot(org.sbml.jsbml.ASTNode)
	 */
	public ASTNodeValue cot(ASTNode node) throws SBMLException {
		return new ASTNodeValue(Maths.cot(node.compile(this).toDouble()), this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.jsbml.ASTNodeCompiler#coth(org.sbml.jsbml.ASTNode)
	 */
	public ASTNodeValue coth(ASTNode node) throws SBMLException {
		return new ASTNodeValue(Maths.coth(node.compile(this).toDouble()), this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.jsbml.ASTNodeCompiler#csc(org.sbml.jsbml.ASTNode)
	 */
	public ASTNodeValue csc(ASTNode node) throws SBMLException {
		return new ASTNodeValue(Maths.csc(node.compile(this).toDouble()), this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.jsbml.ASTNodeCompiler#csch(org.sbml.jsbml.ASTNode)
	 */
	public ASTNodeValue csch(ASTNode node) throws SBMLException {
		return new ASTNodeValue(Maths.csch(node.compile(this).toDouble()), this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.jsbml.ASTNodeCompiler#delay(java.lang.String,
	 * org.sbml.jsbml.ASTNode, double, java.lang.String)
	 */
	public ASTNodeValue delay(String delayName, ASTNode x, double time,
			String timeUnits) throws SBMLException {
		// TODO Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.jsbml.ASTNodeCompiler#equal(org.sbml.jsbml.ASTNode,
	 * org.sbml.jsbml.ASTNode)
	 */
	public ASTNodeValue eq(ASTNode left, ASTNode right) throws SBMLException {
		return new ASTNodeValue(left.compile(this).toDouble() == right.compile(
				this).toDouble(), this);
	}

	/**
	 * Evaluates the algebraic rules of the given model to assignment rules
	 * 
	 * @param ar
	 * @param changeRate
	 * @throws ModelOverdeterminedException
	 */
	private void evaluateAlgebraicRule() throws ModelOverdeterminedException {
		OverdeterminationValidator odv = new OverdeterminationValidator(model);
		// model has not to be overdetermined (violation of the SBML
		// specifications)
		if (odv.isOverdetermined()) {
			throw new ModelOverdeterminedException();
		}
		// create assignment rules out of the algebraic rules
		AlgebraicRuleConverter arc = new AlgebraicRuleConverter(odv
				.getMatching(), model);
		algebraicRules = arc.getAssignmentRules();
	}

	/**
	 * Evaluates the assignment rules of the given model. This method is not to
	 * be used at timepoints > 0 because the new value is directly written into
	 * the changeRate array which is only valid at the starting point of the
	 * simulation. At later timepoints, the solver takes care of assignment
	 * rules with the help of the method processAssignmentRules
	 * 
	 * @param as
	 * @param Y
	 * @throws SBMLException
	 */
	private void evaluateAssignmentRule(AssignmentRule as, double changeRate[])
			throws SBMLException {
		int speciesIndex;
		// get symbol and assign its new value
		speciesIndex = symbolHash.get(as.getVariable());
		changeRate[speciesIndex] = as.getMath().compile(this).toDouble();
	}

	/**
	 * Evaluates the rate rules of the given model
	 * 
	 * @param rr
	 * @param changeRate
	 * @throws SBMLException
	 */
	private void evaluateRateRule(RateRule rr, double changeRate[])
			throws SBMLException {
		int speciesIndex;
		// get symbol and assign its new rate
		speciesIndex = symbolHash.get(rr.getVariable());
		changeRate[speciesIndex] = rr.getMath().compile(this).toDouble();

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.jsbml.ASTNodeCompiler#exp(org.sbml.jsbml.ASTNode)
	 */
	public ASTNodeValue exp(ASTNode node) throws SBMLException {
		return new ASTNodeValue(Math.exp(node.compile(this).toDouble()), this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.jsbml.ASTNodeCompiler#factorial(org.sbml.jsbml.ASTNode)
	 */
	public ASTNodeValue factorial(ASTNode node) throws SBMLException {
		return new ASTNodeValue(Maths.factorial(node.compile(this).toDouble()),
				this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.jsbml.ASTNodeCompiler#floor(org.sbml.jsbml.ASTNode)
	 */
	public ASTNodeValue floor(ASTNode node) throws SBMLException {
		return new ASTNodeValue(Math.floor(node.compile(this).toDouble()), this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.jsbml.ASTNodeCompiler#frac(org.sbml.jsbml.ASTNode,
	 * org.sbml.jsbml.ASTNode)
	 */
	public ASTNodeValue frac(ASTNode left, ASTNode right) throws SBMLException {
		return new ASTNodeValue(left.compile(this).toDouble()
				/ right.compile(this).toDouble(), this);
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
			List<ASTNode> arguments) throws SBMLException {
		ASTNode lambda = function.getMath();
		Hashtable<String, Double> argValues = new Hashtable<String, Double>();
		for (int i = 0; i < arguments.size(); i++) {
			argValues.put(lambda.getChild(i).compile(this).toString(),
					arguments.get(i).compile(this).toDouble());
		}
		funcArgs = argValues;
		ASTNodeValue value = lambda.getRightChild().compile(this);
		funcArgs.clear();
		return value;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.jsbml.ASTNodeCompiler#geq(org.sbml.jsbml.ASTNode,
	 * org.sbml.jsbml.ASTNode)
	 */
	public ASTNodeValue geq(ASTNode nodeleft, ASTNode noderight)
			throws SBMLException {
		return new ASTNodeValue(nodeleft.compile(this).toDouble() >= noderight
				.compile(this).toDouble(), this);
	}

	/**
	 * Checks if the given symbol id refers to a species and returns the value
	 * of its compartment or 1d otherwise
	 * 
	 * @param symbol
	 * 
	 * @param val
	 * @return
	 */
	private double getCompartmentValueOf(String symbol) {
		Integer compartmentIndex = compartmentHash.get(symbol);

		// Is species with compartment
		if (compartmentIndex != null) {
			if (Y[compartmentIndex] != 0d) {
				return Y[compartmentIndex];
			}
		}

		// Is compartment or parameter or there is no compartment for this
		// species
		return 1d;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.jsbml.ASTNodeCompiler#getConstantAvogadro(java.lang.String)
	 */
	public ASTNodeValue getConstantAvogadro(String name) {
		return new ASTNodeValue(Maths.AVOGADRO, this);
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see eva2.tools.math.des.EventDESystem#getPositionOfParameters()
	 */
	public int getPositionOfParameters() {
		return model.getNumCompartments() + model.getNumSpecies() - 1;
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
	 * Returns the timepoint where the simulation is currently situated
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
	public double[] getValue(double time, double[] Y) throws Exception {
		int i;
		this.currentTime = time;
		// create a new array with the same size of Y where the rate of change
		// is stored for every symbol in the simulation
		double changeRate[] = new double[Y.length];
		this.Y = Y;

		isProcessingVelocities = true;
		processVelocities(changeRate);
		isProcessingVelocities = false;

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
	public void getValue(double time, double[] Y, double[] changeRate)
			throws Exception {
		System
				.arraycopy(getValue(time, Y), 0, changeRate, 0,
						changeRate.length);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.jsbml.ASTNodeCompiler#greaterThan(org.sbml.jsbml.ASTNode,
	 * org.sbml.jsbml.ASTNode)
	 */
	public ASTNodeValue gt(ASTNode left, ASTNode right) throws SBMLException {
		return new ASTNodeValue(left.compile(this).toDouble() > right.compile(
				this).toDouble(), this);
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
	 * @throws ModelOverdeterminedException
	 * @throws SBMLException
	 */
	@SuppressWarnings("unchecked")
	public void init() throws ModelOverdeterminedException, SBMLException {
		int i;
		symbolHash = new HashMap<String, Integer>();
		compartmentHash = new HashMap<String, Integer>();
		Integer compartmentIndex, yIndex = 0;
		currentTime = 0d;
		isProcessingVelocities = false;

		this.Y = new double[model.getNumCompartments() + model.getNumSpecies()
				+ model.getNumParameters()];

		/*
		 * Save starting values of the model's compartment in Y
		 */
		for (i = 0; i < model.getNumCompartments(); i++) {
			Compartment c = model.getCompartment(i);

			if (Double.isNaN(c.getSize())) {
				Y[yIndex] = 0;
			} else {
				Y[yIndex] = c.getSize();
			}

			symbolHash.put(c.getId(), yIndex);
			yIndex++;

		}

		/*
		 * Save starting values of the model's species in Y and link them with
		 * their compartment
		 */
		for (i = 0; i < model.getNumSpecies(); i++) {
			Species s = model.getSpecies(i);
			compartmentIndex = symbolHash.get(s.getCompartment());

			if (s.isSetInitialAmount()) {
				Y[yIndex] = s.getInitialAmount();
			} else {
				Y[yIndex] = s.getInitialConcentration();
			}
			symbolHash.put(s.getId(), yIndex);

			compartmentHash.put(s.getId(), compartmentIndex);

			yIndex++;

		}

		/*
		 * Save starting values of the model's parameter in Y
		 */
		for (i = 0; i < model.getNumParameters(); i++) {
			Parameter p = model.getParameter(i);

			Y[yIndex] = p.getValue();
			symbolHash.put(p.getId(), yIndex);
			yIndex++;

		}

		/*
		 * Initial assignments
		 */
		processInitialAssignments();

		/*
		 * Evaluate Constraints
		 */
		if (model.getNumConstraints() > 0) {
			this.listOfContraintsViolations = (List<Double>[]) new LinkedList<?>[(int) model
					.getNumConstraints()];
			for (i = 0; i < (int) model.getNumConstraints(); i++) {
				if (listOfContraintsViolations[i] == null) {
					this.listOfContraintsViolations[i] = new LinkedList<Double>();
				}
				if (model.getConstraint(i).getMath().compile(this).toBoolean()) {
					this.listOfContraintsViolations[i].add(Double.valueOf(0d));
				}
			}
		}

		/*
		 * Initialize Events
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
	 * @throws SBMLException
	 * 
	 */
	private void initEvents() throws SBMLException {
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
	public ASTNodeValue lambda(List<ASTNode> nodes) throws SBMLException {
		double d[] = new double[Math.max(0, nodes.size() - 1)];
		for (int i = 0; i < nodes.size() - 1; i++) {
			d[i++] = nodes.get(i).compile(this).toDouble();
		}
		// TODO: what happens with d?
		ASTNodeValue function = nodes.get(nodes.size() - 1).compile(this);
		return function;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.jsbml.ASTNodeCompiler#lessEqual(org.sbml.jsbml.ASTNode,
	 * org.sbml.jsbml.ASTNode)
	 */
	public ASTNodeValue leq(ASTNode left, ASTNode right) throws SBMLException {
		return new ASTNodeValue(left.compile(this).toDouble() <= right.compile(
				this).toDouble(), this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.jsbml.ASTNodeCompiler#ln(org.sbml.jsbml.ASTNode)
	 */
	public ASTNodeValue ln(ASTNode node) throws SBMLException {
		return new ASTNodeValue(Maths.ln(node.compile(this).toDouble()), this);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.jsbml.ASTNodeCompiler#log(org.sbml.jsbml.ASTNode)
	 */
	public ASTNodeValue log(ASTNode node) throws SBMLException {
		return new ASTNodeValue(Maths.log(node.compile(this).toDouble()), this);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.jsbml.ASTNodeCompiler#log(org.sbml.jsbml.ASTNode,
	 * org.sbml.jsbml.ASTNode)
	 */
	public ASTNodeValue log(ASTNode left, ASTNode right) throws SBMLException {
		return new ASTNodeValue(Maths.log(left.compile(this).toDouble(), right
				.compile(this).toDouble()), this);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.jsbml.ASTNodeCompiler#lessThan(org.sbml.jsbml.ASTNode,
	 * org.sbml.jsbml.ASTNode)
	 */
	public ASTNodeValue lt(ASTNode nodeleft, ASTNode noderight)
			throws SBMLException {
		return new ASTNodeValue(nodeleft.compile(this).toDouble() < noderight
				.compile(this).toDouble(), this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.jsbml.ASTNodeCompiler#minus(org.sbml.jsbml.ASTNode[])
	 */
	public ASTNodeValue minus(List<ASTNode> nodes) throws SBMLException {
		double value = 0d;
		if (nodes.size() > 0) {
			value = nodes.get(0).compile(this).toDouble();
		}
		for (int i = 1; i < nodes.size(); i++) {
			value -= nodes.get(i).compile(this).toDouble();
		}
		return new ASTNodeValue(value, this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.jsbml.ASTNodeCompiler#notEqual(org.sbml.jsbml.ASTNode,
	 * org.sbml.jsbml.ASTNode)
	 */
	public ASTNodeValue neq(ASTNode left, ASTNode right) throws SBMLException {
		return new ASTNodeValue(left.compile(this).toDouble() != right.compile(
				this).toDouble(), this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.jsbml.ASTNodeCompiler#not(org.sbml.jsbml.ASTNode)
	 */
	public ASTNodeValue not(ASTNode node) throws SBMLException {
		return node.compile(this).toBoolean() ? getConstantFalse()
				: getConstantTrue();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.jsbml.ASTNodeCompiler#or(org.sbml.jsbml.ASTNode[])
	 */
	public ASTNodeValue or(List<ASTNode> nodes) throws SBMLException {
		for (ASTNode node : nodes) {
			if (node.compile(this).toBoolean()) {
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
	public ASTNodeValue piecewise(List<ASTNode> nodes) throws SBMLException {
		int i;
		for (i = 1; i < nodes.size() - 1; i += 2) {
			if (nodes.get(i).compile(this).toBoolean()) {
				return new ASTNodeValue(nodes.get(i - 1).compile(this)
						.toDouble(), this);
			}
		}
		return new ASTNodeValue(nodes.get(i - 1).compile(this).toDouble(), this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.jsbml.ASTNodeCompiler#plus(org.sbml.jsbml.ASTNode[])
	 */
	public ASTNodeValue plus(List<ASTNode> nodes) throws SBMLException {
		double value = 0d;
		for (ASTNode node : nodes) {
			value += node.compile(this).toDouble();
		}
		return new ASTNodeValue(value, this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.jsbml.ASTNodeCompiler#pow(org.sbml.jsbml.ASTNode,
	 * org.sbml.jsbml.ASTNode)
	 */
	public ASTNodeValue pow(ASTNode left, ASTNode right) throws SBMLException {
		return new ASTNodeValue(Math.pow(left.compile(this).toDouble(), right
				.compile(this).toDouble()), this);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see eva2.tools.math.des.EventDESystem#processAssignmentRules(double,
	 * double[], double[])
	 */
	public ArrayList<DESAssignment> processAssignmentRules(double t, double Y[])
			throws SBMLException {
		ArrayList<DESAssignment> assignmentRules = new ArrayList<DESAssignment>();
		Integer symbolIndex;

		for (int i = 0; i < model.getNumRules(); i++) {
			Rule rule = model.getRule(i);
			if (rule.isAssignment()) {
				AssignmentRule as = (AssignmentRule) rule;
				symbolIndex = symbolHash.get(as.getVariable());
				assignmentRules.add(new DESAssignment(t, symbolIndex, as
						.getMath().compile(this).toDouble()));
			}
		}

		if (algebraicRules != null) {
			for (AssignmentRule as : algebraicRules) {
				symbolIndex = symbolHash.get(as.getVariable());
				assignmentRules.add(new DESAssignment(t, symbolIndex, as
						.getMath().compile(this).toDouble()));
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
	public ArrayList<DESAssignment> processEvents(double t, double[] Y)
			throws SBMLException {
		ArrayList<DESAssignment> events = new ArrayList<DESAssignment>();
		// change point because of different time point due to events
		this.Y = Y;
		this.currentTime = t;
		Integer symbolIndex;
		ASTNode assignment_math;
		Variable variable;
		double newVal;
		int i;
		DESAssignment desa;
		Event ev;

		// number of events = listOfEvents_delay.length
		for (i = 0; i < model.getNumEvents(); i++) {
			ev = model.getEvent(i);
			isProcessingVelocities = true;
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
						symbolIndex = symbolHash.get(variable.getId());

						// check conditions of the event
						if (ev.getDelay() != null) {
							if (ev.getUseValuesFromTriggerTime()) {

								newVal = processAssignmentVaribale(variable
										.getId(), assignment_math);

								this.events.add(new DESAssignment(currentTime
										+ ev.getDelay().getMath().compile(this)
												.toDouble(), symbolIndex,
										newVal));

							} else {
								desa = new DESAssignment(currentTime
										+ ev.getDelay().getMath().compile(this)
												.toDouble(), symbolIndex);
								this.eventMath.put(desa, assignment_math);
								this.eventSpecies.put(desa, variable.getId());
								this.events.add(desa);
							}

						} else {

							newVal = processAssignmentVaribale(
									variable.getId(), assignment_math);

							events.add(new DESAssignment(currentTime,
									symbolIndex, newVal));
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

					desa.setValue(processAssignmentVaribale(eventSpecies
							.get(desa), eventMath.get(desa)));

					events.add(desa);
					this.events.remove(desa);
					this.eventMath.remove(desa);
					this.eventSpecies.remove(desa);

				}
			} else {
				i++;
			}
		}
		isProcessingVelocities = false;

		return events;
	}

	/**
	 * Processes the variable of an assignment in terms of determining whether
	 * the variable references to a species or not and if so accounts the
	 * compartment in an appropriate way.
	 * 
	 * @param variable
	 * @param math
	 * @return
	 * @throws SBMLException
	 */
	private double processAssignmentVaribale(String variable, ASTNode math)
			throws SBMLException {
		double compartmentValue, result = 0d;
		Species s;
		if (compartmentHash.containsKey(variable)) {
			s = model.getSpecies(variable);
			if (s.isSetInitialAmount() && !s.getHasOnlySubstanceUnits()) {
				compartmentValue = getCompartmentValueOf(s.getId());				
				result = math.compile(this).toDouble() * compartmentValue;
			} else if (s.isSetInitialConcentration()
					&& s.getHasOnlySubstanceUnits()) {				
				compartmentValue = getCompartmentValueOf(s.getId());
				result = math.compile(this).toDouble() / compartmentValue;
			}
			else{
				result = math.compile(this).toDouble();
			}

		} else {			
			result = math.compile(this).toDouble();
		}

		return result;
	}

	/**
	 * Processes the initial assignments of the model
	 * 
	 * @throws SBMLException
	 */
	private void processInitialAssignments() throws SBMLException {
		for (int i = 0; i < model.getNumInitialAssignments(); i++) {
			InitialAssignment iA = model.getInitialAssignment(i);
			Integer index = null;
			if (iA.isSetMath() && iA.isSetVariable()) {
				if (model.getSpecies(iA.getVariable()) != null) {
					Species s = model.getSpecies(iA.getVariable());
					index = symbolHash.get(s.getId());
					//TODO: consider compartment of the species					
					this.Y[index] = iA.getMath().compile(this).toDouble();					
				} else if (model.getCompartment(iA.getVariable()) != null) {
					Compartment c = model.getCompartment(iA.getVariable());
					index = symbolHash.get(c.getId());
					this.Y[index] = iA.getMath().compile(this).toDouble();
				} else if (model.getParameter(iA.getVariable()) != null) {
					Parameter p = model.getParameter(iA.getVariable());
					index = symbolHash.get(p.getId());
					this.Y[index] = iA.getMath().compile(this).toDouble();
				} else {
					System.err
							.println("The model contains an initial assignment for a component other than species, compartment or parameter.");
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
	// private ASTNode replace(ASTNode lambda, Hashtable<String, Double>
	// args) {
	// String name;
	// for (ASTNode child : lambda.getListOfNodes())
	// if (child.isName() && args.containsKey(child.getName())) {
	// name = child.getName();
	// child.setType(ASTNode.Type.REAL);
	// child.setValue(args.get(name));
	// } else if (child.getNumChildren() > 0)
	// child = replace(child, args);
	// return lambda;
	// }

	/**
	 * 
	 * @param changeRate
	 * @throws SBMLException
	 */
	private void processRules(double[] changeRate) throws SBMLException {
		// evaluation of assignment rules through the DESystem itself
		// only at time point 0d, at time points >=0d the solver carries on
		// with this task. Assignment rules are only processed during
		// initialization in this class

		for (int i = 0; i < model.getNumRules(); i++) {
			Rule rule = model.getRule(i);
			if (rule.isRate() && currentTime > 0d) {
				RateRule rr = (RateRule) rule;
				evaluateRateRule(rr, changeRate);
			} else if (rule.isAssignment() && currentTime == 0d) {
				AssignmentRule as = (AssignmentRule) rule;
				evaluateAssignmentRule(as, changeRate);
			} else /* if (rule.isScalar()) */{
				// a rule is scalar if it is an assignment rule.
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
	 * @throws SBMLException
	 */
	protected void processVelocities(double[] changeRate) throws SBMLException {
		int reactionIndex, sReferenceIndex, speciesIndex;
		Species species;
		SpeciesReference speciesRef;
		HashSet<String> inConcentration = new HashSet<String>();

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

				if (!species.getBoundaryCondition() && !species.getConstant()) {
					speciesIndex = symbolHash.get(species.getId());

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

					if (species.isSetInitialConcentration()) {
						inConcentration.add(species.getId());
					}
				}
			}

			for (sReferenceIndex = 0; sReferenceIndex < r.getNumProducts(); sReferenceIndex++) {
				speciesRef = r.getProduct(sReferenceIndex);
				species = speciesRef.getSpeciesInstance();

				if (!species.getBoundaryCondition() && !species.getConstant()) {
					speciesIndex = symbolHash.get(species.getId());

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

					if (species.isSetInitialConcentration()) {
						inConcentration.add(species.getId());
					}
				}
			}
		}

		// When the unit of reacting specie is given mol/volume
		// then it has to be considered in the change rate that should
		// always be only in mol/time
		for (String s : inConcentration) {
			speciesIndex = symbolHash.get(s);
			changeRate[speciesIndex] = changeRate[speciesIndex]
					/ getCompartmentValueOf(s);
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.jsbml.ASTNodeCompiler#root(org.sbml.jsbml.ASTNode,
	 * org.sbml.jsbml.ASTNode)
	 */
	public ASTNodeValue root(ASTNode rootExponent, ASTNode radiant)
			throws SBMLException {
		return root(rootExponent.compile(this).toDouble(), radiant);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.jsbml.ASTNodeCompiler#root(double, org.sbml.jsbml.ASTNode)
	 */
	public ASTNodeValue root(double rootExponent, ASTNode radiant)
			throws SBMLException {
		return new ASTNodeValue(Maths.root(radiant.compile(this).toDouble(),
				rootExponent), this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.jsbml.ASTNodeCompiler#sec(org.sbml.jsbml.ASTNode)
	 */
	public ASTNodeValue sec(ASTNode node) throws SBMLException {
		return new ASTNodeValue(Maths.sec(node.compile(this).toDouble()), this);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.jsbml.ASTNodeCompiler#sech(org.sbml.jsbml.ASTNode)
	 */
	public ASTNodeValue sech(ASTNode node) throws SBMLException {
		return new ASTNodeValue(Maths.sech(node.compile(this).toDouble()), this);

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
			try {
				init();
			} catch (Exception e) {
				// This can never happen
			}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.jsbml.ASTNodeCompiler#sin(org.sbml.jsbml.ASTNode)
	 */
	public ASTNodeValue sin(ASTNode node) throws SBMLException {
		return new ASTNodeValue(Math.sin(node.compile(this).toDouble()), this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.jsbml.ASTNodeCompiler#sinh(org.sbml.jsbml.ASTNode)
	 */
	public ASTNodeValue sinh(ASTNode node) throws SBMLException {
		return new ASTNodeValue(Math.sinh(node.compile(this).toDouble()), this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.jsbml.ASTNodeCompiler#sqrt(org.sbml.jsbml.ASTNode)
	 */
	public ASTNodeValue sqrt(ASTNode node) throws SBMLException {
		return new ASTNodeValue(Math.sqrt(node.compile(this).toDouble()), this);
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
	public ASTNodeValue tan(ASTNode node) throws SBMLException {
		return new ASTNodeValue(Math.tan(node.compile(this).toDouble()), this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.jsbml.ASTNodeCompiler#tanh(org.sbml.jsbml.ASTNode)
	 */
	public ASTNodeValue tanh(ASTNode node) throws SBMLException {
		return new ASTNodeValue(Math.tanh(node.compile(this).toDouble()), this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.jsbml.ASTNodeCompiler#times(org.sbml.jsbml.ASTNode[])
	 */
	public ASTNodeValue times(List<ASTNode> nodes) throws SBMLException {
		if (nodes.size() == 0) {
			return new ASTNodeValue(0d, this);
		}
		double value = 1d;
		for (ASTNode node : nodes) {
			value *= node.compile(this).toDouble();
		}
		return new ASTNodeValue(value, this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.jsbml.ASTNodeCompiler#toString(org.sbml.jsbml.ASTNode)
	 */
	public String toString(ASTNode value) {
		return value.toString();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.jsbml.ASTNodeCompiler#uiMinus(org.sbml.jsbml.ASTNode)
	 */
	public ASTNodeValue uMinus(ASTNode node) throws SBMLException {
		return new ASTNodeValue(-node.compile(this).toDouble(), this);
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
	public ASTNodeValue xor(List<ASTNode> nodes) throws SBMLException {
		boolean value = false;
		for (int i = 0; i < nodes.size(); i++) {
			if (nodes.get(i).compile(this).toBoolean()) {
				if (value) {
					return getConstantFalse();
				} else {
					value = true;
				}
			}
		}
		return new ASTNodeValue(value, this);
	}

	public ASTNodeValue lambdaFunction(List<ASTNode> children)
			throws SBMLException {
		// TODO Auto-generated method stub
		return null;
	}

	public ASTNodeValue delay(String delayName, ASTNode x, ASTNode delay,
			String timeUnits) throws SBMLException {
		// TODO Auto-generated method stub
		return null;
	}

}
