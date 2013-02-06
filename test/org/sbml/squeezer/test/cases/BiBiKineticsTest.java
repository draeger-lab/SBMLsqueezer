/*
 * $Id:  BiBiKineticsTest.java 3:02:18 PM jpfeuffer$
 * $URL$
 * ---------------------------------------------------------------------
 * This file is part of SBMLsqueezer, a Java program that creates rate 
 * equations for reactions in SBML files (http://sbml.org).
 *
 * Copyright (C) 2006-2013 by the University of Tuebingen, Germany.
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
import org.sbml.squeezer.UnitConsistencyType;
import org.sbml.squeezer.kinetics.CommonModularRateLaw;
import org.sbml.squeezer.kinetics.DirectBindingModularRateLaw;
import org.sbml.squeezer.kinetics.ForceDependentModularRateLaw;
import org.sbml.squeezer.kinetics.PowerLawModularRateLaw;
import org.sbml.squeezer.kinetics.SimultaneousBindingModularRateLaw;
import org.sbml.squeezer.kinetics.TypeStandardVersion;

/**
 * Test class to test reactions of the form A + B (<)-> C + D
 * @author Julianus Pfeuffer
 * @version $Rev$
 * @since 1.4
 */
public class BiBiKineticsTest extends KineticsTest {

	/**
	 * Initialization of species and reaction
	 */
	private Reaction r1;
	
	/* (non-Javadoc)
	 * @see org.sbml.squeezer.test.cases.KineticsTest#initModel()
	 */
	public Model initModel() {
		SBMLDocument doc = new SBMLDocument(2, 4);
		Model model = doc.createModel("bibi_model");
		Compartment c = model.createCompartment("c1");
		Species s1 = model.createSpecies("s1", c);
		Species s2 = model.createSpecies("s2", c);
		Species p1 = model.createSpecies("p1", c);
		Species p2 = model.createSpecies("p2", c);
		Species e1 = model.createSpecies("e1", c);
		
		for (Species s : model.getListOfSpecies()) {
			s.setHasOnlySubstanceUnits(false);
		}
		
		r1 = model.createReaction("r1");
		r1.setReversible(false);
		r1.createReactant(s1);
		r1.createReactant(s2);
		r1.createProduct(p1);
		r1.createProduct(p2);
		r1.createModifier(e1);
		return model;
	}
	
	/*===================================================
	   R E V E R S I B L E   R E A C T I O N S
	  ===================================================*/
	
	/**
	 * Tests the {@link CommonModularRateLaw} for the reaction A + B <-> C + D
	 * @throws Throwable
	 */
	@Test
	public void testCommonModularRateLawRev() throws Throwable{
		KineticLaw kl2 = klg.createKineticLaw(r1, CommonModularRateLaw.class, true, TypeStandardVersion.cat, UnitConsistencyType.amount, 1d);
		test(r1, kl2, "(vmaf_r1*(s1*c1/kmc_r1_s1)^hco_r1*(s2*c1/kmc_r1_s2)^hco_r1-vmar_r1*(p1*c1/kmc_r1_p1)^hco_r1*(p2*c1/kmc_r1_p2)^hco_r1)/((1+s1*c1/kmc_r1_s1)^hco_r1*(1+s2*c1/kmc_r1_s2)^hco_r1+(1+p1*c1/kmc_r1_p1)^hco_r1*(1+p2*c1/kmc_r1_p2)^hco_r1-1)");
	}
	
	/**
	 * Tests the {@link DirectBindingModularRateLaw} for the reaction A + B <-> C + D
	 * @throws Throwable
	 */
	@Test
	public void testDirectBindingModularRateLawRev() throws Throwable{
		KineticLaw kl = klg.createKineticLaw(r1, DirectBindingModularRateLaw.class, true, TypeStandardVersion.cat, UnitConsistencyType.amount, 1d);
		test(r1, kl, "(vmaf_r1*(s1*c1/kmc_r1_s1)^hco_r1*(s2*c1/kmc_r1_s2)^hco_r1-vmar_r1*(p1*c1/kmc_r1_p1)^hco_r1*(p2*c1/kmc_r1_p2)^hco_r1)/(1+(s1*c1/kmc_r1_s1)^hco_r1*(s2*c1/kmc_r1_s2)^hco_r1+(p1*c1/kmc_r1_p1)^hco_r1*(p2*c1/kmc_r1_p2)^hco_r1)");
	}
	
	/**
	 * Tests the {@link PowerLawModularRateLaw} for the reaction A + B <-> C + D
	 * @throws Throwable
	 */
	@Test
	public void testPowerLawModularRateLawRev() throws Throwable{
		KineticLaw kl = klg.createKineticLaw(r1, PowerLawModularRateLaw.class, true, TypeStandardVersion.cat, UnitConsistencyType.amount, 1d);
		test(r1, kl, "vmaf_r1*(s1*c1/kmc_r1_s1)^hco_r1*(s2*c1/kmc_r1_s2)^hco_r1-vmar_r1*(p1*c1/kmc_r1_p1)^hco_r1*(p2*c1/kmc_r1_p2)^hco_r1");
	}
	
	/**
	 * Tests the {@link PowerLawModularRateLaw} for the reaction A + B <-> C + D
	 * @throws Throwable
	 */
	@Test
	public void testPowerLawModularRateLawHalRev() throws Throwable{
		KineticLaw kl = klg.createKineticLaw(r1, PowerLawModularRateLaw.class, true, TypeStandardVersion.hal, UnitConsistencyType.amount, 1d);
		test(r1, kl, "vmag_r1*(keq_r1^hco_r1)^(0.5)*(s1*c1)^hco_r1*(s2*c1)^hco_r1/(kmc_r1_s1^hco_r1*kmc_r1_s2^hco_r1*kmc_r1_p1^hco_r1*kmc_r1_p2^hco_r1)^(0.5)-vmag_r1*1/(keq_r1^hco_r1)^(0.5)*(p1*c1)^hco_r1*(p2*c1)^hco_r1/(kmc_r1_s1^hco_r1*kmc_r1_s2^hco_r1*kmc_r1_p1^hco_r1*kmc_r1_p2^hco_r1)^(0.5)");
	}
	
	/**
	 * Tests the {@link PowerLawModularRateLaw} for the reaction A + B <-> C + D
	 * @throws Throwable
	 */
	@Test
	public void testPowerLawModularRateLawWegRev() throws Throwable{
		KineticLaw kl = klg.createKineticLaw(r1, PowerLawModularRateLaw.class, true, TypeStandardVersion.weg, UnitConsistencyType.amount, 1d);
		test(r1, kl, "(s1*c1)^hco_r1*(s2*c1)^hco_r1/e^(scp_s1*scp_s2*scp_p1*scp_p2*hco_r1/(2*T*R))*vmag_r1/(kmc_r1_s1^hco_r1*kmc_r1_s2^hco_r1*kmc_r1_p1^hco_r1*kmc_r1_p2^hco_r1)^(0.5)-(p1*c1)^hco_r1*(p2*c1)^hco_r1*e^(scp_s1*scp_s2*scp_p1*scp_p2*hco_r1/(2*T*R))*vmag_r1/(kmc_r1_s1^hco_r1*kmc_r1_s2^hco_r1*kmc_r1_p1^hco_r1*kmc_r1_p2^hco_r1)^(0.5)");
	}
	
	/**
	 * Tests the {@link SimultaneousBindingModularRateLaw} for the reaction A + B <-> C + D
	 * @throws Throwable
	 */
	@Test
	public void testSimultaneousBindingModularRateLawRev() throws Throwable{
		KineticLaw kl = klg.createKineticLaw(r1, SimultaneousBindingModularRateLaw.class, true, TypeStandardVersion.cat, UnitConsistencyType.amount, 1d);
		test(r1, kl, "(vmaf_r1*(s1*c1/kmc_r1_s1)^hco_r1*(s2*c1/kmc_r1_s2)^hco_r1-vmar_r1*(p1*c1/kmc_r1_p1)^hco_r1*(p2*c1/kmc_r1_p2)^hco_r1)/((1+s1*c1/kmc_r1_s1)^hco_r1*(1+s2*c1/kmc_r1_s2)^hco_r1*(1+p1*c1/kmc_r1_p1)^hco_r1*(1+p2*c1/kmc_r1_p2)^hco_r1)");
	}
	
	/**
	 * Tests the {@link ForceDependentModularRateLaw} for the reaction A + B <-> C + D
	 * @throws Throwable
	 */
	@Test
	public void testForceDependentModularRateLawRev() throws Throwable{
		KineticLaw kl = klg.createKineticLaw(r1, ForceDependentModularRateLaw.class, true, TypeStandardVersion.cat, UnitConsistencyType.amount, 1d);
		test(r1, kl, "(vmaf_r1*(s1*c1/kmc_r1_s1)^hco_r1*(s2*c1/kmc_r1_s2)^hco_r1-vmar_r1*(p1*c1/kmc_r1_p1)^hco_r1*(p2*c1/kmc_r1_p2)^hco_r1)/((s1*c1/kmc_r1_s1)^hco_r1*(s2*c1/kmc_r1_s2)^hco_r1*(p1*c1/kmc_r1_p1)^hco_r1*(p2*c1/kmc_r1_p2)^hco_r1)^(0.5)");
	}

}
