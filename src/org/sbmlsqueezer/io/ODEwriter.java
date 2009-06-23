package org.sbmlsqueezer.io;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

import org.sbmlsqueezer.kinetics.KineticLawGenerator;

/**
 * This class writes the differential equations given by the {@see
 * KineticLawGenerator} to a LaTeX or plain text file.
 * 
 * @since 2.0
 * @version
 * @author Andreas Dr&auml;ger (draeger) <andreas.draeger@uni-tuebingen.de>
 * @author Nadine Hassis <Nadine.hassis@gmail.com> Copyright (c) ZBiT,
 *         University of T&uuml;bingen, Germany Compiler: JDK 1.6.0
 * @date Aug 1, 2007
 */
public class ODEwriter {

	/**
	 * <p>
	 * Default constructor.
	 * </p>
	 */
	public ODEwriter() {
	}

	/**
	 * <p>
	 * This constructor analyzes the given file. If it ends with "*.txt", a
	 * plain text file will be generated, which contains the ordinary equation
	 * system. If the file ends with "*.tex", the system will be written into a
	 * LaTeX file. Upper and lower cases for the file ending are ignored. In all
	 * other cases, nothing will happen.
	 * </p>
	 * 
	 * @param file
	 * @throws IOException
	 */
	public ODEwriter(File file, KineticLawGenerator klg) throws IOException {
		if ((new MyFileFilter(true, true)).accept(file)) {
			if (file.getPath().toLowerCase().endsWith(".txt")) {
				writeTextFile(file, klg);
			} else if (file.getPath().toLowerCase().toLowerCase().endsWith(
					".tex")) {
				writeLaTeXFile(file, klg);
			}
		}
	}

	/**
	 * This method writes the ordinary differential equation system given by the
	 * {@see KineticLawGenerator} into a LaTeX file for further processing. Note
	 * that the file extension does not matter.
	 * 
	 * @param file
	 * @param klg
	 * @throws IOException
	 * @deprecated Use LaTeXExport.
	 */
	@Deprecated
	public final void writeLaTeXFile(File file, KineticLawGenerator klg)
			throws IOException {
		int i;
		BufferedWriter out = new BufferedWriter(new OutputStreamWriter(
				new FileOutputStream(file.getPath())));
		append("\\documentclass[11pt,a4paper]{scrartcl}", out);
		append("\\usepackage[scaled=.9]{helvet}", out);
		append("\\usepackage{courier}", out);
		append("\\usepackage{times}", out);
		append("\\usepackage{a4wide}", out);
		append("\\usepackage[english]{babel}", out);
		append("\\usepackage{amsmath}", out);
		String title = "\\title{\\textsc{SBMLsqueezer}: Ordinary Differential Equation System ``";
		title += klg.getModel().getName().length() > 0 ? klg.getModel()
				.getName().replaceAll("_", " ") : klg.getModel().getId()
				.replaceAll("_", " ");
		append(title + "\"}", out);
		append(
				"\\author{Andreas Dr{\\\"a}ger\\and Nadine Hassis\\and Jochen Supper\\and Adrian Schr{\\\"o}der\\and Andreas Zell}",
				out);
		append("\\date{\\today}", out);
		append("\\begin{document}", out);
		append("\\maketitle", out);
		append(System.getProperty("line.separator"), out);
		// append("\\tableofcontents");
		append("\\section{Rate Laws}", out);

		for (i = 0; i < klg.getKineticLawsAsTeX().length; i++) {
			String toWriteKinetic = "v_{" + (i + 1) + "} = "
					+ klg.getKineticLawsAsTeX()[i];
			String toWriteReaction = "Reaction: \\texttt{"
					+ klg.getModel().getReaction(i).getId() + "}, "
					+ klg.getKineticLawNames()[i];

			append("\\subsection{" + toWriteReaction + "}", out);
			if (toWriteKinetic.endsWith("\\end{multline}")) {
				append("\\begin{multline}", out);
				append(toWriteKinetic, out);
			} else {
				append("\\begin{equation}", out);
				append(toWriteKinetic, out);
				append("\\end{equation}", out);
			}
		}

		append("\\section{Equations}", out);
		append("\\begin{description}", out);

		for (i = 0; i < klg.getModel().getNumSpecies(); i++) {
			String toWriteSpecies = "Species: \\texttt{"
					+ klg.getModel().getSpecies(i).getName().replaceAll("_",
							"\\_") + "} (\\texttt{"
					+ klg.getAllSpeciesNumAndIDs().get(i) + "})";
			String toWriteODE = "\\frac{\\mathrm d ["
					+ klg.getAllSpeciesNumAndIDs().get(i)
					+ "]}{\\mathrm d t} = "
					+ klg.getSpeciesAndSimpleODETeX().get(
							klg.getAllSpeciesNumAndIDs().get(i));

			append("\\item[" + toWriteSpecies + "]", out);
			append("\\begin{equation}", out);
			append(toWriteODE, out);
			append("\\end{equation}", out);
		}

		append("\\end{description}", out);
		append("\\newline",out);
		append("For a more comprehensive \\LaTeX{} export, see", out);
		append("\\begin{center}", out);
		append("http://www.ra.cs.uni-tuebingen.de/software/SBML2LaTeX", out);
		append("\\end{center}", out);
		append("\\end{document}", out);
		out.close();
	}

	/**
	 * This method writes the ordinary differential equation system given by the
	 * {@see KineticLawGenerator} into a plain text file. Note that the file
	 * extension does not matter.
	 * 
	 * @param file
	 * @param klg
	 * @throws IOException
	 */
	public final void writeTextFile(File file, KineticLawGenerator klg)
			throws IOException {
		int i;
		BufferedWriter out = new BufferedWriter(new OutputStreamWriter(
				new FileOutputStream(file.getPath())));
		append("SBMLsqueezer generated and transfered values", out);
		append("--------------------------------------------", out);

		for (i = 0; i < klg.getReactionNumAndKineticLaw().size(); i++) {
			String toWriteReaction = "Reaction: "
					+ klg.getReactionNumAndId().get(i) + ", "
					+ klg.getReactionNumAndKineticLaw().get(i).getName();
			String toWrite = "Kinetic: v" + i + " = "
					+ klg.getReactionNumAndKineticLaw().get(i).getFormula();
			append(toWriteReaction, out);
			append(toWrite, out);
			append(" ", out);
		}
		append(" ", out);
		for (i = 0; i < klg.getAllSpeciesNumAndIDs().size(); i++) {
			String toWrite = "Species: "
					+ klg.getAllSpeciesNumAndIDs().get(i)
					+ " ODE: d["
					+ klg.getAllSpeciesNumAndIDs().get(i)
					+ "]/dt = "
					+ klg.getSpecieAndSimpleODE().get(
							klg.getAllSpeciesNumAndIDs().get(i));
			append(toWrite, out);
			append(" ", out);
		}
		out.close();
	}

	/**
	 * This method appends one line to the given writer.
	 * 
	 * @param str
	 * @param writer
	 * @throws IOException
	 */
	private final void append(String str, BufferedWriter writer)
			throws IOException {
		writer.write(str);
		writer.newLine();
	}

}
