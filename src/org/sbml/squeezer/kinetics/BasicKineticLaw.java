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
package org.sbml.squeezer.kinetics;

import java.io.IOException;
import java.util.IllegalFormatException;
import java.util.LinkedList;
import java.util.List;

import org.sbml.jlibsbml.ASTNode;
import org.sbml.jlibsbml.KineticLaw;
import org.sbml.jlibsbml.Model;
import org.sbml.jlibsbml.ModifierSpeciesReference;
import org.sbml.jlibsbml.Parameter;
import org.sbml.jlibsbml.Reaction;
import org.sbml.jlibsbml.SBO;
import org.sbml.squeezer.RateLawNotApplicableException;
import org.sbml.squeezer.io.StringTools;

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
public abstract class BasicKineticLaw extends KineticLaw {

	/**
	 * identify which Modifer is used
	 * 
	 * @param reactionNum
	 */
	public static final void identifyModifers(Reaction reaction,
			List<String> inhibitors, List<String> transActivators,
			List<String> transInhibitors, List<String> activators,
			List<String> enzymes, List<String> nonEnzymeCatalyzers) {
		inhibitors.clear();
		transActivators.clear();
		transInhibitors.clear();
		activators.clear();
		enzymes.clear();
		nonEnzymeCatalyzers.clear();
		int type;
		for (ModifierSpeciesReference modifier : reaction.getListOfModifiers()) {
			type = modifier.getSBOTerm();
			if (SBO.isModifier(type)) {
				inhibitors.add(modifier.getSpecies());
				activators.add(modifier.getSpecies());
			} else if (SBO.isInhibitor(type))
				inhibitors.add(modifier.getSpecies());
			else if (SBO.isTranscriptionalActivation(type)
					|| SBO.isTranslationalActivation(type))
				transActivators.add(modifier.getSpecies());
			else if (SBO.isTranscriptionalInhibitor(type)
					|| SBO.isTranslationalInhibitor(type))
				transInhibitors.add(modifier.getSpecies());
			else if (SBO.isTrigger(type) || SBO.isStimulator(type))
				// no extra support for unknown catalysis anymore...
				// physical stimulation is now also a stimulator.
				activators.add(modifier.getSpecies());
			else if (SBO.isCatalyst(type)) {
				if (SBO.isEnzymaticCatalysis(type))
					enzymes.add(modifier.getSpecies());
				else
					nonEnzymeCatalyzers.add(modifier.getSpecies());
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
	public static boolean isApplicable(Reaction reaction) {
		return false;
	}

	final Character underscore = StringTools.underscore;

	/**
	 * 
	 * @param parentReaction
	 * @throws RateLawNotApplicableException
	 * @throws IOException
	 * @throws IllegalFormatException
	 */
	public BasicKineticLaw(Reaction parentReaction)
			throws RateLawNotApplicableException, IOException,
			IllegalFormatException {
		super(parentReaction);
		List<String> modActi = new LinkedList<String>();
		List<String> modCat = new LinkedList<String>();
		List<String> modInhib = new LinkedList<String>();
		List<String> modE = new LinkedList<String>();
		List<String> modTActi = new LinkedList<String>();
		List<String> modTInhib = new LinkedList<String>();
		identifyModifers(parentReaction, modInhib, modTActi, modTInhib,
				modActi, modE, modCat);
		setMath(createKineticEquation(modE, modActi, modTActi, modInhib,
				modTInhib, modCat));
	}

	/**
	 * Returns the name of the kinetic formula of this object.
	 * 
	 * @return
	 */
	public abstract String getName();

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.MathContainer#toString()
	 */
	// @Override
	public String toString() {
		if (!isSetSBOTerm())
			return getName();
		return getSBOTermID();
	}

	/**
	 * Adds the given parameter only to the list of global parameters if this
	 * list does not yet contain this parameter. Furthermore, the parameter's
	 * default value is set to one instead of SBML's default of NaN (if no value
	 * has been set for this parameter before).
	 * 
	 * @param parameter
	 */
	void addGlobalParameter(Parameter parameter) {
		Model m = getModel();
		if (!parameter.isSetValue())
			parameter.setValue(1d);
		if (!m.getListOfParameters().contains(parameter))
			m.addParameter(new Parameter(parameter));
	}

	/**
	 * Adds the given parameter only to the list of local parameters if this
	 * list does not yet contain this parameter. Furthermore, the parameter's
	 * default value is set to one instead of SBML's default of NaN (if no value
	 * has been set for this parameter before).
	 * 
	 * @param parameter
	 */
	// @Override
	public void addParameter(Parameter parameter) {
		if (!parameter.isSetValue())
			parameter.setValue(1d);
		super.addParameter(parameter);
	}

	/**
	 * Convenient method to add multiple parameters.
	 * 
	 * @param parameters
	 */
	void addLocalParameters(Parameter... parameters) {
		for (Parameter parameter : parameters)
			addParameter(parameter);
	}

	/**
	 * 
	 * @param k
	 * @param things
	 * @return
	 */
	StringBuffer append(StringBuffer k, Object... things) {
		return StringTools.append(k, things);
	}

	/**
	 * 
	 * @param things
	 * @return
	 */
	StringBuffer concat(Object... things) {
		return StringTools.concat(things);
	}

	/**
	 * 
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
	abstract ASTNode createKineticEquation(List<String> modE,
			List<String> modActi, List<String> modTActi, List<String> modInhib,
			List<String> modTInhib, List<String> modCat)
			throws RateLawNotApplicableException, IllegalFormatException;

	/**
	 * Goes through the formula and identifies all global parameters that are
	 * referenced by this rate equation.
	 * 
	 * @return
	 */
	public List<Parameter> getReferencedGlobalParameters() {
		return getReferencedGlobalParameters(getMath());
	}

	/**
	 * 
	 * @param math
	 * @return
	 */
	private List<Parameter> getReferencedGlobalParameters(ASTNode math) {
		LinkedList<Parameter> pList = new LinkedList<Parameter>();
		if (math.getType().equals(ASTNode.Type.AST_NAME)
				&& (math.getVariable() instanceof Parameter)
				&& (getModel().getParameter(math.getVariable().getId()) != null))
			pList.add((Parameter) math.getVariable());
		for (ASTNode child : math.getListOfNodes())
			pList.addAll(getReferencedGlobalParameters(child));
		return pList;
	}
}
