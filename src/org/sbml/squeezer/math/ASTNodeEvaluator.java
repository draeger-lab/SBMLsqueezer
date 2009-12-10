/*
 * Copyright (C) 2009 ZBIT, University of Tübingen, Andreas Dräger
 */
package org.sbml.squeezer.math;

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
import java.util.Hashtable;

import org.sbml.jsbml.ASTNode;
import org.sbml.jsbml.ASTNodeCompiler;
import org.sbml.jsbml.Compartment;
import org.sbml.jsbml.FunctionDefinition;
import org.sbml.jsbml.NamedSBase;

/**
 * @author Andreas Dr&auml;ger <a
 *         href="mailto:andreas.draeger@uni-tuebingen.de">
 *         andreas.draeger@uni-tuebingen.de</a>
 * 
 */
public class ASTNodeEvaluator implements ASTNodeCompiler {

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
	public Object compile(NamedSBase arg0) {
		// TODO Auto-generated method stub
		return null;
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
	 * @see org.sbml.jsbml.ASTNodeCompiler#functionDelay(java.lang.String)
	 */
	public Object functionDelay(String arg0) {
		// TODO Auto-generated method stub
		return null;
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
	 * @see org.sbml.jsbml.ASTNodeCompiler#getNegativeInfinity()
	 */
	public Double getNegativeInfinity() {
		return Double.valueOf(Double.NEGATIVE_INFINITY);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.jsbml.ASTNodeCompiler#getPositiveInfinity()
	 */
	public Double getPositiveInfinity() {
		return Double.valueOf(Double.POSITIVE_INFINITY);
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
		return Double.valueOf(((((Double) node.compile(this)).doubleValue() == getConstantTrue())) ? 0.0
				: 1.0);
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
	public Object piecewise(ASTNode... arg0) {
		// TODO Auto-generated method stub
		return null;
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
		return Double
				.valueOf(Math.sqrt(((Double) node.compile(this)).doubleValue()));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.jsbml.ASTNodeCompiler#symbolTime(java.lang.String)
	 */
	public Object symbolTime(String arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.jsbml.ASTNodeCompiler#tan(org.sbml.jsbml.ASTNode)
	 */
	public Double tan(ASTNode node) {
		return Double.valueOf(Math.tan(((Double) node.compile(this)).doubleValue()));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.jsbml.ASTNodeCompiler#tanh(org.sbml.jsbml.ASTNode)
	 */
	public Double tanh(ASTNode node) {
		return Double.valueOf(Math.tanh(((Double) node.compile(this)).doubleValue()));
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

}
