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
 * @author Andreas Dr&auml;ger <a
 *         href="mailto:andreas.draeger@uni-tuebingen.de">
 *         andreas.draeger@uni-tuebingen.de</a>
 * @date 2009-09-01
 */
public enum Kinetics {
	/**
	 * Generalized mass action kinetics
	 */
	GENERALIZED_MASS_ACTION,
	/**
	 * Convenience kinetics
	 */
	CONVENIENCE_KINETICS,
	/**
	 * Michaelis Menten kinetics
	 */
	MICHAELIS_MENTEN,
	/**
	 * Random order mechanism
	 */
	RANDOM_ORDER_MECHANISM,
	/**
	 * Ping-pong mechanism
	 */
	PING_PONG_MECAHNISM,
	/**
	 * Ordered mechanism
	 */
	ORDERED_MECHANISM,
	/**
	 * Hill equation
	 */
	HILL_EQUATION,
	/**
	 * Irreversible non-modulation
	 */
	IRREV_NON_MODULATED_ENZYME_KIN,
	/**
	 * Zeroth order forward generalized mass action kinetics
	 */
	ZEROTH_ORDER_FORWARD_MA,
	/**
	 * Zeroth order reverse generalized mass action kinetics
	 */
	ZEROTH_ORDER_REVERSE_MA,
	/**
	 * Competitive non-exclusive, non-cooperative inihibition
	 */
	COMPETETIVE_NON_EXCLUSIVE_INHIB,
	/**
	 * 
	 */
	REVERSIBLE_POWER_LAW,
	/**
	 * Test equation
	 */
	TEST_KINETIK;

	/**
	 * <ol>
	 * <li>Irreversible non-modulated non-interacting enzyme kinetics</li>
	 * <li>Generalized mass action</li>
	 * <li>Simple convenience/independent convenience</li>
	 * <li>Michaelis-Menten</li>
	 * <li>Random order mechanism</li>
	 * <li>Ping-Pong mechanism</li>
	 * <li>Ordered mechanism</li>
	 * <li>Hill equation</li>
	 * <li>Zeroth order forward/reverse mass action kinetics</li>
	 * <li>Irreversible non-exclusive non-cooperative competitive inihibition</li>
	 * <ol>
	 */
	public String getEquationName() {
		// TODO: impractical to store the names here! Should access the kinetic
		// classes.
		switch (this) {
		case COMPETETIVE_NON_EXCLUSIVE_INHIB:
			return "Irreversible non-exclusive non-cooperative competitive inihibition";
		case ZEROTH_ORDER_REVERSE_MA:
			return "Zeroth order reverse mass action kinetics";
		case ZEROTH_ORDER_FORWARD_MA:
			return "Zeroth order forward mass action kinetics";
		case IRREV_NON_MODULATED_ENZYME_KIN:
			return "Irreversible non-modulated non-interacting reactants";
		case HILL_EQUATION:
			return "Hill equation";
		case ORDERED_MECHANISM:
			return "Ordered mechanism";
		case PING_PONG_MECAHNISM:
			return "Ping-Pong mechanism";
		case RANDOM_ORDER_MECHANISM:
			return "Random order mechanism";
		case MICHAELIS_MENTEN:
			return "Michaelis-Menten";
		case CONVENIENCE_KINETICS:
			return "Convenience kinetics";
		case REVERSIBLE_POWER_LAW:
			return "Reversible power law";
		case TEST_KINETIK:
			return "Test-Kinetik!";
		default:
			return "Generalized mass-action";
		}
	}

	/**
	 * 
	 * @param name
	 * @return
	 */
	static public Kinetics getTypeForName(String name) {
		String prefix = "org.sbml.squeezer.kinetics.";
		if (name
				.equals(prefix + "IrrevCompetNonCooperativeEnzymes"))
			return COMPETETIVE_NON_EXCLUSIVE_INHIB;
		if (name.equals(prefix + "ZerothOrderReverseGMAK"))
			return ZEROTH_ORDER_REVERSE_MA;
		if (name.equals(prefix + "ZerothOrderForwardGMAK"))
			return ZEROTH_ORDER_FORWARD_MA;
		if (name
				.equals(prefix + "IrrevNonModulatedNonInteractingEnzymes"))
			return IRREV_NON_MODULATED_ENZYME_KIN;
		if (name.equals(prefix + "HillEquation"))
			return HILL_EQUATION;
		if (name.equals(prefix + "OrderedMechanism"))
			return ORDERED_MECHANISM;
		if (name.equals(prefix + "PingPongMechanism"))
			return PING_PONG_MECAHNISM;
		if (name.equals(prefix + "RandomOrderMechanism"))
			return RANDOM_ORDER_MECHANISM;
		if (name.equals(prefix + "MichaelisMenten"))
			return MICHAELIS_MENTEN;
		if (name.startsWith(prefix + "Convenience"))
			return CONVENIENCE_KINETICS;
		if (name.equals(prefix + "TestKinetik"))
			return TEST_KINETIK;
		if (name.equals(prefix + "ReversiblePowerLaw"))
			return REVERSIBLE_POWER_LAW;
		// if (name.equals(prefix + "GeneralizedMassAction"))
		return GENERALIZED_MASS_ACTION;
	}
}
