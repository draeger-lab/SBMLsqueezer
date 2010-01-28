/*
 * Copyright (C) 2009 ZBIT, University of Tübingen, Andreas Dräger
 */
package org.sbml.squeezer.math;

import java.util.Arrays;

import org.sbml.jsbml.Model;
import org.sbml.jsbml.xml.libsbml.LibSBMLReader;
import org.sbml.jsbml.xml.libsbml.LibSBMLWriter;
import org.sbml.squeezer.io.SBMLio;

import sun.security.krb5.internal.crypto.Des;

import eva2.gui.Plot;
import eva2.tools.math.des.RKSolver;

/**
 * @author Andreas Dr&auml;ger <a
 *         href="mailto:andreas.draeger@uni-tuebingen.de">
 *         andreas.draeger@uni-tuebingen.de</a>
 * 
 */
public class SimulationTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			System.loadLibrary("sbmlj");
			// Extra check to be sure we have access to libSBML:
			Class.forName("org.sbml.libsbml.libsbml");
		} catch (Exception e) {
			System.err.println("Error: could not load the libSBML library");
			e.printStackTrace();
			System.exit(1);
		}
		String path;
		path = "C:/Dokumente und Einstellungen/radbarbeit11/Desktop/tst suite/sbml-test-cases-2009-09-05/cases/semantic/";
		path += "00025/00025-sbml-l2v4.xml";
		SBMLio sbmlIo = new SBMLio(new LibSBMLReader(), new LibSBMLWriter());
		// System.out.println(args[0]);
		// System.out.println(path);
		//Model model = sbmlIo.readModel(args[0]);
		Model model = sbmlIo.readModel(path);
		RKSolver rk = new RKSolver();
		SBMLinterpreter interpreter = new SBMLinterpreter(model);
		double time = 0;

		double solution[][] = rk.solveByStepSize(interpreter, interpreter
				.getInitialValues(), time, 5);
//		rk.solveAtTimePoints(interpreter, interpreter
//				.getInitialValues(), timePoints)
//		
		
		System.out.println(solution[0].length);
		if (rk.isUnstable())
			System.err.println("unstable!");
		else {
			int from = model.getNumCompartments();
			int to = from + model.getNumSpecies();
			Plot plot = new Plot("Simulation", "time", "value");
			for (int i = 0; i < solution.length; i++) {
				double[] symbol = solution[i];
				System.out.println();
				for (int j = from; j < to; j++) {

					double sym = symbol[j];
					plot.setConnectedPoint(time, sym, j);

				}
				time += rk.getStepSize();

			}
		}

	}
}
