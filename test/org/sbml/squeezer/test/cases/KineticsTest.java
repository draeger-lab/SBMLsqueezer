/*
 * $Id: KineticsTest.java 14.03.2012 10:59:29 draeger$
 * $URL$
 * ---------------------------------------------------------------------
 * This file is part of SBMLsqueezer, a Java program that creates rate 
 * equations for reactions in SBML files (http://sbml.org).
 *
 * Copyright (C) 2006-2012 by the University of Tuebingen, Germany.
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.sbml.jsbml.KineticLaw;
import org.sbml.jsbml.Model;
import org.sbml.jsbml.UnitDefinition;
import org.sbml.squeezer.KineticLawGenerator;
import org.sbml.squeezer.SqueezerOptions;

import de.zbit.util.prefs.SBPreferences;

/**
 * Abstract class for classes that test generated kinetic equations and derived units
 * @author Andreas Dr&auml;ger
 * @version $Rev$
 * @since 1.4
 */
public abstract class KineticsTest {
	
	private static int count;
	
	
	/**
	 * Initializes counter and the summary report before the test.
	 */
	@BeforeClass
	public static void init() {
		count = 0;
		System.out.print("Count\tReaction\tDerived units\tRate law\n");
		System.out.print("-----\t--------\t-------------\t--------\n");
	}
	
	/**
	 * The rate law generator.
	 */
	protected KineticLawGenerator klg;
	/**
	 * 
	 */
	protected Model model;
	/**
	 * User settings for the test cases.
	 */
	protected SBPreferences prefs;

	
	/**
	 * The abstract constructor that automatically initializes a {@link Model} and a corresponding
	 * {@link KineticLawGenerator}
	 */
	public KineticsTest() {
		model = initModel();
		prefs = SBPreferences.getPreferencesFor(SqueezerOptions.class);
		try {
			klg = new KineticLawGenerator(model);
		} catch (Throwable e) {
			e.printStackTrace();
			fail();
		}
	}

	/**
	 * Creates a model for testing purposes.
	 * 
	 * @return
	 */
	protected abstract Model initModel();
	
	/**
	 * Report the result of a single unit test
	 * @param kl
	 * @throws Throwable
	 */
	public void testUnits(KineticLaw kl) throws Throwable {
		UnitDefinition ud = kl.getDerivedUnitDefinition();
		if (ud != null) {
			System.out.printf("%d\t%s\t\t%s\t%s\n",
				count++,
				kl.getParent(), UnitDefinition.printUnits(ud, true), 
				kl.getClass().getSimpleName());
			assertTrue("Derived UnitDefinition is not of the type SubstancePerTime",ud.isVariantOfSubstancePerTime());
		} else {
			fail("Could not derive a UnitDefinition");
		}
	}
	
	/**
	 * Do the complete test, incl. unit checking with the JUnit 4 {@link Assert}.assertEquals method
	 * @param kl
	 * @param formula
	 * @throws Throwable
	 */
	protected void test(KineticLaw kl, String formula) throws Throwable {
		testUnits(kl);
		assertEquals(formula, kl.getMath().toFormula());
	}
	
}
