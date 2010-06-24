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
 *  along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.sbml.squeezer.math;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import org.sbml.jsbml.ASTNode;
import org.sbml.jsbml.AlgebraicRule;
import org.sbml.jsbml.AssignmentRule;
import org.sbml.jsbml.FunctionDefinition;
import org.sbml.jsbml.MathContainer;
import org.sbml.jsbml.Model;
import org.sbml.jsbml.Rule;
import org.sbml.jsbml.ASTNode.Type;

/**
 * This class converts the algebraic rules of a model to assignment rules based
 * on the given matching
 * 
 * @author Alexander D&ouml;rr
 * @since 1.4
 */
public class AlgebraicRuleConverter {

	/**
	 * 
	 * @author Alexander D&ouml;rr
	 * @since 1.4
	 */
	private class EquationObject {
		/**
		 * 
		 */
		private ASTNode node;
		/**
		 * 
		 */
		private boolean isNegative;

		public EquationObject(ASTNode node, boolean isNegative) {
			this.node = node;
			this.isNegative = isNegative;
		}

		public ASTNode getNode() {
			return node;
		}

		public boolean isNegative() {
			return isNegative;
		}

	}

	/**
	 * HashMap representing the current matching with value of the left node ->
	 * value of the right node
	 */
	private HashMap<String, String> matching;
	/**
	 * The given SBML model
	 */
	private Model model;
	/**
	 * ASTNodes for the node of the variable an algebraic refers to and its
	 * parent
	 */
	private ASTNode variableNodeParent, variableNode;
	/**
	 * A boolean that states if the variable of an algebraic rule is linked
	 * additive
	 */
	private boolean additive = true;
	/**
	 * A boolean that states if the variable of an algebraic rule can remain on
	 * its side
	 */
	private boolean remainOnSide = true;
	/**
	 * The depth of nesting of the current analysed MathML expression
	 */
	private int nestingDepth;
	/**
	 * The depth of nesting of the current analysed MathML expression
	 */
	private ArrayList<ArrayList<EquationObject>> equationObjects;
	/**
	 * The container that holds the current rule.
	 */
	private MathContainer pso;

	/**
	 * Creates a new AlgebraicRuleConverter for the given matching and model
	 * 
	 * @param model
	 */
	public AlgebraicRuleConverter(HashMap<String, String> matching, Model model) {
		this.matching = matching;
		this.model = model;
	}

	/**
	 * Creates an assignment rule out of the given ASTNode and the Id of its
	 * algebraic rule
	 * 
	 * @param node
	 * @param ruleId
	 * @return
	 */
	private AssignmentRule createAssignmentRule(ASTNode node, String ruleId) {
		String variable = new String();
		AssignmentRule as = null;

		// Search for the corresponding variable in the matching
		variable = matching.get(ruleId);

		// Evaluate and reorganize the equation of the given ASTNode
		if (variable.length() > 0) {
			this.variableNodeParent = null;
			this.variableNode = null;
			System.out.println("before: " + node.toFormula());
			System.out.println("variable: " + variable);
			as = new AssignmentRule();
			as.setVariable(variable);
			setNodeWithVariable(node, variable);
			nestingDepth = 0;
			evaluateEquation(variableNodeParent);
			System.out.println("nesting depth: " + nestingDepth);

			// old
			// as.setMath(reorganizeEquation(node));
			// ----

			// new
			equationObjects = new ArrayList<ArrayList<EquationObject>>();
			equationObjects.add(new ArrayList<EquationObject>());
			equationObjects.add(new ArrayList<EquationObject>());

			deleteVariable();
			sortEquationObjects(node, false, false, false);
			as.setMath(buildEquation());
			// ----

			System.out.println("after: " + as.getMath().toFormula());
		}

		return as;
	}

	/**
	 * Checks if the Variable has to be moved to the other side of the equation
	 * or not and if its connection to the eqution is additiv or multiplicative.
	 * 
	 * 
	 * @param node
	 * @return
	 */
	private void evaluateEquation(ASTNode node) {
		if (node != null) {
			if (node.getType() == Type.TIMES) {
				if (node.getNumChildren() == 2) {
					if (node.getLeftChild().isMinusOne()
							|| node.getRightChild().isMinusOne()) {
						remainOnSide = false;
					} else {
						additive = false;
						nestingDepth++;
					}

				} else {
					additive = false;
					nestingDepth++;
				}

			} else if (node.getType() == Type.DIVIDE) {
				additive = false;
				remainOnSide = false;
				nestingDepth++;
			}

			evaluateEquation((ASTNode) node.getParent());
		}

	}

	/**
	 * Creates a list an assignment rule for every algebraic rule in the given
	 * model
	 * 
	 * @return
	 */
	public ArrayList<AssignmentRule> getAssignmentRules() {
		ArrayList<AssignmentRule> assignmentRules = new ArrayList<AssignmentRule>();
		AssignmentRule as;
		if (matching != null) {
			for (Map.Entry<String, String> entry : matching.entrySet()) {
				System.out.println(entry.getKey() + " -> " + entry.getValue());
			}
		} else
			System.out.println("no matching found");

		// create for every algebraic rule an adequate assignment rule
		for (int i = 0; i < model.getNumRules(); i++) {
			Rule r = model.getRule(i);
			if (r instanceof AlgebraicRule) {
				AlgebraicRule ar = (AlgebraicRule) r;
				ASTNode node = ar.getMath().clone();

				// substitute function definitions
				if (model.getNumFunctionDefinitions() > 0) {
					node = substituteFunctions(node, 0);
				}
				pso = node.getParentSBMLObject();
				as = createAssignmentRule(node, ar.getMetaId());

				// whenn assignment rule created add to the list
				if (as != null) {
					assignmentRules.add(as);
					as = null;
				}
			}
		}

		return assignmentRules;
	}

	/**
	 * Return the other child of the given nodes parent
	 * 
	 * @param node
	 * @return
	 */
	private ASTNode getOtherChild(ASTNode node) {
		ASTNode parent = (ASTNode) node.getParent();
		if (parent.getLeftChild() == node) {
			return parent.getRightChild();
		} else
			return parent.getLeftChild();

	}

	/**
	 * Takes the equation stored in node an reorganizes it on the basis of the
	 * evaluation of this equation and the variable
	 * 
	 * @param node
	 * @return
	 */
	private ASTNode reorganizeEquation(ASTNode node) {
		ASTNode timesNode, valueNode, divideNode, parent, withVariable, rest;
		int index;

		System.out.println("reorganizing equation...");
		System.out.println("additive: " + additive);
		System.out.println("remainOnSide: " + remainOnSide);

		if (additive) {
			if (!remainOnSide) {
				index = variableNodeParent.getParent().getIndex(
						variableNodeParent);
				parent = (ASTNode) variableNodeParent.getParent();
				parent.removeChild(index);

				return node;

			} else {
				index = variableNodeParent.getParent().getIndex(
						variableNodeParent);
				parent = (ASTNode) variableNodeParent.getParent();
				parent.removeChild(index);
				timesNode = new ASTNode(Type.TIMES, null);
				valueNode = new ASTNode(-1, null);
				timesNode.addChild(node);
				timesNode.addChild(valueNode);

				return timesNode;
			}
		} else {
			timesNode = new ASTNode(Type.TIMES, null);
			valueNode = new ASTNode(-1, null);
			divideNode = new ASTNode(Type.DIVIDE, null);
			parent = (ASTNode) variableNodeParent.getParent();
			withVariable = getOtherChild(variableNode);
			rest = getOtherChild(variableNodeParent);
			index = variableNodeParent.getParent().getIndex(variableNodeParent);

			parent = (ASTNode) variableNodeParent.getParent();
			parent.removeChild(index);

			if (remainOnSide) {
				timesNode.addChild(rest);
				timesNode.addChild(valueNode);
				divideNode.addChild(timesNode);
				divideNode.addChild(withVariable);

				return divideNode;

			} else {
				timesNode.addChild(rest);
				timesNode.addChild(valueNode);
				divideNode.addChild(withVariable);
				divideNode.addChild(timesNode);
				return divideNode;

			}
		}
	}

	/**
	 * Replaces the names of given ASTNode's childern with the value stored in
	 * the given HashMaps if there is an entry in any of the HashMaps
	 * 
	 * 
	 * @param node
	 * @param varibales
	 */
	private void replaceNames(ASTNode node, Map<String, String> varibales,
			Map<String, Double> numberHash, Map<String, ASTNode> nodeHash) {

		if (node.isName()) {
			if (varibales.get(node.getName()) != null) {
				node.setName(varibales.get(node.getName()));
			} else if (numberHash.get(node.getName()) != null) {
				node.setValue(numberHash.get(node.getName()));
			} else if (nodeHash.get(node.getName()) != null) {
				ASTNode parent = (ASTNode) node.getParent();
				int index = parent.getIndex(node);
				parent.replaceChild(index, nodeHash.get(node.getName()));
			}
		}
		// proceed with the children
		for (int i = 0; i < node.getNumChildren(); i++) {
			replaceNames(node.getChild(i), varibales, numberHash, nodeHash);
		}
	}

	private void sortEquationObjects(ASTNode node, boolean plus, boolean times,
			boolean divide) {

		if (node.isOperator()) {
			if (node.getType() == Type.PLUS) {

				for (int i = 0; i < node.getNumChildren(); i++) {
					sortEquationObjects(node.getChild(i), true, false, false);
				}
			}
			else if (node.getType() == Type.MINUS) {

				for (int i = 0; i < node.getNumChildren(); i++) {
					sortEquationObjects(node.getChild(i), true, true, false);
				}
			}

			else if (node.getType() == Type.TIMES) {
				if (node.getNumChildren() == 2) {
					if (node.getLeftChild().isMinusOne()
							&& !node.getRightChild().isMinusOne()) {
						sortEquationObjects(node.getRightChild(), true, true,
								false);
					}

					else if (node.getLeftChild().isMinusOne()
							&& !node.getRightChild().isMinusOne()) {
						sortEquationObjects(node.getLeftChild(), true, true,
								false);
					} else {
						for (int i = 0; i < node.getNumChildren(); i++) {
							sortEquationObjects(node.getChild(i), false, true,
									false);
						}
					}

				} else {
					// for (int i = 0; i < node.getNumChildren(); i++) {
					// sortEquationObjects(node.getChild(i), false, true,
					// false);
					// }
					equationObjects.get(1).add(
							new EquationObject(node.clone(), false));
				}

			}
		} else {
			if (plus && times) {
				equationObjects.get(0).add(
						new EquationObject(node.clone(), true));
			} else if (plus) {
				equationObjects.get(0).add(
						new EquationObject(node.clone(), false));

			} else if (times) {

			}
		}

	}

	private void deleteVariable() {
		int index;
		if (variableNodeParent.getNumChildren() == 2 && variableNodeParent.getType() ==  Type.TIMES) {
			if (variableNodeParent.getLeftChild().isMinusOne()
					|| variableNodeParent.getRightChild().isMinusOne()) {

				index = variableNodeParent.getParent().getIndex(
						variableNodeParent);
				variableNodeParent.getParent().removeChild(index);
			} else {
				index = variableNodeParent.getIndex(variableNode);
				variableNodeParent.removeChild(index);
			}

		} else {
			index = variableNodeParent.getIndex(variableNode);
			variableNodeParent.removeChild(index);
		}
	}

	private ASTNode buildEquation() {
		System.out.println("rebuilding equation...");
		System.out.println("additive: " + additive);
		System.out.println("remainOnSide: " + remainOnSide);
		EquationObject eo;
		ArrayList<EquationObject> addition = equationObjects.get(0);
		ArrayList<EquationObject> multiplication = equationObjects.get(1);
		ASTNode add = new ASTNode(Type.PLUS, pso);
		ASTNode multiply = new ASTNode(Type.TIMES, pso);
		ASTNode divide = new ASTNode(Type.DIVIDE, pso);
		ASTNode node = null;
		if (additive) {
			if (!remainOnSide) {
				ASTNode minus;
				for (int i = 0; i < addition.size(); i++) {
					eo = addition.get(i);
					if (eo.isNegative()) {
						minus = new ASTNode(Type.MINUS, pso);
						minus.addChild(eo.getNode());
						add.addChild(minus);
					} else {
						add.addChild(eo.getNode());
					}

				}
				node = add;
				if (multiplication.size() > 0) {
					for (int i = 0; i < multiplication.size(); i++) {
						multiply.addChild(multiplication.get(i).getNode());
					}
					multiply.addChild(add);

					node = multiply;
				}

			} else {
				ASTNode minus;
				for (int i = 0; i < addition.size(); i++) {
					eo = addition.get(i);
					if (eo.isNegative()) {
						add.addChild(eo.getNode());
					} else {
						minus = new ASTNode(Type.MINUS, pso);
						minus.addChild(eo.getNode());
						add.addChild(minus);
					}
				}

				node = add;
				if (multiplication.size() > 0) {

					if (multiplication.size() == 1) {

						for (int i = 0; i < multiplication.size(); i++) {
							multiply.addChild(multiplication.get(i).getNode());
						}
						divide.addChild(add);
						divide.addChild(multiply);
					} else {
						divide.addChild(add);
						divide.addChild(multiplication.get(0).getNode());
					}

					node = divide;
				}

			}
		} else {
			if (!remainOnSide) {
				ASTNode minus;
				for (int i = 0; i < addition.size(); i++) {
					eo = addition.get(i);
					if (eo.isNegative()) {
						minus = new ASTNode(Type.MINUS, pso);
						minus.addChild(eo.getNode());
						add.addChild(minus);
					} else {
						add.addChild(eo.getNode());
					}

				}
				node = add;
				if (multiplication.size() > 0) {

					if (multiplication.size() == 1) {

						divide.addChild(multiplication.get(0).getNode());
						divide.addChild(add);
					} else {

						for (int i = 0; i < multiplication.size(); i++) {
							multiply.addChild(multiplication.get(i).getNode());
						}
						divide.addChild(multiply);
						divide.addChild(add);

					}

					node = divide;
				}

			}

			else {
				ASTNode minus;
				for (int i = 0; i < addition.size(); i++) {
					eo = addition.get(i);
					if (eo.isNegative()) {
						add.addChild(eo.getNode());
					} else {
						minus = new ASTNode(Type.MINUS, pso);
						minus.addChild(eo.getNode());
						add.addChild(minus);
					}

				}
		
				
				node = add;
				if (multiplication.size() > 0) {

					if (multiplication.size() == 1) {
						divide.addChild(add);
						divide.addChild(multiplication.get(0).getNode());

					} else {
						for (int i = 0; i < multiplication.size(); i++) {
							multiply.addChild(multiplication.get(i).getNode());
						}
						divide.addChild(add);
						divide.addChild(multiply);
			
					}

					node = divide;
				}
			}

		}

		return node;

	}

	/**
	 * Searches in the given ASTNode for a node with the same name as the given
	 * String. Afterwards the variables variableNode and variable are set.
	 * 
	 * @param node
	 * @param variable
	 */
	private void setNodeWithVariable(ASTNode node, String variable) {
		Enumeration<ASTNode> nodes = node.children();
		ASTNode subnode;

		while (nodes.hasMoreElements()) {
			subnode = (ASTNode) nodes.nextElement();
			if (subnode.isName()) {
				if (subnode.getName() == variable) {
					variableNodeParent = node;
					variableNode = subnode;
				}
			} else
				setNodeWithVariable((ASTNode) subnode, variable);
		}

	}

	/**
	 * Replaces all functions in the given ASTNode with the function definition
	 * 
	 * @param node
	 * @param indexParent
	 */
	private ASTNode substituteFunctions(ASTNode node, int indexParent) {
		// check if node is a function
		if (node.isName()) {
			FunctionDefinition fd = model.getFunctionDefinition(node.getName());
			// node represents a function definiton in the model
			if (fd != null) {
				ASTNode function = fd.getMath();
				HashMap<String, String> nameHash = new HashMap<String, String>();
				HashMap<String, Double> numberHash = new HashMap<String, Double>();
				HashMap<String, ASTNode> nodeHash = new HashMap<String, ASTNode>();

				ASTNode parent;

				// Hash its variables to the parameter
				for (int i = 0; i < node.getNumChildren(); i++) {
					if (node.getChild(i).isFunction()) {
						nodeHash.put(function.getChild(i).getName(), node
								.getChild(i).clone());
					} else if (node.getChild(i).isOperator()) {
						nodeHash.put(function.getChild(i).getName(), node
								.getChild(i).clone());
					} else if (node.getChild(i).isName()) {
						nameHash.put(function.getChild(i).getName(), node
								.getChild(i).getName());
					} else if (node.getChild(i).isNumber()) {
						if (node.getChild(i).isInteger()) {
							numberHash.put(function.getChild(i).getName(),(double) node
									.getChild(i).getInteger());
						}
						else{
							numberHash.put(function.getChild(i).getName(),(double) node
									.getChild(i).getReal());
						}
					}

				}
				parent = (ASTNode) node.getParent();
				// function definiton is child
				if (parent != null) {
					System.out.println(parent.getType());
					// replace the reference to a function definition with the
					// function definiton itself
					parent.replaceChild(indexParent, function.getRightChild()
							.clone());
					// substitute the variables with the parameter
					replaceNames(parent.getChild(indexParent), nameHash,
							numberHash, nodeHash);
					
					node = parent;

				}
				// function definiton is root
				else {
					// replace the reference to a function definition with the
					// function definiton itself
					node = function.getRightChild().clone();
					// substitute the variables with the parameter
					replaceNames(node, nameHash, numberHash, nodeHash);
				}
			}

		}

		// move on with its children
		for (int i = 0; i < node.getNumChildren(); i++) {
			substituteFunctions(node.getChild(i), i);
		}

		return node;

	}

}
