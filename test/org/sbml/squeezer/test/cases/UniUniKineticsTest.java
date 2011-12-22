/*
 * $Id:  UniUniKineticsTest.java 3:05:26 PM jpfeuffer$
 * $URL: UniUniKineticsTest.java $
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

import java.io.File;

import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.sbml.jsbml.Compartment;
import org.sbml.jsbml.KineticLaw;
import org.sbml.jsbml.Model;
import org.sbml.jsbml.ModifierSpeciesReference;
import org.sbml.jsbml.Reaction;
import org.sbml.jsbml.SBMLDocument;
import org.sbml.jsbml.SBMLWriter;
import org.sbml.jsbml.SBase;
import org.sbml.jsbml.Species;
import org.sbml.jsbml.Unit.Kind;
import org.sbml.squeezer.KineticLawGenerator;
import org.sbml.squeezer.SBMLsqueezer;
import org.sbml.squeezer.SqueezerOptions;
import org.sbml.squeezer.io.SqSBMLWriter;
import org.sbml.squeezer.kinetics.HillEquation;
import org.sbml.squeezer.kinetics.MichaelisMenten;
import org.sbml.tolatex.SBML2LaTeX;

import de.zbit.util.prefs.SBPreferences;

/**
 * @author Julianus Pfeuffer
 * @version $Rev$
 * @since 1.4
 */

public class UniUniKineticsTest {
	static Reaction r1;
	static KineticLawGenerator klg;
	
	@BeforeClass
	public static void initTest() throws Throwable{
		SBMLDocument doc = new SBMLDocument(2, 4);
		Model model = doc.createModel("uniuni_model");
		Compartment c = model.createCompartment("c1");
		Species s1 = model.createSpecies("s1", c);
		Species p1 = model.createSpecies("p1", c);
		//Species i1 = model.createSpecies("i1", c);
		r1 = model.createReaction("r1");
		r1.setReversible(false);
		r1.createReactant(s1);
		r1.createProduct(p1);

		//ModifierSpeciesReference mod = r1.createModifier(i1);
		//mod.setSBOTerm(20); // inhibitor
		
		klg = new KineticLawGenerator(model);
		
		//klg.generateLaws();
		//klg.storeKineticLaws();
	}
	@Test
	public void testMichMent() throws Throwable{
		
		//KineticLaw kl = klg.createKineticLaw(r1, MichaelisMenten.class.getName(), false);
//		SBPreferences prefs = SBPreferences.getPreferencesFor(SqueezerOptions.class);
//		prefs.put(SqueezerOptions., value)
		KineticLaw kl1 = klg.createKineticLaw(r1, "MichaelisMenten", false);
		assertEquals("vmax_r1*s1*c1/(kmc_r1_s1+s1*c1)",kl1.getMath().toFormula());
	}
	
	@Test
	public void testMichMentRev() throws Throwable{
		KineticLaw kl2 = klg.createKineticLaw(r1, "MichaelisMenten", true);
		assertEquals("(vmaf_r1/kmc_r1_s1*s1*c1-vmar_r1/kmc_r1_p1*p1*c1)/(1+(s1*c1/kmc_r1_s1+p1*c1/kmc_r1_p1))",kl2.getMath().toFormula());
	}
	
	/*
	File store = new File("test.sbml");
	SBMLWriter.write(doc, store, ' ', (short) 2);
	SBMLWriter.write(doc, System.out, ' ', (short) 2);
	System.out.println();
	SBML2LaTeX.convert(doc, new File(System.getProperty("user.dir") + "test.pdf"));
	*/
}
