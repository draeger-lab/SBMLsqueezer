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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import org.sbml.jsbml.ASTNode;
import org.sbml.jsbml.AlgebraicRule;
import org.sbml.jsbml.AssignmentRule;
import org.sbml.jsbml.Compartment;
import org.sbml.jsbml.FunctionDefinition;
import org.sbml.jsbml.ListOf;
import org.sbml.jsbml.LocalParameter;
import org.sbml.jsbml.Model;
import org.sbml.jsbml.Parameter;
import org.sbml.jsbml.RateRule;
import org.sbml.jsbml.Reaction;
import org.sbml.jsbml.Rule;
import org.sbml.jsbml.Species;
import org.sbml.jsbml.SpeciesReference;
import org.sbml.jsbml.ASTNode.Type;

/**
 * This class converts the algebraic rules of a model to assignment rules using
 * the Hopcroft-Karp-Algorithm
 * 
 * @author Alexander D&ouml;rr
 * @since 1.4
 */
public class AlgebraicRuleConverter {

	/**
	 * This class represents an inner node in the bipartite graph, e.g., a
	 * varibale or an reaction
	 * 
	 * @author Alexander D&ouml;rr
	 * @since 1.4
	 */
	private class InnerNode implements Node {
		/**
		 * 
		 */
		private List<Node> nodes;
		/**
		 * 
		 */
		private String value;

		/**
		 * Creates a new inner node
		 * 
		 * @param name
		 */
		public InnerNode(String name) {
			this.nodes = new ArrayList<Node>();
			this.value = name;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * org.sbml.squeezer.math.AlgebraicRuleConverter.Node#addNode(org.sbml
		 * .squeezer.math.AlgebraicRuleConverter.Node)
		 */
		public void addNode(Node node) {
			this.nodes.add(node);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * org.sbml.squeezer.math.AlgebraicRuleConverter.Node#deleteNode(org
		 * .sbml.squeezer.math.AlgebraicRuleConverter.Node)
		 */
		public void deleteNode(Node node) {
			nodes.remove(node);

		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.sbml.squeezer.math.AlgebraicRuleConverter.Node#getNextNode()
		 */
		public Node getNextNode() {
			if (nodes.isEmpty())
				return null;
			else
				return nodes.get(0);

		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.sbml.squeezer.math.AlgebraicRuleConverter.Node#getValue()
		 */
		public String getValue() {
			return value;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.sbml.squeezer.math.AlgebraicRuleConverter.Node#getNode(int)
		 */
		public Node getNode(int i) {
			if (nodes.size() > i && i >= 0)
				return nodes.get(i);
			else
				return null;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.sbml.squeezer.math.AlgebraicRuleConverter.Node#getNodes()
		 */
		public List<Node> getNodes() {
			return this.nodes;
		}

	}

	/**
	 * This Interface represents a node in the bipartite graph
	 * 
	 * @author Alexander D&ouml;rr
	 * @since 1.4
	 */
	private interface Node {
		/**
		 * Adds a node to the list of nodes (creates an edge from this node to
		 * another one)
		 * 
		 * @param node
		 */
		public void addNode(Node node);

		/**
		 * Deletes node from the list of linked nodes
		 * 
		 * @param node
		 */
		public void deleteNode(Node node);

		/**
		 * Returns the next node in the list of nodes
		 * 
		 * @return
		 */
		public Node getNextNode();

		/**
		 * Returns the ith node in the list of nodes
		 * 
		 * @return
		 */
		public Node getNode(int i);

		/**
		 * Returns the value of this node
		 * 
		 * @return
		 */
		public String getValue();

		/**
		 * Returns the list of adjacent nodes
		 * 
		 * @return
		 */
		public List<Node> getNodes();
	}

	/**
	 * This class represents the start node in the bipartite graph
	 * 
	 * @author Alexander D&ouml;rr
	 * @since 1.4
	 */
	private class StartNode implements Node {
		private List<Node> nodes;

		/**
		 * Creates a new start node
		 */
		public StartNode() {
			this.nodes = new ArrayList<Node>();
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * org.sbml.squeezer.math.AlgebraicRuleConverter.Node#addNode(org.sbml
		 * .squeezer.math.AlgebraicRuleConverter.Node)
		 */
		public void addNode(Node node) {
			this.nodes.add(node);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * org.sbml.squeezer.math.AlgebraicRuleConverter.Node#deleteNode(org
		 * .sbml.squeezer.math.AlgebraicRuleConverter.Node)
		 */
		public void deleteNode(Node node) {
			nodes.remove(node);

		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.sbml.squeezer.math.AlgebraicRuleConverter.Node#getNextNode()
		 */
		public Node getNextNode() {
			if (nodes.isEmpty())
				return null;
			else
				return nodes.get(0);

		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.sbml.squeezer.math.AlgebraicRuleConverter.Node#getValue()
		 */
		public String getValue() {
			return null;
		}

		/**
		 * Clones this Graph without terminal node
		 */
		@Override
		public StartNode clone() {
			StartNode start = new StartNode();
			Node ln, rn, rnode, lnode;
			int index;
			HashMap<String, Node> variables = new HashMap<String, Node>();

			for (int i = 0; i < this.nodes.size(); i++) {
				lnode = this.getNode(i);
				ln = new InnerNode(lnode.getValue());
				start.addNode(ln);
				index = 0;
				rnode = lnode.getNode(index);
				while (rnode != null) {
					if (variables.get(rnode.getValue()) != null) {
						rn = variables.get(rnode.getValue());
					} else {
						rn = new InnerNode(rnode.getValue());
						variables.put(rnode.getValue(), rn);
					}

					ln.addNode(rn);
					rn.addNode(ln);
					index++;
					rnode = lnode.getNode(index);
				}

			}

			return start;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.sbml.squeezer.math.AlgebraicRuleConverter.Node#getNode(int)
		 */
		public Node getNode(int i) {
			if (nodes.size() > i && i >= 0)
				return nodes.get(i);
			else
				return null;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.sbml.squeezer.math.AlgebraicRuleConverter.Node#getNodes()
		 */
		public List<Node> getNodes() {
			return this.nodes;
		}

	}

	/**
	 * This class represents the end node in the bipartite graph
	 * 
	 * @author Alexander D&ouml;rr
	 * @since 1.4
	 */
	private class TerminalNode implements Node {

		/**
		 * Creates a new terminal node
		 */
		public TerminalNode() {

		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * org.sbml.squeezer.math.AlgebraicRuleConverter.Node#addNode(org.sbml
		 * .squeezer.math.AlgebraicRuleConverter.Node)
		 */
		public void addNode(Node node) {

		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * org.sbml.squeezer.math.AlgebraicRuleConverter.Node#deleteNode(org
		 * .sbml.squeezer.math.AlgebraicRuleConverter.Node)
		 */
		public void deleteNode(Node node) {

		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.sbml.squeezer.math.AlgebraicRuleConverter.Node#getNextNode()
		 */
		public Node getNextNode() {
			return null;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.sbml.squeezer.math.AlgebraicRuleConverter.Node#getValue()
		 */
		public String getValue() {
			return null;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.sbml.squeezer.math.AlgebraicRuleConverter.Node#getNode(int)
		 */
		public Node getNode(int i) {
			return null;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.sbml.squeezer.math.AlgebraicRuleConverter.Node#getNodes()
		 */
		public List<Node> getNodes() {
			return null;
		}

	}

	/**
	 * List with nodes representing an equation in the model
	 */
	private List<Node> equations;
	/**
	 * List with nodes representing an variable in the model
	 */
	private List<Node> variables;
	/**
	 * HashMap with id -> node for variables
	 */
	private HashMap<String, Node> variableHash;
	/**
	 * HashMap with id -> node for equations
	 */
	private HashMap<String, Node> equationHash;
	/**
	 * HashMap with value of the left node -> value of the right node
	 */
	private HashMap<String, String> matching;
	/**
	 * The source node of the bipartite graph
	 */
	private StartNode bipartiteGraph;
	/**
	 * A list where the ids of all global species in an MathML expression are
	 * saved temporarily
	 */
	private List<String> svariables;
	/**
	 * A list of all paths of a certain length in the graph
	 */
	private ArrayList<ArrayList<Node>> paths;
	/**
	 * A set with the ids of all reactants in the model
	 */
	private Set<String> reactants;
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
	 * Creates a new AlgebraicRuleConverter for the given model
	 * 
	 * @param model
	 */
	public AlgebraicRuleConverter(Model model) {
		this.model = model;
		init();
	}

	/**
	 * Build the bipartite graph
	 */
	private void buildGraph() {
		equations = new ArrayList<Node>();
		variables = new ArrayList<Node>();
		variableHash = new HashMap<String, Node>();
		equationHash = new HashMap<String, Node>();
		Node equation, variable;
		int i;

		// Build vertices for compartments and hash them
		for (Compartment c : model.getListOfCompartments()) {
			if (!c.isConstant()) {
				variable = new InnerNode(c.getId());
				variables.add(variable);
				variableHash.put(variable.getValue(), variable);
			}
		}
		// Build vertices for species and hash them
		for (Species s : model.getListOfSpecies()) {
			if (!s.isConstant()) {
				variable = new InnerNode(s.getId());
				variables.add(variable);
				variableHash.put(variable.getValue(), variable);
			}
		}
		// Build vertices for parameter and hash them
		for (Parameter p : model.getListOfParameters()) {
			if (!p.isConstant()) {
				variable = new InnerNode(p.getId());
				variables.add(variable);
				variableHash.put(variable.getValue(), variable);
			}
		}

		// Build vertices for reaction and hash them
		for (Reaction r : model.getListOfReactions()) {
			variable = new InnerNode(r.getId());
			variables.add(variable);
			variableHash.put(variable.getValue(), variable);
		}

		// Create edges with reactions
		for (i = 0; i < model.getNumReactions(); i++) {
			Reaction r = model.getReaction(i);

			// Create vertices and edges for products
			for (SpeciesReference sref : r.getListOfProducts()) {
				if (!sref.getSpeciesInstance().isConstant()) {
					variable = variableHash.get(sref.getSpeciesInstance()
							.getId());
					if (!sref.getSpeciesInstance().getBoundaryCondition()) {

						equation = equationHash.get(sref.getSpeciesInstance()
								.getId());
						if (equation == null) {
							equation = new InnerNode(sref.getSpeciesInstance()
									.getId());
							equations.add(equation);
							equationHash.put(sref.getSpeciesInstance().getId(),
									equation);
							// link
							variable.addNode(equation);
							equation.addNode(variable);
							variableHash.put(variable.getValue(), variable);

						}
					}
				}
			}

			// Create vertices and edges for reactants
			for (SpeciesReference sref : r.getListOfReactants()) {

				if (!sref.getSpeciesInstance().isConstant()) {
					variable = variableHash.get(sref.getSpeciesInstance()
							.getId());
					if (!sref.getSpeciesInstance().getBoundaryCondition()) {

						equation = equationHash.get(sref.getSpeciesInstance()
								.getId());
						if (equation == null) {
							equation = new InnerNode(sref.getSpeciesInstance()
									.getId());
							equations.add(equation);
							equationHash.put(sref.getSpeciesInstance().getId(),
									equation);
							// link
							variable.addNode(equation);
							equation.addNode(variable);
							variableHash.put(variable.getValue(), variable);

						}
					}
				}
			}
			// link reaction with its kinetic law
			equation = new InnerNode(r.getId());
			equations.add(equation);
			variable = variableHash.get(equation.getValue());

			variable.addNode(equation);
			equation.addNode(variable);
			/**
			 * Not in 3.1 // link kinetic law with its variables
			 * svariables.clear();
			 * getVariables(r.getKineticLaw().getListOfParameters(), r
			 * .getKineticLaw().getMath(), svariables);
			 * 
			 * for (int j = 0; j < svariables.size(); j++) { variable =
			 * variableHash.get(svariables.get(j)); if (variable != null) {
			 * variable.addNode(equation); equation.addNode(variable); } }
			 */

		}

		// Create vertices and edges for assignment and rate rules
		for (i = 0; i < model.getNumRules(); i++) {
			equation = new InnerNode(model.getRule(i).getMetaId());
			Rule r = model.getRule(i);
			if (r instanceof RateRule) {
				equations.add(equation);
				variable = variableHash.get(((RateRule) r)
						.getVariableInstance().getId());
				// link
				variable.addNode(equation);
				equation.addNode(variable);

				/**
				 * Not in 3.1 // -- self creation svariables.clear();
				 * getVariables(null, model.getRule(i).getMath(), svariables);
				 * // link rule with its variables for (int j = 0; j <
				 * svariables.size(); j++) { variable =
				 * variableHash.get(svariables.get(j)); if (variable != null) {
				 * variable.addNode(equation); equation.addNode(variable);
				 * 
				 * } } // --
				 */
			}

			else if (r instanceof AssignmentRule) {
				equations.add(equation);
				variable = variableHash.get(((AssignmentRule) r)
						.getVariableInstance().getId());
				// link
				variable.addNode(equation);
				equation.addNode(variable);

				/**
				 * Not in 3.1 svariables.clear(); getVariables(null,
				 * model.getRule(i).getMath(), svariables); // link rule with
				 * its variables for (int j = 0; j < svariables.size(); j++) {
				 * variable = variableHash.get(svariables.get(j)); if (variable
				 * != null) { variable.addNode(equation);
				 * equation.addNode(variable);
				 * 
				 * } }
				 */
			}
		}

		// Create vertices and edges for algebraic rules
		for (i = 0; i < model.getNumRules(); i++) {
			equation = new InnerNode(model.getRule(i).getMetaId());
			Rule r = model.getRule(i);
			if (r instanceof AlgebraicRule) {
				equations.add(equation);
				// all identifiers withn the MathML of this AlgebraicRule
				svariables.clear();
				getVariables(null, model.getRule(i).getMath(), svariables);
				// link rule with its variables
				for (int j = 0; j < svariables.size(); j++) {
					variable = variableHash.get(svariables.get(j));
					if (variable != null) {
						variable.addNode(equation);
						equation.addNode(variable);
					}
				}
			}
		}
	}

	/**
	 * Build the maximum matching
	 */
	private void buildMatching() {
		StartNode matchingGraph;
		// the source node
		matchingGraph = new StartNode();
		// the sink node
		TerminalNode tnode = new TerminalNode();
		matching = new HashMap<String, String>();
		Set<Node> B = new HashSet<Node>();
		Stack<Node> stack = new Stack<Node>();
		Node first, last;
		int i;

		// connect equations with source node
		for (i = 0; i < equations.size(); i++) {
			matchingGraph.addNode(equations.get(i));
		}
		// connect equations with sink node
		for (i = 0; i < variables.size(); i++) {
			variables.get(i).addNode(tnode);
		}

		bipartiteGraph = matchingGraph.clone();

		// push source node on the stack
		stack.push(matchingGraph);

		while (!stack.isEmpty()) {

			// if node on stack has linked node
			if (stack.peek().getNextNode() != null) {
				// get first linked node
				first = stack.peek().getNextNode();
				// if node not already in the matching
				if (!B.contains(first)) {
					// delete connection
					stack.peek().deleteNode(first);
					first.deleteNode(stack.peek());
					// push first on stack
					stack.push(first);

					// if first not the sink node add to the matching
					if (!(stack.peek() instanceof TerminalNode)) {
						B.add(first);
					}// first is sink node
					else {
						// remove sink node
						stack.pop();
						// build matching between 2 neighbouring in the list and
						// leave source node on the stack
						while (stack.size() > 1) {
							last = stack.pop();
							matching.put(stack.pop().getValue(), last
									.getValue());
						}
					}

				} // else delete connection
				else {
					stack.peek().deleteNode(first);
					first.deleteNode(stack.peek());
				}

			}// else remove from stack
			else
				stack.pop();

		}
		for (Map.Entry<String, String> entry : matching.entrySet()) {
			System.out.println(entry.getKey() + " -> " + entry.getValue());
		}

		augmentMatching();

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

		// search for the corresponding variable in the matching
		variable = matching.get(ruleId);

		// evalute and reorganize the equation of the given ASTNode
		if (variable.length() > 0) {
			System.out.println("before: " + node.toFormula());
			System.out.println("Variable: " + variable);
			as = new AssignmentRule();
			as.setVariable(variable);
			setNodeWithVariable(node, variable);
			evaluateEquation(variableNodeParent);
			as.setMath(reorganizeEquation(node));
			System.out.println("after: " + as.getMath().toFormula());
		}

		return as;
	}

	/**
	 * Checks if the Variable has to be moved to the other side of the equation
	 * or not and if its connection to the eqution is additiv or multiplicativ.
	 * *
	 * 
	 * @param node
	 * @return
	 */
	private void evaluateEquation(ASTNode node) {
		if (node != null) {
			if (node.getType() == Type.TIMES) {
				if (node.getNumChildren() == 2) {
					if (node.getLeftChild().isNumber()
							|| node.getRightChild().isNumber()) {
						remainOnSide = false;
					} else
						additive = false;
				} else
					additive = false;

			} else if (node.getType() == Type.DIVIDE) {
				additive = false;
				remainOnSide = false;
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
			System.out.println("No matching found");

		// create for every algebraic rule an adequate assignment rule
		for (int i = 0; i < model.getNumRules(); i++) {
			Rule r = model.getRule(i);
			if (r instanceof AlgebraicRule) {
				AlgebraicRule ar = (AlgebraicRule) r;
				ASTNode node = ar.getMath().clone();

				// substitute function definitions
				if (model.getNumFunctionDefinitions() > 0) {
					substituteFunctions(node, 0);
				}

				as = createAssignmentRule(node, ar.getMetaId());

				// whenn assignment rule created add to the list
				if (as != null) {
					// assignmentRules.add(as);
					as = null;
				}
			}
		}

		return assignmentRules;
	}

	/**
	 * 
	 * @param node
	 * @return
	 */
	private int getIndexOfNode(ASTNode node) {
		ASTNode parent = (ASTNode) node.getParent();
		for (int i = 0; i < parent.getChildCount(); i++) {
			ASTNode n = parent.getChild(i);
			if (node == n)
				return i;
		}
		return -1;
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
	 * Returns the variables in a MathML object without local parameter
	 * 
	 * @param param
	 * 
	 * @param node
	 * @param variables
	 */
	private void getVariables(ListOf<LocalParameter> param, ASTNode node,
			List<String> variables) {
		// found node with species
		if (node.isName() && !node.isFunction()) {
			if (!node.isConstant()) {
				if (param == null)
					variables.add(node.getName());
				else {
					if (!param.contains(node.getName())) {
						variables.add(node.getName());
					}
				}
			}

		}// else found operator or function
		else {
			// carry on with all children
			Enumeration<ASTNode> nodes = node.children();
			while (nodes.hasMoreElements()) {
				getVariables(param, nodes.nextElement(), variables);
			}
		}

	}

	/**
	 * Initializes the Converter
	 */
	private void init() {
		this.svariables = new ArrayList<String>();
		this.reactants = new HashSet<String>();
		this.variableNodeParent = null;

		for (int i = 0; i < model.getNumReactions(); i++) {

			for (SpeciesReference sref : model.getReaction(i)
					.getListOfProducts()) {
				reactants.add(sref.getSpecies());
			}

			for (SpeciesReference sref : model.getReaction(i)
					.getListOfReactants()) {
				reactants.add(sref.getSpecies());
			}

		}

		buildGraph();
		buildMatching();

	}

	/**
	 * Returns a boolean that indicates whether the given model is
	 * overdetermined or not.
	 * 
	 * @return
	 */
	public boolean isOverdetermined() {
		return (equations.size() > matching.size());
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
				index = getIndexOfNode(variableNodeParent);
				parent = (ASTNode) variableNodeParent.getParent();
				parent.removeChild(index);

				return node;

			} else {
				index = getIndexOfNode(variableNodeParent);
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
			index = getIndexOfNode(variableNodeParent);

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
			Map<String, Integer> numberHash, Map<String, ASTNode> nodeHash) {

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
	private void substituteFunctions(ASTNode node, int indexParent) {
		// check if node is a function
		if (node.isName()) {
			FunctionDefinition fd = model.getFunctionDefinition(node.getName());
			// node represents a function definiton in the model
			if (fd != null) {
				ASTNode function = fd.getMath();
				HashMap<String, String> nameHash = new HashMap<String, String>();
				HashMap<String, Integer> numberHash = new HashMap<String, Integer>();
				HashMap<String, ASTNode> nodeHash = new HashMap<String, ASTNode>();
				ASTNode parent;

				// Hash its variables to the parameter
				for (int i = 0; i < node.getNumChildren(); i++) {
					if (node.getChild(i).isName())
						nameHash.put(function.getChild(i).getName(), node
								.getChild(i).getName());
					else if (node.getChild(i).isNumber()) {
						numberHash.put(function.getChild(i).getName(), node
								.getChild(i).getInteger());
					} else if (node.getChild(i).isOperator()) {
						nodeHash.put(function.getChild(i).getName(), node
								.getChild(i).clone());
					}

				}
				parent = (ASTNode) node.getParent();
				// replace the reference to a function definition with the
				// function definiton itself
				parent.replaceChild(indexParent, function.getRightChild()
						.clone());
				// substitute the variables with the parameter
				replaceNames(parent.getChild(indexParent), nameHash,
						numberHash, nodeHash);
			}

			// else move on with its children
		} else {
			for (int i = 0; i < node.getNumChildren(); i++) {
				substituteFunctions(node.getChild(i), i);
			}
		}

	}

	/**
	 * Improves the matching as far as possible with augmenting paths
	 */
	private void augmentMatching() {
		paths = new ArrayList<ArrayList<Node>>();
		int length = 1;

		while (length < (variables.size() + equations.size())) {
			System.out.println("Searching path of size: " + length);
			for (Node node : bipartiteGraph.getNodes()) {
				findShortestPath(length, node, new ArrayList<Node>());

			}
			System.out.println("Found: " + paths.size());

			augmentPath(length);

			if (matching.size() == equations.size())
				break;

			length++;
		}

	}

	/**
	 * Tries augment every path found in the graph to a new path of the given
	 * length + 1
	 * 
	 * @param length
	 */
	private void augmentPath(int length) {
		System.out
				.println("Searching augmenting path of size: " + (length + 2));
		Node start = null, end = null;
		ArrayList<Node> path;

		while (!paths.isEmpty()) {
			path = paths.get(0);
			// search for the start node of the path an unmatched adjacent node
			for (Node node : path.get(0).getNodes()) {
				// is one node enough
				if (!matching.containsKey(node.getValue())
						&& !matching.containsValue(node.getValue())) {
					start = node;
					break;
				}
			}

			// search for the end node of the path an unmatched adjacent node
			for (Node node : path.get(path.size() - 1).getNodes()) {
				// is one node enough
				if (!matching.containsKey(node.getValue())
						&& !matching.containsValue(node.getValue())) {
					end = node;
					break;
				}

			}
			// new start and end node for this path found -> update path
			if (start != null && end != null) {
				path.add(0, start);
				path.add(path.size(), end);

				// update matching
				updateMatching(path);
			}
			paths.remove(path);
		}
	}

	/**
	 * Updates the matching of the model on the basis of the found augmented
	 * path. Please note that because of starting the search for a path through
	 * the graph at an equation the first node in the augmented path is always a
	 * variable and the last one an equation
	 * 
	 * @param path
	 */
	private void updateMatching(ArrayList<Node> path) {
		System.out.println("new length: " + (path.size() - 1));
		int index;
		index = 1;
		while (path.size() > index) {
			matching.remove(path.get(index).getValue());
			matching.put(path.get(index).getValue(), path.get(index - 1)
					.getValue());
			index = index + 2;

		}

	}

	/**
	 * Finds all paths of the length i whose nodes are part of the matching
	 * 
	 * @param i
	 * @param node
	 * @param path
	 */
	private void findShortestPath(int i, Node node, ArrayList<Node> path) {
		String value;
		if (path.size() == i * 2) {
			paths.add(path);

		} else {
			value = matching.get(node.getValue());
			for (Node next : node.getNodes()) {
				if (next.getValue() == value) {
					path.add(node);
					path.add(next);
					for (Node nextnext : next.getNodes()) {
						if (nextnext.getValue() != node.getValue())
							findShortestPath(i, nextnext,
									(ArrayList<Node>) path.clone());
					}

				}

			}
		}

	}

}
