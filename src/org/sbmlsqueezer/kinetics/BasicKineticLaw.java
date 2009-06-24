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
package org.sbmlsqueezer.kinetics;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

import jp.sbi.celldesigner.plugin.PluginKineticLaw;
import jp.sbi.celldesigner.plugin.PluginModel;
import jp.sbi.celldesigner.plugin.PluginReaction;
import jp.sbi.celldesigner.plugin.PluginSpecies;
import jp.sbi.celldesigner.plugin.PluginSpeciesReference;

import org.sbml.libsbml.ASTNode;
import org.sbml.libsbml.libsbmlConstants;
import org.sbmlsqueezer.io.LaTeXExport;

/**
 * An abstract super class of specialized kinetic laws.
 * 
 * @since 1.0
 * @version
 * @author <a href="mailto:andreas.draeger@uni-tuebingen.de">Andreas
 *         Dr&auml;ger</a>
 * @author <a href="mailto:Nadine.hassis@gmail.com">Nadine Hassis</a>
 * @author <a href="mailto:hannes.borch@googlemail.com">Hannes Borch</a>
 * @date Aug 1, 2007
 */
public abstract class BasicKineticLaw extends PluginKineticLaw implements
		libsbmlConstants {

	/**
	 * 
	 */
	protected static final Character underscore = Character.valueOf('_');

	/**
	 * Takes the given StringBuffer as input and appends every further Object to
	 * it.
	 * 
	 * @param k
	 * @param things
	 * @return
	 */
	public static final StringBuffer append(StringBuffer k, Object... things) {
		for (Object t : things)
			k.append(t);
		return k;
	}

	/**
	 * 
	 * @param sb
	 * @return
	 */
	public static final StringBuffer brackets(Object sb) {
		return concat(Character.valueOf('('), sb, Character.valueOf(')'));
	}

	/**
	 * Clones an abstract syntax tree.
	 * 
	 * @param ast
	 * @return
	 */
	public static final ASTNode clone(ASTNode ast) {
		ASTNode copy = new ASTNode();
		copy.setType(ast.getType());
		if (ast.isConstant() || ast.isInteger() || ast.isNumber()
				|| ast.isReal())
			copy.setValue(ast.getReal());
		else if (ast.isName())
			copy.setName(new String(ast.getName()));
		for (long i = 0; i < ast.getNumChildren(); i++)
			copy.addChild(clone(ast.getChild(i)));
		return copy;
	}

	/**
	 * This method concatenates two or more object strings into a new
	 * stringbuffer.
	 * 
	 * @param buffers
	 * @return
	 */
	public static final StringBuffer concat(Object... buffers) {
		StringBuffer res = new StringBuffer();
		for (Object buffer : buffers)
			res.append(buffer.toString());
		return res;
	}

	/**
	 * Returns the difference of the given elements as StringBuffer.
	 * 
	 * @param subtrahents
	 * @return
	 */
	public static final StringBuffer diff(Object... subtrahents) {
		if (subtrahents.length == 1)
			return brackets(concat(Character.valueOf('-'), subtrahents));
		return brackets(arith('-', subtrahents));
	}

	/**
	 * Returns a fraction with the given elements as numerator and denominator.
	 * 
	 * @param numerator
	 * @param denominator
	 * @return
	 */
	public static final StringBuffer frac(Object numerator, Object denominator) {
		return brackets(arith('/', numerator,
				containsArith(denominator) ? brackets(denominator)
						: denominator));
	}

	/**
	 * Tests whether the String representation of the given object contains any
	 * arithmetic symbols and if the given object is already sorrounded by
	 * brackets.
	 * 
	 * @param something
	 * @return True if either brackets are set around the given object or the
	 *         object does not contain any symbols such as +, -, *, /.
	 */
	private static boolean containsArith(Object something) {
		boolean arith = false;
		String d = something.toString();
		if (d.length() > 0) {
			char c;
			for (int i = 0; (i < d.length()) && !arith; i++) {
				c = d.charAt(i);
				arith = ((c == '+') || (c == '-') || (c == '*') || (c == '/'));
			}
		}
		return arith;
	}

	/**
	 * Creates and returns a list of molecule types accepted as an enzyme by
	 * default. These are: <ul type="disk"> <li>ANTISENSE_RNA</li> <li>
	 * SIMPLE_MOLECULE</li> <li>UNKNOWN</li> <li>COMPLEX</li> <li>TRUNCATED</li>
	 * <li>GENERIC</li> <li>RNA</li> <li>RECEPTOR</li> </ul>
	 * 
	 * @return
	 */
	public static final List<String> getDefaultListOfPossibleEnzymes() {
		List<String> listOfPossibleEnzymes = new Vector<String>();
		listOfPossibleEnzymes.add("ANTISENSE_RNA");
		listOfPossibleEnzymes.add("SIMPLE_MOLECULE");
		listOfPossibleEnzymes.add("UNKNOWN");
		listOfPossibleEnzymes.add("COMPLEX");
		listOfPossibleEnzymes.add("TRUNCATED");
		listOfPossibleEnzymes.add("GENERIC");
		listOfPossibleEnzymes.add("RNA");
		listOfPossibleEnzymes.add("RECEPTOR");
		return listOfPossibleEnzymes;
	}

	/**
	 * identify which Modifer is used
	 * 
	 * @param reactionNum
	 */
	public static final void identifyModifers(PluginReaction reaction,
			List<String> listOfPossibleEnzymes, List<String> inhibitors,
			List<String> transActivators, List<String> transInhibitors,
			List<String> activators, List<String> enzymes,
			List<String> nonEnzymeCatalyzers) {
		inhibitors.clear();
		transActivators.clear();
		transInhibitors.clear();
		activators.clear();
		enzymes.clear();
		nonEnzymeCatalyzers.clear();
		String type;
		for (int modifierNum = 0; modifierNum < reaction.getNumModifiers(); modifierNum++) {
			type = reaction.getModifier(modifierNum).getModificationType()
					.toUpperCase();
			if (type.equals("MODULATION")) {
				inhibitors.add(reaction.getModifier(modifierNum).getSpecies());
				activators.add(reaction.getModifier(modifierNum).getSpecies());
			} else if (type.equals("INHIBITION"))
				inhibitors.add(reaction.getModifier(modifierNum).getSpecies());
			else if (type.equals("TRANSCRIPTIONAL_ACTIVATION")
					|| type.equals("TRANSLATIONAL_ACTIVATION"))
				transActivators.add(reaction.getModifier(modifierNum)
						.getSpecies());
			else if (type.equals("TRANSCRIPTIONAL_INHIBITION")
					|| type.equals("TRANSLATIONAL_INHIBITION"))
				transInhibitors.add(reaction.getModifier(modifierNum)
						.getSpecies());
			else if (type.equals("UNKNOWN_CATALYSIS") || type.equals("TRIGGER")
					|| type.equals("PHYSICAL_STIMULATION"))
				activators.add(reaction.getModifier(modifierNum).getSpecies());
			else if (type.equals("CATALYSIS")) {
				PluginSpecies species = reaction.getModifier(modifierNum)
						.getSpeciesInstance();
				String speciesAliasType = species.getSpeciesAlias(0).getType()
						.equals("PROTEIN") ? species.getSpeciesAlias(0)
						.getProtein().getType() : species.getSpeciesAlias(0)
						.getType();
				if (listOfPossibleEnzymes.contains(speciesAliasType))
					enzymes.add(reaction.getModifier(modifierNum).getSpecies());
				else
					nonEnzymeCatalyzers.add(reaction.getModifier(modifierNum)
							.getSpecies());
			}
		}
	}

	/**
	 * This method returns true if and only if the kinetic law can be assigned
	 * to the given reaction. If the structure of the reaction, i.e. the number
	 * of reactants or products, the number and type of modifiers does not allow
	 * to assign this type of kinetic law to this reaction, false will be
	 * returned. This method must be implemented by more specialized instances
	 * of this class.
	 * 
	 * @param reaction
	 * @return
	 */
	public static boolean isApplicable(PluginReaction reaction) {
		return false;
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
		if (Double.parseDouble(exponent.toString()) == 0f)
			return new StringBuffer('1');
		if (Double.parseDouble(exponent.toString()) == 1f)
			return basis instanceof StringBuffer ? (StringBuffer) basis
					: new StringBuffer(basis.toString());
		return arith('^', basis, exponent);
	}

	/**
	 * Returns the exponent-th root of the basis as StringBuffer.
	 * 
	 * @param exponent
	 * @param basis
	 * @return
	 * @throws IllegalFormatException
	 *             If the given exponent represents a zero.
	 */
	public static final StringBuffer root(Object exponent, Object basis)
			throws IllegalFormatException {
		if (Double.parseDouble(exponent.toString()) == 0f)
			throw new IllegalFormatException(
					"Cannot extract a zeroth root of anything");
		if (Double.parseDouble(exponent.toString()) == 1f)
			return new StringBuffer(basis.toString());
		return concat("root(", exponent, Character.valueOf(','), basis,
				Character.valueOf(')'));
	}

	public static final StringBuffer sqrt(Object basis) {
		try {
			return root(Integer.valueOf(2), basis);
		} catch (IllegalFormatException e) {
			return pow(basis, frac(Integer.valueOf(1), Integer.valueOf(2)));
		}
	}

	/**
	 * Returns the sum of the given elements as StringBuffer.
	 * 
	 * @param summands
	 * @return
	 */
	public static final StringBuffer sum(Object... summands) {
		return brackets(arith('+', summands));
	}

	/**
	 * 
	 * @param summands
	 * @return
	 */
	public static final StringBuffer sum(StringBuffer... summands) {
		return brackets(arith('+', summands));
	}

	/**
	 * Returns the product of the given elements as StringBuffer.
	 * 
	 * @param factors
	 * @return
	 */
	public static final StringBuffer times(Object... factors) {
		return arith('*', factors);
	}

	/**
	 * Returns the product of the given elements as StringBuffer.
	 * 
	 * @param factors
	 * @return
	 */
	public static final StringBuffer times(StringBuffer... factors) {
		return arith('*', factors);
	}

	public static final StringBuffer toText(PluginModel model, ASTNode astnode) {
		// if (astnode == null)
		// return null;
		//
		// ASTNode ast;
		// StringBuffer value = null;
		// if (astnode.isUMinus()) {
		// value = new StringBuffer('-');
		// if (astnode.getLeftChild().getNumChildren() > 0)
		// value.append(brackets(toText(model, astnode.getLeftChild())));
		// else
		// value.append(toText(model, astnode.getLeftChild()));
		// return value;
		// } else if (astnode.isSqrt())
		// return value = sqrt(toText(model, astnode.getChild(astnode
		// .getNumChildren() - 1)));
		// else if (astnode.isInfinity())
		// return value = new StringBuffer(POSITIVE_INFINITY);
		// else if (astnode.isNegInfinity())
		// return value = new StringBuffer(NEGATIVE_ININITY);
		//
		// switch (astnode.getType()) {
		// /*
		// * Numbers
		// */
		// case AST_REAL:
		// return value = new StringBuffer(Double.toString(astnode.getReal()));
		//
		// case AST_INTEGER:
		// return value = new StringBuffer(Integer.toString(astnode
		// .getInteger()));
		// /*
		// * Basic Functions
		// */
		// case AST_FUNCTION_LOG: {
		// value = new StringBuffer("\\log");
		// if (astnode.getNumChildren() == 2) {
		// value.append("_{");
		// value.append(toText(model, astnode.getLeftChild()));
		// value.append('}');
		// }
		// value.append('{');
		// if (astnode.getChild(astnode.getNumChildren() - 1).getNumChildren() >
		// 0)
		// value.append(brackets(toText(model, astnode.getChild(astnode
		// .getNumChildren() - 1))));
		// else
		// value.append(toText(model, astnode.getChild(astnode
		// .getNumChildren() - 1)));
		// value.append('}');
		// return value;
		// }
		// /*
		// * Operators
		// */
		// case AST_POWER:
		// value = toText(model, astnode.getLeftChild());
		// if (astnode.getLeftChild().getNumChildren() > 0)
		// value = brackets(value);
		// value.append("^{");
		// value.append(toText(model, astnode.getRightChild()));
		// value.append("}");
		// return value;
		//
		// case AST_PLUS:
		// value = toText(model, astnode.getLeftChild());
		// for (int i = 1; i < astnode.getNumChildren(); i++) {
		// ast = astnode.getChild(i);
		// value.append(" + ");
		// if (ast.getType() == AST_MINUS)
		// value.append(brackets(toText(model, ast)));
		// else
		// value.append(toText(model, ast));
		// }
		// return value;
		//
		// case AST_MINUS:
		// value = toText(model, astnode.getLeftChild());
		// for (int i = 1; i < astnode.getNumChildren(); i++) {
		// ast = astnode.getChild(i);
		// value.append(" - ");
		// if (ast.getType() == AST_PLUS)
		// value.append(brackets(toText(model, ast)));
		// else
		// value.append(toText(model, ast));
		// }
		// return value;
		//
		// case AST_TIMES:
		// value = toText(model, astnode.getLeftChild());
		// if ((1 < astnode.getLeftChild().getNumChildren())
		// && ((astnode.getLeftChild().getType() == AST_MINUS) || (astnode
		// .getLeftChild().getType() == AST_PLUS)))
		// value = brackets(value);
		// for (int i = 1; i < astnode.getNumChildren(); i++) {
		// ast = astnode.getChild(i);
		// value.append("\\cdot");
		// if ((ast.getType() == AST_MINUS) || (ast.getType() == AST_PLUS))
		// value.append(brackets(toText(model, ast)));
		// else {
		// value.append(' ');
		// value.append(toText(model, ast));
		// }
		// }
		// return value;
		//
		// case AST_DIVIDE:
		// return value = frac(toText(model, astnode.getLeftChild()), toText(
		// model, astnode.getRightChild()));
		//
		// case AST_RATIONAL:
		// return value = frac(Double.toString(astnode.getNumerator()), Double
		// .toString(astnode.getDenominator()));
		//
		// case AST_NAME_TIME:
		// return value = mathrm(astnode.getName());
		//
		// case AST_FUNCTION_DELAY:
		// return value = mathrm(astnode.getName());
		//
		// /*
		// * Names of identifiers: parameters, functions, species etc.
		// */
		// case AST_NAME:
		// if (model.getSpecies(astnode.getName()) != null) {
		// // Species.
		// Species species = model.getSpecies(astnode.getName());
		// Compartment c = model.getCompartment(species.getCompartment());
		// boolean concentration = !species.getHasOnlySubstanceUnits()
		// && (0 < c.getSpatialDimensions());
		// value = new StringBuffer();
		// if (concentration)
		// value.append('[');
		// value.append(getNameOrID(species, true));
		// if (concentration)
		// value.append(']');
		// return value;
		//
		// } else if (model.getCompartment(astnode.getName()) != null) {
		// // Compartment
		// Compartment c = model.getCompartment(astnode.getName());
		// return value = getSize(c);
		// }
		// // TODO: weitere spezialfälle von Namen!!!
		// return value = new StringBuffer(mathtt(maskSpecialChars(astnode
		// .getName())));
		// /*
		// * Constants: pi, e, true, false
		// */
		// case AST_CONSTANT_PI:
		// return value = new StringBuffer(CONSTANT_PI);
		// case AST_CONSTANT_E:
		// return value = new StringBuffer(CONSTANT_E);
		// case AST_CONSTANT_TRUE:
		// return value = new StringBuffer(CONSTANT_TRUE);
		// case AST_CONSTANT_FALSE:
		// return value = new StringBuffer(CONSTANT_FALSE);
		// case AST_REAL_E:
		// return value = new StringBuffer(Double.toString(astnode.getReal()));
		// /*
		// * More complicated functions
		// */
		// case AST_FUNCTION_ABS:
		// return value = abs(toText(model, astnode.getChild(astnode
		// .getNumChildren() - 1)));
		//
		// case AST_FUNCTION_ARCCOS:
		// if (astnode.getLeftChild().getNumChildren() > 0)
		// return value = arccos(brackets(toText(model, astnode
		// .getLeftChild())));
		// return value = arccos(toText(model, astnode.getLeftChild()));
		//
		// case AST_FUNCTION_ARCCOSH:
		// if (astnode.getLeftChild().getNumChildren() > 0)
		// return value = arccosh(brackets(toText(model, astnode
		// .getLeftChild())));
		// return value = arccosh(toText(model, astnode.getLeftChild()));
		//
		// case AST_FUNCTION_ARCCOT:
		// value = new StringBuffer("\\arcot{");
		// if (0 < astnode.getLeftChild().getNumChildren())
		// value.append(brackets(toText(model, astnode.getLeftChild())));
		// else
		// value.append(toText(model, astnode.getLeftChild()));
		// value.append('}');
		// return value;
		//
		// case AST_FUNCTION_ARCCOTH:
		// value = mathrm("arccoth");
		// if (0 < astnode.getLeftChild().getNumChildren())
		// value.append(brackets(toText(model, astnode.getLeftChild())));
		// else
		// value.append(toText(model, astnode.getLeftChild()));
		// return value;
		//
		// case AST_FUNCTION_ARCCSC:
		// value = new StringBuffer("\\arccsc{");
		// if (0 < astnode.getLeftChild().getNumChildren())
		// value.append(brackets(toText(model, astnode.getLeftChild())));
		// else
		// value.append(toText(model, astnode.getLeftChild()));
		// value.append('}');
		// return value;
		//
		// case AST_FUNCTION_ARCCSCH:
		// value = mathrm("arccsh");
		// if (0 < astnode.getLeftChild().getNumChildren())
		// value.append(brackets(toText(model, astnode.getLeftChild())));
		// else
		// value.append(toText(model, astnode.getLeftChild()));
		// return value;
		//
		// case AST_FUNCTION_ARCSEC:
		// value = new StringBuffer("\\arcsec{");
		// if (0 < astnode.getLeftChild().getNumChildren())
		// value.append(brackets(toText(model, astnode.getLeftChild())));
		// else
		// value.append(toText(model, astnode.getLeftChild()));
		// value.append('}');
		// return value;
		//
		// case AST_FUNCTION_ARCSECH:
		// value = mathrm("arcsech");
		// if (0 < astnode.getLeftChild().getNumChildren())
		// value.append(brackets(toText(model, astnode.getLeftChild())));
		// else
		// value.append(toText(model, astnode.getLeftChild()));
		// return value;
		//
		// case AST_FUNCTION_ARCSIN:
		// value = new StringBuffer("\\arcsin{");
		// if (0 < astnode.getLeftChild().getNumChildren())
		// value.append(brackets(toText(model, astnode.getLeftChild())));
		// else
		// value.append(toText(model, astnode.getLeftChild()));
		// value.append('}');
		// return value;
		//
		// case AST_FUNCTION_ARCSINH:
		// value = mathrm("arcsinh");
		// if (0 < astnode.getLeftChild().getNumChildren())
		// value.append(brackets(toText(model, astnode.getLeftChild())));
		// else
		// value.append(toText(model, astnode.getLeftChild()));
		// return value;
		//
		// case AST_FUNCTION_ARCTAN:
		// value = new StringBuffer("\\arctan{");
		// if (0 < astnode.getLeftChild().getNumChildren())
		// value.append(brackets(toText(model, astnode.getLeftChild())));
		// else
		// value.append(toText(model, astnode.getLeftChild()));
		// value.append('}');
		// return value;
		//
		// case AST_FUNCTION_ARCTANH:
		// value = new StringBuffer("\\arctanh{");
		// if (0 < astnode.getLeftChild().getNumChildren())
		// value.append(brackets(toText(model, astnode.getLeftChild())));
		// else
		// value.append(toText(model, astnode.getLeftChild()));
		// value.append('}');
		// return value;
		//
		// case AST_FUNCTION_CEILING:
		// return value = ceiling(toText(model, astnode.getLeftChild()));
		//
		// case AST_FUNCTION_COS:
		// value = new StringBuffer("\\cos{");
		// if (0 < astnode.getLeftChild().getNumChildren())
		// value.append(brackets(toText(model, astnode.getLeftChild())));
		// else
		// value.append(toText(model, astnode.getLeftChild()));
		// value.append('}');
		// return value;
		//
		// case AST_FUNCTION_COSH:
		// value = new StringBuffer("\\cosh{");
		// if (0 < astnode.getLeftChild().getNumChildren())
		// value.append(brackets(toText(model, astnode.getLeftChild())));
		// else
		// value.append(toText(model, astnode.getLeftChild()));
		// value.append('}');
		// return value;
		//
		// case AST_FUNCTION_COT:
		// value = new StringBuffer("\\cot{");
		// if (0 < astnode.getLeftChild().getNumChildren())
		// value.append(brackets(toText(model, astnode.getLeftChild())));
		// else
		// value.append(toText(model, astnode.getLeftChild()));
		// value.append('}');
		// return value;
		//
		// case AST_FUNCTION_COTH:
		// value = new StringBuffer("\\coth{");
		// if (0 < astnode.getLeftChild().getNumChildren())
		// value.append(brackets(toText(model, astnode.getLeftChild())));
		// else
		// value.append(toText(model, astnode.getLeftChild()));
		// value.append('}');
		// return value;
		//
		// case AST_FUNCTION_CSC:
		// value = new StringBuffer("\\csc{");
		// if (0 < astnode.getLeftChild().getNumChildren())
		// value.append(brackets(toText(model, astnode.getLeftChild())));
		// else
		// value.append(toText(model, astnode.getLeftChild()));
		// value.append('}');
		// return value;
		//
		// case AST_FUNCTION_CSCH:
		// value = mathrm("csch");
		// if (0 < astnode.getLeftChild().getNumChildren())
		// value.append(brackets(toText(model, astnode.getLeftChild())));
		// else
		// value.append(toText(model, astnode.getLeftChild()));
		// return value;
		//
		// case AST_FUNCTION_EXP:
		// value = new StringBuffer("\\exp{");
		// if (0 < astnode.getLeftChild().getNumChildren())
		// value.append(brackets(toText(model, astnode.getLeftChild())));
		// else
		// value.append(toText(model, astnode.getLeftChild()));
		// value.append('}');
		// return value;
		//
		// case AST_FUNCTION_FACTORIAL:
		// if (astnode.getLeftChild().getNumChildren() > 0)
		// value = brackets(toText(model, astnode.getLeftChild()));
		// else
		// value = new StringBuffer(toText(model, astnode.getLeftChild()));
		// value.append('!');
		// return value;
		//
		// case AST_FUNCTION_FLOOR:
		// return value = floor(toText(model, astnode.getLeftChild()));
		//
		// case AST_FUNCTION_LN:
		// value = new StringBuffer("\\ln{");
		// if (astnode.getLeftChild().getNumChildren() > 0)
		// value = brackets(toText(model, astnode.getLeftChild()));
		// else
		// value.append(toText(model, astnode.getLeftChild()));
		// value.append('}');
		// return value;
		//
		// case AST_FUNCTION_POWER:
		// if (astnode.getLeftChild().getNumChildren() > 0)
		// value = brackets(toText(model, astnode.getLeftChild()));
		// else
		// value = new StringBuffer(toText(model, astnode.getLeftChild()));
		// value.append("^{");
		// value.append(toText(model, astnode.getChild(astnode
		// .getNumChildren() - 1)));
		// value.append('}');
		// return value;
		//
		// case AST_FUNCTION_ROOT:
		// ASTNode left = astnode.getLeftChild();
		// if ((astnode.getNumChildren() > 1)
		// && ((left.isInteger() && (left.getInteger() != 2)) || (left
		// .isReal() && (left.getReal() != 2d))))
		// return value = root(toText(model, astnode.getLeftChild()),
		// toText(model, astnode.getRightChild()));
		// return value = sqrt(toText(model, astnode.getChild(astnode
		// .getNumChildren() - 1)));
		//
		// case AST_FUNCTION_SEC:
		// value = new StringBuffer("\\sec{");
		// if (astnode.getLeftChild().getNumChildren() > 0)
		// value.append(brackets(toText(model, astnode.getLeftChild())));
		// else
		// value.append(toText(model, astnode.getLeftChild()));
		// value.append('}');
		// return value;
		//
		// case AST_FUNCTION_SECH:
		// value = mathrm("sech");
		// if (astnode.getLeftChild().getNumChildren() > 0)
		// value.append(brackets(toText(model, astnode.getLeftChild())));
		// else
		// value.append(toText(model, astnode.getLeftChild()));
		// value.append('}');
		// return value;
		//
		// case AST_FUNCTION_SIN:
		// value = new StringBuffer("\\sin{");
		// if (astnode.getLeftChild().getNumChildren() > 0)
		// value.append(brackets(toText(model, astnode.getLeftChild())));
		// else
		// value.append(toText(model, astnode.getLeftChild()));
		// value.append('}');
		// return value;
		//
		// case AST_FUNCTION_SINH:
		// value = new StringBuffer("\\sinh{");
		// if (astnode.getLeftChild().getNumChildren() > 0)
		// value.append(brackets(toText(model, astnode.getLeftChild())));
		// else
		// value.append(toText(model, astnode.getLeftChild()));
		// value.append('}');
		// return value;
		//
		// case AST_FUNCTION_TAN:
		// value = new StringBuffer("\\tan{");
		// if (astnode.getLeftChild().getNumChildren() > 0)
		// value.append(brackets(toText(model, astnode.getLeftChild())));
		// else
		// value.append(toText(model, astnode.getLeftChild()));
		// value.append('}');
		// return value;
		//
		// case AST_FUNCTION_TANH:
		// value = new StringBuffer("\\tanh{");
		// if (astnode.getLeftChild().getNumChildren() > 0)
		// value.append(brackets(toText(model, astnode.getLeftChild())));
		// else
		// value.append(toText(model, astnode.getLeftChild()));
		// value.append('}');
		// return value;
		//
		// case AST_FUNCTION:
		// value = new StringBuffer(
		// mathtt(maskSpecialChars(astnode.getName())));
		// StringBuffer args = new StringBuffer(toText(model, astnode
		// .getLeftChild()));
		// for (int i = 1; i < astnode.getNumChildren(); i++) {
		// args.append(", ");
		// args.append(toText(model, astnode.getChild(i)));
		// }
		// args = brackets(args);
		// value.append(args);
		// return value;
		//
		// case AST_LAMBDA:
		// value = new StringBuffer(toText(model, astnode.getLeftChild()));
		// // mathtt(maskLaTeXspecialSymbols(astnode.getName())) == LAMBDA!!!
		// // value.append('(');
		// for (int i = 1; i < astnode.getNumChildren() - 1; i++) {
		// value.append(", ");
		// value.append(toText(model, astnode.getChild(i)));
		// }
		// value = brackets(value);
		// value.append(" = ");
		// value.append(toText(model, astnode.getRightChild()));
		// return value;
		//
		// case AST_LOGICAL_AND:
		// return value = mathematicalOperation(astnode, model, "\\wedge ");
		// case AST_LOGICAL_XOR:
		// return value = mathematicalOperation(astnode, model, "\\oplus ");
		// case AST_LOGICAL_OR:
		// return value = mathematicalOperation(astnode, model, "\\lor ");
		// case AST_LOGICAL_NOT:
		// value = new StringBuffer("\\neg ");
		// if (0 < astnode.getLeftChild().getNumChildren())
		// value.append(brackets(toText(model, astnode.getLeftChild())));
		// else
		// value.append(toText(model, astnode.getLeftChild()));
		// return value;
		//
		// case AST_FUNCTION_PIECEWISE:
		// value = new StringBuffer("\\begin{dcases}");
		// value.append(newLine);
		// for (int i = 0; i < astnode.getNumChildren() - 1; i++) {
		// value.append(toText(model, astnode.getChild(i)));
		// value.append(((i % 2) == 0) ? " & \\text{if\\ } " : lineBreak);
		// }
		// value.append(toText(model, astnode.getChild(astnode
		// .getNumChildren() - 1)));
		// if ((astnode.getNumChildren() % 2) == 1) {
		// value.append(" & \\text{otherwise}");
		// value.append(newLine);
		// }
		// value.append("\\end{dcases}");
		// return value;
		//
		// case AST_RELATIONAL_EQ:
		// value = new StringBuffer(toText(model, astnode.getLeftChild()));
		// value.append(" = ");
		// value.append(toText(model, astnode.getRightChild()));
		// return value;
		//
		// case AST_RELATIONAL_GEQ:
		// value = new StringBuffer(toText(model, astnode.getLeftChild()));
		// value.append(" \\geq ");
		// value.append(toText(model, astnode.getRightChild()));
		// return value;
		//
		// case AST_RELATIONAL_GT:
		// value = new StringBuffer(toText(model, astnode.getLeftChild()));
		// value.append(" > ");
		// value.append(toText(model, astnode.getRightChild()));
		// return value;
		//
		// case AST_RELATIONAL_NEQ:
		// value = new StringBuffer(toText(model, astnode.getLeftChild()));
		// value.append(" \\neq ");
		// value.append(toText(model, astnode.getRightChild()));
		// return value;
		//
		// case AST_RELATIONAL_LEQ:
		// value = new StringBuffer(toText(model, astnode.getLeftChild()));
		// value.append(" \\leq ");
		// value.append(toText(model, astnode.getRightChild()));
		// return value;
		//
		// case AST_RELATIONAL_LT:
		// value = new StringBuffer(toText(model, astnode.getLeftChild()));
		// value.append(" < ");
		// value.append(toText(model, astnode.getRightChild()));
		// return value;
		//
		// case AST_UNKNOWN:
		// return value = mathtext(" unknown ");
		//
		// default:
		// return value = new StringBuffer();
		// }
		return null;
	}

	/**
	 * Basic method which links several elements with a mathematical operator.
	 * All empty StringBuffer object are excluded.
	 * 
	 * @param operator
	 * @param elements
	 * @return
	 */
	private static final StringBuffer arith(char operator, Object... elements) {
		Vector<Object> vsb = new Vector<Object>();
		StringBuffer equation = new StringBuffer();
		for (Object sb : elements)
			if (sb.toString().length() > 0)
				vsb.add(sb);
		if (vsb.size() >= 1)
			equation.append(vsb.get(0));
		for (int count = 1; count < vsb.size(); count++)
			append(equation, operator, vsb.get(count));
		return equation;
	}

	private static StringBuffer floor(StringBuffer text) {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * This method constructs a full length SBO number from a given SBO id.
	 * Whenever a SBO number is used in the model please don't forget to add
	 * this identifier to the Set of SBO numbers (only those numbers in this set
	 * will be displayed in the glossary).
	 * 
	 * @param sbo
	 * @return
	 */
	protected static String getSBOnumber(int sbo) {
		if (sbo < 0)
			return "none";
		String sboString = Integer.toString(sbo);
		while (sboString.length() < 7)
			sboString = '0' + sboString;
		return sboString;
	}

	/**
	 * Returns the id of a PluginSpeciesReference object's belonging species as
	 * an object of type StringBuffer.
	 * 
	 * @param ref
	 * @return
	 */
	protected static final StringBuffer getSpecies(PluginSpeciesReference ref) {
		return new StringBuffer(ref.getSpecies());
	}

	protected HashMap<String, String> idAndName;

	protected StringBuffer formelTeX;

	private List<StringBuffer> listOfLocalParameters, listOfGlobalParameters;

	protected String sboTerm;

	protected PluginModel model;

	/**
	 * 
	 * @param parentReaction
	 * @param model
	 * @throws RateLawNotApplicableException
	 * @throws IOException
	 * @throws IllegalFormatException
	 */
	public BasicKineticLaw(PluginReaction parentReaction, PluginModel model)
			throws RateLawNotApplicableException, IOException,
			IllegalFormatException {
		this(parentReaction, model, getDefaultListOfPossibleEnzymes());
	}

	/**
	 * 
	 * @param parentReaction
	 * @param model
	 * @param listOfPossibleEnzymes
	 * @param type
	 * @throws RateLawNotApplicableException
	 * @throws IOException
	 * @throws IllegalFormatException
	 */
	public BasicKineticLaw(PluginReaction parentReaction, PluginModel model,
			List<String> listOfPossibleEnzymes)
			throws RateLawNotApplicableException, IOException,
			IllegalFormatException {
		super(parentReaction);
		this.model = model;
		sboTerm = null;
		idAndName = new HashMap<String, String>();
		listOfLocalParameters = new ArrayList<StringBuffer>();
		listOfGlobalParameters = new ArrayList<StringBuffer>();
		List<String> modActi = new ArrayList<String>();
		List<String> modCat = new ArrayList<String>();
		List<String> modInhib = new ArrayList<String>();
		List<String> modE = new ArrayList<String>();
		List<String> modTActi = new ArrayList<String>();
		List<String> modTInhib = new ArrayList<String>();
		identifyModifers(parentReaction, listOfPossibleEnzymes, modInhib,
				modTActi, modTInhib, modActi, modE, modCat);
		StringBuffer formula = createKineticEquation(model, modE, modActi,
				modTActi, modInhib, modTInhib, modCat);
		if (getMath() == null) {
			setFormula(formula.toString());
			setMathFromFormula();
		}
		formelTeX = (new LaTeXExport().toLaTeX(model, getMath()));
	}

	/**
	 * Returns a list of names of all parameters, which are only allowed to be
	 * stored globally.
	 * 
	 * @return
	 */
	public List<StringBuffer> getGlobalParameters() {
		return listOfGlobalParameters;
	}

	/**
	 * Returns the LaTeX expression of the generated formual of this kinetic
	 * law.
	 * 
	 * @return
	 */
	public StringBuffer getKineticTeX() {
		return formelTeX;
	}

	/**
	 * Returns a list of the names of all parameters used by this law. This list
	 * contains parameters, which can be either stored locally, i.e., assigned
	 * to the specific kinetic law or globally for the whole model.
	 * 
	 * @return
	 */
	public List<StringBuffer> getLocalParameters() {
		return listOfLocalParameters;
	}

	/**
	 * Returns the name of the kinetic formula of this object.
	 * 
	 * @return
	 */
	public abstract String getName();

	/**
	 * Recursively removes waste brackets from a formula.
	 * 
	 * TODO Refinement
	 * 
	 * @param sb
	 * @return
	 */
	/*
	 * protected StringBuffer removeBrackets(StringBuffer sb) { if (sb.length()
	 * > 0) { if ((sb.charAt(0) == '(') && (sb.charAt(sb.length() - 1) == ')'))
	 * { sb.deleteCharAt(sb.length() - 1); sb.deleteCharAt(0); sb =
	 * removeBrackets(sb); } } return sb; }
	 */

	/**
	 * Returns the SBO identifier of the respective kinetic law if there is one
	 * or an empty String otherwise.
	 * 
	 * @return
	 */
	public abstract String getSBO();

	// @Override
	public String toString() {
		if (sboTerm == null)
			sboTerm = getName();
		return sboTerm;
	}

	/**
	 * Adds the given parameter only to the list of global parameters if this
	 * list does not yet contain this parameter.
	 * 
	 * @param parameter
	 */
	protected void addGlobalParameter(StringBuffer parameter) {
		if (!listOfGlobalParameters.contains(parameter))
			listOfGlobalParameters.add(parameter);
	}

	/**
	 * Adds the given parameter only to the list of local parameters if this
	 * list does not yet contain this parameter.
	 * 
	 * @param parameter
	 */
	protected void addLocalParameter(StringBuffer parameter) {
		if (!listOfLocalParameters.contains(parameter))
			listOfLocalParameters.add(parameter);
	}

	/**
	 * 
	 * @param model
	 * @param modE
	 * @param modActi
	 * @param modTActi
	 * @param modInhib
	 * @param modTInhib
	 * @param modCat
	 * @return
	 * @throws RateLawNotApplicableException
	 * @throws IllegalFormatException
	 */
	protected abstract StringBuffer createKineticEquation(PluginModel model,
			List<String> modE, List<String> modActi, List<String> modTActi,
			List<String> modInhib, List<String> modTInhib, List<String> modCat)
			throws RateLawNotApplicableException, IllegalFormatException;

	/**
	 * Returns the value of a PluginSpeciesReference object's stoichiometry
	 * either as a double or, if the stoichiometry has an integer value, as an
	 * int object.
	 * 
	 * @param ref
	 * @return
	 */
	protected final StringBuffer getStoichiometry(PluginSpeciesReference ref) {
		if (ref.getStoichiometryMath() == null) {
			double stoich = ref.getStoichiometry();
			if ((int) stoich - stoich == 0)
				return new StringBuffer(Integer.toString((int) stoich));
			else
				return new StringBuffer(Double.toString(stoich));
		}
		return toText(model, ref.getStoichiometryMath().getMath());
	}

}
