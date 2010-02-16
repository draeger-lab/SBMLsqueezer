/*
 SBML2LaTeX converts SBML files (http://sbml.org) into LaTeX files.
 Copyright (C) 2009 ZBIT, University of Tübingen, Andreas Dräger

 This program is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.sbml.squeezer.io;

import java.util.Properties;

import org.sbml.jsbml.ASTNode;
import org.sbml.jsbml.ASTNodeCompiler;
import org.sbml.jsbml.Compartment;
import org.sbml.jsbml.FunctionDefinition;
import org.sbml.jsbml.NamedSBase;
import org.sbml.jsbml.Species;
import org.sbml.jsbml.ASTNode.Type;
import org.sbml.squeezer.CfgKeys;

import atp.sHotEqn;

/**
 * Converts {@link ASTNode} objects into a LaTeX {@link String} to be included
 * into scientific writings or to be displayed by {@link sHotEqn} in a GUI.
 * 
 * @author Andreas Dr&auml;ger <a
 *         href="mailto:andreas.draeger@uni-tuebingen.de">
 *         andreas.draeger@uni-tuebingen.de</a>
 * @since 1.1
 * @date 2009-01-03
 */
public class LaTeX extends StringTools implements ASTNodeCompiler {

	/**
	 * Requires LaTeX package booktabs. Produces a fancy line at the bottom of a
	 * table. This variable also includes the <code>end{longtable}</code>
	 * command and a new line.
	 */
	public static final String bottomrule = "\\bottomrule\\end{longtable}"
			+ newLine;

	/**
	 * The constant pi
	 */
	public static final String CONSTANT_PI = "\\pi";

	/**
	 * Surrounded by new line symbols. The begin of a description environment in
	 * LaTeX.
	 */
	public static final String descriptionBegin = "\\begin{description}"
			+ newLine;

	/**
	 * Surrounded by new line symbols. The end of a description environment.
	 */
	public static final String descriptionEnd = "\\end{description}" + newLine;

	/**
	 * Surrounded by new line symbols. Begin equation. This type of equation
	 * requires the LaTeX package breqn. It will produce equations with
	 * automatic line breaks (LaTeX will compute the optimal place for line
	 * breaks). Unfortunately, this does not work for very long denominators.
	 */
	public static final String eqBegin = newLine + "\\begin{dmath}" + newLine; // equation

	/**
	 * End equation; cf. eqBegin. Surrounded by new line symbols.
	 */
	public static final String eqEnd = newLine + "\\end{dmath}" + newLine; // equation

	public static final String leftBrace = "\\left(";

	/**
	 * An opening quotation mark.
	 */
	public static final String leftQuotationMark = "``";

	/**
	 * This is a LaTeX line break. The line break symbol double backslash
	 * followed by a new line symbol of the operating system.
	 */
	public static final String lineBreak = "\\\\" + newLine;

	/**
	 * Produces a fancy line in tables. Requires LaTeX package booktabs. Starts
	 * and ends with a new line.
	 */
	public static final String midrule = newLine + "\\midrule" + newLine;

	public static final String NEGATIVE_ININITY = "-\\infty";

	public static final String or = "\\lor ";

	public static final String POSITIVE_INFINITY = "\\infty";

	public static final String rightBrace = "\\right)";

	/**
	 * An closing quotation mark.
	 */
	public static final String rightQuotationMark = "\"";

	/**
	 * Needed for the beginning of a table. Requires LaTeX package booktabs.
	 * Surounded by new line symbols.
	 */
	public static final String toprule = newLine + "\\toprule" + newLine;

	public static final String wedge = "\\wedge ";

	public static final String xor = "\\oplus ";

	/**
	 * 
	 * @param command
	 * @param what
	 * @return
	 */
	private static StringBuffer command(String command, Object what) {
		StringBuffer sb = new StringBuffer("\\");
		sb.append(command);
		sb.append('{');
		sb.append(what);
		sb.append('}');
		return sb;
	}

	/**
	 * 
	 * @param command
	 * @param first
	 * @param second
	 * @return
	 */
	private static StringBuffer command(String command, Object first,
			Object second) {
		StringBuffer sb = command(command, first);
		sb.append('{');
		sb.append(second);
		sb.append('}');
		return sb;
	}

	/**
	 * 
	 * @param number
	 * @return
	 */
	public static String getNumbering(long number) {
		if ((Integer.MIN_VALUE < number) && (number < Integer.MAX_VALUE))
			switch ((int) number) {
			case 1:
				return "first";
			case 2:
				return "second";
			case 3:
				return "third";
			case 5:
				return "fifth";
			case 13:
				return "thirteenth";
			default:
				if (number < 13) {
					String word = StringTools.getWordForNumber(number);
					return word.endsWith("t") ? word + 'h' : word + "th";
				}
				break;
			}
		String numberWord = Long.toString(number);
		switch (numberWord.charAt(numberWord.length() - 1)) {
		case '1':
			return StringTools.getWordForNumber(number)
					+ "\\textsuperscript{st}";
		case '2':
			return StringTools.getWordForNumber(number)
					+ "\\textsuperscript{nd}";
		case '3':
			return StringTools.getWordForNumber(number)
					+ "\\textsuperscript{rd}";
		default:
			return StringTools.getWordForNumber(number)
					+ "\\textsuperscript{th}";
		}
	}

	/**
	 * Creates head lines.
	 * 
	 * @param kind
	 *            E.g., section, subsection, subsubsection, paragraph etc.
	 * @param title
	 *            The title of the heading.
	 * @param numbering
	 *            If true a number will be placed in front of the title.
	 * @return
	 */
	private static StringBuffer heading(String kind, String title,
			boolean numbering) {
		StringBuffer heading = new StringBuffer(newLine);
		heading.append("\\");
		heading.append(kind);
		if (!numbering)
			heading.append('*');
		heading.append('{');
		heading.append(title);
		heading.append('}');
		heading.append(newLine);
		return heading;
	}

	/**
	 * Masks all special characters used by LaTeX with a backslash including
	 * hyphen symbols.
	 * 
	 * @param string
	 * @return
	 */
	public static String maskSpecialChars(String string) {
		return maskSpecialChars(string, true);
	}

	/**
	 * 
	 * @param string
	 * @param hyphen
	 *            if true a hyphen symbol is introduced at each position where a
	 *            special character has to be masked anyway.
	 * @return
	 */
	public static String maskSpecialChars(String string, boolean hyphen) {
		StringBuffer masked = new StringBuffer();
		for (int i = 0; i < string.length(); i++) {
			char atI = string.charAt(i);
			if (atI == '<')
				masked.append("$<$");
			else if (atI == '>')
				masked.append("$>$");
			else {
				if ((atI == '_') || (atI == '\\') || (atI == '$')
						|| (atI == '&') || (atI == '#') || (atI == '{')
						|| (atI == '}') || (atI == '~') || (atI == '%')
						|| (atI == '^')) {
					if ((i == 0) || (!hyphen))
						masked.append('\\');
					else if (hyphen && (string.charAt(i - 1) != '\\'))
						masked.append("\\-\\"); // masked.append('\\');
					// } else if ((atI == '[') || (atI == ']')) {
				}
				masked.append(atI);
			}
		}
		return masked.toString().trim();
	}

	public final String CONSTANT_E = mathrm("e").toString();

	public final String CONSTANT_FALSE = mathrm("false").toString();

	public final String CONSTANT_TRUE = mathrm("true").toString();

	/**
	 * Important for LaTeX export to decide whether the name or the id of a
	 * NamedSBase should be printed.
	 */
	private boolean printNameIfAvailable;

	/**
	 * 
	 * 
	 */
	public LaTeX() {
		printNameIfAvailable = false;
	}

	/**
	 * 
	 * @param settings
	 */
	public LaTeX(Properties settings) {
		setPrintNameIfAvailable(((Boolean) settings
				.get(CfgKeys.LATEX_NAMES_IN_EQUATIONS)).booleanValue());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.jsbml.ASTNodeCompiler#abs(org.sbml.jsbml.ASTNode)
	 */
	public StringBuffer abs(ASTNode value) {
		StringBuffer abs = new StringBuffer("\\left\\lvert");
		abs.append(value.compile(this));
		abs.append("\\right\\rvert");
		return abs;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.jsbml.ASTNodeCompiler#arccos(org.sbml.jsbml.ASTNode)
	 */
	public StringBuffer arccos(ASTNode value) {
		Object result = value.compile(this);
		if (value.getLeftChild().getNumChildren() > 0)
			result = brackets(result);
		return command("arccos", result);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.jsbml.ASTNodeCompiler#arccosh(org.sbml.jsbml.ASTNode)
	 */
	public StringBuffer arccosh(ASTNode value) {
		StringBuffer result = (StringBuffer) value.compile(this);
		if (value.getNumChildren() > 0)
			result = brackets(result);
		return function("arccosh", result);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.jsbml.ASTNodeCompiler#arccot(org.sbml.jsbml.ASTNode)
	 */
	public StringBuffer arccot(ASTNode node) {
		return function("arcot", node);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.jsbml.ASTNodeCompiler#arccoth(org.sbml.jsbml.ASTNode)
	 */
	public StringBuffer arccoth(ASTNode node) {
		return function("arccoth", node);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.jsbml.ASTNodeCompiler#arccsc(org.sbml.jsbml.ASTNode)
	 */
	public StringBuffer arccsc(ASTNode node) {
		return function("arccsc", node);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.jsbml.ASTNodeCompiler#arccsch(org.sbml.jsbml.ASTNode)
	 */
	public StringBuffer arccsch(ASTNode node) {
		return function("arccsh", node);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.jsbml.ASTNodeCompiler#arcsec(org.sbml.jsbml.ASTNode)
	 */
	public StringBuffer arcsec(ASTNode node) {
		return function("arcsec", node);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.jsbml.ASTNodeCompiler#arcsech(org.sbml.jsbml.ASTNode)
	 */
	public StringBuffer arcsech(ASTNode node) {
		return function("arcsech", node);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.jsbml.ASTNodeCompiler#arcsin(org.sbml.jsbml.ASTNode)
	 */
	public StringBuffer arcsin(ASTNode node) {
		return function("arcsin", node);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.jsbml.ASTNodeCompiler#arcsinh(org.sbml.jsbml.ASTNode)
	 */
	public StringBuffer arcsinh(ASTNode node) {
		return function("arcsinh", node);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.jsbml.ASTNodeCompiler#arctan(org.sbml.jsbml.ASTNode)
	 */
	public StringBuffer arctan(ASTNode node) {
		return function("arctan", node);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.jsbml.ASTNodeCompiler#arctanh(org.sbml.jsbml.ASTNode)
	 */
	public StringBuffer arctanh(ASTNode node) {
		return function("\\arctanh", node);
	}

	/**
	 * Encloses the given formula in brackets.
	 * 
	 * @param formula
	 * @return
	 */
	public StringBuffer brackets(Object formula) {
		StringBuffer buffer = new StringBuffer("\\left(");
		buffer.append(formula);
		buffer.append("\\right)");
		return buffer;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.jsbml.ASTNodeCompiler#ceiling(org.sbml.jsbml.ASTNode)
	 */
	public StringBuffer ceiling(ASTNode value) {
		StringBuffer ceiling = new StringBuffer("\\left\\lceil ");
		ceiling.append(value.compile(this));
		ceiling.append("\\right\\rceil ");
		return ceiling;
	}

	/**
	 * 
	 * @param color
	 * @param what
	 * @return
	 */
	public StringBuffer colorbox(String color, Object what) {
		return command("colorbox", color, what);
	}

	/**
	 * This method returns the correct LaTeX expression for a function which
	 * returns the size of a compartment. This can be a volume, an area, a
	 * length or a point.
	 */
	public StringBuffer compile(Compartment c) {
		StringBuffer value = new StringBuffer();
		switch ((int) c.getSpatialDimensions()) {
		case 3:
			value.append("vol");
			break;
		case 2:
			value.append("area");
			break;
		case 1:
			value.append("length");
			break;
		default:
			value.append("point");
			break;
		}
		return mathrm(value.toString()).append(brackets(getNameOrID(c)));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.jsbml.ASTNodeCompiler#compile(double)
	 */
	public StringBuffer compile(double real) {
		return new StringBuffer(Double.toString(real));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.jsbml.ASTNodeCompiler#compile(int)
	 */
	public StringBuffer compile(int integer) {
		return new StringBuffer(Integer.toString(integer));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.jsbml.ASTNodeCompiler#compile(org.sbml.jsbml.NamedSBase)
	 */
	public StringBuffer compile(NamedSBase variable) {
		if (variable instanceof Species) {
			Species species = (Species) variable;
			Compartment c = species.getCompartmentInstance();
			boolean concentration = !species.getHasOnlySubstanceUnits()
					&& (0 < c.getSpatialDimensions());
			StringBuffer value = new StringBuffer();
			if (concentration)
				value.append('[');
			value.append(getNameOrID(species));
			if (concentration)
				value.append(']');
			return value;

		} else if (variable instanceof Compartment) {
			Compartment c = (Compartment) variable;
			return compile(c);
		}
		// TODO: weitere spezialfälle von Namen!!! PARAMETER, FUNCTION DEF,
		// REACTION.
		return new StringBuffer(mathtt(maskSpecialChars(variable.getId())));
		// else if (variable instanceof Parameter) {
		// return new StringBuffer("parameter");
		// }
		// return new StringBuffer("variable:"+variable);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.jsbml.ASTNodeCompiler#compile(java.lang.String)
	 */
	public StringBuffer compile(String name) {
		return new StringBuffer(maskSpecialChars(name));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.jsbml.ASTNodeCompiler#cos(org.sbml.jsbml.ASTNode)
	 */
	public StringBuffer cos(ASTNode node) {
		return function("\\cos", node);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.jsbml.ASTNodeCompiler#cosh(org.sbml.jsbml.ASTNode)
	 */
	public Object cosh(ASTNode node) {
		return function("\\cosh", node);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.jsbml.ASTNodeCompiler#cot(org.sbml.jsbml.ASTNode)
	 */
	public Object cot(ASTNode node) {
		return function("\\cot", node);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.jsbml.ASTNodeCompiler#coth(org.sbml.jsbml.ASTNode)
	 */
	public Object coth(ASTNode node) {
		return function("\\coth", node);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.jsbml.ASTNodeCompiler#csc(org.sbml.jsbml.ASTNode)
	 */
	public Object csc(ASTNode node) {
		return function("\\csc", node);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.jsbml.ASTNodeCompiler#csch(org.sbml.jsbml.ASTNode)
	 */
	public Object csch(ASTNode node) {
		return function("csch", node);
	}

	/*
	 * @see org.sbml.jsbml.ASTNodeCompiler#delay(org.sbml.jsbml.ASTNode, double)
	 */
	public StringBuffer delay(ASTNode x, double d) {
		return concat(mathrm("delay"), brackets(concat(x.compile(this), ", ", toString(d))));
	}

	/**
	 * This method simplifies the process of creating descriptions. There is an
	 * item entry together with a description. No new line or space is needed
	 * for separation.
	 * 
	 * @param item
	 *            e.g., "my item"
	 * @param description
	 *            e.g., "my description"
	 * @return
	 */
	public StringBuffer descriptionItem(String item, Object description) {
		StringBuffer itemBuffer = new StringBuffer("\\item[");
		itemBuffer.append(item);
		itemBuffer.append("] ");
		itemBuffer.append(description);
		itemBuffer.append(newLine);
		return itemBuffer;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.jsbml.ASTNodeCompiler#exp(org.sbml.jsbml.ASTNode)
	 */
	public Object exp(ASTNode node) {
		return function("\\exp", node);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.jsbml.ASTNodeCompiler#factorial(org.sbml.jsbml.ASTNode)
	 */
	public StringBuffer factorial(ASTNode node) {
		StringBuffer value;
		if (node.getNumChildren() > 0)
			value = brackets(node.compile(this));
		else
			value = new StringBuffer(node.compile(this).toString());
		value.append('!');
		return value;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.jsbml.ASTNodeCompiler#floor(org.sbml.jsbml.ASTNode)
	 */
	public StringBuffer floor(ASTNode formula) {
		StringBuffer floor = new StringBuffer("\\left\\lfloor ");
		floor.append(formula.compile(this));
		floor.append("\\right\\rfloor ");
		return floor;
	}

	/**
	 * This method returns a <code>StringBuffer</code> representing a properly
	 * LaTeX formatted number. However, if the <code>double</code> argument
	 * contains "Exx" (power of ten), then the returned value starts and ends
	 * with a dollar symbol.
	 * 
	 * @param value
	 * @return
	 */
	public StringBuffer format(double value) {
		StringBuffer sb = new StringBuffer();
		String val = Double.toString(value);
		if (val.contains("E")) {
			String split[] = val.split("E");
			val = "10^{" + format(Double.parseDouble(split[1])) + "}";
			if (split[0].equals("-1.0"))
				val = "-" + val;
			else if (!split[0].equals("1.0"))
				val = format(Double.parseDouble(split[0])) + "\\cdot " + val;
			sb.append(math(val));
		} else if (value - ((int) value) == 0)
			sb.append(((int) value));
		else
			sb.append(val);
		return sb;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.jsbml.ASTNodeCompiler#frac(org.sbml.jsbml.ASTNode,
	 * org.sbml.jsbml.ASTNode)
	 */
	public StringBuffer frac(ASTNode left, ASTNode right) {
		return frac(left.compile(this), right.compile(this));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.jsbml.ASTNodeCompiler#frac(int, int)
	 */
	public StringBuffer frac(int numerator, int denominator) {
		return frac(Integer.valueOf(numerator), Integer.valueOf(denominator));
	}

	/**
	 * 
	 * @param numerator
	 * @param denominator
	 * @return
	 */
	public StringBuffer frac(Object numerator, Object denominator) {
		return command("frac", numerator, denominator);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.jsbml.ASTNodeCompiler#function(java.lang.String,
	 * org.sbml.jsbml.ASTNode[])
	 */
	public StringBuffer function(FunctionDefinition fun, ASTNode... args) {
		StringBuffer value = new StringBuffer();
		int length;
		if (fun != null) {
			value.append(mathtt(LaTeX.maskSpecialChars(fun.getId())));
			length = args.length;
		} else
			length = args.length - 1;
		StringBuffer argList = new StringBuffer();

		for (int i = 0; i < args.length - 1; i++) {
			if (i > 0)
				argList.append(", ");
			argList.append(args[i].compile(this));
		}
		value.append(brackets(argList));
		if (length < args.length) {
			value.append(" = ");
			value.append(args[args.length - 1].compile(this));
		}
		return value;
	}

	/**
	 * Decides whether to produce brackets.
	 * 
	 * @param func
	 * @param node
	 * @return
	 */
	private StringBuffer function(String func, ASTNode node) {
		StringBuffer value = new StringBuffer();
		if (0 < node.getNumChildren())
			value.append(brackets(node.compile(this)));
		else
			value.append(node.compile(this));
		return function(func, value);
	}

	/**
	 * Without brackets.
	 * 
	 * @param func
	 * @param value
	 * @return
	 */
	private StringBuffer function(String func, Object value) {
		boolean command = func.startsWith("\\");
		StringBuffer fun = command ? new StringBuffer(func) : mathrm(func);
		if (command)
			fun.append('{');
		fun.append(value);
		if (command)
			fun.append('}');
		return fun;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.jsbml.ASTNodeCompiler#getConstantE()
	 */
	public StringBuffer getConstantE() {
		return new StringBuffer(CONSTANT_E);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.jsbml.ASTNodeCompiler#getConstantFalse()
	 */
	public StringBuffer getConstantFalse() {
		return new StringBuffer(CONSTANT_FALSE);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.jsbml.ASTNodeCompiler#getConstantPi()
	 */
	public StringBuffer getConstantPi() {
		return new StringBuffer(CONSTANT_PI);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.jsbml.ASTNodeCompiler#getConstantTrue()
	 */
	public StringBuffer getConstantTrue() {
		return new StringBuffer(CONSTANT_TRUE);
	}

	/**
	 * If the field printNameIfAvailable is false this method returns a the id
	 * of the given SBase. If printNameIfAvailable is true this method looks for
	 * the name of the given SBase and will return it.
	 * 
	 * @param sbase
	 *            the SBase, whose name or id is to be returned.
	 * @param mathMode
	 *            if true this method returns the name typesetted in mathmode,
	 *            i.e., mathrm for names and mathtt for ids, otherwise texttt
	 *            will be used for ids and normalfont (nothing) will be used for
	 *            names.
	 * @return The name or the ID of the SBase (according to the field
	 *         printNameIfAvailable), whose LaTeX special symbols are masked and
	 *         which is type set in typewriter font if it is an id. The mathmode
	 *         argument decides if mathtt or mathrm has to be used.
	 */
	private StringBuffer getNameOrID(NamedSBase sbase) {
		String name = "";
		if (sbase.isSetName() && printNameIfAvailable)
			name = sbase.getName();
		else if (sbase.isSetId())
			name = sbase.getId();
		else
			name = "Undefinded";
		name = maskSpecialChars(name);
		return printNameIfAvailable ? mathrm(name) : mathtt(name);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.jsbml.ASTNodeCompiler#getNegativeInfinity()
	 */
	public StringBuffer getNegativeInfinity() {
		return new StringBuffer(NEGATIVE_ININITY);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.jsbml.ASTNodeCompiler#getPositiveInfinity()
	 */
	public StringBuffer getPositiveInfinity() {
		return new StringBuffer(POSITIVE_INFINITY);
	}

	/**
	 * Creates a hyper link to the given target and the text to be visible in
	 * the document.
	 * 
	 * @param target
	 *            The target to which this link points to.
	 * @param text
	 *            The text to be written in the link.
	 * @return
	 */
	public StringBuffer href(String target, Object text) {
		return command("href", target, text);
	}

	/**
	 * 
	 * @param target
	 * @param text
	 * @return
	 */
	public StringBuffer hyperref(String target, Object text) {
		StringBuffer sb = new StringBuffer("\\hyperref[");
		sb.append(target);
		sb.append("]{");
		sb.append(text);
		sb.append('}');
		return sb;
	}

	/**
	 * 
	 * @return
	 */
	public boolean isPrintNameIfAvailable() {
		return printNameIfAvailable;
	}

	public StringBuffer label(String id) {
		return command("label", new StringBuffer(id));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.jsbml.ASTNodeCompiler#lambda(org.sbml.jsbml.ASTNode[])
	 */
	public StringBuffer lambda(ASTNode... nodes) {
		return function((FunctionDefinition) null, nodes);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.jsbml.ASTNodeCompiler#ln(org.sbml.jsbml.ASTNode)
	 */
	public Object ln(ASTNode node) {
		return function("\\ln", node);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.jsbml.ASTNodeCompiler#log(org.sbml.jsbml.ASTNode)
	 */
	public StringBuffer log(ASTNode node) {
		return log(null, node);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.jsbml.ASTNodeCompiler#log(org.sbml.jsbml.ASTNode,
	 * org.sbml.jsbml.ASTNode)
	 */
	public StringBuffer log(ASTNode left, ASTNode right) {
		StringBuffer value = new StringBuffer("\\log");
		if (left != null) {
			value.append("_{");
			value.append(left.compile(this));
			value.append('}');
		}
		value.append('{');
		if (right.getNumChildren() > 0)
			value.append(brackets(right.compile(this)));
		else
			value.append(right.compile(this));
		value.append('}');
		return value;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.jsbml.ASTNodeCompiler#logicalAND(org.sbml.jsbml.ASTNode[])
	 */
	public String logicalAND(ASTNode... nodes) {
		return logicalOperation(wedge, nodes);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.jsbml.ASTNodeCompiler#logicalNot(org.sbml.jsbml.ASTNode)
	 */
	public Object logicalNot(ASTNode node) {
		StringBuffer value = new StringBuffer("\\neg ");
		if (0 < node.getNumChildren())
			value.append(brackets(node.compile(this)));
		else
			value.append(node.compile(this));
		return value;
	}

	/**
	 * 
	 * @param symbol
	 * @param nodes
	 * @return
	 */
	private String logicalOperation(String symbol, ASTNode... nodes) {
		StringBuffer value = new StringBuffer();
		int i = 0;
		for (ASTNode node : nodes) {
			if (1 < node.getNumChildren())
				value.append(leftBrace);
			value.append(node.compile(this));
			if (1 < node.getNumChildren())
				value.append(rightBrace);
			if (i < nodes.length - 1)
				value.append(symbol);
			i++;
		}
		return value.toString();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.jsbml.ASTNodeCompiler#logicalOR(org.sbml.jsbml.ASTNode[])
	 */
	public String logicalOR(ASTNode... nodes) {
		return logicalOperation(or, nodes);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.jsbml.ASTNodeCompiler#logicalXOR(org.sbml.jsbml.ASTNode[])
	 */
	public String logicalXOR(ASTNode... nodes) {
		return logicalOperation(xor, nodes);
	}

	/**
	 * Creates a head for a longtable in LaTeX.
	 * 
	 * @param columnDef
	 *            without leading and ending brackets, e.g., "lrrc",
	 * @param caption
	 *            caption of this table without leading and ending brackets
	 * @param headLine
	 *            table head without leading and ending brackets and without
	 *            double backslashes at the end
	 * @return
	 */
	public StringBuffer longtableHead(String columnDef, String caption,
			String headLine) {
		StringBuffer buffer = new StringBuffer("\\begin{longtable}[h!]{");
		buffer.append(columnDef);
		buffer.append('}');
		buffer.append(newLine);
		buffer.append("\\caption{");
		buffer.append(caption);
		buffer.append('}');
		buffer.append("\\\\");
		StringBuffer head = new StringBuffer(toprule);
		head.append(headLine);
		head.append("\\\\");
		head.append(midrule);
		buffer.append(head);
		buffer.append("\\endfirsthead");
		// buffer.append(newLine);
		buffer.append(head);
		buffer.append("\\endhead");
		// buffer.append(bottomrule);
		// buffer.append("\\endlastfoot");
		buffer.append(newLine);
		return buffer;
	}

	/**
	 * Encloses the given formula in dollar symbols (inline math mode).
	 * 
	 * @param formula
	 * @return
	 */
	public StringBuffer math(Object formula) {
		StringBuffer math = new StringBuffer();
		String f = String.valueOf(formula);
		if (f.length() == 0)
			return math;
		if (f.charAt(0) != '$')
			math.append('$');
		math.append(f);
		if (f.charAt(f.length() - 1) != '$')
			math.append('$');
		return math;
	}

	/**
	 * 
	 * @param symbol
	 * @return
	 */
	public StringBuffer mathrm(char symbol) {
		return command("mathrm", Character.valueOf(symbol));
	}

	/**
	 * 
	 * @param text
	 * @return
	 */
	public StringBuffer mathrm(String text) {
		return command("mathrm", text);
	}

	/**
	 * 
	 * @param text
	 * @return
	 */
	public StringBuffer mathtext(String text) {
		return command("text", text);
	}

	/**
	 * Returns the LaTeX code to set the given String in type writer font within
	 * a math environment.
	 * 
	 * @param id
	 * @return
	 */
	public StringBuffer mathtt(String id) {
		return command("mathtt", new StringBuffer(id));
	}

	public StringBuffer mbox(String s) {
		StringBuffer sb = new StringBuffer();
		sb.append(" \\mbox{");
		sb.append(s);
		sb.append("} ");
		return sb;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.jsbml.ASTNodeCompiler#minus(org.sbml.jsbml.ASTNode[])
	 */
	public StringBuffer minus(ASTNode... nodes) {
		if (nodes.length == 0)
			return new StringBuffer();
		StringBuffer value = (StringBuffer) nodes[0].compile(this);
		for (int i = 1; i < nodes.length; i++) {
			value.append('-');
			if (nodes[i].getType() == Type.PLUS)
				value.append(brackets(nodes[i].compile(this)));
			else
				value.append(nodes[i].compile(this));
		}
		return value;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.jsbml.ASTNodeCompiler#piecewise(org.sbml.jsbml.ASTNode[])
	 */
	public StringBuffer piecewise(ASTNode... nodes) {
		StringBuffer value = new StringBuffer("\\begin{dcases}");
		value.append(newLine);
		for (int i = 0; i < nodes.length - 1; i++) {
			value.append(nodes[i].compile(this));
			value.append(((i % 2) == 0) ? " & \\text{if\\ } " : lineBreak);
		}
		value.append(nodes[nodes.length - 1].compile(this));
		if ((nodes.length % 2) == 1) {
			value.append(" & \\text{otherwise}");
			value.append(newLine);
		}
		value.append("\\end{dcases}");
		return value;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.jsbml.ASTNodeCompiler#plus(org.sbml.jsbml.ASTNode[])
	 */
	public StringBuffer plus(ASTNode... nodes) {
		if (nodes.length > 0) {
			StringBuffer value = (StringBuffer) nodes[0].compile(this);
			for (int i = 1; i < nodes.length; i++) {
				if (nodes[i].isUMinus())
					value.append(nodes[i].compile(this));
				else {
					value.append('+');
					if (nodes[i].getType() == Type.MINUS)
						value.append(brackets(nodes[i].compile(this)));
					else
						value.append(nodes[i].compile(this));
				}
			}
			return value;
		}
		return new StringBuffer();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.jsbml.ASTNodeCompiler#pow(org.sbml.jsbml.ASTNode,
	 * org.sbml.jsbml.ASTNode)
	 */
	public StringBuffer pow(ASTNode left, ASTNode right) {
		StringBuffer value = (StringBuffer) left.compile(this);
		if (left.getNumChildren() > 0)
			value = brackets(value);
		value.append('^');
		value.append('{');
		value.append(right.compile(this));
		value.append('}');
		return value;
	}

	/**
	 * Creates a relation between two astnodes.
	 * 
	 * @param left
	 * @param relationSymbol
	 * @param right
	 * @return
	 */
	private StringBuffer relation(ASTNode left, String relationSymbol,
			ASTNode right) {
		StringBuffer value = new StringBuffer();
		value.append(left.compile(this));
		value.append(" = ");
		value.append(right.compile(this));
		return value;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.jsbml.ASTNodeCompiler#relationEqual(org.sbml.jsbml.ASTNode,
	 * org.sbml.jsbml.ASTNode)
	 */
	public StringBuffer relationEqual(ASTNode left, ASTNode right) {
		return relation(left, " = ", right);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.sbml.jsbml.ASTNodeCompiler#relationGreaterEqual(org.sbml.jsbml.ASTNode
	 * , org.sbml.jsbml.ASTNode)
	 */
	public Object relationGreaterEqual(ASTNode left, ASTNode right) {
		return relation(left, " \\geq ", right);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.sbml.jsbml.ASTNodeCompiler#relationGreaterThan(org.sbml.jsbml.ASTNode
	 * , org.sbml.jsbml.ASTNode)
	 */
	public Object relationGreaterThan(ASTNode left, ASTNode right) {
		return relation(left, " > ", right);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.jsbml.ASTNodeCompiler#equal(java.lang.Object,
	 * java.lang.Object)
	 */
	public StringBuffer relationLessEqual(ASTNode left, ASTNode right) {
		StringBuffer value = new StringBuffer();
		value.append(left.compile(this));
		value.append(" \\leq ");
		value.append(right.compile(this));
		return value;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.jsbml.ASTNodeCompiler#lessThan(java.lang.Object,
	 * java.lang.Object)
	 */
	public StringBuffer relationLessThan(ASTNode left, ASTNode right) {
		StringBuffer value = new StringBuffer();
		value.append(left.compile(this));
		value.append(" < ");
		value.append(right.compile(this));
		return value;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.sbml.jsbml.ASTNodeCompiler#relationNotEqual(org.sbml.jsbml.ASTNode,
	 * org.sbml.jsbml.ASTNode)
	 */
	public StringBuffer relationNotEqual(ASTNode left, ASTNode right) {
		StringBuffer value = new StringBuffer();
		value.append(left.compile(this));
		value.append(" \\neq ");
		value.append(right.compile(this));
		return value;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.jsbml.ASTNodeCompiler#root(org.sbml.jsbml.ASTNode,
	 * org.sbml.jsbml.ASTNode)
	 */
	public StringBuffer root(ASTNode rootExponent, ASTNode value) {
		Object degree = rootExponent.compile(this);
		if (degree.toString().equals("2"))
			return sqrt(value);
		StringBuffer sqrt = new StringBuffer("\\sqrt");
		sqrt.append('[');
		sqrt.append(degree);
		sqrt.append("]{");
		sqrt.append(value.compile(this));
		sqrt.append('}');
		return sqrt;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.jsbml.ASTNodeCompiler#sec(org.sbml.jsbml.ASTNode)
	 */
	public Object sec(ASTNode node) {
		return function("\\sec", node);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.jsbml.ASTNodeCompiler#sech(org.sbml.jsbml.ASTNode)
	 */
	public Object sech(ASTNode node) {
		return function("sech", node);
	}

	public StringBuffer section(String title, boolean numbering) {
		return heading("section", title, numbering);
	}

	/**
	 * 
	 * @param printNameIfAvailable
	 */
	public void setPrintNameIfAvailable(boolean printNameIfAvailable) {
		this.printNameIfAvailable = printNameIfAvailable;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.jsbml.ASTNodeCompiler#sin(org.sbml.jsbml.ASTNode)
	 */
	public Object sin(ASTNode node) {
		return function("\\sin", node);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.jsbml.ASTNodeCompiler#sinh(org.sbml.jsbml.ASTNode)
	 */
	public Object sinh(ASTNode node) {
		return function("\\sinh", node);
	}

	/**
	 * 
	 * @param value
	 * @return
	 */
	public StringBuffer sqrt(ASTNode value) {
		return command("sqrt", value.compile(this));
	}

	/**
	 * 
	 * @param title
	 * @param numbering
	 * @return
	 */
	public StringBuffer subsection(String title, boolean numbering) {
		return heading("subsection", title, numbering);
	}

	/**
	 * 
	 * @param title
	 * @param numbering
	 * @return
	 */
	public StringBuffer subsubsection(String title, boolean numbering) {
		return heading("subsubsection", title, numbering);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.jsbml.ASTNodeCompiler#symbolTime(java.lang.String)
	 */
	public StringBuffer symbolTime(String time) {
		return mathrm(time);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.jsbml.ASTNodeCompiler#tan(org.sbml.jsbml.ASTNode)
	 */
	public Object tan(ASTNode node) {
		return function("\\tan", node);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.jsbml.ASTNodeCompiler#tanh(org.sbml.jsbml.ASTNode)
	 */
	public Object tanh(ASTNode node) {
		return function("\\tanh", node);
	}

	/**
	 * 
	 * @param color
	 * @param text
	 * @return
	 */
	public StringBuffer textcolor(String color, Object text) {
		return command("textcolor", color, text);
	}

	/**
	 * Returns the LaTeX code to set the given String in type writer font.
	 * 
	 * @param id
	 * @return
	 */
	public StringBuffer texttt(String id) {
		return command("texttt", new StringBuffer(id));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.jsbml.ASTNodeCompiler#times(org.sbml.jsbml.ASTNode[])
	 */
	public StringBuffer times(ASTNode... nodes) {
		if (nodes.length == 0)
			return new StringBuffer();
		StringBuffer value = (StringBuffer) nodes[0].compile(this);
		if (nodes[0].getNumChildren() > 1
				&& (nodes[0].getType() == Type.MINUS || nodes[0].getType() == Type.PLUS))
			value = brackets(value);
		for (int i = 1; i < nodes.length; i++) {
			value.append("\\cdot");
			if ((nodes[i].getType() == Type.MINUS || nodes[i].getType() == Type.PLUS)
					&& nodes[i].getNumChildren() > 1)
				value.append(brackets(nodes[i].compile(this)));
			else {
				value.append(' ');
				value.append(nodes[i].compile(this));
			}
		}
		return value;
	}

	/**
	 * 
	 * @param factors
	 * @return
	 */
	public StringBuffer times(Object... factors) {
		return times("\\cdot", factors);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.jsbml.ASTNodeCompiler#uiMinus(org.sbml.jsbml.ASTNode)
	 */
	public StringBuffer uiMinus(ASTNode node) {
		StringBuffer value = new StringBuffer();
		value.append(node.getCharacter());
		if (node.getLeftChild().getNumChildren() > 0
				&& node.getLeftChild().getType() != Type.TIMES)
			value.append(brackets(node.getLeftChild().compile(this)));
		else
			value.append(node.getLeftChild().compile(this));
		return value;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.jsbml.ASTNodeCompiler#unknownASTNode()
	 */
	public StringBuffer unknownASTNode() {
		return mathtext(" unknown ");
	}

	/**
	 * Creates a usepackage command for the given package with the optional
	 * options.
	 * 
	 * @param latexPackage
	 *            the name of the latex package
	 * @param options
	 *            options without commas
	 * @return usepackage command including system-dependent new line character.
	 */
	public StringBuffer usepackage(String latexPackage, String... options) {
		StringBuffer usepackage = new StringBuffer("\\usepackage");
		if (options.length > 0) {
			usepackage.append('[');
			boolean first = true;
			for (String option : options) {
				if (!first)
					usepackage.append(',');
				else
					first = false;
				usepackage.append(option);
			}
			usepackage.append(']');
		}
		usepackage.append('{');
		usepackage.append(latexPackage);
		usepackage.append('}');
		usepackage.append(newLine);
		return usepackage;
	}
}
