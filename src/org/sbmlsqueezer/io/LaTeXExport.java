/**
 * @date Nov, 2007
 * @since 2.0
 * @version
 * @author Andreas Dr&auml;ger (draeger) <andreas.draeger@uni-tuebingen.de>
 * @author Dieudonne Motsou Wouamba <dwouamba@yahoo.fr> Copyright (c) ZBiT,
 *         University of T&uuml;bingen, Germany Compiler: JDK 1.6.0
 */
package org.sbmlsqueezer.io;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.StringTokenizer;
import java.util.Vector;

import jp.sbi.celldesigner.plugin.PluginEvent;
import jp.sbi.celldesigner.plugin.PluginModel;
import jp.sbi.celldesigner.plugin.PluginReaction;
import jp.sbi.celldesigner.plugin.PluginSpecies;
import jp.sbi.celldesigner.plugin.PluginSpeciesReference;

import org.sbml.libsbml.ASTNode;
import org.sbml.libsbml.libsbmlConstants;

/**
 * This class is used to export a sbml model as LaTex file.
 * 
 * @since 2.0
 * @version
 * @author Dieudonne Motsou Wouamba <dwouamba@yahoo.fr>
 * @author Andreas Dr&auml;ger (draeger) <andreas.draeger@uni-tuebingen.de>
 *         Copyright (c) ZBiT, University of T&uuml;bingen, Germany Compiler:
 *         JDK 1.6.0
 * @date Dec 4, 2007
 */
public class LaTeXExport implements libsbmlConstants {

	/**
	 * Method that writes the kinetic law (mathematical formula) of into latex
	 * code
	 * 
	 * @param astnode
	 * @return String
	 */
	public static String toLaTeX(PluginModel model, ASTNode astnode) {
		String value;

		if (astnode.isUMinus()) {
			return (astnode.getLeftChild().getLeftChild() != null) ? "- \\left("
					+ toLaTeX(model, astnode.getLeftChild()) + "\\right)"
					: "-" + toLaTeX(model, astnode.getLeftChild());
		} else if (astnode.isSqrt()) {
			return "\\sqrt{" + toLaTeX(model, astnode.getLeftChild()) + "}";
		} else if (astnode.isNumber()) {
			if (astnode.getReal() == Double.POSITIVE_INFINITY)
				return "\\infty";
			if (astnode.getReal() == Double.NEGATIVE_INFINITY)
				return "-\\infty";
		} else if (astnode.getType() == AST_FUNCTION_LOG) {
			return (astnode.getRightChild().getLeftChild() != null) ? "\\log_{"
					+ toLaTeX(model, astnode.getRightChild()) + "} {\\left("
					+ toLaTeX(model, astnode.getLeftChild()) + "\\right)}"
					: "\\log_{" + toLaTeX(model, astnode.getRightChild())
							+ "} {" + toLaTeX(model, astnode.getLeftChild())
							+ "}";
		}
		/*
		 * Numbers
		 */
		switch (astnode.getType()) {
		case AST_REAL:
			double d = astnode.getReal();
			return (((int) d) - d == 0) ? Integer.toString((int) d) : Double
					.toString(d);
		case AST_INTEGER:
			return Integer.toString(astnode.getInteger());
			/*
			 * Basic Functions
			 */
		case AST_FUNCTION_LOG:
			return (astnode.getRightChild().getLeftChild() != null) ? "\\log {\\left("
					+ toLaTeX(model, astnode.getRightChild()) + "\\right)}"
					: "\\log {" + toLaTeX(model, astnode.getRightChild()) + "}";
			/*
			 * Operators
			 */
		case AST_POWER:
			if (toLaTeX(model, astnode.getRightChild()).equals("1"))
				return (astnode.getLeftChild().getLeftChild() != null) ? "\\left("
						+ toLaTeX(model, astnode.getLeftChild()) + "\\right)"
						: toLaTeX(model, astnode.getLeftChild());
			else
				return (astnode.getLeftChild().getLeftChild() != null) ? "\\left("
						+ toLaTeX(model, astnode.getLeftChild())
						+ "\\right)^{"
						+ toLaTeX(model, astnode.getRightChild()) + "}"
						: toLaTeX(model, astnode.getLeftChild()) + "^{"
								+ toLaTeX(model, astnode.getRightChild()) + "}";
		case AST_PLUS:
			if (astnode.getNumChildren() > 0) {
				value = toLaTeX(model, astnode.getLeftChild());
				ASTNode ast;
				for (int i = 1; i < astnode.getNumChildren(); i++) {
					ast = astnode.getChild(i);
					switch (ast.getType()) {
					case AST_MINUS:
						value = value + "+ \\left(" + toLaTeX(model, ast)
								+ "\\right)";
						break;
					default:
						value = value + " + " + toLaTeX(model, ast);
						break;
					}

				}

				return value;
			}
		case AST_MINUS:
			if (astnode.getNumChildren() > 0) {
				value = toLaTeX(model, astnode.getLeftChild());
				ASTNode ast;
				for (int i = 1; i < astnode.getNumChildren(); i++) {
					ast = astnode.getChild(i);
					switch (ast.getType()) {
					case AST_PLUS:
						value = value + " -  \\left(" + toLaTeX(model, ast)
								+ "\\right)";
						break;
					default:
						value = value + " - " + toLaTeX(model, ast);
						break;
					}

				}

				return value;
			}
		case AST_TIMES:
			if (astnode.getNumChildren() > 0) {
				value = toLaTeX(model, astnode.getLeftChild());
				if (astnode.getLeftChild().getNumChildren() > 1
						&& (astnode.getLeftChild().getType() == AST_MINUS || astnode
								.getLeftChild().getType() == AST_PLUS))
					value = "\\left(" + value + "\\right)";
				ASTNode ast;
				for (int i = 1; i < astnode.getNumChildren(); i++) {
					ast = astnode.getChild(i);
					switch (ast.getType()) {
					case AST_MINUS:
						value = value + "\\cdot\\left(" + toLaTeX(model, ast)
								+ "\\right)";
						break;
					case AST_PLUS:
						value = value + "\\cdot\\left(" + toLaTeX(model, ast)
								+ "\\right)";
						break;
					default:
						value = value + "\\cdot " + toLaTeX(model, ast);
						break;
					}
				}
				return value;
			}

		case AST_DIVIDE:
			String num = toLaTeX(model, astnode.getLeftChild());
			String denum = toLaTeX(model, astnode.getRightChild());

			return "\\frac{" + num + "}{" + denum + "}";

		case AST_RATIONAL:
			if (Double.toString(astnode.getDenominator()).toString()
					.equals("1"))
				return Double.toString(astnode.getNumerator());
			else
				return "\\frac{" + Double.toString(astnode.getNumerator())
						+ "}{" + Double.toString(astnode.getDenominator())
						+ "}";

			/*
			 * Names of identifiers: parameters, functions, species etc.
			 */
		case AST_NAME:
			// Species.
			if (model.getSpecies(astnode.getName()) != null) {
				return "[" + idToTeX(model.getSpecies(astnode.getName())) + "]";
			} else if (model.getCompartment(astnode.getName()) != null) {
				return "Vol(" + toTeX(astnode.getName()) + ")";
			}
			return toTeX(astnode.getName());
			/*
			 * Constants: pi, e, true, false
			 */

		case AST_CONSTANT_PI:
			return "\\pi";
		case AST_CONSTANT_E:
			return "\\mathrm{e}";
		case AST_CONSTANT_TRUE:
			return "\\mathbf{true}";
		case AST_CONSTANT_FALSE:
			return "\\mathbf{false}";
		case AST_REAL_E:
			return Double.toString(astnode.getReal());
			/*
			 * More complicated functions
			 */
		case AST_FUNCTION_ABS:
			return "\\left\\lvert" + toLaTeX(model, astnode.getRightChild())
					+ "\\right\\rvert";

		case AST_FUNCTION_ARCCOS:
			return (astnode.getLeftChild().getLeftChild() != null) ? "\\arccos{\\left("
					+ toLaTeX(model, astnode.getLeftChild()) + "\\right)}"
					: "\\arccos{" + toLaTeX(model, astnode.getLeftChild())
							+ "}";
		case AST_FUNCTION_ARCCOSH:
			return (astnode.getLeftChild().getLeftChild() != null) ? "\\mathrm{arccosh}{\\left("
					+ toLaTeX(model, astnode.getLeftChild()) + "\\right)}"
					: "\\mathrm{arccosh}{"
							+ toLaTeX(model, astnode.getLeftChild()) + "}";

		case AST_FUNCTION_ARCCOT:
			return (astnode.getLeftChild().getLeftChild() != null) ? "\\arcot{\\left("
					+ toLaTeX(model, astnode.getLeftChild()) + "\\right)}"
					: "\\arcot{" + toLaTeX(model, astnode.getLeftChild()) + "}";

		case AST_FUNCTION_ARCCOTH:
			return (astnode.getLeftChild().getLeftChild() != null) ? "\\mathrm{arccoth}{\\left("
					+ toLaTeX(model, astnode.getLeftChild()) + "\\right)}"
					: "\\mathrm{arccoth}{"
							+ toLaTeX(model, astnode.getLeftChild()) + ")}";

		case AST_FUNCTION_ARCCSC:
			return (astnode.getLeftChild().getLeftChild() != null) ? "\\arccsc{\\left("
					+ toLaTeX(model, astnode.getLeftChild()) + "\\right)}"
					: "\\arccsc{" + toLaTeX(model, astnode.getLeftChild())
							+ "}";

		case AST_FUNCTION_ARCCSCH:
			return (astnode.getLeftChild().getLeftChild() != null) ? "\\mathrm{arccsh}\\left("
					+ toLaTeX(model, astnode.getLeftChild()) + "\\right)"
					: "\\mathrm{arccsh}"
							+ toLaTeX(model, astnode.getLeftChild()) + "}";

		case AST_FUNCTION_ARCSEC:
			return (astnode.getLeftChild().getLeftChild() != null) ? "\\arcsec{\\left("
					+ toLaTeX(model, astnode.getLeftChild()) + "\\right)}"
					: "\\arcsec{" + toLaTeX(model, astnode.getLeftChild())
							+ "}";

		case AST_FUNCTION_ARCSECH:
			return (astnode.getLeftChild().getLeftChild() != null) ? "\\mathrm{arcsech}{\\left("
					+ toLaTeX(model, astnode.getLeftChild()) + "\\right)}"
					: "\\mathrm{arcsech}{"
							+ toLaTeX(model, astnode.getLeftChild()) + "}";

		case AST_FUNCTION_ARCSIN:
			return (astnode.getLeftChild().getLeftChild() != null) ? "\\arcsin{\\left("
					+ toLaTeX(model, astnode.getLeftChild()) + "\\right)}"
					: "\\arcsin{" + toLaTeX(model, astnode.getLeftChild())
							+ "}";

		case AST_FUNCTION_ARCSINH:
			return (astnode.getLeftChild().getLeftChild() != null) ? "\\mathrm{arcsinh}{\\left("
					+ toLaTeX(model, astnode.getLeftChild()) + "\\right)}"
					: "\\mathrm{arcsinh}{"
							+ toLaTeX(model, astnode.getLeftChild()) + "}";

		case AST_FUNCTION_ARCTAN:
			return (astnode.getLeftChild().getLeftChild() != null) ? "\\arctan{\\left("
					+ toLaTeX(model, astnode.getLeftChild()) + "\\right)}"
					: "\\arctan{" + toLaTeX(model, astnode.getLeftChild())
							+ "}";

		case AST_FUNCTION_ARCTANH:
			return (astnode.getLeftChild().getLeftChild() != null) ? "\\arctanh{\\left("
					+ toLaTeX(model, astnode.getLeftChild()) + "\\right)}"
					: "\\arctanh{" + toLaTeX(model, astnode.getLeftChild())
							+ "}";

		case AST_FUNCTION_CEILING:
			return (astnode.getLeftChild().getLeftChild() != null) ? "\\ceiling{\\left("
					+ toLaTeX(model, astnode.getLeftChild()) + "\\right)}"
					: "\\ceiling{" + toLaTeX(model, astnode.getLeftChild())
							+ "}";
		case AST_FUNCTION_COS:
			return (astnode.getLeftChild().getLeftChild() != null) ? "\\cos{\\left("
					+ toLaTeX(model, astnode.getLeftChild()) + "\\right)}"
					: "\\cos{" + toLaTeX(model, astnode.getLeftChild()) + "}";

		case AST_FUNCTION_COSH:
			return (astnode.getLeftChild().getLeftChild() != null) ? "\\cosh{\\left("
					+ toLaTeX(model, astnode.getLeftChild()) + "\\right)}"
					: "\\cosh{" + toLaTeX(model, astnode.getLeftChild()) + "}";

		case AST_FUNCTION_COT:
			return (astnode.getLeftChild().getLeftChild() != null) ? "\\cot{\\left("
					+ toLaTeX(model, astnode.getLeftChild()) + "\\right)}"
					: "\\cot{" + toLaTeX(model, astnode.getLeftChild()) + "}";

		case AST_FUNCTION_COTH:
			return (astnode.getLeftChild().getLeftChild() != null) ? "\\coth{\\left("
					+ toLaTeX(model, astnode.getLeftChild()) + "\\right)}"
					: "\\coth{" + toLaTeX(model, astnode.getLeftChild()) + "}";

		case AST_FUNCTION_CSC:
			return (astnode.getLeftChild().getLeftChild() != null) ? "\\csc{\\left("
					+ toLaTeX(model, astnode.getLeftChild()) + "\\right)}"
					: "\\csc{" + toLaTeX(model, astnode.getLeftChild()) + "}";

		case AST_FUNCTION_CSCH:
			return (astnode.getLeftChild().getLeftChild() != null) ? "\\mathrm{csch}{\\left("
					+ toLaTeX(model, astnode.getLeftChild()) + "\\right)}"
					: "\\mathrm{csch}{"
							+ toLaTeX(model, astnode.getLeftChild()) + "}";

		case AST_FUNCTION_EXP:
			return (astnode.getLeftChild().getLeftChild() != null) ? "\\exp{\\left("
					+ toLaTeX(model, astnode.getLeftChild()) + "\\right)}"
					: "\\exp{" + toLaTeX(model, astnode.getLeftChild()) + "}";

		case AST_FUNCTION_FACTORIAL:
			return (astnode.getLeftChild().getLeftChild() != null) ? "\\left("
					+ toLaTeX(model, astnode.getLeftChild()) + "\\right)!"
					: toLaTeX(model, astnode.getLeftChild()) + "!";

		case AST_FUNCTION_FLOOR:
			return (astnode.getLeftChild().getLeftChild() != null) ? "\\mathrm{floor}\\left("
					+ toLaTeX(model, astnode.getLeftChild()) + "\\right)"
					: "\\mathrm{floor}"
							+ toLaTeX(model, astnode.getLeftChild());

		case AST_FUNCTION_LN:
			return (astnode.getLeftChild().getLeftChild() != null) ? "\\ln{\\left("
					+ toLaTeX(model, astnode.getLeftChild()) + "\\right)}"
					: "\\ln{" + toLaTeX(model, astnode.getLeftChild()) + "}";

		case AST_FUNCTION_POWER:
			return (astnode.getLeftChild().getLeftChild() != null) ? "\\left("
					+ toLaTeX(model, astnode.getLeftChild()) + "\\right)^{"
					+ toLaTeX(model, astnode.getRightChild()) + "}" : toLaTeX(
					model, astnode.getLeftChild())
					+ "^{" + toLaTeX(model, astnode.getRightChild()) + "}";

		case AST_FUNCTION_ROOT:
			return (astnode.getRightChild() != null) ? "\\sqrt["
					+ toLaTeX(model, astnode.getLeftChild()) + "]{"
					+ toLaTeX(model, astnode.getRightChild()) + "}" : "\\sqrt{"
					+ toLaTeX(model, astnode.getLeftChild()) + "}";

		case AST_FUNCTION_SEC:
			return (astnode.getLeftChild().getLeftChild() != null) ? "\\sec{\\left("
					+ toLaTeX(model, astnode.getLeftChild()) + "\\right)}"
					: "\\sec{" + toLaTeX(model, astnode.getLeftChild()) + "}";

		case AST_FUNCTION_SECH:
			return (astnode.getLeftChild().getLeftChild() != null) ? "\\mathrm{sech}{\\left("
					+ toLaTeX(model, astnode.getLeftChild()) + "\\right)}"
					: "\\mathrm{sech}{"
							+ toLaTeX(model, astnode.getLeftChild()) + "}";

		case AST_FUNCTION_SIN:
			return (astnode.getLeftChild().getLeftChild() != null) ? "\\sin{\\left("
					+ toLaTeX(model, astnode.getLeftChild()) + "\\right)}"
					: "\\sin{" + toLaTeX(model, astnode.getLeftChild()) + "}";

		case AST_FUNCTION_SINH:
			return (astnode.getLeftChild().getLeftChild() != null) ? "\\sinh{\\left("
					+ toLaTeX(model, astnode.getLeftChild()) + "\\right)}"
					: "\\sinh{" + toLaTeX(model, astnode.getLeftChild()) + "}";

		case AST_FUNCTION_TAN:
			return (astnode.getLeftChild().getLeftChild() != null) ? "\\tan{"
					+ toLaTeX(model, astnode.getLeftChild()) + "\\right)}"
					: "\\tan{\\left(" + toLaTeX(model, astnode.getLeftChild())
							+ "}";

		case AST_FUNCTION_TANH:
			return (astnode.getLeftChild().getLeftChild() != null) ? "\\tanh{\\left("
					+ toLaTeX(model, astnode.getLeftChild()) + "\\right)}"
					: "\\tanh{" + toLaTeX(model, astnode.getLeftChild()) + "}";
			/*
			 * TODO Lambda
			 */

		case AST_LAMBDA:
			break;

		case AST_LOGICAL_AND:
			return (astnode.getLeftChild().getLeftChild() != null) ? "\\left("
					+ toLaTeX(model, astnode.getLeftChild()) + "\\wedge"
					+ toLaTeX(model, astnode.getRightChild()) + "\\right)}"
					: "" + toLaTeX(model, astnode.getLeftChild()) + "\\wedge"
							+ toLaTeX(model, astnode.getRightChild()) + "}";

		case AST_LOGICAL_XOR:
			return (astnode.getLeftChild().getLeftChild() != null) ? "\\left("
					+ toLaTeX(model, astnode.getLeftChild()) + "\\mathrm{XOR}"
					+ toLaTeX(model, astnode.getRightChild()) + "\\right)}"
					: "" + toLaTeX(model, astnode.getLeftChild())
							+ "\\mathrm{XOR}"
							+ toLaTeX(model, astnode.getRightChild()) + "}";

		case AST_LOGICAL_OR:
			return (astnode.getLeftChild().getLeftChild() != null) ? "\\left("
					+ toLaTeX(model, astnode.getLeftChild()) + "\\lor"
					+ toLaTeX(model, astnode.getRightChild()) + "\\right)}"
					: "" + toLaTeX(model, astnode.getLeftChild()) + "\\lor"
							+ toLaTeX(model, astnode.getRightChild()) + "}";

		case AST_LOGICAL_NOT:
			return (astnode.getLeftChild().getLeftChild() != null) ? "\\neg\\left("
					+ toLaTeX(model, astnode.getLeftChild()) + "\\right)"
					: "\\neg" + toLaTeX(model, astnode.getLeftChild());

		case AST_FUNCTION_PIECEWISE:
			return "\\$\\${" + " -" + toLaTeX(model, astnode.getLeftChild())
					+ "& for $ " + toLaTeX(model, astnode.getLeftChild())
					+ "<0$\\" + " 0 & for $"
					+ toLaTeX(model, astnode.getLeftChild()) + "=0$\\"
					+ toLaTeX(model, astnode.getLeftChild()) + "& for $"
					+ toLaTeX(model, astnode.getLeftChild()) + ">0$\\}\\$\\$";

		case AST_RELATIONAL_EQ:
			return toLaTeX(model, astnode.getLeftChild()) + " \\eq "
					+ toLaTeX(model, astnode.getRightChild());
		case AST_RELATIONAL_GEQ:
			return toLaTeX(model, astnode.getLeftChild()) + "\\geq "
					+ toLaTeX(model, astnode.getRightChild());
		case AST_RELATIONAL_GT:
			return toLaTeX(model, astnode.getLeftChild()) + " > "
					+ toLaTeX(model, astnode.getRightChild());
		case AST_RELATIONAL_NEQ:
			return toLaTeX(model, astnode.getLeftChild()) + " \\neq "
					+ toLaTeX(model, astnode.getRightChild());
		case AST_RELATIONAL_LEQ:
			return toLaTeX(model, astnode.getLeftChild()) + " \\leq "
					+ toLaTeX(model, astnode.getRightChild());
		case AST_RELATIONAL_LT:
			return toLaTeX(model, astnode.getLeftChild()) + " < "
					+ toLaTeX(model, astnode.getRightChild());
		case AST_UNKNOWN:
			return "\\text{unknown}";

		default:
			break;
		}
		return null;
	}

	/**
	 * Writing laTeX code of a string name
	 * 
	 * @param name
	 * @return String
	 */
	private static String toTeX(String name) {
		String tex = "";
		String help = "";
		String sign = "";
		if (name.toLowerCase().startsWith("kass")) {
			tex += "k^\\mathrm{ass}";
			name = name.substring(4, name.length());
		} else if (name.toLowerCase().startsWith("kcatp")) {
			tex += "k^\\mathrm{cat}";
			name = name.substring(5, name.length());
			sign = "+";
		} else if (name.toLowerCase().startsWith("kcatn")) {
			tex += "k^\\mathrm{cat}";
			name = name.substring(5, name.length());
			sign = "-";
		} else if (name.toLowerCase().startsWith("kdiss")) {
			tex += "k^\\mathrm{diss}";
			name = name.substring(5, name.length());
		} else if (name.toLowerCase().startsWith("km")) {
			tex += "k^\\mathrm{m}";
			name = name.substring(2, name.length());
		} else if (name.toLowerCase().startsWith("ki")) {
			tex += "k^\\mathrm{i}";
			name = name.substring(2, name.length());
		} else {
			int j = 0;
			while (j < name.length() && !(name.substring(j, j + 1).equals("_"))
					&& !(Character.isDigit(name.charAt(j)))) {
				tex += name.substring(j, j + 1);
				j++;
			}
			name = name.substring(j - 1, name.length());
		}
		String s = "_{" + sign;
		String nameIndex = "";
		for (int i = 0; i < name.length(); i++) {
			if (i > 0) {
				nameIndex = name.substring(i, i + 1);
				if (Character.isDigit(name.charAt(i))) {
					int k = i;
					while (i < name.length()) {
						if (Character.isDigit(name.charAt(i)))
							i++;
						else
							break;
					}
					nameIndex = name.substring(k, i);
					if (name.substring(k - 1, k).equals("_")) {
						if (s.endsWith("{") || s.endsWith("+")
								|| s.endsWith("-"))
							s += nameIndex;
						else if (!s.endsWith(","))
							s += ", " + nameIndex;
					} else {
						if (s.endsWith("{")) {
							s += help + "_{" + nameIndex + "}";
							help = "";
						} else {
							s += ", " + help + "_{" + nameIndex + "}";
							help = "";
						}
					}
				} else if (!nameIndex.equals("_"))
					help += nameIndex;
			}
		}
		s += "}";
		return tex + s;
	}

	private static String name_idToLaTex(String s) {
		return "$" + toTeX(s) + "$";
	}

	/**
	 * Writing laTeX code of a string name
	 * 
	 * @param name
	 * @return String
	 */
	public static String nameToTeX(String name) {
		String speciesTeX = name;
		int numUnderscore = (new StringTokenizer(speciesTeX, "_"))
				.countTokens() - 1;
		if (numUnderscore > 1)
			speciesTeX = replaceAll("_", speciesTeX, "\\_");
		else if ((numUnderscore == 0) && (0 < speciesTeX.length())) {
			int index = -1;
			while (!Character.isDigit(name.charAt(index + 1)))
				index++;
			if ((-1 < index) && (index < name.length())) {
				String num = name.substring(++index);
				speciesTeX = speciesTeX.substring(0, index++) + "_";
				speciesTeX += (num.length() == 1) ? num : "{" + num + "}";
			}
		}
		return speciesTeX;
	}

	/**
	 * a methode for string replacement
	 * 
	 * @param what
	 * @param inString
	 * @param replacement
	 * @return string
	 */
	public static String replaceAll(String what, String inString,
			String replacement) {
		StringTokenizer st = new StringTokenizer(inString, what);
		String end = st.nextElement().toString();
		while (st.hasMoreElements())
			end += replacement + st.nextElement().toString();
		return end;
	}

	/**
	 * Writing laTeX code of a string id
	 * 
	 * @param pluginSpecies
	 * @return String
	 */
	public static String idToTeX(PluginSpecies pluginSpecies) {
		return nameToTeX(pluginSpecies.getId());
	}

	/**
	 * Writing a laTeX file
	 * 
	 * @param model
	 * @param file
	 * @throws IOException
	 */
	public static void toLaTeX(PluginModel model, File file) throws IOException {
		BufferedWriter bw = new BufferedWriter(new FileWriter(file));
		bw.write(toLaTeX(model));
		bw.close();
	}

	/**
	 * This is a method to write the latex file
	 * 
	 * @param astnode
	 * @param file
	 * @throws IOException
	 */

	public static String toLaTeX(PluginModel model) {
		String latex;
		String newLine = System.getProperty("line.separator");
		String title = model.getName().length() > 0 ? model.getName()
				.replaceAll("_", " ") : model.getId().replaceAll("_", " ");
		String head = "\\documentclass[11pt,a4paper]{scrartcl}"
				+ newLine
				+ "\\usepackage[scaled=.9]{helvet}"
				+ newLine
				+ "\\usepackage{amsmath}"
				+ newLine
				+ "\\usepackage{courier}"
				+ newLine
				+ "\\usepackage{times}"
				+ newLine
				+ "\\usepackage[english]{babel}"
				+ newLine
				+ "\\usepackage{a4wide}"
				+ newLine
				+ "\\usepackage{longtable}"
				+ newLine
				+ "\\usepackage{booktabs}"
				+ newLine
				+ "\\title{\\textsc{SBMLsqeezer}: Differential Equation System ``"
				+ title + "\"}" + newLine + "\\date{\\today}" + newLine
				+ "\\begin{document}" + newLine + "\\author{}" + newLine
				+ "\\maketitle" + newLine;

		String rateHead = newLine + "\\section{Rate Laws}" + newLine;
		String speciesHead = newLine + "\\section{Equations}";
		String begin = newLine + "\\begin{equation}" + newLine;
		String end = newLine + "\\end{equation}" + newLine + newLine;
		String tail = newLine + "\\end{document}" + newLine + newLine;

		String rateLaws[] = new String[(int) model.getNumReactions()];
		String sp[] = new String[(int) model.getNumSpecies()];
		int reactionIndex, speciesIndex, sReferenceIndex;
		PluginSpecies species;
		PluginSpeciesReference speciesRef;
		HashMap<String, Integer> speciesIDandIndex = new HashMap<String, Integer>();
		for (reactionIndex = 0; reactionIndex < model.getNumReactions(); reactionIndex++) {
			PluginReaction r = model.getReaction(reactionIndex);
			int latexReactionIndex = reactionIndex + 1;

			rateLaws[reactionIndex] = (!r.getName().equals("") && !r.getName()
					.equals(r.getId())) ? "\\subsection{Reaction: \\texttt{"
					+ replaceAll("_", r.getId(), "\\_") + "}" + " ("
					+ replaceAll("_", r.getName(), "\\_") + ")}" + newLine
					+ begin + "v_{" + latexReactionIndex + "}="
					: "\\subsection{Reaction: \\texttt{"
							+ replaceAll("_", r.getId(), "\\_") + "}}"
							+ newLine + begin + "v_{" + latexReactionIndex
							+ "}=";
			if (r.getKineticLaw() != null) {
				if (r.getKineticLaw().getMath() != null)
					rateLaws[reactionIndex] += toLaTeX(model, r.getKineticLaw()
							.getMath());
				else
					rateLaws[reactionIndex] += "\\text{no mathematics specified}";
			} else
				rateLaws[reactionIndex] += "\\text{no kinetic law specified}";
			for (speciesIndex = 0; speciesIndex < model.getNumSpecies(); speciesIndex++) {
				speciesIDandIndex.put(model.getSpecies(speciesIndex).getId(),
						Integer.valueOf(speciesIndex));
			}
		}

		Vector<PluginSpecies> reactants = new Vector<PluginSpecies>();
		Vector<PluginSpecies> products = new Vector<PluginSpecies>();
		Vector<Integer> reactantsReaction = new Vector<Integer>();
		Vector<Integer> productsReaction = new Vector<Integer>();
		Vector<PluginSpeciesReference> reactantsStochiometric = new Vector<PluginSpeciesReference>();
		Vector<PluginSpeciesReference> productsStochiometric = new Vector<PluginSpeciesReference>();

		for (reactionIndex = 0; reactionIndex < model.getNumReactions(); reactionIndex++) {
			PluginReaction r = model.getReaction(reactionIndex);
			int latexReactionIndex = reactionIndex + 1;
			int reactant = 0;
			int product = 0;
			for (sReferenceIndex = 0; sReferenceIndex < r.getNumReactants(); sReferenceIndex++) {
				speciesRef = r.getReactant(sReferenceIndex);
				speciesIndex = (int) speciesIDandIndex.get(
						speciesRef.getSpecies()).longValue();
				species = model.getSpecies(speciesIndex);
				reactants.add(reactant, species);
				reactantsReaction.add(reactant, latexReactionIndex);
				reactantsStochiometric.add(reactant, speciesRef);
				reactant++;
			}

			for (sReferenceIndex = 0; sReferenceIndex < r.getNumProducts(); sReferenceIndex++) {
				speciesRef = r.getProduct(sReferenceIndex);
				speciesIndex = (int) speciesIDandIndex.get(
						speciesRef.getSpecies()).longValue();
				species = model.getSpecies(speciesIndex);
				products.add(product, species);
				productsReaction.add(product, latexReactionIndex);
				productsStochiometric.add(product, speciesRef);
				product++;
			}
		}
		for (speciesIndex = 0; speciesIndex < model.getNumSpecies(); speciesIndex++) {
			String sEquation = "";
			ASTNode stoch;
			species = model.getSpecies(speciesIndex);
			for (int k = 0; k < reactants.size(); k++) {
				if (species.getId().equals(reactants.get(k).getId())) {
					stoch = reactantsStochiometric.get(k)
							.getStoichiometryMath();
					if (stoch != null) {
						sEquation += (stoch.getType() == AST_PLUS || stoch
								.getType() == AST_MINUS) ? sEquation += "-\\left("
								+ toLaTeX(model, stoch)
								+ "\\right)v_{"
								+ reactantsReaction.get(k) + "}"
								: "-" + toLaTeX(model, stoch) + "v_{"
										+ reactantsReaction.get(k) + "}";
					} else {
						double doubleStoch = reactantsStochiometric.get(k)
								.getStoichiometry();
						if (doubleStoch == 1.0)
							sEquation += "-v_{" + reactantsReaction.get(k)
									+ "}";
						else {
							int intStoch = (int) doubleStoch;
							if ((doubleStoch - intStoch) == 0.0)
								sEquation += "-" + intStoch + "v_{"
										+ reactantsReaction.get(k) + "}";
							else
								sEquation += "-" + doubleStoch + "v_{"
										+ reactantsReaction.get(k) + "}";
						}
					}
				}
			}
			for (int k = 0; k < products.size(); k++) {
				if (species.getId().equals(products.get(k).getId())) {
					stoch = productsStochiometric.get(k).getStoichiometryMath();
					if (sEquation == "") {

						if (stoch != null) {
							sEquation += (stoch.getType() == AST_PLUS || stoch
									.getType() == AST_MINUS) ? sEquation += "\\left("
									+ toLaTeX(model, stoch)
									+ "\\right)v_{"
									+ productsReaction.get(k) + "}"
									: toLaTeX(model, stoch) + "v_{"
											+ productsReaction.get(k) + "}";
						} else {
							double doubleStoch = productsStochiometric.get(k)
									.getStoichiometry();
							if (doubleStoch == 1.0)
								sEquation += "v_{" + productsReaction.get(k)
										+ "}";
							else {
								int intStoch = (int) doubleStoch;
								if ((doubleStoch - intStoch) == 0.0)
									sEquation += intStoch + "v_{"
											+ productsReaction.get(k) + "}";
								else
									sEquation += doubleStoch + "v_{"
											+ productsReaction.get(k) + "}";
							}
						}

					} else {

						if (stoch != null) {
							;
							sEquation += (stoch.getType() == AST_PLUS || stoch
									.getType() == AST_MINUS) ? sEquation += "+\\left("
									+ toLaTeX(model, stoch)
									+ "\\right)v_{"
									+ productsReaction.get(k) + "}"
									: "+" + toLaTeX(model, stoch) + "v_{"
											+ productsReaction.get(k) + "}";
						} else {
							double doubleStoch = productsStochiometric.get(k)
									.getStoichiometry();
							if (doubleStoch == 1.0)
								sEquation += "+v_{" + productsReaction.get(k)
										+ "}";
							else {
								int intStoch = (int) doubleStoch;
								if ((doubleStoch - intStoch) == 0.0)
									sEquation += "+" + intStoch + "v_{"
											+ productsReaction.get(k) + "}";
								else
									sEquation += "+" + doubleStoch + "v_{"
											+ productsReaction.get(k) + "}";
							}
						}
					}
				}
			}

			if (sEquation.equals("")) {
				sp[speciesIndex] = (!species.getName().equals("") && !species
						.getName().equals(species.getId())) ? "\\subsection{Species: \\texttt{"
						+ replaceAll("_", species.getId(), "\\_")
						+ "}"
						+ " ("
						+ replaceAll("_", species.getName(), "\\_")
						+ ")}"
						+ begin
						+ "\\frac{\\mathrm {d["
						+ idToTeX(species)
						+ "]}}{\\mathrm dt}= 0"
						: "\\subsection{Species: \\texttt{"
								+ replaceAll("_", species.getId(), "\\_")
								+ "}}" + begin + "\\frac{\\mathrm{d["
								+ idToTeX(species) + "]}}{\\mathrm dt}= 0";
			} else if (!species.getBoundaryCondition()
					&& !species.getConstant()) {
				sp[speciesIndex] = (!species.getName().equals("") && !species
						.getName().equals(species.getId())) ? "\\subsection{Species: \\texttt{"
						+ replaceAll("_", species.getId(), "\\_")
						+ "}"
						+ " ("
						+ replaceAll("_", species.getName(), "\\_")
						+ ")}"
						+ begin
						+ "\\frac{\\mathrm{d["
						+ idToTeX(species)
						+ "]}}{\\mathrm dt}= " + sEquation
						: "\\subsection{Species: \\texttt{"
								+ replaceAll("_", species.getId(), "\\_")
								+ "}}" + begin + "\\frac{\\mathrm{d["
								+ idToTeX(species) + "]}}{\\mathrm {dt}}= "
								+ sEquation;
			} else {
				sp[speciesIndex] = (!species.getName().equals("") && !species
						.getName().equals(species.getId())) ? "\\subsection{Species: \\texttt{"
						+ replaceAll("_", species.getId(), "\\_")
						+ "}"
						+ " ("
						+ replaceAll("_", species.getName(), "\\_")
						+ ")}"
						+ begin
						+ "\\frac{\\mathrm {d["
						+ idToTeX(species)
						+ "]}}{\\mathrm {dt}}= 0"
						: "\\subsection{Species: \\texttt{"
								+ replaceAll("_", species.getId(), "\\_")
								+ "}}" + begin + "\\frac{\\mathrm {d["
								+ idToTeX(species) + "]}}{\\mathrm {dt}}= 0";
			}
		}
		String rulesHead = newLine + "\\section{Rules}" + newLine;
		String eventsHead = newLine + "\\section{Events}";
		String constraintsHead = newLine + "\\section{Constraints}";
		LinkedList events[] = new LinkedList[(int) model.getNumEvents()];
		int i;
		// writing latex
		latex = head;
		// writing Rate Laws
		latex += rateHead;
		for (i = 0; i < rateLaws.length; i++) {
			latex += rateLaws[i] + end;
		}
		// writing Equations
		latex += speciesHead;
		for (i = 0; i < sp.length; i++) {
			latex += sp[i] + end;
		}
		// writing Rules

		// writing Events
		if (model.getNumEvents() != 0) {
			PluginEvent ev;
			for (i = 0; i < model.getNumEvents(); i++) {
				ev = model.getEvent(i);
				LinkedList<String> assignments = new LinkedList<String>();
				assignments.add(toLaTeX(model, ev.getTrigger()));
				for (int j = 0; j < ev.getNumEventAssignments(); j++)
					assignments.add(toLaTeX(model, ev.getEventAssignment(j)
							.getMath()));
				events[i] = assignments;
			}
			latex += eventsHead;
			String var;
			for (i = 0; i < events.length; i++) {
				ev = model.getEvent(i);
				if (ev.getName().equals(null))
					latex += "\\subsection{Event:}";
				else
					latex += "\\subsection{Event: " + ev.getName() + "}";
				if (ev.getNumEventAssignments() > 1) {
					latex += "\\texttt{Triggers if: }" + newLine
							+ "\\begin{equation}" + events[i].get(0)
							+ "\\end{equation}" + newLine;
					if (ev.getDelay().equals(null))
						latex += newLine
								+ "\\texttt{and assigns the following rule: }"
								+ newLine;
					else {
						latex += newLine
								+ "\\texttt{and assigns after a delay of "
								+ toLaTeX(model, ev.getDelay());
						if (!ev.getTimeUnits().equals(null))
							latex += ev.getTimeUnits()
									+ " the following rules: }" + newLine;
						else
							latex += " s the following rules: }" + newLine;
					}
				} else {
					latex += "\\texttt{Triggers if: }" + newLine
							+ "\\begin{equation}" + events[i].get(0)
							+ "\\end{equation}" + newLine;
					if (ev.getDelay().equals(null))
						latex += newLine
								+ "\\texttt{and assigns the following rule: }"
								+ newLine;
					else {
						latex += newLine
								+ "\\texttt{and assigns after a delay of "
								+ toLaTeX(model, ev.getDelay());
						if (!ev.getTimeUnits().equals(null))
							latex += ev.getTimeUnits()
									+ " the following rule: }" + newLine;
						else
							latex += " s the following rule: }" + newLine;
					}
				}
				if (events[i].size() > 1)
					for (int j = 0; j < events[i].size() - 1; j++) {
						var = ev.getEventAssignment(j).getVariable();
						if (model.getSpecies(var) != null)
							latex += begin + "["
									+ idToTeX(model.getSpecies(var)) + "]"
									+ " = " + events[i].get(j + 1) + end
									+ newLine;
						else if (model.getParameter(var) != null)
							latex += begin
									+ name_idToLaTex(model.getParameter(var)
											.getId()) + " = "
									+ events[i].get(j + 1) + end + newLine;
						else
							latex += begin + events[i].get(j + 1) + end
									+ newLine;
					}
				else
					for (int j = 0; j < events[i].size() - 1; j++) {
						var = ev.getEventAssignment(j).getVariable();
						if (model.getSpecies(var) != null)
							latex += begin + "["
									+ idToTeX(model.getSpecies(var)) + "]"
									+ " = " + events[i].get(j + 1) + end
									+ newLine;
						else if (model.getParameter(var) != null)
							latex += begin
									+ name_idToLaTex(model.getParameter(var)
											.getId()) + " = "
									+ events[i].get(j + 1) + end + newLine;
						else
							latex += begin + events[i].get(j + 1) + end
									+ newLine;
					}
			}
		}

		// writing Constraints

		// writing parameters
		if (model.getNumParameters() > 0) {
			latex += newLine + "\\section{Parameters}";
			latex += "\\begin{longtable}{@{}llr@{}}" + newLine + "\\toprule "
					+ newLine + "Parameter & Value \\\\  " + newLine
					+ "\\midrule" + newLine;
			for (i = 0; i < model.getNumParameters(); i++) {
				latex += name_idToLaTex(model.getParameter(i).getId()) + "&"
						+ model.getParameter(i).getValue() + "\\\\" + newLine;
			}
			latex += "\\bottomrule " + newLine + "\\end{longtable}";
		}
		// writing species list and compartment.
		if (model.getNumSpecies() > 0) {
			latex += newLine + "\\section{Species}" + newLine;
			latex += "\\begin{longtable}{@{}llr@{}} " + newLine + "\\toprule "
					+ newLine
					+ "Species & Initial concentration & compartment \\\\  "
					+ newLine + "\\midrule" + newLine;
			for (i = 0; i < model.getNumSpecies(); i++) {
				latex += name_idToLaTex(model.getSpecies(i).getId()) + "&"
						+ model.getSpecies(i).getInitialConcentration() + "&"
						+ model.getSpecies(i).getCompartment() + "\\\\"
						+ newLine;
			}
			latex += "\\bottomrule " + newLine + "\\end{longtable}";
		}
		if (model.getNumCompartments() > 0) {
			latex += newLine + "\\section{Compartments}";
			latex += "\\begin{longtable}{@{}llr@{}}" + newLine + "\\toprule "
					+ newLine + "Compartment & Volume \\\\  " + newLine
					+ "\\midrule" + newLine;
			for (i = 0; i < model.getNumCompartments(); i++) {
				latex += name_idToLaTex(model.getCompartment(i).getId()) + "&"
						+ model.getCompartment(i).getVolume() + "\\\\"
						+ newLine;
			}
			latex += "\\bottomrule " + newLine + "\\end{longtable}";
		}
		latex += newLine + tail;
		return latex;

	}
}
