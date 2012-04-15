/*
 * $Id:  HillRaddeEquationTest.java 3:02:18 PM snagel$
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

import org.junit.Test;
import org.sbml.jsbml.Compartment;
import org.sbml.jsbml.KineticLaw;
import org.sbml.jsbml.Model;
import org.sbml.jsbml.Reaction;
import org.sbml.jsbml.SBMLDocument;
import org.sbml.jsbml.SBO;
import org.sbml.jsbml.Species;
import org.sbml.squeezer.UnitConsistencyType;
import org.sbml.squeezer.kinetics.HillRaddeEquation;
import org.sbml.squeezer.kinetics.TypeStandardVersion;

/**
 * 
 * @author Sebastian Nagel
 * @version $Rev$
 * @since 1.4
 */
public class HillRaddeEquationTest extends KineticsTest{
	
	public HillRaddeEquationTest() {
		super();
	}

	public Model initModel() {
		SBMLDocument doc = new SBMLDocument(2, 4);
		Model model = doc.createModel("m1");
		Compartment c = model.createCompartment("c1");
		Species s1 = model.createSpecies("s1", c);
		s1.setSBOTerm(SBO.getEmptySet());
		Species p1 = model.createSpecies("p1", c);
		p1.setSBOTerm(SBO.getRNA());
		Species e1 = model.createSpecies("e1", c);
		e1.setSBOTerm(SBO.getGene());
		
		Reaction r1 = model.createReaction("r1");
		r1.setReversible(false);
		r1.createReactant(s1);
		r1.createProduct(p1);
		r1.createModifier(e1);
		r1.setSBOTerm(SBO.getTrigger());
		
		return model;
	}
	
	/**
	 * 
	 * @throws Throwable
	 */
	@Test
	public void testHillRaddeEquation() throws Throwable {
		Reaction r1 = model.getReaction("r1");
		KineticLaw kl = klg.createKineticLaw(r1, HillRaddeEquation.class, true, TypeStandardVersion.cat, UnitConsistencyType.amount, 1d);
		test(r1, kl, "");
	}
	
	
}
