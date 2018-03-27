package org.sbml.squeezer.functionTermGenerator;

import java.io.StringReader;

import org.sbml.jsbml.ASTNode;
import org.sbml.jsbml.ASTNode.Type;
import org.sbml.jsbml.Model;
import org.sbml.jsbml.ext.qual.Input;
import org.sbml.jsbml.ext.qual.QualConstants;
import org.sbml.jsbml.ext.qual.QualModelPlugin;
import org.sbml.jsbml.ext.qual.Sign;
import org.sbml.jsbml.ext.qual.Transition;
import org.sbml.jsbml.math.ASTCiNumberNode;
import org.sbml.jsbml.math.ASTCnIntegerNode;
import org.sbml.jsbml.math.ASTFunction;
import org.sbml.jsbml.math.ASTLogicalOperatorNode;
import org.sbml.jsbml.math.ASTNode2;
import org.sbml.jsbml.math.ASTRelationalOperatorNode;
import org.sbml.jsbml.text.parser.FormulaParserLL3;
import org.sbml.jsbml.text.parser.IFormulaParser;

/**
 * This class generates default function terms for 
 * each transition in the selected model.
 * 
 * @author Andreas Dräger
 * @author Lisa Falk
 * @since 2.1.1
 * @version $Rev$
 */

public class FunctionTermGenerator {
  private Sign sign = null;
  private static DefaultTerm defaultTerm = null;

  public DefaultTerm getDefaultTerm() {
    return defaultTerm;
  }

  public void setDefaultTerm(DefaultTerm defaultTerm) {
    FunctionTermGenerator.defaultTerm = defaultTerm;
  }

  public boolean isSetDefaultTerm() {
    return defaultTerm != null;
  }

  public Sign getSign() {
    return sign;
  }

  public void setSign(Sign sign) {
    this.sign = sign;
  }

  public boolean isSetSign() {
    return sign != null;
  }

  public FunctionTermGenerator() {

  }
  
  /**
   * @param model, that a default sign should be assigned
   */
  public void setDefaultSign (Model model) {

	  QualModelPlugin qm = (QualModelPlugin) model.getPlugin(QualConstants.shortLabel);

	  for (Transition t : qm.getListOfTransitions()) {
		  for (Input i : t.getListOfInputs()) {
			  // if no sign or sign=unknown the default sign is set
			  if (!i.isSetSign() || i.getSign().equals(Sign.unknown)) {
				  i.setSign(sign);
			  }
		  }
	  }	  
  }

  /**
   * @param model, that a default function term should be assigned
   * @return the kineticsGeneRegulation
   */
  public void generateFunctionTerms(Model model) throws Exception {

	  QualModelPlugin qm = (QualModelPlugin) model.getPlugin(QualConstants.shortLabel);

	  // exit the method if no default function term scheme is applied
	  if (defaultTerm.equals(DefaultTerm.none)) {
		  return;
	  }

	  for (Transition t : qm.getListOfTransitions()) {

		  if (!t.isSetListOfFunctionTerms() || (t.isSetListOfFunctionTerms() && 
				  ((t.getListOfFunctionTerms().size() == 1 && t.getListOfFunctionTerms().get(0).isDefaultTerm()) ||
						  t.getListOfFunctionTerms().isEmpty()))) {
			  
			  ASTNode2 math;

			  // scheme oneActivatorAndNoInhibitor
			  if (defaultTerm.equals(DefaultTerm.oneActivatorAndNoInhibitor)) {
				  math = generateFunctionTermForOneTransition(t, ASTNode.Type.LOGICAL_OR);
			  }
			  // scheme allActivatorsAndNoInhibitor
			  else {
				  math = generateFunctionTermForOneTransition(t, ASTNode.Type.LOGICAL_AND);
			  }

			  // beta solution to use a parser to convert from ASTNode2 to ASTNode 
			  // TODO: implementation converter in JSBML
			  IFormulaParser parser = new FormulaParserLL3(new StringReader(""));
			  String helper = math.toFormula().replace("xor", "Xor");
			  ASTNode node = ASTNode.parseFormula(helper, parser);

			  t.createFunctionTerm(node);
			  
			  //set the resultLevel of the newly created default function term
			  t.getListOfFunctionTerms().get(t.getFunctionTermCount()-1).setResultLevel(1);
		  }
	  }
  }
  
  private ASTFunction generateFunctionTermForOneTransition(Transition t, Type logicalJunction) {

    if (t.isSetListOfInputs()) {

      ASTFunction ai = new ASTLogicalOperatorNode(logicalJunction);
      ASTFunction ri = new ASTLogicalOperatorNode(ASTNode.Type.LOGICAL_AND);
      ASTFunction ari = new ASTLogicalOperatorNode(ASTNode.Type.LOGICAL_XOR);
      ASTFunction singleA = null;
      ASTFunction singleR = null;
      ASTFunction functionTerm = new ASTLogicalOperatorNode(ASTNode.Type.LOGICAL_AND);

      for (Input i : t.getListOfInputs()) {

        // concatenate all activators
        if (i.getSign().name().equals("positive")) {
          if (singleA != null) {
            ai.addChild(singleA);
          }
          singleA = generateEquation(i, 1);
        }

        // concatenate all inhibitors
        if (i.getSign().name().equals("negative")) {
          if (singleR != null) {
            ri.addChild(singleR);
          }
          singleR = generateEquation(i, 0);
        }   
        
  	  // activator or inhibitor depending on the co-factors
        if (i.getSign().name().equals("dual")) {
        	ari.addChild(generateEquation(i, 0));   
        	ari.addChild(generateEquation(i, 1));
        }
      }
      
      //case 1:  more than one activator and more than one inhibitor
      if ((ai.getChildCount() != 0) && (ri.getChildCount() != 0)) {
        ai.addChild(singleA);
        functionTerm.addChild(ai);
        ri.addChild(singleR);
        functionTerm.addChild(ri);
        if (ari.getChildCount() > 0) {
        	functionTerm.addChild(ari);
        }
        return functionTerm;
      }

      // all the cases, where activators and inhibitors are involved
      if ((singleA != null) && (singleR != null)) {
        if (ai.getChildCount() != 0) {
          ai.addChild(singleA);
          functionTerm.addChild(ai);}
        else {
          functionTerm.addChild(singleA);
        }
        if (ri.getChildCount() != 0) {
          ri.addChild(singleR);
          functionTerm.addChild(ri);
        }
        else {
          functionTerm.addChild(singleR);
        }
        if (ari.getChildCount() > 0) {
        	functionTerm.addChild(ari);
        }
        return functionTerm;
      }

      // case no inhibitor
      if (singleA == null && singleR != null) {
    	  if (ri.getChildCount() != 0) {
    		  ri.addChild(singleR);
    		  if (ari.getChildCount() > 0) {
    			  functionTerm.addChild(ri);
    			  functionTerm.addChild(ari);
    			  return functionTerm;
    		  }
    		  return ri;
    	  }
    	  else {
    		  if (ari.getChildCount() > 0) {
    			  functionTerm.addChild(singleR);
    			  functionTerm.addChild(ari);
    			  return functionTerm;
    		  }
    		  return singleR;
    	  }
      }

      // case no activator
      if (singleR == null && singleA !=null) {
    	  if (ai.getChildCount() != 0) {
    		  ai.addChild(singleA);
    		  if (ari.getChildCount() > 0) {
    			  functionTerm.addChild(ai);
    			  functionTerm.addChild(ari);
    			  return functionTerm;
    		  }
    		  return ai;
    	  }
    	  else {
    		  if (ari.getChildCount() > 0) {
    			  functionTerm.addChild(singleA);
    			  functionTerm.addChild(ari);
    			  return functionTerm;
    		  }
    		  return singleA;
    	  }
      }
      
      // case there is only a species with a dual sign
      if (ari.getChildCount() > 0) {
    	  return ari;
      }
    }
    return null;
  }

  private static ASTFunction generateEquation(Input i, int value) {

    ASTRelationalOperatorNode eq = new ASTRelationalOperatorNode(ASTNode.Type.RELATIONAL_EQ);

    // generate node with id
    ASTCiNumberNode ci = new ASTCiNumberNode();
    ci.setRefId(i.getQualitativeSpecies());
    eq.addChild(ci);

    //generate node with number
    ASTCnIntegerNode cn = new ASTCnIntegerNode();
    cn.setNumber(value);
    eq.addChild(cn);

    return eq;
  }
}
