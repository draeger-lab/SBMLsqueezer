/*
 * $Id:  KineticTests.java 3:35:48 PM jpfeuffer$
 * $URL: KineticTests.java $
 * ---------------------------------------------------------------------
 * This file is part of SBMLsqueezer, a Java program that creates rate 
 * equations for reactions in SBML files (http://sbml.org).
 *
 * Copyright (C) 2006-2011 by the University of Tuebingen, Germany.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 * ---------------------------------------------------------------------
 */

package org.sbml.squeezer.test.cases;

import static org.junit.Assert.*;

import org.sbml.jsbml.KineticLaw;
import org.sbml.jsbml.UnitDefinition;

/**
 * @author Julianus Pfeuffer
 * @version $Rev$
 * @since 1.4
 */

public abstract class KineticTests {

	/**
	 * Checks if the derived unit definition are of the type mol/s or similar.
	 * @param kl
	 * @throws Throwable
	 */
	protected void testUnits(KineticLaw kl) throws Throwable {
		UnitDefinition ud = kl.getDerivedUnitDefinition();
		if (ud != null) {
			assertTrue("Could not derive unit definition of type substance per time.",ud.isVariantOfSubstancePerTime());
		} else {
			fail("Derived unit definition was null.");
		}
	}
	
	/**
	 * Checks if the unit is correct, then compares the derived formula with a given, expected one.
	 * @param kl
	 * @param formula
	 * @throws Throwable
	 */
	protected void test(KineticLaw kl, String formula) throws Throwable {
		testUnits(kl);
		assertEquals(formula, kl.getMath().toFormula());
	}
}
