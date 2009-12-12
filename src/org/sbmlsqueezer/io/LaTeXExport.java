/**
 * @date Nov, 2007
 * @since 2.0
 * @version
 * @author Andreas Dr&auml;ger (draeger) <andreas.draeger@uni-tuebingen.de>
 * @author Dieudonne Motsou Wouamba <dwouamba@yahoo.fr> Copyright (c) ZBiT,
 *         University of T&uuml;bingen, Germany Compiler: JDK 1.6.0
 */
package org.sbmlsqueezer.io;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.StringTokenizer;
import java.util.Vector;

import jp.sbi.celldesigner.plugin.PluginCompartment;
import jp.sbi.celldesigner.plugin.PluginEvent;
import jp.sbi.celldesigner.plugin.PluginModel;
import jp.sbi.celldesigner.plugin.PluginReaction;
import jp.sbi.celldesigner.plugin.PluginSBase;
import jp.sbi.celldesigner.plugin.PluginSpecies;
import jp.sbi.celldesigner.plugin.PluginSpeciesReference;

import org.sbml.libsbml.ASTNode;
import org.sbml.libsbml.StoichiometryMath;
import org.sbml.libsbml.libsbmlConstants;

/**
 * This class is used to export a sbml model as LaTex file.
 * 
 * @since 2.0
 * @version
 * @author Dieudonne Motsou Wouamba <dwouamba@yahoo.fr>
 * @author Andreas Dr&auml;ger (draeger) <andreas.draeger@uni-tuebingen.de>
 *         Copyright (c) ZBiT, University of T&uuml;bingen, Germany Compiler:
 *         JDK 1.6.0
 * @date Dec 4, 2007
 */
public class LaTeXExport extends LaTeX implements libsbmlConstants {

	/**
	 * New line separator of this operating system
	 */
	private final String newLine = System.getProperty("line.separator");

	/**
	 * This is a LaTeX line break. The line break symbol double backslash
	 * followed by a new line symbol of the operating system.
	 */
	private final String lineBreak = "\\\\" + newLine;

	/**
	 * This is the font size to be used in this document. Allowed values are:
	 * <ul>
	 * <li>8</li>
	 * <li>9</li>
	 * <li>10</li>
	 * <li>11</li>
	 * <li>12</li>
	 * <li>14</li>
	 * <li>16</li>
	 * <li>17</li>
	 * </ul>
	 * Other values are set to the default of 11.
	 */
	private short fontSize;

	/**
	 * Allowed are
	 * <ul>
	 * <li>letter</li>
	 * <li>legal</li>
	 * <li>executive</li>
	 * <li>a* where * stands for values from 0 thru 9</li>
	 * <li>b*</li>
	 * <li>c*</li>
	 * <li>d*</li>
	 * </ul>
	 * The default is a4.
	 */
	private String paperSize;

	/**
	 * If true a title page will be created by LaTeX for the resulting document.
	 * Otherwise there will only be a title on top of the first page.
	 */
	private boolean titlepage;

	/**
	 * If true this will produce LaTeX files for for entirely landscape
	 * documents
	 */
	private boolean landscape;

	/**
	 * If true species (reactants, modifiers and products) in reaction equations
	 * will be displayed with their name if they have one. By default the ids of
	 * the species are used in these equations.
	 */
	private boolean printNameIfAvailable = true;

	/**
	 * If true ids are set in typewriter font (default).
	 */
	private boolean typeWriter = true;

	// private boolean numberEquations = true;

	/**
	 * Constructs a new instance of LaTeX export. For each document to be
	 * translated a new instance has to be created. Here default values are used
	 * (A4 paper, 11pt, portrait, fancy headings, no titlepage).
	 */
	public LaTeXExport() {
		this(false, true, (short) 11, "a4", /* true, */false, false/* , true */);
	}

	/**
	 * Constructs a new instance of LaTeX export. For each document to be
	 * translated a new instance has to be created. This constructor allows you
	 * to set many properties of the resulting LaTeX file.
	 * 
	 * @param landscape
	 *            If <code>true</code> the whole document will be set to
	 *            landscape format, otherwise portrait.
	 * @param typeWriter
	 *            If <code>true</code> ids are set in typewriter font (default).
	 *            Otherwise the regular font is used.
	 * @param fontSize
	 *            The size of the font to be used here. The default is 11.
	 *            Allowed values are 8, 9, 10, 11, 12, 14, 16 and 17.
	 * @param paperSize
	 *            Allowed are
	 *            <ul>
	 *            <li>letter</li>
	 *            <li>legal</li>
	 *            <li>executive</li>
	 *            <li>a* where * stands for values from 0 thru 9</li>
	 *            <li>b*</li>
	 *            <li>c*</li>
	 *            <li>d*</li>
	 *            </ul>
	 * @param addMissingUnitDeclarations
	 *            If true SBML built-in units will be made explicitly if not
	 *            overridden in the model.
	 * @param titlepage
	 *            if true a title page will be created for the model report.
	 *            Default is false (just a caption).
	 */

	public LaTeXExport(boolean landscape, boolean typeWriter, short fontSize,
			String paperSize, /* boolean addMissingUnitDeclarations, */
			boolean titlepage, boolean printNameIfAvailable/*
															 * , boolean
															 * numberEquations
															 */) {
		setLandscape(landscape);
		setTypeWriter(typeWriter);
		setFontSize(fontSize);
		setPaperSize(paperSize);
		setTitlepage(titlepage);
		setPrintNameIfAvailable(printNameIfAvailable);
		// setNumberEquations(numberEquations);
	}

	/**
	 * Writing laTeX code of a string id
	 * 
	 * @param pluginSpecies
	 * @return String
	 */
	public String idToTeX(PluginSpecies pluginSpecies) {
		return nameToTeX(pluginSpecies.getId());
	}

	/**
	 * Writing laTeX code of a string name
	 * 
	 * @param name
	 * @return String
	 */
	public String nameToTeX(String name) {
		String speciesTeX = name;
		int numUnderscore = (new StringTokenizer(speciesTeX, "_"))
				.countTokens() - 1;
		if (numUnderscore > 1)
			speciesTeX = replaceAll("_", speciesTeX, "\\_");
		else if ((numUnderscore == 0) && (0 < speciesTeX.length())) {
			int index = -1;
			while (index != (name.length() - 1)
					&& !Character.isDigit(name.charAt(index + 1)))
				index++;
			if ((-1 < index) && (index < name.length())) {
				String num = name.substring(++index);
				speciesTeX = speciesTeX.substring(0, index++) + "_";
				speciesTeX += (num.length() == 1) ? num : "{" + num + "}";
			}
		}
		return speciesTeX;
	}

	/**
	 * a methode for string replacement
	 * 
	 * @param what
	 * @param inString
	 * @param replacement
	 * @return string
	 */
	public String replaceAll(String what, String inString, String replacement) {
		StringTokenizer st = new StringTokenizer(inString, what);
		String end = st.nextElement().toString();
		while (st.hasMoreElements())
			end += replacement + st.nextElement().toString();
		return end;
	}

	/**
	 * This is the font size to be used in this document.
	 * 
	 * @param Allowed
	 *            values are:
	 *            <ul>
	 *            <li>8</li>
	 *            <li>9</li>
	 *            <li>10</li>
	 *            <li>11</li>
	 *            <li>12</li>
	 *            <li>14</li>
	 *            <li>16</li>
	 *            <li>17</li>
	 *            </ul>
	 *            Other values are set to the default of 11.
	 * 
	 */
	public void setFontSize(short fontSize) {
		if ((fontSize < 8) || (fontSize == 13) || (17 < fontSize))
			this.fontSize = 11;
		this.fontSize = fontSize;
	}

	/**
	 * If true is given the whole document will be created in landscape mode.
	 * Default is portrait.
	 * 
	 * @param landscape
	 */
	public void setLandscape(boolean landscape) {
		this.landscape = landscape;
	}

	/**
	 * Allowed are
	 * <ul>
	 * <li>letter</li>
	 * <li>legal</li>
	 * <li>executive</li>
	 * <li>a* where * stands for values from 0 thru 9</li>
	 * <li>b*</li>
	 * <li>c*</li>
	 * <li>d*</li>
	 * </ul>
	 * The default is a4.
	 */
	public void setPaperSize(String paperSize) {
		paperSize = paperSize.toLowerCase();
		if (paperSize.equals("letter") || paperSize.equals("legal")
				|| paperSize.equals("executive"))
			this.paperSize = paperSize;
		else if (paperSize.length() == 2) {
			if (!Character.isDigit(paperSize.charAt(1))
					|| ((paperSize.charAt(0) != 'a')
							&& (paperSize.charAt(0) != 'b')
							&& (paperSize.charAt(0) != 'c') && (paperSize
							.charAt(0) != 'd')))
				this.paperSize = "a4";
			else {
				short size = Short.parseShort(Character.toString(paperSize
						.charAt(1)));
				if ((0 <= size) && (size < 10))
					this.paperSize = paperSize;
				else
					this.paperSize = "a4";
			}
		} else
			this.paperSize = "a4";
		this.paperSize = paperSize;
	}

	/**
	 * If true species (reactants, modifiers and products) in reaction equations
	 * will be displayed with their name if they have one. By default the ids of
	 * the species are used in these equations.
	 */
	public void setPrintNameIfAvailable(boolean printNameIfAvailable) {
		this.printNameIfAvailable = printNameIfAvailable;
	}

	/**
	 * If true an extra title page is created. Default false.
	 * 
	 * @param titlepage
	 */
	public void setTitlepage(boolean titlepage) {
		this.titlepage = titlepage;
	}

	/**
	 * If true ids are set in typewriter font (default).
	 * 
	 * @param typeWriter
	 */
	public void setTypeWriter(boolean typeWriter) {
		this.typeWriter = typeWriter;
	}

	/**
	 * This is a method to write the latex file
	 * 
	 * @param astnode
	 * @param file
	 * @throws IOException
	 */

	public StringBuffer toLaTeX(PluginModel model) throws IOException {
		StringBuffer laTeX;
		String newLine = System.getProperty("line.separator");
		String title = model.getName().length() > 0 ? model.getName()
				.replaceAll("_", " ") : model.getId().replaceAll("_", " ");
		StringBuffer head = getDocumentHead(title);

		String rateHead = newLine + "\\section{Rate Laws}" + newLine;
		String speciesHead = newLine + "\\section{Equations}";
		String begin = /* (numberEquations) ? */newLine + "\\begin{equation}"
				+ newLine/* : newLine + "\\begin{equation*}" + newLine */;
		String end = /* (numberEquations) ? */newLine + "\\end{equation}"
				+ newLine
		/* : newLine + "\\end{equation*}" + newLine */;
		String tail = newLine
				+ "\\begin{center}"
				+ newLine
				+ "For a more comprehensive \\LaTeX{} export, see "
				+ "\\url{http://www.ra.cs.uni-tuebingen.de/software/SBML2LaTeX}"
				+ newLine + "\\end{center}" + newLine + "\\end{document}"
				+ newLine + newLine;

		String rateLaws[] = new String[(int) model.getNumReactions()];
		String sp[] = new String[(int) model.getNumSpecies()];
		int reactionIndex, speciesIndex, sReferenceIndex;
		PluginSpecies species;
		PluginSpeciesReference speciesRef;
		HashMap<String, Integer> speciesIDandIndex = new HashMap<String, Integer>();
		for (reactionIndex = 0; reactionIndex < model.getNumReactions(); reactionIndex++) {
			PluginReaction r = model.getReaction(reactionIndex);
			int latexReactionIndex = reactionIndex + 1;

			rateLaws[reactionIndex] = (!r.getName().equals("") && !r.getName()
					.equals(r.getId())) ? "\\subsection{Reaction: \\texttt{"
					+ replaceAll("_", r.getId(), "\\_") + "}" + " ("
					+ replaceAll("_", r.getName(), "\\_") + ")}" + newLine
					+ begin + "v_{" + latexReactionIndex + "}="
					: "\\subsection{Reaction: \\texttt{"
							+ replaceAll("_", r.getId(), "\\_") + "}}"
							+ newLine + begin + "v_{" + latexReactionIndex
							+ "}=";
			if (r.getKineticLaw() != null) {
				if (r.getKineticLaw().getMath() != null)
					rateLaws[reactionIndex] += toLaTeX(model, r.getKineticLaw()
							.getMath());
				else
					rateLaws[reactionIndex] += "\\text{no mathematics specified}";
			} else
				rateLaws[reactionIndex] += "\\text{no kinetic law specified}";
			for (speciesIndex = 0; speciesIndex < model.getNumSpecies(); speciesIndex++) {
				speciesIDandIndex.put(model.getSpecies(speciesIndex).getId(),
						Integer.valueOf(speciesIndex));
			}
		}

		Vector<PluginSpecies> reactants = new Vector<PluginSpecies>();
		Vector<PluginSpecies> products = new Vector<PluginSpecies>();
		Vector<Integer> reactantsReaction = new Vector<Integer>();
		Vector<Integer> productsReaction = new Vector<Integer>();
		Vector<PluginSpeciesReference> reactantsStochiometric = new Vector<PluginSpeciesReference>();
		Vector<PluginSpeciesReference> productsStochiometric = new Vector<PluginSpeciesReference>();

		for (reactionIndex = 0; reactionIndex < model.getNumReactions(); reactionIndex++) {
			PluginReaction r = model.getReaction(reactionIndex);
			int latexReactionIndex = reactionIndex + 1;
			int reactant = 0;
			int product = 0;
			for (sReferenceIndex = 0; sReferenceIndex < r.getNumReactants(); sReferenceIndex++) {
				speciesRef = r.getReactant(sReferenceIndex);
				speciesIndex = (int) speciesIDandIndex.get(
						speciesRef.getSpecies()).longValue();
				species = model.getSpecies(speciesIndex);
				reactants.add(reactant, species);
				reactantsReaction.add(reactant, latexReactionIndex);
				reactantsStochiometric.add(reactant, speciesRef);
				reactant++;
			}

			for (sReferenceIndex = 0; sReferenceIndex < r.getNumProducts(); sReferenceIndex++) {
				speciesRef = r.getProduct(sReferenceIndex);
				speciesIndex = (int) speciesIDandIndex.get(
						speciesRef.getSpecies()).longValue();
				species = model.getSpecies(speciesIndex);
				products.add(product, species);
				productsReaction.add(product, latexReactionIndex);
				productsStochiometric.add(product, speciesRef);
				product++;
			}
		}
		for (speciesIndex = 0; speciesIndex < model.getNumSpecies(); speciesIndex++) {
			String sEquation = "";
			ASTNode stoch = null;
			StoichiometryMath stochMath;
			PluginSpeciesReference ref;
			species = model.getSpecies(speciesIndex);
			for (int k = 0; k < reactants.size(); k++) {
				if (species.getId().equals(reactants.get(k).getId())) {
					ref = reactantsStochiometric.get(k);
					if (ref != null) {
						stochMath = ref.getStoichiometryMath();
						if (stochMath != null && stochMath.isSetMath()) {
							stoch = stochMath.getMath();
							sEquation += (stoch.getType() == AST_PLUS || stoch
									.getType() == AST_MINUS) ? sEquation += "-\\left("
									+ toLaTeX(model, stoch)
									+ "\\right)v_{"
									+ reactantsReaction.get(k) + "}"
									: "-" + toLaTeX(model, stoch) + "v_{"
											+ reactantsReaction.get(k) + "}";
						} else {
							double doubleStoch = reactantsStochiometric.get(k)
									.getStoichiometry();
							if (doubleStoch == 1.0)
								sEquation += "-v_{" + reactantsReaction.get(k)
										+ "}";
							else {
								int intStoch = (int) doubleStoch;
								if ((doubleStoch - intStoch) == 0.0)
									sEquation += "-" + intStoch + "v_{"
											+ reactantsReaction.get(k) + "}";
								else
									sEquation += "-" + doubleStoch + "v_{"
											+ reactantsReaction.get(k) + "}";
							}
						}
					}
				}
			}

			for (int k = 0; k < products.size(); k++) {
				if (species.getId().equals(products.get(k).getId())) {
					ref = productsStochiometric.get(k);
					if (ref != null) {
						stochMath = ref.getStoichiometryMath();
						if (stochMath != null) {
							if (stochMath.isSetMath())
								stoch = stochMath.getMath();
							if (sEquation == "") {
								if (stoch != null) {
									sEquation += (stoch.getType() == AST_PLUS || stoch
											.getType() == AST_MINUS) ? sEquation += "\\left("
											+ toLaTeX(model, stoch)
											+ "\\right)v_{"
											+ productsReaction.get(k) + "}"
											: toLaTeX(model, stoch) + "v_{"
													+ productsReaction.get(k)
													+ "}";
								} else {
									double doubleStoch = productsStochiometric
											.get(k).getStoichiometry();
									if (doubleStoch == 1.0)
										sEquation += "v_{"
												+ productsReaction.get(k) + "}";
									else {
										int intStoch = (int) doubleStoch;
										if ((doubleStoch - intStoch) == 0.0)
											sEquation += intStoch + "v_{"
													+ productsReaction.get(k)
													+ "}";
										else
											sEquation += doubleStoch + "v_{"
													+ productsReaction.get(k)
													+ "}";
									}
								}

							} else {
								if (stoch != null) {
									sEquation += (stoch.getType() == AST_PLUS || stoch
											.getType() == AST_MINUS) ? sEquation += "+\\left("
											+ toLaTeX(model, stoch)
											+ "\\right)v_{"
											+ productsReaction.get(k) + "}"
											: "+" + toLaTeX(model, stoch)
													+ "v_{"
													+ productsReaction.get(k)
													+ "}";
								} else {
									double doubleStoch = productsStochiometric
											.get(k).getStoichiometry();
									if (doubleStoch == 1.0)
										sEquation += "+v_{"
												+ productsReaction.get(k) + "}";
									else {
										int intStoch = (int) doubleStoch;
										if ((doubleStoch - intStoch) == 0.0)
											sEquation += "+" + intStoch + "v_{"
													+ productsReaction.get(k)
													+ "}";
										else
											sEquation += "+" + doubleStoch
													+ "v_{"
													+ productsReaction.get(k)
													+ "}";
									}
								}
							}
						}
					}
				}
			}

			if (sEquation.equals("")) {
				sp[speciesIndex] = (!species.getName().equals("") && !species
						.getName().equals(species.getId())) ? "\\subsection{Species: \\texttt{"
						+ replaceAll("_", species.getId(), "\\_")
						+ "}"
						+ " ("
						+ replaceAll("_", species.getName(), "\\_")
						+ ")}"
						+ begin
						+ "\\frac{\\mathrm {d["
						+ idToTeX(species)
						+ "]}}{\\mathrm dt}= 0"
						: "\\subsection{Species: \\texttt{"
								+ replaceAll("_", species.getId(), "\\_")
								+ "}}" + begin + "\\frac{\\mathrm{d["
								+ idToTeX(species) + "]}}{\\mathrm dt}= 0";
			} else if (!species.getBoundaryCondition()
					&& !species.getConstant()) {
				sp[speciesIndex] = (!species.getName().equals("") && !species
						.getName().equals(species.getId())) ? "\\subsection{Species: \\texttt{"
						+ replaceAll("_", species.getId(), "\\_")
						+ "}"
						+ " ("
						+ replaceAll("_", species.getName(), "\\_")
						+ ")}"
						+ begin
						+ "\\frac{\\mathrm{d["
						+ idToTeX(species)
						+ "]}}{\\mathrm dt}= " + sEquation
						: "\\subsection{Species: \\texttt{"
								+ replaceAll("_", species.getId(), "\\_")
								+ "}}" + begin + "\\frac{\\mathrm{d["
								+ idToTeX(species) + "]}}{\\mathrm {dt}}= "
								+ sEquation;
			} else {
				sp[speciesIndex] = (!species.getName().equals("") && !species
						.getName().equals(species.getId())) ? "\\subsection{Species: \\texttt{"
						+ replaceAll("_", species.getId(), "\\_")
						+ "}"
						+ " ("
						+ replaceAll("_", species.getName(), "\\_")
						+ ")}"
						+ begin
						+ "\\frac{\\mathrm {d["
						+ idToTeX(species)
						+ "]}}{\\mathrm {dt}}= 0"
						: "\\subsection{Species: \\texttt{"
								+ replaceAll("_", species.getId(), "\\_")
								+ "}}" + begin + "\\frac{\\mathrm {d["
								+ idToTeX(species) + "]}}{\\mathrm {dt}}= 0";
			}
		}
		// String rulesHead = newLine + "\\section{Rules}" + newLine;
		String eventsHead = newLine + "\\section{Events}";
		// String constraintsHead = newLine + "\\section{Constraints}";
		LinkedList events[] = new LinkedList[(int) model.getNumEvents()];
		int i;
		// writing latex
		laTeX = head;
		// writing Rate Laws
		laTeX.append(rateHead);
		for (i = 0; i < rateLaws.length; i++) {
			laTeX.append(rateLaws[i] + end);
		}
		// writing Equations
		laTeX.append(speciesHead);
		for (i = 0; i < sp.length; i++) {
			laTeX.append(sp[i] + end);
		}
		// writing Rules

		// writing Events
		if (model.getNumEvents() > 0) {
			PluginEvent ev;
			for (i = 0; i < model.getNumEvents(); i++) {
				ev = model.getEvent(i);
				LinkedList<StringBuffer> assignments = new LinkedList<StringBuffer>();
				assignments.add(toLaTeX(model, ev.getTrigger().getMath()));
				for (int j = 0; j < ev.getNumEventAssignments(); j++)
					assignments.add(toLaTeX(model, ev.getEventAssignment(j)
							.getMath()));
				events[i] = assignments;
			}
			laTeX.append(eventsHead);
			String var;
			for (i = 0; i < events.length; i++) {
				ev = model.getEvent(i);
				if (ev.getName() == null)
					laTeX.append("\\subsection{Event:}");
				else
					laTeX.append("\\subsection{Event: " + ev.getName() + "}");
				if (ev.getNumEventAssignments() > 1) {
					laTeX.append("\\texttt{Triggers if: }" + newLine);
					laTeX.append(/* (numberEquations) ? */"\\begin{equation}"
							+ events[i].get(0) + "\\end{equation}" + newLine);
					/*
					 * : "\\begin{equation*}" + events[i].get(0) +
					 * "\\end{equation*}" + newLine
					 */;
					if (ev.getDelay() == null)
						laTeX.append(newLine
								+ "\\texttt{and assigns the following rule: }"
								+ newLine);
					else {
						laTeX.append(newLine
								+ "\\texttt{and assigns after a delay of "
								+ toLaTeX(model, ev.getDelay().getMath()));
						if (!ev.getTimeUnits().equals(null))
							laTeX.append(ev.getTimeUnits()
									+ " the following rules: }" + newLine);
						else
							laTeX.append(" s the following rules: }" + newLine);
					}
				} else {
					laTeX.append("\\texttt{Triggers if: }" + newLine);
					laTeX.append(/* (numberEquations) ? */"\\begin{equation}"
							+ events[i].get(0) + "\\end{equation}" + newLine)
					/*
					 * : "\\begin{equation*}" + events[i].get(0) +
					 * "\\end{equation*}" + newLine
					 */;
					if (ev.getDelay() == null)
						laTeX.append(newLine
								+ "\\texttt{and assigns the following rule: }"
								+ newLine);
					else {
						laTeX.append(newLine
								+ "\\texttt{and assigns after a delay of "
								+ toLaTeX(model, ev.getDelay().getMath()));
						if (!ev.getTimeUnits().equals(null))
							laTeX.append(ev.getTimeUnits()
									+ " the following rule: }" + newLine);
						else
							laTeX.append(" s the following rule: }" + newLine);
					}
				}
				if (events[i].size() > 1)
					for (int j = 0; j < events[i].size() - 1; j++) {
						var = ev.getEventAssignment(j).getVariable();
						if (model.getSpecies(var) != null)
							laTeX.append(begin + "["
									+ idToTeX(model.getSpecies(var)) + "]"
									+ " = " + events[i].get(j + 1) + end
									+ newLine);
						else if (model.getParameter(var) != null)
							laTeX.append(begin
									+ toTeX(model.getParameter(var).getId())
									+ " = " + events[i].get(j + 1) + end
									+ newLine);
						else
							laTeX.append(begin + events[i].get(j + 1) + end
									+ newLine);
					}
				else
					for (int j = 0; j < events[i].size() - 1; j++) {
						var = ev.getEventAssignment(j).getVariable();
						if (model.getSpecies(var) != null)
							laTeX.append(begin + "["
									+ idToTeX(model.getSpecies(var)) + "]"
									+ " = " + events[i].get(j + 1) + end
									+ newLine);
						else if (model.getParameter(var) != null)
							laTeX.append(begin
									+ toTeX(model.getParameter(var).getId())
									+ " = " + events[i].get(j + 1) + end
									+ newLine);
						else
							laTeX.append(begin + events[i].get(j + 1) + end
									+ newLine);
					}
			}
		}

		// writing Constraints

		// writing parameters
		if (model.getNumParameters() > 0) {
			laTeX.append(newLine + "\\section{Parameters}");
			laTeX.append("\\begin{longtable}{@{}llr@{}}" + newLine + "\\toprule "
					+ newLine + "Parameter & Value \\\\  " + newLine
					+ "\\midrule" + newLine);
			for (i = 0; i < model.getNumParameters(); i++) {
				laTeX.append(name_idToLaTex(model.getParameter(i).getId()) + "&"
						+ model.getParameter(i).getValue() + "\\\\" + newLine);
			}
			laTeX.append("\\bottomrule " + newLine + "\\end{longtable}");
		}
		// writing species list and compartment.
		if (model.getNumSpecies() > 0) {
			laTeX.append(newLine + "\\section{Species}" + newLine);
			laTeX.append("\\begin{longtable}{@{}llr@{}} " + newLine + "\\toprule "
					+ newLine
					+ "Species & Initial concentration & compartment \\\\  "
					+ newLine + "\\midrule" + newLine);
			for (i = 0; i < model.getNumSpecies(); i++) {
				laTeX.append(name_idToLaTex(model.getSpecies(i).getId()) + "&"
						+ model.getSpecies(i).getInitialConcentration() + "&"
						+ model.getSpecies(i).getCompartment() + "\\\\"
						+ newLine);
			}
			laTeX.append("\\bottomrule " + newLine + "\\end{longtable}");
		}
		if (model.getNumCompartments() > 0) {
			laTeX.append(newLine + "\\section{Compartments}");
			laTeX.append("\\begin{longtable}{@{}llr@{}}" + newLine + "\\toprule "
					+ newLine + "Compartment & Volume \\\\  " + newLine
					+ "\\midrule" + newLine);
			for (i = 0; i < model.getNumCompartments(); i++) {
				laTeX.append(name_idToLaTex(model.getCompartment(i).getId()) + "&"
						+ model.getCompartment(i).getVolume() + "\\\\"
						+ newLine);
			}
			laTeX.append("\\bottomrule " + newLine + "\\end{longtable}");
		}
		laTeX.append(newLine + tail);
		return laTeX;
	}

	/**
	 * Method that writes the kinetic law (mathematical formula) of into latex
	 * code
	 * 
	 * @param astnode
	 * @return String
	 */

	public StringBuffer toLaTeX(PluginModel model, ASTNode astnode)
			throws IOException {
		if (astnode == null)
			return mathrm("undefined");

		ASTNode ast;
		StringBuffer value;
		if (astnode.isUMinus()) {
			value = new StringBuffer('-');
			if (astnode.getLeftChild().getNumChildren() > 0)
				value.append(brackets(toLaTeX(model, astnode.getLeftChild())));
			else
				value.append(toLaTeX(model, astnode.getLeftChild()));
			return value;
		} else if (astnode.isSqrt())
			return sqrt(toLaTeX(model, astnode.getChild(astnode
					.getNumChildren() - 1)));
		else if (astnode.isInfinity())
			return new StringBuffer(POSITIVE_INFINITY);
		else if (astnode.isNegInfinity())
			return new StringBuffer(NEGATIVE_ININITY);

		switch (astnode.getType()) {
		/*
		 * Numbers
		 */
		case AST_REAL:
			return format(astnode.getReal());

		case AST_INTEGER:
			return value = new StringBuffer(Integer.toString(astnode
					.getInteger()));
			/*
			 * Basic Functions
			 */
		case AST_FUNCTION_LOG: {
			value = new StringBuffer("\\log");
			if (astnode.getNumChildren() == 2) {
				value.append("_{");
				value.append(toLaTeX(model, astnode.getLeftChild()));
				value.append('}');
			}
			value.append('{');
			if (astnode.getChild(astnode.getNumChildren() - 1).getNumChildren() > 0)
				value.append(brackets(toLaTeX(model, astnode.getChild(astnode
						.getNumChildren() - 1))));
			else
				value.append(toLaTeX(model, astnode.getChild(astnode
						.getNumChildren() - 1)));
			value.append('}');
			return value;
		}
			/*
			 * Operators
			 */
		case AST_POWER:
			value = toLaTeX(model, astnode.getLeftChild());
			if (astnode.getLeftChild().getNumChildren() > 0)
				value = brackets(value);
			value.append("^{");
			value.append(toLaTeX(model, astnode.getRightChild()));
			value.append("}");
			return value;

		case AST_PLUS:
			value = toLaTeX(model, astnode.getLeftChild());
			for (int i = 1; i < astnode.getNumChildren(); i++) {
				ast = astnode.getChild(i);
				value.append(" + ");
				if (ast.getType() == AST_MINUS)
					value.append(brackets(toLaTeX(model, ast)));
				else
					value.append(toLaTeX(model, ast));
			}
			return value;

		case AST_MINUS:
			value = toLaTeX(model, astnode.getLeftChild());
			for (int i = 1; i < astnode.getNumChildren(); i++) {
				ast = astnode.getChild(i);
				value.append(" - ");
				if (ast.getType() == AST_PLUS)
					value.append(brackets(toLaTeX(model, ast)));
				else
					value.append(toLaTeX(model, ast));
			}
			return value;

		case AST_TIMES:
			value = toLaTeX(model, astnode.getLeftChild());
			if (astnode.getLeftChild().getNumChildren() > 1
					&& (astnode.getLeftChild().getType() == AST_MINUS || astnode
							.getLeftChild().getType() == AST_PLUS))
				value = brackets(value);
			for (int i = 1; i < astnode.getNumChildren(); i++) {
				ast = astnode.getChild(i);
				value.append("\\cdot");
				if ((ast.getType() == AST_MINUS) || (ast.getType() == AST_PLUS))
					value.append(brackets(toLaTeX(model, ast)));
				else {
					value.append(' ');
					value.append(toLaTeX(model, ast));
				}
			}
			return value;

		case AST_DIVIDE:
			return frac(toLaTeX(model, astnode.getLeftChild()), toLaTeX(model,
					astnode.getRightChild()));

		case AST_RATIONAL:
			return frac(Double.toString(astnode.getNumerator()), Double
					.toString(astnode.getDenominator()));

		case AST_NAME_TIME:
			return mathrm(astnode.getName());

		case AST_FUNCTION_DELAY:
			return mathrm(astnode.getName());

			/*
			 * Names of identifiers: parameters, functions, species etc.
			 */
		case AST_NAME:
			if (model.getSpecies(astnode.getName()) != null) {
				// Species.
				PluginSpecies species = model.getSpecies(astnode.getName());
				PluginCompartment c = model.getCompartment(species
						.getCompartment());
				boolean concentration = !species.getHasOnlySubstanceUnits()
						&& (0 < c.getSpatialDimensions());
				value = new StringBuffer();
				if (concentration)
					value.append('[');
				value.append(getNameOrID(species));
				if (concentration)
					value.append(']');
				return value;

			} else if (model.getCompartment(astnode.getName()) != null) {
				// Compartment
				PluginCompartment c = model.getCompartment(astnode.getName());
				return getSize(c);
			}
			// TODO: weitere spezialfälle von Namen!!!
			return value = new StringBuffer(mathtt(maskSpecialChars(astnode
					.getName())));
			/*
			 * Constants: pi, e, true, false
			 */
		case AST_CONSTANT_PI:
			return new StringBuffer(CONSTANT_PI);
		case AST_CONSTANT_E:
			return new StringBuffer(CONSTANT_E);
		case AST_CONSTANT_TRUE:
			return new StringBuffer(CONSTANT_TRUE);
		case AST_CONSTANT_FALSE:
			return new StringBuffer(CONSTANT_FALSE);
		case AST_REAL_E:
			return new StringBuffer(Double.toString(astnode.getReal()));
			/*
			 * More complicated functions
			 */
		case AST_FUNCTION_ABS:
			return abs(toLaTeX(model, astnode
					.getChild(astnode.getNumChildren() - 1)));

		case AST_FUNCTION_ARCCOS:
			if (astnode.getLeftChild().getNumChildren() > 0)
				return arccos(brackets(toLaTeX(model, astnode.getLeftChild())));
			return arccos(toLaTeX(model, astnode.getLeftChild()));

		case AST_FUNCTION_ARCCOSH:
			if (astnode.getLeftChild().getNumChildren() > 0)
				return arccosh(brackets(toLaTeX(model, astnode.getLeftChild())));
			return arccosh(toLaTeX(model, astnode.getLeftChild()));

		case AST_FUNCTION_ARCCOT:
			value = new StringBuffer("\\arcot{");
			if (0 < astnode.getLeftChild().getNumChildren())
				value.append(brackets(toLaTeX(model, astnode.getLeftChild())));
			else
				value.append(toLaTeX(model, astnode.getLeftChild()));
			value.append('}');
			return value;

		case AST_FUNCTION_ARCCOTH:
			value = mathrm("arccoth");
			if (0 < astnode.getLeftChild().getNumChildren())
				value.append(brackets(toLaTeX(model, astnode.getLeftChild())));
			else
				value.append(toLaTeX(model, astnode.getLeftChild()));
			return value;

		case AST_FUNCTION_ARCCSC:
			value = new StringBuffer("\\arccsc{");
			if (0 < astnode.getLeftChild().getNumChildren())
				value.append(brackets(toLaTeX(model, astnode.getLeftChild())));
			else
				value.append(toLaTeX(model, astnode.getLeftChild()));
			value.append('}');
			return value;

		case AST_FUNCTION_ARCCSCH:
			value = mathrm("arccsh");
			if (0 < astnode.getLeftChild().getNumChildren())
				value.append(brackets(toLaTeX(model, astnode.getLeftChild())));
			else
				value.append(toLaTeX(model, astnode.getLeftChild()));
			return value;

		case AST_FUNCTION_ARCSEC:
			value = new StringBuffer("\\arcsec{");
			if (0 < astnode.getLeftChild().getNumChildren())
				value.append(brackets(toLaTeX(model, astnode.getLeftChild())));
			else
				value.append(toLaTeX(model, astnode.getLeftChild()));
			value.append('}');
			return value;

		case AST_FUNCTION_ARCSECH:
			value = mathrm("arcsech");
			if (0 < astnode.getLeftChild().getNumChildren())
				value.append(brackets(toLaTeX(model, astnode.getLeftChild())));
			else
				value.append(toLaTeX(model, astnode.getLeftChild()));
			return value;

		case AST_FUNCTION_ARCSIN:
			value = new StringBuffer("\\arcsin{");
			if (0 < astnode.getLeftChild().getNumChildren())
				value.append(brackets(toLaTeX(model, astnode.getLeftChild())));
			else
				value.append(toLaTeX(model, astnode.getLeftChild()));
			value.append('}');
			return value;

		case AST_FUNCTION_ARCSINH:
			value = mathrm("arcsinh");
			if (0 < astnode.getLeftChild().getNumChildren())
				value.append(brackets(toLaTeX(model, astnode.getLeftChild())));
			else
				value.append(toLaTeX(model, astnode.getLeftChild()));
			return value;

		case AST_FUNCTION_ARCTAN:
			value = new StringBuffer("\\arctan{");
			if (0 < astnode.getLeftChild().getNumChildren())
				value.append(brackets(toLaTeX(model, astnode.getLeftChild())));
			else
				value.append(toLaTeX(model, astnode.getLeftChild()));
			value.append('}');
			return value;

		case AST_FUNCTION_ARCTANH:
			value = new StringBuffer("\\arctanh{");
			if (0 < astnode.getLeftChild().getNumChildren())
				value.append(brackets(toLaTeX(model, astnode.getLeftChild())));
			else
				value.append(toLaTeX(model, astnode.getLeftChild()));
			value.append('}');
			return value;

		case AST_FUNCTION_CEILING:
			return ceiling(toLaTeX(model, astnode.getLeftChild()));

		case AST_FUNCTION_COS:
			value = new StringBuffer("\\cos{");
			if (0 < astnode.getLeftChild().getNumChildren())
				value.append(brackets(toLaTeX(model, astnode.getLeftChild())));
			else
				value.append(toLaTeX(model, astnode.getLeftChild()));
			value.append('}');
			return value;

		case AST_FUNCTION_COSH:
			value = new StringBuffer("\\cosh{");
			if (0 < astnode.getLeftChild().getNumChildren())
				value.append(brackets(toLaTeX(model, astnode.getLeftChild())));
			else
				value.append(toLaTeX(model, astnode.getLeftChild()));
			value.append('}');
			return value;

		case AST_FUNCTION_COT:
			value = new StringBuffer("\\cot{");
			if (0 < astnode.getLeftChild().getNumChildren())
				value.append(brackets(toLaTeX(model, astnode.getLeftChild())));
			else
				value.append(toLaTeX(model, astnode.getLeftChild()));
			value.append('}');
			return value;

		case AST_FUNCTION_COTH:
			value = new StringBuffer("\\coth{");
			if (0 < astnode.getLeftChild().getNumChildren())
				value.append(brackets(toLaTeX(model, astnode.getLeftChild())));
			else
				value.append(toLaTeX(model, astnode.getLeftChild()));
			value.append('}');
			return value;

		case AST_FUNCTION_CSC:
			value = new StringBuffer("\\csc{");
			if (0 < astnode.getLeftChild().getNumChildren())
				value.append(brackets(toLaTeX(model, astnode.getLeftChild())));
			else
				value.append(toLaTeX(model, astnode.getLeftChild()));
			value.append('}');
			return value;

		case AST_FUNCTION_CSCH:
			value = mathrm("csch");
			if (0 < astnode.getLeftChild().getNumChildren())
				value.append(brackets(toLaTeX(model, astnode.getLeftChild())));
			else
				value.append(toLaTeX(model, astnode.getLeftChild()));
			return value;

		case AST_FUNCTION_EXP:
			value = new StringBuffer("\\exp{");
			if (0 < astnode.getLeftChild().getNumChildren())
				value.append(brackets(toLaTeX(model, astnode.getLeftChild())));
			else
				value.append(toLaTeX(model, astnode.getLeftChild()));
			value.append('}');
			return value;

		case AST_FUNCTION_FACTORIAL:
			if (astnode.getLeftChild().getNumChildren() > 0)
				value = brackets(toLaTeX(model, astnode.getLeftChild()));
			else
				value = new StringBuffer(toLaTeX(model, astnode.getLeftChild()));
			value.append('!');
			return value;

		case AST_FUNCTION_FLOOR:
			return floor(toLaTeX(model, astnode.getLeftChild()));

		case AST_FUNCTION_LN:
			value = new StringBuffer("\\ln{");
			if (astnode.getLeftChild().getNumChildren() > 0)
				value = brackets(toLaTeX(model, astnode.getLeftChild()));
			else
				value.append(toLaTeX(model, astnode.getLeftChild()));
			value.append('}');
			return value;

		case AST_FUNCTION_POWER:
			if (astnode.getLeftChild().getNumChildren() > 0)
				value = brackets(toLaTeX(model, astnode.getLeftChild()));
			else
				value = new StringBuffer(toLaTeX(model, astnode.getLeftChild()));
			value.append("^{");
			value.append(toLaTeX(model, astnode.getChild(astnode
					.getNumChildren() - 1)));
			value.append('}');
			return value;

		case AST_FUNCTION_ROOT:
			ASTNode left = astnode.getLeftChild();
			if ((astnode.getNumChildren() > 1)
					&& ((left.isInteger() && (left.getInteger() != 2)) || (left
							.isReal() && (left.getReal() != 2d))))
				value = root(toLaTeX(model, astnode.getLeftChild()), toLaTeX(
						model, astnode.getRightChild()));
			value = sqrt(toLaTeX(model, astnode.getChild(astnode
					.getNumChildren() - 1)));
			return value;

		case AST_FUNCTION_SEC:
			value = new StringBuffer("\\sec{");
			if (astnode.getLeftChild().getNumChildren() > 0)
				value.append(brackets(toLaTeX(model, astnode.getLeftChild())));
			else
				value.append(toLaTeX(model, astnode.getLeftChild()));
			value.append('}');
			return value;

		case AST_FUNCTION_SECH:
			value = mathrm("sech");
			if (astnode.getLeftChild().getNumChildren() > 0)
				value.append(brackets(toLaTeX(model, astnode.getLeftChild())));
			else
				value.append(toLaTeX(model, astnode.getLeftChild()));
			value.append('}');
			return value;

		case AST_FUNCTION_SIN:
			value = new StringBuffer("\\sin{");
			if (astnode.getLeftChild().getNumChildren() > 0)
				value.append(brackets(toLaTeX(model, astnode.getLeftChild())));
			else
				value.append(toLaTeX(model, astnode.getLeftChild()));
			value.append('}');
			return value;

		case AST_FUNCTION_SINH:
			value = new StringBuffer("\\sinh{");
			if (astnode.getLeftChild().getNumChildren() > 0)
				value.append(brackets(toLaTeX(model, astnode.getLeftChild())));
			else
				value.append(toLaTeX(model, astnode.getLeftChild()));
			value.append('}');
			return value;

		case AST_FUNCTION_TAN:
			value = new StringBuffer("\\tan{");
			if (astnode.getLeftChild().getNumChildren() > 0)
				value.append(brackets(toLaTeX(model, astnode.getLeftChild())));
			else
				value.append(toLaTeX(model, astnode.getLeftChild()));
			value.append('}');
			return value;

		case AST_FUNCTION_TANH:
			value = new StringBuffer("\\tanh{");
			if (astnode.getLeftChild().getNumChildren() > 0)
				value.append(brackets(toLaTeX(model, astnode.getLeftChild())));
			else
				value.append(toLaTeX(model, astnode.getLeftChild()));
			value.append('}');
			return value;

		case AST_FUNCTION:
			value = new StringBuffer(
					mathtt(maskSpecialChars(astnode.getName())));
			StringBuffer args = new StringBuffer(toLaTeX(model, astnode
					.getLeftChild()));
			for (int i = 1; i < astnode.getNumChildren(); i++) {
				args.append(", ");
				args.append(toLaTeX(model, astnode.getChild(i)));
			}
			args = brackets(args);
			value.append(args);
			return value;

		case AST_LAMBDA:
			value = new StringBuffer(toLaTeX(model, astnode.getLeftChild()));
			// mathtt(maskLaTeXspecialSymbols(astnode.getName())) == LAMBDA!!!
			// value.append('(');
			for (int i = 1; i < astnode.getNumChildren() - 1; i++) {
				value.append(", ");
				value.append(toLaTeX(model, astnode.getChild(i)));
			}
			value = brackets(value);
			value.append(" = ");
			value.append(toLaTeX(model, astnode.getRightChild()));
			return value;

		case AST_LOGICAL_AND:
			return mathematicalOperation(astnode, model, "\\wedge ");
		case AST_LOGICAL_XOR:
			return mathematicalOperation(astnode, model, "\\oplus ");
		case AST_LOGICAL_OR:
			return mathematicalOperation(astnode, model, "\\lor ");
		case AST_LOGICAL_NOT:
			value = new StringBuffer("\\neg ");
			if (0 < astnode.getLeftChild().getNumChildren())
				value.append(brackets(toLaTeX(model, astnode.getLeftChild())));
			else
				value.append(toLaTeX(model, astnode.getLeftChild()));
			return value;

		case AST_FUNCTION_PIECEWISE:
			value = new StringBuffer("\\begin{dcases}");
			value.append(newLine);
			for (int i = 0; i < astnode.getNumChildren() - 1; i++) {
				value.append(toLaTeX(model, astnode.getChild(i)));
				value.append(((i % 2) == 0) ? " & \\text{if\\ } " : lineBreak);
			}
			value.append(toLaTeX(model, astnode.getChild(astnode
					.getNumChildren() - 1)));
			if ((astnode.getNumChildren() % 2) == 1) {
				value.append(" & \\text{otherwise}");
				value.append(newLine);
			}
			value.append("\\end{dcases}");
			return value;

		case AST_RELATIONAL_EQ:
			value = new StringBuffer(toLaTeX(model, astnode.getLeftChild()));
			value.append(" = ");
			value.append(toLaTeX(model, astnode.getRightChild()));
			return value;

		case AST_RELATIONAL_GEQ:
			value = new StringBuffer(toLaTeX(model, astnode.getLeftChild()));
			value.append(" \\geq ");
			value.append(toLaTeX(model, astnode.getRightChild()));
			return value;

		case AST_RELATIONAL_GT:
			value = new StringBuffer(toLaTeX(model, astnode.getLeftChild()));
			value.append(" > ");
			value.append(toLaTeX(model, astnode.getRightChild()));
			return value;

		case AST_RELATIONAL_NEQ:
			value = new StringBuffer(toLaTeX(model, astnode.getLeftChild()));
			value.append(" \\neq ");
			value.append(toLaTeX(model, astnode.getRightChild()));
			return value;

		case AST_RELATIONAL_LEQ:
			value = new StringBuffer(toLaTeX(model, astnode.getLeftChild()));
			value.append(" \\leq ");
			value.append(toLaTeX(model, astnode.getRightChild()));
			return value;

		case AST_RELATIONAL_LT:
			value = new StringBuffer(toLaTeX(model, astnode.getLeftChild()));
			value.append(" < ");
			value.append(toLaTeX(model, astnode.getRightChild()));
			return value;

		case AST_UNKNOWN:
			return mathtext(" unknown ");

		default:
			return value = new StringBuffer();
		}
	}

	/**
	 * Writing a laTeX file
	 * 
	 * @param model
	 * @param file
	 * @throws IOException
	 */
	public void toLaTeX(PluginModel model, File file) throws IOException {
		BufferedWriter bw = new BufferedWriter(new FileWriter(file));
		bw.write(toLaTeX(model).toString());
		bw.close();
	}

	public StringBuffer toLaTeX(PluginModel model, PluginReaction reaction)
			throws IOException {
		String title = model.getName().length() > 0 ? model.getName()
				.replaceAll("_", " ") : model.getId().replaceAll("_", " ");
		StringBuffer laTeX = getDocumentHead(title);
		laTeX.append((!reaction.getName().equals("") && !reaction.getName()
				.equals(reaction.getId())) ? "Reaction: \\texttt{"
				+ replaceAll("_", reaction.getId(), "\\_") + "}" + " ("
				+ replaceAll("_", reaction.getName(), "\\_") + ")" + newLine
				+ "\\begin{equation*}" + newLine + "v=" : "Reaction: \\texttt{"
				+ replaceAll("_", reaction.getId(), "\\_") + "}" + newLine
				+ "\\begin{equation*}" + newLine + "v=");
		if ((reaction.getKineticLaw() != null)
				&& (reaction.getKineticLaw().getMath() != null))
			laTeX.append(toLaTeX(model, reaction.getKineticLaw().getMath()));
		else
			laTeX.append(" \\mathrm{undefined} ");
		laTeX.append(newLine + "\\end{equation*}");
		laTeX
				.append(newLine
						+ "\\begin{center} For a more comprehensive \\LaTeX{} "
						+ "export, see \\url{http://www.ra.cs.uni-tuebingen.de/software/SBML2LaTeX}"
						+ "\\end{center}");
		laTeX.append(newLine + "\\end{document}");
		return laTeX;
	}

	/*
	 * public void setNumberEquations(boolean numberEquations) {
	 * this.numberEquations = numberEquations; }
	 */

	private StringBuffer getDocumentHead(String title) {
		StringBuffer head = new StringBuffer("\\documentclass[" + fontSize + "pt");
		if (titlepage) {
			head.append(",titlepage");
		}
		if (landscape) {
			head.append(",landscape");
		}
		head.append("," + paperSize + "paper]{scrartcl}");
		head.append(newLine + "\\usepackage[scaled=.9]{helvet}" + newLine
				+ "\\usepackage{amsmath}" + newLine + "\\usepackage{courier}"
				+ newLine + "\\usepackage{times}" + newLine
				+ "\\usepackage[english]{babel}" + newLine
				+ "\\usepackage{a4wide}" + newLine + "\\usepackage{longtable}"
				+ newLine + "\\usepackage{booktabs}" + newLine);
		head.append("\\usepackage{url}" + newLine);
		if (landscape)
			head.append("\\usepackage[landscape]{geometry}" + newLine);
		head.append("\\title{\\textsc{SBMLsqueezer}: Differential Equation System ``"
				+ title
				+ "\"}"
				+ newLine
				+ "\\date{\\today}"
				+ newLine
				+ "\\begin{document}"
				+ newLine
				+ "\\author{}"
				+ newLine
				+ "\\maketitle" + newLine);
		return head;
	}

	/**
	 * If the field printNameIfAvailable is false this method returns a the id
	 * of the given SBase. If printNameIfAvailable is true this method looks for
	 * the name of the given SBase and will return it.
	 * 
	 * @param sbase
	 *            the SBase, whose name or id is to be returned.
	 * @param mathMode
	 *            if true this method returns the name typesetted in mathmode,
	 *            i.e., mathrm for names and mathtt for ids, otherwise texttt
	 *            will be used for ids and normalfont (nothing) will be used for
	 *            names.
	 * @return The name or the ID of the SBase (according to the field
	 *         printNameIfAvailable), whose LaTeX special symbols are masked and
	 *         which is type set in typewriter font if it is an id. The mathmode
	 *         argument decides if mathtt or mathrm has to be used.
	 */
	private StringBuffer getNameOrID(PluginSBase sbase) {
		String name = "";
		if (sbase instanceof PluginCompartment) {
			name = (printNameIfAvailable) ? ((PluginCompartment) sbase)
					.getName() : ((PluginCompartment) sbase).getId();
		} else if (sbase instanceof PluginSpecies) {
			name = (printNameIfAvailable) ? ((PluginSpecies) sbase).getName()
					: ((PluginSpecies) sbase).getId();
		} else {
			name = "Undefinded";
		}
		name = maskSpecialChars(name);
		if (printNameIfAvailable) {
			return new StringBuffer("\\text{" + name + "}");
		} else {
			return mathtt(name);
		}
	}

	/**
	 * This method returns the correct LaTeX expression for a function which
	 * returns the size of a compartment. This can be a volume, an area, a
	 * length or a point.
	 */
	private StringBuffer getSize(PluginCompartment c) {
		StringBuffer value = new StringBuffer("\\mathrm{");
		switch ((int) c.getSpatialDimensions()) {
		case 3:
			value.append("vol");
			break;
		case 2:
			value.append("area");
			break;
		case 1:
			value.append("length");
			break;
		default:
			value.append("point");
			break;
		}
		value.append("}(");
		value.append(getNameOrID(c));
		value.append(')');
		return value;
	}

	/**
	 * This method decides if brakets are to be set. The symbol is a
	 * mathematical operator, e.g., plus, minus, multiplication etc. in LaTeX
	 * syntax (for instance
	 * 
	 * <pre>
	 * \cdot
	 * </pre>
	 * 
	 * ). It simply counts the number of descendants on the left and the right
	 * hand side of the symbol.
	 * 
	 * @param astnode
	 * @param model
	 * @param symbol
	 * @return
	 * @throws IOException
	 */
	private StringBuffer mathematicalOperation(ASTNode astnode,
			PluginModel model, String symbol) throws IOException {
		StringBuffer value = new StringBuffer();
		if (1 < astnode.getLeftChild().getNumChildren())
			value.append("\\left(");
		value.append(toLaTeX(model, astnode.getLeftChild()));
		if (1 < astnode.getLeftChild().getNumChildren())
			value.append("\\right)");
		value.append(symbol);
		if (1 < astnode.getRightChild().getNumChildren())
			value.append("\\left(");
		value.append(toLaTeX(model, astnode.getRightChild()));
		if (1 < astnode.getRightChild().getNumChildren())
			value.append("\\right)");
		return value;
	}

	private String name_idToLaTex(String s) {
		return "$" + toTeX(s) + "$";
	}

	/**
	 * Writing laTeX code of a string name
	 * 
	 * @param name
	 * @return String
	 */
	private String toTeX(String name) {
		String tex = "";
		String help = "";
		String sign = "";
		if (name.toLowerCase().startsWith("kass")) {
			tex += "k^\\mathrm{ass}";
			name = name.substring(4, name.length());
		} else if (name.toLowerCase().startsWith("kcatp")) {
			tex += "k^\\mathrm{cat}";
			name = name.substring(5, name.length());
			sign = "+";
		} else if (name.toLowerCase().startsWith("kcatn")) {
			tex += "k^\\mathrm{cat}";
			name = name.substring(5, name.length());
			sign = "-";
		} else if (name.toLowerCase().startsWith("kdiss")) {
			tex += "k^\\mathrm{diss}";
			name = name.substring(5, name.length());
		} else if (name.toLowerCase().startsWith("km")) {
			tex += "k^\\mathrm{m}";
			name = name.substring(2, name.length());
		} else if (name.toLowerCase().startsWith("ki")) {
			tex += "k^\\mathrm{i}";
			name = name.substring(2, name.length());
		} else {
			int j = 0;
			while (j < name.length() && !(name.substring(j, j + 1).equals("_"))
					&& !(Character.isDigit(name.charAt(j)))) {
				tex += name.substring(j, j + 1);
				j++;
			}
			name = name.substring(j - 1, name.length());
		}
		String s = "_{" + sign;
		String nameIndex = "";
		for (int i = 0; i < name.length(); i++) {
			if (i > 0) {
				nameIndex = name.substring(i, i + 1);
				if (Character.isDigit(name.charAt(i))) {
					int k = i;
					while (i < name.length()) {
						if (Character.isDigit(name.charAt(i)))
							i++;
						else
							break;
					}
					nameIndex = name.substring(k, i);
					if (name.substring(k - 1, k).equals("_")) {
						if (s.endsWith("{") || s.endsWith("+")
								|| s.endsWith("-"))
							s += nameIndex;
						else if (!s.endsWith(","))
							s += ", " + nameIndex;
					} else {
						if (s.endsWith("{")) {
							s += help + "_{" + nameIndex + "}";
							help = "";
						} else {
							s += ", " + help + "_{" + nameIndex + "}";
							help = "";
						}
					}
				} else if (!nameIndex.equals("_"))
					help += nameIndex;
			}
		}
		s += "}";
		return tex + s;
	}
}
