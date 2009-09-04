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
import org.sbml.jlibsbml.Constants;
import org.sbml.jlibsbml.Model;
import org.sbml.jlibsbml.NamedSBase;
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
		case AST_REAL:
			ast = new org.sbml.libsbml.ASTNode(libsbmlConstants.AST_REAL);
			ast.setValue(a.getReal());
			break;
		case AST_INTEGER:
			ast = new org.sbml.libsbml.ASTNode(libsbmlConstants.AST_INTEGER);
			ast.setValue(a.getInteger());
			break;
		case AST_FUNCTION_LOG:
			ast = new org.sbml.libsbml.ASTNode(
					libsbmlConstants.AST_FUNCTION_LOG);
			break;
		case AST_POWER:
			ast = new org.sbml.libsbml.ASTNode(libsbmlConstants.AST_POWER);
			break;
		case AST_PLUS:
			ast = new org.sbml.libsbml.ASTNode(libsbmlConstants.AST_PLUS);
			break;
		case AST_MINUS:
			ast = new org.sbml.libsbml.ASTNode(libsbmlConstants.AST_MINUS);
			break;
		case AST_TIMES:
			ast = new org.sbml.libsbml.ASTNode(libsbmlConstants.AST_TIMES);
			break;
		case AST_DIVIDE:
			ast = new org.sbml.libsbml.ASTNode(libsbmlConstants.AST_DIVIDE);
			break;
		case AST_RATIONAL:
			ast = new org.sbml.libsbml.ASTNode(libsbmlConstants.AST_RATIONAL);
			break;
		case AST_NAME_TIME:
			ast = new org.sbml.libsbml.ASTNode(libsbmlConstants.AST_NAME_TIME);
			break;
		case AST_FUNCTION_DELAY:
			ast = new org.sbml.libsbml.ASTNode(
					libsbmlConstants.AST_FUNCTION_DELAY);
			break;
		case AST_NAME:
			ast = new org.sbml.libsbml.ASTNode(libsbmlConstants.AST_NAME);
			ast.setName(a.getName());
			break;
		case AST_CONSTANT_PI:
			ast = new org.sbml.libsbml.ASTNode(libsbmlConstants.AST_CONSTANT_PI);
			break;
		case AST_CONSTANT_E:
			ast = new org.sbml.libsbml.ASTNode(libsbmlConstants.AST_CONSTANT_E);
			break;
		case AST_CONSTANT_TRUE:
			ast = new org.sbml.libsbml.ASTNode(
					libsbmlConstants.AST_CONSTANT_TRUE);
			break;
		case AST_CONSTANT_FALSE:
			ast = new org.sbml.libsbml.ASTNode(
					libsbmlConstants.AST_CONSTANT_FALSE);
			break;
		case AST_REAL_E:
			ast = new org.sbml.libsbml.ASTNode(libsbmlConstants.AST_REAL_E);
			ast.setValue(a.getMantissa(), a.getExponent());
			break;
		case AST_FUNCTION_ABS:
			ast = new org.sbml.libsbml.ASTNode(
					libsbmlConstants.AST_FUNCTION_ABS);
			break;
		case AST_FUNCTION_ARCCOS:
			ast = new org.sbml.libsbml.ASTNode(
					libsbmlConstants.AST_FUNCTION_ARCCOS);
			break;
		case AST_FUNCTION_ARCCOSH:
			ast = new org.sbml.libsbml.ASTNode(
					libsbmlConstants.AST_FUNCTION_ARCCOSH);
			break;
		case AST_FUNCTION_ARCCOT:
			ast = new org.sbml.libsbml.ASTNode(
					libsbmlConstants.AST_FUNCTION_ARCCOT);
			break;
		case AST_FUNCTION_ARCCOTH:
			ast = new org.sbml.libsbml.ASTNode(
					libsbmlConstants.AST_FUNCTION_ARCCOTH);
			break;
		case AST_FUNCTION_ARCCSC:
			ast = new org.sbml.libsbml.ASTNode(
					libsbmlConstants.AST_FUNCTION_ARCCSC);
			break;
		case AST_FUNCTION_ARCCSCH:
			ast = new org.sbml.libsbml.ASTNode(
					libsbmlConstants.AST_FUNCTION_ARCCSCH);
			break;
		case AST_FUNCTION_ARCSEC:
			ast = new org.sbml.libsbml.ASTNode(
					libsbmlConstants.AST_FUNCTION_ARCSEC);
			break;
		case AST_FUNCTION_ARCSECH:
			ast = new org.sbml.libsbml.ASTNode(
					libsbmlConstants.AST_FUNCTION_ARCSECH);
			break;
		case AST_FUNCTION_ARCSIN:
			ast = new org.sbml.libsbml.ASTNode(
					libsbmlConstants.AST_FUNCTION_ARCSIN);
			break;
		case AST_FUNCTION_ARCSINH:
			ast = new org.sbml.libsbml.ASTNode(
					libsbmlConstants.AST_FUNCTION_ARCSINH);
			break;
		case AST_FUNCTION_ARCTAN:
			ast = new org.sbml.libsbml.ASTNode(
					libsbmlConstants.AST_FUNCTION_ARCTAN);
			break;
		case AST_FUNCTION_ARCTANH:
			ast = new org.sbml.libsbml.ASTNode(
					libsbmlConstants.AST_FUNCTION_ARCTANH);
			break;
		case AST_FUNCTION_CEILING:
			ast = new org.sbml.libsbml.ASTNode(
					libsbmlConstants.AST_FUNCTION_CEILING);
			break;
		case AST_FUNCTION_COS:
			ast = new org.sbml.libsbml.ASTNode(
					libsbmlConstants.AST_FUNCTION_COS);
			break;
		case AST_FUNCTION_COSH:
			ast = new org.sbml.libsbml.ASTNode(
					libsbmlConstants.AST_FUNCTION_COSH);
			break;
		case AST_FUNCTION_COT:
			ast = new org.sbml.libsbml.ASTNode(
					libsbmlConstants.AST_FUNCTION_COT);
			break;
		case AST_FUNCTION_COTH:
			ast = new org.sbml.libsbml.ASTNode(
					libsbmlConstants.AST_FUNCTION_COTH);
			break;
		case AST_FUNCTION_CSC:
			ast = new org.sbml.libsbml.ASTNode(
					libsbmlConstants.AST_FUNCTION_CSC);
			break;
		case AST_FUNCTION_CSCH:
			ast = new org.sbml.libsbml.ASTNode(
					libsbmlConstants.AST_FUNCTION_CSCH);
			break;
		case AST_FUNCTION_EXP:
			ast = new org.sbml.libsbml.ASTNode(
					libsbmlConstants.AST_FUNCTION_EXP);
			break;
		case AST_FUNCTION_FACTORIAL:
			ast = new org.sbml.libsbml.ASTNode(
					libsbmlConstants.AST_FUNCTION_FACTORIAL);
			break;
		case AST_FUNCTION_FLOOR:
			ast = new org.sbml.libsbml.ASTNode(
					libsbmlConstants.AST_FUNCTION_FLOOR);
			break;
		case AST_FUNCTION_LN:
			ast = new org.sbml.libsbml.ASTNode(libsbmlConstants.AST_FUNCTION_LN);
			break;
		case AST_FUNCTION_POWER:
			ast = new org.sbml.libsbml.ASTNode(
					libsbmlConstants.AST_FUNCTION_POWER);
			break;
		case AST_FUNCTION_ROOT:
			ast = new org.sbml.libsbml.ASTNode(
					libsbmlConstants.AST_FUNCTION_ROOT);
			break;
		case AST_FUNCTION_SEC:
			ast = new org.sbml.libsbml.ASTNode(
					libsbmlConstants.AST_FUNCTION_SEC);
			break;
		case AST_FUNCTION_SECH:
			ast = new org.sbml.libsbml.ASTNode(
					libsbmlConstants.AST_FUNCTION_SECH);
			break;
		case AST_FUNCTION_SIN:
			ast = new org.sbml.libsbml.ASTNode(
					libsbmlConstants.AST_FUNCTION_SIN);
			break;
		case AST_FUNCTION_SINH:
			ast = new org.sbml.libsbml.ASTNode(
					libsbmlConstants.AST_FUNCTION_SINH);
			break;
		case AST_FUNCTION_TAN:
			ast = new org.sbml.libsbml.ASTNode(
					libsbmlConstants.AST_FUNCTION_TAN);
			break;
		case AST_FUNCTION_TANH:
			ast = new org.sbml.libsbml.ASTNode(
					libsbmlConstants.AST_FUNCTION_TANH);
			break;
		case AST_FUNCTION:
			ast = new org.sbml.libsbml.ASTNode(libsbmlConstants.AST_FUNCTION);
			ast.setName(a.getName());
			break;
		case AST_LAMBDA:
			ast = new org.sbml.libsbml.ASTNode(libsbmlConstants.AST_LAMBDA);
			break;
		case AST_LOGICAL_AND:
			ast = new org.sbml.libsbml.ASTNode(libsbmlConstants.AST_LOGICAL_AND);
			break;
		case AST_LOGICAL_XOR:
			ast = new org.sbml.libsbml.ASTNode(libsbmlConstants.AST_LOGICAL_XOR);
			break;
		case AST_LOGICAL_OR:
			ast = new org.sbml.libsbml.ASTNode(libsbmlConstants.AST_LOGICAL_OR);
			break;
		case AST_LOGICAL_NOT:
			ast = new org.sbml.libsbml.ASTNode(libsbmlConstants.AST_LOGICAL_NOT);
			break;
		case AST_FUNCTION_PIECEWISE:
			ast = new org.sbml.libsbml.ASTNode(
					libsbmlConstants.AST_FUNCTION_PIECEWISE);
			break;
		case AST_RELATIONAL_EQ:
			ast = new org.sbml.libsbml.ASTNode(
					libsbmlConstants.AST_RELATIONAL_EQ);
			break;
		case AST_RELATIONAL_GEQ:
			ast = new org.sbml.libsbml.ASTNode(
					libsbmlConstants.AST_RELATIONAL_GEQ);
			break;
		case AST_RELATIONAL_GT:
			ast = new org.sbml.libsbml.ASTNode(
					libsbmlConstants.AST_RELATIONAL_GT);
			break;
		case AST_RELATIONAL_NEQ:
			ast = new org.sbml.libsbml.ASTNode(
					libsbmlConstants.AST_RELATIONAL_NEQ);
			break;
		case AST_RELATIONAL_LEQ:
			ast = new org.sbml.libsbml.ASTNode(
					libsbmlConstants.AST_RELATIONAL_LEQ);
			break;
		case AST_RELATIONAL_LT:
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

}
