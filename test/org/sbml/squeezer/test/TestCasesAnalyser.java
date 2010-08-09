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
package org.sbml.squeezer.test;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

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
 * @author draeger
 * 
 */
public class TestCasesAnalyser {

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
		SBMLio sbmlIo = new SBMLio(new LibSBMLReader(), new LibSBMLWriter());

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
		Set<String> withFunctionDefinitions = new HashSet<String>();
		Set<String> withConstraints = new HashSet<String>();
		Set<String> withStoichiometricMath = new HashSet<String>();

		for (File dir : f.listFiles()) {
			if (dir.isDirectory())
				try {
					Model model = sbmlIo.convert2Model(f.getAbsolutePath()
							+ "/" + dir.getName() + "/" + dir.getName()
							+ "-sbml-l2v4.xml");
					if (model.getNumEvents() > 0) {
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
					if (model.getNumRules() > 0) {
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
					if (model.getNumInitialAssignments() > 0) {
						withInitialAssignments.add(dir.getName());
						checkDelay(model.getListOfInitialAssignments(), dir,
								withDelayFunctions);
					}
					if (model.getNumReactions() > 0) {
						withReactions.add(dir.getName());
						for (Reaction r : model.getListOfReactions()) {
							if (r.isFast()) {
								withFastReactions.add(dir.getName());
								break;
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
					}
					if (model.getNumFunctionDefinitions() > 0) {
						withFunctionDefinitions.add(dir.getName());
						checkDelay(model.getListOfFunctionDefinitions(), dir,
								withDelayFunctions);
					}
					if (model.getNumConstraints() > 0) {
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
