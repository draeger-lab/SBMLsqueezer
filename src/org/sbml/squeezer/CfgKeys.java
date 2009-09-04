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
package org.sbml.squeezer;

/**
 * 
 * @author Andreas Dr&auml;ger <a
 *         href="mailto:andreas.draeger@uni-tuebingen.de">
 *         andreas.draeger@uni-tuebingen.de</a>
 * 
 */
public enum CfgKeys {
	/**
	 * 
	 */
	OPEN_DIR,
	/**
	 * 
	 */
	SAVE_DIR,
	
	/**
	 * Possible values are:
	 * <ul>
	 * <li>1 = generalized mass-action kinetics</li>
	 * <li>2 = Convenience kinetics</li>
	 * <li>3 = Michaelis-Menten kinetics</li>
	 * </ul>
	 */
	UNI_UNI_TYPE,
	/**
	 *            Possible values are:
	 *            <ul>
	 *            <li>1 = generalized mass-action kinetics</li>
	 *            <li>2 = Convenience kinetics</li>
	 *            <li>4 = Random Order Michealis Menten kinetics</li>
	 *            <li>6 = Ordered</li>
	 *            </ul>
	 */
	BI_UNI_TYPE,
	/**
	 *            Possible values are:
	 *            <ul>
	 *            <li>1 = generalized mass-action kinetics</li>
	 *            <li>2 = Convenience kinetics</li>
	 *            <li>4 = Random Order Michealis Menten kinetics</li>
	 *            <li>5 = Ping-Pong</li>
	 *            <li>6 = Ordered</li>
	 *            </ul>
	 */
	BI_BI_TYPE,
	/**
	 * If true, all reactions within the network are considered enzyme reactions. If 
	 * false, an explicit enzymatic catalyst must be assigned to a reaction to obtain
	 * this status.
	 */
	ALL_REACTIONS_ARE_ENZYME_CATALYZED,
	/**
	 * If true a new rate law will be created for each reaction irrespective of whether
	 * there is already a rate law assigned to this reaction or not.
	 */
	GENERATE_KINETIC_LAW_FOR_EACH_REACTION,
	/**
	 * If true all parameters are stored globally for the whole model (default)
	 * else parameters are stored locally for the respective kinetic equation
	 * they belong to.
	 */
	ADD_NEW_PARAMETERS_ALWAYS_GLOBALLY,
	/**
	 * Property that decides whether to set all reactions to reversible before
	 * creating new kinetic equations.
	 */
	TREAT_ALL_REACTIONS_REVERSIBLE, 
	/**
	 * The maximal number of reactants so that the reaction is still considered
	 * plausible.
	 */
	MAX_NUMBER_OF_REACTANTS
	/*
	 *            A list which contains the names of all species that are
	 *            accepted to act as enzymes during a reaction. Valid are the
	 *            following entries (upper and lower case is ignored):
	 *            <ul>
	 *            <li>ANTISENSE_RNA</li>
	 *            <li>SIMPLE_MOLECULE</li>
	 *            <li>RECEPTOR</li>
	 *            <li>UNKNOWN</li>
	 *            <li>COMPLEX</li>
	 *            <li>TRUNCATED</li>
	 *            <li>GENERIC</li>
	 *            <li>RNA</li>
	 *            </ul>
	 *            Not allowed entries like <li>Phenotype</li> <li>Gene</li> <li>
	 *            IonChannel</li> <li>Ion</li> </ul> will be filtered out of the
	 *            list.
	 */
}
