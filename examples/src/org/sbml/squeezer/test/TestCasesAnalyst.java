/*
 * $Id$
 * $URL$
 * ---------------------------------------------------------------------
 * This file is part of SBMLsqueezer, a Java program that creates rate
 * equations for reactions in SBML files (http://sbml.org).
 *
 * Copyright (C) 2006-2016 by the University of Tuebingen, Germany.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 * ---------------------------------------------------------------------
 */
package org.sbml.squeezer.test;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.junit.Ignore;
import org.sbml.jsbml.ASTNode;
import org.sbml.jsbml.Event;
import org.sbml.jsbml.ListOf;
import org.sbml.jsbml.MathContainer;
import org.sbml.jsbml.Model;
import org.sbml.jsbml.Reaction;
import org.sbml.jsbml.Rule;
import org.sbml.jsbml.SpeciesReference;
import org.sbml.jsbml.xml.libsbml.LibSBMLReader;
import org.sbml.jsbml.xml.libsbml.LibSBMLWriter;
import org.sbml.squeezer.io.SBMLio;

/**
 * Analyses a given directory of SBML files for the contained SBML features in
 * all of the models in all sub-folders.
 * 
 * @author Andreas Dr&auml;ger
 * @version $Rev$
 * @since 2.0
 */
@Ignore
public class TestCasesAnalyst {
  
  /**
   * @param args
   *            The path to the main directory of the test cases
   * @throws FileNotFoundException
   */
  public static void main(String[] args) throws FileNotFoundException {
    try {
      System.loadLibrary("sbmlj");
      // Extra check to be sure we have access to libSBML:
      Class.forName("org.sbml.libsbml.libsbml");
    } catch (Exception e) {
      System.err.println("Error: could not load the libSBML library");
      e.printStackTrace();
      System.exit(1);
    }
    SBMLio<org.sbml.libsbml.Model> sbmlIo = new SBMLio<org.sbml.libsbml.Model>(new LibSBMLReader(), new LibSBMLWriter());
    
    File f = new File(args[0]);
    
    Set<String> withEvents = new HashSet<String>();
    Set<String> withAlgebraicRules = new HashSet<String>();
    Set<String> withAssignmentRules = new HashSet<String>();
    Set<String> withRateRules = new HashSet<String>();
    Set<String> withInitialAssignments = new HashSet<String>();
    Set<String> withDelayFunctions = new HashSet<String>();
    Set<String> withDelayedEvents = new HashSet<String>();
    Set<String> withFastReactions = new HashSet<String>();
    Set<String> withReactions = new HashSet<String>();
    Set<String> withOutReactions = new HashSet<String>();
    Set<String> withFunctionDefinitions = new HashSet<String>();
    Set<String> withConstraints = new HashSet<String>();
    Set<String> withStoichiometricMath = new HashSet<String>();
    Set<String> withLocalParameters = new HashSet<String>();
    
    for (File dir : f.listFiles()) {
      if (dir.isDirectory()) {
        try {
          Model model = sbmlIo.convertSBMLDocument(f.getAbsolutePath()
            + "/" + dir.getName() + "/" + dir.getName()
            + "-sbml-l2v4.xml").getModel();
          if (model.getEventCount() > 0) {
            withEvents.add(dir.getName());
            for (Event e : model.getListOfEvents()) {
              if (e.isSetDelay()) {
                withDelayedEvents.add(dir.getName());
                checkDelay(e.getDelay(), dir,
                  withDelayFunctions);
              }
              checkDelay(e.getListOfEventAssignments(), dir,
                withDelayFunctions);
              checkDelay(e.getTrigger(), dir, withDelayFunctions);
            }
          }
          if (model.getRuleCount() > 0) {
            for (Rule r : model.getListOfRules()) {
              if (r.isAlgebraic()) {
                withAlgebraicRules.add(dir.getName());
              } else if (r.isAssignment()) {
                withAssignmentRules.add(dir.getName());
              } else if (r.isRate()) {
                withRateRules.add(dir.getName());
              }
              checkDelay(r, dir, withDelayFunctions);
            }
          }
          if (model.getInitialAssignmentCount() > 0) {
            withInitialAssignments.add(dir.getName());
            checkDelay(model.getListOfInitialAssignments(), dir,
              withDelayFunctions);
          }
          if (model.getReactionCount() > 0) {
            withReactions.add(dir.getName());
            for (Reaction r : model.getListOfReactions()) {
              if (r.isFast()) {
                withFastReactions.add(dir.getName());
                break;
              }
              if (r.isSetKineticLaw() && (r.getKineticLaw().getLocalParameterCount() > 0)) {
                withLocalParameters.add(dir.getName());
              }
              checkForStoichiometryMath(r.getListOfReactants(),
                dir, withStoichiometricMath,
                withDelayFunctions);
              checkForStoichiometryMath(r.getListOfProducts(),
                dir, withStoichiometricMath,
                withDelayFunctions);
              checkDelay(r.getKineticLaw(), dir,
                withDelayFunctions);
            }
          } else {
            withOutReactions.add(dir.getName());
          }
          if (model.getFunctionDefinitionCount() > 0) {
            withFunctionDefinitions.add(dir.getName());
            checkDelay(model.getListOfFunctionDefinitions(), dir,
              withDelayFunctions);
          }
          if (model.getConstraintCount() > 0) {
            withConstraints.add(dir.getName());
            checkDelay(model.getListOfConstraints(), dir,
              withDelayFunctions);
          }
          
        } catch (Throwable e) {
          System.err.println("error in " + dir.getName());
          e.printStackTrace();
          System.exit(1);
        }
      }
    }
    
    FileOutputStream fos = new FileOutputStream(f.getAbsolutePath()
      + "/overview.html");
    PrintStream out = new PrintStream(fos);
    
    out.println("<html><head></head><body>");
    out.println("<table><tr><th>Element</th><th>Models</th></tr>");
    
    format("Events", withEvents, out);
    format("AlgebraicRules", withAlgebraicRules, out);
    format("AssignmentRules", withAssignmentRules, out);
    format("RateRules", withRateRules, out);
    format("InitialAssignments", withInitialAssignments, out);
    format("Delay functions", withDelayFunctions, out);
    format("Delay", withDelayedEvents, out);
    format("Fast reactions", withFastReactions, out);
    format("Reactions", withReactions, out);
    format("Local parameters", withLocalParameters, out);
    format("No reactions", withOutReactions, out);
    format("FunctionDefinitions", withFunctionDefinitions, out);
    format("Constraints", withConstraints, out);
    format("StoichiometryMath", withStoichiometricMath, out);
    
    out.println("</table>");
    out.println("</body></html>");
  }
  
  /**
   * 
   * @param name
   * @param set
   */
  private static void format(String name, Set<String> set, PrintStream out) {
    Object elements[] = set.toArray();
    Arrays.sort(elements);
    StringBuilder list = new StringBuilder();
    for (Object e : elements) {
      list.append("<a href=\"");
      list.append(e.toString());
      list.append('/');
      list.append(e.toString());
      list.append("-model.html\">");
      list.append(e.toString());
      list.append("</a>, ");
    }
    out.print("<tr>");
    out.printf("<td>%s</td>", name);
    out.printf("<td>%s</td>", list.toString());
    out.println("</tr>");
  }
  
  /**
   * 
   * @param list
   * @param dir
   * @param withStoichiometryMath
   * @param withDelayFunctions
   */
  @SuppressWarnings("deprecation")
  private static void checkForStoichiometryMath(
    ListOf<SpeciesReference> list, File dir,
    Set<String> withStoichiometryMath, Set<String> withDelayFunctions) {
    for (SpeciesReference specRef : list) {
      if (specRef.isSetStoichiometryMath()) {
        withStoichiometryMath.add(dir.getName());
        checkDelay(specRef.getStoichiometryMath(), dir,
          withDelayFunctions);
      }
    }
  }
  
  /**
   * 
   * @param list
   * @param dir
   * @param withDelayFunctions
   */
  private static void checkDelay(ListOf<? extends MathContainer> list,
    File dir, Set<String> withDelayFunctions) {
    for (MathContainer mc : list) {
      checkDelay(mc, dir, withDelayFunctions);
    }
  }
  
  /**
   * 
   * @param mc
   * @param dir
   * @param withDelayFunctions
   */
  private static void checkDelay(MathContainer mc, File dir,
    Set<String> withDelayFunctions) {
    ASTNode math = mc.getMath();
    if (containsDelay(math)) {
      withDelayFunctions.add(dir.getName());
    }
  }
  
  /**
   * 
   * @param math
   * @return
   */
  private static boolean containsDelay(ASTNode math) {
    if (math.isLeaf()) {
      return math.getType() == ASTNode.Type.FUNCTION_DELAY;
    }
    for (ASTNode child : math.getListOfNodes()) {
      if (containsDelay(child)) {
        return true;
      }
    }
    return false;
  }
  
}
