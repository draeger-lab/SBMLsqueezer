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
package org.sbml;

import java.util.LinkedList;
import java.util.List;

/**
 * @author Andreas Dr&auml;ger <a
 *         href="mailto:andreas.draeger@uni-tuebingen.de">
 *         andreas.draeger@uni-tuebingen.de</a>
 * 
 */
public class ASTNode {

	private int denominator;
	private int exponent;
	private int integer;
	private LinkedList<ASTNode> listOfChildren;
	private double mantissa;
	private String name;
	private int numerator;
	private char operator;
	private Constants type;
	private NamedSBase variable;
	SBase parentSBMLObject;

	/**
	 * 
	 */
	public ASTNode() {
		type = Constants.AST_UNKNOWN;
		listOfChildren = new LinkedList<ASTNode>();
	}

	/**
	 * @param arg0
	 */
	public ASTNode(ASTNode astNode) {
		this();
		setType(astNode.getType());
		parentSBMLObject = astNode.parentSBMLObject;
		for (ASTNode child : astNode.listOfChildren)
			listOfChildren.add(new ASTNode(child));
	}

	/**
	 * Creates and returns a new ASTNode.
	 * 
	 * By default, the returned node will have a type of AST_UNKNOWN. The
	 * calling code should set the node type to something else as soon as
	 * possible using setType(int)
	 * 
	 * @param type
	 */
	public ASTNode(Constants type) {
		this();
		setType(type);
	}

	public void addChild(ASTNode child) {
		listOfChildren.add(child);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#clone()
	 */
	// @Override
	public ASTNode clone() {
		return new ASTNode(this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	// @Override
	public boolean equals(Object o) {
		if (o instanceof ASTNode) {
			ASTNode ast = (ASTNode) o;
			boolean equal = ast.getType() == type;
			if (equal)
				for (ASTNode child : listOfChildren)
					equal = equal && child.equals(ast);
			return equal;
		}
		return false;
	}

	/**
	 * Get the value of this node as a single character. This function should be
	 * called only when ASTNode.getType() is one of AST_PLUS, AST_MINUS,
	 * AST_TIMES, AST_DIVIDE or AST_POWER.
	 * 
	 * @return the value of this ASTNode as a single character
	 */
	public char getCharacter() {
		if (isOperator())
			return operator;
		throw new RuntimeException(new IllegalAccessException(
				"getCharacter() should be called only when isOperator()."));
	}

	/**
	 * Get a child of this node according to an index number.
	 * 
	 * @param n
	 *            the index of the child to get
	 * @return the nth child of this ASTNode or NULL if this node has no nth
	 *         child (n > getNumChildren() - 1).
	 */
	public ASTNode getChild(int n) {
		return listOfChildren.get(n);
	}

	/**
	 * Get the value of the denominator of this node. This function should be
	 * called only when getType() == AST_RATIONAL.
	 * 
	 * 
	 * @return the value of the denominator of this ASTNode.
	 */
	public int getDenominator() {
		if (isRational())
			return denominator;
		throw new RuntimeException(
				new IllegalAccessException(
						"getDenominator() should be called only when getType() == AST_RATIONAL."));
	}

	/**
	 * Get the exponent value of this ASTNode. This function should be called
	 * only when getType() returns AST_REAL_E or AST_REAL.
	 * 
	 * @return the value of the exponent of this ASTNode.
	 */
	public int getExponent() {
		if (type == Constants.AST_REAL || type == Constants.AST_REAL_E)
			return exponent;
		throw new RuntimeException(
				new IllegalAccessException(
						"getExponent() should be called only when getType() == AST_REAL_E or AST_REAL"));
	}

	/**
	 * Get the value of this node as an integer. This function should be called
	 * only when getType() == AST_INTEGER.
	 * 
	 * @return the value of this ASTNode as a (long) integer.
	 */
	public int getInteger() {
		if (isInteger())
			return integer;
		throw new RuntimeException(
				new IllegalAccessException(
						"getInteger() should be called only when getType() == AST_INTEGER"));
	}

	/**
	 * Get the left child of this node.
	 * 
	 * @return the left child of this ASTNode. This is equivalent to
	 *         getChild(0);
	 */
	public ASTNode getLeftChild() {
		return getChild(0);
	}

	/**
	 * 
	 * @return
	 */
	public List<ASTNode> getListOfNodes() {
		return listOfChildren;
	}

	/**
	 * Get the mantissa value of this node. This function should be called only
	 * when getType() returns AST_REAL_E or AST_REAL. If getType() returns
	 * AST_REAL, this method is identical to getReal().
	 * 
	 * @return the value of the mantissa of this ASTNode.
	 */
	public double getMantissa() {
		switch (type) {
		case AST_REAL:
			getReal();
		case AST_REAL_E:
			return mantissa;
		default:
			throw new RuntimeException(
					new IllegalAccessException(
							"getMantissa() should be called only when getType() == AST_REAL or AST_REAL_E"));
		}
	}

	/**
	 * Get the value of this node as a string. This function may be called on
	 * nodes that are not operators (isOperator() == false) or numbers
	 * (isNumber() == false).
	 * 
	 * @return the value of this ASTNode as a string.
	 */
	public String getName() {
		if (!isOperator() && !isNumber())
			return variable == null ? name : variable.getId();
		throw new RuntimeException(
				new IllegalAccessException(
						"getName() should be called only when !isNumber() || !isOperator()"));
	}

	/**
	 * Get the number of children that this node has.
	 * 
	 * @return the number of children of this ASTNode, or 0 is this node has no
	 *         children.
	 */
	public int getNumChildren() {
		return listOfChildren.size();
	}

	/**
	 * Get the value of the numerator of this node. This function should be
	 * called only when getType() == AST_RATIONAL.
	 * 
	 * 
	 * @return the value of the numerator of this ASTNode.
	 */
	public int getNumerator() {
		if (isRational())
			return numerator;
		throw new RuntimeException(new IllegalAccessException(
				"getNumerator() should be called only when isRational()"));
	}

	/**
	 * This method is convenient when holding an object nested inside other
	 * objects in an SBML model. It allows direct access to the &lt;model&gt;
	 * 
	 * element containing it.
	 * 
	 * @return Returns the parent SBML object.
	 */
	public SBase getParentSBMLObject() {
		return parentSBMLObject;
	}

	/**
	 * Get the real-numbered value of this node. This function should be called
	 * only when isReal() == true.
	 * 
	 * This function performs the necessary arithmetic if the node type is
	 * AST_REAL_E (mantissa^exponent) or AST_RATIONAL (numerator / denominator).
	 * 
	 * @return the value of this ASTNode as a real (double).
	 */
	public double getReal() {
		if (isReal() || type == Constants.AST_CONSTANT_E || type == Constants.AST_CONSTANT_PI) {
			switch (type) {
			case AST_REAL:
				return mantissa;
			case AST_REAL_E:
				return Math.pow(getMantissa(), getExponent());
			case AST_RATIONAL:
				return getNumerator() / getDenominator();
			case AST_CONSTANT_E:
				return Math.E;
			case AST_CONSTANT_PI:
				return Math.PI;
			default:
				break;
			}
		}
		throw new RuntimeException(new IllegalAccessException(
				"getReal() should be called only when isReal()"));
	}

	public ASTNode getRightChild() {
		return listOfChildren.getLast();
	}

	public Constants getType() {
		return type;
	}

	public NamedSBase getVariable() {
		if (isName())
			return variable;
		throw new RuntimeException(
				new IllegalAccessException(
						"getVariable() should be called only when !isNumber() || !isOperator()"));
	}

	/**
	 * Insert the given ASTNode at point n in the list of children of this
	 * ASTNode. Inserting a child within an ASTNode may result in an inaccurate
	 * representation.
	 * 
	 * @param n
	 *            long the index of the ASTNode being added
	 * @param newChild
	 *            ASTNode to insert as the nth child
	 */
	public void insertChild(int n, ASTNode newChild) {
		listOfChildren.add(n, newChild);
	}

	/**
	 * Predicate returning true (non-zero) if this node has a boolean type (a
	 * logical operator, a relational operator, or the constants true or false).
	 * 
	 * @return true if this ASTNode is a boolean, false otherwise.
	 */
	public boolean isBoolean() {
		return type == Constants.AST_CONSTANT_FALSE
				|| type == Constants.AST_CONSTANT_TRUE
				|| type == Constants.AST_LOGICAL_AND
				|| type == Constants.AST_LOGICAL_NOT
				|| type == Constants.AST_LOGICAL_OR
				|| type == Constants.AST_LOGICAL_XOR;
	}

	/**
	 * Predicate returning true (non-zero) if this node represents a MathML
	 * constant (e.g., true, Pi).
	 * 
	 * @return true if this ASTNode is a MathML constant, false otherwise.
	 */
	public boolean isConstant() {
		return type.toString().startsWith("AST_CONSTANT");
	}

	/**
	 * Predicate returning true (non-zero) if this node represents a MathML
	 * function (e.g., abs()), or an SBML Level 1 function, or a user-defined
	 * function.
	 * 
	 * @return true if this ASTNode is a function, false otherwise.
	 */
	public boolean isFunction() {
		return type.toString().startsWith("AST_FUNCTION");
	}

	/**
	 * Predicate returning true (non-zero) if this node represents the special
	 * IEEE 754 value infinity, false (zero) otherwise.
	 * 
	 * @return true if this ASTNode is the special IEEE 754 value infinity,
	 *         false otherwise.
	 */
	public boolean isInfinity() {
		return isReal() && Double.isInfinite(getReal());
	}

	/**
	 * Predicate returning true (non-zero) if this node contains an integer
	 * value, false (zero) otherwise.
	 * 
	 * @return true if this ASTNode is of type AST_INTEGER, false otherwise.
	 */
	public boolean isInteger() {
		return type == Constants.AST_INTEGER;
	}

	/**
	 * Predicate returning true (non-zero) if this node is a MathML
	 * &lt;lambda&gt;, false (zero) otherwise.
	 * 
	 * @return true if this ASTNode is of type AST_LAMBDA, false otherwise.
	 */
	public boolean isLambda() {
		return type == Constants.AST_LAMBDA;
	}

	/**
	 * Predicate returning true (non-zero) if this node represents a log10()
	 * function, false (zero) otherwise. More precisely, this predicate returns
	 * true if the node type is AST_FUNCTION_LOG with two children, the first of
	 * which is an AST_INTEGER equal to 10.
	 * 
	 * @return true if the given ASTNode represents a log10() function, false
	 *         otherwise.
	 */
	public boolean isLog10() {
		return type == Constants.AST_FUNCTION_LOG && listOfChildren.size() == 2
				&& getLeftChild().isInteger()
				&& getLeftChild().getInteger() == 10;
	}

	/**
	 * Predicate returning true (non-zero) if this node is a MathML logical
	 * operator (i.e., and, or, not, xor).
	 * 
	 * @return true if this ASTNode is a MathML logical operator.
	 */
	public boolean isLogical() {
		return type.toString().startsWith("AST_LOGICAL_");
	}

	/**
	 * Predicate returning true (non-zero) if this node is a user-defined
	 * variable name in SBML L1, L2 (MathML), or the special symbols delay or
	 * time. The predicate returns false (zero) otherwise.
	 * 
	 * @return true if this ASTNode is a user-defined variable name in SBML L1,
	 *         L2 (MathML) or the special symbols delay or time.
	 */
	public boolean isName() {
		return type == Constants.AST_NAME;
	}

	/**
	 * Predicate returning true (non-zero) if this node represents the special
	 * IEEE 754 value 'not a number' (NaN), false (zero) otherwise.
	 * 
	 * @return true if this ASTNode is the special IEEE 754 NaN
	 */
	public boolean isNaN() {
		return isReal() && Double.isNaN(getReal());
	}

	/**
	 * Predicate returning true (non-zero) if this node represents the special
	 * IEEE 754 value 'negative infinity', false (zero) otherwise.
	 * 
	 * @return true if this ASTNode is the special IEEE 754 value negative
	 *         infinity, false otherwise.
	 */
	public boolean isNegInfinity() {
		return isReal() && Double.isInfinite(-1 * getReal());
	}

	/**
	 * Predicate returning true (non-zero) if this node contains a number, false
	 * (zero) otherwise. This is functionally equivalent to the following code:
	 * 
	 * <pre>
	 * isInteger() || isReal()
	 * </pre>
	 * 
	 * @return true if this ASTNode is a number, false otherwise.
	 */
	public boolean isNumber() {
		return isInteger() || isReal();
	}

	/**
	 * Predicate returning true (non-zero) if this node is a mathematical
	 * operator, meaning, +, -, *, / or ^ (power).
	 * 
	 * @return true if this ASTNode is an operator.
	 */
	public boolean isOperator() {
		return type == Constants.AST_PLUS || type == Constants.AST_MINUS
				|| type == Constants.AST_TIMES || type == Constants.AST_DIVIDE
				|| type == Constants.AST_POWER;
	}

	/**
	 * Predicate returning true (non-zero) if this node is the MathML
	 * &lt;piecewise&gt; construct, false (zero) otherwise.
	 * 
	 * @return true if this ASTNode is a MathML piecewise function
	 */
	public boolean isPiecewise() {
		return type == Constants.AST_FUNCTION_PIECEWISE;
	}

	/**
	 * Predicate returning true (non-zero) if this node represents a rational
	 * number, false (zero) otherwise.
	 * 
	 * @return true if this ASTNode is of type AST_RATIONAL.
	 */
	public boolean isRational() {
		return type == Constants.AST_RATIONAL;
	}

	/**
	 * Predicate returning true (non-zero) if this node can represent a real
	 * number, false (zero) otherwise. More precisely, this node must be of one
	 * of the following types: AST_REAL, AST_REAL_E or AST_RATIONAL.
	 * 
	 * @return true if the value of this ASTNode can represented as a real
	 *         number, false otherwise.
	 */
	public boolean isReal() {
		return type == Constants.AST_REAL || type == Constants.AST_REAL_E
				|| type == Constants.AST_RATIONAL;
	}

	/**
	 * Predicate returning true (non-zero) if this node is a MathML relational
	 * operator, meaning ==, >=, >, <, and !=.
	 * 
	 * @return true if this ASTNode is a MathML relational operator, false
	 *         otherwise.
	 */
	public boolean isRelational() {
		return type == Constants.AST_RATIONAL;
	}

	/**
	 * Predicate returning true (non-zero) if this node represents a square root
	 * function, false (zero) otherwise. More precisely, the node type must be
	 * AST_FUNCTION_ROOT with two children, the first of which is an AST_INTEGER
	 * node having value equal to 2.
	 * 
	 * 
	 * @return true if the given ASTNode represents a sqrt() function, false
	 *         otherwise.
	 */
	public boolean isSqrt() {
		return type == Constants.AST_FUNCTION_ROOT
				&& listOfChildren.size() == 2 && getLeftChild().isInteger()
				&& getLeftChild().getInteger() == 2;
	}

	/**
	 * Predicate returning true (non-zero) if this node is a unary minus
	 * operator, false (zero) otherwise. A node is defined as a unary minus node
	 * if it is of type AST_MINUS and has exactly one child.
	 * 
	 * For numbers, unary minus nodes can be 'collapsed' by negating the number.
	 * In fact, SBML_parseFormula() does this during its parse. However, unary
	 * minus nodes for symbols (AST_NAMES) cannot be 'collapsed', so this
	 * predicate function is necessary.
	 * 
	 * @return true if this ASTNode is a unary minus, false otherwise.
	 */
	public boolean isUMinus() {
		return type == Constants.AST_MINUS && getNumChildren() == 1;
	}

	/**
	 * Predicate returning true (non-zero) if this node has an unknown type.
	 * 
	 * 'Unknown' nodes have the type AST_UNKNOWN. Nodes with unknown types will
	 * not appear in an ASTNode tree returned by libSBML based upon valid SBML
	 * input; the only situation in which a node with type AST_UNKNOWN may
	 * appear is immediately after having create a new, untyped node using the
	 * ASTNode constructor. Callers creating nodes should endeavor to set the
	 * type to a valid node type as soon as possible after creating new nodes.
	 * 
	 * @return true if this ASTNode is of type AST_UNKNOWN, false otherwise.
	 */
	public boolean isUnknown() {
		return type == Constants.AST_UNKNOWN;
	}

	/**
	 * Adds the given node as a child of this ASTNode. This method adds child
	 * nodes from right to left.
	 * 
	 * @param child
	 */
	public void prependChild(ASTNode child) {
		listOfChildren.addLast(child);
	}

	/**
	 * Reduces this ASTNode to a binary tree, e.g., if the formula in this
	 * ASTNode is and(x, y, z) then the formula of the reduced node would be
	 * and(and(x, y), z)
	 */
	public void reduceToBinary() {
		// TODO
		// restructure this tree.
	}

	/**
	 * Removes child n of this ASTNode. Removing a child from an ASTNode may
	 * result in an inaccurate representation.
	 * 
	 * @param n
	 *            the index of the child to remove
	 * @return boolean indicating the success or failure of the operation
	 * 
	 */
	public boolean removeChild(long n) {
		return listOfChildren.remove(n);
	}

	/**
	 * Replaces occurences of a name within this ASTNode with the
	 * name/value/formula represented by the second argument ASTNode, e.g., if
	 * the formula in this ASTNode is x + y; bvar is x and arg is an ASTNode
	 * representing the real value 3 ReplaceArgument substitutes 3 for x within
	 * this ASTNode.
	 * 
	 * @param bvar
	 *            a string representing the variable name to be substituted
	 * @param arg
	 *            an ASTNode representing the name/value/formula to substitute
	 */
	public void replaceArgument(String bvar, ASTNode arg) {
		int n = 0;
		for (ASTNode child : listOfChildren) {
			if (child.isName() && child.getName().equals(bvar))
				replaceChild(n, arg.clone());
			else if (child.getNumChildren() > 0)
				child.replaceArgument(bvar, arg);
			n++;
		}
	}

	/**
	 * Replaces the nth child of this ASTNode with the given ASTNode.
	 * 
	 * @param n
	 *            long the index of the child to replace
	 * @param newChild
	 *            ASTNode to replace the nth child
	 * @return the element previously at the specified position
	 */
	public ASTNode replaceChild(int n, ASTNode newChild) {
		return listOfChildren.set(n, newChild);
	}

	/**
	 * Sets the value of this ASTNode to the given character. If character is
	 * one of +, -, *, / or ^, the node type will be set accordingly. For all
	 * other characters, the node type will be set to AST_UNKNOWN.
	 * 
	 * @param value
	 *            the character value to which the node's value should be set.
	 */
	public void setCharacter(char value) {
		if (value == '+' || value == '-' || value == '*' || value == '/'
				|| value == '^') {
			operator = value;
			switch (value) {
			case '+':
				type = Constants.AST_PLUS;
				break;
			case '-':
				type = Constants.AST_MINUS;
				break;
			case '*':
				type = Constants.AST_TIMES;
				break;
			case '/':
				type = Constants.AST_DIVIDE;
				break;
			case '^':
				type = Constants.AST_POWER;
				break;
			}
		} else
			type = Constants.AST_UNKNOWN;
	}

	/**
	 * Sets the value of this ASTNode to the given name.
	 * 
	 * The node type will be set (to AST_NAME) only if the ASTNode was
	 * previously an operator (isOperator(node) == true) or number
	 * (isNumber(node) == true). This allows names to be set for AST_FUNCTIONs
	 * and the like.
	 * 
	 * @param name
	 */
	public void setName(String name) {
		variable = identifyVariable(name);
		if (variable == null)
			this.name = name;
	}

	/**
	 * Sets the type of this ASTNode to the given ASTNodeType_t. A side-effect
	 * of doing this is that any numerical values previously stored in this node
	 * are reset to zero.
	 * 
	 * @param type
	 *            the type to which this node should be set
	 */
	public void setType(Constants type) {
		// TODO
		switch (type) {
		case AST_DIVIDE:
			setCharacter('/');
			break;
		case AST_PLUS:
			break;
		case AST_MINUS:
			break;
		case AST_POWER:
			break;
		case AST_TIMES:
			break;
		case AST_NAME:
			break;
		case AST_NAME_TIME:
			break;
		default:
			this.type = type;
			break;
		}
	}

	/**
	 * Sets the value of this ASTNode to the given real (double) and sets the
	 * node type to AST_REAL.
	 * 
	 * This is functionally equivalent to:
	 * 
	 * <pre>
	 * setValue(value, 0);
	 * </pre>
	 * 
	 * @param value
	 *            the double format number to which this node's value should be
	 *            set
	 */
	public void setValue(double value) {
		setValue(value, 0);
		type = Constants.AST_REAL;
	}

	/**
	 * Sets the value of this ASTNode to the given real (double) in two parts:
	 * the mantissa and the exponent. The node type is set to AST_REAL_E.
	 * 
	 * @param mantissa
	 *            the mantissa of this node's real-numbered value
	 * @param exponent
	 *            the exponent of this node's real-numbered value
	 */
	public void setValue(double mantissa, int exponent) {
		type = Constants.AST_REAL_E;
		this.mantissa = mantissa;
		this.exponent = exponent;
	}

	/**
	 * Sets the value of this ASTNode to the given (long) integer and sets the
	 * node type to AST_INTEGER.
	 * 
	 * @param value
	 */
	public void setValue(int value) {
		type = Constants.AST_INTEGER;
		integer = value;
	}

	/**
	 * Sets the value of this ASTNode to the given rational in two parts: the
	 * numerator and denominator. The node type is set to AST_RATIONAL.
	 * 
	 * @param numerator
	 *            the numerator value of the rational
	 * @param denominator
	 *            the denominator value of the rational
	 */
	public void setValue(int numerator, int denominator) {
		type = Constants.AST_RATIONAL;
		this.numerator = numerator;
		this.denominator = denominator;
	}

	/**
	 * 
	 * @param variable
	 */
	public void setVariable(NamedSBase variable) {
		type = Constants.AST_NAME;
		this.variable = variable;
	}

	/**
	 * Swap the children of this ASTNode with the children of that ASTNode.
	 * 
	 * @param that
	 *            the other node whose children should be used to replace this
	 *            node's children
	 */
	public void swapChildren(ASTNode that) {
		LinkedList<ASTNode> swap = that.listOfChildren;
		that.listOfChildren = listOfChildren;
		listOfChildren = swap;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	// @Override
	public String toString() {
		// TODO
		return "";
	}

	/**
	 * try to figure out the meaning of this name.
	 * 
	 * @param id an id indicating a variable of the model.
	 * @return null if no model is available or the model does not contain a
	 *         compartment, species, or parameter wit the given id.
	 */
	private NamedSBase identifyVariable(String id) {
		NamedSBase variable = null;
		if (parentSBMLObject != null && parentSBMLObject.getModel() != null) {
			Model m = parentSBMLObject.getModel();
			variable = m.getCompartment(id);
			if (variable == null)
				variable = m.getSpecies(id);
			if (variable == null)
				variable = m.getParameter(id);
		}
		return variable;
	}
}
