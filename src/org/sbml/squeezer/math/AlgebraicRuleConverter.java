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
import org.sbml.jsbml.Model;
import org.sbml.jsbml.Parameter;
import org.sbml.jsbml.Species;
import org.sbml.jsbml.SpeciesReference;
import org.sbml.jsbml.Symbol;
import org.sbml.jsbml.ASTNode.Type;

public class AlgebraicRuleConverter {

	private interface Node {
		public void addNode(Node node);

		public Node getNextNode();

		public void deleteNode(Node node);

		public String getValue();
	}

	private class InnerNode implements Node {
		private List<Node> nodes;
		private String value;

		public InnerNode(String name) {
			this.nodes = new ArrayList<Node>();
			this.value = name;
		}

		@Override
		public String getValue() {
			return value;
		}

		@Override
		public void addNode(Node node) {
			this.nodes.add(node);
		}

		@Override
		public Node getNextNode() {
			if (nodes.isEmpty())
				return null;
			else
				return nodes.get(0);

		}

		@Override
		public void deleteNode(Node node) {
			nodes.remove(node);

		}

	}

	private class StartNode implements Node {
		private List<Node> nodes;

		public StartNode() {
			this.nodes = new ArrayList<Node>();
		}

		@Override
		public Node getNextNode() {
			if (nodes.isEmpty())
				return null;
			else
				return nodes.get(0);

		}

		@Override
		public String getValue() {
			return null;
		}

		@Override
		public void addNode(Node node) {
			this.nodes.add(node);
		}

		@Override
		public void deleteNode(Node node) {
			nodes.remove(node);

		}

	}

	private class TerminalNode implements Node {

		public TerminalNode() {

		}

		@Override
		public String getValue() {
			return null;
		}

		@Override
		public void addNode(Node node) {

		}

		@Override
		public Node getNextNode() {
			return null;
		}

		@Override
		public void deleteNode(Node node) {

		}

	}

	private class Match {
		String reaction;
		String variable;

		public Match(String reaction, String variabale) {
			this.reaction = reaction;
			this.variable = variabale;
		}

		public String getVariable() {
			return variable;
		}

		public String getReaction() {
			return reaction;
		}
	}

	private List<Node> equations;
	private HashMap<String,Node> variableHash;
	private List<Node> variables;
	private StartNode bipartiteGraph;
	private List<Match> matching;
	private List<String> svariables;
	private Set<String> reactants;
	private Model model;
	private String symbol;
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
		//buildMatching();
	}

	private void buildGraph() {
		equations = new ArrayList<Node>();
		variables = new ArrayList<Node>();
		variableHash = new HashMap<String, Node>();
		Node equation, variable;
		int i;
		
		for (i = 0; i < model.getNumReactions(); i++) {
			
			for (SpeciesReference sref : model.getReaction(i)
					.getListOfProducts()) {
				if (!sref.getSpeciesInstance().isConstant()) {
					variable = new InnerNode(sref.getSpeciesInstance().getId());
					variables.add(variable);
					if (!sref.getSpeciesInstance().getHasOnlySubstanceUnits()) {
						equation = new InnerNode(sref.getSpeciesInstance().getId());
						equations.add(equation);
						//link
						variable.addNode(equation);
						equation.addNode(variable);
						variableHash.put(variable.getValue(), variable);
					}					
				}
			}

			for (SpeciesReference sref : model.getReaction(i)
					.getListOfReactants()) {
				
				if (!sref.getSpeciesInstance().isConstant()) {
					variable = new InnerNode(sref.getSpeciesInstance().getId());
					variables.add(variable);
					if (!sref.getSpeciesInstance().getHasOnlySubstanceUnits()) {
						equation = new InnerNode(sref.getSpeciesInstance().getId());
						equations.add(equation);
						//link
						variable.addNode(equation);
						equation.addNode(variable);
						variableHash.put(variable.getValue(), variable);
					}				
				}		
			}	
		}
		//Restlichen variablen hashen
		
		
		for (i = 0; i < model.getNumRules(); i++) {
			equation = new InnerNode(model.getRule(i).getMetaId());			
			equations.add(equation);
			if (model.getRule(i) instanceof AlgebraicRule) {
				//all identifiers withn the MathML of this AlgebraicRule
				svariables.clear();
				getVariables(model.getRule(i).getMath(), svariables);
				
				for (int j = 0; j < svariables.size(); j++) {
					
				}
			}
			else{			
			variable = variableHash.get(model.getRule(i).getMath().getVariable().getId());
			//link
			variable.addNode(equation);
			equation.addNode(variable);
			
			}
		}
		

	}

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
			stack.peek().getNextNode();

			while (stack.peek().getNextNode() != null) {
				first = stack.peek().getNextNode();

				if (!B.contains(first)) {

					stack.peek().deleteNode(first);
					first.deleteNode(stack.peek());
					stack.push(first);

					if (!(stack.peek() instanceof TerminalNode)) {
						B.add(first);

					} else {
						while (stack.size() > 1) {
							stack.pop();
							last = stack.pop();
							matching.add(new Match(stack.pop().getValue(), last
									.getValue()));

						}
					}

				}

				else
					stack.pop();
			}

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

		//System.out.println(stay);

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
