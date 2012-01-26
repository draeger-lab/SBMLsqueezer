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

import org.junit.BeforeClass;
import org.junit.Test;
import org.sbml.jsbml.Compartment;
import org.sbml.jsbml.KineticLaw;
import org.sbml.jsbml.Model;
import org.sbml.jsbml.ModifierSpeciesReference;
import org.sbml.jsbml.Reaction;
import org.sbml.jsbml.SBMLDocument;
import org.sbml.jsbml.Species;
import org.sbml.jsbml.SpeciesReference;
import org.sbml.squeezer.KineticLawGenerator;
import org.sbml.squeezer.ReactionType;


/**
 * @author Julianus Pfeuffer
 * @version $Rev$
 * @since 1.4
 */

public class UniUniKineticsTest {
	static Reaction r1;
	static Reaction r2;
	static Reaction r3;
	static Reaction r4;
	static KineticLawGenerator klg;
	static SBMLDocument doc = new SBMLDocument(2, 4);
	static Model model = doc.createModel("uniuni_model");
	
	@BeforeClass
	public static void initTest() throws Throwable{
		
		/**
		 * Initialization
		 */
		
		Compartment c = model.createCompartment("c1");
		Species s1 = model.createSpecies("s1", c);
		Species p1 = model.createSpecies("p1", c);
		Species i1 = model.createSpecies("i1", c);
		Species k1 = model.createSpecies("k1", c);
		Species a1 = model.createSpecies("a1", c);
		r1 = model.createReaction("r1");
		r1.setReversible(false);
		SpeciesReference s1ref = r1.createReactant(s1);
		r1.createProduct(p1);
//		ReactionType r1type = new ReactionType(r1);
//		System.out.println(r1type.reactantOrder(r1));
//		System.out.println(r1type.productOrder(r1));
		
		r2 = model.createReaction("r2");
		r2.setReversible(false);
		ModifierSpeciesReference inh = r2.createModifier(i1);
		inh.setSBOTerm(20); // inhibitor
		r2.createReactant(s1);
		r2.createProduct(p1);
		
		r3 = model.createReaction("r3");
		r3.setReversible(false);
		ModifierSpeciesReference kat = r3.createModifier(k1);
		kat.setSBOTerm(460);
		r3.createReactant(s1);
		r3.createProduct(p1);
		
		r4 = model.createReaction("r4");
		r4.setReversible(false);
		ModifierSpeciesReference act = r4.createModifier(a1);
		act.setSBOTerm(462);
		r4.createReactant(s1);
		r4.createProduct(p1);
		//klg.generateLaws();
		//klg.storeKineticLaws();
	}
	
	/**
	 * From here on, the Kinetic Laws for irreversible reactions
	 */
	
	@Test
	public void testMichMent() throws Throwable{
		klg = new KineticLawGenerator(model);
		//KineticLaw kl = klg.createKineticLaw(r1, MichaelisMenten.class.getName(), false);
//		SBPreferences prefs = SBPreferences.getPreferencesFor(SqueezerOptions.class);
//		prefs.put(SqueezerOptions., value)
		KineticLaw kl = klg.createKineticLaw(r1, "MichaelisMenten", false);
		//klg.getReactionType(reactionID)
		assertEquals("vmax_r1*s1*c1/(kmc_r1_s1+s1*c1)",kl.getMath().toFormula());
	}
	
	@Test
	public void testCMRL() throws Throwable{
		KineticLaw kl = klg.createKineticLaw(r1, "CommonModularRateLaw", false);
		assertEquals("vmax_r1*(s1*c1/kmc_r1_s1)^(hco_r1)/((1+s1*c1/kmc_r1_s1)^(hco_r1)+(1+p1*c1/kmc_r1_p1)^(hco_r1)-1)",kl.getMath().toFormula());
	}
	
	@Test
	public void testPMRL() throws Throwable{
		KineticLaw kl = klg.createKineticLaw(r1, "PowerLawModularRateLaw", false);
		assertEquals("vmax_r1*(s1*c1/kmc_r1_s1)^(hco_r1)",kl.getMath().toFormula());
	}
	
	@Test
	public void testSMRL() throws Throwable{
		KineticLaw kl = klg.createKineticLaw(r1, "SimultaneousBindingModularRateLaw", false);
		assertEquals("vmax_r1*(s1*c1/kmc_r1_s1)^(hco_r1)/((1+s1*c1/kmc_r1_s1)^(hco_r1)*(1+p1*c1/kmc_r1_p1)^(hco_r1))",kl.getMath().toFormula());
	}
	
	@Test
	public void testFMRL() throws Throwable{
		KineticLaw kl = klg.createKineticLaw(r1, "ForceDependentModularRateLaw", false);
		assertEquals("vmax_r1*(s1*c1/kmc_r1_s1)^(hco_r1)/((s1*c1/kmc_r1_s1)^(hco_r1)*(p1*c1/kmc_r1_p1)^(hco_r1))^(0.5)",kl.getMath().toFormula());
	}
	
	@Test
	public void testHill() throws Throwable{
		KineticLaw kl = klg.createKineticLaw(r1, "HillEquation", false);
		assertEquals("vmax_r1*(s1*c1)^(hic_r1)/(ksp_r1^(hic_r1)+(s1*c1)^(hic_r1))",kl.getMath().toFormula());
	}
	
	@Test
	public void testConv() throws Throwable{
		KineticLaw kl = klg.createKineticLaw(r1, "ConvenienceKinetics", false);
		assertEquals("vmag_r1*s1*c1/kmc_r1_s1*(kG_s1*kmc_r1_s1/(kG_p1*kmc_r1_p1))^(0.5)/(1+s1*c1/kmc_r1_s1-1)",kl.getMath().toFormula());
	}
	
	
//	@Test
//	public void testGMA() throws Throwable{
//		KineticLaw kl = klg.createKineticLaw(r1, "GeneralizedMassAction", false);
//		assertEquals("vmax_r1*(s1*c1)^(hic_r1)/(ksp_r1^(hic_r1)+(s1*c1)^(hic_r1))",kl.getMath().toFormula());
//	}

	
	/**
	 * From here on, the Kinetic Laws for reversible reactions
	 */
	@Test
	public void testHillRev() throws Throwable{
		klg = new KineticLawGenerator(model);
		KineticLaw kl = klg.createKineticLaw(r1, "HillEquation", true);
		assertEquals("vmaf_r1*s1*c1/ksp_r1*(1-p1*c1/(keq_r1*s1*c1))*(s1*c1/ksp_r1+p1*c1/ksp_r1)^(hic_r1-1)/(1+(s1*c1/ksp_r1+p1*c1/ksp_r1)^(hic_r1))",kl.getMath().toFormula());
	}
	
	@Test
	public void testMichMentRev() throws Throwable{
		klg = new KineticLawGenerator(model);
		KineticLaw kl = klg.createKineticLaw(r1, "MichaelisMenten", true);
		assertEquals("(vmaf_r1/kmc_r1_s1*s1*c1-vmar_r1/kmc_r1_p1*p1*c1)/(1+(s1*c1/kmc_r1_s1+p1*c1/kmc_r1_p1))",kl.getMath().toFormula());
	}
	
	@Test
	public void testCMRLRev() throws Throwable{
		KineticLaw kl = klg.createKineticLaw(r1, "CommonModularRateLaw", true);
		assertEquals("(vmaf_r1*(s1*c1/kmc_r1_s1)^(hco_r1)-vmar_r1*(p1*c1/kmc_r1_p1)^(hco_r1))/((1+s1*c1/kmc_r1_s1)^(hco_r1)+(1+p1*c1/kmc_r1_p1)^(hco_r1)-1)",kl.getMath().toFormula());
	}
	
	@Test
	public void testPMRLRev() throws Throwable{
		KineticLaw kl = klg.createKineticLaw(r1, "PowerLawModularRateLaw", true);
		assertEquals("vmaf_r1*(s1*c1/kmc_r1_s1)^(hco_r1)-vmar_r1*(p1*c1/kmc_r1_p1)^(hco_r1)",kl.getMath().toFormula());
	}
	
	@Test
	public void testSMRLRev() throws Throwable{
		KineticLaw kl = klg.createKineticLaw(r1, "SimultaneousBindingModularRateLaw", true);
		assertEquals("(vmaf_r1*(s1*c1/kmc_r1_s1)^(hco_r1)-vmar_r1*(p1*c1/kmc_r1_p1)^(hco_r1))/((1+s1*c1/kmc_r1_s1)^(hco_r1)*(1+p1*c1/kmc_r1_p1)^(hco_r1))",kl.getMath().toFormula());
	}
	
	@Test
	public void testFMRLRev() throws Throwable{
		KineticLaw kl = klg.createKineticLaw(r1, "ForceDependentModularRateLaw", true);
		assertEquals("(vmaf_r1*(s1*c1/kmc_r1_s1)^(hco_r1)-vmar_r1*(p1*c1/kmc_r1_p1)^(hco_r1))/((s1*c1/kmc_r1_s1)^(hco_r1)*(p1*c1/kmc_r1_p1)^(hco_r1))^(0.5)",kl.getMath().toFormula());
	}
	
	@Test
	public void testConvRev() throws Throwable{
		KineticLaw kl = klg.createKineticLaw(r1, "ConvenienceKinetics", true);
		assertEquals("vmag_r1*(s1*c1/kmc_r1_s1*(kG_s1*kmc_r1_s1/(kG_p1*kmc_r1_p1))^(0.5)-p1*c1/kmc_r1_p1*(kG_p1*kmc_r1_p1/(kG_s1*kmc_r1_s1))^(0.5))/(1+s1*c1/kmc_r1_s1+p1*c1/kmc_r1_p1)",kl.getMath().toFormula());
	}
	
	/**
	 * From here on, the Kinetic Laws for irreversible reactions with an inhibitor
	 */
	@Test
	public void testHillInh() throws Throwable{
		//Modifier added!
		klg = new KineticLawGenerator(model);
		//use reaction r2
		KineticLaw kl = klg.createKineticLaw(r2, "HillEquation", false);
		assertEquals("vmax_r2*(s1*c1)^(hic_r2)/((kmc_r2_i1^(hic_r2)+(i1*c1)^(hic_r2))/(kmc_r2_i1^(hic_r2)+beta_r2*(i1*c1)^(hic_r2))*ksp_r2^(hic_r2)+(s1*c1)^(hic_r2))",kl.getMath().toFormula());
	}
	
	@Test
	public void testConvInh() throws Throwable{
		KineticLaw kl = klg.createKineticLaw(r2, "ConvenienceKinetics", false);
		assertEquals("kic_r2_i1/(kic_r2_i1+i1*c1)*vmag_r2*s1*c1/kmc_r2_s1*(kG_s1*kmc_r2_s1/(kG_p1*kmc_r2_p1))^(0.5)/(1+s1*c1/kmc_r2_s1-1)",kl.getMath().toFormula());
	}
	
	/**
	 * From here on, the Kinetic Laws for reversible reactions with an inhibitor
	 */
	@Test
	public void testHillInhRev() throws Throwable{
		KineticLaw kl = klg.createKineticLaw(r2, "HillEquation", true);
		assertEquals("vmaf_r2*s1*c1/ksp_r2*(1-p1*c1/(keq_r2*s1*c1))*(s1*c1/ksp_r2+p1*c1/ksp_r2)^(hic_r2-1)/((kmc_r2_i1^(hic_r2)+(i1*c1)^(hic_r2))/(kmc_r2_i1^(hic_r2)+beta_r2*(i1*c1)^(hic_r2))+(s1*c1/ksp_r2+p1*c1/ksp_r2)^(hic_r2))",kl.getMath().toFormula());
	}
	
	@Test
	public void testConvInhRev() throws Throwable{
		KineticLaw kl = klg.createKineticLaw(r2, "ConvenienceKinetics", true);
		assertEquals("kic_r2_i1/(kic_r2_i1+i1*c1)*vmag_r2*(s1*c1/kmc_r2_s1*(kG_s1*kmc_r2_s1/(kG_p1*kmc_r2_p1))^(0.5)-p1*c1/kmc_r2_p1*(kG_p1*kmc_r2_p1/(kG_s1*kmc_r2_s1))^(0.5))/(1+s1*c1/kmc_r2_s1+p1*c1/kmc_r2_p1)",kl.getMath().toFormula());
	}
	
	/**
	 * From here on, the Kinetic Laws for irreversible reactions with an activator
	 */
	@Test
	public void testHillAct() throws Throwable{
		//Modifier added!
		klg = new KineticLawGenerator(model);
		//use reaction r2
		KineticLaw kl = klg.createKineticLaw(r2, "HillEquation", false);
		assertEquals("vmax_r2*(s1*c1)^(hic_r2)/((kmc_r2_i1^(hic_r2)+(i1*c1)^(hic_r2))/(kmc_r2_i1^(hic_r2)+beta_r2*(i1*c1)^(hic_r2))*ksp_r2^(hic_r2)+(s1*c1)^(hic_r2))",kl.getMath().toFormula());
	}
	
	@Test
	public void testConvAct() throws Throwable{
		KineticLaw kl = klg.createKineticLaw(r2, "ConvenienceKinetics", false);
		assertEquals("kic_r2_i1/(kic_r2_i1+i1*c1)*vmag_r2*s1*c1/kmc_r2_s1*(kG_s1*kmc_r2_s1/(kG_p1*kmc_r2_p1))^(0.5)/(1+s1*c1/kmc_r2_s1-1)",kl.getMath().toFormula());
	}
	
	/**
	 * From here on, the Kinetic Laws for reversible reactions with an activator
	 */
	@Test
	public void testHillActRev() throws Throwable{
		KineticLaw kl = klg.createKineticLaw(r4, "HillEquation", true);
		assertEquals("vmaf_r4*s1*c1/ksp_r4*(1-p1*c1/(keq_r4*s1*c1))*(s1*c1/ksp_r4+p1*c1/ksp_r4)^(hic_r4-1)/((kmc_r4_a1^(hic_r4)+(a1*c1)^(hic_r4))/(kmc_r4_a1^(hic_r4)+beta_r4*(a1*c1)^(hic_r4))+(s1*c1/ksp_r4+p1*c1/ksp_r4)^(hic_r4))",kl.getMath().toFormula());
	}
	
	@Test
	public void testConvActRev() throws Throwable{
		KineticLaw kl = klg.createKineticLaw(r4, "ConvenienceKinetics", true);
		assertEquals("a1*c1/(kac_r4_a1+a1*c1)*vmag_r4*(s1*c1/kmc_r4_s1*(kG_s1*kmc_r4_s1/(kG_p1*kmc_r4_p1))^(0.5)-p1*c1/kmc_r4_p1*(kG_p1*kmc_r4_p1/(kG_s1*kmc_r4_s1))^(0.5))/(1+s1*c1/kmc_r4_s1+p1*c1/kmc_r4_p1)",kl.getMath().toFormula());
	}
	
	/**
	 * From here on, the Kinetic Laws for irreversible reactions with a catalysator
	 */
	@Test
	public void testHillKat() throws Throwable{
		//Modifier added!
		klg = new KineticLawGenerator(model);
		//use reaction r3
		KineticLaw kl = klg.createKineticLaw(r3, "HillEquation", false);
		assertEquals("vmax_r2*(s1*c1)^(hic_r2)/((kmc_r2_i1^(hic_r2)+(i1*c1)^(hic_r2))/(kmc_r2_i1^(hic_r2)+beta_r2*(i1*c1)^(hic_r2))*ksp_r2^(hic_r2)+(s1*c1)^(hic_r2))",kl.getMath().toFormula());
	}
	
	@Test
	public void testConvKat() throws Throwable{
		KineticLaw kl = klg.createKineticLaw(r3, "ConvenienceKinetics", false);
		assertEquals("kic_r2_i1/(kic_r2_i1+i1*c1)*vmag_r2*s1*c1/kmc_r2_s1*(kG_s1*kmc_r2_s1/(kG_p1*kmc_r2_p1))^(0.5)/(1+s1*c1/kmc_r2_s1-1)",kl.getMath().toFormula());
	}

	
	

	
	
	
	
	/*
	File store = new File("test.sbml");
	SBMLWriter.write(doc, store, ' ', (short) 2);
	SBMLWriter.write(doc, System.out, ' ', (short) 2);
	System.out.println();
	SBML2LaTeX.convert(doc, new File(System.getProperty("user.dir") + "test.pdf"));
	*/
}