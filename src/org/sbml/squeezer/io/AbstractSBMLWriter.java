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

import org.sbml.jsbml.ASTNode;
import org.sbml.jsbml.Model;
import org.sbml.jsbml.SBMLException;
import org.sbml.jsbml.SBMLWriter;
import org.sbml.jsbml.SpeciesReference;
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
	 * @throws SBMLException 
	 */
	public abstract void saveChanges(Model model, Object object) throws SBMLException;

	/**
	 * 
	 * @param sr
	 * @throws SBMLException 
	 */
	public abstract void saveSpeciesReferenceProperties(SpeciesReference sr,
			Object specRef) throws SBMLException;

	/**
	 * 
	 * @param ast
	 * @return
	 */
	public org.sbml.libsbml.ASTNode convert(ASTNode astnode) {
		org.sbml.libsbml.ASTNode libAstNode;
		switch (astnode.getType()) {
		case REAL:
			libAstNode = new org.sbml.libsbml.ASTNode(libsbmlConstants.AST_REAL);
			libAstNode.setValue(astnode.getReal());
			break;
		case INTEGER:
			libAstNode = new org.sbml.libsbml.ASTNode(libsbmlConstants.AST_INTEGER);
			libAstNode.setValue(astnode.getInteger());
			break;
		case FUNCTION_LOG:
			libAstNode = new org.sbml.libsbml.ASTNode(
					libsbmlConstants.AST_FUNCTION_LOG);
			break;
		case POWER:
			libAstNode = new org.sbml.libsbml.ASTNode(libsbmlConstants.AST_POWER);
			break;
		case PLUS:
			libAstNode = new org.sbml.libsbml.ASTNode(libsbmlConstants.AST_PLUS);
			break;
		case MINUS:
			libAstNode = new org.sbml.libsbml.ASTNode(libsbmlConstants.AST_MINUS);
			break;
		case TIMES:
			libAstNode = new org.sbml.libsbml.ASTNode(libsbmlConstants.AST_TIMES);
			break;
		case DIVIDE:
			libAstNode = new org.sbml.libsbml.ASTNode(libsbmlConstants.AST_DIVIDE);
			break;
		case RATIONAL:
			libAstNode = new org.sbml.libsbml.ASTNode(libsbmlConstants.AST_RATIONAL);
			break;
		case NAME_TIME:
			libAstNode = new org.sbml.libsbml.ASTNode(libsbmlConstants.AST_NAME_TIME);
			break;
		case FUNCTION_DELAY:
			libAstNode = new org.sbml.libsbml.ASTNode(
					libsbmlConstants.AST_FUNCTION_DELAY);
			break;
		case NAME:
			libAstNode = new org.sbml.libsbml.ASTNode(libsbmlConstants.AST_NAME);
			libAstNode.setName(astnode.getName());
			break;
		case CONSTANT_PI:
			libAstNode = new org.sbml.libsbml.ASTNode(libsbmlConstants.AST_CONSTANT_PI);
			break;
		case CONSTANT_E:
			libAstNode = new org.sbml.libsbml.ASTNode(libsbmlConstants.AST_CONSTANT_E);
			break;
		case CONSTANT_TRUE:
			libAstNode = new org.sbml.libsbml.ASTNode(
					libsbmlConstants.AST_CONSTANT_TRUE);
			break;
		case CONSTANT_FALSE:
			libAstNode = new org.sbml.libsbml.ASTNode(
					libsbmlConstants.AST_CONSTANT_FALSE);
			break;
		case REAL_E:
			libAstNode = new org.sbml.libsbml.ASTNode(libsbmlConstants.AST_REAL_E);
			libAstNode.setValue(astnode.getMantissa(), astnode.getExponent());
			break;
		case FUNCTION_ABS:
			libAstNode = new org.sbml.libsbml.ASTNode(
					libsbmlConstants.AST_FUNCTION_ABS);
			break;
		case FUNCTION_ARCCOS:
			libAstNode = new org.sbml.libsbml.ASTNode(
					libsbmlConstants.AST_FUNCTION_ARCCOS);
			break;
		case FUNCTION_ARCCOSH:
			libAstNode = new org.sbml.libsbml.ASTNode(
					libsbmlConstants.AST_FUNCTION_ARCCOSH);
			break;
		case FUNCTION_ARCCOT:
			libAstNode = new org.sbml.libsbml.ASTNode(
					libsbmlConstants.AST_FUNCTION_ARCCOT);
			break;
		case FUNCTION_ARCCOTH:
			libAstNode = new org.sbml.libsbml.ASTNode(
					libsbmlConstants.AST_FUNCTION_ARCCOTH);
			break;
		case FUNCTION_ARCCSC:
			libAstNode = new org.sbml.libsbml.ASTNode(
					libsbmlConstants.AST_FUNCTION_ARCCSC);
			break;
		case FUNCTION_ARCCSCH:
			libAstNode = new org.sbml.libsbml.ASTNode(
					libsbmlConstants.AST_FUNCTION_ARCCSCH);
			break;
		case FUNCTION_ARCSEC:
			libAstNode = new org.sbml.libsbml.ASTNode(
					libsbmlConstants.AST_FUNCTION_ARCSEC);
			break;
		case FUNCTION_ARCSECH:
			libAstNode = new org.sbml.libsbml.ASTNode(
					libsbmlConstants.AST_FUNCTION_ARCSECH);
			break;
		case FUNCTION_ARCSIN:
			libAstNode = new org.sbml.libsbml.ASTNode(
					libsbmlConstants.AST_FUNCTION_ARCSIN);
			break;
		case FUNCTION_ARCSINH:
			libAstNode = new org.sbml.libsbml.ASTNode(
					libsbmlConstants.AST_FUNCTION_ARCSINH);
			break;
		case FUNCTION_ARCTAN:
			libAstNode = new org.sbml.libsbml.ASTNode(
					libsbmlConstants.AST_FUNCTION_ARCTAN);
			break;
		case FUNCTION_ARCTANH:
			libAstNode = new org.sbml.libsbml.ASTNode(
					libsbmlConstants.AST_FUNCTION_ARCTANH);
			break;
		case FUNCTION_CEILING:
			libAstNode = new org.sbml.libsbml.ASTNode(
					libsbmlConstants.AST_FUNCTION_CEILING);
			break;
		case FUNCTION_COS:
			libAstNode = new org.sbml.libsbml.ASTNode(
					libsbmlConstants.AST_FUNCTION_COS);
			break;
		case FUNCTION_COSH:
			libAstNode = new org.sbml.libsbml.ASTNode(
					libsbmlConstants.AST_FUNCTION_COSH);
			break;
		case FUNCTION_COT:
			libAstNode = new org.sbml.libsbml.ASTNode(
					libsbmlConstants.AST_FUNCTION_COT);
			break;
		case FUNCTION_COTH:
			libAstNode = new org.sbml.libsbml.ASTNode(
					libsbmlConstants.AST_FUNCTION_COTH);
			break;
		case FUNCTION_CSC:
			libAstNode = new org.sbml.libsbml.ASTNode(
					libsbmlConstants.AST_FUNCTION_CSC);
			break;
		case FUNCTION_CSCH:
			libAstNode = new org.sbml.libsbml.ASTNode(
					libsbmlConstants.AST_FUNCTION_CSCH);
			break;
		case FUNCTION_EXP:
			libAstNode = new org.sbml.libsbml.ASTNode(
					libsbmlConstants.AST_FUNCTION_EXP);
			break;
		case FUNCTION_FACTORIAL:
			libAstNode = new org.sbml.libsbml.ASTNode(
					libsbmlConstants.AST_FUNCTION_FACTORIAL);
			break;
		case FUNCTION_FLOOR:
			libAstNode = new org.sbml.libsbml.ASTNode(
					libsbmlConstants.AST_FUNCTION_FLOOR);
			break;
		case FUNCTION_LN:
			libAstNode = new org.sbml.libsbml.ASTNode(libsbmlConstants.AST_FUNCTION_LN);
			break;
		case FUNCTION_POWER:
			libAstNode = new org.sbml.libsbml.ASTNode(
					libsbmlConstants.AST_FUNCTION_POWER);
			break;
		case FUNCTION_ROOT:
			libAstNode = new org.sbml.libsbml.ASTNode(
					libsbmlConstants.AST_FUNCTION_ROOT);
			break;
		case FUNCTION_SEC:
			libAstNode = new org.sbml.libsbml.ASTNode(
					libsbmlConstants.AST_FUNCTION_SEC);
			break;
		case FUNCTION_SECH:
			libAstNode = new org.sbml.libsbml.ASTNode(
					libsbmlConstants.AST_FUNCTION_SECH);
			break;
		case FUNCTION_SIN:
			libAstNode = new org.sbml.libsbml.ASTNode(
					libsbmlConstants.AST_FUNCTION_SIN);
			break;
		case FUNCTION_SINH:
			libAstNode = new org.sbml.libsbml.ASTNode(
					libsbmlConstants.AST_FUNCTION_SINH);
			break;
		case FUNCTION_TAN:
			libAstNode = new org.sbml.libsbml.ASTNode(
					libsbmlConstants.AST_FUNCTION_TAN);
			break;
		case FUNCTION_TANH:
			libAstNode = new org.sbml.libsbml.ASTNode(
					libsbmlConstants.AST_FUNCTION_TANH);
			break;
		case FUNCTION:
			libAstNode = new org.sbml.libsbml.ASTNode(libsbmlConstants.AST_FUNCTION);
			libAstNode.setName(astnode.getName());
			break;
		case LAMBDA:
			libAstNode = new org.sbml.libsbml.ASTNode(libsbmlConstants.AST_LAMBDA);
			break;
		case LOGICAL_AND:
			libAstNode = new org.sbml.libsbml.ASTNode(libsbmlConstants.AST_LOGICAL_AND);
			break;
		case LOGICAL_XOR:
			libAstNode = new org.sbml.libsbml.ASTNode(libsbmlConstants.AST_LOGICAL_XOR);
			break;
		case LOGICAL_OR:
			libAstNode = new org.sbml.libsbml.ASTNode(libsbmlConstants.AST_LOGICAL_OR);
			break;
		case LOGICAL_NOT:
			libAstNode = new org.sbml.libsbml.ASTNode(libsbmlConstants.AST_LOGICAL_NOT);
			break;
		case FUNCTION_PIECEWISE:
			libAstNode = new org.sbml.libsbml.ASTNode(
					libsbmlConstants.AST_FUNCTION_PIECEWISE);
			break;
		case RELATIONAL_EQ:
			libAstNode = new org.sbml.libsbml.ASTNode(
					libsbmlConstants.AST_RELATIONAL_EQ);
			break;
		case RELATIONAL_GEQ:
			libAstNode = new org.sbml.libsbml.ASTNode(
					libsbmlConstants.AST_RELATIONAL_GEQ);
			break;
		case RELATIONAL_GT:
			libAstNode = new org.sbml.libsbml.ASTNode(
					libsbmlConstants.AST_RELATIONAL_GT);
			break;
		case RELATIONAL_NEQ:
			libAstNode = new org.sbml.libsbml.ASTNode(
					libsbmlConstants.AST_RELATIONAL_NEQ);
			break;
		case RELATIONAL_LEQ:
			libAstNode = new org.sbml.libsbml.ASTNode(
					libsbmlConstants.AST_RELATIONAL_LEQ);
			break;
		case RELATIONAL_LT:
			libAstNode = new org.sbml.libsbml.ASTNode(
					libsbmlConstants.AST_RELATIONAL_LT);
			break;
		default:
			libAstNode = new org.sbml.libsbml.ASTNode(libsbmlConstants.AST_UNKNOWN);
			break;
		}
		for (ASTNode child : astnode.getListOfNodes())
			libAstNode.addChild(convert(child));
		return libAstNode;
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
			equal &= math.getName().equals(libMath.getName());
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
		if (equal && math.getNumChildren() > 0)
			for (int i = 0; i < math.getNumChildren(); i++)
				equal &= equal(math.getChild(i), libMath.getChild(i));
		return equal;
	}
}
