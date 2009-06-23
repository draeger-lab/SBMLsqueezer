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
import org.sbml.libsbml.libsbmlConstants;
import org.sbmlsqueezer.io.LaTeXExport;
import org.sbmlsqueezer.io.TextExport;

/**
 * An abstract super class of specialized kinetic laws.
 * 
 * @since 1.0
 * @version
 * @author <a href="mailto:andreas.draeger@uni-tuebingen.de">Andreas Dr&auml;ger</a>
 * @author <a href="mailto:Nadine.hassis@gmail.com">Nadine Hassis</a>
 * @author <a href="mailto:hannes.borch@googlemail.com">Hannes Borch</a>
 * @date Aug 1, 2007
 */
public abstract class BasicKineticLaw extends PluginKineticLaw implements
		libsbmlConstants {


	protected static final Character underscore = Character.valueOf('_');

	/**
	 * 
	 */
	public static final boolean FORWARD = true;

	/**
	 * 
	 */
	public static final boolean REVERSE = !FORWARD;

	protected HashMap<String, String> idAndName;

	protected StringBuffer formelTeX;

	private List<StringBuffer> listOfLocalParameters, listOfGlobalParameters;

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
		StringBuffer formula = createKineticEquation(model, modE, modActi,
				modTActi, modInhib, modTInhib, modCat);
		if (getMath() == null) {
			setFormula(formula.toString());
			setMathFromFormula();
		}
		formelTeX = (new LaTeXExport().toLaTeX(model, getMath()));
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
	 * Returns a list of names of all parameters, which are only allowed to be
	 * stored globally.
	 * 
	 * @return
	 */
	public List<StringBuffer> getGlobalParameters() {
		return listOfGlobalParameters;
	}

	/**
	 * Returns the LaTeX expression of the generated formual of this kinetic
	 * law.
	 * 
	 * @return
	 */
	public StringBuffer getKineticTeX() {
		return formelTeX;
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

	// @Override
	public String toString() {
		if (sboTerm == null)
			sboTerm = getName();
		return sboTerm;
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
	 * Returns the sum of the given elements as StringBuffer.
	 * 
	 * @param summands
	 * @return
	 */
	protected StringBuffer sum(Object... summands) {
		return brackets(arith('+', summands));
	}

	/**
	 * Returns the difference of the given elements as StringBuffer.
	 * 
	 * @param subtrahents
	 * @return
	 */
	protected StringBuffer diff(Object... subtrahents) {
		if (subtrahents.length == 1)
			return brackets(concat(Character.valueOf('-'), subtrahents));
		return brackets(arith('-', subtrahents));
	}

	/**
	 * Returns the product of the given elements as StringBuffer.
	 * 
	 * @param factors
	 * @return
	 */
	protected StringBuffer times(Object... factors) {
		return arith('*', factors);
	}

	/**
	 * This method concatenates two or more object strings into a new
	 * stringbuffer.
	 * 
	 * @param buffers
	 * @return
	 */
	protected StringBuffer concat(Object... buffers) {
		StringBuffer res = new StringBuffer();
		for (Object buffer : buffers)
			res.append(buffer.toString());
		return res;
	}

	/**
	 * Returns a fraction with the given elements as numerator and denominator.
	 * 
	 * @param numerator
	 * @param denominator
	 * @return
	 */
	protected StringBuffer frac(Object numerator, Object denominator) {
		return brackets(arith('/', numerator, denominator));
	}

	/**
	 * Returns the basis to the power of the exponent as StringBuffer. Several
	 * special cases are treated.
	 * 
	 * @param basis
	 * @param exponent
	 * @return
	 */
	protected StringBuffer pow(Object basis, Object exponent) {
		if (Double.parseDouble(exponent.toString()) == 0.0)
			return new StringBuffer('1');
		else if (Double.parseDouble(exponent.toString()) == 1.0)
			return basis instanceof StringBuffer ? (StringBuffer) basis
					: new StringBuffer(basis.toString());
		else
			return arith('^', basis, exponent);
	}

	/**
	 * Returns the exponent-th root of the basis as StringBuffer.
	 * 
	 * @param exponent
	 * @param basis
	 * @return
	 * @throws IllegalFormatException
	 */
	protected StringBuffer root(Object exponent, Object basis)
			throws IllegalFormatException {
		if (Double.parseDouble(exponent.toString()) == 0f)
			throw new IllegalFormatException(
					"Cannot extract a zeroth root of anything");
		else if (Double.parseDouble(exponent.toString()) == 1f)
			return new StringBuffer(basis.toString());
		else {
			StringBuffer root = new StringBuffer("root(");
			root.append(exponent);
			root.append(',');
			root.append(basis);
			root.append(')');
			return root;
		}
	}

	/**
	 * Basic method which links several elements with a mathematical operator.
	 * All empty StringBuffer object are excluded.
	 * 
	 * @param operator
	 * @param elements
	 * @return
	 */
	protected StringBuffer arith(char operator, Object... elements) {
		Vector<Object> vsb = new Vector<Object>();
		StringBuffer equation = new StringBuffer();
		for (Object sb : elements)
			if (sb.toString().length() > 0)
				vsb.add(sb);
		if (vsb.size() >= 1)
			equation.append(vsb.get(0));

		for (int count = 1; count < vsb.size(); count++) {
			equation.append(operator);
			equation.append(vsb.get(count));
		}
		return equation;/*
						 * ((operator == '*') || (operator == '/')) ? equation :
						 * brackets(equation);
						 */

	}

	protected StringBuffer brackets(StringBuffer sb) {
		return concat(Character.valueOf('('), sb.append(')'));
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
	 * Returns the value of a PluginSpeciesReference object's stoichiometry
	 * either as a double or, if the stoichiometry has an integer value, as an
	 * int object.
	 * 
	 * @param ref
	 * @return
	 */
	protected StringBuffer getStoichiometry(PluginSpeciesReference ref) {
		if (ref.getStoichiometryMath() == null) {
			double stoich = ref.getStoichiometry();
			if ((int) stoich - stoich == 0)
				return new StringBuffer(Integer.toString((int) stoich));
			else
				return new StringBuffer(Double.toString(stoich));
		} return TextExport.toText(model, ref.getStoichiometryMath().getMath());
	}

	/**
	 * Returns the id of a PluginSpeciesReference object's belonging species
	 * as an object of type StringBuffer.
	 * 
	 * @param ref
	 * @return
	 */
	protected StringBuffer getSpecies(PluginSpeciesReference ref) {
		return new StringBuffer(ref.getSpecies());
	}
	
}
