/*
 * $Id:  UniUniKineticsTest.java 3:05:26 PM jpfeuffer$
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
import org.sbml.jsbml.ModifierSpeciesReference;
import org.sbml.jsbml.Reaction;
import org.sbml.jsbml.SBMLDocument;
import org.sbml.jsbml.Species;
import org.sbml.squeezer.UnitConsistencyType;
import org.sbml.squeezer.kinetics.CommonModularRateLaw;
import org.sbml.squeezer.kinetics.ConvenienceKinetics;
import org.sbml.squeezer.kinetics.DirectBindingModularRateLaw;
import org.sbml.squeezer.kinetics.ForceDependentModularRateLaw;
import org.sbml.squeezer.kinetics.HillEquation;
import org.sbml.squeezer.kinetics.MichaelisMenten;
import org.sbml.squeezer.kinetics.PowerLawModularRateLaw;
import org.sbml.squeezer.kinetics.SimultaneousBindingModularRateLaw;
import org.sbml.squeezer.kinetics.TypeStandardVersion;


/**
 * @author Julianus Pfeuffer
 * @version $Rev$
 * @since 1.4
 */
public class UniUniKineticsTest extends KineticsTest {

	/**
	 * 
	 */
	private Reaction r1, r2, r3, r4;
	
	/**
	 * Initialization of a model and basic reactions for this test class.
	 * It handles reactions of the form A (<)-> B. 
	 * @throws Throwable
	 */
	public Model initModel() {
		
		/**
		 * Initialization
		 */
		SBMLDocument doc = new SBMLDocument(2, 4);
		Model model = doc.createModel("uniuni_model");
		Compartment c = model.createCompartment("c1");
		Species s1 = model.createSpecies("s1", c);
		Species p1 = model.createSpecies("p1", c);
		Species i1 = model.createSpecies("i1", c);
		Species k1 = model.createSpecies("k1", c);
		Species a1 = model.createSpecies("a1", c);
		
		for (Species s : model.getListOfSpecies()) {
			s.setHasOnlySubstanceUnits(false);
		}
		
		/**
		 * reaction without modifier
		 */
		r1 = model.createReaction("r1");
		r1.setReversible(false);
		r1.createReactant(s1);
		r1.createProduct(p1);
//		ReactionType r1type = new ReactionType(r1);
//		System.out.println(r1type.reactantOrder(r1));
//		System.out.println(r1type.productOrder(r1));
		
		/**
		 * reaction with inhibitor
		 */
		r2 = model.createReaction("r2");
		r2.setReversible(false);
		ModifierSpeciesReference inh = r2.createModifier(i1);
		inh.setSBOTerm(20); // inhibitor
		r2.createReactant(s1);
		r2.createProduct(p1);
		
		/**
		 * reaction with catalyst
		 */
		r3 = model.createReaction("r3");
		r3.setReversible(false);
		ModifierSpeciesReference kat = r3.createModifier(k1);
		kat.setSBOTerm(460);
		r3.createReactant(s1);
		r3.createProduct(p1);
		
		/**
		 * reaction with activator
		 */
		r4 = model.createReaction("r4");
		r4.setReversible(false);
		ModifierSpeciesReference act = r4.createModifier(a1);
		act.setSBOTerm(462);
		r4.createReactant(s1);
		r4.createProduct(p1);
		
		return model;
	}



	/*=========================================================*
	 *      I R R E V E R S I B L E   R E A C T I O N S        *
	 *=========================================================*/



	/**
	 * Tests the {@link MichaelisMenten} kinetics for A -> B
	 * @throws Throwable
	 */
	@Test
	public void testMichMent() throws Throwable{
		KineticLaw kl = klg.createKineticLaw(r1, MichaelisMenten.class, false, TypeStandardVersion.cat, UnitConsistencyType.amount, 1d);
		test(r1, kl, "vmax_r1*s1*c1/(kmc_r1_s1+s1*c1)");
	}
	
	/**
	 * Tests the {@link CommonModularRateLaw} for A -> B
	 * @throws Throwable
	 */
	@Test
	public void testCMRL() throws Throwable{
		KineticLaw kl = klg.createKineticLaw(r1, CommonModularRateLaw.class, false, TypeStandardVersion.cat, UnitConsistencyType.amount, 1d);
		test(r1, kl, "vmax_r1*(s1*c1/kmc_r1_s1)^hco_r1/((1+s1*c1/kmc_r1_s1)^hco_r1+(1+p1*c1/kmc_r1_p1)^hco_r1-1)");
	}
	
	/**
	 * Tests the {@link DirectBindingModularRateLaw} for A -> B
	 * @throws Throwable
	 */
	@Test
	public void testDMRL() throws Throwable{
		KineticLaw kl = klg.createKineticLaw(r1, DirectBindingModularRateLaw.class, false, TypeStandardVersion.cat, UnitConsistencyType.amount, 1d);
		test(r1, kl, "vmax_r1*(s1*c1/kmc_r1_s1)^hco_r1/(1+(s1*c1/kmc_r1_s1)^hco_r1+(p1*c1/kmc_r1_p1)^hco_r1)");
	}
	
	/**
	 * Tests the {@link PowerLawModularRateLaw} for A -> B
	 * @throws Throwable
	 */
	@Test
	public void testPMRL() throws Throwable{
		KineticLaw kl = klg.createKineticLaw(r1, PowerLawModularRateLaw.class, false, TypeStandardVersion.cat, UnitConsistencyType.amount, 1d);
		test(r1, kl, "vmax_r1*(s1*c1/kmc_r1_s1)^hco_r1");
	}
	
	/**
	 * Tests the {@link SimultaneousBindingModularRateLaw} for A -> B
	 * @throws Throwable
	 */
	@Test
	public void testSMRL() throws Throwable{
		KineticLaw kl = klg.createKineticLaw(r1, SimultaneousBindingModularRateLaw.class, false, TypeStandardVersion.cat, UnitConsistencyType.amount, 1d);
		test(r1, kl, "vmax_r1*(s1*c1/kmc_r1_s1)^hco_r1/((1+s1*c1/kmc_r1_s1)^hco_r1*(1+p1*c1/kmc_r1_p1)^hco_r1)");
	}
	
	/**
	 * Tests the {@link ForceDependentModularRateLaw} for A -> B
	 * @throws Throwable
	 */
	@Test
	public void testFMRL() throws Throwable{
		KineticLaw kl = klg.createKineticLaw(r1, ForceDependentModularRateLaw.class, false, TypeStandardVersion.cat, UnitConsistencyType.amount, 1d);
		test(r1, kl, "vmax_r1*(s1*c1/kmc_r1_s1)^hco_r1/((s1*c1/kmc_r1_s1)^hco_r1*(p1*c1/kmc_r1_p1)^hco_r1)^(0.5)");
	}
	
	/**
	 * Tests the {@link HillEquation} for A -> B
	 * @throws Throwable
	 */
	@Test
	public void testHill() throws Throwable{
		KineticLaw kl = klg.createKineticLaw(r1, HillEquation.class, false, TypeStandardVersion.cat, UnitConsistencyType.amount, 1d);
		test(r1, kl, "vmax_r1*(s1*c1)^hic_r1/(ksp_r1^hic_r1+(s1*c1)^hic_r1)");
	}
	
	/**
	 * Tests the {@link ConvenienceKinetics} for A -> B
	 * @throws Throwable
	 */
	@Test
	public void testConv() throws Throwable{
		KineticLaw kl = klg.createKineticLaw(r1, ConvenienceKinetics.class, false, TypeStandardVersion.cat, UnitConsistencyType.amount, 1d);
		test(r1, kl, "vmag_r1*s1*c1/kmc_r1_s1*(kG_s1*kmc_r1_s1/(kG_p1*kmc_r1_p1))^(0.5)/(1+s1*c1/kmc_r1_s1)");
	}
	
//	@Test
//	public void testGMA() throws Throwable{
//		KineticLaw kl = klg.createKineticLaw(r1, GeneralizedMassAction.class, false, TypeStandardVersion.cat, UnitConsistencyType.amount, 1d);
//		assertEquals("vmax_r1*(s1*c1)^(hic_r1)/(ksp_r1^(hic_r1)+(s1*c1)^(hic_r1))",kl.getMath().toFormula());
//	}



	/*=========================================================*
	 *        R E V E R S I B L E   R E A C T I O N S          *
	 *=========================================================*/



	/**
	 * Tests the {@link MichaelisMenten} kinetics for A <-> B
	 * @throws Throwable
	 */
	@Test
	public void testMichMentRev() throws Throwable{
		KineticLaw kl = klg.createKineticLaw(r1, MichaelisMenten.class, true, TypeStandardVersion.cat, UnitConsistencyType.amount, 1d);
		test(r1, kl, "(vmaf_r1/kmc_r1_s1*s1*c1-vmar_r1/kmc_r1_p1*p1*c1)/(1+(s1*c1/kmc_r1_s1+p1*c1/kmc_r1_p1))");
	}
	
	/**
	 * Tests the {@link CommonModularRateLaw} for A <-> B
	 * @throws Throwable
	 */
	@Test
	public void testCMRLRev() throws Throwable{
		KineticLaw kl = klg.createKineticLaw(r1, CommonModularRateLaw.class, true, TypeStandardVersion.cat, UnitConsistencyType.amount, 1d);
		test(r1, kl, "(vmaf_r1*(s1*c1/kmc_r1_s1)^hco_r1-vmar_r1*(p1*c1/kmc_r1_p1)^hco_r1)/((1+s1*c1/kmc_r1_s1)^hco_r1+(1+p1*c1/kmc_r1_p1)^hco_r1-1)");
	}
	
	/**
	 * Tests the {@link DirectBindingModularRateLaw} for A <-> B
	 * @throws Throwable
	 */
	@Test
	public void testDMRLRev() throws Throwable{
		KineticLaw kl = klg.createKineticLaw(r1, DirectBindingModularRateLaw.class, true, TypeStandardVersion.cat, UnitConsistencyType.amount, 1d);
		test(r1, kl, "(vmaf_r1*(s1*c1/kmc_r1_s1)^hco_r1-vmar_r1*(p1*c1/kmc_r1_p1)^hco_r1)/(1+(s1*c1/kmc_r1_s1)^hco_r1+(p1*c1/kmc_r1_p1)^hco_r1)");
	}
	
	/**
	 * Tests the {@link PowerLawModularRateLaw} for A <-> B
	 * @throws Throwable
	 */
	@Test
	public void testPMRLRev() throws Throwable{
		KineticLaw kl = klg.createKineticLaw(r1, PowerLawModularRateLaw.class, true, TypeStandardVersion.cat, UnitConsistencyType.amount, 1d);
		test(r1, kl, "vmaf_r1*(s1*c1/kmc_r1_s1)^hco_r1-vmar_r1*(p1*c1/kmc_r1_p1)^hco_r1");
	}
	
	/**
	 * Tests the {@link SimultaneousBindingModularRateLaw} for A <-> B
	 * @throws Throwable
	 */
	@Test
	public void testSMRLRev() throws Throwable{
		KineticLaw kl = klg.createKineticLaw(r1, SimultaneousBindingModularRateLaw.class, true, TypeStandardVersion.cat, UnitConsistencyType.amount, 1d);
		test(r1, kl, "(vmaf_r1*(s1*c1/kmc_r1_s1)^hco_r1-vmar_r1*(p1*c1/kmc_r1_p1)^hco_r1)/((1+s1*c1/kmc_r1_s1)^hco_r1*(1+p1*c1/kmc_r1_p1)^hco_r1)");
	}
	
	/**
	 * Tests the {@link ForceDependentModularRateLaw} for A <-> B
	 * @throws Throwable
	 */
	@Test
	public void testFMRLRev() throws Throwable{
		KineticLaw kl = klg.createKineticLaw(r1, ForceDependentModularRateLaw.class, true, TypeStandardVersion.cat, UnitConsistencyType.amount, 1d);
		test(r1, kl, "(vmaf_r1*(s1*c1/kmc_r1_s1)^hco_r1-vmar_r1*(p1*c1/kmc_r1_p1)^hco_r1)/((s1*c1/kmc_r1_s1)^hco_r1*(p1*c1/kmc_r1_p1)^hco_r1)^(0.5)");
	}
	
	/**
	 * Tests the {@link ConvenienceKinetics} for A <-> B
	 * @throws Throwable
	 */
	@Test
	public void testConvRev() throws Throwable{
		KineticLaw kl = klg.createKineticLaw(r1, ConvenienceKinetics.class, true, TypeStandardVersion.cat, UnitConsistencyType.amount, 1d);
		test(r1, kl, "vmag_r1*(s1*c1/kmc_r1_s1*(kG_s1*kmc_r1_s1/(kG_p1*kmc_r1_p1))^(0.5)-p1*c1/kmc_r1_p1*(kG_p1*kmc_r1_p1/(kG_s1*kmc_r1_s1))^(0.5))/(1+s1*c1/kmc_r1_s1+p1*c1/kmc_r1_p1)");
	}
	
	/**
	 * Tests the {@link HillEquation} for A <-> B
	 * @throws Throwable
	 */
	@Test
	public void testHillRev() throws Throwable{
		KineticLaw kl = klg.createKineticLaw(r1, HillEquation.class, true, TypeStandardVersion.cat, UnitConsistencyType.amount, 1d);
		test(r1, kl, "vmaf_r1*s1*c1/ksp_r1*(1-p1*c1/(keq_r1*s1*c1))*(s1*c1/ksp_r1+p1*c1/ksp_r1)^(hic_r1-1)/(1+(s1*c1/ksp_r1+p1*c1/ksp_r1)^hic_r1)");
	}




	/*=========================================================*
	 *  I R R E V E R S I B L E   W I T H   I N H I B I T O R  *
	 *=========================================================*/



	/**
	 * Tests the {@link HillEquation} for A -> B (with inhibitor)
	 * @throws Throwable
	 */
	@Test
	public void testHillInh() throws Throwable{
		KineticLaw kl = klg.createKineticLaw(r2, HillEquation.class, false, TypeStandardVersion.cat, UnitConsistencyType.amount, 1d);
		test(r2, kl, "vmax_r2*(s1*c1)^hic_r2/((kmc_r2_i1^hic_r2+(i1*c1)^hic_r2)/(kmc_r2_i1^hic_r2+beta_r2*(i1*c1)^hic_r2)*ksp_r2^hic_r2+(s1*c1)^hic_r2)");
	}

	/**
	 * Tests the {@link ConvenienceKinetics} for A -> B (with inhibitor)
	 * @throws Throwable
	 */
	@Test
	public void testConvInh() throws Throwable{
		KineticLaw kl = klg.createKineticLaw(r2, ConvenienceKinetics.class, false, TypeStandardVersion.cat, UnitConsistencyType.amount, 1d);
		test(r2, kl, "kic_r2_i1/(kic_r2_i1+i1*c1)*vmag_r2*s1*c1/kmc_r2_s1*(kG_s1*kmc_r2_s1/(kG_p1*kmc_r2_p1))^(0.5)/(1+s1*c1/kmc_r2_s1)");
	}




	/*=========================================================*
	 *    R E V E R S I B L E   W I T H   I N H I B I T O R    *
	 *=========================================================*/
	
	
		
	/**
	 * Tests the {@link HillEquation} for A <-> B (with inhibitor)
	 * @throws Throwable
	 */
	@Test
	public void testHillInhRev() throws Throwable{
		KineticLaw kl = klg.createKineticLaw(r2, HillEquation.class, true, TypeStandardVersion.cat, UnitConsistencyType.amount, 1d);
		test(r2, kl, "vmaf_r2*s1*c1/ksp_r2*(1-p1*c1/(keq_r2*s1*c1))*(s1*c1/ksp_r2+p1*c1/ksp_r2)^(hic_r2-1)/((kmc_r2_i1^hic_r2+(i1*c1)^hic_r2)/(kmc_r2_i1^hic_r2+beta_r2*(i1*c1)^hic_r2)+(s1*c1/ksp_r2+p1*c1/ksp_r2)^hic_r2)");
	}
	
	/**
	 * Tests the {@link ConvenienceKinetics} for A <-> B (with inhibitor)
	 * @throws Throwable
	 */
	@Test
	public void testConvInhRev() throws Throwable{
		KineticLaw kl = klg.createKineticLaw(r2, ConvenienceKinetics.class, true, TypeStandardVersion.cat, UnitConsistencyType.amount, 1d);
		test(r2, kl, "kic_r2_i1/(kic_r2_i1+i1*c1)*vmag_r2*(s1*c1/kmc_r2_s1*(kG_s1*kmc_r2_s1/(kG_p1*kmc_r2_p1))^(0.5)-p1*c1/kmc_r2_p1*(kG_p1*kmc_r2_p1/(kG_s1*kmc_r2_s1))^(0.5))/(1+s1*c1/kmc_r2_s1+p1*c1/kmc_r2_p1)");
	}
	
	
	
	
	/*=========================================================*
	 *  I R R E V E R S I B L E   W I T H   A C T I V A T O R  *
	 *=========================================================*/
	
	
	
	
	/**
	 * Tests the {@link HillEquation} for A -> B (with activator)
	 * @throws Throwable
	 */
	@Test
	public void testHillAct() throws Throwable{
		KineticLaw kl = klg.createKineticLaw(r2, HillEquation.class, false, TypeStandardVersion.cat, UnitConsistencyType.amount, 1d);
		test(r2, kl, "vmax_r2*(s1*c1)^hic_r2/((kmc_r2_i1^hic_r2+(i1*c1)^hic_r2)/(kmc_r2_i1^hic_r2+beta_r2*(i1*c1)^hic_r2)*ksp_r2^hic_r2+(s1*c1)^hic_r2)");
	}
	
	/**
	 * Tests the {@link ConvenienceKinetics} for A -> B (with activator)
	 * @throws Throwable
	 */
	@Test
	public void testConvAct() throws Throwable{
		KineticLaw kl = klg.createKineticLaw(r2, ConvenienceKinetics.class, false, TypeStandardVersion.cat, UnitConsistencyType.amount, 1d);
		test(r2, kl, "kic_r2_i1/(kic_r2_i1+i1*c1)*vmag_r2*s1*c1/kmc_r2_s1*(kG_s1*kmc_r2_s1/(kG_p1*kmc_r2_p1))^(0.5)/(1+s1*c1/kmc_r2_s1)");
	}




	/*=========================================================*
	 *    R E V E R S I B L E   W I T H   A C T I V A T O R    *
	 *=========================================================*/



	/**
	 * Tests the {@link HillEquation} for A <-> B (with activator)
	 * @throws Throwable
	 */
	@Test
	public void testHillActRev() throws Throwable{
		KineticLaw kl = klg.createKineticLaw(r4, HillEquation.class, true, TypeStandardVersion.cat, UnitConsistencyType.amount, 1d);
		test(r4, kl, "vmaf_r4*s1*c1/ksp_r4*(1-p1*c1/(keq_r4*s1*c1))*(s1*c1/ksp_r4+p1*c1/ksp_r4)^(hic_r4-1)/((kmc_r4_a1^hic_r4+(a1*c1)^hic_r4)/(kmc_r4_a1^hic_r4+beta_r4*(a1*c1)^hic_r4)+(s1*c1/ksp_r4+p1*c1/ksp_r4)^hic_r4)");
	}
	
	/**
	 * Tests the {@link ConvenienceKinetics} for A <-> B (with activator)
	 * @throws Throwable
	 */
	@Test
	public void testConvActRev() throws Throwable{
		KineticLaw kl = klg.createKineticLaw(r4, ConvenienceKinetics.class, true, TypeStandardVersion.cat, UnitConsistencyType.amount, 1d);
		test(r4, kl, "a1*c1/(kac_r4_a1+a1*c1)*vmag_r4*(s1*c1/kmc_r4_s1*(kG_s1*kmc_r4_s1/(kG_p1*kmc_r4_p1))^(0.5)-p1*c1/kmc_r4_p1*(kG_p1*kmc_r4_p1/(kG_s1*kmc_r4_s1))^(0.5))/(1+s1*c1/kmc_r4_s1+p1*c1/kmc_r4_p1)");
	}



	
	/*=========================================================*
	 *   I R R E V E R S I B L E   W I T H   C A T A L Y S T   *
	 *=========================================================*/



	/**
	 * Tests the {@link HillEquation} for A -> B (with catalyst)
	 * @throws Throwable
	 */
	@Test
	public void testHillKat() throws Throwable{
		KineticLaw kl = klg.createKineticLaw(r3, HillEquation.class, false, TypeStandardVersion.cat, UnitConsistencyType.amount, 1d);
		test(r3, kl, "kcat_r3_k1*k1*c1*(s1*c1)^hic_r3_k1/(ksp_r3_k1^hic_r3_k1+(s1*c1)^hic_r3_k1)");
	}

	/**
	 * Tests the {@link ConvenienceKinetics} for A -> B (with catalyst)
	 * @throws Throwable
	 */
	@Test
	public void testConvKat() throws Throwable{
		KineticLaw kl = klg.createKineticLaw(r3, ConvenienceKinetics.class, false, TypeStandardVersion.cat, UnitConsistencyType.amount, 1d);
		test(r3, kl, "k1*c1*kcrg_r3_k1*s1*c1/kmc_r3_s1_k1*(kG_s1*kmc_r3_s1_k1/(kG_p1*kmc_r3_p1_k1))^(0.5)/(1+s1*c1/kmc_r3_s1_k1)");
	}



	/*=========================================================*
	 *     R E V E R S I B L E   W I T H   C A T A L Y S T     *
	 *=========================================================*/



	/**
	 * Tests the {@link ConvenienceKinetics} for A <-> B (with catalyst)
	 * @throws Throwable
	 */
	@Test
	public void testConvKatRev() throws Throwable{
		KineticLaw kl = klg.createKineticLaw(r3, ConvenienceKinetics.class, true, TypeStandardVersion.cat, UnitConsistencyType.amount, 1d);
		test(r3, kl, "k1*c1*kcrg_r3_k1*(s1*c1/kmc_r3_s1_k1*(kG_s1*kmc_r3_s1_k1/(kG_p1*kmc_r3_p1_k1))^(0.5)-p1*c1/kmc_r3_p1_k1*(kG_p1*kmc_r3_p1_k1/(kG_s1*kmc_r3_s1_k1))^(0.5))/(1+s1*c1/kmc_r3_s1_k1+p1*c1/kmc_r3_p1_k1)");
	}
	
	/**
	 * Tests the {@link HillEquation} for A <-> B (with catalyst)
	 * @throws Throwable
	 */
	@Test
	public void testHillKatRev() throws Throwable{
		KineticLaw kl = klg.createKineticLaw(r3, HillEquation.class, true, TypeStandardVersion.cat, UnitConsistencyType.amount, 1d);
		test(r3, kl, "kcrf_r3_k1*k1*c1*s1*c1/ksp_r3_k1*(1-p1*c1/(keq_r3*s1*c1))*(s1*c1/ksp_r3_k1+p1*c1/ksp_r3_k1)^(hic_r3_k1-1)/(1+(s1*c1/ksp_r3_k1+p1*c1/ksp_r3_k1)^hic_r3_k1)");
	}

}
