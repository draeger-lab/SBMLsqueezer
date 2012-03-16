/*
 * $Id:  BiUniKinetics.java 3:20:45 PM jpfeuffer$
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
import org.sbml.jsbml.Species;
import org.sbml.jsbml.SpeciesReference;
import org.sbml.squeezer.KineticLawGenerator;
import org.sbml.squeezer.kinetics.CommonModularRateLaw;
import org.sbml.squeezer.kinetics.DirectBindingModularRateLaw;
import org.sbml.squeezer.kinetics.ForceDependentModularRateLaw;
import org.sbml.squeezer.kinetics.OrderedMechanism;
import org.sbml.squeezer.kinetics.PowerLawModularRateLaw;
import org.sbml.squeezer.kinetics.SimultaneousBindingModularRateLaw;

/**
 * @author Julianus Pfeuffer
 * @version $Rev$
 * @since 1.4
 */
public class BiUniKineticsTest extends KineticsTest {
		
	/**
	 * 
	 */
	private Reaction r1;
	/**
	 * 
	 */
	private SpeciesReference s1ref;
	
	/**
	 * Initialization of a model and basic reactions for this test class.
	 * It handles reactions of the form A + B (<)-> C and 2A (<)-> C. 
	 * @throws Throwable
	 */
	protected Model initModel() {
		
		/**
		 * Initialization
		 */
		SBMLDocument doc = new SBMLDocument(2, 4);
		model = doc.createModel("biuni_model");
		Compartment c = model.createCompartment("c1");
		System.out.println(model.getSpeciesReferenceCount());
		Species s1 = model.createSpecies("s1", c);
		Species s2 = model.createSpecies("s2", c);
		Species p1 = model.createSpecies("p1", c);
		
		/**
		 * Reaction with two substrates and one product. No modifiers (later added).
		 */
		r1 = model.createReaction("r1");
		r1.setReversible(false);
		s1ref = r1.createReactant(s1);
		r1.createReactant(s2);
		r1.createProduct(p1);
		
		return model;
	}
	
	/*
	 * Tests for reactions like A + B -> C
	 */
	
	/**
	 * Test for the {@link CommonModularRateLaw} for A + B -> C 
	 * @throws Throwable
	 */
	@Test
	public void testCommonModularRateLaw() throws Throwable {
		KineticLaw kl = klg.createKineticLaw(r1, CommonModularRateLaw.class, false);
		test(kl, "vmax_r1*(s1*c1/kmc_r1_s1)^(hco_r1)*(s2*c1/kmc_r1_s2)^(hco_r1)/((1+s1*c1/kmc_r1_s1)^(hco_r1)*(1+s2*c1/kmc_r1_s2)^(hco_r1)+(1+p1*c1/kmc_r1_p1)^(hco_r1)-1)");
	}
	
	/**
	 * Test for the {@link DirectBindingModularRateLaw} for A + B -> C 
	 * @throws Throwable
	 */
	@Test
	public void testDirectBindingModularRateLaw() throws Throwable {
		KineticLaw kl = klg.createKineticLaw(r1, DirectBindingModularRateLaw.class, false);
		test(kl, "vmax_r1*(s1*c1/kmc_r1_s1)^(hco_r1)*(s2*c1/kmc_r1_s2)^(hco_r1)/(1+(s1*c1/kmc_r1_s1)^(hco_r1)*(s2*c1/kmc_r1_s2)^(hco_r1)+(p1*c1/kmc_r1_p1)^(hco_r1))");
	}
	
	/**
	 * Test for the {@link PowerLawModularRateLaw} for A + B -> C 
	 * @throws Throwable
	 */
	@Test
	public void testPowerLawModularRateLaw() throws Throwable {
		KineticLaw kl = klg.createKineticLaw(r1, PowerLawModularRateLaw.class, false);
		test(kl, "vmax_r1*(s1*c1/kmc_r1_s1)^(hco_r1)*(s2*c1/kmc_r1_s2)^(hco_r1)");
	}
	
	/**
	 * Test for the {@link SimultaneousBindingModularRateLaw} for A + B -> C 
	 * @throws Throwable
	 */
	@Test
	public void testSimultaneousBindingModularRateLaw() throws Throwable {
		KineticLaw kl = klg.createKineticLaw(r1, SimultaneousBindingModularRateLaw.class, false);
		test(kl, "vmax_r1*(s1*c1/kmc_r1_s1)^(hco_r1)*(s2*c1/kmc_r1_s2)^(hco_r1)/((1+s1*c1/kmc_r1_s1)^(hco_r1)*(1+s2*c1/kmc_r1_s2)^(hco_r1)*(1+p1*c1/kmc_r1_p1)^(hco_r1))");
	}
	
	/**
	 * Test for the {@link ForceDependentModularRateLaw} for A + B -> C 
	 * @throws Throwable
	 */
	@Test
	public void testForceDependentModularRateLaw() throws Throwable {
		KineticLaw kl = klg.createKineticLaw(r1, ForceDependentModularRateLaw.class, false);
		test(kl, "vmax_r1*(s1*c1/kmc_r1_s1)^(hco_r1)*(s2*c1/kmc_r1_s2)^(hco_r1)/((s1*c1/kmc_r1_s1)^(hco_r1)*(s2*c1/kmc_r1_s2)^(hco_r1)*(p1*c1/kmc_r1_p1)^(hco_r1))^(0.5)");
	}
	
	/**
	 * Test for the {@link OrderedMechanism} for A + B -> C 
	 * @throws Throwable
	 */
	@Test
	public void testOrderedMech() throws Throwable {
		KineticLaw kl = klg.createKineticLaw(r1, OrderedMechanism.class, false);
		test(kl, "vmax_r1*(s1*c1/kmc_r1_s1)^(hco_r1)*(s2*c1/kmc_r1_s2)^(hco_r1)/((1+s1*c1/kmc_r1_s1)^(hco_r1)*(1+s2*c1/kmc_r1_s2)^(hco_r1)+(1+p1*c1/kmc_r1_p1)^(hco_r1)-1)");
	}
	
	
	/*
	 * From here: Reversible reactions
	 */
	
	/**
	 * Test for the {@link CommonModularRateLaw} for A + B <-> C 
	 * @throws Throwable
	 */
	@Test
	public void testCommonModularRateLawRev() throws Throwable {
		KineticLaw kl = klg.createKineticLaw(r1, CommonModularRateLaw.class, true);
		test(kl, "(vmaf_r1*(s1*c1/kmc_r1_s1)^(hco_r1)*(s2*c1/kmc_r1_s2)^(hco_r1)-vmar_r1*(p1*c1/kmc_r1_p1)^(hco_r1))/((1+s1*c1/kmc_r1_s1)^(hco_r1)*(1+s2*c1/kmc_r1_s2)^(hco_r1)+(1+p1*c1/kmc_r1_p1)^(hco_r1)-1)");
	}
	
	/**
	 * Test for the {@link DirectBindingModularRateLaw} for A + B <-> C 
	 * @throws Throwable
	 */
	@Test
	public void testDirectBindingModularRateLawRev() throws Throwable {
		KineticLaw kl = klg.createKineticLaw(r1, DirectBindingModularRateLaw.class, true);
		test(kl, "(vmaf_r1*(s1*c1/kmc_r1_s1)^(hco_r1)*(s2*c1/kmc_r1_s2)^(hco_r1)-vmar_r1*(p1*c1/kmc_r1_p1)^(hco_r1))/(1+(s1*c1/kmc_r1_s1)^(hco_r1)*(s2*c1/kmc_r1_s2)^(hco_r1)+(p1*c1/kmc_r1_p1)^(hco_r1))");
	}
	
	/**
	 * Test for the {@link SimultaneousBindingModularRateLaw} for A + B <-> C 
	 * @throws Throwable
	 */
	@Test
	public void testSimultaneousBindingModularRateLawRev() throws Throwable {
		KineticLaw kl = klg.createKineticLaw(r1, SimultaneousBindingModularRateLaw.class, true);
		test(kl, "(vmaf_r1*(s1*c1/kmc_r1_s1)^(hco_r1)*(s2*c1/kmc_r1_s2)^(hco_r1)-vmar_r1*(p1*c1/kmc_r1_p1)^(hco_r1))/((1+s1*c1/kmc_r1_s1)^(hco_r1)*(1+s2*c1/kmc_r1_s2)^(hco_r1)*(1+p1*c1/kmc_r1_p1)^(hco_r1))");
	}
	
	/**
	 * Test for the {@link ForeDependentModularRateLaw} for A + B <-> C 
	 * @throws Throwable
	 */
	@Test
	public void testForceDependentRateLawRev() throws Throwable {
		KineticLaw kl = klg.createKineticLaw(r1, ForceDependentModularRateLaw.class, true);
		test(kl, "(vmaf_r1*(s1*c1/kmc_r1_s1)^(hco_r1)*(s2*c1/kmc_r1_s2)^(hco_r1)-vmar_r1*(p1*c1/kmc_r1_p1)^(hco_r1))/((s1*c1/kmc_r1_s1)^(hco_r1)*(s2*c1/kmc_r1_s2)^(hco_r1)*(p1*c1/kmc_r1_p1)^(hco_r1))^(0.5)");
	}
	
	/**
	 * Test for the {@link PowerLawModularRateLaw} for A + B <-> C 
	 * @throws Throwable
	 */
	@Test
	public void testPowerLawModularRateLawRev() throws Throwable {
		KineticLaw kl = klg.createKineticLaw(r1, PowerLawModularRateLaw.class, true);
		test(kl, "vmaf_r1*(s1*c1/kmc_r1_s1)^(hco_r1)*(s2*c1/kmc_r1_s2)^(hco_r1)-vmar_r1*(p1*c1/kmc_r1_p1)^(hco_r1)");
	}
	
	/**
	 * From here tests with Reactions like 2A -> C
	 */
	
	/**
	 * Test for the {@link CommonModularRateLaw} for 2A -> C
	 * @throws Throwable
	 */
	@Test
	public void testStoch2CommonModularRateLaw() throws Throwable {
		r1.removeReactant("s2");
		s1ref.setStoichiometry(2.0);
		r1.setReversible(false);
		klg = new KineticLawGenerator(model);
		KineticLaw kl = klg.createKineticLaw(r1, CommonModularRateLaw.class, false);
		test(kl, "vmax_r1*(s1*c1/kmc_r1_s1)^(2*hco_r1)/((1+s1*c1/kmc_r1_s1)^(2*hco_r1)+(1+p1*c1/kmc_r1_p1)^(hco_r1)-1)");
	}
	
	/**
	 * Test for the {@link DirectBindingModularRateLaw} for 2A -> C
	 * @throws Throwable
	 */
	@Test
	public void testStoch2DirectBindingModularRateLaw() throws Throwable {
		klg = new KineticLawGenerator(model);
		KineticLaw kl = klg.createKineticLaw(r1, DirectBindingModularRateLaw.class, false);
		test(kl, "vmax_r1*(s1*c1/kmc_r1_s1)^(2*hco_r1)/(1+(s1*c1/kmc_r1_s1)^(2*hco_r1)+(p1*c1/kmc_r1_p1)^(hco_r1))");
	}
	
	/**
	 * Test for the {@link PowerLawModularRateLaw} for 2A -> C
	 * @throws Throwable
	 */
	@Test
	public void testStoch2PowerLawModularRateLaw() throws Throwable {
		klg = new KineticLawGenerator(model);
		KineticLaw kl = klg.createKineticLaw(r1, PowerLawModularRateLaw.class, false);
		test(kl, "vmax_r1*(s1*c1/kmc_r1_s1)^(2*hco_r1)");
	}
	
	/**
	 * Test for the {@link SimultaneousBindingModularRateLaw} for 2A -> C
	 * @throws Throwable
	 */
	@Test
	public void testStoch2SimultaneousBindingModularRateLaw() throws Throwable {
		klg = new KineticLawGenerator(model);
		KineticLaw kl = klg.createKineticLaw(r1, SimultaneousBindingModularRateLaw.class, false);
		test(kl, "vmax_r1*(s1*c1/kmc_r1_s1)^(2*hco_r1)/((1+s1*c1/kmc_r1_s1)^(2*hco_r1)*(1+p1*c1/kmc_r1_p1)^(hco_r1))");
	}
	
	/**
	 * Test for the {@link ForeDependentModularRateLaw} for 2A -> C
	 * @throws Throwable
	 */
	@Test
	public void testStoch2ForceDependentModularRateLaw() throws Throwable {
		klg = new KineticLawGenerator(model);
		KineticLaw kl = klg.createKineticLaw(r1, ForceDependentModularRateLaw.class, false);
		test(kl, "vmax_r1*(s1*c1/kmc_r1_s1)^(2*hco_r1)/((s1*c1/kmc_r1_s1)^(hco_r1*2)*(p1*c1/kmc_r1_p1)^(hco_r1))^(0.5)");
	}
	
	/**
	 * Irreversible Reactions like 2A <-> C
	 */
	
	/**
	 * Test for the {@link CommonModularRateLaw} for 2A <-> C
	 * @throws Throwable
	 */
	@Test
	public void testStoch2CommonModularRateLawRev() throws Throwable {
		klg = new KineticLawGenerator(model);
		KineticLaw kl = klg.createKineticLaw(r1, CommonModularRateLaw.class, true);
		test(kl, "(vmaf_r1*(s1*c1/kmc_r1_s1)^(2*hco_r1)-vmar_r1*(p1*c1/kmc_r1_p1)^(hco_r1))/((1+s1*c1/kmc_r1_s1)^(2*hco_r1)+(1+p1*c1/kmc_r1_p1)^(hco_r1)-1)");
	}
	
	/**
	 * Test for the {@link DirectBindingModularRateLaw} for 2A <-> C
	 * @throws Throwable
	 */
	@Test
	public void testStoch2DirectBindingModularRateLawRev() throws Throwable {
		klg = new KineticLawGenerator(model);
		KineticLaw kl = klg.createKineticLaw(r1, DirectBindingModularRateLaw.class, true);
		test(kl, "(vmaf_r1*(s1*c1/kmc_r1_s1)^(2*hco_r1)-vmar_r1*(p1*c1/kmc_r1_p1)^(hco_r1))/(1+(s1*c1/kmc_r1_s1)^(2*hco_r1)+(p1*c1/kmc_r1_p1)^(hco_r1))");
	}
	
	/**
	 * Test for the {@link PowerLawModularRateLaw} for 2A <-> C
	 * @throws Throwable
	 */
	@Test
	public void testStoch2PowerLawModularRateLawRev() throws Throwable {
		klg = new KineticLawGenerator(model);
		KineticLaw kl = klg.createKineticLaw(r1, PowerLawModularRateLaw.class, true);
		test(kl, "vmaf_r1*(s1*c1/kmc_r1_s1)^(2*hco_r1)-vmar_r1*(p1*c1/kmc_r1_p1)^(hco_r1)");
	}
	
	/**
	 * Test for the {@link SimultaneousBindingModularRateLaw} for 2A <-> C
	 * @throws Throwable
	 */
	@Test
	public void testStoch2SimultaneousBindingModularRateLawRev() throws Throwable {
		klg = new KineticLawGenerator(model);
		KineticLaw kl = klg.createKineticLaw(r1, SimultaneousBindingModularRateLaw.class, true);
		test(kl, "(vmaf_r1*(s1*c1/kmc_r1_s1)^(2*hco_r1)-vmar_r1*(p1*c1/kmc_r1_p1)^(hco_r1))/((1+s1*c1/kmc_r1_s1)^(2*hco_r1)*(1+p1*c1/kmc_r1_p1)^(hco_r1))");
	}
	
	/**
	 * Test for the {@link ForeDependentModularRateLaw} for 2A <-> C
	 * @throws Throwable
	 */
	@Test
	public void testStoch2ForceDependentModularRateLawRev() throws Throwable {
		klg = new KineticLawGenerator(model);
		KineticLaw kl = klg.createKineticLaw(r1, ForceDependentModularRateLaw.class, true);
		test(kl, "(vmaf_r1*(s1*c1/kmc_r1_s1)^(2*hco_r1)-vmar_r1*(p1*c1/kmc_r1_p1)^(hco_r1))/((s1*c1/kmc_r1_s1)^(hco_r1*2)*(p1*c1/kmc_r1_p1)^(hco_r1))^(0.5)");
	}

}
