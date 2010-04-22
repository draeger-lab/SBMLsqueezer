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
import java.util.Set;
import java.util.Stack;

import javax.swing.tree.TreeNode;

import org.sbml.jsbml.ASTNode;
import org.sbml.jsbml.AlgebraicRule;
import org.sbml.jsbml.AssignmentRule;
import org.sbml.jsbml.Compartment;
import org.sbml.jsbml.Model;
import org.sbml.jsbml.Parameter;
import org.sbml.jsbml.RateRule;
import org.sbml.jsbml.Reaction;
import org.sbml.jsbml.Rule;
import org.sbml.jsbml.Species;
import org.sbml.jsbml.SpeciesReference;
import org.sbml.jsbml.Symbol;
import org.sbml.jsbml.ASTNode.Type;

/**
 * This Class converts the algebraic rules of a model to assignment rules using
 * the Hopcroft-Karp-Algorithm
 * 
 * @author Alexander D&ouml;rr
 * @since 1.4
 */
public class AlgebraicRuleConverter {

	/**
	 * This Interface represents a node in the bipartite graph
	 * 
	 * @author Alexander D&ouml;rr
	 * @since 1.4
	 */
	private interface Node {
		/**
		 * 
		 * 
		 * @param node
		 */
		public void addNode(Node node);

		/**
		 * 
		 * 
		 * @return
		 */
		public Node getNextNode();

		/**
		 * Deletes node from the list of linked nodes
		 * 
		 * @param node
		 */
		public void deleteNode(Node node);

		/**
		 * 
		 * @return
		 */
		public String getValue();
	}

	/**
	 * This Class represents an inner node in the bipartite graph, e.g.
	 * a varibale or an reaction
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
		 * @see org.sbml.squeezer.math.AlgebraicRuleConverter.Node#getValue()
		 */
		public String getValue() {
			return value;
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
		 * @see
		 * org.sbml.squeezer.math.AlgebraicRuleConverter.Node#deleteNode(org
		 * .sbml.squeezer.math.AlgebraicRuleConverter.Node)
		 */
		public void deleteNode(Node node) {
			nodes.remove(node);

		}

	}

	/**
	 * This Class represents the start node in the bipartite graph
	 * 
	 * @author Alexander D&ouml;rr
	 * @since 1.4
	 */
	private class StartNode implements Node {
		private List<Node> nodes;

		/**
		 * 
		 */
		public StartNode() {
			this.nodes = new ArrayList<Node>();
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

	}

	/**
	 * This Class represents the end node in the bipartite graph
	 * 
	 * @author Alexander D&ouml;rr
	 * @since 1.4
	 */
	private class TerminalNode implements Node {

		/**
		 * 
		 */
		public TerminalNode() {

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
		 * @see
		 * org.sbml.squeezer.math.AlgebraicRuleConverter.Node#addNode(org.sbml
		 * .squeezer.math.AlgebraicRuleConverter.Node)
		 */
		public void addNode(Node node) {

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
		 * @see
		 * org.sbml.squeezer.math.AlgebraicRuleConverter.Node#deleteNode(org
		 * .sbml.squeezer.math.AlgebraicRuleConverter.Node)
		 */
		public void deleteNode(Node node) {

		}

	}

	/**
	 * This Class represents a calculated matching between vertices of a
	 * bipartite graph
	 * 
	 * @author Alexander D&ouml;rr
	 * @since 1.4
	 */
	private class Match {
		/**
		 * 
		 */
		private String reaction;
		/**
		 * 
		 */
		private String variable;

		/**
		 * 
		 * @param reaction
		 * @param variabale
		 */
		public Match(String reaction, String variabale) {
			this.reaction = reaction;
			this.variable = variabale;
		}

		/**
		 * 
		 * @return
		 */
		public String getVariable() {
			return variable;
		}

		/**
		 * 
		 * @return
		 */
		public String getReaction() {
			return reaction;
		}
	}

	/**
	 * 
	 */
	private List<Node> equations;
	/**
	 * 
	 */
	private HashMap<String, Node> variableHash;
	/**
	 * 
	 */
	private List<Node> variables;
	/**
	 * 
	 */
	private StartNode bipartiteGraph;
	/**
	 * 
	 */
	private List<Match> matching;
	/**
	 * 
	 */
	private List<String> svariables;
	/**
	 * 
	 */
	private Set<String> reactants;
	/**
	 * 
	 */
	private Model model;
	/**
	 * 
	 */
	private String symbol;
	/**
	 * 
	 */
	private ASTNode variableNode;

	/**
	 * 
	 * @param model
	 */
	public AlgebraicRuleConverter(Model model) {
		this.model = model;
		init();
	}

	/**
	 * 
	 */
	private void init() {
		this.svariables = new ArrayList<String>();
		this.reactants = new HashSet<String>();
		this.variableNode = null;

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
		
		System.out.println(matching.size());
		
		for (int i = 0; i < matching.size(); i++) {
			
		}
	}

	/**
	 * 
	 */
	private void buildGraph() {
		equations = new ArrayList<Node>();
		variables = new ArrayList<Node>();
		variableHash = new HashMap<String, Node>();
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

		for (i = 0; i < model.getNumReactions(); i++) {
			Reaction r = model.getReaction(i);

			// Create vertices and edges for products
			for (SpeciesReference sref : r.getListOfProducts()) {
				if (!sref.getSpeciesInstance().isConstant()) {
					variable = variableHash.get(sref.getSpeciesInstance()
							.getId());
					if (!sref.getSpeciesInstance().getHasOnlySubstanceUnits()) {
						equation = new InnerNode(sref.getSpeciesInstance()
								.getId());
						equations.add(equation);
						// link
						variable.addNode(equation);
						equation.addNode(variable);

					}
				}
			}

			// Create vertices and edges for reactants
			for (SpeciesReference sref : r.getListOfReactants()) {

				if (!sref.getSpeciesInstance().isConstant()) {
					variable = variableHash.get(sref.getSpeciesInstance()
							.getId());
					if (!sref.getSpeciesInstance().getHasOnlySubstanceUnits()) {
						equation = new InnerNode(sref.getSpeciesInstance()
								.getId());
						equations.add(equation);
						// link
						variable.addNode(equation);
						equation.addNode(variable);
						variableHash.put(variable.getValue(), variable);
					}
				}
			}

			equation = new InnerNode(r.getId());
			equations.add(equation);
			variable = variableHash.get(equation.getValue());

			// link reation with kinetic law
			variable.addNode(equation);
			equation.addNode(variable);

			svariables.clear();
			getVariables(r.getKineticLaw().getMath(), svariables);
			for (int j = 0; j < svariables.size(); j++) {
				variable = variableHash.get(svariables.get(j));
				if (variable != null) {
					variable.addNode(equation);
					equation.addNode(variable);
				}
			}

		}

		// Create vertices and edges for rules
		for (i = 0; i < model.getNumRules(); i++) {
			equation = new InnerNode(model.getRule(i).getMetaId());
			equations.add(equation);
			Rule r = model.getRule(i);
			if (r instanceof AlgebraicRule) {
				// all identifiers withn the MathML of this AlgebraicRule
				svariables.clear();
				getVariables(model.getRule(i).getMath(), svariables);

				for (int j = 0; j < svariables.size(); j++) {
					variable = variableHash.get(svariables.get(j));
					if (variable != null) {
						variable.addNode(equation);
						equation.addNode(variable);

					}
					
					
				}
			} else if (r instanceof RateRule) {

				variable = variableHash.get(((RateRule) r)
						.getVariableInstance().getId());
				// link
				variable.addNode(equation);
				equation.addNode(variable);
			}

			else if (r instanceof AssignmentRule) {

				variable = variableHash.get(((AssignmentRule) r)
						.getVariableInstance().getId());
				// link
				variable.addNode(equation);
				equation.addNode(variable);

				svariables.clear();
				getVariables(model.getRule(i).getMath(), svariables);

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
	 * 
	 */
	private void buildMatching() {
		bipartiteGraph = new StartNode();
		TerminalNode tnode = new TerminalNode();
		matching = new ArrayList<Match>();
		Set<Node> B = new HashSet<Node>();
		Stack<Node> stack = new Stack<Node>();
		Node first, last;
		int i;

		for (i = 0; i < equations.size(); i++) {
			bipartiteGraph.addNode(equations.get(i));
		}

		for (i = 0; i < variables.size(); i++) {
			variables.get(i).addNode(tnode);
		}

		stack.push(bipartiteGraph);

		while (!stack.isEmpty()) {
			
			if (stack.peek().getNextNode() != null) {
				first = stack.peek().getNextNode();

				if (!B.contains(first)) {

					stack.peek().deleteNode(first);
					first.deleteNode(stack.peek());
					stack.push(first);

					if (!(stack.peek() instanceof TerminalNode)) {
						B.add(first);

					} else {
						stack.pop();
						while (stack.size() > 1) {
							last = stack.pop();
							matching.add(new Match(stack.pop().getValue(), last
									.getValue()));

						}
					}

				}

				else
					stack.pop();
			}
			else
				stack.pop();

		}

	}

	/**
	 * 
	 * @param ar
	 * @return
	 */
	public AssignmentRule getAssignmentRule(AlgebraicRule ar) {

		getVariables(ar.getMath(), svariables);
		this.symbol = getUnknownQuantity(svariables, reactants);

		boolean stay = chooseSide(ar.getMath());

		// System.out.println(stay);

		// System.out.println(symbol);

		AssignmentRule as = new AssignmentRule(new Species(symbol, 1, 1));

		return as;
	}

	/**
	 * 
	 * @param math
	 * @return
	 */
	private boolean chooseSide(ASTNode math) {
		getNodeWithVariable(math);
		Enumeration<TreeNode> nodes;
		ASTNode subnode;

		if (variableNode.getType() == Type.TIMES) {

			if (variableNode.getNumChildren() == 2) {
				nodes = variableNode.children();
				while (nodes.hasMoreElements()) {
					subnode = (ASTNode) nodes.nextElement();
					if (subnode.isNumber())
						return false;

				}

			}

		}

		// System.out.println(variableNode.getCharacter());

		return true;

	}

	/**
	 * 
	 * @param node
	 */
	private void getNodeWithVariable(ASTNode node) {
		Enumeration<TreeNode> nodes = node.children();
		ASTNode subnode;

		while (nodes.hasMoreElements()) {
			subnode = (ASTNode) nodes.nextElement();
			if (subnode.isName())
				if (subnode.getName() == symbol)
					variableNode = node;
		}

		nodes = node.children();
		while (nodes.hasMoreElements()) {
			getNodeWithVariable((ASTNode) nodes.nextElement());
		}

	}

	/**
	 * Returns the variables in a MathML object
	 * 
	 * @param node
	 * @param variables
	 */
	private void getVariables(ASTNode node, List<String> variables) {
		if (node.isName()) {
			variables.add(node.getName());
		} else {
			Enumeration<TreeNode> nodes = node.children();
			while (nodes.hasMoreElements()) {
				getVariables((ASTNode) nodes.nextElement(), variables);
			}
		}

	}

	/**
	 * 
	 * @param variables
	 * @return
	 */
	private String getUnknownQuantity(List<String> variables,
			Set<String> reactants) {
		for (String string : variables) {
			Symbol s;
			s = model.getSpecies(string);

			if (s instanceof Species) {
				if (!((Species) s).isSetInitialAmount()
						&& !((Species) s).isSetInitialConcentration())
					return string;

				if (!reactants.contains(string))
					return string;

			}

			if (s instanceof Parameter) {
				if (!((Parameter) s).isSetValue())
					return string;
			}

		}

		return null;
	}
}
