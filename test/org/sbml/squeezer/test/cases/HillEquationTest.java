/*
 * $Id:  HillEquationTest.java 3:02:18 PM snagel$
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

import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.sbml.jsbml.Compartment;
import org.sbml.jsbml.KineticLaw;
import org.sbml.jsbml.Model;
import org.sbml.jsbml.Reaction;
import org.sbml.jsbml.SBMLDocument;
import org.sbml.jsbml.Species;
import org.sbml.squeezer.SqueezerOptions;
import org.sbml.squeezer.UnitConsistencyType;
import org.sbml.squeezer.kinetics.HillEquation;
import org.sbml.squeezer.kinetics.TypeStandardVersion;

/**
 * 
 * @author Sebastian Nagel
 * @version $Rev$
 * @since 1.4
 */
public class HillEquationTest extends KineticsTest{
	
	public HillEquationTest() {
		super();
		// Init the default ignore list for species:
		prefs.put(SqueezerOptions.IGNORE_THESE_SPECIES_WHEN_CREATING_LAWS, "C00001,C00038,C00070,C00076,C00080,C00175,C00238,C00282,C00291,C01327,C01528,C14818,C14819");
	}

	public Model initModel() {
		SBMLDocument doc = new SBMLDocument(2, 4);
		Model model = doc.createModel("m1");
		Compartment c = model.createCompartment("c1");
		Species s1 = model.createSpecies("s1", c);
		Species s2 = model.createSpecies("s2", c);
		Species p1 = model.createSpecies("p1", c);
		Species p2 = model.createSpecies("p2", c);
		Species e1 = model.createSpecies("e1", c);
		
		for (Species s : model.getListOfSpecies()) {
			s.setHasOnlySubstanceUnits(false);
		}
		
		Reaction r1 = model.createReaction("r1");
		r1.setReversible(false);
		r1.createReactant(s1);
		r1.createReactant(s2);
		r1.createProduct(p1);
		r1.createProduct(p2);
		r1.createModifier(e1);
		
		return model;
	}
	
	/**
	 * 
	 * @throws Throwable
	 */
	@Test
	public void testHillEquation() throws Throwable {
		Reaction r1 = model.getReaction("r1");
		KineticLaw kl = klg.createKineticLaw(r1, HillEquation.class, true, TypeStandardVersion.cat, UnitConsistencyType.amount, 1d);
		test(r1, kl, "vmaf_r1*s1*c1/ksp_r1*(1-p1*c1/(keq_r1*s1*c1))*(s1*c1/ksp_r1+p1*c1/ksp_r1)^(hic_r1-1)/(1+(s1*c1/ksp_r1+p1*c1/ksp_r1)^(hic_r1))");
		assertTrue(r1.isReversible());
	}
	
}
