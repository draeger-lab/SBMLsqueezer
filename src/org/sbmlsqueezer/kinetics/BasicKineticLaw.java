package org.sbmlsqueezer.kinetics;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

import org.sbml.libsbml.ASTNode;
import org.sbml.libsbml.libsbmlConstants;

import jp.sbi.celldesigner.plugin.PluginKineticLaw;
import jp.sbi.celldesigner.plugin.PluginModel;
import jp.sbi.celldesigner.plugin.PluginReaction;
import jp.sbi.celldesigner.plugin.PluginSpecies;

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

	protected HashMap<String, String> idAndName;

	protected String formelTeX;

	protected List<String> listOfLocalParameters, listOfGlobalParameters;

	protected String sboTerm;

	/**
	 * @param listOfPossibleEnzymes
	 * @param parent
	 * @throws RateLawNotApplicableException
	 */
	@SuppressWarnings("deprecation")
	public BasicKineticLaw(PluginReaction parentReaction, PluginModel model,
			List<String> listOfPossibleEnzymes)
			throws RateLawNotApplicableException {
		super(parentReaction);
		int reactionNum = 0;
		sboTerm = null;
		while (!model.getReaction(reactionNum).getId().equals(
				parentReaction.getId()))
			reactionNum++;
		idAndName = new HashMap<String, String>();
		listOfLocalParameters = new ArrayList<String>();
		listOfGlobalParameters = new ArrayList<String>();
		List<String> modActi = new ArrayList<String>();
		List<String> modCat = new ArrayList<String>();
		List<String> modInhib = new ArrayList<String>();
		List<String> modE = new ArrayList<String>();
		List<String> modTActi = new ArrayList<String>();
		List<String> modTInhib = new ArrayList<String>();
		identifyModifers(parentReaction, listOfPossibleEnzymes, modInhib,
				modTActi, modTInhib, modActi, modE, modCat);
		String formula = createKineticEquation(model, reactionNum, modE,
				modActi, modTActi, modInhib, modTInhib, modCat);
		if (getMath() == null) {
			setFormula(formula);
			setMathFromFormula();
		} 
	}

	/**
	 * @param parentReaction
	 * @param model
	 * @throws RateLawNotApplicableException
	 */
	public BasicKineticLaw(PluginReaction parentReaction, PluginModel model)
			throws RateLawNotApplicableException {
		this(parentReaction, model, getDefaultListOfPossibleEnzymes());
	}

	/**
	 * Creates and returns a list of molecule types accepted as an enzyme by
	 * default. These are:
	 * <ul type="disk">
	 * <li>ANTISENSE_RNA</li>
	 * <li>SIMPLE_MOLECULE</li>
	 * <li>UNKNOWN</li>
	 * <li>COMPLEX</li>
	 * <li>TRUNCATED</li>
	 * <li>GENERIC</li>
	 * <li>RNA</li>
	 * <li>RECEPTOR</li>
	 * </ul>
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
	 * TODO: comment missing
	 * 
	 * @param modInhib
	 * @param modActi
	 * @param modE
	 * @param reactionNum
	 * @param model
	 * @param modTInhib
	 * @param modTInhib
	 * @param modCat
	 * @throws RateLawNotApplicableException
	 */
	protected abstract String createKineticEquation(PluginModel model,
			int reactionNum, List<String> modE, List<String> modActi,
			List<String> modTActi, List<String> modInhib,
			List<String> modTInhib, List<String> modCat)
			throws RateLawNotApplicableException;

	/**
	 * Returns a list of the names of all parameters used by this law. This
	 * list contains parameters, which can be either stored locally, i.e., 
	 * assigned to the specific kinetic law or globally for the whole model.
	 * 
	 * @return
	 */
	public List<String> getLocalParameters() {
		return listOfLocalParameters;
	}
	
	/**
	 * Returns a list of names of all parameters, which are only allowed to
	 * be stored globally.
	 * @return
	 */
	public List<String> getGlobalParameters() {
		return listOfGlobalParameters;
	}

	/**
	 * Returns the LaTeX expression of the generated formual of this kinetic
	 * law.
	 * 
	 * @return
	 */
	public String getKineticTeX() {
		return formelTeX;
	}

	/**
	 * Returns the name of the kinetic formula of this object.
	 * 
	 * @return
	 */
	public abstract String getName();

	@Override
	public String toString() {
		if (sboTerm == null)
			sboTerm = getName();
		return sboTerm;
	}

	/**
	 * Returns the SBO identifier of the respective kinetic law if there is one
	 * or an empty String otherwise.
	 * 
	 * @return
	 */
	public abstract String getSBO();

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

}
