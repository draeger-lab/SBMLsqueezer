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
package org.sbml.squeezer.io;

import org.sbml.jlibsbml.ASTNode;
import org.sbml.jlibsbml.Model;
import org.sbml.jlibsbml.SBMLWriter;
import org.sbml.jlibsbml.SpeciesReference;
import org.sbml.libsbml.libsbmlConstants;

/**
 * @author Andreas Dr&auml;ger <a
 *         href="mailto:andreas.draeger@uni-tuebingen.de">
 *         andreas.draeger@uni-tuebingen.de</a>
 * 
 */
public abstract class AbstractSBMLWriter implements SBMLWriter {

	public AbstractSBMLWriter() {
	}

	/**
	 * Save the changes in the model.
	 * 
	 * @param model
	 * @param object
	 */
	public abstract void saveChanges(Model model, Object object);

	/**
	 * 
	 * @param sr
	 */
	public abstract void saveSpeciesReferenceProperties(SpeciesReference sr,
			Object specRef);

	/**
	 * 
	 * @param ast
	 * @return
	 */
	public org.sbml.libsbml.ASTNode convert(ASTNode a) {
		org.sbml.libsbml.ASTNode ast;
		switch (a.getType()) {
		case REAL:
			ast = new org.sbml.libsbml.ASTNode(libsbmlConstants.AST_REAL);
			ast.setValue(a.getReal());
			break;
		case INTEGER:
			ast = new org.sbml.libsbml.ASTNode(libsbmlConstants.AST_INTEGER);
			ast.setValue(a.getInteger());
			break;
		case FUNCTION_LOG:
			ast = new org.sbml.libsbml.ASTNode(
					libsbmlConstants.AST_FUNCTION_LOG);
			break;
		case POWER:
			ast = new org.sbml.libsbml.ASTNode(libsbmlConstants.AST_POWER);
			break;
		case PLUS:
			ast = new org.sbml.libsbml.ASTNode(libsbmlConstants.AST_PLUS);
			break;
		case MINUS:
			ast = new org.sbml.libsbml.ASTNode(libsbmlConstants.AST_MINUS);
			break;
		case TIMES:
			ast = new org.sbml.libsbml.ASTNode(libsbmlConstants.AST_TIMES);
			break;
		case DIVIDE:
			ast = new org.sbml.libsbml.ASTNode(libsbmlConstants.AST_DIVIDE);
			break;
		case RATIONAL:
			ast = new org.sbml.libsbml.ASTNode(libsbmlConstants.AST_RATIONAL);
			break;
		case NAME_TIME:
			ast = new org.sbml.libsbml.ASTNode(libsbmlConstants.AST_NAME_TIME);
			break;
		case FUNCTION_DELAY:
			ast = new org.sbml.libsbml.ASTNode(
					libsbmlConstants.AST_FUNCTION_DELAY);
			break;
		case NAME:
			ast = new org.sbml.libsbml.ASTNode(libsbmlConstants.AST_NAME);
			ast.setName(a.getName());
			break;
		case CONSTANT_PI:
			ast = new org.sbml.libsbml.ASTNode(libsbmlConstants.AST_CONSTANT_PI);
			break;
		case CONSTANT_E:
			ast = new org.sbml.libsbml.ASTNode(libsbmlConstants.AST_CONSTANT_E);
			break;
		case CONSTANT_TRUE:
			ast = new org.sbml.libsbml.ASTNode(
					libsbmlConstants.AST_CONSTANT_TRUE);
			break;
		case CONSTANT_FALSE:
			ast = new org.sbml.libsbml.ASTNode(
					libsbmlConstants.AST_CONSTANT_FALSE);
			break;
		case REAL_E:
			ast = new org.sbml.libsbml.ASTNode(libsbmlConstants.AST_REAL_E);
			ast.setValue(a.getMantissa(), a.getExponent());
			break;
		case FUNCTION_ABS:
			ast = new org.sbml.libsbml.ASTNode(
					libsbmlConstants.AST_FUNCTION_ABS);
			break;
		case FUNCTION_ARCCOS:
			ast = new org.sbml.libsbml.ASTNode(
					libsbmlConstants.AST_FUNCTION_ARCCOS);
			break;
		case FUNCTION_ARCCOSH:
			ast = new org.sbml.libsbml.ASTNode(
					libsbmlConstants.AST_FUNCTION_ARCCOSH);
			break;
		case FUNCTION_ARCCOT:
			ast = new org.sbml.libsbml.ASTNode(
					libsbmlConstants.AST_FUNCTION_ARCCOT);
			break;
		case FUNCTION_ARCCOTH:
			ast = new org.sbml.libsbml.ASTNode(
					libsbmlConstants.AST_FUNCTION_ARCCOTH);
			break;
		case FUNCTION_ARCCSC:
			ast = new org.sbml.libsbml.ASTNode(
					libsbmlConstants.AST_FUNCTION_ARCCSC);
			break;
		case FUNCTION_ARCCSCH:
			ast = new org.sbml.libsbml.ASTNode(
					libsbmlConstants.AST_FUNCTION_ARCCSCH);
			break;
		case FUNCTION_ARCSEC:
			ast = new org.sbml.libsbml.ASTNode(
					libsbmlConstants.AST_FUNCTION_ARCSEC);
			break;
		case FUNCTION_ARCSECH:
			ast = new org.sbml.libsbml.ASTNode(
					libsbmlConstants.AST_FUNCTION_ARCSECH);
			break;
		case FUNCTION_ARCSIN:
			ast = new org.sbml.libsbml.ASTNode(
					libsbmlConstants.AST_FUNCTION_ARCSIN);
			break;
		case FUNCTION_ARCSINH:
			ast = new org.sbml.libsbml.ASTNode(
					libsbmlConstants.AST_FUNCTION_ARCSINH);
			break;
		case FUNCTION_ARCTAN:
			ast = new org.sbml.libsbml.ASTNode(
					libsbmlConstants.AST_FUNCTION_ARCTAN);
			break;
		case FUNCTION_ARCTANH:
			ast = new org.sbml.libsbml.ASTNode(
					libsbmlConstants.AST_FUNCTION_ARCTANH);
			break;
		case FUNCTION_CEILING:
			ast = new org.sbml.libsbml.ASTNode(
					libsbmlConstants.AST_FUNCTION_CEILING);
			break;
		case FUNCTION_COS:
			ast = new org.sbml.libsbml.ASTNode(
					libsbmlConstants.AST_FUNCTION_COS);
			break;
		case FUNCTION_COSH:
			ast = new org.sbml.libsbml.ASTNode(
					libsbmlConstants.AST_FUNCTION_COSH);
			break;
		case FUNCTION_COT:
			ast = new org.sbml.libsbml.ASTNode(
					libsbmlConstants.AST_FUNCTION_COT);
			break;
		case FUNCTION_COTH:
			ast = new org.sbml.libsbml.ASTNode(
					libsbmlConstants.AST_FUNCTION_COTH);
			break;
		case FUNCTION_CSC:
			ast = new org.sbml.libsbml.ASTNode(
					libsbmlConstants.AST_FUNCTION_CSC);
			break;
		case FUNCTION_CSCH:
			ast = new org.sbml.libsbml.ASTNode(
					libsbmlConstants.AST_FUNCTION_CSCH);
			break;
		case FUNCTION_EXP:
			ast = new org.sbml.libsbml.ASTNode(
					libsbmlConstants.AST_FUNCTION_EXP);
			break;
		case FUNCTION_FACTORIAL:
			ast = new org.sbml.libsbml.ASTNode(
					libsbmlConstants.AST_FUNCTION_FACTORIAL);
			break;
		case FUNCTION_FLOOR:
			ast = new org.sbml.libsbml.ASTNode(
					libsbmlConstants.AST_FUNCTION_FLOOR);
			break;
		case FUNCTION_LN:
			ast = new org.sbml.libsbml.ASTNode(libsbmlConstants.AST_FUNCTION_LN);
			break;
		case FUNCTION_POWER:
			ast = new org.sbml.libsbml.ASTNode(
					libsbmlConstants.AST_FUNCTION_POWER);
			break;
		case FUNCTION_ROOT:
			ast = new org.sbml.libsbml.ASTNode(
					libsbmlConstants.AST_FUNCTION_ROOT);
			break;
		case FUNCTION_SEC:
			ast = new org.sbml.libsbml.ASTNode(
					libsbmlConstants.AST_FUNCTION_SEC);
			break;
		case FUNCTION_SECH:
			ast = new org.sbml.libsbml.ASTNode(
					libsbmlConstants.AST_FUNCTION_SECH);
			break;
		case FUNCTION_SIN:
			ast = new org.sbml.libsbml.ASTNode(
					libsbmlConstants.AST_FUNCTION_SIN);
			break;
		case FUNCTION_SINH:
			ast = new org.sbml.libsbml.ASTNode(
					libsbmlConstants.AST_FUNCTION_SINH);
			break;
		case FUNCTION_TAN:
			ast = new org.sbml.libsbml.ASTNode(
					libsbmlConstants.AST_FUNCTION_TAN);
			break;
		case FUNCTION_TANH:
			ast = new org.sbml.libsbml.ASTNode(
					libsbmlConstants.AST_FUNCTION_TANH);
			break;
		case FUNCTION:
			ast = new org.sbml.libsbml.ASTNode(libsbmlConstants.AST_FUNCTION);
			ast.setName(a.getName());
			break;
		case LAMBDA:
			ast = new org.sbml.libsbml.ASTNode(libsbmlConstants.AST_LAMBDA);
			break;
		case LOGICAL_AND:
			ast = new org.sbml.libsbml.ASTNode(libsbmlConstants.AST_LOGICAL_AND);
			break;
		case LOGICAL_XOR:
			ast = new org.sbml.libsbml.ASTNode(libsbmlConstants.AST_LOGICAL_XOR);
			break;
		case LOGICAL_OR:
			ast = new org.sbml.libsbml.ASTNode(libsbmlConstants.AST_LOGICAL_OR);
			break;
		case LOGICAL_NOT:
			ast = new org.sbml.libsbml.ASTNode(libsbmlConstants.AST_LOGICAL_NOT);
			break;
		case FUNCTION_PIECEWISE:
			ast = new org.sbml.libsbml.ASTNode(
					libsbmlConstants.AST_FUNCTION_PIECEWISE);
			break;
		case RELATIONAL_EQ:
			ast = new org.sbml.libsbml.ASTNode(
					libsbmlConstants.AST_RELATIONAL_EQ);
			break;
		case RELATIONAL_GEQ:
			ast = new org.sbml.libsbml.ASTNode(
					libsbmlConstants.AST_RELATIONAL_GEQ);
			break;
		case RELATIONAL_GT:
			ast = new org.sbml.libsbml.ASTNode(
					libsbmlConstants.AST_RELATIONAL_GT);
			break;
		case RELATIONAL_NEQ:
			ast = new org.sbml.libsbml.ASTNode(
					libsbmlConstants.AST_RELATIONAL_NEQ);
			break;
		case RELATIONAL_LEQ:
			ast = new org.sbml.libsbml.ASTNode(
					libsbmlConstants.AST_RELATIONAL_LEQ);
			break;
		case RELATIONAL_LT:
			ast = new org.sbml.libsbml.ASTNode(
					libsbmlConstants.AST_RELATIONAL_LT);
			break;
		default:
			ast = new org.sbml.libsbml.ASTNode(libsbmlConstants.AST_UNKNOWN);
			break;
		}
		for (int i = 0; i < a.getNumChildren(); i++)
			ast.addChild(convert(a.getChild(i)));
		return ast;
	}

	/**
	 * Determins whether the two ASTNode objects are equal.
	 * 
	 * @param math
	 * @param libMath
	 * @return
	 */
	public boolean equal(ASTNode math, org.sbml.libsbml.ASTNode libMath) {
		if (math == null || libMath == null)
			return false;
		boolean equal = true;
		switch (math.getType()) {
		case REAL:
			equal &= libMath.getType() == libsbmlConstants.AST_REAL;
			equal &= libMath.getReal() == math.getReal();
			break;
		case INTEGER:
			equal &= libMath.getType() == libsbmlConstants.AST_INTEGER;
			equal &= libMath.getInteger() == math.getInteger();
			break;
		case FUNCTION_LOG:
			equal &= libMath.getType() == libsbmlConstants.AST_FUNCTION_LOG;
			break;
		case POWER:
			equal &= libMath.getType() == libsbmlConstants.AST_POWER;
			break;
		case PLUS:
			equal &= libMath.getType() == libsbmlConstants.AST_PLUS;
			break;
		case MINUS:
			equal &= libMath.getType() == libsbmlConstants.AST_MINUS;
			break;
		case TIMES:
			equal &= libMath.getType() == libsbmlConstants.AST_TIMES;
			break;
		case DIVIDE:
			equal &= libMath.getType() == libsbmlConstants.AST_DIVIDE;
			break;
		case RATIONAL:
			equal &= libMath.getType() == libsbmlConstants.AST_RATIONAL;
			break;
		case NAME_TIME:
			equal &= libMath.getType() == libsbmlConstants.AST_NAME_TIME;
			break;
		case FUNCTION_DELAY:
			equal &= libMath.getType() == libsbmlConstants.AST_FUNCTION_DELAY;
			break;
		case NAME:
			equal &= libMath.getType() == libsbmlConstants.AST_NAME;
			equal &= libMath.getName().equals(math.getName());
			break;
		case CONSTANT_PI:
			equal &= libMath.getType() == libsbmlConstants.AST_CONSTANT_PI;
			break;
		case CONSTANT_E:
			equal &= libMath.getType() == libsbmlConstants.AST_CONSTANT_E;
			break;
		case CONSTANT_TRUE:
			equal &= libMath.getType() == libsbmlConstants.AST_CONSTANT_TRUE;
			break;
		case CONSTANT_FALSE:
			equal &= libMath.getType() == libsbmlConstants.AST_CONSTANT_FALSE;
			break;
		case REAL_E:
			equal &= libMath.getType() == libsbmlConstants.AST_REAL_E;
			equal &= libMath.getMantissa() == math.getMantissa();
			equal &= libMath.getExponent() == math.getExponent();
			break;
		case FUNCTION_ABS:
			equal &= libMath.getType() == libsbmlConstants.AST_FUNCTION_ABS;
			break;
		case FUNCTION_ARCCOS:
			equal &= libMath.getType() == libsbmlConstants.AST_FUNCTION_ARCCOS;
			break;
		case FUNCTION_ARCCOSH:
			equal &= libMath.getType() == libsbmlConstants.AST_FUNCTION_ARCCOSH;
			break;
		case FUNCTION_ARCCOT:
			equal &= libMath.getType() == libsbmlConstants.AST_FUNCTION_ARCCOT;
			break;
		case FUNCTION_ARCCOTH:
			equal &= libMath.getType() == libsbmlConstants.AST_FUNCTION_ARCCOTH;
			break;
		case FUNCTION_ARCCSC:
			equal &= libMath.getType() == libsbmlConstants.AST_FUNCTION_ARCCSC;
			break;
		case FUNCTION_ARCCSCH:
			equal &= libMath.getType() == libsbmlConstants.AST_FUNCTION_ARCCSCH;
			break;
		case FUNCTION_ARCSEC:
			equal &= libMath.getType() == libsbmlConstants.AST_FUNCTION_ARCSEC;
			break;
		case FUNCTION_ARCSECH:
			equal &= libMath.getType() == libsbmlConstants.AST_FUNCTION_ARCSECH;
			break;
		case FUNCTION_ARCSIN:
			equal &= libMath.getType() == libsbmlConstants.AST_FUNCTION_ARCSIN;
			break;
		case FUNCTION_ARCSINH:
			equal &= libMath.getType() == libsbmlConstants.AST_FUNCTION_ARCSINH;
			break;
		case FUNCTION_ARCTAN:
			equal &= libMath.getType() == libsbmlConstants.AST_FUNCTION_ARCTAN;
			break;
		case FUNCTION_ARCTANH:
			equal &= libMath.getType() == libsbmlConstants.AST_FUNCTION_ARCTANH;
			break;
		case FUNCTION_CEILING:
			equal &= libMath.getType() == libsbmlConstants.AST_FUNCTION_CEILING;
			break;
		case FUNCTION_COS:
			equal &= libMath.getType() == libsbmlConstants.AST_FUNCTION_COS;
			break;
		case FUNCTION_COSH:
			equal &= libMath.getType() == libsbmlConstants.AST_FUNCTION_COSH;
			break;
		case FUNCTION_COT:
			equal &= libMath.getType() == libsbmlConstants.AST_FUNCTION_COT;
			break;
		case FUNCTION_COTH:
			equal &= libMath.getType() == libsbmlConstants.AST_FUNCTION_COTH;
			break;
		case FUNCTION_CSC:
			equal &= libMath.getType() == libsbmlConstants.AST_FUNCTION_CSC;
			break;
		case FUNCTION_CSCH:
			equal &= libMath.getType() == libsbmlConstants.AST_FUNCTION_CSCH;
			break;
		case FUNCTION_EXP:
			equal &= libMath.getType() == libsbmlConstants.AST_FUNCTION_EXP;
			break;
		case FUNCTION_FACTORIAL:
			equal &= libMath.getType() == libsbmlConstants.AST_FUNCTION_FACTORIAL;
			break;
		case FUNCTION_FLOOR:
			equal &= libMath.getType() == libsbmlConstants.AST_FUNCTION_FLOOR;
			break;
		case FUNCTION_LN:
			equal &= libMath.getType() == libsbmlConstants.AST_FUNCTION_LN;
			break;
		case FUNCTION_POWER:
			equal &= libMath.getType() == libsbmlConstants.AST_FUNCTION_POWER;
			break;
		case FUNCTION_ROOT:
			equal &= libMath.getType() == libsbmlConstants.AST_FUNCTION_ROOT;
			break;
		case FUNCTION_SEC:
			equal &= libMath.getType() == libsbmlConstants.AST_FUNCTION_SEC;
			break;
		case FUNCTION_SECH:
			equal &= libMath.getType() == libsbmlConstants.AST_FUNCTION_SECH;
			break;
		case FUNCTION_SIN:
			equal &= libMath.getType() == libsbmlConstants.AST_FUNCTION_SIN;
			break;
		case FUNCTION_SINH:
			equal &= libMath.getType() == libsbmlConstants.AST_FUNCTION_SINH;
			break;
		case FUNCTION_TAN:
			equal &= libMath.getType() == libsbmlConstants.AST_FUNCTION_TAN;
			break;
		case FUNCTION_TANH:
			equal &= libMath.getType() == libsbmlConstants.AST_FUNCTION_TANH;
			break;
		case FUNCTION:
			equal &= libMath.getType() == libsbmlConstants.AST_FUNCTION;
			equal &= libMath.getName().equals(math.getName());
			break;
		case LAMBDA:
			equal &= libMath.getType() == libsbmlConstants.AST_LAMBDA;
			break;
		case LOGICAL_AND:
			equal &= libMath.getType() == libsbmlConstants.AST_LOGICAL_AND;
			break;
		case LOGICAL_XOR:
			equal &= libMath.getType() == libsbmlConstants.AST_LOGICAL_XOR;
			break;
		case LOGICAL_OR:
			equal &= libMath.getType() == libsbmlConstants.AST_LOGICAL_OR;
			break;
		case LOGICAL_NOT:
			equal &= libMath.getType() == libsbmlConstants.AST_LOGICAL_NOT;
			break;
		case FUNCTION_PIECEWISE:
			equal &= libMath.getType() == libsbmlConstants.AST_FUNCTION_PIECEWISE;
			break;
		case RELATIONAL_EQ:
			equal &= libMath.getType() == libsbmlConstants.AST_RELATIONAL_EQ;
			break;
		case RELATIONAL_GEQ:
			equal &= libMath.getType() == libsbmlConstants.AST_RELATIONAL_GEQ;
			break;
		case RELATIONAL_GT:
			equal &= libMath.getType() == libsbmlConstants.AST_RELATIONAL_GT;
			break;
		case RELATIONAL_NEQ:
			equal &= libMath.getType() == libsbmlConstants.AST_RELATIONAL_NEQ;
			break;
		case RELATIONAL_LEQ:
			equal &= libMath.getType() == libsbmlConstants.AST_RELATIONAL_LEQ;
			break;
		case RELATIONAL_LT:
			equal &= libMath.getType() == libsbmlConstants.AST_RELATIONAL_LT;
			break;
		default:
			equal &= libMath.getType() == libsbmlConstants.AST_UNKNOWN;
			break;
		}
		equal &= math.getNumChildren() == libMath.getNumChildren();
		if (equal)
			for (int i = 0; i < math.getNumChildren(); i++)
				equal &= equal(math.getChild(i), libMath.getChild(i));
		return equal;
	}
}
