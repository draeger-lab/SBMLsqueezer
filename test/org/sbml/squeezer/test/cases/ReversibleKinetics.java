/*
 * $Id:  ReversibleKinetics.java 1:23:59 PM jpfeuffer$
 * $URL: ReversibleKinetics.java $
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

import static org.junit.Assert.assertEquals;

import org.junit.BeforeClass;
import org.junit.Test;
import org.sbml.jsbml.Compartment;
import org.sbml.jsbml.KineticLaw;
import org.sbml.jsbml.Model;
import org.sbml.jsbml.ModifierSpeciesReference;
import org.sbml.jsbml.Reaction;
import org.sbml.jsbml.SBMLDocument;
import org.sbml.jsbml.Species;
import org.sbml.squeezer.KineticLawGenerator;
import org.sbml.squeezer.kinetics.*;

/**
 * @author Julianus Pfeuffer
 * @version $Rev$
 * @since 1.4
 */

public class ReversibleKinetics {
	
		static Reaction r1;
;
		static KineticLawGenerator klg;
		static SBMLDocument doc = new SBMLDocument(2, 4);
		static Model model = doc.createModel("uniuni_model");
		
		/**
		 * 
		 * @throws Throwable
		 */
		@BeforeClass
		public static void initTest() throws Throwable{
			
			/**
			 * Initialization
			 */
			
			Compartment c = model.createCompartment("c1");
			Species s1 = model.createSpecies("s1", c);
			Species p1 = model.createSpecies("p1", c);
			Species p2 = model.createSpecies("p2", c);
			Species inh = model.createSpecies("i1", c);
			Species kat = model.createSpecies("k1", c);
			Species act = model.createSpecies("a1", c);
			inh.setSBOTerm(20);
			act.setSBOTerm(462);
			kat.setSBOTerm(460);
			
			// Reaction with 2 products and all three modifiers.
			r1 = model.createReaction("r1");
			r1.createReactant(s1);
			r1.createProduct(p1);
			r1.createProduct(p2);
			r1.createModifier(act);
			r1.createModifier(kat);
			r1.createModifier(inh);
			

		}
		
		/**
		 * From here on, the Kinetic Laws for reversible reactions
		 */
		@Test
		public void testAdditiveLinear() throws Throwable{
			klg = new KineticLawGenerator(model);
			KineticLaw kl = klg.createKineticLaw(r1, AdditiveModelLinear.class, false);
			assertEquals("m_r1*(w_r1_p1*p1*c1+w_r1_p2*p2*c1+(b_r1+(v_r1_a1*a1*c1+v_r1_k1*k1*c1+v_r1_i1*i1*c1)))",kl.getMath().toFormula());
		}

}
