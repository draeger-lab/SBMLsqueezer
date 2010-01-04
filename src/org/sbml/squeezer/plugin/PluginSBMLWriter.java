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
package org.sbml.squeezer.plugin;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import jp.sbi.celldesigner.plugin.CellDesignerPlugin;
import jp.sbi.celldesigner.plugin.PluginAlgebraicRule;
import jp.sbi.celldesigner.plugin.PluginAssignmentRule;
import jp.sbi.celldesigner.plugin.PluginCompartment;
import jp.sbi.celldesigner.plugin.PluginCompartmentType;
import jp.sbi.celldesigner.plugin.PluginConstraint;
import jp.sbi.celldesigner.plugin.PluginEvent;
import jp.sbi.celldesigner.plugin.PluginEventAssignment;
import jp.sbi.celldesigner.plugin.PluginFunctionDefinition;
import jp.sbi.celldesigner.plugin.PluginInitialAssignment;
import jp.sbi.celldesigner.plugin.PluginKineticLaw;
import jp.sbi.celldesigner.plugin.PluginListOf;
import jp.sbi.celldesigner.plugin.PluginModel;
import jp.sbi.celldesigner.plugin.PluginModifierSpeciesReference;
import jp.sbi.celldesigner.plugin.PluginParameter;
import jp.sbi.celldesigner.plugin.PluginRateRule;
import jp.sbi.celldesigner.plugin.PluginReaction;
import jp.sbi.celldesigner.plugin.PluginRule;
import jp.sbi.celldesigner.plugin.PluginSBase;
import jp.sbi.celldesigner.plugin.PluginSimpleSpeciesReference;
import jp.sbi.celldesigner.plugin.PluginSpecies;
import jp.sbi.celldesigner.plugin.PluginSpeciesAlias;
import jp.sbi.celldesigner.plugin.PluginSpeciesReference;
import jp.sbi.celldesigner.plugin.PluginSpeciesType;
import jp.sbi.celldesigner.plugin.PluginUnit;
import jp.sbi.celldesigner.plugin.PluginUnitDefinition;
import jp.sbi.celldesigner.plugin.util.PluginCompartmentSymbolType;
import jp.sbi.celldesigner.plugin.util.PluginReactionSymbolType;
import jp.sbi.celldesigner.plugin.util.PluginSpeciesSymbolType;

import org.sbml.jsbml.ASTNode;
import org.sbml.jsbml.AlgebraicRule;
import org.sbml.jsbml.AssignmentRule;
import org.sbml.jsbml.CVTerm;
import org.sbml.jsbml.Compartment;
import org.sbml.jsbml.CompartmentType;
import org.sbml.jsbml.Constraint;
import org.sbml.jsbml.Delay;
import org.sbml.jsbml.Event;
import org.sbml.jsbml.EventAssignment;
import org.sbml.jsbml.FunctionDefinition;
import org.sbml.jsbml.InitialAssignment;
import org.sbml.jsbml.KineticLaw;
import org.sbml.jsbml.ListOf;
import org.sbml.jsbml.MathContainer;
import org.sbml.jsbml.Model;
import org.sbml.jsbml.ModelHistory;
import org.sbml.jsbml.ModifierSpeciesReference;
import org.sbml.jsbml.NamedSBase;
import org.sbml.jsbml.Parameter;
import org.sbml.jsbml.RateRule;
import org.sbml.jsbml.Reaction;
import org.sbml.jsbml.Rule;
import org.sbml.jsbml.SBMLException;
import org.sbml.jsbml.SBMLWriter;
import org.sbml.jsbml.SBO;
import org.sbml.jsbml.SBase;
import org.sbml.jsbml.SimpleSpeciesReference;
import org.sbml.jsbml.Species;
import org.sbml.jsbml.SpeciesReference;
import org.sbml.jsbml.SpeciesType;
import org.sbml.jsbml.StoichiometryMath;
import org.sbml.jsbml.Trigger;
import org.sbml.jsbml.Unit;
import org.sbml.jsbml.UnitDefinition;
import org.sbml.jsbml.io.IOProgressListener;
import org.sbml.jsbml.xml.libsbml.LibSBMLWriter;
import org.sbml.libsbml.libsbml;
import org.sbml.libsbml.libsbmlConstants;
import org.sbml.squeezer.io.MessageListener;
import org.sbml.squeezer.io.MessageProcessor;

/**
 * @author Andreas Dr&auml;ger <a
 *         href="mailto:andreas.draeger@uni-tuebingen.de">
 *         andreas.draeger@uni-tuebingen.de</a>
 * 
 */
public class PluginSBMLWriter implements SBMLWriter {

	/**
	 * 
	 */
	private static final String error = " must be an instance of ";
	/**
	 * 
	 */
	private static MessageListener msg = new MessageProcessor();
	/**
	 * 
	 */
	private CellDesignerPlugin plugin;
	/**
	 * 
	 */
	private PluginModel pluginModel;
	/**
	 * 
	 */
	private Set<IOProgressListener> setIOListeners;

	/**
	 * 
	 * @param plugin
	 */
	public PluginSBMLWriter(CellDesignerPlugin plugin) {
		this.plugin = plugin;
		msg.setVerbose(false);
		setIOListeners = new HashSet<IOProgressListener>();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seeorg.sbml.jsbml.SBMLWriter#addIOProgressListener(org.sbml.jsbml.io.
	 * IOProgressListener)
	 */
	public void addIOProgressListener(IOProgressListener listener) {
		setIOListeners.add(listener);
	}

	/**
	 * 
	 * @param ast
	 * @return
	 */
	private org.sbml.libsbml.ASTNode convert(ASTNode astnode) {
		org.sbml.libsbml.ASTNode libAstNode;
		switch (astnode.getType()) {
		case REAL:
			libAstNode = new org.sbml.libsbml.ASTNode(libsbmlConstants.AST_REAL);
			libAstNode.setValue(astnode.getReal());
			break;
		case INTEGER:
			libAstNode = new org.sbml.libsbml.ASTNode(
					libsbmlConstants.AST_INTEGER);
			libAstNode.setValue(astnode.getInteger());
			break;
		case FUNCTION_LOG:
			libAstNode = new org.sbml.libsbml.ASTNode(
					libsbmlConstants.AST_FUNCTION_LOG);
			break;
		case POWER:
			libAstNode = new org.sbml.libsbml.ASTNode(
					libsbmlConstants.AST_POWER);
			break;
		case PLUS:
			libAstNode = new org.sbml.libsbml.ASTNode(libsbmlConstants.AST_PLUS);
			break;
		case MINUS:
			libAstNode = new org.sbml.libsbml.ASTNode(
					libsbmlConstants.AST_MINUS);
			break;
		case TIMES:
			libAstNode = new org.sbml.libsbml.ASTNode(
					libsbmlConstants.AST_TIMES);
			break;
		case DIVIDE:
			libAstNode = new org.sbml.libsbml.ASTNode(
					libsbmlConstants.AST_DIVIDE);
			break;
		case RATIONAL:
			libAstNode = new org.sbml.libsbml.ASTNode(
					libsbmlConstants.AST_RATIONAL);
			break;
		case NAME_TIME:
			libAstNode = new org.sbml.libsbml.ASTNode(
					libsbmlConstants.AST_NAME_TIME);
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
			libAstNode = new org.sbml.libsbml.ASTNode(
					libsbmlConstants.AST_CONSTANT_PI);
			break;
		case CONSTANT_E:
			libAstNode = new org.sbml.libsbml.ASTNode(
					libsbmlConstants.AST_CONSTANT_E);
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
			libAstNode = new org.sbml.libsbml.ASTNode(
					libsbmlConstants.AST_REAL_E);
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
			libAstNode = new org.sbml.libsbml.ASTNode(
					libsbmlConstants.AST_FUNCTION_LN);
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
			libAstNode = new org.sbml.libsbml.ASTNode(
					libsbmlConstants.AST_FUNCTION);
			libAstNode.setName(astnode.getName());
			break;
		case LAMBDA:
			libAstNode = new org.sbml.libsbml.ASTNode(
					libsbmlConstants.AST_LAMBDA);
			break;
		case LOGICAL_AND:
			libAstNode = new org.sbml.libsbml.ASTNode(
					libsbmlConstants.AST_LOGICAL_AND);
			break;
		case LOGICAL_XOR:
			libAstNode = new org.sbml.libsbml.ASTNode(
					libsbmlConstants.AST_LOGICAL_XOR);
			break;
		case LOGICAL_OR:
			libAstNode = new org.sbml.libsbml.ASTNode(
					libsbmlConstants.AST_LOGICAL_OR);
			break;
		case LOGICAL_NOT:
			libAstNode = new org.sbml.libsbml.ASTNode(
					libsbmlConstants.AST_LOGICAL_NOT);
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
			libAstNode = new org.sbml.libsbml.ASTNode(
					libsbmlConstants.AST_UNKNOWN);
			break;
		}
		for (ASTNode child : astnode.getListOfNodes())
			libAstNode.addChild(convert(child));
		return libAstNode;
	}

	/**
	 * 
	 * @param date
	 * @return
	 */
	private Object convertDate(Date date) {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * Determins whether the two ASTNode objects are equal.
	 * 
	 * @param math
	 * @param libMath
	 * @return
	 */
	private boolean equal(ASTNode math, org.sbml.libsbml.ASTNode libMath) {
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

	/**
	 * 
	 * 
	 * @param u
	 * @param unit
	 * @return
	 */
	private boolean equal(Unit u, PluginUnit unit) {
		if (u == null || unit == null)
			return false;
		boolean equal = true;
		switch (unit.getKind()) {
		case libsbmlConstants.UNIT_KIND_AMPERE:
			equal &= u.getKind() == Unit.Kind.AMPERE;
			break;
		case libsbmlConstants.UNIT_KIND_BECQUEREL:
			equal &= u.getKind() == Unit.Kind.BECQUEREL;
			break;
		case libsbmlConstants.UNIT_KIND_CANDELA:
			equal &= u.getKind() == Unit.Kind.CANDELA;
			break;
		case libsbmlConstants.UNIT_KIND_CELSIUS:
			equal &= u.getKind() == Unit.Kind.CELSIUS;
			break;
		case libsbmlConstants.UNIT_KIND_COULOMB:
			equal &= u.getKind() == Unit.Kind.COULOMB;
			break;
		case libsbmlConstants.UNIT_KIND_DIMENSIONLESS:
			equal &= u.getKind() == Unit.Kind.DIMENSIONLESS;
			break;
		case libsbmlConstants.UNIT_KIND_FARAD:
			equal &= u.getKind() == Unit.Kind.FARAD;
			break;
		case libsbmlConstants.UNIT_KIND_GRAM:
			equal &= u.getKind() == Unit.Kind.GRAM;
			break;
		case libsbmlConstants.UNIT_KIND_GRAY:
			equal &= u.getKind() == Unit.Kind.GRAY;
			break;
		case libsbmlConstants.UNIT_KIND_HENRY:
			equal &= u.getKind() == Unit.Kind.HENRY;
			break;
		case libsbmlConstants.UNIT_KIND_HERTZ:
			equal &= u.getKind() == Unit.Kind.HERTZ;
			break;
		case libsbmlConstants.UNIT_KIND_INVALID:
			equal &= u.getKind() == Unit.Kind.INVALID;
			break;
		case libsbmlConstants.UNIT_KIND_ITEM:
			equal &= u.getKind() == Unit.Kind.ITEM;
			break;
		case libsbmlConstants.UNIT_KIND_JOULE:
			equal &= u.getKind() == Unit.Kind.JOULE;
			break;
		case libsbmlConstants.UNIT_KIND_KATAL:
			equal &= u.getKind() == Unit.Kind.KATAL;
			break;
		case libsbmlConstants.UNIT_KIND_KELVIN:
			equal &= u.getKind() == Unit.Kind.KELVIN;
			break;
		case libsbmlConstants.UNIT_KIND_KILOGRAM:
			equal &= u.getKind() == Unit.Kind.KILOGRAM;
			break;
		case libsbmlConstants.UNIT_KIND_LITER:
			equal &= u.getKind() == Unit.Kind.LITER;
			break;
		case libsbmlConstants.UNIT_KIND_LITRE:
			equal &= u.getKind() == Unit.Kind.LITRE;
			break;
		case libsbmlConstants.UNIT_KIND_LUMEN:
			equal &= u.getKind() == Unit.Kind.LUMEN;
			break;
		case libsbmlConstants.UNIT_KIND_LUX:
			equal &= u.getKind() == Unit.Kind.LUX;
			break;
		case libsbmlConstants.UNIT_KIND_METER:
			equal &= u.getKind() == Unit.Kind.METER;
			break;
		case libsbmlConstants.UNIT_KIND_METRE:
			equal &= u.getKind() == Unit.Kind.METRE;
			break;
		case libsbmlConstants.UNIT_KIND_MOLE:
			equal &= u.getKind() == Unit.Kind.MOLE;
			break;
		case libsbmlConstants.UNIT_KIND_NEWTON:
			equal &= u.getKind() == Unit.Kind.NEWTON;
			break;
		case libsbmlConstants.UNIT_KIND_OHM:
			equal &= u.getKind() == Unit.Kind.OHM;
			break;
		case libsbmlConstants.UNIT_KIND_PASCAL:
			equal &= u.getKind() == Unit.Kind.PASCAL;
			break;
		case libsbmlConstants.UNIT_KIND_RADIAN:
			equal &= u.getKind() == Unit.Kind.RADIAN;
			break;
		case libsbmlConstants.UNIT_KIND_SECOND:
			equal &= u.getKind() == Unit.Kind.SECOND;
			break;
		case libsbmlConstants.UNIT_KIND_SIEMENS:
			equal &= u.getKind() == Unit.Kind.SIEMENS;
			break;
		case libsbmlConstants.UNIT_KIND_SIEVERT:
			equal &= u.getKind() == Unit.Kind.SIEVERT;
			break;
		case libsbmlConstants.UNIT_KIND_STERADIAN:
			equal &= u.getKind() == Unit.Kind.STERADIAN;
			break;
		case libsbmlConstants.UNIT_KIND_TESLA:
			equal &= u.getKind() == Unit.Kind.TESLA;
			break;
		case libsbmlConstants.UNIT_KIND_VOLT:
			equal &= u.getKind() == Unit.Kind.VOLT;
			break;
		case libsbmlConstants.UNIT_KIND_WATT:
			equal &= u.getKind() == Unit.Kind.WATT;
			break;
		case libsbmlConstants.UNIT_KIND_WEBER:
			equal &= u.getKind() == Unit.Kind.WEBER;
			break;
		}
		equal &= u.getExponent() == unit.getExponent();
		equal &= u.getMultiplier() == unit.getMultiplier();
		equal &= u.getScale() == unit.getScale();
		equal &= u.getOffset() == unit.getOffset();
		return equal;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.jsbml.SBMLWriter#getNumErrors(java.lang.Object)
	 */
	public int getNumErrors(Object sbase) {
		// TODO Auto-generated method stub
		return 0;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.jsbml.SBMLWriter#getWriteWarnings(java.lang.Object)
	 */
	public List<SBMLException> getWriteWarnings(Object sbase) {
		return new LinkedList<SBMLException>();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.sbml.jsbml.SBMLWriter#removeUnneccessaryElements(org.sbml.jsbml.Model
	 * , java.lang.Object)
	 */
	public void removeUnneccessaryElements(Model model, Object orig) {
		if (!(orig instanceof PluginModel))
			throw new IllegalArgumentException(
					"only instances of PluginModel can be considered.");
		pluginModel = (PluginModel) orig;
		boolean changed = false;
		int i;

		// remove unnecessary function definitions
		for (i = pluginModel.getNumFunctionDefinitions() - 1; i >= 0; i--) {
			PluginFunctionDefinition c = pluginModel.getFunctionDefinition(i);
			if (model.getFunctionDefinition(c.getId()) == null) {
				plugin.notifySBaseDeleted(pluginModel
						.getListOfFunctionDefinitions().remove(i));
				changed = true;
			}
		}
		if (changed) {
			plugin.notifySBaseChanged(pluginModel
					.getListOfFunctionDefinitions());
			changed = false;
			msg.logln("removed unnecessary function definitions");
		}

		// remove unnecessary units
		for (i = pluginModel.getNumUnitDefinitions() - 1; i >= 0; i--) {
			PluginUnitDefinition ud = pluginModel.getUnitDefinition(i);
			if (model.getUnitDefinition(ud.getId()) == null) {
				plugin.notifySBaseDeleted(pluginModel
						.getListOfUnitDefinitions().remove(i));
				changed = true;
			}
		}
		if (changed) {
			plugin.notifySBaseChanged(pluginModel.getListOfUnitDefinitions());
			changed = false;
			msg.logln("removed unnecessary unit definitions");
		}

		// remove unnecessary compartmentTypes
		for (i = pluginModel.getNumCompartmentTypes() - 1; i >= 0; i--) {
			PluginCompartmentType c = pluginModel.getCompartmentType(i);
			if (model.getCompartmentType(c.getId()) == null) {
				plugin.notifySBaseDeleted(pluginModel
						.getListOfCompartmentTypes().remove(i));
				changed = true;
			}
		}
		if (changed) {
			plugin.notifySBaseChanged(pluginModel.getListOfCompartmentTypes());
			changed = false;
			msg.logln("removed unnecessary compartment types");
		}

		// remove unnecessary speciesTypes
		for (i = pluginModel.getNumSpeciesTypes() - 1; i >= 0; i--) {
			PluginSpeciesType c = pluginModel.getSpeciesType(i);
			if (model.getSpeciesType(c.getId()) == null) {
				plugin.notifySBaseDeleted(pluginModel.getListOfSpeciesTypes()
						.remove(i));
				changed = true;
			}
		}
		if (changed) {
			plugin.notifySBaseChanged(pluginModel.getListOfSpeciesTypes());
			changed = false;
			msg.logln("removed unnecessary species types");
		}

		// remove unnecessary compartments
		for (i = pluginModel.getNumCompartments() - 1; i >= 0; i--) {
			PluginCompartment c = pluginModel.getCompartment(i);
			if (model.getCompartment(c.getId()) == null) {
				plugin.notifySBaseDeleted(pluginModel.getListOfCompartments()
						.remove(i));
				changed = true;
			}
		}
		if (changed) {
			plugin.notifySBaseChanged(pluginModel.getListOfCompartments());
			changed = false;
			msg.logln("removed unnecessary compartments");
		}

		// remove unnecessary species
		for (i = pluginModel.getNumSpecies() - 1; i >= 0; i--) {
			PluginSpecies s = pluginModel.getSpecies(i);
			if (model.getSpecies(s.getId()) == null) {
				plugin.notifySBaseDeleted(pluginModel.getListOfSpecies()
						.remove(i));
				changed = true;
			}
		}
		if (changed) {
			plugin.notifySBaseChanged(pluginModel.getListOfSpecies());
			changed = false;
			msg.logln("removed unnecessary species");
		}

		// remove parameters
		for (i = pluginModel.getNumParameters() - 1; i >= 0; i--) {
			PluginParameter p = pluginModel.getParameter(i);
			if (model.getParameter(p.getId()) == null) {
				plugin.notifySBaseDeleted(pluginModel.getListOfParameters()
						.remove(i));
				changed = true;
			}
		}
		if (changed) {
			plugin.notifySBaseChanged(pluginModel.getListOfParameters());
			changed = false;
			msg.logln("removed unnecessary parameters");
		}

		// remove unnecessary initial assignments
		for (i = pluginModel.getNumInitialAssignments() - 1; i >= 0; i--) {
			PluginInitialAssignment c = pluginModel.getInitialAssignment(i);
			boolean contains = false;
			for (int j = 0; j < model.getNumInitialAssignments() && !contains; j++) {
				InitialAssignment ia = model.getInitialAssignment(j);
				if (ia.getSymbol().equals(c.getSymbol())
						&& equal(ia.getMath(), libsbml
								.parseFormula(c.getMath())))
					contains = true;
			}
			if (!contains) {
				plugin.notifySBaseDeleted(pluginModel
						.getListOfInitialAssignments().remove(i));
				changed = true;
			}
		}
		if (changed) {
			plugin
					.notifySBaseChanged(pluginModel
							.getListOfInitialAssignments());
			changed = false;
			msg.logln("removed unnecessary initial assignments");
		}

		// remove unnecessary rules
		for (i = pluginModel.getNumRules() - 1; i >= 0; i--) {
			PluginRule c = pluginModel.getRule(i);
			boolean contains = false;
			for (int j = 0; j < model.getNumRules() && !contains; j++) {
				Rule r = model.getRule(j);
				if ((c instanceof PluginRateRule && r instanceof RateRule && ((PluginRateRule) c)
						.getVariable().equals(((RateRule) r).getVariable()))
						|| (c instanceof PluginAssignmentRule
								&& r instanceof AssignmentRule && ((AssignmentRule) r)
								.getVariable().equals(
										((PluginAssignmentRule) c)
												.getVariable()))
						|| (c instanceof PluginAlgebraicRule && r instanceof AlgebraicRule))
					if (equal(r.getMath(), c.getMath()))
						contains = true;
			}
			if (!contains) {
				plugin.notifySBaseDeleted(pluginModel.getListOfRules()
						.remove(i));
				changed = true;
			}
		}
		if (changed) {
			plugin.notifySBaseChanged(pluginModel.getListOfRules());
			changed = false;
			msg.logln("removed unnecessary rules");
		}

		// remove unnecessary constraints
		for (i = pluginModel.getNumConstraints() - 1; i >= 0; i--) {
			PluginConstraint c = pluginModel.getConstraint(i);
			boolean contains = false;
			for (int j = 0; j < model.getNumConstraints() && !contains; j++) {
				Constraint ia = model.getConstraint(j);
				if (equal(ia.getMath(), libsbml.parseFormula(c.getMath())))
					contains = true;
			}
			if (!contains) {
				plugin.notifySBaseDeleted(pluginModel.getListOfConstraints()
						.remove(i));
				changed = true;
			}
		}
		if (changed) {
			plugin.notifySBaseChanged(pluginModel.getListOfConstraints());
			changed = false;
			msg.logln("removed unnecessary constraints");
		}

		// remove reactions
		for (i = pluginModel.getNumReactions() - 1; i >= 0; i--) {
			PluginReaction r = pluginModel.getReaction(i);
			if (model.getReaction(r.getId()) == null) {
				plugin.notifySBaseDeleted(pluginModel.getListOfReactions()
						.remove(i));
				changed = true;
			}
		}
		if (changed) {
			plugin.notifySBaseChanged(pluginModel.getListOfReactions());
			changed = false;
			msg.logln("removed unnecessary reactions");
		}

		// remove events
		for (i = pluginModel.getNumEvents() - 1; i >= 0; i--) {
			PluginEvent eventOrig = pluginModel.getEvent(i);
			boolean contains = false;
			for (Event e : model.getListOfEvents())
				if (e.getId().equals(eventOrig)
						|| e.getName().equals(eventOrig.getName())
						|| e.getNotesString()
								.equals(eventOrig.getNotesString()))
					contains = true;
			if (!contains) {
				plugin.notifySBaseDeleted(pluginModel.getListOfEvents().remove(
						i));
				changed = true;
			}
		}
		if (changed) {
			plugin.notifySBaseChanged(pluginModel.getListOfEvents());
			changed = false;
			msg.logln("removed unnecessary events");
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.jsbml.SBMLWriter#saveChanges(org.sbml.jsbml.Model,
	 * java.lang.Object)
	 */
	public void saveChanges(Model model, Object orig) throws SBMLException {
		if (!(orig instanceof PluginModel))
			throw new IllegalArgumentException(
					"only instances of PluginModel can be considered.");
		pluginModel = (PluginModel) orig;
		int i;

		// Function definitions
		for (FunctionDefinition c : model.getListOfFunctionDefinitions()) {
			if (pluginModel.getFunctionDefinition(c.getId()) == null) {
				PluginFunctionDefinition fd = writeFunctionDefinition(c);
				pluginModel.addFunctionDefinition(fd);
				plugin.notifySBaseAdded(fd);
			} else {
				PluginFunctionDefinition fd = pluginModel
						.getFunctionDefinition(c.getId());
				saveMathContainerProperties(c, fd);
				plugin.notifySBaseChanged(fd);
			}
		}

		// Unit definitions
		for (UnitDefinition ud : model.getListOfUnitDefinitions())
			if (!ud.equals(UnitDefinition.substance(ud.getLevel(), ud
					.getVersion()))
					&& !ud.equals(UnitDefinition.volume(ud.getLevel(), ud
							.getVersion()))
					&& !ud.equals(UnitDefinition.area(ud.getLevel(), ud
							.getVersion()))
					&& !ud.equals(UnitDefinition.length(ud.getLevel(), ud
							.getVersion()))
					&& !ud.equals(UnitDefinition.time(ud.getLevel(), ud
							.getVersion()))) {
				PluginUnitDefinition libU = pluginModel.getUnitDefinition(ud
						.getId());
				if (libU != null)
					saveUnitDefinitionProperties(ud, libU);
				else {
					PluginUnitDefinition udef = writeUnitDefinition(ud);
					pluginModel.addUnitDefinition(udef);
					plugin.notifySBaseAdded(udef);
				}
			}

		// Compartment types
		for (CompartmentType c : model.getListOfCompartmentTypes()) {
			if (pluginModel.getCompartmentType(c.getId()) == null) {
				PluginCompartmentType ct = writeCompartmentType(c);
				pluginModel.addCompartmentType(ct);
				plugin.notifySBaseAdded(ct);
			} else {
				PluginCompartmentType ct = pluginModel.getCompartmentType(c
						.getId());
				saveSBaseProperties(c, ct);
				plugin.notifySBaseChanged(ct);
			}
		}

		// Species types
		for (SpeciesType c : model.getListOfSpeciesTypes()) {
			if (pluginModel.getSpeciesType(c.getId()) == null) {
				PluginSpeciesType st = writeSpeciesType(c);
				pluginModel.addSpeciesType(st);
				plugin.notifySBaseAdded(st);
			} else {
				PluginSpeciesType st = pluginModel.getSpeciesType(c.getId());
				saveSBaseProperties(c, st);
				plugin.notifySBaseChanged(st);
			}
		}

		// Compartments
		for (Compartment c : model.getListOfCompartments()) {
			if (pluginModel.getCompartment(c.getId()) == null) {
				PluginCompartment pc = writeCompartment(c);
				pluginModel.addCompartment(pc);
				plugin.notifySBaseAdded(pc);
			} else {
				PluginCompartment pc = pluginModel.getCompartment(c.getId());
				saveCompartmentProperties(c, pc);
				plugin.notifySBaseChanged(pc);
			}
		}

		// Species
		for (Species s : model.getListOfSpecies()) {
			if (pluginModel.getSpecies(s.getId()) == null) {
				PluginSpecies ps = writeSpecies(s);
				pluginModel.addSpecies(ps);
				plugin.notifySBaseAdded(ps);
			} else {
				PluginSpecies ps = pluginModel.getSpecies(s.getId());
				saveSpeciesProperties(s, ps);
				plugin.notifySBaseChanged(ps);
			}
		}

		// add or change parameters
		for (Parameter p : model.getListOfParameters()) {
			if (pluginModel.getParameter(p.getId()) == null) {
				PluginParameter pp = writeParameter(p, pluginModel);
				pluginModel.addParameter(pp);
				plugin.notifySBaseAdded(pp);
			} else {
				PluginParameter pp = pluginModel.getParameter(p.getId());
				saveParameterProperties(p, pp);
				plugin.notifySBaseChanged(pp);
			}
		}

		// initial assignments
		for (i = 0; i < model.getNumInitialAssignments(); i++) {
			InitialAssignment ia = model.getInitialAssignment(i);
			int contains = -1;
			for (int j = 0; j < pluginModel.getNumInitialAssignments()
					&& contains < 0; j++) {
				PluginInitialAssignment libIA = pluginModel
						.getInitialAssignment(j);
				if (libIA.getSymbol().equals(ia.getSymbol())
						&& equal(ia.getMath(), libsbml.parseFormula(libIA
								.getMath())))
					contains = j;
			}
			if (contains < 0) {
				PluginInitialAssignment pia = writeInitialAssignment(ia);
				pluginModel.addInitialAssignment(pia);
				plugin.notifySBaseAdded(pia);
			} else {
				PluginInitialAssignment pia = pluginModel
						.getInitialAssignment(contains);
				saveMathContainerProperties(ia, pia);
				plugin.notifySBaseChanged(pia);
			}
		}

		// rules
		for (i = 0; i < model.getNumRules(); i++) {
			Rule rule = model.getRule(i);
			int contains = -1;
			for (int j = 0; j < pluginModel.getNumRules() && contains < 0; j++) {
				boolean equal = false;
				PluginRule ruleOrig = pluginModel.getRule(j);
				if (rule instanceof RateRule
						&& ruleOrig instanceof PluginRateRule) {
					equal = ((RateRule) rule).getVariable().equals(
							((PluginRateRule) ruleOrig).getVariable());
				} else if (rule instanceof AssignmentRule
						&& ruleOrig instanceof PluginAssignmentRule) {
					equal = ((AssignmentRule) rule).getVariable().equals(
							((PluginAssignmentRule) ruleOrig).getVariable());
				} else if (rule instanceof AlgebraicRule
						&& ruleOrig instanceof PluginAlgebraicRule) {
					equal = true;
				}
				if (equal)
					equal &= equal(rule.getMath(), ruleOrig.getMath());
				if (equal)
					contains = j;
			}
			if (contains < 0) {
				PluginRule r = writeRule(rule);
				pluginModel.addRule(r);
				plugin.notifySBaseAdded(r);
			} else {
				// math is equal anyway...
				PluginRule r = pluginModel.getRule(contains);
				saveSBaseProperties(rule, r);
				plugin.notifySBaseChanged(r);
			}
		}

		// constraints
		for (i = 0; i < model.getNumConstraints(); i++) {
			Constraint ia = model.getConstraint(i);
			int contains = -1;
			for (int j = 0; j < pluginModel.getNumConstraints() && contains < 0; j++) {
				PluginConstraint c = pluginModel.getConstraint(j);
				if (equal(ia.getMath(), libsbml.parseFormula(c.getMath())))
					contains = j;
			}
			if (contains < 0) {
				PluginConstraint constr = writeConstraint(ia);
				pluginModel.addConstraint(constr);
				plugin.notifySBaseAdded(constr);
			} else {
				PluginConstraint constr = pluginModel.getConstraint(contains);
				saveMathContainerProperties(ia, constr);
				plugin.notifySBaseChanged(constr);
			}
		}

		// add or change reactions
		for (Reaction r : model.getListOfReactions()) {
			if (pluginModel.getReaction(r.getId()) == null) {
				PluginReaction reac = writeReaction(r);
				pluginModel.addReaction(reac);
				plugin.notifySBaseAdded(reac);
			} else {
				PluginReaction reac = pluginModel.getReaction(r.getId());
				saveReactionProperties(r, reac);
				plugin.notifySBaseChanged(reac);
			}
		}

		// events
		for (i = 0; i < model.getNumEvents(); i++) {
			Event ia = model.getEvent(i);
			int contains = -1;
			for (int j = 0; j < pluginModel.getNumEvents() && contains < 0; j++) {
				PluginEvent libIA = pluginModel.getEvent(j);
				if (libIA.getId().equals(ia.getId())
						|| libIA.getName().equals(ia.getName())
						|| libIA.getNotesString().equals(ia.getNotesString()))
					contains = j;
			}
			if (contains < 0) {
				PluginEvent pia = writeEvent(ia);
				pluginModel.addEvent(pia);
				plugin.notifySBaseAdded(pia);
			} else {
				PluginEvent pia = pluginModel.getEvent(contains);
				saveEventProperties(ia, pia);
				plugin.notifySBaseChanged(pia);
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.jsbml.SBMLWriter#saveChanges(org.sbml.jsbml.Reaction,
	 * java.lang.Object)
	 */
	public void saveChanges(Reaction reaction, Object model)
			throws SBMLException {
		if (!(model instanceof PluginModel))
			throw new IllegalArgumentException("model" + error + "PluginModel");
		pluginModel = (PluginModel) model;
		msg.logln("Obtained plugin model.");
		for (SpeciesReference specRef : reaction.getListOfReactants())
			saveChanges(specRef.getSpeciesInstance(), pluginModel);
		msg.logln("Saved reactants");
		for (SpeciesReference specRef : reaction.getListOfProducts())
			saveChanges(specRef.getSpeciesInstance(), pluginModel);
		msg.logln("saved products");
		for (ModifierSpeciesReference modSpecRef : reaction
				.getListOfModifiers())
			saveChanges(modSpecRef.getSpeciesInstance(), pluginModel);
		msg.logln("saved modifiers");
		if (reaction.isSetKineticLaw() && reaction.getKineticLaw().isSetMath()) {
			ASTNode math = reaction.getKineticLaw().getMath();
			Model m = reaction.getModel();
			for (FunctionDefinition fd : m.getListOfFunctionDefinitions())
				if (math.refersTo(fd.getId())) {
					if (pluginModel.getFunctionDefinition(fd.getId()) == null) {
						PluginFunctionDefinition pluF = writeFunctionDefinition(fd);
						pluginModel.addFunctionDefinition(pluF);
						plugin.notifySBaseAdded(pluF);
					} else
						saveMathContainerProperties(fd, pluginModel
								.getFunctionDefinition(fd.getId()));
				}
			msg.logln("saved all function definition references");
			for (Compartment c : m.getListOfCompartments())
				if (math.refersTo(c.getId())) {
					if (c.isSetUnits()
							&& !Unit.isUnitKind(c.getUnits(), c.getLevel(), c
									.getVersion())
							&& pluginModel.getUnitDefinition(c.getUnits()) == null) {
						PluginUnitDefinition pluU = writeUnitDefinition(c
								.getUnitsInstance());
						pluginModel.addUnitDefinition(pluU);
						plugin.notifySBaseAdded(pluU);
					}
					if (pluginModel.getCompartment(c.getId()) == null) {
						PluginCompartment pluC = writeCompartment(c);
						pluginModel.addCompartment(pluC);
						plugin.notifySBaseAdded(pluC);
					} else
						saveCompartmentProperties(c, pluginModel
								.getCompartment(c.getId()));
				}
			msg.logln("saved all compartment references");
			for (Parameter p : m.getListOfParameters())
				if (math.refersTo(p.getId())) {
					if (p.isSetUnits()
							&& !Unit.isUnitKind(p.getUnits(), p.getLevel(), p
									.getVersion())) {
						if (pluginModel.getUnitDefinition(p.getUnits()) == null) {
							PluginUnitDefinition ud = writeUnitDefinition(p
									.getUnitsInstance());
							pluginModel.addUnitDefinition(ud);
							plugin.notifySBaseAdded(ud);
						} else {
							saveUnitDefinitionProperties(p.getUnitsInstance(),
									pluginModel.getUnitDefinition(p.getUnits()));
						}
					}
					if (pluginModel.getParameter(p.getId()) == null) {
						PluginParameter pluP = writeParameter(p, pluginModel);
						pluginModel.addParameter(pluP);
						plugin.notifySBaseAdded(pluP);
					} else
						saveParameterProperties(p, pluginModel.getParameter(p
								.getId()));
					plugin
							.notifySBaseChanged(pluginModel
									.getListOfParameters());
				}
			msg.logln("saved all parameter references");
		}
		msg.logln("saved kinetic law");
		saveReactionProperties(reaction, pluginModel.getReaction(reaction
				.getId()));
		msg.logln("saved all other reaction properties");
		removeUnneccessaryElements(reaction.getModel(), model);
		msg.logln("removed unnecessary elements");
	}

	/**
	 * 
	 * @param speciesInstance
	 * @param pluMo
	 */
	private void saveChanges(Species species, PluginModel pluMo) {
		PluginSpecies pluSpec = pluMo.getSpecies(species.getId());
		if (species.isSetSubstanceUnits()
				&& !Unit.isUnitKind(species.getSubstanceUnits(), species
						.getLevel(), species.getVersion())) {
			if (pluMo.getUnitDefinition(species.getSubstanceUnits()) == null) {
				PluginUnitDefinition ud = writeUnitDefinition(species
						.getSubstanceUnitsInstance());
				pluMo.addUnitDefinition(ud);
				plugin.notifySBaseAdded(ud);
			} else {
				saveUnitDefinitionProperties(species
						.getSubstanceUnitsInstance(), pluMo
						.getUnitDefinition(species.getSubstanceUnits()));
			}
		}
		if (species.isSetCompartment()) {
			if (pluMo.getCompartment(species.getCompartment()) == null) {
				PluginCompartment c = writeCompartment(species
						.getCompartmentInstance());
				pluMo.addCompartment(c);
				plugin.notifySBaseAdded(c);
			} else
				saveCompartmentProperties(species.getCompartmentInstance(),
						pluMo.getCompartment(species.getCompartment()));
		}
		if (species.isSetSpeciesType()) {
			if (pluMo.getSpeciesType(species.getSpeciesType()) == null) {
				PluginSpeciesType st = writeSpeciesType(species
						.getSpeciesTypeInstance());
				pluMo.addSpeciesType(st);
				plugin.notifySBaseAdded(st);
			} else
				saveNamedSBaseProperties(species.getSpeciesTypeInstance(),
						pluMo.getSpeciesType(species.getSpeciesType()));
		}
		saveSpeciesProperties(species, pluSpec);
	}

	/**
	 * 
	 * @param c
	 * @param compartment
	 */
	private void saveCompartmentProperties(Compartment c, Object compartment) {
		if (!(compartment instanceof PluginCompartment))
			throw new IllegalArgumentException(
					"compartment must be an instance of PluginCompartment.");
		PluginCompartment comp = (PluginCompartment) compartment;
		saveNamedSBaseProperties(c, comp);
		if (c.isSetSize() && c.getSize() != comp.getSize())
			comp.setSize(c.getSize());
		if (c.isSetCompartmentType()
				&& !c.getCompartmentType().equals(comp.getCompartmentType()))
			comp.setCompartmentType(c.getCompartmentType());
		if (c.getSpatialDimensions() != comp.getSpatialDimensions())
			comp.setSpatialDimensions(c.getSpatialDimensions());
		if (c.isSetUnits() && !c.getUnits().equals(comp.getUnits()))
			comp.setUnits(c.getUnits());
		if (c.isSetOutside() && !c.getOutside().equals(comp.getOutside()))
			comp.setOutside(c.getOutside());
		if (c.getConstant() != comp.getConstant())
			comp.setConstant(c.getConstant());
		plugin.notifySBaseChanged(comp);
	}

	/**
	 * 
	 * @param cvt
	 * @param term
	 */
	private void saveCVTermProperties(CVTerm cvt, Object term) {
		// TODO Auto-generated method stub
	}

	/**
	 * 
	 * @param ev
	 * @param event
	 * @throws SBMLException
	 */
	private void saveEventProperties(Event ev, Object event)
			throws SBMLException {
		if (!(event instanceof PluginEvent))
			throw new IllegalArgumentException(
					"event must be an instance of PluginEvent.");
		PluginEvent e = (PluginEvent) event;
		saveNamedSBaseProperties(ev, e);
		if (ev.getUseValuesFromTriggerTime() != e.getUseValuesFromTriggerTime())
			e.setUseValuesFromTriggerTime(ev.getUseValuesFromTriggerTime());
		if (ev.isSetTimeUnits() && ev.getTimeUnits() != e.getTimeUnits())
			e.setTimeUnits(ev.getTimeUnits());
		if (ev.isSetDelay()) {
			if (e.getDelay() != null) {
				e.setDelay(writeDelay(ev.getDelay()));
			} else
				saveMathContainerProperties(ev.getDelay(), e.getDelay());
		} else if (e.getDelay() == null)
			e.setDelay(new org.sbml.libsbml.ASTNode(0));
		if (ev.isSetTrigger()) {
			if (e.getTrigger() == null)
				e.setTrigger(writeTrigger(ev.getTrigger()));
			else
				saveMathContainerProperties(ev.getTrigger(), e.getTrigger());
		}
		// synchronize event assignments

		for (EventAssignment ea : ev.getListOfEventAssignments()) {
			int contains = -1;
			for (int i = 0; i < e.getNumEventAssignments() && contains < 0; i++) {
				PluginEventAssignment libEA = e.getEventAssignment(i);
				if (libEA.getVariable().equals(ea.getVariable())
						&& equal(ea.getMath(), libEA.getMath()))
					contains = i;
			}
			if (contains < 0) {
				PluginEventAssignment pev = writeEventAssignment(ea,
						(PluginEvent) event);
				e.addEventAssignment(pev);
				plugin.notifySBaseAdded(pev);
			} else {
				PluginEventAssignment pev = e.getEventAssignment(contains);
				saveMathContainerProperties(ea, pev);
				plugin.notifySBaseChanged(pev);
			}
		}
		// remove unnecessary event assignments
		for (int i = e.getNumEventAssignments() - 1; i >= 0; i--) {
			PluginEventAssignment ea = e.getEventAssignment(i);
			boolean contains = false;
			for (int j = 0; j < ev.getNumEventAssignments() && !contains; j++) {
				EventAssignment eventA = ev.getEventAssignment(j);
				if (eventA.isSetVariable()
						&& eventA.getVariable().equals(ea.getVariable())
						&& equal(eventA.getMath(), ea.getMath()))
					contains = true;
			}
			if (!contains) {
				PluginEventAssignment pev = e.getEventAssignment(i);
				e.removeEventAssignment(i);
				plugin.notifySBaseDeleted(pev);
			}
		}
		plugin.notifySBaseChanged(e);
	}

	/**
	 * 
	 * @param kl
	 * @param kineticLaw
	 * @throws SBMLException
	 */
	private void saveKineticLawProperties(KineticLaw kl, Object kineticLaw)
			throws SBMLException {
		if (!(kineticLaw instanceof PluginKineticLaw))
			throw new IllegalArgumentException(
					"kineticLaw must be an instance of PluginKineticLaw.");
		PluginKineticLaw libKinLaw = (PluginKineticLaw) kineticLaw;
		// add or change parameters
		saveSBaseProperties(kl, libKinLaw);
		int para = 0;
		for (Parameter p : kl.getListOfParameters()) {
			PluginParameter libParam = libKinLaw.getParameter(para);
			para++;
			if (p.isSetUnits()
					&& !Unit.isUnitKind(p.getUnits(), p.getLevel(), p
							.getVersion())
					&& pluginModel.getUnitDefinition(p.getUnits()) == null) {
				PluginUnitDefinition ud = writeUnitDefinition(p
						.getUnitsInstance());
				pluginModel.addUnitDefinition(ud);
				plugin.notifySBaseAdded(ud);
			}
			if (libParam == null) {
				PluginParameter pp = writeParameter(p, libKinLaw);
				libKinLaw.addParameter(pp);
				plugin.notifySBaseAdded(pp);
				plugin.notifySBaseChanged(pluginModel.getListOfParameters());
			} else {
				saveParameterProperties(p, libParam);
				plugin.notifySBaseChanged(libParam);
				plugin.notifySBaseChanged(libKinLaw.getListOfParameters());
			}
		}
		// remove parameters
		for (int i = libKinLaw.getNumParameters() - 1; i >= 0; i--) {
			PluginParameter p = libKinLaw.getParameter(i);
			if (kl.getParameter(p.getId()) == null)
				plugin.notifySBaseDeleted(libKinLaw.getListOfParameters()
						.remove(i));
		}
		saveMathContainerProperties(kl, libKinLaw);
		plugin.notifySBaseChanged(libKinLaw);
	}

	/**
	 * 
	 * @param listOf
	 * @param ro
	 * @param kind
	 *            0 means reactant, 1 product, and 2 modifier.
	 * @throws SBMLException
	 */
	private void saveListOfProperties(
			ListOf<? extends SimpleSpeciesReference> listOf, PluginReaction ro,
			short kind) throws SBMLException {
		int i, contains;
		PluginListOf pluli;
		switch (kind) {
		case 0:
			pluli = ro.getListOfReactants();
			msg.logln("obtained list of reactants");
			break;
		case 1:
			pluli = ro.getListOfProducts();
			msg.logln("obtained list of products");
			break;
		default:
			pluli = ro.getListOfModifiers();
			msg.logln("obtained list of modifiers");
			break;
		}
		for (SimpleSpeciesReference sr : listOf) {
			contains = -1;
			for (i = 0; i < pluli.size() && contains < 0; i++) {
				if (sr.getSpecies().equals(
						((PluginSimpleSpeciesReference) pluli.get(i))
								.getSpecies()))
					contains = i;
			}
			if (contains < 0) {
				msg
						.logf(
								"couldn't find a reference to species %s -> inserting a new one\n",
								sr.getSpecies());
				if (sr instanceof SpeciesReference) {
					String type;
					switch (kind) {
					case 0:
						type = PluginSpeciesReference.REACTANT;
						break;
					case 1:
						type = PluginSpeciesReference.PRODUCT;
						break;
					default:
						type = PluginSpeciesReference.MODIFIER;
						break;
					}
					PluginSpeciesReference psr = writeSpeciesReference(
							(SpeciesReference) sr, ro, type);
					if (kind == 0)
						ro.addReactant(psr);
					else if (kind == 1)
						ro.addProduct(psr);
					plugin.notifySBaseAdded(psr);
					msg
							.logf(
									"inserted new SimpleSpeciesReference of type %s pointing to species %s\n",
									type, sr.getSpecies());
				} else {
					PluginModifierSpeciesReference pmsr = writeModifierSpeciesReference(
							(ModifierSpeciesReference) sr, ro, pluginModel);
					ro.addModifier(pmsr);
					plugin.notifySBaseAdded(pmsr);
					msg
							.logf(
									"inserted new ModifierSpeciesReference of pointing to species %s\n",
									sr.getSpecies());
				}
			} else {
				msg
						.logf(
								"identified reference to species %s now storing properties\n",
								sr.getSpecies());
				if (sr instanceof SpeciesReference) {
					PluginSpeciesReference psr = kind == 0 ? ro
							.getReactant(contains) : ro.getProduct(contains);
					saveSpeciesReferenceProperties((SpeciesReference) sr, psr);
					plugin.notifySBaseChanged(psr);
				} else if (sr instanceof ModifierSpeciesReference) {
					PluginModifierSpeciesReference pmsr = ro
							.getModifier(contains);
					msg.logln("obtained modifier species reference");
					saveModifierSpeciesReferenceProperties(
							(ModifierSpeciesReference) sr, pmsr);
					msg
							.logln("saved properties of this modifier species reference");
					plugin.notifySBaseChanged(pmsr);
				}
			}
		}
		msg.logln("saved all SimpleSpeciesReference properties");
		// remove unnecessary elements.
		for (i = pluli.size() - 1; i >= 0; i--) {
			PluginSimpleSpeciesReference rospref = (PluginSimpleSpeciesReference) pluli
					.get(i);
			boolean keep = false;
			for (SimpleSpeciesReference ssr : listOf)
				if (ssr.getSpecies().equals(rospref.getSpecies())) {
					keep = true;
					break;
				}
			if (!keep)
				plugin.notifySBaseDeleted(pluli.remove(i));
		}
		msg.logln("removed all unnecessary references");
	}

	/**
	 * 
	 * @param mc
	 * @param sbase
	 * @throws SBMLException
	 */
	private void saveMathContainerProperties(MathContainer mc, Object sbase)
			throws SBMLException {
		if (mc instanceof NamedSBase)
			saveSBaseProperties((NamedSBase) mc, sbase);
		else
			saveSBaseProperties(mc, sbase);
		if (sbase instanceof PluginConstraint) {
			PluginConstraint kl = (PluginConstraint) sbase;
			boolean equal = (kl.getMath() != null) && mc.isSetMath()
					&& equal(mc.getMath(), libsbml.parseFormula(kl.getMath()));
			if (mc.isSetMath() && !equal) {
				throw new SBMLException("Unable to set math of "
						+ mc.getClass().getSimpleName() + " in "
						+ kl.getClass().getName());
				// plugin.notifySBaseChanged(kl);
			}

		} else if (sbase instanceof PluginEventAssignment) {
			PluginEventAssignment kl = (PluginEventAssignment) sbase;
			boolean equal = (kl.getMath() != null) && mc.isSetMath()
					&& equal(mc.getMath(), kl.getMath());
			if (mc.isSetMath() && !equal) {
				kl.setMath(convert(mc.getMath()));
				plugin.notifySBaseChanged(kl);
			}

		} else if (sbase instanceof PluginFunctionDefinition) {
			PluginFunctionDefinition kl = (PluginFunctionDefinition) sbase;
			boolean equal = (kl.getMath() != null) && mc.isSetMath()
					&& equal(mc.getMath(), kl.getMath());
			if (mc.isSetMath() && !equal) {
				kl.setMath(convert(mc.getMath()));
				plugin.notifySBaseChanged(kl);
			}

		} else if (sbase instanceof PluginInitialAssignment) {
			PluginInitialAssignment kl = (PluginInitialAssignment) sbase;
			boolean equal = (kl.getMath() != null) && mc.isSetMath()
					&& equal(mc.getMath(), libsbml.parseFormula(kl.getMath()));
			if (mc.isSetMath() && !equal) {
				kl.setMath(libsbml.formulaToString(convert(mc.getMath())));
				plugin.notifySBaseChanged(kl);
			}

		} else if (sbase instanceof PluginKineticLaw) {
			PluginKineticLaw kl = (PluginKineticLaw) sbase;
			boolean equal = kl.getMath() != null && mc.isSetMath()
					&& equal(mc.getMath(), kl.getMath());
			if (mc.isSetMath() && !equal) {
				kl.setMath(convert(mc.getMath()));
				plugin.notifySBaseChanged(kl);
			}

		} else if (sbase instanceof PluginRule) {
			PluginRule kl = (PluginRule) sbase;
			boolean equal = (kl.getMath() != null) && mc.isSetMath()
					&& equal(mc.getMath(), kl.getMath());
			if (mc.isSetMath() && !equal) {
				kl.setMath(convert(mc.getMath()));
				plugin.notifySBaseChanged(kl);
			}
		}
		// else if (sbase instanceof PluginStoichiometryMath) {
		// PluginStoichiometryMath kl = (PluginStoichiometryMath) sbase;
		// boolean equal = kl.isSetMath() && mc.isSetMath()
		// && equal(mc.getMath(), kl.getMath());
		// if (mc.isSetMath()
		// && !equal
		// && kl.setMath(convert(mc.getMath())) !=
		// libsbmlConstants.LIBSBML_OPERATION_SUCCESS)
		// throw new SBMLException("Unable to set math of "
		// + mc.getClass().getSimpleName() + " in "
		// + kl.getClass().getName());
		// } else if (sbase instanceof PluginTrigger) {
		// PluginTrigger kl = (PluginTrigger) sbase;
		// boolean equal = kl.isSetMath() && mc.isSetMath()
		// && equal(mc.getMath(), kl.getMath());
		// if (mc.isSetMath()
		// && !equal
		// && kl.setMath(convert(mc.getMath())) !=
		// libsbmlConstants.LIBSBML_OPERATION_SUCCESS)
		// throw new SBMLException("Unable to set math of "
		// + mc.getClass().getSimpleName() + " in "
		// + kl.getClass().getName());
		// }
	}

	/**
	 * 
	 * @param mh
	 * @param modelHistory
	 */
	private void saveModelHistoryProperties(ModelHistory mh, Object modelHistory) {
		// TODO Auto-generated method stub
	}

	/**
	 * 
	 * @param modifierSpeciesReference
	 * @param msr
	 */
	private void saveModifierSpeciesReferenceProperties(
			ModifierSpeciesReference modifierSpeciesReference, Object msr) {
		if (!(msr instanceof PluginModifierSpeciesReference))
			throw new IllegalArgumentException("modifierSpeciesReference"
					+ error + "PluginModifierSpeciesReference.");
		PluginModifierSpeciesReference pmsr = (PluginModifierSpeciesReference) msr;
		saveNamedSBaseProperties(modifierSpeciesReference, pmsr);
		msg.logln("saved NamedSBase properties");
		if (modifierSpeciesReference.isSetSBOTerm()) {
			String type = SBO.convertSBO2Alias(modifierSpeciesReference
					.getSBOTerm());
			if (type.length() > 0) {
				msg.logf("setting alias for SBO %d to %s",
						modifierSpeciesReference.getSBOTerm(), type);
				pmsr.setModificationType(type);
			} else {
				pmsr.setModificationType(PluginReactionSymbolType.MODULATION);
				msg.logf("no valid type found. Setting alias for SBO %d to %s",
						modifierSpeciesReference.getSBOTerm(),
						PluginReactionSymbolType.MODULATION);
			}
		}
		// if (modifierSpeciesReference.isSetSpecies() &&
		// !modifierSpeciesReference.getSpecies().equals(pmsr.getSpecies()))
		plugin.notifySBaseChanged(pmsr);
	}

	/**
	 * 
	 * @param nsb
	 * @param sb
	 */
	private void saveNamedSBaseProperties(NamedSBase nsb, Object sb) {
		saveSBaseProperties(nsb, sb);
		if (sb instanceof PluginCompartmentType) {
			PluginCompartmentType pt = (PluginCompartmentType) sb;
			// if (nsb.isSetId() && !pt.getId().equals(nsb.getId()))
			// pt.setId(nsb.getId());
			if (nsb.isSetName() && !pt.getName().equals(nsb.getName())) {
				pt.setName(nsb.getName());
				plugin.notifySBaseChanged(pt);
			}
		} else if (sb instanceof PluginEvent) {
			PluginEvent pt = (PluginEvent) sb;
			// if (nsb.isSetId() && !pt.getId().equals(nsb.getId()))
			// pt.setId(nsb.getId());
			if (nsb.isSetName() && !pt.getName().equals(nsb.getName())) {
				pt.setName(nsb.getName());
				plugin.notifySBaseChanged(pt);
			}
		} else if (sb instanceof PluginModel) {
			PluginModel pt = (PluginModel) sb;
			// if (nsb.isSetId() && !pt.getId().equals(nsb.getId()))
			// pt.setId(nsb.getId());
			if (nsb.isSetName() && !pt.getName().equals(nsb.getName())) {
				pt.setName(nsb.getName());
				plugin.notifySBaseChanged(pt);
			}
		} else if (sb instanceof PluginReaction) {
			PluginReaction pt = (PluginReaction) sb;
			// if (nsb.isSetId() && !pt.getId().equals(nsb.getId()))
			// pt.setId(nsb.getId());
			if (nsb.isSetName() && !pt.getName().equals(nsb.getName())) {
				pt.setName(nsb.getName());
				plugin.notifySBaseChanged(pt);
			}
		} else if (sb instanceof PluginSimpleSpeciesReference) {
			// PluginSimpleSpeciesReference pt = (PluginSimpleSpeciesReference)
			// sb;
			// if (nsb.isSetId() && !pt.getId().equals(nsb.getId()))
			// pt.setId(nsb.getId());
			// if (nsb.isSetName() && !pt.getName().equals(nsb.getName()))
			// pt.setName(nsb.getName());
			if (sb instanceof PluginModifierSpeciesReference) {
			} else if (sb instanceof PluginSpeciesReference) {
			}
		} else if (sb instanceof PluginSpeciesType) {
			PluginSpeciesType pt = (PluginSpeciesType) sb;
			// if (nsb.isSetId() && !pt.getId().equals(nsb.getId()))
			// pt.setId(nsb.getId());
			if (nsb.isSetName() && !pt.getName().equals(nsb.getName())) {
				pt.setName(nsb.getName());
				plugin.notifySBaseChanged(pt);
			}
		} else if (sb instanceof PluginCompartment) {
			PluginCompartment pt = (PluginCompartment) sb;
			// if (nsb.isSetId() && !pt.getId().equals(nsb.getId()))
			// pt.setId(nsb.getId());
			if (nsb.isSetName() && !pt.getName().equals(nsb.getName())) {
				pt.setName(nsb.getName());
				plugin.notifySBaseChanged(pt);
			}
		} else if (sb instanceof PluginParameter) {
			PluginParameter pt = (PluginParameter) sb;
			if (nsb.isSetId() && !pt.getId().equals(nsb.getId())) {
				pt.setId(nsb.getId());
				plugin.notifySBaseChanged(pt);
			}
			if (nsb.isSetName() && !pt.getName().equals(nsb.getName())) {
				pt.setName(nsb.getName());
				plugin.notifySBaseChanged(pt);
			}
		} else if (sb instanceof PluginSpecies) {
			// PluginSpecies pt = (PluginSpecies) sb;
			// if (nsb.isSetId() && !pt.getId().equals(nsb.getId()))
			// pt.setId(nsb.getId());
			// if (nsb.isSetName() && !pt.getName().equals(nsb.getName()))
			// pt.setName(nsb.getName());
		} else if (sb instanceof PluginUnitDefinition) {
			PluginUnitDefinition pt = (PluginUnitDefinition) sb;
			// if (nsb.isSetId() && !pt.getId().equals(nsb.getId()))
			// pt.setId(nsb.getId());
			if (nsb.isSetName() && !pt.getName().equals(nsb.getName())) {
				pt.setName(nsb.getName());
				plugin.notifySBaseChanged(pt);
			}
		} else if (sb instanceof PluginFunctionDefinition) {

		}
	}

	/**
	 * 
	 * @param p
	 * @param parameter
	 */
	private void saveParameterProperties(Parameter p, Object parameter) {
		if (!(parameter instanceof PluginParameter))
			throw new IllegalArgumentException("parameter" + error
					+ "PluginParameter.");
		PluginParameter po = (PluginParameter) parameter;
		saveNamedSBaseProperties(p, po);
		boolean changed = false;
		if (p.isSetValue() && p.getValue() != po.getValue()) {
			po.setValue(p.getValue());
			changed = true;
		}
		if (p.getConstant() != po.getConstant()) {
			po.setConstant(p.getConstant());
			changed = true;
		}
		if (p.isSetUnits() && !p.getUnits().equals(po.getUnits())) {
			po.setUnits(p.getUnits());
			changed = true;
		}
		if (changed)
			plugin.notifySBaseChanged(po);
	}

	/**
	 * 
	 * @param r
	 * @param reaction
	 * @throws SBMLException
	 */
	private void saveReactionProperties(Reaction r, Object reaction)
			throws SBMLException {
		if (!(reaction instanceof PluginReaction))
			throw new IllegalArgumentException("reaction" + error
					+ "PluginReaction.");
		PluginReaction ro = (PluginReaction) reaction;
		saveNamedSBaseProperties(r, ro);
		msg.logf("saved NamedSBase properties for reaction %s\n", r.getId());
		boolean changed = false;
		if (r.getFast() != ro.getFast()) {
			ro.setFast(r.getFast());
			changed = true;
		}
		if (r.getReversible() != ro.getReversible()) {
			ro.setReversible(r.getReversible());
			changed = true;
		}
		msg.logln("saved general reaction properties");
		saveListOfProperties(r.getListOfReactants(), ro, (short) 0);
		msg.logln("saved list of reactants");
		saveListOfProperties(r.getListOfProducts(), ro, (short) 1);
		msg.logln("saved list of products");
		saveListOfProperties(r.getListOfModifiers(), ro, (short) 2);
		msg.logln("saved list of modifiers");
		if (r.isSetKineticLaw()) {
			if (ro.getKineticLaw() == null) {
				PluginKineticLaw plukin = writeKineticLaw(r.getKineticLaw(),
						(PluginReaction) reaction);
				ro.setKineticLaw(plukin);
				plugin.notifySBaseAdded(plukin);
			} else {
				PluginKineticLaw plukin = ro.getKineticLaw();
				saveKineticLawProperties(r.getKineticLaw(), plukin);
				plugin.notifySBaseChanged(plukin);
			}
			msg.logln("saved kinetic law properties");
		} else if (ro.getKineticLaw() != null) {
			PluginKineticLaw plukin = ro.getKineticLaw();
			ro.setKineticLaw(null);
			plugin.notifySBaseDeleted(plukin);
			msg.logln("wrote new kinetic law");
		}
		if (changed)
			plugin.notifySBaseChanged(ro);
	}

	/**
	 * 
	 * @param s
	 * @param sb
	 */
	private void saveSBaseProperties(SBase s, Object sb) {
		if (!(sb instanceof PluginSBase))
			throw new IllegalArgumentException("sb" + error + "PluginSBase");
		PluginSBase plusbas = (PluginSBase) sb;
		if (!plusbas.getNotesString().equals(s.getNotesString())) {
			plusbas.setNotes(s.getNotesString());
			plugin.notifySBaseChanged(plusbas);
		}
	}

	/**
	 * 
	 * @param s
	 * @param species
	 */
	private void saveSpeciesProperties(Species s, Object species) {
		if (!(species instanceof PluginSpecies))
			throw new IllegalArgumentException("species" + error
					+ "PluginSpecies");
		PluginSpecies spec = (PluginSpecies) species;
		saveNamedSBaseProperties(s, spec);
		boolean changed = false;
		if (s.isSetSpeciesType()
				&& !s.getSpeciesType().equals(spec.getSpeciesType())) {
			spec.setSpeciesType(s.getSpeciesType());
			changed = true;
		}
		if (s.isSetCompartment()
				&& !s.getCompartment().equals(spec.getCompartment())) {
			spec.setCompartment(s.getCompartment());
			changed = true;
		}
		if (s.isSetInitialAmount()) {
			if (!spec.isSetInitialAmount()
					|| s.getInitialAmount() != spec.getInitialAmount()) {
				spec.setInitialAmount(s.getInitialAmount());
				changed = true;
			}
		} else if (s.isSetInitialConcentration())
			if (!spec.isSetInitialConcentration()
					|| s.getInitialConcentration() != spec
							.getInitialConcentration()) {
				spec.setInitialConcentration(s.getInitialConcentration());
				changed = true;
			}
		if (s.isSetSubstanceUnits()
				&& !s.getSubstanceUnits().equals(spec.getSubstanceUnits())) {
			spec.setSubstanceUnits(s.getSubstanceUnits());
			changed = true;
		}
		if (s.getHasOnlySubstanceUnits() != spec.getHasOnlySubstanceUnits()) {
			spec.setHasOnlySubstanceUnits(s.getHasOnlySubstanceUnits());
			changed = true;
		}
		if (s.getBoundaryCondition() != spec.getBoundaryCondition()) {
			spec.setBoundaryCondition(spec.getBoundaryCondition());
			changed = true;
		}
		if (s.isSetCharge() && s.getCharge() != spec.getCharge()) {
			spec.setCharge(s.getCharge());
			changed = true;
		}
		if (s.getConstant() != spec.getConstant()) {
			spec.setConstant(s.getConstant());
			changed = true;
		}
		if (s.isSetSBOTerm()) {
			String type = SBO.convertSBO2Alias(s.getSBOTerm());
			if (type.length() > 0) {
				spec.getSpeciesAlias(0).setType(type);
				changed = true;
			}
		}
		if (changed)
			plugin.notifySBaseChanged(spec);
	}

	/**
	 * 
	 * @param sr
	 * @param specRef
	 * @throws SBMLException
	 */
	private void saveSpeciesReferenceProperties(SpeciesReference sr,
			Object specRef) throws SBMLException {
		if (!(specRef instanceof PluginSpeciesReference))
			throw new IllegalArgumentException(
					"specRef must be an instance of PluginSpeciesReference.");
		PluginSpeciesReference sp = (PluginSpeciesReference) specRef;
		saveNamedSBaseProperties(sr, sp);
		// if (sr.isSetSpecies() && !sr.getSpecies().equals(sp.getSpecies()))
		// sp.setSpecies(sr.getSpecies());
		if (sr.isSetStoichiometryMath()) {
			if (sp.getStoichiometryMath() != null
					&& !equal(sr.getStoichiometryMath().getMath(), sp
							.getStoichiometryMath().getMath()))
				saveMathContainerProperties(sr.getStoichiometryMath(), sp
						.getStoichiometryMath());
			else {
				sp.setStoichiometryMath(writeStoichoimetryMath(sr
						.getStoichiometryMath()));
			}
		} else
			sp.setStoichiometry(sr.getStoichiometry());
		plugin.notifySBaseChanged(sp);
	}

	/**
	 * 
	 * @param ud
	 * @param libU
	 */
	private void saveUnitDefinitionProperties(UnitDefinition ud,
			PluginUnitDefinition libU) {
		saveSBaseProperties(ud, libU);
		for (int i = libU.getNumUnits() - 1; i >= 0; i--) {
			PluginUnit pu = libU.getUnit(i);
			libU.removeUnit(i);
			plugin.notifySBaseDeleted(pu);
		}
		for (Unit u : ud.getListOfUnits()) {
			PluginUnit unit = writeUnit(u, libU);
			libU.addUnit(unit);
			plugin.notifySBaseAdded(unit);
		}
		plugin.notifySBaseChanged(libU);
		plugin.notifySBaseChanged(pluginModel.getListOfUnitDefinitions());
	}

	/**
	 * 
	 * @param compartment
	 * @return
	 */
	private PluginCompartment writeCompartment(Compartment compartment) {
		PluginCompartment c = new PluginCompartment(
				PluginCompartmentSymbolType.SQUARE);
		if (compartment.isSetUnits()
				&& !Unit.isUnitKind(compartment.getUnits(), compartment
						.getLevel(), compartment.getVersion())
				&& pluginModel.getUnitDefinition(compartment.getUnits()) == null) {
			PluginUnitDefinition ud = writeUnitDefinition(compartment
					.getUnitsInstance());
			pluginModel.addUnitDefinition(ud);
			plugin.notifySBaseAdded(ud);
		}
		if (compartment.isSetCompartmentType()
				&& pluginModel.getCompartmentType(compartment
						.getCompartmentType()) == null) {
			PluginCompartmentType ct = writeCompartmentType(compartment
					.getCompartmentTypeInstance());
			pluginModel.addCompartmentType(ct);
			plugin.notifySBaseAdded(ct);
		}
		saveCompartmentProperties(compartment, c);
		return c;
	}

	/**
	 * 
	 * @param compartmentType
	 * @return
	 */
	private PluginCompartmentType writeCompartmentType(
			CompartmentType compartmentType) {
		PluginCompartmentType ct = new PluginCompartmentType(compartmentType
				.getId());
		saveNamedSBaseProperties(compartmentType, ct);
		return ct;
	}

	/**
	 * 
	 * @param constraint
	 * @return
	 */
	private PluginConstraint writeConstraint(Constraint constraint) {
		PluginConstraint c = new PluginConstraint(libsbml
				.formulaToString(convert(constraint.getMath())));
		saveSBaseProperties(constraint, c);
		if (constraint.isSetMessage())
			c.setMessage(constraint.getMessage());
		return c;
	}

	/**
	 * 
	 * @param cvt
	 * @return
	 */
	private Object writeCVTerm(CVTerm cvt) {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * 
	 * @param delay
	 * @return
	 */
	private org.sbml.libsbml.ASTNode writeDelay(Delay delay) {
		return convert(delay.getMath());
	}

	/**
	 * 
	 * @param event
	 * @return
	 * @throws SBMLException
	 */
	private PluginEvent writeEvent(Event event) throws SBMLException {
		PluginEvent e = new PluginEvent(event.getId());
		saveEventProperties(event, e);
		return e;
	}

	/**
	 * 
	 * @param eventAssignment
	 * @param ev
	 * @return
	 * @throws SBMLException
	 */
	private PluginEventAssignment writeEventAssignment(
			EventAssignment eventAssignment, Object... ev) throws SBMLException {
		if (ev.length != 1 || !(ev[0] instanceof PluginEvent))
			throw new IllegalArgumentException(
					"parent must be of type PluginEvent!");
		PluginEventAssignment ea = new PluginEventAssignment(
				(PluginEvent) ev[0]);
		saveMathContainerProperties(eventAssignment, ea);
		if (eventAssignment.isSetVariable())
			ea.setVariable(eventAssignment.getVariable());
		return ea;
	}

	/**
	 * 
	 * @param functionDefinition
	 * @return
	 * @throws SBMLException
	 */
	private PluginFunctionDefinition writeFunctionDefinition(
			FunctionDefinition functionDefinition) throws SBMLException {
		PluginFunctionDefinition fd = new PluginFunctionDefinition(
				functionDefinition.getId());
		saveNamedSBaseProperties(functionDefinition, fd);
		saveMathContainerProperties(functionDefinition, fd);
		return fd;
	}

	/**
	 * 
	 * @param initialAssignment
	 * @return
	 * @throws SBMLException
	 */
	private PluginInitialAssignment writeInitialAssignment(
			InitialAssignment initialAssignment) throws SBMLException {
		PluginInitialAssignment ia = new PluginInitialAssignment(
				initialAssignment.getSymbol());
		saveSBaseProperties(initialAssignment, ia);
		saveMathContainerProperties(initialAssignment, ia);
		return ia;
	}

	/**
	 * 
	 * @param kineticLaw
	 * @param parent
	 * @return
	 * @throws SBMLException
	 */
	private PluginKineticLaw writeKineticLaw(KineticLaw kineticLaw,
			Object... parent) throws SBMLException {
		if (parent.length != 1 || !(parent[0] instanceof PluginReaction))
			throw new IllegalArgumentException("parent" + error
					+ "PluginReaction");
		PluginKineticLaw k = new PluginKineticLaw((PluginReaction) parent[0]);
		saveKineticLawProperties(kineticLaw, k);
		return k;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.SBMLWriter#writeModel(org.sbml.Model)
	 */
	public PluginModel writeModel(Model model) throws SBMLException {
		LibSBMLWriter writer = new LibSBMLWriter();
		pluginModel = new PluginModel((org.sbml.libsbml.Model) writer
				.writeModel(model));
		saveChanges(model, pluginModel);
		return pluginModel;
	}

	/**
	 * 
	 * @param modifierSpeciesReference
	 * @param parent
	 * @return
	 */
	private PluginModifierSpeciesReference writeModifierSpeciesReference(
			ModifierSpeciesReference modifierSpeciesReference, Object... parent) {
		if (parent.length != 2 || !(parent[0] instanceof PluginReaction)
				|| !(parent[1] instanceof PluginModel))
			throw new IllegalArgumentException(
					"a PluginReaction and a PluginModel must be provided");
		PluginReaction pluReac = (PluginReaction) parent[0];
		PluginModel pluMod = (PluginModel) parent[1];
		String modificationType;
		if (modifierSpeciesReference.isSetSBOTerm())
			modificationType = SBO.convertSBO2Alias(modifierSpeciesReference
					.getSBOTerm());
		else
			modificationType = PluginReactionSymbolType.MODULATION;
		PluginModifierSpeciesReference m = new PluginModifierSpeciesReference(
				pluReac, new PluginSpeciesAlias(pluMod
						.getSpecies(modifierSpeciesReference.getSpecies()),
						modificationType));
		saveModifierSpeciesReferenceProperties(modifierSpeciesReference, m);
		return m;
	}

	/**
	 * 
	 * @param parameter
	 * @param parent
	 * @return
	 */
	private PluginParameter writeParameter(Parameter parameter,
			Object... parent) {
		if (parent.length != 1
				|| !((parent[0] instanceof PluginKineticLaw) || (parent[0] instanceof PluginModel)))
			throw new IllegalArgumentException("parent" + error
					+ "PluginKineticLaw or PluginModel");
		PluginParameter p;
		if (parent[0] instanceof PluginKineticLaw)
			p = new PluginParameter((PluginKineticLaw) parent[0]);
		else
			p = new PluginParameter((PluginModel) parent[0]);
		saveParameterProperties(parameter, p);
		return p;
	}

	/**
	 * 
	 * @param reaction
	 * @return
	 * @throws SBMLException
	 */
	private PluginReaction writeReaction(Reaction reaction)
			throws SBMLException {
		PluginReaction r = new PluginReaction();
		saveReactionProperties(reaction, r);
		return r;
	}

	/**
	 * 
	 * @param rule
	 * @param parent
	 * @return
	 */
	private PluginRule writeRule(Rule rule, Object... parent) {
		if (parent.length != 1 || !(parent[0] instanceof PluginModel))
			throw new IllegalArgumentException(
					"parent must be of type PluginModel!");
		PluginRule r;
		if (rule.isAlgebraic()) {
			r = new PluginAlgebraicRule((PluginModel) parent[0]);
		} else {
			if (rule.isAssignment()) {
				r = new PluginAssignmentRule((PluginModel) parent[0]);
				if (((AssignmentRule) rule).isSetVariable())
					((PluginAssignmentRule) r)
							.setVariable(((AssignmentRule) rule).getVariable());
			} else {
				r = new PluginRateRule((PluginModel) parent[0]);
				if (((RateRule) rule).isSetVariable())
					((PluginRateRule) r).setVariable(((RateRule) rule)
							.getVariable());
			}
		}
		if (rule.isSetMath())
			r.setMath(convert(rule.getMath()));
		saveSBaseProperties(rule, r);
		if (rule.getFormula() != null)
			r.setFormula(rule.getFormula());
		return r;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.jsbml.SBMLWriter#writeSBML(java.lang.Object,
	 * java.lang.String)
	 */
	public boolean writeSBML(Object sbmlModel, String filename)
			throws IOException, SBMLException {
		return writeSBML(sbmlModel, filename, null, null);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.jsbml.SBMLWriter#writeSBML(java.lang.Object,
	 * java.lang.String, java.lang.String, java.lang.String)
	 */
	public boolean writeSBML(Object sbmlModel, String filename,
			String programName, String versionNumber) throws SBMLException,
			IOException {
		if (!(sbmlModel instanceof PluginSBase))
			throw new IllegalArgumentException("sbmlModel" + error
					+ "PluginSBase");
		PluginSBase sbase = (PluginSBase) sbmlModel;
		BufferedWriter bw = new BufferedWriter(new FileWriter(filename));
		if (programName != null || versionNumber != null) {
			bw.append("<!-- ");
			if (programName != null) {
				bw.append("created by ");
				bw.append(programName);
				if (versionNumber != null)
					bw.append(' ');
			}
			if (versionNumber != null) {
				bw.append("version ");
				bw.append(versionNumber);
			}
			bw.append(" -->");
			bw.newLine();
		}
		bw.append(sbase.toSBML());
		bw.close();
		return true;
	}

	/**
	 * 
	 * @param species
	 * @return
	 */
	private PluginSpecies writeSpecies(Species species) {
		String spectype = SBO.convertSBO2Alias(species.getSBOTerm());
		if (spectype == null || spectype.length() == 0)
			spectype = PluginSpeciesSymbolType.SIMPLE_MOLECULE;
		PluginSpecies s = new PluginSpecies(spectype, species.getName());
		saveSpeciesProperties(species, s);
		return s;
	}

	/**
	 * 
	 * @param speciesReference
	 * @param parent
	 * @return
	 * @throws SBMLException
	 */
	private PluginSpeciesReference writeSpeciesReference(
			SpeciesReference speciesReference, Object... parent)
			throws SBMLException {
		if (parent.length != 2 || !(parent[0] instanceof PluginReaction)
				|| !(parent[1] instanceof String))
			throw new IllegalArgumentException("parent" + error
					+ "PluginReaction and type (String) must be given");
		PluginSpeciesReference sr = new PluginSpeciesReference(
				(PluginReaction) parent[0], new PluginSpeciesAlias(pluginModel
						.getSpecies(speciesReference.getSpecies()), parent[1]
						.toString()));
		saveSpeciesReferenceProperties(speciesReference, sr);
		return sr;
	}

	/**
	 * 
	 * @param speciesType
	 * @return
	 */
	private PluginSpeciesType writeSpeciesType(SpeciesType speciesType) {
		PluginSpeciesType st = new PluginSpeciesType(speciesType.getId());
		saveNamedSBaseProperties(speciesType, st);
		return st;
	}

	/**
	 * 
	 * @param st
	 * @return
	 */
	private org.sbml.libsbml.StoichiometryMath writeStoichoimetryMath(
			StoichiometryMath st) {
		// org.sbml.libsbml.StoichiometryMath sm = new
		// org.sbml.libsbml.StoichiometryMath();
		// if (st.isSetMetaId())
		// sm.setMetaId(st.getMetaId());
		// if (st.isSetAnnotation())
		// sm.setAnnotation(st.getAnnotationString());
		// if (st.isSetNotes())
		// sm.setNotes(st.getNotesString());
		// if (st.isSetMath())
		// sm.setMath(convert(st.getMath()));
		// return sm;
		return null;
	}

	/**
	 * 
	 * @param trigger
	 * @return
	 */
	private org.sbml.libsbml.ASTNode writeTrigger(Trigger trigger) {
		return convert(trigger.getMath());
	}

	/**
	 * 
	 * @param unit
	 * @param parent
	 * @return
	 */
	private PluginUnit writeUnit(Unit unit, Object... parent) {
		if (parent.length != 1 || !(parent[0] instanceof PluginUnitDefinition))
			throw new IllegalArgumentException("parent" + error
					+ "PluginUnitDefinition");
		PluginUnit u = new PluginUnit((PluginUnitDefinition) parent[0]);
		saveSBaseProperties(unit, u);
		switch (unit.getKind()) {
		case AMPERE:
			u.setKind(libsbmlConstants.UNIT_KIND_AMPERE);
			break;
		case BECQUEREL:
			u.setKind(libsbmlConstants.UNIT_KIND_BECQUEREL);
			break;
		case CANDELA:
			u.setKind(libsbmlConstants.UNIT_KIND_CANDELA);
			break;
		case CELSIUS:
			u.setKind(libsbmlConstants.UNIT_KIND_CELSIUS);
			break;
		case COULOMB:
			u.setKind(libsbmlConstants.UNIT_KIND_COULOMB);
			break;
		case DIMENSIONLESS:
			u.setKind(libsbmlConstants.UNIT_KIND_DIMENSIONLESS);
			break;
		case FARAD:
			u.setKind(libsbmlConstants.UNIT_KIND_FARAD);
			break;
		case GRAM:
			u.setKind(libsbmlConstants.UNIT_KIND_GRAM);
			break;
		case GRAY:
			u.setKind(libsbmlConstants.UNIT_KIND_GRAY);
			break;
		case HENRY:
			u.setKind(libsbmlConstants.UNIT_KIND_HENRY);
			break;
		case HERTZ:
			u.setKind(libsbmlConstants.UNIT_KIND_HERTZ);
			break;
		case INVALID:
			u.setKind(libsbmlConstants.UNIT_KIND_INVALID);
			break;
		case ITEM:
			u.setKind(libsbmlConstants.UNIT_KIND_ITEM);
			break;
		case JOULE:
			u.setKind(libsbmlConstants.UNIT_KIND_JOULE);
			break;
		case KATAL:
			u.setKind(libsbmlConstants.UNIT_KIND_KATAL);
			break;
		case KELVIN:
			u.setKind(libsbmlConstants.UNIT_KIND_KELVIN);
			break;
		case KILOGRAM:
			u.setKind(libsbmlConstants.UNIT_KIND_KILOGRAM);
			break;
		case LITER:
			u.setKind(libsbmlConstants.UNIT_KIND_LITER);
			break;
		case LITRE:
			u.setKind(libsbmlConstants.UNIT_KIND_LITRE);
			break;
		case LUMEN:
			u.setKind(libsbmlConstants.UNIT_KIND_LUMEN);
			break;
		case LUX:
			u.setKind(libsbmlConstants.UNIT_KIND_LUX);
			break;
		case METER:
			u.setKind(libsbmlConstants.UNIT_KIND_METER);
			break;
		case METRE:
			u.setKind(libsbmlConstants.UNIT_KIND_METRE);
			break;
		case MOLE:
			u.setKind(libsbmlConstants.UNIT_KIND_MOLE);
			break;
		case NEWTON:
			u.setKind(libsbmlConstants.UNIT_KIND_NEWTON);
			break;
		case OHM:
			u.setKind(libsbmlConstants.UNIT_KIND_OHM);
			break;
		case PASCAL:
			u.setKind(libsbmlConstants.UNIT_KIND_PASCAL);
			break;
		case RADIAN:
			u.setKind(libsbmlConstants.UNIT_KIND_RADIAN);
			break;
		case SECOND:
			u.setKind(libsbmlConstants.UNIT_KIND_SECOND);
			break;
		case SIEMENS:
			u.setKind(libsbmlConstants.UNIT_KIND_SIEMENS);
			break;
		case SIEVERT:
			u.setKind(libsbmlConstants.UNIT_KIND_SIEVERT);
			break;
		case STERADIAN:
			u.setKind(libsbmlConstants.UNIT_KIND_STERADIAN);
			break;
		case TESLA:
			u.setKind(libsbmlConstants.UNIT_KIND_TESLA);
			break;
		case VOLT:
			u.setKind(libsbmlConstants.UNIT_KIND_VOLT);
			break;
		case WATT:
			u.setKind(libsbmlConstants.UNIT_KIND_WATT);
			break;
		case WEBER:
			u.setKind(libsbmlConstants.UNIT_KIND_WEBER);
			break;
		}
		u.setExponent(unit.getExponent());
		u.setMultiplier(unit.getMultiplier());
		u.setOffset(unit.getOffset());
		u.setScale(unit.getScale());
		return u;
	}

	/**
	 * 
	 * @param unitDefinition
	 * @return
	 */
	private PluginUnitDefinition writeUnitDefinition(
			UnitDefinition unitDefinition) {
		PluginUnitDefinition ud = new PluginUnitDefinition(unitDefinition
				.getId());
		saveNamedSBaseProperties(unitDefinition, ud);
		for (Unit u : unitDefinition.getListOfUnits()) {
			PluginUnit unit = writeUnit(u, ud);
			ud.addUnit(unit);
			plugin.notifySBaseAdded(unit);
		}
		return ud;
	}
}
