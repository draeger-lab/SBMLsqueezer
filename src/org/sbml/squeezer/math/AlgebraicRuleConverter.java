package org.sbml.squeezer.math;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;

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
	private ArrayList<String> variables;
	private HashSet<String> reactants;
	private Model model;
	private String symbol;
	private ASTNode variableNode;
	
	/**
	 * 
	 * @param model
	 */
	public AlgebraicRuleConverter(Model model){
		this.model = model;
		init();
	}

	/**
	 * 
	 */
	private void init(){
		this.variables = new ArrayList<String>();
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
	}

	/**
	 * 
	 * @param ar
	 * @return
	 */
	public AssignmentRule getAssignmentRule(AlgebraicRule ar){
		//System.out.println(ar.getFormula());
		//System.out.println(ar.getMath().getCharacter());

		getVariables(ar.getMath(), variables);
		this.symbol = getUnknownQuantity(variables, reactants);

		boolean stay = chooseSide(ar.getMath());
		
		System.out.println(stay);
		
		//System.out.println(symbol);
		
		AssignmentRule as = new AssignmentRule(new Species(symbol, 1, 1));
		
		return as;
	}
	
	/**
	 * 
	 * @param math
	 * @return
	 */
	private boolean chooseSide(ASTNode math){
		getNodeWithVariable(math);
		Enumeration<TreeNode> nodes;
		ASTNode subnode;	
		
		if (variableNode.getType() == Type.TIMES){
			
			if (variableNode.getNumChildren() == 2){
				nodes = variableNode.children();
				while (nodes.hasMoreElements()) {
					subnode = (ASTNode) nodes.nextElement();
					if (subnode.isNumber())
						return false;
										
				}
				
			}	
			
		}	
		
		//System.out.println(variableNode.getCharacter());
		
		return true;
		
	}
	
	
	/**
	 * 
	 * @param node
	 */
	private void getNodeWithVariable(ASTNode node){
		Enumeration<TreeNode> nodes = node.children();
		ASTNode subnode;
		
		while (nodes.hasMoreElements()) {
			subnode =  (ASTNode) nodes.nextElement();
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
	private void getVariables(ASTNode node, ArrayList<String> variables) {
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
	private String getUnknownQuantity(ArrayList<String> variables,
			HashSet<String> reactants) {
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
