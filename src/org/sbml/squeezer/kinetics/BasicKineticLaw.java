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
 *  along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.sbml.squeezer.kinetics;

import java.util.LinkedList;
import java.util.List;

import org.sbml.jsbml.ASTNode;
import org.sbml.jsbml.KineticLaw;
import org.sbml.jsbml.Parameter;
import org.sbml.jsbml.Reaction;
import org.sbml.jsbml.SBO;
import org.sbml.jsbml.SimpleSpeciesReference;
import org.sbml.jsbml.Species;
import org.sbml.jsbml.UnitDefinition;
import org.sbml.squeezer.ParameterFactory;
import org.sbml.squeezer.RateLawNotApplicableException;
import org.sbml.squeezer.ReactionType;
import org.sbml.squeezer.util.StringTools;

/**
 * An abstract super class of specialized kinetic laws, which provides methods
 * for creating {@link Parameter} and {@link UnitDefinition} objects and
 * maintains all these in dedicated lists. All variants of {@link KineticLaw}s
 * that are to be displayed in SBMLsqueezer's GUI and to be available in
 * SBMLsqueezer at all must extend this class and at least one of the interfaces
 * in this package.
 * 
 * @since 1.0
 * @version
 * @author Andreas Dr&auml;ger
 * @author Nadine Hassis
 * @author Hannes Borch
 * @date Aug 1, 2007
 */
public abstract class BasicKineticLaw extends KineticLaw {

	/**
	 * Generated serial version identifier.
	 */
	private static final long serialVersionUID = -7857020266193305483L;

	/**
	 * The ids of activators that enhance the progress of this rate law.
	 */
	private List<String> activators;

	/**
	 * If true all species whose hasOnlySubstanceUnits attribute is true are
	 * divided by the size of their surrounding compartment. If false species
	 * whose hasOnlySubstanceUnits attribute is false are multiplied with the
	 * size of their surrounding compartment.
	 */
	private boolean bringToConcentration;

	/**
	 * The default value that is used to initialize new parameters.
	 */
	private double defaultParamValue;

	/**
	 * The ids of the enzymes catalyzing the reaction described by this rate
	 * law.
	 */
	private List<String> enzymes;

	/**
	 * Ids of inhibitors that lower the velocity of this rate law.
	 */
	private List<String> inhibitors;

	/**
	 * The ids of catalysts that are no enzymes.
	 */
	private List<String> nonEnzymeCatalysts;

	/**
	 * Optional parameters needed to initialize subtypes of this object.
	 */
	private Object typeParameters[];

	/**
	 * True if the reaction system to which the parent reaction belongs has a
	 * full collumn rank.
	 */
	boolean fullRank;

	/**
	 * Allows for zeroth order reverse kinetics.
	 */
	double orderProducts;

	/**
	 * Allows for zeroth order forward kinetics.
	 */
	double orderReactants;

	/**
	 * <ol>
	 * <li>cat</li>
	 * <li>hal</li>
	 * <li>weg</li>
	 * </ol>
	 */
	short type;
	/**
	 * 
	 */
	ParameterFactory parameterFactory;

	/**
	 * 
	 */
	final Character underscore = StringTools.underscore;

	/**
	 * 
	 * @param parentReaction
	 * @param typeParameters
	 * @throws RateLawNotApplicableException
	 */
	public BasicKineticLaw(Reaction parentReaction, Object... typeParameters)
			throws RateLawNotApplicableException {
		super(parentReaction);
		this.typeParameters = typeParameters;
		this.fullRank = true;
		this.bringToConcentration = false;
		this.defaultParamValue = 1d;
		if (typeParameters.length > 0) {
			type = Short.parseShort(getTypeParameters()[0].toString());
		}
		if (typeParameters.length > 1
				&& !Boolean.parseBoolean(getTypeParameters()[1].toString())) {
			fullRank = false;
		}
		if (typeParameters.length > 2) {
			bringToConcentration = ((Integer) typeParameters[2]).intValue() != 0;
		}
		if (typeParameters.length > 3) {
			defaultParamValue = Double
					.parseDouble(typeParameters[3].toString());
		}
		enzymes = new LinkedList<String>();
		activators = new LinkedList<String>();
		inhibitors = new LinkedList<String>();
		nonEnzymeCatalysts = new LinkedList<String>();
		ReactionType.identifyModifers(parentReaction, enzymes, activators,
				inhibitors, nonEnzymeCatalysts);
		this.parameterFactory = new ParameterFactory(this, defaultParamValue,
				orderReactants, orderProducts, bringToConcentration);
		setMath(createKineticEquation(enzymes, activators, inhibitors,
				nonEnzymeCatalysts));
	}

	/**
	 * 
	 * @param enzymes
	 * @param activators
	 * @param inhibitors
	 * @param nonEnzymeCatalysts
	 * @return
	 * @throws RateLawNotApplicableException
	 */
	abstract ASTNode createKineticEquation(List<String> enzymes,
			List<String> activators, List<String> inhibitors,
			List<String> nonEnzymeCatalysts)
			throws RateLawNotApplicableException;

	/**
	 * Returns a string that gives a simple description of this rate equation.
	 * 
	 * @return
	 */
	public abstract String getSimpleName();

	/**
	 * 
	 * @return
	 */
	public Object[] getTypeParameters() {
		return typeParameters;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.jsbml.MathContainer#setMath(org.sbml.jsbml.ASTNode)
	 */
	public void setMath(ASTNode ast) {
		if (!isSetMath()) {
			super.setMath(ast);
		}
	}

	/**
	 * 
	 * @param simpleSpecRef
	 * @return
	 */
	ASTNode speciesTerm(SimpleSpeciesReference simpleSpecRef) {
		return speciesTerm(simpleSpecRef.getSpeciesInstance());
	}

	/**
	 * 
	 * @param species
	 * @return
	 */
	ASTNode speciesTerm(Species species) {
		ASTNode specTerm = new ASTNode(species, this);
		if (species.getHasOnlySubstanceUnits()) {
			if (bringToConcentration) {
				specTerm.divideBy(species.getCompartmentInstance());
			}
		} else if (!bringToConcentration) {
			specTerm.multiplyWith(species.getCompartmentInstance());
		}
		return specTerm;
	}

	/**
	 * 
	 * @param species
	 * @return
	 */
	ASTNode speciesTerm(String species) {
		return speciesTerm(getModel().getSpecies(species));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.MathContainer#toString()
	 */
	@Override
	public String toString() {
		return isSetSBOTerm() ? SBO.getTerm(getSBOTerm()).getDescription()
				.replace("\\,", ",") : getClass().getSimpleName();
	}
}
