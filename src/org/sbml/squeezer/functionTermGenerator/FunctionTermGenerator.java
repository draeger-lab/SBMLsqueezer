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

  public void generateFunctionTerms(Model m) throws Exception {
	  
	QualModelPlugin qm = (QualModelPlugin) m.getPlugin(QualConstants.shortLabel);
	
	if (defaultTerm.equals(DefaultTerm.none)) {
		return;
	}
	
    for (Transition t : qm.getListOfTransitions()) {
      ASTNode2 math;
      // case or
      if (defaultTerm.equals(DefaultTerm.oneActivatorAndNoInhibitor)) {
        math = generateFunctionTermForOneTransition(t, ASTNode.Type.LOGICAL_OR);
      }
      // case and
      else {
        math = generateFunctionTermForOneTransition(t, ASTNode.Type.LOGICAL_AND);
      }

      IFormulaParser parser = new FormulaParserLL3(new StringReader(""));
      ASTNode node = ASTNode.parseFormula(math.toFormula(), parser);
      
      if (t.isSetListOfFunctionTerms() && t.getListOfFunctionTerms().size() >= 0) {
       continue;
      }
      else {
        t.createFunctionTerm(node);
      }
    }
  }

  private static ASTFunction generateFunctionTermForOneTransition(Transition t, Type logicalJunction) {

    if (t.isSetListOfInputs()) {

      ASTFunction ai = new ASTLogicalOperatorNode(logicalJunction);
      ASTFunction ri = new ASTLogicalOperatorNode(ASTNode.Type.LOGICAL_AND);
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
      }

      //case 1:  more than one activator and more than one inhibitor
      if ((ai.getChildCount() != 0) && (ri.getChildCount() != 0)) {
        ai.addChild(singleA);
        functionTerm.addChild(ai);
        ri.addChild(singleR);
        functionTerm.addChild(ri);
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
        return functionTerm;
      }

      // case no inhibitor
      if (singleA == null) {
        if (ri.getChildCount() != 0) {
          ri.addChild(singleR);
          return ri;
        }
        else {
          return singleR;
        }
      }

      // case no activator
      if (singleR == null) {
        if (ai.getChildCount() != 0) {
          ai.addChild(singleA);
          return ai;
        }
        else {
          return singleA;
        }
      }
    }
    return null;

  }

  private static ASTFunction generateEquation(Input i, int value) {

    ASTRelationalOperatorNode eq = new ASTRelationalOperatorNode(ASTNode.Type.RELATIONAL_EQ);

    // generate node with id
    ASTCiNumberNode ci = new ASTCiNumberNode();
    ci.setRefId(i.getId());
    eq.addChild(ci);

    //generate node with number
    ASTCnIntegerNode cn = new ASTCnIntegerNode();
    cn.setNumber(value);
    eq.addChild(cn);

    return eq;
  }
}
