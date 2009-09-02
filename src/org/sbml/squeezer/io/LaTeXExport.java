/**
 * @date Nov, 2007
 * @since 2.0
 * @version
 * @author Andreas Dr&auml;ger (draeger) <andreas.draeger@uni-tuebingen.de>
 * @author Dieudonne Motsou Wouamba <dwouamba@yahoo.fr> Copyright (c) ZBiT,
 *         University of T&uuml;bingen, Germany Compiler: JDK 1.6.0
 */
package org.sbml.squeezer.io;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.StringTokenizer;
import java.util.Vector;

import org.sbml.ASTNode;
import org.sbml.Compartment;
import org.sbml.Constants;
import org.sbml.Event;
import org.sbml.Model;
import org.sbml.Reaction;
import org.sbml.SBase;
import org.sbml.SIUnit;
import org.sbml.Species;
import org.sbml.SpeciesReference;
import org.sbml.StoichiometryMath;
import org.sbml.Unit;
import org.sbml.UnitDefinition;

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
public class LaTeXExport extends LaTeX {

	/**
	 * Masks all special characters used by LaTeX with a backslash including
	 * hyphen symbols.
	 * 
	 * @param string
	 * @return
	 */
	public static String maskSpecialChars(String string) {
		return maskSpecialChars(string, true);
	}

	/**
	 * 
	 * @param string
	 * @param hyphen
	 *            if true a hyphen symbol is introduced at each position where a
	 *            special character has to be masked anyway.
	 * @return
	 */
	public static String maskSpecialChars(String string, boolean hyphen) {
		StringBuffer masked = new StringBuffer();
		for (int i = 0; i < string.length(); i++) {
			char atI = string.charAt(i);
			if (atI == '<')
				masked.append("$<$");
			else if (atI == '>')
				masked.append("$>$");
			else {
				if ((atI == '_') || (atI == '\\') || (atI == '$')
						|| (atI == '&') || (atI == '#') || (atI == '{')
						|| (atI == '}') || (atI == '~') || (atI == '%')
						|| (atI == '^')) {
					if ((i == 0) || (!hyphen))
						masked.append('\\');
					else if (hyphen && (string.charAt(i - 1) != '\\'))
						masked.append("\\-\\"); // masked.append('\\');
					// } else if ((atI == '[') || (atI == ']')) {
				}
				masked.append(atI);
			}
		}
		return masked.toString().trim();
	}
	
	/**
	 * Returns a properly readable unit definition.
	 * 
	 * @param def
	 * @return
	 */
	public StringBuffer format(UnitDefinition def) {
		StringBuffer buffer = new StringBuffer();
		for (int j = 0; j < def.getNumUnits(); j++) {
			buffer.append(format((Unit) def.getListOfUnits().get(j)));
			if (j < def.getListOfUnits().size() - 1)
				buffer.append("\\cdot ");
		}
		return buffer;
	}
	
	/**
	 * Returns a unit.
	 * 
	 * @param u
	 * @return
	 */
	public StringBuffer format(Unit u) {
		StringBuffer buffer = new StringBuffer();
		boolean standardScale = (u.getScale() == 18) || (u.getScale() == 12)
				|| (u.getScale() == 9) || (u.getScale() == 6)
				|| (u.getScale() == 3) || (u.getScale() == 2)
				|| (u.getScale() == 1) || (u.getScale() == 0)
				|| (u.getScale() == -1) || (u.getScale() == -2)
				|| (u.getScale() == -3) || (u.getScale() == -6)
				|| (u.getScale() == -9) || (u.getScale() == -12)
				|| (u.getScale() == -15) || (u.getScale() == -18);
		if (u.getOffset() != 0d) {
			buffer.append(format(u.getOffset()).toString()
					.replaceAll("\\$", ""));
			if ((u.getMultiplier() != 0) || (!standardScale))
				buffer.append('+');
		}
		if (u.getMultiplier() != 1d) {
			if (u.getMultiplier() == -1d)
				buffer.append('-');
			else {
				buffer.append(format(u.getMultiplier()).toString().replaceAll(
						"\\$", ""));
				buffer.append(!standardScale ? "\\cdot " : "\\;");
			}
		}
		if (u.isKilogram()) {
			u.setScale(u.getScale() + 3);
			u.setKind(SIUnit.UNIT_KIND_GRAM);
		}
		if (!u.isDimensionless()) {
			switch (u.getScale()) {
			case 18:
				buffer.append(mathrm('E'));
				break;
			case 15:
				buffer.append(mathrm('P'));
				break;
			case 12:
				buffer.append(mathrm('T'));
				break;
			case 9:
				buffer.append(mathrm('G'));
				break;
			case 6:
				buffer.append(mathrm('M'));
				break;
			case 3:
				buffer.append(mathrm('k'));
				break;
			case 2:
				buffer.append(mathrm('h'));
				break;
			case 1:
				buffer.append(mathrm("da"));
				break;
			case 0:
				break;
			case -1:
				buffer.append(mathrm('d'));
				break;
			case -2:
				buffer.append(mathrm('c'));
				break;
			case -3:
				buffer.append(mathrm('m'));
				break;
			case -6:
				buffer.append("\\upmu");
				break;
			case -9:
				buffer.append(mathrm('n'));
				break;
			case -12:
				buffer.append(mathrm('p'));
				break;
			case -15:
				buffer.append(mathrm('f'));
				break;
			case -18:
				buffer.append(mathrm('a'));
				break;
			default:
				buffer.append("10^{");
				buffer.append(Integer.toString(u.getScale()));
				buffer.append("}\\cdot ");
				break;
			}
			switch (u.getKind()) {
			case UNIT_KIND_AMPERE:
				buffer.append(mathrm('A'));
				break;
			case UNIT_KIND_BECQUEREL:
				buffer.append(mathrm("Bq"));
				break;
			case UNIT_KIND_CANDELA:
				buffer.append(mathrm("cd"));
				break;
			case UNIT_KIND_CELSIUS:
				buffer.append("\\text{\\textcelsius}");
				break;
			case UNIT_KIND_COULOMB:
				buffer.append(mathrm('C'));
				break;
			case UNIT_KIND_DIMENSIONLESS:
				break;
			case UNIT_KIND_FARAD:
				buffer.append(mathrm('F'));
				break;
			case UNIT_KIND_GRAM:
				buffer.append(mathrm('g'));
				break;
			case UNIT_KIND_GRAY:
				buffer.append(mathrm("Gy"));
				break;
			case UNIT_KIND_HENRY:
				buffer.append(mathrm('H'));
				break;
			case UNIT_KIND_HERTZ:
				buffer.append(mathrm("Hz"));
				break;
			case UNIT_KIND_INVALID:
				buffer.append(mathrm("invalid"));
				break;
			case UNIT_KIND_ITEM:
				buffer.append(mathrm("item"));
				break;
			case UNIT_KIND_JOULE:
				buffer.append(mathrm('J'));
				break;
			case UNIT_KIND_KATAL:
				buffer.append(mathrm("kat"));
				break;
			case UNIT_KIND_KELVIN:
				buffer.append(mathrm('K'));
				break;
			// case UNIT_KIND_KILOGRAM:
			// buffer.append("\\mathrm{kg}");
			// break;
			case UNIT_KIND_LITER:
				buffer.append(mathrm('l'));
				break;
			case UNIT_KIND_LITRE:
				buffer.append(mathrm('l'));
				break;
			case UNIT_KIND_LUMEN:
				buffer.append(mathrm("lm"));
				break;
			case UNIT_KIND_LUX:
				buffer.append(mathrm("lx"));
				break;
			case UNIT_KIND_METER:
				buffer.append(mathrm('m'));
				break;
			case UNIT_KIND_METRE:
				buffer.append(mathrm('m'));
				break;
			case UNIT_KIND_MOLE:
				buffer.append(mathrm("mol"));
				break;
			case UNIT_KIND_NEWTON:
				buffer.append(mathrm('N'));
				break;
			case UNIT_KIND_OHM:
				buffer.append("\\upOmega");
				break;
			case UNIT_KIND_PASCAL:
				buffer.append(mathrm("Pa"));
				break;
			case UNIT_KIND_RADIAN:
				buffer.append(mathrm("rad"));
				break;
			case UNIT_KIND_SECOND:
				buffer.append(mathrm('s'));
				break;
			case UNIT_KIND_SIEMENS:
				buffer.append(mathrm('S'));
				break;
			case UNIT_KIND_SIEVERT:
				buffer.append(mathrm("Sv"));
				break;
			case UNIT_KIND_STERADIAN:
				buffer.append(mathrm("sr"));
				break;
			case UNIT_KIND_TESLA:
				buffer.append(mathrm('T'));
				break;
			case UNIT_KIND_VOLT:
				buffer.append(mathrm('V'));
				break;
			case UNIT_KIND_WATT:
				buffer.append(mathrm('W'));
				break;
			case UNIT_KIND_WEBER:
				buffer.append(mathrm("Wb"));
				break;
			}
		} else {
			if (u.getScale() != 0) {
				buffer.append("10^{");
				buffer.append(Integer.toString(u.getScale()));
				buffer.append("}\\;");
			}
			buffer.append(mathrm("dimensionless"));
		}
		if (((u.getOffset() != 0d) || (u.getMultiplier() != 1d) || !standardScale)
				&& (u.getExponent() != 1d))
			buffer = brackets(buffer);
		if (u.getExponent() != 1) {
			buffer.append("^{");
			buffer.append(Integer.toString(u.getExponent()));
			buffer.append('}');
		}
		return buffer;
	}

	/**
	 * 
	 * @param reaction
	 * @return
	 */
	public static String reactionEquation(Reaction reaction) {
		StringBuffer reactionEqn = new StringBuffer();
		reactionEqn.append(LaTeX.eqBegin);
		int count = 0;
		for (SpeciesReference specRef : reaction.getListOfReactants()) {
			if (count > 0)
				reactionEqn.append(" + ");
			if (specRef.isSetStoichiometryMath())
				reactionEqn.append(specRef.getStoichiometryMath().getMath()
						.toLaTeX());
			else if (specRef.getStoichiometry() != 1d)
				reactionEqn.append(specRef.getStoichiometry());
			reactionEqn.append(' ');
			reactionEqn.append(LaTeX.mbox(LaTeX.maskSpecialChars(specRef
					.getSpecies())));
			count++;
		}
		if (reaction.getNumReactants() == 0)
			reactionEqn.append("\\emptyset");
		reactionEqn.append(reaction.getReversible() ? " \\leftrightarrow"
				: " \\rightarrow");
		// if (reaction.getNumModifiers() > 0) {
		// reactionEqn.append('{');
		// count = 0;
		// for (ModifierSpeciesReference modRef : reaction
		// .getListOfModifiers()) {
		// reactionEqn
		// .append(LaTeX.mbox(
		// LaTeX.maskSpecialChars(modRef.getSpecies()))
		// .toString());
		// if (count < reaction.getNumModifiers() - 1)
		// reactionEqn.append(", ");
		// count++;
		// }
		// reactionEqn.append('}');
		// }
		reactionEqn.append(' ');
		count = 0;
		for (SpeciesReference specRef : reaction.getListOfProducts()) {
			if (count > 0)
				reactionEqn.append(" + ");
			if (specRef.isSetStoichiometryMath())
				reactionEqn.append(specRef.getStoichiometryMath().getMath()
						.toLaTeX());
			else if (specRef.getStoichiometry() != 1d)
				reactionEqn.append(specRef.getStoichiometry());
			reactionEqn.append(' ');
			reactionEqn.append(LaTeX.mbox(LaTeX.maskSpecialChars(specRef
					.getSpecies())));
			count++;
		}
		if (reaction.getNumProducts() == 0)
			reactionEqn.append("\\emptyset");
		reactionEqn.append(LaTeX.eqEnd);
		return reactionEqn.toString();
	}

	/**
	 * New line separator of this operating system
	 */
	private final String newLine = System.getProperty("line.separator");

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

	// private boolean numberEquations = true;

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
	public String idToTeX(Species pluginSpecies) {
		return nameToTeX(pluginSpecies.getId());
	}

	public boolean isTypeWriter() {
		return typeWriter;
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

	public StringBuffer toLaTeX(Model model) throws IOException {
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
		Species species;
		SpeciesReference speciesRef;
		HashMap<String, Integer> speciesIDandIndex = new HashMap<String, Integer>();
		for (reactionIndex = 0; reactionIndex < model.getNumReactions(); reactionIndex++) {
			Reaction r = model.getReaction(reactionIndex);
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
					rateLaws[reactionIndex] += r.getKineticLaw().getMath()
							.toLaTeX();
				else
					rateLaws[reactionIndex] += "\\text{no mathematics specified}";
			} else
				rateLaws[reactionIndex] += "\\text{no kinetic law specified}";
			for (speciesIndex = 0; speciesIndex < model.getNumSpecies(); speciesIndex++) {
				speciesIDandIndex.put(model.getSpecies(speciesIndex).getId(),
						Integer.valueOf(speciesIndex));
			}
		}

		Vector<Species> reactants = new Vector<Species>();
		Vector<Species> products = new Vector<Species>();
		Vector<Integer> reactantsReaction = new Vector<Integer>();
		Vector<Integer> productsReaction = new Vector<Integer>();
		Vector<SpeciesReference> reactantsStochiometric = new Vector<SpeciesReference>();
		Vector<SpeciesReference> productsStochiometric = new Vector<SpeciesReference>();

		for (reactionIndex = 0; reactionIndex < model.getNumReactions(); reactionIndex++) {
			Reaction r = model.getReaction(reactionIndex);
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
			SpeciesReference ref;
			species = model.getSpecies(speciesIndex);
			for (int k = 0; k < reactants.size(); k++) {
				if (species.getId().equals(reactants.get(k).getId())) {
					ref = reactantsStochiometric.get(k);
					if (ref != null) {
						stochMath = ref.getStoichiometryMath();
						if (stochMath != null && stochMath.isSetMath()) {
							stoch = stochMath.getMath();
							sEquation += (stoch.getType() == Constants.AST_PLUS || stoch
									.getType() == Constants.AST_MINUS) ? sEquation += "-\\left("
									+ stoch.toLaTeX()
									+ "\\right)v_{"
									+ reactantsReaction.get(k) + "}"
									: "-" + stoch.toLaTeX() + "v_{"
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
									sEquation += (stoch.getType() == Constants.AST_PLUS || stoch
											.getType() == Constants.AST_MINUS) ? sEquation += "\\left("
											+ stoch.toLaTeX()
											+ "\\right)v_{"
											+ productsReaction.get(k) + "}"
											: stoch.toLaTeX() + "v_{"
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
									sEquation += (stoch.getType() == Constants.AST_PLUS || stoch
											.getType() == Constants.AST_MINUS) ? sEquation += "+\\left("
											+ stoch.toLaTeX()
											+ "\\right)v_{"
											+ productsReaction.get(k) + "}"
											: "+" + stoch.toLaTeX() + "v_{"
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
		LinkedList<?> events[] = new LinkedList[(int) model.getNumEvents()];
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
			Event ev;
			for (i = 0; i < model.getNumEvents(); i++) {
				ev = model.getEvent(i);
				LinkedList<StringBuffer> assignments = new LinkedList<StringBuffer>();
				assignments.add(ev.getTrigger().getMath().toLaTeX());
				for (int j = 0; j < ev.getNumEventAssignments(); j++)
					assignments.add(ev.getEventAssignment(j).getMath()
							.toLaTeX());
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
								+ ev.getDelay().getMath().toLaTeX());
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
								+ ev.getDelay().getMath().toLaTeX());
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
			laTeX.append("\\begin{longtable}{@{}llr@{}}" + newLine
					+ "\\toprule " + newLine + "Parameter & Value \\\\  "
					+ newLine + "\\midrule" + newLine);
			for (i = 0; i < model.getNumParameters(); i++) {
				laTeX.append(name_idToLaTex(model.getParameter(i).getId())
						+ "&" + model.getParameter(i).getValue() + "\\\\"
						+ newLine);
			}
			laTeX.append("\\bottomrule " + newLine + "\\end{longtable}");
		}
		// writing species list and compartment.
		if (model.getNumSpecies() > 0) {
			laTeX.append(newLine + "\\section{Species}" + newLine);
			laTeX.append("\\begin{longtable}{@{}llr@{}} " + newLine
					+ "\\toprule " + newLine
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
			laTeX.append("\\begin{longtable}{@{}llr@{}}" + newLine
					+ "\\toprule " + newLine + "Compartment & Volume \\\\  "
					+ newLine + "\\midrule" + newLine);
			for (i = 0; i < model.getNumCompartments(); i++) {
				laTeX.append(name_idToLaTex(model.getCompartment(i).getId())
						+ "&" + model.getCompartment(i).getVolume() + "\\\\"
						+ newLine);
			}
			laTeX.append("\\bottomrule " + newLine + "\\end{longtable}");
		}
		laTeX.append(newLine + tail);
		return laTeX;
	}

	/**
	 * Writing a laTeX file
	 * 
	 * @param model
	 * @param file
	 * @throws IOException
	 */
	public void toLaTeX(Model model, File file) throws IOException {
		BufferedWriter bw = new BufferedWriter(new FileWriter(file));
		bw.append(toLaTeX(model));
		bw.close();
	}

	public StringBuffer toLaTeX(Reaction reaction)
			throws IOException {
		Model model = reaction.getModel();
		String title = model.getName().length() > 0 ? model.getName()
				.replaceAll("_", " ") : model.getId().replaceAll("_", " ");
		StringBuffer laTeX = getDocumentHead(title);
		String name = maskSpecialChars(reaction.getId());
		laTeX.append("\\begin{equation*}");
		laTeX.append(newLine);
		laTeX.append("v_\\mathtt{");
		laTeX.append(name);
		laTeX.append("}= ");
		if ((reaction.getKineticLaw() != null)
				&& (reaction.getKineticLaw().getMath() != null))
			laTeX.append(reaction.getKineticLaw().getMath().toLaTeX());
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
		StringBuffer head = new StringBuffer("\\documentclass[" + fontSize
				+ "pt");
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
		head
				.append("\\title{\\textsc{SBMLsqueezer}: Differential Equation System ``"
						+ title
						+ "\"}"
						+ newLine
						+ "\\date{\\today}"
						+ newLine
						+ "\\begin{document}"
						+ newLine
						+ "\\author{}"
						+ newLine + "\\maketitle" + newLine);
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
	private StringBuffer getNameOrID(SBase sbase) {
		String name = "";
		if (sbase instanceof Compartment) {
			name = (printNameIfAvailable) ? ((Compartment) sbase).getName()
					: ((Compartment) sbase).getId();
		} else if (sbase instanceof Species) {
			name = (printNameIfAvailable) ? ((Species) sbase).getName()
					: ((Species) sbase).getId();
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
