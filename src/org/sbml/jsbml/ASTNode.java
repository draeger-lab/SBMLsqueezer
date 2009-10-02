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
package org.sbml.jsbml;

import java.util.Enumeration;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.swing.tree.TreeNode;

/**
 * A node in the Abstract Syntax Tree (AST) representation of a mathematical
 * expression.
 * 
 * @author Andreas Dr&auml;ger <a
 *         href="mailto:andreas.draeger@uni-tuebingen.de">
 *         andreas.draeger@uni-tuebingen.de</a>
 * 
 */
public class ASTNode implements TreeNode {

	/**
	 * 
	 * @author Andreas Dr&auml;ger <a
	 *         href="mailto:andreas.draeger@uni-tuebingen.de"
	 *         >andreas.draeger@uni-tuebingen.de</a>
	 * 
	 */
	public static enum Type {
		/**
		 * 
		 */
		CONSTANT_E,
		/**
		 * 
		 */
		CONSTANT_FALSE,
		/**
		 * 
		 */
		CONSTANT_PI,
		/**
		 * 
		 */
		CONSTANT_TRUE,
		/**
		 * 
		 */
		DIVIDE,
		/**
		 * 
		 */
		FUNCTION,
		/**
		 * 
		 */
		FUNCTION_ABS,
		/**
		 * 
		 */
		FUNCTION_ARCCOS,
		/**
		 * 
		 */
		FUNCTION_ARCCOSH,
		/**
		 * 
		 */
		FUNCTION_ARCCOT,
		/**
		 * 
		 */
		FUNCTION_ARCCOTH,
		/**
		 * 
		 */
		FUNCTION_ARCCSC,
		/**
		 * 
		 */
		FUNCTION_ARCCSCH,
		/**
		 * 
		 */
		FUNCTION_ARCSEC,
		/**
		 * 
		 */
		FUNCTION_ARCSECH,
		/**
		 * 
		 */
		FUNCTION_ARCSIN,
		/**
		 * 
		 */
		FUNCTION_ARCSINH,
		/**
		 * 
		 */
		FUNCTION_ARCTAN,
		/**
		 * 
		 */
		FUNCTION_ARCTANH,
		/**
		 * 
		 */
		FUNCTION_CEILING,
		/**
		 * 
		 */
		FUNCTION_COS,
		/**
		 * 
		 */
		FUNCTION_COSH,
		/**
		 * 
		 */
		FUNCTION_COT,
		/**
		 * 
		 */
		FUNCTION_COTH,
		/**
		 * 
		 */
		FUNCTION_CSC,
		/**
		 * 
		 */
		FUNCTION_CSCH,
		/**
		 * 
		 */
		FUNCTION_DELAY,
		/**
		 * 
		 */
		FUNCTION_EXP,
		/**
		 * 
		 */
		FUNCTION_FACTORIAL,
		/**
		 * 
		 */
		FUNCTION_FLOOR,
		/**
		 * 
		 */
		FUNCTION_LN,
		/**
		 * 
		 */
		FUNCTION_LOG,
		/**
		 * 
		 */
		FUNCTION_PIECEWISE,
		/**
		 * 
		 */
		FUNCTION_POWER,
		/**
		 * 
		 */
		FUNCTION_ROOT,
		/**
		 * 
		 */
		FUNCTION_SEC,
		/**
		 * 
		 */
		FUNCTION_SECH,
		/**
		 * 
		 */
		FUNCTION_SIN,
		/**
		 * 
		 */
		FUNCTION_SINH,
		/**
		 * 
		 */
		FUNCTION_TAN,
		/**
		 * 
		 */
		FUNCTION_TANH,
		/**
		 * 
		 */
		INTEGER,
		/**
		 * 
		 */
		LAMBDA,
		/**
		 * 
		 */
		LOGICAL_AND,
		/**
		 * 
		 */
		LOGICAL_NOT,
		/**
		 * 
		 */
		LOGICAL_OR,
		/**
		 * 
		 */
		LOGICAL_XOR,
		/**
		 * 
		 */
		MINUS,
		/**
		 * 
		 */
		NAME,
		/**
		 * 
		 */
		NAME_TIME,
		/**
		 * 
		 */
		PLUS,
		/**
		 * 
		 */
		POWER,
		/**
		 * 
		 */
		RATIONAL,
		/**
		 * 
		 */
		REAL,
		/**
		 * 
		 */
		REAL_E,
		/**
		 * 
		 */
		RELATIONAL_EQ,
		/**
		 * 
		 */
		RELATIONAL_GEQ,
		/**
		 * 
		 */
		RELATIONAL_GT,
		/**
		 * 
		 */
		RELATIONAL_LEQ,
		/**
		 * 
		 */
		RELATIONAL_LT,
		/**
		 * 
		 */
		RELATIONAL_NEQ,
		/**
		 * 
		 */
		TIMES,
		/**
		 * 
		 */
		UNKNOWN
	}

	/**
	 * 
	 * @param operator
	 * @param ast
	 * @return
	 */
	private static ASTNode arithmethicOperation(Type operator, ASTNode... ast) {
		LinkedList<ASTNode> astList = new LinkedList<ASTNode>();
		if (ast != null)
			for (ASTNode node : ast) {
				if (node != null
						&& !(operator == Type.TIMES && node.isOne() && ast.length > 1))
					astList.add(node);
			}
		if (astList.size() == 0)
			return null;
		if (astList.size() == 1)
			return astList.getFirst().clone();
		if (operator == Type.PLUS || operator == Type.MINUS
				|| operator == Type.TIMES || operator == Type.DIVIDE
				|| operator == Type.POWER) {
			MathContainer mc = astList.getFirst().parentSBMLObject;
			ASTNode arithmetic = new ASTNode(operator, mc);
			for (ASTNode nodes : astList) {
				arithmetic.addChild(nodes);
				setParentSBMLObject(nodes, mc, 0);
			}
			return arithmetic;
		} else
			throw new RuntimeException(
					new IllegalArgumentException(
							"The operator must be one of the following constants: PLUS, MINUS, TIMES, DIVIDE, or POWER."));
	}

	/**
	 * Creates a new ASTNode of type MINUS and adds the given nodes as children
	 * 
	 * @param parent
	 * @param ast
	 * @return
	 */
	public static ASTNode diff(ASTNode... ast) {
		return arithmethicOperation(Type.MINUS, ast);
	}

	/**
	 * Returns a new ASTNode that represents Euler's constant raised by the
	 * power of the given exponent.
	 * 
	 * @param exponent
	 * @return
	 */
	public static ASTNode exp(ASTNode exponent) {
		ASTNode e = new ASTNode(Type.CONSTANT_E, exponent.getParentSBMLObject());
		return e.raiseByThePowerOf(exponent);
	}

	/**
	 * Creates a new ASTNode of type DIVIDE with the given nodes as children.
	 * 
	 * @param numerator
	 * @param denominator
	 * @return
	 */
	public static ASTNode frac(ASTNode numerator, ASTNode denominator) {
		setParentSBMLObject(denominator, numerator.getParentSBMLObject(), 0);
		numerator.divideBy(denominator);
		return numerator;
	}

	/**
	 * 
	 * @param container
	 * @param numerator
	 * @param denominator
	 * @return
	 */
	public static ASTNode frac(int numerator, ASTNode denominator) {
		return frac(new ASTNode(numerator, denominator.getParentSBMLObject()),
				denominator);
	}

	/**
	 * Creates a new ASTNode that divides two named sbase objects.
	 * 
	 * @param container
	 * @param numerator
	 * @param denominator
	 * @return
	 */
	public static ASTNode frac(MathContainer container, NamedSBase numerator,
			NamedSBase denominator) {
		return frac(new ASTNode(numerator, container), new ASTNode(denominator,
				container));
	}

	/**
	 * Returns a fraction of the two entities.
	 * 
	 * @param container
	 * @param numeratorId
	 * @param denominatorId
	 * @return
	 */
	public static ASTNode frac(MathContainer container, String numeratorId,
			String denominatorId) {
		return frac(new ASTNode(numeratorId, container), new ASTNode(
				denominatorId, container));
	}

	/**
	 * 
	 * @param formula
	 * @return
	 */
	public static ASTNode parseFormula(String formula) {
		// TODO Auto-generated method stub
		throw new Error("Not yet implemented.");
	}

	/**
	 * 
	 * @param basis
	 * @param exponent
	 * @return
	 */
	public static ASTNode pow(ASTNode basis, ASTNode exponent) {
		if (!(exponent.isInteger() && exponent.getInteger() == 1)
				&& !(exponent.getType() == Type.REAL && exponent.getReal() == 1d)) {
			if ((exponent.isInteger() && exponent.getInteger() == 0)
					|| (exponent.getType() == Type.REAL && exponent.getReal() == 0d))
				basis = new ASTNode(1, basis.getParentSBMLObject());
			else {
				setParentSBMLObject(exponent, basis.getParentSBMLObject(), 0);
				basis.raiseByThePowerOf(exponent);
			}
		}
		return basis;
	}

	/**
	 * 
	 * @param basis
	 * @param exponent
	 * @return
	 */
	public static ASTNode pow(ASTNode basis, double exponent) {
		basis.raiseByThePowerOf(exponent);
		return basis;
	}

	/**
	 * 
	 * @param basis
	 * @param exponent
	 * @return
	 */
	public static ASTNode pow(ASTNode basis, int exponent) {
		basis.raiseByThePowerOf(exponent);
		return basis;
	}

	/**
	 * Raises the given basis by the power of the given exponent.
	 * 
	 * @param container
	 * @param basis
	 * @param exponent
	 * @return
	 */
	public static ASTNode pow(MathContainer container, NamedSBase basis,
			NamedSBase exponent) {
		return pow(new ASTNode(basis, container), new ASTNode(exponent,
				container));
	}

	/**
	 * 
	 * @param radicant
	 * @param rootExponent
	 * @return
	 */
	public static ASTNode root(ASTNode rootExponent, ASTNode radicant) {
		ASTNode root = new ASTNode(Type.FUNCTION_ROOT, radicant
				.getParentSBMLObject());
		root.addChild(rootExponent);
		root.addChild(radicant);
		setParentSBMLObject(rootExponent, radicant.getParentSBMLObject(), 0);
		return root;
	}

	/**
	 * set the Parent of the node and its children to the given value
	 * 
	 * @param node
	 * @param parent
	 */
	private static void setParentSBMLObject(ASTNode node, MathContainer parent,
			int depth) {
		node.parentSBMLObject = parent;
		for (ASTNode child : node.listOfNodes)
			setParentSBMLObject(child, parent, depth + 1);
	}

	/**
	 * 
	 * @param radicand
	 * @return
	 */
	public static ASTNode sqrt(ASTNode radicand) {
		return root(new ASTNode(2, radicand.getParentSBMLObject()), radicand);
	}

	/**
	 * Creates a AstNode of type Plus with the given nodes as children
	 * 
	 * @param parent
	 * @param ast
	 * @return
	 */
	public static ASTNode sum(ASTNode... ast) {
		return arithmethicOperation(Type.PLUS, ast);
	}

	/**
	 * Sum of several NamedSBase objects.
	 * 
	 * @param parent
	 * @param sbase
	 * @return
	 */
	public static ASTNode sum(MathContainer parent, NamedSBase... sbase) {
		ASTNode elements[] = new ASTNode[sbase.length];
		for (int i = 0; i < sbase.length; i++)
			elements[i] = new ASTNode(sbase[i], parent);
		return sum(elements);
	}

	/**
	 * Creates an ASTNode of type times and adds the given nodes as children.
	 * 
	 * @param ast
	 * @return
	 */
	public static ASTNode times(ASTNode... ast) {
		return arithmethicOperation(Type.TIMES, ast);
	}

	/**
	 * Multiplication of several NamedSBase objects.
	 * 
	 * @param parent
	 * @param sbase
	 * @return
	 */
	public static ASTNode times(MathContainer parent, NamedSBase... sbase) {
		ASTNode elements[] = new ASTNode[sbase.length];
		for (int i = 0; i < sbase.length; i++)
			elements[i] = new ASTNode(sbase[i], parent);
		return times(elements);
	}

	private int denominator;

	private int exponent;

	/**
	 * Child nodes.
	 */
	private LinkedList<ASTNode> listOfNodes;

	private double mantissa;

	/**
	 * If no NamedSBase object exists or can be identified when setName() is
	 * called, the given name is stored in this field.
	 */
	private String name;

	/**
	 * This value stores the numerator if this.isRational() is true, or the
	 * value of an integer if this.isInteger() is true.
	 */
	private int numerator;

	/**
	 * important for the TreeNode interface.
	 */
	private ASTNode parent;

	/**
	 * The container that holds this ASTNode.
	 */
	MathContainer parentSBMLObject;

	/**
	 * The type of this ASTNode.
	 */
	private Type type;

	private NamedSBase variable;

	/**
	 * Copy constructor; Creates a deep copy of the given ASTNode.
	 * 
	 * @param astNode
	 *            the ASTNode to be copied.
	 */
	public ASTNode(ASTNode astNode) {
		this(astNode.getParentSBMLObject());
		setType(astNode.getType());
		this.denominator = astNode.denominator;
		this.exponent = astNode.exponent;
		this.mantissa = astNode.mantissa;
		this.name = astNode.name == null ? null : new String(astNode.name);
		this.variable = astNode.variable;
		this.numerator = astNode.numerator;
		for (ASTNode child : astNode.listOfNodes)
			this.listOfNodes.add(child.clone());
	}

	/**
	 * 
	 * @param real
	 * @param parent
	 */
	public ASTNode(double real, MathContainer parent) {
		this(Type.REAL, parent);
		setValue(real);
	}

	/**
	 * 
	 * @param integer
	 * @param parent
	 */
	public ASTNode(int integer, MathContainer parent) {
		this(Type.INTEGER, parent);
		setValue(integer);
	}

	/**
	 * Creates and returns a new ASTNode.
	 * 
	 * By default, the returned node will have a type of UNKNOWN. The calling
	 * code should set the node type to something else as soon as possible using
	 * setType(int)
	 * 
	 * @param astNode
	 *            the parent SBML object
	 */
	public ASTNode(MathContainer parent) {
		parentSBMLObject = parent;
		listOfNodes = null;
		initDefaults();
	}

	/**
	 * 
	 * @param nsb
	 * @param parent
	 */
	public ASTNode(NamedSBase nsb, MathContainer parent) {
		this(Type.NAME, parent);
		setVariable(nsb);
	}

	/**
	 * 
	 * @param name
	 * @param parent
	 */
	public ASTNode(String name, MathContainer parent) {
		this(Type.NAME, parent);
		setName(name);

	}

	/**
	 * Creates and returns a new ASTNode.
	 * 
	 * By default, the returned node will have a type of UNKNOWN. The calling
	 * code should set the node type to something else as soon as possible using
	 * setType(int)
	 * 
	 * @param type
	 * @param the
	 *            parent SBML object
	 */
	public ASTNode(Type type, MathContainer parent) {
		this(parent);
		setType(type);
	}

	/**
	 * 
	 * @param child
	 */
	public void addChild(ASTNode child) {
		listOfNodes.add(child);
		child.parent = this;
	}

	/**
	 * Creates a new node with the type of this node, moves all children of this
	 * node to this new node, sets the type of this node to the given operator,
	 * adds the new node as left child of this node and the given astnode as the
	 * right child of this node. The parentSBMLObject of the whole resulting
	 * ASTNode is then set to the parent of this node.
	 * 
	 * @param operator
	 *            The new type of this node. This has to be one of the
	 *            following: PLUS, MINUS, TIMES, DIVIDE, or POWER. Otherwise a
	 *            runtime error is thrown.
	 * @param astnode
	 *            The new right child of this node
	 */
	private void arithmeticOperation(Type operator, ASTNode astnode) {
		if (operator == Type.PLUS || operator == Type.MINUS
				|| operator == Type.TIMES || operator == Type.DIVIDE
				|| operator == Type.POWER || operator == Type.FUNCTION_ROOT) {
			if (astnode.isZero() && operator == Type.DIVIDE)
				throw new RuntimeException(new IllegalArgumentException(
						"Cannot divide by zero."));
			if (!(astnode.isOne() && (operator == Type.TIMES || operator == Type.DIVIDE))) {
				ASTNode swap = new ASTNode(type, getParentSBMLObject());
				swap.denominator = denominator;
				swap.exponent = exponent;
				swap.mantissa = mantissa;
				swap.name = name;
				swap.numerator = numerator;
				swap.variable = variable;
				swapChildren(swap);
				setType(operator);
				if (operator == Type.FUNCTION_ROOT) {
					addChild(astnode);
					addChild(swap);
				} else {
					addChild(swap);
					addChild(astnode);
				}
				setParentSBMLObject(astnode, getParentSBMLObject(), 0);
			}
		} else
			throw new RuntimeException(
					new IllegalArgumentException(
							"The operator must be one of the following constants: PLUS, MINUS, TIMES, DIVIDE, or POWER."));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.tree.TreeNode#children()
	 */
	public Enumeration<TreeNode> children() {
		return new Enumeration<TreeNode>() {
			private int pos = 0;

			/*
			 * (non-Javadoc)
			 * 
			 * @see java.util.Enumeration#hasMoreElements()
			 */
			public boolean hasMoreElements() {
				return pos < listOfNodes.size();
			}

			/*
			 * (non-Javadoc)
			 * 
			 * @see java.util.Enumeration#nextElement()
			 */
			public ASTNode nextElement() {
				return listOfNodes.get(pos++);
			}
		};
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

	/**
	 * Compiles this astnode and returns the result.
	 * 
	 * @param compiler
	 * @return
	 */
	public Object compile(ASTNodeCompiler compiler) {
		if (isUMinus())
			return compiler.uiMinus(this);
		else if (isSqrt())
			return compiler.sqrt(getChild(getNumChildren() - 1));
		else if (isInfinity())
			return compiler.getPositiveInfinity();
		else if (isNegInfinity())
			return compiler.getNegativeInfinity();
		switch (getType()) {
		/*
		 * Numbers
		 */
		case REAL:
			return compiler.compile(getReal());
		case INTEGER:
			return compiler.compile(getInteger());
			/*
			 * Basic Functions
			 */
		case FUNCTION_LOG: {
			if (getNumChildren() == 2)
				return compiler.log(getLeftChild(), getRightChild());
			else
				return compiler.log(getChild(getNumChildren() - 1));
		}
			/*
			 * Operators
			 */
		case POWER:
			return compiler.pow(getLeftChild(), getRightChild());
		case PLUS:
			return compiler.plus(listOfNodes.toArray(new ASTNode[] {}));
		case MINUS:
			return compiler.minus(listOfNodes.toArray(new ASTNode[] {}));
		case TIMES:
			return compiler.times(listOfNodes.toArray(new ASTNode[] {}));
		case DIVIDE:
			return compiler.frac(getLeftChild(), getRightChild());
		case RATIONAL:
			return compiler.frac(getNumerator(), getDenominator());
		case NAME_TIME:
			return compiler.symbolTime(getName());
		case FUNCTION_DELAY:
			return compiler.functionDelay(getName());
			/*
			 * Names of identifiers: parameters, functions, species etc.
			 */
		case NAME:
			return compiler.compile(variable);
			/*
			 * Type: pi, e, true, false
			 */
		case CONSTANT_PI:
			return compiler.getConstantPi();
		case CONSTANT_E:
			return compiler.getConstantE();
		case CONSTANT_TRUE:
			return compiler.getConstantTrue();
		case CONSTANT_FALSE:
			return compiler.getConstantFalse();
		case REAL_E:
			return compiler.compile(getReal());
			/*
			 * More complicated functions
			 */
		case FUNCTION_ABS:
			return compiler.abs(getChild(getNumChildren() - 1));
		case FUNCTION_ARCCOS:
			return compiler.arccos(getLeftChild());
		case FUNCTION_ARCCOSH:
			return compiler.arccosh(getLeftChild());
		case FUNCTION_ARCCOT:
			return compiler.arccot(getLeftChild());
		case FUNCTION_ARCCOTH:
			return compiler.arccoth(getLeftChild());
		case FUNCTION_ARCCSC:
			return compiler.arccsc(getLeftChild());
		case FUNCTION_ARCCSCH:
			return compiler.arccsch(getLeftChild());
		case FUNCTION_ARCSEC:
			return compiler.arcsec(getLeftChild());
		case FUNCTION_ARCSECH:
			return compiler.arcsech(getLeftChild());
		case FUNCTION_ARCSIN:
			return compiler.arcsin(getLeftChild());
		case FUNCTION_ARCSINH:
			return compiler.arcsinh(getLeftChild());
		case FUNCTION_ARCTAN:
			return compiler.arctan(getLeftChild());
		case FUNCTION_ARCTANH:
			return compiler.arctanh(getLeftChild());
		case FUNCTION_CEILING:
			return compiler.ceiling(getLeftChild());
		case FUNCTION_COS:
			return compiler.cos(getLeftChild());
		case FUNCTION_COSH:
			return compiler.cosh(getLeftChild());
		case FUNCTION_COT:
			return compiler.cot(getLeftChild());
		case FUNCTION_COTH:
			return compiler.coth(getLeftChild());
		case FUNCTION_CSC:
			return compiler.csc(getLeftChild());
		case FUNCTION_CSCH:
			return compiler.csch(getLeftChild());
		case FUNCTION_EXP:
			return compiler.exp(getLeftChild());
		case FUNCTION_FACTORIAL:
			return compiler.factorial(getLeftChild());
		case FUNCTION_FLOOR:
			return compiler.floor(getLeftChild());
		case FUNCTION_LN:
			return compiler.ln(getLeftChild());
		case FUNCTION_POWER:
			return compiler.pow(getLeftChild(), getChild(getNumChildren() - 1));
		case FUNCTION_ROOT:
			ASTNode left = getLeftChild();
			if ((getNumChildren() > 1)
					&& ((left.isInteger() && (left.getInteger() != 2)) || (left
							.isReal() && (left.getReal() != 2d))))
				return compiler.root(getLeftChild(), getRightChild());
			return compiler.sqrt(getChild(getNumChildren() - 1));
		case FUNCTION_SEC:
			return compiler.sec(getLeftChild());
		case FUNCTION_SECH:
			return compiler.sech(getLeftChild());
		case FUNCTION_SIN:
			return compiler.sin(getLeftChild());
		case FUNCTION_SINH:
			return compiler.sinh(getLeftChild());
		case FUNCTION_TAN:
			return compiler.tan(getLeftChild());
		case FUNCTION_TANH:
			return compiler.tanh(getLeftChild());
		case FUNCTION:
			return compiler.function(getName(), listOfNodes.toArray(new ASTNode[]{}));
		case LAMBDA:
			return compiler.lambda(listOfNodes.toArray(new ASTNode[]{}));
		case LOGICAL_AND:
			return compiler.logicalOperation(this);
		case LOGICAL_XOR:
			return compiler.logicalOperation(this);
		case LOGICAL_OR:
			return compiler.logicalOperation(this);
		case LOGICAL_NOT:
			return compiler.logicalNot(getLeftChild());
		case FUNCTION_PIECEWISE:
			return compiler.piecewise(listOfNodes.toArray(new ASTNode[] {}));
		case RELATIONAL_EQ:
			return compiler.relationEqual(getLeftChild(), getRightChild());
		case RELATIONAL_GEQ:
			return compiler.relationGreaterEqual(getLeftChild(),
					getRightChild());
		case RELATIONAL_GT:
			return compiler.relationGraterThan(getLeftChild(), getRightChild());
		case RELATIONAL_NEQ:
			return compiler.relationNotEqual(getLeftChild(), getRightChild());
		case RELATIONAL_LEQ:
			return compiler.relationLessEqual(getLeftChild(), getRightChild());
		case RELATIONAL_LT:
			return compiler.relationLessThan(getLeftChild(), getRightChild());
		default: // UNKNOWN:
			return compiler.unknownASTNode();
		}
	}

	/**
	 * Divides this node through? the given node
	 * 
	 * param ast
	 */
	public ASTNode divideBy(ASTNode ast) {
		arithmeticOperation(Type.DIVIDE, ast);
		return this;
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
			if (isInteger() && ast.isInteger())
				equal &= ast.getInteger() == getInteger();
			if (isName() && ast.isName())
				equal &= ast.getName().equals(getName());
			if (isRational() && ast.isRational())
				equal &= ast.getNumerator() == getNumerator()
						&& ast.getDenominator() == getDenominator();
			if (isReal() && ast.isReal())
				equal &= ast.getReal() == getReal();
			if (ast.getType() == Type.REAL_E && type == Type.REAL_E)
				equal &= ast.getMantissa() == getMantissa()
						&& ast.getExponent() == getExponent();
			if (equal)
				for (ASTNode child : listOfNodes)
					equal &= child.equals(ast);
			return equal;
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.tree.TreeNode#getAllowsChildren()
	 */
	public boolean getAllowsChildren() {
		return !isConstant() && !isInfinity() && !isNumber()
				&& !isNegInfinity() && !isNaN() && !isRational();
	}

	/**
	 * Get the value of this node as a single character. This function should be
	 * called only when ASTNode.getType() is one of PLUS, MINUS, TIMES, DIVIDE
	 * or POWER.
	 * 
	 * @return the value of this ASTNode as a single character
	 */
	public char getCharacter() {
		if (isOperator())
			switch (type) {
			case PLUS:
				return '+';
			case MINUS:
				return '-';
			case TIMES:
				return '*';
			case DIVIDE:
				return '/';
			case POWER:
				return '^';
			default:
				break;
			}
		throw new RuntimeException(new IllegalArgumentException(
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
		return listOfNodes.get(n);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.tree.TreeNode#getChildAt(int)
	 */
	public TreeNode getChildAt(int i) {
		return listOfNodes.get(i);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.tree.TreeNode#getChildCount()
	 */
	public int getChildCount() {
		return getNumChildren();
	}

	/**
	 * Get the value of the denominator of this node. This function should be
	 * called only when getType() == RATIONAL.
	 * 
	 * 
	 * @return the value of the denominator of this ASTNode.
	 */
	public int getDenominator() {
		if (isRational())
			return denominator;
		throw new RuntimeException(
				new IllegalArgumentException(
						"getDenominator() should be called only when getType() == RATIONAL."));
	}

	/**
	 * Get the exponent value of this ASTNode. This function should be called
	 * only when getType() returns REAL_E or REAL.
	 * 
	 * @return the value of the exponent of this ASTNode.
	 */
	public int getExponent() {
		if (type == Type.REAL || type == Type.REAL_E)
			return exponent;
		throw new RuntimeException(
				new IllegalArgumentException(
						"getExponent() should be called only when getType() == REAL_E or REAL"));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.tree.TreeNode#getIndex(javax.swing.tree.TreeNode)
	 */
	public int getIndex(TreeNode node) {
		for (int i = 0; i < listOfNodes.size(); i++) {
			TreeNode n = listOfNodes.get(i);
			if (node.equals(n))
				return i;
		}
		return -1;
	}

	/**
	 * Get the value of this node as an integer. This function should be called
	 * only when getType() == INTEGER.
	 * 
	 * @return the value of this ASTNode as a (long) integer.
	 */
	public int getInteger() {
		if (isInteger())
			return numerator;
		throw new RuntimeException(new IllegalArgumentException(
				"getInteger() should be called only when getType() == INTEGER"));
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
		return listOfNodes;
	}

	/**
	 * Get the mantissa value of this node. This function should be called only
	 * when getType() returns REAL_E or REAL. If getType() returns REAL, this
	 * method is identical to getReal().
	 * 
	 * @return the value of the mantissa of this ASTNode.
	 */
	public double getMantissa() {
		switch (type) {
		case REAL:
			getReal();
		case REAL_E:
			return mantissa;
		default:
			throw new RuntimeException(
					new IllegalArgumentException(
							"getMantissa() should be called only when getType() == REAL or REAL_E"));
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
				new IllegalArgumentException(
						"getName() should be called only when !isNumber() || !isOperator()"));
	}

	/**
	 * Get the number of children that this node has.
	 * 
	 * @return the number of children of this ASTNode, or 0 is this node has no
	 *         children.
	 */
	public int getNumChildren() {
		return listOfNodes.size();
	}

	/**
	 * Get the value of the numerator of this node. This function should be
	 * called only when getType() == RATIONAL.
	 * 
	 * 
	 * @return the value of the numerator of this ASTNode.
	 */
	public int getNumerator() {
		if (isRational())
			return numerator;
		throw new RuntimeException(new IllegalArgumentException(
				"getNumerator() should be called only when isRational()"));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.tree.TreeNode#getParent()
	 */
	public TreeNode getParent() {
		return parent;
	}

	/**
	 * This method is convenient when holding an object nested inside other
	 * objects in an SBML model. It allows direct access to the &lt;model&gt;
	 * 
	 * element containing it.
	 * 
	 * @return Returns the parent SBML object.
	 */
	public MathContainer getParentSBMLObject() {
		return parentSBMLObject;
	}

	/**
	 * Get the real-numbered value of this node. This function should be called
	 * only when isReal() == true.
	 * 
	 * This function performs the necessary arithmetic if the node type is
	 * REAL_E (mantissa^exponent) or RATIONAL (numerator / denominator).
	 * 
	 * @return the value of this ASTNode as a real (double).
	 */
	public double getReal() {
		if (isReal() || type == Type.CONSTANT_E || type == Type.CONSTANT_PI) {
			switch (type) {
			case REAL:
				return mantissa;
			case REAL_E:
				return getMantissa() * Math.pow(10, getExponent());
			case RATIONAL:
				return ((double) getNumerator()) / ((double) getDenominator());
			case CONSTANT_E:
				return Math.E;
			case CONSTANT_PI:
				return Math.PI;
			default:
				break;
			}
		}
		throw new RuntimeException(new IllegalArgumentException(
				"getReal() should be called only when isReal()"));
	}

	/**
	 * Just for testing purposes...
	 * 
	 * @return
	 */
	public Set<NamedSBase> getReferencedNamedSBases() {
		Set<NamedSBase> l = new HashSet<NamedSBase>();
		if (isName()) {
			if (getVariable() != null)
				l.add(getVariable());
			else
				System.err.println("Name of this node is " + getName()
						+ " but no variable is set.");
		}
		for (ASTNode child : listOfNodes)
			l.addAll(child.getReferencedNamedSBases());
		return l;
	}

	public ASTNode getRightChild() {
		return listOfNodes.getLast();
	}

	public Type getType() {
		return type;
	}

	public NamedSBase getVariable() {
		if (isName())
			return variable;
		throw new RuntimeException(
				new IllegalArgumentException(
						"getVariable() should be called only when !isNumber() || !isOperator()"));
	}

	/**
	 * 
	 */
	private void initDefaults() {
		type = Type.UNKNOWN;
		if (listOfNodes == null)
			listOfNodes = new LinkedList<ASTNode>();
		else
			listOfNodes.clear();
		variable = null;
		mantissa = Double.NaN;
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
		listOfNodes.add(n, newChild);
	}

	/**
	 * Predicate returning true (non-zero) if this node has a boolean type (a
	 * logical operator, a relational operator, or the constants true or false).
	 * 
	 * @return true if this ASTNode is a boolean, false otherwise.
	 */
	public boolean isBoolean() {
		return type == Type.CONSTANT_FALSE || type == Type.CONSTANT_TRUE
				|| type == Type.LOGICAL_AND || type == Type.LOGICAL_NOT
				|| type == Type.LOGICAL_OR || type == Type.LOGICAL_XOR;
	}

	/**
	 * Predicate returning true (non-zero) if this node represents a MathML
	 * constant (e.g., true, Pi).
	 * 
	 * @return true if this ASTNode is a MathML constant, false otherwise.
	 */
	public boolean isConstant() {
		return type.toString().startsWith("CONSTANT");
	}

	/**
	 * Predicate returning true (non-zero) if this node represents a MathML
	 * function (e.g., abs()), or an SBML Level 1 function, or a user-defined
	 * function.
	 * 
	 * @return true if this ASTNode is a function, false otherwise.
	 */
	public boolean isFunction() {
		return type.toString().startsWith("FUNCTION");
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
	 * @return true if this ASTNode is of type INTEGER, false otherwise.
	 */
	public boolean isInteger() {
		return type == Type.INTEGER;
	}

	/**
	 * Predicate returning true (non-zero) if this node is a MathML
	 * &lt;lambda&gt;, false (zero) otherwise.
	 * 
	 * @return true if this ASTNode is of type LAMBDA, false otherwise.
	 */
	public boolean isLambda() {
		return type == Type.LAMBDA;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.tree.TreeNode#isLeaf()
	 */
	public boolean isLeaf() {
		return getNumChildren() == 0;
	}

	/**
	 * Predicate returning true (non-zero) if this node represents a log10()
	 * function, false (zero) otherwise. More precisely, this predicate returns
	 * true if the node type is FUNCTION_LOG with two children, the first of
	 * which is an INTEGER equal to 10.
	 * 
	 * @return true if the given ASTNode represents a log10() function, false
	 *         otherwise.
	 */
	public boolean isLog10() {
		return type == Type.FUNCTION_LOG && listOfNodes.size() == 2
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
		return type.toString().startsWith("LOGICAL_");
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
		return type == Type.NAME;
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
	 * Returns true if this astnode represents the number one (either as integer
	 * or as real value).
	 * 
	 * @return
	 */
	public boolean isOne() {
		return (isReal() && getReal() == 1d)
				|| (isInteger() && getInteger() == 1);
	}

	/**
	 * Predicate returning true (non-zero) if this node is a mathematical
	 * operator, meaning, +, -, *, / or ^ (power).
	 * 
	 * @return true if this ASTNode is an operator.
	 */
	public boolean isOperator() {
		return type == Type.PLUS || type == Type.MINUS || type == Type.TIMES
				|| type == Type.DIVIDE || type == Type.POWER;
	}

	/**
	 * Predicate returning true (non-zero) if this node is the MathML
	 * &lt;piecewise&gt; construct, false (zero) otherwise.
	 * 
	 * @return true if this ASTNode is a MathML piecewise function
	 */
	public boolean isPiecewise() {
		return type == Type.FUNCTION_PIECEWISE;
	}

	/**
	 * Predicate returning true (non-zero) if this node represents a rational
	 * number, false (zero) otherwise.
	 * 
	 * @return true if this ASTNode is of type RATIONAL.
	 */
	public boolean isRational() {
		return type == Type.RATIONAL;
	}

	/**
	 * Predicate returning true (non-zero) if this node can represent a real
	 * number, false (zero) otherwise. More precisely, this node must be of one
	 * of the following types: REAL, REAL_E or RATIONAL.
	 * 
	 * @return true if the value of this ASTNode can represented as a real
	 *         number, false otherwise.
	 */
	public boolean isReal() {
		return type == Type.REAL || type == Type.REAL_E
				|| type == Type.RATIONAL;
	}

	/**
	 * Predicate returning true (non-zero) if this node is a MathML relational
	 * operator, meaning ==, >=, >, <, and !=.
	 * 
	 * @return true if this ASTNode is a MathML relational operator, false
	 *         otherwise.
	 */
	public boolean isRelational() {
		return type == Type.RELATIONAL_EQ || type == Type.RELATIONAL_GEQ
				|| type == Type.RELATIONAL_GT || type == Type.RELATIONAL_LEQ
				|| type == Type.RELATIONAL_LT || type == Type.RELATIONAL_NEQ;
	}

	/**
	 * Predicate returning true (non-zero) if this node represents a square root
	 * function, false (zero) otherwise. More precisely, the node type must be
	 * FUNCTION_ROOT with two children, the first of which is an INTEGER node
	 * having value equal to 2.
	 * 
	 * 
	 * @return true if the given ASTNode represents a sqrt() function, false
	 *         otherwise.
	 */
	public boolean isSqrt() {
		return type == Type.FUNCTION_ROOT && listOfNodes.size() == 2
				&& getLeftChild().isInteger()
				&& getLeftChild().getInteger() == 2;
	}

	/**
	 * Predicate returning true (non-zero) if this node is a unary minus
	 * operator, false (zero) otherwise. A node is defined as a unary minus node
	 * if it is of type MINUS and has exactly one child.
	 * 
	 * For numbers, unary minus nodes can be 'collapsed' by negating the number.
	 * In fact, SBML_parseFormula() does this during its parse. However, unary
	 * minus nodes for symbols (NAMES) cannot be 'collapsed', so this predicate
	 * function is necessary.
	 * 
	 * @return true if this ASTNode is a unary minus, false otherwise.
	 */
	public boolean isUMinus() {
		return type == Type.MINUS && getNumChildren() == 1;
	}

	/**
	 * Predicate returning true (non-zero) if this node has an unknown type.
	 * 
	 * 'Unknown' nodes have the type UNKNOWN. Nodes with unknown types will not
	 * appear in an ASTNode tree returned by libSBML based upon valid SBML
	 * input; the only situation in which a node with type UNKNOWN may appear is
	 * immediately after having create a new, untyped node using the ASTNode
	 * constructor. Callers creating nodes should endeavor to set the type to a
	 * valid node type as soon as possible after creating new nodes.
	 * 
	 * @return true if this ASTNode is of type UNKNOWN, false otherwise.
	 */
	public boolean isUnknown() {
		return type == Type.UNKNOWN;
	}

	/**
	 * Returns true if this astnode represents the number zero (either as
	 * integer or as real value).
	 * 
	 * @return
	 */
	public boolean isZero() {
		return (isReal() && getReal() == 0d)
				|| (isInteger() && getInteger() == 0);
	}

	/**
	 * subtracts the given ASTNode from this node
	 * 
	 * @param ast
	 */

	public ASTNode minus(ASTNode ast) {
		arithmeticOperation(Type.MINUS, ast);
		return this;
	}

	/**
	 * Substracts the given real number from this node.
	 * 
	 * @param real
	 */
	public ASTNode minus(double real) {
		minus(new ASTNode(real, getParentSBMLObject()));
		return this;
	}

	/**
	 * Substracts the given integer from this node.
	 * 
	 * @param integer
	 */
	public ASTNode minus(int integer) {
		minus(new ASTNode(integer, getParentSBMLObject()));
		return this;
	}

	/**
	 * multiplies this ASTNode with the given node
	 * 
	 * @param ast
	 */
	public ASTNode multiplyWith(ASTNode ast) {
		arithmeticOperation(Type.TIMES, ast);
		return this;
	}

	public ASTNode multiplyWith(ASTNode... nodes) {
		for (ASTNode node : nodes)
			multiplyWith(node);
		return this;
	}

	/**
	 * 
	 * @param nsb
	 * @return
	 */
	public ASTNode multiplyWith(NamedSBase nsb) {
		return multiplyWith(new ASTNode(nsb, getParentSBMLObject()));
	}

	/**
	 * adds a given node to this node
	 * 
	 * @param ast
	 */

	public ASTNode plus(ASTNode ast) {
		arithmeticOperation(Type.PLUS, ast);
		return this;
	}

	/**
	 * 
	 * @param real
	 */
	public ASTNode plus(double real) {
		plus(new ASTNode(real, getParentSBMLObject()));
		return this;
	}

	/**
	 * 
	 * @param integer
	 */
	public ASTNode plus(int integer) {
		plus(new ASTNode(integer, getParentSBMLObject()));
		return this;
	}

	/**
	 * 
	 * @param nsb
	 * @return
	 */
	public ASTNode plus(NamedSBase nsb) {
		plus(new ASTNode(nsb, getParentSBMLObject()));
		return this;
	}

	/**
	 * Adds the given node as a child of this ASTNode. This method adds child
	 * nodes from right to left.
	 * 
	 * @param child
	 */
	public void prependChild(ASTNode child) {
		listOfNodes.addLast(child);
	}

	/**
	 * 
	 * @param exponent
	 */
	public ASTNode raiseByThePowerOf(ASTNode exponent) {
		arithmeticOperation(Type.POWER, exponent);
		return this;
	}

	/**
	 * 
	 * @param exponent
	 */
	public ASTNode raiseByThePowerOf(double exponent) {
		if (exponent == 0d) {
			setValue(1);
			listOfNodes.clear();
		} else if (exponent != 1d)
			raiseByThePowerOf(new ASTNode(exponent, getParentSBMLObject()));
		return this;
	}

	/**
	 * Reduces this ASTNode to a binary tree, e.g., if the formula in this
	 * ASTNode is and(x, y, z) then the formula of the reduced node would be
	 * and(and(x, y), z)
	 */
	public void reduceToBinary() {
		if (getNumChildren() > 2) {
			int i;
			switch (type) {
			case PLUS:
				ASTNode plus = new ASTNode(Type.PLUS, parentSBMLObject);
				for (i = getNumChildren() - 1; i >= 0; i--)
					plus.addChild(listOfNodes.remove(i));
				addChild(plus);
				break;
			case MINUS:
				// TODO
				break;
			case TIMES:
				ASTNode times = new ASTNode(Type.TIMES, parentSBMLObject);
				for (i = getNumChildren() - 1; i >= 0; i--)
					times.addChild(listOfNodes.remove(i));
				addChild(times);
				break;
			case DIVIDE:
				// TODO
				break;
			case LOGICAL_AND:
				ASTNode and = new ASTNode(Type.LOGICAL_AND, parentSBMLObject);
				for (i = getNumChildren() - 1; i >= 0; i--)
					and.addChild(listOfNodes.remove(i));
				addChild(and);
				break;
			case LOGICAL_OR:
				ASTNode or = new ASTNode(Type.LOGICAL_OR, parentSBMLObject);
				for (i = getNumChildren() - 1; i >= 0; i--)
					or.addChild(listOfNodes.remove(i));
				addChild(or);
				break;
			case LOGICAL_NOT:
				// TODO
				break;
			case LOGICAL_XOR:
				// TODO
				break;
			default:
				// TODO
				break;
			}
		}
		// recursively restructure this tree.
		for (ASTNode child : listOfNodes)
			child.reduceToBinary();
	}

	/**
	 * Returns true if this astnode or one of its descendents contains some
	 * identifier with the given id. This method can be used to scan a formula
	 * and for a specific parameter or species and detect weather this component
	 * is used by this formula. This search is done using a DFS.
	 * 
	 * @param id
	 * @return
	 */
	public boolean refersTo(String id) {
		if (isName() && getName().equals(id))
			return true;
		boolean childContains = false;
		for (ASTNode child : listOfNodes)
			childContains |= child.refersTo(id);
		return childContains;
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
		return listOfNodes.remove(n);
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
		for (ASTNode child : listOfNodes) {
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
		return listOfNodes.set(n, newChild);
	}

	/**
	 * Sets the value of this ASTNode to the given character. If character is
	 * one of +, -, *, / or ^, the node type will be set accordingly. For all
	 * other characters, the node type will be set to UNKNOWN.
	 * 
	 * @param value
	 *            the character value to which the node's value should be set.
	 */
	public void setCharacter(char value) {
		switch (value) {
		case '+':
			type = Type.PLUS;
			break;
		case '-':
			type = Type.MINUS;
			break;
		case '*':
			type = Type.TIMES;
			break;
		case '/':
			type = Type.DIVIDE;
			break;
		case '^':
			type = Type.POWER;
			break;
		default:
			type = Type.UNKNOWN;
			break;
		}
	}

	/**
	 * Sets the value of this ASTNode to the given name.
	 * 
	 * The node type will be set (to NAME) only if the ASTNode was previously an
	 * operator (isOperator(node) == true) or number (isNumber(node) == true).
	 * This allows names to be set for FUNCTIONs and the like.
	 * 
	 * @param name
	 */
	public void setName(String name) {
		variable = null;
		Model m = getParentSBMLObject().getModel();
		if (m != null)
			variable = m.findNamedSBase(name);
		if (variable == null)
			this.name = name;
		if (type != Type.NAME && type != Type.FUNCTION)
			type = variable == null ? Type.FUNCTION : Type.NAME;
	}

	/**
	 * Sets the type of this ASTNode to the given ASTNodeType_t. A side-effect
	 * of doing this is that any numerical values previously stored in this node
	 * are reset to zero.
	 * 
	 * @param type
	 *            the type to which this node should be set
	 */
	public void setType(Type type) {
		String sType = type.toString();
		if (sType.startsWith("NAME") || sType.startsWith("CONSTANT"))
			initDefaults();
		if (type == Type.NAME_TIME)
			name = "time";
		else if (type == Type.FUNCTION_DELAY) {
			name = "delay";
			initDefaults();
		}
		this.type = type;
	}

	/**
	 * Sets the value of this ASTNode to the given real (double) and sets the
	 * node type to REAL.
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
		type = Type.REAL;
	}

	/**
	 * Sets the value of this ASTNode to the given real (double) in two parts:
	 * the mantissa and the exponent. The node type is set to REAL_E.
	 * 
	 * @param mantissa
	 *            the mantissa of this node's real-numbered value
	 * @param exponent
	 *            the exponent of this node's real-numbered value
	 */
	public void setValue(double mantissa, int exponent) {
		type = Type.REAL_E;
		this.mantissa = mantissa;
		this.exponent = exponent;
	}

	/**
	 * Sets the value of this ASTNode to the given (long) integer and sets the
	 * node type to INTEGER.
	 * 
	 * @param value
	 */
	public void setValue(int value) {
		type = Type.INTEGER;
		numerator = value;
		denominator = 1;
	}

	/**
	 * Sets the value of this ASTNode to the given rational in two parts: the
	 * numerator and denominator. The node type is set to RATIONAL.
	 * 
	 * @param numerator
	 *            the numerator value of the rational
	 * @param denominator
	 *            the denominator value of the rational
	 */
	public void setValue(int numerator, int denominator) {
		type = Type.RATIONAL;
		this.numerator = numerator;
		this.denominator = denominator;
	}

	/**
	 * 
	 * @param variable
	 */
	public void setVariable(NamedSBase variable) {
		type = Type.NAME;
		this.variable = variable;
	}

	/**
	 * Applies the square root function on this syntax tree and returns the
	 * resulting tree.
	 */
	public ASTNode sqrt() {
		arithmeticOperation(Type.FUNCTION_ROOT, new ASTNode(2,
				getParentSBMLObject()));
		return this;
	}

	/**
	 * Swap the children of this ASTNode with the children of that ASTNode.
	 * 
	 * @param that
	 *            the other node whose children should be used to replace this
	 *            node's children
	 */
	public void swapChildren(ASTNode that) {
		LinkedList<ASTNode> swap = that.listOfNodes;
		that.listOfNodes = listOfNodes;
		listOfNodes = swap;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	// @Override
	public String toString() {
		// TODO
		if (isInteger())
			return Integer.toString(getInteger());
		else if (isReal())
			return Double.toString(getReal());
		else if (isOperator())
			return Character.toString(getCharacter());
		else if (isRelational())
			switch (type) {
			case RELATIONAL_EQ:
				return Character.toString('=');
			case RELATIONAL_GEQ:
				return ">=";
			case RELATIONAL_GT:
				return Character.toString('>');
			case RELATIONAL_LEQ:
				return "<=";
			case RELATIONAL_LT:
				return Character.toString('<');
			case RELATIONAL_NEQ:
				return "!=";
			}
		else if (type.toString().startsWith("FUNCTION"))
			return type.toString().substring(9).toLowerCase();
		else
			switch (type) {
			case NAME_TIME:
				return type.toString().substring(4).toLowerCase();
			default:
				break;
			}
		return isName() ? getName() : getType().toString();
	}

}
