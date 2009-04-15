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

import org.sbml.libsbml.ASTNode;
import org.sbml.libsbml.libsbmlConstants;
import org.sbmlsqueezer.io.LaTeXExport;

/**
 * An abstract super class of specialized kinetic laws.
 * 
 * @since 2.0
 * @version
 * @author Andreas Dr&auml;ger (draeger) <andreas.draeger@uni-tuebingen.de>
 * @author Nadine Hassis <Nadine.hassis@gmail.com> Copyright (c) ZBiT,
 *         University of T&uuml;bingen, Germany Compiler: JDK 1.6.0
 * @date Aug 1, 2007
 */
public abstract class BasicKineticLaw extends PluginKineticLaw implements
		libsbmlConstants {

	protected static final int ACTIVATION = 0;

	protected static final int INHIBITION = 1;

	protected static final int ASSOCIATION = 2;

	protected static final int DISSOCIATION = 3;

	public static final int FORWARD = 4;

	public static final int REVERSE = 5;

	protected HashMap<String, String> idAndName;

	protected StringBuffer formelTeX;

	protected List<StringBuffer> listOfLocalParameters, listOfGlobalParameters;

	protected String sboTerm;

	protected int type;

	/**
	 * 
	 * @param parentReaction
	 * @param model
	 * @throws RateLawNotApplicableException
	 * @throws IOException
	 */
	public BasicKineticLaw(PluginReaction parentReaction, PluginModel model)
			throws RateLawNotApplicableException, IOException {
		this(parentReaction, model, getDefaultListOfPossibleEnzymes(), 0);
	}

	/**
	 * 
	 * @param parentReaction
	 * @param model
	 * @param type
	 * @throws RateLawNotApplicableException
	 * @throws IOException
	 */
	public BasicKineticLaw(PluginReaction parentReaction, PluginModel model,
			int type) throws RateLawNotApplicableException, IOException {
		this(parentReaction, model, getDefaultListOfPossibleEnzymes(), type);
	}

	/**
	 * 
	 * @param parentReaction
	 * @param model
	 * @param listOfPossibleEnzymes
	 * @throws RateLawNotApplicableException
	 * @throws IOException
	 */
	public BasicKineticLaw(PluginReaction parentReaction, PluginModel model,
			List<String> listOfPossibleEnzymes)
			throws RateLawNotApplicableException, IOException {
		this(parentReaction, model, listOfPossibleEnzymes, 0);
	}

	/**
	 * 
	 * @param parentReaction
	 * @param model
	 * @param listOfPossibleEnzymes
	 * @param type
	 * @throws RateLawNotApplicableException
	 * @throws IOException
	 */
	@SuppressWarnings("deprecation")
	public BasicKineticLaw(PluginReaction parentReaction, PluginModel model,
			List<String> listOfPossibleEnzymes, int type)
			throws RateLawNotApplicableException, IOException {
		super(parentReaction);
		int reactionNum = 0;
		sboTerm = null;
		while (!model.getReaction(reactionNum).getId().equals(
				parentReaction.getId()))
			reactionNum++;
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
		StringBuffer formula = createKineticEquation(model, reactionNum, modE,
				modActi, modTActi, modInhib, modTInhib, modCat, type);
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

	@Override
	public String toString() {
		if (sboTerm == null)
			sboTerm = getName();
		return sboTerm;
	}

	/**
	 * 
	 * @param model
	 * @param reactionNum
	 * @param modE
	 * @param modActi
	 * @param modTActi
	 * @param modInhib
	 * @param modTInhib
	 * @param modCat
	 * @return
	 * @throws RateLawNotApplicableException
	 */
	protected abstract StringBuffer createKineticEquation(PluginModel model,
			int reactionNum, List<String> modE, List<String> modActi,
			List<String> modTActi, List<String> modInhib,
			List<String> modTInhib, List<String> modCat)
			throws RateLawNotApplicableException;

	/**
	 * TODO: comment missing
	 * 
	 * @param model
	 * @param reactionNum
	 * @param modE
	 * @param modActi
	 * @param modTActi
	 * @param modInhib
	 * @param modTInhib
	 * @param modCat
	 * @param type
	 * @return
	 * @throws RateLawNotApplicableException
	 */
	protected StringBuffer createKineticEquation(PluginModel model,
			int reactionNum, List<String> modE, List<String> modActi,
			List<String> modTActi, List<String> modInhib,
			List<String> modTInhib, List<String> modCat, int type)
			throws RateLawNotApplicableException {
		this.type = type;
		return createKineticEquation(model, reactionNum, modE, modActi,
				modTActi, modInhib, modTInhib, modCat);
	}

	/**
	 * 
	 * @param summands
	 * @return
	 */
	protected StringBuffer sum(StringBuffer... summands) {
		return arith("+", summands);
	}

	/**
	 * 
	 * @param subtrahents
	 * @return
	 */
	protected StringBuffer diff(StringBuffer... subtrahents) {
		return arith("-", subtrahents);
	}

	/**
	 * 
	 * @param factors
	 * @return
	 */
	protected StringBuffer times(StringBuffer... factors) {
		return arith("*", factors);
	}

	/**
	 * 
	 * @param numerator
	 * @param denominator
	 * @return
	 */
	protected StringBuffer frac(StringBuffer numerator, StringBuffer denominator) {
		return arith("/", numerator, denominator);
	}

	/**
	 * 
	 * @param basis
	 * @param exponent
	 * @return
	 */
	protected StringBuffer pow(StringBuffer basis, StringBuffer exponent) {
		if (Double.parseDouble(exponent.toString()) == 0.0)
			return new StringBuffer(1);
		else if (Double.parseDouble(exponent.toString()) == 1.0)
			return basis;
		else
			return arith("^", basis, exponent);
	}

	protected StringBuffer root(StringBuffer basis, StringBuffer exponent)
			throws IllegalFormatException {
		if (Double.parseDouble(exponent.toString()) == 0.0)
			throw new IllegalFormatException(
					"Cannot extract a zeroth root of anything");
		else if (Double.parseDouble(exponent.toString()) == 1.0)
			return basis;
		else {
			StringBuffer root = new StringBuffer("root(");
			root.append(exponent);
			root.append(",");
			root.append(basis);
			root.append(")");
			return root;
		}

	}

	/**
	 * 
	 * @param operator
	 * @param elements
	 * @return
	 */
	protected StringBuffer arith(String operator, StringBuffer... elements) {
		Vector<StringBuffer> vsb = new Vector<StringBuffer>();
		for (StringBuffer sb : elements)
			if (sb.length() > 0)
				vsb.add(sb);
		if (vsb.size() > 0)
			if (vsb.size() == 1)
				return vsb.get(0);
			else {
				StringBuffer sb = new StringBuffer("(");
				sb.append(vsb.get(0));
				for (int i = 1; i < vsb.size(); i++) {
					sb.append(operator);
					sb.append(vsb.get(i));
				}
				sb.append(")");
				return sb;
			}
		else
			return new StringBuffer();
	}

	/**
	 * 
	 * @return
	 */
	public int getType() {
		return type;
	}

	/**
	 * 
	 * @param sb
	 * @return
	 */
	protected StringBuffer removeBrackets(StringBuffer sb) {
		if (sb.length() > 0)
			if (sb.charAt(0) == '(' && sb.charAt(sb.length() - 1) == ')') {
				sb.deleteCharAt(sb.length() - 1);
				sb.deleteCharAt(0);
			}
		return sb;
	}
}
