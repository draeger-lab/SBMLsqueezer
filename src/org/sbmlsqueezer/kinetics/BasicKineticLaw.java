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
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.sbmlsqueezer.kinetics;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

import jp.sbi.celldesigner.plugin.PluginKineticLaw;
import jp.sbi.celldesigner.plugin.PluginModel;
import jp.sbi.celldesigner.plugin.PluginReaction;
import jp.sbi.celldesigner.plugin.PluginSpecies;
import jp.sbi.celldesigner.plugin.PluginSpeciesReference;

import org.sbml.libsbml.ASTNode;
import org.sbml.libsbml.libsbml;
import org.sbml.libsbml.libsbmlConstants;
import org.sbmlsqueezer.io.LaTeXExport;

/**
 * An abstract super class of specialized kinetic laws.
 * 
 * @since 1.0
 * @version
 * @author <a href="mailto:andreas.draeger@uni-tuebingen.de">Andreas
 *         Dr&auml;ger</a>
 * @author <a href="mailto:Nadine.hassis@gmail.com">Nadine Hassis</a>
 * @author <a href="mailto:hannes.borch@googlemail.com">Hannes Borch</a>
 * @date Aug 1, 2007
 */
public abstract class BasicKineticLaw extends PluginKineticLaw implements
		libsbmlConstants {

	/**
	 * 
	 */
	protected static final Character underscore = Character.valueOf('_');

	/**
	 * Takes the given StringBuffer as input and appends every further Object to
	 * it.
	 * 
	 * @param k
	 * @param things
	 * @return
	 */
	public static final StringBuffer append(StringBuffer k, Object... things) {
		for (Object t : things)
			k.append(t);
		return k;
	}

	/**
	 * Basic method which links several elements with a mathematical operator.
	 * All empty StringBuffer object are excluded.
	 * 
	 * @param operator
	 * @param elements
	 * @return
	 */
	private static final StringBuffer arith(char operator, Object... elements) {
		List<Object> vsb = new Vector<Object>();
		for (Object sb : elements)
			if (sb.toString().length() > 0)
				vsb.add(sb);
		StringBuffer equation = new StringBuffer();
		if (vsb.size() > 0)
			equation.append(vsb.get(0));
		Character op = Character.valueOf(operator);
		for (int count = 1; count < vsb.size(); count++)
			append(equation, op, vsb.get(count));
		return equation;
	}

	/**
	 * 
	 * @param sb
	 * @return
	 */
	public static final StringBuffer brackets(Object sb) {
		return concat(Character.valueOf('('), sb, Character.valueOf(')'));
	}

	/**
	 * Clones an abstract syntax tree.
	 * 
	 * @param ast
	 * @return
	 */
	public static final ASTNode clone(ASTNode ast) {
		ASTNode copy = new ASTNode();
		copy.setType(ast.getType());
		if (ast.isConstant() || ast.isInteger() || ast.isNumber()
				|| ast.isReal())
			copy.setValue(ast.getReal());
		else if (ast.isName())
			copy.setName(new String(ast.getName()));
		for (long i = 0; i < ast.getNumChildren(); i++)
			copy.addChild(clone(ast.getChild(i)));
		return copy;
	}

	/**
	 * This method concatenates two or more object strings into a new
	 * stringbuffer.
	 * 
	 * @param buffers
	 * @return
	 */
	public static final StringBuffer concat(Object... buffers) {
		StringBuffer res = new StringBuffer();
		for (Object buffer : buffers)
			res.append(buffer.toString());
		return res;
	}

	/**
	 * Tests whether the String representation of the given object contains any
	 * arithmetic symbols and if the given object is already sorrounded by
	 * brackets.
	 * 
	 * @param something
	 * @return True if either brackets are set around the given object or the
	 *         object does not contain any symbols such as +, -, *, /.
	 */
	private static boolean containsArith(Object something) {
		boolean arith = false;
		String d = something.toString();
		if (d.length() > 0) {
			char c;
			for (int i = 0; (i < d.length()) && !arith; i++) {
				c = d.charAt(i);
				arith = ((c == '+') || (c == '-') || (c == '*') || (c == '/'));
			}
		}
		return arith;
	}

	/**
	 * Returns the difference of the given elements as StringBuffer.
	 * 
	 * @param subtrahents
	 * @return
	 */
	public static final StringBuffer diff(Object... subtrahents) {
		if (subtrahents.length == 1)
			return brackets(concat(Character.valueOf('-'), subtrahents));
		return brackets(arith('-', subtrahents));
	}

	public static final StringBuffer diff(StringBuffer... subtrahents) {
		if (subtrahents.length == 1)
			return brackets(concat(Character.valueOf('-'), subtrahents));
		return brackets(arith('-', subtrahents));
	}

	private static StringBuffer floor(StringBuffer text) {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * Returns a fraction with the given elements as numerator and denominator.
	 * 
	 * @param numerator
	 * @param denominator
	 * @return
	 */
	public static final StringBuffer frac(Object numerator, Object denominator) {
		return brackets(arith('/',
				(containsArith(numerator) ? brackets(numerator) : numerator),
				containsArith(denominator) ? brackets(denominator)
						: denominator));
	}

	/**
	 * Creates and returns a list of molecule types accepted as an enzyme by
	 * default. These are: <ul type="disk"> <li>ANTISENSE_RNA</li> <li>
	 * SIMPLE_MOLECULE</li> <li>UNKNOWN</li> <li>COMPLEX</li> <li>TRUNCATED</li>
	 * <li>GENERIC</li> <li>RNA</li> <li>RECEPTOR</li> </ul>
	 * 
	 * @return
	 */
	public static final List<String> getDefaultListOfPossibleEnzymes() {
		List<String> listOfPossibleEnzymes = new Vector<String>();
		listOfPossibleEnzymes.add("ANTISENSE_RNA");
		listOfPossibleEnzymes.add("SIMPLE_MOLECULE");
		listOfPossibleEnzymes.add("UNKNOWN");
		listOfPossibleEnzymes.add("COMPLEX");
		listOfPossibleEnzymes.add("TRUNCATED");
		listOfPossibleEnzymes.add("GENERIC");
		listOfPossibleEnzymes.add("RNA");
		listOfPossibleEnzymes.add("RECEPTOR");
		return listOfPossibleEnzymes;
	}

	/**
	 * This method constructs a full length SBO number from a given SBO id.
	 * Whenever a SBO number is used in the model please don't forget to add
	 * this identifier to the Set of SBO numbers (only those numbers in this set
	 * will be displayed in the glossary).
	 * 
	 * @param sbo
	 * @return
	 */
	protected static String getSBOnumber(int sbo) {
		if (sbo < 0)
			return "none";
		String sboString = Integer.toString(sbo);
		while (sboString.length() < 7)
			sboString = '0' + sboString;
		return sboString;
	}

	/**
	 * Returns the id of a PluginSpeciesReference object's belonging species as
	 * an object of type StringBuffer.
	 * 
	 * @param ref
	 * @return
	 */
	protected static final StringBuffer getSpecies(PluginSpeciesReference ref) {
		return new StringBuffer(ref.getSpecies());
	}

	/**
	 * identify which Modifer is used
	 * 
	 * @param reactionNum
	 */
	public static final void identifyModifers(PluginReaction reaction,
			List<String> listOfPossibleEnzymes, List<String> inhibitors,
			List<String> transActivators, List<String> transInhibitors,
			List<String> activators, List<String> enzymes,
			List<String> nonEnzymeCatalyzers) {
		inhibitors.clear();
		transActivators.clear();
		transInhibitors.clear();
		activators.clear();
		enzymes.clear();
		nonEnzymeCatalyzers.clear();
		String type;
		for (int modifierNum = 0; modifierNum < reaction.getNumModifiers(); modifierNum++) {
			type = reaction.getModifier(modifierNum).getModificationType()
					.toUpperCase();
			if (type.equals("MODULATION")) {
				inhibitors.add(reaction.getModifier(modifierNum).getSpecies());
				activators.add(reaction.getModifier(modifierNum).getSpecies());
			} else if (type.equals("INHIBITION"))
				inhibitors.add(reaction.getModifier(modifierNum).getSpecies());
			else if (type.equals("TRANSCRIPTIONAL_ACTIVATION")
					|| type.equals("TRANSLATIONAL_ACTIVATION"))
				transActivators.add(reaction.getModifier(modifierNum)
						.getSpecies());
			else if (type.equals("TRANSCRIPTIONAL_INHIBITION")
					|| type.equals("TRANSLATIONAL_INHIBITION"))
				transInhibitors.add(reaction.getModifier(modifierNum)
						.getSpecies());
			else if (type.equals("UNKNOWN_CATALYSIS") || type.equals("TRIGGER")
					|| type.equals("PHYSICAL_STIMULATION"))
				activators.add(reaction.getModifier(modifierNum).getSpecies());
			else if (type.equals("CATALYSIS")) {
				PluginSpecies species = reaction.getModifier(modifierNum)
						.getSpeciesInstance();
				String speciesAliasType = species.getSpeciesAlias(0).getType()
						.equals("PROTEIN") ? species.getSpeciesAlias(0)
						.getProtein().getType() : species.getSpeciesAlias(0)
						.getType();
				if (listOfPossibleEnzymes.contains(speciesAliasType))
					enzymes.add(reaction.getModifier(modifierNum).getSpecies());
				else
					nonEnzymeCatalyzers.add(reaction.getModifier(modifierNum)
							.getSpecies());
			}
		}
	}

	/**
	 * This method returns true if and only if the kinetic law can be assigned
	 * to the given reaction. If the structure of the reaction, i.e. the number
	 * of reactants or products, the number and type of modifiers does not allow
	 * to assign this type of kinetic law to this reaction, false will be
	 * returned. This method must be implemented by more specialized instances
	 * of this class.
	 * 
	 * @param reaction
	 * @return
	 */
	public static boolean isApplicable(PluginReaction reaction) {
		return false;
	}

	/**
	 * Returns the basis to the power of the exponent as StringBuffer. Several
	 * special cases are treated.
	 * 
	 * @param basis
	 * @param exponent
	 * @return
	 */
	public static final StringBuffer pow(Object basis, Object exponent) {
		try {
			if (Double.parseDouble(exponent.toString()) == 0f)
				return new StringBuffer('1');
			if (Double.parseDouble(exponent.toString()) == 1f)
				return basis instanceof StringBuffer ? (StringBuffer) basis
						: new StringBuffer(basis.toString());
		} catch (NumberFormatException exc) {
			//System.out.println("pow(" + basis + ", " + exponent + ")");
		}
		return arith('^', basis, exponent);
	}

	/**
	 * Returns the exponent-th root of the basis as StringBuffer.
	 * 
	 * @param exponent
	 * @param basis
	 * @return
	 * @throws IllegalFormatException
	 *             If the given exponent represents a zero.
	 */
	public static final StringBuffer root(Object exponent, Object basis)
			throws IllegalFormatException {
		if (Double.parseDouble(exponent.toString()) == 0f)
			throw new IllegalFormatException(
					"Cannot extract a zeroth root of anything");
		if (Double.parseDouble(exponent.toString()) == 1f)
			return new StringBuffer(basis.toString());
		return concat("root(", exponent, Character.valueOf(','), basis,
				Character.valueOf(')'));
	}

	public static final StringBuffer sqrt(Object basis) {
		try {
			return root(Integer.valueOf(2), basis);
		} catch (IllegalFormatException e) {
			return pow(basis, frac(Integer.valueOf(1), Integer.valueOf(2)));
		}
	}

	/**
	 * Returns the sum of the given elements as StringBuffer.
	 * 
	 * @param summands
	 * @return
	 */
	public static final StringBuffer sum(Object... summands) {
		return brackets(arith('+', summands));
	}

	/**
	 * 
	 * @param summands
	 * @return
	 */
	public static final StringBuffer sum(StringBuffer... summands) {
		return brackets(arith('+', summands));
	}

	/**
	 * Returns the product of the given elements as StringBuffer.
	 * 
	 * @param factors
	 * @return
	 */
	public static final StringBuffer times(Object... factors) {
		return arith('*', factors);
	}

	/**
	 * Returns the product of the given elements as StringBuffer.
	 * 
	 * @param factors
	 * @return
	 */
	public static final StringBuffer times(StringBuffer... factors) {
		return arith('*', factors);
	}

	public static final StringBuffer toText(ASTNode astnode) {
		return new StringBuffer(libsbml.formulaToString(astnode));
	}

	protected HashMap<String, String> idAndName;

	protected StringBuffer formelTeX;

	protected List<StringBuffer> listOfLocalParameters;

	private List<StringBuffer> listOfGlobalParameters;

	protected String sboTerm;

	protected PluginModel model;

	/**
	 * 
	 * @param parentReaction
	 * @param model
	 * @throws RateLawNotApplicableException
	 * @throws IOException
	 * @throws IllegalFormatException
	 */
	public BasicKineticLaw(PluginReaction parentReaction, PluginModel model)
			throws RateLawNotApplicableException, IOException,
			IllegalFormatException {
		this(parentReaction, model, getDefaultListOfPossibleEnzymes());
	}

	/**
	 * 
	 * @param parentReaction
	 * @param model
	 * @param listOfPossibleEnzymes
	 * @param type
	 * @throws RateLawNotApplicableException
	 * @throws IOException
	 * @throws IllegalFormatException
	 */
	public BasicKineticLaw(PluginReaction parentReaction, PluginModel model,
			List<String> listOfPossibleEnzymes)
			throws RateLawNotApplicableException, IOException,
			IllegalFormatException {
		super(parentReaction);
		this.model = model;
		sboTerm = null;
		idAndName = new HashMap<String, String>();
		listOfLocalParameters = new ArrayList<StringBuffer>();
		listOfGlobalParameters = new ArrayList<StringBuffer>();
		List<String> modActi = new ArrayList<String>();
		List<String> modCat = new ArrayList<String>();
		List<String> modInhib = new ArrayList<String>();
		List<String> modE = new ArrayList<String>();
		List<String> modTActi = new ArrayList<String>();
		List<String> modTInhib = new ArrayList<String>();
		identifyModifers(parentReaction, listOfPossibleEnzymes, modInhib,
				modTActi, modTInhib, modActi, modE, modCat);
		setMath(libsbml.parseFormula(createKineticEquation(model, modE,
				modActi, modTActi, modInhib, modTInhib, modCat).toString()));
		formelTeX = (new LaTeXExport().toLaTeX(model, getMath()));
	}

	/**
	 * Adds the given parameter only to the list of global parameters if this
	 * list does not yet contain this parameter.
	 * 
	 * @param parameter
	 */
	protected void addGlobalParameter(StringBuffer parameter) {
		if (!listOfGlobalParameters.contains(parameter))
			listOfGlobalParameters.add(parameter);
	}

	/**
	 * Adds the given parameter only to the list of local parameters if this
	 * list does not yet contain this parameter.
	 * 
	 * @param parameter
	 */
	protected void addLocalParameter(StringBuffer parameter) {
		if (!listOfLocalParameters.contains(parameter))
			listOfLocalParameters.add(parameter);
	}

	/**
	 * 
	 * @param model
	 * @param modE
	 * @param modActi
	 * @param modTActi
	 * @param modInhib
	 * @param modTInhib
	 * @param modCat
	 * @return
	 * @throws RateLawNotApplicableException
	 * @throws IllegalFormatException
	 */
	protected abstract StringBuffer createKineticEquation(PluginModel model,
			List<String> modE, List<String> modActi, List<String> modTActi,
			List<String> modInhib, List<String> modTInhib, List<String> modCat)
			throws RateLawNotApplicableException, IllegalFormatException;

	/**
	 * Returns a list of names of all parameters, which are only allowed to be
	 * stored globally.
	 * 
	 * @return
	 */
	public List<StringBuffer> getGlobalParameters() {
		return listOfGlobalParameters;
	}

	/**
	 * Recursively removes waste brackets from a formula.
	 * 
	 * TODO Refinement
	 * 
	 * @param sb
	 * @return
	 */
	/*
	 * protected StringBuffer removeBrackets(StringBuffer sb) { if (sb.length()
	 * > 0) { if ((sb.charAt(0) == '(') && (sb.charAt(sb.length() - 1) == ')'))
	 * { sb.deleteCharAt(sb.length() - 1); sb.deleteCharAt(0); sb =
	 * removeBrackets(sb); } } return sb; }
	 */

	/**
	 * Returns the LaTeX expression of the generated formual of this kinetic
	 * law.
	 * 
	 * @return
	 */
	public String getKineticTeX() {
		return formelTeX.toString();
	}

	/**
	 * Returns a list of the names of all parameters used by this law. This list
	 * contains parameters, which can be either stored locally, i.e., assigned
	 * to the specific kinetic law or globally for the whole model.
	 * 
	 * @return
	 */
	public List<StringBuffer> getLocalParameters() {
		return listOfLocalParameters;
	}

	/**
	 * Returns the name of the kinetic formula of this object.
	 * 
	 * @return
	 */
	public abstract String getName();

	/**
	 * Returns the SBO identifier of the respective kinetic law if there is one
	 * or an empty String otherwise.
	 * 
	 * @return
	 */
	public abstract String getSBO();

	/**
	 * Returns the value of a PluginSpeciesReference object's stoichiometry
	 * either as a double or, if the stoichiometry has an integer value, as an
	 * int object.
	 * 
	 * @param ref
	 * @return
	 */
	protected static final StringBuffer getStoichiometry(
			PluginSpeciesReference ref) {
		if (ref.getStoichiometryMath() == null) {
			double stoich = ref.getStoichiometry();
			if ((int) stoich - stoich == 0)
				return new StringBuffer(Integer.toString((int) stoich));
			else
				return new StringBuffer(Double.toString(stoich));
		}
		return toText(ref.getStoichiometryMath().getMath());
	}

	// @Override
	public String toString() {
		if (sboTerm == null)
			sboTerm = getName();
		return sboTerm;
	}

}
