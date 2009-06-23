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
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.sbmlsqueezer.io;

import jp.sbi.celldesigner.plugin.PluginModel;

import org.sbml.libsbml.ASTNode;
import org.sbml.libsbml.libsbmlConstants;

/**
 * @author <a href="mailto:andreas.draeger@uni-tuebingen.de">Andreas
 *         Dr&auml;ger</a>
 * 
 */
public class TextExport implements libsbmlConstants {

	public static StringBuffer toText(PluginModel model, ASTNode astnode) {
		String value;
		//
		// if (astnode.isUMinus()) {
		// return (astnode.getLeftChild().getLeftChild() != null) ? "- ("
		// + toText(model, astnode.getLeftChild()) + ")" : "-"
		// + toText(model, astnode.getLeftChild());
		// } else if (astnode.isSqrt()) {
		// return "sqrt(" + toText(model, astnode.getLeftChild()) + ")";
		// } else if (astnode.isNumber()) {
		// if (astnode.getReal() == Double.POSITIVE_INFINITY)
		// return Double.toString(Double.POSITIVE_INFINITY);
		// if (astnode.getReal() == Double.NEGATIVE_INFINITY)
		// return Double.toString(Double.NEGATIVE_INFINITY);
		// } else if (astnode.getType() == AST_FUNCTION_LOG) {
		// return (astnode.getRightChild().getLeftChild() != null) ? "log("
		// + toText(model, astnode.getRightChild()) + ","
		// + toText(model, astnode.getLeftChild()) + ")" : "log("
		// + toText(model, astnode.getRightChild()) + ","
		// + toText(model, astnode.getLeftChild()) + ")";
		// }
		// /*
		// * Numbers
		// */
		// switch (astnode.getType()) {
		// case AST_REAL:
		// double d = astnode.getReal();
		// return (((int) d) - d == 0) ? Integer.toString((int) d) : Double
		// .toString(d);
		// case AST_INTEGER:
		// return Integer.toString(astnode.getInteger());
		// /*
		// * Basic Functions
		// */
		// case AST_FUNCTION_LOG:
		// return (astnode.getRightChild().getLeftChild() != null) ? "log("
		// + toText(model, astnode.getRightChild()) + ")" : "log("
		// + toText(model, astnode.getRightChild()) + ")";
		// /*
		// * Operators
		// */
		// case AST_POWER:
		// if (toText(model, astnode.getRightChild()).equals("1"))
		// return "(" + toText(model, astnode.getLeftChild()) + ")";
		// else
		// return "(" + toText(model, astnode.getLeftChild()) + ")^("
		// + toText(model, astnode.getRightChild()) + ")";
		// case AST_PLUS:
		// if (astnode.getNumChildren() > 0) {
		// value = toText(model, astnode.getLeftChild());
		// ASTNode ast;
		// for (int i = 1; i < astnode.getNumChildren(); i++) {
		// ast = astnode.getChild(i);
		// switch (ast.getType()) {
		// case AST_MINUS:
		// value += " + (" + toText(model, ast) + ")";
		// break;
		// default:
		// value += " + " + toText(model, ast);
		// break;
		// }
		//
		// }
		// return value;
		// }
		// case AST_MINUS:
		// if (astnode.getNumChildren() > 0) {
		// value = toText(model, astnode.getLeftChild());
		// ASTNode ast;
		// for (int i = 1; i < astnode.getNumChildren(); i++) {
		// ast = astnode.getChild(i);
		// switch (ast.getType()) {
		// case AST_PLUS:
		// value += " - (" + toText(model, ast) + ")";
		// break;
		// default:
		// value += " - " + toText(model, ast);
		// break;
		// }
		//
		// }
		// return value;
		// }
		// case AST_TIMES:
		// if (astnode.getNumChildren() > 0) {
		// value = toText(model, astnode.getLeftChild());
		// if (astnode.getLeftChild().getNumChildren() > 1
		// && (astnode.getLeftChild().getType() == AST_MINUS || astnode
		// .getLeftChild().getType() == AST_PLUS))
		// value = "(" + value + ")";
		// ASTNode ast;
		// for (int i = 1; i < astnode.getNumChildren(); i++) {
		// ast = astnode.getChild(i);
		// switch (ast.getType()) {
		// case AST_MINUS:
		// value += " * (" + toText(model, ast) + ")";
		// break;
		// case AST_PLUS:
		// value += " * (" + toText(model, ast) + ")";
		// break;
		// default:
		// value += " * " + toText(model, ast);
		// break;
		// }
		// }
		// return value;
		// }
		//
		// case AST_DIVIDE:
		// String num = toText(model, astnode.getLeftChild());
		// String denum = toText(model, astnode.getRightChild());
		//
		// return "(" + num + ")/(" + denum + ")";
		//
		// case AST_RATIONAL:
		// if (Double.toString(astnode.getDenominator()).toString()
		// .equals("1"))
		// return Double.toString(astnode.getNumerator());
		// else
		// return "(" + Double.toString(astnode.getNumerator()) + ")/("
		// + Double.toString(astnode.getDenominator()) + ")";
		//
		// /*
		// * Names of identifiers: parameters, functions, species etc.
		// */
		// case AST_NAME:
		// return astnode.getName();
		// /*
		// * Constants: pi, e, true, false
		// */
		//
		// case AST_CONSTANT_PI:
		// return "Pi";
		// case AST_CONSTANT_E:
		// return "ExponentialE";
		// case AST_CONSTANT_TRUE:
		// return "True";
		// case AST_CONSTANT_FALSE:
		// return "False";
		// case AST_REAL_E:
		// return Double.toString(astnode.getReal());
		// /*
		// * More complicated functions
		// */
		// case AST_FUNCTION_ABS:
		// return "abs(" + toText(model, astnode.getRightChild()) + ")";
		// case AST_FUNCTION_ARCCOS:
		// return "arccos(" + toText(model, astnode.getLeftChild()) + ")";
		// case AST_FUNCTION_ARCCOSH:
		// return "arccosh(" + toText(model, astnode.getLeftChild()) + ")";
		//
		// case AST_FUNCTION_ARCCOT:
		// return "arcot(" + toText(model, astnode.getLeftChild()) + ")";
		//
		// case AST_FUNCTION_ARCCOTH:
		// return "arccoth(" + toText(model, astnode.getLeftChild()) + ")";
		//
		// case AST_FUNCTION_ARCCSC:
		// return "arccsc(" + toText(model, astnode.getLeftChild()) + ")";
		//
		// case AST_FUNCTION_ARCCSCH:
		// return "arccsh(" + toText(model, astnode.getLeftChild()) + ")";
		//
		// case AST_FUNCTION_ARCSEC:
		// return "arcsec(" + toText(model, astnode.getLeftChild()) + ")";
		//
		// case AST_FUNCTION_ARCSECH:
		// return "arcsech(" + toText(model, astnode.getLeftChild()) + ")";
		//
		// case AST_FUNCTION_ARCSIN:
		// return "arcsin(" + toText(model, astnode.getLeftChild()) + ")";
		//
		// case AST_FUNCTION_ARCSINH:
		// return "arcsinh(" + toText(model, astnode.getLeftChild()) + ")";
		//
		// case AST_FUNCTION_ARCTAN:
		// return "arctan(" + toText(model, astnode.getLeftChild()) + ")";
		//
		// case AST_FUNCTION_ARCTANH:
		// return "arctanh(" + toText(model, astnode.getLeftChild()) + ")";
		//
		// case AST_FUNCTION_CEILING:
		// return "ceiling(" + toText(model, astnode.getLeftChild()) + ")";
		// case AST_FUNCTION_COS:
		// return "cos(" + toText(model, astnode.getLeftChild()) + ")";
		//
		// case AST_FUNCTION_COSH:
		// return "cosh(" + toText(model, astnode.getLeftChild()) + ")";
		//
		// case AST_FUNCTION_COT:
		// return "cot(" + toText(model, astnode.getLeftChild()) + ")";
		//
		// case AST_FUNCTION_COTH:
		// return "coth(" + toText(model, astnode.getLeftChild()) + ")";
		//
		// case AST_FUNCTION_CSC:
		// return "csc(" + toText(model, astnode.getLeftChild()) + ")";
		//
		// case AST_FUNCTION_CSCH:
		// return "csch(" + toText(model, astnode.getLeftChild()) + ")";
		//
		// case AST_FUNCTION_EXP:
		// return "exp(" + toText(model, astnode.getLeftChild()) + ")";
		//
		// case AST_FUNCTION_FACTORIAL:
		// return "(" + toText(model, astnode.getLeftChild()) + ")!";
		//
		// case AST_FUNCTION_FLOOR:
		// return "floor(" + toText(model, astnode.getLeftChild()) + ")";
		//
		// case AST_FUNCTION_LN:
		// return "ln(" + toText(model, astnode.getLeftChild()) + ")";
		//
		// case AST_FUNCTION_POWER:
		// return "power(" + toText(model, astnode.getLeftChild()) + ","
		// + toText(model, astnode.getRightChild()) + ")";
		//
		// case AST_FUNCTION_ROOT:
		// return (astnode.getRightChild() != null) ? "sqrt("
		// + toText(model, astnode.getLeftChild()) + ","
		// + toText(model, astnode.getRightChild()) + ")" : "sqrt("
		// + toText(model, astnode.getLeftChild()) + ")";
		//
		// case AST_FUNCTION_SEC:
		// return "sec(" + toText(model, astnode.getLeftChild()) + ")";
		//
		// case AST_FUNCTION_SECH:
		// return "sech(" + toText(model, astnode.getLeftChild()) + ")";
		//
		// case AST_FUNCTION_SIN:
		// return "sin(" + toText(model, astnode.getLeftChild()) + ")";
		//
		// case AST_FUNCTION_SINH:
		// return "sinh(" + toText(model, astnode.getLeftChild()) + ")";
		//
		// case AST_FUNCTION_TAN:
		// return "tan(" + toText(model, astnode.getLeftChild()) + ")";
		//
		// case AST_FUNCTION_TANH:
		// return "tanh(" + toText(model, astnode.getLeftChild()) + ")";
		// /*
		// * TODO Lambda
		// */
		//
		// case AST_LAMBDA:
		// break;
		//
		// case AST_LOGICAL_AND:
		// return "(" + toText(model, astnode.getLeftChild()) + ") && ("
		// + toText(model, astnode.getRightChild()) + ")";
		//
		// case AST_LOGICAL_XOR:
		// return "((" + toText(model, astnode.getLeftChild()) + ") && !("
		// + toText(model, astnode.getRightChild()) + ")) || (!("
		// + toText(model, astnode.getLeftChild()) + ") && ("
		// + toText(model, astnode.getRightChild()) + "))";
		//
		// case AST_LOGICAL_OR:
		// return "(" + toText(model, astnode.getLeftChild()) + ") || ("
		// + toText(model, astnode.getRightChild()) + ")";
		//
		// case AST_LOGICAL_NOT:
		// return "!(" + toText(model, astnode.getLeftChild()) + ")";
		//
		// case AST_FUNCTION_PIECEWISE: // TODO
		// return "";
		//
		// case AST_RELATIONAL_EQ:
		// return "eq(" + toText(model, astnode.getLeftChild()) + ","
		// + toText(model, astnode.getRightChild()) + ")";
		// case AST_RELATIONAL_GEQ:
		// return "geq(" + toText(model, astnode.getLeftChild()) + ","
		// + toText(model, astnode.getRightChild()) + ")";
		// case AST_RELATIONAL_GT:
		// return "gt(" + toText(model, astnode.getLeftChild()) + ","
		// + toText(model, astnode.getRightChild()) + ")";
		// case AST_RELATIONAL_NEQ:
		// return "neq(" + toText(model, astnode.getLeftChild()) + ","
		// + toText(model, astnode.getRightChild()) + ")";
		// case AST_RELATIONAL_LEQ:
		// return "leq(" + toText(model, astnode.getLeftChild()) + ","
		// + toText(model, astnode.getRightChild()) + ")";
		// case AST_RELATIONAL_LT:
		// return "lt("+toText(model, astnode.getLeftChild()) + ","
		// + toText(model, astnode.getRightChild()) + ")";
		// case AST_UNKNOWN:
		// return "unknown";
		//
		// default:
		// break;
		// }
		return null;
	}
}
