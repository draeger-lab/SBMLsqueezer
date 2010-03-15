/*
 * Copyright (C) 2009 ZBIT, University of Tübingen, Andreas Dräger
 */
package org.sbml.squeezer.math;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import javax.imageio.ImageIO;

import org.sbml.jsbml.Model;
import org.sbml.jsbml.xml.libsbml.LibSBMLReader;
import org.sbml.jsbml.xml.libsbml.LibSBMLWriter;
import org.sbml.squeezer.io.SBMLio;

import au.com.bytecode.opencsv.CSVReader;

import eva2.gui.Plot;
import eva2.tools.math.des.RKEventSolver;

/**
 * @author Andreas Dr&auml;ger <a
 *         href="mailto:andreas.draeger@uni-tuebingen.de">
 *         andreas.draeger@uni-tuebingen.de</a>
 * 
 */
public class SimulationTestAutomatic {

	/**
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {
		try {
			System.loadLibrary("sbmlj");
			// Extra check to be sure we have access to libSBML:
			Class.forName("org.sbml.libsbml.libsbml");
		} catch (Exception e) {
			System.err.println("Error: could not load the libSBML library");
			e.printStackTrace();
			System.exit(1);
		}

		String sbmlfile, folder, resultfolder, csvfile, configfile, line = "";
		int modelnr, start = 0;
		double duration = 0d, steps = 0d;
		BufferedReader reader;
		BufferedWriter writer;
		
		folder = args[0];			
		resultfolder = args[1];
		
		for (modelnr = 203; modelnr <= 203; modelnr++) {
			System.out.println("model " + modelnr);
			
			if (modelnr < 100) {
				sbmlfile = folder + "000" + modelnr + "/000" + modelnr
						+ "-sbml-l2v4.xml";
				csvfile = folder + "000" + modelnr + "/000" + modelnr
						+ "-results.csv";
				configfile = folder + "000" + modelnr + "/000" + modelnr
						+ "-settings.txt";
			} else {
				sbmlfile = folder + "00" + modelnr + "/00" + modelnr
						+ "-sbml-l2v4.xml";
				csvfile = folder + "00" + modelnr + "/00" + modelnr
						+ "-results.csv";
				configfile = folder + "00" + modelnr + "/00" + modelnr
						+ "-settings.txt";
			}

			try {
				reader = new BufferedReader(new FileReader(configfile));
				line = reader.readLine();
				start = Integer.valueOf(line
						.substring(line.lastIndexOf(" ") + 1));
				line = reader.readLine();
				duration = Double.valueOf(line
						.substring(line.lastIndexOf(" ") + 1));
				line = reader.readLine();
				steps = Double.valueOf(line
						.substring(line.lastIndexOf(" ") + 1));
				reader.close();
			} catch (FileNotFoundException e) {

				e.printStackTrace();
			}
			
			SBMLio sbmlIo = new SBMLio(new LibSBMLReader(), new LibSBMLWriter());

			Model model = sbmlIo.readModel(sbmlfile);
			//RKSolver rk = new RKSolver();
			RKEventSolver rk = new RKEventSolver();
			
			SBMLinterpreter interpreter = new SBMLinterpreter(model);
			double time = 0;

			// get timepoints
			CSVReader csvreader = new CSVReader(new FileReader(csvfile), ',',
					'\'', 1);
			List<String[]> input = csvreader.readAll();
			double[] timepoints = new double[input.size()];
			for (int i = 0; i < timepoints.length; i++) {
				timepoints[i] = Double.valueOf(input.get(i)[0]);

			}
			csvreader.close();

			// solve By StepSize
			// rk.setStepSize(duration / steps);
			// double solution[][] = rk.solveByStepSize(interpreter, interpreter
			// .getInitialValues(), time, duration);

			// solve At StepSize

			double solution[][] = rk.solveAtTimePoints(interpreter, interpreter
					.getInitialValues(), timepoints);

			csvreader = new CSVReader(new FileReader(csvfile), ',', '\'', 1);
			input = csvreader.readAll();
			csvreader.close();
			double[][] data = new double[input.get(0).length - 1][input.size()];
			double[][] solutiontrans = new double[input.get(0).length - 1][input
					.size()];
			int from = model.getNumCompartments();
			//from = 0;
			int to = from + model.getNumSpecies();
			
			for (int i = 1; i < data.length + 1; i++) {

				for (int j = 0; j < input.size(); j++) {

					data[i - 1][j] = Double.valueOf(input.get(j)[i]);
					solutiontrans[i - 1][j] = solution[j][i - 1 + from];
				}
			}

			Distance distance = new RSE();
			
			
			writer = new BufferedWriter(
					new FileWriter(
							resultfolder+modelnr+"-deviation.txt"));
			writer.write("relative distance for model-" + modelnr);
			writer.newLine();
			writer.write(String.valueOf(distance.distance(data, solutiontrans)));
			writer.close();

			if (rk.isUnstable())
				System.err.println("unstable!");
			else {
				Plot plot = new Plot("Simulation", "time", "value");

				for (int i = 0; i < solution.length; i++) {
					for (int j = 0; j < solutiontrans.length; j++) {

						double sym = solutiontrans[j][i];
						double un = data[j][i];
						plot.setConnectedPoint(time, sym, j);
						plot.setUnconnectedPoint(time, un, 90 + j);

					}
					time += rk.getStepSize();

				}
				//save graph as jpg
				 BufferedImage img = new
				 BufferedImage(plot.getFunctionArea().getWidth(),
				 plot.getFunctionArea().getHeight(),
				 BufferedImage.TYPE_INT_RGB);
				 plot.getFunctionArea().paint(img.createGraphics());
				 ImageIO.write(img, "jpg", new
				 File(resultfolder+modelnr+"-graph.jpg"));
				 
				 //plot.dispose();
			}
		}
	}
}
