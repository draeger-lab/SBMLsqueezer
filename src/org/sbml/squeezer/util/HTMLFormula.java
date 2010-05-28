/*
 * $Id: TextFormula.java 151 2010-02-11 17:23:46Z andreas-draeger $
 * $URL: https://jsbml.svn.sourceforge.net/svnroot/jsbml/trunk/src/org/sbml/jsbml/util/TextFormula.java $
 *
 *
 *==================================================================================
 * Copyright (c) 2009 the copyright is held jointly by the individual
 * authors. See the file AUTHORS for the list of authors.
 *
 * This file is part of jsbml, the pure java SBML library. Please visit
 * http://sbml.org for more information about SBML, and http://jsbml.sourceforge.net/
 * to get the latest version of jsbml.
 *
 * jsbml is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 2.1 of the License, or
 * (at your option) any later version.
 *
 * jsbml is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with jsbml.  If not, see <http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html>.
 *
 *===================================================================================
 *
 */
package org.sbml.squeezer.util;

import java.util.List;
import java.util.Vector;

import org.sbml.jsbml.ASTNode;
import org.sbml.jsbml.ASTNodeCompiler;
import org.sbml.jsbml.ASTNodeValue;
import org.sbml.jsbml.Compartment;
import org.sbml.jsbml.FunctionDefinition;
import org.sbml.jsbml.NamedSBaseWithDerivedUnit;
import org.sbml.jsbml.Reaction;
import org.sbml.jsbml.SpeciesReference;
import org.sbml.jsbml.Unit;
import org.sbml.jsbml.UnitDefinition;
import org.sbml.jsbml.util.StringTools;
import org.sbml.squeezer.gui.GUITools;

/**
 * @author Andreas Dr&auml;ger
 * @date 2010-04-08
 * 
 */
public class HTMLFormula extends StringTools implements ASTNodeCompiler {

	/**
	 * Creates an HTML string representation of this UnitDefinition.
	 * 
	 * @return
	 */
	public static String toHTML(UnitDefinition ud) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < ud.getNumUnits(); i++) {
			Unit unit = ud.getUnit(i);
			if (i > 0) {
				sb.append(" &#8901; ");
			}
			sb.append(toHTML(unit));
		}
		return sb.toString();
	}

	/**
	 * 
	 * @return
	 */
	@SuppressWarnings("deprecation")
	public static String toHTML(Unit u) {
		StringBuffer times = new StringBuffer();
		if (u.getMultiplier() != 0) {
			if (u.getMultiplier() != 1)
				times.append(StringTools.toString(u.getMultiplier()));
			StringBuffer pow = new StringBuffer();
			pow.append(u.getKind().getSymbol());
			String prefix = u.getPrefix();
			if (prefix.length() > 0 && !prefix.startsWith("10")) {
				pow.insert(0, prefix);
			} else if (u.getScale() != 0) {
				pow.insert(0, ' ');
				pow = HTMLFormula.times(HTMLFormula.pow(Integer.valueOf(10), u
						.getScale()), pow);
			}
			times = HTMLFormula.times(times, pow);
		}
		if (u.getOffset() != 0) {
			times = HTMLFormula.sum(StringTools.toString(u.getOffset()), times);
		}
		return HTMLFormula.pow(times, Double.valueOf(u.getExponent()))
				.toString();
	}

	/**
	 * HTML code for the empty set symbol.
	 */
	private static final String EMPTY_SET = "&#8709;";
	/**
	 * HTML code for the right arrow.
	 */
	private static final String RIGHT_ARROW = "&#8594;";
	/**
	 * HTML code for the reversible reaction arrow whose upper side is directed
	 * to the right.
	 */
	private static final String REVERSIBLE_REACTION_ARROW = "&#x21cc;";

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.jsbml.ASTNodeCompiler#minus(org.sbml.jsbml.ASTNode[])
	 */
	public Object minus(ASTNode... nodes) {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * Returns the basis to the power of the exponent as StringBuffer. Several
	 * special cases are treated.
	 * 
	 * @param basis
	 * @param exponent
	 * @return
	 */
	public static final StringBuffer pow(Object basis, Object exponent) {
		try {
			if (Double.parseDouble(exponent.toString()) == 0f)
				return new StringBuffer("1");
			if (Double.parseDouble(exponent.toString()) == 1f)
				return basis instanceof StringBuffer ? (StringBuffer) basis
						: new StringBuffer(basis.toString());
		} catch (NumberFormatException exc) {
		}
		String b = basis.toString();
		if (b.contains("&#8901;") || b.contains("-") || b.contains("+")
				|| b.contains("/") || b.contains("<sup>"))
			basis = brackets(basis);
		String e = exponent.toString();
		if (e.contains("&#8901;") || e.substring(1).contains("-")
				|| e.contains("+") || e.contains("/") || e.contains("<sup>"))
			exponent = brackets(e);
		return concat(basis, "<sup>", exponent, "</sup>");
	}

	/**
	 * Returns the product of the given elements as StringBuffer.
	 * 
	 * @param factors
	 * @return
	 */
	public static final StringBuffer times(Object... factors) {
		return arith("&#8901;", factors);
	}

	/**
	 * Returns the sum of the given elements as StringBuffer.
	 * 
	 * @param summands
	 * @return
	 */
	public static final StringBuffer sum(Object... summands) {
		return brackets(arith(Character.valueOf('+'), summands));
	}

	/**
	 * 
	 * @param arith
	 * @return
	 */
	private static StringBuffer brackets(Object arith) {
		return concat("(", arith, ")");
	}

	/**
	 * Basic method which links several elements with a mathematical operator.
	 * All empty StringBuffer object are excluded.
	 * 
	 * @param operator
	 * @param elements
	 * @return
	 */
	private static final StringBuffer arith(Object operator, Object... elements) {
		List<Object> vsb = new Vector<Object>();
		for (Object sb : elements)
			if (sb != null && sb.toString().length() > 0)
				vsb.add(sb);
		StringBuffer equation = new StringBuffer();
		if (vsb.size() > 0)
			equation.append(vsb.get(0));
		String op = operator.toString();
		for (int count = 1; count < vsb.size(); count++)
			append(equation, op, vsb.get(count));
		return equation;
	}

	public ASTNodeValue abs(ASTNodeValue value) {
		// TODO Auto-generated method stub
		return null;
	}

	public ASTNodeValue and(ASTNodeValue... values) {
		// TODO Auto-generated method stub
		return null;
	}

	public ASTNodeValue arccos(ASTNodeValue value) {
		// TODO Auto-generated method stub
		return null;
	}

	public ASTNodeValue arccosh(ASTNodeValue value) {
		// TODO Auto-generated method stub
		return null;
	}

	public ASTNodeValue arccot(ASTNodeValue value) {
		// TODO Auto-generated method stub
		return null;
	}

	public ASTNodeValue arccoth(ASTNodeValue value) {
		// TODO Auto-generated method stub
		return null;
	}

	public ASTNodeValue arccsc(ASTNodeValue value) {
		// TODO Auto-generated method stub
		return null;
	}

	public ASTNodeValue arccsch(ASTNodeValue value) {
		// TODO Auto-generated method stub
		return null;
	}

	public ASTNodeValue arcsec(ASTNodeValue value) {
		// TODO Auto-generated method stub
		return null;
	}

	public ASTNodeValue arcsech(ASTNodeValue value) {
		// TODO Auto-generated method stub
		return null;
	}

	public ASTNodeValue arcsin(ASTNodeValue value) {
		// TODO Auto-generated method stub
		return null;
	}

	public ASTNodeValue arcsinh(ASTNodeValue value) {
		// TODO Auto-generated method stub
		return null;
	}

	public ASTNodeValue arctan(ASTNodeValue value) {
		// TODO Auto-generated method stub
		return null;
	}

	public ASTNodeValue arctanh(ASTNodeValue value) {
		// TODO Auto-generated method stub
		return null;
	}

	public ASTNodeValue ceiling(ASTNodeValue value) {
		// TODO Auto-generated method stub
		return null;
	}

	public ASTNodeValue compile(Compartment c) {
		// TODO Auto-generated method stub
		return null;
	}

	public ASTNodeValue compile(double real) {
		// TODO Auto-generated method stub
		return null;
	}

	public ASTNodeValue compile(int integer) {
		// TODO Auto-generated method stub
		return null;
	}

	public ASTNodeValue compile(NamedSBaseWithDerivedUnit variable) {
		// TODO Auto-generated method stub
		return null;
	}

	public ASTNodeValue compile(String name) {
		// TODO Auto-generated method stub
		return null;
	}

	public ASTNodeValue cos(ASTNodeValue value) {
		// TODO Auto-generated method stub
		return null;
	}

	public ASTNodeValue cosh(ASTNodeValue value) {
		// TODO Auto-generated method stub
		return null;
	}

	public ASTNodeValue cot(ASTNodeValue value) {
		// TODO Auto-generated method stub
		return null;
	}

	public ASTNodeValue coth(ASTNodeValue value) {
		// TODO Auto-generated method stub
		return null;
	}

	public ASTNodeValue csc(ASTNodeValue value) {
		// TODO Auto-generated method stub
		return null;
	}

	public ASTNodeValue csch(ASTNodeValue value) {
		// TODO Auto-generated method stub
		return null;
	}

	public ASTNodeValue delay(ASTNodeValue x, double d) {
		// TODO Auto-generated method stub
		return null;
	}

	public ASTNodeValue equal(ASTNodeValue left, ASTNodeValue right) {
		// TODO Auto-generated method stub
		return null;
	}

	public ASTNodeValue exp(ASTNodeValue value) {
		// TODO Auto-generated method stub
		return null;
	}

	public ASTNodeValue factorial(ASTNodeValue value) {
		// TODO Auto-generated method stub
		return null;
	}

	public ASTNodeValue floor(ASTNodeValue value) {
		// TODO Auto-generated method stub
		return null;
	}

	public ASTNodeValue frac(ASTNodeValue left, ASTNodeValue right) {
		// TODO Auto-generated method stub
		return null;
	}

	public ASTNodeValue frac(int numerator, int denominator) {
		// TODO Auto-generated method stub
		return null;
	}

	public ASTNodeValue function(FunctionDefinition namedSBase,
			ASTNodeValue... args) {
		// TODO Auto-generated method stub
		return null;
	}

	public ASTNodeValue getConstantE() {
		// TODO Auto-generated method stub
		return null;
	}

	public ASTNodeValue getConstantFalse() {
		// TODO Auto-generated method stub
		return null;
	}

	public ASTNodeValue getConstantPi() {
		// TODO Auto-generated method stub
		return null;
	}

	public ASTNodeValue getConstantTrue() {
		// TODO Auto-generated method stub
		return null;
	}

	public ASTNodeValue getNegativeInfinity() {
		// TODO Auto-generated method stub
		return null;
	}

	public ASTNodeValue getPositiveInfinity() {
		// TODO Auto-generated method stub
		return null;
	}

	public ASTNodeValue greaterEqual(ASTNodeValue left, ASTNodeValue right) {
		// TODO Auto-generated method stub
		return null;
	}

	public ASTNodeValue greaterThan(ASTNodeValue left, ASTNodeValue right) {
		// TODO Auto-generated method stub
		return null;
	}

	public ASTNodeValue lambda(ASTNodeValue... values) {
		// TODO Auto-generated method stub
		return null;
	}

	public ASTNodeValue lessEqual(ASTNodeValue left, ASTNodeValue right) {
		// TODO Auto-generated method stub
		return null;
	}

	public ASTNodeValue lessThan(ASTNodeValue left, ASTNodeValue right) {
		// TODO Auto-generated method stub
		return null;
	}

	public ASTNodeValue ln(ASTNodeValue value) {
		// TODO Auto-generated method stub
		return null;
	}

	public ASTNodeValue log(ASTNodeValue value) {
		// TODO Auto-generated method stub
		return null;
	}

	public ASTNodeValue log(ASTNodeValue left, ASTNodeValue right) {
		// TODO Auto-generated method stub
		return null;
	}

	public ASTNodeValue minus(ASTNodeValue... values) {
		// TODO Auto-generated method stub
		return null;
	}

	public ASTNodeValue not(ASTNodeValue value) {
		// TODO Auto-generated method stub
		return null;
	}

	public ASTNodeValue notEqual(ASTNodeValue left, ASTNodeValue right) {
		// TODO Auto-generated method stub
		return null;
	}

	public ASTNodeValue or(ASTNodeValue... values) {
		// TODO Auto-generated method stub
		return null;
	}

	public ASTNodeValue piecewise(ASTNodeValue... values) {
		// TODO Auto-generated method stub
		return null;
	}

	public ASTNodeValue plus(ASTNodeValue... values) {
		// TODO Auto-generated method stub
		return null;
	}

	public ASTNodeValue pow(ASTNodeValue left, ASTNodeValue right) {
		// TODO Auto-generated method stub
		return null;
	}

	public ASTNodeValue root(ASTNodeValue rootExponent, ASTNodeValue radiant) {
		// TODO Auto-generated method stub
		return null;
	}

	public ASTNodeValue root(double rootExponent, ASTNodeValue radiant) {
		// TODO Auto-generated method stub
		return null;
	}

	public ASTNodeValue sec(ASTNodeValue value) {
		// TODO Auto-generated method stub
		return null;
	}

	public ASTNodeValue sech(ASTNodeValue value) {
		// TODO Auto-generated method stub
		return null;
	}

	public ASTNodeValue sin(ASTNodeValue value) {
		// TODO Auto-generated method stub
		return null;
	}

	public ASTNodeValue sinh(ASTNodeValue value) {
		// TODO Auto-generated method stub
		return null;
	}

	public ASTNodeValue sqrt(ASTNodeValue value) {
		// TODO Auto-generated method stub
		return null;
	}

	public ASTNodeValue symbolTime(String time) {
		// TODO Auto-generated method stub
		return null;
	}

	public ASTNodeValue tan(ASTNodeValue value) {
		// TODO Auto-generated method stub
		return null;
	}

	public ASTNodeValue tanh(ASTNodeValue value) {
		// TODO Auto-generated method stub
		return null;
	}

	public ASTNodeValue times(ASTNodeValue... values) {
		// TODO Auto-generated method stub
		return null;
	}

	public ASTNodeValue uiMinus(ASTNodeValue value) {
		// TODO Auto-generated method stub
		return null;
	}

	public ASTNodeValue unknownValue() {
		// TODO Auto-generated method stub
		return null;
	}

	public ASTNodeValue xor(ASTNodeValue... values) {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * @param reaction
	 * @return
	 */
	public String reactionEquation(Reaction reaction) {
		StringBuilder reactionEqn = new StringBuilder();
		int count = 0;
		for (SpeciesReference reactant : reaction.getListOfReactants()) {
			if (count > 0) {
				reactionEqn.append(" + ");
			}
			if (reactant.isSetStoichiometryMath()) {
				reactionEqn.append(reactant.getStoichiometryMath().getMath()
						.compile(this));
			} else if (reactant.getStoichiometry() != 1d) {
				reactionEqn.append(reactant.getStoichiometry());
			}
			reactionEqn.append(' ');
			reactionEqn.append(encodeForHTML(reactant.getSpecies()));
			count++;
		}
		if (reaction.getNumReactants() == 0) {
			reactionEqn.append(EMPTY_SET);
		}
		reactionEqn.append(' ');
		reactionEqn.append(reaction.getReversible() ? RIGHT_ARROW
				: REVERSIBLE_REACTION_ARROW);
		reactionEqn.append(' ');
		count = 0;
		for (SpeciesReference product : reaction.getListOfProducts()) {
			if (count > 0) {
				reactionEqn.append(" + ");
			}
			if (product.isSetStoichiometryMath()) {
				reactionEqn.append(product.getStoichiometryMath().getMath()
						.compile(this));
			} else if (product.getStoichiometry() != 1d) {
				reactionEqn.append(product.getStoichiometry());
			}
			reactionEqn.append(' ');
			reactionEqn.append(encodeForHTML(product.getSpecies()));
			count++;
		}
		if (reaction.getNumProducts() == 0) {
			reactionEqn.append(EMPTY_SET);
		}
		return GUITools.toHTML(reactionEqn.toString());
	}
}
