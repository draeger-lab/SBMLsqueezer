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

import java.util.IllegalFormatException;
import java.util.LinkedList;
import java.util.List;

import org.sbml.jsbml.ASTNode;
import org.sbml.jsbml.KineticLaw;
import org.sbml.jsbml.Model;
import org.sbml.jsbml.ModifierSpeciesReference;
import org.sbml.jsbml.Parameter;
import org.sbml.jsbml.Reaction;
import org.sbml.jsbml.SBO;
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
	private Object typeParameters[];

	/**
	 * 
	 * @param parentReaction
	 * @param typeParameters 
	 * @throws RateLawNotApplicableException
	 * @throws IllegalFormatException
	 */
	public BasicKineticLaw(Reaction parentReaction, Object... typeParameters)
			throws RateLawNotApplicableException, IllegalFormatException {
		super(parentReaction);
		this.typeParameters = typeParameters;
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
	 * 
	 * @return
	 */
	public Object[] getTypeParameters() {
		return typeParameters;
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
	 * If the parent model does not yet contain a parameter with the given id, a
	 * new parameter is created, its value is set to 1 and it is added to the
	 * list of global parameters in the model. If there is already a parameter
	 * with this id, a reference to it will be returned.
	 * 
	 * @param id
	 *            the identifier of the global parameter.
	 */
	Parameter createOrGetGlobalParameter(String id) {
		Model m = getModel();
		Parameter p = m.getParameter(id);
		if (p == null) {
			p = new Parameter(id, getLevel(), getVersion());
			p.setValue(1d);
			m.addParameter(p);
		}
		return p;
	}

	/**
	 * If a parameter with the given identifier has already been created and is
	 * contained in the list of local parameters for this kinetic law, a pointer
	 * to it will be returned. If no such parameter exists, a new parameter with
	 * a value of 1 will be created and a pointer to it will be returned.
	 * 
	 * @param id
	 *            the identifier of the local parameter.
	 * @return
	 */
	Parameter createOrGetParameter(String id) {
		Parameter p = getParameter(id);
		if (p == null) {
			p = new Parameter(id, getLevel(), getVersion());
			p.setValue(1d);
			addParameter(p);
		}
		return p;
	}

	/**
	 * Equvivalent to {@see createOrGetParameter} but the parts of the id can be
	 * given separately to this method and are concatenated.
	 * 
	 * @param idParts
	 * @return
	 */
	Parameter createOrGetParameter(Object... idParts) {
		return createOrGetParameter(concat(idParts).toString());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.MathContainer#toString()
	 */
	// @Override
	public String toString() {
		return isSetSBOTerm() ? SBO.getTerm(getSBOTerm()).getDescription()
				.replace("\\,", ",") : getClass().getSimpleName();
	}
}
