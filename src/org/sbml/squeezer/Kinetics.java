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
	 * Liebermeister's reversible power law formulation.
	 */
	REVERSIBLE_POWER_LAW,
	/**
	 * S-System based equation
	 */
	SSYSTEM_KINETIC,
	/**
	 * An additive model equation
	 */
	ADDITIVE_KINETIC,
	/**
	 * A special additive model equation (Vohradský, J. 2001 and Vu, T. T. & Vohradský, J. 2007)
	 */
	ADDITIVE_KINETIC1,
	/**
	 * A special additive model equation (Weaver, D.; Workman, C. & Stormo, G. 1999)
	 */
	ADDITIVE_KINETIC2,
	/**
	 * A special additive model equation (NetGenerator linear model)
	 */
	ADDITIVE_KINETIC_NGlinear,
	/**
	 * A special additive model equation (NetGenerator non-linear model)
	 */
	ADDITIVE_KINETIC_NGnonlinear;

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
		case SSYSTEM_KINETIC:
			return "S-System based kinetic";
		case ADDITIVE_KINETIC:
			return "An additive model equation";
		case ADDITIVE_KINETIC1:
			return "A special additive model equation (Vohradský 2001 and Vu & Vohradský 2007)";
		case ADDITIVE_KINETIC2:
			return "A special additive model equation (Weaver, Workman & Stormo 1999)";
		case ADDITIVE_KINETIC_NGlinear:
			return "A special additive model equation (NetGenerator linear model)";
		case ADDITIVE_KINETIC_NGnonlinear:
			return "A special additive model equation (NetGenerator non-linear model)";
		default: // TODO: default?
			return "Generalized mass-action";
		}
	}

	/**
	 * 
	 * @param className
	 * @return
	 */
	public static Kinetics getTypeForName(String className) {
		if (!className.startsWith("org.sbml.squeezer.kinetics."))
			return null;
		className = className.substring(27);
		if (className.startsWith("Convenience"))
			return CONVENIENCE_KINETICS;
		else if (className.equals("GeneralizedMassAction"))
			return GENERALIZED_MASS_ACTION;
		else if (className.equals("GRNSSystemEquation"))
			return SSYSTEM_KINETIC;
		else if (className.equals("GRNSAdditivModel"))
			return ADDITIVE_KINETIC;
		else if (className.equals("GRNSAdditivModel_1"))
			return ADDITIVE_KINETIC1;
		else if (className.equals("GRNSAdditivModel_2"))
			return ADDITIVE_KINETIC2;		
		else if (className.equals("GRNSAdditivModel_NGlinear"))
			return ADDITIVE_KINETIC_NGlinear;
		else if (className.equals("GRNSAdditivModel_NGnonlinear"))
			return ADDITIVE_KINETIC_NGnonlinear;		
		else if (className.equals("HillEquation"))
			return HILL_EQUATION;
		else if (className.equals("IrrevCompetNonCooperativeEnzymes"))
			return COMPETETIVE_NON_EXCLUSIVE_INHIB;
		else if (className.equals("MichaelisMenten"))
			return MICHAELIS_MENTEN;
		else if (className.equals("OrderedMechanism"))
			return ORDERED_MECHANISM;
		else if (className.equals("PingPongMechanism"))
			return PING_PONG_MECAHNISM;
		else if (className.equals("RandomOrderMechanism"))
			return RANDOM_ORDER_MECHANISM;
		else if (className.equals("ReversiblePowerLaw"))
			return REVERSIBLE_POWER_LAW;
		else if (className.equals("ZerothOrderForwardGMAK"))
			return ZEROTH_ORDER_FORWARD_MA;
		else if (className.equals("ZerothOrderReverseGMAK"))
			return Kinetics.ZEROTH_ORDER_REVERSE_MA;
		return null;
	}
}
