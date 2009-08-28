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
import java.util.ArrayList;
import java.util.List;

import org.sbml.ASTNode;
import org.sbml.KineticLaw;
import org.sbml.Model;
import org.sbml.Parameter;
import org.sbml.Reaction;
import org.sbml.SBO;
import org.sbml.squeezer.io.StringTools;
import org.sbml.squeezer.plugin.PluginSBMLReader;

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
		for (int modifierNum = 0; modifierNum < reaction.getNumModifiers(); modifierNum++) {
			type = reaction.getModifier(modifierNum).getSBOTerm();
			if (SBO.isModulation(type)) {
				inhibitors.add(reaction.getModifier(modifierNum).getSpecies());
				activators.add(reaction.getModifier(modifierNum).getSpecies());
			} else if (SBO.isInhibition(type))
				inhibitors.add(reaction.getModifier(modifierNum).getSpecies());
			else if (SBO.isTranscriptionalActivation(type)
					|| SBO.isTranslationalActivation(type))
				transActivators.add(reaction.getModifier(modifierNum)
						.getSpecies());
			else if (SBO.isTranscriptionalInhibition(type)
					|| SBO.isTranslationalInhibition(type))
				transInhibitors.add(reaction.getModifier(modifierNum)
						.getSpecies());
			else if (SBO.isUnknownCatalysis(type) || SBO.isTrigger(type)
					|| SBO.isPhysicalStimulation(type))
				activators.add(reaction.getModifier(modifierNum).getSpecies());
			else if (SBO.isCatalysis(type)) {
				if (SBO.isEnzymaticCatalysis(type)
						|| PluginSBMLReader.listOfPossibleEnzymes
								.contains(Integer.valueOf(type)))
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
	public static boolean isApplicable(Reaction reaction) {
		return false;
	}

	final Character underscore = StringTools.underscore;

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
	public BasicKineticLaw(Reaction parentReaction)
			throws RateLawNotApplicableException, IOException,
			IllegalFormatException {
		super(parentReaction);
		List<String> modActi = new ArrayList<String>();
		List<String> modCat = new ArrayList<String>();
		List<String> modInhib = new ArrayList<String>();
		List<String> modE = new ArrayList<String>();
		List<String> modTActi = new ArrayList<String>();
		List<String> modTInhib = new ArrayList<String>();
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
	 * list does not yet contain this parameter.
	 * 
	 * @param parameter
	 */
	void addGlobalParameter(Parameter parameter) {
		Model m = getModel();
		if (!m.getListOfParameters().contains(parameter))
			m.addParameter(new Parameter(parameter));
	}

	/**
	 * Adds the given parameter only to the list of local parameters if this
	 * list does not yet contain this parameter.
	 * 
	 * @param parameter
	 */
	void addLocalParameter(Parameter parameter) {
		if (!getListOfParameters().contains(parameter))
			addParameter(parameter);
	}

	/**
	 * 
	 * @param k
	 * @param things
	 * @return
	 */
	String append(StringBuffer k, Object... things) {
		return StringTools.append(k, things).toString();
	}

	/**
	 * 
	 * @param things
	 * @return
	 */
	String concat(Object... things) {
		return StringTools.concat(things).toString();
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
	abstract ASTNode createKineticEquation(List<String> modE,
			List<String> modActi, List<String> modTActi, List<String> modInhib,
			List<String> modTInhib, List<String> modCat)
			throws RateLawNotApplicableException, IllegalFormatException;

}
